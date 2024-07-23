package com.g50;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.BarChart.BarData;
import com.DrawBatteryView;
import com.File.FileItem;
import com.File.FileProtocol;
import com.File.FileUtil;
import com.Logcat.APPLog;
import com.SlideMenu.SlideLeftFragment;
import com.SlideMenu.SlideRightFragment;
import com.SlideMenu.SlidingFragmentActivity;
import com.SlideMenu.SlidingMenu;
import com.Util.BarChartUtil;
import com.Util.DataUtil;
import com.Util.DateUtil;
import com.Util.PermissionsUtil;
import com.Util.PrefUtil;
import com.Util.TextTTS;
import com.Util.Util;
import com.Wifi.WifiBarView;
import com.Wifi.WifiData;
import com.arfcn.Control.MessageManager;
import com.arfcn.H20Sdk;
import com.arfcn.Module.Bean.CellBean;
import com.g50.Bean.Enum;
import com.g50.Bean.GnbBean;
import com.g50.Bean.PaCtl;
import com.g50.UI.Adpter.ArfcnAdapter;
import com.g50.UI.Adpter.FileAdapter;
import com.g50.UI.Bean.DeviceInfo;
import com.g50.UI.Bean.GnbCity;
import com.g50.UI.Bean.ImsiBean;
import com.g50.UI.Bean.ScanArfcnBean;
import com.g50.UI.Bean.TraceBean;
import com.g50.UI.Bean.TraceUtil;
import com.g50.UI.Dialog.CfgArfcnDialog;
import com.g50.UI.Dialog.GnbCityDialog;
import com.g50.UI.Dialog.GpsOffsetTestDialog;
import com.g50.UI.Dialog.LoginDialog;
import com.g50.UI.Dialog.SetArfcnChangeDialog;
import com.g50.UI.Dialog.TraceDialog;
import com.github.mikephil.charting.charts.BarChart;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.nr70.Arfcn.Bean.AtCmd;
import com.nr70.Arfcn.Bean.LteBand;
import com.nr70.Arfcn.Bean.NrBand;
import com.nr70.FTP.FTPUtil;
import com.nr70.Gnb.Bean.GnbProtocol;
import com.nr70.Gnb.Bean.PaBean;
import com.nr70.Gnb.Bean.UeidBean;
import com.nr70.Gnb.Response.GnbCatchCfgRsp;
import com.nr70.Gnb.Response.GnbCmdRsp;
import com.nr70.Gnb.Response.GnbFreqScanGetDocumentRsp;
import com.nr70.Gnb.Response.GnbFreqScanRsp;
import com.nr70.Gnb.Response.GnbFtpRsp;
import com.nr70.Gnb.Response.GnbGetSysInfoRsp;
import com.nr70.Gnb.Response.GnbGpioRsp;
import com.nr70.Gnb.Response.GnbGpsInOutRsp;
import com.nr70.Gnb.Response.GnbGpsRsp;
import com.nr70.Gnb.Response.GnbMethIpRsp;
import com.nr70.Gnb.Response.GnbStateRsp;
import com.nr70.Gnb.Response.GnbTraceRsp;
import com.nr70.Gnb.Response.GnbUserDataRsp;
import com.nr70.Gnb.Response.GnbVersionRsp;
import com.nr70.NrSdk;
import com.nr70.Socket.ConnectProtocol;
import com.nr70.Socket.MessageControl.MessageController;
import com.nr70.Socket.OnSocketChangeListener;
import com.nr70.Socket.ZTcpService;
import com.nr70.Util.Battery;
import com.nr70.Util.OpLog;

