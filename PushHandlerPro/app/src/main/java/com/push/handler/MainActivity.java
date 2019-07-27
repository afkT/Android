package com.push.handler;

import android.os.Bundle;

import com.push.handler.utils.LogPrintUtils;

import cn.jpush.android.api.JPushInterface;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 打印日志
        LogPrintUtils.setPrintLog(true);
        // 表示属于debug
        JPushInterface.setDebugMode(true);
        // 初始化
        JPushInterface.init(getApplicationContext());
        // 绑定别名
        JPushInterface.setAlias(this, 101010, "123456");

        // 默认绑定别名 123456

        // 该项目主要实现此需求:

        // 收到推送, 点击推送消息处理
        // 1. 如果 应用已经打开, 这直接做处理, 如跳转页面, 打开链接等功能
        // 2. 如果 应用没有开启, 则默认先开启应用 (如果存在引导页面, 则显示引导页面), 然后进入首页后, 再做对应的处理(跳转页面, 打开链接等)

        // 同微信一样
        // 如果微信没有打开(后台被销毁), 有人发送消息过来, 点击通知栏, 则默认是打开微信, 显示欢迎页, 接着进入对应的群聊、或者私聊页面, 返回键则返回到消息列表页面
        // 如果微信已打开, 则不会再次启动微信, 直接进入对应的页面


        // == 效果 ==

        // 收到推送后, 并且点击推送

        // 没开启应用：

        // 打开应用 -> 默认显示 Launcher 页面( WelcomeActivity ) -> 接着进入 MainActivity -> 触发onResume方法, 判断成功后, 跳转到对应的 PushXxxActivity

        // 已经开启应用

        // 默认停留在 MainActivity 页面, 触发onResume方法, 判断成功后, 跳转到对应的 PushXxxActivity
    }
}
