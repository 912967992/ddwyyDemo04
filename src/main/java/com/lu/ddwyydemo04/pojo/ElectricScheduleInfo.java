package com.lu.ddwyydemo04.pojo;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class ElectricScheduleInfo {
    private Integer id;
    private Integer electric_info_id;

    private String tester;

    private LocalDate schedule_start_date;
    private LocalDate schedule_end_date;

    private Integer row_index;
    private Integer column_index;
    private LocalDateTime create_time;
    private LocalDateTime update_time;

    private String sizecoding;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getElectric_info_id() {
        return electric_info_id;
    }

    public void setElectric_info_id(Integer electric_info_id) {
        this.electric_info_id = electric_info_id;
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

    public Integer getRow_index() {
        return row_index;
    }

    public void setRow_index(Integer row_index) {
        this.row_index = row_index;
    }

    public Integer getColumn_index() {
        return column_index;
    }

    public void setColumn_index(Integer column_index) {
        this.column_index = column_index;
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

    @Override
    public String toString() {
        return "ElectricScheduleInfo{" +
                "id=" + id +
                ", electric_info_id=" + electric_info_id +
                ", tester='" + tester + '\'' +
                ", schedule_start_date=" + schedule_start_date +
                ", schedule_end_date=" + schedule_end_date +
                ", row_index=" + row_index +
                ", column_index=" + column_index +
                ", create_time=" + create_time +
                ", update_time=" + update_time +
                ", sizecoding='" + sizecoding + '\'' +
                '}';
    }
}
