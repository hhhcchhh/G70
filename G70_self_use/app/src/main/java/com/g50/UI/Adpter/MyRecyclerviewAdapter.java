package com.g50.UI.Adpter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.Util.PrefUtil;
import com.g50.R;
import com.g50.databinding.LayoutAddArfcnListBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyRecyclerviewAdapter extends RecyclerView.Adapter<MyRecyclerviewAdapter.ViewHolder> {
    LayoutAddArfcnListBinding binding;
    Context context;
    private final List<String> arfcnList = new ArrayList<>();
    String band;
    EditText lastEditText = null;
    public MyRecyclerviewAdapter(Context context, List<Integer> arfcnList, String band){
        this.context =context;
        this.arfcnList.clear();
        this.band = band;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate( LayoutInflater.from(parent.getContext()), R.layout.layout_add_arfcn_list,parent,false );
        ViewHolder holder = new ViewHolder(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.arfcn.setText(arfcnList.get(position));
        holder.binding.arfcn.setEnabled(false);
        lastEditText = null;
        if (position == getItemCount() - 1){
            if (holder.binding.arfcn.getText().toString().isEmpty()){
                holder.binding.arfcn.setEnabled(true);
                holder.binding.arfcn.requestFocus();
                lastEditText = holder.binding.arfcn;
            }
        }

        holder.binding.btnDel.setOnClickListener(new BtnClick(position));
    }

    @Override
    public int getItemCount() {
        return arfcnList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear(){
        arfcnList.clear();
        notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void setDate(List<Integer> list){
        if (arfcnList.size()>0){
            arfcnList.clear();
        }
        for (Integer integer : list) {
            arfcnList.add(String.valueOf(integer));
        }

        notifyDataSetChanged();
    }
    public boolean addData(String arfcn){
        if (!arfcnList.contains(arfcn)){
            //arfcnList.add(arfcn);
            arfcnList.set(getItemCount()-1, arfcn);
            return true;
        }
        return false;
    }
    public void addEditView(){
        arfcnList.add("");
        notifyDataSetChanged();
    }

    public String getLastViewText(){
        if (lastEditText != null && lastEditText.isEnabled()) return lastEditText.getText().toString();
        return null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        LayoutAddArfcnListBinding binding;
        public ViewHolder (LayoutAddArfcnListBinding view) {
            super( view.getRoot() );
            this.binding = view;
        }
        public LayoutAddArfcnListBinding getBinding(){
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
            listener.OnDelArfcn(arfcnList.get(pos),band);
            arfcnList.remove(pos);
            if (arfcnList.size() == 0) lastEditText = null;
            notifyDataSetChanged();
            try {
                PrefUtil.build().setArfcn(string2Json(arfcnList,band),band);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private String string2Json(List<String> list,String key) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        JSONObject tmpObj = null;
        for(int i = 0; i < list.size(); i++) {
            tmpObj = new JSONObject();
            tmpObj.put(key , list.get(i));
            jsonArray.put(tmpObj);
            tmpObj = null;
        }
        String arfcnInfos = jsonArray.toString(); // 将JSONArray转换得到String
        return arfcnInfos;
    }

    public void removeOnDelArfcnListListener(){
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
