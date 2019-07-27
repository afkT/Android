package com.record.video.bean.item.media;

import com.record.video.bean.MediaInfoBean;

/**
 * detail: 视频信息实体类, 特殊处理
 * Created by Ttt
 */
public class VideoInfoItem extends MediaInfoBean {

    /**
     * 生成视频信息数据模型类
     * @param mediaUri
     * @param isPortrait
     * @param isFrontCamera
     * @param cameraRotate
     * @return
     */
    public static VideoInfoItem build(String mediaUri, boolean isPortrait, boolean isFrontCamera, int cameraRotate){
        VideoInfoItem videoInfoItem = new VideoInfoItem();
        // ==
        videoInfoItem.mediaTypeEnum = MediaTypeEnum.VIDEO;
        videoInfoItem.setMediaUri(mediaUri);
        videoInfoItem.setPortrait(isPortrait);
        videoInfoItem.setFrontCamera(isFrontCamera);
        videoInfoItem.setCameraRotate(cameraRotate);
        return videoInfoItem;
    }

    @Override
    public String toString() {
        // 进行拼接
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\n文件名字:");
        stringBuffer.append(getMediaName());
        stringBuffer.append("\n文件类型");
        stringBuffer.append(getMediaSuffix());
        stringBuffer.append("\n文件处理MD5");
        stringBuffer.append(getMediaMD5());
        stringBuffer.append("\n文件信息");
        stringBuffer.append("宽(" + getMediaWidth() + "), 高(" + getMediaHeight() + "), 时长(" + getMediaTime() + "), 文件大小(" + getMediaSize() + ")");
        stringBuffer.append("\n文件地址");
        stringBuffer.append(getMediaUri());
        // 返回字符串
        return stringBuffer.toString();
    }
}
