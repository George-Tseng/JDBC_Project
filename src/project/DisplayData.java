package project;

import java.util.List;
import java.util.Scanner;

public class DisplayData {

    /**
     * 顯示List中的資料
     *
     * @param scan Scanner物件
     * @param wifiInfoList 自定義類的List
     */
    /*顯示List中的資料*/
    protected static void showData(Scanner scan, List<Wifi_Info> wifiInfoList){
        int nTmp, nCount, nCountMin;
        while(true){
            System.out.println("請選擇要從第幾筆開始載入(預設為1)，共有" + wifiInfoList.size() + "筆");

            String startPoint = scan.nextLine();
            if(startPoint.equals("")){
                nCount = 0;
                nCountMin = 0;
                break;
            }
            else{
                try{
                    nTmp = Integer.parseInt(startPoint);
                    if(nTmp <= wifiInfoList.size() - 10 && nTmp >= 1){
                        nCount = nTmp - 1;
                        nCountMin = nTmp - 1;
                        break;
                    }
                    else{
                        System.out.println("輸入的值無效，請重新輸入");
                    }
                }catch(NumberFormatException nfeE){
                    System.out.println("輸入的值無效，請重新輸入");
                }
            }
        }

        /*顯示內容*/
        while(nCount < wifiInfoList.size()) {
            if (nCount + 10 < wifiInfoList.size()) {
                nCount += 10;
            } else {
                nCount = wifiInfoList.size();
            }
            System.out.println("以下為第" + (nCountMin + 1) + "筆至第" + nCount + "筆的資料，共有" + wifiInfoList.size() + "筆");

            System.out.println("[");
            for (int i = nCountMin; i < nCount; i++) {
                System.out.println("  {");
                System.out.println("    熱點代碼：" + wifiInfoList.get(i).getSiteId());
                System.out.println("    熱點名稱：" + wifiInfoList.get(i).getName());
                System.out.println("    郵遞區號：" + wifiInfoList.get(i).getAreaCode());
                System.out.println("    縣市區域：" + wifiInfoList.get(i).getAreaName());
                System.out.println("    所在地址：" + wifiInfoList.get(i).getAddress());
                System.out.println("    所在經度：" + wifiInfoList.get(i).getLongitude());
                System.out.println("    所在緯度：" + wifiInfoList.get(i).getLatitude());
                System.out.println("    主管機關：" + wifiInfoList.get(i).getAgency());
                System.out.println("  }");
            }
            System.out.println("]");

            System.out.println("請問是否要繼續顯示？(Y/N，預設為N)");
            String displayOption = scan.nextLine();

            if (displayOption.toUpperCase().equals("Y") && nCount != wifiInfoList.size()) {
                nCountMin += 10;
            } else {
                break;
            }
        }
    }
}
