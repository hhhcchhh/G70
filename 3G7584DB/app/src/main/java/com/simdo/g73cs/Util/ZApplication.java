package com.simdo.g73cs.Util;

import android.app.Application;
import android.content.Context;

public class ZApplication extends Application {
    private static ZApplication instance;
    private Context _context;
    public boolean isFirstStartApp = false;

    @Override
    public void onCreate() {
        super.onCreate();
        _context = this;
        instance = this;
        String string = PrefUtil.build().getValue("isFirstStartApp", "0").toString();
        if (string.equals("0")) isFirstStartApp = true;
        else {
            int value = Integer.parseInt(string);
            value++;
            PrefUtil.build().putValue("isFirstStartApp", String.valueOf(value));
        }
    }

    public static ZApplication getInstance() {
        return instance;
    }

    public Context getContext() {
        return _context;
    }
}
