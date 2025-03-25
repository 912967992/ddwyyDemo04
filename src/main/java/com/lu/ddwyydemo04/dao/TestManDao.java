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

    public List<PassbackData> getPassbackByElectricInfoIds(@Param("electricInfoIds")List<Integer> electricInfoIds);


    public List<ElectricScheduleInfo> getSchedulesByStartDate(@Param("schedule_start_date")String schedule_start_date);

    public List<ElectricScheduleInfo> getSchedulesByStartAndEndDate(@Param("schedule_start_date")String schedule_start_date,
                                                                    @Param("schedule_end_date")String schedule_end_date);




}
