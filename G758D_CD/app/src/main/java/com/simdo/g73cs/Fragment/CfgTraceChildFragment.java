package com.simdo.g73cs.Fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.Group;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.Gnb.Bean.GnbProtocol;
import com.simdo.g73cs.Adapter.AutoSearchAdapter;
import com.simdo.g73cs.Adapter.DualRecyclerviewAdapter;
import com.simdo.g73cs.Adapter.FreqResultListAdapter;
import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.HistoryBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Dialog.CustomDialog;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DBUtil;
import com.simdo.g73cs.Util.DividerItemDecoration;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CfgTraceChildFragment extends Fragment implements ListItemListener, DualRecyclerviewAdapter.OnDelArfcnListListener {

    Context mContext;
    private final TraceCatchFragment mTraceCatchFragment;
    private final SettingFragment settingFragment;
    private FreqFragment freqFragment;
    //是否频点轮询
    private boolean isCell1SetArfcnChange, isCell2SetArfcnChange, isCell3SetArfcnChange, isCell4SetArfcnChange;
    ImageView iv_instance;
    TextView tv_do_btn;
    RecyclerView rv_arfcn_1, rv_arfcn_2, rv_arfcn_3, rv_arfcn_4;
    TextView tv_import_1;
    TextView tv_import_2;
    TextView tv_import_3;
    TextView tv_import_4;
    Button btn_test;
    public boolean isAutoMode = false;   //是否自动模式


    public boolean isCloseDistance = false;   //是否近距
    public boolean isAutoModeFreqRunning = false;   //自动模式是否在运行

    private List<ScanArfcnBean> cfgTraceChildFreqList = new ArrayList<>();


    public CfgTraceChildFragment(Context context, TraceCatchFragment traceCatchFragment) {
        this.mContext = context;
        this.mTraceCatchFragment = traceCatchFragment;
        settingFragment = new SettingFragment(mContext);        //能new一个吗？
    }

    public CfgTraceChildFragment(Context context, TraceCatchFragment traceCatchFragment, FreqFragment freqFragment) {
        this.mContext = context;
        this.mTraceCatchFragment = traceCatchFragment;
        this.freqFragment = freqFragment;
        settingFragment = new SettingFragment(mContext);        //能new一个吗？
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.I("CatchChildFragment onCreate");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.I("CatchChildFragment onCreateView");

        View root = inflater.inflate(R.layout.child_pager_cfg, container, false);

        initView(root);
        return root;
    }

    AutoCompleteTextView actv_imsi, actv_imsi_2, actv_imsi_3, actv_imsi_4;
    //    EditText ed_arfcn, ed_arfcn_2, ed_arfcn_3, ed_arfcn_4;
    private List<String> dropImsiList = new ArrayList<>();  //imsi历史列表

    public AutoSearchAdapter getDropImsiAdapter() {
        return dropImsiAdapter;
    }

    private AutoSearchAdapter dropImsiAdapter;
    Group trace_group_1, trace_group_2, trace_group_3, trace_group_4;

    boolean isChange1 = false, isChange2 = false, isChange3 = false, isChange4 = false;

    DualRecyclerviewAdapter TD1_adapter, TD2_adapter, TD3_adapter, TD4_adapter;

    Switch sw_auto_trace;
    AutoCompleteTextView ed_auto_imsi;
    TextView tv_switch_text_cfg, tv_switch_text_auto;
    NestedScrollView sv_cfg_trace;
    LinearLayout ll_auto_trace;
    private final int minDelta = 200;           // threshold in ms
    private long focusTime = 0;                 // time of last touch
    private View focusTarget = null;

    private boolean isChanging = true;      //是否是在修改最新的值，为false说明此值已经添加过，点击添加按钮时需要重新判断

    //修复失焦问题
    View.OnFocusChangeListener onFocusChangeListener = (view, hasFocus) -> {
        long t = System.currentTimeMillis();
        long delta = t - focusTime;
        if (hasFocus) {     // gained focus
            if (delta > minDelta) {
                focusTime = t;
                focusTarget = view;
            }
        } else {              // lost focus
            if (delta <= minDelta && view == focusTarget) {
                focusTarget.postDelayed(() ->
                        focusTarget.requestFocus(), 1000);

            }
        }
    };

    private void initView(View root) {
        dropImsiList.clear();
        dropImsiList = PrefUtil.build().getDropImsiList();

        btn_test = root.findViewById(R.id.btn_test);
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

                    arfcnList.append("\nTD1_adapter lastArfcn/pci：").append(TD1_adapter.getLastViewText());
                    arfcnList.append("\nTD2_adapter lastArfcn/pci：").append(TD2_adapter.getLastViewText());
                    arfcnList.append("\nTD3_adapter lastArfcn/pci：").append(TD3_adapter.getLastViewText());
                    arfcnList.append("\nTD4_adapter lastArfcn/pci：").append(TD4_adapter.getLastViewText());

                    //arfcnList.append("\nfreqList：").append(getFreqList());
                    arfcnList.append("\nisAutoMode：").append(isAutoMode);
                    arfcnList.append("\nmTraceUtilNr：cell1").append(mTraceCatchFragment.mTraceUtilNr.getArfcn(0));
                    arfcnList.append("\nmTraceUtilNr：cell2").append(mTraceCatchFragment.mTraceUtilNr.getArfcn(1));
                    arfcnList.append("\nmTraceUtilNr：cell3").append(mTraceCatchFragment.mTraceUtilNr.getArfcn(2));
                    arfcnList.append("\nmTraceUtilNr：cell4").append(mTraceCatchFragment.mTraceUtilNr.getArfcn(3));
                    arfcnList.append("\nhistoryBean TD1").append(historyBean.getTD1().toString());
                    arfcnList.append("\nhistoryBean TD2").append(historyBean.getTD2().toString());
                    arfcnList.append("\nhistoryBean TD3").append(historyBean.getTD3().toString());
                    arfcnList.append("\nhistoryBean TD4").append(historyBean.getTD4().toString());

                    arfcnList.append("\nisChanging :").append(isChanging());
//                    arfcnList.append("\n1350、3741 :").append(LteBand.earfcn2freq(1350)).append("、").append(LteBand.earfcn2freq(3741));

                    CustomDialog customDialog = new CustomDialog(mContext, arfcnList.toString());
                    customDialog.show();
                }
        );


        //第一次初始化去掉之前的数据
        settingFragment.cleanAllPrefArfcnList();

        //自动定位按钮
        ed_auto_imsi = root.findViewById(R.id.ed_auto_imsi);
        sw_auto_trace = root.findViewById(R.id.sw_auto_trace);
        tv_switch_text_cfg = root.findViewById(R.id.tv_switch_text_cfg);
        tv_switch_text_auto = root.findViewById(R.id.tv_switch_text_auto);
        sv_cfg_trace = root.findViewById(R.id.sv_cfg_trace);
        ll_auto_trace = root.findViewById(R.id.ll_auto_trace);
        sw_auto_trace.setOnClickListener(view -> {
            if (tv_do_btn.getText().toString().equals("结束")) {
                sw_auto_trace.setChecked(!sw_auto_trace.isChecked());
                Util.showToast("请先结束定位");
                return;
            }
            if (sw_auto_trace.isChecked()) {
                tv_switch_text_cfg.setVisibility(View.GONE);
                tv_switch_text_auto.setVisibility(View.VISIBLE);
                sv_cfg_trace.setVisibility(View.GONE);
                ll_auto_trace.setVisibility(View.VISIBLE);
            } else {
                tv_switch_text_cfg.setVisibility(View.VISIBLE);
                tv_switch_text_auto.setVisibility(View.GONE);
                sv_cfg_trace.setVisibility(View.VISIBLE);
                ll_auto_trace.setVisibility(View.GONE);
            }

        });

        // 统一隐藏组
        trace_group_1 = root.findViewById(R.id.trace_group_1);
        trace_group_2 = root.findViewById(R.id.trace_group_2);
        trace_group_3 = root.findViewById(R.id.trace_group_3);
        trace_group_4 = root.findViewById(R.id.trace_group_4);
        Spanned spanned = Html.fromHtml(getString(R.string.hint_code));
        TextView tv_cell_tip_1 = root.findViewById(R.id.tv_cell_tip_1);
        TextView tv_cell_tip_2 = root.findViewById(R.id.tv_cell_tip_2);
        TextView tv_cell_tip_3 = root.findViewById(R.id.tv_cell_tip_3);
        TextView tv_cell_tip_4 = root.findViewById(R.id.tv_cell_tip_4);
        if (PaCtl.build().isB97502) {
            tv_cell_tip_1.setText("(/N41/N78/N79)");
            tv_cell_tip_2.setText("(/B3/B5/B8/" + getString(R.string.order) + "B40)");
            tv_cell_tip_3.setText("(/B1/N1/N28)");
            tv_cell_tip_4.setText("(/B34/B39/B40/B41/" + getString(R.string.order) + "B3)");
        } else {
            tv_cell_tip_1.setText("(/N28/N78/N79)");
            tv_cell_tip_2.setText("(/B34/B39/B40/B41)");
            tv_cell_tip_3.setText("(/N1/N41)");
            tv_cell_tip_4.setText("(/B1/B3/B5/B8)");
        }

        actv_imsi = root.findViewById(R.id.actv_imsi);
        actv_imsi_2 = root.findViewById(R.id.actv_imsi_2);
        actv_imsi_3 = root.findViewById(R.id.actv_imsi_3);
        actv_imsi_4 = root.findViewById(R.id.actv_imsi_4);

        dropImsiAdapter = new AutoSearchAdapter(mContext, dropImsiList);
        actv_imsi.setAdapter(dropImsiAdapter);
        actv_imsi_2.setAdapter(dropImsiAdapter);
        actv_imsi_3.setAdapter(dropImsiAdapter);
        actv_imsi_4.setAdapter(dropImsiAdapter);
        ed_auto_imsi.setAdapter(dropImsiAdapter);

        root.findViewById(R.id.tv_down_same).setOnClickListener(view -> {
            String string = actv_imsi.getText().toString();
            actv_imsi_2.setText(string);
            actv_imsi_3.setText(string);
            actv_imsi_4.setText(string);
        });
        root.findViewById(R.id.tv_up_same_2).setOnClickListener(view -> actv_imsi_2.setText(actv_imsi.getText().toString()));
        root.findViewById(R.id.tv_up_same_3).setOnClickListener(view -> actv_imsi_3.setText(actv_imsi_2.getText().toString()));
        root.findViewById(R.id.tv_up_same_4).setOnClickListener(view -> actv_imsi_4.setText(actv_imsi_3.getText().toString()));

        historyBean = new HistoryBean(2, "");
        onHistoryItemClickListener(DBUtil.getLastTraceCfgToDB());

        TD1_adapter = new DualRecyclerviewAdapter(mContext, historyBean.getTD1(), "TD1", false);
        TD2_adapter = new DualRecyclerviewAdapter(mContext, historyBean.getTD2(), "TD2", false);
        TD3_adapter = new DualRecyclerviewAdapter(mContext, historyBean.getTD3(), "TD3", false);
        TD4_adapter = new DualRecyclerviewAdapter(mContext, historyBean.getTD4(), "TD4", false);


        TD1_adapter.setOnDelArfcnListListener(this);
        TD2_adapter.setOnDelArfcnListListener(this);
        TD3_adapter.setOnDelArfcnListListener(this);
        TD4_adapter.setOnDelArfcnListListener(this);

        rv_arfcn_1 = root.findViewById(R.id.rv_arfcn_1);
        rv_arfcn_1.setAdapter(TD1_adapter);
        rv_arfcn_1.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        rv_arfcn_1.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        root.findViewById(R.id.btn_add_1).setOnClickListener(v -> {
            btnAddData(v, TD1_adapter, historyBean.getTD1(), "TD1");
            TD1_adapter.showSoftInput(true);
        });

        rv_arfcn_2 = root.findViewById(R.id.rv_arfcn_2);
        rv_arfcn_2.setAdapter(TD2_adapter);
        rv_arfcn_2.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        rv_arfcn_2.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        root.findViewById(R.id.btn_add_2).setOnClickListener(v -> {
            btnAddData(v, TD2_adapter, historyBean.getTD2(), "TD2");
            TD2_adapter.showSoftInput(true);
        });

        rv_arfcn_3 = root.findViewById(R.id.rv_arfcn_3);
        rv_arfcn_3.setAdapter(TD3_adapter);
        rv_arfcn_3.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        rv_arfcn_3.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        root.findViewById(R.id.btn_add_3).setOnClickListener(v -> {
            btnAddData(v, TD3_adapter, historyBean.getTD3(), "TD3");
            TD3_adapter.showSoftInput(true);
        });

        rv_arfcn_4 = root.findViewById(R.id.rv_arfcn_4);
        rv_arfcn_4.setAdapter(TD4_adapter);
        rv_arfcn_4.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        rv_arfcn_4.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        root.findViewById(R.id.btn_add_4).setOnClickListener(v -> {
            btnAddData(v, TD4_adapter, historyBean.getTD4(), "TD4");
            TD4_adapter.showSoftInput(true);
        });

        tv_import_1 = root.findViewById(R.id.tv_import_1);
        tv_import_2 = root.findViewById(R.id.tv_import_2);
        tv_import_3 = root.findViewById(R.id.tv_import_3);
        tv_import_4 = root.findViewById(R.id.tv_import_4);

        tv_import_1.setOnClickListener(view -> showFreqListDialog(0));
        tv_import_2.setOnClickListener(view -> showFreqListDialog(1));
        tv_import_3.setOnClickListener(view -> showFreqListDialog(2));
        tv_import_4.setOnClickListener(view -> showFreqListDialog(3));

        // 近距增益按钮
        iv_instance = root.findViewById(R.id.iv_instance);
        iv_instance.setOnClickListener(v -> {
            List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
            if (deviceList.size() <= 0) {
                Util.showToast(getString(R.string.dev_offline));
                return;
            }
            if (mTraceCatchFragment.getBtnStr().equals(getString(R.string.open))) {
                Util.showToast(getString(R.string.no_work));
                return;
            }

            if (isCloseDistance) {
                iv_instance.setBackgroundResource(R.mipmap.icon_distance2_n);
                mTraceCatchFragment.doSetTxPwrOffset(0);
                isCloseDistance = false;
            } else {
                iv_instance.setBackgroundResource(R.mipmap.icon_distance2_h);
                mTraceCatchFragment.doSetTxPwrOffset(-2);
                isCloseDistance = true;
            }

        });

        //黑名单
        ImageView iv_black_list = root.findViewById(R.id.iv_black_list);
        iv_black_list.setOnClickListener(view -> mTraceCatchFragment.showBlackListDialog());


        // 开始、结束工作按钮
        tv_do_btn = root.findViewById(R.id.tv_do_btn);
        tv_do_btn.setOnClickListener(view -> {
            if (sw_auto_trace.isChecked()) clickAutoTrace();
            else mTraceCatchFragment.doWork("");
        });

        //初始化按钮
        tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
    }

    public void setbtn_testVisibility(boolean isVisible) {
        if (btn_test == null) return;
        btn_test.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void banLeadingZero(EditText ed_pci_n) {
        ed_pci_n.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input = editable.toString();
                if (input.length() > 1 && input.startsWith("0")) {
                    // 删除后续输入并设置新的光标位置
                    editable.delete(editable.length() - 1, editable.length());
                    ed_pci_n.setSelection(editable.length());
                }
            }
        });
    }

    private void clickAutoTrace() {
        String text = getTv_do_btn().getText().toString();
        if (text.equals(getString(R.string.open))) {
            List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
            if (deviceList.size() <= 0) {
                Util.showToast(getString(R.string.dev_offline));
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (DeviceInfoBean bean : deviceList) {
                switch (bean.getWorkState()) {
                    case GnbBean.State.FREQ_SCAN:
                    case GnbBean.State.NONE:
                        sb.append("-1");
                        break;
                    case GnbBean.State.IDLE:
                        sb.append("0");
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
            if (doStr.equals("00")) {
                autoTraceDialog();  // 两设备均在空闲状态下，启动定位
                return;
            }
            if (doStr.contains("0")) {
                autoTraceDialog(); // 单设备在空闲状态下，启动定位
            }
        } else if (text.equals(getString(R.string.stop))) {
            mTraceCatchFragment.stopTraceDialog();
        } else if (text.equals(getString(R.string.starting))) {
            Util.showToast(getString(R.string.starting_cannot_fast));
        } else if (text.equals(getString(R.string.stoping))) {
            Util.showToast(getString(R.string.stoping_cannot_fast));
        }
    }

    private void hideSoftKeyBoard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public TextView getTv_do_btn() {
        return tv_do_btn;
    }

    //设置定位配置页能否更改
    public void setcfgEnable(int cellId, boolean enable) {
        AppLog.D("setcfgEnable cellId = " + cellId + ", enable = " + enable);
        if (rv_arfcn_1 == null) return;
        if (cellId == -1) {
            setcfgEnable(10, enable);
            //自动定位输入框关闭
            ed_auto_imsi.setClickable(enable);
            ed_auto_imsi.setFocusable(enable);
            ed_auto_imsi.setFocusableInTouchMode(enable);

//            trace_group_all.setVisibility(enable ? View.VISIBLE : View.GONE);
            return;
        }
        if (cellId == 0 || cellId == 10) {
            TD1_adapter.setFocusable(enable);

            tv_import_1.setVisibility(enable ? View.VISIBLE : View.GONE);

            actv_imsi.setClickable(enable);
            actv_imsi.setFocusable(enable);
            actv_imsi.setFocusableInTouchMode(enable);

            trace_group_1.setVisibility(enable ? View.VISIBLE : View.GONE);

            if (cellId == 10) setcfgEnable(11, enable);
        } else if (cellId == 1 || cellId == 11) {
            TD2_adapter.setFocusable(enable);

            tv_import_2.setVisibility(enable ? View.VISIBLE : View.GONE);

            actv_imsi_2.setClickable(enable);
            actv_imsi_2.setFocusable(enable);
            actv_imsi_2.setFocusableInTouchMode(enable);

            trace_group_2.setVisibility(enable ? View.VISIBLE : View.GONE);

            if (cellId == 11) setcfgEnable(12, enable);
        } else if (cellId == 2 || cellId == 12) {
            TD3_adapter.setFocusable(enable);

            tv_import_3.setVisibility(enable ? View.VISIBLE : View.GONE);

            actv_imsi_3.setClickable(enable);
            actv_imsi_3.setFocusable(enable);
            actv_imsi_3.setFocusableInTouchMode(enable);

            trace_group_3.setVisibility(enable ? View.VISIBLE : View.GONE);

            if (cellId == 12) setcfgEnable(13, enable);
        } else if (cellId == 3 || cellId == 13) {
            TD4_adapter.setFocusable(enable);

            tv_import_4.setVisibility(enable ? View.VISIBLE : View.GONE);

            actv_imsi_4.setClickable(enable);
            actv_imsi_4.setFocusable(enable);
            actv_imsi_4.setFocusableInTouchMode(enable);

            trace_group_4.setVisibility(enable ? View.VISIBLE : View.GONE);
        }
    }

    //自动计算pci并将pci设置到页面
    public boolean importArfcn(ScanArfcnBean bean, int cell, int inPci) {
        int band;
        int arfcn = bean.getDl_arfcn();
        int pci = bean.getPci();
        boolean isOk = false;
        if (arfcn < 100000) {
            band = LteBand.earfcn2band(arfcn);
            // 4G PCI 算法： 公网pci % 3，取余 +- 1，如果为 0则+1， 2则-1， 1则+-1都可以
            if (pci % 3 == 2) pci -= 1;
            else pci += 1;

            if (PaCtl.build().isB97502) {
                switch (band) {
                    case 1:
                        if (cell == -1 || cell == 2) {
                            TD3_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                            isOk = true;
                        }
                        break;
                    case 3:
                    case 40:
                        if (cell == -1 || cell == 1) {
                            TD2_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                            isOk = true;
                        }
                        if (cell == -1 || cell == 3) {
                            TD4_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                            isOk = true;
                        }
                        break;
                    case 5:
                    case 8:
                        if (cell == -1 || cell == 1) {
                            TD2_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                            isOk = true;
                        }
                        break;
                    case 34:
                    case 39:
                    case 41:
                        if (cell == -1 || cell == 3) {
                            TD4_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                            isOk = true;
                        }
                        break;
                }
            } else {
                switch (band) {
                    case 1:
                    case 3:
                    case 5:
                    case 8:
                        if (cell == -1 || cell == 3) {
                            TD4_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                            isOk = true;
                        }
                        break;
                    case 34:
                    case 39:
                    case 40:
                    case 41:
                        if (cell == -1 || cell == 1) {
                            TD2_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                            isOk = true;
                        }
                        break;
                }
            }
        } else {
            band = NrBand.earfcn2band(arfcn);
            // 5G PCI 算法：
            // 若在方法调用传进来的pci不为0， 表示做处理了，这里直接取用。
            // 否则走 % 3 取余逻辑（若发现和公网有冲突，也不理了，谁让你特意选这么个玩意进来）
            if (inPci != 0) pci = inPci;
            else {
                if (pci % 3 == 2) pci -= 1;
                else pci += 1;
            }
            switch (band) {
                case 41:
                    if (PaCtl.build().isB97502) {
                        if (cell == -1 || cell == 0) {
                            TD1_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                            isOk = true;
                        }
                    } else {
                        if (cell == -1 || cell == 2) {
                            TD3_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                            isOk = true;
                        }
                    }
                    break;
                case 78:
                case 79:
                    if (cell == -1 || cell == 0) {
                        TD1_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                        isOk = true;
                    }
                    break;
                case 1:
                    if (cell == -1 || cell == 2) {
                        TD3_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                        isOk = true;
                    }
                    break;
                case 28:
                    if (PaCtl.build().isB97502) {
                        if (cell == -1 || cell == 2) {
                            TD3_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                            isOk = true;
                        }
                    } else {
                        if (cell == -1 || cell == 0) {
                            TD1_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                            isOk = true;
                        }
                    }
                    break;
            }
        }
        return isOk;
    }

    //自动计算pci并将pci设置到页面
    public int countPci(ScanArfcnBean bean, int cell, int inPci) {
        int band;
        int arfcn = bean.getDl_arfcn();
        int pci = bean.getPci();
        boolean isOk = false;
        if (arfcn < 100000) {
            band = LteBand.earfcn2band(arfcn);
            // 4G PCI 算法： 公网pci % 3，取余 +- 1，如果为 0则+1， 2则-1， 1则+-1都可以
            if (pci % 3 == 2) pci -= 1;
            else pci += 1;
        } else {
            band = NrBand.earfcn2band(arfcn);
            // 5G PCI 算法：
            // 若在方法调用传进来的pci不为0， 表示做处理了，这里直接取用。
            // 否则走 % 3 取余逻辑（若发现和公网有冲突，也不理了，谁让你特意选这么个玩意进来）
            if (inPci != 0) pci = inPci;
            else {
                if (pci % 3 == 2) pci -= 1;
                else pci += 1;
            }
        }
        return pci;
    }

    /*
     * 自动定位dialog
     *
     * */
    private void autoTraceDialog() {
        AppLog.D("autoTraceDialog()");
        String autoTraceIMSI = ed_auto_imsi.getEditableText().toString();
        if (autoTraceIMSI.length() != GnbProtocol.MAX_IMSI_USE_LEN) {
            Util.showToast(getString(R.string.imsi_err_tip));
            return;
        }
        if (!Util.existSameData(dropImsiList, autoTraceIMSI)) {
            if (dropImsiList.size() > 20) {
                dropImsiList.remove(0);
            }
            dropImsiList.add(autoTraceIMSI);
            PrefUtil.build().setDropImsiList(dropImsiList);
            dropImsiAdapter.setList(dropImsiList);
        }
        //开启定位
        isAutoMode = true;
        mTraceCatchFragment.doWork(autoTraceIMSI);
        MainActivity.getInstance().closeCustomDialog();
    }

    private void btnAddData(View v, DualRecyclerviewAdapter adapter, List<ArfcnPciBean> list, String key) {
        ArfcnPciBean lastArfcnPciBean = adapter.getLastViewText();
        if (lastArfcnPciBean == null) {
            adapter.addEditView();
            return;
        }
        String lastArfcn = lastArfcnPciBean.getArfcn();
        if (lastArfcn.isEmpty()) {
            showToast(mContext.getString(R.string.arfcn_empty_err));
            return;
        }

        String lastPci = lastArfcnPciBean.getPci();
        if (lastPci.isEmpty()) {
            showToast(mContext.getString(R.string.pci_empty_err));
            return;
        }
        int i_pci = Integer.parseInt(lastPci);

        if (!checkParam(lastArfcn, i_pci, key)) return;

        List<ArfcnPciBean> listDelete = new ArrayList<>(list);

        if (isChanging()) {
            //删除最后一个再比较
            listDelete.remove(listDelete.size() - 1);
        }

        //相同值检查
        for (ArfcnPciBean arfcnPciBean : listDelete) {
            if (arfcnPciBean.getArfcn().equals(lastArfcn) && arfcnPciBean.getPci().equals(lastPci)) {
                Util.showToast("已存在相同数据值，请修改");
                return;
            }
        }

        //通过相同判断
        list.clear();
        list.addAll(listDelete);
        if (!isChanging()) {
            list.remove(list.size() - 1);
        }

//        adapter.addData(lastArfcnPciBean);
        list.add(lastArfcnPciBean);
        adapter.addEditView();
        adapter.setClickAddBtn(true);   //点击添加按钮后点击删除按钮需要把最后一个空的删了
//        adapter.setLastArfcnPciBeanNull();
        adapter.notifyDataSetChanged();
        setIsChanging(false);
    }

    HistoryBean historyBean;

    public boolean checkLastParam() {
        AppLog.I("checkLastParam()");
        ArfcnPciBean lastBean1 = TD1_adapter.getLastViewText();
        String imsi = actv_imsi.getText().toString();
        if (!imsi.isEmpty() && imsi.length() != 15) {
            Util.showToast(mContext.getString(R.string.cell_0_imsi_err));
            return false;
        }
        if (!Util.existSameData(dropImsiList, imsi)) {
            if (dropImsiList.size() > 20) {
                dropImsiList.remove(0);
            }
            dropImsiList.add(imsi);
            PrefUtil.build().setDropImsiList(dropImsiList);
            dropImsiAdapter.setList(dropImsiList);
        }
        historyBean.setImsiFirst(imsi);
        if (lastBean1 != null) {
            if (!lastBean1.getArfcn().isEmpty() && !lastBean1.getPci().isEmpty()) {
                if (!checkParam(lastBean1.getArfcn(), Integer.parseInt(lastBean1.getPci()), "TD1")) {
                    if (!isChanging()) { //点了添加按钮要把最后那个空的删了
                        historyBean.getTD1().remove(historyBean.getTD1().size() - 1);
                        isChanging = true;
                        historyBean.getTD1().add(new ArfcnPciBean(lastBean1.getArfcn(), lastBean1.getPci()));
                    }
                    return false;
                }
                //点击添加按钮输入新的之后需要先添加进去再比较
                //判断是否重复
                List<ArfcnPciBean> list = historyBean.getTD1();
                List<ArfcnPciBean> listDelete = new ArrayList<>(list);
//                if (isChanging()) {
                //删除最后一个再比较
                listDelete.remove(listDelete.size() - 1);
//                }

                //相同值检查
                for (ArfcnPciBean arfcnPciBean : listDelete) {
                    if (arfcnPciBean.getArfcn().equals(lastBean1.getArfcn()) && arfcnPciBean.getPci().equals(lastBean1.getPci())) {
                        Util.showToast("已存在相同数据值，请修改");
                        if (!isChanging()) { //点了添加按钮要把最后那个空的删了
                            historyBean.getTD1().remove(historyBean.getTD1().size() - 1);
                            isChanging = true;
                            historyBean.getTD1().add(new ArfcnPciBean(lastBean1.getArfcn(), lastBean1.getPci()));
                        }
                        return false;
                    }
                }
                //通过相同判断
                list.clear();
                list.addAll(listDelete);

                list.add(lastBean1);
                TD1_adapter.notifyDataSetChanged();
            } else if (historyBean.getTD1().size() > 0) {   //如果没填全直接去掉
                historyBean.getTD1().removeLast();
            }
            setIsChanging(true);        //为了下次点击添加的时候能够识别为会被修改的，不论是否为空
        }
        while (historyBean.getTD1().size() > 0 && historyBean.getTD1().get(0).getArfcn().isEmpty())
            historyBean.getTD1().remove(0);


        ArfcnPciBean bean2 = TD2_adapter.getLastViewText();
        imsi = actv_imsi_2.getText().toString();
        if (!imsi.isEmpty() && imsi.length() != 15) {
            Util.showToast(mContext.getString(R.string.cell_1_imsi_err));
            return false;
        }
        if (!Util.existSameData(dropImsiList, imsi)) {
            if (dropImsiList.size() > 20) {
                dropImsiList.remove(0);
            }
            dropImsiList.add(imsi);
            PrefUtil.build().setDropImsiList(dropImsiList);
            dropImsiAdapter.setList(dropImsiList);
        }
        historyBean.setImsiSecond(imsi);
        if (bean2 != null) {
            if (!bean2.getArfcn().isEmpty() && !bean2.getPci().isEmpty()) {
                if (!checkParam(bean2.getArfcn(), Integer.parseInt(bean2.getPci()), "TD2")) {
                    //如果在这里或下面return了会导致最后一个消失，先试试和删除一样把他加回去
                    if (!isChanging()) { //点了添加按钮要把最后那个空的删了
                        historyBean.getTD2().remove(historyBean.getTD2().size() - 1);
                        isChanging = true;
                        historyBean.getTD2().add(new ArfcnPciBean(bean2.getArfcn(), bean2.getPci()));
                    }
                    return false;
                }
                //判断是否重复
                List<ArfcnPciBean> list = historyBean.getTD2();
                List<ArfcnPciBean> listDelete = new ArrayList<>(list);
//                if (isChanging()) {
                //删除最后一个再比较
                listDelete.remove(listDelete.size() - 1);
//                }

                //相同值检查
                for (ArfcnPciBean arfcnPciBean : listDelete) {
                    if (arfcnPciBean.getArfcn().equals(bean2.getArfcn()) && arfcnPciBean.getPci().equals(bean2.getPci())) {
                        Util.showToast("已存在相同数据值，请修改");
                        if (!isChanging()) { //点了添加按钮要把最后那个空的删了
                            historyBean.getTD2().remove(historyBean.getTD2().size() - 1);
                            isChanging = true;
                            historyBean.getTD2().add(new ArfcnPciBean(bean2.getArfcn(), bean2.getPci()));
                        }
                        return false;
                    }
                }
                //通过相同判断
                list.clear();
                list.addAll(listDelete);

                list.add(bean2);
                TD2_adapter.notifyDataSetChanged();
//                TD2_adapter.addData(bean2);
            } else if (historyBean.getTD2().size() > 0) historyBean.getTD2().removeLast();
            setIsChanging(true);        //为了下次点击添加的时候能够识别为会被修改的
        }
        while (historyBean.getTD2().size() > 0 && historyBean.getTD2().get(0).getArfcn().isEmpty())
            historyBean.getTD2().remove(0);


        ArfcnPciBean bean3 = TD3_adapter.getLastViewText();
        imsi = actv_imsi_3.getText().toString();
        if (!imsi.isEmpty() && imsi.length() != 15) {
            Util.showToast(mContext.getString(R.string.cell_2_imsi_err));
            return false;
        }
        if (!Util.existSameData(dropImsiList, imsi)) {
            if (dropImsiList.size() > 20) {
                dropImsiList.remove(0);
            }
            dropImsiList.add(imsi);
            PrefUtil.build().setDropImsiList(dropImsiList);
            dropImsiAdapter.setList(dropImsiList);
        }
        historyBean.setImsiThird(imsi);
        if (bean3 != null) {
            if (!bean3.getArfcn().isEmpty() && !bean3.getPci().isEmpty()) {
                if (!checkParam(bean3.getArfcn(), Integer.parseInt(bean3.getPci()), "TD3")) {
                    if (!isChanging()) { //点了添加按钮要把最后那个空的删了
                        historyBean.getTD3().remove(historyBean.getTD3().size() - 1);
                        isChanging = true;
                        historyBean.getTD3().add(new ArfcnPciBean(bean3.getArfcn(), bean3.getPci()));
                    }
                    return false;
                }
                //判断是否重复
                List<ArfcnPciBean> list = historyBean.getTD3();
                List<ArfcnPciBean> listDelete = new ArrayList<>(list);
//                if (isChanging()) {
                //删除最后一个再比较（）有输入时删除最新的，没输入时删除空值
                listDelete.remove(listDelete.size() - 1);
//                }

                //相同值检查
                for (ArfcnPciBean arfcnPciBean : listDelete) {
                    if (arfcnPciBean.getArfcn().equals(bean3.getArfcn()) && arfcnPciBean.getPci().equals(bean3.getPci())) {
                        Util.showToast("已存在相同数据值，请修改");
                        if (!isChanging()) { //点了添加按钮要把最后那个空的删了
                            historyBean.getTD3().remove(historyBean.getTD3().size() - 1);
                            isChanging = true;
                            historyBean.getTD3().add(new ArfcnPciBean(bean3.getArfcn(), bean3.getPci()));
                        }
                        return false;
                    }
                }
                //通过相同判断
                list.clear();
                list.addAll(listDelete);

                list.add(bean3);
                TD3_adapter.notifyDataSetChanged();
//                TD3_adapter.addData(bean3);
            } else if (historyBean.getTD3().size() > 0) historyBean.getTD3().removeLast();
            setIsChanging(true);        //为了下次点击添加的时候能够识别为会被修改的
        }
        while (historyBean.getTD3().size() > 0 && historyBean.getTD3().get(0).getArfcn().isEmpty())
            historyBean.getTD3().remove(0);


        ArfcnPciBean bean4 = TD4_adapter.getLastViewText();
        imsi = actv_imsi_4.getText().toString();
        if (!imsi.isEmpty() && imsi.length() != 15) {
            Util.showToast(mContext.getString(R.string.cell_3_imsi_err));
            return false;
        }
        if (!Util.existSameData(dropImsiList, imsi)) {
            if (dropImsiList.size() > 20) {
                dropImsiList.remove(0);
            }
            dropImsiList.add(imsi);
            PrefUtil.build().setDropImsiList(dropImsiList);
            dropImsiAdapter.setList(dropImsiList);
        }
        historyBean.setImsiFourth(imsi);
        if (bean4 != null) {
            if (!bean4.getArfcn().isEmpty() && !bean4.getPci().isEmpty()) {
                if (!checkParam(bean4.getArfcn(), Integer.parseInt(bean4.getPci()), "TD4")) {
                    if (!isChanging()) { //点了添加按钮要把最后那个空的删了
                        historyBean.getTD4().remove(historyBean.getTD4().size() - 1);
                        isChanging = true;
                        historyBean.getTD4().add(new ArfcnPciBean(bean4.getArfcn(), bean4.getPci()));
                    }
                    return false;
                }
                //判断是否重复
                List<ArfcnPciBean> list = historyBean.getTD4();
                List<ArfcnPciBean> listDelete = new ArrayList<>(list);

//                if (isChanging()) {
                //删除最后一个再比较
                listDelete.remove(listDelete.size() - 1);
//                }

                //相同值检查
                for (ArfcnPciBean arfcnPciBean : listDelete) {
                    if (arfcnPciBean.getArfcn().equals(bean4.getArfcn()) && arfcnPciBean.getPci().equals(bean4.getPci())) {
                        Util.showToast("已存在相同数据值，请修改");
                        if (!isChanging()) { //点了添加按钮要把最后那个空的删了
                            historyBean.getTD4().remove(historyBean.getTD4().size() - 1);
                            isChanging = true;
                            historyBean.getTD4().add(new ArfcnPciBean(bean4.getArfcn(), bean4.getPci()));
                        }
                        return false;
                    }
                }
                //通过相同判断
                list.clear();
                list.addAll(listDelete);

                list.add(bean4);
                TD4_adapter.notifyDataSetChanged();
            } else if (historyBean.getTD4().size() > 0) historyBean.getTD4().removeLast();
            setIsChanging(true);        //为了下次点击添加的时候能够识别为会被修改的
        }
        while (historyBean.getTD4().size() > 0 && historyBean.getTD4().get(0).getArfcn().isEmpty())
            historyBean.getTD4().remove(0);

        if (PaCtl.build().isB97502) {
            // 双频点检测
            int td2 = historyBean.getTD2().size();
            int td4 = historyBean.getTD4().size();
            int arfcn_band_2 = 0;
            if (td2 > 0)
                arfcn_band_2 = LteBand.earfcn2band(Integer.parseInt(historyBean.getTD2().get(0).getArfcn()));

            int arfcn_band_4 = 0;
            if (td4 > 0)
                arfcn_band_4 = LteBand.earfcn2band(Integer.parseInt(historyBean.getTD4().get(0).getArfcn()));

            // 双频点检测
            if (arfcn_band_2 == 40 && (arfcn_band_4 != 40 || td4 > 1)) {
                Util.showToast(mContext.getString(R.string.cell3_need_b40));
                return false;
            }
            if (arfcn_band_4 == 3 && (arfcn_band_2 != 3 || td2 > 1)) {
                Util.showToast(mContext.getString(R.string.cell1_need_b3));
                return false;
            }

            // 不支持N1(B1)和 B34/39/40/41同开
            int arfcn_band_3_nr = 0;
            int arfcn_band_3_lte = 0;
            int td3 = historyBean.getTD3().size();
            if (td3 > 0) {
                arfcn_band_3_nr = NrBand.earfcn2band(Integer.parseInt(historyBean.getTD3().get(0).getArfcn()));
                arfcn_band_3_lte = LteBand.earfcn2band(Integer.parseInt(historyBean.getTD3().get(0).getArfcn()));
            }
            boolean allB3 = true;
            if (td4 > 0) {
                for (ArfcnPciBean bean : historyBean.getTD4()) {
                    if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) != 3) {
                        allB3 = false;
                        break;
                    }
                }
            }

            if ((arfcn_band_3_nr == 1 || arfcn_band_3_lte == 1) && td4 > 0 && !allB3) {
                Util.showToast(mContext.getString(R.string.not_use_cell3));
                return false;
            }
        }

        if (historyBean.getTD1().size() == 0 && historyBean.getTD2().size() == 0 && historyBean.getTD3().size() == 0 && historyBean.getTD4().size() == 0) {
            Util.showToast(mContext.getString(R.string.no_input_data));
            historyBean.getTD1().add(new ArfcnPciBean("", ""));
            historyBean.getTD2().add(new ArfcnPciBean("", ""));
            historyBean.getTD3().add(new ArfcnPciBean("", ""));
            historyBean.getTD4().add(new ArfcnPciBean("", ""));
            return false;
        }
        return true;
    }

    //检查imsi和arfcn的对应关系
    public boolean checkImsiArfcnParam() {
//       ArfcnPciBean bean1 = TD1_adapter.getLastViewText();
        String imsi = historyBean.getImsiFirst();
        if (!imsi.isEmpty()) {
            String plmn = imsi2Plmn(imsi);
            //通过arfcn计算出对应的imsi然后进行比较
            for (ArfcnPciBean bean : historyBean.getTD1()) {
                String plmnFromArfcn = getPlmnFromArfcn(bean.getArfcn());
                if (!isSameOperator(plmn, plmnFromArfcn)) {
                    Util.showToast("通道一频点" + bean.getArfcn() + "与imsi号运营商不一致，请修改配置", Toast.LENGTH_LONG);
                    return false;
                }
            }
        }

        imsi = historyBean.getImsiSecond();
        if (!imsi.isEmpty()) {
            String plmn = imsi2Plmn(imsi);
            //通过arfcn计算出对应的imsi然后进行比较
            for (ArfcnPciBean bean : historyBean.getTD2()) {
                String plmnFromArfcn = getPlmnFromArfcn(bean.getArfcn());
                if (!isSameOperator(plmn, plmnFromArfcn)) {
                    Util.showToast("通道二频点" + bean.getArfcn() + "与imsi号运营商不一致，请修改配置", Toast.LENGTH_LONG);
                    return false;
                }
            }
        }

        imsi = historyBean.getImsiThird();
        if (!imsi.isEmpty()) {
            String plmn = imsi2Plmn(imsi);
            //通过arfcn计算出对应的imsi然后进行比较
            for (ArfcnPciBean bean : historyBean.getTD3()) {
                String plmnFromArfcn = getPlmnFromArfcn(bean.getArfcn());
                if (!isSameOperator(plmn, plmnFromArfcn)) {
                    Util.showToast("通道三频点" + bean.getArfcn() + "与imsi号运营商不一致，请修改配置", Toast.LENGTH_LONG);
                    return false;
                }
            }
        }

        imsi = historyBean.getImsiFourth();
        if (!imsi.isEmpty()) {
            String plmn = imsi2Plmn(imsi);
            //通过arfcn计算出对应的imsi然后进行比较
            for (ArfcnPciBean bean : historyBean.getTD4()) {
                String plmnFromArfcn = getPlmnFromArfcn(bean.getArfcn());
                if (!isSameOperator(plmn, plmnFromArfcn)) {
                    Util.showToast("通道四频点" + bean.getArfcn() + "与imsi号运营商不一致，请修改配置", Toast.LENGTH_LONG);
                    return false;
                }
            }
        }
        return true;
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


    private String getPlmnFromArfcn(String arfcn) {
        int band;
        if (arfcn.length() < 6) {
            band = LteBand.earfcn2band(Integer.parseInt(arfcn));
            switch (band) {
                case 1:
                case 5:
                    return "46001";
                case 34:
                case 39:
                case 40:
                case 41:
                    return "46000";
                case 3:
                case 8:
                    int freq = LteBand.earfcn2freq(Integer.parseInt(arfcn));
                    if ((freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949))
                        return "46000";
                    else return "46001";
            }
        } else {
            band = NrBand.earfcn2band(Integer.parseInt(arfcn));
            switch (band) {
                case 1:
                case 78:
                    return "46001";
                case 28:
                case 41:
                case 79:
                    return "46000";
            }
        }
        return "00000";
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

    public HistoryBean getHistoryBean() {
        return historyBean;
    }

    private boolean checkParam(String arfcn, int i_pci, String key) {
        int band;
        switch (key) {
            case "TD1":
                band = NrBand.earfcn2band(Integer.parseInt(arfcn));
                if (PaCtl.build().isB97502) { // N41/N78/N79
                    if (band != 41 && band != 78 && band != 79) {
                        showToast(mContext.getString(R.string.b97502_cell0_support_tip));
                        return false;
                    }
                } else {// N28/N78/N79
                    if (band != 28 && band != 78 && band != 79) {
                        showToast(mContext.getString(R.string.b97501_cell0_support_tip));
                        return false;
                    }
                }
                if (i_pci < 0 || i_pci > 1007) {
                    showToast(mContext.getString(R.string.pci_err_tip_5g));
                    return false;
                }
                break;
            case "TD2":
                band = LteBand.earfcn2band(Integer.parseInt(arfcn));
                if (PaCtl.build().isB97502) {
                    if (band != 3 && band != 5 && band != 8 && band != 40) {
                        showToast(mContext.getString(R.string.b97502_cell1_support_tip));
                        return false;
                    }
                    if (historyBean.getTD2().size() > 1) {
                        if (band == 40){
                            showToast(mContext.getString(R.string.same_err_tip));
                            return false;
                        }
                        for (ArfcnPciBean bean : historyBean.getTD2()) {
                            if (bean.getArfcn().isEmpty()) continue;
                            if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 40) { //次频点
                                showToast(mContext.getString(R.string.same_err_tip));
                                return false;
                            }
                        }
                    }
                } else { // B34/B39/B40/B41
                    if (band != 34 && band != 39 && band != 40 && band != 41) {
                        showToast(mContext.getString(R.string.b97501_cell1_support_tip));
                        return false;
                    }
                }
                if (i_pci < 0 || i_pci > 503) {
                    showToast(mContext.getString(R.string.pci_err_tip_4g));
                    return false;
                }
                break;
            case "TD3":
                band = NrBand.earfcn2band(Integer.parseInt(arfcn));
                if (PaCtl.build().isB97502) {
                    int lte_band = LteBand.earfcn2band(Integer.parseInt(arfcn));
                    if (lte_band != 1 && band != 1 && band != 28) {
                        showToast(mContext.getString(R.string.b97502_cell2_support_tip));
                        return false;
                    }
                } else {// N1/N41
                    if (band != 1 && band != 41) {
                        showToast(mContext.getString(R.string.b97501_cell2_support_tip));
                        return false;
                    }
                }
                if (i_pci < 0 || i_pci > 1007) {
                    showToast(mContext.getString(R.string.pci_err_tip_5g));
                    return false;
                }
                break;
            case "TD4":
                band = LteBand.earfcn2band(Integer.parseInt(arfcn));
                if (PaCtl.build().isB97502) {
                    if (band != 3 && band != 34 && band != 39 && band != 40 && band != 41) {
                        showToast(mContext.getString(R.string.b97502_cell3_support_tip));
                        return false;
                    }
                    if (historyBean.getTD4().size() > 1) {
                        if (band == 3){
                            showToast(mContext.getString(R.string.same_err_tip));
                            return false;
                        }
                        for (ArfcnPciBean bean : historyBean.getTD4()) {
                            if (bean.getArfcn().isEmpty()) continue;
                            if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 3) { //次频点
                                showToast(mContext.getString(R.string.same_err_tip));
                                return false;
                            }
                        }
                    }
                } else {// B1/B3/B5/B8
                    if (band != 1 && band != 3 && band != 5 && band != 8) {
                        showToast(mContext.getString(R.string.b97501_cell3_support_tip));
                        return false;
                    }
                }
                if (i_pci < 0 || i_pci > 503) {
                    showToast(mContext.getString(R.string.pci_err_tip_4g));
                    return false;
                }
                break;
        }

        return true;
    }

    private void showFreqListDialog(int cell_id) {
        if (getScanArfcnBeanList() == null) {
            Util.showToast(getString(R.string.no_freq_data_tip));
            return;
        }
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_freqlist_silde, null);

        view.findViewById(R.id.back).setOnClickListener(
                view1 -> MainActivity.getInstance().closeCustomDialog());

        RecyclerView freq_list = view.findViewById(R.id.freq_list);
        freq_list.setLayoutManager(new LinearLayoutManager(mContext));

        FreqResultListAdapter adapter = new FreqResultListAdapter(mContext, getScanArfcnBeanList(), new ListItemListener() {
            @Override
            public void onItemClickListener(int position) {
                //判断是否有重复频点
                ScanArfcnBean bean = getScanArfcnBeanList().get(position);
                int importArfcn = bean.getDl_arfcn();
                LinkedList<ArfcnPciBean> list;
                DualRecyclerviewAdapter adapter;
                if (cell_id == 0) {
                    list = historyBean.getTD1();
                    adapter = TD1_adapter;
                } else if (cell_id == 1) {
                    list = historyBean.getTD2();
                    adapter = TD2_adapter;
                } else if (cell_id == 2) {
                    list = historyBean.getTD3();
                    adapter = TD3_adapter;
                } else {
                    list = historyBean.getTD4();
                    adapter = TD4_adapter;
                }
                int importPci = countPci(bean, cell_id, 0);  //扫频结果已经处理过，所以传入0

                String lastArfcn = adapter.getLastViewText().getArfcn();    //最后的arfcn
                String lastPci = adapter.getLastViewText().getPci();
                //直接添加到第一个，不影响其他数据，不需检查last为空
                if (adapter.getLastViewText() != null) {
                    //相同信息
                    if (lastArfcn.equals(String.valueOf(importArfcn)) && lastPci.equals(String.valueOf(importPci))) {
                        Util.showToast("已存在相同数据，导入失败");
                        return;
                    }
                }

                if (list.size() > 1) {   //等于1的时候只需要检查last就够了
                    for (ArfcnPciBean arfcnPciBean : list) {
                        if (arfcnPciBean.getArfcn().equals(String.valueOf(importArfcn)) && arfcnPciBean.getPci().equals(String.valueOf(importPci))) {
                            Util.showToast("已存在相同数据，导入失败");
                            return;
                        }
                    }
                }

                list.remove(list.size() - 1);
                list.add(adapter.getLastViewText());
                adapter.notifyDataSetChanged();
                setIsChanging(true);

                if (checkParam(String.valueOf(importArfcn), importPci, "TD" + (cell_id + 1))) {
                    if (importArfcn(getScanArfcnBeanList().get(position), cell_id, 0)) {
//                    adapter.setLastEditText(lastArfcn, lastPci);        //为什么这个是在前面运行的
                        MainActivity.getInstance().closeCustomDialog();
                    } else Util.showToast(getString(R.string.cell_not_support_tip));
                }

            }
        });
        freq_list.setLayoutManager(new LinearLayoutManager(mContext));
        freq_list.setAdapter(adapter);
        MainActivity.getInstance().showCustomDialog(view, false, true);
    }

    private ArrayList<ScanArfcnBean> getScanArfcnBeanList() {
        return MainActivity.getInstance().getScanArfcnBeanList();
    }

    private void showToast(String msg) {
        Context context = mContext.getApplicationContext();
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

    public boolean isCell1SetArfcnChange() {
        return isCell1SetArfcnChange;
    }

    public void setCell1SetArfcnChange(boolean cell1SetArfcnChange) {
        isCell1SetArfcnChange = cell1SetArfcnChange;
    }

    public boolean isCell2SetArfcnChange() {
        return isCell2SetArfcnChange;
    }

    public void setCell2SetArfcnChange(boolean cell2SetArfcnChange) {
        isCell2SetArfcnChange = cell2SetArfcnChange;
    }

    public boolean isCell3SetArfcnChange() {
        return isCell3SetArfcnChange;
    }

    public void setCell3SetArfcnChange(boolean cell3SetArfcnChange) {
        isCell3SetArfcnChange = cell3SetArfcnChange;
    }

    public boolean isCell4SetArfcnChange() {
        return isCell4SetArfcnChange;
    }

    public void setCell4SetArfcnChange(boolean cell4SetArfcnChange) {
        isCell4SetArfcnChange = cell4SetArfcnChange;
    }

    public boolean isAutoModeFreqRunning() {
        return isAutoModeFreqRunning;
    }

    public void setAutoModeFreqRunning(boolean autoModeFreqRunning) {
        isAutoModeFreqRunning = autoModeFreqRunning;
    }

    public boolean isChange1() {
        return isChange1;
    }

    public void setChange1(boolean change1) {
        isChange1 = change1;
    }

    public boolean isChange2() {
        return isChange2;
    }

    public void setChange2(boolean change2) {
        isChange2 = change2;
    }

    public boolean isChange3() {
        return isChange3;
    }

    public void setChange3(boolean change3) {
        isChange3 = change3;
    }

    public boolean isChange4() {
        return isChange4;
    }

    public void setChange4(boolean change4) {
        isChange4 = change4;
    }

    public boolean isChanging() {
        return isChanging;
    }

    public void setIsChanging(boolean changing) {
        isChanging = changing;
    }

    public void setAutoMode(boolean autoMode) {
        isAutoMode = autoMode;
    }

    public boolean isCloseDistance() {
        return isCloseDistance;
    }

    public void setCloseDistance(boolean closeDistance) {
        isCloseDistance = closeDistance;
    }

    public void setIsCloaseDistance(Boolean isCloseDistance) {
        this.isCloseDistance = isCloseDistance;
        if (isCloseDistance) {
            iv_instance.setBackgroundResource(R.mipmap.icon_distance2_h);
            mTraceCatchFragment.doSetTxPwrOffset(-2);
        } else {
            iv_instance.setBackgroundResource(R.mipmap.icon_distance2_n);
            mTraceCatchFragment.doSetTxPwrOffset(0);
        }
    }

    @Override
    public void onItemClickListener(int position) {

    }

    @Override
    public void onHistoryItemClickListener(JSONArray jsonArray) {
        AppLog.D("onHistoryItemClickListener jsonArray = " + jsonArray.toString() + ", " + jsonArray.length());
        if (jsonArray.length() == 0) {
            historyBean.getTD1().add(new ArfcnPciBean("", ""));
            historyBean.getTD2().add(new ArfcnPciBean("", ""));
            historyBean.getTD3().add(new ArfcnPciBean("", ""));
            historyBean.getTD4().add(new ArfcnPciBean("", ""));
            return;
        }
        try {
            //数据导入定位页
            JSONObject jsonObject1 = jsonArray.getJSONObject(0);
            JSONObject jsonObject2 = jsonArray.getJSONObject(1);
            JSONObject jsonObject3 = jsonArray.getJSONObject(2);
            JSONObject jsonObject4 = jsonArray.getJSONObject(3);

            String imsi1 = jsonObject1.getString("imsi");
            String imsi2 = jsonObject2.getString("imsi");
            String imsi3 = jsonObject3.getString("imsi");
            String imsi4 = jsonObject4.getString("imsi");

            String arfcnJsonArray1 = jsonObject1.getString("arfcnJsonArray");
            String arfcnJsonArray2 = jsonObject2.getString("arfcnJsonArray");
            String arfcnJsonArray3 = jsonObject3.getString("arfcnJsonArray");
            String arfcnJsonArray4 = jsonObject4.getString("arfcnJsonArray");

            historyBean.getTD1().clear();
            historyBean.getTD2().clear();
            historyBean.getTD3().clear();
            historyBean.getTD4().clear();
            JSONArray arfcnArray1 = new JSONArray(arfcnJsonArray1);
            for (int i = 0; i < arfcnArray1.length(); i++) {
                String[] string = arfcnArray1.getString(i).split("/");
                historyBean.getTD1().add(new ArfcnPciBean(string[0], string[1]));
            }
            JSONArray arfcnArray2 = new JSONArray(arfcnJsonArray2);
            for (int i = 0; i < arfcnArray2.length(); i++) {
                String[] string = arfcnArray2.getString(i).split("/");
                historyBean.getTD2().add(new ArfcnPciBean(string[0], string[1]));
            }
            JSONArray arfcnArray3 = new JSONArray(arfcnJsonArray3);
            for (int i = 0; i < arfcnArray3.length(); i++) {
                String[] string = arfcnArray3.getString(i).split("/");
                historyBean.getTD3().add(new ArfcnPciBean(string[0], string[1]));
            }
            JSONArray arfcnArray4 = new JSONArray(arfcnJsonArray4);
            for (int i = 0; i < arfcnArray4.length(); i++) {
                String[] string = arfcnArray4.getString(i).split("/");
                historyBean.getTD4().add(new ArfcnPciBean(string[0], string[1]));
            }

            actv_imsi.setText(imsi1);
            actv_imsi_2.setText(imsi2);
            actv_imsi_3.setText(imsi3);
            actv_imsi_4.setText(imsi4);

            if (historyBean.getTD1().size() == 0)
                historyBean.getTD1().add(new ArfcnPciBean("", ""));
            if (historyBean.getTD2().size() == 0)
                historyBean.getTD2().add(new ArfcnPciBean("", ""));
            if (historyBean.getTD3().size() == 0)
                historyBean.getTD3().add(new ArfcnPciBean("", ""));
            if (historyBean.getTD4().size() == 0)
                historyBean.getTD4().add(new ArfcnPciBean("", ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void resetArfcnList() {
        AppLog.I("resetArfcnList");
        if (historyBean.getTD1().size() == 0) {
            historyBean.getTD1().add(new ArfcnPciBean("", ""));
            TD1_adapter.notifyDataSetChanged();
        }
        if (historyBean.getTD2().size() == 0) {
            historyBean.getTD2().add(new ArfcnPciBean("", ""));
            TD2_adapter.notifyDataSetChanged();
        }
        if (historyBean.getTD3().size() == 0) {
            historyBean.getTD3().add(new ArfcnPciBean("", ""));
            TD3_adapter.notifyDataSetChanged();
        }
        if (historyBean.getTD4().size() == 0) {
            historyBean.getTD4().add(new ArfcnPciBean("", ""));
            TD4_adapter.notifyDataSetChanged();
        }
    }

    public void addDropImsi(String imsi) {
        if (!Util.existSameData(dropImsiList, imsi)) {
            if (dropImsiList.size() > 20) {
                dropImsiList.remove(0);
            }
            dropImsiList.add(imsi);
            PrefUtil.build().setDropImsiList(dropImsiList);
            dropImsiAdapter.setList(dropImsiList);
        }
    }

    public void setIsChangingTrue() {
        setIsChanging(true);
    }
}