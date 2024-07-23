package com.Wifi;

import java.util.List;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

public class WifiAdmin {
	/**
	 * These values are matched in string arrays -- changes must be kept in sync
	 */
	private static final int SECURITY_NONE = 0;
	private static final int SECURITY_WEP = 1;
	private static final int SECURITY_PSK = 2;
	// / M: security type @{
	private static final int SECURITY_WPA_PSK = 3;
	private static final int SECURITY_WPA2_PSK = 4;
	private static final int SECURITY_EAP = 5;
	private static final int SECURITY_WAPI_PSK = 6;
	private static final int SECURITY_WAPI_CERT = 7;
    //定义一个WifiManager对象
	private WifiManager mWifiManager;
	//定义一个WifiInfo对象
	private WifiInfo mWifiInfo;
	//扫描出的网络连接列表
	private List<ScanResult> mWifiList;
	//网络连接列表
	private List<WifiConfiguration> mWifiConfigurations;
	WifiLock mWifiLock;
	private boolean mWifiEnable = true;

	public WifiAdmin(Context context) {
		//取得WifiManager对象
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		/** Keep Wi-Fi awake */
		mWifiLock = mWifiManager.createWifiLock(
				WifiManager.WIFI_MODE_SCAN_ONLY, "WiFi");
		if (false == mWifiLock.isHeld()) {
			mWifiLock.acquire();
		}

		//取得WifiInfo对象
		mWifiInfo = mWifiManager.getConnectionInfo();
	}

	//打开wifi
	public void openWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}
	//关闭wifi
	public void closeWifi(){
		if(mWifiManager.isWifiEnabled()){
			mWifiManager.setWifiEnabled(false);
		}
	}
	 // 检查当前wifi状态  
    public int checkState() {  
        return mWifiManager.getWifiState();  
    }  
	//锁定wifiLock
	public void acquireWifiLock(){
		mWifiLock.acquire();
	}
	//解锁wifiLock
	public void releaseWifiLock(){
		//判断是否锁定
		if(mWifiLock.isHeld()){
			mWifiLock.acquire();
		}
	}
	//创建一个wifiLock
	public void createWifiLock(){
		mWifiLock=mWifiManager.createWifiLock("test");
	}
	//得到配置好的网络
	public List<WifiConfiguration> getConfiguration(){
		return mWifiConfigurations;
	}
	//指定配置好的网络进行连接
	public void connetionConfiguration(int index){
		if(index>mWifiConfigurations.size()){
			return ;
		}
		//连接配置好指定ID的网络
		mWifiManager.enableNetwork(mWifiConfigurations.get(index).networkId, true);
	}
	public void startScan() {
		if (mWifiList != null) {
			mWifiList.clear();
		}
		mWifiManager.startScan();
		//得到扫描结果
		mWifiList=mWifiManager.getScanResults();
		//得到配置好的网络连接
		mWifiConfigurations=mWifiManager.getConfiguredNetworks();
	}
	//得到网络列表
	public List<ScanResult> getWifiList(){
		return mWifiList;
	}
	//查看扫描结果
	public StringBuffer lookUpScan(){
		StringBuffer sb=new StringBuffer();
		int len = mWifiList.size();
		if (len <= 0) {
			sb.append("\n");
		} else {
			for(int i = 0; i < len; i++) {
				 // 将ScanResult信息转换成一个字符串包  
	            // 其中把包括：BSSID、SSID、capabilities、frequency、level  
				sb.append("\n");
				sb.append("名称(SSID): ");
				sb.append(mWifiList.get(i).SSID);
				sb.append("\n");
				sb.append("MAC(BSSID): ");
				sb.append(mWifiList.get(i).BSSID);
				sb.append("\n");
				sb.append("信号强度(level): ");
				sb.append(mWifiList.get(i).level + " dbm");
				sb.append("\n");
				sb.append("频率: ");
				sb.append(mWifiList.get(i).frequency + " MHz");
				sb.append("\n");
				sb.append("加密类型: ");
				sb.append(mWifiList.get(i).capabilities);
				sb.append("\n");
			}
		}
		return sb;	
	}

	public String getSSID(){
		return (mWifiInfo==null)?"NULL":mWifiInfo.getSSID();
	}
	public String getMacAddress(){
		return (mWifiInfo==null)?"NULL":mWifiInfo.getMacAddress();
	}
	public String getBSSID(){
		return (mWifiInfo==null)?"NULL":mWifiInfo.getBSSID();
	}
	public int getIpAddress(){
		return (mWifiInfo==null)?0:mWifiInfo.getIpAddress();
	}
	//得到连接的ID
	public int getNetWordId(){
		return (mWifiInfo==null)?0:mWifiInfo.getNetworkId();
	}
	//得到wifiInfo的所有信息
	public String getWifiInfo(){
		return (mWifiInfo==null)?"NULL":mWifiInfo.toString();
	}
	//添加一个网络并连接
	public void addNetWork(WifiConfiguration configuration){
		int wcgId=mWifiManager.addNetwork(configuration);
		mWifiManager.enableNetwork(wcgId, true);
	}
	//断开指定ID的网络
	public void disConnectionWifi(int netId){
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
	}

	public int getSecurity(ScanResult result) {
		if (result.capabilities.contains("WAPI-PSK")) {
			// / M: WAPI_PSK
			return SECURITY_WAPI_PSK;
		} else if (result.capabilities.contains("WAPI-CERT")) {
			// / M: WAPI_CERT
			return SECURITY_WAPI_CERT;
		} else if (result.capabilities.contains("WEP")) {
			return SECURITY_WEP;
		} else if (result.capabilities.contains("PSK")) {
			return SECURITY_PSK;
		} else if (result.capabilities.contains("EAP")) {
			return SECURITY_EAP;
		}
		return SECURITY_NONE;
	}

	public void onResume() {
		switch (mWifiManager.getWifiState()) {
		case WifiManager.WIFI_STATE_DISABLED:
			mWifiEnable = false;
			mWifiManager.setWifiEnabled(!mWifiEnable);
			break;
		case WifiManager.WIFI_STATE_DISABLING:

			break;
		case WifiManager.WIFI_STATE_UNKNOWN:

			break;
		default:
			break;
		}
	}

	public void onDestory() {
		try {
			if (true == mWifiLock.isHeld()) {
				mWifiLock.release();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//closeWifi();
	}
}
