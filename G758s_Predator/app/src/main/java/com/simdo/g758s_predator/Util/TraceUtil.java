/**
 * 定位参数
 */
package com.simdo.g758s_predator.Util;

import com.dwdbsdk.Bean.DW.DWProtocol;
import com.dwdbsdk.Bean.LteBand;
import com.dwdbsdk.Bean.NrBand;
import com.simdo.g758s_predator.Bean.GnbBean;
import com.simdo.g758s_predator.Bean.TraceBean;

public class TraceUtil {

    public TraceUtil() {
        firstCell = new TraceBean(GnbBean.DW_State.NONE);
        secondCell = new TraceBean(GnbBean.DW_State.NONE);
        thirdCell = new TraceBean(GnbBean.DW_State.NONE);
        fourthCell = new TraceBean(GnbBean.DW_State.NONE);
    }

    public int getCfr(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getCfr();
        } if (id == DWProtocol.CellId.SECOND) {
            return secondCell.getCfr();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getCfr();
        }else {
            return fourthCell.getCfr();
        }
    }

    public void setCfr(int id, int cfr) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setCfr(cfr);
        } else {
            secondCell.setCfr(cfr);
        }
    }

    public boolean isTacChange(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.isTacChange();
        }else if (id == DWProtocol.CellId.SECOND) {
            return secondCell.isTacChange();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.isTacChange();
        } else {
            return fourthCell.isTacChange();
        }
    }

    public void setTacChange(int id, boolean tacChange) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setTacChange(tacChange);
        }else if (id == DWProtocol.CellId.SECOND) {
            secondCell.setTacChange(tacChange);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setTacChange(tacChange);
        } else {
            fourthCell.setTacChange(tacChange);
        }
    }

    public boolean isEnable(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.isEnable();
        }else if(id == DWProtocol.CellId.SECOND) {
            return secondCell.isEnable();
        }else if(id == DWProtocol.CellId.THIRD) {
            return thirdCell.isEnable();
        }else {
            return fourthCell.isEnable();
        }
    }
    public void setSaveOpLog(int id, boolean save) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setSaveOpLog(save);
        }else if (id == DWProtocol.CellId.SECOND) {
            secondCell.setSaveOpLog(save);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setSaveOpLog(save);
        } else {
            fourthCell.setSaveOpLog(save);
        }
    }
    public boolean isSaveOpLog(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.isSaveOpLog();
        }else if (id == DWProtocol.CellId.SECOND) {
            return secondCell.isSaveOpLog();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.isSaveOpLog();
        } else {
            return fourthCell.isSaveOpLog();
        }
    }

    public void setEnable(int id, boolean enable) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setEnable(enable);
        }else if (id == DWProtocol.CellId.SECOND) {
            secondCell.setEnable(enable);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setEnable(enable);
        } else {
            fourthCell.setEnable(enable);
        }
    }

    public String getImsi(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getImsi();
        } else if (id == DWProtocol.CellId.SECOND){
            return secondCell.getImsi();
        }else if (id == DWProtocol.CellId.THIRD){
            return thirdCell.getImsi();
        }else {
            return secondCell.getImsi();
        }
    }

    public void setImsi(int id, String imsi) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setImsi(imsi);
        } else if (id == DWProtocol.CellId.SECOND){
            secondCell.setImsi(imsi);
        } else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setImsi(imsi);
        } else if (id == DWProtocol.CellId.FOURTH){
            secondCell.setImsi(imsi);
        }
    }

    public String getPlmn(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getPlmn();
        }else if (id == DWProtocol.CellId.SECOND) {
            return secondCell.getPlmn();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getPlmn();
        }else {
            return fourthCell.getPlmn();
        }
    }

    public void setPlmn(int id, String plmn) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setPlmn(plmn);
        }else if (id == DWProtocol.CellId.SECOND) {
            secondCell.setPlmn(plmn);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setPlmn(plmn);
        } else {
            fourthCell.setPlmn(plmn);
        }
    }

    public String getSubPlmn(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getSubPlmn();
        }else if (id == DWProtocol.CellId.SECOND) {
            return secondCell.getSubPlmn();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getSubPlmn();
        } else {
            return fourthCell.getSubPlmn();
        }
    }

    public void setSubPlmn(int id, String subPlmn) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setSubPlmn(subPlmn);
        }else if (id == DWProtocol.CellId.SECOND) {
            secondCell.setSubPlmn(subPlmn);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setSubPlmn(subPlmn);
        } else {
            fourthCell.setSubPlmn(subPlmn);
        }
    }

    public String getArfcn(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getArfcn();
        } else if (id == DWProtocol.CellId.SECOND){
            return secondCell.getArfcn();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getArfcn();
        } else {
            return fourthCell.getArfcn();
        }
    }

    public void setArfcn(int id, String arfcn) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setArfcn(arfcn);
        } else if (id == DWProtocol.CellId.SECOND){
            secondCell.setArfcn(arfcn);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setArfcn(arfcn);
        } else {
            fourthCell.setArfcn(arfcn);
        }
    }

    public String getPci(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getPci();
        } else if (id == DWProtocol.CellId.SECOND){
            return secondCell.getPci();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getPci();
        } else {
            return fourthCell.getPci();
        }
    }

    public void setPci(int id, String pci) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setPci(pci);
        } else if (id == DWProtocol.CellId.SECOND) {
            secondCell.setPci(pci);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setPci(pci);
        }else {
            fourthCell.setPci(pci);
        }
    }

    public int getTimingOffset(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getTimingOffset();
        } else if (id == DWProtocol.CellId.SECOND) {
            return secondCell.getTimingOffset();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getTimingOffset();
        }else {
            return fourthCell.getTimingOffset();
        }
    }
    public int getTimingOffset(String arfcn) {
        int band;
        if (arfcn.length()>5){//nr
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
            if (band == 41 || band ==79 ||band ==28) {
                return 3000000;
            } else  {
                return 0;
            }
        }else {
            band = LteBand.earfcn2band(Integer.parseInt(arfcn));
            if (band == 39 || band == 40 || band == 34 || band == 41) {
                return 9038000;
            }else{
                return 0;
            }
        }

    }

    public void setTimingOffset(int id, int timingOffset) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setTimingOffset(timingOffset);
        }else if (id == DWProtocol.CellId.SECOND) {
            secondCell.setTimingOffset(timingOffset);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setTimingOffset(timingOffset);
        } else {
            fourthCell.setTimingOffset(timingOffset);
        }
    }
    public int getLastRsrp(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getLastRsrp();
        } else if (id == DWProtocol.CellId.SECOND){
            return secondCell.getLastRsrp();
        } else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getLastRsrp();
        } else {
            return fourthCell.getLastRsrp();
        }
    }

    public void setLastRsrp(int id, int rsrp) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setLastRsrp(rsrp);
        } else if (id == DWProtocol.CellId.SECOND){
            secondCell.setLastRsrp(rsrp);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setLastRsrp(rsrp);
        } else {
            fourthCell.setLastRsrp(rsrp);
        }
    }

    public int getRsrp(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getTraceRsrp(id);
        } else if (id == DWProtocol.CellId.SECOND){
            return secondCell.getTraceRsrp(id);
        } else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getTraceRsrp(id);
        } else {
            return fourthCell.getTraceRsrp(id);
        }
    }

    public void setRsrp(int id, int rsrp) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setTraceRsrp(rsrp);
        } else if (id == DWProtocol.CellId.SECOND){
            secondCell.setTraceRsrp(rsrp);
        } else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setTraceRsrp(rsrp);
        } else {
            fourthCell.setTraceRsrp(rsrp);
        }
    }

    public int getAirSync(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getAirSync();
        }else if (id == DWProtocol.CellId.SECOND) {
            return secondCell.getAirSync();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getAirSync();
        } else {
            return fourthCell.getAirSync();
        }
    }

    public void setAirSync(int id, int airSync) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setAirSync(airSync);
        }else if (id == DWProtocol.CellId.SECOND) {
            secondCell.setAirSync(airSync);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setAirSync(airSync);
        } else {
            fourthCell.setAirSync(airSync);
        }
    }

    public int getBandWidth(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getBandWidth();
        }else if (id == DWProtocol.CellId.SECOND) {
            return secondCell.getBandWidth();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getBandWidth();
        } else {
            return fourthCell.getBandWidth();
        }
    }

    public void setBandWidth(int id, int bandWidth) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setBandWidth(bandWidth);
        }else if (id == DWProtocol.CellId.SECOND) {
            secondCell.setBandWidth(bandWidth);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setBandWidth(bandWidth);
        } else {
            fourthCell.setBandWidth(bandWidth);
        }
    }

    public int getSsbBitmap(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getSsbBitmap();
        }else if (id == DWProtocol.CellId.SECOND) {
            return secondCell.getSsbBitmap();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getSsbBitmap();
        } else {
            return fourthCell.getSsbBitmap();
        }
    }

    public void setSsbBitmap(int id, int ssbBitmap) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setSsbBitmap(ssbBitmap);
        }else if (id == DWProtocol.CellId.SECOND) {
            secondCell.setSsbBitmap(ssbBitmap);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setSsbBitmap(ssbBitmap);
        } else {
            fourthCell.setSsbBitmap(ssbBitmap);
        }
    }

    public int getTxPwr(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getTxPwr();
        }else if (id == DWProtocol.CellId.SECOND) {
            return secondCell.getTxPwr();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getTxPwr();
        } else {
            return fourthCell.getTxPwr();
        }
    }

    public void setTxPwr(int id, int txPwr) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setTxPwr(txPwr);
        }else if (id == DWProtocol.CellId.SECOND) {
            secondCell.setTxPwr(txPwr);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setTxPwr(txPwr);
        } else {
            fourthCell.setTxPwr(txPwr);
        }
    }

    public String getUeMaxTxpwr(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getUeMaxTxpwr();
        }else if (id == DWProtocol.CellId.SECOND) {
            return secondCell.getUeMaxTxpwr();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getUeMaxTxpwr();
        } else {
            return fourthCell.getUeMaxTxpwr();
        }
    }

    public void setUeMaxTxpwr(int id, String ueMaxTxpwr) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setUeMaxTxpwr(ueMaxTxpwr);
        }else if (id == DWProtocol.CellId.SECOND) {
            secondCell.setUeMaxTxpwr(ueMaxTxpwr);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setUeMaxTxpwr(ueMaxTxpwr);
        } else {
            fourthCell.setUeMaxTxpwr(ueMaxTxpwr);
        }
    }

    public long getCid(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getCid();
        }else if (id == DWProtocol.CellId.SECOND) {
            return secondCell.getCid();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getCid();
        } else {
            return fourthCell.getCid();
        }
    }

    public void setCid(int id, long cid) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setCid(cid);
        }else if (id == DWProtocol.CellId.SECOND) {
            secondCell.setCid(cid);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setCid(cid);
        } else {
            fourthCell.setCid(cid);
        }
    }

    public int getWorkState(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getWorkState();
        }else if (id == DWProtocol.CellId.SECOND) {
            return secondCell.getWorkState();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getWorkState();
        } else {
            return fourthCell.getWorkState();
        }
    }

    public void setWorkState(int id, int workState) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setWorkState(workState);
        }else if (id == DWProtocol.CellId.SECOND) {
            secondCell.setWorkState(workState);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setWorkState(workState);
        } else {
            fourthCell.setWorkState(workState);
        }
    }

    public long getAtCmdTimeOut(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getAtCmdTimeOut();
        }else if (id == DWProtocol.CellId.SECOND) {
            return secondCell.getAtCmdTimeOut();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getAtCmdTimeOut();
        } else {
            return fourthCell.getAtCmdTimeOut();
        }
    }

    public void setAtCmdTimeOut(int id, long time) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setAtCmdTimeOut(time);
        }else if (id == DWProtocol.CellId.SECOND) {
            secondCell.setAtCmdTimeOut(time);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setAtCmdTimeOut(time);
        } else {
            fourthCell.setAtCmdTimeOut(time);
        }
    }
    public int getSwap_rf(int id) {
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getSwap_rf();
        } else {
            return secondCell.getSwap_rf();
        }
    }

    public void setSwap_rf(int id, int swap_rf) {
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setSwap_rf(swap_rf);
        } else {
            secondCell.setSwap_rf(swap_rf);
        }

    }
    public String getSplit_arfcn_dl(int id){
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getSplitArfcnDl();
        }else if (id == DWProtocol.CellId.SECOND) {
            return secondCell.getSplitArfcnDl();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getSplitArfcnDl();
        } else {
            return fourthCell.getSplitArfcnDl();
        }
    }
    public void setSplit_arfcn_dl(int id,String split_arfcn_dl){
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setSplitArfcnDl(split_arfcn_dl);
        }else if (id == DWProtocol.CellId.SECOND) {
            secondCell.setSplitArfcnDl(split_arfcn_dl);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setSplitArfcnDl(split_arfcn_dl);
        } else {
            fourthCell.setSplitArfcnDl(split_arfcn_dl);
        }
    }
    public int getMobRejectCode(int id){
        if (id == DWProtocol.CellId.FIRST) {
            return firstCell.getMobRejectCode();
        }else if (id == DWProtocol.CellId.SECOND) {
            return secondCell.getMobRejectCode();
        }else if (id == DWProtocol.CellId.THIRD) {
            return thirdCell.getMobRejectCode();
        } else {
            return fourthCell.getMobRejectCode();
        }
    }
    public void setMobRejectCode(int id,int mob_reject_code){
        if (id == DWProtocol.CellId.FIRST) {
            firstCell.setMobRejectCode(mob_reject_code);
        }else if (id == DWProtocol.CellId.SECOND) {
            secondCell.setMobRejectCode(mob_reject_code);
        }else if (id == DWProtocol.CellId.THIRD) {
            thirdCell.setMobRejectCode(mob_reject_code);
        } else {
            fourthCell.setMobRejectCode(mob_reject_code);
        }
    }
    private TraceBean firstCell, secondCell,thirdCell,fourthCell;
}
