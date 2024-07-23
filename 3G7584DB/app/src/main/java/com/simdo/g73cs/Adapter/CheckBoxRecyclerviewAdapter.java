package com.simdo.g73cs.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.Bean.CheckBoxBean;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.PrefUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CheckBoxRecyclerviewAdapter extends RecyclerView.Adapter<CheckBoxRecyclerviewAdapter.ViewHolder> {
    Context context;
    private final List<CheckBoxBean> check_box_list;
    private final ListItemListener listener;
    public CheckBoxRecyclerviewAdapter(Context context, List<CheckBoxBean> check_box_list, ListItemListener listener){
        this.context =context;
        this.check_box_list = check_box_list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_check_box, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        final int position = pos;
        holder.check_box.setText(check_box_list.get(position).getText());
        holder.check_box.setChecked(check_box_list.get(position).isChecked());
        holder.check_box.setOnClickListener(view -> {
            check_box_list.get(position).setChecked(!check_box_list.get(position).isChecked());
            if (listener != null) listener.onItemClickListener(position);
        });
        holder.check_box.setEnabled(isEnable);
    }

    @Override
    public int getItemCount() {
        return check_box_list.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setChecked(boolean b) {
        for (int i = 0; i < check_box_list.size(); i++) check_box_list.get(i).setChecked(b);
        notifyDataSetChanged();
    }

    boolean isEnable = true;
    @SuppressLint("NotifyDataSetChanged")
    public void setEnable(boolean b) {
        isEnable = b;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        CheckBox check_box;
        public ViewHolder (View view) {
            super(view);
            check_box = view.findViewById(R.id.check_box);
        }
    }
}
