/**
 * 定位参数
 */
package com.g50.UI.Bean;

import com.Logcat.APPLog;
import com.g50.Bean.GnbBean;
import com.nr70.Gnb.Bean.GnbProtocol;

public class TraceUtil {
    private static TraceUtil instance;

    public static TraceUtil build() {
        synchronized (TraceUtil.class) {
            if (instance == null) {
                instance = new TraceUtil();
            }
        }
        return instance;
    }

    public TraceUtil() {

    }

    public void init() {
        firstCell = new TraceBean(GnbBean.State.NONE);
        secondCell = new TraceBean(GnbBean.State.NONE);
        thirdCell = new TraceBean(GnbBean.State.NONE);
        fourthCell = new TraceBean(GnbBean.State.NONE);
    }

    public int getCfr(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getCfr();
        } else {
            return secondCell.getCfr();
        }
    }

    public void setCfr(int id, int cfr) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setCfr(cfr);
        } else {
            secondCell.setCfr(cfr);
        }
    }

    public boolean isTacChange(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.isTacChange();
        } else {
            return secondCell.isTacChange();
        }
    }

    public void setTacChange(int id, boolean tacChange) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setTacChange(tacChange);
        } else {
            secondCell.setTacChange(tacChange);
        }
    }

    public boolean isEnable(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.isEnable();
        } else {
            return secondCell.isEnable();
        }
    }
    public void setSaveOpLog(int id, boolean save) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setSaveOpLog(save);
        } else {
            secondCell.setSaveOpLog(save);
        }
    }
    public boolean isSaveOpLog(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.isSaveOpLog();
        } else {
            return secondCell.isSaveOpLog();
        }
    }

    public void setEnable(int id, boolean enable) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setEnable(enable);
        } else {
            secondCell.setEnable(enable);
        }
    }

    public String getImsi(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getImsi();
        } else if (id == GnbProtocol.CellId.SECOND){
            return secondCell.getImsi();
        }else if (id == GnbProtocol.CellId.THIRD){
            return firstCell.getImsi();
        }else {
            return secondCell.getImsi();
        }
    }

    public void setImsi(int id, String imsi) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setImsi(imsi);
        } else if (id == GnbProtocol.CellId.SECOND){
            secondCell.setImsi(imsi);
        } else if (id == GnbProtocol.CellId.THIRD) {
            firstCell.setImsi(imsi);
        } else if (id == GnbProtocol.CellId.FOURTH){
            secondCell.setImsi(imsi);
        }
    }

    public String getPlmn(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getPlmn();
        } else {
            return secondCell.getPlmn();
        }
    }

    public void setPlmn(int id, String plmn) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setPlmn(plmn);
        } else {
            secondCell.setPlmn(plmn);
        }
    }

    public String getSubPlmn(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getSubPlmn();
        } else {
            return secondCell.getSubPlmn();
        }
    }

    public void setSubPlmn(int id, String subPlmn) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setSubPlmn(subPlmn);
        } else {
            secondCell.setSubPlmn(subPlmn);
        }
    }

    public String getArfcn(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getArfcn();
        } else if (id == GnbProtocol.CellId.SECOND){
            return secondCell.getArfcn();
        }else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getArfcn();
        } else {
            return fourthCell.getArfcn();
        }
    }

    public void setArfcn(int id, String arfcn) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setArfcn(arfcn);
        } else if (id == GnbProtocol.CellId.SECOND){
            secondCell.setArfcn(arfcn);
        }else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setArfcn(arfcn);
        } else {
            fourthCell.setArfcn(arfcn);
        }
    }

    public String getPci(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getPci();
        } else {
            return secondCell.getPci();
        }
    }

    public void setPci(int id, String pci) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setPci(pci);
        } else {
            secondCell.setPci(pci);
        }
    }

    public int getTimingOffset(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getTimingOffset();
        } else {
            return secondCell.getTimingOffset();
        }
    }

    public void setTimingOffset(int id, int timingOffset) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setTimingOffset(timingOffset);
        } else {
            secondCell.setTimingOffset(timingOffset);
        }
    }
    public int getLastRsrp(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getLastRsrp();
        } else if (id == GnbProtocol.CellId.SECOND){
            return secondCell.getLastRsrp();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getLastRsrp();
        } else {
            return fourthCell.getLastRsrp();
        }
    }

    public void setLastRsrp(int id, int rsrp) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setLastRsrp(rsrp);
        } else if (id == GnbProtocol.CellId.SECOND){
            secondCell.setLastRsrp(rsrp);
        }else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setLastRsrp(rsrp);
        } else {
            fourthCell.setLastRsrp(rsrp);
        }
    }

    public int getRsrp(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getTraceRsrp(id);
        } else if (id == GnbProtocol.CellId.SECOND){
            return secondCell.getTraceRsrp(id);
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getTraceRsrp(id);
        } else {
            return fourthCell.getTraceRsrp(id);
        }
    }

    public void setRsrp(int id, int rsrp) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setTraceRsrp(rsrp);
        } else if (id == GnbProtocol.CellId.SECOND){
            secondCell.setTraceRsrp(rsrp);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setTraceRsrp(rsrp);
        } else {
            fourthCell.setTraceRsrp(rsrp);
        }
    }

    public int getAirSync(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getAirSync();
        } else {
            return secondCell.getAirSync();
        }
    }

    public void setAirSync(int id, int airSync) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setAirSync(airSync);
        } else {
            secondCell.setAirSync(airSync);
        }
    }

    public int getBandWidth(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getBandWidth();
        } else {
            return secondCell.getBandWidth();
        }
    }

    public void setBandWidth(int id, int bandWidth) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setBandWidth(bandWidth);
        } else {
            secondCell.setBandWidth(bandWidth);
        }
    }

    public int getSsbBitmap(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getSsbBitmap();
        } else {
            return secondCell.getSsbBitmap();
        }
    }

    public void setSsbBitmap(int id, int ssbBitmap) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setSsbBitmap(ssbBitmap);
        } else {
            secondCell.setSsbBitmap(ssbBitmap);
        }
    }

    public int getTxPwr(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getTxPwr();
        } else {
            return secondCell.getTxPwr();
        }
    }

    public void setTxPwr(int id, int txPwr) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setTxPwr(txPwr);
        } else {
            secondCell.setTxPwr(txPwr);
        }
    }
    public boolean getArfcnSetChange(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.isArfcnChange();
        } else {
            return secondCell.isArfcnChange();
        }
    }

    public void setArfcnSetChange(int id, boolean enable) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setArfcnChange(enable);
        } else {
            secondCell.setArfcnChange(enable);
        }
    }

    public String getUeMaxTxpwr(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getUeMaxTxpwr();
        } else {
            return secondCell.getUeMaxTxpwr();
        }
    }

    public void setUeMaxTxpwr(int id, String ueMaxTxpwr) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setUeMaxTxpwr(ueMaxTxpwr);
        } else {
            secondCell.setUeMaxTxpwr(ueMaxTxpwr);
        }
    }

    public long getCid(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getCid();
        } else {
            return secondCell.getCid();
        }
    }

    public void setCid(int id, long cid) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setCid(cid);
        } else {
            secondCell.setCid(cid);
        }
    }

    public int getWorkState(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getWorkState();
        } else {
            return secondCell.getWorkState();
        }
    }

    public void setWorkState(int id, int workState) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setWorkState(workState);
        } else {
            secondCell.setWorkState(workState);
        }
    }

    public long getAtCmdTimeOut(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getAtCmdTimeOut();
        } else {
            return secondCell.getAtCmdTimeOut();
        }
    }

    public void setAtCmdTimeOut(int id, long time) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setAtCmdTimeOut(time);
        } else {
            secondCell.setAtCmdTimeOut(time);
        }
    }
    public int getSwap_rf(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getSwap_rf();
        } else {
            return secondCell.getSwap_rf();
        }
    }

    public void setSwap_rf(int id, int swap_rf) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setSwap_rf(swap_rf);
        } else {
            secondCell.setSwap_rf(swap_rf);
        }
    }
    public String getSplit_arfcn_dl(int id){
        if (id == GnbProtocol.CellId.FIRST || id == GnbProtocol.CellId.THIRD) {
          return firstCell.getSplitArfcnDl();
        } else {
           return secondCell.getSplitArfcnDl();
        }
    }
    public void setSplit_arfcn_dl(int id,String split_arfcn_dl){
        if (id == GnbProtocol.CellId.FIRST || id == GnbProtocol.CellId.THIRD) {
            firstCell.setSplitArfcnDl(split_arfcn_dl);
        } else {
            secondCell.setSplitArfcnDl(split_arfcn_dl);
        }
    }
    public int getMobRejectCode(int id){
        if (id == GnbProtocol.CellId.FIRST) {
          return firstCell.getMobRejectCode();
        } else {
           return secondCell.getMobRejectCode();
        }
    }
    public void setMobRejectCode(int id,int mob_reject_code){
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setMobRejectCode(mob_reject_code);
        } else {
            secondCell.setMobRejectCode(mob_reject_code);
        }
    }
    private TraceBean firstCell, secondCell,thirdCell,fourthCell;
}
