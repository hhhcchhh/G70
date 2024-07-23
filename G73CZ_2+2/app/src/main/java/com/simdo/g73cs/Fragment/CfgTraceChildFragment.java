package com.simdo.g73cs.Fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
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

import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.Gnb.Bean.GnbProtocol;
import com.simdo.g73cs.Adapter.AutoSearchAdapter;
import com.simdo.g73cs.Adapter.FreqResultListAdapter;
import com.simdo.g73cs.Adapter.MyRecyclerviewAdapter;
import com.simdo.g73cs.Bean.ArfcnBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Dialog.CustomDialog;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DeviceUtil;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.TraceUtil;
import com.simdo.g73cs.Util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CfgTraceChildFragment extends Fragment implements MyRecyclerviewAdapter.OnDelArfcnListListener
        , ListItemListener {

    Context mContext;
    private final TraceCatchFragment mTraceCatchFragment;
    private final SettingFragment settingFragment;
    private FreqFragment freqFragment;
    //是否频点轮询
    private boolean isCell1SetArfcnChange, isCell2SetArfcnChange, isCell3SetArfcnChange, isCell4SetArfcnChange;
    ImageView iv_instance;
    TextView tv_do_btn;
    RecyclerView rv_arfcn_1, rv_arfcn_2, rv_arfcn_3, rv_arfcn_4;
    EditText et_arfcn_1, et_arfcn_2, et_arfcn_3, et_arfcn_4;
    EditText et_arfcn_5, et_arfcn_6, et_arfcn_7, et_arfcn_8;
    Switch sw_auto_trace;
    EditText ed_auto_imsi;
    TextView tv_switch_text_cfg, tv_switch_text_auto;
    NestedScrollView sv_cfg_trace;
    LinearLayout ll_auto_trace;
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
    AutoCompleteTextView actv_imsi_5, actv_imsi_6, actv_imsi_7, actv_imsi_8;
    //    EditText ed_arfcn, ed_arfcn_2, ed_arfcn_3, ed_arfcn_4;
    private List<String> dropImsiList = new ArrayList<>();  //imsi历史列表
    private AutoSearchAdapter dropImsiAdapter;
    Group trace_group_1, trace_group_2, trace_group_3, trace_group_4;
    Group trace_group_5, trace_group_6, trace_group_7, trace_group_8;
//    MyRecyclerviewAdapter TD1_adapter, TD2_adapter, TD3_adapter, TD4_adapter;   //废除,不影响的不用改
//    MyRecyclerviewAdapter TD5_adapter, TD6_adapter, TD7_adapter, TD8_adapter;

    //暂存arfcnList，最后传给map
    LinkedList<Integer> TD1, TD2, TD3, TD4;     //只用第一个作为arfcn的值
    LinkedList<Integer> TD5, TD6, TD7, TD8;
    EditText ed_pci_1, ed_pci_2, ed_pci_3, ed_pci_4;
    EditText ed_pci_5, ed_pci_6, ed_pci_7, ed_pci_8;
    TextView tv_auto_trace;
    private final int minDelta = 200;           // threshold in ms
    private long focusTime = 0;                 // time of last touch
    private View focusTarget = null;

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
                        focusTarget.requestFocus(), 500);

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
                    StringBuilder arfcnList = new StringBuilder();
                    arfcnList.append("\nTD1=").append(TD1.toString()).append("\nTD2=").append(TD2).append("\nTD3=").append(TD3).append("\nTD4=").append(TD4);
                    arfcnList.append("\nmTraceCatchFragment arfcnBeanHashMap");
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD1") != null) {
                        arfcnList.append("\n通道一=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD1").getArfcnList().toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD2") != null) {
                        arfcnList.append("\n通道二=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD2").getArfcnList().toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD3") != null) {
                        arfcnList.append("\n通道三=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD3").getArfcnList().toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD4") != null) {
                        arfcnList.append("\n通道四=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD4").getArfcnList().toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD5") != null) {
                        arfcnList.append("\n通道五=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD5").getArfcnList().toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD6") != null) {
                        arfcnList.append("\n通道六=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD6").getArfcnList().toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD7") != null) {
                        arfcnList.append("\n通道七=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD7").getArfcnList().toString());
                    }
                    if (mTraceCatchFragment.arfcnBeanHashMap.get("TD8") != null) {
                        arfcnList.append("\n通道八=").append(mTraceCatchFragment.arfcnBeanHashMap.get("TD8").getArfcnList().toString());
                    }

                    arfcnList.append("\nfreqList：").append(getFreqList());
                    arfcnList.append("\nisAutoMode：").append(isAutoMode);
                    arfcnList.append("\nmLogicTraceUtilNr：cell1").append(TraceCatchFragment.mLogicTraceUtilNr.getArfcn(0));
                    arfcnList.append("\nmLogicTraceUtilNr：cell2").append(TraceCatchFragment.mLogicTraceUtilNr.getArfcn(1));
                    arfcnList.append("\nmLogicTraceUtilNr2：cell1").append(TraceCatchFragment.mLogicTraceUtilNr2.getArfcn(0));
                    arfcnList.append("\nmLogicTraceUtilNr2：cell2").append(TraceCatchFragment.mLogicTraceUtilNr2.getArfcn(1));
                    arfcnList.append("\nmLogicTraceUtilLte：cell1").append(TraceCatchFragment.mLogicTraceUtilLte.getArfcn(0));
                    arfcnList.append("\nmLogicTraceUtilLte：cell2").append(TraceCatchFragment.mLogicTraceUtilLte.getArfcn(1));
                    arfcnList.append("\nmLogicTraceUtilLte2：cell1").append(TraceCatchFragment.mLogicTraceUtilLte2.getArfcn(0));
                    arfcnList.append("\nmLogicTraceUtilLte2：cell2").append(TraceCatchFragment.mLogicTraceUtilLte2.getArfcn(1));

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
        trace_group_5 = root.findViewById(R.id.trace_group_5);
        trace_group_6 = root.findViewById(R.id.trace_group_6);
        trace_group_7 = root.findViewById(R.id.trace_group_7);
        trace_group_8 = root.findViewById(R.id.trace_group_8);

        Spanned spanned = Html.fromHtml(getString(R.string.hint_code));
        TextView tv_cell_tip_1 = root.findViewById(R.id.tv_cell_tip_1);
        TextView tv_cell_tip_2 = root.findViewById(R.id.tv_cell_tip_2);
        TextView tv_cell_tip_3 = root.findViewById(R.id.tv_cell_tip_3);
        TextView tv_cell_tip_4 = root.findViewById(R.id.tv_cell_tip_4);
        TextView tv_cell_tip_5 = root.findViewById(R.id.tv_cell_tip_5);
        TextView tv_cell_tip_6 = root.findViewById(R.id.tv_cell_tip_6);
        TextView tv_cell_tip_7 = root.findViewById(R.id.tv_cell_tip_7);
        TextView tv_cell_tip_8 = root.findViewById(R.id.tv_cell_tip_8);

        if (PaCtl.build().isB97502) {
            tv_cell_tip_1.setText("(N41)");
            tv_cell_tip_2.setText("(N1)");
            tv_cell_tip_3.setText("(N78/N79)");
            tv_cell_tip_4.setText("(N28)");
            tv_cell_tip_5.setText("(B3/B34)(5、6通道不可下双FDD)");
            tv_cell_tip_6.setText("(B5/B39)(5、6通道不可下双FDD)");
            tv_cell_tip_7.setText("(B8/B40)(7、8通道不可下双FDD)");
            tv_cell_tip_8.setText("(B1/B41)(7、8通道不可下双FDD)");
        } else {
            tv_cell_tip_1.setText("(N28/N78/N79)");
            tv_cell_tip_2.setText("(B34/B39/B40/B41)");
            tv_cell_tip_3.setText("(N1/N41)");
            tv_cell_tip_4.setText("(B1/B3/B5/B8)");
        }
        et_arfcn_1 = root.findViewById(R.id.et_arfcn_1);
        et_arfcn_2 = root.findViewById(R.id.et_arfcn_2);
        et_arfcn_3 = root.findViewById(R.id.et_arfcn_3);
        et_arfcn_4 = root.findViewById(R.id.et_arfcn_4);
        et_arfcn_5 = root.findViewById(R.id.et_arfcn_5);
        et_arfcn_6 = root.findViewById(R.id.et_arfcn_6);
        et_arfcn_7 = root.findViewById(R.id.et_arfcn_7);
        et_arfcn_8 = root.findViewById(R.id.et_arfcn_8);

        ed_pci_1 = root.findViewById(R.id.ed_pci);
        ed_pci_2 = root.findViewById(R.id.ed_pci_2);
        ed_pci_3 = root.findViewById(R.id.ed_pci_3);
        ed_pci_4 = root.findViewById(R.id.ed_pci_4);
        ed_pci_5 = root.findViewById(R.id.ed_pci_5);
        ed_pci_6 = root.findViewById(R.id.ed_pci_6);
        ed_pci_7 = root.findViewById(R.id.ed_pci_7);
        ed_pci_8 = root.findViewById(R.id.ed_pci_8);

        actv_imsi = root.findViewById(R.id.actv_imsi);
        actv_imsi_2 = root.findViewById(R.id.actv_imsi_2);
        actv_imsi_3 = root.findViewById(R.id.actv_imsi_3);
        actv_imsi_4 = root.findViewById(R.id.actv_imsi_4);
        actv_imsi_5 = root.findViewById(R.id.actv_imsi_5);
        actv_imsi_6 = root.findViewById(R.id.actv_imsi_6);
        actv_imsi_7 = root.findViewById(R.id.actv_imsi_7);
        actv_imsi_8 = root.findViewById(R.id.actv_imsi_8);

