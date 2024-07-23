package com.simdo.g73cs.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dwdbsdk.Bean.DW.DWProtocol;
import com.dwdbsdk.Bean.LteBand;
import com.dwdbsdk.Bean.NrBand;
import com.dwdbsdk.MessageControl.MessageController;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.simdo.g73cs.Adapter.AutoSearchAdapter;
import com.simdo.g73cs.Adapter.BlackListAdapter;
import com.simdo.g73cs.Adapter.DualRecyclerviewAdapter;
import com.simdo.g73cs.Adapter.FreqResultListAdapter;
import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.HistoryBean_3g758cx;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.DBViewModel;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DataBaseUtil_3g758cx;
import com.simdo.g73cs.Util.DeviceUtil;
import com.simdo.g73cs.Util.DividerItemDecoration;
import com.simdo.g73cs.Util.LteBand2;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.ZApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CfgTraceChildFragment extends Fragment implements ListItemListener, DualRecyclerviewAdapter.OnDelArfcnListListener {
    Context mContext;
    TraceCatchFragment mTraceCatchFragment;

    private DWSettingFragment DWSettingFragment;
    private FreqFragment freqFragment;
    //是否频点轮询
    private boolean isCell1SetArfcnChange, isCell2SetArfcnChange, isCell3SetArfcnChange, isCell4SetArfcnChange;
    ImageView iv_instance, iv_operator_liandian, iv_operator_yiguang;

    TextView tv_do_btn;
    RecyclerView rv_arfcn_yiguang, rv_arfcn_liandian;
    public boolean isAutoMode = false;   //是否自动模式


    public boolean isCloseDistance = false;   //是否近距
    public boolean isAutoModeFreqRunning = false;   //自动模式是否在运行

    private List<ScanArfcnBean> cfgTraceChildFreqList = new ArrayList<>();
    HistoryBean_3g758cx historyBean;
    DBViewModel DBViewModel;

    public CfgTraceChildFragment() {
        mContext = ZApplication.getInstance().getContext();
    }

    public CfgTraceChildFragment(TraceCatchFragment mTraceCatchFragment) {
        mContext = ZApplication.getInstance().getContext();
        this.mTraceCatchFragment = mTraceCatchFragment;
    }

    public CfgTraceChildFragment(TraceCatchFragment traceCatchFragment, FreqFragment freqFragment, SettingFragment settingFragment) {
        mContext = ZApplication.getInstance().getContext();
        this.mTraceCatchFragment = traceCatchFragment;
        this.freqFragment = freqFragment;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        AppLog.D("CfgTraceChildFragment onSaveInstanceState");
        // 使用 FragmentState 将 Fragment 的状态保存到 Bundle 中
        if (!requireActivity().getSupportFragmentManager().isStateSaved()) {
            saveFragmentState(freqFragment, "freqFragment", outState);
            saveFragmentState(mTraceCatchFragment, "mTraceCatchFragment", outState);
            // 保存其他 Fragment 的状态
        }
    }

    private void saveFragmentState(Fragment fragment, String tag, Bundle outState) {
        if (fragment != null && fragment.isAdded()) {
            SavedState fragmentState = requireActivity().getSupportFragmentManager()
                    .saveFragmentInstanceState(fragment);
            outState.putParcelable(tag, fragmentState);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.I("CfgTraceChildFragment onCreate");
        if (savedInstanceState != null) {
            FragmentManager mainManager = requireActivity().getSupportFragmentManager();
            freqFragment = (FreqFragment) mainManager.getFragment(savedInstanceState, "freqFragment");
            mTraceCatchFragment = (TraceCatchFragment) mainManager.getFragment(savedInstanceState, "mTraceCatchFragment");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppLog.I("CfgTraceChildFragment onViewCreated");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AppLog.I("CfgTraceChildFragment onDestroyView");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.I("CfgTraceChildFragment onCreateView");

        View root = inflater.inflate(R.layout.child_pager_cfg, container, false);
        DBViewModel = new ViewModelProvider(requireActivity()).get(DBViewModel.class);

        initView(root);
        return root;
    }

    AutoCompleteTextView actv_imsi_yiguang;
    AutoCompleteTextView actv_imsi_liandian;
    LinearLayout ll_test_11imsi;


    AutoCompleteTextView actv_imsi2;
    AutoCompleteTextView actv_imsi3;
    AutoCompleteTextView actv_imsi4;
    AutoCompleteTextView actv_imsi5;
    AutoCompleteTextView actv_imsi6;
    AutoCompleteTextView actv_imsi7;
    AutoCompleteTextView actv_imsi8;
    AutoCompleteTextView actv_imsi9;
    AutoCompleteTextView actv_imsi10;
    AutoCompleteTextView actv_imsi11;
    AutoCompleteTextView actv_imsi12;
    Button btn_set_all_imsi;

    LinearLayout cl_yiguang, cl_liandian;
    NestedScrollView nsv_test;
    //    EditText ed_arfcn, ed_arfcn_2, ed_arfcn_3, ed_arfcn_4;
    private List<String> dropImsiList = new ArrayList<>();  //imsi历史列表

    public AutoSearchAdapter getDropImsiAdapter() {
        return dropImsiAdapter;
    }

    private AutoSearchAdapter dropImsiAdapter;
    ImageView btn_add_1, btn_add_2;
    TextView tv_test_start_tint;
    boolean isChange1 = false, isChange2 = false, isChange3 = false, isChange4 = false;


    DualRecyclerviewAdapter yiguang_adapter, liandian_adapter;   //

    Switch sw_auto_trace;
    AutoCompleteTextView ed_auto_imsi;
    TextView tv_switch_text_cfg, tv_switch_text_auto;
    LinearLayout ll_auto_trace;
    LinearLayoutCompat cl_cfg_trace;
    private final int minDelta = 200;           // threshold in ms
    private long focusTime = 0;                 // time of last touch
    private View focusTarget = null;

    //是否是在修改最新的值，为false说明此值已经添加过
    //只有在点击添加按钮成功添加进去时的状态为false
    //为true时在检查相同值的时候需要先删除最后一个，因为最后一个就是它本身，不删除会重复判断
    //为false在检查相同值的时候不需要删除最后一个，为了避免连续添加不检查
    private boolean isChanging = true;

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
        btn_add_1 = root.findViewById(R.id.btn_add_yiguang);
        btn_add_2 = root.findViewById(R.id.btn_add_liandian);
        tv_test_start_tint = root.findViewById(R.id.tv_test_start_tint);
        dropImsiList.clear();
        dropImsiList = PrefUtil.build().getDropImsiList();

        //自动定位按钮
        ed_auto_imsi = root.findViewById(R.id.ed_auto_imsi);
        sw_auto_trace = root.findViewById(R.id.sw_auto_trace);
        tv_switch_text_cfg = root.findViewById(R.id.tv_switch_text_cfg);
        tv_switch_text_auto = root.findViewById(R.id.tv_switch_text_auto);
        ll_auto_trace = root.findViewById(R.id.ll_auto_trace);
        cl_cfg_trace = root.findViewById(R.id.cl_cfg_trace);
        sw_auto_trace.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            //车载自动定位暂时封杀
//            Util.showToast("暂不支持自动定位模式");
//            sw_auto_trace.setChecked(!sw_auto_trace.isChecked());
//            return;
            if (tv_do_btn.getText().toString().equals("结束")) {
                sw_auto_trace.setChecked(!sw_auto_trace.isChecked());
                Util.showToast("请先结束定位");
                return;
            }
            if (sw_auto_trace.isChecked()) {
                tv_switch_text_cfg.setVisibility(View.GONE);
                tv_switch_text_auto.setVisibility(View.VISIBLE);
                ll_auto_trace.setVisibility(View.VISIBLE);
                cl_cfg_trace.setVisibility(View.GONE);
            } else {
                tv_switch_text_cfg.setVisibility(View.VISIBLE);
                tv_switch_text_auto.setVisibility(View.GONE);
                ll_auto_trace.setVisibility(View.GONE);
                cl_cfg_trace.setVisibility(View.VISIBLE);
            }
        }));

        actv_imsi_yiguang = root.findViewById(R.id.actv_imsi);
        actv_imsi_liandian = root.findViewById(R.id.actv_imsi_liandian);

        //测试用
        ll_test_11imsi = root.findViewById(R.id.ll_test_11imsi);
        ll_test_11imsi.setVisibility(MainActivity.ifDebug ? View.VISIBLE : View.GONE);
        actv_imsi2 = root.findViewById(R.id.actv_imsi2);
        actv_imsi3 = root.findViewById(R.id.actv_imsi3);
        actv_imsi4 = root.findViewById(R.id.actv_imsi4);
        actv_imsi5 = root.findViewById(R.id.actv_imsi5);
        actv_imsi6 = root.findViewById(R.id.actv_imsi6);
        actv_imsi7 = root.findViewById(R.id.actv_imsi7);
        actv_imsi8 = root.findViewById(R.id.actv_imsi8);
        actv_imsi9 = root.findViewById(R.id.actv_imsi9);
        actv_imsi10 = root.findViewById(R.id.actv_imsi10);
        actv_imsi11 = root.findViewById(R.id.actv_imsi11);
        actv_imsi12 = root.findViewById(R.id.actv_imsi12);
        btn_set_all_imsi = root.findViewById(R.id.btn_set_all_imsi);
        btn_set_all_imsi.setOnClickListener(view -> {
            actv_imsi2.setText(actv_imsi_yiguang.getText());
            actv_imsi3.setText(actv_imsi_yiguang.getText());
            actv_imsi4.setText(actv_imsi_yiguang.getText());
            actv_imsi5.setText(actv_imsi_yiguang.getText());
            actv_imsi6.setText(actv_imsi_yiguang.getText());
            actv_imsi7.setText(actv_imsi_yiguang.getText());
            actv_imsi8.setText(actv_imsi_yiguang.getText());
            actv_imsi9.setText(actv_imsi_yiguang.getText());
            actv_imsi10.setText(actv_imsi_yiguang.getText());
            actv_imsi11.setText(actv_imsi_yiguang.getText());
            actv_imsi12.setText(actv_imsi_yiguang.getText());
        });

        cl_yiguang = root.findViewById(R.id.cl_yiguang);
        nsv_test = root.findViewById(R.id.nsv_test);
        cl_liandian = root.findViewById(R.id.cl_liandian);

        dropImsiAdapter = new AutoSearchAdapter(mContext, dropImsiList);
        actv_imsi_yiguang.setAdapter(dropImsiAdapter);
        actv_imsi_liandian.setAdapter(dropImsiAdapter);
        actv_imsi2.setAdapter(dropImsiAdapter);
        actv_imsi3.setAdapter(dropImsiAdapter);
        actv_imsi4.setAdapter(dropImsiAdapter);
        actv_imsi5.setAdapter(dropImsiAdapter);
        actv_imsi6.setAdapter(dropImsiAdapter);
        actv_imsi7.setAdapter(dropImsiAdapter);
        actv_imsi8.setAdapter(dropImsiAdapter);
        actv_imsi9.setAdapter(dropImsiAdapter);
        actv_imsi10.setAdapter(dropImsiAdapter);
        actv_imsi11.setAdapter(dropImsiAdapter);
        actv_imsi12.setAdapter(dropImsiAdapter);

        ed_auto_imsi.setAdapter(dropImsiAdapter);

        historyBean = new HistoryBean_3g758cx(2, "");
        onHistoryItemClickListener(DataBaseUtil_3g758cx.getLastTraceCfgToDB());

        yiguang_adapter = new DualRecyclerviewAdapter(mContext, historyBean.getYgList(), "TD1", false);
        liandian_adapter = new DualRecyclerviewAdapter(mContext, historyBean.getLdList(), "TD2", false);

        yiguang_adapter.setOnDelArfcnListListener(this);
        liandian_adapter.setOnDelArfcnListListener(this);

        rv_arfcn_yiguang = root.findViewById(R.id.rv_arfcn_1);
        rv_arfcn_yiguang.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        // 根据屏幕密度计算RecyclerView的高度，以确保显示的项数不会超过指定的数量
