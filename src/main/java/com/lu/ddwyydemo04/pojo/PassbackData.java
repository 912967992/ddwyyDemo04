package com.lu.ddwyydemo04.pojo;

import org.apache.tomcat.jni.Local;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// 这个PassbackData实体类，是eletric_info的封装对象类
public class PassbackData {

    private int id;
    private String sample_id;
    private String sample_actual_id;
    private String sample_category;
    private String sample_model;
//    private String sample_coding;
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

    private int isUsed;

    private String changeRecord; // 变更记录

    private LocalDateTime schedule_start_date; // 排期开始时间
    private LocalDateTime schedule_end_date;   // 排期结束时间


    private BigDecimal scheduleDays;

    private int isCancel;
    private String cancel_reason;
    private String cancel_by;
    private String cancel_date;
    private String cancel_code;

    private LocalDateTime actual_start_time;
    private LocalDateTime actual_finish_time;

    private List<MaterialItem> materialItems;  // List for material items
    private List<ElectricalTestItem> electricalTestItems;  // List for electrical test items

    private String schedule_color;
    private LocalDateTime reportReviewTime;
    private String sampleRecognizeResult;
    private String tester;
    private String testDuration;

    private String filepath;

    private String remark;

    private String waitSample_classify;

    //20250708 新增字段
    private String sample_sender;   // 送样人
    private String sample_type;   //送样类别： 功能样品OR大货样

    private String sample_quantity;   //送样数量
    private String high_frequency;   // 是否高频
    private String productRequirement;   // 产品开发要求
    private String productApprovalDoc;   // 产品承认书


    // 0707新增字段，送养人，样品状态，送样备注，电气测试数量，是否含高频，产品开发要求，产品承认书

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

    public String getSample_actual_id() {
        return sample_actual_id;
    }

