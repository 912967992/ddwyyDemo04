package com.lu.ddwyydemo04.Service;

import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.*;
import com.lu.ddwyydemo04.Service.DQE.DQEproblemMoudleService;
import com.lu.ddwyydemo04.Service.DQE.ScheduleBoardService;
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
import java.time.DayOfWeek;
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

    @Autowired
    private ScheduleBoardService scheduleBoardService;

    public String getAccessToken() throws ApiException {
        int maxRetries = 2; // 最大重试次数（减少重试次数，避免用户等待过久）
        int retryDelay = 1000; // 重试延迟（毫秒，缩短延迟时间）
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.info("尝试获取钉钉AccessToken，第 {} 次尝试", attempt);
                DefaultDingTalkClient client = new DefaultDingTalkClient(GET_TOKEN_URL);
                OapiGettokenRequest request = new OapiGettokenRequest();
                request.setAppkey(APP_KEY);
                request.setAppsecret(APP_SECRET);
                request.setHttpMethod("GET");

                OapiGettokenResponse response = client.execute(request);
                if (response.getErrcode() == 0) {
                    logger.info("成功获取钉钉AccessToken");
                    return response.getAccessToken();
                } else {
                    String errorMsg = "Unable to get access_token, errcode: " + response.getErrcode() + ", errmsg: " + response.getErrmsg();
                    logger.error("获取AccessToken失败: {}", errorMsg);
                    throw new ApiException(errorMsg);
                }
            } catch (ApiException e) {
                String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                
                // 如果是业务错误（非网络错误），直接抛出
                if (errorMsg.contains("errcode") && !errorMsg.contains("timeout") && !errorMsg.contains("timed out")) {
                    throw e;
                }
                
                // 网络超时或其他网络错误，进行重试
                if (attempt < maxRetries) {
                    logger.warn("获取AccessToken失败（第 {} 次尝试），{} 毫秒后重试。错误信息: {}", 
                               attempt, retryDelay, e.getMessage());
                    try {
                        Thread.sleep(retryDelay);
                        retryDelay *= 2; // 指数退避：每次重试延迟时间翻倍
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ApiException("获取AccessToken时被中断", e);
                    }
                } else {
                    logger.error("获取AccessToken失败，已达到最大重试次数 {}。最后一次错误: {}", maxRetries, e.getMessage(), e);
                    throw new ApiException("获取AccessToken失败，已重试 " + maxRetries + " 次: " + e.getMessage(), e);
                }
            } catch (Exception e) {
                // 捕获其他可能的异常（如SocketTimeoutException等）
                String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                boolean isNetworkError = errorMsg.contains("timeout") || 
                                       errorMsg.contains("timed out") || 
                                       errorMsg.contains("connect") ||
                                       e.getClass().getSimpleName().contains("Timeout");
                
                if (isNetworkError && attempt < maxRetries) {
                    logger.warn("网络错误（第 {} 次尝试），{} 毫秒后重试。错误类型: {}, 错误信息: {}", 
                               attempt, retryDelay, e.getClass().getSimpleName(), e.getMessage());
                    try {
                        Thread.sleep(retryDelay);
                        retryDelay *= 2;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ApiException("获取AccessToken时被中断", e);
                    }
                } else {
                    logger.error("获取AccessToken时发生异常，第 {} 次尝试。错误类型: {}, 错误信息: {}", 
                                attempt, e.getClass().getSimpleName(), e.getMessage(), e);
                    throw new ApiException("获取AccessToken失败: " + e.getMessage(), e);
                }
            }
        }
        
        // 理论上不会到达这里，但为了编译通过
        throw new ApiException("获取AccessToken失败");
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
    public Map<String, Object> findUserIdByUsernameInDeptHierarchy(String receiver,  Samples sample,
                                                      String statusBarBgColor,String sender,String notify_time,
                                                    String warn_time,String personCharge) throws ApiException, UnsupportedEncodingException {

        //20241211写一个方法先去检查receiver是什么job，再来看页面跳转链接要不要触发点击搜索问题点操作
        String reveiverJob = getJobFromUsers(receiver);

        if (reveiverJob == null) {
            logger.info("接收者为空");
            Map<String, Object> result = new HashMap<>();
            result.put("taskId", 1L);
            result.put("messageUrl", null);
            return result;
        }
        // 定义要发送的消息内容
        //下边这个链接很重要，OA消息发送的时候用户点击的话会跳转到工作台直接进入我的应用
//        String sampleId = sample.getSample_id(); // 获取 sample_id，类型为 int
//        String baseUrl = "http://219.134.191.195:64000/problemMoudle?dd_orientation=landscape";
//        String redirectUrl = String.format(
//                "%s&sample_id=%s&username=%s&job=%s",
//                baseUrl,
//                sampleId,
//                URLEncoder.encode(receiver, "UTF-8"), // 编码 username 参数
//                URLEncoder.encode(reveiverJob, "UTF-8")       // 编码 job 参数
//        );
//        String messageUrl = String.format(
//                "dingtalk://dingtalkclient/action/openapp?corpid=ding39a9d20442a933ec35c2f4657eb6378f&container_type=work_platform&app_id=0_3078576183&redirect_type=jump&redirect_url=%s",
//                URLEncoder.encode(redirectUrl, "UTF-8")
//        );

        // 新增一个中转页面，获取username和job然后跳转到问题点页面
        String sampleId = sample.getSample_id(); // 获取 sample_id
        String redirectMidPageUrl = String.format(
                "http://219.134.191.195:64000/authRedirect?sample_id=%s", // 注意：sample_id 也传进去
                URLEncoder.encode(sampleId, "UTF-8")
        );

//        String redirectMidPageUrl = String.format(
//                "http://j77482b6.natappfree.cc/authRedirect?sample_id=%s", // 注意：sample_id 也传进去
//                URLEncoder.encode(sampleId, "UTF-8")
//        );

        String messageUrl = String.format(
                "dingtalk://dingtalkclient/action/openapp?corpid=ding39a9d20442a933ec35c2f4657eb6378f" +
                        "&container_type=work_platform&app_id=0_3078576183&redirect_type=jump&redirect_url=%s",
                URLEncoder.encode(redirectMidPageUrl, "UTF-8")
        );

//        String messageUrl = String.format(
//                "dingtalk://dingtalkclient/action/openapp?corpid=ding39a9d20442a933ec35c2f4657eb6378f" +
//                        "&container_type=work_platform&app_id=0_3152575892&redirect_type=jump&redirect_url=%s",
//                URLEncoder.encode(redirectMidPageUrl, "UTF-8")
//        );


        String userId = getUserIdByName(receiver);

//        System.out.println("messageUrl:"+messageUrl);

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
            logger.info("OA消息发送成功，用户名：" + receiver + ", 用户ID: " + userId);
            Map<String, Object> result = new HashMap<>();
            result.put("taskId", response.getTaskId());
            result.put("messageUrl", messageUrl);
            return result;
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

        // 区分发给卢绮敏的信息,卢健的userId是：2329612068682307，卢绮敏的是：210139201721546755 ， 许梦瑶：01555026451935233352
        if(userId.equals("210139201721546755") || userId.equals("16016618681230627") || userId.equals("01555026451935233352")){
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
                    senderOnce = "检测到此项目"+notify_days_dqe+"天进度没动，属于第一次超期";
//                    receiver = "张华";
                    receiver = "李良健";
//                    receiver = "卢健";
                    personCharge = sample.getSample_DQE();

                }else if(nodeNumber.equals("2")){
                    senderOnce = "检测到此项目"+notify_days_rd+"天进度没动，属于第一次超期";
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
//                Long task_id_Once = findUserIdByUsernameInDeptHierarchy(receiver,sample,statusBarBgColor,senderOnce,createTimeString,warnTimeString,personCharge);
                Map<String, Object> resultMap = findUserIdByUsernameInDeptHierarchy(receiver, sample, statusBarBgColor, senderOnce, createTimeString, warnTimeString, personCharge);
                Long task_id_Once = (Long) resultMap.get("taskId");

                int updateTaskNodesOnce = updateTaskNodesOnce(id, currentTime);

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
                    senderSecond = "检测到此项目"+doubledDays_dqe+"天进度没动，属于第二次超期";
//                    receiver = "黄家灿";
//                    receiver = "卢健";
                    receiver = "";

                    personCharge = sample.getSample_DQE();
                }else if(nodeNumber.equals("2")){
                    senderSecond = "检测到此项目"+doubledDays_rd+"天进度没动，属于第二次超期";
//                    receiver = "肖政文";
//                    receiver = "卢健";
                    receiver = "";

                    personCharge = sample.getSample_Developer();
                }
//                else if (nodeNumber.equals("3")) {
//                    senderSecond = "系统检测到此项目"+doubledDays_dqe+"天进度没动，属于第二次超期，发送此警示信息给对应的经理级别，节点负责人为："+sample.getSample_DQE();
////                    receiver = "黄家灿";
//                    receiver = "卢健";
//                }
//                Long task_id_OSecond = findUserIdByUsernameInDeptHierarchy(receiver,sample,statusBarBgColor,senderSecond,createTimeString,warnTimeString, personCharge);

                Map<String, Object> resultMap_OSecond = findUserIdByUsernameInDeptHierarchy(receiver, sample, statusBarBgColor, senderSecond, createTimeString, warnTimeString, personCharge);
                Long task_id_OSecond = (Long) resultMap_OSecond.get("taskId");

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
    @Scheduled(cron = "0 25 06 * * ?")
    public void refreshUserIds() throws ApiException {
        // 20260108  62632390产品经营部去掉了，不存在钉钉了，暂时先去掉吧
        List<Long> targetDeptIds = Arrays.asList(62712385L, 523528658L,913639520L); // 示例大部门 ID,913639520是NAS网通组
//        List<Long> targetDeptIds = Arrays.asList(349996662L); // 示例大部门 ID,913639520是NAS网通组
//        List<Long> targetDeptIds = Arrays.asList(523528658L); // 示例大部门 ID,913639520是NAS网通组
//        List<Long> targetDeptIds = Arrays.asList( 63652303L); // 示例大部门 ID
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

            // 如果部门ID是1044809148，设置为tester
            if (deptId != null && deptId.equals(1044809148L)) {
                user.setPosition("tester");
            }
            // 特殊条件设置：产品经营部的耳机组（925840291和925828219）中，除了高玄英和姜呈祥，其他人都设为 job=rd
            else if (majorDeptId.equals(62632390L) && (deptId.equals(925840291L) || deptId.equals(925828219L))) {
                if (user.getUsername().equals("高玄英") || user.getUsername().equals("姜呈祥")) {
                    user.setPosition("projectLeader");
                } else {
                    user.setPosition("rd");
                }
            } else if (user.getUsername().equals("卢健") || user.getUsername().equals("卢绮敏") || user.getUsername().equals("李良健")) {
                user.setPosition("DQE"); // 特殊条件：如果是卢健或者卢绮敏，则职位为 DQE
            } else if (user.getUsername().equals("官旺华") || user.getUsername().equals("赵梓宇") || user.getUsername().equals("刘鹏飞")) {
                user.setPosition("tester"); // 特殊条件：如果是官旺华，则职位为 tester,因为他被放到了电子DQE租
            }else if (user.getUsername().equals("胡雪梅")) {
                user.setDepartmentName("数据储存");
                user.setPosition("DQE");
            } else if (majorDeptId.equals(913639520L)) { // NAS网通组
                user.setPosition("DQE");
                user.setDepartmentName("NAS网通组");
            } else if (majorDeptId.equals(62712385L) || majorDeptId.equals(349996662L)) { // 产品研发部
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
        huangJiaCan.setJob_number("2352");
        huangJiaCan.setPosition("manager");

        User rongChengYu = new User();
        rongChengYu.setUserId("093738071833125882");
        rongChengYu.setUsername("荣成彧");
        rongChengYu.setDeptId(90070106L);
        rongChengYu.setMajorDeptId(90070106L);
        rongChengYu.setJob_number("2756");
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
        } else if (judge.equals("5")) {
            judge = "验证样品合格";
        } else if (judge.equals("6")) {
            judge = "验证样品不合格";
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

    public int deleteTaskNodeBefore(String sample_id){
        return dqeDao.deleteTaskNodeBefore(sample_id);
    }

    public String getJobFromUsers(String username){
        return dqeDao.getJobFromUsers(username);
    }


    @Scheduled(fixedRate = 900000) // 15分钟 = 900000毫秒
    public void checkScheduleEndTime() {
        logger.info("开始执行排期结束时间检查任务...");
        
        try {
            // 获取当前时间
            LocalDateTime currentTime = LocalDateTime.now();
            // 计算1小时后的时间
            LocalDateTime oneHourLater = currentTime.plusHours(1);
            
            logger.info("当前时间: " + currentTime + ", 检查时间范围: " + oneHourLater.minusMinutes(15) + " 到 " + oneHourLater.plusMinutes(15));
            
            // 查询距离排期结束时间还有1小时的samples记录
            List<Samples> samplesToNotify = findSamplesNearScheduleEndTime(oneHourLater);
            
            logger.info("找到 " + samplesToNotify.size() + " 个样品需要发送通知");
            
            for (Samples sample : samplesToNotify) {
                try {
                    // 获取测试人员姓名
                    String tester = sample.getTester();
                    if (tester != null && !tester.trim().isEmpty()) {
                        // 发送通知给测试人员
                        sendScheduleEndTimeNotification(tester, sample);
                        logger.info("已发送排期结束时间通知给测试人员: " + tester + ", 样品ID: " + sample.getSample_id());
                    } else {
                        logger.warn("样品ID: " + sample.getSample_id() + " 的测试人员为空，跳过通知");
                    }
                } catch (Exception e) {
                    logger.error("发送排期结束时间通知失败，样品ID: " + sample.getSample_id() + ", 错误: " + e.getMessage(), e);
                }
            }
            
            if (!samplesToNotify.isEmpty()) {
                logger.info("本次检查发现 " + samplesToNotify.size() + " 个样品需要发送排期结束时间通知");
            } else {
                logger.info("本次检查没有发现需要发送排期结束时间通知的样品");
            }
            
        } catch (Exception e) {
            logger.error("执行排期结束时间检查任务时出错: ", e);
        }
    }
    
    /**
     * 查询距离排期结束时间还有1小时的samples记录
     */
    private List<Samples> findSamplesNearScheduleEndTime(LocalDateTime targetTime) {
        // 先获取所有有效的samples记录
        List<Samples> allSamples = dqeDao.findAllValidSamples();
        List<Samples> filteredSamples = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (Samples sample : allSamples) {
            try {
                String scheduleEndTimeStr = sample.getScheduleEndTime();
                if (scheduleEndTimeStr != null && !scheduleEndTimeStr.trim().isEmpty()) {
                    // 验证日期格式
                    if (scheduleEndTimeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                        LocalDateTime scheduleEndTime = LocalDateTime.parse(scheduleEndTimeStr, formatter);
                        
                        // 检查是否在目标时间前后15分钟范围内
                        LocalDateTime startTime = targetTime.minusMinutes(15);
                        LocalDateTime endTime = targetTime.plusMinutes(15);
                        
                        if (scheduleEndTime.isAfter(startTime) && scheduleEndTime.isBefore(endTime)) {
                            filteredSamples.add(sample);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("解析样品ID " + sample.getSample_id() + " 的排期结束时间失败: " + sample.getScheduleEndTime() + ", 错误: " + e.getMessage());
            }
        }
        
        return filteredSamples;
    }
    
    /**
     * 发送排期结束时间通知给测试人员
     */
    private void sendScheduleEndTimeNotification(String tester, Samples sample) throws ApiException, UnsupportedEncodingException {
        // 获取测试人员的userId
        String userId = getUserIdByName(tester);
        
        if (userId == null || userId.isEmpty()) {
            logger.warn("未找到测试人员 " + tester + " 的userId");
            return;
        }
        
        // 构建通知消息
        String messageContent = buildScheduleEndTimeMessage(sample);
        
        // 发送文本消息通知
        sendTextNotification(userId, messageContent);
    }
    
    /**
     * 构建排期结束时间通知消息
     */
    private String buildScheduleEndTimeMessage(Samples sample) {
        StringBuilder message = new StringBuilder();
        message.append("【排期提醒】\n");
        message.append("您的测试样品即将到达排期结束时间：\n");
        message.append("样品ID: ").append(sample.getSample_id()).append("\n");
        message.append("完整型号: ").append(sample.getFull_model()).append("\n");
        message.append("产品名称: ").append(sample.getSample_name()).append("\n");
        message.append("排期结束时间: ").append(sample.getScheduleEndTime()).append("\n");
        message.append("距离结束时间还有1小时，请及时完成测试工作！");
        
        return message.toString();
    }
    
    /**
     * 发送文本消息通知
     */
    public void sendTextNotification(String userId, String content) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(SEND_MESSAGE_URL);
        OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
        
        request.setAgentId(AGENT_ID);
        request.setUseridList(userId);
        request.setToAllUser(false);
        
        // 设置文本消息
        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setMsgtype("text");
        
        OapiMessageCorpconversationAsyncsendV2Request.Text text = new OapiMessageCorpconversationAsyncsendV2Request.Text();
        text.setContent(content);
        msg.setText(text);
        
        request.setMsg(msg);
        
        // 发送请求
        OapiMessageCorpconversationAsyncsendV2Response response = client.execute(request, getAccessToken());
        
        if (response.getErrcode() != 0) {
            logger.error("发送文本通知失败，错误码: " + response.getErrcode() + "，错误信息: " + response.getErrmsg());
        }
    }
    
    /**
     * 测试方法：验证SQL查询是否正常工作
     */
    public void testScheduleEndTimeQuery() {
        try {
            LocalDateTime testTime = LocalDateTime.now().plusHours(1);
            logger.info("开始测试排期结束时间检查，目标时间: " + testTime);
            
            // 先获取所有有效记录
            List<Samples> allSamples = dqeDao.findAllValidSamples();
            logger.info("数据库中共有 " + allSamples.size() + " 个有效样品记录");
            
            // 进行过滤
            List<Samples> samples = findSamplesNearScheduleEndTime(testTime);
            logger.info("过滤后找到 " + samples.size() + " 个需要通知的样品");
            
            for (Samples sample : samples) {
                logger.info("样品ID: " + sample.getSample_id() + 
                          ", 排期结束时间: " + sample.getScheduleEndTime() + 
                          ", 测试人员: " + sample.getTester() +
                          ", 完整型号: " + sample.getFull_model());
            }
        } catch (Exception e) {
            logger.error("测试查询失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 简单测试方法：只测试findAllValidSamples查询
     */
    public void testFindAllValidSamples() {
        try {
            logger.info("开始测试findAllValidSamples查询...");
            List<Samples> allSamples = dqeDao.findAllValidSamples();
            logger.info("查询成功，找到 " + allSamples.size() + " 个有效样品记录");
            
            // 显示前5个记录的scheduleEndTime值
            int count = 0;
            for (Samples sample : allSamples) {
                if (count < 5) {
                    logger.info("样品ID: " + sample.getSample_id() + 
                              ", scheduleEndTime: '" + sample.getScheduleEndTime() + "'" +
                              ", tester: '" + sample.getTester() + "'");
                    count++;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("测试findAllValidSamples失败: " + e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 9 * * ?") // 每天早上9点执行
    public void checkUnconfirmedStatus() {
        logger.info("开始执行定时任务，检查 DQE 和研发未确认状态...");
        
        // 检查是否为周末，如果是周末则跳过提醒
        LocalDateTime currentTime = LocalDateTime.now();
        DayOfWeek dayOfWeek = currentTime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            logger.info("今天是周末（" + dayOfWeek + "），跳过未回传提醒功能");
            return;
        }
        
        try {
            // 查询 DQE 未确认的记录
            List<Map<String, Object>> dqeUnconfirmedSamples = scheduleBoardService.findDQEUnconfirmedSamples();
            
            // 统计 DQE 未确认的信息并发送通知
            logger.info("找到 " + dqeUnconfirmedSamples.size() + " 个 DQE 未确认的样品记录");
            StringBuilder dqeReport = new StringBuilder("DQE 未确认样品列表：\n");
            dqeReport.append("数量：").append(dqeUnconfirmedSamples.size()).append("\n");
            
            // 按 DQE 人名分组汇总数据
            Map<String, List<Map<String, Object>>> dqeGroupedByName = new HashMap<>();
            for (Map<String, Object> sample : dqeUnconfirmedSamples) {
                String dqeName = (String) sample.get("sample_DQE");
                if (dqeName != null && !dqeName.isEmpty()) {
                    dqeGroupedByName.computeIfAbsent(dqeName, k -> new ArrayList<>()).add(sample);
                }
            }
            
            // 按人名发送汇总通知
            for (Map.Entry<String, List<Map<String, Object>>> entry : dqeGroupedByName.entrySet()) {
                String dqeName = entry.getKey();
                List<Map<String, Object>> samples = entry.getValue();
                
                StringBuilder message = new StringBuilder();
                message.append("【DQE未确认提醒】\n");
                message.append("您有 ").append(samples.size()).append(" 个报告未确认，详情如下：\n");
                
                for (Map<String, Object> sample : samples) {
                    String electricSampleId = (String) sample.get("electric_sample_id");
                    Object actualFinishTimeObj = sample.get("actual_finish_time");
                    String actualFinishTime = "";
                    if (actualFinishTimeObj instanceof LocalDateTime) {
                        actualFinishTime = ((LocalDateTime) actualFinishTimeObj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } else if (actualFinishTimeObj instanceof String) {
                        actualFinishTime = (String) actualFinishTimeObj;
                    }
                    String sku = sample.get("sample_model") + " " + sample.get("sample_coding");

                    message.append("电气编号：").append(electricSampleId).append("\n");
                    message.append("SKU：").append(sku).append("\n");
                    message.append("测试人员实际完成时间：").append(actualFinishTime).append("\n");
                    message.append("DQE：").append(dqeName).append("\n\n");
                    
                    dqeReport.append("电气编号：").append(electricSampleId)
                                .append("，SKU：").append(sku)
                                .append("，实际完成时间：").append(actualFinishTime)
                                .append("，DQE：").append(dqeName)
                                .append("\n");
                }
                
                // 发送汇总通知给对应的 DQE
                if (dqeName != null && !dqeName.isEmpty()) {
                    String userId = getUserIdByName(dqeName);
                    if (userId != null && !userId.isEmpty()) {
                        sendTextNotification(userId, message.toString());
                        logger.info("已发送未确认汇总通知给 DQE 用户: " + dqeName + "，报告数量: " + samples.size());
                    } else {
                        logger.warn("未找到 DQE 用户 " + dqeName + " 的userId");
                    }
                }
            }
            logger.info(dqeReport.toString());
            
            // 查询研发未确认的记录
            List<Map<String, Object>> rdUnconfirmedSamples = scheduleBoardService.findRDUnconfirmedSamples();
            
            // 统计研发未确认的信息并发送通知
            logger.info("找到 " + rdUnconfirmedSamples.size() + " 个研发未确认的样品记录");
            StringBuilder rdReport = new StringBuilder("研发未确认样品列表：\n");
            rdReport.append("数量：").append(rdUnconfirmedSamples.size()).append("\n");
            
            // 按 Developer 人名分组汇总数据
            Map<String, List<Map<String, Object>>> rdGroupedByName = new HashMap<>();
            for (Map<String, Object> sample : rdUnconfirmedSamples) {
                String developerName = (String) sample.get("sample_Developer");
                if (developerName != null && !developerName.isEmpty()) {
                    rdGroupedByName.computeIfAbsent(developerName, k -> new ArrayList<>()).add(sample);
                }
            }
            
            // 按人名发送汇总通知
            for (Map.Entry<String, List<Map<String, Object>>> entry : rdGroupedByName.entrySet()) {
                String developerName = entry.getKey();
                List<Map<String, Object>> samples = entry.getValue();
                
                StringBuilder message = new StringBuilder();
                message.append("【研发未确认提醒】\n");
                message.append("您有 ").append(samples.size()).append(" 个报告未确认，详情如下：\n");
                
                for (Map<String, Object> sample : samples) {
                    String electricSampleId = (String) sample.get("electric_sample_id");
                    Object actualFinishTimeObj = sample.get("actual_finish_time");
                    String actualFinishTime = "";
                    if (actualFinishTimeObj instanceof LocalDateTime) {
                        actualFinishTime = ((LocalDateTime) actualFinishTimeObj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } else if (actualFinishTimeObj instanceof String) {
                        actualFinishTime = (String) actualFinishTimeObj;
                    }
                    String tester = (String) sample.get("tester");
                    String sku = sample.get("sample_model") + " " + sample.get("sample_coding");
                    
                    message.append("电气编号：").append(electricSampleId).append("\n");
                    message.append("SKU：").append(sku).append("\n");
                    message.append("测试人员实际完成时间：").append(actualFinishTime).append("\n");
                    message.append("研发工程师：").append(developerName).append("\n\n");
                    
                    rdReport.append("电气编号：").append(electricSampleId)
                               .append("，SKU：").append(sku)
                               .append("，实际完成时间：").append(actualFinishTime)
                               .append("，测试人员：").append(tester)
                               .append("，研发：").append(developerName)
                               .append("\n");
                }
                
                // 发送汇总通知给对应的 Developer
                if (developerName != null && !developerName.isEmpty()) {
                    String userId = getUserIdByName(developerName);
                    if (userId != null && !userId.isEmpty()) {
                        sendTextNotification(userId, message.toString());
                        logger.info("已发送未确认汇总通知给研发用户: " + developerName + "，报告数量: " + samples.size());
                    } else {
                        logger.warn("未找到研发用户 " + developerName + " 的userId");
                    }
                }
            }
            logger.info(rdReport.toString());
            
            // 查询测试人员未回传的记录
            List<Map<String, Object>> testerUnreturnedSamples = scheduleBoardService.findTesterUnconfirmedSamples();
            
            // 统计测试人员未回传的信息并发送通知
            logger.info("找到 " + testerUnreturnedSamples.size() + " 个测试人员未回传的样品记录");
            StringBuilder testerReport = new StringBuilder("测试人员未回传样品列表：\n");
            testerReport.append("数量：").append(testerUnreturnedSamples.size()).append("\n");
            
            // 按 Tester 人名分组汇总数据
            Map<String, List<Map<String, Object>>> testerGroupedByName = new HashMap<>();
            for (Map<String, Object> sample : testerUnreturnedSamples) {
                String testerName = (String) sample.get("tester");
                if (testerName != null && !testerName.isEmpty()) {
                    testerGroupedByName.computeIfAbsent(testerName, k -> new ArrayList<>()).add(sample);
                }
            }
            
            // 按人名发送汇总通知
            for (Map.Entry<String, List<Map<String, Object>>> entry : testerGroupedByName.entrySet()) {
                String testerName = entry.getKey();
                List<Map<String, Object>> samples = entry.getValue();
                
                StringBuilder message = new StringBuilder();
                message.append("【测试人员未回传提醒】\n");
                message.append("您有 ").append(samples.size()).append(" 个报告未回传，详情如下：\n");
                
                for (Map<String, Object> sample : samples) {
                    String sampleId = String.valueOf(sample.get("sample_id"));
                    Object submitTimeObj = sample.get("finish_time");
                    String submitTime = "";
                    if (submitTimeObj instanceof LocalDateTime) {
                        submitTime = ((LocalDateTime) submitTimeObj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } else if (submitTimeObj instanceof String) {
                        submitTime = (String) submitTimeObj;
                    }
                    
                    message.append("样品编号：").append(sampleId).append("\n");
                    message.append("提交时间：").append(submitTime).append("\n");
                    message.append("测试人员：").append(testerName).append("\n\n");
                    
                    testerReport.append("样品编号：").append(sampleId)
                                .append("，提交时间：").append(submitTime)
                                .append("，测试人员：").append(testerName)
                                .append("\n");
                }
                
                // 发送汇总通知给对应的 Tester
                if (testerName != null && !testerName.isEmpty()) {
                    String userId = getUserIdByName(testerName);
                    if (userId != null && !userId.isEmpty()) {
                        sendTextNotification(userId, message.toString());
                        logger.info("已发送未回传汇总通知给测试人员: " + testerName + "，报告数量: " + samples.size());
                    } else {
                        logger.warn("未找到测试人员 " + testerName + " 的userId");
                    }
                }
            }
            logger.info(testerReport.toString());
            
        } catch (Exception e) {
            logger.error("执行定时任务检查 DQE 和研发未确认状态时出错: ", e);
        }
    }
    
    /**
     * 发送未确认通知给指定用户
     */
    private void sendUnconfirmedNotification(String userName, String sampleId, String fullModel, String actualFinishTime, String role) throws ApiException {
        String userId = getUserIdByName(userName);
        if (userId == null || userId.isEmpty()) {
            logger.warn("未找到用户 " + userName + " 的userId");
            return;
        }
        
        StringBuilder message = new StringBuilder();
        message.append("【未确认提醒】\n");
        message.append("您有一个样品需要确认：\n");
        message.append("样品ID: ").append(sampleId).append("\n");
        message.append("电气编号: ").append(fullModel).append("\n");
        message.append("实际完成时间: ").append(actualFinishTime).append("\n");
        message.append("请尽快处理！");
        
        sendTextNotification(userId, message.toString());
        logger.info("已发送未确认通知给 " + role + " 用户: " + userName + ", 样品ID: " + sampleId);
    }

    /**
     * 查询指定部门的子部门（分组）并打印详细信息
     * @param deptId 部门ID
     */
    public void queryAndPrintDeptUsers(Long deptId) {
        try {
            logger.info("========== 开始查询部门 ID: " + deptId + " 的子部门（分组）信息 ==========");
            
            // 获取部门名称和上级部门信息
            String deptName = getDepartmentNameByDeptId(deptId);
            logger.info("部门名称: " + deptName);
            logger.info("部门ID: " + deptId);
            
            // 获取上级部门信息
            try {
                DingTalkClient infoClient = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/department/get");
                OapiV2DepartmentGetRequest infoReq = new OapiV2DepartmentGetRequest();
                infoReq.setDeptId(deptId);
                OapiV2DepartmentGetResponse infoRsp = infoClient.execute(infoReq, getAccessToken());
                
                // 提取上级部门ID
                String parentIdStr = extractParamOfResult(infoRsp.getBody(), "parent_id");
                if (parentIdStr != null && !parentIdStr.isEmpty() && !parentIdStr.equals("0")) {
                    Long parentId = Long.parseLong(parentIdStr);
                    String parentDeptName = getDepartmentNameByDeptId(parentId);
                    logger.info("上级部门名称: " + parentDeptName);
                    logger.info("上级部门ID: " + parentId);
                } else {
                    logger.info("上级部门: 无（顶级部门）");
                }
            } catch (Exception e) {
                logger.warn("获取上级部门信息失败: " + e.getMessage());
            }
            
            logger.info("----------------------------------------");
            
            // 查询该部门的子部门列表
            List<OapiV2DepartmentListsubResponse.DeptBaseResponse> subDepts = getDeptListByDeptId(deptId);
            
            logger.info("----------------------------------------");
            logger.info("共找到 " + subDepts.size() + " 个子部门（分组）");
            logger.info("========================================");
            
            // 打印子部门详细信息
            if (subDepts.isEmpty()) {
                logger.info("该部门下没有子部门（分组）");
            } else {
                int index = 1;
                for (OapiV2DepartmentListsubResponse.DeptBaseResponse subDept : subDepts) {
                    logger.info("【子部门（分组） " + index + "】");
                    logger.info("  部门ID: " + subDept.getDeptId());
                    logger.info("  部门名称: " + subDept.getName());
                    logger.info("  父部门ID: " + subDept.getParentId());
                    logger.info("  是否自动添加用户: " + subDept.getAutoAddUser());
                    logger.info("  是否创建部门群: " + subDept.getCreateDeptGroup());
                    logger.info("  ---");
                    index++;
                }
            }
            
            logger.info("========== 查询完成 ==========");
            
        } catch (ApiException e) {
            logger.error("查询部门子部门时发生错误: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("查询部门子部门时发生未知错误: " + e.getMessage(), e);
        }
    }

    /**
     * 定时任务：查询部门 63652303L 的子部门（分组）信息并打印
     * 可以通过取消注释 @Scheduled 注解来启用定时执行
     * 或者直接调用此方法手动执行
     * 
     * 使用方式：
     * 1. 定时执行：取消下面任意一个 @Scheduled 注解的注释
     * 2. 手动执行：在 Controller 或其他地方调用此方法
     */
//     @Scheduled(cron = "0 47 10 * * ?") // 每天早上10点31分执行
    // @Scheduled(cron = "0 0 */1 * * ?") // 每1小时执行一次
    // @Scheduled(fixedRate = 3600000) // 每1小时执行一次（毫秒）
//     @Scheduled(fixedRate = 60000) // 每1分钟执行一次（测试用）
    public void scheduledQueryDept63652303Users() {
        logger.info("========== 定时任务开始：查询部门 63652303L 的子部门（分组）信息 ==========");
        Long deptId = 63652303L;
        queryAndPrintDeptUsers(deptId);
        logger.info("========== 定时任务结束 ==========");
    }

}
