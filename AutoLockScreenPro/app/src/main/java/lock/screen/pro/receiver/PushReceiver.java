package lock.screen.pro.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import cn.jpush.android.api.JPushInterface;
import lock.screen.pro.utils.LockScreenUtils;
import lock.screen.pro.utils.LogPrintUtils;

/**
 * detail: 推送广播
 * Created by Ttt
 */
public class PushReceiver extends BroadcastReceiver {

    /** 日志Tag */
    private final String TAG = PushReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        // 获取跳转意图
        String action = intent.getAction();
        // ========
        // 判断处理
        // ========
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(action)) {
            LogPrintUtils.dTag(TAG, "JPush用户注册成功");
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(action)) { // 透传
            LogPrintUtils.dTag(TAG, "接受到推送下来的自定义消息");
            // 锁定屏幕
            LockScreenUtils.lockScreen();
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(action)) { // 通知
            LogPrintUtils.dTag(TAG, "接受到推送下来的通知"); // SDK 自动创建通知栏样式
            // 锁定屏幕
            LockScreenUtils.lockScreen();
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(action)) { // 点击通知栏消息
            LogPrintUtils.dTag(TAG, "用户点击打开了通知"); // JPushInterface.EXTRA_EXTRA
            // 锁定屏幕
            LockScreenUtils.lockScreen();
        } else {
            LogPrintUtils.dTag(TAG, "Unhandled intent - " + action);
        }
    }
}
