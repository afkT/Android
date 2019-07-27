package com.record.video.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

/**
 * detail: 图片旋转
 * Created by Ttt
 */
public class RotateTransformation extends BitmapTransformation {

    // 获取拍摄角度
    int cameraRotate;
    // 判断是否竖屏
    boolean isPortrait;
    // 是否前置摄像头
    boolean isFrontCamera;

    public RotateTransformation(Context context, int cameraRotate, boolean isPortrait, boolean isFrontCamera) {
        super(context);
        this.cameraRotate = cameraRotate;
        this.isPortrait = isPortrait;
        this.isFrontCamera = isFrontCamera;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        // 进行图片旋转
        Matrix matrix = new Matrix();
        // 判断是否需要
        if (isFrontCamera && isPortrait){
            matrix.preRotate(Math.abs(360 - cameraRotate));
        } else {
            matrix.preRotate(cameraRotate);
        }
        // 旋转处理图片
        return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true);
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {

    }
}
