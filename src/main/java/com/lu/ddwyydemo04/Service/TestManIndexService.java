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


}
