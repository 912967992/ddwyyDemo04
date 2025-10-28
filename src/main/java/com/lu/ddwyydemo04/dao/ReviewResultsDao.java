package com.lu.ddwyydemo04.dao;

import com.lu.ddwyydemo04.pojo.ReviewResults;
import com.lu.ddwyydemo04.pojo.TestIssuesView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 评审结果数据访问层
 */
@Mapper
public interface ReviewResultsDao {

    /**
     * 插入评审结果数据
     * @param reviewResults 评审结果对象
     * @return 影响行数
     */
    int insert(ReviewResults reviewResults);

    /**
     * 根据大编码、小编码、项目阶段、供应商、问题点查询是否存在记录（版本字段可选）
     * @param majorCode 大编码
     * @param minorCode 小编码
     * @param projectPhase 项目阶段
     * @param version 版本（可为空）
     * @param supplier 供应商
     * @param problemPoint 问题点
     * @return 存在的记录ID，如果不存在返回null
     */
    Long findExistingRecord(@Param("majorCode") String majorCode, 
                           @Param("minorCode") String minorCode, 
                           @Param("projectPhase") String projectPhase, 
                           @Param("version") String version, 
                           @Param("supplier") String supplier,
                           @Param("problemPoint") String problemPoint);

    /**
     * 更新评审结果数据
     * @param reviewResults 评审结果对象
     * @return 影响行数
     */
    int update(ReviewResults reviewResults);

    /**
     * 批量插入评审结果数据
     * @param reviewResultsList 评审结果列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<ReviewResults> reviewResultsList);

    /**
     * 根据ID删除评审结果
     * @param id 记录ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除评审结果
     * @param ids ID列表
     * @return 影响行数
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 根据ID查询评审结果
     * @param id 记录ID
     * @return 评审结果对象
     */
    ReviewResults findById(@Param("id") Long id);

    /**
     * 查询所有评审结果
     * @return 评审结果列表
     */
    List<ReviewResults> findAll();

    /**
     * 根据ID列表查询评审结果
     * @param ids ID列表
     * @return 评审结果列表
     */
    List<ReviewResults> findByIds(@Param("ids") List<Long> ids);

    /**
     * 根据筛选条件删除评审结果
     * @param filters 筛选条件
     * @return 影响行数
     */
    int deleteByFilters(@Param("filters") Map<String, Object> filters);

    /**
     * 根据筛选条件查询评审结果
     * @param filters 筛选条件
     * @return 评审结果列表
     */
    List<ReviewResults> findByFilters(@Param("filters") Map<String, Object> filters);

    /**
     * 查询所有电气测试问题点（联查test_issues和samples表，只返回最新历史版本）
     * @return 电气测试问题点列表
     */
    List<TestIssuesView> findAllTestIssues();

    /**
     * 根据ID列表查询电气测试问题点
     * @param ids ID列表
     * @return 电气测试问题点列表
     */
    List<TestIssuesView> findTestIssuesByIds(@Param("ids") List<Long> ids);
}
