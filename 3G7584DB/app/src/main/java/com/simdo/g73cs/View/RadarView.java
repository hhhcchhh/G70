package com.simdo.g73cs.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;

import java.util.ArrayList;
import java.util.List;

public class RadarView extends View {
    private Bitmap pointImg;
    // 参数调整>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private final boolean usePointBitmap = true;
    private final float pointRadius = 6f;  // 扫描点的半径
    private final int MAX_POINT_SHOW = 1;// 最多显示n个点
    private final MaskFilter outLightFilter = new BlurMaskFilter(pointRadius, BlurMaskFilter.Blur.SOLID); // 外发光
    private final int maxShowPointAngel = 360; // 最大显示范围, 最大360度(在此范围内点才会显示)
    private final int bgColor = 0xaaffffff; // 背景颜色
    private final int bgRadar = 0xaaffffff; // 雷达区域背景色
    private final int colorCircle = 0xaaffffff; // 内部园环及分割线的颜色
    private final int radarColor = Color.parseColor("#AAffffff"); // 扫描扇形的颜色
    private final int mNumCicle = 5; // 多少个园环
    private final int mNumLines = 4; // 对角线的个数
    // 参数调整<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


    private SweepGradient sweepGradient = null;
    private boolean isSearching = false;
    private final Paint mPaint = new Paint();
    private int mCurrentAngel = 0;
    private final List<MyPoint> mPointArray = new ArrayList<MyPoint>();
    private int mWidth = 0;
    private int mHeight = 0;

    private int mCx = 0; // x、y轴中心点
    private int mCy = 0;
    private float mOutsideRadius = 0;// 外、内圆半径
    private float mInsideRadius = 0;
    private float mOutsideStrokeWidth = 3f; // 外圆宽度
    private float mRadarStrokeWidth = 5f;   //雷达图外圆宽度

    public RadarView(Context context) {
        super(context);
        init(context);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
//        if (usePointBitmap) {
        pointImg = BitmapFactory.decodeResource(getResources(), R.mipmap.point);
        // 默认硬件加速
        setLayerType(View.LAYER_TYPE_HARDWARE, (Paint) null);
//        } else {
//            setLayerType(View.LAYER_TYPE_SOFTWARE, (Paint) null);
//        }

    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 获取控件区域宽高
        if (mWidth == 0 || mHeight == 0) {
            int minimumWidth = getSuggestedMinimumWidth();
            int minimumHeight = getSuggestedMinimumHeight();
            mWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
            mHeight = resolveMeasured(heightMeasureSpec, minimumHeight);
            // 获取x/y轴中心点
            mCx = mWidth / 2;
            mCy = mHeight / 2;
            // 计算内、外半径
            mOutsideRadius = ((float) mWidth - mOutsideStrokeWidth) / 2f;// 外圆的半径
            mInsideRadius = (mWidth - mOutsideStrokeWidth - (float) mWidth / 10f) / mNumCicle / 2;// 内圆的半径,除最外层,其它圆的半径=层数*insideRadius
            AppLog.D("onMeasure mCx=" + mCx + ", mCy=" + mCy + ", mOutsideRadius=" + mOutsideRadius + ", mInsideRadius=" + mInsideRadius);
            sweepGradient = new SweepGradient((float) mCx, (float) mCy, 0, radarColor);
        }

    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        // 1. 绘制圆形背景
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(bgColor);
        mPaint.setShader(null);
        mPaint.setAlpha(100);
        mPaint.setStrokeWidth(mOutsideStrokeWidth);
        canvas.drawCircle((float) mCx, (float) mCy, (float) mOutsideRadius, mPaint);
        // 2. 绘制雷达区域背景
        mPaint.setAlpha(255);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mRadarStrokeWidth);
        mPaint.setColor(bgRadar);
        canvas.drawCircle((float) mCx, (float) mCy, (float) mInsideRadius * (float) mNumCicle, mPaint);
        // 3. 绘制园环
        mPaint.setAlpha(50);
        mPaint.setStrokeWidth(1f);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(colorCircle);
        for (int num = mNumCicle; num >= 1; --num) {
            if (num == 2) {
                mPaint.setAlpha(255);
                mPaint.setStrokeWidth(mRadarStrokeWidth);
            } else {
                mPaint.setAlpha(50);
                mPaint.setStrokeWidth(1f);
            }
            canvas.drawCircle((float) mCx, (float) mCy, (float) mInsideRadius * (float) num, mPaint);
        }
        // 4. 绘制圆弧线
        float radarBgRadius = (float) mInsideRadius * (float) mNumCicle;
        float ovalRadius = ((float) mOutsideRadius - mOutsideStrokeWidth / 2f + radarBgRadius + mRadarStrokeWidth / 2f) / 2f;
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAlpha(150);
        mPaint.setStrokeWidth(15f);
        RectF oval = new RectF();
        oval.left = (mCx - ovalRadius);
        oval.top = (mCy - ovalRadius);
        oval.right = (mCx + ovalRadius);
        oval.bottom = (mCy + ovalRadius);
        //绘制背景圆弧
        canvas.drawArc(oval, 20, 225, false, mPaint);

