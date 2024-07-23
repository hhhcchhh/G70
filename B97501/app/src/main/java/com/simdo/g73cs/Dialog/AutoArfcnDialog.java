package com.simdo.g73cs.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.simdo.g73cs.Adapter.FreqArfcnListCfgAdapter;
import com.simdo.g73cs.Adapter.MyRecyclerviewAdapter;
import com.simdo.g73cs.Bean.ArfcnBean;
import com.simdo.g73cs.Bean.FreqArfcnBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DividerItemDecoration;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.StatusBarUtil;
import com.simdo.g73cs.Util.Util;

import org.json.JSONException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class AutoArfcnDialog extends Dialog {
    FreqArfcnListCfgAdapter adapter;
    List<FreqArfcnBean> list;

    public AutoArfcnDialog(Context context) {
        super(context, R.style.Theme_G73CS);
        this.mContext = context;
        View contentView = View.inflate(context, R.layout.dialog_auto_arfcn, null);
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
        initData();
        initView(contentView);
    }

    private void initData() {
        list = new ArrayList<>();

        // N41 504990、 512910、 516990、 507150、 525630
        List<ArfcnBean> freq_n41 = PrefUtil.build().getFreqArfcnList("freq_N41");
        list.add(new FreqArfcnBean("N41", freq_n41));

        // N1 427250、422890、 428910、 426030
        List<ArfcnBean> freq_n1 = PrefUtil.build().getFreqArfcnList("freq_N1");
        list.add(new FreqArfcnBean("N1", freq_n1));

        // N78 627264、 633984
        List<ArfcnBean> freq_n78 = PrefUtil.build().getFreqArfcnList("freq_N78");
        list.add(new FreqArfcnBean("N78", freq_n78));

        // N28 154810、 152650、 152890、 156970、 154570、 156490、 155770
        List<ArfcnBean> freq_n28 = PrefUtil.build().getFreqArfcnList("freq_N28");
        list.add(new FreqArfcnBean("N28", freq_n28));

        // N79 723360
        List<ArfcnBean> freq_n79 = PrefUtil.build().getFreqArfcnList("freq_N79");
        list.add(new FreqArfcnBean("N79", freq_n79));

        // B3 1300、 1275、 1650、 1506、 1500、 1531、 1524、 1850
        List<ArfcnBean> freq_b3 = PrefUtil.build().getFreqArfcnList("freq_B3");
        list.add(new FreqArfcnBean("B3", freq_b3));

        // 4G 常见频点
        // B1  350、 375、 400、 450、 500、 100
        List<ArfcnBean> freq_b1 = PrefUtil.build().getFreqArfcnList("freq_B1");
        list.add(new FreqArfcnBean("B1", freq_b1));

        // B5 2452
        List<ArfcnBean> freq_b5 = PrefUtil.build().getFreqArfcnList("freq_B5");
        list.add(new FreqArfcnBean("B5", freq_b5));

        // B8 3682、 3683、 3641、 3621、 3590、 3725、 3768、 3769、 3770、 3775
        List<ArfcnBean> freq_b8 = PrefUtil.build().getFreqArfcnList("freq_B8");
        list.add(new FreqArfcnBean("B8", freq_b8));

        // B40 38950、 39148、 39292、 38750
        List<ArfcnBean> freq_b40 = PrefUtil.build().getFreqArfcnList("freq_B40");
        list.add(new FreqArfcnBean("B40", freq_b40));

        // B34 36275
        List<ArfcnBean> freq_b34 = PrefUtil.build().getFreqArfcnList("freq_B34");
        list.add(new FreqArfcnBean("B34", freq_b34));

        // B39 38400、 38544
        List<ArfcnBean> freq_b39 = PrefUtil.build().getFreqArfcnList("freq_B39");
        list.add(new FreqArfcnBean("B39", freq_b39));

        // B41 40936、 40340
        List<ArfcnBean> freq_b41 = PrefUtil.build().getFreqArfcnList("freq_B41");
        list.add(new FreqArfcnBean("B41", freq_b41));

        adapter = new FreqArfcnListCfgAdapter(mContext, list);
    }

    private void initView(View contentView) {

        contentView.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        contentView.findViewById(R.id.iv_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.getInstance().createCustomDialog(false);

                View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_add_arfcn, null);
                final EditText ed_arfcn = view.findViewById(R.id.ed_arfcn);
                final TextView as_band = view.findViewById(R.id.as_band);
                ed_arfcn.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        while (s.toString().startsWith("0")) s.delete(0, 1);
                        String string = s.toString();
                        String band_str = "null";
                        if (string.length() > 5) {
                            int band1 = NrBand.earfcn2band(Integer.parseInt(string));
                            if (band1 != 0) band_str = "N" + band1;
                        } else if (string.length() > 2) {
                            int band1 = LteBand.earfcn2band(Integer.parseInt(string));
                            if (band1 != 0) band_str = "B" + band1;
                        }
                        as_band.setText(MessageFormat.format("{0}{1}", mContext.getString(R.string.as_band), band_str));
                    }
                });
                view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = ed_arfcn.getEditableText().toString().trim();
                        if (text.isEmpty()){
                            MainActivity.getInstance().showToast(mContext.getString(R.string.arfcn_empty_err));
                            return;
                        }else {
                            int arfcn = Integer.parseInt(text);
                            String newBand = as_band.getText().toString().split("：")[1];
                            if (newBand.contains("null")) {
                                MainActivity.getInstance().showToast(mContext.getString(R.string.no_as_band));
                                return;
                            }
                            for (int i = 0; i < list.size(); i++) {
                                if (list.get(i).getBand().equals(newBand)){
                                    for (ArfcnBean arfcnBean : list.get(i).getArfcnList()) {
                                        if (arfcnBean.getArfcn() == arfcn){
                                            MainActivity.getInstance().showToast(mContext.getString(R.string.value_exist));
                                            return;
                                        }
                                    }

                                    list.get(i).getArfcnList().add(new ArfcnBean(arfcn, true));
                                    adapter.notifyItemChanged(i);
                                    PrefUtil.build().putFreqArfcnList("freq_" + list.get(i).getBand(), list.get(i).getArfcnList());

                                    MainActivity.getInstance().showToast(mContext.getString(R.string.updated));
                                    break;
                                }
                            }
                        }

                        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

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
        });

        RecyclerView list_arfcn = contentView.findViewById(R.id.list_arfcn);
        //list_arfcn.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        list_arfcn.setLayoutManager(new LinearLayoutManager(mContext));
        //list_arfcn.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        list_arfcn.setAdapter(adapter);
    }
    private final Context mContext;

}
