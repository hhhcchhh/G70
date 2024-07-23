package com.simdo.g73cs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import com.Logcat.SLog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
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
import com.nr.Util.OpLog;
import com.simdo.g73cs.Adapter.FragmentAdapter;
import com.simdo.g73cs.Adapter.FreqResultListAdapter;
import com.simdo.g73cs.Adapter.StepAdapter;
import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.HistoryBean;
import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.Bean.TraceBean;
import com.simdo.g73cs.Dialog.AutoFreqDialog;
import com.simdo.g73cs.Dialog.LoginDialog;
import com.simdo.g73cs.Fragment.CatchChildFragment;
import com.simdo.g73cs.Fragment.CfgTraceChildFragment;
import com.simdo.g73cs.Fragment.TraceChildFragment;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.SQL.DbManage;
import com.simdo.g73cs.SlideMenu.SlideLeftFragment;
import com.simdo.g73cs.SlideMenu.SlideRightFragment;
import com.simdo.g73cs.SlideMenu.SlidingMenu;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DateUtil;
import com.simdo.g73cs.Util.FreqUtil;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.ParseDataUtil;
import com.simdo.g73cs.Util.PermissionsUtil;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.TraceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnSocketChangeListener,
        MessageController.OnSetGnbListener, FTPUtil.OnFtpListener {

    private static MainActivity instance;
    public Context mContext;
    public Activity mActivity;
    private final List<MyUeidBean> blackList = new ArrayList<>(); // 存储黑/白名单

    public static DeviceInfoBean device = null;
    public int[] ueMax = new int[]{0, 0};
    public int scanCount = 2;
    public int nearValue = -3;
    public boolean enableSpecial;
    private int tipType = 0; // 0:未提示  1：电压小于17提示  2：电压小于16提示
    public List<HistoryBean> historyList = new ArrayList<>();

    private AutoFreqDialog mAutoFreqDialog = null;

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
        window.setNavigationBarColor(Color.parseColor("#2A72FF"));
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        instance = this;
        mContext = this;
        mActivity = this;

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        screenWidth = windowManager.getDefaultDisplay().getWidth();
        screenHeight = windowManager.getDefaultDisplay().getHeight();

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
        // 弹出登录界面
        mLoginDialog = new LoginDialog(mContext);
        mLoginDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mLoginDialog = null;
            }
        });
        mLoginDialog.show();
        initView();   // 初始化视图以及监听事件
        initSlideMenu();
        initSDK(); // 先绑定服务
        initObject(); // 初始化对象以及数据
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
        ZTcpService.build().addConnectListener(this);
        MessageController.build().setOnSetGnbListener(this);
        FTPUtil.build().setFtpListener(this);
    }

    FragmentAdapter fragmentAdapter;
    CfgTraceChildFragment mCfgTraceChildFragment;
    TraceChildFragment mTraceChildFragment;
    CatchChildFragment mCatchChildFragment;
    private List<ImsiBean> mImsiList;
    private TextView tv_dev_state, tv_do_btn;
    private TextView tv_gain_btn;
    private int pos = 0;
    ViewPager2 view_pager;

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        tv_gain_btn = findViewById(R.id.tv_gain_btn);
        tv_gain_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (device == null) {
                    showToast(getString(R.string.dev_offline));
                    tv_gain_btn.setVisibility(View.GONE);
                    return;
                }
                boolean isNear = tv_gain_btn.getTag().toString().equals("near");
                // 相反处理
                tv_gain_btn.setTag(isNear ? "standard" : "near");
                tv_gain_btn.setBackgroundResource(isNear ? R.drawable.btn_bg_gain : R.drawable.btn_bg_gain_near);
                doSetTxPwrOffset(isNear ? 0 : nearValue);
            }
        });
        tv_gain_btn.setOnTouchListener(moveListener);
        tv_do_btn = findViewById(R.id.tv_do_btn);
        tv_do_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doWork();
            }
        });
        tv_do_btn.setOnTouchListener(moveListener);
        tv_dev_state = findViewById(R.id.tv_dev_state);
        tv_dev_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                menu.setMode(SlidingMenu.LEFT);
                menu.showMenu();
            }
        });
        findViewById(R.id.iv_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                menu.setMode(SlidingMenu.LEFT_RIGHT);
                menu.showSecondaryMenu();
            }
        });

        setDevState(-1, getString(R.string.not_ready));
        // 加载底部导航标签
        List<Fragment> fragmentList = new ArrayList<>();
        mCfgTraceChildFragment = new CfgTraceChildFragment(mContext);
        mTraceChildFragment = new TraceChildFragment(mContext);
        mCatchChildFragment = new CatchChildFragment(mContext, mImsiList = new ArrayList<>());
        fragmentList.add(mCfgTraceChildFragment);
        fragmentList.add(mTraceChildFragment);
        fragmentList.add(mCatchChildFragment);
        String[] titles = new String[]{getString(R.string.cfg_trace), getString(R.string.target_report), getString(R.string.catch_list)};
        int[] icons = new int[]{R.drawable.location_tab_icon, R.drawable.target_tab_icon, R.drawable.list_tab_icon};
        int[] colors = new int[]{Color.parseColor("#2A72FF"), Color.parseColor("#8F9399")}; //选中颜色  正常颜色

        int[][] states = new int[2][]; //状态
        states[0] = new int[]{android.R.attr.state_selected}; //选中
        states[1] = new int[]{}; //默认
        ColorStateList colorList = new ColorStateList(states, colors);

        view_pager = findViewById(R.id.view_pager);
        final TextView main_page_title = findViewById(R.id.main_page_title);
        main_page_title.setText(getString(R.string.cfg_trace));
        view_pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                main_page_title.setText(titles[position]);
                pos = position;
                setMenuOpenMode(position);
            }
        });
        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), getLifecycle(), fragmentList);
        view_pager.setAdapter(fragmentAdapter);
        view_pager.setCurrentItem(0);

        TabLayout tab_layout = findViewById(R.id.tab_layout);
        tab_layout.setTabRippleColorResource(R.color.trans);// 点击无波纹，需动态设置
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

    private float lastX = 0;
    private float lastY = 0;
    private float beginX = 0;
    private float beginY = 0;
    private int screenWidth = 0;
    private int screenHeight = 0;

    View.OnTouchListener moveListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //限制左右滑动
                    menu.setStatic(true);
                    //获取点击时x y 轴的数据
                    lastX = event.getRawX();
                    lastY = event.getRawY();
                    // 记录控件开始位置
                    beginX = lastX;
                    beginY = lastY;
                    break;
                case MotionEvent.ACTION_MOVE:
                    //获得x y轴的偏移量
                    int dx = (int) (event.getRawX() - lastX);
                    int dy = (int) (event.getRawY() - lastY);
                    //获得控件上下左右的位置信息,加上我们的偏移量,新得到的位置就是我们控件将要出现的位置
                    int l = view.getLeft() + dx;
                    int t = view.getTop() + dy;
                    int r = view.getRight() + dx;
                    int b = view.getBottom() + dy;
                    //判断四个实际位置,如果有一边已经划出屏幕,那就把这边位置设置为0然后相反的边的位置就设置成控件的高度或者宽度
                    if (l < 0) {
                        l = 0;
                        r = l + view.getWidth();
                    } else if (r > screenWidth) {
                        r = screenWidth;
                        l = r - view.getWidth();
                    }
                    if (t < 0) {
                        t = 0;
                        b = t + view.getHeight();
                    } else if (b > screenHeight - 50) {
                        b = screenHeight - 50;
                        t = b - view.getHeight();
                    }
                    //然后使用我们view的layout重新在布局中把我们的控件画出来
                    view.layout(l, t, r, b);
                    //并把现在的x y设置给lastX lastY
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    //绘制
                    view.postInvalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    // 恢复左右滑动
                    menu.setStatic(false);
                    // 解决拖拽的时候松手点击事件触发
                    if (Math.abs(lastX - beginX) > 10 || Math.abs(lastY - beginY) > 10) return true;
                    break;
            }
            return false;
        }
    };
    
    boolean[] isChangeImsi = new boolean[]{false, false, false, false};
    private void doWork() {
        String text = tv_do_btn.getText().toString();
        if (text.equals(getString(R.string.open))) {
            if (device == null) {
                showToast(getString(R.string.dev_offline));
                return;
            }
            if (device.getWorkState() != GnbBean.State.IDLE) {
                showToast(getString(R.string.dev_busy_please_wait));
                return;
            }
            Double vol = device.getRsp().getVoltageList().get(1);
            if (vol < 1){
            }else if (vol < 16.6) {
                showRemindDialog(getString(R.string.vol_min_title), getString(R.string.vol_min_tip));
                return;
            }

            startRunWork();

        } else if (text.equals(getString(R.string.stop))) {
            stopTraceDialog();
        } else if (text.equals(getString(R.string.starting))) {
            showToast(getString(R.string.starting_cannot_fast));
        } else if (text.equals(getString(R.string.stoping))) {
            showToast(getString(R.string.stoping_cannot_fast));
        }
    }

    private void doSetTxPwrOffset(final int value) {
        AppLog.I("TraceFragment rg_rx_gain rxValue = " + value);

        TraceUtil traceUtil = device.getTraceUtil();
        String id = device.getRsp().getDeviceId();
        if (MessageController.build().getTraceType(id) == GnbProtocol.TraceType.TRACE) {
            int delay = 0;
            for (int cell_id = 0; cell_id < 4; cell_id++){
                String arfcn = traceUtil.getArfcn(cell_id);
                if (!arfcn.isEmpty()) {
                    int finalCell_id = cell_id;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MessageController.build().setTxPwrOffset(id, finalCell_id, Integer.parseInt(arfcn), value);
                        }
                    }, delay);
                    delay += 300;
                }
            }
        }
    }

    private HistoryBean checkHistoryBean; // 当前勾选的启动项
    private boolean isInFreqForPci = false; // 标志是否在PCI自适应扫频中
    private final HashMap<String, Boolean> bandRunOneMap = new HashMap<>(); // 标志是否N79/B34/B5/B8 连续10s内无上号，则切换下一个频点轮巡,且后续不再参与轮巡
    private final HashMap<Integer/*通道*/, Integer/*Handle消息号*/> bandRunOneHandleMap = new HashMap<>(); // 记录是否N79/B34/B5/B8 连续10s内无上号定时线程号

    public void startRunWork() {
        int checkPos = mCfgTraceChildFragment.getLastPos();
        if (checkPos == -1) {
            if (device == null) showToast(getString(R.string.dev_offline));
            else showToast(getString(R.string.check_item_tip));
            return;
        }
        mHandler.removeCallbacksAndMessages(null);
        mCatchChildFragment.restartCatch();
        mImsiList.clear();
        mCatchChildFragment.resetShowData(true);
        mTraceChildFragment.resetRsrp();
        ParseDataUtil.build().initData();
        isClickStop = false;
        isRsrpStart = false;
        for (int i = 0; i < 4; i++){
            // 跳过列表第一个数据，取消第一个频点多一次轮询的机会
            autoArfcnIndex[i] = 1;
            runFailCount[i] = 0;
            autoRun[i] = false;
            isChangeImsi[i] = false;
            countCell[i] = autoArfcnTime + 1; //避免因这个条件导致一开始无上号一直不走轮循线程
        }

        AppLog.I("startRunWork() 定位启动");
        tv_gain_btn.setTag("standard");
        tv_gain_btn.setBackgroundResource(R.drawable.btn_bg_gain);
        String sync_mode = PrefUtil.build().getValue("sync_mode", "Air").toString();
        int airSync = sync_mode.equals("Air") || sync_mode.equals(getString(R.string.air)) ? 1 : 0;

        checkHistoryBean = historyList.get(checkPos);

        // 10秒无上号数据逻辑1   启动前恢复N79/B34/B5/B8参与轮巡
        bandRunOneMap.put("N79", true);
        bandRunOneMap.put("B34", true);
        bandRunOneMap.put("B5", true);
        bandRunOneMap.put("B8", true);
        bandRunOneHandleMap.clear();

        if (checkHistoryBean.getMode() == 2) { // 2 专业
            goSetParamAndRun();
        }else if(checkHistoryBean.getMode() == 3){ // 3 专业 自配置PCI
            int delay = 0;
            if (!checkHistoryBean.getImsiFirst().isEmpty() && checkHistoryBean.getTD1().size() > 0) delay = 500;
            if (!checkHistoryBean.getImsiSecond().isEmpty() && checkHistoryBean.getTD2().size() > 0) delay += 500;
            if (!checkHistoryBean.getImsiThird().isEmpty() && checkHistoryBean.getTD3().size() > 0) delay += 500;
            if (!checkHistoryBean.getImsiFourth().isEmpty() && checkHistoryBean.getTD4().size() > 0) delay += 500;
            if (delay == 0) {
                showToast(getString(R.string.no_imsi));
                return;
            }
            freqList.clear();
            isInFreqForPci = true;
            index = 0;
            indexTD = 0;
            setStartBtnText(getString(R.string.starting));
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_stop);
            setDevState(1, getString(R.string.working));
            freqScan(device.getRsp().getDeviceId());
        }else {// 0 自动、1 名单
            String tracePlmn = checkHistoryBean.getImsiFirst().substring(0, 5);
            final boolean isMS;
            switch (tracePlmn) {
                //移动
                case "46000":
                case "46002":
                case "46004":
                case "46007":
                case "46008":
                case "46013":
                    //广电
                case "46015":
                    isMS = true;
                    break;
                default:
                    isMS = false;
                    break;
            }
            mAutoFreqDialog = new AutoFreqDialog(mContext, isMS);
            mAutoFreqDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    if (!mAutoFreqDialog.isUserCancel()) {
                        checkHistoryBean.setTD1(mAutoFreqDialog.getTD1());
                        checkHistoryBean.setTD3(mAutoFreqDialog.getTD3());
                        if (PaCtl.build().isB97502){
                            if (isMS){// B97502移动只有通道四有，如果B40有多个，需要分一个到通道二做为次B40开启
                                if (mAutoFreqDialog.getTD2().size() == 0){
                                    String firstB40Arfcn = "";
                                    for (int i = 0; i < mAutoFreqDialog.getTD4().size(); i++){
                                        String arfcn = mAutoFreqDialog.getTD4().get(i).getArfcn();
                                        if (LteBand.earfcn2band(Integer.parseInt(arfcn)) == 40){
                                            if (arfcn.equals(firstB40Arfcn)) continue;
                                            if (!firstB40Arfcn.isEmpty()){ // 第二个B40
                                                mAutoFreqDialog.getTD2().add(mAutoFreqDialog.getTD4().get(i));
                                                mAutoFreqDialog.getTD4().remove(i);
                                                break;
                                            }
                                            firstB40Arfcn = arfcn;
                                        }
                                    }
                                }
                            }else {// 联通B3同理, 联通只有通道二有，如果B3有多个，需要分一个到通道四做为次B3开启
                                String firstB3Arfcn = "";
                                for (int i = 0; i < mAutoFreqDialog.getTD2().size(); i++){
                                    String arfcn = mAutoFreqDialog.getTD2().get(i).getArfcn();
                                    if (LteBand.earfcn2band(Integer.parseInt(arfcn)) == 3){
                                        if (arfcn.equals(firstB3Arfcn)) continue;
                                        if (!firstB3Arfcn.isEmpty()){ // 第二个B3
                                            mAutoFreqDialog.getTD4().add(mAutoFreqDialog.getTD2().get(i));
                                            mAutoFreqDialog.getTD2().remove(i);
                                            break;
                                        }
                                        firstB3Arfcn = arfcn;
                                    }
                                }
                            }
                        }
                        checkHistoryBean.setTD2(mAutoFreqDialog.getTD2());
                        checkHistoryBean.setTD4(mAutoFreqDialog.getTD4());

                        AppLog.D("freq end, start run size 1 = " + checkHistoryBean.getTD1().size() + ", 2 = " + checkHistoryBean.getTD2().size() + ", 3 = " + checkHistoryBean.getTD3().size() + ", 4 = " + checkHistoryBean.getTD4().size());

                        device.getTraceUtil().setAirSync(GnbProtocol.CellId.FIRST, airSync);
                        device.getTraceUtil().setAirSync(GnbProtocol.CellId.SECOND, airSync);
                        device.getTraceUtil().setAirSync(GnbProtocol.CellId.THIRD, airSync);
                        device.getTraceUtil().setAirSync(GnbProtocol.CellId.FOURTH, airSync);
                        PaCtl.build().closePA(device.getRsp().getDeviceId());
                        int delay = 0;
                        if (mAutoFreqDialog.getTD1().size() > 0) {
                            runCell0();
                            delay += 500;
                        }
                        if (mAutoFreqDialog.getTD2().size() > 0) {
                            // 如果通道四的第一个频点也为B40，才开启这里的次B40
                            boolean isStartSecond = PaCtl.build().isB97502 && isMS &&
                                    LteBand.earfcn2band(Integer.parseInt(mAutoFreqDialog.getTD2().get(0).getArfcn())) == 40 && LteBand.earfcn2band(Integer.parseInt(mAutoFreqDialog.getTD4().get(0).getArfcn())) == 40;
                            runCell1(isStartSecond ? delay + 1500 : delay);
                            delay += 500;
                        }
                        if (mAutoFreqDialog.getTD3().size() > 0) {
                            runCell2(delay);
                            delay += 500;
                        }
                        if (mAutoFreqDialog.getTD4().size() > 0) {
                            if (PaCtl.build().isB97502 && !isMS){
                                // 如果通道四有数据，说明是被分了一个次B3过来
                                if (LteBand.earfcn2band(Integer.parseInt(mAutoFreqDialog.getTD2().get(0).getArfcn())) == 3){
                                    // 如果通道四的第一个频点也为B3，才开启这里的次B3
                                    runCell3(delay + 1500);
                                }
                            }else {
                                runCell3(delay);
                            }
                        }
                    }
                    mAutoFreqDialog = null;
                }
            });
            mAutoFreqDialog.show();
        }
    }

    private void runCell0() {
        updateParamAndStart(checkHistoryBean.getImsiFirst(), checkHistoryBean.getTD1().get(0), GnbProtocol.CellId.FIRST);
        freqEndGoRunWork();
    }

    private void runCell1(int delay) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateParamAndStart(checkHistoryBean.getImsiSecond(), checkHistoryBean.getTD2().get(0), GnbProtocol.CellId.SECOND);
                if (delay == 0) freqEndGoRunWork();
            }
        }, delay);
    }

    private void runCell2(int delay) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateParamAndStart(checkHistoryBean.getImsiThird(), checkHistoryBean.getTD3().get(0), GnbProtocol.CellId.THIRD);
                if (delay == 0) freqEndGoRunWork();
            }
        }, delay);
    }

    private void runCell3(int delay) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateParamAndStart(checkHistoryBean.getImsiFourth(), checkHistoryBean.getTD4().get(0), GnbProtocol.CellId.FOURTH);
                if (delay == 0) freqEndGoRunWork();
            }
        }, delay);
    }

    int index = 0;
    int indexTD = 0;
    private void freqScan(String id) {
        boolean isNotStart = true;
        switch (indexTD){
            case 0:
                if (!checkHistoryBean.getImsiFirst().isEmpty()){
                    for (int i = 0; i < checkHistoryBean.getTD1().size(); i++){
                        if (i == index){
                            isNotStart = false;
                            startFreqScan(id, checkHistoryBean.getTD1().get(i).getArfcn());
                            index++;
                            break;
                        }
                    }
                }
                if (isNotStart){
                    index = 0;
                    indexTD++;
                    freqScan(id);
                    return;
                }
                break;
            case 1:
                if (!checkHistoryBean.getImsiSecond().isEmpty()){
                    for (int i = 0; i < checkHistoryBean.getTD2().size(); i++){
                        if (i == index){
                            isNotStart = false;
                            startFreqScan(id, checkHistoryBean.getTD2().get(i).getArfcn());
                            index++;
                            break;
                        }
                    }
                }
                if (isNotStart){
                    index = 0;
                    indexTD++;
                    freqScan(id);
                    return;
                }
                break;
            case 2:
                if (!checkHistoryBean.getImsiThird().isEmpty()){
                    for (int i = 0; i < checkHistoryBean.getTD3().size(); i++){
                        if (i == index){
                            isNotStart = false;
                            startFreqScan(id, checkHistoryBean.getTD3().get(i).getArfcn());
                            index++;
                            break;
                        }
                    }
                }
                if (isNotStart){
                    index = 0;
                    indexTD++;
                    freqScan(id);
                    return;
                }
                break;
            case 3:
                if (!checkHistoryBean.getImsiFourth().isEmpty()){
                    for (int i = 0; i < checkHistoryBean.getTD4().size(); i++){
                        if (i == index){
                            isNotStart = false;
                            startFreqScan(id, checkHistoryBean.getTD4().get(i).getArfcn());
                            index++;
                            break;
                        }
                    }
                }
                break;
        }

        if (isNotStart) {
            isInFreqForPci = false;
            LinkedList<LinkedList<ArfcnPciBean>> linkedLists = FreqUtil.build().decFreqList(freqList);
            LinkedList<ArfcnPciBean> TD1 = new LinkedList<>(linkedLists.get(0));
            LinkedList<ArfcnPciBean> TD2 = new LinkedList<>(linkedLists.get(1));
            LinkedList<ArfcnPciBean> TD3 = new LinkedList<>(linkedLists.get(2));
            LinkedList<ArfcnPciBean> TD4 = new LinkedList<>(linkedLists.get(3));
            for (int i = 0; i < checkHistoryBean.getTD1().size(); i++){
                for (ArfcnPciBean bean : TD1) {
                    if (bean.getArfcn().equals(checkHistoryBean.getTD1().get(i).getArfcn())){
                        checkHistoryBean.getTD1().get(i).setPci(bean.getPci());
                        break;
                    }
                }
            }

            for (int i = 0; i < checkHistoryBean.getTD2().size(); i++){
                boolean isSecondArfcn = false;
                if (PaCtl.build().isB97502){
                    String arfcn = checkHistoryBean.getTD2().get(i).getArfcn();
                    if (LteBand.earfcn2band(Integer.parseInt(arfcn)) == 40) isSecondArfcn = true;
                }
                if (isSecondArfcn){
                    for (ArfcnPciBean bean : TD4) {
                        if (bean.getArfcn().equals(checkHistoryBean.getTD2().get(i).getArfcn())){
                            checkHistoryBean.getTD2().get(i).setPci(bean.getPci());
                            break;
                        }
                    }
                }else {
                    for (ArfcnPciBean bean : TD2) {
                        if (bean.getArfcn().equals(checkHistoryBean.getTD2().get(i).getArfcn())){
                            checkHistoryBean.getTD2().get(i).setPci(bean.getPci());
                            break;
                        }
                    }
                }
            }

            for (int i = 0; i < checkHistoryBean.getTD3().size(); i++){
                for (ArfcnPciBean bean : TD3) {
                    if (bean.getArfcn().equals(checkHistoryBean.getTD3().get(i).getArfcn())){
                        checkHistoryBean.getTD3().get(i).setPci(bean.getPci());
                        break;
                    }
                }
            }

            for (int i = 0; i < checkHistoryBean.getTD4().size(); i++){
                boolean isSecondArfcn = false;
                if (PaCtl.build().isB97502){
                    String arfcn = checkHistoryBean.getTD4().get(i).getArfcn();
                    if (LteBand.earfcn2band(Integer.parseInt(arfcn)) == 3) isSecondArfcn = true;
                }
                if (isSecondArfcn){
                    for (ArfcnPciBean bean : TD2) {
                        if (bean.getArfcn().equals(checkHistoryBean.getTD4().get(i).getArfcn())){
                            checkHistoryBean.getTD4().get(i).setPci(bean.getPci());
                            break;
                        }
                    }
                }else {
                    for (ArfcnPciBean bean : TD4) {
                        if (bean.getArfcn().equals(checkHistoryBean.getTD4().get(i).getArfcn())){
                            checkHistoryBean.getTD4().get(i).setPci(bean.getPci());
                            break;
                        }
                    }
                }
            }
            goSetParamAndRun();
        }
    }

    private void goSetParamAndRun() {
        String sync_mode = PrefUtil.build().getValue("sync_mode", "Air").toString();
        int airSync = sync_mode.equals("Air") || sync_mode.equals(getString(R.string.air)) ? 1 : 0;
        TraceUtil traceUtil = new TraceUtil();
        device.setTraceUtil(traceUtil);
        device.getTraceUtil().setAirSync(GnbProtocol.CellId.FIRST, airSync);
        device.getTraceUtil().setAirSync(GnbProtocol.CellId.SECOND, airSync);
        device.getTraceUtil().setAirSync(GnbProtocol.CellId.THIRD, airSync);
        device.getTraceUtil().setAirSync(GnbProtocol.CellId.FOURTH, airSync);
        PaCtl.build().closePA(device.getRsp().getDeviceId());
        int delay = 0;
        if (!checkHistoryBean.getImsiFirst().isEmpty() && checkHistoryBean.getTD1().size() > 0) {
            runCell0();
            delay = 500;
        }
        if (!checkHistoryBean.getImsiSecond().isEmpty() && checkHistoryBean.getTD2().size() > 0) {
            int isSecondB40 = 0;
            if (PaCtl.build().isB97502){
                ArfcnPciBean bean = checkHistoryBean.getTD2().get(0);
                if (bean.getArfcn().isEmpty()) isSecondB40 = -1;
                else if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 40) isSecondB40 = 1;
            }
            if (isSecondB40 == 1) runCell1(delay + 1500);
            else {
                runCell1(delay);
                delay += 500;
            }
        }
        if (!checkHistoryBean.getImsiThird().isEmpty() && checkHistoryBean.getTD3().size() > 0) {
            runCell2(delay);
            delay += 500;
        }
        if (!checkHistoryBean.getImsiFourth().isEmpty() && checkHistoryBean.getTD4().size() > 0) {
            runCell3(delay);
            delay += 500;
        }
        if (delay == 0) showToast(getString(R.string.no_imsi));
    }

    private void startFreqScan(String id, String arfcn) {
        AppLog.D("MainActivity startFreqScan id = " + id + ", arfcn = " + arfcn);
        PaCtl.build().closePA(id);
        boolean isLte = arfcn.length() < 6;
        int intArfcn = Integer.parseInt(arfcn);
        int band = isLte ? LteBand.earfcn2band(intArfcn) : NrBand.earfcn2band(intArfcn);
        String bandStr = (isLte ? "B" : "N") + band;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                PaCtl.build().openPAByBand(id, bandStr);
            }
        }, 100);
        int offset = 0;
        switch (bandStr) {
            case "N1":
            case "N78":
            case "B1":
            case "B3":
            case "B5":
            case "B8":
                offset = 0;
                break;
            case "N28":
            case "N41":
            case "N79":
                offset = GnbCity.build().getTimingOffset("5G");
                break;
            case "B34":
                offset = GnbCity.build().getTimingOffset("B34");
                break;
            case "B39":
                offset = GnbCity.build().getTimingOffset("B39");
                break;
            case "B40":
                offset = GnbCity.build().getTimingOffset("B40");
                break;
            case "B41":
                offset = GnbCity.build().getTimingOffset("B41");
                break;
        }
        int chan = 1;
        if (PaCtl.build().isB97502){
            switch (bandStr){
                case "N1":
                case "N28":
                case "B1":
                    chan = 3;
                    break;
                case "N41":
                case "N78":
                case "N79":
                    chan = 1;
                    break;
                case "B3":
                case "B5":
                case "B8":
                    chan = 2;
                    break;
                case "B34":
                case "B39":
                case "B40":
                case "B41":
                    chan = 4;
                    break;
            }
        }else {
            switch (bandStr){
                case "N1":
                case "N41":
                    chan = 3;
                    break;
                case "N28":
                case "N78":
                case "N79":
                    chan = 1;
                    break;
                case "B1":
                case "B3":
                case "B5":
                case "B8":
                    chan = 4;
                    break;
                case "B34":
                case "B39":
                case "B40":
                case "B41":
                    chan = 2;
                    break;
            }
        }

        final List<Integer> arfcn_list = new ArrayList<>();
        final List<Integer> time_offset = new ArrayList<>();
        final List<Integer> chan_id = new ArrayList<>();
        arfcn_list.add(intArfcn);
        time_offset.add(offset);
        chan_id.add(chan);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String sync_mode = PrefUtil.build().getValue("sync_mode", "Air").toString();
                int airSync = sync_mode.equals("Air") || sync_mode.equals(getString(R.string.air)) ? 1 : 0;
                MessageController.build().startFreqScan(id, 0, airSync, arfcn_list.size(), chan_id, arfcn_list, time_offset);
            }
        }, 200);
    }
    public void freqEndGoRunWork() {
        view_pager.setCurrentItem(1);
        showToast(getString(R.string.cfging_trace));
        refreshTraceBtn();
    }

    boolean isClickStop = false;

    private void stopTraceDialog() {
        createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClickStop = true;
                for (int i = 0; i < 4; i++){
                    isChangeImsi[i] = false;
                    autoRun[i] = false;
                }

                setStartBtnText(getString(R.string.stoping));
                final String id = device.getRsp().getDeviceId();

                int delay = 0;
                if (device.getTraceUtil().isEnable(GnbProtocol.CellId.FOURTH) &&
                        device.getTraceUtil().getWorkState(GnbProtocol.CellId.FOURTH) == GnbBean.State.TRACE) {
                    String arfcn = device.getTraceUtil().getArfcn(GnbProtocol.CellId.FOURTH);
                    if (!arfcn.isEmpty()) {
                        updateSteps(0, StepBean.State.success, getString(R.string.cell3_stoping));
                        updateProgress(0, 50, 3, getString(R.string.stoping), false);
                        device.getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FOURTH, System.currentTimeMillis());
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.STOP);

                        MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FOURTH);

                        delay += 300;
                    }
                } else {
                    if (device.getRsp().getFourthState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        updateSteps(0, StepBean.State.success, getString(R.string.cell3_stoping));
                        updateProgress(0, 50, 3, getString(R.string.stoping), false);
                        device.getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FOURTH, System.currentTimeMillis());
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.STOP);
                        MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FOURTH);
                        delay += 300;
                    } else if (device.getTraceUtil().getWorkState(GnbProtocol.CellId.FOURTH) == GnbBean.State.GNB_CFG) {
                        updateProgress(0, 50, 3, getString(R.string.stoping), false);
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.STOP);
                    } else {
                        updateProgress(0, 0, 3, getString(R.string.idle), false);
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.IDLE);
                    }
                }

                if (device.getTraceUtil().isEnable(GnbProtocol.CellId.THIRD) &&
                        device.getTraceUtil().getWorkState(GnbProtocol.CellId.THIRD) == GnbBean.State.TRACE) {
                    String arfcn = device.getTraceUtil().getArfcn(GnbProtocol.CellId.THIRD);
                    if (!arfcn.isEmpty()) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateSteps(0, StepBean.State.success, getString(R.string.cell2_stoping));
                                updateProgress(0, 50, 2, getString(R.string.stoping), false);

                                device.getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.THIRD, System.currentTimeMillis());
                                device.getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.STOP);

                                MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.THIRD);
                            }
                        }, delay);
                        delay += 300;
                    }
                } else {
                    if (device.getRsp().getThirdState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        updateSteps(0, StepBean.State.success, getString(R.string.cell2_stoping));
                        updateProgress(0, 50, 2, getString(R.string.stoping), false);

                        device.getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.THIRD, System.currentTimeMillis());
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.STOP);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.THIRD);
                            }
                        }, delay);
                        delay += 300;
                    } else if (device.getTraceUtil().getWorkState(GnbProtocol.CellId.THIRD) == GnbBean.State.GNB_CFG) {
                        updateProgress(0, 50, 2, getString(R.string.stoping), false);
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.STOP);
                    } else {
                        updateProgress(0, 0, 2, getString(R.string.idle), false);
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.IDLE);
                    }
                }

                if (device.getTraceUtil().isEnable(GnbProtocol.CellId.SECOND) &&
                        device.getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.TRACE) {
                    String arfcn = device.getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND);
                    if (!arfcn.isEmpty()) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateSteps(0, StepBean.State.success, getString(R.string.cell1_stoping));
                                updateProgress(0, 50, 1, getString(R.string.stoping), false);

                                device.getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.SECOND, System.currentTimeMillis());
                                device.getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP);

                                MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.SECOND);
                                refreshTraceBtn();
                            }
                        }, delay);
                        delay += 300;
                    }
                } else {
                    if (device.getRsp().getSecondState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        updateSteps(0, StepBean.State.success, getString(R.string.cell1_stoping));
                        updateProgress(0, 50, 1, getString(R.string.stoping), false);

                        device.getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.SECOND, System.currentTimeMillis());
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.SECOND);
                            }
                        }, delay);
                        delay += 300;
                    } else if (device.getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.GNB_CFG) {
                        updateProgress(0, 50, 1, getString(R.string.stoping), false);
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP);
                    } else {
                        updateProgress(0, 0, 1, getString(R.string.idle), false);
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.IDLE);
                    }
                }

                if (device.getTraceUtil().isEnable(GnbProtocol.CellId.FIRST) &&
                        device.getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST) == GnbBean.State.TRACE) {
                    String arfcn = device.getTraceUtil().getArfcn(GnbProtocol.CellId.FIRST);
                    if (!arfcn.isEmpty()) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateSteps(0, StepBean.State.success, getString(R.string.cell0_stoping));
                                updateProgress(0, 50, 0, getString(R.string.stoping), false);

                                device.getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FIRST, System.currentTimeMillis());
                                device.getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP);
                                MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FIRST);
                                refreshTraceBtn();
                            }
                        }, delay);
                    }
                } else {
                    if (device.getRsp().getFirstState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        updateSteps(0, StepBean.State.success, getString(R.string.cell0_stoping));
                        updateProgress(0, 50, 0, getString(R.string.stoping), false);

                        device.getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FIRST, System.currentTimeMillis());
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.FIRST);
                            }
                        }, delay);
                    } else if (device.getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST) == GnbBean.State.GNB_CFG) {
                        updateProgress(0, 50, 0, getString(R.string.stoping), false);
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP);
                    } else {
                        updateProgress(0, 0, 0, getString(R.string.idle), false);
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.IDLE);
                    }
                }

                if (isStopSecondArfcn[0] || isStopSecondArfcn[2]){
                    isStopSecondArfcn[0] = false;
                    isStopSecondArfcn[2] = false;
                    mTraceChildFragment.setConfigInfo(1, 0, "", "", "");
                }

                if (isStopSecondArfcn[1] || isStopSecondArfcn[3]){
                    isStopSecondArfcn[1] = false;
                    isStopSecondArfcn[3] = false;
                    mTraceChildFragment.setConfigInfo(3, 0, "", "", "");
                }

                refreshTraceBtn();
                PaCtl.build().closePA(id);

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

    private void refreshTraceBtn() {
        if (device == null) { // 无设备
            AppLog.I("refreshTraceBtn deviceList size = 0");
            setStartBtnText(getString(R.string.open));
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_start);
            return;
        }

        TraceUtil traceUtil = device.getTraceUtil();

        int workStateFirst = traceUtil.getWorkState(GnbProtocol.CellId.FIRST); // 通道一
        int workStateSecond = traceUtil.getWorkState(GnbProtocol.CellId.SECOND); // 通道二
        int workStateThird = traceUtil.getWorkState(GnbProtocol.CellId.THIRD); // 通道三
        int workStateFourth = traceUtil.getWorkState(GnbProtocol.CellId.FOURTH); // 通道四

        AppLog.I("refreshTraceBtn deviceList size = 1, workStateFirst = " + workStateFirst
                + ", workStateSecond = " + workStateSecond + ", workStateThird = " + workStateThird + ", workStateFourth = " + workStateFourth);

        if (workStateFirst == GnbBean.State.TRACE || workStateSecond == GnbBean.State.TRACE || workStateThird == GnbBean.State.TRACE || workStateFourth == GnbBean.State.TRACE) {
            device.setWorkState(GnbBean.State.TRACE);
            setStartBtnText(getString(R.string.stop));
        } else if (workStateFirst == GnbBean.State.CATCH || workStateSecond == GnbBean.State.CATCH || workStateThird == GnbBean.State.CATCH || workStateFourth == GnbBean.State.CATCH) {
            device.setWorkState(GnbBean.State.CATCH);
            setStartBtnText(getString(R.string.stop));
        } else if (workStateFirst == GnbBean.State.STOP || workStateSecond == GnbBean.State.STOP || workStateThird == GnbBean.State.STOP || workStateFourth == GnbBean.State.STOP) {
            device.setWorkState(GnbBean.State.STOP);
            setStartBtnText(getString(R.string.stoping));
        } else if (workStateFirst == GnbBean.State.IDLE && workStateSecond == GnbBean.State.IDLE && workStateThird == GnbBean.State.IDLE && workStateFourth == GnbBean.State.IDLE) {
            device.setWorkState(GnbBean.State.IDLE);
            setStartBtnText(getString(R.string.open));
        } else {
            setStartBtnText(getString(R.string.starting));
        }

        if (getBtnStr().contains(getString(R.string.open))) {
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_start);
            tv_gain_btn.setVisibility(View.GONE);
            if (isClickStop) mTraceChildFragment.resetRsrp(0);
            isRsrpStart = false;
            isStartCatchHandler = false;
            enableSpecial = false;// 停止业务恢复关闭状态
            mSlideRightFrag.setEnableSpecial();
            if (device != null && device.getWorkState() != GnbBean.State.NONE && device.getWorkState() != GnbBean.State.TRACE)
                setDevState(0, getString(R.string.connected));
            mCfgTraceChildFragment.setWorking(false);
        } else {
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_stop);
            tv_gain_btn.setVisibility(View.VISIBLE);
            setDevState(1, getString(R.string.working));
            mCfgTraceChildFragment.setWorking(true);
        }
    }

    private void setStartBtnText(String text) {
        tv_do_btn.setText(text);
    }

    public String getBtnStr() {
        return tv_do_btn.getText().toString();
    }

    private void setMenuOpenMode(int position) {
        if (position == 0) {
            menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
            menu.setMode(SlidingMenu.LEFT);
        } else if (position == 1) {
            menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
            menu.setMode(SlidingMenu.LEFT_RIGHT);
        } else {
            menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
            menu.setMode(SlidingMenu.RIGHT);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setDevState(int i, String s) {
        Drawable mDrawable = null;
        int color = Color.WHITE;
        switch (i) {
            case -1:
                color = Color.parseColor("#e65c5c");
                mDrawable = getResources().getDrawable(R.mipmap.icon_home_not);
                break;
            case 0:
                mDrawable = getResources().getDrawable(R.mipmap.icon_home_ok);
                break;
            case 1:
                mDrawable = getResources().getDrawable(R.mipmap.icon_home_work);
                break;
        }

        tv_dev_state.setText(s);
        tv_dev_state.setTextColor(color);
        if (mDrawable != null)
            mDrawable.setBounds(0, 0, mDrawable.getMinimumWidth() + 10, mDrawable.getMinimumHeight() + 10);
        tv_dev_state.setCompoundDrawables(mDrawable, null, null, null);
    }

    /**
     * 初始化左右滑动菜单
     */
    SlideLeftFragment mSlideLeftFrag;
    SlideRightFragment mSlideRightFrag;
    public SlidingMenu menu;

    private void initSlideMenu() {
        menu = new SlidingMenu(this);
        // 设置触摸屏幕的模式
        menu.setTouchModeBehind(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        menu.setMode(SlidingMenu.LEFT);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow_left);
        // 设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setSecondaryShadowDrawable(R.drawable.shadow_left);//右侧菜单的阴影图片
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.slide_left_menu_frame);
        menu.setOffsetFadeDegree(0.4f);// 设置主界面剩余部分的透明的
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.id_left_menu_frame, mSlideLeftFrag = new SlideLeftFragment()).commit();

        menu.setSecondaryShadowDrawable(R.drawable.shadow_right);
        // 设置右边侧滑菜单
        menu.setSecondaryMenu(R.layout.slide_right_menu_frame);
        getSupportFragmentManager().beginTransaction().replace(R.id.id_right_menu_frame, mSlideRightFrag = new SlideRightFragment(this)).commit();

        menu.setOnClosedListener(new SlidingMenu.OnClosedListener() {
            @Override
            public void onClosed() {
                setMenuOpenMode(pos);
            }
        });
    }

    StepAdapter mStepAdapter;
    public DbManage mDbManage;

    public void showStepDialog() {
        createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_steps, null);

        // 时间线
        RecyclerView rv_steps = view.findViewById(R.id.rv_steps);

        mStepAdapter = new StepAdapter(nrStepsList);
        rv_steps.setLayoutManager(new LinearLayoutManager(mContext));
        rv_steps.setAdapter(mStepAdapter);

        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeCustomDialog();
            }
        });
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mStepAdapter = null;
            }
        });

        showCustomDialog(view, false, true);
    }

    /**
     * 初始化对象
     */
    private void initObject() {
        AppLog.D("initObject()");
        mDbManage = DbManage.getInstance(mContext);
        historyList.addAll(mDbManage.getHistoryData());
        int lastPos = -1;
        for (int i = 0; i < historyList.size(); i++) {
            if (historyList.get(i).isChecked()) {
                lastPos = i;
                break;
            }
        }
        if (lastPos != -1) checkHistoryBean = historyList.get(lastPos);

        //GpsUtil.build().openGPSSettings();
        List<MyUeidBean> ueidBeanList = PrefUtil.build().getUeidList();
        if (ueidBeanList != null && ueidBeanList.size() > 0) {
            blackList.clear();
            blackList.addAll(ueidBeanList);
        }
        GnbCity.build().init();
        resetAutoArfcnTime();
    }


    /**
     * 退出应用对SDK包处理
     */
    private void closeSDK() {
        MessageController.build().removeOnSetGnbListener();
        ZTcpService.build().removeConnectListener(this);
        NrSdk.build().onDestroy();
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
            showToast(getString(R.string.click_again));
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
     * 更新连接状态
     *
     * @param type  0 NR   1 LTE
     * @param state 101 连接中  103 已连接  100 未连接
     */
    public void updateConnectState(int type, int state) {
        if (mLoginDialog != null) mLoginDialog.updateConnectState(type, state);
        if (mSlideLeftFrag != null) mSlideLeftFrag.updateConnectState(type, state);
    }

    public void showFreqListDialog() {
        if (freqList.size() <= 0) {
            showToast(getString(R.string.no_freq_data));
            return;
        }
        createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_freq, null);

        TextView tv_freq_info = view.findViewById(R.id.tv_freq_info);
        tv_freq_info.setText(R.string.freq_end);
        TextView tv_freq_count = view.findViewById(R.id.tv_freq_count);
        tv_freq_count.setText("");

        RecyclerView freq_list = view.findViewById(R.id.freq_list);
        freq_list.setLayoutManager(new LinearLayoutManager(mContext));

        FreqResultListAdapter adapter = new FreqResultListAdapter(mContext, freqList, new ListItemListener() {
            @Override
            public void onItemClickListener(int position) {

            }
        });
        freq_list.setLayoutManager(new LinearLayoutManager(mContext));
        freq_list.setAdapter(adapter);

        TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setText(R.string.cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false, true);
    }

    /**
     * 初始化工作加载进度条
     *
     * @param type 0 NR   1 LTE
     */
    public void initProgress(int type) {
        String not_ready_str = getString(R.string.not_ready);
        updateProgress(type, 0, 0, not_ready_str, true);
        updateProgress(type, 0, 1, not_ready_str, true);
        updateProgress(type, 0, 2, not_ready_str, true);
        updateProgress(type, 0, 3, not_ready_str, true);
    }

    /**
     * 更新工作加载进度条
     *
     * @param type    type 0 NR   1 LTE
     * @param pro     进度值
     * @param cell_id 小区id
     */
    public void updateProgress(int type, int pro, int cell_id, String info, boolean isFail) {
        if (cell_id == 0 && autoRun[0]) return;
        if (cell_id == 1 && autoRun[1]) return;
        if (cell_id == 2 && autoRun[2]) return;
        if (cell_id == 3 && autoRun[3]) return;
        int color = isFail ? ContextCompat.getColor(mContext, R.color.color_e65c5c) : ContextCompat.getColor(mContext, R.color.color_1a1a1a);
        if (mSlideLeftFrag != null) mSlideLeftFrag.updateProgress(type, pro, cell_id, info, color);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateSteps(int type, int state, String info) {
        AppLog.I("updateSteps type = " + type + ", state = " + state + ", info = " + info);
        // 先转换第一个数据的状态，失败的不变，成功的置灰
        if (nrStepsList.size() > 0) if (nrStepsList.get(0).getState() != StepBean.State.fail)
            nrStepsList.get(0).setState(StepBean.State.success_end);
        // 插到第一个位置
        nrStepsList.add(0, new StepBean(state, DateUtil.getCurrentTime(), info));
        if (mStepAdapter != null) mStepAdapter.notifyDataSetChanged();
    }

    @Override
    public void OnFtpConnectFail(String id, boolean b) {
        AppLog.I("OnFtpConnectFail():  id = " + id + ", b = " + b);
        mSlideRightFrag.OnFtpConnectFail(id, b);
    }

    @Override
    public void OnFtpGetFileRsp(String id, boolean b) {
        AppLog.I("OnFtpGetFileRsp():  id = " + id + ", b = " + b);
        mSlideRightFrag.OnFtpGetFileRsp(id, b);
    }

    @Override
    public void OnFtpPutFileRsp(String id, boolean b) {
        AppLog.I("OnFtpPutFileRsp(): b = " + b);
        mSlideRightFrag.OnFtpPutFileRsp(b);
    }

    @Override
    public void OnFtpGetFileProcess(String id, long l) {
        AppLog.I("OnFtpGetFileProcess():  id = " + id + ", l = " + l);
    }

    @Override
    public void onHeartStateRsp(GnbStateRsp rsp) {
        AppLog.I("onHeartStateRsp():  rsp = " + rsp);
        if (rsp == null) return;
        String id = rsp.getDeviceId();
        if (device == null) {
            device = new DeviceInfoBean();
            device.setRsp(rsp);
            MessageController.build().setGnbTime(id); // 初次，需同步单板时间

            updateConnectState(0, 103);
            setDevState(0, getString(R.string.connected));

            updateSteps(0, StepBean.State.success, getString(R.string.connected_dev));

            if (GnbStateRsp.gnbState.GNB_STATE_TRACE == rsp.getFirstState()) {
                // 定位过程中切换操作机 ,结束通道一的工作
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateSteps(0, StepBean.State.success, getString(R.string.cfg_state_err_tip));
                        updateSteps(0, StepBean.State.success, getString(R.string.recover_cell0_work));
                        MessageController.build().getCellCfg(id, GnbProtocol.CellId.FIRST, GnbProtocol.UI_2_gNB_CFG_gNB);
//                        updateSteps(0, StepBean.State.success, getString(R.string.stop_cell0_work));
//                        MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.FIRST);
                    }
                }, 400);
            } else if (GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == rsp.getFirstState())
                updateProgress(0, 0, 0, getString(R.string.idle), false);

            if (GnbStateRsp.gnbState.GNB_STATE_TRACE == rsp.getSecondState()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateSteps(0, StepBean.State.success, getString(R.string.cfg_state_err_tip));
                        updateSteps(0, StepBean.State.success, getString(R.string.recover_cell1_work));
                        MessageController.build().getCellCfg(id, GnbProtocol.CellId.SECOND, GnbProtocol.UI_2_eNB_CFG_gNB);
//                        updateSteps(0, StepBean.State.success, getString(R.string.stop_cell1_work));
//                        MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.SECOND);
                    }
                }, 600);
            } else if (GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == rsp.getSecondState())
                updateProgress(0, 0, 1, getString(R.string.idle), false);

            if (GnbStateRsp.gnbState.GNB_STATE_TRACE == rsp.getThirdState()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateSteps(0, StepBean.State.success, getString(R.string.cfg_state_err_tip));
                        updateSteps(0, StepBean.State.success, getString(R.string.recover_cell2_work));
                        MessageController.build().getCellCfg(id, GnbProtocol.CellId.THIRD, GnbProtocol.UI_2_gNB_CFG_gNB);
//                        updateSteps(0, StepBean.State.success, getString(R.string.stop_cell2_work));
//                        MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.THIRD);
                    }
                }, 800);
            } else if (GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == rsp.getThirdState())
                updateProgress(0, 0, 2, getString(R.string.idle), false);

            if (GnbStateRsp.gnbState.GNB_STATE_TRACE == rsp.getFourthState()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateSteps(0, StepBean.State.success, getString(R.string.cfg_state_err_tip));
                        updateSteps(0, StepBean.State.success, getString(R.string.recover_cell3_work));
                        MessageController.build().getCellCfg(id, GnbProtocol.CellId.FOURTH, GnbProtocol.UI_2_eNB_CFG_gNB);
//                        updateSteps(0, StepBean.State.success, getString(R.string.stop_cell3_work));
//                        MessageController.build().setCmdAndCellID(rsp.getDeviceId(), GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FOURTH);
                    }
                }, 1000);
            } else if (GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG == rsp.getFourthState())
                updateProgress(0, 0, 3, getString(R.string.idle), false);
        } else device.setRsp(rsp);

        if (rsp.getDevState() == GnbStateRsp.devState.ABNORMAL) {
            // 非定位状态走这里
            // 定位状态下，走GNB_STATE_PHY_ABNORMAL
            updateSteps(0, StepBean.State.fail, getString(R.string.dev_err_tip));
            showToast(getString(R.string.dev_err_tip));
            //showRemindDialog("异常提示", "设备异常，须掉电重启");
            return;
        }

        boolean isShowVolTip = false;
        Double vol = rsp.getVoltageList().get(1);
        String tip = "";
        if (vol < 1) {
        } else if (vol < 16) {
            if (tipType == 1 || tipType == 0) {
                if (isTipcount > 6){
                    tipType = 2;
                    isShowVolTip = true;
                    if (lastTip.contains(getString(R.string.vol_min))) closeCustomDialog();
                    if (tv_do_btn.getText().equals(getString(R.string.stop))) {
                        tip = getString(R.string.vol_min_close_tip);
                        stopNoUpCell();
                    }else tip = getString(R.string.vol_min_tip);
                }else isTipcount++;
            }
        } else if (vol < 17) {
            if (tipType == 0){
                if (isTipcount > 3) {
                    tipType = 1;
                    isShowVolTip = true;
                    tip = getString(R.string.add_vol_tip);
                }else isTipcount++;
            } else if (tipType == 2) {
                tipType = 1;
                isTipcount = 4;
            }
        } else {
            isTipcount = 0;
            if (tipType == 1 && vol > 17.2) tipType = 0;
        }

        if (isShowVolTip && !lastTip.equals(tip)) {
            lastTip = tip;
            showRemindDialog(getString(R.string.vol_min_title), tip);
        }
        if (mSlideLeftFrag != null)
            mSlideLeftFrag.updateState(rsp.getVoltageList(), rsp.getTempList(), rsp.getGnss_select(), rsp.getGpsSyncState(), rsp.getFirstAirState(), rsp.getSecondAirState(), rsp.getThirdAirState(), rsp.getFourthAirState());
    }

    private void stopNoUpCell() {
        boolean[] cellUp = mTraceChildFragment.getCellUp();
        final String id = device.getRsp().getDeviceId();
        int delay = 0;
        for (int i = 3; i > -1; i--) {
            int finalI = i;
            String stopInfo = getString(R.string.cell0_stoping);
            if (i == 3) stopInfo = getString(R.string.cell3_stoping);
            else if (i == 2) stopInfo = getString(R.string.cell2_stoping);
            else if (i == 1) stopInfo = getString(R.string.cell1_stoping);
            if (!cellUp[i]){
                if (device.getTraceUtil().isEnable(i) && device.getTraceUtil().getWorkState(i) == GnbBean.State.TRACE) {
                    String arfcn = device.getTraceUtil().getArfcn(i);
                    if (!arfcn.isEmpty()) {
                        String finalStopInfo = stopInfo;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateSteps(0, StepBean.State.success, finalStopInfo);
                                updateProgress(0, 50, finalI, getString(R.string.stoping), false);

                                device.getTraceUtil().setAtCmdTimeOut(finalI, System.currentTimeMillis());
                                device.getTraceUtil().setWorkState(finalI, GnbBean.State.STOP);
                                MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, finalI);
                            }
                        }, delay);
                        delay += 300;
                    }
                } else {
                    int state = device.getRsp().getFirstState();
                    if (i == 3) state = device.getRsp().getFourthState();
                    else if (i == 2) state = device.getRsp().getThirdState();
                    else if (i == 1) state = device.getRsp().getSecondState();
                    if (state == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        updateSteps(0, StepBean.State.success, stopInfo);
                        updateProgress(0, 50, finalI, getString(R.string.stoping), false);

                        device.getTraceUtil().setAtCmdTimeOut(finalI, System.currentTimeMillis());
                        device.getTraceUtil().setWorkState(finalI, GnbBean.State.STOP);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_TRACE, finalI);
                            }
                        }, delay);
                        delay += 300;
                    } else if (device.getTraceUtil().getWorkState(finalI) == GnbBean.State.GNB_CFG) {
                        updateProgress(0, 50, finalI, getString(R.string.stoping), false);
                        device.getTraceUtil().setWorkState(finalI, GnbBean.State.STOP);
                    }
                }
            }
        }
        refreshTraceBtn();
    }

    String lastTip = "";
    int isTipcount = 0;

    @Override
    public void onQueryVersionRsp(String id, GnbVersionRsp rsp) {
        AppLog.I("onQueryVersionRsp():  id = " + id + ", rsp = " + rsp);
        if (rsp != null) {
            if (rsp.getSwVer() != null) {
                device.setSoftVer(rsp.getSwVer());
                device.setHwVer(rsp.getHwVer());
                device.setFpgaVer(rsp.getFpgaVer());
            } else {
                updateSteps(0, StepBean.State.fail, getString(R.string.query_version_file));
            }
        }
    }

    public int autoArfcnTime;

    @Override
    public void onStartTraceRsp(String id, GnbTraceRsp rsp) {
        if (rsp == null || device == null) return;
        final TraceUtil traceUtil = device.getTraceUtil();
        if (rsp.getCmdRsp() != null) {
            AppLog.I("onStartTraceRsp():  id = " + id + ", rsp = " + rsp);
            final int cell_id = rsp.getCmdRsp().getCellId();
            if (isClickStop) {
                MessageController.build().setCmdAndCellID(id, rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_gNB_START_TRACE ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, cell_id);
                return;
            }
            if (rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_gNB_START_TRACE || rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_gNB_START_LTE_TRACE) {
                String s = getCellStr(cell_id);
                if (rsp.getCmdRsp().getRspValue() == GnbProtocol.OAM_ACK_OK) {

                    //第五步.定位中，这里做判断，是设置状态为侦码中还是定位中
                    String imsi = device.getTraceUtil().getImsi(cell_id);

                    device.getTraceUtil().setEnable(cell_id, true);
                    device.getTraceUtil().setWorkState(cell_id, GnbBean.State.TRACE);

                    runFailCount[cell_id] = 99; // 这里随便给个99值，大于空口失败允许循环次数就行，启动后，TAC循环空口失败就一直重复循环，直到用户点击停止

                    // 10秒无上号数据逻辑2   仅自动模式下  开始计时
                    if (checkHistoryBean.getMode() != 2 && checkHistoryBean.getMode() != 3){
                        String arfcn = device.getTraceUtil().getArfcn(cell_id);
                        int iArfcn = Integer.parseInt(arfcn);
                        int band;
                        if (arfcn.length() < 6){
                            band = LteBand.earfcn2band(iArfcn);
                            if (band == 34){
                                // send不用判断是B97502还是B97501
                                // 也不用管这个通道是否有其他频段，因为仅B34的情况不会走轮循线程，这个10s虽多此一举但不影响
                                // 4G mHandler 的what统一+1000以区分
                                mHandler.sendEmptyMessageDelayed(1034, 10000);
                                bandRunOneHandleMap.put(PaCtl.build().isB97502 ? 3 : 1, 1034);
                            }else if (band == 5 || band == 8){
                                // send不用判断是B97502还是B97501
                                // 也不用管这个通道是否有其他频段，因为仅B34的情况不会走轮循线程，这个10s虽多此一举但不影响
                                // 4G mHandler 的what统一+1000以区分
                                boolean isSend = false;
                                LinkedList<ArfcnPciBean> td = PaCtl.build().isB97502 ? checkHistoryBean.getTD2() : checkHistoryBean.getTD4();
                                for (ArfcnPciBean bean : td) {
                                    int band_td = LteBand.earfcn2band(Integer.parseInt(bean.getArfcn()));
                                    if (band_td != 5 && band_td != 8) {
                                        isSend = true;
                                        break;
                                    }
                                }
                                if (isSend) {
                                    mHandler.sendEmptyMessageDelayed(1000 + band, 10000);
                                    bandRunOneHandleMap.put(PaCtl.build().isB97502 ? 1 : 3, 1000 + band);
                                }
                            }
                        }else {
                            band = NrBand.earfcn2band(iArfcn);
                            if (band == 79){ 
                                // send不用判断是B97502还是B97501
                                // 也不用管这个通道是否有其他频段，因为仅N79的情况不会走轮循线程，这个10s虽多此一举但不影响
                                mHandler.sendEmptyMessageDelayed(79, 10000);
                                bandRunOneHandleMap.put(0, 79);
                            }
                        }
                    }

                    // 刷新处于工作状态
                    freshDoWorkState(0, cell_id, imsi, traceUtil.getArfcn(cell_id), traceUtil.getPci(cell_id));

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            OpLog.build().write(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + cell_id
                                    + "\t\t开始定位，目标IMSI: " + traceUtil.getImsi(cell_id));
                        }
                    }, 500);

                    switch (cell_id){
                        case 0:
                            if (checkHistoryBean.getTD1().size() > 1)
                                mHandler.sendEmptyMessageDelayed(cell_id, autoArfcnTime * 1000L);
                            break;
                        case 1:
                            if (PaCtl.build().isB97502){
                                String arfcn = traceUtil.getArfcn(cell_id);
                                String arfcn_second = device.getTraceUtil().getArfcn(3);
                                if (LteBand.earfcn2band(Integer.parseInt(arfcn)) == 40 && !arfcn_second.isEmpty() && LteBand.earfcn2band(Integer.parseInt(arfcn_second)) == 40 && device.getTraceUtil().getWorkState(3) != GnbBean.State.TRACE) {
                                    PaCtl.build().openLtePA(id, arfcn, 3); // 双频点模式下，主频的启动失败，次频点启动成功的情况下，把主频点的功放开起来
                                }
                                if (LteBand.earfcn2band(Integer.parseInt(arfcn)) == 3
                                        && checkHistoryBean.getTD4().size() > 0 && device.getTraceUtil().getWorkState(GnbProtocol.CellId.FOURTH) == GnbBean.State.IDLE){
                                    // 即将开的B3，也把通道四的次B3开了
                                    updateParamAndStart(checkHistoryBean.getImsiFourth(), checkHistoryBean.getTD4().get(0).getArfcn(), checkHistoryBean.getTD4().get(0).getPci(), 3);
                                }
                            }
                            if (checkHistoryBean.getTD2().size() > 1)
                                mHandler.sendEmptyMessageDelayed(cell_id, autoArfcnTime * 1000L);
                            break;
                        case 2:
                            if (checkHistoryBean.getTD3().size() > 1)
                                mHandler.sendEmptyMessageDelayed(cell_id, autoArfcnTime * 1000L);
                            break;
                        case 3:
                            if (PaCtl.build().isB97502){
                                String arfcn = traceUtil.getArfcn(cell_id);
                                String arfcn_second = device.getTraceUtil().getArfcn(1);
                                if (LteBand.earfcn2band(Integer.parseInt(arfcn)) == 3 && !arfcn_second.isEmpty() && LteBand.earfcn2band(Integer.parseInt(arfcn_second)) == 3 && device.getTraceUtil().getWorkState(1) != GnbBean.State.TRACE) {
                                    PaCtl.build().openLtePA(id, arfcn, 1); // 双频点模式下，主频的启动失败，次频点启动成功的情况下，把主频点的功放开起来
                                }
                                if (LteBand.earfcn2band(Integer.parseInt(arfcn)) == 40
                                        && checkHistoryBean.getTD2().size() > 0 && device.getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.IDLE){
                                    // 即将开的B40，也把通道二的次B40开了
                                    updateParamAndStart(checkHistoryBean.getImsiSecond(), checkHistoryBean.getTD2().get(0).getArfcn(), checkHistoryBean.getTD2().get(0).getPci(), 1);
                                }
                            }
                            if (checkHistoryBean.getTD4().size() > 1)
                                mHandler.sendEmptyMessageDelayed(cell_id, autoArfcnTime * 1000L);
                            break;
                    }
                } else {
                    if ((isChangeImsi[0] && cell_id == 0) || (isChangeImsi[1] && cell_id == 1) || (isChangeImsi[2] && cell_id == 2) || (isChangeImsi[3] && cell_id == 3)) {
                        MainActivity.getInstance().updateSteps(0, StepBean.State.fail, getString(R.string.change_trace_fail));
                        MainActivity.getInstance().showToast(getString(R.string.cell) + s + getString(R.string.change_trace_fail));
                    } else {
                        MainActivity.getInstance().updateSteps(0, StepBean.State.fail, getString(R.string.open_fail));
                        MainActivity.getInstance().showToast(getString(R.string.cell) + s + getString(R.string.trace_start_fail));
                        updateProgress(0, 90, cell_id, getString(R.string.open_fail), true);
                        device.getTraceUtil().setWorkState(cell_id, GnbBean.State.IDLE);
                    }
                }
                if ((isChangeImsi[0] && cell_id == 0) || (isChangeImsi[1] && cell_id == 1) || (isChangeImsi[2] && cell_id == 2) || (isChangeImsi[3] && cell_id == 3)) {
                    String imsi = device.getTraceUtil().getImsi(cell_id);
                    // 只会有一个是true，在这就不做判断将谁改为false，都置为false
                    isChangeImsi[0] = false;
                    isChangeImsi[1] = false;
                    isChangeImsi[2] = false;
                    isChangeImsi[3] = false;
                    MainActivity.getInstance().showToast(getString(R.string.cell) + s + getString(R.string.change_trace_success));
                    if (cell_id < 3) {
                        String arfcn;
                        int cellID = cell_id;
                        do {
                            cellID++;
                            arfcn = traceUtil.getArfcn(cellID);
                            if (cellID == 3) break;
                        } while (arfcn.isEmpty());
                        if (!arfcn.isEmpty()) {
                            setChangeImsi(cellID, arfcn, imsi);
                        }
                    }
                }
                refreshTraceBtn();
            }
        } else { // IMSI上报及上号报值
            if (rsp.getCellId() != -1) {
                int cell_id = rsp.getCellId();
                // 10秒无上号数据逻辑3   仅自动模式下  有上号则重新计时
                if (checkHistoryBean != null && checkHistoryBean.getMode() != 2 && checkHistoryBean.getMode() != 3 && bandRunOneHandleMap.containsKey(cell_id)){
                    int what = bandRunOneHandleMap.get(cell_id);
                    mHandler.removeMessages(what);
                    mHandler.sendEmptyMessageDelayed(what, 10000);
                }

                String traceArfcn = traceUtil.getArfcn(cell_id);
                String traceImsi = traceUtil.getImsi(cell_id);
                String tracePci = traceUtil.getPci(cell_id);

                if (rsp.getRsrp() < 5 || traceImsi.isEmpty() || traceArfcn.isEmpty() || traceArfcn.equals("0") || tracePci.isEmpty())
                    return;

                List<String> imsiList = rsp.getImsiList();
                if (imsiList != null && imsiList.size() > 0) {
                    for (int i = 0; i < imsiList.size(); i++) {
                        String imsi = imsiList.get(i);
                        int rsrp = rsp.getRsrp();
                        if (rsrp < 5) rsrp = -1;
                        if (traceImsi.equals(imsi) && (device.getTraceUtil().getRsrp(cell_id) < 11 || rsrp != 7)) {
                            device.getTraceUtil().setRsrp(cell_id, rsrp);
                            ParseDataUtil.build().addDataToList(cell_id, rsrp);
                            if (!enableSpecial && rsp.getPhone_type() > 4){
                                enableSpecial = true;
                                mSlideRightFrag.setEnableSpecial();
                            }
                        }

                        boolean add = true;
                        for (int j = 0; j < mImsiList.size(); j++) {
                            if (mImsiList.get(j).getImsi().equals(imsi)/* && mImsiList.get(j).getArfcn().equals(traceArfcn)*/) {
                                add = false;
                                mImsiList.get(j).setRsrp(rsrp);
                                mImsiList.get(j).setArfcn(traceArfcn);
                                mImsiList.get(j).setPci(tracePci);
                                mImsiList.get(j).setUpCount(mImsiList.get(j).getUpCount() + 1);
                                mImsiList.get(j).setLatestTime(System.currentTimeMillis());
                                if (traceImsi.equals(imsi))
                                    mImsiList.get(j).setState(ImsiBean.State.IMSI_NOW);
                                else if (MainActivity.getInstance().isInBlackList(imsi))
                                    mImsiList.get(j).setState(ImsiBean.State.IMSI_BL);
                                break;
                            }
                        }
                        if (add) {
                            if (traceImsi.equals(imsi)) { // 当前定位IMSI
                                mImsiList.add(0, new ImsiBean(ImsiBean.State.IMSI_NOW, imsi, traceArfcn, tracePci, rsrp, System.currentTimeMillis(), cell_id));
                            } else if (MainActivity.getInstance().isInBlackList(imsi)) {
                                mImsiList.add(new ImsiBean(ImsiBean.State.IMSI_BL, imsi, traceArfcn, tracePci, rsrp, System.currentTimeMillis(), cell_id));
                            } else {
                                mImsiList.add(new ImsiBean(ImsiBean.State.IMSI_NEW, imsi, traceArfcn, tracePci, rsrp, System.currentTimeMillis(), cell_id));
                            }
                            mCatchChildFragment.refreshListView();
                        }
                    }
                }
                if (!isStartCatchHandler) {
                    isStartCatchHandler = true;
                    mCatchChildFragment.resetShowData(false); // 刷新视图
                    mHandler.sendEmptyMessageDelayed(9, 1500);
                }
                if (device.getTraceUtil().getWorkState(cell_id) == GnbBean.State.TRACE && !isRsrpStart && rsp.getRsrp() > 5) {
                    countCell[0] = 0;
                    countCell[1] = 0;
                    countCell[2] = 0;
                    countCell[3] = 0;
                    isRsrpStart = true;
                    mHandler.sendEmptyMessageDelayed(8, TraceBean.RSRP_TIME_INTERVAL);
                    AppLog.I("onStartTraceRsp start run 8 handler.");
                }
            }
        }
    }

    @Override
    public void onStopTraceRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onStopTraceRsp():  id = " + id + ", rsp = " + rsp);
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_STOP_TRACE || rsp.getCmdType() == GnbProtocol.UI_2_gNB_STOP_LTE_TRACE) {

                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK){
                    switch (rsp.getCellId()) {
                        case 0:
                            if (autoRun[0] && device.getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST) != GnbBean.State.STOP) {
                                updateParamAndStart(device.getTraceUtil().getImsi(rsp.getCellId()), device.getTraceUtil().getArfcn(rsp.getCellId()), device.getTraceUtil().getPci(rsp.getCellId()), rsp.getCellId());
                                return;
                            }
                            break;
                        case 1:
                            if (autoRun[1] && device.getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) != GnbBean.State.STOP) {
                                updateParamAndStart(device.getTraceUtil().getImsi(rsp.getCellId()), device.getTraceUtil().getArfcn(rsp.getCellId()), device.getTraceUtil().getPci(rsp.getCellId()), rsp.getCellId());
                                if (PaCtl.build().isB97502 && LteBand.earfcn2band(Integer.parseInt(device.getTraceUtil().getArfcn(rsp.getCellId()))) == 3
                                        && checkHistoryBean.getTD4().size() > 0 && device.getTraceUtil().getWorkState(GnbProtocol.CellId.FOURTH) == GnbBean.State.IDLE){
                                    // 即将开的B3，也把通道四的次B3开了
                                    updateParamAndStart(checkHistoryBean.getImsiFourth(), checkHistoryBean.getTD4().get(0).getArfcn(), checkHistoryBean.getTD4().get(0).getPci(), 3);
                                }
                                return;
                            }
                            break;
                        case 2:
                            if (autoRun[2] && device.getTraceUtil().getWorkState(GnbProtocol.CellId.THIRD) != GnbBean.State.STOP) {
                                updateParamAndStart(device.getTraceUtil().getImsi(rsp.getCellId()), device.getTraceUtil().getArfcn(rsp.getCellId()), device.getTraceUtil().getPci(rsp.getCellId()), rsp.getCellId());
                                return;
                            }
                            break;
                        case 3:
                            if (autoRun[3] && device.getTraceUtil().getWorkState(GnbProtocol.CellId.FOURTH) != GnbBean.State.STOP) {
                                updateParamAndStart(device.getTraceUtil().getImsi(rsp.getCellId()), device.getTraceUtil().getArfcn(rsp.getCellId()), device.getTraceUtil().getPci(rsp.getCellId()), rsp.getCellId());
                                if (PaCtl.build().isB97502 && LteBand.earfcn2band(Integer.parseInt(device.getTraceUtil().getArfcn(rsp.getCellId()))) == 40
                                        && checkHistoryBean.getTD2().size() > 0 && device.getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.IDLE){
                                    // 即将开的B40，也把通道二的次B40开了
                                    updateParamAndStart(checkHistoryBean.getImsiSecond(), checkHistoryBean.getTD2().get(0).getArfcn(), checkHistoryBean.getTD2().get(0).getPci(), 1);
                                }
                                return;
                            }
                            break;
                    }
                }

                String s = getCellStr(rsp.getCellId());
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_E_BUSY) {
                    /*String info = getErrInfo(rsp.getCellId(), rsp.getRspValue());
                    MainActivity.getInstance().updateSteps(0, StepBean.State.fail, info);
                    updateProgress(0, 50, rsp.getCellId(), getString(R.string.stop_fail), true);
                    MainActivity.getInstance().showToast(info);*/
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MessageController.build().setCmdAndCellID(id, rsp.getCmdType(), rsp.getCellId());
                        }
                    }, 5000);
                } else {
                    device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.cell) + s + getString(R.string.stop_work_success));
                    updateProgress(0, 0, rsp.getCellId(), getString(R.string.idle), false);
                    if (rsp.getCellId() == 1 && (isStopSecondArfcn[1] || isStopSecondArfcn[3])) {
                        String[] split = getString(isStopSecondArfcn[1] ? R.string.change_b40_stop_second : R.string.start_b39_stop_b8).split("，");
                        mTraceChildFragment.setCfgInfo(rsp.getCellId(), split[0], split[1]);
                        mTraceChildFragment.setRsrpValue(rsp.getCellId(), 0, 0);
                    } else if (rsp.getCellId() == 3 && (isStopSecondArfcn[0] || isStopSecondArfcn[2])) {
                        String[] split = getString(isStopSecondArfcn[0] ? R.string.change_b3_stop_second : R.string.start_b39_stop_b8).split("，");
                        mTraceChildFragment.setCfgInfo(rsp.getCellId(), split[0], split[1]);
                        mTraceChildFragment.setRsrpValue(rsp.getCellId(), 0, 0);
                    } else mTraceChildFragment.setConfigInfo(rsp.getCellId(), 0, "", "", "");
                }
                OpLog.build().write(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + rsp.getCellId() + "\t\t结束定位");
                device.getTraceUtil().setTacChange(rsp.getCellId(), false);
                device.getTraceUtil().setEnable(rsp.getCellId(), false);
                device.getTraceUtil().setRsrp(rsp.getCellId(), 0);
                device.getTraceUtil().setLastRsrp(rsp.getCellId(), -1);
                mHandler.removeMessages(rsp.getCellId());
                refreshTraceBtn();
            }
        }
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
    public void onSetDataTo485(String s, GnbSetDataTo485Rsp gnbSetDataTo485Rsp) {

    }

    @Override
    public void onGetCellCfg(String id, JSONObject jsonObject) {
        AppLog.I("onGetCellCfg():  id = " + id + ", jsonObject = " + jsonObject);
        try {
            int cell_id = jsonObject.getInt("cell_id");
            MessageController.build().recoverTacLoop(id, cell_id);
            tv_do_btn.setEnabled(false);
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
    public void onRedirectUeCfg(String id, GnbCmdRsp rsp) {
        AppLog.I("onStartCatchRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onResetPlmnCfg(String id, GnbCmdRsp rsp) {
        AppLog.I("onStartCatchRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onSetPerPwrCfg(String s, GnbCmdRsp gnbCmdRsp) {

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
        final TraceUtil traceUtil = device.getTraceUtil();
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_SET_BLACK_UE_LIST || rsp.getCmdType() == GnbProtocol.UI_2_gNB_START_LTE_BLACK_UE_LIST) {
                if (isClickStop) {
                    device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    updateProgress(0, 0, rsp.getCellId(), getString(R.string.idle), false);
                    refreshTraceBtn();
                    return;
                }
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    // 发配置定位参数指令
                    int traceTac = PrefUtil.build().getTac();
                    int maxTac = traceTac + GnbProtocol.MAX_TAC_NUM;
                    // 4G tac 不能大于65535， 在这4G 5G共用相同tac，因此做相同处理
                    if (maxTac > 65535) {
                        traceTac = 1234;
                        maxTac = traceTac + GnbProtocol.MAX_TAC_NUM;
                        PrefUtil.build().setTac(maxTac); // 从头再来
                    }
                    final String plmn = traceUtil.getPlmn(rsp.getCellId());
                    final String[] arfcn = {traceUtil.getArfcn(rsp.getCellId())};
                    final String[] pci = {traceUtil.getPci(rsp.getCellId())};
                    //String ue_max_pwr = traceUtil.getUeMaxTxpwr(rsp.getCellId());
                    final int air_sync = traceUtil.getAirSync(rsp.getCellId());
                    final int ssbBitmap = traceUtil.getSsbBitmap(rsp.getCellId());
                    final int bandwidth = traceUtil.getBandWidth(rsp.getCellId());
                    final int cfr = traceUtil.getCfr(rsp.getCellId());
                    final long cid = traceUtil.getCid(rsp.getCellId());
                    int swap_rf = traceUtil.getSwap_rf(rsp.getCellId());
                    //int mob_reject_code = traceUtil.getMobRejectCode(rsp.getCellId());
                    String split_arfcn_dl = traceUtil.getSplit_arfcn_dl(rsp.getCellId());

                    // 第二步，配置频点参数
                    device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.GNB_CFG);

                    updateProgress(0, 30, rsp.getCellId(), getString(R.string.cfging), false);
                    if (Integer.parseInt(arfcn[0]) > 100000) PaCtl.build().initPA(id, arfcn[0]);
                    else PaCtl.build().initLtePA(id, arfcn[0], rsp.getCellId());

                    /*initGnbTrace(int cell_id, int startTac, int maxTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                    	int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                      int band_width, int cfr_enable, int swap_rf)
                    定位配置频点参数
                    mod_reject_code = 0(正常)、9（强上号）
                    split_arfcn_dl   载波分裂频点*/
                    int finalTraceTac = traceTac;
                    int finalMaxTac = maxTac;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int iArfcn = Integer.parseInt(arfcn[0]);
                            int time_offset = 0;
                            int band;
                            if (iArfcn > 100000) {
                                band = NrBand.earfcn2band(iArfcn);
                                if (band == 28 || band == 41 || band == 79) time_offset = GnbCity.build().getTimingOffset("5G");
                            } else {
                                band = LteBand.earfcn2band(iArfcn);
                                switch (band) {
                                    case 34:
                                        time_offset = GnbCity.build().getTimingOffset("B34");
                                        break;
                                    case 39:
                                        time_offset = GnbCity.build().getTimingOffset("B39");
                                        break;
                                    case 40:
                                        time_offset = GnbCity.build().getTimingOffset("B40");
                                        break;
                                    case 41:
                                        time_offset = GnbCity.build().getTimingOffset("B41");
                                        break;
                                }
                            }

                            boolean mob_reject_code1 = (boolean) PrefUtil.build().getValue("mob_reject_code", false);
                            int mob_reject_code = mob_reject_code1 ? 9 : 0;
                            if (iArfcn > 100000) {
                                MessageController.build().initGnbTrace(id, rsp.getCellId(), finalTraceTac, finalMaxTac, plmn, arfcn[0], pci[0], String.valueOf(ueMax[0] * 5 + 10),
                                        time_offset, 1, air_sync, "0", 9,
                                        cid, ssbBitmap, bandwidth, cfr, swap_rf, 15, -70, mob_reject_code, split_arfcn_dl);
                            } else {
                                MessageController.build().initGnbLteTrace(id, rsp.getCellId(), finalTraceTac, finalMaxTac, plmn, arfcn[0], pci[0], String.valueOf(ueMax[1] * 5 + 10),
                                        time_offset, 1, air_sync, "0", 9,
                                        cid, ssbBitmap, bandwidth, cfr, swap_rf, 15, -70, mob_reject_code, split_arfcn_dl);
                            }
                            int finalTime_offset = time_offset;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    String msg = "plmn = " + plmn + ", arfcn = " + arfcn[0] + ", pci = " + pci[0] + ", cid = " + cid
                                            + ", time offfset = " + finalTime_offset;
                                    OpLog.build().write(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + rsp.getCellId() + "\t\t定位参数：" + msg);
                                }
                            }, 300);
                        }
                    }, 300);
                } else if (rsp.getRspValue() == GnbProtocol.OAM_ACK_E_BUSY) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendBlackList(id, rsp.getCellId());
                        }
                    }, 5000);
                } else {
                    String s = getErrInfo(rsp.getCellId(), rsp.getRspValue());
                    MainActivity.getInstance().updateSteps(0, StepBean.State.fail, s);
                    updateProgress(0, 30, rsp.getCellId(), getString(R.string.cfging), true);
                    MainActivity.getInstance().showToast(s);
                    device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    refreshTraceBtn();
                }
            }
        }
    }

    private String getErrInfo(int cellId, int code) {
        String info = getString(R.string.cell) + getCellStr(cellId);
        switch (code) {
            case 3:
                info += getString(R.string.system_busy);
                break;
            case 6:
                info += getString(R.string.air_sync_fail);
                break;
            case 7:
                info += getString(R.string.gps_unlock);
                break;
            default:
                info += getString(R.string.cfg_trace_fail);
                break;
        }
        return info;
    }

    int[] autoArfcnIndex = new int[]{0, 0, 0, 0};
    int[] runFailCount = new int[]{0, 0, 0, 0};
    boolean[] autoRun = new boolean[]{false, false, false, false};

    @Override
    public void onSetGnbRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetGnbRsp():  id = " + id + ", rsp = " + rsp);
        final TraceUtil traceUtil = device.getTraceUtil();
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_CFG_gNB || rsp.getCmdType() == GnbProtocol.UI_2_eNB_CFG_gNB) { //UI_2_gNB_CFG_gNB = 10 配置频点参数
                if (isClickStop) {
                    device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    updateProgress(0, 0, rsp.getCellId(), getString(R.string.idle), false);
                    refreshTraceBtn();
                    return;
                }
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    if (/*!traceUtil.isTacChange(rsp.getCellId())
                            && */traceUtil.getWorkState(rsp.getCellId()) == GnbBean.State.GNB_CFG) {

                        updateProgress(0, 50, rsp.getCellId(), getString(R.string.cfging), false);
                        //第三步.设置功率衰减
                        //MessageController.build().setTraceType(id, GnbProtocol.TraceType.STARTTRACE);
                        device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.CFG_TRACE);
                        MessageController.build().setTxPwrOffset(id, rsp.getCellId(), Integer.parseInt(traceUtil.getArfcn(rsp.getCellId())), 0);

                        if (!device.getTraceUtil().getImsi(rsp.getCellId()).startsWith("460")){
                            ArrayList<String> plmns = new ArrayList<>();
                            plmns.add("52005");
                            plmns.add("52018");
                            plmns.add("52047");
                            plmns.add("52017");
                            plmns.add("52003");
                            plmns.add("52001");
                            plmns.add("52015");
                            plmns.add("52004");
                            plmns.add("52099");
                            plmns.add("52000");
                            MessageController.build().resetPlmn(id, rsp.getCellId(), rsp.getCmdType() == GnbProtocol.UI_2_eNB_CFG_gNB, plmns);
                        }
                    }
                } else {
                    switch (rsp.getRspValue()) {
                        case 6:
                            switch (rsp.getCellId()) {
                                case 0:
                                    syncFailGoNext(rsp.getCellId(), checkHistoryBean.getTD1(), checkHistoryBean.getImsiFirst());
                                    break;
                                case 1:
                                    syncFailGoNext(rsp.getCellId(), checkHistoryBean.getTD2(), checkHistoryBean.getImsiSecond());
                                    break;
                                case 2:
                                    syncFailGoNext(rsp.getCellId(), checkHistoryBean.getTD3(), checkHistoryBean.getImsiThird());
                                    break;
                                case 3:
                                    syncFailGoNext(rsp.getCellId(), checkHistoryBean.getTD4(), checkHistoryBean.getImsiFourth());
                                    break;
                            }
                            break;
                        case 2:
                            AppLog.E("Tac change fail, param err cell = " + rsp.getCellId());
                            break;
                        case 3:
                            AppLog.E("Tac change fail, system busy err cell = " + rsp.getCellId());
                            break;
                        case 5:
                            AppLog.E("Tac change fail, system err cell = " + rsp.getCellId());
                            //device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.STOP);
                            //MessageController.build().setCmdAndCellID(id, device.getTraceUtil().getArfcn(rsp.getCellId()).length() > 5 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, rsp.getCellId());
                            break;
                        case 11:
                            AppLog.E("Tac change fail, cell is in cfg. cell = " + rsp.getCellId());
                            break;
                        default:
                            device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                            String info = getErrInfo(rsp.getCellId(), rsp.getRspValue());
                            MainActivity.getInstance().updateSteps(0, StepBean.State.fail, info);
                            updateProgress(0, 50, rsp.getCellId(), getString(R.string.start_fail), true);
                            mTraceChildFragment.setCfgInfo(rsp.getCellId(), getString(R.string.start_fail), info);
                            showToast(info);
                            break;
                    }
                    refreshTraceBtn();
                }
            }
        }
    }

    private void syncFailGoNext(int cell_id, LinkedList<ArfcnPciBean> td, String imsi){
        String errInfo = getString(R.string.air_sync_fail);
        ArfcnPciBean bean;
        // 循环两次，失败则提示失败
        if (autoArfcnIndex[cell_id] >= td.size() && runFailCount[cell_id] > 1) {
            MainActivity.getInstance().updateSteps(0, StepBean.State.fail, errInfo);
            updateProgress(0, 50, cell_id, getString(R.string.start_fail), true);
            mTraceChildFragment.setCfgInfo(cell_id, getString(R.string.start_fail), getString(R.string.air_sync_fail));
            MainActivity.getInstance().showToast(errInfo);
            device.getTraceUtil().setWorkState(cell_id, GnbBean.State.IDLE);
        } else {
            if (autoArfcnIndex[cell_id] >= td.size()) autoArfcnIndex[cell_id] = 0;
            bean = td.get(autoArfcnIndex[cell_id]);
            AppLog.D("cell " + cell_id + " sync fail reset arfcn/pci = " + bean.toString() + ", runFailCount = " + runFailCount[cell_id] + ", autoArfcnIndex = " + autoArfcnIndex[cell_id]);
            updateParamAndStart(imsi, bean, cell_id);
            autoArfcnIndex[cell_id]++;
            runFailCount[cell_id]++;
        }
    }

    private void updateParamAndStart(String imsi, ArfcnPciBean bean, int first) {
        updateParamAndStart(imsi, bean.getArfcn(), bean.getPci(), first);
    }
    private void updateParamAndStart(String imsi, String arfcn, String pci, int cell_id) {
        if (arfcn.isEmpty()){
            AppLog.E("updateParamAndStart Arfcn isEmpty.");
            return;
        }
        mTraceChildFragment.setCfgInfo(cell_id, arfcn + "/" + pci, mContext.getString(R.string.cfging));
        int band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        device.getTraceUtil().setArfcn(cell_id, arfcn);
        device.getTraceUtil().setPci(cell_id, pci);
        switch (cell_id) {
            case 0:
                device.getTraceUtil().setImsi(cell_id, imsi);
                if (imsi.startsWith("460")) device.getTraceUtil().setPlmn(GnbProtocol.CellId.FIRST, band == 78 ? "46001" : "46000");
                else device.getTraceUtil().setPlmn(GnbProtocol.CellId.FIRST, imsi.substring(0, 5));

                if (PaCtl.build().isB97502) { // N41/N78/N79
                    device.getTraceUtil().setBandWidth(GnbProtocol.CellId.FIRST, 100);
                    device.getTraceUtil().setSsbBitmap(GnbProtocol.CellId.FIRST, 255);
                } else { // N28/N78/N79
                    device.getTraceUtil().setBandWidth(GnbProtocol.CellId.FIRST, band == 28 ? 20 : 100);
                    device.getTraceUtil().setSsbBitmap(GnbProtocol.CellId.FIRST, band == 28 ? 240 : 255);
                }
                break;
            case 1:
                device.getTraceUtil().setImsi(cell_id, imsi);
                device.getTraceUtil().setBandWidth(GnbProtocol.CellId.SECOND, 5);
                device.getTraceUtil().setSsbBitmap(GnbProtocol.CellId.SECOND, 128);
                if (PaCtl.build().isB97502) { // B3/B5/B8/B40
                    int freq = LteBand.earfcn2freq(Integer.parseInt(arfcn));
                    int bandLte = LteBand.earfcn2band(Integer.parseInt(arfcn));
                    if (imsi.startsWith("460")){
                        if (bandLte == 40 || (freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949))
                            device.getTraceUtil().setPlmn(GnbProtocol.CellId.SECOND, "46000");
                        else device.getTraceUtil().setPlmn(GnbProtocol.CellId.SECOND, "46001");
                    }else device.getTraceUtil().setPlmn(GnbProtocol.CellId.SECOND, imsi.substring(0, 5));
                } else { // B34/B39/B40/B41
                    if (imsi.startsWith("460")) device.getTraceUtil().setPlmn(GnbProtocol.CellId.SECOND, "46000");
                    else device.getTraceUtil().setPlmn(GnbProtocol.CellId.SECOND, imsi.substring(0, 5));
                }
                break;
            case 2:
                device.getTraceUtil().setImsi(cell_id, imsi);
                if (imsi.startsWith("460")) device.getTraceUtil().setPlmn(GnbProtocol.CellId.THIRD, band == 1 ? "46001" : "46000");
                else device.getTraceUtil().setPlmn(GnbProtocol.CellId.THIRD, imsi.substring(0, 5));
                if (PaCtl.build().isB97502) { // // B1/N1/N28
                    if (arfcn.length() < 6){
                        device.getTraceUtil().setBandWidth(GnbProtocol.CellId.THIRD, 5);
                        device.getTraceUtil().setSsbBitmap(GnbProtocol.CellId.THIRD, 128);
                        if (imsi.startsWith("460")) device.getTraceUtil().setPlmn(GnbProtocol.CellId.THIRD, "46001");
                        else device.getTraceUtil().setPlmn(GnbProtocol.CellId.THIRD, imsi.substring(0, 5));
                    }else {
                        device.getTraceUtil().setBandWidth(GnbProtocol.CellId.THIRD, 20);
                        device.getTraceUtil().setSsbBitmap(GnbProtocol.CellId.THIRD, 240);
                    }
                } else { // N1/N41
                    device.getTraceUtil().setBandWidth(GnbProtocol.CellId.THIRD, band == 1 ? 20 : 100);
                    device.getTraceUtil().setSsbBitmap(GnbProtocol.CellId.THIRD, band == 1 ? 240 : 255);
                }
                break;
            case 3:
                device.getTraceUtil().setImsi(cell_id, imsi);
                device.getTraceUtil().setBandWidth(GnbProtocol.CellId.FOURTH, 5);
                device.getTraceUtil().setSsbBitmap(GnbProtocol.CellId.FOURTH, 128);

                if (PaCtl.build().isB97502 && LteBand.earfcn2band(Integer.parseInt(arfcn)) != 3) {
                    if (imsi.startsWith("460")) device.getTraceUtil().setPlmn(GnbProtocol.CellId.FOURTH, "46000");
                    else device.getTraceUtil().setPlmn(GnbProtocol.CellId.FOURTH, imsi.substring(0, 5));
                } else {
                    int freq = LteBand.earfcn2freq(Integer.parseInt(arfcn));
                    if (imsi.startsWith("460")) {
                        if ((freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949))
                            device.getTraceUtil().setPlmn(GnbProtocol.CellId.FOURTH, "46000");
                        else device.getTraceUtil().setPlmn(GnbProtocol.CellId.FOURTH, "46001");
                    }else device.getTraceUtil().setPlmn(GnbProtocol.CellId.FOURTH, imsi.substring(0, 5));

                }
                break;
        }
        // 第一步，配置黑名单
        device.getTraceUtil().setWorkState(cell_id, GnbBean.State.BLACKLIST);
        //发送配置黑名单指令
        sendBlackList(device.getRsp().getDeviceId(), cell_id);
    }

    private void sendBlackList(String id, int cellId) {
        final List<UeidBean> blackList = new ArrayList<>();
        for (MyUeidBean bean : MainActivity.getInstance().getBlackList())
            blackList.add(bean.getUeidBean());
        MessageController.build().setBlackList(id, device.getTraceUtil().getArfcn(cellId).length() < 6, cellId, blackList.size(), blackList);
    }

    @Override
    public void onSetTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetTxPwrOffsetRsp():  id = " + id + ", rsp = " + rsp);
        final TraceUtil traceUtil = device.getTraceUtil();
        if (rsp != null) {
            //int traceType = MessageController.build().getTraceType(id);
            int traceType = traceUtil.getWorkState(rsp.getCellId());
            AppLog.I("onSetTxPwrOffsetRsp get TraceType = " + traceType);
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_SET_TX_POWER_OFFSET) {
                if (isClickStop) {
                    device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    updateProgress(0, 0, rsp.getCellId(), getString(R.string.idle), false);
                    refreshTraceBtn();
                    return;
                }
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    if (traceType == GnbBean.State.CFG_TRACE) {
                        //4、开pa
                        if (device.getTraceUtil().getArfcn(rsp.getCellId()).length() > 5)
                            PaCtl.build().openPA(id, traceUtil.getArfcn(rsp.getCellId())); // 开PA
                        else
                            PaCtl.build().openLtePA(id, traceUtil.getArfcn(rsp.getCellId()), rsp.getCellId()); // 开Lte PA
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 5、发开始定位指令
                                device.getTraceUtil().setRsrp(rsp.getCellId(), 0);
                                device.getTraceUtil().setTacChange(rsp.getCellId(), true);
                                //第四步.启动定位
                                updateProgress(0, 70, rsp.getCellId(), getString(R.string.cfging), false);

                                device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.CFG_TRACE);
                                if (device.getTraceUtil().getArfcn(rsp.getCellId()).length() > 5)
                                    MessageController.build().startTrace(id, rsp.getCellId(), traceUtil.getImsi(rsp.getCellId()), 1);
                                else
                                    MessageController.build().startLteTrace(id, rsp.getCellId(), traceUtil.getImsi(rsp.getCellId()), 1);
                            }
                        }, 300);
                    }
                } else {
                    if (traceType == GnbBean.State.CFG_TRACE) {
                        String info = getErrInfo(rsp.getCellId(), rsp.getRspValue());
                        MainActivity.getInstance().updateSteps(0, StepBean.State.fail, info);
                        updateProgress(0, 70, rsp.getCellId(), getString(R.string.open_fail), true);
                        MainActivity.getInstance().showToast(info);

                        refreshTraceBtn();
                    } else {
                        MainActivity.getInstance().updateSteps(0, StepBean.State.fail, getString(R.string.cell) + getCellStr(rsp.getCellId()) + getString(R.string.gain_fail));
                        MainActivity.getInstance().showToast(getString(R.string.gain_fail));
                    }
                }
                //MessageController.build().setTraceType(id, GnbProtocol.TraceType.TRACE);
            }
        }
    }

    @Override
    public void onSetNvTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetNvTxPwrOffsetRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onSetTimeRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetTimeRsp():  id = " + id + ", rsp = " + rsp);
        MessageController.build().setOnlyCmd(id, GnbProtocol.UI_2_gNB_QUERY_gNB_VERSION); // 初次，读取一次版本
    }

    @Override
    public void onSetRebootRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetRebootRsp():  id = " + id + ", rsp = " + rsp);
        mSlideRightFrag.onSetRebootRsp(id, rsp);
    }

    @Override
    public void onFirmwareUpgradeRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onFirmwareUpgradeRsp():  id = " + id + ", rsp = " + rsp);
        mSlideRightFrag.onFirmwareUpgradeRsp(id, rsp);
    }

    @Override
    public void onSetWifiInfoRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetWifiInfoRsp():  id = " + id + ", rsp = " + rsp);
    }

    @Override
    public void onGetLogRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onGetLogRsp():  id = " + id + ", rsp = " + rsp);
        mSlideRightFrag.onGetLogRsp(id, rsp);
    }

    @Override
    public void onGetOpLogRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onGetOpLogRsp():  id = " + id + ", rsp = " + rsp);
        mSlideRightFrag.onGetOpLogRsp(id, rsp);
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
        if (isInFreqForPci){
            if (rsp != null) {
                if (!isClickStop) {
                    if (rsp.getReportStep() == 2) freqScan(id);
                }
                if (rsp.getScanResult() == GnbProtocol.OAM_ACK_OK && rsp.getReportStep() == 1) {
                    AppLog.I("onFreqScanRsp() isClickStop " + isClickStop + ", arfcn = " + rsp.getUl_arfcn() + ", Rsrp = " + rsp.getRsrp());
                    if (freqList.size() == 0) {
                        freqList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
                                rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
                                rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
                    } else {
                        boolean isAdd = true;
                        for (int i = 0; i < freqList.size(); i++) {
                            if (freqList.get(i).getUl_arfcn() == rsp.getUl_arfcn() &&
                                    freqList.get(i).getPci() == rsp.getPci()) {
                                isAdd = false;
                                freqList.remove(freqList.get(i));
                                ScanArfcnBean arfcnBean = new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
                                        rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
                                        rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth());
                                if (rsp.getTac().isEmpty() || rsp.getEci().isEmpty()){
                                    freqList.add(arfcnBean);
                                }else {
                                    freqList.add(0, arfcnBean);
                                }
                                break;
                            }
                        }
                        if (isAdd) {
                            ScanArfcnBean arfcnBean = new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
                                    rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
                                    rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth());
                            if (rsp.getTac().isEmpty() || rsp.getEci().isEmpty()){
                                freqList.add(arfcnBean);
                            }else {
                                freqList.add(0, arfcnBean);
                            }
                        }
                    }
                }
            }
        }
        if (mAutoFreqDialog != null) mAutoFreqDialog.onFreqScanRsp(id, rsp);
        mSlideRightFrag.onFreqScanRsp(id, rsp);
        //mFreqFragment.onFreqScanRsp(id, rsp);
    }

    @Override
    public void onFreqScanGetDocumentRsp(String id, GnbFreqScanGetDocumentRsp rsp) {
        AppLog.I("onFreqScanGetDocumentRsp():  id = " + id + ", rsp = " + rsp);
        if (rsp != null) mSlideRightFrag.onFreqScanGetDocumentRsp(id, rsp);
    }

    @Override
    public void onStopFreqScanRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onStopFreqScanRsp():  id = " + id + ", rsp = " + rsp);
        //mFreqFragment.onStopFreqScanRsp(id, rsp);
    }

    @Override
    public void onStartTdMeasure(String id, GnbCmdRsp rsp) {
        AppLog.I("onStartTdMeasure():  id = " + id + ", rsp = " + rsp);
        if (rsp != null) mSlideRightFrag.onStartTdMeasure(id, rsp);
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
        if (device == null || !device.getRsp().getDeviceId().equals(id)) return;

        if (newState == ConnectProtocol.SOCKET.STATE_CONNECTING) {
            updateConnectState(0, 101);
            updateSteps(0, StepBean.State.success, getString(R.string.connecting_dev));
        } else if (newState == ConnectProtocol.SOCKET.STATE_CONNECTED) {
            mHandler.removeMessages(7);

            if (device.getWorkState() == GnbBean.State.NONE) {
                device.setWorkState(GnbBean.State.IDLE);

                MessageController.build().setGnbTime(id); // 断线、重启，再同步单板时间
            }

            updateConnectState(0, 103);
            setDevState(0, getString(R.string.connected));
            updateSteps(0, StepBean.State.success, getString(R.string.connected_dev));
            tv_do_btn.setEnabled(false);
            mHandler.removeMessages(999);
            mHandler.removeMessages(998);
            Message message = new Message();
            message.what = 998;
            message.obj = id;
            mHandler.sendMessageDelayed(message, 2000);
        } else {
            updateConnectState(0, 100);
            initProgress(0);
            device.setWorkState(GnbBean.State.NONE);
            setBtnStr(false);
            mSlideRightFrag.setDeviceDis(id);
            if (mAutoFreqDialog != null) {
                mAutoFreqDialog.dismiss();
                mAutoFreqDialog = null;
            }
            String dis_str = getString(R.string.disconnect_dev);
            updateSteps(0, StepBean.State.fail, dis_str);
            setDevState(-1, getString(R.string.not_ready));
            showToast(dis_str);
            Message message = new Message();
            message.what = 7;
            message.obj = id;
            mHandler.sendMessageDelayed(message, 90 * 1000);
        }
    }

    boolean isStartCatchHandler = false;
    boolean[] isStopSecondArfcn = {false, false, false, false};
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (device != null && countCell[0] > autoArfcnTime && autoArfcnTime != 0 && device.getTraceUtil().getWorkState(0) == GnbBean.State.TRACE) {
                        if (autoArfcnIndex[0] >= checkHistoryBean.getTD1().size()) autoArfcnIndex[0] = 0;
                        for (int i = 0; i < checkHistoryBean.getTD1().size(); i++) {
                            if (i >= autoArfcnIndex[0]) {
                                String value = checkHistoryBean.getTD1().get(i).getArfcn();
                                String pci = checkHistoryBean.getTD1().get(i).getPci();
                                int band = NrBand.earfcn2band(Integer.parseInt(value));
                                if (band == 79 && !bandRunOneMap.get("N79")){
                                    autoArfcnIndex[0]++;
                                    mHandler.sendEmptyMessage(0);
                                    return;
                                }
                                if (band == NrBand.earfcn2band(Integer.parseInt(device.getTraceUtil().getArfcn(GnbProtocol.CellId.FIRST)))) {
                                    AppLog.D("mHandler 0 change arfcn = " + value + ", autoArfcnIndex = " + autoArfcnIndex[0]);
                                    mTraceChildFragment.setConfigInfo(GnbProtocol.CellId.FIRST, 0, value, pci, device.getTraceUtil().getImsi(GnbProtocol.CellId.FIRST));
                                    MessageController.build().setArfcn(device.getRsp().getDeviceId(), 0, value);
                                    MessageController.build().setPci(device.getRsp().getDeviceId(), 0, pci);
                                    autoArfcnIndex[0]++;
                                } else {
                                    AppLog.D("mHandler 0 change arfcn = " + value + " is different band, stop at first" + ", autoArfcnIndex = " + autoArfcnIndex[0]);
                                    autoRun[0] = true;
                                    String deviceId = device.getRsp().getDeviceId();
                                    MessageController.build().setCmdAndCellID(deviceId, GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.FIRST);
                                    PaCtl.build().closePAByArfcn(deviceId, device.getTraceUtil().getArfcn(GnbProtocol.CellId.FIRST));
                                    device.getTraceUtil().setArfcn(msg.what, value);
                                    device.getTraceUtil().setPci(msg.what, pci);
                                    autoArfcnIndex[0]++;
                                    return;
                                }
                                break;
                            }
                        }
                    }
                    if (autoArfcnTime == 0) mHandler.sendEmptyMessageDelayed(0, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(0, autoArfcnTime * 1000L);
                    break;
                case 1:
                    if (device != null && countCell[1] > autoArfcnTime && autoArfcnTime != 0 && device.getTraceUtil().getWorkState(1) == GnbBean.State.TRACE) {
                        if (autoArfcnIndex[1] >= checkHistoryBean.getTD2().size()) autoArfcnIndex[1] = 0;
                        for (int i = 0; i < checkHistoryBean.getTD2().size(); i++) {
                            if (i >= autoArfcnIndex[1]) {
                                String value = checkHistoryBean.getTD2().get(i).getArfcn();
                                String arfcn = device.getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND);
                                String pci = checkHistoryBean.getTD2().get(i).getPci();
                                int band = LteBand.earfcn2band(Integer.parseInt(value));
                                boolean isReturn = false;
                                if (PaCtl.build().isB97502){
                                    if ((band == 5 && !bandRunOneMap.get("B5")) || (band == 8 && !bandRunOneMap.get("B8"))) isReturn = true;
                                }else {
                                    if (band == 34 && !bandRunOneMap.get("B34")) isReturn = true;
                                }
                                if (isReturn){
                                    autoArfcnIndex[1]++;
                                    mHandler.sendEmptyMessage(1);
                                    return;
                                }

                                if (band == LteBand.earfcn2band(Integer.parseInt(arfcn))) {
                                    int freqValue = LteBand.earfcn2freq(Integer.parseInt(value));
                                    int freqArfcn = LteBand.earfcn2freq(Integer.parseInt(arfcn));
                                    if (!PaCtl.build().isB97502 || (((freqArfcn > 1709 && freqArfcn < 1735) || (freqArfcn > 1804 && freqArfcn < 1830)) && (freqValue > 1709 && freqValue < 1735) || (freqValue > 1804 && freqValue < 1830)
                                            || ((freqArfcn > 888 && freqArfcn < 904) || (freqArfcn > 933 && freqArfcn < 949)) && (freqValue > 888 && freqValue < 904) || (freqValue > 933 && freqValue < 949))) {
                                        AppLog.D("mHandler 1 change arfcn = " + value + ", autoArfcnIndex = " + autoArfcnIndex[1]);
                                        mTraceChildFragment.setConfigInfo(GnbProtocol.CellId.SECOND, 0, value, pci, device.getTraceUtil().getImsi(GnbProtocol.CellId.SECOND));
                                        MessageController.build().setArfcn(device.getRsp().getDeviceId(), 1, value);
                                        MessageController.build().setPci(device.getRsp().getDeviceId(), 1, pci);
                                    } else {
                                        AppLog.D("mHandler 1 change need stop, arfcn = " + value + ", autoArfcnIndex = " + autoArfcnIndex[1]);
                                        autoRun[1] = true;
                                        String deviceId = device.getRsp().getDeviceId();
                                        MessageController.build().setCmdAndCellID(deviceId, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.SECOND);
                                        PaCtl.build().closePAByArfcn(deviceId, device.getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND));
                                        device.getTraceUtil().setArfcn(msg.what, value);
                                        device.getTraceUtil().setPci(msg.what, pci);
                                    }
                                    autoArfcnIndex[1]++;
                                } else {
                                    AppLog.D("mHandler 1 change arfcn = " + value + " is different band, stop at first" + ", autoArfcnIndex = " + autoArfcnIndex[1]);
                                    autoRun[1] = true;
                                    String deviceId = device.getRsp().getDeviceId();
                                    if (PaCtl.build().isB97502){
                                        band = LteBand.earfcn2band(Integer.parseInt(arfcn));
                                        AppLog.D("mHandler 1 change arfcn = " + band + ", getWorkState(GnbProtocol.CellId.FOURTH) = " + device.getTraceUtil().getWorkState(GnbProtocol.CellId.FOURTH));
                                        if (3 == band && device.getTraceUtil().getWorkState(GnbProtocol.CellId.FOURTH) == GnbBean.State.TRACE && (LteBand.earfcn2band(Integer.parseInt(device.getTraceUtil().getArfcn(GnbProtocol.CellId.FOURTH)))) == 3){
                                            // 即将开的非B3，关闭正在跑的为B3，也把通道四的次B3关了
                                            isStopSecondArfcn[0] = true;
                                            MessageController.build().setCmdAndCellID(deviceId, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FOURTH);
                                            PaCtl.build().closePAByArfcn(deviceId, device.getTraceUtil().getArfcn(GnbProtocol.CellId.FOURTH));
                                        }
                                    }else {
                                        band = LteBand.earfcn2band(Integer.parseInt(arfcn));
                                        AppLog.D("mHandler 1 change arfcn = " + band + ", getWorkState(GnbProtocol.CellId.FOURTH) = " + device.getTraceUtil().getWorkState(GnbProtocol.CellId.FOURTH));
                                        if (39 == band && device.getTraceUtil().getWorkState(GnbProtocol.CellId.FOURTH) == GnbBean.State.TRACE && (LteBand.earfcn2band(Integer.parseInt(device.getTraceUtil().getArfcn(GnbProtocol.CellId.FOURTH)))) == 8){
                                            // 即将开B39，把通道四的B8关了
                                            isStopSecondArfcn[2] = true;
                                            MessageController.build().setCmdAndCellID(deviceId, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FOURTH);
                                            PaCtl.build().closePAByArfcn(deviceId, device.getTraceUtil().getArfcn(GnbProtocol.CellId.FOURTH));
                                        }
                                    }
                                    MessageController.build().setCmdAndCellID(deviceId, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.SECOND);
                                    PaCtl.build().closePAByArfcn(deviceId, device.getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND));
                                    device.getTraceUtil().setArfcn(msg.what, value);
                                    device.getTraceUtil().setPci(msg.what, pci);
                                    autoArfcnIndex[1]++;
                                    return;
                                }
                                break;
                            }
                        }
                    }
                    if (autoArfcnTime == 0) mHandler.sendEmptyMessageDelayed(1, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(1, autoArfcnTime * 1000L);
                    break;
                case 2:
                    if (device != null && countCell[2] > autoArfcnTime && autoArfcnTime != 0 && device.getTraceUtil().getWorkState(2) == GnbBean.State.TRACE) {
                        if (autoArfcnIndex[2] >= checkHistoryBean.getTD3().size()) autoArfcnIndex[2] = 0;
                        for (int i = 0; i < checkHistoryBean.getTD3().size(); i++) {
                            if (i >= autoArfcnIndex[2]) {
                                String value = checkHistoryBean.getTD3().get(i).getArfcn();
                                String pci = checkHistoryBean.getTD3().get(i).getPci();
                                // B97502会有B1的情况，在这判断也直接用NrBand.earfcn2band，0 ！= 5G其他，因此让其走结束再启动
                                if (NrBand.earfcn2band(Integer.parseInt(value)) == NrBand.earfcn2band(Integer.parseInt(device.getTraceUtil().getArfcn(GnbProtocol.CellId.THIRD)))) {
                                    AppLog.D("mHandler 2 change arfcn = " + value + ", autoArfcnIndex = " + autoArfcnIndex[2]);
                                    mTraceChildFragment.setConfigInfo(GnbProtocol.CellId.THIRD, 0, value, pci, device.getTraceUtil().getImsi(GnbProtocol.CellId.THIRD));
                                    MessageController.build().setArfcn(device.getRsp().getDeviceId(), 2, value);
                                    MessageController.build().setPci(device.getRsp().getDeviceId(), 2, pci);
                                    autoArfcnIndex[2]++;
                                } else {
                                    AppLog.D("mHandler 2 change arfcn = " + value + " is different band, stop at first" + ", autoArfcnIndex = " + autoArfcnIndex[2]);
                                    autoRun[2] = true;
                                    String deviceId = device.getRsp().getDeviceId();
                                    MessageController.build().setCmdAndCellID(deviceId, device.getTraceUtil().getArfcn(GnbProtocol.CellId.THIRD).length() < 6 ? GnbProtocol.UI_2_gNB_STOP_LTE_TRACE : GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.THIRD);
                                    PaCtl.build().closePAByArfcn(deviceId, device.getTraceUtil().getArfcn(GnbProtocol.CellId.THIRD));
                                    device.getTraceUtil().setArfcn(msg.what, value);
                                    device.getTraceUtil().setPci(msg.what, pci);
                                    autoArfcnIndex[2]++;
                                    return;
                                }
                                break;
                            }
                        }
                    }
                    if (autoArfcnTime == 0) mHandler.sendEmptyMessageDelayed(2, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(2, autoArfcnTime * 1000L);
                    break;
                case 3:
                    if (device != null && countCell[3] > autoArfcnTime && autoArfcnTime != 0 && device.getTraceUtil().getWorkState(3) == GnbBean.State.TRACE) {
                        if (autoArfcnIndex[3] >= checkHistoryBean.getTD4().size()) autoArfcnIndex[3] = 0;
                        for (int i = 0; i < checkHistoryBean.getTD4().size(); i++) {
                            if (i >= autoArfcnIndex[3]) {
                                String value = checkHistoryBean.getTD4().get(i).getArfcn();
                                String arfcn = device.getTraceUtil().getArfcn(GnbProtocol.CellId.FOURTH);
                                String pci = checkHistoryBean.getTD4().get(i).getPci();
                                int band = LteBand.earfcn2band(Integer.parseInt(value));
                                boolean isReturn = false;
                                if (!PaCtl.build().isB97502){
                                    if ((band == 5 && !bandRunOneMap.get("B5")) || (band == 8 && !bandRunOneMap.get("B8"))) isReturn = true;
                                }else {
                                    if (band == 34 && !bandRunOneMap.get("B34")) isReturn = true;
                                }
                                if (isReturn){
                                    autoArfcnIndex[3]++;
                                    mHandler.sendEmptyMessage(3);
                                    return;
                                }
                                if (LteBand.earfcn2band(Integer.parseInt(value)) == LteBand.earfcn2band(Integer.parseInt(arfcn))) {

                                    int freqValue = LteBand.earfcn2freq(Integer.parseInt(value));
                                    int freqArfcn = LteBand.earfcn2freq(Integer.parseInt(arfcn));

                                    if (PaCtl.build().isB97502 || (((freqArfcn > 1709 && freqArfcn < 1735) || (freqArfcn > 1804 && freqArfcn < 1830)) && (freqValue > 1709 && freqValue < 1735) || (freqValue > 1804 && freqValue < 1830)
                                            || ((freqArfcn > 888 && freqArfcn < 904) || (freqArfcn > 933 && freqArfcn < 949)) && (freqValue > 888 && freqValue < 904) || (freqValue > 933 && freqValue < 949))) {
                                        AppLog.D("mHandler 3 change arfcn = " + value + ", autoArfcnIndex = " + autoArfcnIndex[3]);
                                        mTraceChildFragment.setConfigInfo(GnbProtocol.CellId.FOURTH, 0, value, pci, device.getTraceUtil().getImsi(GnbProtocol.CellId.FOURTH));
                                        MessageController.build().setArfcn(device.getRsp().getDeviceId(), 3, value);
                                        MessageController.build().setPci(device.getRsp().getDeviceId(), 3, pci);
                                    } else {
                                        AppLog.D("mHandler 3 change need stop, arfcn = " + value + ", autoArfcnIndex = " + autoArfcnIndex[3]);
                                        autoRun[3] = true;
                                        String deviceId = device.getRsp().getDeviceId();
                                        MessageController.build().setCmdAndCellID(deviceId, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FOURTH);
                                        PaCtl.build().closePAByArfcn(deviceId, device.getTraceUtil().getArfcn(GnbProtocol.CellId.FOURTH));
                                        device.getTraceUtil().setArfcn(msg.what, value);
                                        device.getTraceUtil().setPci(msg.what, pci);
                                    }
                                    autoArfcnIndex[3]++;
                                } else {
                                    AppLog.D("mHandler 3 change arfcn = " + value + " is different band, stop at first" + ", autoArfcnIndex = " + autoArfcnIndex[3]);
                                    autoRun[3] = true;
                                    String deviceId = device.getRsp().getDeviceId();
                                    if (PaCtl.build().isB97502 && device.getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.TRACE){
                                        band = LteBand.earfcn2band(Integer.parseInt(arfcn));
                                        int band1 = LteBand.earfcn2band(Integer.parseInt(device.getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND)));
                                        AppLog.D("mHandler 3 change cell 4  = " + band + ", cell 2 band = " + band1);
                                        if (40 == band && band1 == 40){
                                            // 即将开的非B40，关闭正在跑的为B40，也把通道二的次B40关了
                                            isStopSecondArfcn[1] = true;
                                            MessageController.build().setCmdAndCellID(deviceId, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.SECOND);
                                            PaCtl.build().closePAByArfcn(deviceId, device.getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND));
                                        }else if (39 == band && band1 == 8){
                                            // 即将开B39，把通道二的B8关了
                                            isStopSecondArfcn[3] = true;
                                            MessageController.build().setCmdAndCellID(deviceId, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.SECOND);
                                            PaCtl.build().closePAByArfcn(deviceId, device.getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND));
                                        }
                                    }
                                    MessageController.build().setCmdAndCellID(deviceId, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FOURTH);
                                    PaCtl.build().closePAByArfcn(deviceId, device.getTraceUtil().getArfcn(GnbProtocol.CellId.FOURTH));
                                    device.getTraceUtil().setArfcn(msg.what, value);
                                    device.getTraceUtil().setPci(msg.what, pci);
                                    autoArfcnIndex[3]++;
                                    return;
                                }
                                break;
                            }
                        }
                    }
                    if (autoArfcnTime == 0) mHandler.sendEmptyMessageDelayed(3, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(3, autoArfcnTime * 1000L);
                    break;
                case 7:
                    deleteDev(msg.obj.toString());
                    break;
                case 8:
                    if (device != null) {
                        TraceUtil traceUtil = device.getTraceUtil();
                        for (int cell_id = 0; cell_id < 4; cell_id++){
                            if (traceUtil.getWorkState(cell_id) == GnbBean.State.TRACE) {
                                if (traceUtil.getRsrp(cell_id) != -1) {
                                    countCell[cell_id] = 0;
                                    String imsi = traceUtil.getImsi(cell_id);
                                    for (int other_cell = 0; other_cell < 4; other_cell++){
                                        if (other_cell == cell_id) continue;
                                        if (imsi.equals(traceUtil.getImsi(other_cell)) && countCell[other_cell] != 0) {
                                            countCell[other_cell] = 0;
                                        }
                                    }
                                } else countCell[cell_id]++;

                                mTraceChildFragment.setRsrpValue(cell_id);
                            }
                        }

                        mHandler.sendEmptyMessageDelayed(8, TraceBean.RSRP_TIME_INTERVAL);
                    }
                    break;
                case 18:

                    break;
                case 9:
                    isStartCatchHandler = false;
                    break;
                // 10秒无上号数据逻辑4 走到此表示连续10s内无上号，那么切换下一个频点轮巡,且后续不再参与轮巡
                case 79:
                    stopBandRunOne("N79", 0, checkHistoryBean.getTD1());
                    break;
                case 1034:
                    stopBandRunOne("B34", PaCtl.build().isB97502 ? 3 : 1, PaCtl.build().isB97502 ? checkHistoryBean.getTD4() : checkHistoryBean.getTD2());
                    break;
                case 1005:
                    stopBandRunOne("B5", PaCtl.build().isB97502 ? 1 : 3, PaCtl.build().isB97502 ? checkHistoryBean.getTD2() : checkHistoryBean.getTD4());
                    break;
                case 1008:
                    stopBandRunOne("B8", PaCtl.build().isB97502 ? 1 : 3, PaCtl.build().isB97502 ? checkHistoryBean.getTD2() : checkHistoryBean.getTD4());
                    break;
                case 998:
                case 999:
                    String id = msg.obj.toString();

                    String trace_str = getString(R.string.traceing);
                    boolean isTrace = false;
                    TraceUtil traceUtil = new TraceUtil();
                    if (MessageController.build().isEnableChangeTac(id, GnbProtocol.CellId.FIRST) && MessageController.build().isTracing(id, GnbProtocol.CellId.FIRST)) {
                        updateProgress(0, 100, 0, trace_str, false);
                        isTrace = true;
                        traceUtil.setWorkState(0, GnbBean.State.TRACE);
                    }
                    if (MessageController.build().isEnableChangeTac(id, GnbProtocol.CellId.SECOND) && MessageController.build().isTracing(id, GnbProtocol.CellId.SECOND)) {
                        updateProgress(0, 100, 1, trace_str, false);
                        isTrace = true;
                        traceUtil.setWorkState(1, GnbBean.State.TRACE);
                    }
                    if (MessageController.build().isEnableChangeTac(id, GnbProtocol.CellId.THIRD) && MessageController.build().isTracing(id, GnbProtocol.CellId.THIRD)) {
                        updateProgress(0, 100, 2, trace_str, false);
                        isTrace = true;
                        traceUtil.setWorkState(2, GnbBean.State.TRACE);
                    }
                    if (MessageController.build().isEnableChangeTac(id, GnbProtocol.CellId.FOURTH) && MessageController.build().isTracing(id, GnbProtocol.CellId.FOURTH)) {
                        updateProgress(0, 100, 3, trace_str, false);
                        isTrace = true;
                        traceUtil.setWorkState(3, GnbBean.State.TRACE);
                    }
                    tv_do_btn.setEnabled(true);
                    if (isTrace) {
                        device.setWorkState(GnbBean.State.TRACE);
                        setBtnStr(true);
                    }
                    if (msg.what == 999){
                        List<TracePara> traceList = MessageController.build().getTraceList();

                        String runHandler = "";
                        if (checkHistoryBean == null) break;
                        for (int i = 0; i < 4; i++){
                            for (TracePara tracePara : traceList) {
                                if (tracePara.getId().equals(id) && tracePara.getCellId() == i) {
                                    String arfcn = MessageController.build().getArfcn(id, i);
                                    String pci = MessageController.build().getPci(id, i);
                                    String imsi = checkHistoryBean.getImsiFirst();
                                    traceUtil.setArfcn(i, arfcn);
                                    traceUtil.setPci(i, pci);
                                    traceUtil.setImsi(i, imsi);
                                    mTraceChildFragment.setConfigInfo(i, 0, arfcn, pci, imsi);

                                    switch (i) {
                                        case 0:
                                            if (checkHistoryBean.getTD1().size() > 1){
                                                mHandler.sendEmptyMessageDelayed(i, autoArfcnTime * 1000L);
                                                runHandler += i;
                                            }
                                            break;
                                        case 1:
                                            if (checkHistoryBean.getTD2().size() > 1){
                                                mHandler.sendEmptyMessageDelayed(i, autoArfcnTime * 1000L);
                                                runHandler += i;
                                            }
                                            break;
                                        case 2:
                                            if (checkHistoryBean.getTD3().size() > 1){
                                                mHandler.sendEmptyMessageDelayed(i, autoArfcnTime * 1000L);
                                                runHandler += i;
                                            }
                                            break;
                                        case 3:
                                            if (checkHistoryBean.getTD4().size() > 1){
                                                mHandler.sendEmptyMessageDelayed(i, autoArfcnTime * 1000L);
                                                runHandler += i;
                                            }
                                            break;
                                    }
                                    break;
                                }
                            }
                        }
                        AppLog.D("mHandler 999 set runHandler = " + runHandler);
                        device.setTraceUtil(traceUtil);
                    }
                    break;
            }
        }
    };

    private void stopBandRunOne(String band, int cell_id, LinkedList<ArfcnPciBean> td){
        bandRunOneMap.put(band, false);
        bandRunOneHandleMap.remove(cell_id);
        if (autoArfcnIndex[cell_id] >= td.size()) autoArfcnIndex[cell_id] = 0;
        AppLog.D("mHandler " + band + " no data in 10s, stop this and start next" + ", autoArfcnIndex = " + autoArfcnIndex[cell_id]);
        autoRun[cell_id] = true;
        String deviceId = device.getRsp().getDeviceId();
        MessageController.build().setCmdAndCellID(deviceId, cell_id == 0 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, cell_id);
        PaCtl.build().closePAByArfcn(deviceId, device.getTraceUtil().getArfcn(cell_id));
        String arfcn = td.get(autoArfcnIndex[cell_id]).getArfcn();
        String pci = td.get(autoArfcnIndex[cell_id]).getPci();
        device.getTraceUtil().setArfcn(cell_id, arfcn);
        device.getTraceUtil().setPci(cell_id, pci);
        autoArfcnIndex[cell_id]++;
        if (autoArfcnIndex[cell_id] >= td.size()) autoArfcnIndex[cell_id] = 0;
    }

    // ****************************************弹框方法开始*******************************************
    public void showToast(String msg) {
        showToast(msg, false);
    }

    public void showToast(String msg, boolean isLongShow) {
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
        toast.setDuration(isLongShow ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
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
                mInInDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        mInInDialog = null;
                    }
                });
                return;
            }
            //创建第二层Dialog
            mInDialog = new Dialog(this, style_dialog);
            mInDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mInDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
            mInDialog.setCancelable(true);   // 返回键不消失
            mInDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    mInDialog = null;
                }
            });
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
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.getDecorView().setPadding(0, 0, 0, 0); // 消除边距
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;   // 设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setNavigationBarColor(Color.parseColor("#2A72FF"));
            window.setAttributes(lp);
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
    private Dialog mDialog, mInDialog, mInInDialog;

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

    public void deleteDev(String id) {
        mHandler.removeCallbacksAndMessages(null);
        device = null;
        updateSteps(0, StepBean.State.success, getString(R.string.stop_work));
    }

    private final ArrayList<StepBean> nrStepsList = new ArrayList<>();
    public LinkedList<ScanArfcnBean> freqList = new LinkedList<>();
    public LinkedList<ScanArfcnBean> freqMSList = new LinkedList<>();
    public LinkedList<ScanArfcnBean> freqUTList = new LinkedList<>();

    public LinkedList<ScanArfcnBean> getFreqList() {
        return freqList;
    }

    public List<ImsiBean> getImsiList() {
        return mImsiList;
    }

    int[] countCell = {0, 0, 0, 0};

    boolean isRsrpStart = false;

    private String getCellStr(int cell_id) {
        if (cell_id == 0) return getString(R.string.first);
        else if (cell_id == 1) return getString(R.string.second);
        else if (cell_id == 2) return getString(R.string.third);
        else return getString(R.string.fourth);
    }

    public void setBtnStr(boolean start) {
        if (start) {
            if (tv_do_btn.getText().equals(getString(R.string.stop))) return;
            setStartBtnText(getString(R.string.stop));
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_stop);
            tv_gain_btn.setVisibility(View.VISIBLE);
            setDevState(1, getString(R.string.working));
            mCfgTraceChildFragment.setWorking(true);
        } else {
            if (tv_do_btn.getText().equals(getString(R.string.open))) return;
            setStartBtnText(getString(R.string.open));
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_start);
            tv_gain_btn.setVisibility(View.GONE);
            if (device != null && device.getWorkState() != GnbBean.State.NONE && device.getWorkState() != GnbBean.State.TRACE)
                setDevState(0, getString(R.string.connected));
            mCfgTraceChildFragment.setWorking(false);
        }
    }

    private void showChangeTraceImsi(MyUeidBean bean) {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView title = view.findViewById(R.id.title);
        title.setText(R.string.is_trace_this_imsi);

        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int workState = device.getWorkState();
                if (workState == GnbBean.State.TRACE) {
                    String imsi = bean.getUeidBean().getImsi();

                    for (int j = 0; j < mImsiList.size(); j++) {
                        if (mImsiList.get(j).getImsi().equals(imsi)) {
                            mImsiList.get(j).setState(ImsiBean.State.IMSI_NOW);
                            //cell_id = mImsiList.get(j).getCellId();
                            mCatchChildFragment.itemChanged(j);
                            break;
                        }
                    }

                    for (int j = 0; j < mImsiList.size(); j++) {
                        if (mImsiList.get(j).getImsi().equals(imsi)) continue;
                        if (mImsiList.get(j).getState() == ImsiBean.State.IMSI_NOW) {
                            //if (mImsiList.get(j).getCellId() == cell_id){
                            if (MainActivity.getInstance().isInBlackList(mImsiList.get(j).getImsi()))
                                mImsiList.get(j).setState(ImsiBean.State.IMSI_BL);
                            else mImsiList.get(j).setState(ImsiBean.State.IMSI_OLD);
                            //}
                            mCatchChildFragment.itemChanged(j);
                            break;
                        }
                    }

                    TraceUtil traceUtil = device.getTraceUtil();
                    String arfcn;
                    int cellID = -1;
                    do {
                        cellID++;
                        arfcn = traceUtil.getArfcn(cellID);
                        if (cellID == 3) break;
                    } while (arfcn.isEmpty());
                    if (!arfcn.isEmpty()) {
                        setChangeImsi(cellID, arfcn, imsi);
                    }
                }
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        final TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    public void setChangeImsi(int cell_id, String arfcn, String imsi) {
        String s = getCellStr(cell_id);
        String id = device.getRsp().getDeviceId();

        MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.cell) + s + getString(R.string.change_imsi));
        MainActivity.getInstance().updateProgress(0, 50, cell_id, getString(R.string.changeing), false);
        device.getTraceUtil().setImsi(cell_id, imsi);
        isChangeImsi[cell_id] = true;
        if (Integer.parseInt(arfcn) > 100000)
            MessageController.build().startTrace(id, cell_id, imsi, 1);
        else MessageController.build().startLteTrace(id, cell_id, imsi, 1);
    }

    public void checkAndChangeImsi(String imsi) {
        int workState = device.getWorkState();
        if (workState == GnbBean.State.TRACE) {
            String id = device.getRsp().getDeviceId();

            int cell_id = -1;//四个通道全部定位同一个目标
            for (int j = 0; j < mImsiList.size(); j++) {
                if (mImsiList.get(j).getImsi().equals(imsi)) {
                    mImsiList.get(j).setState(ImsiBean.State.IMSI_NOW);
                    //cell_id = mImsiList.get(j).getCellId();
                    break;
                }
            }

            for (int j = 0; j < mImsiList.size(); j++) {
                if (mImsiList.get(j).getImsi().equals(imsi)) continue;
                if (mImsiList.get(j).getState() == ImsiBean.State.IMSI_NOW) {
                    //if (mImsiList.get(j).getCellId() == cell_id){
                    if (MainActivity.getInstance().isInBlackList(mImsiList.get(j).getImsi()))
                        mImsiList.get(j).setState(ImsiBean.State.IMSI_BL);
                    else mImsiList.get(j).setState(ImsiBean.State.IMSI_OLD);
                    //}
                    break;
                }
            }

            mCatchChildFragment.refreshListView();

            TraceUtil traceUtil = device.getTraceUtil();
            String arfcn;
            int cellID = -1;
            do {
                cellID++;
                arfcn = traceUtil.getArfcn(cellID);
                if (cellID == 3) break;
            } while (arfcn.isEmpty());
            if (!arfcn.isEmpty()) {
                MainActivity.getInstance().setChangeImsi(cellID, arfcn, imsi);
            }
        }
    }

    private void freshDoWorkState(int type, int cell_id, String imsi, String arfcn, String pci) {
        if (!imsi.isEmpty()) {
            //imsi = imsi.substring(0, 5) + "****" + imsi.substring(11);
            mTraceChildFragment.setConfigInfo(cell_id, 0, arfcn, pci, imsi);
        }
        if (cell_id == 0 && autoRun[0]) return;
        if (cell_id == 1 && autoRun[1]) return;
        if (cell_id == 2 && autoRun[2]) return;
        if (cell_id == 3 && autoRun[3]) return;
        String s = getString(R.string.cell) + getCellStr(cell_id) + getString(R.string.traceing);

        MainActivity.getInstance().updateSteps(0, StepBean.State.success, s);
        updateProgress(0, 100, cell_id, getString(R.string.traceing), false);
    }

    public void resetAutoArfcnTime() {
        String string = PrefUtil.build().getValue("Auto_Arfcn_time", "60").toString();
        if (string.isEmpty()) autoArfcnTime = 0;
        else autoArfcnTime = Integer.parseInt(string);
        String[] ue_max = PrefUtil.build().getValue("ue_max", "0:0").toString().split(":");
        ueMax[0] = Integer.parseInt(ue_max[0]);
        ueMax[1] = Integer.parseInt(ue_max[1]);
        String scan_count = PrefUtil.build().getValue("scan_count", "2").toString();
        scanCount = Integer.parseInt(scan_count);
        nearValue = (int) PrefUtil.build().getValue("near_value", -3);
    }
}