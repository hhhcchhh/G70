package com.simdo.g73cs.Bean;

import org.json.JSONException;
import org.json.JSONObject;

public class OperatorLogBean {
    public OperatorLogBean() {
        this.account = "000000000000000";
        this.operation = "000000";
        this.time = 0;
    }

    public boolean matchesConstraint(CharSequence constraint) {
        // 如果名称包含了搜索条件（constraint），则返回 true，否则返回 false
        return account.contains(constraint.toString());
    }

    String account;
    String operation;
    long time;

    public OperatorLogBean(String account, String operation,long time) {
        this.account = account;
        this.operation = operation;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getAccount(){
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public static OperatorLogBean fromJson(JSONObject jsonObject) {
        OperatorLogBean operatorLogBean = new OperatorLogBean();
        try {
            if (jsonObject.has("ACCOUNT")) {
                operatorLogBean.setAccount(jsonObject.getString("ACCOUNT"));
            }
            if (jsonObject.has("OPERATION")) {
                operatorLogBean.setOperation(jsonObject.getString("OPERATION"));
            }
            if (jsonObject.has("TIME")) {
                operatorLogBean.setTime(jsonObject.getLong("TIME"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return operatorLogBean;
    }
}
