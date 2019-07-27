package t.app.info.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.gson.GsonBuilder;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import dev.DevUtils;
import dev.lib.other.EventBusUtils;
import dev.utils.app.info.AppInfoBean;
import dev.utils.app.info.AppInfoUtils;
import dev.utils.app.logger.DevLogger;
import dev.utils.app.share.IPreference;
import dev.utils.app.share.SharedUtils;
import t.app.info.base.config.Constants;
import t.app.info.base.event.QueryAppEvent;

/**
 * detail: 项目工具类
 * Created by Ttt
 */
public final class ProUtils {

    private ProUtils() {
    }

    // 日志 TAG
    private static final String TAG = ProUtils.class.getSimpleName();
    // 判断是否获取app列表中
    private static boolean isGetAppsIng = false;
    /** 保存APP信息 */
    public static final HashMap<AppInfoBean.AppType, ArrayList<AppInfoBean>> sMapAppInfos = new HashMap<>();

    /**
     * 转换String
     * @param obj
     * @return
     */
    public static String toJsonString(Object obj) {
        try {
            return new GsonBuilder().setPrettyPrinting().create().toJson(obj);
        } catch (Exception e) {
            DevLogger.eTag(TAG, e, "toJsonString");
        }
        return null;
    }

    /**
     * 重置复位
     */
    public static void reset() {
        isGetAppsIng = false;
        sMapAppInfos.clear();
    }

    /**
     * 清空APP数据
     * @param appType
     */
    public static void clearAppData(AppInfoBean.AppType appType) {
        // 清空数据
        sMapAppInfos.put(appType, null);
    }

    /**
     * 获取App列表
     * @param appType
     * @return
     */
    public static ArrayList<AppInfoBean> getAppLists(AppInfoBean.AppType appType) {
        // 获取对应的类型应用列表
        ArrayList<AppInfoBean> listApps = sMapAppInfos.get(appType);
        if (listApps == null) {
            if (isGetAppsIng) {
                return listApps;
            }
            // 表示查询中
            isGetAppsIng = true;
            // 开启线程查询
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 后台获取请求列表
                    getAppLists();
                }
            }).start();
        }
        return listApps;
    }

    /**
     * 获取 App 列表
     */
    private static void getAppLists() {
        // 用户安装应用
        ArrayList<AppInfoBean> listUserApps = new ArrayList<>();
        // 系统应用
        ArrayList<AppInfoBean> listSystemApps = new ArrayList<>();
        // 管理应用程序包
        PackageManager pManager = DevUtils.getContext().getPackageManager();
        // 获取手机内所有应用
        List<PackageInfo> packlist = pManager.getInstalledPackages(0);
        // 遍历 app 列表
        for (int i = 0, len = packlist.size(); i < len; i++) {
            PackageInfo pInfo = packlist.get(i);
            // 获取app 类型
            AppInfoBean.AppType appType = AppInfoBean.getAppType(pInfo);
            // 判断类型
            switch (appType) {
                case USER:
                    // 添加符合条件的 App 应用信息
                    listUserApps.add(AppInfoUtils.getAppInfoBean(pInfo.packageName));
                    break;
                case SYSTEM:
                    // 添加符合条件的 App 应用信息
                    listSystemApps.add(AppInfoUtils.getAppInfoBean(pInfo.packageName));
                    break;
            }
        }
        // 进行排序
        sortAppLists(listUserApps);
        sortAppLists(listSystemApps);
        // 表示查询结束
        isGetAppsIng = false;
        // 保存用户应用
        sMapAppInfos.put(AppInfoBean.AppType.USER, listUserApps);
        // 保存系统应用
        sMapAppInfos.put(AppInfoBean.AppType.SYSTEM, listSystemApps);
        // 发送查询 APP 列表结束通知事件
        EventBusUtils.sendEvent(new QueryAppEvent(Constants.Notify.H_QUERY_APPLIST_END_NOTIFY));
    }

    // -

    /**
     * 获取排序类型
     * @return
     */
    public static int getAppSortType() {
        // 获取选中索引
        int sortPos = SharedUtils.get(Constants.Key.KEY_APP_SORT, IPreference.DataType.INTEGER);
        // 获取最大值，防止负数
        sortPos = Math.max(sortPos, 0);
        // 获取排序类型
        return sortPos;
    }

    /**
     * 排序 app 列表
     * @param listApps
     * @return
     */
    private static void sortAppLists(ArrayList<AppInfoBean> listApps) {
        // 进行排序
        Collections.sort(listApps, new AppListsComparator(ProUtils.getAppSortType()));
    }

    // ==

    /**
     * detail: App 列表排序对比类
     * Created by Ttt
     */
    private static class AppListsComparator implements Comparator<AppInfoBean> {
        // 排序类型
        private final int sortType;
        // 中文排序 - https://blog.csdn.net/u013249965/article/details/52507343
        private final Collator mCollator = Collator.getInstance();


        public AppListsComparator(int sortType) {
            this.sortType = sortType;
            this.mCollator.setStrength(Collator.PRIMARY);
        }

        public final int compare(AppInfoBean a, AppInfoBean b) {
            if (a != null && b != null) {
                if (sortType == 0) { // 按应用名称
                    return a.getAppName().compareTo(b.getAppName());
                } else if (sortType == 1) { // 文件大小
                    if (a.getApkSize() == b.getApkSize()) {
                        return 0; // 大小相同
                    } else {
                        return a.getApkSize() > b.getApkSize() ? 1 : -1; // 小的前面, 大的后面
                    }
                } else if (sortType == 2) { // 安装时间
                    if (a.getFirstInstallTime() == b.getFirstInstallTime()) {
                        return 0; // 安装时间相等
                    } else { //
                        return a.getFirstInstallTime() > b.getFirstInstallTime() ? -1 : 1; // 近期安装的在最前面
                    }
                }else if (sortType == 3) { // 更新时间
                    if (a.getLastUpdateTime() == b.getLastUpdateTime()) {
                        return 0; // 最后更新时间相同
                    } else {
                        return a.getLastUpdateTime() > b.getLastUpdateTime() ? -1 : 1; // 近期更新的在最前面
                    }
                }
            }
            return 0;
        }
    }
}
