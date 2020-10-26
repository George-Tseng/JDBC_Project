package project;

import java.util.TimerTask;

public class File_Timer extends TimerTask {
    public void run(){
        String timeNow = TimeNow.getDateNow();
        FileCSV.writeTmpFile(timeNow);
    }
}
