package com.lu.ddwyydemo04.pojo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ChangeRecord {

    private String tester;
    private LocalDate schedule_start_date;
    private LocalDate  schedule_end_date;
    private LocalDateTime  update_time;
    private String schedule_color;
    private String isUsed;
    private String remark;

    public String getTester() {
        return tester;
    }

    public void setTester(String tester) {
        this.tester = tester;
    }

    public LocalDate getSchedule_start_date() {
        return schedule_start_date;
    }

    public void setSchedule_start_date(LocalDate schedule_start_date) {
        this.schedule_start_date = schedule_start_date;
    }

    public LocalDate getSchedule_end_date() {
        return schedule_end_date;
    }

    public void setSchedule_end_date(LocalDate schedule_end_date) {
        this.schedule_end_date = schedule_end_date;
    }

    public LocalDateTime getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(LocalDateTime update_time) {
        this.update_time = update_time;
    }

    public String getSchedule_color() {
        return schedule_color;
    }

    public void setSchedule_color(String schedule_color) {
        this.schedule_color = schedule_color;
    }

    public String getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(String isUsed) {
        this.isUsed = isUsed;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "changeRecord{" +
                "tester='" + tester + '\'' +
                ", schedule_start_date=" + schedule_start_date +
                ", schedule_end_date=" + schedule_end_date +
                ", update_time=" + update_time +
                ", schedule_color='" + schedule_color + '\'' +
                ", isUsed='" + isUsed + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
