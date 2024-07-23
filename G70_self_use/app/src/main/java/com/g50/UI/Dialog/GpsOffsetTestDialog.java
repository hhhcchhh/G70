package com.g50.UI.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Logcat.APPLog;
import com.Util.DataUtil;
import com.Util.Util;
import com.g50.Bean.PaCtl;
import com.g50.G50Activity;
import com.g50.R;
import com.g50.UI.Bean.ArfcnTimingOffset;
import com.g50.UI.Bean.CityBean;
import com.g50.UI.Bean.GnbCity;
import com.nr70.Arfcn.Bean.NrBand;
import com.nr70.Socket.MessageControl.MessageController;

import java.util.ArrayList;
import java.util.List;

public class GpsOffsetTestDialog extends Dialog implements OnClickListener {

	public GpsOffsetTestDialog(Context context) {
    	 super(context, R.style.style_dialog);
        this.mContext = context;
        contentView = (LinearLayout) View.inflate(context, R.layout.dialog_gps_offset, null);
        this.setContentView(contentView);
		this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失 
		//this.setCancelable(false);   // 返回键不消失
		//设置dialog大小，这里是一个小赠送，模块好的控件大小设置
		Window window = this.getWindow();
		window.setGravity(Gravity.CENTER); //可设置dialog的位置
		window.getDecorView().setPadding(0, 0, 0, 0); //消除边距  
		WindowManager.LayoutParams lp = window.getAttributes();
//		lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 6 / 7;
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
		lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		window.setAttributes(lp);

        initView();
    }
	
    private void initView() {
		contentView.findViewById(R.id.btn_ok).setOnClickListener(this);
		contentView.findViewById(R.id.btn_cancel).setOnClickListener(this);

		ed_arfcn = (EditText) contentView.findViewById(R.id.ed_arfcn);
		ed_pa = (EditText) contentView.findViewById(R.id.ed_pa);
		ed_pk = (EditText) contentView.findViewById(R.id.ed_pk);
		tv_offset = (TextView) contentView.findViewById(R.id.tv_offset);
//		ed_arfcn.setEnabled(false);
		btn_import = (Button) contentView.findViewById(R.id.btn_import);
		btn_import.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_ok:
				startTest();
				break;

			case R.id.btn_import:
				addToCurCity();
				break;

			case R.id.btn_cancel:
				dismiss();
				break;
		}
	}

	private void startTest() {
		final String arfcn = ed_arfcn.getEditableText().toString();
		final String pk = ed_pk.getEditableText().toString();
		final String pa = ed_pa.getEditableText().toString();
		if (arfcn.length() == 0) {
			Util.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.error_null));
			return;
		}
//		if (pk.length() == 0) {
//			Util.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.error_null));
//			return;
//		}
//		if (pa.length() == 0) {
//			Util.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.error_null));
//			return;
//		}
		tv_offset.setText("测试中");
		PaCtl.build().openPA(arfcn, G50Activity.PA_Type);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// startTdMeasure(int cell_id, int swap_rf, int arfcn, int pk, int pa)
				int swap_rf = 0;
				int band = NrBand.earfcn2band(Integer.parseInt(arfcn));
				if (band == 78 || band == 28 || band == 79) {
					swap_rf = 1;
				}
				MessageController.build().startTdMeasure(0, swap_rf, Integer.parseInt(arfcn), 0, 0);
			}
		}, 200);
	}

	public void refreshView(int time_offset) {
		int offset = parseTimingOffset(time_offset);
		if (offset == -1) {
			tv_offset.setText("未检测，再来一次");
		} else {
			int is = 3000000 - gpsTimingOffset;
			String s =  is + " (src:" + gpsTimingOffset + ")";
			gpsTimingOffset = is;
			tv_offset.setText(s);
		}
		PaCtl.build().closePA();
	}
	private void addToCurCity() {
		APPLog.D("gpsTimingOffset = " + gpsTimingOffset);
		if (gpsTimingOffset < 0) {
			Util.showToast(mContext.getApplicationContext(), "配置失败：无效时偏参数");
			return;
		}
		CityBean city = GnbCity.build().getCurCity();
		if (city == null) {
			Util.showToast(mContext.getApplicationContext(), "城市列表为空, 请在“时偏配置(GPS)”菜单手动添加");
			return;
		}
		String arfcn = ed_arfcn.getEditableText().toString().trim();
		if (TextUtils.isEmpty(arfcn) || !DataUtil.isNumeric(arfcn)) {
			Util.showToast(mContext.getApplicationContext(), "频点数据有误，请确认");
			return;
		}
		List<ArfcnTimingOffset> alist = city.getArfcnList();
		if (alist == null) {
			alist = new ArrayList<ArfcnTimingOffset>();
			alist.add(new ArfcnTimingOffset(arfcn, gpsTimingOffset));
			Util.showToast(mContext.getApplicationContext().getApplicationContext(), "时偏参数已添加");
		} else {
			boolean add = true;
			if (alist.size() > 0){
				alist.get(0).setTimingOffset(gpsTimingOffset);
				Util.showToast(mContext.getApplicationContext(), "时偏参数已更新");
				add = false;
			}
			/*for (int i = 0; i < alist.size(); i++) {
				if (alist.get(i).getArfcn() != null && alist.get(i).getArfcn().equals(arfcn)) {
					alist.get(i).setTimingOffset(gpsTimingOffset);
					Util.showToast(mContext.getApplicationContext(), "时偏参数已更新");
					add = false;
					break;
				}
			}*/
			if (add) {
				alist.add(new ArfcnTimingOffset(arfcn, gpsTimingOffset));
				Util.showToast(mContext.getApplicationContext(), "时偏参数已添加");
			}
		}
		GnbCity.build().save();
	}
	private int parseTimingOffset(int time_offset) {
		APPLog.D("parseTimingOffset() L: " + time_offset);
		if (time_offset < 0) {
			return time_offset;
		}
		// 3000000 2330000 2303000
		int t1 = 1000; // 千级别及以下数据不要，归0
		int m = time_offset % t1;
		time_offset = time_offset - m;
		return availableData(time_offset);
	}

	public int availableData(int offset) {
		// 3000000 2330000 2303000
		gpsTimingOffset = offset;
		if (offset > 2900000) {
			gpsTimingOffset = 3000000;
		} else if (offset > 2328000 && offset < 2340000) {
			gpsTimingOffset = 2330000;
		} else if (offset > 2290000 && offset < 2310000) {
			gpsTimingOffset = 2303000;
		}
		//gpsTimingOffset = 3000000 - gpsTimingOffset;
		APPLog.D("availableData() gpsTimingOffset: " + gpsTimingOffset);
		return gpsTimingOffset;
	}

	private LinearLayout contentView;
	private Context mContext;
	private EditText ed_arfcn, ed_pa, ed_pk;
	private TextView tv_offset;
	private Button btn_import;
	private int gpsTimingOffset;
	// 当计时器延时用
	private Handler handler = new Handler();

}