//        et_arfcn_1.setOnFocusChangeListener(onFocusChangeListener);
//        et_arfcn_2.setOnFocusChangeListener(onFocusChangeListener);
//        et_arfcn_3.setOnFocusChangeListener(onFocusChangeListener);
//        et_arfcn_4.setOnFocusChangeListener(onFocusChangeListener);
//        et_arfcn_5.setOnFocusChangeListener(onFocusChangeListener);
//        et_arfcn_6.setOnFocusChangeListener(onFocusChangeListener);
//        et_arfcn_7.setOnFocusChangeListener(onFocusChangeListener);
//        et_arfcn_8.setOnFocusChangeListener(onFocusChangeListener);
//        ed_pci_1.setOnFocusChangeListener(onFocusChangeListener);
//        ed_pci_2.setOnFocusChangeListener(onFocusChangeListener);
//        ed_pci_3.setOnFocusChangeListener(onFocusChangeListener);
//        ed_pci_4.setOnFocusChangeListener(onFocusChangeListener);
//        ed_pci_5.setOnFocusChangeListener(onFocusChangeListener);
//        ed_pci_6.setOnFocusChangeListener(onFocusChangeListener);
//        ed_pci_7.setOnFocusChangeListener(onFocusChangeListener);
//        ed_pci_8.setOnFocusChangeListener(onFocusChangeListener);
//        actv_imsi.setOnFocusChangeListener(onFocusChangeListener);
//        actv_imsi_2.setOnFocusChangeListener(onFocusChangeListener);
//        actv_imsi_3.setOnFocusChangeListener(onFocusChangeListener);
//        actv_imsi_4.setOnFocusChangeListener(onFocusChangeListener);
//        actv_imsi_5.setOnFocusChangeListener(onFocusChangeListener);
//        actv_imsi_6.setOnFocusChangeListener(onFocusChangeListener);
//        actv_imsi_7.setOnFocusChangeListener(onFocusChangeListener);
//        actv_imsi_8.setOnFocusChangeListener(onFocusChangeListener);


        dropImsiAdapter = new AutoSearchAdapter(mContext, dropImsiList);
        actv_imsi.setAdapter(dropImsiAdapter);
        actv_imsi_2.setAdapter(dropImsiAdapter);
        actv_imsi_3.setAdapter(dropImsiAdapter);
        actv_imsi_4.setAdapter(dropImsiAdapter);
        actv_imsi_5.setAdapter(dropImsiAdapter);
        actv_imsi_6.setAdapter(dropImsiAdapter);
        actv_imsi_7.setAdapter(dropImsiAdapter);
        actv_imsi_8.setAdapter(dropImsiAdapter);

        String cfg_param = PrefUtil.build().getValue("cfg_param", "").toString();
        if (!cfg_param.isEmpty()) {
            String[] cfg_params = cfg_param.split("_");
            if (cfg_params.length > 0) et_arfcn_1.setText(cfg_params[0]);
            if (cfg_params.length > 1) ed_pci_1.setText(cfg_params[1]);
            if (cfg_params.length > 2) actv_imsi.setText(cfg_params[2]);

            if (cfg_params.length > 3) et_arfcn_2.setText(cfg_params[3]);
            if (cfg_params.length > 4) ed_pci_2.setText(cfg_params[4]);
            if (cfg_params.length > 5) actv_imsi_2.setText(cfg_params[5]);

            if (cfg_params.length > 6) et_arfcn_3.setText(cfg_params[6]);
            if (cfg_params.length > 7) ed_pci_3.setText(cfg_params[7]);
            if (cfg_params.length > 8) actv_imsi_3.setText(cfg_params[8]);

            if (cfg_params.length > 9) et_arfcn_4.setText(cfg_params[9]);
            if (cfg_params.length > 10) ed_pci_4.setText(cfg_params[10]);
            if (cfg_params.length > 11) actv_imsi_4.setText(cfg_params[11]);

            if (cfg_params.length > 12) et_arfcn_5.setText(cfg_params[12]);
            if (cfg_params.length > 13) ed_pci_5.setText(cfg_params[13]);
            if (cfg_params.length > 14) actv_imsi_5.setText(cfg_params[14]);
            if (cfg_params.length > 15) et_arfcn_6.setText(cfg_params[15]);
            if (cfg_params.length > 16) ed_pci_6.setText(cfg_params[16]);
            if (cfg_params.length > 17) actv_imsi_6.setText(cfg_params[17]);
            if (cfg_params.length > 18) et_arfcn_7.setText(cfg_params[18]);
            if (cfg_params.length > 19) ed_pci_7.setText(cfg_params[19]);
            if (cfg_params.length > 20) actv_imsi_7.setText(cfg_params[20]);
            if (cfg_params.length > 21) et_arfcn_8.setText(cfg_params[21]);
            if (cfg_params.length > 22) ed_pci_8.setText(cfg_params[22]);
            if (cfg_params.length > 23) actv_imsi_8.setText(cfg_params[23]);
        }


        root.findViewById(R.id.tv_down_same).setOnClickListener(view -> {
            String string = actv_imsi.getText().toString();
            actv_imsi_2.setText(string);
            actv_imsi_3.setText(string);
            actv_imsi_4.setText(string);
            actv_imsi_5.setText(string);
            actv_imsi_6.setText(string);
            actv_imsi_7.setText(string);
            actv_imsi_8.setText(string);
        });
        root.findViewById(R.id.tv_up_same_2).setOnClickListener(view -> actv_imsi_2.setText(actv_imsi.getText().toString()));
        root.findViewById(R.id.tv_up_same_3).setOnClickListener(view -> actv_imsi_3.setText(actv_imsi_2.getText().toString()));
        root.findViewById(R.id.tv_up_same_4).setOnClickListener(view -> actv_imsi_4.setText(actv_imsi_3.getText().toString()));
        root.findViewById(R.id.tv_up_same_5).setOnClickListener(view -> actv_imsi_5.setText(actv_imsi_4.getText().toString()));
        root.findViewById(R.id.tv_up_same_6).setOnClickListener(view -> actv_imsi_6.setText(actv_imsi_5.getText().toString()));
        root.findViewById(R.id.tv_up_same_7).setOnClickListener(view -> actv_imsi_7.setText(actv_imsi_6.getText().toString()));
        root.findViewById(R.id.tv_up_same_8).setOnClickListener(view -> actv_imsi_8.setText(actv_imsi_7.getText().toString()));

        root.findViewById(R.id.tv_import_1).setOnClickListener(view -> showFreqListDialog(0));
        root.findViewById(R.id.tv_import_2).setOnClickListener(view -> showFreqListDialog(1));
        root.findViewById(R.id.tv_import_3).setOnClickListener(view -> showFreqListDialog(2));
        root.findViewById(R.id.tv_import_4).setOnClickListener(view -> showFreqListDialog(3));
        root.findViewById(R.id.tv_import_5).setOnClickListener(view -> showFreqListDialog(4));
        root.findViewById(R.id.tv_import_6).setOnClickListener(view -> showFreqListDialog(5));
        root.findViewById(R.id.tv_import_7).setOnClickListener(view -> showFreqListDialog(6));
        root.findViewById(R.id.tv_import_8).setOnClickListener(view -> showFreqListDialog(7));

        TD1 = new LinkedList<>();
        TD2 = new LinkedList<>();
        TD3 = new LinkedList<>();
        TD4 = new LinkedList<>();
        TD5 = new LinkedList<>();
        TD6 = new LinkedList<>();
        TD7 = new LinkedList<>();
        TD8 = new LinkedList<>();

        ed_pci_1 = root.findViewById(R.id.ed_pci);
        ed_pci_2 = root.findViewById(R.id.ed_pci_2);
        ed_pci_3 = root.findViewById(R.id.ed_pci_3);
        ed_pci_4 = root.findViewById(R.id.ed_pci_4);
        ed_pci_5 = root.findViewById(R.id.ed_pci_5);
        ed_pci_6 = root.findViewById(R.id.ed_pci_6);
        ed_pci_7 = root.findViewById(R.id.ed_pci_7);
        ed_pci_8 = root.findViewById(R.id.ed_pci_8);

        //禁止输入前置0
        banLeadingZero(ed_pci_1);
        banLeadingZero(ed_pci_2);
        banLeadingZero(ed_pci_3);
        banLeadingZero(ed_pci_4);
        banLeadingZero(ed_pci_5);
        banLeadingZero(ed_pci_6);
        banLeadingZero(ed_pci_7);
        banLeadingZero(ed_pci_8);

//        TD1_adapter = new MyRecyclerviewAdapter(mContext, TD1, "TD1");
//        TD2_adapter = new MyRecyclerviewAdapter(mContext, TD2, "TD2");
//        TD3_adapter = new MyRecyclerviewAdapter(mContext, TD3, "TD3");
//        TD4_adapter = new MyRecyclerviewAdapter(mContext, TD4, "TD4");
//
//        TD1_adapter.setOnDelArfcnListListener(this);
//        TD2_adapter.setOnDelArfcnListListener(this);
//        TD3_adapter.setOnDelArfcnListListener(this);
//        TD4_adapter.setOnDelArfcnListListener(this);


//        et_arfcn_1.setText(TD1.get(0));
//        et_arfcn_2.setText(TD2.get(0));
//        et_arfcn_3.setText(TD3.get(0));
//        et_arfcn_4.setText(TD4.get(0));
//        et_arfcn_5.setText(TD5.get(0));
//        et_arfcn_6.setText(TD6.get(0));
//        et_arfcn_7.setText(TD7.get(0));
//        et_arfcn_8.setText(TD8.get(0));

