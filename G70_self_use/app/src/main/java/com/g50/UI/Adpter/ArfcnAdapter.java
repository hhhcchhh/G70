package com.g50.UI.Adpter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.Logcat.APPLog;
import com.Util.Util;
import com.g50.R;
import com.g50.UI.Bean.ScanArfcnBean;
import com.g50.databinding.ListArfcnItemBinding;
import com.nr70.Arfcn.Bean.LocBean;

import android.view.View.OnClickListener;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ArfcnAdapter extends RecyclerView.Adapter<ArfcnAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<ScanArfcnBean> mLocList = new ArrayList<>();
    private boolean isScan = false;
    private ListArfcnItemBinding binding;
    private boolean IsMain = true;
    public ArfcnAdapter(Context context, List<ScanArfcnBean> dataList,boolean isMain) {
        this.mInflater = LayoutInflater.from(context);
        this.mLocList.clear();
        this.mLocList.addAll((Util.sortList_op(dataList)));
        this.IsMain = isMain;
    }
    public void setArfcnList(List<ScanArfcnBean> dataList){
        this.mLocList.clear();
        this.mLocList.addAll((Util.sortList_op(dataList)));
        notifyDataSetChanged();
    }
    public void setScan(boolean isScan){
        this.isScan = isScan;
    }

    @NonNull
    @Override
    public ArfcnAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        binding = DataBindingUtil.inflate( LayoutInflater.from(parent.getContext()), R.layout.list_arfcn_item,parent,false );
        ArfcnAdapter.ViewHolder holder = new ArfcnAdapter.ViewHolder(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ArfcnAdapter.ViewHolder holder, int position) throws Resources.NotFoundException{
        if (IsMain){
            holder.binding.btnImport.setVisibility(View.INVISIBLE);
        }else {
            holder.binding.btnImport.setVisibility(View.VISIBLE);
        }
        holder.binding.tvTac.setText(mLocList.get(position).getTac());
        holder.binding.tvEci.setText(mLocList.get(position).getEci());
        holder.binding.tvArfcn.setText(String.valueOf(mLocList.get(position).getDl_arfcn()));
        holder.binding.tvPci.setText(String.valueOf(mLocList.get(position).getPci()));
        holder.binding.tvRx.setText(String.valueOf(mLocList.get(position).getRsrp()));
        holder.binding.btnImport.setOnClickListener(new ImportClick(position));
    }
    @Override
    public int getItemCount() {
        return mLocList.size();
    }

    private class ImportClick implements OnClickListener {
        private int pos;
        public ImportClick(int pos) {
            this.pos = pos;
        }
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onImportArfcn(String.valueOf(mLocList.get(pos).getDl_arfcn()),
                        String.valueOf(Util.Mode3(mLocList.get(pos).getPci())),isScan);
            }
        }
    }
    public void setOnImportArfcnListener(OnImportArfcnListener listener) {
        this.listener = listener;
    }
    public interface OnImportArfcnListener {
        void onImportArfcn(String arfcn, String pci,boolean isScan);
    }
    private OnImportArfcnListener listener;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ListArfcnItemBinding binding;
        public ViewHolder(@NonNull ListArfcnItemBinding view) {
            super(view.getRoot());
            this.binding = view;
        }
        public ListArfcnItemBinding getBinding(){
            return binding;
        }
    }
}