        // 4. 绘制对角线
        double angle = (double) (180 / mNumLines);
        float lineRadius = mInsideRadius * mNumCicle;
        for (int num = 0; num < mNumLines; ++num) {
            double radianS = Math.toRadians(num * angle); // 将角度转换为弧度
            double radianE = Math.toRadians(num * angle + 180);
            if (num == 0 || num == 2) {
                mPaint.setStrokeWidth(5f);
                mPaint.setAlpha(255);
            } else {
                mPaint.setStrokeWidth(3f);
                mPaint.setAlpha(50);
            }
            canvas.drawLine(
                    (float) (mCx + lineRadius * Math.cos(radianS)),
                    (float) (mCy + lineRadius * Math.sin(radianS)),
                    (float) (mCx + lineRadius * Math.cos(radianE)),
                    (float) (mCy + lineRadius * Math.sin(radianE)),
                    mPaint
            );
        }
        // 5.绘制扫描扇形图
        if (isSearching) {// 判断是否处于扫描
            canvas.save();
            canvas.rotate(
                    mCurrentAngel,
                    mCx,
                    mCy
            );

            mPaint.setStrokeWidth(5f);
            mPaint.setAlpha(255);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setShader(sweepGradient);
            mCurrentAngel += 3;
            canvas.drawCircle(
                    mCx,
                    mCy,
                    mInsideRadius * mNumCicle,
                    mPaint
            );
            canvas.restore();


            // 6.开始绘制动态点
            mPaint.setAlpha(255);
            mPaint.setColor(Color.RED);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setShader(null);
            mPaint.setStrokeWidth(20f);
//            if (!usePointBitmap) mPaint.setMaskFilter(outLightFilter); // 绘制圆的外发光, 需要关闭硬件加速

            for (int i = mPointArray.size() - 1; i >= 0; i--) { // 倒序，先画最新的，然后更改不透明度
                MyPoint p = mPointArray.get(i);
                int notShowAngelStart = p.angel - (360 - maxShowPointAngel);        //不显示的起始角
                if (notShowAngelStart > 0) {
                    //当前角度大于不显示的起始角且小于点所在的角度
                    if (mCurrentAngel % 360f > notShowAngelStart && mCurrentAngel % 360f < p.angel % 360f) {
//                        AppLog.D("notShowAngelStart > 0 remove point mCurrentAngel " + mCurrentAngel % 360f + "maxShowPointAngel " + maxShowPointAngel % 360f + "p.angel " + p.angel % 360f + "maxShowPointAngel + p.angel " + (maxShowPointAngel + p.angel) % 360f);
                        mPointArray.remove(i); //倒序移除
                    } else {
                        int sAngel = (mCurrentAngel - p.angel - 1) % 360; // 这里减一，是因为浮点型导致最后一个又正常显示了
//                        AppLog.D("sAngel" + sAngel);
                        if (sAngel == 0) {
                            mPaint.setAlpha(255);
                        } else {
                            mPaint.setAlpha((int) (255 - (float) sAngel / maxShowPointAngel * 255));
                        }

                        if (usePointBitmap) {
                            canvas.drawBitmap(
                                    pointImg,
                                    p.pos.x - pointImg.getWidth() / 2f,
                                    p.pos.y - pointImg.getHeight() / 2f,
                                    mPaint
                            );
                        } else {
                            canvas.drawCircle(
                                    p.pos.x,
                                    p.pos.y,
                                    pointRadius,
                                    mPaint
                            );
                        }
                    }
                } else {
                    notShowAngelStart = notShowAngelStart + 360;
                    //当前角度大于0且小于点所在的角度或者当前角度大于不显示的起始角且小于360
                    if (mCurrentAngel % 360f > 0 && mCurrentAngel % 360f < p.angel % 360f ||
                            mCurrentAngel % 360f > notShowAngelStart && mCurrentAngel % 360f < 360f) {
//                        AppLog.D("notShowAngelStart < 0 remove point mCurrentAngel " + mCurrentAngel % 360f + "maxShowPointAngel " + maxShowPointAngel % 360f + "p.angel " + p.angel % 360f + "maxShowPointAngel + p.angel " + (maxShowPointAngel + p.angel) % 360f);
                        mPointArray.remove(i); //倒序移除
                    } else {
                        int sAngel = (mCurrentAngel - p.angel - 1) % 360; // 这里减一，是因为浮点型导致最后一个又正常显示了
//                        AppLog.D("sAngel" + sAngel);
                        if (sAngel == 0) {
                            mPaint.setAlpha(255);
                        } else {
                            mPaint.setAlpha((int) (255 - (float) sAngel / maxShowPointAngel * 255));
                        }

                        if (usePointBitmap) {
                            canvas.drawBitmap(
                                    pointImg,
                                    p.pos.x - pointImg.getWidth() / 2f,
                                    p.pos.y - pointImg.getHeight() / 2f,
                                    mPaint
                            );
                        } else {
                            canvas.drawCircle(
                                    p.pos.x,
                                    p.pos.y,
                                    pointRadius,
                                    mPaint
                            );
                        }
                    }
                }

            }
            this.invalidate();
        }

    }

    public final void setSearching(boolean status) {
        isSearching = status;
        if (!isSearching) {
            mPointArray.clear();
        }

        invalidate();
    }

    public final boolean isSearching() {
        return isSearching;
    }

    /**
     * @param percent     从圆心开始到点的距离占圆心开始到雷达外圈的距离的百分比
     * @param mPointAngel 点的角度
     */
    public final void addPoint(float percent, int mPointAngel) {
        if (!isSearching) return;
        // 如果点的角度在当前角度范围内才去添加点
        //需要根据具体报值的速度改变刷新的数值范围，速度越快范围应该越大，默认为-6（6之内的数值）
        AppLog.D("mCurrentAngel=" + (mCurrentAngel % 360 - 36) + ",mPointAngel=" + mPointAngel % 360);
        if (mCurrentAngel % 360 - 36 < mPointAngel % 360 && mCurrentAngel % 360 >= mPointAngel % 360) {
            AppLog.D("addPoint percent=" + percent + ",mPointAngel=" + mPointAngel);
            double radian = Math.toRadians(mPointAngel);  //将角度转化为弧度
//            AppLog.D("radian" + radian);
            float lineRadius = mInsideRadius * mNumCicle * percent;
//            AppLog.D("mInsideRadius " + mInsideRadius + "mNumCicle " + mNumCicle + "lineRadius " + lineRadius);
            float pX = (float) (mCx + lineRadius * Math.cos(radian));
            float pY = (float) (mCy + lineRadius * Math.sin(radian));
//            AppLog.D("pX" + pX + "pY" + pY + "Math.cos(radian) " + Math.cos(radian) + "Math.sin(radian)" + Math.sin(radian));
            MyPoint point = new MyPoint(percent, new PointF(pX, pY), mPointAngel);
            mPointArray.add(point);
            if (mPointArray.size() > MAX_POINT_SHOW) { // 超过允许的点个数，移除最开始的一个
                mPointArray.remove(0); //TODO 测试是否存在重新分配数组的问题
            }
            this.invalidate();
        }

    }

    private int resolveMeasured(int measureSpec, int desired) {
        int specSize = MeasureSpec.getSize(measureSpec);
        int result;
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.max(specSize, desired);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            default:
                result = specSize;
                break;
        }

        return result;
    }

    public void setPointMipmap(int point) {
        pointImg = BitmapFactory.decodeResource(getResources(), point);
    }


    public static final class MyPoint {
        float percent;
        PointF pos;
        int angel;

        public String toString() {
            return "MyPoint(percent=" + percent + ", pos=" + pos + "， mOffsetArgs=" + angel + ",)";
        }

        public MyPoint(float percent, @NonNull PointF pos, int angel) {
            super();
            this.percent = percent;
            this.pos = pos;
            this.angel = angel;
        }
    }
}
