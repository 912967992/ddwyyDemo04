package com.lu.ddwyydemo04.pojo;

public class SystemInfo {
    private int id;                          // 自增主键
    private String version;                  // 操作系统版本
    private String installationDate;  // 安装日期
    private String osVersion;                // 操作系统详细版本号
    private String architecture;             // 系统架构
    private String computerName;             // 计算机名称
    private String systemModel;              // 系统型号
    private String created_at;
    private String fullOS;
    private String cpu;
    private String memory;
    private String displays;
    private String networkAdapters;


    // 无参构造函数
//    public SystemInfo() {
//    }

    // 全参构造函数
//    public SystemInfo(int id, String version, String installationDate, String osVersion, String architecture,
//                      String computerName, String systemModel, String created_at) {
//        this.id = id;
//        this.version = version;
//        this.installationDate = installationDate;
//        this.osVersion = osVersion;
//        this.architecture = architecture;
//        this.computerName = computerName;
//        this.systemModel = systemModel;
//    }

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

    public String getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(String installationDate) {
        this.installationDate = installationDate;
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

    public String getSystemModel() {
        return systemModel;
    }

    public void setSystemModel(String systemModel) {
        this.systemModel = systemModel;
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

    public String getNetworkAdapters() {
        return networkAdapters;
    }

    public void setNetworkAdapters(String networkAdapters) {
        this.networkAdapters = networkAdapters;
    }

    @Override
    public String toString() {
        return "SystemInfo{" +
                "id=" + id +
                ", version='" + version + '\'' +
                ", installationDate='" + installationDate + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", architecture='" + architecture + '\'' +
                ", computerName='" + computerName + '\'' +
                ", systemModel='" + systemModel + '\'' +
                ", created_at='" + created_at + '\'' +
                ", fullOS='" + fullOS + '\'' +
                ", cpu='" + cpu + '\'' +
                ", memory='" + memory + '\'' +
                ", display='" + displays + '\'' +
                ", networkAdapters='" + networkAdapters + '\'' +
                '}';
    }
}