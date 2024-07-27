package com.lu.ddwyydemo04.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.*;
import com.lu.ddwyydemo04.Service.AccessTokenService;
import com.lu.ddwyydemo04.Service.TestManIndexService;
import com.lu.ddwyydemo04.exceptions.SessionTimeoutException;
import com.lu.ddwyydemo04.pojo.FileData;
import com.lu.ddwyydemo04.pojo.QuestData;
import com.lu.ddwyydemo04.pojo.Samples;
import com.lu.ddwyydemo04.pojo.TotalData;
import com.taobao.api.ApiException;
import com.taobao.api.FileItem;
import com.taobao.api.internal.util.WebUtils;
import jdk.nashorn.internal.objects.NativeMath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.SocketUtils;
import org.springframework.web.bind.annotation.*;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class testManIndexController {
    @Autowired
    private TestManIndexService testManIndexService;

    @Autowired
    private AccessTokenService accessTokenService;

    @Value("${dingtalk.agentid}")
    private String agentid;

    @Value("${file.storage.templatespath}")
    private String templatesPath;

    @Value("${file.storage.savepath}")
    private String savepath;


    private static final Logger logger = LoggerFactory.getLogger(testManIndexController.class);


    @GetMapping("/testManIndex") // 处理页面跳转请求
    public String loginTestManIndex() {
        // 返回跳转页面的视图名称
        return "testManIndex";
    }




    @GetMapping("/data") // 定义一个GET请求的接口，路径为 /data
    @ResponseBody
    public Map<String, Integer> getData(@RequestParam(required = false) String username) {
        // 创建一个Map对象，存储测试中、待审核、已完成、总数、逾期和失责的数量
        Map<String, Integer> data = new HashMap<>();
        if(username == null){
            logger.info("searchSamples用户信息不存在");
            // 会话超时，抛出自定义异常
            throw new SessionTimeoutException("会话已超时，请重新登录。");
        }else{
            //如果未创建过报告文件，则需要在这里创建用户进total表，才有数据展示
            int countTotal = testManIndexService.queryCountTotal(username);
            if(countTotal==0){
                TotalData totalData = new TotalData(username,0,0,0,0,0,0);
                testManIndexService.createTotal(totalData);
                logger.info("createTotal创建用户进total成功");

            }
            // 更新 total 表中的数据
            testManIndexService.updateTotal(username);
        }
        //获取用户的total信息并返回到前端展示
        Map<String, Integer> data1 = testManIndexService.getindexPanel(username);
        System.out.println(data1);

        data.put("testing", data1.get("testing")); // 测试中数量
        data.put("pending", data1.get("pending")); // 待审核1数量
        data.put("completed", data1.get("completed")); // 已完成数量
        data.put("total", data1.get("total")); // 总数
        data.put("overdue", data1.get("overdue")); // 逾期数量
        data.put("danger", data1.get("danger")); // 失责数量
        return data;
    }


    @GetMapping("/home/getFileCategories")
    @ResponseBody
    public List<String> getCategories() {
        File directory = new File(templatesPath);
        File[] files = directory.listFiles(File::isDirectory);
        System.out.println(templatesPath);

        List<String> categories = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                categories.add(file.getName());
            }
        }
        System.out.println(categories);
        return categories;
    }

    @GetMapping("/home/getFiles")
    @ResponseBody
    public List<FileData> getFiles(@RequestParam String category) {
        System.out.println("category:"+category);
        List<FileData> fileList = new ArrayList<>();

        // 拼接完整的文件夹路径
        String directoryPath = templatesPath + "/" + category;

        // 获取文件夹下的所有文件和文件夹
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        // 遍历文件夹下的所有文件和文件夹，并将它们添加到 fileList 中
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() || file.getName().endsWith(".xlsx")) {
                    fileList.add(new FileData(file.getName(), file.isDirectory() ? "directory" : "file"));
                }
            }
        }
        // 遍历并打印 fileList 中每个元素的文件名
        for (FileData data : fileList) {
            System.out.println("文件名: " + data.getName());
        }
        return fileList;
    }

    @GetMapping("/getTestManPanel")
    @ResponseBody
    public List<Samples> getTestManPanel(@RequestParam(required = false) String username){
        // 检查 username 参数是否传递
        if (username == null || username.isEmpty()) {
            // 处理用户信息不存在的情况，比如返回一个错误响应或重定向到登录页面
            return Collections.emptyList(); // 或者返回适当的错误响应
        } else {
            // 使用 username 查询数据
            logger.info("getTestManPanel查询成功："+testManIndexService.getTestManPanel(username));

            return testManIndexService.getTestManPanel(username);

        }

    }

    @PostMapping("/searchSamples")
    @ResponseBody
    public List<Samples> searchSamples(@RequestBody Map<String, Object> data){
        // 从会话中获取用户信息
        String username = (String) data.get("username");
        if (username == null) {
            // 处理用户信息不存在的情况，比如返回一个错误响应或重定向到登录页面
            logger.info("searchSamples用户信息不存在");
            // 会话超时，抛出自定义异常
            throw new SessionTimeoutException("会话已超时，请重新登录。");
        }

        String keyword = (String) data.get("keyword");
        String sort = (String) data.get("sortFilter");

        if(sort.equals("asc")){
            return testManIndexService.searchSamplesByAsc(username,keyword);
        }else if (sort.equals("desc")){
            return testManIndexService.searchSamplesByDesc(username,keyword);
        }else{
            return testManIndexService.searchSamples(username,keyword);
        }

    }

    @PostMapping("/testManIndex/getjumpFilepath")
    @ResponseBody
    public String getjumpFilepath(@RequestBody Map<String, Object> data){

        String model = (String) data.get("model");
        String coding = (String) data.get("coding");
        String category = (String) data.get("category");
        String version = (String) data.get("version");
        String big_species = (String) data.get("big_species");
        String small_species = (String) data.get("small_species");
        String high_frequency = (String) data.get("high_frequency");
        String questStats = (String) data.get("questStats");
        String sample_frequencyStr = (String) data.get("sample_frequency");
        Samples sample = new Samples();
        sample.setSample_model(model);
        sample.setSample_coding(coding);
        sample.setSample_category(category);
        sample.setVersion(version);
        sample.setBig_species(big_species);
        sample.setSmall_species(small_species);
        sample.setHigh_frequency(high_frequency);
        sample.setQuestStats(questStats);

        int sample_frequency =  Integer.parseInt(sample_frequencyStr.trim());
        sample.setSample_frequency(sample_frequency);

        return testManIndexService.queryFilepath(sample);
    }


    @PostMapping("/testManIndex/updateSamples")
    @ResponseBody
    public Map<String, Object> updateSamples(
            @RequestParam("edit_model") String editModel,
            @RequestParam("edit_coding") String editCoding,
            @RequestParam("edit_category") String editCategory,
            @RequestParam("edit_questStats") String questStats,
            @RequestParam("edit_big_species") String big_species,
            @RequestParam("edit_small_species") String small_species,
            @RequestParam("edit_high_frequency") String high_frequency,
            @RequestParam("edit_sample_frequency") String editsample_frequency,

            @RequestParam("edit_version") String editVersion,
            @RequestParam("edit_sample_name") String editSampleName,
            @RequestParam("edit_planfinish_time") String editPlanfinishTime,
            @RequestParam("edit_chip_control") String editChipControl,
            @RequestParam("edit_version_software") String editVersionSoftware,
            @RequestParam("edit_version_hardware") String editVersionHardware,
            @RequestParam("edit_supplier") String editsupplier,
            @RequestParam("edit_test_Overseas") String editTestOverseas,
            @RequestParam("edit_priority") String editpriority,
            @RequestParam("edit_sample_remark") String editsample_remark,
            @RequestParam("edit_sample_DQE") String editSampleDQE,
            @RequestParam("edit_sample_Developer") String editSampleDeveloper,
            @RequestParam("edit_sample_leader") String editSampleleader,
            @RequestParam("tester") String tester
            ) {

        Map<String, Object> response = new HashMap<>();
        try {

            // 调用服务类的方法来更新样品信息
            Samples sample = new Samples();
            sample.setSample_model(editModel);
            sample.setSample_coding(editCoding);
            sample.setSample_category(editCategory);
            sample.setVersion(editVersion);
            sample.setSample_name(editSampleName);
            sample.setPlanfinish_time(editPlanfinishTime);
            sample.setChip_control(editChipControl);
            sample.setVersion_software(editVersionSoftware);
            sample.setVersion_hardware(editVersionHardware);
            sample.setSupplier(editsupplier);
            sample.setTest_Overseas(editTestOverseas); // 确保参数名一致
            sample.setPriority(editpriority);
            sample.setSample_remark(editsample_remark);
            sample.setSample_DQE(editSampleDQE);
            sample.setSample_Developer(editSampleDeveloper);
            sample.setSample_leader(editSampleleader);
            sample.setTester(tester);

            int sample_frequency =  Integer.parseInt(editsample_frequency.trim());
            sample.setSample_frequency(sample_frequency);

            sample.setBig_species(big_species);
            sample.setSmall_species(small_species);
            sample.setHigh_frequency(high_frequency);

            sample.setQuestStats(questStats);
            //如果有更新产品名称则需要更新文件名
            String old_name = testManIndexService.querySample_name(sample);
            String oldFilePath = testManIndexService.queryFilepath(sample);
            String oldtester = testManIndexService.queryTester(sample); //已经添加questStats,20240709


            if(!Objects.equals(oldtester, tester)){
                response.put("message", "更换当前测试人");
            }

            String high_sign = "";
            sample.setFilepath(oldFilePath);
            if(!Objects.equals(old_name, editSampleName)){
                File oldFile = new File(oldFilePath);

                if(high_frequency.equals("是")){
                    high_sign = "高频_";
                }

                // 生成新的文件名字符串
                String newFileName = savepath.replace("/","\\") + "\\" +editModel + " " + editCoding + "_" + editCategory + "_" + editVersion + "_第" + editsample_frequency + "次送样_" + high_sign  + editSampleName + ".xlsx";
                File newFile = new File(newFileName);
                logger.info("尝试重命名文件：oldFilePath=" + oldFilePath + ", newFileName=" + newFileName);

                try {
                    if (oldFile.renameTo(newFile)) {
                        sample.setFilepath(newFileName);
                        logger.info("updateSamples文件重命名成功" + oldFile + " -> " + newFileName);
                        response.put("rename","重命名成功");
                    } else {
                        throw new IOException("文件重命名失败" + oldFile + " -> " + newFileName);
                    }
                } catch (IOException e) {
                    logger.error("重命名文件时出错: " + e.getMessage());
                    response.put("status", "error");
                    response.put("message", "文件重命名失败，请确保文件未被占用并且路径正确: " + e.getMessage());
                    return response;
                }

            }

            //如果有更改当前测试人，则需要添加共同测试人
            String tester_teamwork = testManIndexService.queryTester_teamwork(sample);
            boolean containsName = tester_teamwork.contains(tester);
            if(containsName){
                testManIndexService.updateSample(sample);
            }else{
                testManIndexService.updateSampleTeamWork(sample);
                logger.info("添加共同测试人:"+tester);
            }
            response.put("oldFilePath", oldFilePath);
            response.put("status", "success");
            logger.info("updateSamples样品信息更新成功:"+tester);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "样品信息更新失败: " + e.getMessage());
            logger.info("updateSamples样品信息更新失败:"+e.getMessage());
        }

        return response;
    }


    @PostMapping("/testManIndex/finishTest")
    @ResponseBody
    public Map<String, Object> finishTest(@RequestBody Map<String, String> request){
        Map<String, Object> response = new HashMap<>();
        String filepath = request.get("filepath");
        String model = request.get("model");
        String coding = request.get("coding");
        String category = request.get("category");
        String version = request.get("version");
        String schedule = request.get("schedule");
        String sample_frequencyStr = request.get("sample_frequency");

        String big_species = request.get("big_species");
        String small_species = request.get("small_species");
        String high_frequency = request.get("high_frequency");

        String questStats = request.get("questStats");

        int sample_frequency =  Integer.parseInt(sample_frequencyStr.trim());

        if (schedule.equals("0")){
            schedule = "1";
            // 设置完成时间为当前的北京时间
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = now.format(formatter);
            testManIndexService.finishTestWithoutTime(schedule,model,coding,category,version,formattedDateTime,big_species,small_species,high_frequency,sample_frequency,questStats);
        }else if(schedule.equals("1")){
            schedule = "0";
            testManIndexService.finishTest(schedule,model,coding,category,version,big_species,small_species,high_frequency,sample_frequency,questStats);
        }



        response.put("status", "success");
        response.put("message", "文件提交成功，接下来请审核！");
        logger.info("finishTest提交文件/撤回审核成功："+filepath);
        return response;
    }




    @PostMapping("/testManIndex/deleteFilepath")
    @ResponseBody
    public Map<String, Object> deleteFilepath(@RequestBody Map<String, String> request){
        Map<String, Object> response = new HashMap<>();
        String filepath = request.get("filepath");

        //删除文件夹里的文件,后续要备份则需要在这里增加备份到别的路径的处理
        try {
            // 创建文件路径的Path对象
            Path fileToDeletePath = Paths.get(filepath);
            // 删除文件
            Files.deleteIfExists(fileToDeletePath);

            // 如果需要备份，可以在这里添加备份的代码
            // backupFile(fileToDeletePath);
            int deleteJudge = testManIndexService.deleteFilepath(filepath);
            if(deleteJudge==1){
                // 返回成功响应
                response.put("status", "success");
                response.put("message", "删除文件成功");
                logger.info("deleteFilepath successfully filepath:"+filepath);
            }else{
                // 返回成功响应
                response.put("status", "error");
                response.put("message", "数据库删除文件失败");
                logger.info("数据库删除文件失败:"+filepath);
            }


        } catch (IOException e) {
            // IO异常处理
            e.printStackTrace();
            // 返回失败响应
            response.put("status", "error");
            response.put("message", "删除异常: " + e.getMessage());
            logger.info("deleteFilepath fail filepath:"+filepath+"e.getMessage():"+e.getMessage());
        }

        return response;
    }


    @PostMapping("/postChooseContact")
    @ResponseBody
    public ResponseEntity<String> receiveContacts(@RequestBody String contactsJson) {
        // 在这里，您可以处理数据，如打印到控制台或存储到数据库
        System.out.println(contactsJson);

        // 返回一个简单的确认信息
        return ResponseEntity.ok("Contacts received");
    }


    @PostMapping("/testManIndex/uploadFileToDingtalk")
    @ResponseBody
    public Map<String, String> uploadFileToDingtalk(@RequestParam("filepath") String filepath,
                                          @RequestParam("dirId") String dirId,
                                          @RequestParam("spaceId") String spaceId,
                                          @RequestParam("receiverId") String receiverId,
                                          @RequestParam("authCode") String authCode) {
        Map<String, String> response = new HashMap<>();
        logger.info("Received request to uploadFileToDingtalk. filepath: {}, dirId: {}, spaceId: {}, receiverId: {}, authCode: {}",
                filepath, dirId, spaceId, receiverId, authCode);
        try {
            String accessToken = accessTokenService.getAccessToken();
            File file = new File(filepath);
            String filename = file.getName();

            String media_id = getMediaId(filepath,accessToken);
            System.out.println("media_id:"+media_id);

            //上传文件到钉盘
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/cspace/add");
            OapiCspaceAddRequest req = new OapiCspaceAddRequest();
            req.setAgentId(agentid);
            req.setCode(authCode);
            req.setFolderId(dirId);
            req.setMediaId(media_id);
            req.setSpaceId(spaceId);
            req.setName(filename);
            req.setOverwrite(true);
            req.setHttpMethod("GET");
            OapiCspaceAddResponse rsp = client.execute(req, accessToken);
            logger.info("Response from DingTalk cspace add: {}", rsp.getBody());
            //发送钉盘文件给用户
            sendDingFileToUser(accessToken,filename,media_id,receiverId);

            response.put("status", "发送成功");
            logger.info("uploadFileToDingtalk successfully.");
        } catch (ApiException e) {
            e.printStackTrace();
            response.put("status", "发送失败");
            logger.info("uploadFileToDingtalk fail.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    public String getMediaId(String filepath,String access_token) throws Exception {
        File file = new File(filepath);
        long fileSize = file.length(); // 获取文件大小

        // 计算分块数
        long chunkSize = 7 * 1024 * 1024; // 7MB
        long chunkNumbers = (fileSize + chunkSize - 1) / chunkSize; // 计算分块数

        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/file/upload/transaction");
        OapiFileUploadTransactionRequest request = new OapiFileUploadTransactionRequest();
        request.setAgentId(agentid);
        request.setFileSize(fileSize);
        request.setChunkNumbers(chunkNumbers);
        request.setHttpMethod("GET");
        OapiFileUploadTransactionResponse response = client.execute(request,access_token);
        String uploadId = response.getUploadId();
        System.out.println("chunkNumbers:"+chunkNumbers);
        System.out.println("response.getBody():"+response.getBody());

//        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");

        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            for (long i = 0; i < chunkNumbers; i++) {
                long startByte = i * chunkSize;
                byte[] chunkData = new byte[(int) Math.min(chunkSize, fileSize - startByte)];
                randomAccessFile.seek(startByte);
                randomAccessFile.readFully(chunkData);

                // 准备上传请求
                OapiFileUploadChunkRequest chunkRequest = new OapiFileUploadChunkRequest();
                chunkRequest.setAgentId(agentid);
                chunkRequest.setUploadId(uploadId);
                chunkRequest.setChunkSequence(i + 1); // 分块序号从1开始

                DingTalkClient chunkClient = new DefaultDingTalkClient("https://oapi.dingtalk.com/file/upload/chunk?"+WebUtils.buildQuery(chunkRequest.getTextParams(),"utf-8"));

                chunkRequest = new OapiFileUploadChunkRequest();
                // 设置文件内容
                FileItem fileItem = new FileItem("chunk" + i, chunkData);
                chunkRequest.setFile(fileItem);

                // 发送上传请求
                OapiFileUploadChunkResponse chunkResponse = chunkClient.execute(chunkRequest, access_token);
            }
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        DingTalkClient transClient = new DefaultDingTalkClient("https://oapi.dingtalk.com/file/upload/transaction");
        OapiFileUploadTransactionRequest transRequest = new OapiFileUploadTransactionRequest();
        transRequest.setAgentId(agentid);
        transRequest.setFileSize(fileSize);
        transRequest.setChunkNumbers(chunkNumbers);
        transRequest.setUploadId(uploadId);
        transRequest.setHttpMethod("GET");
        OapiFileUploadTransactionResponse transResponse = transClient.execute(transRequest,access_token);
        System.out.println("transResponse:"+transResponse.getBody());

        return transResponse.getMediaId();
    }



    public void sendDingFileToUser(String accesstoken,String filename,String media_id,String userid) throws ApiException, IOException {
        OapiCspaceAddToSingleChatRequest request = new OapiCspaceAddToSingleChatRequest();
        request.setAgentId(agentid);
        request.setUserid(userid);
        request.setMediaId(media_id);
        request.setFileName(filename);
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/cspace/add_to_single_chat?"+WebUtils.buildQuery(request.getTextParams(),"utf-8"));
        OapiCspaceAddToSingleChatResponse response = client.execute(request, accesstoken);
        logger.info("sendDingFileToUser successfully."+response.getBody());
    }
    private static String parseUploadId(String responseBody) throws ApiException {
        JSONObject jsonObject = JSON.parseObject(responseBody);
        if (jsonObject.getInteger("errcode") == 0) {
            return jsonObject.getString("upload_id");
        } else {
            throw new ApiException("Error uploading file: " + jsonObject.getString("errmsg"));
        }
    }

    private String parseMediaId(String responseBody) throws ApiException {
        JSONObject jsonObject = JSON.parseObject(responseBody);
        if (jsonObject.getInteger("errcode") == 0) {
            return jsonObject.getString("media_id");
        } else {
            throw new ApiException("Error uploading file: " + jsonObject.getString("errmsg"));
        }
    }

    @PostMapping("/log/debug")
    @ResponseBody
    public String logDebug(@RequestBody Map<String, String> log) {
        String message = log.get("message");
        logger.info(message);
        return message;
    }

}
