package com.werb.weibo.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.werb.weibo.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Werb on 2016/7/12.
 * Email：1025004680@qq.com
 */
public class PictureActivity extends AppCompatActivity{

    public static final String IMAGE_URL = "image_url";
    PhotoViewAttacher mPhotoViewAttacher;
    @Bind(R.id.picture) ImageView picture;

    String mImageUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        ButterKnife.bind(this);
        parseIntent();
        Picasso.with(this).load(mImageUrl).into(picture);
        mPhotoViewAttacher = new PhotoViewAttacher(picture);
//        picture.setAdapter();
    }


    public static Intent newIntent(Context context, String url) {
        Intent intent = new Intent(context, PictureActivity.class);
        intent.putExtra(PictureActivity.IMAGE_URL, url);
        return intent;
    }

    /**
     * 得到Intent传递的数据
     */
    private void parseIntent() {
        mImageUrl = getIntent().getStringExtra(IMAGE_URL);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

//    class PicAdapter extends PagerAdapter {
//
//        @Override
//        public int getCount() {
//            return 0;
//        }
//
//        @Override
//        public boolean isViewFromObject(View view, Object object) {
//            return false;
//        }
//
//        @Override
//        public Object instantiateItem(ViewGroup container, int position) {
//
//            return super.instantiateItem(container, position);
//        }
//
//        @Override
//        public void destroyItem(ViewGroup container, int position, Object object) {
//            super.destroyItem(container, position, object);
//        }
//    }
}
