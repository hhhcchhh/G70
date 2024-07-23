package com.simdo.g73cs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
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
import com.nr.Gnb.Response.GnbSetDataTo485Rsp;
import com.nr.Gnb.Response.GnbSetFuncRsp;
import com.nr.Gnb.Response.GnbStateRsp;
import com.nr.Gnb.Response.GnbTraceRsp;
import com.nr.Gnb.Response.GnbUserDataRsp;
import com.nr.Gnb.Response.GnbVersionRsp;
import com.nr.NrSdk;
import com.nr.Socket.Bean.TracePara;
import com.nr.Socket.ConnectProtocol;
import com.nr.Socket.MessageControl.MessageController;
import com.nr.Socket.OnSocketChangeListener;
import com.nr.Socket.ZTcpService;
import com.simdo.g73cs.Adapter.FragmentAdapter;
import com.simdo.g73cs.Adapter.FreqResultListAdapter;
import com.simdo.g73cs.Bean.ArfcnBean;
import com.simdo.g73cs.Bean.DataUpBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.Dialog.LoginDialog;
import com.simdo.g73cs.Fragment.SettingFragment;
import com.simdo.g73cs.Fragment.TraceCatchFragment;
import com.simdo.g73cs.SlideMenu.SlideLeftFragment;
import com.simdo.g73cs.SlideMenu.SlidingMenu;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DateUtil;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PermissionsUtil;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.TextTTS;
import com.simdo.g73cs.Util.TraceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnSocketChangeListener, MessageController.OnSetGnbListener,
        FTPUtil.OnFtpListener, SlideLeftFragment.OnSlideLeftMenuListener, MessageController.OnSetGnbIn {

    private static MainActivity instance;
    public int showMinRsrp;
    private Context mContext;
    public Activity mActivity;
    private final List<DeviceInfoBean> deviceList = new ArrayList<>(); // 存储已连接的设备
    private final List<MyUeidBean> blackList = new ArrayList<>(); // 存储白名单
    public LinkedList<ScanArfcnBean> freqList = new LinkedList<>();
    public static String devA = "";
    public static String devB = "G758_B";
    public boolean isUseDefault = true;
    public boolean isVibrate = false;
    public String connectMode;
    public boolean isEnablePci = true;
    public boolean isUpFTP = false;
    public boolean isShowAllImsi = false;

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //StatusBarUtil.setLightStatusBar(this, true);
        //保持屏幕唤醒
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(getResources().getColor(R.color.main_bg_color));
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setBackgroundDrawableResource(R.drawable.gradient_status_bar);

        instance = this;
        mContext = this;
        mActivity = this;
        /*String dir = getExternalFilesDir(null).getPath();
        FileUtil.build().changeLogDir(dir);
        NrSdk.build().changeLogDir(dir);*/
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) list.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
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
        if (requestCode == PermissionsUtil.getInstance().mRequestCode){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) PermissionsUtil.getInstance().showSystemPermissionsSettingDialog(mActivity);
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
            Toast.makeText(getApplicationContext(), "拒绝权限，无法提供服务!", Toast.LENGTH_SHORT).show();
            finish();
        }
    };
    LoginDialog mLoginDialog;
    private void startActivity() {
        AppLog.createLogFile("+++ ON CREATE +++");
        initView();   // 初始化视图以及监听事件
        initSDK(); // 先绑定服务
        initObject(); // 初始化对象以及数据
        initSlideMenu();
        // 弹出登录界面
        mLoginDialog = new LoginDialog(mContext);
        mLoginDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mLoginDialog = null;
            }
        });
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
        MessageController.build().setOnSetGnbIn(this);
        FTPUtil.build().setFtpListener(this);
    }

    TextView tv_cell_1_nr, tv_cell_2_nr, tv_cell_1_lte, tv_cell_2_lte;
    ImageView iv_cell_1_nr, iv_cell_2_nr, iv_cell_1_lte, iv_cell_2_lte;

    FragmentAdapter fragmentAdapter;
    TraceCatchFragment mTraceCatchFragment;
    SettingFragment mSettingFragment;

    private void initView() {

        iv_cell_1_nr = findViewById(R.id.iv_cell_1_nr);
        iv_cell_1_nr.setImageResource(R.mipmap.icon_home_not2_signal1);

        iv_cell_2_nr = findViewById(R.id.iv_cell_2_nr);
        iv_cell_2_nr.setImageResource(R.mipmap.icon_home_not2_signal2);

        iv_cell_1_lte = findViewById(R.id.iv_cell_1_lte);
        iv_cell_1_lte.setImageResource(R.mipmap.icon_home_not2_signal3);

        iv_cell_2_lte = findViewById(R.id.iv_cell_2_lte);
        iv_cell_2_lte.setImageResource(R.mipmap.icon_home_not2_signal4);

        tv_cell_1_nr = findViewById(R.id.tv_cell_1_nr);
        tv_cell_2_nr = findViewById(R.id.tv_cell_2_nr);
        tv_cell_1_lte = findViewById(R.id.tv_cell_1_lte);
        tv_cell_2_lte = findViewById(R.id.tv_cell_2_lte);

        View.OnClickListener listener = new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.iv_cell_1_nr:
                    case R.id.tv_cell_1_nr:
                        clickCellImg(0);
                        break;
                    case R.id.iv_cell_2_nr:
                    case R.id.tv_cell_2_nr:
                        clickCellImg(1);
                        break;
                    case R.id.iv_cell_1_lte:
                    case R.id.tv_cell_1_lte:
                        clickCellImg(2);
                        break;
                }
            }
        };

        iv_cell_1_nr.setOnClickListener(listener);
        iv_cell_2_nr.setOnClickListener(listener);
        iv_cell_1_lte.setOnClickListener(listener);
        tv_cell_1_nr.setOnClickListener(listener);
        tv_cell_2_nr.setOnClickListener(listener);
        tv_cell_1_lte.setOnClickListener(listener);

        initProgress(0);

        // 加载底部导航标签
        List<Fragment> fragmentList = new ArrayList<>();
        mTraceCatchFragment = new TraceCatchFragment(mContext);
        //mDataFragment = new DataFragment(mContext);
        mSettingFragment = new SettingFragment(mContext, mTraceCatchFragment);
        fragmentList.add(mTraceCatchFragment);
        //fragmentList.add(mDataFragment);
        fragmentList.add(mSettingFragment);

        String[] titles = new String[]{"搜寻", "设置"};
        int[] icons = new int[]{R.drawable.location_tab_icon, R.drawable.setting_tab_icon};
        int[] colors = new int[]{Color.parseColor("#4367F1"), Color.parseColor("#8F9399")}; //选中颜色  正常颜色

        int[][] states = new int[2][]; //状态
        states[0] = new int[]{android.R.attr.state_selected}; //选中
        states[1] = new int[]{}; //默认
        ColorStateList colorList = new ColorStateList(states, colors);

        ViewPager2 view_pager = findViewById(R.id.view_pager);
        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), getLifecycle(), fragmentList);
        view_pager.setAdapter(fragmentAdapter);
        view_pager.setCurrentItem(0);
        view_pager.setUserInputEnabled(false);
        TabLayout tab_layout = findViewById(R.id.tab_layout);
        tab_layout.setTabRippleColorResource(R.color.trans);
        TabLayoutMediator tab = new TabLayoutMediator(tab_layout, view_pager, new TabLayoutMediator.TabConfigurationStrategy() {
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

    final long[] clickTime = {0, 0, 0};
    final int[] clickCount = {0, 0, 0};
    private void clickCellImg(int index){
        if (clickTime[index] + 2000 > System.currentTimeMillis()) {
            clickTime[index] = System.currentTimeMillis();
            clickCount[index]++;
            if (clickCount[index] > 4) {
                if (deviceList.size() <= 0) {
                    MainActivity.getInstance().showToast("设备不在线，请先开启并连接设备");
                    return;
                }
                PrefUtil.build().putValue("func_cfg", "5612;" + index + ";");
                MessageController.build().setFuncCfg(deviceList.get(0).getRsp().getDeviceId(), 5612, index, "", 0);
                clickCount[index] = 0;
            }
        } else {
            clickTime[index] = System.currentTimeMillis();
            clickCount[index] = 1;
        }
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
        if (PrefUtil.build().getValue("freq_N41", "-1").equals("-1")){
            // N41 504990、 512910、 516990、 507150、 525630
            ArrayList<ArfcnBean> listN41 = new ArrayList<>();
            listN41.add(new ArfcnBean(504990, true));
            listN41.add(new ArfcnBean(512910, true));
            listN41.add(new ArfcnBean(516990, true));
            listN41.add(new ArfcnBean(507150, false));
            listN41.add(new ArfcnBean(525630, false));
            PrefUtil.build().putFreqArfcnList("freq_N41", listN41);

            // N1 427250、422890、 428910、 426030
            ArrayList<ArfcnBean> listN1 = new ArrayList<>();
            listN1.add(new ArfcnBean(427250, true));
            listN1.add(new ArfcnBean(422890, true));
            listN1.add(new ArfcnBean(428910, true));
            listN1.add(new ArfcnBean(427210, true));
            listN1.add(new ArfcnBean(426750, true));
            listN1.add(new ArfcnBean(422930, true));
            listN1.add(new ArfcnBean(426030, false));
            PrefUtil.build().putFreqArfcnList("freq_N1", listN1);

            // N78 627264、 633984
            ArrayList<ArfcnBean> listN78 = new ArrayList<>();
            listN78.add(new ArfcnBean(627264, true));
            listN78.add(new ArfcnBean(633984, true));
            PrefUtil.build().putFreqArfcnList("freq_N78", listN78);

            // N28 154810、 152650、 152890、 156970、 154570、 156490、 155770
            ArrayList<ArfcnBean> listN28 = new ArrayList<>();
            listN28.add(new ArfcnBean(154810, true));
            listN28.add(new ArfcnBean(152650, true));
            listN28.add(new ArfcnBean(152890, true));
            listN28.add(new ArfcnBean(156970, true));
            listN28.add(new ArfcnBean(154570, false));
            listN28.add(new ArfcnBean(156490, false));
            listN28.add(new ArfcnBean(155770, false));
            PrefUtil.build().putFreqArfcnList("freq_N28", listN28);

            // N79 723360
            ArrayList<ArfcnBean> listN79 = new ArrayList<>();
            listN79.add(new ArfcnBean(723360, true));
            PrefUtil.build().putFreqArfcnList("freq_N79", listN79);

            // B3 1300、 1275、 1650、 1506、 1500、 1531、 1524、 1850
            ArrayList<ArfcnBean> listB3 = new ArrayList<>();
            listB3.add(new ArfcnBean(1650, true));
            listB3.add(new ArfcnBean(1300, true));
            listB3.add(new ArfcnBean(1350, true));
            listB3.add(new ArfcnBean(1275, true));
            listB3.add(new ArfcnBean(1506, true));
            listB3.add(new ArfcnBean(1500, false));
            listB3.add(new ArfcnBean(1531, true));
            listB3.add(new ArfcnBean(1524, false));
            listB3.add(new ArfcnBean(1825, true));
            listB3.add(new ArfcnBean(1850, true));
            PrefUtil.build().putFreqArfcnList("freq_B3", listB3);

            // 4G 常见频点
            // B1  350、 375、 400、 450、 500、 100
            ArrayList<ArfcnBean> listB1 = new ArrayList<>();
            listB1.add(new ArfcnBean(100, true));
            listB1.add(new ArfcnBean(50, true));
            listB1.add(new ArfcnBean(300, true));
            listB1.add(new ArfcnBean(350, false));
            listB1.add(new ArfcnBean(375, false));
            listB1.add(new ArfcnBean(400, false));
            listB1.add(new ArfcnBean(450, false));
            listB1.add(new ArfcnBean(500, false));
            PrefUtil.build().putFreqArfcnList("freq_B1", listB1);

            // B5 2452
            ArrayList<ArfcnBean> listB5 = new ArrayList<>();
            listB5.add(new ArfcnBean(2452, true));
            PrefUtil.build().putFreqArfcnList("freq_B5", listB5);

            // B8 3682、 3683、 3641、 3621、 3590、 3725、 3768、 3769、 3770、 3775
            ArrayList<ArfcnBean> listB8 = new ArrayList<>();
            listB8.add(new ArfcnBean(3682, false));
            listB8.add(new ArfcnBean(3683, true));
            listB8.add(new ArfcnBean(3641, false));
            listB8.add(new ArfcnBean(3621, false));
            listB8.add(new ArfcnBean(3590, true));
            listB8.add(new ArfcnBean(3725, false));
            listB8.add(new ArfcnBean(3768, false));
            listB8.add(new ArfcnBean(3769, false));
            listB8.add(new ArfcnBean(3770, false));
            listB8.add(new ArfcnBean(3775, false));
            listB8.add(new ArfcnBean(3745, false));
            listB8.add(new ArfcnBean(3710, false));
            listB8.add(new ArfcnBean(3737, false));
            PrefUtil.build().putFreqArfcnList("freq_B8", listB8);

            // B40 38950、 39148、 39292、 38750
            ArrayList<ArfcnBean> listB40 = new ArrayList<>();
            listB40.add(new ArfcnBean(38950, true));
            listB40.add(new ArfcnBean(39148, true));
            listB40.add(new ArfcnBean(39292, true));
            listB40.add(new ArfcnBean(38750, true));
            PrefUtil.build().putFreqArfcnList("freq_B40", listB40);

            // B34 36275
            ArrayList<ArfcnBean> listB34 = new ArrayList<>();
            listB34.add(new ArfcnBean(36275, true));
            PrefUtil.build().putFreqArfcnList("freq_B34", listB34);

            // B38 37900、 38098
            /*ArrayList<ArfcnBean> listB38 = new ArrayList<>();
            listB38.add(new ArfcnBean(37900, false));
            listB38.add(new ArfcnBean(38098, false));
            PrefUtil.build().putFreqArfcnList("freq_B38", listB38);*/

            // B39 38400、 38544
            ArrayList<ArfcnBean> listB39 = new ArrayList<>();
            listB39.add(new ArfcnBean(38400, true));
            listB39.add(new ArfcnBean(38544, true));
            PrefUtil.build().putFreqArfcnList("freq_B39", listB39);

            // B41 40936、 40340
            ArrayList<ArfcnBean> listB41 = new ArrayList<>();
            listB41.add(new ArfcnBean(40936, true));
            listB41.add(new ArfcnBean(40340, true));
            listB41.add(new ArfcnBean(41134, true));
            listB41.add(new ArfcnBean(41332, true));
            PrefUtil.build().putFreqArfcnList("freq_B41", listB41);
        }
        showMinRsrp = Integer.parseInt(PrefUtil.build().getValue("show_min_rsrp", "0").toString());
        connectMode = PrefUtil.build().getValue("connect_mode", "热点").toString();
        isVibrate = PrefUtil.build().getValue("is_vibrate", "1").toString().equals("1");
        TextTTS.build().initTTS(); // 初始化语音包
        GnbCity.build().init();

        String dataUp = PrefUtil.build().getValue("data_up", "").toString();
        if (dataUp.isEmpty()) mDataUpBean = new DataUpBean();
        else {
            Gson gson = new Gson();
            mDataUpBean = gson.fromJson(dataUp, DataUpBean.class);
        }

        mDataUpList = new ArrayList<>();
    }
    public DataUpBean mDataUpBean;
    public ArrayList<String> mDataUpList;

    /**
     * 初始化左右滑动菜单
     */
    SlideLeftFragment mSlideLeftFrag;
    public SlidingMenu menu;
    private void initSlideMenu(){
        /*mSlideLeftFrag = new SlideLeftFragment();
        mSlideLeftFrag.setOnSlideLeftMenuListener(this);
        //setBehindContentView(R.layout.slide_left_menu_frame);
        getSupportFragmentManager().beginTransaction() .replace(R.id.id_left_menu_frame, mSlideLeftFrag).commit();
        //final SlidingMenu menu = getSlidingMenu();
        final SlidingMenu menu = new SlidingMenu(mActivity);
        menu.setMode(SlidingMenu.LEFT_RIGHT);
        // 设置触摸屏幕的模式
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow_left);
        // 设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        // 设置渐入渐出效果的值
        menu.setFadeDegree(0.35f);*/

        menu = new SlidingMenu(this);
        // 设置触摸屏幕的模式
        menu.setTouchModeBehind(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(getDrawable(R.drawable.shadow_left));
        // 设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setSecondaryShadowDrawable(getDrawable(R.drawable.shadow_left));//右侧菜单的阴影图片
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.slide_left_menu_frame);
        menu.setOffsetFadeDegree(0.4f);// 设置主界面剩余部分的透明的
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.id_left_menu_frame, mSlideLeftFrag = new SlideLeftFragment()).commit();
    }

    /**
     * 退出应用对SDK包处理
     */
    private void closeSDK() {
        MessageController.build().removeOnSetGnbListener();
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
            showToast("再次点击返回键退出程序");
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
                //android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        }, 100);
    }

    // 》》》》》》》》》》》》》》》》》》》》》》》供Fragment 调用方法开始》》》》》》》》》》》》》》》》》》》》》》

    /**
     * 初始化侦码数量
     */
    public void initCatchCount() {
        mSlideLeftFrag.initCatchCount();
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
        mSlideLeftFrag.updateCatchCount(all_count, telecom_count, mobile_count, unicom_count, sva_count);
    }

    public void initParam() {
        if (mSlideLeftFrag != null) mSlideLeftFrag.initParam();
    }

    public void initState() {
        if (mSlideLeftFrag != null) mSlideLeftFrag.initState();
    }

    public void updateParam(int cell_id, String arfcn, String pci) {
        mSlideLeftFrag.updateParam(cell_id, arfcn, pci);
    }

    /**
     * 更新连接状态
     *
     * @param type  0 NR   1 LTE
     * @param state 101 连接中  103 已连接  100 未连接
     */
    public void updateConnectState(int type, int state) {
        AppLog.I("updateConnectState type = " + type + ", state = " + state);
        if (mLoginDialog != null) mLoginDialog.updateConnectState(type, state);
        int imageId1;
        int imageId2;

        if (state == ConnectProtocol.SOCKET.STATE_CONNECTED) {
            imageId1 = type == 0 ? R.mipmap.icon_home_signal1 : R.mipmap.icon_home_signal3;
            imageId2 = type == 0 ? R.mipmap.icon_home_signal2 : R.mipmap.icon_home_signal4;
            updateProgress(type, 0, 0, "空闲", false);
            updateProgress(type, 0, 1, "空闲", false);
            updateProgress(type, 0, 2, "空闲", false);
            updateProgress(type, 0, 3, "空闲", false);
        } else {
            imageId1 = type == 0 ? R.mipmap.icon_home_not2_signal1 : R.mipmap.icon_home_not2_signal3;
            imageId2 = type == 0 ? R.mipmap.icon_home_not2_signal2 : R.mipmap.icon_home_not2_signal4;
            updateProgress(type, 0, 0, "未就绪", false);
            updateProgress(type, 0, 1, "未就绪", false);
            updateProgress(type, 0, 2, "未就绪", false);
            updateProgress(type, 0, 3, "未就绪", false);
            initParam();
            initState();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (type == 0) {
                    iv_cell_1_nr.setImageResource(imageId1);
                    iv_cell_2_nr.setImageResource(imageId2);
                } else {
                    iv_cell_1_lte.setImageResource(imageId1);
                    iv_cell_2_lte.setImageResource(imageId2);
                }
            }
        });
    }

    /**
     * 初始化工作加载进度条
     *
     * @param type 0 NR   1 LTE
     */
    public void initProgress(int type) {
        updateProgress(type, 0, 0, "未就绪", false);
        updateProgress(type, 0, 1, "未就绪", false);
        updateProgress(type, 0, 2, "未就绪", false);
        updateProgress(type, 0, 3, "未就绪", false);
    }

    /**
     * 更新工作加载进度条
     *
     * @param type    type 0 NR   1 LTE
     * @param pro     进度值
     * @param cell_id 小区id
     */
    public void updateProgress(int type, int pro, int cell_id, String info, boolean isFail) {
        int color = isFail ? ContextCompat.getColor(mContext, R.color.color_e65c5c) : ContextCompat.getColor(mContext, R.color.white);
        if (type == 0) {
            if (cell_id == 0) {
                tv_cell_1_nr.setText(info);
                tv_cell_1_nr.setTextColor(color);
            } else if (cell_id == 1){
                tv_cell_2_nr.setText(info);
                tv_cell_2_nr.setTextColor(color);
            } else if (cell_id == 2) {
                tv_cell_1_lte.setText(info);
                tv_cell_1_lte.setTextColor(color);
            } else {
                tv_cell_2_lte.setText(info);
                tv_cell_2_lte.setTextColor(color);
            }
        } else {

        }
    }

    public void vibrate(String imsiEnd) {
        if (isVibrate){
            Vibrator vib = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);
            vib.vibrate(1000);
            TextTTS.build().play("发现苹果目标" + imsiEnd, false);
            AppLog.I("vibrate and say 发现苹果目标 = " + imsiEnd);
        }
    }

    // 《《《《《《《《《《《《《《《《《《《《《《《供Fragment 调用方法结束《《《《《《《《《《《《《《《《《《《《《《


    // **************************************更新UI方法开始*******************************************
    private void refreshHeartInfo(String id, int index) {
        AppLog.I("refreshUiInfo(): update id = " + id + ", index = " + index);

        int firstState = deviceList.get(index).getRsp().getFirstState();
        int secondState = deviceList.get(index).getRsp().getSecondState();
        if (deviceList.get(index).getRsp().getDevState() == GnbStateRsp.devState.ABNORMAL) {
            showToast("设备异常，须掉电重启");
            return;
            /*if (firstState != GnbBean.State.TRACE && secondState != GnbBean.State.TRACE) {
                // 非定位状态走这里
                // 定位状态下，走GNB_STATE_PHY_ABNORMAL
                refreshWorkState(index, GnbBean.State.NONE, "设备异常，须掉电重启");
                return;
            }*/
        }
        if (firstState == GnbStateRsp.gnbState.GNB_STATE_PHY_ABNORMAL) {
            refreshWorkState(index, GnbBean.State.NONE, "PHY异常，请结束定位后重试");
        }
        if (secondState == GnbStateRsp.gnbState.GNB_STATE_PHY_ABNORMAL) {
            refreshWorkState(index, GnbBean.State.NONE, "PHY异常，请结束定位后重试");
        }
    }

    private void refreshWorkState(int index, int state, String stateStr) {

        deviceList.get(index).setWorkState(state); // 更新工作状态

        String deviceName = deviceList.get(index).getRsp().getDevName();

        if (deviceName.contains(devA)) {
            if (state == GnbBean.State.NONE)
                updateSteps(0, StepBean.State.fail, stateStr);
        } else if (deviceName.contains(devB)) {
            if (state == GnbBean.State.NONE)
                updateSteps(1, StepBean.State.fail, stateStr);
        }
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
    public void OnFtpGetFileProcess(String id, long process) {
        AppLog.I("OnFtpGetFileProcess(): id = " + id + ", process = " + process);
    }

    @Override
    public void onHeartStateRsp(GnbStateRsp rsp) {
        AppLog.I("onHeartStateRsp():  rsp = " + rsp);
        if (rsp == null) return;
        String typeStr = "";
        if (rsp.getDevName().contains(devA)) typeStr = devA;
        else if (rsp.getDevName().contains(devB)) typeStr = devB;

        //if (typeStr.isEmpty()) return; // 既不是NR 也不是 LTE，不往下走

        for (DeviceInfoBean bean : deviceList) {
            if (bean.getRsp().getDevName().contains(typeStr) && !bean.getRsp().getDevName().equals(rsp.getDevName()))
                return; // 已经包含是NR 或 LTE，又来另外的NR 或 LTE，不往下走
        }
        int type = rsp.getDevName().contains(devA) ? 0 : 1;
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
        if (isAdd && deviceList.size() < 2) { // 是否为新数据，且当前已连接的设备未超过2个

            DeviceInfoBean deviceInfoBean = new DeviceInfoBean();
            deviceInfoBean.setRsp(rsp);

            deviceList.add(deviceInfoBean);

            int index = deviceList.size() - 1;
            refreshHeartInfo(id, index); // 刷新UI信息
            MessageController.build().setGnbTime(id); // 初次，需同步单板时间
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    MessageController.build().setOnlyCmd(id, GnbProtocol.UI_2_gNB_QUERY_gNB_VERSION); // 初次，读取一次版本
                }
            },200);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    MessageController.build().setOnlyCmd(id, GnbProtocol.OAM_MSG_GET_SYS_INFO); // 断线、重启，再读取一次license
                }
            },400);
            updateConnectState(0, 103);
            updateConnectState(1, 103);
            MainActivity.getInstance().updateSteps(type, StepBean.State.success, "已连接设备");

            if (GnbStateRsp.gnbState.GNB_STATE_CONTROL == deviceList.get(index).getRsp().getFirstState()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, "检测与上次状态信息不匹配");
                        updateSteps(0, StepBean.State.success, getString(R.string.recover_cell0_work));
                        MessageController.build().getCellCfg(id, GnbProtocol.CellId.FIRST, GnbProtocol.UI_2_gNB_CFG_gNB);
                        //MainActivity.getInstance().updateSteps(type, StepBean.State.success, "结束通道一的工作");
                        //MessageController.build().setCmdAndCellID(deviceList.get(index).getRsp().getDeviceId(), GnbProtocol.UI_2_gNB_STOP_CONTROL, GnbProtocol.CellId.FIRST);
                    }
                }, 600);
            }else if (GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == deviceList.get(index).getRsp().getFirstState()){
                updateProgress(type, 0, 0, "空闲", false);
            }
            if (GnbStateRsp.gnbState.GNB_STATE_CONTROL == deviceList.get(index).getRsp().getSecondState()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, "检测与上次状态信息不匹配");
                        updateSteps(0, StepBean.State.success, getString(R.string.recover_cell1_work));
                        MessageController.build().getCellCfg(id, GnbProtocol.CellId.SECOND, GnbProtocol.UI_2_eNB_CFG_gNB);
                        //MainActivity.getInstance().updateSteps(type, StepBean.State.success, "结束通道二的工作");
                        //MessageController.build().setCmdAndCellID(deviceList.get(index).getRsp().getDeviceId(), GnbProtocol.UI_2_eNB_STOP_CONTROL, GnbProtocol.CellId.SECOND);
                    }
                }, 800);
            }else if (GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == deviceList.get(index).getRsp().getSecondState()){
                updateProgress(type, 0, 1, "空闲", false);
            }

            if (GnbStateRsp.gnbState.GNB_STATE_CONTROL == deviceList.get(index).getRsp().getThirdState()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateSteps(type, StepBean.State.success, "检测与上次状态信息不匹配");
                        updateSteps(0, StepBean.State.success, getString(R.string.recover_cell2_work));
                        MessageController.build().getCellCfg(id, GnbProtocol.CellId.THIRD, GnbProtocol.UI_2_gNB_CFG_gNB);
                        //updateSteps(type, StepBean.State.success, "结束通道三的工作");
                        //MessageController.build().setCmdAndCellID(deviceList.get(index).getRsp().getDeviceId(), GnbProtocol.UI_2_gNB_STOP_CONTROL, GnbProtocol.CellId.THIRD);
                    }
                }, 1000);
            }else if (GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == deviceList.get(index).getRsp().getThirdState()){
                updateProgress(type, 0, 2, "空闲", false);
            }

            if (GnbStateRsp.gnbState.GNB_STATE_CONTROL == deviceList.get(index).getRsp().getFourthState()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateSteps(type, StepBean.State.success, "检测与上次状态信息不匹配");
                        updateSteps(0, StepBean.State.success, getString(R.string.recover_cell3_work));
                        MessageController.build().getCellCfg(id, GnbProtocol.CellId.FOURTH, GnbProtocol.UI_2_eNB_CFG_gNB);
                        //updateSteps(type, StepBean.State.success, "结束通道四的工作");
                        //MessageController.build().setCmdAndCellID(deviceList.get(index).getRsp().getDeviceId(), GnbProtocol.UI_2_eNB_STOP_CONTROL, GnbProtocol.CellId.FOURTH);
                    }
                }, 1200);
            }else if (GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == deviceList.get(index).getRsp().getFourthState()){
                updateProgress(type, 0, 3, "空闲", false);
            }
        }
        if (mSlideLeftFrag != null)
            mSlideLeftFrag.updateState(rsp.getVoltageList(), rsp.getTempList(), rsp.getGpsSyncState(), rsp.getFirstAirState(), rsp.getSecondAirState(), rsp.getThirdAirState(), rsp.getFourthAirState());
    }

    @Override
    public void onQueryVersionRsp(String id, GnbVersionRsp rsp) {
        AppLog.I("onQueryVersionRsp():  id = " + id + ", rsp = " + rsp);
        if (rsp != null) {
            int indexById = getIndexById(id);
            if (indexById == -1) return;
            if (rsp.getSwVer() != null) {

                deviceList.get(indexById).setSoftVer(rsp.getSwVer());
                deviceList.get(indexById).setHwVer(rsp.getHwVer());
                deviceList.get(indexById).setFpgaVer(rsp.getFpgaVer());
                String[] split = rsp.getSwVer().split("_");
                try {
                    if (split.length > 2 && Integer.parseInt(split[2]) >= 20231206) {
                        MessageController.build().setFuncCfg(id, 5612, 0, "", 1); //读Iphone模式
                        MessageController.build().setFuncCfg(id, 11, 0, "", 1); //读热点网线模式
                    }
                }catch (Exception e){
                    AppLog.I("onQueryVersionRsp(): err = " + e.getMessage());
                }
                /* StringBuilder sb = new StringBuilder();
                sb.append("硬件版本: ").append(rsp.getHwVer()).append("\r\n");
                sb.append("逻辑版本: ").append(rsp.getFpgaVer()).append("\r\n");
                sb.append("软件版本: ").append(rsp.getSwVer());
                showRemindDialog("单板一版本", sb.toString()); */
            } else {
                refreshWorkState(indexById, GnbBean.State.IDLE, "查询基带版本失败");
            }
        }
    }

    @Override
    public void onStartTraceRsp(String id, GnbTraceRsp rsp) {
        AppLog.I("onStartTraceRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onStopTraceRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onStopTraceRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onStartControlRsp(String id, GnbTraceRsp rsp) {
        //AppLog.I("onStartControlRsp():  id = " + id + ", rsp = " + rsp);
        //Log.d("onStartControlRsp", "start time = " + System.currentTimeMillis());
        if (rsp == null) return;
        mTraceCatchFragment.onStartControlRsp(id, rsp);
    }

    @Override
    public void onStopControlRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onStopControlRsp():  id = " + id + ", rsp = " + rsp);
        mTraceCatchFragment.onStopControlRsp(id, rsp);
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
    public void onSetLicRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetLicRsp():  id = " + id + ", rsp = " + rsp);
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.OAM_MSG_SET_LIC_INFO) {
                String msg = "设置授权时间失败";
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK){
                    msg = "设置授权时间成功";
                    MessageController.build().setOnlyCmd(id, GnbProtocol.OAM_MSG_GET_SYS_INFO); // 断线、重启，再读取一次license
                }
                showToast(msg);
            }
        }
    }

    @Override
    public void onSetDataTo485(String s, GnbSetDataTo485Rsp gnbSetDataTo485Rsp) {

    }

    @Override
    public void onGetCellCfg(String id, JSONObject jsonObject) {
        AppLog.I("onGetCellCfg():  id = " + id + ", jsonObject = " + jsonObject);
        try {
            int cell_id = jsonObject.getInt("cell_id");
            MessageController.build().recoverTacLoop(id, cell_id);
            showToast("通道" + (cell_id + 1) + "业务恢复中");
            mTraceCatchFragment.setBtnEnable(false);
            mHandler.removeMessages(999);
            mHandler.removeMessages(998);
            Message message = new Message();
            message.what = 999;
            message.obj = id;
            mHandler.sendMessageDelayed(message, 2000);
        } catch (JSONException e) {
            AppLog.I("onGetCellCfg() err, id = " + id + ", msg = " + e.getMessage());
        }
    }

    @Override
    public void onRedirectUeCfg(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onResetPlmnCfg(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetPerPwrCfg(String s, GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetPhoneTypeRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetPhoneTypeRsp():  id = " + id + ", rsp = " + rsp);
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_SET_PHONE_TYPE || rsp.getCmdType() == GnbProtocol.UI_2_eNB_SET_PHONE_TYPE) {
                String msg = "添加到iphone库成功";
                if (rsp.getRspValue() != GnbProtocol.OAM_ACK_OK) msg = "添加到iphone库失败";
                showToast(msg);
            }
        }
    }

    @Override
    public void onSetFuncCfgRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetFuncCfgRsp():  id = " + id + ", rsp = " + rsp);
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.OAM_MSG_SET_FUNC_CFG) {
                String func_cfg = PrefUtil.build().getValue("func_cfg", "").toString();
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    if (!func_cfg.isEmpty()){
                        String[] split = func_cfg.split(";");
                        switch (split[0]){
                            case "5612":
                                showToast(split[1]);
                                return;
                            case "5612+":
                                PrefUtil.build().putValue("iphone_mode", split[1]);
                                break;
                            case "11":
                                PrefUtil.build().putValue("connect_mode", split[1].equals("1") ? "网线" : "热点");
                                break;
                        }
                    }
                    rebootDialog();
                }else {
                    String msg = "设置失败";
                    showToast(msg);
                }
            }
        }
    }

    private void rebootDialog() {
        createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView title = view.findViewById(R.id.title);
        title.setText("设置成功,立即重启生效？");
        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < deviceList.size(); i++) {
                    String devName = deviceList.get(i).getRsp().getDevName();
                    int type = devName.contains(devA) ? 0 : 1;
                    int workState = deviceList.get(i).getWorkState();
                    if (workState != GnbBean.State.REBOOT && workState != GnbBean.State.NONE) {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, "正在配置重启");
                        MainActivity.getInstance().deviceList.get(i).setWorkState(GnbBean.State.REBOOT);
                        String id = deviceList.get(i).getRsp().getDeviceId();
                        MessageController.build().setOnlyCmd(id, GnbProtocol.UI_2_gNB_REBOOT_gNB);
                    }
                }

                closeCustomDialog();
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    @Override
    public void onGetFuncCfgRsp(String id, GnbSetFuncRsp rsp) {
        AppLog.I("onGetFuncCfgRsp():  id = " + id + ", rsp = " + rsp);
        mSettingFragment.onGetFuncCfgRsp(id, rsp);
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
        AppLog.I("onGetOpLogRsp():  id = " + id + ", rsp = " + rsp);
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
                if (rsp.getRspValue() != GnbProtocol.OAM_ACK_OK && rsp.getRspValue() != GnbProtocol.OAM_ACK_E_BUSY) {
                    showRemindDialog("警告", "PA控制IO口配置失败");
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
        if (rsp != null) {
            int indexById = getIndexById(id);
            if (indexById == -1) return;
            if (rsp.getLicense() != null) {
                isShowGuoQi = false;
                String license = rsp.getLicense();
                deviceList.get(indexById).setLicense(license);
                String date = "2099-12-31";
                String useHour = "0";
                String hour = "0";
                int hasNum = 9999;
                if (license.contains("EXPIRED")) {
                    String[] strings = license.split("\n");

                    for (String string : strings) {
                        if (string.contains("EXPIRED")) {
                            if (string.contains("date")) {
                                // 新版本
                                String[] expired = string.substring(string.indexOf("(") + 1).split(",");
                                String en = expired[0].trim().split(" ")[1];
                                if (en.equals("0")) break; // 不生效，就不用走下面逻辑
                                date = expired[1].trim().split(" ")[1];
                                if (date.equals("0")) date = "2099-12-31";
                                else
                                    date = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6);

                                int hourIndex = expired[2].trim().indexOf('/');
                                useHour = expired[2].trim().substring(5, hourIndex);
                                hour = expired[2].trim().substring(hourIndex + 1, expired[2].trim().length() - 1);

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                try {
                                    Date date0 = sdf.parse(date);
                                    Date date1 = sdf.parse(sdf.format(new Date()));
                                    long lDate1 = date0.getTime();
                                    long lDate2 = date1.getTime();
                                    long dateX = dateX(lDate1, lDate2);
                                    hasNum = (int) dateX;
                                    if (lDate1 < lDate2) hasNum = 0;

                                    if (hasNum > 3){
                                        int x = Integer.parseInt(hour) - Integer.parseInt(useHour);
                                        if (x <= 0) hasNum = 0;
                                        else if (x < 24 * 60) hasNum = 1;
                                        else if (x < 48 * 60) hasNum = 2;
                                        else if (x < 72 * 60) hasNum = 3;
                                    }
                                } catch (Exception e) {
                                    AppLog.E("read license err = " + e.getMessage());
                                }
                            }
                            break;
                        }
                    }
                }
                if (hasNum <= 3){
                    showRemindDialog("过期提示", hasNum == 0 ? "设备使用时间已到期，请联系管理人员" : "设备使用时间即将过期，剩余天数：" + hasNum);
                }
            }
        }
    }

    private long dateX(long lDate1, long lDate2) {
        long diff = (lDate1 < lDate2) ? (lDate2 - lDate1) : (lDate1 - lDate2);
        long day = diff / (24 * 60 * 60 * 1000);
        long hour = diff / (60 * 60 * 1000) - day * 24;
        long min = diff / (60 * 1000) - day * 24 * 60 - hour * 60;
        long sec = diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60;
        AppLog.D("date1 与 date2 相差 " + day + "天" + hour + "小时" + min + "分" + sec + "秒");

        return day;
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
        mTraceCatchFragment.onFreqScanRsp(id, rsp);
    }

    @Override
    public void onFreqScanGetDocumentRsp(String id, GnbFreqScanGetDocumentRsp rsp) {
        AppLog.I("onFreqScanGetDocumentRsp():  id = " + id + ", rsp = " + rsp);
        //mFreqFragment.onFreqScanGetDocumentRsp(id, rsp);
    }

    @Override
    public void onStopFreqScanRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onStopFreqScanRsp():  id = " + id + ", rsp = " + rsp);
        //mFreqFragment.onStopFreqScanRsp(id, rsp);
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

    @Override
    public void onSocketStateChange(String id, int lastState, int newState) {
        AppLog.I("onSocketStateChange():  id = " + id + ", lastState = " + lastState + ", newState = " + newState);

        if (deviceList.size() <= 0) return;
        int indexById = getIndexById(id);
        if (indexById == -1) return;

        int type = deviceList.get(indexById).getRsp().getDevName().contains(devA) ? 0 : 1;

        final TraceUtil traceUtil = deviceList.get(indexById).getTraceUtil();
        if (newState == ConnectProtocol.SOCKET.STATE_CONNECTING) {
            updateConnectState(type, 101);
            updateSteps(type, StepBean.State.success, "检测到设备，正在连接中");
        } else if (newState == ConnectProtocol.SOCKET.STATE_CONNECTED) {

            //refreshWorkState(indexById, GnbBean.State.IDLE, "连接成功");

            updateConnectState(0, 103);
            updateConnectState(1, 103);
            updateSteps(type, StepBean.State.success, "已连接设备");
            mHandler.removeCallbacksAndMessages(null);

            mTraceCatchFragment.setBtnEnable(false);
            mHandler.removeMessages(999);
            mHandler.removeMessages(998);
            Message message = new Message();
            message.what = 998;
            message.obj = id;
            mHandler.sendMessageDelayed(message, 2000);

            if (deviceList.get(indexById).getWorkState() == GnbBean.State.NONE) {
                deviceList.get(indexById).setWorkState(GnbBean.State.IDLE);
                MessageController.build().setGnbTime(id); // 断线、重启，再同步单板时间
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MessageController.build().setOnlyCmd(id, GnbProtocol.UI_2_gNB_QUERY_gNB_VERSION); // 断线、重启，再读取一次版本
                    }
                },200);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MessageController.build().setOnlyCmd(id, GnbProtocol.OAM_MSG_GET_SYS_INFO); // 断线、重启，再读取一次license
                    }
                },400);
            }
        } else {

            updateConnectState(0, 100);
            updateConnectState(1, 100);
            if (deviceList.size() == 0 || deviceList.size() == 1) mTraceCatchFragment.setBtnStr(false);
            else if (deviceList.size() == 2) {
                if (type == 0) {
                    if (tv_cell_1_lte.getText().equals("未就绪")) mTraceCatchFragment.setBtnStr(false);
                    else {
                        for (DeviceInfoBean bean : deviceList) {
                            if (bean.getRsp().getDevName().contains(devB)){
                                mTraceCatchFragment.setBtnStr(bean.getWorkState() == GnbBean.State.CONTROL);
                                break;
                            }
                        }
                    }
                }
            }
            mSettingFragment.setDeviceDis(id);
            MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "连接已断开, 重连中");
            mTraceCatchFragment.setHandler(true, 0);
            //deleteDev(id);
            Message message = new Message();
            if (type == 0){
                message.what = 1;
            }else {
                message.what = 2;
            }
            message.obj = id;
            mHandler.sendMessageDelayed(message, 90 * 1000);
            showToast("设备已断开连接, 重连中");
        }
    }

    // ****************************************弹框方法开始*******************************************
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

    /**
     * 显示DIALOG通用接口
     */
    public void createCustomDialog(boolean isFull) {
        int style_dialog = isFull ? R.style.Theme_G73CS : R.style.style_dialog;
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
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(getResources().getColor(R.color.main_bg_color));
            window.setBackgroundDrawableResource(R.drawable.gradient_status_bar);
            //StatusBarUtil.setLightStatusBar(window, true);
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
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(getResources().getColor(R.color.main_bg_color));
            window.setBackgroundDrawableResource(R.drawable.gradient_status_bar);
            //StatusBarUtil.setLightStatusBar(window, true);
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

    boolean isShowGuoQi = false;
    /**
     * 仅显示提示信息
     */
    public void showRemindDialog(String title, String msg) {
        if (title.equals("过期提示")){
            if (isShowGuoQi) return;
            isShowGuoQi = true;
        }
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
                int type = deviceList.get(i).getRsp().getDevName().contains(devA) ? 0 : 1;
                deviceList.remove(i);
                MainActivity.getInstance().updateSteps(type, StepBean.State.success, "结束工作");
            }
        }
    }

    private final ArrayList<StepBean> nrStepsList = new ArrayList<>();
    private final ArrayList<StepBean> lteStepsList = new ArrayList<>();

    public void updateSteps(int type, int state, String info) {
        AppLog.I("updateSteps type = " + type + ", state = " + state + ", info = " + info);

        if (type == 0) {
            // 先转换第一个数据的状态，失败的不变，成功的置灰
            if (nrStepsList.size() > 0) if (nrStepsList.get(0).getState() != StepBean.State.fail)
                nrStepsList.get(0).setState(StepBean.State.success_end);
            // 插到第一个位置
            nrStepsList.add(0, new StepBean(state, DateUtil.getCurrentTime(), info));
        } else {
            // 先转换第一个数据的状态，失败的不变，成功的置灰
            if (lteStepsList.size() > 0) if (lteStepsList.get(0).getState() != StepBean.State.fail)
                lteStepsList.get(0).setState(StepBean.State.success_end);
            // 插到第一个位置
            lteStepsList.add(0, new StepBean(state, DateUtil.getCurrentTime(), info));
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
                case 998:
                case 999:
                    String id = msg.obj.toString();

                    if (msg.what == 999){
                        List<TracePara> traceList = MessageController.build().getTraceList();
                        TraceUtil traceUtil = new TraceUtil();
                        for (int i = 0; i < 4; i++){
                            for (TracePara tracePara : traceList) {
                                if (tracePara.getId().equals(id) && tracePara.getCellId() == i) {
                                    String arfcn = MessageController.build().getArfcn(id, i);
                                    String pci = MessageController.build().getPci(id, i);
                                    traceUtil.setArfcn(i, arfcn);
                                    traceUtil.setPci(i, pci);
                                    break;
                                }
                            }
                        }
                        deviceList.get(0).setTraceUtil(traceUtil);
                    }

                    for (int i = 0; i < 4; i++){
                        deviceList.get(0).getTraceUtil().setWorkState(i, GnbBean.State.IDLE);
                        if (MessageController.build().isEnableChangeTac(id, i) && MessageController.build().isTracing(id, i)){
                            updateProgress(0, 100, i, "搜寻中",false);
                            deviceList.get(0).getTraceUtil().setWorkState(i, GnbBean.State.CONTROL);
                            updateParam(i, deviceList.get(0).getTraceUtil().getArfcn(i), deviceList.get(0).getTraceUtil().getPci(i));
                            mTraceCatchFragment.setHandler(false, i);
                        }
                    }
                    mTraceCatchFragment.setBtnEnable(true);
                    mTraceCatchFragment.refreshTraceBtn();
                    break;
            }
        }
    };

    public void addDataUaResult(String result){
        mDataUpList.add(0, DateUtil.getCurrentTime() + ":" + result);
        mSettingFragment.notifyUpDataChanged();
    }

    @Override
    public void onSlideMenuItemToggle(int type, boolean checked) {

    }

    @Override
    public void onSlideMenuItemClick(int type) {
    }

    public void showFreqListDialog(){
        if (freqList.size() <= 0){
            showToast("当前无扫频数据");
            return;
        }
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_freq, null);

        TextView tv_freq_info = view.findViewById(R.id.tv_freq_info);
        tv_freq_info.setText("扫频已结束，搜寻业务中");
        TextView tv_freq_count = view.findViewById(R.id.tv_freq_count);
        tv_freq_count.setText("");

        RecyclerView freq_list = view.findViewById(R.id.freq_list);
        freq_list.setLayoutManager(new LinearLayoutManager(mContext));

        FreqResultListAdapter adapter = new FreqResultListAdapter(mContext, freqList);
        freq_list.setLayoutManager(new LinearLayoutManager(mContext));
        freq_list.setAdapter(adapter);

        TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setText("返回");
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false, true);
    }
}