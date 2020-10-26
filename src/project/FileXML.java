package project;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileXML {
    /* 使用者設定檔 */
    private static final File userConf = new File("user/userCf.xml");

    // /*檢查使用者目錄下的xml是否可寫入*/
    // protected static boolean checkUserConfXMLWrite() {
    // return userConf.canWrite();
    // }

    /**
     * 檢查使用者目錄下的xml是否可讀取+
     *
     * @return True代表檔案可以被讀取
     */
    /* 檢查使用者目錄下的xml是否可讀取 */
    protected static boolean checkUserConfXMLRead() {
        return userConf.canRead();
    }

    /**
     * 取得使用者目錄下的xml檔案絕對路徑
     *
     * @return xml檔案的絕對路徑(String)
     */
    /* 取得使用者目錄下的xml檔案絕對路徑 */
    protected static String getUserConfXMLPath() {
        return userConf.getAbsolutePath();
    }

    /**
     * 寫入使用者設定檔
     *
     * @param encodeMessage 傳入加密後的資料
     *
     * @return 回傳結果，第一項Fail表失敗、Success表成功，第二項為相關描述
     */
    /* 寫入使用者設定檔 */
    protected static List<String> createUserConfXML(List<String> encodeMessage) {
        List<String> createUserConfResult = new ArrayList<>();
        OutputFormat of0 = OutputFormat.createPrettyPrint();
        FileOutputStream fos0;
        XMLWriter xmlW0;

        Document doc0 = DocumentHelper.createDocument();
        Element rt0 = doc0.addElement("User");
        Element userInfo = rt0.addElement("userInfo");
        userInfo.addElement("timeNow").addText(encodeMessage.get(0));
        userInfo.addElement("macAddress").addText(encodeMessage.get(1));
        userInfo.addElement("user").addText(encodeMessage.get(2));
        userInfo.addElement("password").addText(encodeMessage.get(3));
        userInfo.addElement("dbUser").addText(encodeMessage.get(4));
        userInfo.addElement("dbPassword").addText(encodeMessage.get(5));
        userInfo.addElement("dbDriverClassName").addText(encodeMessage.get(6));
        userInfo.addElement("dbUrl").addText(encodeMessage.get(7));

        try {
            fos0 = new FileOutputStream(userConf);
            of0.setEncoding("UTF-8");
            xmlW0 = new XMLWriter(new OutputStreamWriter(fos0), of0);

            xmlW0.write(doc0);
            xmlW0.flush();
            fos0.close();
            xmlW0.close();

            createUserConfResult.add("Success");
            createUserConfResult.add("使用者設定檔成功建立在：" + System.lineSeparator() + getUserConfXMLPath());
        } catch (IOException ioE) {
            createUserConfResult.add("Fail");
            createUserConfResult.add("遇到錯誤：" + System.lineSeparator() + ioE.toString());
        }

        return createUserConfResult;
    }

    /**
     * 讀取使用者設定檔
     *
     * @return 第一項代表結果(Success/Fail)，第二項為發生例外時的異常資訊，第三項為讀取的資料陣列(List<String>)
     */
    /* 讀取使用者設定檔 */
    protected static ReadXMLDataResult readUserConfXML() {
        ReadXMLDataResult userInfoXmlResult = new ReadXMLDataResult();
        FileInputStream fis0;
        InputStreamReader isr0;
        SAXReader saxR0;
        Document doc0;
        /* 存放資料的陣列 */
        List<String> userInfoList = new ArrayList<>();

        /* 確定檔案可讀取 */
        if (checkUserConfXMLRead()) {
            try {
                fis0 = new FileInputStream(userConf);
                isr0 = new InputStreamReader(fis0, StandardCharsets.UTF_8);
                saxR0 = new SAXReader();
                doc0 = saxR0.read(isr0);

                /* 取得更內層的資料，用陣列接 */
                List<Node> nodeList0 = doc0.selectNodes("User/userInfo");

                for (Node node0 : nodeList0) {
                    userInfoList.add(node0.selectSingleNode("timeNow").getText());
                    userInfoList.add(node0.selectSingleNode("macAddress").getText());
                    userInfoList.add(node0.selectSingleNode("user").getText());
                    userInfoList.add(node0.selectSingleNode("password").getText());
                    userInfoList.add(node0.selectSingleNode("dbUser").getText());
                    userInfoList.add(node0.selectSingleNode("dbPassword").getText());
                    userInfoList.add(node0.selectSingleNode("dbDriverClassName").getText());
                    userInfoList.add(node0.selectSingleNode("dbUrl").getText());
                }

                /* 關閉資源 */
                isr0.close();
                fis0.close();

                userInfoXmlResult.setResult("Success");
                userInfoXmlResult.setEncodeMessage(userInfoList);
            } catch (DocumentException docE) {
                userInfoXmlResult.setResult("Fail");
                userInfoXmlResult.setContents("遇到錯誤：" + System.lineSeparator() + docE.toString());
            } catch (IOException ioE) {
                userInfoXmlResult.setResult("Fail");
                userInfoXmlResult.setContents("遇到錯誤：" + System.lineSeparator() + ioE.toString());
            } catch (Exception e) {
                userInfoXmlResult.setResult("Fail");
                userInfoXmlResult.setContents("遇到錯誤：" + System.lineSeparator() + e.toString());
            }
        }
        /* 無法讀取 */
        else {
            userInfoXmlResult.setResult("Fail");
            userInfoXmlResult.setContents("遇到錯誤，無法讀取檔案");
        }

        return userInfoXmlResult;
    }

    /**
     * 將對SQL的操作產生XML檔
     *
     * @param createFileTime 執行產生檔案時的時間(String)
     * @param mode           1->更新，2->修改，3->刪除，4->查詢(int)
     * @param PKList         受影響的主鍵列表(List<String>)
     * @param wifiInfoList   讀取自json的資料陣列
     *
     * @return 第一項為結果(Success/Fail)，第二項為發生例外時的錯誤訊息(String)，第三項後為所建立檔案的所在位置，第四項為檔名
     */
    /* 將對SQL的操作產生XML檔 */
    protected static List<String> createSQLXML(String createFileTime, int mode, List<String> PKList,
            List<Wifi_Info> wifiInfoList) {
        List<String> createSQLXmlResult = new ArrayList<>();
        /* 設定檔案名稱 */
        String xmlFileName1 = "";
        File xmlFile1;
        FileOutputStream fos1;
        OutputFormat of1 = OutputFormat.createPrettyPrint();
        XMLWriter xmlW1;

        if (mode == 1) {
            xmlFileName1 = "insertedResult_XML_UTF8_" + createFileTime + ".xml";
        } else if (mode == 2) {
            xmlFileName1 = "updatedResult_XML_UTF8_" + createFileTime + ".xml";
        } else if (mode == 3) {
            xmlFileName1 = "deletedResult_XML_UTF8_" + createFileTime + ".xml";
        } else if (mode == 4) {
            xmlFileName1 = "searchedResult_XML_UTF8_" + createFileTime + ".xml";
        }

        try {
            /* 設定檔案位置 */
            if (xmlFileName1.equals("")) {
                createSQLXmlResult.add("Fail");
                createSQLXmlResult.add("遇到錯誤：" + System.lineSeparator() + "無法產生檔案名稱");
            } else {
                /* 設定檔案位置 */
                xmlFile1 = new File(xmlFileName1);
                /* 設定編碼 */
                of1.setEncoding("UTF-8");

                Document doc1 = DocumentHelper.createDocument();

                Element rt1 = doc1.addElement("Public_Wifi_Site");

                if (mode != 4 || mode != 2) {
                    for (Wifi_Info wifiData : wifiInfoList) {
                        if (PKList.contains(wifiData.getSiteId())) {
                            Element wifiSiteInfo1 = rt1.addElement("Wifi_Site_Info");
                            /* 熱點代碼 */
                            wifiSiteInfo1.addElement("siteId").addText(wifiData.getSiteId());
                            /* 熱點名稱 */
                            wifiSiteInfo1.addElement("name").addText(wifiData.getName());
                            /* 郵遞區號 */
                            wifiSiteInfo1.addElement("areaCode").addText(String.valueOf(wifiData.getAreaCode()));
                            /* 縣市區域 */
                            wifiSiteInfo1.addElement("areaName").addText(wifiData.getAreaName());
                            /* 地址 */
                            wifiSiteInfo1.addElement("areaName").addText(wifiData.getAddress());
                            /* 經度 */
                            wifiSiteInfo1.addElement("longitude").addText(String.valueOf(wifiData.getLongitude()));
                            /* 緯度 */
                            wifiSiteInfo1.addElement("latitude").addText(String.valueOf(wifiData.getLatitude()));
                            /* 主管機關 */
                            wifiSiteInfo1.addElement("agency").addText(wifiData.getAgency());
                            /* 版本 */
                            wifiSiteInfo1.addElement("version").addText(String.valueOf(wifiData.getVersion()));
                        }
                    }
                } else {
                    for (Wifi_Info wifiData : wifiInfoList) {
                        Element wifiSiteInfo1 = rt1.addElement("Wifi_Site_Info");
                        /* 熱點代碼 */
                        wifiSiteInfo1.addElement("siteId").addText(wifiData.getSiteId());
                        /* 熱點名稱 */
                        wifiSiteInfo1.addElement("name").addText(wifiData.getName());
                        /* 郵遞區號 */
                        wifiSiteInfo1.addElement("areaCode").addText(String.valueOf(wifiData.getAreaCode()));
                        /* 縣市區域 */
                        wifiSiteInfo1.addElement("areaName").addText(wifiData.getAreaName());
                        /* 地址 */
                        wifiSiteInfo1.addElement("areaName").addText(wifiData.getAddress());
                        /* 經度 */
                        wifiSiteInfo1.addElement("longitude").addText(String.valueOf(wifiData.getLongitude()));
                        /* 緯度 */
                        wifiSiteInfo1.addElement("latitude").addText(String.valueOf(wifiData.getLatitude()));
                        /* 主管機關 */
                        wifiSiteInfo1.addElement("agency").addText(wifiData.getAgency());
                        /* 版本 */
                        wifiSiteInfo1.addElement("version").addText(String.valueOf(wifiData.getVersion()));
                    }
                }

                /* 設定FileOutputStream */
                fos1 = new FileOutputStream(xmlFile1);
                /* 設定OutputStreamWriter */
                xmlW1 = new XMLWriter(new OutputStreamWriter(fos1), of1);

                /* 寫入檔案 */
                xmlW1.write(doc1);
                /* 刷新 */
                xmlW1.flush();
                /* 關閉資源 */
                xmlW1.close();
                fos1.close();

                createSQLXmlResult.add("Success");
                createSQLXmlResult.add("");
                createSQLXmlResult.add("檔案已儲存於：" + System.lineSeparator() + xmlFile1.getAbsolutePath());
                createSQLXmlResult.add(xmlFileName1);
            }
        } catch (IOException ioE) {
            createSQLXmlResult.add("Fail");
            createSQLXmlResult.add("遇到錯誤：" + System.lineSeparator() + ioE.toString());
        } catch (Exception e) {
            createSQLXmlResult.add("Fail");
            createSQLXmlResult.add("遇到錯誤：" + System.lineSeparator() + e.toString());
        }

        return createSQLXmlResult;
    }

    /**
     * 讀取程式所產生的XML檔
     *
     * @param mode           1->更新，2->修改，3->刪除，4->查詢(int)
     * @param createFileTime 執行產生檔案時的時間(String)
     *
     * @return 第一項為結果(boolean)，第二項為發生例外時的錯誤訊息(String)，第三項為資料陣列(自定義類)
     */
    /* 讀取程式所產生的XML檔 */
    protected static QueryResult readSQLXML(int mode, String createFileTime) {
        QueryResult readXmlResult = new QueryResult();

        FileInputStream fis1;
        InputStreamReader isr1;
        SAXReader saxR1;
        Document doc1;
        /* 設定檔案名稱 */
        String xmlFileName1 = "";
        File xmlFile1;

        if (mode == 1) {
            xmlFileName1 = "insertedResult_XML_UTF8_" + createFileTime + ".xml";
        } else if (mode == 2) {
            xmlFileName1 = "updatedResult_XML_UTF8_" + createFileTime + ".xml";
        } else if (mode == 3) {
            xmlFileName1 = "deletedResult_XML_UTF8_" + createFileTime + ".xml";
        } else if (mode == 4) {
            xmlFileName1 = "searchedResult_XML_UTF8_" + createFileTime + ".xml";
        }

        try {
            xmlFile1 = new File(xmlFileName1);
            fis1 = new FileInputStream(xmlFile1);
            isr1 = new InputStreamReader(fis1, StandardCharsets.UTF_8);
            saxR1 = new SAXReader();
            doc1 = saxR1.read(isr1);

            /* 取得更內層的資料，用陣列接 */
            List<Node> nodeList1 = doc1.selectNodes("Public_Wifi_Site/Wifi_Site_Info");
            /* 宣告要傳回的資料陣列 */
            List<Wifi_Info> xmlWifiDataList = new ArrayList<>();

            for (Node node1 : nodeList1) {
                Wifi_Info xmlWifiData = new Wifi_Info("", "", 0, "", "", new BigDecimal("0"), new BigDecimal("0"), "",
                        0);

                xmlWifiData.setSiteId(node1.selectSingleNode("siteId").getText());
                xmlWifiData.setName(node1.selectSingleNode("name").getText());
                xmlWifiData.setAreaCode(Integer.parseInt(node1.selectSingleNode("areaCode").getText()));
                xmlWifiData.setAreaName(node1.selectSingleNode("areaName").getText());
                xmlWifiData.setAddress(node1.selectSingleNode("address").getText());
                xmlWifiData.setLongitude(new BigDecimal(node1.selectSingleNode("longitude").getText()));
                xmlWifiData.setLatitude(new BigDecimal(node1.selectSingleNode("latitude").getText()));
                xmlWifiData.setAgency(node1.selectSingleNode("agency").getText());
                xmlWifiData.setVersion(Integer.parseInt(node1.selectSingleNode("version").getText()));

                xmlWifiDataList.add(xmlWifiData);
            }

            /* 關閉資源 */
            isr1.close();
            fis1.close();

            readXmlResult.setSuccess(true);
            readXmlResult.setSqlData(xmlWifiDataList);
        } catch (DocumentException docE) {
            readXmlResult.setSuccess(false);
            readXmlResult.setContents("遇到錯誤：" + System.lineSeparator() + docE.toString());
        } catch (IOException ioE) {
            readXmlResult.setSuccess(false);
            readXmlResult.setContents("遇到錯誤：" + System.lineSeparator() + ioE.toString());
        } catch (Exception e) {
            readXmlResult.setSuccess(false);
            readXmlResult.setContents("遇到錯誤：" + System.lineSeparator() + e.toString());
        }

        return readXmlResult;
    }
}
