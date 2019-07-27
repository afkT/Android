package t.app.info.base.config;

import java.io.File;

import dev.utils.app.SDCardUtils;

/**
 * detail: 项目常量
 * Created by Ttt
 */
public final class ProConstants {

    // ============== AppInfo ===============
    /** 项目名 */
    public static final String BASE_NAME = "AppInfo";
    /** 缩写标识  - 小写 */
    public static final String BASE_NAME_SHORT = "ai";
    /** 缩写标识  - 大写 */
    public static final String BASE_NAME_SHORT_CAP = "ai";
    /** SD卡路径 */
    public static final String BASE_SDCARD_PATH = SDCardUtils.getSDCardPath();

    // ---------------------- 本地SDCard数据 -------------------------

    /** 统一文件夹 */
    public static final String PRO_PATH =  BASE_SDCARD_PATH + File.separator + BASE_NAME + File.separator;

    /** 本地导出资源地址 */
    public static final String EXPORT_PATH = PRO_PATH + File.separator + "Export" + File.separator;

    /** App 信息导出地址 */
    public static final String EXPORT_APP_MSG_PATH = EXPORT_PATH + File.separator + "AppMsg" + File.separator;

    /** Apk 信息导出地址 */
    public static final String EXPORT_APK_MSG_PATH = EXPORT_PATH + File.separator + "ApkMsg" + File.separator;

    /** Apk 文件导出地址 */
    public static final String EXPORT_APK_PATH = EXPORT_PATH + File.separator + "APK" + File.separator;
}