//        setRVHeight(rv_arfcn_yiguang, yiguang_adapter);
        rv_arfcn_yiguang.setItemViewCacheSize(20);
        rv_arfcn_yiguang.setAdapter(yiguang_adapter);
        rv_arfcn_yiguang.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        root.findViewById(R.id.btn_add_yiguang).setOnClickListener(v -> {
            btnAddData(v, yiguang_adapter, historyBean.getYgList(), "yiguang");
            yiguang_adapter.showSoftInput(true);    //先设置后面再刷新
//            setRVHeight(rv_arfcn_yiguang, yiguang_adapter);
        });

        //导入
        root.findViewById(R.id.tv_import_1).setOnClickListener(view -> showFreqListDialog(0));

        rv_arfcn_liandian = root.findViewById(R.id.rv_arfcn_1_liandian);
        rv_arfcn_liandian.setItemViewCacheSize(20);
        rv_arfcn_liandian.setAdapter(liandian_adapter);
        // 根据屏幕密度计算RecyclerView的高度，以确保显示的项数不会超过指定的数量
//        setRVHeight(rv_arfcn_liandian, liandian_adapter);
        rv_arfcn_liandian.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        rv_arfcn_liandian.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        root.findViewById(R.id.btn_add_liandian).setOnClickListener(v -> {
            btnAddData(v, liandian_adapter, historyBean.getLdList(), "liandian");
            liandian_adapter.showSoftInput(true);   //先设置后面再刷新
//            setRVHeight(rv_arfcn_liandian, liandian_adapter);
        });

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
                mTraceCatchFragment.dosetDWTxPwrOffset(0);
                isCloseDistance = false;
                DBViewModel.setRxGain(0);
                MessageController.build().setDBRxGain(DBViewModel.getDeviceId(), 0);
            } else {
                iv_instance.setBackgroundResource(R.mipmap.icon_distance2_h);
                mTraceCatchFragment.dosetDWTxPwrOffset(-3);
                isCloseDistance = true;
                DBViewModel.setRxGain(2);
                MessageController.build().setDBRxGain(DBViewModel.getDeviceId(), 2);
            }
        });

        //运营商改变
        TextView tv_tint = root.findViewById(R.id.tv_tint);
        TextView tv_tint_liandian = root.findViewById(R.id.tv_tint_liandian);
        iv_operator_liandian = root.findViewById(R.id.iv_operator_liandian);
        iv_operator_yiguang = root.findViewById(R.id.iv_operator_yiguang);

        iv_operator_yiguang.setOnClickListener(v -> {
            if (!tv_do_btn.getText().toString().equals("开启")) {
//                AppLog.D("click iv_operator");
//                sw_auto_trace.setChecked(!sw_auto_trace.isChecked());
                Util.showToast("请先结束定位");
                return;
            }
            tv_tint_liandian.setVisibility(View.GONE);
            tv_tint.setVisibility(View.VISIBLE);
            iv_operator_liandian.setBackgroundResource(R.mipmap.icon_liandian_notcheck);
            iv_operator_yiguang.setBackgroundResource(R.mipmap.icon_yiguang);
            cl_yiguang.setVisibility(View.VISIBLE);
            nsv_test.setVisibility(View.VISIBLE);
            cl_liandian.setVisibility(View.GONE);

        });
        iv_operator_liandian.setOnClickListener(v -> {
            if (!tv_do_btn.getText().toString().equals("开启")) {
//                AppLog.D("click iv_operator");
//                sw_auto_trace.setChecked(!sw_auto_trace.isChecked());
                Util.showToast("请先结束定位");
                return;
            }
            tv_tint_liandian.setVisibility(View.VISIBLE);
            tv_tint.setVisibility(View.GONE);
            iv_operator_liandian.setBackgroundResource(R.mipmap.icon_liandian);
            iv_operator_yiguang.setBackgroundResource(R.mipmap.icon_yiguang_notcheck);
            cl_yiguang.setVisibility(View.GONE);
            nsv_test.setVisibility(View.GONE);
            cl_liandian.setVisibility(View.VISIBLE);

        });

        //黑名单
        ImageView iv_black_list = root.findViewById(R.id.iv_black_list);
        iv_black_list.setOnClickListener(view -> mTraceCatchFragment.showBlackListDialog());
        root.findViewById(R.id.iv_black_list_auto).setOnClickListener(view -> mTraceCatchFragment.showBlackListDialog());

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch switch_force_catch = root.findViewById(R.id.switch_force_catch);
        switch_force_catch.setOnClickListener(view -> {
            if (MainActivity.getInstance().getDeviceList().size() == 0) {
                Util.showToast(mContext, "设备未连接");
                switch_force_catch.setChecked(false);
                return;
            }
            //未开业务的时候才能开启
            boolean ifIsIdle = true;
            for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                if (bean.getWorkState() != GnbBean.DW_State.IDLE) {
                    ifIsIdle = false;
                    break;
                }
            }
            if (!ifIsIdle) {
                Util.showToast(mContext, Util.getString(R.string.state_predator));
                switch_force_catch.setChecked(!switch_force_catch.isChecked());
                return;
            }
            for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                int logicIndex = DeviceUtil.getLogicIndexById(bean.getRsp().getDeviceId());
                for (int i = 0; i < 4; i++) {
                    int channelNum = DeviceUtil.getChannelNum(logicIndex, i);
                    if (channelNum == 1 || channelNum == 3 || channelNum == 5 || channelNum == 7 || channelNum == 9)    //只有5g的才改
                        bean.getTraceUtil().setMobRejectCode(i, switch_force_catch.isChecked() ? 9 : 0);
                }
            }

        });

        // 开始、结束工作按钮
        tv_do_btn = root.findViewById(R.id.tv_do_btn);
        tv_do_btn.setOnClickListener(view -> {
            if (sw_auto_trace.isChecked()) clickAutoTrace();
            else mTraceCatchFragment.doWork("");
        });

        //初始化按钮
        tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);

        resetArfcnList();
    }

    public void showBlackDialog(boolean isAutoMode) {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_blacklist_silde, null);

        RecyclerView black_list = view.findViewById(R.id.black_list);
        black_list.setLayoutManager(new LinearLayoutManager(mContext));
        List<MyUeidBean> blackList = MainActivity.getInstance().getBlackList();
        BlackListAdapter blackListAdapter = new BlackListAdapter(mContext, blackList, new ListItemListener() {
            @Override
            public void onItemClickListener(int position) {
                if (isAutoMode) {
                    if (ed_auto_imsi != null)
                        ed_auto_imsi.setText(blackList.get(position).getUeidBean().getImsi());
                } else {
                    if (isYiguangChecked()){
                        if (actv_imsi_yiguang != null)
                            actv_imsi_yiguang.setText(blackList.get(position).getUeidBean().getImsi());
                    } else {
                        if (actv_imsi_liandian != null)
                            actv_imsi_liandian.setText(blackList.get(position).getUeidBean().getImsi());
                    }
                }
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        black_list.setAdapter(blackListAdapter);
        view.findViewById(R.id.tv_add).setVisibility(View.GONE);
        view.findViewById(R.id.back).setOnClickListener(view1 -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, false, true);
    }
    private void showFreqListDialog(int cell) {
        if (getFreqList() == null) {
            Util.showToast(mContext.getString(R.string.no_freq_data_tip));
            return;
        }
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_freqlist_silde, null);

        view.findViewById(R.id.back).setOnClickListener(view1 -> MainActivity.getInstance().closeCustomDialog());

        RecyclerView freq_list = view.findViewById(R.id.freq_list);
        freq_list.setLayoutManager(new LinearLayoutManager(mContext));

        FreqResultListAdapter adapter = new FreqResultListAdapter(mContext, getFreqList(), new ListItemListener() {
            @Override
            public void onItemClickListener(int position) {
                if (importArfcn(getFreqList().get(position), cell, 0))
                    MainActivity.getInstance().closeCustomDialog();
                else Util.showToast(mContext.getString(R.string.cell_not_support_tip));
            }
        });
        freq_list.setLayoutManager(new LinearLayoutManager(mContext));
        freq_list.setAdapter(adapter);
        MainActivity.getInstance().showCustomDialog(view, false, true);
    }

    private LinkedList<ScanArfcnBean> getFreqList() {
        return MainActivity.getInstance().getFreqList();
    }

    private void setRVHeight(RecyclerView rv_arfcn, DualRecyclerviewAdapter adapter) {
        int visibleItemCount = adapter.getItemCount() > 6 ? 6 : adapter.getItemCount(); // 设置每个画面显示的项数

        // 根据屏幕密度计算RecyclerView的高度，以确保显示的项数不会超过指定的数量
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int itemHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, adapter.getItemCount() > 6 ? 39 : 37, displayMetrics);
        int recyclerViewHeight = itemHeight * visibleItemCount;

        // 设置RecyclerView的高度
        ViewGroup.LayoutParams layoutParams = rv_arfcn.getLayoutParams();
        layoutParams.height = recyclerViewHeight;
        rv_arfcn.setLayoutParams(layoutParams);
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
            StringBuilder sb = new StringBuilder();
            for (DeviceInfoBean bean : deviceList) {
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
            AppLog.I("clickAutoTrace() 开启定位 sb = " + sb);
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
                autoTraceDialog(); // 多设备在空闲状态下，启动定位
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

    private void hideSoftKeyBoard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public TextView getTv_do_btn() {
        return tv_do_btn;
    }

    /**
     * @param operatorNum -1自动定位+后两个 0移动 1联通
     * @param enable
     */
    //设置定位配置页能否更改
    public void setcfgEnable(int operatorNum, boolean enable) {
        AppLog.D("setcfgEnable operatorNum = " + operatorNum + ", enable = " + enable);
        if (rv_arfcn_yiguang == null) return;
        if (operatorNum == -1) {
            setcfgEnable(10, enable);
            //自动定位输入框关闭
            ed_auto_imsi.setClickable(enable);
            ed_auto_imsi.setFocusable(enable);
            ed_auto_imsi.setFocusableInTouchMode(enable);

//            trace_group_all.setVisibility(enable ? View.VISIBLE : View.GONE);
            return;
        }
        if (operatorNum == 0 || operatorNum == 10) {
            yiguang_adapter.setFocusable(enable);

            iv_operator_liandian.setClickable(enable);
            iv_operator_liandian.setClickable(enable);
            iv_operator_liandian.setClickable(enable);

            actv_imsi_yiguang.setClickable(enable);
            actv_imsi_yiguang.setFocusable(enable);
            actv_imsi_yiguang.setFocusableInTouchMode(enable);

            btn_add_1.setBackgroundResource(enable ? R.mipmap.icon_increase_n : android.R.color.transparent);
            btn_add_1.setClickable(enable);

            if (operatorNum == 10) setcfgEnable(11, enable);
        } else if (operatorNum == 1 || operatorNum == 11) {
            liandian_adapter.setFocusable(enable);

            iv_operator_liandian.setClickable(enable);
            iv_operator_liandian.setClickable(enable);
            iv_operator_liandian.setClickable(enable);

            actv_imsi_liandian.setClickable(enable);
            actv_imsi_liandian.setFocusable(enable);
            actv_imsi_liandian.setFocusableInTouchMode(enable);

            btn_add_2.setBackgroundResource(enable ? R.mipmap.icon_increase_n : android.R.color.transparent);
            btn_add_2.setClickable(enable);
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

            if (PaCtl.build().is3G758) {
                switch (band) {
                    case 1:
                    case 5:
                        if (cell == -1 || cell == 1) {
                            liandian_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                            isOk = true;
                        }
                        break;
                    case 3:
                    case 8:
                        if (cell == -1 || cell == 1) {
                            int freq = LteBand2.earfcn2freq(arfcn);
                            if ((freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949))
                                yiguang_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                            else
                                liandian_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                            isOk = true;
                        }
                        break;
                    case 34:
                    case 39:
                    case 40:
                    case 41:
                        if (cell == -1 || cell == 3) {
                            yiguang_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
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
//                            TD4_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                            isOk = true;
                        }
                        break;
                    case 34:
                    case 39:
                    case 40:
                    case 41:
                        if (cell == -1 || cell == 1) {
                            liandian_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
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
                case 28:
                case 41:
                case 79:
                    if (PaCtl.build().is3G758) {
                        if (cell == -1 || cell == 0) {
                            yiguang_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                            isOk = true;
                        }
                    } else {
                        if (cell == -1 || cell == 2) {
//                            TD3_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                            isOk = true;
                        }
                    }
                    break;
                case 78:
                case 1:
                case 3:
                    if (cell == -1 || cell == 0) {
                        liandian_adapter.addImportDataToStart(String.valueOf(arfcn), String.valueOf(pci));
                        isOk = true;
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
        if (autoTraceIMSI.length() != DWProtocol.MAX_IMSI_USE_LEN) {
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
            Util.showToast(mContext.getString(R.string.arfcn_empty_err));
            return;
        }

        String lastPci = lastArfcnPciBean.getPci();
        if (lastPci.isEmpty()) {
            Util.showToast(mContext.getString(R.string.pci_empty_err));
            return;
        }
        int i_pci = Integer.parseInt(lastPci);

        if (!checkParam(lastArfcn, i_pci, key)) return; //检查频点本身是否正确

        //检查频点和其他频点是否不冲突，最后一个值可修改的话不比较最后一个值，不可修改的话比较最后一个值
        //最后再添加到list中
        List<ArfcnPciBean> listDelete = new ArrayList<>(list);

//        if (isChanging()) {
        //删除最后一个再比较
        listDelete.remove(listDelete.size() - 1);
        if (!listDelete.isEmpty())
            AppLog.D("listDelete last = " + listDelete.get(listDelete.size() - 1).toString());
//        }

        //相同值检查
        for (ArfcnPciBean arfcnPciBean : listDelete) {
//            if (arfcnPciBean.getArfcn().equals(lastArfcn) && arfcnPciBean.getPci().equals(lastPci)) {
            if (arfcnPciBean.getArfcn().equals(lastArfcn)) {
                Util.showToast(lastArfcn + "已存在，请修改配置");
                return;
            }
        }

        //通过相同判断
        list.clear();
        list.addAll(listDelete);
//        if (!isChanging()) {
//            list.remove(list.size() - 1);
//        }

//        adapter.addData(lastArfcnPciBean);

        AppLog.D("lastArfcnPciBean = " + lastArfcnPciBean.toString());
        list.add(lastArfcnPciBean);

        adapter.addEditView();
        if (key.equals("yiguang")) {
            rv_arfcn_yiguang.scrollToPosition(list.size() - 1);
        } else {
            rv_arfcn_liandian.scrollToPosition(list.size() - 1);
        }
        adapter.setClickAddBtn(true);   //点击添加按钮后点击删除按钮需要把最后一个空的删了
//        adapter.setLastArfcnPciBeanNull();
        adapter.notifyDataSetChanged();
        setIsChanging(false);
    }


    public boolean checkLastParam() {
        AppLog.I("checkLastParam()");
        ArfcnPciBean lastBean1;
        String imsi;
        String key;
        LinkedList<ArfcnPciBean> list;
        DualRecyclerviewAdapter adapter;
        if (isYiguangChecked()) {
            imsi = actv_imsi_yiguang.getText().toString();
            adapter = yiguang_adapter;
            lastBean1 = yiguang_adapter.getLastViewText();
            key = "yiguang";
            list = historyBean.getYgList();
        } else {
            imsi = actv_imsi_liandian.getText().toString();
            adapter = liandian_adapter;
            lastBean1 = liandian_adapter.getLastViewText();
            key = "liandian";
            list = historyBean.getLdList();
        }
        if (imsi.isEmpty()) {
            Util.showToast("IMSI不可为空！");
            return false;
        }
        if (!imsi.isEmpty() && imsi.length() != 15) {
            Util.showToast(mContext.getString(R.string.cell_0_imsi_err));
            return false;
        }

        //imsi自动填充列表
        if (!Util.existSameData(dropImsiList, imsi)) {
            if (dropImsiList.size() > 20) {
                dropImsiList.remove(0);
            }
            dropImsiList.add(imsi);
            PrefUtil.build().setDropImsiList(dropImsiList);
            dropImsiAdapter.setList(dropImsiList);
        }
        if (MainActivity.ifDebug) storeTestImsi();

        //设置imsi
        if (isYiguangChecked()) historyBean.setImsi_yg(imsi);
        else historyBean.setImsi_ld(imsi);

        if (lastBean1 != null) {
            if (!lastBean1.getArfcn().isEmpty() && !lastBean1.getPci().isEmpty()) { //输入不为空
                if (!checkParam(lastBean1.getArfcn(), Integer.parseInt(lastBean1.getPci()), key)) {
                    //不通过时
                    if (!isChanging()) { //列表内最后一个为空则替换
                        list.remove(list.size() - 1);
                        isChanging = true;
                        list.add(new ArfcnPciBean(lastBean1.getArfcn(), lastBean1.getPci()));
                        adapter.notifyDataSetChanged();
                    }
                    return false;
                }
                //点击添加按钮输入新的之后需要先添加进去再比较
                //判断是否重复
                List<ArfcnPciBean> listDelete = new ArrayList<>(list);
//                if (isChanging()) {
                //删除最后一个再比较
                listDelete.remove(listDelete.size() - 1);
//                }

                //相同值检查
                for (ArfcnPciBean arfcnPciBean : listDelete) {
//                    if (arfcnPciBean.getArfcn().equals(lastBean1.getArfcn()) && arfcnPciBean.getPci().equals(lastBean1.getPci())) {
                    if (arfcnPciBean.getArfcn().equals(lastBean1.getArfcn())) {
                        Util.showToast(lastBean1.getArfcn() + "已存在，请修改配置");
                        if (!isChanging()) { //点了添加按钮要把最后那个空的删了
                            list.remove(list.size() - 1);
                            isChanging = true;
                            list.add(new ArfcnPciBean(lastBean1.getArfcn(), lastBean1.getPci()));
                            adapter.notifyDataSetChanged();
                        }
                        return false;
                    }
                }
                //通过相同判断
                list.clear();
                list.addAll(listDelete);

                list.add(lastBean1);
                adapter.notifyDataSetChanged();
            } else if (list.size() > 0) {   //如果没填全直接去掉
                list.removeLast();
                adapter.notifyDataSetChanged();
            }
            setIsChanging(true);        //为了下次点击添加的时候能够识别为会被修改的，不论是否为空
        }
        while (list.size() > 0 && list.get(0).getArfcn().isEmpty()) {
            list.remove(0);
            adapter.notifyDataSetChanged();
        }


        //不可轮询逻辑：除B3、N78、B40能配两个之外，其他的只能配一个频点
        String tint = ifOnlyOneArfcn(list);
        if (!tint.equals("成功")) {
            Util.showToast("每个通道只能配置一个频点！" + tint + "!", Toast.LENGTH_LONG);
            return false;
        }

        if (PaCtl.build().is3G758 && !MainActivity.ifDebug) {
            // 不支持N1(B1)和 B34/39/40/41同开
            boolean hasN1orB1 = false;
            boolean hasB34orB39orB40orB41 = false;
            for (ArfcnPciBean bean : list) {
                int band;
                if (bean.getArfcn().length() > 5) {
                    band = NrBand.earfcn2band(Integer.parseInt(bean.getArfcn()));
                    if (band == 1) {
                        hasN1orB1 = true;
                    }
                } else {
                    band = LteBand.earfcn2band(Integer.parseInt(bean.getArfcn()));
                    if (band == 1) {
                        hasN1orB1 = true;
                    }
                    if (band == 34 || band == 39 || band == 40 || band == 41) {
                        hasB34orB39orB40orB41 = true;
                    }
                }
            }

            if (hasN1orB1 && hasB34orB39orB40orB41) {
                Util.showToast("不支持N1/B1和 B34/39/40/41同开");
                return false;
            }
        }


        // B41需要有N41同开
        boolean hasB41 = false;
        boolean hasN41 = false;
        for (ArfcnPciBean bean : list) {
            int band;
            if (bean.getArfcn().length() > 5) {
                band = NrBand.earfcn2band(Integer.parseInt(bean.getArfcn()));
                if (band == 41) {
                    hasN41 = true;
                }
            } else {
                band = LteBand.earfcn2band(Integer.parseInt(bean.getArfcn()));
                if (band == 41) {
                    hasB41 = true;
                }
            }
        }
        if (hasB41) {
            if (!hasN41) {
                Util.showToast("B41频点需和N41同开");
                return false;
            }
        }


        if (list.size() == 0) {
            Util.showToast(mContext.getString(R.string.no_input_data));
            list.add(new ArfcnPciBean("", ""));
            return false;
        }
        return true;
    }

    private void storeTestImsi() {
        String imsi2 = actv_imsi2.getText().toString();
        String imsi3 = actv_imsi3.getText().toString();
        String imsi4 = actv_imsi4.getText().toString();
        String imsi5 = actv_imsi5.getText().toString();
        String imsi6 = actv_imsi6.getText().toString();
        String imsi7 = actv_imsi7.getText().toString();
        String imsi8 = actv_imsi8.getText().toString();
        String imsi9 = actv_imsi9.getText().toString();
        String imsi10 = actv_imsi10.getText().toString();
        String imsi11 = actv_imsi11.getText().toString();
        String imsi12 = actv_imsi12.getText().toString();
        String[] imsiList = new String[]{imsi2, imsi3, imsi4, imsi5, imsi6, imsi7, imsi8, imsi9, imsi10, imsi11, imsi12};
        for (String imsi : imsiList) {
            if (!Util.existSameData(dropImsiList, imsi) && imsi != null && !imsi.isEmpty()) {
                if (dropImsiList.size() > 20) {
                    dropImsiList.remove(0);
                }
                dropImsiList.add(imsi);
                PrefUtil.build().setDropImsiList(dropImsiList);
                dropImsiAdapter.setList(dropImsiList);
            }
        }
    }

    /**
     * 3G758测向：
     * 1：N78\79
     * 2:B1
     * 3:N1
     * 4:B3
     * 5:N28\N78次
     * 6:B40次
     * 7:N41
     * 8:B40
     * 9:N3
     * 10:B39
     * 11:B5\B8
     * 12:B34\B41
     * B3N78B40能开两个
     * N78和N79不可同时存在、N28和次N78不可同时存在，B5和B8不可同时存在，B34和B41不可同时存在
     */
    private String ifOnlyOneArfcn(LinkedList<ArfcnPciBean> list) {
        String tint = "";
        ArrayList<Integer> arrayListNr = new ArrayList<>();
        ArrayList<Integer> arrayListLte = new ArrayList<>();
        ArrayList<Integer> arrayB3N78B40 = new ArrayList<>();
        ArrayList<Integer> arrayN78N79 = new ArrayList<>();
        ArrayList<Integer> arrayN28SecN78 = new ArrayList<>();
        ArrayList<Integer> arrayB5B8 = new ArrayList<>();
        ArrayList<Integer> arrayB34B41 = new ArrayList<>();

        for (ArfcnPciBean bean : list) {
            int band;
            if (bean.getArfcn().length() > 5) {
                band = NrBand.earfcn2band(Integer.parseInt(bean.getArfcn()));
                if (!arrayListNr.contains(band)) {
                    if (band == 79 && arrayListNr.contains(78)
                            || band == 78 && arrayListNr.contains(79))
                        return "N78与N79冲突";   //N78和N79不可同时存在
                    if (band == 28 && arrayB3N78B40.contains(78))
                        return "N28与次N78冲突";   //N28和次N78不可同时存在
                    arrayListNr.add(band);
                } else {
                    if (band == 78) {
                        if (!arrayB3N78B40.contains(band)) {    //第二个数组没有就添加进去
                            if (arrayListNr.contains(28))
                                return "N28与次N78冲突";   //N28和次N78不可同时存在
                            arrayB3N78B40.add(band);
                        } else return "已存在两个B3/N78/B40频点";    //已存在两个B3N78B40频点
                    } else return "已存在相同频段频点";    //已存在相同频段
                }
            } else {
                band = LteBand.earfcn2band(Integer.parseInt(bean.getArfcn()));
                if (!arrayListLte.contains(band)) {
                    if (band == 5 && arrayListLte.contains(8)
                            || band == 8 && arrayListLte.contains(5))
                        return "B5与B8冲突";   //B5和B8不可同时存在
                    if (band == 34 && arrayListLte.contains(41)
                            || band == 41 && arrayListLte.contains(34))
                        return "B34与B41冲突";   //B34和B41不可同时存在
                    arrayListLte.add(band);
                } else {
                    if (band == 3 || band == 40) {
                        if (!arrayB3N78B40.contains(band)) {    //第二个数组没有就添加进去
                            arrayB3N78B40.add(band);
                        } else return "已存在两个B3/N78/B40频点";    //已存在两个B3N78B40频点
                    } else return "已存在相同频段频点";    //已存在一个相同频点
                }
            }
        }
        return "成功";
    }

    //检查imsi和arfcn的对应关系
    public boolean checkImsiArfcnParam() {
        if (MainActivity.ifDebug) return true;
//       ArfcnPciBean bean1 = TD1_adapter.getLastViewText();
        String imsi;
        LinkedList<ArfcnPciBean> list;
        if (isYiguangChecked()) {
            imsi = historyBean.getImsi_yg();
            list = historyBean.getYgList();
        } else {
            imsi = historyBean.getImsi_ld();
            list = historyBean.getLdList();
        }
        if (!imsi.isEmpty()) {
            String plmn = imsi2Plmn(imsi);
            //通过arfcn计算出对应的imsi然后进行比较
            for (ArfcnPciBean bean : list) {
                String plmnFromArfcn = getPlmnFromArfcn(bean.getArfcn());
                AppLog.D("checkImsiArfcnParam arfcn:" + bean.getArfcn() + " plmnFromArfcn:" + plmnFromArfcn);
                if (!isSameOperator(plmn, plmnFromArfcn)) {
                    Util.showToast("频点" + bean.getArfcn() + "与imsi号运营商不一致，请修改配置", Toast.LENGTH_LONG);

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
                    int freq = LteBand2.earfcn2freq(Integer.parseInt(arfcn));
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


    private boolean checkParam(String arfcn, int i_pci, String key) {

        if (MainActivity.ifDebug) return true;
        int band;
        switch (key) {
            case "yiguang":
                if (arfcn.length() > 5) {
                    band = NrBand.earfcn2band(Integer.parseInt(arfcn));
                    if (PaCtl.build().is3G758) { // N41/N78/N79
                        if (band != 28 && band != 41 && band != 79) {
                            Util.showToast(mContext.getString(R.string._3g758cx_yg_support_tip));
                            return false;
                        }
                    } else {// N28/N78/N79
                        if (band != 28 && band != 78 && band != 79) {
                            Util.showToast(mContext.getString(R.string.b97501_cell0_support_tip));
                            return false;
                        }
                    }
                    if (i_pci < 0 || i_pci > 1007) {
                        Util.showToast(mContext.getString(R.string.pci_err_tip_5g));
                        return false;
                    }
                    break;
                } else {  //4g
                    band = LteBand.earfcn2band(Integer.parseInt(arfcn));
//                    AppLog.D("checkParam arfcn:" + arfcn + " band:" + band);
                    if (PaCtl.build().is3G758) { // B3\B8\B34\B39\B40\41
                        if (band != 3 && band != 8 && band != 34 && band != 39 && band != 40 && band != 41) {
                            Util.showToast(mContext.getString(R.string._3g758cx_yg_support_tip));
                            return false;
                        }
                        if (band == 3 || band == 8) {
                            int freq = LteBand2.earfcn2freq(Integer.parseInt(arfcn));
                            if (!((freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949))) {
                                Util.showToast(mContext.getString(R.string._3g758cx_yg_support_tip));
                                return false;
                            }
                        }
                    } else {
                        if (band != 28 && band != 78 && band != 79) {
                            Util.showToast(mContext.getString(R.string.b97501_cell0_support_tip));
                            return false;
                        }
                    }
                    if (i_pci < 0 || i_pci > 503) {
                        Util.showToast(mContext.getString(R.string.pci_err_tip_4g));
                        return false;
                    }
                }
                break;
            case "liandian":
                if (arfcn.length() > 5) {
                    band = NrBand.earfcn2band(Integer.parseInt(arfcn));
                    if (PaCtl.build().is3G758) { // N1/N78
                        if (band != 1 && band != 3 && band != 78) {
                            Util.showToast(mContext.getString(R.string._3g758cx_ld_support_tip));
                            return false;
                        }
                    } else {// N28/N78/N79
                        if (band != 28 && band != 78 && band != 79) {
                            Util.showToast(mContext.getString(R.string.b97501_cell0_support_tip));
                            return false;
                        }
                    }
                    if (i_pci < 0 || i_pci > 1007) {
                        Util.showToast(mContext.getString(R.string.pci_err_tip_5g));
                        return false;
                    }
                    break;
                } else {  //4g
                    band = LteBand.earfcn2band(Integer.parseInt(arfcn));
                    if (PaCtl.build().is3G758) { // B3\B5\B8
                        if (band != 1 && band != 3 && band != 5 && band != 8) {
                            Util.showToast(mContext.getString(R.string._3g758cx_ld_support_tip));
                            return false;
                        }
                        if (band == 3 || band == 8) {
                            int freq = LteBand2.earfcn2freq(Integer.parseInt(arfcn));
                            if ((freq > 1709 && freq < 1735) || (freq > 1804 && freq < 1830) || (freq > 888 && freq < 904) || (freq > 933 && freq < 949)) {
                                Util.showToast(mContext.getString(R.string._3g758cx_ld_support_tip));
                                return false;
                            }
                        }
                    } else {
                        if (band != 28 && band != 78 && band != 79) {
                            Util.showToast(mContext.getString(R.string.b97501_cell0_support_tip));
                            return false;
                        }
                    }
                    if (i_pci < 0 || i_pci > 503) {
                        Util.showToast(mContext.getString(R.string.pci_err_tip_4g));
                        return false;
                    }
                }
                break;
        }
        return true;
    }

    //    private void showFreqListDialog(int cell_id) {
//        if (getScanArfcnBeanList() == null) {
//            Util.showToast(getString(R.string.no_freq_data_tip));
//            return;
//        }
//        MainActivity.getInstance().createCustomDialog(false);
//        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_freqlist_silde, null);
//
//        view.findViewById(R.id.back).setOnClickListener(
//                view1 -> MainActivity.getInstance().closeCustomDialog());
//
//        RecyclerView freq_list = view.findViewById(R.id.freq_list);
//        freq_list.setLayoutManager(new LinearLayoutManager(mContext));
//
//        FreqResultListAdapter adapter = new FreqResultListAdapter(mContext, getScanArfcnBeanList(), new ListItemListener() {
//            @Override
//            public void onItemClickListener(int position) {
//                //判断是否有重复频点
//                ScanArfcnBean bean = getScanArfcnBeanList().get(position);
//                int importArfcn = bean.getDl_arfcn();
//                LinkedList<ArfcnPciBean> list;
//                DualRecyclerviewAdapter adapter;
//                if (cell_id == 0) {
//                    list = historyBean.getTD1();
//                    adapter = yiguang_adapter;
//                } else if (cell_id == 1) {
//                    list = historyBean.getTD2();
//                    adapter = liandian_adapter;
//                } else if (cell_id == 2) {
//                    list = historyBean.getTD3();
//                    adapter = TD3_adapter;
//                } else {
//                    list = historyBean.getTD4();
//                    adapter = TD4_adapter;
//                }
//                int importPci = countPci(bean, cell_id, 0);  //扫频结果已经处理过，所以传入0
//
//                String lastArfcn = adapter.getLastViewText().getArfcn();    //最后的arfcn
//                String lastPci = adapter.getLastViewText().getPci();
//                //直接添加到第一个，不影响其他数据，不需检查last为空
//                if (adapter.getLastViewText() != null) {
//                    //相同信息
//                    if (lastArfcn.equals(String.valueOf(importArfcn)) && lastPci.equals(String.valueOf(importPci))) {
//                        Util.showToast("已存在相同数据，导入失败");
//                        return;
//                    }
//                }
//
//                if (list.size() > 1) {   //等于1的时候只需要检查last就够了
//                    for (ArfcnPciBean arfcnPciBean : list) {
//                        if (arfcnPciBean.getArfcn().equals(String.valueOf(importArfcn)) && arfcnPciBean.getPci().equals(String.valueOf(importPci))) {
//                            Util.showToast("已存在相同数据，导入失败");
//                            return;
//                        }
//                    }
//                }
//
//                list.remove(list.size() - 1);
//                list.add(adapter.getLastViewText());
//                adapter.notifyDataSetChanged();
//                setIsChanging(true);
//
//                if (checkParam(String.valueOf(importArfcn), importPci, "TD" + (cell_id + 1))) {
//                    if (importArfcn(getScanArfcnBeanList().get(position), cell_id, 0)) {
////                    adapter.setLastEditText(lastArfcn, lastPci);        //为什么这个是在前面运行的
//                        MainActivity.getInstance().closeCustomDialog();
//                    } else Util.showToast(getString(R.string.cell_not_support_tip));
//                }
//
//            }
//        });
//        freq_list.setLayoutManager(new LinearLayoutManager(mContext));
//        freq_list.setAdapter(adapter);
//        MainActivity.getInstance().showCustomDialog(view, false, true);
//    }
    public boolean isYiguangChecked() {
        if (sw_auto_trace.isChecked()) {    //自动模式判定方式
            if (ed_auto_imsi.getEditableText().toString().length() == 15) {
                String tracePlmn = ed_auto_imsi.getEditableText().toString().substring(0, 5);
                switch (tracePlmn) {
                    case "46003":
                    case "46005":
                    case "46011":
                    case "46012":
                        return false;
                    case "46000":
                    case "46002":
                    case "46004":
                    case "46007":
                    case "46008":
                    case "46013":
                        return true;
                    case "46001":
                    case "46006":
                    case "46009":
                    case "46010":
                        return false;
                    case "46015":
                        return true;
                }
            }
        }
        if (cl_yiguang != null) {   //手动模式判定方式
            return cl_yiguang.getVisibility() == View.VISIBLE;
        }
        return true;
    }

    public void clearCfg() {
        if (yiguang_adapter != null) {
            yiguang_adapter.clear();
        }
        if (liandian_adapter != null) {
            liandian_adapter.clear();
        }
    }

    public HistoryBean_3g758cx getHistoryBean() {
        return historyBean;
    }

    private ArrayList<ScanArfcnBean> getScanArfcnBeanList() {
        return MainActivity.getInstance().getScanArfcnBeanList();
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
            mTraceCatchFragment.dosetDWTxPwrOffset(-3);
            if (DBViewModel.getWorkState() == GnbBean.DB_State.PWR_DETECT) {
                MessageController.build().setDBRxGain(DBViewModel.getDeviceId(), 2);
                DBViewModel.setRxGain(2);
            }
        } else {
            iv_instance.setBackgroundResource(R.mipmap.icon_distance2_n);
            mTraceCatchFragment.dosetDWTxPwrOffset(0);
            if (DBViewModel.getWorkState() == GnbBean.DB_State.PWR_DETECT) {
                MessageController.build().setDBRxGain(DBViewModel.getDeviceId(), 0);
                DBViewModel.setRxGain(0);
            }
        }
    }

    @Override
    public void onItemClickListener(int position) {

    }

    @Override
    public void onHistoryItemClickListener(JSONArray jsonArray) {
        AppLog.D("onHistoryItemClickListener jsonArray = " + jsonArray.toString() + ", " + jsonArray.length());
        if (jsonArray.length() == 0) {
            historyBean.getYgList().add(new ArfcnPciBean("", ""));
            historyBean.getLdList().add(new ArfcnPciBean("", ""));
            return;
        }
        try {
            //数据导入定位页
            JSONObject jsonObject1 = jsonArray.getJSONObject(0);
            JSONObject jsonObject2 = jsonArray.getJSONObject(1);

            String imsi1 = jsonObject1.getString("imsi");
            String imsi2 = jsonObject2.getString("imsi");

            String arfcnJsonArray1 = jsonObject1.getString("arfcnPciJsonArray");
            String arfcnJsonArray2 = jsonObject2.getString("arfcnPciJsonArray");

            historyBean.getYgList().clear();
            historyBean.getLdList().clear();
            JSONArray arfcnArray1 = new JSONArray(arfcnJsonArray1);
            for (int i = 0; i < arfcnArray1.length(); i++) {
                String[] string = arfcnArray1.getString(i).split("/");
                if (string.length > 1)
                    historyBean.getYgList().add(new ArfcnPciBean(string[0], string[1]));
            }
            JSONArray arfcnArray2 = new JSONArray(arfcnJsonArray2);
            for (int i = 0; i < arfcnArray2.length(); i++) {
                String[] string = arfcnArray2.getString(i).split("/");
                if (string.length > 1)
                    historyBean.getLdList().add(new ArfcnPciBean(string[0], string[1]));
            }

            actv_imsi_yiguang.setText(imsi1);
            actv_imsi_liandian.setText(imsi2);

            if (historyBean.getYgList().size() == 0)
                historyBean.getYgList().add(new ArfcnPciBean("", ""));
            if (historyBean.getLdList().size() == 0)
                historyBean.getLdList().add(new ArfcnPciBean("", ""));

            if (yiguang_adapter != null)
                yiguang_adapter.notifyDataSetChanged();
            if (liandian_adapter != null)
                liandian_adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void resetArfcnList() {
        AppLog.I("resetArfcnList");
        if (historyBean.getYgList().size() == 0) {
            historyBean.getYgList().add(new ArfcnPciBean("", ""));
            yiguang_adapter.notifyDataSetChanged();
        }
        if (historyBean.getLdList().size() == 0) {
            historyBean.getLdList().add(new ArfcnPciBean("", ""));
            liandian_adapter.notifyDataSetChanged();
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

    @Override
    public void refreshRVHeight() {
//        setRVHeight(rv_arfcn_liandian, liandian_adapter);
//        setRVHeight(rv_arfcn_yiguang, yiguang_adapter);
    }

    public DualRecyclerviewAdapter getYiguang_adapter() {
        return yiguang_adapter;
    }

    public DualRecyclerviewAdapter getLiandian_adapter() {
        return liandian_adapter;
    }

    public AutoCompleteTextView getActv_imsi2() {
        return actv_imsi2;
    }

    public AutoCompleteTextView getActv_imsi3() {
        return actv_imsi3;
    }

    public AutoCompleteTextView getActv_imsi4() {
        return actv_imsi4;
    }

    public AutoCompleteTextView getActv_imsi5() {
        return actv_imsi5;
    }

    public AutoCompleteTextView getActv_imsi6() {
        return actv_imsi6;
    }

    public AutoCompleteTextView getActv_imsi7() {
        return actv_imsi7;
    }

    public AutoCompleteTextView getActv_imsi8() {
        return actv_imsi8;
    }

    public AutoCompleteTextView getActv_imsi9() {
        return actv_imsi9;
    }

    public AutoCompleteTextView getActv_imsi10() {
        return actv_imsi10;
    }

    public AutoCompleteTextView getActv_imsi11() {
        return actv_imsi11;
    }

    public AutoCompleteTextView getActv_imsi12() {
        return actv_imsi12;
    }

    public void setIfDebug(boolean isDebug) {
        tv_test_start_tint.setVisibility(isDebug ? View.VISIBLE : View.GONE);
        ll_test_11imsi.setVisibility(isDebug ? View.VISIBLE : View.GONE);
        iv_instance.setVisibility(isDebug ? View.VISIBLE : View.GONE);
    }


}