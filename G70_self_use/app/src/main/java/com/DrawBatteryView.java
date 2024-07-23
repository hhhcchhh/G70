package com;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class DrawBatteryView extends View {
    private int mBoder;     //电池外框的宽带
    private int mHeight;   //总高
    private int mHeadWidth = 60;
    private RectF mMainRect;
    private RectF mHeadRect;
    private float mRadius = 1f;   //圆角
    private float mPower = 100;

    public DrawBatteryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public DrawBatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public DrawBatteryView(Context context) {
        super(context);

    }

    private void initView() {
        //更新参数， 实现图标大小填充整个view start
        //总长
        int mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mBoder = 4;
        //电池内芯与边框的距离
        int mMargin = 2;
        mHeadWidth = mWidth / 10;
        int mHeadHeight = (int) (mHeight / 2.5);
        mRadius = 1;

        float left = mMargin;
        float top = mBoder;
        float right = mWidth - mHeadWidth;
        float bottom = mHeight - mBoder;
        mMainRect = new RectF(left, top, right, bottom);

        mHeadRect = new RectF(mMainRect.right + mMargin, (mHeight - mHeadHeight) / 2f, mMainRect.right + mHeadWidth + mMargin, (mHeight + mHeadHeight) / 2f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint1 = new Paint();
        paint1.setAntiAlias(true);

        //画外框
        paint1.setStyle(Paint.Style.STROKE);    //设置空心矩形
        paint1.setStrokeWidth(mBoder);          //设置边框宽度
        paint1.setColor(Color.WHITE);
        canvas.drawRoundRect(mMainRect, mRadius, mRadius, paint1);
        //画电池芯
        Paint paint = new Paint();

        if ( mPower > 0 && mPower < 20 ) {
            paint.setColor(Color.parseColor("#FF7070"));
        } else if ( mPower >= 20 ) {
            paint.setColor(Color.WHITE);
        }

        int b1 = mHeight / 6;
        int width = (int) (mPower / 100 * (mMainRect.right - mMainRect.left - 2 * b1));

        int left = (int) (mMainRect.left + b1 );
        int right = (int) (mMainRect.left + b1 + width);
        int top = (int) (mMainRect.top + b1);
        int bottom = (int) (mMainRect.bottom - b1);
        RectF rect = new RectF(left, top, right, bottom);
        float r = (bottom - top) / 10f;
        canvas.drawRoundRect(rect, r, r, paint);

        //画电池头
        paint1.setStyle(Paint.Style.FILL);
        paint1.setColor(Color.WHITE);
        int rH = mHeadWidth / 4;
        canvas.drawRoundRect(mHeadRect, rH, rH, paint1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initView();
    }

    public void setPower(float power) {
        mPower = power;
        invalidate();
    }
}
