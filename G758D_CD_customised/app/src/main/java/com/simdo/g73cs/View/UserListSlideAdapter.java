package com.simdo.g73cs.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.Bean.UserManagerBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;

import java.util.List;

public class UserListSlideAdapter extends RecyclerView.Adapter<UserListSlideAdapter.ViewHolder>
        implements ItemSlideHelper.Callback {

    private Context context;


    private List<UserManagerBean> userList;
    private ListItemListener listener;

    public void setListener(ListItemListener listener) {
        this.listener = listener;
    }

    public interface ListItemListener {
        void onChangeDescClickListener(UserManagerBean bean);

        void onChangePasswordClickListener(UserManagerBean bean);

        void onDeleteClickListener(UserManagerBean bean);

    }

    private RecyclerView mRecyclerView;

    public UserListSlideAdapter() {
    }

    public UserListSlideAdapter(List<UserManagerBean> list, ListItemListener listener) {
        userList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserListSlideAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_user_manage_item, viewGroup, false);

        return new UserListSlideAdapter.ViewHolder(view);
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
        if (MainActivity.account.equals("admin"))
            mRecyclerView.addOnItemTouchListener(new ItemSlideHelper(mRecyclerView.getContext(), this));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int pos) {
        //如果不是管理员只显示自己
        if (!MainActivity.account.equals("admin")) {
            if (!userList.get(pos).getAccount().equals(MainActivity.account)) {
                holder.rl_change_desc.setVisibility(View.GONE);
                holder.rl_change_password.setVisibility(View.GONE);
                holder.tv_delete.setVisibility(View.GONE);
                return;
            }
        }
        final int position = pos;

        holder.tv_account.setText(userList.get(position).getAccount());
        holder.tv_desc.setText(userList.get(position).getDescription());
        /**
         * -->特别注意，敲黑板了啊！！！在执行notify的时候，取position要取holder.getAdapterPosition()，
         * 消息被删除之后，他原来的position是final的，所以取到的值不准确，会报数组越界。
         */

        holder.rl_change_desc.setOnClickListener(view -> {
            if (listener != null) listener.onChangeDescClickListener(userList.get(position));
        });
        holder.rl_change_password.setOnClickListener(view -> {
            if (listener != null) listener.onChangePasswordClickListener(userList.get(position));
        });
        holder.tv_delete.setOnClickListener(view -> {
            if (listener != null) listener.onDeleteClickListener(userList.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUserList(List<UserManagerBean> userList) {
        this.userList = userList;
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
//            return viewGroup.getChildAt(1).getLayoutParams().width
//                    + viewGroup.getChildAt(2).getLayoutParams().width;
            return viewGroup.getChildAt(1).getLayoutParams().width;
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
        TextView tv_account;
        TextView tv_desc;
        RelativeLayout rl_change_desc;
        RelativeLayout rl_change_password;
        TextView tv_delete;

        public ViewHolder(View view) {
            super(view);
            tv_account = view.findViewById(R.id.adapter_user_manage_user_name);
            tv_desc = view.findViewById(R.id.adapter_user_manage_desc);
            rl_change_desc = view.findViewById(R.id.adapter_user_manage_change_desc);
            rl_change_password = view.findViewById(R.id.adapter_user_manage_change_password);
            tv_delete = view.findViewById(R.id.adapter_user_manage_delete_user_btn);
        }
    }


}