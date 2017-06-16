package com.ymt.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;

/**
 * Created by sunsheng on 2017/4/19.
 */
public class AdbLog extends Thread {

    private static final Logger logger = LoggerFactory.getLogger("adbLog");

    private String cmd;

    public AdbLog(String cmd) {

        //主线程执行完后,改线程停止
        this.setDaemon(true);

        cleanLogcat();

        this.cmd = cmd;
    }

    public void cleanLogcat() {

        new CmdUtil(null).runAdbCmd("logcat -c");

    }


    @Override
    public void run() {

        cmdInvoke(cmd);

    }

    private void cmdInvoke(String cmd) {

        BufferedReader br = null;

        try {

            br = new CmdUtil(null).getBufferedReader(cmd);

            String line;

            while ((line = br.readLine()) != null) {
                logger.info(line+"\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String args[]) {

        new AdbLog("adb -s logcat -b main -b system -b events -b radio *:I").start();

    }

}
