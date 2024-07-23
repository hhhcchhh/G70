package com.simdo.g73cs.Fragment;

import static com.simdo.g73cs.MainActivity.devA;
import static com.simdo.g73cs.MainActivity.devB;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.UeidBean;
import com.nr.Gnb.Response.GnbCmdRsp;
import com.nr.Gnb.Response.GnbFreqScanRsp;
import com.nr.Gnb.Response.GnbStateRsp;
import com.nr.Gnb.Response.GnbTraceRsp;
import com.nr.Socket.MessageControl.MessageController;
import com.nr.Util.OpLog;
import com.simdo.g73cs.Adapter.FragmentAdapter;
import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.Bean.DataFwdBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.HistoryBean;
import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.Bean.TraceBean;
import com.simdo.g73cs.Dialog.FreqDialog;
import com.simdo.g73cs.Listener.OnCfgTraceChildListener;
import com.simdo.g73cs.Listener.OnTraceSetListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.CatchImsiDBUtil;
import com.simdo.g73cs.Util.HistoryDBUtil;
import com.simdo.g73cs.Util.DateUtil;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.ParseDataUtil;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.TraceUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.View.BlackListSlideAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TraceCatchFragment extends Fragment implements OnTraceSetListener {

    Context mContext;
    private List<ImsiBean> mImsiList;   //侦码imsi列表
    private BlackListSlideAdapter adapter;
    public TraceUtil mTraceUtilNr;
    public TraceUtil mTraceUtilLte;
    private int time_count = 60;    //轮询时间
//    private int time_count = 30;    //轮询时间

    FreqFragment mFreqFragment;
    //是否频点轮询
    private boolean isCell1SetArfcnChange, isCell2SetArfcnChange, isCell3SetArfcnChange, isCell4SetArfcnChange;
    //下一个要轮询到的频点索引
    int autoArfcnIndex0 = 0, autoArfcnIndex1 = 0, autoArfcnIndex2 = 0, autoArfcnIndex3 = 0;
    boolean isClickStop = false;
    //设置是否要在结束时接着运行（切换的频点不在同一个频段的时候、侦码列表切换的时候）
    boolean restart1 = false, restart2 = false, restart3 = false, restart4 = false;

    FreqDialog mFreqDialog;
    private String auto_trace_imsi;
    DataFwdBean dataFwdBean = new DataFwdBean();
    OnCfgTraceChildListener onCfgTraceChildListener;
    private int notifyPosition = 0; //帧码列表需要更新的索引

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
    public final HashMap<String, LinkedList<ArfcnPciBean>> arfcnBeanHashMap = new HashMap<>();

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
//        new Handler().postDelayed(() -> {
        view_pager.setCurrentItem(0);
//        }, 200);
        TabLayout tab_layout = root.findViewById(R.id.tab_trace_catch);

//        setTabWidthSame(tab_layout);

        TabLayoutMediator tab = new TabLayoutMediator(tab_layout, view_pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(titles[position]);
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
        mCatchChildFragment.setCatchCount(all_count, telecom_count, mobile_count, unicom_count, sva_count);
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
        mTraceUtilNr = new TraceUtil();
        // 初始化定位参数
        mTraceUtilNr.setTacChange(GnbProtocol.CellId.FIRST, false);
        mTraceUtilNr.setTxPwr(GnbProtocol.CellId.FIRST, 0);
        mTraceUtilNr.setCfr(GnbProtocol.CellId.FIRST, 1);
        mTraceUtilNr.setUeMaxTxpwr(GnbProtocol.CellId.FIRST, "10");
        mTraceUtilNr.setCid(GnbProtocol.CellId.FIRST, 65536);
        mTraceUtilNr.setSplit_arfcn_dl(GnbProtocol.CellId.FIRST, "0");

        mTraceUtilNr.setTacChange(GnbProtocol.CellId.SECOND, false);
        mTraceUtilNr.setTxPwr(GnbProtocol.CellId.SECOND, 0);
        mTraceUtilNr.setCfr(GnbProtocol.CellId.SECOND, 1);
        mTraceUtilNr.setUeMaxTxpwr(GnbProtocol.CellId.SECOND, "10");
        mTraceUtilNr.setCid(GnbProtocol.CellId.SECOND, 65537);
        mTraceUtilNr.setSplit_arfcn_dl(GnbProtocol.CellId.SECOND, "0");

        mTraceUtilNr.setTacChange(GnbProtocol.CellId.THIRD, false);
        mTraceUtilNr.setTxPwr(GnbProtocol.CellId.THIRD, 0);
        mTraceUtilNr.setCfr(GnbProtocol.CellId.THIRD, 1);
        mTraceUtilNr.setUeMaxTxpwr(GnbProtocol.CellId.THIRD, "10");
        mTraceUtilNr.setCid(GnbProtocol.CellId.THIRD, 65538);
        mTraceUtilNr.setSplit_arfcn_dl(GnbProtocol.CellId.THIRD, "0");

        mTraceUtilNr.setTacChange(GnbProtocol.CellId.FOURTH, false);
        mTraceUtilNr.setTxPwr(GnbProtocol.CellId.FOURTH, 0);
        mTraceUtilNr.setCfr(GnbProtocol.CellId.FOURTH, 1);
        mTraceUtilNr.setUeMaxTxpwr(GnbProtocol.CellId.FOURTH, "10");
        mTraceUtilNr.setCid(GnbProtocol.CellId.FOURTH, 65539);
        mTraceUtilNr.setSplit_arfcn_dl(GnbProtocol.CellId.FOURTH, "0");

        /*mTraceUtilLte.setTacChange(GnbProtocol.CellId.FIRST, false);
        mTraceUtilLte.setTxPwr(GnbProtocol.CellId.FIRST, 0);
        mTraceUtilLte.setCfr(GnbProtocol.CellId.FIRST, 1);
        mTraceUtilLte.setUeMaxTxpwr(GnbProtocol.CellId.FIRST, "10");
        mTraceUtilLte.setCid(GnbProtocol.CellId.FIRST, 65538);
        mTraceUtilLte.setSplit_arfcn_dl(GnbProtocol.CellId.FIRST, "0");

        mTraceUtilLte.setTacChange(GnbProtocol.CellId.SECOND, false);
        mTraceUtilLte.setTxPwr(GnbProtocol.CellId.SECOND, 0);
        mTraceUtilLte.setCfr(GnbProtocol.CellId.SECOND, 1);
        mTraceUtilLte.setUeMaxTxpwr(GnbProtocol.CellId.SECOND, "10");
        mTraceUtilLte.setCid(GnbProtocol.CellId.SECOND, 65539);
        mTraceUtilLte.setSplit_arfcn_dl(GnbProtocol.CellId.SECOND, "0");

        mTraceUtilLte.setTacChange(GnbProtocol.CellId.THIRD, false);
        mTraceUtilLte.setTxPwr(GnbProtocol.CellId.THIRD, 0);
        mTraceUtilLte.setCfr(GnbProtocol.CellId.THIRD, 1);
        mTraceUtilLte.setUeMaxTxpwr(GnbProtocol.CellId.THIRD, "10");
        mTraceUtilLte.setCid(GnbProtocol.CellId.THIRD, 65536);
        mTraceUtilLte.setSplit_arfcn_dl(GnbProtocol.CellId.THIRD, "0");

        mTraceUtilLte.setTacChange(GnbProtocol.CellId.FOURTH, false);
        mTraceUtilLte.setTxPwr(GnbProtocol.CellId.FOURTH, 0);
        mTraceUtilLte.setCfr(GnbProtocol.CellId.FOURTH, 1);
        mTraceUtilLte.setUeMaxTxpwr(GnbProtocol.CellId.FOURTH, "10");
        mTraceUtilLte.setCid(GnbProtocol.CellId.FOURTH, 65537);
        mTraceUtilLte.setSplit_arfcn_dl(GnbProtocol.CellId.FOURTH, "0");*/

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
        AppLog.I("TraceFragment doSetTxPwrOffset value = " + value);

//        if (txValue == value || txValue == -1) {
//            return;
//        }
//        txValue = value;
//        AppLog.I("TraceFragment rg_rx_gain rxValue = " + value);
        for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
            TraceUtil traceUtil = bean.getTraceUtil();
            String id = bean.getRsp().getDeviceId();
            AppLog.I("TraceFragment bean.getWorkState() = " + bean.getWorkState());

            String first_arfcn = traceUtil.getArfcn(GnbProtocol.CellId.FIRST);
            String second_arfcn = traceUtil.getArfcn(GnbProtocol.CellId.SECOND);
            String third_arfcn = traceUtil.getArfcn(GnbProtocol.CellId.THIRD);
            String fourth_arfcn = traceUtil.getArfcn(GnbProtocol.CellId.FOURTH);
            AppLog.D("MessageController.build().getTraceType(id) == GnbProtocol.TraceType.TRACE first_arfcn" + first_arfcn
                    + " second_arfcn" + second_arfcn + "third_arfcn" + third_arfcn + "fourth_arfcn" + fourth_arfcn);

            if (!first_arfcn.isEmpty())
                MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.FIRST, Integer.parseInt(first_arfcn), value);
            if (!second_arfcn.isEmpty())
                MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.SECOND, Integer.parseInt(second_arfcn), value);
            if (!third_arfcn.isEmpty())
                MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.THIRD, Integer.parseInt(third_arfcn), value);
            if (!fourth_arfcn.isEmpty())
                MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.FOURTH, Integer.parseInt(fourth_arfcn), value);

