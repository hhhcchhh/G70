package com.simdo.g73cs.Util;


import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.simdo.g73cs.Bean.ScanArfcnBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class FreqUtil {
    private static FreqUtil instance;

    public FreqUtil() {
    }

    public static FreqUtil build() {
        synchronized (FreqUtil.class) {
            if (instance == null) instance = new FreqUtil();
        }
        return instance;
    }

    public LinkedHashMap<String, ArrayList<Integer>> getFreqMapMS(){

        // 移广优先级： 5G:  N41---N28---N79   4G：B40-B3-B41-B39-B8-B34

        // freqMap
        LinkedHashMap<String, ArrayList<Integer>> freqMap = new LinkedHashMap<>();

        // N41 504990、 512910、 516990、 507150、 525630
        ArrayList<Integer> listN41 = new ArrayList<>();
        listN41.add(504990);
        listN41.add(512910);
        listN41.add(516990);
        listN41.add(507150);
        listN41.add(525630);
        freqMap.put("N41", listN41);

        // N28 154810、 152650、 152890、 156970、 154570、 156490、 155770
        ArrayList<Integer> listN28 = new ArrayList<>();
        listN28.add(154810);
        listN28.add(152650);
        listN28.add(152890);
        listN28.add(156970);
        listN28.add(154570);
        listN28.add(156490);
        listN28.add(155770);
        freqMap.put("N28", listN28);

        // N79 723360
        ArrayList<Integer> listN79 = new ArrayList<>();
        listN79.add(723360);
        freqMap.put("N79", listN79);

        // B40 38950、 39148、 39292、 38750
        ArrayList<Integer> listB40 = new ArrayList<>();
        listB40.add(38950);
        listB40.add(39148);
        listB40.add(39292);
        listB40.add(38750);
        freqMap.put("B40", listB40);

        // B3 1300、 1275
        ArrayList<Integer> listB3MS = new ArrayList<>();
        listB3MS.add(1300);
        listB3MS.add(1275);
        listB3MS.add(1350);
        freqMap.put("B3", listB3MS);

        // B41 40936
        ArrayList<Integer> listB41MS = new ArrayList<>();
        listB41MS.add(40936);
        listB41MS.add(40340);
        listB41MS.add(41134);
        listB41MS.add(41332);
        freqMap.put("B41", listB41MS);

        // B39 38400、 38544
        ArrayList<Integer> listB39 = new ArrayList<>();
        listB39.add(38400);
        listB39.add(38544);
        freqMap.put("B39", listB39);

        // B8 3682、 3683、 3641、 3621、 3590、 3725、 3768、 3769、 3770、 3775
        ArrayList<Integer> listB8MS = new ArrayList<>();
        listB8MS.add(3682);
        listB8MS.add(3683);
        listB8MS.add(3641);
        listB8MS.add(3621);
        listB8MS.add(3590);
        freqMap.put("B8", listB8MS);

        // B34 36275
        ArrayList<Integer> listB34 = new ArrayList<>();
        listB34.add(36275);
        freqMap.put("B34", listB34);

        return freqMap;
    }
    public LinkedHashMap<String, ArrayList<Integer>> getFreqMapUT(){

        // 联电优先级： 5G:  N78---N1  4G：B1-B3-B5-B8

        // freqMap
        LinkedHashMap<String, ArrayList<Integer>> freqMap = new LinkedHashMap<>();

        // N78 627264、 633984
        ArrayList<Integer> listN78 = new ArrayList<>();
        listN78.add(627264);
        listN78.add(633984);
        freqMap.put("N78", listN78);

        // N1 427250、422890、 428910、 426030
        ArrayList<Integer> listN1 = new ArrayList<>();
        listN1.add(427250);
        listN1.add(422890);
        listN1.add(428910);
        listN1.add(426030);
        listN1.add(427210);
        listN1.add(426750);
        listN1.add(422930);
        freqMap.put("N1", listN1);

        // B1  350、 375、 400、 450、 500、 100
        ArrayList<Integer> listB1 = new ArrayList<>();
        listB1.add(100);
        listB1.add(300);
        listB1.add(50);
        listB1.add(350);
        listB1.add(375);
        listB1.add(400);
        listB1.add(450);
        listB1.add(500);
        freqMap.put("B1", listB1);

        // B3 1650、 1506、 1500、 1531、 1524、 1850
        ArrayList<Integer> listB3UT = new ArrayList<>();
        listB3UT.add(1650);
        listB3UT.add(1506);
        listB3UT.add(1500);
        listB3UT.add(1531);
        listB3UT.add(1524);
        listB3UT.add(1850);
        listB3UT.add(1600);
        listB3UT.add(1800);
        listB3UT.add(1825);
        freqMap.put("B3", listB3UT);

        // B5 2452
        ArrayList<Integer> listB5 = new ArrayList<>();
        listB5.add(2452);
        freqMap.put("B5", listB5);

        ArrayList<Integer> listB8UT = new ArrayList<>();
        listB8UT.add(3725);
        listB8UT.add(3768);
        listB8UT.add(3769);
        listB8UT.add(3770);
        listB8UT.add(3775);
        listB8UT.add(3745);
        listB8UT.add(3710);
        listB8UT.add(3737);
        listB8UT.add(3741);
        freqMap.put("B8", listB8UT);

        return freqMap;
    }

    private int getPci(LinkedList<ScanArfcnBean> list, ScanArfcnBean bean) {
        /*---------------------
         *     扫频导入 算法
         *---------------------
         * 方案一(最优)：
         * 若 rsrp 大于 分界值，按照+3原则。 （已在addData方法中做了处理）

         * 方案二(次选)：
         * 若 rsrp 均小于 分界值，则需遍历对比，选择 rsrp 值最大的做 % 3 取余，根据余数情况对pci值做 +- 1 运算
         *    2.1) 余数为 0，则 pci值 + 1或2（避开公网其他PCI MOD3，不能避开，选择rsrp小的mod相同）
         *    2.2) 余数为 1，则 pci值 + 1或-1（同上）
         *    2.3) 余数为 2，则 pci值 - 1或-2（同上）

         * 方案三(最次)：
         * 若 方案二 +- 1运算后的pci值，在此次扫频结果中存在，表示冲突，需重新对pci值 +- 1 再运算一次，如遇到极值，处理如下
         *    3.1) 4G 若pci极值大于 503，则使用 pci值 - 3 后的结果
         *    3.2) 5G 若pci极值大于 1007，则使用 pci值 - 3 后的结果
         * */

        // 此处从方案二(次选)开始，判断逻辑是否需要走延伸方案三(最次)逻辑
        int pci = bean.getPci();
        // 余数为 2  pci值 - 1, 余数为 0/1  pci值 + 1
        pci += pci % 3 == 2 ? -1 : 1;

        // 5G 最大值 1007， 4G 最大值 503
        int max = bean.getDl_arfcn() > 100000 ? 1007 : 503;

        for (ScanArfcnBean bean1 : list) {
            // 第一次+-1处理后，已有在列表中，需要走延伸方案三(最次)逻辑
            if (bean1.getPci() == pci) {
                // 重新对pci值 +- 1 再运算一次
                pci = bean.getPci();
                // 余数为 0  pci值 + 1, 余数为 1/2  pci值 - 1
                pci += pci % 3 == 2 ? -1 : 1;

                int rsrpMin = 0, indexMin = 0;
                boolean isAsIn = false;
                for (int i = 0; i < list.size(); i++) {
                    // 第二次+-1处理后，若仍有在列表中，选择与rsrp小的pci相同
                    if (list.get(i).getRsrp() < rsrpMin || rsrpMin == 0) {
                        // 从列表中筛选出最小值的rsrp
                        indexMin = i;
                        rsrpMin = list.get(i).getRsrp();
                    }
                    if (list.get(i).getPci() == pci) isAsIn = true;
                }
                // 如不能避开公网其他PCI，选择与rsrp小的pci相同
                if (isAsIn) pci = list.get(indexMin).getPci();
                break;
            }
        }
        if (pci > max) pci = bean.getPci() - 3; // 遇到极值
        return pci;
    }



    private static class SizeBean {

        public SizeBean(int size, int arfcn, int rsrp, int maxRsrp) {
            this.size = size;
            Arfcn = arfcn;
            this.rsrp = rsrp;
            this.maxRsrp = maxRsrp;
        }

        public int getSize() {
            return size;
        }

        public int getArfcn() {
            return Arfcn;
        }

        public int getRsrp() {
            return rsrp;
        }
        public int getMaxRsrp() {
            return maxRsrp;
        }

        public void setMaxRsrp(int maxRsrp) {
            this.maxRsrp = maxRsrp;
        }
        int size;
        int Arfcn;
        int rsrp;
        int maxRsrp;
    }


    private static class TdDataBean {


        public HashMap<String, LinkedList<SizeBean>> getBandNumMap() {
            return bandNumMap;
        }


        HashMap<String, LinkedList<SizeBean>> bandNumMap;
    }
}
