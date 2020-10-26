package project;

import java.math.BigDecimal;

public class Wifi_Info_Json {
    private String SITE_ID; // 站點代碼
    private String AGENCY; // 管理單位
    private String NAME; // 熱點名稱
    private String ADDR; // 地址
    private BigDecimal LONGITUDE; // 經度
    private BigDecimal LATITUDE; // 緯度

    protected String getSITE_ID() {
        return SITE_ID;
    }

    protected void setSITE_ID(String SITE_ID) {
        this.SITE_ID = SITE_ID;
    }

    protected String getAGENCY() {
        return AGENCY;
    }

    protected void setAGENCY(String AGENCY) {
        this.AGENCY = AGENCY;
    }

    protected String getNAME() {
        return NAME;
    }

    protected void setNAME(String NAME) {
        this.NAME = NAME;
    }

    protected String getADDR() {
        return ADDR;
    }

    protected void setADDR(String ADDR) {
        this.ADDR = ADDR;
    }

    protected BigDecimal getLONGITUDE() {
        return LONGITUDE;
    }

    protected void setLONGITUDE(BigDecimal LONGITUDE) {
        this.LONGITUDE = LONGITUDE;
    }

    protected BigDecimal getLATITUDE() {
        return LATITUDE;
    }

    protected void setLATITUDE(BigDecimal LATITUDE) {
        this.LATITUDE = LATITUDE;
    }
}
