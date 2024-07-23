package com.simdo.g73cs.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.simdo.g73cs.R;

public class PercentCircleView extends View {

    private final Paint circlePaint = new Paint();
    private final Paint circleColorPaint = new Paint();
    private final int textGrayColor = ContextCompat.getColor(getContext(), R.color.white);
    private int textBlueColor = ContextCompat.getColor(getContext(), R.color.main_color);
    private float viewRadius = 0f;
    private final float circleSize = dip2px(1f);
    private float minRadius = 0f;
    private float maxRadius = 0f;
    private final float strokeWidth = dip2px(0.001f);
    private int percent = 0;
    private RectF rectF = null;
    private final Rect textRect = new Rect();

    public PercentCircleView(Context context) {
        this(context, null);
    }

    public PercentCircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PercentCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(textGrayColor);
        circlePaint.setTextSize(sp2px(6));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(dip2px(1));
        circlePaint.setTextAlign(Paint.Align.CENTER);
        circlePaint.setFakeBoldText(false);
        circlePaint.getTextBounds("8", 0, 1, textRect);

        circleColorPaint.setColor(textBlueColor);
        circleColorPaint.setAntiAlias(true);
        circleColorPaint.setStyle(Paint.Style.STROKE);
        circleColorPaint.setStrokeWidth(dip2px(1));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewRadius = Math.min(getMeasuredWidth(), getMeasuredHeight()) * 1.0f / 2;
        maxRadius = viewRadius - strokeWidth;
        minRadius = viewRadius - circleSize;
        rectF = new RectF(circleSize / 2, circleSize / 2, 2 * viewRadius - circleSize / 2 - strokeWidth, 2 * viewRadius - circleSize / 2 - strokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawArc(rectF, -92f, 360, false, circlePaint);

        canvas.drawArc(rectF, -92f, 360 * (percent * 1.0f / 100), false, circleColorPaint);

        circlePaint.setStrokeWidth(0);
        canvas.drawText(percent + "%", viewRadius, viewRadius + (textRect.bottom - textRect.top) / 2, circlePaint);
    }

    private float dip2px(float dipValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return dipValue * scale;
    }

    private int sp2px(float spVal) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                spVal, getContext().getResources().getDisplayMetrics()
        );
    }

    public void setProgress(int progress) {
        percent = progress;
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;
        invalidate();
    }

    public void setTextBlueColor(int textBlueColor) {
        circleColorPaint.setColor(textBlueColor);
        invalidate();
    }
}
