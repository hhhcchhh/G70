package com.BarChart;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class BarChartView extends View {
	private String mTitle = "";
	private int mTitleFontSize = 20; // 标题字体大小
	private int mTitleColor = Color.GREEN; // 标题颜色
	private int mBgRectColor = Color.BLUE; // 边框颜色
	private int mBgColor = Color.WHITE; // 背景填充色
	private int mAxisColor = Color.BLACK; // 坐标轴颜色
	private int mAxisLableColor = Color.RED; // 坐标轴标签颜色
	private int mAxisLableFontSize = 14; // 字体大小
	private int mAxisStrokeWidth = 2; // 坐标轴线宽
	private int mAxisYMax; // Y轴最大值
	private int mAxisYMin; // Y轴最小值
	private int mAxisYStep; // Y轴刻度间距
	private int mMiddleAxle; // 中轴
	private int mAxisYChangeColorValue; // 柱状变色起始值
	private int mAxisYLableOffset = 23; // Y轴标签与轴之间的间距
	private int mAxisYAlignLeft = 39; // Y轴与左边界距离
	private int mAxisXStep = 12; // X轴每一刻度显示多少根柱子
	private boolean mIsPlusValue; // true = 正数, false = 负数
	private boolean mEnableDisplayAxisYLabel = false; // 是否显示柱状图上方的文字
	private List<String> mAxisXLableList  = new ArrayList<String>();
	private List<DataElement> mList  = new ArrayList<DataElement>();
	
	public int getMiddleAxle() {
		return mMiddleAxle;
	}

	public void setMiddleAxle(int mMiddleAxle) {
		this.mMiddleAxle = mMiddleAxle;
	}
	
	public boolean IsPlusValue() {
		return mIsPlusValue;
	}

	public void setIsPlusValue(boolean mIsPlusValue) {
		this.mIsPlusValue = mIsPlusValue;
	}

	public int getmAxisYStep() {
		return mAxisYStep;
	}

	public void setAxisYStep(int mAxisYStep) {
		this.mAxisYStep = mAxisYStep;
	}
	/**
	 * @return the mAxisColor
	 */
	public int getAxisColor() {
		return mAxisColor;
	}

	/**
	 * @param color the mAxisColor to set
	 */
	public void setAxisColor(int color) {
		this.mAxisColor = color;
	}

	/**
	 * @return the mAxisYChangeColorValue
	 */
	public int getAxisYChangeColorValue() {
		return mAxisYChangeColorValue;
	}

	/**
	 * @param Value the mAxisYChangeColorValue to set
	 */
	public void setAxisYChangeColorValue(int Value) {
		this.mAxisYChangeColorValue = Value;
	}

	/**
	 * @return the mAxisXStep
	 */
	public int getAxisXStep() {
		return mAxisXStep;
	}

	/**
	 * @param step the mAxisXStep to set
	 */
	public void setAxisXStep(int step) {
		this.mAxisXStep = step;
	}
	/**
	 * @return the mBgRectColor
	 */
	public int getBgRectColor() {
		return mBgRectColor;
	}

	/**
	 * @param color the mBgRectColor to set
	 */
	public void setBgRectColor(int color) {
		this.mBgRectColor = color;
	}

	/**
	 * @return the mBgColor
	 */
	public int getBgColor() {
		return mBgColor;
	}

	/**
	 * @param color the mBgColor to set
	 */
	public void setBgColor(int color) {
		this.mBgColor = color;
	}

	/**
	 * @return the mIsDisplayAxisYLabel
	 */
	public boolean isDisplayAxisYLabel() {
		return mEnableDisplayAxisYLabel;
	}

	/**
	 * @param value the mIsDisplayAxisYLabel to set
	 */
	public void enableDisplayAxisYLabel(boolean value) {
		this.mEnableDisplayAxisYLabel = value;
	}
	
	/**
	 * @return the mAxisYMax
	 */
	public int getAxisYMax() {
		return mAxisYMax;
	}

	/**
	 * @param YMax the mAxisYMax to set
	 */
	public void setAxisYMax(int YMax) {
		this.mAxisYMax = YMax;
	}

	/**
	 * @return the mAxisYMin
	 */
	public int getAxisYMin() {
		return mAxisYMin;
	}

	/**
	 * @param YMin the mAxisYMin to set
	 */
	public void setAxisYMin(int YMin) {
		this.mAxisYMin = YMin;
	}

	/**
	 * @return the mAxisXLableList
	 */
	public List<String> getAxisXLableList() {
		return mAxisXLableList;
	}

	/**
	 * @param XLableList the mAxisXLableList to set
	 */
	public void setAxisXLableList(List<String> XLableList) {
		this.mAxisXLableList.addAll(XLableList);
	}

	/**
	 * @return the mList
	 */
	public List<DataElement> getList() {
		return mList;
	}

	/**
	 * @param list the mList to set
	 */
	public void setList(List<DataElement> list) {
		this.mList.addAll(list);
	}
	
	public BarChartView(Context context, String title) {
		this(context);
		this.mTitle = title;
	}

	public BarChartView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public BarChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public BarChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onDraw(Canvas canvas) {
//		initTestData();
		// 获取控件宽、高
		int width = getWidth();
		int height = getHeight();
		// 绘画
		Paint myPaint = new Paint();
		myPaint.setAntiAlias(true);// 设置反锯齿效果,使得图像看起来更平滑
		// 绘制工作区
		myPaint.setColor(mBgRectColor); // 勾勒边沿
		myPaint.setStrokeWidth(2);
		canvas.drawRect(1, 1, width - 1, height - 1, myPaint);
		myPaint.setColor(mBgColor); // 设置背景色
		myPaint.setStrokeWidth(0);
		canvas.drawRect(3, 3, width - 3, height - 3, myPaint);
		// 绘制x,y轴: 设置偏移量，按x,y轴等比例缩放
		int xOffset = (int) (width * 0.1) - mAxisYAlignLeft ; // 离左边边界距离
		int yOffset = (int) (height * 0.1); // 离边底部边界距离
		myPaint.setStyle(Style.FILL);
		myPaint.setColor(mAxisColor);
		myPaint.setStrokeWidth(mAxisStrokeWidth);
		int padding_left = 2;
		int padding_right = 5;
		int padding_top = 2;
		int padding_bottom = 2;
		int axis_start_x = padding_left + xOffset;
		int axis_start_y = height - padding_bottom - yOffset;
		int axis_end_x = width - padding_right;
		int axis_end_y = padding_top;
		int axis_width = width - xOffset - padding_left - padding_right;
		int axis_height = height - yOffset - padding_bottom - padding_top;
		// X轴
		canvas.drawLine(axis_start_x, axis_start_y, axis_end_x + 3, axis_start_y, myPaint);
		// Y轴
		canvas.drawLine(axis_start_x, axis_start_y, axis_start_x, axis_end_y, myPaint);
		// 显示标题
		int title_h = 20;
		if (mTitle != null && !mTitle.equals("")) {
			title_h = 80; // TITLE显示高度
			myPaint.setTextSize(mTitleFontSize);
			myPaint.setColor(mTitleColor);
			canvas.drawText(mTitle, (width - 2) / 4, 30, myPaint);
		}
		
		// 绘制RX值
		/*String strRx = "-90";
		if (mList != null && mList.size() > 0) {
			if (mList.get(0).getValue() > 50) {
				myPaint.setColor(Color.BLACK);
			} else {
				myPaint.setColor(Color.RED);
			}
			strRx = "-" + mList.get(0).getValue();
		}
		myPaint.setTextSize(50);
		canvas.drawText(strRx, axis_width - 135, (int)padding_top + 50, myPaint);
		myPaint.setColor(Color.BLACK);
		myPaint.setTextSize(40);
		canvas.drawText("dbm", axis_width - 60, (int)padding_top + 50, myPaint);*/
		// 画距离
		/*
		String strRx = "100.0";
		if (mList != null && mList.size() > 0) {
			strRx = "-" + mList.get(0).getValue();
			strRx = Distance(Integer.valueOf(strRx));
			if (Double.valueOf(strRx) > 20.0) {
				myPaint.setColor(Color.BLACK);
			} else {
				myPaint.setColor(Color.RED);
			}
		}
		myPaint.setTextSize(50);
		canvas.drawText(strRx + "米", axis_width - 135, (int)padding_top + 50, myPaint);
		*/
		// 将x轴均等份
		int xUnit_value; // 每个刻度总宽度
		if (mAxisXLableList == null || mAxisXLableList.size() <= 0) {
			xUnit_value = axis_width / (mList.size() + 1);//将x轴均等份  
		} else {
			xUnit_value = (axis_width) / (mAxisXLableList.size() - 1);
		}
		
		// 绘制x轴上面的横坐标值及刻度
		for (int j = 0; j < mAxisXLableList.size(); j++) {
			String str = mAxisXLableList.get(j);
			int pos_x = xOffset + xUnit_value * j;
			int pos_y = height - yOffset + 28;
			myPaint.setColor(mAxisLableColor);
			myPaint.setTextSize(mAxisLableFontSize);
			if (j < 2) {
				canvas.drawText(str, pos_x - 5, pos_y - 3, myPaint);
			} else if (j == 2) {
				canvas.drawText(str, pos_x - 10, pos_y - 3, myPaint);
			} else {
				canvas.drawText(str, pos_x - 18, pos_y - 3, myPaint);
			}
			
			myPaint.setStyle(Style.FILL);
			myPaint.setColor(mAxisColor);
			myPaint.setStrokeWidth(mAxisStrokeWidth);
			canvas.drawLine(pos_x + padding_left, pos_y - 18, 
					pos_x + padding_left, axis_start_y, myPaint);
		}
		// 绘制y轴，将y轴按要求分成几等分
		double min = Math.abs(mAxisYMin);
		double max = Math.abs(mAxisYMax);
		double axis_max = max;
		double axis_min = min;
		double tmp = 0;
		if (max > min) {
			axis_max = max;
			tmp = max - min;
		} else {
			axis_max = min;
			tmp = min - max; // Y轴设置总高度
		}
		// 实际有几个刻度 = 设置总高度/每个高度值 
		double yUnit  = tmp / mAxisYStep; 
		// 实际每个刻度的高度值
		double yUnit_value = (axis_height - title_h) / yUnit;//减去顶部，为标题预留的高度值 
		// PathEffect 是指路径的方式，DashPathEffect是PathEffect的一个子类，
		// 其中的float数组，必须为偶数，且>=2,指定了多少长度的实线，之后再画多少长度的虚线。
		// 程序中，是先绘制长度为1的实线，再绘制长度为3的空白，1是偏移量。
		myPaint.setPathEffect(new DashPathEffect(new float[] {1, 3}, 1));
		for (int j = 0; j < yUnit; j++) {// 这个虚线的边界解决
			// 绘制虚线
			if (j == mMiddleAxle) { // 中轴
				myPaint.setColor(Color.RED);
				myPaint.setStyle(Style.FILL);
				myPaint.setStrokeWidth(2);
			} else {
				myPaint.setColor(Color.LTGRAY);
				myPaint.setStyle(Style.STROKE);
				myPaint.setStrokeWidth(1);
			}
			canvas.drawLine(axis_start_x, axis_start_y - ((int)yUnit_value * (j + 1)), 
					        axis_end_x, axis_start_y - ((int)yUnit_value * (j + 1)), myPaint);
			
			myPaint.setStyle(Style.FILL);
			myPaint.setColor(mAxisColor);
			myPaint.setStrokeWidth(mAxisStrokeWidth);
			canvas.drawLine(axis_start_x - 8, axis_start_y - ((int)yUnit_value * (j + 1)), 
					axis_start_x, axis_start_y - ((int)yUnit_value * (j + 1)), myPaint);
		}

		// 在y轴方向上，刻度赋值
		myPaint.setStyle(Style.STROKE);
		myPaint.setPathEffect(null);
		myPaint.setStrokeWidth(0);
		myPaint.setColor(mAxisLableColor);
		myPaint.setTextSize(mAxisLableFontSize);
		for (int j = 0; j <= yUnit; j++) {
			String axis = String.valueOf(mAxisYMin + j * mAxisYStep);
			canvas.drawText(axis, xOffset - mAxisYLableOffset - 5, axis_start_y - ((int)yUnit_value * (j)) + 4, myPaint);// xoffset-2避免字体显示的位置太靠近
		}

		// 绘制柱状图
		myPaint.setStyle(Style.FILL);
		myPaint.setStrokeWidth(2);
		for (int j = 0; j < mList.size(); j++) {
			DataElement element = mList.get(j);
			int barWidth = (int) (xUnit_value / mAxisXStep) + 1;
			double value = Math.abs(element.getValue());
			double o_value = axis_max - value; // 负值用
			if (IsPlusValue()) {
				o_value = value - axis_min;	// 正值用
			}
			// 刻度值*多少刻度
			double barHeight = yUnit_value * (o_value / mAxisYStep);
			int startPos_x = axis_start_x + barWidth * j + 1; 
			// 画起始Y轴坐标
			double startPos_y = axis_start_y - barHeight;
			int endPos_x = startPos_x + barWidth;
			if (endPos_x > (axis_width + xOffset)) {
				endPos_x = axis_width - padding_right; 
			}
			int endPos_y = axis_start_y - 1;
			// 渐变化开始坐标
			int color_value = Math.abs(mAxisYChangeColorValue);
			// 绘制柱状图
			// 当前设置颜色暂时不用
//			myPaint.setColor(mList.get(j).getColor());
			
			LinearGradient shader;
			if (value <= color_value) {
				// 不变色
				shader = new LinearGradient(startPos_x, (int)startPos_y, endPos_x, endPos_y,
						new int[] {Color.rgb(132, 134, 28), Color.rgb(71, 124, 80), Color.rgb(39, 138, 133)}, 
						null,//new float[] {0.25f, 0.5f, 0.75f, 1f},
						LinearGradient.TileMode.REPEAT);   
				myPaint.setShader(shader);
				canvas.drawRect(startPos_x + 1, (int)startPos_y, endPos_x, endPos_y, myPaint);
			} else {
				// 线性渐变色
				double color_h_value = axis_max - color_value;
				double color_barHeight = yUnit_value * (color_h_value / mAxisYStep);
				double color_startPos_y = axis_start_y - color_barHeight;
				shader = new LinearGradient(startPos_x, (int)color_startPos_y, endPos_x, endPos_y,
						new int[] {Color.rgb(132, 134, 28), Color.rgb(71, 124, 80), Color.rgb(39, 138, 133)}, 
						null, LinearGradient.TileMode.REPEAT);   
				myPaint.setShader(shader);
				canvas.drawRect(startPos_x + 1, (int)color_startPos_y, endPos_x, endPos_y, myPaint);
				// 渐变色
				shader = new LinearGradient(startPos_x, (int)startPos_y, endPos_x, (int)color_startPos_y,
						new int[] {Color.rgb(125, 51, 43), Color.rgb(132, 134, 28)}, 
						null, LinearGradient.TileMode.REPEAT);   
				myPaint.setShader(shader);
				canvas.drawRect(startPos_x + 1, (int)startPos_y, endPos_x, (int)color_startPos_y, myPaint);
				
				
			}
			if (mEnableDisplayAxisYLabel) {
				// 绘制柱状图上方数据值
				myPaint.setColor(Color.GREEN);
				myPaint.setTextSize(8);
				canvas.drawText(String.valueOf(element.getValue()), startPos_x, (int)startPos_y - 4, myPaint);
			}
			
		}
		// 绘图
		/*Bitmap bitmap;
		if (mList.size() > 0 && Math.abs(mList.get(0).getValue()) <= 60) {
			// 从资源文件中生成位图
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.alarm_on);
		} else {
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.alarm_off);
		}
		canvas.drawBitmap(bitmap, axis_start_x + 10, padding_top + 10, myPaint);*/
	}

	public void refreshData(List<DataElement> data) {
		if (mList != null) {
			mList.clear();
			mList.addAll(data);
			postInvalidate(); // 重绘控件
		}
	}
}