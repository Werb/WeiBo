package com.werb.weibo.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.openapi.models.User;
import com.squareup.picasso.Picasso;
import com.werb.weibo.R;
import com.werb.weibo.info.Constants;
import com.werb.weibo.ui.adapter.RecyclerListAdapter;
import com.werb.weibo.util.AccessTokenKeeper;
import com.werb.weibo.util.PrefUtils;
import com.werb.weibo.util.RxBus;
import com.werb.weibo.util.RxEvents;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by Werb on 2016/7/11.
 * Email：1025004680@qq.com
 */
public class UserFragment extends BaseFragment {

    private static final String U_ID = "u_id";
    private static final String IS_ADMIN = "is_admin";

    /**
     * 当前 Token 信息
     */
    private Oauth2AccessToken mAccessToken;
    /**
     * 用于获取微博信息流等操作的API
     */
    private UsersAPI mUserApi;
    private StatusesAPI mStatusesAPI;

    @Bind(R.id.swipe_refresh)
    public SwipeRefreshLayout swipe_refresh;
    @Bind(R.id.recycler_list)
    RecyclerView recycler_list;
    @Bind(R.id.title)
    TextView userName;
    @Bind(R.id.tv_user_desc)
    TextView tv_user_desc;
    @Bind(R.id.tv_user_weibo_count)
    TextView tv_user_weibo_count;
    @Bind(R.id.tv_user_friends_count)
    TextView tv_user_friends_count;
    @Bind(R.id.tv_user_flowers_count)
    TextView tv_user_flowers_count;
    @Bind(R.id.iv_user_icon)
    CircleImageView iv_user_icon;

