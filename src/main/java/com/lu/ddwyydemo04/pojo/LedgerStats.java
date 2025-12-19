package com.lu.ddwyydemo04.pojo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 台账统计表实体类
 * 用于存储每日的进出存统计数据
 */
public class LedgerStats {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 统计日期
     */
    private LocalDate statDate;
    
    /**
     * 进账数量（今日新增，不含CD）
     */
    private Integer inCount;
    
    /**
     * 出账数量（今日结束，不含CD）
     */
    private Integer outCount;
    
    /**
     * 存账数量（当前库存）
     */
    private Integer stockCount;
    
    /**
     * 初始存账数量（由许梦瑶或卢健设置）
     */
    private Integer initialStockCount;
    
    /**
     * 初始存账设置人
     */
    private String initialSetBy;
    
    /**
     * 初始存账设置时间
     */
    private LocalDateTime initialSetTime;
    
    /**
     * 备注信息
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // Getter and Setter methods
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getStatDate() {
        return statDate;
    }

    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }

    public Integer getInCount() {
        return inCount;
    }

    public void setInCount(Integer inCount) {
        this.inCount = inCount;
    }

    public Integer getOutCount() {
        return outCount;
    }

    public void setOutCount(Integer outCount) {
        this.outCount = outCount;
    }

    public Integer getStockCount() {
        return stockCount;
    }

    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    public Integer getInitialStockCount() {
        return initialStockCount;
    }

    public void setInitialStockCount(Integer initialStockCount) {
        this.initialStockCount = initialStockCount;
    }

    public String getInitialSetBy() {
        return initialSetBy;
    }

    public void setInitialSetBy(String initialSetBy) {
        this.initialSetBy = initialSetBy;
    }

    public LocalDateTime getInitialSetTime() {
        return initialSetTime;
    }

    public void setInitialSetTime(LocalDateTime initialSetTime) {
        this.initialSetTime = initialSetTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "LedgerStats{" +
                "id=" + id +
                ", statDate=" + statDate +
                ", inCount=" + inCount +
                ", outCount=" + outCount +
                ", stockCount=" + stockCount +
                ", initialStockCount=" + initialStockCount +
                ", initialSetBy='" + initialSetBy + '\'' +
                ", initialSetTime=" + initialSetTime +
                ", remark='" + remark + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}


