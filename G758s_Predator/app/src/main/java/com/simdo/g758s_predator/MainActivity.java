package com.simdo.g758s_predator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.JsonReader;
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
import com.dwdbsdk.Interface.DBBusinessListener;
import com.dwdbsdk.Interface.DWBusinessListener;
import com.dwdbsdk.Interface.FtpListener;
import com.dwdbsdk.Interface.SocketStateListener;
import com.dwdbsdk.DwDbSdk;
import com.dwdbsdk.MessageControl.MessageController;
import com.dwdbsdk.Response.DB.MsgCmdRsp;
import com.dwdbsdk.Response.DB.MsgGetJamRsp;
import com.dwdbsdk.Response.DB.MsgReadDataFwdRsp;
import com.dwdbsdk.Response.DB.MsgScanRsp;
import com.dwdbsdk.Response.DB.MsgSenseReportRsp;
import com.dwdbsdk.Response.DB.MsgStateRsp;
import com.dwdbsdk.Response.DB.MsgVersionRsp;
import com.dwdbsdk.Response.DW.GnbCatchCfgRsp;
import com.dwdbsdk.Response.DW.GnbCmdRsp;
import com.dwdbsdk.Response.DW.GnbFreqScanGetDocumentRsp;
import com.dwdbsdk.Response.DW.GnbFreqScanRsp;
import com.dwdbsdk.Response.DW.GnbFtpRsp;
import com.dwdbsdk.Response.DW.GnbGetSysInfoRsp;
import com.dwdbsdk.Response.DW.GnbGpioRsp;
import com.dwdbsdk.Response.DW.GnbGpsInOutRsp;
import com.dwdbsdk.Response.DW.GnbGpsRsp;
import com.dwdbsdk.Response.DW.GnbMethIpRsp;
import com.dwdbsdk.Response.DW.GnbReadDataFwdRsp;
import com.dwdbsdk.Response.DW.GnbSetDataTo485Rsp;
import com.dwdbsdk.Response.DW.GnbStateRsp;
import com.dwdbsdk.Response.DW.GnbTraceRsp;
import com.dwdbsdk.Response.DW.GnbUserDataRsp;
import com.dwdbsdk.Response.DW.GnbVersionRsp;
import com.dwdbsdk.SCP.ScpUtil;
import com.dwdbsdk.Socket.ZTcpService;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.simdo.g758s_predator.Bean.DataFwdBean;
import com.simdo.g758s_predator.Bean.DwDeviceInfoBean;
import com.simdo.g758s_predator.Bean.GnbBean;
import com.simdo.g758s_predator.Ui.Adapter.FragmentAdapter;
import com.simdo.g758s_predator.Bean.EventMessageBean;
import com.simdo.g758s_predator.Ui.Fragment.DBFragment;
import com.simdo.g758s_predator.Ui.Fragment.DWFragment;
import com.simdo.g758s_predator.Ui.Fragment.SettingFragment;
import com.simdo.g758s_predator.Ui.GnbCityDialog;
import com.simdo.g758s_predator.Util.AppLog;
import com.simdo.g758s_predator.Util.CustomDialogUtil;
import com.simdo.g758s_predator.Util.DataUtil;
import com.simdo.g758s_predator.Util.PermissionsUtil;
import com.simdo.g758s_predator.Util.PrefUtil;
import com.simdo.g758s_predator.Util.StatusBarUtil;
import com.simdo.g758s_predator.Util.TraceUtil;


