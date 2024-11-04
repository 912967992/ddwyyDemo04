package com.lu.ddwyydemo04.dao.DQE;


import com.lu.ddwyydemo04.pojo.Samples;
import com.lu.ddwyydemo04.pojo.TaskNode;
import com.lu.ddwyydemo04.pojo.TestIssues;

import com.lu.ddwyydemo04.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
            @Param("problemTimeStart") String  problemTimeStart,
            @Param("problemTimeEnd") String problemTimeEnd);
    public Map<String, Object> addNewRow(@Param("sampleId") int sampleId);

    public Map<String, Object> addNewRowHistroyidZero(@Param("sampleId") int sampleId);


    public int searchTestIssuesHistroyidFromId(@Param("sampleId") int sampleId);

    public void updateTestIssues(TestIssues testIssues);
    public void insertTestIssues(TestIssues testIssues);

    public int deleteProblemById(Long id);

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
}
