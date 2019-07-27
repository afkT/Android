package ttt.scan;

import android.app.Application;

import dev.DevUtils;
import dev.utils.app.logger.DevLogger;
import dev.utils.app.logger.LogConfig;
import dev.utils.app.logger.LogLevel;

public class BaseApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        // 默认初始化
        DevUtils.init(this);
        // 打开日志
        DevUtils.openLog();
        // == 初始化日志配置 ==
        // 设置默认Logger配置
        LogConfig logConfig = new LogConfig();
        logConfig.logLevel = LogLevel.DEBUG;
        DevLogger.init(logConfig);
    }
}
