package com.record.video.config.constants;

/**
 * detail: 通知相关常量
 * Created by Ttt
 */
public class NotifyConstants {

    // ==================================
    // ============ 通知常量 ============
    // ==================================

    /** 通知常量 基数 */
    private static final int BASE_NOTIFY = 10000;
    /** 检查权限 */
    public static final int NOTIFY_CHECK_PERMISSION = BASE_NOTIFY + 1;
    /** 录制结束 */
    public static final int NOTIFY_RECORD_END = BASE_NOTIFY + 2;
    /** 开始录制 */
    public static final int NOTIFY_RECORD_START = BASE_NOTIFY + 3;
    /** 录制计时 */
    public static final int NOTIFY_RECORD_TIMING = BASE_NOTIFY + 4;
    /** 录制检测 */
    public static final int NOTIFY_RECORD_CHECK = BASE_NOTIFY + 5;
    /** 录制操作回调 */
    public static final int NOTIFY_RECORD_OPERATE = BASE_NOTIFY + 6;

}
