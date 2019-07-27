package com.play.pro;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap.Config;

/**
 * 整个项目全局对象
 */
public class BaseApplication extends Application {

	/** 全局上下文 */
	protected Context mContext;

	@Override
	public void onCreate() {
		super.onCreate();
		// 初始化全局上下文
		mContext = getApplicationContext();
		// 初始化ImageLoader
		initImageLoader();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	// =============================

	public void initImageLoader() {
		// 图片缓存
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				//.showImageOnLoading(R.drawable.bg_df_loader) // 设置图片在下载期间显示的图片
				////.showStubImage(R.drawable.loading_image_hint) // 设置图片在下载期间显示的图片
				//.showImageForEmptyUri(R.drawable.bg_df_loader) // 设置图片Uri为空或是错误的时候显示的图片 
				//.showImageOnFail(R.drawable.bg_df_loader) // 设置图片加载/解码过程中错误时候显示的图片
				.imageScaleType(ImageScaleType.EXACTLY) // 设置图片缩放
				.bitmapConfig(Config.RGB_565) // 图片解码类型
				.cacheInMemory(true)// 是否保存到内存
				.cacheOnDisk(true).build();// 是否保存到sd卡上（硬盘控件）

		// 针对图片缓存的全局配置，主要有线程类、缓存大小、磁盘大小、图片下载与解析、日志方面的配置。
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext)
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
				.imageDownloader(new BaseImageDownloader(mContext)) // default
				.tasksProcessingOrder(QueueProcessingType.LIFO).build();
		ImageLoader.getInstance().init(config);
	}
}