//                if (bean.getRsp().getDualCell() == 2) {
//                    String arfcn = traceUtil.getArfcn(GnbProtocol.CellId.SECOND);
//                    if (!arfcn.isEmpty()) {
//                        new Handler().postDelayed(() ->
//                                MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.SECOND, Integer.parseInt(arfcn), value), 300);
//                    }
//                }

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
            Double vol = deviceList.get(0).getRsp().getVoltageList().get(1);
            if (vol < 1) {
            } else if (vol < 16.6) {
                MainActivity.getInstance().showRemindDialog(getString(R.string.vol_min_title), getString(R.string.vol_min_tip));
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
            AppLog.I("doWork() 开启定位 sb = " + sb);
            String doStr = sb.toString();

            if (doStr.isEmpty() || doStr.equals("-1")) {
                Util.showToast(getString(R.string.dev_busy_please_wait));
                return;
            }
            if (doStr.equals("-1-1")) {
                Util.showToast(getString(R.string.dev_offline));
                return;
            }
            ParseDataUtil.build().initData();
            if (doStr.equals("00")) {
                startTrace("", auto_trace_imsi);  // 两设备均在空闲状态下，启动定位
                return;
            }
            if (doStr.contains("0")) {
                startTrace(idleDev, auto_trace_imsi); // 单设备在空闲状态下，启动定位
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

    public void setProgress(int type, int pro, int cell_id, String info, boolean isFail) {
        MainActivity.getInstance().updateProgress(type, pro, cell_id, info, isFail);
    }

    public void showBlackListDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_blacklist_silde, null);

        RecyclerView black_list = view.findViewById(R.id.black_list);
        black_list.setLayoutManager(new LinearLayoutManager(mContext));

        adapter = new BlackListSlideAdapter(mContext, MainActivity.getInstance().getBlackList(), new BlackListSlideAdapter.ListItemListener() {
            @Override
            public void onItemClickListener(int pos, MyUeidBean bean) {
                showChangeTraceImsi(bean, true, pos, null);
            }

            @Override
            public void onItemDeleteListener(int pos, MyUeidBean bean) {
                if (mCatchChildFragment != null) {    //侦码列表
                    mCatchChildFragment.updateImsiListToOLD(bean.getUeidBean().getImsi());
                }
            }
        });
        black_list.setAdapter(adapter);
        view.findViewById(R.id.tv_add).setOnClickListener(view1 -> showBlackListCfgDialog(true, -1));
        view.findViewById(R.id.back).setOnClickListener(view12 -> {
            MainActivity.getInstance().closeCustomDialog();
//            mCfgTraceChildFragment.restoreFocus();
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
                    if (MainActivity.getInstance().getBlackList().size() >= 10) {
                        Util.showToast(getString(R.string.imsi_max_tip));
                        return;
                    } else {
                        MainActivity.getInstance().getBlackList().add(bean);
                    }
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
            mCfgTraceChildFragment.addDropImsi(imsi);
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
                    MyUeidBean bean = MainActivity.getInstance().getBlackList().get(position);
                    if (mCatchChildFragment != null) {    //侦码列表
                        mCatchChildFragment.updateImsiListToOLD(bean.getUeidBean().getImsi());
                    }
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

    boolean isChangeImsi = false;

    public void showChangeTraceImsi(MyUeidBean bean, boolean isInBlackList, int pos, ImsiBean imsiBean) {
        AppLog.D("showChangeTraceImsi bean" + bean.toString());
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView title = view.findViewById(R.id.title);
        String imsi = bean.getUeidBean().getImsi();
        title.setText("请选择对目标" + imsi.substring(imsi.length() - 4, imsi.length()) + "的操作");

        //定位
        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setText("发至定位");
        btn_ok.setOnClickListener(v -> {
            //修改配置页的imsi
            mCfgTraceChildFragment.setActv_imsi(bean.getUeidBean().getImsi());

            for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                DeviceInfoBean deviceInfoBean = MainActivity.getInstance().getDeviceList().get(i);
                int workState = deviceInfoBean.getWorkState();
                int type = deviceInfoBean.getRsp().getDevName().contains(devA) ? 0 : 1;
                if (workState == GnbBean.State.CATCH || workState == GnbBean.State.TRACE) {
                    //正在开启中判断是否imsi的运营商一致，不一致不给开
                    String tracePlmn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getPlmn(0);    //暂时只获取小区1的imsi
                    String plmn = imsi2Plmn(imsi);
                    if (!isSameOperator(plmn, tracePlmn)) {
                        Util.showToast("选中目标与定位目标运营商冲突，请重新选择");
                        MainActivity.getInstance().closeCustomDialog();
                        return;
                    }
                }


                boolean canAdd = true;
                for (MyUeidBean bean1 : MainActivity.getInstance().getBlackList()) {
                    if (bean1.getUeidBean().getImsi().equals(imsi)) {
                        canAdd = false;
                        break;
                    }
                }
                if (canAdd) {   //侦码列表如果没有在黑名单中添加到黑名单
                    MyUeidBean bean2 = new MyUeidBean(bean.getName(), new UeidBean(imsi, imsi), false, false);
                    if (MainActivity.getInstance().getBlackList().size() >= 10) {
                        Util.showToast("黑名单已达上限，请手动删改黑名单后重试");
                        return;
                    } else {
                        MainActivity.getInstance().getBlackList().add(bean);
                    }
                }

                if (workState == GnbBean.State.CATCH || workState == GnbBean.State.TRACE) {

                    String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();

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
                        AppLog.D("cell_id == -1");
                        TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil();
                        String arfcn = "";
                        String pci = "";
                        int cellID = -1;

                        if (canAdd) {   //如果当前imsi没有在黑名单中就结束重发
                            do {
                                cellID++;
                                if (traceUtil.getWorkState(cellID) == GnbBean.State.TRACE) {
                                    arfcn = traceUtil.getArfcn(cellID);
                                    pci = traceUtil.getPci(cellID);
                                    if (!arfcn.isEmpty()) {
                                        MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_TRACE, cellID);
                                        PaCtl.build().closePAByArfcn(id, false, mTraceUtilNr.getArfcn(cellID));
                                        MainActivity.getInstance().updateProgress(0, 50, cellID, getString(R.string.changeing), false);
                                        MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setImsi(cellID, imsi);
                                        Util.showToast(getString(R.string.change_trace_success));
                                        if (cellID == 0) restart1 = true;
                                        else if (cellID == 1) restart2 = true;
                                        else if (cellID == 2) restart3 = true;
                                        else if (cellID == 3) restart4 = true;
                                    }
                                }
                                if (cellID == 3) break;
                            } while (cellID < 4);
                        } else {    //在黑名单中直接改变imsi
                            do {
                                cellID++;
                                if (traceUtil.getWorkState(cellID) == GnbBean.State.TRACE) {
                                    arfcn = traceUtil.getArfcn(cellID);
                                    pci = traceUtil.getPci(cellID);
                                }
                                if (cellID == 3) break;
                            } while (arfcn.isEmpty());
                            if (!arfcn.isEmpty()) {
                                setChangeImsi(cellID, arfcn, imsi);
                            }
                        }
                    } else {
                        String info = workState == GnbBean.State.TRACE ? getString(R.string.cell) + getCellStr(cell_id) + getString(R.string.change_imsi) : getString(R.string.cell) + getCellStr(cell_id) + getString(R.string.change_work_to_trace);

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setImsi(cell_id, bean.getUeidBean().getImsi());
                        if (Integer.parseInt(deviceInfoBean.getTraceUtil().getArfcn(0)) > 100000)
                            MessageController.build().startTrace(id, cell_id, imsi, 1);
                        else MessageController.build().startLteTrace(id, cell_id, imsi, 1);
                        isChangeImsi = true;
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, info);
                        MainActivity.getInstance().updateProgress(type, 50, cell_id, getString(R.string.changeing), false);
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

        TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        if (isInBlackList) btn_cancel.setVisibility(View.GONE);
        btn_cancel.setText("仅添加黑名单");
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            if (MainActivity.getInstance().getBlackList().size() >= 10) {
                Util.showToast(getString(R.string.imsi_max_tip));
                MainActivity.getInstance().closeCustomDialog();
                return;
            } else {
                MainActivity.getInstance().getBlackList().add(bean);
                if (!isInBlackList && imsiBean != null) {    //侦码列表
                    mCatchChildFragment.updateImsiListToBL(pos, imsiBean);
                }
                Util.showToast("添加成功");
                MainActivity.getInstance().closeCustomDialog();
            }
        });


        final TextView tv_back = view.findViewById(R.id.tv_back);
        tv_back.setVisibility(View.VISIBLE);
        tv_back.setOnClickListener(v -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    private boolean isSameOperator(String plmn, String plmnFromArfcn) {
        if (plmn.equals(plmnFromArfcn)) {
            return true;
        }
        if (plmn.equals("46000") && plmnFromArfcn.equals("46015") ||
                plmn.equals("46015") && plmnFromArfcn.equals("46000") ||
                plmn.equals("46001") && plmnFromArfcn.equals("46011") ||
                plmn.equals("46011") && plmnFromArfcn.equals("46001")) {
            return true;
        }
        return false;

    }

    private String imsi2Plmn(String imsi) {
        String tracePlmn = imsi.substring(0, 5);
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
        }
        return tracePlmn;
    }

    public void setChangeImsi(int cell_id, String arfcn, String imsi) {
        String s = getCellStr(cell_id);
        String id = MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId();

        MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.cell) + s + getString(R.string.change_imsi));
        MainActivity.getInstance().updateProgress(0, 50, cell_id, getString(R.string.changeing), false);
        MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setImsi(cell_id, imsi);
        isChangeImsi = true;
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
            mFreqDialog = new FreqDialog(mContext, this, mCfgTraceChildFragment, tracePlmn);
            mFreqDialog.setOnTraceSetListener(this);
            mFreqDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!mFreqDialog.isUserCancel()) {
                        txValue = 0;
                        isClickStop = false;
                        mImsiList.clear();
                        mCatchChildFragment.resetShowData(true);
                        arfcnBeanHashMap.put("TD1", mFreqDialog.getTD1());
                        arfcnBeanHashMap.put("TD2", mFreqDialog.getTD2());
                        arfcnBeanHashMap.put("TD3", mFreqDialog.getTD3());
                        arfcnBeanHashMap.put("TD4", mFreqDialog.getTD4());

                        int airSync = PrefUtil.build().getValue("sync_mode", "Air").toString().equals("Air") ? 1 : 0;

                        mTraceUtilNr.setAirSync(GnbProtocol.CellId.FIRST, airSync);
                        mTraceUtilNr.setAirSync(GnbProtocol.CellId.SECOND, airSync);
                        mTraceUtilNr.setAirSync(GnbProtocol.CellId.THIRD, airSync);
                        mTraceUtilNr.setAirSync(GnbProtocol.CellId.FOURTH, airSync);
                        AtomicInteger delay = new AtomicInteger();
                        String id = MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId();
                        PaCtl.build().closePA(id);
                        if (mFreqDialog.getTD1().size() > 0) {
                            runCell0(id);
                            delay.addAndGet(500);
                        }
                        if (mFreqDialog.getTD2().size() > 0) {
                            int isSecondB40 = 0;
                            if (PaCtl.build().isB97502) {
                                ArfcnPciBean bean = arfcnBeanHashMap.get("TD2").get(0);
                                if (bean.getArfcn().isEmpty()) isSecondB40 = -1;
                                else if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 40)
                                    isSecondB40 = 1;
                            }
                            if (isSecondB40 != 1) {      //直接执行
                                runCell1(id, 500);
                                delay.addAndGet(500);
                            }
                            if (isSecondB40 == 1) {    //次B40等待信号
                                // 在子线程中执行等待信号的逻辑
                                new Thread(() -> {
                                    synchronized (lockB40) { // 使用共享的锁对象
                                        try {
                                            AppLog.D("SecondB40 is waiting");
                                            lockB40.wait(); // 子线程等待信号
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    // 收到信号后，在主线程执行任务
                                    mainHandler.post(() -> {
                                        runCell1(id, 500);  //确保通道四完成之后再执行
                                        delay.addAndGet(500);
                                    });
                                }).start();
                            }
                        }
                        if (mFreqDialog.getTD3().size() > 0) {
                            runCell2(id, delay.get());
                            delay.addAndGet(500);
                        }
                        if (mFreqDialog.getTD4().size() > 0) {
                            int isSecondB3 = 0;
                            if (PaCtl.build().isB97502) {
                                ArfcnPciBean bean = arfcnBeanHashMap.get("TD2").get(0);
                                if (bean.getArfcn().isEmpty()) isSecondB3 = -1;
                                else if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 40)
                                    isSecondB3 = 1;
                            }
                            if (isSecondB3 != 1) {      //直接执行
                                runCell3(id, delay.get());
                                delay.addAndGet(500);
                            }
                            if (isSecondB3 == 1) {    //次B40等待信号
                                // 在子线程中执行等待信号的逻辑
                                new Thread(() -> {
                                    synchronized (lockB3) { // 使用共享的锁对象
                                        try {
                                            AppLog.D("SecondB40 is waiting");
                                            lockB3.wait(); // 子线程等待信号
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    // 收到信号后，在主线程执行任务
                                    mainHandler.post(() -> {
                                        runCell3(id, 500);
                                        delay.addAndGet(500);
                                    });
                                }).start();
                            }
                        }
                    }
                    mFreqDialog = null;
                }
            });
            mFreqDialog.show();
            mCfgTraceChildFragment.setAutoModeFreqRunning(true);

            mTraceUtilNr.setImsi(0, auto_trace_imsi);
            mTraceUtilNr.setImsi(1, auto_trace_imsi);
            mTraceUtilNr.setImsi(2, auto_trace_imsi);
            mTraceUtilNr.setImsi(3, auto_trace_imsi);

            mTraceUtilNr.setPlmn(0, tracePlmn);
            mTraceUtilNr.setPlmn(1, tracePlmn);
            mTraceUtilNr.setPlmn(2, tracePlmn);
            mTraceUtilNr.setPlmn(3, tracePlmn);
            return;
        }

        mCfgTraceChildFragment.setChange1(false);
        mCfgTraceChildFragment.setChange2(false);
        mCfgTraceChildFragment.setChange3(false);
        mCfgTraceChildFragment.setChange4(false);

        //更新map的值
        /*MyRecyclerviewAdapter TD1_adapter, TD2_adapter, TD3_adapter, TD4_adapter;
        TD1_adapter = mCfgTraceChildFragment.getTD1_adapter();
        TD2_adapter = mCfgTraceChildFragment.getTD2_adapter();
        TD3_adapter = mCfgTraceChildFragment.getTD3_adapter();
        TD4_adapter = mCfgTraceChildFragment.getTD4_adapter();
        LinkedList<ArfcnPciBean> TD1, TD2, TD3, TD4;
        TD1 = mCfgTraceChildFragment.getTD1();
        TD2 = mCfgTraceChildFragment.getTD2();
        TD3 = mCfgTraceChildFragment.getTD3();
        TD4 = mCfgTraceChildFragment.getTD4();
        TD1.clear();
        TD2.clear();
        TD3.clear();
        TD4.clear();
        //刷新当前页面的数据
        List<String> arfcnList1 = TD1_adapter.getArfcnList();
        List<String> arfcnList2 = TD2_adapter.getArfcnList();
        List<String> arfcnList3 = TD3_adapter.getArfcnList();
        List<String> arfcnList4 = TD4_adapter.getArfcnList();

        for (String list : arfcnList1) {
            if (list.equals("")) {  //list内容是空且lastEditText内有值
                if (TD1_adapter.getLastViewText() != null && !TD1_adapter.getLastViewText().equals(""))
                    list = TD1_adapter.getLastViewText();
                else continue;
            }
            TD1.add(Integer.parseInt(list));
        }
        for (String list : arfcnList2) {
            if (list.equals("")) {  //list内容是空且lastEditText内有值
                if (TD2_adapter.getLastViewText() != null && !TD2_adapter.getLastViewText().equals(""))
                    list = TD2_adapter.getLastViewText();
                else continue;
            }
            TD2.add(Integer.parseInt(list));
        }
        for (String list : arfcnList3) {
            if (list.equals("")) {  //list内容是空且lastEditText内有值
                if (TD3_adapter.getLastViewText() != null && !TD3_adapter.getLastViewText().equals(""))
                    list = TD3_adapter.getLastViewText();
                else continue;
            }
            TD3.add(Integer.parseInt(list));
        }
        for (String list : arfcnList4) {
            if (list.equals("")) {  //list内容是空且lastEditText内有值
                if (TD4_adapter.getLastViewText() != null && !TD4_adapter.getLastViewText().equals(""))
                    list = TD4_adapter.getLastViewText();
                else continue;
            }
            TD4.add(Integer.parseInt(list));
        }*/

        isChangeImsi = false;
        if (mCfgTraceChildFragment.checkLastParam()) {  //检查没问题
            if (mCfgTraceChildFragment.checkImsiArfcnParam()) {   //检查imsi和频点的关系
                if (MainActivity.getInstance().getDeviceList().get(0).getWorkState() == GnbBean.State.FREQ_SCAN) {
                    //停止扫频开始定位
                    for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                        if (MainActivity.getInstance().getDeviceList().get(i).getWorkState() == GnbBean.State.FREQ_SCAN) {
                            String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
                            MessageController.build().setOnlyCmd(id, GnbProtocol.OAM_MSG_STOP_FREQ_SCAN);
                            int finalI = i;
                            new Handler().postDelayed(() -> {
                                if (MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDevName().contains(devA))
                                    PaCtl.build().closePA(id);
                                else PaCtl.build().closeLtePA(id);
                            }, 300);
                        }
                    }

                    //刷新页面
                    mCfgTraceChildFragment.setcfgEnable(-1, false);
                    refreshTraceBtn();
                    Util.showToast("正在停止扫频， 请稍等", Toast.LENGTH_LONG);

                    new Handler().postDelayed(() -> {
                        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                            if (MainActivity.getInstance().getDeviceList().get(i).getWorkState() == GnbBean.State.FREQ_SCAN) {
                                MainActivity.getInstance().getDeviceList().get(i).setWorkState(GnbBean.State.TRACE);
                                int type = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName().contains(devA) ? 0 : 1;
                                MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.freq_stoped));
                            }
                        }
                        mFreqFragment.setFreqWorkState(false); // 避免命令下发不响应，这里也做一次清除状态
                        startRunWork(); //开始定位
                    }, 8000);
                } else {
                    //刷新页面
                    mCfgTraceChildFragment.setcfgEnable(-1, false);
                    HistoryBean historyBean = mCfgTraceChildFragment.getHistoryBean();
                    mTraceUtilNr.setImsi(0, historyBean.getImsiFirst());
                    mTraceUtilNr.setImsi(1, historyBean.getImsiSecond());
                    mTraceUtilNr.setImsi(2, historyBean.getImsiThird());
                    mTraceUtilNr.setImsi(3, historyBean.getImsiFourth());
                    arfcnBeanHashMap.put("TD1", historyBean.getTD1());
                    arfcnBeanHashMap.put("TD2", historyBean.getTD2());
                    arfcnBeanHashMap.put("TD3", historyBean.getTD3());
                    arfcnBeanHashMap.put("TD4", historyBean.getTD4());


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

    public void stopTraceDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> {
            AppLog.D("stopTraceDialog click stop");
            isClickStop = true;
            txValue = -1;
            isChangeImsi = false;
            restart1 = false;
            restart2 = false;
            restart3 = false;
            restart4 = false;
            mCfgTraceChildFragment.isAutoMode = false;
            mCfgTraceChildFragment.isAutoModeFreqRunning = false;

            for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                mCfgTraceChildFragment.getTv_do_btn().setText(getString(R.string.stoping));
                int type = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName().contains(devA) ? 0 : 1;
                final String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();

                if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().isEnable(GnbProtocol.CellId.FOURTH) &&
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.FOURTH) == GnbBean.State.TRACE) {
                    String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(GnbProtocol.CellId.FOURTH);
                    if (!arfcn.isEmpty()) {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell3_stoping));
                        setProgress(type, 50, 3, getString(R.string.stoping), false);

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FOURTH, System.currentTimeMillis());
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.STOP);

                        MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FOURTH);
                    }
                } else {
                    if (MainActivity.getInstance().getDeviceList().get(i).getRsp().getFourthState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell3_stoping));
                        setProgress(type, 50, 3, getString(R.string.stoping), false);

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FOURTH, System.currentTimeMillis());
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.STOP);
                        MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FOURTH);
                    } else if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.FOURTH) == GnbBean.State.GNB_CFG) {
                        setProgress(type, 50, 3, "结束中", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.STOP);
                    } else {
                        setProgress(type, 0, 3, "空闲", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.IDLE);
                    }
                }

                if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().isEnable(GnbProtocol.CellId.THIRD) &&
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.THIRD) == GnbBean.State.TRACE) {
                    String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(GnbProtocol.CellId.THIRD);
                    if (!arfcn.isEmpty()) {
                        int finalI = i;
                        new Handler().postDelayed(() -> {
                            MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell2_stoping));
                            setProgress(type, 50, 2, getString(R.string.stoping), false);

                            MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.THIRD, System.currentTimeMillis());
                            MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.STOP);

                            MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.THIRD);
                        }, 300);
                    }
                } else {
                    if (MainActivity.getInstance().getDeviceList().get(i).getRsp().getThirdState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell2_stoping));
                        setProgress(type, 50, 2, getString(R.string.stoping), false);

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.THIRD, System.currentTimeMillis());
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.STOP);
                        new Handler().postDelayed(() -> MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.THIRD), 300);

                    } else if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.THIRD) == GnbBean.State.GNB_CFG) {
                        setProgress(type, 50, 2, "结束中", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.STOP);
                    } else {
                        setProgress(type, 0, 2, "空闲", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.IDLE);
                    }
                }

                if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().isEnable(GnbProtocol.CellId.SECOND) &&
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.TRACE) {
                    String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND);
                    if (!arfcn.isEmpty()) {
                        int finalI = i;
                        new Handler().postDelayed(() -> {
                            MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell1_stoping));
                            setProgress(type, 50, 1, getString(R.string.stoping), false);

                            MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.SECOND, System.currentTimeMillis());
                            MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP);

                            MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.SECOND);
                            refreshTraceBtn();
                        }, 600);
                    }
                } else {
                    if (MainActivity.getInstance().getDeviceList().get(i).getRsp().getSecondState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell1_stoping));
                        setProgress(type, 50, 1, getString(R.string.stoping), false);

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.SECOND, System.currentTimeMillis());
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP);
                        new Handler().postDelayed(() -> MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.SECOND), 600);

                    } else if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.GNB_CFG) {
                        setProgress(type, 50, 1, "结束中", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP);
                    } else {
                        setProgress(type, 0, 1, "空闲", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.IDLE);
                    }
                }

                if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().isEnable(GnbProtocol.CellId.FIRST) &&
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST) == GnbBean.State.TRACE) {
                    String arfcn = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getArfcn(GnbProtocol.CellId.FIRST);
                    if (!arfcn.isEmpty()) {
                        int finalI = i;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell0_stoping));
                                setProgress(type, 50, 0, getString(R.string.stoping), false);

                                MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FIRST, System.currentTimeMillis());
                                MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP);
                                MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FIRST);
                                refreshTraceBtn();
                            }
                        }, 900);
                    }
                } else {
                    if (MainActivity.getInstance().getDeviceList().get(i).getRsp().getFirstState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell0_stoping));
                        setProgress(type, 50, 0, getString(R.string.stoping), false);

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FIRST, System.currentTimeMillis());
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.FIRST);
                            }
                        }, 900);

                    } else if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST) == GnbBean.State.GNB_CFG) {
                        setProgress(type, 50, 0, "结束中", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP);
                    } else {
                        setProgress(type, 0, 0, "空闲", false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.IDLE);
                    }
                }
