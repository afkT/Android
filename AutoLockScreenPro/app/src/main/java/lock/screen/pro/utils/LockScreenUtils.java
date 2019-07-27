package lock.screen.pro.utils;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import lock.screen.pro.receiver.LockReceiver;

/**
 * detail: 锁屏工具类
 * Created by Ttt
 */
public final class LockScreenUtils {

    // 日志 TAG
    private static final String TAG = LockScreenUtils.class.getSimpleName();

    // 设备策略管理器
    private static DevicePolicyManager policyManager;
    // ComponentName
    private static ComponentName componentName;

    /**
     * 初始化
     * @param context
     */
    public static void init(Context context) {
        if (context != null) {
            if (policyManager == null) {
                // 初始化对象
                componentName = new ComponentName(context, LockReceiver.class);
                policyManager = (DevicePolicyManager) context.getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
            }
        }
    }

    /**
     * 请求权限
     * @param context
     * @return
     */
    public static boolean reqPermission(Context context) {
        // 判断是否存在权限
        boolean isAdminActive = isAdminActive();
        // 不存在才申请
        if (!isAdminActive) {
            // 启动设备管理(隐式Intent) - 在 AndroidManifest.xml 中设定相应过滤器
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "激活后才能使用锁屏功能");
            context.startActivity(intent);
        }
        return isAdminActive;
    }

    /**
     * 锁屏
     */
    public static void lockScreen() {
        try {
            StringBuilder builder = new StringBuilder();
            // =
            boolean isAdminActive = policyManager.isAdminActive(componentName);
            // 判断是否有权限
            if (isAdminActive) {
                policyManager.lockNow();
            }
            builder.append("是否存在权限: " + isAdminActive);
            builder.append("\n");
            builder.append("组件信息: " + componentName.toString());
            LogPrintUtils.dTag(TAG, builder.toString());
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "lockScreen");
        }
    }

    /**
     * 判断是否存在权限
     * @return
     */
    public static boolean isAdminActive() {
        boolean isAdminActive = false;
        try {
            // 判断是否存在权限
            isAdminActive = policyManager.isAdminActive(componentName);
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "isAdminActive");
        }
        // 打印信息
        LogPrintUtils.dTag(TAG, "isAdminActive() - 是否存在权限: " + isAdminActive);
        // 返回结果
        return isAdminActive;
    }
}
