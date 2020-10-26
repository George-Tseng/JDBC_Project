package project;

import java.util.TimerTask;

public class File_Timer2 extends TimerTask {
    public void run(){
        String timeNow = TimeNow.getDateNow();
        FileCSV.writeTmpFile2(timeNow);
    }
}
