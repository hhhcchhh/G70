package com.simdo.g73cs.Dialog;

import static com.simdo.g73cs.MainActivity.devA;
import static com.simdo.g73cs.MainActivity.devB;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.Adapter.SetArfcnChagerAdapter;
import com.simdo.g73cs.Bean.CheckBoxBean;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.StatusBarUtil;
import com.simdo.g73cs.Util.Util;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class SetArfcnChangeDialog extends Dialog {
    List<CheckBoxBean> check_box_list = new ArrayList<>();
    EditText ed_time_count;
    private int time_count = 60;

    public SetArfcnChangeDialog(Context context) {
        super(context, R.style.Theme_G73CS);
        this.mContext = context;

        contentView = View.inflate(context, R.layout.dialog_set_arfcn, null);
        this.setContentView(contentView);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失

        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        //window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        StatusBarUtil.setLightStatusBar(window, true);
        WindowManager.LayoutParams lp = window.getAttributes();

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        initView();
    }

    private void initView() {
        contentView.findViewById(R.id.back).setOnClickListener(view -> dismiss());
        ed_time_count = contentView.findViewById(R.id.time_count);
        ed_time_count.setText("" + time_count);
        ed_time_count.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(ed_time_count.getText().toString()))
                    time_count = Integer.parseInt("60");
                else
                    time_count = Integer.parseInt(ed_time_count.getText().toString());
                if (listener != null) {
                    listener.onSetTime(time_count);
                }

            }
        });
        readArfcnData();    //获取上次的数据
        RecyclerView list_arfcn = contentView.findViewById(R.id.list_arfcn);
        list_arfcn.setLayoutManager(new LinearLayoutManager(mContext));
        list_arfcn.setAdapter(new SetArfcnChagerAdapter(mContext, check_box_list));    //设置数据到页面
    }

