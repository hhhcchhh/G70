package com.simdo.g73cs.Bean;
public class StepBean {
    /** 状态 */
    private int state;
    /** 用户 */
    private String account;
    /** 时间 */
    private String time;
    /** 描述 */
    private String info;
    public StepBean(int state, String account,String time, String info) {
        this.state = state;
        this.account = account;
        this.time = time;
        this.info = info;
    }
    public void setState(int state) {
        this.state = state;
    }
    public int getState() {
        return state;
    }
    public String getTime() {
        return time;
    }
    public String getInfo() {
        return info;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "StepBean{" +
                "state=" + state +
                ", account='" + account + '\'' +
                ", time='" + time + '\'' +
                ", info='" + info + '\'' +
                '}';
    }

    public interface State {
        int success = 0;
        int fail = 1;
        int success_end = 2;
    }
}