import org.json.JSONObject;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SocketStateListener, FtpListener,
        ScpUtil.OnScpListener , DWBusinessListener, DBBusinessListener {

    private static MainActivity instance;
    private Context mContext;
    private Activity mActivity;
    private LayoutInflater mInflater;
    private DWFragment mDWFragment1,mDWFragment2,mDWFragment3;
    private DBFragment mDBFragment;
//    private SettingFragment mSettingFragment;
    private Dialog mDialog;
    private List<DwDeviceInfoBean> dwDeviceList = new ArrayList<>();
    List<DWFragment> dwFragmentList = new ArrayList<>();
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
//        initLiveEventBus();
        requestPermissions();
//        Debug.stopMethodTracing();
    }


    private void initView() {
        mInflater = LayoutInflater.from(this);
        TabLayout tl = findViewById(R.id.tl);
        ViewPager2 vp2 = findViewById(R.id.vp2);

        List<Fragment> fragmentList = new ArrayList<>();
        mDWFragment1 = new DWFragment(this);
        mDWFragment2 = new DWFragment(this);
        mDWFragment3 = new DWFragment(this);
        mDBFragment = new DBFragment(this);
//        mSettingFragment = new SettingFragment(this);
        fragmentList.add(mDWFragment1);
        fragmentList.add(mDWFragment2);
        fragmentList.add(mDWFragment3);
        fragmentList.add(mDBFragment);
//        fragmentList.add(mSettingFragment);
        dwFragmentList.add(mDWFragment1);
        dwFragmentList.add(mDWFragment2);
        dwFragmentList.add(mDWFragment3);
        vp2.setOffscreenPageLimit(4);
        vp2.setAdapter(new FragmentAdapter(getSupportFragmentManager(), getLifecycle(), fragmentList));

//        String[] titles = new String[]{"定位1","定位2","定位3", "测向", "设置"};
        String[] titles = new String[]{"定位1","定位2","定位3", "测向"};
        int[] icons = new int[]{R.drawable.location_tab_icon, R.drawable.location_tab_icon,R.drawable.location_tab_icon,R.drawable.db_tab_icon, R.drawable.setting_tab_icon};
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

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            List<String> list = new ArrayList<>(Arrays.asList(permissions));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                list.add(Manifest.permission.READ_MEDIA_AUDIO);
                list.add(Manifest.permission.READ_MEDIA_IMAGES);
                list.add(Manifest.permission.READ_MEDIA_VIDEO);
                list.add(Manifest.permission.NEARBY_WIFI_DEVICES);
                //list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                list.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                list.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                list.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
            permissions = list.toArray(new String[0]);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionsUtil.getInstance().mRequestCode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager())
                    PermissionsUtil.getInstance().showSystemPermissionsSettingDialog(mActivity);
            }
            requestPermissions();
        }
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
        DwDbSdk.build().setDWBusinessListener(this);
        DwDbSdk.build().setDBBusinessListener(true,this);
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
        mDWFragment1.onDestroy();
        mDBFragment.onDestroy();
//        mSettingFragment.onDestroy();
        mDWFragment1 = null;
        mDBFragment = null;
