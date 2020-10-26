package project;

import java.math.BigDecimal;

public class Wifi_Info {
    private String siteId; // 站點代碼
    private String name; // 熱點名稱
    private int areaCode; // 郵遞區號
    private String areaName; // 地區
    private String address; // 地址
    private BigDecimal longitude; // 經度
    private BigDecimal latitude; // 緯度
    private String agency; // 管理單位
    private int version; // 資料版本

    protected String getSiteId() {
        return siteId;
    }

    protected void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    protected String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected int getAreaCode() {
        return areaCode;
    }

    protected void setAreaCode(int areaCode) {
        this.areaCode = areaCode;
    }

    protected String getAreaName() {
        return areaName;
    }

    protected void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    protected String getAddress() {
        return address;
    }

    protected void setAddress(String address) {
        this.address = address;
    }

    protected BigDecimal getLongitude() {
        return longitude;
    }

    protected void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    protected BigDecimal getLatitude() {
        return latitude;
    }

    protected void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    protected String getAgency() {
        return agency;
    }

    protected void setAgency(String agency) {
        this.agency = agency;
    }

    protected int getVersion() {
        return version;
    }

    protected void setVersion(int version) {
        this.version = version;
    }

    protected Wifi_Info(String siteId, String name, int areaCode, String areaName, String address, BigDecimal longitude,
            BigDecimal latitude, String agency, int version) {
        this.siteId = siteId;
        this.name = name;
        this.areaCode = areaCode;
        this.areaName = areaName;
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.agency = agency;
        this.version = version;
    }
}
