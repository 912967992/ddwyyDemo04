package com.lu.ddwyydemo04.pojo;

import java.time.LocalDateTime;

/**
 * 代表数据库中 `test_issues` 表的实体类.
 */
public class TestIssues {

    // 主键，自增字段
    private Long id;

    // 完整编码(大小编码)
    private String full_model;

    // 样品阶段
    private String sample_stage;

    // 版本
    private String version;

    // 芯片方案
    private String chip_solution;

    // 报告日期，格式为 'YYYYMMDD'
    private String problem_time;

    // 测试人员
    private String tester;

    // 测试平台
    private String test_platform;

    // 测试设备
    private String test_device;

    // 其他设备
    private String other_device;

    private String problem;

    // 问题视频或图片路径
    private String problem_image_or_video;

    // 复现手法
    private String reproduction_method;

    // 恢复方法
    private String recovery_method;

    // 复现概率
    private String reproduction_probability;

    // 缺陷等级
    private String defect_level;

    // 当前状态
    private String current_status;

    // 对比上一版或竞品
    private String comparison_with_previous;

    // DQE & 研发确认
    private String dqe_and_development_confirm;

    // 改善对策（研发回复）
    private String improvement_plan;

    // 分析责任人
    private String responsible_person;

    // 改善后风险
    private String post_improvement_risk;

    // 下一版回归测试
    private String next_version_regression_test;

    // 备注
    private String remark;

    // 创建时间，默认为当前时间
    private LocalDateTime created_at;

    // 历史版本 ID
    private int history_id;

    // 关联的样品ID
    private int sample_id;

    private String final_status;
    private String modifier;
    private LocalDateTime modified_time;

    private String dqe_status;
    private String dqe_comments;
    private LocalDateTime dqe_reviewed_at;
    private String rd_status;
    private String rd_comments;
    private LocalDateTime rd_reviewed_at;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFull_model() {
        return full_model;
    }

    public void setFull_model(String full_model) {
        this.full_model = full_model;
    }

    public String getSample_stage() {
        return sample_stage;
    }

