package com.lu.ddwyydemo04.pojo;

import java.time.LocalDateTime;

public class PassbackData {
    private String sampleId;
    private String sampleCategory;
    private String sampleModel;
    private String sampleCoding;
    private String materialCode;
    private String sampleFrequency;
    private String sampleName;
    private String version;
    private String priority;
    private String sampleLeader;
    private String supplier;
    private String testProjectCategory;
    private String testProjects;
    private String schedule;

    private LocalDateTime createTime; // 新增字段

    // Getters and Setters
    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getSampleCategory() {
        return sampleCategory;
    }

    public void setSampleCategory(String sampleCategory) {
        this.sampleCategory = sampleCategory;
    }

    public String getSampleModel() {
        return sampleModel;
    }

    public void setSampleModel(String sampleModel) {
        this.sampleModel = sampleModel;
    }

    public String getSampleCoding() {
        return sampleCoding;
    }

    public void setSampleCoding(String sampleCoding) {
        this.sampleCoding = sampleCoding;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getSampleFrequency() {
        return sampleFrequency;
    }

    public void setSampleFrequency(String sampleFrequency) {
        this.sampleFrequency = sampleFrequency;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
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

    public String getSampleLeader() {
        return sampleLeader;
    }

    public void setSampleLeader(String sampleLeader) {
        this.sampleLeader = sampleLeader;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
