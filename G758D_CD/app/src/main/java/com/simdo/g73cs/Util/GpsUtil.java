package com.simdo.g73cs.Util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.Logcat.SLog;
import com.simdo.g73cs.ZApplication;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GpsUtil {
    private static GpsUtil instance;

    public static GpsUtil build() {
        synchronized (GpsUtil.class) {
            if (instance == null) {
                instance = new GpsUtil();
            }
        }
        return instance;
    }

    public GpsUtil() {
        mContext = ZApplication.getInstance().getContext();
        gpsTime = 0;
    }

    public void openGPSSettings() {
        SLog.D("GPS: openGPSSettings()" );
        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        }
        getLocation();
    }

    // 状态监听
    GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                // 第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    // mGpsStatus.setText("第一次定位");
                    break;
                // 卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    // 获取当前状态
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    Activity#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for Activity#requestPermissions for more details.
                            return;
                        }
                    }
                    GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                    // 获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    // 创建一个迭代器保存所有卫星
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    int inusedcnt = 0;
                    while (iters.hasNext() && count <= maxSatellites) {
                        GpsSatellite gpsS = iters.next();
                        numSatelliteList.add(gpsS);
                        if (gpsS.usedInFix()) {
                            inusedcnt++;
                        }
                        count++;
                    }
                    break;
                // 定位启动
                case GpsStatus.GPS_EVENT_STARTED:
                    // setProgressBarIndeterminateVisibility(true);
                    // mGpsStatus.setText("定位启动");
                    break;
                // 定位结束
                case GpsStatus.GPS_EVENT_STOPPED:
                    // setProgressBarIndeterminateVisibility(false);
                    // mGpsStatus.setText( "定位结束");
                    break;
            }
        }

        ;
    };

    private void getLocation() {
        // 获取位置管理服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        String provider = LocationManager.GPS_PROVIDER;
        Location location = mLocationManager.getLastKnownLocation(provider);// 通过GPS获取位置
        if (location == null) {
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } else {
            mGpsLongitude = 0;
            mGpsLatitude = 0;
            mGpsSpeed = 0;
            mGpsBearing = 0;
            mGpsAltitude = 0;
            mGpsAzimuth = 0;
            location.reset();
        }
        updateToNewLocation(location);
        // 设置每60秒，每移动十米向LocationProvider获取一次GPS的定位信息
        // 当LocationProvider可用，不可用或定位信息改变时，调用updateView,更新显示
        // mLocationManager.requestLocationUpdates(provider, 60000, 10,
        // mLocationListener);
        // 设置监听器，自动更新的最小时间为间隔N秒(1秒为1*1000)或最小位移变化超过N米
        mLocationManager.requestLocationUpdates(provider, 1000, 0, mLocationListener);
        mLocationManager.addGpsStatusListener(mGpsStatusListener); // 注册状态信息回调
    }

    public void resetGps() {
        // 获取位置管理服务
        String provider = LocationManager.GPS_PROVIDER;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        Location location = mLocationManager.getLastKnownLocation(provider);// 通过GPS获取位置
        if (location == null) {
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location != null) {
            location.reset();
        }
        mGpsLongitude = 0;
        mGpsLatitude = 0;
        mGpsSpeed = 0;
        mGpsBearing = 0;
        mGpsAltitude = 0;
        mGpsAzimuth = 0;
        updateToNewLocation(location);
    }

    private void updateToNewLocation(Location location) {
        StringBuffer buffer = new StringBuffer();
        if (location == null) {
            return;
        }
        mGpsLongitude = location.getLongitude();
        mGpsLatitude = location.getLatitude();
        mGpsSpeed = location.getSpeed();
        mGpsBearing = location.getBearing();
        mGpsAltitude = location.getAltitude();
        gpsTime = location.getTime();
    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
            if (location != null) {
                updateToNewLocation(location);
            }
        }

        public void onProviderDisabled(String provider) {
            // Provider被disable时触发此函数，比如GPS被关闭
            updateToNewLocation(null);
        }

        public void onProviderEnabled(String provider) {
            // Provider被enable时触发此函数，比如GPS被打开
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Provider的转态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        }
    };

    public boolean isConnected() {
        if (mGpsLongitude == 0.000000 || mGpsLatitude == 0.000000) {
            return false;
        }
        return true;
    }

    public long getGpsTime() {
        if (gpsTime > 0) {
            SLog.D("gpsTime = " + gpsTime + ": " + DateUtil.longToDate(gpsTime));
        }
        return gpsTime;
    }

    public String getGpsLongitude() {
        DecimalFormat df = new DecimalFormat("######0.000000");
        String tmp = df.format(mGpsLongitude);
        return tmp;
    }

    public String getGpsLatitude() {
        DecimalFormat df = new DecimalFormat("######0.000000");
        String tmp = df.format(mGpsLatitude);
        return tmp;
    }

    public String getGpsSpeed() {
        DecimalFormat df = new DecimalFormat("######0.000");
        String tmp = df.format(mGpsSpeed);
        return tmp;
    }

    public String getGpsBearing() {
        DecimalFormat df = new DecimalFormat("######0.000");
        String tmp = df.format(mGpsBearing);
        return tmp;
    }

    public String getGpsAltitude() {
        DecimalFormat df = new DecimalFormat("######0.000");
        String tmp = df.format(mGpsAltitude);
        return tmp;
    }

    public void onStop() {
        numSatelliteList.clear();
        if (mLocationManager!=null){
            mLocationManager.removeUpdates(mLocationListener);
            mLocationManager.removeGpsStatusListener(mGpsStatusListener);
            mLocationManager = null;
        }
    }

    public double mGpsLongitude = 0;
    public double mGpsLatitude = 0;
    public double mGpsSpeed = 0;
    public double mGpsBearing = 0;
    public double mGpsAltitude = 0;
    public double mGpsAzimuth = 0;
    private final List<GpsSatellite> numSatelliteList = new ArrayList<>(); // 卫星信号
    private Context mContext;
    private LocationManager mLocationManager;

    private long gpsTime;
}
