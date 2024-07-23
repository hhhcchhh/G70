package com.g50.UI.Adpter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class FragmentAdapter extends FragmentPagerAdapter {
   List<Fragment> fragmentList;
   private String [] title;
   public FragmentAdapter(FragmentManager fm, List<Fragment> fragmentList, String [] title) {
      super(fm);
      this.fragmentList = fragmentList;
      this.title = title;
   }


   @NonNull
   @Override
   public Fragment getItem(int i) {
      return fragmentList.get(i);
   }

   @Override
   public int getCount() {
      return fragmentList.size();
   }
   @Override
   public CharSequence getPageTitle(int position) {
      return title[position];
   }
}
