package com.lu.ddwyydemo04.Service;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiCspaceAddToSingleChatRequest;
import com.dingtalk.api.request.OapiFileUploadChunkRequest;
import com.dingtalk.api.request.OapiFileUploadTransactionRequest;
import com.dingtalk.api.response.OapiCspaceAddToSingleChatResponse;
import com.dingtalk.api.response.OapiFileUploadChunkResponse;
import com.dingtalk.api.response.OapiFileUploadTransactionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lu.ddwyydemo04.controller.testManIndexController;
import com.lu.ddwyydemo04.dao.QuestDao;
import com.lu.ddwyydemo04.dao.TestManDao;
import com.lu.ddwyydemo04.pojo.*;
import com.taobao.api.ApiException;
import com.taobao.api.FileItem;
import com.taobao.api.internal.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("TestManIndexService")
public class TestManIndexService {
    @Autowired
    private QuestDao questDao;

    @Autowired
    private TestManDao testManDao;

    private static final Logger logger = LoggerFactory.getLogger(testManIndexController.class);

    public Map<String, Integer> getindexPanel(String name){
        return questDao.getindexPanel(name);
    }

    public List<Samples> getTestManPanel(String tester){
        return testManDao.getTestManPanel(tester);
    }

    public int queryCountTotal(String name){ return testManDao.queryCountTotal(name);}

    public void createTotal(TotalData totalData){
        testManDao.createTotal(totalData);
    }

    public void updateTotal(String name){
        testManDao.updateTotal(name);
    }

    public List<Samples> searchSamples(String tester,String keyword){
        return testManDao.searchSamples(tester,keyword);
    }

    public List<Samples> searchSamplesByAsc(String tester,String keyword){
        return testManDao.searchSamplesByAsc(tester,keyword);
    }

    public List<Samples> searchSamplesByDesc(String tester,String keyword){
        return testManDao.searchSamplesByDesc(tester,keyword);
    }


    public void updateSample(Samples sample) {
        testManDao.updateSample(sample);
    }

    public void updateSampleTeamWork(Samples sample){
        testManDao.updateSampleTeamWork(sample);
    }

    public void finishTest(String schedule,String sample_id){
        testManDao.finishTest(schedule,sample_id);
    }
    public void finishTestWithoutTime(String schedule,String finish_time,String sample_id){

        testManDao.finishTestWithoutTime(schedule,finish_time,sample_id);
    }



    public LocalDateTime  queryCreateTime(String sample_id){
        return testManDao.queryCreateTime(sample_id);
    }

    public String queryTester_teamwork(String sample_id){
        return testManDao.queryTester_teamwork(sample_id);
    }

    public String querySample_name(String sample_id){
        return testManDao.querySample_name(sample_id);
    }

    public String queryFilepath(String sample_id){
        return testManDao.queryFilepath(sample_id);
    }

    public String queryTester(String sample_id){
        return testManDao.queryTester(sample_id);
    }

    public int deleteFromTestIssues(int sample_id){
        return testManDao.deleteFromTestIssues(sample_id);
    }

    public int deleteFromSamples(int sample_id){
        return testManDao.deleteFromSamples(sample_id);
    }

    //提取问题点的相关服务层
    //通过大小编码，版本，送样次数，是否高频，来返回sample_id
    public int querySampleId(String filepath){
        return testManDao.querySampleId(filepath);
    }

    public int insertTestIssues(TestIssues testIssues){
        return testManDao.insertTestIssues(testIssues);
    }

    public int queryHistoryid(int sample_id){
        return testManDao.queryHistoryid(sample_id);
    }
    public int setDuration(double planWorkDays,double workDays,String sample_id){
        return testManDao.setDuration(planWorkDays,workDays,sample_id);
    }

    public BigDecimal queryPlanFinishTime(String sample_id){
        return testManDao.queryPlanFinishTime(sample_id);
    }