//        rv_arfcn_1 = root.findViewById(R.id.et_arfcn_1);
//        rv_arfcn_1.setAdapter(TD1_adapter);
//        rv_arfcn_1.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
//        rv_arfcn_1.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
//        root.findViewById(R.id.btn_add_1).setOnClickListener(v -> {
//            isArfcnChange1 = true;
//            if (PaCtl.build().isB97502) {
//                addData2(v, TD1_adapter, TD1, "TD1");
//            } else {
//                addData1(v, TD1_adapter, TD1, "TD1");
//            }
//        });
//        rv_arfcn_2 = root.findViewById(R.id.et_arfcn_2);
//        rv_arfcn_2.setAdapter(TD2_adapter);
//        rv_arfcn_2.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
//        rv_arfcn_2.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
//        root.findViewById(R.id.btn_add_2).setOnClickListener(v -> {
//            isArfcnChange2 = true;
//            if (PaCtl.build().isB97502) {
//                addData2(v, TD2_adapter, TD2, "TD2");
//            } else {
//                addData1(v, TD2_adapter, TD2, "TD2");
//            }
//        });
//        rv_arfcn_3 = root.findViewById(R.id.et_arfcn_3);
//        rv_arfcn_3.setAdapter(TD3_adapter);
//        rv_arfcn_3.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
//        rv_arfcn_3.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
//        root.findViewById(R.id.btn_add_3).setOnClickListener(v -> {
//            isArfcnChange3 = true;
//            if (PaCtl.build().isB97502) {
//                addData2(v, TD3_adapter, TD3, "TD3");
//            } else {
//                addData1(v, TD3_adapter, TD3, "TD3");
//            }
//        });
//        rv_arfcn_4 = root.findViewById(R.id.et_arfcn_8);
//        rv_arfcn_4.setAdapter(TD4_adapter);
//        rv_arfcn_4.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
//        rv_arfcn_4.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
//        root.findViewById(R.id.btn_add_8).setOnClickListener(v -> {
//            isArfcnChange4 = true;
//            if (PaCtl.build().isB97502) {
//                addData2(v, TD4_adapter, TD4, "TD4");
//            } else {
//                addData1(v, TD4_adapter, TD4, "TD4");
//            }
//        });

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
            for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                if (MainActivity.getInstance().getDeviceList().get(i).getWorkState() == GnbBean.State.FREQ_SCAN
                || MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().getWorkState(0) == GnbBean.State.FREQ_SCAN) {
                    Util.showToast("设备正忙，请先结束工作");
                    return;
                }
            }
            if (sw_auto_trace.isChecked()) clickAutoTrace();
            else mTraceCatchFragment.doWork("");
        });
        //初始化按钮
        tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
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

    //1
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
            //有设备空闲就能开启
            String doStr = sb.toString();
            if (doStr.equals("0000")) {
                autoTraceDialog();  // 四设备均在空闲状态下，启动定位
                return;
            }
            if (doStr.contains("0")) {
                autoTraceDialog(); // 多设备在空闲状态下，启动定位
                return;
            }
            if (doStr.equals("-1-1-1-1")) {
                Util.showToast(getString(R.string.dev_offline));
                return;
            }
            Util.showToast(getString(R.string.dev_busy_please_wait));
            return;

        } else if (text.equals(getString(R.string.stop))) {
            mTraceCatchFragment.stopTraceDialog();
        } else if (text.equals(getString(R.string.starting))) {
            Util.showToast(getString(R.string.starting_cannot_fast));
        } else if (text.equals(getString(R.string.stoping))) {
            Util.showToast(getString(R.string.stoping_cannot_fast));
        }
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

        mTraceCatchFragment.arfcnBeanHashMap.put("TD1", new ArfcnBean(TD1));    //更新最新的值
        mTraceCatchFragment.arfcnBeanHashMap.put("TD2", new ArfcnBean(TD2));
        mTraceCatchFragment.arfcnBeanHashMap.put("TD3", new ArfcnBean(TD3));
        mTraceCatchFragment.arfcnBeanHashMap.put("TD4", new ArfcnBean(TD4));
    }

    private void hideSoftKeyBoard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //1
    //只存了最后的一个进去
    private void store8ChannelDataToPref() {
//        if (TD1_adapter.getLastViewText() != null && !TD1_adapter.getLastViewText().equals("")) {
//            int lastTextInt = Integer.parseInt(TD1_adapter.getLastViewText());
//            addArfcnToCfgPref(TD1_adapter, lastTextInt, true);
//        }
//        if (TD2_adapter.getLastViewText() != null && TD2_adapter.getLastViewText().equals("")) {
//            int lastTextInt = Integer.parseInt(TD2_adapter.getLastViewText());
//            addArfcnToCfgPref(TD2_adapter, lastTextInt, true);
//        }
//        if (TD3_adapter.getLastViewText() != null && TD3_adapter.getLastViewText().equals("")) {
//            int lastTextInt = Integer.parseInt(TD3_adapter.getLastViewText());
//            addArfcnToCfgPref(TD3_adapter, lastTextInt, true);
//        }
//        if (TD4_adapter.getLastViewText() != null && TD4_adapter.getLastViewText().equals("")) {
//            int lastTextInt = Integer.parseInt(TD4_adapter.getLastViewText());
//            addArfcnToCfgPref(TD4_adapter, lastTextInt, true);
//        }
//        if (TD5_adapter.getLastViewText() != null && TD5_adapter.getLastViewText().equals("")) {
//            int lastTextInt = Integer.parseInt(TD5_adapter.getLastViewText());
//            addArfcnToCfgPref(TD5_adapter, lastTextInt, true);
//        }
//        if (TD6_adapter.getLastViewText() != null && TD6_adapter.getLastViewText().equals("")) {
//            int lastTextInt = Integer.parseInt(TD6_adapter.getLastViewText());
//            addArfcnToCfgPref(TD6_adapter, lastTextInt, true);
//        }
//        if (TD7_adapter.getLastViewText() != null && TD7_adapter.getLastViewText().equals("")) {
//            int lastTextInt = Integer.parseInt(TD7_adapter.getLastViewText());
//            addArfcnToCfgPref(TD7_adapter, lastTextInt, true);
//        }
//        if (TD8_adapter.getLastViewText() != null && TD8_adapter.getLastViewText().equals("")) {
//            int lastTextInt = Integer.parseInt(TD8_adapter.getLastViewText());
//            addArfcnToCfgPref(TD8_adapter, lastTextInt, true);
//        }
    }

    //1
    //将一个值存入对应的频段中
    public void addArfcnToCfgPref(MyRecyclerviewAdapter adapter, int arfcn, Boolean ifCheck) {
        AppLog.D("CfgTraceChildFragment addArfcnToCfgPref: " + arfcn);
        String key;
        if (arfcn < 100000) {
            key = "B" + LteBand.earfcn2band(arfcn);
        } else {
            key = "N" + NrBand.earfcn2band(arfcn);
        }
        //先取出原有的添加进去
        List<Integer> list;
        try {
            list = Util.json2Int(PrefUtil.build().getValue(key + "cfg", "").toString(), key);
            list.clear();
            list.add(arfcn);
            PrefUtil.build().putValue(key + "cfg", Util.int2Json(list, key));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public TextView getTv_do_btn() {
        return tv_do_btn;
    }

    private void showFreqListDialog(int cell) {
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
                if (importArfcn(getScanArfcnBeanList().get(position), cell, 0))
                    MainActivity.getInstance().closeCustomDialog();
                else Util.showToast(getString(R.string.cell_not_support_tip));
            }
        });
        freq_list.setLayoutManager(new LinearLayoutManager(mContext));
        freq_list.setAdapter(adapter);
        MainActivity.getInstance().showCustomDialog(view, false, true);
    }

    //判断此通道的此arfcn是否成功
    private boolean checkArfcnParamAndSetTraceUtil(String ed_arfcn_str, String ed_pci_str, String imsi_str, int logicIndex, int cell_id) {
        AppLog.I("checkArfcnParamAndSetTraceUtil: ed_arfcn_str" + ed_arfcn_str + "ed_pci_str" + ed_pci_str
                + "imsi_str" + imsi_str + "logicIndex" + logicIndex + "cell_id" + cell_id);
        int band;
        int band_lte;
        TraceUtil traceUtil = TraceCatchFragment.mLogicTraceUtilNr;
        switch (logicIndex) {
            case 0:
                traceUtil = TraceCatchFragment.mLogicTraceUtilNr;
                break;
            case 1:
                traceUtil = TraceCatchFragment.mLogicTraceUtilNr2;
                break;
            case 2:
                traceUtil = TraceCatchFragment.mLogicTraceUtilLte;
                break;
            case 3:
                traceUtil = TraceCatchFragment.mLogicTraceUtilLte2;
                break;
        }
        AppLog.D("checkArfcnParamAndSetTraceUtil: ed_arfcn_str" + ed_arfcn_str);
        if (ed_arfcn_str.isEmpty() || imsi_str.isEmpty() || ed_pci_str.isEmpty()) {   //如果单个arfcn为空，不通过
            traceUtil.setArfcn(cell_id, ed_arfcn_str);
            traceUtil.setPci(cell_id, ed_pci_str);
            traceUtil.setImsi(cell_id, imsi_str);
//            setToastText("检测到配置通道频点为空");
            return false;
        } else {

            band = NrBand.earfcn2band(Integer.parseInt(ed_arfcn_str));
            band_lte = LteBand.earfcn2band(Integer.parseInt(ed_arfcn_str));

            int channelNum = logicIndex * 2 + cell_id + 1;
            String cellStr = getString(R.string.cell0);
            switch (channelNum) {
                case 1:
                    cellStr = getString(R.string.cell0);
                    break;
                case 2:
                    cellStr = getString(R.string.cell1);
                    break;
                case 3:
                    cellStr = getString(R.string.cell2);
                    break;
                case 4:
                    cellStr = getString(R.string.cell3);
                    break;
                case 5:
                    cellStr = "通道五";
                    break;
                case 6:
                    cellStr = "通道六";
                    break;
                case 7:
                    cellStr = "通道七";
                    break;
                case 8:
                    cellStr = "通道八";
                    break;
            }

            switch (logicIndex) {
                case 0:
                    if (PaCtl.build().isB97502) {
                        if (cell_id == GnbProtocol.CellId.FIRST) {
                            if (band != 41) {
                                setToastText("通道" + DeviceUtil.getCellStr(logicIndex, cell_id) + "仅支持输入N41对应的频点!");
                                return false;
                            }

                        } else if (cell_id == GnbProtocol.CellId.SECOND) {
                            if (band != 1) {
                                setToastText("通道" + DeviceUtil.getCellStr(logicIndex, cell_id) + "仅支持输入N1对应的频点!");
                                return false;
                            }
                        }
                    } else {
                        if (cell_id == GnbProtocol.CellId.FIRST) {
                            if (band != 28 && band != 78 && band != 79) {
                                setToastText(getString(R.string.b97501_cell0_support_tip));
                                return false;
                            }
                        } else if (cell_id == GnbProtocol.CellId.SECOND) {
                            if (band_lte != 34 && band_lte != 39 && band_lte != 40 && band_lte != 41) {
                                setToastText(getString(R.string.b97501_cell1_support_tip));
                                return false;
                            }
                        } else if (cell_id == GnbProtocol.CellId.THIRD) {
                            if (band != 1 && band != 41) {
                                setToastText(getString(R.string.b97501_cell2_support_tip));
                                return false;
                            }
                        } else {
                            if (band_lte != 1 && band_lte != 3 && band_lte != 5 && band_lte != 8) {
                                setToastText(getString(R.string.b97501_cell3_support_tip));
                                return false;
                            }
                        }
                    }
                    break;
                case 1:
                    if (PaCtl.build().isB97502) {
                        if (cell_id == GnbProtocol.CellId.FIRST) {
                            if (band != 78 && band != 79) {
                                setToastText("通道" + DeviceUtil.getCellStr(logicIndex, cell_id) + "仅支持输入N78/N79对应的频点!");
                                return false;
                            }
                        } else if (cell_id == GnbProtocol.CellId.SECOND) {
                            if (band != 28) {
                                setToastText("通道" + DeviceUtil.getCellStr(logicIndex, cell_id) + "仅支持输入N28对应的频点!");
                                return false;
                            }
                        }
                    }
                    break;
                case 2:
                    if (PaCtl.build().isB97502) {
                        if (cell_id == GnbProtocol.CellId.FIRST) {
                            if (band_lte != 34 && band_lte != 3) {
                                setToastText("通道" + DeviceUtil.getCellStr(logicIndex, cell_id) + "仅支持输入B3/B34对应的频点!");
                                return false;
                            }
                        } else if (cell_id == GnbProtocol.CellId.SECOND) {
                            if (band_lte != 39 && band_lte != 5) {
                                setToastText("通道" + DeviceUtil.getCellStr(logicIndex, cell_id) + "仅支持输入B5/B39对应的频点!");
                                return false;
                            }
                        }
                    }
                    break;
                case 3:
                    if (PaCtl.build().isB97502) {
                        if (cell_id == GnbProtocol.CellId.FIRST) {
                            if (band_lte != 40 && band_lte != 8) {
                                setToastText("通道" + DeviceUtil.getCellStr(logicIndex, cell_id) + "仅支持输入B8/B40对应的频点!");
                                return false;
                            }
                        } else if (cell_id == GnbProtocol.CellId.SECOND) {
                            if (band_lte != 41 && band_lte != 1) {
                                setToastText("通道" + DeviceUtil.getCellStr(logicIndex, cell_id) + "仅支持输入B1/B41对应的频点!");
                                return false;
                            }
                        }
                    }
                    break;
            }

            if (ed_pci_str.isEmpty()) {
                setToastText(cellStr + getString(R.string.pci_empty_tip));
                return false;
            }
            int pci = Integer.parseInt(ed_pci_str);

            if (band_lte != 0) {
                if (pci > 503) {
                    setToastText(getString(R.string.detect) + cellStr + getString(R.string.pci_err_tip_4g));
                    return false;
                }
            } else {
                if (pci >= 1008) {
                    setToastText(getString(R.string.detect) + cellStr + getString(R.string.pci_err_tip_5g));
                    return false;
                }
            }

            if (imsi_str.length() != GnbProtocol.MAX_IMSI_USE_LEN) {
                setToastText(cellStr + getString(R.string.imsi_err_tip));
                traceUtil.setPci(cell_id, ed_pci_str);
                return false;
            }

            if (!Util.existSameData(dropImsiList, imsi_str)) {
                if (dropImsiList.size() > 20) {
                    dropImsiList.remove(0);
                }
                dropImsiList.add(imsi_str);
                PrefUtil.build().setDropImsiList(dropImsiList);
                dropImsiAdapter.setList(dropImsiList);
            }

            String tracePlmn = imsi_str.substring(0, 5);
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
                    setToastText("IMSI输入错误,请检查输入值");
                    return false;
            }

            //频点和运营商配对
            String plmn = imsi2Plmn(imsi_str);
            String plmnFromArfcn = getPlmnFromArfcn(ed_arfcn_str);
            if (!isSameOperator(plmn, plmnFromArfcn)) {
                setToastText(cellStr + "频点" + ed_arfcn_str + "与imsi号运营商不一致，请修改配置");
                return false;
            }

            //BW:5g双通道TDD 100， FDD 20，  4g 5
            //SSB: 5g TDD 255， FDD 240,    4g 128，
            if (logicIndex == 0) {  //N41
                traceUtil.setSwap_rf(cell_id, 0);

                if (cell_id == GnbProtocol.CellId.FIRST) {
                    traceUtil.setBandWidth(cell_id, 100);
                    traceUtil.setSsbBitmap(cell_id, 255);
                } else if (cell_id == GnbProtocol.CellId.SECOND) {
                    traceUtil.setBandWidth(cell_id, 20);
                    traceUtil.setSsbBitmap(cell_id, 240);
                }
            } else if (logicIndex == 1) {   //N1
                traceUtil.setSwap_rf(cell_id, 0);

                if (cell_id == GnbProtocol.CellId.FIRST) {
                    traceUtil.setBandWidth(cell_id, 100);
                    traceUtil.setSsbBitmap(cell_id, 255);
                } else if (cell_id == GnbProtocol.CellId.SECOND) {
                    traceUtil.setBandWidth(cell_id, 20);
                    traceUtil.setSsbBitmap(cell_id, 240);
                }
            } else if (logicIndex == 2) {   //4g    //B34/B39/B3/B5
                traceUtil.setSwap_rf(cell_id, 0);
                traceUtil.setBandWidth(cell_id, 5);
                traceUtil.setSsbBitmap(cell_id, 128);
            } else if (logicIndex == 3) {   //B40/B41/B8/B1
                traceUtil.setSwap_rf(cell_id, 0);
                traceUtil.setBandWidth(cell_id, 5);
                traceUtil.setSsbBitmap(cell_id, 128);
            }
            traceUtil.setArfcn(cell_id, ed_arfcn_str);
            traceUtil.setPci(cell_id, ed_pci_str);
            traceUtil.setImsi(cell_id, imsi_str);
            traceUtil.setPlmn(cell_id, tracePlmn);

            setcfgEnable(-1, false);
        }
        AppLog.D("checkArfcnParamAndSetTraceUtil return true");
        return true;
    }

    public void setToastText(String s) {
        if (toastText.isEmpty())
            toastText = s;
    }

    String toastText = "";

    //将合格的频点加入配置mTraceCatchFragment.mTraceUtilNr中
    //并将最后一个存入Nx/Bx+cfg文件中
    public boolean checkArfcnParamAndSetTraceUtilAndCache() {
        AppLog.I("checkArfcnParamAndCacheAndSetTraceUtil()");
        boolean b = false;
        int imsiEmptyNum = 0;
        int channelCount = 0;
        StringBuffer param = new StringBuffer();
        String[] paramStr = new String[]{"", "", "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "", "", ""};    //24位
        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
            int logicIndex = DeviceUtil.getLogicIndexByDeviceName(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName());
            channelCount += 2;
            boolean ifIsFDD = false;    //双通道不能同时下FDD的
            for (int channelNum = logicIndex * 2 + 1; channelNum <= logicIndex * 2 + 2; channelNum++) { //双通道
                String ed_arfcn_str = "";
                String ed_pci_str = getPciText(channelNum);
                String imsi_str = getActv_imsiText(channelNum);
                LinkedList<Integer> TDN = getTDN(channelNum);
                int cell_id = DeviceUtil.getCellIdByChannelNum(channelNum);

                //改成循环判断，那么最后输入checkParam的数据就会是最后一个数据，其他的不影响，默认开启频点轮询,循环取TD1输入这里
//                for (Integer integer : TDN) {   //如果TD全是空不会走这里所以不会设置TraceUtil为空
                int lteBand = 0;
                if (TDN.size() > 0) {
                    Integer integer = TDN.get(0);
                    AppLog.D("checkArfcnParamAndCacheAndSetTraceUtil：" + "channelNum " + channelNum + "integer " + integer);
                    ed_arfcn_str = String.valueOf(integer);
                    lteBand = LteBand.earfcn2band(integer);
                }

                //不能同时为FDD
                if (logicIndex > 1) {    //4G
                    if (lteBand == 1 || lteBand == 3 || lteBand == 5 || lteBand == 8) {
                        AppLog.D("lteBand ==" + lteBand + "ifIsFDD " + ifIsFDD);
                        if (ifIsFDD) {
                            Util.showToast("禁止双通道下发FDD");
                            setcfgEnable(-1, true);
                            return false;
                        }
                        ifIsFDD = true;
                    }
                }
                boolean bi;
                //应该是true还是false呢？
//                if (imsi_str.isEmpty()) {   //走正常逻辑
//                imsiEmptyNum++;
                bi = checkArfcnParamAndSetTraceUtil(ed_arfcn_str, ed_pci_str, imsi_str, logicIndex, cell_id);
                b = b || bi;
//                } else {    //imsi填了表示需要检测，如果是false则全置为false不给通过
//                    bi = checkArfcnParamAndSetTraceUtil(ed_arfcn_str, ed_pci_str, imsi_str, logicIndex, cell_id);
//                    b = b && bi;
//                }


                AppLog.D("checkArfcnParam:" + "channelNum " + channelNum + "ed_arfcn_str " + ed_arfcn_str + "b " + b + "bi " + bi);
                //保存开启的数据
                paramStr[(channelNum - 1) * 3] = ed_arfcn_str;
                paramStr[(channelNum - 1) * 3 + 1] = ed_pci_str;
                paramStr[(channelNum - 1) * 3 + 2] = imsi_str;
//                }
            }
        }


        if (imsiEmptyNum == channelCount) {
            b = false;
        }

        //按顺序保存
        for (String str : paramStr) {
            if (str != null) {
                param.append(str).append("_");
            }
        }

        PrefUtil.build().putValue("cfg_param", param.toString());
        //待修改
        if (!b) {   //如果全不通过
            AppLog.D("checkArfcnParamAndCacheAndSetTraceUtil: setcfgEnable -1" + "b: " + b);
            setcfgEnable(-1, true);
            if (!toastText.isEmpty()) {
                Util.showToast(toastText);
            }
            return false;
        } else {
//            store8ChannelDataToPref();
        }
        return b;
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
                case 3:
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

    private LinkedList<Integer> getTDN(int channelNum) {
        switch (channelNum) {
            case 1:
                return TD1;
            case 2:
                return TD2;
            case 3:
                return TD3;
            case 4:
                return TD4;
            case 5:
                return TD5;
            case 6:
                return TD6;
            case 7:
                return TD7;
            case 8:
                return TD8;
        }
        return TD1;
    }

    private String getActv_imsiText(int channelNum) {
        switch (channelNum) {
            case 1:
                return actv_imsi.getText().toString();
            case 2:
                return actv_imsi_2.getText().toString();
            case 3:
                return actv_imsi_3.getText().toString();
            case 4:
                return actv_imsi_4.getText().toString();
            case 5:
                return actv_imsi_5.getText().toString();
            case 6:
                return actv_imsi_6.getText().toString();
            case 7:
                return actv_imsi_7.getText().toString();
            case 8:
                return actv_imsi_8.getText().toString();
        }
        return actv_imsi.getText().toString();
    }

    private String getPciText(int channelNum) {
        switch (channelNum) {
            case 1:
                return ed_pci_1.getText().toString();
            case 2:
                return ed_pci_2.getText().toString();
            case 3:
                return ed_pci_3.getText().toString();
            case 4:
                return ed_pci_4.getText().toString();
            case 5:
                return ed_pci_5.getText().toString();
            case 6:
                return ed_pci_6.getText().toString();
            case 7:
                return ed_pci_7.getText().toString();
            case 8:
                return ed_pci_8.getText().toString();
        }
        return ed_pci_1.getText().toString();
    }

    //1
    //设置定位配置页能否更改   //默认全禁，启动时不开启
    public void setcfgEnable(int channelIndex, boolean enable) {
        AppLog.D("setcfgEnable channelIndex = " + channelIndex + ", enable = " + enable);
        if (et_arfcn_1 == null) return;
        if (channelIndex == -1) {
            setcfgEnable(10, enable);
//            trace_group_all.setVisibility(enable ? View.VISIBLE : View.GONE);
            return;
        }
        if (channelIndex == 0 || channelIndex == 10) {
//            TD1_adapter.setFocusable(enable);
            et_arfcn_1.setClickable(enable);
            et_arfcn_1.setFocusable(enable);
            et_arfcn_1.setFocusableInTouchMode(enable);

            ed_pci_1.setClickable(enable);
            ed_pci_1.setFocusable(enable);
            ed_pci_1.setFocusableInTouchMode(enable);

            actv_imsi.setClickable(enable);
            actv_imsi.setFocusable(enable);
            actv_imsi.setFocusableInTouchMode(enable);

            trace_group_1.setVisibility(enable ? View.VISIBLE : View.GONE);

            if (channelIndex == 10) setcfgEnable(11, enable);
        } else if (channelIndex == 1 || channelIndex == 11) {
//            TD2_adapter.setFocusable(enable);
            et_arfcn_2.setClickable(enable);
            et_arfcn_2.setFocusable(enable);
            et_arfcn_2.setFocusableInTouchMode(enable);

            ed_pci_2.setClickable(enable);
            ed_pci_2.setFocusable(enable);
            ed_pci_2.setFocusableInTouchMode(enable);

            actv_imsi_2.setClickable(enable);
            actv_imsi_2.setFocusable(enable);
            actv_imsi_2.setFocusableInTouchMode(enable);

            trace_group_2.setVisibility(enable ? View.VISIBLE : View.GONE);

            if (channelIndex == 11) setcfgEnable(12, enable);
        } else if (channelIndex == 2 || channelIndex == 12) {
//            TD3_adapter.setFocusable(enable);
            et_arfcn_3.setClickable(enable);
            et_arfcn_3.setFocusable(enable);
            et_arfcn_3.setFocusableInTouchMode(enable);

            ed_pci_3.setClickable(enable);
            ed_pci_3.setFocusable(enable);
            ed_pci_3.setFocusableInTouchMode(enable);

            actv_imsi_3.setClickable(enable);
            actv_imsi_3.setFocusable(enable);
            actv_imsi_3.setFocusableInTouchMode(enable);

            trace_group_3.setVisibility(enable ? View.VISIBLE : View.GONE);

            if (channelIndex == 12) setcfgEnable(13, enable);
        } else if (channelIndex == 3 || channelIndex == 13) {
//            TD4_adapter.setFocusable(enable);
            et_arfcn_4.setClickable(enable);
            et_arfcn_4.setFocusable(enable);
            et_arfcn_4.setFocusableInTouchMode(enable);

            ed_pci_4.setClickable(enable);
            ed_pci_4.setFocusable(enable);
            ed_pci_4.setFocusableInTouchMode(enable);

            actv_imsi_4.setClickable(enable);
            actv_imsi_4.setFocusable(enable);
            actv_imsi_4.setFocusableInTouchMode(enable);

            trace_group_4.setVisibility(enable ? View.VISIBLE : View.GONE);

            if (channelIndex == 13) setcfgEnable(14, enable);
        } else if (channelIndex == 4 || channelIndex == 14) {
//            TD5_adapter.setFocusable(enable);
            et_arfcn_5.setClickable(enable);
            et_arfcn_5.setFocusable(enable);
            et_arfcn_5.setFocusableInTouchMode(enable);

            ed_pci_5.setClickable(enable);
            ed_pci_5.setFocusable(enable);
            ed_pci_5.setFocusableInTouchMode(enable);

            actv_imsi_5.setClickable(enable);
            actv_imsi_5.setFocusable(enable);
            actv_imsi_5.setFocusableInTouchMode(enable);

            trace_group_5.setVisibility(enable ? View.VISIBLE : View.GONE);

            if (channelIndex == 14) setcfgEnable(15, enable);
        } else if (channelIndex == 5 || channelIndex == 15) {
//            TD6_adapter.setFocusable(enable);
            et_arfcn_6.setClickable(enable);
            et_arfcn_6.setFocusable(enable);
            et_arfcn_6.setFocusableInTouchMode(enable);

            ed_pci_6.setClickable(enable);
            ed_pci_6.setFocusable(enable);
            ed_pci_6.setFocusableInTouchMode(enable);

            actv_imsi_6.setClickable(enable);
            actv_imsi_6.setFocusable(enable);
            actv_imsi_6.setFocusableInTouchMode(enable);

            trace_group_6.setVisibility(enable ? View.VISIBLE : View.GONE);

            if (channelIndex == 15) setcfgEnable(16, enable);
        } else if (channelIndex == 6 || channelIndex == 16) {
//            TD7_adapter.setFocusable(enable);
            et_arfcn_7.setClickable(enable);
            et_arfcn_7.setFocusable(enable);
            et_arfcn_7.setFocusableInTouchMode(enable);

            ed_pci_7.setClickable(enable);
            ed_pci_7.setFocusable(enable);
            ed_pci_7.setFocusableInTouchMode(enable);

            actv_imsi_7.setClickable(enable);
            actv_imsi_7.setFocusable(enable);
            actv_imsi_7.setFocusableInTouchMode(enable);

            trace_group_7.setVisibility(enable ? View.VISIBLE : View.GONE);

            if (channelIndex == 16) setcfgEnable(17, enable);
        } else if (channelIndex == 7 || channelIndex == 17) {
//            TD8_adapter.setFocusable(enable);
            et_arfcn_8.setClickable(enable);
            et_arfcn_8.setFocusable(enable);
            et_arfcn_8.setFocusableInTouchMode(enable);

            ed_pci_8.setClickable(enable);
            ed_pci_8.setFocusable(enable);
            ed_pci_8.setFocusableInTouchMode(enable);

            actv_imsi_8.setClickable(enable);
            actv_imsi_8.setFocusable(enable);
            actv_imsi_8.setFocusableInTouchMode(enable);

            trace_group_8.setVisibility(enable ? View.VISIBLE : View.GONE);
        }

    }

    public void setFreqList(List<ScanArfcnBean> list) {
        cfgTraceChildFreqList = list;
    }

    private String getFreqList() {
        StringBuffer sb = new StringBuffer();
        for (ScanArfcnBean bean : cfgTraceChildFreqList) {
            sb.append(bean.getDl_arfcn()).append(",");
        }
        return sb.toString();
    }

    private ArrayList<ScanArfcnBean> getScanArfcnBeanList() {
        return MainActivity.getInstance().getScanArfcnBeanList();
    }

    //1
    //自动计算pci并将pci设置到页面
    public boolean importArfcn(ScanArfcnBean bean, int channelNum, int inPci) {
        AppLog.I("importArfcn: channelNum = " + channelNum + ", inPci = " + inPci);
        int band;
        int arfcn = bean.getDl_arfcn();
        int pci = bean.getPci();
        boolean isOk = false;
        if (arfcn < 100000) {
            band = LteBand.earfcn2band(arfcn);
            // 4G PCI 算法： 公网pci % 3，取余 +- 1，如果为 0则+1， 2则-1， 1则+-1都可以
            if (pci % 3 == 2) pci -= 1;
            else pci += 1;

            //待修改：载波分裂
            if (PaCtl.build().isB97502) {
                switch (band) {
                    case 34:
                    case 3:
                        if (channelNum == -1 || channelNum == 5) {
                            et_arfcn_5.setText(String.valueOf(arfcn));
                            ed_pci_5.setText(String.valueOf(pci));
                            isOk = true;
                        }
                        break;
                    case 39:
                    case 5:
                        if (channelNum == -1 || channelNum == 6) {
                            et_arfcn_6.setText(String.valueOf(arfcn));
                            ed_pci_6.setText(String.valueOf(pci));
                            isOk = true;
                        }
                        break;
                    case 40:
                    case 8:
                        if (channelNum == -1 || channelNum == 7) {
                            et_arfcn_7.setText(String.valueOf(arfcn));
                            ed_pci_7.setText(String.valueOf(pci));
                            isOk = true;
                        }
                        break;
                    case 41:
                    case 1:
                        if (channelNum == -1 || channelNum == 8) {
                            et_arfcn_8.setText(String.valueOf(arfcn));
                            ed_pci_8.setText(String.valueOf(pci));
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
                        if (channelNum == -1 || channelNum == 3) {
//                            ed_arfcn_4.setText(String.valueOf(arfcn));
//                            TD4_adapter.addData(String.valueOf(arfcn));
                            ed_pci_4.setText(String.valueOf(pci));
                            isOk = true;
                        }
                        break;
                    case 34:
                    case 39:
                    case 40:
                    case 41:
                        if (channelNum == -1 || channelNum == 1) {
//                            ed_arfcn_2.setText(String.valueOf(arfcn));
//                            TD2_adapter.addData(String.valueOf(arfcn));
                            ed_pci_2.setText(String.valueOf(pci));
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
                        if (channelNum == -1 || channelNum == 1) {
                            et_arfcn_1.setText(String.valueOf(arfcn));
                            ed_pci_1.setText(String.valueOf(pci));
                            isOk = true;
                        }
                    } else {
                        if (channelNum == -1 || channelNum == 2) {
//                            ed_arfcn_3.setText(String.valueOf(arfcn));
//                            TD3_adapter.addData(String.valueOf(arfcn));
                            ed_pci_3.setText(String.valueOf(pci));
                            isOk = true;
                        }
                    }
                    break;
                case 1:
                    if (channelNum == -1 || channelNum == 2) {
                        et_arfcn_2.setText(String.valueOf(arfcn));
                        ed_pci_2.setText(String.valueOf(pci));
                        isOk = true;
                    }
                    break;
                case 78:
                case 79:
                    if (channelNum == -1 || channelNum == 3) {
                        et_arfcn_3.setText(String.valueOf(arfcn));
                        ed_pci_3.setText(String.valueOf(pci));
                        isOk = true;
                    }
                    break;
                case 28:
                    if (channelNum == -1 || channelNum == 4) {
                        et_arfcn_4.setText(String.valueOf(arfcn));
                        ed_pci_4.setText(String.valueOf(pci));
                        isOk = true;
                    }
                    break;
            }
        }
        return isOk;
    }

    //1
    //自动计算pci并将pci保存到本地、将arfcn和pci更新到页面
    public boolean importArfcn2(ScanArfcnBean bean, int cell_id, int inPci) {
        AppLog.I("importArfcn2: cell_id = " + cell_id + ", inPci = " + inPci);
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
                    case 34:
                    case 3:
                        if (cell_id == -1 || cell_id == 4) {
                            et_arfcn_5.setText(String.valueOf(arfcn));
                            ed_pci_5.setText(String.valueOf(pci));
                            //保存pci自动计算
                            PrefUtil.build().putValue("PCI_TD5", String.valueOf(pci));
                            isOk = true;
                        }
                        break;
                    case 39:
                    case 5:
                        if (cell_id == -1 || cell_id == 5) {
                            et_arfcn_6.setText(String.valueOf(arfcn));
                            ed_pci_6.setText(String.valueOf(pci));
                            //保存pci自动计算
                            PrefUtil.build().putValue("PCI_TD6", String.valueOf(pci));
                            isOk = true;
                        }
                        break;
                    case 40:
                    case 8:
                        if (cell_id == -1 || cell_id == 6) {
                            et_arfcn_7.setText(String.valueOf(arfcn));
                            ed_pci_7.setText(String.valueOf(pci));
                            //保存pci自动计算
                            PrefUtil.build().putValue("PCI_TD7", String.valueOf(pci));
                            isOk = true;
                        }
                        break;
                    case 41:
                    case 1:
                        if (cell_id == -1 || cell_id == 7) {
                            et_arfcn_8.setText(String.valueOf(arfcn));
                            ed_pci_8.setText(String.valueOf(pci));
                            PrefUtil.build().putValue("PCI_TD8", String.valueOf(pci));
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
                        if (cell_id == -1 || cell_id == 3) {
//                            ed_arfcn_4.setText(String.valueOf(arfcn));
//                            TD4_adapter.addData(String.valueOf(arfcn));
//                            ed_pci_4.setText(String.valueOf(pci));
                            PrefUtil.build().putValue("PCI_TD4", String.valueOf(pci));
                            isOk = true;
                        }
                        break;
                    case 34:
                    case 39:
                    case 40:
                    case 41:
                        if (cell_id == -1 || cell_id == 1) {
//                            ed_arfcn_2.setText(String.valueOf(arfcn));
//                            TD2_adapter.addData(String.valueOf(arfcn));
//                            ed_pci_2.setText(String.valueOf(pci));
                            PrefUtil.build().putValue("PCI_TD2", String.valueOf(pci));
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
                        if (cell_id == -1 || cell_id == 0) {
                            et_arfcn_1.setText(String.valueOf(arfcn));
                            ed_pci_1.setText(String.valueOf(pci));
                            PrefUtil.build().putValue("PCI_TD1", String.valueOf(pci));
                            isOk = true;
                        }
                    } else {
                        if (cell_id == -1 || cell_id == 2) {
//                            ed_arfcn_3.setText(String.valueOf(arfcn));
//                            TD3_adapter.addData(String.valueOf(arfcn));
//                            ed_pci_3.setText(String.valueOf(pci));
                            et_arfcn_3.setText(String.valueOf(arfcn));
                            ed_pci_3.setText(String.valueOf(pci));
                            PrefUtil.build().putValue("PCI_TD3", String.valueOf(pci));
                            isOk = true;
                        }
                    }
                    break;
                case 1:
                    if (cell_id == -1 || cell_id == 1) {
                        et_arfcn_2.setText(String.valueOf(arfcn));
                        ed_pci_2.setText(String.valueOf(pci));
                        PrefUtil.build().putValue("PCI_TD2", String.valueOf(pci));
                        isOk = true;
                    }
                    break;
                case 78:
                case 79:
                    if (cell_id == -1 || cell_id == 2) {
                        et_arfcn_3.setText(String.valueOf(arfcn));
                        ed_pci_3.setText(String.valueOf(pci));
                        PrefUtil.build().putValue("PCI_TD3", String.valueOf(pci));
                        isOk = true;
                    }
                    break;
                case 28:
                    if (PaCtl.build().isB97502) {
                        et_arfcn_4.setText(String.valueOf(arfcn));
                        ed_pci_4.setText(String.valueOf(pci));
                        if (cell_id == -1 || cell_id == 3) {
                            PrefUtil.build().putValue("PCI_TD4", String.valueOf(pci));
                            isOk = true;
                        }
                    } else {
                        if (cell_id == -1 || cell_id == 0) {
//                            ed_arfcn.setText(String.valueOf(arfcn));
//                            TD1_adapter.addData(String.valueOf(arfcn));
//                            ed_pci_1.setText(String.valueOf(pci));
                            PrefUtil.build().putValue("PCI_TD1", String.valueOf(pci));
                            isOk = true;
                        }
                    }
                    break;
            }
        }
        return isOk;
    }

    public boolean isChangeAndAdd1() {
        AppLog.I("isChangeAndAdd1");
        //改为不需要判断是否改变直接判断是否属于通道
//        String text = TD1_adapter.getLastViewText();
        String text = "";
        if (text != null && !text.isEmpty()) {
            int band = NrBand.earfcn2band(Integer.parseInt(text));
            if (band != 28 && band != 78 && band != 79) {
                Util.showToast("新频点数据值不属于通道一，请修改!");
                return false;
            }
            int arfcn = Integer.parseInt(text);
            if (!TD1.contains(arfcn)) TD1.add(arfcn);
        }
        if (text != null && text.isEmpty()) {
            Util.showToast("频点不能为空！");
            return false;
        }


//        text = TD2_adapter.getLastViewText();
        if (text != null && !text.isEmpty()) {
            int band = LteBand.earfcn2band(Integer.parseInt(text));
            if (band != 34 && band != 39 && band != 40 && band != 41) {
                Util.showToast("新频点数据值不属于通道二，请修改!");
                return false;
            }

            int arfcn = Integer.parseInt(text);
            if (!TD2.contains(arfcn)) TD2.add(arfcn);
        }


//        text = TD3_adapter.getLastViewText();
        if (text != null && !text.isEmpty()) {
            int band = NrBand.earfcn2band(Integer.parseInt(text));
            if (band != 1 && band != 41) {
                Util.showToast("新频点数据值不属于通道三，请修改!");
                return false;
            }

            int arfcn = Integer.parseInt(text);
            if (!TD3.contains(arfcn)) TD3.add(arfcn);
        }


//        text = TD4_adapter.getLastViewText();
        if (text != null && !text.isEmpty()) {
            int band = LteBand.earfcn2band(Integer.parseInt(text));
            if (band != 1 && band != 3 && band != 5 && band != 8) {
                Util.showToast("新频点数据值不属于通道四，请修改!");
                return false;
            }

            int arfcn = Integer.parseInt(text);
            if (!TD4.contains(arfcn)) TD4.add(arfcn);
        }

        return true;
    }

    //将lastEditView如果合格加入TDx
    public boolean isChangeAndAdd2ToTD() {
        AppLog.I("isChangeAndAdd2ToTD");
        //改为不需要判断是否改变直接判断是否属于通道
        String text = et_arfcn_1.getEditableText().toString();
        if (text != null && !text.isEmpty()) {
            int band = NrBand.earfcn2band(Integer.parseInt(text));
            if (band != 41) {
                Util.showToast("新频点数据值不属于通道一，请修改!");
                return false;
            }
            int arfcn = Integer.parseInt(text);
            TD1.clear();
            if (!TD1.contains(arfcn)) TD1.add(arfcn);
        } else {
            TD1.clear();
        }

        String text2 = et_arfcn_2.getEditableText().toString();
        if (text2 != null && !text2.isEmpty()) {
            int band = NrBand.earfcn2band(Integer.parseInt(text2));
            if (band != 1) {
                Util.showToast("新频点数据值不属于通道二，请修改!");
                return false;
            }
            int arfcn = Integer.parseInt(text2);
            TD2.clear();
            if (!TD2.contains(arfcn)) TD2.add(arfcn);
        } else {
            TD2.clear();
        }


        String text3 = et_arfcn_3.getEditableText().toString();
        if (text3 != null && !text3.isEmpty()) {
            int band = NrBand.earfcn2band(Integer.parseInt(text3));
            if (band != 78 && band != 79) {
                Util.showToast("新频点数据值不属于通道三，请修改!");
                return false;
            }
            int arfcn = Integer.parseInt(text3);
            TD3.clear();
            if (!TD3.contains(arfcn)) TD3.add(arfcn);
        } else {
            TD3.clear();
        }

        String text4 = et_arfcn_4.getEditableText().toString();
        if (text4 != null && !text4.isEmpty()) {
            int band = NrBand.earfcn2band(Integer.parseInt(text4));
            if (band != 28) {
                Util.showToast("新频点数据值不属于通道四，请修改!");
                return false;
            }

            int arfcn = Integer.parseInt(text4);
            TD4.clear();
            if (!TD4.contains(arfcn)) TD4.add(arfcn);
        } else {
            TD4.clear();
        }

        String text5 = et_arfcn_5.getEditableText().toString();
        if (text5 != null && !text5.isEmpty()) {
            int band_lte = LteBand.earfcn2band(Integer.parseInt(text5));
            if (band_lte != 34 && band_lte != 3) {
                Util.showToast("新频点数据值不属于通道五，请修改!");
                return false;
            }

            int arfcn = Integer.parseInt(text5);
            TD5.clear();
            if (!TD5.contains(arfcn)) TD5.add(arfcn);
        } else {
            TD5.clear();
        }

        String text6 = et_arfcn_6.getEditableText().toString();
        if (text6 != null && !text6.isEmpty()) {
            int band_lte = LteBand.earfcn2band(Integer.parseInt(text6));
            if (band_lte != 39 && band_lte != 5) {
                Util.showToast("新频点数据值不属于通道六，请修改!");
                return false;
            }

            int arfcn = Integer.parseInt(text6);
            TD6.clear();
            if (!TD6.contains(arfcn)) TD6.add(arfcn);
        } else {
            TD6.clear();
        }

        String text7 = et_arfcn_7.getEditableText().toString();
        if (text7 != null && !text7.isEmpty()) {
            int band_lte = LteBand.earfcn2band(Integer.parseInt(text7));
            if (band_lte != 40 && band_lte != 8) {
                Util.showToast("新频点数据值不属于通道七，请修改!");
                return false;
            }

            int arfcn = Integer.parseInt(text7);
            TD7.clear();
            if (!TD7.contains(arfcn)) TD7.add(arfcn);
        } else {
            TD7.clear();
        }

        String text8 = et_arfcn_8.getEditableText().toString();
        if (text8 != null && !text8.isEmpty()) {
            int band_lte = LteBand.earfcn2band(Integer.parseInt(text8));
            if (band_lte != 41 && band_lte != 1) {
                Util.showToast("新频点数据值不属于通道八，请修改!");
                return false;
            }

            int arfcn = Integer.parseInt(text8);
            TD8.clear();
            if (!TD8.contains(arfcn)) TD8.add(arfcn);
        } else {
            TD8.clear();
        }
        return true;
    }

    //1
    //将合格的pci存"PCI_TD1"文件
    public boolean isPciFalseParam() {
        AppLog.I("isPciFalseParam");
        String ed_pci_str = ed_pci_1.getText().toString();
        //如果arfcn有值，pci需要不为空
        if (TD1.size() > 0) {
            if (ed_pci_str.isEmpty()) {
                Util.showToast("通道一PCI不能为空");
                return true;
            }

            int pci = Integer.parseInt(ed_pci_str);
            if (pci > 1007) {
                Util.showToast("通道一PCI数据错误,取值范围：0~1007");
                return true;
            }
        }

        String ed_pci_str_2 = ed_pci_2.getText().toString();
        if (TD2.size() > 0) {
            if (ed_pci_str_2.isEmpty()) {
                Util.showToast("通道二PCI不能为空");
                return true;
            }

            int pci2 = Integer.parseInt(ed_pci_str_2);
            if (pci2 > 1007) {
                Util.showToast("通道二PCI数据错误,取值范围：0~1007");
                return true;
            }
        }

        String ed_pci_str_3 = ed_pci_3.getText().toString();
        if (TD3.size() > 0) {
            if (ed_pci_str_3.isEmpty()) {
                Util.showToast("通道三PCI不能为空");
                return true;
            }

            int pci3 = Integer.parseInt(ed_pci_str_3);
            if (pci3 > 1007) {
                Util.showToast("通道三PCI数据错误,取值范围：0~1007");
                return true;
            }
        }

        String ed_pci_str_4 = ed_pci_4.getText().toString();
        if (TD4.size() > 0) {
            if (ed_pci_str_4.isEmpty()) {
                Util.showToast("通道四PCI不能为空");
                return true;
            }

            int pci4 = Integer.parseInt(ed_pci_str_4);
            if (pci4 > 1007) {
                Util.showToast("通道四PCI数据错误,取值范围：0~1007");
                return true;
            }
        }

        String ed_pci_str_5 = ed_pci_5.getText().toString();
        if (TD5.size() > 0) {
            if (ed_pci_str_5.isEmpty()) {
                Util.showToast("通道五PCI不能为空");
                return true;
            }

            int pci5 = Integer.parseInt(ed_pci_str_5);
            if (pci5 > 503) {
                Util.showToast("通道五PCI数据错误,取值范围：0~503");
                return true;
            }
        }

        String ed_pci_str_6 = ed_pci_6.getText().toString();
        if (TD6.size() > 0) {
            if (ed_pci_str_6.isEmpty()) {
                Util.showToast("通道六PCI不能为空");
                return true;
            }

            int pci6 = Integer.parseInt(ed_pci_str_6);
            if (pci6 > 503) {
                Util.showToast("通道六PCI数据错误,取值范围：0~503");
                return true;
            }
        }

        String ed_pci_str_7 = ed_pci_7.getText().toString();
        if (TD7.size() > 0) {
            if (ed_pci_str_7.isEmpty()) {
                Util.showToast("通道七PCI不能为空");
                return true;
            }

            int pci7 = Integer.parseInt(ed_pci_str_7);
            if (pci7 > 503) {
                Util.showToast("通道七PCI数据错误,取值范围：0~503");
                return true;
            }
        }

        String ed_pci_str_8 = ed_pci_8.getText().toString();
        if (TD8.size() > 0) {
            if (ed_pci_str_8.isEmpty()) {
                Util.showToast("通道八PCI不能为空");
                return true;
            }

            int pci8 = Integer.parseInt(ed_pci_str_8);
            if (pci8 > 503) {
                Util.showToast("通道八PCI数据错误,取值范围：0~503");
                return true;
            }
        }

        PrefUtil.build().putValue("PCI_TD1", ed_pci_1.getText().toString());
        PrefUtil.build().putValue("PCI_TD2", ed_pci_2.getText().toString());
        PrefUtil.build().putValue("PCI_TD3", ed_pci_3.getText().toString());
        PrefUtil.build().putValue("PCI_TD4", ed_pci_4.getText().toString());
        PrefUtil.build().putValue("PCI_TD5", ed_pci_5.getText().toString());
        PrefUtil.build().putValue("PCI_TD6", ed_pci_6.getText().toString());
        PrefUtil.build().putValue("PCI_TD7", ed_pci_7.getText().toString());
        PrefUtil.build().putValue("PCI_TD8", ed_pci_8.getText().toString());
        return false;
    }


    boolean hasChange = false;
    boolean isChange0 = false;
    boolean isChange1 = false;
    boolean isChange2 = false;
    boolean isChange3 = false;
    boolean isChange4 = false;
    boolean isChange5 = false;
    boolean isChange6 = false;
    boolean isChange7 = false;

    //待修改：载波分裂
    //通过TDx判断是否修改了通道内容并将TDx存入"Auto_Arfcn_TDx"文件和map中，返回是否修改过
    public boolean isChangeArfcnUpdateTDAndMap() {
        AppLog.I("isChangeArfcnUpdateTDAndMap");
        for (int i = 1; i < 9; i++) {
            ifChangeUpdateTDAndMap(i);
        }

        if (hasChange) {
            if (!isChange0) {
                TD1.clear();
                TD1.add(504990);
                PrefUtil.build().putValue("Auto_Arfcn_TD1", String.valueOf(504990));
                mTraceCatchFragment.arfcnBeanHashMap.put("TD1", new ArfcnBean(TD1));
            }

            if (!isChange1) {
                TD2.clear();
                TD2.add(427250);
                PrefUtil.build().putValue("Auto_Arfcn_TD2", String.valueOf(427250));
                mTraceCatchFragment.arfcnBeanHashMap.put("TD2", new ArfcnBean(TD2));
            }

            if (!isChange2) {
                TD3.clear();
                TD3.add(627264);
                PrefUtil.build().putValue("Auto_Arfcn_TD3", String.valueOf(627264));
                mTraceCatchFragment.arfcnBeanHashMap.put("TD3", new ArfcnBean(TD3));
            }

            if (!isChange3) {
                TD4.clear();
                TD4.add(154810);
                PrefUtil.build().putValue("Auto_Arfcn_TD4", String.valueOf(154810));
                mTraceCatchFragment.arfcnBeanHashMap.put("TD4", new ArfcnBean(TD4));
            }

            if (!isChange4) {
                TD5.clear();
                TD5.add(36275);
                PrefUtil.build().putValue("Auto_Arfcn_TD5", String.valueOf(36275));
                mTraceCatchFragment.arfcnBeanHashMap.put("TD5", new ArfcnBean(TD5));
            }
            if (!isChange5) {
                TD6.clear();
                TD6.add(38400);
                PrefUtil.build().putValue("Auto_Arfcn_TD6", String.valueOf(38400));
                mTraceCatchFragment.arfcnBeanHashMap.put("TD6", new ArfcnBean(TD6));
            }
            if (!isChange6) {
                TD7.clear();
                TD7.add(38950);
                PrefUtil.build().putValue("Auto_Arfcn_TD7", String.valueOf(38950));
                mTraceCatchFragment.arfcnBeanHashMap.put("TD7", new ArfcnBean(TD7));
            }
            if (!isChange7) {
                TD8.clear();
                TD8.add(40936);
                PrefUtil.build().putValue("Auto_Arfcn_TD8", String.valueOf(40936));
                mTraceCatchFragment.arfcnBeanHashMap.put("TD8", new ArfcnBean(TD8));
            }
        }
        AppLog.D("isChangeArfcn():" + "hasChange" + hasChange + "isChange0" + isChange0 + "isChange1" + isChange1 + "isChange2" + isChange2 + "isChange3" + isChange3 + "isChange4" + isChange4 + "isChange5" + isChange5 + "isChange6" + isChange6 + "isChange7" + isChange7);
        return hasChange;
    }

    private void ifChangeUpdateTDAndMap(int channelNum) {
        AppLog.I("ifChangeUpdateTDAndMap");
        LinkedList<Integer> TD = TD1;
        switch (channelNum) {
            case 1:
                TD = TD1;
                break;
            case 2:
                TD = TD2;
                break;
            case 3:
                TD = TD3;
                break;
            case 4:
                TD = TD4;
                break;
            case 5:
                TD = TD5;
                break;
            case 6:
                TD = TD6;
                break;
            case 7:
                TD = TD7;
                break;
            case 8:
                TD = TD8;
                break;
        }
        int arfcn = 504990; //默认的频点
        switch (channelNum) {
            case 1:
                arfcn = 504990;
                break;
            case 2:
                arfcn = 427250;
                break;
            case 3:
                arfcn = 627264;
                break;
            case 4:
                arfcn = 154810;
                break;
            case 5:
                arfcn = 36275;
                break;
            case 6:
                arfcn = 38400;
                break;
            case 7:
                arfcn = 38950;
                break;
            case 8:
                arfcn = 40936;
                break;
        }
        boolean isChange = getIsChange(channelNum);
        StringBuilder str = new StringBuilder();

        if (TD.size() != 1 || TD.get(0) != arfcn) {
            hasChange = true;
            isChange = true;
            for (Integer integer : TD) {
                str.append(integer);
                str.append("_");
            }

            if (str.length() > 0) str = new StringBuilder(str.substring(0, str.length() - 1));
            PrefUtil.build().putValue("Auto_Arfcn_TD" + channelNum, str.toString());
            mTraceCatchFragment.arfcnBeanHashMap.put("TD" + channelNum, new ArfcnBean(TD));
            AppLog.D("ifChangeUpdateTD:hasChange" + "TD" + channelNum + ":" + str.toString());
        } else if (TD.size() == 1 && TD.get(0) == arfcn) {
            mTraceCatchFragment.arfcnBeanHashMap.put("TD" + channelNum, new ArfcnBean(TD));
            AppLog.D("ifChangeUpdateTD:noChange" + "TD" + channelNum + ":" + str.toString());
        }
        setIsChange(channelNum, isChange);
    }

    private void setIsChange(int channelNum, boolean isChange) {
        switch (channelNum) {
            case 1:
                isChange0 = isChange;
                break;
            case 2:
                isChange1 = isChange;
                break;
            case 3:
                isChange2 = isChange;
                break;
            case 4:
                isChange3 = isChange;
                break;
            case 5:
                isChange4 = isChange;
                break;
            case 6:
                isChange5 = isChange;
                break;
            case 7:
                isChange6 = isChange;
                break;
            case 8:
                isChange7 = isChange;
                break;
        }
    }

    private boolean getIsChange(int channelNum) {
        switch (channelNum) {
            case 1:
                return isChange0;
            case 2:
                return isChange1;
            case 3:
                return isChange2;
            case 4:
                return isChange3;
            case 5:
                return isChange4;
            case 6:
                return isChange5;
            case 7:
                return isChange6;
            case 8:
                return isChange7;
        }
        return isChange0;
    }

    private void clearList() {
        TD1.clear();
        TD2.clear();
        TD3.clear();
        TD4.clear();
        TD5.clear();
        TD6.clear();
        TD7.clear();
        TD8.clear();
    }

    //重设频点轮询的arfcn列表
    public void resetLastAutoArfcnList1() {
        clearList();
        // N28/N78/N79
        String listStr = PrefUtil.build().getValue("Auto_Arfcn_TD1", "-1").toString();
        if (listStr.equals("-1")) TD1.add(627264);
        else if (!listStr.isEmpty()) {
            String[] strings = listStr.split("_");
            for (String string : strings) TD1.add(Integer.parseInt(string));
        }

        // B34/B39/B40/B41
        listStr = PrefUtil.build().getValue("Auto_Arfcn_TD2", "-1").toString();
        if (listStr.equals("-1")) TD2.add(1825);
        else if (!listStr.isEmpty()) {
            String[] strings = listStr.split("_");
            for (String string : strings) TD2.add(Integer.parseInt(string));
        }

        // N1/N41
        listStr = PrefUtil.build().getValue("Auto_Arfcn_TD3", "-1").toString();
        if (listStr.equals("-1")) TD3.add(154810);
        else if (!listStr.isEmpty()) {
            String[] strings = listStr.split("_");
            for (String string : strings) TD3.add(Integer.parseInt(string));
        }

        // B1/B3/B5/B8
        listStr = PrefUtil.build().getValue("Auto_Arfcn_TD4", "-1").toString();
        if (listStr.equals("-1")) TD4.add(38950);
        else if (!listStr.isEmpty()) {
            String[] strings = listStr.split("_");
            for (String string : strings) TD4.add(Integer.parseInt(string));
        }

        mTraceCatchFragment.arfcnBeanHashMap.put("TD1", new ArfcnBean(TD1));
        mTraceCatchFragment.arfcnBeanHashMap.put("TD2", new ArfcnBean(TD2));
        mTraceCatchFragment.arfcnBeanHashMap.put("TD3", new ArfcnBean(TD3));
        mTraceCatchFragment.arfcnBeanHashMap.put("TD4", new ArfcnBean(TD4));
        mTraceCatchFragment.arfcnBeanHashMap.put("TD5", new ArfcnBean(TD5));
        mTraceCatchFragment.arfcnBeanHashMap.put("TD6", new ArfcnBean(TD6));
        mTraceCatchFragment.arfcnBeanHashMap.put("TD7", new ArfcnBean(TD7));
        mTraceCatchFragment.arfcnBeanHashMap.put("TD8", new ArfcnBean(TD8));

        listStr = PrefUtil.build().getValue("PCI_TD1", "1001").toString();
        ed_pci_1.setText(listStr);
        listStr = PrefUtil.build().getValue("PCI_TD2", "1002").toString();
        ed_pci_2.setText(listStr);
        listStr = PrefUtil.build().getValue("PCI_TD3", "1003").toString();
        ed_pci_3.setText(listStr);
        listStr = PrefUtil.build().getValue("PCI_TD4", "1004").toString();
        ed_pci_4.setText(listStr);
        listStr = PrefUtil.build().getValue("PCI_TD5", "501").toString();
        ed_pci_5.setText(listStr);
        listStr = PrefUtil.build().getValue("PCI_TD6", "502").toString();
        ed_pci_6.setText(listStr);
        listStr = PrefUtil.build().getValue("PCI_TD7", "503").toString();
        ed_pci_7.setText(listStr);
        listStr = PrefUtil.build().getValue("PCI_TD8", "504").toString();
        ed_pci_8.setText(listStr);
    }

    //重设频点轮询的arfcn列表
    public void resetLastAutoArfcnList2() {
        clearList();
        // N41/N78/N79
        String listStr = PrefUtil.build().getValue("Auto_Arfcn_TD1", "-1").toString();
        if (listStr.equals("-1")) TD1.add(627264);
        else if (!listStr.isEmpty()) {
            String[] strings = listStr.split("_");
            for (String string : strings) TD1.add(Integer.parseInt(string));
        }

        // B3/B5/B8
        listStr = PrefUtil.build().getValue("Auto_Arfcn_TD2", "-1").toString();
        if (listStr.equals("-1")) TD2.add(1825);
        else if (!listStr.isEmpty()) {
            String[] strings = listStr.split("_");
            for (String string : strings) TD2.add(Integer.parseInt(string));
        }

        // N1(B1)/N41
        listStr = PrefUtil.build().getValue("Auto_Arfcn_TD3", "-1").toString();
        if (listStr.equals("-1")) TD3.add(154810);
        else if (!listStr.isEmpty()) {
            String[] strings = listStr.split("_");
            for (String string : strings) TD3.add(Integer.parseInt(string));
        }

        // B34/B39/B40/B41
        listStr = PrefUtil.build().getValue("Auto_Arfcn_TD4", "-1").toString();
        if (listStr.equals("-1")) TD4.add(38950);
        else if (!listStr.isEmpty()) {
            String[] strings = listStr.split("_");
            for (String string : strings) TD4.add(Integer.parseInt(string));
        }

        mTraceCatchFragment.arfcnBeanHashMap.put("TD1", new ArfcnBean(TD1));
        mTraceCatchFragment.arfcnBeanHashMap.put("TD2", new ArfcnBean(TD2));
        mTraceCatchFragment.arfcnBeanHashMap.put("TD3", new ArfcnBean(TD3));
        mTraceCatchFragment.arfcnBeanHashMap.put("TD4", new ArfcnBean(TD4));
        mTraceCatchFragment.arfcnBeanHashMap.put("TD5", new ArfcnBean(TD5));
        mTraceCatchFragment.arfcnBeanHashMap.put("TD6", new ArfcnBean(TD6));
        mTraceCatchFragment.arfcnBeanHashMap.put("TD7", new ArfcnBean(TD7));
        mTraceCatchFragment.arfcnBeanHashMap.put("TD8", new ArfcnBean(TD8));

        listStr = PrefUtil.build().getValue("PCI_TD1", "1001").toString();
        ed_pci_1.setText(listStr);
        listStr = PrefUtil.build().getValue("PCI_TD2", "1002").toString();
        ed_pci_2.setText(listStr);
        listStr = PrefUtil.build().getValue("PCI_TD3", "1003").toString();
        ed_pci_3.setText(listStr);
        listStr = PrefUtil.build().getValue("PCI_TD4", "1004").toString();
        ed_pci_4.setText(listStr);
        listStr = PrefUtil.build().getValue("PCI_TD5", "501").toString();
        ed_pci_5.setText(listStr);
        listStr = PrefUtil.build().getValue("PCI_TD6", "502").toString();
        ed_pci_6.setText(listStr);
        listStr = PrefUtil.build().getValue("PCI_TD7", "503").toString();
        ed_pci_7.setText(listStr);
        listStr = PrefUtil.build().getValue("PCI_TD8", "504").toString();
        ed_pci_8.setText(listStr);
    }

    //1
    /*
     * 自动定位dialog
     *
     * */
    private void autoTraceDialog() {
        AppLog.D("SettingFragment getOpLogDialog()");
//        MainActivity.getInstance().createCustomDialog(false);
//
//        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_log, null);
//        final EditText device_imsi = view.findViewById(R.id.ed_file_name);
//        device_imsi.setInputType(InputType.TYPE_CLASS_NUMBER);  //数字
//        InputFilter[] filters = new InputFilter[1];     //限制输入15个
//        filters[0] = new InputFilter.LengthFilter(15);
//        device_imsi.setFilters(filters);
//
//        TextView tv_title = view.findViewById(R.id.tv_title);
//        tv_title.setText("开启自动定位");
//        TextView tv_prompt = view.findViewById(R.id.tv_prompt);
//        tv_prompt.setText("请输入定位设备imsi");
//        EditText ed_file_name = view.findViewById(R.id.ed_file_name);
//        ed_file_name.setText("");
//
//        TextView btn_ok = view.findViewById(R.id.btn_ok);
//        btn_ok.setOnClickListener(v -> {
        String autoTraceIMSI = ed_auto_imsi.getEditableText().toString();
        if (autoTraceIMSI.length() != GnbProtocol.MAX_IMSI_USE_LEN) {
            Util.showToast(getString(R.string.imsi_err_tip));
            return;
        }
        //开启定位
        isAutoMode = true;
        mTraceCatchFragment.doWork(autoTraceIMSI);
        MainActivity.getInstance().closeCustomDialog();
//        });
//        TextView btn_cancel = view.findViewById(R.id.btn_cancel);
//        btn_cancel.setOnClickListener(v -> MainActivity.getInstance().closeCustomDialog());
//        MainActivity.getInstance().showCustomDialog(view, false);
    }

    private void addData1(View v, MyRecyclerviewAdapter adapter, List<Integer> list, String key) {
        AppLog.D("CfgTraceChildFragment addData1");
        String text = adapter.getLastViewText();
        if (text == null) {
            adapter.addEditView();
            return;
        }
        if (text.isEmpty()) {
            showToast("不能添加空，请先填写!");
            return;
        }
        int band = 0;
        switch (key) {
            case "TD1":// N28/N78/N79
                band = NrBand.earfcn2band(Integer.parseInt(text));
                if (band != 28 && band != 78 && band != 79) {
                    showToast("该数据值不属于此通道，请修改!");
                    return;
                }
                break;
            case "TD2":// B34/B39/B40/B41
                band = LteBand.earfcn2band(Integer.parseInt(text));
                if (band != 34 && band != 39 && band != 40 && band != 41) {
                    showToast("该数据值不属于此通道，请修改!");
                    return;
                }
                break;
            case "TD3":// N1/N41
                band = NrBand.earfcn2band(Integer.parseInt(text));
                if (band != 1 && band != 41) {
                    showToast("该数据值不属于此通道，请修改!");
                    return;
                }
                break;
            case "TD4":// B1/B3/B5/B8
                band = LteBand.earfcn2band(Integer.parseInt(text));
                if (band != 1 && band != 3 && band != 5 && band != 8) {
                    showToast("该数据值不属于此通道，请修改!");
                    return;
                }
                break;
        }
//        if (isArfcnChange1 || isArfcnChange2 || isArfcnChange3 || isArfcnChange4
//                || isArfcnChange5 || isArfcnChange6 || isArfcnChange7 || isArfcnChange8) {    //有修改才判断是否添加有重复
        if (adapter.addData(text)) {
            adapter.addEditView();
            list.add(Integer.parseInt(text));
            addArfcnToCfgPref(adapter, Integer.parseInt(text), false);
        } else showToast("已存在相同数据值，请修改!");
//        } else {
//        adapter.addEditView();
//        list.add(Integer.parseInt(text));
//        addArfcnToCfgPref(adapter, Integer.parseInt(text), false);
//        }
    }

    //待修改：暂时不修改好像不用
    //当前使用,
    //判断arfcn是否正确并更新TD1
    private void checkArfcnAndUpdate(View v, MyRecyclerviewAdapter adapter, List<Integer> list, String key) {
        AppLog.D("CfgTraceChildFragment addData");
        String text = adapter.getLastViewText();
        if (text.isEmpty()) {
            showToast("不能添加空，请先填写!");
            return;
        }
        int band = 0;
        switch (key) {
            case "TD1":// N41/N78/N79
                band = NrBand.earfcn2band(Integer.parseInt(text));
                if (band != 41 && band != 78 && band != 79) {
                    showToast("该数据值不属于此通道，请修改!");
                    return;
                }
                break;
            case "TD2":// B3/B5/B8
                band = LteBand.earfcn2band(Integer.parseInt(text));
                if (band != 3 && band != 5 && band != 8) {
                    showToast("该数据值不属于此通道，请修改!");
                    return;
                }
                break;
            case "TD3":// N1(B1)/N41
                if (text.length() >= 6) {  //5g
                    band = NrBand.earfcn2band(Integer.parseInt(text));
                    if (band != 1 && band != 41) {
                        showToast("该数据值不属于此通道，请修改!");
                        return;
                    }
                } else {
                    band = LteBand.earfcn2band(Integer.parseInt(text));
                    if (band != 1) {
                        showToast("该数据值不属于此通道，请修改!");
                        return;
                    }
                }
                break;
            case "TD4":// B34/B39/B40/B41
                band = LteBand.earfcn2band(Integer.parseInt(text));
                if (band != 34 && band != 39 && band != 40 && band != 41) {
                    showToast("该数据值不属于此通道，请修改!");
                    return;
                }
                break;
        }

        list.clear();
        list.add(Integer.parseInt(text));
        addArfcnToCfgPref(adapter, Integer.parseInt(text), false);

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

//    public MyRecyclerviewAdapter getTD1_adapter() {
//        return TD1_adapter;
//    }
//
//    public void setTD1_adapter(MyRecyclerviewAdapter TD1_adapter) {
//        this.TD1_adapter = TD1_adapter;
//    }
//
//    public MyRecyclerviewAdapter getTD2_adapter() {
//        return TD2_adapter;
//    }
//
//    public void setTD2_adapter(MyRecyclerviewAdapter TD2_adapter) {
//        this.TD2_adapter = TD2_adapter;
//    }
//
//    public MyRecyclerviewAdapter getTD3_adapter() {
//        return TD3_adapter;
//    }
//
//    public void setTD3_adapter(MyRecyclerviewAdapter TD3_adapter) {
//        this.TD3_adapter = TD3_adapter;
//    }
//
//    public MyRecyclerviewAdapter getTD4_adapter() {
//        return TD4_adapter;
//    }
//
//    public void setTD4_adapter(MyRecyclerviewAdapter TD4_adapter) {
//        this.TD4_adapter = TD4_adapter;
//    }
//
//    public MyRecyclerviewAdapter getTD5_adapter() {
//        return TD5_adapter;
//    }
//
//    public void setTD5_adapter(MyRecyclerviewAdapter TD5_adapter) {
//        this.TD5_adapter = TD5_adapter;
//    }
//
//    public MyRecyclerviewAdapter getTD6_adapter() {
//        return TD6_adapter;
//    }
//
//    public void setTD6_adapter(MyRecyclerviewAdapter TD6_adapter) {
//        this.TD6_adapter = TD6_adapter;
//    }
//
//    public MyRecyclerviewAdapter getTD7_adapter() {
//        return TD7_adapter;
//    }
//
//    public void setTD7_adapter(MyRecyclerviewAdapter TD7_adapter) {
//        this.TD7_adapter = TD7_adapter;
//    }
//
//    public MyRecyclerviewAdapter getTD8_adapter() {
//        return TD8_adapter;
//    }
//
//    public void setTD8_adapter(MyRecyclerviewAdapter TD8_adapter) {
//        this.TD8_adapter = TD8_adapter;
//    }

    public LinkedList<Integer> getTD1() {
        return TD1;
    }

    public void setTD1(LinkedList<Integer> TD1) {
        this.TD1 = TD1;
    }

    public LinkedList<Integer> getTD2() {
        return TD2;
    }

    public void setTD2(LinkedList<Integer> TD2) {
        this.TD2 = TD2;
    }

    public LinkedList<Integer> getTD3() {
        return TD3;
    }

    public void setTD3(LinkedList<Integer> TD3) {
        this.TD3 = TD3;
    }

    public LinkedList<Integer> getTD4() {
        return TD4;
    }

    public void setTD4(LinkedList<Integer> TD4) {
        this.TD4 = TD4;
    }

    public LinkedList<Integer> getTD5() {
        return TD5;
    }

    public void setTD5(LinkedList<Integer> TD5) {
        this.TD5 = TD5;
    }

    public LinkedList<Integer> getTD6() {
        return TD6;
    }

    public void setTD6(LinkedList<Integer> TD6) {
        this.TD6 = TD6;
    }

    public LinkedList<Integer> getTD7() {
        return TD7;
    }

    public void setTD7(LinkedList<Integer> TD7) {
        this.TD7 = TD7;
    }

    public LinkedList<Integer> getTD8() {
        return TD8;
    }

    public void setTD8(LinkedList<Integer> TD8) {
        this.TD8 = TD8;
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
    public void OnDelArfcn(String arfcn, String band) {
        if (arfcn.isEmpty()) return;
        Integer intArfcn = Integer.valueOf(arfcn);
        switch (band) {
            case "TD1":// N28/N78/N79
                TD1.remove(intArfcn);
                break;
            case "TD2":// B34/B39/B40/B41
                TD2.remove(intArfcn);
                break;
            case "TD3":// N1/N41
                TD3.remove(intArfcn);
                break;
            case "TD4":// B1/B3/B5/B8
                TD4.remove(intArfcn);
                break;
            case "TD5":
                TD5.remove(intArfcn);
                break;
            case "TD6":
                TD6.remove(intArfcn);
                break;
            case "TD7":
                TD7.remove(intArfcn);
                break;
            case "TD8":
                TD8.remove(intArfcn);
                break;
        }
    }


    @Override
    public void onItemClickListener(int position) {

    }

    @Override
    public void onHistoryItemClickListener(JSONArray jsonArray) {
        try {
            //数据导入定位页
            JSONObject jsonObject1 = jsonArray.getJSONObject(0);
            JSONObject jsonObject2 = jsonArray.getJSONObject(1);
            JSONObject jsonObject3 = jsonArray.getJSONObject(2);
            JSONObject jsonObject4 = jsonArray.getJSONObject(3);
            JSONObject jsonObject5 = jsonArray.getJSONObject(4);
            JSONObject jsonObject6 = jsonArray.getJSONObject(5);
            JSONObject jsonObject7 = jsonArray.getJSONObject(6);
            JSONObject jsonObject8 = jsonArray.getJSONObject(7);

            String pci1 = jsonObject1.getString("pci");
            String imsi1 = jsonObject1.getString("imsi");
            ArrayList<String> arfcnList1 = new ArrayList<>();
            String pci2 = jsonObject2.getString("pci");
            String imsi2 = jsonObject2.getString("imsi");
            ArrayList<String> arfcnList2 = new ArrayList<>();
            String pci3 = jsonObject3.getString("pci");
            String imsi3 = jsonObject3.getString("imsi");
            ArrayList<String> arfcnList3 = new ArrayList<>();
            String pci4 = jsonObject4.getString("pci");
            String imsi4 = jsonObject4.getString("imsi");
            ArrayList<String> arfcnList4 = new ArrayList<>();
            String pci5 = jsonObject5.getString("pci");
            String imsi5 = jsonObject5.getString("imsi");
            ArrayList<String> arfcnList5 = new ArrayList<>();
            String pci6 = jsonObject6.getString("pci");
            String imsi6 = jsonObject6.getString("imsi");
            ArrayList<String> arfcnList6 = new ArrayList<>();
            String pci7 = jsonObject7.getString("pci");
            String imsi7 = jsonObject7.getString("imsi");
            ArrayList<String> arfcnList7 = new ArrayList<>();
            String pci8 = jsonObject8.getString("pci");
            String imsi8 = jsonObject8.getString("imsi");
            ArrayList<String> arfcnList8 = new ArrayList<>();

            String arfcnJsonArray1 = jsonObject1.getString("arfcnJsonArray");
            String arfcnJsonArray2 = jsonObject2.getString("arfcnJsonArray");
            String arfcnJsonArray3 = jsonObject3.getString("arfcnJsonArray");
            String arfcnJsonArray4 = jsonObject4.getString("arfcnJsonArray");
            String arfcnJsonArray5 = jsonObject5.getString("arfcnJsonArray");
            String arfcnJsonArray6 = jsonObject6.getString("arfcnJsonArray");
            String arfcnJsonArray7 = jsonObject7.getString("arfcnJsonArray");
            String arfcnJsonArray8 = jsonObject8.getString("arfcnJsonArray");

            JSONArray arfcnArray1 = new JSONArray(arfcnJsonArray1);
            for (int i = 0; i < arfcnArray1.length(); i++) {
                arfcnList1.add(arfcnArray1.getString(i));
            }
            JSONArray arfcnArray2 = new JSONArray(arfcnJsonArray2);
            for (int i = 0; i < arfcnArray2.length(); i++) {
                arfcnList2.add(arfcnArray2.getString(i));
            }
            JSONArray arfcnArray3 = new JSONArray(arfcnJsonArray3);
            for (int i = 0; i < arfcnArray3.length(); i++) {
                arfcnList3.add(arfcnArray3.getString(i));
            }
            JSONArray arfcnArray4 = new JSONArray(arfcnJsonArray4);
            for (int i = 0; i < arfcnArray4.length(); i++) {
                arfcnList4.add(arfcnArray4.getString(i));
            }
            JSONArray arfcnArray5 = new JSONArray(arfcnJsonArray5);
            for (int i = 0; i < arfcnArray5.length(); i++) {
                arfcnList5.add(arfcnArray5.getString(i));
            }
            JSONArray arfcnArray6 = new JSONArray(arfcnJsonArray6);
            for (int i = 0; i < arfcnArray6.length(); i++) {
                arfcnList6.add(arfcnArray6.getString(i));
            }
            JSONArray arfcnArray7 = new JSONArray(arfcnJsonArray7);
            for (int i = 0; i < arfcnArray7.length(); i++) {
                arfcnList7.add(arfcnArray7.getString(i));
            }
            JSONArray arfcnArray8 = new JSONArray(arfcnJsonArray8);
            for (int i = 0; i < arfcnArray8.length(); i++) {
                arfcnList8.add(arfcnArray8.getString(i));
            }

            ed_pci_1.setText(pci1);
            ed_pci_2.setText(pci2);
            ed_pci_3.setText(pci3);
            ed_pci_4.setText(pci4);
            ed_pci_5.setText(pci5);
            ed_pci_6.setText(pci6);
            ed_pci_7.setText(pci7);
            ed_pci_8.setText(pci8);
            actv_imsi.setText(imsi1);
            actv_imsi_2.setText(imsi2);
            actv_imsi_3.setText(imsi3);
            actv_imsi_4.setText(imsi4);
            actv_imsi_5.setText(imsi5);
            actv_imsi_6.setText(imsi6);
            actv_imsi_7.setText(imsi7);
            actv_imsi_8.setText(imsi8);
//            TD1_adapter.setData(arfcnList1);
//            TD2_adapter.setData(arfcnList2);
//            TD3_adapter.setData(arfcnList3);
//            TD4_adapter.setData(arfcnList4);
//            TD5_adapter.setData(arfcnList5);
//            TD6_adapter.setData(arfcnList6);
//            TD7_adapter.setData(arfcnList7);
//            TD8_adapter.setData(arfcnList8);
            et_arfcn_1.setText(arfcnList1.size() > 0 ? arfcnList1.get(0) : "");
            et_arfcn_2.setText(arfcnList2.size() > 0 ? arfcnList2.get(0) : "");
            et_arfcn_3.setText(arfcnList3.size() > 0 ? arfcnList3.get(0) : "");
            et_arfcn_4.setText(arfcnList4.size() > 0 ? arfcnList4.get(0) : "");
            et_arfcn_5.setText(arfcnList5.size() > 0 ? arfcnList5.get(0) : "");
            et_arfcn_6.setText(arfcnList6.size() > 0 ? arfcnList6.get(0) : "");
            et_arfcn_7.setText(arfcnList7.size() > 0 ? arfcnList7.get(0) : "");
            et_arfcn_8.setText(arfcnList8.size() > 0 ? arfcnList8.get(0) : "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setbtn_testVisibility(boolean isVisible) {
        if (btn_test == null) return;
        btn_test.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void setToastTextEmpty() {
        toastText = "";
    }
}