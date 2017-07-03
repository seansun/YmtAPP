package com.ymt.engine;

import com.ymt.entity.Action;
import com.ymt.entity.Step;
import com.ymt.tools.AdbUtils;
import com.ymt.tools.CmdUtil;
import com.ymt.tools.LimitQueue;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

public class Engine {


    private static final Logger logger = LoggerFactory.getLogger(Engine.class);


    public AndroidDriver driver;

    public static String udid;

    public LimitQueue<Step> results;

    public AdbUtils adbUtils;

    //当前年月日时间
    public static String currentTime = new SimpleDateFormat("yyyyMMdd").format(new Date());

    //单次任务保存截图的index
    private Integer screenIndex = 1;

    //单次任务最多保存截图的上限
    private int MAX_SCREENSHOT_COUNT = 15;

    //当前任务的序号
    public static int taskId = 0;

    // 截图文件路径
    public static String SCREENSHOT_PATH = null;

    //默认的等待控件时间
    private static int WAIT_TIME = 3;

    //默认滑动百分比
    private final int SWIPE_DEFAULT_PERCENT = 5;

    //默认滑动持续时间500ms
    private final int SWIPE_DURING = 100;

    public static int width;

    public static int height;

    public Engine() {

    }

    public Engine(AndroidDriver driver, LimitQueue<Step> results) {

        //设置存储操作步骤 Queue 长度
        results.setLimit(MAX_SCREENSHOT_COUNT);

        this.driver = driver;
        this.results = results;

        this.width = driver.manage().window().getSize().getWidth();
        ;
        this.height = driver.manage().window().getSize().getHeight();

        this.udid = driver.getCapabilities().getCapability("deviceName").toString();

        logger.info("当前设备号 udid:{}", udid);

        adbUtils = new AdbUtils(udid);

        taskId++;

        //截图地址加上当前时间，当前的执行taskid
        SCREENSHOT_PATH = System.getProperty("user.dir")
                + File.separator + String.format("results\\%s\\screenshots\\%s\\", currentTime, getTaskId());

        try {
            FileUtils.forceMkdir(new File(SCREENSHOT_PATH));
        } catch (IOException e) {
            //e.printStackTrace();
            logger.error("创建截图文件路径:{}失败", SCREENSHOT_PATH);
        }

        logger.info("保存截图的位置为:{}", SCREENSHOT_PATH);


    }

    public int getScreenIndex() {
        return screenIndex;
    }

    public int getMaxScreenshotCount() {
        return MAX_SCREENSHOT_COUNT;
    }

    public int getTaskId() {

        return taskId;
    }

    /**
     * 显示等待，等待Id对应的控件出现time秒，一出现马上返回，time秒不出现也返回
     */
    public AndroidElement waitAuto(By by, int time) {
        try {
            return new AndroidDriverWait(driver, time)
                    .until(new ExpectedCondition<AndroidElement>() {
                        @Override
                        public AndroidElement apply(AndroidDriver androidDriver) {
                            return (AndroidElement) androidDriver.findElement(by);
                        }
                    });
        } catch (TimeoutException e) {

            logger.warn("查找元素超时!!{}秒之后还没找到元素 [{}]", time, by.toString());

            return null;

        }
    }

    public AndroidElement waitAutoById(String id) {
        return waitAutoById(id, WAIT_TIME);
    }

    public AndroidElement waitAutoById(String id, int time) {
        return waitAuto(By.id(id), time);
    }

    public AndroidElement waitAutoByXp(String xPath) {
        return waitAutoByXp(xPath, WAIT_TIME);
    }

    public AndroidElement waitAutoByXp(String xPath, int time) {
        return waitAuto(By.xpath(xPath), time);
    }


    public String takeScreenShot() {

        String screenShotName = "screenShot" + screenIndex;

        screenShot(screenShotName);

        screenIndex++;

        if (screenIndex > MAX_SCREENSHOT_COUNT) screenIndex = 1;

        return screenShotName;
    }


