package com.push.handler;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * detail: 欢迎页面
 * Created by Ttt
 */
public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // 2秒 延迟 跳转
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(mActivity, MainActivity.class));
                finish();
            }
        }, 2000);
    }
}
