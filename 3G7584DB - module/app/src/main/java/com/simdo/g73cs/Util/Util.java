package com.simdo.g73cs.Util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.dwdbsdk.Bean.UeidBean;
import com.simdo.g73cs.R;
import com.simdo.g73cs.ZApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    private static Toast toast;
    private static boolean isToastShowing = false;
    /**
     * delay ms
     *
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
     * @param msg
     */
    public static void showToast(String msg) {
        //如果正在显示什么都不做
        if (isToastShowing) {
            return;
        }
        //创建Toast中的文字
        TextView textView = new TextView(ZApplication.getInstance().getContext());
        textView.setText(msg);
        textView.setBackgroundResource(R.drawable.radio_main);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14);
        textView.setPadding(24, 14, 24, 14);
//        if (toast != null) {
//            toast.cancel();
//        }

        if (toast == null) {
            toast = new Toast(ZApplication.getInstance().getContext());
            toast.setView(textView);
            toast.setGravity(Gravity.BOTTOM, 0, 200);
            toast.setDuration(Toast.LENGTH_SHORT);

        } else {
            // 如果toast不为null但没有显示，更新Toast的文本内容并重新显示
            ((TextView) toast.getView()).setText(msg);

        }
        isToastShowing = true;
        toast.show();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                isToastShowing = false;
            }
        }, 2000);
    }
    /**
     * 如果没有其他提示信息短暂显示提示信息
     *
     * @param msg
     */
    public static void showToastIfToastNotExist(String msg) {
        //如果正在显示什么都不做
        if (isToastShowing) {
            return;
        }
        //创建Toast中的文字
        TextView textView = new TextView(ZApplication.getInstance().getContext());
        textView.setText(msg);
        textView.setBackgroundResource(R.drawable.radio_main);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14);
        textView.setPadding(24, 14, 24, 14);
        if (toast != null) {
            toast.cancel();
        }
        toast = new Toast(ZApplication.getInstance().getContext());
        toast.setView(textView); //把layout设置进入Toast
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_SHORT);
        isToastShowing = true;
        toast.show();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                isToastShowing = false;
            }
        }, 2000); // Adjust time based on the toast duration
    }

    public static void showToast(String msg, int duration) {
        //如果正在显示什么都不做
        if (isToastShowing) {
            return;
        }
        //创建Toast中的文字
        TextView textView = new TextView(ZApplication.getInstance().getContext());
        textView.setText(msg);
        textView.setBackgroundResource(R.drawable.radio_main);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14);
        textView.setPadding(24, 14, 24, 14);
//        if (toast != null) {
//            toast.cancel();
//        }

        if (toast == null) {
            toast = new Toast(ZApplication.getInstance().getContext());
            toast.setView(textView);
            toast.setGravity(Gravity.BOTTOM, 0, 200);
            toast.setDuration(duration);

        } else {
            // 如果toast不为null但没有显示，更新Toast的文本内容并重新显示
            ((TextView) toast.getView()).setText(msg);

        }
        isToastShowing = true;
        toast.show();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                isToastShowing = false;
            }
        }, duration == Toast.LENGTH_LONG ? 3500 : 2000); // Adjust time based on the toast duration
    }
    public static void showToast(Context context, String msg) {
        showToast(msg);
    }
    /*
     * 取消提示信息
     *
     * */
    public static void cancelToast() {
        if (toast != null) {
            toast.cancel();
        }
    }
    public static String getString(@StringRes int resId) {
        return ZApplication.getInstance().getContext().getResources().getString(resId);
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

    //在jsonArray中取出jsonObject并返回列表
    public static List<Integer> json2Int(String str, String band) throws JSONException {
        List<Integer> list = new ArrayList<>();
        if (!TextUtils.isEmpty(str)) {
            JSONArray jsonArray = new JSONArray(str);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                if (object.getString(band).isEmpty()) continue;
                list.add(Integer.parseInt(object.getString(band)));
            }
        }
        return list;
    }

    public static String int2Json(List<Integer> list, String key) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject tmpObj = null;
        for (int i = 0; i < list.size(); i++) {
            tmpObj = new JSONObject();
            tmpObj.put(key, list.get(i));
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

    public static int dp2px(float dpValue) {
        return (int) (0.5f + dpValue * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getColor(int colorId) {
        return ContextCompat.getColor(ZApplication.getInstance().getContext(),
                colorId);
    }

}
