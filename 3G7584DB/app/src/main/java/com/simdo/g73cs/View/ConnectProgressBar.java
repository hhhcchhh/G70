package com.simdo.g73cs.View;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;

public class    ConnectProgressBar extends View {
    private Paint bgPaint;
    private Paint progressPaint;
    private Paint tipPaint;
    private Paint textPaint;

    private int mWidth;
    private int mHeight;

    private int mViewHeight;
    /**
     * 进度
     */
    private float mProgress;
    //描述文字的高度
    private float textHeight;
    //描述文字的高度
    private float textWidth;
    /**
     * 当前进度
     */
    private float currentProgress;

    /**
     * 进度动画
     */
    private ValueAnimator progressAnimator;

    /**
     * 动画执行时间
     */
    private int duration = 1000;
    /**
     * 动画延时启动时间
     */
    private int startDelay = 500;

    /**
     * 进度条画笔的宽度
     */
    private int progressPaintWidth;

    private int progressHeight;

    /**
     * 百分比提示框画笔的宽度
     */
    private int tipPaintWidth;

    /**
     * 百分比提示框的高度
     */
    private int tipHeight;

    /**
     * 百分比提示框的宽度
     */
    private int tipWidth;

    /**
     * 画三角形的path
     */
    private Path path = new Path();
    /**
     * 三角形的高
     */
    private int triangleHeight;

    /**
     * 进度条距离提示框的高度
     */
    private int progressMarginTop;

    /**
     * 进度移动的距离
     */
    private float moveDis;

    private Rect textRect = new Rect();
    /**
     * 绘制提示框的矩形
     */
    private RectF rectF = new RectF();

    private String textString = "0%";
    /**
     * 百分比文字字体大小
     */
    private int textPaintSize;

    /**
     * 进度条背景颜色
     */
    private int bgColor = 0xFFFFFFFF;
    /**
     * 进度条颜色
     */
    private int textBgColor = 0xFFffffff;
    private int textColor = 0xFF00E5CA;

    /**
     * 渐变颜色组
     */
    private int[] GRADIENT_COLORS = {
            Color.parseColor("#FF76FFE6"), Color.parseColor("#FF00E5CA"),
            Color.parseColor("#FF76FFE6"), Color.parseColor("#FF00E5CA")};
    private RectF bgRectF = new RectF();
    private RectF progressRectF = new RectF();

    /**
     * 圆角矩形的圆角半径
     */
    private int roundRectRadius;
    private int roundTipRadius;

    /**
     * 进度监听回调
     */
    private ProgressListener progressListener;

    public ConnectProgressBar(Context context) {
        this(context, null);
    }

    public ConnectProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConnectProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initPaint();
        initTextPaint();
    }
    /**
     * 初始化画笔宽度及view大小
     */
    private void init() {
        progressPaintWidth = dp2px(1);
        progressHeight = dp2px(4);
        roundRectRadius = dp2px(4);
        tipHeight = dp2px(15);
        tipWidth = dp2px(30);
        moveStart = 0;
        len = 0;
        tipPaintWidth = dp2px(1);
        triangleHeight = dp2px(3);
        roundTipRadius = dp2px(2);
        textPaintSize = sp2px(10);
        textHeight = dp2px(10);
        progressMarginTop = dp2px(10);

        //view真实的高度
        mViewHeight = (int) (textHeight + progressMarginTop + progressPaintWidth * 2 + progressHeight);
    }


    private void initPaint() {
        bgPaint = getPaint(progressPaintWidth, bgColor, Paint.Style.FILL);
        progressPaint = getPaint(progressPaintWidth, textBgColor, Paint.Style.FILL);
        tipPaint = getPaint(tipPaintWidth, textBgColor, Paint.Style.FILL);
    }

    /**
     * 初始化文字画笔
     */
    private void initTextPaint() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textPaintSize);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
    }


    /**
     * 统一处理paint
     *
     * @param strokeWidth 画笔宽度
     * @param color       颜色
     * @param style       风格
     * @return paint
     */
    private Paint getPaint(int strokeWidth, int color, Paint.Style style) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStyle(style);
        return paint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(measureWidth(widthMode, width), measureHeight(heightMode, height));
    }

    /**
     * 测量宽度
     *
     * @param mode
     * @param width
     * @return
     */
    private int measureWidth(int mode, int width) {
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                break;
            case MeasureSpec.EXACTLY:
                mWidth = width - dp2px(60);
                break;
        }
        return mWidth;
    }

    /**
     * 测量高度
     *
     * @param mode
     * @param height
     * @return
     */
    private int measureHeight(int mode, int height) {
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                mHeight = mViewHeight;
                break;
            case MeasureSpec.EXACTLY:
                mHeight = height;
                break;
        }
        return mHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //背景
