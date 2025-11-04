package com.lu.ddwyydemo04.dao;

import com.lu.ddwyydemo04.pojo.SearchHistory;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchHistoryDao {
    
    /**
     * 保存搜索历史
     * @param searchHistory 搜索历史对象
     * @return 影响行数
     */
    int saveSearchHistory(SearchHistory searchHistory);
    
    /**
     * 根据用户名查询搜索历史（按时间倒序）
     * @param username 用户名
     * @param limit 查询数量限制（可选，用于限制返回的条数）
     * @return 搜索历史列表
     */
    List<SearchHistory> getSearchHistoryByUser(@Param("username") String username, @Param("limit") Integer limit);
    
    /**
     * 根据ID查询搜索历史
     * @param id 搜索历史ID
     * @return 搜索历史对象
     */
    SearchHistory getSearchHistoryById(@Param("id") Long id);
    
    /**
     * 根据ID删除搜索历史
     * @param id 搜索历史ID
     * @return 影响行数
     */
    int deleteSearchHistory(@Param("id") Long id);
    
    /**
     * 删除用户的所有搜索历史
     * @param username 用户名
     * @return 影响行数
     */
    int deleteAllByUser(@Param("username") String username);
}