//    private void initArfcnData() {
//        AppLog.I("FreqFragment initArfcnData()");
//        try {
//            if (ZApplication.getInstance().isFirstStartApp) {
//                String string = PrefUtil.build().getValue("isFirstStartApp", "0").toString();
//                int value = Integer.parseInt(string);
//                value++;
//                PrefUtil.build().putValue("isFirstStartApp", String.valueOf(value));
//
//                List<Integer> list = new ArrayList<>();
//                // 5G
//                list.add(427250);
//                list.add(428910);
//                list.add(422890);
//                PrefUtil.build().putValue("N1" + "cfg", Util.int2Json(list, "N1"));
//
//                list.clear();
//                list.add(154810);
//                list.add(152650);
//                PrefUtil.build().putValue("N28" + "cfg", Util.int2Json(list, "N28"));
//
//                list.clear();
//                list.add(504990);
//                list.add(512910);
//                list.add(516990);
//                PrefUtil.build().putValue("N41" + "cfg", Util.int2Json(list, "N41"));
//
//                list.clear();
//                list.add(627264);
//                list.add(633984);
//                PrefUtil.build().putValue("N78" + "cfg", Util.int2Json(list, "N78"));
//
//                list.clear();
//                list.add(723360);
//                PrefUtil.build().putValue("N79" + "cfg", Util.int2Json(list, "N79"));
//
//                // 4G
//                list.clear();
//                list.add(100);
//                list.add(450);
//                PrefUtil.build().putValue("B1" + "cfg", Util.int2Json(list, "B1"));
//
//                list.clear();
//                list.add(1275);
//                list.add(1650);
//                list.add(1825);
//                PrefUtil.build().putValue("B3" + "cfg", Util.int2Json(list, "B3"));
//
//                list.clear();
//                list.add(2452);
//                PrefUtil.build().putValue("B5" + "cfg", Util.int2Json(list, "B5"));
//
//                list.clear();
//                list.add(3683);
//                PrefUtil.build().putValue("B8" + "cfg", Util.int2Json(list, "B8"));
//
//                list.clear();
//                list.add(36275);
//                PrefUtil.build().putValue("B34" + "cfg", Util.int2Json(list, "B34"));
//
//                list.clear();
//                list.add(37900);
//                PrefUtil.build().putValue("B38" + "cfg", Util.int2Json(list, "B38"));
//
//                list.clear();
//                list.add(38400);
//                PrefUtil.build().putValue("B39" + "cfg", Util.int2Json(list, "B39"));
//
//                list.clear();
//                list.add(38950);
//                PrefUtil.build().putValue("B40" + "cfg", Util.int2Json(list, "B40"));
//
//                list.clear();
//                list.add(40340);
//                list.add(40936);
//                PrefUtil.build().putValue("B41" + "cfg", Util.int2Json(list, "B41"));
//            }
//        } catch (JSONException e) {
//            AppLog.E("readArfcnData JSONException e = " + e);
//        }
//    }

    private void readArfcnData() {
        AppLog.I("setArfcnChangeDialoog readArfcnData()");
        if (PaCtl.build().is3G758) {
            if (check_box_list.size() > 0) return;
            check_box_list.add(new CheckBoxBean("N1", 3, devB));
            check_box_list.add(new CheckBoxBean("N28", 3, devB));
            check_box_list.add(new CheckBoxBean("N41", 1, devA));
            check_box_list.add(new CheckBoxBean("N78", 1, devA));
            check_box_list.add(new CheckBoxBean("N79", 1, devA));
            check_box_list.add(new CheckBoxBean("B1", 3, devB));
            check_box_list.add(new CheckBoxBean("B3", 2, devA));
            check_box_list.add(new CheckBoxBean("B5", 2, devA));
            check_box_list.add(new CheckBoxBean("B8", 2, devA));
            check_box_list.add(new CheckBoxBean("B34", 4, devB));
            check_box_list.add(new CheckBoxBean("B39", 4, devB));
            check_box_list.add(new CheckBoxBean("B40", 4, devB));
            check_box_list.add(new CheckBoxBean("B41", 4, devB));
        } else {
            if (check_box_list.size() > 0) return;
            check_box_list.add(new CheckBoxBean("N1", 3, devB));
            check_box_list.add(new CheckBoxBean("N28", 1, devB));
            check_box_list.add(new CheckBoxBean("N41", 3, devA));
            check_box_list.add(new CheckBoxBean("N78", 1, devA));
            check_box_list.add(new CheckBoxBean("N79", 1, devA));
            check_box_list.add(new CheckBoxBean("B1", 4, devB));
            check_box_list.add(new CheckBoxBean("B3", 4, devA));
            check_box_list.add(new CheckBoxBean("B5", 4, devA));
            check_box_list.add(new CheckBoxBean("B8", 4, devA));
            check_box_list.add(new CheckBoxBean("B34", 2, devB));
            check_box_list.add(new CheckBoxBean("B39", 2, devB));
            check_box_list.add(new CheckBoxBean("B40", 2, devB));
            check_box_list.add(new CheckBoxBean("B41", 2, devB));
        }
//		initArfcnData();	//不初始化数据
        try {
            //获取本地的数据
            for (int i = 0; i < check_box_list.size(); i++) {
                String band = check_box_list.get(i).getText();
                AppLog.D("readArfcnData band = " + band);
                if (band.isEmpty()) continue;
                String value = PrefUtil.build().getValue(band + "cfg", "").toString();  //文件名
                check_box_list.get(i).addAllArfcnList(Util.json2Int(value, band));  //json的key
                AppLog.I("readArfcnData band = " + band + ", value = " + value);
            }
        } catch (JSONException e) {
            AppLog.E("readArfcnData JSONException e = " + e);
        }
    }

    public void cleanAllPrefArfcnList() {
        for (int i = 0; i < check_box_list.size(); i++) {
            String band = check_box_list.get(i).getText();
            PrefUtil.build().putValue(band + "cfg", "");
            AppLog.D("cleanAllPrefArfcnList");
        }
    }

    public int getTime_count() {
        return time_count;
    }

    public void setTime_count(int time_count) {
        AppLog.I("time_count == " + this.time_count);
        this.time_count = time_count;
        ed_time_count.setText("" + time_count);
    }

    private onSetArfcnChangeListener listener;

    public void setOnSetArfcnChangeListener(onSetArfcnChangeListener listener) {
        this.listener = listener;
    }

    public void removeOnSetArfcnChangeListener() {
        this.listener = null;
    }

    public interface onSetArfcnChangeListener {
        void onSetTime(int time);
    }

    private final View contentView;
    private final Context mContext;

}
