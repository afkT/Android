package com.record.video.bean.item.media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.record.video.bean.MediaInfoBean;

/**
 * detail: 图片信息实体类, 特殊处理
 * Created by Ttt
 */
public class ImageInfoItem extends MediaInfoBean {

    /**
     * 生成图片信息数据模型类
     * @param mediaUri
     * @param isPortrait
     * @param isFrontCamera
     * @param cameraRotate
     * @return
     */
    public static ImageInfoItem build(String mediaUri, boolean isPortrait, boolean isFrontCamera, int cameraRotate){
        ImageInfoItem imageInfoItem = new ImageInfoItem();
        // ==
        imageInfoItem.mediaTypeEnum = MediaTypeEnum.IMAGE;
        imageInfoItem.setMediaUri(mediaUri);
        imageInfoItem.setPortrait(isPortrait);
        imageInfoItem.setFrontCamera(isFrontCamera);
        imageInfoItem.setCameraRotate(cameraRotate);
        // 获取图片宽度高度
        int[] whArys = getImageWidthHeight(mediaUri);
        // 设置宽高
        imageInfoItem.setMediaWidth(whArys[0]);
        imageInfoItem.setMediaHeight(whArys[1]);
        return imageInfoItem;
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
        stringBuffer.append("宽(" + getMediaWidth() + "), 高(" + getMediaHeight() + "), 文件大小(" + getMediaSize() + ")");
        stringBuffer.append("\n文件地址");
        stringBuffer.append(getMediaUri());
        // 返回字符串
        return stringBuffer.toString();
    }

    /**
     * 获取图片宽度高度
     * @param path
     * @return
     */
    public static int[] getImageWidthHeight(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 不解析图片信息
        options.inJustDecodeBounds = true;
        // 此时返回的bitmap为null
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        // options.outHeight为原始图片的高
        return new int[]{options.outWidth,options.outHeight};
    }
}
