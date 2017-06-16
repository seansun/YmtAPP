package com.ymt.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunsheng on 2017/5/10.
 */
public class AdbUtils extends Thread {

    private static final Logger logger = LoggerFactory.getLogger("adbLog");

    private CmdUtil cmdUtil;

    public AdbUtils() {

        cmdUtil = new CmdUtil(null);

        init();

    }

    private void init() {

        //启动adb 服务
        cmdUtil.runAdbCmd("start-server");

    }

    // 获取连接的设备列表
    public List<String> getDevices() {
        String cmd = "adb devices|findstr -v List";

        String line;

        List<String> uuidList = new ArrayList<String>();

        BufferedReader br = null;

        try {

            br = cmdUtil.getBufferedReader(cmd);

            while ((line = br.readLine()) != null) {
                if (line.length() > 0) {
                    line = line.split("\\t")[0];
                    uuidList.add(line.trim());
                }
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
        return uuidList;

    }


    @Override
    public void run() {

        String udid="2bad9d02";

        String cmd=String.format("adb -s %s logcat -b main -b system -b events -b radio *:I",udid);

        cleanLogcat();

        getLogcatLog(cmd);

    }

    public void cleanLogcat() {

        cmdUtil.runAdbCmd("logcat -c");

    }

    private void getLogcatLog(String cmd){

        BufferedReader br = null;

        try {

            br = cmdUtil.getBufferedReader(cmd);

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


    public static void main(String ...args) {

        new AdbUtils().start();

    }
}

