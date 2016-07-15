package com.werb.weibo.ui.fragment.weibodetail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.werb.weibo.R;
import com.werb.weibo.info.Constants;
import com.werb.weibo.ui.adapter.RecyclerListAdapter;
import com.werb.weibo.ui.fragment.BaseFragment;
import com.werb.weibo.util.AccessTokenKeeper;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Werb on 2016/7/13.
 * Email：1025004680@qq.com
 * 转发界面的fragment
 */
public class WeiboDetailFragment extends BaseFragment {

    private static final String WERBO_ID = "weibo_id";
    private static final String FG_TAg = "fg_tag";

    /**
     * 当前 Token 信息
     */
    private Oauth2AccessToken mAccessToken;
    /**
     * 用于获取微博信息流等操作的API
     */

    private String tag;

    private StatusesAPI mStatusesAPI;
    private CommentsAPI mCommentsAPI;
    private long weibo_id;
    private List<Status> statusList;
    private RecyclerListAdapter weiboRepostsAdapter;

    @Bind(R.id.fg_reposts_list)
    RecyclerView fg_reposts_list;



    public static WeiboDetailFragment newInstance(String pid, String tag) {
        //通过Bundle保存数据
        Bundle args = new Bundle();
        args.putString(WeiboDetailFragment.WERBO_ID, pid);
        args.putString(WeiboDetailFragment.FG_TAg, tag);
        WeiboDetailFragment fragment = new WeiboDetailFragment();
        //将Bundle设置为fragment的参数
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weibo_id = Long.valueOf(getArguments().getString(WERBO_ID));
        tag = getArguments().getString(FG_TAg);
        System.out.println("WERBO_ID=" + weibo_id);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = View.inflate(inflater.getContext(), R.layout.fragment_reposts, null);
        ButterKnife.bind(this, rootView);
        initView();
        return rootView;
    }

    private void initView() {
        fg_reposts_list.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
    }

    @Override
    public void initData() {
        getDataFromSina();
    }

    /**
     * 获取微博信息
     */
    private void getDataFromSina() {
        // 获取当前已保存过的 Token
        mAccessToken = AccessTokenKeeper.readAccessToken(getContext());
        // 对statusAPI实例化
        if(tag.equals("Reposts")){
            mStatusesAPI = new StatusesAPI(getActivity(), Constants.APP_KEY, mAccessToken);
            mStatusesAPI.repostTimeline(weibo_id, 0L, 0L, 50, 1, 0, mListener);
        }else if(tag.equals("Comments")){
            mCommentsAPI = new CommentsAPI(getActivity(),Constants.APP_KEY,mAccessToken);
            mCommentsAPI.show(weibo_id,0L,0L,50,1,0,mListener);
        }

    }

    /**
     * 微博 OpenAPI 回调接口。
     */
    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                if (response.startsWith("{\"reposts\"")) {
                    // 调用 StatusList#parse 解析字符串成微博列表对象
                    StatusList statuses = StatusList.parse(response,"reposts");
                    if (statuses != null && statuses.total_number > 0) {
                        if(statuses.statusList!=null){
                            Toast.makeText(getContext(),
                                    "获取微博转发列表成功, 条数: " + statuses.statusList.size(),
                                    Toast.LENGTH_LONG).show();
                            System.out.println("----获取微博转发列表---"+statuses.statusList.toString());
                            statusList = statuses.statusList;
                            weiboRepostsAdapter = new RecyclerListAdapter(getContext(),"Fg_detail",statusList);
                            fg_reposts_list.setAdapter(weiboRepostsAdapter);
                            weiboRepostsAdapter.notifyDataSetChanged();

                        }
                    }
                }else if(response.startsWith("{\"comments\"")){
                    // 调用 StatusList#parse 解析字符串成微博列表对象
                    StatusList statuses = StatusList.parse(response,"comments");
                    if (statuses != null && statuses.total_number > 0) {
                        if(statuses.statusList!=null){
                            Toast.makeText(getContext(),
                                    "获取微博评论列表成功, 条数: " + statuses.statusList.size(),
                                    Toast.LENGTH_LONG).show();
                            System.out.println("----获取微博评论列表---"+statuses.statusList.toString());
                            statusList = statuses.statusList;
                            weiboRepostsAdapter = new RecyclerListAdapter(getContext(),"Fg_detail",statusList);
                            fg_reposts_list.setAdapter(weiboRepostsAdapter);
                            weiboRepostsAdapter.notifyDataSetChanged();
                        }
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
