package com.simdo.g73cs.Fragment;

import static com.simdo.g73cs.MainActivity.devA;
import static com.simdo.g73cs.MainActivity.devB;
import static com.simdo.g73cs.Util.DataUtil.isNumeric;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.Logcat.SLog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.UeidBean;
import com.nr.Gnb.Response.GnbCmdRsp;
import com.nr.Gnb.Response.GnbFreqScanRsp;
import com.nr.Gnb.Response.GnbStateRsp;
import com.nr.Gnb.Response.GnbTraceRsp;
import com.nr.Socket.ConnectProtocol;
import com.nr.Socket.MessageControl.MessageController;
import com.nr.Util.OpLog;
import com.simdo.g73cs.Adapter.FragmentAdapter;
import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.Bean.DataUpBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.Dialog.FreqDialog;
import com.simdo.g73cs.Dialog.TraceDialog;
import com.simdo.g73cs.File.FileProtocol;
import com.simdo.g73cs.File.FileUtil;
import com.simdo.g73cs.File.SftpUploader;
import com.simdo.g73cs.Listener.OnTraceSetListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DateUtil;
import com.simdo.g73cs.Util.ExcelUtil;
import com.simdo.g73cs.Util.FreqUtil;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.TraceUtil;
import com.simdo.g73cs.View.BlackListSlideAdapter;
import com.simdo.g73cs.View.MyRadioGroup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class TraceCatchFragment extends Fragment implements OnTraceSetListener {

    Context mContext;
    private List<ImsiBean> mImsiList;
    private List<ImsiBean> mAllImsiList;
    private BlackListSlideAdapter adapter;
    private TraceUtil mTraceUtilNr;
    private boolean isResetBlack = false;
    public boolean isAutoRun = false;
    public final HashMap<String, LinkedList<ArfcnPciBean>> arfcnBeanHashMap = new HashMap<>();

    public TraceCatchFragment() {
    }

    public TraceCatchFragment(Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AppLog.I("TraceCatchFragment onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.I("TraceCatchFragment onCreateView");

        View root = inflater.inflate(R.layout.pager_trace_catch, container, false);
        initData();
        initView(root);
        return root;
    }

    int all_count = 0, unicom_count = 0, mobile_count = 0, telecom_count = 0, sva_count = 0;
    TextView tv_do_btn;
    CatchChildFragment mCatchChildFragment;
    //TraceChildFragment mTraceChildFragment;
    ShowChildFragment mShowChildFragment;
    Switch switch_tx;
    ImageView iv_go_left, iv_go_right, iv_add_remove_black;
    int autoArfcnTime;
    boolean isClickClear = false;
    private void initView(View root) {

        ImageView iv_clear = root.findViewById(R.id.iv_clear);
        iv_clear.setImageResource(R.mipmap.clear_icon);
        iv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAllImsiList.size() > 0) {
                    new AlertDialog.Builder(mContext)
                            .setTitle("清空提示")
                            .setMessage("确定要清空侦码列表吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    isClickClear = true;
                                    all_count = 0;
                                    unicom_count = 0;
                                    mobile_count = 0;
                                    telecom_count = 0;
                                    sva_count = 0;
                                    mCatchChildFragment.setCatchCount(all_count, telecom_count, mobile_count, unicom_count, sva_count);
                                    mHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mImsiList.clear();
                                            mAllImsiList.clear();
                                            mCatchChildFragment.clear();
                                            isClickClear = false;
                                        }
                                    },500);
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }else {
                    mCatchChildFragment.clear();
                    MainActivity.getInstance().showToast("列表已无数据");
                }

            }
        });

        final ImageView iv_sort = root.findViewById(R.id.iv_sort);
        iv_sort.setImageResource(R.mipmap.no_sort_icon);
        iv_sort.setTag("no");
        iv_sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAllImsiList.size() <= 0) {
                    MainActivity.getInstance().showToast("列表为空，无法排序");
                    return;
                }
                /*if (!tv_do_btn.getText().toString().equals("开启")) {
                    MainActivity.getInstance().showToast("请停止工作后，再进行场强排序");
                    return;
                }*/
                String tag = iv_sort.getTag().toString();
                boolean b;
                switch (tag) {
                    case "up":
                        b = mCatchChildFragment.setSortModel("no");
                        if (b) {
                            iv_sort.setImageResource(R.mipmap.no_sort_icon);
                            iv_sort.setTag("no");
                        } else MainActivity.getInstance().showToast("请勿点击过快");
                        break;
                    case "down":
                        b = mCatchChildFragment.setSortModel("up");
                        if (b) {
                            iv_sort.setImageResource(R.mipmap.up_icon);
                            iv_sort.setTag("up");
                        } else MainActivity.getInstance().showToast("请勿点击过快");
                        break;
                    case "no":
                        b = mCatchChildFragment.setSortModel("down");
                        if (b) {
                            iv_sort.setImageResource(R.mipmap.down_icon);
                            iv_sort.setTag("down");
                        } else MainActivity.getInstance().showToast("请勿点击过快");
                        break;
                }
            }
        });

        ImageView iv_black_list = root.findViewById(R.id.iv_black_list);
        iv_black_list.setImageResource(R.mipmap.black_list_icon);
        iv_black_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBlackListDialog();
            }
        });

        // 开始、结束工作按钮
        tv_do_btn = root.findViewById(R.id.tv_do_btn);
        tv_do_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doWork();
            }
        });

        // 近距增益按钮
        switch_tx = root.findViewById(R.id.switch_tx);

        switch_tx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
                if (deviceList.size() <= 0) {
                    switch_tx.setChecked(!switch_tx.isChecked());
                    MainActivity.getInstance().showToast("设备不在线，请先开启并连接设备");
                    return;
                }
                if (getBtnStr().equals("开启")) {
                    switch_tx.setChecked(!switch_tx.isChecked());
                    MainActivity.getInstance().showToast("不在工作状态，无法调整");
                    return;
                }
                if (switch_tx.isChecked()) doSetTxPwrOffset(-4);
                else doSetTxPwrOffset(0);
            }
        });

        // 左右滑动导航
        List<Fragment> fragmentList = new ArrayList<>();
        mCatchChildFragment = new CatchChildFragment(mContext, mImsiList, this);
        //mTraceChildFragment = new TraceChildFragment(mContext);
        mShowChildFragment = new ShowChildFragment(mContext);
        fragmentList.add(mCatchChildFragment);
        fragmentList.add(mShowChildFragment);
        String[] titles = new String[]{"搜寻模式", "报值趋势"};

        ViewPager2 view_pager = root.findViewById(R.id.view_pager_trace_catch);
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getChildFragmentManager(), getLifecycle(), fragmentList);
        view_pager.setAdapter(fragmentAdapter);
        view_pager.setCurrentItem(0);

        // 左右滑动图标
        iv_go_left = root.findViewById(R.id.iv_go_left);
        iv_go_left.setImageResource(R.mipmap.left_info_icon);
        iv_go_right = root.findViewById(R.id.iv_go_right);
        iv_go_right.setImageResource(R.mipmap.right_arrow);
        iv_go_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view_pager.getCurrentItem() == 0) {
                    MainActivity.getInstance().menu.showMenu();
                    return;
                }
                view_pager.setCurrentItem(0);
                //iv_go_left.setImageResource(R.mipmap.left_info_icon);
                iv_go_right.setImageResource(R.mipmap.right_arrow);
                mShowChildFragment.setStart(null, false);
            }
        });

        iv_go_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view_pager.getCurrentItem() == 1) return;
                view_pager.setCurrentItem(1);
                iv_go_left.setImageResource(R.mipmap.left_arrow);
                //iv_go_right.setImageResource(R.mipmap.right_arrow_dark);
                mShowChildFragment.setStart(mImsiBean, true);
            }
        });

        iv_add_remove_black = root.findViewById(R.id.iv_add_remove_black);
        iv_add_remove_black.setVisibility(View.GONE);
        iv_add_remove_black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<MyUeidBean> blackListAll = MainActivity.getInstance().getBlackList();
                boolean isAdd = true;
                for (int i = 0; i < blackListAll.size(); i++) {
                    if (blackListAll.get(i).getUeidBean().getImsi().equals(mImsiBean.getImsi())) {
                        MainActivity.getInstance().getBlackList().remove(i);
                        isAdd = false;
                        iv_add_remove_black.setImageResource(R.mipmap.add_to_black);
                        break;
                    }
                }
                if (isAdd) {
                    MainActivity.getInstance().addBlackList(mImsiBean.getImsi().substring(11), mImsiBean.getImsi());
                    iv_add_remove_black.setImageResource(R.mipmap.remove_to_black);
                }

                for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                    sendAllCellBlackList(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId());
                }
                PrefUtil.build().putUeidList(MainActivity.getInstance().getBlackList());
                MainActivity.getInstance().showToast(isAdd ? "已添加进白名单" : "已从白名单移除");
            }
        });

        TabLayout tab_layout = root.findViewById(R.id.tab_trace_catch);

        final TextView tv_title = root.findViewById(R.id.tv_title);
        tv_title_mode = root.findViewById(R.id.tv_title_mode);
        tv_title_mode.setText(isAutoRun ? "自动模式" : "手动模式");
        tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (view_pager.getCurrentItem() == 0) {
                    iv_go_left.setImageResource(R.mipmap.left_info_icon);
                    iv_go_right.setImageResource(R.mipmap.right_arrow);
                    mShowChildFragment.setStart(null, false);
                } else {
                    iv_go_left.setImageResource(R.mipmap.left_arrow);
                    iv_go_right.setImageResource(R.mipmap.right_arrow_dark);
                    mShowChildFragment.setStart(mImsiBean, true);
                }
                tv_title.setText(tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        TabLayoutMediator tab = new TabLayoutMediator(tab_layout, view_pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(titles[position]);
            }
        });
        tab.attach();
    }
    TextView tv_title_mode;
    public void setIsAutoRun(boolean isAutoRun){
        if (!getBtnStr().equals("开启")) {
            MainActivity.getInstance().showToast("设备工作中，无法修改");
            return;
        }
        this.isAutoRun = isAutoRun;
        tv_title_mode.setText(isAutoRun ? "自动模式" : "手动模式");
    }

    private void initData() {
        String string = PrefUtil.build().getValue("Auto_Arfcn_time", "60").toString();
        if (string.isEmpty()) autoArfcnTime = 0;
        else autoArfcnTime = Integer.parseInt(string);
        mImsiList = new ArrayList<>();
        mAllImsiList = new ArrayList<>();
        mTraceUtilNr = new TraceUtil();
        // 初始化搜寻参数
        // 通道一
        mTraceUtilNr.setTacChange(GnbProtocol.CellId.FIRST, false);
        mTraceUtilNr.setSwap_rf(GnbProtocol.CellId.FIRST, 0);
        mTraceUtilNr.setTxPwr(GnbProtocol.CellId.FIRST, 0);
        mTraceUtilNr.setCfr(GnbProtocol.CellId.FIRST, 1);
        mTraceUtilNr.setUeMaxTxpwr(GnbProtocol.CellId.FIRST, "10");
        mTraceUtilNr.setCid(GnbProtocol.CellId.FIRST, 65536);
        mTraceUtilNr.setSplit_arfcn_dl(GnbProtocol.CellId.FIRST, "0");

        // 通道二
        mTraceUtilNr.setTacChange(GnbProtocol.CellId.SECOND, false);
        mTraceUtilNr.setSwap_rf(GnbProtocol.CellId.SECOND, 0);
        mTraceUtilNr.setTxPwr(GnbProtocol.CellId.SECOND, 0);
        mTraceUtilNr.setCfr(GnbProtocol.CellId.SECOND, 1);
        mTraceUtilNr.setUeMaxTxpwr(GnbProtocol.CellId.SECOND, "10");
        mTraceUtilNr.setCid(GnbProtocol.CellId.SECOND, 65537);
        mTraceUtilNr.setSplit_arfcn_dl(GnbProtocol.CellId.SECOND, "0");

        // 通道三
        mTraceUtilNr.setTacChange(GnbProtocol.CellId.THIRD, false);
        mTraceUtilNr.setSwap_rf(GnbProtocol.CellId.THIRD, 0);
        mTraceUtilNr.setTxPwr(GnbProtocol.CellId.THIRD, 0);
        mTraceUtilNr.setCfr(GnbProtocol.CellId.THIRD, 1);
        mTraceUtilNr.setUeMaxTxpwr(GnbProtocol.CellId.THIRD, "10");
        mTraceUtilNr.setCid(GnbProtocol.CellId.THIRD, 65538);
        mTraceUtilNr.setSplit_arfcn_dl(GnbProtocol.CellId.THIRD, "0");

        // 通道四
        mTraceUtilNr.setTacChange(GnbProtocol.CellId.FOURTH, false);
        mTraceUtilNr.setSwap_rf(GnbProtocol.CellId.FOURTH, 0);
        mTraceUtilNr.setTxPwr(GnbProtocol.CellId.FOURTH, 0);
        mTraceUtilNr.setCfr(GnbProtocol.CellId.FOURTH, 1);
        mTraceUtilNr.setUeMaxTxpwr(GnbProtocol.CellId.FOURTH, "10");
        mTraceUtilNr.setCid(GnbProtocol.CellId.FOURTH, 65539);
        mTraceUtilNr.setSplit_arfcn_dl(GnbProtocol.CellId.FOURTH, "0");

        all_count = 0;
        unicom_count = 0;
        mobile_count = 0;
        telecom_count = 0;
        sva_count = 0;
        initAutoArfcnList();
    }

    public void initAutoArfcnList() {
        LinkedList<ArfcnPciBean> TD1 = new LinkedList<>(); // N28/N78/N79
        TD1.add(new ArfcnPciBean("627264", "1001"));
        TD1.add(new ArfcnPciBean("633984", "1001"));
        TD1.add(new ArfcnPciBean("154810", "1001"));
        TD1.add(new ArfcnPciBean("152650", "1001"));
        TD1.add(new ArfcnPciBean("723360", "1001"));

        LinkedList<ArfcnPciBean> TD2 = new LinkedList<>(); // B34/B39/B40/B41
        TD2.add(new ArfcnPciBean("38950", "501"));
        TD2.add(new ArfcnPciBean("38350", "501"));
        TD2.add(new ArfcnPciBean("36275", "501"));
        TD2.add(new ArfcnPciBean("38400", "501"));
        TD2.add(new ArfcnPciBean("39148", "501"));
        TD2.add(new ArfcnPciBean("40936", "501"));
        TD2.add(new ArfcnPciBean("38544", "501"));
        TD2.add(new ArfcnPciBean("39292", "501"));

        LinkedList<ArfcnPciBean> TD3 = new LinkedList<>(); // N1/N41
        TD3.add(new ArfcnPciBean("504990", "1002"));
        TD3.add(new ArfcnPciBean("428910", "1002"));
        TD3.add(new ArfcnPciBean("427250", "1002"));
        TD3.add(new ArfcnPciBean("422890", "1002"));
        TD3.add(new ArfcnPciBean("516990", "1002"));

        LinkedList<ArfcnPciBean> TD4 = new LinkedList<>(); // B1/B3/B5/B8
        TD4.add(new ArfcnPciBean("1650", "502"));
        TD4.add(new ArfcnPciBean("100", "502"));
        TD4.add(new ArfcnPciBean("1300", "502"));
        TD4.add(new ArfcnPciBean("1850", "502"));
        TD4.add(new ArfcnPciBean("2452", "502"));
        TD4.add(new ArfcnPciBean("3683", "502"));
        TD4.add(new ArfcnPciBean("450", "502"));
        TD4.add(new ArfcnPciBean("1825", "502"));

        arfcnBeanHashMap.put("TD1", TD1);
        arfcnBeanHashMap.put("TD2", TD2);
        arfcnBeanHashMap.put("TD3", TD3);
        arfcnBeanHashMap.put("TD4", TD4);
    }

    public void resetAutoArfcnTime() {
        String string = PrefUtil.build().getValue("Auto_Arfcn_time", "60").toString();
        if (string.isEmpty()) autoArfcnTime = 0;
        else autoArfcnTime = Integer.parseInt(string);
    }

    int txValue = 0; // 增益的值

    private void doSetTxPwrOffset(final int value) {
        if (txValue == value || txValue == -1) return;
        txValue = value;

        AppLog.I("TraceFragment rg_rx_gain rxValue = " + value);
        for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
            TraceUtil traceUtil = bean.getTraceUtil();
            String id = bean.getRsp().getDeviceId();
            if (MessageController.build().getTraceType(id) == GnbProtocol.TraceType.CONTROL) {

                String first_arfcn = traceUtil.getArfcn(GnbProtocol.CellId.FIRST);
                if (!first_arfcn.isEmpty())
                    MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.FIRST, Integer.parseInt(first_arfcn), value);

                String second_arfcn = traceUtil.getArfcn(GnbProtocol.CellId.SECOND);
                if (!second_arfcn.isEmpty()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.SECOND, Integer.parseInt(second_arfcn), value);
                        }
                    }, 300);
                }

                String third_arfcn = traceUtil.getArfcn(GnbProtocol.CellId.THIRD);
                if (!third_arfcn.isEmpty()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.THIRD, Integer.parseInt(third_arfcn), value);
                        }
                    }, 600);
                }

                String fourth_arfcn = traceUtil.getArfcn(GnbProtocol.CellId.FOURTH);
                if (!fourth_arfcn.isEmpty()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.FOURTH, Integer.parseInt(fourth_arfcn), value);
                        }
                    }, 900);
                }
            }
        }
    }

    private void doWork() {
        String text = tv_do_btn.getText().toString();
        switch (text) {
            case "开启": {
                List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
                if (deviceList.size() <= 0) {
                    MainActivity.getInstance().showToast("设备不在线，请先开启并连接设备");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                String idleDev = "";
                for (DeviceInfoBean bean : deviceList) {
                    switch (bean.getWorkState()) {
                        case GnbBean.State.NONE:
                            sb.append("-1");
                            break;
                        case GnbBean.State.IDLE:
                            sb.append("0");
                            idleDev = bean.getRsp().getDevName();
                            break;
                    }
                }
                AppLog.I("doWork() 开启搜寻 sb = " + sb);
                String doStr = sb.toString();

                if (doStr.isEmpty() || doStr.equals("-1")) {
                    MainActivity.getInstance().showToast("设备忙，请等待或停止其他工作");
                    return;
                }
                if (doStr.equals("-1-1")) {
                    MainActivity.getInstance().showToast("设备不在线，请先开启并连接设备");
                    return;
                }
                if (doStr.equals("00")) {
                    startTrace("");  // 两设备均在空闲状态下，启动搜寻
                    return;
                }
                if (doStr.contains("0")) {
                    startTrace(idleDev); // 单设备在空闲状态下，启动搜寻
                }

                break;
            }
            case "启/停": {
                StringBuilder sb = new StringBuilder();
                String idleDev = "";
                for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                    if (bean.getWorkState() == GnbBean.State.IDLE) {
                        idleDev = bean.getRsp().getDevName();
                        sb.append("\t\n").append(bean.getRsp().getDevName());
                    }
                }
                AppLog.I("doWork() 开启/结束搜寻 sb = " + sb);

                String finalIdleDev = idleDev;
                new AlertDialog.Builder(mContext)
                        .setTitle("操作引导")
                        .setMessage("当前仅有设备：" + sb + "\t\n处于空闲状态\t\n确定开启搜寻？")
                        .setNeutralButton("开启搜寻", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startTrace(finalIdleDev); // 单设备在空闲状态下，启动搜寻
                            }
                        })
                        .setPositiveButton("结束所有", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                stopTraceDialog();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();

                break;
            }
            case "结束":
                stopTraceDialog();
                break;
            case "取消":
                //MainActivity.getInstance().showToast("取消中，请勿多次点击!");
                cancelWorkDialog();
                break;
            case "结束中":
                MainActivity.getInstance().showToast("结束中，请勿多次点击!");
                break;
        }
    }

    private void cancelWorkDialog() {
        AppLog.D("FreqFragment stopFreqScanDialog()");
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);

        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClickStop = true;
                MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP);
                MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP);
                MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.STOP);
                MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.STOP);
                refreshTraceBtn();

                mHandler.removeMessages(11);
                mHandler.sendEmptyMessageDelayed(11, 8000);

                MainActivity.getInstance().closeCustomDialog();
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    public void setProgress(int type, int pro, int cell_id, String info, boolean isFail) {
        if (cell_id == 0 && autoRun[0]) return;
        if (cell_id == 1 && autoRun[1]) return;
        if (cell_id == 2 && autoRun[2]) return;
        if (cell_id == 3 && autoRun[3]) return;
        MainActivity.getInstance().updateProgress(type, pro, cell_id, info, isFail);
    }

    private void showBlackListDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_blacklist_silde, null);

        RecyclerView black_list = view.findViewById(R.id.black_list);
        black_list.setLayoutManager(new LinearLayoutManager(mContext));

        adapter = new BlackListSlideAdapter(mContext, MainActivity.getInstance().getBlackList(), new BlackListSlideAdapter.ListItemListener() {
            @Override
            public void onItemClickListener(MyUeidBean bean) {
                //showChangeTraceImsi(bean);
            }

            @Override
            public void onItemRemoveListener(MyUeidBean myUeidBean) {
                for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                    sendAllCellBlackList(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId());
                }
            }
        });
        black_list.setAdapter(adapter);
        view.findViewById(R.id.tv_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBlackListCfgDialog(true, -1);
            }
        });

        view.findViewById(R.id.tv_import).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //Uri uri = Uri.fromFile(new File(FileUtil.build().getSDPath()));
                //设置xls xlsx 2种类型 , 以 | 划分
                intent.setDataAndType(null, "application/vnd.ms-excel|application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                //在API>=19之后设置多个类型采用以下方式，setType不再支持多个类型
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent.putExtra(Intent.EXTRA_MIME_TYPES,
                            new String[]{"application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"});
                } else {
                    intent.setType("application/vnd.ms-excel|application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                }
                //intent.setDataAndType(null, "*/*");
                //startActivityForResult(intent, 100);
                activityForResult.launch(intent);
            }
        });

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false, true);
    }

    /**
     * 添加黑名单
     */
    private void showBlackListCfgDialog(final boolean isAdd, final int position) {

        MainActivity.getInstance().createCustomDialog(false);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_black_list_cfg, null);

        TextView tv_black_cfg_title = view.findViewById(R.id.tv_black_cfg_title);

        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        final TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        TextView back = view.findViewById(R.id.back);
        final EditText ed_imsi_name = view.findViewById(R.id.ed_imsi_name);
        final AutoCompleteTextView actv_imsi = view.findViewById(R.id.actv_imsi);

        if (isAdd) {
            tv_black_cfg_title.setText("添加名单");
            btn_ok.setText("添加");
            btn_cancel.setText("取消");
            btn_cancel.setTextColor(Color.parseColor("#666666"));
            back.setVisibility(View.GONE);
        } else {
            tv_black_cfg_title.setText("编辑名单");
            btn_ok.setText("确认修改");
            btn_cancel.setText("删除");
            btn_cancel.setTextColor(Color.RED);
            back.setVisibility(View.VISIBLE);
            if (position != -1) {
                MyUeidBean bean = MainActivity.getInstance().getBlackList().get(position);
                ed_imsi_name.setText(bean.getName());
                actv_imsi.setText(bean.getUeidBean().getImsi());
            }
        }

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String imsi_name = ed_imsi_name.getText().toString();
                String imsi = actv_imsi.getText().toString();
                if (imsi_name.isEmpty() || imsi.isEmpty()) {
                    MainActivity.getInstance().showToast("数据不能为空");
                    return;
                }
                if (imsi.length() != GnbProtocol.MAX_IMSI_USE_LEN) {
                    MainActivity.getInstance().showToast("IMSI数据错误,正确值为：15位数字");
                    return;
                }

                MyUeidBean bean;
                if (isAdd) {
                    boolean canAdd = true;
                    for (MyUeidBean bean1 : MainActivity.getInstance().getBlackList()) {
                        if (bean1.getUeidBean().getImsi().equals(imsi)) {
                            canAdd = false;
                            break;
                        }
                    }
                    if (canAdd) {
                        bean = new MyUeidBean(imsi_name, new UeidBean(imsi, imsi), false, false);
                        MainActivity.getInstance().getBlackList().add(bean);
                        adapter.notifyDataSetChanged();
                        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                            sendAllCellBlackList(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId());
                        }
                    } else {
                        MainActivity.getInstance().showToast("添加失败，当前IMSI已存在黑名单列表中，请重新输入!");
                        return;
                    }
                } else {
                    bean = MainActivity.getInstance().getBlackList().get(position);
                    bean.setName(imsi_name);
                    bean.getUeidBean().setImsi(imsi);
                    MainActivity.getInstance().getBlackList().set(position, bean);
                    adapter.notifyItemChanged(position);
                }
                PrefUtil.build().putUeidList(MainActivity.getInstance().getBlackList());
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_cancel.getText().toString().equals("删除")) {
                    new AlertDialog.Builder(mContext)
                            .setTitle("删除提示")
                            .setMessage("确定要删除该黑名单吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    MainActivity.getInstance().getBlackList().remove(position);
                                    PrefUtil.build().putUeidList(MainActivity.getInstance().getBlackList());
                                    adapter.notifyDataSetChanged();
                                    MainActivity.getInstance().closeCustomDialog();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                } else MainActivity.getInstance().closeCustomDialog();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    ActivityResultLauncher<Intent> activityForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            AppLog.I("SettingFragment onActivityResult result = " + result);
            if (result.getResultCode() == Activity.RESULT_OK) {
                if (result.getData() != null) {
                    AppLog.I("SettingFragment onActivityResult result.getData().getDataString() = " + result.getData().getDataString());
                    String dataString = result.getData().getDataString();
                    if (dataString != null && !dataString.isEmpty()) {
                        String[] split = dataString.split("%");
                        StringBuilder path = new StringBuilder(FileUtil.build().getSDPath());
                        for (int i = 1; i < split.length; i++) {
                            path.append("/");
                            path.append(split[i].substring(2));
                        }

                        //path.deleteCharAt(path.length() - 1);

                        List<List<String>> lists = ExcelUtil.readExcel(path.toString());
                        if (lists == null)
                            lists = ExcelUtil.readExcel(mContext, result.getData().getData(), true);
                        if (lists == null)
                            lists = ExcelUtil.readExcel(mContext, result.getData().getData(), false);
                        if (lists == null) {
                            MainActivity.getInstance().showToast("数据格式不符合规范，请检查!");
                            return;
                        }
                        StringBuilder sb = new StringBuilder();
                        int allCount = lists.size();
                        int reCount = 0;
                        int errCount = 0;
                        int sucCount = 0;
                        for (List<String> list : lists) {
                            String name = "";
                            String imsi = "";
                            for (int i = 0; i < list.size(); i++) {
                                String iStr = list.get(i);
                                if (imsi.isEmpty() && iStr.length() == 15 && isNumeric(iStr)) {
                                    imsi = iStr;
                                } else if (name.isEmpty() && !iStr.isEmpty()) {
                                    name = iStr;
                                }
                                if (!imsi.isEmpty() && !name.isEmpty()) break;
                            }

                            if (imsi.isEmpty() && name.isEmpty()) {
                                errCount++;
                            } else {
                                boolean b = MainActivity.getInstance().addBlackList(list.get(0), imsi);
                                if (b) sucCount++;
                                else reCount++;
                            }
                        }
                        sb.append("导入总数量：\t").append(allCount).append("\n");
                        sb.append("错误数：\t").append(errCount).append("\n");
                        sb.append("重复数：\t").append(reCount).append("\n");
                        sb.append("成功数：\t").append(sucCount).append("\n");
                        adapter.notifyDataSetChanged();
                        MainActivity.getInstance().showRemindDialog("导入结果", sb.toString());
                        if (sucCount > 0)
                            PrefUtil.build().putUeidList(MainActivity.getInstance().getBlackList());
                    }
                }
            } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                //MainActivity.getInstance().showToast("您未选择文件!");
            }
        }
    });


    public String getBtnStr() {
        return tv_do_btn.getText().toString();
    }

    public void setBtnStr(boolean start) {
        if (start) {
            if (tv_do_btn.getText().equals("结束")) return;
            tv_do_btn.setText("结束");
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_delete);
            isDataUpFlag = false;
            mDataUpHandler.removeMessages(0);
            mDataUpHandler.sendEmptyMessageDelayed(0, MainActivity.getInstance().mDataUpBean.getUpCycle() * 1000);
        } else {
            if (tv_do_btn.getText().equals("开启")) return;
            tv_do_btn.setText("开启");
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
        }
    }
    public void setBtnEnable(boolean enable){
        if (tv_do_btn != null) tv_do_btn.setEnabled(enable);
    }

    private ImsiBean mImsiBean;

    public void setToRightBtnShow(ImsiBean bean) {
        mImsiBean = bean;
        if (bean != null) {
            mShowChildFragment.setConfigInfo(bean);

            boolean isInBlack = false;
            for (MyUeidBean myUeidBean : MainActivity.getInstance().getBlackList()) {
                if (myUeidBean.getUeidBean().getImsi().equals(bean.getImsi())) {
                    isInBlack = true;
                    iv_add_remove_black.setImageResource(R.mipmap.remove_to_black);

                    break;
                }
            }
            if (!isInBlack) iv_add_remove_black.setImageResource(R.mipmap.add_to_black);

            iv_add_remove_black.setVisibility(View.VISIBLE);
            return;
        }
        iv_add_remove_black.setVisibility(View.GONE);
    }

    FreqDialog mFreqDialog;

    private void startTrace(String devName) {
        if (isAutoRun) {
            initAutoArfcnList();
            mFreqDialog = new FreqDialog(mContext, this);
            mFreqDialog.setOnTraceSetListener(this);
            mFreqDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    mFreqDialog = null;
                }
            });
            mFreqDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    mFreqDialog = null;
                }
            });
            mFreqDialog.show();
            return;
        }
        TraceDialog mDialog = new TraceDialog(mContext, this);
        mDialog.setOnTraceSetListener(this);
        mDialog.show();
    }

    boolean isClickStop = false;
    private void stopTraceDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClickStop = true;
                txValue = -1;
                autoRun[0] = false;
                autoRun[1] = false;
                autoRun[2] = false;
                autoRun[3] = false;
                for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                    tv_do_btn.setText("结束中");
                    int type = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName().contains(devA) ? 0 : 1;
                    final String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();

                    if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().isEnable(GnbProtocol.CellId.FOURTH) &&
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.FOURTH) == GnbBean.State.CONTROL) {
                        String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(GnbProtocol.CellId.FOURTH);
                        if (!arfcn.isEmpty()) {
                            MainActivity.getInstance().updateSteps(type, StepBean.State.success, "通道四结束工作中");
                            setProgress(type, 50, 3, "结束中", false);

                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FOURTH, System.currentTimeMillis());
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.STOP);

                            MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_CONTROL : GnbProtocol.UI_2_eNB_STOP_CONTROL, GnbProtocol.CellId.FOURTH);
                        }
                    } else {
                        if (MainActivity.getInstance().getDeviceList().get(i).getRsp().getFourthState() == GnbStateRsp.gnbState.GNB_STATE_CONTROL) {
                            MainActivity.getInstance().updateSteps(type, StepBean.State.success, "通道四结束工作中");
                            setProgress(type, 50, 3, "结束中", false);
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FOURTH, System.currentTimeMillis());
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.STOP);
                            MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_eNB_STOP_CONTROL, GnbProtocol.CellId.FOURTH);
                        } else if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.FOURTH) == GnbBean.State.GNB_CFG){
                            setProgress(type, 50, 3, "结束中", false);
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.STOP);
                        } else {
                            setProgress(type, 0, 3, "空闲", false);
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.IDLE);
                        }
                    }

                    if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().isEnable(GnbProtocol.CellId.THIRD) &&
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.THIRD) == GnbBean.State.CONTROL) {
                        String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(GnbProtocol.CellId.THIRD);
                        if (!arfcn.isEmpty()) {
                            int finalI = i;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, "通道三结束工作中");
                                    setProgress(type, 50, 2, "结束中", false);

                                    MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.THIRD, System.currentTimeMillis());
                                    MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.STOP);

                                    MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_CONTROL : GnbProtocol.UI_2_eNB_STOP_CONTROL, GnbProtocol.CellId.THIRD);

                                }
                            }, 300);
                        }
                    } else {
                        if (MainActivity.getInstance().getDeviceList().get(i).getRsp().getThirdState() == GnbStateRsp.gnbState.GNB_STATE_CONTROL) {
                            MainActivity.getInstance().updateSteps(type, StepBean.State.success, "通道三结束工作中");
                            setProgress(type, 50, 2, "结束中", false);

                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.THIRD, System.currentTimeMillis());
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.STOP);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_CONTROL, GnbProtocol.CellId.THIRD);
                                }
                            }, 300);

                        } else if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.THIRD) == GnbBean.State.GNB_CFG){
                            setProgress(type, 50, 2, "结束中", false);
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.STOP);
                        } else {
                            setProgress(type, 0, 2, "空闲", false);
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.IDLE);
                        }
                    }

                    if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().isEnable(GnbProtocol.CellId.SECOND) &&
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.CONTROL) {
                        String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND);
                        if (!arfcn.isEmpty()) {
                            int finalI = i;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, "通道二结束工作中");
                                    setProgress(type, 50, 1, "结束中", false);

                                    MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.SECOND, System.currentTimeMillis());
                                    MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP);

                                    MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_CONTROL : GnbProtocol.UI_2_eNB_STOP_CONTROL, GnbProtocol.CellId.SECOND);

                                }
                            }, 600);
                        }
                    } else {
                        if (MainActivity.getInstance().getDeviceList().get(i).getRsp().getSecondState() == GnbStateRsp.gnbState.GNB_STATE_CONTROL) {
                            MainActivity.getInstance().updateSteps(type, StepBean.State.success, "通道二结束工作中");
                            setProgress(type, 50, 1, "结束中", false);

                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.SECOND, System.currentTimeMillis());
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_eNB_STOP_CONTROL, GnbProtocol.CellId.SECOND);
                                }
                            }, 600);

                        } else if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.GNB_CFG){
                            setProgress(type, 50, 1, "结束中", false);
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP);
                        } else {
                            setProgress(type, 0, 1, "空闲", false);
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.IDLE);
                        }
                    }

                    if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().isEnable(GnbProtocol.CellId.FIRST) &&
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST) == GnbBean.State.CONTROL) {
                        String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(GnbProtocol.CellId.FIRST);
                        if (!arfcn.isEmpty()) {
                            int finalI = i;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, "通道一结束工作中");
                                    setProgress(type, 50, 0, "结束中", false);

                                    MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FIRST, System.currentTimeMillis());
                                    MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP);
                                    MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_CONTROL : GnbProtocol.UI_2_eNB_STOP_CONTROL, GnbProtocol.CellId.FIRST);
                                    refreshTraceBtn();
                                }
                            }, 900);
                        }
                    } else {
                        if (MainActivity.getInstance().getDeviceList().get(i).getRsp().getFirstState() == GnbStateRsp.gnbState.GNB_STATE_CONTROL) {
                            MainActivity.getInstance().updateSteps(type, StepBean.State.success, "通道一结束工作中");
                            setProgress(type, 50, 0, "结束中", false);

                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FIRST, System.currentTimeMillis());
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_CONTROL, GnbProtocol.CellId.FIRST);
                                }
                            }, 900);

                        } else if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST) == GnbBean.State.GNB_CFG){
                            setProgress(type, 50, 0, "结束中", false);
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP);
                        } else {
                            setProgress(type, 0, 0, "空闲", false);
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.IDLE);
                        }
                    }
                    iv_add_remove_black.setVisibility(View.GONE);
                    refreshTraceBtn();
                    PaCtl.build().closePA(id);
                    mHandler.removeMessages(11);
                    mHandler.sendEmptyMessageDelayed(11, 8000);
                }

                MainActivity.getInstance().closeCustomDialog();
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    public void onSetBlackListRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;

        int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains(devA) ? 0 : 1;
        final TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil();
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_SET_BLACK_UE_LIST || rsp.getCmdType() == GnbProtocol.UI_2_gNB_START_LTE_BLACK_UE_LIST) {
                if (isClickStop){
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    setProgress(type, 0, rsp.getCellId(), "空闲", false);
                    refreshTraceBtn();
                    return;
                }
                if (isResetBlack) return;
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    // 发配置搜寻参数指令
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
                    String ue_max_pwr = traceUtil.getUeMaxTxpwr(rsp.getCellId());
                    final int air_sync = traceUtil.getAirSync(rsp.getCellId());
                    final int ssbBitmap = traceUtil.getSsbBitmap(rsp.getCellId());
                    final int bandwidth = traceUtil.getBandWidth(rsp.getCellId());
                    final int cfr = traceUtil.getCfr(rsp.getCellId());
                    final long cid = traceUtil.getCid(rsp.getCellId());
                    int swap_rf = traceUtil.getSwap_rf(rsp.getCellId());
                    int mob_reject_code = traceUtil.getMobRejectCode(rsp.getCellId());
                    String split_arfcn_dl = traceUtil.getSplit_arfcn_dl(rsp.getCellId());
                    int force_cfg = traceUtil.getForce_cfg(rsp.getCellId());

                    // 第二步，配置频点参数
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.GNB_CFG);

                    setProgress(type, 30, rsp.getCellId(), "配置中", false);
                    if (Integer.parseInt(arfcn[0]) > 100000) PaCtl.build().initPA(id, arfcn[0]);
                    else PaCtl.build().initLtePA(id, arfcn[0]);

                    /*initGnbTrace(int cell_id, int startTac, int maxTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                    	int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                      int band_width, int cfr_enable, int swap_rf)
                    搜寻配置频点参数
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
                            if (iArfcn > 100000) {
                                MessageController.build().initGnbTrace(id, rsp.getCellId(), finalTraceTac, finalMaxTac, plmn, arfcn[0], pci[0], ue_max_pwr,
                                        time_offset, 0, air_sync, "0", 9,
                                        cid, ssbBitmap, bandwidth, cfr, swap_rf, 15, -70, mob_reject_code, split_arfcn_dl, force_cfg);
                            } else {
                                MessageController.build().initGnbLteTrace(id, rsp.getCellId(), finalTraceTac, finalMaxTac, plmn, arfcn[0], pci[0], ue_max_pwr,
                                        time_offset, 0, air_sync, "0", 9,
                                        cid, ssbBitmap, bandwidth, cfr, swap_rf, 15, -70, mob_reject_code, split_arfcn_dl, force_cfg);
                            }
                            int finalTime_offset = time_offset;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setSaveOpLog(rsp.getCellId(), false);
                                    String msg = "plmn = " + plmn + ", arfcn = " + arfcn[0] + ", pci = " + pci[0] + ", cid = " + cid
                                            + ", time offfset = " + finalTime_offset;
                                    OpLog.build().write(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + rsp.getCellId() + "\t\t搜寻参数：" + msg);
                                }
                            }, 300);
                        }
                    }, 300);
                    updateParam(rsp.getCellId(), arfcn[0], pci[0]);
                } else {
                    String s = getCellStr(rsp.getCellId());
                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "通道" + s + "搜寻配置失败");
                    setProgress(type, 30, rsp.getCellId(), "启动失败", true);
                    MainActivity.getInstance().showToast("通道" + s + "搜寻开启失败，请检查配置参数");

                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);

                    //startSecondTrace(id, indexById, rsp.getCellId(), true); //通道搜寻失败，开始下一通道的搜寻流程

                    refreshTraceBtn();
                }
            }
        }
    }

    private String getCellStr(int cell_id) {
        if (cell_id == 0) return "一";
        else if (cell_id == 1) return "二";
        else if (cell_id == 2) return "三";
        else return "四";
    }

    public void onSetGnbRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;

        int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains(devA) ? 0 : 1;
        final TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil();
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_CFG_gNB || rsp.getCmdType() == GnbProtocol.UI_2_eNB_CFG_gNB) { //UI_2_gNB_CFG_gNB = 10 配置频点参数
                if (isClickStop){
                    boolean isIdle = false;
                    if (rsp.getRspValue() == 0){
                        switch (rsp.getCellId()){
                            case 0:
                                isIdle = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getFirstState() == 0;
                                break;
                            case 1:
                                isIdle = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getSecondState() == 0;
                                break;
                            case 2:
                                isIdle = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getThirdState() == 0;
                                break;
                            case 3:
                                isIdle = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getFourthState() == 0;
                                break;
                        }
                    }
                    if (isIdle){
                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                        setProgress(type, 0, rsp.getCellId(), "空闲", false);
                    }
                    refreshTraceBtn();
                    return;
                }
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    if (/*!traceUtil.isTacChange(rsp.getCellId())
                            && */traceUtil.getWorkState(rsp.getCellId()) == GnbBean.State.GNB_CFG) {
                        setProgress(type, 50, rsp.getCellId(), "配置中", false);
                        //第三步.设置功率衰减
                        //MessageController.build().setTraceType(id, GnbProtocol.TraceType.STARTTRACE);
                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.CFG_TRACE);
                        MessageController.build().setTxPwrOffset(id, rsp.getCellId(), Integer.parseInt(traceUtil.getArfcn(rsp.getCellId())), 0);
                    }
                } else {
                    if (rsp.getRspValue() == 10) {
                        setProgress(type, 50, rsp.getCellId(), "授权过期", true);
                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                        refreshTraceBtn();

                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.STOP);
                        traceUtil.setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                        MessageController.build().setCmdAndCellID(id, Integer.parseInt(MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getArfcn(rsp.getCellId())) > 100000 ? GnbProtocol.UI_2_gNB_STOP_CONTROL : GnbProtocol.UI_2_eNB_STOP_CONTROL, rsp.getCellId());

                        MainActivity.getInstance().showRemindDialog("过期提示", "设备使用时间已到期，请联系管理人员");
                        return;
                    }
                    if (rsp.getRspValue() == 6) {
                        // 空口失败，继续跑下一个频点
                        airFailGoNext(id, rsp.getCellId());
                        return;
                    }

                    String s = getCellStr(rsp.getCellId());
                    if ((rsp.getRspValue() == 2 || rsp.getRspValue() == 3 || rsp.getRspValue() == 5) &&
                            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getWorkState(rsp.getCellId()) == GnbBean.State.CONTROL) {
                        //MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "通道" + s + "TAC循环配置失败");
                        return;
                    }

                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "通道" + s + "搜寻配置失败 err_code:" + rsp.getRspValue());
                    setProgress(type, 50, rsp.getCellId(), "启动失败", true);
                    MainActivity.getInstance().showToast("通道" + s + (rsp.getRspValue() == 7 ? "GPS 未锁定" : "搜寻开启失败，请检查配置参数"));
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);

                    //startSecondTrace(id, indexById, rsp.getCellId(), true); //通道搜寻失败，开始下一通道的搜寻流程

                    refreshTraceBtn();
                    if (rsp.getRspValue() == 5) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.STOP);
                                traceUtil.setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                                MessageController.build().setCmdAndCellID(id, Integer.parseInt(MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getArfcn(rsp.getCellId())) > 100000 ? GnbProtocol.UI_2_gNB_STOP_CONTROL : GnbProtocol.UI_2_eNB_STOP_CONTROL, rsp.getCellId());
                            }
                        }, 500);
                    }
                }
            }
        }
    }

    private void airFailGoNext(String id, int cell_id){
        LinkedList<ArfcnPciBean> list;
        String key = "TD" + (cell_id + 1); // arfcnBeanHashMap的 key值

        String arfcn = "-1";
        String pci = "0";
        // 索引先不加加，让第一个多跑一次
        list = arfcnBeanHashMap.get(key);
        if (list != null){
            int size = list.size();
            if (autoArfcnIndex[cell_id] < size) {
                arfcn = list.get(autoArfcnIndex[cell_id]).getArfcn();
                pci = list.get(autoArfcnIndex[cell_id]).getPci();
            }
            autoArfcnIndex[cell_id]++;
            AppLog.D("cell " + cell_id + " sync fail reset arfcn = " + arfcn);
            int force_cfg = 0;
            if (arfcn.equals("-1")) {
                if (autoArfcnIndex[cell_id] >= size) autoArfcnIndex[cell_id] = 0;
                arfcn = list.get(autoArfcnIndex[cell_id]).getArfcn();
                pci = list.get(autoArfcnIndex[cell_id]).getPci();
                force_cfg = 1;
            }
            updateParamAndStart(id, arfcn, pci, force_cfg, cell_id);
        }
    }

    private void updateParamAndStart(String id, String arfcn, String pci, int force_cfg, int cell_id) {
        int band = NrBand.earfcn2band(Integer.parseInt(arfcn));
        mTraceUtilNr.setArfcn(cell_id, arfcn);
        mTraceUtilNr.setPci(cell_id, pci);
        mTraceUtilNr.setForce_cfg(cell_id, force_cfg);
        switch (cell_id) {
            case 0:
                mTraceUtilNr.setPlmn(GnbProtocol.CellId.FIRST, band == 78 ? "46001" : "46000");
                if (PaCtl.build().isB97502) { // N41/N78/N79
                    mTraceUtilNr.setBandWidth(GnbProtocol.CellId.FIRST, 100);
                    mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.FIRST, 255);
                } else { // N28/N78/N79
                    mTraceUtilNr.setBandWidth(GnbProtocol.CellId.FIRST, band == 28 ? 20 : 100);
                    mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.FIRST, band == 28 ? 240 : 255);
                }
                break;
            case 1:
                mTraceUtilNr.setBandWidth(GnbProtocol.CellId.SECOND, 5);
                mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.SECOND, 128);
                if (PaCtl.build().isB97502) { // B3/B5/B8/B40
                    int freq = LteBand.earfcn2freq(Integer.parseInt(arfcn));
                    if ((freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949))
                        mTraceUtilNr.setPlmn(GnbProtocol.CellId.SECOND, "46000");
                    else mTraceUtilNr.setPlmn(GnbProtocol.CellId.SECOND, "46001");
                } else { // B34/B39/B40/B41
                    mTraceUtilNr.setPlmn(GnbProtocol.CellId.SECOND, "46000");
                }
                break;
            case 2:
                mTraceUtilNr.setPlmn(GnbProtocol.CellId.THIRD, band == 1 ? "46001" : "46000");
                if (PaCtl.build().isB97502) { // // N1/N28
                    mTraceUtilNr.setBandWidth(GnbProtocol.CellId.THIRD, 20);
                    mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.THIRD, 240);
                } else { // N1/N41
                    mTraceUtilNr.setBandWidth(GnbProtocol.CellId.THIRD, band == 1 ? 20 : 100);
                    mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.THIRD, band == 1 ? 240 : 255);
                }
                break;
            case 3:
                mTraceUtilNr.setBandWidth(GnbProtocol.CellId.FOURTH, 5);
                mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.FOURTH, 128);

                if (PaCtl.build().isB97502 && LteBand.earfcn2band(Integer.parseInt(arfcn)) != 3) {
                    mTraceUtilNr.setPlmn(GnbProtocol.CellId.FOURTH, "46000");
                } else {
                    int freq = LteBand.earfcn2freq(Integer.parseInt(arfcn));
                    if ((freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949))
                        mTraceUtilNr.setPlmn(GnbProtocol.CellId.FOURTH, "46000");
                    else mTraceUtilNr.setPlmn(GnbProtocol.CellId.FOURTH, "46001");
                }
                break;
        }

        MainActivity.getInstance().getDeviceList().get(0).setTraceUtil(mTraceUtilNr);

        //发送配置黑名单指令
        sendBlackList(id, Integer.parseInt(arfcn), cell_id);
    }

    private void sendAllCellBlackList(String id) {
        final List<UeidBean> blackList = new ArrayList<>();
        List<MyUeidBean> myUeidBeanList = MainActivity.getInstance().getBlackList();
        for (int j = myUeidBeanList.size() - 1; j >= 0; j--){
            if (blackList.size() >= 40) break;
            blackList.add(myUeidBeanList.get(j).getUeidBean());
        }
        isResetBlack = true;
        MessageController.build().setBlackList(id, false, GnbProtocol.CellId.FIRST, blackList.size(), blackList);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isResetBlack = true;
                MessageController.build().setBlackList(id, true, GnbProtocol.CellId.SECOND, blackList.size(), blackList);
            }
        }, 300);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isResetBlack = true;
                MessageController.build().setBlackList(id, false, GnbProtocol.CellId.THIRD, blackList.size(), blackList);
            }
        }, 600);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isResetBlack = true;
                MessageController.build().setBlackList(id, true, GnbProtocol.CellId.FOURTH, blackList.size(), blackList);
            }
        }, 900);
    }

    private void sendBlackList(String id, int arfcn, int cellId) {
        final List<UeidBean> blackList = new ArrayList<>();
        List<MyUeidBean> myUeidBeanList = MainActivity.getInstance().getBlackList();
        for (int j = myUeidBeanList.size() - 1; j >= 0; j--){
            if (blackList.size() >= 40) break;
            blackList.add(myUeidBeanList.get(j).getUeidBean());
        }
        MessageController.build().setBlackList(id, arfcn < 100000, cellId, blackList.size(), blackList);
    }

    public void onSetTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;

        int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains(devA) ? 0 : 1;
        final TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil();
        if (rsp != null) {
            //int traceType = MessageController.build().getTraceType(id);
            int traceType = traceUtil.getWorkState(rsp.getCellId());
            AppLog.I("onSetTxPwrOffsetRsp get TraceType = " + traceType);
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_SET_TX_POWER_OFFSET) {
                if (isClickStop){
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    setProgress(type, 0, rsp.getCellId(), "空闲", false);
                    refreshTraceBtn();
                    return;
                }
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    if (traceType == GnbBean.State.CFG_TRACE) {
                        //4、开pa
                        if (Integer.parseInt(traceUtil.getArfcn(rsp.getCellId())) > 100000)
                            PaCtl.build().openPA(id, traceUtil.getArfcn(rsp.getCellId())); // 开PA
                        else
                            PaCtl.build().openLtePA(id, traceUtil.getArfcn(rsp.getCellId())); // 开Lte PA
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 5、发开始搜寻指令
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setRsrp(rsp.getCellId(), 0);
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setTacChange(rsp.getCellId(), true);
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setEnable(rsp.getCellId(), true);
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                                //第四步.启动搜寻
                                setProgress(type, 70, rsp.getCellId(), "配置中", false);

                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.CFG_TRACE);
                                if (Integer.parseInt(traceUtil.getArfcn(rsp.getCellId())) > 100000) {
                                    MessageController.build().startControl(id, rsp.getCellId(), 3, 1);
                                } else {
                                    MessageController.build().startLteControl(id, rsp.getCellId(), 3, 1);
                                }
                            }
                        }, 300);
                    }
                } else {
                    String s = getCellStr(rsp.getCellId());
                    if (traceType == GnbBean.State.CFG_TRACE) {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "通道" + s + "搜寻配置失败");
                        setProgress(type, 70, rsp.getCellId(), "启动失败", true);
                        MainActivity.getInstance().showToast("通道" + s + "搜寻开启失败，请检查配置参数");

                        //startSecondTrace(id, indexById, rsp.getCellId(), true); //通道搜寻失败，开始下一通道的搜寻流程
                        refreshTraceBtn();
                    } else {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "通道" + s + "增益调整失败");
                        MainActivity.getInstance().showToast("增益调整失败");
                        switch_tx.setChecked(!switch_tx.isChecked());
                    }
                }
            }
        }
    }

    boolean isStartCatchHandler = false;

    public void refreshTraceBtn() {

        List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();

        if (deviceList.size() <= 0) { // 无设备
            AppLog.I("refreshTraceBtn deviceList size = 0");
            tv_do_btn.setText("开启");
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
            return;
        }
        if (deviceList.size() == 1) { // 一个设备
            TraceUtil traceUtil = deviceList.get(0).getTraceUtil();

            int workStateFirst = traceUtil.getWorkState(GnbProtocol.CellId.FIRST); // 通道一
            int workStateSecond = traceUtil.getWorkState(GnbProtocol.CellId.SECOND); // 通道二
            int workStateThird = traceUtil.getWorkState(GnbProtocol.CellId.THIRD); // 通道三
            int workStateFourth = traceUtil.getWorkState(GnbProtocol.CellId.FOURTH); // 通道四

            AppLog.I("refreshTraceBtn deviceList size = 1, workStateFirst = " + workStateFirst
                    + ", workStateSecond = " + workStateSecond + ", workStateThird = " + workStateThird + ", workStateFourth = " + workStateFourth);

            if (workStateFirst == GnbBean.State.CONTROL || workStateSecond == GnbBean.State.CONTROL || workStateThird == GnbBean.State.CONTROL || workStateFourth == GnbBean.State.CONTROL) {
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.CONTROL);
                tv_do_btn.setText("结束");
            } else if (workStateFirst == GnbBean.State.STOP || workStateSecond == GnbBean.State.STOP || workStateThird == GnbBean.State.STOP || workStateFourth == GnbBean.State.STOP) {
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.STOP);
                tv_do_btn.setText("结束中");
            } else if (workStateFirst == GnbBean.State.IDLE && workStateSecond == GnbBean.State.IDLE && workStateThird == GnbBean.State.IDLE && workStateFourth == GnbBean.State.IDLE) {
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.IDLE);
                tv_do_btn.setText("开启");
            } else {
                tv_do_btn.setText("取消");
            }
            if (getBtnStr().contains("开启")){
                tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
                switch_tx.setChecked(false);
                if (getBtnStr().equals("开启")) {
                    initParam();
                    mShowChildFragment.initRsrp();
                }
            } else {
                tv_do_btn.setBackgroundResource(R.drawable.btn_bg_delete);
            }
            return;
        }

        // 两个设备
        TraceUtil traceUtil0 = deviceList.get(0).getTraceUtil(); // 设备1
        int workStateFirst0 = traceUtil0.getWorkState(GnbProtocol.CellId.FIRST);  // 设备1  通道一
        int workStateSecond0 = traceUtil0.getWorkState(GnbProtocol.CellId.SECOND); // 设备1 通道二

        TraceUtil traceUtil1 = deviceList.get(1).getTraceUtil(); // 设备2
        int workStateFirst1 = traceUtil1.getWorkState(GnbProtocol.CellId.FIRST);  // 设备2  通道一
        int workStateSecond1 = traceUtil1.getWorkState(GnbProtocol.CellId.SECOND); // 设备2 通道二

        AppLog.I("refreshTraceBtn deviceList size = 2, workStateFirst0 = " + workStateFirst0 + ", workStateSecond0 = " + workStateSecond0
                + ", workStateFirst1 = " + workStateFirst1 + ", workStateSecond1 = " + workStateSecond1);

        if (workStateFirst0 == GnbBean.State.IDLE && (deviceList.get(0).getRsp().getDualCell() == 1 || workStateSecond0 == GnbBean.State.IDLE) && workStateFirst1 == GnbBean.State.IDLE && (deviceList.get(1).getRsp().getDualCell() == 1 || workStateSecond1 == GnbBean.State.IDLE)) {
            MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.IDLE);
            MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.IDLE);
            tv_do_btn.setText("开启");
        } else if (workStateFirst0 == GnbBean.State.STOP || workStateSecond0 == GnbBean.State.STOP || workStateFirst1 == GnbBean.State.STOP || workStateSecond1 == GnbBean.State.STOP) {
            if (workStateFirst1 == GnbBean.State.STOP || workStateSecond1 == GnbBean.State.STOP) {
                MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.STOP);
            } else
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.STOP);
            tv_do_btn.setText("结束中");
        } else if (workStateFirst0 == GnbBean.State.CONTROL || workStateSecond0 == GnbBean.State.CONTROL) {
            MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.CONTROL);
            tv_do_btn.setText("结束");
            if (workStateFirst1 == GnbBean.State.CONTROL || workStateSecond1 == GnbBean.State.CONTROL) {
                MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.CONTROL);
            } else if (workStateFirst1 == GnbBean.State.CATCH || workStateSecond1 == GnbBean.State.CATCH) {
                MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.CATCH);
                tv_do_btn.setText("启/停");
            }
        } else if (workStateFirst1 == GnbBean.State.CONTROL || workStateSecond1 == GnbBean.State.CONTROL) {
            MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.CONTROL);
            tv_do_btn.setText("结束");
            if (workStateFirst0 == GnbBean.State.CATCH || workStateSecond0 == GnbBean.State.CATCH) {
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.CATCH);
                tv_do_btn.setText("启/停");
            }
        } else if (workStateFirst0 == GnbBean.State.TRACE || workStateSecond0 == GnbBean.State.TRACE) {
            MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.TRACE);
            tv_do_btn.setText("结束");
            if (workStateFirst1 == GnbBean.State.TRACE || workStateSecond1 == GnbBean.State.TRACE) {
                MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.TRACE);
            } else if (workStateFirst1 == GnbBean.State.CATCH || workStateSecond1 == GnbBean.State.CATCH) {
                MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.CATCH);
                tv_do_btn.setText("启/停");
            }
        } else if (workStateFirst1 == GnbBean.State.TRACE || workStateSecond1 == GnbBean.State.TRACE) {
            MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.TRACE);
            tv_do_btn.setText("结束");
            if (workStateFirst0 == GnbBean.State.CATCH || workStateSecond0 == GnbBean.State.CATCH) {
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.CATCH);
                tv_do_btn.setText("启/停");
            }
        } else if ((workStateFirst0 == GnbBean.State.CATCH || workStateSecond0 == GnbBean.State.CATCH)
                && (workStateFirst1 == GnbBean.State.CATCH || workStateSecond1 == GnbBean.State.CATCH)) {
            MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.CATCH);
            MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.CATCH);

            tv_do_btn.setText("结束");
        } else if (workStateFirst0 == GnbBean.State.CATCH || workStateSecond0 == GnbBean.State.CATCH) {
            MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.CATCH);
            if (workStateFirst1 == GnbBean.State.IDLE || workStateSecond1 == GnbBean.State.IDLE) {
                MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.IDLE);

                tv_do_btn.setText("启/停");
            } else {
                MainActivity.getInstance().getDeviceList().get(1).setWorkState(workStateFirst1);

                tv_do_btn.setText("结束");
            }
        } else if (workStateFirst1 == GnbBean.State.CATCH || workStateSecond1 == GnbBean.State.CATCH) {
            MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.CATCH);
            if (workStateFirst0 == GnbBean.State.IDLE || workStateSecond0 == GnbBean.State.IDLE) {
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.IDLE);

                tv_do_btn.setText("启/停");
            } else {
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(workStateFirst0);

                tv_do_btn.setText("结束");
            }
        } else {

            tv_do_btn.setText("取消");
        }
        if (getBtnStr().contains("开启")) {
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
            switch_tx.setChecked(false);
        } else {
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_delete);
        }
    }

    int[] autoArfcnIndex = {0, 0, 0, 0};
    boolean[] autoRun = {false, false, false, false};

    private void handleRunAuto(int cell_id){
        if (MainActivity.getInstance().getDeviceList().size() > 0 && mImsiBean == null && autoArfcnTime != 0
                && MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getWorkState(cell_id) == GnbBean.State.CONTROL) {
            LinkedList<ArfcnPciBean> arfcnList = arfcnBeanHashMap.get("TD" + (cell_id + 1));
            if (arfcnList == null || arfcnList.size() == 1) return;
            for (int i = 0; i < arfcnList.size(); i++) {
                if (i >= autoArfcnIndex[cell_id]) {
                    String arfcn = arfcnList.get(i).getArfcn();
                    String pci = arfcnList.get(i).getPci();
                    boolean isReturn = false;
                    if (cell_id == 0){
                        if (NrBand.earfcn2band(Integer.parseInt(arfcn)) == 79 && !bandRunOneMap.get("N79")) isReturn = true;
                    }else if (cell_id == 1){
                        int band = LteBand.earfcn2band(Integer.parseInt(arfcn));
                        if (PaCtl.build().isB97502){
                            if ((band == 5 && !bandRunOneMap.get("B5")) || (band == 8 && !bandRunOneMap.get("B8"))) isReturn = true;
                        }else {
                            if (band == 34 && !bandRunOneMap.get("B34")) isReturn = true;
                        }
                    }else if (cell_id == 3){
                        int band = LteBand.earfcn2band(Integer.parseInt(arfcn));
                        if (!PaCtl.build().isB97502){
                            if ((band == 5 && !bandRunOneMap.get("B5")) || (band == 8 && !bandRunOneMap.get("B8"))) isReturn = true;
                        }else {
                            if (band == 34 && !bandRunOneMap.get("B34")) isReturn = true;
                        }
                    }
                    if (isReturn){
                        autoArfcnIndex[cell_id]++;
                        if (autoArfcnIndex[cell_id] >= arfcnList.size()) autoArfcnIndex[cell_id] = 0;
                        handleRunAuto(cell_id);
                        return;
                    }
                    String id = MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId();
                    boolean isSame = (cell_id == 1 || cell_id == 3) ?
                            LteBand.earfcn2band(Integer.parseInt(arfcn)) == LteBand.earfcn2band(Integer.parseInt(mTraceUtilNr.getArfcn(cell_id))) :
                            NrBand.earfcn2band(Integer.parseInt(arfcn)) == NrBand.earfcn2band(Integer.parseInt(mTraceUtilNr.getArfcn(cell_id))); // b97502 的B1/N1会直接不同NrBand解析的B1为0，没必要再用LteBand来判断
                    if (isSame) {
                        if (cell_id == 3){
                            int freqValue = LteBand.earfcn2freq(Integer.parseInt(arfcn));
                            int freqArfcn = LteBand.earfcn2freq(Integer.parseInt(mTraceUtilNr.getArfcn(cell_id)));
                            // 同频段，需对B3和B8不同的运营商做判断
                            if (PaCtl.build().isB97502 || (((freqArfcn > 1709 && freqArfcn < 1735) || (freqArfcn > 1804 && freqArfcn < 1830)) && ((freqValue > 1709 && freqValue < 1735) || (freqValue > 1804 && freqValue < 1830)))
                                    || (((freqArfcn > 888 && freqArfcn < 904) || (freqArfcn > 933 && freqArfcn < 949)) && ((freqValue > 888 && freqValue < 904) || (freqValue > 933 && freqValue < 949)))) {
                                AppLog.D("mHandler 3 change arfcn = " + arfcn + ", pci = " + pci + ", autoArfcnIndex = " + autoArfcnIndex[cell_id]);
                                updateParam(cell_id, arfcn, pci);
                                MessageController.build().setArfcn(id, 3, arfcn);
                                MessageController.build().setPci(id, 3, pci);
                            } else {
                                AppLog.D("mHandler 3 change need stop, arfcn = " + arfcn + ", autoArfcnIndex = " + autoArfcnIndex[cell_id]);
                                autoRun[cell_id] = true;
                                MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_eNB_STOP_CONTROL, cell_id);
                                PaCtl.build().closePAByArfcn(id, true, mTraceUtilNr.getArfcn(cell_id));
                                MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setArfcn(cell_id, arfcn);
                                MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setPci(cell_id, pci);
                            }
                        }else if (cell_id == 1){
                            int freqValue = LteBand.earfcn2freq(Integer.parseInt(arfcn));
                            int freqArfcn = LteBand.earfcn2freq(Integer.parseInt(mTraceUtilNr.getArfcn(cell_id)));
                            // 同频段，需对B3和B8不同的运营商做判断
                            if (!PaCtl.build().isB97502 || (((freqArfcn > 1709 && freqArfcn < 1735) || (freqArfcn > 1804 && freqArfcn < 1830)) && (freqValue > 1709 && freqValue < 1735) || (freqValue > 1804 && freqValue < 1830)
                                    || ((freqArfcn > 888 && freqArfcn < 904) || (freqArfcn > 933 && freqArfcn < 949)) && (freqValue > 888 && freqValue < 904) || (freqValue > 933 && freqValue < 949))) {
                                AppLog.D("mHandler " + cell_id + " change arfcn = " + arfcn + ", pci = " + pci + ", autoArfcnIndex = " + autoArfcnIndex[cell_id]);
                                updateParam(cell_id, arfcn, pci);
                                MessageController.build().setArfcn(id, 1, arfcn);
                                MessageController.build().setPci(id, 1, pci);
                            } else {
                                AppLog.D("mHandler 1 change need stop, arfcn = " + arfcn + ", autoArfcnIndex = " + autoArfcnIndex[cell_id]);
                                autoRun[cell_id] = true;
                                MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_eNB_STOP_CONTROL, cell_id);
                                PaCtl.build().closePAByArfcn(id, true, mTraceUtilNr.getArfcn(cell_id));
                                MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setArfcn(cell_id, arfcn);
                                MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setPci(cell_id, pci);
                            }
                        }else {
                            // 同频段，直接切换
                            AppLog.D("mHandler " + cell_id + " change arfcn = " + arfcn + ", pci = " + pci + ", autoArfcnIndex = " + autoArfcnIndex[cell_id]);
                            updateParam(cell_id, arfcn, pci);
                            MessageController.build().setArfcn(id, cell_id, arfcn);
                            MessageController.build().setPci(id, cell_id, pci);
                        }
                        autoArfcnIndex[cell_id]++;
                        if (autoArfcnIndex[cell_id] >= arfcnList.size()) autoArfcnIndex[cell_id] = 0;
                    } else {
                        AppLog.D("mHandler " + cell_id + " change arfcn = " + arfcn + ", pci = " + pci + " is different band, stop at first" + ", autoArfcnIndex = " + autoArfcnIndex[cell_id]);
                        autoRun[cell_id] = true;
                        String deviceId = MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId();
                        MessageController.build().setCmdAndCellID(deviceId, (cell_id == 0 || cell_id == 2) ? GnbProtocol.UI_2_gNB_STOP_CONTROL : GnbProtocol.UI_2_eNB_STOP_CONTROL, cell_id);
                        PaCtl.build().closePAByArfcn(deviceId, cell_id == 1 || cell_id == 3, mTraceUtilNr.getArfcn(cell_id));
                        MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setArfcn(cell_id, arfcn);
                        MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setPci(cell_id, pci);
                        autoArfcnIndex[cell_id]++;
                        if (autoArfcnIndex[cell_id] >= arfcnList.size()) autoArfcnIndex[cell_id] = 0;
                        return;
                    }
                    break;
                }
            }
        }
        if (autoArfcnTime == 0) mHandler.sendEmptyMessageDelayed(cell_id, 10 * 1000L);
        else mHandler.sendEmptyMessageDelayed(cell_id, autoArfcnTime * 1000L);
    }
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                case 1:
                case 2:
                case 3:
                    handleRunAuto(msg.what);
                    break;
                case 9:
                    isStartCatchHandler = false;
                    break;
                case 11:
                    if (MainActivity.getInstance().getDeviceList().size() == 0) return;
                    boolean isAllStop = true;
                    if (MainActivity.getInstance().getDeviceList().get(0).getRsp().getFirstState() != 0) isAllStop = false;
                    else {
                        MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setWorkState(0, GnbBean.State.IDLE);
                        setProgress(0, 0, 0, "空闲", false);
                    }
                    if (MainActivity.getInstance().getDeviceList().get(0).getRsp().getSecondState() != 0) isAllStop = false;
                    else {
                        MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setWorkState(1, GnbBean.State.IDLE);
                        setProgress(0, 0, 1, "空闲", false);
                    }
                    if (MainActivity.getInstance().getDeviceList().get(0).getRsp().getThirdState() != 0) isAllStop = false;
                    else {
                        MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setWorkState(2, GnbBean.State.IDLE);
                        setProgress(0, 0, 2, "空闲", false);
                    }
                    if (MainActivity.getInstance().getDeviceList().get(0).getRsp().getFourthState() != 0) isAllStop = false;
                    else {
                        MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setWorkState(3, GnbBean.State.IDLE);
                        setProgress(0, 0, 3, "空闲", false);
                    }
                    refreshTraceBtn();
                    if (!isAllStop) mHandler.sendEmptyMessageDelayed(11, 3000);
                    break;
                // 10秒无上号数据逻辑4 走到此表示连续10s内无上号，那么切换下一个频点轮巡,且后续不再参与轮巡
                case 79:
                    stopBandRunOne("N79", 0, arfcnBeanHashMap.get("TD1"));
                    break;
                case 1034:
                    stopBandRunOne("B34", PaCtl.build().isB97502 ? 3 : 1, PaCtl.build().isB97502 ? arfcnBeanHashMap.get("TD4") : arfcnBeanHashMap.get("TD2"));
                    break;
                case 1005:
                    stopBandRunOne("B5", PaCtl.build().isB97502 ? 1 : 3, PaCtl.build().isB97502 ? arfcnBeanHashMap.get("TD2") : arfcnBeanHashMap.get("TD4"));
                    break;
                case 1008:
                    stopBandRunOne("B8", PaCtl.build().isB97502 ? 1 : 3, PaCtl.build().isB97502 ? arfcnBeanHashMap.get("TD2") : arfcnBeanHashMap.get("TD4"));
                    break;
            }
        }
    };

    boolean isDataUpFlag = false;
    String nowWriteFileName = "1_data.txt";
    String newCreateFileName = "";
    @SuppressLint("HandlerLeak")
    private final Handler mDataUpHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!MainActivity.getInstance().isUpFTP) return;
            if (msg.what == 0) {
                if (newCreateFileName.isEmpty()){
                    int indexFile = Integer.parseInt(nowWriteFileName.substring(0, 1)) + 1;
                    newCreateFileName = indexFile + "_data.txt";
                    new File(FileProtocol.DIR_UP_DATA, newCreateFileName);
                    AppLog.D("创建新文件：" + newCreateFileName);
                }
                isDataUpFlag = true;
            } else if (msg.what == 1) {
                String oldFileName = Integer.parseInt(nowWriteFileName.substring(0, 1)) - 1 + "_data.txt";
                DataUpBean mDataUpBean = MainActivity.getInstance().mDataUpBean;
                String newFileName = "EVT-" + mDataUpBean.getSubCode() + "-" + mDataUpBean.getFacCode() + "-" + System.currentTimeMillis() / 1000 + ".bcp";
                boolean to = new File(FileProtocol.DIR_UP_DATA, oldFileName).renameTo(new File(FileProtocol.DIR_UP_DATA, newFileName));
                AppLog.D("修改文件名：" + newFileName + ", result = " + to);
                new Thread(new Runnable() {
                    public void run() {
                        SftpUploader.uploadFilesToServer(new SftpUploader.UpDataListener() {
                            @Override
                            public void onUpDataListener(String result) {
                                AppLog.D("上传result：" + result);
                                if (result.equals("finish")) {
                                    AppLog.D("SFTP上传完成");
                                    if (getBtnStr().equals("结束"))
                                        mDataUpHandler.sendEmptyMessageDelayed(0, MainActivity.getInstance().mDataUpBean.getUpCycle() * 1000);
                                    return;
                                }
                                MainActivity.getInstance().addDataUaResult(result);
                            }
                        });
                    }
                }).start();
            }
        }
    };

    private void stopBandRunOne(String band, int cell_id, LinkedList<ArfcnPciBean> td){
        bandRunOneMap.put(band, false);
        bandRunOneHandleMap.remove(cell_id);
        if (autoArfcnIndex[cell_id] >= td.size()) autoArfcnIndex[cell_id] = 0;
        AppLog.D("mHandler " + band + " no data in 10s, stop this and start next" + ", autoArfcnIndex = " + autoArfcnIndex[cell_id]);
        autoRun[cell_id] = true;
        String deviceId = MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId();
        MessageController.build().setCmdAndCellID(deviceId, cell_id == 0 ? GnbProtocol.UI_2_gNB_STOP_CONTROL : GnbProtocol.UI_2_eNB_STOP_CONTROL, cell_id);
        PaCtl.build().closePAByArfcn(deviceId, cell_id != 0, MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getArfcn(cell_id));
        String arfcn = td.get(autoArfcnIndex[cell_id]).getArfcn();
        String pci = td.get(autoArfcnIndex[cell_id]).getPci();
        MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setArfcn(cell_id, arfcn);
        MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setPci(cell_id, pci);
        autoArfcnIndex[cell_id]++;
        if (autoArfcnIndex[cell_id] >= td.size()) autoArfcnIndex[cell_id] = 0;
    }

    private void freshDoWorkState(int type, int cell_id, String imsi, String arfcn, String pci) {

        String s = "通道" + getCellStr(cell_id) + "搜寻中";

        MainActivity.getInstance().updateSteps(type, StepBean.State.success, s);
        setProgress(type, 100, cell_id, "搜寻中", false);
    }

    public List<ImsiBean> getDataList() {
        return mAllImsiList;
    }

    private void initParam() {
        MainActivity.getInstance().initParam();
    }

    private void updateParam(int cell_id, String arfcn, String pci) {
        MainActivity.getInstance().updateParam(cell_id, arfcn, pci);
    }

    boolean isInFreqForPci = false;
    private final HashMap<String, Boolean> bandRunOneMap = new HashMap<>(); // 标志是否N79/B34/B5/B8 连续10s内无上号，则切换下一个频点轮巡,且后续不再参与轮巡
    private final HashMap<Integer/*通道*/, Integer/*Handle消息号*/> bandRunOneHandleMap = new HashMap<>(); // 记录是否N79/B34/B5/B8 连续10s内无上号定时线程号

    @Override
    public void onTraceConfig() {
        for (int i = 0; i < 4; i++){
            // 跳过列表第一个数据，取消第一个频点多一次轮询的机会
            autoArfcnIndex[i] = 1;
            autoRun[i] = false;
        }
        isFirstStart = true;
        // 10秒无上号数据逻辑1   启动前恢复N79/B34/B5/B8参与轮巡
        bandRunOneMap.put("N79", true);
        bandRunOneMap.put("B34", true);
        bandRunOneMap.put("B5", true);
        bandRunOneMap.put("B8", true);
        bandRunOneHandleMap.clear();

        mHandler.removeCallbacksAndMessages(null);

        txValue = 0;
        mCatchChildFragment.restartCatch();

        mImsiList.clear();
        mAllImsiList.clear();
        mCatchChildFragment.resetShowData(null);
        all_count = 0;
        unicom_count = 0;
        mobile_count = 0;
        telecom_count = 0;
        sva_count = 0;
        mCatchChildFragment.setCatchCount(all_count, telecom_count, mobile_count, unicom_count, sva_count);
        isClickStop = false;

        if (!isAutoRun && MainActivity.getInstance().isEnablePci){
            List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
            if (deviceList.size() <= 0) {
                MainActivity.getInstance().showToast("设备不在线，请先开启并连接设备");
                return;
            }
            MainActivity.getInstance().freqList.clear();
            isInFreqForPci = true;
            index = 0;
            indexTD = 0;
            tv_do_btn.setText(getString(R.string.cancel));
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_delete);
            setProgress(0, 20, 0, "检测中", false);
            setProgress(0, 20, 1, "检测中", false);
            setProgress(0, 20, 2, "检测中", false);
            setProgress(0, 20, 3, "检测中", false);
            freqScan(MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId());
            return;
        }

        goSetParamAndRun();
    }

    int index = 0;
    int indexTD = 0;
    private void freqScan(String id) {
        boolean isNotStart = true;
        switch (indexTD){
            case 0:
                for (int i = 0; i < arfcnBeanHashMap.get("TD1").size(); i++){
                    if (i == index){
                        isNotStart = false;
                        startFreqScan(id, arfcnBeanHashMap.get("TD1").get(i).getArfcn());
                        index++;
                        break;
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
                for (int i = 0; i < arfcnBeanHashMap.get("TD2").size(); i++){
                    if (i == index){
                        isNotStart = false;
                        startFreqScan(id, arfcnBeanHashMap.get("TD2").get(i).getArfcn());
                        index++;
                        break;
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
                for (int i = 0; i < arfcnBeanHashMap.get("TD3").size(); i++){
                    if (i == index){
                        isNotStart = false;
                        startFreqScan(id, arfcnBeanHashMap.get("TD3").get(i).getArfcn());
                        index++;
                        break;
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
                for (int i = 0; i < arfcnBeanHashMap.get("TD4").size(); i++){
                    if (i == index){
                        isNotStart = false;
                        startFreqScan(id, arfcnBeanHashMap.get("TD4").get(i).getArfcn());
                        index++;
                        break;
                    }
                }
                break;
        }

        if (isNotStart) {
            isInFreqForPci = false;
            LinkedList<LinkedList<ArfcnPciBean>> linkedLists = FreqUtil.build().decFreqList(MainActivity.getInstance().freqList);
            LinkedList<ArfcnPciBean> TD1 = new LinkedList<>(linkedLists.get(0));
            LinkedList<ArfcnPciBean> TD2 = new LinkedList<>(linkedLists.get(1));
            LinkedList<ArfcnPciBean> TD3 = new LinkedList<>(linkedLists.get(2));
            LinkedList<ArfcnPciBean> TD4 = new LinkedList<>(linkedLists.get(3));
            for (int i = 0; i < arfcnBeanHashMap.get("TD1").size(); i++){
                for (ArfcnPciBean bean : TD1) {
                    if (bean.getArfcn().equals(arfcnBeanHashMap.get("TD1").get(i).getArfcn())){
                        arfcnBeanHashMap.get("TD1").get(i).setPci(bean.getPci());
                        break;
                    }
                }
            }

            for (int i = 0; i < arfcnBeanHashMap.get("TD2").size(); i++){
                for (ArfcnPciBean bean : TD2) {
                    if (bean.getArfcn().equals(arfcnBeanHashMap.get("TD2").get(i).getArfcn())){
                        arfcnBeanHashMap.get("TD2").get(i).setPci(bean.getPci());
                        break;
                    }
                }
            }

            for (int i = 0; i < arfcnBeanHashMap.get("TD3").size(); i++){
                for (ArfcnPciBean bean : TD3) {
                    if (bean.getArfcn().equals(arfcnBeanHashMap.get("TD3").get(i).getArfcn())){
                        arfcnBeanHashMap.get("TD3").get(i).setPci(bean.getPci());
                        break;
                    }
                }
            }

            for (int i = 0; i < arfcnBeanHashMap.get("TD4").size(); i++){
                for (ArfcnPciBean bean : TD4) {
                    if (bean.getArfcn().equals(arfcnBeanHashMap.get("TD4").get(i).getArfcn())){
                        arfcnBeanHashMap.get("TD4").get(i).setPci(bean.getPci());
                        break;
                    }
                }
            }
            goSetParamAndRun();
        }
    }

    private void goSetParamAndRun() {
        int type = 0;
        MainActivity.getInstance().updateSteps(type, StepBean.State.success, "开始启动搜寻，配置中");

        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
            PaCtl.build().closePA(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId());
            String name = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName();
            boolean isLte = name.contains(devB);

            //boolean isGpsSync = MainActivity.getInstance().getDeviceList().get(i).getRsp().getGpsSyncState() == GnbStateRsp.Gps.SUCC;
            boolean isAirSync = PrefUtil.build().getValue("sync_mode", "空口").toString().equals("空口");
            int enableAir = isAirSync ? 1 : 0;
            // 通道一
            boolean runCell0 = false;
            boolean runCell1 = false;
            boolean runCell2 = false;
            boolean runCell3 = false;
            String arfcn_cfg = "";
            String pci_cfg = "";
            LinkedList<ArfcnPciBean> td1 = arfcnBeanHashMap.get("TD1");
            if (td1 != null && td1.size() > 0) {
                arfcn_cfg = td1.get(0).getArfcn();
                pci_cfg = td1.get(0).getPci();
            }
            mTraceUtilNr.setAirSync(GnbProtocol.CellId.FIRST, enableAir);
            int band = 0;
            if (!arfcn_cfg.isEmpty()) {
                band = NrBand.earfcn2band(Integer.parseInt(arfcn_cfg));
                mTraceUtilNr.setBandWidth(GnbProtocol.CellId.FIRST, band == 28 ? 20 : 100);
                mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.FIRST, band == 28 ? 240 : 255);
                mTraceUtilNr.setPlmn(GnbProtocol.CellId.FIRST, band == 78 ? "46011" : "46015");
                runCell0 = true;
            }
            mTraceUtilNr.setArfcn(GnbProtocol.CellId.FIRST, arfcn_cfg);
            mTraceUtilNr.setPci(GnbProtocol.CellId.FIRST, pci_cfg);

            // 通道二
            arfcn_cfg = "";
            pci_cfg = "";
            LinkedList<ArfcnPciBean> td2 = arfcnBeanHashMap.get("TD2");
            if (td2 != null && td2.size() > 0) {
                arfcn_cfg = td2.get(0).getArfcn();
                pci_cfg = td2.get(0).getPci();
            }
            if (!arfcn_cfg.isEmpty()) runCell1 = true;
            mTraceUtilNr.setAirSync(GnbProtocol.CellId.SECOND, enableAir);
            mTraceUtilNr.setBandWidth(GnbProtocol.CellId.SECOND, 5);
            mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.SECOND, 128);
            mTraceUtilNr.setPlmn(GnbProtocol.CellId.SECOND, "46000");
            mTraceUtilNr.setArfcn(GnbProtocol.CellId.SECOND, arfcn_cfg);
            mTraceUtilNr.setPci(GnbProtocol.CellId.SECOND, pci_cfg);

            // 通道三
            arfcn_cfg = "";
            pci_cfg = "";
            LinkedList<ArfcnPciBean> td3 = arfcnBeanHashMap.get("TD3");
            if (td3 != null && td3.size() > 0) {
                arfcn_cfg = td3.get(0).getArfcn();
                pci_cfg = td3.get(0).getPci();
            }
            mTraceUtilNr.setAirSync(GnbProtocol.CellId.THIRD, enableAir);
            if (!arfcn_cfg.isEmpty()) {
                band = NrBand.earfcn2band(Integer.parseInt(arfcn_cfg));
                mTraceUtilNr.setBandWidth(GnbProtocol.CellId.THIRD, band == 1 ? 20 : 100);
                mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.THIRD, band == 1 ? 240 : 255);
                mTraceUtilNr.setPlmn(GnbProtocol.CellId.THIRD, band == 1 ? "46001" : "46000");
                runCell2 = true;
            }
            mTraceUtilNr.setArfcn(GnbProtocol.CellId.THIRD, arfcn_cfg);
            mTraceUtilNr.setPci(GnbProtocol.CellId.THIRD, pci_cfg);

            // 通道四
            arfcn_cfg = "";
            pci_cfg = "";
            LinkedList<ArfcnPciBean> td4 = arfcnBeanHashMap.get("TD4");
            if (td4 != null && td4.size() > 0) {
                arfcn_cfg = td4.get(0).getArfcn();
                pci_cfg = td4.get(0).getPci();
            }
            if (!arfcn_cfg.isEmpty()) {
                int freq = LteBand.earfcn2freq(Integer.parseInt(arfcn_cfg));
                if ((freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949)) mTraceUtilNr.setPlmn(GnbProtocol.CellId.FOURTH, "46000");
                else mTraceUtilNr.setPlmn(GnbProtocol.CellId.FOURTH, "46001");
                runCell3 = true;
            }
            mTraceUtilNr.setAirSync(GnbProtocol.CellId.FOURTH, enableAir);
            mTraceUtilNr.setBandWidth(GnbProtocol.CellId.FOURTH, 5);
            mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.FOURTH, 128);

            mTraceUtilNr.setArfcn(GnbProtocol.CellId.FOURTH, arfcn_cfg);
            mTraceUtilNr.setPci(GnbProtocol.CellId.FOURTH, pci_cfg);

            MainActivity.getInstance().getDeviceList().get(i).setTraceUtil(mTraceUtilNr);

            final List<UeidBean> blackList = new ArrayList<>();
            List<MyUeidBean> myUeidBeanList = MainActivity.getInstance().getBlackList();
            for (int j = myUeidBeanList.size() - 1; j >= 0; j--){
                if (blackList.size() >= 40) break;
                blackList.add(myUeidBeanList.get(j).getUeidBean());
            }

            // 第一步，配置黑名单
            isResetBlack = false;
            if (!runCell0 && !runCell1 && !runCell2 && !runCell3) {
                MainActivity.getInstance().showToast("未检测到配置频点/PCI");
                return;
            }
            if (runCell0) {
                MessageController.build().setBlackList(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId(), false, GnbProtocol.CellId.FIRST,
                        blackList.size(), blackList);

                MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.BLACKLIST);
                setProgress(type, 20, GnbProtocol.CellId.FIRST, "配置中", false);
            }
            int finalI = i;
            if (runCell1) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MessageController.build().setBlackList(MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDeviceId(), true, GnbProtocol.CellId.SECOND,
                                blackList.size(), blackList);

                        MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.BLACKLIST);
                        setProgress(type, 20, GnbProtocol.CellId.SECOND, "配置中", false);
                        refreshTraceBtn();
                    }
                },500);
            }
            if (runCell2) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MessageController.build().setBlackList(MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDeviceId(), false, GnbProtocol.CellId.THIRD,
                                blackList.size(), blackList);
                        MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.BLACKLIST);
                        setProgress(type, 20, GnbProtocol.CellId.THIRD, "配置中", false);
                        refreshTraceBtn();
                    }
                },1000);
            }

            if (runCell3) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MessageController.build().setBlackList(MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDeviceId(), true, GnbProtocol.CellId.FOURTH,
                                blackList.size(), blackList);

                        MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.BLACKLIST);
                        setProgress(type, 20, GnbProtocol.CellId.FOURTH, "配置中", false);
                        refreshTraceBtn();
                    }
                },1500);
            }

            MainActivity.getInstance().showToast("正在配置启动搜寻..");
            AppLog.I("startTrace() 搜寻启动, isUseDefault = " + MainActivity.getInstance().isUseDefault + ", isAutoRun = " + isAutoRun);
        }
        refreshTraceBtn();
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
                int async_enable = PrefUtil.build().getValue("sync_mode", mContext.getString(R.string.air)).toString().equals("GPS") ? 0 : 1;
                MessageController.build().startFreqScan(id, 0, async_enable, arfcn_list.size(), chan_id, arfcn_list, time_offset);
            }
        }, 200);
    }

    boolean isFirstStart = true;
    public void onStartControlRsp(String id, GnbTraceRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;

        //int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains(devA) ? 0 : 1;
        int type = 0;
        DeviceInfoBean deviceInfoBean = MainActivity.getInstance().getDeviceList().get(indexById);
        final TraceUtil traceUtil = deviceInfoBean.getTraceUtil();
        if (rsp.getCmdRsp() != null) {
            AppLog.I("onStartControlRsp():  id = " + id + ", rsp = " + rsp);
            final int cell_id = rsp.getCmdRsp().getCellId();

            if (rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_gNB_START_CONTROL || rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_eNB_START_CONTROL) {
                String s = getCellStr(cell_id);
                if (rsp.getCmdRsp().getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    if (isClickStop){
                        MessageController.build().setCmdAndCellID(id, rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_gNB_START_CONTROL ? GnbProtocol.UI_2_gNB_STOP_CONTROL : GnbProtocol.UI_2_eNB_STOP_CONTROL, cell_id);
                        return;
                    }
                    if (isFirstStart){
                        File file = new File(FileProtocol.DIR_UP_DATA);
                        if (!file.exists()) file.mkdirs();
                        File file1 = new File(FileProtocol.DIR_UP_DATA + "/" + nowWriteFileName);
                        if (file1.exists()) file1.delete();
                        isFirstStart = false;
                        mDataUpHandler.removeMessages(0);
                        mDataUpHandler.sendEmptyMessageDelayed(0, MainActivity.getInstance().mDataUpBean.getUpCycle() * 1000);
                    }
                    //第五步.搜寻中，这里做判断，是设置状态为搜寻中还是定位中
                    String imsi = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getImsi(cell_id);

                    // 10秒无上号数据逻辑2   仅自动模式下  开始计时
                    if (isAutoRun){
                        String arfcn = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getArfcn(cell_id);
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
                                LinkedList<ArfcnPciBean> td = PaCtl.build().isB97502 ? arfcnBeanHashMap.get("TD2") : arfcnBeanHashMap.get("TD4");
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

                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(cell_id, GnbBean.State.CONTROL);
                    mHandler.removeMessages(cell_id);
                    if (!MainActivity.getInstance().isUseDefault || isAutoRun)
                        mHandler.sendEmptyMessageDelayed(cell_id, autoArfcnTime * 1000L);

                    // 刷新处于工作状态
                    freshDoWorkState(type, cell_id, imsi, traceUtil.getArfcn(cell_id), traceUtil.getPci(cell_id));

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            OpLog.build().write(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + cell_id
                                    + "\t\t开始搜寻，目标IMSI: " + traceUtil.getImsi(cell_id));
                        }
                    }, 500);

                    final String cfg_plmn_mode = PrefUtil.build().getValue("cfg_plmn_mode", mContext.getString(R.string.use_def)).toString();
                    String plmns = PrefUtil.build().getValue("cfg_plmn", "").toString();
                    ArrayList<String> list = new ArrayList<>();
                    if (!plmns.isEmpty()){
                        String[] split = plmns.split(";");
                        list.addAll(Arrays.asList(split));
                    }
                    if (!cfg_plmn_mode.equals(mContext.getString(R.string.use_def)) && list.size() > 0) MessageController.build().resetPlmn(id, cell_id, rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_eNB_START_CONTROL, list);
                    //startSecondTrace(id, indexById, cell_id, false); //通道搜寻成功后再开始下一通道的搜寻流程
                } else {

                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(cell_id, GnbBean.State.IDLE);
                    setProgress(type, 90, cell_id, "启动失败", true);
                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "搜寻启动失败");
                    MainActivity.getInstance().showToast("通道" + s + "搜寻开启失败，请检查配置参数");

                    //startSecondTrace(id, indexById, rsp.getCellId(), true); //通道搜寻失败，开始下一通道的搜寻流程
                }
                refreshTraceBtn();
            }
        } else { // IMSI上报及上号报值
            if (isClickClear) return;
            if (rsp.getCellId() != -1) {
                int cell_id = rsp.getCellId();
                // 10秒无上号数据逻辑3   仅自动模式下  有上号则重新计时
                if (isAutoRun && bandRunOneHandleMap.containsKey(cell_id)){
                    int what = bandRunOneHandleMap.get(cell_id);
                    mHandler.removeMessages(what);
                    mHandler.sendEmptyMessageDelayed(what, 10000);
                }
                String traceArfcn = traceUtil.getArfcn(cell_id);

                String traceImsi = traceUtil.getImsi(cell_id);
                String tracePci = traceUtil.getPci(cell_id);
                if (traceArfcn.isEmpty()) return;
                if (rsp.getRsrp() < 5 && (traceImsi.isEmpty() || traceArfcn.equals("0") || tracePci.isEmpty()))
                    return;

                List<String> imsiList = rsp.getImsiList();
                if (imsiList != null && imsiList.size() > 0) {
                    String imsi = imsiList.get(0);
                    // 黑名单中的不显示
                    if (MainActivity.getInstance().isInBlackList(imsi)) return;
                    int rsrp = rsp.getRsrp();

                    if (rsrp <= 5) rsrp = -1;

                    boolean isNowShow = false;
                    if (mImsiBean != null && mImsiBean.getImsi().equals(imsi)) {
                        isNowShow = true;
                        if (mImsiBean.getRsrp() >= 11 && rsrp == 7) rsrp = mImsiBean.getRsrp();
                        mImsiBean.setRsrp(rsrp);
                        mShowChildFragment.resetRsrp(rsrp, traceArfcn, tracePci, cell_id);
                    }
                    //Log.d("onStartControlRsp", "into time = " + System.currentTimeMillis());
                    boolean add = true;
                    for (int j = 0; j < mImsiList.size(); j++) {
                        if (mImsiList.get(j).getImsi().equals(imsi)) {
                            add = false;
                            mImsiList.get(j).setCellId(cell_id);
                            mImsiList.get(j).setArfcn(traceArfcn);
                            mImsiList.get(j).setPci(tracePci);
                            if (mImsiList.get(j).getRsrp() < 11 || rsrp != 7) mImsiList.get(j).setRsrp(rsrp);
                            mImsiList.get(j).setRnti(rsp.getRnti());
                            mImsiList.get(j).setPhone_type(rsp.getPhone_type());
                            mImsiList.get(j).setUpCount(mImsiList.get(j).getUpCount() + 1);
                            mImsiList.get(j).setLatestTime(System.currentTimeMillis());
                            break;
                        }
                    }

                    all_count++;
                    if (add){
                        ImsiBean newBean = new ImsiBean(ImsiBean.State.IMSI_NEW, imsi, traceArfcn, tracePci, rsrp, System.currentTimeMillis(), cell_id);
                        if (rsp.getPhone_type() > 1 && rsrp > MainActivity.getInstance().showMinRsrp) newBean.setPhone_type(rsp.getPhone_type());
                        mImsiList.add(newBean);
                    }
                    add = true;
                    for (int j = 0; j < mAllImsiList.size(); j++) {
                        if (mAllImsiList.get(j).getImsi().equals(imsi)) {
                            add = false;
                            if (rsp.getPhone_type() > 1 && mAllImsiList.get(j).getPhone_type() < 2 && rsrp > MainActivity.getInstance().showMinRsrp) {
                                mAllImsiList.get(j).setPhone_type(rsp.getPhone_type());
                                MainActivity.getInstance().vibrate(imsi.substring(11));
                                /*if (rsp.getPhone_type() > 1) {
                                    String imsiEnd = imsi.substring(11);
                                    MainActivity.getInstance().vibrate(imsiEnd);
                                    int finalRsrp = rsrp;
                                    new Thread(){
                                        @Override
                                        public void run() {
                                            try {
                                                DatagramSocket socket = new DatagramSocket(1230); //本地口号
                                                InetAddress address = InetAddress.getByName("255.255.255.255"); //对端IP
                                                String data = imsiEnd + ";" + finalRsrp;
                                                byte[] dataByte = data.getBytes(); //建立数据
                                                DatagramPacket packet = new DatagramPacket(dataByte, dataByte.length, address, 1231); //通过该数据建包
                                                socket.send(packet); //开始发送该包
                                                socket.close();
                                                AppLog.D("Udp SendData To Peer, data: " + data);
                                            } catch (Exception e) {
                                                AppLog.E("Udp SendData To Peer, error: " + e);
                                            }
                                        }
                                    }.start();
                                }*/
                                if (isNowShow)
                                    mShowChildFragment.setIphoneIcon(rsp.getPhone_type() > 1);
                            }
                            break;
                        }
                    }
                    String Operators = "其他";
                    int OperatorsNum = 5;
                    int net_type = 7;
                    String net_str = "SA";

                    String tracePlmn = imsi.substring(0, 5);

                    switch (tracePlmn) {
                        case "46011":
                        case "46003":
                        case "46005":
                        case "46012":
                            if (add) telecom_count++;
                            Operators = "电信";
                            OperatorsNum = 3;
                            break;
                        case "46000":
                        case "46002":
                        case "46007":
                        case "46004":
                        case "46008":
                        case "46013":
                            if (add) mobile_count++;
                            Operators = "移动";
                            OperatorsNum = 1;
                            break;
                        case "46001":
                        case "46009":
                        case "46006":
                        case "46010":
                            if (add) unicom_count++;
                            Operators = "联通";
                            OperatorsNum = 2;
                            break;
                        case "46015":
                            if (add) sva_count++;
                            Operators = "广电";
                            OperatorsNum = 4;
                            break;
                    }
                    if (add) {
                        ImsiBean newBean = new ImsiBean(ImsiBean.State.IMSI_NEW, imsi, traceArfcn, tracePci, rsrp, System.currentTimeMillis(), cell_id);
                        if (rsp.getPhone_type() > 1 && rsrp > MainActivity.getInstance().showMinRsrp) {
                            newBean.setPhone_type(rsp.getPhone_type());
                            MainActivity.getInstance().vibrate(imsi.substring(11));
                        }
                        mAllImsiList.add(newBean);
                        if (rsp.getPhone_type() > 1 && rsrp > MainActivity.getInstance().showMinRsrp)
                            MainActivity.getInstance().vibrate(imsi.substring(11));
                    }

                    if (MainActivity.getInstance().isUpFTP){
                        if (traceArfcn.length() < 6) {
                            int band = LteBand.earfcn2band(Integer.parseInt(traceArfcn));
                            if (band < 33 || band > 53) {
                                net_type = 6;
                                net_str = "FDD-LTE";
                            } else {
                                net_type = 5;
                                net_str = "TDD-LTE";
                            }
                        }
                        DataUpBean mDataUpBean = MainActivity.getInstance().mDataUpBean;
                        String content = mDataUpBean.getUuid() + "\t" + mDataUpBean.getWlCode() + "\t"
                                + System.currentTimeMillis() / 1000 + "\t" + deviceInfoBean.getRsp().getLongitude() + "\t" + deviceInfoBean.getRsp().getLatitude() + "\t"
                                + imsi + "\t\t\t\t\t" + OperatorsNum + "\t" + Operators + "\t" + net_type + "\t" + net_str + "\t\t不发送短信\r\n";
                        fileStreamWrite(content);
                    }

                    mCatchChildFragment.setCatchCount(all_count, telecom_count, mobile_count, unicom_count, sva_count);
                    /*int finalRsrp = rsrp;
                    if (finalRsrp > MainActivity.getInstance().showMinRsrp) {
                        String imsiEnd = imsi.substring(11);
                        new Thread(){
                            @Override
                            public void run() {
                                try {
                                    DatagramSocket socket = new DatagramSocket(1230); //本地口号
                                    InetAddress address = InetAddress.getByName("255.255.255.255"); //对端IP
                                    String data = imsiEnd + ";" + finalRsrp + ";" + (rsp.getPhone_type() > 1 ? 1 : 0);
                                    byte[] dataByte = data.getBytes(); //建立数据
                                    DatagramPacket packet = new DatagramPacket(dataByte, dataByte.length, address, 1231); //通过该数据建包
                                    socket.send(packet); //开始发送该包
                                    socket.close();
                                } catch (Exception e) {
                                    AppLog.E("Udp SendData To Peer, error: " + e);
                                }
                            }
                        }.start();
                    }*/
                }

                if (!isStartCatchHandler) {
                    isStartCatchHandler = true;
                    ArrayList<ImsiBean> arrayList = new ArrayList<>(mImsiList);
                    mImsiList.clear();
                    mCatchChildFragment.resetShowData(arrayList); // 刷新视图
                    mHandler.sendEmptyMessageDelayed(9, 2000);
                }
                //Log.d("onStartControlRsp", "end time = " + System.currentTimeMillis());
            }
        }
    }

    private synchronized void fileStreamWrite(String content) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(FileProtocol.DIR_UP_DATA + "/" + nowWriteFileName, true), "gbk"));
            out.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) out.close();
                if (isDataUpFlag) {
                    isDataUpFlag = false;
                    nowWriteFileName = newCreateFileName;
                    newCreateFileName = "";
                    mDataUpHandler.sendEmptyMessage(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setHandler(boolean isStop, int cell_id){
        if (isStop){
            mHandler.removeCallbacksAndMessages(null);
            isStartCatchHandler = false;
        }else {
            mHandler.removeMessages(cell_id);
            AppLog.D("reset mHandler isUseDefault = " + MainActivity.getInstance().isUseDefault + ", isAutoRun = " + isAutoRun + ", cell_id = " + cell_id);
            if (!MainActivity.getInstance().isUseDefault || isAutoRun)
                mHandler.sendEmptyMessageDelayed(cell_id, autoArfcnTime * 1000L + cell_id * 500L);
        }
    }

    public void onStopControlRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;

        int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains(devA) ? 0 : 1;
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_STOP_CONTROL || rsp.getCmdType() == GnbProtocol.UI_2_eNB_STOP_CONTROL) {

                switch (rsp.getCellId()) {
                    case 0:
                        if (autoRun[0] && MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST) != GnbBean.State.STOP) {
                            if (rsp.getRspValue() == 3) {
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_CONTROL, rsp.getCellId());
                                        PaCtl.build().closePAByArfcn(id, false, mTraceUtilNr.getArfcn(rsp.getCellId()));
                                    }
                                }, 5000);
                                return;
                            }
                            updateParamAndStart(id, MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getArfcn(rsp.getCellId()), MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getPci(rsp.getCellId()), 0, rsp.getCellId());
                            return;
                        }
                        break;
                    case 1:
                        if (autoRun[1] && MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) != GnbBean.State.STOP) {
                            if (rsp.getRspValue() == 3) {
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_eNB_STOP_CONTROL, rsp.getCellId());
                                        PaCtl.build().closePAByArfcn(id, true, mTraceUtilNr.getArfcn(rsp.getCellId()));
                                    }
                                }, 5000);
                                return;
                            }
                            updateParamAndStart(id, MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getArfcn(rsp.getCellId()), MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getPci(rsp.getCellId()), 0, rsp.getCellId());
                            return;
                        }
                        break;
                    case 2:
                        if (autoRun[2] && MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getWorkState(GnbProtocol.CellId.THIRD) != GnbBean.State.STOP) {
                            if (rsp.getRspValue() == 3) {
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_CONTROL, rsp.getCellId());
                                        PaCtl.build().closePAByArfcn(id, false, mTraceUtilNr.getArfcn(rsp.getCellId()));
                                    }
                                }, 5000);
                                return;
                            }
                            updateParamAndStart(id, MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getArfcn(rsp.getCellId()), MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getPci(rsp.getCellId()), 0, rsp.getCellId());
                            return;
                        }
                        break;
                    case 3:
                        if (autoRun[3] && MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getWorkState(GnbProtocol.CellId.FOURTH) != GnbBean.State.STOP) {
                            if (rsp.getRspValue() == 3) {
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_eNB_STOP_CONTROL, rsp.getCellId());
                                        PaCtl.build().closePAByArfcn(id, true, mTraceUtilNr.getArfcn(rsp.getCellId()));
                                    }
                                }, 5000);
                                return;
                            }
                            updateParamAndStart(id, MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getArfcn(rsp.getCellId()), MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getPci(rsp.getCellId()), 0, rsp.getCellId());
                            return;
                        }
                        break;
                }

                String s = getCellStr(rsp.getCellId());
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    mHandler.removeMessages(rsp.getCellId());
                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, "通道" + s + "结束工作成功");
                    setProgress(type, 0, rsp.getCellId(), "空闲", false);
                    //mTraceChildFragment.resetRsrp(type);
                } else {
                    if (rsp.getRspValue() == 3) {
                        MainActivity.getInstance().showToast("通道" + s + "忙，请稍后再结束");
                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.CONTROL);
                        setProgress(type, 100, rsp.getCellId(), "搜寻中", false);
                        refreshTraceBtn();
                        return;
                    } else if (rsp.getRspValue() == 5) {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, "通道" + s + "结束工作成功");
                        setProgress(type, 0, rsp.getCellId(), "空闲", false);
                    } else {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "通道" + s + "结束工作失败");
                        setProgress(type, 50, rsp.getCellId(), "结束失败", true);
                        MainActivity.getInstance().showToast("通道" + s + "结束工作失败, 请重试");
                    }
                }
                //setAnimWork(type == 0 ? iv_anim_nr : iv_anim_lte, -1, false);
                OpLog.build().write(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + rsp.getCellId() + "\t\t结束搜寻");
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setTacChange(rsp.getCellId(), false);
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setEnable(rsp.getCellId(), false);
                //refreshTraceValue(indexById, rsp.getCellId(), 0);
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setRsrp(rsp.getCellId(), 0);
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setLastRsrp(rsp.getCellId(), -1);
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setImsi(rsp.getCellId(), "");
                //MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setArfcn(rsp.getCellId(), "");
                //MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setPci(rsp.getCellId(), "");

                /*mHandler.removeMessages(2);
                mHandler.removeMessages(3);
                isRsrpStart = false;
                isStartCatchHandler = false;*/
                refreshTraceBtn();
            }
        }
    }

    public void onFreqScanRsp(String id, GnbFreqScanRsp rsp) {
        if (isInFreqForPci){
            if (rsp != null) {
                AppLog.I("FreqFragment onFreqScanRsp() isClickStop " + isClickStop + ", rsp.getReportStep() = " + rsp.getReportStep() + ", rsp.getScanResult() = " + rsp.getScanResult());
                if (!isClickStop) {
                    if (rsp.getReportStep() == 2) freqScan(id);
                }
                if (rsp.getScanResult() == GnbProtocol.OAM_ACK_OK && rsp.getReportStep() == 1) {
                    if (MainActivity.getInstance().freqList.size() == 0) {
                        MainActivity.getInstance().freqList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
                                rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
                                rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
                    } else {
                        boolean isAdd = true;
                        for (int i = 0; i < MainActivity.getInstance().freqList.size(); i++) {
                            if (MainActivity.getInstance().freqList.get(i).getUl_arfcn() == rsp.getUl_arfcn() &&
                                    MainActivity.getInstance().freqList.get(i).getPci() == rsp.getPci()) {
                                isAdd = false;
                                MainActivity.getInstance().freqList.remove(MainActivity.getInstance().freqList.get(i));
                                MainActivity.getInstance().freqList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
                                        rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
                                        rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
                                break;
                            }
                        }
                        if (isAdd) {
                            MainActivity.getInstance().freqList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
                                    rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
                                    rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
                        }
                    }
                }
            }
        }
        if (mFreqDialog != null) mFreqDialog.onFreqScanRsp(id, rsp);
    }
}