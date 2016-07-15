package com.werb.weibo.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.utils.LogUtil;
import com.werb.weibo.R;
import com.werb.weibo.info.Constants;
import com.werb.weibo.ui.adapter.ViewPagerFgAdapter;
import com.werb.weibo.ui.base.ToolbarActivity;
import com.werb.weibo.ui.fragment.BaseFragment;
import com.werb.weibo.ui.fragment.weibodetail.LikeFragment;
import com.werb.weibo.ui.fragment.weibodetail.WeiboDetailFragment;
import com.werb.weibo.util.AccessTokenKeeper;
import com.werb.weibo.util.DataUtil;
import com.werb.weibo.util.ScreenUtil;
import com.werb.weibo.util.StringUtil;
import com.werb.weibo.widget.ninegridlayout.NineGridlayout;
import com.werb.weibo.widget.ninegridlayout.OneImage;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Werb on 2016/7/13.
 * Email：1025004680@qq.com
 * 微博信息详情页
 */
public class WeiBoDetailActivity extends ToolbarActivity {

    public static final String WEIBO_STATUS = "weibo_status";
    private static final String TAG = WeiBoDetailActivity.class.getName();


    /** 当前 Token 信息 */
    private Oauth2AccessToken mAccessToken;
    /** 用于获取微博信息流等操作的API */
    private StatusesAPI mStatusesAPI;
    public Status mStatus;
    public String weibo_id;
    private List<BaseFragment> fragmentList;

    //原文
    @Bind(R.id.tv_user_name) TextView tv_user_name;
    @Bind(R.id.tv_user_create_at) TextView tv_user_create_at;
    @Bind(R.id.tv_weibo_text) TextView tv_weibo_text;
    @Bind(R.id.tv_weibo_source) TextView tv_weibo_source;
    @Bind(R.id.iv_user_icon) CircleImageView iv_user_icon;
    @Bind(R.id.iv_ngrid_layout) NineGridlayout iv_ngrid_layout;
    @Bind(R.id.iv_oneimage) OneImage iv_oneimage;
    @Bind(R.id.tv_weibo_attitudes_count) TextView tv_weibo_attitudes_count;
    @Bind(R.id.tv_weibo_reposts_count) TextView tv_weibo_reposts_count;
    @Bind(R.id.tv_weibo_comments_count) TextView tv_weibo_comments_count;
    //转发
    @Bind(R.id.zhuanfa_card) LinearLayout zhuanfa_card;
    @Bind(R.id.tv_zhuanfa_user_name) TextView tv_zhuanfa_user_name;
    @Bind(R.id.tv_weibo_zhuanfa_text) TextView tv_weibo_zhuanfa_text;
    @Bind(R.id.tv_weibo_zhuanfa_reposts_count) TextView tv_weibo_zhuanfa_reposts_count;
    @Bind(R.id.tv_weibo_zhuanfa_comments_count) TextView tv_weibo_zhuanfa_comments_count;
    @Bind(R.id.iv_zhuanfa_ngrid_layout) NineGridlayout iv_zhuanfa_ngrid_layout;
    @Bind(R.id.iv_zhuanfa_oneimage) OneImage iv_zhuanfa_oneimage;


    @Override
    protected int provideContentViewId() {
        return R.layout.activity_weibo_detail;
    }

    @Override
    public boolean canBack() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        parseIntent();

