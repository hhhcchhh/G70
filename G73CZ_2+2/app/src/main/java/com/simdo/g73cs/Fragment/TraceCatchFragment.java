package com.simdo.g73cs.Fragment;

import static com.simdo.g73cs.MainActivity.devA;
import static com.simdo.g73cs.MainActivity.devB;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.FTP.FTPUtil;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.UeidBean;
import com.nr.Gnb.Response.GnbCmdRsp;
import com.nr.Gnb.Response.GnbFreqScanRsp;
import com.nr.Gnb.Response.GnbStateRsp;
import com.nr.Gnb.Response.GnbTraceRsp;
import com.nr.Socket.MessageControl.MessageController;
import com.nr.Util.OpLog;
import com.simdo.g73cs.Adapter.FragmentAdapter;
import com.simdo.g73cs.Bean.ArfcnBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Bean.TraceBean;
import com.simdo.g73cs.Dialog.FreqDialog;
import com.simdo.g73cs.Listener.OnTraceSetListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DBUtil;
import com.simdo.g73cs.Util.DateUtil;
import com.simdo.g73cs.Util.DeviceUtil;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.TraceUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.View.BlackListSlideAdapter;
import com.simdo.g73cs.ViewModel.TraceCatchVM;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

//1
public class TraceCatchFragment extends Fragment implements OnTraceSetListener {

    Context mContext;
    private List<ImsiBean> mImsiList;   //侦码imsi列表
    private BlackListSlideAdapter adapter;
    public static TraceUtil mLogicTraceUtilNr;  //单设备的所有信息包括arfcn，最后会拿出来用
    public static TraceUtil mLogicTraceUtilNr2;
    public static TraceUtil mLogicTraceUtilLte;
    public static TraceUtil mLogicTraceUtilLte2;
    private int time_count = 60;    //轮询时间
//    private int time_count = 30;    //轮询时间

    FreqFragment mFreqFragment;
    //是否频点轮询
    private boolean isCell1SetArfcnChange, isCell2SetArfcnChange, isCell3SetArfcnChange, isCell4SetArfcnChange;
    private boolean isCell5SetArfcnChange, isCell6SetArfcnChange, isCell7SetArfcnChange, isCell8SetArfcnChange;
    int autoArfcnIndex0 = 0, autoArfcnIndex1 = 0, autoArfcnIndex2 = 0, autoArfcnIndex3 = 0;
    int autoArfcnIndex4 = 0, autoArfcnIndex5 = 0, autoArfcnIndex6 = 0, autoArfcnIndex7 = 0;
    boolean isClickStop = false;
    //设置是否要在结束时接着运行（切换的频点不在同一个频段的时候）
    boolean autoRun1 = false, autoRun2 = false, autoRun3 = false, autoRun4 = false;
    boolean autoRun5 = false, autoRun6 = false, autoRun7 = false, autoRun8 = false;

    FreqDialog mFreqDialog;
    private String auto_trace_imsi;

    private TraceCatchVM traceCatchVM;


    public TraceCatchFragment(Context context) {
        this.mContext = context;
    }

