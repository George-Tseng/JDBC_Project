package project;

import java.util.List;

public interface WifiInfoDAO {

    /**
     * 查詢資料
     *
     * @return 第一項為boolean，代表成功/失敗，第二項為失敗時的訊息說明(String)，第三項為成功回傳時的資料陣列
     */
    /* 查詢全部資料 */
    QueryResult queryDBData(List<String> dbInfo);

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
    List<String> insertDBData(List<String> insertPKList, List<Wifi_Info> wifiInfoList, List<String> dbInfo);

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
    public QueryResult updateData(List<String> dbInfo, List<Wifi_Info> dbDataList, Wifi_Info updateInfo);

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
    List<String> deleteDBData(List<String> deletedPKList, List<Wifi_Info> dbDataList, List<String> dbInfo);

    /**
     * 查詢資料
     *
     * @param dbInfo          DB資訊(List<String>)
     * @param searchCondition 查詢條件(自定義類)
     *
     * @return 第一項為boolean，代表成功/失敗，第二項為失敗時的訊息說明(String)，第三項為成功回傳時的資料陣列
     */
    /* 查詢資料 */
    QueryResult selectDBData(List<String> dbInfo, Wifi_Info searchCondition);
}