        initViewWithData(mStatus);
        initTabView();
        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        mStatusesAPI = new StatusesAPI(this, Constants.APP_KEY,mAccessToken);
        mStatusesAPI.show(Long.valueOf(weibo_id),mListener);
    }

    public static Intent newIntent(Context context,Status status) {
        Intent intent = new Intent(context, WeiBoDetailActivity.class);
        intent.putExtra(WeiBoDetailActivity.WEIBO_STATUS,status);
        return intent;
    }

    /**
     * 得到Intent传递的数据
     */
    private void parseIntent() {
        mStatus = (Status) getIntent().getSerializableExtra(WEIBO_STATUS);
        weibo_id = mStatus.id;
        System.out.println("---detail--id-"+weibo_id);
    }

    //初始化Tab滑动
    public void initTabView(){

        fragmentList = new ArrayList<>();
        fragmentList.add(new LikeFragment());
        fragmentList.add(WeiboDetailFragment.newInstance(weibo_id,"Reposts"));
        fragmentList.add(WeiboDetailFragment.newInstance(weibo_id,"Comments"));

        TabLayout tabLayout= (TabLayout) findViewById(R.id.tabLayout);
        ViewPager mViewPager= (ViewPager) findViewById(R.id.detail_viewPager);

        mViewPager.setAdapter(new ViewPagerFgAdapter(getSupportFragmentManager(),fragmentList));//给ViewPager设置适配器
        tabLayout.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtil.instance(this).getScreenHeight()/2);
        mViewPager.setLayoutParams(params);
    }

    /**
     * 根据数据适配
     */
    private void initViewWithData(Status status){
        //原文信息
        tv_user_name.setText(status.user.name);
        tv_user_create_at.setText(DataUtil.showTime(DataUtil.getDataFormat(status.created_at)));
        tv_weibo_source.setText(StringUtil.getWeiboSource(status.source));
        tv_weibo_attitudes_count.setText(status.attitudes_count+"");
        tv_weibo_reposts_count.setText(status.reposts_count+"");
        tv_weibo_comments_count.setText(status.comments_count+"");

        System.out.println("---icon--" + status.attitudes_count);

        //正文
        StringUtil.setWeiBoText(WeiBoDetailActivity.this,status.text,tv_weibo_text);

        //头像
        Glide.clear(iv_user_icon);
        Glide.with(WeiBoDetailActivity.this).load(status.user.avatar_hd).centerCrop().into(iv_user_icon);


        //转发信息
        if(status.retweeted_status==null){
            zhuanfa_card.setVisibility(View.GONE);
            if (status.pic_urls != null) {
                System.out.println("---pic" + status.pic_urls.toString());
                if (status.pic_urls.size() == 1) {
                    iv_ngrid_layout.setVisibility(View.GONE);
                    iv_oneimage.setVisibility(View.VISIBLE);
                    iv_oneimage.setImageUrl(status.pic_urls.get(0));
                } else {
                    iv_ngrid_layout.setVisibility(View.VISIBLE);
                    iv_oneimage.setVisibility(View.GONE);

                    iv_ngrid_layout.setImagesData(status.pic_urls);
                }
            } else {
                iv_ngrid_layout.setVisibility(View.GONE);
                iv_oneimage.setVisibility(View.GONE);
            }
        }else {
            zhuanfa_card.setVisibility(View.VISIBLE);
            iv_ngrid_layout.setVisibility(View.GONE);
            iv_oneimage.setVisibility(View.GONE);
            //转发信息用户名
            StringUtil.setWeiBoText(WeiBoDetailActivity.this,"@"+status.retweeted_status.user.screen_name,tv_zhuanfa_user_name);

            tv_weibo_zhuanfa_comments_count.setText(String.valueOf(status.retweeted_status.comments_count));
            tv_weibo_zhuanfa_reposts_count.setText(String.valueOf(status.retweeted_status.reposts_count));

            //转发信息正文
            StringUtil.setWeiBoText(WeiBoDetailActivity.this,status.retweeted_status.text,tv_weibo_zhuanfa_text);

            if (status.retweeted_status.pic_urls != null) {
                if (status.retweeted_status.pic_urls.size() == 1) {
                    iv_zhuanfa_ngrid_layout.setVisibility(View.GONE);
                    iv_zhuanfa_oneimage.setVisibility(View.VISIBLE);
                    iv_zhuanfa_oneimage.setImageUrl(status.retweeted_status.pic_urls.get(0));
                } else {
                    iv_zhuanfa_ngrid_layout.setVisibility(View.VISIBLE);
                    iv_zhuanfa_oneimage.setVisibility(View.GONE);

                    iv_zhuanfa_ngrid_layout.setImagesData(status.retweeted_status.pic_urls);
                }
            } else {
                iv_zhuanfa_ngrid_layout.setVisibility(View.GONE);
                iv_zhuanfa_oneimage.setVisibility(View.GONE);
            }
        }
    }



    /**
     * 微博 OpenAPI 回调接口。
     */
    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                LogUtil.i(TAG, response);
                //将JSON串解析成User对象
                Status status = Status.parse(response);
                System.out.println("---WeiboDetail---"+status.toString());

                //初始化
                initViewWithData(status);
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(WeiBoDetailActivity.this, info.toString(), Toast.LENGTH_LONG).show();
        }
    };

}
