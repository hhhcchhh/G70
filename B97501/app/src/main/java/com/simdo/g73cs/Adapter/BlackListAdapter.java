package com.simdo.g73cs.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.R;

import java.util.List;

@SuppressLint({ "ViewHolder", "InflateParams" })
public class BlackListAdapter extends RecyclerView.Adapter<BlackListAdapter.ViewHolder> {
	private List<MyUeidBean> blackList;
	private final ListItemListener listener;

	public BlackListAdapter(Context context, List<MyUeidBean> list, ListItemListener listener) {
		//由于上一次保存的有可能不是应用退出前的列表，因此需要清楚上一次保存的勾选状态
		blackList = list;
		for (int i=0; i< blackList.size(); i++){
			blackList.get(i).setFirstChecked(false);
			blackList.get(i).setSecondChecked(false);
		}
		this.listener = listener;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(viewGroup.getContext())
				.inflate(R.layout.black_list_item, viewGroup, false);

		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder viewHolder, int pos) {
		final int position = pos;
		String name = blackList.get(position).getName();
		viewHolder.tv_black_name.setText(name);

		viewHolder.tv_black_name.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				if (listener!=null) listener.onItemClickListener(position);
				return false;
			}
		});

		String imsi = blackList.get(position).getUeidBean().getImsi();
		viewHolder.tv_black_imsi.setText(imsi);

		viewHolder.tv_black_imsi.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				if (listener!=null) listener.onItemClickListener(position);
				return false;
			}
		});

	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}


	@Override
	public int getItemCount() {
		return blackList.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		TextView tv_black_name;
		TextView tv_black_imsi;
		public ViewHolder(View view) {
			super(view);
			tv_black_name = view.findViewById(R.id.tv_black_name);

			tv_black_imsi = view.findViewById(R.id.tv_black_imsi);
		}
	}
}
