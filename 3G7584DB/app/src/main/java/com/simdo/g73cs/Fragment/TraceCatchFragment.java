package com.simdo.g73cs.Fragment;

import static com.simdo.g73cs.MainActivity.devA;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.dwdbsdk.Bean.DW.DWProtocol;
import com.dwdbsdk.Bean.LteBand;
import com.dwdbsdk.Bean.NrBand;
import com.dwdbsdk.Bean.UeidBean;
import com.dwdbsdk.MessageControl.MessageController;
import com.dwdbsdk.Response.DW.GnbCmdRsp;
import com.dwdbsdk.Response.DW.GnbFreqScanRsp;
import com.dwdbsdk.Response.DW.GnbStateRsp;
import com.dwdbsdk.Response.DW.GnbTraceRsp;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.simdo.g73cs.Adapter.FragmentAdapter;
import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.Bean.DataFwdBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.HistoryBean_3g758cx;
import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.Bean.TraceBean;
import com.simdo.g73cs.DBViewModel;
import com.simdo.g73cs.Dialog.FreqDialog;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DataBaseUtil_3g758cx;
import com.simdo.g73cs.Util.DateUtil;
import com.simdo.g73cs.Util.DbGpioCtl;
import com.simdo.g73cs.Util.DeviceUtil;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.LteBand2;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.ParseDataUtil;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.TraceUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.View.BlackListSlideAdapter;
import com.simdo.g73cs.ZApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TraceCatchFragment extends Fragment implements TraceChildFragment.TraceCatchFragmentCallback {

    Context mContext;
    private List<ImsiBean> mImsiList;   //侦码imsi列表
    private BlackListSlideAdapter adapter;
    private int time_count = 60;    //轮询时间
//    private int time_count = 30;    //轮询时间

    FreqFragment mFreqFragment;
    //是否频点轮询
    private boolean isCell1SetArfcnChange, isCell2SetArfcnChange, isCell3SetArfcnChange, isCell4SetArfcnChange;
    //下一个要轮询到的频点索引
    int autoArfcnIndex0 = 0, autoArfcnIndex1 = 0, autoArfcnIndex2 = 0, autoArfcnIndex3 = 0,
            autoArfcnIndex4 = 0, autoArfcnIndex5 = 0, autoArfcnIndex6 = 0, autoArfcnIndex7 = 0,
            autoArfcnIndex8 = 0, autoArfcnIndex9 = 0, autoArfcnIndex10 = 0, autoArfcnIndex11 = 0;

    boolean isClickStop = false;
    //设置是否要在结束时接着运行（切换的频点不在同一个频段的时候）
    boolean autoRun1 = false, autoRun2 = false, autoRun3 = false, autoRun4 = false, autoRun5 = false, autoRun6 = false, autoRun7 = false, autoRun8 = false, autoRun9 = false, autoRun10 = false, autoRun11 = false, autoRun12 = false;

    FreqDialog mFreqDialog;
    private String auto_trace_imsi;
    boolean isStartCatchHandler1 = false;    //是否上号
    boolean isStartCatchHandler2 = false;    //是否上号
    boolean isStartCatchHandler3 = false;    //是否上号
    boolean isStartCatchHandler4 = false;    //是否上号
    boolean isStartCatchHandler5 = false;    //是否上号
    boolean isStartCatchHandler6 = false;    //是否上号
    boolean isStartCatchHandler7 = false;    //是否上号
    boolean isStartCatchHandler8 = false;    //是否上号
    boolean isStartCatchHandler9 = false;    //是否上号
    boolean isStartCatchHandler10 = false;    //是否上号
    boolean isStartCatchHandler11 = false;    //是否上号
    boolean isStartCatchHandler12 = false;    //是否上号
    int countIfsetRsrpValueFirst = 0;   //发6次-1进去成功报掉线
    int countIfsetRsrpValueSecond = 0;
    int countIfsetRsrpValueThird = 0;
    int countIfsetRsrpValueFourth = 0;
    int countIfsetRsrpValueFifth = 0;
    int countIfsetRsrpValueSixth = 0;
    int countIfsetRsrpValueSeventh = 0;
    int countIfsetRsrpValueEighth = 0;
    int countIfsetRsrpValueNinth = 0;
    int countIfsetRsrpValueTenth = 0;
    int countIfsetRsrpValueEleventh = 0;
    int countIfsetRsrpValueTwelfth = 0;

    int countFirst = 0; //通道掉线计数
    int countSecond = 0;
    int countThird = 0;
    int countFourth = 0;
    int countFifth = 0;
    int countSixth = 0;
    int countSeventh = 0;
    int countEighth = 0;
    int countNinth = 0;
    int countTenth = 0;
    int countEleventh = 0;
    int countTwelfth = 0;
    boolean isRsrpStart = false;
    int setArfcnChangeCell1, setArfcnChangeCell2, setArfcnChangeCell3, setArfcnChangeCell4; //当前的频点循环索引
    List<Integer> arfcnListCell1 = new ArrayList<>();
    List<Integer> arfcnListCell2 = new ArrayList<>();
    List<Integer> arfcnListCell3 = new ArrayList<>();
    List<Integer> arfcnListCell4 = new ArrayList<>();
    boolean ifRecountFirst = false;
    boolean ifRecountSecond = false;
    boolean ifRecountThird = false;
    boolean ifRecountFourth = false;
    boolean ifRecountFifth = false;
    boolean ifRecountSixth = false;
    boolean ifRecountSeventh = false;
    boolean ifRecountEighth = false;
    boolean ifRecountNinth = false;
    boolean ifRecountTenth = false;
    boolean ifRecountEleventh = false;
    boolean ifRecountTwelfth = false;

    Handler mainHandler = new Handler(Looper.getMainLooper());
    final Object lockB40 = new Object();
    final Object lockB3 = new Object();
    int mainB40Success = -1;        //-1主B40未执行，0主B40执行失败,1主B40执行成功
    int mainB3Success = -1;        //-1主B40未执行，0主B40执行失败,1主B40执行成功
    boolean isMainB40 = false;  //在使用时要置为false
    boolean isMainB3 = false;  //在使用时要置为false
    AtomicInteger delay = new AtomicInteger();
    DBViewModel DBViewModel;
    DataFwdBean dataFwdBean = new DataFwdBean();

    public TraceCatchFragment() {
        mContext = ZApplication.getInstance().getContext();
    }


    public TraceCatchFragment(FreqFragment freqFragment) {
        mContext = ZApplication.getInstance().getContext();
        this.mFreqFragment = freqFragment;
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
        DBViewModel = new ViewModelProvider(requireActivity()).get(DBViewModel.class);

        initData();
        initView(root);
//        mHandler.sendEmptyMessage(666);
        return root;
    }

    int all_count = 0, unicom_count = 0, mobile_count = 0, telecom_count = 0, sva_count = 0;

    public CfgTraceChildFragment getmCfgTraceChildFragment() {
        return mCfgTraceChildFragment;
    }

    CfgTraceChildFragment mCfgTraceChildFragment;

    TraceChildFragment mTraceChildFragment;
    CatchChildFragment mCatchChildFragment;
    SettingFragment mSettingFragment;
    ViewPager2 view_pager;
    public final HashMap<String, LinkedList<ArfcnPciBean>> arfcnBeanHashMap = new HashMap<>();  //存储12通道的频点

    private void initView(View root) {
        // 左右滑动导航
        List<Fragment> fragmentList = new ArrayList<>();
        mCfgTraceChildFragment = new CfgTraceChildFragment(this, mFreqFragment, mSettingFragment);
        mTraceChildFragment = new TraceChildFragment(this);
        AppLog.D("TraceCatchFragment mImsiList " + mImsiList.toString());
        mCatchChildFragment = new CatchChildFragment(mImsiList, this);
        fragmentList.add(mCfgTraceChildFragment);
        fragmentList.add(mTraceChildFragment);
        fragmentList.add(mCatchChildFragment);
        String[] titles = new String[]{getString(R.string.cfg_trace), getString(R.string.target_report), getString(R.string.catch_list)};

        view_pager = root.findViewById(R.id.view_pager_trace_catch);
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getChildFragmentManager(), getLifecycle(), fragmentList);
        view_pager.setAdapter(fragmentAdapter);
//        new Handler().postDelayed(() -> {
        view_pager.setCurrentItem(0);
//        }, 200);
        TabLayout tab_layout = root.findViewById(R.id.tab_trace_catch);

//        setTabWidthSame(tab_layout);

        TabLayoutMediator tab1 = new TabLayoutMediator(tab_layout, view_pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(titles[position]);
            }
        });
        tab1.attach();

        // 设置TabLayout的监听器
        tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTabView(tab, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                updateTabView(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 在重新选择时的处理
            }
        });


    }

    // 自定义TabLayout.Tab的视图
    private void updateTabView(TabLayout.Tab tab1, boolean isSelected) {
        // 使用SpannableString来设置字体样式
        SpannableString spannableString = new SpannableString(tab1.getText());
        StyleSpan styleSpan = new StyleSpan(isSelected ? Typeface.BOLD : Typeface.NORMAL);
        spannableString.setSpan(styleSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tab1.setText(spannableString);
    }

    public void clickClean() {
        mCatchChildFragment.clear();

        all_count = 0;
        unicom_count = 0;
        mobile_count = 0;
        telecom_count = 0;
        sva_count = 0;
    }

    /**
     * 设置tab的下划线和文字一样宽度
     * 此方法会忽视这个属性app:tabGravity="center"
     */
    public void setTabWidthSame(final TabLayout tabs) {
        ViewTreeObserver viewTreeObserver = tabs.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                tabs.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //拿到tabLayout的mTabStrip属性
                LinearLayout mTabStrip = (LinearLayout) tabs.getChildAt(0);
                int totalWidth = mTabStrip.getMeasuredWidth();
                int tabWidth = totalWidth / mTabStrip.getChildCount();
                for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                    View child = mTabStrip.getChildAt(i);
                    child.setPadding(0, 0, 0, 0);

                    Field mTextViewField = null;
                    int width = 0;
                    try {
                        try {
                            mTextViewField = child.getClass().getDeclaredField("mTextView");
                        } catch (Exception e) {
                            mTextViewField = child.getClass().getDeclaredField("textView"); //安卓28变量名变了
                        }
                        mTextViewField.setAccessible(true);
                        TextView mTextView = (TextView) mTextViewField.get(child);
                        //因为我想要的效果是   字多宽线就多宽，所以测量mTextView的宽度
                        width = mTextView.getWidth();
                        if (width == 0) {
                            mTextView.measure(0, 0);
                            width = mTextView.getMeasuredWidth();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child.getLayoutParams();
                    params.width = width;
                    params.leftMargin = (tabWidth - width) / 2;
                    params.rightMargin = (tabWidth - width) / 2;
                    child.setLayoutParams(params);
                    child.invalidate();
                }
            }
        });
    }

    private void initData() {
        mImsiList = new ArrayList<>();
        AppLog.D("mImsiList create");

        all_count = 0;
        unicom_count = 0;
        mobile_count = 0;
        telecom_count = 0;
        sva_count = 0;
    }

    private void getArfcnList(int cell, String arfcn) {
        isCell1SetArfcnChange = mCfgTraceChildFragment.isCell1SetArfcnChange();
        isCell2SetArfcnChange = mCfgTraceChildFragment.isCell2SetArfcnChange();
        isCell3SetArfcnChange = mCfgTraceChildFragment.isCell3SetArfcnChange();
        isCell4SetArfcnChange = mCfgTraceChildFragment.isCell4SetArfcnChange();

        String band_cell1 = "";
        String band_cell2 = "";
        String band_cell3 = "";
        String band_cell4 = "";
        String arfcn_cell1 = "";
        String arfcn_cell2 = "";
        String arfcn_cell3 = "";
        String arfcn_cell4 = "";
        if (cell == 0) {
            arfcn_cell1 = arfcn;
            if (arfcn_cell1.length() > 5) {
                band_cell1 = "N" + NrBand.earfcn2band(Integer.parseInt(arfcn_cell1));
            } else {
                band_cell1 = "B" + LteBand.earfcn2band(Integer.parseInt(arfcn_cell1));
            }
            arfcnListCell1.clear();
            try {
                if (!TextUtils.isEmpty(band_cell1)) {
                    arfcnListCell1.addAll(Util.json2Int(PrefUtil.build().getValue(band_cell1 + "cfg", "").toString(), band_cell1));
                }
            } catch (JSONException e) {
                AppLog.E("readArfcnData JSONException e = " + e);
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
                    arfcnListCell2.addAll(Util.json2Int(PrefUtil.build().getValue(band_cell2 + "cfg", "").toString(), band_cell2));
                }
            } catch (JSONException e) {
                AppLog.E("readArfcnData JSONException e = " + e);
            }
        } else if (cell == 2) {
            arfcn_cell3 = arfcn;
            if (arfcn_cell3.length() > 5) {
                band_cell3 = "N" + NrBand.earfcn2band(Integer.parseInt(arfcn_cell3));
            } else {
                band_cell3 = "B" + LteBand.earfcn2band(Integer.parseInt(arfcn_cell3));
            }
            arfcnListCell3.clear();
            try {
                if (!TextUtils.isEmpty(band_cell3)) {
                    arfcnListCell3.addAll(Util.json2Int(PrefUtil.build().getValue(band_cell3 + "cfg", "").toString(), band_cell3));
                }
            } catch (JSONException e) {
                AppLog.E("readArfcnData JSONException e = " + e);
            }
        } else if (arfcn_cell4 != null) {
            arfcn_cell4 = arfcn;
            if (arfcn_cell4.length() > 5) {
                band_cell4 = "N" + NrBand.earfcn2band(Integer.parseInt(arfcn_cell4));
            } else {
                band_cell4 = "B" + LteBand.earfcn2band(Integer.parseInt(arfcn_cell4));
            }
            arfcnListCell4.clear();
            try {
                if (!TextUtils.isEmpty(band_cell4)) {
                    arfcnListCell4.addAll(Util.json2Int(PrefUtil.build().getValue(band_cell4 + "cfg", "").toString(), band_cell4));
                }
            } catch (JSONException e) {
                AppLog.E("readArfcnData JSONException e = " + e);
            }
        }
    }

    int txValue = 0; // 增益的值

    public void dosetDWTxPwrOffset(final int value) {
        AppLog.I("TraceFragment dosetDWTxPwrOffset value = " + value + " txValue = " + txValue);

        if (txValue == value || txValue == -1) {
            return;
        }
        txValue = value;
        AppLog.I("TraceFragment rg_rx_gain rxValue = " + value);
        for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
            TraceUtil traceUtil = bean.getTraceUtil();
            String id = bean.getRsp().getDeviceId();
            AppLog.I("TraceFragment dosetDWTxPwrOffset getTraceType = " + MessageController.build().getTraceType(id));
            if (MessageController.build().getTraceType(id) == DWProtocol.TraceType.TRACE ||
                    MessageController.build().getTraceType(id) == DWProtocol.TraceType.LTE_TRACE) {
                String first_arfcn = traceUtil.getArfcn(DWProtocol.CellId.FIRST);
                String second_arfcn = traceUtil.getArfcn(DWProtocol.CellId.SECOND);
                String third_arfcn = traceUtil.getArfcn(DWProtocol.CellId.THIRD);
                String fourth_arfcn = traceUtil.getArfcn(DWProtocol.CellId.FOURTH);

                if (!first_arfcn.isEmpty())
                    MessageController.build().setDWTxPwrOffset(id, DWProtocol.CellId.FIRST, Integer.parseInt(first_arfcn), value);
                if (!second_arfcn.isEmpty())
                    MessageController.build().setDWTxPwrOffset(id, DWProtocol.CellId.SECOND, Integer.parseInt(second_arfcn), value);
                if (!third_arfcn.isEmpty())
                    MessageController.build().setDWTxPwrOffset(id, DWProtocol.CellId.THIRD, Integer.parseInt(third_arfcn), value);
                if (!fourth_arfcn.isEmpty())
                    MessageController.build().setDWTxPwrOffset(id, DWProtocol.CellId.FOURTH, Integer.parseInt(fourth_arfcn), value);

//                if (bean.getRsp().getDualCell() == 2) {
//                    String arfcn = traceUtil.getArfcn(DWProtocol.CellId.SECOND);
//                    if (!arfcn.isEmpty()) {
//                        new Handler().postDelayed(() ->
//                                MessageController.build().setDWTxPwrOffset(id, DWProtocol.CellId.SECOND, Integer.parseInt(arfcn), value), 300);
//                    }
//                }
            }
        }
    }

    public void doWork(String auto_trace_imsi) {
        this.auto_trace_imsi = auto_trace_imsi;
        String text = mCfgTraceChildFragment.getTv_do_btn().getText().toString();
        if (text.equals(getString(R.string.open))) {
            StringBuilder sb = new StringBuilder();
            for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                switch (bean.getWorkState()) {
                    case GnbBean.DW_State.NONE:
                        sb.append("-2");
                        break;
                    case GnbBean.DW_State.FREQ_SCAN:
                        sb.append("-1");
                        break;
                    case GnbBean.DW_State.IDLE:
                        sb.append("0");
                        break;
                }
            }
            AppLog.I("doWork() 开启定位 sb = " + sb);
            String doStr = sb.toString();
            if (doStr.isEmpty() || doStr.equals("-2-2-2")) {
                Util.showToast(getString(R.string.dev_offline));
                return;
            }
            if (doStr.contains("-1")) {
                Util.showToast(getString(R.string.dev_busy_please_wait));
                return;
            }
            if (doStr.contains("0")) {
                startTrace("", auto_trace_imsi); // 单设备在空闲状态下，启动定位
                return;
            }
            Util.showToast(getString(R.string.dev_busy_please_wait));
            return;

        } else if (text.equals(getString(R.string.open_stop))) {
            StringBuilder sb = new StringBuilder();
            String idleDev = "";
            for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                if (bean.getWorkState() == GnbBean.DW_State.IDLE) {
                    idleDev = bean.getRsp().getDevName();
                    sb.append("\t\n").append(bean.getRsp().getDevName());
                }
            }
            AppLog.I("doWork() 开启/结束定位 sb = " + sb);

            String finalIdleDev = idleDev;
            new AlertDialog.Builder(mContext)
                    .setTitle(R.string.do_guide)
                    .setMessage(getString(R.string.open_tip_start) + sb + getString(R.string.open_tip_end))
                    .setNeutralButton(R.string.start_trace, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startTrace(finalIdleDev, auto_trace_imsi); // 单设备在空闲状态下，启动定位
                        }
                    })
                    .setPositiveButton(R.string.stop_all, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            stopTraceDialog();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();

        } else if (text.equals(getString(R.string.stop))) {
            stopTraceDialog();
        } else if (text.equals(getString(R.string.starting))) {
            Util.showToast(getString(R.string.starting_cannot_fast));
        } else if (text.equals(getString(R.string.stoping))) {
            Util.showToast(getString(R.string.stoping_cannot_fast));
        }
    }

    public void updateLeftTvState(int type, int pro, int logicIndex, int cell_id, String info, boolean isFail) {
        MainActivity.getInstance().updateLeftTvState(type, pro, logicIndex, cell_id, info, isFail);
    }

    public void showBlackListDialog() {
        MainActivity.getInstance().createCustomDialog(true);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_blacklist_silde, null);

        RecyclerView black_list = view.findViewById(R.id.black_list);
        black_list.setLayoutManager(new LinearLayoutManager(mContext));

        adapter = new BlackListSlideAdapter(mContext, MainActivity.getInstance().getBlackList(), new BlackListSlideAdapter.ListItemListener() {
            @Override
            public void onItemClickListener(MyUeidBean bean) {
                if (MainActivity.getInstance().getDeviceList().size() <= 0) {
                    Util.showToast("未检测到设备,请先连接设备");
                    return;
                }
                showChangeTraceImsi(bean);
            }
        });
        black_list.setAdapter(adapter);
        view.findViewById(R.id.tv_add).setOnClickListener(view1 -> showBlackListCfgDialog(true, -1));
        view.findViewById(R.id.back).setOnClickListener(view12 -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, true);
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

        actv_imsi.setAdapter(mCfgTraceChildFragment.getDropImsiAdapter());

        if (isAdd) {
            tv_black_cfg_title.setText(R.string.add_black_title);
            btn_ok.setText(R.string.add);
            btn_cancel.setText(R.string.cancel);
            btn_cancel.setTextColor(Color.parseColor("#666666"));
            back.setVisibility(View.GONE);
        } else {
            tv_black_cfg_title.setText(R.string.edit_black_title);
            btn_ok.setText(R.string.confirm_modify);
            btn_cancel.setText(R.string.delete);
            btn_cancel.setTextColor(Color.RED);
            back.setVisibility(View.VISIBLE);
            if (position != -1) {
                MyUeidBean bean = MainActivity.getInstance().getBlackList().get(position);
                ed_imsi_name.setText(bean.getName());
                actv_imsi.setText(bean.getUeidBean().getImsi());
            }
        }

        btn_ok.setOnClickListener(v -> {

            String imsi_name = ed_imsi_name.getText().toString();
            String imsi = actv_imsi.getText().toString();
            if (imsi_name.isEmpty() || imsi.isEmpty()) {
                Util.showToast(getString(R.string.data_empty_tip));
                return;
            }
            if (imsi.length() != DWProtocol.MAX_IMSI_USE_LEN) {
                Util.showToast(getString(R.string.imsi_err_tip));
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
                } else {
                    Util.showToast(getString(R.string.imsi_repeat_tip));
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
        });

        btn_cancel.setOnClickListener(v -> {
            if (btn_cancel.getText().toString().equals(getString(R.string.delete))) {
                MainActivity.getInstance().createCustomDialog(false);
                View view2 = LayoutInflater.from(mContext).inflate(R.layout.dialog_confirm, null);
                TextView tv_title = (TextView) view2.findViewById(R.id.tv_title);
                TextView tv_msg = (TextView) view2.findViewById(R.id.tv_msg);
                tv_title.setText(mContext.getResources().getString(R.string.warning));

                tv_msg.setText(R.string.delete_tip);
                view2.findViewById(R.id.btn_ok).setOnClickListener(view1 -> {
                    MainActivity.getInstance().getBlackList().remove(position);
                    PrefUtil.build().putUeidList(MainActivity.getInstance().getBlackList());
                    adapter.notifyDataSetChanged();
                    Util.showToast("删除成功");
                    MainActivity.getInstance().closeCustomDialog();
                });
                view2.findViewById(R.id.btn_cancel).setOnClickListener(view1 -> MainActivity.getInstance().closeCustomDialog());
                MainActivity.getInstance().showCustomDialog(view2, false);
            } else MainActivity.getInstance().closeCustomDialog();
        });
        back.setOnClickListener(view1 -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    boolean isChangeImsi1 = false;
    boolean isChangeImsi2 = false;
    boolean isChangeImsi3 = false;

    private void showChangeTraceImsi(MyUeidBean bean) {
        AppLog.D("showChangeTraceImsi" + bean.getUeidBean().getImsi());
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView title = view.findViewById(R.id.title);
        title.setText(R.string.is_trace_this_imsi);


        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> {
            for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                DeviceInfoBean deviceInfoBean = MainActivity.getInstance().getDeviceList().get(i);
                int logicIndex = DeviceUtil.getLogicIndexById(deviceInfoBean.getRsp().getDeviceId());
                int type = deviceInfoBean.getRsp().getDevName().contains(devA) ? 0 : 1;

                int workState = deviceInfoBean.getWorkState();
                if (workState == GnbBean.DW_State.CATCH || workState == GnbBean.DW_State.TRACE) {
                    String id = deviceInfoBean.getRsp().getDeviceId();
                    String imsi = bean.getUeidBean().getImsi();

                    int cell_id = -1;//四个通道全部定位同一个目标
                    //如果是侦码列表内的，改变对应状态
                    for (int j = 0; j < mImsiList.size(); j++) {
                        if (mImsiList.get(j).getImsi().equals(imsi)) {
                            mImsiList.get(j).setState(ImsiBean.State.IMSI_NOW);
                            //cell_id = mImsiList.get(j).getCellId();
                            mCatchChildFragment.itemChanged(j);
                            break;
                        }
                    }

                    //如果是黑名单里的，改变对应状态
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

                    if (cell_id == -1) {
                        TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil();
                        int cellID = -1;
                        do {
                            String arfcn = "";
                            cellID++;
                            if (traceUtil.getWorkState(cellID) == GnbBean.DW_State.TRACE)
                                arfcn = traceUtil.getArfcn(cellID);
                            if (!arfcn.isEmpty()) {
                                setChangeImsi(logicIndex, cellID, arfcn, imsi);
                            }
                        } while (cellID < 3);
                    } else {
                        String info = workState == GnbBean.DW_State.TRACE ? getString(R.string.cell) + getCellStr(logicIndex, cell_id) + getString(R.string.change_imsi) : getString(R.string.cell) + getCellStr(logicIndex, cell_id) + getString(R.string.change_work_to_trace);

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setImsi(cell_id, bean.getUeidBean().getImsi());
                        if (Integer.parseInt(deviceInfoBean.getTraceUtil().getArfcn(0)) > 100000)
                            MessageController.build().startDWNrTrace(id, cell_id, 1, imsi);
                        else MessageController.build().startDWLteTrace(id, cell_id, 1, imsi);
                        if (logicIndex == 0)
                            isChangeImsi1 = true;
                        else if (logicIndex == 1)
                            isChangeImsi2 = true;
                        else if (logicIndex == 2)
                            isChangeImsi3 = true;
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, info);
                        MainActivity.getInstance().updateLeftTvState(type, 50, logicIndex, cell_id, getString(R.string.changeing), false);
                    }
                } else if (workState == GnbBean.DW_State.IDLE) {
                    //开启自动定位
                    mCfgTraceChildFragment.isAutoMode = true;
                    doWork(bean.getUeidBean().getImsi());
                }
            }
            MainActivity.getInstance().closeCustomDialog();
            MainActivity.getInstance().closeCustomDialog();
        });
        final TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(v -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    public void setChangeImsi(int logicIndex, int cell_id, String arfcn, String imsi) {
        AppLog.D("setChangeImsi logicIndex" + logicIndex + " cell_id" + cell_id + " arfcn" + arfcn + " imsi" + imsi);
        String s = getCellStr(logicIndex, cell_id);
        int index = DeviceUtil.getIndexByChannelNum(DeviceUtil.getChannelNum(logicIndex, cell_id));
        if (index == -1) return;
        String id = MainActivity.getInstance().getDeviceList().get(index).getRsp().getDeviceId();

        MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.cell) + s + getString(R.string.change_imsi));
        MainActivity.getInstance().updateLeftTvState(0, 50, logicIndex, cell_id, getString(R.string.changeing), false);
        MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setImsi(cell_id, imsi);
        if (logicIndex == 0)
            isChangeImsi1 = true;
        else if (logicIndex == 1)
            isChangeImsi2 = true;
        else if (logicIndex == 2)
            isChangeImsi3 = true;
        if (Integer.parseInt(arfcn) > 100000)
            MessageController.build().startDWNrTrace(id, cell_id, 1, imsi);
        else MessageController.build().startDWLteTrace(id, cell_id, 1, imsi);
    }

    /**
     * 添加黑名单
     */

    public String getBtnStr() {
        return mCfgTraceChildFragment.getTv_do_btn().getText().toString();
    }

    public void setBtnStr(boolean start) {
        TextView tv_do_btn = mCfgTraceChildFragment.getTv_do_btn();
        if (start) {
            if (tv_do_btn.getText().equals(getString(R.string.stop))) return;
            tv_do_btn.setText(getString(R.string.stop));
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_delete);
            mCfgTraceChildFragment.setcfgEnable(-1, false);
        } else {
            if (tv_do_btn.getText().equals(getString(R.string.open))) return;
            tv_do_btn.setText(getString(R.string.open));
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
            mCfgTraceChildFragment.setcfgEnable(-1, true);
        }
    }

    private void startTrace(String devName, String auto_trace_imsi) {
        AppLog.D("startTrace");
        if (!auto_trace_imsi.isEmpty()) mCfgTraceChildFragment.isAutoMode = true;
        //自动运行
        if (mCfgTraceChildFragment.isAutoMode) {
//            initAutoArfcnList();
            String tracePlmn = "";
            if (auto_trace_imsi.length() == 0) {
                mCfgTraceChildFragment.isAutoMode = false;
                startTrace(devName, "");
                return;
            }
            tracePlmn = auto_trace_imsi.substring(0, 5);

            switch (tracePlmn) {
                case "46003":
                case "46005":
                case "46011":
                case "46012":
                    tracePlmn = "46011";
                    break;
                case "46000":
                case "46002":
                case "46004":
                case "46007":
                case "46008":
                case "46013":
                    tracePlmn = "46000";
                    break;
                case "46001":
                case "46006":
                case "46009":
                case "46010":
                    tracePlmn = "46001";
                    break;
                case "46015":
                    tracePlmn = "46015";
                    break;
                default:
                    Util.showToast("IMSI输入错误,请检查输入值");
                    mCfgTraceChildFragment.isAutoMode = false;
                    mCfgTraceChildFragment.isAutoModeFreqRunning = false;
                    return;
            }
            mCfgTraceChildFragment.addDropImsi(auto_trace_imsi);
            mFreqDialog = new FreqDialog(requireActivity(), this, mCfgTraceChildFragment, tracePlmn);
//            mFreqDialog.setOnTraceSetListener(this);
            mFreqDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!mFreqDialog.isUserCancel()) {
                        txValue = 0;
                        isClickStop = false;
                        mImsiList.clear();
                        mCatchChildFragment.refreshSearchEditText();
                        arfcnBeanHashMap.put("TD1", mFreqDialog.getTD1());
                        arfcnBeanHashMap.put("TD2", mFreqDialog.getTD2());
                        arfcnBeanHashMap.put("TD3", mFreqDialog.getTD3());
                        arfcnBeanHashMap.put("TD4", mFreqDialog.getTD4());
                        arfcnBeanHashMap.put("TD5", mFreqDialog.getTD5());
                        arfcnBeanHashMap.put("TD6", mFreqDialog.getTD6());
                        arfcnBeanHashMap.put("TD7", mFreqDialog.getTD7());
                        arfcnBeanHashMap.put("TD8", mFreqDialog.getTD8());
                        arfcnBeanHashMap.put("TD9", mFreqDialog.getTD9());
                        arfcnBeanHashMap.put("TD10", mFreqDialog.getTD10());
                        arfcnBeanHashMap.put("TD11", mFreqDialog.getTD11());
                        arfcnBeanHashMap.put("TD12", mFreqDialog.getTD12());

                        int airSync = PrefUtil.build().getValue("sync_mode_dw", "Air").toString().equals("GPS") ? 0 : 1;

                        for (DeviceInfoBean deviceInfoBean : MainActivity.getInstance().getDeviceList()) {
                            TraceUtil traceUtil = deviceInfoBean.getTraceUtil();
                            if (traceUtil != null) {
                                traceUtil.setAirSync(DWProtocol.CellId.FIRST, airSync);
                                traceUtil.setAirSync(DWProtocol.CellId.SECOND, airSync);
                                traceUtil.setAirSync(DWProtocol.CellId.THIRD, airSync);
                                traceUtil.setAirSync(DWProtocol.CellId.FOURTH, airSync);
                            }
                        }

                        AtomicInteger delay = new AtomicInteger();
                        delay.set(2500);

                        for (DeviceInfoBean deviceInfoBean : MainActivity.getInstance().getDeviceList()) {
                            String id = deviceInfoBean.getRsp().getDeviceId();
                            PaCtl.build().closePAById(id);
                            int logicIndex = DeviceUtil.getLogicIndexById(id);
                            switch (logicIndex) {
                                case 0:
                                    if (mFreqDialog.getTD1().size() > 0) {
                                        runCell0(id, 1);
                                        delay.addAndGet(500);
                                    }
                                    if (mFreqDialog.getTD2().size() > 0) {
                                        int isSecondB40 = 0;
                                        if (PaCtl.build().is3G758) {
                                            ArfcnPciBean bean = arfcnBeanHashMap.get("TD2").get(0);
                                            if (bean.getArfcn().isEmpty()) isSecondB40 = -1;
                                            else if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 40)
                                                isSecondB40 = 1;
                                        }
                                        if (isSecondB40 != 1) {      //直接执行
                                            runCell1(id, delay.get(), 2);
                                            delay.addAndGet(500);
                                        }
                                        //无需等待
                                        if (isSecondB40 == 1) {    //次B40等待信号
                                            // 在子线程中执行等待信号的逻辑
//                                new Thread(() -> {
//                                    synchronized (lockB40) { // 使用共享的锁对象
//                                        try {
//                                            AppLog.D("SecondB40 is waiting");
//                                            lockB40.wait(); // 子线程等待信号
//                                        } catch (InterruptedException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }

                                            // 收到信号后，在主线程执行任务
//                                    mainHandler.post(() -> {
                                            runCell1(id, delay.get(), 2);  //确保通道四完成之后再执行
                                            delay.addAndGet(500);
//                                    });
//                                }).start();
                                        }
                                    }
                                    if (mFreqDialog.getTD3().size() > 0) {
                                        runCell2(id, delay.get(), 3);
                                        delay.addAndGet(500);
                                    }
                                    if (mFreqDialog.getTD4().size() > 0) {
                                        int isSecondB3 = 0;
                                        if (PaCtl.build().is3G758) {
                                            ArfcnPciBean bean = arfcnBeanHashMap.get("TD4").get(0);
                                            if (bean.getArfcn().isEmpty()) isSecondB3 = -1;
                                            else if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 40)
                                                isSecondB3 = 1;
                                        }
                                        if (isSecondB3 != 1) {      //直接执行
                                            runCell3(id, delay.get(), 4);
                                            delay.addAndGet(500);
                                        }
                                        //无需等待
                                        if (isSecondB3 == 1) {    //次B40等待信号
                                            //在子线程中执行等待信号的逻辑
//                                new Thread(() -> {
//                                    synchronized (lockB3) { // 使用共享的锁对象
//                                        try {
//                                            AppLog.D("SecondB40 is waiting");
//                                            lockB3.wait(); // 子线程等待信号
//                                        } catch (InterruptedException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//
                                            // 收到信号后，在主线程执行任务
//                                    mainHandler.post(() -> {
                                            runCell3(id, delay.get(), 4);
                                            delay.addAndGet(500);
//                                    });
//                                }).start();
                                        }
                                    }
                                    break;
                                case 1:
                                    if (mFreqDialog.getTD5().size() > 0) {
                                        runCell4(id, delay.get(), 5);
                                        delay.addAndGet(500);
                                    }
                                    if (mFreqDialog.getTD6().size() > 0) {
                                        runCell4(id, delay.get(), 6);
                                        delay.addAndGet(500);
                                    }
                                    if (mFreqDialog.getTD7().size() > 0) {
                                        runCell4(id, delay.get(), 7);
                                        delay.addAndGet(500);
                                    }
                                    if (mFreqDialog.getTD8().size() > 0) {
                                        runCell4(id, delay.get(), 8);
                                        delay.addAndGet(500);
                                    }
                                    break;
                                case 2:
                                    if (mFreqDialog.getTD9().size() > 0) {
                                        runCell4(id, delay.get(), 9);
                                        delay.addAndGet(500);
                                    }
                                    if (mFreqDialog.getTD10().size() > 0) {
                                        runCell4(id, delay.get(), 10);
                                        delay.addAndGet(500);
                                    }
                                    if (mFreqDialog.getTD11().size() > 0) {
                                        runCell4(id, delay.get(), 11);
                                        delay.addAndGet(500);
                                    }
                                    if (mFreqDialog.getTD12().size() > 0) {
                                        runCell4(id, delay.get(), 12);
                                        delay.addAndGet(500);
                                    }
                            }
                        }

                    }
                    mFreqDialog = null;
                }
            });
            mFreqDialog.show();
            mCfgTraceChildFragment.setAutoModeFreqRunning(true);
            mTraceChildFragment.resetRsrp();

            for (DeviceInfoBean deviceInfoBean : MainActivity.getInstance().getDeviceList()) {
                TraceUtil traceUtil = deviceInfoBean.getTraceUtil();
                if (traceUtil != null) {
                    traceUtil.setImsi(DWProtocol.CellId.FIRST, auto_trace_imsi);
                    traceUtil.setImsi(DWProtocol.CellId.SECOND, auto_trace_imsi);
                    traceUtil.setImsi(DWProtocol.CellId.THIRD, auto_trace_imsi);
                    traceUtil.setImsi(DWProtocol.CellId.FOURTH, auto_trace_imsi);

                    traceUtil.setPlmn(DWProtocol.CellId.FIRST, tracePlmn);
                    traceUtil.setPlmn(DWProtocol.CellId.SECOND, tracePlmn);
                    traceUtil.setPlmn(DWProtocol.CellId.THIRD, tracePlmn);
                    traceUtil.setPlmn(DWProtocol.CellId.FOURTH, tracePlmn);
                }
            }

            return;
        }

        mCfgTraceChildFragment.setChange1(false);
        mCfgTraceChildFragment.setChange2(false);
        mCfgTraceChildFragment.setChange3(false);
        mCfgTraceChildFragment.setChange4(false);

        isChangeImsi1 = false;
        isChangeImsi2 = false;
        isChangeImsi3 = false;
        if (mCfgTraceChildFragment.checkLastParam()) {  //检查没问题
            if (mCfgTraceChildFragment.checkImsiArfcnParam()) {   //检查imsi和频点的关系
                if (MainActivity.getInstance().getDeviceList().get(0).getWorkState() == GnbBean.DW_State.FREQ_SCAN) {
                    //停止扫频开始定位
                    for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                        if (MainActivity.getInstance().getDeviceList().get(i).getWorkState() == GnbBean.DW_State.FREQ_SCAN) {
                            String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
                            MessageController.build().stopDWFreqScan(id);
                            int finalI = i;
                            new Handler().postDelayed(() -> {
                                PaCtl.build().closePAById(id);
                            }, 300);
                        }
                    }

                    //刷新页面
                    mCfgTraceChildFragment.setcfgEnable(-1, false);
                    refreshTraceBtn();
                    Util.showToast("正在停止扫频， 请稍等", Toast.LENGTH_LONG);

                    new Handler().postDelayed(() -> {
                        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                            if (MainActivity.getInstance().getDeviceList().get(i).getWorkState() == GnbBean.DW_State.FREQ_SCAN) {
                                MainActivity.getInstance().getDeviceList().get(i).setWorkState(GnbBean.DW_State.TRACE);
                                int type = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName().contains(devA) ? 0 : 1;
                                MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.freq_stoped));
                            }
                            mFreqFragment.setFreqWorkState(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId(), false); // 避免命令下发不响应，这里也做一次清除状态
                        }
                        startRunWork(); //开始定位
                    }, 8000);
                } else {
                    //刷新页面
                    mCfgTraceChildFragment.setcfgEnable(-1, false);
                    HistoryBean_3g758cx historyBean = mCfgTraceChildFragment.getHistoryBean();
                    String imsi;
                    LinkedList<ArfcnPciBean> list;
                    if (mCfgTraceChildFragment.isYiguangChecked()) {
                        imsi = historyBean.getImsi_yg();
                        list = historyBean.getYgList();
                    } else {
                        imsi = historyBean.getImsi_ld();
                        list = historyBean.getLdList();
                    }

                    if (MainActivity.ifDebug) setAllLogicTraceUtilImsiTest(imsi);
                    else setAllLogicTraceUtilImsi(imsi);

                    putListIntoMap_3g758cx(list);

                    startRunWork();//直接开始定位
                    refreshTraceBtn();
                }
                //存入历史记录
                storeIntoHistory();
            } else {
                mCfgTraceChildFragment.resetArfcnList();
            }
        } else {
            mCfgTraceChildFragment.resetArfcnList();
        }
    }


    //把页面上移广、联电的列表分配到map中
    private void putListIntoMap_3g758cx(LinkedList<ArfcnPciBean> list) {
        LinkedList<ArfcnPciBean> TD1 = new LinkedList<>();
        LinkedList<ArfcnPciBean> TD2 = new LinkedList<>();
        LinkedList<ArfcnPciBean> TD3 = new LinkedList<>();
        LinkedList<ArfcnPciBean> TD4 = new LinkedList<>();
        LinkedList<ArfcnPciBean> TD5 = new LinkedList<>();
        LinkedList<ArfcnPciBean> TD6 = new LinkedList<>();
        LinkedList<ArfcnPciBean> TD7 = new LinkedList<>();
        LinkedList<ArfcnPciBean> TD8 = new LinkedList<>();
        LinkedList<ArfcnPciBean> TD9 = new LinkedList<>();
        LinkedList<ArfcnPciBean> TD10 = new LinkedList<>();
        LinkedList<ArfcnPciBean> TD11 = new LinkedList<>();
        LinkedList<ArfcnPciBean> TD12 = new LinkedList<>();
        LinkedList<ArfcnPciBean> N78 = new LinkedList<>();
        LinkedList<ArfcnPciBean> B3 = new LinkedList<>();
        for (ArfcnPciBean arfcnPciBean : list) {
            String arfcn = arfcnPciBean.getArfcn();
            int band;

            if (DeviceUtil.arfcnIsNr(arfcn)) {
                band = NrBand.earfcn2band(Integer.parseInt(arfcn));
                switch (band) {
                    case 1:
                        TD3.add(arfcnPciBean);
                        break;
                    case 3:     //次频点最后处理
                        TD9.add(arfcnPciBean);
                        break;
                    case 28:
                        TD5.add(arfcnPciBean);
                        break;
                    case 41:
                        TD7.add(arfcnPciBean);
                        break;
                    case 78:    //次频点最后处理
                        N78.add(arfcnPciBean);
                        break;
                    case 79:
                        TD1.add(arfcnPciBean);
                }
            } else {
                band = LteBand.earfcn2band(Integer.parseInt(arfcn));
                switch (band) {
                    case 1:
                        TD2.add(arfcnPciBean);
                        break;
                    case 3:
                        B3.add(arfcnPciBean);
                        break;
                    case 5:
                    case 8:
                        TD11.add(arfcnPciBean);
                        break;
                    case 34:
                    case 41:
                        TD12.add(arfcnPciBean);
                        break;
                    case 39:
                        TD10.add(arfcnPciBean);
                        break;
                    case 40:
                        if (TD8.size() <= TD6.size()) TD8.add(arfcnPciBean);
                        else TD6.add(arfcnPciBean);
                        break;
                }
            }
        }

        //处理次频点N78
        if (N78.size() > 0) {
            if (TD5.size() > 0) {   //全放主频段
                TD1.addAll(N78);
            } else {   //平分
                for (int i = 0; i < N78.size(); i++) {
                    ArfcnPciBean arfcnPciBean = N78.get(i);
                    if (i < N78.size() / 2.0f) {
                        TD1.add(arfcnPciBean);
                    } else TD5.add(arfcnPciBean);
                }
            }
        }
        //处理次频点B3
        if (B3.size() > 0) {
            if (TD9.size() > 0) {   //全放主频段
                TD4.addAll(B3);
            } else {   //平分
                for (int i = 0; i < B3.size(); i++) {
                    ArfcnPciBean arfcnPciBean = B3.get(i);
                    if (i < B3.size() / 2.0f) {
                        TD4.add(arfcnPciBean);
                    } else TD9.add(arfcnPciBean);
                }
            }
        }

        AppLog.D("TD1=" + TD1.toString() + "  TD2=" + TD2.toString() + "  TD3=" + TD3.toString() + " TD4=" +
                TD4.toString() + " TD5=" + TD5.toString() + " TD6=" + TD6.toString() + " TD7=" + TD7.toString() + " TD8=" +
                TD8.toString() + " TD9=" + TD9.toString() + " TD10=" + TD10.toString() + " TD11=" + TD11.toString() + " TD12=" + TD12.toString());
        arfcnBeanHashMap.put("TD1", TD1);
        arfcnBeanHashMap.put("TD2", TD2);
        arfcnBeanHashMap.put("TD3", TD3);
        arfcnBeanHashMap.put("TD4", TD4);
        arfcnBeanHashMap.put("TD5", TD5);
        arfcnBeanHashMap.put("TD6", TD6);
        arfcnBeanHashMap.put("TD7", TD7);
        arfcnBeanHashMap.put("TD8", TD8);
        arfcnBeanHashMap.put("TD9", TD9);
        arfcnBeanHashMap.put("TD10", TD10);
        arfcnBeanHashMap.put("TD11", TD11);
        arfcnBeanHashMap.put("TD12", TD12);
    }

    private void setAllLogicTraceUtilImsi(String imsi) {
        AppLog.D("setAllLogicTraceUtilImsi=" + imsi);
        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
            TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil();
            traceUtil.setImsi(0, imsi);
            traceUtil.setImsi(1, imsi);
            traceUtil.setImsi(2, imsi);
            traceUtil.setImsi(3, imsi);
        }
    }

    private void setAllLogicTraceUtilImsiTest(String imsi) {
        AppLog.D("setAllLogicTraceUtilImsiTest");
        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
            TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil();
            int logicIndex = DeviceUtil.getLogicIndexById(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId());
            if (logicIndex == 0) {
                traceUtil.setImsi(0, imsi);
                traceUtil.setImsi(1, mCfgTraceChildFragment.getActv_imsi2().getText().toString());
                traceUtil.setImsi(2, mCfgTraceChildFragment.getActv_imsi3().getText().toString());
                traceUtil.setImsi(3, mCfgTraceChildFragment.getActv_imsi4().getText().toString());
            } else if (logicIndex == 1) {
                traceUtil.setImsi(0, mCfgTraceChildFragment.getActv_imsi5().getText().toString());
                traceUtil.setImsi(1, mCfgTraceChildFragment.getActv_imsi6().getText().toString());
                traceUtil.setImsi(2, mCfgTraceChildFragment.getActv_imsi7().getText().toString());
                traceUtil.setImsi(3, mCfgTraceChildFragment.getActv_imsi8().getText().toString());
            } else if (logicIndex == 2) {
                traceUtil.setImsi(0, mCfgTraceChildFragment.getActv_imsi9().getText().toString());
                traceUtil.setImsi(1, mCfgTraceChildFragment.getActv_imsi10().getText().toString());
                traceUtil.setImsi(2, mCfgTraceChildFragment.getActv_imsi11().getText().toString());
                traceUtil.setImsi(3, mCfgTraceChildFragment.getActv_imsi12().getText().toString());
            }
        }
    }

    public void stopTraceDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> {
            AppLog.D("stopTraceDialog click stop");
            isClickStop = true;
            txValue = -1;
            isChangeImsi1 = false;

            isChangeImsi2 = false;
            isChangeImsi3 = false;
            autoRun1 = false;
            autoRun2 = false;
            autoRun3 = false;
            autoRun4 = false;
            autoRun5 = false;
            autoRun6 = false;
            autoRun7 = false;
            autoRun8 = false;
            autoRun9 = false;
            autoRun10 = false;
            autoRun11 = false;
            autoRun12 = false;
            mCfgTraceChildFragment.isAutoMode = false;
            mCfgTraceChildFragment.isAutoModeFreqRunning = false;
            mCfgTraceChildFragment.resetArfcnList();

            for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                mCfgTraceChildFragment.getTv_do_btn().setText(getString(R.string.stoping));
                int type = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName().contains(devA) ? 0 : 1;
                final String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
                int logicIndex = DeviceUtil.getLogicIndexById(id);
                if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().isEnable(DWProtocol.CellId.FOURTH) &&
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(DWProtocol.CellId.FOURTH) == GnbBean.DW_State.TRACE) {
                    String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(DWProtocol.CellId.FOURTH);
                    if (!arfcn.isEmpty()) {
//                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell3_stoping));
                        updateLeftTvState(type, 50, logicIndex, 3, getString(R.string.stoping), false);

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(DWProtocol.CellId.FOURTH, System.currentTimeMillis());
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(DWProtocol.CellId.FOURTH, GnbBean.DW_State.STOP);

                        if (Integer.parseInt(arfcn) > 100000)
                            MessageController.build().stopDWNrTrace(id, DWProtocol.CellId.FOURTH);
                        else
                            MessageController.build().stopDWLteTrace(id, DWProtocol.CellId.FOURTH);
                    }
                } else {
                    if (MainActivity.getInstance().getDeviceList().get(i).getRsp().getFourthState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
//                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell3_stoping));
                        updateLeftTvState(type, 50, logicIndex, 3, getString(R.string.stoping), false);

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(DWProtocol.CellId.FOURTH, System.currentTimeMillis());
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(DWProtocol.CellId.FOURTH, GnbBean.DW_State.STOP);
                        String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(DWProtocol.CellId.FOURTH);
                        if (!arfcn.isEmpty()) {
                            if (Integer.parseInt(arfcn) > 100000)
                                MessageController.build().stopDWNrTrace(id, DWProtocol.CellId.FOURTH);
                            else
                                MessageController.build().stopDWLteTrace(id, DWProtocol.CellId.FOURTH);
                        }
                    } else if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(DWProtocol.CellId.FOURTH) == GnbBean.DW_State.GNB_CFG) {
                        updateLeftTvState(type, 50, logicIndex, 3, "结束中", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(DWProtocol.CellId.FOURTH, GnbBean.DW_State.STOP);
                    } else {
                        updateLeftTvState(type, 0, logicIndex, 3, "空闲", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(DWProtocol.CellId.FOURTH, GnbBean.DW_State.IDLE);
                    }
                }

                if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().isEnable(DWProtocol.CellId.THIRD) &&
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(DWProtocol.CellId.THIRD) == GnbBean.DW_State.TRACE) {
                    String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(DWProtocol.CellId.THIRD);
                    if (!arfcn.isEmpty()) {
                        int finalI = i;
                        new Handler().postDelayed(() -> {
//                            MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell2_stoping));
                            updateLeftTvState(type, 50, logicIndex, 2, getString(R.string.stoping), false);

                            MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setAtCmdTimeOut(DWProtocol.CellId.THIRD, System.currentTimeMillis());
                            MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(DWProtocol.CellId.THIRD, GnbBean.DW_State.STOP);

                            if (Integer.parseInt(arfcn) > 100000)
                                MessageController.build().stopDWNrTrace(id, DWProtocol.CellId.THIRD);
                            else
                                MessageController.build().stopDWLteTrace(id, DWProtocol.CellId.THIRD);
                        }, 300);
                    }
                } else {
                    if (MainActivity.getInstance().getDeviceList().get(i).getRsp().getThirdState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell2_stoping));
                        updateLeftTvState(type, 50, logicIndex, 2, getString(R.string.stoping), false);

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(DWProtocol.CellId.THIRD, System.currentTimeMillis());
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(DWProtocol.CellId.THIRD, GnbBean.DW_State.STOP);

                        String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(DWProtocol.CellId.THIRD);
                        if (!arfcn.isEmpty()) {
                            if (Integer.parseInt(arfcn) > 100000)
                                MessageController.build().stopDWNrTrace(id, DWProtocol.CellId.THIRD);
                            else
                                MessageController.build().stopDWLteTrace(id, DWProtocol.CellId.THIRD);
                        }

                    } else if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(DWProtocol.CellId.THIRD) == GnbBean.DW_State.GNB_CFG) {
                        updateLeftTvState(type, 50, logicIndex, 2, "结束中", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(DWProtocol.CellId.THIRD, GnbBean.DW_State.STOP);
                    } else {
                        updateLeftTvState(type, 0, logicIndex, 2, "空闲", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(DWProtocol.CellId.THIRD, GnbBean.DW_State.IDLE);
                    }
                }

                if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().isEnable(DWProtocol.CellId.SECOND) &&
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(DWProtocol.CellId.SECOND) == GnbBean.DW_State.TRACE) {
                    String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(DWProtocol.CellId.SECOND);
                    if (!arfcn.isEmpty()) {
                        int finalI = i;
                        new Handler().postDelayed(() -> {
                            MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell1_stoping));
                            updateLeftTvState(type, 50, logicIndex, 1, getString(R.string.stoping), false);

                            MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setAtCmdTimeOut(DWProtocol.CellId.SECOND, System.currentTimeMillis());
                            MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(DWProtocol.CellId.SECOND, GnbBean.DW_State.STOP);

                            if (Integer.parseInt(arfcn) > 100000)
                                MessageController.build().stopDWNrTrace(id, DWProtocol.CellId.SECOND);
                            else
                                MessageController.build().stopDWLteTrace(id, DWProtocol.CellId.SECOND);
                            refreshTraceBtn();
                        }, 600);
                    }
                } else {
                    if (MainActivity.getInstance().getDeviceList().get(i).getRsp().getSecondState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell1_stoping));
                        updateLeftTvState(type, 50, logicIndex, 1, getString(R.string.stoping), false);

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(DWProtocol.CellId.SECOND, System.currentTimeMillis());
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(DWProtocol.CellId.SECOND, GnbBean.DW_State.STOP);
                        String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(DWProtocol.CellId.SECOND);
                        if (!arfcn.isEmpty()) {
                            if (Integer.parseInt(arfcn) > 100000)
                                MessageController.build().stopDWNrTrace(id, DWProtocol.CellId.SECOND);
                            else
                                MessageController.build().stopDWLteTrace(id, DWProtocol.CellId.SECOND);
                        }

                    } else if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(DWProtocol.CellId.SECOND) == GnbBean.DW_State.GNB_CFG) {
                        updateLeftTvState(type, 50, logicIndex, 1, "结束中", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(DWProtocol.CellId.SECOND, GnbBean.DW_State.STOP);
                    } else {
                        updateLeftTvState(type, 0, logicIndex, 1, "空闲", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(DWProtocol.CellId.SECOND, GnbBean.DW_State.IDLE);
                    }
                }

                if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().isEnable(DWProtocol.CellId.FIRST) &&
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(DWProtocol.CellId.FIRST) == GnbBean.DW_State.TRACE) {
                    String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(DWProtocol.CellId.FIRST);
                    if (!arfcn.isEmpty()) {
                        int finalI = i;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell0_stoping));
                                updateLeftTvState(type, 50, logicIndex, 0, getString(R.string.stoping), false);

                                MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setAtCmdTimeOut(DWProtocol.CellId.FIRST, System.currentTimeMillis());
                                MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(DWProtocol.CellId.FIRST, GnbBean.DW_State.STOP);
                                if (Integer.parseInt(arfcn) > 100000)
                                    MessageController.build().stopDWNrTrace(id, DWProtocol.CellId.FIRST);
                                else
                                    MessageController.build().stopDWLteTrace(id, DWProtocol.CellId.FIRST);
                                refreshTraceBtn();
                            }
                        }, 900);
                    }
                } else {
                    if (MainActivity.getInstance().getDeviceList().get(i).getRsp().getFirstState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell0_stoping));
                        updateLeftTvState(type, 50, logicIndex, 0, getString(R.string.stoping), false);

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(DWProtocol.CellId.FIRST, System.currentTimeMillis());
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(DWProtocol.CellId.FIRST, GnbBean.DW_State.STOP);
                        int finalI1 = i;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                String arfcn = MainActivity.getInstance().getDeviceList().get(finalI1).getTraceUtil().getArfcn(DWProtocol.CellId.FIRST);
                                if (!arfcn.isEmpty()) {
                                    if (Integer.parseInt(arfcn) > 100000)
                                        MessageController.build().stopDWNrTrace(id, DWProtocol.CellId.FIRST);
                                    else
                                        MessageController.build().stopDWLteTrace(id, DWProtocol.CellId.FIRST);
                                }
                            }
                        }, 900);

                    } else if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(DWProtocol.CellId.FIRST) == GnbBean.DW_State.GNB_CFG) {
                        updateLeftTvState(type, 50, logicIndex, 0, "结束中", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(DWProtocol.CellId.FIRST, GnbBean.DW_State.STOP);
                    } else {
                        updateLeftTvState(type, 0, logicIndex, 0, "空闲", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(DWProtocol.CellId.FIRST, GnbBean.DW_State.IDLE);
                    }
                }
//                    iv_add_remove_black.setVisibility(View.GONE);
                refreshTraceBtn();
                PaCtl.build().closePAById(id);
                //连下两次才能结束
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PaCtl.build().closePAById(id);
                    }
                }, 1000);
            }

            MainActivity.getInstance().closeCustomDialog();
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
        int logicIndex = DeviceUtil.getLogicIndexById(id);
        int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains(devA) ? 0 : 1;
        final TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil();
        if (rsp != null) {
            if (rsp.getCmdType() == DWProtocol.UI_2_gNB_SET_BLACK_UE_LIST || rsp.getCmdType() == DWProtocol.UI_2_gNB_START_LTE_BLACK_UE_LIST) {
                if (isClickStop) {
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.IDLE);
                    updateLeftTvState(type, 0, logicIndex, rsp.getCellId(), "空闲", false);
                    refreshTraceBtn();
                    return;
                }
//                if (isResetBlack) return;
                if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                    // 发配置定位参数指令
                    int traceTac = PrefUtil.build().getTac();
                    int maxTac = traceTac + DWProtocol.MAX_TAC_NUM;
                    // 4G tac 不能大于65535， 在这4G 5G共用相同tac，因此做相同处理
                    if (maxTac > 65535) {
                        traceTac = 1234;
                        maxTac = traceTac + DWProtocol.MAX_TAC_NUM;
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

                    // 第二步，配置频点参数
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.GNB_CFG);

                    updateLeftTvState(type, 30, logicIndex, rsp.getCellId(), getString(R.string.cfging), false);
                    refreshTraceBtn();
                    if (Integer.parseInt(arfcn[0]) > 100000)
                        PaCtl.build().initPA(id, arfcn[0], DeviceUtil.getMainChannelNumByarfcn_3G758_CX(arfcn[0]));
                    else
                        PaCtl.build().initLtePA(id, arfcn[0], DeviceUtil.getMainChannelNumByarfcn_3G758_CX(arfcn[0]));
//
                    int finalTraceTac = traceTac;
                    int finalMaxTac = maxTac;
                    new Handler().postDelayed(() -> {
                        int iArfcn = Integer.parseInt(arfcn[0]);
//                            int band = iArfcn > 100000 ? NrBand.earfcn2band(iArfcn) : LteBand.earfcn2band(iArfcn);
                        //int time_offset = Integer.parseInt(PrefUtil.build().getValue("sync_time_offset_" + (iArfcn > 100000 ? "N" : "B") + band, "0").toString());
//                            int time_offset = GnbCity.build().getTimimgOffset(arfcn[0]);
                        /*
                        4g:
                        TDD：10000000-GPS时偏
                        FDD：0
                        5g:
                        移动：GPS时偏
                        联通电信：0
                        得到多少就下发多少，不加减了
                        */
                        int time_offset = 0;
                        int band;
                        if (iArfcn > 100000) {
                            band = NrBand.earfcn2band(iArfcn);
                            if (band == 28 || band == 41 || band == 79)
                                time_offset = GnbCity.build().getTimingOffset("5G");
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
//                        int time_offset = Integer.parseInt(PrefUtil.build().getValue("sync_time_offset", "0").toString());
                        if (iArfcn > 100000) {
                            MessageController.build().initDWNrTrace(id, rsp.getCellId(), finalTraceTac, plmn, arfcn[0], pci[0], ue_max_pwr,
                                    time_offset, 10, air_sync, "0", 9,
                                    cid, ssbBitmap, bandwidth, cfr, swap_rf, 15, -70, mob_reject_code, split_arfcn_dl);
                        } else {
//                            int band = LteBand.earfcn2band(Integer.parseInt(arfcn[0]));
//                            if (MainActivity.getInstance().getDeviceList().get(0).getRsp().getGpsSyncState() == GnbStateRsp.Gps.SUCC && band >= 33 && band <= 46) {
//                                time_offset = 10000000;
//                            }

                            MessageController.build().initDWLteTrace(id, rsp.getCellId(), finalTraceTac, plmn, arfcn[0], pci[0], ue_max_pwr,
                                    time_offset, 10, air_sync, "0", 9,
                                    cid, ssbBitmap, bandwidth, cfr, swap_rf, 15, -70, mob_reject_code, split_arfcn_dl);
                        }
//                            getArfcnList(rsp.getCellId(), arfcn[0]);
                        int finalTime_offset = time_offset;
                        new Handler().postDelayed(() -> {
                            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setSaveOpLog(rsp.getCellId(), false);
                            String msg = "plmn = " + plmn + ", arfcn = " + arfcn[0] + ", pci = " + pci[0] + ", cid = " + cid
                                    + ", time offset = " + finalTime_offset;
                            MessageController.build().writeDWOpLog(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + rsp.getCellId() + "\t\t定位参数：" + msg);
                        }, 300);
                    }, 300);
//                    updateParam(rsp.getCellId(), arfcn[0], pci[0]);   //更新左边通道状态
                } else {
                    //如果是主B40失败
                    String arfcn = traceUtil.getArfcn(rsp.getCellId());
                    if (isMainB40 && arfcn.length() < 6
                            && LteBand.earfcn2band(Integer.parseInt(arfcn)) == 40) {
                        mainB40Success = 0;
                        isMainB40 = false;
                        synchronized (lockB40) {
                            lockB40.notify(); // 通知子线程
                        }
                    }
                    //如果是主B3失败
                    if (isMainB3 && arfcn.length() < 6
                            && LteBand.earfcn2band(Integer.parseInt(arfcn)) == 3) {
                        mainB3Success = 0;
                        isMainB3 = false;
                        synchronized (lockB3) {
                            lockB3.notify(); // 通知子线程
                        }
                    }
                    String s = getCellStr(logicIndex, rsp.getCellId());
                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.cell) + s + getString(R.string.cfg_trace_fail));
                    updateLeftTvState(type, 30, logicIndex, rsp.getCellId(), getString(R.string.open_fail), true);
                    Util.showToast(getString(R.string.cell) + s + getString(R.string.trace_start_fail));

                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.IDLE);