    /**
     * 截图 由子类实现
     */
    public void screenShot(String fileName) {


    }

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public int getScreenWidth() {
        return width;
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public int getScreenHeight() {
        return height;
    }

    /**
     * 元素点击
     */
    public void click(AndroidElement element) {

        String result = "pass";
        Step step = new Step();
        String screenShotName = null;

        try {

            Point location = element.getLocation();

            int x = location.getX();
            int y = location.getY();

            Dimension size = element.getSize();

            int w = size.getWidth();
            int h = size.getHeight();

            step.setX(x);
            step.setY(y);
            step.setW(w);
            step.setH(h);

            logger.info("随机点击控件tagName :{} ", element.getTagName());
            logger.info("随机点击控件Location :{} ", element.getLocation());

            //截图
            screenShotName = takeScreenShot();

            logger.info("点击截图:{}", screenShotName);


        } catch (Exception e) {

            logger.error("截图失败:{}", e);

            screenShotName = null;

            step.setResult(String.format("截图失败 %s", e.getStackTrace().toString()));
        }

        step.setElementName(element.getTagName());

        step.setAction("Click");

        step.setScreenShotName(screenShotName);

        try {
            element.click();
        } catch (Exception e) {

            logger.error("Click element :{} error :{}", element, e);

            step.setResult((CmdUtil.isWindows() ? "//r//n" : "//r") + e.getStackTrace().toString());

            result = "fail";
        }

        step.setResult(result);

        results.offer(step);

    }


    public void swipeToUp(int during) {
        swipeToUp(during, SWIPE_DEFAULT_PERCENT);
    }

    /**
     * 向上滑动，
     *
     * @param during
     */
    public void swipeToUp(int during, int percent) {
        int width = this.width;
        int height = this.height;

        driver.swipe(width / 2, height * (percent - 1) / percent, width / 2, height / percent, during);
    }

    public void swipeToDown(int during) {
        swipeToDown(during, SWIPE_DEFAULT_PERCENT);

    }

    public void swipeToLeft(int during) {

        swipeToLeft(during, SWIPE_DEFAULT_PERCENT);


    }

    /**
     * 向下滑动，
     *
     * @param during 滑动时间
     */
    public void swipeToDown(int during, int percent) {
        int width = this.width;
        int height = this.height;

        driver.swipe(width / 2, height / percent, width / 2, height * (percent - 1) / percent, during);
    }


    /**
     * 向左滑动，
     *
     * @param during  滑动时间
     * @param percent 位置的百分比，2-10， 例如3就是 从2/3滑到1/3
     */
    public void swipeToLeft(int during, int percent) {
        int width = this.width;
        int height = this.height;

        driver.swipe(width * (percent - 1) / percent, height / 2, width / percent, height / 2, during);
    }


    public void swipeToRight(int during) {
        swipeToRight(during, SWIPE_DEFAULT_PERCENT);
    }

    /**
     * 向右滑动，
     *
     * @param during  滑动时间
     * @param percent 位置的百分比，2-10， 例如3就是 从1/3滑到2/3
     */
    public void swipeToRight(int during, int percent) {
        int width = this.width;
        int height = this.height;
        driver.swipe(width / percent, height / 2, width * (percent - 1) / percent, height / 2, during);
    }

    /**
     * 在某个方向上滑动
     *
     * @param direction 方向，UP DOWN LEFT RIGHT
     * @param duration  持续时间
     */
    public void swip(String direction, int duration) {

        String result = "pass";

        Step step = new Step();

        try {

            switch (direction) {
                case Action.SWIP_UP:
                    swipeToUp(duration);
                    break;
                case Action.SWIP_DOWN:
                    swipeToDown(duration);
                    break;
                case Action.SWIP_LEFT:
                    swipeToLeft(duration);
                    break;
                case Action.SWIP_RIGHT:
                    swipeToRight(duration);
                    break;
            }
        } catch (Exception e) {

            logger.error("Event {} error :{}", direction, e);

            result = "fail";

            step.setResult(e.getStackTrace().toString());

        }

        String screenShotName = null;

        logger.info(" Event : {} ,duration {}", direction, duration);

        try {

            //截图
            screenShotName = takeScreenShot();

            logger.info("点击截图:{}", screenShotName);

        } catch (Exception e) {

            logger.error("截图失败:{}", e);

            screenShotName = null;

            step.setResult(String.format("截图失败 %s", e.getStackTrace().toString()));
        }

        step.setElementName("Page");
        step.setAction(direction);
        step.setScreenShotName(screenShotName);
        step.setResult(result);

        results.offer(step);
    }


    public void clickScreen(int x, int y) {

        String result = "pass";

        Step step = new Step();
        //截图
        String screenShotName = takeScreenShot();

        TouchAction action = new TouchAction(this.driver);

        logger.info("Event 点击屏幕 x:{} ,y:{} ", x, y);

        step.setElementName("Page");
        step.setAction(Action.CLICK_SCREEN);
        step.setX(x);
        step.setY(y);
        step.setScreenShotName(screenShotName);
        step.setResult(result);

        action.tap(x, y).perform();

        results.offer(step);

    }


    /**
     * 操作方法
     */
    public void doElementAction(AndroidElement element, String nextAction) {

        logger.info("element:{},nextAction:{}", element, nextAction);

        switch (nextAction) {

            case Action.CLICK:
                click(element);
                break;
            case Action.BACK:
                back();
                break;
            case Action.BACK_APP:
                backApp();
                break;
            case Action.LAUNCH_APP:
                driver.launchApp();
                break;
            case Action.SKIP:

                break;
            default:
                swip(nextAction, SWIPE_DURING);
                break;
        }

    }

    /**
     * 尝试返回app
     */
    private void backApp() {

        logger.info("Event 尝试返回 app");

        driver.navigate().back();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 尝试后退
     */
    public void homePress() {


    }

    /**
     * 尝试后退
     */
    public void back() {

    }

    /**
     * 获取页面pagesource 超时10s返回
     */
    public String getPageSource() {

        logger.info("开始获取pagesource");

        String source = null;

        //source=driver.getPageSource();

        final ExecutorService exec = Executors.newFixedThreadPool(1);

        Callable<String> call = new Callable<String>() {

            public String call() throws Exception {
                //开始执行耗时操作
                return driver.getPageSource();
            }

        };
        try {
            Future<String> future = exec.submit(call);

            source = future.get(10000, TimeUnit.MILLISECONDS); //任务处理超时时间设为 10 秒

            logger.debug("pagesource 返回:{}", source);

        } catch (java.util.concurrent.TimeoutException ex) {
            logger.info("{} 获取pagesource超时");

        } catch (Exception e) {
            logger.error("获取pagesource 处理失败");
        }
        // 关闭线程池
        //exec.shutdown();
        exec.shutdownNow();

        return source;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        System.out.println();

    }

}
