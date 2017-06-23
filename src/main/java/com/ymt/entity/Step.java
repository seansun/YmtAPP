package com.ymt.entity;

/**
 * Created by sunsheng on 2017/5/5.
 */
public class Step {

    private String action;
    //adb 手机截图文件名
    private String screenShotName;
    private String elementName;

    private String result;
    //元素element location x,y,  Dimension size w,h
    private int x;
    private int y;
    private int w;
    private int h;


    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getScreenShotName() {
        return screenShotName;
    }

    public void setScreenShotName(String screenShotName) {
        this.screenShotName = screenShotName;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }


    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }
}