    public TraceCatchFragment(Context context, FreqFragment freqFragment) {
        this.mContext = context;
        this.mFreqFragment = freqFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AppLog.I("TraceCatchFragment onCreate");
        super.onCreate(savedInstanceState);
        traceCatchVM = new ViewModelProvider(requireActivity()).get(TraceCatchVM.class);

        traceCatchVM.getDoWorkNow().observe(this, this::doWork);
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

    public CfgTraceChildFragment getmCfgTraceChildFragment() {
        return mCfgTraceChildFragment;
    }

    CfgTraceChildFragment mCfgTraceChildFragment;
    TraceChildFragment mTraceChildFragment;
    CatchChildFragment mCatchChildFragment;
    ViewPager2 view_pager;
    //存储所有通道里的arfcnList，频点轮询时拿出来使用
    public final HashMap<String, ArfcnBean> arfcnBeanHashMap = new HashMap<>();

    private void initView(View root) {
        // 左右滑动导航
        List<Fragment> fragmentList = new ArrayList<>();
        mCfgTraceChildFragment = new CfgTraceChildFragment(mContext, this, mFreqFragment);
        mTraceChildFragment = new TraceChildFragment(mContext);
        mCatchChildFragment = new CatchChildFragment(mContext, mImsiList, this);
        fragmentList.add(mCfgTraceChildFragment);
        fragmentList.add(mTraceChildFragment);
        fragmentList.add(mCatchChildFragment);
        String[] titles = new String[]{getString(R.string.cfg_trace), getString(R.string.target_report), getString(R.string.catch_list)};

        view_pager = root.findViewById(R.id.view_pager_trace_catch);
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getChildFragmentManager(), getLifecycle(), fragmentList);
        view_pager.setAdapter(fragmentAdapter);
        view_pager.setCurrentItem(0);
        TabLayout tab_layout = root.findViewById(R.id.tab_trace_catch);

//        setTabWidthSame(tab_layout);

        TabLayoutMediator tab = new TabLayoutMediator(tab_layout, view_pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setCustomView(R.layout.tab_layout_item);
                TextView textView = tab.getCustomView().findViewById(R.id.tabTextView);
                textView.setText(titles[position]);
                // 根据选中状态设置不同的颜色
                tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab selectedTab) {
                        if (selectedTab.getPosition() == position) {
                            textView.setTextColor(Color.parseColor("#FF1663FF"));
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab unselectedTab) {
                        if (unselectedTab.getPosition() == position) {
                            textView.setTextColor(Color.parseColor("#ffffff"));
                        }
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        // Do nothing
                    }
                });
            }
        });
        tab.attach();


    }

    public void clickClean() {
        mCatchChildFragment.clear();
        all_count = 0;
        unicom_count = 0;
        mobile_count = 0;
        telecom_count = 0;
        sva_count = 0;
//        mCatchChildFragment.setCatchCount(all_count, telecom_count, mobile_count, unicom_count, sva_count);
    }

    public void initAutoArfcnList() {
        LinkedList<Integer> TD1 = new LinkedList<>(); // N28/N78/N79
        TD1.add(627264);
        TD1.add(633984);
        TD1.add(154810);
        TD1.add(152650);
        TD1.add(723360);

        LinkedList<Integer> TD2 = new LinkedList<>(); // B34/B39/B40/B41
        TD2.add(38950);
        TD2.add(38350);
        TD2.add(36275);
        TD2.add(38400);
        TD2.add(39148);
        TD2.add(40936);
        TD2.add(38544);
        TD2.add(39292);

        LinkedList<Integer> TD3 = new LinkedList<>(); // N1/N41
        TD3.add(504990);
        TD3.add(428910);
        TD3.add(427250);
        TD3.add(422890);
        TD3.add(516990);

        LinkedList<Integer> TD4 = new LinkedList<>(); // B1/B3/B5/B8
        TD4.add(1650);
        TD4.add(100);
        TD4.add(1300);
        TD4.add(1850);
        TD4.add(2452);
        TD4.add(3683);
        TD4.add(450);
        TD4.add(1825);

        arfcnBeanHashMap.put("TD1", new ArfcnBean(TD1));
        arfcnBeanHashMap.put("TD2", new ArfcnBean(TD2));
        arfcnBeanHashMap.put("TD3", new ArfcnBean(TD3));
        arfcnBeanHashMap.put("TD4", new ArfcnBean(TD4));
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
        mLogicTraceUtilNr = new TraceUtil();
        mLogicTraceUtilNr2 = new TraceUtil();
        mLogicTraceUtilLte = new TraceUtil();
        mLogicTraceUtilLte2 = new TraceUtil();
        // 初始化定位参数
        mLogicTraceUtilNr.setTacChange(GnbProtocol.CellId.FIRST, false);
        mLogicTraceUtilNr.setTxPwr(GnbProtocol.CellId.FIRST, 0);
        mLogicTraceUtilNr.setCfr(GnbProtocol.CellId.FIRST, 1);
        mLogicTraceUtilNr.setUeMaxTxpwr(GnbProtocol.CellId.FIRST, "10");
        mLogicTraceUtilNr.setCid(GnbProtocol.CellId.FIRST, 65536);
        mLogicTraceUtilNr.setSplit_arfcn_dl(GnbProtocol.CellId.FIRST, "0");

        mLogicTraceUtilNr.setTacChange(GnbProtocol.CellId.SECOND, false);
        mLogicTraceUtilNr.setTxPwr(GnbProtocol.CellId.SECOND, 0);
        mLogicTraceUtilNr.setCfr(GnbProtocol.CellId.SECOND, 1);
        mLogicTraceUtilNr.setUeMaxTxpwr(GnbProtocol.CellId.SECOND, "10");
        mLogicTraceUtilNr.setCid(GnbProtocol.CellId.SECOND, 65537);
        mLogicTraceUtilNr.setSplit_arfcn_dl(GnbProtocol.CellId.SECOND, "0");

        mLogicTraceUtilNr2.setTacChange(GnbProtocol.CellId.FIRST, false);
        mLogicTraceUtilNr2.setTxPwr(GnbProtocol.CellId.FIRST, 0);
        mLogicTraceUtilNr2.setCfr(GnbProtocol.CellId.FIRST, 1);
        mLogicTraceUtilNr2.setUeMaxTxpwr(GnbProtocol.CellId.FIRST, "10");
        mLogicTraceUtilNr2.setCid(GnbProtocol.CellId.FIRST, 65536);
        mLogicTraceUtilNr2.setSplit_arfcn_dl(GnbProtocol.CellId.FIRST, "0");

        mLogicTraceUtilNr2.setTacChange(GnbProtocol.CellId.SECOND, false);
        mLogicTraceUtilNr2.setTxPwr(GnbProtocol.CellId.SECOND, 0);
        mLogicTraceUtilNr2.setCfr(GnbProtocol.CellId.SECOND, 1);
        mLogicTraceUtilNr2.setUeMaxTxpwr(GnbProtocol.CellId.SECOND, "10");
        mLogicTraceUtilNr2.setCid(GnbProtocol.CellId.SECOND, 65537);
        mLogicTraceUtilNr2.setSplit_arfcn_dl(GnbProtocol.CellId.SECOND, "0");

        mLogicTraceUtilLte.setTacChange(GnbProtocol.CellId.FIRST, false);
        mLogicTraceUtilLte.setTxPwr(GnbProtocol.CellId.FIRST, 0);
        mLogicTraceUtilLte.setCfr(GnbProtocol.CellId.FIRST, 1);
        mLogicTraceUtilLte.setUeMaxTxpwr(GnbProtocol.CellId.FIRST, "10");
        mLogicTraceUtilLte.setCid(GnbProtocol.CellId.FIRST, 65538);
        mLogicTraceUtilLte.setSplit_arfcn_dl(GnbProtocol.CellId.FIRST, "0");

        mLogicTraceUtilLte.setTacChange(GnbProtocol.CellId.SECOND, false);
        mLogicTraceUtilLte.setTxPwr(GnbProtocol.CellId.SECOND, 0);
        mLogicTraceUtilLte.setCfr(GnbProtocol.CellId.SECOND, 1);
        mLogicTraceUtilLte.setUeMaxTxpwr(GnbProtocol.CellId.SECOND, "10");
        mLogicTraceUtilLte.setCid(GnbProtocol.CellId.SECOND, 65539);
        mLogicTraceUtilLte.setSplit_arfcn_dl(GnbProtocol.CellId.SECOND, "0");

        mLogicTraceUtilLte2.setTacChange(GnbProtocol.CellId.FIRST, false);
        mLogicTraceUtilLte2.setTxPwr(GnbProtocol.CellId.FIRST, 0);
        mLogicTraceUtilLte2.setCfr(GnbProtocol.CellId.FIRST, 1);
        mLogicTraceUtilLte2.setUeMaxTxpwr(GnbProtocol.CellId.FIRST, "10");
        mLogicTraceUtilLte2.setCid(GnbProtocol.CellId.FIRST, 65538);
        mLogicTraceUtilLte2.setSplit_arfcn_dl(GnbProtocol.CellId.FIRST, "0");

        mLogicTraceUtilLte2.setTacChange(GnbProtocol.CellId.SECOND, false);
        mLogicTraceUtilLte2.setTxPwr(GnbProtocol.CellId.SECOND, 0);
        mLogicTraceUtilLte2.setCfr(GnbProtocol.CellId.SECOND, 1);
        mLogicTraceUtilLte2.setUeMaxTxpwr(GnbProtocol.CellId.SECOND, "10");
        mLogicTraceUtilLte2.setCid(GnbProtocol.CellId.SECOND, 65539);
        mLogicTraceUtilLte2.setSplit_arfcn_dl(GnbProtocol.CellId.SECOND, "0");

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

    public void doSetTxPwrOffset(final int value) {
        if (txValue == value || txValue == -1) return;
        txValue = value;
        AppLog.I("TraceFragment rg_rx_gain rxValue = " + value);
        for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
            TraceUtil traceUtil = bean.getTraceUtil();
            String id = bean.getRsp().getDeviceId();
            if (MessageController.build().getTraceType(id) == GnbProtocol.TraceType.TRACE) {
                String first_arfcn = traceUtil.getArfcn(GnbProtocol.CellId.FIRST);
                String second_arfcn = traceUtil.getArfcn(GnbProtocol.CellId.SECOND);

                if (!first_arfcn.isEmpty())
                    MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.FIRST, Integer.parseInt(first_arfcn), value);
                if (!second_arfcn.isEmpty())
                    MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.SECOND, Integer.parseInt(second_arfcn), value);
//                if (bean.getRsp().getDualCell() == 2) {
//                    String arfcn = traceUtil.getArfcn(GnbProtocol.CellId.SECOND);
//                    if (!arfcn.isEmpty()) {
//                        new Handler().postDelayed(() ->
//                                MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.SECOND, Integer.parseInt(arfcn), value), 300);
//                    }
//                }
            }
        }
    }

    public void doWork(String auto_trace_imsi) {
        this.auto_trace_imsi = auto_trace_imsi;
        String text = mCfgTraceChildFragment.getTv_do_btn().getText().toString();
        if (text.equals(getString(R.string.open))) {
            List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
            if (deviceList.size() <= 0) {
                Util.showToast(getString(R.string.dev_offline));
                return;
            }
            StringBuilder sb = new StringBuilder();
            String idleDev = "";
            for (DeviceInfoBean bean : deviceList) {
                switch (bean.getWorkState()) {
                    case GnbBean.State.NONE:
                        sb.append("-1");
                        break;
                    case GnbBean.State.FREQ_SCAN:   //有自动停止扫频
                    case GnbBean.State.IDLE:
                        sb.append("0");
                        idleDev = bean.getRsp().getDevName();
                        break;
                }
            }
            AppLog.I("doWork() 开启定位 sb = " + sb);
            String doStr = sb.toString();

            if (doStr.equals("-1-1-1-1")) {
                Util.showToast(getString(R.string.dev_offline));
                return;
            }
            if (doStr.isEmpty() || doStr.contains("-1")) {
                Util.showToast(getString(R.string.dev_busy_please_wait));
                return;
            }

            if (doStr.equals("0000")) {
                startTrace("", auto_trace_imsi);  // 两设备均在空闲状态下，启动定位
                return;
            }
            if (doStr.contains("0")) {
                startTrace(idleDev, auto_trace_imsi); // 单设备或多设备在空闲状态下，启动定位
            }


        } else if (text.equals(getString(R.string.open_stop))) {
            StringBuilder sb = new StringBuilder();
            String idleDev = "";
            for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                if (bean.getWorkState() == GnbBean.State.IDLE) {
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

    //1
    public void setProgress(int logicIndex, int pro, int cell_id, String info, boolean isFail) {
        MainActivity.getInstance().updateProgress(logicIndex, pro, cell_id, info, isFail);
    }


    public void showBlackListDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_blacklist_silde, null);

        RecyclerView black_list = view.findViewById(R.id.black_list);
        black_list.setLayoutManager(new LinearLayoutManager(mContext));

        adapter = new BlackListSlideAdapter(mContext, MainActivity.getInstance().getBlackList(), new BlackListSlideAdapter.ListItemListener() {
            @Override
            public void onItemClickListener(MyUeidBean bean) {
                showChangeTraceImsi(bean);
                //自动定位逻辑待开发
//                showWaitDevelop(bean);
            }
        });
        black_list.setAdapter(adapter);
        view.findViewById(R.id.tv_add).setOnClickListener(view1 -> showBlackListCfgDialog(true, -1));
        view.findViewById(R.id.back).setOnClickListener(view12 -> MainActivity.getInstance().closeCustomDialog());
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
            if (imsi.length() != GnbProtocol.MAX_IMSI_USE_LEN) {
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
                tv_title.setText(mContext.getResources().getString(R.string.delete_tip_title));

                tv_msg.setText(R.string.delete_tip);
                view2.findViewById(R.id.btn_ok).setOnClickListener(view1 -> {
                    MainActivity.getInstance().getBlackList().remove(position);
                    PrefUtil.build().putUeidList(MainActivity.getInstance().getBlackList());
                    adapter.notifyDataSetChanged();
                    MainActivity.getInstance().closeCustomDialog();
                });
                view2.findViewById(R.id.btn_cancel).setOnClickListener(view1 -> MainActivity.getInstance().closeCustomDialog());
                MainActivity.getInstance().showCustomDialog(view2, false);

            } else MainActivity.getInstance().closeCustomDialog();
        });
        back.setOnClickListener(view1 -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    boolean isChangeImsi = false;

    private void showChangeTraceImsi(MyUeidBean bean) {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView title = view.findViewById(R.id.title);
        title.setText(R.string.is_trace_this_imsi);

        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> {
            for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                DeviceInfoBean deviceInfoBean = MainActivity.getInstance().getDeviceList().get(i);
                int logicIndex = DeviceUtil.getLogicIndexByDeviceName(deviceInfoBean.getRsp().getDevName());
                int workState = deviceInfoBean.getWorkState();
                int firstState = deviceInfoBean.getTraceUtil().getWorkState(0);
                int secondState = deviceInfoBean.getTraceUtil().getWorkState(1);
                if (firstState == GnbBean.State.CATCH || firstState == GnbBean.State.TRACE
                        || secondState == GnbBean.State.CATCH || secondState == GnbBean.State.TRACE) {
                    String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
                    String imsi = bean.getUeidBean().getImsi();

                    //如果在侦码列表内的，改为NOW
                    for (int j = 0; j < mImsiList.size(); j++) {
                        if (mImsiList.get(j).getImsi().equals(imsi)) {
                            mImsiList.get(j).setState(ImsiBean.State.IMSI_NOW);
                            //cell_id = mImsiList.get(j).getCellId();
                            mCatchChildFragment.itemChanged(j);
                            break;
                        }
                    }

                    //循环侦码列表，将正在定位的NOW改为BL/OLD
                    for (int j = 0; j < mImsiList.size(); j++) {
                        if (mImsiList.get(j).getImsi().equals(imsi)) continue;  //侦码列表内，跳过
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

                    int cell_id = -1;//四个通道全部定位同一个目标
                    if (cell_id == -1) {
                        String arfcn;
                        int cellID = -1;
                        do {
                            cellID++;
                            arfcn = deviceInfoBean.getTraceUtil().getArfcn(cellID);
                            if (cellID == 3) break;
                        } while (arfcn.isEmpty());
                        if (!arfcn.isEmpty()) {
                            setChangeImsi(i, cellID, arfcn, imsi);
                        }
                    } else {
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setImsi(cell_id, bean.getUeidBean().getImsi());
                        if (Integer.parseInt(deviceInfoBean.getTraceUtil().getArfcn(cell_id)) > 100000)
                            MessageController.build().startTrace(id, cell_id, imsi, 1);
                        else MessageController.build().startLteTrace(id, cell_id, imsi, 1);
                        traceCatchVM.setIsChangeImsi(true);
                        MainActivity.getInstance().updateProgress(logicIndex, 50, cell_id, getString(R.string.changeing), false);
                    }
                } else if (workState == GnbBean.State.IDLE) {
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

    private void showWaitDevelop(MyUeidBean bean) {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView title = view.findViewById(R.id.title);
        title.setText("功能正在开发中...");

        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setVisibility(View.GONE);
        final TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setText("确定");
        btn_cancel.setOnClickListener(v -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    //更新状态，设置imsi并启动定位
    public void setChangeImsi(int index, int cell_id, String arfcn, String imsi) {
        String id = MainActivity.getInstance().getDeviceList().get(index).getRsp().getDeviceId();
        int logicIndex = DeviceUtil.getLogicIndexById(id);
        MainActivity.getInstance().updateProgress(logicIndex, 50, cell_id, getString(R.string.changeing), false);
        MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setImsi(cell_id, imsi);
        traceCatchVM.setIsChangeImsi(true);
        if (Integer.parseInt(arfcn) > 100000)
            MessageController.build().startTrace(id, cell_id, imsi, 1);
        else MessageController.build().startLteTrace(id, cell_id, imsi, 1);
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
//            mCfgTraceChildFragment.setcfgEnable(-1, false);
        } else {
            if (tv_do_btn.getText().equals(getString(R.string.open))) return;
            tv_do_btn.setText(getString(R.string.open));
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
            mCfgTraceChildFragment.setcfgEnable(-1, true);
        }
    }

    private void startTrace(String devName, String auto_trace_imsi) {
        AppLog.I("startTrace");
        if (!auto_trace_imsi.isEmpty()) mCfgTraceChildFragment.isAutoMode = true;
        //自动运行
        if (mCfgTraceChildFragment.isAutoMode) {
            List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
            if (deviceList.size() <= 0) {
                Util.showToast(getString(R.string.dev_offline));
                return;
            }
            String tracePlmn = "";
            if (auto_trace_imsi != null) {
                if (auto_trace_imsi.length() == 0) {
                    mCfgTraceChildFragment.isAutoMode = false;
                    startTrace(devName, "");
                    return;
                }
                tracePlmn = auto_trace_imsi.substring(0, 5);

            }
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
            mFreqDialog = new FreqDialog(mContext, this, mCfgTraceChildFragment, tracePlmn);
            mFreqDialog.setOnTraceSetListener(this);
            mFreqDialog.setOnCancelListener(dialogInterface -> mFreqDialog = null);
            mFreqDialog.show();
            mCfgTraceChildFragment.setAutoModeFreqRunning(true);
            return;
        }

        //更新map的值
//        MyRecyclerviewAdapter TD1_adapter, TD2_adapter, TD3_adapter, TD4_adapter;
//        MyRecyclerviewAdapter TD5_adapter, TD6_adapter, TD7_adapter, TD8_adapter;
//        TD1_adapter = mCfgTraceChildFragment.getTD1_adapter();
//        TD2_adapter = mCfgTraceChildFragment.getTD2_adapter();
//        TD3_adapter = mCfgTraceChildFragment.getTD3_adapter();
//        TD4_adapter = mCfgTraceChildFragment.getTD4_adapter();
//        TD5_adapter = mCfgTraceChildFragment.getTD5_adapter();
//        TD6_adapter = mCfgTraceChildFragment.getTD6_adapter();
//        TD7_adapter = mCfgTraceChildFragment.getTD7_adapter();
//        TD8_adapter = mCfgTraceChildFragment.getTD8_adapter();
        LinkedList<Integer> TD1, TD2, TD3, TD4;
        LinkedList<Integer> TD5, TD6, TD7, TD8;
        TD1 = mCfgTraceChildFragment.getTD1();
        TD2 = mCfgTraceChildFragment.getTD2();
        TD3 = mCfgTraceChildFragment.getTD3();
        TD4 = mCfgTraceChildFragment.getTD4();
        TD5 = mCfgTraceChildFragment.getTD5();
        TD6 = mCfgTraceChildFragment.getTD6();
        TD7 = mCfgTraceChildFragment.getTD7();
        TD8 = mCfgTraceChildFragment.getTD8();
        TD1.clear();
        TD2.clear();
        TD3.clear();
        TD4.clear();
        TD5.clear();
        TD6.clear();
        TD7.clear();
        TD8.clear();

        mCfgTraceChildFragment.setToastTextEmpty();

        //判断设备
        if (PaCtl.build().isB97502) {
            if (!mCfgTraceChildFragment.isChangeAndAdd2ToTD()) return;
        }
        if (mCfgTraceChildFragment.isPciFalseParam()) return;
        if (mCfgTraceChildFragment.isChangeArfcnUpdateTDAndMap()) {
            MainActivity.getInstance().isUseDefault = false;
        }

        traceCatchVM.setIsChangeImsi(false);
        if (mCfgTraceChildFragment.checkArfcnParamAndSetTraceUtilAndCache()) {  //检查没问题
            if (MainActivity.getInstance().getDeviceList().get(0).getWorkState() == GnbBean.State.FREQ_SCAN) {
                //停止扫频开始定位
                for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                    if (MainActivity.getInstance().getDeviceList().get(i).getWorkState() == GnbBean.State.FREQ_SCAN) {
                        String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
                        MessageController.build().setOnlyCmd(id, GnbProtocol.OAM_MSG_STOP_FREQ_SCAN);
                        int finalI = i;
                        new Handler().postDelayed(() -> {
                            if (MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDevName().contains(devA) ||
                                    MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDevName().contains(devB))
                                PaCtl.build().closePA(id);
                            else PaCtl.build().closeLtePA(id);
                        }, 300);
                    }
                }

                //刷新页面
//                mCfgTraceChildFragment.setcfgEnable(-1, false);
                refreshTraceBtn();
                Util.showToast("正在停止扫频， 请稍等", Toast.LENGTH_LONG);

                //如果在扫频改为定位
                new Handler().postDelayed(() -> {
                    for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                        if (MainActivity.getInstance().getDeviceList().get(i).getWorkState() == GnbBean.State.FREQ_SCAN) {
                            MainActivity.getInstance().getDeviceList().get(i).setWorkState(GnbBean.State.TRACE);
                        }
                    }
                    mFreqFragment.setFreqWorkState(false); // 避免命令下发不响应，这里也做一次清除状态
                    startRunWork(); //开始定位
                }, 8000);

            } else {
                //刷新页面
//                mCfgTraceChildFragment.setcfgEnable(-1, false);
                refreshTraceBtn();
                startRunWork();//直接开始定位
            }
            //存入历史记录
            storeIntoHistory();

        } else {    //不通过

        }
    }

    //待修改：载波分裂
    void stopTraceDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> {
            isClickStop = true;
            txValue = -1;
            traceCatchVM.setIsChangeImsi(false);
            autoRun1 = false;
            autoRun2 = false;
            autoRun3 = false;
            autoRun4 = false;
            autoRun5 = false;
            autoRun6 = false;
            autoRun7 = false;
            autoRun8 = false;
            mCfgTraceChildFragment.isAutoMode = false;
            mCfgTraceChildFragment.isAutoModeFreqRunning = false;

            for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                mCfgTraceChildFragment.getTv_do_btn().setText(getString(R.string.stoping));
                final String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
                int logicIndex = DeviceUtil.getLogicIndexById(id);

                if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().isEnable(GnbProtocol.CellId.FIRST) &&
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST) == GnbBean.State.TRACE) {
                    String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(GnbProtocol.CellId.FIRST);
                    if (!arfcn.isEmpty()) {
                        int finalI = i;
                        new Handler().postDelayed(() -> {
                            setProgress(logicIndex, 50, 0, getString(R.string.stoping), false);

                            MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FIRST, System.currentTimeMillis());
                            MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP);
                            MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FIRST);
                            refreshTraceBtn();
                        }, 900);
                    }
                } else {
                    String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(GnbProtocol.CellId.FIRST);
                    if (MainActivity.getInstance().getDeviceList().get(i).getRsp().getFirstState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {   //基站状态
                        setProgress(logicIndex, 50, 0, getString(R.string.stoping), false);

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FIRST, System.currentTimeMillis());
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP);
                        new Handler().postDelayed(() -> MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FIRST), 900);

                    } else if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST) == GnbBean.State.GNB_CFG) {
                        setProgress(logicIndex, 50, 0, "结束中", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP);
                    } else {
                        setProgress(logicIndex, 0, 0, "空闲", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.IDLE);
                    }
                }
                if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().isEnable(GnbProtocol.CellId.SECOND) &&
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.TRACE) {
                    String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND);
                    if (!arfcn.isEmpty()) {
                        int finalI = i;
                        new Handler().postDelayed(() -> {
                            setProgress(logicIndex, 50, 1, getString(R.string.stoping), false);

                            MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.SECOND, System.currentTimeMillis());
                            MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP);

                            MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.SECOND);
                            refreshTraceBtn();
                        }, 600);
                    }
                } else {
                    String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND);
                    if (MainActivity.getInstance().getDeviceList().get(i).getRsp().getSecondState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        setProgress(logicIndex, 50, 1, getString(R.string.stoping), false);

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.SECOND, System.currentTimeMillis());
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP);
                        new Handler().postDelayed(() -> MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.SECOND), 600);

                    } else if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.GNB_CFG) {
                        setProgress(logicIndex, 50, 1, "结束中", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP);
                    } else {
                        setProgress(logicIndex, 0, 1, "空闲", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.IDLE);
                    }
                }

                refreshTraceBtn();
                mCfgTraceChildFragment.setcfgEnable(-1, true);
                String arfcn1 = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(GnbProtocol.CellId.FIRST);
                String arfcn2 = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND);
                if (!arfcn1.isEmpty() && Integer.parseInt(arfcn1) > 100000 ||
                        !arfcn2.isEmpty() && Integer.parseInt(arfcn2) > 100000)
                    PaCtl.build().closePA(id);
                else PaCtl.build().closeLtePA(id);
            }
            MainActivity.getInstance().closeCustomDialog();
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    public void onSetBlackListRsp(String id, GnbCmdRsp rsp) {
        int logicIndex = MainActivity.getInstance().getLogicIndexById(id);
        if (logicIndex == -1) return;
        int index = DeviceUtil.getIndexById(id);
        final TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_SET_BLACK_UE_LIST || rsp.getCmdType() == GnbProtocol.UI_2_gNB_START_LTE_BLACK_UE_LIST) {
                if (isClickStop) {
                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    setProgress(logicIndex, 0, rsp.getCellId(), "空闲", false);
                    refreshTraceBtn();
                    mCfgTraceChildFragment.isAutoMode = false;
                    return;
                }
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    // 发配置定位参数指令
                    int traceTac = PrefUtil.build().getTac();
                    int maxTac = traceTac + GnbProtocol.MAX_TAC_NUM;
                    // 4G tac 不能大于65535， 在这4G 5G共用相同tac，因此做相同处理
                    if (maxTac == 65535) {  //不需要改变traceTac
                        maxTac = traceTac + GnbProtocol.MAX_TAC_NUM;
                        PrefUtil.build().setTac(maxTac + 1); // 从头再来
                    } else if (maxTac > 65535) {
                        traceTac = 1234;
                        maxTac = traceTac + GnbProtocol.MAX_TAC_NUM;
                        PrefUtil.build().setTac(maxTac + 1); // 从头再来
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
                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.GNB_CFG);

                    setProgress(logicIndex, 30, rsp.getCellId(), getString(R.string.cfging), false);
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
                        int time_offset = GnbCity.build().getTimimgOffset(arfcn[0]);
//                        int time_offset = Integer.parseInt(PrefUtil.build().getValue("sync_time_offset", "0").toString());
                        if (iArfcn > 100000) {
                            AppLog.D("onSetBlackListRsp: traceTac=" + finalTraceTac);
                            MessageController.build().initGnbTrace(id, rsp.getCellId(), finalTraceTac, finalMaxTac, plmn, arfcn[0], pci[0], ue_max_pwr,
                                    time_offset, 0, air_sync, "0", 9,
                                    cid, ssbBitmap, bandwidth, cfr, swap_rf, 15, -70, mob_reject_code, split_arfcn_dl);
                        } else {
//                            int band = LteBand.earfcn2band(Integer.parseInt(arfcn[0]));
//                            if (MainActivity.getInstance().getDeviceList().get(0).getRsp().getGpsSyncState() == GnbStateRsp.Gps.SUCC && band >= 33 && band <= 46) {
//                                time_offset = 10000000;
//                            }

                            AppLog.D("onSetBlackListRsp: traceTac=" + finalTraceTac);
                            MessageController.build().initGnbLteTrace(id, rsp.getCellId(), finalTraceTac, finalMaxTac, plmn, arfcn[0], pci[0], ue_max_pwr,
                                    time_offset, 0, air_sync, "0", 9,
                                    cid, ssbBitmap, bandwidth, cfr, swap_rf, 15, -70, mob_reject_code, split_arfcn_dl);
                        }
//                            getArfcnList(rsp.getCellId(), arfcn[0]);
                        new Handler().postDelayed(() -> {
                            MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setSaveOpLog(rsp.getCellId(), false);
                            String msg = "plmn = " + plmn + ", arfcn = " + arfcn[0] + ", pci = " + pci[0] + ", cid = " + cid
                                    + ", time offset = " + time_offset;
                            OpLog.build().write(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + rsp.getCellId() + "\t\t定位参数：" + msg);
                        }, 300);
                    }, 300);
                } else {
                    String s = DeviceUtil.getCellStr(logicIndex, rsp.getCellId());
                    setProgress(logicIndex, 30, rsp.getCellId(), getString(R.string.open_fail), true);
                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    MainActivity.getInstance().getDeviceList().get(index).setWorkState(GnbBean.State.IDLE);
                    AppLog.I("onSetBlackListRsp: " + getString(R.string.cell) + s + getString(R.string.trace_start_fail));
                    Util.showToast(getString(R.string.cell) + s + getString(R.string.trace_start_fail));
//                    mCfgTraceChildFragment.setcfgEnable(-1, true);
                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    mCfgTraceChildFragment.isAutoMode = false;
                    refreshTraceBtn();
                }
            }
        }
    }

    public void onSetGnbRsp(String id, GnbCmdRsp rsp) {
        int logicIndex = MainActivity.getInstance().getLogicIndexById(id);
        if (logicIndex == -1) return;
        int index = DeviceUtil.getIndexById(id);
        final TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_CFG_gNB || rsp.getCmdType() == GnbProtocol.UI_2_eNB_CFG_gNB) { //UI_2_gNB_CFG_gNB = 10 配置频点参数
                if (isClickStop) {
                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    setProgress(logicIndex, 0, rsp.getCellId(), "空闲", false);
                    refreshTraceBtn();
                    mCfgTraceChildFragment.isAutoMode = false;
                    return;
                }
                int arfcnCount = -1;    //此通道的频点数量
                int TDNum = logicIndex * 2 + rsp.getCellId() + 1;
                if (arfcnBeanHashMap.get("TD" + TDNum) != null) {
                    arfcnCount = arfcnBeanHashMap.get("TD" + TDNum).getArfcnList().size();
                }
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    if (/*!traceUtil.isTacChange(rsp.getCellId())
                            && */traceUtil.getWorkState(rsp.getCellId()) == GnbBean.State.GNB_CFG) {
                        setProgress(logicIndex, 50, rsp.getCellId(), "配置中", false);
                        //第三步.设置功率衰减
                        //MessageController.build().setTraceType(id, GnbProtocol.TraceType.STARTTRACE);
                        MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.CFG_TRACE);
                        MessageController.build().setTxPwrOffset(id, rsp.getCellId(), Integer.parseInt(traceUtil.getArfcn(rsp.getCellId())), 0);
                    }
                } else {
                    if (rsp.getRspValue() == 10) {
                        setProgress(logicIndex, 50, rsp.getCellId(), "授权过期", true);
                        MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                        refreshTraceBtn();
                        mCfgTraceChildFragment.isAutoMode = false;

                        MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.STOP);
                        traceUtil.setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                        MessageController.build().setCmdAndCellID(id, Integer.parseInt(MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getArfcn(rsp.getCellId())) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, rsp.getCellId());

                        MainActivity.getInstance().showRemindDialog("过期提示", "设备使用时间已到期，请联系管理人员");
                        return;
                    }
                    int autoArfcnIndex = 0; //记录当前通道的频点在通道所处的位置
                    if (TDNum == 1) autoArfcnIndex = autoArfcnIndex0;
                    else if (TDNum == 2) autoArfcnIndex = autoArfcnIndex1;
                    else if (TDNum == 3) autoArfcnIndex = autoArfcnIndex2;
                    else if (TDNum == 4) autoArfcnIndex = autoArfcnIndex3;
                    else if (TDNum == 5) autoArfcnIndex = autoArfcnIndex4;
                    else if (TDNum == 6) autoArfcnIndex = autoArfcnIndex5;
                    else if (TDNum == 7) autoArfcnIndex = autoArfcnIndex6;
                    else if (TDNum == 8) autoArfcnIndex = autoArfcnIndex7;
                    AppLog.D("TD" + TDNum + " arfcnCount = " + arfcnCount + " autoArfcnIndex=" + autoArfcnIndex + " autoArfcnIndex0=" + autoArfcnIndex0 + " autoArfcnIndex1=" + autoArfcnIndex1 + " autoArfcnIndex2=" + autoArfcnIndex2 + " autoArfcnIndex3=" + autoArfcnIndex3
                            + " autoArfcnIndex4=" + autoArfcnIndex4 + " autoArfcnIndex5=" + autoArfcnIndex5 + " autoArfcnIndex6=" + autoArfcnIndex6 + " autoArfcnIndex7=" + autoArfcnIndex7);
                    //rspValue = 6或者是列表中的最后一个
                    if (rsp.getRspValue() == GnbProtocol.OAM_ACK_E_ASYNC_FAIL || autoArfcnIndex == arfcnCount && arfcnCount != 0) {
//                        ArfcnBean arfcnBean;
//                        int arfcn = -1;
                        // 索引先不加加，让第一个多跑一次
//                        if (TDNum == 3) { // 通道三循环逻辑
//                            //String arfcn_cfg = PrefUtil.build().getValue("arfcn_cfg3", "504990").toString();
//                            arfcnBean = arfcnBeanHashMap.get("TD3");
//                            if (arfcnBean != null && autoArfcnIndex2 < arfcnBean.getArfcnList().size()) {
//                                arfcn = arfcnBean.getArfcnFromList(autoArfcnIndex2);
//                            }
//                            autoArfcnIndex2++;
//                            // 判断通道一是否为FDD，如果是FDD，通道三就不再下FDD
//                            if (logicIndex == 0 && MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getWorkState(0) == GnbBean.State.TRACE) {
//                                boolean firstIsFdd = NrBand.earfcn2band(Integer.parseInt(MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getArfcn(0))) == 28;
//                                if (firstIsFdd) {
//                                    boolean secondIsFdd = true;
//                                    while (secondIsFdd) {
//                                        if (NrBand.earfcn2band(arfcn) == 1) {
//                                            if (arfcnBean != null && autoArfcnIndex2 < arfcnBean.getArfcnList().size()) {
//                                                arfcn = arfcnBean.getArfcnFromList(autoArfcnIndex2);
//                                                autoArfcnIndex2++;
//                                                if (Integer.parseInt(MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getArfcn(rsp.getCellId())) == arfcn) {
//                                                    if (autoArfcnIndex2 < arfcnBean.getArfcnList().size()) {
//                                                        arfcn = arfcnBean.getArfcnFromList(autoArfcnIndex2);
//                                                        autoArfcnIndex2++;
//                                                    } else {
//                                                        secondIsFdd = false;
//                                                        arfcn = -1;
//                                                    }
//                                                }
//                                            } else {
//                                                secondIsFdd = false;
//                                                arfcn = -1;
//                                            }
//                                        } else {
//                                            secondIsFdd = false;
//                                        }
//                                    }
//                                }
//                            }
//                            AppLog.D("cell 3 sync fail reset arfcn = " + arfcn);
//                            if (arfcnBean != null && arfcn == -1) {
//                                autoArfcnIndex2 = 0;
//                                arfcn = arfcnBean.getArfcnFromList(autoArfcnIndex2);
//                            }
//                            if (arfcnBean != null && arfcnBean.getArfcnList().size() == 1) {
//                                //只有一个频点不轮询
//                                setProgress(logicIndex, 0, 2, "空闲", false);
//                                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(2, GnbBean.State.IDLE);
//                                refreshTraceBtn();
//                                Util.showToast("通道三空口同步失败");
//                                return;
//                            }
//                            updateParamAndStart(id, arfcn, rsp.getCellId());
//                            return;
//                        } else {
                        //轮询逻辑
                        pollingArfcn(id, rsp, index, TDNum, autoArfcnIndex);
                        return;
//                        }
                    }

                    String s = DeviceUtil.getCellStr(logicIndex, rsp.getCellId());
                    if (rsp.getRspValue() == 2 &&
                            MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getWorkState(rsp.getCellId()) == GnbBean.State.TRACE) {
                        mCfgTraceChildFragment.isAutoMode = false;
                        return;
                    }

                    setProgress(logicIndex, 50, rsp.getCellId(), "启动失败", true);
                    AppLog.I("onSetGnbRsp: " + getString(R.string.cell) + s + getString(R.string.trace_start_fail));
                    Util.showToast("通道" + s + Util.getString(R.string.trace_start_fail));
                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    mCfgTraceChildFragment.isAutoMode = false;

