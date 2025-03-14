package com.lu.ddwyydemo04.pojo;

import java.time.LocalDateTime;

// 这个PassbackData实体类，是eletric_info的封装对象类
public class PassbackData {

    private int id;
    private String sample_id;
    private String sample_category;
    private String sample_model;
    private String sample_coding;
    private String materialCode;
    private String sample_frequency;
    private String sample_name;
    private String version;
    private String priority;
    private String sample_leader;
    private String supplier;
    private String testProjectCategory;
    private String testProjects;
    private String schedule;
    private LocalDateTime create_time;

    private Integer scheduleDays;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSample_id() {
        return sample_id;
    }

    public void setSample_id(String sample_id) {
        this.sample_id = sample_id;
    }

    public String getSample_category() {
        return sample_category;
    }

    public void setSample_category(String sample_category) {
        this.sample_category = sample_category;
    }

    public String getSample_model() {
        return sample_model;
    }

    public void setSample_model(String sample_model) {
        this.sample_model = sample_model;
    }

    public String getSample_coding() {
        return sample_coding;
    }

    public void setSample_coding(String sample_coding) {
        this.sample_coding = sample_coding;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getSample_frequency() {
        return sample_frequency;
    }

    public void setSample_frequency(String sample_frequency) {
        this.sample_frequency = sample_frequency;
    }

    public String getSample_name() {
        return sample_name;
    }

    public void setSample_name(String sample_name) {
        this.sample_name = sample_name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSample_leader() {
        return sample_leader;
    }

    public void setSample_leader(String sample_leader) {
        this.sample_leader = sample_leader;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getTestProjectCategory() {
        return testProjectCategory;
    }

    public void setTestProjectCategory(String testProjectCategory) {
        this.testProjectCategory = testProjectCategory;
    }

    public String getTestProjects() {
        return testProjects;
    }

    public void setTestProjects(String testProjects) {
        this.testProjects = testProjects;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public LocalDateTime getCreate_time() {
        return create_time;
    }

    public void setCreate_time(LocalDateTime create_time) {
        this.create_time = create_time;
    }

    public Integer getScheduleDays() {
        return scheduleDays;
    }

    public void setScheduleDays(Integer scheduleDays) {
        this.scheduleDays = scheduleDays;
    }

    @Override
    public String toString() {
        return "PassbackData{" +
                "id=" + id +
                ", sample_id='" + sample_id + '\'' +
                ", sample_category='" + sample_category + '\'' +
                ", sample_model='" + sample_model + '\'' +
                ", sample_coding='" + sample_coding + '\'' +
                ", materialCode='" + materialCode + '\'' +
                ", sample_frequency='" + sample_frequency + '\'' +
                ", sample_name='" + sample_name + '\'' +
                ", version='" + version + '\'' +
                ", priority='" + priority + '\'' +
                ", sample_leader='" + sample_leader + '\'' +
                ", supplier='" + supplier + '\'' +
                ", testProjectCategory='" + testProjectCategory + '\'' +
                ", testProjects='" + testProjects + '\'' +
                ", schedule='" + schedule + '\'' +
                ", create_time=" + create_time +
                ", scheduleDays=" + scheduleDays +
                '}';
    }
}
