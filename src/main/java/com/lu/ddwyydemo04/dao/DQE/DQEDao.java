package com.lu.ddwyydemo04.dao.DQE;


import com.lu.ddwyydemo04.pojo.*;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface DQEDao {

    public List<TestIssues> searchTestissues();
    public List<TestIssues> selectTestIssues(List<Integer> selectedIds);
    public List<TestIssues> selectTestIssuesFromSampleid(int sampleId);
    public List<Samples> searchSamplesDQE(
            @Param("sample_id") String sample_id,
            @Param("full_model") String full_model,
            @Param("questStats") String questStats,
            @Param("sample_category") String sample_category,
            @Param("version") String version,
            @Param("big_species") String big_species,
            @Param("small_species") String small_species,
            @Param("supplier") String supplier,
            @Param("test_Overseas") String test_Overseas,
            @Param("sample_DQE") String sample_DQE,
            @Param("sample_Developer") String sample_Developer,
            @Param("tester") String tester,
            @Param("priority") String priority,
            @Param("sample_schedule") String sample_schedule,
            @Param("result_judge") String result_judge,
            @Param("rd_result_judge") String rd_result_judge,
            @Param("problemTimeStart") String  problemTimeStart,
            @Param("problemTimeEnd") String problemTimeEnd,
            @Param("sample_leader") String sample_leader,
            @Param("key") String key);
    public Map<String, Object> addNewRow(@Param("sampleId") int sampleId);

    public Map<String, Object> addNewRowHistroyidZero(@Param("sampleId") int sampleId);


    public int searchTestIssuesHistroyidFromId(@Param("sampleId") int sampleId);

    public void updateTestIssues(TestIssues testIssues);
    public void insertTestIssues(TestIssues testIssues);

    public int deleteProblemById(Long id);
    
    public Long getSampleIdByProblemId(Long problemId);

    int uploadImage(@Param("id") Long id, @Param("problem_image_or_video") String problem_image_or_video);

    Map<String, String> searchScheduleAndResultJudge(@Param("sample_id") String sample_id);


    int updateSampleWithSchAndResult(@Param("sample_id")String sample_id,@Param("sample_schedule") String sample_schedule,
                                     @Param("job") String job, @Param("selectedOption") String selectedOption);




    String getFilePathBySampleId( @Param("sample_id")String sample_id);

    List<Samples> querySamples( @Param("sample_id") String sample_id);

    String queryWarnDays(@Param("setting_role") String setting_rol);


    void insertOrUpdateUser(User user);

    String getUserIdByName(@Param("username") String username);

    int insertTaskNode(TaskNode taskNode);
    int updateTaskNodesOnce(@Param("id")int id, @Param("currentTime") LocalDateTime currentTime);
    int updateTaskNodesSecond(@Param("id")int id,@Param("currentTime") LocalDateTime currentTime);
    int updatePreviousNodes(TaskNode taskNode);
    List<Long> selectTaskId(@Param("sample_id") String sample_id);

    int updateSetting(@Param("setting_role") String setting_role,@Param("warn_days") String warn_days);

    List<Map<String, Object>> findOnceOverdueSampleIds(@Param("currentTime") LocalDateTime currentTime);
//    查询DQE相关的进度师傅第二次超期
    List<Map<String, Object>> findSecondOverdueSampleIdsDQE(@Param("currentTime") LocalDateTime currentTime);
    List<Map<String, Object>> findSecondOverdueSampleIdsRD(@Param("currentTime") LocalDateTime currentTime);

    List<TaskNode> findFirstOverdueSamplesOnce();
    List<TaskNode> getRecentNotifications();
    List<Samples> selectFromSampleId(@Param("sample_id")int selectFromSampleId);

    String getWarningDaysByManager(@Param("manager")String manager);

    Boolean updateOverdueReason(@Param("id")String id, @Param("overdueReason")String overdueReason);

    int insertSys(SystemInfo systemInfo);
    int checkIfComputerNameExists(@Param("computerName")String computerName);
    int updateSys(SystemInfo systemInfo);


    List<Map<String, Object>> countDefectLevelsBySampleId(@Param("sampleId") String sampleId);

    int updatepProblemCounts(@Param("sample_id") String sample_id,@Param("problemCounts") String problemCounts);

    int deleteTaskNodeBefore(@Param("sample_id") String sample_id);


    List<SystemInfo> performSysInfo(@Param("id")String id,
                                    @Param("personCharge")String personCharge,@Param("computerName")String computerName,
                                    @Param("brand")String brand,@Param("area")String area,
                                    @Param("deviceType")String deviceType,
                                    @Param("version")String version,
                                    @Param("osVersion")String osVersion,
                                    @Param("fullOS")String fullOS,
                                    @Param("architecture")String architecture,
                                    @Param("cpu")String cpu,@Param("memory")String memory,
                                    @Param("displays")String displays,
                                    @Param("maxResolution")String maxResolution,
                                    @Param("maxRefreshRate")String maxRefreshRate,@Param("interfaceInfo")String interfaceInfo);


    String getJobFromUsers(@Param("username")String username);

    int updateResult(@Param("sample_id")String sample_id,
                     @Param("job") String job, @Param("selectedOption") String selectedOption);

    int queryResults(@Param("sample_id")String sample_id);
    int updateNodeAsFinishWithDQE(@Param("sample_id")String sample_id);
    int updateNodeAsFinishWithRD(@Param("sample_id")String sample_id);

    Map<String, String> findUserByUsername(@Param("username") String username);

    // 查tb_test_engineer_info表，看工程师是不是已经存在
    Integer countEngineerByName(@Param("testEngineerName") String testEngineerName);

    // 插入新工程师
    void insertEngineer(@Param("engineerId") String engineerId, @Param("testEngineerName") String testEngineerName,
                        @Param("hire_date") String hire_date,
                        @Param("responsible_category")String responsible_category);

    // 更新已有工程师信息
    void updateEngineer(@Param("engineerId") String engineerId, @Param("testEngineerName") String testEngineerName,
                        @Param("hire_date")String hire_date,
                        @Param("responsible_category")String responsible_category);

    String queryResultJudge(@Param("sample_id")String sample_id);
    
    /**
     * 查询所有有效的samples记录（用于Java端过滤）
     */
    List<Samples> findAllValidSamples();

    void insertAgents(@Param("agent_name")String agent_name,@Param("username")String username);
    void updateAgents(@Param("agent_name")String agent_name,@Param("username")String username);

    int queryAgents(@Param("username")String username);
    String getAgents(@Param("username")String username);

    // 获取待审核的项目详情
    List<Map<String, Object>> getRecentNotificationsDetails(@Param("username") String username);

    // 获取超期项目详情
    List<Map<String, Object>> getOverdueDetails(@Param("username") String username);

    String queryPassbakConfirm(@Param("sample_id")String sample_id);

    // 获取电气项目数据
    List<Map<String, Object>> getElectricProjectData();

}
