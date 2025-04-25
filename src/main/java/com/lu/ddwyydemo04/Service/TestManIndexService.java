package com.lu.ddwyydemo04.Service;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiCspaceAddToSingleChatRequest;
import com.dingtalk.api.request.OapiFileUploadChunkRequest;
import com.dingtalk.api.request.OapiFileUploadTransactionRequest;
import com.dingtalk.api.response.OapiCspaceAddToSingleChatResponse;
import com.dingtalk.api.response.OapiFileUploadChunkResponse;
import com.dingtalk.api.response.OapiFileUploadTransactionResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


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

    public void finishTest(String schedule,int sample_id){
        testManDao.finishTest(schedule,sample_id);
    }
    public void finishTestWithoutTime(String schedule,String finish_time,int sample_id){

        testManDao.finishTestWithoutTime(schedule,finish_time,sample_id);
    }



    public LocalDateTime  queryCreateTime(int sample_id){
        return testManDao.queryCreateTime(sample_id);
    }

    public String queryTester_teamwork(int sample_id){
        return testManDao.queryTester_teamwork(sample_id);
    }

    public String querySample_name(int sample_id){
        return testManDao.querySample_name(sample_id);
    }

    public String queryFilepath(int sample_id){
        return testManDao.queryFilepath(sample_id);
    }

    public String queryTester(int sample_id){
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
    public int setDuration(double planWorkDays,double workDays,int sample_id){
        return testManDao.setDuration(planWorkDays,workDays,sample_id);
    }

    public BigDecimal queryPlanFinishTime(int sample_id){
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

    public List<ElectricScheduleInfo> getAllSchedules(){
        return testManDao.getAllSchedules();
    }

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

    public void processScheduleUpdate(String sampleId, Map<String, String> latestChange, List<Map<String, String>> allChanges) {
        // 这里写你的数据库操作逻辑
        String change = latestChange.get("change");
        String sample_id = latestChange.get("sample_id");
        String sizecoding = latestChange.get("sizecoding");
        String tester = latestChange.get("tester");
        String start_date = latestChange.get("start_date");
        String end_date = latestChange.get("end_date");
        String scheduleDays = latestChange.get("scheduleDays");
        String schedule_color = latestChange.get("schedule_color");

        // 如果scheduleDays没有值，这里会显示为null
//        System.out.println("scheduleDays:"+scheduleDays);

        int count = testManDao.getCountSchedules(sampleId);

        if(count>0){
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
                String existingLog = testManDao.getChangeRecordBySampleId(sampleId);
                String newLog = (existingLog == null || existingLog.isEmpty())
                        ? changeLog
                        : existingLog + " | " + changeLog;

                // 更新 electric_info.changeRecord 字段
                testManDao.updateChangeRecord(sampleId, newLog);
            }


            if ("delete".equals(latestChange.get("change"))) {
                // 删除 electric_schedule_info 数据
                // 更新 electric_info 里的 sample_id 的 isUsed = 0

                testManDao.deleteElectric_schedule_info(sample_id);

                System.out.println("count>0的delete的:"+latestChange);
            } else if ("add".equals(latestChange.get("change"))) {
                // 新增 electric_schedule_info 数据
                // 更新 electric_info 里的 sample_id 的 isUsed = 1
                testManDao.updateElectric_schedule_info(sample_id,tester,start_date,end_date,sizecoding,scheduleDays,schedule_color);
                System.out.println("count>0的add的:"+latestChange);
            }
        }else{
            if ("delete".equals(latestChange.get("change"))) {
                // 数据库本来就无此条数据，故无需操作
                System.out.println("count<0的delete的:"+latestChange);
            } else if ("add".equals(latestChange.get("change"))) {
                // 新增 electric_schedule_info 数据
                // 更新 electric_info 里的 sample_id 的 isUsed = 1
                testManDao.insertElectric_schedule_info(sample_id,tester,start_date,end_date,sizecoding,schedule_color);
                testManDao.changeIsUsedAsOne(sample_id,scheduleDays);
                System.out.println("count<0的add的:" + latestChange);
            }
        }
        // 示例：更新数据库


        // 记录变更日志（可以存入数据库或者日志表）
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

    @Transactional
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
            System.out.println("data.getElectricalTestItems():"+data.getElectricalTestItems());

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

    @Transactional
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


}
