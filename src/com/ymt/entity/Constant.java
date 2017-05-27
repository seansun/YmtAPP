package com.ymt.entity;

import java.io.File;

/**
 * Created by sunsheng on 2017/5/10.
 */
public class Constant {

    private static final String ROOT = System.getProperty("user.dir");

    public static File getScreenshotPath(){return new File(ROOT, "results/screenshots/");}

    public static File getMinicap() {
        return new File(ROOT, "minicap");
    }

    public static File getMinicapBin() {
        return new File(ROOT, "minicap/bin");}

    public static File getMinicapSo() {
        return new File(ROOT, "minicap/shared");
    }




    public static void main(String args[]){

        System.out.print(Constant.getScreenshotPath().getAbsolutePath());

    }
}
