package project;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeNow {
    /*時間格式*/
    private static SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss:SSS");
    private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");

    public static String getDateNow(){
        /*取得目前時間*/
        Date timeCurrent = new Date();
        /*格式化時間*/
        return sdf0.format(timeCurrent);
    }
    
    public static String getCreateFileTime(){
        /*取得目前時間*/
        Date timeCurrent = new Date();
        /*格式化時間*/
        return sdf1.format(timeCurrent);
    }
}