    private List<Status> statusList;
    private RecyclerListAdapter weiboUserAdapter;
    private String uId;
    private boolean isAdmin;
    private long max_id = 0L;
    private LinearLayoutManager mLayoutManager;
    private int lastVisibleItem;
    private boolean upTwice = false; // 是否下滑两次
    private boolean isLoadMore = false; // 是否加载过更多

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uId = getArguments().getString(U_ID);
        isAdmin = getArguments().getBoolean(IS_ADMIN);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user, container, false);
        ButterKnife.bind(this, rootView);
        initView(rootView);
        return rootView;
    }

    public static UserFragment newInstance(String uId, boolean isAdmin) {
        //通过Bundle保存数据
        Bundle args = new Bundle();
        args.putString(UserFragment.U_ID, uId);
        args.putBoolean(UserFragment.IS_ADMIN, isAdmin);
        UserFragment fragment = new UserFragment();
        //将Bundle设置为fragment的参数
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 初始化布局
     *
     * @param rootView 根布局
     */
    private void initView(View rootView) {

        if (swipe_refresh != null) {
            swipe_refresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
            swipe_refresh.setColorSchemeResources(R.color.colorPrimary,
                    R.color.colorPrimaryDark, R.color.colorAccent);
            swipe_refresh.setProgressViewOffset(true, 0, (int) TypedValue
                    .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getContext()
                            .getResources().getDisplayMetrics()));
        }
        mLayoutManager = new LinearLayoutManager(getContext());
        recycler_list.setLayoutManager(mLayoutManager);

        String userInfo;
        if (isAdmin) {
            userInfo = PrefUtils.getString(getContext(), "userInfo", " 用户信息");
            User user = User.parse(userInfo);
            System.out.println("tttt-u" + user.toString());
            userName.setText(user.screen_name);
            tv_user_desc.setText(user.description);
            tv_user_weibo_count.setText("微博 " + user.statuses_count);
            tv_user_friends_count.setText("关注 " + user.friends_count);
            tv_user_flowers_count.setText("粉丝 " + user.followers_count);
            Picasso.with(getContext()).load(user.avatar_hd).into(iv_user_icon);
        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!isAdmin) {
            getDataFromSina();
        }
        addListener();
        RxBus.getInstance().toObserverable().subscribe(event -> {
            if (event instanceof RxEvents.RadionBtnClick) {
                RxEvents.RadionBtnClick radionBtnClick = (RxEvents.RadionBtnClick) event;
                if (radionBtnClick.getId() == R.id.rb_user) {
                    getDataFromSina();
                }
            }
        });

    }

    /**
     * 添加监听事件
     */
    private void addListener() {
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDataFromSina();
            }
        });
        recycler_list.addOnScrollListener(new RecyclerView.OnScrollListener() {

                                              @Override
                                              public void onScrollStateChanged(RecyclerView recyclerView,
                                                                               int newState) {
                                                  super.onScrollStateChanged(recyclerView, newState);
                                                  if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                                      lastVisibleItem = mLayoutManager
                                                              .findLastVisibleItemPosition();
                                                      if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                                          if (lastVisibleItem + 1 == mLayoutManager
                                                                  .getItemCount()) {
                                                              weiboUserAdapter.updateLoadStatus(RecyclerListAdapter.LOAD_PULL_TO);
                                                              if (!isLoadMore) {
                                                                  if (!upTwice) {
                                                                      weiboUserAdapter.updateLoadStatus(RecyclerListAdapter.LOAD_MORE);
                                                                      new Handler().postDelayed(new Runnable() {
                                                                          @Override
                                                                          public void run() {
                                                                              getMoreDataFromSina();
                                                                              isLoadMore = false;
                                                                              upTwice = false;
                                                                          }
                                                                      }, 2500);
                                                                  }
                                                              }
                                                          }

                                                      }
                                                  }
                                              }
                                              @Override
                                              public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                                  super.onScrolled(recyclerView, dx, dy);
                                                  lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                                              }

                                          }

        );
    }

    private void getDataFromSina() {
        swipe_refresh.setRefreshing(true);
        // 获取当前已保存过的 Token
        mAccessToken = AccessTokenKeeper.readAccessToken(getContext());
        // 获取用户信息接口
        mUserApi = new UsersAPI(getContext(), Constants.APP_KEY, mAccessToken);
        mStatusesAPI = new StatusesAPI(getContext(), Constants.APP_KEY, mAccessToken);
        long uid = Long.parseLong(uId);
        mUserApi.show(uid, mListener);
        mStatusesAPI.userTimeline(uid, 0L, 0L, 50, 1, false, 0, false, mListener);
    }

    /**
     * 获取更多
     */
    private void getMoreDataFromSina() {
        max_id = Long.valueOf(PrefUtils.getString(getContext(), "max_id", "0"));
        // 获取当前已保存过的 Token
        mAccessToken = AccessTokenKeeper.readAccessToken(getContext());
        long uid = Long.parseLong(uId);
        mStatusesAPI.userTimeline(uid, 0L, max_id, 50, 1, false, 0, false, mListenerSlide);
    }

    /**
     * 微博 OpenAPI 回调接口。
     */
    private RequestListener mListener = new RequestListener() {

        //只能获取授权人的微博信息，点击未授权账户将会返回空，从而没有数据报错

        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                if (response.startsWith("{\"id\"")) {
                    System.out.println("---user-response--" + response);
                    swipe_refresh.setRefreshing(false);
                    User user = User.parse(response);
                    userName.setText(user.screen_name);
                    tv_user_desc.setText(user.description);
                    tv_user_weibo_count.setText("微博 " + user.statuses_count);
                    tv_user_friends_count.setText("关注 " + user.friends_count);
                    tv_user_flowers_count.setText("粉丝 " + user.followers_count);

                    Picasso.with(getContext()).load(user.avatar_hd).into(iv_user_icon);
                } else if (response.startsWith("{\"statuses\"")) {
                    StatusList statuses = StatusList.parse(response, "statuses");
                    if (statuses != null && !statuses.equals("")) {
                        System.out.println("---statuses-response--" + response);
                        statusList = statuses.statusList;
                        weiboUserAdapter = new RecyclerListAdapter(getContext(), "HomeFg", statusList);
                        recycler_list.setAdapter(weiboUserAdapter);
                        weiboUserAdapter.notifyDataSetChanged();
                    }
                }

            }

            //让子弹飞一会
            swipe_refresh.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (swipe_refresh != null) {
                        swipe_refresh.setRefreshing(false);
                    }
                }
            }, 1500);
        }

        @Override
        public void onWeiboException(WeiboException e) {
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(getContext(), info.toString(), Toast.LENGTH_LONG).show();
        }
    };

    private RequestListener mListenerSlide = new RequestListener() {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                if (response.startsWith("{\"statuses\"")) {
                    // 调用 StatusList#parse 解析字符串成微博列表对象
                    StatusList statuses = StatusList.parse(response, "statuses");
                    if (statuses != null && statuses.total_number > 0) {

                        Toast.makeText(getContext(),
                                "上滑加载成功, 条数: " + statuses.statusList.size(),
                                Toast.LENGTH_LONG).show();
                        System.out.println("----获取上滑加载信息流---" + statuses.statusList.toString());
                        statusList.addAll(statuses.statusList.subList(1,statuses.statusList.size()-1));

                        weiboUserAdapter.notifyDataSetChanged();

                        //保存最后一个微博的id
                        PrefUtils.setString(getContext(), "max_id", statusList.get(statusList.size() - 1).id);
                    }

                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(getContext(), info.toString(), Toast.LENGTH_LONG).show();
        }
    };
}
