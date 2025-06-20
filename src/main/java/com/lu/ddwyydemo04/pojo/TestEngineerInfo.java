package com.lu.ddwyydemo04.pojo;

import java.sql.Timestamp;

public class TestEngineerInfo {

    private Integer id;

    private String engineer_id;

    private String test_engineer_name;
    private String hire_date;

    private String responsible_category;

    private String skill_description;

    private String skill_improvernment;

    private String current_skill_level;

    private Timestamp created_at;

    private Timestamp updated_at;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEngineer_id() {
        return engineer_id;
    }

    public void setEngineer_id(String engineer_id) {
        this.engineer_id = engineer_id;
    }

    public String getTest_engineer_name() {
        return test_engineer_name;
    }

    public void setTest_engineer_name(String test_engineer_name) {
        this.test_engineer_name = test_engineer_name;
    }

    public String getHire_date() {
        return hire_date;
    }

    public void setHire_date(String hire_date) {
        this.hire_date = hire_date;
    }

    public String getResponsible_category() {
        return responsible_category;
    }

    public void setResponsible_category(String responsible_category) {
        this.responsible_category = responsible_category;
    }

    public String getSkill_description() {
        return skill_description;
    }

    public void setSkill_description(String skill_description) {
        this.skill_description = skill_description;
    }

    public String getSkill_improvernment() {
        return skill_improvernment;
    }

    public void setSkill_improvernment(String skill_improvernment) {
        this.skill_improvernment = skill_improvernment;
    }

    public String getCurrent_skill_level() {
        return current_skill_level;
    }

    public void setCurrent_skill_level(String current_skill_level) {
        this.current_skill_level = current_skill_level;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public Timestamp getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Timestamp updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "TestEngineerInfo{" +
                "id=" + id +
                ", engineer_id='" + engineer_id + '\'' +
                ", test_engineer_name='" + test_engineer_name + '\'' +
                ", hire_date='" + hire_date + '\'' +
                ", responsible_category='" + responsible_category + '\'' +
                ", skill_description='" + skill_description + '\'' +
                ", skill_improvernment='" + skill_improvernment + '\'' +
                ", current_skill_level='" + current_skill_level + '\'' +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
                '}';
    }
}
