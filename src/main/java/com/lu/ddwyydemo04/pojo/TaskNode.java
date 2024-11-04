package com.lu.ddwyydemo04.pojo;

import java.sql.Timestamp;
import java.util.Date;

public class TaskNode {
    private int id;
    private int sample_id;
    private String node_number;
    private Long task_id;
    private String status_value;
    private String create_time; // 使用 Timestamp 来匹配数据库的时间戳类型
    private String warn_time; // 使用 Date 来表示日期
    private Boolean isOverdue; // 使用 Boolean 类型来表示是否逾期

    private String notify_once;

    private String notify_once_time;

    private String notify_second;

    private String notify_second_time;
    // Getters 和 Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSample_id() {
        return sample_id;
    }

    public void setSample_id(int sample_id) {
        this.sample_id = sample_id;
    }

    public String getNode_number() {
        return node_number;
    }

    public void setNode_number(String node_number) {
        this.node_number = node_number;
    }

    public Long getTask_id() {
        return task_id;
    }

    public void setTask_id(Long task_id) {
        this.task_id = task_id;
    }

    public String getStatus_value() {
        return status_value;
    }

    public void setStatus_value(String status_value) {
        this.status_value = status_value;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getWarn_time() {
        return warn_time;
    }

    public void setWarn_time(String warn_time) {
        this.warn_time = warn_time;
    }

    public Boolean getOverdue() {
        return isOverdue;
    }

    public void setOverdue(Boolean overdue) {
        isOverdue = overdue;
    }

    public String getNotify_once() {
        return notify_once;
    }

    public void setNotify_once(String notify_once) {
        this.notify_once = notify_once;
    }

    public String getNotify_second() {
        return notify_second;
    }

    public void setNotify_second(String notify_second) {
        this.notify_second = notify_second;
    }

    public String getNotify_once_time() {
        return notify_once_time;
    }

    public void setNotify_once_time(String notify_once_time) {
        this.notify_once_time = notify_once_time;
    }

    public String getNotify_second_time() {
        return notify_second_time;
    }

    public void setNotify_second_time(String notify_second_time) {
        this.notify_second_time = notify_second_time;
    }

    @Override
    public String toString() {
        return "TaskNode{" +
                "id=" + id +
                ", sample_id=" + sample_id +
                ", node_number='" + node_number + '\'' +
                ", task_id=" + task_id +
                ", status_value='" + status_value + '\'' +
                ", create_time='" + create_time + '\'' +
                ", warn_time='" + warn_time + '\'' +
                ", isOverdue=" + isOverdue +
                ", notify_once='" + notify_once + '\'' +
                ", notify_once_time='" + notify_once_time + '\'' +
                ", notify_second='" + notify_second + '\'' +
                ", notify_second_time='" + notify_second_time + '\'' +
                '}';
    }
}