    public String getMediaId(String filepath,String access_token,String agentid) throws Exception {
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
//        System.out.println("chunkNumbers:"+chunkNumbers);
//        System.out.println("response.getBody():"+response.getBody());

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

                DingTalkClient chunkClient = new DefaultDingTalkClient("https://oapi.dingtalk.com/file/upload/chunk?"+ WebUtils.buildQuery(chunkRequest.getTextParams(),"utf-8"));

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

    public void sendDingFileToUser(String accesstoken,String filename,String media_id,String userid,String agentid) throws ApiException, IOException {
        OapiCspaceAddToSingleChatRequest request = new OapiCspaceAddToSingleChatRequest();
        request.setAgentId(agentid);
        request.setUserid(userid);
        request.setMediaId(media_id);
        request.setFileName(filename);
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/cspace/add_to_single_chat?"+WebUtils.buildQuery(request.getTextParams(),"utf-8"));
        OapiCspaceAddToSingleChatResponse response = client.execute(request, accesstoken);
        logger.info("sendDingFileToUser successfully."+response.getBody());
    }



    public String getInterfaceData(int id){
        return testManDao.getInterfaceData(id);
    }

    public void updateInterface(String id,String interfaceInfo){
        testManDao.updateInterface(id, interfaceInfo);
    }

    public List<PassbackData> getReceivedData(){
        return testManDao.getReceivedData();
    }

    public int queryElectricalCode(String sample_id){
        return testManDao.queryElectricalCode(sample_id);

    }

    public int insertElectricInfo(PassbackData passbackData){
        return testManDao.insertElectricInfo(passbackData);

    }

    public int updateElectricInfo(PassbackData passbackData){
        return testManDao.updateElectricInfo(passbackData);

    }

//    public List<ElectricScheduleInfo> getAllSchedules(){
//        return testManDao.getAllSchedules();
//    }

    public int saveScheduleDays(String sample_id,String scheduleDays){
        return testManDao.saveScheduleDays(sample_id, scheduleDays);
    }

    public List<PassbackData> getPassbackByElectricInfoIds(List<String> electricInfoIds){
        return testManDao.getPassbackByElectricInfoIds(electricInfoIds);
    }


    public List<ElectricScheduleInfo> getSchedulesByStartDate(String startDate){
        return testManDao.getSchedulesByStartDate(startDate);
    }
    public List<ElectricScheduleInfo> getSchedulesByStartAndEndDate(String startDate, String endDate){
        return testManDao.getSchedulesByStartAndEndDate(startDate, endDate);
    }

    public boolean cancelElectricalCode(String sample_id, String cancel_reason, String cancel_by, String cancel_code, LocalDateTime cancel_date){
        return testManDao.cancelElectricalCode(sample_id,cancel_reason,cancel_by,
                cancel_code,cancel_date);
    }

    public boolean StartTestElectricalTest(String sample_id,String actual_start_time){
        return testManDao.StartTestElectricalTest(sample_id,actual_start_time);
    }
    public boolean FinishTestElectricalTest(String sample_id,String actual_finish_time){
        return testManDao.FinishTestElectricalTest(sample_id,actual_finish_time);
    }

    public String queryActualWorkTime(String sample_id){
        return testManDao.queryActualWorkTime(sample_id);
    }

    public void processScheduleUpdate(String sampleId, Map<String, String> latestChange) {
        // 这里写你的数据库操作逻辑
        String change = latestChange.get("change");
        String sample_id = latestChange.get("sample_id");
        String tester = latestChange.get("tester");
        String start_date = latestChange.get("start_date");
        String end_date = latestChange.get("end_date");
        String scheduleDays = latestChange.get("scheduleDays");
        String schedule_color = latestChange.get("schedule_color");

        // 如果scheduleDays没有值，这里会显示为null
//        System.out.println("scheduleDays:"+scheduleDays);
        if ("delete".equals(change)) {
            // 更新 electric_info 里的 sample_id 的 isUsed = 0
            testManDao.deleteElectric_info(sample_id);

            System.out.println("count>0的delete的:"+latestChange);
        } else if ("add".equals(change)) {
            // 更新 electric_info 里的 sample_id 的 isUsed = 1
            testManDao.updateElectric_info(sample_id,tester,start_date,end_date,scheduleDays,schedule_color);
            System.out.println("count>0的add的:"+latestChange);
        }
        // 查询旧数据
        Map<String, Object> oldSchedule = testManDao.getScheduleInfoBySampleId(sampleId);
        if (oldSchedule != null) {
            String changeLog = oldSchedule.get("tester") + "#" +
                    oldSchedule.get("schedule_start_date") + "#" +
                    oldSchedule.get("schedule_end_date") + "#" +
                    oldSchedule.get("update_time") + "#" +
                    oldSchedule.get("schedule_color") + "#" +
                    oldSchedule.get("isUsed");

            // 获取 electric_info 表原有的 changeRecord 内容
            String existingLog = getChangeRecordBySampleId(sampleId);
            String newLog = (existingLog == null || existingLog.isEmpty())
                    ? changeLog
                    : existingLog + " | " + changeLog;

            // 更新 electric_info.changeRecord 字段
            testManDao.updateChangeRecord(sampleId, newLog);
        }



    }

    public List<Map<String, Object>> getAllTesters(){
        return testManDao.getAllTesters();
    }

    public void insertElectricalTestItem(String sample_id,  List<ElectricalTestItem> list){
        testManDao.insertElectricalTestItem(sample_id, list);
    }


    public void insertMaterialItem(String sample_id, List<MaterialItem> list){
        testManDao.insertMaterialItem(sample_id, list);
    }


    public List<PassbackData> getAllReceivedData(String sample_id){
        return testManDao.getAllReceivedData(sample_id);
    }


    public List<PassbackData> getPendingSampleData(String waitSample_classify){
        return testManDao.getPendingSampleData(waitSample_classify);
    }

    public void saveAll(List<PassbackData> requestData) {
        for (PassbackData data : requestData) {
            String sampleId = data.getSample_id();

            int exist = queryElectricalCode(sampleId);
            if (exist == 0) {
                int insert = insertElectricInfo(data);
                if (insert <= 0) {
                    throw new RuntimeException("插入 electric_info 失败: " + sampleId);
                }
            } else {
                int update = updateElectricInfo(data);
                if (update <= 0) {
                    throw new RuntimeException("更新 electric_info 失败: " + sampleId);
                }
            }

            if (data.getElectricalTestItems() != null && !data.getElectricalTestItems().isEmpty()) {
                insertElectricalTestItem(sampleId, data.getElectricalTestItems());
            } else {
                // 无电性测试项目
                logger.info("Sample " + sampleId + " 没有electrical_test_items电性测试项目");
            }

            if(data.getMaterialItems()!=null  && !data.getMaterialItems().isEmpty()){
                insertMaterialItem(sampleId, data.getMaterialItems());
            }else{
                logger.info("Sample " + sampleId + " 没有material_items测试项目");
            }


        }
    }


    public void updateElectricInfoColor(String sample_id,String schedule_color){
        testManDao.updateElectricInfoColor(sample_id,schedule_color);
    }
    public void updateScheduleInfoColorIfExists(String sample_id,String schedule_color){
        testManDao.updateScheduleInfoColorIfExists(sample_id,schedule_color);
    }

    public void updateElectricInfoReview(String sample_id,String reportReviewTime,String sampleRecognizeResult){
        testManDao.updateElectricInfoReview(sample_id,reportReviewTime,sampleRecognizeResult);
    }


    public void saveElectricInfoFilePath(String sample_id,String filepath){
        testManDao.saveElectricInfoFilePath(sample_id,filepath);
    }

    public String queryElectricInfoFilepath(String sample_id){
        return testManDao.queryElectricInfoFilepath(sample_id);
    }


    public void saveSystemInfoChange(List<SystemInfo> list) {
        for (SystemInfo info : list) {
            System.out.println("info:"+info);
            testManDao.saveSystemInfoChange(info);
        }
    }

    public int findByComputerName(String computerName){
        return testManDao.findByComputerName(computerName);
    }

    public void updateSystemInfoByXlsx(SystemInfo systemInfo){
        testManDao.updateSystemInfoByXlsx(systemInfo);
    }

    public void insertSystemInfoByXlsx(SystemInfo systemInfo){
        testManDao.insertSystemInfoByXlsx(systemInfo);
    }

    public void deleteSystemInfoById(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            for (Integer id : ids) {
                // 逐个删除每个 ID
                testManDao.deleteSystemInfoById(id);
            }
        }
    }

