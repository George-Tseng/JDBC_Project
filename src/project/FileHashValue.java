package project;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.zip.CRC32;

public class FileHashValue {

    /**
     * 取得檔案的HASH值
     * 
     * @param resultFile 要查詢的檔案(File)
     * @param mode       1->CRC32、2->MD5、3->SHA1、4->SHA-256、5->SHA-384、6->SHA-512
     * 
     * @return 第一項為結果(Success/Fail)、第二項為發生例外時的相關訊息(成功時為空)，第三項為HASH值
     */
    /* 取得檔案的HASH值 */
    protected static List<String> getFileHashValue(File resultFile, int mode) {
        /* 宣告CRC */
        CRC32 fileCRC = new CRC32();
        /* 宣告回傳資訊 */
        List<String> hashResultSet = new ArrayList<>();
        /* 參數 */
        byte[] bytes = new byte[1024];
        int count;
        DigestInputStream dis1 = null;
        /* 如果檔案存在 */
        if (resultFile.exists()) {
            /* CRC */
            if (mode == 1) {
                try (FileInputStream fis1 = new FileInputStream(resultFile);
                        BufferedInputStream bis1 = new BufferedInputStream(fis1);) {

                    while ((count = bis1.read(bytes)) != -1) {
                        fileCRC.update(bytes, 0, count);
                    }

                    hashResultSet.add("Success");
                    hashResultSet.add("");
                    /* 這邊多轉換一次成16進位，以便能跟HashMyFile之類的工具上的運行結果對得起來 */
                    hashResultSet.add(Long.toHexString(fileCRC.getValue()));
                } catch (IOException ioE) {
                    hashResultSet.add("Fail");
                    hashResultSet.add("遇到錯誤：" + System.lineSeparator() + ioE.toString());
                } catch (Exception e) {
                    hashResultSet.add("Fail");
                    hashResultSet.add("遇到錯誤：" + System.lineSeparator() + e.toString());
                }
            }
            /* MD5/SHA1/SHA-256/SHA-384/SHA-512 */
            else {
                try (FileInputStream fis1 = new FileInputStream(resultFile);) {
                    /* 建立相關實例 */
                    MessageDigest md1 = null;
                    if (mode == 2) {
                        md1 = MessageDigest.getInstance("MD5");
                    } else if (mode == 3) {
                        md1 = MessageDigest.getInstance("SHA1");
                    } else if (mode == 4) {
                        md1 = MessageDigest.getInstance("SHA-256");
                    } else if (mode == 5) {
                        md1 = MessageDigest.getInstance("SHA-384");
                    } else if (mode == 6) {
                        md1 = MessageDigest.getInstance("SHA-512");
                    }

                    dis1 = new DigestInputStream(fis1, md1);

                    Formatter formatter = new Formatter();

                    while (dis1.read(bytes) != -1)
                        ;
                    byte[] hashDigests = md1.digest();

                    for (byte b1 : hashDigests) {
                        formatter.format("%02x", b1);
                    }

                    hashResultSet.add("Success");
                    hashResultSet.add("");
                    hashResultSet.add(formatter.toString());

                    formatter.close();
                } catch (NoSuchAlgorithmException NSAe) {
                    hashResultSet.add("Fail");
                    hashResultSet.add("遇到錯誤：" + System.lineSeparator() + NSAe.toString());
                } catch (IOException ioE) {
                    hashResultSet.add("Fail");
                    hashResultSet.add("遇到錯誤：" + System.lineSeparator() + ioE.toString());
                } catch (Exception e) {
                    hashResultSet.add("Fail");
                    hashResultSet.add("遇到錯誤：" + System.lineSeparator() + e.toString());
                } finally {
                    if (dis1 != null) {
                        try {
                            dis1.close();
                        } catch (IOException ioE0) {
                            hashResultSet.add("Fail");
                            hashResultSet.add("遇到錯誤：" + System.lineSeparator() + ioE0.toString());
                        } catch (Exception e0) {
                            hashResultSet.add("Fail");
                            hashResultSet.add("遇到錯誤：" + System.lineSeparator() + e0.toString());
                        }
                    }
                }
            }
        }
        /* 如果檔案不在 */
        else {
            hashResultSet.add("Fail");
            hashResultSet.add("遇到錯誤：檔案不存在！");
        }

        /* 回傳 */
        return hashResultSet;
    }
}