    public void setSample_actual_id(String sample_actual_id) {
        this.sample_actual_id = sample_actual_id;
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

    public BigDecimal getScheduleDays() {
        return scheduleDays;
    }

    public void setScheduleDays(BigDecimal scheduleDays) {
        this.scheduleDays = scheduleDays;
    }

    public int getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(int isUsed) {
        this.isUsed = isUsed;
    }

    public int getIsCancel() {
        return isCancel;
    }

    public void setIsCancel(int isCancel) {
        this.isCancel = isCancel;
    }

    public String getCancel_reason() {
        return cancel_reason;
    }

    public void setCancel_reason(String cancel_reason) {
        this.cancel_reason = cancel_reason;
    }

    public String getCancel_by() {
        return cancel_by;
    }

    public void setCancel_by(String cancel_by) {
        this.cancel_by = cancel_by;
    }

    public String getCancel_date() {
        return cancel_date;
    }

    public void setCancel_date(String cancel_date) {
        this.cancel_date = cancel_date;
    }

    public String getCancel_code() {
        return cancel_code;
    }

    public void setCancel_code(String cancel_code) {
        this.cancel_code = cancel_code;
    }


    public LocalDateTime getActual_start_time() {
        return actual_start_time;
    }

    public void setActual_start_time(LocalDateTime actual_start_time) {
        this.actual_start_time = actual_start_time;
    }

    public LocalDateTime getActual_finish_time() {
        return actual_finish_time;
    }

    public void setActual_finish_time(LocalDateTime actual_finish_time) {
        this.actual_finish_time = actual_finish_time;
    }

    public List<MaterialItem> getMaterialItems() {
        return materialItems;
    }

    public void setMaterialItems(List<MaterialItem> materialItems) {
        this.materialItems = materialItems;
    }

    public List<ElectricalTestItem> getElectricalTestItems() {
        return electricalTestItems;
    }

    public void setElectricalTestItems(List<ElectricalTestItem> electricalTestItems) {
        this.electricalTestItems = electricalTestItems;
    }

    public String getSchedule_color() {
        return schedule_color;
    }

    public void setSchedule_color(String schedule_color) {
        this.schedule_color = schedule_color;
    }

    public LocalDateTime getReportReviewTime() {
        return reportReviewTime;
    }

    public void setReportReviewTime(LocalDateTime reportReviewTime) {
        this.reportReviewTime = reportReviewTime;
    }

    public String getSampleRecognizeResult() {
        return sampleRecognizeResult;
    }

    public void setSampleRecognizeResult(String sampleRecognizeResult) {
        this.sampleRecognizeResult = sampleRecognizeResult;
    }

    public String getTester() {
        return tester;
    }

    public void setTester(String tester) {
        this.tester = tester;
    }

    public String getTestDuration() {
        return testDuration;
    }

    public void setTestDuration(String testDuration) {
        this.testDuration = testDuration;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }


    public String getChangeRecord() {
        return changeRecord;
    }

    public void setChangeRecord(String changeRecord) {
        this.changeRecord = changeRecord;
    }

    public LocalDateTime getSchedule_start_date() {
        return schedule_start_date;
    }

    public void setSchedule_start_date(LocalDateTime schedule_start_date) {
        this.schedule_start_date = schedule_start_date;
    }

    public LocalDateTime getSchedule_end_date() {
        return schedule_end_date;
    }

    public void setSchedule_end_date(LocalDateTime schedule_end_date) {
        this.schedule_end_date = schedule_end_date;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getWaitSample_classify() {
        return waitSample_classify;
    }

    public void setWaitSample_classify(String waitSample_classify) {
        this.waitSample_classify = waitSample_classify;
    }

    public String getSample_sender() {
        return sample_sender;
    }

    public void setSample_sender(String sample_sender) {
        this.sample_sender = sample_sender;
    }

    public String getSample_type() {
        return sample_type;
    }

    public void setSample_type(String sample_type) {
        this.sample_type = sample_type;
    }

    public String getSample_quantity() {
        return sample_quantity;
    }

    public void setSample_quantity(String sample_quantity) {
        this.sample_quantity = sample_quantity;
    }

    public String getHigh_frequency() {
        return high_frequency;
    }

    public void setHigh_frequency(String high_frequency) {
        this.high_frequency = high_frequency;
    }

    public String getProductRequirement() {
        return productRequirement;
    }

    public void setProductRequirement(String productRequirement) {
        this.productRequirement = productRequirement;
    }

    public String getProductApprovalDoc() {
        return productApprovalDoc;
    }

    public void setProductApprovalDoc(String productApprovalDoc) {
        this.productApprovalDoc = productApprovalDoc;
    }

    @Override
    public String toString() {
        return "PassbackData{" +
                "id=" + id +
                ", sample_id='" + sample_id + '\'' +
                ", sample_actual_id='" + sample_actual_id + '\'' +
                ", sample_category='" + sample_category + '\'' +
                ", sample_model='" + sample_model + '\'' +
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
                ", isUsed=" + isUsed +
                ", changeRecord='" + changeRecord + '\'' +
                ", schedule_start_date=" + schedule_start_date +
                ", schedule_end_date=" + schedule_end_date +
                ", scheduleDays=" + scheduleDays +
                ", isCancel=" + isCancel +
                ", cancel_reason='" + cancel_reason + '\'' +
                ", cancel_by='" + cancel_by + '\'' +
                ", cancel_date='" + cancel_date + '\'' +
                ", cancel_code='" + cancel_code + '\'' +
                ", actual_start_time=" + actual_start_time +
                ", actual_finish_time=" + actual_finish_time +
                ", materialItems=" + materialItems +
                ", electricalTestItems=" + electricalTestItems +
                ", schedule_color='" + schedule_color + '\'' +
                ", reportReviewTime=" + reportReviewTime +
                ", sampleRecognizeResult='" + sampleRecognizeResult + '\'' +
                ", tester='" + tester + '\'' +
                ", testDuration='" + testDuration + '\'' +
                ", filepath='" + filepath + '\'' +
                ", remark='" + remark + '\'' +
                ", waitSample_classify='" + waitSample_classify + '\'' +
                ", sample_sender='" + sample_sender + '\'' +
                ", sample_type='" + sample_type + '\'' +
                ", sample_quantity='" + sample_quantity + '\'' +
                ", high_frequency='" + high_frequency + '\'' +
                ", productRequirement='" + productRequirement + '\'' +
                ", productApprovalDoc='" + productApprovalDoc + '\'' +
                '}';
    }
}
