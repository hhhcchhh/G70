package com.simdo.g73cs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.nr.FTP.FTPUtil;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.UeidBean;
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
import com.nr.Socket.ConnectProtocol;
import com.nr.Socket.LogAndUpgrade;
import com.nr.Socket.MessageControl.MessageController;
import com.nr.Socket.OnSocketChangeListener;
import com.nr.Socket.ZTcpService;
import com.simdo.g73cs.Adapter.FragmentAdapter;
import com.simdo.g73cs.Adapter.StepAdapter;
import com.simdo.g73cs.Bean.CheckBoxBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.Dialog.LoginDialog;
import com.simdo.g73cs.Fragment.FreqFragment;
import com.simdo.g73cs.Fragment.SettingFragment;
import com.simdo.g73cs.Fragment.TraceCatchFragment;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DBUtil;
import com.simdo.g73cs.Util.DeviceUtil;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PermissionsUtil;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.StatusBarUtil;
import com.simdo.g73cs.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

//1
public class MainActivity extends AppCompatActivity implements OnSocketChangeListener,
        MessageController.OnSetGnbListener, LogAndUpgrade.OnScpListener, FTPUtil.OnFtpListener {

    private static MainActivity instance;
    private Context mContext;
    public Activity mActivity;
    private final List<DeviceInfoBean> deviceList = new ArrayList<>(); // 存储已连接的设备
    private final List<MyUeidBean> blackList = new ArrayList<>(); // 存储黑/白名单
    private int tipType = 0; // 0:未提示  1：电压小于17提示  2：电压小于16提示
    public static String devA = "5G_A";   //5G_A
    public static String devB = "5G_B";   //5G_B
    public static String devC = "4G_A";   //4G_A
    public static String devD = "4G_B";   //4G_B
    public static boolean _5G_READY_1 = false;   //5G设备状态（双板）
    public static boolean _5G_READY_2 = false;   //5G设备状态（双板）
    public static boolean _4G_READY_1 = false;   //5G设备状态（双板）
    public static boolean _4G_READY_2 = false;   //5G设备状态（双板）
    public static Boolean ifDebug = false;

    public List<CheckBoxBean> getCheck_box_list() {
        return check_box_list;
    }

    private final List<CheckBoxBean> check_box_list = new ArrayList<>();    //自动扫频列表

    private DrawerLayout drawerLayout;
    private View drawerView;
    private TextView tv_statue;
    private ImageView iv_state;
    private ImageView iv_5G_state;
    private ImageView iv_4G_state;
    private ImageView iv_left;
    private ConstraintLayout left;
    public boolean isUseDefault = false;
    private PopupWindow popupWindow;
    ConstraintLayout group_status;
    public LinkedList<ScanArfcnBean> freqList = new LinkedList<>();
    private boolean isStopScan = true;  //是否结束扫频
    private ImageView iv_anim_freq;
    private Button btn_state_test;
    private int heartBeatCount = 0;

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLog.I("MainActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //StatusBarUtil.setLightStatusBar(this, true);
        //保持屏幕唤醒
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        instance = this;
        mContext = this;
        mActivity = this;
        requestPermissions();
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
            Toast.makeText(getApplicationContext(), R.string.refuse_tip, Toast.LENGTH_SHORT).show();
            finish();
        }
    };
    LoginDialog mLoginDialog;

    private void startActivity() {
        AppLog.createLogFile("+++ ON CREATE +++");
        initObject(); // 初始化对象以及数据
        initView();   // 初始化视图以及监听事件
        initSDK(); // 先绑定服务
        DBUtil.initDataBase();  //初始化sqlite
        // 弹出登录界面
        mLoginDialog = new LoginDialog(mContext);
        mLoginDialog.setOnDismissListener(dialogInterface -> mLoginDialog = null);
        mLoginDialog.show();

    }

    private void initSDK() {
        // 先绑定服务
        startBindService(mContext);
    }

    /**
     * 绑定主服务
     */
    public void startBindService(Context context) {
        AppLog.I("#####startBindService");
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
            // 再开SDK
            NrSdk.build().init(mContext);
            // 定位、扫频、SOCKET、升级取LOG等监听配置
            initSdkListener();
        }
    };

    /**
     * 解除绑定主服务
     */
    public void unBindService() {
        unbindService(connection);
    }

    private void initSdkListener() {
        // 定位、扫频、SOCKET、升级取LOG等监听配置
        ZTcpService.build().addConnectListener(this);
        MessageController.build().setOnSetGnbListener(this);
        LogAndUpgrade.build().setOnScpListener(this);
        FTPUtil.build().setFtpListener(this);
    }

    TextView tv_first_channel_state, tv_second_channel_state, tv_third_channel_state, tv_forth_channel_state;
    TextView tv_5_channel_state, tv_6_channel_state, tv_7_channel_state, tv_8_channel_state;
    ImageView iv_cell_1_nr, iv_cell_2_nr, iv_cell_1_lte, iv_cell_2_lte;
    FragmentAdapter fragmentAdapter;
    //StateFragment mStateFragment;
    TraceCatchFragment mTraceCatchFragment;
    FreqFragment mFreqFragment;
    SettingFragment mSettingFragment;

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initView() {
        //焦点监听器
        final ViewTreeObserver viewTreeObserver = this.getWindow().getDecorView().getViewTreeObserver();
        viewTreeObserver.addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View view, View view1) {
                AppLog.D("old view : " + view);
                AppLog.D("new  view : " + view1);
            }
        });

        btn_state_test = findViewById(R.id.btn_state_test);
        btn_state_test.setOnClickListener(view -> {
                    if (deviceList.isEmpty()) {
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    for (DeviceInfoBean device : deviceList) {
                        sb.append("device: ").append(device.getRsp().getDevName()).append(" workState").append(device.getWorkState())
                                .append("\n").append("channel 1 state").append(device.getTraceUtil().getWorkState(0))
                                .append("\n").append("channel 2 state").append(device.getTraceUtil().getWorkState(1));
                    }
                    Util.showToast(sb.toString(), Toast.LENGTH_LONG);
                }
        );

        AppLog.I("MainActivity initView");
        tv_first_channel_state = findViewById(R.id.tv_first_channel_state);
        tv_second_channel_state = findViewById(R.id.tv_second_channel_state);
        tv_third_channel_state = findViewById(R.id.tv_third_channel_state);
        tv_forth_channel_state = findViewById(R.id.tv_forth_channel_state);
        tv_5_channel_state = findViewById(R.id.tv_5_channel_state);
        tv_6_channel_state = findViewById(R.id.tv_6_channel_state);
        tv_7_channel_state = findViewById(R.id.tv_7_channel_state);
        tv_8_channel_state = findViewById(R.id.tv_8_channel_state);

        drawerLayout = findViewById(R.id.drawer_layout);
        tv_statue = findViewById(R.id.tv_state);
        iv_state = findViewById(R.id.iv_state);
        iv_5G_state = findViewById(R.id.iv_5G_state);
        iv_4G_state = findViewById(R.id.iv_4G_state);
        iv_left = findViewById(R.id.iv_left);
        left = findViewById(R.id.left);

        tv_statue.setOnClickListener(view -> drawerLayout.openDrawer(left));
        iv_state.setOnClickListener(view -> drawerLayout.openDrawer(left));
        iv_5G_state.setOnClickListener(view -> drawerLayout.openDrawer(left));
        iv_4G_state.setOnClickListener(view -> drawerLayout.openDrawer(left));
        iv_left.setOnClickListener(view -> drawerLayout.openDrawer(left));

        initProgress();

        initCatchCount(); // 初始化侦码数量文本

        //设置状态栏颜色
        StatusBarUtil.setStatusBarColor(this, R.color.status_bar_color);
        getWindow().setNavigationBarColor(ContextCompat.getColor(ZApplication.getInstance().getContext(), R.color.navigationBar_color));
        final TextView main_page_title = findViewById(R.id.main_page_title);
        main_page_title.setText(getString(R.string.trace));
        // 加载底部导航标签
        List<Fragment> fragmentList = new ArrayList<>();
        //mStateFragment = new StateFragment(mContext);
        mFreqFragment = new FreqFragment(mContext);
        mTraceCatchFragment = new TraceCatchFragment(mContext, mFreqFragment);
        mSettingFragment = new SettingFragment(mContext, mTraceCatchFragment, mFreqFragment);
        //fragmentList.add(mStateFragment);
        fragmentList.add(mTraceCatchFragment);
        fragmentList.add(mFreqFragment);
        fragmentList.add(mSettingFragment);

        String[] titles = new String[]{getString(R.string.trace), getString(R.string.freq), getString(R.string.setting)};
        int[] icons = new int[]{R.drawable.location_tab_icon, R.drawable.freq_tab_icon, R.drawable.setting_tab_icon};
        int[] colors = new int[]{Color.parseColor("#FF1663FF"), Color.parseColor("#858484")}; //选中颜色  正常颜色

        int[][] states = new int[2][]; //状态
        states[0] = new int[]{android.R.attr.state_selected}; //选中
        states[1] = new int[]{}; //默认
        ColorStateList colorList = new ColorStateList(states, colors);

        ViewPager2 view_pager = findViewById(R.id.view_pager);
        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), getLifecycle(), fragmentList);
        view_pager.setAdapter(fragmentAdapter);
        view_pager.setCurrentItem(0);
        view_pager.setUserInputEnabled(false);