//                    iv_add_remove_black.setVisibility(View.GONE);
                refreshTraceBtn();
                PaCtl.build().closePA(id);
            }

            mCfgTraceChildFragment.resetArfcnList();
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

//    private void startSecondTrace(String id, int indexById, int cell_id, boolean isFail) {
//        AppLog.D("startSecondTrace id=" + id + " indexById=" + indexById + " cell_id=" + cell_id + " isFail=" + isFail);
//        if (!isChangeImsi) {
//            final List<UeidBean> blackList = new ArrayList<>();
//            for (MyUeidBean bean : MainActivity.getInstance().getBlackList())
//                blackList.add(bean.getUeidBean());
//            if (cell_id == GnbProtocol.CellId.FIRST) {
//                if (isFail) { // 通道一定位失败，开始通道三的定位流程  G758暂时限制依赖关系
//                    String arfcn = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getArfcn(GnbProtocol.CellId.THIRD);
//                    if (!arfcn.isEmpty() &&
//                            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getWorkState(GnbProtocol.CellId.THIRD) != GnbBean.State.TRACE)
//                        MessageController.build().setBlackList(id, Integer.parseInt(arfcn) < 100000, GnbProtocol.CellId.THIRD, blackList.size(), blackList);
//                } else { // 通道一定位成功，开始通道二的定位流程
//                    String arfcn = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND);
//                    if (arfcn.isEmpty()) {
//                        arfcn = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getArfcn(GnbProtocol.CellId.THIRD);
//                        if (!arfcn.isEmpty())
//                            MessageController.build().setBlackList(id, Integer.parseInt(arfcn) < 100000, GnbProtocol.CellId.THIRD, blackList.size(), blackList);
//                    } else
//                        MessageController.build().setBlackList(id, Integer.parseInt(arfcn) < 100000, GnbProtocol.CellId.SECOND, blackList.size(), blackList);
//                }
//            } else if (cell_id == GnbProtocol.CellId.SECOND) {       // 通道一定位成功或者失败，开始通道三的定位流程
//                String arfcn = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getArfcn(GnbProtocol.CellId.THIRD);
//                if (!arfcn.isEmpty() &&
//                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getWorkState(GnbProtocol.CellId.THIRD) != GnbBean.State.TRACE)
//                    MessageController.build().setBlackList(id, Integer.parseInt(arfcn) < 100000, GnbProtocol.CellId.THIRD, blackList.size(), blackList);
//            } else if (cell_id == GnbProtocol.CellId.THIRD) {       //通道一定位失败，开始通道二的定位流程
//                if (isFail) {
//                    // 通道三定位失败，不开通道四的定位流程  G758暂时限制依赖关系
//                } else {
//                    String arfcn = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getArfcn(GnbProtocol.CellId.FOURTH);
//                    if (!arfcn.isEmpty())
//                        MessageController.build().setBlackList(id, Integer.parseInt(arfcn) < 100000, GnbProtocol.CellId.FOURTH, blackList.size(), blackList);
//                }
//            }
//        }
//    }

    public void onSetBlackListRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;

        int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains(devA) ? 0 : 1;
        final TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil();
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_SET_BLACK_UE_LIST || rsp.getCmdType() == GnbProtocol.UI_2_gNB_START_LTE_BLACK_UE_LIST) {
                if (isClickStop) {
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    setProgress(type, 0, rsp.getCellId(), "空闲", false);
                    refreshTraceBtn();
                    return;
                }
//                if (isResetBlack) return;
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
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.GNB_CFG);

                    setProgress(type, 30, rsp.getCellId(), getString(R.string.cfging), false);
                    refreshTraceBtn();
                    if (Integer.parseInt(arfcn[0]) > 100000) PaCtl.build().initPA(id, arfcn[0]);
                    else PaCtl.build().initLtePA(id, arfcn[0], rsp.getCellId());

                    /*initGnbTrace(int cell_id, int startTac, int maxTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                    	int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                      int band_width, int cfr_enable, int swap_rf)
                   目标配置频点参数
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
                        //空口模式时偏为-1强制走空口
                        if (PrefUtil.build().getValue("sync_mode", "Air").toString().equals("Air"))
                            time_offset = -1;
//                        int time_offset = Integer.parseInt(PrefUtil.build().getValue("sync_time_offset", "0").toString());
                        //i4 调度周期 1 40ms
                        if (iArfcn > 100000) {
                            MessageController.build().initGnbTrace(id, rsp.getCellId(), finalTraceTac, finalMaxTac, plmn, arfcn[0], pci[0], ue_max_pwr,
                                    time_offset, 1, air_sync, "0", 9,
                                    cid, ssbBitmap, bandwidth, cfr, swap_rf, 15, -70, mob_reject_code, split_arfcn_dl);
                        } else {
//                            int band = LteBand.earfcn2band(Integer.parseInt(arfcn[0]));
//                            if (MainActivity.getInstance().getDeviceList().get(0).getRsp().getGpsSyncState() == GnbStateRsp.Gps.SUCC && band >= 33 && band <= 46) {
//                                time_offset = 10000000;
//                            }

                            MessageController.build().initGnbLteTrace(id, rsp.getCellId(), finalTraceTac, finalMaxTac, plmn, arfcn[0], pci[0], ue_max_pwr,
                                    time_offset, 10, air_sync, "0", 9,
                                    cid, ssbBitmap, bandwidth, cfr, swap_rf, 15, -70, mob_reject_code, split_arfcn_dl);
                        }
//                            getArfcnList(rsp.getCellId(), arfcn[0]);
                        int finalTime_offset = time_offset;
                        new Handler().postDelayed(() -> {
                            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setSaveOpLog(rsp.getCellId(), false);
                            String msg = "plmn = " + plmn + ", arfcn = " + arfcn[0] + ", pci = " + pci[0] + ", cid = " + cid
                                    + ", time offset = " + finalTime_offset;
                            OpLog.build().write(id, MainActivity.getOpNum() + "\t\t" + MainActivity.account + "\t\t" + MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getImsi(rsp.getCellId()) + "\t\t" +
                                    "\t\t定位参数：" + msg + "\t\t" + DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss"));
                            MainActivity.opNumPlus();
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
                    String s = getCellStr(rsp.getCellId());
                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.cell) + s + getString(R.string.cfg_trace_fail));
                    setProgress(type, 30, rsp.getCellId(), getString(R.string.cfging), true);
                    Util.showToast(getString(R.string.cell) + s + getString(R.string.trace_start_fail), Toast.LENGTH_LONG);

                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);

//                    startSecondTrace(id, indexById, rsp.getCellId(), true); //通道定位失败，开始下一通道的定位流程

                    refreshTraceBtn();
                }
            }
        }
    }

    public String getCellStr(int cell_id) {
        if (cell_id == 0) return getString(R.string.first);
        else if (cell_id == 1) return getString(R.string.second);
        else if (cell_id == 2) return getString(R.string.third);
        else return getString(R.string.fourth);
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

    public void onSetGnbRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        AppLog.I("indexById = " + indexById);
        if (indexById == -1) return;

        int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains(devA) ? 0 : 1;
        final TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil();
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_CFG_gNB || rsp.getCmdType() == GnbProtocol.UI_2_eNB_CFG_gNB) { //UI_2_gNB_CFG_gNB = 10 配置频点参数
                if (isClickStop) {
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    setProgress(type, 0, rsp.getCellId(), "空闲", false);
                    refreshTraceBtn();
                    return;
                }

                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
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
                    AppLog.I("onSetGnbRsp cell_id " + rsp.getCellId() + " workState = " + traceUtil.getWorkState(rsp.getCellId()));
                    if (/*!traceUtil.isTacChange(rsp.getCellId())
                            && */traceUtil.getWorkState(rsp.getCellId()) == GnbBean.State.GNB_CFG) {
                        setProgress(type, 50, rsp.getCellId(), "配置中", false);
                        //第三步.设置功率衰减
                        //MessageController.build().setTraceType(id, GnbProtocol.TraceType.STARTTRACE);
                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.CFG_TRACE);
                        AppLog.I("onSetGnbRsp setTxPwrOffset");
                        doSetTxPwrOffset(mCfgTraceChildFragment.getTracingCloseDistanceValue(mCfgTraceChildFragment.getCloseDistance()));
                    }

                    if (traceUtil.getWorkState(rsp.getCellId()) == GnbBean.State.IDLE) {  //断连恢复状态
                        AppLog.I("onSetGnbRsp 断连恢复状态-----------------------------------------------------------");
                        MainActivity.getInstance().getDeviceList().get(indexById).setWorkState(GnbBean.State.TRACE);
                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.TRACE);

                        final int cell_id = rsp.getCellId();
                        if (cell_id == GnbProtocol.CellId.FIRST) {
                            if (!mHandler.hasMessages(11)) {
                                mHandler.sendEmptyMessageDelayed(11, time_count * 1000L);
                            }
                        } else if (cell_id == GnbProtocol.CellId.SECOND) {
                            if (!mHandler.hasMessages(12)) {
                                mHandler.sendEmptyMessageDelayed(12, time_count * 1000L);
                            }
                        } else if (cell_id == GnbProtocol.CellId.THIRD) {
                            if (!mHandler.hasMessages(13)) {
                                mHandler.sendEmptyMessageDelayed(13, time_count * 1000L);
                            }
                        } else if (cell_id == GnbProtocol.CellId.FOURTH) {
                            if (!mHandler.hasMessages(14)) {
                                mHandler.sendEmptyMessageDelayed(14, time_count * 1000L);
                            }
                        }
                        // 刷新处于工作状态
                        freshDoWorkState(type, cell_id, traceUtil.getImsi(cell_id), traceUtil.getArfcn(cell_id), traceUtil.getPci(cell_id), traceUtil.getBandWidth(cell_id));
                        String s = getString(R.string.cell) + getCellStr(cell_id) + getString(R.string.traceing);
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, s);
                        refreshTraceBtn();
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
                            String errInfo = getErrInfo(rsp.getCellId(), rsp.getRspValue());
                            ArfcnPciBean bean;
                            int TDNum = rsp.getCellId() + 1;
                            LinkedList<ArfcnPciBean> linkedList = arfcnBeanHashMap.get("TD" + TDNum);
                            if (linkedList!=null){
                                switch (rsp.getCellId()) {
                                    case 0:
                                        if (linkedList.size() == 1) {
                                            MainActivity.getInstance().updateSteps(0, StepBean.State.fail, errInfo);
                                            setProgress(0, 50, rsp.getCellId(), getString(R.string.start_fail), true);
                                            Util.showToast(errInfo, Toast.LENGTH_LONG);
                                            mCfgTraceChildFragment.setcfgEnable(-1, true);
                                            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
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
                                            setProgress(0, 50, rsp.getCellId(), getString(R.string.start_fail), true);
                                            Util.showToast(errInfo, Toast.LENGTH_LONG);
                                            mCfgTraceChildFragment.setcfgEnable(-1, true);
                                            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
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
                                            setProgress(0, 50, rsp.getCellId(), getString(R.string.start_fail), true);
                                            Util.showToast(errInfo, Toast.LENGTH_LONG);
                                            mCfgTraceChildFragment.setcfgEnable(-1, true);
                                            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
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
                                            setProgress(0, 50, rsp.getCellId(), getString(R.string.start_fail), true);
                                            Util.showToast(errInfo, Toast.LENGTH_LONG);
                                            mCfgTraceChildFragment.setcfgEnable(-1, true);
                                            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
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
                        default:
                            mCfgTraceChildFragment.setcfgEnable(-1, true);
                            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                            String info = getErrInfo(rsp.getCellId(), rsp.getRspValue());
                            MainActivity.getInstance().updateSteps(0, StepBean.State.fail, info);
                            setProgress(0, 50, rsp.getCellId(), getString(R.string.start_fail), true);
                            Util.showToast(info, Toast.LENGTH_LONG);
                            refreshTraceBtn();
                            break;
                    }
                    resetArfcnList();
                }
            }
        }
    }

    //更新配置并调用启动黑名单开始
    private void updateParamAndStart(String id, ArfcnPciBean bean, int cell_id) {
        updateParamAndStart(id, bean.getArfcn(), bean.getPci(), cell_id);
    }

    private void updateParamAndStart(String id, String arfcnStr, String pci, int cell_id) {
        AppLog.I("updateParamAndStart id = " + id + "arfcn= " + arfcnStr + " pci = " + pci + " cell_id = " + cell_id);
        if (arfcnStr.isEmpty()) return;
        int arfcn = Integer.parseInt(arfcnStr);
        if (PaCtl.build().isB97502) {
            switch (cell_id) {
                case 0:
                    int band = NrBand.earfcn2band(arfcn);
                    mTraceUtilNr.setBandWidth(GnbProtocol.CellId.FIRST, band == 79 ? 100 : 20); //2024年6月20日13:55:09 临时版本N79改为100
                    mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.FIRST, 255);
                    mTraceUtilNr.setPlmn(GnbProtocol.CellId.FIRST, band == 78 ? "46011" : "46015");
                    mTraceUtilNr.setArfcn(cell_id, arfcnStr);
                    mTraceUtilNr.setPci(cell_id, pci);
                    break;
                case 1:
                    mTraceUtilNr.setBandWidth(GnbProtocol.CellId.SECOND, 5);
                    mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.SECOND, 128);
                    if (PaCtl.build().isB97502) { // B3/B5/B8/B40
                        int freq = LteBand.earfcn2freq(arfcn);
                        int band1 = LteBand.earfcn2band(arfcn);
                        if (band1 == 40 || (freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949))
                            mTraceUtilNr.setPlmn(GnbProtocol.CellId.SECOND, "46000");
                        else mTraceUtilNr.setPlmn(GnbProtocol.CellId.SECOND, "46001");
                    } else { // B34/B39/B40/B41
                        mTraceUtilNr.setPlmn(GnbProtocol.CellId.SECOND, "46000");
                    }
                    mTraceUtilNr.setArfcn(cell_id, arfcnStr);
                    mTraceUtilNr.setPci(cell_id, pci);
                    break;
                case 2:
                    int band2 = arfcn > 100000 ? NrBand.earfcn2band(arfcn) : LteBand.earfcn2band(arfcn);
                    mTraceUtilNr.setBandWidth(GnbProtocol.CellId.THIRD, arfcn > 100000 ? 20 : 5);
                    mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.THIRD, arfcn > 100000 ? 240 : 128);
                    mTraceUtilNr.setPlmn(GnbProtocol.CellId.THIRD, band2 == 28 ? "46000" : "46001");
                    mTraceUtilNr.setArfcn(cell_id, arfcnStr);
                    mTraceUtilNr.setPci(cell_id, pci);
                    break;
                case 3:
                    mTraceUtilNr.setBandWidth(GnbProtocol.CellId.FOURTH, 5);
                    mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.FOURTH, 128);
                    int band3 = LteBand.earfcn2band(arfcn);
                    int freq = LteBand.earfcn2freq(arfcn);
                    //B34\39\40\41\次B3
                    if ((freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949)
                            || band3 == 34 || band3 == 38 || band3 == 39 || band3 == 40 || band3 == 41)
                        mTraceUtilNr.setPlmn(GnbProtocol.CellId.FOURTH, "46000");
                    else mTraceUtilNr.setPlmn(GnbProtocol.CellId.FOURTH, "46001");
                    mTraceUtilNr.setArfcn(cell_id, arfcnStr);
                    mTraceUtilNr.setPci(cell_id, pci);
                    break;
            }
        } else {
            switch (cell_id) {
                case 0:
                    int band = NrBand.earfcn2band(arfcn);
                    mTraceUtilNr.setBandWidth(GnbProtocol.CellId.FIRST, band == 28 ? 20 : 100);
                    mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.FIRST, band == 28 ? 240 : 255);
                    mTraceUtilNr.setPlmn(GnbProtocol.CellId.FIRST, band == 78 ? "46011" : "46015");
                    mTraceUtilNr.setArfcn(cell_id, arfcnStr);
                    mTraceUtilNr.setPci(cell_id, pci);
                    break;
                case 1:
                    mTraceUtilNr.setBandWidth(GnbProtocol.CellId.SECOND, 5);
                    mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.SECOND, 128);
                    mTraceUtilNr.setPlmn(GnbProtocol.CellId.SECOND, "46000");
                    mTraceUtilNr.setArfcn(cell_id, arfcnStr);
                    mTraceUtilNr.setPci(cell_id, pci);
                    break;
                case 2:
                    int band1 = NrBand.earfcn2band(arfcn);
                    mTraceUtilNr.setBandWidth(GnbProtocol.CellId.THIRD, band1 == 1 ? 20 : 100);
                    mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.THIRD, band1 == 1 ? 240 : 255);
                    mTraceUtilNr.setPlmn(GnbProtocol.CellId.THIRD, band1 == 1 ? "46001" : "46000");
                    mTraceUtilNr.setArfcn(cell_id, arfcnStr);
                    mTraceUtilNr.setPci(cell_id, pci);
                    break;
                case 3:
                    mTraceUtilNr.setBandWidth(GnbProtocol.CellId.FOURTH, 5);
                    mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.FOURTH, 128);
                    int freq = LteBand.earfcn2freq(arfcn);
                    if ((freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949))
                        mTraceUtilNr.setPlmn(GnbProtocol.CellId.FOURTH, "46000");
                    else mTraceUtilNr.setPlmn(GnbProtocol.CellId.FOURTH, "46001");
                    mTraceUtilNr.setArfcn(cell_id, arfcnStr);
                    mTraceUtilNr.setPci(cell_id, pci);
                    break;
            }
        }
        mCfgTraceChildFragment.setcfgEnable(-1, false);
        MainActivity.getInstance().getDeviceList().get(0).setTraceUtil(mTraceUtilNr);

        //发送配置黑名单指令
        MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setWorkState(cell_id, GnbBean.State.BLACKLIST);
        sendBlackList(id, arfcn, cell_id);
    }

    private void sendBlackList(String id, int arfcn, int cellId) {
        final List<UeidBean> blackList = new ArrayList<>();
        for (MyUeidBean bean : MainActivity.getInstance().getBlackList())
            blackList.add(bean.getUeidBean());
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
                if (isClickStop) {
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
                            PaCtl.build().openLtePA(id, traceUtil.getArfcn(rsp.getCellId()), rsp.getCellId()); // 开Lte PA
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 5、发开始定位指令
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setRsrp(rsp.getCellId(), 0);
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setTacChange(rsp.getCellId(), true);
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setEnable(rsp.getCellId(), true);
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                                //第四步.启动定位
                                setProgress(type, 70, rsp.getCellId(), getString(R.string.cfging), false);


                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.CFG_TRACE);
                                if (Integer.parseInt(traceUtil.getArfcn(rsp.getCellId())) > 100000) {
                                    MessageController.build().startTrace(id, rsp.getCellId(), traceUtil.getImsi(rsp.getCellId()), 1);
                                } else {
                                    MessageController.build().startLteTrace(id, rsp.getCellId(), traceUtil.getImsi(rsp.getCellId()), 1);
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
                    String s = getCellStr(rsp.getCellId());
                    if (traceType == GnbBean.State.CFG_TRACE) {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.cell) + s + getString(R.string.cfg_trace_fail));
                        setProgress(type, 70, rsp.getCellId(), getString(R.string.open_fail), true);
                        Util.showToast(getString(R.string.cell) + s + getString(R.string.trace_start_fail), Toast.LENGTH_LONG);
//                        startSecondTrace(id, indexById, rsp.getCellId(), true); //通道定位失败，开始下一通道的定位流程
                        refreshTraceBtn();
                    } else {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.cell) + s + getString(R.string.gain_fail));
                        Util.showToast(getString(R.string.gain_fail));
                        mCfgTraceChildFragment.setCloseDistance(mCfgTraceChildFragment.closeDistanceOrigin);
                        mCfgTraceChildFragment.choose_nr_sp_power_spinner.setSelection(mCfgTraceChildFragment.closeDistanceOrigin);
                    }
                }
                //MessageController.build().setTraceType(id, GnbProtocol.TraceType.TRACE);
            }
        }
    }

    boolean isStartCatchHandler1 = false;    //是否上号
    boolean isStartCatchHandler2 = false;    //是否上号
    boolean isStartCatchHandler3 = false;    //是否上号
    boolean isStartCatchHandler4 = false;    //是否上号

    public void onStartTraceRsp(String id, GnbTraceRsp rsp) {
        AppLog.I("onStartTraceRsp id=" + id + " rsp=" + rsp);
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (id.equals("666666")) indexById = 1; //模拟信号
        if (indexById == -1) return;

        int type = 0;
        TraceUtil traceUtil = new TraceUtil();
        if (!id.equals("666666"))
            type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains(devA) ? 0 : 1;
        if (!id.equals("666666"))
            traceUtil = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil();
        if (rsp.getCmdRsp() != null) {
            final int cell_id = rsp.getCmdRsp().getCellId();
            if (cell_id == GnbProtocol.CellId.FIRST) {
                mHandler.removeMessages(11);
                mHandler.sendEmptyMessageDelayed(11, time_count * 1000L);
            } else if (cell_id == GnbProtocol.CellId.SECOND) {
                mHandler.removeMessages(12);
                mHandler.sendEmptyMessageDelayed(12, time_count * 1000L);
            } else if (cell_id == GnbProtocol.CellId.THIRD) {
                mHandler.removeMessages(13);
                mHandler.sendEmptyMessageDelayed(13, time_count * 1000L);
            } else if (cell_id == GnbProtocol.CellId.FOURTH) {
                mHandler.removeMessages(14);
                mHandler.sendEmptyMessageDelayed(14, time_count * 1000L);
            }
            //如果主B40失败跳过次B40,只走上面轮询逻辑，不走下面代码
            String value = traceUtil.getArfcn(cell_id);
            if (LteBand.earfcn2band(Integer.parseInt(value)) == 40 && mainB40Success == 0) {
                setProgress(type, 90, cell_id, getString(R.string.open_fail), true);
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(cell_id, GnbBean.State.IDLE);
                MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.open_fail));
                Util.showToast(getString(R.string.cell) + "二次频点开启失败", Toast.LENGTH_LONG);
                refreshTraceBtn();
                return;
            }
            //如果主B3失败跳过次B3,只走上面轮询逻辑，不走下面代码
            if (LteBand.earfcn2band(Integer.parseInt(value)) == 3 && mainB3Success == 0) {
                setProgress(type, 90, cell_id, getString(R.string.open_fail), true);
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(cell_id, GnbBean.State.IDLE);
                MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.open_fail));
                Util.showToast(getString(R.string.cell) + "四次频点开启失败", Toast.LENGTH_LONG);
                refreshTraceBtn();
                return;
            }
            if (rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_gNB_START_TRACE || rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_gNB_START_LTE_TRACE) {
                String s = getCellStr(cell_id);
                if (rsp.getCmdRsp().getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    if (isClickStop) {
                        MessageController.build().setCmdAndCellID(id, rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_gNB_START_TRACE ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, cell_id);
                        return;
                    }

                    doSetTxPwrOffset(mCfgTraceChildFragment.getTracingCloseDistanceValue(mCfgTraceChildFragment.getCloseDistance()));

                    //第五步.定位中，这里做判断，是设置状态为侦码中还是定位中
                    String imsi = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getImsi(cell_id);

                    // 记住业务IMSI、频点、PCI, 为恢复做准备
//                    PrefUtil.build().putValue("last_work_cfg", imsi + "/" + traceUtil.getArfcn(cell_id) + "/" + traceUtil.getPci(cell_id));

//                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setEnable(cell_id, true);
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(cell_id, GnbBean.State.TRACE);

                    // 刷新处于工作状态
                    freshDoWorkState(type, cell_id, imsi, traceUtil.getArfcn(cell_id), traceUtil.getPci(cell_id), traceUtil.getBandWidth(cell_id));
                    String str = getString(R.string.cell) + getCellStr(cell_id) + getString(R.string.traceing);
                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, str);

                    TraceUtil finalTraceUtil = traceUtil;
                    new Handler().postDelayed(() -> {
                        OpLog.build().write(id, MainActivity.getOpNum() + "\t\t" + MainActivity.account + "\t\t" + finalTraceUtil.getImsi(cell_id) + "\t\t" +
                                "\t\t启动定位" + "\t\t" + DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss"));
                        MainActivity.opNumPlus();
                    }, 500);

//                    startSecondTrace(id, indexById, cell_id, false); //通道定位成功后再开始下一通道的定位流程

                    //2024.6.5客户需求有一个成功配置才跳转目标报值页面，全失败不跳转
                    if (!isChangeImsi) {
                        if (!mCfgTraceChildFragment.isHasSuccess()) {
                            mCfgTraceChildFragment.setHasSuccess(true);
                            if (view_pager != null)
                                view_pager.setCurrentItem(1);
                        }
                    }

                    if (isChangeImsi) {
                        isChangeImsi = false;
                        Util.showToast(getString(R.string.change_trace_success));
                        if (cell_id < 3) {
                            String arfcn = "";
                            int cellID = cell_id;
                            do {
                                cellID++;
                                if (traceUtil.getWorkState(cellID) == GnbBean.State.TRACE)
                                    arfcn = traceUtil.getArfcn(cellID);
                                if (cellID == 3) break;
                            } while (arfcn.isEmpty());
                            if (!arfcn.isEmpty()) {
                                setChangeImsi(cellID, arfcn, imsi);
                            }
                        }
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
                    setProgress(type, 90, cell_id, getString(R.string.open_fail), true);
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(cell_id, GnbBean.State.IDLE);

//                    if (MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getImsi(cell_id).endsWith("0000000000")) {
                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.open_fail));

                    Util.showToast(getString(R.string.cell) + s + getString(R.string.trace_start_fail), Toast.LENGTH_LONG);
//                    } else {
//                        MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.change_trace_fail));
//                        MainActivity.getInstance().showToast(getString(R.string.cell) + s + getString(R.string.change_trace_fail));
//                    }
//                    startSecondTrace(id, indexById, rsp.getCellId(), true); //通道定位失败，开始下一通道的定位流程
                }
                refreshTraceBtn();
            }
        } else { // IMSI上报及上号报值
            if (rsp.getCellId() != -1) {
                int cell_id = rsp.getCellId();
                String traceArfcn = traceUtil.getArfcn(cell_id);
                String traceImsi = traceUtil.getImsi(cell_id);
                String tracePci = traceUtil.getPci(cell_id);
                if (rsp.getRsrp() == 666) {
                    traceArfcn = "666666";
                    traceImsi = "66666666666666";
                    tracePci = "666";
                }

                if (traceArfcn.isEmpty()) return;
                if (rsp.getRsrp() < 5 || traceImsi.isEmpty() || traceArfcn.equals("0") || tracePci.isEmpty())
                    return;


                List<String> imsiList = rsp.getImsiList();
                ContentValues values = new ContentValues();
                if (imsiList != null && imsiList.size() > 0) {
                    //侦码有报值了
                    for (int i = 0; i < imsiList.size(); i++) {
                        String rspImsi = imsiList.get(i);
                        // 黑名单中的不显示
//                        if (MainActivity.getInstance().isInBlackList(rspImsi)) return;
                        int rsrp = rsp.getRsrp();
                        AppLog.I("traceImsi = " + traceImsi + ", rspImsi = " + rspImsi + "  rsrp = " + rsrp);

                        if (rsrp < 5) rsrp = -1;

                        if (traceImsi.equals(rspImsi) && !traceImsi.equals("66666666666666")) { //上号--------------------------------------------------------------
                            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setRsrp(cell_id, rsrp);
                            ParseDataUtil.build().addDataToList(cell_id, rsrp);
                            // 刷新处于工作状态
                            freshDoWorkState(type, cell_id, MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getImsi(cell_id)
                                    , traceUtil.getArfcn(cell_id), traceUtil.getPci(cell_id), traceUtil.getBandWidth(cell_id));
                            //刷新上号信息
                            mTraceChildFragment.setConfigInfo(66, type, traceUtil.getArfcn(cell_id), traceUtil.getPci(cell_id), traceUtil.getImsi(cell_id), traceUtil.getBandWidth(cell_id));

                            //数传定位配置给单兵
                            //先判断下单兵是否开启，开启了才发，否则清除消息1
                            if (mTraceChildFragment != null && mTraceChildFragment.getSwitch_pef_pwr() != null && mTraceChildFragment.getSwitch_pef_pwr().isChecked()) {
                                if (!dataFwdBean.getArfcn().equals(traceArfcn) && rsp.getRsrp() != 0 && rsp.getRsrp() != -1) {
                                    dataFwdBean.setArfcn(traceArfcn);
                                    dataFwdBean.setPci(Integer.parseInt(tracePci));
                                    dataFwdBean.setBandwithd(traceUtil.getBandWidth(rsp.getCellId()));
                                    dataFwdBean.setTime_offset(traceUtil.getTimingOffset(rsp.getCellId()));
                                    mHandler.removeMessages(1);
                                    Message msg = mHandler.obtainMessage(1);
                                    mHandler.sendMessageDelayed(msg, 10);

                                    int value = mTraceChildFragment.getSwitch_pef_pwr().isChecked() ? -50 : -84;
                                    for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                                        for (int j = 0; j < 4; j++) {
                                            MessageController.build().setPefPwr(bean.getRsp().getDeviceId(), j, value);
                                        }
                                    }
                                }
                            } else {
                                mHandler.removeMessages(1);
                                dataFwdBean.setArfcn("");
                            }
                        }

                        boolean add = true;
                        for (int j = 0; j < mImsiList.size(); j++) {
                            if (mImsiList.get(j).getImsi().equals(rspImsi)/* && mImsiList.get(j).getArfcn().equals(traceArfcn)*/) {     //帧码列表内有当前imsi
                                notifyPosition = j;
                                add = false;
//                                mImsiList.get(j).setArfcn(traceArfcn);
                                mImsiList.get(j).setRsrp(rsrp);
                                mImsiList.get(j).setUpCount(mImsiList.get(j).getUpCount() + 1);
                                mImsiList.get(j).setLatestTime(System.currentTimeMillis());
                                if (traceImsi.equals(rspImsi))
                                    mImsiList.get(j).setState(ImsiBean.State.IMSI_NOW);
                                else if (MainActivity.getInstance().isInBlackList(rspImsi))
                                    mImsiList.get(j).setState(ImsiBean.State.IMSI_BL);

                                values = updateContentValues(mImsiList.get(j));
                                break;
                            }
                        }
                        all_count++;
                        if (add) {  //帧码列表内没有当前imsi新增
                            ImsiBean imsiBean;
                            if (traceImsi.equals(rspImsi)) { // 当前定位IMSI
                                imsiBean = new ImsiBean(ImsiBean.State.IMSI_NOW, rspImsi, traceArfcn, tracePci, rsrp, System.currentTimeMillis(), cell_id);
                                mImsiList.add(0, imsiBean);
                                notifyPosition = 0;
                            } else if (MainActivity.getInstance().isInBlackList(rspImsi)) {
                                imsiBean = new ImsiBean(ImsiBean.State.IMSI_BL, rspImsi, traceArfcn, tracePci, rsrp, System.currentTimeMillis(), cell_id);
                                mImsiList.add(imsiBean);
                                notifyPosition = mImsiList.size() - 1;
                            } else {
                                imsiBean = new ImsiBean(ImsiBean.State.IMSI_NEW, rspImsi, traceArfcn, tracePci, rsrp, System.currentTimeMillis(), cell_id);
                                mImsiList.add(imsiBean);
                                notifyPosition = mImsiList.size() - 1;
                            }

//                            if (mCatchChildFragment != null)
//                                mCatchChildFragment.addToMImsiAdapterOriginData();
                            values = updateContentValues(imsiBean);
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
                        mCatchChildFragment.setCatchCount(all_count, telecom_count, mobile_count, unicom_count, sva_count);

                        //上号才报目标已上号且插入，否则是侦码直接插入数据库
                        if (traceImsi.equals(rspImsi)) {    // 上号imsi
                            if (!isClickStop) {
                                if (cell_id == 0) {
                                    if (!isStartCatchHandler1) { //每次第一次上号插入数据库
                                        CatchImsiDBUtil.insertCatchImsiInDB(values);
                                        if (!mHandler.hasMessages(2)) {
                                            mTraceChildFragment.say(type, 0, getString(R.string.trace_catch), true);
                                            mHandler.sendEmptyMessageDelayed(2, TraceBean.RSRP_TIME_INTERVAL);
                                        }
                                    }
                                    setExceptOtherStartCatchHandler(1, true);
                                } else if (cell_id == 1) {
                                    if (!isStartCatchHandler2) { //每次第一次上号插入数据库
                                        CatchImsiDBUtil.insertCatchImsiInDB(values);
                                        if (!mHandler.hasMessages(2)) {
                                            mTraceChildFragment.say(type, 0, getString(R.string.trace_catch), true);
                                            mHandler.sendEmptyMessageDelayed(2, TraceBean.RSRP_TIME_INTERVAL);
                                        }
                                    }
                                    setExceptOtherStartCatchHandler(2, true);
                                } else if (cell_id == 2) {
                                    if (!isStartCatchHandler3) { //每次第一次上号插入数据库
                                        CatchImsiDBUtil.insertCatchImsiInDB(values);
                                        if (!mHandler.hasMessages(2)) {
                                            mTraceChildFragment.say(type, 0, getString(R.string.trace_catch), true);
                                            mHandler.sendEmptyMessageDelayed(2, TraceBean.RSRP_TIME_INTERVAL);
                                        }
                                    }
                                    setExceptOtherStartCatchHandler(3, true);
                                } else if (cell_id == 3) {
                                    if (!isStartCatchHandler4) { //每次第一次上号插入数据库
                                        CatchImsiDBUtil.insertCatchImsiInDB(values);
                                        if (!mHandler.hasMessages(2)) {
                                            mTraceChildFragment.say(type, 0, getString(R.string.trace_catch), true);
                                            mHandler.sendEmptyMessageDelayed(2, TraceBean.RSRP_TIME_INTERVAL);
                                        }
                                    }
                                    setExceptOtherStartCatchHandler(4, true);
                                }
                            }
                        } else {    //侦码imsi
                            CatchImsiDBUtil.insertCatchImsiInDB(values);
                        }
                    }

                    if (!mHandler.hasMessages(9)) {
                        mCatchChildFragment.resetShowData(false); // 刷新视图
                        mHandler.sendEmptyMessage(9);
                    }
                }

                if (id != "666666" && MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getWorkState(cell_id) == GnbBean.State.IDLE) {
                    //断连后重连
                    AppLog.I("onStartTraceRsp 上号断连恢复状态-----------------------------------------------------------");
                    MainActivity.getInstance().getDeviceList().get(indexById).setWorkState(GnbBean.State.TRACE);
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(cell_id, GnbBean.State.TRACE);
                    getmCfgTraceChildFragment().setcfgEnable(-1, false);
                    setBtnStr(true);
                }

                if (id != "666666" && MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getWorkState(cell_id) == GnbBean.State.TRACE && !isRsrpStart && rsp.getRsrp() > 5) {
                    countFirst = 0;
                    countSecond = 0;
                    countThird = 0;
                    countFourth = 0;
                    isRsrpStart = true;
                }
            }
        }
    }

    public void sendEmptyMessageIfNotExist(int what) {
        if (!mHandler.hasMessages(what)) {
            mHandler.sendEmptyMessage(what);
        }
    }

    //将除了输入的数字之外的其他的都设为与他不相等的boolean
    private void setExceptOtherStartCatchHandler(int i, boolean b) {
        isStartCatchHandler1 = !b;
        isStartCatchHandler2 = !b;
        isStartCatchHandler3 = !b;
        isStartCatchHandler4 = !b;
        switch (i) {
            case 1:
                isStartCatchHandler1 = b;
                break;
            case 2:
                isStartCatchHandler2 = b;
                break;
            case 3:
                isStartCatchHandler3 = b;
                break;
            case 4:
                isStartCatchHandler4 = b;
                break;
        }
    }

    private ContentValues updateContentValues(ImsiBean imsiBean) {
        ContentValues values = new ContentValues();
        values.put("IMSI", imsiBean.getImsi());
        values.put("ARFCN", imsiBean.getArfcn());
        values.put("PCI", imsiBean.getPci());
        values.put("FIRSTTIME", imsiBean.getFirstTime());
        values.put("LATESTTIME", imsiBean.getLatestTime());
        values.put("STATE", imsiBean.getState());
        values.put("CELLID", imsiBean.getCellId());
        values.put("LOSSCOUNT", imsiBean.getLossCount());
        values.put("UPCOUNT", imsiBean.getUpCount());
        values.put("RSRP", imsiBean.getRsrp());
        return values;
    }


    public void onStopTraceRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;

        int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains(devA) ? 0 : 1;
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_STOP_TRACE || rsp.getCmdType() == GnbProtocol.UI_2_gNB_STOP_LTE_TRACE) {
                isCell1SetArfcnChange = false;
                isCell2SetArfcnChange = false;
                isCell3SetArfcnChange = false;
                isCell4SetArfcnChange = false;

                //切换频段接着运行
                String arfcn = MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getArfcn(rsp.getCellId());
                String pci = MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getPci(rsp.getCellId());
                switch (rsp.getCellId()) {
                    case 0:
                        if (restart1 && MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST) != GnbBean.State.STOP) {
                            updateParamAndStart(id, arfcn, pci, rsp.getCellId());
                            return;
                        }
                        break;
                    case 1:
                        if (restart2 && MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) != GnbBean.State.STOP) {
                            updateParamAndStart(id, arfcn, pci, rsp.getCellId());
                            return;
                        }
                        break;
                    case 2:
                        if (restart3 && MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getWorkState(GnbProtocol.CellId.THIRD) != GnbBean.State.STOP) {
                            updateParamAndStart(id, arfcn, pci, rsp.getCellId());
                            return;
                        }
                        break;
                    case 3:
                        if (restart4 && MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().getWorkState(GnbProtocol.CellId.FOURTH) != GnbBean.State.STOP) {
                            updateParamAndStart(id, arfcn, pci, rsp.getCellId());
                            return;
                        }
                        break;
                }


                String s = getCellStr(rsp.getCellId());
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.cell) + s + getString(R.string.stop_work_success));
                    setProgress(type, 0, rsp.getCellId(), getString(R.string.idle), false);
                    mTraceChildFragment.resetRsrp(type);
                } else {
                    if (rsp.getRspValue() == 3) {
                        Util.showToast("小区" + s + "忙，请稍后再结束", Toast.LENGTH_LONG);
                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.TRACE);
                        setProgress(type, 100, rsp.getCellId(), "定位中", false);
                        refreshTraceBtn();
                        return;
                    } else if (rsp.getRspValue() == 5) {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, "小区" + s + "结束工作成功");
                        setProgress(type, 0, rsp.getCellId(), "空闲", false);
                    } else {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "小区" + s + "结束工作失败");
                        setProgress(type, 50, rsp.getCellId(), "结束失败", true);
                        Util.showToast("小区" + s + "结束工作失败, 请重试", Toast.LENGTH_LONG);
                    }
                }

                //停止数传,arfcn设为0
                //先判断下单兵是否开启，开启了才发，否则清除消息1
                mTraceChildFragment.setSwitch_pef_pwr(false);
                if (mTraceChildFragment.getSwitch_pef_pwr() != null && mTraceChildFragment.getSwitch_pef_pwr().isChecked()) {
                    dataFwdBean.setArfcn("0");
                    dataFwdBean.setPci(1);
                    Gson gson = new Gson();
                    String json = gson.toJson(dataFwdBean);
                    for (DeviceInfoBean deviceInfoBean : MainActivity.getInstance().getDeviceList()) {
                        AppLog.D("setDWDataFwd deviceInfoBean.getRsp().getDeviceId()" + deviceInfoBean.getRsp().getDeviceId() + "json = " + json);
                        MessageController.build().setDataFwd(deviceInfoBean.getRsp().getDeviceId(), json, true);
                    }
                }
                dataFwdBean.setArfcn("");

                OpLog.build().write(id, MainActivity.getOpNum() + "\t\t" + MainActivity.account + "\t\t" + MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getImsi(rsp.getCellId()) + "\t\t" +
                        "\t\t结束定位" + "\t\t" + DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss"));
                MainActivity.opNumPlus();
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setTacChange(rsp.getCellId(), false);
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setEnable(rsp.getCellId(), false);
                //refreshTraceValue(indexById, rsp.getCellId(), 0);
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setRsrp(rsp.getCellId(), 0);
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setLastRsrp(rsp.getCellId(), -1);
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setImsi(rsp.getCellId(), "");
                //MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setArfcn(rsp.getCellId(), "");
                //MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setPci(rsp.getCellId(), "");
//                mHandler.removeMessages(2);
//                mHandler.removeMessages(3);
                if (rsp.getCellId() == GnbProtocol.CellId.FIRST) {
                    mHandler.removeMessages(11);
                } else if (rsp.getCellId() == GnbProtocol.CellId.SECOND) {
                    mHandler.removeMessages(12);
                } else if (rsp.getCellId() == GnbProtocol.CellId.THIRD) {
                    mHandler.removeMessages(13);
                } else if (rsp.getCellId() == GnbProtocol.CellId.FOURTH) {
                    mHandler.removeMessages(14);
                }
                isRsrpStart = false;
                isStartCatchHandler1 = false;
                isStartCatchHandler2 = false;
                isStartCatchHandler3 = false;
                isStartCatchHandler4 = false;
                if (mHandler.hasMessages(2))
                    mHandler.removeMessages(2);
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

    //通过4个通道状态设置按钮状态
    //有定位则显示结束，有侦码则显示侦码，有结束则显示结束中，全空闲总的是扫频显示启动中否则显示开启，否则显示启动中
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
        if (deviceList.size() == 1) { // 一个设备
            TraceUtil traceUtil = deviceList.get(0).getTraceUtil();

            int workStateFirst = traceUtil.getWorkState(GnbProtocol.CellId.FIRST); // 通道一
            int workStateSecond = traceUtil.getWorkState(GnbProtocol.CellId.SECOND); // 通道二
            int workStateThird = traceUtil.getWorkState(GnbProtocol.CellId.THIRD); // 通道三
            int workStateFourth = traceUtil.getWorkState(GnbProtocol.CellId.FOURTH); // 通道四

            AppLog.I("refreshTraceBtn deviceList size = 1, workStateFirst = " + workStateFirst
                    + ", workStateSecond = " + workStateSecond + ", workStateThird = " + workStateThird + ", workStateFourth = " + workStateFourth);

            if (workStateFirst == GnbBean.State.TRACE || workStateSecond == GnbBean.State.TRACE || workStateThird == GnbBean.State.TRACE || workStateFourth == GnbBean.State.TRACE) {
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.TRACE);
                tv_do_btn.setText(getString(R.string.stop));
                mCfgTraceChildFragment.setcfgEnable(-1, false);

            } else if (workStateFirst == GnbBean.State.CATCH || workStateSecond == GnbBean.State.CATCH || workStateThird == GnbBean.State.CATCH || workStateFourth == GnbBean.State.CATCH) {
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.CATCH);
                tv_do_btn.setText(getString(R.string.stop));
                mCfgTraceChildFragment.setcfgEnable(-1, false);

            } else if (workStateFirst == GnbBean.State.STOP || workStateSecond == GnbBean.State.STOP || workStateThird == GnbBean.State.STOP || workStateFourth == GnbBean.State.STOP) {
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.STOP);
                tv_do_btn.setText(getString(R.string.stoping));
            } else if (workStateFirst == GnbBean.State.IDLE && workStateSecond == GnbBean.State.IDLE && workStateThird == GnbBean.State.IDLE && workStateFourth == GnbBean.State.IDLE) {
                if (deviceList.get(0).getWorkState() == GnbBean.State.FREQ_SCAN) {
                    tv_do_btn.setText(getString(R.string.starting));
                    mCfgTraceChildFragment.setcfgEnable(-1, false);
                } else {
                    MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.IDLE);
                    tv_do_btn.setText(getString(R.string.open));
                    mCfgTraceChildFragment.setcfgEnable(-1, true);
                }
            } else {
                tv_do_btn.setText(getString(R.string.starting));
                mCfgTraceChildFragment.setcfgEnable(-1, false);
            }
            if (getBtnStr().contains(getString(R.string.stop))) {
                tv_do_btn.setBackgroundResource(R.drawable.btn_bg_delete);
            } else {
                tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
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

        if (workStateFirst0 == GnbBean.State.IDLE
                && (deviceList.get(0).getRsp().getDualCell() == 1 || workStateSecond0 == GnbBean.State.IDLE || workStateSecond0 == GnbBean.State.NONE)
                && workStateFirst1 == GnbBean.State.IDLE
                && (deviceList.get(1).getRsp().getDualCell() == 1 || workStateSecond1 == GnbBean.State.IDLE || workStateSecond1 == GnbBean.State.NONE)) {
            MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.IDLE);
            MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.IDLE);
            tv_do_btn.setText(getString(R.string.open));
        } else if (workStateFirst0 == GnbBean.State.STOP || workStateSecond0 == GnbBean.State.STOP || workStateFirst1 == GnbBean.State.STOP || workStateSecond1 == GnbBean.State.STOP) {
            if (workStateFirst1 == GnbBean.State.STOP || workStateSecond1 == GnbBean.State.STOP) {
                MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.STOP);
            } else
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.STOP);
            tv_do_btn.setText(getString(R.string.stoping));
        } else if (workStateFirst0 == GnbBean.State.TRACE || workStateSecond0 == GnbBean.State.TRACE || workStateFirst1 == GnbBean.State.TRACE || workStateSecond1 == GnbBean.State.TRACE) {
            if (workStateFirst1 == GnbBean.State.TRACE || workStateSecond1 == GnbBean.State.TRACE) {
                MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.TRACE);
            } else
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.TRACE);
            tv_do_btn.setText(getString(R.string.stop));
        } else if (workStateFirst0 == GnbBean.State.CATCH || workStateSecond0 == GnbBean.State.CATCH || workStateFirst1 == GnbBean.State.CATCH || workStateSecond1 == GnbBean.State.CATCH) {
            if (workStateFirst1 == GnbBean.State.CATCH || workStateSecond1 == GnbBean.State.CATCH) {
                MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.CATCH);
            } else
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.CATCH);
            tv_do_btn.setText(getString(R.string.stop));
        } else {
            tv_do_btn.setText(getString(R.string.starting));
        }
        if (getBtnStr().contains(getString(R.string.stop)))
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_delete);
        else {
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
        }
    }

    int countFirst = 0; //报值为-1的次数
    int countSecond = 0;
    int countThird = 0;
    int countFourth = 0;
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
    int sendIndex = 0;  //每3秒发2次
    int countIfsetRsrpValueFirst = 0;   //发6次-1进去成功报掉线
    int countIfsetRsrpValueSecond = 0;
    int countIfsetRsrpValueThird = 0;
    int countIfsetRsrpValueFourth = 0;
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (dataFwdBean == null) return;
                    Message msg2 = new Message();
                    msg2.what = msg.what;
                    if (sendIndex == 2) {
                        mHandler.sendMessageDelayed(msg2, 3000);
                        sendIndex = 0;
                    } else {
                        Gson gson = new Gson();
                        String json = gson.toJson(dataFwdBean);
                        for (DeviceInfoBean deviceInfoBean : MainActivity.getInstance().getDeviceList()) {
//                            AppLog.D("handler 1 setDWDataFwd deviceInfoBean.getRsp().getDeviceId()" + deviceInfoBean.getRsp().getDeviceId() + "json = " + json);
                            MessageController.build().setDataFwd(deviceInfoBean.getRsp().getDeviceId(), json, true);
                        }
                        mHandler.sendMessageDelayed(msg2, 100);
                        sendIndex++;
                    }
                    break;
                case 2: //刷新数值
                    AppLog.I("mHandler 2 --------------------------------------------------------");
                    List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
                    for (int i = 0; i < deviceList.size(); i++) {
                        TraceUtil traceUtil = deviceList.get(i).getTraceUtil();
//                        AppLog.D("traceUtil.getWorkState(GnbProtocol.CellId.FIRST)" + traceUtil.getWorkState(GnbProtocol.CellId.FIRST) + "isStartCatchHandler1" + isStartCatchHandler1 + "\n" +
//                                "traceUtil.getWorkState(GnbProtocol.CellId.Second)" + traceUtil.getWorkState(GnbProtocol.CellId.SECOND) + "isStartCatchHandler2" + isStartCatchHandler2 + "\n" +
//                                "traceUtil.getWorkState(GnbProtocol.CellId.Third)" + traceUtil.getWorkState(GnbProtocol.CellId.THIRD) + "isStartCatchHandler3" + isStartCatchHandler3 + "\n" +
//                                "traceUtil.getWorkState(GnbProtocol.CellId.FOURTH)" + traceUtil.getWorkState(GnbProtocol.CellId.FOURTH) + "isStartCatchHandler4" + isStartCatchHandler4);
                        if (traceUtil.getWorkState(GnbProtocol.CellId.FIRST) == GnbBean.State.TRACE) {

                            int rsrp = -1;
                            if (traceUtil.getRsrp(GnbProtocol.CellId.FIRST) != -1) {
                                rsrp = traceUtil.getRsrp(GnbProtocol.CellId.FIRST);
                                countFirst = 0;
                                ifRecountFirst = true;
                            } else {
                                countFirst++;
                            }
//                            AppLog.D("mHandler update first value = " + rsrp + ", countFirst = " + countFirst);
                            if (countFirst < 6) {
                                if (rsrp != -1) {
                                    mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.FIRST, i, rsrp);
                                }
                                countIfsetRsrpValueFirst = 0;
                            } else {    //连续6次-1值视为掉线，开始报-1
                                //掉线，恢复轮询
                                if (ifRecountFirst) {
                                    AppLog.I("通道一掉线重计时--------------------");
                                    mHandler.removeMessages(11);
                                    mHandler.sendEmptyMessageDelayed(11, time_count * 1000L);
                                    ifRecountFirst = false;
                                }
                                countIfsetRsrpValueFirst++;
                                if (countIfsetRsrpValueFirst > 6) {   //完全掉线
                                    isStartCatchHandler1 = false;
                                    if (!hasStartCatchHandlerTrue()) {  //没有其他上号的则去掉
                                        removeMessages(2);
                                        return;
                                    }
                                }
                                if (isStartCatchHandler1)
                                    mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.FIRST, i, rsrp);
                            }
                        }
                        if (traceUtil.getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.TRACE) {

                            int rsrp = -1;
                            if (traceUtil.getRsrp(GnbProtocol.CellId.SECOND) != -1) {
                                rsrp = traceUtil.getRsrp(GnbProtocol.CellId.SECOND);
                                countSecond = 0;
                                ifRecountSecond = true;
                            } else {
                                countSecond++;
                            }
                            //AppLog.I("mHandler update second value = " + rsrp + ", countSecond = " + countSecond);
                            if (countSecond < 6) {
                                if (rsrp != -1) {
                                    mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.SECOND, i, rsrp);
                                }
                                countIfsetRsrpValueSecond = 0;
                            } else {
                                //掉线，恢复轮询
                                if (ifRecountSecond) {
                                    AppLog.I("通道二掉线重计时--------------------");
                                    mHandler.removeMessages(12);
                                    mHandler.sendEmptyMessageDelayed(12, time_count * 1000L);
                                    ifRecountSecond = false;
                                }
                                countIfsetRsrpValueSecond++;
                                if (countIfsetRsrpValueSecond > 6) {
                                    isStartCatchHandler2 = false;
                                    if (!hasStartCatchHandlerTrue()) {  //没有其他上号的则去掉
                                        removeMessages(2);
                                        return;
                                    }
                                }
                                if (isStartCatchHandler2)
                                    mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.SECOND, i, rsrp);
                            }
                        }

                        if (traceUtil.getWorkState(GnbProtocol.CellId.THIRD) == GnbBean.State.TRACE) {

                            int rsrp = -1;
                            if (traceUtil.getRsrp(GnbProtocol.CellId.THIRD) != -1) {
                                rsrp = traceUtil.getRsrp(GnbProtocol.CellId.THIRD);
                                countThird = 0;
                                ifRecountThird = true;
                            } else {
                                countThird++;
                            }
                            //AppLog.I("mHandler update second value = " + rsrp + ", countSecond = " + countSecond);
                            if (countThird < 6) {
                                if (rsrp != -1) {
                                    mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.THIRD, i, rsrp);
                                }
                                countIfsetRsrpValueThird = 0;
                            } else {
                                //掉线，恢复轮询
                                if (ifRecountThird) {
                                    AppLog.I("通道三掉线重计时--------------------");
                                    mHandler.removeMessages(13);
                                    mHandler.sendEmptyMessageDelayed(13, time_count * 1000L);
                                    ifRecountThird = false;
                                }
                                countIfsetRsrpValueThird++;
                                if (countIfsetRsrpValueThird > 6) {
                                    isStartCatchHandler3 = false;
                                    if (!hasStartCatchHandlerTrue()) {  //没有其他上号的则去掉
                                        removeMessages(2);
                                        return;
                                    }
                                }
                                if (isStartCatchHandler3)
                                    mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.THIRD, i, rsrp);
                            }
                        }

                        if (traceUtil.getWorkState(GnbProtocol.CellId.FOURTH) == GnbBean.State.TRACE) {

                            int rsrp = -1;
                            if (traceUtil.getRsrp(GnbProtocol.CellId.FOURTH) != -1) {
                                rsrp = traceUtil.getRsrp(GnbProtocol.CellId.FOURTH);
                                countFourth = 0;
                                ifRecountFourth = true;
                            } else {
                                countFourth++;
                            }
                            if (countFourth < 6) {
                                if (rsrp != -1) {
                                    mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.FOURTH, i, rsrp);
                                }
                                countIfsetRsrpValueFourth = 0;
                            } else {
                                //掉线，恢复轮询
                                if (ifRecountFourth) {
                                    AppLog.I("通道四掉线重计时--------------------");
                                    mHandler.removeMessages(14);
                                    mHandler.sendEmptyMessageDelayed(14, time_count * 1000L);
                                    ifRecountFourth = false;
                                }
                                countIfsetRsrpValueFourth++;
                                if (countIfsetRsrpValueFourth > 6) {
                                    isStartCatchHandler4 = false;
                                    if (!hasStartCatchHandlerTrue()) {  //没有其他上号的则去掉
                                        removeMessages(2);
                                        return;
                                    }
                                }
                                if (isStartCatchHandler4)
                                    mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.FOURTH, i, rsrp);
                            }
                        }
                    }

                    mHandler.sendEmptyMessageDelayed(2, TraceBean.RSRP_TIME_INTERVAL);
                    break;
                case 9:
