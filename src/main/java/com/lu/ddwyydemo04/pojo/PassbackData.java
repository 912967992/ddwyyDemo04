package com.lu.ddwyydemo04.pojo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// 这个PassbackData实体类，是eletric_info的封装对象类
public class PassbackData {

    private int id;
    private String sample_id;
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

    @Override
    public String toString() {
        return "PassbackData{" +
                "id=" + id +
                ", sample_id='" + sample_id + '\'' +
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
                '}';
    }
}
