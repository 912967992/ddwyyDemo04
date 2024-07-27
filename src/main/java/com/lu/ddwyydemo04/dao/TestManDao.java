package com.lu.ddwyydemo04.dao;

import com.lu.ddwyydemo04.pojo.FileData;
import com.lu.ddwyydemo04.pojo.Samples;
import com.lu.ddwyydemo04.pojo.TotalData;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    public void finishTest(@Param("sample_schedule")String sample_schedule,@Param("sample_model")String sample_model,
                           @Param("sample_coding")String sample_coding,@Param("sample_category")String sample_category,
                           @Param("version")String version,@Param("big_species")String big_species,@Param("small_species")String small_species,
                           @Param("high_frequency")String high_frequency,@Param("sample_frequency")int sample_frequency,
                           @Param("questStats")String questStats);
    public void finishTestWithoutTime(@Param("sample_schedule")String sample_schedule,@Param("sample_model")String sample_model,
                           @Param("sample_coding")String sample_coding,@Param("sample_category")String sample_category,
                           @Param("version")String version,@Param("finish_time")String finish_time,
                                      @Param("big_species")String big_species,@Param("small_species")String small_species,@Param("high_frequency")String high_frequency,@Param("sample_frequency")int sample_frequency,
                                      @Param("questStats")String questStats);


    public String queryFinishtime(@Param("sample_model")String sample_model,
                                         @Param("sample_coding")String sample_coding, @Param("sample_category")String sample_category,
                                         @Param("version")String version,@Param("big_species")String big_species,@Param("small_species")String small_species,@Param("high_frequency")String high_frequency);
    public String querySample_name(Samples sample);

    public String queryFilepath(Samples sample);

    public String queryTester_teamwork(Samples sample);

    public String queryTester(Samples sample);

    public int deleteFilepath(String filepath);//根据文件删除sample数据库的数据


}
