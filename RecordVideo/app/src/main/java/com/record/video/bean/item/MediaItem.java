package com.record.video.bean.item;

import android.text.TextUtils;

import java.io.File;

/**
 * detail: 多媒体Item, 单独特殊处理
 * Created by Ttt
 */
public class MediaItem extends AbsMediaInfoItem {

    /** 多媒体类型 */
    public enum MediaTypeEnum {

        UNKNOWN(0),

        // 视频
        VIDEO(1),

        // 图片
        IMAGE(2),

        // 音频
        AUDIO(3);

        private int val;

        MediaTypeEnum(int val){
            this.val = val;
        }

        /**
         * 获取值
         * @return
         */
        public int getValue(){
            return this.val;
        }

        /**
         * 获取多媒体类型
         * @param val
         * @return
         */
        public static MediaTypeEnum getMediaType(int val){
            if (val == 1){
                return VIDEO;
            } else if (val == 2){
                return IMAGE;
            } else if (val == 3){
                return AUDIO;
            }
            return UNKNOWN;
        }
    }

    // ==

    // 多媒体类型
    protected MediaTypeEnum mediaTypeEnum;

    /**
     * 获取多媒体类型
     * @return
     */
    public MediaTypeEnum getMediaTypeEnum() {
        return mediaTypeEnum;
    }

    // ==

    /**
     * 判断是否网络资源
     * @return
     */
    public boolean isHttpRes(){
        return isHttpRes(getMediaUri());
    }

    /**
     * 判断是否网络资源
     * @param uri
     * @return
     */
    public static boolean isHttpRes(String uri){
        if (!TextUtils.isEmpty(uri)){
            // 属于第一位开始, 才是属于网络视频
            if (uri.toLowerCase().startsWith("http")){
                return true;
            }
        }
        return false;
    }

    // ==

    /**
     * 判断是否本地资源
     * @return
     */
    public boolean isLocalRes(){
        return isLocalRes(getMediaUri());
    }

    /**
     * 判断是否本地资源
     * @param uri
     * @return
     */
    public static boolean isLocalRes(String uri){
        if (!TextUtils.isEmpty(uri)){
            return new File(uri).exists();
        }
        return false;
    }


}
