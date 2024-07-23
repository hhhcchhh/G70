package com.simdo.g73cs.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.PrefUtil;

import java.util.List;

public class BlackListSlideAdapter extends RecyclerView.Adapter<BlackListSlideAdapter.ViewHolder>
        implements ItemSlideHelper.Callback {

    private Context context;
    private List<MyUeidBean> blackList;
    private final ListItemListener listener;
    public interface ListItemListener {
        void onItemClickListener(MyUeidBean bean);
    }
    private RecyclerView mRecyclerView;

    public BlackListSlideAdapter(Context context, List<MyUeidBean> list, ListItemListener listener) {
        //由于上一次保存的有可能不是应用退出前的列表，因此需要清楚上一次保存的勾选状态
        blackList = list;
        for (int i=0; i< blackList.size(); i++){
            blackList.get(i).setFirstChecked(false);
            blackList.get(i).setSecondChecked(false);
        }
        this.listener = listener;
    }

    @NonNull
    @Override
    public BlackListSlideAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.black_list_slide_item, viewGroup, false);

        return new BlackListSlideAdapter.ViewHolder(view);
    }

    /**
     * 将recyclerView绑定Slide事件
     *
     * @param recyclerView
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
        mRecyclerView.addOnItemTouchListener(new ItemSlideHelper(mRecyclerView.getContext(), this));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int pos) {

        final int position = pos;

        holder.tv_black_name.setText(blackList.get(position).getName());
        holder.tv_black_imsi.setText(blackList.get(position).getUeidBean().getImsi());

        /**
         * -->特别注意，敲黑板了啊！！！在执行notify的时候，取position要取holder.getAdapterPosition()，
         * 消息被删除之后，他原来的position是final的，所以取到的值不准确，会报数组越界。
         */

        //标记已读监听
        holder.tv_to_trace.setOnClickListener(view -> {
            if (listener!=null) listener.onItemClickListener(blackList.get(position));
        });
        //删除监听
        holder.tv_delete.setOnClickListener(view -> removeData(position));
    }

    @Override
    public int getItemCount() {
        return blackList.size();
    }

    /**
     * 此方法用来计算水平方向移动的距离
     *
     * @param holder
     * @return
     */
    @Override
    public int getHorizontalRange(RecyclerView.ViewHolder holder) {
        if (holder.itemView instanceof LinearLayout) {
            ViewGroup viewGroup = (ViewGroup) holder.itemView;
            //viewGroup包含3个控件，即消息主item、标记已读、删除，返回为标记已读宽度+删除宽度
            return viewGroup.getChildAt(1).getLayoutParams().width
                    + viewGroup.getChildAt(2).getLayoutParams().width;
        }
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder getChildViewHolder(View childView) {
        return mRecyclerView.getChildViewHolder(childView);
    }

    @Override
    public View findTargetView(float x, float y) {
        return mRecyclerView.findChildViewUnder(x, y);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_black_name;
        TextView tv_black_imsi;
        TextView tv_to_trace;
        TextView tv_delete;
        public ViewHolder(View view) {
            super(view);
            tv_black_name = view.findViewById(R.id.tv_black_name);

            tv_black_imsi = view.findViewById(R.id.tv_black_imsi);
            tv_to_trace = view.findViewById(R.id.tv_to_trace);
            tv_delete = view.findViewById(R.id.tv_delete);
        }
    }

    /**
     * 删除单条数据
     */
    public void removeData(int position) {
        blackList.remove(position);
        PrefUtil.build().putUeidList(blackList);
        notifyDataSetChanged();
    }
}