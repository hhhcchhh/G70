package com.simdo.g73cs.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.R;

import java.util.List;

public class FreqResultListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
   private List<ScanArfcnBean> dataList;
   private Context context;
   private ListItemListener listener;
   public FreqResultListAdapter(Context context, List<ScanArfcnBean> dataList, ListItemListener listener){
      this.context = context;
      this.dataList = dataList;
      this.listener = listener;
   }

   @NonNull
   @Override
   public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup arg0, int arg1) {
      if (-1 == arg1) return new EmptyHolder(LayoutInflater.from(context).inflate(R.layout.item_empty, arg0, false));

      View item = LayoutInflater.from(context).inflate(R.layout.item_result_freq, arg0, false);
      return new ViewHolder(item);
   }

   @Override
   public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {

      if (holder instanceof EmptyHolder) return;

      final int position = pos;
      final ViewHolder viewHolder = (ViewHolder) holder;
      final ScanArfcnBean item = dataList.get(pos);
      viewHolder.tv_dl_arfcn.setText(String.valueOf(item.getDl_arfcn()));
      viewHolder.tv_pci.setText(String.valueOf(item.getPci()));
      viewHolder.tv_rx.setText(String.valueOf(item.getRsrp()));
      viewHolder.tv_bandwidth.setText(String.valueOf(item.getBandwidth()));
      View.OnClickListener clickListener = view -> {
         if (listener != null) listener.onItemClickListener(position);
      };
      viewHolder.tv_dl_arfcn.setOnClickListener(clickListener);
      viewHolder.tv_pci.setOnClickListener(clickListener);
      viewHolder.tv_rx.setOnClickListener(clickListener);
      viewHolder.tv_bandwidth.setOnClickListener(clickListener);
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

      TextView tv_dl_arfcn, tv_pci, tv_rx, tv_bandwidth;

      public ViewHolder(View itemView) {
         super(itemView);
         tv_dl_arfcn = itemView.findViewById(R.id.tv_dl_arfcn);
         tv_pci = itemView.findViewById(R.id.tv_pci);
         tv_rx = itemView.findViewById(R.id.tv_rx);
         tv_bandwidth = itemView.findViewById(R.id.tv_bandwidth);
      }
   }
}
