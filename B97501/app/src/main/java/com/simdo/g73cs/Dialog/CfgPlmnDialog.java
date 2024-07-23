package com.simdo.g73cs.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.simdo.g73cs.Adapter.PlmnAdapter;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.View.MyRadioGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CfgPlmnDialog extends Dialog {

    public CfgPlmnDialog(Context context) {
        super(context, R.style.style_dialog);
        this.mContext = context;
        View view = View.inflate(context, R.layout.dialog_cfg_plmn, null);
        this.setContentView(view);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        //window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 9 / 10;// 设置宽度屏幕的 9 / 10
        lp.height = mContext.getResources().getDisplayMetrics().heightPixels * 3 / 4;
        window.setAttributes(lp);

        initView(view);
    }

    private void initView(View view) {
        final String cfg_plmn_mode = PrefUtil.build().getValue("cfg_plmn_mode", mContext.getString(R.string.use_def)).toString();
        MyRadioGroup rb_mode = view.findViewById(R.id.rb_mode);
        if (cfg_plmn_mode.equals(mContext.getString(R.string.use_def))) {
            rb_mode.check(R.id.rb_first);
        } else rb_mode.check(R.id.rb_second);

        rb_mode.setOnCheckedChangeListener(new MyRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(MyRadioGroup group, int checkedId) {
                PrefUtil.build().putValue("cfg_plmn_mode", rb_mode.getCheckedRadioButtonId() == R.id.rb_first ? mContext.getString(R.string.use_def) : mContext.getString(R.string.use_custom));
            }
        });
        String plmns = PrefUtil.build().getValue("cfg_plmn", "").toString();
        list = new ArrayList<>();
        if (!plmns.isEmpty()){
            String[] split = plmns.split(";");
            list.addAll(Arrays.asList(split));
        }

        RecyclerView mListView = view.findViewById(R.id.list);
        mListView.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        mListAdapter = new PlmnAdapter(mContext, list);
        mListView.setAdapter(mListAdapter);
        view.findViewById(R.id.tv_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddCityDialog();
            }
        });
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    /**
     * 创建任务名
     */
    private void showAddCityDialog() {
        createCustomDialog();

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_add_plmn, null);
        final EditText ed_plmn = view.findViewById(R.id.ed_plmn);
        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String plmn = ed_plmn.getEditableText().toString();
                if (plmn.length() == 0 || plmn.length() < 5) {
                    MainActivity.getInstance().showToast(mContext.getResources().getString(R.string.error_plmn));
                    return;
                }
                if (list.size() > 11){
                    MainActivity.getInstance().showToast(mContext.getResources().getString(R.string.max_size_plmn));
                    return;
                }
                if (list.contains(plmn)){
                    MainActivity.getInstance().showToast(mContext.getResources().getString(R.string.value_exist));
                    return;
                }
                list.add(plmn);
                mListAdapter.notifyDataSetChanged();
                String plmns = "";
                for (String item : list){
                    plmns += item;
                    plmns += ";";
                }
                if (plmns.endsWith(";")) plmns = plmns.substring(0, plmns.length() - 1);
                PrefUtil.build().putValue("cfg_plmn", plmns);
                MainActivity.getInstance().showToast(mContext.getResources().getString(R.string.add_ok_next_start_tip));
                closeCustomDialog();
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    /**
     * 显示DIALOG通用接口
     */
    private void createCustomDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }
        mDialog = new Dialog(mContext, R.style.style_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mDialog.setCancelable(false);   // 返回键不消失
    }

    private void closeCustomDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private void showCustomDialog(View view, boolean bottom) {
        mDialog.setContentView(view);
        mDialog.show();
        if (bottom) {
            Window window = mDialog.getWindow();
            window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
            window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        } else {
            Window window = mDialog.getWindow();
            window.setGravity(Gravity.CENTER); //可设置dialog的位置
            WindowManager.LayoutParams lp = window.getAttributes();

            lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 6 / 7;//WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }

    private final Context mContext;
    private PlmnAdapter mListAdapter;
    private Dialog mDialog;
    private List<String> list;

}
