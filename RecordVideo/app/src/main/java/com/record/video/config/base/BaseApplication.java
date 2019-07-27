package com.record.video.config.base;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.record.video.utils.MediaDealUtils;

import dev.logger.DevLogger;
import dev.logger.DevLoggerUtils;
import dev.logger.LogConfig;
import dev.logger.LogLevel;

/**
 * detail: BaseApplication
 * Created by Ttt
 */
public class BaseApplication extends Application {

    /** 日志TAG */
    static final String TAG = BaseApplication.class.getSimpleName();
    /** 全局上下文 */
    static Context sContext;
    /** 全局Handler,便于子线程快捷操作等 */
    static Handler sHandler;
    /** 获取当前线程,主要判断是否属于主线程 */
    static Thread sUiThread;
    /** 当前Application对象 */
    static BaseApplication cInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化全局上下文
        sContext = getApplicationContext();
        // 保存当前线程信息
        sUiThread = Thread.currentThread();
        // 初始化全局Handler
        sHandler = new Handler();
        // 保存当前Application 对象
        cInstance = this;

        // 初始化日志库
        DevLogger.init(DevLoggerUtils.getDebugLogConfig(BaseApplication.class.getSimpleName()));
        DevLoggerUtils.appInit(sContext);
        // 初始化视频缩略图处理操作
        MediaDealUtils.getInstance().initCtx(sContext);

        // 图片缓存
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY) // 设置图片缩放
                .bitmapConfig(Bitmap.Config.RGB_565) // 图片解码类型
                .cacheInMemory(true) // 是否保存到内存
                .cacheOnDisc(true).build(); // 是否保存到sd卡上（硬盘控件）

        // 针对图片缓存的全局配置，主要有线程类、缓存大小、磁盘大小、图片下载与解析、日志方面的配置。
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions) // 加载DisplayImageOptions参数
                .threadPriority(Thread.NORM_PRIORITY - 2) // 线程池内加载的数量
                .denyCacheImageMultipleSizesInMemory()
                //.memoryCache(new UsingFreqLimitedMemoryCache(1024 * 1024)) // 通过自己的内存缓存实现
                .memoryCacheSize(2 * 1024 * 1024) // 内存缓存最大值
                .memoryCacheSizePercentage(13)
                //.diskCacheSize(50 * 1024 * 1024) // SD卡缓存最大值 50mb
                //.discCacheFileNameGenerator(new Md5FileNameGenerator()) // 将保存的时候的URI名称用MD5 加密
                //.diskCacheFileCount(100) // 缓存的文件数量
                //.memoryCache(new WeakMemoryCache()).diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .imageDownloader(new BaseImageDownloader(getApplicationContext())) // default
                .tasksProcessingOrder(QueueProcessingType.LIFO).build();
        ImageLoader.getInstance().init(config);

        // == 在BaseApplication 中调用 ==
        // 初始化日志配置
        LogConfig lConfig = new LogConfig();
        // 堆栈方法总数(显示经过的方法)
        lConfig.methodCount = 3;
        // 堆栈方法索引偏移(0 = 最新经过调用的方法信息,偏移则往上推,如 1 = 倒数第二条经过调用的方法信息)
        lConfig.methodOffset = 0;
        // 是否输出全部方法(在特殊情况下，如想要打印全部经过的方法，但是不知道经过的总数)
        lConfig.isOutputMethodAll = false;
        // 显示日志线程信息(特殊情况，显示经过的线程信息,具体情况如上)
        lConfig.isDisplayThreadInfo = false;
        // 是否排序日志(格式化后)
        lConfig.isSortLog = false;
        // 日志级别
        lConfig.logLevel = LogLevel.DEBUG;
        // 设置Tag（特殊情况使用，不使用全部的Tag时,如单独输出在某个Tag下）
        lConfig.tag = TAG;
        // 进行初始化配置 => 这样设置后, 默认全部日志都使用改配置, 特殊使用 DevLogger.other(config).d(xxx);
        DevLogger.init(lConfig);
        // 进行初始化配置 - 必须调用 => 在DevUtils.init() 内部调用了
        DevLoggerUtils.appInit(sContext);
    }

    /**
     * 获取当前Application
     * @return
     */
    public static BaseApplication getInstance() {
        return cInstance;
    }

    /**
     * 获取全局上下文
     * @return
     */
    public static Context getAppContext() {
        return sContext;
    }

    /**
     * @param action 若当前非UI线程则切换到UI线程执行
     */
    public static void runOnUiThread(Runnable action) {
        if (Thread.currentThread() != sUiThread) {
            sHandler.post(action);
        } else {
            action.run();
        }
    }
}
