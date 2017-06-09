package com.ymt.operation;

import com.ymt.entity.Action;
import com.ymt.entity.CmdConfig;
import com.ymt.entity.Step;
import com.ymt.tools.CmdUtil;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.*;

public class OperateAppium {

	private static final Logger logger = LoggerFactory.getLogger(OperateAppium.class);

	private AndroidDriver driver;

	private String udid;

	private List<Step> results;

	private CmdUtil cmd=new CmdUtil(null);

	private static int screenIndex=0;

	private static int MAX_SCREENSHOT_COUNT=9;

	// 截图文件路径
	public static final String SCREENSHOT_PATH = (System.getProperty("user.dir")
			+ File.separator + "results/screenshots/").replace("\\","/");

	//默认的等待控件时间
	private static int WAIT_TIME = 10;

	//默认滑动百分比
	private final int SWIPE_DEFAULT_PERCENT = 5;

	//默认滑动持续时间500ms
	private final int SWIPE_DURING=100;

	private int width;

	private int height;

	public OperateAppium() {

	}

	public OperateAppium(AndroidDriver driver, List<Step> results) {

		this.driver = driver;
		this.results=results;

		this.width=this.getScreenWidth();
		this.height=this.getScreenHeight();


		this.udid=driver.getCapabilities().getCapability("deviceName").toString();

		logger.info("udid:{}",udid);
	}


	public int getMaxScreenshotCount() {
		return MAX_SCREENSHOT_COUNT;
	}

