package project;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FileDownload {
    /* 下載檔案&目錄設定 */
    private static final File downloadDir = new File("ref");

    /**
     * 檢查下載目錄下的json是否存在
     *
     * @return True代表檔案存在
     */
    /* 檢查下載目錄下的json是否存在 */
    protected static boolean checkDownloadJson() {
        return FileJSON.getDownloadJson().exists();
    }

    /**
     * 下載遠端json
     *
     * @return 為一個字串陣列，第一項代表結果(Fail/Success)、第二項提供更多資訊(錯誤訊息/檔案下載的絕對路徑)
     */
    /* 下載遠端json */
    protected static List<String> downloadJson() {
        /* 宣告執行結果的陣列 */
        List<String> result = new ArrayList<>();
        /* boolean gate */
        boolean keep0 = true;

        /* 遠端URL */
        URL downloadJsonFile;

        try {
            downloadJsonFile = new URL("https://www.gsp.gov.tw/iTaiwan/itw_tw.json");

            /* 檢查下載目錄是否存在，不存在就建立一個 */
            if (!downloadDir.exists()) {
                if (!downloadDir.mkdir()) {
                    keep0 = false;
                    result.add("Fail");
                    result.add("遇到錯誤，無法建立下載目錄");
                }
            }

            if (keep0) {
                /* 檢查目錄的有效性 */
                if (checkDownloadJson() && !downloadDir.isDirectory()) {
                    result.add("Fail");
                    result.add("遇到錯誤，無效的下載目錄");
                }
                /* 檢查是否可寫入 */
                else if (checkDownloadJson() && !FileJSON.checkDownloadJsonWrite()) {
                    result.add("Fail");
                    result.add("遇到錯誤，無法寫入檔案");
                } else {
                    try (InputStream inputStream = downloadJsonFile.openStream();
                            BufferedInputStream bis0 = new BufferedInputStream(inputStream);
                            FileOutputStream fos = new FileOutputStream(FileJSON.getDownloadJson());
                            BufferedOutputStream bos0 = new BufferedOutputStream(fos)) {

                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = bis0.read(buffer)) != -1) {
                            bos0.write(buffer, 0, length);
                        }
                    } catch (IOException ioE) {
                        result.add("Fail");
                        result.add("遇到錯誤：" + System.lineSeparator() + ioE.toString());
                    } catch (Exception e) {
                        result.add("Fail");
                        result.add("遇到錯誤：" + System.lineSeparator() + e.toString());
                    }

                    result.add("Success");
                    result.add("下載來源檔案成功！該檔案位於：" + System.lineSeparator() + FileJSON.getDownloadJsonPath());
                }
            }
        } catch (MalformedURLException urlE) {
            result.add("Fail");
            result.add("遇到錯誤：" + System.lineSeparator() + urlE.toString());
        } catch (Exception e0) {
            result.add("Fail");
            result.add("遇到錯誤：" + System.lineSeparator() + e0.toString());
        }

        return result;
    }
}
