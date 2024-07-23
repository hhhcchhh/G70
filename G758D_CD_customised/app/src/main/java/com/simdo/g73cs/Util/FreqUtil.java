package com.simdo.g73cs.Util;


import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.simdo.g73cs.Bean.ArfcnPciBean;
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

    /**
     * 对扫频列表做解析
     *
     * 不同设备，对应的通道支持的频段有所不同，此工具类可稍微调整，即可快速支持其他设备对应的通道频段
     * 调整点：decFreqList方法中的 几个switch，根据不同设备支持的频段，调整TD列表
     * */
    public LinkedList<LinkedList<ArfcnPciBean>> decFreqList(LinkedList<ScanArfcnBean> mFreqList) {
        // 调整顺序
        // 移广优先级： 5G:  N41---N28---N79   4G：B40-B3-B41-B39-B8-B34
        // 联电优先级： 5G:  N78->N1  4G：B1->B3->B5->B8   4/5G并存：B1->N1
        LinkedList<ScanArfcnBean> freqList = new LinkedList<>();
        HashMap<String, Integer> bandNum = new HashMap<>();
        for (ScanArfcnBean arfcnBean : mFreqList) {
            int dl_arfcn = arfcnBean.getDl_arfcn();
            String band_str = dl_arfcn > 100000 ? "N" + NrBand.earfcn2band(dl_arfcn) : "B" + LteBand.earfcn2band(dl_arfcn);
            int sizeIndex = 0;
            if (bandNum.containsKey(band_str)) sizeIndex = bandNum.get(band_str);
            String[] bands;
            switch (band_str){
                case "N41":
                case "N78": // 开头数据，直接添加
                    freqList.add(sizeIndex, arfcnBean);
                    break;
                case "N28": // 前面可能有N41
                    bands = new String[]{"N41"};
                    freqList.add(sizeIndex + getAddIndex(bandNum, bands), arfcnBean);
                    break;
                case "N79": // 前面可能有N41  N28
                    bands = new String[]{"N41", "N28"};
                    freqList.add(sizeIndex + getAddIndex(bandNum, bands), arfcnBean);
                    break;
                case "B40": // 前面可能有N41  N78
                    bands = new String[]{"N41", "N28", "N79"};
                    freqList.add(sizeIndex + getAddIndex(bandNum, bands), arfcnBean);
                    break;
                case "B1": // 前面可能有N78
                    bands = new String[]{"N78"};
                    freqList.add(sizeIndex + getAddIndex(bandNum, bands), arfcnBean);
                    break;
                case "N1": // 前面可能有N78 B1
                    bands = new String[]{"N78", "B1"};
                    freqList.add(sizeIndex + getAddIndex(bandNum, bands), arfcnBean);
                    break;
                case "B3": // 前面可能有N78 N1 B1  或者 N41 B40
                    bands = new String[]{"N78", "B1", "N1", "N41", "N28", "N79", "B40"};
                    freqList.add(sizeIndex + getAddIndex(bandNum, bands), arfcnBean);
                    break;
                case "B5": // 前面可能有N78 N1 B1 B3
                    bands = new String[]{"N78", "B1", "N1", "B3"};
                    freqList.add(sizeIndex + getAddIndex(bandNum, bands), arfcnBean);
                    break;
                case "B41": // 前面可能有N41 N28 N79 B40 B3
                    bands = new String[]{"N41", "N28", "N79", "B40", "B3"};
                    freqList.add(sizeIndex + getAddIndex(bandNum, bands), arfcnBean);
                    break;
                case "B39": // 前面可能有N41 N28 N79 B40 B3 B41
                    bands = new String[]{"N41", "N28", "N79", "B40", "B3", "B41"};
                    freqList.add(sizeIndex + getAddIndex(bandNum, bands), arfcnBean);
                    break;
                case "B8": // 前面可能有N41 N28 N79 B40 B3 B41  或者 N78 N1 B1 B3
                    bands = new String[]{"N41", "N28", "N79", "B40", "B3", "B41", "B39", "N78", "B1", "N1", "B5"};
                    freqList.add(sizeIndex + getAddIndex(bandNum, bands), arfcnBean);
                    break;
                case "B34": // 前面可能有N41 N28 N79 B40 B3 B39 B41 B39 B8
                    bands = new String[]{"N41", "N28", "N79", "B40", "B3", "B41", "B39", "B8"};
                    freqList.add(sizeIndex + getAddIndex(bandNum, bands), arfcnBean);
                    break;
            }

            sizeIndex += 1;
            bandNum.put(band_str, sizeIndex);
        }

        HashMap<String, LinkedList<SizeBean>> bandNumMap = new HashMap<>();  // 频段的个数，用于下发优先级排序
        LinkedList<ArfcnPciBean> TD1, TD2, TD3, TD4; // 第一方案的列表
        LinkedList<ArfcnPciBean> TD11, TD12, TD13, TD14; // 第二方案的列表
        TD1 = new LinkedList<>();
        TD2 = new LinkedList<>();
        TD3 = new LinkedList<>();
        TD4 = new LinkedList<>();

        TD11 = new LinkedList<>();
        TD12 = new LinkedList<>();
        TD13 = new LinkedList<>();
        TD14 = new LinkedList<>();

        for (ScanArfcnBean bean : freqList) {
            int arfcn = bean.getDl_arfcn();
            ArrayList<TdDataBean> tdData;
            if (arfcn > 100000) {
                int band = NrBand.earfcn2band(arfcn);
                if (PaCtl.build().isB97502) {
                    switch (band) {
                        case 41:
                        case 78:
                        case 79:
                            tdData = getTdData(freqList, TD1, TD11, bandNumMap, bean, arfcn, band);
                            if (tdData != null) {
                                TD1 = tdData.get(0).getTD1();
                                TD11 = tdData.get(0).getTD2();
                                bandNumMap = tdData.get(0).getBandNumMap();
                            }
                            break;
                        case 1:
                        case 28:
                            tdData = getTdData(freqList, TD3, TD13, bandNumMap, bean, arfcn, band);
                            if (tdData != null) {
                                TD3 = tdData.get(0).getTD1();
                                TD13 = tdData.get(0).getTD2();
                                bandNumMap = tdData.get(0).getBandNumMap();
                            }
                            break;
                    }
                } else {
                    switch (band) {
                        case 28:
                        case 78:
                        case 79:
                            tdData = getTdData(freqList, TD1, TD11, bandNumMap, bean, arfcn, band);
                            if (tdData != null) {
                                TD1 = tdData.get(0).getTD1();
                                TD11 = tdData.get(0).getTD2();
                                bandNumMap = tdData.get(0).getBandNumMap();
                            }
                            break;
                        case 1:
                        case 41:
                            tdData = getTdData(freqList, TD3, TD13, bandNumMap, bean, arfcn, band);
                            if (tdData != null) {
                                TD3 = tdData.get(0).getTD1();
                                TD13 = tdData.get(0).getTD2();
                                bandNumMap = tdData.get(0).getBandNumMap();
                            }
                            break;
                    }
                }
            } else {
                int band = LteBand.earfcn2band(arfcn);
                if (PaCtl.build().isB97502) {
                    switch (band) {
                        case 3:
                        case 5:
                        case 8:
                            tdData = getTdData(freqList, TD2, TD12, bandNumMap, bean, arfcn, band);
                            if (tdData != null) {
                                TD2 = tdData.get(0).getTD1();
                                TD12 = tdData.get(0).getTD2();
                                bandNumMap = tdData.get(0).getBandNumMap();
                            }
                            break;
                        case 34:
                        case 39:
                        case 40:
                        case 41:
                            tdData = getTdData(freqList, TD4, TD14, bandNumMap, bean, arfcn, band);
                            if (tdData != null) {
                                TD4 = tdData.get(0).getTD1();
                                TD14 = tdData.get(0).getTD2();
                                bandNumMap = tdData.get(0).getBandNumMap();
                            }
                            break;
                        case 1:
                            tdData = getTdData(freqList, TD3, TD13, bandNumMap, bean, arfcn, band);
                            if (tdData != null) {
                                TD3 = tdData.get(0).getTD1();
                                TD13 = tdData.get(0).getTD2();
                                bandNumMap = tdData.get(0).getBandNumMap();
                            }
                            break;
                    }
                } else {
                    switch (band) {
                        case 34:
                        case 39:
                        case 40:
                        case 41:
                            tdData = getTdData(freqList, TD2, TD12, bandNumMap, bean, arfcn, band);
                            if (tdData != null) {
                                TD2 = tdData.get(0).getTD1();
                                TD12 = tdData.get(0).getTD2();
                                bandNumMap = tdData.get(0).getBandNumMap();
                            }
                            break;
                        case 1:
                        case 3:
                        case 5:
                        case 8:
                            tdData = getTdData(freqList, TD4, TD14, bandNumMap, bean, arfcn, band);
                            if (tdData != null) {
                                TD4 = tdData.get(0).getTD1();
                                TD14 = tdData.get(0).getTD2();
                                bandNumMap = tdData.get(0).getBandNumMap();
                            }
                            break;
                    }
                }
            }
        }

        // 把第二方案的列表补到第一方案的后面，需要轮循的时候根据这个列表顺序轮循
        TD1.addAll(TD11);
        TD2.addAll(TD12);
        TD3.addAll(TD13);
        TD4.addAll(TD14);

        LinkedList<LinkedList<ArfcnPciBean>> result = new LinkedList<>();
        result.add(TD1);
        result.add(TD2);
        result.add(TD3);
        result.add(TD4);

        return result;
    }
    //主频点多次添加,count: 再添加count次
    public void addFreqMapMSByCount(LinkedHashMap<String, ArrayList<Integer>> freqMap, int count) {
        // 移广优先级： 5G:  N41---N28---N79   4G：B40-B3-B41-B39-B8-B34
        int num = 0;
        while (num < count) {
            // N41 504990、 512910、 516990、 507150、 525630
            ArrayList<Integer> listN41 = new ArrayList<>();
            listN41.add(504990);
            listN41.add(512910);
            listN41.add(516990);
            listN41.add(507150);
            listN41.add(525630);
            freqMap.put("N41_" + (num + 2), listN41);

            // B40 38950、 39148、 39292、 38750
            ArrayList<Integer> listB40 = new ArrayList<>();
            listB40.add(38950);
            listB40.add(39148);
            listB40.add(39292);
            listB40.add(38750);
            freqMap.put("B40_" + (num + 2), listB40);

            // B3 1300、 1275
            ArrayList<Integer> listB3MS = new ArrayList<>();
            listB3MS.add(1300);
            listB3MS.add(1275);
            listB3MS.add(1350);
            freqMap.put("B3_" + (num + 2), listB3MS);

            // B41 40936
            ArrayList<Integer> listB41MS = new ArrayList<>();
            listB41MS.add(40936);
            listB41MS.add(40340);
            listB41MS.add(41134);
            listB41MS.add(41332);
            freqMap.put("B41_" + (num + 2), listB41MS);

            // B39 38400、 38544
            ArrayList<Integer> listB39 = new ArrayList<>();
            listB39.add(38400);
            listB39.add(38544);
            freqMap.put("B39_" + (num + 2), listB39);

            num++;
        }
    }
    public void addFreqMapUTByCount(LinkedHashMap<String, ArrayList<Integer>> freqMap, int count) {
        int num = 0;
        while (num < count) {
            // 联电优先级： 5G:  N78->N1  4G：B1->B3->B5->B8   4/5G并存：B1->N1
            // N78 627264、 633984
            ArrayList<Integer> listN78 = new ArrayList<>();
            listN78.add(627264);
            listN78.add(633984);
            freqMap.put("N78_" + (num + 2), listN78);

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
            freqMap.put("B1_" + (num + 2), listB1);

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
            freqMap.put("B3_" + (num + 2), listB3UT);
            num++;
        }
    }

    public int getAddIndex(HashMap<String, Integer> bandNum, String[] bands){
        int addIndex = 0;
        for (String band : bands) {
            if (bandNum.containsKey(band)) addIndex += bandNum.get(band);
        }
        return addIndex;
    }
    private boolean listIsContains(LinkedList<ArfcnPciBean> td, int arfcn) {
        // 判断列表中是否包含该频点
        for (ArfcnPciBean bean : td) {
            if (bean.getArfcn().equals(String.valueOf(arfcn))) return true;
        }
        return false;
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

    private ArrayList<ResultBean> addData(LinkedList<ScanArfcnBean> freqList, ScanArfcnBean bean) {
        ArrayList<ResultBean> resultList = new ArrayList<>();
        LinkedList<ArfcnPciBean> TD1 = new LinkedList<>(); // 存储第一方案
        LinkedList<ArfcnPciBean> TD2 = new LinkedList<>(); // 存储第二方案
        int middleMaxRsrp = -70; //定义最大分界值
        int middleMinRsrp = -95; //定义最小分界值

        int arfcn = bean.getDl_arfcn();

        LinkedList<ScanArfcnBean> list = new LinkedList<>();
        for (ScanArfcnBean scanArfcnBean : freqList) {
            if (scanArfcnBean.getDl_arfcn() == arfcn) list.add(scanArfcnBean);
        }

        int size = list.size();
        if (size == 1) {
            // 仅扫出一个
            int rsrp = bean.getRsrp();
            int pci = bean.getPci();
            // 若 rsrp 大于 最大分界值
            if (rsrp > middleMaxRsrp) pci += 3;
            else pci += pci % 3 == 2 ? -1 : 1;
            // 5G 最大值 1007， 4G 最大值 503
            int max = bean.getDl_arfcn() > 100000 ? 1007 : 503;
            if (pci > max) pci = bean.getPci() - 3; // 遇到极值
            TD1.add(new ArfcnPciBean(String.valueOf(arfcn), String.valueOf(pci), rsrp));
        } else {
            // 扫出多个相同频点
            int rsrpMax = 0;
            int rsrpMin = 0;
            int indexMax = 0;
            int indexMin = 0;
            for (int i = 0; i < list.size(); i++) {
                int rsrp = list.get(i).getRsrp();
                if (rsrp > rsrpMax || rsrpMax == 0) {
                    // 从列表中筛选出最大值的rsrp
                    indexMax = i;
                    rsrpMax = rsrp;
                }

                if (rsrp < rsrpMin || rsrpMin == 0) {
                    // 从列表中筛选出最小值的rsrp
                    indexMin = i;
                    rsrpMin = rsrp;
                }
            }

            // 遍历完之后，判断2逻辑是否需要走延伸逻辑
            if (rsrpMin < middleMinRsrp) {
                // 方案一(最优)，若有 rsrp 小于 -95pci，可直接使用此pci, 添加到第一次启动的列表
                TD1.add(new ArfcnPciBean(String.valueOf(list.get(indexMin).getDl_arfcn()), String.valueOf(list.get(indexMin).getPci()), rsrpMin));
                // 方案二(次选)，其余，选最大的一个，添加到第二次启动的列表
                TD2.add(new ArfcnPciBean(String.valueOf(list.get(indexMax).getDl_arfcn()), String.valueOf(getPci(list, list.get(indexMax))), rsrpMax));
            } else {
                // 方案二(次选)，若所有 rsrp 均大于 -95pci，选最大的一个，添加到第一次启动的列表
                TD1.add(new ArfcnPciBean(String.valueOf(list.get(indexMax).getDl_arfcn()), String.valueOf(getPci(list, list.get(indexMax))), rsrpMax));
            }
        }

        // size 用于下发排序
        resultList.add(new ResultBean(size, TD1, TD2));
        return resultList;
    }

    private ArrayList<TdDataBean> getTdData(LinkedList<ScanArfcnBean> freqList, LinkedList<ArfcnPciBean> TD1, LinkedList<ArfcnPciBean> TD2, HashMap<String, LinkedList<SizeBean>> bandNumMap, ScanArfcnBean bean, int arfcn, int band) {
        ArrayList<TdDataBean> list = null;
        if (!listIsContains(TD1, arfcn)) {
            // 根据方案添加数据，获取添加结果
            ArrayList<ResultBean> resultBeans = addData(freqList, bean);
            int size = resultBeans.get(0).getSize();
            int maxRsrp = resultBeans.get(0).getTD1().get(0).getRsrp();
            if (resultBeans.get(0).getTD2().size() > 0) maxRsrp = resultBeans.get(0).getTD2().get(0).getRsrp();
            String bandStr = (arfcn > 100000 ? "N" : "B") + band;
            // 根据个数排序，多的在前，少的在后
            if (bandNumMap.containsKey(bandStr)) {
                LinkedList<SizeBean> listNum = bandNumMap.get(bandStr);
                // index, 插入索引，用于判断要插到该频段其他频点的，前还是后
                int index = listNum.size();
                for (int i = 0; i < listNum.size(); i++) {
                    if (maxRsrp > -85 || maxRsrp < -105){ // 扫频 RSRP > -85 或RSRP < -105，按照RSRP大小来选择，RSRP越大，优先权越高
                        boolean isAdd = false;
                        do {
                            if (maxRsrp > listNum.get(i).getMaxRsrp()){
                                listNum.add(i, new SizeBean(size, bean.getDl_arfcn(), bean.getRsrp(), maxRsrp));
                                index = i; // 将索引更新为最开始的地方
                                isAdd = true;
                                break;
                            }
                            i++;
                        }while (i < listNum.size() && size == listNum.get(i).getSize());
                        if (!isAdd) {
                            index = i + 1;
                            listNum.add(new SizeBean(size, bean.getDl_arfcn(), bean.getRsrp(), maxRsrp));
                        }
                        break;
                    }else { // 扫频-105 < RSRP <-85 时按照扫频 PCI数量多少来选择，个数越多，优选权越高
                        if (size > listNum.get(i).getSize()) {
                            listNum.add(i, new SizeBean(size, bean.getDl_arfcn(), bean.getRsrp(), maxRsrp));
                            index = i; // 将索引更新为最开始的地方
                            break;
                        }else if (size == listNum.get(i).getSize()){
                            // 表示和上一个相等数量的
                            // 判断是否有多个相等数量的，如果有，按照扫频强度rsrp来选择，rsrp越大，优先权越高
                            boolean isAdd = false;
                            do {
                                if (bean.getRsrp() > listNum.get(i).getRsrp()){
                                    listNum.add(i, new SizeBean(size, bean.getDl_arfcn(), bean.getRsrp(), maxRsrp));
                                    index = i; // 将索引更新为最开始的地方
                                    isAdd = true;
                                    break;
                                }
                                i++;
                            }while (i < listNum.size() && size == listNum.get(i).getSize());
                            if (!isAdd) {
                                index = i + 1;
                                listNum.add(new SizeBean(size, bean.getDl_arfcn(), bean.getRsrp(), maxRsrp));
                            }
                            break;
                        }
                    }
                }
                // 更新bandNumMap
                bandNumMap.put(bandStr, listNum);
                // TD1根据索引添加最新数据
                for (int i = 0; i < TD1.size(); i++) {
                    int band_this = TD1.get(i).getArfcn().length() < 6 ? LteBand.earfcn2band(Integer.parseInt(TD1.get(i).getArfcn())) : NrBand.earfcn2band(Integer.parseInt(TD1.get(i).getArfcn()));
                    if (band_this == band) {
                        int td1_index = i + index;
                        if (td1_index > TD1.size()) td1_index = TD1.size();
                        TD1.addAll(td1_index, resultBeans.get(0).getTD1());
                        break;
                    }
                }
                // TD2根据索引添加最新数据，TD2就不做比较了，都是喽货没必要
                for (int i = 0; i < TD2.size(); i++) {
                    int band_this = TD2.get(i).getArfcn().length() < 6 ? LteBand.earfcn2band(Integer.parseInt(TD2.get(i).getArfcn())) : NrBand.earfcn2band(Integer.parseInt(TD2.get(i).getArfcn()));
                    if (band_this == band) {
                        int td2_index = i + index;
                        if (td2_index > TD2.size()) td2_index = TD2.size();
                        TD2.addAll(td2_index, resultBeans.get(0).getTD2());
                        break;
                    }
                }
            } else {
                // bandNumMap不包含对应键值，表示第一个，直接往列表最后位置添加
                LinkedList<SizeBean> listNum = new LinkedList<>();
                listNum.add(new SizeBean(size, bean.getDl_arfcn(), bean.getRsrp(), maxRsrp));
                bandNumMap.put(bandStr, listNum);

                TD1.addAll(resultBeans.get(0).getTD1());
                TD2.addAll(resultBeans.get(0).getTD2());
            }
            list = new ArrayList<>();
            list.add(new TdDataBean(bandNumMap, TD1, TD2));
        }

        return list;
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

    private static class ResultBean {

        int size;
        LinkedList<ArfcnPciBean> TD1, TD2;

        public ResultBean(int size, LinkedList<ArfcnPciBean> TD1, LinkedList<ArfcnPciBean> TD2) {
            this.size = size;
            this.TD1 = TD1;
            this.TD2 = TD2;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public LinkedList<ArfcnPciBean> getTD1() {
            return TD1;
        }

        public LinkedList<ArfcnPciBean> getTD2() {
            return TD2;
        }
    }

    private static class TdDataBean {

        public TdDataBean(HashMap<String, LinkedList<SizeBean>> bandNumMap, LinkedList<ArfcnPciBean> TD1, LinkedList<ArfcnPciBean> TD2) {
            this.bandNumMap = bandNumMap;
            this.TD1 = TD1;
            this.TD2 = TD2;
        }

        public HashMap<String, LinkedList<SizeBean>> getBandNumMap() {
            return bandNumMap;
        }

        public LinkedList<ArfcnPciBean> getTD1() {
            return TD1;
        }

        public LinkedList<ArfcnPciBean> getTD2() {
            return TD2;
        }

        HashMap<String, LinkedList<SizeBean>> bandNumMap;
        LinkedList<ArfcnPciBean> TD1, TD2;
    }
}
