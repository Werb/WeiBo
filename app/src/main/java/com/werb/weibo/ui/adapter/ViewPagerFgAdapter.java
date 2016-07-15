package com.werb.weibo.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.werb.weibo.ui.fragment.BaseFragment;

import java.util.List;

/**
 * Created by Werb on 2016/7/8.
 * ViewPager 嵌套 Fragment 的数据适配器
 */
public class ViewPagerFgAdapter extends FragmentPagerAdapter {
    private List<BaseFragment> fragmentList;


    public ViewPagerFgAdapter(FragmentManager supportFragmentManager, List<BaseFragment> fragmentList) {
        super(supportFragmentManager);
        this.fragmentList = fragmentList;
    }

    @Override
    public Fragment getItem(int position) {

        return fragmentList.get(position);
    }



    @Override
    public int getCount() {
        if(fragmentList!=null){
            return fragmentList.size();
        }
        return 0;
    }
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "赞";
            case 1:
                return "转发";
            case 2:
                return "评论";
        }
        return null;
    }
}
