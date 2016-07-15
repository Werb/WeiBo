package com.werb.weibo.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.werb.weibo.ui.UrlActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Werb on 2016/7/11.
 * Email：1025004680@qq.com
 * 微博文本信息工具类
 */
public class StringUtil {

    //<a href="http://weibo.com" rel="nofollow">新浪微博</a>

    /**
     * 截取source中的来源
     *
     * @param html
     * @return
     */
    public static String getWeiboSource(String html) {
        int s0 = html.indexOf(">");
        if (s0 == -1) return null;
        int s1 = html.indexOf("</a>", s0);
        if (s1 == -1) return null;
        try {
            return html.substring(s0 + 1, s1);
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将微博正文中的 @ 和 # ，url标识出
     * @param text
     * @return
     */
    public static void setWeiBoText(Context context, String text, TextView textView) {
        //三种正则表达式
        Pattern AT_PATTERN = Pattern.compile("@[\\u4e00-\\u9fa5\\w\\-]+");
        Pattern TAG_PATTERN = Pattern.compile("#([^\\#|.]+)#");
        Pattern Url_PATTERN = Pattern.compile("((http|https|ftp|ftps):\\/\\/)?([a-zA-Z0-9-]+\\.){1,5}(com|cn|net|org|hk|tw)((\\/(\\w|-)+(\\.([a-zA-Z]+))?)+)?(\\/)?(\\??([\\.%:a-zA-Z0-9_-]+=[#\\.%:a-zA-Z0-9_-]+(&amp;)?)+)?");


        SpannableString spannable = new SpannableString (text);

        Matcher tag = TAG_PATTERN.matcher(spannable);
        while (tag.find()) {
            String tagNameMatch = tag.group();
            int start = tag.start();
            System.out.println("#-" + tagNameMatch + "-" + start + "-" + tagNameMatch.length());
            ForegroundColorSpan span = new ForegroundColorSpan(Color.parseColor("#3F51B5"));
            spannable.setSpan(span, start, start+tagNameMatch.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        Matcher at = AT_PATTERN.matcher(spannable);
        while (at.find()) {
            String atUserName = at.group();
            int start = at.start();
            System.out.println("@-" + atUserName + "-" + start + "-" + atUserName.length());
            ForegroundColorSpan span = new ForegroundColorSpan(Color.parseColor("#3F51B5"));
            spannable.setSpan(span, start, start+atUserName.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        Matcher url = Url_PATTERN.matcher(spannable);
        while (url.find()) {
            String urlString = url.group();
            int start = url.start();
            System.out.println("url-" + urlString + "-" + start + "-" + urlString.length());
            ForegroundColorSpan span = new ForegroundColorSpan(Color.parseColor("#f44336"));
            spannable.setSpan(span, start, start+urlString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new MyURLSpan(context,urlString),start,start+urlString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spannable);

    }

    /**
     * 用于weibo text中的连接跳转
     */
    private static class MyURLSpan extends ClickableSpan {
        private String mUrl;
        private Context context;
        MyURLSpan(Context ctx,String url) {
            context = ctx;
            mUrl = url;
        }

        @Override
        public void onClick(View widget) {
            System.out.println("---url---activity"+mUrl);
            Intent intent = UrlActivity.newIntent(context,mUrl);
            context.startActivity(intent);

        }
    }
}
