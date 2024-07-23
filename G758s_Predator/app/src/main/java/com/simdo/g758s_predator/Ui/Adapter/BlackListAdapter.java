package com.simdo.g758s_predator.Ui.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.dwdbsdk.Bean.UeidBean;
import com.simdo.g758s_predator.Bean.ImsiBean;
import com.simdo.g758s_predator.R;
import com.simdo.g758s_predator.Util.DateUtil;
import com.simdo.g758s_predator.databinding.DialogBlackListItemBinding;
import com.simdo.g758s_predator.databinding.DialogImsiListItemBinding;

import java.util.List;

public class BlackListAdapter extends RecyclerView.Adapter<BlackListAdapter.ViewHolder> {
    DialogBlackListItemBinding binding;
    Context mContext;
    List<UeidBean> blackList;

    public BlackListAdapter(Context mContext, List<UeidBean> blackList){
        this.mContext =mContext;
        this.blackList = blackList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate( LayoutInflater.from(parent.getContext()), R.layout.dialog_black_list_item,parent,false );
        ViewHolder holder = new ViewHolder(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UeidBean item = this.blackList.get(position);
        holder.binding.tv.setText(item.getImsi() );
        holder.binding.btnDel.setOnClickListener(new BtnClick(position));
    }

    @Override
    public int getItemCount() {
        return this.blackList.size();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void clear(){
        this.blackList.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        DialogBlackListItemBinding binding;
        public ViewHolder (DialogBlackListItemBinding view) {
            super( view.getRoot() );
            this.binding = view;
        }
        public DialogBlackListItemBinding getBinding(){
            return binding;
        }
    }
    class BtnClick implements View.OnClickListener {
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