//        mSettingFragment = null;
//        GnbCity.build().
    }

    @Override
    public void OnSocketStateChange(String id, int lastState, int state) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).OnSocketStateChange(id,lastState,state);
                break;
            }
            if (id.equals(mDBFragment.getDeviceId())){
                mDBFragment.OnSocketStateChange(id,lastState,state);
                break;
            }
        }
    }

    @Override
    public void onFtpConnectState(String id, boolean state) {
        AppLog.I("onFtpConnectState(): id = " + id + ", state = " + state);
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onFtpConnectState(id, state);
                break;
            }
        }
    }

    @Override
    public void onFtpGetFileRsp(String id, boolean state) {
        AppLog.I("onFtpGetFileRsp(): id = " + id + "state" + state);
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onFtpGetFileRsp(id,state);
                break;
            }
        }
    }

    @Override
    public void onFtpPutFileRsp(String id, boolean state) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).OnFtpPutFileRsp(id,state);
                break;
            }
        }
    }

    @Override
    public void onFtpGetFileProcess(String s, long l) {
        AppLog.I(s + ":拷贝LOG文件当前进度 : " + l);
    }

    @Override
    public void OnScpConnectFail() {
        mDBFragment.setWorkState(GnbBean.DW_State.IDLE);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDBFragment.refreshStateView(getStr(R.string.state_prepared));
                showRemindDialog(getStr(R.string.tips), getStr(R.string.state_scp_connect_fail));
            }
        });

    }

    @Override
    public void OnScpGetLogRsp(boolean state) {
        AppLog.I("OnScpGetLogRsp(): " + state);
        if (state) {
            showRemindDialog(getStr(R.string.bs_log), getStr(R.string.get_log_success));
            mDBFragment.setWorkState(GnbBean.DB_State.READY);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDBFragment.refreshStateView(getStr(R.string.state_prepared));
                }
            });
        } else {
            showRemindDialog(getStr(R.string.bs_log), getStr(R.string.get_log_fail));
        }
    }

    @Override
    public void OnScpUpgradeFileRsp(boolean state) {
        AppLog.I("OnScpUpgradeFileRsp" + state);
        if (state) {
            mDBFragment.setWorkState(GnbBean.DB_State.UPGRADE);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDBFragment.refreshStateView(getStr(R.string.state_upgrade_ing));
                }
            });
            MessageController.build().setDBUpgrade(mDBFragment.getDeviceId(), 0, mDBFragment.getUpgradeFileName(), mDBFragment.getUpgradeFilePath());
        } else {
            mDBFragment.setWorkState(GnbBean.DW_State.IDLE);
            mDBFragment.refreshStateView(getStr(R.string.state_prepared));
            showRemindDialog(getStr(R.string.bs_upgrade), getStr(R.string.state_upgrade_copying_file_fail));
        }
    }

    public void showRemindDialog(String title, String msg) {
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


    @Override
    public void onDWHeartStateRsp(String id, GnbStateRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWHeartStateRsp()：" + rsp.toString());
            if (!rsp.getDevName().contains("A")&&!rsp.getDevName().contains("B")&&!rsp.getDevName().contains("C")){
                return;
            }
            if (dwDeviceList.size() == 0) {
                DwDeviceInfoBean device = new DwDeviceInfoBean();
                device.setId(rsp.getDeviceId());
                device.setRsp(rsp);
                device.setWorkState(GnbBean.DW_State.IDLE);
                device.setLicense("");
                device.setHwVer("");
                device.setFpgaVer("");
                device.setTraceUtil(new TraceUtil());
                dwDeviceList.add(device);
                MessageController.build().setDWTime(rsp.getDeviceId());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MessageController.build().getDWVersion(rsp.getDeviceId());
                    }
                }, 500);
            } else {
                boolean isAdd = true;
                for (int i = 0; i < dwDeviceList.size(); i++) {
                    if (dwDeviceList.get(i).getId().equals(rsp.getDeviceId())) {
                        isAdd = false;
                        break;
                    }
                }
                if (isAdd&&dwDeviceList.size()<3) {
                    DwDeviceInfoBean device = new DwDeviceInfoBean();
                    device.setId(rsp.getDeviceId());
                    device.setRsp(rsp);
                    device.setName(rsp.getDevName());
                    device.setWorkState(GnbBean.DW_State.IDLE);
                    device.setLicense("");
                    device.setHwVer("");
                    device.setFpgaVer("");
                    device.setTraceUtil(new TraceUtil());
                    dwDeviceList.add(device);
                    MessageController.build().setDWTime(rsp.getDeviceId());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MessageController.build().getDWVersion(rsp.getDeviceId());
                        }
                    }, 500);

                }
            }
            for (int i = 0; i < dwDeviceList.size(); i++) {
                if (rsp.getDeviceId().equals(dwDeviceList.get(i).getId())){
                    dwFragmentList.get(i).setDwDevice(dwDeviceList.get(i));
                    dwFragmentList.get(i).onDWHeartStateRsp(rsp.getDeviceId(),rsp);
                }
            }
        }

    }

    @Override
    public void onDWSetTimeRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onDWQueryVersionRsp(String id, GnbVersionRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWQueryVersionRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetBlackListRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetBlackListRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetGnbRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetGnbRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetPaGpioRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetPaGpioRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWGetPaGpioRsp(String id, GnbGpioRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWGetPaGpioRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetTxPwrOffsetRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetNvTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetNvTxPwrOffsetRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWStartTraceRsp(String id, GnbTraceRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWStartTraceRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWStopTraceRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWStopTraceRsp(id,rsp);
            }
        }
    }


    @Override
    public void onDWStartCatchRsp(String id, GnbTraceRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWStartCatchRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWStopCatchRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWStopCatchRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWGetCatchCfg(String id, GnbCatchCfgRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWGetCatchCfg(id,rsp);
            }
        }
    }

    @Override
    public void onDWStartControlRsp(String id, GnbTraceRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWStartControlRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWStopControlRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWStopControlRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWFreqScanRsp(String id, GnbFreqScanRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWFreqScanRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWFreqScanGetDocumentRsp(String id, GnbFreqScanGetDocumentRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWFreqScanGetDocumentRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWStopFreqScanRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWStopFreqScanRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWStartBandScan(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWStartBandScan(id,rsp);
            }
        }
    }

    @Override
    public void onDWGetLogRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWGetLogRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWGetOpLogRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWGetOpLogRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWGetSysLogRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWGetSysLogRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWWriteOpLogRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWWriteOpLogRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWDeleteOpLogRsp(String s, GnbCmdRsp gnbCmdRsp) {
    }

    @Override
    public void onDWSetDualCellRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetDualCellRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWStartTdMeasure(String id, GnbCmdRsp rsp) {
        if (mGnbCityDialog != null) mGnbCityDialog.onStartTdMeasure(id, rsp);
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWStartTdMeasure(id,rsp);
                break;
            }
        }
    }

    @Override
    public void onDWSetDevNameRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetDevNameRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWGetSysInfoRsp(String id, GnbGetSysInfoRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWGetSysInfoRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetWifiInfoRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetWifiInfoRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetBtNameRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetBtNameRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetMethIpRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetMethIpRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWGetMethIpRsp(String id, GnbMethIpRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWGetMethIpRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetFtpRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetFtpRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWGetFtpRsp(String id, GnbFtpRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWGetFtpRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetGpsRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetGpsRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWGetGpsRsp(String id, GnbGpsRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWGetGpsRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetGpsInOut(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetGpsInOut(id,rsp);
            }
        }
    }

    @Override
    public void onDWGetGpsInOut(String id, GnbGpsInOutRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWGetGpsInOut(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetFanSpeedRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetFanSpeedRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetFanAutoSpeedRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetFanAutoSpeedRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetRxGainRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetRxGainRsp(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetJamArfcn(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetJamArfcn(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetForwardUdpMsg(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetForwardUdpMsg(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetGpioTxRx(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetGpioTxRx(id,rsp);
            }
        }
    }

    @Override
    public void onDWGetUserData(String id, GnbUserDataRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWGetUserData(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetUserData(String id, GnbUserDataRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetUserData(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetDataTo485(String id, GnbSetDataTo485Rsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetDataTo485(id,rsp);
            }
        }
    }

    @Override
    public void onDWSetDataFwd(String id, GnbReadDataFwdRsp rsp) {
        if (rsp!=null){
            for (int i = 0; i < dwDeviceList.size(); i++) {
                if (id.equals(dwDeviceList.get(i).getId())) {
                    dwFragmentList.get(i).onDWSetDataFwd(id, rsp);
                }
            }
        }
    }

    @Override
    public void onDWRebootRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWRebootRsp(id,rsp);
                break;
            }
        }
    }

    @Override
    public void onDWUpgradeRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWUpgradeRsp(id,rsp);
                break;
            }
        }
    }

    @Override
    public void onDWGetCellCfg(String s, JSONObject jsonObject) {

    }

    @Override
    public void onDWRedirectUeCfg(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onDWResetPlmnCfg(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onDWSetPerPwrCfg(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDWSetPerPwrCfg(id,rsp);
                break;
            }
        }
    }

    @Override
    public void onDBHeartStateRsp(String id, MsgStateRsp rsp) {
        mDBFragment.onDBHeartStateRsp(id,rsp);
    }

    @Override
    public void onDBSetTimeRsp(String id, MsgStateRsp rsp) {
        mDBFragment.onDBSetTimeRsp(id,rsp);
    }

    @Override
    public void onDBGetVersionRsp(String id, MsgVersionRsp rsp) {
        mDBFragment.onDBGetVersionRsp(id,rsp);
    }

    @Override
    public void onDBGetLogRsp(String id, MsgStateRsp rsp) {
        mDBFragment.onDBGetLogRsp(id,rsp);
    }

    @Override
    public void onDBSetBtNameRsp(String id, MsgStateRsp rsp) {
        mDBFragment.onDBSetBtNameRsp(id,rsp);
    }

    @Override
    public void onDBSetDevNameRsp(String id, MsgCmdRsp rsp) {
        mDBFragment.onDBSetDevNameRsp(id,rsp);
    }

    @Override
    public void onDBWifiCfgRsp(String id, MsgStateRsp rsp) {
        mDBFragment.onDBWifiCfgRsp(id,rsp);
    }

    @Override
    public void onDBRxGainCfgRsp(String id, MsgStateRsp rsp) {
        mDBFragment.onDBRxGainCfgRsp(id,rsp);
    }

    @Override
    public void onDBStartSGRsp(String id, MsgStateRsp rsp) {
        mDBFragment.onDBStartSGRsp(id,rsp);
    }

    @Override
    public void onDBStopSGRsp(String id, MsgStateRsp rsp) {
        mDBFragment.onDBStopSGRsp(id,rsp);
    }

    @Override
    public void onDBStartJamRsp(String id, MsgStateRsp rsp) {
        mDBFragment.onDBStartJamRsp(id,rsp);
    }

    @Override
    public void onDBStopJamRsp(String id, MsgStateRsp rsp) {
        mDBFragment.onDBStopJamRsp(id,rsp);
    }

    @Override
    public void onDBGetJamRsp(String id, MsgGetJamRsp rsp) {
        mDBFragment.onDBGetJamRsp(id,rsp);
    }

    @Override
    public void onDBStartScanRsp(String id, MsgScanRsp rsp) {
        mDBFragment.onDBStartScanRsp(id,rsp);
    }

    @Override
    public void onDBStartPwrDetectRsp(String id, MsgScanRsp rsp) {
        mDBFragment.onDBStartPwrDetectRsp(id,rsp);
    }

    @Override
    public void onDBStopPwrDetectRsp(String id, MsgStateRsp rsp) {
        mDBFragment.onDBStopPwrDetectRsp(id,rsp);
    }

    @Override
    public void onDBSetGpioCfgRsp(String id, MsgStateRsp rsp) {
        mDBFragment.onDBSetGpioCfgRsp(id,rsp);
    }

    @Override
    public void onReadDataFwdRsp(String id, MsgReadDataFwdRsp rsp) {
        try {
            mDBFragment.onReadDataFwdRsp(id,rsp);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDBRebootRsp(String id, MsgStateRsp rsp) {
        mDBFragment.onDBRebootRsp(id,rsp);
    }

    @Override
    public void onDBUpgradeRsp(String id, MsgStateRsp rsp) {
        mDBFragment.onDBUpgradeRsp(id,rsp);
    }

    @Override
    public void onSetDeviceId(String id, MsgStateRsp rsp) {
        mDBFragment.onSetDeviceId(id,rsp);
    }

    @Override
    public void onStartSenseRsp(String id, MsgCmdRsp rsp) {
        mDBFragment.onStartSenseRsp(id,rsp);
    }

    @Override
    public void onStopSenseRsp(String id, MsgCmdRsp rsp) {
        mDBFragment.onStopSenseRsp(id,rsp);
    }

    @Override
    public void onSenseReportRsp(String id, MsgSenseReportRsp rsp) {
        mDBFragment.onSenseReportRsp(id,rsp);
    }

    @Override
    public void onDBStartSSDetectRsp(String id, MsgCmdRsp rsp) {
        AppLog.I("onDBStartSSDetectRsp(): id = " + id + ", rsp = " + rsp);
        mDBFragment.onDBStartSSDetectRsp(id,rsp);
    }

    GnbCityDialog mGnbCityDialog;
    public void cityDialog() {
        mGnbCityDialog = new GnbCityDialog(mContext);
        mGnbCityDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //final String sync_mode = PrefUtil.build().getValue("sync_mode", "Air").toString();
                //tv_sync_model.setText(sync_mode.equals("Air") ? mContext.getString(R.string.air) : mContext.getString(R.string.GPS));
                mGnbCityDialog = null;
            }
        });
        mGnbCityDialog.show();
    }
}