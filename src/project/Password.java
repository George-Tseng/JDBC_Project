package project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Password {
    /* 使用者設定檔目錄設定 */
    private static final File userDir = new File("user");

    /**
     * 回傳目錄下有多少檔案數量
     *
     * @return 檔案數量(length)
     */
    /* 回傳目錄下有多少檔案數量 */
    protected static long getUserFilesNum() {
        long userFilesNum = 0;
        String[] fileList = userDir.list();

        if (fileList != null) {
            userFilesNum = fileList.length;
        }

        return userFilesNum;
    }

    /**
     * 檢查目錄是否存在
     *
     * @return True代表檔案存在
     */
    /* 檢查目錄是否存在 */
    protected static boolean checkUserDir() {
        return userDir.exists();
    }

    /**
     * 檢查目錄是否為目錄
     *
     * @return True代表檔案為目錄
     */
    /* 檢查目錄是否為目錄 */
    protected static boolean checkUserIsDir() {
        return userDir.isDirectory();
    }

    /**
     * 建立目錄
     *
     * @return 回傳結果，第一項Fail表失敗、Success表成功，第二項為相關描述
     */
    /* 建立目錄 */
    protected static List<String> createUserDir() {
        List<String> createResult = new ArrayList<>();
        if (userDir.mkdir()) {
            createResult.add("Success");
            createResult.add("成功建立使用者目錄");
        } else {
            createResult.add("Fail");
            createResult.add("遇到錯誤，無法建立目錄");
        }

        return createResult;
    }
}
