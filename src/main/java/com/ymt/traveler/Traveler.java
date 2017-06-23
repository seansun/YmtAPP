package com.ymt.traveler;

import com.ymt.entity.*;
import com.ymt.operation.OperateAppium;
import com.ymt.tools.*;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * Created by sunsheng on 2017/5/26.
 */
public class Traveler {

    private static final Logger logger = LoggerFactory.getLogger(Traveler.class);

    public DataRecord record = new DataRecord();

    public List<Step> results = new ArrayList<Step>();

    // 种子值
    public long seed = System.currentTimeMillis();
    // 随机数生成器
    public Random random = new Random(seed);

    public List<String> xpathList = null;

    public OperateAppium operateAppium;

    //可以点击的控件
    public List<String> clickList = new ArrayList<String>();

    public List<String> blackList = new ArrayList<String>();

    public long beginTime;

    private static Map<String, Integer> pageCount = new HashMap<String, Integer>();

    private static String currentPageAction = Action.CLICK;

    public AndroidDriver driver;

    public Config config;

    public Traveler() {

        loadConfig();

    }

    /**
     * 加载yaml 配置文件
     */
    public void loadConfig() {

        config = YamlUtil.loadYaml();

        clickList = config.getClickList();

        blackList = config.getBlackList();

    }

    public void setupDriver() {

        startAppiumServer();


    }


    public void afterTravel() {

        try {

            driver.quit();

        } catch (Exception e) {

            logger.error("driver.quit 出现异常，{}", e.getStackTrace());

        } finally {

            record.setDuringTime(String.format("%s s", (System.currentTimeMillis() - beginTime) / 1000));

            getLog();

            record.setPageCount(pageCount);

            record.setResults(results);

            new Report().generateReport(record, "YMT", operateAppium.getMaxScreenshotCount());

            cleanEnv();

        }
    }

    public void beforeTravel() {

        beginTime = System.currentTimeMillis();

    }


    /**
     * 开始随机遍历
     */
    public boolean start() {

        boolean isNeedRetry = false;

        try {

            setupDriver();

            beforeTravel();

            logger.debug("Current PageSource:{}", driver.getPageSource());

            //主页面 activity name
            String mainActivity = driver.currentActivity();

            logger.info("主界面 mainActivity:{}", mainActivity);

            while (true) {

                getPageInfo();

                AndroidElement element = null;

                if (refreshPage()) {

                    element = beforeAction();

                }

                operateAppium.doElementAction(element, getPageAction());

            }
        } catch (Exception e) {

            logger.error("遍历出现异常:{}", e.getStackTrace());

            isNeedRetry = true;

        } finally {

            afterTravel();

        }

        return isNeedRetry;

    }

    /**
     * 刷新页面
     */
    private boolean refreshPage() {

        boolean isSuccess = true;

        try {

            setPageAction(getNextAction());

            if (getPageAction().equals(Action.CLICK)) {

                String xpath = "//*[@clickable='true' and @enabled='true']";

                String pageSource = operateAppium.getPageSource();

                //解析 pagesource 超时
                if (null == pageSource) {

                    //android 解析失败，重新尝试 launch app
                    setPageAction(Action.LAUNCHAPP);
                    logger.info("driver:{} ,解析 pageSource超时,pageSource为null", driver);
                    return false;
                }

                logger.debug("page source :{}", pageSource);

                XmlParser xmlParser = new XmlParser(pageSource);

                //触发条件
                if (triggerProcessing(xmlParser)) {

                    setPageAction(Action.SKIP);

                    return false;
                }

                NodeList nodeList = xmlParser.getNodeListFromXPath(xpath);

                List<Map<String, String>> pageAttributes = xmlParser.getAttributes(nodeList);

                if (CollectionUtils.isEmpty(pageAttributes)) {
                    setPageAction(Action.DOBACK);

                    return false;
                }
                if (!getValidElement(pageAttributes)) {

                    setPageAction(Action.DOBACK);

                    return false;
                }

                xpathList = xmlParser.getXpathList(pageAttributes);

                String appName = xmlParser.getAppName();

                logger.info("app name: {}", appName);

                if (!appName.equals("com.ymatou.shop")) {

                    afterTravel();

                    System.exit(-1);

                    setPageAction(Action.BACKAPP);

                    logger.info("currentActivity: {},appName:{} ", appName, driver.currentActivity());

                    return false;

                }

            }

        } catch (Exception e) {

            logger.error("refresh error,{ }", e);

            isSuccess = false;

        }
        return isSuccess;
    }

