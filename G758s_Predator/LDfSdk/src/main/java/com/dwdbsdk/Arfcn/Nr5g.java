package com.dwdbsdk.Arfcn;

import com.dwdbsdk.Bean.DW.AtCmd;
import com.dwdbsdk.Bean.DW.BsBeam;
import com.dwdbsdk.Bean.DW.LocBean;
import com.dwdbsdk.Bean.NrBand;
import com.dwdbsdk.Bean.DW.PwrCtl;
import com.dwdbsdk.Interface.Nr5gScanArfcnListener;
import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.SerialPort.SerialPortController;
import com.dwdbsdk.Util.DataUtil;
import com.dwdbsdk.Util.SdkPref;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Nr5g {
    public static Nr5g build() {
        synchronized (Nr5g.class) {
            if (instance == null) {
                instance = new Nr5g();
            }
        }
        return instance;
    }

    public Nr5g() {
    }

    public void init() {
        qsFlag = SdkPref.build().getArfcnMode();
        //SdkLog.I("ARFCN: init() qsFlag= " + qsFlag);
        rstCount = 0;
        saveData.clear();
        cmdType = STATE.AT_CNMP;
        errorCnt = 0;
        simCard = 0;
        enableArfcnLock = false;
        lockArfcn = -1;
        initInfo();
        stopTimeDelay();
    }

    public void stop() {
        stopTimeDelay();
        instance = null;
    }

    private void initInfo() {
        curList.clear();
    }

    public String getArfcn() {
        if (lockArfcn != -1) {
            String arfcn = AT_ARFCN + lockArfcn + "\r";
            SdkLog.I("ARFCN:getArfcn(): arfcn = " + arfcn);
            return arfcn;
        }
        return null;
    }

    public void sendQsCmd() {
        //SdkLog.I("ARFCN: sendAtCmd() cmdType = " + cmdType);
        if (cmdType == STATE.AT_BAND) {
            if (scanType == AtCmd.OP.MOBILE) {
                SerialPortController.build().sendAtCmd(AT_BAND_M);
            } else {
                SerialPortController.build().sendAtCmd(AT_BAND_UT);
            }
        } else if (cmdType == STATE.AT_CNMP) {
            SerialPortController.build().sendAtCmd(AT_CNMP);

        } else if (cmdType == STATE.AT_CRESET) {
            SerialPortController.build().sendAtCmd(AT_CRESET);

        } else if (cmdType == STATE.AT_CFUN_0) {
            SerialPortController.build().sendAtCmd(AT_CFUN_0);
        } else if (cmdType == STATE.AT_CFUN_1) {
            SerialPortController.build().sendAtCmd(AT_CFUN_1);
        } else if (cmdType == STATE.AT_CSYSSEL) {
            SerialPortController.build().sendAtCmd(AT_CSYSSEL);

        } else if (cmdType == STATE.AT_START) {
            SerialPortController.build().sendAtCmd(AT_START);

        } else if (cmdType == STATE.AT_STOP) {
            SerialPortController.build().sendAtCmd(AT_STOP);

        } else if (cmdType == STATE.AT_CNWSEARCH) {
            SerialPortController.build().sendAtCmd(AT_CNWSEARCH);
        }
    }

    private void sendNormalCmd() {
        //SdkLog.I("ARFCN: sendNormalCmd() cmdType = " + cmdType);
        if (cmdType == STATE.AT_CPIN) {
            SerialPortController.build().sendAtCmd(AT_CPIN);
        } else if (cmdType == STATE.AT_CPOF) {
            SerialPortController.build().sendAtCmd(AT_CPOF);
        } else if (cmdType == STATE.AT_CRESET) {
            SerialPortController.build().sendAtCmd(AT_CRESET);
        } else if (cmdType == STATE.AT_SIMCOMATI) {
            SerialPortController.build().sendAtCmd(AT_SIMCOMATI);
        } else if (cmdType == STATE.AT_CNMP) {
            SerialPortController.build().sendAtCmd(AT_CNMP);
        } else if (cmdType == STATE.AT_CMGRMI) {
            SerialPortController.build().sendAtCmd(AT_CMGRMI);
        } else if (cmdType == STATE.AT_UNLOCK) {
            SerialPortController.build().sendAtCmd(AT_UNLOCK);
        } else if (cmdType == STATE.AT_BAND) {
            if (scanType == AtCmd.OP.MOBILE) {
                SerialPortController.build().sendAtCmd(AT_BAND_M);
            } else {
                SerialPortController.build().sendAtCmd(AT_BAND_UT);
            }
        } else if (cmdType == STATE.AT_ARFCN) {
            String arfcn = getArfcn();
            if (arfcn != null) {
                SerialPortController.build().sendAtCmd(arfcn);
            } else {
                cmdType = STATE.AT_UNLOCK;
                enableArfcnLock = false;
                SerialPortController.build().sendAtCmd(AT_UNLOCK);
            }
        } else if (cmdType == STATE.AT_CFUN_0) {
            SerialPortController.build().sendAtCmd(AT_CFUN_0);
        } else if (cmdType == STATE.AT_CFUN_1) {
            SerialPortController.build().sendAtCmd(AT_CFUN_1);
        } else if (cmdType == STATE.AT_CMGRMI_NR) {
            SerialPortController.build().sendAtCmd(AT_CMGRMI_NR);
        }
    }

    public List<LocBean> parseQsData(String tmpStr) {
        //SdkLog.D("ARFCN: tmpStr = " + tmpStr);
        rstCount = 0;
        if (STATE.AT_CNMP == cmdType) {
            if (tmpStr.contains("71OK")) {
                cmdType = STATE.AT_BAND;
            }
        } else if (STATE.AT_CSYSSEL == cmdType) {
            if (tmpStr.contains("OK")) {
                cmdType = STATE.AT_BAND;
            }
        } else if (STATE.AT_CRESET == cmdType) {
            if (tmpStr.contains("OK")) {
                cmdType = STATE.AT_CNMP;
            }
        } else if (STATE.AT_BAND == cmdType) {
            if (tmpStr.contains("OK")) {
                cmdType = STATE.AT_CFUN_0;
            }
        } else if (STATE.AT_START == cmdType) {
            if (tmpStr.contains("OK")) {
                cmdType = STATE.AT_CNWSEARCH;
            }
        } else if (STATE.AT_STOP == cmdType) {
            if (tmpStr.contains("OK")) {
                cmdType = STATE.AT_CSYSSEL;
            } else if (tmpStr.contains("ERROR")) {
                cmdType = STATE.AT_STOP;
                if (++errorCnt >= 2) {
                    cmdType = STATE.AT_CNMP;
                }
            }
        } else if (STATE.AT_CFUN_0 == cmdType) {
            if (tmpStr.contains("OK")) {
                cmdType = STATE.AT_CFUN_1;
            }
        } else if (STATE.AT_CFUN_1 == cmdType) {
            if (tmpStr.contains("OK")) {
                cmdType = STATE.AT_START;
            }
        } else if (STATE.AT_CNWSEARCH == cmdType) {
            //mcc:460,mnc:00,Tac:1241,cellid:3032,pci:119,freq:504990,rsrp:62.070,rsrq:10.344
            //mcc:460,mnc:00,Tac:1248,cellid:3032,pci:115,freq:504990,rsrp:62.008,rsrq:10.375OK
            if (tmpStr.contains("CNWSEARCH=\"nr5g\",2OK")) {
                if (++errorCnt >= 6) {
                    errorCnt = 0;
                    cmdType = STATE.AT_STOP;
                }
            } else if (tmpStr.contains("mcc:")) {
                if (tmpStr.contains("OK")) {
                    tmpStr = tmpStr.replace("OK", "");
                }
                String[] data = tmpStr.split("mcc:");
                if (data.length > 0) {
                    curList.clear();
                    for (int idx = 0; idx < data.length; idx++) {
                        parseQsInfo(data[idx]);
                    }
                }
                for (Nr5gScanArfcnListener listener : listenerList) {
                    if (listener != null)
                        listener.onNr5gScanArfcnRsp(curList);
                }
            }
        }
        return curList;
    }

    public List<LocBean> parseNormalData(String tmpStr) {
        // SdkLog.D("ARFCN: tmpStr = " + tmpStr);
        rstCount = 0;
        if (STATE.AT_CPIN == cmdType) {
            if (tmpStr.contains("READY")) {
                simCard = 1;
                cmdType = STATE.AT_CNMP;
                errorCnt = 0;
            } else if (tmpStr.contains("ERROR")) {
                simCard = 0;
                cmdType = STATE.AT_CNMP;
            }
        } else if (STATE.AT_SIMCOMATI == cmdType) {
            cmdType = STATE.AT_CNMP;

        } else if (STATE.AT_CPOF == cmdType) {
            if (tmpStr.contains("OK")) {
                rstCount = 9;
                cmdType = STATE.AT_CNMP;
            }
        } else if (STATE.AT_CRESET == cmdType) {
            if (tmpStr.contains("OK")) {
                cmdType = STATE.AT_CNMP;
            }
        } else if (STATE.AT_CNMP == cmdType) {
            if (tmpStr.contains("71OK")) {
                cmdType = STATE.AT_UNLOCK;
            }
        } else if (STATE.AT_CMGRMI == cmdType) {
            if (tmpStr.contains("OK")) {
                if (simCard == 1) {
                    cmdType = STATE.AT_CMGRMI_NR;
                } else {
                    cmdType = STATE.AT_BAND;
                }
            } else if (tmpStr.contains("ERROR")) {
                cmdType = STATE.AT_UNLOCK;
            }
        } else if (STATE.AT_UNLOCK == cmdType) {
            if (enableArfcnLock) {
                if (tmpStr.contains("OK")) {
                    cmdType = STATE.AT_ARFCN;
                } else if (tmpStr.contains("ERROR")) {
                    cmdType = STATE.AT_ARFCN;
                }
            } else {
                if (tmpStr.contains("OK")) {
                    cmdType = STATE.AT_CMGRMI;
                } else if (tmpStr.contains("ERROR")) {
                    cmdType = STATE.AT_CMGRMI;
                }
            }
        } else if (STATE.AT_BAND == cmdType) {
            if (tmpStr.contains("OK")) {
                cmdType = STATE.AT_CFUN_0;
            } else if (tmpStr.contains("ERROR")) {
                cmdType = STATE.AT_UNLOCK;
            }
        } else if (STATE.AT_ARFCN == cmdType) {
            if (tmpStr != null) {
                if (tmpStr.contains("OK")) {
                    cmdType = STATE.AT_CMGRMI_NR;
                } else if (tmpStr.contains("ERROR")) {
                    cmdType = STATE.AT_UNLOCK;
                }
            }
        } else if (STATE.AT_CFUN_0 == cmdType) {
            if (tmpStr.contains("OK")) {
                cmdType = STATE.AT_CFUN_1;
            }
        } else if (STATE.AT_CFUN_1 == cmdType) {
            if (tmpStr.contains("OK")) {
                cmdType = STATE.AT_CMGRMI_NR;
            }
        } else if (STATE.AT_CMGRMI_NR == cmdType) {
            //NR_M,AT+CMGRMI=6+CMGRMI:Main_Info,6,1,1,1,1
            //+CMGRMI:Serving_Cell,460,00,0x1421C5,51576627203,403,504990,-990,-140
            //+CMGRMI:5G_NGHBC,504990,41,140.0,40.0,(0,3,140.0)OK
            if (tmpStr.contains("CMGRMI=6ERROR")) {
                if (++errorCnt > 5) {
                    cmdType = STATE.AT_CNMP;
                    errorCnt = 0;
                    enableArfcnLock = false;
                    SdkLog.D("ARFCN: 6ERROR: " + errorCnt);
                }
            } else {
                if (tmpStr.contains("Serving_Cell")) {
                    initInfo();
                    parseNrData(tmpStr);
                }
            }
            handleNeiberData();
            for (Nr5gScanArfcnListener listener : listenerList) {
                if (listener != null)
                    listener.onNr5gScanArfcnRsp(curList);
            }
        }
        return curList;
    }

    private void parseNrData(String tmpStr) {
        //AT+CMGRMI=6
        // +CMGRMI:Main_Info,6,1,1,1,1
        // +CMGRMI:Serving_Cell,460,60,0x610000,26038263810,68,154110,-1300,-220
        // +CNWSEARCH:5G_NGHBC,627264,204,100.4,11.2,(0,1,100.4)
        // +CNWSEARCH:5G_NGHBC,627264,591,113.1,16.2,(0,5,113.1)
        // +CNWSEARCH:5G_NGHBC,627264,204,99.8,11.1,(0,1,99.8),(1,0,108.4),(2,2,108.7),(3,6,116.7)
        // +CNWSEARCH:5G_NGHBC,627264,147,111.6,17.0,(0,2,111.6)
        // +CNWSEARCH:5G_NGHBC,504990,114,121.4,19.9,(0,5,121.4)
        // +CNWSEARCH:5G_NGHBC,154110,68,126.4,18.2,(0,3,126.4)OK
        String[] info = tmpStr.split("\\+CMGRMI:");
        if (tmpStr.contains("CMGRMI:5G_NGHBC")) {
            for (int idx = 0; idx < info.length; idx++) {
                if (info[idx].contains("Serving_Cell")) {
                    parseServingCell(info[idx]);
                } else if (info[idx].contains("5G_NGHBC")) {
                    parseNeighberCell(info[idx]);
                }
            }
        } else if (tmpStr.contains("CNWSEARCH:5G_NGHBC")) {
            if (info.length > 2) {
                for (int idx = 2; idx < info.length; idx++) {
                    String[] data = info[idx].split("\\+CNWSEARCH:");
                    for (int i = 0; i < data.length; i++) {
                        if (data[i].contains("Serving_Cell")) {
                            parseServingCell(data[i]);
                        } else if (data[i].contains("5G_NGHBC")) {
                            parseNeighberCell(data[i]);
                        }
                    }
                }
            }
        } else {
            for (int idx = 0; idx < info.length; idx++) {
                if (info[idx].contains("Serving_Cell")) {
                    parseServingCell(info[idx]);
                }
            }
        }
    }

    private void parseQsInfo(String info) {
        //mcc:460,mnc:00,Tac:1241,cellid:3032,pci:119,freq:504990,rsrp:62.070,rsrq:10.344
        String mcc = ""; // 0
        String mnc = ""; // 1
        String tac = ""; // 3
        String band = ""; // 4
        String arfcn = ""; // 5
        String nci = ""; // 6
        String rsrp = ""; // 7
        String pci = ""; // 8
        info = info.replaceAll(" ", "");
        String[] data = info.split(",");
        if (data.length < 8) {
            return;
        }
        mcc = data[0].replace("mcc:", "");
        mnc = data[1].replace("mnc:", "");
        tac = data[2].replace("Tac:", "");
        nci = data[3].replace("cellid:", "");
        pci = data[4].replace("pci:", "");
        arfcn = data[5].replace("freq:", "");
        rsrp = data[6].replace("rsrp:", "");
        if (rsrp.contains(".")) {
            rsrp = rsrp.substring(0, rsrp.indexOf("."));
        }
        try {
            if (DataUtil.onlyNumeric(rsrp)) {
                if (Long.parseLong(rsrp) > 130) {
                    rsrp = "-110";
                } else {
                    rsrp = "-" + rsrp;
                }

            } else {
                rsrp = "-110";
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            rsrp = "-110";
        }
        if (!DataUtil.onlyNumeric(tac) || !DataUtil.onlyNumeric(nci) || !DataUtil.onlyNumeric(arfcn) || !DataUtil.onlyNumeric(pci)) {
            return;
        }
        LocBean lc;
        if (scanType == AtCmd.OP.UNICOM) {
            if (mnc.equals("01")) {
                lc = new LocBean(tac, nci, arfcn, pci, "", rsrp, null);
                curList.add(lc);
            }
        } else if (scanType == AtCmd.OP.TELECOM) {
            if (mnc.equals("11")) {
                lc = new LocBean(tac, nci, arfcn, pci, "", rsrp, null);
                curList.add(lc);
            }
        } else {
            lc = new LocBean(tac, nci, arfcn, pci, "", rsrp, null);
            curList.add(lc);
        }
    }

    private void parseServingCell(String info) {
        //SdkLog.I("ARFCN: parseServingCell(): " + info);
        //Serving_Cell,460,00,0x1421C5,51576627203,403,504990,-990,-140
        String mnc = "", mcc = "";
        String tac = "", eci = "", pci = "", arfcn = "", rsrp = "";
        String[] data = info.split(",");
        if ((data.length >= 2) && (data[1] != null)) {
            mcc = data[1];
            if (mcc.length() != 3) {
                return;
            }
        }
        if ((data.length >= 3) && (data[2] != null)) {
            mnc = data[2];
            if (mnc.length() != 2) {
                return;
            }
        }
        if ((data.length >= 4) && (data[3] != null)) {
            tac = data[3].replace("0x", "");
            long num = DataUtil.str2Int(tac, 16);
            if (-1 != num) {
                tac = String.valueOf(num);
            }
            if (!DataUtil.onlyNumeric(tac)) {
                return;
            }
        }
        if ((data.length >= 5) && (data[4] != null)) {
            eci = data[4];
            if (!DataUtil.onlyNumeric(eci)) {
                return;
            }
        }
        if ((data.length >= 6) && (data[5] != null)) {
            pci = data[5];
            if (!DataUtil.onlyNumeric(pci)) {
                return;
            }
        }
        if ((data.length >= 7) && (data[6] != null)) {
            arfcn = data[6];
            if (!DataUtil.onlyNumeric(arfcn)) {
                return;
            }
        }
        if ((data.length >= 8) && (data[7] != null)) {
            if (data[7].contains(".")) { // error 03.5
                return;
            }
            rsrp = data[7].replaceAll("-", "");
            if (!DataUtil.onlyNumeric(rsrp)) {
                rsrp = "-110";
            } else {
                try {
                    int rx = Integer.valueOf(rsrp) / 10;
                    if (rx < 130) {
                        rsrp = "-" + rx;
                    } else {
                        rsrp = "-110";
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    rsrp = "-110";
                }

            }
        }
        addCell(tac, eci, arfcn, pci, rsrp, null, false);
        if (++lockCnt > 5) {
            lockCnt = 0;
            if (enableArfcnLock) enableArfcnLock = false;
        }
        if (!tac.equals("") && !eci.equals("")) {
            int size = saveData.size();
            for (int i = 0; i < size; i++) {
                if (tac.equals(saveData.get(i).getTac())
                        && eci.equals(saveData.get(i).getEci())) {
                    return;
                }
            }
            saveData.add(new LocBean(tac, eci, arfcn, pci, rsrp));
        }
    }

    private void parseNeighberCell(String info) {
        //SdkLog.I("ARFCN: parseNeighberCell(): " + info);
        // 5G_NGHBC,524910,500,86.0,10.5,(0,0,86.0),(0,0,86.0),(0,0,86.0),(0,0,86.0)
        // AT+CMGRMI=6
        // +CMGRMI:Main_Info,6,1,1,1,1
        // +CMGRMI:Serving_Cell,460,00,0x4d2,3032,50,504990,-910,-110
        // +CNWSEARCH:5G_NGHBC,627264,204,107.6,13.1,(0,1,107.6)
        // +CNWSEARCH:5G_NGHBC,627264,147,117.3,19.1,(0,2,117.3)
        // +CNWSEARCH:5G_NGHBC,627264,204,110.0,14.7,(0,1,110.0),(1,0,116.5)
        // +CNWSEARCH:5G_NGHBC,504990,50,90.6,10.4,(0,0,90.6)
        // +CNWSEARCH:5G_NGHBC,504990,740,113.1,14.8,(0,3,113.1)OK
        String arfcn = "", pci = "", rsrp = "";
        List<BsBeam> beamList = new ArrayList<BsBeam>();

        String[] beam = info.trim().split("\\(");
        for (int i = 0; i < beam.length; i++) {
            if (beam[i].contains(")")) {
                int beam_num = -1;
                int ssb_id = -1;
                double brsrp = 0;
                beam[i] = beam[i].trim().replace(")", "");
                beam[i] = beam[i].replace("OK", "");
                String[] data = beam[i].split(",");
                if ((data.length >= 1) && (data[0] != null)) {
                    if (!DataUtil.onlyNumeric(data[0])) {
                        continue;
                    }
                    beam_num = Integer.valueOf(data[0]);
                }
                if ((data.length >= 2) && (data[1] != null)) {
                    if (!DataUtil.onlyNumeric(data[1])) {
                        continue;
                    }
                    ssb_id = Integer.valueOf(data[1]);
                }
                if ((data.length >= 3) && (data[2] != null)) {
                    rsrp = data[2];
                    if (data[2].contains(".")) {
                        rsrp = data[2].substring(0, data[2].indexOf("."));
                    }
                    if (!DataUtil.onlyNumeric(rsrp)) {
                        return;
                    }
                    try {
                        if (Long.parseLong(rsrp) > 130) {
                            rsrp = "-110";
                        } else {
                            rsrp = "-" + rsrp;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        rsrp = "-110";
                    }

                    beamList.add(new BsBeam(beam_num, ssb_id, Integer.valueOf(rsrp)));
                }
            } else {
                String[] data = beam[i].split(",");
                if ((data.length >= 2) && (data[1] != null)) {
                    arfcn = data[1];
                    if (!DataUtil.onlyNumeric(arfcn)) {
                        return;
                    }
                }
                if ((data.length >= 3) && (data[2] != null)) {
                    pci = data[2];
                    if (!DataUtil.onlyNumeric(pci)) {
                        return;
                    }
                }
                if ((data.length >= 4) && (data[3] != null)) {
                    rsrp = data[3];
                    if (data[3].contains(".")) {
                        rsrp = data[3].substring(0, data[3].indexOf("."));
                    }
                    if (!DataUtil.onlyNumeric(rsrp)) {
                        return;
                    }
                    try {
                        if (Long.parseLong(rsrp) > 130) {
                            rsrp = "-110";
                        } else {
                            rsrp = "-" + rsrp;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        rsrp = "-110";
                    }
                }
            }
        }
        if (!arfcn.equals("") && !arfcn.equals("0") && !pci.equals("") && !pci.equals("0")) {
            addNeighberCell("", "", arfcn, pci, rsrp, beamList);
        }
    }

    private void addCell(String tac, String eci, String arfcn, String pci, String rsrp, List<BsBeam> beamList, boolean other) {
        for (int i = 0; i < curList.size(); i++) {
            if (pci.equals(curList.get(i).getPci()) && arfcn.equals(curList.get(i).getArfcn())) {
                curList.get(i).setRx(rsrp);
                if (curList.get(i).getTac().equals("") && !tac.equals("")) {
                    curList.get(i).setTac(tac);
                    curList.get(i).setEci(eci);
                    if (i != 0) {
                        LocBean tmp = curList.get(i);
                        curList.remove(i);
                        curList.add(0, tmp);
                    }
                }
                return;
            }
        }
        LocBean lc = new LocBean(tac, eci, arfcn, pci, "", rsrp, beamList);
        long band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        if (scanType == AtCmd.OP.MOBILE) {
            if (band == 28 || band == 41 || band == 79) {
                if (tac.equals("")) {
                    curList.add(lc);
                } else {
                    curList.add(0, lc);
                }
            }
        } else {
            if (band == 1 || band == 3 || band == 78) {
                if (tac.equals("")) {
                    curList.add(lc);
                } else {
                    curList.add(0, lc);
                    if (!isQsFlag() && !enableArfcnLock) {
                        if (arfcn.equals("633984")) {
                            cmdType = STATE.AT_UNLOCK;
                            lockArfcn = 627264;
                            enableArfcnLock = true;
                            lockCnt = 0;
                        } else if (arfcn.equals("627264")) {
                            cmdType = STATE.AT_UNLOCK;
                            lockArfcn = 633984;
                            enableArfcnLock = true;
                            lockCnt = 0;
                        } else {
                            enableArfcnLock = false;
                        }
                    }
                }
            }
        }
    }

    private void addNeighberCell(String tac, String eci, String arfcn, String pci, String rsrp, List<BsBeam> beamList) {
        for (int i = 0; i < curList.size(); i++) {
            if (pci.equals(curList.get(i).getPci()) && arfcn.equals(curList.get(i).getArfcn())) {
                curList.get(i).setRx(rsrp);
                if (beamList != null) {
                    curList.get(i).setBeamList(beamList);
                }
                return;
            }
        }
        long band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        if (scanType == AtCmd.OP.MOBILE) {
            if (band == 28 || band == 41 || band == 79)
                curList.add(new LocBean(tac, eci, arfcn, pci, "", rsrp, beamList));
        } else {
            if (band == 1 || band == 3 || band == 78)
                curList.add(new LocBean(tac, eci, arfcn, pci, "", rsrp, beamList));
        }
    }

    private void handleNeiberData() {
        int size = saveData.size();
        for (int i = 0; i < curList.size(); i++) {
            if (!curList.get(i).getArfcn().equals("") && !curList.get(i).getArfcn().equals("0")) {
                // earfcn != 0 or --
                for (int j = 0; j < size; j++) {
                    if (curList.get(i).getArfcn().equals(saveData.get(j).getArfcn())
                            && curList.get(i).getPci().equals(saveData.get(j).getPci())
                            && curList.get(i).getTac().equals("")) {

                        curList.get(i).setTac(saveData.get(j).getTac());
                        curList.get(i).setEci(saveData.get(j).getEci());
                        if (i != 0) {
                            LocBean tmp = curList.get(i);
                            if (curList.get(0).getTac().equals("")) {
                                curList.remove(i);
                                curList.add(0, tmp);
                            } else {
                                curList.remove(i);
                                curList.add(1, tmp);
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    public void setArfcnType(int type) {
        scanType = type;
        if (isQsFlag()) {
            cmdType = STATE.AT_STOP;
        } else {
            cmdType = STATE.AT_CNMP;
        }
        enableArfcnLock = false;
        lockArfcn = -1;
        curList.clear();
        for (Nr5gScanArfcnListener listener : listenerList) {
            if (listener != null)
                listener.onNr5gScanArfcnRsp(curList);
        }
        SdkLog.I("ARFCN: scanType: " + scanType);
    }

    public boolean isQsFlag() {
        return qsFlag;
    }

    public void setQsFlag(boolean flag) {
        qsFlag = flag;
        SdkPref.build().setArfcnMode(qsFlag);
        cmdType = STATE.AT_CRESET;
        initInfo();
        stopTimeDelay();
        startTimeDelay();
    }

    public int getBaemNum() {
        return baemNum;
    }

    public int getMinSsbIidx() {
        return minSsbIidx;
    }

    public List<LocBean> getList() {
        return curList;
    }

    public void startTimeDelay() {
        if (timerDelay == null) {
            SdkLog.I("ARFCN: startTimeDelay()");
            timerDelay = new Timer();
            timerDelay.schedule(new TimerTask() {
                public void run() {
                    if (isQsFlag()) {
                        sendQsCmd();
                    } else {
                        sendNormalCmd();
                    }
                    if (++rstCount > 25) {
                        SdkLog.I("ARFCN: rstCount = " + rstCount);
                        rstCount = 0;
                        cmdType = STATE.AT_CNMP;
                        PwrCtl.build().modulePwrCtl(true);
                    }
                }
            }, 10, 2000);
        }
    }

    private void stopTimeDelay() {
        SdkLog.E("ARFCN: stopTimeDelay()");
        if (timerDelay != null) {
            timerDelay.cancel();
            timerDelay = null;
        }
    }

    public void addOnScanArfcnListener(Nr5gScanArfcnListener listener) {
        if (!listenerList.contains(listener))
            listenerList.add(listener);
    }

    public void removeNr5gScanArfcnListener(Nr5gScanArfcnListener listener) {
        listenerList.remove(listener);
    }

    public void removeListener() {
        listenerList.clear();
    }

    private final ArrayList<Nr5gScanArfcnListener> listenerList = new ArrayList<>();
    private static Nr5g instance;
    private final List<LocBean> curList = new ArrayList<>();
    private final List<LocBean> saveData = new ArrayList<>();
    private int rstCount = 0; // 重启模块标志
    private int errorCnt = 0;
    private int lockCnt = 0;
    // 无卡 = 0 ，有卡 = 1， 未开机 = 2
    private int simCard;
    private int scanType = AtCmd.OP.MOBILE;
    private Timer timerDelay = null;
    private int cmdType;
    private int lockArfcn = -1;
    private boolean qsFlag, enableArfcnLock;
    private int baemNum, minSsbIidx;
    private static final String AT_CNMP = "AT+CNMP=71\r";
    private static final String AT_CFUN_0 = "AT+CFUN=0\r";
    private static final String AT_CFUN_1 = "AT+CFUN=1\r";
    private static final String AT_SIMCOMATI = "AT+SIMCOMATI\r";
    private static final String AT_CRESET = "AT+CRESET\r";
    private static final String AT_CPOF = "AT+CPOF\r";
    private static final String AT_CPIN = "AT+CPIN?\r";
    private static final String AT_CMGRMI = "AT+CMGRMI\r";
    private static final String AT_CMGRMI_NR = "AT+CMGRMI=6\r";
    private static final String AT_UNLOCK = "AT+C5GCELLCFG=\"unlock\"\r";
    private static final String AT_BAND_M = "AT+CSYSSEL=\"nr5g_band\",28:41:79\r";
    private static final String AT_BAND_UT = "AT+CSYSSEL=\"nr5g_band\",1:3:78\r";
    private static final String AT_ARFCN_M = "AT+C5GCELLCFG =\"arfcn\",1,1:504990\r";
    private static final String AT_ARFCN_U = "AT+C5GCELLCFG =\"arfcn\",1,1:633984\r";
    private static final String AT_ARFCN_T = "AT+C5GCELLCFG=\"arfcn\",1,1:627264\r";
    private static final String AT_ARFCN = "AT+C5GCELLCFG =\"arfcn\",1,1:"; // 锁任意频点

    private static final String AT_BAND_Q = "AT+CSYSSEL=\"nr5g_band\"\r"; //查看当前锁定BAND
    private static final String AT_CSYSSEL = "AT+CSYSSEL\r"; //恢复锁BAND到全网
    private static final String AT_START = "AT+CNWSEARCH=\"nr5g\",1\r"; //开始搜索周围5G小区
    private static final String AT_STOP = "AT+CNWSEARCH=\"nr5g\",0\r"; //查询该PCI小区的详细信息
    private static final String AT_CNWSEARCH = "AT+CNWSEARCH=\"nr5g\",2\r"; //查询该PCI小区的详细信息

    public static class STATE {
        private static final int AT_CNMP = 1;
        // for qs
        private static final int AT_CSYSSEL = 2;
        private static final int AT_BAND = 3;
        private static final int AT_START = 4;
        private static final int AT_STOP = 5;
        private static final int AT_CNWSEARCH = 6;
        // for normal
        private static final int AT_CPIN = 100;
        private static final int AT_CPSI = 101;
        private static final int AT_CNMP_NSA = 102;
        private static final int AT_CFUN_0 = 103;
        private static final int AT_CFUN_1 = 104;
        private static final int AT_CRESET = 105;
        private static final int AT_CPOF = 106;
        private static final int AT_ARFCN = 107;
        private static final int AT_CMGRMI = 108; // 打开邻区搜索功能
        private static final int AT_CMGRMI_NR = 109;
        private static final int AT_UNLOCK = 110;
        private static final int AT_SIMCOMATI = 111;
    }
}
