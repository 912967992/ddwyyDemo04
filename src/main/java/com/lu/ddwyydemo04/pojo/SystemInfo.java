package com.lu.ddwyydemo04.pojo;

public class SystemInfo {
    private int id;                          // 自增主键
    private String personCharge;                          //  负责统计的人

    private String computerName;             // 计算机名称
    private String brand;             // 品牌
    private String area;             // 区域

    private String deviceType;   // 设备类型：屏幕，主机，显示器，游戏机等
    private String version;                  // 操作系统版本
    private String osVersion;                // 操作系统详细版本号

    private String fullOS;
    private String architecture;             // 系统架构

    private String created_at;

    private String cpu;
    private String memory;
    private String displays;

//    private double roundedScreenSize;  //屏幕尺寸
    private String maxResolution;  //最大分辨率
    private String maxRefreshRate;  //刷新率
    private String interfaceInfo;  //接口信息



    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }



    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }


    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getFullOS() {
        return fullOS;
    }

    public void setFullOS(String fullOS) {
        this.fullOS = fullOS;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getDisplays() {
        return displays;
    }

    public void setDisplays(String displays) {
        this.displays = displays;
    }




    public String getMaxResolution() {
        return maxResolution;
    }

    public void setMaxResolution(String maxResolution) {
        this.maxResolution = maxResolution;
    }

    public String getMaxRefreshRate() {
        return maxRefreshRate;
    }

    public void setMaxRefreshRate(String maxRefreshRate) {
        this.maxRefreshRate = maxRefreshRate;
    }


    public String getInterfaceInfo() {
        return interfaceInfo;
    }

    public void setInterfaceInfo(String interfaceInfo) {
        this.interfaceInfo = interfaceInfo;
    }


    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getPersonCharge() {
        return personCharge;
    }

    public void setPersonCharge(String personCharge) {
        this.personCharge = personCharge;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Override
    public String toString() {
        return "SystemInfo{" +
                "id=" + id +
                ", personCharge='" + personCharge + '\'' +
                ", computerName='" + computerName + '\'' +
                ", brand='" + brand + '\'' +
                ", area='" + area + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", version='" + version + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", fullOS='" + fullOS + '\'' +
                ", architecture='" + architecture + '\'' +
                ", created_at='" + created_at + '\'' +
                ", cpu='" + cpu + '\'' +
                ", memory='" + memory + '\'' +
                ", displays='" + displays + '\'' +
                ", maxResolution='" + maxResolution + '\'' +
                ", maxRefreshRate='" + maxRefreshRate + '\'' +
                ", interfaceInfo='" + interfaceInfo + '\'' +
                '}';
    }
}