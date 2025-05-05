package com.lu.ddwyydemo04.Service;

import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.*;
import com.lu.ddwyydemo04.Service.DQE.DQEproblemMoudleService;
import com.lu.ddwyydemo04.controller.testManIndexController;
import com.lu.ddwyydemo04.dao.DQE.DQEDao;
import com.lu.ddwyydemo04.pojo.Samples;
import com.lu.ddwyydemo04.pojo.TaskNode;
import com.lu.ddwyydemo04.pojo.User;
import com.taobao.api.ApiException;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service("AccessTokenService")
public class AccessTokenService {
    @Value("${dingtalk.appKey}")
    private String APP_KEY;

    @Value("${dingtalk.appSecret}")
    private String APP_SECRET;

    @Value("${dingtalk.agentid}")
    private Long AGENT_ID;


    @Autowired
    private DQEDao dqeDao;

    private static final Logger logger = LoggerFactory.getLogger(testManIndexController.class);

    private static final String GET_TOKEN_URL = "https://oapi.dingtalk.com/gettoken";


    private static final String SEND_MESSAGE_URL = "https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2";

    private static final String GET_SEND_RESULT_URL = "https://oapi.dingtalk.com/topapi/message/corpconversation/getsendresult";

    private static final String DING_TALK_URL = "https://oapi.dingtalk.com/topapi/message/corpconversation/status_bar/update";

    @Autowired
    private DQEproblemMoudleService dqeproblemMoudleService;

    public String getAccessToken() throws ApiException {
        DefaultDingTalkClient client = new DefaultDingTalkClient(GET_TOKEN_URL);
        OapiGettokenRequest request = new OapiGettokenRequest();
        request.setAppkey(APP_KEY);
        request.setAppsecret(APP_SECRET);
        request.setHttpMethod("GET");

        OapiGettokenResponse response = client.execute(request);
        if (response.getErrcode() == 0) {
            return response.getAccessToken();
        } else {
            throw new ApiException("Unable to get access_token, errcode: " + response.getErrcode() + ", errmsg: " + response.getErrmsg());
        }
    }

    // 根据dept_id去部门列表信息：部门ID: 971739387
    //部门名称: 蓝牙线材家居组
    //父部门ID: 523528658
    //是否自动添加用户: false
    //是否创建部门群: false
    public List<OapiV2DepartmentListsubResponse.DeptBaseResponse> getDeptListByDeptId(Long deptId) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/department/listsub");
        OapiV2DepartmentListsubRequest req = new OapiV2DepartmentListsubRequest();
        req.setDeptId(deptId);
        req.setLanguage("zh_CN");

        OapiV2DepartmentListsubResponse rsp = client.execute(req, getAccessToken());

        if (rsp.getErrcode() != 0) {
            throw new ApiException("Error: " + rsp.getErrmsg());
        }

