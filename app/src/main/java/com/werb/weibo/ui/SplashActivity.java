package com.werb.weibo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.werb.weibo.R;
import com.werb.weibo.util.AccessTokenKeeper;

/**
 * Created by Werb on 2016/7/13.
 * Email：1025004680@qq.com
 * 闪屏界面
 */
public class SplashActivity extends AppCompatActivity {

    private Oauth2AccessToken mAccessToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        LoginOrMain();
    }

    private void LoginOrMain(){
        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        if(mAccessToken.isSessionValid()){
            startActivity(new Intent(SplashActivity.this,MainActivity.class));
        }else {
            startActivity(new Intent(SplashActivity.this,LoginActivity.class));
        }
    }
}
