package com.simdo.g73cs.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.PrefUtil;

import java.util.List;

public class PlmnAdapter extends RecyclerView.Adapter<PlmnAdapter.ViewHolder> {
    Context context;
    private final List<String> list;

    public PlmnAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public PlmnAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_update_arfcn_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.arfcn.setText(list.get(position));
        holder.arfcn.setEnabled(false);
        holder.btn_del.setOnClickListener(new BtnClick(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        EditText arfcn;
        ImageView btn_del;

        public ViewHolder(View view) {
            super(view);
            arfcn = view.findViewById(R.id.arfcn);
            btn_del = view.findViewById(R.id.btn_del);
        }
    }

    class BtnClick implements View.OnClickListener {
        int pos;

        public BtnClick(int pos) {
            this.pos = pos;
        }

        @Override
        public void onClick(View v) {
            list.remove(pos);
            String plmns = "";
            for (String item : list){
                plmns += item;
                plmns += ";";
            }
            if (plmns.endsWith(";")) plmns = plmns.substring(0, plmns.length() - 1);
            PrefUtil.build().putValue("cfg_plmn", plmns);
            notifyDataSetChanged();
        }
    }
}
