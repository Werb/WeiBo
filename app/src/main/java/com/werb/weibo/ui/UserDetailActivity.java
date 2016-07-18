package com.werb.weibo.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.werb.weibo.R;
import com.werb.weibo.ui.fragment.UserFragment;

import butterknife.ButterKnife;

/**
 * Created by Werb on 2016/7/15.
 * Email：1025004680@qq.com
 * 点击home中微博用户的头像
 */
public class UserDetailActivity extends FragmentActivity {

    private static final String USER_ID = "user_id";
    private static final String USER_NAME = "user_name";

    private String user_id;
    private String user_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        ButterKnife.bind(this);
        parseIntent();

        UserFragment userFragment = UserFragment.newInstance(user_id,user_name,false);
        getSupportFragmentManager().beginTransaction().add(R.id.user_fg,userFragment).commit();
    }

    public static Intent newIntent(Context context, String id,String name) {
        Intent intent = new Intent(context, UserDetailActivity.class);
        intent.putExtra(UserDetailActivity.USER_ID, id);
        intent.putExtra(UserDetailActivity.USER_NAME, name);
        return intent;
    }

    /**
     * 得到Intent传递的数据
     */
    private void parseIntent() {
        user_id = getIntent().getStringExtra(USER_ID);
        user_name = getIntent().getStringExtra(USER_NAME);
    }
}
