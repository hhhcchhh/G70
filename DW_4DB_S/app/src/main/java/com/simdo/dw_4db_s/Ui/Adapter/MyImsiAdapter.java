package com.simdo.dw_4db_s.Ui.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.dw_4db_s.Bean.ImsiBean;
import com.simdo.dw_4db_s.R;
import com.simdo.dw_4db_s.Util.DateUtil;
import com.simdo.dw_4db_s.databinding.DialogImsiListItemBinding;

import java.util.List;

public class MyImsiAdapter extends RecyclerView.Adapter<MyImsiAdapter.ViewHolder> {
    DialogImsiListItemBinding binding;
    Context mContext;
    private List<ImsiBean> mImsiList;

    public MyImsiAdapter(Context mContext, List<ImsiBean> ImsiList){
        this.mContext =mContext;
        this.mImsiList = ImsiList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate( LayoutInflater.from(parent.getContext()), R.layout.dialog_imsi_list_item,parent,false );
        ViewHolder holder = new ViewHolder(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImsiBean item = mImsiList.get(position);
        if (item.getCellId() == 1||item.getCellId() == 3) {
            holder.binding.tvCellId.setBackgroundResource(R.mipmap.ic_cell_2);
        } else {
            holder.binding.tvCellId.setBackgroundResource(R.mipmap.ic_cell_1);
        }
//        holder.binding.tvCellId.setText(mImsiList.get(position).getCellId());
        holder.binding.tvImsi.setText(mImsiList.get( position ).getImsi() );
        holder.binding.tvArfcn.setText(mImsiList.get( position ).getArfcn() );
        holder.binding.tvPci.setText( mImsiList.get( position ).getPci() );
        holder.binding.tvFtime.setText(DateUtil.formateTimeHMS(item.getFirstTime()));
        holder.binding.tvLtime.setText(DateUtil.formateTimeHMS(item.getLatestTime()));
//        holder.binding.tvFtime.setVisibility(View.GONE);
        if (item.getState() == ImsiBean.State.IMSI_OLD) {
            // 上一次定位到的IMSI，此次未定位到但在黑名单中的，则显示蓝色
            holder.binding.tvImsi.setTextColor(mContext.getResources().getColor(R.color.state_text_color));
            holder.binding.tvArfcn.setTextColor(mContext.getResources().getColor(R.color.state_text_color));
            holder.binding.tvPci.setTextColor(mContext.getResources().getColor(R.color.state_text_color));
            holder.binding.tvLtime.setTextColor(mContext.getResources().getColor(R.color.state_text_color));
        } else if (item.getState() == ImsiBean.State.IMSI_BL) {
            holder.binding.tvImsi.setTextColor(mContext.getResources().getColor(R.color.red));
            holder.binding.tvArfcn.setTextColor(mContext.getResources().getColor(R.color.red));
            holder.binding.tvPci.setTextColor(mContext.getResources().getColor(R.color.red));
            holder.binding.tvLtime.setTextColor(mContext.getResources().getColor(R.color.red));
        } else {
            holder.binding.tvImsi.setTextColor(mContext.getResources().getColor(R.color.main_text_color));
            holder.binding.tvArfcn.setTextColor(mContext.getResources().getColor(R.color.main_text_color));
            holder.binding.tvPci.setTextColor(mContext.getResources().getColor(R.color.main_text_color));
            holder.binding.tvFtime.setTextColor(mContext.getResources().getColor(R.color.main_text_color));
            holder.binding.tvLtime.setTextColor(mContext.getResources().getColor(R.color.main_text_color));
        }
        holder.itemView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        } );
    }

    @Override
    public int getItemCount() {
        return mImsiList.size();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void clear(){
        mImsiList.clear();
        notifyDataSetChanged();
    }
    public void setTempData(List<ImsiBean> data) {
        mImsiList.clear();
        mImsiList.addAll(data);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        DialogImsiListItemBinding binding;
        public ViewHolder (DialogImsiListItemBinding view) {
            super( view.getRoot() );
            this.binding = view;
        }
        public DialogImsiListItemBinding getBinding(){
            return binding;
        }
    }

}
