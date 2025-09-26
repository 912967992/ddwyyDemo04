package com.lu.ddwyydemo04.pojo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 新品进度管理实体类
 * 对应数据库表 new_product_progress_management
 */
public class NewProductProgress {
    
    private Long id;
    private String productCategoryLevel3;
    private String priority;
    private String model;
    private String sku;
    private String stage;
    private String productName;
    private String imageUrl;
    private String projectStartTime;
    private String targetLaunchTime;
    private String displayType;
    private String productLevel;
    private String primarySupplier;
    private String leadDqe;
    private String electronicRd;
    private String groupName;
    private String status;
    private String designReviewProblem;
    private String evtProblem;
    private String dvtProblem;
    private String mainProjectProgress;
    private String mainQualityRisks;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createBy;
    private String updateBy;
    private Integer isDeleted;

    // 无参构造函数
    public NewProductProgress() {}

    // 全参构造函数
    public NewProductProgress(Long id, String productCategoryLevel3, String priority, String model, 
                            String sku, String stage, String productName, String imageUrl, 
                            String projectStartTime, String targetLaunchTime, String displayType, 
                            String productLevel, String primarySupplier, String leadDqe, 
                            String electronicRd, String status, String designReviewProblem, 
                            String evtProblem, String dvtProblem, String mainProjectProgress, 
                            String mainQualityRisks, LocalDateTime createTime, LocalDateTime updateTime, 
                            String createBy, String updateBy, Integer isDeleted) {
        this.id = id;
        this.productCategoryLevel3 = productCategoryLevel3;
        this.priority = priority;
        this.model = model;
        this.sku = sku;
        this.stage = stage;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.projectStartTime = projectStartTime;
        this.targetLaunchTime = targetLaunchTime;
        this.displayType = displayType;
        this.productLevel = productLevel;
        this.primarySupplier = primarySupplier;
        this.leadDqe = leadDqe;
        this.electronicRd = electronicRd;
        this.status = status;
        this.designReviewProblem = designReviewProblem;
        this.evtProblem = evtProblem;
        this.dvtProblem = dvtProblem;
        this.mainProjectProgress = mainProjectProgress;
        this.mainQualityRisks = mainQualityRisks;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.createBy = createBy;
        this.updateBy = updateBy;
        this.isDeleted = isDeleted;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductCategoryLevel3() {
        return productCategoryLevel3;
    }

    public void setProductCategoryLevel3(String productCategoryLevel3) {
        this.productCategoryLevel3 = productCategoryLevel3;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getProjectStartTime() {
        return projectStartTime;
    }

    public void setProjectStartTime(String projectStartTime) {
        this.projectStartTime = projectStartTime;
    }

    public String getTargetLaunchTime() {
        return targetLaunchTime;
    }

    public void setTargetLaunchTime(String targetLaunchTime) {
        this.targetLaunchTime = targetLaunchTime;
    }

    public String getDisplayType() {
        return displayType;
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public String getProductLevel() {
        return productLevel;
    }

    public void setProductLevel(String productLevel) {
        this.productLevel = productLevel;
    }

    public String getPrimarySupplier() {
        return primarySupplier;
    }

    public void setPrimarySupplier(String primarySupplier) {
        this.primarySupplier = primarySupplier;
    }

    public String getLeadDqe() {
        return leadDqe;
    }

    public void setLeadDqe(String leadDqe) {
        this.leadDqe = leadDqe;
    }

    public String getElectronicRd() {
        return electronicRd;
    }

    public void setElectronicRd(String electronicRd) {
        this.electronicRd = electronicRd;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDesignReviewProblem() {
        return designReviewProblem;
    }

    public void setDesignReviewProblem(String designReviewProblem) {
        this.designReviewProblem = designReviewProblem;
    }

    public String getEvtProblem() {
        return evtProblem;
    }

    public void setEvtProblem(String evtProblem) {
        this.evtProblem = evtProblem;
    }

    public String getDvtProblem() {
        return dvtProblem;
    }

    public void setDvtProblem(String dvtProblem) {
        this.dvtProblem = dvtProblem;
    }

    public String getMainProjectProgress() {
        return mainProjectProgress;
    }

    public void setMainProjectProgress(String mainProjectProgress) {
        this.mainProjectProgress = mainProjectProgress;
    }

    public String getMainQualityRisks() {
        return mainQualityRisks;
    }

    public void setMainQualityRisks(String mainQualityRisks) {
        this.mainQualityRisks = mainQualityRisks;
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

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return "NewProductProgress{" +
                "id=" + id +
                ", productCategoryLevel3='" + productCategoryLevel3 + '\'' +
                ", priority='" + priority + '\'' +
                ", model='" + model + '\'' +
                ", sku='" + sku + '\'' +
                ", stage='" + stage + '\'' +
                ", productName='" + productName + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", projectStartTime='" + projectStartTime + '\'' +
                ", targetLaunchTime='" + targetLaunchTime + '\'' +
                ", displayType='" + displayType + '\'' +
                ", productLevel='" + productLevel + '\'' +
                ", primarySupplier='" + primarySupplier + '\'' +
                ", leadDqe='" + leadDqe + '\'' +
                ", electronicRd='" + electronicRd + '\'' +
                ", groupName='" + groupName + '\'' +
                ", status='" + status + '\'' +
                ", designReviewProblem='" + designReviewProblem + '\'' +
                ", evtProblem='" + evtProblem + '\'' +
                ", dvtProblem='" + dvtProblem + '\'' +
                ", mainProjectProgress='" + mainProjectProgress + '\'' +
                ", mainQualityRisks='" + mainQualityRisks + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", createBy='" + createBy + '\'' +
                ", updateBy='" + updateBy + '\'' +
                ", isDeleted=" + isDeleted +
                '}';
    }
}


