package com.lu.ddwyydemo04.dao;

import com.lu.ddwyydemo04.pojo.ProjectTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目总表数据访问层
 */
@Mapper
public interface ProjectTableMapper {

    /**
     * 查询所有项目表格数据
     * @return 项目表格数据列表
     */
    List<ProjectTable> selectAll();

    /**
     * 根据ID查询项目表格数据
     * @param id 主键ID
     * @return 项目表格数据
     */
    ProjectTable selectById(@Param("id") Long id);

    /**
     * 插入项目表格数据
     * @param projectTable 项目表格数据
     * @return 影响行数
     */
    int insert(ProjectTable projectTable);

    /**
     * 更新项目表格数据
     * @param projectTable 项目表格数据
     * @return 影响行数
     */
    int update(ProjectTable projectTable);

    /**
     * 根据ID删除项目表格数据
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 分页查询项目表格数据
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 项目表格数据列表
     */
    List<ProjectTable> selectByPage(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询项目表格数据总数
     * @return 总数
     */
    int countAll();

    /**
     * 根据条件查询项目表格数据
     * @param supplier 供应商
     * @param sampleModel 样品型号
     * @param sampleName 样品名称
     * @param categoryDetail 品类细分
     * @param tester 测试人
     * @param dqe DQE
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 项目表格数据列表
     */
    List<ProjectTable> selectByCondition(
            @Param("supplier") String supplier,
            @Param("sampleModel") String sampleModel,
            @Param("sampleName") String sampleName,
            @Param("categoryDetail") String categoryDetail,
            @Param("tester") String tester,
            @Param("dqe") String dqe,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 根据条件查询项目表格数据总数
     * @param supplier 供应商
     * @param sampleModel 样品型号
     * @param sampleName 样品名称
     * @param categoryDetail 品类细分
     * @param tester 测试人
     * @param dqe DQE
     * @return 总数
     */
    int countByCondition(
            @Param("supplier") String supplier,
            @Param("sampleModel") String sampleModel,
            @Param("sampleName") String sampleName,
            @Param("categoryDetail") String categoryDetail,
            @Param("tester") String tester,
            @Param("dqe") String dqe
    );
}

