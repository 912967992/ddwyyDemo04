package com.lu.ddwyydemo04.pojo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 项目总表实体类
 */
public class ProjectTable {
    
    private Long id;
    private LocalDateTime sampleReceiptTime;
    private String sampleWeek;
    private String supplier;
    private Integer sampleCount;
    private String sampleModel;
    private String smallCode;
    private String sampleName;
    private String categoryDetail;
    private String versionNumber;
    private String testNumber;
    private BigDecimal requiredDuration;
    private Integer totalQuantity;
    private Integer electricalCompatibilityQuantity;
    private Integer highFrequencyQuantity;
    private Integer conductionRfQuantity;
    private Integer reliabilityQuantity;
    private Integer environmentalQuantity;
    private String sampleCategory;
    private String sampleNature;
    private String projectAttribute;
    private LocalDateTime electricalTestPlanStart;
    private LocalDateTime electricalTestPlanEnd;
    private LocalDateTime electricalTestActualStart;
    private LocalDateTime electricalTestActualEnd;
    private String actualCompletionWeek;
    private LocalDateTime engineerDqeConfirmStart;
    private LocalDateTime engineerDqeConfirmEnd;
    private BigDecimal testDurationDays;
    private BigDecimal actualStartVsReceiptDays;
    private BigDecimal actualStartVsPlanStartDays;
    private BigDecimal planTotalDurationDays;
    private BigDecimal actualTotalDurationDays;
    private BigDecimal actualOvertimeDays;
    private BigDecimal problemConfirmTotalDurationDays;
    private BigDecimal problemConfirmOvertimeDays;
    private Integer verificationCount;
    private String tester;
    private String electronicEngineer;
    private String projectEngineer;
    private String dqe;
    private String dqeCategoryDetail;
    private String newCategory;
    private String urgentSync;
    private String isInPlan;
    private BigDecimal sampleCycle;
    private BigDecimal testCycle;
    private BigDecimal queueCycle;
    private String judgmentResult;
    private String isSigned;
    private String domesticForeign;
    private String supplierPreposition;
    private String sampleRemarks;
    private String problemDefinition;
    private Integer sClassProblemCount;
    private Integer aClassProblemCount;
    private Integer bClassProblemCount;
    private Integer cClassProblemCount;
    private Integer dClassProblemCount;
    private Integer totalProblemCount;
    private Integer openCount;
    private Integer closeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 构造函数
    public ProjectTable() {}

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public LocalDateTime getSampleReceiptTime() {
        return sampleReceiptTime;
    }

    public void setSampleReceiptTime(LocalDateTime sampleReceiptTime) {
        this.sampleReceiptTime = sampleReceiptTime;
    }

    public String getSampleWeek() {
        return sampleWeek;
    }

    public void setSampleWeek(String sampleWeek) {
        this.sampleWeek = sampleWeek;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }

    public String getSampleModel() {
        return sampleModel;
    }

    public void setSampleModel(String sampleModel) {
        this.sampleModel = sampleModel;
    }

    public String getSmallCode() {
        return smallCode;
    }

