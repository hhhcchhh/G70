package com.simdo.g73cs.Fragment;

//任务调度
public class teskDispatch {
    /*
     * 给你k个任务和n台机器，其中机器i处理一个任务所需的时间为t_{i};
     * 求处理所有任务所需的最短时间。
     * 比如k=8，n=3，t[1..3] ={2,3,1}时，最短时间为9
     * */
    /*
     * 递归思路
     * 将一个任务分配给一台机器，然后递归分配剩余任务。
     * 尝试探索所有可能的任务分配方式，然后找到最短时间的分配方式。
     * */
    /*
     * 非递归分治思路
     * 当时间固定时此时的任务是可以求出来的
     * 所以解题关键在于找到最大的时间，然后将时间分割直到找到需要的时间为止
     *
     * */
    public static int findShortestTime(int meshines, int task, int[] times) {
        //
        int minTime = 1;
        int maxTime = getMaxTime(times, task);
        times = new int[]{1, 2, 3};

        int totalTime = (minTime + maxTime) / 2;
        int completedTask = completedTask(totalTime, times);
        while (maxTime > minTime) {
            if (completedTask > task) {
                maxTime = totalTime - 1;
                totalTime = (minTime + maxTime) / 2;
            } else if (completedTask < task){
                minTime = totalTime + 1;
                totalTime = (minTime + maxTime) / 2;
            }
            completedTask = completedTask(totalTime, times);
        }
        return maxTime;
    }

    private static int getMaxTime(int[] times, int task) {
        int minTime = Integer.MAX_VALUE;
        for (int time : times) {
            if (time < minTime) minTime = time;
        }
        return minTime * task;
    }

    //输入时间、机器耗费的时间数列，返回任务量
    private static int completedTask(int totalTime, int[] times) {
        int completedTask = 0;
        for (int time : times) {
            completedTask += totalTime / time;
        }
        return completedTask;
    }


}
