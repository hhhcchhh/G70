package com.simdo.g73cs.Fragment;

import java.util.Arrays;

//字符全排列
public class getStringPermutations {
    //递归方法：
    //从前往后将每个值放到第一位然后对其他的值做全排列
    //不能每次只传后一段进去递归的原因是你需要打印全部的话就不能只传一段进去不然没法打印
    //所以需要添加起始标表示现在到哪一段了
    //必须换回来因为不换回来一轮结束的时候是没法确定已经换过的数到哪里去的

    //试试只传一段进去的
    /*
     * 如果只传一段进去的话就需要提前打印
     * 确实是没法打的，因为当你循环进去之后就没法打前面的内容了。除非你保存传入，那就变成前面那种整个传进去是一样了。
     * */
    public static void getStringPermutations(char[] charArray, int begin) {
        //边界检查
        if (charArray != null && begin >= 0 && begin <= charArray.length)
            //结束值
            if (begin == charArray.length - 1) System.out.println(charArray);
        //递归
        for (int i = begin; i < charArray.length; i++) {
            swap(charArray, begin, i);
            getStringPermutations(charArray, begin + 1);
            swap(charArray, i, begin);
        }
    }

    public static void swap(char[] charArray, int from, int to) {
        char store = charArray[from];
        charArray[from] = charArray[to];
        charArray[to] = store;
    }


    //---------------------------------------------------------
    //非递归方法
    /*
    * 1234 1243 ‘1324’ 1342 ’1423’ ’1432’
    * 2134 ‘2143’ 2314 2341 ’2413’ ’2431’
    * ‘3124’ 3142 3214 3241 ’3412’ ’3421’
    * 4123 4132 4213 ‘4231’ ’4312’ ’4321’
    * */

    /*
    * 如果用原来的思路（轮流放第一个然后后面的换）要用循环的方法解决是个无限循环（有无限个后面的换），所以要换思路
    *
    * 循环charArray.length次，每个循环内将前一个和后一个换个位×
    * */

    /*
    * 字典序全排列
    * 12345，12354，"12435", "12453", "12534", "12543", "13245", ...
    * 数字从小变大
    * 一个数全升序是最小的，升序后面是降序是最大的
    * 算法本质：
    * 找到升序的倒数第二个数，将它和从右向左比他大一位的数交换，然后将后面的数翻转得到倒序的数
    * 因为比这个数大一位的数一定在升序的末尾倒数第二个数改变。
    * 如果不是升序的一定是倒序，所以换完之后还需要翻转变成升序，保证后面的数是最小的
    * 1、
    *
    *
    *
    * */
    public static void circulate1(char[] charArray){
        for (int i = 0; i < charArray.length; i++){
            for (int j = 0; j<charArray.length; j++){
                System.out.println(charArray);
                swap(charArray, charArray[j], charArray[j+1]);
            }
        }
    }


}
