package com.simdo.dw_db_s.Bean;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.simdo.dw_db_s.R;


public class DialogBean {
   private Context mContext;
   private Dialog mDialog;

   public DialogBean(Context context, Dialog dialog) {
      this.mContext = context;
      this.mDialog = dialog;
   }

   /**
    * 显示DIALOG通用接口
    */
   public void createCustomDialog() {
      if (mDialog != null && mDialog.isShowing()) {
         return;
      }
      mDialog = new Dialog(mContext, R.style.style_dialog);
      mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
      mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
      mDialog.setCancelable(true);   // 返回键不消失
   }

   public void closeCustomDialog() {
      if (mDialog != null) {
         mDialog.dismiss();
         mDialog = null;
      }
   }

   public void showCustomDialog(View view, boolean bottom) {
      mDialog.setContentView(view);
      if (bottom) {
         Window window = mDialog.getWindow();
         window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
         window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
         WindowManager.LayoutParams lp = window.getAttributes();
         lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
         lp.height = WindowManager.LayoutParams.MATCH_PARENT;
         window.setAttributes(lp);
      } else {
         Window window = mDialog.getWindow();
         window.setGravity(Gravity.CENTER); //可设置dialog的位置
         WindowManager.LayoutParams lp = window.getAttributes();

         lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 6 / 7;//WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
         lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
         window.setAttributes(lp);
      }
      mDialog.show();
   }

}
