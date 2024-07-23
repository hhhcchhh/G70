package com.dwdbsdk.Socket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.MessageControl.MessageController;
import com.dwdbsdk.MessageControl.MessageTransceiver;

public class SocketService extends Service {

    private ZTcpService mZTcpService;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SdkLog.I("SocketService onCreate");
        initSocket();
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

        // 启动UDP监听广播
        UdpControl.build().startUdp();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SdkLog.I("SocketService onDestroy");
        // 结束UDP监听广播
        UdpControl.build().closeUdp();
        mZTcpService.onDestroy();
    }
}
