package com.ymt.entity;

import com.ymt.tools.LimitQueue;

import java.util.List;
import java.util.Map;

/**
 * Created by sunsheng on 2017/5/5.
 */
public class DataRecord {

    private String deviceName;
    private String appInfo;
    private String appLog;
    private String appiumLog;
    private String OperaterLog;
    private String duringTime;
    private int totalStep;


    public String getDuringTime() {
        return duringTime;
    }

    public void setDuringTime(String duringTime) {
        this.duringTime = duringTime;
    }

    private Map<String ,Integer> pageCount;

    private LimitQueue<Step>  results;

    public Map<String, Integer> getPageCount() {
        return pageCount;
    }

    public void setPageCount(Map<String, Integer> pageCount) {
        this.pageCount = pageCount;
    }

    public LimitQueue<Step>  getResults() {
        return results;
    }

    public void setResults(LimitQueue<Step>  results) {
        this.results = results;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(String appInfo) {
        this.appInfo = appInfo;
    }


    public String getAppLog() {
        return appLog;
    }

    public void setAppLog(String appLog) {
        this.appLog = appLog;
    }

    public String getAppiumLog() {
        return appiumLog;
    }

    public void setAppiumLog(String appiumLog) {
        this.appiumLog = appiumLog;
    }

    public String getOperaterLog() {
        return OperaterLog;
    }

    public void setOperaterLog(String operaterLog) {
        OperaterLog = operaterLog;
    }


    public int getTotalStep() {
        return totalStep;
    }

    public void setTotalStep(int totalStep) {
        this.totalStep = totalStep;
    }
}
