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

import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//没用
public class MyRecyclerviewAdapter extends RecyclerView.Adapter<MyRecyclerviewAdapter.ViewHolder> {
    Context context;

    public List<String> getArfcnList() {
        return arfcnList;
    }

    private final List<String> arfcnList = new ArrayList<>();
    String band;
    EditText lastEditText = null;
    private final int minDelta = 200;           // threshold in ms
    private long focusTime = 0;                 // time of last touch
    private View focusTarget = null;
    private List<ViewHolder> yourViewHolders = new ArrayList<>();


    private Boolean focusable = true;

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
        ViewHolder viewHolder = new ViewHolder(view);
        yourViewHolders.add(viewHolder); // 添加到列表
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppLog.D("onBindViewHolder focusable" + focusable);
        holder.arfcn.setText(arfcnList.get(position));
        holder.arfcn.setEnabled(false);
        lastEditText = null;
        if (position == getItemCount() - 1) {
            if (holder.arfcn.getText().toString().isEmpty()) {
                holder.arfcn.setEnabled(true);
//                holder.arfcn.requestFocus();
                lastEditText = holder.arfcn;
            }
        }
        holder.btn_del.setOnClickListener(new BtnClick(position));


//        if (!holder.ifClickable){
//            holder.btn_del.setVisibility(View.GONE);
//        }
//        holder.btn_del.setClickable(holder.ifClickable);
//        holder.btn_del.setEnabled(holder.ifClickable);
//        holder.btn_del.setFocusable(holder.ifClickable);
//        holder.btn_del.setFocusableInTouchMode(holder.ifClickable);

