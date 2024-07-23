package com.simdo.dw_multiple.Ui.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.dw_multiple.Bean.ScanArfcnBean;
import com.simdo.dw_multiple.R;
import com.simdo.dw_multiple.databinding.ListScanArfcnItemBinding;

import java.util.List;

public class FreqScanListAdapter extends RecyclerView.Adapter<FreqScanListAdapter.ViewHolder>{
   ListScanArfcnItemBinding binding;
   private List<ScanArfcnBean> freqList;
   private Context mContext;
   public FreqScanListAdapter(Context context,List<ScanArfcnBean> list){
      this.mContext = context;
      this.freqList = list;
   }

   @NonNull
   @Override
   public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      binding = DataBindingUtil.inflate( LayoutInflater.from(parent.getContext()), R.layout.list_scan_arfcn_item,parent,false );
      ViewHolder holder = new ViewHolder(binding);
      return holder;
   }

   @Override
   public void onBindViewHolder(@NonNull ViewHolder holder, int pos) throws Resources.NotFoundException {
      final ScanArfcnBean item = freqList.get(pos);
      holder.binding.tvTac.setText(String.valueOf(item.getTac()));
      holder.binding.tvEci.setText(String.valueOf(item.getEci()));
      holder.binding.tvUlArfcn.setText(String.valueOf(item.getUl_arfcn()));
      holder.binding.tvDlArfcn.setText(String.valueOf(item.getDl_arfcn()));
      holder.binding.tvPci.setText(String.valueOf(item.getPci()));
      holder.binding.tvRx.setText(String.valueOf(item.getRsrp()));
      holder.binding.tvPa.setText(String.valueOf(item.getPa()));
      holder.binding.tvPk.setText(String.valueOf(item.getPk()));
   }
   @Override
   public int getItemCount() {
      return freqList.size();
   }
   @SuppressLint("NotifyDataSetChanged")
   public void setFreqList(List<ScanArfcnBean> list){
      freqList.clear();
      freqList.addAll(list);
      notifyDataSetChanged();
   }
   @SuppressLint("NotifyDataSetChanged")
   public void clear(){
      freqList.clear();
      notifyDataSetChanged();
   }
   static class ViewHolder extends RecyclerView.ViewHolder{
      ListScanArfcnItemBinding binding;
      public ViewHolder (ListScanArfcnItemBinding view) {
         super( view.getRoot() );
         this.binding = view;
      }
      public ListScanArfcnItemBinding getBinding(){
         return binding;
      }
   }
}
