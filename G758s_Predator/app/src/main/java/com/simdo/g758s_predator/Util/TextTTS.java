package com.simdo.g758s_predator.Util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.simdo.g758s_predator.ZApplication;


public class TextTTS extends UtteranceProgressListener implements TextToSpeech.OnUtteranceCompletedListener {

    private TextToSpeech textToSpeech = null;
    private Context mContext;
    private static TextTTS instance;

    public static TextTTS build() {
        synchronized (TextTTS.class) {
            if (instance == null) {
                instance = new TextTTS();
            }
        }
        return instance;
    }

    public TextTTS() {
        super();
        mContext = ZApplication.getInstance().getContext();
    }

    @Override
    public void onStart(String utteranceId) {

    }

    @Override
    public void onDone(String utteranceId) {

    }

    @Override
    public void onError(String utteranceId) {

    }

    //播报完成回调
    @Override
    public void onUtteranceCompleted(String utteranceId) {

    }
    /**
     * 初始化语音报读
     */
    public void initTTS() {
        //实例化自带语音对象
        textToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                //APPLog.I("TTS: onInit(): " + status);
                if (status == 0) {
                    textToSpeech.setPitch(1.0f);//方法用来控制音调
                    textToSpeech.setSpeechRate(1.0f);//用来控制语速
                    //判断是否支持下面两种语言
                    //int result1 = textToSpeech.setLanguage(Locale.CHINESE);
                    // int result2 = textToSpeech.setLanguage(Locale.SIMPLIFIED_CHINESE);
                    // boolean a = (result1 == TextToSpeech.LANG_MISSING_DATA || result1 == TextToSpeech.LANG_NOT_SUPPORTED);
                    // boolean b = (result2 == TextToSpeech.LANG_MISSING_DATA || result2 == TextToSpeech.LANG_NOT_SUPPORTED);
                    // APPLog.I( "CHINESE支持否: " + a + "\n zh-CN支持否: " + b);
                } else {
                    //APPLog.I("TTS: 数据丢失或不支持");
                }
            }
        });
    }

    public void play(String content, boolean force) {
        if (textToSpeech!=null && !textToSpeech.isSpeaking()) {
            //APPLog.I("TTS: play(): " + content);
            //textToSpeech.setLanguage(Locale.CHINESE);
            //设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
            //textToSpeech.setPitch(1.0f);
            //设置语速
            //textToSpeech.setSpeechRate(1.0f);
            //输入中文，若不支持的设备则不会读出来
            textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null);
        } else if (force) {
            textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
