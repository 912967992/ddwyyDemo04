package com.lu.ddwyydemo04.pojo;

import java.time.LocalDateTime;

/**
 * 用户访问记录实体类
 */
public class UserAccessLog {
    
    private Long id;                    // 主键ID
    private String username;             // 用户名
    private String job;                  // 职位/角色
    private LocalDateTime accessTime;    // 访问时间
    private String userAgent;            // 操作设备信息
    private String ipAddress;            // IP地址
    private String accessPage;           // 访问页面
    
    // 构造函数
    public UserAccessLog() {
    }
    
    // Getter和Setter方法
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
    
    public String getJob() {
        return job;
    }
    
    public void setJob(String job) {
        this.job = job;
    }
    
    public LocalDateTime getAccessTime() {
        return accessTime;
    }
    
    public void setAccessTime(LocalDateTime accessTime) {
        this.accessTime = accessTime;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getAccessPage() {
        return accessPage;
    }
    
    public void setAccessPage(String accessPage) {
        this.accessPage = accessPage;
    }
}
