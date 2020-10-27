package project;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileJSON {
    /* 檔案路徑 */
    private static final File downloadJson = new File("ref/Public_Wifi_Info_List.json");

    /**
     * 取得檔案資訊
     *
     * @return Json檔案的相對路徑(File)
     */
    /* 取得下載目錄下的json檔案資訊 */
    protected static File getDownloadJson() {
        return downloadJson;
    }

    /**
     * 取得檔案絕對路徑
     *
     * @return Json檔案的絕對路徑(String)
     */
    /* 取得下載目錄下的json檔案絕對路徑 */
    protected static String getDownloadJsonPath() {
        return downloadJson.getAbsolutePath();
    }

    /**
     * 檢查下載目錄下的json是否可寫入
     *
     * @return True代表檔案可以被寫入
     */
    /* 檢查下載目錄下的json是否可寫入 */
    protected static boolean checkDownloadJsonWrite() {
        return FileJSON.getDownloadJson().canWrite();
    }

    /**
     * 檢查下載目錄下的json是否可讀取+
     *
     * @return True代表檔案可以被讀取
     */
    /* 檢查下載目錄下的json是否可讀取 */
    protected static boolean checkDownloadJsonRead() {
        return FileJSON.getDownloadJson().canRead();
    }

    /**
     * 讀取下載目錄下的json
     *
     * @return 第一項為讀取的結果(Success代表成功、Fail代表失敗)、第二項為補充資訊(如觸發例外時的相關資訊)、第三項為json檔案中所提取的List
     */
    /* 讀取下載目錄下的json */
    protected static ReadJsonOriginalDataResult readDownloadJson() {
        ReadJsonOriginalDataResult wifiInfoJsonResult = new ReadJsonOriginalDataResult();
        /* 實際上存資料的陣列 */
        List<Wifi_Info> wifiInfoJsonList = new ArrayList<>();
        /* 建立Gson物件 */
        Gson gson0 = new Gson();
        /* 設定檔案位置 */
        FileInputStream fis1 = null;
        /* 設定編碼 */
        InputStreamReader isr1 = null;

        /* 確定檔案可讀取 */
        if (checkDownloadJsonRead()) {
            try {
                fis1 = new FileInputStream(downloadJson);
                isr1 = new InputStreamReader(fis1, StandardCharsets.UTF_8);

                int count;
                StringBuilder sb0 = new StringBuilder();

                while ((count = isr1.read()) != -1) {
                    char inputChar = (char) count;
                    sb0.append(inputChar);
                }

                /* 用jsonString接 */
                String jsonDataString = sb0.toString();

                /* 宣告存放資料的陣列，來接反序列化後的資料 */
                List<Wifi_Info_Json> wifiDataJsonList = gson0.fromJson(jsonDataString,
                        new TypeToken<List<Wifi_Info_Json>>() {
                        }.getType());

                for (Wifi_Info_Json wifiJsonItem : wifiDataJsonList) {
                    Wifi_Info wifiJsonData = new Wifi_Info("", "", 0, "", "", new BigDecimal("0"), new BigDecimal("0"),
                            "", 0);

                    /* 取站點代碼 */
                    wifiJsonData.setSiteId(wifiJsonItem.getSITE_ID());
                    /* 取名稱 */
                    wifiJsonData.setName(wifiJsonItem.getNAME());
                    /* 取郵遞區號 */
                    wifiJsonData.setAreaCode(Integer.parseInt(wifiJsonItem.getADDR().substring(0, 3)));
                    /* 拆地址 */
                    if (wifiJsonItem.getADDR().charAt(7) == '區') {
                        /* 取行政區 */
                        wifiJsonData.setAreaName(wifiJsonItem.getADDR().substring(3, 8));
                        /* 剩餘的地址資訊 */
                        wifiJsonData.setAddress(wifiJsonItem.getADDR().substring(8));
                    } else {
                        /* 取行政區 */
                        wifiJsonData.setAreaName(wifiJsonItem.getADDR().substring(3, 9));
                        /* 剩餘的地址資訊 */
                        wifiJsonData.setAddress(wifiJsonItem.getADDR().substring(9));
                    }
                    /* 取經度 */
                    wifiJsonData.setLongitude(wifiJsonItem.getLONGITUDE());
                    /* 取緯度 */
                    wifiJsonData.setLatitude(wifiJsonItem.getLATITUDE());
                    /* 取主管單位 */
                    wifiJsonData.setAgency(wifiJsonItem.getAGENCY());

                    wifiInfoJsonList.add(wifiJsonData);
                }
                wifiInfoJsonResult.setResult("Success");
                wifiInfoJsonResult.setWifiDataList(wifiInfoJsonList);
            } catch (IOException ioE) {
                wifiInfoJsonResult.setResult("Fail");
                wifiInfoJsonResult.setContents("遇到錯誤：" + System.lineSeparator() + ioE.toString());
            } finally {
                try {
                    if (fis1 != null) {
                        fis1.close();
                    }
                    if (isr1 != null) {
                        isr1.close();
                    }
                } catch (IOException ioE) {
                    System.out.println("遇到錯誤：" + System.lineSeparator() + ioE.toString());
                }
            }
        }
        /* 不可讀取 */
        else {
            wifiInfoJsonResult.setResult("Fail");
            wifiInfoJsonResult.setContents("遇到錯誤，無法讀取檔案");
        }

        return wifiInfoJsonResult;
    }

    /**
     * 將對SQL的操作產生JSON檔
     *
     * @param createFileTime 執行產生檔案時的時間(String)
     * @param mode           1->更新，2->修改，3->刪除，4->查詢(int)
     * @param PKList         受影響的主鍵列表(List<String>)
     * @param wifiInfoList   讀取自json的資料陣列
     *
     * @return 第一項為結果(Success/Fail)，第二項為發生例外時的錯誤訊息(String)，第三項後為所建立檔案的所在位置，第四項為檔名
     */
    /* 將對SQL的操作產生JSON檔 */
    protected static List<String> createSQLJSON(String createFileTime, int mode, List<String> PKList,
            List<Wifi_Info> wifiInfoList) {
        List<String> createSQLJsonResult = new ArrayList<>();
        /* 設定檔案名稱 */
        String jsonFileName1 = "";
        File jsonFile1;
        /* 建立Gson物件 */
        Gson gson0 = new Gson();
        /* 宣告輸出用的陣列 */
        List<Wifi_Info> wifiOutputList = new ArrayList<>();
        /* 宣告要使用的jsonString */
        String jsonDataString;

        if (mode == 1) {
            jsonFileName1 = "insertedResult_JSON_UTF8_" + createFileTime + ".json";
        } else if (mode == 2) {
            jsonFileName1 = "updatedResult_JSON_UTF8_" + createFileTime + ".json";
        } else if (mode == 3) {
            jsonFileName1 = "deletedResult_JSON_UTF8_" + createFileTime + ".json";
        } else if (mode == 4) {
            jsonFileName1 = "searchedResult_JSON_UTF8_" + createFileTime + ".json";
        }

        FileOutputStream fos1;
        try {
            /* 設定檔案位置 */
            if (jsonFileName1.equals("")) {
                createSQLJsonResult.add("Fail");
                createSQLJsonResult.add("遇到錯誤：" + System.lineSeparator() + "無法產生檔案名稱");
            } else {
                /* 設定檔案位置 */
                jsonFile1 = new File(jsonFileName1);
                /* 設定FileOutputStream */
                fos1 = new FileOutputStream(jsonFile1);
                /* 設定編碼 */
                OutputStreamWriter osw1 = new OutputStreamWriter(fos1, StandardCharsets.UTF_8);

                if(mode == 2 || mode == 4) {
                    /* 產生json字串 */
                    jsonDataString = gson0.toJson(wifiInfoList);
                    
                } else {
                    for (Wifi_Info wifiData : wifiInfoList) {
                        if (PKList.contains(wifiData.getSiteId())) {
                            Wifi_Info wifiOutputData = wifiData; 
                            wifiOutputList.add(wifiOutputData);
                        }
                    }
                    /* 產生json字串 */
                    jsonDataString = gson0.toJson(wifiOutputList);
                }

                /* 寫入檔案 */
                osw1.write(jsonDataString);
                /* 刷新 */
                osw1.flush();
                /* 關閉資源 */
                osw1.close();
                fos1.close();

                createSQLJsonResult.add("Success");
                createSQLJsonResult.add("");
                createSQLJsonResult.add("檔案已儲存於：" + System.lineSeparator() + jsonFile1.getAbsolutePath());
                createSQLJsonResult.add(jsonFileName1);
            }
        } catch (IOException ioE) {
            createSQLJsonResult.add("Fail");
            createSQLJsonResult.add("遇到錯誤：" + System.lineSeparator() + ioE.toString());
        } catch (Exception e) {
            createSQLJsonResult.add("Fail");
            createSQLJsonResult.add("遇到錯誤：" + System.lineSeparator() + e.toString());
        }

        return createSQLJsonResult;
    }

    /**
     * 讀取程式所產生的JSON檔
     *
     * @param mode           1->更新，2->修改，3->刪除，4->查詢(int)
     * @param createFileTime 執行產生檔案時的時間(String)
     *
     * @return 第一項為結果(boolean)，第二項為發生例外時的錯誤訊息(String)，第三項為資料陣列(自定義類)
     */
    /* 讀取程式所產生的JSON檔 */
    protected static QueryResult readSQLJSON(int mode, String createFileTime) {
        QueryResult readJsonResult = new QueryResult();

        /* 設定檔案名稱 */
        String jsonFileName1 = "";
        File jsonFile1;
        /* 建立Gson物件 */
        Gson gson0 = new Gson();
        /* 宣告輸出用的陣列 */
        List<Wifi_Info> jsonWifiDataList = new ArrayList<>();

        if (mode == 1) {
            jsonFileName1 = "insertedResult_JSON_UTF8_" + createFileTime + ".json";
        } else if (mode == 2) {
            jsonFileName1 = "updatedResult_JSON_UTF8_" + createFileTime + ".json";
        } else if (mode == 3) {
            jsonFileName1 = "deletedResult_JSON_UTF8_" + createFileTime + ".json";
        } else if (mode == 4) {
            jsonFileName1 = "searchedResult_JSON_UTF8_" + createFileTime + ".json";
        }

        FileInputStream fis1;
        InputStreamReader isr1;

        try {
            jsonFile1 = new File(jsonFileName1);
            fis1 = new FileInputStream(jsonFile1);
            isr1 = new InputStreamReader(fis1, StandardCharsets.UTF_8);

            int count;
            StringBuilder sb0 = new StringBuilder();

            while ((count = isr1.read()) != -1) {
                char inputChar = (char) count;
                sb0.append(inputChar);
            }

            /* 用jsonString接 */
            String jsonDataString = sb0.toString();
            /* 接反序列化後的資料 */
            jsonWifiDataList = gson0.fromJson(jsonDataString, new TypeToken<List<Wifi_Info>>() {
            }.getType());

            /* 關閉資源 */
            isr1.close();
            fis1.close();

            readJsonResult.setSuccess(true);
            readJsonResult.setSqlData(jsonWifiDataList);
        } catch (IOException ioE) {
            readJsonResult.setSuccess(false);
            readJsonResult.setContents("遇到錯誤：" + System.lineSeparator() + ioE.toString());
        } catch (Exception e) {
            readJsonResult.setSuccess(false);
            readJsonResult.setContents("遇到錯誤：" + System.lineSeparator() + e.toString());
        }

        return readJsonResult;
    }
}
