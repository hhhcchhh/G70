package com.simdo.g73cs.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Response.GnbCmdRsp;
import com.nr.Gnb.Response.GnbFreqScanRsp;
import com.nr.Socket.MessageControl.MessageController;
import com.simdo.g73cs.Adapter.FreqResultListAdapter;
import com.simdo.g73cs.Bean.ArfcnBean;
import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Fragment.TraceCatchFragment;
import com.simdo.g73cs.Listener.OnTraceSetListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.FreqUtil;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FreqDialog extends Dialog {
	TraceCatchFragment mTraceCatchFragment;
	public FreqDialog(Context context, TraceCatchFragment traceCatchFragment) {
		super(context, R.style.style_dialog);
		this.mContext = context;
		this.mTraceCatchFragment = traceCatchFragment;
		View view = View.inflate(context, R.layout.dialog_freq, null);
		this.setContentView(view);
		this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
		this.setCancelable(false);   // 返回键不消失
		//设置dialog大小，这里是一个小赠送，模块好的控件大小设置
		Window window = this.getWindow();
		//window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
		window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
		window.setNavigationBarColor(mContext.getResources().getColor(R.color.main_bg_color));
		//StatusBarUtil.setLightStatusBar(window, true);
		WindowManager.LayoutParams lp = window.getAttributes();

		lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 9 / 10;// 设置宽度屏幕的 9 / 10
		lp.height = mContext.getResources().getDisplayMetrics().heightPixels * 3 / 4;
		window.setAttributes(lp);
		initData();
		initView(view);
		isStopScan = false;
		freqScan(MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId()); //直接开始扫频
	}

	private final Context mContext;
	OnTraceSetListener listener;
	public void setOnTraceSetListener(OnTraceSetListener listener) {
		this.listener = listener;
	}

	private void initData() {
		index = 0;
		freqList = new LinkedList<>();
		freqMap = new LinkedHashMap<>();

		// 5G 常见频点

		// N41 504990、 512910、 516990、 507150、 525630
		ArrayList<Integer> listN41 = new ArrayList<>();
		List<ArfcnBean> freq_n41 = PrefUtil.build().getFreqArfcnList("freq_N41");
		for (ArfcnBean arfcnBean : freq_n41) {
			if (arfcnBean.isChecked()) listN41.add(arfcnBean.getArfcn());
		}
		if (listN41.size() > 0) freqMap.put("N41", listN41);

		// N1 427250、422890、 428910、 426030
		ArrayList<Integer> listN1 = new ArrayList<>();
		List<ArfcnBean> freq_n1 = PrefUtil.build().getFreqArfcnList("freq_N1");
		for (ArfcnBean arfcnBean : freq_n1) {
			if (arfcnBean.isChecked()) listN1.add(arfcnBean.getArfcn());
		}
		if (listN1.size() > 0) freqMap.put("N1", listN1);

		// N78 627264、 633984
		ArrayList<Integer> listN78 = new ArrayList<>();
		List<ArfcnBean> freq_n78 = PrefUtil.build().getFreqArfcnList("freq_N78");
		for (ArfcnBean arfcnBean : freq_n78) {
			if (arfcnBean.isChecked()) listN78.add(arfcnBean.getArfcn());
		}
		if (listN78.size() > 0) freqMap.put("N78", listN78);

		// N28 154810、 152650、 152890、 156970、 154570、 156490、 155770
		ArrayList<Integer> listN28 = new ArrayList<>();
		List<ArfcnBean> freq_n28 = PrefUtil.build().getFreqArfcnList("freq_N28");
		for (ArfcnBean arfcnBean : freq_n28) {
			if (arfcnBean.isChecked()) listN28.add(arfcnBean.getArfcn());
		}
		if (listN28.size() > 0) freqMap.put("N28", listN28);

		// N79 723360
		ArrayList<Integer> listN79 = new ArrayList<>();
		List<ArfcnBean> freq_n79 = PrefUtil.build().getFreqArfcnList("freq_N79");
		for (ArfcnBean arfcnBean : freq_n79) {
			if (arfcnBean.isChecked()) listN79.add(arfcnBean.getArfcn());
		}
		if (listN79.size() > 0) freqMap.put("N79", listN79);

		// B3 1300、 1275、 1650、 1506、 1500、 1531、 1524、 1850
		ArrayList<Integer> listB3 = new ArrayList<>();
		List<ArfcnBean> freq_b3 = PrefUtil.build().getFreqArfcnList("freq_B3");
		for (ArfcnBean arfcnBean : freq_b3) {
			if (arfcnBean.isChecked()) listB3.add(arfcnBean.getArfcn());
		}
		if (listB3.size() > 0) freqMap.put("B3", listB3);

		// 4G 常见频点
		// B1  350、 375、 400、 450、 500、 100
		ArrayList<Integer> listB1 = new ArrayList<>();
		List<ArfcnBean> freq_b1 = PrefUtil.build().getFreqArfcnList("freq_B1");
		for (ArfcnBean arfcnBean : freq_b1) {
			if (arfcnBean.isChecked()) listB1.add(arfcnBean.getArfcn());
		}
		if (listB1.size() > 0) freqMap.put("B1", listB1);

		// B5 2452
		ArrayList<Integer> listB5 = new ArrayList<>();
		List<ArfcnBean> freq_b5 = PrefUtil.build().getFreqArfcnList("freq_B5");
		for (ArfcnBean arfcnBean : freq_b5) {
			if (arfcnBean.isChecked()) listB5.add(arfcnBean.getArfcn());
		}
		if (listB5.size() > 0) freqMap.put("B5", listB5);

		// B8 3682、 3683、 3641、 3621、 3590、 3725、 3768、 3769、 3770、 3775
		ArrayList<Integer> listB8 = new ArrayList<>();
		List<ArfcnBean> freq_b8 = PrefUtil.build().getFreqArfcnList("freq_B8");
		for (ArfcnBean arfcnBean : freq_b8) {
			if (arfcnBean.isChecked()) listB8.add(arfcnBean.getArfcn());
		}
		if (listB8.size() > 0) freqMap.put("B8", listB8);

		// B40 38950、 39148、 39292、 38750
		ArrayList<Integer> listB40 = new ArrayList<>();
		List<ArfcnBean> freq_b40 = PrefUtil.build().getFreqArfcnList("freq_B40");
		for (ArfcnBean arfcnBean : freq_b40) {
			if (arfcnBean.isChecked()) listB40.add(arfcnBean.getArfcn());
		}
		if (listB40.size() > 0) freqMap.put("B40", listB40);

		// B34 36275
		ArrayList<Integer> listB34 = new ArrayList<>();
		List<ArfcnBean> freq_b34 = PrefUtil.build().getFreqArfcnList("freq_B34");
		for (ArfcnBean arfcnBean : freq_b34) {
			if (arfcnBean.isChecked()) listB34.add(arfcnBean.getArfcn());
		}
		if (listB34.size() > 0) freqMap.put("B34", listB34);

		// B38 37900、 38098
		/*ArrayList<Integer> listB38 = new ArrayList<>();
		listB38.add(37900);
		listB38.add(38098);
		freqMap.put("B38", listB38);*/

		// B39 38400、 38544
		ArrayList<Integer> listB39 = new ArrayList<>();
		List<ArfcnBean> freq_b39 = PrefUtil.build().getFreqArfcnList("freq_B39");
		for (ArfcnBean arfcnBean : freq_b39) {
			if (arfcnBean.isChecked()) listB39.add(arfcnBean.getArfcn());
		}
		if (listB39.size() > 0) freqMap.put("B39", listB39);

		// B41 40936、 40340
		ArrayList<Integer> listB41 = new ArrayList<>();
		List<ArfcnBean> freq_b41 = PrefUtil.build().getFreqArfcnList("freq_B41");
		for (ArfcnBean arfcnBean : freq_b41) {
			if (arfcnBean.isChecked()) listB41.add(arfcnBean.getArfcn());
		}
		if (listB41.size() > 0) freqMap.put("B41", listB41);
	}

	TextView tv_freq_info, tv_freq_count;
	LinkedList<ScanArfcnBean> freqList;
	boolean isStopScan = true;
	LinkedHashMap<String, ArrayList<Integer>> freqMap;
	FreqResultListAdapter adapter;

	private void initView(View view) {
		tv_freq_info = view.findViewById(R.id.tv_freq_info);
		tv_freq_count = view.findViewById(R.id.tv_freq_count);

		RecyclerView freq_list = view.findViewById(R.id.freq_list);
		freq_list.setLayoutManager(new LinearLayoutManager(mContext));

		adapter = new FreqResultListAdapter(mContext, freqList);
		freq_list.setLayoutManager(new LinearLayoutManager(mContext));
		freq_list.setAdapter(adapter);

		TextView btn_cancel = view.findViewById(R.id.btn_cancel);
		btn_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (btn_cancel.getText().equals("取消")){
					isStopScan = true;
					btn_cancel.setText("结束中");
					MainActivity.getInstance().showToast("结束扫频中，请稍后");
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							dismiss();
						}
					}, 6000);
				}
			}
		});
	}

	int index;
	private void freqScan(String id) {
		int count = 0;
		boolean isNotStart = true;

		for (Map.Entry<String, ArrayList<Integer>> entry : freqMap.entrySet()) {
			if (count == index){
				isNotStart = false;
				startFreqScan(id, entry.getKey(), entry.getValue());
				break;
			}
			count++;
		}

		if (isNotStart){ // 列表走完，扫频结束
			MainActivity.getInstance().showToast("扫频完成，启动配置");
			LinkedList<ArfcnPciBean> TD1, TD2, TD3, TD4;
			LinkedList<LinkedList<ArfcnPciBean>> linkedLists = FreqUtil.build().decFreqList(freqList);
			TD1 = new LinkedList<>(linkedLists.get(0));
			TD2 = new LinkedList<>(linkedLists.get(1));
			TD3 = new LinkedList<>(linkedLists.get(2));
			TD4 = new LinkedList<>(linkedLists.get(3));

			if (TD1.size() > 0) mTraceCatchFragment.arfcnBeanHashMap.put("TD1", TD1);
			if (TD2.size() > 0) mTraceCatchFragment.arfcnBeanHashMap.put("TD2", TD2);
			if (TD3.size() > 0) mTraceCatchFragment.arfcnBeanHashMap.put("TD3", TD3);
			if (TD4.size() > 0) mTraceCatchFragment.arfcnBeanHashMap.put("TD4", TD4);
			listener.onTraceConfig();
			MainActivity.getInstance().freqList.clear();
			MainActivity.getInstance().freqList.addAll(freqList);
			dismiss();
		}
	}

	private void startFreqScan(String id, String band, List<Integer> list) {
		AppLog.D("FreqDialog startFreqScan id = " + id + ", band = " + band);
		PaCtl.build().closePA(id);
		tv_freq_info.setText(MessageFormat.format("{0}扫频中..", band));
		tv_freq_count.setText(MessageFormat.format("{0}/{1}", index + 1, freqMap.size()));

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				PaCtl.build().openPAByBand(id, band);
			}
		}, 300);

		int this_chan_id = 1;
		switch (band){
			// N28/N78/N79
			case "N28":
			case "N78":
			case "N79":
				this_chan_id = 1;
				break;
			// B34/B39/B40/B41
			case "B34":
			case "B39":
			case "B40":
			case "B41":
				this_chan_id = 2;
				break;
			// N1/N41
			case "N1":
			case "N41":
				this_chan_id = 3;
				break;
			// B1/B3/B5/B8
			case "B1":
			case "B3":
			case "B5":
			case "B8":
				this_chan_id = 4;
				break;
		}
		int offset = 0;
		switch (band) {
			case "N1":
			case "N78":
			case "B1":
			case "B3":
			case "B5":
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
			chan_id.add(this_chan_id);
		}

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				MessageController.build().startFreqScan(id, 0, 1, arfcn_list.size(), chan_id, arfcn_list, time_offset);
			}
		}, 600);
	}

	public void onFreqScanRsp(String id, GnbFreqScanRsp rsp) {
		int indexById = MainActivity.getInstance().getIndexById(id);
		if (indexById == -1) return;
		if (rsp != null) {
			AppLog.I("onFreqScanRsp() isStopScan " + isStopScan + ", rsp.getReportStep() = " + rsp.getReportStep() + ", rsp.getScanResult() = " + rsp.getScanResult());
			if (!isStopScan) {
				if (rsp.getReportStep() == 2) {
					index++;
					freqScan(id);
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
				if (freqList.size() == 0) {
					freqList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
							rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
							rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
				} else {
					boolean isAdd = true;
					for (int i = 0; i < freqList.size(); i++) {
						if (freqList.get(i).getUl_arfcn() == rsp.getUl_arfcn() &&
								freqList.get(i).getPci() == rsp.getPci()) {
							isAdd = false;
							freqList.remove(freqList.get(i));
							freqList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
									rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
									rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
							break;
						}
					}
					if (isAdd) {
						freqList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
								rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
								rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
					}
				}
				adapter.notifyDataSetChanged();
			}
		}
	}

	public void onStopFreqScanRsp(String id, GnbCmdRsp rsp) {
		int indexById = MainActivity.getInstance().getIndexById(id);
		if (indexById == -1) return;
		if (rsp != null) {
			if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
				dismiss();
			}
		}
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
}