    public void setSample_stage(String sample_stage) {
        this.sample_stage = sample_stage;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getChip_solution() {
        return chip_solution;
    }

    public void setChip_solution(String chip_solution) {
        this.chip_solution = chip_solution;
    }

    public String getProblem_time() {
        return problem_time;
    }

    public void setProblem_time(String problem_time) {
        this.problem_time = problem_time;
    }

    public String getTester() {
        return tester;
    }

    public void setTester(String tester) {
        this.tester = tester;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getTest_platform() {
        return test_platform;
    }

    public void setTest_platform(String test_platform) {
        this.test_platform = test_platform;
    }

    public String getTest_device() {
        return test_device;
    }

    public void setTest_device(String test_device) {
        this.test_device = test_device;
    }

    public String getOther_device() {
        return other_device;
    }

    public void setOther_device(String other_device) {
        this.other_device = other_device;
    }

    public String getProblem_image_or_video() {
        return problem_image_or_video;
    }

    public void setProblem_image_or_video(String problem_image_or_video) {
        this.problem_image_or_video = problem_image_or_video;
    }

    public String getReproduction_method() {
        return reproduction_method;
    }

    public void setReproduction_method(String reproduction_method) {
        this.reproduction_method = reproduction_method;
    }

    public String getRecovery_method() {
        return recovery_method;
    }

    public void setRecovery_method(String recovery_method) {
        this.recovery_method = recovery_method;
    }

    public String getReproduction_probability() {
        return reproduction_probability;
    }

    public void setReproduction_probability(String reproduction_probability) {
        this.reproduction_probability = reproduction_probability;
    }

    public String getDefect_level() {
        return defect_level;
    }

    public void setDefect_level(String defect_level) {
        this.defect_level = defect_level;
    }

    public String getCurrent_status() {
        return current_status;
    }

    public void setCurrent_status(String current_status) {
        this.current_status = current_status;
    }

    public String getComparison_with_previous() {
        return comparison_with_previous;
    }

    public void setComparison_with_previous(String comparison_with_previous) {
        this.comparison_with_previous = comparison_with_previous;
    }

    public String getDqe_and_development_confirm() {
        return dqe_and_development_confirm;
    }

    public void setDqe_and_development_confirm(String dqe_and_development_confirm) {
        this.dqe_and_development_confirm = dqe_and_development_confirm;
    }

    public String getImprovement_plan() {
        return improvement_plan;
    }

    public void setImprovement_plan(String improvement_plan) {
        this.improvement_plan = improvement_plan;
    }

    public String getResponsible_person() {
        return responsible_person;
    }

    public void setResponsible_person(String responsible_person) {
        this.responsible_person = responsible_person;
    }

    public String getPost_improvement_risk() {
        return post_improvement_risk;
    }

    public void setPost_improvement_risk(String post_improvement_risk) {
        this.post_improvement_risk = post_improvement_risk;
    }

    public String getNext_version_regression_test() {
        return next_version_regression_test;
    }

    public void setNext_version_regression_test(String next_version_regression_test) {
        this.next_version_regression_test = next_version_regression_test;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public int getHistory_id() {
        return history_id;
    }

    public void setHistory_id(int history_id) {
        this.history_id = history_id;
    }

    public int getSample_id() {
        return sample_id;
    }

    public void setSample_id(int sample_id) {
        this.sample_id = sample_id;
    }

    public String getFinal_status() {
        return final_status;
    }

    public void setFinal_status(String final_status) {
        this.final_status = final_status;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public LocalDateTime getModified_time() {
        return modified_time;
    }

    public void setModified_time(LocalDateTime modified_time) {
        this.modified_time = modified_time;
    }

    public String getDqe_status() {
        return dqe_status;
    }

    public void setDqe_status(String dqe_status) {
        this.dqe_status = dqe_status;
    }

    public String getDqe_comments() {
        return dqe_comments;
    }

    public void setDqe_comments(String dqe_comments) {
        this.dqe_comments = dqe_comments;
    }

    public LocalDateTime getDqe_reviewed_at() {
        return dqe_reviewed_at;
    }

    public void setDqe_reviewed_at(LocalDateTime dqe_reviewed_at) {
        this.dqe_reviewed_at = dqe_reviewed_at;
    }

    public String getRd_status() {
        return rd_status;
    }

    public void setRd_status(String rd_status) {
        this.rd_status = rd_status;
    }

    public String getRd_comments() {
        return rd_comments;
    }

    public void setRd_comments(String rd_comments) {
        this.rd_comments = rd_comments;
    }

    public LocalDateTime getRd_reviewed_at() {
        return rd_reviewed_at;
    }

    public void setRd_reviewed_at(LocalDateTime rd_reviewed_at) {
        this.rd_reviewed_at = rd_reviewed_at;
    }

    @Override
    public String toString() {
        return "TestIssues{" +
                "id=" + id +
                ", full_model='" + full_model + '\'' +
                ", sample_stage='" + sample_stage + '\'' +
                ", version='" + version + '\'' +
                ", chip_solution='" + chip_solution + '\'' +
                ", problem_time='" + problem_time + '\'' +
                ", tester='" + tester + '\'' +
                ", test_platform='" + test_platform + '\'' +
                ", test_device='" + test_device + '\'' +
                ", other_device='" + other_device + '\'' +
                ", problem='" + problem + '\'' +
                ", problem_image_or_video='" + problem_image_or_video + '\'' +
                ", reproduction_method='" + reproduction_method + '\'' +
                ", recovery_method='" + recovery_method + '\'' +
                ", reproduction_probability='" + reproduction_probability + '\'' +
                ", defect_level='" + defect_level + '\'' +
                ", current_status='" + current_status + '\'' +
                ", comparison_with_previous='" + comparison_with_previous + '\'' +
                ", dqe_and_development_confirm='" + dqe_and_development_confirm + '\'' +
                ", improvement_plan='" + improvement_plan + '\'' +
                ", responsible_person='" + responsible_person + '\'' +
                ", post_improvement_risk='" + post_improvement_risk + '\'' +
                ", next_version_regression_test='" + next_version_regression_test + '\'' +
                ", remark='" + remark + '\'' +
                ", created_at=" + created_at +
                ", history_id=" + history_id +
                ", sample_id=" + sample_id +
                ", final_status='" + final_status + '\'' +
                ", modifier='" + modifier + '\'' +
                ", modified_time=" + modified_time +
                ", dqe_status='" + dqe_status + '\'' +
                ", dqe_comments='" + dqe_comments + '\'' +
                ", dqe_reviewed_at=" + dqe_reviewed_at +
                ", rd_status='" + rd_status + '\'' +
                ", rd_comments='" + rd_comments + '\'' +
                ", rd_reviewed_at=" + rd_reviewed_at +
                '}';
    }
}
