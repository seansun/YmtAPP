package com.ymt.tools;

/**
 * Created by sunsheng on 2017/4/18.
 */

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class XmlParser {

    private static List<String> xpathExpr= Arrays.asList("name", "label", "value", "resource-id", "content-desc", "index", "text","scrollable","bounds");

    private static final Logger logger = LoggerFactory.getLogger(XmlParser.class);

    private Document doc;

    public XmlParser(){

    }

    public XmlParser(String pageSource){
        this.load(pageSource);
    }

    private void load(String pageSource){

        StringBuilder xmlStringBuilder = new StringBuilder();

        xmlStringBuilder.append(pageSource);

        ByteArrayInputStream input=null;

        try {
            input =  new ByteArrayInputStream(
                    xmlStringBuilder.toString().replace("[\\x00-\\x1F]","").replace("&#", StringEscapeUtils.escapeXml("&#")).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        DocumentBuilderFactory dbFactory
                = DocumentBuilderFactory.newInstance();

        DocumentBuilder dBuilder = null;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            this.doc =dBuilder.parse(input);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 根据xpath 获取当前页面 node list
     *
     * @param xpath
     * @return
     */
    public NodeList getNodeListFromXPath (String xpath) {

        this.doc.getDocumentElement().normalize();

        XPath xPath =  XPathFactory.newInstance().newXPath();

        NodeList nodeList = null;
        try {
            nodeList = (NodeList) xPath.compile(xpath).evaluate(doc, XPathConstants.NODESET);

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return nodeList;

    }

    /**
     * 根据xpath 获取当前页面 node list
     *
     * @param pageSource
     * @param xpath
     * @return
     */
    public NodeList getNodeListFromXPath (String pageSource,String xpath) {

        this.load(pageSource);

        this.doc.getDocumentElement().normalize();

        XPath xPath =  XPathFactory.newInstance().newXPath();

        NodeList nodeList = null;
        try {
            nodeList = (NodeList) xPath.compile(xpath).evaluate(doc, XPathConstants.NODESET);

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return nodeList;

        }


    /**
     * 根据当前页面node list 生成 Attributes list
     *
     * @param nodeList
     * @return
     */
    public List<Map<String,String>> getAttributes(NodeList nodeList){

        List<Map<String, String>> pageAttributes = new ArrayList<Map<String, String>>();

        logger.debug("getAttributes nodeList 参数 length {}", nodeList.getLength());

        for (int i = 0; i < nodeList.getLength(); i++) {

            Node node = nodeList.item(i);

            if (node.hasAttributes()) {

                Map<String, String> attributeMap = new HashMap<String, String>();

                attributeMap.put("tag", node.getNodeName());

                for (int j = 0; j < node.getAttributes().getLength(); j++) {

                    Node kv = node.getAttributes().item(j);

                    if (xpathExpr.contains(kv.getNodeName()) && !kv.getNodeValue().isEmpty()) {

                        attributeMap.put(kv.getNodeName(), kv.getNodeValue());

                    }

                }

                pageAttributes.add(attributeMap);

            }

        }
        logger.debug("page Attributes: {}",pageAttributes);

        return pageAttributes;
    }

    /**
     * 根据当前页面Attributes list 生成 xpath list
     *
     * @param pageAttributes
     * @return
     */
    public List<String> getXpathList(List<Map<String, String>> pageAttributes) {

        //List<Map<String, String>> pageAttributes = getAttributes(nodeList);

        logger.debug("getXpathList pageAttributes 参数 length {}", pageAttributes.size());

        List<String> xpathList=new ArrayList<String>();

        for(Map map:pageAttributes){

            StringBuffer xpath=new StringBuffer();

            if (map.containsKey("resource-id")){

                xpath.append("//");
                xpath.append(map.get("tag"));
                xpath.append("[@resource-id='");
                xpath.append(map.get("resource-id"));
                xpath.append("']");

            }
            else {
                xpath.append("//");
                xpath.append(map.get("tag"));
                xpath.append("[@bounds='");
                xpath.append(map.get("bounds"));
                xpath.append("']");
            }

            if (xpath.length()>0) {
                xpathList.add(xpath.toString());
            }

        }

        return  xpathList;
    }

    /**
     * 获取当前窗口唯一标识
     */
    public String getPageUrl(){

        //String baseUrl="";

        String url=null;

        NodeList nodeList=this.getNodeListFromXPath("//*");

        List<Map<String,String>> pageAttributes=getAttributes(nodeList);

        for(Map map:pageAttributes){

            if (map.containsKey("text")){

                url=((String)map.get("text")).trim();

                logger.info("url:{}",url);

                if (!url.isEmpty()){

                    url=url+map.get("bounds");

                    break;
                }
            }
        }
        return url;
    }

    /**
     *
     *  返回当前页面app name
     */
    public String getAppName(){

        NodeList nodeList=this.getNodeListFromXPath("(//*[@package!=''])[1]");

        NamedNodeMap nodeMap=nodeList.item(0).getAttributes();

        return nodeMap.getNamedItem("package").getNodeValue();

    }

    public static void main(String ...args) {



        String source="<?xml version=\"1.0\" encoding=\"UTF-8\"?><hierarchy rotation=\"0\"><android.widget.FrameLayout index=\"0\" text=\"\" class=\"android.widget.FrameLayout\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[0,0][1080,1920]\" resource-id=\"\" instance=\"0\"><android.widget.LinearLayout index=\"0\" text=\"\" class=\"android.widget.LinearLayout\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[0,0][1080,1920]\" resource-id=\"\" instance=\"0\"><android.widget.FrameLayout index=\"0\" text=\"\" class=\"android.widget.FrameLayout\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[0,60][1080,1920]\" resource-id=\"\" instance=\"1\"><android.widget.LinearLayout index=\"0\" text=\"\" class=\"android.widget.LinearLayout\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[0,60][1080,1920]\" resource-id=\"com.ymatou.shop:id/action_bar_root\" instance=\"1\"><android.widget.FrameLayout index=\"0\" text=\"\" class=\"android.widget.FrameLayout\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[0,60][1080,1920]\" resource-id=\"android:id/content\" instance=\"2\"><android.widget.RelativeLayout index=\"0\" text=\"\" class=\"android.widget.RelativeLayout\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[0,60][1080,1920]\" resource-id=\"\" instance=\"0\"><android.widget.RelativeLayout index=\"0\" text=\"\" class=\"android.widget.RelativeLayout\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[0,60][1080,228]\" resource-id=\"com.ymatou.shop:id/login_topbar\" instance=\"1\"><android.widget.TextView index=\"0\" text=\"登录\" class=\"android.widget.TextView\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[489,60][591,228]\" resource-id=\"\" instance=\"0\"/><android.widget.ImageButton NAF=\"true\" index=\"1\" text=\"\" class=\"android.widget.ImageButton\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"true\" enabled=\"true\" focusable=\"true\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[948,60][1080,228]\" resource-id=\"com.ymatou.shop:id/ib_back_button_login_activity\" instance=\"0\"/></android.widget.RelativeLayout><android.widget.LinearLayout index=\"1\" text=\"\" class=\"android.widget.LinearLayout\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[0,228][1080,1920]\" resource-id=\"\" instance=\"2\"><android.widget.RelativeLayout index=\"0\" text=\"\" class=\"android.widget.RelativeLayout\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[66,228][1014,348]\" resource-id=\"\" instance=\"2\"><android.widget.ImageView index=\"0\" text=\"\" class=\"android.widget.ImageView\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[66,228][108,348]\" resource-id=\"com.ymatou.shop:id/ivAccount\" instance=\"0\"/><android.widget.EditText index=\"1\" text=\"邮箱/手机号\" class=\"android.widget.EditText\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"true\" enabled=\"true\" focusable=\"true\" focused=\"true\" scrollable=\"false\" long-clickable=\"true\" password=\"false\" selected=\"false\" bounds=\"[123,228][1014,348]\" resource-id=\"com.ymatou.shop:id/ed_username_login_activity\" instance=\"0\"/></android.widget.RelativeLayout><android.widget.ImageView index=\"1\" text=\"\" class=\"android.widget.ImageView\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[66,369][1014,371]\" resource-id=\"\" instance=\"1\"/><android.widget.RelativeLayout index=\"2\" text=\"\" class=\"android.widget.RelativeLayout\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[66,392][1014,512]\" resource-id=\"\" instance=\"3\"><android.widget.ImageView index=\"0\" text=\"\" class=\"android.widget.ImageView\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[66,392][105,512]\" resource-id=\"com.ymatou.shop:id/ivPass\" instance=\"2\"/><android.widget.EditText NAF=\"true\" index=\"1\" text=\"\" class=\"android.widget.EditText\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"true\" enabled=\"true\" focusable=\"true\" focused=\"false\" scrollable=\"false\" long-clickable=\"true\" password=\"true\" selected=\"false\" bounds=\"[120,392][814,512]\" resource-id=\"com.ymatou.shop:id/ed_password_login_activity\" instance=\"1\"/><android.view.View index=\"2\" text=\"\" class=\"android.view.View\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[814,422][816,482]\" resource-id=\"com.ymatou.shop:id/v_login_line\" instance=\"0\"/><android.widget.TextView index=\"3\" text=\"忘记密码\" class=\"android.widget.TextView\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"true\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[846,423][1014,480]\" resource-id=\"com.ymatou.shop:id/tv_resetPasswordTip_newloginactivity\" instance=\"1\"/></android.widget.RelativeLayout><android.widget.ImageView index=\"3\" text=\"\" class=\"android.widget.ImageView\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[66,533][1014,535]\" resource-id=\"\" instance=\"3\"/><android.widget.RelativeLayout index=\"4\" text=\"\" class=\"android.widget.RelativeLayout\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"false\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[66,595][1014,715]\" resource-id=\"com.ymatou.shop:id/btn_login\" instance=\"4\"><android.widget.RelativeLayout index=\"0\" text=\"\" class=\"android.widget.RelativeLayout\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[75,604][1005,706]\" resource-id=\"\" instance=\"5\"><android.widget.TextView index=\"0\" text=\"登录\" class=\"android.widget.TextView\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[477,622][573,687]\" resource-id=\"com.ymatou.shop:id/tv_login_progress\" instance=\"2\"/></android.widget.RelativeLayout></android.widget.RelativeLayout><android.widget.TextView index=\"5\" text=\"还可选择使用以下方式登录\" class=\"android.widget.TextView\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"true\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[288,805][792,862]\" resource-id=\"com.ymatou.shop:id/tvLoginUseThirdPartyTip\" instance=\"3\"/><android.widget.LinearLayout index=\"6\" text=\"\" class=\"android.widget.LinearLayout\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[45,892][1035,1027]\" resource-id=\"com.ymatou.shop:id/rlLoginUseThirdPartyTip\" instance=\"3\"><android.widget.ImageView NAF=\"true\" index=\"0\" text=\"\" class=\"android.widget.ImageView\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"true\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[91,892][317,1027]\" resource-id=\"com.ymatou.shop:id/iv_weiboTip\" instance=\"4\"/><android.widget.ImageView NAF=\"true\" index=\"1\" text=\"\" class=\"android.widget.ImageView\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"true\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[317,892][543,1027]\" resource-id=\"com.ymatou.shop:id/iv_qqTip\" instance=\"5\"/><android.widget.ImageView NAF=\"true\" index=\"2\" text=\"\" class=\"android.widget.ImageView\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"true\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[543,892][769,1027]\" resource-id=\"com.ymatou.shop:id/iv_weixinTip\" instance=\"6\"/><android.widget.ImageView NAF=\"true\" index=\"3\" text=\"\" class=\"android.widget.ImageView\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"true\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[769,895][989,1024]\" resource-id=\"com.ymatou.shop:id/iv_alipayTip\" instance=\"7\"/></android.widget.LinearLayout></android.widget.LinearLayout><android.widget.LinearLayout index=\"2\" text=\"\" class=\"android.widget.LinearLayout\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[0,1662][1080,1830]\" resource-id=\"\" instance=\"4\"><android.widget.TextView index=\"0\" text=\"还没有账号?\" class=\"android.widget.TextView\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[425,1662][655,1719]\" resource-id=\"\" instance=\"4\"/><android.widget.TextView index=\"1\" text=\" 手机号快速注册 \" class=\"android.widget.TextView\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"true\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[338,1743][742,1830]\" resource-id=\"com.ymatou.shop:id/tv_register_titlebar_login_activity\" instance=\"5\"/></android.widget.LinearLayout></android.widget.RelativeLayout></android.widget.FrameLayout></android.widget.LinearLayout></android.widget.FrameLayout></android.widget.LinearLayout><android.view.View index=\"1\" text=\"\" class=\"android.view.View\" package=\"com.ymatou.shop\" content-desc=\"\" checkable=\"false\" checked=\"false\" clickable=\"false\" enabled=\"true\" focusable=\"false\" focused=\"false\" scrollable=\"false\" long-clickable=\"false\" password=\"false\" selected=\"false\" bounds=\"[0,0][1080,60]\" resource-id=\"android:id/statusBarBackground\" instance=\"1\"/></android.widget.FrameLayout></hierarchy>";
        //String xpath="//*[@clickable='true' and @enabled='true']";
        String xpath="//*[@text='登lv']";

        XmlParser xmlParser=new XmlParser();

        NodeList nodeList= xmlParser.getNodeListFromXPath(source,xpath);

        logger.info("nodeList list {}",nodeList);

        List<String> xpathList=xmlParser.getXpathList(xmlParser.getAttributes(nodeList));

        logger.info("xpath list {}",xpathList);

        int index=new Random().nextInt(xpathList.size());

        String newXpath=xpathList.get(index);
        logger.debug("new newXpath {}",newXpath);
        logger.debug("new element {}",xmlParser.getNodeListFromXPath(source,newXpath).getLength());
        logger.info("page Url {}",new XmlParser(source).getPageUrl());


    }


}

