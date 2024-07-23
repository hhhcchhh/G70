package com.simdo.g758s_predator.Util;

import java.net.UnknownHostException;

public class IpUtil {

    private static IpUtil instance;

    public static IpUtil build() {
        synchronized (IpUtil.class) {
            if (instance == null) {
                instance = new IpUtil();
            }
        }
        return instance;
    }

    public IpUtil() {
    }

    /**
     * string IP to INT ip
     *
     * @param ipStr
     * @return
     */
    public static int strIp2intIp(String ipStr) throws UnknownHostException {
        try {
            if (ipStr == null)
                return 0;

            String[] parts = ipStr.split("\\.");
            if (parts.length != 4) {
                throw new UnknownHostException(ipStr);
            }
            int a = Integer.parseInt(parts[0]);
            int b = Integer.parseInt(parts[1]) << 8;
            int c = Integer.parseInt(parts[2]) << 16;
            int d = Integer.parseInt(parts[3]) << 24;
            return a | b | c | d;
        } catch (NumberFormatException ex) {
            throw new UnknownHostException(ipStr);
        }
    }
    /**
     * Int IP to String ip
     * @param iip
     * @return
     */
    public static String intIp2StrIp(int iip) {
        return ((iip & 0xFF) + "." + ((iip >> 8) & 0xFF) + "." + ((iip >> 16) & 0xFF) + "." + ((iip >> 24) & 0xFF));
    }
}
