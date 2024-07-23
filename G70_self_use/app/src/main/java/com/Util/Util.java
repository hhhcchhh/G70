package com.Util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.g50.R;
import com.g50.UI.Bean.ScanArfcnBean;
import com.nr70.Gnb.Bean.UeidBean;

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
		//创建一个Toast
		Toast toast = new Toast(context);
		//创建Toast中的文字
		TextView textView = new TextView(context);
		textView.setText(msg);
		textView.setBackground(context.getDrawable(R.drawable.radio_n));
		//文字设置颜色
		textView.setTextColor(Color.WHITE);
		textView.setGravity(Gravity.CENTER);
		//文字设置大小
		textView.setTextSize(14);
		//设置内边距
		textView.setPadding(20, 10, 20, 10);
		//把layout设置进入Toast
		toast.setView(textView);
		//设置Toast位置居中
		Point size = new Point();
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getSize(size);
		toast.setGravity(Gravity.BOTTOM, 0, size.y/8);
		//设置显示时间
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.show();
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
	 *
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
	public static List<ScanArfcnBean> sortList_op(List<ScanArfcnBean> data){
		List<ScanArfcnBean> list = new ArrayList<>();
		for (int i = 0; i < data.size(); i++) {
			if (!TextUtils.isEmpty(data.get(i).getTac())){
				list.add(0,data.get(i));
			}else {
				list.add(data.get(i));
			}
		}
		return list;
	}
	public static int Mode3(int pci){
		if (pci%3!=0){
			pci -=1;
		}else{
			pci +=1;
		}
		return pci;
	}
}
