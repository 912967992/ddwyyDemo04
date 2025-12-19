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

    public List<String> getSampleCategories() {
        return scheduleBoardDao.getSampleCategories();
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

    public List<PassbackData> searchProjects(String sample_id , String sample_model, String tester, String createTimeStart, String createTimeEnd, String sampleCategory, String sampleSender, String dqeGroup){
        return scheduleBoardDao.searchProjects(sample_id, sample_model, tester, createTimeStart, createTimeEnd, sampleCategory, sampleSender, dqeGroup);
    }

    /**
     * 根据送样人获取DQE分组
     */
    public String getDqeGroupBySender(String senderName) {
        return scheduleBoardDao.getDqeGroupBySender(senderName);
    }

    /**
     * 获取所有DQE分组信息
     */
    public Map<String, String> getAllDqeGroups() {
        return scheduleBoardDao.getAllDqeGroups();
    }

    /**
     * 获取所有DQE分组名称列表
     */
    public List<String> getAllDqeGroupNames() {
        return scheduleBoardDao.getAllDqeGroupNames();
    }

    /**
     * 获取所有DQE分组详细信息
     */
    public List<Map<String, String>> getAllDqeGroupDetails() {
        return scheduleBoardDao.getAllDqeGroupDetails();
    }

    /**
     * 添加DQE分组
     */
    public boolean addDqeGroup(String groupName) {
        return scheduleBoardDao.addDqeGroup(groupName);
    }

    /**
     * 删除DQE分组
     */
    public boolean deleteDqeGroup(String groupName) {
        return scheduleBoardDao.deleteDqeGroup(groupName);
    }

    /**
     * 从分组中移除人员
     */
    public boolean removeMemberFromGroup(String memberName, String groupName) {
        return scheduleBoardDao.removeMemberFromGroup(memberName, groupName);
    }

    /**
     * 添加人员到分组
     */
    public boolean addMemberToGroup(String memberName, String groupName) {
        return scheduleBoardDao.addMemberToGroup(memberName, groupName);
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

    public int updateScheduleColorGray(String sample_category){
        return scheduleBoardDao.updateScheduleColorGray(sample_category);
    }

    public int updateScheduleColorGreen(String sample_category){
        return scheduleBoardDao.updateScheduleColorGreen(sample_category);
    }

    public List<Map<String, Object>> findDQEUnconfirmedSamples() {
        return scheduleBoardDao.findDQEUnconfirmedSamples();
    }
   public List<Map<String, Object>> findRDUnconfirmedSamples() {
        return scheduleBoardDao.findRDUnconfirmedSamples();
    }
   public List<Map<String, Object>> findTesterUnconfirmedSamples() {
        return scheduleBoardDao.findTesterUnconfirmedSamples();
    }

    /**
     * 获取台账进账数量
     */
    public int getLedgerInCount() {
        return scheduleBoardDao.getLedgerInCount();
    }

    /**
     * 获取台账出账数量
     */
    public int getLedgerOutCount() {
        return scheduleBoardDao.getLedgerOutCount();
    }

    /**
     * 获取台账存账数量
     */
    public int getLedgerStockCount() {
        return scheduleBoardDao.getLedgerStockCount();
    }

    /**
     * 获取台账统计数据
     */
    public Map<String, Object> getLedgerStats() {
        return scheduleBoardDao.getLedgerStats();
    }

    /**
     * 设置初始存账值
     */
    public boolean setInitialStockCount(Integer initialStockCount, String setBy) {
        return scheduleBoardDao.setInitialStockCount(initialStockCount, setBy);
    }

    /**
     * 检查是否已设置初始存账
     */
    public boolean checkInitialStockSet() {
        return scheduleBoardDao.checkInitialStockSet();
    }

    /**
     * 自动计算并保存今天的统计数据
     */
    public void autoCalculateAndSaveTodayStats() {
        scheduleBoardDao.autoCalculateAndSaveTodayStats();
    }

}