import org.json.JSONException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class G50Activity extends SlidingFragmentActivity implements View.OnClickListener, MessageController.OnSetGnbListener,
        TraceDialog.OnTraceSetListener, LoginDialog.OnLoginListener,ArfcnAdapter.OnImportArfcnListener,FTPUtil.OnFtpListener,
        SlideLeftFragment.OnSlideLeftMenuListener, OnSocketChangeListener,
        SlideRightFragment.OnSlideRightMenuListener , MessageManager.OnBaseStationScanListener{
    private boolean isCell0Failed = false;
    public static int PA_Type = 1;
    int setArfcnChangeCell1, setArfcnChangeCell2;
    List<Integer> arfcnListCell1 = new ArrayList<>();
    List<Integer> arfcnListCell2 = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //保持屏幕唤醒
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_trace);

        requestPermissions();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        PA_Type = bundle.getInt("PA_Type");
        APPLog.I("PA_Type = " + PA_Type);
        //柱状图
        barChartUtil = new BarChartUtil(barChart);
        barChartUtil1 = new BarChartUtil(barChart1);
        barChartUtil.setXYAxis(100,0,10);
        barChartUtil1.setXYAxis(100,0,10);
        barChartUtil.addBarDataSet("小区一",getResources().getColor(R.color.blue));
        barChartUtil1.addBarDataSet("小区二",getResources().getColor(R.color.orange));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().toString().equals(getResources().getString(R.string.rb_cell_0))){
                    APPLog.I("rb_cell_0");
                    barChart.setVisibility(View.VISIBLE);
                    barChart1.setVisibility(View.GONE);
                }else  if (tab.getText().toString().equals(getResources().getString(R.string.rb_cell_1))){
                    APPLog.I("rb_cell_1");
                    barChart.setVisibility(View.GONE);
                    barChart1.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
    /**
     * 获取自己应用内部的版本名
     */
    public static String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }
    private void startActivity() {
        APPLog.creatLogFile("+++ ON CREATE +++  ");
        initLayout();
        initObject();
        initSDK();
        heartBeatTimerOut();
    }

    /**
     * 初始化各对像
     */
    private void initObject() {
        APPLog.D("initObject()");
        // 开启程序必须走下一条
        GnbCity.build().init();
        TraceUtil.build().init();
        TextTTS.build().initTTS();
        initArfcnData();

    }
    private void initArfcnData(){
        try {
            if (Util.json2Int(PrefUtil.build().getArfcn("N1"),"N1" ).size()==0&&
                    Util.json2Int(PrefUtil.build().getArfcn("N28"),"N28" ).size()==0&&
                    Util.json2Int(PrefUtil.build().getArfcn("N41"),"N41" ).size()==0&&
                    Util.json2Int(PrefUtil.build().getArfcn("N78"),"N78" ).size()==0&&
                    Util.json2Int(PrefUtil.build().getArfcn("N79"),"N79" ).size()==0&&
                    Util.json2Int(PrefUtil.build().getArfcn("N1"+"cfg"),"N1" ).size()==0&&
                    Util.json2Int(PrefUtil.build().getArfcn("N28"+"cfg"),"N28" ).size()==0&&
                    Util.json2Int(PrefUtil.build().getArfcn("N41"+"cfg"),"N41" ).size()==0&&
                    Util.json2Int(PrefUtil.build().getArfcn("N78"+"cfg"),"N78" ).size()==0&&
                    Util.json2Int(PrefUtil.build().getArfcn("N79"+"cfg"),"N79" ).size()==0){
                initList();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            arfcnList_N1.addAll(Util.json2Int(PrefUtil.build().getArfcn("N1"),"N1" ));
            arfcnList_N28.addAll(Util.json2Int(PrefUtil.build().getArfcn("N28"),"N28" ));
            arfcnList_N41.addAll(Util.json2Int(PrefUtil.build().getArfcn("N41"),"N41" ));
            arfcnList_N78.addAll(Util.json2Int(PrefUtil.build().getArfcn("N78"),"N78" ));
            arfcnList_N79.addAll(Util.json2Int(PrefUtil.build().getArfcn("N79"),"N79" ));
            setArfcnList_N1.addAll(Util.json2Int(PrefUtil.build().getArfcn("N1"+"cfg"),"N1" ));
            setArfcnList_N28.addAll(Util.json2Int(PrefUtil.build().getArfcn("N28"+"cfg"),"N28" ));
            setArfcnList_N41.addAll(Util.json2Int(PrefUtil.build().getArfcn("N41"+"cfg"),"N41" ));
            setArfcnList_N78.addAll(Util.json2Int(PrefUtil.build().getArfcn("N78"+"cfg"),"N78" ));
            setArfcnList_N79.addAll(Util.json2Int(PrefUtil.build().getArfcn("N79"+"cfg"),"N79" ));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void initList(){
        try {
            Log.i("","N1 = "+Util.json2Int(PrefUtil.build().getArfcn("N1"),"N1" ).size());
            if (Util.json2Int(PrefUtil.build().getArfcn("N1"),"N1" ).size()==0){
                List<Integer> list =new ArrayList<>();
                list.add(427250);
                list.add(428910);
                list.add(422890);
                PrefUtil.build().setArfcn(Util.int2Json(list,"N1"),"N1");
            }
            if (Util.json2Int(PrefUtil.build().getArfcn("N28"),"N28" ).size()==0){
                List<Integer> list =new ArrayList<>();
                list.add(154810);
                list.add(152650);
                PrefUtil.build().setArfcn(Util.int2Json(list,"N28"),"N28");
            }
            if (Util.json2Int(PrefUtil.build().getArfcn("N41"),"N41" ).size()==0){
                List<Integer> list =new ArrayList<>();
                list.add(504990);
                list.add(512910);
                list.add(516990);
                PrefUtil.build().setArfcn(Util.int2Json(list,"N41"),"N41");
            }
            if (Util.json2Int(PrefUtil.build().getArfcn("N78"),"N78" ).size()==0){
                List<Integer> list =new ArrayList<>();
                list.add(627264);
                list.add(633984);
                PrefUtil.build().setArfcn(Util.int2Json(list,"N78"),"N78");
            }
            if (Util.json2Int(PrefUtil.build().getArfcn("N79"),"N79" ).size()==0){
                List<Integer> list =new ArrayList<>();
                list.add(723360);
                PrefUtil.build().setArfcn(Util.int2Json(list,"N79"),"N79");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (Util.json2Int(PrefUtil.build().getArfcn("N1"+"cfg"),"N1" ).size()==0){
                List<Integer> list =new ArrayList<>();
                list.add(427250);
                list.add(428910);
                list.add(422890);
                PrefUtil.build().setArfcn(Util.int2Json(list,"N1"),"N1"+"cfg");
            }
            if (Util.json2Int(PrefUtil.build().getArfcn("N28"+"cfg"),"N28" ).size()==0){
                List<Integer> list =new ArrayList<>();
                list.add(154810);
                list.add(152650);
                PrefUtil.build().setArfcn(Util.int2Json(list,"N28"),"N28"+"cfg");
            }
            if (Util.json2Int(PrefUtil.build().getArfcn("N41"+"cfg"),"N41" ).size()==0){
                List<Integer> list =new ArrayList<>();
                list.add(504990);
                list.add(512910);
                list.add(516990);
                PrefUtil.build().setArfcn(Util.int2Json(list,"N41"),"N41"+"cfg");
            }
            if (Util.json2Int(PrefUtil.build().getArfcn("N78"+"cfg"),"N78" ).size()==0){
                List<Integer> list =new ArrayList<>();
                list.add(627264);
                list.add(633984);
                PrefUtil.build().setArfcn(Util.int2Json(list,"N78"),"N78"+"cfg");
            }
            if (Util.json2Int(PrefUtil.build().getArfcn("N79"+"cfg"),"N79" ).size()==0){
                List<Integer> list =new ArrayList<>();
                list.add(723360);
                PrefUtil.build().setArfcn(Util.int2Json(list,"N79"),"N79"+"cfg");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * SDK包初始化在此
     */
    private void initSDK() {
        // 先绑定服务
        startBindService(this);
        // 再开SDK
        NrSdk.build().init(this);

        // 定位、扫频、SOCKET、升级取LOG等监听配置
        ZTcpService.build().addConnectListener(this);
        MessageController.build().setOnSetGnbListener(this);
//        H20Sdk.build().init(this);
        MessageManager.build().addBaseStationScanListener(this);
//        Nr5g.build().addOnScanArfcnListener(this);
        FTPUtil.build().setFtpListener(this);
    }
    /**
     * 退出应用对SDK包处理
     */
    private void closeSDK() {
        NrSdk.build().onDestory();
        MessageController.build().removeOnSetGnbListener();
        //Nr5g.build().removeOnScanArfcnListener();
        FTPUtil.build().removeFtpListener();
        ZTcpService.build().removeConnectListener(this);
//        H20Sdk.build().onDestory();
        MessageManager.build().removeBaseStationScanListener(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBindService();
        closeSDK();
        if (mDialog != null){
            if (mDialog.isShowing()){
                mDialog.dismiss();
            }
            mDialog = null;
        }
        acquireWakeLock(false);
        APPLog.I("++++++ MainActivity onDestroy()");

        System.exit(0);
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{
                    Manifest.permission.VIBRATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
            PermissionsUtil.getInstance().checkPermissions(G50Activity.this, permissions, permissionsResult);
        } else {
            startActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionsUtil.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    PermissionsUtil.IPermissionsResult permissionsResult = new PermissionsUtil.IPermissionsResult() {
        @Override
        public void permitPermissions() {
            //处理通过请求后的操作
            // Toast.makeText(MainActivity.this, "已授权", Toast.LENGTH_SHORT).show();
            startActivity();
        }

        @Override
        public void refusePermissions() {
            //处理被拒绝申请的权限的操作
            Toast.makeText(getApplicationContext(), "已拒绝", Toast.LENGTH_SHORT).show();
            finish();
        }
    };
    /**
     * 初始化左右滑动菜单
     */
    private void initSlideMenu() {
        mSlideLeftFrag = new SlideLeftFragment();
        mSlideLeftFrag.setOnSlideLeftMenuListener(this);
        setBehindContentView(R.layout.slide_left_menu_frame);
        getSupportFragmentManager().beginTransaction() .replace(R.id.id_left_menu_frame, mSlideLeftFrag).commit();
        final SlidingMenu menu = getSlidingMenu();
        menu.setMode(SlidingMenu.LEFT_RIGHT);
        // 设置触摸屏幕的模式
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow_left);
        // 设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        // 设置渐入渐出效果的值
        menu.setFadeDegree(0.35f);
        //menu.setBehindScrollScale(1.0f);
        menu.setSecondaryShadowDrawable(R.drawable.shadow_right);
        // 设置右边（二级）侧滑菜单
        menu.setSecondaryMenu(R.layout.slide_right_menu_frame);
        mSlideRightFrag = new SlideRightFragment(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.id_right_menu_frame, mSlideRightFrag).commit();
        //监听slidingmenu打开时调用
        menu.setOnOpenListener(new SlidingMenu.OnOpenListener() {
            @Override
            public void onOpen() {
                if (menu.isSecondaryMenuShowing()) {
                    APPLog.D("setOnOpenListener: isRightMenuShowing");
                    refreshRightSlideMenu();
                } else {
                    APPLog.D("setOnOpenListener: isLeftMenuShowing");
                    refreshLeftSlideMenu();
                }
            }
        });
        mSlideLeftFrag.setOnSlideLeftMenuListener(this);
        mSlideRightFrag.setOnSlideRightMenuListener(this);
    }
    /**
     * 显示左滑菜单
     */
    private void showLeftSlideMenu() {
        refreshLeftSlideMenu();
        getSlidingMenu().showMenu(); // 显示菜单
    }

    /**
     * 显示右滑菜单
     */
    private void showRightSlideMenu() {
        getSlidingMenu().showSecondaryMenu(); // 显示菜单
    }
    /**
     * 刷新右滑菜单
     */
    private void refreshRightSlideMenu() {

    }
    /**
     * 刷新左滑菜单
     */
    private void refreshLeftSlideMenu() {
        mSlideLeftFrag.refreshVersion(gnbVersion);
        mSlideLeftFrag.refreshTraceInfo(TraceUtil.build().getImsi(GnbProtocol.CellId.FIRST),
                TraceUtil.build().getArfcn(GnbProtocol.CellId.FIRST), TraceUtil.build().getPci(GnbProtocol.CellId.FIRST));
        if (dualCell == 2) mSlideLeftFrag.refresh2TraceInfo(TraceUtil.build().getImsi(GnbProtocol.CellId.SECOND),
                TraceUtil.build().getArfcn(GnbProtocol.CellId.SECOND), TraceUtil.build().getPci(GnbProtocol.CellId.SECOND));
        mSlideLeftFrag.refreshTraceBar(traceBarEnable);
        mSlideLeftFrag.refreshMode(workMode);
        mSlideLeftFrag.setDualCell(dualCell);
        mSlideLeftFrag.refreshModeRejectCode(TraceUtil.build().getMobRejectCode(GnbProtocol.CellId.FIRST) == 9 ||
                TraceUtil.build().getMobRejectCode(GnbProtocol.CellId.SECOND) == 9, noTraceFlag);
        if (gnbVersion.equals("")) {
            if (workState == GnbBean.State.IDLE) {
                //MessageController.build().setOnlyTypeCmd(GnbProtocol.UI_2_gNB_QUERY_gNB_VERSION);
            }
        }
    }
    /**
     * SCROLL LIST VIEW 高度处理
     * 发现在ScrollView中嵌套ListView空间，无法正确的计算ListView的大小，保显示一行
     * @param listView
     */
    public void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }
    /**
     * 界面布局处理
     */
    private void initLayout() {
        mContext = G50Activity.this;
        mInflater = LayoutInflater.from(this);
        battery = (TextView) findViewById(R.id.battery);
        tv_temp = (TextView) findViewById(R.id.tv_temp);
        batteryView = (DrawBatteryView) findViewById(R.id.battery_view);
        batteryView.setPower(0);
        battery.setText("0%");
        traceBarView = (LinearLayout) findViewById(R.id.bar_chart_view);
        traceBarView.setVisibility(View.GONE);
//        iv_top = (ImageView) findViewById(R.id.iv_top);
        tv_value = (TextView) findViewById(R.id.tv_value);
        tv_2_value = (TextView) findViewById(R.id.tv_2_value);
        tv_gps = (TextView) findViewById(R.id.tv_gps);
        tv_air_sync = (TextView) findViewById(R.id.tv_air_sync);
        tv_air_sync1 = (TextView) findViewById(R.id.tv_air_sync1);
        tv_state = (TextView) findViewById(R.id.tv_state);
        tv_process = (TextView) findViewById(R.id.tv_process);
        tv_state.setText(getStr(R.string.state_device_start));
        tv_imsi_cnt = (TextView) findViewById(R.id.tv_imsi_cnt);
        tv_imsi = (TextView) findViewById(R.id.tv_imsi);

//        iv_top.setImageResource(R.mipmap.top_bg_n);
        tv_imsi_cnt.setVisibility(View.GONE);
        tv_imsi.setVisibility(View.GONE);
        tv_value.setText("");
        tv_2_value.setText("");
        if (dualCell == 2) {
            tv_2_value.setVisibility(View.VISIBLE);
        }else {
            tv_2_value.setVisibility(View.INVISIBLE);
        }
        // arfcn
//        scanArfcnBeanList.add(new ScanArfcnBean("123","456",1,1,1,1,1,1,1,1,1,1,1,1));
//        scanArfcnBeanList.add(new ScanArfcnBean("123","456",1,1,1,1,1,1,1,1,1,1,1,1));
//        scanArfcnBeanList.add(new ScanArfcnBean("123","456",1,1,1,1,1,1,1,1,1,1,1,1));
//        scanArfcnBeanList.add(new ScanArfcnBean("123","456",1,1,1,1,1,1,1,1,1,1,1,1));
//        scanArfcnBeanList.add(new ScanArfcnBean("123","456",1,1,1,1,1,1,1,1,1,1,1,1));
//        scanArfcnBeanList.add(new ScanArfcnBean("123","456",1,1,1,1,1,1,1,1,1,1,1,1));
//        scanArfcnBeanList.add(new ScanArfcnBean("123","456",1,1,1,1,1,1,1,1,1,1,1,1));
//        scanArfcnBeanList.add(new ScanArfcnBean("123","456",1,1,1,1,1,1,1,1,1,1,1,1));
//        scanArfcnBeanList.add(new ScanArfcnBean("987","456",1,1,1,1,1,1,1,1,1,1,1,9));
        arfcnListView = (RecyclerView) findViewById(R.id.arfcn_list);
        arfcnAdapter = new ArfcnAdapter(mContext, scanArfcnBeanList,true);
        arfcnAdapter.setOnImportArfcnListener(this);
        arfcnListView.setAdapter(arfcnAdapter);
        arfcnListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
//        setListViewHeightBasedOnChildren(arfcnListView);
        iv_temp = (ImageView) findViewById(R.id.iv_temp);
        btn_trace = (Button) findViewById(R.id.btn_trace);
        btn_txpwr = (Button) findViewById(R.id.btn_txpwr);
        btn_arfcn = (Button) findViewById(R.id.btn_arfcn);
//        tb_query = (Button) findViewById(R.id.tb_query);
        btn_slide_left = (Button) findViewById(R.id.btn_slide_left);
        btn_slide_right = (Button) findViewById(R.id.btn_slide_right);
        btn_trace.setOnClickListener(this);
        btn_txpwr.setOnClickListener(this);
        btn_arfcn.setOnClickListener(this);
//        tb_query.setOnClickListener(this);
        btn_slide_left.setOnClickListener(this);
        btn_slide_right.setOnClickListener(this);

        initSlideMenu();
        traceBarEnable = false;
//        initBarChartView();

        wifi_bar = (WifiBarView)findViewById(R.id.wifi_bar);
        wifi_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curWifiAlarmIdx++;
                if (curWifiAlarmIdx >= wifiAlarmList.size()) {
                    curWifiAlarmIdx = 0;
                }
                refreshWifiBarChart();
                APPLog.I("Wifi alarm curWifiAlarmIdx = " + curWifiAlarmIdx);
            }
        });
        initWifiBarView();
        barChart = (BarChart) findViewById(R.id.bar_chart);
        barChart1 = (BarChart) findViewById(R.id.bar_chart1);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
         /* LoginDialog loginDialog = new LoginDialog(this, mInflater);
        loginDialog.setOnLoginListener(this);
        loginDialog.show();*/
    }

    private void initWifiBarView() {
        wifiAlarmList.clear();
        wifi_bar.setVisibility(View.GONE);
        //Util.mWifiAlarmList.add(new WifiData("simdo123", "6C:E8:73:81:E6:B2", 0));
        List<String> axisXLableList = new ArrayList<String>();
        axisXLableList.add("0分钟");
        axisXLableList.add("1");
        axisXLableList.add("2");
        axisXLableList.add("3");
        wifi_bar.setAxisXLableList(axisXLableList); // X轴上的度值
        // X轴上每个刻度显示几根柱子，这里5分钟总共显示5*12=60根
        // 每5秒钟刷新一次数据，即1分钟60 = 5*12
        int x_unit = axisXLableList.size() - 1;
        wifi_bar.setAxisXStep(WifiData.DOT_MAX/x_unit);
        wifi_bar.setAxisYMax(-20);
        wifi_bar.setAxisYMin(-90);
        wifi_bar.setAxisYChangeColorValue(-50); // 柱状图变色值
        wifi_bar.setBgRectColor(Color.BLUE); // 边框
        wifi_bar.setBgColor(Color.WHITE); // 背景填充色
        wifi_bar.setAxisColor(Color.BLACK); // 坐标轴颜色
        wifi_bar.enableDisplayAxisYLabel(false); // 柱状上面是否显示标签
    }

    private void refreshWifiBarChart() {
        if (wifiAlarmList.size() > 0) {
            wifiAlarmList.get(curWifiAlarmIdx).handleData(Math.abs(wifiAlarmList.get(curWifiAlarmIdx).getLEVEL()));
            wifi_bar.refreshData(wifiAlarmList.get(curWifiAlarmIdx).getList());
        }
    }

    private boolean isCanWork(){
        if (workState == GnbBean.State.NONE) {
            showToast(getStr(R.string.device_start));
            return false;
        }
        if (workState == GnbBean.State.UPDATE) {
            showToast(getStr(R.string.reminder_upgrading));
            return false;
        }
        if (workState == GnbBean.State.GETLOG) {
            showToast(getStr(R.string.reminder_get_log));
            return false;
        }
        if (workState == GnbBean.State.REBOOT) {
            showToast(getStr(R.string.reminder_rebooting));
            return false;
        }
        return true;
    }
    
    private void showToast(String msg){
        Util.showToast(getApplicationContext(),msg);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_trace:
//                clickTraceBtn();
                if (workState == GnbBean.State.FREQ_SCAN) {
                    showToast(getStr(R.string.state_scanning));
                    return;
                }
                if (isCanWork()) clickTraceBtn();
                break;
            case R.id.btn_txpwr:
                if (isCanWork()) clickTxPwrOffsetBtn();
                break;
            case R.id.btn_arfcn:
//                if (TraceUtil.build().getWorkState(GnbProtocol.CellId.FIRST) == GnbBean.State.TRACE ||
//                        TraceUtil.build().getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.TRACE) {
//                    showToast(getStr(R.string.state_tracing));
//                    return;
//                }
                if (workState == GnbBean.State.FREQ_SCAN){
                    showStopFreqScanDialog("停止提示","确定要停止当前扫频吗？");
                } else if (isCanWork()) {
                    clickScanArfcnBtn();
                }

                break;
//            case R.id.tb_query:
//                clickQueryImsiBtn();
//                break;
            case R.id.btn_slide_left:
                showLeftSlideMenu();
                break;
            case R.id.btn_slide_right:
                showRightSlideMenu();
                break;
        }
    }
    /**
     * 查询IMSI号
     */
    private void clickQueryImsiBtn() {
        createCustomDialog();

        View view = mInflater.inflate(R.layout.dialog_query_imsi, null);
        final TextView tv_query_imsi = (TextView) view.findViewById(R.id.tv_query_imsi);
        final EditText ed_query_imsi = (EditText) view.findViewById(R.id.ed_query_imsi);
        final TextView tv_query_fail = (TextView) view.findViewById(R.id.tv_query_fail);
        tv_query_fail.setVisibility(View.GONE);
        final Button btn_query = (Button) view.findViewById(R.id.btn_query);
        final Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryImsi = ed_query_imsi.getEditableText().toString();
                if (queryImsi.length() != GnbProtocol.MAX_IMSI_USE_LEN) {
                    showToast(getStr(R.string.error_imsi));
                    return;
                }
                boolean noImsi = true;
                for (int i = 0; i < imsiList.size(); i++) {
                    if (queryImsi.equals(imsiList.get(i).getImsi())) {
                        noImsi = false;
                        mSlideRightFrag.refreshListView(imsiList, i, true);
                        showRightSlideMenu();
                        break;
                    }
                }

                if (noImsi) {
                    tv_query_fail.setVisibility(View.VISIBLE);
                    btn_query.setVisibility(View.GONE);
                    btn_cancel.setText(getStr(R.string.known));
                } else {
                    tv_query_fail.setVisibility(View.GONE);
                    InputMethodManager imm = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
                    imm.hideSoftInputFromWindow(ed_query_imsi.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);

                    closeCustomDialog();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });

        ed_query_imsi.setText(queryImsi);
        showCustomDialog(view, false);
    }

    private void showCfgArfcnDialog(){
        APPLog.D("showCfgArfcnDialog()");
        if (mCfgArfcnDialog != null) {
            mCfgArfcnDialog.show();
        } else {
            mCfgArfcnDialog = new CfgArfcnDialog(mContext);
            try {
                arfcnList_N1.clear();
                arfcnList_N28.clear();
                arfcnList_N41.clear();
                arfcnList_N78.clear();
                arfcnList_N79.clear();
                arfcnList_N1.addAll(Util.json2Int(PrefUtil.build().getArfcn("N1"),"N1" ));
                arfcnList_N28.addAll(Util.json2Int(PrefUtil.build().getArfcn("N28"),"N28" ));
                arfcnList_N41.addAll(Util.json2Int(PrefUtil.build().getArfcn("N41"),"N41" ));
                arfcnList_N78.addAll(Util.json2Int(PrefUtil.build().getArfcn("N78"),"N78" ));
                arfcnList_N79.addAll(Util.json2Int(PrefUtil.build().getArfcn("N79"),"N79" ));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            mCfgArfcnDialog.setList(arfcnList_N1, arfcnList_N28, arfcnList_N41, arfcnList_N78, arfcnList_N79);
            mCfgArfcnDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {

                    mCfgArfcnDialog = null;
                }
            });
            mCfgArfcnDialog.show();
        }
    }

    /**
     *  配置扫频【移动、联通、电信】
     */
    private void clickScanArfcnBtn() {
        APPLog.D("clickScanArfcnBtn()");
        if (workState == GnbBean.State.TRACE) {
            showToast(getStr(R.string.state_tracing));
            return;
        }
        createCustomDialog();

        View view = mInflater.inflate(R.layout.dialog_scan_arfcn, null);
        final RadioGroup rg_arfcn = (RadioGroup) view.findViewById(R.id.rg_arfcn);

        view.findViewById(R.id.tv_cfg_arfcn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCfgArfcnDialog();
            }
        });
        final CheckBox freq_scan_bs = view.findViewById(R.id.freq_scan_bs);
        final CheckBox freq_scan_h20 = view.findViewById(R.id.freq_scan_h20);
        if (workState != GnbBean.State.IDLE){
            freq_scan_bs.setChecked(false);
            freq_scan_h20.setChecked(true);
        }
        freq_scan_bs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (workState != GnbBean.State.IDLE){
                    freq_scan_bs.setChecked(false);
                }
                if (freq_scan_bs.isChecked()){
                    freq_scan_h20.setChecked(false);
                }else {
                    freq_scan_h20.setChecked(true);
                }
            }
        });
        freq_scan_h20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (workState != GnbBean.State.IDLE){
                    freq_scan_bs.setChecked(false);
                    freq_scan_h20.setChecked(true);
                }
                if (freq_scan_h20.isChecked()){
                    freq_scan_bs.setChecked(false);
                }else {
                    freq_scan_bs.setChecked(true);
                }
            }
        });
        final ToggleButton tb_nr = view.findViewById(R.id.tb_nr);
        final ToggleButton tb_lte = view.findViewById(R.id.tb_lte);
        tb_nr.setChecked(H20Sdk.build().isOpenNR());
        tb_lte.setChecked(H20Sdk.build().isOpenLte());
        tb_nr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tb_nr.isChecked()) {
                } else {
                    if (H20Sdk.build().openNR(false)) {
                        tb_nr.setChecked(false);
                    } else {
                        tb_nr.setChecked(true);
                    }

                }
            }
        });
        tb_lte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tb_lte.isChecked()) {
                } else {
                    if (H20Sdk.build().openLTE(false)) {
                        tb_lte.setChecked(false);
                    } else {
                        tb_lte.setChecked(true);
                    }

                }
            }
        });
        rg_arfcn.check(R.id.rb_gps);

        final CheckBox cb_1 = view.findViewById(R.id.cb_1);
        final CheckBox cb_28 = view.findViewById(R.id.cb_28);
        final CheckBox cb_41 = view.findViewById(R.id.cb_41);
        final CheckBox cb_78 = view.findViewById(R.id.cb_78);
        final CheckBox cb_79 = view.findViewById(R.id.cb_79);
        /*if (scanArfcnOp == AtCmd.OP.MOBILE) {
            rg_arfcn.check(R.id.rb_mobile);
        } else if (scanArfcnOp == AtCmd.OP.UNICOM) {
            rg_arfcn.check(R.id.rb_unicom);
        } else {
            rg_arfcn.check(R.id.rb_telecom);
        }*/
        final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (freq_scan_bs.isChecked()){
                    switch (rg_arfcn.getCheckedRadioButtonId()) {
                        case R.id.rb_gps:
                            async_enable = 0;
                            isGpsScan = true;
                            break;
                        case R.id.rb_air:
                            async_enable = 1;
                            isGpsScan = false;
                            break;
                    }
                    if (!cb_1.isChecked() && !cb_28.isChecked() && !cb_41.isChecked() && !cb_78.isChecked() && !cb_79.isChecked()){
                        showToast("请先勾选扫描频点");
                        return;
                    }
                    txPwrOffset = 0;
                    //Nr5g.build().setArfcnType(scanArfcnOp);
                    isStopScan = false;
                    arfcnAdapter.setScan(true);
                    enablelList.clear();
                    enablelList.add(cb_1.isChecked());
                    enablelList.add(cb_28.isChecked());
                    enablelList.add(cb_41.isChecked());
                    enablelList.add(cb_78.isChecked());
                    enablelList.add(cb_79.isChecked());
                    workState = GnbBean.State.FREQ_SCAN;
                    btn_arfcn.setText("结束");
                    if (enablelList.get(0)) {
                        startFreqScan("N1", report_level, async_enable);
                    } else if (enablelList.get(1)) {
                        startFreqScan("N28", report_level, async_enable);
                    } else if (enablelList.get(2)) {
                        startFreqScan("N41", report_level, async_enable);
                    } else if (enablelList.get(3)) {
                        startFreqScan("N78", report_level, async_enable);
                    } else if (enablelList.get(4)) {
                        startFreqScan("N79", report_level, async_enable);
                    }
                }else {
                    if (tb_nr.isChecked()) {
                        H20Sdk.build().openNR(true);
                    } else {
                        if (H20Sdk.build().openNR(false)) {
                            tb_nr.setChecked(false);
                        } else {
                            tb_nr.setChecked(true);
                        }

                    }
                    if (tb_lte.isChecked()) {
                        H20Sdk.build().openLTE(true);
                    } else {
                        if (H20Sdk.build().openLTE(false)) {
                            tb_lte.setChecked(false);
                        } else {
                            tb_lte.setChecked(true);
                        }

                    }

                }
                scanArfcnBeanList.clear();
                closeCustomDialog();
            }
        });
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, true);
    }
    /**
     * 定位按钮
     */
    private void clickTraceBtn() {
        APPLog.D("clickTraceBtn()" + TraceUtil.build().getWorkState(GnbProtocol.CellId.FIRST) + ", " + TraceUtil.build().getWorkState(GnbProtocol.CellId.SECOND));

        if (TraceUtil.build().getWorkState(GnbProtocol.CellId.FIRST) == GnbBean.State.NONE &&
                TraceUtil.build().getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.NONE) {
            startTraceDialog("","");
        } else if (workState == GnbBean.State.TRACE || workState == GnbBean.State.BLACKLIST || workState == GnbBean.State.GNBCFG || workState == GnbBean.State.CFG_TRACE) {
            if (TraceUtil.build().getWorkState(GnbProtocol.CellId.FIRST)!=GnbBean.State.STOP
                    &&TraceUtil.build().getWorkState(GnbProtocol.CellId.SECOND)!=GnbBean.State.STOP){
            stopTraceDialog(getStr(R.string.stop), getStr(R.string.stop_trace_reminder));}
        }
//        else {
//            showToast(getStr(R.string.state_no_ready));
//        }
    }
    /**
     * 定位配置
     */
    private void startTraceDialog(String arfcn, String pci) {
        if (traceDialog == null) {
            traceDialog = new TraceDialog(this, scanArfcnBeanList, blackList,this);
            traceDialog.setOnTraceSetListener(this);
        }
        traceDialog.setInfo(scanArfcnBeanList, blackList, dualCell, isDualSsbBitmap);
        if (!arfcn.isEmpty() && !pci.isEmpty()) traceDialog.setCfgStr(arfcn, pci);
        noTraceFlag = true;
        traceDialog.show();
    }
    private void setPaTypeDialog() {
            createCustomDialog();
            View view = mInflater.inflate(R.layout.layout_set_pa, null);
            final RadioGroup radioGroup = view.findViewById(R.id.group);
            final RadioButton type0 = view.findViewById(R.id.device_type_0);
            final RadioButton type1 = view.findViewById(R.id.device_type_1);
            final RadioButton type2 = view.findViewById(R.id.device_type_2);
            final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
//            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(RadioGroup group, int checkedId) {
//                    if (checkedId == type0.getId()){
//                        type = 0;
//                        APPLog.D("PA_Type = "+PA_Type);
//                    }else if (checkedId == type1.getId()){
//                        PA_Type = 1;
//                        APPLog.D("PA_Type = "+PA_Type);
//                    }else if (checkedId == type2.getId()){
//                        PA_Type = 2;
//                        APPLog.D("PA_Type = "+PA_Type);
//                    }
//                }
//            });
            if (PA_Type ==0){
                type0.setChecked(true);
                type1.setChecked(false);
                type2.setChecked(false);
            }else if (PA_Type ==1){
                type0.setChecked(false);
                type1.setChecked(true);
                type2.setChecked(false);
            }else if (PA_Type ==2){
                type0.setChecked(false);
                type1.setChecked(false);
                type2.setChecked(true);
            }
            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  if (type0.isChecked()){
                      PA_Type = 0;
                  }else if(type1.isChecked()){
                      PA_Type = 1;
                  }else if(type2.isChecked()){
                      PA_Type = 2;
                  }
                    APPLog.D("PA_Type = "+PA_Type);
                    closeCustomDialog();
                }

            });
            final Button btn_canel = (Button) view.findViewById(R.id.btn_cancel);
            btn_canel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeCustomDialog();
                }
            });
            showCustomDialog(view, false);
    }
    /**
     * 结束定位
     *
     * @param title
     * @param msg
     */
    private void stopTraceDialog(String title, String msg) {
            createCustomDialog();
            View view = mInflater.inflate(R.layout.dialog_confirm, null);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
            tv_title.setText(title);
            TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
            tv_msg.setText(msg);

            final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //setWorkState(Enum.State.STOP);

                    if (TraceUtil.build().isEnable(GnbProtocol.CellId.SECOND)) {
                        //setTraceState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP, getStr(R.string.state_stop_trace));
                        TraceUtil.build().setAtCmdTimeOut(GnbProtocol.CellId.SECOND, System.currentTimeMillis());
                        MessageController.build().setCmdAndCellID(GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.SECOND);
                        setTraceState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP);
                    }
                    if (TraceUtil.build().isEnable(GnbProtocol.CellId.FIRST)) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //setTraceState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP, getStr(R.string.state_stop_trace));
                                TraceUtil.build().setAtCmdTimeOut(GnbProtocol.CellId.FIRST, System.currentTimeMillis());
                                MessageController.build().setCmdAndCellID(GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.FIRST);
                                setTraceState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP);
                            }
                        }, 500);
                    }
                    refreshStateView(getStr(R.string.state_stop_trace));
                    PaCtl.build().closePA();
                    closeCustomDialog();
                }
            });
            final Button btn_canel = (Button) view.findViewById(R.id.btn_cancel);
            btn_canel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeCustomDialog();
                }
            });
            showCustomDialog(view, false);
    }
    /**
     * 配置定位功率衰减，掉电恢复默认
     */
    private void clickTxPwrOffsetBtn() {
        APPLog.D("clickTxPwrOffsetBtn()");
//        if ( PaCtl.build().isVehicle()) {
//            showToast(getStr(R.string.state_mode_car));
//            return;
//        }
        if (workState != GnbBean.State.TRACE) {
            showToast(getStr(R.string.state_not_trace));
            return;
        }
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_tx_pwr, null);
        final RadioGroup rg_cell = (RadioGroup) view.findViewById(R.id.rg_cell);
        int txpwr = 0;
        if (TraceUtil.build().isEnable(GnbProtocol.CellId.FIRST)) {
            rg_cell.check(R.id.rb_cell_0);
            txpwr = TraceUtil.build().getTxPwr(GnbProtocol.CellId.FIRST);
        } else {
            rg_cell.check(R.id.rb_cell_1);
            txpwr = TraceUtil.build().getTxPwr(GnbProtocol.CellId.SECOND);
        }
        rg_power = (RadioGroup) view.findViewById(R.id.rg_power);
        if (txPwrOffset == -1) {
            txPwrOffset = 0;
            rg_power.check(R.id.rb_normal);
        } else {
            if (txPwrOffset == 3) {
                rg_power.check(R.id.rb_far);
            } else if (txPwrOffset == 0) {
                rg_power.check(R.id.rb_normal);
            } else {
                rg_power.check(R.id.rb_near);
            }
        }
        final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (rg_power.getCheckedRadioButtonId()) {
                    case R.id.rb_far:
                        txPwrOffset = 3;
                        break;
                    case R.id.rb_normal:
                        txPwrOffset = 0;
                        break;
                    case R.id.rb_near:
                        txPwrOffset = -3;
                        break;
                }
                switch (rg_cell.getCheckedRadioButtonId()) {
                    case R.id.rb_cell_1:
                        if (TraceUtil.build().isEnable(GnbProtocol.CellId.SECOND)) {
                            TraceUtil.build().setTxPwr(GnbProtocol.CellId.SECOND, txPwrOffset);
                            MessageController.build().setTxPwrOffset(GnbProtocol.CellId.SECOND, Integer.parseInt(TraceUtil.build().getArfcn(GnbProtocol.CellId.SECOND)), txPwrOffset);
                        } else {
                            showToast("通道二未开启定位，请定位先");
                        }
                        break;
                    case R.id.rb_cell_0:
                        if (TraceUtil.build().isEnable(GnbProtocol.CellId.FIRST)) {
                            TraceUtil.build().setTxPwr(GnbProtocol.CellId.FIRST, txPwrOffset);
                            MessageController.build().setTxPwrOffset(GnbProtocol.CellId.FIRST, Integer.parseInt(TraceUtil.build().getArfcn(GnbProtocol.CellId.FIRST)), txPwrOffset);
                        } else {
                            showToast("通道一未开启定位，请定位先");
                        }
                        break;
                }
                closeCustomDialog();
            }
        });
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }
    /**
    * @description 频点轮循
    * @param
    * @return
    * @author
    * @time
    */

    private void showArfcnSetChangeDialog() {
        if (mSetArfcnChangeDialog != null) {
            mSetArfcnChangeDialog.show();
        } else {
            mSetArfcnChangeDialog = new SetArfcnChangeDialog(mContext);
            try {
                setArfcnList_N1.clear();
                setArfcnList_N28.clear();
                setArfcnList_N41.clear();
                setArfcnList_N78.clear();
                setArfcnList_N79.clear();
                setArfcnList_N1.addAll(Util.json2Int(PrefUtil.build().getArfcn("N1"+"cfg"),"N1" ));
                setArfcnList_N28.addAll(Util.json2Int(PrefUtil.build().getArfcn("N28"+"cfg"),"N28" ));
                setArfcnList_N41.addAll(Util.json2Int(PrefUtil.build().getArfcn("N41"+"cfg"),"N41" ));
                setArfcnList_N78.addAll(Util.json2Int(PrefUtil.build().getArfcn("N78"+"cfg"),"N78" ));
                setArfcnList_N79.addAll(Util.json2Int(PrefUtil.build().getArfcn("N79"+"cfg"),"N79" ));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSetArfcnChangeDialog.setList(setArfcnList_N1, setArfcnList_N28, setArfcnList_N41, setArfcnList_N78, setArfcnList_N79);
            mSetArfcnChangeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    mSetArfcnChangeDialog = null;
                }
            });
            mSetArfcnChangeDialog.show();
        }
    }
    /**
     * 重启设备
     *
     * @param title
     * @param msg
     */
    private void showRebootDialog(String title, String msg) {
        createCustomDialog();

        View view = mInflater.inflate(R.layout.dialog_confirm, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        tv_title.setText(title);
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        tv_msg.setText(msg);

        final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWorkState(GnbBean.State.REBOOT);
                refreshStateView(getStr(R.string.state_reboot_cfg));
                MessageController.build().setOnlyCmd(GnbProtocol.UI_2_gNB_REBOOT_gNB);
                TraceUtil.build().setEnable(GnbProtocol.CellId.FIRST, false);
                TraceUtil.build().setEnable(GnbProtocol.CellId.SECOND, false);
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ZTcpService.build().reconnect();
                    }
                }, 20*1000);
                closeCustomDialog();
            }
        });
        final Button btn_canel = (Button) view.findViewById(R.id.btn_cancel);
        btn_canel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }
    /**
     * 重启设备
     *
     * @param title
     * @param msg
     */
    private void showStopFreqScanDialog(String title, String msg) {
        createCustomDialog();

        View view = mInflater.inflate(R.layout.dialog_confirm, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        tv_title.setText(title);
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        tv_msg.setText(msg);

        final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStopScan = true;
                scanCount = 0;
                StopFreqScanTime = System.currentTimeMillis();
                progressDialog = ProgressDialog.show(G50Activity.this, "请稍后", "正在结束扫频");
                refreshStateView(getStr(R.string.state_stop_arfcn));
                closeCustomDialog();
            }
        });
        final Button btn_canel = (Button) view.findViewById(R.id.btn_cancel);
        btn_canel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    private void showUpgradeDialog() {
        createCustomDialog();
        mUpdateFilesList.clear();
        mUpdateFilesList = FileUtil.build().getUpdateFileList();
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_updata_file, null);
        // 附近频点
        TextView emptylist = (TextView) view.findViewById(R.id.file_list_empty);
        ListView fileListView = (ListView) view.findViewById(R.id.file_list);
        FileAdapter fileAdapter = new FileAdapter(G50Activity.this, mUpdateFilesList);
        fileListView.setAdapter(fileAdapter);

        if (mUpdateFilesList != null && mUpdateFilesList.size() <= 0) {
            emptylist.setVisibility(View.VISIBLE);
            fileListView.setVisibility(View.GONE);
        } else {
            emptylist.setVisibility(View.GONE);
            fileListView.setVisibility(View.VISIBLE);
        }
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                setWorkState(Enum.State.UPDATE);
                refreshStateView(getStr(R.string.state_upgrade_copying_file));
                upgradeFilePath = mUpdateFilesList.get(pos).getPath();
                upgradeFileName = mUpdateFilesList.get(pos).getFileName();
                FTPUtil.build().startPutFile(upgradeFilePath);
                closeCustomDialog();
            }
        });
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    private void showGetBAPPLogDialog() {
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_create_log, null);
        final EditText ed_file_name = (EditText) view.findViewById(R.id.ed_file_name);
        Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gnbLogName = "";
                String time = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmm");
                String name = ed_file_name.getEditableText().toString().trim();
                if (name.startsWith("+")||name.startsWith("-")||name.startsWith("/")||name.startsWith("@")){
                    Util.showToast(mContext, "请输入正确的文件名！");
                    return;
                }
                if (TextUtils.isEmpty(name)){
                    Util.showToast(mContext, "请输入正确的文件名！");
                    return;
                }
                byte[] bytes = name.getBytes();
                if (bytes.length > 35) { // 64 - 15 = 49
                    showToast(getResources().getString(R.string.error_log_name));
                    return;
                }
                gnbLogName = name + "_" + time;
                gnbLogName = gnbLogName.replaceAll(" ", "");
                setWorkState(GnbBean.State.GETLOG);
                tv_process.setText("进度: 0 / 100");
                tv_process.setVisibility(View.VISIBLE);
                tv_imsi_cnt.setVisibility(View.GONE);
                tv_imsi.setVisibility(View.GONE);
                refreshStateView(getStr(R.string.state_get_log));
                MessageController.build().getLog(3, gnbLogName);
                closeCustomDialog();
            }
        });
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    /**
     * 配置设备当前工作状态
     *
     * @param state
     */
    private void setWorkState(int state) {
        APPLog.I("setWorkState: state = " + state);
        workState = state;
        mSlideLeftFrag.setWorkState(state);
    }

    private void setTraceState(int cell_id, int state) {
        APPLog.I("setTraceState[" + cell_id + "]" + ", state = " + state);
        if (cell_id == -1){
            setWorkState(GnbBean.State.NONE);
            return;
        }
        TraceUtil.build().setWorkState(cell_id, state);
        setWorkState(state);
    }
    /**
     * 定位报值
     *
     * @param rsrp
     * @param rssi
     * @param reset
     */
    private void refreshTraceBarView(int rsrp, int rssi, boolean reset) {
        if (reset) {
            traceBarData.reInit();
            tv_value.setText("");
//            iv_top.setImageResource(R.mipmap.top_bg_n);
        } else {
            if (rsrp != 0) {
                tv_value.setText("" + rsrp);
                traceBarData.handleData(rsrp);
//                iv_top.setImageResource(R.mipmap.top_bg_v);
            } else {
                tv_value.setText("");
                traceBarData.handleData(0);
//                iv_top.setImageResource(R.mipmap.top_bg_n);
            }
        }
//        if (traceBarEnable) {
//            traceBarView.refreshData(traceBarData.getList());
//        }
    }


    @Override
    public void onTraceConfig(int cell_id) {
        //TraceDialog
        APPLog.I("onTraceConfig()");
        noTraceFlag = false;
        mHandler.removeMessages(2);
        mHandler.removeMessages(3);
        mHandler.removeMessages(6);
        mHandler.removeMessages(7);
        isFirstStart = false;
        isSecondStart = false;
        isThirdStart = false;
        isFourthStart = false;
        if (TraceUtil.build().getSwap_rf(cell_id)==1){
            if (cell_id == GnbProtocol.CellId.FIRST){
                PaCtl.build().initPA(TraceUtil.build().getArfcn(GnbProtocol.CellId.SECOND),GnbProtocol.CellId.SECOND);
            }else {
                PaCtl.build().initPA(TraceUtil.build().getArfcn(GnbProtocol.CellId.FIRST),GnbProtocol.CellId.FIRST);
            }
        }else {
            PaCtl.build().initPA(TraceUtil.build().getArfcn(cell_id),cell_id);
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MessageController.build().setTraceType(GnbProtocol.TraceType.TRACE);
        MessageController.build().setBlackList(cell_id,false, blackList.size(), blackList);
        btn_trace.setText("结束");
        setTraceState(cell_id, GnbBean.State.BLACKLIST);

        if(!TextUtils.isEmpty(TraceUtil.build().getArfcn(GnbProtocol.CellId.SECOND))){
            setTraceState(GnbProtocol.CellId.SECOND, GnbBean.State.BLACKLIST);
        }
        refreshStateView(getStr(R.string.state_set_black_list));
        traceBarEnable = true;
        traceBarView.setVisibility(View.VISIBLE);

    }

    @Override
    public void OnLoginSucessful(String user, String ajbh) {
        //LoginDialog
        APPLog.I("OnLoginSucessful()");
    }

    @Override
    public void OnLoginFail() {
        //LoginDialog
        APPLog.I("OnLoginSucessful: finish()");
        finish();
    }

    @Override
    public void onImportArfcn(String arfcn, String pci,boolean isScan) {
        APPLog.I("onImportArfcn()");
        if (workState != GnbBean.State.IDLE){
            showToast(getStr(R.string.state_busy));
            return;
        }
        startTraceDialog(arfcn, pci);
    }

    /*@Override
    public void onSlideMenuGetBAPPLog() {
        APPLog.D("onSlideMenuGetBAPPLog()");
        if (workState == Enum.State.TRACE) {
            showToast(getStr(R.string.state_tracing));
            return;
        }
        showGetBAPPLogDialog();
        getSlidingMenu().showContent(); // 关闭菜单
    }*/
    private void showGetOpLogDialog() {
        createCustomDialog();

        View view = mInflater.inflate(R.layout.dialog_create_oplog, null);
        final EditText ed_file_name = (EditText) view.findViewById(R.id.ed_file_name);
        Button btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gnbLogName = "";
                String time = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmmss");
                String name = ed_file_name.getEditableText().toString().trim();
                if (name.startsWith("+")||name.startsWith("-")||name.startsWith("/")||name.startsWith("@")){
                    Util.showToast(mContext, "请输入正确的文件名！");
                    return;
                }
                if (TextUtils.isEmpty(name)){
                    Util.showToast(mContext, "请输入正确的文件名！");
                    return;
                }
                byte[] bytes = name.getBytes();
                if (bytes.length > 35) { // 64 - 15 = 49
                    Util.showToast(getApplicationContext(), getResources().getString(R.string.error_log_name));
                    return;
                }
                gnbLogName = name + "_" + time;
                gnbLogName = gnbLogName.replaceAll(" ", "");
                setWorkState(GnbBean.State.GETLOG);
                refreshStateView(getStr(R.string.state_get_op_log));
                tv_process.setText("进度: 0 / 100");
                tv_process.setVisibility(View.VISIBLE);
                tv_imsi_cnt.setVisibility(View.GONE);
                tv_imsi.setVisibility(View.GONE);
                MessageController.build().getOpLog(3, gnbLogName);
                closeCustomDialog();
            }
        });
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }
    /**
     * 配置工作模式
     */
    private ProgressDialog progressDialog;
    private void showDualCellDialog() {
        APPLog.D("showDualCellDialog()");
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_work_mode, null);
        rb_mode = (RadioGroup) view.findViewById(R.id.rb_mode);
        if (dualCell == 1) {
            rb_mode.check(R.id.rb_single);
        } else {
            rb_mode.check(R.id.rb_dual);
        }
        final Button btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (rb_mode.getCheckedRadioButtonId()) {
                    case R.id.rb_single:
                        dualCell = GnbProtocol.DualCell.SINGLE;
                        APPLog.D("clickWorkModeBtn() GnbProtocol.DualCell.SINGLE");
                        break;
                    case R.id.rb_dual:
                        dualCell = GnbProtocol.DualCell.DUAL;
                        APPLog.D("clickWorkModeBtn() GnbProtocol.DualCell.DUAL");
                        break;
                }

                MessageController.build().setDualCell(dualCell);
                closeCustomDialog();

                progressDialog = ProgressDialog.show(mContext, "请稍后", "正在切换工作模式");
            }
        });
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }
    int isDualSsbBitmap = PrefUtil.build().getSsbBitmapModel();
    private void showSsbBitmapDialog() {
        APPLog.D("showSsbBitmapDialog()");
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_ssb_bitmap_mode, null);
        rb_mode = (RadioGroup) view.findViewById(R.id.rb_mode);

        if (isDualSsbBitmap == 1) {
            rb_mode.check(R.id.rb_single);
        } else {
            rb_mode.check(R.id.rb_dual);
        }
        final Button btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (rb_mode.getCheckedRadioButtonId()) {
                    case R.id.rb_single:
                        isDualSsbBitmap = GnbProtocol.DualCell.SINGLE;
                        PrefUtil.build().setSsbBitmapModel(isDualSsbBitmap);
                        APPLog.D("clickSsbBitmapBtn() 选择了单波束");
                        break;
                    case R.id.rb_dual:
                        isDualSsbBitmap = GnbProtocol.DualCell.DUAL;
                        PrefUtil.build().setSsbBitmapModel(isDualSsbBitmap);
                        APPLog.D("clickWorkModeBtn() 选择了多波束");
                        break;
                }
                closeCustomDialog();
            }
        });
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    private GpsOffsetTestDialog gpsOffsetTestDialog;
    @Override
    public void onSlideMenuItemClick(int type) {
        APPLog.D("onSlideMenuItemClick() type = " + type);
        if (type == SlideLeftFragment.ClickType.tv_set_device_type){
            if (workState == GnbBean.State.FREQ_SCAN||workState == GnbBean.State.TRACE) {
                showToast(getStr(R.string.state_busy));
                return;
            }
            setPaTypeDialog();
        }
        if (type == SlideLeftFragment.ClickType.tv_mob_reject_code){
            showToast("定位中，不可操作！");
            return;
        }
        if (!isCanWork()) return;

        if (type == SlideLeftFragment.ClickType.tv_city){
            if (gnbCityDialog == null) {
                gnbCityDialog = new GnbCityDialog(mContext, mInflater);
            }
            gnbCityDialog.show();
            return;
        }
        if (type == SlideLeftFragment.ClickType.tv_gps_offset){
            if (DeviceInfo.build().getGpsEnable() != GnbStateRsp.Gps.SUCC) {
                showToast(getStr(R.string.state_gps_unlock));
                return;
            }
            if (gpsOffsetTestDialog == null) {
                gpsOffsetTestDialog = new GpsOffsetTestDialog(mContext);
            }
            gpsOffsetTestDialog.show();
            return;
        }
        if (workState == GnbBean.State.TRACE) {
            showToast(getStr(R.string.state_tracing));
            return;
        }
        switch (type){
            case SlideLeftFragment.ClickType.tv_ssb_bitmap:
                if (workState == GnbBean.State.FREQ_SCAN) {
                    showToast(getStr(R.string.state_scanning));
                    return;
                }
                showSsbBitmapDialog();
                break;
            case SlideLeftFragment.ClickType.tv_dual_cell:
                if (workState == GnbBean.State.FREQ_SCAN) {
                    showToast(getStr(R.string.state_scanning));
                    return;
                }

                showDualCellDialog();
                break;
            case SlideLeftFragment.ClickType.tv_op:
                if (workState != GnbBean.State.IDLE) {
                    showToast(getStr(R.string.state_busy));
                    return;
                }
                showGetOpLogDialog();
                break;
            case SlideLeftFragment.ClickType.tv_log:
                if (workState == GnbBean.State.FREQ_SCAN) {
                    showToast(getStr(R.string.state_scanning));
                    return;
                }

                showGetBAPPLogDialog();
                break;
            case SlideLeftFragment.ClickType.tv_upgrade:
                if (workState == GnbBean.State.FREQ_SCAN) {
                    showToast(getStr(R.string.state_scanning));
                    return;
                }

                showUpgradeDialog();
                break;
            case SlideLeftFragment.ClickType.tv_reboot:
                if (workState == GnbBean.State.FREQ_SCAN) {
                    showToast(getStr(R.string.state_scanning));
                    return;
                }

                showRebootDialog(getStr(R.string.warning), getStr(R.string.reboot_device));
                break;
            case SlideLeftFragment.ClickType.tv_arfcn_set_change:
                if (workState == GnbBean.State.FREQ_SCAN) {
                    showToast(getStr(R.string.state_scanning));
                    return;
                }

                showArfcnSetChangeDialog();
                break;

        }
        getSlidingMenu().showContent(); // 关闭菜单
    }



    @Override
    public void onSlideMenuItemToggle(int type, boolean checked) {
        APPLog.D("onSlideMenuItemToggle() type = " + type);
        switch (type){
            case SlideLeftFragment.ClickType.tb_trace_bar:
                traceBarEnable = checked;
                traceBarView.setVisibility(checked ? View.VISIBLE : View.GONE);
                break;
            case SlideLeftFragment.ClickType.tb_mode:
                if (workState == GnbBean.State.TRACE) {
                    showToast(getStr(R.string.state_tracing));
                    mSlideLeftFrag.refreshMode(workMode);
                    return;
                }
                if (workMode == Enum.WorkMode.NORMAL) {
                    workMode = Enum.WorkMode.VEHICLE;
                } else {
                    workMode = Enum.WorkMode.NORMAL;
                }
                break;
            case SlideLeftFragment.ClickType.tb_mob_reject_code:
                if (checked) {
                    TraceUtil.build().setMobRejectCode(GnbProtocol.CellId.FIRST, 9);
                    TraceUtil.build().setMobRejectCode(GnbProtocol.CellId.SECOND, 9);
                } else {
                    TraceUtil.build().setMobRejectCode(GnbProtocol.CellId.FIRST, 0);
                    TraceUtil.build().setMobRejectCode(GnbProtocol.CellId.SECOND, 0);
                }
                break;
        }
        //getSlidingMenu().showContent(); // 关闭菜单
    }

    @Override
    public void onSocketStateChange(int lastState, int state) {
        APPLog.D("MainActivity: onSocketStateChange() lastState = " + lastState + ", state = " + state);
        if (state == ConnectProtocol.SOCKET.STATE_CONNECTING) {
            refreshStateView(getStr(R.string.state_socket_conecting));
            workState = GnbBean.State.NONE;
        } else if (state == ConnectProtocol.SOCKET.STATE_CONNECTED) {
            refreshStateView(getStr(R.string.state_socket_conected));
            workState = GnbBean.State.NONE;
            isStart = true;
        } else {
            refreshStateView(getStr(R.string.state_socket_disconnect));
            ZTcpService.build().reconnect();
            btn_trace.setText("定位");
            btn_arfcn.setText("扫频");
            setWorkState(GnbBean.State.IDLE);
            setTraceState(GnbProtocol.CellId.FIRST,GnbBean.State.NONE);
            setTraceState(GnbProtocol.CellId.SECOND,GnbBean.State.NONE);
            workState = GnbBean.State.NONE;
        }
    }
    private Thread heartBeatThread;
    private long receivedTime = System.currentTimeMillis();
    private final long HEART_BEAT_RATE = 20 * 1000;
    public void heartBeatTimerOut() {
        heartBeatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (System.currentTimeMillis() - receivedTime > HEART_BEAT_RATE) {
                        receivedTime = System.currentTimeMillis();
//                        Message msg = new Message();
//                        msg.what = 4;
//                        mHandler.sendMessage(msg);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        heartBeatThread.start();
    }
    @Override
    public void onHeartStateRsp(GnbStateRsp rsp) {
        if (rsp != null) {
            APPLog.I("onHeartStateRsp()：" + rsp);
            DeviceInfo.build().setGpsEnable(rsp.getGpsSyncState());
            if (isFirstRec&&workState == GnbBean.State.IDLE) {
                isFirstRec = false;
                dualCell = rsp.getDualCell();
                PrefUtil.build().setCell(dualCell);
                mSlideLeftFrag.setDualCell(dualCell);
                tv_2_value.setText("");
                if (dualCell == 2) {
                    tv_2_value.setVisibility(View.VISIBLE);
                }else {
                    tv_2_value.setVisibility(View.INVISIBLE);
                }
                MessageController.build().setOnlyCmd(GnbProtocol.UI_2_gNB_QUERY_gNB_VERSION);
            }
//            if (isStopScan && workState == GnbBean.State.FREQ_SCAN) {
//                setWorkState(GnbBean.State.IDLE);
//            }
            if (isStopScan && workState == GnbBean.State.FREQ_SCAN && (System.currentTimeMillis() - StopFreqScanTime > 40 * 1000)) {
                APPLog.I("扫频结束超时");
                MessageController.build().setOnlyCmd(GnbProtocol.OAM_MSG_STOP_FREQ_SCAN);
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                PaCtl.build().closePA();
                btn_arfcn.setText("扫频");
                arfcnAdapter.setScan(false);
//                setWorkState(GnbBean.State.STOP);
                workState = GnbBean.State.IDLE;
            }
            if (rsp.getDevState() != GnbStateRsp.devState.NORMAL) {
                // 非定位状态走这里
                // 定位状态下，走GNB_STATE_PHY_ABNORMAL
                setWorkState(GnbBean.State.IDLE);
                showRemindDialog("警告",getStr(R.string.state_dev_abnormal));
                return;
            }
            int firstState = TraceUtil.build().getWorkState(GnbProtocol.CellId.FIRST);
            int secondState = TraceUtil.build().getWorkState(GnbProtocol.CellId.SECOND);
            if (isStart) {
                PaCtl.build().closePA();
                isStart = false;
                if (rsp.getFirstState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                    if (firstState != GnbBean.State.TRACE && firstState != GnbBean.State.CFG_TRACE && firstState != GnbBean.State.STOP) {
                        // 定位时非法退出先结束定位
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setWorkState(GnbBean.State.STOP);
                                refreshStateView(getStr(R.string.state_stop_trace));
                                MessageController.build().setCmdAndCellID(GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.FIRST);
                            }
                        }, 200);

                    }
                }
                if (rsp.getSecondState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                    if (secondState != GnbBean.State.TRACE && secondState != GnbBean.State.CFG_TRACE && secondState != GnbBean.State.STOP) {
                        // 定位时非法退出先结束定位
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setWorkState(GnbBean.State.STOP);
                                refreshStateView(getStr(R.string.state_stop_trace));
                                MessageController.build().setCmdAndCellID(GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.SECOND);
                            }
                        }, 400);

                    }
                }
            }
            receivedTime = System.currentTimeMillis();
            refreshBattery();
            handleBoardDetectedStatus(rsp);
            dualCell = rsp.getDualCell();
