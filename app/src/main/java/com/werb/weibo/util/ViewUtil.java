package com.werb.weibo.util;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Werb on 2016/7/14.
 * Email：1025004680@qq.com
 * View工具类
 */
public class ViewUtil {

    public static void setListViewHeightBasedOnChildren(RecyclerView list) {
        RecyclerView.Adapter listAdapter = list.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getItemCount(); i++) {
            View listItem = list.getChildAt(i);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = list.getLayoutParams();
        params.height = totalHeight;
        list.setLayoutParams(params);
    }

    /**
     * 获取Listview的高度，然后设置ViewPager的高度
     * @param list
     * @return
     */
    public static int setListViewHeightBasedOnChildren1(RecyclerView list) {
        //获取ListView对应的Adapter
        RecyclerView.Adapter listAdapter = list.getAdapter();
        if (listAdapter == null) {
            return 0;
        }

        int totalHeight = 0;
        for (int i = 0, len =  listAdapter.getItemCount(); i < len; i++) { //listAdapter.getCount()返回数据项的数目
            View listItem = list.getChildAt(i);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight(); //统计所有子项的总高度
        }

        ViewGroup.LayoutParams params = list.getLayoutParams();
        params.height = totalHeight;
        //listView.getDividerHeight()获取子项间分隔符占用的高度
        //params.height最后得到整个ListView完整显示需要的高度
        list.setLayoutParams(params);
        return params.height;
    }
}
