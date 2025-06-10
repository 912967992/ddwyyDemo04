package com.lu.ddwyydemo04.pojo;

public class TrashProject {
    private String sample_id;            // 测试编号
    private String sample_name;          // 产品名称
    private String sample_model;           // 大小编码
    private String materialCode;           // 大小编码
    private String sample_leader;        // 项目负责人
    private String scheduleDays;         // 排期天数（数据库为 decimal(3,1)，这里用 String 或 BigDecimal）
    private String schedule_color;       // 颜色
    private String waitSample_classify;  // 放池子分类（待送样归类）
    private String remark;               // 送样备注
    private String cancel_by;            // 作废人
    private String cancel_code;          // 作废人工号
    private String cancel_date;          // 作废时间
    private String cancel_reason;        // 作废理由

    // Getters and Setters
    public String getSample_id() {
        return sample_id;
    }
    public void setSample_id(String sample_id) {
        this.sample_id = sample_id;
    }

    public String getSample_name() {
        return sample_name;
    }
    public void setSample_name(String sample_name) {
        this.sample_name = sample_name;
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

    public String getSample_leader() {
        return sample_leader;
    }
    public void setSample_leader(String sample_leader) {
        this.sample_leader = sample_leader;
    }

    public String getScheduleDays() {
        return scheduleDays;
    }
    public void setScheduleDays(String scheduleDays) {
        this.scheduleDays = scheduleDays;
    }

    public String getSchedule_color() {
        return schedule_color;
    }
    public void setSchedule_color(String schedule_color) {
        this.schedule_color = schedule_color;
    }

    public String getWaitSample_classify() {
        return waitSample_classify;
    }
    public void setWaitSample_classify(String waitSample_classify) {
        this.waitSample_classify = waitSample_classify;
    }

    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCancel_by() {
        return cancel_by;
    }
    public void setCancel_by(String cancel_by) {
        this.cancel_by = cancel_by;
    }

    public String getCancel_code() {
        return cancel_code;
    }
    public void setCancel_code(String cancel_code) {
        this.cancel_code = cancel_code;
    }

    public String getCancel_date() {
        return cancel_date;
    }
    public void setCancel_date(String cancel_date) {
        this.cancel_date = cancel_date;
    }

    public String getCancel_reason() {
        return cancel_reason;
    }
    public void setCancel_reason(String cancel_reason) {
        this.cancel_reason = cancel_reason;
    }

    @Override
    public String toString() {
        return "TrashProject{" +
                "sample_id='" + sample_id + '\'' +
                ", sample_name='" + sample_name + '\'' +
                ", sample_model='" + sample_model + '\'' +
                ", materialCode='" + materialCode + '\'' +
                ", sample_leader='" + sample_leader + '\'' +
                ", scheduleDays='" + scheduleDays + '\'' +
                ", schedule_color='" + schedule_color + '\'' +
                ", waitSample_classify='" + waitSample_classify + '\'' +
                ", remark='" + remark + '\'' +
                ", cancel_by='" + cancel_by + '\'' +
                ", cancel_code='" + cancel_code + '\'' +
                ", cancel_date='" + cancel_date + '\'' +
                ", cancel_reason='" + cancel_reason + '\'' +
                '}';
    }
}
