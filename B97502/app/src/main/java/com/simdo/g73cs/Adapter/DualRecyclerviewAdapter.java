package com.simdo.g73cs.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.PrefUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class DualRecyclerviewAdapter extends RecyclerView.Adapter<DualRecyclerviewAdapter.ViewHolder> {
    Context context;
    private final List<ArfcnPciBean> arfcnList;
    String band;
    EditText lastEditText = null;
    EditText lastPciEditText = null;
    boolean isWorking;
    public DualRecyclerviewAdapter(Context context, List<ArfcnPciBean> arfcnList, String band, boolean isWorking){
        this.context = context;
        this.arfcnList = arfcnList;
        this.band = band;
        this.isWorking = isWorking;
    }
    @NonNull
    @Override
    public DualRecyclerviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_update_arfcn_pci_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.arfcn.setText(arfcnList.get(position).getArfcn());
        holder.pci.setText(arfcnList.get(position).getPci());
        if (isWorking){
            holder.arfcn.setEnabled(false);
            holder.pci.setEnabled(false);
            return;
        }
        holder.arfcn.setEnabled(false);
        holder.pci.setEnabled(false);
        lastEditText = null;
        lastPciEditText = null;
        if (position == getItemCount() - 1){
            holder.arfcn.setEnabled(true);
            holder.pci.setEnabled(true);
            holder.arfcn.requestFocus();
            lastEditText = holder.arfcn;
            lastPciEditText = holder.pci;
        }
        holder.btn_del.setOnClickListener(new BtnClick(position));
        holder.pci.setVisibility(isEnablePci ? View.GONE : View.VISIBLE);
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
    public boolean addData(ArfcnPciBean arfcn){
        if (getItemCount() == 0){
            arfcnList.add(arfcn);
            notifyDataSetChanged();
            return true;
        }
        if (arfcnList.size() == 1) {
            arfcnList.set(0, arfcn);
            notifyItemChanged(0);
            return true;
        }
        for (int i = 0; i < arfcnList.size() - 1; i++){
            if (arfcnList.get(i).getArfcn().equals(arfcn.getArfcn()) && arfcnList.get(i).getPci().equals(arfcn.getPci())) return false;
        }
        arfcnList.set(getItemCount() - 1, arfcn);
        notifyItemChanged(getItemCount() - 1);
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addEditView(){
        arfcnList.add(new ArfcnPciBean("", ""));
        notifyDataSetChanged();
    }

    public ArfcnPciBean getLastViewText(){
        if (lastEditText != null && lastEditText.isEnabled()) return new ArfcnPciBean(lastEditText.getText().toString(), lastPciEditText.getText().toString());
        return null;
    }

    @SuppressLint("NotifyDataSetChanged")
    public boolean addImportData(String arfcn, String pci) {
        ArfcnPciBean bean = new ArfcnPciBean(arfcn, pci);
        return addData(bean);
    }

    boolean isEnablePci = false;
    @SuppressLint("NotifyDataSetChanged")
    public void setEnablePci(boolean enable) {
        isEnablePci = enable;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        EditText arfcn;
        EditText pci;
        ImageView btn_del;
        public ViewHolder (View view) {
            super(view);
            arfcn = view.findViewById(R.id.arfcn);
            pci = view.findViewById(R.id.pci);
            Spanned spanned1 = Html.fromHtml("Arfcn");
            Spanned spanned2 = Html.fromHtml("PCI");
            arfcn.setHint(spanned1);
            pci.setHint(spanned2);
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
            if (arfcnList.size() == 1) {
                lastEditText.setText("");
                lastPciEditText.setText("");
                return;
            }
            listener.OnDelArfcn(arfcnList.get(pos), band);
            arfcnList.remove(pos);
            notifyDataSetChanged();
        }
    }

    public void removeOnDelArfcnListListener(){
        this.listener = null;
    }
    public void setOnDelArfcnListListener(OnDelArfcnListListener listener) {
        this.listener = listener;
    }

    public interface OnDelArfcnListListener {
        void OnDelArfcn(ArfcnPciBean bean, String band);
    }

    private OnDelArfcnListListener listener;
}
