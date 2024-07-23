package com.simdo.g73cs;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nr.NrSdk;
import com.nr.Socket.OnSocketChangeListener;
import com.nr.Socket.ZTcpService;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.PermissionsUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements OnSocketChangeListener {

    private Context mContext;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;
    }

    private void initView() {

        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        });

    }



    /**
     * 绑定主服务
     */
    public void startBindService(Context context) {
        AppLog.I("#####startBindService");
        try {
            Intent intent = new Intent(context, MainService.class);
            startService(intent);
            // 再开SDK
            NrSdk.build().init(mContext);
            // 定位、扫频、SOCKET、升级取LOG等监听配置
            ZTcpService.build().addConnectListener(LoginActivity.this);
            //bindService(intent, connection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSocketStateChange(String id, int lastState, int newState) {
        AppLog.I("onSocketStateChange():  id = " + id + ", lastState = " + lastState + ", newState = " + newState);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext = null;
    }
}
