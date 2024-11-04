package com.lu.ddwyydemo04.pojo;

// 定义 User 类以存储用户 ID 和用户名
public class User {
    private String userId; // 用户 ID
    private String username; // 用户名
    private Long deptId; // 小部门 ID
    private Long majorDeptId; // 大部门 ID
    private String departmentName; // 小部门名称
    private String position; // 职位

    // 其他属性、构造函数、getter 和 setter 方法...


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getDeptId() {
        return deptId; // 返回小部门 ID
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId; // 设置小部门 ID
    }

    public Long getMajorDeptId() {
        return majorDeptId; // 返回大部门 ID
    }

    public void setMajorDeptId(Long majorDeptId) {
        this.majorDeptId = majorDeptId; // 设置大部门 ID
    }

    public String getDepartmentName() {
        return departmentName; // 返回小部门名称
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName; // 设置小部门名称
    }

    public String getPosition() {
        return position; // 返回职位
    }

    public void setPosition(String position) {
        this.position = position; // 设置职位
    }

    // 其他 getter 和 setter 方法...
}
