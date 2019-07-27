package lock.screen.pro;

import android.app.Application;
import android.content.Context;

import lock.screen.pro.utils.LockScreenUtils;
import lock.screen.pro.utils.LogPrintUtils;

public class BaseApplication extends Application{

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        // 打印日志
        LogPrintUtils.setPrintLog(true);
        // 首先初始化
        LockScreenUtils.init(this);
    }

    public static Context getContext() {
        return sContext;
    }
}
