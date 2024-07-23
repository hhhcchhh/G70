package com.simdo.g73cs.Fragment;

public class FibonacciSequence {
    /**
     * Title: 斐波纳契数列
     * <p>
     * Description: 斐波纳契数列，又称黄金分割数列，指的是这样一个数列：1、1、2、3、5、8、13、21、……
     * 在数学上，斐波纳契数列以如下被以递归的方法定义：F0=0，F1=1，Fn=F(n-1)+F(n-2)（n>=2，n∈N*）。
     * <p>
     * 两种递归解法：经典解法和优化解法
     * 两种非递归解法：递推法和数组法
     **/
    //递归做法
    public static int FibonacciRecursion(int n) {
        //结束值
        if (n == 0) return 0;
        if (n == 1) return 1;
        //递归
        return FibonacciRecursion(n - 1) + FibonacciRecursion(n - 2);
    }

    //优化first是数列的第一个数，second是数列的第二个数，n是此数列的第几项
    //如何想到要构造这样一个函数呢？
    //想把两层递归合成一层，关键 在于将两个递归表示的数用一个递归表示（将两层的关系合成一层）
    //实质是利用传参保存了下一个计算出来的值，那样就不需要重复计算了。
    public static int FibonacciRecursion2(int first, int second, int n) {
        //结束值
        if (n <= 0) return -1;
        if (n == 1) return first;
        if (n == 2) return second;
        if (n == 3) return first + second;
        //递归（F(f,s,n) = F(s, f+s, n-1)）
        return FibonacciRecursion2(second, first + second, n - 1);
    }

}