//                    startSecondTrace(id, indexById, rsp.getCellId(), true); //通道定位失败，开始下一通道的定位流程

                    refreshTraceBtn();
                }
            }
        }
    }

    private String getCellStr(int logicIndex, int cell_id) {
        int channelNum = DeviceUtil.getChannelNum(logicIndex, cell_id);
        if (channelNum == 1) return getString(R.string.first);
        else if (channelNum == 2) return getString(R.string.second);
        else if (channelNum == 3) return getString(R.string.third);
        else if (channelNum == 4) return getString(R.string.fourth);
        else if (channelNum == 5) return "五";
        else if (channelNum == 6) return "六";
        else if (channelNum == 7) return "七";
        else if (channelNum == 8) return "八";
        else if (channelNum == 9) return "九";
        else if (channelNum == 10) return "十";
        else if (channelNum == 11) return "十一";
        else if (channelNum == 12) return "十二";
        return "零";
    }

    private void setCountNum(int logicIndex, int value) {
        switch (logicIndex) {
            case 0:
                countFirst = value;
                countSecond = value;
                countThird = value;
                countFourth = value;
                break;
            case 1:
                countFifth = value;
                countSixth = value;
                countSeventh = value;
                countEighth = value;
                break;
            case 2:
                countNinth = value;
                countTenth = value;
                countEleventh = value;
                countTwelfth = value;
        }
    }

    private String getErrInfo(int logicIndex, int cellId, int code) {

        String info = getString(R.string.cell) + getCellStr(logicIndex, cellId);
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

    public void onSetGnbRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;
        int logicIndex = DeviceUtil.getLogicIndexById(id);

        int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains(devA) ? 0 : 1;
        final TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil();
        if (rsp != null) {
            if (rsp.getCmdType() == DWProtocol.UI_2_gNB_CFG_gNB || rsp.getCmdType() == DWProtocol.UI_2_gNB_LTE_CFG_gNB) { //UI_2_gNB_CFG_gNB = 10 配置频点参数
                if (isClickStop) {
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.IDLE);
                    updateLeftTvState(type, 0, logicIndex, rsp.getCellId(), "空闲", false);
                    refreshTraceBtn();
                    return;
                }

                if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                    //如果是主B40成功
                    String arfcn = traceUtil.getArfcn(rsp.getCellId());
                    if (isMainB40 && arfcn.length() < 6
                            && LteBand.earfcn2band(Integer.parseInt(arfcn)) == 40) {
                        mainB40Success = 1;
                        isMainB40 = false;
                        synchronized (lockB40) {
                            lockB40.notify(); // 通知子线程
                        }
                    }
                    //如果是主B3成功
                    if (isMainB3 && arfcn.length() < 6
                            && LteBand.earfcn2band(Integer.parseInt(arfcn)) == 3) {
                        mainB3Success = 1;
                        isMainB3 = false;
                        synchronized (lockB3) {
                            lockB3.notify(); // 通知子线程
                        }
                    }
                    AppLog.I("onSetGnbRsp indexById = " + indexById + " cell_id = " + rsp.getCellId() + " workState = " + traceUtil.getWorkState(rsp.getCellId())
                            + " arfcn = " + traceUtil.getArfcn(rsp.getCellId()));
                    if (/*!traceUtil.isTacChange(rsp.getCellId())
                            && */traceUtil.getWorkState(rsp.getCellId()) == GnbBean.DW_State.GNB_CFG) {
                        updateLeftTvState(type, 50, logicIndex, rsp.getCellId(), "配置中", false);
                        //第三步.设置功率衰减
                        //MessageController.build().setTraceType(id, DWProtocol.TraceType.STARTTRACE);
                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.CFG_TRACE);
                        AppLog.I("onSetGnbRsp setDWTxPwrOffset");
                        MessageController.build().setDWTxPwrOffset(id, rsp.getCellId(), Integer.parseInt(traceUtil.getArfcn(rsp.getCellId())), 0);
                        //2024.6.7在12号消息返回会下发，把这里去掉
//                        if (arfcn.length() > 5) {
//                            PaCtl.build().openPA(id, traceUtil.getArfcn(rsp.getCellId()), 0);
//                        } else {
//                            PaCtl.build().openLtePA(id, traceUtil.getArfcn(rsp.getCellId()), 0);
//                        }
                    }
                } else {
                    //如果是主B40失败
                    String arfcn = traceUtil.getArfcn(rsp.getCellId());
                    if (isMainB40 && arfcn.length() < 6
                            && LteBand.earfcn2band(Integer.parseInt(arfcn)) == 40) {
                        mainB40Success = 0;
                        isMainB40 = false;
                        synchronized (lockB40) {
                            lockB40.notify(); // 通知子线程
                        }
                    }
                    //如果是主B3失败
                    if (isMainB3 && arfcn.length() < 6
                            && LteBand.earfcn2band(Integer.parseInt(arfcn)) == 3) {
                        mainB3Success = 0;
                        isMainB3 = false;
                        synchronized (lockB3) {
                            lockB3.notify(); // 通知子线程
                        }
                    }
                    switch (rsp.getRspValue()) {
                        case 6:
                            String errInfo = getErrInfo(DeviceUtil.getLogicIndexById(id), rsp.getCellId(), rsp.getRspValue());
                            ArfcnPciBean bean;
                            int TDNum = DeviceUtil.getChannelNum(logicIndex, rsp.getCellId());
                            LinkedList<ArfcnPciBean> linkedList = arfcnBeanHashMap.get("TD" + TDNum);
                            if (linkedList == null) return;
                            switch (rsp.getCellId()) {
                                case 0:
                                    if (linkedList.size() == 1) {
                                        MainActivity.getInstance().updateSteps(0, StepBean.State.fail, errInfo);
                                        updateLeftTvState(0, 50, logicIndex, rsp.getCellId(), getString(R.string.open_fail), true);
                                        Util.showToast(errInfo, Toast.LENGTH_LONG);
//                                        mCfgTraceChildFragment.setcfgEnable(-1, true);
                                        //改为失败的时候不置为空闲，必须要下发停止之后才置为空闲,防止基带板失败之后还是一直在跑
//                                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.IDLE);
                                        refreshTraceBtn();
                                    } else {
                                        autoArfcnIndex0++;
                                        if (autoArfcnIndex0 >= linkedList.size())
                                            autoArfcnIndex0 = 0;
                                        bean = linkedList.get(autoArfcnIndex0);
                                        AppLog.I("cell 0 sync fail reset arfcn/pci = " + bean.toString());
                                        updateParamAndStart(id, bean, rsp.getCellId());
                                        return; //不执行最后的reset
                                    }
                                    break;
                                case 1:
                                    if (linkedList.size() == 1) {
                                        MainActivity.getInstance().updateSteps(0, StepBean.State.fail, errInfo);
                                        updateLeftTvState(0, 50, logicIndex, rsp.getCellId(), getString(R.string.open_fail), true);
                                        Util.showToast(errInfo, Toast.LENGTH_LONG);
//                                        mCfgTraceChildFragment.setcfgEnable(-1, true);
                                        //改为失败的时候不置为空闲，必须要下发停止之后才置为空闲,防止基带板失败之后还是一直在跑
//                                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.IDLE);
                                        refreshTraceBtn();
                                    } else {
                                        autoArfcnIndex1++;
                                        if (autoArfcnIndex1 >= linkedList.size())
                                            autoArfcnIndex1 = 0;
                                        bean = linkedList.get(autoArfcnIndex1);
                                        AppLog.I("cell 1 sync fail reset arfcn/pci = " + bean.toString());
                                        updateParamAndStart(id, bean, rsp.getCellId());
                                        return;
                                    }
                                    break;
                                case 2:
                                    if (linkedList.size() == 1) {
                                        MainActivity.getInstance().updateSteps(0, StepBean.State.fail, errInfo);
                                        updateLeftTvState(0, 50, logicIndex, rsp.getCellId(), getString(R.string.open_fail), true);
                                        Util.showToast(errInfo, Toast.LENGTH_LONG);
//                                        mCfgTraceChildFragment.setcfgEnable(-1, true);
                                        //改为失败的时候不置为空闲，必须要下发停止之后才置为空闲,防止基带板失败之后还是一直在跑
//                                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.IDLE);
                                        refreshTraceBtn();
                                    } else {
                                        autoArfcnIndex2++;
                                        if (autoArfcnIndex2 >= linkedList.size())
                                            autoArfcnIndex2 = 0;
                                        bean = linkedList.get(autoArfcnIndex2);
                                        AppLog.I("cell 2 sync fail reset arfcn/pci = " + bean.toString());
                                        updateParamAndStart(id, bean, rsp.getCellId());
                                        return;
                                    }
                                    break;
                                case 3:
                                    if (linkedList.size() == 1) {
                                        MainActivity.getInstance().updateSteps(0, StepBean.State.fail, errInfo);
                                        updateLeftTvState(0, 50, logicIndex, rsp.getCellId(), getString(R.string.open_fail), true);
                                        Util.showToast(errInfo, Toast.LENGTH_LONG);
//                                        mCfgTraceChildFragment.setcfgEnable(-1, true);
                                        //改为失败的时候不置为空闲，必须要下发停止之后才置为空闲,防止基带板失败之后还是一直在跑
//                                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.IDLE);
                                        refreshTraceBtn();
                                    } else {
                                        autoArfcnIndex3++;
                                        if (autoArfcnIndex3 >= linkedList.size())
                                            autoArfcnIndex3 = 0;
                                        bean = linkedList.get(autoArfcnIndex3);
                                        AppLog.I("cell 3 sync fail reset arfcn/pci = " + bean.toString());
                                        updateParamAndStart(id, bean, rsp.getCellId());
                                        return;
                                    }
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
                            break;
                        default:
                            mCfgTraceChildFragment.setcfgEnable(-1, true);
                            //改为失败的时候不置为空闲，必须要下发停止之后才置为空闲,防止基带板失败之后还是一直在跑
//                            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.IDLE);
                            String info = getErrInfo(logicIndex, rsp.getCellId(), rsp.getRspValue());
                            MainActivity.getInstance().updateSteps(0, StepBean.State.fail, info);
                            updateLeftTvState(0, 50, logicIndex, rsp.getCellId(), getString(R.string.open_fail), true);
                            Util.showToast(info);
                            refreshTraceBtn();
                            break;
                    }
                    resetArfcnList();
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.IDLE);
                }
            }
        }
    }

    //更新配置并调用启动黑名单开始
    private void updateParamAndStart(String id, ArfcnPciBean bean, int cell_id) {
        updateParamAndStart(id, bean.getArfcn(), bean.getPci(), cell_id);
    }

    //1:N78\79 2:B1 3:N1 4:B3 5:N28\N78 6:B40 7:N41 8:B40
