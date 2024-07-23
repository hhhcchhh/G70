package com.simdo.g73cs.View;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

import com.simdo.g73cs.R;

public class SlashTextView extends AppCompatTextView {
    private Paint paint;

    public SlashTextView(Context context) {
        super(context);
        init(context);
    }

    public SlashTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlashTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        Resources resources = context.getResources();
        //replace with your color
        int dividerColor = resources.getColor(R.color.color_b6b6b6);

        paint = new Paint();
        paint.setColor(dividerColor);
        //replace with your desired width
        paint.setStrokeWidth(3);
    }

    boolean drawLine = true;
    public void setIsDrawLine(boolean drawLine){
        this.drawLine = drawLine;
        invalidate();
    }

    public boolean getIsDrawLine(){
        return drawLine;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawLine) canvas.drawLine(0, getHeight() - 5, getWidth() - 5, 0, paint);
    }
}