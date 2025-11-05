package com.lu.ddwyydemo04.dao;

import com.lu.ddwyydemo04.pojo.*;

import com.lu.ddwyydemo04.pojo.ElectricalTestItem;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface TestManDao {

    public List<Samples> getTestManPanel(@Param("tester") String tester);

    public int queryCountTotal(@Param("name")String name);

    public void createTotal(TotalData totalData);

    public void updateTotal(@Param("name")String name);

    public List<Samples> searchSamples(@Param("tester") String tester,@Param("keyword") String keyword);

    public List<Samples> searchSamplesByAsc(@Param("tester") String tester,@Param("keyword") String keyword);

    public List<Samples> searchSamplesByDesc(@Param("tester") String tester,@Param("keyword") String keyword);

    public void updateSample(Samples sample);

    public void updateSampleTeamWork(Samples sample);

    public void finishTest(@Param("sample_schedule")String sample_schedule,@Param("sample_id")String sample_id);

    public void finishTestWithoutTime(@Param("sample_schedule")String sample_schedule,@Param("finish_time")String finish_time,
                                      @Param("sample_id")String sample_id);


    public LocalDateTime  queryCreateTime(@Param("sample_id")String sample_id);
    public String querySample_name(String sample_id);

    public String queryFilepath(@Param("sample_id")String sample_id);

    public String queryTester_teamwork(String sample_id);

    public String queryTester(String sample_id);

    public int deleteFromTestIssues(int sample_id);//根据文件删除sample数据库的数据
    public int deleteFromSamples(int sample_id);//根据文件删除sample数据库的数据

    //提取问题点相关
    public int querySampleId(String filepath);//根据文件删除sample数据库的数据

    public int insertTestIssues(TestIssues testIssues);


    public int queryHistoryid(@Param("sample_id") int sample_id);
    public int setDuration(@Param("planTestDuration") double planTestDuration,@Param("testDuration") double testDuration,
                           @Param("sample_id") String sample_id);


    public BigDecimal queryPlanFinishTime(@Param("sample_id")String sample_id);


    public List<Samples> searchSampleTestMan(@Param("keyword") String keyword,
                                             @Param("problemTimeStart")String problemTimeStart,
                                             @Param("problemTimeEnd")String problemTimeEnd,
                                             @Param("problemFinishStart")String problemFinishStart,
                                             @Param("problemFinishEnd")String problemFinishEnd,
                                             @Param("sample_schedule")String sample_schedule);

    public String getInterfaceData(@Param("id")int id);
    public void updateInterface(@Param("id")String id,@Param("interfaceInfo")String interfaceInfo);

    public List<PassbackData> getReceivedData();

    public int queryElectricalCode(@Param("sample_id")String sample_id);
    public int insertElectricInfo(PassbackData passbackData);
    public int updateElectricInfo(PassbackData passbackData);

//    public List<ElectricScheduleInfo> getAllSchedules();

    public int saveScheduleDays(@Param("sample_id")String sample_id, @Param("scheduleDays")String scheduleDays);
    public int saveRemark(@Param("sample_id")String sample_id, @Param("remark")String remark);

    public List<PassbackData> getPassbackByElectricInfoIds(@Param("electricInfoIds")List<String> electricInfoIds);


    public List<ElectricScheduleInfo> getSchedulesByStartDate(@Param("schedule_start_date")String schedule_start_date);

    public List<ElectricScheduleInfo> getSchedulesByStartAndEndDate(@Param("schedule_start_date")String schedule_start_date,
                                                                    @Param("schedule_end_date")String schedule_end_date);


    public boolean cancelElectricalCode(@Param("sample_id") String sample_id,@Param("cancel_reason")  String cancel_reason, @Param("cancel_by") String cancel_by,
                                        @Param("cancel_code") String cancel_code,@Param("cancel_date")  LocalDateTime cancel_date);

    public boolean StartTestElectricalTest(@Param("sample_id")String sample_id,@Param("actual_start_time")String actual_start_time);
    public boolean FinishTestElectricalTest(@Param("sample_id")String sample_id,@Param("actual_finish_time")String actual_finish_time);
    public String queryActualWorkTime(@Param("sample_id")String sample_id);
//    public int getCountSchedules(@Param("sample_id")String sample_id);

    public void deleteElectric_info(@Param("sample_id")String sample_id,@Param("waitSample_classify")String waitSample_classify);


    public void updateElectric_info(@Param("sample_id")String sample_id,
                                                @Param("tester")String tester,
                                                @Param("schedule_start_date")String schedule_start_date,
                                                @Param("schedule_end_date")String schedule_end_date,
                                                @Param("scheduleDays")String scheduleDays,
                                                @Param("schedule_color")String schedule_color
                                             );

    public List<Map<String, Object>> getAllTesters();

    public void insertElectricalTestItem(@Param("sample_id") String sample_id,
                                         @Param("list") List<ElectricalTestItem> list);



    public void insertMaterialItem(@Param("sample_id") String sample_id,
                                   @Param("list") List<MaterialItem> list);

    public List<PassbackData> getAllReceivedData(@Param("sample_id") String sample_id);
    public List<PassbackData> getPendingSampleData(@Param("waitSample_classify") String waitSample_classify, 
                                                     @Param("sample_category") String sample_category);


    Map<String, Object> getScheduleInfoBySampleId(@Param("sample_id") String sample_id);

    // 查询 changeRecord 原内容
    String getChangeRecordBySampleId(@Param("sample_id") String sample_id);

    // 更新 electric_info 表中 changeRecord 字段
    void updateChangeRecord(@Param("sample_id") String sample_id, @Param("changeRecord") String changeRecord);

    void updateElectricInfoColor(@Param("sample_id")String sample_id,@Param("schedule_color") String schedule_color);

    void updateElectricInfoReview(@Param("sample_id")String sample_id,@Param("reportReviewTime") String reportReviewTime,@Param("sampleRecognizeResult") String sampleRecognizeResult);
    void updateRdElectricInfoReview(@Param("sample_id")String sample_id,@Param("reportReviewTime") String reportReviewTime,@Param("rd_sampleRecognizeResult") String rd_sampleRecognizeResult);

    void saveElectricInfoFilePath(@Param("sample_id") String sample_id,@Param("filepath")String filepath);

    String queryElectricInfoFilepath(@Param("sample_id") String sample_id);
    void saveSystemInfoChange(SystemInfo systemInfo);
    int  findByComputerName(String computerName);
    void  updateSystemInfoByXlsx(SystemInfo systemInfo);
    void  insertSystemInfoByXlsx(SystemInfo systemInfo);

    void deleteSystemInfoById(int id);

    String queryJobnumberFromUser(@Param("username")String username);

    List<String> getScheduleSampleIdByName(@Param("tester")String testers);
    List<PassbackData> getElectricInfo(@Param("sample_id")String sample_id);

    List<MaterialItem> getDistinctMaterialCodes(@Param("sample_id")String sample_id);

    int insertSampleFromElectric(Samples sample);
    int updateActualSampleId(@Param("sample_id")String sample_id,@Param("sample_actual_id")String sample_actual_id,
                             @Param("actual_start_time")String actual_start_time);

    int removeTargetIdFromAllSampleActualIds(@Param("targetId")int targetId);

    String queryElectricIdByActualId(@Param("sample_actual_id")String sample_actual_id);

    int updateElectricActualEndTime(@Param("sample_id")String sample_id);

    int insertElectric_sample_id(@Param("electric_sample_id")String electric_sample_id,@Param("sample_id")String sample_id);

    String queryElectric_sample_id(@Param("sample_id")String sample_id);

    int updateRemark(@Param("sample_id") String sample_id,@Param("remark")String remark);
    String queryRemark(@Param("sample_id") String sample_id);

    /**
     * 获取提单待测试的数量：sample_sender=username 且 actual_start_time 为空
     * @param username 用户名
     * @return 提单待测试的数量
     */
    public int getLadingBillWaitTestCount(@Param("username") String username);

    /**
     * 获取测试进行中的数量：sample_sender=username 且 actual_start_time 有值但 actual_finish_time 为空
     * @param username 用户名
     * @return 测试进行中的数量
     */
    public int getTestingCount(@Param("username") String username);

    // 查询测试项数据
    List<ElectricalTestItem> getTestItemsBySampleId(@Param("sample_id") String sample_id);

    List<TrashProject> getTrashedProjects();
    boolean recoverProjectById(@Param("sample_id")String sample_id);
    boolean updateElectricSampleId(@Param("oldSample_id")String oldSample_id,@Param("newSample_id")String newSample_id);
    boolean migrateData(@Param("oldSample_id")String oldSample_id,@Param("newSample_id")String newSample_id);
    boolean updateSamplesElectircid(@Param("oldElectric_sample_id")String oldElectric_sample_id,@Param("newElectric_sample_id")String newElectric_sample_id);
    boolean deleteElectricInfoBySampleId(@Param("sample_id")String sample_id);

    List<Holiday> getAllHolidays();
    boolean addHoliday(@Param("holiday_date")String holiday_date,@Param("holiday_name")String holiday_name);
    int queryHoliday(@Param("holiday_date")String holiday_date);
    boolean updateHoliday(@Param("holiday_date")String holiday_date,@Param("holiday_name")String holiday_name);
    boolean deleteHoliday(@Param("id")int id);

    String getProductApprovalDoc(@Param("sample_id")String sample_id);
    String getProductRequirement(@Param("sample_id")String sample_id);

    // 插入变更记录到 change_records 表
    int insertChangeRecord(ChangeRecord changeRecord);

    // 从 change_records 表获取变更记录
    List<ChangeRecord> getChangeRecordsBySampleId(@Param("sample_id") String sample_id);

    int queryCountElectricinfo(@Param("sample_sender")String sample_sender);

    // 获取提单待测试的项目详情
    List<Map<String, Object>> getLadingBillWaitTestDetails(@Param("username") String username);

    // 获取测试进行中的项目详情
    List<Map<String, Object>> getTestingDetails(@Param("username") String username);

    // 获取所有项目详情
    List<Map<String, Object>> getAllProjectDetails(@Param("username") String username);

    // 获取闭环完成的数量
    int getClosedCount(@Param("username") String username);

    // 获取闭环完成的项目详情
    List<Map<String, Object>> getClosedDetails(@Param("username") String username);

    int updateSamplePassbackConfirm(@Param("sample_id")int sample_id,@Param("passbackConfirm")String passbackConfirm);

    // 根据样品ID查询样品详细信息
    Samples getSampleById(@Param("sample_id")int sample_id);

}