// 9:N3 10:B39 11:B5\B8 12:B34\B41
    private void updateParamAndStart(String id, String arfcnStr, String pci, int cell_id) {
        AppLog.I("updateParamAndStart id = " + id + "arfcn= " + arfcnStr + " pci = " + pci + " cell_id = " + cell_id);
        if (arfcnStr.isEmpty()) return;
        int arfcn = Integer.parseInt(arfcnStr);
        int logicIndex = DeviceUtil.getLogicIndexById(id);
        int index = DeviceUtil.getIndexById(id);
        TraceUtil mLogicTraceUtil = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
        AppLog.D("updateParamAndStart logicIndex = " + logicIndex);
        if (PaCtl.build().is3G758) {
            String plmn_auto = DeviceUtil.getPlmn(arfcn);
            int band = arfcn > 100000 ? NrBand.earfcn2band(arfcn) : LteBand.earfcn2band(arfcn);
            int bandWidth = getBandWidth(arfcn > 100000, band);
            int ssb = getSsb(arfcn > 100000, band);
            mLogicTraceUtil.setBandWidth(cell_id, bandWidth);
            mLogicTraceUtil.setSsbBitmap(cell_id, ssb);

            //自动模式已经设过了
            if (!mCfgTraceChildFragment.isAutoMode) {   //不是自动模式
                if (mCfgTraceChildFragment.isYiguangChecked()) {   //手动模式判定方式
                    String imsi = mCfgTraceChildFragment.getHistoryBean().getImsi_yg();
                    mLogicTraceUtil.setPlmn(cell_id, imsi.substring(0, 5));
                } else {
                    String imsi = mCfgTraceChildFragment.getHistoryBean().getImsi_ld();
                    mLogicTraceUtil.setPlmn(cell_id, imsi.substring(0, 5));
                }
            }
            mLogicTraceUtil.setArfcn(cell_id, arfcnStr);
            mLogicTraceUtil.setPci(cell_id, pci);
        }
        mCfgTraceChildFragment.setcfgEnable(-1, false);
        MainActivity.getInstance().getDeviceList().get(index).setTraceUtil(mLogicTraceUtil);

        //发送配置黑名单指令
        MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(cell_id, GnbBean.DW_State.BLACKLIST);

        sendBlackList(id, arfcn, cell_id);

    }


    private int getSsb(boolean isNr, int band) {
        if (isNr) {
            if (DeviceUtil.isTdd(isNr, band)) return 255;
            else return 240;
        } else return 128;
    }

    private int getBandWidth(boolean isNr, int band) {
        if (isNr) {
            if (DeviceUtil.MaxCellIdPerDevice == 2) {
                if (DeviceUtil.isTdd(isNr, band)) return 100;
                else return 20;
            } else if (DeviceUtil.MaxCellIdPerDevice == 4) {
                if (DeviceUtil.isTdd(isNr, band)) return 100;
                else return 20;
//                return 20;
            }
        } else return 5;
        return 5;
    }

    private void sendBlackList(String id, int arfcn, int cellId) {
        final List<UeidBean> blackList = new ArrayList<>();
        for (MyUeidBean bean : MainActivity.getInstance().getBlackList())
            blackList.add(bean.getUeidBean());
        if (arfcn < 100000)
            MessageController.build().setDWLteBlackList(id, cellId, blackList.size(), blackList);
        else
            MessageController.build().setDWNrBlackList(id, cellId, blackList.size(), blackList);
    }

    public void onsetDWTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;
        int logicIndex = DeviceUtil.getLogicIndexById(id);

        int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains(devA) ? 0 : 1;
        final TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil();
        if (rsp != null) {
            //int traceType = MessageController.build().getTraceType(id);
            int traceType = traceUtil.getWorkState(rsp.getCellId());
            AppLog.I("onsetDWTxPwrOffsetRsp get TraceType = " + traceType);
            if (rsp.getCmdType() == DWProtocol.UI_2_gNB_SET_TX_POWER_OFFSET) {
                if (isClickStop) {
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.IDLE);
                    updateLeftTvState(type, 0, logicIndex, rsp.getCellId(), "空闲", false);
                    refreshTraceBtn();
                    return;
                }
                if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                    if (traceType == GnbBean.DW_State.CFG_TRACE) {
                        //4、开pa
                        if (Integer.parseInt(traceUtil.getArfcn(rsp.getCellId())) > 100000)
                            PaCtl.build().openPA(id, traceUtil.getArfcn(rsp.getCellId()), DeviceUtil.getMainChannelNumByarfcn_3G758_CX(traceUtil.getArfcn(rsp.getCellId()))); // 开PA
                        else
                            PaCtl.build().openLtePA(id, traceUtil.getArfcn(rsp.getCellId()), DeviceUtil.getMainChannelNumByarfcn_3G758_CX(traceUtil.getArfcn(rsp.getCellId()))); // 开Lte PA
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 5、发开始定位指令
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setRsrp(rsp.getCellId(), 0);
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setTacChange(rsp.getCellId(), true);
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setEnable(rsp.getCellId(), true);
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                                //第四步.启动定位
                                updateLeftTvState(type, 70, logicIndex, rsp.getCellId(), getString(R.string.cfging), false);

                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.CFG_TRACE);
                                if (Integer.parseInt(traceUtil.getArfcn(rsp.getCellId())) > 100000) {
                                    MessageController.build().startDWNrTrace(id, rsp.getCellId(), 1, traceUtil.getImsi(rsp.getCellId()));
                                } else {
                                    MessageController.build().startDWLteTrace(id, rsp.getCellId(), 1, traceUtil.getImsi(rsp.getCellId()));
                                }
                            }
                        }, 300);
                    }
                } else {
                    //如果是主B40失败
                    String arfcn = traceUtil.getArfcn(rsp.getCellId());
                    if (isMainB40 && arfcn.length() < 6
                            && LteBand.earfcn2band(Integer.parseInt(arfcn)) == 40) {
                        mainB40Success = 0;
                        isMainB40 = false;
                        synchronized (lockB40) {
                            lockB40.notify(); // 通知子线程
                        }
                    }
                    //如果是主B3失败
                    if (isMainB3 && arfcn.length() < 6
                            && LteBand.earfcn2band(Integer.parseInt(arfcn)) == 3) {
                        mainB3Success = 0;
                        isMainB3 = false;
                        synchronized (lockB3) {
                            lockB3.notify(); // 通知子线程
                        }
                    }
                    String s = getCellStr(logicIndex, rsp.getCellId());
                    if (traceType == GnbBean.DW_State.CFG_TRACE) {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.cell) + s + getString(R.string.cfg_trace_fail));
                        updateLeftTvState(type, 70, logicIndex, rsp.getCellId(), getString(R.string.open_fail), true);
                        Util.showToast(getString(R.string.cell) + s + getString(R.string.trace_start_fail));

