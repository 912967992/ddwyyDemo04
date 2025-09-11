package com.lu.ddwyydemo04.dao;

import com.lu.ddwyydemo04.pojo.TestIssues;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ProblemLibraryDao {


    /**
     * 根据条件搜索问题点
     * @param filters 搜索条件
     * @return 问题点列表
     */
    List<TestIssues> searchProblems(@Param("filters") Map<String, Object> filters);

    /**
     * 更新问题点信息
     * @param testIssues 问题点对象
     * @return 影响行数
     */
    int updateProblem(TestIssues testIssues);

    /**
     * 根据ID获取问题点详情
     * @param id 问题点ID
     * @return 问题点对象
     */
    TestIssues getProblemById(@Param("id") Long id);



    /**
     * 获取指定sample_id的历史版本
     * @param sampleId 样品ID
     * @return 历史版本列表
     */
    List<TestIssues> getHistoryVersions(@Param("sampleId") String sampleId);

    /**
     * 获取指定sample_id的最新版本
     * @param sampleId 样品ID
     * @return 最新版本
     */
    TestIssues getLatestVersion(@Param("sampleId") String sampleId);

    /**
     * 获取大类小类选项
     * @return 包含大类和小类选项的Map
     */
    Map<String, Object> getSpeciesOptions();
}
