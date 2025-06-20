package com.lu.ddwyydemo04.pojo;

public class Group {

    private String id;
    private String name;
    private boolean is_displayed;
    private int display_order;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIs_displayed() {
        return is_displayed;
    }

    public void setIs_displayed(boolean is_displayed) {
        this.is_displayed = is_displayed;
    }

    public int getDisplay_order() {
        return display_order;
    }

    public void setDisplay_order(int display_order) {
        this.display_order = display_order;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", is_displayed=" + is_displayed +
                ", display_order=" + display_order +
                '}';
    }
}
