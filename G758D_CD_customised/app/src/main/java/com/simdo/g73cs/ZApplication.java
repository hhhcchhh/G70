package com.simdo.g73cs;

import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.Logcat.LogcatHandler;
import com.nr.Gnb.Bean.UeidBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Util.PrefUtil;

import java.util.ArrayList;
import java.util.List;

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
