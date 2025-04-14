package com.lu.ddwyydemo04.dao;

import com.lu.ddwyydemo04.pojo.*;
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

    public void finishTest(@Param("sample_schedule")String sample_schedule,@Param("sample_id")int sample_id);

    public void finishTestWithoutTime(@Param("sample_schedule")String sample_schedule,@Param("finish_time")String finish_time,
                                      @Param("sample_id")int sample_id);


    public LocalDateTime  queryCreateTime(@Param("sample_id")int sample_id);
    public String querySample_name(int sample_id);

    public String queryFilepath(@Param("sample_id")int sample_id);

    public String queryTester_teamwork(int sample_id);

    public String queryTester(int sample_id);

    public int deleteFromTestIssues(int sample_id);//根据文件删除sample数据库的数据
    public int deleteFromSamples(int sample_id);//根据文件删除sample数据库的数据

    //提取问题点相关
    public int querySampleId(String filepath);//根据文件删除sample数据库的数据

    public int insertTestIssues(TestIssues testIssues);


    public int queryHistoryid(@Param("sample_id") int sample_id);
    public int setDuration(@Param("planTestDuration") double planTestDuration,@Param("testDuration") double testDuration,
                           @Param("sample_id") int sample_id);


    public BigDecimal queryPlanFinishTime(@Param("sample_id")int sample_id);


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

    public List<ElectricScheduleInfo> getAllSchedules();

    public int saveScheduleDays(@Param("sample_id")String sample_id, @Param("scheduleDays")String scheduleDays);

    public List<PassbackData> getPassbackByElectricInfoIds(@Param("electricInfoIds")List<String> electricInfoIds);


    public List<ElectricScheduleInfo> getSchedulesByStartDate(@Param("schedule_start_date")String schedule_start_date);

    public List<ElectricScheduleInfo> getSchedulesByStartAndEndDate(@Param("schedule_start_date")String schedule_start_date,
                                                                    @Param("schedule_end_date")String schedule_end_date);


    public boolean cancelElectricalCode(@Param("sample_id") String sample_id,@Param("cancel_reason")  String cancel_reason, @Param("cancel_by") String cancel_by,
                                        @Param("cancel_code") String cancel_code,@Param("cancel_date")  LocalDateTime cancel_date);

    public boolean StartTestElectricalTest(@Param("sample_id")String sample_id,@Param("actual_start_time")String actual_start_time);
    public boolean FinishTestElectricalTest(@Param("sample_id")String sample_id,@Param("actual_finish_time")String actual_finish_time);

    public int getCountSchedules(@Param("sample_id")String sample_id);

    public boolean deleteElectric_schedule_info(@Param("sample_id")String sample_id,
                                                @Param("tester")String tester,
                                                @Param("schedule_start_date")String schedule_start_date,
                                                @Param("schedule_end_date")String schedule_end_date);

    public boolean changeIsUsedAsZero(@Param("sample_id")String sample_id);
    public boolean changeIsUsedAsOne(@Param("sample_id")String sample_id, @Param("scheduleDays") String scheduleDays);
    public boolean insertElectric_schedule_info(@Param("sample_id")String sample_id,
                                                @Param("tester")String tester,
                                                @Param("schedule_start_date")String schedule_start_date,
                                                @Param("schedule_end_date")String schedule_end_date,
                                                @Param("sizecoding")String sizecoding);

    public boolean updateElectric_schedule_info(@Param("sample_id")String sample_id,
                                                @Param("tester")String tester,
                                                @Param("schedule_start_date")String schedule_start_date,
                                                @Param("schedule_end_date")String schedule_end_date,
                                                @Param("sizecoding")String sizecoding,
                                                @Param("scheduleDays")String scheduleDays);

    public List<String> getAllTesters();

    public void insertElectricalTestItem(@Param("sample_id") String sample_id,
                                         @Param("list") List<ElectricalTestItem> list);



    public void insertMaterialItem(@Param("sample_id") String sample_id,
                                   @Param("list") List<MaterialItem> list);

    public List<PassbackData> getAllReceivedData(@Param("sample_id") String sample_id);


    // 查询 electric_schedule_info 表中记录
    Map<String, Object> getScheduleInfoBySampleId(@Param("sample_id") String sample_id);

    // 查询 changeRecord 原内容
    String getChangeRecordBySampleId(@Param("sample_id") String sample_id);

    // 更新 electric_info 表中 changeRecord 字段
    boolean updateChangeRecord(@Param("sample_id") String sample_id, @Param("changeRecord") String changeRecord);

    void updateElectricInfoColor(@Param("sample_id")String sample_id,@Param("schedule_color") String schedule_color);
    void updateScheduleInfoColorIfExists(@Param("sample_id")String sample_id,@Param("schedule_color") String schedule_color);
    void updateElectricInfoReview(@Param("sample_id")String sample_id,@Param("reportReviewTime") String reportReviewTime,@Param("sampleRecognizeResult") String sampleRecognizeResult);

    void saveElectricInfoFilePath(@Param("sample_id") String sample_id,@Param("filepath")String filepath);

    String queryElectricInfoFilepath(@Param("sample_id") String sample_id);


}
