package com.simdo.g73cs.Util;

import static com.simdo.g73cs.MainActivity.devA;
import static com.simdo.g73cs.MainActivity.devB;
import static com.simdo.g73cs.MainActivity.devC;
import static com.simdo.g73cs.Util.Util.getString;

import com.dwdbsdk.Bean.DW.DWProtocol;
import com.dwdbsdk.Bean.LteBand;
import com.dwdbsdk.Bean.NrBand;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;

//只有ByDeviceName的方法不需要devicelist内有设备,否则会闪退！
public class DeviceUtil {
    public static int MaxDeviceCount = 3;     //最大设备数
    public static int MaxCellIdPerDevice = 4;     //每个设备的通道数
    public static int MaxChannelNum = MaxDeviceCount * MaxCellIdPerDevice;     //最大通道数

    //通过设备名判断是第几个
    public static int getLogicIndexByDeviceName(String deviceName) {
        if (deviceName.contains(devA)) {
            return 0;
        } else if (deviceName.contains(devB)) {
            return 1;
        } else if (deviceName.contains(devC)) {
            return 2;
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
            case 3:
            case 4:
                return devA;
            case 5:
            case 6:
            case 7:
            case 8:
                return devB;
            case 9:
            case 10:
            case 11:
            case 12:
                return devC;
        }
        return "";
    }

    public static int getLogicIndexByChannelNum(int channelNum) {
        switch (channelNum) {
            case 1:
            case 2:
            case 3:
            case 4:
                return 0;
            case 5:
            case 6:
            case 7:
            case 8:
                return 1;
            case 9:
            case 10:
            case 11:
            case 12:
                return 2;
        }
        return 0;
    }

    public static String getCellStr(int logicIndex, int cell_id) {
        int channelNum = DeviceUtil.getChannelNum(logicIndex, cell_id);
        if (channelNum == 1) return getString(R.string.first);
        else if (channelNum == 2) return getString(R.string.second);
        else if (channelNum == 3) return getString(R.string.third);
        else if (channelNum == 4) return getString(R.string.fourth);
        else if (channelNum == 5) return "五";
        else if (channelNum == 6) return "六";
        else if (channelNum == 7) return "七";
        else if (channelNum == 8) return "八";
        else if (channelNum == 9) return "九";
        else if (channelNum == 10) return "十";
        else if (channelNum == 11) return "十一";
        else if (channelNum == 12) return "十二";
        else return "一";
    }

    public static int getCellIdByChannelNum(int channelNum) {
        switch (channelNum) {
            case 1:
            case 5:
            case 9:
                return DWProtocol.CellId.FIRST;
            case 2:
            case 6:
            case 10:
                return DWProtocol.CellId.SECOND;
            case 3:
            case 7:
            case 11:
                return DWProtocol.CellId.THIRD;
            case 4:
            case 8:
            case 12:
                return DWProtocol.CellId.FOURTH;
        }
        return DWProtocol.CellId.FIRST;
    }



    public static int getMainChannelNumByarfcn_3G758_CX(String arfcn) {
        int band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        int lteBand = LteBand.earfcn2band(Integer.parseInt(arfcn));
        int channelNum = 0;
        if (Integer.parseInt(arfcn) > 100000) {
            switch (band) {
                case 41:
                    channelNum = 7;
                    break;
                case 78:
                case 79:
                    channelNum = 1;
                    break;
                case 1:
                    channelNum = 3;
                    break;
                case 28:
                    channelNum = 5;
                    break;
            }
        } else {
            switch (lteBand) {
                case 3:
                    channelNum = 4;
                    break;
                case 34:
                    channelNum = 12;
                    break;
                case 8:
                case 5:
                    channelNum = 11;
                    break;
                case 39:
                    channelNum = 10;
                    break;
                case 40:
                    channelNum = 8;
                    break;
                case 1:
                    channelNum = 2;
                    break;
                case 41:
                    channelNum = 7;
                    break;
            }
        }
        return channelNum;
    }

    public static int getChannelNum(int logicIndex, int cell_id) {
        return logicIndex * 4 + (cell_id + 1);
    }

    public static boolean isNr(int channelNum) {
        switch (channelNum) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 9:
                return true;
            case 2:
            case 4:
            case 6:
            case 8:
            case 10:
            case 11:
            case 12:
                return false;
        }
        return true;
    }

    public static boolean arfcnIsNr(int arfcn) {
        return arfcn > 100000;
    }

    public static boolean arfcnIsNr(String arfcn) {
        if (!arfcn.matches("^\\d+$")) return false;     //  全为数字
        return Integer.parseInt(arfcn) > 100000;
    }

    public static boolean isTdd(boolean isNr, int band) {
        if (isNr) {
            switch (band) {
                case 41:
                case 78:
                case 79:
                    return true;
                case 1:
                case 3:
                case 28:
                    return false;
            }
        } else {
            switch (band) {
                case 1:
                case 3:
                case 5:
                case 8:
                    return false;
                case 34:
                case 39:
                case 40:
                case 41:
                    return true;
            }
        }
        return true;
    }

    public static String getPlmn(int arfcn) {
        boolean isNr = arfcn > 100000;
        int band;
        if (isNr) {
            band = NrBand.earfcn2band(arfcn);
            if (band == 1 || band == 3 || band == 78) {
                return "46001";
            } else if (band == 28 || band == 41 || band == 79) {
                return "46000";
            }
        } else {
            band = LteBand.earfcn2band(arfcn);
            if (band == 34 || band == 39 || band == 40 || band == 41) {
                return "46000";
            } else if (band == 1 || band == 5) {
                return "46001";
            } else if (band == 3 || band == 8) {
                int freq = LteBand2.earfcn2freq(arfcn);
                if ((freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949))
                    return "46000";
                else return "46001";
            }
        }
        return "46000";
    }
}
