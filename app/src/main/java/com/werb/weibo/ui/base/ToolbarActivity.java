package com.werb.weibo.ui.base;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.werb.weibo.R;


/**
 * Created by Werb on 2016/7/13.
 * Email：1025004680@qq.com
 * 一个带有Toolbar的Activity，作为部分Activity的基类
 */
public abstract class ToolbarActivity extends AppCompatActivity {
    //两个用于继承实现的方法
    abstract protected int provideContentViewId();//用于引入布局文件

    protected AppBarLayout mAppBar;
    protected Toolbar mToolbar;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(provideContentViewId());//布局
        mAppBar = (AppBarLayout) findViewById(R.id.app_bar_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null || mAppBar == null) {
            throw new IllegalStateException(
                    "Please setting a Toolbar ");
        }
        setSupportActionBar(mToolbar); //把Toolbar当做ActionBar给设置

        if (canBack()) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);//设置ActionBar一个返回箭头，主界面没有，次级界面有
        }
        if (Build.VERSION.SDK_INT >= 21) {
            mAppBar.setElevation(10.6f);//Z轴浮动
        }
    }

    /**
     * 判断当前 Activity 是否允许返回
     * 主界面不允许返回，次级界面允许返回
     * @return false
     */
    public boolean canBack() {
        return false;
    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        // 此时android.R.id.home即为返回箭头
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}
