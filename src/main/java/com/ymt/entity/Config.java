package com.ymt.entity;

import java.util.List;

/**
 * Created by sunsheng on 2017/4/14.
 */
public class Config {

    private Capability capability;

    private AndroidCapability androidCapability;

    private IOSCapability iosCapability;

    private List<String> clickList;

    private List<String> blackList;

    private List<TriggerAction> triggerActions;

    public Capability getCapability() {
        return capability;
    }

    public void setCapability(Capability capability) {
        this.capability = capability;
    }

    public AndroidCapability getAndroidCapability() {
        return androidCapability;
    }

    public void setAndroidCapability(AndroidCapability androidCapability) {
        this.androidCapability = androidCapability;
    }

    public IOSCapability getIosCapability() {
        return iosCapability;
    }

    public void setIosCapability(IOSCapability iosCapability) {
        this.iosCapability = iosCapability;
    }

    public List<String> getClickList() {
        return clickList;
    }

    public void setClickList(List<String> clickList) {
        this.clickList = clickList;
    }

    public List<String> getBlackList() {
        return blackList;
    }

    public void setBlackList(List<String> blackList) {
        this.blackList = blackList;
    }

    public List<TriggerAction> getTriggerActions() {
        return triggerActions;
    }

    public void setTriggerActions(List<TriggerAction> triggerActions) {
        this.triggerActions = triggerActions;
    }
}