        // 直接返回 result 列表
        return rsp.getResult();
    }

    //获取部门用户userid列表
    public List<String> getUserIdListByDeptId(Long deptId) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/user/listid");
        OapiUserListidRequest req = new OapiUserListidRequest();
        req.setDeptId(deptId);

        OapiUserListidResponse rsp = client.execute(req, getAccessToken());

        if (rsp.getErrcode() != 0) {
            throw new ApiException("Error: " + rsp.getErrmsg());
        }

        // 返回 userid_list
        return rsp.getResult().getUseridList();
    }

    public Map<String, String> getUsernameByUserid(String userid) throws ApiException {
        // 使用userId获取用户的详细信息
        DingTalkClient infoClient = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/get");
        OapiV2UserGetRequest infoReq = new OapiV2UserGetRequest();
        infoReq.setUserid(userid);
        infoReq.setLanguage("zh_CN");
        OapiV2UserGetResponse infoRsp = infoClient.execute(infoReq, getAccessToken());
//        System.out.println("infoRsp.getBody():"+infoRsp.getBody());
        // 提取所需的字段
        String username = extractParamOfResult(infoRsp.getBody(), "name");
        String jobNumber = extractParamOfResult(infoRsp.getBody(), "job_number");
        // result的create_time才是入职日期，我看了日志的
        String hire_date = extractParamOfResult(infoRsp.getBody(), "create_time");

        // 将结果存入Map
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", username);
        userInfo.put("job_number", jobNumber);
        userInfo.put("hire_date", hire_date);

        return userInfo;
    }



    //提取result里的参数param，想提取什么参数就写param，但是此处只提取不带多元素的,例如title是职位，name是姓名
    public static String extractParamOfResult(String userInfoJson,String param) {
        // 使用fastjson将JSON字符串解析为JSONObject对象
        JSONObject userInfo = JSONObject.parseObject(userInfoJson);

        // 获取result字段中的值
        JSONObject resultObj = userInfo.getJSONObject("result");
        if (resultObj != null) {
            // 获取name字段的值并返回
            String value = resultObj.getString(param);
            return value;
        } else {
            // 如果result字段为空，则返回空字符串或者其他默认值
            return "";
        }
    }


    // 根据用户名和部门id来遍历部门，直到匹配就返回userid
    public Long findUserIdByUsernameInDeptHierarchy(String receiver,  Samples sample,
                                                      String statusBarBgColor,String sender,String notify_time,
                                                    String warn_time,String personCharge) throws ApiException, UnsupportedEncodingException {

        //20241211写一个方法先去检查receiver是什么job，再来看页面跳转链接要不要触发点击搜索问题点操作
        String reveiverJob = getJobFromUsers(receiver);

        if (reveiverJob == null) {
            logger.info("接收者为空");
            // 根据业务逻辑决定是抛异常还是直接返回
            return 1L;
        }

        // 定义要发送的消息内容
        //下边这个链接很重要，OA消息发送的时候用户点击的话会跳转到工作台直接进入我的应用
//        String messageUrl = "dingtalk://dingtalkclient/action/openapp?corpid=ding39a9d20442a933ec35c2f4657eb6378f&container_type=work_platform&app_id=0_3078576183&redirect_type=jump&redirect_url=http://219.134.191.195:64000"; // 跳转链接
        int sampleId = sample.getSample_id(); // 获取 sample_id，类型为 int
        String baseUrl = "http://219.134.191.195:64000/problemMoudle?dd_orientation=landscape";
//        String baseUrl = "http://38knkf.natappfree.cc/problemMoudle?dd_orientation=landscape";
        String redirectUrl = String.format(
                "%s&sample_id=%d&username=%s&job=%s",
                baseUrl,
                sampleId,
                URLEncoder.encode(receiver, "UTF-8"), // 编码 username 参数
                URLEncoder.encode(reveiverJob, "UTF-8")       // 编码 job 参数
        );
        String messageUrl = String.format(
                "dingtalk://dingtalkclient/action/openapp?corpid=ding39a9d20442a933ec35c2f4657eb6378f&container_type=work_platform&app_id=0_3078576183&redirect_type=jump&redirect_url=%s",
                URLEncoder.encode(redirectUrl, "UTF-8")
        );

        String userId = getUserIdByName(receiver);
        System.out.println("userId:"+userId);

        OapiMessageCorpconversationAsyncsendV2Response response = sendDingTalkNotification(
                userId,
                getAccessToken(),
                sample.getFull_model(),
                sample.getSample_schedule(),
                sample.getSample_category(),
                sample.getVersion(),
                sample.getSample_name(),
                notify_time,
                warn_time,
                messageUrl,
                sender,statusBarBgColor,sample.getResult_judge(),sample.getRd_result_judge(),personCharge
        );

        // 处理发送结果
        if (response.getErrcode() == 0) {
            logger.info("OA消息发送成功，用户名：" +receiver +",用户ID: " + userId);
            return response.getTaskId(); // 返回 taskId
        } else {
            logger.info("OA消息发送失败，错误码: " + response.getErrcode() + "，错误信息: " + response.getErrmsg());
            return null; // 或者抛出异常，根据需要
        }

    }

    public OapiMessageCorpconversationAsyncsendV2Response sendDingTalkNotification(
            String userId,
            String accessToken,
            String fullModel,
            String sampleSchedule,
            String sampleCategory,
            String version,
            String sampleName,
            String notifyTime,
            String warn_time,
            String messageUrl,
            String sender,
            String statusBarBgColor,String result_judge,String rd_result_judge,String personCharge) throws ApiException {


        DingTalkClient client = new DefaultDingTalkClient(SEND_MESSAGE_URL);
        OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();

        request.setAgentId(AGENT_ID);
        request.setUseridList(userId);
        request.setToAllUser(false);

        // 设置消息内容
        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();

        // 区分发给卢绮敏的信息,卢健的userId是：2329612068682307，卢绮敏的是：210139201721546755
        if(userId.equals("210139201721546755")){
            msg.setMsgtype("text");

            // 创建 Text 类型的消息内容
            OapiMessageCorpconversationAsyncsendV2Request.Text text = new OapiMessageCorpconversationAsyncsendV2Request.Text();

            // 构建消息内容
            StringBuilder contentBuilder = new StringBuilder();

            // 添加基本状态信息
            contentBuilder.append("测试进度: ").append(returnSchedule(sampleSchedule)).append("\n");

            // 添加完整型号等其他信息
            contentBuilder.append("完整型号: ").append(fullModel).append("\n");
            contentBuilder.append("样品阶段: ").append(sampleCategory).append("\n");
            contentBuilder.append("版本: ").append(version).append("\n");
            contentBuilder.append("产品名称: ").append(sampleName).append("\n");
            contentBuilder.append("提交时间: ").append(notifyTime).append("\n");
            contentBuilder.append("报告提交者: ").append(sender).append("\n");

            // 设置消息内容
            text.setContent(contentBuilder.toString());
            // 设置消息内容到 Msg 对象
            msg.setText(text);
        }else{
            // 设置 OA 消息
            OapiMessageCorpconversationAsyncsendV2Request.OA oa = new OapiMessageCorpconversationAsyncsendV2Request.OA();
            msg.setMsgtype("oa");
            oa.setMessageUrl(messageUrl);
            oa.setPcMessageUrl(messageUrl);

            // 设置状态栏内容
            OapiMessageCorpconversationAsyncsendV2Request.StatusBar statusBar = new OapiMessageCorpconversationAsyncsendV2Request.StatusBar();
            StringBuilder statusValueBuilder = new StringBuilder();

            // 添加基本状态信息
            statusValueBuilder.append("测试进度: ").append(returnSchedule(sampleSchedule)).append("\r\n");

            // 设置状态栏的最终内容
            statusBar.setStatusValue(statusValueBuilder.toString());
            oa.setStatusBar(statusBar);
            oa.getStatusBar().setStatusBg(statusBarBgColor);

            // 设置消息头部
            OapiMessageCorpconversationAsyncsendV2Request.Head head = new OapiMessageCorpconversationAsyncsendV2Request.Head();
            head.setBgcolor("6699ff");  // 头部微应用程序名的字体颜色黄色
            head.setText("完整型号: " + fullModel);  // 头部标题
            oa.setHead(head);

            // 设置消息主体内容
            OapiMessageCorpconversationAsyncsendV2Request.Body body = new OapiMessageCorpconversationAsyncsendV2Request.Body();
            body.setTitle(fullModel + " 进度更新: " + returnSchedule(sampleSchedule));

            // 设置表单内容
            List<OapiMessageCorpconversationAsyncsendV2Request.Form> formList = new ArrayList<>();

            OapiMessageCorpconversationAsyncsendV2Request.Form form1 = new OapiMessageCorpconversationAsyncsendV2Request.Form();
            form1.setKey("完整型号：");
            form1.setValue(fullModel);
            formList.add(form1);

            OapiMessageCorpconversationAsyncsendV2Request.Form form2 = new OapiMessageCorpconversationAsyncsendV2Request.Form();
            form2.setKey("样品阶段：");
            form2.setValue(sampleCategory);
            formList.add(form2);

            OapiMessageCorpconversationAsyncsendV2Request.Form form3 = new OapiMessageCorpconversationAsyncsendV2Request.Form();
            form3.setKey("版本：");
            form3.setValue(version);
            formList.add(form3);

            OapiMessageCorpconversationAsyncsendV2Request.Form form4 = new OapiMessageCorpconversationAsyncsendV2Request.Form();
            form4.setKey("产品名称：");
            form4.setValue(sampleName);
            formList.add(form4);

            OapiMessageCorpconversationAsyncsendV2Request.Form form5 = new OapiMessageCorpconversationAsyncsendV2Request.Form();
            form5.setKey("通知时间：");
            form5.setValue(notifyTime);
            formList.add(form5);

            if (warn_time != null) { // 仅在 warn_time 不为空时添加
                OapiMessageCorpconversationAsyncsendV2Request.Form form6 = new OapiMessageCorpconversationAsyncsendV2Request.Form();
                form6.setKey("警报时间：");
                form6.setValue(warn_time);
                formList.add(form6);
            }

            if(rd_result_judge!=null){
                OapiMessageCorpconversationAsyncsendV2Request.Form form7 = new OapiMessageCorpconversationAsyncsendV2Request.Form();
                form7.setKey("研发承认结果：");
                form7.setValue(returnResult(rd_result_judge));
                formList.add(form7);
            }

            if(result_judge!=null){
                OapiMessageCorpconversationAsyncsendV2Request.Form form8 = new OapiMessageCorpconversationAsyncsendV2Request.Form();
                form8.setKey("DQE承认结果：");
                form8.setValue(returnResult(result_judge));
                formList.add(form8);
            }


            if(personCharge !=null && !Objects.equals(personCharge, "")){
                OapiMessageCorpconversationAsyncsendV2Request.Form form9 = new OapiMessageCorpconversationAsyncsendV2Request.Form();
                form9.setKey("节点负责人：");
                form9.setValue(personCharge);
                formList.add(form9);
            }

            body.setForm(formList);
            body.setAuthor(sender);

//            // 设置大段文本内容为指定格式
//        body.setContent(fullModel + " 测试进度为：------> " + sampleSchedule);

            oa.setBody(body);
            msg.setOa(oa);
        }
        request.setMsg(msg);

        // 发送请求
        return client.execute(request, accessToken);
    }

    public String returnSchedule(String sampleSchedule){
        if(Objects.equals(sampleSchedule, "0")){
            sampleSchedule = "测试中";
        }else if(Objects.equals(sampleSchedule, "1")){
            sampleSchedule = "待DQE和研发审核";
        }else if(Objects.equals(sampleSchedule, "2")){
            sampleSchedule = "审核完成";
        }
//        else if(Objects.equals(sampleSchedule, "3")){
//            sampleSchedule = "待DQE判定";
//        }else if(Objects.equals(sampleSchedule, "4")){
//            sampleSchedule = "已完成";
//        }
        else if(Objects.equals(sampleSchedule, "9")){
            sampleSchedule = "测试人员退样";
        }else if(Objects.equals(sampleSchedule, "10")){
            sampleSchedule = "测试人员竞品完成";
        }
        return sampleSchedule;
    }



