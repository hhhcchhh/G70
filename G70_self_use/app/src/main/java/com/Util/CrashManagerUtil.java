package com.Util;

import android.content.Context;
import android.os.Looper;

import com.Logcat.APPLog;

public class CrashManagerUtil {
    private static CrashManagerUtil mInstance;
    private static Context mContext;

    private CrashManagerUtil() {

    }

    public static CrashManagerUtil getInstance(Context context) {
        if (mInstance == null) {
            mContext = context;
            mInstance = new CrashManagerUtil();
        }
        return mInstance;
    }

    public void init() {
        //crach 防护
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                APPLog.E("uncaughtException-->" + e.toString());
                Util.showToast(mContext.getApplicationContext(),"捕获到意外异常，请联系管理员查看!");
                handleFileException(e);
                if (t == Looper.getMainLooper().getThread()) {
                    handleMainThread(e);
                }
            }
        });
    }

    //这里对异常信息作处理，可本地保存，可上传至第三方平台
    private void handleFileException(Throwable e) {

    }

    private void handleMainThread(Throwable e) {
        while (true) {
            try {
                Looper.loop();
            } catch (Throwable e1) {
                handleFileException(e1);
            }
        }
    }
}
