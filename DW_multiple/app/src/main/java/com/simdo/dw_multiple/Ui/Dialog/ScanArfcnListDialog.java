package com.simdo.dw_multiple.Ui.Dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.simdo.dw_multiple.Bean.ScanArfcnBean;
import com.simdo.dw_multiple.R;
import com.simdo.dw_multiple.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class ScanArfcnListDialog extends Dialog  {

	private LinearLayout contentView;
	private Context mContext;
	private LayoutInflater mInflater;
	private Dialog mDialog;
	private ListView mArfcnListView;
	private ArfcnListAdapter mArfcnListAdapter;
	private List<ScanArfcnBean> mArfcnList ;
	private boolean isTrance = false;
	private int cell_id = 0;
	public ScanArfcnListDialog(Context context, LayoutInflater mInflater, List<ScanArfcnBean> data, boolean isTrance, int cell_id) {
		super(context, R.style.style_dialog);
        this.mContext = context;
		this.mInflater =mInflater;
		this.mArfcnList = data;
		this.isTrance = isTrance;
		this.cell_id = cell_id;
        contentView = (LinearLayout) View.inflate(context, R.layout.dialog_scan_arfcn_list, null);
        this.setContentView(contentView);
		this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
		//this.setCancelable(false);   // 返回键不消失
		//设置dialog大小，这里是一个小赠送，模块好的控件大小设置
		Window window = this.getWindow();  
		window.setGravity(Gravity.BOTTOM); //可设置dialog的位置  
		window.getDecorView().setPadding(0, 0, 0, 0); //消除边距  
		WindowManager.LayoutParams lp = window.getAttributes();   
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕  
		lp.height = WindowManager.LayoutParams.MATCH_PARENT;  
		window.setAttributes(lp);
        initView();

    }
	
    private void initView() {
		mArfcnListAdapter = new ArfcnListAdapter(mInflater,mArfcnList);
    	mArfcnListView =  contentView.findViewById(R.id.list);
		mArfcnListView.setSelected(true);
		mArfcnListView.setAdapter(mArfcnListAdapter);

    }
	public void setTempCell(List<ScanArfcnBean> cells){
		mArfcnListAdapter.addTempCell(cells);

	}

	public class ArfcnListAdapter extends BaseAdapter {
    	
    	private final class ViewHolder {
    		private TextView tac;
    		private TextView eci;
    		private TextView ul_arfcn;
    		private TextView dl_arfcn;
    		private TextView pci;
    		private TextView rx;
			private TextView pa;
			private TextView pk;
    	}
    	
		private LayoutInflater mInflater;
		private List<ScanArfcnBean> tempData = new ArrayList<>();
		public ArfcnListAdapter(LayoutInflater mInflater, List<ScanArfcnBean> datas) {
			this.mInflater = mInflater;
			tempData.clear();
			tempData.addAll(datas);
		}

		public void clearTempData() {
	        tempData.clear();
	    }

	    public void addTempCell(List<ScanArfcnBean> parent) {
			tempData.clear();
			tempData.addAll(parent);
			notifyDataSetChanged();
	    }
	    
		@Override
		public int getCount() {
			return tempData.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int pos, View convertView, ViewGroup viewGroup) {
			 ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.list_scan_arfcn_item, null);
				holder.tac =  convertView.findViewById(R.id.tv_tac);
				holder.eci =  convertView.findViewById(R.id.tv_eci);
				holder.ul_arfcn =  convertView.findViewById(R.id.tv_ul_arfcn);
				holder.dl_arfcn =  convertView.findViewById(R.id.tv_dl_arfcn);
				holder.pci =  convertView.findViewById(R.id.tv_pci);
				holder.rx =  convertView.findViewById(R.id.tv_rx);
				holder.pa =  convertView.findViewById(R.id.tv_pa);
				holder.pk =  convertView.findViewById(R.id.tv_pk);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final ScanArfcnBean item = tempData.get(pos);
			holder.tac.setText(String.valueOf(item.getTac()));
			holder.eci.setText(String.valueOf(item.getEci()));
			holder.ul_arfcn.setText(String.valueOf(item.getUl_arfcn()));
			holder.dl_arfcn.setText(String.valueOf(item.getDl_arfcn()));
			holder.pci.setText(String.valueOf(item.getPci()));
			holder.rx.setText(String.valueOf(item.getRsrp()));
			holder.pa.setText(String.valueOf(item.getPa()));
			holder.pk.setText(String.valueOf(item.getPk()));
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isTrance){
						listener.OnImport(item.getUl_arfcn(),item.getPci(),cell_id);
					}
				}
			});
			return convertView;
		}
	}

	private void createCustomDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
		mDialog = new Dialog(mContext, R.style.style_dialog);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失 
		mDialog.setCancelable(false);   // 返回键不消失
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
	public void removeOnScanArfcnDialogListener(){
		this.listener = null;
	}
	public void setOnScanArfcnDialogListener(OnScanArfcnDialogListener listener) {
		this.listener = listener;
	}

	public interface OnScanArfcnDialogListener {
		void OnImport(int arfcn, int pci, int cell_id);
	}

	private OnScanArfcnDialogListener listener;
}
