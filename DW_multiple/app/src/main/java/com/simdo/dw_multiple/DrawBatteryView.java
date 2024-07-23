package com.simdo.dw_multiple;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

public class DrawBatteryView extends View {

    private Paint batteryPaint = new Paint();    //电池画笔
    private Paint electricQuantityRedPaint = new Paint();     //红色电量画笔
    private Paint electricQuantityGreenPaint = new Paint();     //绿色电量画笔
    private int StrokeWidth = 10;               //笔画宽度
    private int ElectricQuantity = 0;          //电量

    //电池尺寸
    private int left = 0;
    private int top = 0;
    private int right = 50;
    private int bottom = 25;

    public DrawBatteryView(Context context) {
        super( context );
    }

    public DrawBatteryView(Context context, AttributeSet attrs) {
        super( context, attrs );
    }

    public DrawBatteryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super( context, attrs, defStyleAttr );
    }

    //外部调用的接口 设置电池尺寸
    public void setBatterySize(int size) {
        this.right = right + (int) (size * 1.5);
        this.bottom = bottom + (int) (size * 0.625);

        //更新调用onDraw重新绘制
        invalidate();
    }

    //外部调用的接口 设置电量 0-100
    public void setElectricQuantity(int electric) {
        if ( electric >= 0 && electric <= 100 ) {
            this.ElectricQuantity = electric;
        } else if ( electric < 0 ) {
            this.ElectricQuantity = 0;
        } else if ( electric > 100 ) {
            this.ElectricQuantity = 100;
        }

        //更新调用onDraw重新绘制
        invalidate();
    }

    @SuppressLint({"ResourceAsColor", "DrawAllocation"})
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw( canvas );

        batteryPaint.setColor( Color.GRAY );
//        batteryPaint.setStyle(Paint.Style.STROKE);   //设置空心
        batteryPaint.setStrokeWidth( StrokeWidth );

        electricQuantityRedPaint.setColor( Color.RED );
        electricQuantityRedPaint.setStrokeWidth( StrokeWidth );

        electricQuantityGreenPaint.setColor( Color.GREEN );
        electricQuantityGreenPaint.setStrokeWidth( StrokeWidth );

        //电池头部位置
        int BatteryHeadLeft = right;
        int BatteryHeadTop = top + (bottom - top) / 4;
        int BatteryHeadRight = right + ((right - left) / 15);
        int BatteryHeadBottom = top + ((bottom - top) / 4 * 3);

        //电量位置
        int ElectricQuantityLeft = left + 2;
        int ElectricQuantityTop = top +2;
        int ElectricQuantityRight = (int) (((right - left) / 100.0f) * ElectricQuantity) - 2;
        int ElectricQuantityBottom = bottom - 2;

        //电量为0时绘制感叹号
        int ExclamationMarkHeadLeft = (int) (((right - left) / 100.0f) * 10);
        int ExclamationMarkHeadTop = top + (bottom - top) / 4;
        int ExclamationMarkHeadRight = (int) (((right - left) / 100.0f) * 20);
        int ExclamationMarkHeadBottom = top + ((bottom - top) / 4 * 3);
        int ExclamationMarkBodyLeft = (int) (((right - left) / 100.0f) * 30);
        int ExclamationMarkBodyTop = top + (bottom - top) / 4;
        int ExclamationMarkBodyRight = (int) (((right - left) / 100.0f) * 85);
        int ExclamationMarkBodyBottom = top + ((bottom - top) / 4 * 3);


        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            canvas.drawRoundRect( left, top, right, bottom, 0, 0, batteryPaint );
            canvas.drawRoundRect( BatteryHeadLeft, BatteryHeadTop, BatteryHeadRight, BatteryHeadBottom, 0, 0, batteryPaint );

            //电量小于20的话使用红色画笔 否则使用绿色画笔
            if ( ElectricQuantity > 0 && ElectricQuantity < 20 ) {
                canvas.drawRoundRect( ElectricQuantityLeft, ElectricQuantityTop, ElectricQuantityRight, ElectricQuantityBottom, 0, 0, electricQuantityRedPaint );
            } else if ( ElectricQuantity >= 20 ) {
                canvas.drawRoundRect( ElectricQuantityLeft, ElectricQuantityTop, ElectricQuantityRight, ElectricQuantityBottom, 0, 0, electricQuantityGreenPaint );
            } else if ( ElectricQuantity == 0 ) {
                canvas.drawRoundRect( ExclamationMarkHeadLeft, ExclamationMarkHeadTop, ExclamationMarkHeadRight, ExclamationMarkHeadBottom, 0, 0, electricQuantityRedPaint );
                canvas.drawRoundRect( ExclamationMarkHeadLeft, ExclamationMarkHeadTop, ExclamationMarkHeadRight, ExclamationMarkHeadBottom, 0, 0, electricQuantityRedPaint );
            }

        } else {
            canvas.drawRoundRect( new RectF( left, top, right, bottom ), 0, 0, batteryPaint );
            canvas.drawRoundRect( new RectF( BatteryHeadLeft, BatteryHeadTop, BatteryHeadRight, BatteryHeadBottom ), 0, 0, batteryPaint );

            //电量小于20的话使用红色画笔 否则使用绿色画笔
            if ( ElectricQuantity > 0 && ElectricQuantity < 20 ) {
                canvas.drawRoundRect( new RectF( ElectricQuantityLeft, ElectricQuantityTop, ElectricQuantityRight, ElectricQuantityBottom ), 0, 0, electricQuantityRedPaint );
            } else if ( ElectricQuantity >= 20 ) {
                canvas.drawRoundRect( new RectF( ElectricQuantityLeft, ElectricQuantityTop, ElectricQuantityRight, ElectricQuantityBottom ), 0, 0, electricQuantityGreenPaint );
            } else if ( ElectricQuantity == 0 ) {
                canvas.drawRoundRect( new RectF( ExclamationMarkHeadLeft, ExclamationMarkHeadTop, ExclamationMarkHeadRight, ExclamationMarkHeadBottom ), 0, 0, electricQuantityRedPaint );
                canvas.drawRoundRect( new RectF( ExclamationMarkBodyLeft, ExclamationMarkBodyTop, ExclamationMarkBodyRight, ExclamationMarkBodyBottom ), 0, 0, electricQuantityRedPaint );
            }
        }
    }
}
