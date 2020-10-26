package project;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileCSV {

    /*固定暫存檔的路徑*/
    private final static File tmpF = new File("timeRecTmp.csv");
    private final static File tmpF2 = new File("timeRecTmp2.csv");
    
    /**
     * 將對SQL的操作產生CSV檔
     *
     * @param createFileTime 執行產生檔案時的時間(String)
     * @param mode           1->更新，2->修改，3->刪除，4->查詢(int)
     * @param PKList         受影響的主鍵列表(List<String>)
     * @param wifiInfoList   讀取自json的資料陣列
     *
     * @return 第一項為結果(Success/Fail)，第二項為發生例外時的錯誤訊息(String)，第三項後為所建立檔案的所在位置，第四項為檔名
     */
    /* 將對SQL的操作產生CSV檔 */
    protected static List<String> createSQLCSV(String createFileTime, int mode, List<String> PKList,
            List<Wifi_Info> wifiInfoList) {
        List<String> createSQLCsvResult = new ArrayList<>();
        /* 設定檔案名稱 */
        String csvFileName1 = "";
        String csvFileName2 = "";
        File csvFile1;
        File csvFile2;

        if (mode == 1) {
            csvFileName1 = "insertedResult_CSV_UTF8_" + createFileTime + ".csv";
            csvFileName2 = "insertedResult_CSV_MS950_" + createFileTime + ".csv";
        } else if (mode == 2) {
            csvFileName1 = "updatedResult_CSV_UTF8_" + createFileTime + ".csv";
            csvFileName2 = "updatedResult_CSV_MS950_" + createFileTime + ".csv";
        } else if (mode == 3) {
            csvFileName1 = "deletedResult_CSV_UTF8_" + createFileTime + ".csv";
            csvFileName2 = "deletedResult_CSV_MS950_" + createFileTime + ".csv";
        } else if (mode == 4) {
            csvFileName1 = "searchedResult_CSV_UTF8_" + createFileTime + ".csv";
            csvFileName2 = "searchedResult_CSV_MS950_" + createFileTime + ".csv";
        }

        FileOutputStream fos1;
        FileOutputStream fos2;
        try {
            /* 設定檔案位置 */
            if (csvFileName1.equals("") | csvFileName2.equals("")) {
                createSQLCsvResult.add("Fail");
                createSQLCsvResult.add("遇到錯誤：" + System.lineSeparator() + "無法產生檔案名稱");
            } else {
                csvFile1 = new File(csvFileName1);
                csvFile2 = new File(csvFileName2);
                /* 設定FileOutputStream */
                fos1 = new FileOutputStream(csvFile1);
                fos2 = new FileOutputStream(csvFile2);
                /* 設定編碼 */
                OutputStreamWriter osw1 = new OutputStreamWriter(fos1, StandardCharsets.UTF_8);
                OutputStreamWriter osw2 = new OutputStreamWriter(fos2, "MS950");
                /* 設定csv欄位 */
                CSVPrinter csvP1 = new CSVPrinter(osw1, CSVFormat.EXCEL.withHeader("siteId", "name", "areaCode",
                        "areaName", "address", "longitude", "latitude", "agency", "version"));
                CSVPrinter csvP2 = new CSVPrinter(osw2, CSVFormat.EXCEL.withHeader("siteId", "name", "areaCode",
                        "areaName", "address", "longitude", "latitude", "agency", "version"));
                /* 寫入資料 */
                if (mode == 1 || mode == 3) {
                    for (Wifi_Info wifiData : wifiInfoList) {
                        if (PKList.contains(wifiData.getSiteId())) {
                            csvP1.printRecord(wifiData.getSiteId(), wifiData.getName(), wifiData.getAreaCode(),
                                    wifiData.getAreaName(), wifiData.getAddress(), wifiData.getLongitude(),
                                    wifiData.getLatitude(), wifiData.getAgency(), wifiData.getVersion());
                            csvP2.printRecord(wifiData.getSiteId(), wifiData.getName(), wifiData.getAreaCode(),
                                    wifiData.getAreaName(), wifiData.getAddress(), wifiData.getLongitude(),
                                    wifiData.getLatitude(), wifiData.getAgency(), wifiData.getVersion());
                        }
                    }
                } else {
                    for (Wifi_Info wifiData : wifiInfoList) {
                        csvP1.printRecord(wifiData.getSiteId(), wifiData.getName(), wifiData.getAreaCode(),
                                wifiData.getAreaName(), wifiData.getAddress(), wifiData.getLongitude(),
                                wifiData.getLatitude(), wifiData.getAgency(), wifiData.getVersion());
                        csvP2.printRecord(wifiData.getSiteId(), wifiData.getName(), wifiData.getAreaCode(),
                                wifiData.getAreaName(), wifiData.getAddress(), wifiData.getLongitude(),
                                wifiData.getLatitude(), wifiData.getAgency(), wifiData.getVersion());

                    }
                }

                /* 刷新 */
                csvP1.flush();
                csvP2.flush();
                /* 關閉資源 */
                csvP1.close();
                csvP2.close();
                osw1.close();
                osw2.close();
                fos1.close();
                fos2.close();

                createSQLCsvResult.add("Success");
                createSQLCsvResult.add("");
                createSQLCsvResult.add("檔案已儲存於：" + System.lineSeparator() + csvFile1.getAbsolutePath()
                        + System.lineSeparator() + csvFile2.getAbsolutePath());
                createSQLCsvResult.add(csvFileName1 + System.lineSeparator() + csvFileName2);

            }
        } catch (IOException ioE) {
            createSQLCsvResult.add("Fail");
            createSQLCsvResult.add("遇到錯誤：" + System.lineSeparator() + ioE.toString());
        } catch (Exception e) {
            createSQLCsvResult.add("Fail");
            createSQLCsvResult.add("遇到錯誤：" + System.lineSeparator() + e.toString());
        }

        return createSQLCsvResult;
    }

    /**
     * 讀取程式所產生的CSV檔
     *
     * @param mode           1->更新，2->修改，3->刪除，4->查詢(int)
     * @param codec          1->UTF8，2->MS950
     * @param createFileTime 執行產生檔案時的時間(String)
     *
     * @return 第一項為結果(boolean)，第二項為發生例外時的錯誤訊息(String)，第三項為資料陣列(自定義類)
     */
    /* 讀取程式所產生的CSV檔 */
    protected static QueryResult readSQLCSV(int mode, int codec, String createFileTime) {
        QueryResult readCsvResult = new QueryResult();

        /* 設定檔案名稱 */
        String csvFileName1 = "";

        File csvFile1;

        if (mode == 1) {
            csvFileName1 = "insertedResult_CSV_UTF8_" + createFileTime + ".csv";
        } else if (mode == 2) {
            csvFileName1 = "updatedResult_CSV_UTF8_" + createFileTime + ".csv";
        } else if (mode == 3) {
            csvFileName1 = "deletedResult_CSV_UTF8_" + createFileTime + ".csv";
        } else if (mode == 4) {
            csvFileName1 = "searchedResult_CSV_UTF8_" + createFileTime + ".csv";
        }

        FileInputStream fis1;
        try {
            /* 設定檔案位置 */
            csvFile1 = new File(csvFileName1);
            /* 設定FileInputStream */
            fis1 = new FileInputStream(csvFile1);
            /* 設定編碼 */
            InputStreamReader isr1 = (codec == 1) ? new InputStreamReader(fis1, StandardCharsets.UTF_8)
                    : new InputStreamReader(fis1, "MS950");
            /* 使用CVSParser讀取資料 */
            CSVParser csvP1 = CSVFormat.EXCEL.withHeader().parse(isr1);
            List<CSVRecord> csvData = csvP1.getRecords();
            /* 宣告準備回傳的資料陣列 */
            List<Wifi_Info> csvWifiDataList = new ArrayList<>();

            /* 處理資料 */
            for (CSVRecord csvR1 : csvData) {
                Wifi_Info csvWifiData = new Wifi_Info("", "", 0, "", "", new BigDecimal("0"), new BigDecimal("0"), "",
                        0);

                csvWifiData.setSiteId(csvR1.get("siteId"));
                csvWifiData.setName(csvR1.get("name"));
                csvWifiData.setAreaCode(Integer.parseInt(csvR1.get("areaCode")));
                csvWifiData.setAreaName(csvR1.get("areaName"));
                csvWifiData.setAddress(csvR1.get("address"));
                csvWifiData.setLongitude(new BigDecimal(csvR1.get("longitude")));
                csvWifiData.setLatitude(new BigDecimal(csvR1.get("latitude")));
                csvWifiData.setAgency(csvR1.get("agency"));
                csvWifiData.setVersion(Integer.parseInt(csvR1.get("version")));

                csvWifiDataList.add(csvWifiData);
            }

            /* 關閉資源 */
            csvP1.close();
            isr1.close();
            fis1.close();

            readCsvResult.setSuccess(true);
            readCsvResult.setSqlData(csvWifiDataList);
        } catch (IOException ioE) {
            readCsvResult.setSuccess(false);
            readCsvResult.setContents("遇到錯誤：" + System.lineSeparator() + ioE.toString());
        } catch (Exception e) {
            readCsvResult.setSuccess(false);
            readCsvResult.setContents("遇到錯誤：" + System.lineSeparator() + e.toString());
        }

        return readCsvResult;
    }

    /**
     * 建立暫存檔
     * 
     * @return 成功回傳空字串，失敗回傳錯誤訊息
     */
    /*建立暫存檔*/
    protected static String createTmpFile(){
        String status = "";
        try{
            if(!tmpF.createNewFile()){
                status = "建立失敗";
            }
        } catch(IOException ioE){
            status = "遇到錯誤：" + System.lineSeparator() + ioE.toString();
        }
        return status;
    }
    
    /**
     * 建立暫存檔
     * 
     * @return 成功回傳空字串，失敗回傳錯誤訊息
     */
    /*建立暫存檔*/
    protected static String createTmpFile2(){
        String status = "";
        try{
            if(!tmpF2.createNewFile()){
                status = "建立失敗";
            }
        } catch(IOException ioE){
            status = "遇到錯誤：" + System.lineSeparator() + ioE.toString();
        }
        return status;
    }

    /**
     * 寫入暫存檔
     * 
     * @param timeNow 當前時間
     * 
     * @return 成功回傳空字串，失敗回傳錯誤訊息
     */
    /*寫入暫存檔*/
    protected static String writeTmpFile(String timeNow){
        String status = "";

        FileOutputStream fos0;
        OutputStreamWriter osw0;

        try{
            fos0 = new FileOutputStream(tmpF);
            osw0 = new OutputStreamWriter(fos0, StandardCharsets.UTF_8);

            osw0.write(timeNow);
            osw0.flush();
            fos0.close();
            osw0.close();
        } catch(IOException ioE){
            status = "遇到錯誤：" + System.lineSeparator() + ioE.toString();
        }
        return status;
    }
    
    /**
     * 寫入暫存檔
     * 
     * @param timeNow 當前時間
     * 
     * @return 成功回傳空字串，失敗回傳錯誤訊息
     */
    /*寫入暫存檔*/
    protected static String writeTmpFile2(String timeNow){
        String status = "";

        FileOutputStream fos0;
        OutputStreamWriter osw0;

        try{
            fos0 = new FileOutputStream(tmpF2);
            osw0 = new OutputStreamWriter(fos0, StandardCharsets.UTF_8);

            osw0.write(timeNow);
            osw0.flush();
            fos0.close();
            osw0.close();
        } catch(IOException ioE){
            status = "遇到錯誤：" + System.lineSeparator() + ioE.toString();
        }
        return status;
    }

    /**
     * 讀取暫存檔
     * 
     * @return 第一項為成功/失敗(String)，第二項為錯誤訊息(String)，第三項為成功時讀取到的檔案內容(String)
     */
    /*讀取暫存檔*/
    protected static List<String> readTmpFile(){
        FileInputStream fis0;
        InputStreamReader isr0;
        int count;
        List<String> result = new ArrayList<>();

        try{
            fis0 = new FileInputStream(tmpF);
            isr0 = new InputStreamReader(fis0, StandardCharsets.UTF_8);
            StringBuilder sb0 = new StringBuilder();

            while((count = isr0.read()) != -1){
                char inputChar = (char)count;
                sb0.append(inputChar);
            }

            result.add("Success");
            result.add("");
            result.add(sb0.toString());
            fis0.close();
            isr0.close();
        } catch(IOException ioE){
            result.add("Fail");
            result.add("遇到錯誤：" + System.lineSeparator() + ioE.toString());
        }
        return result;
    }
    
    /**
     * 讀取暫存檔
     * 
     * @return 第一項為成功/失敗(String)，第二項為錯誤訊息(String)，第三項為成功時讀取到的檔案內容(String)
     */
    /*讀取暫存檔*/
    protected static List<String> readTmpFile2(){
        FileInputStream fis0;
        InputStreamReader isr0;
        int count;
        List<String> result = new ArrayList<>();

        try{
            fis0 = new FileInputStream(tmpF2);
            isr0 = new InputStreamReader(fis0, StandardCharsets.UTF_8);
            StringBuilder sb0 = new StringBuilder();

            while((count = isr0.read()) != -1){
                char inputChar = (char)count;
                sb0.append(inputChar);
            }

            result.add("Success");
            result.add("");
            result.add(sb0.toString());
            fis0.close();
            isr0.close();
        } catch(IOException ioE){
            result.add("Fail");
            result.add("遇到錯誤：" + System.lineSeparator() + ioE.toString());
        }
        return result;
    }

    /**
     * 程式結束時刪除暫存檔
     * 
     * @return 成功時才回傳空字串
     */
    /*程式結束時刪除暫存檔*/
    protected static String deleteTmpFile(){
        String status = "";
        try{
            tmpF.deleteOnExit();
        } catch(SecurityException sE){
            status = "遇到錯誤：" + System.lineSeparator() + sE.toString();
        }
        return status;
    }
    
    /**
     * 程式結束時刪除暫存檔
     * 
     * @return 成功時才回傳空字串
     */
    /*程式結束時刪除暫存檔*/
    protected static String deleteTmpFile2(){
        String status = "";
        try{
            tmpF2.deleteOnExit();
        } catch(SecurityException sE){
            status = "遇到錯誤：" + System.lineSeparator() + sE.toString();
        }
        return status;
    }

    /**
     * 立刻刪除暫存檔
     * 
     * @return 成功時才回傳空字串
     */
    /*立刻刪除暫存檔*/
    protected static String deleteTmpFileNow(){
        String status = "";
        try {
            if(!tmpF.delete()){
                status = "清除失敗";
            }
        } catch(SecurityException sE){
            status = "遇到錯誤：" + System.lineSeparator() + sE.toString();
        }
        return status;
    }
    
    /**
     * 立刻刪除暫存檔
     * 
     * @return 成功時才回傳空字串
     */
    /*立刻刪除暫存檔*/
    protected static String deleteTmpFile2Now(){
        String status = "";
        try {
            if(!tmpF2.delete()){
                status = "清除失敗";
            }
        } catch(SecurityException sE){
            status = "遇到錯誤：" + System.lineSeparator() + sE.toString();
        }
        return status;
    }

    /**
     * 確認暫存檔是否存在
     * 
     * @return 存在回傳True
     */
    /*確認暫存檔是否存在*/
    protected static boolean checkTmpFile(){
        return tmpF.exists();
    }
    
    /**
     * 確認暫存檔是否存在
     * 
     * @return 存在回傳True
     */
    /*確認暫存檔是否存在*/
    protected static boolean checkTmpFile2(){
        return tmpF2.exists();
    }
}
