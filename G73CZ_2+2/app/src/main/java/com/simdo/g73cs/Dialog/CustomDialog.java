package com.simdo.g73cs.Dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import com.simdo.g73cs.R;

public class CustomDialog extends AlertDialog {

    private String content;

    public CustomDialog(Context context, String content) {
        super(context);
        this.content = content;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog_layout); // 创建一个自定义的布局文件

        TextView textView = findViewById(R.id.textView); // 获取TextView视图
        textView.setText(content); // 设置TextView的文本内容

        setButton(DialogInterface.BUTTON_POSITIVE, "确认", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 在这里你可以执行用户确认后的操作
                dismiss(); // 关闭对话框
            }
        });

        setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss(); // 关闭对话框
            }
        });
    }
}
