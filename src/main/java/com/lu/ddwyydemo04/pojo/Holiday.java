package com.lu.ddwyydemo04.pojo;

import java.util.Date;

public class Holiday {
    private Integer id;
    private String holiday_date;
    private String holiday_name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHoliday_date() {
        return holiday_date;
    }

    public void setHoliday_date(String holiday_date) {
        this.holiday_date = holiday_date;
    }

    public String getHoliday_name() {
        return holiday_name;
    }

    public void setHoliday_name(String holiday_name) {
        this.holiday_name = holiday_name;
    }

    @Override
    public String toString() {
        return "Holiday{" +
                "id=" + id +
                ", holiday_date=" + holiday_date +
                ", holiday_name='" + holiday_name + '\'' +
                '}';
    }
}
