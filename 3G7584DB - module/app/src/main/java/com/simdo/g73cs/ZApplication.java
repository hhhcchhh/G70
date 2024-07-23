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

//import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.android.arouter.launcher.ARouter;
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


        // 这两行必须写在init之前，否则这些配置在init过程中将无效
        if (BuildConfig.DEBUG) {
            // 打印日志
            ARouter.openLog();
            // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
            ARouter.openDebug();
        }
        // 尽可能早，推荐在Application中初始化
        ARouter.init(this);
//        if (CommonBuild.IS_TEST_VERSION){



    }

    public static ZApplication getInstance() {
        return instance;
    }

    public Context getContext() {
        return _context;
    }
}
