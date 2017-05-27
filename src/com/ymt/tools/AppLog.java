package com.ymt.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;

/**
 * Created by sunsheng on 2017/4/19.
 */
public class AppLog extends Thread{

    private static final Logger logger = LoggerFactory.getLogger("applog");

    private String cmd;

    public AppLog(String cmd) {

        cleanLogcat();

        this.cmd = cmd;
    }

    public void cleanLogcat(){

        new CmdUtil(null).runAdbCmd("logcat -c");

    }


    @Override
    public void run() {

        cmdInvoke(cmd);

    }

        private void cmdInvoke(String cmd) {

            BufferedReader br = null;

            try {

                br= new CmdUtil(null).getBufferedReader(cmd);

                String line;

                while ((line = br.readLine()) != null) {
                    logger.info(line);
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

    public static void main(String args[]){

       new AppLog("adb -s logcat -b main -b system -b events -b radio *:I").start();

    }

}
