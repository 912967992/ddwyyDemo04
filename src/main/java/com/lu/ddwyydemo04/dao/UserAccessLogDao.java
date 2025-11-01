package com.lu.ddwyydemo04.dao;

import com.lu.ddwyydemo04.pojo.UserAccessLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserAccessLogDao {
    
    /**
     * 插入用户访问记录
     */
    void insertUserAccessLog(UserAccessLog userAccessLog);
    
    /**
     * 根据用户名查询访问系统的次数（访问系统的值）
     */
    Integer getAccessSystemCountByUsername(@Param("username") String username);
    
    /**
     * 根据用户名查询最近的访问记录
     */
    List<UserAccessLog> getRecentAccessLogsByUsername(@Param("username") String username, @Param("limit") Integer limit);
    
    /**
     * 查询所有用户的访问统计（访问系统的值）
     */
    List<UserAccessLog> getAllUserAccessStatistics();
}
