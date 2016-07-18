package com.werb.weibo.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.werb.weibo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Werb on 2016/7/18.
 * Email：1025004680@qq.com
 * Weibo 选择图片适配器
 */
public class WeiBoPhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<String> photos;
    public List<Bitmap> bitmaps = new ArrayList<>();

    public WeiBoPhotoAdapter(Context ctx,List<String> list) {
        context = ctx;
        photos = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(),R.layout.send_item_photopick,null);
        return new PhotoPickviewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PhotoPickviewHolder pickviewHolder = (PhotoPickviewHolder) holder;
        String photoUrl = photos.get(position);
        Uri uri = Uri.fromFile(new File(photoUrl));
        Glide.with(context)
                .load(uri)
                .centerCrop()
                .thumbnail(0.1f)
                .into(pickviewHolder.iv_photo);
        getBitmapFromUri(uri,false,position);
        System.out.println("--ps--"+bitmaps.size());
        pickviewHolder.iv_delete.setOnClickListener(v->{
            Glide.clear(pickviewHolder.iv_photo);
            getBitmapFromUri(null,true,position);
        });




    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    class PhotoPickviewHolder extends RecyclerView.ViewHolder{

        @Bind(R.id.iv_photo)
        ImageView iv_photo;
        @Bind(R.id.iv_delete)
        ImageView iv_delete;

        public PhotoPickviewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }


    }

    private void getBitmapFromUri(Uri uri,boolean isDelete,int position){
        if(!isDelete){
            try
            {
                // 读取uri所在的图片
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                bitmaps.add(bitmap);
            }
            catch (Exception e)
            {
                Log.e("[Android]", e.getMessage());
                Log.e("[Android]", "目录为：" + uri);
                e.printStackTrace();
            }
        }else {
            bitmaps.remove(position);
        }
        System.out.println("--ps--"+bitmaps.size());
    }
}
