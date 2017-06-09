package com.ymt.entity;

import java.util.List;

/**
 * Created by sunsheng on 2017/4/18.
 */
public class TriggerAction {

    private String triggerCondition;

    public List<Operate> getActionList() {
        return actionList;
    }

    public void setActionList(List<Operate> actionList) {
        this.actionList = actionList;
    }

    public String getTriggerCondition() {
        return triggerCondition;
    }

    public void setTriggerCondition(String triggerCondition) {
        this.triggerCondition = triggerCondition;
    }

    private List<Operate> actionList;


}
