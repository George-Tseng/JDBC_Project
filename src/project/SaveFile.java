package project;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class SaveFile {

    /**
     * 將SQL動作的結果儲存成csv、json、xml檔
     *
     * @param successMessage 成功時顯示的訊息(String)
     * @param scan           Scanner物件
     * @param tmpPKList      暫存用的主鍵列表(使用查詢功能時不會用到，List<String>)
     * @param wifiInfoList   自定義類的List，為SQL動作執行有關的資料陣列
     * @param mode           1->更新，2->修改，3->刪除，4->查詢(int)
     */
    /* 將SQL動作的結果儲存成csv、json、xml檔 */
    protected static void saveAllTypeFile(String successMessage, Scanner scan, List<String> tmpPKList,
            List<Wifi_Info> wifiInfoList, int mode) {
        while (true) {
            System.out.println(successMessage);
            String saveFile = scan.nextLine();

            if (saveFile.toUpperCase().equals("Y") || saveFile.equals("")) {
                String createInsertFileTime = TimeNow.getCreateFileTime();
                /* 產生CSV */
                List<String> createCsvResult = FileCSV.createSQLCSV(createInsertFileTime, mode, tmpPKList,
                        wifiInfoList);
                if (createCsvResult.get(0).equals("Success")) {
                    System.out.println("成功產生CSV檔案！");
                    System.out.println(createCsvResult.get(2));
                } else {
                    System.out.println("無法產生CSV檔案！");
                    System.out.println(createCsvResult.get(1));
                }
                /* 產生json */
                List<String> createJsonResult = FileJSON.createSQLJSON(createInsertFileTime, mode, tmpPKList,
                        wifiInfoList);
                if (createJsonResult.get(0).equals("Success")) {
                    System.out.println("成功產生JSON檔案！");
                    System.out.println(createJsonResult.get(2));
                } else {
                    System.out.println("無法產生JSON檔案！");
                    System.out.println(createJsonResult.get(1));
                }
                /* 產生xml */
                List<String> createXmlResult = FileXML.createSQLXML(createInsertFileTime, mode, tmpPKList,
                        wifiInfoList);
                if (createXmlResult.get(0).equals("Success")) {
                    System.out.println("成功產生XML檔案！");
                    System.out.println(createXmlResult.get(2));
                } else {
                    System.out.println("無法產生XML檔案！");
                    System.out.println(createXmlResult.get(1));
                }
                System.out.println();

                if (createCsvResult.size() > 2 || createJsonResult.size() > 2 || createXmlResult.size() > 2) {
                    while (true) {
                        System.out.println("請問是否要顯示生成檔案的驗證資訊？(Y/N，預設為Y)");
                        String showHash = scan.nextLine();

                        if (showHash.toUpperCase().equals("Y") || showHash.equals("")) {
                            /* 假如成功產生CSV的話 */
                            if (createCsvResult.size() > 2) {
                                String[] csvFilePathSpace = createCsvResult.get(2).split(System.lineSeparator());
                                String csvFile1Path = csvFilePathSpace[1];
                                String csvFile2Path = csvFilePathSpace[2];
                                String[] csvFileNameSpace = createCsvResult.get(3).split(System.lineSeparator());
                                String csvFile1Name = csvFileNameSpace[0];
                                String csvFile2Name = csvFileNameSpace[1];

                                System.out.println(System.lineSeparator() + csvFile1Name);
                                List<String> csvCRC32ResultSet1 = FileHashValue.getFileHashValue(new File(csvFile1Path),
                                        1);
                                if (csvCRC32ResultSet1.get(0).equals("Success")) {
                                    System.out.println("CRC32：" + csvCRC32ResultSet1.get(2));
                                } else {
                                    System.out.println(csvCRC32ResultSet1.get(1));
                                }
                                List<String> csvMD5ResultSet1 = FileHashValue.getFileHashValue(new File(csvFile1Path),
                                        2);
                                if (csvMD5ResultSet1.get(0).equals("Success")) {
                                    System.out.println("MD5：" + csvMD5ResultSet1.get(2));
                                } else {
                                    System.out.println(csvMD5ResultSet1.get(1));
                                }
                                List<String> csvSHA1ResultSet1 = FileHashValue.getFileHashValue(new File(csvFile1Path),
                                        3);
                                if (csvSHA1ResultSet1.get(0).equals("Success")) {
                                    System.out.println("SHA1：" + csvSHA1ResultSet1.get(2));
                                } else {
                                    System.out.println(csvSHA1ResultSet1.get(1));
                                }
                                List<String> csvSHA256ResultSet1 = FileHashValue
                                        .getFileHashValue(new File(csvFile1Path), 4);
                                if (csvSHA256ResultSet1.get(0).equals("Success")) {
                                    System.out.println("SHA-256：" + csvSHA256ResultSet1.get(2));
                                } else {
                                    System.out.println(csvSHA256ResultSet1.get(1));
                                }
                                List<String> csvSHA384ResultSet1 = FileHashValue
                                        .getFileHashValue(new File(csvFile1Path), 5);
                                if (csvSHA384ResultSet1.get(0).equals("Success")) {
                                    System.out.println("SHA-384：" + csvSHA384ResultSet1.get(2));
                                } else {
                                    System.out.println(csvSHA384ResultSet1.get(1));
                                }
                                List<String> csvSHA512ResultSet1 = FileHashValue
                                        .getFileHashValue(new File(csvFile1Path), 6);
                                if (csvSHA512ResultSet1.get(0).equals("Success")) {
                                    System.out.println("SHA-512：" + csvSHA512ResultSet1.get(2));
                                } else {
                                    System.out.println(csvSHA512ResultSet1.get(1));
                                }

                                System.out.println(System.lineSeparator() + csvFile2Name);
                                List<String> csvCRC32ResultSet2 = FileHashValue.getFileHashValue(new File(csvFile2Path),
                                        1);
                                if (csvCRC32ResultSet2.get(0).equals("Success")) {
                                    System.out.println("CRC32：" + csvCRC32ResultSet2.get(2));
                                } else {
                                    System.out.println(csvCRC32ResultSet2.get(1));
                                }
                                List<String> csvMD5ResultSet2 = FileHashValue.getFileHashValue(new File(csvFile2Path),
                                        2);
                                if (csvMD5ResultSet2.get(0).equals("Success")) {
                                    System.out.println("MD5：" + csvMD5ResultSet2.get(2));
                                } else {
                                    System.out.println(csvMD5ResultSet2.get(1));
                                }
                                List<String> csvSHA1ResultSet2 = FileHashValue.getFileHashValue(new File(csvFile2Path),
                                        3);
                                if (csvSHA1ResultSet2.get(0).equals("Success")) {
                                    System.out.println("SHA1：" + csvSHA1ResultSet2.get(2));
                                } else {
                                    System.out.println(csvSHA1ResultSet2.get(1));
                                }
                                List<String> csvSHA256ResultSet2 = FileHashValue
                                        .getFileHashValue(new File(csvFile2Path), 4);
                                if (csvSHA256ResultSet2.get(0).equals("Success")) {
                                    System.out.println("SHA-256：" + csvSHA256ResultSet2.get(2));
                                } else {
                                    System.out.println(csvSHA256ResultSet2.get(1));
                                }
                                List<String> csvSHA384ResultSet2 = FileHashValue
                                        .getFileHashValue(new File(csvFile2Path), 5);
                                if (csvSHA384ResultSet2.get(0).equals("Success")) {
                                    System.out.println("SHA-384：" + csvSHA384ResultSet2.get(2));
                                } else {
                                    System.out.println(csvSHA384ResultSet2.get(1));
                                }
                                List<String> csvSHA512ResultSet2 = FileHashValue
                                        .getFileHashValue(new File(csvFile2Path), 6);
                                if (csvSHA512ResultSet2.get(0).equals("Success")) {
                                    System.out.println("SHA-512：" + csvSHA512ResultSet2.get(2));
                                } else {
                                    System.out.println(csvSHA512ResultSet2.get(1));
                                }
                            }
                            /* 假如成功產生JSON的話 */
                            if (createJsonResult.size() > 2) {
                                String[] jsonFilePathSpace = createJsonResult.get(2).split(System.lineSeparator());
                                String jsonFile1Path = jsonFilePathSpace[1];
                                String[] jsonFileNameSpace = createJsonResult.get(3).split(System.lineSeparator());
                                String jsonFile1Name = jsonFileNameSpace[0];

                                System.out.println(System.lineSeparator() + jsonFile1Name);
                                List<String> jsonCRC32ResultSet = FileHashValue
                                        .getFileHashValue(new File(jsonFile1Path), 1);
                                if (jsonCRC32ResultSet.get(0).equals("Success")) {
                                    System.out.println("CRC32：" + jsonCRC32ResultSet.get(2));
                                } else {
                                    System.out.println(jsonCRC32ResultSet.get(1));
                                }
                                List<String> jsonMD5ResultSet = FileHashValue.getFileHashValue(new File(jsonFile1Path),
                                        2);
                                if (jsonMD5ResultSet.get(0).equals("Success")) {
                                    System.out.println("MD5：" + jsonMD5ResultSet.get(2));
                                } else {
                                    System.out.println(jsonMD5ResultSet.get(1));
                                }
                                List<String> jsonSHA1ResultSet = FileHashValue.getFileHashValue(new File(jsonFile1Path),
                                        3);
                                if (jsonSHA1ResultSet.get(0).equals("Success")) {
                                    System.out.println("SHA1：" + jsonSHA1ResultSet.get(2));
                                } else {
                                    System.out.println(jsonSHA1ResultSet.get(1));
                                }
                                List<String> jsonSHA256ResultSet = FileHashValue
                                        .getFileHashValue(new File(jsonFile1Path), 4);
                                if (jsonSHA256ResultSet.get(0).equals("Success")) {
                                    System.out.println("SHA-256：" + jsonSHA256ResultSet.get(2));
                                } else {
                                    System.out.println(jsonSHA256ResultSet.get(1));
                                }
                                List<String> jsonSHA384ResultSet = FileHashValue
                                        .getFileHashValue(new File(jsonFile1Path), 5);
                                if (jsonSHA384ResultSet.get(0).equals("Success")) {
                                    System.out.println("SHA-384：" + jsonSHA384ResultSet.get(2));
                                } else {
                                    System.out.println(jsonSHA384ResultSet.get(1));
                                }
                                List<String> jsonSHA512ResultSet = FileHashValue
                                        .getFileHashValue(new File(jsonFile1Path), 6);
                                if (jsonSHA512ResultSet.get(0).equals("Success")) {
                                    System.out.println("SHA-512：" + jsonSHA512ResultSet.get(2));
                                } else {
                                    System.out.println(jsonSHA512ResultSet.get(1));
                                }
                            }
                            /* 假如成功產生XML的話 */
                            if (createXmlResult.size() > 2) {
                                String[] xmlFilePathSpace = createXmlResult.get(2).split(System.lineSeparator());
                                String xmlFile1Path = xmlFilePathSpace[1];
                                String[] xmlFileNameSpace = createXmlResult.get(3).split(System.lineSeparator());
                                String xmlFile1Name = xmlFileNameSpace[0];

                                System.out.println(System.lineSeparator() + xmlFile1Name);
                                List<String> xmlCRC32ResultSet = FileHashValue.getFileHashValue(new File(xmlFile1Path),
                                        1);
                                if (xmlCRC32ResultSet.get(0).equals("Success")) {
                                    System.out.println("CRC32：" + xmlCRC32ResultSet.get(2));
                                } else {
                                    System.out.println(xmlCRC32ResultSet.get(1));
                                }
                                List<String> xmlMD5ResultSet = FileHashValue.getFileHashValue(new File(xmlFile1Path),
                                        2);
                                if (xmlMD5ResultSet.get(0).equals("Success")) {
                                    System.out.println("MD5：" + xmlMD5ResultSet.get(2));
                                } else {
                                    System.out.println(xmlMD5ResultSet.get(1));
                                }
                                List<String> xmlSHA1ResultSet = FileHashValue.getFileHashValue(new File(xmlFile1Path),
                                        3);
                                if (xmlSHA1ResultSet.get(0).equals("Success")) {
                                    System.out.println("SHA1：" + xmlSHA1ResultSet.get(2));
                                } else {
                                    System.out.println(xmlSHA1ResultSet.get(1));
                                }
                                List<String> xmlSHA256ResultSet = FileHashValue.getFileHashValue(new File(xmlFile1Path),
                                        4);
                                if (xmlSHA256ResultSet.get(0).equals("Success")) {
                                    System.out.println("SHA-256：" + xmlSHA256ResultSet.get(2));
                                } else {
                                    System.out.println(xmlSHA256ResultSet.get(1));
                                }
                                List<String> xmlSHA384ResultSet = FileHashValue.getFileHashValue(new File(xmlFile1Path),
                                        5);
                                if (xmlSHA384ResultSet.get(0).equals("Success")) {
                                    System.out.println("SHA-384：" + xmlSHA384ResultSet.get(2));
                                } else {
                                    System.out.println(xmlSHA384ResultSet.get(1));
                                }
                                List<String> xmlSHA512ResultSet = FileHashValue.getFileHashValue(new File(xmlFile1Path),
                                        6);
                                if (xmlSHA512ResultSet.get(0).equals("Success")) {
                                    System.out.println("SHA-512：" + xmlSHA512ResultSet.get(2));
                                } else {
                                    System.out.println(xmlSHA512ResultSet.get(1));
                                }
                            }
                            System.out.println();
                            break;
                        } else if (showHash.toUpperCase().equals("N")) {
                            break;
                        } else {
                            System.out.println("無效的輸入值，請重新輸入");
                        }
                    }
                }

                if (mode != 4) {
                    while (true) {
                        System.out.println("請選擇是否要開啟方才產生的檔案？(Y/N，預設為N)");
                        String openOption = scan.nextLine();

                        if (openOption.toUpperCase().equals("N") || openOption.equals("")) {
                            System.out.println("將返回主功能處..." + System.lineSeparator());
                            break;
                        } else if (openOption.toUpperCase().equals("Y")) {
                            while (true) {
                                String message = "請選擇要開啟的檔案：" + System.lineSeparator() + "1.UTF8編碼的CSV檔"
                                        + System.lineSeparator() + "2.MS950編碼的CSV檔" + System.lineSeparator()
                                        + "3.UTF8編碼的JSON檔" + System.lineSeparator() + "4.UTF8編碼的XML檔";
                                System.out.println(message);
                                String fileOption = scan.nextLine();

                                boolean block = false;

                                QueryResult readResult = new QueryResult();
                                switch (fileOption) {
                                    case "1":
                                        readResult = FileCSV.readSQLCSV(mode, 1, createInsertFileTime);
                                        block = true;
                                        break;
                                    case "2":
                                        readResult = FileCSV.readSQLCSV(mode, 2, createInsertFileTime);
                                        block = true;
                                        break;
                                    case "3":
                                        readResult = FileJSON.readSQLJSON(mode, createInsertFileTime);
                                        block = true;
                                        break;
                                    case "4":
                                        readResult = FileXML.readSQLXML(mode, createInsertFileTime);
                                        block = true;
                                        break;
                                    default:
                                        System.out.println("無效的輸入值，請重新輸入");
                                        break;
                                }

                                if (block) {
                                    /* 失敗 */
                                    if (!readResult.getSuccess()) {
                                        System.out.println(readResult.getContents());
                                    }
                                    /* 成功 */
                                    else {
                                        List<Wifi_Info> readList = readResult.getSqlData();
                                        /* 顯示資料 */
                                        DisplayData.showData(scan, readList);
                                    }
                                    break;
                                }
                            }
                            break;
                        } else {
                            System.out.println("無效的輸入值，請重新輸入");
                        }
                    }
                }
                break;
            } else if (saveFile.toUpperCase().equals("N")) {
                System.out.println("將返回主功能處..." + System.lineSeparator());
                break;
            } else {
                System.out.println("無效的輸入值，請重新輸入");
            }
        }
    }
}
