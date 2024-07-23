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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;

import java.util.List;

public class DualRecyclerviewAdapter extends RecyclerView.Adapter<DualRecyclerviewAdapter.ViewHolder> {
    Context context;
    private final List<ArfcnPciBean> arfcnPciList;
    String band;
    EditText lastEditText = null;       //保存最后一个输入的editText
    EditText lastPciEditText = null;    //保存最后一个输入的pciEditText
    //    boolean firstOpen = true;
    boolean showSoftInput = false;

    TextWatcher lastTextWatcher = null;
    TextWatcher lastPciTextWatcher = null;

    boolean isWorking;

    public DualRecyclerviewAdapter(Context context, List<ArfcnPciBean> arfcnPciList, String band,
                                   boolean isWorking) {
        this.context = context;
        this.arfcnPciList = arfcnPciList;
        this.band = band;
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
//        AppLog.D("onBindViewHolder" + position);
        holder.arfcn.setText(arfcnPciList.get(position).getArfcn());
        holder.pci.setText(arfcnPciList.get(position).getPci());
        banLeadingZero(holder.pci);
        banLeadingZero(holder.arfcn);
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
            AppLog.D("position == getItemCount() - 1");
            holder.arfcn.setEnabled(focusable);
            holder.pci.setEnabled(focusable);
            if (showSoftInput) {
                holder.arfcn.requestFocus();
                new Handler().postDelayed(() -> {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(holder.arfcn, InputMethodManager.SHOW_IMPLICIT);
                }, 200);
                showSoftInput(false);
            }
            lastEditText = holder.arfcn;
            lastPciEditText = holder.pci;
            // 检查是否已经为最后一项添加了TextWatcher
            if (lastTextWatcher == null) {
                lastTextWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (holder.getAdapterPosition() == getItemCount() - 1) {   //保证是最后一个才触发
                            AppLog.D("afterTextChanged" + s.toString() + "clickAddBtnPci" + clickAddBtnPci + "clickAddBtnArfcn" + clickAddBtnArfcn);
                            if (!clickAddBtnPci || !clickAddBtnArfcn) {   //没有点击添加按钮才做这个事，防止第一次点击之后自动填充上一个数据的bug
                                if (!arfcnPciList.isEmpty() && !s.toString().equals(arfcnPciList.get(0).getArfcn()))
                                    arfcnPciList.get(getItemCount() - 1).setArfcn(s.toString());
                            }
                            if (clickAddBtnArfcn) clickAddBtnArfcn = false;
                            AppLog.D("afterTextChanged" + s.toString() + "clickAddBtnPci" + clickAddBtnPci + "clickAddBtnArfcn" + clickAddBtnArfcn);
                        }
                    }
                };

                lastEditText.addTextChangedListener(lastTextWatcher);
            }

            if (lastPciTextWatcher == null) {
                lastPciTextWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        AppLog.D("beforeTextChanged" + s.toString());
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        AppLog.D("onTextChanged" + s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (holder.getAdapterPosition() == getItemCount() - 1) {   //保证是最后一个才触发
                            AppLog.D("afterTextChanged" + s.toString() + "clickAddBtnPci" + clickAddBtnPci + "clickAddBtnArfcn" + clickAddBtnArfcn);
                            if (!clickAddBtnPci || !clickAddBtnArfcn) {   //没有点击添加按钮才做这个事，防止第一次点击之后自动填充上一个数据的bug
                                if (!arfcnPciList.isEmpty() && !s.toString().equals(arfcnPciList.get(0).getPci()))  //防止自动填充第一个数据的bug
                                    arfcnPciList.get(getItemCount() - 1).setPci(s.toString());  //点击开始之后会执行这个导致最后一个
                            }
                            if (clickAddBtnPci) clickAddBtnPci = false;
                            AppLog.D("afterTextChanged" + s.toString() + "clickAddBtnPci" + clickAddBtnPci + "clickAddBtnArfcn" + clickAddBtnArfcn);
                        }
                    }
                };

                lastPciEditText.addTextChangedListener(lastPciTextWatcher);
            }
        } else {
//            AppLog.D("position != getItemCount() - 1");
            // 如果不是最后一项，移除TextWatcher
            if (lastTextWatcher != null) {
                lastEditText.removeTextChangedListener(lastTextWatcher);
                lastTextWatcher = null;
            }

            if (lastPciTextWatcher != null) {
                lastPciEditText.removeTextChangedListener(lastPciTextWatcher);
                lastPciTextWatcher = null;
            }
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

    private Boolean clickAddBtnArfcn = false;
    private Boolean clickAddBtnPci = false;

    public Boolean getFocusable() {
        return focusable;
    }

    public void setFocusable(Boolean focusable) {
        this.focusable = focusable;
        notifyDataSetChanged();
    }

    public Boolean getClickAddBtnArfcn() {
        return clickAddBtnArfcn;
    }

    public void setClickAddBtn(Boolean clickAddBtn) {
        this.clickAddBtnArfcn = clickAddBtn;
        this.clickAddBtnPci = clickAddBtn;
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
                listener.refreshRVHeight();
                return;
            }
            if (arfcnPciList.size() > 1) {
                if (pos == getItemCount() - 1) {    //删除最后一个
                    AppLog.D("pos == getItemCount() - 1");
                    arfcnPciList.remove(pos);
//                    lastEditText.setText("");
//                    lastPciEditText.setText("");
                    listener.setIsChangingTrue();
                    listener.refreshRVHeight();
                    notifyDataSetChanged();
                    return;
                } else if (pos > getItemCount() - 1) {
                    AppLog.D("pos > getItemCount() - 1" + pos + "  " + getItemCount());
                    notifyDataSetChanged();
                    return;
                }
                AppLog.D("pos < getItemCount() - 1");
                arfcnPciList.remove(pos);
                //把最后一个加进去
                if (clickAddBtnArfcn) { //点了添加按钮要把最后那个空的删了
                    arfcnPciList.remove(arfcnPciList.size() - 1);
                    clickAddBtnArfcn = false;
                    arfcnPciList.add(new ArfcnPciBean(lastEditText.getText().toString(), lastPciEditText.getText().toString()));
                }
                listener.setIsChangingTrue();
                listener.refreshRVHeight();
            }
            notifyItemRemoved(pos);
            notifyDataSetChanged();     //使用notifyItemRemoved会导致onbind不执行导致position错误
        }
    }

    public void removeOnDelArfcnListListener() {
        this.listener = null;
    }
    private void banLeadingZero(EditText ed_pci_n) {
        ed_pci_n.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input = editable.toString();
                if (input.length() > 1 && input.startsWith("0")) {
                    // 删除后续输入并设置新的光标位置
                    editable.delete(editable.length() - 1, editable.length());
                    ed_pci_n.setSelection(editable.length());
                }
            }
        });
    }
    public void setOnDelArfcnListListener(OnDelArfcnListListener listener) {
        this.listener = listener;
    }

    public interface OnDelArfcnListListener {
        void setIsChangingTrue();

        void refreshRVHeight();
    }

    private OnDelArfcnListListener listener;
}
