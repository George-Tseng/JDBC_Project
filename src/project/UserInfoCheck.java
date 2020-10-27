package project;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UserInfoCheck {

    private static final String dbDriverClassName0 = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String dbDriverClassName1 = "org.mariadb.jdbc.Driver";
    private static final String dbUrl0 = "jdbc:sqlserver://";
    private static final String dbUrl1 = "jdbc:mariadb://";
    private static final int dbDefaultPort0 = 1433;
    private static final int dbDefaultPort1 = 3066;

    protected static List<String> checkUserInfo(Scanner scan) {
        List<String> userInfoList = new ArrayList<>();

        while(true) {
            System.out.println("請輸入使用者名稱(僅接受英文+數字，最長10個字，最短4個字，首字不能為數字)：");
            String userIdText = scan.nextLine();

            if(checkInput(userIdText)) {
                userInfoList.add(userIdText);
                break;
            } else {
                System.out.println("輸入的使用者名稱不符合條件！請重新輸入..." + System.lineSeparator());
            }
        }

        while(true) {
            System.out.println("請輸入使用者密碼(僅接受英文+數字，最長20個字，最短4個字，首字不能為數字)：");
            String userPasswordText = scan.nextLine();

            if(checkInput2(userPasswordText)) {
                userInfoList.add(userPasswordText);
                break;
            } else {
                System.out.println("輸入的使用者密碼不符合條件！請重新輸入..." + System.lineSeparator());
            }
        }

        while(true) {
            System.out.println("請輸入資料庫使用者名稱(僅接受英文+數字，最長10個字，最短4個字，首字不能為數字)：");
            String dbUserIdText = scan.nextLine();

            if(checkInput(dbUserIdText)) {
                userInfoList.add(dbUserIdText);
                break;
            } else {
                System.out.println("輸入的資料庫使用者名稱不符合條件！請重新輸入..." + System.lineSeparator());
            }
        }

        while(true) {
            System.out.println("請輸入資料庫使用者密碼(僅接受英文+數字，最長20個字，最短4個字，首字不能為數字)：");
            String dbUserPasswordText = scan.nextLine();

            if(checkInput2(dbUserPasswordText)) {
                userInfoList.add(dbUserPasswordText);
                break;
            } else {
                System.out.println("輸入的資料庫使用者密碼不符合條件！請重新輸入..." + System.lineSeparator());
            }
        }

        while(true) {
            System.out.println("請選擇SQL資料庫種類：" + System.lineSeparator() + "1.Microsoft SQL Server" + System.lineSeparator() + "2.MariaDB");
            String sqlDbVerText = scan.nextLine();

            if (sqlDbVerText.equals("1")) {
                userInfoList.add(dbDriverClassName0);
                userInfoList.add(dbUrl0);
                break;
            } else if (sqlDbVerText.equals("2")) {
                userInfoList.add(dbDriverClassName1);
                userInfoList.add(dbUrl1);
                break;
            } else {
                System.out.println("輸入的資料庫種類不存在或暫不支援！請重新輸入..." + System.lineSeparator());
            }
        }

        while(true) {
            System.out.println("請輸入連線的IP位置，可接受localhost或實體IP位置(按「Enter」會套用預設值-localhost)：");
            String ipText = scan.nextLine();

            if(ipText.toLowerCase().equals("localhost") || ipText.equals("")) {
                userInfoList.set(5, userInfoList.get(5) + "localhost");
                break;
            } else if(checkInputIPV4(ipText)) {
                userInfoList.set(5, userInfoList.get(5) + ipText);
                break;
            } else {
                System.out.println("輸入的IP資訊無效或暫不支援！請重新輸入..." + System.lineSeparator());
            }
        }

        while(true) {
            System.out.println("請輸入連線的port，按「Enter」略過將採用該Server預設port");
            String portText = scan.nextLine();

            if (portText.equals("")) {
                if(userInfoList.get(4).equals(dbDriverClassName0)) {
                    userInfoList.set(5, userInfoList.get(5) + ":" + dbDefaultPort0);
                    break;
                } else if(userInfoList.get(4).equals(dbDriverClassName1)) {
                    userInfoList.set(5, userInfoList.get(5) + ":" + dbDefaultPort1);
                    break;
                }
            } else if(checkIsNumber(portText)) {
                if(Integer.parseInt(portText) >= 1024 && Integer.parseInt(portText) <= 65535) {
                    userInfoList.set(5, userInfoList.get(5) + ":" + portText);
                    break;
                }
                else {
                    System.out.println("輸入的port資訊無效或暫不支援！請重新輸入..." + System.lineSeparator());
                }
            } else {
                System.out.println("輸入的port資訊無效或暫不支援！請重新輸入..." + System.lineSeparator());
            }
        }

        while(true) {
            System.out.println("請輸入連線的資料庫名稱");
            String dbNameText = scan.nextLine();

            if(!dbNameText.equals("") && userInfoList.get(4).equals(dbDriverClassName0)) {
                userInfoList.set(5, userInfoList.get(5) + ";databaseName=" + dbNameText);
                break;
            } else if(!dbNameText.equals("") && userInfoList.get(4).equals(dbDriverClassName1)) {
                userInfoList.set(5, userInfoList.get(5) + "/" + dbNameText);
                break;
            } else {
                System.out.println("輸入的資料庫名稱不可留白！請重新輸入..." + System.lineSeparator());
            }
        }

        return userInfoList;
    }

    protected static List<String> checkUserInfo(Scanner scan, Console cons) {
        List<String> userInfoList = new ArrayList<>();

        while(true) {
            System.out.println("請輸入使用者名稱(僅接受英文+數字，最長10個字，最短4個字，首字不能為數字)：");
            String userIdText = scan.nextLine();

            if(checkInput(userIdText)) {
                userInfoList.add(userIdText);
                break;
            } else {
                System.out.println("輸入的使用者名稱不符合條件！請重新輸入..." + System.lineSeparator());
            }
        }

        while(true) {
            char[] inputPasswd = cons.readPassword("請輸入使用者密碼(僅接受英文+數字，最長20個字，最短4個字，首字不能為數字)：");
            String userPasswordText = new String(inputPasswd);

            if(checkInput2(userPasswordText)) {
                userInfoList.add(userPasswordText);
                break;
            } else {
                System.out.println("輸入的使用者密碼不符合條件！請重新輸入..." + System.lineSeparator());
            }
        }

        while(true) {
            System.out.println("請輸入資料庫使用者名稱(僅接受英文+數字，最長10個字，最短4個字，首字不能為數字)：");
            String dbUserIdText = scan.nextLine();

            if(checkInput(dbUserIdText)) {
                userInfoList.add(dbUserIdText);
                break;
            } else {
                System.out.println("輸入的資料庫使用者名稱不符合條件！請重新輸入..." + System.lineSeparator());
            }
        }

        while(true) {
            char[] inputDBPasswd = cons.readPassword("請輸入資料庫使用者密碼(僅接受英文+數字，最長20個字，最短4個字，首字不能為數字)：");
            String dbUserPasswordText = new String(inputDBPasswd);

            if(checkInput2(dbUserPasswordText)) {
                userInfoList.add(dbUserPasswordText);
                break;
            } else {
                System.out.println("輸入的資料庫使用者密碼不符合條件！請重新輸入..." + System.lineSeparator());
            }
        }

        while(true) {
            System.out.println("請選擇SQL資料庫種類：" + System.lineSeparator() + "1.Microsoft SQL Server" + System.lineSeparator() + "2.MariaDB");
            String sqlDbVerText = scan.nextLine();

            if (sqlDbVerText.equals("1")) {
                userInfoList.add(dbDriverClassName0);
                userInfoList.add(dbUrl0);
                break;
            } else if (sqlDbVerText.equals("2")) {
                userInfoList.add(dbDriverClassName1);
                userInfoList.add(dbUrl1);
                break;
            } else {
                System.out.println("輸入的資料庫種類不存在或暫不支援！請重新輸入..." + System.lineSeparator());
            }
        }

        while(true) {
            System.out.println("請輸入連線的IP位置，可接受localhost或實體IP位置(預設為localhost)：");
            String ipText = scan.nextLine();

            if(ipText.toLowerCase().equals("localhost") || ipText.equals("")) {
                userInfoList.set(5, userInfoList.get(5) + "localhost");
                break;
            } else if(checkInputIPV4(ipText)) {
                userInfoList.set(5, userInfoList.get(5) + ipText);
                break;
            } else {
                System.out.println("輸入的IP資訊無效或暫不支援！請重新輸入..." + System.lineSeparator());
            }
        }

        while(true) {
            System.out.println("請輸入連線的port，略過將採用該Server預設port");
            String portText = scan.nextLine();

            if (portText.equals("")) {
                if(userInfoList.get(4).equals(dbDriverClassName0)) {
                    userInfoList.set(5, userInfoList.get(5) + ":" + dbDefaultPort0);
                    break;
                } else if(userInfoList.get(4).equals(dbDriverClassName1)) {
                    userInfoList.set(5, userInfoList.get(5) + ":" + dbDefaultPort1);
                    break;
                }
            } else if(checkIsNumber(portText)) {
                if(Integer.parseInt(portText) >= 1024 && Integer.parseInt(portText) <= 65535) {
                    userInfoList.set(5, userInfoList.get(5) + ":" + portText);
                    break;
                }
                else {
                    System.out.println("輸入的port資訊無效或暫不支援！請重新輸入..." + System.lineSeparator());
                }
            } else {
                System.out.println("輸入的port資訊無效或暫不支援！請重新輸入..." + System.lineSeparator());
            }
        }

        while(true) {
            System.out.println("請輸入連線的資料庫名稱");
            String dbNameText = scan.nextLine();

            if(!dbNameText.equals("")) {
                userInfoList.set(5, userInfoList.get(5) + ";databaseName=" + dbNameText);
                break;
            } else {
                System.out.println("輸入的資料庫名稱不可留白！請重新輸入..." + System.lineSeparator());
            }
        }

        return userInfoList;
    }

    protected static boolean checkInput(String inputText) {
        return inputText.matches("[a-zA-Z][a-zA-Z0-9]{3,9}");
    }

    protected static boolean checkInput2(String inputText) {
        return inputText.matches("[a-zA-Z][a-zA-Z0-9]{3,19}");
    }

    protected static boolean checkInputIPV4(String inputIpText) {
        boolean result;
        int count = 0;

        String[] ipSpace = inputIpText.split(".");
        if(Integer.parseInt(ipSpace[0]) <= 255 && Integer.parseInt(ipSpace[0]) >= 0) {
            count++;
        }
        if(Integer.parseInt(ipSpace[1]) <= 255 && Integer.parseInt(ipSpace[0]) >= 0) {
            count++;
        }
        if(Integer.parseInt(ipSpace[2]) <= 255 && Integer.parseInt(ipSpace[0]) >= 0) {
            count++;
        }
        if(Integer.parseInt(ipSpace[3]) <= 255 && Integer.parseInt(ipSpace[0]) >= 0) {
            count++;
        }

        if(count == 4) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

//    未完成的IPV6檢測方式
//    protected static boolean checkInputIPV6(String inputIpText) {
//        boolean result;
//        return result;
//    }

    protected static boolean checkIsNumber(String inputNumberText) {
        return inputNumberText.matches("[0-9]{5}");
    }
}
