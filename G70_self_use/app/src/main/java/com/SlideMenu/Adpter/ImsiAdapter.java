package com.SlideMenu.Adpter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.g50.R;
import com.nr70.Arfcn.Bean.LocBean;

import java.util.List;

@SuppressLint({ "ViewHolder", "InflateParams" })
public class ImsiAdapter extends BaseAdapter {
	private Context mContext;
	private LayoutInflater mInflater;
	private Handler mHandler;
	private List<LocBean> mTextList;

	public ImsiAdapter(Context context, Handler handler, List<LocBean> mTextList) {
		this.mContext = context;
		this.mHandler = handler;
		this.mInflater = LayoutInflater.from(context);
		this.mTextList = mTextList;
	}

	@Override
	public int getCount() {
		return mTextList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		convertView = mInflater.inflate(R.layout.list_text_item, null);
		TextView tv = (TextView) convertView.findViewById(R.id.tv);
		
		String s = position + 1 + "、";
		s = s + mTextList.get(position).getArfcn() + " - " + mTextList.get(position).getPci();
		tv.setText(s);
		Button btn_del = (Button) convertView.findViewById(R.id.btn_del);
		btn_del.setOnClickListener(new BtnClick(position));
		return convertView;
	}

	class BtnClick implements OnClickListener {
		int pos;

		public BtnClick(int pos) {
			this.pos = pos;
		}

		@Override
		public void onClick(View v) {
				mTextList.remove(pos);
				notifyDataSetChanged();
		}
	}

	public void setList(List<LocBean> mImsiList) {
		this.mTextList = mImsiList;
		notifyDataSetChanged();
	};
}
