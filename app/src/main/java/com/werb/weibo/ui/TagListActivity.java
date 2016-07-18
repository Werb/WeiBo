package com.werb.weibo.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.widget.Toast;

import com.werb.weibo.R;
import com.werb.weibo.ui.base.ToolbarActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Werb on 2016/7/15.
 * Email：1025004680@qq.com
 * 话题搜索
 * 由于话题搜索属于高级接口，需要申请才能获取，所以现在只能这样，但跳转参数已经写好，只需调用API请求即可
 */
public class TagListActivity extends ToolbarActivity {

    private static final String WEIBO_TAG = "weibo_tag";
    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout swipe_refresh;
    @Bind(R.id.recycler_list)
    RecyclerView recycler_list;

    private LinearLayoutManager mLayoutManager;
    private String weibo_tag;

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_tag;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        parseIntent();
        initView();

        Toast.makeText(this,"话题："+weibo_tag,Toast.LENGTH_SHORT).show();
    }

    public static Intent newIntent(Context context, String tag) {
        Intent intent = new Intent(context, TagListActivity.class);
        intent.putExtra(TagListActivity.WEIBO_TAG, tag);
        return intent;
    }

    /**
     * 得到Intent传递的数据
     */
    private void parseIntent() {
        weibo_tag = getIntent().getStringExtra(WEIBO_TAG);
    }

    /**
     * 初始化布局
     */
    private void initView() {

        if (swipe_refresh != null) {
            swipe_refresh.setColorSchemeResources(R.color.colorPrimary,
                    R.color.colorPrimaryDark, R.color.colorAccent);
            swipe_refresh.setProgressViewOffset(true, 0, (int) TypedValue
                    .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24,getResources().getDisplayMetrics()));
        }
        mLayoutManager = new LinearLayoutManager(this);
        recycler_list.setLayoutManager(mLayoutManager);

    }
}
