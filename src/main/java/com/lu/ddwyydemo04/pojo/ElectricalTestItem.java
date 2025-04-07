package com.lu.ddwyydemo04.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ElectricalTestItem {

    @JsonProperty("ETTestCode")  // 映射前端 JSON 字段名
    private String ETTestCode;
    private String testProjectCategory;
    private String testProjects;

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

    @Override
    public String toString() {
        return "ElectricalTestItem{" +
                "ETTestCode='" + ETTestCode + '\'' +
                ", testProjectCategory='" + testProjectCategory + '\'' +
                ", testProjects='" + testProjects + '\'' +
                '}';
    }
}
