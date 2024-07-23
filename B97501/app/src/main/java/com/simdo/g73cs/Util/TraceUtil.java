/**
 * 定位参数
 */
package com.simdo.g73cs.Util;

import com.nr.Gnb.Bean.GnbProtocol;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.TraceBean;

public class TraceUtil {

    public TraceUtil() {
        firstCell = new TraceBean(GnbBean.State.IDLE);
        secondCell = new TraceBean(GnbBean.State.IDLE);
        thirdCell = new TraceBean(GnbBean.State.IDLE);
        fourthCell = new TraceBean(GnbBean.State.IDLE);
    }

    public int getCfr(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getCfr();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getCfr();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getCfr();
        } else {
            return fourthCell.getCfr();
        }
    }

    public void setCfr(int id, int cfr) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setCfr(cfr);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setCfr(cfr);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setCfr(cfr);
        } else {
            fourthCell.setCfr(cfr);
        }
    }

    public boolean isTacChange(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.isTacChange();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.isTacChange();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.isTacChange();
        } else {
            return fourthCell.isTacChange();
        }
    }

    public void setTacChange(int id, boolean tacChange) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setTacChange(tacChange);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setTacChange(tacChange);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setTacChange(tacChange);
        } else {
            fourthCell.setTacChange(tacChange);
        }
    }

    public boolean isEnable(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.isEnable();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.isEnable();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.isEnable();
        } else {
            return fourthCell.isEnable();
        }
    }

    public void setSaveOpLog(int id, boolean save) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setSaveOpLog(save);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setSaveOpLog(save);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setSaveOpLog(save);
        } else {
            fourthCell.setSaveOpLog(save);
        }
    }

    public boolean isSaveOpLog(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.isSaveOpLog();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.isSaveOpLog();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.isSaveOpLog();
        } else {
            return fourthCell.isSaveOpLog();
        }
    }

    public void setEnable(int id, boolean enable) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setEnable(enable);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setEnable(enable);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setEnable(enable);
        } else {
            fourthCell.setEnable(enable);
        }
    }

    public String getImsi(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getImsi();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getImsi();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getImsi();
        } else {
            return fourthCell.getImsi();
        }
    }

    public void setImsi(int id, String imsi) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setImsi(imsi);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setImsi(imsi);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setImsi(imsi);
        } else if (id == GnbProtocol.CellId.FOURTH) {
            fourthCell.setImsi(imsi);
        }
    }

    public String getPlmn(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getPlmn();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getPlmn();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getPlmn();
        } else {
            return fourthCell.getPlmn();
        }
    }

    public void setPlmn(int id, String plmn) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setPlmn(plmn);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setPlmn(plmn);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setPlmn(plmn);
        } else {
            fourthCell.setPlmn(plmn);
        }
    }

    public String getSubPlmn(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getSubPlmn();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getSubPlmn();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getSubPlmn();
        } else {
            return fourthCell.getSubPlmn();
        }
    }

    public void setSubPlmn(int id, String subPlmn) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setSubPlmn(subPlmn);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setSubPlmn(subPlmn);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setSubPlmn(subPlmn);
        } else {
            fourthCell.setSubPlmn(subPlmn);
        }
    }

    public String getArfcn(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getArfcn();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getArfcn();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getArfcn();
        } else {
            return fourthCell.getArfcn();
        }
    }

    public void setArfcn(int id, String arfcn) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setArfcn(arfcn);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setArfcn(arfcn);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setArfcn(arfcn);
        } else {
            fourthCell.setArfcn(arfcn);
        }
    }

    public String getPci(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getPci();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getPci();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getPci();
        } else {
            return fourthCell.getPci();
        }
    }

    public void setPci(int id, String pci) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setPci(pci);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setPci(pci);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setPci(pci);
        } else {
            fourthCell.setPci(pci);
        }
    }

    public int getTimingOffset(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getTimingOffset();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getTimingOffset();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getTimingOffset();
        } else {
            return fourthCell.getTimingOffset();
        }
    }

    public void setTimingOffset(int id, int timingOffset) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setTimingOffset(timingOffset);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setTimingOffset(timingOffset);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setTimingOffset(timingOffset);
        } else {
            fourthCell.setTimingOffset(timingOffset);
        }
    }

    public int getLastRsrp(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getLastRsrp();
        } else if (id == GnbProtocol.CellId.SECOND) {
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
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setLastRsrp(rsrp);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setLastRsrp(rsrp);
        } else {
            fourthCell.setLastRsrp(rsrp);
        }
    }

    public int getRsrp(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getTraceRsrp(id);
        } else if (id == GnbProtocol.CellId.SECOND) {
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
        } else if (id == GnbProtocol.CellId.SECOND) {
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
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getAirSync();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getAirSync();
        } else {
            return fourthCell.getAirSync();
        }
    }

    public void setAirSync(int id, int airSync) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setAirSync(airSync);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setAirSync(airSync);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setAirSync(airSync);
        } else {
            fourthCell.setAirSync(airSync);
        }
    }

    public int getBandWidth(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getBandWidth();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getBandWidth();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getBandWidth();
        } else {
            return fourthCell.getBandWidth();
        }
    }

    public void setBandWidth(int id, int bandWidth) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setBandWidth(bandWidth);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setBandWidth(bandWidth);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setBandWidth(bandWidth);
        } else {
            fourthCell.setBandWidth(bandWidth);
        }
    }

    public int getSsbBitmap(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getSsbBitmap();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getSsbBitmap();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getSsbBitmap();
        } else {
            return fourthCell.getSsbBitmap();
        }
    }

    public void setSsbBitmap(int id, int ssbBitmap) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setSsbBitmap(ssbBitmap);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setSsbBitmap(ssbBitmap);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setSsbBitmap(ssbBitmap);
        } else {
            fourthCell.setSsbBitmap(ssbBitmap);
        }
    }

    public int getTxPwr(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getTxPwr();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getTxPwr();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getTxPwr();
        } else {
            return fourthCell.getTxPwr();
        }
    }

    public void setTxPwr(int id, int txPwr) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setTxPwr(txPwr);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setTxPwr(txPwr);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setTxPwr(txPwr);
        } else {
            fourthCell.setTxPwr(txPwr);
        }
    }

    public String getUeMaxTxpwr(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getUeMaxTxpwr();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getUeMaxTxpwr();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getUeMaxTxpwr();
        } else {
            return fourthCell.getUeMaxTxpwr();
        }
    }

    public void setUeMaxTxpwr(int id, String ueMaxTxpwr) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setUeMaxTxpwr(ueMaxTxpwr);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setUeMaxTxpwr(ueMaxTxpwr);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setUeMaxTxpwr(ueMaxTxpwr);
        } else {
            fourthCell.setUeMaxTxpwr(ueMaxTxpwr);
        }
    }

    public long getCid(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getCid();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getCid();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getCid();
        } else {
            return fourthCell.getCid();
        }
    }

    public void setCid(int id, long cid) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setCid(cid);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setCid(cid);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setCid(cid);
        } else {
            fourthCell.setCid(cid);
        }
    }

    public int getWorkState(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getWorkState();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getWorkState();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getWorkState();
        } else {
            return fourthCell.getWorkState();
        }
    }

    public void setWorkState(int id, int workState) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setWorkState(workState);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setWorkState(workState);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setWorkState(workState);
        } else {
            fourthCell.setWorkState(workState);
        }
    }

    public long getAtCmdTimeOut(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getAtCmdTimeOut();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getAtCmdTimeOut();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getAtCmdTimeOut();
        } else {
            return fourthCell.getAtCmdTimeOut();
        }
    }

    public void setAtCmdTimeOut(int id, long time) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setAtCmdTimeOut(time);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setAtCmdTimeOut(time);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setAtCmdTimeOut(time);
        } else {
            fourthCell.setAtCmdTimeOut(time);
        }
    }

    public int getSwap_rf(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getSwap_rf();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getSwap_rf();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getSwap_rf();
        } else {
            return fourthCell.getSwap_rf();
        }
    }

    public void setSwap_rf(int id, int swap_rf) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setSwap_rf(swap_rf);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setSwap_rf(swap_rf);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setSwap_rf(swap_rf);
        } else {
            fourthCell.setSwap_rf(swap_rf);
        }
    }

    public int getForce_cfg(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getForce_cfg();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getForce_cfg();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getForce_cfg();
        } else {
            return fourthCell.getForce_cfg();
        }
    }

    public void setForce_cfg(int id, int force_cfg) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setForce_cfg(force_cfg);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setForce_cfg(force_cfg);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setForce_cfg(force_cfg);
        } else {
            fourthCell.setForce_cfg(force_cfg);
        }
    }

    public String getSplit_arfcn_dl(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getSplitArfcnDl();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getSplitArfcnDl();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getSplitArfcnDl();
        } else {
            return fourthCell.getSplitArfcnDl();
        }
    }

    public void setSplit_arfcn_dl(int id, String split_arfcn_dl) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setSplitArfcnDl(split_arfcn_dl);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setSplitArfcnDl(split_arfcn_dl);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setSplitArfcnDl(split_arfcn_dl);
        } else {
            fourthCell.setSplitArfcnDl(split_arfcn_dl);
        }
    }

    public int getMobRejectCode(int id) {
        if (id == GnbProtocol.CellId.FIRST) {
            return firstCell.getMobRejectCode();
        } else if (id == GnbProtocol.CellId.SECOND) {
            return secondCell.getMobRejectCode();
        } else if (id == GnbProtocol.CellId.THIRD) {
            return thirdCell.getMobRejectCode();
        } else {
            return fourthCell.getMobRejectCode();
        }
    }

    public void setMobRejectCode(int id, int mob_reject_code) {
        if (id == GnbProtocol.CellId.FIRST) {
            firstCell.setMobRejectCode(mob_reject_code);
        } else if (id == GnbProtocol.CellId.SECOND) {
            secondCell.setMobRejectCode(mob_reject_code);
        } else if (id == GnbProtocol.CellId.THIRD) {
            thirdCell.setMobRejectCode(mob_reject_code);
        } else {
            fourthCell.setMobRejectCode(mob_reject_code);
        }
    }

    private final TraceBean firstCell;
    private final TraceBean secondCell;
    private final TraceBean thirdCell;
    private final TraceBean fourthCell;
}
