package project;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MacAddress {

    /**
     * 取得網卡MacAddress
     *
     * @return 網卡資訊(String)
     */
    protected static String getMacAddress() {

        InetAddress myIP;
        /* 生成StringBuilder物件 */
        StringBuilder sb0 = new StringBuilder();

        try {
            /* 取得本地的裝置Domain Name+IP位址 */
            myIP = InetAddress.getLocalHost();
            /* 取得本地的IP位址 */
            // System.out.println("Local Host IP為：" + myIP.getHostAddress());

            /* 用getByInetAddress()找出跟本地IP有關的資訊 */
            NetworkInterface myNetIF = NetworkInterface.getByInetAddress(myIP);
            /* 取出實體位址(macAddress)，並存入陣列中 */
            byte[] myMacAddress = myNetIF.getHardwareAddress();

            /* 用迴圈讓StringBuilder裝填完macAddress */
            for (int i = 0; i < myMacAddress.length; i++) {
                sb0.append(String.format(("%02X%s"), myMacAddress[i], (i < myMacAddress.length - 1) ? "-" : ""));
            }

            /* 多種catch條件可以寫在一起 */
        } catch (UnknownHostException | SocketException E) {
            return "";
        } catch (Exception e0) {
            return "";
        }
        /* 用toString()方法讓StringBuilder轉回String */
        return sb0.toString();

    }
}
