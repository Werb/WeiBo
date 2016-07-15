package com.werb.weibo.util;

import com.sina.weibo.sdk.openapi.models.User;

/**
 * Created by Werb on 2016/7/13.
 * Email：1025004680@qq.com
 * RxBus异步响应事件类型
 */
public class RxEvents {

    /**
     * 获取授权用户信息
     */
    public static class UserEvent {
        public User user;

        public UserEvent(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }
    }

    /**
     * 点击RadioBtn实现加载数据
     */
    public static class RadionBtnClick{
        public int id;

        public RadionBtnClick(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