//                    mCatchChildFragment.resetShowData(false); // 刷新视图
                    AppLog.D("mhandler 9 notifyPosition = " + notifyPosition);
                    //刷新某一项
                    mCatchChildFragment.notifyMimsiAdapterItemChanged();
                    mHandler.sendEmptyMessageDelayed(9, 1500);
                    break;
                case 11:
                    if (MainActivity.getInstance().getDeviceList().get(0).getWorkState() != GnbBean.State.TRACE) {
                        AppLog.I("mHandler message 11 not in TRACE----------------------------------------------------------------------");
                        return;
                    }
                    AppLog.I("mHandler message 11----------------------------------------------------------------------");
                    if (MainActivity.getInstance().getDeviceList().size() > 0
                            && time_count != 0 && !isStartCatchHandler1) { //上号不执行
                        LinkedList<ArfcnPciBean> arfcnList = arfcnBeanHashMap.get("TD1");
                        if (arfcnList == null || arfcnList.size() < 2) return;
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex0) {
                                String value = String.valueOf(arfcnList.get(i).getArfcn());    //下一个准备轮询的arfcn值
                                String pci = String.valueOf(arfcnList.get(i).getPci());    //下一个准备轮询的arfcn值
                                if (NrBand.earfcn2band(Integer.parseInt(value)) == NrBand.earfcn2band(Integer.parseInt(mTraceUtilNr.getArfcn(GnbProtocol.CellId.FIRST)))) {
                                    AppLog.I("mHandler 0 change arfcn = " + value + ", autoArfcnIndex0 = " + autoArfcnIndex0);
                                    //苏文更新左侧用的
//                                    updateParam(0, value, mTraceUtilNr.getPci(GnbProtocol.CellId.FIRST));
                                    //更新页面信息
                                    freshDoWorkState(0, GnbProtocol.CellId.FIRST, mTraceUtilNr.getImsi(GnbProtocol.CellId.FIRST), value, mTraceUtilNr.getPci(GnbProtocol.CellId.FIRST), mTraceUtilNr.getBandWidth(GnbProtocol.CellId.FIRST));
                                    MessageController.build().setArfcn(MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId(), 0, value);
                                    MessageController.build().setPci(MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId(), 0, pci);
                                    autoArfcnIndex0++;
                                    AppLog.D("autoArfcnIndex0++, autoArfcnIndex0 = " + autoArfcnIndex0);
                                    if (autoArfcnIndex0 >= arfcnList.size())
                                        autoArfcnIndex0 = 0;
                                } else {
                                    AppLog.I("mHandler 0 change arfcn = " + value + " is different band, stop at first" + ", autoArfcnIndex0 = " + autoArfcnIndex0);
                                    restart1 = true;
                                    String deviceId = MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId();
                                    MessageController.build().setCmdAndCellID(deviceId, GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.FIRST);
                                    PaCtl.build().closePAByArfcn(deviceId, false, mTraceUtilNr.getArfcn(GnbProtocol.CellId.FIRST));
                                    MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setArfcn(GnbProtocol.CellId.FIRST, value);
                                    MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setPci(GnbProtocol.CellId.FIRST, pci);
                                    autoArfcnIndex0++;
//                                    AppLog.D("autoArfcnIndex0++, autoArfcnIndex0 = " + autoArfcnIndex0);
                                    if (autoArfcnIndex0 >= arfcnList.size())
                                        autoArfcnIndex0 = 0;
                                    return;
                                }
                                mTraceUtilNr.setArfcn(GnbProtocol.CellId.FIRST, value); //更新当前配置的值
                                mTraceUtilNr.setPci(GnbProtocol.CellId.FIRST, pci); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(11, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(11, time_count * 1000L);
                    break;
                case 12:
                    if (MainActivity.getInstance().getDeviceList().get(0).getWorkState() != GnbBean.State.TRACE) {
                        AppLog.I("mHandler message 12 not in TRACE----------------------------------------------------------------------");
                        return;
                    }
                    AppLog.I("mHandler message 12");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler2) {
                        LinkedList<ArfcnPciBean> arfcnList = arfcnBeanHashMap.get("TD2");
                        if (arfcnList == null || arfcnList.size() < 2) return;
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex1) {
                                String value = String.valueOf(arfcnList.get(i).getArfcn());
                                String pci = String.valueOf(arfcnList.get(i).getPci());
                                //如果主B40没执行过，循环等待主B40结果,会卡在上一个频点等待
                                if (LteBand.earfcn2band(Integer.parseInt(value)) == 40 && mainB40Success == -1) {
                                    AppLog.I("mHandler 1 wait mainB40 arfcn = " + value + ", autoArfcnIndex = " + autoArfcnIndex1);
                                    mHandler.sendEmptyMessageDelayed(12, 10 * 1000L);   //10秒检查一次
                                    return;
                                }
                                //如果主B40失败跳过次B40
                                if (LteBand.earfcn2band(Integer.parseInt(value)) == 40 && mainB40Success == 0) {
                                    autoArfcnIndex1++;  //执行下一个频点
                                    continue;
                                }
                                if (LteBand.earfcn2band(Integer.parseInt(value)) == LteBand.earfcn2band(Integer.parseInt(mTraceUtilNr.getArfcn(GnbProtocol.CellId.SECOND)))) {
                                    AppLog.I("mHandler 1 change arfcn = " + value + ", autoArfcnIndex = " + autoArfcnIndex1);
//                                    updateParam(1, value, mTraceUtilNr.getPci(GnbProtocol.CellId.SECOND));
                                    //更新页面信息
                                    freshDoWorkState(0, GnbProtocol.CellId.SECOND, mTraceUtilNr.getImsi(GnbProtocol.CellId.SECOND), value, mTraceUtilNr.getPci(GnbProtocol.CellId.SECOND), mTraceUtilNr.getBandWidth(GnbProtocol.CellId.SECOND));
                                    MessageController.build().setArfcn(MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId(), 1, value);
                                    MessageController.build().setPci(MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId(), 1, pci);
                                    autoArfcnIndex1++;
                                    if (autoArfcnIndex1 >= arfcnList.size())
                                        autoArfcnIndex1 = 0;
                                } else {
                                    AppLog.I("mHandler 1 change arfcn = " + value + " is different band, stop at first" + ", autoArfcnIndex = " + autoArfcnIndex1);
                                    restart2 = true;
                                    String deviceId = MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId();
                                    MessageController.build().setCmdAndCellID(deviceId, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.SECOND);
                                    PaCtl.build().closePAByArfcn(deviceId, true, mTraceUtilNr.getArfcn(GnbProtocol.CellId.SECOND));
                                    MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setArfcn(GnbProtocol.CellId.SECOND, value);
                                    MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setPci(GnbProtocol.CellId.SECOND, pci);
                                    autoArfcnIndex1++;
                                    if (autoArfcnIndex1 >= arfcnList.size())
                                        autoArfcnIndex1 = 0;
                                    return;
                                }
                                mTraceUtilNr.setArfcn(GnbProtocol.CellId.SECOND, value); //更新当前配置的值
                                mTraceUtilNr.setPci(GnbProtocol.CellId.SECOND, pci); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(12, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(12, time_count * 1000L);
                    break;
                case 13:
                    if (MainActivity.getInstance().getDeviceList().get(0).getWorkState() != GnbBean.State.TRACE) {
                        AppLog.I("mHandler message 13 not in TRACE----------------------------------------------------------------------");
                        return;
                    }
                    AppLog.I("mHandler message 13");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler3) {
                        LinkedList<ArfcnPciBean> arfcnList = arfcnBeanHashMap.get("TD3");
                        if (arfcnList == null || arfcnList.size() < 2) return;
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex2) {
                                String value = String.valueOf(arfcnList.get(i).getArfcn());
                                String pci = String.valueOf(arfcnList.get(i).getPci());
                                if (NrBand.earfcn2band(Integer.parseInt(value)) == NrBand.earfcn2band(Integer.parseInt(mTraceUtilNr.getArfcn(GnbProtocol.CellId.THIRD)))) {
                                    AppLog.I("mHandler 2 change arfcn = " + value + ", autoArfcnIndex = " + autoArfcnIndex2);
//                                    updateParam(2, value, mTraceUtilNr.getPci(GnbProtocol.CellId.THIRD));
                                    //更新页面信息
                                    freshDoWorkState(0, GnbProtocol.CellId.THIRD, mTraceUtilNr.getImsi(GnbProtocol.CellId.THIRD), value, mTraceUtilNr.getPci(GnbProtocol.CellId.THIRD), mTraceUtilNr.getBandWidth(GnbProtocol.CellId.THIRD));
                                    MessageController.build().setArfcn(MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId(), 2, value);
                                    MessageController.build().setPci(MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId(), 2, pci);
                                    autoArfcnIndex2++;
                                    if (autoArfcnIndex2 >= arfcnList.size())
                                        autoArfcnIndex2 = 0;
                                } else {
                                    AppLog.D("mHandler 2 change arfcn = " + value + " is different band, stop at first" + ", autoArfcnIndex = " + autoArfcnIndex2);
                                    restart3 = true;
                                    String deviceId = MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId();
                                    MessageController.build().setCmdAndCellID(deviceId, GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.THIRD);
                                    PaCtl.build().closePAByArfcn(deviceId, false, mTraceUtilNr.getArfcn(GnbProtocol.CellId.THIRD));
                                    MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setArfcn(GnbProtocol.CellId.THIRD, value);
                                    MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setPci(GnbProtocol.CellId.THIRD, pci);
                                    autoArfcnIndex2++;
                                    if (autoArfcnIndex2 >= arfcnList.size())
                                        autoArfcnIndex2 = 0;
                                    return;
                                }
                                mTraceUtilNr.setArfcn(GnbProtocol.CellId.THIRD, value); //更新当前配置的值
                                mTraceUtilNr.setPci(GnbProtocol.CellId.THIRD, pci); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(13, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(13, time_count * 1000L);
                    break;
                case 14:
                    if (MainActivity.getInstance().getDeviceList().get(0).getWorkState() != GnbBean.State.TRACE) {
                        AppLog.I("mHandler message 14 not in TRACE----------------------------------------------------------------------");
                        return;
                    }
                    AppLog.I("mHandler message 14");
                    if (MainActivity.getInstance().getDeviceList().size() > 0 && time_count != 0 && !isStartCatchHandler4) {
                        LinkedList<ArfcnPciBean> arfcnList = arfcnBeanHashMap.get("TD4");
                        AppLog.D("mHandler message 14 arfcnList size = " + arfcnList.size() + ", autoArfcnIndex3 = " + autoArfcnIndex3);
                        if (arfcnList == null || arfcnList.size() < 2) return;
                        for (int i = 0; i < arfcnList.size(); i++) {
                            if (i >= autoArfcnIndex3) {
                                String value = String.valueOf(arfcnList.get(i).getArfcn());
                                String pci = String.valueOf(arfcnList.get(i).getPci());
                                String arfcn = mTraceUtilNr.getArfcn(GnbProtocol.CellId.FOURTH);
                                //如果主B3没执行过，循环等待主B3结果,会卡在上一个频点等待
                                if (LteBand.earfcn2band(Integer.parseInt(value)) == 3 && mainB3Success == -1) {
                                    AppLog.I("mHandler 3 wait mainB3 arfcn = " + value + ", autoArfcnIndex = " + autoArfcnIndex3);
                                    mHandler.sendEmptyMessageDelayed(14, 10 * 1000L);   //10秒检查一次
                                    return;
                                }
                                //如果主B3失败跳过次B3
                                if (LteBand.earfcn2band(Integer.parseInt(value)) == 3 && mainB3Success == 0) {
                                    autoArfcnIndex3++;  //执行下一个频点
                                    continue;
                                }
                                if (LteBand.earfcn2band(Integer.parseInt(value)) == LteBand.earfcn2band(Integer.parseInt(arfcn))) {

                                    int freqValue = LteBand.earfcn2freq(Integer.parseInt(value));
                                    int freqArfcn = LteBand.earfcn2freq(Integer.parseInt(arfcn));

                                    if (((freqArfcn > 1709 && freqArfcn < 1735) || (freqArfcn > 1804 && freqArfcn < 1830)) && (freqValue > 1709 && freqValue < 1735) || (freqValue > 1804 && freqValue < 1830)
                                            || ((freqArfcn > 888 && freqArfcn < 904) || (freqArfcn > 933 && freqArfcn < 949)) && (freqValue > 888 && freqValue < 904) || (freqValue > 933 && freqValue < 949)) {
                                        AppLog.I("mHandler 3 change arfcn = " + value + ", autoArfcnIndex = " + autoArfcnIndex3);
//                                        updateParam(3, value, mTraceUtilNr.getPci(GnbProtocol.CellId.FOURTH));
                                        //更新页面信息
                                        freshDoWorkState(0, GnbProtocol.CellId.FOURTH, mTraceUtilNr.getImsi(GnbProtocol.CellId.FOURTH), value, mTraceUtilNr.getPci(GnbProtocol.CellId.FOURTH), mTraceUtilNr.getBandWidth(GnbProtocol.CellId.FOURTH));
                                        MessageController.build().setArfcn(MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId(), 3, value);
                                        MessageController.build().setPci(MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId(), 3, pci);
                                    } else {
                                        AppLog.I("mHandler 3 change arfcn = " + value + " need stop, , autoArfcnIndex = " + autoArfcnIndex3);
                                        restart4 = true;
                                        String deviceId = MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId();
                                        MessageController.build().setCmdAndCellID(deviceId, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FOURTH);
                                        PaCtl.build().closePAByArfcn(deviceId, true, mTraceUtilNr.getArfcn(GnbProtocol.CellId.FOURTH));
                                        MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setArfcn(GnbProtocol.CellId.FOURTH, value);
                                        MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setPci(GnbProtocol.CellId.FOURTH, pci);
                                    }
                                    autoArfcnIndex3++;
                                    if (autoArfcnIndex3 >= arfcnList.size())
                                        autoArfcnIndex3 = 0;
                                } else {
                                    AppLog.I("mHandler 3 change arfcn = " + value + " is different band, stop at first" + ", autoArfcnIndex = " + autoArfcnIndex3);
                                    restart4 = true;
                                    String deviceId = MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId();
                                    MessageController.build().setCmdAndCellID(deviceId, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FOURTH);
                                    PaCtl.build().closePAByArfcn(deviceId, true, mTraceUtilNr.getArfcn(GnbProtocol.CellId.FOURTH));
                                    MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setArfcn(GnbProtocol.CellId.FOURTH, value);
                                    MainActivity.getInstance().getDeviceList().get(0).getTraceUtil().setPci(GnbProtocol.CellId.FOURTH, pci);
                                    autoArfcnIndex3++;
                                    if (autoArfcnIndex3 >= arfcnList.size())
                                        autoArfcnIndex3 = 0;
                                    return;
                                }
                                mTraceUtilNr.setArfcn(GnbProtocol.CellId.FOURTH, value); //更新当前配置的值
                                mTraceUtilNr.setPci(GnbProtocol.CellId.FOURTH, pci); //更新当前配置的值
                                break;
                            }
                        }
                    }
                    if (time_count == 0) mHandler.sendEmptyMessageDelayed(14, 10 * 1000L);
                    else mHandler.sendEmptyMessageDelayed(14, time_count * 1000L);
                    break;
            }
        }
    };


    /**
     * 更新侧边栏状态、报值页信息
     *
     * @param type
     * @param cell_id
     * @param imsi
     * @param arfcn
     * @param pci
     * @param bandwidth
     */
    public void freshDoWorkState(int type, int cell_id, String imsi, String arfcn, String pci,
                                 int bandwidth) {
        AppLog.D("freshDoWorkState type = " + type + ", cell_id = " + cell_id + ", imsi = " + imsi + ", arfcn = " + arfcn + ", pci = " + pci);
        setProgress(type, 100, cell_id, getString(R.string.traceing), false);
        if (!imsi.isEmpty()) {
            //imsi = imsi.substring(0, 5) + "****" + imsi.substring(11);
            mTraceChildFragment.setConfigInfo(cell_id, type, arfcn, pci, imsi, bandwidth);
        }
    }

    public List<ImsiBean> getDataList() {
        return mImsiList;
    }

    public void onFreqScanRsp(String id, GnbFreqScanRsp rsp) {
        if (mFreqDialog != null) mFreqDialog.onFreqScanRsp(id, rsp);
    }

    //没用的
    //自动定位
    //扫频走完开始定位，使用扫出来的pci算法
    @Override
    public void onTraceConfig() {
        AppLog.D("onTraceConfig");
        autoArfcnIndex0 = 0;
        autoArfcnIndex1 = 0;
        autoArfcnIndex2 = 0;
        autoArfcnIndex3 = 0;

        restart1 = false;
        restart2 = false;
        restart3 = false;
        restart4 = false;

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

        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
            PaCtl.build().closePA(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId());
            boolean isAirSync = PrefUtil.build().getValue("sync_mode", "Air").toString().equals("Air");
            int enableAir = isAirSync ? 1 : 0;
            long timeMillis = System.currentTimeMillis();
            mTraceUtilNr.setAtCmdTimeOut(GnbProtocol.CellId.FIRST, timeMillis);
            mTraceUtilNr.setAtCmdTimeOut(GnbProtocol.CellId.SECOND, timeMillis);
            mTraceUtilNr.setAtCmdTimeOut(GnbProtocol.CellId.THIRD, timeMillis);
            mTraceUtilNr.setAtCmdTimeOut(GnbProtocol.CellId.FOURTH, timeMillis);

            //设置配置信息
            // 通道一
            boolean runCell1 = false;
            boolean runCell2 = false;
            boolean runCell3 = false;
            boolean runCell4 = false;
            String arfcn_cfg = "";
            String pci_cfg = "";
            LinkedList<ArfcnPciBean> td1 = arfcnBeanHashMap.get("TD1");
            if (td1 != null) {
                if (td1.size() > 0) arfcn_cfg = String.valueOf(td1.get(0));
                //拿到pci计算
                mTraceUtilNr.setAirSync(GnbProtocol.CellId.FIRST, enableAir);
                if (!arfcn_cfg.isEmpty()) {
                    int band = NrBand.earfcn2band(Integer.parseInt(arfcn_cfg));
                    mTraceUtilNr.setBandWidth(GnbProtocol.CellId.FIRST, 20);
                    mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.FIRST, 255);
                    mTraceUtilNr.setPlmn(GnbProtocol.CellId.FIRST, band == 78 ? "46011" : "46015");
                    runCell1 = true;
                }
                mTraceUtilNr.setArfcn(GnbProtocol.CellId.FIRST, arfcn_cfg);
                mTraceUtilNr.setPci(GnbProtocol.CellId.FIRST, pci_cfg);
                if (!auto_trace_imsi.isEmpty())
                    mTraceUtilNr.setImsi(GnbProtocol.CellId.FIRST, auto_trace_imsi);
            }


            // 通道二
            arfcn_cfg = "";
            LinkedList<ArfcnPciBean> td2 = arfcnBeanHashMap.get("TD2");
            if (td2 != null) {
                if (td2.size() > 0) arfcn_cfg = String.valueOf(td2.get(0));

                if (!arfcn_cfg.isEmpty()) runCell2 = true;
                pci_cfg = PrefUtil.build().getValue("PCI_TD2", "501").toString();
                mTraceUtilNr.setAirSync(GnbProtocol.CellId.SECOND, enableAir);
                int band1 = NrBand.earfcn2band(Integer.parseInt(arfcn_cfg));
                mTraceUtilNr.setBandWidth(GnbProtocol.CellId.SECOND, 5);
                mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.SECOND, 128);
                String plmn;
                if (band1 == 3) {
                    int freq = LteBand.earfcn2freq(Integer.parseInt(arfcn_cfg));
                    if (((freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949)))
                        plmn = "46015";
                    else plmn = "46011";
                } else plmn = band1 == 5 ? "46011" : "46015";
                mTraceUtilNr.setPlmn(GnbProtocol.CellId.SECOND, plmn);
                mTraceUtilNr.setArfcn(GnbProtocol.CellId.SECOND, arfcn_cfg);
                mTraceUtilNr.setPci(GnbProtocol.CellId.SECOND, pci_cfg);
                if (!auto_trace_imsi.isEmpty())
                    mTraceUtilNr.setImsi(GnbProtocol.CellId.SECOND, auto_trace_imsi);
            }

            // 通道三
            arfcn_cfg = "";
            LinkedList<ArfcnPciBean> td3 = arfcnBeanHashMap.get("TD3");
            if (td3 != null) {
                if (td3.size() > 0) arfcn_cfg = String.valueOf(td3.get(0));

                pci_cfg = PrefUtil.build().getValue("PCI_TD3", "1002").toString();
                mTraceUtilNr.setAirSync(GnbProtocol.CellId.THIRD, enableAir);
                if (!arfcn_cfg.isEmpty()) {
                    int band2 = Integer.parseInt(arfcn_cfg) > 100000 ? NrBand.earfcn2band(Integer.parseInt(arfcn_cfg)) : LteBand.earfcn2band(Integer.parseInt(arfcn_cfg));
                    mTraceUtilNr.setBandWidth(GnbProtocol.CellId.THIRD, Integer.parseInt(arfcn_cfg) > 100000 ? 20 : 5);
                    mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.THIRD, Integer.parseInt(arfcn_cfg) > 100000 ? 240 : 128);
                    mTraceUtilNr.setPlmn(GnbProtocol.CellId.THIRD, band2 == 28 ? "46000" : "46001");
                    runCell3 = true;
                }
                mTraceUtilNr.setArfcn(GnbProtocol.CellId.THIRD, arfcn_cfg);
                mTraceUtilNr.setPci(GnbProtocol.CellId.THIRD, pci_cfg);
                if (!auto_trace_imsi.isEmpty())
                    mTraceUtilNr.setImsi(GnbProtocol.CellId.THIRD, auto_trace_imsi);
            }

            // 通道四
            arfcn_cfg = "";
            LinkedList<ArfcnPciBean> td4 = arfcnBeanHashMap.get("TD4");
            if (td4 != null) {
                if (td4.size() > 0) arfcn_cfg = String.valueOf(td4.get(0));

                if (!arfcn_cfg.isEmpty()) {
                    int band3 = NrBand.earfcn2band(Integer.parseInt(arfcn_cfg));
                    int freq = LteBand.earfcn2freq(Integer.parseInt(arfcn_cfg));
                    //B34\39\40\41\次B3
                    if ((freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949)
                            || band3 == 34 || band3 == 39 || band3 == 40 || band3 == 41)
                        mTraceUtilNr.setPlmn(GnbProtocol.CellId.FOURTH, "46000");
                    else mTraceUtilNr.setPlmn(GnbProtocol.CellId.FOURTH, "46001");
                    runCell4 = true;
                }
                pci_cfg = PrefUtil.build().getValue("PCI_TD4", "502").toString();
                mTraceUtilNr.setAirSync(GnbProtocol.CellId.FOURTH, enableAir);
                mTraceUtilNr.setBandWidth(GnbProtocol.CellId.FOURTH, 5);
                mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.FOURTH, 128);

                mTraceUtilNr.setArfcn(GnbProtocol.CellId.FOURTH, arfcn_cfg);
                mTraceUtilNr.setPci(GnbProtocol.CellId.FOURTH, pci_cfg);
                if (!auto_trace_imsi.isEmpty())
                    mTraceUtilNr.setImsi(GnbProtocol.CellId.FOURTH, auto_trace_imsi);
            }

            MainActivity.getInstance().getDeviceList().get(i).setTraceUtil(mTraceUtilNr);

            final List<UeidBean> blackList = new ArrayList<>();
            for (MyUeidBean bean : MainActivity.getInstance().getBlackList())
                blackList.add(bean.getUeidBean());

            if (MainActivity.getInstance().getDeviceList().size() <= 0) {
                Util.showToast(getString(R.string.dev_not_online));
                return;
            } else if (!(runCell1 || runCell2 || runCell3 || runCell4)) {
                Util.showToast(getString(R.string.no_cfg));
                return;
            }

            //存入历史记录
            storeAutoInfoIntoHistory(td1, td2, td3, td4);

            AppLog.I("onTraceConfig() 定位启动 = " + i);
            // 第一步，配置黑名单
            if (runCell1) {
                MessageController.build().setBlackList(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId(), false, GnbProtocol.CellId.FIRST,
                        blackList.size(), blackList);
                MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.BLACKLIST);
            }

            int finalI = i;
            if (runCell2) {
                new Handler().postDelayed(() -> {
                    MessageController.build().setBlackList(MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDeviceId(), true, GnbProtocol.CellId.SECOND,
                            blackList.size(), blackList);
                    MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.BLACKLIST);
                }, 500);
            }
            if (runCell3) {
                new Handler().postDelayed(() -> {
                    MessageController.build().setBlackList(MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDeviceId(), false, GnbProtocol.CellId.THIRD,
                            blackList.size(), blackList);
                    MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.BLACKLIST);
                }, 1000);
            }

            if (runCell4) {
                new Handler().postDelayed(() -> {
                    MessageController.build().setBlackList(MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDeviceId(), true, GnbProtocol.CellId.FOURTH,
                            blackList.size(), blackList);
                    MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.BLACKLIST);
                }, 1500);
            }
        }

        mCfgTraceChildFragment.setcfgEnable(-1, false);
        //延迟200ms防止切换失败
