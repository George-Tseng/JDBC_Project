package project;

//IN CMD
import java.io.Console;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;

public class JDBCProject_CMD {
    public static void main(String[] args) {
        /* boolean gate */
        boolean hasUserDir = false;
        boolean isUserDir = false;
        boolean createUser = false;
        boolean loginPass = false;
        boolean startSQL = false;
        boolean leave = false;
        /* 宣告Timer */ 
        Timer tr0 = new Timer();

        /* DB上的資料數 */
        int dataCount;

        /* DB上取得的資料陣列 */
        List<Wifi_Info> DBDataList;
        /* DB上取得的PK列表 */
        List<String> PKList = new ArrayList<>();
        /* Json讀取轉換後的資料陣列 */
        List<Wifi_Info> wifiInfoList = new ArrayList<>();
        /* Json資料中的PK列表 */
        List<String> jsonPKList = new ArrayList<>();
        /* SQL操作暫存的PK列表 */
        List<String> tmpPKList = new ArrayList<>();

        /* 宣告Scanner */
        // In IDE
        // Scanner scan = new Scanner(System.in);
        // In CMD
        Console cons = System.console();
        Scanner scan = new Scanner(System.in, "UTF-8");

        /*暫存檔存在*/
        if(FileCSV.checkTmpFile2()) {
            System.out.println("程式上次使用時似乎並未正常地被關閉...");
            /*讀取暫存檔中的紀錄*/
            List<String> tmpTimeLog = FileCSV.readTmpFile2();
            if(tmpTimeLog.get(0).equals("Success")) {
                System.out.println("最後運作時間：" + tmpTimeLog.get(2));
            }
            else {
                System.out.println("無法讀取紀錄！" + tmpTimeLog.get(1));
            }

            /*嘗試立即刪除暫存檔*/
            if(!FileCSV.deleteTmpFile2Now().equals("")){
                System.out.println("上次執行時的暫存檔清除失敗...");
                System.out.println(FileCSV.deleteTmpFile2Now());
            }
        }
        
        /*嘗試建立暫存檔*/
        if(!FileCSV.createTmpFile2().equals("")) {
            System.out.println(FileCSV.createTmpFile2());
        }
        /*成功才執行寫入方法，立即執行第一次，之後每過一毫秒執行一次*/
        else{
            tr0.schedule(new File_Timer2(), 0, 1);
        }

        /*設定JVM關閉後自動刪除暫存檔*/
        if(!FileCSV.deleteTmpFile2().equals("")) {
            System.out.println(FileCSV.deleteTmpFile2());
        }

        while (true) {
            System.out.println();
            System.out.println("歡迎使用本程式" + System.lineSeparator());
            /* 檢查使用者目錄是否存在 */
            if (!Password.checkUserDir()) {
                /* 不存在就建立一個 */
                List<String> createResult = Password.createUserDir();
                if (createResult.get(0).equals("Success")) {
                    hasUserDir = true;
                }
                System.out.println(createResult.get(1));
            } else {
                hasUserDir = true;
                /* 檢查是否為目錄 */
                if (Password.checkUserIsDir()) {
                    isUserDir = true;
                    /* 檢查目錄中的檔案數量 */
                    if (Password.getUserFilesNum() == 0) {
                        createUser = true;
                    }
                } else {
                    System.out.println("使用者目錄指向的位置並非目錄！請退出程式將其刪除後再次開啟本程式");
                }
            }

            /* 建立使用者檔案 */
            if (createUser) {
                System.out.println("開始建立使用者檔案...");
                /* username&password */

                List<String> userInfoList = UserInfoCheck.checkUserInfo(scan, cons);
                if(userInfoList.size() == 6) {
                    String[] userInfo = new String[6];
                    for(int i = 0; i < userInfoList.size(); i++) {
                        userInfo[i] = userInfoList.get(i);
                    }
//                    String[] userInfo = { "scott", "tiger", "George", "SQL017u109George",
//                            "com.microsoft.sqlserver.jdbc.SQLServerDriver",
//                           "jdbc:sqlserver://localhost:1433;databaseName=JDBCProject" };
                    /* timeStamp */
                    String nowTime = TimeNow.getDateNow();
                    /* macAddress */
                    String macAddress = MacAddress.getMacAddress();
                    if (macAddress.equals("NotFound")) {
                        System.out.println("建立使用者檔案失敗，請檢查網路連線...");
                    } else {
                        String encodeTime = Translator.getMessageASCII(nowTime);
                        String encodeMacAddress = Translator.getMessageASCII(macAddress);
                        String encodeUser = KeyCodec.getEncodeKeyLong(encodeTime, encodeMacAddress, userInfo[0]);
                        String encodePassword = KeyCodec.getEncodeKeyLong(encodeTime, encodeMacAddress, userInfo[1]);
                        String encodeDBUser = KeyCodec.getEncodeKeyLong(encodeTime, encodeMacAddress, userInfo[2]);
                        String encodeDBPassword = KeyCodec.getEncodeKeyLong(encodeTime, encodeMacAddress, userInfo[3]);
                        String encodeDBDriverClassName = KeyCodec.getEncodeKeyLong(encodeTime, encodeMacAddress,
                                userInfo[4]);
                        String encodeDBUrl = KeyCodec.getEncodeKeyLong(encodeTime, encodeMacAddress, userInfo[5]);

                        List<String> encodeMessage = new ArrayList<>();
                        encodeMessage.add(encodeTime);
                        encodeMessage.add(encodeMacAddress);
                        encodeMessage.add(encodeUser);
                        encodeMessage.add(encodePassword);
                        encodeMessage.add(encodeDBUser);
                        encodeMessage.add(encodeDBPassword);
                        encodeMessage.add(encodeDBDriverClassName);
                        encodeMessage.add(encodeDBUrl);

                        List<String> createUserConfResult = FileXML.createUserConfXML(encodeMessage);
                        System.out.println(createUserConfResult.get(1));
                        createUser = false;
                        if (createUserConfResult.get(0).equals("Fail")) {
                            System.out.println("程式無法繼續..." + System.lineSeparator());
                            break;
                        }
                    }
                } else {
                    System.out.println("建立使用者檔案失敗...");
                }
            }

            /* 準備驗證 */
            if (!loginPass && (hasUserDir && isUserDir && Password.getUserFilesNum() > 0)) {
                /* 載入XML */
                ReadXMLDataResult xmlUserConfResult = FileXML.readUserConfXML();
                if (xmlUserConfResult.getResult().equals("Fail")) {
                    System.out.println(xmlUserConfResult.getContents());
                    System.out.println("程式無法繼續，請關閉程式後檢查相關檔案，或刪除後再次啟動，由程式重新建立");
                    break;
                } else {
                    List<String> encodeUserInfo = xmlUserConfResult.getEncodeMessage();
                    while (true) {
                        System.out.println("載入設定檔完成..." + System.lineSeparator() + "請輸入使用者帳戶:");
                        String inputUser = scan.nextLine();
                        if (!inputUser.equals("")
                                && KeyCodec.getEncodeKeyLong(encodeUserInfo.get(0), encodeUserInfo.get(1), inputUser)
                                        .equals(encodeUserInfo.get(2))) {
                            // IN IDE
                            // System.out.println("請輸入使用者密碼(預設為tiger):");
                            // String inputPassword = scan.nextLine();
                            // In CMD
                            char[] inputPasswd = cons.readPassword("請輸入使用者密碼:");
                            String inputPassword = new String(inputPasswd);
                            if (!inputPassword.equals("") && KeyCodec
                                    .getEncodeKeyLong(encodeUserInfo.get(0), encodeUserInfo.get(1), inputPassword)
                                    .equals(encodeUserInfo.get(3))) {
                                loginPass = true;
                                break;
                            } else {
                                System.out.println("無效的使用者密碼，請再輸入一次");
                            }
                        } else {
                            System.out.println("無效的使用者名稱，請再輸入一次");
                        }
                    }

                }
            }

            /* 登入 */
            if (loginPass) {
                System.out.println("登入成功！現在時間為" + TimeNow.getDateNow());

                /* 檢查資料來源的json檔案是否存在 */
                if (!FileDownload.checkDownloadJson()) {
                    /* 嘗試下載json檔案 */
                    List<String> downloadJsonResult = FileDownload.downloadJson();
                    System.out.println(downloadJsonResult.get(1));
                    /* 無法成功下載時 */
                    if (downloadJsonResult.get(0).equals("Fail")) {
                        System.out.println("程式無法繼續，請關閉程式後檢查相關設定與檔案是否正常");
                        break;
                    }
                    else {
                        System.out.println(System.lineSeparator() + "所下載的檔案相關驗證資訊為：");
                        List<String> downloadJsonCRC32ResultSet = FileHashValue.getFileHashValue(FileJSON.getDownloadJson(), 1);
                        if (downloadJsonCRC32ResultSet.get(0).equals("Success")) {
                            System.out.println("CRC32：" + downloadJsonCRC32ResultSet.get(2));
                        } else {
                            System.out.println(downloadJsonCRC32ResultSet.get(1));
                        }
                        List<String> downloadJsonMD5ResultSet = FileHashValue.getFileHashValue(FileJSON.getDownloadJson(), 2);
                        if (downloadJsonMD5ResultSet.get(0).equals("Success")) {
                            System.out.println("MD5：" + downloadJsonMD5ResultSet.get(2));
                        } else {
                            System.out.println(downloadJsonMD5ResultSet.get(1));
                        }
                        List<String> downloadJsonSHA1ResultSet = FileHashValue.getFileHashValue(FileJSON.getDownloadJson(), 3);
                        if (downloadJsonSHA1ResultSet.get(0).equals("Success")) {
                            System.out.println("SHA1：" + downloadJsonSHA1ResultSet.get(2));
                        } else {
                            System.out.println(downloadJsonSHA1ResultSet.get(1));
                        }
                        List<String> downloadJsonSHA256ResultSet = FileHashValue.getFileHashValue(FileJSON.getDownloadJson(), 4);
                        if (downloadJsonSHA256ResultSet.get(0).equals("Success")) {
                            System.out.println("SHA-256：" + downloadJsonSHA256ResultSet.get(2));
                        } else {
                            System.out.println(downloadJsonSHA256ResultSet.get(1));
                        }
                        List<String> downloadJsonSHA384ResultSet = FileHashValue.getFileHashValue(FileJSON.getDownloadJson(), 5);
                        if (downloadJsonSHA384ResultSet.get(0).equals("Success")) {
                            System.out.println("SHA-384：" + downloadJsonSHA384ResultSet.get(2));
                        } else {
                            System.out.println(downloadJsonSHA384ResultSet.get(1));
                        }
                        List<String> downloadJsonSHA512ResultSet = FileHashValue.getFileHashValue(FileJSON.getDownloadJson(), 6);
                        if (downloadJsonSHA512ResultSet.get(0).equals("Success")) {
                            System.out.println("SHA-512：" + downloadJsonSHA512ResultSet.get(2));
                        } else {
                            System.out.println(downloadJsonSHA512ResultSet.get(1));
                        } 
                        System.out.println(); 
                    }
                }
                /* 詢問是否要重新下載 */
                else {
                    while (true) {
                        System.out.println("程式目錄內已有資料檔，請問是否要下載最新版本的檔案？(Y/N，預設為N)");
                        String option = scan.nextLine();
                        if (option.toUpperCase().equals("Y")) {
                            /* 嘗試下載json檔案 */
                            List<String> downloadJsonResult = FileDownload.downloadJson();
                            System.out.println(downloadJsonResult.get(1));
                            if(downloadJsonResult.get(0).equals("Success")){
                                System.out.println(System.lineSeparator() + "所下載的檔案相關驗證資訊為：");
                                List<String> downloadJsonCRC32ResultSet = FileHashValue.getFileHashValue(FileJSON.getDownloadJson(), 1);
                                if (downloadJsonCRC32ResultSet.get(0).equals("Success")) {
                                    System.out.println("CRC32：" + downloadJsonCRC32ResultSet.get(2));
                                } else {
                                    System.out.println(downloadJsonCRC32ResultSet.get(1));
                                }
                                List<String> downloadJsonMD5ResultSet = FileHashValue.getFileHashValue(FileJSON.getDownloadJson(), 2);
                                if (downloadJsonMD5ResultSet.get(0).equals("Success")) {
                                    System.out.println("MD5：" + downloadJsonMD5ResultSet.get(2));
                                } else {
                                    System.out.println(downloadJsonMD5ResultSet.get(1));
                                }
                                List<String> downloadJsonSHA1ResultSet = FileHashValue.getFileHashValue(FileJSON.getDownloadJson(), 3);
                                if (downloadJsonSHA1ResultSet.get(0).equals("Success")) {
                                    System.out.println("SHA1：" + downloadJsonSHA1ResultSet.get(2));
                                } else {
                                    System.out.println(downloadJsonSHA1ResultSet.get(1));
                                }
                                List<String> downloadJsonSHA256ResultSet = FileHashValue.getFileHashValue(FileJSON.getDownloadJson(), 4);
                                if (downloadJsonSHA256ResultSet.get(0).equals("Success")) {
                                    System.out.println("SHA-256：" + downloadJsonSHA256ResultSet.get(2));
                                } else {
                                    System.out.println(downloadJsonSHA256ResultSet.get(1));
                                }
                                List<String> downloadJsonSHA384ResultSet = FileHashValue.getFileHashValue(FileJSON.getDownloadJson(), 5);
                                if (downloadJsonSHA384ResultSet.get(0).equals("Success")) {
                                    System.out.println("SHA-384：" + downloadJsonSHA384ResultSet.get(2));
                                } else {
                                    System.out.println(downloadJsonSHA384ResultSet.get(1));
                                }
                                List<String> downloadJsonSHA512ResultSet = FileHashValue.getFileHashValue(FileJSON.getDownloadJson(), 6);
                                if (downloadJsonSHA512ResultSet.get(0).equals("Success")) {
                                    System.out.println("SHA512：" + downloadJsonSHA512ResultSet.get(2));
                                } else {
                                    System.out.println(downloadJsonSHA512ResultSet.get(1));
                                } 
                                System.out.println(); 
                            }
                            break;
                        } else if (option.toUpperCase().equals("N") || option.equals("")) {
                            break;
                        } else {
                            System.out.println("無效的輸入參數，請重新輸入..." + System.lineSeparator());
                        }
                    }
                }

                /* 讀取json */
                ReadJsonOriginalDataResult jsonDataReadResult = FileJSON.readDownloadJson();
                if (jsonDataReadResult.getResult().equals("Fail")) {
                    System.out.println(jsonDataReadResult.getContents());
                    System.out.println("請嘗試重新下載來源json檔");
                } else {
                    wifiInfoList = jsonDataReadResult.getWifiDataList();
                    for (Wifi_Info jsonData : wifiInfoList) {
                        jsonPKList.add(jsonData.getSiteId());
                    }

                    startSQL = true;
                }

                if (startSQL) {
                    /* 載入XML */
                    ReadXMLDataResult xmlUserConfResult = FileXML.readUserConfXML();
                    if (xmlUserConfResult.getResult().equals("Success")) {
                        List<String> dbInfo = xmlUserConfResult.getEncodeMessage();
                        while (true) {
                            /* 準備顯示SQL功能，先預載SQL資料回來 */
                            WifiInfoDAO wifiInfoJDBCDAO = new WifiInfoJDBCDAO();
                            QueryResult sqlSelectAllList = wifiInfoJDBCDAO.queryDBData(dbInfo);

                            if (sqlSelectAllList.getSuccess()) {
                                dataCount = sqlSelectAllList.getSqlData().size();
                                DBDataList = sqlSelectAllList.getSqlData();
                                PKList.clear();

                                for (Wifi_Info data : DBDataList) {
                                    PKList.add(data.getSiteId());
                                }
                            } else {
                                System.out.println(sqlSelectAllList.getContents());
                                System.out.println("似乎無法連線到資料庫或發生其他異常，程式無法繼續...");
                                break;
                            }

                            String banner = "請輸入數字以選擇要使用的功能，目前可使用的功能有：" + System.lineSeparator() + "1.執行新增";
                            banner = (dataCount > 0)
                                    ? banner + System.lineSeparator() + "2.執行修改" + System.lineSeparator() + "3.執行刪除"
                                            + System.lineSeparator() + "4.執行查詢"
                                    : banner;
                            banner = banner + System.lineSeparator() + "輸入「exit」以離開" + System.lineSeparator()
                                    + "輸入「logout」以登出";
                            System.out.println(banner);

                            String function = scan.nextLine();

                            /* 新增模塊 */
                            if (function.equals("1")) {
                                boolean runInsert = true;

                                System.out.println("開始載入json中轉換後的內容..." + System.lineSeparator());
                                /* 顯示資料 */
                                DisplayData.showData(scan, wifiInfoList);

                                /* 準備輸入更新的資料 */
                                while (true) {
                                    System.out.println("請輸入欲新增的熱點之代碼：" + System.lineSeparator() + "輸入「main」可返回主功能處"
                                            + System.lineSeparator() + "輸入「all」則會新增全部資料");
                                    String inputPK = scan.nextLine();
                                    if (jsonPKList.contains(inputPK) && !PKList.contains(inputPK)) {
                                        tmpPKList.add(inputPK);
                                        System.out.println("是否要再繼續輸入？(Y/N，預設為N)");
                                        String keepType = scan.nextLine();
                                        if (keepType.toUpperCase().equals("N") || keepType.equals("")) {
                                            break;
                                        } else if (keepType.toUpperCase().equals("Y")) {
                                            System.out.println("請準備輸入下一個");
                                        } else {
                                            System.out.println("無效的輸入值，請重新輸入");
                                        }
                                    } else if (inputPK.toLowerCase().equals("main")) {
                                        runInsert = false;
                                        break;
                                    } else if (inputPK.toLowerCase().equals("all")) {
                                        for (String inputJsonPK : jsonPKList) {
                                            if (jsonPKList.contains(inputJsonPK) && !PKList.contains(inputJsonPK)) {
                                                tmpPKList.add(inputJsonPK);
                                            }
                                        }
                                        break;
                                    } else {
                                        System.out.println("無效或已存在於資料庫中的輸入值，請重新輸入");
                                    }
                                }

                                /* 正式執行新增 */
                                if (runInsert) {
                                    List<String> insertResult = wifiInfoJDBCDAO.insertDBData(tmpPKList, wifiInfoList,
                                            dbInfo);
                                    /* 失敗 */
                                    if (insertResult.get(0).equals("Fail")) {
                                        System.out.println(insertResult.get(1));
                                    }
                                    /* 成功 */
                                    else {
                                        String successMessage = "順利完成新增操作..." + System.lineSeparator()
                                                + insertResult.get(1) + System.lineSeparator()
                                                + "請問是否要將新增的資料儲存成檔案備份？(Y/N，預設為Y)";
                                        /* 執行存檔 */
                                        SaveFile.saveAllTypeFile(successMessage, scan, tmpPKList, wifiInfoList, 1);
                                    }
                                }

                                /* 清空選取的PKList */
                                tmpPKList.clear();
                            }

                            /* 修改模塊 */
                            else if (dataCount > 0 && function.equals("2")) {
                                boolean runUpdate = true;

                                /* 手動修改用變數 */
                                Wifi_Info updateInfo = new Wifi_Info("", "", 0, "", "", new BigDecimal("0"),
                                        new BigDecimal("0"), "", 0);

                                System.out.println("開始載入資料庫中的內容..." + System.lineSeparator());

                                List<Wifi_Info> dbDataList = sqlSelectAllList.getSqlData();
                                /* 顯示資料 */
                                DisplayData.showData(scan, dbDataList);

                                /* 準備輸入修改的資料 */
                                while (true) {
                                    System.out.println(
                                            "請問是否要透過來源檔進行更新？(Y/N，預設為Y)" + System.lineSeparator() + "輸入「main」可返回主功能處");
                                    String inputSource = scan.nextLine();
                                    /* 從json檔進行更新 */
                                    if (inputSource.toUpperCase().equals("Y") || inputSource.equals("")) {
                                        while (true) {
                                            System.out.println("請輸入欲修改的熱點之代碼：");
                                            String inputPK = scan.nextLine();
                                            if (jsonPKList.contains(inputPK) && PKList.contains(inputPK)) {
                                                tmpPKList.add(inputPK);
                                                for (Wifi_Info jsonWifiData : wifiInfoList) {
                                                    if (jsonWifiData.getSiteId().equals(inputPK)) {
                                                        updateInfo = jsonWifiData;
                                                    }
                                                }
                                                break;
                                            } else {
                                                System.out.println("無效的輸入值，請重新輸入");
                                            }
                                        }
                                        break;
                                    }
                                    /* 手動輸入修改資料 */
                                    else if (inputSource.toUpperCase().equals("N")) {
                                        while (true) {
                                            System.out.println("請輸入欲修改的熱點之代碼：");
                                            String inputPK = scan.nextLine();
                                            if (PKList.contains(inputPK)) {
                                                updateInfo.setSiteId(inputPK);

                                                System.out.println("請輸入修改後的熱點名稱(不修改請直接按「Enter」)");
                                                String updateName = scan.nextLine();
                                                if (!updateName.equals("")) {
                                                    updateInfo.setName(updateName);
                                                }

                                                while(true){
                                                    System.out.println("請輸入修改後的郵遞區號(不修改請直接按「Enter」)");
                                                    String updateAreaCodeText = scan.nextLine();
                                                    if (!updateAreaCodeText.equals("")) {
                                                        try {
                                                            int updateAreaCode = Integer.parseInt(updateAreaCodeText);
                                                            updateInfo.setAreaCode(updateAreaCode);
                                                            break;
                                                        } catch(NumberFormatException NFe){
                                                            System.out.println("無效的輸入值，請重新輸入");
                                                        }
                                                    }
                                                    else {
                                                        break;
                                                    }
                                                }

                                                System.out.println("請輸入修改後的縣/市/區/鄉/鎮等行政區域(不修改請直接按「Enter」)");
                                                String updateAreaName = scan.nextLine();
                                                if (!updateAreaName.equals("")) {
                                                    updateInfo.setAreaName(updateAreaName);
                                                }

                                                System.out.println("請輸入修改後的地址(不修改請直接按「Enter」)");
                                                String updateAddress = scan.nextLine();
                                                if (!updateAddress.equals("")) {
                                                    updateInfo.setAddress(updateAddress);
                                                }

                                                while(true){
                                                    System.out.println("請輸入修改後的經度(不修改請直接按「Enter」)");
                                                    String updateLongitudeText = scan.nextLine();
                                                    if (!updateLongitudeText.equals("")) {
                                                        try {
                                                            BigDecimal updateLongitude = new BigDecimal(updateLongitudeText);
                                                            updateInfo.setLongitude(updateLongitude);
                                                            break;
                                                        } catch(NumberFormatException NFe){
                                                            System.out.println("無效的輸入值，請重新輸入");
                                                        }
                                                    }
                                                    else {
                                                        break;
                                                    }
                                                }

                                                while(true){
                                                    System.out.println("請輸入修改後的緯度(不修改請直接按「Enter」)");
                                                    String updateLatitudeText = scan.nextLine();
                                                    if (!updateLatitudeText.equals("")) {
                                                        try {
                                                            BigDecimal updateLatitude = new BigDecimal(updateLatitudeText);
                                                            updateInfo.setLatitude(updateLatitude);
                                                            break;
                                                        } catch(NumberFormatException NFe){
                                                            System.out.println("無效的輸入值，請重新輸入");
                                                        }
                                                    }
                                                    else {
                                                        break;
                                                    }
                                                }

                                                System.out.println("請輸入修改後的管理單位(不修改請直接按「Enter」)");
                                                String updateAgency = scan.nextLine();
                                                if (!updateAgency.equals("")) {
                                                    updateInfo.setAddress(updateAgency);
                                                }

                                                tmpPKList.add(inputPK);
                                                break;
                                            } else {
                                                System.out.println("無效的輸入值，請重新輸入");
                                            }
                                        }
                                        break;
                                    } else if (inputSource.toLowerCase().equals("main")) {
                                        runUpdate = false;
                                        break;
                                    } else {
                                        System.out.println("無效或已存在於資料庫中的輸入值，請重新輸入");
                                    }
                                }

                                /* 正式執行修改 */
                                if (runUpdate) {
                                    QueryResult sqlUpdateResult = wifiInfoJDBCDAO.updateData(dbInfo, dbDataList,
                                            updateInfo);
                                    /* 失敗 */
                                    if (!sqlUpdateResult.getSuccess()) {
                                        System.out.println(sqlUpdateResult.getContents());
                                    }
                                    /* 成功 */
                                    else {
                                        String successMessage = "順利完成修改操作..." + System.lineSeparator()
                                                + "請問是否要將修改的資料儲存成檔案備份？(Y/N，預設為Y)";
                                        /* 執行存檔 */
                                        SaveFile.saveAllTypeFile(successMessage, scan, tmpPKList, sqlUpdateResult.getSqlData(), 2);
                                    }
                                }

                                /* 清空選取的PKList */
                                tmpPKList.clear();
                            }

                            /* 刪除模塊 */
                            else if (dataCount > 0 && function.equals("3")) {
                                boolean runDelete = true;

                                System.out.println("開始載入資料庫中的內容..." + System.lineSeparator());

                                List<Wifi_Info> dbDataList = sqlSelectAllList.getSqlData();
                                /* 顯示資料 */
                                DisplayData.showData(scan, dbDataList);

                                /* 準備輸入刪除的資料 */
                                while (true) {
                                    System.out.println("請輸入欲刪除的熱點代碼：" + System.lineSeparator() + "輸入「main」可返回主功能處");
                                    String inputPK = scan.nextLine();
                                    if (PKList.contains(inputPK)) {
                                        tmpPKList.add(inputPK);
                                        System.out.println("是否要再繼續輸入？(Y/N，預設為N)");
                                        String keepType = scan.nextLine();
                                        if (keepType.toUpperCase().equals("N") || keepType.equals("")) {
                                            break;
                                        } else if (keepType.toUpperCase().equals("Y")) {
                                            System.out.println("請準備輸入下一個");
                                        } else {
                                            System.out.println("無效的輸入值，請重新輸入");
                                        }
                                    } else if (inputPK.toLowerCase().equals("main")) {
                                        runDelete = false;
                                        break;
                                    } else {
                                        System.out.println("無效的輸入值，請重新輸入");
                                    }
                                }

                                /* 正式執行刪除 */
                                if (runDelete) {
                                    List<String> deleteResult = wifiInfoJDBCDAO.deleteDBData(tmpPKList, dbDataList,
                                            dbInfo);
                                    /* 失敗 */
                                    if (deleteResult.get(0).equals("Fail")) {
                                        System.out.println(deleteResult.get(1));
                                    }
                                    /* 成功 */
                                    else {
                                        String successMessage = "順利完成刪除操作..." + System.lineSeparator()
                                                + "請問是否要將刪除的資料儲存成檔案備份？(Y/N，預設為Y)";
                                        /* 執行存檔 */
                                        SaveFile.saveAllTypeFile(successMessage, scan, tmpPKList, wifiInfoList, 3);
                                    }
                                }

                                /* 清空選取的PKList */
                                tmpPKList.clear();
                            }

                            /* 查詢模塊 */
                            else if (dataCount > 0 && function.equals("4")) {
                                boolean runSelect;
                                boolean isDefault = false;
                                boolean displayResult = false;

                                /* 查詢用參數 */
                                Wifi_Info searchCondition = new Wifi_Info("", "", 0, "", "", new BigDecimal("0"),
                                        new BigDecimal("0"), "", 0);

                                /* 郵遞區號列表 */
                                List<Integer> areaCodeList = new ArrayList<>();
                                for (Wifi_Info dbData : sqlSelectAllList.getSqlData()) {
                                    areaCodeList.add(dbData.getAreaCode());
                                }
                                /* 經度列表 */
                                List<BigDecimal> longitudeList = new ArrayList<>();
                                for (Wifi_Info dbData : sqlSelectAllList.getSqlData()) {
                                    longitudeList.add(dbData.getLongitude());
                                }
                                /* 緯度列表 */
                                List<BigDecimal> latitudeList = new ArrayList<>();
                                for (Wifi_Info dbData : sqlSelectAllList.getSqlData()) {
                                    latitudeList.add(dbData.getLatitude());
                                }

                                /* 準備輸入查詢的條件 */
                                while (true) {
                                    String searchText = "請輸入欲查詢的條件名稱：" + System.lineSeparator() + "輸入「1」代表熱點代碼"
                                            + System.lineSeparator() + "輸入「2」代表熱點名稱" + System.lineSeparator()
                                            + "輸入「3」代表郵遞區號" + System.lineSeparator() + "輸入「4」代表縣/市/區/鄉/鎮等行政區域"
                                            + System.lineSeparator() + "輸入「5」代表地址" + System.lineSeparator()
                                            + "輸入「6」代表管理單位" + System.lineSeparator() + "輸入「7」代表經度"
                                            + System.lineSeparator() + "輸入「8」代表緯度" + System.lineSeparator()
                                            + "輸入 [all] 可以查詢全部資料" + System.lineSeparator() + "輸入「main」可返回主功能處";
                                    System.out.println(searchText);
                                    String inputCondition = scan.nextLine();

                                    if (inputCondition.equals("1")) {
                                        System.out.println("請輸入欲查詢的熱點代碼關鍵字:");
                                        String siteIdCon = scan.nextLine();
                                        if (!siteIdCon.equals("")) {
                                            searchCondition.setSiteId(siteIdCon);
                                            runSelect = true;
                                            break;
                                        } else {
                                            System.out.println("無效的輸入值，請重新輸入");
                                        }
                                    }
                                    if (inputCondition.equals("2")) {
                                        System.out.println("請輸入欲查詢的熱點名稱關鍵字:");
                                        String nameCon = scan.nextLine();
                                        if (!nameCon.equals("")) {
                                            searchCondition.setName(nameCon);
                                            runSelect = true;
                                            break;
                                        } else {
                                            System.out.println("無效的輸入值，請重新輸入");
                                        }
                                    }
                                    if (inputCondition.equals("3")) {
                                        System.out.println("請輸入欲查詢的郵遞區號:");
                                        String areaCodeText = scan.nextLine();
                                        try {
                                            int areaCode = Integer.parseInt(areaCodeText);
                                            /* 檢查輸入值是否為DB上有的郵遞區號 */
                                            if (areaCodeList.contains(areaCode)) {
                                                searchCondition.setAreaCode(areaCode);
                                                runSelect = true;
                                                break;
                                            } else {
                                                System.out.println("無效的輸入值，請重新輸入");
                                            }
                                        } catch (NumberFormatException nfE) {
                                            System.out.println("無效的輸入值，請重新輸入");
                                        }
                                    }
                                    if (inputCondition.equals("4")) {
                                        System.out.println("請輸入欲查詢的縣/市/區/鄉/鎮關鍵字:");
                                        String areaNameCon = scan.nextLine();
                                        if (!areaNameCon.equals("")) {
                                            searchCondition.setAreaName(areaNameCon);

                                            while (true) {
                                                System.out.println("請選擇是否要一併查詢地址？(Y/N-預設為Y)");
                                                String keepAdd = scan.nextLine();
                                                if (keepAdd.toUpperCase().equals("Y") || keepAdd.equals("")) {
                                                    inputCondition = "5";
                                                    break;
                                                } else if (keepAdd.toUpperCase().equals("N")) {
                                                    break;
                                                } else {
                                                    System.out.println("無效的輸入值，請重新輸入");
                                                }
                                            }
                                            if (!inputCondition.equals("5")) {
                                                runSelect = true;
                                                break;
                                            }
                                        } else {
                                            System.out.println("無效的輸入值，請重新輸入");
                                        }
                                    }
                                    if (inputCondition.equals("5")) {
                                        System.out.println("請輸入欲查詢的地址關鍵字:");
                                        String addressCon = scan.nextLine();
                                        if (!addressCon.equals("")) {
                                            searchCondition.setAreaName(addressCon);
                                            runSelect = true;
                                            break;
                                        } else {
                                            System.out.println("無效的輸入值，請重新輸入");
                                        }
                                    }
                                    if (inputCondition.equals("6")) {
                                        System.out.println("請輸入欲查詢的經度:");
                                        String longitudeText = scan.nextLine();
                                        try {
                                            BigDecimal longitude = new BigDecimal(longitudeText);
                                            /* 檢查輸入值是否為DB上有的經度 */
                                            if (longitudeList.contains(longitude)) {
                                                searchCondition.setLongitude(longitude);
                                                while (true) {
                                                    System.out.println("請選擇是否要一併查詢緯度？(Y/N-預設為Y)");
                                                    String keepAdd = scan.nextLine();
                                                    if (keepAdd.toUpperCase().equals("Y") || keepAdd.equals("")) {
                                                        inputCondition = "7";
                                                        break;
                                                    } else if (keepAdd.toUpperCase().equals("N")) {
                                                        break;
                                                    } else {
                                                        System.out.println("無效的輸入值，請重新輸入");
                                                    }
                                                }
                                                if (!inputCondition.equals("7")) {
                                                    runSelect = true;
                                                    break;
                                                }
                                            } else {
                                                System.out.println("無效的輸入值，請重新輸入");
                                            }
                                        } catch (NumberFormatException nfE) {
                                            System.out.println("無效的輸入值，請重新輸入");
                                        }
                                    }
                                    if (inputCondition.equals("7")) {
                                        System.out.println("請輸入欲查詢的緯度:");
                                        String latitudeText = scan.nextLine();
                                        try {
                                            BigDecimal latitude = new BigDecimal(latitudeText);
                                            /* 檢查輸入值是否為DB上有的緯度 */
                                            if (latitudeList.contains(latitude)) {
                                                while (true) {
                                                    System.out.println("請選擇是否要一併查詢經度？(Y/N-預設為Y)");
                                                    String keepAdd = scan.nextLine();
                                                    if (keepAdd.toUpperCase().equals("Y") || keepAdd.equals("")) {
                                                        inputCondition = "6";
                                                        break;
                                                    } else if (keepAdd.toUpperCase().equals("N")) {
                                                        break;
                                                    } else {
                                                        System.out.println("無效的輸入值，請重新輸入");
                                                    }
                                                }
                                                if (!inputCondition.equals("6")) {
                                                    runSelect = true;
                                                    break;
                                                }
                                            } else {
                                                System.out.println("無效的輸入值，請重新輸入");
                                            }
                                        } catch (NumberFormatException nfE) {
                                            System.out.println("無效的輸入值，請重新輸入");
                                        }
                                    }
                                    if (inputCondition.equals("8")) {
                                        System.out.println("請輸入欲查詢的主管機關關鍵字:");
                                        String agencyCon = scan.nextLine();
                                        if (!agencyCon.equals("")) {
                                            searchCondition.setAreaName(agencyCon);
                                            runSelect = true;
                                            break;
                                        } else {
                                            System.out.println("無效的輸入值，請重新輸入");
                                        }
                                    }
                                    if (inputCondition.toLowerCase().equals("all")) {
                                        runSelect = true;
                                        isDefault = true;
                                        break;
                                    } else if (inputCondition.toLowerCase().equals("main")) {
                                        runSelect = false;
                                        break;
                                    } else {
                                        System.out.println("無效的輸入值，請重新輸入");
                                    }
                                }

                                /* 正式執行查詢 */
                                if (runSelect) {
                                    QueryResult sqlSelectList = (isDefault) ? wifiInfoJDBCDAO.queryDBData(dbInfo)
                                            : wifiInfoJDBCDAO.selectDBData(dbInfo, searchCondition);

                                    /* 失敗 */
                                    if (!sqlSelectList.getSuccess()) {
                                        System.out.println(sqlSelectList.getContents());
                                    }
                                    /* 成功 */
                                    else {
                                        displayResult = true;
                                    }

                                    if (displayResult) {
                                        List<Wifi_Info> selectedDataList = sqlSelectList.getSqlData();

                                        if (selectedDataList.size() > 0) {
                                            /* 顯示資料 */
                                            DisplayData.showData(scan, selectedDataList);
                                            String successMessage = "順利完成查詢操作..." + System.lineSeparator()
                                                    + "請問是否要將查詢的資料儲存成檔案備份？(Y/N，預設為Y)";
                                            /* 執行存檔 */
                                            SaveFile.saveAllTypeFile(successMessage, scan, tmpPKList, selectedDataList, 4);
                                        } else {
                                            System.out.println("沒有查到任何符合條件的資料");
                                        }
                                    }
                                }

                                /* 清空郵遞區號列表 */
                                areaCodeList.clear();
                            }

                            /* 登出 */
                            else if (function.toLowerCase().equals("logout")) {
                                loginPass = false;
                                break;
                            }

                            /* 離開 */
                            else if (function.toLowerCase().equals("exit")) {
                                leave = true;
                                break;
                            }

                            /* 無效輸入 */
                            else {
                                System.out.println("無效的輸入參數，請重新輸入" + System.lineSeparator());
                            }
                        }
                    } else {
                        System.out.println(xmlUserConfResult.getContents());
                        System.out.println("載入連線相關設定失敗！程式無法繼續，請關閉程式後檢查相關檔案，或刪除後再次啟動，由程式重新建立");
                        leave = true;
                    }
                }

            }
            /* 正常關閉程式 */
            if (leave) {
                break;
            }
        }

        System.out.println("程式已停止...");
        /* 關閉scanner */
        scan.close();
        /* 中止Timer */
        tr0.cancel();
    }
}