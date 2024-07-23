package com.simdo.g73cs.Dialog;

import static com.simdo.g73cs.MainActivity.device;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.nr.FTP.FTPUtil;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.PaBean;
import com.nr.Gnb.Response.GnbFreqScanGetDocumentRsp;
import com.nr.Gnb.Response.GnbFreqScanRsp;
import com.nr.Gnb.Response.GnbStateRsp;
import com.nr.Socket.ConnectProtocol;
import com.nr.Socket.MessageControl.MessageController;
import com.simdo.g73cs.Adapter.CheckBoxRecyclerviewAdapter;
import com.simdo.g73cs.Adapter.FreqScanListAdapter;
import com.simdo.g73cs.Bean.CheckBoxBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.File.FileProtocol;
import com.simdo.g73cs.Fragment.TraceCatchFragment;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.View.ConnectProgressBar;
import com.simdo.g73cs.ZApplication;

import org.json.JSONException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FreqDialog extends Dialog {
	private final LinkedList<ScanArfcnBean> scanArfcnBeanList;
	private boolean isGpsScan = false;
	private boolean isStopScan = true;
	private boolean isBandScan = false;
	private int report_level = 0;
	private ProgressDialog progressDialog;
	private final Context mContext;
	
	public FreqDialog(Context context, LinkedList<ScanArfcnBean> list) {
		super(context, R.style.Theme_G73CS);
        this.mContext = context;
        this.scanArfcnBeanList = list;
        View view = View.inflate(context, R.layout.pager_freq, null);
		this.setContentView(view);
		this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
		//this.setCancelable(false);   // 返回键不消失
		//设置dialog大小，这里是一个小赠送，模块好的控件大小设置
		Window window = this.getWindow();
		//window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
		window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
		window.setNavigationBarColor(Color.parseColor("#2A72FF"));
		window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//StatusBarUtil.setLightStatusBar(window, true);
		WindowManager.LayoutParams lp = window.getAttributes();

		lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
		lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		window.setAttributes(lp);

		readArfcnData();
		initView(view);
    }

	@Override
	public void onBackPressed() {
		if (!isStopScan){
			clickStopFreqScan(true);
			return;
		}
		dismiss();
	}

	private final List<CheckBoxBean> check_box_list = new ArrayList<>();
	private TextView tv_start_scan, tv_freq_state;
	private ImageView iv_anim_freq;
	private FreqScanListAdapter adapter;
	private CheckBoxRecyclerviewAdapter checkBoxAdapter;
	private CheckBox cb_all;
	private TextView tv_freq_title;
	private void initView(View root) {
		tv_freq_title = root.findViewById(R.id.tv_freq_title);
		tv_freq_title.setText(mContext.getString(R.string.arfcn_real));
		RecyclerView list_check_box_nr = root.findViewById(R.id.list_check_box);
		list_check_box_nr.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
		checkBoxAdapter = new CheckBoxRecyclerviewAdapter(mContext, check_box_list, new ListItemListener() {
			@Override
			public void onItemClickListener(int position) {
				boolean isAllChecked = true;
				for (CheckBoxBean bean : check_box_list) {
					if (bean.isChecked()) continue;
					isAllChecked = false;
					break;
				}
				cb_all.setChecked(isAllChecked);
			}
		});
		list_check_box_nr.setAdapter(checkBoxAdapter);

		cb_all = root.findViewById(R.id.cb_all);
		cb_all.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				checkBoxAdapter.setChecked(cb_all.isChecked());
			}
		});
		
		root.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isStopScan){
					clickStopFreqScan(true);
					return;
				}
                dismiss();
			}
		});

		root.findViewById(R.id.freq_setting).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isStopScan){
					MainActivity.getInstance().showToast(mContext.getString(R.string.in_freq_tip));
					return;
				}
				showFreqSettingDialog(v);
			}
		});

		root.findViewById(R.id.iv_cfg_arfcn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                /*if (!isStopScan){
                    MainActivity.getInstance().showToast("扫频中，无法进入设置");
                    return;
                }*/
				showCfgArfcnDialog();
			}
		});
		root.findViewById(R.id.tv_import_to_trace).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (scanArfcnBeanList == null || scanArfcnBeanList.size() <= 0){
					MainActivity.getInstance().showToast(mContext.getString(R.string.no_freq_data_tip));
					return;
				}
				for (ScanArfcnBean bean : scanArfcnBeanList) {
					int arfcn = bean.getDl_arfcn();
					int rsrp = bean.getRsrp();
					int pci = 0;
					if (arfcn > 100000) {
						/*
						 * 5G扫频导入 算法
						 *
						 * 5G扫频，扫出一个或多个相同频点，pci选择逻辑
						 * （以下文字说明 rsrp大小比 不考虑 -负号，代码大小比需考虑）
						 * 1、 rsrp 若存在 > 95 ,可直接选择，且选择rsrp值最大的
						 * 2、 rsrp 均 < 95 ,需选择rsrp值最小的 % 3 取余然后做 +- 1 运算
						 *   2、的延伸：若 +- 1运算后的pci，也在这几个相同频点中，则 使用余数+1000
						 * */
						ArrayList<ScanArfcnBean> list = new ArrayList<>();
						for (ScanArfcnBean scanArfcnBean : scanArfcnBeanList)
							if (scanArfcnBean.getDl_arfcn() == arfcn) list.add(scanArfcnBean);
						if (list.size() == 1) {
							// 仅扫出一个
						}else if (list.size() > 1){
							// 扫出多个相同频点
							for (ScanArfcnBean scanArfcnBean : list) {
								int rsrp1 = scanArfcnBean.getRsrp();
								if (rsrp1 < rsrp){
									if (rsrp1 < -95){
										rsrp = rsrp1;
										pci = scanArfcnBean.getPci();
										bean = scanArfcnBean;
									}
								}else {
									if (pci == 0) { // 说明暂时没发现符合说明中 1 逻辑的数据，那就先走 2 逻辑，把最强的rsrp赋值到当前
										rsrp = rsrp1;
										bean = scanArfcnBean;
									}
								}
							}
						}

						// 遍历完之后，判断2逻辑是否需要走延伸逻辑
						if (pci == 0){
							pci = bean.getPci();
							if (pci % 3 == 2) pci -= 1;
							else pci += 1;
							for (ScanArfcnBean scanArfcnBean : list) {
								if (scanArfcnBean.getPci() == pci){
									pci = bean.getPci() % 3 + 1000;
									break;
								}
							}
						}
					}
					//mTraceCatchFragment.importArfcn(bean, pci);
				}
				MainActivity.getInstance().showToast(mContext.getString(R.string.import_arfcn_success));
			}
		});

		iv_anim_freq = root.findViewById(R.id.iv_anim_freq);
		iv_anim_freq.setVisibility(View.GONE);
		tv_freq_state = root.findViewById(R.id.tv_freq_state);

		tv_start_scan = root.findViewById(R.id.tv_start_scan);
		tv_start_scan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (tv_start_scan.getText().toString().equals(mContext.getString(R.string.start_freq))) clickStartFreqScan();
				else clickStopFreqScan(false);
			}
		});

		RecyclerView freq_scan_list = root.findViewById(R.id.freq_scan_list);
		adapter = new FreqScanListAdapter(mContext, scanArfcnBeanList);
		freq_scan_list.setLayoutManager(new LinearLayoutManager(mContext));
		freq_scan_list.setAdapter(adapter);
	}

	private void showFreqSettingDialog(View v) {

		View view = LayoutInflater.from(mContext).inflate(R.layout.popup_freq_setting_menu, null);
		final PopupWindow popupWindow = new PopupWindow(view, Util.dp2px(mContext, 16 * 9), Util.dp2px(mContext, 40 * 4), true);
		view.findViewById(R.id.tv_arfcn_real).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				isBandScan = false;
				report_level = 0;
				tv_freq_title.setText(mContext.getString(R.string.arfcn_real));
				popupWindow.dismiss();
			}
		});
		view.findViewById(R.id.tv_arfcn_file).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				isBandScan = false;
				report_level = 1;
				tv_freq_title.setText(mContext.getString(R.string.arfcn_file));
				popupWindow.dismiss();
			}
		});

		view.findViewById(R.id.tv_band_real).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				isBandScan = true;
				report_level = 0;
				tv_freq_title.setText(mContext.getString(R.string.band_real));
				popupWindow.dismiss();
			}
		});
		view.findViewById(R.id.tv_band_file).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				isBandScan = true;
				report_level = 1;
				tv_freq_title.setText(mContext.getString(R.string.band_file));
				popupWindow.dismiss();
			}
		});

		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);

		Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.bg_popup);
		popupWindow.setBackgroundDrawable(drawable);

		popupWindow.showAsDropDown(v, 0, 10);

	}

	private void readArfcnData() {
		AppLog.I("FreqFragment readArfcnData()");
		if (PaCtl.build().isB97502){
			check_box_list.add(new CheckBoxBean("N41", 1, ""));
			check_box_list.add(new CheckBoxBean("N78", 1, ""));
			check_box_list.add(new CheckBoxBean("B3", 2, ""));
			check_box_list.add(new CheckBoxBean("B1", 3, ""));
			//check_box_list.add(new CheckBoxBean("B7", 3, ""));
			check_box_list.add(new CheckBoxBean("B40", 4, ""));
			check_box_list.add(new CheckBoxBean("B39", 4, ""));
			check_box_list.add(new CheckBoxBean("B41", 4, ""));
			check_box_list.add(new CheckBoxBean("N1", 3, ""));
			check_box_list.add(new CheckBoxBean("N28", 3, ""));
			check_box_list.add(new CheckBoxBean("N79", 1, ""));
			check_box_list.add(new CheckBoxBean("B8", 2, ""));
			check_box_list.add(new CheckBoxBean("B5", 2, ""));
			check_box_list.add(new CheckBoxBean("B34", 4, ""));
		}else {
			check_box_list.add(new CheckBoxBean("N41", 3, ""));
			check_box_list.add(new CheckBoxBean("N78", 1, ""));
			check_box_list.add(new CheckBoxBean("B3", 4, ""));
			check_box_list.add(new CheckBoxBean("B1", 4, ""));
			check_box_list.add(new CheckBoxBean("B40", 2, ""));
			check_box_list.add(new CheckBoxBean("B39", 2, ""));
			check_box_list.add(new CheckBoxBean("B41", 2, ""));
			check_box_list.add(new CheckBoxBean("N1", 3, ""));
			check_box_list.add(new CheckBoxBean("N28", 1, ""));
			check_box_list.add(new CheckBoxBean("N79", 1, ""));
			check_box_list.add(new CheckBoxBean("B5", 4, ""));
			check_box_list.add(new CheckBoxBean("B8", 4, ""));
			check_box_list.add(new CheckBoxBean("B34", 2, ""));
		}

		initArfcnData();
		try {
			for (int i = 0; i < check_box_list.size(); i++) {
				String band = check_box_list.get(i).getText();
				String value = PrefUtil.build().getValue(band, "").toString();
				check_box_list.get(i).addAllArfcnList(Util.json2Int(value, band));
				AppLog.D("readArfcnData band = " + band + ", value = " + value);
			}
		} catch (JSONException e) {
			AppLog.E("readArfcnData JSONException e = " + e);
		}
	}

	private void initArfcnData() {
		AppLog.I("FreqFragment initArfcnData()");
		try {
			if (ZApplication.getInstance().isFirstStartApp){
				String string = PrefUtil.build().getValue("isFirstStartApp", "0").toString();
				int value = Integer.parseInt(string);
				value++;
				PrefUtil.build().putValue("isFirstStartApp", String.valueOf(value));

				List<Integer> list = new ArrayList<>();
				// 5G
				list.add(427250);
				list.add(428910);
				list.add(422890);
				list.add(426030);
				list.add(427210);
				list.add(426750);
				list.add(422930);
				PrefUtil.build().putValue("N1", Util.int2Json(list, "N1"));

				list.clear();
				list.add(154810);
				list.add(152650);
				list.add(152890);
				list.add(156970);
				list.add(154570);
				list.add(156490);
				list.add(155770);
				PrefUtil.build().putValue("N28", Util.int2Json(list, "N28"));

				list.clear();
				list.add(504990);
				list.add(512910);
				list.add(516990);
				list.add(507150);
				list.add(525630);
				PrefUtil.build().putValue("N41", Util.int2Json(list, "N41"));

				list.clear();
				list.add(627264);
				list.add(633984);
				PrefUtil.build().putValue("N78", Util.int2Json(list, "N78"));

				list.clear();
				list.add(723360);
				PrefUtil.build().putValue("N79", Util.int2Json(list, "N79"));

				// 4G
				list.clear();
				list.add(100);
				list.add(300);
				list.add(50);
				list.add(350);
				list.add(375);
				list.add(400);
				list.add(450);
				list.add(500);
				PrefUtil.build().putValue("B1", Util.int2Json(list, "B1"));

				list.clear();
				list.add(1275);
				list.add(1300);
				list.add(1350);
				list.add(1506);
				list.add(1650);
				list.add(1500);
				list.add(1531);
				list.add(1524);
				list.add(1825);
				list.add(1600);
				list.add(1800);
				list.add(1850);
				PrefUtil.build().putValue("B3", Util.int2Json(list, "B3"));

				list.clear();
				list.add(2452);
				PrefUtil.build().putValue("B5", Util.int2Json(list, "B5"));

				/*list.clear();
				list.add(3350);
				list.add(3375);
				PrefUtil.build().putValue("B7", Util.int2Json(list, "B7"));*/

				list.clear();
				list.add(3590);
				list.add(3682);
				list.add(3683);
				list.add(3641);
				list.add(3621);
				list.add(3725);
				list.add(3768);
				list.add(3769);
				list.add(3770);
				list.add(3775);
				list.add(3745);
				list.add(3710);
				list.add(3737);
				list.add(3741);
				PrefUtil.build().putValue("B8", Util.int2Json(list, "B8"));

				list.clear();
				list.add(36275);
				PrefUtil.build().putValue("B34", Util.int2Json(list, "B34"));

				list.clear();
				list.add(38400);
				list.add(38544);
				PrefUtil.build().putValue("B39", Util.int2Json(list, "B39"));

				list.clear();
				list.add(38950);
				list.add(39148);
				list.add(39292);
				list.add(38750);
				PrefUtil.build().putValue("B40", Util.int2Json(list, "B40"));

				list.clear();
				list.add(40340);
				list.add(40936);
				list.add(41134);
				list.add(41332);
				PrefUtil.build().putValue("B41", Util.int2Json(list, "B41"));
			}
		} catch (JSONException e) {
			AppLog.E("readArfcnData JSONException e = " + e);
		}
	}

	private void clickStopFreqScan(boolean isBack) {
		AppLog.D("FreqFragment clickStopFreqScan()");
		MainActivity.getInstance().createCustomDialog(false);
		View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);

		final TextView title = view.findViewById(R.id.title);
		title.setText(R.string.stop_freq_tip);
		view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (device.getWorkState() == GnbBean.State.FREQ_SCAN) {
					String id = device.getRsp().getDeviceId();
					MessageController.build().setOnlyCmd(id, GnbProtocol.OAM_MSG_STOP_FREQ_SCAN);
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							PaCtl.build().closePA(id);
						}
					},300);
				}

				isStopScan = true;
				progressDialog = new ProgressDialog(mContext, R.style.ThemeProgressDialogStyle);
				progressDialog.setTitle(mContext.getString(R.string.wait));
				progressDialog.setMessage(mContext.getString(R.string.freq_stoping));
				progressDialog.show();
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						if (progressDialog != null) {
							progressDialog.dismiss();
							progressDialog = null;

							if (device.getWorkState() == GnbBean.State.FREQ_SCAN) {
								device.setWorkState(GnbBean.State.IDLE);
								MainActivity.getInstance().updateSteps(0, StepBean.State.success, mContext.getString(R.string.freq_stoped));
							}

							setWorkState(false); // 避免命令下发不响应，这里也做一次清除状态
							if (isBack) dismiss();
						}
					}
				}, 8000);

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

	private void clickStartFreqScan() {
		AppLog.D("FreqFragment clickStartFreqScan()");
		if (device == null) {
			MainActivity.getInstance().showToast(mContext.getString(R.string.dev_offline));
			return;
		}
		if (device.getWorkState() == GnbBean.State.CATCH) {
			MainActivity.getInstance().showToast(mContext.getString(R.string.catching_tip));
			return;
		} else if (device.getWorkState() == GnbBean.State.TRACE) {
			MainActivity.getInstance().showToast(mContext.getString(R.string.traceing_tip));
			return;
		} else if (device.getWorkState() == GnbBean.State.STOP) {
			MainActivity.getInstance().showToast(mContext.getString(R.string.other_stoping_tip));
			return;
		}

		Double vol = device.getRsp().getVoltageList().get(1);
		if (vol < 1){
		}else if (vol < 16.6) {
			MainActivity.getInstance().showRemindDialog(mContext.getString(R.string.vol_min_title), mContext.getString(R.string.vol_min_tip));
			return;
		}

		int size = 0;
		for (CheckBoxBean bean : check_box_list) {
			if (bean.isChecked()) {
				size++;
			}
		}
		if (size == 0){
			MainActivity.getInstance().showToast(mContext.getString(R.string.select_band));
			return;
		}

		String sync_mode = PrefUtil.build().getValue("sync_mode", "Air").toString();
		boolean airSync = sync_mode.equals("Air") || sync_mode.equals(mContext.getString(R.string.air));
		isGpsScan = !airSync;

		if (isGpsScan && device.getRsp().getGpsSyncState() != GnbStateRsp.Gps.SUCC){
			//MainActivity.getInstance().showToast(mContext.getString(R.string.gps_not_sync_tip));
			//return;
		}

		scanArfcnBeanList.clear();
		isHasFile = false;
		index = 1;
		scanBand = "";

		if (device.getWorkState() == GnbBean.State.IDLE) {
			String id = device.getRsp().getDeviceId();
			for (CheckBoxBean bean : check_box_list) {
				if (bean.isChecked()) {
					device.setWorkState(GnbBean.State.FREQ_SCAN);
					setWorkState(true);
					startFreqScan(id, bean, report_level);
					break;
				}
			}
		}
	}
	int index = 1;

	private void showCfgArfcnDialog() {
		AppLog.D("FreqFragment showCfgArfcnDialog()");

		CfgArfcnDialog mCfgArfcnDialog = new CfgArfcnDialog(mContext, check_box_list);

		mCfgArfcnDialog.show();
	}

	private void freqScan(String id, String devName) {
		index++;
		int count = 0;
		boolean isNotStart = true;
		for (CheckBoxBean bean : check_box_list) {
			if (bean.isChecked()) {
				count++;
				if (count == index){
					isNotStart = false;
					startFreqScan(id, bean, report_level);
					break;
				}
			}
		}
		if (isNotStart){
			if (report_level == 1 || isBandScan){
				device.setWorkState(GnbBean.State.IDLE);
				setWorkState(false); // 避免命令下发不响应，这里也做一次清除状态
				if (isHasFile){
					MainActivity.getInstance().updateSteps(0, StepBean.State.success, mContext.getString(R.string.get_log_success));
					MainActivity.getInstance().showRemindDialog(mContext.getString(R.string.tip), mContext.getString(R.string.get_freq_success_go_path));
				}
				return;
			}
			index = 1;
			for (CheckBoxBean bean : check_box_list)
				if (bean.isChecked()) {
					startFreqScan(id, bean, report_level);
					break;
				}
		}
	}

	String scanBand = "";
	private void startFreqScan(String id, CheckBoxBean bean, final int report_level) {
		AppLog.D("FreqFragment startFreqScan id = " + id + ", type = " + bean.getText() + ", report_level = " + report_level);
		scanBand = bean.getText();
		int async_enable = isGpsScan ? 0 : 1;
		tv_freq_state.setText(MessageFormat.format(mContext.getString(R.string.freq_scaning_info), scanBand));

		if (isBandScan){
			int band = 0;
			if (bean.getText().contains("N")) band = Integer.parseInt(bean.getText().replace("N", ""));
			else if (bean.getText().contains("B")) band = 1000 + Integer.parseInt(bean.getText().replace("B", ""));
			MessageController.build().startBandScan(id, report_level, async_enable, 0, band, 0);
			return;
		}

		List<Integer> list = bean.getArfcnList();

		//int offset = Integer.parseInt(PrefUtil.build().getValue("sync_time_offset_" + bean.getText(), "0").toString());

		if (list.size() == 0) {
			MainActivity.getInstance().showToast(bean.getText() + mContext.getString(R.string.freq_list_empty));
			freqScan(id, "");
			return;
		}

		int offset = 0;
		switch (bean.getText()) {
			case "N1":
			case "N78":
			case "B1":
			case "B3":
			case "B5":
			case "B7":
			case "B8":
				offset = 0;
				break;
			case "N28":
			case "N41":
			case "N79":
				offset = GnbCity.build().getTimingOffset("5G");
				break;
			case "B34":
				offset = GnbCity.build().getTimingOffset("B34");
				break;
			case "B39":
				offset = GnbCity.build().getTimingOffset("B39");
				break;
			case "B40":
				offset = GnbCity.build().getTimingOffset("B40");
				break;
			case "B41":
				offset = GnbCity.build().getTimingOffset("B41");
				break;
		}

		final List<Integer> arfcn_list = new ArrayList<>();
		final List<Integer> time_offset = new ArrayList<>();
		final List<Integer> chan_id = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			arfcn_list.add(list.get(i));
			time_offset.add(offset);
			chan_id.add(bean.getChan_id());
		}

		//refreshWorkState(getIndexById(id), GnbBean.State.FREQ_SCAN, "扫频中");

		AppLog.D("FreqFragment startFreqScan async_enable = " + async_enable + ", " + isBandScan + ", " + report_level + ", " + PaBean.build().toString());
		MessageController.build().startFreqScan(id, report_level, async_enable, arfcn_list.size(), chan_id, arfcn_list, time_offset);
	}

	public void onFreqScanRsp(String id, GnbFreqScanRsp rsp) {
		if (rsp != null) {
			AppLog.I("FreqFragment onFreqScanRsp() isStopScan " + isStopScan + ", rsp.getReportStep() = " + rsp.getReportStep() + ", rsp.getScanResult() = " + rsp.getScanResult());
			if (!isStopScan) {
				if (rsp.getReportStep() == 2) {
					freqScan(id, "");
				}
			} else {
				if (rsp.getReportStep() == 2) {
                    /*if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }*/
					//refreshWorkState(indexById, GnbBean.State.IDLE, "准备就绪");
				}
			}
			if (rsp.getScanResult() == GnbProtocol.OAM_ACK_OK && rsp.getReportStep() == 1) {
				if (scanArfcnBeanList.size() == 0) {
					scanArfcnBeanList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
							rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
							rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
				} else {
					boolean isAdd = true;
					for (int i = 0; i < scanArfcnBeanList.size(); i++) {
						if (scanArfcnBeanList.get(i).getUl_arfcn() == rsp.getUl_arfcn() &&
								scanArfcnBeanList.get(i).getPci() == rsp.getPci()) {
							isAdd = false;
							scanArfcnBeanList.remove(scanArfcnBeanList.get(i));
							ScanArfcnBean arfcnBean = new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
									rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
									rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth());
							if (rsp.getTac().isEmpty() || rsp.getEci().isEmpty()){
								scanArfcnBeanList.add(arfcnBean);
							}else {
								scanArfcnBeanList.add(0, arfcnBean);
							}
							break;
						}
					}
					if (isAdd) {
						ScanArfcnBean arfcnBean = new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
								rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
								rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth());
						if (rsp.getTac().isEmpty() || rsp.getEci().isEmpty()){
							scanArfcnBeanList.add(arfcnBean);
						}else {
							scanArfcnBeanList.add(0, arfcnBean);
						}
					}
				}
				adapter.notifyDataSetChanged();
			}
		}
	}

	private void setWorkState(boolean isStart) {
		if (isStart) {
			if (isStopScan){
				isStopScan = false;
				tv_start_scan.setText(mContext.getString(R.string.stop_freq));
				String scanning = mContext.getString(R.string.freq_scanning);
				MainActivity.getInstance().updateProgress(0, 100, 0, scanning,false);
				MainActivity.getInstance().updateProgress(0, 100, 1, scanning,false);
				MainActivity.getInstance().updateProgress(0, 100, 2, scanning,false);
				MainActivity.getInstance().updateProgress(0, 100, 3, scanning,false);
				MainActivity.getInstance().updateSteps(0, StepBean.State.success, mContext.getString(R.string.start_freq));
				checkBoxAdapter.setEnable(false);
				cb_all.setEnabled(false);

				iv_anim_freq.setVisibility(View.VISIBLE);
				AnimationDrawable drawable = (AnimationDrawable) iv_anim_freq.getDrawable();
				drawable.start();
			}
		} else {
			isStopScan = true;
			tv_freq_state.setText("");
			tv_start_scan.setText(mContext.getString(R.string.start_freq));
			String idle = mContext.getString(R.string.idle);
			MainActivity.getInstance().updateProgress(0, 100, 0, idle,false);
			MainActivity.getInstance().updateProgress(0, 100, 1, idle,false);
			MainActivity.getInstance().updateProgress(0, 100, 2, idle,false);
			MainActivity.getInstance().updateProgress(0, 100, 3, idle,false);
			MainActivity.getInstance().updateSteps(0, StepBean.State.success, mContext.getString(R.string.freq_stoped));
			checkBoxAdapter.setEnable(true);
			cb_all.setEnabled(true);

			AnimationDrawable drawable = (AnimationDrawable) iv_anim_freq.getDrawable();
			drawable.stop();
			iv_anim_freq.setVisibility(View.GONE);
		}
	}

	boolean isHasFile = false;
	public void onFreqScanGetDocumentRsp(String id, GnbFreqScanGetDocumentRsp rsp) {
		if (rsp.getReportLevel() == 1 && rsp.getScanResult() == 0){
			isHasFile = true;
			MainActivity.getInstance().showToast(scanBand + mContext.getString(R.string.scan_ok_get_local));
			FTPUtil.build().startGetFile(id, device.getRsp().getWifiIp(), FileProtocol.DIR_Scan, rsp.getFileName().replace(".zip", ""));
		}
		freqScan(id, "");
	}
}