//                        startSecondTrace(id, indexById, rsp.getCellId(), true); //通道定位失败，开始下一通道的定位流程
                        refreshTraceBtn();
                    } else {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.cell) + s + getString(R.string.gain_fail));
                        Util.showToast(getString(R.string.gain_fail));
                        mCfgTraceChildFragment.setIsCloaseDistance(false);
                    }
                }
                //MessageController.build().setTraceType(id, DWProtocol.TraceType.TRACE);
            }
        }
    }


    public void onStartTraceRsp(String id, GnbTraceRsp rsp) {
        AppLog.I("onStartTraceRsp id=" + id + " isChangeImsi123 = " + isChangeImsi1 + isChangeImsi2 + isChangeImsi3 + " rsp=" + rsp);
        int index = MainActivity.getInstance().getIndexById(id);
        if (id.equals("666666")) index = 0; //模拟信号
        if (index == -1) return;

        int type = 0;
        TraceUtil traceUtil = new TraceUtil();
        int logicIndex = DeviceUtil.getLogicIndexById(id);
        if (!id.equals("666666"))
            type = MainActivity.getInstance().getDeviceList().get(index).getRsp().getDevName().contains(devA) ? 0 : 1;
        if (!id.equals("666666"))
            traceUtil = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
        if (id.equals("666666")) {
            traceUtil.setWorkState(0, GnbBean.DW_State.TRACE);
        }
        if (rsp.getCmdRsp() != null) {
            final int cell_id = rsp.getCmdRsp().getCellId();
            int channelNum = DeviceUtil.getChannelNum(logicIndex, cell_id);
            mHandler.removeMessages(channelNum + 10);
            mHandler.sendEmptyMessageDelayed(channelNum + 10, time_count * 1000L);
            //如果主B40失败跳过次B40,只走上面轮询逻辑，不走下面代码
            String value = traceUtil.getArfcn(cell_id);
            if (!value.isEmpty() && LteBand.earfcn2band(Integer.parseInt(value)) == 40 && mainB40Success == 0) {
                updateLeftTvState(type, 90, logicIndex, cell_id, getString(R.string.open_fail), true);
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(cell_id, GnbBean.DW_State.IDLE);
                MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.open_fail));
                Util.showToast(getString(R.string.cell) + "二次频点开启失败", Toast.LENGTH_LONG);
                refreshTraceBtn();
                return;
            }
            //如果主B3失败跳过次B3,只走上面轮询逻辑，不走下面代码
            if (!value.isEmpty() && LteBand.earfcn2band(Integer.parseInt(value)) == 3 && mainB3Success == 0) {
                updateLeftTvState(type, 90, logicIndex, cell_id, getString(R.string.open_fail), true);
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(cell_id, GnbBean.DW_State.IDLE);
                MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.open_fail));
                Util.showToast(getString(R.string.cell) + "四次频点开启失败", Toast.LENGTH_LONG);
                refreshTraceBtn();
                return;
            }
            if (rsp.getCmdRsp().getCmdType() == DWProtocol.UI_2_gNB_START_TRACE || rsp.getCmdRsp().getCmdType() == DWProtocol.UI_2_gNB_START_LTE_TRACE) {
                String s = getCellStr(logicIndex, cell_id);
                if (rsp.getCmdRsp().getRspValue() == DWProtocol.OAM_ACK_OK) {
                    if (isClickStop) {
                        if (rsp.getCmdRsp().getCmdType() == DWProtocol.UI_2_gNB_START_TRACE)
                            MessageController.build().stopDWLteTrace(id, cell_id);
                        else
                            MessageController.build().stopDWNrTrace(id, cell_id);
                        return;
                    }
                    MessageController.build().setDWTxPwrOffset(id, cell_id, Integer.parseInt(traceUtil.getArfcn(cell_id)), mCfgTraceChildFragment.isCloseDistance() ? -2 : 0);

                    //第五步.定位中，这里做判断，是设置状态为侦码中还是定位中
                    String imsi = traceUtil.getImsi(cell_id);

                    // 记住业务IMSI、频点、PCI, 为恢复做准备
//                    PrefUtil.build().putValue("last_work_cfg", imsi + "/" + traceUtil.getArfcn(cell_id) + "/" + traceUtil.getPci(cell_id));

//                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setEnable(cell_id, true);
                    traceUtil.setWorkState(cell_id, GnbBean.DW_State.TRACE);

                    // 刷新处于工作状态
                    freshDoWorkState(type, logicIndex, cell_id, imsi, traceUtil.getArfcn(cell_id), traceUtil.getPci(cell_id));


                    TraceUtil finalTraceUtil = traceUtil;
                    new Handler().postDelayed(() ->
                            MessageController.build().writeDWOpLog(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + cell_id
                                    + "\t\t开始定位，目标IMSI: " + finalTraceUtil.getImsi(cell_id)), 500);

//                    startSecondTrace(id, index, cell_id, false); //通道定位成功后再开始下一通道的定位流程

                    boolean isChangeImsi = logicIndex == 0 ? isChangeImsi1 : logicIndex == 1 ? isChangeImsi2 : isChangeImsi3;
                    if (isChangeImsi) {
                        if (logicIndex == 0)
                            isChangeImsi1 = false;
                        else if (logicIndex == 1)
                            isChangeImsi2 = false;
                        else if (logicIndex == 2)
                            isChangeImsi3 = false;
                        Util.showToast(getString(R.string.change_trace_success));
//                        if (cell_id < 3) {
//                            String arfcn = "";
//                            int cellID = cell_id;
//                            do {
//                                cellID++;
//                                if (traceUtil.getWorkState(cellID) == GnbBean.DW_State.TRACE)
//                                    arfcn = traceUtil.getArfcn(cellID);
//                                if (cellID == 3) break;
//                            } while (arfcn.isEmpty());
//                            if (!arfcn.isEmpty()) {
//                                setChangeImsi(logicIndex, cellID, arfcn, imsi);
//                            }
//                        }
                    }
                } else {
                    //如果是主B40失败
                    String arfcn = traceUtil.getArfcn(cell_id);
                    if (isMainB40 && arfcn.length() < 6
                            && LteBand.earfcn2band(Integer.parseInt(arfcn)) == 40) {
                        mainB40Success = 0;
                        isMainB40 = false;
                        synchronized (lockB40) {
                            lockB40.notify(); // 通知子线程
                        }
                    }
                    //如果是主B3失败
                    if (isMainB3 && arfcn.length() < 6
                            && LteBand.earfcn2band(Integer.parseInt(arfcn)) == 3) {
                        mainB3Success = 0;
                        isMainB3 = false;
                        synchronized (lockB3) {
                            lockB3.notify(); // 通知子线程
                        }
                    }
                    updateLeftTvState(type, 90, logicIndex, cell_id, getString(R.string.open_fail), true);
                    traceUtil.setWorkState(cell_id, GnbBean.DW_State.IDLE);

//                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.open_fail));

                    Util.showToast(getString(R.string.cell) + s + getString(R.string.trace_start_fail));
                }
                refreshTraceBtn();
            }
        } else { // IMSI上报及上号报值
            if (rsp.getCellId() != -1) {

                int cell_id = rsp.getCellId();
                String traceArfcn = traceUtil.getArfcn(cell_id);

                String traceImsi = traceUtil.getImsi(cell_id);
                String tracePci = traceUtil.getPci(cell_id);
                if (rsp.getRsrp() == 6666) {
                    traceArfcn = "666666";
                    traceImsi = "66666666666666";
                    tracePci = "666";
                }
                if (traceArfcn.isEmpty()) return;
                if (rsp.getRsrp() < 5 || traceImsi.isEmpty() || traceArfcn.equals("0") || tracePci.isEmpty())
                    return;

                List<String> rspImsiList = rsp.getImsiList();
                if (rspImsiList != null && rspImsiList.size() > 0) {
                    for (int i = 0; i < rspImsiList.size(); i++) {
                        String rspImsi = rspImsiList.get(i);
                        // 黑名单中的不显示
//                        if (MainActivity.getInstance().isInBlackList(rspImsi)) return;
                        int rsrp = rsp.getRsrp();
                        AppLog.I("traceImsi = " + traceImsi + ", rspImsi = " + rspImsi + "  rsrp = " + rsrp);

                        if (rsrp < 5) rsrp = -1;

                        if (traceImsi.equals(rspImsi)) {   //只有确定是定位的设备时才刷新页面
                            traceUtil.setRsrp(cell_id, rsrp);
                            ParseDataUtil.build().addDataToList(cell_id, rsrp);
                            // 刷新处于工作状态
                            freshDoWorkState(type, logicIndex, cell_id, traceUtil.getImsi(cell_id)
                                    , traceUtil.getArfcn(cell_id), traceUtil.getPci(cell_id));
                            setIsStartCatchHandlerNum(logicIndex, cell_id, true);    //上号

                            if (rsrp == 6666 || MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getRsrp(cell_id) < 11 || rsrp != 7) {  //特殊机型
                                if (!MainActivity.getInstance().enableSpecial && rsp.getPhone_type() > 4) {
                                    AppLog.D("特殊机型上号，开启特殊机型开关");
                                    MainActivity.getInstance().enableSpecial = true;
                                    if (mSettingFragment != null && mSettingFragment.getDwSettingFragment() != null)
                                        mSettingFragment.getDwSettingFragment().setEnableSpecialSw();
                                }
                            }

                            //数传定位配置给单兵
                            //先判断下单兵是否开启，开启了才发，否则清除消息1
                            //2024.6.6下单兵隐藏掉了开启数传
                            if (mTraceChildFragment.getSwitch_pef_pwr() != null && mTraceChildFragment.getSwitch_pef_pwr().isChecked()) {
                                if (!dataFwdBean.getArfcn().equals(traceArfcn) && rsp.getRsrp() != 0 && rsp.getRsrp() != -1) {  //如果频点和上次的不一致
                                    dataFwdBean.setArfcn(traceArfcn);
                                    dataFwdBean.setPci(Integer.parseInt(tracePci));
                                    dataFwdBean.setBandwithd(traceUtil.getBandWidth(rsp.getCellId()));
                                    dataFwdBean.setTime_offset(traceUtil.getTimingOffset(rsp.getCellId()));
                                    if (!mHandler.hasMessages(1)) {
                                        mHandler.sendEmptyMessage(1);
                                    }

                                    //切频点上号时触发
                                    int value = mTraceChildFragment.getSwitch_pef_pwr().isChecked() ? -50 : -84;
                                    MessageController.build().setDWPefPwr(id, cell_id, value);
                                }
                            } else {
                                mHandler.removeMessages(1);
                                dataFwdBean.setArfcn("");
                            }
                            //启动单兵
                            doDBWork(id, traceArfcn, tracePci);
                        }
                        boolean add = true;
                        for (int j = 0; j < mImsiList.size(); j++) {
                            if (mImsiList.get(j).getImsi().equals(rspImsi)/* && mImsiList.get(j).getArfcn().equals(traceArfcn)*/) {
                                add = false;
                                mImsiList.get(j).setArfcn(traceArfcn);
                                mImsiList.get(j).setPci(tracePci);
                                mImsiList.get(j).setRsrp(rsrp);
                                mImsiList.get(j).setUpCount(mImsiList.get(j).getUpCount() + 1);
                                mImsiList.get(j).setLatestTime(System.currentTimeMillis());
                                if (traceImsi.equals(rspImsi))
                                    mImsiList.get(j).setState(ImsiBean.State.IMSI_NOW);
                                else if (MainActivity.getInstance().isInBlackList(rspImsi))
                                    mImsiList.get(j).setState(ImsiBean.State.IMSI_BL);
                                break;
                            }
                        }
                        all_count++;
                        if (add) {
                            if (traceImsi.equals(rspImsi)) { // 当前定位IMSI
                                mImsiList.add(0, new ImsiBean(ImsiBean.State.IMSI_NOW, rspImsi, traceArfcn, tracePci, rsrp, System.currentTimeMillis(), cell_id));
                            } else if (MainActivity.getInstance().isInBlackList(rspImsi)) {
                                mImsiList.add(new ImsiBean(ImsiBean.State.IMSI_BL, rspImsi, traceArfcn, tracePci, rsrp, System.currentTimeMillis(), cell_id));
                            } else {
                                mImsiList.add(new ImsiBean(ImsiBean.State.IMSI_NEW, rspImsi, traceArfcn, tracePci, rsrp, System.currentTimeMillis(), cell_id));
                            }
                            String tracePlmn = rspImsi.substring(0, 5);

                            switch (tracePlmn) {
                                case "46011":
                                case "46003":
                                case "46005":
                                case "46012":
                                    telecom_count++;
                                    break;
                                case "46000":
                                case "46002":
                                case "46007":
                                case "46004":
                                case "46008":
                                case "46013":
                                    mobile_count++;
                                    break;
                                case "46001":
                                case "46009":
                                case "46006":
                                case "46010":
                                    unicom_count++;
                                    break;
                                case "46015":
                                    sva_count++;
                                    break;
                            }
                        }

                    }

                    if (!mHandler.hasMessages(9)) {
//                        mCatchChildFragment.resetShowData(false); // 刷新视图
                        mHandler.sendEmptyMessage(9);
                    }

                    if ( traceUtil.getWorkState(cell_id) == GnbBean.DW_State.TRACE && !isRsrpStart && rsp.getRsrp() > 5) {
                        setCountNum(logicIndex, 0);
                        isRsrpStart = true;
                        if (!mHandler.hasMessages(2))
                            mHandler.sendEmptyMessageDelayed(2, TraceBean.RSRP_TIME_INTERVAL);
                    }
                }
            }
        }
    }

    /**
     * doa_type:
     * 测向方向个数
     * 0：单兵
     * 2：正四个方向
     * 3：等边六个方向
     * <p>
     * host_type( board_type):
     * 板子类型计算rb_start 1:G73 不需要计的 2: 6758 不需要计算的 3: G73 需计的4: G758 需算的
     * <p>
     * bandwidth:
     */
    private void doDBWork(String id, String arfcn, String pci) {
        AppLog.I("doDBWork id = " + id + ", arfcn = " + arfcn + ", pci = " + pci);
        final int[] doa_type = {2};
        final int[] host_type = {4};
        final int[] bandwidth = {5};
        int iArfcn = Integer.parseInt(arfcn);
        int iPci = Integer.parseInt(pci);
        /*
            空口模式：-1
            4g:
            TDD：10000000-GPS时偏
            FDD：0
            5g:
            移动：GPS时偏
            联通电信：0
            得到多少就下发多少，不加减了
        */
        int time_offset = 0;
        int band;
        if (iArfcn > 100000) {
            band = NrBand.earfcn2band(iArfcn);
            if (band == 28 || band == 41 || band == 79)
                time_offset = GnbCity.build().getTimingOffset("5G");
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

        //空口模式时偏为-1
        //2024.4.10：测向默认走GPS，不需要走空口
        final String sync_mode = PrefUtil.build().getValue("sync_mode_cx", "GPS").toString();
        if (sync_mode.equals("Air")) {
            time_offset = -1;
        }

        int nrBand = NrBand.earfcn2band(iArfcn);
        int lteBand = LteBand.earfcn2band(iArfcn);
        if (nrBand > 0 || lteBand > 0) {
            bandwidth[0] = getBandWidthByArfcn(arfcn);
        }

        /**
         * 如果是准备则开启
         * 如果是开启中不做事
         * 如果是开启先判断定位状态结束了则一起结束，如果是工作状态判断频点和pci是否一致，不一致则结束，否则不做处理
         * 如果是结束中不做事
         */

        if (DBViewModel.getWorkState() == GnbBean.DB_State.READY) {
            AppLog.I("-----------------------侧向启动-------------------------------------");
            getmTraceChildFragment().setRadarViewSearching(true);
            DbGpioCtl.build().openGPIO(DBViewModel.getDeviceId(), iArfcn, doa_type[0]);
            MessageController.build().startDBPwrDetect(DBViewModel.getDeviceId(), iArfcn, iPci, time_offset, 16, doa_type[0], host_type[0], bandwidth[0]);
            AppLog.D("startDBPwrDetect deviceId = " + DBViewModel.getDeviceId() + ", iArfcn = " + iArfcn +
                    ", iPci = " + iPci + ", time_offset = " + time_offset + ", period = " + 16 +
                    ", doa_type = " + doa_type[0] + ", host_type = " + host_type[0] + ", bandwidth = " + bandwidth[0]);
            DBViewModel.setDbArfcn(iArfcn);
            DBViewModel.setDbPci(iPci);
            DBViewModel.setWorkState(GnbBean.DB_State.START);
        } else if (DBViewModel.getWorkState() == GnbBean.DB_State.START) {
            return;
        } else if (DBViewModel.getWorkState() == GnbBean.DB_State.PWR_DETECT) {
            int IdleCount = 0;
            for (DeviceInfoBean deviceInfoBean : MainActivity.getInstance().getDeviceList()) {
                if (deviceInfoBean.getWorkState() == GnbBean.DW_State.STOP ||
                        deviceInfoBean.getWorkState() == GnbBean.DW_State.IDLE) {
                    IdleCount++;
                }
                if (deviceInfoBean.getWorkState() == GnbBean.DW_State.TRACE) {
                    if (iArfcn != DBViewModel.getDbArfcn() || iPci != DBViewModel.getDbPci()) {
                        AppLog.I("-----------------------定位频点或pci不一致，侧向停止-------------------------------------");
                        getmTraceChildFragment().setRadarViewSearching(false);
                        MessageController.build().stopDBPwrDetect(DBViewModel.getDeviceId());
                        AppLog.D("stopDBPwrDetect deviceId = " + DBViewModel.getDeviceId());
                        DBViewModel.setWorkState(GnbBean.DB_State.STOP);
                        return;
                    }
                }

            }

            //所有设备未运行，停止单兵
            if (IdleCount == MainActivity.getInstance().getDeviceList().size()) {
                AppLog.I("-----------------------定位停止，侧向停止-------------------------------------");
                AppLog.I("IdleCount=" + IdleCount);
                getmTraceChildFragment().setRadarViewSearching(false);
                MessageController.build().stopDBPwrDetect(DBViewModel.getDeviceId());
                AppLog.D("stopDBPwrDetect deviceId = " + DBViewModel.getDeviceId());
                DBViewModel.setWorkState(GnbBean.DB_State.STOP);
                return;
            }
        } else if (DBViewModel.getWorkState() == GnbBean.DB_State.STOP) {
            return;
        }

    }

    private int getBandWidthByArfcn(String arfcn) {
        if (arfcn.length() > 5) {
            if (isTdd(true, NrBand.earfcn2band(Integer.parseInt(arfcn)))) {
                return 100;
            } else return 20;
        } else {
            return 5;
        }
    }

    public static boolean isTdd(boolean isNr, int band) {
        if (isNr) {
            switch (band) {
                case 41:
                case 78:
                case 79:
                    return true;
                case 1:
                case 3:
                case 28:
                    return false;
            }
        } else {
            switch (band) {
                case 1:
                case 3:
                case 5:
                case 8:
                    return false;
                case 34:
                case 39:
                case 40:
                case 41:
                    return true;
            }
        }
        return true;
    }

    private boolean isStartCatch() {
        boolean isStartCatch = false;
        for (int i = 0; i < DeviceUtil.MaxDeviceCount; i++) {
            for (int j = 0; j < DeviceUtil.MaxCellIdPerDevice; j++) {
                if (getIsStartCatchHandlerNum(i, j)) {
                    isStartCatch = true;
                    break;
                }
            }
        }
        return isStartCatch;

    }

    public boolean getAutoRun(int channelNum) {
        switch (channelNum) {
            case 1:
                return autoRun1;
            case 2:
                return autoRun2;
            case 3:
                return autoRun3;
            case 4:
                return autoRun4;
            case 5:
                return autoRun5;
            case 6:
                return autoRun6;
            case 7:
                return autoRun7;
            case 8:
                return autoRun8;
            case 9:
                return autoRun9;
            case 10:
                return autoRun10;
            case 11:
                return autoRun11;
            case 12:
                return autoRun12;
        }
        return autoRun1;
    }

    public void onStopTraceRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;
        int logicIndex = DeviceUtil.getLogicIndexById(id);
        int channelNum = DeviceUtil.getChannelNum(logicIndex, rsp.getCellId());
        int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains(devA) ? 0 : 1;
        if (rsp.getCmdType() == DWProtocol.UI_2_gNB_STOP_TRACE || rsp.getCmdType() == DWProtocol.UI_2_gNB_STOP_LTE_TRACE) {
            //切换频段接着运行
            String arfcn = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getArfcn(rsp.getCellId());
            String pci = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getPci(rsp.getCellId());

            boolean autoRun = getAutoRun(channelNum);
            if (autoRun && MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getWorkState(rsp.getCellId()) != GnbBean.DW_State.STOP) {
                updateParamAndStart(id, arfcn, pci, rsp.getCellId());
                return;
            }
            setAutoRun(channelNum, autoRun);

            String s = getCellStr(logicIndex, rsp.getCellId());
            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.IDLE);
            if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell) + s + getString(R.string.stop_work_success));
                updateLeftTvState(type, 0, logicIndex, rsp.getCellId(), getString(R.string.idle), false);
                mTraceChildFragment.resetRsrp(mCfgTraceChildFragment.isYiguangChecked() ? 0 : 1);
            } else {
                if (rsp.getRspValue() == 3) {
                    Util.showToast("通道" + s + "忙，请稍后再结束");
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.TRACE);
                    updateLeftTvState(type, 100, logicIndex, rsp.getCellId(), Util.getString(R.string.traceing), false);
                    refreshTraceBtn();
                    return;
                } else if (rsp.getRspValue() == 5) {
                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, "通道" + s + "结束工作成功");
                    updateLeftTvState(type, 0, logicIndex, rsp.getCellId(), "空闲", false);
                } else {
                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "通道" + s + "结束工作失败");
                    updateLeftTvState(type, 50, logicIndex, rsp.getCellId(), "结束失败", true);
                    Util.showToast("通道" + s + "结束工作失败, 请重试");
                }
            }
            MessageController.build().writeDWOpLog(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + rsp.getCellId() + "\t\t结束定位");
            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setTacChange(rsp.getCellId(), false);
            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setEnable(rsp.getCellId(), false);
            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setRsrp(rsp.getCellId(), 0);
            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setLastRsrp(rsp.getCellId(), -1);
            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setImsi(rsp.getCellId(), "");
            mHandler.removeMessages(channelNum + 10);
            //停止数传,arfcn设为0
            //先判断下单兵是否开启，开启了才发，否则清除消息1
            if (mTraceChildFragment.getSwitch_pef_pwr() != null && mTraceChildFragment.getSwitch_pef_pwr().isChecked()) {
                dataFwdBean.setArfcn("0");
                dataFwdBean.setPci(1);
                Gson gson = new Gson();
                String json = gson.toJson(dataFwdBean);
                for (DeviceInfoBean deviceInfoBean : MainActivity.getInstance().getDeviceList()) {
                    AppLog.D("setDWDataFwd deviceInfoBean.getRsp().getDeviceId()" + deviceInfoBean.getRsp().getDeviceId() + "json = " + json);
                    MessageController.build().setDWDataFwd(deviceInfoBean.getRsp().getDeviceId(), json);
                }
            }
            mTraceChildFragment.setSwitch_pef_pwr(false);
            //结束业务的时候恢复
            MessageController.build().setDWPefPwr(id, rsp.getCellId(), -84);
            mHandler.removeMessages(1);
            dataFwdBean.setArfcn("");
            isRsrpStart = false;
