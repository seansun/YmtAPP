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


    public static String REPORT_PATH= Constant.getResultPath().getPath()
                      + File.separator +OperateAppium.currentTime+File.separator;


    public void generateReport(DataRecord record, String reportName, int resultLimt){
        try {

            List<Step> resultList=record.getResults();

            int size=resultList.size();

            record.setTotalStep(size);

            File input=new File(Constant.getResultPath().getPath()+ "/reportTemplete.html");

            Document doc=  Jsoup.parse(input,"UTF-8");

            Element p=doc.select("p").first();
            Element p2=doc.getElementsByTag("p").get(1);
            Element p3=doc.getElementsByTag("p").get(2);
            Element p4=doc.getElementsByTag("p").get(3);

            p.text(p.text()+record.getDeviceName());
            p2.text(p2.text()+record.getAppInfo());
            p3.text(p3.text()+record.getTotalStep());
            p4.text(p4.text()+record.getDuringTime());


            Map<String, Integer> pageInfo= record.getPageCount();

            List<Entry<String,Integer>> list =
                    new ArrayList<Entry<String,Integer>>(pageInfo.entrySet());

            //排序
            list.sort((a,b)->{
                return b.getValue().compareTo(a.getValue());
            });

            Elements pageInfoDetail=doc.select("#pageInfo");

            StringBuffer element=new StringBuffer();

            for(Entry entry :list){

                element.append("<tr>");
                element.append(String.format("<td>%s</td>",entry.getKey()));
                element.append(String.format("<td>%s</td>",entry.getValue()));
                element.append("</tr>");

            }

            pageInfoDetail.append(element.toString());



            Elements pageLog=doc.select("#log");

            pageLog.append(record.getAppiumLog());
            pageLog.append(record.getAppLog());


            if (size>resultLimt){

                resultList=resultList.subList(size-resultLimt,size);
            }

            for(Step step:resultList){

                if (step.getAction().equals("Click")){

                    if (null!=step.getScreenShot()){

                        //处理图片
                        markPicHightLight(step);

                    }
                }
            }
            //生成详细操作步骤
            writeDetail(resultList,doc);

            String fileName=REPORT_PATH+String.format("report%s.html",OperateAppium.taskId-1);


            File file=new File(fileName);

            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));

            writer.write(doc.html());

            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void writeDetail(List<Step> result, Document doc){


        Element h2=doc.select("h2").first();
        h2.text("最后"+result.size()+"步操作步骤明细");

        Elements deltail=doc.select("#detail");

        String th="<th style=\"width:1%;font-weight:bold;\">step</th><th style=\"width:1%;font-weight:bold;\">elementType</th><th style=\"width:1%;font-weight:bold;\">action</th><th style=\"width:8%;font-weight:bold;\">screenShot</th><th style=\"width:1%;font-weight:bold;\">result</th>";

        //先处理header 头
        StringBuffer header=new StringBuffer();
        header.append("<tr>");

        for(Step step :result) {

            header.append(th);

        }
        header.append("</tr>");

        deltail.append(header.toString());


        StringBuffer element=new StringBuffer();

        element.append("<tr>");
        int i=1;

        for(Step step :result){
            //element.append("<tr>");
            element.append(String.format("<td>%d</td>",i));
            element.append(String.format("<td>%s</td>",step.getElementName()));
            element.append(String.format("<td>%s</td>",step.getAction()));
            // element.append("<td><a href='./screenshots/0.ps.jpg'>screenShots</a></td>");
            element.append(String.format("<td><img src='./screenshots/%s/%s' align='absmiddle' width='250' height='400'/></td>",OperateAppium.taskId-1,step.getScreenShot().replace(OperateAppium.SCREENSHOT_PATH,"")));
            element.append(String.format("<td>%s</td>",step.getResult()));
/*            element.append(String.format("<td>%s</td>",step.getOperaterLog()));
            element.append("<td>xxxx</td>");
            element.append("<td>xxxxss</td>");*/
            //element.append("</tr>");

            i++;
        }
        element.append("<tr>");

        deltail.append(element.toString());

    }

    /**
     * 截图 ，处理图片
     */
    public  void markPicHightLight(Step step){

        String oriImageFileName = step.getScreenShot();

        String psImageFileName = oriImageFileName.replace("ori","ps");

        File file=new File(oriImageFileName);

        BufferedImage img=null;
        try {

            img= ImageIO.read (file);

        } catch (IOException e) {
            e.printStackTrace ();

            logger.error("读取截图文件 {}",e);

            return ;

        }

        Graphics2D graph=img.createGraphics ();

        graph.setStroke (new BasicStroke (5));
        graph.setColor (Color.red);

        graph.drawRect (step.getX(),step.getY(),step.getW(),step.getH());

        graph.dispose ();

        //生成ps处理后图片
        generatePsPic(img,psImageFileName);

        //删掉原始图片
        FileUtils.deleteQuietly(new File(oriImageFileName));

        step.setScreenShot(psImageFileName);

    }

    private void generatePsPic(BufferedImage img,String psPathFileName){

        try {
            ImageIO.write (img,"png",new File (psPathFileName));

            logger.info("高亮截图:{}",psPathFileName);

        } catch (IOException e) {
            e.printStackTrace ();
            logger.error("高亮截图 {},{}",psPathFileName,e);
        }

    }

    public static void main(String args[]){

        //System.out.print(Report.REPORT_PATH);

       // new Report().generateReport(null,null,0);


    }

}
