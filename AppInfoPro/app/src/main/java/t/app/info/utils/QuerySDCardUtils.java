package t.app.info.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import dev.lib.other.EventBusUtils;
import dev.utils.app.SDCardUtils;
import dev.utils.app.info.AppInfoBean;
import dev.utils.app.info.AppInfoUtils;
import dev.utils.app.logger.DevLogger;
import dev.utils.common.DevCommonUtils;
import dev.utils.common.FileUtils;
import t.app.info.base.config.Constants;
import t.app.info.base.event.QueryFileEvent;
import t.app.info.beans.item.FileResItem;

/**
 * detail: 搜索SD卡工具类
 * Created by Ttt
 */
public final class QuerySDCardUtils {

    private QuerySDCardUtils() {
    }

    // 日志 TAG
    private final String TAG = QuerySDCardUtils.class.getSimpleName();
    // 初始化实体类
    private static QuerySDCardUtils instance = new QuerySDCardUtils();

    /**
     * 单例获取数据
     * @return
     */
    public static QuerySDCardUtils getInstance() {
        return instance;
    }

    /** 是否查询过 */
    private boolean isQueryed;
    /** 是否查询中 */
    private boolean isQueryIng;
    /** 后台查询线程 */
    private Runnable querySDCardRun;
    /** 查询SDCard资源文件 */
    private ArrayList<FileResItem> listFileResItems;

    /** 重置复位 */
    public void reset() {
        isQueryed = false;
        isQueryIng = false;
        if (listFileResItems != null) {
            listFileResItems.clear();
        }
        listFileResItems = null;
    }

    /**
     * 查询 SDCard 资源文件
     */
    public void querySDCardRes() {
        if (isQueryed) {
            // 发送搜索文件资源结束通知事件
            EventBusUtils.sendEvent(new QueryFileEvent(Constants.Notify.H_QUERY_FILE_RES_END_NOTIFY));
            return;
        }
        // 发送搜索文件资源中通知事件
        EventBusUtils.sendEvent(new QueryFileEvent(Constants.Notify.H_QUERY_FILE_RES_ING_NOTIFY));
        // 初始化线程
        initQueryRunn();
        // 如果查询中则不处理
        if(!isQueryIng) {
            // 表示查询中
            isQueryIng = true;
            // 开启线程查询
            new Thread(querySDCardRun).start();
        }
    }

    /**
     * 是否搜索结束
     * @return
     */
    public boolean isQueryed() {
        return isQueryed;
    }

    /**
     * 判断是否查询中
     * @return
     */
    public boolean isQueryIng() {
        return isQueryIng;
    }

    /**
     * 获取数据源
     * @return
     */
    public ArrayList<FileResItem> getListFileResItems() {
        return listFileResItems;
    }

    // ====

    /**
     * 初始化查询资源线程
     * @return
     */
    private Runnable initQueryRunn() {
        if(querySDCardRun == null) {
            querySDCardRun = new Thread() {
                @Override
                public void run() {
                    try {
                        ArrayList<FileResItem> listDatas = new ArrayList<>();
                        // 查询数据
                        int result = querySDCardRes(SDCardUtils.getSDCardPath() + File.separator, listDatas, QuerySuffixUtils.getFilterSuffixs());
                        // 进行保存
                        listFileResItems = new ArrayList<>(listDatas);
                        // 通知搜索结束
                        isQueryed = true;
                        isQueryIng = false;
                        // 进行排序
                        Collections.sort(listFileResItems, new ApkListsComparator());
                        // 发送搜索文件资源结束通知事件
                        EventBusUtils.sendEvent(new QueryFileEvent(Constants.Notify.H_QUERY_FILE_RES_END_NOTIFY, result));
                    } catch (Exception e) {
                    }
                }
            };
        }
        return querySDCardRun;
    };
    
    // =

    /**
     * 获取SDCard 资源
     * @param bPath 根目录地址
     * @param listRes 数据源
     * @param filterSuffixs 过滤的后缀
     * return 返回状态 -1 异常(无权限)， 0 无文件，1 查询成功, -2 过滤条件为null
     */
    private int querySDCardRes(String bPath, ArrayList<FileResItem> listRes, String[] filterSuffixs) {
        if (filterSuffixs == null) {
            return -2;
        }
        int resultCode = 0;
        try {
            // 清空旧数据
            listRes.clear();
            // 获取SDCard 根目录
            File file = new File(bPath);
            if(file != null) {
                // 获取文件夹全部子文件
                String[] filelist = file.list();
                // 获取文件总数
                int count = filelist.length;
                if(count!=0) {
                    queryFile(bPath + File.separator, listRes, filterSuffixs);
                    resultCode = 1;
                }
            }
        } catch (Exception e) {
            resultCode = -1;
        }
        return resultCode;
    }


    /**
     * 查询文件
     * @param filePath
     * @param listRes 数据源
     * @param filterSuffixs 过滤的后缀
     */
    private void queryFile(String filePath, ArrayList<FileResItem> listRes, String[] filterSuffixs) {
        try {
            File file = new File(filePath);
            if(file != null) {
                // 获取文件夹全部子文件
                String[] filelist = file.list();
                // 获取文件总数
                int count = filelist.length;
                // 共用实体类
                FileResItem fileResItem;
                if(count!=0) {
                    for (int i = 0; i < count; i++) {
                        // 路径加上文件名
                        File readfile = new File(filePath + filelist[i]);
                        // 判断是否属于文件夹
                        if (readfile.isDirectory()) {
                            // 属于文件夹
                            queryFile(readfile.getPath() + File.separator, listRes, filterSuffixs);
                        } else {
                            // 属于文件
                            fileResItem = getFileToFileResItem(readfile, filterSuffixs);
                            if(fileResItem != null) {
                                listRes.add(fileResItem);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            DevLogger.eTag(TAG, e, "queryFile");
        }
    }

    /**
     * 通过File 获取 FileResItem
     * @param file 文件信息
     * @param filterSuffixs 过滤的后缀
     * @return
     */
    private FileResItem getFileToFileResItem(File file, String[] filterSuffixs) {
        try {
            // 文件不等于null,并且存在该文件
            if(file != null && file.exists()) {
                String fName = file.getName(); // 文件名
                String fPath = file.getPath(); // 完整路径 file.getAbsolutePath()
//                String fPrefix = FileUtils.getFileNotSuffix(fName); // 获取文件前缀
//                String fSuffix = FileUtils.getFileSuffix(fName); // 获取文件后缀
                // 判断是否符合结尾
                if (DevCommonUtils.isEndsWith(true, fName, filterSuffixs)) {
                    // 获取App信息
                    AppInfoBean appInfoBean = AppInfoUtils.getAppInfoBeanToPath(fPath);
                    if (appInfoBean != null) {
                        // 初始化实体类
                        return new FileResItem(appInfoBean, file, fName, fPath, FileUtils.getFileMD5ToString(file));
                    }
                }
            }
        } catch (Exception e) {
            DevLogger.eTag(TAG, e, "getFileToFileResItem");
        }
        return null;
    }

    /**
     * detail: apk 列表排序对比类
     * Created by Ttt
     */
    private static class ApkListsComparator implements Comparator<FileResItem> {

        public final int compare(FileResItem a, FileResItem b) {
            if (a != null && b != null) {
                if (a.getLastModified() == b.getLastModified()) {
                    return 0; // 安装时间相等
                } else { //
                    return a.getLastModified() > b.getLastModified() ? -1 : 1; // 近期安装的在最前面
                }
            }
            return 0;
        }
    }
}
