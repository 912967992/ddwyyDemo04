package com.lu.ddwyydemo04.pojo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ChangeRecord {
    private Long id;
    private String electric_sample_id;
    private String tester;
    private LocalDate start_date;
    private LocalDate end_date;
    private LocalDateTime update_time;
    private String schedule_color;
    private String is_used;
    private String remark;

    public ChangeRecord() {}

    public ChangeRecord(String electric_sample_id, String tester, LocalDate start_date, 
                       LocalDate end_date, LocalDateTime update_time, String schedule_color, 
                       String is_used, String remark) {
        this.electric_sample_id = electric_sample_id;
        this.tester = tester;
        this.start_date = start_date;
        this.end_date = end_date;
        this.update_time = update_time;
        this.schedule_color = schedule_color;
        this.is_used = is_used;
        this.remark = remark;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getElectric_sample_id() {
        return electric_sample_id;
    }

    public void setElectric_sample_id(String electric_sample_id) {
        this.electric_sample_id = electric_sample_id;
    }

    public String getTester() {
        return tester;
    }

    public void setTester(String tester) {
        this.tester = tester;
    }

    public LocalDate getStart_date() {
        return start_date;
    }

    public void setStart_date(LocalDate start_date) {
        this.start_date = start_date;
    }

    public LocalDate getEnd_date() {
        return end_date;
    }

    public void setEnd_date(LocalDate end_date) {
        this.end_date = end_date;
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

    public String getIs_used() {
        return is_used;
    }

    public void setIs_used(String is_used) {
        this.is_used = is_used;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "ChangeRecord{" +
                "id=" + id +
                ", electric_sample_id='" + electric_sample_id + '\'' +
                ", tester='" + tester + '\'' +
                ", start_date=" + start_date +
                ", end_date=" + end_date +
                ", update_time=" + update_time +
                ", schedule_color='" + schedule_color + '\'' +
                ", is_used='" + is_used + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