//    /**
//     * 获取钉钉发送工作通知的结果
//     *
//     * @param agentId      应用的Agent ID
//     * @param taskId       任务的Task ID
//     * @param accessToken  钉钉的access token
//     * @return             发送结果的详细信息或错误信息
//     */
//    public String getDingTalkSendResult(Long agentId, Long taskId, String accessToken) {
//        DingTalkClient client = new DefaultDingTalkClient(GET_SEND_RESULT_URL);
//        OapiMessageCorpconversationGetsendresultRequest request = new OapiMessageCorpconversationGetsendresultRequest();
//
//        request.setAgentId(agentId);
//        request.setTaskId(taskId);
//
//        try {
//            OapiMessageCorpconversationGetsendresultResponse response = client.execute(request, accessToken);
//            System.out.println(response.getBody());
//            return response.getBody();
//        } catch (ApiException e) {
//            return "系统异常：" + e.getMessage();
//        }
//    }


    public List<User> findUsersByDeptId(Long deptId) throws ApiException {
        System.out.println("正在查找部门 ID: " + deptId); // 打印当前部门 ID

        List<String> userIdList = getUserIdListByDeptId(deptId); // 获取用户 ID 列表
        List<User> matchedUsers = new ArrayList<>(); // 用于存储匹配的用户

        // 查找用户 ID
        for (String userId : userIdList) {
            Map<String, String> userInfo = getUsernameByUserid(userId); // 获取用户名
            // 提取用户名和工号
            String username = userInfo.get("username");
            String jobNumber = userInfo.get("job_number");
            String hire_date = userInfo.get("hire_date");

            User user = new User();
            user.setJob_number(jobNumber);
            user.setHire_date(hire_date);
            user.setUserId(userId); // 设置用户 ID
            user.setUsername(username); // 设置用户名
            user.setDeptId(deptId); // 设置小部门 ID
            user.setMajorDeptId(deptId); // 暂时设置为小部门 ID，后面可以更新为大部门 ID
            user.setDepartmentName(getDepartmentNameByDeptId(deptId)); // 获取小部门名称
            matchedUsers.add(user); // 添加到结果列表
            System.out.println("找到用户 ID: " + userId + " 来自部门 ID: " + deptId + "，用户名: " + username); // 打印找到的用户信息
        }

        // 递归查找子部门
        List<OapiV2DepartmentListsubResponse.DeptBaseResponse> subDeptList = getDeptListByDeptId(deptId);
        for (OapiV2DepartmentListsubResponse.DeptBaseResponse subDept : subDeptList) {
            System.out.println("查找子部门 ID: " + subDept.getDeptId() + " (部门名称: " + subDept.getName() + ")"); // 打印子部门信息
            List<User> subDeptUsers = findUsersByDeptId(subDept.getDeptId()); // 递归调用
            for (User subDeptUser : subDeptUsers) {
                subDeptUser.setMajorDeptId(deptId); // 更新大部门 ID
                matchedUsers.add(subDeptUser); // 添加到结果列表
            }
        }

        return matchedUsers; // 返回所有用户的列表
    }


    // 新增获取部门名称的方法
    public String getDepartmentNameByDeptId(Long deptId) throws ApiException {
        // 使用部门 ID 获取部门的详细信息
        DingTalkClient infoClient = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/department/get");
        OapiV2DepartmentGetRequest infoReq = new OapiV2DepartmentGetRequest();
        infoReq.setDeptId(deptId);
        OapiV2DepartmentGetResponse infoRsp = infoClient.execute(infoReq, getAccessToken());
        return extractParamOfResult(infoRsp.getBody(), "name");
    }


