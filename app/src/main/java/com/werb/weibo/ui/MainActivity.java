package com.werb.weibo.ui;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.User;
import com.sina.weibo.sdk.utils.LogUtil;
import com.werb.weibo.R;
import com.werb.weibo.info.Constants;
import com.werb.weibo.ui.adapter.ViewPagerFgAdapter;
import com.werb.weibo.ui.fragment.BaseFragment;
import com.werb.weibo.ui.fragment.HomeFragment;
import com.werb.weibo.ui.fragment.MessageFragment;
import com.werb.weibo.ui.fragment.SearchFragment;
import com.werb.weibo.ui.fragment.UserFragment;
import com.werb.weibo.util.AccessTokenKeeper;
import com.werb.weibo.util.PrefUtils;
import com.werb.weibo.util.RxBus;
import com.werb.weibo.util.RxEvents;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 微博登陆后的主界面
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();

    @Bind(R.id.vp_pager_fg)
    ViewPager vp_pager_fg;
    @Bind(R.id.rg_home)
    RadioGroup rg_home;
    @Bind(R.id.rb_home)
    RadioButton rb_home;
    @Bind(R.id.rb_message)
    RadioButton rb_message;
    @Bind(R.id.rb_search)
    RadioButton rb_search;
    @Bind(R.id.rb_user)
    RadioButton rb_user;

    private List<BaseFragment> fragmentList;

    /** 当前 Token 信息 */
    private Oauth2AccessToken mAccessToken;
    /** 用户信息接口 */
    private UsersAPI mUsersAPI;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        rb_home.setOnClickListener(this);
        rb_message.setOnClickListener(this);
        rb_search.setOnClickListener(this);
        rb_user.setOnClickListener(this);

        // 获取当前已保存过的 Token
        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        // 获取用户信息接口
        mUsersAPI = new UsersAPI(this, Constants.APP_KEY, mAccessToken);
        long uid = Long.parseLong(mAccessToken.getUid());
        mUsersAPI.show(uid, mListener);
    }

    /**
     * 初始化界面
     */
    private void initView(){

        String userInfo = PrefUtils.getString(MainActivity.this, "userInfo", " 用户信息");
        User user = User.parse(userInfo);
        String id = user.id;

        fragmentList = new ArrayList<>();
        fragmentList.add(new HomeFragment());
        fragmentList.add(new MessageFragment());
        fragmentList.add(new SearchFragment());
        fragmentList.add(UserFragment.newInstance(id,true));

        vp_pager_fg.setAdapter(new ViewPagerFgAdapter(getSupportFragmentManager(), fragmentList));

        vp_pager_fg.setCurrentItem(0);
        rg_home.check(R.id.rb_home);
        rg_home.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_home:
                        vp_pager_fg.setCurrentItem(0,false);
                        break;
                    case R.id.rb_message:
                        vp_pager_fg.setCurrentItem(1,false);
                        break;
                    case R.id.rb_search:
                        vp_pager_fg.setCurrentItem(2,false);
                        break;
                    case R.id.rb_user:
                        vp_pager_fg.setCurrentItem(3,false);
                }
            }
        });

    }

    /**
     * 微博 OpenAPI 回调接口。
     */
    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                LogUtil.i(TAG, response);
                // 调用 User#parse 将JSON串解析成User对象
                User user = User.parse(response);
                if (user != null) {
                    Toast.makeText(MainActivity.this,
                            "获取User信息成功，用户昵称：" + user.screen_name,
                            Toast.LENGTH_LONG).show();
                    System.out.println("--MAin-user--"+user.toString());
                    PrefUtils.setString(MainActivity.this,"userInfo",response);

                    initView();
                } else {
                    Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(MainActivity.this, info.toString(), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onClick(View v) {
        RxBus.getInstance().send(new RxEvents.RadionBtnClick(v.getId()));
    }
}
