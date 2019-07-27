package com.record.video.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;

import dev.logger.DevLogger;

/**
 * detail: 开发工具类
 * Created by Ttt
 */
public final class DevUtils {

    private DevUtils(){
    }

    static final String TAG = DevUtils.class.getSimpleName();

    // ==================
    // == 简单判断方法 ==
    // ==================

    /**
     * 获取长度，如果字符串为null,则返回0
     * @param str
     * @return
     */
    public static int length(String str) {
        return str == null ? 0 : str.length();
    }

    /**
     * 字符串转整数
     * @param str 需要转换的字符串
     * @param defValue 转换失败，默认返回值
     * @return
     */
    public static int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
        }
        return defValue;
    }

    /**
     * 字符串转 float
     * @param str 需要转换的字符串
     * @param defValue 转换失败，默认返回值
     * @return
     */
    public static float toFloat(String str, long defValue){
        try {
            return Float.parseFloat(str);
        } catch (Exception e) {
        }
        return defValue;
    }

    /**
     * 字符串转 long
     * @param str 需要转换的字符串
     * @param defValue 转换失败，默认返回值
     * @return
     */
    public static long toLong(String str, long defValue){
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
        }
        return defValue;
    }

    // ==============
    // == 文件操作 ==
    // ==============

    /**
     * 获取文件名
     * @param path 文件路径
     * @return
     */
    public static String getName(String path){
        return getName(path, "");
    }

    /**
     * 获取文件名
     * @param path 文件路径
     * @param dfStr
     * @return
     */
    public static String getName(String path, String dfStr){
        if (path != null && path.length() != 0) {
            try {
                return new File(path).getName();
            } catch (Exception e){
            }
        }
        return dfStr;
    }

    /**
     * 获得文件后缀名
     * @param fName 文件名
     * @return
     */
    public static String getFileSuffix(String fName) {
        String result = null;
        if (fName != null) {
            result = "";
            if (fName.lastIndexOf('.') != -1) {
                result = fName.substring(fName.lastIndexOf('.'));
                if (result.startsWith(".")) {
                    result = result.substring(1);
                }
            }
        }
        return result;
    }

    /**
     * 获得文件名(无后缀)
     * @param fName
     * @return
     */
    public static String getFileNotSuffix(String fName) {
        String result = null;
        if (fName != null) {
            if (fName.lastIndexOf('.') != -1) {
                result = fName.substring(0,fName.lastIndexOf('.'));
            } else {
                result = fName;
            }
        }
        return result;
    }

    /**
     * 检查是否存在某个文件
     * @param fPath 文件路径
     * @return 是否存在文件
     */
    public static boolean isFileExists(String fPath) {
        if (fPath != null){
            try {
                File file = new File(fPath);
                if(file.exists()) {
                    return true;
                }
            } catch (Exception e) {
                DevLogger.eTag(TAG, e, "isFileExists");
                // --
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 保存图片
     * @param fPath 保存路径
     * @param bmp 图片数据
     * @param quality 压缩比例
     */
    public static void saveBitmap(String fPath, Bitmap bmp, Bitmap.CompressFormat cfType, int quality) {
        File f = new File(fPath);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bmp.compress(cfType, quality, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            DevLogger.eTag(TAG, e, "saveBitmap - 报错失败");
        }
    }

    /**
     * 保存文件
     * @param bytes 保存内容
     * @param fPath 保存路径
     * @param fName 文件名
     * @return 是否保存成功
     */
    public static boolean saveFile(byte[] bytes, String fPath, String fName) {
        try {
            // 防止文件没创建
            DevUtils.createFile(fPath);
            // 保存路径
            File sFile = new File(fPath, fName);
            // 保存内容到一个文件
            FileOutputStream fos = new FileOutputStream(sFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(bytes);
            bos.close();
            fos.close();
            return true;
        } catch (Exception e) {
            DevLogger.eTag(TAG, e, "saveFile");
            e.printStackTrace();
        }
        return false;
    }

    // =======================
    // === SDCard 操作相关 ===
    // =======================

    /**
     * 判断SDCard是否正常挂载
     * @return
     */
    public static boolean isSDCardEnable() {
        // android.os.Environment
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡路径
     *
     * @return
     */
    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }

    /**
     * 获取缓存地址
     * @param mContext
     * @return
     */
    public static String getDiskCacheDir(Context mContext) {
        String cachePath;
        if (isSDCardEnable()) { // 判断SDCard是否挂载
            cachePath = mContext.getExternalCacheDir().getPath();
        } else {
            cachePath = mContext.getCacheDir().getPath();
        }
        // 防止不存在目录文件，自动创建
        DevUtils.createFile(cachePath);
        // 返回文件存储地址
        return cachePath;
    }


    /**
     * 获取缓存资源地址
     * @param mContext
     * @param fPath 文件地址
     * @return
     */
    public static File getCacheFile(Context mContext, String fPath) {
        return new File(getCachePath(mContext, fPath));
    }

    /**
     * 获取缓存资源地址
     * @param mContext
     * @param fPath 文件地址
     * @return
     */
    public static String getCachePath(Context mContext, String fPath){
        // 获取缓存地址
        String cachePath = new File(getDiskCacheDir(mContext), fPath).getAbsolutePath();
        // 防止不存在目录文件，自动创建
        DevUtils.createFile(cachePath);
        // 返回头像地址
        return cachePath;
    }

    // ==================
    // == 文件操作相关 ==
    // ==================

    /**
     * 判断某个文件夹是否创建,未创建则创建(纯路径 - 无文件名)
     * @param fPath 文件夹路径
     */
    public static File createFile(String fPath) {
        if (fPath != null) {
            try {
                File file = new File(fPath);
                // 当这个文件夹不存在的时候则创建文件夹
                if (!file.exists()) {
                    // 允许创建多级目录
                    file.mkdirs();
                    // 这个无法创建多级目录
                    // rootFile.mkdir();
                }
                return file;
            } catch (Exception e) {
                DevLogger.eTag(TAG, e, "createFile");
                // --
                e.printStackTrace();
            }
        }
        return null;
    }

    // =====================
    // == MD5相关处理操作 ==
    // =====================

    // 小写
    private static final char HEX_DIGITS[]={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
    // 大写
    private static final char HEX_DIGITS_UPPER[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};


    /**
     * 加密内容 - 32位大小MD5 - 小写
     * @param str 加密内容
     * @return
     */
    public final static String MD5(String str) {
        try {
            byte[] btInput = str.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            return toHexString(md, HEX_DIGITS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 加密内容 - 32位大小MD5 - 大写
     * @param str 加密内容
     * @return
     */
    public final static String MD5Upper(String str) {
        try {
            byte[] btInput = str.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            return toHexString(md, HEX_DIGITS_UPPER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 进行转换
     * @param bData
     * @param hexDigits
     * @return
     */
    static String toHexString(byte[] bData, char[] hexDigits) {
        StringBuilder sBuilder = new StringBuilder(bData.length * 2);
        for (int i = 0; i < bData.length; i++) {
            sBuilder.append(hexDigits[(bData[i] & 0xf0) >>> 4]);
            sBuilder.append(hexDigits[bData[i] & 0x0f]);
        }
        return sBuilder.toString();
    }

    // ===========================
    // == Context/Activity 判断 ==
    // ===========================

    /**
     * 判断页面是否关闭
     * @param activity
     * @return
     */
    public static boolean isFinishing(Activity activity){
        if (activity != null){
            return activity.isFinishing();
        }
        return false;
    }

    /**
     * 判断页面是否关闭
     * @param mContext
     * @return
     */
    public static boolean isFinishingCtx(Context mContext){
        if (mContext != null){
            try {
                return ((Activity) mContext).isFinishing();
            } catch (Exception e){
            }
        }
        return false;
    }

    // =============================
    // ======== Dialog 相关 ========
    // =============================

    /**
     * 关闭Dialog
     * @param dialog
     */
    public static void closeDialog(Dialog dialog){
        if (dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    // == 点击判断 ==

    // 上一次点击的标识id = viewId 等
    private static int lastTagId = -1;
    /** 上次点击时间 */
    private static long lastClickTime = 0l; // 局限性是, 全局统一事件，如果上次点击后，立刻点击其他就无法点
    /** 默认间隔时间 */
    private static long DF_DIFF = 1000l; // 点击间隔1秒内

    /**
     * 判断两次点击的间隔 小于默认间隔时间(1秒), 则认为是多次无效点击
     * @return
     */
    public static boolean isFastDoubleClick() {
        return isFastDoubleClick(-1, DF_DIFF);
    }

    /**
     * 判断两次点击的间隔 小于默认间隔时间(1秒), 则认为是多次无效点击
     * @param tagId
     * @return
     */
    public static boolean isFastDoubleClick(int tagId) {
        return isFastDoubleClick(tagId, DF_DIFF);
    }

    /**
     * 判断两次点击的间隔 小于间隔时间(diff), 则认为是多次无效点击
     * @param tagId
     * @param diff
     * @return
     */
    public static boolean isFastDoubleClick(int tagId, long diff) {
        long cTime = System.currentTimeMillis();
        long dTime = cTime - lastClickTime;
        // 判断时间是否超过
        if (lastTagId == tagId && lastClickTime > 0 && dTime < diff) {
            return true;
        }
        lastTagId = tagId;
        lastClickTime = cTime;
        return false;
    }

    // ======================
    // == 获取屏幕相关参数 ==
    // ======================

    /**
     * 通过上下文获取 DisplayMetrics (获取关于显示的通用信息，如显示大小，分辨率和字体)
     * @param mContext
     * @return
     */
    private static DisplayMetrics getDisplayMetrics(Context mContext) {
        try {
            WindowManager wManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            if (wManager != null) {
                DisplayMetrics dMetrics = new DisplayMetrics();
                wManager.getDefaultDisplay().getMetrics(dMetrics);
                return dMetrics;
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 通过上下文获取屏幕宽度
     * @param mContext
     * @return
     */
    @SuppressWarnings("deprecation")
    public static int getScreenWidth(Context mContext) {
        try {
            // 获取屏幕信息
            DisplayMetrics dMetrics = getDisplayMetrics(mContext);
            if (dMetrics != null) {
                return dMetrics.widthPixels;
            }
            // 这种也可以获取，不过已经提问过时(下面这段可以注释掉)
            WindowManager wManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            if (wManager != null) {
                return wManager.getDefaultDisplay().getWidth();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 通过上下文获取屏幕高度
     * @param mContext
     * @return
     */
    @SuppressWarnings("deprecation")
    public static int getScreenHeight(Context mContext) {
        try {
            // 获取屏幕信息
            DisplayMetrics dMetrics = getDisplayMetrics(mContext);
            if (dMetrics != null) {
                return dMetrics.heightPixels;
            }
            // 这种也可以获取，不过已经提示过时(下面这段可以注释掉)
            WindowManager wManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            if (wManager != null) {
                return wManager.getDefaultDisplay().getHeight();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 通过上下文获取屏幕宽度高度
     * @param mContext
     * @return int[] 0 = 宽度，1 = 高度
     */
    @SuppressWarnings("deprecation")
    public static int[] getScreenWidthHeight(Context mContext) {
        try {
            // 获取屏幕信息
            DisplayMetrics dMetrics = getDisplayMetrics(mContext);
            if (dMetrics != null) {
                return new int[] { dMetrics.widthPixels, dMetrics.heightPixels };
            }
            // 这种也可以获取，不过已经提示过时(下面这段可以注释掉)
            WindowManager wManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            if (wManager != null) {
                int width = wManager.getDefaultDisplay().getWidth();
                int height = wManager.getDefaultDisplay().getHeight();
                return new int[] { width, height };
            }
        } catch (Exception e) {
        }
        return null;
    }


    // ================
    // === View操作 ===
    // ================



    // --

    /**
     * 设置View显示状态
     * @param isVisibility
     * @param view
     */
    public static void setVisibility(boolean isVisibility, View view){
        if (view != null){
            view.setVisibility(isVisibility ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 设置View显示的状态
     * @param isVisibility
     * @param view
     */
    public static void setVisibility(int isVisibility, View view){
        if (view != null){
            view.setVisibility(isVisibility);
        }
    }

    // --

    /**
     * 设置View 显示的状态
     * @param isVisibility
     * @param views
     */
    public static void setVisibilitys(boolean isVisibility, View... views){
        setVisibilitys(isVisibility ? View.VISIBLE : View.GONE, views);
    }

    /**
     * 设置View 显示的状态
     * @param isVisibility
     * @param views
     */
    public static void setVisibilitys(int isVisibility, View... views){
        if (views != null && views.length != 0){
            for (int i = 0, c = views.length; i < c; i++){
                View view = views[i];
                if (view != null){
                    view.setVisibility(isVisibility);
                }
            }
        }
    }

    /**
     * 判断View 是否显示
     * @param view
     * @return
     */
    public static boolean isVisibility(View view){
        return isVisibility(view, true);
    }

    /**
     * 判断View 是否显示
     * @param view
     * @param isDf
     * @return
     */
    public static boolean isVisibility(View view, boolean isDf){
        if (view != null){
            // 判断是否显示
            return (view.getVisibility() == View.VISIBLE);
        }
        // 出现意外返回默认值
        return isDf;
    }
}