    /**
     * android 统计页面 activity 访问次数
     */
    private void getPageInfo() {

        String pageUrl = driver.currentActivity();

        if (pageCount.containsKey(pageUrl)) {

            pageCount.put(pageUrl, pageCount.get(pageUrl) + 1);

        } else
            pageCount.put(pageUrl, 1);

    }

    /**
     * 随机动作
     */
    private AndroidElement beforeAction() {

        AndroidElement element = null;

        if (getPageAction().equals(Action.CLICK)) {

            if (!CollectionUtils.isEmpty(xpathList)) {

                int index = new Random().nextInt(xpathList.size());

                String xpath = xpathList.get(index);

                logger.info("元素定位xpath为:{}", xpath);

                element = operateAppium.waitAutoByXp(xpath);

                if (null == element) {

                    logger.info("元素 element为:null return");

                    setPageAction(Action.DOBACK);

                    return element;

                }

            } else {

                logger.info("xpathList : {} is null ", xpathList);
            }
        }
        return element;
    }

    /**
     * 启动appium server 服务
     */
    public void startAppiumServer() {

        logger.info("启动appium server 服务");

        new AppiumServer("appium").start();

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 清理环境
     */
    public void cleanEnv() {

    }

    /**
     * 检查当前控件是否在可点击列表中
     */
    private boolean getValidElement(List<Map<String, String>> pageAttributes) {

        boolean flag = false;

        if (CollectionUtils.isEmpty(pageAttributes)) {

            logger.info("pageAttributes 当前页面元素属性列表大小为0");

            return flag;

        }

        Iterator<Map<String, String>> it = pageAttributes.iterator();

        while (it.hasNext()) {

            Map<String, String> elementAttribute = it.next();

            String tag = elementAttribute.get("tag");

            if (!clickList.contains(tag)) {

                it.remove();

                continue;
            }

            if (elementAttribute.containsKey("text")) {

                String text = elementAttribute.get("text");

                if (blackList.contains(text)) {

                    it.remove();

                    continue;
                }
            }

            if (elementAttribute.containsKey("resource-id")) {

                String resourceid = elementAttribute.get("resource-id");

                if (blackList.contains(resourceid)) {

                    it.remove();

                    continue;
                }
            }


        }

        logger.info("当前页面过滤掉不能点击的元素后可操作元素大小为:{}", pageAttributes.size());

        if (pageAttributes.size() > 0) {
            flag = true;
        }

        return flag;
    }

    /**
     * 获取下一个随机动作
     *
     * @return
     */
    private String getNextAction() {

        String nextAction = null;

        int action = Math.abs(random.nextInt() % 19);

        switch (action) {

            case 0:
                nextAction = Action.SWIP_LEFT;
                break;
            case 1:
                nextAction = Action.SWIP_RIGHT;
                break;
            case 2:
            case 3:
                nextAction = Action.SWIP_DOWN;
                break;
            case 4:
            case 5:
                nextAction = Action.SWIP_UP;
                break;
            default:
                nextAction = Action.CLICK;
                break;

        }
        logger.info("next action is : {}", nextAction);

        return nextAction;

    }

    /**
     * 设置页面随机动作
     */
    public void setPageAction(String action) {

        currentPageAction = action;

    }

    /**
     * 获取页面随机动作
     */
    public String getPageAction() {

        return currentPageAction;
    }

    /**
     * 解析配置文件的触发动作
     */
    public boolean triggerProcessing(XmlParser xmlParser) {

        boolean result = false;

        if (null == xmlParser) return false;

        List<TriggerAction> triggerActionList = config.getTriggerActions();

        for (TriggerAction triggerAction : triggerActionList) {

            String xpath = triggerAction.getTriggerCondition();

            NodeList nodeList = xmlParser.getNodeListFromXPath(xpath);

            if (null == nodeList) continue;

            if (nodeList.getLength() == 0) {

                continue;

            } else {

                List<Operate> operateList = triggerAction.getActionList();

                for (Operate operate : operateList) {

                    String action = operate.getAction();

                    if (action.equalsIgnoreCase(Action.DOBACK)) {

                        logger.info("triggerProcessing DOBACK");

                        driver.pressKeyCode(4);

                        result = true;

                    }
                    if (action.equalsIgnoreCase(Action.CLICK)) {

                        logger.info("triggerProcessing CLICK");

                        AndroidElement element = operateAppium.waitAutoByXp(operate.getXpath(), 1);

                        if (element != null) {

                            element.click();
                        }
                        result = true;

                    }


                }

            }

        }

        return result;
    }


    public void getLog() {
    }


    public static void main(String... args) {

        Traveler traveler = new AndroidTraveler();

        boolean result = traveler.start();


        while (result) {

            result = traveler.start();

        }


    }

}