    public String queryJobnumberFromUser(String username){
        return testManDao.queryJobnumberFromUser(username);
    }

    public List<String> getScheduleSampleIdByName(String tester){
        return testManDao.getScheduleSampleIdByName(tester);
    }

    public List<PassbackData> getElectricInfo(String sample_id){
        return testManDao.getElectricInfo(sample_id);
    }

    public List<String> getMaterialCodes(String sampleId) {
        List<MaterialItem> items = getDistinctMaterialCodes(sampleId);

        // 将 STTestCode 和 sample_frequency 拼接为 STTestCode-sample_frequency
        return items.stream()
                .map(item -> item.getMaterialCode() + "-" + item.getSample_frequency())
                .collect(Collectors.toList());
    }

    public List<MaterialItem> getDistinctMaterialCodes(String sample_id){
        return testManDao.getDistinctMaterialCodes(sample_id);
    }

    public int insertSampleFromElectric(Samples sample){
        return testManDao.insertSampleFromElectric(sample);
    }

    public int updateActualSampleId(String sample_id,String sample_actual_id){
        return testManDao.updateActualSampleId(sample_id,sample_actual_id);
    }

    public int removeTargetIdFromAllSampleActualIds(int targetId){
        return testManDao.removeTargetIdFromAllSampleActualIds(targetId);
    }

