package com.lu.ddwyydemo04.pojo;

public class SystemInfo {
    private int id;                          // 自增主键
    private String version;                  // 操作系统版本
    private String installationDate;  // 安装日期
    private String osVersion;                // 操作系统详细版本号
    private String architecture;             // 系统架构
    private String computerName;             // 计算机名称
    private String systemModel;              // 系统型号


    // 无参构造函数
    public SystemInfo() {
    }

    // 全参构造函数
    public SystemInfo(int id, String version, String installationDate, String osVersion, String architecture,
                      String computerName, String systemModel, String localTimestamp) {
        this.id = id;
        this.version = version;
        this.installationDate = installationDate;
        this.osVersion = osVersion;
        this.architecture = architecture;
        this.computerName = computerName;
        this.systemModel = systemModel;
    }

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



    @Override
    public String toString() {
        return "SystemInfo{" +
                "id=" + id +
                ", version='" + version + '\'' +
                ", installationDate=" + installationDate +
                ", osVersion='" + osVersion + '\'' +
                ", architecture='" + architecture + '\'' +
                ", computerName='" + computerName + '\'' +
                ", systemModel='" + systemModel + '\'' +
                '}';
    }
}