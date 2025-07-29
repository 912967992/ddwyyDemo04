package com.lu.ddwyydemo04.Service.DQE;

import com.lu.ddwyydemo04.dao.DQE.DQEDao;
import com.lu.ddwyydemo04.dao.DQE.ScheduleBoardDao;
import com.lu.ddwyydemo04.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ScheduleBoardService {

    @Autowired
    private ScheduleBoardDao scheduleBoardDao;

    public List<TestEngineerInfo> getTestEngineers() {
        return scheduleBoardDao.getTestEngineers();
    }

    public void updateTesterInfo(TesterInfo testerInfo){
        scheduleBoardDao.updateTesterInfo(testerInfo);
    }

    public int queryExistTester(TesterInfo testerInfo){
        return scheduleBoardDao.queryExistTester(testerInfo);
    }

    public Map<String, String> queryJobnumber(TesterInfo testerInfo){
        return scheduleBoardDao.queryJobnumber(testerInfo);
    }

    public boolean insertTesterInfo(TesterInfo testerInfo){
        return scheduleBoardDao.insertTesterInfo(testerInfo);
    }

    public boolean deleteTester(String test_engineer_name){
        return scheduleBoardDao.deleteTester(test_engineer_name);
    }


    public List<Group> getGroupData(){
        return scheduleBoardDao.getGroupData();
    }

    public void updateGroup(Group group){
        scheduleBoardDao.updateGroup(group);
    }

    public List<Group> getAllGroupsOrderByDisplayOrder(){
        return scheduleBoardDao.getAllGroupsOrderByDisplayOrder();
    }

    public void addGroup(Group group){
        scheduleBoardDao.addGroup(group);
    }

    public List<PassbackData> searchProjects(String sample_id , String sample_model, String tester){
        return scheduleBoardDao.searchProjects(sample_id, sample_model, tester);
    }

    /**
     * 基于 changeRecord 的搜索功能
     */
    public List<Map<String, Object>> searchByChangeRecord(String tester, String startDate, String endDate, String isUsed, String remark) {
        return scheduleBoardDao.searchByChangeRecord(tester, startDate, endDate, isUsed, remark);
    }

    /**
     * 获取变更记录统计信息
     */
    public Map<String, Object> getChangeRecordStats(String startDate, String endDate) {
        return scheduleBoardDao.getChangeRecordStats(startDate, endDate);
    }

    /**
     * 测试方法：查看 changeRecord 数据
     */
    public List<PassbackData> testChangeRecord() {
        return scheduleBoardDao.testChangeRecord();
    }

    /**
     * 获取所有 changeRecord 数据
     */
    public List<String> getAllChangeRecords() {
        return scheduleBoardDao.getAllChangeRecords();
    }

    public boolean deleteGroupById(String id){
        return scheduleBoardDao.deleteGroupById(id);
    }

    /**
     * 获取所有变更记录数据
     */
    public List<PassbackData> getAllChangeRecordData() {
        return scheduleBoardDao.getAllChangeRecordData();
    }

    /**
     * 获取指定时间之后的变更记录数据
     */
    public List<PassbackData> getChangeRecordDataAfterTime(String lastSyncTime) {
        return scheduleBoardDao.getChangeRecordDataAfterTime(lastSyncTime);
    }

    /**
     * 基于新表的变更记录搜索
     */
    public List<Map<String, Object>> searchByChangeRecordNew(String ETTestCode, String sampleModel, String sampleCategory, 
                                                           String sampleLeader, String sampleSender, String tester, String startDate, 
                                                           String endDate, String isUsed, String isCancel, String schedule, 
                                                           String remark, String dateRangeStart, String dateRangeEnd, String changeRecordRemark) {
        return scheduleBoardDao.searchByChangeRecordNew(ETTestCode, sampleModel, sampleCategory, sampleLeader, sampleSender, 
                                                      tester, startDate, endDate, isUsed, isCancel, schedule, remark, 
                                                      dateRangeStart, dateRangeEnd, changeRecordRemark);
    }

    /**
     * 获取变更记录统计信息（基于新表）
     */
    public Map<String, Object> getChangeRecordStatsNew(String startDate, String endDate) {
        return scheduleBoardDao.getChangeRecordStatsNew(startDate, endDate);
    }

}
