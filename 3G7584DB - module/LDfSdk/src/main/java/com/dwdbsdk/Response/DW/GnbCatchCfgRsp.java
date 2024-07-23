/**
 * 版本号查询反馈
 * typedef struct {
 * int sync_header;
 * int msg_type;          		//UI_2_gNB_OAM_MSG
 * int cmd_type;				//UI_2_gNB_QUERY_gNB_VERSION
 * int cmd_param;
 * <p>
 * char hw_ver[OAM_STR_MAX];
 * char fpga_ver[OAM_STR_MAX];
 * char sw_ver[OAM_STR_MAX];
 * <p>
 * int sync_footer;
 * } oam_get_version_t; //out
 */
package com.dwdbsdk.Response.DW;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GnbCatchCfgRsp {
    int rspValue;
    int cellId;
    int saveFlag;
    int startTac;//[0 ～ 4294967294]
    int endTac;   // 大于 startTac
    int tacInterval; // 1-1440 分钟

    public int getRspValue() {
        return rspValue;
    }

    public void setRspValue(int rspValue) {
        this.rspValue = rspValue;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public int getSaveFlag() {
        return saveFlag;
    }

    public void setSaveFlag(int saveFlag) {
        this.saveFlag = saveFlag;
    }

    public int getStartTac() {
        return startTac;
    }

    public void setStartTac(int startTac) {
        this.startTac = startTac;
    }

    public int getEndTac() {
        return endTac;
    }

    public void setEndTac(int endTac) {
        this.endTac = endTac;
    }

    public int getTacInterval() {
        return tacInterval;
    }

    public void setTacInterval(int tacInterval) {
        this.tacInterval = tacInterval;
    }

    public Map<String, String> getGnbCfg() {
        return gnbCfg;
    }

    public void setGnbCfg(Map<String, String> gnbCfg) {
        this.gnbCfg = gnbCfg;
    }

    Map<String, String> gnbCfg;    //启动时的频点参数配置 参看: 3.8 配置参数配置

    public GnbCatchCfgRsp() {
        this.cellId = 0;
        this.saveFlag = 0;
        this.startTac = 0;
        this.endTac = 0;
        this.tacInterval = 0;
        this.gnbCfg = null;
    }

    @Override
    public String toString() {
        return "GnbCatchCfgRsp{" +
                "rspValue=" + rspValue +
                ", cellId=" + cellId +
                ", saveFlag=" + saveFlag +
                ", startTac=" + startTac +
                ", endTac=" + endTac +
                ", tacInterval=" + tacInterval +
                ", gnbCfg=" + Map2String(gnbCfg) +
                '}';
    }

    private String Map2String(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, String>> set = map.entrySet();
        Iterator<Map.Entry<String, String>> it = set.iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + ":" + value + " , ");
        }
        return sb.toString();
    }
}