    public Map<String, Object> pushToRemoteElectricalFinish(String testNumber, String actualWorkTime) {
        Map<String, Object> result = new HashMap<>();

        String actualTestEndDate = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String, String> requestPayload = new HashMap<>();
        requestPayload.put("ETTestCode", testNumber);
        requestPayload.put("ActualTestEndDate", actualTestEndDate);
        requestPayload.put("ActualTestWorkHour", actualWorkTime);
        requestPayload.put("ActualTestWrokHour", actualWorkTime); // 注意拼写是否正确

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic MzUxMDpMaXVkaW5nMjAyMg==");

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestPayload, headers);

        String targetUrl = "https://www.ugreensmart.com/backend/ugreen-qc/Api/ElectricalTest/FinishTestElectricalTest";
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> apiResponse = restTemplate.exchange(targetUrl, HttpMethod.POST, requestEntity, Map.class);
            result.put("remoteStatus", apiResponse.getStatusCodeValue());
            result.put("remoteBody", apiResponse.getBody());
        } catch (Exception e) {
            result.put("remoteStatus", 500);
            result.put("remoteError", "调用远程接口失败: " + e.getMessage());
        }

        return result;
    }

    public String queryElectricIdByActualId(String sample_actual_id){
        return testManDao.queryElectricIdByActualId(sample_actual_id);
    }

    public int updateElectricActualEndTime(String sample_id){
        return testManDao.updateElectricActualEndTime(sample_id);
    }

    public Map<String, Object> processTestElectricalTest(String testNumber, String sampleRecognizeResult,String filePath) {
        String reportReviewTime = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);  // 输出 2025-04-14T16:34:23

        Map<String, Object> result = new HashMap<>();

        if (testNumber == null || testNumber.isEmpty()) {
            result.put("staus", 400);
            result.put("msg", "测试编号不能为空");
        }

        // 本地保存数据库状态
        updateElectricInfoReview(testNumber, reportReviewTime, sampleRecognizeResult);

//        System.out.println("testNumber:"+testNumber);
        try {
            // 拼接文件路径
//            String filePath = queryElectricInfoFilepath(testNumber);
            File file = new File(filePath);

            // 日志输出文件信息
            System.out.println("准备上传文件路径：" + filePath);
            System.out.println("文件是否存在：" + file.exists());
            System.out.println("文件大小：" + file.length());

            if (!file.exists()) {
                result.put("staus", 404);
                result.put("msg", "未找到对应的文件: " + filePath);
            }

            // request JSON 字符串
            Map<String, String> jsonMap = new HashMap<>();
            jsonMap.put("ETTestCode", testNumber);
            jsonMap.put("ReportReviewTime", reportReviewTime);
            jsonMap.put("SampleRecognizeResult", sampleRecognizeResult);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(jsonMap);

            System.out.println("发送 JSON 参数：" + jsonString);

            // 构造 multipart/form-data 请求体
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("request", jsonString); // 不再用 HttpEntity 包裹
            body.add("fileList", new FileSystemResource(file));

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Basic MzUxMDpMaXVkaW5nMjAyMg==");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 发送请求
            String targetUrl = "https://www.ugreensmart.com/backend/ugreen-qc/Api/ElectricalTest/ReportReviewElectricalTest";
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.exchange(targetUrl, HttpMethod.POST, requestEntity, Map.class);

            result.put("remoteStatus", response.getStatusCodeValue());
            result.put("remoteBody", response.getBody());

            System.out.println("远程接口响应状态码：" + response.getStatusCode());
            System.out.println("远程接口响应体：" + response.getBody());




        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈
            result.put("staus", 500);
            result.put("msg",e.getMessage());

        }
        return result;
    }

    public String getChangeRecordBySampleId(String sample_id){
        return testManDao.getChangeRecordBySampleId(sample_id);
    }

}
