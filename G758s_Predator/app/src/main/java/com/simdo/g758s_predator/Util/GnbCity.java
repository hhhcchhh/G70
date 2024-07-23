/**
 * 配置定位参数表
 */
package com.simdo.g758s_predator.Util;

import com.simdo.g758s_predator.Bean.ArfcnTimingOffset;
import com.simdo.g758s_predator.Bean.CityBean;
import com.simdo.g758s_predator.MainActivity;
import com.simdo.g758s_predator.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GnbCity {
    private static GnbCity instance;

    public static GnbCity build() {
        synchronized (GnbCity.class) {
            if (instance == null) {
                instance = new GnbCity();
            }
        }
        return instance;
    }

    public GnbCity() {

    }

    /**
     * 解析城市列表
     */
    String splitIn = "&/%";
    String splitOut = "&;%";
    public void init() {
        cityList.clear();
        String list = PrefUtil.build().getValue("city_list_pref","").toString();
        if (!list.contains("/")) {
            cityList.add(new CityBean(MainActivity.getInstance().getString(R.string.sz), "3000000", "9030000", "9030000", "9038000", "9036000"));
        } else {
            String[] splits = list.split(splitOut);
            for (String split : splits) {
                String[] split1 = split.split(splitIn);
                cityList.add(new CityBean(split1[0], split1[1], split1[2], split1[3], split1[4], split1[5]));
            }
        }
    }

    /**
     * 添加城市
     */
    public boolean addCity(CityBean city) {
        boolean add = true;
        for (int i = 0; i < cityList.size(); i++) { // 相同城市，更换数据
            if (cityList.get(i).getCity().equals(city.getCity())) {
                cityList.get(i).setOffset_5g(city.getOffset_5g());
                cityList.get(i).setOffset_b34(city.getOffset_b34());
                cityList.get(i).setOffset_b39(city.getOffset_b39());
                cityList.get(i).setOffset_b40(city.getOffset_b40());
                cityList.get(i).setOffset_b41(city.getOffset_b41());
                add = false;
                break;
            }
        }
        if (add) cityList.add(city);
        save();
        return add;
    }

    /**
     * 添加城市
     */
    public void deleteCity(String city) {
        for (int i = 0; i < cityList.size(); i++) { // 相同城市，更换数据
            if (cityList.get(i).getCity().equals(city)) {
                cityList.remove(i);
                break;
            }
        }
        save();
    }

    /**
     * 保存城市数据，供下次启动调用
     */
    public void save() {
        StringBuilder saveStr = new StringBuilder();
        for (CityBean cityBean : cityList) {
            String info = cityBean.getCity() + splitIn + cityBean.getOffset_5g() + splitIn + cityBean.getOffset_b34() + splitIn + cityBean.getOffset_b39() + splitIn + cityBean.getOffset_b40() + splitIn + cityBean.getOffset_b41();
            saveStr.append(info);
            saveStr.append(splitOut);
        }
        PrefUtil.build().putValue("city_list_pref", saveStr.toString());
    }
    /**
     * 匹配当前选择城市
     */
    public void setCurCity(String city) {
        for (int i = 0; i < cityList.size(); i++) { // 相同城市，更换数据
            if (cityList.get(i).getCity().equals(city)) {
                CityBean tmp = cityList.get(i); // 当前城市置顶
                cityList.remove(i);
                cityList.add(0, tmp);
                break;
            }
        }
        save();
    }

    public int getTimingOffset(String band) {
        int offset = 0;
        if (cityList.size() > 0) {
            CityBean cityBean = cityList.get(0);
            switch (band){
                case "5G":
                    offset = Integer.parseInt(cityBean.getOffset_5g());
                    break;
                case "B34":
                    offset = Integer.parseInt(cityBean.getOffset_b34());
                    break;
                case "B39":
                    offset = Integer.parseInt(cityBean.getOffset_b39());
                    break;
                case "B40":
                    offset = Integer.parseInt(cityBean.getOffset_b40());
                    break;
                case "B41":
                    offset = Integer.parseInt(cityBean.getOffset_b41());
                    break;
            }
        }
        return offset;
    }

    public CityBean getCurCity() {
        if (cityList != null && cityList.size() > 0) {
            return cityList.get(0);
        }
        return null;
    }

    /**
     * 城市列表
     */
    public List<CityBean> getCityList() {
        return cityList;
    }

    List<CityBean> cityList = new ArrayList<>(); // 数据列表
}
