package com.simdo.g73cs.Util;

import static com.simdo.g73cs.MainActivity.devA;
import static com.simdo.g73cs.MainActivity.devB;
import static com.simdo.g73cs.MainActivity.devC;
import static com.simdo.g73cs.MainActivity.devD;
import static com.simdo.g73cs.Util.Util.getString;

import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.Gnb.Bean.GnbProtocol;
import com.simdo.g73cs.Fragment.TraceCatchFragment;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeviceUtil {
    //通过设备名判断是第几个
    //只有ByDeviceName的方法不需要devicelist内有设备
    public static int getLogicIndexByDeviceName(String deviceName) {
        if (deviceName.contains(devA)) {
            return 0;
        } else if (deviceName.contains(devB)) {
            return 1;
        } else if (deviceName.contains(devC)) {
            return 2;
        } else if (deviceName.contains(devD)) {
            return 3;
        }
        return -1;
    }

    public static int getLogicIndexById(String deviceId) {
        String deviceName;
        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
            if (MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId().contains(deviceId)) {
                deviceName = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName();
                return getLogicIndexByDeviceName(deviceName);
            }
        }
        return -1;
    }

    public static int getIndexByChannelNum(int channelNum) {
        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
            if (MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName().contains(getDeviceNameByChannelNum(channelNum))) {
                return i;
            }
        }
        return -1;
    }

    public static int getIndexById(String deviceId) {
        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
            if (MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId().contains(deviceId)) {
                return i;
            }
        }
        return -1;
    }

    public static String getDeviceNameByChannelNum(int channelNum) {
        switch (channelNum) {
            case 1:
            case 2:
                return devA;
            case 3:
            case 4:
                return devB;
            case 5:
            case 6:
                return devC;
            case 7:
            case 8:
                return devD;
        }
        return "";
    }

    public static int getLogicIndexByChannelNum(int channelNum) {
        switch (channelNum) {
            case 1:
            case 2:
                return 0;
            case 3:
            case 4:
                return 1;
            case 5:
            case 6:
                return 2;
            case 7:
            case 8:
                return 3;
        }
        return 0;
    }

    public static int getDeviceChannelIndexByChannelNum(int channelNum) {
        switch (channelNum) {
            case 1:
            case 3:
            case 5:
            case 7:
                return GnbProtocol.CellId.FIRST;
            case 2:
            case 4:
            case 6:
            case 8:
                return GnbProtocol.CellId.SECOND;

        }
        return GnbProtocol.CellId.FIRST;
    }

    public static String getCellStr(int logicIndex, int cell_id) {
        int channelNum = logicIndex * 2 + cell_id + 1;
        if (channelNum == 1) return getString(R.string.first);
        else if (channelNum == 2) return getString(R.string.second);
        else if (channelNum == 3) return getString(R.string.third);
        else if (channelNum == 4) return getString(R.string.fourth);
        else if (channelNum == 5) return "五";
        else if (channelNum == 6) return "六";
        else if (channelNum == 7) return "七";
        else if (channelNum == 8) return "八";
        else return "一";
    }

    public static int getCellIdByChannelNum(int channelNum) {
        switch (channelNum) {
            case 1:
            case 3:
            case 5:
            case 7:
                return 0;
            case 2:
            case 4:
            case 6:
            case 8:
                return 1;
        }
        return 0;
    }

    public static TraceUtil getTraceUtilByLogicIndex(int logicIndex) {
        switch (logicIndex) {
            case 0:
                return TraceCatchFragment.mLogicTraceUtilNr;
            case 1:
                return TraceCatchFragment.mLogicTraceUtilNr2;
            case 2:
                return TraceCatchFragment.mLogicTraceUtilLte;
            case 3:
                return TraceCatchFragment.mLogicTraceUtilLte2;
        }
        return null;
    }

    public static TraceUtil getTraceUtilByDeviceName(String devName) {
        if (devName.contains(devA)) return TraceCatchFragment.mLogicTraceUtilNr;
        else if (devName.contains(devB)) return TraceCatchFragment.mLogicTraceUtilNr2;
        else if (devName.contains(devC)) return TraceCatchFragment.mLogicTraceUtilLte;
        else return TraceCatchFragment.mLogicTraceUtilLte2;
    }


    public static int getChannelNumByarfcn_G73CZ2plus2(String arfcn) {
        int band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        int lteBand = LteBand.earfcn2band(Integer.parseInt(arfcn));
        int channelNum = 0;
        if (Integer.parseInt(arfcn) > 100000) {
            switch (band) {
                case 41:
                    channelNum = 1;
                    break;
                case 78:
                case 79:
                    channelNum = 3;
                    break;
                case 1:
                    channelNum = 2;
                    break;
                case 28:
                    channelNum = 4;
                    break;
            }
        } else {
            switch (lteBand) {
                case 3:
                case 34:
                    channelNum = 5;
                    break;
                case 5:
                case 39:
                    channelNum = 6;
                    break;
                case 8:
                case 40:
                    channelNum = 7;
                    break;
                case 1:
                case 41:
                    channelNum = 8;
                    break;
            }
        }
        return channelNum;
    }
}
