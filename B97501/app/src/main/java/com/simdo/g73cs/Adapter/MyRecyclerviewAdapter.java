package com.simdo.g73cs.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.PrefUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MyRecyclerviewAdapter extends RecyclerView.Adapter<MyRecyclerviewAdapter.ViewHolder> {
    Context context;
    private final List<String> arfcnList = new ArrayList<>();
    String band;
    EditText lastEditText = null;

    public MyRecyclerviewAdapter(Context context, List<Integer> arfcnList, String band) {
        this.context = context;
        for (Integer integer : arfcnList) this.arfcnList.add(String.valueOf(integer));
        this.band = band;
    }

    public MyRecyclerviewAdapter(Context context, LinkedList<ArfcnPciBean> arfcnList, String band) {
        this.context = context;
        for (ArfcnPciBean bean : arfcnList) this.arfcnList.add(bean.getArfcn());
        this.band = band;
    }

    public MyRecyclerviewAdapter(Context context, String band) {
        this.context = context;
        this.arfcnList.clear();
        this.band = band;
    }

    @NonNull
    @Override
    public MyRecyclerviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_update_arfcn_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.arfcn.setText(arfcnList.get(position));
        holder.arfcn.setEnabled(false);
        lastEditText = null;
        if (position == getItemCount() - 1) {
            if (holder.arfcn.getText().toString().isEmpty()) {
                holder.arfcn.setEnabled(true);
                holder.arfcn.requestFocus();
                lastEditText = holder.arfcn;
            }
        }

        holder.btn_del.setOnClickListener(new BtnClick(position));
    }

    @Override
    public int getItemCount() {
        return arfcnList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        arfcnList.clear();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDate(List<Integer> list) {
        arfcnList.clear();
        for (Integer integer : list) {
            arfcnList.add(String.valueOf(integer));
        }
        notifyDataSetChanged();
    }

    public boolean addData(String arfcn) {
        if (!arfcnList.contains(arfcn)) {
            arfcnList.set(getItemCount() - 1, arfcn);
            return true;
        }
        return false;
    }

    public void addEditView() {
        arfcnList.add("");
        notifyDataSetChanged();
    }

    public String getLastViewText() {
        if (lastEditText != null && lastEditText.isEnabled())
            return lastEditText.getText().toString();
        return null;
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
            listener.OnDelArfcn(arfcnList.get(pos), band);
            arfcnList.remove(pos);
            if (arfcnList.size() == 0) lastEditText = null;
            notifyDataSetChanged();
        }
    }

    private String string2Json(List<String> list, String key) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        JSONObject tmpObj = null;
        for (int i = 0; i < list.size(); i++) {
            tmpObj = new JSONObject();
            tmpObj.put(key, list.get(i));
            jsonArray.put(tmpObj);
            tmpObj = null;
        }
        return jsonArray.toString();
    }

    public void removeOnDelArfcnListListener() {
        this.listener = null;
    }

    public void setOnDelArfcnListListener(OnDelArfcnListListener listener) {
        this.listener = listener;
    }

    public interface OnDelArfcnListListener {
        void OnDelArfcn(String arfcn, String band);
    }

    private OnDelArfcnListListener listener;
}
