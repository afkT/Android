package t.app.info.base;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.support.multidex.MultiDex;

import dev.DevUtils;
import dev.utils.app.logger.DevLogger;
import dev.utils.app.logger.LogConfig;
import dev.utils.app.logger.LogLevel;
import t.app.info.BuildConfig;

/**
 * detail: BaseApplication
 * Created by Ttt
 */
public class BaseApplication extends Application {

    // 日志TAG
    private final String LOG_TAG = BaseApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化全局上下文
        DevUtils.init(getApplicationContext());
        // 属于 DEBUG 才开启日志
        if (BuildConfig.DEBUG){
            // == 初始化日志配置 ==
            // 设置默认Logger配置
            LogConfig logConfig = new LogConfig();
            logConfig.logLevel = LogLevel.DEBUG;
            logConfig.tag = LOG_TAG;
            logConfig.displayThreadInfo = false;
            logConfig.methodCount = 0;
            logConfig.sortLog = true; // 美化日志, 边框包围
            DevLogger.init(logConfig);
            // 打开 lib 内部日志 - 线上环境, 不调用方法就行
            DevUtils.openLog();
            DevUtils.openDebug();
        }

        // android 7.0系统解决拍照的问题
        if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.JELLY_BEAN_MR2) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}
