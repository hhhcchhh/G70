package com.simdo.g73cs.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.simdo.g73cs.Adapter.DualRecyclerviewAdapter;
import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.Fragment.TraceCatchFragment;
import com.simdo.g73cs.Listener.OnTraceSetListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DividerItemDecoration;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;

import java.util.LinkedList;

public class TraceDialog extends Dialog implements DualRecyclerviewAdapter.OnDelArfcnListListener {

    LinkedList<ArfcnPciBean> TD1, TD2, TD3, TD4;
    DualRecyclerviewAdapter TD1_adapter, TD2_adapter, TD3_adapter, TD4_adapter;
    TraceCatchFragment mTraceCatchFragment;
    OnTraceSetListener listener;
    View contentView;
    Context mContext;

    public TraceDialog(Context context, TraceCatchFragment traceCatchFragment) {
        super(context, R.style.Theme_G73CS);
        this.mContext = context;
        this.mTraceCatchFragment = traceCatchFragment;
        contentView = View.inflate(context, R.layout.dialog_trace, null);
        this.setContentView(contentView);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        //window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(mContext.getResources().getColor(R.color.main_bg_color));
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setBackgroundDrawableResource(R.drawable.gradient_status_bar);
        //StatusBarUtil.setLightStatusBar(window, true);
        WindowManager.LayoutParams lp = window.getAttributes();

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        initView();
    }

