package com.werb.weibo.widget.ninegridlayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.werb.weibo.ui.PictureActivity;
import com.werb.weibo.util.ScreenUtil;

/**
 * Created by Werb on 2016/7/12.
 * Email：1025004680@qq.com
 * 单图显示 imageView
 */
public class OneImage extends ImageView implements View.OnClickListener {

    private String url;
    private boolean isAttachedToWindow;
    private boolean imageIsTouch;

    public OneImage(Context context) {
        super(context);
        setOnClickListener(this);
    }

    public OneImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    public OneImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(this);
    }

    @Override
    public void onAttachedToWindow() {
        isAttachedToWindow = true;
        setImageUrl(url);
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Glide.clear(this);
    }


    public void setImageUrl(String url) {

        int width = ScreenUtil.instance(getContext()).getScreenWidth();
        int height = width/2;

        if (!TextUtils.isEmpty(url)) {
            this.url = url;
            if (isAttachedToWindow) {
                Glide.with(getContext()).load(url).override((width/3)*2, height).placeholder(new ColorDrawable(Color.parseColor("#f5f5f5"))).into(this);
            }
        }
    }

    //打开PicActivity
    private void startPictureActivity(OneImage image) {
        Intent intent = PictureActivity.newIntent(getContext(), image.url);
        //异常处理
        getContext().startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        OneImage image = this;
        Toast.makeText(getContext(), this.url, Toast.LENGTH_SHORT).show();
        if (!imageIsTouch) {
            imageIsTouch = true;
            Picasso.with(getContext()).load(this.url).fetch(new Callback() {
                @Override
                public void onSuccess() {
                    imageIsTouch = false;
                    startPictureActivity(image);
                }

                @Override
                public void onError() {
                    imageIsTouch = false;
                }
            });
        }
    }
}
