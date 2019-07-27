package t.app.info.beans.item;

import java.io.File;

import dev.utils.app.info.AppInfoBean;

/**
 * detail: 文件资源 Item
 * Created by Ttt
 */
public class FileResItem {

    // APP信息
    private AppInfoBean appInfoBean;
    // 文件 - File
    private File file;
    // 文件 - 名字(前缀.后缀)
    private String frName;
    // 文件 - 地址
    private String frUri;
    // 文件 - MD5
    private String frMD5;
    // 文件最后操作时间
    private long lastModified;

    /**
     * 初始化构造函数
     * @param appInfoBean
     * @param file
     * @param frName
     * @param frUri
     * @param frMD5
     */
    public FileResItem(AppInfoBean appInfoBean, File file, String frName, String frUri, String frMD5) {
        this.appInfoBean = appInfoBean;
        this.file = file;
        this.frName = frName;
        this.frUri = frUri;
        this.frMD5 = frMD5;
        this.lastModified = file.lastModified();
    }

    public AppInfoBean getAppInfoBean() {
        return appInfoBean;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return frName;
    }

    public String getUri() {
        return frUri;
    }

    public String getMD5() {
        return frMD5;
    }

    public long getLastModified() {
        return lastModified;
    }
}
