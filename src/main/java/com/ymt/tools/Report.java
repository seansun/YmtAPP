package com.ymt.tools;

import com.ymt.entity.Constant;
import com.ymt.entity.DataRecord;
import com.ymt.entity.Step;
import com.ymt.operation.OperateAppium;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by sunsheng on 2017/5/5.
 */
public class Report {

    private static final Logger logger = LoggerFactory.getLogger(Report.class);

    public static String REPORT_PATH = Constant.getResultPath().getPath()
            + File.separator + OperateAppium.currentTime + File.separator;

    private String screenshotPath = OperateAppium.SCREENSHOT_PATH;

    public void generateReport(DataRecord record, String reportName, int resultLimt) {

        try {

            String udid = OperateAppium.udid;

            AdbUtils adbUtils = new AdbUtils(udid);

            for (int i = 0; i < resultLimt; i++) {

                String fileName = String.format("screenShot%s", i);
                //将图片导入到本地
                adbUtils.pullScreen(fileName, String.format("%s%s.%s", screenshotPath, fileName, "png"));

            }

            List<Step> resultList = record.getResults();

            int size = resultList.size();

            record.setTotalStep(size);


           // File input = new File(Report.class.getResource("/reportTemplete.html").getPath());
            InputStream inputStream = (Report.class.getResourceAsStream("/reportTemplete.html"));

            Document doc =  Jsoup.parse(inputStream,"UTF-8","");


            Element p = doc.getElementById("device");
            Element p2 = doc.getElementById("app");
            Element p3 = doc.getElementById("steps");
            Element p4 = doc.getElementById("time");

            p.text(record.getDeviceName());
            p2.text(record.getAppInfo());
            p3.text(String.valueOf(record.getTotalStep()));
            p4.text(record.getDuringTime());


            Map<String, Integer> pageInfo = record.getPageCount();

            List<Entry<String, Integer>> list =
                    new ArrayList<Entry<String, Integer>>(pageInfo.entrySet());

            //排序
            list.sort((a, b) -> {
                return b.getValue().compareTo(a.getValue());
            });

            Elements pageInfoDetail = doc.select("#pageInfo");

            StringBuffer element = new StringBuffer();

            for (Entry entry : list) {

                element.append("<tr>");
                element.append(String.format("<td>%s</td>", entry.getKey()));
                element.append(String.format("<td>%s</td>", entry.getValue()));
                element.append("</tr>");

            }

            pageInfoDetail.append(element.toString());


            Elements pageLog = doc.select("#log");

            pageLog.append(record.getAppiumLog());
            pageLog.append(record.getAppLog());


            if (size > resultLimt) {

                resultList = resultList.subList(size - resultLimt, size);
            }

            for (Step step : resultList) {

                if (step.getAction().equals("Click")) {

                    if (null != step.getScreenShotName()) {
                        //处理图片
                        markPicHightLight(step);

                    }
                }
                if (step.getAction().contains("SWIP")) {
                    if (null != step.getScreenShotName()) {
                        //处理图片
                        markPicText(step, OperateAppium.width / 2 - 250, OperateAppium.height / 2);

                    }

                }
            }
            //生成详细操作步骤
            writeDetail(resultList, doc);

            String fileName = REPORT_PATH + String.format("report%s.html", OperateAppium.taskId );


            File file = new File(fileName);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

            writer.write(doc.html());

            writer.close();

        } catch (Exception e) {

            logger.error("生成report 异常");
            e.printStackTrace();
        }

    }

