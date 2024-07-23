package com.simdo.g73cs.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.R;

import java.util.List;

public class FreqScanListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
   private List<ScanArfcnBean> dataList;
   private Context context;
   public FreqScanListAdapter(Context context, List<ScanArfcnBean> dataList){
      this.dataList = dataList;
      this.context = context;
   }

   @NonNull
   @Override
   public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup arg0, int arg1) {
      if (-1 == arg1) return new EmptyHolder(LayoutInflater.from(context).inflate(R.layout.item_empty, arg0, false));

      View item = LayoutInflater.from(context).inflate(R.layout.item_freq, arg0, false);
      return new ViewHolder(item);
   }

   @Override
   public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {

      if (holder instanceof EmptyHolder) return;

      final ViewHolder viewHolder = (ViewHolder) holder;
      final ScanArfcnBean item = dataList.get(pos);
      viewHolder.tv_tac.setText(String.valueOf(item.getTac()));
      viewHolder.tv_eci.setText(String.valueOf(item.getEci()));
      viewHolder.tv_ul_arfcn.setText(String.valueOf(item.getUl_arfcn()));
      viewHolder.tv_dl_arfcn.setText(String.valueOf(item.getDl_arfcn()));
      viewHolder.tv_pci.setText(String.valueOf(item.getPci()));
      viewHolder.tv_rx.setText(String.valueOf(item.getRsrp()));
      viewHolder.tv_pa.setText(String.valueOf(item.getPa()));
      viewHolder.tv_pk.setText(String.valueOf(item.getPk()));
      viewHolder.tv_bandwidth.setText(String.valueOf(item.getBandwidth()));
   }
   @Override
   public int getItemCount() {
      return dataList.size()>0?dataList.size():1;//不同点4,也是关键点
   }

   @Override
   public int getItemViewType(int position) {
      if(dataList.size()<= 0 ) return -1;
      return super.getItemViewType(position);
   }

   /**
    * 空数据显示的   不同点1
    */
   static class EmptyHolder extends RecyclerView.ViewHolder{

      public EmptyHolder(View itemView) {
         super(itemView);
      }

   }

   static class ViewHolder extends RecyclerView.ViewHolder{

      TextView tv_tac, tv_eci, tv_ul_arfcn, tv_dl_arfcn, tv_pci, tv_rx, tv_pa, tv_pk, tv_bandwidth;

      public ViewHolder(View itemView) {
         super(itemView);
         tv_tac = itemView.findViewById(R.id.tv_tac);
         tv_eci = itemView.findViewById(R.id.tv_eci);
         tv_ul_arfcn = itemView.findViewById(R.id.tv_ul_arfcn);
         tv_dl_arfcn = itemView.findViewById(R.id.tv_dl_arfcn);
         tv_pci = itemView.findViewById(R.id.tv_pci);
         tv_rx = itemView.findViewById(R.id.tv_rx);
         tv_pa = itemView.findViewById(R.id.tv_pa);
         tv_pk = itemView.findViewById(R.id.tv_pk);
         tv_bandwidth = itemView.findViewById(R.id.tv_bandwidth);

      }
   }
}