    private void initView() {

        TD1 = new LinkedList<>();
        TD2 = new LinkedList<>();
        TD3 = new LinkedList<>();
        TD4 = new LinkedList<>();

        contentView.findViewById(R.id.trace_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyBoard(view);
            }
        });

        final TextView tv_set_last = contentView.findViewById(R.id.tv_set_last);
        tv_set_last.setText(MainActivity.getInstance().isUseDefault ? "使用上次" : "恢复默认");
        if (MainActivity.getInstance().isUseDefault) initDefaultList();
        else resetLastAutoArfcnList();
        tv_set_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getInstance().isUseDefault = !MainActivity.getInstance().isUseDefault;

                if (MainActivity.getInstance().isUseDefault) {
                    mTraceCatchFragment.initAutoArfcnList();
                    clearList();
                    initDefaultList();
                } else resetLastAutoArfcnList();
                TD1_adapter.setDate(TD1);
                TD2_adapter.setDate(TD2);
                TD3_adapter.setDate(TD3);
                TD4_adapter.setDate(TD4);

                tv_set_last.setText(MainActivity.getInstance().isUseDefault ? "使用上次" : "恢复默认");
            }
        });

        TD1_adapter = new DualRecyclerviewAdapter(mContext, TD1, "TD1");
        TD2_adapter = new DualRecyclerviewAdapter(mContext, TD2, "TD2");
        TD3_adapter = new DualRecyclerviewAdapter(mContext, TD3, "TD3");
        TD4_adapter = new DualRecyclerviewAdapter(mContext, TD4, "TD4");
        TD1_adapter.setOnDelArfcnListListener(this);
        TD2_adapter.setOnDelArfcnListListener(this);
        TD3_adapter.setOnDelArfcnListListener(this);
        TD4_adapter.setOnDelArfcnListListener(this);

        RecyclerView rv_arfcn_1 = contentView.findViewById(R.id.rv_arfcn_1);
        rv_arfcn_1.setAdapter(TD1_adapter);
        rv_arfcn_1.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        rv_arfcn_1.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        contentView.findViewById(R.id.btn_add_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData(TD1_adapter, "TD1");
            }
        });

        RecyclerView rv_arfcn_2 = contentView.findViewById(R.id.rv_arfcn_2);
        rv_arfcn_2.setAdapter(TD2_adapter);
        rv_arfcn_2.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        rv_arfcn_2.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        contentView.findViewById(R.id.btn_add_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData(TD2_adapter, "TD2");
            }
        });

        RecyclerView rv_arfcn_3 = contentView.findViewById(R.id.rv_arfcn_3);
        rv_arfcn_3.setAdapter(TD3_adapter);
        rv_arfcn_3.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        rv_arfcn_3.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        contentView.findViewById(R.id.btn_add_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData(TD3_adapter, "TD3");
            }
        });

        RecyclerView rv_arfcn_4 = contentView.findViewById(R.id.rv_arfcn_4);
        rv_arfcn_4.setAdapter(TD4_adapter);
        rv_arfcn_4.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        rv_arfcn_4.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        contentView.findViewById(R.id.btn_add_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData(TD4_adapter, "TD4");
            }
        });

        contentView.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkLastParam()) return;
                if (isChangeArfcn()) MainActivity.getInstance().isUseDefault = false;
                if (listener != null) listener.onTraceConfig();
                hideSoftKeyBoard(view);
                dismiss();
            }
        });

        contentView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyBoard(view);
                dismiss();
            }
        });

        final Switch switch_auto_pci = contentView.findViewById(R.id.switch_auto_pci);
        switch_auto_pci.setChecked(MainActivity.getInstance().isEnablePci);
        TD1_adapter.setEnablePci();
        TD2_adapter.setEnablePci();
        TD3_adapter.setEnablePci();
        TD4_adapter.setEnablePci();
        switch_auto_pci.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getInstance().isEnablePci = switch_auto_pci.isChecked();
                TD1_adapter.setEnablePci();
                TD2_adapter.setEnablePci();
                TD3_adapter.setEnablePci();
                TD4_adapter.setEnablePci();
            }
        });
    }

    private void initDefaultList(){
        TD1.add(new ArfcnPciBean("627264", "1001"));
        TD2.add(new ArfcnPciBean("38950", "501"));
        TD3.add(new ArfcnPciBean("504990", "1002"));
        TD4.add(new ArfcnPciBean("1650", "502"));
    }

    private void clearList(){
        TD1.clear();
        TD2.clear();
        TD3.clear();
        TD4.clear();
    }

    public void resetLastAutoArfcnList(){
        clearList();
        // N28/N78/N79
        String listStr = PrefUtil.build().getValue("Auto_Arfcn_TD1", "-1").toString();
        if (listStr.equals("-1")) TD1.add(new ArfcnPciBean("627264", "1001"));
        else if (!listStr.isEmpty()) {
            String[] strings = listStr.split("_");
            for (String string : strings) {
                AppLog.D("Auto_Arfcn_TD1 = " + string);
                String[] split = string.split("/");
                if (split.length < 1) continue;
                TD1.add(new ArfcnPciBean(split[0], split.length == 1 ? "1001" : string.split("/")[1]));
            }
        }

        // B34/B39/B40/B41
        listStr = PrefUtil.build().getValue("Auto_Arfcn_TD2", "-1").toString();
        if (listStr.equals("-1")) TD2.add(new ArfcnPciBean("38950", "501"));
        else if (!listStr.isEmpty()) {
            String[] strings = listStr.split("_");
            for (String string : strings) {
                AppLog.D("Auto_Arfcn_TD2 = " + string);
                String[] split = string.split("/");
                if (split.length < 1) continue;
                TD2.add(new ArfcnPciBean(split[0], split.length == 1 ? "501" : string.split("/")[1]));
            }
        }

        // N1/N41
        listStr = PrefUtil.build().getValue("Auto_Arfcn_TD3", "-1").toString();
        if (listStr.equals("-1")) TD3.add(new ArfcnPciBean("504990", "1002"));
        else if (!listStr.isEmpty()) {
            String[] strings = listStr.split("_");
            for (String string : strings) {
                AppLog.D("Auto_Arfcn_TD3 = " + string);
                String[] split = string.split("/");
                if (split.length < 1) continue;
                TD3.add(new ArfcnPciBean(split[0], split.length == 1 ? "1002" : string.split("/")[1]));
            }
        }

        // B1/B3/B5/B8
        listStr = PrefUtil.build().getValue("Auto_Arfcn_TD4", "-1").toString();
        if (listStr.equals("-1")) TD4.add(new ArfcnPciBean("1650", "502"));
        else if (!listStr.isEmpty()) {
            String[] strings = listStr.split("_");
            for (String string : strings) {
                AppLog.D("Auto_Arfcn_TD4 = " + string);
                String[] split = string.split("/");
                if (split.length < 1) continue;
                TD4.add(new ArfcnPciBean(split[0], split.length == 1 ? "502" : string.split("/")[1]));
            }
        }

        mTraceCatchFragment.arfcnBeanHashMap.put("TD1", TD1);
        mTraceCatchFragment.arfcnBeanHashMap.put("TD2", TD2);
        mTraceCatchFragment.arfcnBeanHashMap.put("TD3", TD3);
        mTraceCatchFragment.arfcnBeanHashMap.put("TD4", TD4);
    }

    private void addData(DualRecyclerviewAdapter adapter, String key) {
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
        if (MainActivity.getInstance().isEnablePci && pci.isEmpty()) {
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
                if (PaCtl.build().isB97502) {
                    if (band != 1 && band != 28) {
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

    private void hideSoftKeyBoard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private boolean checkLastParam() {
        ArfcnPciBean bean1 = TD1_adapter.getLastViewText();
        if (bean1 != null){
            if (MainActivity.getInstance().isEnablePci && bean1.getPci().isEmpty()) bean1.setPci("50");
            if (!bean1.getArfcn().isEmpty() && !bean1.getPci().isEmpty()){
                if (!checkParam(bean1.getArfcn(), Integer.parseInt(bean1.getPci()), "TD1")) return false;
                if (!TD1_adapter.addData(bean1)){
                    showToast(mContext.getString(R.string.value_exist));
                    return false;
                }
            }else if (TD1.size() > 0) TD1.removeLast();
        }

        ArfcnPciBean bean2 = TD2_adapter.getLastViewText();
        if (bean2 != null){
            if (MainActivity.getInstance().isEnablePci && bean2.getPci().isEmpty()) bean2.setPci("50");
            if (!bean2.getArfcn().isEmpty() && !bean2.getPci().isEmpty()){
                if (!checkParam(bean2.getArfcn(), Integer.parseInt(bean2.getPci()), "TD2")) return false;
                if (!TD2_adapter.addData(bean2)){
                    showToast(mContext.getString(R.string.value_exist));
                    return false;
                }
            }else if (TD2.size() > 0) TD2.removeLast();
        }

        ArfcnPciBean bean3 = TD3_adapter.getLastViewText();
        if (bean3 != null){
            if (MainActivity.getInstance().isEnablePci && bean3.getPci().isEmpty()) bean3.setPci("50");
            if (!bean3.getArfcn().isEmpty() && !bean3.getPci().isEmpty()){
                if (!checkParam(bean3.getArfcn(), Integer.parseInt(bean3.getPci()), "TD3")) return false;
                if (!TD3_adapter.addData(bean3)){
                    showToast(mContext.getString(R.string.value_exist));
                    return false;
                }
            }else if (TD3.size() > 0) TD3.removeLast();
        }

        ArfcnPciBean bean4 = TD4_adapter.getLastViewText();
        if (bean4 != null){
            if (MainActivity.getInstance().isEnablePci && bean4.getPci().isEmpty()) bean4.setPci("50");
            if (!bean4.getArfcn().isEmpty() && !bean4.getPci().isEmpty()){
                if (!checkParam(bean4.getArfcn(), Integer.parseInt(bean4.getPci()), "TD4")) return false;
                if (!TD4_adapter.addData(bean4)){
                    showToast(mContext.getString(R.string.value_exist));
                    return false;
                }
            }else if (TD4.size() > 0) TD4.removeLast();
        }

        if (PaCtl.build().isB97502){
            // 双频点检测
            int td2 = TD2.size();
            int td4 = TD4.size();
            int arfcn_band_2 = 0;
            if (td2 > 0) arfcn_band_2 = LteBand.earfcn2band(Integer.parseInt(TD2.get(0).getArfcn()));

            int arfcn_band_4 = 0;
            if (td4 > 0) arfcn_band_4 = LteBand.earfcn2band(Integer.parseInt(TD4.get(0).getArfcn()));

            if (PaCtl.build().isB97502){
                // 双频点检测
                if (arfcn_band_2 == 40 && (arfcn_band_4 != 40 || td4 > 1)){
                    MainActivity.getInstance().showToast(mContext.getString(R.string.cell3_need_b40));
                    return false;
                }

                if (arfcn_band_4 == 3 && (arfcn_band_2 != 3 || td2 > 1)){
                    MainActivity.getInstance().showToast(mContext.getString(R.string.cell1_need_b3));
                    return false;
                }
            }
        }

        if (TD1.size() == 0 && TD2.size() == 0 && TD3.size() == 0 && TD4.size() == 0){
            MainActivity.getInstance().showToast(mContext.getString(R.string.no_input_data));
            TD1.add(new ArfcnPciBean("", ""));
            TD2.add(new ArfcnPciBean("", ""));
            TD3.add(new ArfcnPciBean("", ""));
            TD4.add(new ArfcnPciBean("", ""));
            return false;
        }
        return true;
    }

    private boolean isChangeArfcn(){

        boolean isChange = false;
        boolean isChange0 = false;
        boolean isChange1 = false;
        boolean isChange2 = false;
        boolean isChange3 = false;

        StringBuilder str = new StringBuilder();

        if (TD1.size() != 1 || !TD1.get(0).getArfcn().equals("627264") || !TD1.get(0).getPci().equals("1001")){
            isChange = true;
            isChange0 = true;
            for (ArfcnPciBean bean : TD1) {
                String info = bean.toString();
                if (info.length() == 1) continue;
                str.append(info);
                str.append("_");
            }

            if (str.length() > 0) str = new StringBuilder(str.substring(0, str.length() - 1));
            AppLog.D("Auto_Arfcn_TD1 = " + str);
            PrefUtil.build().putValue("Auto_Arfcn_TD1", str.toString());

        }

        str = new StringBuilder();
        if (TD2.size() != 1 || !TD2.get(0).getArfcn().equals("38950") || !TD2.get(0).getPci().equals("501")){
            isChange = true;
            isChange1 = true;
            for (ArfcnPciBean bean : TD2) {
                String info = bean.toString();
                if (info.length() == 1) continue;
                str.append(info);
                str.append("_");
            }
            if (str.length() > 0) str = new StringBuilder(str.substring(0, str.length() - 1));
            AppLog.D("Auto_Arfcn_TD2 = " + str);
            PrefUtil.build().putValue("Auto_Arfcn_TD2", str.toString());

        }

        str = new StringBuilder();
        if (TD3.size() != 1 || !TD3.get(0).getArfcn().equals("504990") || !TD3.get(0).getPci().equals("1002")){
            isChange = true;
            isChange2 = true;
            for (ArfcnPciBean bean : TD3) {
                String info = bean.toString();
                if (info.length() == 1) continue;
                str.append(info);
                str.append("_");
            }
            if (str.length() > 0) str = new StringBuilder(str.substring(0, str.length() - 1));
            AppLog.D("Auto_Arfcn_TD3 = " + str);
            PrefUtil.build().putValue("Auto_Arfcn_TD3", str.toString());

        }

        str = new StringBuilder();
        if (TD4.size() != 1 || !TD4.get(0).getArfcn().equals("1650") || !TD4.get(0).getPci().equals("502")){
            isChange = true;
            isChange3 = true;
            for (ArfcnPciBean bean : TD4) {
                String info = bean.toString();
                if (info.length() == 1) continue;
                str.append(info);
                str.append("_");
            }
            if (str.length() > 0) str = new StringBuilder(str.substring(0, str.length() - 1));
            AppLog.D("Auto_Arfcn_TD4 = " + str);
            PrefUtil.build().putValue("Auto_Arfcn_TD4", str.toString());

        }
        if (isChange){
            if (!isChange0){
                TD1.clear();
                TD1.add(new ArfcnPciBean("627264", "1001"));
                PrefUtil.build().putValue("Auto_Arfcn_TD1", "627264/1001");
            }

            if (!isChange1){
                TD2.clear();
                TD2.add(new ArfcnPciBean("38950", "501"));
                PrefUtil.build().putValue("Auto_Arfcn_TD2", "38950/501");
            }

            if (!isChange2){
                TD3.clear();
                TD3.add(new ArfcnPciBean("504990", "1002"));
                PrefUtil.build().putValue("Auto_Arfcn_TD3", "504990/1002");
            }

            if (!isChange3){
                TD4.clear();
                TD4.add(new ArfcnPciBean("1650", "502"));
                PrefUtil.build().putValue("Auto_Arfcn_TD4", "1650/502");
            }
        }
        mTraceCatchFragment.arfcnBeanHashMap.put("TD1", TD1);
        mTraceCatchFragment.arfcnBeanHashMap.put("TD2", TD2);
        mTraceCatchFragment.arfcnBeanHashMap.put("TD3", TD3);
        mTraceCatchFragment.arfcnBeanHashMap.put("TD4", TD4);
        return isChange;
    }

    public void setOnTraceSetListener(OnTraceSetListener listener) {
        this.listener = listener;
    }

    @Override
    public void OnDelArfcn(ArfcnPciBean bean, String band) {
        /*if (arfcn.isEmpty()) return;
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
        }*/
    }
}