//                    mCfgTraceChildFragment.setcfgEnable(-1, true);
                    refreshTraceBtn();
                    if (rsp.getRspValue() == 5) {
                        new Handler().postDelayed(() -> {
                            MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.STOP);
                            traceUtil.setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                            MessageController.build().setCmdAndCellID(id, Integer.parseInt(MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getArfcn(rsp.getCellId())) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, rsp.getCellId());
                        }, 500);
                    }
                }
            }
        }
    }

    //频点轮询逻辑
    private void pollingArfcn(String id, GnbCmdRsp rsp, int index, int TDNum, int autoArfcnIndex) {
        int logicIndex = DeviceUtil.getLogicIndexByChannelNum(TDNum);
        ArfcnBean arfcnBean = arfcnBeanHashMap.get("TD" + TDNum);
        int arfcn = -1;
        if (arfcnBean.getArfcnList().size() == 0) {   //没有频点
            Util.showToast("通道" + DeviceUtil.getCellStr(logicIndex, rsp.getCellId()) + "未配置频点");
            refreshTraceBtn();
            setProgress(logicIndex, 0, DeviceUtil.getCellIdByChannelNum(TDNum), "空闲", false);
            MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(0, GnbBean.State.IDLE);
//            mCfgTraceChildFragment.setcfgEnable(-1, true);
            return;
        }
        if (arfcnBean != null && autoArfcnIndex < arfcnBean.getArfcnList().size()) {
            arfcn = arfcnBean.getArfcnFromList(autoArfcnIndex);
        }
        autoArfcnIndex++;
        AppLog.I("cell " + TDNum + " sync fail reset arfcn = " + arfcn + " autoArfcnIndex = " + autoArfcnIndex);
        //多轮询一次
        if (arfcnBean != null && arfcn == -1 && arfcnBean.getArfcnList().size() > 0) {
            autoArfcnIndex = 0;
            arfcn = arfcnBean.getArfcnFromList(autoArfcnIndex);
        }
        //只有一个频点不轮询
        if (arfcnBean != null && arfcnBean.getArfcnList().size() == 1) {
            setProgress(logicIndex, 0, DeviceUtil.getCellIdByChannelNum(TDNum), "启动失败", false);
            MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(DeviceUtil.getCellIdByChannelNum(TDNum), GnbBean.State.IDLE);
            refreshTraceBtn();
            Util.showToast("通道" + DeviceUtil.getCellStr(logicIndex, rsp.getCellId()) + "空口同步失败");
//            mCfgTraceChildFragment.setcfgEnable(-1, true);
            return;
        }
        updateParamAndStart(id, arfcn, rsp.getCellId());
        setAutoArfcnIndex(TDNum, autoArfcnIndex);
    }

    //设置autoArfcnIndex值
    private void setAutoArfcnIndex(int tdNum, int autoArfcnIndex) {
        switch (tdNum) {
            case 1:
                autoArfcnIndex0 = autoArfcnIndex;
                break;
            case 2:
                autoArfcnIndex1 = autoArfcnIndex;
                break;
            case 3:
                autoArfcnIndex2 = autoArfcnIndex;
                break;
            case 4:
                autoArfcnIndex3 = autoArfcnIndex;
                break;
            case 5:
                autoArfcnIndex4 = autoArfcnIndex;
                break;
            case 6:
                autoArfcnIndex5 = autoArfcnIndex;
                break;
            case 7:
                autoArfcnIndex6 = autoArfcnIndex;
                break;
            case 8:
                autoArfcnIndex7 = autoArfcnIndex;
                break;
        }
    }

    //待修改：载波分裂
    //更新配置并调用启动黑名单开始
    private void updateParamAndStart(String id, int arfcn, int cell_id) {
        AppLog.I("updateParamAndStart id = " + id + "arfcn= " + arfcn + " cell_id = " + cell_id);
        int logicIndex = DeviceUtil.getLogicIndexById(id);
        int index = DeviceUtil.getIndexById(id);
        int channelNum = logicIndex * 2 + cell_id + 1;
        if (PaCtl.build().isB97502) {
            //bw:5g双通道TDD 100， FDD 20，4g 5
            //ssb:5g TDD 255， FDD 240, 4g 128，
            switch (channelNum) {
                case 1: //N41
                    mLogicTraceUtilNr.setBandWidth(GnbProtocol.CellId.FIRST, 100);
                    mLogicTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.FIRST, 255);
                    mLogicTraceUtilNr.setPlmn(GnbProtocol.CellId.FIRST, "46000");
                    mLogicTraceUtilNr.setArfcn(GnbProtocol.CellId.FIRST, String.valueOf(arfcn));
                    MainActivity.getInstance().getDeviceList().get(index).setTraceUtil(mLogicTraceUtilNr);
                    break;
                case 2:
                    mLogicTraceUtilNr.setBandWidth(GnbProtocol.CellId.SECOND, 20);
                    mLogicTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.SECOND, 240);
                    mLogicTraceUtilNr.setPlmn(GnbProtocol.CellId.SECOND, "46001");
                    mLogicTraceUtilNr.setArfcn(GnbProtocol.CellId.SECOND, String.valueOf(arfcn));
                    MainActivity.getInstance().getDeviceList().get(index).setTraceUtil(mLogicTraceUtilNr);
                    break;
                case 3:
                    int band2 = arfcn > 100000 ? NrBand.earfcn2band(arfcn) : LteBand.earfcn2band(arfcn);
                    mLogicTraceUtilNr2.setBandWidth(GnbProtocol.CellId.FIRST, 100);
                    mLogicTraceUtilNr2.setSsbBitmap(GnbProtocol.CellId.FIRST, 255);
                    mLogicTraceUtilNr2.setPlmn(GnbProtocol.CellId.FIRST, band2 == 78 ? "46001" : "46000");
                    mLogicTraceUtilNr2.setArfcn(GnbProtocol.CellId.FIRST, String.valueOf(arfcn));
                    MainActivity.getInstance().getDeviceList().get(index).setTraceUtil(mLogicTraceUtilNr2);
                    break;
                case 4:
                    mLogicTraceUtilNr2.setBandWidth(GnbProtocol.CellId.SECOND, 20);
                    mLogicTraceUtilNr2.setSsbBitmap(GnbProtocol.CellId.SECOND, 240);
                    mLogicTraceUtilNr2.setPlmn(GnbProtocol.CellId.SECOND, "46000");
                    mLogicTraceUtilNr2.setArfcn(GnbProtocol.CellId.SECOND, String.valueOf(arfcn));
                    MainActivity.getInstance().getDeviceList().get(index).setTraceUtil(mLogicTraceUtilNr2);
                    break;
                case 5: //B34/B3
                    mLogicTraceUtilLte.setBandWidth(GnbProtocol.CellId.FIRST, 5);
                    mLogicTraceUtilLte.setSsbBitmap(GnbProtocol.CellId.FIRST, 128);
                    mLogicTraceUtilLte.setPlmn(GnbProtocol.CellId.FIRST, "46000");
                    mLogicTraceUtilLte.setArfcn(GnbProtocol.CellId.FIRST, String.valueOf(arfcn));
                    MainActivity.getInstance().getDeviceList().get(index).setTraceUtil(mLogicTraceUtilLte);
                    break;
                case 6: //B39/B5
                    int band = arfcn > 100000 ? NrBand.earfcn2band(arfcn) : LteBand.earfcn2band(arfcn);
                    mLogicTraceUtilLte.setBandWidth(GnbProtocol.CellId.SECOND, 5);
                    mLogicTraceUtilLte.setSsbBitmap(GnbProtocol.CellId.SECOND, 128);
                    mLogicTraceUtilLte.setPlmn(GnbProtocol.CellId.SECOND, band == 5 ? "46011" : "46000");
                    mLogicTraceUtilLte.setArfcn(GnbProtocol.CellId.SECOND, String.valueOf(arfcn));
                    MainActivity.getInstance().getDeviceList().get(index).setTraceUtil(mLogicTraceUtilLte);
                    break;
                case 7: //B40/B8
                    mLogicTraceUtilLte2.setBandWidth(GnbProtocol.CellId.FIRST, 5);
                    mLogicTraceUtilLte2.setSsbBitmap(GnbProtocol.CellId.FIRST, 128);
                    mLogicTraceUtilLte2.setPlmn(GnbProtocol.CellId.FIRST, "46000");
                    mLogicTraceUtilLte2.setArfcn(GnbProtocol.CellId.FIRST, String.valueOf(arfcn));
                    MainActivity.getInstance().getDeviceList().get(index).setTraceUtil(mLogicTraceUtilLte2);
                    break;
                case 8:
                    int band3 = arfcn > 100000 ? NrBand.earfcn2band(arfcn) : LteBand.earfcn2band(arfcn);
                    mLogicTraceUtilLte2.setBandWidth(GnbProtocol.CellId.SECOND, 5);
                    mLogicTraceUtilLte2.setSsbBitmap(GnbProtocol.CellId.SECOND, 128);
                    mLogicTraceUtilLte2.setPlmn(GnbProtocol.CellId.SECOND, band3 == 1 ? "46001" : "46000");
                    mLogicTraceUtilLte2.setArfcn(GnbProtocol.CellId.SECOND, String.valueOf(arfcn));
                    MainActivity.getInstance().getDeviceList().get(index).setTraceUtil(mLogicTraceUtilLte2);
                    break;
            }
        } else {
            switch (channelNum) {
                case 0:
                    int band = NrBand.earfcn2band(arfcn);
                    mLogicTraceUtilNr.setBandWidth(GnbProtocol.CellId.FIRST, band == 28 ? 20 : 100);
                    mLogicTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.FIRST, band == 28 ? 240 : 255);
                    mLogicTraceUtilNr.setPlmn(GnbProtocol.CellId.FIRST, band == 78 ? "46011" : "46015");
                    mLogicTraceUtilNr.setArfcn(GnbProtocol.CellId.FIRST, String.valueOf(arfcn));
                    break;
                case 1:
                    mLogicTraceUtilNr.setBandWidth(GnbProtocol.CellId.SECOND, 5);
                    mLogicTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.SECOND, 128);
                    mLogicTraceUtilNr.setPlmn(GnbProtocol.CellId.SECOND, "46000");
                    mLogicTraceUtilNr.setArfcn(GnbProtocol.CellId.SECOND, String.valueOf(arfcn));
                    break;
                case 2:
                    int band1 = NrBand.earfcn2band(arfcn);
                    mLogicTraceUtilNr.setBandWidth(GnbProtocol.CellId.THIRD, band1 == 1 ? 20 : 100);
                    mLogicTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.THIRD, band1 == 1 ? 240 : 255);
                    mLogicTraceUtilNr.setPlmn(GnbProtocol.CellId.THIRD, band1 == 1 ? "46001" : "46000");
                    mLogicTraceUtilNr.setArfcn(GnbProtocol.CellId.THIRD, String.valueOf(arfcn));
                    break;
                case 3:
                    mLogicTraceUtilNr.setBandWidth(GnbProtocol.CellId.FOURTH, 5);
                    mLogicTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.FOURTH, 128);
                    int freq = LteBand.earfcn2freq(arfcn);
                    if ((freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949))
                        mLogicTraceUtilNr.setPlmn(GnbProtocol.CellId.FOURTH, "46000");
                    else mLogicTraceUtilNr.setPlmn(GnbProtocol.CellId.FOURTH, "46001");
                    mLogicTraceUtilNr.setArfcn(GnbProtocol.CellId.FOURTH, String.valueOf(arfcn));
                    break;
            }
        }

        //发送配置黑名单指令
        sendBlackList(id, arfcn, cell_id);
    }

    private void sendBlackList(String id, int arfcn, int cellId) {
        final List<UeidBean> blackList = new ArrayList<>();
        for (MyUeidBean bean : MainActivity.getInstance().getBlackList())
            blackList.add(bean.getUeidBean());
        MessageController.build().setBlackList(id, arfcn < 100000, cellId, blackList.size(), blackList);
    }

    public void onSetTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetTxPwrOffsetRsp id = " + id + "rsp=" + rsp.toString());
        int logicIndex = MainActivity.getInstance().getLogicIndexById(id);
        if (logicIndex == -1) return;
        int index = DeviceUtil.getIndexById(id);
        final TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
        if (rsp != null) {
            //int traceType = MessageController.build().getTraceType(id);
            int traceType = traceUtil.getWorkState(rsp.getCellId());
            AppLog.I("onSetTxPwrOffsetRsp get TraceType = " + traceType);
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_SET_TX_POWER_OFFSET) {
                if (isClickStop) {
                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    setProgress(logicIndex, 0, rsp.getCellId(), "空闲", false);
                    refreshTraceBtn();
                    mCfgTraceChildFragment.isAutoMode = false;
                    return;
                }
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    if (traceType == GnbBean.State.CFG_TRACE) {
                        //4、开pa
                        if (Integer.parseInt(traceUtil.getArfcn(rsp.getCellId())) > 100000)
                            PaCtl.build().openPA(id, traceUtil.getArfcn(rsp.getCellId())); // 开PA
                        else
                            PaCtl.build().openLtePA(id, traceUtil.getArfcn(rsp.getCellId()), rsp.getCellId()); // 开Lte PA
                        new Handler().postDelayed(() -> {
                            // 5、发开始定位指令
                            MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setRsrp(rsp.getCellId(), 0);
                            MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setTacChange(rsp.getCellId(), true);
                            MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setEnable(rsp.getCellId(), true);
                            MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                            //第四步.启动定位
                            setProgress(logicIndex, 70, rsp.getCellId(), getString(R.string.cfging), false);

                            MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.CFG_TRACE);
                            if (Integer.parseInt(traceUtil.getArfcn(rsp.getCellId())) > 100000) {
                                MessageController.build().startTrace(id, rsp.getCellId(), traceUtil.getImsi(rsp.getCellId()), 1);
                            } else {
                                MessageController.build().startLteTrace(id, rsp.getCellId(), traceUtil.getImsi(rsp.getCellId()), 1);
                            }
                        }, 300);
                    }
                } else {
                    String s = DeviceUtil.getCellStr(logicIndex, rsp.getCellId());
                    if (traceType == GnbBean.State.CFG_TRACE) {
                        setProgress(logicIndex, 70, rsp.getCellId(), getString(R.string.open_fail), true);
                        AppLog.I("onSetTxPwrOffsetRsp: " + getString(R.string.cell) + s + getString(R.string.trace_start_fail));
                        Util.showToast(getString(R.string.cell) + s + getString(R.string.trace_start_fail));
                        refreshTraceBtn();
                    } else {
                        Util.showToast(getString(R.string.gain_fail));
                        mCfgTraceChildFragment.setIsCloaseDistance(false);
                    }
                    mCfgTraceChildFragment.isAutoMode = false;
//                    mCfgTraceChildFragment.setcfgEnable(-1, true);
                    refreshTraceBtn();
                }
            }
        }
    }

    boolean isStartCatchHandler1 = false;    //是否上号
    boolean isStartCatchHandler2 = false;    //是否上号
    boolean isStartCatchHandler3 = false;    //是否上号
    boolean isStartCatchHandler4 = false;    //是否上号
    boolean isStartCatchHandler5 = false;    //是否上号
    boolean isStartCatchHandler6 = false;    //是否上号
    boolean isStartCatchHandler7 = false;    //是否上号
    boolean isStartCatchHandler8 = false;    //是否上号

    public void onStartTraceRsp(String id, GnbTraceRsp rsp) {
        AppLog.I("onStartTraceRsp id=" + id + " rsp=" + rsp);
        int logicIndex = MainActivity.getInstance().getLogicIndexById(id);
        if (logicIndex == -1) return;
        int index = DeviceUtil.getIndexById(id);
        MainActivity.getInstance().getDeviceList().get(index).setWorkState(GnbBean.State.TRACE);
        final TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
        if (rsp.getCmdRsp() != null) {
            final int cell_id = rsp.getCmdRsp().getCellId();
            int channelNum = logicIndex * 2 + (cell_id + 1);
            int what = 10 + logicIndex * 2 + (cell_id + 1);
            if (cell_id == GnbProtocol.CellId.FIRST) {
                mHandler.removeMessages(what);
                mHandler.sendEmptyMessageDelayed(what, time_count * 1000L);
            } else if (cell_id == GnbProtocol.CellId.SECOND) {
                mHandler.removeMessages(what);
                mHandler.sendEmptyMessageDelayed(what, time_count * 1000L);
            }

            if (rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_gNB_START_TRACE || rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_gNB_START_LTE_TRACE) {
                String s = DeviceUtil.getCellStr(logicIndex, cell_id);
                if (rsp.getCmdRsp().getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    if (isClickStop) {
                        MessageController.build().setCmdAndCellID(id, rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_gNB_START_TRACE ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, cell_id);
                        mCfgTraceChildFragment.isAutoMode = false;
                        return;
                    }
                    //第五步.定位中，这里做判断，是设置状态为侦码中还是定位中
                    String imsi = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getImsi(cell_id);

                    // 记住业务IMSI、频点、PCI, 为恢复做准备
//                    PrefUtil.build().putValue("last_work_cfg", imsi + "/" + traceUtil.getArfcn(cell_id) + "/" + traceUtil.getPci(cell_id));

//                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setEnable(cell_id, true);
                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(cell_id, GnbBean.State.TRACE);

                    // 刷新处于工作状态
                    freshDoWorkState(logicIndex, cell_id, imsi, traceUtil.getArfcn(cell_id), traceUtil.getPci(cell_id));

                    new Handler().postDelayed(() ->
                            OpLog.build().write(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t通道" + channelNum
                                    + "\t\t开始定位，目标IMSI: " + traceUtil.getImsi(cell_id)), 500);

                    //定位中切换定位
                    Boolean isChangeImsi = traceCatchVM.getIsChangeImsi().getValue();
                    if (isChangeImsi != null && isChangeImsi) {
                        traceCatchVM.setIsChangeImsi(false);
                        Util.showToast(getString(R.string.change_trace_success));
                        if (cell_id < 3) {
                            String arfcn;
                            int cellID = cell_id;
                            do {
                                cellID++;
                                arfcn = traceUtil.getArfcn(cellID);
                                if (cellID == 3) break;
                            } while (arfcn.isEmpty());
                            if (!arfcn.isEmpty()) {
                                setChangeImsi(index, cellID, arfcn, imsi);
                            }
                        }
                    }
                    mCfgTraceChildFragment.isAutoMode = false;
                } else {
                    setProgress(logicIndex, 90, cell_id, getString(R.string.open_fail), true);
                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(cell_id, GnbBean.State.IDLE);
//                    if (MainActivity.getInstance().getDeviceList().get(logicIndex).getTraceUtil().getImsi(cell_id).endsWith("0000000000")) {
                    mCfgTraceChildFragment.isAutoMode = false;
//                    mCfgTraceChildFragment.setcfgEnable(-1, true);
                    AppLog.D("onStartTraceRsp: " + getString(R.string.cell) + s + getString(R.string.trace_start_fail));
//                    Util.showToast(getString(R.string.cell) + s + getString(R.string.trace_start_fail));
                    Util.showToast(getString(R.string.trace_start_fail));
//                    } else {
//                        MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.change_trace_fail));
//                        MainActivity.getInstance().showToast(getString(R.string.cell) + s + getString(R.string.change_trace_fail));
//                    }
                }
                refreshTraceBtn();
            }
        } else { // IMSI上报及上号报值
            if (rsp.getCellId() != -1) {

                int cell_id = rsp.getCellId();
                String traceArfcn = traceUtil.getArfcn(cell_id);

                String traceImsi = traceUtil.getImsi(cell_id);
                String tracePci = traceUtil.getPci(cell_id);
                if (traceArfcn.isEmpty()) return;
                if (rsp.getRsrp() < 5 || traceImsi.isEmpty() || traceArfcn.equals("0") || tracePci.isEmpty()) {
                    mCfgTraceChildFragment.isAutoMode = false;
                    return;
                }

                // 刷新处于工作状态
//                freshDoWorkState(type, cell_id, MainActivity.getInstance().getDeviceList().get(logicIndex).getTraceUtil().getImsi(cell_id)
//                        , traceUtil.getArfcn(cell_id), traceUtil.getPci(cell_id));

                //侦码列表
                List<String> imsiList = rsp.getImsiList();
                if (imsiList != null && imsiList.size() > 0) {
                    for (int i = 0; i < imsiList.size(); i++) {
                        String imsi = imsiList.get(i);
                        // 黑名单中的不显示
//                        if (MainActivity.getInstance().isInBlackList(imsi)) return;
                        int rsrp = rsp.getRsrp();
                        AppLog.I("traceImsi = " + traceImsi + ", imsi = " + imsi + "  rsrp = " + rsrp);

                        if (rsrp < 5) rsrp = -1;

                        if (traceImsi.equals(imsi)) {
                            MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setRsrp(cell_id, rsrp);
                        }

                        boolean add = true;
                        //已存在，更新信息
                        for (int j = 0; j < mImsiList.size(); j++) {
                            if (mImsiList.get(j).getImsi().equals(imsi)/* && mImsiList.get(j).getArfcn().equals(traceArfcn)*/) {
                                add = false;
//                                mImsiList.get(j).setArfcn(traceArfcn);
                                mImsiList.get(j).setRsrp(rsrp);
                                mImsiList.get(j).setUpCount(mImsiList.get(j).getUpCount() + 1);
                                mImsiList.get(j).setLatestTime(System.currentTimeMillis());
                                if (traceImsi.equals(imsi))
                                    mImsiList.get(j).setState(ImsiBean.State.IMSI_NOW);
                                else if (MainActivity.getInstance().isInBlackList(imsi))
                                    mImsiList.get(j).setState(ImsiBean.State.IMSI_BL);
                                break;
                            }
                        }
                        all_count++;
                        if (add) {
                            if (traceImsi.equals(imsi)) { // 当前定位IMSI
                                mImsiList.add(0, new ImsiBean(ImsiBean.State.IMSI_NOW, imsi, traceArfcn, tracePci, rsrp, System.currentTimeMillis(), cell_id));
                            } else if (MainActivity.getInstance().isInBlackList(imsi)) {
                                mImsiList.add(new ImsiBean(ImsiBean.State.IMSI_BL, imsi, traceArfcn, tracePci, rsrp, System.currentTimeMillis(), cell_id));
                            } else {
                                mImsiList.add(new ImsiBean(ImsiBean.State.IMSI_NEW, imsi, traceArfcn, tracePci, rsrp, System.currentTimeMillis(), cell_id));
                            }
                            String tracePlmn = imsi.substring(0, 5);

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
                        mCatchChildFragment.setCatchCount(all_count, telecom_count, mobile_count, unicom_count, sva_count);
                    }
                }
                if (!isStartCatchHandler1) {
                    isStartCatchHandler1 = true;
                    mCatchChildFragment.resetShowData(false); // 刷新视图
                }
                if (!isStartCatchHandler2) {
                    isStartCatchHandler2 = true;
                    mCatchChildFragment.resetShowData(false); // 刷新视图
                }
                if (!isStartCatchHandler3) {
                    isStartCatchHandler3 = true;
                    mCatchChildFragment.resetShowData(false); // 刷新视图
                }
                if (!isStartCatchHandler4) {
                    isStartCatchHandler4 = true;
                    mCatchChildFragment.resetShowData(false); // 刷新视图
                }
                if (!isStartCatchHandler5) {
                    isStartCatchHandler5 = true;
                    mCatchChildFragment.resetShowData(false); // 刷新视图
                }
                if (!isStartCatchHandler6) {
                    isStartCatchHandler6 = true;
                    mCatchChildFragment.resetShowData(false); // 刷新视图
                }
                if (!isStartCatchHandler7) {
                    isStartCatchHandler7 = true;
                    mCatchChildFragment.resetShowData(false); // 刷新视图
                }
                if (!isStartCatchHandler8) {
                    isStartCatchHandler8 = true;
                    mCatchChildFragment.resetShowData(false); // 刷新视图
                }
                if (MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getWorkState(cell_id) == GnbBean.State.TRACE && !isRsrpStart && rsp.getRsrp() > 5) {
                    countFirst = 0;
                    countSecond = 0;
                    countThird = 0;
                    countFourth = 0;
                    isRsrpStart = true;
                    mHandler.sendEmptyMessageDelayed(2, TraceBean.RSRP_TIME_INTERVAL);
                }
            }
            mCfgTraceChildFragment.isAutoMode = false;
        }
    }

    public void onStopTraceRsp(String id, GnbCmdRsp rsp) {
        int logicIndex = MainActivity.getInstance().getLogicIndexById(id);
        int index = DeviceUtil.getIndexById(id);
        if (logicIndex == -1) return;
        int channelNum = logicIndex * 2 + rsp.getCellId() + 1;
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_STOP_TRACE || rsp.getCmdType() == GnbProtocol.UI_2_gNB_STOP_LTE_TRACE) {
                isCell1SetArfcnChange = false;
                isCell2SetArfcnChange = false;
                isCell3SetArfcnChange = false;
                isCell4SetArfcnChange = false;
                isCell5SetArfcnChange = false;
                isCell6SetArfcnChange = false;
                isCell7SetArfcnChange = false;
                isCell8SetArfcnChange = false;

                mCfgTraceChildFragment.isAutoMode = false;
                //待修改：载波频点
                //切换频段接着运行
                switch (channelNum) {
                    case 1:
                        if (autoRun1 && MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST) != GnbBean.State.STOP) {
                            updateParamAndStart(id, Integer.parseInt(MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getArfcn(GnbProtocol.CellId.FIRST)), rsp.getCellId());
                            return;
                        }
                        break;
                    case 2:
                        if (autoRun2 && MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) != GnbBean.State.STOP) {
                            updateParamAndStart(id, Integer.parseInt(MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND)), rsp.getCellId());
                            return;
                        }
                        break;
                    case 3:
                        if (autoRun3 && MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST) != GnbBean.State.STOP) {
                            updateParamAndStart(id, Integer.parseInt(MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getArfcn(GnbProtocol.CellId.FIRST)), rsp.getCellId());
                            return;
                        }
                        break;
                    case 4:
                        if (autoRun4 && MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) != GnbBean.State.STOP) {
                            updateParamAndStart(id, Integer.parseInt(MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND)), rsp.getCellId());
                            return;
                        }
                        break;
                    case 5:
                        if (autoRun5 && MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST) != GnbBean.State.STOP) {
                            updateParamAndStart(id, Integer.parseInt(MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getArfcn(GnbProtocol.CellId.FIRST)), rsp.getCellId());
                            return;
                        }
                        break;
                    case 6:
                        if (autoRun6 && MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) != GnbBean.State.STOP) {
                            updateParamAndStart(id, Integer.parseInt(MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND)), rsp.getCellId());
                            return;
                        }
                        break;
                    case 7:
                        if (autoRun7 && MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST) != GnbBean.State.STOP) {
                            updateParamAndStart(id, Integer.parseInt(MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getArfcn(GnbProtocol.CellId.FIRST)), rsp.getCellId());
                            return;
                        }
                        break;
                    case 8:
                        if (autoRun8 && MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) != GnbBean.State.STOP) {
                            updateParamAndStart(id, Integer.parseInt(MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND)), rsp.getCellId());
                            return;
                        }
                        break;
                }

                String s = DeviceUtil.getCellStr(logicIndex, rsp.getCellId());
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    setProgress(logicIndex, 0, rsp.getCellId(), getString(R.string.idle), false);
                    mTraceChildFragment.resetRsrp(logicIndex);
                    mCfgTraceChildFragment.setcfgEnable(-1, true);
                } else {
                    if (rsp.getRspValue() == 3) {
                        Util.showToast("通道" + s + "忙，请稍后再结束");
                        MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.TRACE);
                        setProgress(logicIndex, 100, rsp.getCellId(), "定位中", false);
                        refreshTraceBtn();
                        return;
                    } else if (rsp.getRspValue() == 5) {
                        setProgress(logicIndex, 0, rsp.getCellId(), "空闲", false);
                    } else {
                        setProgress(logicIndex, 50, rsp.getCellId(), "结束失败", true);
                        Util.showToast("通道" + s + "结束工作失败, 请重试");
                    }
                }
                OpLog.build().write(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + channelNum + "\t\t结束定位");
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setTacChange(rsp.getCellId(), false);
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setEnable(rsp.getCellId(), false);
                //refreshTraceValue(logicIndex, rsp.getCellId(), 0);
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setRsrp(rsp.getCellId(), 0);
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setLastRsrp(rsp.getCellId(), -1);
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setImsi(rsp.getCellId(), "");
                //根据通道调用mHandler.removeMessages
                switch (channelNum) {
                    case 1:
                        mHandler.removeMessages(11);
                        break;
                    case 2:
                        mHandler.removeMessages(12);
                        break;
                    case 3:
                        mHandler.removeMessages(13);
                        break;
                    case 4:
                        mHandler.removeMessages(14);
                        break;
                    case 5:
                        mHandler.removeMessages(15);
                        break;
                    case 6:
                        mHandler.removeMessages(16);
                        break;
                    case 7:
                        mHandler.removeMessages(17);
                        break;
                    case 8:
                        mHandler.removeMessages(18);
                        break;
                }

                isRsrpStart = false;
                isStartCatchHandler1 = false;
                isStartCatchHandler2 = false;
                isStartCatchHandler3 = false;
                isStartCatchHandler4 = false;
                isStartCatchHandler5 = false;
                isStartCatchHandler6 = false;
                isStartCatchHandler7 = false;
                isStartCatchHandler8 = false;
                refreshTraceBtn();
            }
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

    //待修改：载波分裂
    //通过4个通道状态设置按钮状态
    public void refreshTraceBtn() {
        TextView tv_do_btn = mCfgTraceChildFragment.getTv_do_btn();
        List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();

        if (deviceList.size() <= 0) { // 无设备
            AppLog.I("refreshTraceBtn deviceList size = 0");
            tv_do_btn.setText(getString(R.string.open));
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
            return;
        }
//        if (deviceList.size() == 1) { // 一个设备
//            TraceUtil traceUtil = deviceList.get(0).getTraceUtil();
//
//            int workStateFirst = traceUtil.getWorkState(GnbProtocol.CellId.FIRST); // 通道一
//            int workStateSecond = traceUtil.getWorkState(GnbProtocol.CellId.SECOND); // 通道二
////            int workStateThird = traceUtil.getWorkState(GnbProtocol.CellId.THIRD); // 通道三
////            int workStateFourth = traceUtil.getWorkState(GnbProtocol.CellId.FOURTH); // 通道四
//
//            AppLog.I("refreshTraceBtn deviceList size = 1, workStateFirst = " + workStateFirst
//                    + ", workStateSecond = " + workStateSecond);
//
//            if (workStateFirst == GnbBean.State.TRACE || workStateSecond == GnbBean.State.TRACE) {
//                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.TRACE);
//                tv_do_btn.setText(getString(R.string.stop));
//
//            } else if (workStateFirst == GnbBean.State.CATCH || workStateSecond == GnbBean.State.CATCH) {
//                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.CATCH);
//                tv_do_btn.setText(getString(R.string.stop));
//
//            } else if (workStateFirst == GnbBean.State.STOP || workStateSecond == GnbBean.State.STOP) {
//                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.STOP);
//                tv_do_btn.setText(getString(R.string.stoping));
//            } else if (workStateFirst == GnbBean.State.IDLE && workStateSecond == GnbBean.State.IDLE) {
//                if (deviceList.get(0).getWorkState() == GnbBean.State.FREQ_SCAN) {
//                    tv_do_btn.setText(getString(R.string.starting));
//                } else {
//                    MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.IDLE);
//                    tv_do_btn.setText(getString(R.string.open));
//                    AppLog.I("refreshTraceBtn deviceList.get(0).getWorkState() = " + deviceList.get(0).getWorkState());
//                    mCfgTraceChildFragment.setcfgEnable(-1, true);
//                }
//            } else {
//                tv_do_btn.setText(getString(R.string.starting));
//            }
//            if (getBtnStr().contains(getString(R.string.stop))) {
//                tv_do_btn.setBackgroundResource(R.drawable.btn_bg_delete);
//            } else {
//                tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
//                mCfgTraceChildFragment.setIsCloaseDistance(false);
//            }
//            return;
//        }

        //多设备
        //只要有一个设备开启（Trace、Catch）就是开启状态，没有开启有停止就是停止状态，都是空闲或未连接就是空闲状态，其他就是启动中
        if (deviceList.size() >= 1) {
            int startCount = 0; //通道开启
            int stopCount = 0;  //通道结束中
            int IDLEOrNoneCount = 0;    //通道空闲或未连接

            for (DeviceInfoBean deviceInfoBean : deviceList) {
                int workStateFirst = deviceInfoBean.getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST);
                int workStateSecond = deviceInfoBean.getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND);

                AppLog.I("refreshTraceBtn deviceInfoBean" + deviceInfoBean.getRsp().getDevName() +
                        ", workStateFirst = " + workStateFirst + ", workStateSecond = " + workStateSecond);

                if (workStateFirst == GnbBean.State.TRACE || workStateFirst == GnbBean.State.CATCH) {
                    startCount++;

                }
                if (workStateSecond == GnbBean.State.TRACE || workStateSecond == GnbBean.State.CATCH) {
                    startCount++;

                }
                if (workStateFirst == GnbBean.State.TRACE || workStateSecond == GnbBean.State.TRACE)
                    deviceInfoBean.setWorkState(GnbBean.State.TRACE);
                if (workStateFirst == GnbBean.State.CATCH || workStateSecond == GnbBean.State.CATCH)
                    deviceInfoBean.setWorkState(GnbBean.State.CATCH);

                if (workStateFirst == GnbBean.State.STOP) {
                    stopCount++;
                }
                if (workStateSecond == GnbBean.State.STOP) {
                    stopCount++;
                }
                if (workStateFirst == GnbBean.State.STOP || workStateSecond == GnbBean.State.STOP) {
                    deviceInfoBean.setWorkState(GnbBean.State.STOP);
                }

                if (workStateFirst == GnbBean.State.IDLE || workStateFirst == GnbBean.State.NONE) {
                    IDLEOrNoneCount++;
                }
                if (workStateSecond == GnbBean.State.IDLE || workStateSecond == GnbBean.State.NONE) {
                    IDLEOrNoneCount++;
                }
                if (workStateFirst == GnbBean.State.IDLE && workStateSecond == GnbBean.State.IDLE) {
                    deviceInfoBean.setWorkState(GnbBean.State.IDLE);
                }
                if (workStateFirst == GnbBean.State.NONE || workStateSecond == GnbBean.State.NONE) {
                    deviceInfoBean.setWorkState(GnbBean.State.NONE);
                }
            }

            AppLog.D("TraceCatchFragment refreshTraceBtn startCount = " + startCount + ", stopCount = " + stopCount + ", IDLEOrNoneCount = " + IDLEOrNoneCount);
            if (startCount > 0) {
                tv_do_btn.setText(getString(R.string.stop));
                mCfgTraceChildFragment.setcfgEnable(-1, false);
            } else if (stopCount > 0) {
                tv_do_btn.setText(getString(R.string.stoping));
                mCfgTraceChildFragment.setcfgEnable(-1, false);
            } else if (IDLEOrNoneCount == MainActivity.getInstance().getDeviceList().size() * 2) {
                tv_do_btn.setText(getString(R.string.open));
                mCfgTraceChildFragment.setcfgEnable(-1, true);
            } else {
                tv_do_btn.setText(getString(R.string.starting));
                mCfgTraceChildFragment.setcfgEnable(-1, false);
            }

            if (getBtnStr().contains(getString(R.string.stop)))
                tv_do_btn.setBackgroundResource(R.drawable.btn_bg_delete);
            else {
                tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
                mCfgTraceChildFragment.setIsCloaseDistance(false);
            }
        }
    }

    int countFirst = 0;
    int countSecond = 0;
    int countThird = 0;
    int countFourth = 0;
    int countFifth = 0;
    int countSixth = 0;
    int countSeventh = 0;
    int countEighth = 0;
    boolean isRsrpStart = false;
    int setArfcnChangeCell1, setArfcnChangeCell2, setArfcnChangeCell3, setArfcnChangeCell4; //当前的频点循环索引
    List<Integer> arfcnListCell1 = new ArrayList<>();
    List<Integer> arfcnListCell2 = new ArrayList<>();
    List<Integer> arfcnListCell3 = new ArrayList<>();
    List<Integer> arfcnListCell4 = new ArrayList<>();

    //1
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    break;
                case 2: //刷新数值
                    List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
                    for (int i = 0; i < deviceList.size(); i++) {
                        TraceUtil traceUtil = deviceList.get(i).getTraceUtil();
                        int logicIndex = DeviceUtil.getLogicIndexById(deviceList.get(i).getRsp().getDeviceId());
                        refreshRsrpByHandler(logicIndex, i, traceUtil);
                    }
                    mHandler.sendEmptyMessageDelayed(2, TraceBean.RSRP_TIME_INTERVAL);
                    break;
                //通道一
                case 11:
                    AppLog.D("mHandler message 11----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler1) { //上号不执行
                        if (arfcnBeanHashMap.get("TD1") == null) return;
                        LinkedList<Integer> arfcnList = arfcnBeanHashMap.get("TD1").getArfcnList();
                        if (arfcnList.size() == 1) return;
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex0) {
                                String value = String.valueOf(arfcnList.get(i));    //下一个准备轮询的arfcn值
                                if (NrBand.earfcn2band(Integer.parseInt(value)) == NrBand.earfcn2band(Integer.parseInt(mLogicTraceUtilNr.getArfcn(GnbProtocol.CellId.FIRST)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(1, value, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(1, value, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilNr.setArfcn(GnbProtocol.CellId.FIRST, value); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(11, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(11, time_count * 1000L);
                    break;
                case 12:
                    AppLog.D("mHandler message 12----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler2) {
                        LinkedList<Integer> arfcnList = arfcnBeanHashMap.get("TD2").getArfcnList();
                        if (arfcnList.size() == 1) return;
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex1) {
                                String value = String.valueOf(arfcnList.get(i));
                                if (LteBand.earfcn2band(Integer.parseInt(value)) == LteBand.earfcn2band(Integer.parseInt(mLogicTraceUtilNr.getArfcn(GnbProtocol.CellId.SECOND)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(2, value, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(2, value, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilNr.setArfcn(GnbProtocol.CellId.SECOND, value); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(12, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(12, time_count * 1000L);
                    break;
                case 13:
                    AppLog.D("mHandler message 13----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler3) {
                        LinkedList<Integer> arfcnList = arfcnBeanHashMap.get("TD3").getArfcnList();
                        if (arfcnList.size() == 1) return;
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex2) {
                                String value = String.valueOf(arfcnList.get(i));
                                if (NrBand.earfcn2band(Integer.parseInt(value)) == NrBand.earfcn2band(Integer.parseInt(mLogicTraceUtilNr2.getArfcn(GnbProtocol.CellId.FIRST)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(3, value, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(3, value, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilNr2.setArfcn(GnbProtocol.CellId.FIRST, value); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(13, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(13, time_count * 1000L);
                    break;
                case 14:
                    AppLog.D("mHandler message 14----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler4) {
                        LinkedList<Integer> arfcnList = arfcnBeanHashMap.get("TD4").getArfcnList();
                        if (arfcnList.size() == 1) return;
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex3) {
                                String value = String.valueOf(arfcnList.get(i));
                                String arfcn = mLogicTraceUtilNr2.getArfcn(GnbProtocol.CellId.SECOND);
                                if (LteBand.earfcn2band(Integer.parseInt(value)) == LteBand.earfcn2band(Integer.parseInt(arfcn))) {
                                    int freqValue = LteBand.earfcn2freq(Integer.parseInt(value));
                                    int freqArfcn = LteBand.earfcn2freq(Integer.parseInt(arfcn));
                                    if (((freqArfcn > 1709 && freqArfcn < 1735) || (freqArfcn > 1804 && freqArfcn < 1830)) && (freqValue > 1709 && freqValue < 1735) || (freqValue > 1804 && freqValue < 1830)
                                            || ((freqArfcn > 888 && freqArfcn < 904) || (freqArfcn > 933 && freqArfcn < 949)) && (freqValue > 888 && freqValue < 904) || (freqValue > 933 && freqValue < 949)) {
                                        AppLog.D("mHandler 3 change arfcn = " + value + ", autoArfcnIndex = " + autoArfcnIndex3);
                                        //更新页面信息
                                        freshDoWorkState(1, GnbProtocol.CellId.SECOND, mLogicTraceUtilNr2.getImsi(GnbProtocol.CellId.SECOND), value, mLogicTraceUtilNr2.getPci(GnbProtocol.CellId.SECOND));
                                        MessageController.build().setArfcn(MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId(), 3, value);
                                    } else {
                                        AppLog.D("mHandler 3 1300 1650 change need stop, arfcn = " + value + ", autoArfcnIndex = " + autoArfcnIndex3);
                                        autoRun4 = true;
                                        String deviceId = MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId();
                                        MessageController.build().setCmdAndCellID(deviceId, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.SECOND);
                                        PaCtl.build().closePAByArfcn(deviceId, true, mLogicTraceUtilNr2.getArfcn(GnbProtocol.CellId.SECOND));
                                        MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setArfcn(GnbProtocol.CellId.SECOND, value);
                                    }
                                    autoArfcnIndex3++;
                                    if (autoArfcnIndex3 >= arfcnList.size()) autoArfcnIndex3 = 0;
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(4, value, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilNr2.setArfcn(GnbProtocol.CellId.FOURTH, value); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(14, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(14, time_count * 1000L);
                    break;
                case 15:
                    AppLog.D("mHandler message 15----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler5) {
                        LinkedList<Integer> arfcnList = arfcnBeanHashMap.get("TD5").getArfcnList();
                        if (arfcnList.size() == 1) return;
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex4) {
                                String value = String.valueOf(arfcnList.get(i));
                                if (NrBand.earfcn2band(Integer.parseInt(value)) == NrBand.earfcn2band(Integer.parseInt(mLogicTraceUtilLte.getArfcn(GnbProtocol.CellId.FIRST)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(5, value, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(5, value, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilLte.setArfcn(GnbProtocol.CellId.FIRST, value); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(15, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(15, time_count * 1000L);
                    break;
                case 16:
                    AppLog.D("mHandler message 16----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler6) {
                        LinkedList<Integer> arfcnList = arfcnBeanHashMap.get("TD6").getArfcnList();
                        if (arfcnList.size() == 1) return;
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex5) {
                                String value = String.valueOf(arfcnList.get(i));
                                if (NrBand.earfcn2band(Integer.parseInt(value)) == NrBand.earfcn2band(Integer.parseInt(mLogicTraceUtilLte.getArfcn(GnbProtocol.CellId.SECOND)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(6, value, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(6, value, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilLte.setArfcn(GnbProtocol.CellId.SECOND, value); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(16, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(16, time_count * 1000L);
                    break;
                case 17:
                    AppLog.D("mHandler message 17----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler7) {
                        LinkedList<Integer> arfcnList = arfcnBeanHashMap.get("TD7").getArfcnList();
                        if (arfcnList.size() == 1) return;
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex6) {
                                String value = String.valueOf(arfcnList.get(i));
                                if (NrBand.earfcn2band(Integer.parseInt(value)) == NrBand.earfcn2band(Integer.parseInt(mLogicTraceUtilLte2.getArfcn(GnbProtocol.CellId.FIRST)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(7, value, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(7, value, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilLte2.setArfcn(GnbProtocol.CellId.FIRST, value); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(17, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(17, time_count * 1000L);
                    break;
                case 18:
                    AppLog.D("mHandler message 18----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler8) {
                        LinkedList<Integer> arfcnList = arfcnBeanHashMap.get("TD8").getArfcnList();
                        if (arfcnList.size() == 1) return;
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex7) {
                                String value = String.valueOf(arfcnList.get(i));
                                if (NrBand.earfcn2band(Integer.parseInt(value)) == NrBand.earfcn2band(Integer.parseInt(mLogicTraceUtilLte2.getArfcn(GnbProtocol.CellId.SECOND)))) {
                                    //要切换的值在同频带
                                    nextArfcnInSameBand(8, value, arfcnList.size());
                                } else {
                                    //不同频带
                                    nextArfcnInDiffBand(8, value, arfcnList.size());
                                    return;
                                }
                                mLogicTraceUtilLte2.setArfcn(GnbProtocol.CellId.SECOND, value); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(18, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(18, time_count * 1000L);
                    break;
            }
        }

        //待修改：载波频点
        private void refreshRsrpByHandler(int logicIndex, int index, TraceUtil traceUtil) {
            if (traceUtil.getWorkState(GnbProtocol.CellId.FIRST) == GnbBean.State.TRACE) {
                int rsrp = -1;
                int channelNum = logicIndex * 2 + 1;
                int count = getCountByChannelNum(channelNum);
                if (traceUtil.getRsrp(GnbProtocol.CellId.FIRST) != -1) {
                    rsrp = traceUtil.getRsrp(GnbProtocol.CellId.FIRST);
                    count = 0;
                } else {
                    count++;
                }
//                            AppLog.D("mHandler update first value = " + rsrp + ", countFirst = " + countFirst);
                if (count < 6) {
                    if (rsrp != -1) {
                        mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.FIRST, index, rsrp);
                    }
                } else {
                    //掉线，恢复轮询
                    isStartCatchHandler1 = false;
                    mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.FIRST, index, rsrp);
                }
                setCount(channelNum, count);
            }
            if (traceUtil.getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.TRACE) {
                int rsrp = -1;
                int channelNum = logicIndex * 2 + 2;
                int count = getCountByChannelNum(channelNum);
                if (traceUtil.getRsrp(GnbProtocol.CellId.SECOND) != -1) {
                    rsrp = traceUtil.getRsrp(GnbProtocol.CellId.SECOND);
                    count = 0;
                } else {
                    count++;
                }
                //AppLog.I("mHandler update second value = " + rsrp + ", countSecond = " + countSecond);
                if (count < 6) {
                    if (rsrp != -1) {
                        mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.SECOND, index, rsrp);
                    }
                } else {
                    //掉线，恢复轮询
                    isStartCatchHandler2 = false;
                    mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.SECOND, index, rsrp);
                }
                setCount(channelNum, count);
            }
        }

        private void setCount(int channelNum, int count) {
            switch (channelNum) {
                case 1:
                    countFirst = count;
                    break;
                case 2:
                    countSecond = count;
                    break;
                case 3:
                    countThird = count;
                    break;
                case 4:
                    countFourth = count;
                    break;
                case 5:
                    countFifth = count;
                    break;
                case 6:
                    countSixth = count;
                    break;
                case 7:
                    countSeventh = count;
                    break;
                case 8:
                    countEighth = count;
                    break;
            }

        }

        private int getCountByChannelNum(int channelNum) {
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
            }
            return 0;
        }

        //同频道
        private void nextArfcnInSameBand(int channelNum, String value, int arfcnListSize) {
            int autoArfcnIndex = channelNumToAutoArfcnIndex(channelNum);
            int deviceChannel = DeviceUtil.getDeviceChannelIndexByChannelNum(channelNum);
            int logicIndex = DeviceUtil.getLogicIndexByChannelNum(channelNum);
            int index = DeviceUtil.getIndexByChannelNum(channelNum);
            AppLog.D("mHandler 1" + channelNum + " change arfcn = " + value + ", autoArfcnIndex" + (channelNum - 1) + " = " + autoArfcnIndex);
            //更新页面信息
            freshDoWorkState(logicIndex, deviceChannel, mLogicTraceUtilNr.getImsi(channelNum - 1), value, mLogicTraceUtilNr.getPci(channelNum - 1));
            MessageController.build().setArfcn(MainActivity.getInstance().getDeviceList().get(index).getRsp().getDeviceId(), deviceChannel, value);
            autoArfcnIndex++;
            AppLog.D("channelNum=" + channelNum + "autoArfcnIndex++, autoArfcnIndex =" + (channelNum - 1) + " " + autoArfcnIndex);
            if (autoArfcnIndex >= arfcnListSize) autoArfcnIndex = 0;
            autoArfcnIndexToNum(channelNum, autoArfcnIndex);
        }

        private void nextArfcnInDiffBand(int channelNum, String value, int arfcnListSize) {
            int autoArfcnIndex = channelNumToAutoArfcnIndex(channelNum);
            int deviceChannel = DeviceUtil.getDeviceChannelIndexByChannelNum(channelNum);
            int deviceIndex = DeviceUtil.getLogicIndexByChannelNum(channelNum);
            int index = DeviceUtil.getIndexByChannelNum(channelNum);
            String deviceId = MainActivity.getInstance().getDeviceList().get(deviceIndex).getRsp().getDeviceId();
            AppLog.D("mHandler 1" + channelNum + " change arfcn = " + value + " is different band, stop at first" + ", autoArfcnIndex" + (channelNum - 1) + " = " + autoArfcnIndex);
            setAutoRunByChannelNum(channelNum, true);
            MessageController.build().setCmdAndCellID(deviceId, GnbProtocol.UI_2_gNB_STOP_TRACE, deviceChannel);
            PaCtl.build().closePAByArfcn(deviceId, channelNum > 4, mLogicTraceUtilNr.getArfcn(deviceChannel));
            MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setArfcn(deviceChannel, value);
            autoArfcnIndex++;
            if (autoArfcnIndex >= arfcnListSize) autoArfcnIndex = 0;
            autoArfcnIndexToNum(channelNum, autoArfcnIndex);
        }

        private void setAutoRunByChannelNum(int channelNum, boolean b) {
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
            }
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
            }
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
            }
            return autoArfcnIndex0;
        }
    };

    //1
    private void freshDoWorkState(int logicIndex, int cell_id, String imsi, String
            arfcn, String pci) {
        AppLog.D("freshDoWorkState logicIndex = " + logicIndex + ", cell_id = " + cell_id + ", imsi = " + imsi + ", arfcn = " + arfcn + ", pci = " + pci);
        setProgress(logicIndex, 100, cell_id, getString(R.string.traceing), false);
        if (!imsi.isEmpty()) {
            //imsi = imsi.substring(0, 5) + "****" + imsi.substring(11);
            mTraceChildFragment.setConfigInfo(cell_id, logicIndex, arfcn, pci, imsi);
        }
    }

    public List<ImsiBean> getDataList() {
        return mImsiList;
    }

    public void onFreqScanRsp(String id, GnbFreqScanRsp rsp) {
        if (mFreqDialog != null) mFreqDialog.onFreqScanRsp(id, rsp);
    }

    //待修改：载波频点
    //自动定位
    //扫频走完开始定位，使用扫出来的pci算法
    @Override
    public void onTraceConfig(String id) {
        AppLog.D("onTraceConfig");
        autoArfcnIndex0 = 0;
        autoArfcnIndex1 = 0;
        autoArfcnIndex2 = 0;
        autoArfcnIndex3 = 0;
        autoArfcnIndex4 = 0;
        autoArfcnIndex5 = 0;
        autoArfcnIndex6 = 0;
        autoArfcnIndex7 = 0;

        autoRun1 = false;
        autoRun2 = false;
        autoRun3 = false;
        autoRun4 = false;
        autoRun5 = false;
        autoRun6 = false;
        autoRun7 = false;
        autoRun8 = false;

        mHandler.removeCallbacksAndMessages(null);

        txValue = 0;
        mCatchChildFragment.restartCatch();
        isClickStop = false;
        mImsiList.clear();
        mCatchChildFragment.resetShowData(true);
        all_count = 0;
        unicom_count = 0;
        mobile_count = 0;
        telecom_count = 0;
        sva_count = 0;
        mCatchChildFragment.setCatchCount(all_count, telecom_count, mobile_count, unicom_count, sva_count);
        mTraceChildFragment.resetRsrp();

        if (MainActivity.getInstance().getDeviceList().size() <= 0) {
            Util.showToast(getString(R.string.dev_not_online));
            mCfgTraceChildFragment.isAutoMode = false;
            return;
        }

        boolean isAirSync = PrefUtil.build().getValue("sync_mode", "空口").toString().equals("空口");
        int enableAir = isAirSync ? 1 : 0;
        long timeMillis = System.currentTimeMillis();
        //待修改：载波频点
        mLogicTraceUtilNr.setAtCmdTimeOut(GnbProtocol.CellId.FIRST, timeMillis);
        mLogicTraceUtilNr.setAtCmdTimeOut(GnbProtocol.CellId.SECOND, timeMillis);
        mLogicTraceUtilNr2.setAtCmdTimeOut(GnbProtocol.CellId.FIRST, timeMillis);
        mLogicTraceUtilNr2.setAtCmdTimeOut(GnbProtocol.CellId.SECOND, timeMillis);
        mLogicTraceUtilLte.setAtCmdTimeOut(GnbProtocol.CellId.FIRST, timeMillis);
        mLogicTraceUtilLte.setAtCmdTimeOut(GnbProtocol.CellId.SECOND, timeMillis);
        mLogicTraceUtilLte2.setAtCmdTimeOut(GnbProtocol.CellId.FIRST, timeMillis);
        mLogicTraceUtilLte2.setAtCmdTimeOut(GnbProtocol.CellId.SECOND, timeMillis);

        //设置配置信息
        // 通道一
        boolean[] runCell = new boolean[]{false, false, false, false, false, false, false, false};
        //待修改：载波频点
        String arfcn_cfg = "";
        String pci_cfg = "";
        LinkedList<Integer> td1 = null;
        if (arfcnBeanHashMap.get("TD1") != null) {
            td1 = arfcnBeanHashMap.get("TD1").getArfcnList();
            if (td1.size() > 0) {
                arfcn_cfg = String.valueOf(td1.get(0));
                if (!auto_trace_imsi.isEmpty())
                    mLogicTraceUtilNr.setImsi(GnbProtocol.CellId.FIRST, auto_trace_imsi);
            }
            //拿到pci计算
            pci_cfg = PrefUtil.build().getValue("PCI_TD1", "1001").toString();
            mLogicTraceUtilNr.setAirSync(GnbProtocol.CellId.FIRST, enableAir);
            mLogicTraceUtilNr.setBandWidth(GnbProtocol.CellId.FIRST, 100);
            mLogicTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.FIRST, 255);
            mLogicTraceUtilNr.setPlmn(GnbProtocol.CellId.FIRST, "46000");
            if (!arfcn_cfg.isEmpty()) runCell[0] = true;
            mLogicTraceUtilNr.setArfcn(GnbProtocol.CellId.FIRST, arfcn_cfg);
            mLogicTraceUtilNr.setPci(GnbProtocol.CellId.FIRST, pci_cfg);
        }


        // 通道二
        arfcn_cfg = "";
        LinkedList<Integer> td2 = null;
        if (arfcnBeanHashMap.get("TD2") != null) {
            td2 = arfcnBeanHashMap.get("TD2").getArfcnList();
            if (td2.size() > 0) {
                arfcn_cfg = String.valueOf(td2.get(0));
                if (!auto_trace_imsi.isEmpty())
                    mLogicTraceUtilNr.setImsi(GnbProtocol.CellId.SECOND, auto_trace_imsi);
            }

            pci_cfg = PrefUtil.build().getValue("PCI_TD2", "1002").toString();
            mLogicTraceUtilNr.setAirSync(GnbProtocol.CellId.SECOND, enableAir);
            if (!arfcn_cfg.isEmpty()) runCell[1] = true;
            mLogicTraceUtilNr.setBandWidth(GnbProtocol.CellId.SECOND, 20);
            mLogicTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.SECOND, 240);
            mLogicTraceUtilNr.setPlmn(GnbProtocol.CellId.SECOND, "46001");
            mLogicTraceUtilNr.setArfcn(GnbProtocol.CellId.SECOND, arfcn_cfg);
            mLogicTraceUtilNr.setPci(GnbProtocol.CellId.SECOND, pci_cfg);
        }

        // 通道三
        arfcn_cfg = "";
        LinkedList<Integer> td3 = null;
        if (arfcnBeanHashMap.get("TD3") != null) {
            td3 = arfcnBeanHashMap.get("TD3").getArfcnList();
            if (td3.size() > 0) {
                arfcn_cfg = String.valueOf(td3.get(0));
                if (!auto_trace_imsi.isEmpty())
                    mLogicTraceUtilNr2.setImsi(GnbProtocol.CellId.FIRST, auto_trace_imsi);
            }

            pci_cfg = PrefUtil.build().getValue("PCI_TD3", "1003").toString();
            mLogicTraceUtilNr2.setAirSync(GnbProtocol.CellId.FIRST, enableAir);
            if (!arfcn_cfg.isEmpty()) {
                int band2 = Integer.parseInt(arfcn_cfg) > 100000 ? NrBand.earfcn2band(Integer.parseInt(arfcn_cfg)) : LteBand.earfcn2band(Integer.parseInt(arfcn_cfg));
                mLogicTraceUtilNr2.setBandWidth(GnbProtocol.CellId.FIRST, 100);
                mLogicTraceUtilNr2.setSsbBitmap(GnbProtocol.CellId.FIRST, 255);
                mLogicTraceUtilNr2.setPlmn(GnbProtocol.CellId.FIRST, band2 == 78 ? "46001" : "46000");
                runCell[2] = true;
            }
            mLogicTraceUtilNr2.setArfcn(GnbProtocol.CellId.FIRST, arfcn_cfg);
            mLogicTraceUtilNr2.setPci(GnbProtocol.CellId.FIRST, pci_cfg);
        }

        // 通道四
        arfcn_cfg = "";
        LinkedList<Integer> td4 = null;
        if (arfcnBeanHashMap.get("TD4") != null) {
            td4 = arfcnBeanHashMap.get("TD4").getArfcnList();
            if (td4.size() > 0) {
                arfcn_cfg = String.valueOf(td4.get(0));
                if (!auto_trace_imsi.isEmpty())
                    mLogicTraceUtilNr2.setImsi(GnbProtocol.CellId.SECOND, auto_trace_imsi);
            }

            if (!arfcn_cfg.isEmpty()) {
                mLogicTraceUtilNr2.setBandWidth(GnbProtocol.CellId.SECOND, 20);
                mLogicTraceUtilNr2.setSsbBitmap(GnbProtocol.CellId.SECOND, 240);
                mLogicTraceUtilNr2.setPlmn(GnbProtocol.CellId.SECOND, "46000");
                runCell[3] = true;
            }
            pci_cfg = PrefUtil.build().getValue("PCI_TD4", "1004").toString();
            mLogicTraceUtilNr2.setAirSync(GnbProtocol.CellId.SECOND, enableAir);
            mLogicTraceUtilNr2.setArfcn(GnbProtocol.CellId.SECOND, arfcn_cfg);
            mLogicTraceUtilNr2.setPci(GnbProtocol.CellId.SECOND, pci_cfg);
        }
        //通道五
        arfcn_cfg = "";
        LinkedList<Integer> td5 = null;
        if (arfcnBeanHashMap.get("TD5") != null) {
            td5 = arfcnBeanHashMap.get("TD5").getArfcnList();
            if (td5.size() > 0) {
                arfcn_cfg = String.valueOf(td5.get(0));
                if (!auto_trace_imsi.isEmpty())
                    mLogicTraceUtilLte.setImsi(GnbProtocol.CellId.FIRST, auto_trace_imsi);
            }

            if (!arfcn_cfg.isEmpty()) {
                mLogicTraceUtilLte.setBandWidth(GnbProtocol.CellId.FIRST, 5);
                mLogicTraceUtilLte.setSsbBitmap(GnbProtocol.CellId.FIRST, 128);
                mLogicTraceUtilLte.setPlmn(GnbProtocol.CellId.FIRST, "46000");
                runCell[4] = true;
            }
            pci_cfg = PrefUtil.build().getValue("PCI_TD5", "501").toString();
            mLogicTraceUtilLte.setAirSync(GnbProtocol.CellId.FIRST, enableAir);
            mLogicTraceUtilLte.setArfcn(GnbProtocol.CellId.FIRST, arfcn_cfg);
            mLogicTraceUtilLte.setPci(GnbProtocol.CellId.FIRST, pci_cfg);
        }

        //通道六
        arfcn_cfg = "";
        LinkedList<Integer> td6 = null;
        if (arfcnBeanHashMap.get("TD6") != null) {
            td6 = arfcnBeanHashMap.get("TD6").getArfcnList();
            if (td6.size() > 0) {
                arfcn_cfg = String.valueOf(td6.get(0));
                if (!auto_trace_imsi.isEmpty())
                    mLogicTraceUtilLte.setImsi(GnbProtocol.CellId.SECOND, auto_trace_imsi);
            }

            if (!arfcn_cfg.isEmpty()) {
                int band = Integer.parseInt(arfcn_cfg) > 100000 ? NrBand.earfcn2band(Integer.parseInt(arfcn_cfg)) : LteBand.earfcn2band(Integer.parseInt(arfcn_cfg));
                mLogicTraceUtilLte.setBandWidth(GnbProtocol.CellId.SECOND, 5);
                mLogicTraceUtilLte.setSsbBitmap(GnbProtocol.CellId.SECOND, 128);
                mLogicTraceUtilLte.setPlmn(GnbProtocol.CellId.SECOND, band == 5 ? "46011" : "46000");
                runCell[5] = true;
            }
            pci_cfg = PrefUtil.build().getValue("PCI_TD6", "502").toString();
            mLogicTraceUtilLte.setAirSync(GnbProtocol.CellId.SECOND, enableAir);
            mLogicTraceUtilLte.setArfcn(GnbProtocol.CellId.SECOND, arfcn_cfg);
            mLogicTraceUtilLte.setPci(GnbProtocol.CellId.SECOND, pci_cfg);
        }
        //通道七
        arfcn_cfg = "";
        LinkedList<Integer> td7 = null;
        if (arfcnBeanHashMap.get("TD7") != null) {
            td7 = arfcnBeanHashMap.get("TD7").getArfcnList();
            if (td7.size() > 0) {
                arfcn_cfg = String.valueOf(td7.get(0));
                if (!auto_trace_imsi.isEmpty())
                    mLogicTraceUtilLte2.setImsi(GnbProtocol.CellId.FIRST, auto_trace_imsi);
            }

            if (!arfcn_cfg.isEmpty()) {
                mLogicTraceUtilLte2.setBandWidth(GnbProtocol.CellId.FIRST, 5);
                mLogicTraceUtilLte2.setSsbBitmap(GnbProtocol.CellId.FIRST, 128);
                mLogicTraceUtilLte2.setPlmn(GnbProtocol.CellId.FIRST, "46000");
                runCell[6] = true;
            }
            pci_cfg = PrefUtil.build().getValue("PCI_TD7", "503").toString();
            mLogicTraceUtilLte2.setAirSync(GnbProtocol.CellId.FIRST, enableAir);
            mLogicTraceUtilLte2.setArfcn(GnbProtocol.CellId.FIRST, arfcn_cfg);
            mLogicTraceUtilLte2.setPci(GnbProtocol.CellId.FIRST, pci_cfg);
        }

        //通道八
        arfcn_cfg = "";
        LinkedList<Integer> td8 = null;
        if (arfcnBeanHashMap.get("TD8") != null) {
            td8 = arfcnBeanHashMap.get("TD8").getArfcnList();
            if (td8.size() > 0) {
                arfcn_cfg = String.valueOf(td8.get(0));
                if (!auto_trace_imsi.isEmpty())
                    mLogicTraceUtilLte2.setImsi(GnbProtocol.CellId.SECOND, auto_trace_imsi);
            }

            if (!arfcn_cfg.isEmpty()) {
                int band3 = Integer.parseInt(arfcn_cfg) > 100000 ? NrBand.earfcn2band(Integer.parseInt(arfcn_cfg)) : LteBand.earfcn2band(Integer.parseInt(arfcn_cfg));
                mLogicTraceUtilLte2.setBandWidth(GnbProtocol.CellId.SECOND, 5);
                mLogicTraceUtilLte2.setSsbBitmap(GnbProtocol.CellId.SECOND, 128);
                mLogicTraceUtilLte2.setPlmn(GnbProtocol.CellId.SECOND, band3 == 1 ? "46001" : "46000");
                runCell[7] = true;
            }
            pci_cfg = PrefUtil.build().getValue("PCI_TD8", "504").toString();
            mLogicTraceUtilLte2.setAirSync(GnbProtocol.CellId.SECOND, enableAir);
            mLogicTraceUtilLte2.setArfcn(GnbProtocol.CellId.SECOND, arfcn_cfg);
            mLogicTraceUtilLte2.setPci(GnbProtocol.CellId.SECOND, pci_cfg);
        }
        boolean ifRun = false;
        for (boolean run : runCell) {
            if (run) {
                ifRun = true;
                break;
            }
        }
        if (!ifRun) {
            Util.showToast(getString(R.string.no_cfg));
            mCfgTraceChildFragment.isAutoMode = false;
            return;
        }
        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
            //设置此设备的traceUtil
            TraceUtil traceUtil = DeviceUtil.getTraceUtilByDeviceName(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName());
            MainActivity.getInstance().getDeviceList().get(i).setTraceUtil(traceUtil);

            int logicIndex = DeviceUtil.getLogicIndexByDeviceName(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName());
            if (logicIndex < 2)
                PaCtl.build().closePA(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId());
            else
                PaCtl.build().closeLtePA(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId());

            final List<UeidBean> blackList = new ArrayList<>();
            for (MyUeidBean bean : MainActivity.getInstance().getBlackList())
                blackList.add(bean.getUeidBean());

            AppLog.I("onTraceConfig() 定位启动 = " + i);
            // 第一步，配置黑名单
            if (runCell[2 * logicIndex]) {
                MessageController.build().setBlackList(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId(), false, GnbProtocol.CellId.FIRST,
                        blackList.size(), blackList);
                MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.BLACKLIST);
            }
            if (runCell[2 * logicIndex + 1]) {
                int finalI = i;
                new Handler().postDelayed(() -> {
                    MessageController.build().setBlackList(MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDeviceId(), false, GnbProtocol.CellId.SECOND,
                            blackList.size(), blackList);
                    MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.BLACKLIST);
                }, 500);
            }
        }

        //存入历史记录
        storeAutoInfoIntoHistory(td1, td2, td3, td4, td5, td6, td7, td8);

//        mCfgTraceChildFragment.setcfgEnable(-1, false);
        view_pager.setCurrentItem(1);
        refreshTraceBtn();
        Util.showToast(getString(R.string.cfging_trace));
    }

    //将当前的数据存入历史记录
    private void storeIntoHistory() {
        try {
            JSONArray jsonArray = new JSONArray();

            //通道一
            JSONArray arfcnJsonArray = new JSONArray();
            JSONObject jsonObject1 = new JSONObject();
            if (mCfgTraceChildFragment.getTD1().size() > 0) {
                arfcnJsonArray.put(mCfgTraceChildFragment.getTD1().get(0));
                jsonObject1.put("arfcnJsonArray", arfcnJsonArray);
            } else jsonObject1.put("arfcnJsonArray", "[]");
            jsonObject1.put("pci", mLogicTraceUtilNr.getPci(0));
            jsonObject1.put("imsi", mLogicTraceUtilNr.getImsi(0));
            jsonArray.put(jsonObject1);

            //通道二
            arfcnJsonArray = new JSONArray();
            JSONObject jsonObject2 = new JSONObject();
            if (mCfgTraceChildFragment.getTD2().size() > 0) {
                arfcnJsonArray.put(mCfgTraceChildFragment.getTD2().get(0));
                jsonObject2.put("arfcnJsonArray", arfcnJsonArray);
            } else jsonObject2.put("arfcnJsonArray", "[]");
            jsonObject2.put("pci", mLogicTraceUtilNr.getPci(1));
            jsonObject2.put("imsi", mLogicTraceUtilNr.getImsi(1));
            jsonArray.put(jsonObject2);

            //通道三
            arfcnJsonArray = new JSONArray();
            JSONObject jsonObject3 = new JSONObject();
            if (mCfgTraceChildFragment.getTD3().size() > 0) {
                arfcnJsonArray.put(mCfgTraceChildFragment.getTD3().get(0));
                jsonObject3.put("arfcnJsonArray", arfcnJsonArray);
            } else jsonObject3.put("arfcnJsonArray", "[]");
            jsonObject3.put("pci", mLogicTraceUtilNr2.getPci(0));
            jsonObject3.put("imsi", mLogicTraceUtilNr2.getImsi(0));
            jsonArray.put(jsonObject3);

            //通道四
            arfcnJsonArray = new JSONArray();
            JSONObject jsonObject4 = new JSONObject();
            if (mCfgTraceChildFragment.getTD4().size() > 0) {
                arfcnJsonArray.put(mCfgTraceChildFragment.getTD4().get(0));
                jsonObject4.put("arfcnJsonArray", arfcnJsonArray);
            } else jsonObject4.put("arfcnJsonArray", "[]");
            jsonObject4.put("pci", mLogicTraceUtilNr2.getPci(1));
            jsonObject4.put("imsi", mLogicTraceUtilNr2.getImsi(1));
            jsonArray.put(jsonObject4);

            //通道五
            arfcnJsonArray = new JSONArray();
            JSONObject jsonObject5 = new JSONObject();
            if (mCfgTraceChildFragment.getTD5().size() > 0) {
                arfcnJsonArray.put(mCfgTraceChildFragment.getTD5().get(0));
                jsonObject5.put("arfcnJsonArray", arfcnJsonArray);
            } else jsonObject5.put("arfcnJsonArray", "[]");
            jsonObject5.put("pci", mLogicTraceUtilLte.getPci(0));
            jsonObject5.put("imsi", mLogicTraceUtilLte.getImsi(0));
            jsonArray.put(jsonObject5);

            //通道六
            arfcnJsonArray = new JSONArray();
            JSONObject jsonObject6 = new JSONObject();
            if (mCfgTraceChildFragment.getTD6().size() > 0) {
                arfcnJsonArray.put(mCfgTraceChildFragment.getTD6().get(0));
                jsonObject6.put("arfcnJsonArray", arfcnJsonArray);
            } else jsonObject6.put("arfcnJsonArray", "[]");
            jsonObject6.put("pci", mLogicTraceUtilLte.getPci(1));
            jsonObject6.put("imsi", mLogicTraceUtilLte.getImsi(1));
            jsonArray.put(jsonObject6);

            //通道七
            arfcnJsonArray = new JSONArray();
            JSONObject jsonObject7 = new JSONObject();
            if (mCfgTraceChildFragment.getTD7().size() > 0) {
                arfcnJsonArray.put(mCfgTraceChildFragment.getTD7().get(0));
                jsonObject7.put("arfcnJsonArray", arfcnJsonArray);
            } else jsonObject7.put("arfcnJsonArray", "[]");
            jsonObject7.put("pci", mLogicTraceUtilLte2.getPci(0));
            jsonObject7.put("imsi", mLogicTraceUtilLte2.getImsi(0));
            jsonArray.put(jsonObject7);

            //通道八
            arfcnJsonArray = new JSONArray();
            JSONObject jsonObject8 = new JSONObject();
            if (mCfgTraceChildFragment.getTD8().size() > 0) {
                arfcnJsonArray.put(mCfgTraceChildFragment.getTD8().get(0));
                jsonObject8.put("arfcnJsonArray", arfcnJsonArray);
            } else jsonObject8.put("arfcnJsonArray", "[]");
            jsonObject8.put("pci", mLogicTraceUtilLte2.getPci(1));
            jsonObject8.put("imsi", mLogicTraceUtilLte2.getImsi(1));
            jsonArray.put(jsonObject8);

            //时间
            jsonArray.put(DateUtil.getCurrentTime());

            long result = DBUtil.insertTraceCfgToDB(jsonArray);
            if (result != -1) {
                AppLog.D("storeIntoHistory 存入历史记录成功 ID: " + result);
            } else {
                AppLog.D("storeIntoHistory 存入历史记录失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //将当前的数据存入历史记录
    private void storeAutoInfoIntoHistory
    (LinkedList<Integer> td1, LinkedList<Integer> td2, LinkedList<Integer> td3, LinkedList<Integer> td4,
     LinkedList<Integer> td5, LinkedList<Integer> td6, LinkedList<Integer> td7, LinkedList<Integer> td8) {
        if (td1 != null || td2 != null || td3 != null || td4 != null
                || td5 != null || td6 != null || td7 != null || td8 != null) {
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject1 = createJsonObject(0, td1, 0);
            JSONObject jsonObject2 = createJsonObject(0, td2, 1);
            JSONObject jsonObject3 = createJsonObject(1, td3, 0);
            JSONObject jsonObject4 = createJsonObject(1, td4, 1);
            JSONObject jsonObject5 = createJsonObject(2, td5, 0);
            JSONObject jsonObject6 = createJsonObject(2, td6, 1);
            JSONObject jsonObject7 = createJsonObject(3, td7, 0);
            JSONObject jsonObject8 = createJsonObject(3, td8, 1);
            jsonArray.put(jsonObject1);
            jsonArray.put(jsonObject2);
            jsonArray.put(jsonObject3);
            jsonArray.put(jsonObject4);
            jsonArray.put(jsonObject5);
            jsonArray.put(jsonObject6);
            jsonArray.put(jsonObject7);
            jsonArray.put(jsonObject8);

            //时间
            jsonArray.put(DateUtil.getCurrentTime());

            long result = DBUtil.insertTraceCfgToDB(jsonArray);
            if (result != -1) {
                AppLog.D("storeAutoInfoIntoHistory 存入历史记录成功 ID: " + result);
            } else {
                AppLog.D("storeAutoInfoIntoHistory 存入历史记录失败");
            }
        }
    }

    private JSONObject createJsonObject(int logicIndex, LinkedList<Integer> tdList, int cell_id) {
        try {
            TraceUtil traceUtil = mLogicTraceUtilNr;
            switch (logicIndex) {
                case 0:
                    traceUtil = mLogicTraceUtilNr;
                    break;
                case 1:
                    traceUtil = mLogicTraceUtilNr2;
                    break;
                case 2:
                    traceUtil = mLogicTraceUtilLte;
                    break;
                case 3:
                    traceUtil = mLogicTraceUtilLte2;
                    break;
            }
            //通道一
            JSONArray arfcnJsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            if (tdList != null) {
                for (int i = 0; i < tdList.size(); i++)
                    arfcnJsonArray.put(tdList.get(i));
                jsonObject.put("arfcnJsonArray", arfcnJsonArray);
            } else jsonObject.put("arfcnJsonArray", "[]");
            jsonObject.put("pci", traceUtil.getPci(cell_id));
            jsonObject.put("imsi", traceUtil.getImsi(cell_id));
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void importArfcn(ScanArfcnBean bean, int pci) {
        mCfgTraceChildFragment.importArfcn(bean, -1, pci);
    }

    //待修改：载波频点
    private void startRunWork() {
        AppLog.D("startRunWork");
        boolean isAirSync = !PrefUtil.build().getValue("sync_mode", getString(R.string.air)).toString().equals("GPS");
        autoArfcnIndex0 = 0;
        autoArfcnIndex1 = 0;
        autoArfcnIndex2 = 0;
        autoArfcnIndex3 = 0;
        autoArfcnIndex4 = 0;
        autoArfcnIndex5 = 0;
        autoArfcnIndex6 = 0;
        autoArfcnIndex7 = 0;

        autoRun1 = false;
        autoRun2 = false;
        autoRun3 = false;
        autoRun4 = false;
        autoRun5 = false;
        autoRun6 = false;
        autoRun7 = false;
        autoRun8 = false;

        mHandler.removeCallbacksAndMessages(null);

        txValue = 0;
        mCatchChildFragment.restartCatch();
        isClickStop = false;
        mImsiList.clear();
        mCatchChildFragment.resetShowData(true);
        all_count = 0;
        unicom_count = 0;
        mobile_count = 0;
        telecom_count = 0;
        sva_count = 0;
        boolean notHaveArfcn = true;
        mCatchChildFragment.setCatchCount(all_count, telecom_count, mobile_count, unicom_count, sva_count);
        mTraceChildFragment.resetRsrp();
        if (MainActivity.getInstance().getDeviceList().size() <= 0) {
            Util.showToast(getString(R.string.dev_not_online));
            return;
        }

        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
            String name = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName();
            int logicIndex = DeviceUtil.getLogicIndexByDeviceName(name);

            long timeMillis = System.currentTimeMillis();
            TraceUtil traceUtil = DeviceUtil.getTraceUtilByLogicIndex(logicIndex);

            AppLog.D("startRunWork() logicIndex = " + logicIndex + "traceUtil=" + traceUtil);
            traceUtil.setAirSync(GnbProtocol.CellId.FIRST, isAirSync ? 1 : 0);
            traceUtil.setAirSync(GnbProtocol.CellId.SECOND, isAirSync ? 1 : 0);

            traceUtil.setAtCmdTimeOut(GnbProtocol.CellId.FIRST, timeMillis);
            traceUtil.setAtCmdTimeOut(GnbProtocol.CellId.SECOND, timeMillis);

            MainActivity.getInstance().getDeviceList().get(i).setTraceUtil(traceUtil);

            final List<UeidBean> blackList = new ArrayList<>();
            for (MyUeidBean bean : MainActivity.getInstance().getBlackList()) {
                blackList.add(bean.getUeidBean());
            }
            AppLog.I("startRunWork() 定位启动 = " + i);
            if (traceUtil.getArfcn(GnbProtocol.CellId.FIRST).isEmpty() &&
                    traceUtil.getArfcn(GnbProtocol.CellId.SECOND).isEmpty()
            ||traceUtil.getImsi(GnbProtocol.CellId.FIRST).isEmpty() &&
                    traceUtil.getImsi(GnbProtocol.CellId.SECOND).isEmpty()) {
                AppLog.D("startRunWork():device i =" + i + "logicIndex=" + logicIndex + getString(R.string.no_cfg)
                        + " 通道1: " + traceUtil.getArfcn(GnbProtocol.CellId.FIRST) + " 通道2: " + traceUtil.getArfcn(GnbProtocol.CellId.SECOND));
                if (i == MainActivity.getInstance().getDeviceList().size() - 1) {   //最后一个
                    if (notHaveArfcn) {
                        Util.showToast(getString(R.string.no_cfg));
                        return;
                    }
                }
                continue;
            }
            notHaveArfcn = false;
            //开启前先关闭功放
            if (logicIndex < 2)
                PaCtl.build().closePA(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId());
            else
                PaCtl.build().closeLtePA(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId());

            //待修改：载波频点
            String arfcnFirst = traceUtil.getArfcn(GnbProtocol.CellId.FIRST);
            String imsiFirst = traceUtil.getImsi(GnbProtocol.CellId.FIRST);
            if (!arfcnFirst.isEmpty()&&!imsiFirst.isEmpty()) {
                // 第一步，配置黑名单
                int finalI1 = i;
                new Handler().postDelayed(() -> {
                    traceUtil.setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.BLACKLIST);
                    MessageController.build().setBlackList(MainActivity.getInstance().getDeviceList().get(finalI1).getRsp().getDeviceId(), Integer.parseInt(arfcnFirst) < 100000, GnbProtocol.CellId.FIRST,
                            blackList.size(), blackList);
                }, 2500);
                mCfgTraceChildFragment.setcfgEnable(-1, false);
            }
            String arfcnSecond = traceUtil.getArfcn(GnbProtocol.CellId.SECOND);
            String imsiSecond = traceUtil.getImsi(GnbProtocol.CellId.SECOND);
            if (!traceUtil.getArfcn(GnbProtocol.CellId.SECOND).isEmpty()&&!imsiSecond.isEmpty()) {
                // 第一步，配置黑名单
                int finalI = i;
                new Handler().postDelayed(() -> {
                    MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.BLACKLIST);
                    MessageController.build().setBlackList(MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDeviceId(), Integer.parseInt(arfcnSecond) < 100000, GnbProtocol.CellId.SECOND,
                            blackList.size(), blackList);
                }, 3000);
                mCfgTraceChildFragment.setcfgEnable(-1, false);
            }
        }
        refreshTraceBtn();
        view_pager.setCurrentItem(1);
        Util.showToast(getString(R.string.cfging_trace));

    }

    public int getTime_count() {

        return time_count;
    }

    public void setTime_count(int time_count) {
        this.time_count = time_count;

    }

    public void setIfDebug(boolean b) {
        mCatchChildFragment.setTv_add_testVisibility(b);
        mCfgTraceChildFragment.setbtn_testVisibility(b);

    }
}