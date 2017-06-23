package com.ymt.traveler;

import com.ymt.entity.AndroidCapability;
import com.ymt.entity.Constant;
import com.ymt.entity.Device;
import com.ymt.operation.OperateAppium;
import com.ymt.tools.AdbUtils;
import com.ymt.tools.CmdUtil;
import com.ymt.tools.FileUtil;
import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.collections.CollectionUtils;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by sunsheng on 2017/5/26.
 */
public class AndroidTraveler extends Traveler {

    private static final Logger logger = LoggerFactory.getLogger(AndroidTraveler.class);

    private AdbUtils adbUtils;

    private AndroidCapability androidCapability;

    public AndroidTraveler() {

        this.androidCapability = config.getAndroidCapability();

    }


    @Override
    public void setupDriver() {

        super.setupDriver();

        List<Device> deviceList = androidCapability.getDeviceNames();

        String appActivity = androidCapability.getAppActivity();

        String appPackage = androidCapability.getAppPackage();

        //获取 Devices list
        List<String> activityDevices = new AdbUtils().getDevices();

        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability(CapabilityType.BROWSER_NAME, "");

        capabilities.setCapability("platformName", "Android");

        //capabilities.setCapability("automationName", "Selendroid");
        //
        capabilities.setCapability("automationName", "Appium");

        //capabilities.setCapability("unicodeKeyboard", "true");

        //设置收到下一条命令的超时时间,超时appium会自动关闭session 30s
        capabilities.setCapability("newCommandTimeout", "30");

        String deviceName = null;
        String platformVersion = null;
        String url = null;

        for (Device device : deviceList) {

            if (device.getDeviceName().equals(activityDevices.get(0))) {

                deviceName = device.getDeviceName();
                platformVersion = device.getPlatformVersion();
                url = device.getAppium();

            }

        }

        capabilities.setCapability("deviceName", deviceName);
        capabilities.setCapability("platformVersion", platformVersion);

        //不需要再次安装
        capabilities.setCapability("noReset", true);
        // 自动接受提示信息
        capabilities.setCapability("autoAcceptAlerts", true);

        //capabilities.setCapability("app", app.getAbsolutePath());

        capabilities.setCapability("appPackage", appPackage);
        capabilities
                .setCapability("appActivity", appActivity);


        try {

            driver = new AndroidDriver(new URL(url),
                    capabilities);


            operateAppium = new OperateAppium(driver, results);


            adbUtils = new AdbUtils(deviceName);

            //抓取adb logcat 日志
            adbUtils.start();


        } catch (MalformedURLException e) {
            e.printStackTrace();

            operateAppium = new OperateAppium(null, results);

            logger.error("加载 AndroidDriver 失败,{}", e);

        }

        record.setAppInfo(String.format("appPackageName %s,appVersion %s", appPackage, adbUtils.getAppVersion(appPackage)));

        record.setDeviceName(String.format("deviceName %s,systemVersion %s,resolution %s", adbUtils.getDeviceName(), adbUtils.getAndroidVersion(), adbUtils.getScreenResolution()));

    }

    /***
     * android 提取appium,adb 日志
     */
    @Override
    public void getLog() {

        String logPath = Constant.getResultPath().getPath() + File.separator + "logs/";

        String appiumLogPath = logPath + "appium.log";

        String adbLogPath = logPath + "adb.log";

        int lastLineNum = 20;
        //处理appium log 日志
        List<String> appiumLog = FileUtil.readLastNLine(new File(appiumLogPath), lastLineNum);

        StringBuilder sbApp = new StringBuilder();

        sbApp.append(String.format("**********appium log最后%s行日志**********<br/>\n", lastLineNum));
        appiumLog.forEach(s -> {
            sbApp.append(s);
            sbApp.append("<br/>");

        });

        //处理app log 日志
        List<String> adbLog = FileUtil.getErrorLine(new File(adbLogPath));
        List<String> adbLog2 = FileUtil.readLastNLine(new File(adbLogPath), lastLineNum);

        StringBuilder sbAdb = new StringBuilder();

        if (!CollectionUtils.isEmpty(adbLog)) {
            sbAdb.append("**********adb log日志**********<br/>\n");
            adbLog.forEach(s -> {
                sbAdb.append(s);
                sbAdb.append("<br/>");
            });
        }

        sbAdb.append(String.format("**********adb log最后%s行日志**********<br/>\n", lastLineNum));
        adbLog2.forEach(s -> {
            sbAdb.append(s);
            sbAdb.append("<br/>");
        });

        record.setAppiumLog(sbApp.toString());
        logger.info(sbApp.toString());
        record.setAppLog(sbAdb.toString());
        logger.info(sbAdb.toString());

    }

    /**
     *
     */
    @Override
    public void beforeTravel() {

        super.beforeTravel();

        adbUtils.delTmpScreenFile();

    }

    /**
     * 清理环境
     */
    @Override
    public void cleanEnv() {

        adbUtils.killAdb();

    }

    public static void main(String... args) {

        new AndroidTraveler().getLog();

    }
}
