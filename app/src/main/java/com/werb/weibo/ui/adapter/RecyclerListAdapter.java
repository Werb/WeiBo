package com.werb.weibo.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sina.weibo.sdk.openapi.models.Status;
import com.werb.weibo.R;
import com.werb.weibo.ui.WeiBoDetailActivity;
import com.werb.weibo.util.DataUtil;
import com.werb.weibo.util.ScreenUtil;
import com.werb.weibo.util.StringUtil;
import com.werb.weibo.widget.ClickCircleImageView;
import com.werb.weibo.widget.ninegridlayout.NineGridlayout;
import com.werb.weibo.widget.ninegridlayout.OneImage;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Werb on 2016/7/11.
 * Email：1025004680@qq.com
 * 数据适配器
 */
public class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int LOAD_MORE = 0;
    public static final int LOAD_PULL_TO = 1;
    public static final int LOAD_NONE = 2;
    public static final int LOAD_END = 3;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = -1;
    private int status = 1;
    private String tag;
    private List list;
    private Context context;

    public RecyclerListAdapter(Context ctx, String tag, List list) {
        context = ctx;
        this.tag = tag;
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        // 最后一个item设置为footerView
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return position;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View view = View.inflate(parent.getContext(), R.layout.base_item_footer, null);
            view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            return new FooterViewHolder(view);
        } else {
            if (tag.equals("HomeFg")) {
                View rootView = View.inflate(parent.getContext(), R.layout.home_item_weibo, null);
                return new WeiboItemViewHolder(rootView);
            } else if (tag.equals("Fg_detail")) {
                View rootView = View.inflate(parent.getContext(), R.layout.fg_reposts_item, null);
                return new WeiboRepostsViewHolder(rootView);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterViewHolder) {
            final FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            switch (status) {
                case LOAD_MORE:
                    footerViewHolder.progress.setVisibility(View.VISIBLE);
                    footerViewHolder.tv_load_prompt.setText("正在加载...");
                    footerViewHolder.itemView.setVisibility(View.VISIBLE);
                    break;
                case LOAD_PULL_TO:
                    footerViewHolder.progress.setVisibility(View.GONE);
                    footerViewHolder.tv_load_prompt.setText("上拉加载更多");
                    footerViewHolder.itemView.setVisibility(View.VISIBLE);
                    break;
                case LOAD_NONE:
                    System.out.println("LOAD_NONE----");
                    footerViewHolder.progress.setVisibility(View.GONE);
                    footerViewHolder.tv_load_prompt.setText("已无更多加载");
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            footerViewHolder.itemView.setVisibility(View.GONE);
                        }
                    }, 2000);
                    break;
                case LOAD_END:
                    footerViewHolder.itemView.setVisibility(View.GONE);
                default:
                    break;
            }
        } else {
            if (tag.equals("HomeFg")) {
                WeiboItemViewHolder itemViewHolder = (WeiboItemViewHolder) holder;
                Status status = (Status) list.get(position);

                //原文信息
                itemViewHolder.tv_user_name.setText(status.user.name);
                itemViewHolder.tv_user_create_at.setText(DataUtil.showTime(DataUtil.getDataFormat(status.created_at)));
                itemViewHolder.tv_weibo_source.setText(StringUtil.getWeiboSource(status.source));
                itemViewHolder.tv_weibo_attitudes_count.setText(String.valueOf(status.attitudes_count));
                itemViewHolder.tv_weibo_comments_count.setText(String.valueOf(status.comments_count));
                itemViewHolder.tv_weibo_reposts_count.setText(String.valueOf(status.reposts_count));
                System.out.println("---icon--" + status.user.avatar_hd);

                //正文
                StringUtil.setWeiBoText(context, status.text, itemViewHolder.tv_weibo_text);

                //头像
                Glide.clear(itemViewHolder.iv_user_icon);
                itemViewHolder.iv_user_icon.setImage(status.user);


                //转发信息
                if (status.retweeted_status == null) {
                    itemViewHolder.zhuanfa_card.setVisibility(View.GONE);
                    if (status.pic_urls != null) {
                        System.out.println("---pic" + status.pic_urls.toString());
                        if (status.pic_urls.size() == 1) {
                            itemViewHolder.iv_ngrid_layout.setVisibility(View.GONE);
                            itemViewHolder.iv_oneimage.setVisibility(View.VISIBLE);
                            itemViewHolder.iv_oneimage.setImageUrl(status.pic_urls.get(0));
                        } else {
                            itemViewHolder.iv_ngrid_layout.setVisibility(View.VISIBLE);
                            itemViewHolder.iv_oneimage.setVisibility(View.GONE);

                            itemViewHolder.iv_ngrid_layout.setImagesData(status.pic_urls);
                        }
                    } else {
                        itemViewHolder.iv_ngrid_layout.setVisibility(View.GONE);
                        itemViewHolder.iv_oneimage.setVisibility(View.GONE);
                    }
                } else {
                    itemViewHolder.zhuanfa_card.setVisibility(View.VISIBLE);
                    itemViewHolder.iv_ngrid_layout.setVisibility(View.GONE);
                    itemViewHolder.iv_oneimage.setVisibility(View.GONE);
                    //转发信息用户名
                    if(status.retweeted_status.user!=null&&!status.retweeted_status.user.equals("")){
                        StringUtil.setWeiBoText(context,"@"+status.retweeted_status.user.screen_name,itemViewHolder.tv_zhuanfa_user_name);
                    }
                    itemViewHolder.tv_weibo_zhuanfa_comments_count.setText("转发 " + String.valueOf(status.retweeted_status.comments_count));
                    itemViewHolder.tv_weibo_zhuanfa_reposts_count.setText("评论 " + String.valueOf(status.retweeted_status.reposts_count));

                    //转发信息正文
                    StringUtil.setWeiBoText(context, status.retweeted_status.text, itemViewHolder.tv_weibo_zhuanfa_text);

                    if (status.retweeted_status.pic_urls != null) {
                        if (status.retweeted_status.pic_urls.size() == 1) {
                            itemViewHolder.iv_zhuanfa_ngrid_layout.setVisibility(View.GONE);
                            itemViewHolder.iv_zhuanfa_oneimage.setVisibility(View.VISIBLE);
                            itemViewHolder.iv_zhuanfa_oneimage.setImageUrl(status.retweeted_status.pic_urls.get(0));
                        } else {
                            itemViewHolder.iv_zhuanfa_ngrid_layout.setVisibility(View.VISIBLE);
                            itemViewHolder.iv_zhuanfa_oneimage.setVisibility(View.GONE);

                            itemViewHolder.iv_zhuanfa_ngrid_layout.setImagesData(status.retweeted_status.pic_urls);
                        }
                    } else {
                        itemViewHolder.iv_zhuanfa_ngrid_layout.setVisibility(View.GONE);
                        itemViewHolder.iv_zhuanfa_oneimage.setVisibility(View.GONE);
                    }
                }

                //微博原文信息点击事件
                itemViewHolder.weibo_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = WeiBoDetailActivity.newIntent(context, status);
                        context.startActivity(intent);
                    }
                });
                //微博转发信息点击事件
                itemViewHolder.zhuanfa_card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = WeiBoDetailActivity.newIntent(context, status.retweeted_status);
                        context.startActivity(intent);
                    }
                });
            } else if (tag.equals("Fg_detail")) {
                WeiboRepostsViewHolder itemViewHolder = (WeiboRepostsViewHolder) holder;
                Status status = (Status) list.get(position);

                //原文信息
                itemViewHolder.tv_user_name.setText(status.user.name);
                itemViewHolder.tv_user_create_at.setText(DataUtil.showTime(DataUtil.getDataFormat(status.created_at)));

                //正文
                StringUtil.setWeiBoText(context, status.text, itemViewHolder.tv_weibo_text);

                //头像
                Glide.clear(itemViewHolder.iv_user_icon);
                Glide.with(context).load(status.user.avatar_hd).centerCrop().into(itemViewHolder.iv_user_icon);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size() + 1;
    }


    /**
     * 脚布局 实现上拉加载
     */
    class FooterViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_load_prompt;
        private ProgressBar progress;

        public FooterViewHolder(View itemView) {
            super(itemView);
            tv_load_prompt = (TextView) itemView
                    .findViewById(R.id.tv_load_prompt);
            progress = (ProgressBar) itemView.findViewById(R.id.progress);

            LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtil.instance(context).dip2px(40));
            itemView.setLayoutParams(params);
        }
    }

    /**
     * weibo信息
     */
    class WeiboItemViewHolder extends RecyclerView.ViewHolder {

        //原文
        @Bind(R.id.tv_user_name)
        TextView tv_user_name;
        @Bind(R.id.tv_user_create_at)
        TextView tv_user_create_at;
        @Bind(R.id.tv_weibo_text)
        TextView tv_weibo_text;
        @Bind(R.id.tv_weibo_source)
        TextView tv_weibo_source;
        @Bind(R.id.tv_weibo_attitudes_count)
        TextView tv_weibo_attitudes_count;
        @Bind(R.id.tv_weibo_reposts_count)
        TextView tv_weibo_reposts_count;
        @Bind(R.id.tv_weibo_comments_count)
        TextView tv_weibo_comments_count;
        @Bind(R.id.iv_user_icon)
        ClickCircleImageView iv_user_icon;
        @Bind(R.id.iv_ngrid_layout)
        NineGridlayout iv_ngrid_layout;
        @Bind(R.id.iv_oneimage)
        OneImage iv_oneimage;
        @Bind(R.id.weibo_layout)
        LinearLayout weibo_layout;
        //转发
        @Bind(R.id.zhuanfa_card)
        LinearLayout zhuanfa_card;
        @Bind(R.id.tv_zhuanfa_user_name)
        TextView tv_zhuanfa_user_name;
        @Bind(R.id.tv_weibo_zhuanfa_text)
        TextView tv_weibo_zhuanfa_text;
        @Bind(R.id.tv_weibo_zhuanfa_reposts_count)
        TextView tv_weibo_zhuanfa_reposts_count;
        @Bind(R.id.tv_weibo_zhuanfa_comments_count)
        TextView tv_weibo_zhuanfa_comments_count;
        @Bind(R.id.iv_zhuanfa_ngrid_layout)
        NineGridlayout iv_zhuanfa_ngrid_layout;
        @Bind(R.id.iv_zhuanfa_oneimage)
        OneImage iv_zhuanfa_oneimage;

        public WeiboItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            ScreenUtil screenUtil = ScreenUtil.instance(context);
            int w = screenUtil.getScreenWidth() - screenUtil.dip2px(70);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(w, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(screenUtil.dip2px(10), 0, screenUtil.dip2px(10), 0);
            weibo_layout.setLayoutParams(params);

            LinearLayout.LayoutParams params_card = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params_card.setMargins(0, screenUtil.dip2px(5), screenUtil.dip2px(5), 0);
            zhuanfa_card.setLayoutParams(params_card);

            LinearLayout.LayoutParams paramsIv = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsIv.gravity = Gravity.LEFT;
            paramsIv.setMargins(0, ScreenUtil.instance(context).dip2px(8), 0, 0);
            iv_oneimage.setLayoutParams(paramsIv);

        }
    }

    /**
     * 微博的转发列表
     */
    class WeiboRepostsViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.iv_user_icon)
        CircleImageView iv_user_icon;
        @Bind(R.id.tv_user_name)
        TextView tv_user_name;
        @Bind(R.id.tv_user_create_at)
        TextView tv_user_create_at;
        @Bind(R.id.tv_weibo_text)
        TextView tv_weibo_text;

        public WeiboRepostsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(params);
        }
    }


    //方法
    public void updateLoadStatus(int status) {
        this.status = status;
        notifyDataSetChanged();
    }
}
