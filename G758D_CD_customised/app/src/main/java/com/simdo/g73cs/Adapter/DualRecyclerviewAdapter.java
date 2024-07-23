package com.simdo.g73cs.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.R;

import java.util.List;

public class DualRecyclerviewAdapter extends RecyclerView.Adapter<DualRecyclerviewAdapter.ViewHolder> {
    Context context;
    private final List<ArfcnPciBean> arfcnPciList;
    int cell_id;
    EditText lastEditText = null;       //保存最后一个输入的editText
    EditText lastPciEditText = null;    //保存最后一个输入的pciEditText
//    boolean firstOpen = true;
    boolean showSoftInput = false;


    boolean isWorking;

    public DualRecyclerviewAdapter(Context context, List<ArfcnPciBean> arfcnPciList, int cell_id,
                                   boolean isWorking) {
        this.context = context;
        this.arfcnPciList = arfcnPciList;
        this.cell_id = cell_id;
        this.isWorking = isWorking;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_update_arfcn_pci_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.arfcn.setText(arfcnPciList.get(position).getArfcn());
        holder.pci.setText(arfcnPciList.get(position).getPci());

        if (isWorking) {
            holder.arfcn.setEnabled(false);
            holder.pci.setEnabled(false);
            return;
        }

        holder.arfcn.setEnabled(false);
        holder.pci.setEnabled(false);
//        lastEditText = null;
//        lastPciEditText = null;
        if (position == getItemCount() - 1) {
            holder.arfcn.setEnabled(focusable);
            holder.pci.setEnabled(focusable);
            if (showSoftInput){
                holder.arfcn.requestFocus();
                new Handler().postDelayed(() -> {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(holder.arfcn, InputMethodManager.SHOW_IMPLICIT);
                }, 200);
                showSoftInput(false);
            }
            lastEditText = holder.arfcn;
            lastPciEditText = holder.pci;
        }
        holder.btn_del.setOnClickListener(new BtnClick(position));
    }

    @Override
    public int getItemCount() {
        return arfcnPciList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        arfcnPciList.clear();
        notifyDataSetChanged();
    }

    public boolean addData(ArfcnPciBean arfcnPciBean) {
        if (getItemCount() == 0) {
            arfcnPciList.add(arfcnPciBean);
            notifyDataSetChanged();
            return true;
        }
//        if (!isListContains(arfcnPciList, arfcnPciBean)) {
        arfcnPciList.set(getItemCount() - 1, arfcnPciBean);
        notifyItemChanged(getItemCount() - 1);
        return true;
//        }
//        return false;
    }

    public boolean addDataToStart(ArfcnPciBean arfcnPciBean) {
        if (getItemCount() == 0) {
            arfcnPciList.add(arfcnPciBean);
            notifyDataSetChanged();
            return true;
        }
//        if (!isListContains(arfcnPciList, arfcnPciBean)) {
        arfcnPciList.set(getItemCount() - 1, arfcnPciBean);
        notifyItemChanged(getItemCount() - 1);
        return true;
//        }
//        return false;
    }

    public boolean isListContains(List<ArfcnPciBean> list, ArfcnPciBean bean) {
        for (ArfcnPciBean arfcnPciBean : list) {
            if (arfcnPciBean.getArfcn().equals(bean.getArfcn()) && arfcnPciBean.getPci().equals(bean.getPci())) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addEditView() {
        arfcnPciList.add(new ArfcnPciBean("", ""));
        notifyDataSetChanged();
    }

    public ArfcnPciBean getLastViewText() {
//        if (lastEditText != null && lastEditText.isEnabled())
        if (lastEditText != null)
            return new ArfcnPciBean(lastEditText.getText().toString(), lastPciEditText.getText().toString());
        return null;
    }

    private Boolean focusable = true;

    private Boolean clickAddBtn = false;

    public Boolean getFocusable() {
        return focusable;
    }

    public void setFocusable(Boolean focusable) {
        this.focusable = focusable;
        notifyDataSetChanged();
    }

    public Boolean getClickAddBtn() {
        return clickAddBtn;
    }

    public void setClickAddBtn(Boolean clickAddBtn) {
        this.clickAddBtn = clickAddBtn;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addImportDataToStart(String arfcn, String pci) {
        ArfcnPciBean bean = new ArfcnPciBean(arfcn, pci);
        arfcnPciList.add(0, bean);
        notifyDataSetChanged();
    }

    public void showSoftInput(boolean b) {
        showSoftInput = b;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        EditText arfcn;
        EditText pci;
        ImageView btn_del;

        public ViewHolder(View view) {
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

    class BtnClick implements View.OnClickListener {
        int pos;

        public BtnClick(int pos) {
            this.pos = pos;
        }

        @Override
        public void onClick(View v) {
            if (!focusable) return;
            if (arfcnPciList.size() == 1) {     //最后一个的时候默认为空
                arfcnPciList.remove(pos);
                arfcnPciList.add(new ArfcnPciBean("", ""));
                notifyDataSetChanged();
                return;
            }
            if (arfcnPciList.size() > 1) {
                if (pos == getItemCount() - 1) {    //删除最后一个
                    listener.setIsChangingTrue(cell_id);
                    arfcnPciList.remove(pos);
                    notifyDataSetChanged();
                    return;
                }
                arfcnPciList.remove(pos);
                //把最后一个加进去
                if (clickAddBtn) { //点了添加按钮要把最后那个空的删了
                    arfcnPciList.remove(arfcnPciList.size() - 1);
                    clickAddBtn = false;
                    arfcnPciList.add(new ArfcnPciBean(lastEditText.getText().toString(), lastPciEditText.getText().toString()));
                }
                listener.setIsChangingTrue(cell_id);
            }
            notifyDataSetChanged();
        }
    }

    public void removeOnDelArfcnListListener() {
        this.listener = null;
    }

    public void setOnDelArfcnListListener(OnDelArfcnListListener listener) {
        this.listener = listener;
    }

    public interface OnDelArfcnListListener {
        void setIsChangingTrue(int bandStr);
    }

    private OnDelArfcnListListener listener;
}
