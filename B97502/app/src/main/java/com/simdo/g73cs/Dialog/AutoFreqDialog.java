package com.simdo.g73cs.Dialog;

import static com.simdo.g73cs.MainActivity.device;

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
import com.nr.Gnb.Response.GnbFreqScanRsp;
import com.nr.Socket.MessageControl.MessageController;
import com.simdo.g73cs.Adapter.FreqResultListAdapter;
import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.FreqUtil;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AutoFreqDialog extends Dialog {

	public AutoFreqDialog(Context context, boolean isMS) {
		super(context, R.style.style_dialog);
		this.mContext = context;
		this.isMS = isMS;
		View view = View.inflate(context, R.layout.dialog_freq, null);
		this.setContentView(view);
		this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
		this.setCancelable(false);   // 返回键不消失
		//设置dialog大小，这里是一个小赠送，模块好的控件大小设置
		Window window = this.getWindow();
		//window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
		window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
		window.setNavigationBarColor(Color.parseColor("#2A72FF"));
		//StatusBarUtil.setLightStatusBar(window, true);
		WindowManager.LayoutParams lp = window.getAttributes();

		lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 9 / 10;// 设置宽度屏幕的 9 / 10
		lp.height = mContext.getResources().getDisplayMetrics().heightPixels * 3 / 4;
		window.setAttributes(lp);
		initData();
		initView(view);
		isStopScan = false;
		freqScan(device.getRsp().getDeviceId()); //直接开始扫频
	}
	private boolean userCancel = false;
	public boolean isUserCancel(){
		return userCancel;
	}

	public LinkedList<ArfcnPciBean> getTD1(){
		return TD1;
	}
	public LinkedList<ArfcnPciBean> getTD2(){
		return TD2;
	}
	public LinkedList<ArfcnPciBean> getTD3(){
		return TD3;
	}
	public LinkedList<ArfcnPciBean> getTD4(){
		return TD4;
	}

	private final Context mContext;
	private boolean isMS;

	private void initData() {
		index = 0;
		scanCount = MainActivity.getInstance().scanCount;
		this.freqMap = new LinkedHashMap<>();
		this.freqList = new LinkedList<>();
		if (isMS) {
			initFreqMapMS();
			this.freqList.addAll(MainActivity.getInstance().freqMSList);
		} else {
			initFreqMapUT();
			this.freqList.addAll(MainActivity.getInstance().freqUTList);
		}
	}
	private void initFreqMapMS(){
		freqMap = FreqUtil.build().getFreqMapMS();
	}
	private void initFreqMapUT(){
		freqMap = FreqUtil.build().getFreqMapUT();
	}
	TextView tv_freq_info, tv_freq_count;
	LinkedList<ScanArfcnBean> freqList;
	boolean isStopScan;
	LinkedHashMap<String, ArrayList<Integer>> freqMap;
	FreqResultListAdapter adapter;
	LinkedList<ArfcnPciBean> TD1, TD2, TD3, TD4;

	private void initView(View view) {
		tv_freq_info = view.findViewById(R.id.tv_freq_info);
		tv_freq_count = view.findViewById(R.id.tv_freq_count);

		RecyclerView freq_list = view.findViewById(R.id.freq_list);
		freq_list.setLayoutManager(new LinearLayoutManager(mContext));

		adapter = new FreqResultListAdapter(mContext, freqList, new ListItemListener() {
			@Override
			public void onItemClickListener(int position) {

			}
		});
		freq_list.setLayoutManager(new LinearLayoutManager(mContext));
		freq_list.setAdapter(adapter);

		TextView btn_cancel = view.findViewById(R.id.btn_cancel);
		btn_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (btn_cancel.getText().equals(mContext.getString(R.string.cancel))){
					isStopScan = true;
					userCancel = true;
					btn_cancel.setText(mContext.getString(R.string.stoping));
					MainActivity.getInstance().showToast(mContext.getString(R.string.freq_ending));
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
	private boolean listIsContains(LinkedList<ArfcnPciBean> td, int arfcn) {
		// 判断列表中是否包含该频点
		for (ArfcnPciBean bean : td) {
			if (bean.getArfcn().equals(String.valueOf(arfcn))) return true;
		}
		return false;
	}
	int index;
	int scanCount;
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
			if (scanCount > 1){
				scanCount--;
				index = 0;
				freqMap.remove("N1");
				freqMap.remove("N28");
				freqMap.remove("N79");
				freqMap.remove("B5");
				freqMap.remove("B8");
				freqMap.remove("B34");
				freqMap.remove("B38");
				MainActivity.getInstance().showToast(MessageFormat.format(mContext.getString(R.string.add_scan_count), MainActivity.getInstance().scanCount - scanCount, MainActivity.getInstance().scanCount - 1), true);
				freqScan(id);
				return;
			}
			MainActivity.getInstance().showToast(mContext.getString(R.string.freq_end_tip));
			//freqList.add(new ScanArfcnBean("", "", 0, 1300, 344, -101, 0, 0, 0, 0, 0, 0, 0, 0));
			LinkedList<LinkedList<ArfcnPciBean>> linkedLists = FreqUtil.build().decFreqList(freqList);
			TD1 = new LinkedList<>(linkedLists.get(0));
			TD2 = new LinkedList<>(linkedLists.get(1));
			TD3 = new LinkedList<>(linkedLists.get(2));
			TD4 = new LinkedList<>(linkedLists.get(3));

			boolean isAddTD1 = TD1.size() == 0;
			boolean isAddTD2 = TD2.size() == 0;
			boolean isAddTD3 = TD3.size() == 0;
			boolean isAddTD4 = TD4.size() == 0;

			if (isAddTD1 || isAddTD2 || isAddTD3 || isAddTD4){
				if (PaCtl.build().isB97502){
					for (Map.Entry<String, ArrayList<Integer>> entry : freqMap.entrySet()) {
						switch (entry.getKey()){
							case "N41":
							case "N79":
								if (isAddTD1 && !listIsContains(TD1, 504990)) TD1.add(new ArfcnPciBean("504990", "50"));
								break;
							case "N78":
								if (isAddTD1 && !listIsContains(TD1, 627264)) {
									TD1.add(new ArfcnPciBean("627264", "50"));
									TD1.add(new ArfcnPciBean("633984", "50"));
								}
								break;
							case "B3":
							case "B5":
							case "B8":
								/*if (isAddTD2){
									for (Integer value : entry.getValue()) TD2.add(new ArfcnPciBean(String.valueOf(value), "501"));
								}*/
								break;
							case "N1":
							case "N28":
								/*if (isAddTD3){
									for (Integer value : entry.getValue()) TD3.add(new ArfcnPciBean(String.valueOf(value), "1002"));
								}*/
								break;
							case "B34":
							case "B39":
							case "B40":
							case "B41":
								if (isAddTD4 && !listIsContains(TD4, 38950)) TD4.add(new ArfcnPciBean("38950", "50"));
								break;
							case "B1":
								if (isAddTD3 && !listIsContains(TD3, 100)) TD3.add(new ArfcnPciBean("100", "50"));
								break;
						}
					}
				}else {
					boolean isUT = false;
					for (Map.Entry<String, ArrayList<Integer>> entry : freqMap.entrySet()) {
						switch (entry.getKey()){
							case "N28":
							case "N79":
								/*if (isAddTD1) {
									TD1.add(new ArfcnPciBean("627264", "50"));
									TD1.add(new ArfcnPciBean("633984", "50"));
								}*/
								break;
							case "N78":
								isUT = true;
								if (isAddTD1 && !listIsContains(TD1, 627264)) {
									TD1.add(new ArfcnPciBean("627264", "50"));
									TD1.add(new ArfcnPciBean("633984", "50"));
								}
								break;
							case "B34":
							case "B39":
							case "B40":
							case "B41":
								if (isAddTD2 && !listIsContains(TD2, 38950)) TD2.add(new ArfcnPciBean("38950", "50"));
								break;
							case "N1":
							case "N41":
								if (isAddTD3 && !listIsContains(TD3, 504990)) TD3.add(new ArfcnPciBean("504990", "50"));
								break;
							case "B1":
							case "B3":
							case "B5":
							case "B8":
								if (isAddTD4 && isUT && !listIsContains(TD3, 100)) TD4.add(new ArfcnPciBean("100", "50"));
								break;
						}
					}
				}
			}
			// 暂定 B3 / B8 / B39处理逻辑
			int b3Num = 0;
			int b8Num = 0;
			int b39Num = 0;
			int b3Max = 0;
			int b39Max = 0;
			for (ScanArfcnBean bean : freqList) {
				if (bean.getDl_arfcn() > 99999) continue;
				int band = LteBand.earfcn2band(bean.getDl_arfcn());
				if (band == 3){
					b3Num++;
					int rsrp = bean.getRsrp();
					// 从列表中筛选出最大值的rsrp
					if (rsrp > b3Max || b3Max == 0) b3Max = rsrp;
				}else if (band == 8){
					b8Num++;
				}else if (band == 39){
					b39Num++;
					int rsrp = bean.getRsrp();
					// 从列表中筛选出最大值的rsrp
					if (rsrp > b39Max || b39Max == 0) b39Max = rsrp;
				}
			}
			boolean isDeleteB3 = true;
			if (b3Num > 0 && b8Num > 0 && b39Num > 0) isDeleteB3 = false;
			else if ((b3Max > -85 || b39Max > -85) || (b3Max < -105 && b39Max < -105)){
				if (b39Max == 0 || b3Max > b39Max) isDeleteB3 = false;
			}else {
				if (b3Num > b39Num) isDeleteB3 = false;
			}
			if (PaCtl.build().isB97502){
				if (isDeleteB3){
					for (int i = TD2.size() - 1; i > -1; i--)
						if (LteBand.earfcn2band(Integer.parseInt(TD2.get(i).getArfcn())) == 3) TD2.remove(i);
				}else if (b3Num > 0){
					for (int i = TD4.size() - 1; i > -1; i--)
						if (LteBand.earfcn2band(Integer.parseInt(TD4.get(i).getArfcn())) == 39) TD4.remove(i);
				}
				// 通道四只有B39，删除通道二的B8
				if (b39Num > 0 && b39Num == TD4.size()) {
					for (int i = TD2.size() - 1; i > -1; i--)
						if (LteBand.earfcn2band(Integer.parseInt(TD2.get(i).getArfcn())) == 8) TD2.remove(i);
				}
			}else {
				if (isDeleteB3){
					for (int i = TD4.size() - 1; i > -1; i--)
						if (LteBand.earfcn2band(Integer.parseInt(TD4.get(i).getArfcn())) == 3) TD4.remove(i);
				}else if (b3Num > 0){
					for (int i = TD2.size() - 1; i > -1; i--)
						if (LteBand.earfcn2band(Integer.parseInt(TD2.get(i).getArfcn())) == 39) TD2.remove(i);
				}
				// 通道二只有B39，删除通道四的B8
				if (b39Num > 0 && b39Num == TD2.size()) {
					for (int i = TD4.size() - 1; i > -1; i--)
						if (LteBand.earfcn2band(Integer.parseInt(TD4.get(i).getArfcn())) == 8) TD4.remove(i);
				}
			}
			StringBuilder freqStr = new StringBuilder();
			for (ScanArfcnBean bean : freqList) {
				freqStr.append("arfcn = ").append(bean.getDl_arfcn()).append(", rsrp = ").append(bean.getRsrp()).append("   ");
			}
			AppLog.D("freqScan end, freqStr = " + freqStr);

			freqStr = new StringBuilder();
			for (ArfcnPciBean bean : TD1) {
				freqStr.append("arfcn = ").append(bean.getArfcn()).append(", rsrp = ").append(bean.getRsrp()).append("   ");
			}
			AppLog.D("freqScan end, TD1 = " + freqStr);

			freqStr = new StringBuilder();
			for (ArfcnPciBean bean : TD2) {
				freqStr.append("arfcn = ").append(bean.getArfcn()).append(", rsrp = ").append(bean.getRsrp()).append("   ");
			}
			AppLog.D("freqScan end, TD2 = " + freqStr);

			freqStr = new StringBuilder();
			for (ArfcnPciBean bean : TD3) {
				freqStr.append("arfcn = ").append(bean.getArfcn()).append(", rsrp = ").append(bean.getRsrp()).append("   ");
			}
			AppLog.D("freqScan end, TD3 = " + freqStr);

			freqStr = new StringBuilder();
			for (ArfcnPciBean bean : TD4) {
				freqStr.append("arfcn = ").append(bean.getArfcn()).append(", rsrp = ").append(bean.getRsrp()).append("   ");
			}
			AppLog.D("freqScan end, TD4 = " + freqStr);
			userCancel = false;
			MainActivity.getInstance().freqList.clear();
			MainActivity.getInstance().freqList.addAll(freqList);
			if (isMS){
				MainActivity.getInstance().freqMSList.clear();
				MainActivity.getInstance().freqMSList.addAll(freqList);
			}else {
				MainActivity.getInstance().freqUTList.clear();
				MainActivity.getInstance().freqUTList.addAll(freqList);
			}
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
		if (PaCtl.build().isB97502){
			switch (band){
				// N41/N78/N79
				case "N41":
				case "N78":
				case "N79":
					this_chan_id = 1;
					break;
				// B1/B3/B5/B8
				case "B1":
				case "B3":
				case "B5":
				case "B8":
					this_chan_id = 2;
					break;
				// N1/N28
				case "N1":
				case "N28":
					this_chan_id = 3;
					break;
				// B34/B39/B40/B41
				case "B34":
				case "B39":
				case "B40":
				case "B41":
					this_chan_id = 4;
					break;
			}
		}else {
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
		if (rsp != null) {
			if (!isStopScan) {
				if (rsp.getReportStep() == 2) {
					index++;
					freqScan(id);
				}
			}
			if (rsp.getScanResult() == GnbProtocol.OAM_ACK_OK && rsp.getReportStep() == 1) {
				AppLog.I("onFreqScanRsp() isStopScan " + isStopScan + ", arfcn = " + rsp.getUl_arfcn() + ", Rsrp = " + rsp.getRsrp());
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
