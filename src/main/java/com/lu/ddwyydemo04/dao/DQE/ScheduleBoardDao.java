package com.lu.ddwyydemo04.dao.DQE;

import com.lu.ddwyydemo04.pojo.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ScheduleBoardDao {

    List<TestEngineerInfo> getTestEngineers();
    void updateTesterInfo(TesterInfo testerInfo);

    int queryExistTester(TesterInfo testerInfo);
    Map<String, String> queryJobnumber(TesterInfo testerInfo);
    boolean insertTesterInfo(TesterInfo testerInfo);

    boolean deleteTester(@Param("test_engineer_name")String test_engineer_name);

    List<Group> getGroupData();
    void updateGroup(Group group);
    void addGroup(Group group);

    List<Group> getAllGroupsOrderByDisplayOrder();

    List<PassbackData> searchProjects(@Param("sample_id")String sample_id, @Param("sample_model")String sample_model, @Param("tester")String tester);

    boolean deleteGroupById(@Param("id")String id);




}