//    @Scheduled(fixedRate = 1800000) // 30分钟 = 1800000毫秒
    @Scheduled(fixedRate = 1800000) // 30分钟 = 1800000毫秒
    public void checkOverdueIssues() {
        logger.info("开始执行定时数据库检查任务...");
        String personCharge = "";

        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        //查询dqe和rd主管设置的警示天数
        String notify_days_dqe = dqeproblemMoudleService.queryWarnDays("dqeManager");
        String notify_days_rd = dqeproblemMoudleService.queryWarnDays("rdManager");
        System.out.println("notify_days_dqe:"+notify_days_dqe);
        System.out.println("notify_days_rd:"+notify_days_rd);

        try {
            // 查询节点进度表是否出现第一次超时，返回列表
            List<Map<String, Object>> onceOverdueSampleIdsAndNodes = findOnceOverdueSampleIds(currentTime);
            String receiver = "检测进度发送错误";
            String statusBarBgColor= "0xFFF65E5E";
            String senderOnce = "系统第一次检测节点进度异常";
            String senderSecond = "系统第二次检测节点进度异常";

            // 打印结果
            for (Map<String, Object> row : onceOverdueSampleIdsAndNodes) {
                int id = (int)row.get("id");
                int sampleId = (int) row.get("sample_id");
                String nodeNumber = (String) row.get("node_number");
                // 获取 create_time 字段
                LocalDateTime createTime = (LocalDateTime) row.get("create_time");
                LocalDateTime warnTime = (LocalDateTime) row.get("warn_time");


                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                // 将 LocalDateTime 转换为 String
                String createTimeString = createTime.format(formatter);
                String warnTimeString = warnTime.format(formatter);


                List<Samples> sampleList = dqeproblemMoudleService.querySamples(String.valueOf(sampleId));
                Samples sample = sampleList.isEmpty() ? null : sampleList.get(0);

                if(nodeNumber.equals("1")){
                    senderOnce = "系统检测到此项目"+notify_days_dqe+"天进度没动，第一次超期发送此警示信息给DQE主管级别,节点负责人为："+sample.getSample_DQE();
                    receiver = "张华";
//                    receiver = "卢健";
                    personCharge = sample.getSample_DQE();

                }else if(nodeNumber.equals("2")){
                    senderOnce = "系统检测到此项目"+notify_days_rd+"天进度没动，第一次超期发送此警示信息给研发主管级别,节点负责人为:"+sample.getSample_Developer();
                    receiver = "钟海龙";
//                    receiver = "卢健";
                    personCharge = sample.getSample_Developer();
                }
//                else if (nodeNumber.equals("3")) {
//                    senderOnce = "系统检测到此项目"+notify_days_dqe+"天进度没动，第一次超期发送此警示信息给DQE主管级别,节点负责人为："+sample.getSample_DQE();
////                    receiver = "张华";
//                    receiver = "卢健";
//
//                }
                Long task_id_Once = findUserIdByUsernameInDeptHierarchy(receiver,sample,statusBarBgColor,senderOnce,createTimeString,warnTimeString,personCharge);
                System.out.println("id:"+id);
                int updateTaskNodesOnce = updateTaskNodesOnce(id, currentTime);
                if(updateTaskNodesOnce>0){
                    System.out.println("更新成功");
                }
            }

            int days_dqe = Integer.parseInt(notify_days_dqe); // 将字符串转换为整数
            int doubledDays_dqe = days_dqe * 2; // 计算两倍
            int days_rd = Integer.parseInt(notify_days_rd); // 将字符串转换为整数
            int doubledDays_rd = days_rd * 2; // 计算两倍
//            System.out.println("doubledDays_dqe:"+doubledDays_dqe);
//            System.out.println("doubledDays_rd:"+doubledDays_rd);
            // 计算 currentTime 减去 days_dqe 天数的时间点
            LocalDateTime currentTimeSecondDQE = currentTime.minusDays(doubledDays_dqe);
//            System.out.println("currentTimeSecondDQE:"+currentTimeSecondDQE);
            LocalDateTime currentTimeSecondRD = currentTime.minusDays(doubledDays_rd);
//            System.out.println("currentTimeSecondRD:"+currentTimeSecondRD);

            // 查询节点进度表是否出现第二次超时，返回列表
            List<Map<String, Object>> secondOverdueSampleIdsAndNodesDQE = findSecondOverdueSampleIdsDQE(currentTimeSecondDQE);
            List<Map<String, Object>> secondOverdueSampleIdsAndNodesRD = findSecondOverdueSampleIdsRD(currentTimeSecondRD);

            // 合并两个列表
            List<Map<String, Object>> secondOverdueSampleIdsAndNodes = new ArrayList<>();
            secondOverdueSampleIdsAndNodes.addAll(secondOverdueSampleIdsAndNodesDQE);
            secondOverdueSampleIdsAndNodes.addAll(secondOverdueSampleIdsAndNodesRD);

            for (Map<String, Object> row : secondOverdueSampleIdsAndNodes) {
                int id = (int)row.get("id");
                int sampleId = (int) row.get("sample_id");
                String nodeNumber = (String) row.get("node_number");
                // 获取 create_time 字段
                LocalDateTime createTime = (LocalDateTime) row.get("create_time");
                LocalDateTime warnTime = (LocalDateTime) row.get("warn_time");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                // 将 LocalDateTime 转换为 String
                String createTimeString = createTime.format(formatter);
                String warnTimeString = warnTime.format(formatter);

                List<Samples> sampleList = dqeproblemMoudleService.querySamples(String.valueOf(sampleId));
                Samples sample = sampleList.isEmpty() ? null : sampleList.get(0);

                if(nodeNumber.equals("1")){
                    senderSecond = "系统检测到此项目"+doubledDays_dqe+"天进度没动，属于第二次超期，发送此警示信息给对应的经理级别，节点负责人为:"+sample.getSample_DQE();
                    receiver = "黄家灿";
//                    receiver = "卢健";

                    personCharge = sample.getSample_DQE();
                }else if(nodeNumber.equals("2")){
                    senderSecond = "系统检测到此项目"+doubledDays_rd+"天进度没动，属于第二次超期，发送此警示信息给对应的经理级别，节点负责人为："+sample.getSample_Developer();
                    receiver = "肖政文";
//                    receiver = "卢健";

                    personCharge = sample.getSample_Developer();
                }
//                else if (nodeNumber.equals("3")) {
//                    senderSecond = "系统检测到此项目"+doubledDays_dqe+"天进度没动，属于第二次超期，发送此警示信息给对应的经理级别，节点负责人为："+sample.getSample_DQE();
////                    receiver = "黄家灿";
//                    receiver = "卢健";
//                }
                Long task_id_OSecond = findUserIdByUsernameInDeptHierarchy(receiver,sample,statusBarBgColor,senderSecond,createTimeString,warnTimeString, personCharge);
//                System.out.println("task_id_OSecond:"+task_id_OSecond);
                int updateTaskNodesSecond = updateTaskNodesSecond(id, currentTime);
                if(updateTaskNodesSecond>0){
                    System.out.println("第二次超期的数据更新数据库成功");
                }
            }

            if (!secondOverdueSampleIdsAndNodes.isEmpty()) {
                // 返回给前端的逻辑，以下只是一个示例
                logger.info("发现第二次超期的sample_id: {}", secondOverdueSampleIdsAndNodes);
                // 可以添加返回前端的逻辑，比如通过WebSocket推送或更新缓存
            } else {
                logger.info("没有发现第二次超期的记录");
            }

        } catch (Exception e) {
            logger.error("执行定时数据库检查任务时出错: ", e);
        }
    }



