//package com.simdo.g73cs.Adapter;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.flexbox.FlexDirection;
//import com.google.android.flexbox.FlexWrap;
//import com.google.android.flexbox.FlexboxLayoutManager;
//import com.nr.Arfcn.Bean.LteBand;
//import com.nr.Arfcn.Bean.NrBand;
//import com.simdo.g73cs.Bean.CheckBoxBean;
//import com.simdo.g73cs.R;
//import com.simdo.g73cs.Util.AppLog;
//import com.simdo.g73cs.Util.DividerItemDecoration;
//import com.simdo.g73cs.Util.PrefUtil;
//import com.simdo.g73cs.Util.Util;
//
//import org.json.JSONException;
//
//import java.util.List;
//
//public class ArfcnListCfgAdapter extends RecyclerView.Adapter<ArfcnListCfgAdapter.ViewHolder> {
//    Context mContext;
//    private List<CheckBoxBean> check_box_list;  //存的是一个个的CheckBoxBean
//
//    public ArfcnListCfgAdapter(Context context, List<CheckBoxBean> check_box_list) {
//        this.mContext = context;
//        this.check_box_list = check_box_list;
//    }
//
//    @NonNull
//    @Override
//    public ArfcnListCfgAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(mContext).inflate(R.layout.item_arfcn, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
//        final int position = pos;
//        holder.tv_text.setText(check_box_list.get(position).getText());
//        MyRecyclerviewAdapter adapter = new MyRecyclerviewAdapter(mContext,
//                check_box_list.get(position).getArfcnList(), check_box_list.get(position).getText());
//        adapter.setOnDelArfcnListListener(new MyRecyclerviewAdapter.OnDelArfcnListListener() {
//            @Override
//            public void OnDelArfcn(String arfcn, String band) {
//                if (arfcn.isEmpty()) return;
//                check_box_list.get(position).getArfcnList().remove(Integer.valueOf(arfcn));
//            }
//        });
//        holder.list.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
//        holder.list.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
//        holder.list.setAdapter(adapter);
//        holder.iv_add.setOnClickListener(v ->   //每次点击的时候将上次的添加到checkboxlist的position位置中的ArfcnList里
//                addData(adapter, check_box_list.get(position).getArfcnList(), check_box_list.get(position).getText()));
//
//    }
//
//    @Override
//    public int getItemCount() {
//        return check_box_list.size();
//    }
//
//    //添加新的EditText并将旧数据存储起来
//    private void addData(MyRecyclerviewAdapter adapter, List<Integer> list, String key) {
//        AppLog.D("ArfcnListCfgAdapter addData");
//        String text = adapter.getLastViewText();
//        if (text == null) { //上次删除了
//            adapter.addEditView();
//            return;
//        }
//        if (!text.isEmpty()) {
//            int arfcn = Integer.parseInt(text);
//            int band = arfcn < 100000 ? LteBand.earfcn2band(arfcn) : NrBand.earfcn2band(arfcn);
//            int keyInt = Integer.parseInt(key.substring(1));
//            if (band != keyInt) {
//                showToast("该数据值不属于此频段，请修改!");
//                return;
//            }
//            if (adapter.addData(text)) {
//                adapter.addEditView();
//                list.add(Integer.parseInt(text));
//                try {
//                    PrefUtil.build().putValue(key, Util.int2Json(list, key));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            } else showToast("已存在相同数据值，请修改!");
//        }
//    }
//
//    //将一个值存入对应的频段中
//    public void addArfcnToCfgPref(MyRecyclerviewAdapter adapter, int arfcn) {
//        AppLog.D("ArfcnListCfgAdapter addArfcnToCfgPref");
//        String key;
//        if (arfcn < 100000) {
//            key = "B" + LteBand.earfcn2band(arfcn);
//        } else {
//            key = "N" + NrBand.earfcn2band(arfcn);
//        }
//
//        if (adapter.addData(String.valueOf(arfcn))) {    //这里检查会不会重复并添加到adapter的list中去
//            //先取出原有的添加进去
//            List<Integer> list;
//            try {
//                list = Util.json2Int(PrefUtil.build().getValue(key + "cfg", "").toString(), key);
//                list.add(arfcn);
//                PrefUtil.build().putValue(key + "cfg", Util.int2Json(list, key));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        } else showToast("已存在相同数据值，请修改!");
//    }
//    private void showToast(String msg) {
//        Context context = mContext.getApplicationContext();
//        Toast toast = new Toast(context);
//        //创建Toast中的文字
//        TextView textView = new TextView(context);
//        textView.setText(msg);
//        textView.setBackgroundResource(R.drawable.radio_main);
//        textView.setTextColor(Color.WHITE);
//        textView.setGravity(Gravity.CENTER);
//        textView.setTextSize(14);
//        textView.setPadding(24, 14, 24, 14);
//        toast.setView(textView); //把layout设置进入Toast
//        toast.setGravity(Gravity.BOTTOM, 0, 200);
//        toast.setDuration(Toast.LENGTH_SHORT);
//        toast.show();
//    }
//
//    static class ViewHolder extends RecyclerView.ViewHolder {
//        ImageView iv_add;
//        TextView tv_text;
//        RecyclerView list;
//
//        public ViewHolder(View view) {
//            super(view);
//            iv_add = view.findViewById(R.id.iv_add);
//            tv_text = view.findViewById(R.id.tv_text);
//            list = view.findViewById(R.id.list);
//        }
//    }
//}
