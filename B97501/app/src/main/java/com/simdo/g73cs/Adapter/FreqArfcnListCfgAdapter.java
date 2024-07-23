package com.simdo.g73cs.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.simdo.g73cs.Bean.ArfcnBean;
import com.simdo.g73cs.Bean.FreqArfcnBean;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.DividerItemDecoration;
import com.simdo.g73cs.Util.PrefUtil;

import java.util.List;

public class FreqArfcnListCfgAdapter extends RecyclerView.Adapter<FreqArfcnListCfgAdapter.ViewHolder> {
    Context mContext;
    private final List<FreqArfcnBean> check_box_list;
    public FreqArfcnListCfgAdapter(Context context, List<FreqArfcnBean> check_box_list){
        this.mContext =context;
        this.check_box_list = check_box_list;
    }
    @NonNull
    @Override
    public FreqArfcnListCfgAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_arfcn, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int pos = position;
        holder.cb_text.setText(check_box_list.get(pos).getBand());
        holder.cb_text.setChecked(check_box_list.get(pos).isChecked());
        final CheckBoxRecyclerviewAdapter adapter = new CheckBoxRecyclerviewAdapter(mContext, check_box_list.get(pos).getArfcnList(), new ListItemListener() {
            @Override
            public void onItemClickListener(int p) {
                boolean isAllChecked = true;
                for (ArfcnBean bean : check_box_list.get(pos).getArfcnList()) {
                    if (bean.isChecked()) continue;
                    isAllChecked = false;
                    break;
                }
                PrefUtil.build().putFreqArfcnList("freq_" + check_box_list.get(pos).getBand(), check_box_list.get(pos).getArfcnList());
                holder.cb_text.setChecked(isAllChecked);
                check_box_list.get(pos).setChecked(isAllChecked);
            }
        });

        holder.list.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));

        holder.list.setAdapter(adapter);

        holder.cb_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isAllChecked = holder.cb_text.isChecked();
                for (int i = 0; i < check_box_list.get(pos).getArfcnList().size(); i++) {
                    check_box_list.get(pos).getArfcnList().get(i).setChecked(isAllChecked);
                }
                check_box_list.get(pos).setChecked(isAllChecked);
                adapter.setChecked(isAllChecked);
                PrefUtil.build().putFreqArfcnList("freq_" + check_box_list.get(pos).getBand(), check_box_list.get(pos).getArfcnList());
            }
        });
    }

    @Override
    public int getItemCount() {
        return check_box_list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        CheckBox cb_text;
        RecyclerView list;
        public ViewHolder (View view) {
            super(view);
            cb_text = view.findViewById(R.id.cb_text);
            list = view.findViewById(R.id.list);
        }
    }
}
