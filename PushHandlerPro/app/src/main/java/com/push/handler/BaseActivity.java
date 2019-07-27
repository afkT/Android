package com.push.handler;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.push.handler.receiver.push.PushHanderActivity;

/**
 * detail: 简化基类
 * Created by Ttt
 */
public class BaseActivity extends AppCompatActivity {

    // 日志TAG
    protected String TAG = BaseActivity.class.getSimpleName();
    /** 上下文 */
    protected Context mContext = null;
    /** 当前Activity */
    protected Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取当前类的类名
        TAG = this.getClass().getSimpleName();
        // 获取上下文
        mContext = this;
        // 获取Activity
        mActivity = this;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 判断是否存在推送消息处理
        PushHanderActivity.checkPush(this, this.getClass().getSimpleName());
    }
}
