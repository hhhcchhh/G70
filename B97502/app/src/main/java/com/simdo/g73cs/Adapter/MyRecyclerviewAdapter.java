package com.simdo.g73cs.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.PrefUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class MyRecyclerviewAdapter extends RecyclerView.Adapter<MyRecyclerviewAdapter.ViewHolder> {
    Context context;
    private final List<String> arfcnList = new ArrayList<>();
    String band;

    public MyRecyclerviewAdapter(Context context, List<Integer> arfcnList, String band) {
        this.context = context;
        this.arfcnList.clear();
        for (Integer integer : arfcnList) {
            this.arfcnList.add(String.valueOf(integer));
        }
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
        final int pos = position;
        holder.arfcn.setText(arfcnList.get(pos));
        holder.arfcn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAutoAddDialog(pos, arfcnList.get(pos));
            }
        });
        holder.btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.OnDelArfcn(arfcnList.get(pos), band);
                arfcnList.remove(pos);
                notifyDataSetChanged();
                try {
                    PrefUtil.build().putValue(band, string2Json(arfcnList, band));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showAutoAddDialog(int pos, String lastArfcn) {
        MainActivity.getInstance().createCustomDialog(false);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_arfcn, null);
        final EditText ed_arfcn = view.findViewById(R.id.ed_arfcn);
        final TextView as_band = view.findViewById(R.id.as_band);
        ed_arfcn.setText(lastArfcn);
        ed_arfcn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                while (s.toString().startsWith("0")) s.delete(0, 1);
                String string = s.toString();
                String band_str = "null";
                if (string.length() > 5) {
                    int band1 = NrBand.earfcn2band(Integer.parseInt(string));
                    if (band1 != 0) band_str = "N" + band1;
                } else if (string.length() > 2) {
                    int band1 = LteBand.earfcn2band(Integer.parseInt(string));
                    if (band1 != 0) band_str = "B" + band1;
                }
                as_band.setText(MessageFormat.format("{0}{1}", context.getString(R.string.as_band), band_str));
                as_band.setTextColor(band.equals(band_str) ? context.getResources().getColor(R.color.color_212121) : context.getResources().getColor(R.color.color_e65c5c));
            }
        });
        as_band.setText(MessageFormat.format("{0}{1}", context.getString(R.string.as_band), band));
        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String arfcn = ed_arfcn.getEditableText().toString().trim();

                String newBand = as_band.getText().toString();
                if (!newBand.contains(band)) {
                    MainActivity.getInstance().showToast("不符合该频段数据，更新失败");
                    return;
                } else {
                    if (arfcnList.contains(arfcn)){
                        MainActivity.getInstance().showToast(context.getString(R.string.value_exist));
                        return;
                    }
                    listener.OnUpdateArfcn(arfcn, pos);
                    arfcnList.set(pos, arfcn);
                    notifyItemChanged(pos);
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    try {
                        PrefUtil.build().putValue(band, string2Json(arfcnList, band));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    MainActivity.getInstance().showToast(context.getString(R.string.updated));
                }
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false);
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView arfcn;
        ImageView btn_del;

        public ViewHolder(View view) {
            super(view);
            arfcn = view.findViewById(R.id.arfcn);
            btn_del = view.findViewById(R.id.btn_del);
        }
    }

    private String string2Json(List<String> list, String key) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject tmpObj;
        for (int i = 0; i < list.size(); i++) {
            tmpObj = new JSONObject();
            tmpObj.put(key, list.get(i));
            jsonArray.put(tmpObj);
        }
        return jsonArray.toString();
    }

    public void removeOnDelArfcnListListener() {
        this.listener = null;
    }

    public void setOnArfcnListListener(OnArfcnListListener listener) {
        this.listener = listener;
    }

    public interface OnArfcnListListener {
        void OnDelArfcn(String arfcn, String band);

        void OnUpdateArfcn(String arfcn, int pos);
    }

    private OnArfcnListListener listener;
}