//        view_pager.s  //不先加载会导致自动扫频找不到界面

        view_pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                main_page_title.setText(titles[position]);
            }
        });

        TabLayout tab_layout = findViewById(R.id.tab_layout);
        TabLayoutMediator tab = new TabLayoutMediator(tab_layout, view_pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {

                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_tab, null);

                TextView tv_tab_name = view.findViewById(R.id.tv_tab_name);
                tv_tab_name.setText(titles[position]);
                tv_tab_name.setTextColor(colorList);

                ImageView iv_tab_icon = view.findViewById(R.id.iv_tab_icon);
                iv_tab_icon.setImageResource(icons[position]);
                //设置图片宽高
                iv_tab_icon.getLayoutParams().width = Util.dp2px(MainActivity.this, 20);
                iv_tab_icon.getLayoutParams().height = Util.dp2px(MainActivity.this, 20);
                tab.setCustomView(view);
            }
        });
        tab.attach();

        //监听
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {
//                AppLog.D("onDrawerSlide滑动中");
            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                AppLog.D("onDrawerOpened打开");
                initDrawerLeft();
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
                AppLog.D("onDrawerClosed关闭");
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }

        });
    }


    StepAdapter mStepAdapter;
    private TextView tv_first_channel_sync, tv_second_channel_sync, tv_third_channel_sync, tv_forth_channel_sync, tv_temp, tv_vol;
    private TextView tv_5_channel_sync, tv_6_channel_sync, tv_7_channel_sync, tv_8_channel_sync;
    private String sync_state_1, sync_state_2, sync_state_3, sync_state_4;
    private String sync_state_5, sync_state_6, sync_state_7, sync_state_8;
    Double vol = 0d, temp = 0d;

    private void setPopupWindowData() {
        if (tv_first_channel_sync != null) {
            tv_first_channel_sync.setText(sync_state_1);
            tv_second_channel_sync.setText(sync_state_2);
            tv_third_channel_sync.setText(sync_state_3);
            tv_forth_channel_sync.setText(sync_state_4);
            tv_5_channel_sync.setText(sync_state_5);
            tv_6_channel_sync.setText(sync_state_6);
            tv_7_channel_sync.setText(sync_state_7);
            tv_8_channel_sync.setText(sync_state_8);
            tv_temp.setText(String.valueOf(temp));
            tv_vol.setText(String.valueOf(vol));
        }
    }

    //1

    /**
     * 创建DrawerLeft
     */
    protected void initDrawerLeft() {
        tv_first_channel_sync = findViewById(R.id.tv_first_channel_sync);
        tv_second_channel_sync = findViewById(R.id.tv_second_channel_sync);
        tv_third_channel_sync = findViewById(R.id.tv_third_channel_sync);
        tv_forth_channel_sync = findViewById(R.id.tv_forth_channel_sync);
        tv_5_channel_sync = findViewById(R.id.tv_5_channel_sync);
        tv_6_channel_sync = findViewById(R.id.tv_6_channel_sync);
        tv_7_channel_sync = findViewById(R.id.tv_7_channel_sync);
        tv_8_channel_sync = findViewById(R.id.tv_8_channel_sync);

        tv_temp = findViewById(R.id.device_tmp);
        tv_vol = findViewById(R.id.device_voltage);
        setPopupWindowData();

        Window window = getWindow();
        DisplayMetrics metric = new DisplayMetrics();
        window.getWindowManager().getDefaultDisplay().getRealMetrics(metric);
    }

    /**
     * 初始化对象
     */
    private void initObject() {
        AppLog.D("initObject()");
        //GpsUtil.build().openGPSSettings();
        List<MyUeidBean> ueidBeanList = PrefUtil.build().getUeidList();
        if (ueidBeanList != null && ueidBeanList.size() > 0) {
            blackList.clear();
            blackList.addAll(ueidBeanList);
        }
        GnbCity.build().init();
        String lose_sync = getString(R.string.lose_sync);
        sync_state_1 = lose_sync;
        sync_state_2 = lose_sync;
        sync_state_3 = lose_sync;
        sync_state_4 = lose_sync;
        sync_state_5 = lose_sync;
        sync_state_6 = lose_sync;
        sync_state_7 = lose_sync;
        sync_state_8 = lose_sync;

        initCheckBoxList();
    }

    /**
     * 退出应用对SDK包处理
     */
    private void closeSDK() {
        MessageController.build().removeOnSetGnbListener();
        LogAndUpgrade.build().removeOnScpListener();
        ZTcpService.build().removeConnectListener(this);
        NrSdk.build().onDestory();
        ZTcpService.build().onDestroy();
    }

    private long backPressed;

    @Override
    public void onBackPressed() {
        if (backPressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();

            //System.exit(0);
            finish();
        } else {
            Util.showToast(getString(R.string.click_again));
            backPressed = System.currentTimeMillis();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppLog.I("++++++ MainActivity onDestroy()");
        closeSDK();
        unBindService();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                System.exit(0);
                //android.os.Process.killProcess(android.os.Process.myPid());
            }
        }, 100);
    }

    // 》》》》》》》》》》》》》》》》》》》》》》》供Fragment 调用方法开始》》》》》》》》》》》》》》》》》》》》》》

    /**
     * 初始化侦码数量
     */
    public void initCatchCount() {

    }

    /**
     * 更新侦码数量
     *
     * @param telecom_count 电信数量
     * @param mobile_count  移动数量
     * @param unicom_count  联通数量
     * @param sva_count     广电数量
     */
    public void updateCatchCount(int all_count, int telecom_count, int mobile_count, int unicom_count, int sva_count) {

    }

    /**
     * 更新连接状态
     *
     * @param logicIndex 0, 1 NR   2, 3 LTE
     * @param state      101 连接中  103 已连接  100 未连接
     */
    //1
    public void updateConnectState(int logicIndex, int state) {
        AppLog.I("updateConnectState logicIndex = " + logicIndex + ", state = " + state);
        if (mLoginDialog != null) mLoginDialog.updateConnectState(logicIndex, state);
        if (state == ConnectProtocol.SOCKET.STATE_CONNECTED) {
            String idle_str = getString(R.string.idle);
            updateProgress(logicIndex, 0, 0, idle_str, false);
            updateProgress(logicIndex, 0, 1, idle_str, false);
        } else {
            String not_ready_str = getString(R.string.not_ready);
            updateProgress(logicIndex, 0, 0, not_ready_str, false);
            updateProgress(logicIndex, 0, 1, not_ready_str, false);
//            tv_statue.setText(not_ready_str);
//            iv_state.setBackgroundResource(R.mipmap.icon_ready_n2);
        }
    }

    /**
     * 初始化工作加载进度条
     */
    //1
    public void initProgress() {
        String not_ready_str = getString(R.string.not_ready);
        updateProgress(0, 0, 0, not_ready_str, false);
        updateProgress(0, 0, 1, not_ready_str, false);
        updateProgress(1, 0, 1, not_ready_str, false);
        updateProgress(1, 0, 0, not_ready_str, false);
        updateProgress(2, 0, 0, not_ready_str, false);
        updateProgress(2, 0, 1, not_ready_str, false);
        updateProgress(3, 0, 0, not_ready_str, false);
        updateProgress(3, 0, 1, not_ready_str, false);
    }


    /**
     * 更新侧边状态
     *
     * @param logicIndex    type 0 NR   1 LTE
     * @param pro           进度值
     * @param DeviceCell_id 小区id 0-2，载波分裂要做特殊处理否则会出错
     */
    //1
    //待修改：载波频点
    public void updateProgress(int logicIndex, int pro, int DeviceCell_id, String info, boolean isFail) {
//        int color = isFail ? ContextCompat.getColor(mContext, R.color.color_e65c5c) : ContextCompat.getColor(mContext, R.color.color_1a1a1a);
        //更新全部
        if (logicIndex == -1) {
            tv_first_channel_state.setText(info);
            tv_second_channel_state.setText(info);
            tv_third_channel_state.setText(info);
            tv_forth_channel_state.setText(info);
            tv_5_channel_state.setText(info);
            tv_6_channel_state.setText(info);
            tv_7_channel_state.setText(info);
            tv_8_channel_state.setText(info);
            return;
        }
        int channelIndex = logicIndex * 2 + DeviceCell_id;
        if (channelIndex == 0) {
            tv_first_channel_state.setText(info);
        } else if (channelIndex == 1) {
            tv_second_channel_state.setText(info);
        } else if (channelIndex == 2) {
            tv_third_channel_state.setText(info);
        } else if (channelIndex == 3) {
            tv_forth_channel_state.setText(info);
        } else if (channelIndex == 4) {
            tv_5_channel_state.setText(info);
        } else if (channelIndex == 5) {
            tv_6_channel_state.setText(info);
        } else if (channelIndex == 6) {
            tv_7_channel_state.setText(info);
        } else if (channelIndex == 7) {
            tv_8_channel_state.setText(info);
        }

    }

    // 《《《《《《《《《《《《《《《《《《《《《《《供Fragment 调用方法结束《《《《《《《《《《《《《《《《《《《《《《


    // **************************************更新UI方法开始*******************************************
    //待修改：载波频点
    private void refreshHeartInfo(String id, int index) {
        AppLog.I("refreshUiInfo(): update id = " + id + ", index = " + index);

        int firstState = deviceList.get(index).getRsp().getFirstState();
        int secondState = deviceList.get(index).getRsp().getSecondState();
        if (deviceList.get(index).getRsp().getDevState() == GnbStateRsp.devState.ABNORMAL) {
            // 非定位状态走这里
            // 定位状态下，走GNB_STATE_PHY_ABNORMAL
            refreshWorkState(index, GnbBean.State.NONE, getString(R.string.dev_err_tip));
            Util.showToast(getString(R.string.dev_err_tip));
            //showRemindDialog("异常提示", "设备异常，须掉电重启");
            return;
            /*if (firstState != GnbBean.State.TRACE && secondState != GnbBean.State.TRACE) {

            }*/
        }
        if (firstState == GnbStateRsp.gnbState.GNB_STATE_PHY_ABNORMAL) {
            refreshWorkState(index, GnbBean.State.NONE, getString(R.string.phy_err_tip));
        }
        if (secondState == GnbStateRsp.gnbState.GNB_STATE_PHY_ABNORMAL) {
            refreshWorkState(index, GnbBean.State.NONE, getString(R.string.phy_err_tip));
        }
    }


    //设备实际索引
    //1
    private void refreshWorkState(int index, int state, String stateStr) {
        deviceList.get(index).setWorkState(state); // 更新工作状态
    }

    // **************************************更新UI方法结束*******************************************

    @Override
    public void OnFtpConnectFail(String id, boolean b) {
        AppLog.I("OnFtpConnectFail():  id = " + id + ", b = " + b);
        mSettingFragment.OnFtpConnectFail(id, b);
    }

    @Override
    public void OnFtpGetFileRsp(String id, boolean b) {
        AppLog.I("OnFtpGetFileRsp():  id = " + id + ", b = " + b);
        mSettingFragment.OnFtpGetFileRsp(id, b);
    }

    @Override
    public void OnFtpPutFileRsp(String id, boolean b) {
        AppLog.I("OnFtpPutFileRsp(): b = " + b);
        mSettingFragment.OnFtpPutFileRsp(b);
    }

    @Override
    public void OnScpConnectFail(String id, boolean b) {
        AppLog.I("OnScpConnectFail():  id = " + id + ", b = " + b);
    }

    @Override
    public void OnScpGetLogRsp(String rsp, boolean b) {
        AppLog.I("OnScpGetLogRsp():  rsp = " + rsp + ", b = " + b);
    }

    @Override
    public void OnScpUpgradeFileRsp(String rsp, boolean b) {
        AppLog.I("OnScpUpgradeFileRsp():  rsp = " + rsp + ", b = " + b);
    }

    //1
    @Override
    public void onHeartStateRsp(GnbStateRsp rsp) {
        AppLog.I("onHeartStateRsp():  rsp = " + rsp);
        if (rsp == null) return;
        String typeStr = "";
        if (rsp.getDevName().contains(devA)) typeStr = devA;
        else if (rsp.getDevName().contains(devB)) typeStr = devB;
        else if (rsp.getDevName().contains(devC)) typeStr = devC;
        else if (rsp.getDevName().contains(devD)) typeStr = devD;

        //设备判断
        if (typeStr.isEmpty()) return; // 既不是NR 也不是 LTE，不往下走
        for (int i = 0; i < deviceList.size(); i++) {
            DeviceInfoBean bean = deviceList.get(i);
            if (bean.getRsp().getDevName().contains(typeStr)) {
                if (!bean.getRsp().getDevName().equals(rsp.getDevName()))
                    return; // 已经包含是NR 或 LTE，又来另外的NR 或 LTE，不往下走
            }
        }

        int logicIndex = DeviceUtil.getLogicIndexByDeviceName(typeStr);
        String id = rsp.getDeviceId();
        boolean isAdd = true;
        for (int i = 0; i < deviceList.size(); i++) {
            if (deviceList.get(i).getRsp().getDeviceId().equals(id)) {
                isAdd = false;
                deviceList.get(i).setRsp(rsp);
                refreshHeartInfo(id, i); // 刷新UI信息
                break;
            }
        }

        // G73 deviceList大小为4
        if (isAdd && deviceList.size() < 4) { // 是否为新数据，且当前已连接的设备未超过2个
            AppLog.D("new device id = " + id);
            DeviceInfoBean deviceInfoBean = new DeviceInfoBean();
            deviceInfoBean.setRsp(rsp);

            addToDeviceListOrdered(deviceInfoBean);

            int index = DeviceUtil.getIndexById(id);
            if (index == -1) return;
            refreshHeartInfo(id, index); // 刷新UI信息
            MessageController.build().setGnbTime(id); // 初次，需同步单板时间
            new Handler().postDelayed(() -> {
                MessageController.build().setOnlyCmd(id, GnbProtocol.UI_2_gNB_QUERY_gNB_VERSION); // 初次，读取一次版本
            }, 300);

            updateConnectState(logicIndex, 103);
            //更新左上角状态
            if (logicIndex == 0) {    //5G
                _5G_READY_1 = true;
            } else if (logicIndex == 1) {    //5G
                _5G_READY_2 = true;
            } else if (logicIndex == 2) {    //4G
                _4G_READY_1 = true;
            } else if (logicIndex == 3) {    //4G
                _4G_READY_2 = true;
            }
            if (_5G_READY_1 && _5G_READY_2) {
                iv_5G_state.setBackgroundResource(R.mipmap.icon_iv_5g_ready);
            } else iv_5G_state.setBackgroundResource(R.mipmap.icon_iv_5g_not_ready);
            if (_4G_READY_1 && _4G_READY_2) {
                iv_4G_state.setBackgroundResource(R.mipmap.icon_iv_4g_ready);
            } else iv_4G_state.setBackgroundResource(R.mipmap.icon_iv_4g_not_ready);
            AppLog.I("onHeartStateRsp::  _5G_READY_1 = " + _5G_READY_1 + ", _5G_READY_2 = "
                    + _5G_READY_2 + ", _4G_READY_1 = " + _4G_READY_1 + ", _4G_READY_2 = " + _4G_READY_2);

            //定位或管控切换操作机
            if (GnbStateRsp.gnbState.GNB_STATE_TRACE == deviceList.get(index).getRsp().getFirstState()
                    || GnbStateRsp.gnbState.GNB_STATE_CONTROL == deviceList.get(index).getRsp().getFirstState()) {
                // 定位过程中切换操作机 ,结束通道一的工作
                new Handler().postDelayed(() -> MessageController.build().setCmdAndCellID(deviceList.get(index).getRsp().getDeviceId(), GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.FIRST), 200);
            } else if (GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == deviceList.get(index).getRsp().getFirstState()) {
                updateProgress(logicIndex, 0, 0, getString(R.string.idle), false);
            }
            if (GnbStateRsp.gnbState.GNB_STATE_TRACE == deviceList.get(index).getRsp().getSecondState()
                    || GnbStateRsp.gnbState.GNB_STATE_CONTROL == deviceList.get(index).getRsp().getSecondState()) {
                new Handler().postDelayed(() -> MessageController.build().setCmdAndCellID(deviceList.get(index).getRsp().getDeviceId(), GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.SECOND), 400);
            } else if (GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == deviceList.get(index).getRsp().getSecondState()) {
                updateProgress(logicIndex, 0, 1, getString(R.string.idle), false);
            }
        }


        if (rsp.getDevName().contains(devA)) {
            if (rsp.getGpsSyncState() == GnbStateRsp.Gps.SUCC) {
                sync_state_1 = getString(R.string.gps_sync);
                sync_state_2 = getString(R.string.gps_sync);
            } else {
                String air_sync = getString(R.string.air_sync);
                String idle = getString(R.string.idle);
                sync_state_1 = rsp.getFirstAirState() == GnbStateRsp.Air.SUCC ? air_sync : idle;
                sync_state_2 = rsp.getSecondAirState() == GnbStateRsp.Air.SUCC ? air_sync : idle;
            }
        } else if (rsp.getDevName().contains(devB)) {
            if (rsp.getGpsSyncState() == GnbStateRsp.Gps.SUCC) {
                sync_state_3 = getString(R.string.gps_sync);
                sync_state_4 = getString(R.string.gps_sync);
            } else {
                String air_sync = getString(R.string.air_sync);
                String idle = getString(R.string.idle);
                sync_state_3 = rsp.getFirstAirState() == GnbStateRsp.Air.SUCC ? air_sync : idle;
                sync_state_4 = rsp.getSecondAirState() == GnbStateRsp.Air.SUCC ? air_sync : idle;
            }
        } else if (rsp.getDevName().contains(devC)) {
            if (rsp.getGpsSyncState() == GnbStateRsp.Gps.SUCC) {
                sync_state_5 = getString(R.string.gps_sync);
                sync_state_6 = getString(R.string.gps_sync);
            } else {
                String air_sync = getString(R.string.air_sync);
                String idle = getString(R.string.idle);
                sync_state_5 = rsp.getFirstAirState() == GnbStateRsp.Air.SUCC ? air_sync : idle;
                sync_state_6 = rsp.getSecondAirState() == GnbStateRsp.Air.SUCC ? air_sync : idle;
            }
        } else if (rsp.getDevName().contains(devD)) {
            if (rsp.getGpsSyncState() == GnbStateRsp.Gps.SUCC) {
                sync_state_7 = getString(R.string.gps_sync);
                sync_state_8 = getString(R.string.gps_sync);
            } else {
                String air_sync = getString(R.string.air_sync);
                String idle = getString(R.string.idle);
                sync_state_7 = rsp.getFirstAirState() == GnbStateRsp.Air.SUCC ? air_sync : idle;
                sync_state_8 = rsp.getSecondAirState() == GnbStateRsp.Air.SUCC ? air_sync : idle;
            }
        }

        //刷新按钮
        mTraceCatchFragment.refreshTraceBtn();

        //设备温度
        for (Double d : rsp.getTempList()) {
            if (d > temp) temp = d;
        }

        Double temp1 = 0d;
        for (Double d : deviceList.get(0).getRsp().getTempList()) {
            if (d > temp1) temp1 = d;
        }
        if (temp1 > temp) temp = temp1;

        Double temp2 = 0d;
        if (deviceList.size() >= 2) {
            for (Double d : deviceList.get(1).getRsp().getTempList()) {
                if (d > temp2) temp2 = d;
            }
        }
        if (temp2 > temp) temp = temp2;

        Double temp3 = 0d;
        if (deviceList.size() >= 3) {
            for (Double d : deviceList.get(2).getRsp().getTempList()) {
                if (d > temp3) temp3 = d;
            }
        }
        if (temp3 > temp) temp = temp3;

        Double temp4 = 0d;
        if (deviceList.size() >= 4) {
            for (Double d : deviceList.get(3).getRsp().getTempList()) {
                if (d > temp4) temp4 = d;
            }
        }
        if (temp4 > temp) temp = temp4;

        //设备电量提示
        boolean isShowVolTip = false;
        Double vol1 = deviceList.get(0).getRsp().getVoltageList().get(1);
        if (vol1 > vol) vol = vol1;
        Double vol2 = 0d;
        if (deviceList.size() >= 2) vol2 = deviceList.get(1).getRsp().getVoltageList().get(1);
        if (vol2 > vol) vol = vol2;
        Double vol3 = 0d;
        if (deviceList.size() >= 3) vol3 = deviceList.get(2).getRsp().getVoltageList().get(1);
        if (vol3 > vol) vol = vol3;
        Double vol4 = 0d;
        if (deviceList.size() >= 4) vol4 = deviceList.get(3).getRsp().getVoltageList().get(1);
        if (vol4 > vol) vol = vol4;

        String tip = "";
        if (vol < 1) {
        } else if (vol < 16) {
            if (tipType == 1 || tipType == 0) {
                tipType = 2;
                isShowVolTip = true;
                if (lastTip.contains(getString(R.string.vol_min))) closeCustomDialog();
                tip = getString(R.string.vol_min_tip);
            }
        } else if (vol < 17) {
            if (tipType == 0) {
                tipType = 1;
                isShowVolTip = true;
                tip = getString(R.string.add_vol_tip);
            }
        } else {
            if (tipType == 1 && vol > 17.2) tipType = 0;
        }

        if (isShowVolTip && !lastTip.equals(tip)) {
            lastTip = tip;
            showRemindDialog(getString(R.string.vol_min_title), tip);
        }
        setPopupWindowData();
    }

    //按顺序加入设备（为了后续循环操作按顺序）
    private void addToDeviceListOrdered(DeviceInfoBean deviceInfoBean) {
        AppLog.D("addToDeviceListOrdered");
        ArrayList<Integer> deviceArray = new ArrayList<>();
        for (int i = 0; i < deviceList.size(); i++) {
            deviceArray.add(DeviceUtil.getLogicIndexByDeviceName(deviceList.get(i).getRsp().getDevName()));
        }
        int logicIndex = DeviceUtil.getLogicIndexByDeviceName(deviceInfoBean.getRsp().getDevName());
        if (logicIndex == -1) return;
        if (logicIndex == 0) deviceList.add(0, deviceInfoBean);
        if (logicIndex == 3) deviceList.add(deviceInfoBean);
        if (logicIndex == 1) {
            if (deviceArray.contains(0)) deviceList.add(1, deviceInfoBean);
            else deviceList.add(0, deviceInfoBean);
        }
        if (logicIndex == 2) {
            if (deviceArray.contains(3)) deviceList.add(deviceList.size() - 1, deviceInfoBean);
            else deviceList.add(deviceInfoBean);
        }

    }

    String lastTip = "";

    @Override
    public void onQueryVersionRsp(String id, GnbVersionRsp rsp) {
        AppLog.I("onQueryVersionRsp():  id = " + id + ", rsp = " + rsp);
        if (rsp != null) {
            int index = DeviceUtil.getIndexById(id);
            if (index == -1) return;
            if (rsp.getSwVer() != null) {
                deviceList.get(index).setSoftVer(rsp.getSwVer());
                deviceList.get(index).setHwVer(rsp.getHwVer());
                deviceList.get(index).setFpgaVer(rsp.getFpgaVer());
            } else {
                refreshWorkState(index, GnbBean.State.IDLE, getString(R.string.query_version_file));
            }
        }
    }

    @Override
    public void onStartTraceRsp(String id, GnbTraceRsp rsp) {
        AppLog.I("onStartTraceRsp():  id = " + id + ", rsp = " + rsp);
        if (rsp == null) return;
        mTraceCatchFragment.onStartTraceRsp(id, rsp);
    }

    @Override
    public void onStopTraceRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onStopTraceRsp():  id = " + id + ", rsp = " + rsp);
        mTraceCatchFragment.onStopTraceRsp(id, rsp);
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
    public void onSetLicRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onStartCatchRsp(String id, GnbTraceRsp rsp) {
        AppLog.I("onStartCatchRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onStopCatchRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onStopCatchRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onSetBlackListRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetBlackListRsp():  id = " + id + ", rsp = " + rsp);
        mTraceCatchFragment.onSetBlackListRsp(id, rsp);
    }

    @Override
    public void onSetGnbRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetGnbRsp():  id = " + id + ", rsp = " + rsp);
        mTraceCatchFragment.onSetGnbRsp(id, rsp);
    }

    @Override
    public void onSetTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetTxPwrOffsetRsp():  id = " + id + ", rsp = " + rsp);
        mTraceCatchFragment.onSetTxPwrOffsetRsp(id, rsp);
    }

    @Override
    public void onSetNvTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetNvTxPwrOffsetRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onSetTimeRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetTimeRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onSetRebootRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetRebootRsp():  id = " + id + ", rsp = " + rsp);
        mSettingFragment.onSetRebootRsp(id, rsp);
    }

    @Override
    public void onFirmwareUpgradeRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onFirmwareUpgradeRsp():  id = " + id + ", rsp = " + rsp);
        mSettingFragment.onFirmwareUpgradeRsp(id, rsp);
    }

    @Override
    public void onSetWifiInfoRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetWifiInfoRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onGetLogRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onGetLogRsp():  id = " + id + ", rsp = " + rsp);
        mSettingFragment.onGetLogRsp(id, rsp);
    }

    @Override
    public void onGetOpLogRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onGetOpLogRsp(): id " + id + "  " + rsp);
        mSettingFragment.onGetOpLogRsp(id, rsp);
    }

    @Override
    public void onWriteOpLogRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onWriteOpLogRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDeleteOpLogRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onDeleteOpLogRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onSetBtNameRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetBtNameRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onSetMethIpRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetMethIpRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onGetMethIpRsp(String id, GnbMethIpRsp rsp) {
        AppLog.I("onGetMethIpRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onGpsOffsetRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onGpsOffsetRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onSetPaGpioRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetPaGpioRsp():  id = " + id + ", rsp = " + rsp);
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.OAM_MSG_SET_GPIO_MODE) {
                if (rsp.getRspValue() != GnbProtocol.OAM_ACK_OK) {
                    showRemindDialog(getString(R.string.warning), getString(R.string.pa_cfg_fail));
                }
            }
        }
    }

    @Override
    public void onGetPaGpioRsp(String id, GnbGpioRsp rsp) {
        AppLog.I("onGetPaGpioRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onSetSysInfoRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetSysInfoRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onGetSysInfoRsp(String id, GnbGetSysInfoRsp rsp) {
        AppLog.I("onGetSysInfoRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onSetDualCellRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetDualCellRsp():  id = " + id + ", rsp = " + rsp);
        //mSettingFragment.onSetDualCellRsp(id, rsp);
    }

    @Override
    public void onSetRxGainRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetRxGainRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onGetCatchCfg(String id, GnbCatchCfgRsp rsp) {
        AppLog.I("onGetCatchCfg():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onSetGpsRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetGpsRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onGetSysLogRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onGetSysLogRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onSetFanSpeedRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetFanSpeedRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onSetFanAutoSpeedRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetFanAutoSpeedRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onSetJamArfcn(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetJamArfcn():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onFreqScanRsp(String id, GnbFreqScanRsp rsp) {
        AppLog.I("onFreqScanRsp():  id = " + id + ", rsp = " + rsp);
        //如果是自动模式走mCfgTraceChildFragment的onFreqScanRsp
        if (mTraceCatchFragment.getmCfgTraceChildFragment().isAutoModeFreqRunning()) {
            mTraceCatchFragment.onFreqScanRsp(id, rsp);
        } else {
            mFreqFragment.onFreqScanRsp(id, rsp);
        }
    }

    @Override
    public void onFreqScanGetDocumentRsp(String id, GnbFreqScanGetDocumentRsp rsp) {
        AppLog.I("onFreqScanGetDocumentRsp():  id = " + id + ", rsp = " + rsp);
        //mFreqFragment.onFreqScanGetDocumentRsp(id, rsp);
    }

    @Override
    public void onStopFreqScanRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onStopFreqScanRsp():  id = " + id + ", rsp = " + rsp);
        mFreqFragment.onStopFreqScanRsp(id, rsp);
    }

    @Override
    public void onStartTdMeasure(String id, GnbCmdRsp rsp) {
        AppLog.I("onStartTdMeasure():  id = " + id + ", rsp = " + rsp);
        mSettingFragment.onStartTdMeasure(id, rsp);
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

    //待修改：载波频点
    @Override
    public void onSocketStateChange(String id, int lastState, int newState) {
        AppLog.I("onSocketStateChange():  id = " + id + ", lastState = " + lastState
                + ", newState = " + newState);
        int logicIndex = getLogicIndexById(id);
        int index = DeviceUtil.getIndexById(id);
        if (logicIndex == -1) return;

        if (newState == ConnectProtocol.SOCKET.STATE_CONNECTING) {
            updateConnectState(logicIndex, 101);
        } else if (newState == ConnectProtocol.SOCKET.STATE_CONNECTED) {
            //refreshWorkState(logicIndex, GnbBean.State.IDLE, "连接成功");
            mHandler.removeMessages(logicIndex == 0 ? 1 : 2);

            //似乎是无效的，连接的逻辑放到心跳中
            if (logicIndex == 0) {    //5G
                _5G_READY_1 = true;
            } else if (logicIndex == 1) {    //5G
                _5G_READY_2 = true;
            } else if (logicIndex == 2) {    //4G
                _4G_READY_1 = true;
            } else if (logicIndex == 3) {    //4G
                _4G_READY_2 = true;
            }
            if (_5G_READY_1 && _5G_READY_2) {
                iv_5G_state.setBackgroundResource(R.mipmap.icon_iv_5g_ready);
            } else iv_5G_state.setBackgroundResource(R.mipmap.icon_iv_5g_not_ready);
            if (_4G_READY_1 && _4G_READY_2) {
                iv_4G_state.setBackgroundResource(R.mipmap.icon_iv_4g_ready);
            } else iv_4G_state.setBackgroundResource(R.mipmap.icon_iv_4g_not_ready);

//            tv_statue.setText("准备就绪");
//            iv_state.setBackgroundResource(R.mipmap.icon_ready_h);

            updateConnectState(logicIndex, 103);
            String trace_str = getString(R.string.traceing);
            if (MessageController.build().isTracing(id, GnbProtocol.CellId.FIRST)) {
                updateProgress(logicIndex, 100, 0, trace_str, false);
                mTraceCatchFragment.setBtnStr(true);
            }
            if (MessageController.build().isTracing(id, GnbProtocol.CellId.SECOND)) {
                updateProgress(logicIndex, 100, 1, trace_str, false);
                mTraceCatchFragment.setBtnStr(true);
            }
            if (deviceList.size() > 0 && deviceList.get(index).getWorkState() == GnbBean.State.NONE) {
                deviceList.get(index).setWorkState(GnbBean.State.IDLE);

                MessageController.build().setGnbTime(id); // 断线、重启，再同步单板时间
                new Handler().postDelayed(() -> {
                    MessageController.build().setOnlyCmd(id, GnbProtocol.UI_2_gNB_QUERY_gNB_VERSION); // 断线、重启，再读取一次版本
                }, 200);
            }
        } else {
            if (logicIndex == 0) {    //5G
                _5G_READY_1 = false;
            } else if (logicIndex == 1) {    //5G
                _5G_READY_2 = false;
            } else if (logicIndex == 2) {    //4G
                _4G_READY_1 = false;
            } else if (logicIndex == 3) {    //4G
                _4G_READY_2 = false;
            }
            if (_5G_READY_1 && _5G_READY_2) {
                iv_5G_state.setBackgroundResource(R.mipmap.icon_iv_5g_ready);
            } else iv_5G_state.setBackgroundResource(R.mipmap.icon_iv_5g_not_ready);
            if (_4G_READY_1 && _4G_READY_2) {
                iv_4G_state.setBackgroundResource(R.mipmap.icon_iv_4g_ready);
            } else iv_4G_state.setBackgroundResource(R.mipmap.icon_iv_4g_not_ready);

            updateConnectState(logicIndex, 100);
            mTraceCatchFragment.refreshTraceBtn();

            mSettingFragment.setDeviceDis(id);

            String dis_str = getString(R.string.disconnect_dev);
            //升级中不提醒
            if (!mSettingFragment.isUpdating())
                Util.showToast(dis_str);

            //将此设备移除
            MainActivity.getInstance().getDeviceList().remove(DeviceUtil.getIndexById(id));
            //deleteDev(id);
            Message message = new Message();
            if (logicIndex == 0) {
                message.what = 1;
            } else {
                message.what = 2;
            }
            message.obj = id;
            mHandler.sendMessageDelayed(message, 60 * 1000);
            sync_state_1 = "失步";
            sync_state_2 = "失步";
            sync_state_3 = "失步";
            sync_state_4 = "失步";
            sync_state_5 = "失步";
            sync_state_6 = "失步";
            sync_state_7 = "失步";
            sync_state_8 = "失步";
            temp = 0d;
            vol = 0d;
            setPopupWindowData();
            updateProgress(-1, 0, -1, "未就绪", false);
        }
        AppLog.I("onSocketStateChange(): _5G_READY_1" + _5G_READY_1 + "  _5G_READY_2" + _5G_READY_2 + "  _4G_READY_1" + _4G_READY_1 + "  _4G_READY_2" + _4G_READY_2);
    }


    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                case 2:
                    deleteDev(msg.obj.toString());
                    break;
            }
        }
    };

    // ****************************************弹框方法开始*******************************************
    public void showToast(String msg) {
        Util.showToast(msg);
    }

    /**
     * 显示DIALOG通用接口
     */
    public void createCustomDialog(boolean isFull) {
        int style_dialog = isFull ? R.style.Theme_G73CS_dialog : R.style.style_dialog;
        if (mDialog != null && mDialog.isShowing()) {
            if (mInDialog != null && mInDialog.isShowing()) {
                //创建第三层Dialog
                mInInDialog = new Dialog(this, style_dialog);
                mInInDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mInInDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
                mInInDialog.setCancelable(true);   // 返回键不消失
                return;
            }
            //创建第二层Dialog
            mInDialog = new Dialog(this, style_dialog);
            mInDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mInDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
            mInDialog.setCancelable(true);   // 返回键不消失
            return;
        }
        //创建最外层Dialog
        mDialog = new Dialog(this, style_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mDialog.setCancelable(true);   // 返回键不消失
    }

    public void closeCustomDialog() {
        if (mInInDialog != null) {
            mInInDialog.dismiss();
            mInInDialog = null;
            return;
        }
        if (mInDialog != null) {
            mInDialog.dismiss();
            mInDialog = null;
            return;
        }
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        lastTip = "";
    }

    public void showCustomDialog(View view, boolean bottom) {
        Window window;
        if (mInInDialog != null) {
            mInInDialog.setContentView(view);
            window = mInInDialog.getWindow();
        } else if (mInDialog != null) {
            mInDialog.setContentView(view);
            window = mInDialog.getWindow();
        } else {
            mDialog.setContentView(view);
            window = mDialog.getWindow();
        }
        if (bottom) {
            window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
            window.getDecorView().setPadding(0, 0, 0, 0); // 消除边距
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;   // 设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
//            StatusBarUtil.setStatusBarColor(window, R.color.status_bar_color);
//            StatusBarUtil.setLightStatusBar(window, true);
        } else {
            window.setGravity(Gravity.CENTER); //可设置dialog的位置
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = getResources().getDisplayMetrics().widthPixels * 6 / 7;// 设置宽度屏幕的 6 / 7
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }

        if (mInInDialog != null) mInInDialog.show();
        else if (mInDialog != null) mInDialog.show();
        else mDialog.show();
    }

    public void showCustomDialog(View view, boolean bottom, boolean setHeight) {
        Window window;
        if (mInInDialog != null) {
            if (setHeight) mInInDialog.setCanceledOnTouchOutside(true);
            mInInDialog.setContentView(view);
            window = mInInDialog.getWindow();
        } else if (mInDialog != null) {
            if (setHeight) mInDialog.setCanceledOnTouchOutside(true);
            mInDialog.setContentView(view);
            window = mInDialog.getWindow();
        } else {
            if (setHeight) mDialog.setCanceledOnTouchOutside(true);
            mDialog.setContentView(view);
            window = mDialog.getWindow();
        }
        if (bottom) {
            window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
            window.getDecorView().setPadding(0, 0, 0, 0); // 消除边距
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;   // 设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
            StatusBarUtil.setStatusBarColor(window, R.color.status_bar_color);
//            StatusBarUtil.setLightStatusBar(window, true);
        } else {
            window.setGravity(Gravity.CENTER); //可设置dialog的位置
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = getResources().getDisplayMetrics().widthPixels * 9 / 10;// 设置宽度屏幕的 6 / 7
            if (setHeight) lp.height = getResources().getDisplayMetrics().heightPixels * 3 / 4;
            else lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }

        if (mInInDialog != null) mInInDialog.show();
        else if (mInDialog != null) mInDialog.show();
        else mDialog.show();
    }

    /**
     * 仅显示提示信息
     */
    public void showRemindDialog(String title, String msg) {
        createCustomDialog(false);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_reminder, null);
        TextView tv_title = view.findViewById(R.id.title_reminder);
        TextView tv_msg = view.findViewById(R.id.tv_reminder_msg);
        tv_title.setText(title);
        tv_msg.setText(msg);
        Button btn_cancel = view.findViewById(R.id.btn_reminder_know);
        btn_cancel.setOnClickListener(v -> closeCustomDialog());

        showCustomDialog(view, false);
    }


    // ****************************************弹框方法结束*******************************************
    private Dialog mDialog, mInDialog, mInInDialog;

    public List<DeviceInfoBean> getDeviceList() {
        return deviceList;
    }

    public List<MyUeidBean> getBlackList() {
        return blackList;
    }

    public boolean addBlackList(String name, String imsi) {
        for (MyUeidBean bean : blackList) {
            if (bean.getUeidBean().getImsi().equals(imsi)) {
                return false;
            }
        }
        blackList.add(new MyUeidBean(name, new UeidBean(imsi, imsi), false, false));
        return true;
    }

    public boolean isInBlackList(String imsi) {
        for (MyUeidBean bean : blackList) {
            if (bean.getUeidBean().getImsi().equals(imsi)) {
                return true;
            }
        }
        return false;
    }

    public int getLogicIndexById(String id) {
        return DeviceUtil.getLogicIndexById(id);
    }

    public void deleteDev(String id) {
        for (int i = deviceList.size() - 1; i >= 0; i--) {
            if (deviceList.get(i).getRsp().getDeviceId().equals(id)) {
                int logicIndex = DeviceUtil.getLogicIndexById(id);
                deviceList.remove(i);
                String idle_str = getString(R.string.idle);
                updateProgress(logicIndex, 0, 0, idle_str, false);
                updateProgress(logicIndex, 0, 1, idle_str, false);

                //MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.FIRST);
                //MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.SECOND);
            }
        }
    }

    private final ArrayList<StepBean> nrStepsList = new ArrayList<>();

    private void initCheckBoxList() {
        AppLog.I("initCheckBoxList()");
        if (PaCtl.build().isB97502) {
            check_box_list.add(new CheckBoxBean("N1", 2, devA));
            check_box_list.add(new CheckBoxBean("N28", 4, devB));
            check_box_list.add(new CheckBoxBean("N41", 1, devA));
            check_box_list.add(new CheckBoxBean("N78", 3, devB));
            check_box_list.add(new CheckBoxBean("N79", 3, devB));
            check_box_list.add(new CheckBoxBean("B1", 8, devD));
            check_box_list.add(new CheckBoxBean("B3", 5, devC));
            check_box_list.add(new CheckBoxBean("B5", 6, devC));
            check_box_list.add(new CheckBoxBean("B8", 7, devD));
            check_box_list.add(new CheckBoxBean("B34", 5, devC));
            check_box_list.add(new CheckBoxBean("B39", 6, devC));
            check_box_list.add(new CheckBoxBean("B40", 7, devD));
            check_box_list.add(new CheckBoxBean("B41", 8, devD));
        } else {
            check_box_list.add(new CheckBoxBean("N1", 3, devB));
            check_box_list.add(new CheckBoxBean("N28", 1, devB));
            check_box_list.add(new CheckBoxBean("N41", 3, devA));
            check_box_list.add(new CheckBoxBean("N78", 1, devA));
            check_box_list.add(new CheckBoxBean("N79", 1, devA));
            check_box_list.add(new CheckBoxBean("B1", 4, devB));
            check_box_list.add(new CheckBoxBean("B3", 4, devA));
            check_box_list.add(new CheckBoxBean("B5", 4, devA));
            check_box_list.add(new CheckBoxBean("B8", 4, devA));
            check_box_list.add(new CheckBoxBean("B34", 2, devB));
            check_box_list.add(new CheckBoxBean("B39", 2, devB));
            check_box_list.add(new CheckBoxBean("B40", 2, devB));
            check_box_list.add(new CheckBoxBean("B41", 2, devB));
        }

        initArfcnData();
    }

    //待修改：载波分裂
    private void initArfcnData() {
        AppLog.I("MainActivity initArfcnData()");
        List<Integer> list = new ArrayList<>();
        // 5G
        list.add(427250);
        list.add(428910);
        list.add(422890);
        check_box_list.get(0).addAllArfcnList(list);

        list.clear();
        list.add(154810);
        list.add(152650);
        check_box_list.get(1).addAllArfcnList(list);

        list.clear();
        list.add(504990);
        list.add(512910);
        list.add(516990);
        check_box_list.get(2).addAllArfcnList(list);

        list.clear();
        list.add(627264);
        list.add(633984);
        check_box_list.get(3).addAllArfcnList(list);

        list.clear();
        list.add(723360);
        check_box_list.get(4).addAllArfcnList(list);

        // 4G
        list.clear();
        list.add(100);
        list.add(450);
        check_box_list.get(5).addAllArfcnList(list);

        list.clear();
        list.add(1275);
        list.add(1650);
        list.add(1825);
        check_box_list.get(6).addAllArfcnList(list);

        list.clear();
        list.add(2452);
        check_box_list.get(7).addAllArfcnList(list);

        list.clear();
        list.add(3683);
        check_box_list.get(8).addAllArfcnList(list);

        list.clear();
        list.add(36275);
        check_box_list.get(9).addAllArfcnList(list);

//        list.clear();
//        list.add(37900);
//        check_box_list.get(10).addAllArfcnList(list);

        list.clear();
        list.add(38400);
        check_box_list.get(10).addAllArfcnList(list);

        list.clear();
        list.add(38950);
        check_box_list.get(11).addAllArfcnList(list);

        list.clear();
        list.add(40340);
        list.add(40936);
        check_box_list.get(12).addAllArfcnList(list);
    }

    public ArrayList<ScanArfcnBean> getScanArfcnBeanList() {
        return mFreqFragment.getScanArfcnBeanList();
    }

    public void importArfcn(ScanArfcnBean bean, int pci) {
        mTraceCatchFragment.importArfcn(bean, pci);
    }

    public void setIfDebug(boolean isDebug) {
        ifDebug = isDebug;
        mTraceCatchFragment.setIfDebug(isDebug);
        mFreqFragment.setIfDebug(isDebug);
        btn_state_test.setVisibility(isDebug ? View.VISIBLE : View.GONE);

    }
}