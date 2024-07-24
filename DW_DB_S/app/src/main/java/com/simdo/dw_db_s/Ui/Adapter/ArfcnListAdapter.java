package com.simdo.dw_db_s.Ui.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dwdbsdk.Bean.DB.ArfcnBeanSsb;
import com.dwdbsdk.Bean.DB.DBSupportArfcn;
import com.dwdbsdk.Bean.NrBand;

import com.simdo.dw_db_s.Bean.ArfcnBean;
import com.simdo.dw_db_s.R;

import java.util.ArrayList;
import java.util.List;

@SuppressLint({ "ViewHolder", "InflateParams" })
public class ArfcnListAdapter extends RecyclerView.Adapter<ArfcnListAdapter.ViewHolder> {
	private Context mContext;
	private LayoutInflater mInflater;
	private List<ArfcnBeanSsb> list = new ArrayList<>();
	public ArfcnListAdapter(Context context, List<ArfcnBeanSsb> dataList) {

		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		this.list.clear();
		this.list.addAll(dataList);
	}
	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view= mInflater.inflate(R.layout.layout_arfcn_item, null);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//		ArfcnBean arfcnBean = new ArfcnBean(list.get(position).getArfcn(), DBSupportArfcn.build().getPa(list.get(position).getArfcn()),DBSupportArfcn.build().getPk(list.get(position).getArfcn()));
		ArfcnBean arfcnBean = new ArfcnBean(list.get(position).getArfcn());
		holder.afrcn.setText(String.valueOf(list.get(position).getArfcn()));
		if (NrBand.earfcn2band(list.get(position).getArfcn())==1|| NrBand.earfcn2band(list.get(position).getArfcn())==28){
			holder.freqCarrier.setText(String.valueOf(arfcnBean.getULArfcn()));
		}else {
			holder.freqCarrier.setText("");
		}

		holder.time_offset.setText(String.valueOf(list.get(position).getTimeOffset()));
		holder.pa.setText(String.valueOf(list.get(position).getPa()));
		holder.pk.setText(String.valueOf(list.get(position).getPk()));
	}

	@Override
	public int getItemCount() {
		Log.i("","getItemCount "+list.size());
		return list.size();
	}

	static class ViewHolder extends RecyclerView.ViewHolder{
		TextView afrcn,freqCarrier,time_offset,pa,pk;
		public ViewHolder (View view) {
			super(view);
			this.afrcn = view.findViewById(R.id.arfcn);
			this.freqCarrier = view.findViewById(R.id.freqCarrier);
			this.time_offset = view.findViewById(R.id.time_offset);
			this.pa = view.findViewById(R.id.pa);
			this.pk = view.findViewById(R.id.pk);
		}
	}
}
