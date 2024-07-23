package com.Util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.Logcat.APPLog;
import com.g50.ZApplication;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class SysInfo {
    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return Build.DISPLAY;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    /**
     * 获取SN
     *
     * @return
     */
    public static String getSn(Context ctx) {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");

        } catch (Exception ignored) {
        }
        return serial;
    }
    /**
     * Flyme 说 5.0 6.0统一使用这个获取IMEI IMEI2 MEID
     *
     * @param ctx
     * @return
     */
    public static Map<String, String> getImeiAndMeid(Context ctx) {
        Map<String, String> map = new HashMap<String, String>();
        TelephonyManager mTelephonyManager = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);
        Class<?> clazz = null;
        Method method = null;//(int slotId)

        try {
            clazz = Class.forName("android.os.SystemProperties");
            method = clazz.getMethod("get", String.class, String.class);
            String gsm = (String) method.invoke(null, "ril.gsm.imei", "");
            String meid = (String) method.invoke(null, "ril.cdma.meid", "");
            map.put("meid", meid);
            /*if (!TextUtils.isEmpty(gsm)) {
                //the value of gsm like:xxxxxx,xxxxxx
                String imeiArray[] = gsm.split(",");
                if (imeiArray != null && imeiArray.length > 0) {
                    map.put("imei1", imeiArray[0]);

                    if (imeiArray.length > 1) {
                        map.put("imei2", imeiArray[1]);
                    } else {
                        map.put("imei2", mTelephonyManager.getDeviceId(1));
                    }
                } else {
                    map.put("imei1", mTelephonyManager.getDeviceId(0));
                    map.put("imei2", mTelephonyManager.getDeviceId(1));
                }
            } else {
                map.put("imei1", mTelephonyManager.getDeviceId(0));
                map.put("imei2", mTelephonyManager.getDeviceId(1));
            }*/
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return map;
    }
    /**
     * gps获取ip
     *
     * @return
     */
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("zdw-exception", ex.toString());
            ex.printStackTrace();
        }
        return null;
    }
    /**
     * wifi获取ip
     *
     * @param context
     * @return
     */
    public static String getIp(Context context) {
        try {
            //获取wifi服务
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            //判断wifi是否开启
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            return intToIp(ipAddress);
        } catch (Exception e) {
            Log.e("zdw-exception", e.toString());
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 格式化ip地址（192.168.11.1）
     */
    private static String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }
    /**
     * 3G/4g网络IP
     */
    public static String getIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("zdw-exception", e.toString());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取本机的ip地址（3中方法都包括）
     *
     * @param context
     * @return
     */
    public static String getIpAdress(Context context) {
        String ip = null;
        try {
            ip = getIp(context);
            if (ip == null) {
                ip = getIpAddress();
                if (ip == null) {
                    ip = getLocalIpAddress();
                }
            }
        } catch (Exception e) {
            Log.e("zdw-exception", e.toString());
            e.printStackTrace();
        }
        Log.e("zdw-IpAdressUtils", "ip==" + ip);
        return ip;
    }
    /**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	public static String getVersionName() {
	    try {
	        PackageManager manager = ZApplication.context().getPackageManager();
	        PackageInfo info = manager.getPackageInfo(ZApplication.context().getPackageName(), 0);
	        return info.versionName;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
    /**
     * [获取应用程序版本名称信息]
     * @return 当前应用的版本名称
     */
    public static int getVersionCode() {
        try {
            PackageManager packageManager = ZApplication.context().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    ZApplication.context().getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    /**
     * [获取应用程序版本名称信息]
     * @return 当前应用的包名
     */
    public static String getPackageName() {
        try {
            PackageManager packageManager = ZApplication.context().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    ZApplication.context().getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getImei() {
        String imei = null;
        TelephonyManager telephonyManager = ((TelephonyManager) ZApplication.context().getSystemService(Context.TELEPHONY_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ZApplication.context().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return null;
            }
        }
        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // getImei(int slotIndex)
            imei = telephonyManager.getImei();
        }
        return imei;
    }

    /**
     * 获取软件版本信息
     */
    public static String getSwVersion() {
        String ver = null;
        try {
            Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method getMethod = systemPropertiesClass.getMethod("get", new Class[] { String.class });
            ver = (String) getMethod.invoke(null, new Object[] { "ro.simdo_sw_version" });
        } catch (Exception e) {
            e.printStackTrace();
        }
        APPLog.D("getSwVersion(): ver = " + ver);

        return ver;
    }
    /**
     * 获取软件版本信息
     */
    public static String getHwVersion() {
        String ver = null;
        try {
            Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method getMethod = systemPropertiesClass.getMethod("get", new Class[] { String.class });
            ver = (String) getMethod.invoke(null, new Object[] { "ro.simdo_hw_version" });
        } catch (Exception e) {
            e.printStackTrace();
        }
        APPLog.D("getHwVersion(): " + ver);

        return ver;
    }

    /**
     * 获取版本号
     * @return 当前应用的版本号
     */
    public static String getAppVersion() {
        try {
            PackageManager manager = ZApplication.context().getPackageManager();
            PackageInfo info = manager.getPackageInfo(ZApplication.context().getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
