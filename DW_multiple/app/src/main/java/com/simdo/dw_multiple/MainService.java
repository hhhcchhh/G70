package com.simdo.dw_multiple;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.nr.Socket.MessageControl.MessageController;
import com.nr.Socket.MessageControl.MessageTransceiver;
import com.nr.Socket.ZTcpService;
import com.simdo.dw_multiple.Util.AppLog;

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
        mZtcpService = ZTcpService.build();
        // 回传数据解析类
        mTransceiver = MessageTransceiver.build();
        // 数据通讯等控制类
        mZtcpService.registerHandler(mTransceiver);
        mTransceiver.setClient(mZtcpService);
        mController = MessageController.build();
        mController.setTransceiver(mTransceiver);
        mZtcpService.launch();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppLog.I("#### MainService$onDestroy");
        mZtcpService.onDestroy();
    }

    public class LocalBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }

    private IBinder mBinder = new LocalBinder();

    private ZTcpService mZtcpService;
    private MessageController mController;
    private MessageTransceiver mTransceiver;
}
