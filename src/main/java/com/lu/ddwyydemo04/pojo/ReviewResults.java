package com.lu.ddwyydemo04.pojo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 评审结果实体类
 * 对应数据库表 reviewResults
 */
public class ReviewResults {
    
    private Long id;
    private LocalDate testDate;
    private String majorCode;
    private String minorCode;
    private String projectPhase;
    private String version;
    private String problemProcess;
    private String problemLevel;
    private String developmentMethod;
    private String supplier;
    private String solutionProvider;
    private String problemPoint;
    private String problemReason;
    private String improvementMeasures;
    private String isPreventable;
    private String responsibleDepartment;
    private LocalDateTime plannedCompletionTime;
    private LocalDateTime actualCompletionTime;
    private Integer delayDays;
    private String problemStatus;
    private String problemTag1;
    private String problemTag2;
    private String preventionNotes;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    // 无参构造函数
    public ReviewResults() {}

    // 全参构造函数
    public ReviewResults(Long id, LocalDate testDate, String majorCode, String minorCode, 
                        String projectPhase, String version, String problemProcess, 
                        String problemLevel, String developmentMethod, String supplier, 
                        String solutionProvider, String problemPoint, String problemReason, 
                        String improvementMeasures, String isPreventable, String responsibleDepartment, 
                        LocalDateTime plannedCompletionTime, LocalDateTime actualCompletionTime, 
                        Integer delayDays, String problemStatus, String problemTag1, 
                        String problemTag2, String preventionNotes, LocalDateTime createdTime, 
                        LocalDateTime updatedTime) {
        this.id = id;
        this.testDate = testDate;
        this.majorCode = majorCode;
        this.minorCode = minorCode;
        this.projectPhase = projectPhase;
        this.version = version;
        this.problemProcess = problemProcess;
        this.problemLevel = problemLevel;
        this.developmentMethod = developmentMethod;
        this.supplier = supplier;
        this.solutionProvider = solutionProvider;
        this.problemPoint = problemPoint;
        this.problemReason = problemReason;
        this.improvementMeasures = improvementMeasures;
        this.isPreventable = isPreventable;
        this.responsibleDepartment = responsibleDepartment;
        this.plannedCompletionTime = plannedCompletionTime;
        this.actualCompletionTime = actualCompletionTime;
        this.delayDays = delayDays;
        this.problemStatus = problemStatus;
        this.problemTag1 = problemTag1;
        this.problemTag2 = problemTag2;
        this.preventionNotes = preventionNotes;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getTestDate() {
        return testDate;
    }

    public void setTestDate(LocalDate testDate) {
        this.testDate = testDate;
    }

    public String getMajorCode() {
        return majorCode;
    }

    public void setMajorCode(String majorCode) {
        this.majorCode = majorCode;
    }

    public String getMinorCode() {
        return minorCode;
    }

    public void setMinorCode(String minorCode) {
        this.minorCode = minorCode;
    }

    public String getProjectPhase() {
        return projectPhase;
    }

    public void setProjectPhase(String projectPhase) {
        this.projectPhase = projectPhase;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProblemProcess() {
        return problemProcess;
    }

    public void setProblemProcess(String problemProcess) {
        this.problemProcess = problemProcess;
    }

    public String getProblemLevel() {
        return problemLevel;
    }

    public void setProblemLevel(String problemLevel) {
        this.problemLevel = problemLevel;
    }

    public String getDevelopmentMethod() {
        return developmentMethod;
    }

    public void setDevelopmentMethod(String developmentMethod) {
        this.developmentMethod = developmentMethod;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getSolutionProvider() {
        return solutionProvider;
    }

    public void setSolutionProvider(String solutionProvider) {
        this.solutionProvider = solutionProvider;
    }

    public String getProblemPoint() {
        return problemPoint;
    }

    public void setProblemPoint(String problemPoint) {
        this.problemPoint = problemPoint;
    }

    public String getProblemReason() {
        return problemReason;
    }

    public void setProblemReason(String problemReason) {
        this.problemReason = problemReason;
    }

    public String getImprovementMeasures() {
        return improvementMeasures;
    }

    public void setImprovementMeasures(String improvementMeasures) {
        this.improvementMeasures = improvementMeasures;
    }

    public String getIsPreventable() {
        return isPreventable;
    }

    public void setIsPreventable(String isPreventable) {
        this.isPreventable = isPreventable;
    }

    public String getResponsibleDepartment() {
        return responsibleDepartment;
    }

    public void setResponsibleDepartment(String responsibleDepartment) {
        this.responsibleDepartment = responsibleDepartment;
    }

    public LocalDateTime getPlannedCompletionTime() {
        return plannedCompletionTime;
    }

    public void setPlannedCompletionTime(LocalDateTime plannedCompletionTime) {
        this.plannedCompletionTime = plannedCompletionTime;
    }

    public LocalDateTime getActualCompletionTime() {
        return actualCompletionTime;
    }

    public void setActualCompletionTime(LocalDateTime actualCompletionTime) {
        this.actualCompletionTime = actualCompletionTime;
    }

    public Integer getDelayDays() {
        return delayDays;
    }

    public void setDelayDays(Integer delayDays) {
        this.delayDays = delayDays;
    }

    public String getProblemStatus() {
        return problemStatus;
    }

    public void setProblemStatus(String problemStatus) {
        this.problemStatus = problemStatus;
    }

    public String getProblemTag1() {
        return problemTag1;
    }

    public void setProblemTag1(String problemTag1) {
        this.problemTag1 = problemTag1;
    }

    public String getProblemTag2() {
        return problemTag2;
    }

    public void setProblemTag2(String problemTag2) {
        this.problemTag2 = problemTag2;
    }

    public String getPreventionNotes() {
        return preventionNotes;
    }

    public void setPreventionNotes(String preventionNotes) {
        this.preventionNotes = preventionNotes;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public String toString() {
        return "ReviewResults{" +
                "id=" + id +
                ", testDate=" + testDate +
                ", majorCode='" + majorCode + '\'' +
                ", minorCode='" + minorCode + '\'' +
                ", projectPhase='" + projectPhase + '\'' +
                ", version='" + version + '\'' +
                ", problemProcess='" + problemProcess + '\'' +
                ", problemLevel='" + problemLevel + '\'' +
                ", developmentMethod='" + developmentMethod + '\'' +
                ", supplier='" + supplier + '\'' +
                ", solutionProvider='" + solutionProvider + '\'' +
                ", problemPoint='" + problemPoint + '\'' +
                ", problemReason='" + problemReason + '\'' +
                ", improvementMeasures='" + improvementMeasures + '\'' +
                ", isPreventable='" + isPreventable + '\'' +
                ", responsibleDepartment='" + responsibleDepartment + '\'' +
                ", plannedCompletionTime=" + plannedCompletionTime +
                ", actualCompletionTime=" + actualCompletionTime +
                ", delayDays=" + delayDays +
                ", problemStatus='" + problemStatus + '\'' +
                ", problemTag1='" + problemTag1 + '\'' +
                ", problemTag2='" + problemTag2 + '\'' +
                ", preventionNotes='" + preventionNotes + '\'' +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }
}

