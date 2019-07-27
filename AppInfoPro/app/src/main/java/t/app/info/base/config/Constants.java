package t.app.info.base.config;

/**
 * detail: 常量
 * Created by Ttt
 */
public final class Constants {

    /**
     * detail: Key 常量
     * Created by Ttt
     */
    public interface Key {
        /** 包名 */
        String KEY_PACKNAME = "packName";
        /** apk地址 */
        String KEY_APK_URI = "apkUri";
        /** 应用排序 */
        String KEY_APP_SORT = "appSort";
        /** 搜索后缀 */
        String KEY_QUERY_SUFFIX = "querySuffix";
    }

    /**
     * detail: 通知 常量
     * Created by Ttt
     */
    public interface Notify {

        int BASE = 1000;

        /** 查询 APP 列表结束通知 */
        int H_QUERY_APPLIST_END_NOTIFY = BASE + 1;
        /** 查询 手机参数 结束通知 */
        int H_QUERY_DEVICE_INFO_END_NOTIFY = BASE + 2;
        /** 导出设备信息通知 */
        int H_EXPORT_DEVICE_MSG_NOTIFY = BASE + 3;
        /** 导出 APP 信息通知 */
        int H_EXPORT_APP_MSG_NOTIFY = BASE + 4;
        /** 导出应用 APK 安装包通知 */
        int H_EXPORT_APP_NOTIFY = BASE + 5;
        /** 应用排序变更 */
        int H_APP_SORT_NOTIFY = BASE + 6;
        /** 搜索合并通知 */
        int H_SEARCH_COLLAPSE = BASE + 7;
        /** 搜索展开通知 */
        int H_SEARCH_EXPAND = BASE + 8;
        /** 搜索输入内容通知 */
        int H_SEARCH_INPUT_CONTENT = BASE + 9;
        /** 切换 Fragment 通知 */
        int H_TOGGLE_FRAGMENT_NOTIFY = BASE + 10;
        /** 搜索文件资源结束通知 */
        int H_QUERY_FILE_RES_END_NOTIFY = BASE + 11;
        /** 搜索文件资源中通知 */
        int H_QUERY_FILE_RES_ING_NOTIFY = BASE + 12;
        /** 刷新通知 */
        int H_REFRESH_NOTIFY = BASE + 13;
        /** 删除文件通知 */
        int H_DELETE_APK_FILE_NOTIFY = BASE + 14;
    }

    /**
     * detail: 跳转回传 请求code
     * Created by Ttt
     */
    public interface RequestCode {

        int BASE = 3000;

        /** 跳转 App 详情页面 回传 */
        int FOR_R_APP_DETAILS = BASE + 1;
        /** 卸载 App 回传 */
        int FOR_R_APP_UNINSTALL = BASE + 2;
    }
}
