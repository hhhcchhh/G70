package com.Util;

import com.Logcat.APPLog;
import com.g50.ZApplication;
import com.nr70.Gnb.Bean.GnbProtocol;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

public class PrefUtil {
	
	public static PrefUtil build() {
        synchronized (PrefUtil.class) {
            if (instance == null) {
                instance = new PrefUtil();
            }
        }
        return instance;
    }

	public PrefUtil() {
		super();
		mContext = ZApplication.getContext();
		initPreferences();
	}
	
	@SuppressWarnings("deprecation")
	public void initPreferences() {
		mPrePlayMusic = mContext.getSharedPreferences(PRE_PLAY_MUSIC, Context.MODE_PRIVATE);
		mPreDropImsi = mContext.getSharedPreferences(PRE_DROP_IMSI, Context.MODE_PRIVATE);
		mPreCityList = mContext.getSharedPreferences(PRE_CITY_LIST, Context.MODE_PRIVATE);
		mPreUserPsw = mContext.getSharedPreferences(PRE_USER_PSW, Context.MODE_PRIVATE);
		mPreTac = mContext.getSharedPreferences(PRE_TAC, Context.MODE_PRIVATE);
		mPreApp = mContext.getSharedPreferences(PRE_APP, Context.MODE_PRIVATE);
	}
	public int getSsbBitmapModel() {
		return mPreApp.getInt("ssb_model", 2);
	}

	public void setSsbBitmapModel(int ssbBitmapModel){
		SharedPreferences.Editor editor = mPreApp.edit();
		editor.putInt("ssb_model", ssbBitmapModel);
		editor.apply();
	}
	public void setCityList(String list) {
		APPLog.D("setCityList(): list = " + list);

		SharedPreferences.Editor editor = mPreCityList.edit();
		editor.putString(PRE_CITY_LIST, list);
		editor.commit();
	}

	public String getCityList() {
		String list = mPreCityList.getString(PRE_CITY_LIST, "");
		APPLog.D("getCityList(): list = " + list);

		return list;
	}
	public void setCell(int cell_model){
		SharedPreferences.Editor editor = mPreTac.edit();
		editor.putInt("cell_model", cell_model);
		editor.apply();
	}
	public int getCell() {
		return mPreTac.getInt("cell_model", 1);
	}

	public void setArfcn(String arfcn,String key) {
		SharedPreferences.Editor editor = mPreTac.edit();
		editor.putString(key, arfcn);
		editor.apply();
	}

	public String getArfcn(String key) {
		return mPreTac.getString(key, "");
	}
	public void enablePlayMusic(String play) {
		APPLog.D("getPlayMusic(): play = " + play);
		SharedPreferences.Editor editor = mPrePlayMusic.edit();
		editor.putString(PRE_PLAY_MUSIC, play);
		editor.commit();
	}

	public boolean isPlayMusic() {
		String play = mPrePlayMusic.getString(PRE_PLAY_MUSIC, "true");
		if (play.equals("true")) {
			return true;
		} else {
			return false;
		}
	}

	public void setDropImsiList(List<String> imsiList) {
		StringBuffer imsi = new StringBuffer();
		for (int i = 0; i < imsiList.size(); i++) {
			imsi.append(imsiList.get(i));
			imsi.append(";");
		}
		SharedPreferences.Editor editor = mPreDropImsi.edit();
		editor.putString(PRE_DROP_IMSI, imsi.toString());
		editor.commit();
	}

	public List<String> getDropImsiList() {
		String imsi = mPreDropImsi.getString(PRE_DROP_IMSI, null);

		List<String> imsiList = new ArrayList<String>();
		if (imsi != null && imsi.length() > 0) {
			String[] data = imsi.split(";");
			for (int i = 0; i < data.length; i++) {
				if (!data[i].equals("")) {
					imsiList.add(data[i]);
				}
			}
		}
		return imsiList;
	}

	public void setUserPsw(String user_psw) {
		APPLog.D("setUserPsw(): user_psw = " + user_psw);

		SharedPreferences.Editor editor = mPreUserPsw.edit();
		editor.putString(PRE_USER_PSW, user_psw);
		editor.commit();
	}

	public String getUserPsw() {
		String user_psw = mPreUserPsw.getString(PRE_USER_PSW, DEFAULT_USER_PSW);
		APPLog.D("getUserPsw(): user = " + user_psw);
		return user_psw;
	}

	public void setTac(int tac) {
		SharedPreferences.Editor editor = mPreTac.edit();
		editor.putInt(PRE_TAC, tac);
		editor.commit();
	}

	public int getTac() {
		int tac = mPreTac.getInt(PRE_TAC, 1234);
		int n_tac = tac + GnbProtocol.MAX_TAC_NUM + 1;
		setTac(n_tac);
		return tac;
	}

	private Context mContext;
	private static PrefUtil instance;

	public final static String DEFAULT_USER_PSW = "admin,123456;";

	private SharedPreferences mPreWifiAp;
	private SharedPreferences mPrePlayMusic;
	private SharedPreferences mPreCityList;
	private SharedPreferences mPreDropImsi;
	private SharedPreferences mPreUserPsw;
	private SharedPreferences mPreTac;
	private SharedPreferences mPreApp;

	private final String PRE_PLAY_MUSIC = "play_music";
	private final String PRE_CITY_LIST = "city_list";
	private final String PRE_DROP_IMSI = "drop_imsi";
	private final String PRE_USER_PSW = "user_psw"; // 用户名密码
	private final String PRE_TAC = "ue_tac";
	private final String PRE_APP = "pre_app";
}