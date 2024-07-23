package com.simdo.g73cs.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;

import java.util.LinkedList;
import java.util.List;

public class DualRecyclerviewAdapter extends RecyclerView.Adapter<DualRecyclerviewAdapter.ViewHolder> {
    Context context;
    private final List<ArfcnPciBean> arfcnList;
    String band;
    EditText lastEditText = null;
    EditText lastPciEditText = null;
    public DualRecyclerviewAdapter(Context context, List<ArfcnPciBean> arfcnList, String band){
        this.context = context;
        this.arfcnList = arfcnList;
        int size = arfcnList.size();
        if (size > 0) {
            lastArfcn = arfcnList.get(size - 1).getArfcn();
            lastPci = arfcnList.get(size - 1).getPci();
        }
        this.band = band;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_update_arfcn_pci_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.arfcn.setText(arfcnList.get(position).getArfcn());
        holder.pci.setText(arfcnList.get(position).getPci());
        holder.arfcn.setEnabled(false);
        holder.pci.setEnabled(false);
        lastEditText = null;
        lastPciEditText = null;
        if (position == getItemCount() - 1){
            holder.arfcn.setEnabled(true);
            holder.pci.setEnabled(true);
            //holder.arfcn.requestFocus();
            lastEditText = holder.arfcn;
            lastPciEditText = holder.pci;
            if (arfcnList.get(position).getArfcn().isEmpty()) lastEditText.setText(lastArfcn);
            if (arfcnList.get(position).getPci().isEmpty()) lastPciEditText.setText(lastPci);
            lastEditText.addTextChangedListener(mTextWatcherListener);
            lastPciEditText.addTextChangedListener(mTextWatcherListener);
        }

        holder.btn_del.setOnClickListener(new BtnClick(position));
        holder.pci.setVisibility(MainActivity.getInstance().isEnablePci ? View.GONE : View.VISIBLE);
    }
    TextWatcher mTextWatcherListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            while (s.toString().startsWith("0")) s.delete(0,1);
        }
    };

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
    public void setEnablePci() {
        notifyDataSetChanged();
    }

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
    public void setDate(LinkedList<ArfcnPciBean> list) {
        if (list.size() == 0) {
            lastEditText = null;
            lastPciEditText = null;
        }
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addEditView(){
        arfcnList.add(new ArfcnPciBean("", ""));
        lastArfcn = "";
        lastPci = "";
        notifyDataSetChanged();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                lastEditText.requestFocus();
            }
        }, 500);
    }

    public ArfcnPciBean getLastViewText(){
        if (lastEditText != null && lastEditText.isEnabled()) return new ArfcnPciBean(lastEditText.getText().toString(), lastPciEditText.getText().toString());
        return null;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addImportData(String arfcn, String pci) {
        String now_arfcn = lastEditText.getText().toString();
        String now_pci = lastPciEditText.getText().toString();
        ArfcnPciBean bean = new ArfcnPciBean(arfcn, pci);
        if (now_arfcn.isEmpty() || now_pci.isEmpty()) addData(bean);
        else arfcnList.add(bean);
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
            Spanned spanned1 = Html.fromHtml("arfcn");
            Spanned spanned2 = Html.fromHtml("pci");
            arfcn.setHint(spanned1);
            pci.setHint(spanned2);
            btn_del = view.findViewById(R.id.btn_del);
        }
    }
    String lastArfcn = "";
    String lastPci = "";

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
            if (pos != arfcnList.size() - 1){
                lastArfcn = lastEditText.getText().toString();
                lastPci = lastPciEditText.getText().toString();
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
