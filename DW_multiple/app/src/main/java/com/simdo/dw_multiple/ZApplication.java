package com.simdo.dw_multiple;

import android.app.Application;
import android.content.Context;

public class ZApplication extends Application {
    private static ZApplication instance;
    private Context _context;


    @Override
    public void onCreate() {
        super.onCreate();
        _context = this;
        instance = this;
    }

    public static ZApplication getInstance() {
        return instance;
    }

    public Context getContext() {
        return _context;
    }

}
