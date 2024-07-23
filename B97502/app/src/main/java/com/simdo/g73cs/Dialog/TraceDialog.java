package com.simdo.g73cs.Dialog;

import static com.simdo.g73cs.MainActivity.device;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.Gnb.Bean.GnbProtocol;
import com.simdo.g73cs.Adapter.AutoSearchAdapter;
import com.simdo.g73cs.Adapter.BlackListAdapter;
import com.simdo.g73cs.Adapter.DualRecyclerviewAdapter;
import com.simdo.g73cs.Adapter.FreqResultListAdapter;
import com.simdo.g73cs.Adapter.HistoryAdapter;
import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.Bean.CityBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.HistoryBean;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.FreqUtil;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.StatusBarUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.View.BlackListSlideAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TraceDialog extends Dialog implements DualRecyclerviewAdapter.OnDelArfcnListListener {

    private final Context mContext;
    private final HistoryAdapter adapter;
    AutoCompleteTextView actv_imsi;
    private final List<String> dropImsiList = new ArrayList<>();
    DualRecyclerviewAdapter TD1_adapter, TD2_adapter, TD3_adapter, TD4_adapter;
    HistoryBean historyBean;
    HistoryBean old_historyBean;
    boolean isAutoPci = false;

    public TraceDialog(Context context, HistoryAdapter adapter, int pos, boolean isWorking) {
        super(context, R.style.Theme_G73CS);
        this.mContext = context;
        this.adapter = adapter;
        if (pos != -1) {
            this.historyBean = MainActivity.getInstance().historyList.get(pos);
            old_historyBean = new HistoryBean(this.historyBean.getMode(), this.historyBean.getImsiFirst());
            old_historyBean.setImsiSecond(this.historyBean.getImsiSecond());
            old_historyBean.setImsiThird(this.historyBean.getImsiThird());
            old_historyBean.setImsiFourth(this.historyBean.getImsiFourth());
            old_historyBean.setTD1(new LinkedList<>(this.historyBean.getTD1()));
            old_historyBean.setTD2(new LinkedList<>(this.historyBean.getTD2()));
            old_historyBean.setTD3(new LinkedList<>(this.historyBean.getTD3()));
            old_historyBean.setTD4(new LinkedList<>(this.historyBean.getTD4()));
        } else this.historyBean = new HistoryBean(2, "");
        View view = View.inflate(context, R.layout.child_pager_cfg, null);
        this.setContentView(view);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setNavigationBarColor(Color.parseColor("#2A72FF"));
        //window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        //StatusBarUtil.setLightStatusBar(window, true);
        WindowManager.LayoutParams lp = window.getAttributes();

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        initView(view, pos, isWorking);
    }

    @Override
    public void onBackPressed() {
        if (old_historyBean != null) {
            historyBean.setTD1(old_historyBean.getTD1());
            historyBean.setTD2(old_historyBean.getTD2());
            historyBean.setTD3(old_historyBean.getTD3());
            historyBean.setTD4(old_historyBean.getTD4());
            old_historyBean = null;
        }
        dismiss();
    }

    private void initView(View root, int pos, boolean isWorking) {
        root.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (old_historyBean != null) {
                    historyBean.setTD1(old_historyBean.getTD1());
                    historyBean.setTD2(old_historyBean.getTD2());
                    historyBean.setTD3(old_historyBean.getTD3());
                    historyBean.setTD4(old_historyBean.getTD4());
                    old_historyBean = null;
                }
                dismiss();
            }
        });

        TextView btn_ok = root.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkLastParam()) return;
                boolean isHas3 = false;
                boolean isHas8 = false;
                boolean isHas39 = false;
                if (PaCtl.build().isB97502){
                    for (ArfcnPciBean bean : historyBean.getTD2()) {
                        int band = LteBand.earfcn2band(Integer.parseInt(bean.getArfcn()));
                        if (band == 3) isHas3 = true;
                        else if (band == 8) isHas8 = true;
                    }
                    for (ArfcnPciBean bean : historyBean.getTD4()) {
                        if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 39){
                            isHas39 = true;
                            break;
                        }
                    }
                }else {
                    for (ArfcnPciBean bean : historyBean.getTD4()) {
                        int band = LteBand.earfcn2band(Integer.parseInt(bean.getArfcn()));
                        if (band == 3) isHas3 = true;
                        else if (band == 8) isHas8 = true;
                    }
                    for (ArfcnPciBean bean : historyBean.getTD2()) {
                        if (LteBand.earfcn2band(Integer.parseInt(bean.getArfcn())) == 39){
                            isHas39 = true;
                            break;
                        }
                    }
                }
                if (isHas3 && isHas39) showTipDialog(view, pos, mContext.getResources().getString(R.string.b3_b39_tip));
                else if (isHas8 && isHas39) showTipDialog(view, pos, mContext.getResources().getString(R.string.b8_b39_tip));
                else checkAddOrUpdate(view, pos);
            }
        });

        TextView tv_cell_tip_1 = root.findViewById(R.id.tv_cell_tip_1);
        tv_cell_tip_1.requestFocus();
        TextView tv_cell_tip_2 = root.findViewById(R.id.tv_cell_tip_2);
        TextView tv_cell_tip_3 = root.findViewById(R.id.tv_cell_tip_3);
        TextView tv_cell_tip_4 = root.findViewById(R.id.tv_cell_tip_4);

        root.findViewById(R.id.tv_import).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBlackDialog();
            }
        });

        actv_imsi = root.findViewById(R.id.actv_imsi);

        AutoSearchAdapter dropImsiAdapter = new AutoSearchAdapter(mContext, dropImsiList);
        actv_imsi.setAdapter(dropImsiAdapter);
        if (PaCtl.build().isB97502) {
            tv_cell_tip_1.setText("(/N41/N78/N79)");
            tv_cell_tip_2.setText("(/B3/B5/B8/" + mContext.getString(R.string.order) + "B40)");
            tv_cell_tip_3.setText("(/B1/N1/N28)");
            tv_cell_tip_4.setText("(/B34/B39/B40/B41/" + mContext.getString(R.string.order) + "B3)");
        } else {
            tv_cell_tip_1.setText("(/N28/N78/N79)");
            tv_cell_tip_2.setText("(/B34/B39/B40/B41)");
            tv_cell_tip_3.setText("(/N1/N41)");
            tv_cell_tip_4.setText("(/B1/B3/B5/B8)");
        }

        if (pos != -1) actv_imsi.setText(historyBean.getImsiFirst());
        if (historyBean.getTD1().size() == 0) historyBean.getTD1().add(new ArfcnPciBean("", ""));
        if (historyBean.getTD2().size() == 0) historyBean.getTD2().add(new ArfcnPciBean("", ""));
        if (historyBean.getTD3().size() == 0) historyBean.getTD3().add(new ArfcnPciBean("", ""));
        if (historyBean.getTD4().size() == 0) historyBean.getTD4().add(new ArfcnPciBean("", ""));

        TD1_adapter = new DualRecyclerviewAdapter(mContext, historyBean.getTD1(), "TD1", isWorking);
        TD2_adapter = new DualRecyclerviewAdapter(mContext, historyBean.getTD2(), "TD2", isWorking);
        TD3_adapter = new DualRecyclerviewAdapter(mContext, historyBean.getTD3(), "TD3", isWorking);
        TD4_adapter = new DualRecyclerviewAdapter(mContext, historyBean.getTD4(), "TD4", isWorking);
        TD1_adapter.setOnDelArfcnListListener(this);
        TD2_adapter.setOnDelArfcnListListener(this);
        TD3_adapter.setOnDelArfcnListListener(this);
        TD4_adapter.setOnDelArfcnListListener(this);

        RecyclerView rv_arfcn_1 = root.findViewById(R.id.rv_arfcn_1);
        rv_arfcn_1.setAdapter(TD1_adapter);
        rv_arfcn_1.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        root.findViewById(R.id.btn_add_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData(v, TD1_adapter, historyBean.getTD1(), "TD1");
            }
        });

        RecyclerView rv_arfcn_2 = root.findViewById(R.id.rv_arfcn_2);
        rv_arfcn_2.setAdapter(TD2_adapter);
        rv_arfcn_2.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        root.findViewById(R.id.btn_add_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData(v, TD2_adapter, historyBean.getTD2(), "TD2");
            }
        });

        RecyclerView rv_arfcn_3 = root.findViewById(R.id.rv_arfcn_3);
        rv_arfcn_3.setAdapter(TD3_adapter);
        rv_arfcn_3.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        root.findViewById(R.id.btn_add_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData(v, TD3_adapter, historyBean.getTD3(), "TD3");
            }
        });

        RecyclerView rv_arfcn_4 = root.findViewById(R.id.rv_arfcn_4);
        rv_arfcn_4.setAdapter(TD4_adapter);
        rv_arfcn_4.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        root.findViewById(R.id.btn_add_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData(v, TD4_adapter, historyBean.getTD4(), "TD4");
            }
        });

        final Switch switch_auto_pci = root.findViewById(R.id.switch_auto_pci);
        isAutoPci = historyBean.getMode() == 3;
        switch_auto_pci.setChecked(isAutoPci);
        TD1_adapter.setEnablePci(isAutoPci);
        TD2_adapter.setEnablePci(isAutoPci);
        TD3_adapter.setEnablePci(isAutoPci);
        TD4_adapter.setEnablePci(isAutoPci);
        switch_auto_pci.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAutoPci = switch_auto_pci.isChecked();
                TD1_adapter.setEnablePci(isAutoPci);
                TD2_adapter.setEnablePci(isAutoPci);
                TD3_adapter.setEnablePci(isAutoPci);
                TD4_adapter.setEnablePci(isAutoPci);
            }
        });

        root.findViewById(R.id.tv_import_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFreqListDialog(0);
            }
        });
        root.findViewById(R.id.tv_import_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFreqListDialog(1);
            }
        });
        root.findViewById(R.id.tv_import_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFreqListDialog(2);
            }
        });
        root.findViewById(R.id.tv_import_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFreqListDialog(3);
            }
        });

        if (isWorking) {
            root.findViewById(R.id.btn_all).setVisibility(View.INVISIBLE);
            actv_imsi.setEnabled(false);
            btn_ok.setVisibility(View.GONE);
        }else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    actv_imsi.requestFocus();
                    actv_imsi.setSelection(actv_imsi.getText().toString().length());
                }
            }, 200);
        }
    }

    private void checkAddOrUpdate(View view, int pos){
        historyBean.setMode(isAutoPci ? 3 : 2);
        if (pos != -1){
            adapter.updateData(pos, historyBean);
        } else if (!adapter.addData(historyBean)) {
            MainActivity.getInstance().showToast(mContext.getString(R.string.value_exist));
            return;
        }
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        MainActivity.getInstance().showToast(pos != -1 ? mContext.getString(R.string.updated) : mContext.getString(R.string.added), true);
        dismiss();
    }

    private void showTipDialog(View root, int pos, String waringInfo){
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_confirm, null);
        TextView tv_title = view.findViewById(R.id.tv_title);
        TextView tv_msg = view.findViewById(R.id.tv_msg);
        tv_title.setText(mContext.getResources().getString(R.string.tip));

        tv_msg.setText(waringInfo);

        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
                checkAddOrUpdate(root, pos);
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

    private void showBlackDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_blacklist_silde, null);

        RecyclerView black_list = view.findViewById(R.id.black_list);
        black_list.setLayoutManager(new LinearLayoutManager(mContext));
        List<MyUeidBean> blackList = MainActivity.getInstance().getBlackList();
        BlackListAdapter blackListAdapter = new BlackListAdapter(mContext, blackList, new ListItemListener() {
            @Override
            public void onItemClickListener(int position) {
                actv_imsi.setText(blackList.get(position).getUeidBean().getImsi());
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        black_list.setAdapter(blackListAdapter);
        view.findViewById(R.id.tv_left_tip).setVisibility(View.GONE);
        view.findViewById(R.id.tv_add).setVisibility(View.GONE);
        view.findViewById(R.id.tv_import).setVisibility(View.GONE);
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false, true);
    }

    private void addData(View v, DualRecyclerviewAdapter adapter, List<ArfcnPciBean> list, String key) {
        ArfcnPciBean arfcnPciBean = adapter.getLastViewText();
        if (arfcnPciBean == null) {
            adapter.addEditView();
            return;
        }
        String arfcn = arfcnPciBean.getArfcn();
        if (arfcn.isEmpty()) {
            showToast(mContext.getString(R.string.arfcn_empty_err));
            return;
        }
        // 如果是自配置，这里先赋值为50，开启时候检测到如果是自配置，则扫频更新pci
        String pci = arfcnPciBean.getPci();
        if (isAutoPci && pci.isEmpty()) {
            arfcnPciBean.setPci("50");
            pci = "50";
        }
        if (pci.isEmpty()) {
            showToast(mContext.getString(R.string.pci_empty_err));
            return;
        }
        int i_pci = Integer.parseInt(pci);

        if (!checkParam(arfcn, i_pci, key)) return;

        if (adapter.addData(arfcnPciBean)) adapter.addEditView();
        else showToast(mContext.getString(R.string.value_exist));
    }

    private boolean checkLastParam() {
        ArfcnPciBean bean1 = TD1_adapter.getLastViewText();
        String imsi = actv_imsi.getText().toString();
        if (!imsi.isEmpty() && imsi.length() != 15){
            MainActivity.getInstance().showToast(mContext.getString(R.string.cell_imsi_err));
            return false;
        }
        historyBean.setImsiFirst(imsi);
        if (bean1 != null){
            if (isAutoPci) bean1.setPci("50");
            if (!bean1.getArfcn().isEmpty() && !bean1.getPci().isEmpty()){
                if (!checkParam(bean1.getArfcn(), Integer.parseInt(bean1.getPci()), "TD1")) return false;
                if (!TD1_adapter.addData(bean1)){
                    showToast(mContext.getString(R.string.value_exist));
                    return false;
                }
            }else if (historyBean.getTD1().size() > 0) historyBean.getTD1().removeLast();
        }
        while (historyBean.getTD1().size() > 0 && historyBean.getTD1().get(0).getArfcn().isEmpty()) historyBean.getTD1().remove(0);

        ArfcnPciBean bean2 = TD2_adapter.getLastViewText();

        historyBean.setImsiSecond(imsi);
        if (bean2 != null){
            if (isAutoPci) bean2.setPci("50");
            if (!bean2.getArfcn().isEmpty() && !bean2.getPci().isEmpty()){
                if (!checkParam(bean2.getArfcn(), Integer.parseInt(bean2.getPci()), "TD2")) return false;
                if (!TD2_adapter.addData(bean2)){
                    showToast(mContext.getString(R.string.value_exist));
                    return false;
                }
            }else if (historyBean.getTD2().size() > 0) historyBean.getTD2().removeLast();
        }
        while (historyBean.getTD2().size() > 0 && historyBean.getTD2().get(0).getArfcn().isEmpty()) historyBean.getTD2().remove(0);

        ArfcnPciBean bean3 = TD3_adapter.getLastViewText();

        historyBean.setImsiThird(imsi);
        if (bean3 != null){
            if (isAutoPci) bean3.setPci("50");
            if (!bean3.getArfcn().isEmpty() && !bean3.getPci().isEmpty()){
                if (!checkParam(bean3.getArfcn(), Integer.parseInt(bean3.getPci()), "TD3")) return false;
                if (!TD3_adapter.addData(bean3)){
                    showToast(mContext.getString(R.string.value_exist));
                    return false;
                }
            }else if (historyBean.getTD3().size() > 0) historyBean.getTD3().removeLast();
        }
        while (historyBean.getTD3().size() > 0 && historyBean.getTD3().get(0).getArfcn().isEmpty()) historyBean.getTD3().remove(0);

        ArfcnPciBean bean4 = TD4_adapter.getLastViewText();

        historyBean.setImsiFourth(imsi);
        if (bean4 != null){
            if (isAutoPci) bean4.setPci("50");
            if (!bean4.getArfcn().isEmpty() && !bean4.getPci().isEmpty()){
                if (!checkParam(bean4.getArfcn(), Integer.parseInt(bean4.getPci()), "TD4")) return false;
                if (!TD4_adapter.addData(bean4)){
                    showToast(mContext.getString(R.string.value_exist));
                    return false;
                }
            }else if (historyBean.getTD4().size() > 0) historyBean.getTD4().removeLast();
        }
        while (historyBean.getTD4().size() > 0 && historyBean.getTD4().get(0).getArfcn().isEmpty()) historyBean.getTD4().remove(0);

        if (PaCtl.build().isB97502){
            // 双频点检测
            int td2 = historyBean.getTD2().size();
            int td4 = historyBean.getTD4().size();
            int arfcn_band_2 = 0;
            if (td2 > 0) arfcn_band_2 = LteBand.earfcn2band(Integer.parseInt(historyBean.getTD2().get(0).getArfcn()));

            int arfcn_band_4 = 0;
            if (td4 > 0) arfcn_band_4 = LteBand.earfcn2band(Integer.parseInt(historyBean.getTD4().get(0).getArfcn()));

            // 双频点检测
            if (arfcn_band_2 == 40 && (arfcn_band_4 != 40 || td4 > 1)){
                MainActivity.getInstance().showToast(mContext.getString(R.string.cell3_need_b40));
                return false;
            }
            if (arfcn_band_4 == 3 && (arfcn_band_2 != 3 || td2 > 1)){
                MainActivity.getInstance().showToast(mContext.getString(R.string.cell1_need_b3));
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
            if ((arfcn_band_3_nr == 1 || arfcn_band_3_lte == 1) && (td4 > 0 && arfcn_band_4 != 3)){
                MainActivity.getInstance().showToast(mContext.getString(R.string.not_use_cell3));
                return false;
            }
        }

        if (historyBean.getTD1().size() == 0 && historyBean.getTD2().size() == 0 && historyBean.getTD3().size() == 0 && historyBean.getTD4().size() == 0){
            MainActivity.getInstance().showToast(mContext.getString(R.string.no_input_data));
            historyBean.getTD1().add(new ArfcnPciBean("", ""));
            historyBean.getTD2().add(new ArfcnPciBean("", ""));
            historyBean.getTD3().add(new ArfcnPciBean("", ""));
            historyBean.getTD4().add(new ArfcnPciBean("", ""));
            return false;
        }
        return true;
    }

    private boolean checkParam(String arfcn, int i_pci, String key){
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
                int lte_band = 0;
                if (PaCtl.build().isB97502) {
                    lte_band = LteBand.earfcn2band(Integer.parseInt(arfcn));
                    if (lte_band != 1/* && lte_band != 7*/ && band != 1 && band != 28) {
                        showToast(mContext.getString(R.string.b97502_cell2_support_tip));
                        return false;
                    }
                } else {// N1/N41
                    if (band != 1 && band != 41) {
                        showToast(mContext.getString(R.string.b97501_cell2_support_tip));
                        return false;
                    }
                }
                if (lte_band == 1){
                    if (i_pci < 0 || i_pci > 503) {
                        showToast(mContext.getString(R.string.pci_err_tip_4g));
                        return false;
                    }
                }else {
                    if (i_pci < 0 || i_pci > 1007) {
                        showToast(mContext.getString(R.string.pci_err_tip_5g));
                        return false;
                    }
                }

                break;
            case "TD4":
                band = LteBand.earfcn2band(Integer.parseInt(arfcn));
                if (PaCtl.build().isB97502) {
                    if (band != 3 && band != 34 && band != 38 && band != 39 && band != 40 && band != 41) {
                        showToast(mContext.getString(R.string.b97502_cell3_support_tip));
                        return false;
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

    private void showFreqListDialog(int cell) {
        if (getFreqList() == null) {
            MainActivity.getInstance().showToast(mContext.getString(R.string.no_freq_data_tip));
            return;
        }
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_freqlist_silde, null);

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });

        RecyclerView freq_list = view.findViewById(R.id.freq_list);
        freq_list.setLayoutManager(new LinearLayoutManager(mContext));

        FreqResultListAdapter adapter = new FreqResultListAdapter(mContext, getFreqList(), new ListItemListener() {
            @Override
            public void onItemClickListener(int position) {
                if (importArfcn(getFreqList().get(position), cell, 0)) MainActivity.getInstance().closeCustomDialog();
                else MainActivity.getInstance().showToast(mContext.getString(R.string.cell_not_support_tip));
            }
        });
        freq_list.setLayoutManager(new LinearLayoutManager(mContext));
        freq_list.setAdapter(adapter);
        MainActivity.getInstance().showCustomDialog(view, false, true);
    }

    private LinkedList<ScanArfcnBean> getFreqList() {
        return MainActivity.getInstance().getFreqList();
    }

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
                        if (cell == -1 || cell == 2) isOk = TD3_adapter.addImportData(String.valueOf(arfcn), String.valueOf(pci));
                        break;
                    case 3:
                    case 5:
                    case 8:
                        if (cell == -1 || cell == 1) isOk = TD2_adapter.addImportData(String.valueOf(arfcn), String.valueOf(pci));
                        break;
                    case 34:
                    case 39:
                    case 40:
                    case 41:
                        if (cell == -1 || cell == 3) isOk = TD4_adapter.addImportData(String.valueOf(arfcn), String.valueOf(pci));
                        break;
                }
            } else {
                switch (band) {
                    case 1:
                    case 3:
                    case 5:
                    case 8:
                        if (cell == -1 || cell == 3) isOk = TD4_adapter.addImportData(String.valueOf(arfcn), String.valueOf(pci));
                        break;
                    case 34:
                    case 39:
                    case 40:
                    case 41:
                        if (cell == -1 || cell == 1) isOk = TD2_adapter.addImportData(String.valueOf(arfcn), String.valueOf(pci));
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
                        if (cell == -1 || cell == 0) isOk = TD1_adapter.addImportData(String.valueOf(arfcn), String.valueOf(pci));
                    } else {
                        if (cell == -1 || cell == 2) isOk = TD3_adapter.addImportData(String.valueOf(arfcn), String.valueOf(pci));
                    }
                    break;
                case 78:
                case 79:
                    if (cell == -1 || cell == 0) isOk = TD1_adapter.addImportData(String.valueOf(arfcn), String.valueOf(pci));
                    break;
                case 1:
                    if (cell == -1 || cell == 2) isOk = TD3_adapter.addImportData(String.valueOf(arfcn), String.valueOf(pci));
                    break;
                case 28:
                    if (PaCtl.build().isB97502) {
                        if (cell == -1 || cell == 2) isOk = TD3_adapter.addImportData(String.valueOf(arfcn), String.valueOf(pci));
                    } else {
                        if (cell == -1 || cell == 0) isOk = TD1_adapter.addImportData(String.valueOf(arfcn), String.valueOf(pci));
                    }
                    break;
            }
        }
        return isOk;
    }

    @Override
    public void OnDelArfcn(ArfcnPciBean bean, String band) {

    }


}
