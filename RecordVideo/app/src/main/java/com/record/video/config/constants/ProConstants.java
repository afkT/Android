package com.record.video.config.constants;

import java.io.File;

/**
 * detail: 项目常量类
 * Created by Ttt
 */
public class ProConstants {
	
	// ============== 录制项目 ===============
	/** 项目名 */
	public static final String BASE_NAME = "RecordVideo";

	// ---------------------- 本地应用数据 -------------------------

	/** 数据缓存 cache*/
	public static final String BASE_APPLICATION_CACHE_PATH = File.separator + BASE_NAME + "Data" + File.separator;

	/** 视频录制保存地址 */
	public static final String AP_VIDEO_RECORD_PATH = BASE_APPLICATION_CACHE_PATH + "Video" + File.separator;

	/** 视频录制保存地址 */
	public static final String AP_VIDEO_RECORD_COVER_PATH = AP_VIDEO_RECORD_PATH + "vCover" + File.separator;

}
