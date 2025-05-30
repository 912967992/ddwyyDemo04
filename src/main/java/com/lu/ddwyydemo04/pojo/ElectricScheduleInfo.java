package com.lu.ddwyydemo04.pojo;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class ElectricScheduleInfo {
    private Integer id;
    private String sample_id;

    private String tester;

    private LocalDate schedule_start_date;
    private LocalDate schedule_end_date;

    private LocalDateTime create_time;
    private LocalDateTime update_time;

    private String sizecoding;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }



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


    public LocalDateTime getCreate_time() {
        return create_time;
    }

    public void setCreate_time(LocalDateTime create_time) {
        this.create_time = create_time;
    }

    public LocalDateTime getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(LocalDateTime update_time) {
        this.update_time = update_time;
    }

    public String getSizecoding() {
        return sizecoding;
    }

    public void setSizecoding(String sizecoding) {
        this.sizecoding = sizecoding;
    }

    public String getSample_id() {
        return sample_id;
    }

    public void setSample_id(String sample_id) {
        this.sample_id = sample_id;
    }

    @Override
    public String toString() {
        return "ElectricScheduleInfo{" +
                "id=" + id +
                ", sample_id=" + sample_id +
                ", tester='" + tester + '\'' +
                ", schedule_start_date=" + schedule_start_date +
                ", schedule_end_date=" + schedule_end_date +
                ", create_time=" + create_time +
                ", update_time=" + update_time +
                ", sizecoding='" + sizecoding + '\'' +
                '}';
    }
}