//        new Handler().postDelayed(() -> {

//        }, 200);
        refreshTraceBtn();
        Util.showToast(getString(R.string.cfging_trace));
    }

    //将当前的数据存入历史记录
    private void storeIntoHistory() {
        try {
            JSONArray jsonArray = new JSONArray();

            HistoryBean historyBean = mCfgTraceChildFragment.getHistoryBean();
            //通道一
            JSONArray arfcnJsonArray = new JSONArray();
            JSONObject jsonObject1 = new JSONObject();
            for (int i = 0; i < historyBean.getTD1().size(); i++)
                arfcnJsonArray.put(historyBean.getTD1().get(i));
            jsonObject1.put("arfcnJsonArray", arfcnJsonArray);
            jsonObject1.put("imsi", mTraceUtilNr.getImsi(0));
            jsonArray.put(jsonObject1);

            //通道二
            arfcnJsonArray = new JSONArray();
            JSONObject jsonObject2 = new JSONObject();
            for (int i = 0; i < historyBean.getTD2().size(); i++)
                arfcnJsonArray.put(historyBean.getTD2().get(i));
            jsonObject2.put("arfcnJsonArray", arfcnJsonArray);
            jsonObject2.put("imsi", mTraceUtilNr.getImsi(1));
            jsonArray.put(jsonObject2);

            //通道三
            arfcnJsonArray = new JSONArray();
            JSONObject jsonObject3 = new JSONObject();
            for (int i = 0; i < historyBean.getTD3().size(); i++)
                arfcnJsonArray.put(historyBean.getTD3().get(i));
            jsonObject3.put("arfcnJsonArray", arfcnJsonArray);
            jsonObject3.put("imsi", mTraceUtilNr.getImsi(2));
            jsonArray.put(jsonObject3);

            //通道四
            arfcnJsonArray = new JSONArray();
            JSONObject jsonObject4 = new JSONObject();
            for (int i = 0; i < historyBean.getTD4().size(); i++)
                arfcnJsonArray.put(historyBean.getTD4().get(i));
            jsonObject4.put("arfcnJsonArray", arfcnJsonArray);
            jsonObject4.put("imsi", mTraceUtilNr.getImsi(3));
            jsonArray.put(jsonObject4);

            //时间
            jsonArray.put(DateUtil.getCurrentTime());

            long result = HistoryDBUtil.insertTraceCfgToDB(jsonArray);
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
    (LinkedList<ArfcnPciBean> td1, LinkedList<ArfcnPciBean> td2, LinkedList<ArfcnPciBean> td3, LinkedList<ArfcnPciBean> td4) {
        if (td1 != null || td2 != null || td3 != null || td4 != null) {
            try {
                JSONArray jsonArray = new JSONArray();

                //通道一
                JSONArray arfcnJsonArray = new JSONArray();
                JSONObject jsonObject1 = new JSONObject();
                if (td1 != null) {
                    for (int i = 0; i < td1.size(); i++)
                        arfcnJsonArray.put(td1.get(i));
                    jsonObject1.put("arfcnJsonArray", arfcnJsonArray);
                } else jsonObject1.put("arfcnJsonArray", "[]");
                jsonObject1.put("pci", mTraceUtilNr.getPci(0));
                jsonObject1.put("imsi", mTraceUtilNr.getImsi(0));
                jsonArray.put(jsonObject1);

                //通道二
                arfcnJsonArray = new JSONArray();
                JSONObject jsonObject2 = new JSONObject();
                if (td2 != null) {
                    for (int i = 0; i < td2.size(); i++)
                        arfcnJsonArray.put(td2.get(i));
                    jsonObject2.put("arfcnJsonArray", arfcnJsonArray);
                } else jsonObject2.put("arfcnJsonArray", "[]");
                jsonObject2.put("pci", mTraceUtilNr.getPci(1));
                jsonObject2.put("imsi", mTraceUtilNr.getImsi(1));
                jsonArray.put(jsonObject2);

                //通道三
                arfcnJsonArray = new JSONArray();
                JSONObject jsonObject3 = new JSONObject();
                if (td3 != null) {
                    for (int i = 0; i < td3.size(); i++)
                        arfcnJsonArray.put(td3.get(i));
                    jsonObject3.put("arfcnJsonArray", arfcnJsonArray);
                } else jsonObject3.put("arfcnJsonArray", "[]");
                jsonObject3.put("pci", mTraceUtilNr.getPci(2));
                jsonObject3.put("imsi", mTraceUtilNr.getImsi(2));
                jsonArray.put(jsonObject3);

                //通道四
                arfcnJsonArray = new JSONArray();
                JSONObject jsonObject4 = new JSONObject();
                if (td4 != null) {
                    for (int i = 0; i < td4.size(); i++)
                        arfcnJsonArray.put(td4.get(i));
                    jsonObject4.put("arfcnJsonArray", arfcnJsonArray);
                } else jsonObject4.put("arfcnJsonArray", "[]");
                jsonObject4.put("pci", mTraceUtilNr.getPci(3));
                jsonObject4.put("imsi", mTraceUtilNr.getImsi(3));
                jsonArray.put(jsonObject4);

                //时间
                jsonArray.put(DateUtil.getCurrentTime());

                long result = HistoryDBUtil.insertTraceCfgToDB(jsonArray);
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

    Handler mainHandler = new Handler(Looper.getMainLooper());
    final Object lockB40 = new Object();

    final Object lockB3 = new Object();
    int mainB40Success = -1;        //-1主B40未执行，0主B40执行失败,1主B40执行成功
    int mainB3Success = -1;        //-1主B40未执行，0主B40执行失败,1主B40执行成功
    boolean isMainB40 = false;  //在使用时要置为false
    boolean isMainB3 = false;  //在使用时要置为false

    private void startRunWork() {
        AppLog.D("startRunWork");
        boolean isAirSync = PrefUtil.build().getValue("sync_mode", "Air").toString().equals("Air");
        autoArfcnIndex0 = 0;
        autoArfcnIndex1 = 0;
        autoArfcnIndex2 = 0;
        autoArfcnIndex3 = 0;

        restart1 = false;
        restart2 = false;
        restart3 = false;
        restart4 = false;

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

        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
            String name = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName();
            boolean isLte = name.contains(devB);
            long timeMillis = System.currentTimeMillis();

            mTraceUtilNr.setAirSync(GnbProtocol.CellId.FIRST, isAirSync ? 1 : 0);
            mTraceUtilNr.setAirSync(GnbProtocol.CellId.SECOND, isAirSync ? 1 : 0);
            mTraceUtilNr.setAirSync(GnbProtocol.CellId.THIRD, isAirSync ? 1 : 0);
            mTraceUtilNr.setAirSync(GnbProtocol.CellId.FOURTH, isAirSync ? 1 : 0);

            mTraceUtilNr.setAtCmdTimeOut(GnbProtocol.CellId.FIRST, timeMillis);
            mTraceUtilNr.setAtCmdTimeOut(GnbProtocol.CellId.SECOND, timeMillis);
            mTraceUtilNr.setAtCmdTimeOut(GnbProtocol.CellId.THIRD, timeMillis);
            mTraceUtilNr.setAtCmdTimeOut(GnbProtocol.CellId.FOURTH, timeMillis);

            MainActivity.getInstance().getDeviceList().get(i).setTraceUtil(mTraceUtilNr);


            String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
            PaCtl.build().closePA(id);
            AtomicInteger delay = new AtomicInteger();
            if (!mTraceUtilNr.getImsi(0).isEmpty() && arfcnBeanHashMap.get("TD1").size() > 0) {
                runCell0(id);
                delay.set(500);
            }
            if (!mTraceUtilNr.getImsi(1).isEmpty() && arfcnBeanHashMap.get("TD2").size() > 0) {
                int isSecondB40 = 0;
                if (PaCtl.build().isB97502) {
                    ArfcnPciBean bean = arfcnBeanHashMap.get("TD2").get(0);
                    if (bean.getArfcn().isEmpty()) isSecondB40 = -1;
                    else if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 40)
                        isSecondB40 = 1;
                }
                if (isSecondB40 != 1) {      //直接执行
                    runCell1(id, 500);
                    delay.addAndGet(500);
                }
                if (isSecondB40 == 1) {    //次B40等待信号
                    // 在子线程中执行等待信号的逻辑
                    new Thread(() -> {
                        synchronized (lockB40) { // 使用共享的锁对象
                            try {
                                AppLog.D("SecondB40 is waiting");
                                lockB40.wait(); // 子线程等待信号
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        // 收到信号后，在主线程执行任务
                        mainHandler.post(() -> {
                            runCell1(id, 500);  //确保通道四完成之后再执行
                            delay.addAndGet(500);
                        });
                    }).start();
                }
            }
            if (!mTraceUtilNr.getImsi(2).isEmpty() && arfcnBeanHashMap.get("TD3").size() > 0) {
                runCell2(id, delay.get());
                delay.addAndGet(500);
            }
            if (!mTraceUtilNr.getImsi(3).isEmpty() && arfcnBeanHashMap.get("TD4").size() > 0) {
                int isSecondB3 = 0;
                if (PaCtl.build().isB97502) {
                    ArfcnPciBean bean = arfcnBeanHashMap.get("TD4").get(0);
                    if (bean.getArfcn().isEmpty()) isSecondB3 = -1;
                    else if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 3)
                        isSecondB3 = 1;
                }
                if (isSecondB3 != 1) {      //直接执行
                    runCell3(id, delay.get());
                    delay.addAndGet(500);
                }
                if (isSecondB3 == 1) {    //次B40等待信号
                    // 在子线程中执行等待信号的逻辑
                    new Thread(() -> {
                        synchronized (lockB3) { // 使用共享的锁对象
                            try {
                                AppLog.D("SecondB3 is waiting");
                                lockB3.wait(); // 子线程等待信号
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        // 收到信号后，在主线程执行任务
                        mainHandler.post(() -> {
                            runCell3(id, 500);
                            delay.addAndGet(500);
                        });
                    }).start();
                }
            }
            if (delay.get() == 0) {
                Util.showToast(getString(R.string.no_imsi));
                return;
            }
        }
    }

    private void runCell0(String id) {
        updateParamAndStart(id, arfcnBeanHashMap.get("TD1").get(0), GnbProtocol.CellId.FIRST);
        freqEndGoRunWork();
    }

    private void runCell1(String id, int delay) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ArfcnPciBean bean = arfcnBeanHashMap.get("TD2").get(0);
                //如果主B40失败跳过次B40
                if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 40 && mainB40Success == 0) {
                    if (arfcnBeanHashMap.get("TD2").size() == 1) {   //只有一个频点
                        setProgress(1, 0, 1, getString(R.string.open_fail), true);
                        int indexById = MainActivity.getInstance().getIndexById(id);
                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(1, GnbBean.State.IDLE);
                        MainActivity.getInstance().updateSteps(1, StepBean.State.fail, getString(R.string.open_fail));
                        Util.showToast(getString(R.string.cell) + "二次频点开启失败", Toast.LENGTH_LONG);
                        refreshTraceBtn();
                        return;
                    } else {    //从下一个频点开始
                        autoArfcnIndex0++;
                        bean = arfcnBeanHashMap.get("TD2").get(1);
                    }
                }

                //判断是否是主B3
                if (bean.getArfcn().length() < 6 && LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 3) {
                    isMainB3 = true;
                }
                updateParamAndStart(id, bean, GnbProtocol.CellId.SECOND);
                if (delay == 0) freqEndGoRunWork();
            }
        }, delay);
    }

    private void runCell2(String id, int delay) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateParamAndStart(id, arfcnBeanHashMap.get("TD3").get(0), GnbProtocol.CellId.THIRD);
                if (delay == 0) freqEndGoRunWork();
            }
        }, delay);
    }

    private void runCell3(String id, int delay) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ArfcnPciBean bean = arfcnBeanHashMap.get("TD4").get(0);
                //如果主B3失败跳过次B3
                if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 3 && mainB40Success == 0) {
                    if (arfcnBeanHashMap.get("TD4").size() == 1) {   //只有一个频点
                        setProgress(1, 0, 1, getString(R.string.open_fail), true);
                        int indexById = MainActivity.getInstance().getIndexById(id);
                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(3, GnbBean.State.IDLE);
                        MainActivity.getInstance().updateSteps(1, StepBean.State.fail, getString(R.string.open_fail));
                        Util.showToast(getString(R.string.cell) + "四次频点开启失败", Toast.LENGTH_LONG);
                        refreshTraceBtn();
                        return;
                    } else {    //从下一个频点开始
                        autoArfcnIndex3++;
                        bean = arfcnBeanHashMap.get("TD4").get(1);
                    }
                }

                //判断是否是主B40
                if (bean.getArfcn().length() < 6 && LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 40) {
                    isMainB40 = true;
                }
                updateParamAndStart(id, bean, GnbProtocol.CellId.FOURTH);
                if (delay == 0) freqEndGoRunWork();
            }
        }, delay);
    }

    public void freqEndGoRunWork() {
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
        mCfgTraceChildFragment.resetArfcnList();
    }

    public void setIfDebug(boolean b) {
        mCatchChildFragment.setTv_add_testVisibility(b);
        mCfgTraceChildFragment.setbtn_testVisibility(b);

    }


    public boolean isStartCatchHandler1() {
        return isStartCatchHandler1;
    }

    public boolean isStartCatchHandler2() {
        return isStartCatchHandler2;
    }

    public boolean isStartCatchHandler3() {
        return isStartCatchHandler3;
    }

    public boolean isStartCatchHandler4() {
        return isStartCatchHandler4;
    }

    public boolean hasStartCatchHandlerTrue() {
        return isStartCatchHandler1 || isStartCatchHandler2 || isStartCatchHandler3 || isStartCatchHandler4;
    }


}