	public int setMaxScreenShotCount(int count){

		return MAX_SCREENSHOT_COUNT=count;

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

			logger.warn("查找元素超时!!{}秒之后还没找到元素 [{}]",time,by.toString());

			return null;

		}
	}

	public AndroidElement waitAutoById(String id) {
		return waitAutoById(id, WAIT_TIME);
	}

	public AndroidElement waitAutoById(String id, int time) {
		return waitAuto(By.id (id), time);
	}

	public AndroidElement waitAutoByXp(String xPath) {
		return waitAutoByXp(xPath, WAIT_TIME);
	}

	public AndroidElement waitAutoByXp(String xPath, int time) {
		return waitAuto(By.xpath(xPath), time);
	}

	/**
	 *
	 * 截图
	 */
	public String screenShot(String path) {

		String preImageFileName =path;

		cmd.run(CmdConfig.SCREEN_CAP.replaceAll("#udid#", this.udid));

		logger.info("开始传输------");
		cmd.run(CmdConfig.PULL_SCREENSHOT.replaceAll("#udid#",
				this.udid).replaceAll("#path2png#",
				preImageFileName));
		logger.info("传输本地完成------");

		//FileUtils.waitFor(new File(preImageFileName),2);

/*		File file= driver.getScreenshotAs (OutputType.FILE);

		try {
			FileUtils.copyFile (file,new File (preImageFileName));

		} catch (IOException e) {
			e.printStackTrace ();

			logger.error("保存截图文件失败 {}",e);

		}*/

		screenIndex++;

		if (screenIndex>MAX_SCREENSHOT_COUNT) screenIndex=0;

		return preImageFileName;

	}

	/**
	 * 截图 ，处理图片添加文字
	 */
	public String  markPicText(String text,int x,int y){


		String psPathFileName = (SCREENSHOT_PATH+ screenIndex+".ori.png");

		screenShot (psPathFileName);

		//String psPathFileName = getScreenPath ()+ getScreenIndex ()+".ps.jpg";

		//file.renameTo(file)

/*
		BufferedImage img=null;
		try {
			img=ImageIO.read (file);

		} catch (IOException e) {
			e.printStackTrace ();

			logger.error("读取截图文件 {}",e);

		}

		Graphics2D graph=img.createGraphics ();

		graph.setStroke (new BasicStroke (5));
		graph.setColor (Color.red);
		graph.setFont(new Font("Serif",Font.PLAIN,120));
		graph.drawString(text,x,y);
		graph.dispose ();

		//生成ps处理后图片
		generatePsPic(img,psPathFileName);

		//删掉原始图片
		FileUtils.deleteQuietly(new File(preImageFileName));*/
		logger.info("滑动的截图:{}",psPathFileName);
		return psPathFileName;
	}

	/**
	 * 获取屏幕宽度
	 *
	 * @return
	 */
	public int getScreenWidth() {
		return driver.manage().window().getSize().getWidth();
	}

	/**
	 * 获取屏幕高度
	 *
	 * @return
	 */
	public int getScreenHeight() {
		return driver.manage().window().getSize().getHeight();
	}
	/**
	 * 元素点击
	 */
	public void click(AndroidElement element){

		String result="pass";
		Step step=new Step();
		String screenShotPath=null;

		try{

			Point location = element.getLocation ();

			int x = location.getX ();
			int y = location.getY ();

			Dimension size = element.getSize ();

			int w = size.getWidth ();
			int h = size.getHeight ();

			step.setX(x);
			step.setY(y);
			step.setW(w);
			step.setH(h);

			logger.info("随机点击控件tagName :{} ",element.getTagName());
			logger.info("随机点击控件Location :{} ",element.getLocation());


			screenShotPath = (SCREENSHOT_PATH+ screenIndex+".ori.png");

			//截图
			screenShot (screenShotPath);

			logger.info("点击截图:{}",screenShotPath);


		}
		catch (Exception e){

			logger.error("截图失败:{}",e);

			screenShotPath=null;

			step.setResult(String.format("截图失败 %s",e.getStackTrace().toString()));
		}

		step.setElementName(element.getTagName());

		step.setAction("Click");

		step.setScreenShot(screenShotPath);

		try {
			element.click();
		}
		catch (Exception e){

			logger.error("Click element :{} error :{}",element,e);

			step.setResult((CmdUtil.isWindows()?"//r//n":"//r")+e.getStackTrace().toString());

			result="fail";
		}

		step.setResult(result);

		results.add(step);

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
		int height =this.height;
		driver.swipe(width / percent, height / 2, width * (percent - 1) / percent, height / 2, during);
	}

	/**
	 * 在某个方向上滑动
	 *
	 * @param direction 方向，UP DOWN LEFT RIGHT
	 * @param duration  持续时间
	 */
	public void swip(String direction, int duration) {

		String result="pass";

		Step step=new Step();

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
		}
		catch (Exception e){

			logger.error("swip {} error : e",direction,e);

			result="fail";

			step.setResult(e.getStackTrace().toString());

		}


		logger.info(" {} : {} ,duration {}",driver.currentActivity(),direction, duration);

		//String text=String.format("%s",direction);

		String screenShot=markPicText(direction,this.width/2-250,this.height/2);


		step.setElementName("Page");
		step.setAction(direction);
		step.setScreenShot(screenShot);
		step.setResult(result);

		results.add(step);

	}

	/**
	 * 操作方法
	 */
	public void doElementAction (AndroidElement element,String nextAction){

		logger.info("element:{},nextAction:{}",element,nextAction);

		switch (nextAction) {

			case Action.CLICK:
				click(element);
				break;
			case Action.DOBACK:
				back();
				break;
			case Action.BACKAPP:
				backApp();
				break;
			case Action.LAUNCHAPP:
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

		logger.info("back to app");

		driver.navigate().back();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 尝试后退
	 */
	public void back(){

		driver.pressKeyCode(4);
		logger.info("返回 do back");
		try {
			Thread.sleep(500);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 获取页面pagesource 超时10s返回
	 */
	public String getPageSource(){

		logger.info("开始获取pagesource");

		String source=null;

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

			source = future.get(15000, TimeUnit.MILLISECONDS); //任务处理超时时间设为 10 秒

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
		 System.out.println (OperateAppium.SCREENSHOT_PATH);

	}

}
