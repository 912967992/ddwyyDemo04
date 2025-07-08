package com.lu.ddwyydemo04.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ElectricalTestItem {

    @JsonProperty("ETTestCode")  // 映射前端 JSON 字段名
    private String ETTestCode;
    private String testProjectCategory;
    private String testProjects;

    // 0707 新增字段：测试归属，对应的测试备注
    private String testOwner;   //测试归属

    private String testRemark;   //对应测试备注

    public String getETTestCode() {
        return ETTestCode;
    }

    public void setETTestCode(String ETTestCode) {
        this.ETTestCode = ETTestCode;
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

    public String getTestOwner() {
        return testOwner;
    }

    public void setTestOwner(String testOwner) {
        this.testOwner = testOwner;
    }

    public String getTestRemark() {
        return testRemark;
    }

    public void setTestRemark(String testRemark) {
        this.testRemark = testRemark;
    }

    @Override
    public String toString() {
        return "ElectricalTestItem{" +
                "ETTestCode='" + ETTestCode + '\'' +
                ", testProjectCategory='" + testProjectCategory + '\'' +
                ", testProjects='" + testProjects + '\'' +
                ", testOwner='" + testOwner + '\'' +
                ", testRemark='" + testRemark + '\'' +
                '}';
    }
}
