package com.lu.ddwyydemo04.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.*;
import com.lu.ddwyydemo04.Service.AccessTokenService;
import com.lu.ddwyydemo04.Service.JsapiTicketService;
import com.lu.ddwyydemo04.dao.DQE.DQEDao;
import com.lu.ddwyydemo04.dao.UserAccessLogDao;
import com.lu.ddwyydemo04.pojo.UserAccessLog;
import com.lu.ddwyydemo04.Service.UserAccessLogService;
import com.taobao.api.ApiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class DingTalkH5Controller {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private DQEDao dqeDao;

    @Autowired
    private UserAccessLogDao userAccessLogDao;

    @Autowired
    private UserAccessLogService userAccessLogService;

    @Autowired
    private com.lu.ddwyydemo04.Service.DingTalkUserCacheService userCacheService;

    @Value("${dingtalk.agentid}")
    private String agentid;

    @Value("${dingtalk.corpid}")
    private String corpid;

    @Value("${file.storage.templatespath}")
    private String templatespath;

    @Value("${file.storage.savepath}")
    private String savepath;

    @Value("${file.storage.imagepath}")
    private String imagepath;
    private static final String GET_USER_INFO_URL = "https://oapi.dingtalk.com/user/getuserinfo";

    private static final Logger logger = LoggerFactory.getLogger(testManIndexController.class);

    // 获取access_token的方法

    /**
     * 恢复用户 Session（用于页面跳转时快速恢复登录状态）
     * 通过 username 从 Redis 缓存中获取用户信息并恢复到 session
     */
    @PostMapping("/api/restoreSession")
    @ResponseBody
    public Map<String, Object> restoreSession(@RequestBody Map<String, String> requestMap, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String username = requestMap.get("username");
            String job = requestMap.get("job");
            
            if (username == null || username.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "缺少用户名参数");
                return result;
            }
            
            System.out.println("🔄 尝试恢复 Session: username=" + username + ", job=" + job);
            logger.info("尝试恢复 Session: username=" + username);
            
            // 从 Redis 缓存中查找用户信息（通过遍历所有缓存的用户）
            com.lu.ddwyydemo04.Service.DingTalkUserCacheService.UserInfo userInfo = userCacheService.getUserInfoByUsername(username);
            
            if (userInfo != null) {
                // 找到了用户信息，恢复到 session
                javax.servlet.http.HttpSession session = httpRequest.getSession(true);
                session.setAttribute("userId", userInfo.getUserId());
                session.setAttribute("username", userInfo.getUsername());
                session.setAttribute("job", userInfo.getJob());
                session.setAttribute("departmentId", userInfo.getDepartmentId());
                if (userInfo.getDepartmentName() != null && !userInfo.getDepartmentName().isEmpty()) {
                    session.setAttribute("departmentName", userInfo.getDepartmentName());
                }
                session.setAttribute("corp_id", userInfo.getCorpId());
                
                System.out.println("✅ Session 恢复成功: " + username + " (ID: " + userInfo.getUserId() + ")");
                logger.info("Session 恢复成功: " + username);
                
                result.put("success", true);
                result.put("message", "Session 恢复成功");
                result.put("username", userInfo.getUsername());
                result.put("job", userInfo.getJob());
            } else {
                // Redis 缓存中没有找到用户信息
                System.out.println("⚠️ Redis 缓存中未找到用户信息: " + username);
                logger.warn("Redis 缓存中未找到用户信息: " + username);
                result.put("success", false);
                result.put("message", "缓存中未找到用户信息，请重新登录");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Session 恢复失败: " + e.getMessage());
            logger.error("Session 恢复失败: " + e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Session 恢复失败: " + e.getMessage());
        }
        
        return result;
    }

    @PostMapping("/api/getUserInfo")
    @ResponseBody
    public Map<String, Object> getUserInfo(@RequestBody Map<String, String> requestMap, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            //获取免登授权码authCode
            String authCode = requestMap.get("authCode");
            if (authCode == null || authCode.isEmpty()) {
                result.put("errorCode", -1);
                result.put("errorMessage", "授权码为空，请重新登录");
                logger.error("获取用户信息失败：授权码为空");
                return result;
            }
            
            String accessToken = accessTokenService.getAccessToken(); // 调用方法获取accessToken

            // 首先使用authCode获取userid（这个API调用是必需的，不能缓存）
            DingTalkClient client = new DefaultDingTalkClient(GET_USER_INFO_URL);
            OapiUserGetuserinfoRequest getUserInfoRequest = new OapiUserGetuserinfoRequest();
            getUserInfoRequest.setCode(authCode);
            getUserInfoRequest.setHttpMethod("GET");

            OapiUserGetuserinfoResponse response = client.execute(getUserInfoRequest, accessToken);
            
            if (response.getErrcode() == 0) {
            // 正常情况下返回用户userid
            String userid = response.getUserid();

            // 检查Redis缓存中是否已有该用户的信息
            System.out.println("检查用户 " + userid + " 的缓存信息...");
            com.lu.ddwyydemo04.Service.DingTalkUserCacheService.UserInfo cachedUserInfo = userCacheService.getUserInfo(userid);
            if (cachedUserInfo != null) {
                // 从缓存中获取用户信息，完全避免调用钉钉API
                System.out.println("🎉 从缓存中获取用户信息成功，避免调用钉钉API: " + userid + " (" + cachedUserInfo.getUsername() + ")");
                logger.info("从缓存获取用户信息: " + cachedUserInfo.getUsername());

                // 返回缓存的用户信息
                result.putAll(cachedUserInfo.toMap());
                
                // 记录用户访问日志
                userAccessLogService.recordUserAccess(cachedUserInfo.getUsername(), cachedUserInfo.getJob(), "登录/获取用户信息", request);
                
                System.out.println("✅ 用户登录成功（使用缓存），返回用户信息: " + cachedUserInfo.getUsername());
                return result;
            }

            // 缓存中没有，从钉钉API获取详细信息（首次登录）
            System.out.println("📡 缓存中没有用户信息，从钉钉API获取: " + userid);
            logger.info("缓存中没有用户信息，从钉钉API获取: " + userid);

            // 使用userId获取用户的详细信息
            DingTalkClient infoClient = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/get");
            OapiV2UserGetRequest infoReq = new OapiV2UserGetRequest();
            infoReq.setUserid(userid);
            infoReq.setLanguage("zh_CN");
            OapiV2UserGetResponse infoRsp = infoClient.execute(infoReq, accessToken);
            String username = extractParamOfResult(infoRsp.getBody(),"name");
            logger.info("name:"+username);


            //提取职位,"测试专员"
//            String job = extractParamOfResult(infoRsp.getBody(),"title");
//            System.out.println("job:"+job);

            //提取部门id,"523459714"是电子测试组的编号
            String departmentId = extractDepartmentIds(infoRsp.getBody());
//            System.out.println("departmentIds:"+departmentId);
            logger.info("departmentIds:"+departmentId);
//            System.out.println("infoRsp.getBody():"+infoRsp.getBody());



            // 将 infoRsp.getBody() 保存到带有用户名的 txt 文件
//            String fileName = "userInfoResponse_" + username + ".txt";
//            try (FileWriter writer = new FileWriter(fileName)) {
//                writer.write(infoRsp.getBody());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            DingTalkClient clientDept = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/department/listparentbyuser");
            OapiV2DepartmentListparentbyuserRequest reqDept = new OapiV2DepartmentListparentbyuserRequest();
            reqDept.setUserid(userid);
            OapiV2DepartmentListparentbyuserResponse rspDept = clientDept.execute(reqDept, accessToken);
            String responseDeptBody = rspDept.getBody();

            // 调用方法检查部门
            String job = checkParentDepartment(responseDeptBody,username);
//            System.out.println(job);
            logger.info("job:"+job);

            // 特殊用户名覆盖 job 为 "projectLeader"
            if ("陈少侠".equals(username) || "郭丽纯".equals(username) ||
                    "占海英".equals(username) || "刘定荣".equals(username) || "姚遥".equals(username)) {
                job = "projectLeader";
            }

            // 从 users 表获取 departmentName
            String departmentName = dqeDao.getDepartmentNameByUsername(username);
            
            // 创建用户信息对象并缓存到Redis（7天有效期）
            com.lu.ddwyydemo04.Service.DingTalkUserCacheService.UserInfo userInfo = 
                new com.lu.ddwyydemo04.Service.DingTalkUserCacheService.UserInfo(
                    userid, username, job, departmentId, departmentName, corpid, templatespath, imagepath, savepath
                );
            System.out.println("🔄 首次登录，准备缓存用户信息: " + username + " (ID: " + userid + ")");
            logger.info("首次登录，缓存用户信息: " + username);
            userCacheService.cacheUserInfo(userInfo);

            //将想要返回的结果保存起来
            result.put("userId", userid);
            result.put("username", username);
            result.put("job", job);
            result.put("departmentId", departmentId);
            if (departmentName != null && !departmentName.isEmpty()) {
                result.put("departmentName", departmentName);
            }
            result.put("corp_id",corpid);
            result.put("templatespath",templatespath);
            result.put("imagepath",imagepath);
            result.put("savepath",savepath);

                // 记录用户访问日志（使用服务类封装的方法）
                // accessPage 可以自定义，例如："登录/获取用户信息"
                userAccessLogService.recordUserAccess(username, job, "登录/获取用户信息", request);

            } else {
                // 发生错误时返回错误信息
                result.put("errorCode", response.getErrcode());
                result.put("errorMessage", "获取用户信息失败：" + response.getErrmsg());
                logger.error("获取用户信息失败，钉钉API返回错误码: {}, 错误信息: {}", response.getErrcode(), response.getErrmsg());
            }
        } catch (ApiException e) {
            // 处理钉钉API异常
            String errorMsg = e.getMessage();
            logger.error("获取用户信息时发生ApiException: {}", errorMsg, e);
            
            // 根据错误类型返回友好的错误信息
            if (errorMsg != null) {
                if (errorMsg.contains("timeout") || errorMsg.contains("timed out") || errorMsg.contains("connect")) {
                    result.put("errorCode", -2);
                    result.put("errorMessage", "连接钉钉服务器超时，请检查网络连接后重试");
                } else if (errorMsg.contains("errcode")) {
                    result.put("errorCode", -3);
                    result.put("errorMessage", "钉钉API调用失败：" + errorMsg);
                } else {
                    result.put("errorCode", -4);
                    result.put("errorMessage", "获取用户信息失败：" + errorMsg);
                }
            } else {
                result.put("errorCode", -5);
                result.put("errorMessage", "获取用户信息失败，请稍后重试");
            }
        } catch (Exception e) {
            // 处理其他异常
            logger.error("获取用户信息时发生未知异常: ", e);
            result.put("errorCode", -6);
            result.put("errorMessage", "系统错误，请联系管理员");
        }

        //目前只返回userid: ,name:卢健，job:测试专员，departmentIds:[523459714]，后续看还需要的话可以从这里获取然后前端保存到sessionStorage
        return result;
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


    //提取部门id
    public static String extractDepartmentIds(String userInfoJson) {
        // 使用fastjson将JSON字符串解析为JSONObject对象
        JSONObject userInfo = JSONObject.parseObject(userInfoJson);

        // 获取result字段中的值
        JSONObject resultObj = userInfo.getJSONObject("result");
        if (resultObj != null) {
            // 获取部门id列表字段的值并返回
            JSONArray deptIdList = resultObj.getJSONArray("dept_id_list");
            return deptIdList.toString();
        } else {
            // 如果result字段为空，则返回空字符串或者其他默认值
            return "";
        }
    }


    // 注入JsapiTicketService
    @Autowired
    private JsapiTicketService jsapiTicketService;

    @GetMapping("/getJsapiConfig")
    @ResponseBody
    public Map<String, Object> getJsapiConfig(@RequestParam("url") String url) throws Exception {
        // 这里使用JsapiTicketService来获取jsapi_ticket，并生成签名等信息
        return generateJsapiConfig(url);
    }

    // 生成JSAPI配置信息的方法，使用钉钉的签名逻辑
    private Map<String, Object> generateJsapiConfig(String url) throws Exception {
        // 获取jsapi_ticket
        String jsapiTicket = jsapiTicketService.getJsapiTicket();

        // 计算时间戳和随机字符串
        String timeStamp = Long.toString(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.randomUUID().toString().replaceAll("-", "");

        // 生成签名
        String signature = sign(jsapiTicket, nonceStr, Long.parseLong(timeStamp), url);

        // 返回配置信息
        Map<String, Object> config = new HashMap<>();
        config.put("agentId", agentid);
        config.put("corpId", corpid);
        config.put("timeStamp", timeStamp);
        config.put("nonceStr", nonceStr);
        config.put("signature", signature);
        config.put("jsApiList", Arrays.asList("plugin.coolAppSdk.sendMessageToGroup","biz.chat.toConversationByOpenConversationId","biz.chat.chooseConversationByCorpId","device.base.getUUID","biz.navigation.close","biz.contact.choose","biz.cspace.chooseSpaceDir","biz.ding.create","biz.cspace.saveFile","runtime.permission.requestAuthCode","biz.util.downloadFile")); // 只需要使用选择联系人的JSAPI

        return config;
    }

    // 钉钉文档中的签名方法
    public static String sign(String jsticket, String nonceStr, long timeStamp, String url) throws Exception {
        String plain = "jsapi_ticket=" + jsticket + "&noncestr=" + nonceStr + "&timestamp=" + timeStamp
                + "&url=" + decodeUrl(url);
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            sha1.reset();
            sha1.update(plain.getBytes("UTF-8"));
            return byteToHex(sha1.digest());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    // 字节数组转化成十六进制字符串
    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    // 因为iOS端上传递的url是encode过的，Android是原始的url。开发者使用的也是原始url,
    // 所以需要把参数进行一般urlDecode
    private static String decodeUrl(String encodedUrl) throws Exception {
        // 首先对传入的URL进行解码
        String decodedUrl = URLDecoder.decode(encodedUrl, "UTF-8");

        // 然后使用解码后的URL创建URL对象
        URL url = new URL(decodedUrl);

        // 构建不包含查询参数的URL
        StringBuilder urlBuffer = new StringBuilder();
        urlBuffer.append(url.getProtocol());
        urlBuffer.append(":");
        if (url.getAuthority() != null && url.getAuthority().length() > 0) {
            urlBuffer.append("//");
            urlBuffer.append(url.getAuthority());
        }
        if (url.getPath() != null) {
            urlBuffer.append(url.getPath());
        }

        // 如果原始URL包含查询参数，将它们添加到构建的URL中
        if (url.getQuery() != null) {
            urlBuffer.append('?');
            urlBuffer.append(url.getQuery());
        }

        return urlBuffer.toString();
    }

    //20241025：此方法是用来提取部门的主列表里是否包含某个部门id来判定是什么部门的

    public String checkParentDepartment(String jsonResponse,String username) {
        // 如果用户名是黄家灿，直接设置 job 为 DQE 并返回
        if ("黄家灿".equals(username) || "荣成彧".equals(username) || "李良健".equals(username) || "邓继元".equals(username)) {
            return "manager";
        }

        //20250526新增官旺华
        if ("官旺华".equals(username) || "赵梓宇".equals(username) || "刘鹏飞".equals(username)) {
            return "tester";
        } else {
            if ( "阙兰".equals(username) ||"卢健".equals(username) || "许梦瑶".equals(username) || "卢绮敏".equals(username) || "蓝明城".equals(username) ) {
                return "DQE";
            }else if("吴川".equals(username)){
                return "SQE";
            }else if ("刘涛".equals(username)){
                return "CQE";
            }

        }





        // 解析 JSON 响应
        JSONObject response = JSON.parseObject(jsonResponse);
//        System.out.println("response:"+response);
        String job = "";
        // 检查 errcode 是否为 0
        if (response.getInteger("errcode") == 0) {
            JSONObject result = response.getJSONObject("result");
            List<JSONObject> parentList = result.getJSONArray("parent_list").toJavaList(JSONObject.class);

            // 遍历所有的父部门列表
            for (JSONObject parent : parentList) {
                List<Long> parentDeptIdList = parent.getJSONArray("parent_dept_id_list").toJavaList(Long.class);

                // 检查部门 ID 并打印相应的信息
                // 如果部门ID是1044809148，设置为tester，1044809148L是光学实验室
                if (parentDeptIdList.contains(1044809148L)) {
                    job = "tester";
                    break;  // 找到后可选择立即返回
                }
                
                // 电子测试组及其所有子部门的ID列表
                Set<Long> testerDeptIds = new HashSet<>(Arrays.asList(
                    523459714L,  // 原电子测试组
                    1044830096L, // 蓝牙音频
                    1045276038L, // 线材
                    1044665230L, // 影音设备
                    1045229052L, // 数据储存/网通
                    1044579225L, // 摄像头产品
                    1045239061L, // 高频信号测试
                    992946901L,  // 产品运营兼容性测试
                    1045082062L, // 选品APP测试
                    993496054L   // 测试开发
                ));
                
                // 检查是否包含电子测试组或其任何子部门（独立判断，不依赖品质工程部）
                boolean isTester = parentDeptIdList.stream().anyMatch(testerDeptIds::contains);
                if (isTester) {
//                    System.out.println("有电子测试组或其子部门，是测试技术员");
                    job = "tester";  // 设置为 "tester"，并优先返回
                    break;  // 找到后可选择立即返回
                }
                
//                if (parentDeptIdList.contains(62712385L) ) {
                if (parentDeptIdList.contains(62712385L) || parentDeptIdList.contains(349996662L)) {
//                    System.out.println("产品研发部");
                    job = "rd";
                }
                if (parentDeptIdList.contains(63652303L)) {
//                    System.out.println("品质工程部");
                    // 如果不是测试组，则设置为DQE
                    if (!isTester) {
                        job = "DQE";
                    }
                }

                //20241105 新增一个产品经营部的job判定方法：
                // 针对大部门 ID 为 62632390L 的情况
                if (parentDeptIdList.contains(62632390L)) {
//                    System.out.println("产品经营部");

                    // 检查是否属于耳机部门的两个指定 ID，并且排除特定用户
                    if ((parentDeptIdList.contains(925840291L) || parentDeptIdList.contains(925828219L))
                            && !username.equals("高玄英") && !username.equals("姜呈祥")) {
                        job = "rd";
//                        System.out.println("耳机部门的用户，设为 RD");
                        break;
                    } else {
                        job = "projectLeader";
//                        System.out.println("非耳机部门或特定用户，设为 Project Leader");
                    }
                }

            }
        } else {
            System.out.println("请求失败: " + response.getString("errmsg"));
        }
        return job;
    }


    @GetMapping("/authRedirect")
    public String authRedirect(
            @RequestParam(value = "sample_id", required = false) String sampleId,
            HttpServletRequest request) {

        // 这里可选将 sampleId 放到请求属性中（如果需要JSP等模板用），
        // 但如果是纯静态html，页面用JS解析URL参数即可，下面可以不写
        if (sampleId != null) {
            request.setAttribute("sample_id", sampleId);
        }

        // 返回静态html页面名（不带后缀）
        return "authRedirect";
    }

}