//            if (isSetModule) {
//                isSetModule = false;
                if (dualCell == GnbProtocol.DualCell.SINGLE) {
                    if (tv_2_value.getVisibility() == View.VISIBLE) {
                        tv_2_value.setVisibility(View.INVISIBLE);
//                        tab_cell_1.setVisibility(View.GONE);
                    }
                } else if (dualCell == GnbProtocol.DualCell.DUAL) {
                    if (tv_2_value.getVisibility() == View.INVISIBLE) {
                        tv_2_value.setVisibility(View.VISIBLE);
//                        tab_cell_1.setVisibility(View.VISIBLE);
                    }
                }
//            }
            if (rsp.getDevState() != GnbStateRsp.devState.NORMAL) {
                if (firstState != GnbBean.State.TRACE && secondState != GnbBean.State.TRACE) {
                    // 非定位状态走这里
                    // 定位状态下，走GNB_STATE_PHY_ABNORMAL
                    setWorkState(GnbBean.State.IDLE);
                    refreshStateView(getStr(R.string.state_phy_abnormal));
                    return;
                }
            }
            switch (rsp.getFirstState()) {
                case GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG:
                    if (workState == GnbBean.State.REBOOT) {
                        APPLog.I("onHeartStateRsp(): rebootCnt = " + rebootCnt);
                        if (++rebootCnt > 2) {
                            rebootCnt = 0;
                            setWorkState(GnbBean.State.IDLE);
                            refreshStateView(getStr(R.string.state_wait_cfg));
                        } else {
                            setWorkState(GnbBean.State.REBOOT);
                            //refreshStateView(getStr(R.string.state_reboot));
                        }
                    } else {
                        if (workState == GnbBean.State.NONE || workState == GnbBean.State.IDLE || workState == GnbBean.State.STOP || workState == GnbBean.State.PHY_ABNORMAL) {
                            if (rsp.getSecondState() == GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG) {
                                if (!bsTimeSet) {
                                    bsTimeSet = true;
                                    MessageController.build().setGnbTime();
                                }
                                setWorkState(GnbBean.State.IDLE);
                                refreshStateView(getStr(R.string.state_wait_cfg));
                                TraceUtil.build().setEnable(GnbProtocol.CellId.FIRST, false);
                                TraceUtil.build().setEnable(GnbProtocol.CellId.SECOND, false);
                            }
                        }
                    }
                    break;
                case GnbStateRsp.gnbState.GNB_STATE_PHY_ABNORMAL:
                    if (firstState == GnbBean.State.TRACE || firstState == GnbBean.State.CATCH) {
                        setWorkState(GnbBean.State.PHY_ABNORMAL);
                        refreshStateView(getStr(R.string.state_phy_abnormal));
                    }
                    break;
                case GnbStateRsp.gnbState.GNB_STATE_CATCH:
                case GnbStateRsp.gnbState.GNB_STATE_CONTROL:
                    break;
                case GnbStateRsp.gnbState.GNB_STATE_TRACE:
                    setWorkState(GnbBean.State.TRACE);
                    refreshStateView(getStr(R.string.state_trace));
                    if (ZTcpService.build().isConnected()) {
                        firstState = TraceUtil.build().getWorkState(GnbProtocol.CellId.FIRST);
                        if (firstState == GnbBean.State.TRACE || firstState == GnbBean.State.CFG_TRACE) {
                            setTraceState(GnbProtocol.CellId.FIRST, GnbBean.State.TRACE);
                        }
                        APPLog.I("定位中状态 = " + firstState);
                    }
                    break;
            }
            switch (rsp.getSecondState()) {
                case GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG:
                case GnbStateRsp.gnbState.GNB_STATE_CATCH:
                case GnbStateRsp.gnbState.GNB_STATE_CONTROL:
                    break;
                case GnbStateRsp.gnbState.GNB_STATE_PHY_ABNORMAL:
                    if (secondState == GnbBean.State.TRACE || secondState == GnbBean.State.CATCH) {
                        setWorkState(GnbBean.State.PHY_ABNORMAL);
                        refreshStateView(getStr(R.string.state_phy_abnormal));
                    }
                    break;
                case GnbStateRsp.gnbState.GNB_STATE_TRACE:
                    setWorkState(GnbBean.State.TRACE);
                    refreshStateView(getStr(R.string.state_trace));
                    if (ZTcpService.build().isConnected()) {
                        secondState = TraceUtil.build().getWorkState(GnbProtocol.CellId.SECOND);
                        if (secondState == GnbBean.State.TRACE || secondState == GnbBean.State.CFG_TRACE) {
                            setTraceState(GnbProtocol.CellId.SECOND, GnbBean.State.TRACE);
                        }
                        APPLog.I("定位中状态 = " + secondState);
                    }
                    break;
            }
        }
    }
    private void handleBoardDetectedStatus(GnbStateRsp rsp) {
        if (rsp.getTempList().size()>0){
            if (rsp.getTempList().get(0)<70){
                iv_temp.setImageResource(R.mipmap.icon_temperaturel);
            }else if (rsp.getTempList().get(0)>=70&&rsp.getTempList().get(0)<80){
                iv_temp.setImageResource(R.mipmap.icon_temperaturel_mid);
            }else {
                iv_temp.setImageResource(R.mipmap.icon_temperaturel_hight);
            }
            tv_temp.setText(String.valueOf(rsp.getTempList().get(0)));
        }
        if (rsp.getGpsSyncState() == GnbStateRsp.Gps.SUCC) {
            tv_gps.setText("同步");
            isGps_sync = true;
        } else {
            tv_gps.setText("失步");
            isGps_sync = false;
            if (!isStopScan && isGpsScan) {
                Util.showToast(mContext, getStr(R.string.error_gps_not_suc));
                isStopScan = true;
                workState = GnbBean.State.IDLE;
                btn_arfcn.setText("扫频");
            }
        }
        if (rsp.getFirstAirState() == GnbStateRsp.Air.SUCC) {
            tv_air_sync.setText("同步");
        } else if (rsp.getFirstAirState() == GnbStateRsp.Air.FAIL) {
            tv_air_sync.setText("失步");
        } else {
            tv_air_sync.setText("空闲");
        }
        if (rsp.getSecondAirState() == GnbStateRsp.Air.SUCC) {
            tv_air_sync1.setText("同步");
        } else if (rsp.getFirstAirState() == GnbStateRsp.Air.FAIL) {
            tv_air_sync1.setText("失步");
        } else {
            tv_air_sync1.setText("空闲");
        }

    }
    /**
     * 设备电量检测
     */
    private void refreshBattery() {
        if (DataUtil.isNumeric(Battery.build().getPercent())) {
            battery.setText(MessageFormat.format("{0}%", Battery.build().getPercent()));
            batteryView.setPower(Integer.parseInt(Battery.build().getPercent()));
            if (Integer.parseInt(Battery.build().getPercent()) < 20) {
                /*if ( isFirst ){
                    new Util().dialogTips( MainActivity.this );
                    isFirst = false;
                }*/
            }
        } else {
            battery.setText("0%");
            batteryView.setPower(0);
        }
    }


    @Override
    public void onStartTraceRsp(GnbTraceRsp rsp) {
        if (rsp != null) {
            APPLog.I("onStartTraceRsp(): " + rsp);
            if (rsp.getCmdRsp() != null) {
                final int cell_id = rsp.getCmdRsp().getCellId();
                if (rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_gNB_START_TRACE) {
                    if (rsp.getCmdRsp().getRspValue() == GnbProtocol.OAM_ACK_OK) {
                        getArfcnList(cell_id, TraceUtil.build().getArfcn(cell_id));
                        if (cell_id == GnbProtocol.CellId.FIRST){
                            APPLog.I("TraceUtil.build().getArfcnSetChange "+TraceUtil.build().getArfcnSetChange(GnbProtocol.CellId.FIRST));

                            if (TraceUtil.build().getArfcnSetChange(GnbProtocol.CellId.FIRST)){
                                mHandler.sendEmptyMessageDelayed(11, 60 * 1000);
                            }
                        }else if (cell_id == GnbProtocol.CellId.SECOND){
                            if (TraceUtil.build().getArfcnSetChange(GnbProtocol.CellId.SECOND)){
                                mHandler.sendEmptyMessageDelayed(12, 60*1000);
                            }
                        }
                        final int cellId = rsp.getCmdRsp().getCellId();
                        if (cellId == GnbProtocol.CellId.FIRST){
                            tv_value.setBackgroundResource(R.mipmap.home_bg_n3);
                        }else if (cellId == GnbProtocol.CellId.SECOND){
                            tv_2_value.setBackgroundResource(R.mipmap.home_bg_n3);
                        }
                        setTraceState(cellId, GnbBean.State.CFG_TRACE);

                        TraceUtil.build().setEnable(cellId, true);

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                OpLog.build().write(DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t开始定位，目标IMSI: " + TraceUtil.build().getImsi(cellId));
                            }
                        }, 500);

                    } else {
                        showErrorResult(rsp.getCmdRsp().getCellId(),rsp.getCmdRsp().getRspValue());
                        TraceUtil.build().setTacChange(rsp.getCmdRsp().getCellId(),false);
                        TraceUtil.build().setEnable(rsp.getCmdRsp().getCellId(), false);
                        setTraceState(rsp.getCmdRsp().getCellId(), GnbBean.State.NONE);
                        if(rsp.getCmdRsp().getCellId() == GnbProtocol.CellId.FIRST){
                            showRemindDialog(getStr(R.string.reminder), "小区一定位启动失败");
                        }else if (rsp.getCmdRsp().getCellId() == GnbProtocol.CellId.SECOND){
                            showRemindDialog(getStr(R.string.reminder), "小区二定位启动失败");
                            workState = GnbBean.State.IDLE;
                            btn_trace.setText("定位");
                            refreshStateView(getStr(R.string.state_wait_cfg));
                        }
                        if (rsp.getCmdRsp().getCellId() == GnbProtocol.CellId.FIRST) {       //通道一定位成功后再开始通道二的定位流程
                            if (dualCell == GnbProtocol.DualCell.DUAL) { //双小区
                                if (!TextUtils.isEmpty(TraceUtil.build().getArfcn(GnbProtocol.CellId.SECOND))){ //配了小区二
                                    if (TraceUtil.build().getSwap_rf(rsp.getCmdRsp().getCellId())==1){
                                        if (cell_id == GnbProtocol.CellId.FIRST){
                                            PaCtl.build().initPA(TraceUtil.build().getArfcn(GnbProtocol.CellId.SECOND),GnbProtocol.CellId.SECOND);
                                        }else {
                                            PaCtl.build().initPA(TraceUtil.build().getArfcn(GnbProtocol.CellId.FIRST),GnbProtocol.CellId.FIRST);
                                        }
                                    }else {
                                        PaCtl.build().initPA(TraceUtil.build().getArfcn(rsp.getCellId()),rsp.getCellId());
                                    }

                                    workState = GnbBean.State.IDLE;
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    MessageController.build().setBlackList(GnbProtocol.CellId.SECOND,false, blackList.size(), blackList);
                                }else { //如果没有配小区二
                                    workState = GnbBean.State.IDLE;
                                    btn_trace.setText("定位");
                                    refreshStateView(getStr(R.string.state_wait_cfg));
                                }
                            }else { //如果是单小区 业务结束
                                workState = GnbBean.State.IDLE;
                                btn_trace.setText("定位");
                                refreshStateView(getStr(R.string.state_wait_cfg));
                            }
                        }
                    }

                    if (rsp.getCmdRsp().getCellId() == GnbProtocol.CellId.FIRST) {       //通道一定位成功后再开始通道二的定位流程
                        if (dualCell == GnbProtocol.DualCell.DUAL) { //双小区
                            if (!TextUtils.isEmpty(TraceUtil.build().getArfcn(GnbProtocol.CellId.SECOND))) { //配了小区二
                                if (TraceUtil.build().getSwap_rf(rsp.getCmdRsp().getCellId()) == 1) {
                                    if (cell_id == GnbProtocol.CellId.FIRST) {
                                        PaCtl.build().initPA(TraceUtil.build().getArfcn(GnbProtocol.CellId.SECOND),GnbProtocol.CellId.SECOND);
                                    } else {
                                        PaCtl.build().initPA(TraceUtil.build().getArfcn(GnbProtocol.CellId.FIRST),GnbProtocol.CellId.FIRST);
                                    }
                                } else {
                                    PaCtl.build().initPA(TraceUtil.build().getArfcn(rsp.getCellId()),rsp.getCellId());
                                }
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                MessageController.build().setBlackList(GnbProtocol.CellId.SECOND, false, blackList.size(), blackList);
                            }
                        }
                    }
                }
            } else { // IMSI上报及上号报值
                if (rsp.getCellId() != -1) {
                    int cell_id = 0;
                    String traceArfcn = "0";
                    try {
                        if (Integer.parseInt(TraceUtil.build().getArfcn(GnbProtocol.CellId.FIRST)) >
                                Integer.parseInt(TraceUtil.build().getSplit_arfcn_dl(GnbProtocol.CellId.FIRST)) &&
                                Integer.parseInt(TraceUtil.build().getSplit_arfcn_dl(GnbProtocol.CellId.FIRST)) != 0) {
                            String temp = TraceUtil.build().getArfcn(GnbProtocol.CellId.FIRST);
                            TraceUtil.build().setArfcn(GnbProtocol.CellId.FIRST, TraceUtil.build().getSplit_arfcn_dl(GnbProtocol.CellId.FIRST));
                            TraceUtil.build().setSplit_arfcn_dl(GnbProtocol.CellId.FIRST, temp);
                        }
                        if (!TextUtils.isEmpty(TraceUtil.build().getArfcn(GnbProtocol.CellId.SECOND))) {
                            if (Integer.parseInt(TraceUtil.build().getArfcn(GnbProtocol.CellId.SECOND)) >
                                    Integer.parseInt(TraceUtil.build().getSplit_arfcn_dl(GnbProtocol.CellId.SECOND)) &&
                                    Integer.parseInt(TraceUtil.build().getSplit_arfcn_dl(GnbProtocol.CellId.SECOND)) != 0) {
                                String temp = TraceUtil.build().getArfcn(GnbProtocol.CellId.SECOND);
                                TraceUtil.build().setArfcn(GnbProtocol.CellId.SECOND, TraceUtil.build().getSplit_arfcn_dl(GnbProtocol.CellId.SECOND));
                                TraceUtil.build().setSplit_arfcn_dl(GnbProtocol.CellId.SECOND, temp);
                            }
                        }

                    } catch (ClassCastException | NumberFormatException e) {
                        //e.printStackTrace();
                    }
                    if (rsp.getCellId() == 0) {
                        cell_id = GnbProtocol.CellId.FIRST;
                        traceArfcn = TraceUtil.build().getArfcn(cell_id);
                    } else if (rsp.getCellId() == 1) {
                        cell_id = GnbProtocol.CellId.SECOND;
                        traceArfcn = TraceUtil.build().getArfcn(cell_id);
                    } else if (rsp.getCellId() == 2) {
                        cell_id = GnbProtocol.CellId.FIRST;
                        traceArfcn = TraceUtil.build().getSplit_arfcn_dl(cell_id);
                    } else if (rsp.getCellId() == 3) {
                        cell_id = GnbProtocol.CellId.SECOND;
                        traceArfcn = TraceUtil.build().getSplit_arfcn_dl(cell_id);
                    }
                    String traceImsi = TraceUtil.build().getImsi(cell_id);
                    String tracePci = TraceUtil.build().getPci(cell_id);

//                    APPLog.I("cell "+cell_id+" arfcn="+traceArfcn+"  traceImsi = " + traceImsi + "  rsrp = " + rsp.getRsrp());
                    if ((rsp.getRsrp() == -1 || rsp.getRsrp() == 0) && (traceImsi.isEmpty() ||
                            traceArfcn.isEmpty() || traceArfcn.equals("0") || tracePci.isEmpty())) return;
                    List<String> ilist = rsp.getImsiList();
                    if (ilist != null && ilist.size() > 0) {
                        for (int i = 0; i < ilist.size(); i++) {
                            String imsi = ilist.get(i);
                            if (traceImsi.equals(imsi)) {
                                TraceUtil.build().setRsrp(rsp.getCellId(), rsp.getRsrp());
                            }
                            boolean add = true;
                            for (int j = 0; j < imsiList.size(); j++) {
                                if (imsiList.get(j).getImsi().equals(imsi)) {
                                    add = false;
                                    imsiList.get(j).setArfcn(traceArfcn);
                                    imsiList.get(j).setPci(tracePci);
                                    imsiList.get(j).setCellId(rsp.getCellId());
                                    imsiList.get(j).setLatestTime(System.currentTimeMillis());
                                    if (traceImsi.equals(imsi)) { // 当前定位IMSI
                                        if (imsiList.get(j).getState()!=ImsiBean.State.IMSI_BL){
                                            imsiList.get(j).setState(ImsiBean.State.IMSI_BL);
                                            ImsiBean imsiBean = imsiList.get(j);
                                            imsiList.remove(j);
                                            imsiList.add(0, imsiBean);
                                        }
                                    }
                                    break;
                                }
                            }
                            if (add) {
                                if (queryImsi.equals(imsi)) {
                                    showRemindDialog(getStr(R.string.reminder), getStr(R.string.imsi_detect) + queryImsi);
                                    queryImsi = "";
                                }
                                if (traceImsi.equals(imsi)) { // 当前定位IMSI
                                    imsiList.add(0, new ImsiBean(ImsiBean.State.IMSI_BL, imsi, traceArfcn, tracePci, System.currentTimeMillis(), rsp.getCellId()));
                                } else {
                                    boolean bl = false;
                                    for (int j = 0; j < blackList.size(); j++) {
                                        if (blackList.get(j).equals(imsi)) {
                                            bl = true;
                                            break;
                                        }
                                    }
                                    if (bl) {// 黑名单中的IMSI
                                        imsiList.add(1, new ImsiBean(ImsiBean.State.IMSI_BL, imsi, traceArfcn, tracePci, System.currentTimeMillis(), rsp.getCellId()));
                                    } else {
                                        imsiList.add(new ImsiBean(ImsiBean.State.IMSI_NEW, imsi, traceArfcn, tracePci, System.currentTimeMillis(),rsp.getCellId()));
                                    }
                                }
                            }
                        }
                        if (tv_imsi_cnt.getVisibility() != View.VISIBLE) {
                            tv_imsi_cnt.setVisibility(View.VISIBLE);
                        }
                        if (tv_imsi.getVisibility() != View.VISIBLE) {
                            tv_imsi.setVisibility(View.VISIBLE);
                        }
                        tv_imsi_cnt.setText(String.valueOf(imsiList.size()));
                        mSlideRightFrag.refreshListView(imsiList, imsiList.size() - 1, false);
                    }

//                    APPLog.D("报值上号 cell_id= " + rsp.getCellId() + "  rsrp = " + rsp.getRsrp() + isFirstStart + isSecondStart + isThirdStart + isFourthStart);
                    //载波分裂报值走这里
                    if (rsp.getCellId() == GnbProtocol.CellId.FIRST && !isFirstStart && rsp.getRsrp() != -1 && rsp.getRsrp() != 0) {
                        if (rsp.getImsiList().get(0).equals(TraceUtil.build().getImsi(GnbProtocol.CellId.FIRST))) {
                            countFirst = 0;
                            isFirstStart = true;
                            mHandler.sendEmptyMessageDelayed(2, TraceBean.RSRP_TIME_INTERVAL);
                        }
                    } else if (rsp.getCellId() == GnbProtocol.CellId.SECOND && !isSecondStart && rsp.getRsrp() != -1 && rsp.getRsrp() != 0) {
                        if (rsp.getImsiList().get(0).equals(TraceUtil.build().getImsi(GnbProtocol.CellId.SECOND))) {
                            countSecond = 0;
                            isSecondStart = true;
                            mHandler.sendEmptyMessageDelayed(3, TraceBean.RSRP_TIME_INTERVAL);
                        }
                    } else if (rsp.getCellId() == GnbProtocol.CellId.THIRD && !isThirdStart && rsp.getRsrp() != -1 && rsp.getRsrp() != 0) {
                        if (rsp.getImsiList().get(0).equals(TraceUtil.build().getImsi(GnbProtocol.CellId.FIRST))) {
                            countThird = 0;
                            isThirdStart = true;
                            mHandler.sendEmptyMessageDelayed(6, TraceBean.RSRP_TIME_INTERVAL);
                        }
                    } else if (rsp.getCellId() == GnbProtocol.CellId.FOURTH && !isFourthStart && rsp.getRsrp() != -1 && rsp.getRsrp() != 0) {
                        if (rsp.getImsiList().get(0).equals(TraceUtil.build().getImsi(GnbProtocol.CellId.SECOND))) {
                            countFourth = 0;
                            isFourthStart = true;
                            mHandler.sendEmptyMessageDelayed(7, TraceBean.RSRP_TIME_INTERVAL);
                        }
                    }
                }
            }
        }
    }
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    break;
                case 2:
                    if (TraceUtil.build().getWorkState(GnbProtocol.CellId.FIRST) != GnbBean.State.NONE &&
                            TraceUtil.build().getWorkState(GnbProtocol.CellId.FIRST) != GnbBean.State.STOP) {
                        int rsrp = -1;

                        if (TraceUtil.build().getRsrp(GnbProtocol.CellId.FIRST) != -1) {
                            rsrp = TraceUtil.build().getRsrp(GnbProtocol.CellId.FIRST);
                            countFirst = 0;
                        } else {
                            countFirst++;
                        }
                        if (countFirst < 6) {
                            if (rsrp != -1) refreshTraceValue(GnbProtocol.CellId.FIRST, rsrp);
                        } else {
                            if (isThirdStart) {
                                if (countThird != 0)
                                    refreshTraceValue(GnbProtocol.CellId.FIRST, rsrp);
                            } else {
                                refreshTraceValue(GnbProtocol.CellId.FIRST, rsrp);
                            }
                        }
                        mHandler.sendEmptyMessageDelayed(2, TraceBean.RSRP_TIME_INTERVAL);
                    }
                    break;
                case 3:
                    if (TraceUtil.build().getWorkState(GnbProtocol.CellId.SECOND) != GnbBean.State.NONE &&
                            TraceUtil.build().getWorkState(GnbProtocol.CellId.SECOND) != GnbBean.State.STOP) {
                        int rsrp = -1;

                        if (TraceUtil.build().getRsrp(GnbProtocol.CellId.SECOND) != -1) {
                            rsrp = TraceUtil.build().getRsrp(GnbProtocol.CellId.SECOND);
                            countSecond = 0;
                        } else {
                            countSecond++;
                        }
                        if (countSecond < 6) {
                            if (rsrp != -1) refreshTraceValue(GnbProtocol.CellId.SECOND, rsrp);
                        } else {
                            if (isFourthStart) {
                                if (countFourth != 0)
                                    refreshTraceValue(GnbProtocol.CellId.SECOND, rsrp);
                            } else {
                                refreshTraceValue(GnbProtocol.CellId.SECOND, rsrp);
                            }
                        }
                        mHandler.sendEmptyMessageDelayed(3, TraceBean.RSRP_TIME_INTERVAL);
                    }
                    break;
                case 4:
                    if (!tv_state.getText().toString().equals(getStr(R.string.state_socket_disconnect))
                            &&!tv_state.getText().toString().equals(getStr(R.string.state_socket_conecting))&&
                        workState!=GnbBean.State.TRACE&&workState!=GnbBean.State.FREQ_SCAN) {
                        refreshStateView(getStr(R.string.state_socket_disconnect));
                        setWorkState(GnbBean.State.NONE);
                    }
                    break;
                case 6:
                    if (TraceUtil.build().getWorkState(GnbProtocol.CellId.FIRST) != GnbBean.State.NONE &&
                            TraceUtil.build().getWorkState(GnbProtocol.CellId.FIRST) != GnbBean.State.STOP) {
                        int rsrp = -1;

                        if (TraceUtil.build().getRsrp(GnbProtocol.CellId.THIRD) != -1) {
                            rsrp = TraceUtil.build().getRsrp(GnbProtocol.CellId.THIRD);
                            countThird = 0;
                        } else {
                            countThird++;
                        }
                        if (countThird < 6) {
                            if (rsrp != -1) refreshTraceValue(GnbProtocol.CellId.THIRD, rsrp);
                        } else {
                            if (isFirstStart) {
                                if (countFirst != 0 && countFirst < 6)
                                    refreshTraceValue(GnbProtocol.CellId.THIRD, rsrp);
                            } else {
                                refreshTraceValue(GnbProtocol.CellId.THIRD, rsrp);
                            }
                        }
                        mHandler.sendEmptyMessageDelayed(6, TraceBean.RSRP_TIME_INTERVAL);
                    }
                    break;
                case 7:
                    if (TraceUtil.build().getWorkState(GnbProtocol.CellId.SECOND) != GnbBean.State.NONE &&
                            TraceUtil.build().getWorkState(GnbProtocol.CellId.SECOND) != GnbBean.State.STOP) {
                        int rsrp = -1;

                        if (TraceUtil.build().getRsrp(GnbProtocol.CellId.FOURTH) != -1) {
                            rsrp = TraceUtil.build().getRsrp(GnbProtocol.CellId.FOURTH);
                            countFourth = 0;
                        } else {
                            countFourth++;
                        }
                        if (countFourth < 6) {
                            if (rsrp != -1) refreshTraceValue(GnbProtocol.CellId.FOURTH, rsrp);
                        } else {
                            if (isSecondStart) {
                                if (countSecond != 0 && countSecond < 6)
                                    refreshTraceValue(GnbProtocol.CellId.FOURTH, rsrp);
                            } else {
                                refreshTraceValue(GnbProtocol.CellId.FOURTH, rsrp);
                            }
                        }

                        mHandler.sendEmptyMessageDelayed(7, TraceBean.RSRP_TIME_INTERVAL);
                    }
                    break;
                case 11:
                    if (arfcnListCell1.size()>0){
                        MessageController.build().setArfcnChange(GnbProtocol.CellId.FIRST,String.valueOf(arfcnListCell1.get(setArfcnChangeCell1)));
                    }
                    setArfcnChangeCell1++;
                    if (setArfcnChangeCell1 == arfcnListCell1.size()){
                        setArfcnChangeCell1 = 0;
                    }
                    if (TraceUtil.build().getArfcnSetChange(GnbProtocol.CellId.FIRST)){
                        mHandler.sendEmptyMessageDelayed(11, 60 * 1000);
                    }
                    break;
                case 12:
                    if (arfcnListCell2.size()>0){
                        MessageController.build().setArfcnChange(GnbProtocol.CellId.SECOND,String.valueOf(arfcnListCell1.get(setArfcnChangeCell2)));
                    }
                    setArfcnChangeCell2++;
                    if (setArfcnChangeCell2 == arfcnListCell2.size()){
                        setArfcnChangeCell2 = 0;
                    }
                    if (TraceUtil.build().getArfcnSetChange(GnbProtocol.CellId.SECOND)){
                        mHandler.sendEmptyMessageDelayed(12, 60 * 1000);
                    }

                    break;
            }
        }
    };

    private int lostCount_1 = 0;
    private int lostCount_2 = 0;
    private boolean isSayLost_1 = false;
    private boolean isSayLost_2 = false;
    /**
     * 报值
     *
     * @param cell_id 通道id
     * @param rsrp    数值
     */
    private void refreshTraceValue(int cell_id, int rsrp) {

        if (rsrp == -1 && TraceUtil.build().getLastRsrp(cell_id) == rsrp) {
            if (cell_id == GnbProtocol.CellId.FIRST || cell_id == GnbProtocol.CellId.THIRD) {
                lostCount_1++;
                if (lostCount_1 > 5) {
                    if (isSayLost_1) return;
                    TraceUtil.build().setLastRsrp(cell_id, rsrp);
                    TextTTS.build().play("定位已掉线", true);
                    OpLog.build().write(DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + cell_id+ "\t\t定位已掉线");
                    isSayLost_1 = true;
                }
            } else {
                lostCount_2++;
                if (lostCount_2 > 5) {
                    if (isSayLost_2) return;
                    TraceUtil.build().setLastRsrp(cell_id, rsrp);
                    //TextTTS.build().play("定位已掉线", true);
                    OpLog.build().write(DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + cell_id + "\t\t定位已掉线");
                    isSayLost_2 = true;
                }
            }
        } else {
            if (rsrp != 0 && rsrp != 3) {
                if (cell_id == GnbProtocol.CellId.FIRST || cell_id == GnbProtocol.CellId.THIRD) {
                    isSayLost_1 = false;
                    lostCount_1 = 0;
                } else {
                    isSayLost_2 = false;
                    lostCount_2 = 0;
                }
                TraceUtil.build().setLastRsrp(cell_id, rsrp);
                if (rsrp == -1) return;
                TextTTS.build().play("" + rsrp, false);
                if (!TraceUtil.build().isSaveOpLog(cell_id)) {
                    TraceUtil.build().setSaveOpLog(cell_id, true);
                    OpLog.build().write(DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t"
                            + cell_id + "\t\t定位已上号，当前报值：" + rsrp);
                }
            }
        }

        if (cell_id == GnbProtocol.CellId.SECOND || cell_id == GnbProtocol.CellId.FOURTH) {
            tv_2_value.setText(String.valueOf(rsrp));
            barChartUtil1.addEntry(0,rsrp);
        } else {
            tv_value.setText(String.valueOf(rsrp));
//            refreshTraceBarView(rsrp, 0, false);
            barChartUtil.addEntry(0,rsrp);
        }
    }

    @Override
    public void onQueryVersionRsp(GnbVersionRsp rsp) {
        if (rsp != null) {
            APPLog.I("onQueryVersionRsp(): " + rsp);
            if (rsp.getSwVer() != null) {
                StringBuilder sb = new StringBuilder();
                DeviceInfo.build().setSoftVer(rsp.getSwVer());
                DeviceInfo.build().setHwVer(rsp.getHwVer());
                DeviceInfo.build().setFpgaVer(rsp.getFpgaVer());
                gnbVersion = rsp.getSwVer();
                sb.append("硬件版本: " + rsp.getHwVer() + "\r\n");
                sb.append("逻辑版本: " + rsp.getFpgaVer() + "\r\n");
                sb.append("软件版本: " + rsp.getSwVer());
                /*if (showBsVersion) {
                    showBsVersion = false;
                    showRemindDialog(getStr(R.string.reminder), sb.toString());
                }*/
                String dev_name = DeviceInfo.build().getDevName();
                if (dev_name == null || dev_name.equals("")) {
                    MessageController.build().setOnlyCmd(GnbProtocol.OAM_MSG_GET_SYS_INFO);
                }
            } else {
                setWorkState(GnbBean.State.IDLE);
                refreshStateView(getStr(R.string.state_get_version_fail));
            }
        }
    }

    @Override
    public void onGetCatchCfg(GnbCatchCfgRsp gnbCatchCfgRsp) {

    }

    @Override
    public void onFirmwareUpgradeRsp(GnbCmdRsp rsp) {
        if (rsp != null) {
            APPLog.I("onFirmwareUpgradeRsp(): " + rsp);
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_VERSION_UPGRADE) {
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    rebootCnt = 0;
                    isFirstRec = true;
                    setWorkState(GnbBean.State.REBOOT);
                } else {
                    setWorkState(GnbBean.State.IDLE);
                }
            }
        }
    }

    @Override
    public void onSetGnbRsp(final GnbCmdRsp rsp) {
        if (rsp != null) {
            APPLog.I("onSetGnbRsp(): " + rsp);
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_CFG_gNB) { //UI_2_gNB_CFG_gNB = 10 配置频点参数
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    APPLog.I(" onSetGnbRsp "+TraceUtil.build().isTacChange(rsp.getCellId())+
                            TraceUtil.build().getWorkState(rsp.getCellId())+
                            MessageController.build().getTraceType() );
                    if (!TraceUtil.build().isTacChange(rsp.getCellId())
                            && TraceUtil.build().getWorkState(rsp.getCellId()) == GnbBean.State.GNBCFG) {
                        //3.设置功率衰减
                        if (MessageController.build().getTraceType() == GnbProtocol.TraceType.TRACE) {
                            MessageController.build().setTraceType(GnbProtocol.TraceType.STARTTRACE);
                            MessageController.build().setTxPwrOffset(rsp.getCellId(), Integer.parseInt(TraceUtil.build().getArfcn(rsp.getCellId())), 0);
                        }
                    }
                } else {
                    //空口同步失败后定位信息仍有显示频点、PCI、IMSI号
                    refreshStateView("配置频点失败");
                    String cell;
                    if(rsp.getCellId() == GnbProtocol.CellId.FIRST){
                       cell = "小区一";
                    }else {
                        cell = "小区二";
                    }
                    if (rsp.getRspValue() == GnbProtocol.OAM_ACK_E_ASYNC_FAIL){
                        showRemindDialog(getStr(R.string.reminder), cell+getStr(R.string.OAM_ACK_E_ASYNC_FAIL));
                    }else if (rsp.getRspValue() == GnbProtocol.OAM_ACK_E_GPS_UNLOCK){
                        showRemindDialog(getStr(R.string.reminder), cell+getStr(R.string.OAM_ACK_E_GPS_UNLOCK));
                    }else {
                        showErrorResult(rsp.getCellId(),rsp.getRspValue());
                    }
                    if (dualCell == GnbProtocol.DualCell.SINGLE){
                        if (rsp.getCellId() == GnbProtocol.CellId.FIRST) {
                            refreshStateView(getStr(R.string.state_wait_cfg));
                            workState = GnbBean.State.IDLE;
                            btn_trace.setText("定位");
                            PaCtl.build().closePA();
                        }
                    }else {
                        if (rsp.getCellId() ==GnbProtocol.CellId.FIRST ){
                            isCell0Failed = true;
                            if (TextUtils.isEmpty(TraceUtil.build().getArfcn(GnbProtocol.CellId.SECOND))){
                                isCell0Failed = false;
                                refreshStateView(getStr(R.string.state_wait_cfg));
                                workState = GnbBean.State.IDLE;
                                btn_trace.setText("定位");
                                PaCtl.build().closePA();
                            }
                        }else {
                            if (isCell0Failed||TextUtils.isEmpty(TraceUtil.build().getArfcn(GnbProtocol.CellId.FIRST))){
                                isCell0Failed = false;
                                refreshStateView(getStr(R.string.state_wait_cfg));
                                workState = GnbBean.State.IDLE;
                                btn_trace.setText("定位");
                                PaCtl.build().closePA();
                            }else {
                                refreshStateView(getStr(R.string.state_trace));
                            }

                        }

                    }
                    if (rsp.getRspValue() == 5) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setTraceState(rsp.getCellId(), GnbBean.State.STOP);
                                refreshStateView("结束定位中");
                                TraceUtil.build().setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                                MessageController.build().setCmdAndCellID(GnbProtocol.UI_2_gNB_STOP_TRACE, rsp.getCellId());
                            }
                        }, 500);
                    }
                    APPLog.D("onSetGnbRsp()" + TraceUtil.build().getWorkState(GnbProtocol.CellId.FIRST) + ", " + TraceUtil.build().getWorkState(GnbProtocol.CellId.SECOND));


                    if (rsp.getCellId() == GnbProtocol.CellId.FIRST) {       //通道一定位失败，开始通道二的定位流程
                        if (dualCell == GnbProtocol.DualCell.DUAL && !TextUtils.isEmpty(TraceUtil.build().getArfcn(GnbProtocol.CellId.SECOND))) {
                            if (TraceUtil.build().getSwap_rf(rsp.getCellId())==1){
                                if (rsp.getCellId() == GnbProtocol.CellId.FIRST){
                                    PaCtl.build().initPA(TraceUtil.build().getArfcn(GnbProtocol.CellId.SECOND),GnbProtocol.CellId.SECOND);
                                }else {
                                    PaCtl.build().initPA(TraceUtil.build().getArfcn(GnbProtocol.CellId.FIRST),GnbProtocol.CellId.FIRST);
                                }
                            }else {
                                PaCtl.build().initPA(TraceUtil.build().getArfcn(rsp.getCellId()),rsp.getCellId());
                            }
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            MessageController.build().setBlackList(GnbProtocol.CellId.SECOND,false, blackList.size(), blackList);
                        }
                    }
                    TraceUtil.build().setImsi(rsp.getCellId(), "");
                    TraceUtil.build().setArfcn(rsp.getCellId(), "");
                    TraceUtil.build().setPci(rsp.getCellId(), "");
                    TraceUtil.build().setTacChange(rsp.getCellId(), false);
                    TraceUtil.build().setEnable(rsp.getCellId(), false);
                    refreshTraceBarView(0,0,true);
                    TraceUtil.build().setRsrp(rsp.getCellId(), 0);
                    TraceUtil.build().setLastRsrp(rsp.getCellId(), -1);
                    TraceUtil.build().setWorkState(rsp.getCellId(),GnbBean.State.NONE) ;
                    setTraceState(rsp.getCellId(), GnbBean.State.NONE);
                }
            }
        }
    }

    @Override
    public void onStartControlRsp(GnbTraceRsp gnbTraceRsp) {

    }

    @Override
    public void onStartCatchRsp(GnbTraceRsp gnbTraceRsp) {

    }

    @Override
    public void onStopTraceRsp(GnbCmdRsp rsp) {
        if (rsp != null) {
            APPLog.I("onStopTraceRsp(): " + rsp);
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_STOP_TRACE) {
                TraceUtil.build().setArfcnSetChange(rsp.getCellId(),false);
                setArfcnChangeCell1 = 0;
                setArfcnChangeCell2 = 0;
                noTraceFlag = true;
                setTraceState(rsp.getCellId(), GnbBean.State.NONE);
                if (TraceUtil.build().getWorkState(GnbProtocol.CellId.FIRST) == GnbBean.State.NONE
                        && TraceUtil.build().getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.NONE) {
                    setWorkState(GnbBean.State.IDLE);
                    btn_trace.setText("定位");
                    refreshStateView(getStr(R.string.state_wait_cfg));
                }
                tv_value.setText("");
                tv_2_value.setText("");
                tv_value.setBackgroundResource(R.mipmap.home_bg_no_value);
                tv_2_value.setBackgroundResource(R.mipmap.home_bg_no_value);
//                btn_trace.setBackground(getResources().getDrawable(R.mipmap.ic_stop));
                OpLog.build().write(DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + rsp.getCellId() + "\t\t结束定位");
                TraceUtil.build().setImsi(rsp.getCellId(), "");
                TraceUtil.build().setArfcn(rsp.getCellId(), "");
                TraceUtil.build().setPci(rsp.getCellId(), "");
                TraceUtil.build().setTacChange(rsp.getCellId(), false);
                TraceUtil.build().setEnable(rsp.getCellId(), false);
                refreshTraceBarView(0,0,true);
                TraceUtil.build().setRsrp(rsp.getCellId(), 0);
                TraceUtil.build().setLastRsrp(rsp.getCellId(), -1);
            }
        }

    }

    @Override
    public void onStopControlRsp(GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onStopCatchRsp(GnbCmdRsp gnbStateRsp) {

    }

    @Override
    public void onSetPaGpioRsp(GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onGetPaGpioRsp(GnbGpioRsp gnbGpioRsp) {

    }

    @Override
    public void onSetBlackListRsp(final GnbCmdRsp rsp) {
        if (rsp != null) {
            APPLog.I("onSetBlackListRsp(): " + rsp);
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_SET_BLACK_UE_LIST) {
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    // 发配置定位参数指令
                    //setTraceState(rsp.getCellId(), GnbBean.State.GNBCFG, getStr(R.string.state_set_arfcn));
                    setTraceState(rsp.getCellId(), GnbBean.State.GNBCFG);
                    refreshStateView(getStr(R.string.state_set_arfcn));
                    int traceTac = PrefUtil.build().getTac();
                    int maxTac = traceTac + GnbProtocol.MAX_TAC_NUM;
                    final String plmn = TraceUtil.build().getPlmn(rsp.getCellId());
                    final String arfcn = TraceUtil.build().getArfcn(rsp.getCellId());
                    final String pci = TraceUtil.build().getPci(rsp.getCellId());
                    String ue_max_pwr = TraceUtil.build().getUeMaxTxpwr(rsp.getCellId());
                    int air_sync = TraceUtil.build().getAirSync(rsp.getCellId());
                    final int ssbBitmap = TraceUtil.build().getSsbBitmap(rsp.getCellId());
                    final int bandwidth = TraceUtil.build().getBandWidth(rsp.getCellId());
                    final int cfr = TraceUtil.build().getCfr(rsp.getCellId());
                    final long cid = TraceUtil.build().getCid(rsp.getCellId());
                    TraceUtil.build().setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                    int swap_rf = TraceUtil.build().getSwap_rf(rsp.getCellId());
                    int mob_reject_code = TraceUtil.build().getMobRejectCode(rsp.getCellId());
                    String split_arfcn_dl = TraceUtil.build().getSplit_arfcn_dl(rsp.getCellId());
                    /*initGnbTrace(int cell_id, int startTac, int maxTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                    	int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                      int band_width, int cfr_enable, int swap_rf)
                    定位配置频点参数
                    mod_reject_code = 0(正常)、9（强上号）
                    split_arfcn_dl   载波分裂频点*/
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {

//                        }
//                    }, 500);
                    TraceUtil.build().setSaveOpLog(rsp.getCellId(), false);
                    String msg = "plmn = " + TraceUtil.build().getPlmn(rsp.getCellId()) + ", arfcn = " + TraceUtil.build().getArfcn(rsp.getCellId()) +
                            ", pci = " + TraceUtil.build().getPci(rsp.getCellId()) + ", cid = " + TraceUtil.build().getCid(rsp.getCellId())
                            + ", time offfset = " + GnbCity.build().getTimimgOffset();
                    OpLog.build().write(DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t小区" + rsp.getCellId() + "\t\t定位参数：" + msg);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isGps_sync){
                        air_sync = 0;
                    }else {
                        air_sync = 1;
                    }
                    MessageController.build().initGnbTrace(rsp.getCellId(), traceTac, maxTac, plmn, arfcn, pci, ue_max_pwr,
                            GnbCity.build().getTimimgOffset(), 0, air_sync, "0", 9,
                            cid, ssbBitmap, bandwidth, cfr, swap_rf, 15, -70, mob_reject_code, split_arfcn_dl);
                } else {
                    //setTraceState(rsp.getCellId(), GnbBean.State.NONE, getStr(R.string.state_set_black_list_fail));
                    showErrorResult(rsp.getCellId(),rsp.getRspValue());
                    setTraceState(rsp.getCellId(), GnbBean.State.NONE);
                    workState = GnbBean.State.IDLE;
                    btn_trace.setText("定位");
                    refreshStateView(getStr(R.string.state_set_black_list_error));
                }
            }

        }
    }

    @Override
    public void onSetTxPwrOffsetRsp(final GnbCmdRsp rsp) {
        if (rsp != null) {
            APPLog.I("onSetTxPwrOffsetRsp(): " + rsp + "  TraceType = " + MessageController.build().getTraceType());
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_SET_TX_POWER_OFFSET) {
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {

                    if (MessageController.build().getTraceType() == GnbProtocol.TraceType.STARTTRACE) {
                        //4、开pa
                        PaCtl.build().openPA(TraceUtil.build().getArfcn(rsp.getCellId()),PA_Type); // 开PA
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 5、发开始定位指令
                                TraceUtil.build().setRsrp(rsp.getCellId(), 0);
                                TraceUtil.build().setTacChange(rsp.getCellId(), true);
                                TraceUtil.build().setEnable(rsp.getCellId(), true);
                                TraceUtil.build().setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                                if (MessageController.build().getTraceType() == GnbProtocol.TraceType.CONTROL) {
                                    MessageController.build().setCmdAndCellID(GnbProtocol.UI_2_gNB_START_CONTROL, rsp.getCellId());
                                    setTraceState(rsp.getCellId(), GnbBean.State.CFG_TRACE);
                                    refreshStateView("管控中");
                                } else if (MessageController.build().getTraceType() == GnbProtocol.TraceType.TRACE) {
                                    MessageController.build().startTrace(rsp.getCellId(), 1, TraceUtil.build().getImsi(rsp.getCellId()),0);
                                    setTraceState(rsp.getCellId(), GnbBean.State.CFG_TRACE);
                                    refreshStateView(getStr(R.string.state_set_imsi));
                                }
                            }
                        }, 300);
                    }
                }
                MessageController.build().setTraceType(GnbProtocol.TraceType.TRACE);
            }
        }
    }

    @Override
    public void onSetNvTxPwrOffsetRsp(GnbCmdRsp gnbStateRsp) {

    }

    @Override
    public void onSetWifiInfoRsp(GnbCmdRsp gnbStateRsp) {

    }

    @Override
    public void onSetBtNameRsp(GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetMethIpRsp(GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onGetMethIpRsp(GnbMethIpRsp gnbMethIpRsp) {

    }

    @Override
    public void onSetFtpRsp(GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onGetFtpRsp(GnbFtpRsp gnbFtpRsp) {

    }

    /*@Override
    public void onSetModeRsp(GnbCmdRsp gnbStateRsp) {
        if (gnbStateRsp != null) {
            APPLog.I("onSetModeRsp(): " + gnbStateRsp.toString());
            *//*if (gnbStateRsp.getRspType() == GnbStateRsp.STATE_CMD
                    && gnbStateRsp.getGnbCmdType() == GnbProtocol.UI_2_gNB_SET_MODE) {
                if (gnbStateRsp.getGnbRspValue() == GnbProtocol.gNB_2_UI_CFG_OK) {
                    if (workMode == Enum.WorkMode.VEHICLE) {
                        MessageController.build().setTxPwrOffset(0);
                    } else {
                        if (txPwrOffset == -1) {
                            txPwrOffset = 0;
                        }
                        MessageController.build().setTxPwrOffset(txPwrOffset);
                    }
                }
            } else if  (gnbStateRsp.getRspType() == GnbStateRsp.STATE_HEART) {
                onHeartStateRsp(gnbStateRsp);
            }*//*
        }
    }*/

    @Override
    public void onSetTimeRsp(GnbCmdRsp rsp) {
        if (rsp != null) {
            APPLog.I("onSetTimeRsp(): " + rsp);
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_SET_TIME) {
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    setWorkState(GnbBean.State.IDLE);
                    refreshStateView(getStr(R.string.state_wait_cfg));
                } else {
                    setWorkState(GnbBean.State.IDLE);
                    refreshStateView(getStr(R.string.state_set_time_fail));
                }
                OpLog.build().write(DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + "admin 123456");
            }
        }
    }

    @Override
    public void onSetRebootRsp(GnbCmdRsp rsp) {
        if (rsp != null) {
            APPLog.I("onSetRebootRsp(): " + rsp);
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_REBOOT_gNB) {
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    rebootCnt = 0;
                    isStart = true;
                    setWorkState(GnbBean.State.REBOOT);
                    refreshStateView(getStr(R.string.state_rebooting));
                } else {
                    setWorkState(GnbBean.State.REBOOT);
                    refreshStateView(getStr(R.string.state_set_reboot_fail));
                }
            }
        }
    }

    /*@Override
    public void onSetRebootRsp(GnbStateRsp gnbStateRsp) {
        //MessageController
        if (gnbStateRsp != null) {
            APPLog.I("onSetRebootRsp(): " + gnbStateRsp.toString());
            if (gnbStateRsp.getRspType() == GnbStateRsp.STATE_CMD
                    && gnbStateRsp.getGnbCmdType() == GnbProtocol.UI_2_gNB_REBOOT_gNB) {
                if (gnbStateRsp.getGnbRspValue() == GnbProtocol.gNB_2_UI_CFG_OK) {
                    setWorkState(Enum.State.REBOOT);
                    refreshStateView(getStr(R.string.state_rebooting));
                    rebootCnt = 0;
                    MessageController.build().setMsgType(GnbProtocol.UI_NONE);
                } else {

                }
            } else if  (gnbStateRsp.getRspType() == GnbStateRsp.STATE_HEART) {
                onHeartStateRsp(gnbStateRsp);
            }
        }
    }*/

    /*@Override
    public void onUpdateVersionRsp(GnbStateRsp gnbStateRsp) {
        if (gnbStateRsp != null) {
            APPLog.I("onVersionUpgradeRsp(): " + gnbStateRsp);
            if (gnbStateRsp.getRspType() == GnbStateRsp.STATE_CMD
                    && gnbStateRsp.getGnbCmdType() == GnbProtocol.UI_2_gNB_VERSION_UPGRADE) {
                switch (gnbStateRsp.getGnbRspValue()) {
                    case GnbProtocol.gNB_2_UI_CFG_OK:
                    case GnbProtocol.upState.DOWNLOADING:
                        refreshStateView(getStr(R.string.state_upgrade_downloading));
                        break;
                    case GnbProtocol.upState.UPGRADING:
                        refreshStateView(getStr(R.string.state_upgrade_ing));
                        break;
                    case GnbProtocol.upState.RESTART:
                        refreshStateView(getStr(R.string.state_upgrade_restart));
                        break;
                    case GnbProtocol.upState.DOWNLOAD_FAILED:
                        setWorkState(Enum.State.IDLE);
                        refreshStateView(getStr(R.string.state_upgrade_download_fail));
                        break;
                    case GnbProtocol.upState.UPGRADE_NG:
                    case GnbProtocol.upState.UPGRADING_FAILED:
                        setWorkState(Enum.State.IDLE);
                        refreshStateView(getStr(R.string.state_upgrade_fail));
                        break;
                    case GnbProtocol.upState.UPGRADE_OK:
                        refreshStateView(getStr(R.string.state_upgrade_succ));
                        setWorkState(Enum.State.REBOOT);
                        refreshStateView(getStr(R.string.state_reboot_cfg));
                        MessageController.build().setOnlyTypeCmd(GnbProtocol.UI_2_gNB_REBOOT_gNB);
                        break;
                }
            }
        }
    }*/

    @Override
    public void onGetLogRsp(GnbCmdRsp rsp) {
        if (rsp != null) {
            APPLog.I("onGetLogRsp(): " + rsp);
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_GET_LOG_REQ) {
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    if (report_level != 1) {
                        setWorkState(GnbBean.State.GETLOG);
                        refreshStateView(getStr(R.string.state_get_log));
                        FTPUtil.build().startGetFile(FileProtocol.FILE_BS_LOG, gnbLogName);
                    } else {
                        FTPUtil.build().startGetFile(FileProtocol.FILE_Scan_Freq, scanFreqName);
                    }
                } else {
                    setWorkState(GnbBean.State.IDLE);
                    refreshStateView(getStr(R.string.get_log_fail));
                }
            }
        }
    }

    @Override
    public void onGetOpLogRsp(GnbCmdRsp rsp) {
        if (rsp != null) {
            APPLog.I("onGetOpLogRsp(): " + rsp);
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_GET_OP_LOG_REQ) {
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    setWorkState(GnbBean.State.GETLOG);
                    refreshStateView(getStr(R.string.state_get_op_log));
                    FTPUtil.build().startGetFile(FileProtocol.FILE_OP_LOG, gnbLogName);
                } else {
                    setWorkState(GnbBean.State.IDLE);
                    refreshStateView(getStr(R.string.state_get_oplog_fail));
                }
            }
        }
    }

    @Override
    public void onWriteOpLogRsp(GnbCmdRsp rsp) {
        if (rsp != null) {
            // 由于大多是在定位过程中写数据，故这里不走回调流程
            APPLog.I("onWriteOpLogRsp(): " + rsp);
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_WRITE_OP_RECORD) {
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {

                }
            }
        }
    }

    @Override
    public void onDeleteOpLogRsp(GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetSysInfoRsp(GnbCmdRsp rsp) {
        if (rsp != null) {
            APPLog.I("onSetSysInfoRsp(): " + rsp);
            if (rsp.getCmdType() == GnbProtocol.OAM_MSG_SET_SYS_INFO) {

                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    setWorkState(GnbBean.State.IDLE);
                    refreshStateView(getStr(R.string.state_wait_cfg));
                    Util.showToast(getApplicationContext(), "设备名称配置成功");
                } else {
                    setWorkState(GnbBean.State.IDLE);
                    refreshStateView(getStr(R.string.state_set_dev_name_fail));
                    showToast(getStr(R.string.state_set_dev_name_fail));
                }
            }
        }
    }

    @Override
    public void onGetSysInfoRsp(GnbGetSysInfoRsp rsp) {
        if (rsp != null) {
            APPLog.I("onGetSysInfoRsp(): " + rsp);
            if (rsp.getDevName() != null) {
                StringBuilder sb = new StringBuilder();
                DeviceInfo.build().setDevName(rsp.getDevName());
                DeviceInfo.build().setLicense(rsp.getLicense());
                sb.append("设备名称: " + rsp.getDevName() + "\r\n");
                sb.append("设备密钥: " + rsp.getLicense() + "\r\n");
                //showRemindDialog(getStr(R.string.reminder), sb.toString());
                setWorkState(GnbBean.State.IDLE);
                refreshStateView(getStr(R.string.state_wait_cfg));
            } else {
                setWorkState(GnbBean.State.IDLE);
                refreshStateView(getStr(R.string.state_get_dev_name_fail));
            }
        }
    }

    @Override
    public void onSetDualCellRsp(GnbCmdRsp rsp) {
        if (rsp != null) {
            APPLog.I("onSetDualCellRsp(): " + rsp.toString());
            if (rsp.getCmdType() == GnbProtocol.OAM_MSG_SET_DUAL_CELL) {
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    setWorkState(GnbBean.State.IDLE);
                    refreshStateView(getStr(R.string.state_wait_cfg));
                    showRemindDialog(getStr(R.string.reminder), getStr(R.string.state_set_dual_cell_succ));
                    isSetModule = true;
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    mSlideLeftFrag.setDualCell(dualCell);
                    setTraceState(GnbProtocol.CellId.FIRST, GnbBean.State.NONE);
                    setTraceState(GnbProtocol.CellId.SECOND, GnbBean.State.NONE);
                    PrefUtil.build().setCell(dualCell);
                } else {
                    setWorkState(GnbBean.State.IDLE);
                    refreshStateView(getStr(R.string.state_set_dual_cell_fail));
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    dualCell = PrefUtil.build().getCell();
                }
            }
        }
    }

    @Override
    public void onSetRxGainRsp(GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetGpsRsp(GnbCmdRsp rsp) {
        if (rsp != null) {
            APPLog.I("onSetGpsRsp(): " + rsp.toString());
            if (rsp.getCmdType() == GnbProtocol.OAM_MSG_SET_GPS_CFG) {
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    showRemindDialog(getStr(R.string.reminder), getStr(R.string.state_set_gps_succ));
                } else {
                    showRemindDialog(getStr(R.string.reminder), getStr(R.string.state_set_gps_fail));
                }
            }
        }
    }

    @Override
    public void onGetSysLogRsp(GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetFanSpeedRsp(GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetFanAutoSpeedRsp(GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetJamArfcn(GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onFreqScanRsp(GnbFreqScanRsp rsp) {

        if (rsp != null) {
            APPLog.I("onFreqScanRsp() " + rsp);
            APPLog.I("onFreqScanRsp() isStopScan " + isStopScan + ", rsp.getReportStep() = " + rsp.getReportStep() + ", rsp.getScanResult() = " + rsp.getScanResult());
            if (!isStopScan) {
                if (rsp.getReportStep() == 2) {
                    scanCount++;
                    freqScan();
                }
            } else {
                if (rsp.getReportStep() == 2) {
                    workState = GnbBean.State.IDLE;
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        arfcnAdapter.setScan(false);
                        btn_arfcn.setText("扫频");
                        PaCtl.build().closePA();
                    }
                }
            }
            if (rsp.getScanResult() == GnbProtocol.OAM_ACK_OK && rsp.getReportStep() == 1) {
                if (scanArfcnBeanList.size() == 0) {
                    scanArfcnBeanList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
                            rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
                            rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
                } else {
                    boolean isAdd = true;
                    for (int i = 0; i < scanArfcnBeanList.size(); i++) {
                        if (scanArfcnBeanList.get(i).getUl_arfcn() == rsp.getUl_arfcn() &&
                                scanArfcnBeanList.get(i).getPci() == rsp.getPci()) {
                            isAdd = false;
                            scanArfcnBeanList.remove(scanArfcnBeanList.get(i));
                            scanArfcnBeanList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
                                    rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
                                    rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
                            break;
                        }
                    }
                    if (isAdd) {
                        scanArfcnBeanList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
                                rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
                                rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
                    }
                }
//                setListViewHeightBasedOnChildren(arfcnListView);
                arfcnAdapter.setArfcnList(scanArfcnBeanList);
            }

        }
    }
    private int getEnableSize() {
        int n = 0;
        for (int i = 0; i < enablelList.size(); i++) {
            if (enablelList.get(i)) n++;
        }
        return n;
    }
    private void freqScan() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < enablelList.size(); i++) {
            if (enablelList.get(i)) {
               list.add(i);
            }
        }
        if (scanCount == getEnableSize()) scanCount = 0;
        if (list.get(scanCount) == 0)  startFreqScan("N1", report_level, async_enable);
        else if (list.get(scanCount) == 1) startFreqScan("N28", report_level, async_enable);
        else if (list.get(scanCount) == 2) startFreqScan("N41", report_level, async_enable);
        else if (list.get(scanCount) == 3) startFreqScan("N78", report_level, async_enable);
        else if (list.get(scanCount) == 4) startFreqScan("N79", report_level, async_enable);


    }
    private void startFreqScan(String type, final int report_level, final int async_enable) {
        try {
            arfcnList_N1.clear();
            arfcnList_N28.clear();
            arfcnList_N41.clear();
            arfcnList_N78.clear();
            arfcnList_N79.clear();
            arfcnList_N1.addAll(Util.json2Int(PrefUtil.build().getArfcn("N1"),"N1" ));
            arfcnList_N28.addAll(Util.json2Int(PrefUtil.build().getArfcn("N28"),"N28" ));
            arfcnList_N41.addAll(Util.json2Int(PrefUtil.build().getArfcn("N41"),"N41" ));
            arfcnList_N78.addAll(Util.json2Int(PrefUtil.build().getArfcn("N78"),"N78" ));
            arfcnList_N79.addAll(Util.json2Int(PrefUtil.build().getArfcn("N79"),"N79" ));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        APPLog.D("startFreqScan type = " + type);
        List<Integer> list = null;
        int offset = 0;
        int chan = 1;
        switch (type){
            case "N1":
                if (arfcnList_N1.size() == 0) {
                    showToast("N1频点列表为空，停止该频点扫频");
                    return;
                }
                if (PA_Type == 0){
                    PaBean.build().setPaGpio(1, 0, 0, 2, 1, 0, 1, 0);
                }else if(PA_Type == 1){
                    PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2);//G73c
                }else if (PA_Type == 2){
                    PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2);//C70
                }
                list = arfcnList_N1;
                chan = 1;
                break;
            case "N28":
                if (arfcnList_N28.size() == 0) {
                    showToast("N28频点列表为空，停止该频点扫频");
                    return;
                }
                if (PA_Type == 0){
                    PaBean.build().setPaGpio(0, 1, 1, 0, 0, 2, 0, 2);
                }else if(PA_Type == 1){
                    PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2);//G73c
                }else if (PA_Type == 2){
                    PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2);//C70
                }
                list = arfcnList_N28;
                offset = GnbCity.build().getTimimgOffset();
                chan = 2;
                break;
            case "N41":
                if (arfcnList_N41.size() == 0) {
                    showToast("N41频点列表为空，停止该频点扫频");
                    return;
                }
                if (PA_Type == 0){
                    PaBean.build().setPaGpio(1, 0, 0, 2, 2, 0, 0, 0);
                }else if(PA_Type == 1){
                    PaBean.build().setPaGpio(1, 0, 0, 2, 2, 0, 1, 0); //G73c
                }else if (PA_Type == 2){
                    PaBean.build().setPaGpio(1, 2, 2, 2, 2, 2, 2, 2); //C70
//                    PaBean.build().setPaGpio(1, 0, 0, 2, 2, 0, 1, 0); //C70
//                    PaBean.build().setPaGpio(1, 0, 0, 3, 2, 2, 1, 2); //C70
                }

                list = arfcnList_N41;
                offset = GnbCity.build().getTimimgOffset();
                chan = 1;
                break;
            case "N78":
                if (arfcnList_N78.size() == 0) {
                    showToast("N78频点列表为空，停止该频点扫频");
                    return;
                }
                if (PA_Type == 0){
                    PaBean.build().setPaGpio(0, 1, 2, 0, 0, 2, 0, 2);
                }else if(PA_Type == 1){
                    PaBean.build().setPaGpio(0, 1, 2, 0, 0, 2, 0, 2); //G73c
                }else if (PA_Type == 2){
                    PaBean.build().setPaGpio(2, 1, 2, 2, 2, 2, 2, 2); //C70
//                    PaBean.build().setPaGpio(0, 1, 2, 0, 0, 2, 0, 2); //C70
//                    PaBean.build().setPaGpio(0, 1, 0, 0, 0, 2, 0, 2); //C70
                }
                chan = 2;
                list = arfcnList_N78;
                break;
            case "N79":
                if (arfcnList_N79.size() == 0) {
                    showToast("N79频点列表为空，停止该频点扫频");
                    return;
                }
                if (PA_Type == 0){
                    PaBean.build().setPaGpio(0, 1, 2, 0, 0, 1, 0, 3);
                }else if(PA_Type == 1){
                    PaBean.build().setPaGpio(0, 1, 2, 0, 0, 1, 0, 2); //G73c
                }else if (PA_Type == 2){
//                    PaBean.build().setPaGpio(0, 1, 2, 0, 0, 1, 0, 2); //C70
                    PaBean.build().setPaGpio(2, 2, 2, 2, 2, 1, 2, 2); //C70
//                    PaBean.build().setPaGpio(2, 1, 2, 2, 2, 1, 3, 2); //C70
                }
                chan = 2;
                list = arfcnList_N79;
                offset = GnbCity.build().getTimimgOffset();
                break;
        }
        APPLog.I("openPA: " + PaBean.build().toString());
        MessageController.build().setGnbPaGpio();

        if (list == null) return;

        final List<Integer> arfcn_list = new ArrayList<>();
        final List<Integer> time_offset = new ArrayList<>();
        final List<Integer> chan_id = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            arfcn_list.add(list.get(i));
            time_offset.add(offset);
            chan_id.add(chan);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setWorkState(GnbBean.State.FREQ_SCAN);
                refreshStateView(getStr(R.string.state_scan_arfcn));
                MessageController.build().startFreqScan(report_level, async_enable, arfcn_list.size(), chan_id, arfcn_list, time_offset);
            }
        }, 500);
    }
    @Override
    public void onFreqScanGetDocumentRsp(GnbFreqScanGetDocumentRsp rsp) {
        if (rsp != null) {
            APPLog.I("onFreqScanGetDocumentRsp() " + rsp);
            if (isStopScan) {
                if (FreqScanCount == 0) {
                    startFreqScan("N28", report_level, async_enable);
                    FreqScanCount++;
                } else if (FreqScanCount == 1) {
                    startFreqScan("N41", report_level, async_enable);
                    FreqScanCount++;
                } else if (FreqScanCount == 2) {
                    startFreqScan("N78", report_level, async_enable);
                    FreqScanCount++;
                } else if (FreqScanCount == 3) {
                    startFreqScan("N79", report_level, async_enable);
                    FreqScanCount++;
                } else if (FreqScanCount == 4) {
                    startFreqScan("N1", report_level, async_enable);
                    FreqScanCount = 0;
                }
            }
            if (rsp.getScanResult() == GnbProtocol.OAM_ACK_OK && rsp.getReportLevel() == 1) {
                scanFreqName = rsp.getFileName().replace(".zip", "");
                APPLog.I("scanFreqName = " + scanFreqName);
                MessageController.build().getLog(3, rsp.getFileName().replace(".zip", ""));
            }
        }
    }

    @Override
    public void onStopFreqScanRsp(GnbCmdRsp rsp) {
        if (rsp != null) {
            APPLog.I("onStopFreqScanRsp rsp = " + rsp);
            APPLog.I("扫频结束");
            isStopScan = true;
            btn_arfcn.setText("结束");
            arfcnAdapter.setScan(false);
            scanCount = 0;
        }
    }

    @Override
    public void onStartTdMeasure(GnbCmdRsp rsp) {
        if (rsp != null) { // GPS 帧偏测量
            APPLog.I("startTdMeasure(): " + rsp);
            if (rsp.getCmdType() == GnbProtocol.OAM_MSG_START_TD_MEASURE) {
                if (gpsOffsetTestDialog != null && gpsOffsetTestDialog.isShowing()) {
                    gpsOffsetTestDialog.refreshView(rsp.getRspValue());
                }
            }
        }
    }

    @Override
    public void onSetGpsInOut(GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onGetGpsInOut(GnbGpsInOutRsp gnbGpsInOutRsp) {

    }

    @Override
    public void onGetGpsRsp(GnbGpsRsp gnbGpsRsp) {

    }

    @Override
    public void onSetForwardUdpMsg(GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onStartBandScan(GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetUserData(GnbUserDataRsp gnbUserDataRsp) {

    }

    @Override
    public void onGetUserData(GnbUserDataRsp gnbUserDataRsp) {

    }

    @Override
    public void onSetGpioTxRx(GnbCmdRsp gnbCmdRsp) {

    }

    @Override
    public void onSetDualStackRsp(GnbCmdRsp gnbCmdRsp) {

    }
   /* @Override
    public void OnScpGetLogRsp(boolean state) {
        APPLog.I("OnScpGetLogRsp(): " + state);
        if (state) {
            showRemindDialog(getStr(R.string.bs_log), getStr(R.string.get_log_success));
        } else {
            showRemindDialog(getStr(R.string.bs_log), getStr(R.string.get_log_fail));
        }
        setWorkState(Enum.State.IDLE);
        MessageController.build().setMsgType(GnbProtocol.UI_NONE);
    }*/

    /*@Override
    public void OnScpUpgradeFileRsp(boolean state) {
        APPLog.I("OnScpUpgradeFileRsp(): " + state);
        if (state) {
            setWorkState(Enum.State.UPDATE);
            MessageController.build().setGnbUpgrade(3, upgradeFileName, upgradeFilePath);
        } else {
            setWorkState(Enum.State.IDLE);
            MessageController.build().setMsgType(GnbProtocol.UI_NONE);
            showRemindDialog(getStr(R.string.bs_upgrade), getStr(R.string.state_upgrade_copying_file_fail));
        }
    }*/
    private void getArfcnList(int cell, String arfcn) {
        String band_cell1 = "";
        String band_cell2 = "";

        String arfcn_cell1 = "";
        String arfcn_cell2 = "";
        if (cell == 0) {
            arfcn_cell1 = arfcn;
            if (arfcn_cell1.length() > 5) {
                band_cell1 = "N" + NrBand.earfcn2band(Integer.parseInt(arfcn_cell1));
            } else {
                band_cell1 = "B" + LteBand.earfcn2band(Integer.parseInt(arfcn_cell1));
            }
            arfcnListCell1.clear();
            APPLog.I("arfcnListCell1 band = "+band_cell1);
            try {
                if (!TextUtils.isEmpty(band_cell1)) {
                    arfcnListCell1.addAll(Util.json2Int(PrefUtil.build().getArfcn(band_cell1 + "cfg"), band_cell1));
                    APPLog.I("arfcnListCell1 string = "+PrefUtil.build().getArfcn(band_cell1 + "cfg"));
                }
            } catch (JSONException e) {
                APPLog.E("readArfcnData JSONException e = " + e);
            }
        } else if (cell == 1) {
            arfcn_cell2 = arfcn;
            if (arfcn_cell2.length() > 5) {
                band_cell2 = "N" + NrBand.earfcn2band(Integer.parseInt(arfcn_cell2));
            } else {
                band_cell2 = "B" + LteBand.earfcn2band(Integer.parseInt(arfcn_cell2));
            }
            arfcnListCell2.clear();
            try {
                if (!TextUtils.isEmpty(band_cell2)) {
                    arfcnListCell2.addAll(Util.json2Int(PrefUtil.build().getArfcn(band_cell2 + "cfg"), band_cell2));
                }
            } catch (JSONException e) {
                APPLog.E("readArfcnData JSONException e = " + e);
            }
        }
    }

    private void showErrorResult(int cell_id,int ACK){
        String cell = "小区一";
        if (cell_id == GnbProtocol.CellId.FIRST){
            cell = "小区一";
        }else {
            cell = "小区二";
        }
        if (ACK == GnbProtocol.OAM_ACK_E_PARAM){
            showRemindDialog(getStr(R.string.reminder), cell+getStr(R.string.OAM_ACK_E_PARAM));
        }else if (ACK == GnbProtocol.OAM_ACK_E_BUSY){
            showRemindDialog(getStr(R.string.reminder), cell+getStr(R.string.OAM_ACK_E_BUSY));
        }else if (ACK == GnbProtocol.OAM_ACK_E_TRANSFER){
            showRemindDialog(getStr(R.string.reminder), cell+getStr(R.string.OAM_ACK_E_TRANSFER));
        }else if (ACK == GnbProtocol.OAM_ACK_E_SYS_STATE){
            showRemindDialog(getStr(R.string.reminder), cell+getStr(R.string.OAM_ACK_E_SYS_STATE));
        }else if (ACK == GnbProtocol.OAM_ACK_E_HW_CFG_FAIL){
            showRemindDialog(getStr(R.string.reminder), cell+getStr(R.string.OAM_ACK_E_HW_CFG_FAIL));
        }else if (ACK == GnbProtocol.OAM_ACK_ERROR){
            showRemindDialog(getStr(R.string.reminder), cell+getStr(R.string.OAM_ACK_ERROR));
        }
    }
    /**
     * 仅显示提示信息
     *
     * @param title
     * @param msg
     */
    private void showRemindDialog(String title, String msg) {
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_reminder, null);
        TextView tv_title = (TextView) view.findViewById(R.id.title_reminder);
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_reminder_msg);

        tv_title.setText(title);
        tv_msg.setText(msg);

        Button btn_cancel = (Button) view.findViewById(R.id.btn_reminder_know);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });

        showCustomDialog(view, false);
    }
    /**
     * 显示DIALOG通用接口
     */
    private void createCustomDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }
        mDialog = new Dialog(this, R.style.style_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mDialog.setCancelable(true);   // 返回键不消失
    }
    private void closeCustomDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
    private void showCustomDialog(View view, boolean bottom) {
        mDialog.setContentView(view);
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
            lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 6 / 7;//WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
        mDialog.show();
    }
    /**
     * 退出APP弹框
     */
    private void exitAppDialog(){
        createCustomDialog();

        View view = mInflater.inflate(R.layout.dialog_confirm, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        tv_title.setText(getStr(R.string.warning));
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        tv_msg.setText(getStr(R.string.exit_app_confirm));
        final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (handler != null) {
                    handler.removeCallbacksAndMessages(null);
                    handler = null;
                }

                finish();
            }
        });

        final Button btn_canel = (Button) view.findViewById(R.id.btn_cancel);
        btn_canel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    public String getStr(int strId) {
        return getResources().getString(strId);
    }

    private void refreshStateView(String state) {
        if (tv_state != null) {
            tv_state.setText(state);
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock(boolean lock) {
        APPLog.I("acquireWakeLock():" + lock);
        if (lock) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "keep_screen_on_tag");
            if (null != mWakeLock) {
                mWakeLock.acquire();
            }
        } else {
            if (null != mWakeLock) {
                mWakeLock.release();
                mWakeLock = null;
            }
        }
    }

    public boolean onKeyDown( int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitAppDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    /*
     * bindService和startService都是启动Service，有什么地方不一样呢：
     *
     * 1. startService Service中使用StartService（）方法来进行方法的调用，调用者和服务之间没有联系，
     * 即使调用者退出了，服务依然在进行
     * 【onCreate()->onStartCommand()->startService()->onDestroy()】，
     * 注意其中没有onStart()，主要是被onStartCommand()方法给取代了，onStart方法不推荐使用了。
     * ######################################################################
     * 2. bindService中使用bindService()方法来绑定服务，调用者和绑定者绑在一起， 调用者一旦退出服务也就终止了
     * 【onCreate()->onBind()->onUnbind()->onDestroy()】。
     *
     * 鱼说:简而言之,我们做个最简单的测试 第一步:使用startService()启动服务播放音乐,退出应用程序后,音乐还在播放.
     * 第二步:使用bindService()启动服务播放音乐,退出应用程序后,音乐的播放也随之停止了.
     *
     * 创建一个 ServiceConnection 对象
     */
    final ServiceConnection connection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            APPLog.I("#####onServiceDisconnected");
        }
        public void onServiceConnected(ComponentName name, IBinder service) {
            APPLog.I("#####onServiceConnected");
        }
    };
    /**
     * 绑定主服务
     *
     * @param context
     */
    public void startBindService(Context context) {
        try {
            Intent intent = new Intent(context, MainService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 解除绑定主服务
     */
    public void unBindService() {
        unbindService(connection);
    }

    // =================================================================================
    private SlideLeftFragment mSlideLeftFrag;
    private SlideRightFragment mSlideRightFrag;
    private TextView tv_value, tv_2_value, tv_state, tv_process, tv_imsi_cnt,tv_imsi,tv_temp,battery,tv_gps,tv_air_sync,tv_air_sync1;
    private Button btn_trace, btn_arfcn, btn_txpwr, tb_query, btn_slide_left, btn_slide_right;
    private ImageView iv_temp;
    private TabItem tab_cell_1;
    private RecyclerView arfcnListView;
    private ArfcnAdapter arfcnAdapter;
    // 用户配置自动布控的基站列表
    private Context mContext;
    private LayoutInflater mInflater;
    // 当计时器延时用
    private Handler handler = new Handler();
    private PowerManager.WakeLock mWakeLock;
    private Dialog mDialog;
    private int scanArfcnOp = AtCmd.OP.MOBILE;
    private RadioGroup rg_arfcn, rg_power, rb_mode;
    private TraceDialog traceDialog;
    private GnbCityDialog gnbCityDialog;
    private CfgArfcnDialog mCfgArfcnDialog;
    private SetArfcnChangeDialog mSetArfcnChangeDialog;
    private List<UeidBean> blackList = new ArrayList<UeidBean>();
    private List<ImsiBean> imsiList = new ArrayList<ImsiBean>();
    private boolean traceBarEnable = false;
    private int workState = GnbBean.State.NONE, txPwrOffset = -1, workMode = Enum.WorkMode.NORMAL;
    private String queryImsi = "", gnbVersion = "";
    private boolean isSetModule = false;
    private int traceTac, maxTac, valueZero, rebootCnt;
    private boolean tacChange, bsTimeSet;

//    private BarChartView traceBarView;
    private LinearLayout traceBarView;
    private BarData traceBarData = new BarData();
    private WifiBarView wifi_bar;
    private List<WifiData> wifiAlarmList = new ArrayList<WifiData>();
    private int curWifiAlarmIdx = 0;
    private String gnbLogName = "";
    private String upgradeFileName = "", upgradeFilePath = "";
    private List<FileItem> mUpdateFilesList = new ArrayList<FileItem>();

    private int FreqScanCount = 0;
    private boolean isGps_sync = false;
    private boolean isStopScan = true;
    private boolean isGpsScan = false;
    private int report_level = 0;
    private int async_enable = 1;
    private final List<Integer> arfcnList_N1 = new ArrayList<>();
    private final List<Integer> arfcnList_N28 = new ArrayList<>();
    private final List<Integer> arfcnList_N41 = new ArrayList<>();
    private final List<Integer> arfcnList_N78 = new ArrayList<>();
    private final List<Integer> arfcnList_N79 = new ArrayList<>();
    private final List<Integer> setArfcnList_N1 = new ArrayList<>();
    private final List<Integer> setArfcnList_N28 = new ArrayList<>();
    private final List<Integer> setArfcnList_N41 = new ArrayList<>();
    private final List<Integer> setArfcnList_N78 = new ArrayList<>();
    private final List<Integer> setArfcnList_N79 = new ArrayList<>();
    private final List<Boolean> enablelList = new ArrayList<>();
    private int countFirst = 0, countSecond = 0 ,countThird = 0, countFourth = 0;
    private boolean isFirstStart = false, isSecondStart = false, isThirdStart = false, isFourthStart = false;
    private String scanFreqName = "";
    private boolean isFirstRec = true;
    private boolean isStart = true, noTraceFlag = true;
    private final ArrayList<ScanArfcnBean> scanArfcnBeanList = new ArrayList<>();
    private int scanCount = 0;
    private int dualCell = PrefUtil.build().getCell();
//    private int dualCell = 2;
    private long StopFreqScanTime = 0;
    private BarChartUtil barChartUtil;
    private BarChartUtil barChartUtil1;
    private BarChart barChart,barChart1;
    private TabLayout tabLayout;
    private DrawBatteryView batteryView;
    @Override
    public void OnFtpConnectFail() {

    }

    @Override
    public void OnFtpGetFileRsp(boolean state) {
        APPLog.I("OnFtpGetFileRsp(): " + state);
        if (state) {
            if (report_level == 1) {
                showRemindDialog(getStr(R.string.reminder), getStr(R.string.get_success));
            } else {
                if (tv_state.getText().toString().equals(getStr(R.string.state_get_log))) {
                    showRemindDialog(getStr(R.string.reminder), "读取LOG文件成功，请到【NR5G→日志与升级】目录下查看");
                } else {
                    showRemindDialog(getStr(R.string.reminder), "读取黑匣子文件成功，请到【NR5G→黑匣子】目录下查看");
                }
                setWorkState(GnbBean.State.IDLE);
                refreshStateView(getStr(R.string.state_wait_cfg));
            }

        } else {
            if (report_level == 1) {
            } else {
                showRemindDialog(getStr(R.string.reminder), getStr(R.string.get_log_fail));
                setWorkState(GnbBean.State.IDLE);
                refreshStateView(getStr(R.string.state_wait_cfg));
            }
        }
        tv_process.setVisibility(View.GONE);
//        tv_imsi_cnt.setVisibility(View.VISIBLE);
    }

    @Override
    public void OnFtpPutFileRsp(boolean state) {
        APPLog.I("OnFtpPutFileRsp(): " + state);
        if (state) {
            setWorkState(GnbBean.State.UPDATE);
            refreshStateView(getStr(R.string.state_upgrade_ing));
            MessageController.build().setGnbUpgrade(3, upgradeFileName, upgradeFilePath);
        } else {
            setWorkState(GnbBean.State.IDLE);
            refreshStateView(getStr(R.string.state_upgrade_copying_file_fail));
            showRemindDialog(getStr(R.string.bs_upgrade), getStr(R.string.state_upgrade_copying_file_fail));
        }
    }

    @Override
    public void OnFtpGetFileProcess(final long l) {
        APPLog.I("OnFtpGetFileProcess(): process:" + l);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_process.setText(MessageFormat.format("进度: {0} / 100", l));
            }
        });
    }

    @Override
    public void onSave() {
        if (imsiList.size()==0){
            Util.showToast(getApplicationContext(), mContext.getResources().getString(R.string.imsi_null));
        }else {
            createFileDialog();
        }
    }

    @Override
    public void onClear() {
        imsiList.clear();
        mSlideRightFrag.refreshListView(imsiList, imsiList.size() - 1, false);
        tv_imsi_cnt.setText("0");
    }
    private void createFileDialog() {
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_create_file, null);
        final EditText mEtFileName = view.findViewById(R.id.ed_file_name);
        Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mEtFileName.getText().toString())) {
                    if (mEtFileName.getText().toString().startsWith("+")||mEtFileName.getText().toString().startsWith("-")
                            ||mEtFileName.getText().toString().startsWith("/")||mEtFileName.getText().toString().startsWith("@")){
                        Util.showToast(mContext, "请输入正确的文件名！");
                        return;
                    }
                    closeCustomDialog();
                    //Util.showToast(getApplicationContext(), "正在保存文件!");
                    saveImsiList(mEtFileName.getText().toString());
                } else {
                    Util.showToast(getApplicationContext(), "请输入文件名!");
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }
    private void saveImsiList(String fileName) {
        String stime = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmmss");
        if (fileName.length() > 0) {
            fileName = fileName + "_" + stime;
        } else {
            fileName = "G70_" + stime;
        }
        String filePath = FileUtil.build().createOrAppendFile(mContext.getResources().getString(R.string.file_name) + fileName, FileProtocol.DIR_TRACE_IMSI, fileName, 0);
        APPLog.D("saveTraceImsi(): filePath = " + filePath);

        StringBuilder sb = new StringBuilder();
        int size = imsiList.size();
        sb.append("\r\n");
        sb.append(mContext.getResources().getString(R.string.file_path)).append(filePath);
        sb.append("\r\n");
        String conunt = String.format(mContext.getResources().getString(R.string.catch_count), size);
        sb.append(conunt);
        sb.append("\r\n");
        for (int i = 0; i < size; i++) {
            sb.append(DateUtil.formateTimeHMS(imsiList.get(i).getLatestTime()));
            sb.append("\t\t");
            sb.append(imsiList.get(i).getImsi());
            sb.append("\t\t");
            sb.append(imsiList.get(i).getArfcn());
            sb.append("\t\t");
            sb.append(imsiList.get(i).getPci());
            sb.append("\r\n");
        }
        FileUtil.build().appendFile(filePath, sb.toString());

        showRemindDialog(mContext.getResources().getString(R.string.reminder), mContext.getResources().getString(R.string.file_path) + filePath);
    }

    @Override
    public void onBaseStationScanResult(int type, List<CellBean> list) {
        try {

            if (scanArfcnBeanList.size() == 0) {
                for (int i = 0; i < list.size(); i++) {
                    scanArfcnBeanList.add(new ScanArfcnBean(list.get(i).getTac(),list.get(i).getEci(),
                            0,Integer.parseInt(list.get(i).getArfcn()),Integer.parseInt(list.get(i).getPci()),
                            Integer.parseInt(list.get(i).getRsrp()),0,0,0, Integer.parseInt(list.get(i).getMcc()),
                            Integer.parseInt(list.get(i).getMnc()),0,0,0));
                }
            } else {
                for (int i = 0; i < list.size(); i++) {
                    boolean isAdd = true;
                    for (int j = 0; j < scanArfcnBeanList.size(); j++) {
                        if (scanArfcnBeanList.get(j).getDl_arfcn() == Integer.parseInt(list.get(i).getArfcn()) &&
                                scanArfcnBeanList.get(j).getPci() == Integer.parseInt(list.get(i).getPci()) ) {
                            isAdd = false;
                            scanArfcnBeanList.remove(scanArfcnBeanList.get(j));
                            scanArfcnBeanList.add(new ScanArfcnBean(list.get(i).getTac(),list.get(i).getEci(),
                                    0,Integer.parseInt(list.get(i).getArfcn()),Integer.parseInt(list.get(i).getPci()),
                                    Integer.parseInt(list.get(i).getRsrp()),0,0,0, Integer.parseInt(list.get(i).getMcc()),
                                    Integer.parseInt(list.get(i).getMnc()),0,0,0));
                            break;
                        }
                    }
                    if (isAdd) {
                        scanArfcnBeanList.add(new ScanArfcnBean(list.get(i).getTac(),list.get(i).getEci(),
                                0,Integer.parseInt(list.get(i).getArfcn()),Integer.parseInt(list.get(i).getPci()),
                                Integer.parseInt(list.get(i).getRsrp()),0,0,0, Integer.parseInt(list.get(i).getMcc()),
                                Integer.parseInt(list.get(i).getMnc()),0,0,0));
                    }
                }

            }
//                setListViewHeightBasedOnChildren(arfcnListView);
            arfcnAdapter.setArfcnList(scanArfcnBeanList);
        }catch (NullPointerException|NumberFormatException exception){
            exception.printStackTrace();
        }

    }
}
