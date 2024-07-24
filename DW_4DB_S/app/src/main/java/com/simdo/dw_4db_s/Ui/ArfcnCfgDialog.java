package com.simdo.dw_4db_s.Ui;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dwdbsdk.Bean.DB.DBSupportArfcn;

import com.simdo.dw_4db_s.R;
import com.simdo.dw_4db_s.Util.Util;

public class ArfcnCfgDialog extends Dialog implements OnClickListener {

    public ArfcnCfgDialog(Context context, LayoutInflater inflater) {
        super(context, R.style.style_dialog);
        this.context = context;
        this.mInflater = inflater;

        contentView = (LinearLayout) View.inflate(context, R.layout.dialog_arfcn_cfg, null);
        this.setContentView(contentView);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        //window.setGravity(Gravity.CENTER); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 0.8);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        //lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        initView();
    }

    private void initView() {
        ed_arfcn = (EditText) contentView.findViewById(R.id.ed_arfcn);
        ed_k1 = (EditText) contentView.findViewById(R.id.ed_k1);
        ed_k2 = (EditText) contentView.findViewById(R.id.ed_k2);
        ed_time_offset = (EditText) contentView.findViewById(R.id.ed_time_offset);
        contentView.findViewById(R.id.btn_add).setOnClickListener(this);
        contentView.findViewById(R.id.btn_delete).setOnClickListener(this);
        contentView.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int iarfcn;
        String arfcn;
        switch (v.getId()) {
            case R.id.btn_add:
                arfcn = ed_arfcn.getEditableText().toString().trim();
                String k1 = ed_k1.getEditableText().toString().trim();
                String k2 = ed_k2.getEditableText().toString().trim();
                String time_offset = ed_time_offset.getEditableText().toString().trim();
                if (arfcn.length() <= 0) {
                    Util.showToast(context, context.getResources().getString(R.string.error_arfcn));
                    return;
                }
                if (k1.length() <= 0) {
                    Util.showToast(context, "k1" + context.getResources().getString(R.string.error_empty));
                    return;
                }
                if (k2.length() <= 0) {
                    Util.showToast(context, "k2" + context.getResources().getString(R.string.error_empty));
                    return;
                }
                if (time_offset.length() <= 0) {
                    Util.showToast(context, context.getResources().getString(R.string.error_timie_offset));
                    return;
                }
                iarfcn = Integer.valueOf(arfcn);
                boolean add = DBSupportArfcn.build().add(iarfcn, Integer.valueOf(k1), Integer.valueOf(k2), Integer.valueOf(time_offset));
                if (add) {
                    String text = String.format(context.getResources().getString(R.string.arfcn_add_ok), iarfcn);
//					GnbCity.build().addArfcnToCurCity(iarfcn, Integer.valueOf(time_offset));
                    Util.showToast(context, text);
                } else {
                    Util.showToast(context, context.getText(R.string.error_same_arfcn).toString());
                }
                dismiss();
                break;

            case R.id.btn_delete:
                arfcn = ed_arfcn.getEditableText().toString().trim();
                if (arfcn.length() <= 0) {
                    Util.showToast(context, context.getResources().getString(R.string.error_arfcn));
                    return;
                }
                showDeleteConfirmDialog(Integer.valueOf(arfcn));
                break;

            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }

    private void showDeleteConfirmDialog(final int arfcn) {
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_confirm, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        tv_msg.setText("确定删除频点: " + arfcn);
        tv_title.setText(context.getResources().getString(R.string.warning));
        final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean delete = DBSupportArfcn.build().delete(arfcn);
                if (delete) {
                    String text = String.format(context.getResources().getString(R.string.arfcn_delete_ok), arfcn);
//					GnbCity.build().deleteCurCityArfcn(arfcn);
                    Util.showToast(context, text);
                } else {
                    String text = String.format(context.getResources().getString(R.string.arfcn_delete_fail), arfcn);
                    Util.showToast(context, text);
                }
                closeCustomDialog();
                dismiss();
            }
        });

        final Button btn_canel = (Button) view.findViewById(R.id.btn_cancel);
        btn_canel.setOnClickListener(new View.OnClickListener() {
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
        mDialog = new Dialog(context, R.style.style_dialog);
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

            lp.width = context.getResources().getDisplayMetrics().widthPixels * 6 / 7;//WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }

    private Dialog mDialog;
    private LinearLayout contentView;
    private EditText ed_arfcn, ed_time_offset, ed_k1, ed_k2;
    private Context context;
    private LayoutInflater mInflater;
}
