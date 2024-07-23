package com.g50.UI.Adpter;

import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.g50.R;
import com.nr70.Gnb.Bean.UeidBean;

@SuppressLint({ "ViewHolder", "InflateParams" })
public class BlackListAdapter extends BaseAdapter {
	private Context mContext;
	private LayoutInflater mInflater;
	private List<UeidBean> blackList;

	private ItemClickInterface mItemClickInterface;
	public BlackListAdapter(Context context, List<UeidBean> list, ItemClickInterface itemClickInterface) {
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		this.blackList = list;
		this.mItemClickInterface = itemClickInterface;
	}

	@Override
	public int getCount() {
		return blackList.size();
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
	public View getView(final int position, View convertView, ViewGroup viewGroup) {
		convertView = mInflater.inflate(R.layout.layout_bl_list_item, null);
		TextView tv = (TextView) convertView.findViewById(R.id.tv);
		String s = position + 1 + ". " + blackList.get(position).getImsi();
		tv.setText(s);
		tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mItemClickInterface!=null) mItemClickInterface.ItemClick(position);
			}
		});
		Button btn_del = (Button) convertView.findViewById(R.id.btn_del);
		btn_del.setOnClickListener(new BtnClick(position));
		return convertView;
	}

	public interface ItemClickInterface{
		void ItemClick(int pos);
	}

	class BtnClick implements OnClickListener {
		int pos;

		public BtnClick(int pos) {
			this.pos = pos;
		}

		@Override
		public void onClick(View v) {
			blackList.remove(pos);
			notifyDataSetChanged();
		}
	}
}
