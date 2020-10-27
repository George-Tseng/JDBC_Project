package project;

import org.apache.commons.dbcp2.BasicDataSource;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WifiInfoJDBCDAO implements WifiInfoDAO {

    /* 各方法中皆使用到的屬性獨立出來 */
    private BasicDataSource dataSource;

    /**
     * 取得連線池所需的BasicDataSource
     *
     * @param dbInfo DB資訊(List<String>)
     *
     * @return BasicDataSource物件
     */
    /* 取得連線池所需的BasicDataSource */
    public BasicDataSource getDataSource(List<String> dbInfo) {
        if (dataSource == null) {
            String decodeTime = Translator.getMessageChar(dbInfo.get(0));
            String decodeMacAddress = Translator.getMessageChar(dbInfo.get(1));
            String dbUser = KeyCodec.getDecodeKeyLong(decodeTime, decodeMacAddress, dbInfo.get(4));
            String dbPassword = KeyCodec.getDecodeKeyLong(decodeTime, decodeMacAddress, dbInfo.get(5));
            String dbDriverClassName = KeyCodec.getDecodeKeyLong(decodeTime, decodeMacAddress, dbInfo.get(6));
            String dbUrl = KeyCodec.getDecodeKeyLong(decodeTime, decodeMacAddress, dbInfo.get(7));

            /* 第一次呼叫時才需要產生屬性 */
            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName(dbDriverClassName);
            ds.setUrl(dbUrl);
            ds.setUsername(dbUser);
            ds.setPassword(dbPassword);
            ds.setMaxIdle(20);// 最大閒置連線數
            ds.setMaxTotal(50);// 最大同時連線數
            dataSource = ds;
        }
        return dataSource;
    }

    /**
     * 查詢全部資料
     *
     * @param dbInfo DB資訊(List<String>)
     *
     * @return 第一項為boolean，代表成功/失敗，第二項為失敗時的訊息說明(String)，第三項為成功回傳時的資料陣列
     */
    /* 查詢全部資料 */
    public QueryResult queryDBData(List<String> dbInfo) {
        QueryResult sqlSelectAllResult = new QueryResult();
        List<Wifi_Info> wifiLocationList = new ArrayList<>();

        try (Connection connection0 = getDataSource(dbInfo).getConnection()) {
            try (PreparedStatement preStmt0 = connection0.prepareStatement("SELECT * FROM dbo.wifi_info")) {
                /* begin transaction */
                connection0.setAutoCommit(false);
                /* 執行 */
                ResultSet rs0 = preStmt0.executeQuery();

                while (rs0.next()) {
                    Wifi_Info wifiLocationData = new Wifi_Info("", "", 0, "", "", new BigDecimal("0"),
                            new BigDecimal("0"), "", 0);

                    wifiLocationData.setSiteId(rs0.getString("SITE_ID"));
                    wifiLocationData.setName(rs0.getString("NAME"));
                    wifiLocationData.setAreaCode(rs0.getInt("AREACODE"));
                    wifiLocationData.setAreaName(rs0.getString("AREANAME"));
                    wifiLocationData.setAddress(rs0.getString("ADDR"));
                    wifiLocationData.setLongitude(rs0.getBigDecimal("LONGITUDE"));
                    wifiLocationData.setLatitude(rs0.getBigDecimal("LATITUDE"));
                    wifiLocationData.setAgency(rs0.getString("AGENCY"));
                    wifiLocationData.setVersion(rs0.getInt("VERSION"));

                    wifiLocationList.add(wifiLocationData);
                }
                /* commit transaction */
                connection0.commit();

                sqlSelectAllResult.setSuccess(true);
                sqlSelectAllResult.setSqlData(wifiLocationList);
            } catch (SQLException sqlE) {
                /* rollback transaction */
                connection0.rollback();

                sqlSelectAllResult.setSuccess(false);
                sqlSelectAllResult.setContents("遇到錯誤：" + System.lineSeparator() + sqlE.toString());
            } catch (Exception e) {
                /* rollback transaction */
                connection0.rollback();

                sqlSelectAllResult.setSuccess(false);
                sqlSelectAllResult.setContents("遇到錯誤：" + System.lineSeparator() + e.toString());
            }
        } catch (SQLException sqlE0) {
            sqlSelectAllResult.setSuccess(false);
            sqlSelectAllResult.setContents("遇到錯誤：" + System.lineSeparator() + sqlE0.toString());
        } catch (Exception e0) {
            sqlSelectAllResult.setSuccess(false);
            sqlSelectAllResult.setContents("遇到錯誤：" + System.lineSeparator() + e0.toString());
        }
        return sqlSelectAllResult;
    }

    /**
     * 新增資料
     *
     * @param insertPKList 所選取的主鍵列表(List<String>)
     * @param wifiInfoList 來源檔的資料陣列
     * @param dbInfo       DB資訊(List<String>)
     *
     * @return 第一項訊息為成功/失敗，第二項為失敗時的訊息說明(String)
     */
    /* 新增資料 */
    public List<String> insertDBData(List<String> insertPKList, List<Wifi_Info> wifiInfoList, List<String> dbInfo) {
        List<String> insertResult = new ArrayList<>();
        int firstVer = 0, batchCount = 0;

        try (Connection connection0 = getDataSource(dbInfo).getConnection()) {
            try (PreparedStatement preStmt0 = connection0.prepareStatement(
                    "INSERT INTO dbo.wifi_info(SITE_ID, NAME, AGENCY, AREACODE, AREANAME, ADDR, LONGITUDE, LATITUDE, VERSION) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                /* begin transaction */
                connection0.setAutoCommit(false);

                for (Wifi_Info wifiInfoData : wifiInfoList) {
                    if (insertPKList.contains(wifiInfoData.getSiteId())) {
                        preStmt0.setString(1, wifiInfoData.getSiteId()); // 設定熱點代碼
                        preStmt0.setString(2, wifiInfoData.getName()); // 設定熱點名稱
                        preStmt0.setString(3, wifiInfoData.getAgency()); // 設定主管機關
                        preStmt0.setInt(4, wifiInfoData.getAreaCode()); // 設定郵遞區號
                        preStmt0.setString(5, wifiInfoData.getAreaName()); // 設定縣市區域
                        preStmt0.setString(6, wifiInfoData.getAddress()); // 設定所在區域
                        preStmt0.setBigDecimal(7, wifiInfoData.getLongitude()); // 設定經度
                        preStmt0.setBigDecimal(8, wifiInfoData.getLatitude()); // 設定緯度
                        preStmt0.setInt(9, firstVer); // 設定版本

                        /* 加入批次 */
                        preStmt0.addBatch();
                        batchCount++;
                        /* 清空變數 */
                        preStmt0.clearParameters();

                        /* 每20筆執行一次 */
                        if (batchCount % 20 == 0) {
                            // 執行executeBatch
                            preStmt0.executeBatch();
                            /* 清空batch */
                            preStmt0.clearBatch();
                        }
                    }
                }

                // 執行executeBatch，以完成剩餘不到20筆的紀錄
                preStmt0.executeBatch();
                /* 清空batch */
                preStmt0.clearBatch();
                /* commit transaction */
                connection0.commit();

                insertResult.add("Success");
                insertResult.add("共新增 " + insertPKList.size() + " 筆資料到資料庫");
            } catch (SQLException sqlE) {
                /* rollback transaction */
                connection0.rollback();

                insertResult.add("Fail");
                insertResult.add("遇到錯誤：" + System.lineSeparator() + sqlE.toString());
            } catch (Exception e) {
                /* rollback transaction */
                connection0.rollback();

                insertResult.add("Fail");
                insertResult.add("遇到錯誤：" + System.lineSeparator() + e.toString());
            }
        } catch (SQLException sqlE0) {
            insertResult.add("Fail");
            insertResult.add("遇到錯誤：" + System.lineSeparator() + sqlE0.toString());
        } catch (Exception e0) {
            insertResult.add("Fail");
            insertResult.add("遇到錯誤：" + System.lineSeparator() + e0.toString());
        }
        return insertResult;
    }

    /**
     * 修改資料
     *
     * @param dbInfo     DB資訊(List<String>)
     * @param dbDataList 來自資料庫的資料陣列
     * @param updateInfo 單筆的修改結果(自定義類)
     *
     * @return 第一項為boolean，代表成功/失敗，第二項為失敗時的訊息說明(String)，第三項為成功時所記錄下的資料陣列(一項)
     */
    /* 修改資料 */
    public QueryResult updateData(List<String> dbInfo, List<Wifi_Info> dbDataList, Wifi_Info updateInfo) {
        QueryResult updateResult = new QueryResult();
        /* 紀錄資料是否有更動 */
        int isChange = 0;
        /* 各欄位是否有改動 */
        boolean isColumn1Change = false, isColumn2Change = false, isColumn3Change = false, isColumn4Change = false,
                isColumn5Change = false, isColumn6Change = false, isColumn7Change = false;
        /* 設定熱點代碼 */
        String siteId = updateInfo.getSiteId();
        Wifi_Info dbItem = new Wifi_Info("", "", 0, "", "", new BigDecimal("0"), new BigDecimal("0"), "", 0);
        /* 即將回傳的陣列 */
        List<Wifi_Info> updateResultList = new ArrayList<>();
        /* 被更新的該筆資料 */
        Wifi_Info updatedItem = new Wifi_Info("", "", 0, "", "", new BigDecimal("0"), new BigDecimal("0"), "", 0);

        for (Wifi_Info dbData : dbDataList) {
            if (dbData.getSiteId().equals(siteId)) {
                dbItem = dbData;
            }
        }

        String name = "";
        if (!dbItem.getName().equals(updateInfo.getName()) && !updateInfo.getName().equals("")) {
            isChange++;
            name = updateInfo.getName();
            /* 帶入熱點名稱 */
            updatedItem.setName(name);
            isColumn1Change = true;
        } else {
            /* 帶入熱點名稱 */
            updatedItem.setName(dbItem.getName());
        }
        int areaCode = 0;
        if (dbItem.getAreaCode() != updateInfo.getAreaCode() && updateInfo.getAreaCode() != 0) {
            isChange++;
            areaCode = updateInfo.getAreaCode();
            /* 帶入郵遞區號 */
            updatedItem.setAreaCode(areaCode);
            isColumn2Change = true;
        } else {
            /* 帶入郵遞區號 */
            updatedItem.setAreaCode(dbItem.getAreaCode());
        }
        String areaName = "";
        if (!dbItem.getAreaName().equals(updateInfo.getAreaName()) && !updateInfo.getAreaName().equals("")) {
            isChange++;
            areaName = updateInfo.getAreaName();
            /* 帶入縣市區域 */
            updatedItem.setAreaName(areaName);
            isColumn3Change = true;
        } else {
            /* 帶入縣市區域 */
            updatedItem.setAreaName(dbItem.getAreaName());
        }
        String address = "";
        if (!dbItem.getAddress().equals(updateInfo.getAddress()) && !updateInfo.getAddress().equals("")) {
            isChange++;
            address = updateInfo.getAddress();
            /* 帶入地址 */
            updatedItem.setAddress(address);
            isColumn4Change = true;
        } else {
            /* 帶入地址 */
            updatedItem.setAddress(dbItem.getAddress());
        }
        BigDecimal longitude = new BigDecimal("0");
        if (dbItem.getLongitude().compareTo(updateInfo.getLongitude()) != 0
                && updateInfo.getLongitude().compareTo(longitude) != 0) {
            isChange++;
            longitude = updateInfo.getLongitude();
            /* 帶入經度 */
            updatedItem.setLongitude(longitude);
            isColumn5Change = true;
        } else {
            /* 帶入經度 */
            updatedItem.setLongitude(dbItem.getLongitude());
        }
        BigDecimal latitude = new BigDecimal("0");
        if (dbItem.getLatitude().compareTo(updateInfo.getLatitude()) != 0
                && updateInfo.getLatitude().compareTo(latitude) != 0) {
            isChange++;
            latitude = updateInfo.getLatitude();
            /* 帶入緯度 */
            updatedItem.setLatitude(latitude);
            isColumn6Change = true;
        } else {
            /* 帶入緯度 */
            updatedItem.setLatitude(dbItem.getLatitude());
        }
        String agency = "";
        if (!dbItem.getAgency().equals(updateInfo.getAgency()) && !updateInfo.getAgency().equals("")) {
            isChange++;
            agency = updateInfo.getAgency();
            /* 帶入主管機關 */
            updatedItem.setAgency(agency);
            isColumn7Change = true;
        } else {
            /* 帶入主管機關 */
            updatedItem.setAgency(dbItem.getAgency());
        }

        /* 資料沒異動 */
        if (isChange == 0) {
            updateResult.setSuccess(false);
            updateResult.setContents("拒絕變動！資料內容沒有改變...");
        }
        /* 有異動 */
        else {
            int version = dbItem.getVersion() + 1;
            /* 帶入熱點代碼 */
            updatedItem.setSiteId(updateInfo.getSiteId());
            /* 帶入資料版本 */
            updatedItem.setVersion(version);

            try (Connection connection0 = getDataSource(dbInfo).getConnection()) {
                PreparedStatement preStmt0;
                try {
                    /* begin transaction */
                    connection0.setAutoCommit(false);
                    /* 熱點名稱、郵遞區號、縣市區域、地址、經度、緯度跟主管機關都有變更 */
                    if (isColumn1Change && isColumn2Change && isColumn3Change && isColumn4Change && isColumn5Change
                            && isColumn6Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, AREANAME = ?, ADDR = ?, LONGITUDE = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ? AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, areaName); // 設定縣市區域
                        preStmt0.setString(4, address); // 設定地址
                        preStmt0.setBigDecimal(5, longitude); // 設定經度
                        preStmt0.setBigDecimal(6, latitude); // 設定緯度
                        preStmt0.setString(7, agency); // 設定主管機關
                    }
                    /* 熱點名稱、郵遞區號、縣市區域、地址、經度跟緯度都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn3Change && isColumn4Change && isColumn5Change
                            && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, AREANAME = ?, ADDR = ?, LONGITUDE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, areaName); // 設定縣市區域
                        preStmt0.setString(4, address); // 設定地址
                        preStmt0.setBigDecimal(5, longitude); // 設定經度
                        preStmt0.setBigDecimal(6, latitude); // 設定緯度
                    }
                    /* 熱點名稱、郵遞區號、縣市區域、地址、經度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn3Change && isColumn4Change && isColumn5Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, AREANAME = ?, ADDR = ?, LONGITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, areaName); // 設定縣市區域
                        preStmt0.setString(4, address); // 設定地址
                        preStmt0.setBigDecimal(5, longitude); // 設定經度
                        preStmt0.setString(6, agency); // 設定主管機關
                    }
                    /* 熱點名稱、郵遞區號、縣市區域、地址、緯度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn3Change && isColumn4Change && isColumn6Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, AREANAME = ?, ADDR = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, areaName); // 設定縣市區域
                        preStmt0.setString(4, address); // 設定地址
                        preStmt0.setBigDecimal(5, latitude); // 設定緯度
                        preStmt0.setString(6, agency); // 設定主管機關
                    }
                    /* 熱點名稱、郵遞區號、縣市區域、經度、緯度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn3Change && isColumn5Change && isColumn6Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, AREANAME = ?, LONGITUDE = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(4, longitude); // 設定精度
                        preStmt0.setBigDecimal(5, latitude); // 設定緯度
                        preStmt0.setString(6, agency); // 設定主管機關
                    }
                    /* 熱點名稱、郵遞區號、地址、經度、緯度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn4Change && isColumn5Change && isColumn6Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, ADDR = ?, LONGITUDE = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, longitude); // 設定精度
                        preStmt0.setBigDecimal(5, latitude); // 設定緯度
                        preStmt0.setString(6, agency); // 設定主管機關
                    }
                    /* 熱點名稱、縣市區域、地址、經度、緯度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn3Change && isColumn4Change && isColumn5Change && isColumn6Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, ADDR = ?, LONGITUDE = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, longitude); // 設定精度
                        preStmt0.setBigDecimal(5, latitude); // 設定緯度
                        preStmt0.setString(6, agency); // 設定主管機關
                    }
                    /* 郵遞區號、縣市區域、地址、經度、緯度跟主管機關都有變更 */
                    else if (isColumn2Change && isColumn3Change && isColumn4Change && isColumn5Change && isColumn6Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, AREANAME = ?, ADDR = ?, LONGITUDE = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, longitude); // 設定精度
                        preStmt0.setBigDecimal(5, latitude); // 設定緯度
                        preStmt0.setString(6, agency); // 設定主管機關
                    }
                    /* 熱點名稱、郵遞區號、縣市區域、地址跟經度都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn3Change && isColumn4Change
                            && isColumn5Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, AREANAME = ?, ADDR = ?, LONGITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, areaName); // 設定縣市區域
                        preStmt0.setString(4, address); // 設定地址
                        preStmt0.setBigDecimal(5, longitude); // 設定經度
                    }
                    /* 熱點名稱、郵遞區號、縣市區域、地址跟緯度都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn3Change && isColumn4Change
                            && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, AREANAME = ?, ADDR = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, areaName); // 設定縣市區域
                        preStmt0.setString(4, address); // 設定地址
                        preStmt0.setBigDecimal(5, latitude); // 設定緯度
                    }
                    /* 熱點名稱、郵遞區號、縣市區域、地址跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn3Change && isColumn4Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, AREANAME = ?, ADDR = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, areaName); // 設定縣市區域
                        preStmt0.setString(4, address); // 設定地址
                        preStmt0.setString(5, agency); // 設定主管機關
                    }
                    /* 熱點名稱、郵遞區號、縣市區域、經度跟緯度都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn3Change && isColumn5Change
                            && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, AREANAME = ?, LONGITUDE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(4, longitude); // 設定經度
                        preStmt0.setBigDecimal(5, latitude); // 設定緯度
                    }
                    /* 熱點名稱、郵遞區號、縣市區域、經度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn3Change && isColumn5Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, AREANAME = ?, LONGITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(4, longitude); // 設定經度
                        preStmt0.setString(5, agency); // 設定主管機關
                    }
                    /* 熱點名稱、郵遞區號、地址、經度跟緯度都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn4Change && isColumn5Change
                            && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, ADDR = ?, LONGITUDE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, longitude); // 設定經度
                        preStmt0.setBigDecimal(5, latitude); // 設定緯度
                    }
                    /* 熱點名稱、郵遞區號、地址、經度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn4Change && isColumn5Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, ADDR = ?, LONGITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, longitude); // 設定經度
                        preStmt0.setString(5, agency); // 設定主管機關
                    }
                    /* 熱點名稱、郵遞區號、地址、緯度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn4Change && isColumn6Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, ADDR = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                        preStmt0.setString(5, agency); // 設定主管機關
                    }
                    /* 熱點名稱、縣市區域、地址、經度跟緯度都有變更 */
                    else if (isColumn1Change && isColumn3Change && isColumn4Change && isColumn5Change
                            && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, ADDR = ?, LONGITUDE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, longitude); // 設定經度
                        preStmt0.setBigDecimal(5, latitude); // 設定緯度
                    }
                    /* 熱點名稱、縣市區域、地址、經度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn3Change && isColumn4Change && isColumn5Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, ADDR = ?, LONGITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, longitude); // 設定經度
                        preStmt0.setString(5, agency); // 設定主管機關
                    }
                    /* 熱點名稱、縣市區域、地址、緯度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn3Change && isColumn4Change && isColumn6Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, ADDR = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                        preStmt0.setString(5, agency); // 設定主管機關
                    }
                    /* 熱點名稱、縣市區域、經度、緯度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn3Change && isColumn5Change && isColumn6Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, LONGITUDE = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                        preStmt0.setString(5, agency); // 設定主管機關
                    }
                    /* 熱點名稱、地址、經度、緯度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn4Change && isColumn5Change && isColumn6Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, ADDR = ?, LONGITUDE = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                        preStmt0.setString(5, agency); // 設定主管機關
                    }
                    /* 郵遞區號、縣市區域、地址、經度跟緯度都有變更 */
                    else if (isColumn2Change && isColumn3Change && isColumn4Change && isColumn5Change
                            && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, ADDR = ?, LONGITUDE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, longitude); // 設定經度
                        preStmt0.setBigDecimal(5, latitude); // 設定緯度
                    }
                    /* 郵遞區號、縣市區域、地址、經度跟主管機關都有變更 */
                    else if (isColumn2Change && isColumn3Change && isColumn4Change && isColumn5Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, ADDR = ?, LONGITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, longitude); // 設定經度
                        preStmt0.setString(5, agency); // 設定主管機關
                    }
                    /* 郵遞區號、縣市區域、地址、緯度跟主管機關都有變更 */
                    else if (isColumn2Change && isColumn3Change && isColumn4Change && isColumn6Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, ADDR = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                        preStmt0.setString(5, agency); // 設定主管機關
                    }
                    /* 郵遞區號、縣市區域、經度、緯度跟主管機關都有變更 */
                    else if (isColumn2Change && isColumn3Change && isColumn5Change && isColumn6Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, LONGITUDE = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                        preStmt0.setString(5, agency); // 設定主管機關
                    }
                    /* 郵遞區號、地址、經度、緯度跟主管機關都有變更 */
                    else if (isColumn2Change && isColumn4Change && isColumn5Change && isColumn6Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, ADDR = ?, LONGITUDE = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                        preStmt0.setString(5, agency); // 設定主管機關
                    }
                    /* 縣市區域、地址、經度、緯度跟主管機關都有變更 */
                    else if (isColumn3Change && isColumn4Change && isColumn5Change && isColumn6Change
                            && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREANAME = ?, ADDR = ?, LONGITUDE = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, areaName); // 設定縣市區域
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                        preStmt0.setString(5, agency); // 設定主管機關
                    }
                    /* 熱點名稱、郵遞區號、縣市區域跟地址都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn3Change && isColumn4Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, AREANAME = ?, ADDR = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, areaName); // 設定縣市區域
                        preStmt0.setString(4, address); // 設定地址
                    }
                    /* 熱點名稱、郵遞區號、縣市區域跟經度都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn3Change && isColumn5Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, AREANAME = ?, LONGITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(4, longitude); // 設定經度
                    }
                    /* 熱點名稱、郵遞區號、縣市區域跟緯度都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn3Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, AREANAME = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                    }
                    /* 熱點名稱、郵遞區號、縣市區域跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn3Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, AREANAME = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, areaName); // 設定縣市區域
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 熱點名稱、郵遞區號、地址跟經度都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn4Change && isColumn5Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, ADDR = ?, LONGITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, longitude); // 設定經度
                    }
                    /* 熱點名稱、郵遞區號、地址跟緯度都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn4Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, ADDR = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                    }
                    /* 熱點名稱、郵遞區號、地址跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn4Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, ADDR = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 熱點名稱、郵遞區號、經度跟緯度都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn5Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, LONGITUDE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                    }
                    /* 熱點名稱、郵遞區號、經度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn5Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, LONGITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 熱點名稱、郵遞區號、緯度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn6Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 熱點名稱、縣市區域、地址跟經度都有變更 */
                    else if (isColumn1Change && isColumn3Change && isColumn4Change && isColumn5Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, ADDR = ?, LONGITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, longitude); // 設定經度
                    }
                    /* 熱點名稱、縣市區域、地址跟緯度都有變更 */
                    else if (isColumn1Change && isColumn3Change && isColumn4Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, ADDR = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                    }
                    /* 熱點名稱、縣市區域、地址跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn3Change && isColumn4Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, ADDR = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 熱點名稱、縣市區域、經度跟緯度都有變更 */
                    else if (isColumn1Change && isColumn3Change && isColumn5Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, LONGITUDE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                    }
                    /* 熱點名稱、縣市區域、經度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn3Change && isColumn5Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, LONGITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 熱點名稱、縣市區域、緯度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn3Change && isColumn6Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 熱點名稱、地址、經度跟緯度都有變更 */
                    else if (isColumn1Change && isColumn4Change && isColumn5Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, ADDR = ?, LONGITUDE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                    }
                    /* 熱點名稱、地址、經度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn4Change && isColumn5Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, ADDR = ?, LONGITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 熱點名稱、地址、緯度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn4Change && isColumn6Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, ADDR = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 熱點名稱、經度、緯度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn5Change && isColumn6Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, LONGITUDE = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setBigDecimal(2, longitude); // 設定經度
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 郵遞區號、縣市區域、地址跟經度都有變更 */
                    else if (isColumn2Change && isColumn3Change && isColumn4Change && isColumn5Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, AREANAME = ?, ADDR = ?, LONGITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, longitude); // 設定經度
                    }
                    /* 郵遞區號、縣市區域、地址跟緯度都有變更 */
                    else if (isColumn2Change && isColumn3Change && isColumn4Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, AREANAME = ?, ADDR = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                    }
                    /* 郵遞區號、縣市區域、地址跟主管機關都有變更 */
                    else if (isColumn2Change && isColumn3Change && isColumn4Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, AREANAME = ?, ADDR = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, address); // 設定地址
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 郵遞區號、縣市區域、經度跟緯度都有變更 */
                    else if (isColumn2Change && isColumn3Change && isColumn5Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, AREANAME = ?, LONGITUDE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                    }
                    /* 郵遞區號、縣市區域、經度跟主管機關都有變更 */
                    else if (isColumn2Change && isColumn3Change && isColumn5Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, AREANAME = ?, LONGITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 郵遞區號、縣市區域、緯度跟主管機關都有變更 */
                    else if (isColumn2Change && isColumn3Change && isColumn6Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, AREANAME = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 郵遞區號、地址、經度跟緯度都有變更 */
                    else if (isColumn2Change && isColumn4Change && isColumn5Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, ADDR = ?, LONGITUDE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                    }
                    /* 郵遞區號、地址、經度跟主管機關都有變更 */
                    else if (isColumn2Change && isColumn4Change && isColumn5Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, ADDR = ?, LONGITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 郵遞區號、地址、緯度跟主管機關都有變更 */
                    else if (isColumn2Change && isColumn4Change && isColumn6Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, ADDR = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 郵遞區號、經度、緯度跟主管機關都有變更 */
                    else if (isColumn2Change && isColumn5Change && isColumn6Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, LONGITUDE = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setBigDecimal(2, latitude); // 設定經度
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 縣市區域、地址、經度跟緯度都有變更 */
                    else if (isColumn3Change && isColumn4Change && isColumn5Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREANAME = ?, ADDR = ?, LONGITUDE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, areaName); // 設定縣市區域
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                        preStmt0.setBigDecimal(4, latitude); // 設定緯度
                    }
                    /* 縣市區域、地址、經度跟主管機關都有變更 */
                    else if (isColumn3Change && isColumn4Change && isColumn5Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREANAME = ?, ADDR = ?, LONGITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, areaName); // 設定縣市區域
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 縣市區域、地址、緯度跟主管機關都有變更 */
                    else if (isColumn3Change && isColumn4Change && isColumn6Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREANAME = ?, ADDR = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, areaName); // 設定縣市區域
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 縣市區域、經度、緯度跟主管機關都有變更 */
                    else if (isColumn3Change && isColumn5Change && isColumn6Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREANAME = ?, LONGITUDE = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(2, longitude); // 設定經度
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 地址、經度、緯度跟主管機關都有變更 */
                    else if (isColumn4Change && isColumn5Change && isColumn6Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET ADDR = ?, LONGITUDE = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, address); // 設定地址
                        preStmt0.setBigDecimal(2, longitude); // 設定經度
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                        preStmt0.setString(4, agency); // 設定主管機關
                    }
                    /* 熱點名稱、郵遞區號跟縣市區域都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn3Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, AREANAME = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, areaName); // 設定縣市區域
                    }
                    /* 熱點名稱、郵遞區號跟地址都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn4Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, ADDR = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, address); // 設定地址
                    }
                    /* 熱點名稱、郵遞區號跟經度都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn5Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, LONGITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                    }
                    /* 熱點名稱、郵遞區號跟緯度都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                    }
                    /* 熱點名稱、郵遞區號跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn2Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                        preStmt0.setString(3, agency); // 設定主管機關
                    }
                    /* 熱點名稱、縣市區域跟地址都有變更 */
                    else if (isColumn1Change && isColumn3Change && isColumn4Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, ADDR = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, address); // 設定地址
                    }
                    /* 熱點名稱、縣市區域跟經度都有變更 */
                    else if (isColumn1Change && isColumn3Change && isColumn5Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, LONGITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                    }
                    /* 熱點名稱、縣市區域跟緯度都有變更 */
                    else if (isColumn1Change && isColumn3Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                    }
                    /* 熱點名稱、縣市區域跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn3Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, agency); // 設定主管機關
                    }
                    /* 熱點名稱、地址跟經度都有變更 */
                    else if (isColumn1Change && isColumn4Change && isColumn5Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, ADDR = ?, LONGITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                    }
                    /* 熱點名稱、地址跟緯度都有變更 */
                    else if (isColumn1Change && isColumn4Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, ADDR = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                    }
                    /* 熱點名稱、地址跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn4Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, ADDR = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setString(3, agency); // 設定主管機關
                    }
                    /* 熱點名稱、經度跟緯度都有變更 */
                    else if (isColumn1Change && isColumn5Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, LONGITUDE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setBigDecimal(2, longitude); // 設定經度
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                    }
                    /* 熱點名稱、經度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn5Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, LONGITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setBigDecimal(2, longitude); // 設定經度
                        preStmt0.setString(3, agency); // 設定主管機關
                    }
                    /* 熱點名稱、緯度跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn6Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setBigDecimal(2, latitude); // 設定緯度
                        preStmt0.setString(3, agency); // 設定主管機關
                    }
                    /* 郵遞區號、縣市區域跟主管機關都有變更 */
                    else if (isColumn2Change && isColumn3Change && isColumn4Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, AREANAME = ?, ADDR = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, address); // 設定地址
                    }
                    /* 郵遞區號、縣市區域跟經度都有變更 */
                    else if (isColumn2Change && isColumn3Change && isColumn5Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, AREANAME = ?, LONGITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                    }
                    /* 郵遞區號、縣市區域跟緯度都有變更 */
                    else if (isColumn2Change && isColumn3Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, AREANAME = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                    }
                    /* 郵遞區號、縣市區域跟主管機關都有變更 */
                    else if (isColumn2Change && isColumn3Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, AREANAME = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, areaName); // 設定縣市區域
                        preStmt0.setString(3, agency); // 設定主管機關
                    }
                    /* 郵遞區號、地址跟經度都有變更 */
                    else if (isColumn2Change && isColumn4Change && isColumn5Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, ADDR = ?, LONGITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                    }
                    /* 郵遞區號、地址跟緯度都有變更 */
                    else if (isColumn2Change && isColumn4Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, ADDR = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                    }
                    /* 郵遞區號、地址跟主管機關都有變更 */
                    else if (isColumn2Change && isColumn4Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, ADDR = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setString(3, agency); // 設定主管機關
                    }
                    /* 郵遞區號、經度跟緯度都有變更 */
                    else if (isColumn2Change && isColumn5Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, LONGITUDE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setBigDecimal(2, longitude); // 設定經度
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                    }
                    /* 郵遞區號、經度跟主管機關都有變更 */
                    else if (isColumn2Change && isColumn5Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, LONGITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setBigDecimal(2, longitude); // 設定經度
                        preStmt0.setString(3, agency); // 設定主管機關
                    }
                    /* 郵遞區號、緯度跟主管機關都有變更 */
                    else if (isColumn2Change && isColumn6Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setBigDecimal(2, latitude); // 設定緯度
                        preStmt0.setString(3, agency); // 設定主管機關
                    }
                    /* 縣市區域、地址跟經度都有變更 */
                    else if (isColumn3Change && isColumn4Change && isColumn5Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREANAME = ?, ADDR = ?, LONGITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, areaName); // 設定縣市區域
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, longitude); // 設定經度
                    }
                    /* 縣市區域、地址跟緯度都有變更 */
                    else if (isColumn3Change && isColumn4Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREANAME = ?, ADDR = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, areaName); // 設定縣市區域
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                    }
                    /* 縣市區域、地址跟主管機關都有變更 */
                    else if (isColumn3Change && isColumn4Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREANAME = ?, ADDR = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, areaName); // 設定縣市區域
                        preStmt0.setString(2, address); // 設定地址
                        preStmt0.setString(3, agency); // 設定主管機關
                    }
                    /* 縣市區域、經度跟緯度都有變更 */
                    else if (isColumn3Change && isColumn5Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREANAME = ?, LONGITUDE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(2, longitude); // 設定經度
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                    }
                    /* 縣市區域、經度跟主管機關都有變更 */
                    else if (isColumn3Change && isColumn5Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREANAME = ?, LONGITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(2, longitude); // 設定經度
                        preStmt0.setString(3, agency); // 設定主管機關
                    }
                    /* 縣市區域、緯度跟主管機關都有變更 */
                    else if (isColumn3Change && isColumn6Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREANAME = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(2, latitude); // 設定緯度
                        preStmt0.setString(3, agency); // 設定主管機關
                    }
                    /* 地址、經度跟緯度都有變更 */
                    else if (isColumn4Change && isColumn5Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET ADDR = ?, LONGITUDE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, address); // 設定地址
                        preStmt0.setBigDecimal(2, longitude); // 設定經度
                        preStmt0.setBigDecimal(3, latitude); // 設定緯度
                    }
                    /* 地址、經度跟主管機關都有變更 */
                    else if (isColumn4Change && isColumn5Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET ADDR = ?, LONGITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, address); // 設定地址
                        preStmt0.setBigDecimal(2, longitude); // 設定經度
                        preStmt0.setString(3, agency); // 設定主管機關
                    }
                    /* 地址、緯度跟主管機關都有變更 */
                    else if (isColumn4Change && isColumn6Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET ADDR = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, address); // 設定地址
                        preStmt0.setBigDecimal(2, latitude); // 設定緯度
                        preStmt0.setString(3, agency); // 設定主管機關
                    }
                    /* 經度、緯度跟主管機關都有變更 */
                    else if (isColumn5Change && isColumn6Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET LONGITUDE = ?, LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setBigDecimal(1, longitude); // 設定經度
                        preStmt0.setBigDecimal(2, latitude); // 設定緯度
                        preStmt0.setString(3, agency); // 設定主管機關
                    }
                    /* 熱點名稱跟郵遞區號都有變更 */
                    else if (isColumn1Change && isColumn2Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREACODE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setInt(2, areaCode); // 設定郵遞區號
                    }
                    /* 熱點名稱跟縣市區域都有變更 */
                    else if (isColumn1Change && isColumn3Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AREANAME = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, areaName); // 設定縣市區域
                    }
                    /* 熱點名稱跟地址都有變更 */
                    else if (isColumn1Change && isColumn4Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, ADDR = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, address); // 設定地址
                    }
                    /* 熱點名稱跟經度都有變更 */
                    else if (isColumn1Change && isColumn5Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, LONGITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setBigDecimal(2, longitude); // 設定經度
                    }
                    /* 熱點名稱跟緯度都有變更 */
                    else if (isColumn1Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setBigDecimal(2, latitude); // 設定緯度
                    }
                    /* 熱點名稱跟主管機關都有變更 */
                    else if (isColumn1Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET NAME = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                        preStmt0.setString(2, agency); // 設定主管機關
                    }
                    /* 郵遞區號跟縣市區域都有變更 */
                    else if (isColumn2Change && isColumn3Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, AREANAME = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, areaName); // 設定縣市區域
                    }
                    /* 郵遞區號跟地址都有變更 */
                    else if (isColumn2Change && isColumn4Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, ADDR = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, address); // 設定地址
                    }
                    /* 郵遞區號跟經度都有變更 */
                    else if (isColumn2Change && isColumn5Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, LONGITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setBigDecimal(2, longitude); // 設定經度
                    }
                    /* 郵遞區號跟緯度都有變更 */
                    else if (isColumn2Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setBigDecimal(2, latitude); // 設定緯度
                    }
                    /* 郵遞區號跟主管機關都有變更 */
                    else if (isColumn2Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                        preStmt0.setString(2, agency); // 設定主管機關
                    }
                    /* 縣市區域跟地址都有變更 */
                    else if (isColumn3Change && isColumn4Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREANAME = ?, ADDR = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, areaName); // 設定縣市區域
                        preStmt0.setString(2, address); // 設定地址
                    }
                    /* 縣市區域跟經度都有變更 */
                    else if (isColumn3Change && isColumn5Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREANAME = ?, LONGITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(2, longitude); // 設定經度
                    }
                    /* 縣市區域跟緯度都有變更 */
                    else if (isColumn3Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREANAME = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, areaName); // 設定縣市區域
                        preStmt0.setBigDecimal(2, latitude); // 設定緯度
                    }
                    /* 縣市區域跟主管機關都有變更 */
                    else if (isColumn3Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREANAME = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, areaName); // 設定縣市區域
                        preStmt0.setString(2, agency); // 設定主管機關
                    }
                    /* 地址跟經度都有變更 */
                    else if (isColumn4Change && isColumn5Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET ADDR = ?, LONGITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, address); // 設定地址
                        preStmt0.setBigDecimal(2, longitude); // 設定經度
                    }
                    /* 地址跟緯度都有變更 */
                    else if (isColumn4Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET ADDR = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, address); // 設定地址
                        preStmt0.setBigDecimal(2, latitude); // 設定緯度
                    }
                    /* 地址跟主管機關都有變更 */
                    else if (isColumn4Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET ADDR = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, address); // 設定地址
                        preStmt0.setString(2, agency); // 設定主管機關
                    }
                    /* 經度跟緯度都有變更 */
                    else if (isColumn5Change && isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET LONGITUDE = ?, LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setBigDecimal(1, longitude); // 設定經度
                        preStmt0.setBigDecimal(2, latitude); // 設定緯度
                    }
                    /* 經度跟主管機關都有變更 */
                    else if (isColumn5Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET LONGITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setBigDecimal(1, longitude); // 設定經度
                        preStmt0.setString(2, agency); // 設定主管機關
                    }
                    /* 緯度跟主管機關都有變更 */
                    else if (isColumn6Change && isColumn7Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET LATITUDE = ?, AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setBigDecimal(1, latitude); // 設定緯度
                        preStmt0.setString(2, agency); // 設定主管機關
                    }
                    /* 只有熱點名稱有變更 */
                    else if (isColumn1Change) {
                        preStmt0 = connection0
                                .prepareStatement("UPDATE dbo.wifi_info SET NAME = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, name); // 設定熱點名稱
                    }
                    /* 只有郵遞區號有變更 */
                    else if (isColumn2Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREACODE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setInt(1, areaCode); // 設定郵遞區號
                    }
                    /* 只有縣市區域有變更 */
                    else if (isColumn3Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AREANAME = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, areaName); // 設定縣市區域
                    }
                    /* 只有地址有變更 */
                    else if (isColumn4Change) {
                        preStmt0 = connection0
                                .prepareStatement("UPDATE dbo.wifi_info SET ADDR = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, address); // 設定地址
                    }
                    /* 只有經度有變更 */
                    else if (isColumn5Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET LONGITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setBigDecimal(1, longitude); // 設定經度
                    }
                    /* 只有緯度有變更 */
                    else if (isColumn6Change) {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET LATITUDE = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setBigDecimal(1, latitude); // 設定緯度
                    }
                    /* 只有主管機關有變更 */
                    else {
                        preStmt0 = connection0.prepareStatement(
                                "UPDATE dbo.wifi_info SET AGENCY = ?, VERSION = ? WHERE SITE_ID = ?  AND VERSION = ?");
                        preStmt0.setString(1, agency); // 設定主管機關
                    }

                    if (isChange == 1) {
                        preStmt0.setInt(2, version); // 設定資料版本
                        preStmt0.setString(3, siteId); // 設定熱點代碼
                        preStmt0.setInt(4, version - 1);
                    } else if (isChange == 2) {
                        preStmt0.setInt(3, version); // 設定資料版本
                        preStmt0.setString(4, siteId); // 設定熱點代碼
                        preStmt0.setInt(5, version - 1);
                    } else if (isChange == 3) {
                        preStmt0.setInt(4, version); // 設定資料版本
                        preStmt0.setString(5, siteId); // 設定熱點代碼
                        preStmt0.setInt(6, version - 1);
                    } else if (isChange == 4) {
                        preStmt0.setInt(5, version); // 設定資料版本
                        preStmt0.setString(6, siteId); // 設定熱點代碼
                        preStmt0.setInt(7, version - 1);
                    } else if (isChange == 5) {
                        preStmt0.setInt(6, version); // 設定資料版本
                        preStmt0.setString(7, siteId); // 設定熱點代碼
                        preStmt0.setInt(8, version - 1);
                    } else if (isChange == 6) {
                        preStmt0.setInt(7, version); // 設定資料版本
                        preStmt0.setString(8, siteId); // 設定熱點代碼
                        preStmt0.setInt(9, version - 1);
                    } else if (isChange == 7) {
                        preStmt0.setInt(8, version); // 設定資料版本
                        preStmt0.setString(9, siteId); // 設定熱點代碼
                        preStmt0.setInt(10, version - 1);
                    }

                    /* 加入批次 */
                    preStmt0.addBatch();
                    /* 清空變數 */
                    preStmt0.clearParameters();
                    // 執行executeBatch
                    preStmt0.executeBatch();
                    /* 清空batch */
                    preStmt0.clearBatch();

                    /* commit transaction */
                    connection0.commit();

                    updateResultList.add(updatedItem);
                    updateResult.setSuccess(true);
                    updateResult.setSqlData(updateResultList);
                    preStmt0.close();
                } catch (SQLException sqlE) {
                    /* rollback transaction */
                    connection0.rollback();

                    updateResult.setSuccess(false);
                    updateResult.setContents("遇到錯誤：" + System.lineSeparator() + sqlE.toString());
                } catch (Exception e) {
                    /* rollback transaction */
                    connection0.rollback();

                    updateResult.setSuccess(false);
                    updateResult.setContents("遇到錯誤：" + System.lineSeparator() + e.toString());
                }
            } catch (SQLException sqlE0) {
                updateResult.setSuccess(false);
                updateResult.setContents("遇到錯誤：" + System.lineSeparator() + sqlE0.toString());
            } catch (Exception e0) {
                updateResult.setSuccess(false);
                updateResult.setContents("遇到錯誤：" + System.lineSeparator() + e0.toString());
            }
        }
        return updateResult;
    }

    /**
     * 刪除資料
     *
     * @param deletedPKList 所選取的主鍵列表(List<String>)
     * @param dbDataList    來自DB的資料陣列
     * @param dbInfo        DB資訊(List<String>)
     *
     * @return 第一項訊息為成功/失敗，第二項為失敗時的訊息說明(String)
     */
    /* 刪除資料 */
    public List<String> deleteDBData(List<String> deletedPKList, List<Wifi_Info> dbDataList, List<String> dbInfo) {
        List<String> deleteResult = new ArrayList<>();
        int batchCount = 0;

        try (Connection connection0 = getDataSource(dbInfo).getConnection()) {
            try (PreparedStatement preStmt0 = connection0
                    .prepareStatement("DELETE FROM dbo.wifi_info WHERE SITE_ID = ? ")) {
                /* begin transaction */
                connection0.setAutoCommit(false);

                for (Wifi_Info wifiInfoData : dbDataList) {
                    if (deletedPKList.contains(wifiInfoData.getSiteId())) {
                        preStmt0.setString(1, wifiInfoData.getSiteId()); // 設定熱點代碼

                        /* 加入批次 */
                        preStmt0.addBatch();
                        batchCount++;
                        /* 清空變數 */
                        preStmt0.clearParameters();

                        /* 每20筆執行一次 */
                        if (batchCount % 20 == 0) {
                            // 執行executeBatch
                            preStmt0.executeBatch();
                            /* 清空batch */
                            preStmt0.clearBatch();
                        }
                    }
                }

                // 執行executeBatch，以完成剩餘不到20筆的紀錄
                preStmt0.executeBatch();
                /* 清空batch */
                preStmt0.clearBatch();

                /* commit transaction */
                connection0.commit();

                deleteResult.add("Success");
            } catch (SQLException sqlE) {
                /* rollback transaction */
                connection0.rollback();

                deleteResult.add("Fail");
                deleteResult.add("遇到錯誤：" + System.lineSeparator() + sqlE.toString());
            } catch (Exception e) {
                /* rollback transaction */
                connection0.rollback();

                deleteResult.add("Fail");
                deleteResult.add("遇到錯誤：" + System.lineSeparator() + e.toString());
            }
        } catch (SQLException sqlE0) {
            deleteResult.add("Fail");
            deleteResult.add("遇到錯誤：" + System.lineSeparator() + sqlE0.toString());
        } catch (Exception e0) {
            deleteResult.add("Fail");
            deleteResult.add("遇到錯誤：" + System.lineSeparator() + e0.toString());
        }
        return deleteResult;
    }

    /**
     * 查詢資料
     *
     * @param dbInfo          DB資訊(List<String>)
     * @param searchCondition 查詢條件(自定義類)
     *
     * @return 第一項為boolean，代表成功/失敗，第二項為失敗時的訊息說明(String)，第三項為成功回傳時的資料陣列
     */
    /* 查詢資料 */
    public QueryResult selectDBData(List<String> dbInfo, Wifi_Info searchCondition) {
        QueryResult sqlSelectResult = new QueryResult();
        List<Wifi_Info> wifiLocationList = new ArrayList<>();
        BigDecimal decimalZero = new BigDecimal("0");

        try (Connection connection0 = getDataSource(dbInfo).getConnection()) {
            PreparedStatement preStmt0 = null;
            {
                try {
                    /* begin transaction */
                    connection0.setAutoCommit(false);

                    /* 用熱點代碼查詢 */
                    if (!searchCondition.getSiteId().equals("")) {
                        preStmt0 = connection0.prepareStatement("SELECT * FROM dbo.wifi_info WHERE SITE_ID LIKE ?");

                        String querySiteId = "%" + searchCondition.getSiteId() + "%";
                        preStmt0.setString(1, querySiteId); // 設定熱點代碼
                    }
                    /* 用熱點名稱查詢 */
                    else if (!searchCondition.getName().equals("")) {
                        preStmt0 = connection0.prepareStatement("SELECT * FROM dbo.wifi_info WHERE NAME LIKE ?");

                        String queryName = "%" + searchCondition.getName() + "%";
                        preStmt0.setString(1, queryName); // 設定熱點名稱
                    }
                    /* 用郵遞區號查詢 */
                    else if (searchCondition.getAreaCode() != 0) {
                        preStmt0 = connection0.prepareStatement("SELECT * FROM dbo.wifi_info WHERE AREACODE = ?");

                        int queryAreaCode = searchCondition.getAreaCode();
                        preStmt0.setInt(1, queryAreaCode); // 設定郵遞區號
                    }
                    /* 用縣市區域查詢 */
                    else if (!searchCondition.getAreaName().equals("") && searchCondition.getAddress().equals("")) {
                        preStmt0 = connection0.prepareStatement("SELECT * FROM dbo.wifi_info WHERE AREANAME LIKE ?");

                        String queryAreaName = "%" + searchCondition.getAreaName() + "%";
                        preStmt0.setString(1, queryAreaName); // 設定縣市區域
                    }
                    /* 用地址查詢 */
                    else if (searchCondition.getAreaName().equals("") && !searchCondition.getAddress().equals("")) {
                        preStmt0 = connection0.prepareStatement("SELECT * FROM dbo.wifi_info WHERE ADDRESS LIKE ?");

                        String queryAddress = "%" + searchCondition.getAddress() + "%";
                        preStmt0.setString(1, queryAddress); // 設定地址
                    }
                    /* 用縣市區域 + 地址查詢 */
                    else if (!searchCondition.getAreaName().equals("") && !searchCondition.getAddress().equals("")) {
                        preStmt0 = connection0.prepareStatement(
                                "SELECT * FROM dbo.wifi_info WHERE AREANAME LIKE ? AND ADDRESS LIKE ?");

                        String queryAreaName = "%" + searchCondition.getAreaName() + "%";
                        String queryAddress = "%" + searchCondition.getAddress() + "%";
                        preStmt0.setString(1, queryAreaName); // 設定縣市區域
                        preStmt0.setString(2, queryAddress); // 設定地址
                    }
                    /* 用經度查詢 */
                    else if (searchCondition.getLongitude().compareTo(decimalZero) != 0
                            && searchCondition.getLatitude().compareTo(decimalZero) == 0) {
                        preStmt0 = connection0.prepareStatement("SELECT * FROM dbo.wifi_info WHERE LONGITUDE = ?");

                        BigDecimal queryLongitude = searchCondition.getLongitude();
                        preStmt0.setBigDecimal(1, queryLongitude); // 設定經度
                    }
                    /* 用緯度查詢 */
                    else if (searchCondition.getLongitude().compareTo(decimalZero) == 0
                            && searchCondition.getLatitude().compareTo(decimalZero) != 0) {
                        preStmt0 = connection0.prepareStatement("SELECT * FROM dbo.wifi_info WHERE LATITUDE = ?");

                        BigDecimal queryLatitude = searchCondition.getLatitude();
                        preStmt0.setBigDecimal(1, queryLatitude); // 設定緯度
                    }
                    /* 用經度 + 緯度查詢 */
                    else if (searchCondition.getLongitude().compareTo(decimalZero) != 0
                            && searchCondition.getLatitude().compareTo(decimalZero) != 0) {
                        preStmt0 = connection0
                                .prepareStatement("SELECT * FROM dbo.wifi_info WHERE LONGITUDE = ? AND LATITUDE = ? ");

                        BigDecimal queryLongitude = searchCondition.getLongitude();
                        BigDecimal queryLatitude = searchCondition.getLatitude();
                        preStmt0.setBigDecimal(1, queryLongitude); // 設定經度
                        preStmt0.setBigDecimal(2, queryLatitude); // 設定緯度
                    }
                    /* 用主管機關查詢 */
                    else if (!searchCondition.getAgency().equals("")) {
                        preStmt0 = connection0.prepareStatement("SELECT * FROM dbo.wifi_info WHERE AGENCY LIKE ?");

                        String queryAgency = "%" + searchCondition.getAgency() + "%";
                        preStmt0.setString(1, queryAgency); // 設定主管機關
                    }

                    if (preStmt0 != null) {
                        /* 執行 */
                        ResultSet rs0 = preStmt0.executeQuery();
                        /* 清空變數 */
                        preStmt0.clearParameters();

                        while (rs0.next()) {
                            Wifi_Info wifiLocationData = new Wifi_Info("", "", 0, "", "", new BigDecimal("0"),
                                    new BigDecimal("0"), "", 0);

                            wifiLocationData.setSiteId(rs0.getString("SITE_ID"));
                            wifiLocationData.setName(rs0.getString("NAME"));
                            wifiLocationData.setAreaCode(rs0.getInt("AREACODE"));
                            wifiLocationData.setAreaName(rs0.getString("AREANAME"));
                            wifiLocationData.setAddress(rs0.getString("ADDR"));
                            wifiLocationData.setLongitude(rs0.getBigDecimal("LONGITUDE"));
                            wifiLocationData.setLatitude(rs0.getBigDecimal("LATITUDE"));
                            wifiLocationData.setAgency(rs0.getString("AGENCY"));
                            wifiLocationData.setVersion(rs0.getInt("VERSION"));

                            wifiLocationList.add(wifiLocationData);
                        }
                        /* commit transaction */
                        connection0.commit();

                        sqlSelectResult.setSuccess(true);
                        sqlSelectResult.setSqlData(wifiLocationList);
                        preStmt0.close();
                    } else {
                        /* rollback transaction */
                        connection0.rollback();

                        sqlSelectResult.setSuccess(false);
                        sqlSelectResult.setContents("遇到錯誤：原因不明的錯誤");
                    }
                } catch (SQLException sqlE) {
                    /* rollback transaction */
                    connection0.rollback();

                    sqlSelectResult.setSuccess(false);
                    sqlSelectResult.setContents("遇到錯誤：" + System.lineSeparator() + sqlE.toString());
                } catch (Exception e) {
                    /* rollback transaction */
                    connection0.rollback();

                    sqlSelectResult.setSuccess(false);
                    sqlSelectResult.setContents("遇到錯誤：" + System.lineSeparator() + e.toString());
                }
            }
        } catch (SQLException sqlE0) {
            sqlSelectResult.setSuccess(false);
            sqlSelectResult.setContents("遇到錯誤：" + System.lineSeparator() + sqlE0.toString());
        } catch (Exception e0) {
            sqlSelectResult.setSuccess(false);
            sqlSelectResult.setContents("遇到錯誤：" + System.lineSeparator() + e0.toString());
        }
        return sqlSelectResult;
    }
}
