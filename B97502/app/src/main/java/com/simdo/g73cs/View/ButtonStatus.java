package com.simdo.g73cs.View;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

/**
 * Created by ccwxf on 2016/2/29.
 */
public interface ButtonStatus {
    /**
     * @return 对应的Status值
     */
    Status getStatus();

    /**
     * 这个状态的事件处理代理
     *
     * @param mEvent
     * @return
     */
    boolean onTouchEvent(MotionEvent mEvent);

    /**
     * 这个状态的绘制代理
     *
     * @param mCanvas
     * @param mPaint
     */
    void onDraw(Canvas mCanvas, Paint mPaint);
}
