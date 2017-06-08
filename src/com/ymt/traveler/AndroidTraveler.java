package com.ymt.traveler;

import com.android.ddmlib.IDevice;
import com.ymt.entity.AndroidCapability;
import com.ymt.entity.Device;
import com.ymt.operation.OperateAppium;
import com.ymt.tools.AdbUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by sunsheng on 2017/5/26.
 */
public class AndroidTraveler extends Traveler {

    private static final Logger logger = LoggerFactory.getLogger(AndroidTraveler.class);

    private AndroidCapability androidCapability;

    public AndroidTraveler() {

        this.androidCapability = config.getAndroidCapability();

    }

    @Override
    public void setupAppium() {

        List<Device> deviceList = androidCapability.getDeviceNames();

        String appActivity = androidCapability.getAppActivity();

        String appPackage = androidCapability.getAppPackage();

        IDevice[] activityDevices = new AdbUtils().getDevices();

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

            if (device.getDeviceName().equals(activityDevices[0].toString())) {

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

            operateAppium=new OperateAppium(driver,results);

        } catch (MalformedURLException e) {
            e.printStackTrace();

            operateAppium=new OperateAppium(null,results);

            logger.error("加载 AndroidDriver 失败,{}",e);

        }

    }


    public static void main(String... args) {

        new AndroidTraveler();

    }
}
