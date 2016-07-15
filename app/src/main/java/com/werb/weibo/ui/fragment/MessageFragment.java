package com.werb.weibo.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.werb.weibo.R;

/**
 * Created by Werb on 2016/7/11.
 * Email：1025004680@qq.com
 */
public class MessageFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = View.inflate(inflater.getContext(), R.layout.fragment_home,null);
        return rootView;
    }

}