//            setIsStartCatchHandler(logicIndex, false);
            setIsStartCatchHandler(0, false);
            setIsStartCatchHandler(1, false);
            setIsStartCatchHandler(2, false);

            refreshTraceBtn();
            MainActivity.getInstance().resetLeftArfcn(-1);
            mCfgTraceChildFragment.setcfgEnable(-1, true);
        }
    }

    private void setIsStartCatchHandler(int logicIndex, boolean isStart) {
        switch (logicIndex) {
            case 0:
                isStartCatchHandler1 = isStart;
                isStartCatchHandler2 = isStart;
                isStartCatchHandler3 = isStart;
                isStartCatchHandler4 = isStart;
                break;
            case 1:
                isStartCatchHandler5 = isStart;
                isStartCatchHandler6 = isStart;
                isStartCatchHandler7 = isStart;
                isStartCatchHandler8 = isStart;
                break;
            case 2:
                isStartCatchHandler9 = isStart;
                isStartCatchHandler10 = isStart;
                isStartCatchHandler11 = isStart;
                isStartCatchHandler12 = isStart;
                break;
        }
    }

    private void setAnimWork(ImageView iv, int resId, boolean start) {
        if (iv == null) return;
        if (start) {
            if (iv.getVisibility() == View.INVISIBLE) {
                iv.setImageResource(resId);
                iv.setVisibility(View.VISIBLE);
                AnimationDrawable drawable = (AnimationDrawable) iv.getDrawable();
                drawable.start();
            }
        } else {
            if (iv.getVisibility() == View.VISIBLE) {
                AnimationDrawable drawable = (AnimationDrawable) iv.getDrawable();
                drawable.stop();
                iv.setVisibility(View.INVISIBLE);
            }
        }
    }

    //通过4个通道状态设置按钮状态
    public void refreshTraceBtn() {
        AppLog.D("refreshTraceBtn");
        TextView tv_do_btn = mCfgTraceChildFragment.getTv_do_btn();
        List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();

        if (deviceList.size() <= 0) { // 无设备
            AppLog.I("refreshTraceBtn deviceList size = 0");
            tv_do_btn.setText(getString(R.string.open));
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
            return;
        }

//        // 两个设备
//        TraceUtil traceUtil0 = deviceList.get(0).getTraceUtil(); // 设备1
//        int workStateFirst0 = traceUtil0.getWorkState(DWProtocol.CellId.FIRST);  // 设备1  通道一
//        int workStateSecond0 = traceUtil0.getWorkState(DWProtocol.CellId.SECOND); // 设备1 通道二
//
//        TraceUtil traceUtil1 = deviceList.get(1).getTraceUtil(); // 设备2
//        int workStateFirst1 = traceUtil1.getWorkState(DWProtocol.CellId.FIRST);  // 设备2  通道一
//        int workStateSecond1 = traceUtil1.getWorkState(DWProtocol.CellId.SECOND); // 设备2 通道二
//
//        AppLog.I("refreshTraceBtn deviceList size = 2, workStateFirst0 = " + workStateFirst0 + ", workStateSecond0 = " + workStateSecond0
//                + ", workStateFirst1 = " + workStateFirst1 + ", workStateSecond1 = " + workStateSecond1);
//
//        if (workStateFirst0 == GnbBean.DW_State.IDLE
//                && (deviceList.get(0).getRsp().getDualCell() == 1 || workStateSecond0 == GnbBean.DW_State.IDLE || workStateSecond0 == GnbBean.DW_State.NONE)
//                && workStateFirst1 == GnbBean.DW_State.IDLE
//                && (deviceList.get(1).getRsp().getDualCell() == 1 || workStateSecond1 == GnbBean.DW_State.IDLE || workStateSecond1 == GnbBean.DW_State.NONE)) {
//            MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.DW_State.IDLE);
//            MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.DW_State.IDLE);
//            tv_do_btn.setText(getString(R.string.open));
//        } else if (workStateFirst0 == GnbBean.DW_State.STOP || workStateSecond0 == GnbBean.DW_State.STOP || workStateFirst1 == GnbBean.DW_State.STOP || workStateSecond1 == GnbBean.DW_State.STOP) {
//            if (workStateFirst1 == GnbBean.DW_State.STOP || workStateSecond1 == GnbBean.DW_State.STOP) {
//                MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.DW_State.STOP);
//            } else
//                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.DW_State.STOP);
//            tv_do_btn.setText(getString(R.string.stoping));
//        } else if (workStateFirst0 == GnbBean.DW_State.TRACE || workStateSecond0 == GnbBean.DW_State.TRACE || workStateFirst1 == GnbBean.DW_State.TRACE || workStateSecond1 == GnbBean.DW_State.TRACE) {
//            if (workStateFirst1 == GnbBean.DW_State.TRACE || workStateSecond1 == GnbBean.DW_State.TRACE) {
//                MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.DW_State.TRACE);
//            } else
//                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.DW_State.TRACE);
//            tv_do_btn.setText(getString(R.string.stop));
//        } else if (workStateFirst0 == GnbBean.DW_State.CATCH || workStateSecond0 == GnbBean.DW_State.CATCH || workStateFirst1 == GnbBean.DW_State.CATCH || workStateSecond1 == GnbBean.DW_State.CATCH) {
//            if (workStateFirst1 == GnbBean.DW_State.CATCH || workStateSecond1 == GnbBean.DW_State.CATCH) {
//                MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.DW_State.CATCH);
//            } else
//                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.DW_State.CATCH);
//            tv_do_btn.setText(getString(R.string.stop));
//        } else {
//            tv_do_btn.setText(getString(R.string.starting));
//        }
//        if (getBtnStr().contains(getString(R.string.stop)))
//            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_delete);
//        else {
//            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
//            mCfgTraceChildFragment.setIsCloaseDistance(false);
//        }

        //多设备
        //只要有一个设备开启（Trace、Catch）就是开启状态，没有开启有停止就是停止状态，都是空闲或未连接就是空闲状态，其他就是启动中
        if (deviceList.size() >= 1) {
            int startCount = 0; //通道开启
            int stopCount = 0;  //通道结束中
            int IDLEOrNoneCount = 0;    //通道空闲或未连接

            for (DeviceInfoBean deviceInfoBean : deviceList) {
                int workStateFirst = deviceInfoBean.getTraceUtil().getWorkState(DWProtocol.CellId.FIRST);
                int workStateSecond = deviceInfoBean.getTraceUtil().getWorkState(DWProtocol.CellId.SECOND);
                int workStateThird = deviceInfoBean.getTraceUtil().getWorkState(DWProtocol.CellId.THIRD);
                int workStateFourth = deviceInfoBean.getTraceUtil().getWorkState(DWProtocol.CellId.FOURTH);

                AppLog.I("refreshTraceBtn deviceInfoBean" + deviceInfoBean.getRsp().getDevName() +
                        ", workStateFirst = " + workStateFirst + ", workStateSecond = " + workStateSecond
                        + ", workStateThird = " + workStateThird + ", workStateFourth = " + workStateFourth);

                if (workStateFirst == GnbBean.DW_State.TRACE || workStateFirst == GnbBean.DW_State.CATCH) {
                    startCount++;
                }
                if (workStateSecond == GnbBean.DW_State.TRACE || workStateSecond == GnbBean.DW_State.CATCH) {
                    startCount++;
                }
                if (workStateThird == GnbBean.DW_State.TRACE || workStateThird == GnbBean.DW_State.CATCH) {
                    startCount++;
                }
                if (workStateFourth == GnbBean.DW_State.TRACE || workStateFourth == GnbBean.DW_State.CATCH) {
                    startCount++;
                }

                if (workStateFirst == GnbBean.DW_State.TRACE || workStateSecond == GnbBean.DW_State.TRACE
                        || workStateThird == GnbBean.DW_State.TRACE || workStateFourth == GnbBean.DW_State.TRACE)
                    deviceInfoBean.setWorkState(GnbBean.DW_State.TRACE);
                if (workStateFirst == GnbBean.DW_State.CATCH || workStateSecond == GnbBean.DW_State.CATCH
                        || workStateThird == GnbBean.DW_State.CATCH || workStateFourth == GnbBean.DW_State.CATCH)
                    deviceInfoBean.setWorkState(GnbBean.DW_State.CATCH);

                if (workStateFirst == GnbBean.DW_State.STOP) {
                    stopCount++;
                }
                if (workStateSecond == GnbBean.DW_State.STOP) {
                    stopCount++;
                }
                if (workStateThird == GnbBean.DW_State.STOP) {
                    stopCount++;
                }
                if (workStateFourth == GnbBean.DW_State.STOP) {
                    stopCount++;
                }

                if (workStateFirst == GnbBean.DW_State.STOP || workStateSecond == GnbBean.DW_State.STOP
                        || workStateThird == GnbBean.DW_State.STOP || workStateFourth == GnbBean.DW_State.STOP) {
                    deviceInfoBean.setWorkState(GnbBean.DW_State.STOP);
                }

                if (workStateFirst == GnbBean.DW_State.IDLE || workStateFirst == GnbBean.DW_State.NONE) {
                    IDLEOrNoneCount++;
                }
                if (workStateSecond == GnbBean.DW_State.IDLE || workStateSecond == GnbBean.DW_State.NONE) {
                    IDLEOrNoneCount++;
                }
                if (workStateThird == GnbBean.DW_State.IDLE || workStateThird == GnbBean.DW_State.NONE) {
                    IDLEOrNoneCount++;
                }
                if (workStateFourth == GnbBean.DW_State.IDLE || workStateFourth == GnbBean.DW_State.NONE) {
                    IDLEOrNoneCount++;
                }

                if (workStateFirst == GnbBean.DW_State.IDLE && workStateSecond == GnbBean.DW_State.IDLE
                        && workStateThird == GnbBean.DW_State.IDLE && workStateFourth == GnbBean.DW_State.IDLE) {
                    deviceInfoBean.setWorkState(GnbBean.DW_State.IDLE);
                }
                if (workStateFirst == GnbBean.DW_State.NONE || workStateSecond == GnbBean.DW_State.NONE
                        || workStateThird == GnbBean.DW_State.NONE || workStateFourth == GnbBean.DW_State.NONE) {
                    deviceInfoBean.setWorkState(GnbBean.DW_State.NONE);
                }
            }

            AppLog.D("TraceCatchFragment refreshTraceBtn startCount = " + startCount + ", stopCount = " + stopCount + ", IDLEOrNoneCount = " + IDLEOrNoneCount);
            if (startCount > 0) {
                tv_do_btn.setText(getString(R.string.stop));
            } else if (stopCount > 0) {
                tv_do_btn.setText(getString(R.string.stoping));
            } else if (IDLEOrNoneCount == MainActivity.getInstance().getDeviceList().size() * DeviceUtil.MaxCellIdPerDevice) {
                tv_do_btn.setText(getString(R.string.open));
                mCfgTraceChildFragment.setcfgEnable(-1, true);
            } else {
                tv_do_btn.setText(getString(R.string.starting));
            }

            if (getBtnStr().contains(getString(R.string.stop)))
                tv_do_btn.setBackgroundResource(R.drawable.btn_bg_delete);
            else {
                MainActivity.getInstance().enableSpecial = false;   // 停止业务恢复关闭状态
                if (mSettingFragment != null && mSettingFragment.getDwSettingFragment() != null)
                    mSettingFragment.getDwSettingFragment().setEnableSpecialSw();
                tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
                mCfgTraceChildFragment.setIsCloaseDistance(false);
            }
        }
    }


    private void freshDoWorkState(int type, int logicIndex, int cell_id, String imsi, String
            arfcn, String pci) {
        AppLog.I("freshDoWorkState type = " + type + ", cell_id = " + cell_id + ", imsi = " + imsi + ", arfcn = " + arfcn + ", pci = " + pci);
        String s = getString(R.string.cell) + getCellStr(logicIndex, cell_id) + getString(R.string.traceing);

        MainActivity.getInstance().updateSteps(type, StepBean.State.success, s);
        updateLeftTvState(type, 100, logicIndex, cell_id, getString(R.string.traceing), false);
        if (!imsi.isEmpty()) {
            AppLog.D("freshDoWorkState isStartCatch" + isStartCatch());
            //上号刷新报值页
//            if (!isStartCatch())
            mTraceChildFragment.setConfigInfo(mCfgTraceChildFragment.isYiguangChecked(), type, arfcn, pci, imsi);
            int channelNum = DeviceUtil.getChannelNum(logicIndex, cell_id);
            MainActivity.getInstance().setLeftConfigInfo(channelNum, type, arfcn, pci, imsi);
        }
    }

    public List<ImsiBean> getDataList() {
        return mImsiList;
    }

    public void onFreqScanRsp(String id, GnbFreqScanRsp rsp) {
        if (mFreqDialog != null) mFreqDialog.onFreqScanRsp(id, rsp);
    }

