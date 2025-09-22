package com.lu.ddwyydemo04.dao;

import com.lu.ddwyydemo04.pojo.NewProductProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 新品进度管理数据访问层
 */
@Mapper
public interface NewProductProgressDao {

    /**
     * 插入新品进度数据
     * @param newProductProgress 新品进度对象
     * @return 影响行数
     */
    int insert(NewProductProgress newProductProgress);

    /**
     * 根据SKU查询是否存在记录
     * @param sku SKU
     * @return 存在的记录ID，如果不存在返回null
     */
    Long findBySku(@Param("sku") String sku);

    /**
     * 更新新品进度数据
     * @param newProductProgress 新品进度对象
     * @return 影响行数
     */
    int update(NewProductProgress newProductProgress);

    /**
     * 根据ID查询新品进度
     * @param id 新品进度ID
     * @return 新品进度对象
     */
    NewProductProgress findById(@Param("id") Long id);

    /**
     * 查询所有新品进度数据
     * @return 新品进度列表
     */
    List<NewProductProgress> findAll();

    /**
     * 根据ID列表批量删除新品进度
     * @param ids ID列表
     * @return 删除数量
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 根据ID删除新品进度（软删除）
     * @param id 新品进度ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据条件查询新品进度数量
     * @param productCategory 产品三级类目
     * @param priority 优先级
     * @param model 型号
     * @param sku SKU
     * @param stage 阶段
     * @param productName 产品名称
     * @param leadDqe 主导DQE
     * @param electronicRd 电子研发
     * @param status 状态
     * @return 记录数量
     */
    int countByConditions(@Param("productCategory") String productCategory,
                         @Param("priority") String priority,
                         @Param("model") String model,
                         @Param("sku") String sku,
                         @Param("stage") String stage,
                         @Param("productName") String productName,
                         @Param("leadDqe") String leadDqe,
                         @Param("electronicRd") String electronicRd,
                         @Param("status") String status);
}