    public void writeDetail(List<Step> result, Document doc) {


        Element h2 = doc.select("h2").first();
        h2.text("最后" + result.size() + "步操作步骤明细");

        Elements deltail = doc.select("#detail");

        String th = "<th style=\"width:1%;font-weight:bold;\">step</th><th style=\"width:1%;font-weight:bold;\">elementType</th><th style=\"width:1%;font-weight:bold;\">action</th><th style=\"width:8%;font-weight:bold;\">screenShot</th><th style=\"width:1%;font-weight:bold;\">result</th>";

        //先处理header 头
        StringBuffer header = new StringBuffer();
        header.append("<tr>");

        for (Step step : result) {

            header.append(th);

        }
        header.append("</tr>");

        deltail.append(header.toString());


        StringBuffer element = new StringBuffer();

        element.append("<tr>");

        int i = 1;

        for (Step step : result) {
            element.append(String.format("<td>%d</td>", i));
            element.append(String.format("<td>%s</td>", step.getElementName()));
            element.append(String.format("<td>%s</td>", step.getAction()));
            element.append(String.format("<td><img src='./screenshots/%s/%s_ps.png' align='absmiddle' width='250' height='400'/></td>", OperateAppium.taskId - 1, step.getScreenShotName()));
            element.append(String.format("<td>%s</td>", step.getResult()));

            i++;
        }
        element.append("<tr>");

        deltail.append(element.toString());

    }


    /**
     * 截图 ，处理图片
     */
    public void markPicHightLight(Step step) {

        String oriImageFileName = screenshotPath + step.getScreenShotName() + ".png";

        String psImageFileName = oriImageFileName.replace(".png", "_ps.png");

        logger.info("markPicHightLight oriImageFile path:{}", oriImageFileName);

        File file = new File(oriImageFileName);

        BufferedImage img = null;

        try {

            img = ImageIO.read(file);

        } catch (Exception e) {

            e.printStackTrace();

            logger.error("markPicHightLight 读取截图文件 {}", e);

            return;

        }

        Graphics2D graph = img.createGraphics();

        graph.setStroke(new BasicStroke(5));
        graph.setColor(Color.red);

        graph.drawRect(step.getX(), step.getY(), step.getW(), step.getH());

        graph.dispose();

        //生成ps处理后图片
        generatePsPic(img, psImageFileName);

        //删掉原始图片
        //FileUtils.deleteQuietly(new File(oriImageFileName));

        //step.setScreenShotName(psImageFileName);

    }

    /**
     * 截图 ，处理图片添加文字
     */
    public void markPicText(Step step, int x, int y) {

        String oriImageFileName = screenshotPath + step.getScreenShotName() + ".png";
        String psImageFileName = oriImageFileName.replace(".png", "_ps.png");

        logger.info("markPicText oriImageFile path:{}", oriImageFileName);

        File file = new File(oriImageFileName);

        BufferedImage img = null;
        try {

            img = ImageIO.read(file);

        } catch (Exception e) {

            e.printStackTrace();
            logger.error("markPicText 读取截图文件 {}", e);

            return ;
        }
        Graphics2D graph = img.createGraphics();
        graph.setStroke(new BasicStroke(5));
        graph.setColor(Color.red);
        graph.setFont(new Font("Serif", Font.PLAIN, 120));
        graph.drawString(step.getAction(), x, y);
        graph.dispose();
        //生成ps处理后图片
        generatePsPic(img, psImageFileName);
        //删掉原始图片
        //FileUtils.deleteQuietly(new File(psImageFileName));

    }


    private void generatePsPic(BufferedImage img, String psPathFileName) {

        try {
            ImageIO.write(img, "png", new File(psPathFileName));

            logger.info("高亮截图:{}", psPathFileName);

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("高亮截图 {},{}", psPathFileName, e);
        }

    }

    public static void main(String args[]) {


        System.out.println(""+XmlParser.class.getResource("/config.yml"));
        System.out.println(Thread.currentThread().getContextClassLoader().getResource("config.yml"));




        //System.out.print(Report.REPORT_PATH);

        // new Report().generateReport(null,null,0);
        File file = new File("C:\\Users\\sunsheng\\Desktop\\YmtAPP\\results\\20170622\\screenshots\\0\\screenShot1.png");

        logger.info(file.getAbsolutePath());
        try {
            BufferedImage img = ImageIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();

        }


    }

}