//待修改，似乎没用这个方法
//自动定位
//扫频走完开始定位，使用扫出来的pci算法
//    @Override
//    public void onTraceConfig() {
//        AppLog.D("onTraceConfig");
//        autoArfcnIndex0 = 0;
//        autoArfcnIndex1 = 0;
//        autoArfcnIndex2 = 0;
//        autoArfcnIndex3 = 0;
//
//        autoRun1 = false;
//        autoRun2 = false;
//        autoRun3 = false;
//        autoRun4 = false;
//
//        mHandler.removeCallbacksAndMessages(null);
//
//        txValue = 0;
//        isClickStop = false;
//        mImsiList.clear();
//        mCatchChildFragment.resetShowData(true);
//        all_count = 0;
//        unicom_count = 0;
//        mobile_count = 0;
//        telecom_count = 0;
//        sva_count = 0;
//        mTraceChildFragment.resetRsrp();
//
//        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
//            PaCtl.build().closePA(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId());
//            boolean isAirSync = PrefUtil.build().getValue("sync_mode_dw", "Air").toString().equals("Air");
//            int enableAir = isAirSync ? 1 : 0;
//            long timeMillis = System.currentTimeMillis();
//            TraceUtil traceUtil = DeviceUtil.getTraceUtilByDeviceName()
//            mLogicTraceUtilA.setAtCmdTimeOut(DWProtocol.CellId.FIRST, timeMillis);
//            mLogicTraceUtilA.setAtCmdTimeOut(DWProtocol.CellId.SECOND, timeMillis);
//            mLogicTraceUtilA.setAtCmdTimeOut(DWProtocol.CellId.THIRD, timeMillis);
//            mLogicTraceUtilA.setAtCmdTimeOut(DWProtocol.CellId.FOURTH, timeMillis);
//
//            //设置配置信息
//            // 通道一
//            boolean runCell1 = false;
//            boolean runCell2 = false;
//            boolean runCell3 = false;
//            boolean runCell4 = false;
//            String arfcn_cfg = "";
//            String pci_cfg = "";
//            LinkedList<ArfcnPciBean> td1 = arfcnBeanHashMap.get("TD1");
//            if (td1 != null) {
//                if (td1.size() > 0) arfcn_cfg = String.valueOf(td1.get(0));
//                //拿到pci计算
//                mLogicTraceUtilA.setAirSync(DWProtocol.CellId.FIRST, enableAir);
//                if (!arfcn_cfg.isEmpty()) {
//                    int band = NrBand.earfcn2band(Integer.parseInt(arfcn_cfg));
//                    mLogicTraceUtilA.setBandWidth(DWProtocol.CellId.FIRST, 20);
//                    mLogicTraceUtilA.setSsbBitmap(DWProtocol.CellId.FIRST, 255);
//                    mLogicTraceUtilA.setPlmn(DWProtocol.CellId.FIRST, band == 78 ? "46011" : "46015");
//                    runCell1 = true;
//                }
//                mLogicTraceUtilA.setArfcn(DWProtocol.CellId.FIRST, arfcn_cfg);
//                mLogicTraceUtilA.setPci(DWProtocol.CellId.FIRST, pci_cfg);
//                if (!auto_trace_imsi.isEmpty())
//                    mLogicTraceUtilA.setImsi(DWProtocol.CellId.FIRST, auto_trace_imsi);
//            }
//
//
//            // 通道二
//            arfcn_cfg = "";
//            LinkedList<ArfcnPciBean> td2 = arfcnBeanHashMap.get("TD2");
//            if (td2 != null) {
//                if (td2.size() > 0) arfcn_cfg = String.valueOf(td2.get(0));
//
//                if (!arfcn_cfg.isEmpty()) runCell2 = true;
//                pci_cfg = PrefUtil.build().getValue("PCI_TD2", "501").toString();
//                mLogicTraceUtilA.setAirSync(DWProtocol.CellId.SECOND, enableAir);
//                int band1 = NrBand.earfcn2band(Integer.parseInt(arfcn_cfg));
//                mLogicTraceUtilA.setBandWidth(DWProtocol.CellId.SECOND, 5);
//                mLogicTraceUtilA.setSsbBitmap(DWProtocol.CellId.SECOND, 128);
//                String plmn;
//                if (band1 == 3) {
//                    int freq = LteBand2.earfcn2freq(Integer.parseInt(arfcn_cfg));
//                    if (((freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949)))
//                        plmn = "46015";
//                    else plmn = "46011";
//                } else plmn = band1 == 5 ? "46011" : "46015";
//                mLogicTraceUtilA.setPlmn(DWProtocol.CellId.SECOND, plmn);
//                mLogicTraceUtilA.setArfcn(DWProtocol.CellId.SECOND, arfcn_cfg);
//                mLogicTraceUtilA.setPci(DWProtocol.CellId.SECOND, pci_cfg);
//                if (!auto_trace_imsi.isEmpty())
//                    mLogicTraceUtilA.setImsi(DWProtocol.CellId.SECOND, auto_trace_imsi);
//            }
//
//            // 通道三
//            arfcn_cfg = "";
//            LinkedList<ArfcnPciBean> td3 = arfcnBeanHashMap.get("TD3");
//            if (td3 != null) {
//                if (td3.size() > 0) arfcn_cfg = String.valueOf(td3.get(0));
//
//                pci_cfg = PrefUtil.build().getValue("PCI_TD3", "1002").toString();
//                mLogicTraceUtilA.setAirSync(DWProtocol.CellId.THIRD, enableAir);
//                if (!arfcn_cfg.isEmpty()) {
//                    int band2 = Integer.parseInt(arfcn_cfg) > 100000 ? NrBand.earfcn2band(Integer.parseInt(arfcn_cfg)) : LteBand.earfcn2band(Integer.parseInt(arfcn_cfg));
//                    mLogicTraceUtilA.setBandWidth(DWProtocol.CellId.THIRD, Integer.parseInt(arfcn_cfg) > 100000 ? 20 : 5);
//                    mLogicTraceUtilA.setSsbBitmap(DWProtocol.CellId.THIRD, Integer.parseInt(arfcn_cfg) > 100000 ? 240 : 128);
//                    mLogicTraceUtilA.setPlmn(DWProtocol.CellId.THIRD, band2 == 28 ? "46000" : "46001");
//                    runCell3 = true;
//                }
//                mLogicTraceUtilA.setArfcn(DWProtocol.CellId.THIRD, arfcn_cfg);
//                mLogicTraceUtilA.setPci(DWProtocol.CellId.THIRD, pci_cfg);
//                if (!auto_trace_imsi.isEmpty())
//                    mLogicTraceUtilA.setImsi(DWProtocol.CellId.THIRD, auto_trace_imsi);
//            }
//
//            // 通道四
//            arfcn_cfg = "";
//            LinkedList<ArfcnPciBean> td4 = arfcnBeanHashMap.get("TD4");
//            if (td4 != null) {
//                if (td4.size() > 0) arfcn_cfg = String.valueOf(td4.get(0));
//
//                if (!arfcn_cfg.isEmpty()) {
//                    int band3 = NrBand.earfcn2band(Integer.parseInt(arfcn_cfg));
//                    int freq = LteBand2.earfcn2freq(Integer.parseInt(arfcn_cfg));
//                    //B34\39\40\41\次B3
//                    if ((freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949)
//                            || band3 == 34 || band3 == 39 || band3 == 40 || band3 == 41)
//                        mLogicTraceUtilA.setPlmn(DWProtocol.CellId.FOURTH, "46000");
//                    else mLogicTraceUtilA.setPlmn(DWProtocol.CellId.FOURTH, "46001");
//                    runCell4 = true;
//                }
//                pci_cfg = PrefUtil.build().getValue("PCI_TD4", "502").toString();
//                mLogicTraceUtilA.setAirSync(DWProtocol.CellId.FOURTH, enableAir);
//                mLogicTraceUtilA.setBandWidth(DWProtocol.CellId.FOURTH, 5);
//                mLogicTraceUtilA.setSsbBitmap(DWProtocol.CellId.FOURTH, 128);
//
//                mLogicTraceUtilA.setArfcn(DWProtocol.CellId.FOURTH, arfcn_cfg);
//                mLogicTraceUtilA.setPci(DWProtocol.CellId.FOURTH, pci_cfg);
//                if (!auto_trace_imsi.isEmpty())
//                    mLogicTraceUtilA.setImsi(DWProtocol.CellId.FOURTH, auto_trace_imsi);
//            }
//
//            MainActivity.getInstance().getDeviceList().get(i).setTraceUtil(mLogicTraceUtilA);
//
//            final List<UeidBean> blackList = new ArrayList<>();
//            for (MyUeidBean bean : MainActivity.getInstance().getBlackList())
//                blackList.add(bean.getUeidBean());
//
//            if (MainActivity.getInstance().getDeviceList().size() <= 0) {
//                Util.showToast(getString(R.string.dev_not_online));
//                return;
//            } else if (!(runCell1 || runCell2 || runCell3 || runCell4)) {
//                Util.showToast(getString(R.string.no_cfg));
//                return;
//            }
//
//            //存入历史记录
//            storeAutoInfoIntoHistory(td1, td2, td3, td4);
//
//            AppLog.I("onTraceConfig() 定位启动 = " + i);
//            // 第一步，配置黑名单
//            if (runCell1) {
//                MessageController.build().setDWNrBlackList(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId(), DWProtocol.CellId.FIRST, blackList.size(), blackList);
//                MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(DWProtocol.CellId.FIRST, GnbBean.DW_State.BLACKLIST);
//            }
//
//            int finalI = i;
//            if (runCell2) {
//                new Handler().postDelayed(() -> {
//                    MessageController.build().setDWLteBlackList(MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDeviceId(), DWProtocol.CellId.SECOND, blackList.size(), blackList);
//                    MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(DWProtocol.CellId.SECOND, GnbBean.DW_State.BLACKLIST);
//                }, 500);
//            }
//            if (runCell3) {
//                new Handler().postDelayed(() -> {
//                    MessageController.build().setDWNrBlackList(MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDeviceId(), DWProtocol.CellId.THIRD, blackList.size(), blackList);
//                    MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(DWProtocol.CellId.THIRD, GnbBean.DW_State.BLACKLIST);
//                }, 1000);
//            }
//
//            if (runCell4) {
//                new Handler().postDelayed(() -> {
//                    MessageController.build().setDWLteBlackList(MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDeviceId(), DWProtocol.CellId.FOURTH, blackList.size(), blackList);
//                    MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(DWProtocol.CellId.FOURTH, GnbBean.DW_State.BLACKLIST);
//                }, 1500);
//            }
//        }
//
//        mCfgTraceChildFragment.setcfgEnable(-1, false);
//        //延迟200ms防止切换失败
////        new Handler().postDelayed(() -> {
//        view_pager.setCurrentItem(1);
////        }, 200);
//        refreshTraceBtn();
//        Util.showToast(getString(R.string.cfging_trace));
//    }

    //将当前的数据存入历史记录
    private void storeIntoHistory() {
        try {
            JSONArray jsonArray = new JSONArray();

            HistoryBean_3g758cx historyBean = mCfgTraceChildFragment.getHistoryBean();
            //通道一
            JSONArray arfcnPciJsonArray = new JSONArray();
            JSONObject jsonObject1 = new JSONObject();
            for (int i = 0; i < historyBean.getYgList().size(); i++)
                arfcnPciJsonArray.put(historyBean.getYgList().get(i));
            jsonObject1.put("arfcnPciJsonArray", arfcnPciJsonArray);
            jsonObject1.put("imsi", mCfgTraceChildFragment.actv_imsi_yiguang.getText().toString());
            jsonArray.put(jsonObject1);

            //通道二
            arfcnPciJsonArray = new JSONArray();
            JSONObject jsonObject2 = new JSONObject();
            for (int i = 0; i < historyBean.getLdList().size(); i++)
                arfcnPciJsonArray.put(historyBean.getLdList().get(i));
            jsonObject2.put("arfcnPciJsonArray", arfcnPciJsonArray);
            jsonObject2.put("imsi", mCfgTraceChildFragment.actv_imsi_liandian.getText().toString());
            jsonArray.put(jsonObject2);

            //时间
            jsonArray.put(DateUtil.getCurrentTime());

            long result = DataBaseUtil_3g758cx.insertTraceCfgToDB(jsonArray);
            if (result != -1) {
                AppLog.D("storeIntoHistory 存入历史记录成功 ID: " + result);
            } else {
                AppLog.D("storeIntoHistory 存入历史记录失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //待修改，底层逻辑不变，取出来之后处理然后在页面上显示
//将当前的数据存入历史记录
    private void storeAutoInfoIntoHistory
    (LinkedList<ArfcnPciBean> td1, LinkedList<ArfcnPciBean> td2, LinkedList<ArfcnPciBean> td3, LinkedList<ArfcnPciBean> td4) {
        if (td1 != null || td2 != null || td3 != null || td4 != null) {
            try {
                TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(0).getTraceUtil();
                JSONArray jsonArray = new JSONArray();

                //通道一
                JSONArray arfcnJsonArray = new JSONArray();
                JSONObject jsonObject1 = new JSONObject();
                if (td1 != null) {
                    for (int i = 0; i < td1.size(); i++)
                        arfcnJsonArray.put(td1.get(i));
                    jsonObject1.put("arfcnPciJsonArray", arfcnJsonArray);
                } else jsonObject1.put("arfcnPciJsonArray", "[]");
                jsonObject1.put("pci", traceUtil.getPci(0));
                jsonObject1.put("imsi", traceUtil.getImsi(0));
                jsonArray.put(jsonObject1);

                //通道二
                arfcnJsonArray = new JSONArray();
                JSONObject jsonObject2 = new JSONObject();
                if (td2 != null) {
                    for (int i = 0; i < td2.size(); i++)
                        arfcnJsonArray.put(td2.get(i));
                    jsonObject2.put("arfcnPciJsonArray", arfcnJsonArray);
                } else jsonObject2.put("arfcnPciJsonArray", "[]");
                jsonObject2.put("pci", traceUtil.getPci(1));
                jsonObject2.put("imsi", traceUtil.getImsi(1));
                jsonArray.put(jsonObject2);

                //通道三
                arfcnJsonArray = new JSONArray();
                JSONObject jsonObject3 = new JSONObject();
                if (td3 != null) {
                    for (int i = 0; i < td3.size(); i++)
                        arfcnJsonArray.put(td3.get(i));
                    jsonObject3.put("arfcnPciJsonArray", arfcnJsonArray);
                } else jsonObject3.put("arfcnPciJsonArray", "[]");
                jsonObject3.put("pci", traceUtil.getPci(2));
                jsonObject3.put("imsi", traceUtil.getImsi(2));
                jsonArray.put(jsonObject3);

                //通道四
                arfcnJsonArray = new JSONArray();
                JSONObject jsonObject4 = new JSONObject();
                if (td4 != null) {
                    for (int i = 0; i < td4.size(); i++)
                        arfcnJsonArray.put(td4.get(i));
                    jsonObject4.put("arfcnPciJsonArray", arfcnJsonArray);
                } else jsonObject4.put("arfcnPciJsonArray", "[]");
                jsonObject4.put("pci", traceUtil.getPci(3));
                jsonObject4.put("imsi", traceUtil.getImsi(3));
                jsonArray.put(jsonObject4);

                //时间
                jsonArray.put(DateUtil.getCurrentTime());

                long result = DataBaseUtil_3g758cx.insertTraceCfgToDB(jsonArray);
                if (result != -1) {
                    AppLog.D("storeAutoInfoIntoHistory 存入历史记录成功 ID: " + result);
                } else {
                    AppLog.D("storeAutoInfoIntoHistory 存入历史记录失败");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void importArfcn(ScanArfcnBean bean, int pci) {
        mCfgTraceChildFragment.importArfcn(bean, -1, pci);
    }


    private void startRunWork() {
        AppLog.D("startRunWork");
        boolean isAirSync = !PrefUtil.build().getValue("sync_mode_dw", "Air").toString().equals("GPS");
        setAutoArfcnIndex(-1, 0);
        setAutoRun(-1, false);

        mHandler.removeCallbacksAndMessages(null);

        txValue = 0;
        isClickStop = false;
        mImsiList.clear();
        mCatchChildFragment.cleanMImsiAdapter();
        mCatchChildFragment.resetShowData(true);
        all_count = 0;
        unicom_count = 0;
        mobile_count = 0;
        telecom_count = 0;
        sva_count = 0;
        mTraceChildFragment.resetRsrp();
        delay.set(2500);    //延迟启动避免弹窗重叠
        ParseDataUtil.build().initData();

        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
            String name = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName();
            String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
            int index = DeviceUtil.getIndexById(id);
            if (index == -1) return;
            TraceUtil mTraceUtil = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
            int logicIndex = DeviceUtil.getLogicIndexById(id);
            long timeMillis = System.currentTimeMillis();
            //boolean isGpsSync = MainActivity.getInstance().getDeviceList().get(i).getRsp().getGpsSyncState() == GnbStateRsp.Gps.SUCC;

            mTraceUtil.setAirSync(DWProtocol.CellId.FIRST, isAirSync ? 1 : 0);
            mTraceUtil.setAirSync(DWProtocol.CellId.SECOND, isAirSync ? 1 : 0);
            mTraceUtil.setAirSync(DWProtocol.CellId.THIRD, isAirSync ? 1 : 0);
            mTraceUtil.setAirSync(DWProtocol.CellId.FOURTH, isAirSync ? 1 : 0);

            mTraceUtil.setAtCmdTimeOut(DWProtocol.CellId.FIRST, timeMillis);
            mTraceUtil.setAtCmdTimeOut(DWProtocol.CellId.SECOND, timeMillis);
            mTraceUtil.setAtCmdTimeOut(DWProtocol.CellId.THIRD, timeMillis);
            mTraceUtil.setAtCmdTimeOut(DWProtocol.CellId.FOURTH, timeMillis);

            MainActivity.getInstance().getDeviceList().get(i).setTraceUtil(mTraceUtil);

            //根据通道运行
            PaCtl.build().closePAById(id);

            //根据通道运行
            for (int cell_id = 0; cell_id < DeviceUtil.MaxCellIdPerDevice; cell_id++) {
//                AppLog.D("arfcnBeanHashMap.get(\"TD\" + DeviceUtil.getChannelNum(logicIndex, cell_id))" + "TD" + DeviceUtil.getChannelNum(logicIndex, cell_id));
                if (!mTraceUtil.getImsi(cell_id).isEmpty() &&
                        arfcnBeanHashMap.get("TD" + DeviceUtil.getChannelNum(logicIndex, cell_id)) != null &&
                        arfcnBeanHashMap.get("TD" + DeviceUtil.getChannelNum(logicIndex, cell_id)).size() > 0) {
                    runNum(DeviceUtil.getChannelNum(logicIndex, cell_id), id);
                }
            }
        }
        if (delay.get() == 2500) {
            Util.showToast("未检测到当前设备可开启频点，请检查配置");
            mCfgTraceChildFragment.setcfgEnable(-1, true);
            return;
        }
    }

    private void runNum(int channelNum, String id) {
        AppLog.D("runNum" + channelNum);
        switch (channelNum) {
            case 1:
                run1(id, channelNum);
                break;
            case 2:
                run2(id, channelNum);
                break;
            case 3:
                run3(id, channelNum);
                break;
            case 4:
                run4(id, channelNum);
                break;
            case 5:
                run5(id, channelNum);
                break;
            case 6:
                run5(id, channelNum);
                break;
            case 7:
                run5(id, channelNum);
                break;
            case 8:
                run5(id, channelNum);
                break;
            case 9:
                run5(id, channelNum);
                break;
            case 10:
                run5(id, channelNum);
                break;
            case 11:
                run5(id, channelNum);
                break;
            case 12:
                run5(id, channelNum);
                break;
        }
    }

    private void run5(String id, int channelNum) {
        runCell4(id, delay.get(), channelNum);
        delay.addAndGet(500);
    }


    private void run4(String id, int channelNum) {
        int isSecondB3 = 0;
        if (PaCtl.build().is3G758) {
            ArfcnPciBean bean = arfcnBeanHashMap.get("TD" + channelNum).get(0);
            if (bean.getArfcn().isEmpty()) isSecondB3 = -1;
            else if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 3)
                isSecondB3 = 1;
        }
        if (isSecondB3 != 1) {      //直接执行
            runCell3(id, delay.get(), channelNum);
            delay.addAndGet(500);
        }
        //无需等待
        if (isSecondB3 == 1) {    //次B40等待信号
            // 在子线程中执行等待信号的逻辑
//            new Thread(() -> {
//                synchronized (lockB3) { // 使用共享的锁对象
//                    try {
//                        AppLog.D("SecondB3 is waiting");
//                        lockB3.wait(); // 子线程等待信号
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                // 收到信号后，在主线程执行任务
//                mainHandler.post(() -> {
            runCell3(id, delay.get(), channelNum);
            delay.addAndGet(500);
//                });
//            }).start();
        }
    }

    private void run3(String id, int channelNum) {
        runCell2(id, delay.get(), channelNum);
        delay.addAndGet(500);
    }

    private void run2(String id, int channelNum) {
        int isSecondB40 = 0;
        if (PaCtl.build().is3G758) {
            ArfcnPciBean bean = arfcnBeanHashMap.get("TD" + channelNum).get(0);
            if (bean.getArfcn().isEmpty()) isSecondB40 = -1;
            else if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 40)
                isSecondB40 = 1;
        }
        if (isSecondB40 != 1) {      //直接执行
            runCell1(id, delay.get(), channelNum);
            delay.addAndGet(500);
        }
        //无需等待
        if (isSecondB40 == 1) {    //次B40等待信号
            // 在子线程中执行等待信号的逻辑
//            new Thread(() -> {
//                synchronized (lockB40) { // 使用共享的锁对象
//                    try {
//                        AppLog.D("SecondB40 is waiting");
//                        lockB40.wait(); // 子线程等待信号
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                // 收到信号后，在主线程执行任务
//                mainHandler.post(() -> {
            runCell1(id, delay.get(), channelNum);  //确保通道四完成之后再执行
            delay.addAndGet(500);
//                });
//            }).start();
        }
    }

    private void run1(String id, int channelNum) {
        runCell0(id, channelNum);
        delay.set(500);
    }

    private void setAutoRun(int channelNum, boolean b) {
        if (channelNum == -1) {
            autoRun1 = b;
            autoRun2 = b;
            autoRun3 = b;
            autoRun4 = b;
            autoRun5 = b;
            autoRun6 = b;
            autoRun7 = b;
            autoRun8 = b;
            autoRun9 = b;
            autoRun10 = b;
            autoRun11 = b;
            autoRun12 = b;
        } else {
            switch (channelNum) {
                case 1:
                    autoRun1 = b;
                    break;
                case 2:
                    autoRun2 = b;
                    break;
                case 3:
                    autoRun3 = b;
                    break;
                case 4:
                    autoRun4 = b;
                    break;
                case 5:
                    autoRun5 = b;
                    break;
                case 6:
                    autoRun6 = b;
                    break;
                case 7:
                    autoRun7 = b;
                    break;
                case 8:
                    autoRun8 = b;
                    break;
                case 9:
                    autoRun9 = b;
                    break;
                case 10:
                    autoRun10 = b;
                    break;
                case 11:
                    autoRun11 = b;
                    break;
                case 12:
                    autoRun12 = b;
                    break;
            }
        }

    }

    private void setAutoArfcnIndex(int index, int value) {
        if (index == -1) {
            autoArfcnIndex0 = value;
            autoArfcnIndex1 = value;
            autoArfcnIndex2 = value;
            autoArfcnIndex3 = value;
            autoArfcnIndex4 = value;
            autoArfcnIndex5 = value;
            autoArfcnIndex6 = value;
            autoArfcnIndex7 = value;
            autoArfcnIndex8 = value;
            autoArfcnIndex9 = value;
            autoArfcnIndex10 = value;
            autoArfcnIndex11 = value;
        } else {
            switch (index) {
                case 0:
                    autoArfcnIndex0 = value;
                    break;
                case 1:
                    autoArfcnIndex1 = value;
                    break;
                case 2:
                    autoArfcnIndex2 = value;
                    break;
                case 3:
                    autoArfcnIndex3 = value;
                    break;
                case 4:
                    autoArfcnIndex4 = value;
                    break;
                case 5:
                    autoArfcnIndex5 = value;
                    break;
                case 6:
                    autoArfcnIndex6 = value;
                    break;
                case 7:
                    autoArfcnIndex7 = value;
                    break;
                case 8:
                    autoArfcnIndex8 = value;
                    break;
                case 9:
                    autoArfcnIndex9 = value;
                    break;
                case 10:
                    autoArfcnIndex10 = value;
                    break;
                case 11:
                    autoArfcnIndex11 = value;
                    break;
            }
        }
    }

    //自动定位和手动定位的共同方法
    private void runCell0(String id, int channelNum) {
        if (DeviceUtil.getIndexById(id) == -1) return;
        MainActivity.getInstance().getDeviceList().get(DeviceUtil.getIndexById(id)).getTraceUtil().setWorkState(DeviceUtil.getCellIdByChannelNum(channelNum), GnbBean.DW_State.BLACKLIST);
        mHandler.postDelayed(() -> {
            updateParamAndStart(id, arfcnBeanHashMap.get("TD" + channelNum).get(0), DeviceUtil.getCellIdByChannelNum(channelNum));
        }, delay.get());
        if (delay.intValue() == 2500) freqEndGoRunWork();
    }

    private void runCell1(String id, int delay, int channelNum) {
        if (DeviceUtil.getIndexById(id) == -1) return;
        MainActivity.getInstance().getDeviceList().get(DeviceUtil.getIndexById(id)).getTraceUtil().setWorkState(DeviceUtil.getCellIdByChannelNum(channelNum), GnbBean.DW_State.BLACKLIST);
        int logicIndex = DeviceUtil.getLogicIndexById(id);
        mHandler.postDelayed(() -> {
            ArfcnPciBean bean = arfcnBeanHashMap.get("TD" + channelNum).get(0);
            //如果主B40失败跳过次B40
            if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 40 && mainB40Success == 0) {
                if (arfcnBeanHashMap.get("TD2").size() == 1) {   //只有一个频点
                    updateLeftTvState(1, 0, logicIndex, 1, getString(R.string.open_fail), true);
                    int indexById = MainActivity.getInstance().getIndexById(id);
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(1, GnbBean.DW_State.IDLE);
                    MainActivity.getInstance().updateSteps(1, StepBean.State.fail, getString(R.string.open_fail));
                    Util.showToast(getString(R.string.cell) + "二次频点开启失败", Toast.LENGTH_LONG);
                    refreshTraceBtn();
                    return;
                } else {    //从下一个频点开始
                    autoArfcnIndex0++;
                    bean = arfcnBeanHashMap.get("TD" + channelNum).get(1);
                }
            }

            //判断是否是主B3
            if (bean.getArfcn().length() < 6 && LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 3) {
                isMainB3 = true;
            }
            updateParamAndStart(id, bean, DeviceUtil.getCellIdByChannelNum(channelNum));
        }, delay);
        if (delay == 2500) freqEndGoRunWork();
    }

    private void runCell2(String id, int delay, int channelNum) {
        if (DeviceUtil.getIndexById(id) == -1) return;
        MainActivity.getInstance().getDeviceList().get(DeviceUtil.getIndexById(id)).getTraceUtil().setWorkState(DeviceUtil.getCellIdByChannelNum(channelNum), GnbBean.DW_State.BLACKLIST);
        mHandler.postDelayed(() ->
                updateParamAndStart(id, arfcnBeanHashMap.get("TD" + channelNum).get(0), DeviceUtil.getCellIdByChannelNum(channelNum)), delay);
        if (delay == 2500) freqEndGoRunWork();
    }

    private void runCell3(String id, int delay, int channelNum) {
        if (DeviceUtil.getIndexById(id) == -1) return;
        MainActivity.getInstance().getDeviceList().get(DeviceUtil.getIndexById(id)).getTraceUtil().setWorkState(DeviceUtil.getCellIdByChannelNum(channelNum), GnbBean.DW_State.BLACKLIST);
        int logicIndex = DeviceUtil.getLogicIndexById(id);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ArfcnPciBean bean = arfcnBeanHashMap.get("TD" + channelNum).get(0);
                //如果主B3失败跳过次B3
                if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 3 && mainB40Success == 0) {
                    if (arfcnBeanHashMap.get("TD" + logicIndex * DeviceUtil.MaxCellIdPerDevice + 4).size() == 1) {   //只有一个频点
                        updateLeftTvState(1, 0, logicIndex, 1, getString(R.string.open_fail), true);
                        int indexById = MainActivity.getInstance().getIndexById(id);
                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(3, GnbBean.DW_State.IDLE);
                        MainActivity.getInstance().updateSteps(1, StepBean.State.fail, getString(R.string.open_fail));
                        Util.showToast(getString(R.string.cell) + "四次频点开启失败", Toast.LENGTH_LONG);
                        refreshTraceBtn();
                        return;
                    } else {    //从下一个频点开始
                        autoArfcnIndex3++;
                        bean = arfcnBeanHashMap.get("TD" + channelNum).get(1);
                    }
                }

                //判断是否是主B40
                if (bean.getArfcn().length() < 6 && LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 40) {
                    isMainB40 = true;
                }
                updateParamAndStart(id, bean, DeviceUtil.getCellIdByChannelNum(channelNum));
            }
        }, delay);
        if (delay == 2500) freqEndGoRunWork();
    }

    private void runCell4(String id, int delay, int channelNum) {
        if (DeviceUtil.getIndexById(id) == -1) return;
        MainActivity.getInstance().getDeviceList().get(DeviceUtil.getIndexById(id)).getTraceUtil().setWorkState(DeviceUtil.getCellIdByChannelNum(channelNum), GnbBean.DW_State.BLACKLIST);
        mHandler.postDelayed(() -> {
            updateParamAndStart(id, arfcnBeanHashMap.get("TD" + channelNum).get(0), DeviceUtil.getCellIdByChannelNum(channelNum));
        }, delay);
        if (delay == 2500) freqEndGoRunWork();
    }

    public void freqEndGoRunWork() {
        view_pager.setCurrentItem(1);
        Util.showToast(getString(R.string.cfging_trace));
        refreshTraceBtn();
    }

    public int getTime_count() {
        return time_count;
    }

    public void setTime_count(int time_count) {
        this.time_count = time_count;

    }

    public void resetArfcnList() {
        if (mCfgTraceChildFragment != null)
            mCfgTraceChildFragment.resetArfcnList();
    }

    public void setIfDebug(boolean b) {
        mCatchChildFragment.setTv_add_testVisibility(b);
        MainActivity.getInstance().setbtn_testVisibility(b);
    }

    private void autoArfcnIndexToNum(int channelNum, int autoArfcnIndex) {
        if (channelNum == 1) {
            autoArfcnIndex0 = autoArfcnIndex;
        } else if (channelNum == 2) {
            autoArfcnIndex1 = autoArfcnIndex;
        } else if (channelNum == 3) {
            autoArfcnIndex2 = autoArfcnIndex;
        } else if (channelNum == 4) {
            autoArfcnIndex3 = autoArfcnIndex;
        } else if (channelNum == 5) {
            autoArfcnIndex4 = autoArfcnIndex;
        } else if (channelNum == 6) {
            autoArfcnIndex5 = autoArfcnIndex;
        } else if (channelNum == 7) {
            autoArfcnIndex6 = autoArfcnIndex;
        } else if (channelNum == 8) {
            autoArfcnIndex7 = autoArfcnIndex;
        } else if (channelNum == 9) {
            autoArfcnIndex8 = autoArfcnIndex;
        } else if (channelNum == 10) {
            autoArfcnIndex9 = autoArfcnIndex;
        } else if (channelNum == 11) {
            autoArfcnIndex10 = autoArfcnIndex;
        } else {
            autoArfcnIndex11 = autoArfcnIndex;
        }
    }

    private void nextArfcnInSameBand(int channelNum, String arfcn, String pci,
                                     int arfcnListSize) {
        int autoArfcnIndex = channelNumToAutoArfcnIndex(channelNum);
        int cell_id = DeviceUtil.getCellIdByChannelNum(channelNum);
        int logicIndex = DeviceUtil.getLogicIndexByChannelNum(channelNum);
        int index = DeviceUtil.getIndexByChannelNum(channelNum);
        if (index == -1) return;
        TraceUtil mTraceUtil = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
        AppLog.D("mHandler 1" + channelNum + " change arfcn = " + arfcn + ", autoArfcnIndex" + (channelNum - 1) + " = " + autoArfcnIndex);
        //更新页面信息
        if (mTraceUtil == null) return;
        mTraceUtil.setArfcn(cell_id, arfcn);
        mTraceUtil.setPci(cell_id, pci);
        freshDoWorkState(0, logicIndex, cell_id, mTraceUtil.getImsi(cell_id), arfcn, pci);
        MessageController.build().setArfcn(MainActivity.getInstance().getDeviceList().get(index).getRsp().getDeviceId(), cell_id, arfcn);
        MessageController.build().setPci(MainActivity.getInstance().getDeviceList().get(index).getRsp().getDeviceId(), cell_id, pci);
        autoArfcnIndex++;
        AppLog.D("channelNum=" + channelNum + "autoArfcnIndex++, autoArfcnIndex =" + (channelNum - 1) + " " + autoArfcnIndex);
        if (autoArfcnIndex >= arfcnListSize) autoArfcnIndex = 0;
        autoArfcnIndexToNum(channelNum, autoArfcnIndex);
    }

    private int channelNumToAutoArfcnIndex(int channelNum) {
        if (channelNum == 1) {
            return autoArfcnIndex0;
        } else if (channelNum == 2) {
            return autoArfcnIndex1;
        } else if (channelNum == 3) {
            return autoArfcnIndex2;
        } else if (channelNum == 4) {
            return autoArfcnIndex3;
        } else if (channelNum == 5) {
            return autoArfcnIndex4;
        } else if (channelNum == 6) {
            return autoArfcnIndex5;
        } else if (channelNum == 7) {
            return autoArfcnIndex6;
        } else if (channelNum == 8) {
            return autoArfcnIndex7;
        } else if (channelNum == 9) {
            return autoArfcnIndex8;
        } else if (channelNum == 10) {
            return autoArfcnIndex9;
        } else if (channelNum == 11) {
            return autoArfcnIndex10;
        } else if (channelNum == 12) {
            return autoArfcnIndex11;
        }
        return autoArfcnIndex0;
    }

    private void nextArfcnInDiffBand(int channelNum, String value, String pci,
                                     int arfcnListSize) {
        int autoArfcnIndex = channelNumToAutoArfcnIndex(channelNum);
        int cell_id = DeviceUtil.getCellIdByChannelNum(channelNum);
        int logicIndex = DeviceUtil.getLogicIndexByChannelNum(channelNum);
        int index = DeviceUtil.getIndexByChannelNum(channelNum);
        boolean isNr = DeviceUtil.isNr(channelNum);
        if (index == -1) return;
        TraceUtil mTraceUtil = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
        String deviceId = MainActivity.getInstance().getDeviceList().get(index).getRsp().getDeviceId();
        AppLog.D("mHandler 1" + channelNum + " change arfcn = " + value + " is different band, stop at first" + ", autoArfcnIndex" + (channelNum - 1) + " = " + autoArfcnIndex);
        setAutoRun(channelNum, true);
        if (DeviceUtil.isNr(channelNum))
            MessageController.build().stopDWNrTrace(deviceId, cell_id);
        else
            MessageController.build().stopDWLteTrace(deviceId, cell_id);
        PaCtl.build().closePAByArfcn(deviceId, !isNr, mTraceUtil.getArfcn(cell_id), DeviceUtil.getMainChannelNumByarfcn_3G758_CX(mTraceUtil.getArfcn(cell_id)));
        MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setArfcn(cell_id, value);
        MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setPci(cell_id, pci);
        autoArfcnIndex++;
        if (autoArfcnIndex >= arfcnListSize) autoArfcnIndex = 0;
        autoArfcnIndexToNum(channelNum, autoArfcnIndex);
    }

    int sendIndex = 0;  //每3秒发2次
    int testNum = 0;
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            boolean isStartCatchHandler = false;    //是否有上号的
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 4; j++) {
                    if (getIsStartCatchHandler(i, j)) {
                        isStartCatchHandler = true;
                        break;
                    }
                }
            }
            switch (msg.what) {
//                case 666:
//                    if (mTraceChildFragment != null) {
//                        mTraceChildFragment.testRsrp(testNum);
//                        testNum++;
//                    }
//                    mHandler.sendEmptyMessageDelayed(666, 1000);
//                    break;
                case 1:
                    if (dataFwdBean == null) return;
                    Message msg2 = new Message();
                    msg2.what = msg.what;
                    if (sendIndex >= 2) {
                        mHandler.sendMessageDelayed(msg2, 3000);
                        sendIndex = 0;
                    } else {
                        Gson gson = new Gson();
                        String json = gson.toJson(dataFwdBean);
                        for (DeviceInfoBean deviceInfoBean : MainActivity.getInstance().getDeviceList()) {
                            AppLog.D("handler 1 setDWDataFwd deviceInfoBean.getRsp().getDeviceId()" + deviceInfoBean.getRsp().getDeviceId() + "json = " + json);
                            MessageController.build().setDWDataFwd(deviceInfoBean.getRsp().getDeviceId(), json);
                        }
                        mHandler.sendMessageDelayed(msg2, 100);
                        sendIndex++;
                    }
                    break;
                case 2: //刷新数值
                    List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
                    for (int i = 0; i < deviceList.size(); i++) {
                        TraceUtil traceUtil = deviceList.get(i).getTraceUtil();
                        int logicIndex = DeviceUtil.getLogicIndexById(deviceList.get(i).getRsp().getDeviceId());
                        for (int cell_id = 0; cell_id < DeviceUtil.MaxCellIdPerDevice; cell_id++) {
                            if (traceUtil.getWorkState(cell_id) == GnbBean.DW_State.TRACE) {
                                int countLost = getCountLost(logicIndex, cell_id);
                                boolean ifRecountNum = getIfRecountNum(logicIndex, cell_id);
                                boolean isStartCatchHandlerNum = getIsStartCatchHandlerNum(logicIndex, cell_id);
                                int countIfsetRsrpValue = getCountIfsetRsrpValue(logicIndex, cell_id);
                                int rsrp = -1;
                                if (traceUtil.getRsrp(cell_id) != -1) {
                                    rsrp = traceUtil.getRsrp(cell_id);
                                    countLost = 0;
                                    ifRecountNum = true;
                                } else {
                                    countLost++;
                                }
//                                AppLog.I("mHandler 2 logicIndex = " + logicIndex + " cell_id = " + cell_id +
//                                        "getRsrp=" + traceUtil.getRsrp(cell_id) + " countLost = " + countLost);
                                if (countLost < 6) {
                                    if (rsrp != -1) {
                                        mTraceChildFragment.setRsrpValue(
                                                mCfgTraceChildFragment.isYiguangChecked(), cell_id, i, ParseDataUtil.build().getRxByMode(mTraceChildFragment.getParseMode(), cell_id));
                                    }
                                    countIfsetRsrpValue = 0;
                                } else {
                                    //掉线，恢复轮询
                                    if (ifRecountNum) {
                                        AppLog.I("通道" + getCellStr(logicIndex, cell_id) + "掉线重计时--------------------");
                                        mHandler.removeMessages(logicIndex * DeviceUtil.MaxCellIdPerDevice + cell_id + 1 + 10);
                                        mHandler.sendEmptyMessageDelayed(logicIndex * DeviceUtil.MaxCellIdPerDevice + cell_id + 1 + 10, time_count * 1000L);
                                        ifRecountNum = false;
                                    }
                                    if (isStartCatchHandlerNum) {
                                        countIfsetRsrpValue++;
                                        AppLog.D("countIfsetRsrpValue = " + countIfsetRsrpValue + "logicIndex = " + logicIndex + "cell_id = " + cell_id);
                                    }
                                    if (countIfsetRsrpValue > 7 || isOtherStartCatchHandlerUp(logicIndex, cell_id))  //如果掉线超过7次或者其他的上线了则置为掉线
                                        isStartCatchHandlerNum = false;
                                    if (isStartCatchHandlerNum) { //是上号的那个频点才报-1 定位已掉线
                                        mTraceChildFragment.setRsrpValue(
                                                mCfgTraceChildFragment.isYiguangChecked(), cell_id, i, -1);
                                    }
                                }
                                setCountIfsetRsrpValue(logicIndex, cell_id, countIfsetRsrpValue);
                                setCountLost(logicIndex, cell_id, countLost);
                                setIfRecountNum(logicIndex, cell_id, ifRecountNum);
                                setIsStartCatchHandlerNum(logicIndex, cell_id, isStartCatchHandlerNum);
                            }
                        }
                    }

                    mHandler.sendEmptyMessageDelayed(2, TraceBean.RSRP_TIME_INTERVAL);
                    break;
                case 9:
//                    mCatchChildFragment.resetShowData(false); // 刷新视图
//                    AppLog.D("mhandler 9 ");
                    //刷新某一项
                    mCatchChildFragment.notifyMimsiAdapterItemChanged();
                    mHandler.sendEmptyMessageDelayed(9, 1500);
                    break;
                case 11:
                    AppLog.I("mHandler message 11----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler) { //上号不执行
                        LinkedList<ArfcnPciBean> arfcnList = arfcnBeanHashMap.get("TD1");
                        AppLog.D("mHandler message 11 arfcnList size = " + arfcnList.size() + ", autoArfcnIndex0 = " + autoArfcnIndex0);
                        if (arfcnList == null || arfcnList.size() < 2) return;
                        int index = DeviceUtil.getIndexByChannelNum(1);
                        if (index == -1) return;
                        TraceUtil mLogicTraceUtilA = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex0) {
                                String value = String.valueOf(arfcnList.get(i).getArfcn());    //下一个准备轮询的arfcn值
                                String pci = String.valueOf(arfcnList.get(i).getPci());    //下一个准备轮询的arfcn值
                                if (NrBand.earfcn2band(Integer.parseInt(value)) == NrBand.earfcn2band(Integer.parseInt(mLogicTraceUtilA.getArfcn(DWProtocol.CellId.FIRST)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(1, value, pci, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(1, value, pci, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilA.setArfcn(DWProtocol.CellId.FIRST, value); //更新当前配置的值
                                mLogicTraceUtilA.setPci(DWProtocol.CellId.FIRST, pci); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(11, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(11, time_count * 1000L);
                    break;
                case 12:
                    AppLog.I("mHandler message 12");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler) {
                        LinkedList<ArfcnPciBean> arfcnList = arfcnBeanHashMap.get("TD2");
                        AppLog.D("mHandler message 12 arfcnList size = " + arfcnList.size() + ", autoArfcnIndex1 = " + autoArfcnIndex1);
                        if (arfcnList == null || arfcnList.size() < 2) return;
                        int index = DeviceUtil.getIndexByChannelNum(2);
                        if (index == -1) return;
                        TraceUtil mLogicTraceUtilA = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex1) {
                                String value = String.valueOf(arfcnList.get(i).getArfcn());
                                String pci = String.valueOf(arfcnList.get(i).getPci());
//                                //如果主B40没执行过，循环等待主B40结果,会卡在上一个频点等待
//                                if (LteBand.earfcn2band(Integer.parseInt(value)) == 40 && mainB40Success == -1) {
//                                    AppLog.I("mHandler 1 wait mainB40 arfcn = " + value + ", autoArfcnIndex = " + autoArfcnIndex1);
//                                    mHandler.sendEmptyMessageDelayed(12, 10 * 1000L);   //10秒检查一次
//                                    return;
//                                }
//                                //如果主B40失败跳过次B40
//                                if (LteBand.earfcn2band(Integer.parseInt(value)) == 40 && mainB40Success == 0) {
//                                    autoArfcnIndex1++;  //执行下一个频点
//                                    continue;
//                                }
                                if (LteBand.earfcn2band(Integer.parseInt(value)) == LteBand.earfcn2band(Integer.parseInt(mLogicTraceUtilA.getArfcn(DWProtocol.CellId.SECOND)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(2, value, pci, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(2, value, pci, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilA.setArfcn(DWProtocol.CellId.SECOND, value); //更新当前配置的值
                                mLogicTraceUtilA.setPci(DWProtocol.CellId.SECOND, pci); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(12, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(12, time_count * 1000L);
                    break;
                case 13:
                    AppLog.I("mHandler message 13");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler) {
                        LinkedList<ArfcnPciBean> arfcnList = arfcnBeanHashMap.get("TD3");
                        AppLog.D("mHandler message 13 arfcnList size = " + arfcnList.size() + ", autoArfcnIndex2 = " + autoArfcnIndex2);
                        if (arfcnList == null || arfcnList.size() < 2) return;
                        int index = DeviceUtil.getIndexByChannelNum(3);
                        if (index == -1) return;
                        TraceUtil mLogicTraceUtilA = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex2) {
                                String value = String.valueOf(arfcnList.get(i).getArfcn());
                                String pci = String.valueOf(arfcnList.get(i).getPci());
                                if (NrBand.earfcn2band(Integer.parseInt(value)) == NrBand.earfcn2band(Integer.parseInt(mLogicTraceUtilA.getArfcn(DWProtocol.CellId.THIRD)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(3, value, pci, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(3, value, pci, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilA.setArfcn(DWProtocol.CellId.THIRD, value); //更新当前配置的值
                                mLogicTraceUtilA.setPci(DWProtocol.CellId.THIRD, pci); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(13, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(13, time_count * 1000L);
                    break;
                case 14:
                    AppLog.I("mHandler message 14");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler) {
                        LinkedList<ArfcnPciBean> arfcnList = arfcnBeanHashMap.get("TD4");
                        AppLog.D("mHandler message 14 arfcnList size = " + arfcnList.size() + ", autoArfcnIndex3 = " + autoArfcnIndex3);
                        if (arfcnList == null || arfcnList.size() < 2) return;
                        int index = DeviceUtil.getIndexByChannelNum(4);
                        if (index == -1) return;
                        TraceUtil mLogicTraceUtilA = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex3) {
                                String value = String.valueOf(arfcnList.get(i).getArfcn());
                                String pci = String.valueOf(arfcnList.get(i).getPci());
                                String arfcn = mLogicTraceUtilA.getArfcn(DWProtocol.CellId.FOURTH);
//                                //如果主B3没执行过，循环等待主B3结果,会卡在上一个频点等待
//                                if (LteBand.earfcn2band(Integer.parseInt(value)) == 3 && mainB3Success == -1) {
//                                    AppLog.I("mHandler 3 wait mainB3 arfcn = " + value + ", autoArfcnIndex = " + autoArfcnIndex3);
//                                    mHandler.sendEmptyMessageDelayed(14, 10 * 1000L);   //10秒检查一次
//                                    return;
//                                }
//                                //如果主B3失败跳过次B3
//                                if (LteBand.earfcn2band(Integer.parseInt(value)) == 3 && mainB3Success == 0) {
//                                    autoArfcnIndex3++;  //执行下一个频点
//                                    continue;
//                                }
                                if (LteBand.earfcn2band(Integer.parseInt(value)) == LteBand.earfcn2band(Integer.parseInt(arfcn))) {
                                    int freqValue = LteBand2.earfcn2freq(Integer.parseInt(value));
                                    int freqArfcn = LteBand2.earfcn2freq(Integer.parseInt(arfcn));

                                    if (((freqArfcn > 1709 && freqArfcn < 1735) || (freqArfcn > 1804 && freqArfcn < 1830)) && (freqValue > 1709 && freqValue < 1735) || (freqValue > 1804 && freqValue < 1830)
                                            || ((freqArfcn > 888 && freqArfcn < 904) || (freqArfcn > 933 && freqArfcn < 949)) && (freqValue > 888 && freqValue < 904) || (freqValue > 933 && freqValue < 949)) {
                                        AppLog.I("mHandler 14 change arfcn = " + value + ", autoArfcnIndex = " + autoArfcnIndex3);
//                                        updateParam(3, value, mTraceUtilNr.getPci(DWProtocol.CellId.FOURTH));
                                        //更新页面信息
                                        freshDoWorkState(0, 0, DWProtocol.CellId.FOURTH, mLogicTraceUtilA.getImsi(DWProtocol.CellId.FOURTH), value, mLogicTraceUtilA.getPci(DWProtocol.CellId.FOURTH));
                                        MessageController.build().setArfcn(MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId(), 3, value);
                                        MessageController.build().setPci(MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId(), 3, pci);
                                    } else {
                                        AppLog.I("mHandler 14 change arfcn = " + value + " need stop, , autoArfcnIndex = " + autoArfcnIndex3);
                                        autoRun4 = true;
                                        String deviceId = MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId();
                                        MessageController.build().stopDWLteTrace(deviceId, DWProtocol.CellId.FOURTH);
                                        PaCtl.build().closePAByArfcn(deviceId, true, mLogicTraceUtilA.getArfcn(DWProtocol.CellId.FOURTH), DeviceUtil.getMainChannelNumByarfcn_3G758_CX(arfcn));
                                        MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setArfcn(DWProtocol.CellId.FOURTH, value);
                                        MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setPci(DWProtocol.CellId.FOURTH, pci);
                                    }
                                    autoArfcnIndex3++;
                                    if (autoArfcnIndex3 >= arfcnList.size())
                                        autoArfcnIndex3 = 0;
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(4, value, pci, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilA.setArfcn(DWProtocol.CellId.FOURTH, value); //更新当前配置的值
                                mLogicTraceUtilA.setPci(DWProtocol.CellId.FOURTH, pci); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(14, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(14, time_count * 1000L);
                    break;
                case 15:
                    AppLog.D("mHandler message 15----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler) {
                        LinkedList<ArfcnPciBean> arfcnList = arfcnBeanHashMap.get("TD5");
                        AppLog.D("mHandler message 15 arfcnList size = " + arfcnList.size() + ", autoArfcnIndex4 = " + autoArfcnIndex4);
                        if (arfcnList.size() == 1) return;
                        int index = DeviceUtil.getIndexByChannelNum(5);
                        if (index == -1) return;
                        TraceUtil mLogicTraceUtilB = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex4) {
                                String value = String.valueOf(arfcnList.get(i).getArfcn());
                                String pci = String.valueOf(arfcnList.get(i).getPci());
                                if (NrBand.earfcn2band(Integer.parseInt(value)) == NrBand.earfcn2band(Integer.parseInt(mLogicTraceUtilB.getArfcn(DWProtocol.CellId.FIRST)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(5, value, pci, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(5, value, pci, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilB.setArfcn(DWProtocol.CellId.FIRST, value); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(15, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(15, time_count * 1000L);
                    break;
                case 16:
                    AppLog.D("mHandler message 16----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler) {
                        LinkedList<ArfcnPciBean> arfcnList = arfcnBeanHashMap.get("TD6");
                        AppLog.D("mHandler message 16 arfcnList size = " + arfcnList.size() + ", autoArfcnIndex5 = " + autoArfcnIndex5);
                        if (arfcnList.size() == 1) return;
                        int index = DeviceUtil.getIndexByChannelNum(6);
                        if (index == -1) return;
                        TraceUtil mLogicTraceUtilB = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex5) {
                                String value = String.valueOf(arfcnList.get(i).getArfcn());    //下一个准备轮询的arfcn值
                                String pci = String.valueOf(arfcnList.get(i).getPci());    //下一个准备轮询的arfcn值
                                if (LteBand.earfcn2band(Integer.parseInt(value)) == LteBand.earfcn2band(Integer.parseInt(mLogicTraceUtilB.getArfcn(DWProtocol.CellId.SECOND)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(6, value, pci, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(6, value, pci, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilB.setArfcn(DWProtocol.CellId.SECOND, value); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(16, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(16, time_count * 1000L);
                    break;
                case 17:
                    AppLog.D("mHandler message 17----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler) {
                        LinkedList<ArfcnPciBean> arfcnList = arfcnBeanHashMap.get("TD7");
                        AppLog.D("mHandler message 17 arfcnList size = " + arfcnList.size() + ", autoArfcnIndex6 = " + autoArfcnIndex6);
                        if (arfcnList.size() == 1) return;
                        int index = DeviceUtil.getIndexByChannelNum(7);
                        if (index == -1) return;
                        TraceUtil mLogicTraceUtilB = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex6) {
                                String arfcn = String.valueOf(arfcnList.get(i).getArfcn());    //下一个准备轮询的arfcn值
                                String pci = String.valueOf(arfcnList.get(i).getPci());    //下一个准备轮询的arfcn值
                                if (NrBand.earfcn2band(Integer.parseInt(arfcn)) == NrBand.earfcn2band(Integer.parseInt(mLogicTraceUtilB.getArfcn(DWProtocol.CellId.THIRD)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(7, arfcn, pci, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(7, arfcn, pci, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilB.setArfcn(DWProtocol.CellId.THIRD, arfcn); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(17, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(17, time_count * 1000L);
                    break;
                case 18:
                    AppLog.D("mHandler message 18----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler) {
                        LinkedList<ArfcnPciBean> arfcnList = arfcnBeanHashMap.get("TD8");
                        AppLog.D("mHandler message 18 arfcnList size = " + arfcnList.size() + ", autoArfcnIndex7 = " + autoArfcnIndex7);
                        if (arfcnList.size() == 1) return;
                        int index = DeviceUtil.getIndexByChannelNum(8);
                        if (index == -1) return;
                        TraceUtil mLogicTraceUtilB = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex7) {
                                String value = String.valueOf(arfcnList.get(i).getArfcn());    //下一个准备轮询的arfcn值
                                String pci = String.valueOf(arfcnList.get(i).getPci());    //下一个准备轮询的arfcn值
                                if (LteBand.earfcn2band(Integer.parseInt(value)) == LteBand.earfcn2band(Integer.parseInt(mLogicTraceUtilB.getArfcn(DWProtocol.CellId.FOURTH)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(8, value, pci, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(8, value, pci, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilB.setArfcn(DWProtocol.CellId.FOURTH, value); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(18, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(18, time_count * 1000L);
                    break;
                case 19:
                    AppLog.D("mHandler message 19----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler) {
                        LinkedList<ArfcnPciBean> arfcnList = arfcnBeanHashMap.get("TD9");
                        AppLog.D("mHandler message 19 arfcnList size = " + arfcnList.size() + ", autoArfcnIndex8 = " + autoArfcnIndex8);
                        if (arfcnList.size() == 1) return;
                        int index = DeviceUtil.getIndexByChannelNum(9);
                        if (index == -1) return;
                        TraceUtil mLogicTraceUtilC = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex8) {
                                String value = String.valueOf(arfcnList.get(i).getArfcn());    //下一个准备轮询的arfcn值
                                String pci = String.valueOf(arfcnList.get(i).getPci());    //下一个准备轮询的arfcn值
                                //N3或次B3
                                if (NrBand.earfcn2band(Integer.parseInt(value)) == NrBand.earfcn2band(Integer.parseInt(mLogicTraceUtilC.getArfcn(DWProtocol.CellId.FIRST)))
                                        || LteBand.earfcn2band(Integer.parseInt(value)) == LteBand.earfcn2band(Integer.parseInt(mLogicTraceUtilC.getArfcn(DWProtocol.CellId.FIRST)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(9, value, pci, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(9, value, pci, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilC.setArfcn(DWProtocol.CellId.FIRST, value); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(19, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(19, time_count * 1000L);
                    break;
                case 20:
                    AppLog.D("mHandler message 20----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler) {
                        LinkedList<ArfcnPciBean> arfcnList = arfcnBeanHashMap.get("TD10");
                        AppLog.D("mHandler message 20 arfcnList size = " + arfcnList.size() + ", autoArfcnIndex9 = " + autoArfcnIndex9);
                        if (arfcnList.size() == 1) return;
                        int index = DeviceUtil.getIndexByChannelNum(10);
                        if (index == -1) return;
                        TraceUtil mLogicTraceUtilC = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex9) {
                                String value = String.valueOf(arfcnList.get(i).getArfcn());    //下一个准备轮询的arfcn值
                                String pci = String.valueOf(arfcnList.get(i).getPci());    //下一个准备轮询的arfcn值
                                if (LteBand.earfcn2band(Integer.parseInt(value)) == LteBand.earfcn2band(Integer.parseInt(mLogicTraceUtilC.getArfcn(DWProtocol.CellId.SECOND)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(10, value, pci, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(10, value, pci, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilC.setArfcn(DWProtocol.CellId.SECOND, value); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(20, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(20, time_count * 1000L);
                    break;
                case 21:
                    AppLog.D("mHandler message 21----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler) {
                        LinkedList<ArfcnPciBean> arfcnList = arfcnBeanHashMap.get("TD11");
                        AppLog.D("mHandler message 21 arfcnList size = " + arfcnList.size() + ", autoArfcnIndex10 = " + autoArfcnIndex10);
                        if (arfcnList.size() == 1) return;
                        int index = DeviceUtil.getIndexByChannelNum(11);
                        if (index == -1) return;
                        TraceUtil mLogicTraceUtilC = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex10) {
                                String value = String.valueOf(arfcnList.get(i).getArfcn());    //下一个准备轮询的arfcn值
                                String pci = String.valueOf(arfcnList.get(i).getPci());    //下一个准备轮询的arfcn值
                                if (LteBand.earfcn2band(Integer.parseInt(value)) == LteBand.earfcn2band(Integer.parseInt(mLogicTraceUtilC.getArfcn(DWProtocol.CellId.THIRD)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(11, value, pci, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(11, value, pci, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilC.setArfcn(DWProtocol.CellId.THIRD, value); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(21, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(21, time_count * 1000L);
                    break;
                case 22:
                    AppLog.D("mHandler message 22----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler) {
                        LinkedList<ArfcnPciBean> arfcnList = arfcnBeanHashMap.get("TD12");
                        AppLog.D("mHandler message 22 arfcnList size = " + arfcnList.size() + ", autoArfcnIndex11 = " + autoArfcnIndex11);
                        if (arfcnList.size() == 1) return;
                        int index = DeviceUtil.getIndexByChannelNum(12);
                        if (index == -1) return;
                        TraceUtil mLogicTraceUtilC = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex11) {
                                String value = String.valueOf(arfcnList.get(i).getArfcn());    //下一个准备轮询的arfcn值
                                String pci = String.valueOf(arfcnList.get(i).getPci());    //下一个准备轮询的arfcn值
                                if (LteBand.earfcn2band(Integer.parseInt(value)) == LteBand.earfcn2band(Integer.parseInt(mLogicTraceUtilC.getArfcn(DWProtocol.CellId.FOURTH)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(12, value, pci, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(12, value, pci, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilC.setArfcn(DWProtocol.CellId.FOURTH, value); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(22, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(22, time_count * 1000L);
                    break;
            }
        }
    };

    private void setCountIfsetRsrpValue(int logicIndex, int cellId, int countIfsetRsrpValue) {
        int channelNum = DeviceUtil.getChannelNum(logicIndex, cellId);
        switch (channelNum) {
            case 1:
                countIfsetRsrpValueFirst = countIfsetRsrpValue;
                break;
            case 2:
                countIfsetRsrpValueSecond = countIfsetRsrpValue;
                break;
            case 3:
                countIfsetRsrpValueThird = countIfsetRsrpValue;
                break;
            case 4:
                countIfsetRsrpValueFourth = countIfsetRsrpValue;
                break;
            case 5:
                countIfsetRsrpValueFifth = countIfsetRsrpValue;
                break;
            case 6:
                countIfsetRsrpValueSixth = countIfsetRsrpValue;
                break;
            case 7:
                countIfsetRsrpValueSeventh = countIfsetRsrpValue;
                break;
            case 8:
                countIfsetRsrpValueEighth = countIfsetRsrpValue;
                break;
            case 9:
                countIfsetRsrpValueNinth = countIfsetRsrpValue;
                break;
            case 10:
                countIfsetRsrpValueTenth = countIfsetRsrpValue;
                break;
            case 11:
                countIfsetRsrpValueEleventh = countIfsetRsrpValue;
                break;
            case 12:
                countIfsetRsrpValueTwelfth = countIfsetRsrpValue;
                break;
        }
    }

    private int getCountIfsetRsrpValue(int logicIndex, int cellId) {
        int channelNum = DeviceUtil.getChannelNum(logicIndex, cellId);
        switch (channelNum) {
            case 1:
                return countIfsetRsrpValueFirst;
            case 2:
                return countIfsetRsrpValueSecond;
            case 3:
                return countIfsetRsrpValueThird;
            case 4:
                return countIfsetRsrpValueFourth;
            case 5:
                return countIfsetRsrpValueFifth;
            case 6:
                return countIfsetRsrpValueSixth;
            case 7:
                return countIfsetRsrpValueSeventh;
            case 8:
                return countIfsetRsrpValueEighth;
            case 9:
                return countIfsetRsrpValueNinth;
            case 10:
                return countIfsetRsrpValueTenth;
            case 11:
                return countIfsetRsrpValueEleventh;
            case 12:
                return countIfsetRsrpValueTwelfth;
            default:
                return 0;
        }
    }

    private void setIsStartCatchHandlerNum(int logicIndex, int cell_id,
                                           boolean isStartCatchHandlerNum) {
        int channelNum = DeviceUtil.getChannelNum(logicIndex, cell_id);
        switch (channelNum) {
            case 1:
                isStartCatchHandler1 = isStartCatchHandlerNum;
                break;
            case 2:
                isStartCatchHandler2 = isStartCatchHandlerNum;
                break;
            case 3:
                isStartCatchHandler3 = isStartCatchHandlerNum;
                break;
            case 4:
                isStartCatchHandler4 = isStartCatchHandlerNum;
                break;
            case 5:
                isStartCatchHandler5 = isStartCatchHandlerNum;
                break;
            case 6:
                isStartCatchHandler6 = isStartCatchHandlerNum;
                break;
            case 7:
                isStartCatchHandler7 = isStartCatchHandlerNum;
                break;
            case 8:
                isStartCatchHandler8 = isStartCatchHandlerNum;
                break;
            case 9:
                isStartCatchHandler9 = isStartCatchHandlerNum;
                break;
            case 10:
                isStartCatchHandler10 = isStartCatchHandlerNum;
                break;
            case 11:
                isStartCatchHandler11 = isStartCatchHandlerNum;
                break;
            case 12:
                isStartCatchHandler12 = isStartCatchHandlerNum;
                break;
        }
    }

    private void setIfRecountNum(int logicIndex, int cell_id, boolean ifRecountNum) {
        int channelNum = DeviceUtil.getChannelNum(logicIndex, cell_id);
        switch (channelNum) {
            case 1:
                ifRecountFirst = ifRecountNum;
                break;
            case 2:
                ifRecountSecond = ifRecountNum;
                break;
            case 3:
                ifRecountThird = ifRecountNum;
                break;
            case 4:
                ifRecountFourth = ifRecountNum;
                break;
            case 5:
                ifRecountFifth = ifRecountNum;
                break;
            case 6:
                ifRecountSixth = ifRecountNum;
                break;
            case 7:
                ifRecountSeventh = ifRecountNum;
                break;
            case 8:
                ifRecountEighth = ifRecountNum;
                break;
            case 9:
                ifRecountNinth = ifRecountNum;
                break;
            case 10:
                ifRecountTenth = ifRecountNum;
                break;
            case 11:
                ifRecountEleventh = ifRecountNum;
                break;
            case 12:
                ifRecountTwelfth = ifRecountNum;
                break;
        }
    }

    private void setCountLost(int logicIndex, int cell_id, int countLost) {
        int channelNum = DeviceUtil.getChannelNum(logicIndex, cell_id);
        switch (channelNum) {
            case 1:
                countFirst = countLost;
                break;
            case 2:
                countSecond = countLost;
                break;
            case 3:
                countThird = countLost;
                break;
            case 4:
                countFourth = countLost;
                break;
            case 5:
                countFifth = countLost;
                break;
            case 6:
                countSixth = countLost;
                break;
            case 7:
                countSeventh = countLost;
                break;
            case 8:
                countEighth = countLost;
                break;
            case 9:
                countNinth = countLost;
                break;
            case 10:
                countTenth = countLost;
                break;
            case 11:
                countEleventh = countLost;
                break;
            case 12:
                countTwelfth = countLost;
                break;
        }
    }

    private boolean getIsStartCatchHandlerNum(int logicIndex, int cell_id) {
        int channelNum = DeviceUtil.getChannelNum(logicIndex, cell_id);
        switch (channelNum) {
            case 1:
                return isStartCatchHandler1;
            case 2:
                return isStartCatchHandler2;
            case 3:
                return isStartCatchHandler3;
            case 4:
                return isStartCatchHandler4;
            case 5:
                return isStartCatchHandler5;
            case 6:
                return isStartCatchHandler6;
            case 7:
                return isStartCatchHandler7;
            case 8:
                return isStartCatchHandler8;
            case 9:
                return isStartCatchHandler9;
            case 10:
                return isStartCatchHandler10;
            case 11:
                return isStartCatchHandler11;
            case 12:
                return isStartCatchHandler12;
        }
        return isStartCatchHandler1;
    }

    //是否其他的通道上号了
    private boolean isOtherStartCatchHandlerUp(int logicIndex, int cell_id) {
        boolean isOtherStartCatchHandlerUp = false;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == logicIndex && j == cell_id) continue;
                if (getIsStartCatchHandlerNum(i, j)) {
                    isOtherStartCatchHandlerUp = true;
                    break;
                }
            }
        }
        return isOtherStartCatchHandlerUp;
    }

    private boolean getIfRecountNum(int logicIndex, int cell_id) {
        int channelNum = DeviceUtil.getChannelNum(logicIndex, cell_id);
        switch (channelNum) {
            case 1:
                return ifRecountFirst;
            case 2:
                return ifRecountSecond;
            case 3:
                return ifRecountThird;
            case 4:
                return ifRecountFourth;
            case 5:
                return ifRecountFifth;
            case 6:
                return ifRecountSixth;
            case 7:
                return ifRecountSeventh;
            case 8:
                return ifRecountEighth;
            case 9:
                return ifRecountNinth;
            case 10:
                return ifRecountTenth;
            case 11:
                return ifRecountEleventh;
            case 12:
                return ifRecountTwelfth;
        }
        return ifRecountFirst;
    }

    private int getCountLost(int logicIndex, int cell_id) {
        int channelNum = DeviceUtil.getChannelNum(logicIndex, cell_id);
        switch (channelNum) {
            case 1:
                return countFirst;
            case 2:
                return countSecond;
            case 3:
                return countThird;
            case 4:
                return countFourth;
            case 5:
                return countFifth;
            case 6:
                return countSixth;
            case 7:
                return countSeventh;
            case 8:
                return countEighth;
            case 9:
                return countNinth;
            case 10:
                return countTenth;
            case 11:
                return countEleventh;
            case 12:
                return countTwelfth;
        }
        return countFirst;
    }


    public TraceChildFragment getmTraceChildFragment() {
        return mTraceChildFragment;
    }

    public void sendEmptyMessageIfNotExist(int what) {
        if (!mHandler.hasMessages(what)) {
            mHandler.sendEmptyMessage(what);
        }
    }

    public boolean isClickStop() {
        return isClickStop;
    }

    public void setSettingFragment(SettingFragment mSettingFragment) {
        this.mSettingFragment = mSettingFragment;
    }

    @Override
    public boolean getIsStartCatchHandler(int logicIndex, int cell_id) {
        return getIsStartCatchHandlerNum(logicIndex, cell_id);
    }
}