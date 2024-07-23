package com.simdo.g73cs.Util;

import java.util.ArrayList;
import java.util.Collections;

public class ParseDataUtil {
    private static ParseDataUtil instance;
    private final ArrayList<ArrayList<Integer>> lists;
    private final int[] getCount; // 读取次数，判断是否掉线用
    private final int[] rx; // 缓存当前四个通道的最新报值

    public ParseDataUtil() {
        lists = new ArrayList<>();
        getCount = new int[]{0, 0, 0, 0};
        rx = new int[]{0, 0, 0, 0};
        for (int i = 0; i < 4; i++) lists.add(new ArrayList<>());
    }

    public static ParseDataUtil build() {
        synchronized (ParseDataUtil.class) {
            if (instance == null) instance = new ParseDataUtil();
        }
        return instance;
    }

    /**初始化数据，用于启动前或结束后调用*/
    public void initData() {
        synchronized (ParseDataUtil.class) {
            for (int i = 0; i < 4; i++) {
                getCount[i] = 0;
                rx[i] = 0;
                lists.get(i).clear();
            }
        }
    }

    /**
     * 添加数据到列表，指定的IMSI上号时调用
     * @param cell_id 通道ID 0 ，1，2，3
     * @param rsrp 报值数据
     */
    public void addDataToList(int cell_id, int rsrp) {
        synchronized (ParseDataUtil.class) {
            lists.get(cell_id).add(rsrp);
        }
    }

    /**
     * 统一获取报值的接口，建议每1s调用一次，rsrp返回-1表示掉线
     * @param mode 模式，0：实时  1：权重  2：舍弃>20, 然后去高去低，再平均  3：中位数
     * @param cell_id 通道ID
     * @return rsrp报值
     */
    public int getRxByMode(int mode, int cell_id) {
        ArrayList<Integer> list;
        synchronized (ParseDataUtil.class) {
            list = new ArrayList<>(lists.get(cell_id));
            lists.get(cell_id).clear();
        }

        if (list.size() < 1) { // 如果没有数据上报，说明有可能已经掉线
            if (getCount[cell_id] < 5) getCount[cell_id]++; // 缓冲5次，若读取5次都是没有数据上报说明已掉线
            else rx[cell_id] = -1;
            return rx[cell_id];
        } else getCount[cell_id] = 0;

        int result = 0;
        switch (mode) {
            case 0:
                result = getRxByRealtime(list);
                break;
            case 1:
                result = getRxByWeight(cell_id, list);
                break;
            case 2:
                result = getRxByAverage(cell_id, list);
                break;
            case 3:
                result = getRxByMedian(list);
                break;
        }
        rx[cell_id] = result;
        return result;
    }

    private int getRxByWeight(int cell_id, ArrayList<Integer> list) {
        // 先获取平均值
        int result = getRxByAverage(cell_id, list);
        // 如果上次有报值，那么 上次报值*60% + 新数值*40%
        if (rx[cell_id] > 5) result = (int) (rx[cell_id] * 0.6 + result * 0.4);
        return result;
    }

    private int getRxByAverage(int cell_id, ArrayList<Integer> list) {
        ArrayList<Integer> oldList = new ArrayList<>(list);
        if (rx[cell_id] > 5) {
            for (int i = list.size() - 1; i > -1; i--)
                // 如果上次有报值，以上次报值为基数，下一个值大于或小于20的舍弃掉
                if (Math.abs(list.get(i) - rx[cell_id]) > 20) list.remove(i);
        }
        //list.size()等于0表示全部都是大于或小于20的，舍弃会显得实时性报值不合理，这里直接换成不舍弃的列表做平均
        if (oldList.size() > 0 && list.size() == 0) list.addAll(oldList);
        // 先排序
        Collections.sort(list);
        if (list.size() > 5) { // 如果列表个数大于5个，舍弃头尾(去高去低)
            list.remove(list.size() - 1);
            list.remove(0);
        }
        if (list.size() == 0) return rx[cell_id];
        // 求和平均
        int sum = 0;
        for (int num : list) sum += num;
        return sum / list.size();
        //return list.stream().collect(Collectors.averagingInt(Integer::intValue));
    }

    private int getRxByMedian(ArrayList<Integer> list) {
        // 先排序
        Collections.sort(list);
        int size = list.size();
        // 再根据列表是奇数返回中间的数，偶数返回中间两个数相加再平均
        return size % 2 == 1 ? list.get((size - 1) / 2) : (list.get(size / 2 - 1) + list.get(size / 2)) / 2;
    }

    private int getRxByRealtime(ArrayList<Integer> list) {
        return list.get(list.size() - 1); // 直接返回最新的数据
    }
}
