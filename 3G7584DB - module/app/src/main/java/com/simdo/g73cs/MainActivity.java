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
import android.content.res.Configuration;
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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.dwdbsdk.Bean.DW.DWProtocol;
import com.dwdbsdk.Bean.UeidBean;
import com.dwdbsdk.DwDbSdk;
import com.dwdbsdk.FTP.FTPUtil;
import com.dwdbsdk.Interface.DWBusinessListener;
import com.dwdbsdk.Interface.FtpListener;
import com.dwdbsdk.Interface.SocketStateListener;
import com.dwdbsdk.MessageControl.MessageController;
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
import com.dwdbsdk.Socket.ConnectProtocol;
import com.dwdbsdk.Socket.ZTcpService;
import com.dwdbsdk.Util.Battery;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.simdo.g73cs.Adapter.FragmentAdapter;
import com.simdo.g73cs.Adapter.StepAdapter;
import com.simdo.g73cs.Bean.CheckBoxBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.Dialog.CustomDialog;
import com.simdo.g73cs.Dialog.LoginDialog;
import com.simdo.g73cs.Fragment.CXSettingFragment;
import com.simdo.g73cs.Fragment.DWSettingFragment;
import com.simdo.g73cs.Fragment.FreqFragment;
import com.simdo.g73cs.Fragment.SettingFragment;
import com.simdo.g73cs.Fragment.TraceCatchFragment;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DataBaseUtil_3g758cx;
import com.simdo.g73cs.Util.DeviceUtil;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PermissionsUtil;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.StatusBarUtil;
import com.simdo.g73cs.Util.Util;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SocketStateListener,
        DWBusinessListener, FtpListener {

    private static MainActivity instance;
    private Context mContext;
    public Activity mActivity;
    private final List<DeviceInfoBean> deviceList = new ArrayList<>(); // 存储已连接的设备
    private final List<MyUeidBean> blackList = new ArrayList<>(); // 存储黑/白名单
    private int tipType = 0; // 0:未提示  1：电压小于17提示  2：电压小于16提示
    public static String devA = "G758_A";   //当前设备
    public static String devB = "G758_B";
    public static String devC = "G758_C";
    public static Boolean ifDebug = false;
    LoginDialog mLoginDialog;
    private final ArrayList<StepBean> nrStepsList = new ArrayList<>();
    private final List<CheckBoxBean> check_box_list = new ArrayList<>();    //自动扫频列表

    private DrawerLayout drawerLayout;
    private View drawerView;
    private ImageView iv_state, iv_dw_state, iv_cx_state;
    private ConstraintLayout left;
    public boolean isUseDefault = false;
    private PopupWindow popupWindow;
    ConstraintLayout group_status;
    public LinkedList<ScanArfcnBean> freqList = new LinkedList<>();
    private boolean isStopScan = true;  //是否结束扫频
    private ImageView iv_anim_freq;
    private Button btn_state_test;
    private int heartBeatCount = 0;
    private Dialog mDialog, mInDialog, mInInDialog;
    TextView tv_first_state, tv_second_state, tv_third_state, tv_forth_state,
            tv_fifth_state, tv_sixth_state, tv_seventh_state, tv_eighth_state,
            tv_ninth_state, tv_tenth_state, tv_eleventh_state, tv_twelfth_state;
    TextView tv_first_arfcn, tv_second_arfcn, tv_third_arfcn, tv_forth_arfcn,
            tv_fifth_arfcn, tv_sixth_arfcn, tv_seventh_arfcn, tv_eighth_arfcn,
            tv_ninth_arfcn, tv_tenth_arfcn, tv_eleventh_arfcn, tv_twelfth_arfcn;
    private String first_arfcn_str, second_arfcn_str, third_arfcn_str, forth_arfcn_str,
            fifth_arfcn_str, sixth_arfcn_str, seventh_arfcn_str, eighth_arfcn_str,
            ninth_arfcn_str, tenth_arfcn_str, eleventh_arfcn_str, twelfth_arfcn_str;
    ImageView iv_cell_1_nr, iv_cell_2_nr, iv_cell_1_lte, iv_cell_2_lte;
    FragmentAdapter fragmentAdapter;
    TraceCatchFragment mTraceCatchFragment;
    FreqFragment mFreqFragment;

    DWSettingFragment mdwSettingFragment;
    CXSettingFragment mCxSettingFragment;
    SettingFragment mSettingFragment;

    StepAdapter mStepAdapter;
    private TextView tv_first_sync, tv_second_sync, tv_third_sync, tv_forth_sync,
            tv_fifth_sync, tv_sixth_sync, tv_seventh_sync, tv_eighth_sync,
            tv_ninth_sync, tv_tenth_sync, tv_eleventh_sync, tv_twelfth_sync,
            tv_temp, tv_vol, tv_vol_warn;
    private String sync_state_1, sync_state_2, sync_state_3, sync_state_4,
            sync_state_5, sync_state_6, sync_state_7, sync_state_8,
            sync_state_9, sync_state_10, sync_state_11, sync_state_12;
    private TextView tv_cx_text_gps, tv_cx_text_air, tv_cx_text_temp, tv_cx_text_state;
    Double vol = 0d, temp = 0d;
    String vol_warn = "";
    int lowCount = 0;
    private long backPressed;

    Button btn_test;
    DBViewModel DBViewModel;

    List<Fragment> fragmentListTrace = new ArrayList<>();
    List<Fragment> fragmentListSetting = new ArrayList<>();


    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLog.I("MainActivity onCreate");
        super.onCreate(savedInstanceState);
        //禁止深色模式
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main2);
        //StatusBarUtil.setLightStatusBar(this, true);
        //保持屏幕唤醒
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        instance = this;
        mContext = this;
        mActivity = this;
        requestPermissions();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    // 打开侧滑菜单
    public void openDrawer() {
//        drawerLayout.openDrawer(drawerView);
    }

    // 关闭侧滑菜单
    public void closeDrawer() {
//        drawerLayout.closeDrawer(drawerView);
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

    private void startActivity() {
        AppLog.createLogFile("+++ ON CREATE +++");
        initObject(); // 初始化对象以及数据
        initView();   // 初始化视图以及监听事件
        initSDK(); // 先绑定服务
        DataBaseUtil_3g758cx.initDataBase();  //初始化sqlite
        initLiveData();
        // 弹出登录界面
        mLoginDialog = new LoginDialog(mContext);
        mLoginDialog.setOnDismissListener(dialogInterface -> mLoginDialog = null);
        mLoginDialog.show();

    }

    private void initLiveData() {
        DBViewModel.getTvGps().observe(this, gps -> {
            tv_cx_text_gps.setText(gps);
        });
        DBViewModel.getTvAirSync().observe(this, air -> {
            tv_cx_text_air.setText(air);
        });
        DBViewModel.getTvTemp().observe(this, temp -> {
            tv_cx_text_temp.setText(temp);
        });
        DBViewModel.getTvState().observe(this, state -> {
//            AppLog.I("MainActivity state = " + state);
            tv_cx_text_state.setText(state);
            if (state.equals(Util.getString(R.string.state_ready))) {
                iv_cx_state.setBackgroundResource(R.mipmap.icon_cx_ready);
            }
        });
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
            DwDbSdk.build().init(mContext);
            // 定位、扫频、SOCKET、升级取LOG等监听配置
            initSdkListener();
            updateSteps(0, StepBean.State.success, getString(R.string.sdk_init_complete));
            updateSteps(0, StepBean.State.success, getString(R.string.into_connect_state));
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
        DwDbSdk.build().addConnectListener(this);
        DwDbSdk.build().setDWBusinessListener(this);
        DwDbSdk.build().setFtpListener(this);
        DBViewModel.initSdkListener();

    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private void initView() {
        //焦点监听器
//        final ViewTreeObserver viewTreeObserver = this.getWindow().getDecorView().getViewTreeObserver();
//        viewTreeObserver.addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
//            @Override
//            public void onGlobalFocusChanged(View view, View view1) {
//                AppLog.D("old view : " + view);
//                AppLog.D("new  view : " + view1);
//                View currentFocus = getCurrentFocus();
//                if (currentFocus != null) {
//                    String focusInfo = currentFocus.getClass().getSimpleName();
//                    AppLog.D("Current focused view: " + focusInfo);
//                } else {
//                    AppLog.D("No view has focus currently.");
//                }
//            }
//        });
        initDrawerLeft();
        initText();

        AppLog.I("MainActivity initView");
        drawerLayout = findViewById(R.id.drawer_layout);
        iv_state = findViewById(R.id.iv_state);
        iv_dw_state = findViewById(R.id.iv_dw_state);
        iv_cx_state = findViewById(R.id.iv_cx_state);
        left = findViewById(R.id.left);

        iv_state.setOnClickListener(view -> drawerLayout.openDrawer(left));
        iv_dw_state.setOnClickListener(view -> drawerLayout.openDrawer(left));
        iv_cx_state.setOnClickListener(view -> drawerLayout.openDrawer(left));
        initProgress(0);

        //设置状态栏颜色
        StatusBarUtil.setStatusBarColor(this, R.color.main_status_bar_color);
        getWindow().setNavigationBarColor(ContextCompat.getColor(ZApplication.getInstance().getContext(), R.color.main_navigation_bar_color));

        final TextView main_page_title = findViewById(R.id.main_page_title);
        main_page_title.setText(getString(R.string.trace));
        main_page_title.setOnClickListener(view -> {
            if (MainActivity.ifDebug) {
                if (mdwSettingFragment.ifClickMany()) {
                    mTraceCatchFragment.setTime_count(20);
                    Util.showToast("已将轮询时间设置为20s");
                }
            }
        });

        // 加载底部导航标签
        List<Fragment> fragmentListBottom = new ArrayList<>();
        mFreqFragment = new FreqFragment();
        mTraceCatchFragment = new TraceCatchFragment(mFreqFragment);
        mSettingFragment = new SettingFragment(mTraceCatchFragment, mFreqFragment);
        mTraceCatchFragment.setSettingFragment(mSettingFragment);
        //fragmentListBottom.add(mStateFragment);
        fragmentListBottom.add(mTraceCatchFragment);
        fragmentListBottom.add(mFreqFragment);
        fragmentListBottom.add(mSettingFragment);

        String[] titles = new String[]{getString(R.string.trace), getString(R.string.freq), getString(R.string.setting)};
        int[] icons = new int[]{R.drawable.location_tab_icon, R.drawable.freq_tab_icon, R.drawable.setting_tab_icon};
        int[] colors = new int[]{Color.parseColor("#FF00E5CA"), Color.parseColor("#FF979797")}; //选中颜色  正常颜色

        int[][] states = new int[2][]; //状态
        states[0] = new int[]{android.R.attr.state_selected}; //选中
        states[1] = new int[]{}; //默认
        ColorStateList colorList = new ColorStateList(states, colors);

        ViewPager2 view_pager = findViewById(R.id.view_pager);
        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), getLifecycle(), fragmentListBottom);
        view_pager.setAdapter(fragmentAdapter);
        view_pager.setCurrentItem(0);
        view_pager.setUserInputEnabled(false);  //禁止滚动

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
                //拦截点击事件
            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
//                AppLog.D("onDrawerOpened打开");
                ViewGroup MainContentView = findViewById(R.id.content_main);
//                disableChildClickEvents(MainContentView);
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
//                AppLog.D("onDrawerClosed关闭");
                ViewGroup MainContentView = findViewById(R.id.content_main);
//                enableChildClickEvents(MainContentView);
//                findViewById(R.id.tab_layout).setClickable(true);
//                findViewById(R.id.tab_layout).setFocusable(true);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
//                AppLog.D("onDrawerStateChanged状态改变" + newState);
                // 在侧边栏打开时禁用主内容视图的所有子视图的点击事件
                if (newState == DrawerLayout.STATE_DRAGGING || newState == DrawerLayout.STATE_SETTLING) {
                } else if (newState == DrawerLayout.STATE_IDLE) {
                    // 恢复主内容视图的子视图的点击事件
                }
            }

            @SuppressLint("ClickableViewAccessibility")
            private void disableChildClickEvents(ViewGroup parent) {
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View child = parent.getChildAt(i);
                    child.setClickable(false);
                    child.setFocusable(false);
                    if (child instanceof ViewGroup) {
                        disableChildClickEvents((ViewGroup) child);
                    }
                }
            }

            private void enableChildClickEvents(ViewGroup parent) {
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View child = parent.getChildAt(i);
                    child.setClickable(true);
                    child.setFocusable(true);
                    if (child instanceof ViewGroup) {
                        enableChildClickEvents((ViewGroup) child);
                    }
                }
            }
        });

        //状态测试
        btn_state_test = findViewById(R.id.btn_state_test);
        if (MainActivity.ifDebug) btn_state_test.setVisibility(View.VISIBLE);
        else btn_state_test.setVisibility(View.GONE);

        btn_state_test.setOnClickListener(view -> {
                    if (deviceList.isEmpty()) {
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < deviceList.size(); i++) {
                        int logicIndex = DeviceUtil.getLogicIndexById(deviceList.get(i).getRsp().getDeviceId());
                        sb.append("device " + logicIndex + " workState: " + deviceList.get(i).getWorkState() + "\n");
                        sb.append("\nchannel " + DeviceUtil.getChannelNum(logicIndex, 0) + " state" + deviceList.get(i).getTraceUtil().getWorkState(0) +
                                "\nchannel " + DeviceUtil.getChannelNum(logicIndex, 1) + " state" + deviceList.get(i).getTraceUtil().getWorkState(1) +
                                "\nchannel " + DeviceUtil.getChannelNum(logicIndex, 2) + " state" + deviceList.get(i).getTraceUtil().getWorkState(2) +
                                "\nchannel " + DeviceUtil.getChannelNum(logicIndex, 3) + " state" + deviceList.get(i).getTraceUtil().getWorkState(3) + "\n");
                    }

                    //单兵
                    if (DBViewModel != null) {
                        sb.append("DB workState: " + DBViewModel.getWorkState());
                    }
                    Util.showToast(sb.toString(), Toast.LENGTH_LONG);
                }
        );

        //列表测试
        btn_test = findViewById(R.id.btn_test);
        if (MainActivity.ifDebug) btn_test.setVisibility(View.VISIBLE);
        else btn_test.setVisibility(View.GONE);
        btn_test.setOnClickListener(v ->
                {
                    StringBuffer arfcnList = new StringBuffer();
                    arfcnList.append("mTraceCatchFragment arfcnBeanHashMap");
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD1") != null) {
                        arfcnList.append("\n通道一=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD1").toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD2") != null) {
                        arfcnList.append("\n通道二=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD2").toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD3") != null) {
                        arfcnList.append("\n通道三=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD3").toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD4") != null) {
                        arfcnList.append("\n通道四=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD4").toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD5") != null) {
                        arfcnList.append("\n通道五=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD5").toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD6") != null) {
                        arfcnList.append("\n通道六=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD6").toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD7") != null) {
                        arfcnList.append("\n通道七=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD7").toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD8") != null) {
                        arfcnList.append("\n通道八=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD8").toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD9") != null) {
                        arfcnList.append("\n通道九=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD9").toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD10") != null) {
                        arfcnList.append("\n通道十=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD10").toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD11") != null) {
                        arfcnList.append("\n通道十一=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD11").toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD12") != null) {
                        arfcnList.append("\n通道十二=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD12").toString());
                    }

                    arfcnList.append("\nTD1_adapter lastArfcn/pci：").append(mTraceCatchFragment.getmCfgTraceChildFragment().getYiguang_adapter().getLastViewText());
                    arfcnList.append("\nTD2_adapter lastArfcn/pci：").append(mTraceCatchFragment.getmCfgTraceChildFragment().getLiandian_adapter().getLastViewText());

                    //arfcnList.append("\nfreqList：").append(getFreqList());
                    arfcnList.append("\nisAutoMode：").append(mTraceCatchFragment.getmCfgTraceChildFragment().isAutoMode);
                    arfcnList.append("\nmLogicTraceUtilA：cell1").append(TraceCatchFragment.mLogicTraceUtilA.getArfcn(0));
                    arfcnList.append("\nmLogicTraceUtilA：cell2").append(TraceCatchFragment.mLogicTraceUtilA.getArfcn(1));
                    arfcnList.append("\nmLogicTraceUtilA：cell3").append(TraceCatchFragment.mLogicTraceUtilA.getArfcn(2));
                    arfcnList.append("\nmLogicTraceUtilA：cell4").append(TraceCatchFragment.mLogicTraceUtilA.getArfcn(3));
                    arfcnList.append("\nmLogicTraceUtilB：cell1").append(TraceCatchFragment.mLogicTraceUtilB.getArfcn(0));
                    arfcnList.append("\nmLogicTraceUtilB：cell2").append(TraceCatchFragment.mLogicTraceUtilB.getArfcn(1));
                    arfcnList.append("\nmLogicTraceUtilB：cell3").append(TraceCatchFragment.mLogicTraceUtilB.getArfcn(2));
                    arfcnList.append("\nmLogicTraceUtilB：cell4").append(TraceCatchFragment.mLogicTraceUtilB.getArfcn(3));
                    arfcnList.append("\nmLogicTraceUtilC：cell1").append(TraceCatchFragment.mLogicTraceUtilC.getArfcn(0));
                    arfcnList.append("\nmLogicTraceUtilC：cell2").append(TraceCatchFragment.mLogicTraceUtilC.getArfcn(1));
                    arfcnList.append("\nmLogicTraceUtilC：cell3").append(TraceCatchFragment.mLogicTraceUtilC.getArfcn(2));
                    arfcnList.append("\nmLogicTraceUtilC：cell4").append(TraceCatchFragment.mLogicTraceUtilC.getArfcn(3));

                    arfcnList.append("\nhistoryBean YgList").append(mTraceCatchFragment.getmCfgTraceChildFragment().getHistoryBean().getYgList().toString());
                    arfcnList.append("\nhistoryBean LdList").append(mTraceCatchFragment.getmCfgTraceChildFragment().getHistoryBean().getLdList().toString());
                    arfcnList.append("\nisChanging :").append(mTraceCatchFragment.getmCfgTraceChildFragment().isChanging());

                    CustomDialog customDialog = new CustomDialog(this, arfcnList.toString());
                    customDialog.show();
                }
        );

    }

    private void setLeftTvSync() {
        if (tv_first_sync != null) {
            tv_first_sync.setText(sync_state_1);
            tv_second_sync.setText(sync_state_2);
            tv_third_sync.setText(sync_state_3);
            tv_forth_sync.setText(sync_state_4);
            tv_fifth_sync.setText(sync_state_5);
            tv_sixth_sync.setText(sync_state_6);
            tv_seventh_sync.setText(sync_state_7);
            tv_eighth_sync.setText(sync_state_8);
            tv_ninth_sync.setText(sync_state_9);
            tv_tenth_sync.setText(sync_state_10);
            tv_eleventh_sync.setText(sync_state_11);
            tv_twelfth_sync.setText(sync_state_12);

            tv_temp.setText(String.valueOf(temp));
            String percent = Battery.build().getPercent();
            tv_vol.setText(percent.equals("检测中") ? percent : percent + "%");
            tv_vol_warn.setText(vol_warn);
        }
    }

    /**
     * 创建PopupWindow
     */
    protected void initDrawerLeft() {
        tv_first_arfcn = findViewById(R.id.tv_first_arfcn);
        tv_second_arfcn = findViewById(R.id.tv_second_arfcn);
        tv_third_arfcn = findViewById(R.id.tv_third_arfcn);
        tv_forth_arfcn = findViewById(R.id.tv_forth_arfcn);
        tv_fifth_arfcn = findViewById(R.id.tv_fifth_arfcn);
        tv_sixth_arfcn = findViewById(R.id.tv_sixth_arfcn);
        tv_seventh_arfcn = findViewById(R.id.tv_seventh_arfcn);
        tv_eighth_arfcn = findViewById(R.id.tv_eighth_arfcn);
        tv_ninth_arfcn = findViewById(R.id.tv_ninth_arfcn);
        tv_tenth_arfcn = findViewById(R.id.tv_tenth_arfcn);
        tv_eleventh_arfcn = findViewById(R.id.tv_eleventh_arfcn);
        tv_twelfth_arfcn = findViewById(R.id.tv_twelfth_arfcn);

        tv_first_sync = findViewById(R.id.tv_first_sync);
        tv_second_sync = findViewById(R.id.tv_second_sync);
        tv_third_sync = findViewById(R.id.tv_third_sync);
        tv_forth_sync = findViewById(R.id.tv_forth_sync);
        tv_fifth_sync = findViewById(R.id.tv_fifth_sync);
        tv_sixth_sync = findViewById(R.id.tv_sixth_sync);
        tv_seventh_sync = findViewById(R.id.tv_seventh_sync);
        tv_eighth_sync = findViewById(R.id.tv_eighth_sync);
        tv_ninth_sync = findViewById(R.id.tv_ninth_sync);
        tv_tenth_sync = findViewById(R.id.tv_tenth_sync);
        tv_eleventh_sync = findViewById(R.id.tv_eleventh_sync);
        tv_twelfth_sync = findViewById(R.id.tv_twelfth_sync);

        tv_first_state = findViewById(R.id.tv_first_state);
        tv_second_state = findViewById(R.id.tv_second_state);
        tv_third_state = findViewById(R.id.tv_third_state);
        tv_forth_state = findViewById(R.id.tv_forth_state);
        tv_fifth_state = findViewById(R.id.tv_fifth_state);
        tv_sixth_state = findViewById(R.id.tv_sixth_state);
        tv_seventh_state = findViewById(R.id.tv_seventh_state);
        tv_eighth_state = findViewById(R.id.tv_eighth_state);
        tv_ninth_state = findViewById(R.id.tv_ninth_state);
        tv_tenth_state = findViewById(R.id.tv_tenth_state);
        tv_eleventh_state = findViewById(R.id.tv_eleventh_state);
        tv_twelfth_state = findViewById(R.id.tv_twelfth_state);

        tv_temp = findViewById(R.id.device_tmp);
        tv_vol = findViewById(R.id.device_voltage);
        tv_vol_warn = findViewById(R.id.device_voltage_warn);
//        setPopupWindowData();

        //单兵
        tv_cx_text_gps = findViewById(R.id.tv_cx_text_gps);
        tv_cx_text_air = findViewById(R.id.tv_cx_text_air);
        tv_cx_text_temp = findViewById(R.id.tv_cx_text_temp);
        tv_cx_text_state = findViewById(R.id.tv_cx_text_state);

        Window window = getWindow();
        DisplayMetrics metric = new DisplayMetrics();
        window.getWindowManager().getDefaultDisplay().getRealMetrics(metric);

    }

    public void initText() {
        if (tv_first_arfcn == null) return;

        tv_first_arfcn.setText("--/--");
        tv_second_arfcn.setText("--/--");
        tv_third_arfcn.setText("--/--");
        tv_forth_arfcn.setText("--/--");
        tv_fifth_arfcn.setText("--/--");
        tv_sixth_arfcn.setText("--/--");
        tv_seventh_arfcn.setText("--/--");
        tv_eighth_arfcn.setText("--/--");
        tv_ninth_arfcn.setText("--/--");
        tv_tenth_arfcn.setText("--/--");
        tv_eleventh_arfcn.setText("--/--");
        tv_twelfth_arfcn.setText("--/--");
    }

    private void updateConfigUi() {
        AppLog.I("TraceChildFragment updateConfigUi ");
        if (tv_first_arfcn == null) return;
        tv_first_arfcn.setText(first_arfcn_str);
        tv_second_arfcn.setText(second_arfcn_str);
        tv_third_arfcn.setText(third_arfcn_str);
        tv_forth_arfcn.setText(forth_arfcn_str);
        tv_fifth_arfcn.setText(fifth_arfcn_str);
        tv_sixth_arfcn.setText(sixth_arfcn_str);
        tv_seventh_arfcn.setText(seventh_arfcn_str);
        tv_eighth_arfcn.setText(eighth_arfcn_str);
        tv_ninth_arfcn.setText(ninth_arfcn_str);
        tv_tenth_arfcn.setText(tenth_arfcn_str);
        tv_eleventh_arfcn.setText(eleventh_arfcn_str);
        tv_twelfth_arfcn.setText(twelfth_arfcn_str);
    }

    public void resetLeftArfcn(int type) {
        first_arfcn_str = "--/--";
        second_arfcn_str = "--/--";
        third_arfcn_str = "--/--";
        forth_arfcn_str = "--/--";
        fifth_arfcn_str = "--/--";
        sixth_arfcn_str = "--/--";
        seventh_arfcn_str = "--/--";
        eighth_arfcn_str = "--/--";
        ninth_arfcn_str = "--/--";
        tenth_arfcn_str = "--/--";
        eleventh_arfcn_str = "--/--";
        twelfth_arfcn_str = "--/--";
        updateConfigUi();

    }

    //刷新报值页面显示，type： devA 0
    public void setConfigInfo(int channelNum, int type, String arfcn, String pci, String imsi) {
        AppLog.I("TraceChildFragment setConfigInfo channelNum = " + channelNum + ", type = " + type + ", arfcn = " + arfcn + ", pci = " + pci + ", imsi = " + imsi);
        if (arfcn.isEmpty()) arfcn = "-";
        if (pci.isEmpty()) pci = "-";
        if (channelNum == 1) {
            first_arfcn_str = arfcn + "/" + pci;
        } else if (channelNum == 2) {
            second_arfcn_str = arfcn + "/" + pci;
        } else if (channelNum == 3) {
            third_arfcn_str = arfcn + "/" + pci;
        } else if (channelNum == 4) {
            forth_arfcn_str = arfcn + "/" + pci;
        } else if (channelNum == 5) {
            fifth_arfcn_str = arfcn + "/" + pci;
        } else if (channelNum == 6) {
            sixth_arfcn_str = arfcn + "/" + pci;
        } else if (channelNum == 7) {
            seventh_arfcn_str = arfcn + "/" + pci;
        } else if (channelNum == 8) {
            eighth_arfcn_str = arfcn + "/" + pci;
        } else if (channelNum == 9) {
            ninth_arfcn_str = arfcn + "/" + pci;
        } else if (channelNum == 10) {
            tenth_arfcn_str = arfcn + "/" + pci;
        } else if (channelNum == 11) {
            eleventh_arfcn_str = arfcn + "/" + pci;
        } else if (channelNum == 12) {
            twelfth_arfcn_str = arfcn + "/" + pci;
        }
        updateConfigUi();
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
        setSyncStateStr(-1, -1, lose_sync);
        DBViewModel = new ViewModelProvider(this).get(DBViewModel.class);
        DBViewModel.initParam();
        initCheckBoxList();
    }

    private List<CheckBoxBean> initCheckBoxList() {
        AppLog.I("initCheckBoxList()");
        if (PaCtl.build().is3G758) {
            check_box_list.add(new CheckBoxBean("N1", 3, devB));
            check_box_list.add(new CheckBoxBean("N28", 3, devB));
            check_box_list.add(new CheckBoxBean("N41", 1, devA));
            check_box_list.add(new CheckBoxBean("N78", 1, devA));
            check_box_list.add(new CheckBoxBean("N79", 1, devA));
            check_box_list.add(new CheckBoxBean("B1", 3, devB));
            check_box_list.add(new CheckBoxBean("B3", 2, devA));
            check_box_list.add(new CheckBoxBean("B5", 2, devA));
            check_box_list.add(new CheckBoxBean("B8", 2, devA));
            check_box_list.add(new CheckBoxBean("B34", 4, devB));
//            check_box_list.add(new CheckBoxBean("B38", 4, devB));
            check_box_list.add(new CheckBoxBean("B39", 4, devB));
            check_box_list.add(new CheckBoxBean("B40", 4, devB));
            check_box_list.add(new CheckBoxBean("B41", 4, devB));
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
        return check_box_list;
    }

    private void initArfcnData() {
        AppLog.I("MainActivity initArfcnData()");
        List<Integer> list = new ArrayList<>();
        // 5G
        list.add(427250);
        list.add(422890);
        list.add(428910);
        list.add(426030);
        list.add(427210);
        list.add(426750);
        list.add(422930);
        check_box_list.get(0).addAllArfcnList(list);

        list.clear();
        list.add(154810);
        list.add(152650);
        list.add(152890);
        list.add(156970);
        list.add(154570);
        list.add(156490);
        list.add(155770);
        check_box_list.get(1).addAllArfcnList(list);

        list.clear();
        list.add(504990);
        list.add(512910);
        list.add(516990);
        list.add(507150);
        list.add(525630);
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
        list.add(300);
        list.add(50);
        list.add(350);
        list.add(375);
        list.add(400);
        list.add(450);
        list.add(500);
        check_box_list.get(5).addAllArfcnList(list);

        list.clear();
        list.add(1275);
        list.add(1300);
        list.add(1350);
        list.add(1650);
        list.add(1506);
        list.add(1500);
        list.add(1531);
        list.add(1524);
        list.add(1850);
        list.add(1600);
        list.add(1800);
        list.add(1825);
        check_box_list.get(6).addAllArfcnList(list);

        list.clear();
        list.add(2452);
        check_box_list.get(7).addAllArfcnList(list);

        list.clear();
        list.add(3682);
        list.add(3683);
        list.add(3641);
        list.add(3621);
        list.add(3590);
        list.add(3725);
        list.add(3741);
        list.add(3768);
        list.add(3769);
        list.add(3770);
        list.add(3775);
        list.add(3745);
        list.add(3710);
        list.add(3737);
        check_box_list.get(8).addAllArfcnList(list);

        list.clear();
        list.add(36275);
        check_box_list.get(9).addAllArfcnList(list);

//        list.clear();
//        list.add(37900);
//        check_box_list.get(10).addAllArfcnList(list);

        list.clear();
        list.add(38400);
        list.add(38544);
        check_box_list.get(10).addAllArfcnList(list);

        list.clear();
        list.add(38950);
        list.add(39148);
        list.add(39292);
        list.add(38750);
        check_box_list.get(11).addAllArfcnList(list);

        list.clear();
        list.add(40936);
        list.add(40340);
        list.add(41134);
        list.add(41332);
        check_box_list.get(12).addAllArfcnList(list);
    }

    /**
     * 退出应用对SDK包处理
     */
    private void closeSDK() {
        DwDbSdk.build().release();
        DwDbSdk.build().removeConnectListener(this);
        DwDbSdk.build().removeDWBusinessListener();
        DwDbSdk.build().removeDBBusinessListener();
        DwDbSdk.build().removeFtpListener();
        ZTcpService.build().removeConnectListener(this);
        MessageController.build().removeDWBusinessListener();
        FTPUtil.build().removeFtpListener();

        DwDbSdk.build().removeDBBusinessListener();
        ScpUtil.build().removeOnScpListener();
    }

    private void dismissLoginDialog() {
        if (mLoginDialog != null && mLoginDialog.isShowing()) {
            mLoginDialog.dismiss();
            mLoginDialog = null;
        }
    }

    @Override
    public void onBackPressed() {
        AppLog.D("MainActivity onBackPressed()");
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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        AppLog.D("MainActivity onConfigurationChanged()");
        super.onConfigurationChanged(newConfig);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppLog.I("++++++ MainActivity onDestroy()");
        DBViewModel = null;
        dismissLoginDialog();
        closeSDK();
        unBindService();

        new Handler().postDelayed(() -> {
            System.exit(0);
            //android.os.Process.killProcess(android.os.Process.myPid());
        }, 100);
    }

    // 》》》》》》》》》》》》》》》》》》》》》》》供Fragment 调用方法开始》》》》》》》》》》》》》》》》》》》》》》
    boolean isDeviceAReady = false;
    boolean isDeviceBReady = false;
    boolean isDeviceCReady = false;
    boolean isDBReady = false;

    /**
     * 更新连接状态
     *
     * @param state 101 连接中  103 已连接  100 未连接
     */
    public void updateConnectState(int logicIndex, int state) {
        AppLog.I("updateConnectState logicIndex = " + logicIndex + ", state = " + state);
        int type;
        if (logicIndex == 0) type = 0;
        else if (logicIndex == 1) type = 1;
        else type = 2;
        if (mLoginDialog != null) mLoginDialog.updateConnectState(type, state);
        String idle_str;

        if (state == ConnectProtocol.SOCKET.STATE_CONNECTED) {
            idle_str = getString(R.string.idle);
            if (logicIndex == 0) isDeviceAReady = true;
            else if (logicIndex == 1) isDeviceBReady = true;
            else isDeviceCReady = true;
        } else {
            idle_str = getString(R.string.not_ready);
            if (logicIndex == 0) isDeviceAReady = false;
            else if (logicIndex == 1) isDeviceBReady = false;
            else isDeviceCReady = false;
        }
        updateLeftTvState(logicIndex, -1, idle_str);
        if (isDeviceAReady && isDeviceBReady && isDeviceCReady)
            iv_dw_state.setBackgroundResource(R.mipmap.icon_dw_ready);
        else
            iv_dw_state.setBackgroundResource(R.mipmap.icon_dw_not_ready);
    }

    /**
     * 更新连接状态
     *
     * @param state 101 连接中  103 已连接  100 未连接
     */
    public void updateDBConnectState(int logicIndex, int state) {
        AppLog.I("updateDBConnectState logicIndex = " + logicIndex + ", state = " + state);

        if (state == ConnectProtocol.SOCKET.STATE_CONNECTED) {
            isDBReady = true;
        } else {
            isDBReady = false;
        }
        if (isDBReady)
            iv_cx_state.setBackgroundResource(R.mipmap.icon_cx_ready);
        else
            iv_cx_state.setBackgroundResource(R.mipmap.icon_cx_not_ready);
    }

    /**
     * 初始化工作加载进度条
     *
     * @param type 0 NR   1 LTE
     */
    public void initProgress(int type) {
        String not_ready_str = getString(R.string.not_ready);
        updateLeftTvState(type, 0, -1, -1, not_ready_str, false);
    }

    /**
     * 更新工作加载进度条
     *
     * @param type    type 0 NR   1 LTE
     * @param pro     进度值
     * @param cell_id 小区id
     */
    public void updateLeftTvState(int type, int pro, int logicIndex, int cell_id, String info, boolean isFail) {
        AppLog.I("updateLeftTvState type = " + type + ", pro = " + pro + ", logicIndex = " + logicIndex + ", cell_id = " + cell_id + ", info = " + info);
        updateLeftTvState(logicIndex, cell_id, info);
    }

    //没用的
    @SuppressLint("NotifyDataSetChanged")
    public void updateSteps(int type, int state, String info) {
//        AppLog.I("updateSteps type = " + type + ", state = " + state + ", info = " + info);
//        // 先转换第一个数据的状态，失败的不变，成功的置灰
//        if (nrStepsList.size() > 0) if (nrStepsList.get(0).getState() != StepBean.State.fail)
//            nrStepsList.get(0).setState(StepBean.State.success_end);
//        // 插到第一个位置
//        nrStepsList.add(0, new StepBean(state, DateUtil.getCurrentTime(), info));
    }

    // 《《《《《《《《《《《《《《《《《《《《《《《供Fragment 调用方法结束《《《《《《《《《《《《《《《《《《《《《《


    // **************************************更新UI方法开始*******************************************
    private void refreshHeartInfo(String id, int index) {
        AppLog.I("refreshUiInfo(): update id = " + id + ", index = " + index);

        int firstState = deviceList.get(index).getRsp().getFirstState();
        int secondState = deviceList.get(index).getRsp().getSecondState();
        int thirdState = deviceList.get(index).getRsp().getThirdState();
        int fourthState = deviceList.get(index).getRsp().getFourthState();
        if (deviceList.get(index).getRsp().getDevState() == GnbStateRsp.devState.ABNORMAL) {
            // 非定位状态走这里
            // 定位状态下，走GNB_STATE_PHY_ABNORMAL
            refreshWorkState(index, GnbBean.DW_State.NONE, getString(R.string.dev_err_tip));
            Util.showToast(getString(R.string.dev_err_tip));
            return;
        }
        if (firstState == GnbStateRsp.gnbState.GNB_STATE_PHY_ABNORMAL
                || secondState == GnbStateRsp.gnbState.GNB_STATE_PHY_ABNORMAL
                || thirdState == GnbStateRsp.gnbState.GNB_STATE_PHY_ABNORMAL
                || fourthState == GnbStateRsp.gnbState.GNB_STATE_PHY_ABNORMAL) {
            refreshWorkState(index, GnbBean.DW_State.NONE, getString(R.string.phy_err_tip));
        }

    }

    //好像只有第一句有用
    private void refreshWorkState(int index, int state, String stateStr) {
        deviceList.get(index).setWorkState(state); // 更新工作状态
        String deviceName = deviceList.get(index).getRsp().getDevName();
        if (deviceName.contains(devA)) {
            if (state == GnbBean.DW_State.NONE)
                updateSteps(0, StepBean.State.fail, stateStr);
        } else if (deviceName.contains(devB)) {
            if (state == GnbBean.DW_State.NONE)
                updateSteps(1, StepBean.State.fail, stateStr);
        }
    }

    public void importArfcn(ScanArfcnBean bean, int pci) {
        mTraceCatchFragment.importArfcn(bean, pci);
    }

    /**
     * @param logicIndex 逻辑索引
     * @param cell_id    设备实际小区
     * @param str        文字
     *                   当logicIndex==-1时为全设置
     *                   当cell_id==-1且logicIndex!=-1时为设置单个设备
     */
    public void updateLeftTvState(int logicIndex, int cell_id, String str) {
        int channelNum = DeviceUtil.getChannelNum(logicIndex, cell_id);
        if (cell_id == -1 || logicIndex == -1) {
            if (logicIndex == -1) {
                tv_first_state.setText(str);
                tv_second_state.setText(str);
                tv_third_state.setText(str);
                tv_forth_state.setText(str);
                tv_fifth_state.setText(str);
                tv_sixth_state.setText(str);
                tv_seventh_state.setText(str);
                tv_eighth_state.setText(str);
                tv_ninth_state.setText(str);
                tv_tenth_state.setText(str);
                tv_eleventh_state.setText(str);
                tv_twelfth_state.setText(str);
            } else {
                switch (logicIndex) {
                    case 0:
                        tv_first_state.setText(str);
                        tv_second_state.setText(str);
                        tv_third_state.setText(str);
                        tv_forth_state.setText(str);
                        break;
                    case 1:
                        tv_fifth_state.setText(str);
                        tv_sixth_state.setText(str);
                        tv_seventh_state.setText(str);
                        tv_eighth_state.setText(str);
                        break;
                    case 2:
                        tv_ninth_state.setText(str);
                        tv_tenth_state.setText(str);
                        tv_eleventh_state.setText(str);
                        tv_twelfth_state.setText(str);
                        break;
                }
            }
            return;
        }
        switch (channelNum) {
            case 1:
                tv_first_state.setText(str);
                break;
            case 2:
                tv_second_state.setText(str);
                break;
            case 3:
                tv_third_state.setText(str);
                break;
            case 4:
                tv_forth_state.setText(str);
                break;
            case 5:
                tv_fifth_state.setText(str);
                break;
            case 6:
                tv_sixth_state.setText(str);
                break;
            case 7:
                tv_seventh_state.setText(str);
                break;
            case 8:
                tv_eighth_state.setText(str);
                break;
            case 9:
                tv_ninth_state.setText(str);
                break;
            case 10:
                tv_tenth_state.setText(str);
                break;
            case 11:
                tv_eleventh_state.setText(str);
                break;
            case 12:
                tv_twelfth_state.setText(str);
                break;
        }
    }

    public void setSyncStateStr(int logicIndex, int cell_id, String str) {
        int channelNum = DeviceUtil.getChannelNum(logicIndex, cell_id);
        if (cell_id == -1 || logicIndex == -1) {
            if (logicIndex == -1) {
                sync_state_1 = str;
                sync_state_2 = str;
                sync_state_3 = str;
                sync_state_4 = str;
                sync_state_5 = str;
                sync_state_6 = str;
                sync_state_7 = str;
                sync_state_8 = str;
                sync_state_9 = str;
                sync_state_10 = str;
                sync_state_11 = str;
                sync_state_12 = str;
            } else {
                switch (logicIndex) {
                    case 0:
                        sync_state_1 = str;
                        sync_state_2 = str;
                        sync_state_3 = str;
                        sync_state_4 = str;
                        break;
                    case 1:
                        sync_state_5 = str;
                        sync_state_6 = str;
                        sync_state_7 = str;
                        sync_state_8 = str;
                        break;
                    case 2:
                        sync_state_9 = str;
                        sync_state_10 = str;
                        sync_state_11 = str;
                        sync_state_12 = str;
                        break;
                }
            }
            return;
        }
        switch (channelNum) {
            case 1:
                sync_state_1 = str;
                break;
            case 2:
                sync_state_2 = str;
                break;
            case 3:
                sync_state_3 = str;
                break;
            case 4:
                sync_state_4 = str;
                break;
            case 5:
                sync_state_5 = str;
                break;
            case 6:
                sync_state_6 = str;
                break;
            case 7:
                sync_state_7 = str;
                break;
            case 8:
                sync_state_8 = str;
                break;
            case 9:
                sync_state_9 = str;
                break;
            case 10:
                sync_state_10 = str;
                break;
            case 11:
                sync_state_11 = str;
                break;
            case 12:
                sync_state_12 = str;
                break;
        }
    }

    // **************************************更新UI方法结束*******************************************

    @Override
    public void onFtpConnectState(String id, boolean b) {
        AppLog.I("onFtpConnectState():  id = " + id + ", b = " + b);
        if (mdwSettingFragment != null)
            mdwSettingFragment.OnFtpConnectFail(id, b);

    }

    @Override
    public void onFtpGetFileProcess(String s, long l) {

    }


    @Override
    public void onFtpGetFileRsp(String id, boolean b) {
        AppLog.I("OnFtpGetFileRsp():  id = " + id + ", b = " + b);
        if (mdwSettingFragment != null)
            mdwSettingFragment.OnFtpGetFileRsp(id, b);
    }

    @Override
    public void onFtpPutFileRsp(String id, boolean b) {
        AppLog.I("OnFtpPutFileRsp(): b = " + b);
        if (mdwSettingFragment != null)
            mdwSettingFragment.OnFtpPutFileRsp(b);
    }

    @Override
    public void onDWHeartStateRsp(String id, GnbStateRsp rsp) {
        AppLog.I("onDWHeartStateRsp():  rsp = " + rsp);
        if (rsp == null) return;
        String typeStr = "";
        if (rsp.getDevName().contains(devA)) typeStr = devA;
        else if (rsp.getDevName().contains(devB)) typeStr = devB;
        else if (rsp.getDevName().contains(devC)) typeStr = devC;

        //检测是否是旧设备
        for (DeviceInfoBean bean : deviceList) {
            if (bean.getRsp().getDevName().contains(typeStr) && !bean.getRsp().getDevName().equals(rsp.getDevName()))
                return;
        }
        int type = -1;
        if (rsp.getDevName().contains(devA)) type = 0;
        else if (rsp.getDevName().contains(devB)) type = 1;
        else if (rsp.getDevName().contains(devC)) type = 2;
        boolean isAdd = true;
        for (int i = 0; i < deviceList.size(); i++) {
            if (deviceList.get(i).getRsp().getDeviceId().equals(id)) {
                isAdd = false;
                deviceList.get(i).setRsp(rsp);
                refreshHeartInfo(id, i); // 刷新UI信息
                break;
            }
        }
        int logicIndex = DeviceUtil.getLogicIndexByDeviceName(rsp.getDevName());
        if (logicIndex == -1) return;
        // G758 deviceList大小暂定为1
        if (isAdd && deviceList.size() < 4) { // 是否为新数据，且当前已连接的设备未超过3个
            AppLog.D("new device id = " + id);
            DeviceInfoBean deviceInfoBean = new DeviceInfoBean();
            deviceInfoBean.setRsp(rsp);

            addToDeviceListOrdered(deviceInfoBean);

            int index = DeviceUtil.getIndexById(id);
            refreshHeartInfo(id, index); // 刷新UI信息
            MessageController.build().setDWTime(id); // 初次，需同步单板时间
            new Handler().postDelayed(() -> {
                MessageController.build().getDWVersion(id); // 初次，读取一次版本
            }, 300);

            updateConnectState(logicIndex, 103);

            if (GnbStateRsp.gnbState.GNB_STATE_TRACE == deviceList.get(index).getRsp().getFirstState()
                    || GnbStateRsp.gnbState.GNB_STATE_CONTROL == deviceList.get(index).getRsp().getFirstState()) {
                // 定位过程中切换操作机 ,结束通道一的工作
                new Handler().postDelayed(() -> {
                    if (DeviceUtil.isNr(DeviceUtil.getChannelNum(logicIndex, DWProtocol.CellId.FIRST)))
                        MessageController.build().stopDWNrTrace(deviceList.get(index).getRsp().getDeviceId(), DWProtocol.CellId.FIRST);
                    else
                        MessageController.build().stopDWLteTrace(deviceList.get(index).getRsp().getDeviceId(), DWProtocol.CellId.FIRST);
                }, 200);
                mTraceCatchFragment.resetArfcnList();
            } else if (GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == deviceList.get(index).getRsp().getFirstState()) {
                updateLeftTvState(type, 0, logicIndex, 0, getString(R.string.idle), false);
            }
            if (GnbStateRsp.gnbState.GNB_STATE_TRACE == deviceList.get(index).getRsp().getSecondState()
                    || GnbStateRsp.gnbState.GNB_STATE_CONTROL == deviceList.get(index).getRsp().getSecondState()) {
                new Handler().postDelayed(() -> {
                    if (DeviceUtil.isNr(DeviceUtil.getChannelNum(logicIndex, DWProtocol.CellId.SECOND)))
                        MessageController.build().stopDWNrTrace(deviceList.get(index).getRsp().getDeviceId(), DWProtocol.CellId.SECOND);
                    else
                        MessageController.build().stopDWLteTrace(deviceList.get(index).getRsp().getDeviceId(), DWProtocol.CellId.SECOND);
                }, 400);
                mTraceCatchFragment.resetArfcnList();
            } else if (GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == deviceList.get(index).getRsp().getSecondState()) {
                updateLeftTvState(type, 0, logicIndex, 1, getString(R.string.idle), false);
            }
            if (GnbStateRsp.gnbState.GNB_STATE_TRACE == deviceList.get(index).getRsp().getThirdState()
                    || GnbStateRsp.gnbState.GNB_STATE_CONTROL == deviceList.get(index).getRsp().getThirdState()) {
                new Handler().postDelayed(() -> {
                    if (DeviceUtil.isNr(DeviceUtil.getChannelNum(logicIndex, DWProtocol.CellId.THIRD)))
                        MessageController.build().stopDWNrTrace(deviceList.get(index).getRsp().getDeviceId(), DWProtocol.CellId.THIRD);
                    else
                        MessageController.build().stopDWLteTrace(deviceList.get(index).getRsp().getDeviceId(), DWProtocol.CellId.THIRD);
                }, 600);
                mTraceCatchFragment.resetArfcnList();
            } else if (GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == deviceList.get(index).getRsp().getThirdState()) {
                updateLeftTvState(type, 0, logicIndex, 2, getString(R.string.idle), false);
            }
            if (GnbStateRsp.gnbState.GNB_STATE_TRACE == deviceList.get(index).getRsp().getFourthState()
                    || GnbStateRsp.gnbState.GNB_STATE_CONTROL == deviceList.get(index).getRsp().getFourthState()) {
                new Handler().postDelayed(() -> {
                    if (DeviceUtil.isNr(DeviceUtil.getChannelNum(logicIndex, DWProtocol.CellId.FOURTH)))
                        MessageController.build().stopDWNrTrace(deviceList.get(index).getRsp().getDeviceId(), DWProtocol.CellId.FOURTH);
                    else
                        MessageController.build().stopDWLteTrace(deviceList.get(index).getRsp().getDeviceId(), DWProtocol.CellId.FOURTH);
                }, 800);
                mTraceCatchFragment.resetArfcnList();
            } else if (GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == deviceList.get(index).getRsp().getFourthState()) {
                updateLeftTvState(type, 0, logicIndex, 3, getString(R.string.idle), false);
            }

            PaCtl.build().closePAById(id);
            //连下两次才能结束
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    PaCtl.build().closePAById(id);
                }
            }, 1000);

        }

        //侧边栏同步状态更新
        if (rsp.getGpsSyncState() == GnbStateRsp.Gps.SUCC) {
            setSyncStateStr(logicIndex, -1, getString(R.string.gps_sync));
        } else {
            String air_sync = getString(R.string.air_sync);
            String idle = getString(R.string.idle);
            setSyncStateStr(logicIndex, 0, rsp.getFirstAirState() == GnbStateRsp.Air.SUCC ? air_sync : idle);
            setSyncStateStr(logicIndex, 1, rsp.getSecondAirState() == GnbStateRsp.Air.SUCC ? air_sync : idle);
            setSyncStateStr(logicIndex, 2, rsp.getThirdAirState() == GnbStateRsp.Air.SUCC ? air_sync : idle);
            setSyncStateStr(logicIndex, 3, rsp.getFourthAirState() == GnbStateRsp.Air.SUCC ? air_sync : idle);
        }


        //设备温度
        for (DeviceInfoBean bean : deviceList) {
            for (Double d : bean.getRsp().getTempList()) {
                if (d > temp) temp = d;
            }
        }

        //设备电压
        int logicIndexLowVol = logicIndex;
        for (DeviceInfoBean bean : deviceList) {
            Double vol1 = bean.getRsp().getVoltageList().get(1);
            if (vol1 < vol) {
                vol = vol1;
                logicIndexLowVol = DeviceUtil.getLogicIndexById(bean.getRsp().getDeviceId());
            }
        }
        boolean isShowVolTip = false;
        String tip = "";
        if (vol < 1) {
        } else if (vol < 16) {
            if (tipType == 1 || tipType == 0) {
                tipType = 2;
                isShowVolTip = true;
                if (lastTip.contains(getString(R.string.vol_min))) closeCustomDialog();
                tip = "板" + (logicIndexLowVol + 1) + getString(R.string.vol_min_tip);
                vol_warn = "板" + (logicIndexLowVol + 1) + "低电";
                lowCount++;
            }
        } else if (vol < 17) {
            if (tipType == 0) {
                tipType = 1;
                isShowVolTip = true;
                tip = "板" + (logicIndexLowVol + 1) + getString(R.string.add_vol_tip);
                vol_warn = "板" + (logicIndexLowVol + 1) + "低电";
                lowCount++;
            }
        } else {
            if ((tipType == 1 || tipType == 2) && vol > 17.2) {
                tipType = 0;
                lowCount = 0;
            }
            vol_warn = "";
        }

        if (isShowVolTip && !lastTip.equals(tip) && lowCount >= 3) {
            lastTip = tip;
            showRemindDialog(getString(R.string.vol_min_title), tip);
        }
        Battery.build().handleVol((int) (vol * 1000));
        setLeftTvSync();

        for (int i = 0; i < deviceList.size(); i++) {
            int logicIndex2 = DeviceUtil.getLogicIndexById(deviceList.get(i).getRsp().getDeviceId());
            String id2 = deviceList.get(i).getRsp().getDeviceId();
            if (deviceList.get(i).getWorkState() == GnbBean.DW_State.GET_LOG ||
                    deviceList.get(i).getWorkState() == GnbBean.DW_State.GETOPLOG)
                continue;

            //解决掉线后结束一直在结束中问题,全空闲显示开始
            if (GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == deviceList.get(i).getRsp().getFirstState() &&
                    GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == deviceList.get(i).getRsp().getSecondState() &&
                    GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == deviceList.get(i).getRsp().getThirdState() &&
                    GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == deviceList.get(i).getRsp().getFourthState()
                    && mTraceCatchFragment.isClickStop()) {
                AppLog.D("设备" + deviceList.get(i).getRsp().getDevName() + "空闲");
                MainActivity.getInstance().getDeviceList().get(i).setWorkState(GnbBean.DW_State.IDLE);
                MainActivity.getInstance().getDeviceList().get(i).getTraceUtil()
                        .setWorkState(DWProtocol.CellId.FIRST, GnbBean.DW_State.IDLE);
                MainActivity.getInstance().getDeviceList().get(i).getTraceUtil()
                        .setWorkState(DWProtocol.CellId.SECOND, GnbBean.DW_State.IDLE);
                MainActivity.getInstance().getDeviceList().get(i).getTraceUtil()
                        .setWorkState(DWProtocol.CellId.THIRD, GnbBean.DW_State.IDLE);
                MainActivity.getInstance().getDeviceList().get(i).getTraceUtil()
                        .setWorkState(DWProtocol.CellId.FOURTH, GnbBean.DW_State.IDLE);
                setLeftTvSync();
                updateLeftTvState(0, 0, logicIndex2, -1, getString(R.string.idle), false);
            }

            if (deviceList.get(i).getWorkState() == GnbBean.DW_State.NONE &&
                    rsp.getSysKickOff() == 1) {     //新增kickOff状态判断为1时设备就绪
                deviceList.get(i).setWorkState(GnbBean.DW_State.IDLE);

                MessageController.build().setDWTime(id2); // 断线、重启，再同步单板时间
                new Handler().postDelayed(() -> {
                    MessageController.build().getDWVersion(id2); // 断线、重启，再读取一次版本
                }, 200);
            }

        }

        //防止一直卡在启动中或结束中
        if (mTraceCatchFragment.getmCfgTraceChildFragment().
                getTv_do_btn().getText().toString().equals("启动中") ||
                mTraceCatchFragment.getmCfgTraceChildFragment().
                        getTv_do_btn().getText().toString().equals("结束中")) {
            mTraceCatchFragment.refreshTraceBtn();
        }
    }

    private void addToDeviceListOrdered(DeviceInfoBean deviceInfoBean) {
        AppLog.D("addToDeviceListOrdered");
        ArrayList<Integer> logicDeviceArray = new ArrayList<>();
        for (int i = 0; i < deviceList.size(); i++) {
            logicDeviceArray.add(DeviceUtil.getLogicIndexByDeviceName(deviceList.get(i).getRsp().getDevName()));
        }
        int logicIndex = DeviceUtil.getLogicIndexByDeviceName(deviceInfoBean.getRsp().getDevName());
        if (logicIndex == -1) return;
        if (logicIndex == 0) deviceList.add(0, deviceInfoBean);
        if (logicIndex == 2) deviceList.add(deviceInfoBean);
        if (logicIndex == 1) {
            if (logicDeviceArray.contains(0)) deviceList.add(1, deviceInfoBean);
            else deviceList.add(0, deviceInfoBean);
        }
    }

    String lastTip = "";

    @Override
    public void onDWQueryVersionRsp(String id, GnbVersionRsp rsp) {
        AppLog.I("onQueryVersionRsp():  id = " + id + ", rsp = " + rsp);
        if (rsp != null) {
            int index = DeviceUtil.getIndexById(id);
            if (index == -1) return;
            if (rsp.getSwVer() != null) {
                deviceList.get(index).setSoftVer(rsp.getSwVer());
                deviceList.get(index).setHwVer(rsp.getHwVer());
                deviceList.get(index).setFpgaVer(rsp.getFpgaVer());

                if (mdwSettingFragment == null) {
                    return;
                }

                //更新版本信息
                if (mdwSettingFragment.getTv_hard_version() != null &&
                        mdwSettingFragment.getTv_login_version() != null &&
                        mdwSettingFragment.getTv_soft_version() != null) {
                    boolean newLine = false;
                    String hard_version = "";
                    String login_version = "";
                    StringBuffer soft_version = new StringBuffer();
                    for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                        if (newLine) {
                            soft_version.append("\n");
                        }
                        newLine = true;
                        soft_version.append(bean.getRsp().getDevName()).append("：\n").append(bean.getSoftVer());
                        if (hard_version.isEmpty()) hard_version = bean.getHwVer();
                        if (login_version.isEmpty()) login_version = bean.getFpgaVer();
                    }
                    mdwSettingFragment.getTv_hard_version().setText(hard_version);
                    mdwSettingFragment.getTv_login_version().setText(login_version);
                    mdwSettingFragment.getTv_soft_version().setText(soft_version.toString());
                }
            } else {
                refreshWorkState(index, GnbBean.DW_State.IDLE, getString(R.string.query_version_file));
            }
        }
    }

    @Override
    public void onDWStartTraceRsp(String id, GnbTraceRsp rsp) {
        AppLog.I("onStartTraceRsp():  id = " + id + ", rsp = " + rsp);
        if (rsp == null) return;
        mTraceCatchFragment.onStartTraceRsp(id, rsp);
    }

    @Override
    public void onDWStopTraceRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onStopTraceRsp():  id = " + id + ", rsp = " + rsp);
        mTraceCatchFragment.onStopTraceRsp(id, rsp);
    }

    @Override
    public void onDWStartControlRsp(String s, GnbTraceRsp gnbTraceRsp) {

    }

    @Override
    public void onDWStopControlRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onDWSetFtpRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onDWGetFtpRsp(String s, GnbFtpRsp gnbFtpRsp) {

    }

    @Override
    public void onDWStartCatchRsp(String id, GnbTraceRsp rsp) {
        AppLog.I("onStartCatchRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWStopCatchRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onStopCatchRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWSetBlackListRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetBlackListRsp():  id = " + id + ", rsp = " + rsp);
        mTraceCatchFragment.onSetBlackListRsp(id, rsp);
    }

    @Override
    public void onDWSetGnbRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetGnbRsp():  id = " + id + ", rsp = " + rsp);
        mTraceCatchFragment.onSetGnbRsp(id, rsp);
    }

    @Override
    public void onDWSetTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetTxPwrOffsetRsp():  id = " + id + ", rsp = " + rsp);
        mTraceCatchFragment.onsetDWTxPwrOffsetRsp(id, rsp);
    }

    @Override
    public void onDWSetNvTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetNvTxPwrOffsetRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWSetTimeRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetTimeRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWRebootRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetRebootRsp():  id = " + id + ", rsp = " + rsp);
        if (mdwSettingFragment != null)
            mdwSettingFragment.onSetRebootRsp(id, rsp);
    }

    @Override
    public void onDWUpgradeRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onFirmwareUpgradeRsp():  id = " + id + ", rsp = " + rsp);
        if (mdwSettingFragment != null)
            mdwSettingFragment.onFirmwareUpgradeRsp(id, rsp);
    }

    @Override
    public void onDWGetCellCfg(String s, JSONObject jsonObject) {
        AppLog.I("onDWGetCellCfg():  id = " + s + ", jsonObject = " + jsonObject);
    }

    @Override
    public void onDWRedirectUeCfg(String s, GnbCmdRsp gnbCmdRsp) {
        AppLog.I("onDWGetCellCfg():  id = " + s + ", gnbCmdRsp = " + gnbCmdRsp);

    }


    @Override
    public void onDWSetWifiInfoRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetWifiInfoRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWGetLogRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onGetLogRsp():  id = " + id + ", rsp = " + rsp);
        if (mdwSettingFragment != null)
            mdwSettingFragment.onGetLogRsp(id, rsp);
    }

    @Override
    public void onDWGetOpLogRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onGetOpLogRsp(): id " + id + "  " + rsp);
        if (mdwSettingFragment != null)
            mdwSettingFragment.onGetOpLogRsp(id, rsp);
    }

    @Override
    public void onDWWriteOpLogRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onWriteOpLogRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWDeleteOpLogRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onDeleteOpLogRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWSetBtNameRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetBtNameRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWSetMethIpRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetMethIpRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWGetMethIpRsp(String id, GnbMethIpRsp rsp) {
        AppLog.I("onGetMethIpRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWSetPaGpioRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetPaGpioRsp():  id = " + id + ", rsp = " + rsp);
        if (rsp != null) {
            int logicIndex = DeviceUtil.getLogicIndexById(id);
            if (rsp.getCmdType() == DWProtocol.OAM_MSG_SET_GPIO_MODE) {
                if (rsp.getRspValue() != DWProtocol.OAM_ACK_OK) {
                    showRemindDialog(getString(R.string.warning), "板" + (logicIndex + 1) + getString(R.string.pa_cfg_fail));
                }
            }
        }
    }

    @Override
    public void onDWGetPaGpioRsp(String id, GnbGpioRsp rsp) {
        AppLog.I("onGetPaGpioRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWGetSysInfoRsp(String id, GnbGetSysInfoRsp rsp) {
        AppLog.I("onGetSysInfoRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWSetDualCellRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetDualCellRsp():  id = " + id + ", rsp = " + rsp);
        //mDWSettingFragment.onSetDualCellRsp(id, rsp);
    }

    @Override
    public void onDWSetRxGainRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetRxGainRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWGetCatchCfg(String id, GnbCatchCfgRsp rsp) {
        AppLog.I("onGetCatchCfg():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWSetGpsRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetGpsRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWGetSysLogRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onGetSysLogRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWSetFanSpeedRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetFanSpeedRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWSetFanAutoSpeedRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetFanAutoSpeedRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWSetJamArfcn(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetJamArfcn():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onDWFreqScanRsp(String id, GnbFreqScanRsp rsp) {
        AppLog.I("onDWFreqScanRsp():  id = " + id + ", rsp = " + rsp);
        //如果是自动模式走mCfgTraceChildFragment的onFreqScanRsp
        if (mTraceCatchFragment.getmCfgTraceChildFragment().isAutoModeFreqRunning()) {
            mTraceCatchFragment.onFreqScanRsp(id, rsp);
        } else {
            mFreqFragment.onFreqScanRsp(id, rsp);
        }
    }

    @Override
    public void onDWFreqScanGetDocumentRsp(String id, GnbFreqScanGetDocumentRsp rsp) {
        AppLog.I("onFreqScanGetDocumentRsp():  id = " + id + ", rsp = " + rsp);
        //mFreqFragment.onFreqScanGetDocumentRsp(id, rsp);
    }

    @Override
    public void onDWStopFreqScanRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onStopFreqScanRsp():  id = " + id + ", rsp = " + rsp);
        mFreqFragment.onStopFreqScanRsp(id, rsp);
    }

    @Override
    public void onDWStartTdMeasure(String id, GnbCmdRsp rsp) {
        AppLog.I("onStartTdMeasure():  id = " + id + ", rsp = " + rsp);
        if (mdwSettingFragment != null)
            mdwSettingFragment.onStartTdMeasure(id, rsp);
    }

    @Override
    public void onDWSetDevNameRsp(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onDWGetGpsRsp(String s, GnbGpsRsp gnbGpsRsp) {

    }

    @Override
    public void onDWSetForwardUdpMsg(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onDWSetGpsInOut(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onDWGetGpsInOut(String s, GnbGpsInOutRsp gnbGpsInOutRsp) {

    }

    @Override
    public void onDWStartBandScan(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onDWSetUserData(String s, GnbUserDataRsp gnbUserDataRsp) {

    }

    @Override
    public void onDWSetDataTo485(String s, GnbSetDataTo485Rsp gnbSetDataTo485Rsp) {
        AppLog.I("id" + s + "onDWSetDataTo485 " + gnbSetDataTo485Rsp.getData());
    }

    @Override
    public void onDWSetDataFwd(String s, GnbReadDataFwdRsp gnbReadDataFwdRsp) {
        if (gnbReadDataFwdRsp != null) {
            AppLog.I("id" + s + "onDWSetDataFwd " + gnbReadDataFwdRsp.getData());
        }
    }

    @Override
    public void onDWGetUserData(String s, GnbUserDataRsp gnbUserDataRsp) {

    }

    @Override
    public void onDWSetGpioTxRx(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void OnSocketStateChange(String id, int lastState, int newState) {
        AppLog.I("onSocketStateChange():  id = " + id + ", lastState = " + lastState + ", newState = " + newState);

        if (id.equals(DBViewModel.getDeviceId())) {
            DBViewModel.OnSocketStateChange(id, lastState, newState);
            updateDBConnectState(-1, newState);
            return;
        }

        int index = DeviceUtil.getIndexById(id);
        int logicIndex = DeviceUtil.getLogicIndexById(id);
        if (index == -1) return;

        if (newState == ConnectProtocol.SOCKET.STATE_CONNECTING) {
            updateConnectState(logicIndex, 101);
//            updateSteps(type, StepBean.State.success, getString(R.string.connecting_dev));
        } else if (newState == ConnectProtocol.SOCKET.STATE_CONNECTED) {
            mHandler.removeMessages(1);
            mHandler.removeMessages(2);

            updateConnectState(logicIndex, 103);
//            updateSteps(type, StepBean.State.success, getString(R.string.connected_dev));
            String trace_str = getString(R.string.traceing);
            if (MessageController.build().isTracing(id, DWProtocol.CellId.FIRST)) {
                updateLeftTvState(0, 100, logicIndex, 0, trace_str, false);
                mTraceCatchFragment.setBtnStr(true);
            }
            if (MessageController.build().isTracing(id, DWProtocol.CellId.SECOND)) {
                updateLeftTvState(0, 100, logicIndex, 1, trace_str, false);
                mTraceCatchFragment.setBtnStr(true);
            }
            if (MessageController.build().isTracing(id, DWProtocol.CellId.THIRD)) {
                updateLeftTvState(0, 100, logicIndex, 2, trace_str, false);
                mTraceCatchFragment.setBtnStr(true);
            }
            if (MessageController.build().isTracing(id, DWProtocol.CellId.FOURTH)) {
                updateLeftTvState(0, 100, logicIndex, 3, trace_str, false);
                mTraceCatchFragment.setBtnStr(true);
            }
            mTraceCatchFragment.resetArfcnList();
            MessageController.build().setDWTime(id);
            new Handler().postDelayed(() -> {
                MessageController.build().getDWVersion(id); // 读取一次版本
            }, 300);
            //刷新按钮
            mTraceCatchFragment.refreshTraceBtn();
        } else {
            updateConnectState(logicIndex, 100);
            //deviceList.get(index).getTraceUtil().setWorkState(DWProtocol.CellId.FIRST, GnbBean.DW_State.IDLE);
            //deviceList.get(index).getTraceUtil().setWorkState(DWProtocol.CellId.SECOND, GnbBean.DW_State.IDLE);
            if (deviceList.size() == 0 || deviceList.size() == 1)
                mTraceCatchFragment.setBtnStr(false);
            else {
                if (tv_first_state.getText().equals(getString(R.string.not_ready))
                        && tv_second_state.getText().equals(getString(R.string.not_ready))
                        && tv_third_state.getText().equals(getString(R.string.not_ready))
                        && tv_forth_state.getText().equals(getString(R.string.not_ready))
                        && tv_fifth_state.getText().equals(getString(R.string.not_ready))
                        && tv_sixth_state.getText().equals(getString(R.string.not_ready))
                        && tv_seventh_state.getText().equals(getString(R.string.not_ready))
                        && tv_eighth_state.getText().equals(getString(R.string.not_ready))
                        && tv_ninth_state.getText().equals(getString(R.string.not_ready))
                        && tv_tenth_state.getText().equals(getString(R.string.not_ready))
                        && tv_eleventh_state.getText().equals(getString(R.string.not_ready))
                        && tv_twelfth_state.getText().equals(getString(R.string.not_ready)))
                    mTraceCatchFragment.setBtnStr(false);
                else {
                    for (DeviceInfoBean bean : deviceList) {
                        if (bean.getWorkState() == GnbBean.DW_State.TRACE) {
                            mTraceCatchFragment.setBtnStr(true);
                            break;
                        }
                    }
                }
            }
            if (mdwSettingFragment != null)
                mdwSettingFragment.setDeviceDis(id);
            String dis_str = getString(R.string.disconnect_dev);
            updateLeftTvState(0, 0, logicIndex, -1, getString(R.string.not_ready), false);
            setSyncStateStr(logicIndex, -1, "失步");
            temp = 0d;
            vol_warn = "";
            setLeftTvSync();
            vol = 0d;
            Util.showToast(dis_str);
            Message message = new Message();
            message.obj = id;
            mHandler.sendMessageDelayed(message, 60 * 1000);
        }
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
                mInInDialog.setOnCancelListener(dialog -> {
                    closeCustomDialog();
                });
                return;
            }
            //创建第二层Dialog
            mInDialog = new Dialog(this, style_dialog);
            mInDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mInDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
            mInDialog.setCancelable(true);   // 返回键不消失
            mInDialog.setOnCancelListener(dialog -> {
                closeCustomDialog();
            });
            return;
        }
        //创建最外层Dialog
        mDialog = new Dialog(this, style_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mDialog.setCancelable(true);   // 返回键不消失
        mDialog.setOnCancelListener(dialog -> {
            closeCustomDialog();
        });
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
            StatusBarUtil.setStatusBarColor(window, R.color.main_status_bar_color);
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
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });

        showCustomDialog(view, false);
    }


    // ****************************************弹框方法结束*******************************************

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

    public int getIndexById(String id) {
        for (int i = 0; i < deviceList.size(); i++) {
            if (deviceList.get(i).getRsp().getDeviceId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public void deleteDev(String id) {
        for (int i = deviceList.size() - 1; i >= 0; i--) {
            if (deviceList.get(i).getRsp().getDeviceId().equals(id)) {
                int logicIndex = DeviceUtil.getLogicIndexById(id);
                deviceList.remove(i);
//                updateSteps(type, StepBean.State.success, getString(R.string.stop_work));
                String idle_str = getString(R.string.idle);
                updateLeftTvState(0, 0, logicIndex, -1, idle_str, false);
            }
        }
    }


    public ArrayList<ScanArfcnBean> getScanArfcnBeanList() {
        return mFreqFragment.getScanArfcnBeanList();
    }


    public void setIfDebug(boolean isDebug) {
        ifDebug = isDebug;
        mTraceCatchFragment.setIfDebug(isDebug);
        mFreqFragment.setIfDebug(isDebug);
        btn_state_test.setVisibility(isDebug ? View.VISIBLE : View.GONE);
        mTraceCatchFragment.getmCfgTraceChildFragment().setIfDebug(isDebug);
        mdwSettingFragment.setIfDebug(isDebug);
        mCxSettingFragment.setIfDebug(isDebug);

    }

    public List<CheckBoxBean> getCheck_box_list() {
        return check_box_list;
    }

    public void setbtn_testVisibility(boolean isVisible) {
        if (btn_test == null) return;
        btn_test.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public DWSettingFragment getMdwSettingFragment() {
        return mdwSettingFragment;
    }

    public void setMdwSettingFragment(DWSettingFragment mdwSettingFragment) {
        this.mdwSettingFragment = mdwSettingFragment;
    }

    public void setMcxSettingFragment(CXSettingFragment mcxSettingFragment) {
        this.mCxSettingFragment = mcxSettingFragment;
    }

    public LinkedList<ScanArfcnBean> getFreqList() {
        return freqList;
    }


}