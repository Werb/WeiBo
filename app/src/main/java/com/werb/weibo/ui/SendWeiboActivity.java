package com.werb.weibo.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.werb.weibo.R;
import com.werb.weibo.info.Constants;
import com.werb.weibo.ui.adapter.WeiBoPhotoAdapter;
import com.werb.weibo.ui.base.ToolbarActivity;
import com.werb.weibo.util.AccessTokenKeeper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.iwf.photopicker.PhotoPicker;

/**
 * Created by Werb on 2016/7/16.
 * Email：1025004680@qq.com
 * 发微博界面
 */
public class SendWeiboActivity extends ToolbarActivity {

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;//

    /**
     * 当前 Token 信息
     */
    private Oauth2AccessToken mAccessToken;
    private StatusesAPI mStatusesAPI;

    @Bind(R.id.et_weibo)
    EditText et_weibo;
    @Bind(R.id.tv_weibo_number)
    TextView tv_weibo_number;
    @Bind(R.id.weibo_photo_grid)
    RecyclerView weibo_photo_grid;

    private String content;
    private List<String> photos = new ArrayList<>();
    private List<Bitmap> bitmaps = new ArrayList<>();
    private WeiBoPhotoAdapter photoAdapter;


    @Override
    protected int provideContentViewId() {
        return R.layout.activity_send_weibo;
    }

    @Override
    public boolean canBack() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        // 对statusAPI实例化
        mStatusesAPI = new StatusesAPI(this, Constants.APP_KEY, mAccessToken);
        initView();
    }

    @OnClick(R.id.weibo_photo)
    void pickphoto() {
        photos.clear();
        photoAdapter.bitmaps.clear();
        PermissionToVerify();
    }

    //初始化布局
    private void initView() {
        et_weibo.addTextChangedListener(mTextWatcher);
        weibo_photo_grid.setLayoutManager(new GridLayoutManager(this, 3));
        photoAdapter = new WeiBoPhotoAdapter(this, photos);
        weibo_photo_grid.setAdapter(photoAdapter);
    }

    //photo pick
    private void photoPick() {
        PhotoPicker.builder()
                .setPhotoCount(1)
                .setShowCamera(true)
                .setShowGif(true)
                .setPreviewEnabled(false)
                .start(this, PhotoPicker.REQUEST_CODE);
    }

    /**
     * 判断当前权限是否允许,弹出提示框来选择
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void PermissionToVerify() {
        // 需要验证的权限
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            // 弹窗询问 ，让用户自己判断
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        photoPick();

    }

    /**
     * 用户进行权限设置后的回调函数 , 来响应用户的操作，无论用户是否同意权限，Activity都会
     * 执行此回调方法，所以我们可以把具体操作写在这里
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //获取图片
                    photoPick();
                } else {
                    Toast.makeText(SendWeiboActivity.this, "权限没有开启", Toast.LENGTH_SHORT).show();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    /**
     * 微博 OpenAPI 回调接口。
     */
    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            Toast.makeText(SendWeiboActivity.this,"发布成功！",Toast.LENGTH_SHORT).show();
            finish();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(SendWeiboActivity.this, info.toString(), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_send:
                Toast.makeText(SendWeiboActivity.this, "click", Toast.LENGTH_SHORT).show();
                content = et_weibo.getText().toString();
                if (content != null && !content.equals("")) {
                    Toast.makeText(SendWeiboActivity.this, content, Toast.LENGTH_SHORT).show();
                    bitmaps = photoAdapter.bitmaps;
                    if(bitmaps!=null&&bitmaps.size()>0){
                        mStatusesAPI.upload(content,bitmaps.get(0),"0.0","0.0",mListener);
                    }else {
                        mStatusesAPI.update(content, "45.0", "120.0", mListener);
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PhotoPicker.REQUEST_CODE) {
            if (data != null) {
                ArrayList<String> listExtra =
                        data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                loadAdpater(listExtra);
                System.out.println("---list---" + listExtra);
            }
        }
    }

    //更新数据适配器
    private void loadAdpater(ArrayList<String> paths) {
        if (photos == null) {
            photos = new ArrayList<>();
        }
        photos.clear();
        photos.addAll(paths);
        photoAdapter.notifyDataSetChanged();
    }

    //输入字符监听
    TextWatcher mTextWatcher = new TextWatcher() {
        private CharSequence temp;
        private int editStart ;
        private int editEnd ;
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub
            temp = s;
            if(140-s.length()==0){
                tv_weibo_number.setText("超出字数限制！");
                tv_weibo_number.setTextColor(Color.RED);
            }else {
                tv_weibo_number.setText(String.valueOf(140-s.length()));
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            editStart = et_weibo.getSelectionStart();
            editEnd = et_weibo.getSelectionEnd();
            if (temp.length() > 140) {
                Toast.makeText(SendWeiboActivity.this,
                        "你输入的字数已经超过了限制！", Toast.LENGTH_SHORT)
                        .show();
                s.delete(editStart-1, editEnd);
                int tempSelection = editStart;
                et_weibo.setText(s);
                et_weibo.setSelection(tempSelection);
            }
        }
    };
}
