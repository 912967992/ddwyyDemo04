package com.lu.ddwyydemo04.pojo;

public class CombinedTaskNode {
    private TaskNode taskNode;
    private String fullModel;
    private String bigSpecial;
    private String smallSpecial;
    private String sampleCategory;

    private String version;
    private String questStats;

    private String dqe;

    private String rd;

    private String projectLeader;



    // getters and setters
    public TaskNode getTaskNode() {
        return taskNode;
    }

    public void setTaskNode(TaskNode taskNode) {
        this.taskNode = taskNode;
    }

    public String getFullModel() {
        return fullModel;
    }

    public void setFullModel(String fullModel) {
        this.fullModel = fullModel;
    }

    public String getBigSpecial() {
        return bigSpecial;
    }

    public void setBigSpecial(String bigSpecial) {
        this.bigSpecial = bigSpecial;
    }

    public String getSmallSpecial() {
        return smallSpecial;
    }

    public void setSmallSpecial(String smallSpecial) {
        this.smallSpecial = smallSpecial;
    }

    public String getSampleCategory() {
        return sampleCategory;
    }

    public void setSampleCategory(String sampleCategory) {
        this.sampleCategory = sampleCategory;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getQuestStats() {
        return questStats;
    }

    public void setQuestStats(String questStats) {
        this.questStats = questStats;
    }

    public String getDqe() {
        return dqe;
    }

    public void setDqe(String dqe) {
        this.dqe = dqe;
    }

    public String getRd() {
        return rd;
    }

    public void setRd(String rd) {
        this.rd = rd;
    }

    public String getProjectLeader() {
        return projectLeader;
    }

    public void setProjectLeader(String projectLeader) {
        this.projectLeader = projectLeader;
    }

    @Override
    public String toString() {
        return "CombinedTaskNode{" +
                "taskNode=" + taskNode +
                ", fullModel='" + fullModel + '\'' +
                ", bigSpecial='" + bigSpecial + '\'' +
                ", smallSpecial='" + smallSpecial + '\'' +
                ", sampleCategory='" + sampleCategory + '\'' +
                ", version='" + version + '\'' +
                ", questStats='" + questStats + '\'' +
                ", dqe='" + dqe + '\'' +
                ", rd='" + rd + '\'' +
                ", projectLeader='" + projectLeader + '\'' +
                '}';
    }
}
