package com.play.pro.constants;

/**
 * 常量配置
 */
public class FinalConstants {

	/** 基数 */
	public static final int FINAL_BASE = 1000;
	// --
	/** 返回 */
	public static final int BACK = FINAL_BASE + 1;
	/** 全屏 */
	public static final int FULL_SCREEN = FINAL_BASE + 2;
	/** 播放 */
	public static final int PLAY_START = FINAL_BASE + 3;
	/** 恢复播放 */
	public static final int PLAY_RESTART = FINAL_BASE + 4;
	/** 暂停播放 */
	public static final int PLAY_PAUSE = FINAL_BASE + 5;
	/** 播放结束 */
	public static final int PLAY_COMPLE = FINAL_BASE + 6;
	/** 播放时间定时器触发 */
	public static final int PLAY_TIME = FINAL_BASE + 7;
	/** 倒计时定时器触发 */
	public static final int COUNT_DOWN = FINAL_BASE + 8;
	/** 重新加载 */
	public static final int RELOAD = FINAL_BASE + 9;
	/** 播放异常 */
	public static final int PLAY_ERROR = FINAL_BASE + 10;
	/** 播放时间改变触发 */
	public static final int PLAY_TIME_CHANGE = FINAL_BASE + 11;

	/** 封面地址 */
	public static final String COVER_URL = "coverUrl";
	/** 视频地址 */
	public static final String VIDEO_URL = "videoUrl";
	/** 是否播放结束 */
	public static final String IS_PLAY_FINISH = "isPlayFinish";
	/** 是否点击Home键 */
	public static final String IS_CLICK_HOME = "isClickHome";
}
