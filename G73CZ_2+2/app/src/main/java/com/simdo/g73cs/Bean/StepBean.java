package com.simdo.g73cs.Bean;
public class StepBean {

    /** 时间 */
    private int state;
    /** 时间 */
    private String time;
    /** 描述 */
    private String info;
    public StepBean(int state, String time, String info) {
        this.state = state;
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
    @Override
    public String toString() {
        return "StepBean{" +
                "state=" + state +
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
