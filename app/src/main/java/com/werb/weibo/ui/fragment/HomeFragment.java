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
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.openapi.models.User;
import com.werb.weibo.R;
import com.werb.weibo.info.Constants;
import com.werb.weibo.ui.adapter.RecyclerListAdapter;
import com.werb.weibo.util.AccessTokenKeeper;
import com.werb.weibo.util.PrefUtils;
import com.werb.weibo.util.RxBus;
import com.werb.weibo.util.RxEvents;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Werb on 2016/7/11.
 * Email：1025004680@qq.com
 * 获取主页微博信息
 */
public class HomeFragment extends BaseFragment {

    /**
     * 当前 Token 信息
     */
    private Oauth2AccessToken mAccessToken;
    /**
     * 用于获取微博信息流等操作的API
     */
    private StatusesAPI mStatusesAPI;

    @Bind(R.id.swipe_refresh)
    public SwipeRefreshLayout swipe_refresh;
    @Bind(R.id.recycler_list)
    RecyclerView recycler_list;
    @Bind(R.id.title)
    TextView userName;

    private List<Status> statusList;
    private RecyclerListAdapter weiboHomeAdapter;
    private LinearLayoutManager mLayoutManager;
    private int lastVisibleItem;
    private boolean upTwice = false; // 是否下滑两次
    private boolean isLoadMore = false; // 是否加载过更多

    private long max_id = 0L;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = View.inflate(inflater.getContext(), R.layout.fragment_home, null);
        ButterKnife.bind(this, rootView);
        initView(rootView);
        return rootView;
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
        statusList = new ArrayList<>();
        mLayoutManager = new LinearLayoutManager(getContext());
        recycler_list.setLayoutManager(mLayoutManager);


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String userInfo = PrefUtils.getString(getContext(), "userInfo", " 用户信息");
        User user = User.parse(userInfo);
        userName.setText(user.screen_name);

        getDataFromSina();
        addListener();
        RxBus.getInstance().toObserverable().subscribe(event -> {
            if (event instanceof RxEvents.RadionBtnClick) {
                RxEvents.RadionBtnClick radionBtnClick = (RxEvents.RadionBtnClick) event;
                if (radionBtnClick.getId() == R.id.rb_home) {
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
                                                              weiboHomeAdapter.updateLoadStatus(RecyclerListAdapter.LOAD_PULL_TO);
                                                              if (!isLoadMore) {
                                                                  if (!upTwice) {
                                                                      weiboHomeAdapter.updateLoadStatus(RecyclerListAdapter.LOAD_MORE);
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

    /**
     * 获取微博信息
     */

    private void getDataFromSina() {
        swipe_refresh.setRefreshing(true);
        // 获取当前已保存过的 Token
        mAccessToken = AccessTokenKeeper.readAccessToken(getContext());
        // 对statusAPI实例化
        mStatusesAPI = new StatusesAPI(getActivity(), Constants.APP_KEY, mAccessToken);
        mStatusesAPI.friendsTimeline(0L, 0L, 10, 1, false, 0, false, mListener);
    }

    /**
     * 获取更多
     */
    private void getMoreDataFromSina() {
        max_id = Long.valueOf(PrefUtils.getString(getContext(), "max_id", "0"));
        // 获取当前已保存过的 Token
        mAccessToken = AccessTokenKeeper.readAccessToken(getContext());
        // 对statusAPI实例化
        mStatusesAPI = new StatusesAPI(getActivity(), Constants.APP_KEY, mAccessToken);
        mStatusesAPI.friendsTimeline(0L, max_id, 10, 1, false, 0, false, mListenerSlide);

    }

    /**
     * 微博 OpenAPI 回调接口。
     */
    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                if (response.startsWith("{\"statuses\"")) {
                    // 调用 StatusList#parse 解析字符串成微博列表对象
                    StatusList statuses = StatusList.parse(response, "statuses");
                    if (statuses != null && statuses.total_number > 0) {
                        Toast.makeText(getContext(),
                                "获取微博信息流成功, 条数: " + statuses.statusList.size(),
                                Toast.LENGTH_LONG).show();
                        System.out.println("----获取微博信息流---" + statuses.statusList.toString());
                        statusList = statuses.statusList;
                        weiboHomeAdapter = new RecyclerListAdapter(getContext(), "HomeFg", statusList);
                        recycler_list.setAdapter(weiboHomeAdapter);
                        weiboHomeAdapter.notifyDataSetChanged();

                        //保存最后一个微博的id
                        PrefUtils.setString(getContext(), "max_id", statusList.get(statusList.size() - 1).id);
                    }

                    //让子弹飞一会
                    swipe_refresh.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (swipe_refresh != null) {
                                swipe_refresh.setRefreshing(false);
                            }
                        }
                    }, 2000);
                }
            }
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

                        weiboHomeAdapter.notifyDataSetChanged();

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
