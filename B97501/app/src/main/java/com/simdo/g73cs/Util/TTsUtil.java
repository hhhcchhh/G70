package com.simdo.g73cs.Util;


import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;

import com.simdo.g73cs.ZApplication;

import java.lang.reflect.Field;
import java.util.Locale;

public class TTsUtil {

    private  static TTsUtil tTsUtil;
    public static TTsUtil getInstance() {
        if (tTsUtil == null) {
            synchronized (TTsUtil.class) {
                if (tTsUtil == null)
                    tTsUtil = new TTsUtil();
            }
        }
        return tTsUtil;
    }

    private TextToSpeech textToSpeech;
    private boolean speechOver = true;
    private TTsUtil() {
//        "com.iflytek.speechcloud" 科大讯飞
//        "com.baidu.duersdk.opensdk" 百度语音
//        "com.google.android.tts" 谷歌语音

        textToSpeech = new TextToSpeech(ZApplication.getInstance().getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.CHINA);
                if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
                        && result != TextToSpeech.LANG_AVAILABLE) {
                    AppLog.D("TTS暂时不支持这种语音的朗读！");
                }

            }else {
                AppLog.D("TTS初始化失败！");
            }
        },"com.google.android.tts");//科大讯飞语音引擎
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                speechOver = false;
            }

            @Override
            public void onDone(String utteranceId) {
                speechOver = true;
            }

            @Override
            public void onError(String utteranceId) {
                speechOver = true;
            }
        });
    }


    public void speak(int content) {
        if (!speechOver)return;
        textToSpeech.speak(int2chineseNum(content), TextToSpeech.QUEUE_ADD, null, "jiance");
    }

    public void speak(String content) {
        //AppLog.D("TTS的speechOver==false" + speechOver);
        if (!speechOver)return;
        if (ismServiceConnectionUsable(textToSpeech)) {//没有被回收
            textToSpeech.speak(content, TextToSpeech.QUEUE_ADD, null, "jiance");
        }else {//被回收了不可用状态
            textToSpeech = new TextToSpeech(ZApplication.getInstance().getContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.CHINA);
                    if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
                            && result != TextToSpeech.LANG_AVAILABLE) {
                        AppLog.D("TTS暂时不支持这种语音的朗读！");
                    }
                    textToSpeech.speak(content, TextToSpeech.QUEUE_ADD, null, "jiance");
                }else {
                    AppLog.D("TTS初始化失败！");
                }
            },"com.google.android.tts");//科大讯飞语音引擎
        }

    }

    public String int2chineseNum(int src) {
        final String num[] = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        final String unit[] = {"", "十", "百", "千", "万", "十", "百", "千", "亿", "十", "百", "千"};
        String dst = "";
        int count = 0;
        while(src > 0) {
            dst = (num[src % 10] + unit[count]) + dst;
            src = src / 10;
            count++;
        }
        return dst.replaceAll("零[千百十]", "零").replaceAll("零+万", "万")
                .replaceAll("零+亿", "亿").replaceAll("亿万", "亿零")
                .replaceAll("零+", "零").replaceAll("零$", "");
    }

    /**
     * TTS初始化之后有时会无法播放语音。
     * 从打印日志看failed: not bound to TTS engine
     * 找到源代码打印处
     * if (mServiceConnection == null) {
     *      Log.w(TAG, method + " failed: not bound to TTS engine");
     *   return errorResult;
     *  }
     *  通过反射判断mServiceConnection是否为空
     * @param tts
     * @return true 可用
     */
    public static boolean ismServiceConnectionUsable(TextToSpeech tts) {

        boolean isBindConnection = true;
        if (tts == null){
            return false;
        }
        Field[] fields = tts.getClass().getDeclaredFields();
        for (int j = 0; j < fields.length; j++) {
            fields[j].setAccessible(true);

            if (TextUtils.equals("mConnectingServiceConnection",fields[j].getName()) && TextUtils.equals("android.speech.tts.TextToSpeech$Connection",fields[j].getType().getName())) {
                try {
                    if(fields[j].get(tts) == null){
                        isBindConnection = false;
                        AppLog.D("*******反射判断 TTS -> mServiceConnection == null*******");
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return isBindConnection;
    }

    /**
     *演示回收TextToSpeech
     */
    public  void getBackobjeckt() {
        if (textToSpeech == null){
            return;
        }else {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

}
