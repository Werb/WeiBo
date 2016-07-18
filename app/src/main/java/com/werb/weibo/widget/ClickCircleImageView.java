package com.werb.weibo.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.bumptech.glide.Glide;
import com.sina.weibo.sdk.openapi.models.User;
import com.werb.weibo.ui.UserDetailActivity;
import com.werb.weibo.util.PrefUtils;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Werb on 2016/7/15.
 * Email：1025004680@qq.com
 */
public class ClickCircleImageView extends CircleImageView implements View.OnClickListener {

    private boolean isAttachedToWindow;
    private User user;
    private String uId;
    private String icon_url;

    public ClickCircleImageView(Context context) {
        super(context);
        setOnClickListener(this);
    }

    public ClickCircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    public ClickCircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnClickListener(this);
    }

    @Override
    public void onAttachedToWindow() {
        isAttachedToWindow = true;
        setImage(user);
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Glide.clear(this);
    }

    public void setImage(User user) {

        this.user = user;
        uId = user.id;
        icon_url = user.avatar_hd;

        if (!TextUtils.isEmpty(icon_url)) {
            if (isAttachedToWindow) {
                Glide.with(getContext()).load(icon_url).centerCrop().into(this);
            }
        }
    }

    @Override
    public void onClick(View v) {
        PrefUtils.setString(getContext(),"OtherUserInfo",user.toString());
        System.out.println("idddd"+uId);
        getContext().startActivity(UserDetailActivity.newIntent(getContext(),uId,null));
    }
}
