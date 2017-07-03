package com.ymt.traveler;

import com.ymt.engine.Engine;
import com.ymt.entity.*;
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

    public LimitQueue<Step> results = new LimitQueue<Step>();

    // 种子值
    public long seed = System.currentTimeMillis();
    // 随机数生成器
    public Random random = new Random(seed);

    public List<String> xpathList = null;

    public Engine engine;

    public static int eventcount = 0;

    //可以点击的控件
    public List<String> clickList = new ArrayList<String>();

    public List<String> blackList = new ArrayList<String>();

    public long startTime;

    public static Map<String, Integer> pageCount = new HashMap<String, Integer>();

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

            record.setDuringTime(String.format("%s s", (System.currentTimeMillis() - startTime) / 1000));

            getLog();

            record.setTotalStep(eventcount);

            record.setPageCount(pageCount);

            record.setResults(results);

            new Report().generateReport(record, "YMT");

            cleanEnv();

        }
    }

    public void beforeTravel() {

        startTime = System.currentTimeMillis();

    }

    /**
     * 开始随机遍历
     */
    public boolean start() {
        return true;


    }

    /**
     * 刷新页面
     */
    public boolean refreshPage() {

        boolean isSuccess = true;

        try {

            setPageAction(getNextAction());

            if (getPageAction().equals(Action.CLICK)) {

                String xpath = "//*[@clickable='true' and @enabled='true']";

                String pageSource = engine.getPageSource();

                //解析 pagesource 超时
                if (null == pageSource) {

                    //android 解析失败，重新尝试 launch app
                    setPageAction(Action.LAUNCH_APP);
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
                    setPageAction(Action.BACK);

                    return false;
                }
                if (!getValidElement(pageAttributes)) {

                    setPageAction(Action.BACK);

                    return false;
                }

                xpathList = xmlParser.getXpathList(pageAttributes);

                String appName = xmlParser.getAppName();

                logger.info("app name: {}", appName);

                if (!appName.equals("com.ymatou.shop")) {

                    afterTravel();

                    System.exit(-1);

                    setPageAction(Action.BACK_APP);

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
     * 统计页面 page 访问次数
     */
    public void getPageInfo() {

    }

    /**
     * 随机动作
     */
    public AndroidElement beforeAction() {

        AndroidElement element = null;

        if (getPageAction().equals(Action.CLICK)) {

            if (!CollectionUtils.isEmpty(xpathList)) {

                int index = new Random().nextInt(xpathList.size());

                String xpath = xpathList.get(index);

                logger.info("元素定位xpath为:{}", xpath);

                element = engine.waitAutoByXp(xpath);

                if (null == element) {

                    logger.info("元素 element为:null return");

                    setPageAction(Action.BACK);

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

                    if (action.equalsIgnoreCase(Action.BACK)) {

                        logger.info("triggerProcessing DOBACK");

                        driver.pressKeyCode(4);

                        result = true;

                    }
                    if (action.equalsIgnoreCase(Action.CLICK)) {

                        logger.info("triggerProcessing CLICK");

                        AndroidElement element = engine.waitAutoByXp(operate.getXpath(), 1);

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


    }

}

