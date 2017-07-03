package com.ymt.engine;

import com.ymt.entity.Action;
import com.ymt.entity.Step;
import com.ymt.tools.LimitQueue;
import io.appium.java_client.android.AndroidDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by sunsheng on 2017/6/27.
 */
public class AndroidEngine extends Engine{

    private static final Logger logger = LoggerFactory.getLogger(Engine.class);

    private static final int  KEYCODE_HOME=3,KEYCODE_MENU=82,KEYCODE_BACK=4;


    public AndroidEngine(AndroidDriver driver, LimitQueue<Step> results){

        super(driver, results);
    }
    /**
     * 截图
     */
    @Override
    public void screenShot(String fileName) {
        logger.info("截图开始");
        adbUtils.screencap(fileName);
        logger.info("截图结束");

    }


    /**
     * 尝试后退
     */
    @Override
    public void homePress() {

        String result = "pass";

        Step step = new Step();
        //截图
        String screenShotName = takeScreenShot();

        logger.info("Event KEYCODE_HOME");

        driver.pressKeyCode(KEYCODE_HOME);


        step.setElementName("Page");
        step.setAction(Action.HOME_PRESS);
        step.setScreenShotName(screenShotName);
        step.setResult(result);

        results.offer(step);

        driver.launchApp();

        try {
            Thread.sleep(200);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 尝试后退
     */
    @Override
    public void back() {
        String result = "pass";

        Step step = new Step();
        //截图
        String screenShotName = takeScreenShot();

        logger.info("Event KEYCODE_BACK");

        driver.pressKeyCode(KEYCODE_BACK);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        step.setElementName("Page");
        step.setAction(Action.BACK);
        step.setScreenShotName(screenShotName);
        step.setResult(result);

        results.offer(step);

    }
}
