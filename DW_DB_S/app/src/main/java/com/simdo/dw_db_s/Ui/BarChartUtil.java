package com.simdo.dw_db_s.Ui;

import android.graphics.Color;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态折线图工具类
 */
public class BarChartUtil {

    private final BarChart mBarChart;
    private final XAxis xAxis;
    private int addCount = 0;
    private int showIndex = 0;
    private final List<String> timeList = new ArrayList<>(); //存储x轴的时间

    private final ArrayList<BarDataSet> mBarDataSetArrayList = new ArrayList<>();
    private BarData mBarData;
    private boolean isMove = true;

    public BarChartUtil(BarChart mBarChart) {
        this.mBarChart = mBarChart;
        xAxis = mBarChart.getXAxis();
        initBarChart();
    }

    /**
     * 初始化BarChart
     */
    private void initBarChart() {
        mBarChart.setDoubleTapToZoomEnabled(true);
        // 不显示数据描述
        mBarChart.getDescription().setEnabled(false);
        // 没有数据的时候，显示“暂无数据”
        mBarChart.setNoDataText("暂无数据");
        //x轴y轴同时进行缩放
        mBarChart.setPinchZoom(false);
        //启用/禁用缩放图表上的两个轴。
        mBarChart.setScaleEnabled(false);
        mBarChart.setExtraOffsets(0, 0, 0, 0);//设置图表距离上下左右的距离
        mBarChart.zoom(1f, 1f, 0, 0);//显示的时候是按照多大的比率缩放显示,1f表示不放大缩小

        //设置为false以禁止通过在其上双击缩放图表。
        mBarChart.setDragEnabled(false); // 是否可以拖拽
        mBarChart.setHighlightPerDragEnabled(false); // 能否拖拽高亮线(数据点与坐标的提示线)，默认为true
        mBarChart.setDragDecelerationEnabled(false); // 拖拽滚动时，手放开是否会持续滚动，默认为true（false：拖到哪是哪，true：停止拖拽之后还会有缓冲）
        mBarChart.setDragDecelerationFrictionCoef(0.5f); // 与上面那个属性配合，持续滚动时的速度快慢，[0,1) 0代表立即停止。
        mBarChart.setDrawGridBackground(false);
        //显示边界
        mBarChart.setDrawBorders(true);

         mBarChart.setBorderColor(Color.BLACK); //设置 chart 边框线的颜色。
        // mBarChart.setBorderWidth(2); //设置 chart 边界线的宽度，单位 dp。

        //折线图例 标签 设置 这里不显示图例
        Legend legend = mBarChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);// 设置form的形状，正方形、圆形、线性
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);//显示位置
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setEnabled(true);

        xAxis.setEnabled(false); //设置轴启用或禁用 如果禁用以下的设置全部不生效
        timeList.add(""); // 添加一个空数据，避免X轴坐标一开始就全部摊开

        /**
         * 并排多列柱状图
         * float groupSpace = 0.3f;   //柱状图组之间的间距
         * float barSpace =  0.05f;  //每条柱状图之间的间距  一组两个柱状图
         * float barWidth = 0.3f;    //每条柱状图的宽度     一组两个柱状图
         * (barWidth + barSpace) * barAmount + groupSpace = (0.3 + 0.05) * 2 + 0.3 = 1.00
         * 3个数值 加起来 必须等于 1 即100% 按照百分比来计算 组间距 柱状图间距 柱状图宽度
         */
        mBarData = new BarData();
        mBarChart.setData(mBarData);
    }

    /**
     * 添加线(一条线)
     *
     * @param name
     * @param color
     */

    public void addBarDataSet(String name, int color){
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            entries.add(new BarEntry(i,-999));
        }
        BarDataSet barDataSet = new BarDataSet(entries, name);
        barDataSet.setColor(color);  // 设置线的颜色
        barDataSet.setValueTextSize(8f); //设置显示值的文字大小
        barDataSet.setDrawValues(true);//显示折线上的值
        barDataSet.setValueTextColor(color);
        barDataSet.setValueTextColor(Color.GRAY);//数据的颜色
        //最开始的时候才添加 BarDataSet（BarDataSet 代表一条线）
        mBarData.addDataSet(barDataSet);
        mBarDataSetArrayList.add(barDataSet);
//        mBarChart.invalidate();
    }

    /**
     * 动态添加数据（一条折线图）
     *
     * @param number
     */
    public void addEntry(int index, float number) {
        BarEntry entry = new BarEntry(mBarDataSetArrayList.get(index).getEntryCount(), number);
        mBarData.addEntry(entry, index);
        //设置组间距占比30% 每条柱状图宽度占比 70% /barAmount  柱状图间距占比 0%
//        float groupSpace = 0.05f; //柱状图组之间的间距
//        float barSpace = 0.1f;
        //设置柱状图宽度
        mBarData.setBarWidth(0.98f);
        //(起始点、柱状图组间距、柱状图之间间距)
//        mBarData.groupBars(0f, groupSpace, barSpace);
        //通知数据已经改变
        mBarData.notifyDataChanged();
        mBarChart.notifyDataSetChanged();
        //设置在曲线图中显示的最大数量
        mBarChart.setVisibleXRangeMaximum(15);
        //移到某个位置
        if (isMove) mBarChart.moveViewToX(mBarDataSetArrayList.get(index).getEntryCount());

    }

    /**
     * 设置是否移动
     * @param enabled
     */
    public void setEnabledMove(boolean enabled){
        isMove = enabled;
    }

    /**
     * 添加 X轴 时间线
     * @param time
     */
    public void addTime(String time){
        //避免集合数据过多，及时清空（做这样的处理，并不知道有没有用，但还是这样做了）
        if (timeList.size() > 5) {
            timeList.remove(0);
        }
        timeList.add(time);
        addCount++;
    }
    /**
     * 根据索引显示或隐藏指定线条
     */
    public void setLineShow(int index, boolean visible) {
        if (mBarData.getDataSets().size() <= 0) return;
        showIndex = index;
        mBarData.getDataSets().get(index).setVisible(visible);
        mBarChart.invalidate();
    }

    /**
     * 设置Y轴值
     */
    public void setXYAxis(float max, float min, int labelCount) {

        if (max < min) {
            float _min = max;
            max = min;
            min = _min;
        }

        YAxis leftAxis = mBarChart.getAxisLeft();
        YAxis rightAxis = mBarChart.getAxisRight();

        //保证Y轴从0开始，不然会上移一点
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(max);
        leftAxis.setAxisMinimum(min);
        leftAxis.setLabelCount(labelCount, false);

        rightAxis.setAxisMinimum(0f);
        rightAxis.setDrawGridLines(true);
        rightAxis.setAxisMaximum(max);
        rightAxis.setAxisMinimum(min);
        rightAxis.setLabelCount(labelCount, false);
        rightAxis.setEnabled(false);//是否显示右侧Y轴
        rightAxis.setTextColor(Color.TRANSPARENT);
        rightAxis.setAxisLineColor(Color.TRANSPARENT);
        rightAxis.setGridColor(Color.TRANSPARENT);
        mBarChart.invalidate();
    }
}