        //修复失焦问题
        View.OnFocusChangeListener onFocusChangeListener = (view, hasFocus) -> {
            long t = System.currentTimeMillis();
            long delta = t - focusTime;
            if (hasFocus) {     // gained focus
                if (delta > minDelta) {
                    focusTime = t;
                    focusTarget = view;
                }
            } else {              // lost focus
                if (delta <= minDelta && view == focusTarget) {
                    focusTarget.postDelayed(() ->
                            focusTarget.requestFocus(), 500);

                }
            }
        };
        holder.arfcn.setOnFocusChangeListener(onFocusChangeListener);

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
    public void setData(List<Integer> list) {
        arfcnList.clear();
        for (Integer integer : list) {
            arfcnList.add(String.valueOf(integer));
        }
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(ArrayList<String> list) {
        arfcnList.clear();
        arfcnList.addAll(list);
        notifyDataSetChanged();
    }

    public boolean addData(String arfcn) {
        if (!arfcnList.contains(arfcn)) {
            if (getItemCount() > 0) {
                if (arfcnList.get(getItemCount() - 1).equals("")) { //最后一个是空
                    arfcnList.remove(getItemCount() - 1);
                }
            }
            arfcnList.add(arfcn);
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
//        Boolean ifClickable = true;

        public ViewHolder(View view) {
            super(view);
            arfcn = view.findViewById(R.id.arfcn);
            btn_del = view.findViewById(R.id.btn_del);
        }


//        public Boolean getIfClickable() {
//            return ifClickable;
//        }
//
//        public void setIfClickable(Boolean ifClickable) {
//            this.ifClickable = ifClickable;
//        }
    }

    class BtnClick implements View.OnClickListener {
        int pos;

        public BtnClick(int pos) {
            this.pos = pos;
        }

        @Override
        public void onClick(View v) {
            try {
                if (!focusable) return;
                //如果是空直接删除
                if (arfcnList.get(pos).equals("")) {
                    arfcnList.remove(pos);
                    notifyDataSetChanged();
                    lastEditText = null;
//                    v.getRootView().clearFocus();
                    return;
                }
                //判断并保存当前数据到当前列表和Nx+cfg文件
                if (!storeLastText2(band)) return;
                //删除页面列表的数据
                listener.OnDelArfcn(arfcnList.get(pos), band);
                //保存当前列表到文件
                PrefUtil.build().putValue(band, string2Json(arfcnList, band));   //这里的band可以是"TDx"也可以是"Nx/Bx"
                //删除对应的band的Pref
                if (!arfcnList.get(pos).equals(""))
                    deleteArfcnFromCfgPref(Integer.parseInt(arfcnList.get(pos)));
                arfcnList.remove(pos);
                if (arfcnList.size() == 0) lastEditText = null;
                notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //将一个值存入对应的频段中删除
    public void deleteArfcnFromCfgPref(int arfcn) {
        String key;
        if (arfcn < 100000) {
            key = "B" + LteBand.earfcn2band(arfcn);
        } else {
            key = "N" + NrBand.earfcn2band(arfcn);
        }
        //先取出原有的删除进去
        List<Integer> list;
        try {
            list = Util.json2Int(PrefUtil.build().getValue(key + "cfg", "").toString(), key);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) == arfcn) {
                    list.remove(i);
                    break;
                }
            }
            PrefUtil.build().putValue(key + "cfg", Util.int2Json(list, key));
        } catch (JSONException e) {
            e.printStackTrace();
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

    //如果当前输入符合，添加到arfcnList并保存到本地,返回能否删除
    private boolean storeLastText2(String key) {
        AppLog.I("CfgTraceChildFragment addData");
        if (lastEditText == null) return true;
        String text = lastEditText.getText().toString();
        if (text.isEmpty()) {
            Util.showToast("不能添加空，请先填写!");
            return false;
        }
        int band;
        switch (key) {
            case "TD1":// N41/N78/N79
                band = NrBand.earfcn2band(Integer.parseInt(text));
                if (band != 41 && band != 78 && band != 79) {
                    Util.showToast("该数据值不属于此通道，请修改!");
                    return false;
                }
                break;
            case "TD2":// B3/B5/B8
                band = LteBand.earfcn2band(Integer.parseInt(text));
                if (band != 3 && band != 5 && band != 8) {
                    Util.showToast("该数据值不属于此通道，请修改!");
                    return false;
                }
                break;
            case "TD3":// N1(B1)/N41
                if (text.length() >= 6) {  //5g
                    band = NrBand.earfcn2band(Integer.parseInt(text));
                    if (band != 1 && band != 41) {
                        Util.showToast("该数据值不属于此通道，请修改!");
                        return false;
                    }
                } else {
                    band = LteBand.earfcn2band(Integer.parseInt(text));
                    if (band != 1) {
                        Util.showToast("该数据值不属于此通道，请修改!");
                        return false;
                    }
                }
                break;
            case "TD4":// B34/B39/B40/B41
                band = LteBand.earfcn2band(Integer.parseInt(text));
                if (band != 34 && band != 39 && band != 40 && band != 41) {
                    Util.showToast("该数据值不属于此通道，请修改!");
                    return false;
                }
                break;
        }
        AppLog.D("CfgTraceChildFragment arfcnList: " + arfcnList + " text: " + text);
        if (!arfcnList.contains(text)) {
            if (arfcnList.get(getItemCount() - 1).equals("")) {
                arfcnList.remove(getItemCount() - 1);
            }
            arfcnList.add(text);
            //保存到相应频点的配置文件
            addArfcnToCfgPref(Integer.parseInt(text), false);
        } else {
            Util.showToast("已存在相同数据值，请修改!");
            return false;
        }
        return true;
    }

    //将一个值存入对应的频段中
    public void addArfcnToCfgPref(int arfcn, Boolean ifCheck) {
        AppLog.I("CfgTraceChildFragment addArfcnToCfgPref: " + arfcn);
        String key;
        if (arfcn < 100000) {
            key = "B" + LteBand.earfcn2band(arfcn);
        } else {
            key = "N" + NrBand.earfcn2band(arfcn);
        }
        if (ifCheck) {
            if (addData(String.valueOf(arfcn))) {    //这里检查会不会重复并添加到adapter的list中去
                //先取出原有的添加进去
                List<Integer> list;
                try {
                    list = Util.json2Int(PrefUtil.build().getValue(key + "cfg", "").toString(), key);
                    list.add(arfcn);
                    PrefUtil.build().putValue(key + "cfg", Util.int2Json(list, key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else Util.showToast("已存在相同数据值，请修改!");
        } else {
            //先取出原有的添加进去
            List<Integer> list;
            try {
                list = Util.json2Int(PrefUtil.build().getValue(key + "cfg", "").toString(), key);
                list.add(arfcn);
                PrefUtil.build().putValue(key + "cfg", Util.int2Json(list, key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public Boolean getFocusable() {
        return focusable;
    }

    public void setFocusable(Boolean focusable) {
        this.focusable = focusable;
        notifyDataSetChanged();
    }
// 禁用所有按钮
//    public void disableAllDel() {
//        for (ViewHolder item : yourViewHolders) {
//            item.setIfClickable(false); // 例如，禁用点击
//        }
//        notifyDataSetChanged(); // 通知适配器刷新所有项
//    }

    public interface OnDelArfcnListListener {
        void OnDelArfcn(String arfcn, String band);

    }

    private OnDelArfcnListListener listener;
}
