package com.simdo.dw_db_s.Util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.dwdbsdk.Bean.UeidBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
	/**
	 * delay ms
	 * @param time
	 */
	public static void delay(long time) {

		long waitTime = System.currentTimeMillis() + time;
		while (System.currentTimeMillis() < waitTime) {

		}
	}
	/**
	 * 短暂显示提示信息
	 *
	 * @param context
	 *
	 * @param msg
	 */
	public static void showToast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	/**
	 * 相同字串匹配
	 *
	 * @param datalist
	 * @param data
	 * @return
	 */
	public static boolean existSameData(List<String> datalist, String data) {
		for (int i = 0; i < datalist.size(); i++) {
			String info = datalist.get(i);
			if (info.equals(data)) {
				return true;
			}
		}
		return false;
	}
	public static boolean existSameBL(List<UeidBean> datalist, String data) {
		for (int i = 0; i < datalist.size(); i++) {
			String info = datalist.get(i).getImsi();
			if (info.equals(data)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 整型
	 * @param content
	 * @return
	 */
	public static boolean onlyNumeric(String content) {
		if (content == null || content.equals("")) {
			return false;
		}
		Pattern pattern = Pattern.compile("[0-9]+");
		Matcher isNum = pattern.matcher(content);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	public static List<Integer> json2Int(String str,String band) throws JSONException {
		List<Integer> list = new ArrayList<>();
		if (!TextUtils.isEmpty(str)){
			JSONArray jsonArray = new JSONArray(str);
			for(int i = 0; i < jsonArray.length(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				list.add(Integer.parseInt(object.getString(band)));
			}
		}
		return list;
	}

	public static String int2Json(List<Integer> list,String key) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		JSONObject tmpObj = null;
		for(int i = 0; i < list.size(); i++) {
			tmpObj = new JSONObject();
			tmpObj.put(key , list.get(i));
			jsonArray.put(tmpObj);
		}
		return jsonArray.toString(); // 将JSONArray转换得到String
	}

	/**
	 * dp转换成px
	 */
	public static int dp2px(Context context, float dpValue) {
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}
