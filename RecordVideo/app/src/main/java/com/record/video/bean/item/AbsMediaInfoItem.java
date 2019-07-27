package com.record.video.bean.item;

import android.text.TextUtils;

import com.record.video.utils.DevUtils;

/**
 * detail: 多媒体信息 - 抽象数据模型类
 * Created by Ttt
 */
abstract class AbsMediaInfoItem {

    // 多媒体文件 - 名字(前缀.后缀)
    private String mediaName;
    // 多媒体文件 - 前缀
    private String mediaPrefix;
    // 多媒体文件 - 后缀(无.)
    private String mediaSuffix;
    // 多媒体文件 - 地址
    private String mediaUri;
    // 多媒体文件 - MD5(文件名).后缀
    private String mediaMD5;
    // 多媒体文件 - 大小
    private long mediaSize;

    // = 特殊 =
    // 多媒体文件 - 宽度(视频、图片等)
    private int mediaWidth;
    // 多媒体文件 - 高度(视频、图片等)
    private int mediaHeight;
    // 多媒体文件 - 时长(视频、音频等)
    private int mediaTime;
    // 多媒体文件 - 缩略图地址(视频第一帧、音频封面、图片缩略图等)
    private String mediaCover;
    // 多媒体文件 - 是否竖屏(视频、图片)
    private boolean isPortrait;
    // 多媒体文件 - 是否前置摄像头(视频、图片)
    private boolean isFrontCamera;
    // 多媒体文件 - 拍摄角度(视频、图片)
    private int cameraRotate;

    // ==

    /**
     * 获取多媒体文件 - 后缀(包含.)
     * @return
     */
    public String getMediaSuffixPint(){
        return "." + mediaSuffix;
    }

    // ==


    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

    public String getMediaPrefix() {
        return mediaPrefix;
    }

    public void setMediaPrefix(String mediaPrefix) {
        this.mediaPrefix = mediaPrefix;
    }

    public String getMediaSuffix() {
        return mediaSuffix;
    }

    public void setMediaSuffix(String mediaSuffix) {
        this.mediaSuffix = mediaSuffix;
    }

    public String getMediaUri() {
        return mediaUri;
    }

    public void setMediaUri(String mediaUri) {
        this.mediaUri = mediaUri;
    }

    public String getMediaMD5() {
        if (!TextUtils.isEmpty(mediaMD5)){
            return mediaMD5;
        }
        mediaMD5 = DevUtils.MD5(mediaPrefix + "") + getMediaSuffixPint();
        // 返回文件名
        return mediaMD5;
    }

    public void setMediaMD5(String mediaMD5) {
        this.mediaMD5 = mediaMD5;
    }

    public long getMediaSize() {
        return mediaSize;
    }

    public void setMediaSize(long mediaSize) {
        this.mediaSize = mediaSize;
    }

    public int getMediaWidth() {
        return mediaWidth;
    }

    public void setMediaWidth(int mediaWidth) {
        this.mediaWidth = mediaWidth;
    }

    public int getMediaHeight() {
        return mediaHeight;
    }

    public void setMediaHeight(int mediaHeight) {
        this.mediaHeight = mediaHeight;
    }

    public int getMediaTime() {
        return mediaTime;
    }

    public void setMediaTime(int mediaTime) {
        this.mediaTime = mediaTime;
    }

    public String getMediaCover() {
        return mediaCover;
    }

    public void setMediaCover(String mediaCover) {
        this.mediaCover = mediaCover;
    }

    public boolean isPortrait() {
        return isPortrait;
    }

    public void setPortrait(boolean portrait) {
        isPortrait = portrait;
    }

    public boolean isFrontCamera() {
        return isFrontCamera;
    }

    public void setFrontCamera(boolean frontCamera) {
        isFrontCamera = frontCamera;
    }

    public int getCameraRotate() {
        return cameraRotate;
    }

    public void setCameraRotate(int cameraRotate) {
        this.cameraRotate = cameraRotate;
    }
}
