package com.simdo.g758s_predator;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.dwdbsdk.DwDbSdk;
import com.dwdbsdk.SCP.ScpUtil;
import com.simdo.g758s_predator.Util.AppLog;
import com.simdo.g758s_predator.Util.GnbCity;

public class InitializeService extends IntentService {

    private static final String ACTION_INIT_WHEN_APP_CREATE = "com.anly.githubapp.service.action.INIT";

    public InitializeService() {
        super("InitializeService");
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, InitializeService.class);
        intent.setAction(ACTION_INIT_WHEN_APP_CREATE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_INIT_WHEN_APP_CREATE.equals(action)) {
                performInit();
            }
        }
    }

    private void performInit() {
        AppLog.I("performInit begin:" + System.currentTimeMillis());

        initSDK();
        initObject();

        AppLog.I("performInit end:" + System.currentTimeMillis());
    }

    private void initSDK() {
        DwDbSdk.build().init(ZApplication.getInstance().getContext());
    }

    private void initObject() {
        GnbCity.build().init();
    }
}