//        drawBgProgress(canvas);
        if (mProgress == 0){
            moveStart+=2;
            int width = getMeasuredWidth();
            if (moveStart > width - dp2px(50) && moveStart < width){ // 末端时，移动条长度慢慢减到0
                if (len > 0) len -= 1;
            }else if (moveStart > width){
                moveStart = 0; // 移动条首位置在进度条最末端时，从0继续
            } else if (moveStart < width) {
                if (len < 50) { // 前端时，移动条长度慢慢加到50
                    moveStart -= 2;
                    len += 1;
                }
            }
            drawZeroProgress(canvas);
            invalidate();
        }else {
            //进度条
            drawProgress(canvas);
            //文字背景框
//            drawTipView(canvas);
            //绘制文字
//            drawText(canvas, textString);
        }
    }

    /**
     * 绘制进度上边提示百分比的view
     *
     * @param canvas
     */
    private void drawTipView(Canvas canvas) {
        drawRoundRect(canvas);
        drawTriangle(canvas);
    }


    /**
     * 绘制圆角矩形
     *
     * @param canvas
     */
    private void drawRoundRect(Canvas canvas) {
        rectF.set(moveDis + tipWidth, 0, tipWidth + moveDis + tipWidth, tipHeight);
        canvas.drawRoundRect(rectF, roundRectRadius, roundRectRadius, tipPaint);
    }

    /**
     * 绘制三角形
     *
     * @param canvas
     */
    private void drawTriangle(Canvas canvas) {
        path.moveTo(tipWidth / 2 - triangleHeight + moveDis + tipWidth, tipHeight);
        path.lineTo(tipWidth / 2 + moveDis + tipWidth, tipHeight + triangleHeight);
        path.lineTo(tipWidth / 2 + triangleHeight + moveDis + tipWidth, tipHeight);
        canvas.drawPath(path, tipPaint);
        path.reset();

    }

    private void drawBgProgress(Canvas canvas) {
        bgRectF.left = tipWidth;
        bgRectF.top = textHeight + progressMarginTop;
        bgRectF.right = mWidth + tipWidth;
        bgRectF.bottom = bgRectF.top + progressHeight;
        canvas.drawRoundRect(bgRectF, roundRectRadius, roundRectRadius, bgPaint);
    }

    private void drawProgress(Canvas canvas) {
        progressRectF.left = tipWidth;
        progressRectF.top = textHeight + progressMarginTop;
        progressRectF.right = currentProgress + tipWidth;
        progressRectF.bottom = progressRectF.top + progressHeight;
        LinearGradient shader;
        if (mProgress < 50)
            shader = new LinearGradient(progressRectF.left, progressRectF.top,
                    progressRectF.right, progressRectF.bottom, GRADIENT_COLORS[0], GRADIENT_COLORS[1], Shader.TileMode.MIRROR);
        else
            shader = new LinearGradient(progressRectF.left, progressRectF.top,
                    progressRectF.right, progressRectF.bottom, GRADIENT_COLORS[2], GRADIENT_COLORS[3], Shader.TileMode.MIRROR);

        progressPaint.setShader(shader);//进度渐变色
        canvas.drawRoundRect(progressRectF, roundRectRadius, roundRectRadius, progressPaint);
    }

    private int moveStart;
    private int len;
    private void drawZeroProgress(Canvas canvas) {
        progressRectF.left = tipWidth + moveStart;
        progressRectF.top = textHeight + progressMarginTop;
        progressRectF.right = progressRectF.left + dp2px(len);
        progressRectF.bottom = progressRectF.top + progressHeight;
        LinearGradient shader = new LinearGradient(progressRectF.left, progressRectF.top,
                progressRectF.right, progressRectF.bottom, GRADIENT_COLORS[0], GRADIENT_COLORS[1], Shader.TileMode.MIRROR);
        progressPaint.setShader(shader);//进度渐变色
        canvas.drawRoundRect(progressRectF, roundRectRadius, roundRectRadius, progressPaint);
    }

    /**
     * 绘制文字
     *
     * @param canvas 画布
     */
    private void drawText(Canvas canvas, String textString) {
        textRect.left = (int) (moveDis + (tipWidth - textWidth) / 2 + tipWidth);
        textRect.top = roundTipRadius;
        textRect.right = (int) (textPaint.measureText(textString) + moveDis + (tipWidth - textWidth) / 2 + tipWidth);
        textRect.bottom = (int) textHeight + roundTipRadius;
        Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
        int baseline = (textRect.bottom + textRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        //文字绘制到整个布局的中心位置
        canvas.drawText(textString, textRect.centerX(), baseline, textPaint);
    }


    /**
     * 进度移动动画  通过插值的方式改变移动的距离
     */
    private void initAnimation(float oldPro) {
        progressAnimator = ValueAnimator.ofFloat(oldPro, mProgress);
        progressAnimator.setDuration(duration);
        progressAnimator.setStartDelay(startDelay);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                //进度数值只显示整数，我们自己的需求，可以忽略
//                textString = formatNum(format2Int(value)) + "%";
//                textWidth = textPaint.measureText(textString);
                //把当前百分比进度转化成view宽度对应的比例
                currentProgress = value * mWidth / 100;
                //进度回调方法
                if (progressListener != null) {
                    progressListener.currentProgressListener(value, oldPro > mProgress);
                }
                //移动百分比提示框，只有当前进度到提示框中间位置之后开始移动，
                //当进度框移动到最右边的时候停止移动，但是进度条还可以继续移动
                //moveDis是tip框移动的距离
//                if (currentProgress >= 0 &&
//                        currentProgress <= mWidth) {
//                    moveDis = currentProgress - tipWidth / 2;
//                }
                invalidate();
            }
        });
        if (!progressAnimator.isStarted()) {
            progressAnimator.start();
        }
    }

    /**
     * 回调接口
     */
    public interface ProgressListener {
        void currentProgressListener(float currentProgress, boolean isBack);
    }

    /**
     * 回调监听事件
     *
     * @param listener
     * @return
     */
    public ConnectProgressBar setProgressListener(ProgressListener listener) {
        progressListener = listener;
        return this;
    }

    public ConnectProgressBar setProgress(float progress) {
        float oldPro = mProgress;
        mProgress = progress;
        if (mProgress < oldPro) oldPro = 0;
        initAnimation(oldPro);
        return this;
    }

    /**
     * 格式化数字(保留一位小数)
     *
     * @param money
     * @return
     */
    public static String formatNum(int money) {
        DecimalFormat format = new DecimalFormat("0");
        return format.format(money);
    }

    /**
     * dp 2 px
     *
     * @param dpVal
     */
    protected int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }

    /**
     * sp 2 px
     *
     * @param spVal
     * @return
     */
    protected int sp2px(int spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, getResources().getDisplayMetrics());
    }

    public static int format2Int(double i) {
        return (int) i;
    }
}
