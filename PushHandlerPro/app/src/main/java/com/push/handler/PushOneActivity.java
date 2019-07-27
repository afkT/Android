package com.push.handler;

import android.os.Bundle;
import android.widget.TextView;

/**
 * detail: 推送测试页面
 * Created by Ttt
 */
public class PushOneActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_one);

        StringBuilder builder = new StringBuilder();
        builder.append("\n" + this.getClass().getSimpleName());
        builder.append(getIntent().getStringExtra("data"));
        ((TextView) findViewById(R.id.vid_tv)).setText(builder.toString());
    }
}
