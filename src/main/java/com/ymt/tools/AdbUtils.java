package com.ymt.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sunsheng on 2017/5/10.
 */
public class AdbUtils extends Thread {

    private static final Logger logger = LoggerFactory.getLogger("adbLog");

    private CmdUtil cmdUtil;

    //单个设备，可不传入参数deviceId
    private String deviceId;

    private String findUtil = CmdUtil.isWindows() ? "findstr" : "grep";


    public AdbUtils() {

        this.deviceId = null;
        init();
    }

    public AdbUtils(String deviceId) {

        this.deviceId = deviceId;
        init();

    }

    private void init() {

        cmdUtil = new CmdUtil(this.deviceId);

        //启动adb 服务
        cmdUtil.runAdbCmd("start-server");

    }

    // 获取连接的设备列表
    public List<String> getDevices() {

        logger.info("获取当前活动的device列表");

        String cmd = String.format("adb devices|%s -v List", findUtil);

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

        String udid = "2bad9d02";

        String cmd = String.format("adb -s %s logcat -b main -b system -b events -b radio *:I", udid);

        cleanLogcat();

        getLogcatLog(cmd);

    }

    public void cleanLogcat() {

        cmdUtil.runAdbCmd("logcat -c");

    }

    private void getLogcatLog(String cmd) {

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

    /**
     * 根据app packageName，获取版本号
     */
    public String getAppVersion(String packageName) {

        String appVersion = null;

        String lines = cmdUtil.runAdbShell(String.format("dumpsys package %s", packageName));

        if (!lines.isEmpty()) {

            String appVerdions[] = lines.split(System.getProperty("line.separator"));

            for (String line : appVerdions) {

                if (line.trim().contains("versionName")) {

                    appVersion = line.split("=")[1].trim();

                }
            }


        }

        return appVersion;

    }

    /**
     * 获取 Device 设备名
     */
    public String getDeviceName() {

        return cmdUtil.runAdbShell("getprop ro.product.model");

    }

    /**
     * 获取设备中的Android版本号
     */
    public String getAndroidVersion() {

        return cmdUtil.runAdbShell("getprop ro.build.version.release");

    }

    /**
     * 删除tmp 下的截图文件
     */
    public void delTmpScreenFile(){

        cmdUtil.runAdbShell("rm -r /data/local/tmp/*.png");
    }

    /**
     * 获取设备屏幕分辨率，return (width, high)
     */
    public String getScreenResolution() {

        String resolution = "";

        Pattern pattern = Pattern.compile("\\d+");

        String lines = cmdUtil.runAdbShell(String.format("dumpsys display | %s PhysicalDisplayInfo", findUtil));

        Matcher matcher = pattern.matcher(lines);

        int i = 0;

        while (matcher.find()) {

            resolution = resolution + matcher.group();

            if (i == 1) break;

            resolution = resolution + "x";

            i++;
        }

        return resolution;

    }

    /**
     * 通过adb 截图
     */
    public void screencap(String fileName) {

        cmdUtil.runAdbShell(String.format("/system/bin/screencap -p /data/local/tmp/%s.png", fileName));

    }

    /**
     * 将截图文件从手机pull到本地
     */
    public void pullScreen(String filename, String computerPath) {

        computerPath = computerPath.replace("\\", "/");

        cmdUtil.runAdbCmd(String.format("pull /data/local/tmp/%s.png %s", filename, computerPath));

    }

    /**
     * kill adb 进程
     */
    public void killAdb() {

        cmdUtil.run("taskkill /f /t /im cmd.exe");
        cmdUtil.run("taskkill /f /t /im adb.exe");
        cmdUtil.run("taskkill /f /t /im conhost.exe");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String... args) {

        //new AdbUtils().start();
        //System.out.println("version:" + new AdbUtils("2bad9d02").getScreenResolution());

        AdbUtils adbUtils = new AdbUtils("2bad9d02");

        for (int i = 0; i < 30; i++) {
            //adbUtils.screencap(String.valueOf(i));

            adbUtils.pullScreen("screenshot" + i, "C:\\Users\\sunsheng\\Desktop\\YmtAPP\\results\\20170622\\screenshots\\0\\" + i + ".png");

        }


    }
}