//    @Scheduled(cron = "0 0 0 * * ?") // 每天午夜12点执行,0 43 14是北京时间的 14：43
    @Scheduled(cron = "0 17 3 * * ?") // 每天北京时间的 10:12
    public void refreshUserIds() throws ApiException {
        List<Long> targetDeptIds = Arrays.asList(62712385L, 523528658L, 62632390L); // 示例大部门 ID
//        List<Long> targetDeptIds = Arrays.asList( 523528658L); // 示例大部门 ID
        List<User> allUsers = new ArrayList<>(); // 用于存放所有用户的列表

        for (Long targetDeptId : targetDeptIds) {
            List<User> users = findUsersByDeptId(targetDeptId); // 获取用户 ID 和用户名列表
            allUsers.addAll(users); // 将当前部门的用户添加到总列表中
        }

        // 用于存储用户的职位，保证每个用户只有一个角色
        Map<String, User> userMap = new HashMap<>();

        for (User user : allUsers) {
            String userId = user.getUserId();
            Long majorDeptId = user.getMajorDeptId(); // 获取大部门 ID
            Long deptId = user.getDeptId(); // 获取小部门 ID

            // 特殊条件设置：产品经营部的耳机组（925840291和925828219）中，除了高玄英和姜呈祥，其他人都设为 job=rd
            if (majorDeptId.equals(62632390L) && (deptId.equals(925840291L) || deptId.equals(925828219L))) {
                if (user.getUsername().equals("高玄英") || user.getUsername().equals("姜呈祥")) {
                    user.setPosition("projectLeader");
                } else {
                    user.setPosition("rd");
                }
            } else if (user.getUsername().equals("卢健") || user.getUsername().equals("卢绮敏")) {
                user.setPosition("DQE"); // 特殊条件：如果是卢健或者卢绮敏，则职位为 DQE
            } else if (user.getUsername().equals("官旺华") || user.getUsername().equals("赵梓宇") || user.getUsername().equals("刘鹏飞")) {
                user.setPosition("tester"); // 特殊条件：如果是官旺华，则职位为 tester,因为他被放到了电子DQE租
            }else if (user.getUsername().equals("蓝明城")) {
                user.setPosition("DQE");
            }  else if (majorDeptId.equals(62712385L)) { // 产品研发部
                user.setPosition("rd");
            } else if (majorDeptId.equals(523528658L)) { // 电子DQE组
                // 进一步检查是否在测试组
                if (user.getDepartmentName() != null && user.getDepartmentName().equals("测试组")) {
                    user.setPosition("tester");
                } else {
                    user.setPosition("DQE");
                }
            } else if (majorDeptId.equals(62632390L)) { // 产品经营部
                user.setPosition("projectLeader");
            }

            // 根据优先级决定存储
            if (userMap.containsKey(userId)) {
                User existingUser = userMap.get(userId);
                // 优先级：projectLeader = rd > tester > DQE
                if (user.getPosition().equals("projectLeader") || user.getPosition().equals("rd")) {
                    userMap.put(userId, user); // 新增或保留 RD
                } else if (existingUser.getPosition().equals("DQE") && user.getPosition().equals("tester")) {
                    userMap.put(userId, user); // 覆盖为 tester
                }
            } else {
                userMap.put(userId, user); // 新增用户
            }
        }

        // 手动插入指定的两位用户信息
        User huangJiaCan = new User();
        huangJiaCan.setUserId("026611696339815501");
        huangJiaCan.setUsername("黄家灿");
        huangJiaCan.setDeptId(63652303L);
        huangJiaCan.setMajorDeptId(63652303L);
        huangJiaCan.setDepartmentName("品质工程部");
        huangJiaCan.setPosition("manager");

        User rongChengYu = new User();
        rongChengYu.setUserId("093738071833125882");
        rongChengYu.setUsername("荣成彧");
        rongChengYu.setDeptId(90070106L);
        rongChengYu.setMajorDeptId(90070106L);
        rongChengYu.setDepartmentName("产品研发中心");
        rongChengYu.setPosition("manager");

        userMap.put(huangJiaCan.getUserId(), huangJiaCan);
        userMap.put(rongChengYu.getUserId(), rongChengYu);

        // 打印所有用户的配对和职位信息
        System.out.println("所有用户的配对列表及其职位: ");
        for (User user : userMap.values()) {
            System.out.println("用户ID: " + user.getUserId() + ", 用户名: " + user.getUsername() + ", 部门名称: " + user.getDepartmentName() + ", 职位: " + user.getPosition());
        }

        // 保存用户到数据库
        for (User user : userMap.values()) {
            dqeDao.insertOrUpdateUser(user); // 保存用户到数据库
        }

    }



    public String returnResult(String judge){
        if (judge.equals("0") ) {
            judge = "签样";
        } else if (judge.equals("1")) {
            judge = "退样";
        } else if (judge.equals("2")) {
            judge = "限收";
        } else if (judge.equals("3")) {
            judge = "特采";
        } else if (judge.equals("4")) {
            judge = "会议评审接受";
        }
        return judge;
    }



    public String getUserIdByName(String username){
        return dqeDao.getUserIdByName(username);
    }

    //获取工作通知消息的发送进度,调用本接口，只能获取24小时内工作通知消息的发送进度。
    public String getOASchedule(Long taskId) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/getsendprogress");
        OapiMessageCorpconversationGetsendprogressRequest req = new OapiMessageCorpconversationGetsendprogressRequest();
        req.setAgentId(AGENT_ID);
        req.setTaskId(taskId);
        OapiMessageCorpconversationGetsendprogressResponse rsp = client.execute(req, getAccessToken());
        System.out.println(rsp.getBody());
        return rsp.getErrmsg();
    }


    //更新OA消息的状态栏
    public String updateStatusBar(Long taskId, String statusValue, String statusBg, Samples sample) {
        DingTalkClient client = new DefaultDingTalkClient(DING_TALK_URL);
        OapiMessageCorpconversationStatusBarUpdateRequest request = new OapiMessageCorpconversationStatusBarUpdateRequest();
        request.setAgentId(AGENT_ID);

        // 使用 StringBuilder 构建状态信息
        StringBuilder statusValueBuilder = new StringBuilder();
        statusValueBuilder.append("测试进度：").append(returnSchedule(statusValue)).append("\r\n");

        // 添加其他信息（假设您有需要添加的判定信息）
        if (sample.getRd_result_judge() != null) {
            statusValueBuilder.append("研发判定：").append(returnResult(sample.getRd_result_judge())).append("\r\n");
        }

        if (sample.getResult_judge() != null) {
            statusValueBuilder.append("DQE判定：").append(returnResult(sample.getResult_judge())).append("\r\n");
        }

        // 设置最终状态值
        request.setStatusValue(statusValueBuilder.toString());
        request.setStatusBg(statusBg);
        request.setTaskId(taskId);

        try {
            OapiMessageCorpconversationStatusBarUpdateResponse response = client.execute(request, getAccessToken());
            return response.getErrmsg();
        } catch (Exception e) {
            e.printStackTrace();
            return "更新状态栏失败: " + e.getMessage();
        }
    }

    // 每天凌晨3:10执行
    @Scheduled(cron = "0 30 4 * * ?")
    public void syncEngineers() {
        String[] engineerNames = {
                "郭喆", "游宏", "殷嘉俊", "唐日顺", "官旺华", "刘鹏飞", "赵梓宇", "段平", "魏民",
                "程奕阳", "赵爽", "罗清虎", "龙运", "黄兰姣", "李智龙", "肖灶炜", "肖龙生", "张国鹏",
                "戴杏华", "李素欣1", "张锐", "阮晓晴", "廖建伟", "蔡义会"
        }; // 测试人员数组

        // 定义组别
        Map<String, String> groupMap = new HashMap<>();
        groupMap.put("龙运", "线材组");
        groupMap.put("张国鹏", "线材组");
        groupMap.put("唐日顺", "新人");
        groupMap.put("殷嘉俊", "新人");
        groupMap.put("赵梓宇", "新人");
        groupMap.put("游宏", "新人");
        groupMap.put("郭喆", "新人");
        groupMap.put("戴杏华", "视频组");
        groupMap.put("李智龙", "视频组");
        groupMap.put("魏民", "视频组");
        groupMap.put("肖灶炜", "视频组");
        groupMap.put("李素欣1", "视频组");
        groupMap.put("程奕阳", "视频组");
        groupMap.put("张锐", "数据网通组");
        groupMap.put("赵爽", "数据网通组");
        groupMap.put("肖龙生", "数据网通组");
        groupMap.put("黄兰姣", "蓝牙组");
        groupMap.put("刘鹏飞", "蓝牙组");
        groupMap.put("段平", "蓝牙组");
        groupMap.put("罗清虎", "蓝牙组");
        groupMap.put("阮晓晴", "耳机组");
        groupMap.put("官旺华", "耳机组");
        groupMap.put("廖建伟", "高频组");
        groupMap.put("蔡义会", "高频组");

        for (String name : engineerNames) {
            Map<String, String> userInfo = findUserByUsername(name);

            if (userInfo != null) {
                String engineerId = userInfo.get("engineerId");
                String testEngineerName = userInfo.get("testEngineerName");
                String hire_date = userInfo.get("hire_date");
                String responsible_category = groupMap.getOrDefault(testEngineerName, null); // 获取组别

                Integer count = countEngineerByName(testEngineerName);

                if (count != null && count > 0) {
                    // 已存在，更新
                    updateEngineer(engineerId, testEngineerName, hire_date, responsible_category);
                } else {
                    // 不存在，插入
                    insertEngineer(engineerId, testEngineerName, hire_date, responsible_category);
                }
            }
        }
        logger.info("定时任务更新测试人员数据库已完毕!");
    }


    public Map<String, String> findUserByUsername(String username){
        return dqeDao.findUserByUsername(username);
    }

    // 查tb_test_engineer_info表，看工程师是不是已经存在
    public Integer countEngineerByName(String testEngineerName){
        return dqeDao.countEngineerByName(testEngineerName);
    }

    // 插入新工程师
    public void insertEngineer(String engineerId, String testEngineerName,String hire_date,String responsible_category){
        dqeDao.insertEngineer(engineerId,testEngineerName,hire_date,responsible_category);
    }

    // 更新已有工程师信息
    public void updateEngineer( String engineerId,String testEngineerName,String hire_date,String responsible_category){
        dqeDao.updateEngineer(engineerId,testEngineerName,hire_date,responsible_category);
    }





    public int insertTaskNode(TaskNode taskNode){
        return dqeDao.insertTaskNode(taskNode);
    }


    public int updatePreviousNodes(TaskNode taskNode){
        return dqeDao.updatePreviousNodes(taskNode);
    }

    public List<Long> selectTaskId(String sample_id){
        return dqeDao.selectTaskId(sample_id);
    }

    public List<Map<String, Object>> findOnceOverdueSampleIds(LocalDateTime  currentTime){
        return dqeDao.findOnceOverdueSampleIds(currentTime);
    }

    public int updateTaskNodesOnce(int id, LocalDateTime currentTime){
        return dqeDao.updateTaskNodesOnce(id, currentTime);
    }

    public int updateTaskNodesSecond(int id, LocalDateTime currentTime){
        return dqeDao.updateTaskNodesSecond(id,currentTime);
    }

    public List<Map<String, Object>> findSecondOverdueSampleIdsDQE(LocalDateTime  currentTime){
        return dqeDao.findSecondOverdueSampleIdsDQE(currentTime);
    }

    public List<Map<String, Object>> findSecondOverdueSampleIdsRD(LocalDateTime  currentTime){
        return dqeDao.findSecondOverdueSampleIdsRD(currentTime);
    }

    public int deleteTaskNodeBefore(int sample_id){
        return dqeDao.deleteTaskNodeBefore(sample_id);
    }

    public String getJobFromUsers(String username){
        return dqeDao.getJobFromUsers(username);
    }


}
