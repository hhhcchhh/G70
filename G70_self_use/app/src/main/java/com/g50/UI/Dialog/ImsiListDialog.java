package com.g50.UI.Dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import com.File.FileProtocol;
import com.File.FileUtil;
import com.Logcat.APPLog;
import com.Util.DateUtil;
import com.Util.Util;
import com.g50.R;
import com.g50.UI.Bean.ImsiBean;
import com.nr70.Gnb.Bean.UeidBean;

public class ImsiListDialog extends Dialog implements OnClickListener {
	
	private LinearLayout contentView;
	private Context mContext;
	private LayoutInflater mInflater;
	private Dialog mDialog;
	private EditText mEtSearch, mEtFileName;
	private String currentKey = "";
	private ListView mImsiListView;
	private ImsiListAdapter mImsiListAdapter;
	private List<ImsiBean> mImsiList;
	private boolean over = true;
    private List<UeidBean> mBlackList;

	public ImsiListDialog(Context context, LayoutInflater inflater, List<ImsiBean> imsiList, List<UeidBean> blackList) {
    	 super(context, R.style.style_dialog);
        this.mContext = context;
        this.mInflater = inflater;
        this.mImsiList = imsiList;
        this.mBlackList = blackList;

        contentView = (LinearLayout) View.inflate(context, R.layout.dialog_imei_list_search, null);
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
    	mImsiListView = (ListView) contentView.findViewById(R.id.imsi_list);
		mImsiListAdapter = new ImsiListAdapter(mInflater, mImsiList);
		mImsiListView.setAdapter(mImsiListAdapter);
		mImsiListView.setSelected(true);
		/*mImsiListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String imsi = mImsiList.get(position).getImsi();
				// 黑名单里面的IMSI
				for (int i = 0; i < mBlackList.size(); i++) {
					if (imsi.equals(mBlackList.get(i)) && !imsi.equals(MessageHelper.build().traceImsi())) {
						break;
					}
				}
				if (listener != null) {
				    listener.onTraceImsiChange(imsi);
                }
			}
		});*/
    	
		Button mBtnSave = (Button) contentView.findViewById(R.id.btn_save_data);
		mBtnSave.setOnClickListener(this);
    	mEtSearch = (EditText) contentView.findViewById(R.id.et_search);
    	mEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mImsiList.size() != 0 && mImsiListAdapter != null) {
                	APPLog.D("refreshImsiList = " + currentKey);
                    currentKey = s.toString();
                    if (!TextUtils.isEmpty(s)) {
                    	mImsiListAdapter.clearTempData();
                        int a = mImsiList.size();
                        for (int i = 0; i < a; i++) {
                            if (mImsiList.get(i).getImsi().contains(currentKey)) {
                            	mImsiListAdapter.addTempCell(mImsiList.get(i));
                            }
                        }
                    } else {
                        mImsiListAdapter.setTempData(mImsiList);
                    }
                    mImsiListAdapter.notifyDataSetChanged();
                }
            }
        });
    }
    
    public void addImsiCell(ImsiBean imsi) {
		if (!TextUtils.isEmpty(currentKey)) {
			if (imsi.getImsi().contains(currentKey)) {
				mImsiListAdapter.addTempCell(imsi);
			}
		} else {
			mImsiListAdapter.addTempCell(imsi);
			int sum = mImsiList.size() - mImsiListAdapter.getCount();
			if (sum > 3) {
				mImsiListAdapter.setTempData(mImsiList);
			}
		}
		mImsiListAdapter.notifyDataSetChanged();
    }
    
    public void refreshImsiList(List<ImsiBean> imsiList) {
		if (over) {
			over = false;
			this.mImsiList = imsiList;
			if (!TextUtils.isEmpty(currentKey)) {
				mImsiListAdapter.clearTempData();
				int a = mImsiList.size();
				for (int i = 0; i < a; i++) {
					if (mImsiList.get(i).getImsi().contains(currentKey)) {
						mImsiListAdapter.addTempCell(mImsiList.get(i));
					}
				}
			} else {
				mImsiListAdapter.setTempData(mImsiList);
			}
			mImsiListAdapter.notifyDataSetChanged();
			over = true;
		}
    }

    public void setImsiList(List<ImsiBean> imsiList) {
		this.mImsiList = imsiList;
		mImsiListAdapter.setTempData(mImsiList);
	}

    private class ImsiListAdapter extends BaseAdapter {
    	
    	private final class ViewHolder {
    		private TextView pos;
    		private TextView imsi;
    		private TextView arfcn;
    		private TextView pci;
    		private TextView time;
    	}
    	
		private LayoutInflater mInflater;
		private List<ImsiBean> tempData = new ArrayList<ImsiBean>();

		public ImsiListAdapter(LayoutInflater mInflater, List<ImsiBean> datas) {
			this.mInflater = mInflater;
			
			tempData.clear();
			tempData.addAll(datas);
		}

		public void setTempData(List<ImsiBean> data) {
			if (tempData.size() > 0) {
				tempData.clear();
			}
			tempData.addAll(data);
			notifyDataSetChanged();
		}
		    
		public void clearTempData() {
	        tempData.clear();
	    }

	    public void addTempCell(ImsiBean parent) {
	        tempData.add(parent);
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
				convertView = mInflater.inflate(R.layout.dialog_imsi_list_item, null);

				holder.pos = (TextView) convertView.findViewById(R.id.tv_serial_number);
				holder.imsi = (TextView) convertView.findViewById(R.id.tv_tmsi);
				holder.arfcn = (TextView) convertView.findViewById(R.id.tv_arfcn);
				holder.pci = (TextView) convertView.findViewById(R.id.tv_pci);
				holder.time = (TextView) convertView.findViewById(R.id.tv_time);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			//APPLog.E("pos = " + pos + ", size = " + tempData.size());
			ImsiBean item = tempData.get(pos);
			int idx = pos + 1;
			holder.pos.setText(String.valueOf(idx) + ".");	
			holder.imsi.setText(item.getImsi());	
			holder.arfcn.setText(item.getArfcn());	
			holder.pci.setText(item.getPci());	
			holder.time.setText(DateUtil.formateTimeHMS(item.getLatestTime()));
			if (item.getState() == ImsiBean.State.IMSI_OLD) {
				// 上一次定位到的IMSI，此次未定位到但在黑名单中的，则显示蓝色
				holder.pos.setTextColor(mContext.getResources().getColor(R.color.state_text_color));
				holder.imsi.setTextColor(mContext.getResources().getColor(R.color.state_text_color));
				holder.arfcn.setTextColor(mContext.getResources().getColor(R.color.state_text_color));
				holder.pci.setTextColor(mContext.getResources().getColor(R.color.state_text_color));
				holder.time.setTextColor(mContext.getResources().getColor(R.color.state_text_color));
			} else if (item.getState() == ImsiBean.State.IMSI_BL) {
				holder.pos.setTextColor(mContext.getResources().getColor(R.color.red));
				holder.imsi.setTextColor(mContext.getResources().getColor(R.color.red));
				holder.arfcn.setTextColor(mContext.getResources().getColor(R.color.red));
				holder.pci.setTextColor(mContext.getResources().getColor(R.color.red));
				holder.time.setTextColor(mContext.getResources().getColor(R.color.red));
			} else {
				holder.pos.setTextColor(mContext.getResources().getColor(R.color.main_text_color));
				holder.imsi.setTextColor(mContext.getResources().getColor(R.color.main_text_color));
				holder.arfcn.setTextColor(mContext.getResources().getColor(R.color.main_text_color));
				holder.pci.setTextColor(mContext.getResources().getColor(R.color.main_text_color));
				holder.time.setTextColor(mContext.getResources().getColor(R.color.main_text_color));
			}
			return convertView;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_save_data:
			if (mImsiList.size() > 0) {
				createFileDialog();
			} else {
				Util.showToast(mContext, mContext.getResources().getString(R.string.imsi_null));
			}
			break;
			
		case R.id.btn_ok:
			saveImsiList();
			break;
			
		case R.id.btn_reminder_know:
		case R.id.btn_cancel:
			closeCustomDialog();
			break;
		}
	}

	private void createFileDialog() {
		createCustomDialog();

		View view = mInflater.inflate(R.layout.dialog_create_file, null);
		
		mEtFileName = (EditText) view.findViewById(R.id.ed_file_name);
		Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
		btn_ok.setOnClickListener(this);
		Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
		btn_cancel.setOnClickListener(this);

		showCustomDialog(view, false);
	}
	
	private void closeCustomDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
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
	
	private void saveImsiList() {
		String stime = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmmss");
		String fileName = mEtFileName.getText().toString().trim();
		if (fileName.length() > 0) {
			fileName = fileName + "_" + stime;
		} else {
			fileName = "G50_" + stime;
		}
		String filePath = FileUtil.build().createOrAppendFile(mContext.getResources().getString(R.string.ed_file_name) + fileName, FileProtocol.DIR_TRACE_IMSI, fileName, 0);
		APPLog.D("saveTraceImsi(): filePath = " + filePath); 
		
		StringBuilder sb = new StringBuilder();
		int size = mImsiList.size();
		sb.append("\r\n");
		sb.append(mContext.getResources().getString(R.string.ed_file_path) + filePath);
		sb.append("\r\n");
		sb.append(mContext.getResources().getString(R.string.time));
        sb.append("\r\t");
        sb.append(mContext.getResources().getString(R.string.imsi));
        sb.append("\r\t");
        sb.append(mContext.getResources().getString(R.string.arfcn));
        sb.append("\r\t");
        sb.append(mContext.getResources().getString(R.string.pci));
        sb.append("\r\n");
        for (int i = 0; i < size; i++) {
			sb.append(mImsiList.get(i).toString());
		}
		FileUtil.build().appendFile(filePath, sb.toString());
		
		showRemindDialog(mContext.getResources().getString(R.string.reminder), mContext.getResources().getString(R.string.ed_file_path)+ filePath);
	}
	
	private void showRemindDialog(String title, String msg) {
		createCustomDialog();
		
		View view = mInflater.inflate(R.layout.dialog_reminder, null);
		TextView tv_title = (TextView) view.findViewById(R.id.title_reminder);
		TextView tv_msg = (TextView) view.findViewById(R.id.tv_reminder_msg);

		tv_title.setText(title);
		tv_msg.setText(msg);
		
		Button btn_reminder_know = (Button) view.findViewById(R.id.btn_reminder_know);
		btn_reminder_know.setOnClickListener(this);
		showCustomDialog(view, false);
	}

    public void setOnImsiDialogListener(OnImsiDialogListener listener) {
        this.listener = listener;
    }

    public interface OnImsiDialogListener {
        /**
         * 定位过程中手动切换IMSI
         * @param imsi
         */
        void onTraceImsiChange(String imsi);
    }

    private OnImsiDialogListener listener;
}
