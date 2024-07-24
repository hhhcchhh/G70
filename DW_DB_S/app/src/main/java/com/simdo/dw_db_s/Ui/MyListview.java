package com.simdo.dw_db_s.Ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class MyListview extends ListView {
    private float lasty;
    public MyListview(Context context) {
        super(context);
    }

    public MyListview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyListview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyListview(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //此处必须让父View不拦截事件，否则后面的事件无法获取
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:

                //获取ListView当前的第一个可见item
                int frist = getFirstVisiblePosition();
                //获取ListView当前的最后一个可见item
                int last = getLastVisiblePosition();
                //获取item总数
                int child = getCount();

                if (y > lasty && frist == 0) {
                    //ListView的第一个可见View的position == 0 并且向下滑动时，请求父View拦截事件
                    getParent().requestDisallowInterceptTouchEvent(false);

                } else if (y < lasty && last == child - 1) {
                    //ListView的最后一个个可见View的position是最后一个item， 并且向上滑动时，请求父View拦截事件
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    //其它情况，事件由本View消费
                    getParent().requestDisallowInterceptTouchEvent(true);
                }

                break;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;

            default:
                break;
        }
        lasty = y;

        return super.dispatchTouchEvent(ev);
    }
}
