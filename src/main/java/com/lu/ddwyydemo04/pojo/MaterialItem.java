package com.lu.ddwyydemo04.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MaterialItem {
//    @JsonProperty("STTestCode")  // 映射前端 JSON 字段名
    private String STTestCode;
    private String materialCode;
    private int sample_frequency;

    public String getSTTestCode() {
        return STTestCode;
    }

    public void setSTTestCode(String STTestCode) {
        this.STTestCode = STTestCode;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public int getSample_frequency() {
        return sample_frequency;
    }

    public void setSample_frequency(int sample_frequency) {
        this.sample_frequency = sample_frequency;
    }

    @Override
    public String toString() {
        return "MaterialItem{" +
                "STTestCode='" + STTestCode + '\'' +
                ", materialCode='" + materialCode + '\'' +
                ", sample_frequency=" + sample_frequency +
                '}';
    }
}
