package com.simdo.g73cs;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.dwdbsdk.MessageControl.MessageController;
import com.dwdbsdk.MessageControl.MessageTransceiver;
import com.dwdbsdk.Socket.ZTcpService;
import com.simdo.g73cs.Util.AppLog;

public class MainService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        AppLog.D("#### MainService$onCreate");
        initSocket();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * 绑定业务操作类
     */
    private void initSocket() {
        // Socket连接
        mZTcpService = ZTcpService.build();
        // 回传数据解析类
        MessageTransceiver mTransceiver = MessageTransceiver.build();
        mZTcpService.registerHandler(mTransceiver);
        mTransceiver.setClient(mZTcpService);
        // 数据通讯等控制类
        MessageController mController = MessageController.build();
        mController.setTransceiver(mTransceiver);
        mZTcpService.launch();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppLog.I("#### MainService$onDestroy");
        mZTcpService.onDestroy();
    }

    public class LocalBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }

    private IBinder mBinder = new LocalBinder();

    private ZTcpService mZTcpService;
}
