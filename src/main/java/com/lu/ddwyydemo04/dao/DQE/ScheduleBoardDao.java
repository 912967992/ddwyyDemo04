package com.lu.ddwyydemo04.dao.DQE;

import com.lu.ddwyydemo04.pojo.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ScheduleBoardDao {

    List<TestEngineerInfo> getTestEngineers();
    List<String> getSampleCategories();
    void updateTesterInfo(TesterInfo testerInfo);

    int queryExistTester(TesterInfo testerInfo);
    Map<String, String> queryJobnumber(TesterInfo testerInfo);
    boolean insertTesterInfo(TesterInfo testerInfo);

    boolean deleteTester(@Param("test_engineer_name")String test_engineer_name);

    List<Group> getGroupData();
    void updateGroup(Group group);
    void addGroup(Group group);

    List<Group> getAllGroupsOrderByDisplayOrder();

    List<PassbackData> searchProjects(@Param("sample_id")String sample_id, @Param("sample_model")String sample_model, @Param("tester")String tester, @Param("createTimeStart")String createTimeStart, @Param("createTimeEnd")String createTimeEnd, @Param("sampleCategory")String sampleCategory, @Param("sampleSender")String sampleSender, @Param("dqeGroup")String dqeGroup);

    /**
     * 根据送样人获取DQE分组
     */
    String getDqeGroupBySender(@Param("senderName") String senderName);

    /**
     * 获取所有DQE分组信息
     */
    Map<String, String> getAllDqeGroups();

    /**
     * 获取所有DQE分组名称列表
     */
    List<String> getAllDqeGroupNames();

    /**
     * 获取所有DQE分组详细信息
     */
    List<Map<String, String>> getAllDqeGroupDetails();

    /**
     * 添加DQE分组
     */
    boolean addDqeGroup(@Param("groupName") String groupName);

    /**
     * 删除DQE分组
     */
    boolean deleteDqeGroup(@Param("groupName") String groupName);

    /**
     * 从分组中移除人员
     */
    boolean removeMemberFromGroup(@Param("memberName") String memberName, @Param("groupName") String groupName);

    /**
     * 添加人员到分组
     */
    boolean addMemberToGroup(@Param("memberName") String memberName, @Param("groupName") String groupName);

    /**
     * 基于 changeRecord 的搜索功能
     */
    List<Map<String, Object>> searchByChangeRecord(@Param("tester") String tester,
                                                   @Param("startDate") String startDate,
                                                   @Param("endDate") String endDate,
                                                   @Param("isUsed") String isUsed,
                                                   @Param("remark") String remark);

    /**
     * 获取变更记录统计信息
     */
    Map<String, Object> getChangeRecordStats(@Param("startDate") String startDate,
                                           @Param("endDate") String endDate);

    /**
     * 测试方法：查看 changeRecord 数据
     */
    List<PassbackData> testChangeRecord();

    /**
     * 获取所有 changeRecord 数据
     */
    List<String> getAllChangeRecords();

    boolean deleteGroupById(@Param("id")String id);

    /**
     * 获取所有变更记录数据
     */
    List<PassbackData> getAllChangeRecordData();

    /**
     * 获取指定时间之后的变更记录数据
     */
    List<PassbackData> getChangeRecordDataAfterTime(@Param("lastSyncTime") String lastSyncTime);

    /**
     * 基于新表的变更记录搜索
     */
    List<Map<String, Object>> searchByChangeRecordNew(@Param("ETTestCode") String ETTestCode,
                                                     @Param("sampleModel") String sampleModel,
                                                     @Param("sampleCategory") String sampleCategory,
                                                     @Param("sampleLeader") String sampleLeader,
                                                     @Param("sampleSender") String sampleSender,
                                                     @Param("tester") String tester,
                                                     @Param("startDate") String startDate,
                                                     @Param("endDate") String endDate,
                                                     @Param("isUsed") String isUsed,
                                                     @Param("isCancel") String isCancel,
                                                     @Param("schedule") String schedule,
                                                     @Param("remark") String remark,
                                                     @Param("dateRangeStart") String dateRangeStart,
                                                     @Param("dateRangeEnd") String dateRangeEnd,
                                                     @Param("changeRecordRemark") String changeRecordRemark);

    /**
     * 获取变更记录统计信息（基于新表）
     */
    Map<String, Object> getChangeRecordStatsNew(@Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    int updateScheduleColorGray();
    int updateScheduleColorGreen();

    List<Map<String, Object>> findDQEUnconfirmedSamples();
    List<Map<String, Object>> findRDUnconfirmedSamples();
    List<Map<String, Object>> findTesterUnconfirmedSamples();




}
