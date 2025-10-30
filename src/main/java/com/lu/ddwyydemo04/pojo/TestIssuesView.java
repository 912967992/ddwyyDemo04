package com.lu.ddwyydemo04.pojo;

import java.time.LocalDateTime;

/**
 * 电气测试问题点视图
 * 用于展示test_issues和samples表的联查结果
 */
public class TestIssuesView {
    
    private Long id;
    private String group;           // 组别 - 从users表的departmentName获取（通过sample_DQE匹配）
    private String category;         // 品类 - s.small_species
    private String dqeResponsible;   // DQE负责人 - s.sample_DQE
    private String problemTime;  // 发生日期 - t.problem_time
    private String sampleModel;  // 大编码 - s.sample_model
    private String sampleCoding; // 小编码 - s.sample_coding
    private String sampleStage;  // 项目阶段 - t.sample_stage
    private String version;      // 版本 - t.version
    private String problemProcess; // 问题工序 - 默认"电气测试报告"
    private String defectLevel;  // 问题等级 - t.defect_level
    private String developmentMethod; // 开发方式 - 为空
    private String supplier;    // 供应商 - t.supplier
    private String solutionProvider; // 方案商 - t.solution_provider
    private String problem;      // 问题点 - t.problem
    private String greenUnionDqe; // 问题原因 - t.green_union_dqe
    private String greenUnionRd; // 改善对策 - t.green_union_rd
    private String isPreventable; // 是否可预防 - 为空
    private String responsibleDepartment; // 责任部门 - t.responsibleDepartment
    private String currentStatus; // 问题状态 - t.current_status
    private String plannedCompletionTime; // 预计完成时间 - 为空
    private String actualCompletionTime; // 实际完成时间 - 为空
    private String delayDays;   // Delay天数 - 为空
    private String problemTag1; // 问题打标1 - 为空
    private String problemTag2; // 问题打标2 - 为空
    private String remark;      // 预防备注 - t.remark
    private LocalDateTime createdAt; // 创建时间 - t.created_at
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getDqeResponsible() {
        return dqeResponsible;
    }
    
    public void setDqeResponsible(String dqeResponsible) {
        this.dqeResponsible = dqeResponsible;
    }
    
    public String getProblemTime() {
        return problemTime;
    }
    
    public void setProblemTime(String problemTime) {
        this.problemTime = problemTime;
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
    
    public String getSampleStage() {
        return sampleStage;
    }
    
    public void setSampleStage(String sampleStage) {
        this.sampleStage = sampleStage;
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
    
    public String getDefectLevel() {
        return defectLevel;
    }
    
    public void setDefectLevel(String defectLevel) {
        this.defectLevel = defectLevel;
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
    
    public String getProblem() {
        return problem;
    }
    
    public void setProblem(String problem) {
        this.problem = problem;
    }
    
    public String getGreenUnionDqe() {
        return greenUnionDqe;
    }
    
    public void setGreenUnionDqe(String greenUnionDqe) {
        this.greenUnionDqe = greenUnionDqe;
    }
    
    public String getGreenUnionRd() {
        return greenUnionRd;
    }
    
    public void setGreenUnionRd(String greenUnionRd) {
        this.greenUnionRd = greenUnionRd;
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
    
    public String getCurrentStatus() {
        return currentStatus;
    }
    
    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }
    
    public String getPlannedCompletionTime() {
        return plannedCompletionTime;
    }
    
    public void setPlannedCompletionTime(String plannedCompletionTime) {
        this.plannedCompletionTime = plannedCompletionTime;
    }
    
    public String getActualCompletionTime() {
        return actualCompletionTime;
    }
    
    public void setActualCompletionTime(String actualCompletionTime) {
        this.actualCompletionTime = actualCompletionTime;
    }
    
    public String getDelayDays() {
        return delayDays;
    }
    
    public void setDelayDays(String delayDays) {
        this.delayDays = delayDays;
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
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

