package com.ymt.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CmdUtil {


    private static final Logger logger = LoggerFactory.getLogger(CmdUtil.class);

    private String deviceId;

    public CmdUtil(String deviceId) {

        this.deviceId = deviceId;

    }

    /**
     * 调用并执行控制台命令
     *
     * @param cmd 控制台命令
     * @return output
     */
    public String runAdbCmd(String cmd) {

        String adbCmd = String.format("adb %s", cmd);

        return this.run(adbCmd);
    }


    /**
     * 调用并执行控制台命令
     *
     * @param cmd 控制台命令
     * @return output
     */
    public String run(String cmd) {
        String line;
        String cmdOut = "";
        BufferedReader br = null;
        try {

            br = getBufferedReader(cmd);

            while ((line = br.readLine()) != null) {
                cmdOut = cmdOut + line + System.getProperty("line.separator");
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

        logger.debug("执行的cmd 命令为 : {}", cmd);
        logger.debug("执行的cmd 命令结果为 : {}", cmdOut);
        return null == cmdOut ? null : cmdOut.trim();
    }

    /**
     * 判断是否Windows操作系统
     *
     * @return 是否windows系统
     */
    public static boolean isWindows() {
        String os = System.getProperty("os.name");
        return (os.toLowerCase().startsWith("win")) ? true : false;
    }

    public BufferedReader getBufferedReader(String cmd) {

        BufferedReader br = null;
        Process p;
        try {
            if (isWindows()) {
                String command = "cmd /c " + cmd;

                p = Runtime.getRuntime().exec(command);

            } else {
                String[] shell = {"sh", "-c", cmd};

                p = Runtime.getRuntime().exec(shell);

            }
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return br;

    }


    public static void main(String args[]) {

        //new CmdUtil().getUdid(CmdConfig.UUID_ANDROID);
        //logger.info("cmd return : {}", uuid);

        //logger.info("result : {}",new CmdUtil().run("taskkill /im appium"));

        //new CmdUtil("94fabcfa").runAdbCmd("start-server");
        CmdUtil cmd = new CmdUtil(null);

        cmd.run("taskkill /f /t /im cmd.exe");
        cmd.run("taskkill /f /t /im adb.exe");
        cmd.run("taskkill /f /t /im conhost.exe");



    }
}