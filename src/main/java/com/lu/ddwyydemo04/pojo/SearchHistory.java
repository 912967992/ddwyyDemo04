package com.lu.ddwyydemo04.pojo;

import java.time.LocalDateTime;

/**
 * 搜索历史实体类
 * 用于记录用户的问题点库搜索条件
 */
public class SearchHistory {
    
    // 主键，自增字段
    private Long id;
    
    // 用户名称
    private String username;
    
    // 搜索条件（JSON格式存储）
    private String filters;
    
    // 搜索条件描述（用于显示）
    private String filterDescription;
    
    // 保存的页面
    private String pageName;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFilters() {
        return filters;
    }
    
    public void setFilters(String filters) {
        this.filters = filters;
    }
    
    public String getFilterDescription() {
        return filterDescription;
    }
    
    public void setFilterDescription(String filterDescription) {
        this.filterDescription = filterDescription;
    }
    
    public String getPageName() {
        return pageName;
    }
    
    public void setPageName(String pageName) {
        this.pageName = pageName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

