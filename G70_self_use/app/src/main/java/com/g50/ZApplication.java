package com.g50;

import com.Logcat.LogcatHandler;
import com.Util.CrashManagerUtil;

import android.app.Application;
import android.content.Context;

public class ZApplication extends Application {
    public static ZApplication instance;
    static Context _context;

    public static synchronized ZApplication context() {
        return (ZApplication) _context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _context = getApplicationContext();
        instance = this;

       LogcatHandler logcatHandler = LogcatHandler.getInstance();
       logcatHandler.init(_context);
       CrashManagerUtil.getInstance(_context).init(); //初始化全局异常捕获管理
    }

    public static ZApplication getInstance() {
        return instance;
    }

    public static Context getContext() {
        return _context;
    }
}
