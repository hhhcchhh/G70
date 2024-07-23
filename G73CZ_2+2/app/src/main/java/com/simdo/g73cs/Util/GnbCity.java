/**
 * 配置定位参数表
 */
package com.simdo.g73cs.Util;

import com.simdo.g73cs.Bean.ArfcnTimingOffset;
import com.simdo.g73cs.Bean.CityBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;

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
    public void init() {
        cityList.clear();
        String list = PrefUtil.build().getValue("city_list_pref","").toString();
        if (list.equals("")) {
            List<ArfcnTimingOffset> alist = new ArrayList<ArfcnTimingOffset>();
            alist.add(new ArfcnTimingOffset("504990", 0));
            alist.add(new ArfcnTimingOffset("723360", 0));
            alist.add(new ArfcnTimingOffset("38950", 9038000));
            //alist.add(new ArfcnTimingOffset(0));
            cityList.add(new CityBean(MainActivity.getInstance().getString(R.string.sz), alist));
        } else {
            try {
                JSONObject jb = new JSONObject(list);
                JSONArray jcity = jb.getJSONArray("city_list");
                if (jcity.length() > 0) {
                    for (int i = 0; i < jcity.length(); i++) {
                        JSONObject jbb = jcity.getJSONObject(i);
                        String city = jbb.getString("city");
                        JSONArray jarfcn = jbb.getJSONArray("arfcn_list");
                        List<ArfcnTimingOffset> alist = new ArrayList<ArfcnTimingOffset>();
                        for (int j = 0; j < jarfcn.length(); j++) {
                            JSONObject jbbb = jarfcn.getJSONObject(j);
                            String arfcn = jbbb.getString("arfcn");
                            int timingOffset = jbbb.getInt("timingOffset");
                            alist.add(new ArfcnTimingOffset(arfcn, timingOffset));
                        }
                        cityList.add(new CityBean(city, alist));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 添加城市
     *
     * @param city
     * @return
     */
    public boolean addCity(CityBean city) {
        boolean add = true;
        for (int i = 0; i < cityList.size(); i++) { // 相同城市，更换数据
            if (cityList.get(i).getCity().equals(city.getCity())) {
                cityList.get(i).setArfcnList(city.getArfcnList());
                add = false;
                break;
            }
        }
        if (add) {
            cityList.add(city);
        }
        save();
        return add;
    }

    /**
     * 添加城市
     *
     * @param city
     * @return
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
        try {
            JSONObject jb = new JSONObject();
            JSONArray jcity = new JSONArray();
            for (int i = 0; i < cityList.size(); i++) {
                JSONObject jbb = new JSONObject();
                jbb.put("city", cityList.get(i).getCity());

                JSONArray jarfcn = new JSONArray();
                List<ArfcnTimingOffset> alist = cityList.get(i).getArfcnList();
                for (int j = 0; j < alist.size(); j++) {
                    JSONObject jbbb = new JSONObject();
                    jbbb.put("arfcn", alist.get(j).getArfcn());
                    jbbb.put("timingOffset", alist.get(j).getTimingOffset());
                    jarfcn.put(jbbb);
                }
                jbb.put("arfcn_list", jarfcn);
                jcity.put(jbb);
            }
            jb.put("city_list", jcity);
            PrefUtil.build().putValue("city_list_pref", jb.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * 匹配当前选择城市
     *
     * @param city
     * @return
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

    public int getTimimgOffset(String arfcn) {
        int offset = 0;
        if (cityList.size() > 0) {
            List<ArfcnTimingOffset> alist = cityList.get(0).getArfcnList();
            for (int i = 0; i < alist.size(); i++) {
                if (alist.get(i).getArfcn().equals(arfcn)) {
                    offset = alist.get(i).getTimingOffset();
                }
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
     *
     * @return
     */
    public List<CityBean> getCityList() {
        return cityList;
    }

    public List<ArfcnTimingOffset> getArfcnList(String city) {
        for (int i = 0; i < cityList.size(); i++) { // 相同城市，更换数据
            if (cityList.get(i).getCity().equals(city)) {
                return cityList.get(i).getArfcnList();
            }
        }
        return null;
    }

    List<CityBean> cityList = new ArrayList<CityBean>(); // 数据列表
}
