package com.poslink.sample.common;

import com.pax.poslink.CommSetting;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

/**
 * Created by Leon.F on 2018/3/9.
 */

public class DemoUtil {
    public static CommSetting buildConnection(String destip) {
        CommSetting coms = new CommSetting();
        coms.setType(CommSetting.TCP);
//        coms.setType(CommSetting.HTTP);
//        coms.setType(CommSetting.UART);
//        coms.setDestIP("053-PC");
        coms.setDestIP(destip);
//        coms.setDestPort("12397");
        coms.setDestPort("10009");
        coms.setSerialPort("COM3");
        coms.setBaudRate("9600");
        coms.setTimeOut("120000");

        return coms;
    }

    public static void openBrowser() {
        try {
            Desktop.getDesktop().browse(URI.create("http://localhost:19071"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
