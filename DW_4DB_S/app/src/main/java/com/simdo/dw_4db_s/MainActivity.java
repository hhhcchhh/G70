package com.simdo.dw_4db_s;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dwdbsdk.FTP.FTPUtil;
import com.dwdbsdk.Interface.FtpListener;
import com.dwdbsdk.Interface.SocketStateListener;
import com.dwdbsdk.DwDbSdk;
import com.dwdbsdk.MessageControl.MessageController;
import com.dwdbsdk.SCP.ScpUtil;
import com.dwdbsdk.Socket.ZTcpService;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.simdo.dw_4db_s.Bean.DeviceInfoBean;
import com.simdo.dw_4db_s.Bean.GnbBean;
import com.simdo.dw_4db_s.Ui.Adapter.FragmentAdapter;
import com.simdo.dw_4db_s.Bean.EventMessageBean;
import com.simdo.dw_4db_s.Ui.Fragment.DBFragment;
import com.simdo.dw_4db_s.Ui.Fragment.DWFragment;
import com.simdo.dw_4db_s.Ui.Fragment.SettingFragment;
import com.simdo.dw_4db_s.Util.AppLog;
import com.simdo.dw_4db_s.Util.CustomDialogUtil;
import com.simdo.dw_4db_s.Util.GnbCity;
import com.simdo.dw_4db_s.Util.PermissionsUtil;
import com.simdo.dw_4db_s.Util.StatusBarUtil;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SocketStateListener, FtpListener,
        ScpUtil.OnScpListener {

    private static MainActivity instance;
    private Context mContext;
    private Activity mActivity;
    private LayoutInflater mInflater;
    private DWFragment mDWFragment;
    private DBFragment mDBFragment;
    private SettingFragment mSettingFragment;
    private Dialog mDialog;

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Debug.startMethodTracing("App");
        setContentView(R.layout.activity_main);
        //保持屏幕唤醒
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        StatusBarUtil.setLightStatusBar(this, true);

        instance = this;
        mContext = this;
        mActivity = this;
        initView(); // 初始化视图以及监听事件
        initLiveEventBus();

        requestPermissions();
//        Debug.stopMethodTracing();
    }


    private void initView() {
        mInflater = LayoutInflater.from(this);
        TabLayout tl = findViewById(R.id.tl);
        ViewPager2 vp2 = findViewById(R.id.vp2);

        List<Fragment> fragmentList = new ArrayList<>();
        mDWFragment = new DWFragment(this);
        mDBFragment = new DBFragment(this);
        mSettingFragment = new SettingFragment(this);
        fragmentList.add(mDWFragment);
        fragmentList.add(mDBFragment);
        fragmentList.add(mSettingFragment);
//        vp2.setOffscreenPageLimit(3);
        vp2.setAdapter(new FragmentAdapter(getSupportFragmentManager(), getLifecycle(), fragmentList));

        String[] titles = new String[]{"定位", "测向", "设置"};
        int[] icons = new int[]{R.drawable.location_tab_icon, R.drawable.db_tab_icon, R.drawable.setting_tab_icon};
        int[] colors = new int[]{Color.parseColor("#07c062"), Color.parseColor("#8F9399")}; //选中颜色  正常颜色

        int[][] states = new int[2][]; //状态
        states[0] = new int[]{android.R.attr.state_selected}; //选中
        states[1] = new int[]{}; //默认
        ColorStateList colorList = new ColorStateList(states, colors);

        //TabLayout 和 Viewpager2 关联
        TabLayoutMediator tab = new TabLayoutMediator(tl, vp2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_tab, null);

                TextView tv_tab_name = view.findViewById(R.id.tv_tab_name);
                tv_tab_name.setText(titles[position]);
                tv_tab_name.setTextColor(colorList);

                ImageView iv_tab_icon = view.findViewById(R.id.iv_tab_icon);
                iv_tab_icon.setImageResource(icons[position]);
                tab.setCustomView(view);
            }
        });
        tab.attach();
    }

    private void initLiveEventBus() {
        LiveEventBus.get("LiveEventBus", EventMessageBean.class)
                .observe(this, emb -> {
                    AppLog.D("LiveEventBus emb.getMsg() = " + emb.getMsg());
                    switch (emb.getMsg()) {
                        case "dw_version" -> mSettingFragment.setVersion_dw(emb.getString());
                        case "db_version" -> mSettingFragment.setVersion_db(emb.getString());
                        case "DB_device_id" -> mSettingFragment.setDB_device_id(emb.getString());
                        case "DW_device" -> mSettingFragment.setDwDevice(emb.getDwDeviceInfoBean());
                        case "onGetLog" -> mDWFragment.onGetLog(emb.getString());
                        case "onGetOpLog" -> mDWFragment.onGetOpLog(emb.getString());
                        case "refreshWorkState" ->
                                mSettingFragment.refreshDeviceWorkState(emb.getWhat());
                        case "refreshDWWorkState" ->
                                mDWFragment.refreshDeviceWorkState(emb.getWhat());
                        case "dismissProgressDialog" ->
                                mSettingFragment.dismissProgressDialog(emb.getWhat());

                    }
                });
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                List<String> list = new ArrayList<>(Arrays.asList(permissions));
                list.add(Manifest.permission.READ_MEDIA_AUDIO);
                list.add(Manifest.permission.READ_MEDIA_IMAGES);
                list.add(Manifest.permission.READ_MEDIA_VIDEO);
                list.add(Manifest.permission.NEARBY_WIFI_DEVICES);
                list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                permissions = list.toArray(new String[0]);
            } else {
                List<String> list = new ArrayList<>(Arrays.asList(permissions));
                list.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                list.add(Manifest.permission.ACCESS_FINE_LOCATION);
                permissions = list.toArray(new String[0]);
            }
            PermissionsUtil.getInstance().checkPermissions(this, permissions, permissionsResult);
        } else {
            startActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsUtil.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    PermissionsUtil.IPermissionsResult permissionsResult = new PermissionsUtil.IPermissionsResult() {
        @Override
        public void permitPermissions() {
            //处理通过请求后的操作
            startActivity();
        }

        @Override
        public void refusePermissions() {
            //处理被拒绝申请的权限的操作
            showToast("拒绝权限，无法提供服务!");
            finish();
        }
    };

    private void startActivity() {
        AppLog.createLogFile("+++ ON CREATE +++");
//        initSDK();    // 初始化SDK
//        initObject(); // 初始化对象以及数据
        DwDbSdk.build().addConnectListener(this);
        DwDbSdk.build().setFtpListener(this);
        ScpUtil.build().setOnScpListener(this);
        InitializeService.start(this);
    }

//    private void initSDK() {
//        DwDbSdk.build().init(mContext);
//        DwDbSdk.build().addConnectListener(this);
//        DwDbSdk.build().setFtpListener(this);
//        ScpUtil.build().setOnScpListener(this);
//    }
//
//    private void initObject() {
//        GnbCity.build().init();
//    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitAppDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出APP弹框
     */
    private void exitAppDialog() {
        CustomDialogUtil.getInstance(this).createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_confirm, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        tv_title.setText(getStr(R.string.warning));
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        tv_msg.setText(getStr(R.string.exit_app_confirm));
        final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseSDK();
                CustomDialogUtil.getInstance(MainActivity.this).closeCustomDialog();
                finish();
                System.exit(0);
            }
        });

        final Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialogUtil.getInstance(MainActivity.this).closeCustomDialog();
            }
        });
        CustomDialogUtil.getInstance(this).showCustomDialog(view, false);
    }

    public String getStr(int strId) {
        return getResources().getString(strId);
    }

    public void showToast(String msg) {
        Context context = getApplicationContext();
        Toast toast = new Toast(context);
        //创建Toast中的文字
        TextView textView = new TextView(context);
        textView.setText(msg);
        textView.setBackgroundResource(R.drawable.radio_main);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14);
        textView.setPadding(24, 14, 24, 14);
        toast.setView(textView); //把layout设置进入Toast
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseSDK();
        releaseObject();
        this.stopService(new Intent(this, InitializeService.class));
        System.exit(0);
    }

    private void releaseSDK() {
        DwDbSdk.build().removeConnectListener(this);
        DwDbSdk.build().removeFtpListener();
        DwDbSdk.build().release();
        MessageController.build().removeDBBusinessListener();
        MessageController.build().removeDWBusinessListener();
        FTPUtil.build().removeFtpListener();
        ZTcpService.build().removeConnectListener(this);
        ScpUtil.build().removeOnScpListener();
    }


    private void releaseObject() {
        instance = null;
        mContext = null;
        mActivity = null;
        mInflater = null;
        mDWFragment.onDestroy();
        mDBFragment.onDestroy();
        mSettingFragment.onDestroy();
        mDWFragment = null;
        mDBFragment = null;
        mSettingFragment = null;
//        GnbCity.build().
    }

    @Override
    public void OnSocketStateChange(String id, int lastState, int state) {
        mDWFragment.OnSocketStateChange(id, lastState, state);
        mDBFragment.OnSocketStateChange(id, lastState, state);
    }

    @Override
    public void onFtpConnectState(String s, boolean b) {

    }

    @Override
    public void onFtpGetFileRsp(String id, boolean state) {
        AppLog.I("onFtpGetFileRsp(): id = " + id + "state" + state);
        //单兵读取基带log当前用的是scp
        String DBDeviceName = mDBFragment.getDeviceName();
        if (state && id.equals(mDBFragment.getDeviceIdByName(DBDeviceName))) {
            showRemindDialog(getStr(R.string.bs_log), getStr(R.string.get_log_success));
            mDBFragment.getDeviceByName(DBDeviceName).setWorkState(GnbBean.DB_State.READY);
            mDBFragment.refreshStateView(DBDeviceName, getStr(R.string.state_prepared));
        } else if (state && id.equals(mDWFragment.getDeviceId())) {
            if (mDWFragment.getDwDeviceList().get(0).getWorkState() == GnbBean.DW_State.GETOPLOG) {
                showRemindDialog(getStr(R.string.bs_log), getStr(R.string.get_op_log_success));
            } else {
                showRemindDialog(getStr(R.string.bs_log), getStr(R.string.get_log_success));
            }
            mDWFragment.getDwDeviceList().get(0).setWorkState(GnbBean.DW_State.IDLE);
            mDWFragment.refreshStateView(getStr(R.string.state_prepared));
        } else {
            showRemindDialog(getStr(R.string.bs_log), getStr(R.string.get_log_fail));
        }

    }

    @Override
    public void onFtpPutFileRsp(String id, boolean state) {
        AppLog.I("onFtpPutFileRsp(): " + state);
        if (state) {
            if (id.equals(mDWFragment.getDeviceId())) {
                mDWFragment.getDwDeviceList().get(0).setWorkState(GnbBean.DW_State.UPDATE);
                mDWFragment.refreshStateView(getStr(R.string.state_upgrade_ing));
                MessageController.build().setDWGnbUpgrade(id, 3, mSettingFragment.getUpgradeFileName(), mSettingFragment.getUpgradeFilePath());
            }
        } else {
            mDWFragment.getDwDeviceList().get(0).setWorkState(GnbBean.DW_State.IDLE);
            mDWFragment.refreshStateView(getStr(R.string.state_prepared));
            showRemindDialog(getStr(R.string.bs_upgrade), getStr(R.string.state_upgrade_copying_file_fail));

            EventMessageBean emb = new EventMessageBean("dismissProgressDialog");
            emb.setWhat(-1);
            LiveEventBus.get("LiveEventBus").post(emb);
        }
    }

    @Override
    public void onFtpGetFileProcess(String s, long l) {
        AppLog.I(s + ":拷贝LOG文件当前进度 : " + l);
    }

    @Override
    public void OnScpConnectFail() {

    }

    @Override
    public void OnScpGetLogRsp(boolean state) {
        AppLog.I("OnScpGetLogRsp(): " + state);
        String DBDeviceName = mDBFragment.getDeviceName();
        if (state) {
            showRemindDialog(getStr(R.string.bs_log), getStr(R.string.get_log_success));
            mDBFragment.getDeviceByName(DBDeviceName).setWorkState(GnbBean.DB_State.READY);
            mDBFragment.refreshStateView(DBDeviceName, getStr(R.string.state_prepared));
        } else {
            showRemindDialog(getStr(R.string.bs_log), getStr(R.string.get_log_fail));
        }
    }

    @Override
    public void OnScpUpgradeFileRsp(boolean state) {
        AppLog.I("OnScpUpgradeFileRsp" + state);
        String DBDeviceName = mDBFragment.getDeviceName();
        if (state) {
            mDBFragment.getDeviceByName(DBDeviceName).setWorkState(GnbBean.DB_State.UPGRADE);
            mDBFragment.refreshStateView(DBDeviceName, getStr(R.string.state_upgrade_ing));
            MessageController.build().setDBUpgrade(mDBFragment.getDeviceIdByName(DBDeviceName), 3, mDBFragment.getUpgradeFileName(), mDBFragment.getUpgradeFilePath());
        } else {
            mDWFragment.getDwDeviceList().get(0).setWorkState(GnbBean.DW_State.IDLE);
            mDWFragment.refreshStateView(getStr(R.string.state_prepared));
            showRemindDialog(getStr(R.string.bs_upgrade), getStr(R.string.state_upgrade_copying_file_fail));

            EventMessageBean emb = new EventMessageBean("dismissProgressDialog");
            emb.setWhat(-1);
            LiveEventBus.get("LiveEventBus").post(emb);
        }
    }

    private void showRemindDialog(String title, String msg) {
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_reminder, null);
        TextView tv_title = (TextView) view.findViewById(R.id.title_reminder);
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_reminder_msg);

        tv_title.setText(title);
        tv_msg.setText(msg);

        Button btn_know = (Button) view.findViewById(R.id.btn_reminder_know);
        btn_know.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });

        showCustomDialog(view, false);
    }

    private void showCustomDialog(View view, boolean bottom) {
        mDialog.setContentView(view);
        mDialog.show();
        if (bottom) {
            Window window = mDialog.getWindow();
            window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
            window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
        } else {
            Window window = mDialog.getWindow();
            window.setGravity(Gravity.CENTER); //可设置dialog的位置
            WindowManager.LayoutParams lp = window.getAttributes();

            lp.width = getResources().getDisplayMetrics().widthPixels * 6 / 7;//WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }

    private void closeCustomDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private void createCustomDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        mDialog = new Dialog(this, R.style.style_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mDialog.setCancelable(false);   // 返回键不消失
    }


}