package com.Wifi;

import java.text.DecimalFormat;

/**
 * 数据格式
 * <p>
 * SSID、BSSID、level、frequency[MHz]、capabilities[dbm]
 */
public class WifiBean {

    public static final int LENGTH = 5;

    String wifiName;
    String wifiMac;
    int frequency;
    int rssi;
    String capabilities; // 安全方式

    boolean trace;
    boolean is5g; // 5G wifi
    boolean isHot; // 是否为热点
    int chanel; // 信道号

    public WifiBean(String wifiName, String wifiMac, int rssi, int frequency, String capabilities) {
        this.wifiName = wifiName;
        this.wifiMac = wifiMac;
        this.rssi = rssi;
        this.frequency = frequency;
        this.capabilities = capabilities;
        this.trace = trace;

        chanel = calcChanel(frequency);
        is5g = is5GHz(frequency);
        isHot = true;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public boolean isTrace() {
        return trace;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public String getWifiMac() {
        return wifiMac;
    }

    public void setWifiMac(String wifiMac) {
        this.wifiMac = wifiMac;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public boolean isHot() {
        return isHot;
    }

    public void setHot(boolean hot) {
        isHot = hot;
    }

    public boolean isIs5g() {
        return is5g;
    }

    public void setIs5g(boolean is5g) {
        this.is5g = is5g;
    }

    public int getChanel() {
        return chanel;
    }

    public void setChanel(int chanel) {
        this.chanel = chanel;
    }

    public String  calcDistance(int rssi) {
		/*距离公式：
		refDistance = 1.0   # 1m    == d0 in formula
		pathLoss = 3.0      #       == n in formula
		此2个参数，可以根据实际情况调整。
		Def  doit_rssi(txPower, rssi):
		    c1 = txPower
		    c2 = pathLoss
		distance=refDistance * math.pow(10, (c1-rssi) / (10 * c2))/100
		根据公式计算距离，其中txPower=21*/

        double c1 = 21.0;
        double c2 = 3.0;

        double dis = 1.0 * Math.pow(10, (c1 - rssi) / (10 * c2)) / 100;
        DecimalFormat df = new DecimalFormat("######0.0");

        return df.format(dis);
    }

    public boolean is24GHz(int freq) {
        return freq > 2400 && freq < 2500;
    }

    public boolean is5GHz(int freq) {
        return freq > 4900 && freq < 5900;
    }

    public int calcChanel(int freq) {
        if (freq == 2412) {
            return 1;
        } else if (freq == 2417) {
            return 2;
        } else if (freq == 2422) {
            return 3;
        } else if (freq == 2427) {
            return 4;
        } else if (freq == 2432) {
            return 5;
        } else if (freq == 2437) {
            return 6;
        } else if (freq == 2442) {
            return 7;
        } else if (freq == 2447) {
            return 8;
        } else if (freq == 2452) {
            return 9;
        } else if (freq == 2457) {
            return 10;
        } else if (freq == 2462) {
            return 11;
        } else if (freq == 2467) {
            return 12;
        } else if (freq == 2472) {
            return 13;
        } else if (freq == 2484) {
            return 14;
        } else if (freq == 5180) {
            return 36;
        } else if (freq == 5190) {
            return 38;
        } else if (freq == 5200) {
            return 40;
        } else if (freq == 5210) {
            return 42;
        } else if (freq == 5220) {
            return 44;
        } else if (freq == 5230) {
            return 46;
        } else if (freq == 5240) {
            return 48;
        } else if (freq == 5260) {
            return 52;
        } else if (freq == 5270) {
            return 54;
        } else if (freq == 5280) {
            return 56;
        } else if (freq == 5290) {
            return 58;
        } else if (freq == 5300) {
            return 60;
        } else if (freq == 5310) {
            return 62;
        } else if (freq == 5320) {
            return 64;
        } else if (freq == 5500) {
            return 100;
        } else if (freq == 5510) {
            return 102;
        } else if (freq == 5520) {
            return 104;
        } else if (freq == 5530) {
            return 106;
        } else if (freq == 5540) {
            return 108;
        } else if (freq == 5550) {
            return 110;
        } else if (freq == 5560) {
            return 112;
        } else if (freq == 5580) {
            return 116;
        } else if (freq == 5590) {
            return 118;
        } else if (freq == 5600) {
            return 120;
        } else if (freq == 5610) {
            return 122;
        } else if (freq == 5620) {
            return 124;
        } else if (freq == 5630) {
            return 126;
        } else if (freq == 5640) {
            return 128;
        } else if (freq == 5660) {
            return 132;
        } else if (freq == 5670) {
            return 134;
        } else if (freq == 5680) {
            return 136;
        } else if (freq == 5690) {
            return 138;
        } else if (freq == 5700) {
            return 140;
        } else if (freq == 5710) {
            return 142;
        } else if (freq == 5720) {
            return 144;
        } else if (freq == 5745) {
            return 149;
        } else if (freq == 5755) {
            return 151;
        } else if (freq == 5765) {
            return 153;
        } else if (freq == 5775) {
            return 155;
        } else if (freq == 5785) {
            return 157;
        } else if (freq == 5795) {
            return 159;
        } else if (freq == 5805) {
            return 161;
        } else if (freq == 5825) {
            return 165;
        } else if (freq == 5835) {
            return 167;
        } else if (freq == 5845) {
            return 169;
        } else if (freq == 5855) {
            return 171;
        } else if (freq == 5865) {
            return 173;
        } else if (freq == 5875) {
            return 175;
        } else if (freq == 5885) {
            return 177;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "wifiName = " + wifiName + " wifiMac = " + wifiMac + " frequency = " + frequency + " rssi = " + rssi + " capabilities = " + capabilities + " is5g = " + is5g + " isHot = " + isHot + " chanel = " + chanel;
    }
}