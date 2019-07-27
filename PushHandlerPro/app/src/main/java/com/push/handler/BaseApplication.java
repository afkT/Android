package com.push.handler;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;

import com.push.handler.receiver.push.PushHanderActivity;

public class BaseApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        // 设置打开应用首个Activity
        PushHanderActivity.setLauncherClass(WelcomeActivity.class);
        // 设置推送处理接口
        PushHanderActivity.setPushHandler(new PushHanderActivity.IPushHandler() {
            @Override
            public boolean isHandlerPush(String value) {
                // 防止为null
                if (TextUtils.isEmpty(value)){
                    return false;
                }
                // ---
                if(value.equals(WelcomeActivity.class.getSimpleName())){
                    return false; // 属于欢迎页面则不进行处理
                }
                // 默认进行处理
                return true;
            }

            @Override
            public void onPushHandler(Activity activity, String pushData, String pushType) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("\npushType: " + pushType);
                buffer.append("\npushData: " + pushData);

                if (!TextUtils.isEmpty(pushData)){
                    if (pushData.startsWith("1")){
                        Intent intent = new Intent(activity, PushOneActivity.class);
                        intent.putExtra("data", buffer.toString());
                        activity.startActivity(intent);
                    } else if (pushData.startsWith("2")){
                        Intent intent = new Intent(activity, PushTwoActivity.class);
                        intent.putExtra("data", buffer.toString());
                        activity.startActivity(intent);
                    }
                }
            }
        });


        // == 实现思路 ==

        // 默认全部推送, 都跳转到统一一个页面 PushHanderActivity, 该页面不显示任何UI, 只是单独中转处理

        // 接着在 PushHanderActivity 中, 保存传递过来的推送消息、推送类型到 SharedPreferences (SP) 中, 并且默认跳转到应用 Launcher 页面 (startActivity(Launcher.class);)

        // 然后在全部 Activity 中的 onResume 中调用 PushHanderActivity.checkPush 方法, 主要就是通过获取 SP 存储的值, 判断是否存在推送消息

        // 不存在推送消息, 则不处理

        // 存在则处理, 并通过接口进行回调通知, => 会先通过 接口 isHandlerPush 方法判断是否处理推送, 如果处理, 并且存在数据则会调用 onPushHandler 接口方法

        // isHandlerPush 可以特殊处理, 正常传入的参数为 class getSimpleName 名, 容易判断是否不处理某些类



        // == 使用步骤 ==

        // 需要三步

        // 1.首先在 Application 中 调用 PushHanderActivity.setLauncherClass, 并且设置 Launcher 页面.class

        // 2.PushHanderActivity.setPushHandler 实现IPushHandler接口, 并且进行编写逻辑判断

        // 3.在整个项目 Activity基类 的onResume方法中, 调用 PushHanderActivity.checkPush(activity, value); // 正常 PushHanderActivity.checkPush(this, this.getClass().getSimpleName());



        // == 方法功能介绍 ==

        // PushHanderActivity.checkPush 该方法功能, 主要是判断是否存在推送数据, 以及对推送数据特殊处理

        // 需要在onResume中处理, 是因为如果应用已经打开, 并且停留在某个页面, 点击推送会再次触发onResume, 这个时候进行判断是否处理推送

        // ====

        // 而 isHandlerPush 方法, 则主要是判断是否在该页面处理推送消息, 正常是忽略欢迎、引导页面

        // 只有正常页面才进行处理, 防止影响到需要强制显示的页面(如欢迎页面, 会显示一定延迟时间后, 才处理推送)

        // onPushHandler 方法是自己逻辑处理，点击推送后做什么操作
    }
}
