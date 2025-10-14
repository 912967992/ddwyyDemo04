package com.lu.ddwyydemo04.pojo;

public class SystemInfo {
    private int id;                          // 序号
    private String deviceCategory;           // 设备分类
    private String storageArea;              // 存放区域
    private String brand;                    // 品牌
    private String deviceName;                  // 设备名称
    private String modelNumber;              // 型号（铭牌型号）
    private String interfaceTypeAndQuantity; // 接口类型&数量
    private String graphicsInterfaceTypeAndQuantity; // 显卡接口类型&数量
    private String maxOutputSpec;            // 最高输出规格
    private String screenSize;               // 内建屏幕尺寸
    private String screenRatio;              // 屏幕比例
    private String releaseDate;              // 上市日期
    private String devicePurchaseDate;       // 购买日期
    private String deviceRepairHistory;      // 维修记录
    private String source;                   // 来源
    private String graphicsSource;           // 显卡来源

    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceCategory() {
        return deviceCategory;
    }

    public void setDeviceCategory(String deviceCategory) {
        this.deviceCategory = deviceCategory;
    }

    public String getStorageArea() {
        return storageArea;
    }

    public void setStorageArea(String storageArea) {
        this.storageArea = storageArea;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getInterfaceTypeAndQuantity() {
        return interfaceTypeAndQuantity;
    }

    public void setInterfaceTypeAndQuantity(String interfaceTypeAndQuantity) {
        this.interfaceTypeAndQuantity = interfaceTypeAndQuantity;
    }

    public String getGraphicsInterfaceTypeAndQuantity() {
        return graphicsInterfaceTypeAndQuantity;
    }

    public void setGraphicsInterfaceTypeAndQuantity(String graphicsInterfaceTypeAndQuantity) {
        this.graphicsInterfaceTypeAndQuantity = graphicsInterfaceTypeAndQuantity;
    }

    public String getMaxOutputSpec() {
        return maxOutputSpec;
    }

    public void setMaxOutputSpec(String maxOutputSpec) {
        this.maxOutputSpec = maxOutputSpec;
    }

    public String getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(String screenSize) {
        this.screenSize = screenSize;
    }

    public String getScreenRatio() {
        return screenRatio;
    }

    public void setScreenRatio(String screenRatio) {
        this.screenRatio = screenRatio;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDevicePurchaseDate() {
        return devicePurchaseDate;
    }

    public void setDevicePurchaseDate(String devicePurchaseDate) {
        this.devicePurchaseDate = devicePurchaseDate;
    }

    public String getDeviceRepairHistory() {
        return deviceRepairHistory;
    }

    public void setDeviceRepairHistory(String deviceRepairHistory) {
        this.deviceRepairHistory = deviceRepairHistory;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getGraphicsSource() {
        return graphicsSource;
    }

    public void setGraphicsSource(String graphicsSource) {
        this.graphicsSource = graphicsSource;
    }

    @Override
    public String toString() {
        return "SystemInfo{" +
                "id=" + id +
                ", deviceCategory='" + deviceCategory + '\'' +
                ", storageArea='" + storageArea + '\'' +
                ", brand='" + brand + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", modelNumber='" + modelNumber + '\'' +
                ", interfaceTypeAndQuantity='" + interfaceTypeAndQuantity + '\'' +
                ", graphicsInterfaceTypeAndQuantity='" + graphicsInterfaceTypeAndQuantity + '\'' +
                ", maxOutputSpec='" + maxOutputSpec + '\'' +
                ", screenSize='" + screenSize + '\'' +
                ", screenRatio='" + screenRatio + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", devicePurchaseDate='" + devicePurchaseDate + '\'' +
                ", deviceRepairHistory='" + deviceRepairHistory + '\'' +
                ", source='" + source + '\'' +
                ", graphicsSource='" + graphicsSource + '\'' +
                '}';
    }
}