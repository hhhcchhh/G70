package com.simdo.g73cs.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.simdo.g73cs.Bean.CheckBoxBean;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DividerItemDecoration;

import java.util.List;

public class ArfcnListCfgAdapter extends RecyclerView.Adapter<ArfcnListCfgAdapter.ViewHolder> {
    Context mContext;
    private final List<CheckBoxBean> check_box_list;
    MyRecyclerviewAdapter adapter;
    public ArfcnListCfgAdapter(Context context, List<CheckBoxBean> check_box_list){
        this.mContext =context;
        this.check_box_list = check_box_list;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_arfcn, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        final int position = pos;
        holder.tv_text.setText(check_box_list.get(position).getText());
        adapter = new MyRecyclerviewAdapter(mContext, check_box_list.get(position).getArfcnList(), check_box_list.get(position).getText());
        adapter.setOnArfcnListListener(new MyRecyclerviewAdapter.OnArfcnListListener() {
            @Override
            public void OnDelArfcn(String arfcn, String band) {
                if (arfcn.isEmpty()) return;
                check_box_list.get(position).getArfcnList().remove(Integer.valueOf(arfcn));
            }

            @Override
            public void OnUpdateArfcn(String arfcn, int pos) {
                check_box_list.get(position).getArfcnList().set(pos, Integer.valueOf(arfcn));
            }
        });
        holder.list.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        if (holder.list.getItemDecorationCount() > 0) holder.list.removeItemDecorationAt(0);
        holder.list.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        holder.list.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return check_box_list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_text;
        RecyclerView list;
        public ViewHolder (View view) {
            super(view);
            tv_text = view.findViewById(R.id.tv_text);
            list = view.findViewById(R.id.list);
        }
    }
}