    public void setSmallCode(String smallCode) {
        this.smallCode = smallCode;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public String getCategoryDetail() {
        return categoryDetail;
    }

    public void setCategoryDetail(String categoryDetail) {
        this.categoryDetail = categoryDetail;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getTestNumber() {
        return testNumber;
    }

    public void setTestNumber(String testNumber) {
        this.testNumber = testNumber;
    }

    public BigDecimal getRequiredDuration() {
        return requiredDuration;
    }

    public void setRequiredDuration(BigDecimal requiredDuration) {
        this.requiredDuration = requiredDuration;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getElectricalCompatibilityQuantity() {
        return electricalCompatibilityQuantity;
    }

    public void setElectricalCompatibilityQuantity(Integer electricalCompatibilityQuantity) {
        this.electricalCompatibilityQuantity = electricalCompatibilityQuantity;
    }

    public Integer getHighFrequencyQuantity() {
        return highFrequencyQuantity;
    }

    public void setHighFrequencyQuantity(Integer highFrequencyQuantity) {
        this.highFrequencyQuantity = highFrequencyQuantity;
    }

    public Integer getConductionRfQuantity() {
        return conductionRfQuantity;
    }

    public void setConductionRfQuantity(Integer conductionRfQuantity) {
        this.conductionRfQuantity = conductionRfQuantity;
    }

    public Integer getReliabilityQuantity() {
        return reliabilityQuantity;
    }

    public void setReliabilityQuantity(Integer reliabilityQuantity) {
        this.reliabilityQuantity = reliabilityQuantity;
    }

    public Integer getEnvironmentalQuantity() {
        return environmentalQuantity;
    }

    public void setEnvironmentalQuantity(Integer environmentalQuantity) {
        this.environmentalQuantity = environmentalQuantity;
    }

    public String getSampleCategory() {
        return sampleCategory;
    }

    public void setSampleCategory(String sampleCategory) {
        this.sampleCategory = sampleCategory;
    }

    public String getSampleNature() {
        return sampleNature;
    }

    public void setSampleNature(String sampleNature) {
        this.sampleNature = sampleNature;
    }

    public String getProjectAttribute() {
        return projectAttribute;
    }

    public void setProjectAttribute(String projectAttribute) {
        this.projectAttribute = projectAttribute;
    }

    public LocalDateTime getElectricalTestPlanStart() {
        return electricalTestPlanStart;
    }

    public void setElectricalTestPlanStart(LocalDateTime electricalTestPlanStart) {
        this.electricalTestPlanStart = electricalTestPlanStart;
    }

    public LocalDateTime getElectricalTestPlanEnd() {
        return electricalTestPlanEnd;
    }

    public void setElectricalTestPlanEnd(LocalDateTime electricalTestPlanEnd) {
        this.electricalTestPlanEnd = electricalTestPlanEnd;
    }

    public LocalDateTime getElectricalTestActualStart() {
        return electricalTestActualStart;
    }

    public void setElectricalTestActualStart(LocalDateTime electricalTestActualStart) {
        this.electricalTestActualStart = electricalTestActualStart;
    }

    public LocalDateTime getElectricalTestActualEnd() {
        return electricalTestActualEnd;
    }

    public void setElectricalTestActualEnd(LocalDateTime electricalTestActualEnd) {
        this.electricalTestActualEnd = electricalTestActualEnd;
    }

    public String getActualCompletionWeek() {
        return actualCompletionWeek;
    }

    public void setActualCompletionWeek(String actualCompletionWeek) {
        this.actualCompletionWeek = actualCompletionWeek;
    }

    public LocalDateTime getEngineerDqeConfirmStart() {
        return engineerDqeConfirmStart;
    }

    public void setEngineerDqeConfirmStart(LocalDateTime engineerDqeConfirmStart) {
        this.engineerDqeConfirmStart = engineerDqeConfirmStart;
    }

    public LocalDateTime getEngineerDqeConfirmEnd() {
        return engineerDqeConfirmEnd;
    }

    public void setEngineerDqeConfirmEnd(LocalDateTime engineerDqeConfirmEnd) {
        this.engineerDqeConfirmEnd = engineerDqeConfirmEnd;
    }

    public BigDecimal getTestDurationDays() {
        return testDurationDays;
    }

    public void setTestDurationDays(BigDecimal testDurationDays) {
        this.testDurationDays = testDurationDays;
    }

    public BigDecimal getActualStartVsReceiptDays() {
        return actualStartVsReceiptDays;
    }

    public void setActualStartVsReceiptDays(BigDecimal actualStartVsReceiptDays) {
        this.actualStartVsReceiptDays = actualStartVsReceiptDays;
    }

    public BigDecimal getActualStartVsPlanStartDays() {
        return actualStartVsPlanStartDays;
    }

    public void setActualStartVsPlanStartDays(BigDecimal actualStartVsPlanStartDays) {
        this.actualStartVsPlanStartDays = actualStartVsPlanStartDays;
    }

    public BigDecimal getPlanTotalDurationDays() {
        return planTotalDurationDays;
    }

    public void setPlanTotalDurationDays(BigDecimal planTotalDurationDays) {
        this.planTotalDurationDays = planTotalDurationDays;
    }

    public BigDecimal getActualTotalDurationDays() {
        return actualTotalDurationDays;
    }

    public void setActualTotalDurationDays(BigDecimal actualTotalDurationDays) {
        this.actualTotalDurationDays = actualTotalDurationDays;
    }

    public BigDecimal getActualOvertimeDays() {
        return actualOvertimeDays;
    }

    public void setActualOvertimeDays(BigDecimal actualOvertimeDays) {
        this.actualOvertimeDays = actualOvertimeDays;
    }

    public BigDecimal getProblemConfirmTotalDurationDays() {
        return problemConfirmTotalDurationDays;
    }

    public void setProblemConfirmTotalDurationDays(BigDecimal problemConfirmTotalDurationDays) {
        this.problemConfirmTotalDurationDays = problemConfirmTotalDurationDays;
    }

    public BigDecimal getProblemConfirmOvertimeDays() {
        return problemConfirmOvertimeDays;
    }

    public void setProblemConfirmOvertimeDays(BigDecimal problemConfirmOvertimeDays) {
        this.problemConfirmOvertimeDays = problemConfirmOvertimeDays;
    }

    public Integer getVerificationCount() {
        return verificationCount;
    }

    public void setVerificationCount(Integer verificationCount) {
        this.verificationCount = verificationCount;
    }

    public String getTester() {
        return tester;
    }

    public void setTester(String tester) {
        this.tester = tester;
    }

    public String getElectronicEngineer() {
        return electronicEngineer;
    }

    public void setElectronicEngineer(String electronicEngineer) {
        this.electronicEngineer = electronicEngineer;
    }

    public String getProjectEngineer() {
        return projectEngineer;
    }

    public void setProjectEngineer(String projectEngineer) {
        this.projectEngineer = projectEngineer;
    }

    public String getDqe() {
        return dqe;
    }

    public void setDqe(String dqe) {
        this.dqe = dqe;
    }

    public String getDqeCategoryDetail() {
        return dqeCategoryDetail;
    }

    public void setDqeCategoryDetail(String dqeCategoryDetail) {
        this.dqeCategoryDetail = dqeCategoryDetail;
    }

    public String getNewCategory() {
        return newCategory;
    }

    public void setNewCategory(String newCategory) {
        this.newCategory = newCategory;
    }

    public String getUrgentSync() {
        return urgentSync;
    }

    public void setUrgentSync(String urgentSync) {
        this.urgentSync = urgentSync;
    }

    public String getIsInPlan() {
        return isInPlan;
    }

    public void setIsInPlan(String isInPlan) {
        this.isInPlan = isInPlan;
    }

    public BigDecimal getSampleCycle() {
        return sampleCycle;
    }

    public void setSampleCycle(BigDecimal sampleCycle) {
        this.sampleCycle = sampleCycle;
    }

    public BigDecimal getTestCycle() {
        return testCycle;
    }

    public void setTestCycle(BigDecimal testCycle) {
        this.testCycle = testCycle;
    }

    public BigDecimal getQueueCycle() {
        return queueCycle;
    }

    public void setQueueCycle(BigDecimal queueCycle) {
        this.queueCycle = queueCycle;
    }

    public String getJudgmentResult() {
        return judgmentResult;
    }

    public void setJudgmentResult(String judgmentResult) {
        this.judgmentResult = judgmentResult;
    }

    public String getIsSigned() {
        return isSigned;
    }

    public void setIsSigned(String isSigned) {
        this.isSigned = isSigned;
    }

    public String getDomesticForeign() {
        return domesticForeign;
    }

    public void setDomesticForeign(String domesticForeign) {
        this.domesticForeign = domesticForeign;
    }

    public String getSupplierPreposition() {
        return supplierPreposition;
    }

    public void setSupplierPreposition(String supplierPreposition) {
        this.supplierPreposition = supplierPreposition;
    }

    public String getSampleRemarks() {
        return sampleRemarks;
    }

    public void setSampleRemarks(String sampleRemarks) {
        this.sampleRemarks = sampleRemarks;
    }

    public String getProblemDefinition() {
        return problemDefinition;
    }

    public void setProblemDefinition(String problemDefinition) {
        this.problemDefinition = problemDefinition;
    }

    public Integer getsClassProblemCount() {
        return sClassProblemCount;
    }

    public void setsClassProblemCount(Integer sClassProblemCount) {
        this.sClassProblemCount = sClassProblemCount;
    }

    public Integer getaClassProblemCount() {
        return aClassProblemCount;
    }

    public void setaClassProblemCount(Integer aClassProblemCount) {
        this.aClassProblemCount = aClassProblemCount;
    }

    public Integer getbClassProblemCount() {
        return bClassProblemCount;
    }

    public void setbClassProblemCount(Integer bClassProblemCount) {
        this.bClassProblemCount = bClassProblemCount;
    }

    public Integer getcClassProblemCount() {
        return cClassProblemCount;
    }

    public void setcClassProblemCount(Integer cClassProblemCount) {
        this.cClassProblemCount = cClassProblemCount;
    }

    public Integer getdClassProblemCount() {
        return dClassProblemCount;
    }

    public void setdClassProblemCount(Integer dClassProblemCount) {
        this.dClassProblemCount = dClassProblemCount;
    }

    public Integer getTotalProblemCount() {
        return totalProblemCount;
    }

    public void setTotalProblemCount(Integer totalProblemCount) {
        this.totalProblemCount = totalProblemCount;
    }

    public Integer getOpenCount() {
        return openCount;
    }

    public void setOpenCount(Integer openCount) {
        this.openCount = openCount;
    }

    public Integer getCloseCount() {
        return closeCount;
    }

    public void setCloseCount(Integer closeCount) {
        this.closeCount = closeCount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "ProjectTable{" +
                "id=" + id +
                ", sampleReceiptTime=" + sampleReceiptTime +
                ", sampleWeek='" + sampleWeek + '\'' +
                ", supplier='" + supplier + '\'' +
                ", sampleCount=" + sampleCount +
                ", sampleModel='" + sampleModel + '\'' +
                ", smallCode='" + smallCode + '\'' +
                ", sampleName='" + sampleName + '\'' +
                ", categoryDetail='" + categoryDetail + '\'' +
                ", versionNumber='" + versionNumber + '\'' +
                ", testNumber='" + testNumber + '\'' +
                ", requiredDuration=" + requiredDuration +
                ", totalQuantity=" + totalQuantity +
                ", electricalCompatibilityQuantity=" + electricalCompatibilityQuantity +
                ", highFrequencyQuantity=" + highFrequencyQuantity +
                ", conductionRfQuantity=" + conductionRfQuantity +
                ", reliabilityQuantity=" + reliabilityQuantity +
                ", environmentalQuantity=" + environmentalQuantity +
                ", sampleCategory='" + sampleCategory + '\'' +
                ", sampleNature='" + sampleNature + '\'' +
                ", projectAttribute='" + projectAttribute + '\'' +
                ", electricalTestPlanStart=" + electricalTestPlanStart +
                ", electricalTestPlanEnd=" + electricalTestPlanEnd +
                ", electricalTestActualStart=" + electricalTestActualStart +
                ", electricalTestActualEnd=" + electricalTestActualEnd +
                ", actualCompletionWeek='" + actualCompletionWeek + '\'' +
                ", engineerDqeConfirmStart=" + engineerDqeConfirmStart +
                ", engineerDqeConfirmEnd=" + engineerDqeConfirmEnd +
                ", testDurationDays=" + testDurationDays +
                ", actualStartVsReceiptDays=" + actualStartVsReceiptDays +
                ", actualStartVsPlanStartDays=" + actualStartVsPlanStartDays +
                ", planTotalDurationDays=" + planTotalDurationDays +
                ", actualTotalDurationDays=" + actualTotalDurationDays +
                ", actualOvertimeDays=" + actualOvertimeDays +
                ", problemConfirmTotalDurationDays=" + problemConfirmTotalDurationDays +
                ", problemConfirmOvertimeDays=" + problemConfirmOvertimeDays +
                ", verificationCount=" + verificationCount +
                ", tester='" + tester + '\'' +
                ", electronicEngineer='" + electronicEngineer + '\'' +
                ", projectEngineer='" + projectEngineer + '\'' +
                ", dqe='" + dqe + '\'' +
                ", dqeCategoryDetail='" + dqeCategoryDetail + '\'' +
                ", newCategory='" + newCategory + '\'' +
                ", urgentSync='" + urgentSync + '\'' +
                ", isInPlan='" + isInPlan + '\'' +
                ", sampleCycle=" + sampleCycle +
                ", testCycle=" + testCycle +
                ", queueCycle=" + queueCycle +
                ", judgmentResult='" + judgmentResult + '\'' +
                ", isSigned='" + isSigned + '\'' +
                ", domesticForeign='" + domesticForeign + '\'' +
                ", supplierPreposition='" + supplierPreposition + '\'' +
                ", sampleRemarks='" + sampleRemarks + '\'' +
                ", problemDefinition='" + problemDefinition + '\'' +
                ", sClassProblemCount=" + sClassProblemCount +
                ", aClassProblemCount=" + aClassProblemCount +
                ", bClassProblemCount=" + bClassProblemCount +
                ", cClassProblemCount=" + cClassProblemCount +
                ", dClassProblemCount=" + dClassProblemCount +
                ", totalProblemCount=" + totalProblemCount +
                ", openCount=" + openCount +
                ", closeCount=" + closeCount +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}

