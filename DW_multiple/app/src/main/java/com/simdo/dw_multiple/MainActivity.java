package com.simdo.dw_multiple;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.nr.FTP.FTPUtil;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Response.GnbCatchCfgRsp;
import com.nr.Gnb.Response.GnbCmdRsp;
import com.nr.Gnb.Response.GnbFreqScanGetDocumentRsp;
import com.nr.Gnb.Response.GnbFreqScanRsp;
import com.nr.Gnb.Response.GnbFtpRsp;
import com.nr.Gnb.Response.GnbGetSysInfoRsp;
import com.nr.Gnb.Response.GnbGpioRsp;
import com.nr.Gnb.Response.GnbGpsInOutRsp;
import com.nr.Gnb.Response.GnbGpsRsp;
import com.nr.Gnb.Response.GnbMethIpRsp;
import com.nr.Gnb.Response.GnbReadDataFwdRsp;
import com.nr.Gnb.Response.GnbStateRsp;
import com.nr.Gnb.Response.GnbTraceRsp;
import com.nr.Gnb.Response.GnbUserDataRsp;
import com.nr.Gnb.Response.GnbVersionRsp;
import com.nr.NrSdk;
import com.nr.Socket.LogAndUpgrade;
import com.nr.Socket.MessageControl.MessageController;
import com.nr.Socket.OnSocketChangeListener;
import com.nr.Socket.ZTcpService;
import com.simdo.dw_multiple.Ui.Adapter.FragmentAdapter;
import com.simdo.dw_multiple.Bean.DwDeviceInfoBean;
import com.simdo.dw_multiple.Bean.EventMessageBean;
import com.simdo.dw_multiple.Bean.GnbBean;
import com.simdo.dw_multiple.Ui.Fragment.DWFragment;
import com.simdo.dw_multiple.Ui.Fragment.SettingFragment;
import com.simdo.dw_multiple.Util.AppLog;
import com.simdo.dw_multiple.Util.CustomDialogUtil;
import com.simdo.dw_multiple.Util.PermissionsUtil;
import com.simdo.dw_multiple.Util.StatusBarUtil;
import com.simdo.dw_multiple.Util.TraceUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnSocketChangeListener, View.OnClickListener,
        MessageController.OnSetGnbListener, LogAndUpgrade.OnScpListener, FTPUtil.OnFtpListener {

    private static MainActivity instance;
    private Context mContext;
    private Activity mActivity;
    private LayoutInflater mInflater;
    private List<DwDeviceInfoBean> dwDeviceList = new ArrayList<>();
    List<DWFragment> dwFragmentList = new ArrayList<>();
    private DWFragment mDWFragment1,mDWFragment2,mDWFragment3,mDWFragment4,mDWFragment5;

    private SettingFragment mSettingFragment;
    public static MainActivity getInstance() {
        return instance;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //保持屏幕唤醒
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        StatusBarUtil.setLightStatusBar(this, true);

        instance = this;
        mContext = this;
        mActivity = this;
        initView(); // 初始化视图以及监听事件
        requestPermissions();
    }

    private void initView() {
        mInflater = LayoutInflater.from(this);
        TabLayout tl = findViewById(R.id.tl);
        ViewPager2 vp2 = findViewById(R.id.vp2);
        List<Fragment> fragmentList = new ArrayList<>();
        mDWFragment1 = new DWFragment(this);
        mDWFragment2 = new DWFragment(this);
        mDWFragment3 = new DWFragment(this);
        mDWFragment4 = new DWFragment(this);
        mDWFragment5 = new DWFragment(this);
        mSettingFragment = new SettingFragment(this,dwDeviceList);
        fragmentList.add(mDWFragment1);
        fragmentList.add(mDWFragment2);
        fragmentList.add(mDWFragment3);
        fragmentList.add(mDWFragment4);
        fragmentList.add(mDWFragment5);
        fragmentList.add(mSettingFragment);
        dwFragmentList.add(mDWFragment1);
        dwFragmentList.add(mDWFragment2);
        dwFragmentList.add(mDWFragment3);
        dwFragmentList.add(mDWFragment4);
        dwFragmentList.add(mDWFragment5);

        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), getLifecycle(), fragmentList);
        vp2.setOffscreenPageLimit(6);
        vp2.setAdapter(adapter);

        String[] titles = new String[]{"设备1", "设备2","设备3","设备4","设备5", "设置"};
        int[] icons = new int[]{R.drawable.location_tab_icon, R.drawable.location_tab_icon,R.drawable.location_tab_icon,R.drawable.location_tab_icon,
                R.drawable.location_tab_icon,R.drawable.setting_tab_icon};
        int[] colors = new int[]{Color.parseColor("#07c062"), Color.parseColor("#8F9399")}; //选中颜色  正常颜色

        int[][] states = new int[2][]; //状态
        states[0] = new int[] {android.R.attr.state_selected}; //选中
        states[1] = new int[] {}; //默认
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
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessageBean emb) {
        if (emb.getMsg().equals("dw_version")) {
            mSettingFragment.setVersion_dw(emb.getString());
        }
    }
    public void startBindService(Context context) {
        try {
            Intent intent = new Intent(context, MainService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    final ServiceConnection connection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            AppLog.I("#####onServiceDisconnected");
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            AppLog.I("#####onServiceConnected");
        }
    };
    /**
     * 解除绑定主服务
     */
    public void unBindService() {
        unbindService(connection);
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
        initSDK();    // 初始化SDK
        initObject(); // 初始化对象以及数据
    }

    private void initSDK() {
        startBindService(this);
        NrSdk.build().init(mContext);
        ZTcpService.build().addConnectListener(this);
        MessageController.build().setOnSetGnbListener(this);
        LogAndUpgrade.build().setOnScpListener(this);
        FTPUtil.build().setFtpListener(this);
    }

    private void initObject() {

    }
    public boolean onKeyDown( int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitAppDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    /**
     * 退出APP弹框
     */
    private void exitAppDialog(){
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
            }
        });

        final Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialogUtil.getInstance(MainActivity.this).closeCustomDialog();
            }
        });
        CustomDialogUtil.getInstance(MainActivity.this).showCustomDialog(view, false);
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
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBindService();
        EventBus.getDefault().unregister(this);
        releaseSDK();
        releaseObject();
    }

    private void releaseSDK() {
        NrSdk.build().onDestory();
    }

    private void releaseObject() {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void OnFtpConnectFail(String s, boolean b) {

    }

    @Override
    public void OnFtpGetFileRsp(String s, boolean b) {

    }

    @Override
    public void OnFtpPutFileRsp(String id,boolean state) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).OnFtpPutFileRsp(state);
            }
        }
    }

    @Override
    public void OnScpConnectFail(String id, boolean state) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).OnScpConnectFail(id,state);
            }
        }
    }

    @Override
    public void OnScpGetLogRsp(String id, boolean state) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).OnScpGetLogRsp(id,state);
            }
        }
    }
    @Override
    public void OnScpUpgradeFileRsp(String id, boolean state) {
        AppLog.I("OnScpUpgradeFileRsp(): id =  "+id +"  "+ state);
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).OnScpUpgradeFileRsp(id,state, mSettingFragment.getUpgradeFileName(), mSettingFragment.getUpgradeFilePath());
            }
        }

    }

    @Override
    public void onHeartStateRsp(GnbStateRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWHeartStateRsp()：" + rsp.toString());
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
                MessageController.build().setGnbTime(rsp.getDeviceId());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MessageController.build().setOnlyCmd(rsp.getDeviceId(),GnbProtocol.UI_2_gNB_QUERY_gNB_VERSION);
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
                if (isAdd&&dwDeviceList.size()<5) {
                    DwDeviceInfoBean device = new DwDeviceInfoBean();
                    device.setId(rsp.getDeviceId());
                    device.setRsp(rsp);
                    device.setWorkState(GnbBean.DW_State.IDLE);
                    device.setLicense("");
                    device.setHwVer("");
                    device.setFpgaVer("");
                    device.setTraceUtil(new TraceUtil());
                    dwDeviceList.add(device);
                    MessageController.build().setGnbTime(rsp.getDeviceId());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MessageController.build().setOnlyCmd(rsp.getDeviceId(),GnbProtocol.UI_2_gNB_QUERY_gNB_VERSION);
                        }
                    }, 500);

                }
            }
            for (int i = 0; i < dwDeviceList.size(); i++) {
                if (rsp.getDeviceId().equals(dwDeviceList.get(i).getId())){
                    dwFragmentList.get(i).setDwDevice(dwDeviceList.get(i));
                    dwFragmentList.get(i).onHeartStateRsp(rsp.getDeviceId(),rsp);
                }
            }
        }
    }

    @Override
    public void onQueryVersionRsp(String id, GnbVersionRsp rsp) {
        AppLog.I("onQueryVersionRsp  "+id+"  "+rsp.toString());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwDeviceList.get(i).setHwVer(rsp.getHwVer());
                dwDeviceList.get(i).setFpgaVer(rsp.getFpgaVer());
                dwDeviceList.get(i).setSoftVer(rsp.getSwVer());
                dwFragmentList.get(i).onQueryVersionRsp(id,rsp);
            }
            sb.append("单板"+(i+1)+":"+"\r\n");
            sb.append("硬件版本: ").append(rsp.getHwVer()).append("\r\n");
            sb.append("逻辑版本: ").append(rsp.getFpgaVer()).append("\r\n");
            sb.append("软件版本: ").append(rsp.getSwVer()).append("\r\n");
            sb.append("\r\n");
        }

        mSettingFragment.setBsVersion(sb.toString());
    }

    @Override
    public void onStartTraceRsp(String id, GnbTraceRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onStartTraceRsp(id,rsp);
            }
        }
    }

    @Override
    public void onStopTraceRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onStopTraceRsp(id,rsp);
            }
        }
    }

    @Override
    public void onStartCatchRsp(String s, GnbTraceRsp gnbTraceRsp) {

    }

    @Override
    public void onStopCatchRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetGnbRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onSetGnbRsp(id,rsp);
            }
        }
    }

    @Override
    public void onSetBlackListRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onSetBlackListRsp(id,rsp);
            }
        }
    }

    @Override
    public void onSetTxPwrOffsetRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetNvTxPwrOffsetRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetTimeRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetRebootRsp(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onFirmwareUpgradeRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onDWUpgradeRsp  " + id + "  " + rsp.toString());
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onFirmwareUpgradeRsp(id,rsp);
            }
        }
    }

    @Override
    public void onSetWifiInfoRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onGetLogRsp(String id, GnbCmdRsp rsp ) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onGetLogRsp(id,rsp,mSettingFragment.getGnbLogName());
            }
        }
    }

    @Override
    public void onGetOpLogRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onGetOpLogRsp(id,rsp,mSettingFragment.getGnbLogName());
            }
        }
    }

    @Override
    public void onWriteOpLogRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onDeleteOpLogRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onDeleteOpLogRsp(id,rsp);
            }
        }
    }

    @Override
    public void onSetBtNameRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onSetBtNameRsp(id,rsp);
            }
        }
    }

    @Override
    public void onSetMethIpRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onSetMethIpRsp(id,rsp);
            }
        }
    }

    @Override
    public void onGetMethIpRsp(String id, GnbMethIpRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onGetMethIpRsp(id,rsp);
            }
        }
    }

    @Override
    public void onGpsOffsetRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetPaGpioRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onGetPaGpioRsp(String s, GnbGpioRsp gnbGpioRsp) {

    }

    @Override
    public void onSetSysInfoRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onGetSysInfoRsp(String id, GnbGetSysInfoRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onGetSysInfoRsp(id,rsp);
            }
        }
    }

    @Override
    public void onSetDualCellRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onSetDualCellRsp(id,rsp);
            }
        }
        mSettingFragment.setProgressGone();
    }

    @Override
    public void onSetRxGainRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onSetRxGainRsp(id,rsp);
            }
        }
    }

    @Override
    public void onGetCatchCfg(String s, GnbCatchCfgRsp gnbCatchCfgRsp) {

    }

    @Override
    public void onSetGpsRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onGetSysLogRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onGetSysLogRsp(id,rsp);
            }
        }
    }

    @Override
    public void onSetFanSpeedRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetFanAutoSpeedRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetJamArfcn(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onFreqScanRsp(String id, GnbFreqScanRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onFreqScanRsp(id,rsp);
            }
        }
    }

    @Override
    public void onFreqScanGetDocumentRsp(String id, GnbFreqScanGetDocumentRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onFreqScanGetDocumentRsp(id,rsp);
            }
        }
    }

    @Override
    public void onStopFreqScanRsp(String id, GnbCmdRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onStopFreqScanRsp(id,rsp);
            }
        }
    }

    @Override
    public void onStartTdMeasure(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onGetGpsRsp(String s, GnbGpsRsp gnbGpsRsp) {

    }

    @Override
    public void onSetForwardUdpMsg(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetGpsInOut(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onGetGpsInOut(String s, GnbGpsInOutRsp gnbGpsInOutRsp) {

    }

    @Override
    public void onStartBandScan(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetUserData(String s, GnbUserDataRsp gnbUserDataRsp) {

    }

    @Override
    public void onGetUserData(String s, GnbUserDataRsp gnbUserDataRsp) {

    }

    @Override
    public void onSetGpioTxRx(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onStartControlRsp(String s, GnbTraceRsp gnbTraceRsp) {

    }

    @Override
    public void onStopControlRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetFtpRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onGetFtpRsp(String s, GnbFtpRsp gnbFtpRsp) {

    }

    @Override
    public void onSetDualStackRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onReadDataFwdRsp(String s, GnbReadDataFwdRsp gnbReadDataFwdRsp) {

    }


    @Override
    public void onSocketStateChange(String id, int lastState, int state) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (id.equals(dwDeviceList.get(i).getId())){
                dwFragmentList.get(i).onSocketStateChange(id,lastState,state);
            }
        }
    }
}