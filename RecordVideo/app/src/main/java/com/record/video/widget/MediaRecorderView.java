package com.record.video.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;

import com.record.video.R;
import com.record.video.config.constants.NotifyConstants;
import com.record.video.config.constants.ProConstants;
import com.record.video.utils.CameraUtils;
import com.record.video.utils.DevUtils;
import com.record.video.utils.TimerUtils;
import com.record.video.utils.ToastUtils;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import dev.logger.DevLogger;

/**
 * detail: 多媒体录制View
 * Created by Ttt
 */
public class MediaRecorderView extends LinearLayout implements MediaRecorder.OnErrorListener {

    // 日志TAG
    private static final String TAG = MediaRecorderView.class.getSimpleName();
    // 摄像头对象
    private Camera mCamera;
    // 摄像头预览画面
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    // 录制对象
    private MediaRecorder mMediaRecorder;
    // 判断是否竖屏
    private boolean isPortrait = true;
    // 是否一开始就打开摄像头
    private boolean isOpenCamera = true;
    // 是否前置摄像头 = true = 前置, false = 后置
    private boolean isFrontCamera = false;
    // 判断是否支持对焦模式
    private static final HashMap<Boolean, Integer> mHashAutoFocus = new HashMap<>();

    // ==

    public MediaRecorderView(Context context) {
        this(context, null);
    }

    public MediaRecorderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public MediaRecorderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // == 初始化View ==
        LayoutInflater.from(context).inflate(R.layout.inflate_media_recorder, this);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new MediaRecorderView.CustomCallBack());
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /** SurfaceHolder回调 */
    private class CustomCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (!isOpenCamera)
                return;
            try {
                resetCamera();
            } catch (Exception e) {
                DevLogger.eTag(TAG, e, "CustomCallBack - surfaceCreated");
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (!isOpenCamera)
                return;
            // 销毁资源
            CameraUtils.freeCameraResource(mCamera);
        }
    }

    // == 摄像头处理 ==

    /** 反转摄像头 */
    public void reverseCamera(){
        // 进行反转
        isFrontCamera = !isFrontCamera;
        // 初始化摄像头操作
        initCameraOperate();
    }

    /** 重置摄像头 */
    public void resetCamera(){
        // 初始化摄像头操作
        initCameraOperate();
    }

    /** 停止录制并释放资源操作 */
    private void stopRelease() {
        // 释放录制资源
        releaseRecord();
        // 销毁资源
        CameraUtils.freeCameraResource(mCamera);
    }

    /** 初始化摄像头操作 */
    private void initCameraOperate(){
        // 开启横竖屏监听
        toggleOrientationListener(true);
        // 停止录制并释放资源操作
        stopRelease();
        try {
            // 重新初始化 Camera
            initCamera();
        } catch (Exception e) {
            DevLogger.eTag(TAG, e, "initCameraOperate");
            // 进行判断是否支持自动对焦
            Integer status = mHashAutoFocus.get(isFrontCamera);
            // 如果等于null, 才进行处理
            if (status != null && status == 1){
                mHashAutoFocus.put(isFrontCamera, 0);
                // 重新初始化
                initCameraOperate();
            } else {
                ToastUtils.showToast(getContext(), "初始化摄像头失败!");
            }
        }
    }

    /** 初始化摄像头 */
    private void initCamera() throws Exception {
        // 初始化摄像头
        mCamera = CameraUtils.initCamera(mCamera, isFrontCamera);
        // 判断使用哪个摄像头
        int cameraFacing = CameraUtils.isUseCameraFacing(isFrontCamera);
        // 重新判断摄像头
        isFrontCamera = CameraUtils.isFrontCamera(cameraFacing);
        // 判断摄像头是否为null
        if (mCamera == null)
            throw new Exception("initCamera Error");

        // 设置摄像头参数
        setCameraParams();
        // 判断是否前置摄像头
        if (isFrontCamera) {
            // 根据摄像头计算角度
            frontCameraRotate();
            // 设置摄像头角度
            mCamera.setDisplayOrientation(mFrontRotate);
        } else {
            // 后置摄像头
            mCamera.setDisplayOrientation(90);
        }
        // 设置预览View, 并开始预览显示
        mCamera.setPreviewDisplay(mSurfaceHolder);
        mCamera.startPreview();
        mCamera.cancelAutoFocus();
        // 如果属于录制才设置
        if (this.isRecord) {
            this.isRecord = false;
            // 导致无法拍照
            mCamera.unlock();
        }
    }

    // == 设置摄像头参数 ==

    // 拍照图片尺寸
    Camera.Size mPictureSize = null;
    // 预览尺寸大小
    Camera.Size mPreviewSize = null;
    // 视频尺寸大小
    Camera.Size mVideoSize = null;

    /** 设置摄像头参数 */
    private void setCameraParams() {
        if (mCamera != null) {
            // 获取屏幕宽、高
            int sWidth = DevUtils.getScreenWidth(getContext());
            int sHeight = DevUtils.getScreenHeight(getContext());
            // 设置摄像头参数
            Camera.Parameters params = mCamera.getParameters();
            // 设置摄像头为竖屏
            params.set("orientation", "portrait");
            // 判断是否处理拍照大小成功
            if (mPictureSize == null){
                // 获取摄像头支持的尺寸
                List<Camera.Size> listSupportedPictureSizes = params.getSupportedPictureSizes();
                // 遍历判断最符合手机屏幕的尺寸
                for (Camera.Size size : listSupportedPictureSizes) {
                    // 打印支持的尺寸
                    DevLogger.dTag(TAG, "PictureSizes - 宽度: " + size.width + ", 高度: " + size.height);
                    // 因为是竖屏, 所以判断需要倒着过来
                    if (sWidth == size.height && sHeight == size.width){
                        // 保存符合比例的大小
                        mPictureSize = size;
                        break;
                    }
                    // 计算合适的比例
                    if (size.height >= sHeight){
                        mPictureSize = size;
                    }
                }
            }
            // 判断是否录制操作
            if (isRecord) {
                // 设置录制预览、视频大小等
                setConfigSize(params);
            } else {
                // 获取最合适的比例
                DevLogger.dTag(TAG, "setPictureSize -> 宽度: " + mPictureSize.width + ", 高度: " + mPictureSize.height + ", 是否竖屏: " + isPortrait);
                // 设置拍照图片大小
                params.setPictureSize(mPictureSize.width, mPictureSize.height);
                // 设置拍照输出格式
                params.setPictureFormat(PixelFormat.JPEG);
                // 照片质量
                params.set("jpeg-quality", 70);
            }
            // 判断是否支持自动对焦
            if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
                // 获取摄像头对焦状态
                Integer status = mHashAutoFocus.get(isFrontCamera);
                // 判断是否允许对焦
                if (status == null || (status == 1)) {
                    mHashAutoFocus.put(isFrontCamera, 1);
                    // 设置对焦模式
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
            }
            // 设置摄像头的参数.否则前面的设置无效
            mCamera.setParameters(params);
        }
    }

    /**
     * 设置配置大小(不同比例控制不同)
     * @param params
     */
    private void setConfigSize(Camera.Parameters params){
        // 计算预览大小
        setPreviewSize(params);
        // 计算录制大小
        setVideoSize(params);
    }

    /**
     * 根据手机支持的视频分辨率，设置预览尺寸
     * @param params
     */
    private void setPreviewSize(Camera.Parameters params) {
        if (mCamera != null) {
            try {
                // 判断是否存在预览大小尺寸
                if (mPreviewSize == null) {
                    // 获取屏幕宽、高
                    int sWidth = DevUtils.getScreenWidth(getContext());
                    int sHeight = DevUtils.getScreenHeight(getContext());
                    // 获取手机支持的分辨率集合,并以宽度为基准降序排序
                    List<Camera.Size> listPreviewSizes = params.getSupportedPreviewSizes();
                    // 进行排序处理
                    Collections.sort(listPreviewSizes, new Comparator<Camera.Size>() {
                        @Override
                        public int compare(Camera.Size lhs, Camera.Size rhs) {
                            if (lhs.width > rhs.width) {
                                return -1;
                            } else if (lhs.width == rhs.width) {
                                return 0;
                            } else {
                                return 1;
                            }
                        }
                    });
                    // 遍历预览大小
                    for (Camera.Size size : listPreviewSizes) {
                        // 打印支持的尺寸
                        DevLogger.dTag(TAG, "PreviewSizes - 宽度: " + size.width + ", 高度: " + size.height);
                        // 因为是竖屏, 所以判断需要倒着过来
                        if (sWidth == size.height && sHeight == size.width){
                            // 保存符合比例的大小
                            mPreviewSize = size;
                            break;
                        }
                        // 计算合适的比例
                        if (size.height >= sHeight){
                            mPreviewSize = size;
                        }
                    }
                }
                // 获取最合适的比例
                DevLogger.dTag(TAG, "setPreviewSize -> 宽度: " + mPreviewSize.width + ", 高度: " + mPreviewSize.height + ", 是否竖屏: " + isPortrait);
                // 预览比率 - 暂时不设置（预览会扭曲）
                params.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            } catch (Exception e){
                DevLogger.eTag(TAG, e, "setPreviewSize - 是否竖屏: " + isPortrait);
            }
        }
    }

    /**
     * 根据手机支持的视频分辨率，设置录制尺寸
     * @param params
     */
    private void setVideoSize(Camera.Parameters params) {
        if (mCamera != null) {
            // 判断是否存在视频大小尺寸
            if (mVideoSize == null) {
                try {
                    // 小数点处理, 只要后两位
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    decimalFormat.setRoundingMode(RoundingMode.FLOOR);
                    // 获取屏幕宽、高
                    int sWidth = DevUtils.getScreenWidth(getContext());
                    int sHeight = DevUtils.getScreenHeight(getContext());
                    // 获取手机支持的分辨率集合,并以宽度为基准降序排序
                    List<Camera.Size> listVideoSizes = params.getSupportedVideoSizes();
                    // 获取支持录制的视频大小
                    Collections.sort(listVideoSizes, new Comparator<Camera.Size>() {
                        @Override
                        public int compare(Camera.Size lhs, Camera.Size rhs) {
                            if (lhs.width > rhs.width) {
                                return -1;
                            } else if (lhs.width == rhs.width) {
                                return 0;
                            } else {
                                return 1;
                            }
                        }
                    });
                    // 默认是否支持固定的大小
                    Camera.Size fixedSize = null;
                    // 计算比例(以高度为基准, 高:宽)
                    float ratio = ((float) sHeight / (float) sWidth) - 1;
                    // 转换保留两位小数点
                    ratio = Float.parseFloat(decimalFormat.format(ratio));
                    // 遍历预览大小
                    for (Camera.Size size : listVideoSizes) {
                        // 打印支持的尺寸
                        DevLogger.dTag(TAG, "VideoSizes - 宽度: " + size.width + ", 高度: " + size.height);
                        // 因为是竖屏, 所以判断需要倒着过来
                        if (sWidth == size.height && sHeight == size.width){
                            // 保存符合比例的大小
                            mVideoSize = size;
                        }
                        // 处理宽大于高的, 因为是使用竖屏, 参数判断都反着处理
                        if (size.width > size.height){
                            // 获取比例
                            float ratioCalc = ((float) size.width / (float) size.height) - 1;
                            // 转换保留两位小数点
                            ratioCalc = Float.parseFloat(decimalFormat.format(ratioCalc));
                            // 判断符合规则的
                            if (ratioCalc == ratio){
                                // 保存尺寸
                                fixedSize = size;
                                // 判断是否支持固定的大小
                                if (size.width == 640 && size.height == 480) {
                                    break;
                                } else if (size.width == 640 && size.height == 360) {
                                    break;
                                }
                            } else {
                                // 最小支持到640
                                if (size.width < 640){
                                    break;
                                }
                                // 保存尺寸
                                fixedSize = size;
                                // 判断是否支持固定的大小
                                if (size.width == 640 && size.height == 480) {
                                    break;
                                } else if (size.width == 640 && size.height == 360) {
                                    break;
                                }
                            }
                        }
                    }
                    // 如果支持固定的大小, 则进行处理
                    if (fixedSize != null) {
                        // 保存固定支持的尺寸
                        mVideoSize = fixedSize;
                    }
                } catch (Exception e) {
                    DevLogger.eTag(TAG, e, "setVideoSize, 是否竖屏: " + isPortrait);
                    // 清空视频大小
                    mVideoSize = null;
                }
            }
        }
    }

    // == 角度相关 ==
    // 摄像头旋转角度
    private int mFrontRotate;
    // 摄像头方向
    private int mFrontOri;
    // 录制角度记录值
    private int mRotationFlag = 90;
    // 录制角度旋值
    private int mRotationRecord = 90;

    // 旋转监听
    private OrientationEventListener mOrientationEventListener = new OrientationEventListener(getContext()) {
        @Override
        public void onOrientationChanged(int rotation) {
            // 如果是录制中, 则不处理
            if (!isRecordIng) {
                if (((rotation >= 0) && (rotation <= 30)) || (rotation >= 330)) {
                    isPortrait = true;
                    // DevLogger.dTag("RQWEAWE", "竖屏拍摄");
                    // 竖屏拍摄
                    if (mRotationFlag != 0) {
                        //这是竖屏视频需要的角度
                        mRotationRecord = 90;
                        //这是记录当前角度的flag
                        mRotationFlag = 0;
                    }
                } else if (((rotation >= 230) && (rotation <= 310))) {
                    isPortrait = false;
                    // DevLogger.dTag("RQWEAWE", "横屏拍摄");
                    // 横屏拍摄
                    if (mRotationFlag != 90) {
                        //这是正横屏视频需要的角度
                        mRotationRecord = 0;
                        //这是记录当前角度的flag
                        mRotationFlag = 90;
                    }
                } else if (rotation > 30 && rotation < 135) {
                    isPortrait = false;
                    // DevLogger.dTag("RQWEAWE", "反横屏拍摄");
                    // 反横屏拍摄
                    if (mRotationFlag != 270) {
                        //这是反横屏视频需要的角度
                        mRotationRecord = 180;
                        //这是记录当前角度的flag
                        mRotationFlag = 270;
                    }
                } else if (rotation > 135 && rotation < 230){
                    isPortrait = true;
                    // DevLogger.dTag("RQWEAWE", "反竖屏拍摄");
                    // 竖屏拍摄
                    if (mRotationFlag != 360) {
                        //这是竖屏视频需要的角度
                        mRotationRecord = 270;
                        //这是记录当前角度的flag
                        mRotationFlag = 360;
                    }
                }
            }
        }
    };

    /** 旋转前置摄像头为正的角度计算 */
    private void frontCameraRotate() {
        // 获取前置摄像头角度
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
        // 获取当前屏幕角度
        int degrees = getDisplayRotation(getContext());
        int result;
        // 前置摄像头
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else { // 后置摄像头
            result = (info.orientation - degrees + 360) % 360;
        }
        // 设置对应的镜头旋转角度
        mFrontRotate = result;
        // 设置对应的镜头旋转方向
        mFrontOri = info.orientation;
    }

    /**
     * 获取旋转角度
     * @param mContext
     * @return
     */
    private int getDisplayRotation(Context mContext) {
        try {
            // 获取当前手机角度
            int rotation = ((Activity) mContext).getWindowManager().getDefaultDisplay().getRotation();
            switch (rotation) {
                case Surface.ROTATION_0:
                    return 0;
                case Surface.ROTATION_90:
                    return 90;
                case Surface.ROTATION_180:
                    return 180;
                case Surface.ROTATION_270:
                    return 270;
            }
        } catch (Exception e){
            DevLogger.eTag(TAG, e, "getDisplayRotation");
        }
        return 0;
    }

    /**
     * 切换手机水平角度改变监听
     * @param isStart
     */
    public void toggleOrientationListener(boolean isStart){
        try {
            if (isStart){
                mOrientationEventListener.enable();
            } else {
                mOrientationEventListener.disable();
            }
        } catch (Exception e){
        }
    }

    // == 录制相关 ==

    // 录制时间
    private int mRecordTime = -1;
    // 开始录制时间
    private long mStartRecordTime = -1l;
    // 判断是否录制
    private boolean isRecord = false;
    // 判断是否录制中
    private boolean isRecordIng = false;
    // 判断是否录制定时关闭
    private boolean isRecordTimerClose = false;
    // 判断是否录制异常
    private boolean isStopError = false;
    // 判断是否回调了错误异常
    private boolean isCallBackError = false;

    @Override // 录制异常回调 => MediaRecorder.OnErrorListener
    public void onError(MediaRecorder mr, int what, int extra) {
        try {
            if (mr != null) {
                mr.reset();
            }
            DevLogger.eTag(TAG, "record - onError, what: " + what + ", extra: " + extra + ", mr: " + (mr != null));
        } catch (Exception e) {
            DevLogger.eTag(TAG, e, "record - onError");
        }
    }

    /** 释放录制资源 */
    private void releaseRecord() {
        if (mMediaRecorder != null) {
            // 设置回调为null
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            try {
                // 停止录制
                mMediaRecorder.stop();
                // 标记暂停没有发生异常
                isStopError = false;
            } catch (Exception e) {
                DevLogger.eTag(TAG, e, "releaseRecord stop");
                // 停止出现异常
                isStopError = true;
            }
            // 停止录制时间
            long endRecordTime = System.currentTimeMillis();
            // 打印停止录制时间
            DevLogger.dTag(TAG, "停止录制时间: " + endRecordTime + ", 录制时间差(毫秒): " + (endRecordTime - mStartRecordTime));
            // -
            try {
                // 重置资源
                mMediaRecorder.reset();
            } catch (Exception e){
            }
            try {
                // 释放资源
                mMediaRecorder.release();
            } catch (Exception e){
            }
        }
        mMediaRecorder = null;
    }

    /**
     * 初始化录制对象及参数
     * @throws Exception
     */
    private void initRecord() throws Exception {
        if (mVideoSize == null){
            throw new Exception("mVideoSize 为 null");
        }
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();
        // 设置Camera预览对象
        if (mCamera != null)
            mMediaRecorder.setCamera(mCamera);
        // -
        mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 音频源
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); // 视频源
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // 输出格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); // 音频格式
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264); // 视频录制格式
        mMediaRecorder.setVideoSize(mVideoSize.width, mVideoSize.height); // 设置分辨率
        //mMediaRecorder.setVideoFrameRate(25);// 设置每秒帧数 这个设置有可能会出问题，有的手机不支持这种帧率就会录制失败，这里使用默认的帧率，当然视频的大小肯定会受影响
        // 获取屏幕大小(录制大小) - recordSize
        int screenSize = mVideoSize.width * mVideoSize.height;
        // 大于1080p则设置小店的
        if (screenSize >= 2073600){ // 1080p = 1920 * 1080
            mMediaRecorder.setVideoEncodingBitRate(3 * mVideoSize.width * mVideoSize.height);
        } else { // 小于1080p
            mMediaRecorder.setVideoEncodingBitRate(3 * mVideoSize.width * mVideoSize.height);
        }
        // 如果属于支持的尺寸 如 640*480, 640*360
        if (screenSize <= 30720){
            mMediaRecorder.setVideoEncodingBitRate(5 * mVideoSize.width * mVideoSize.height);
        }
        // 设置录制秒数, 10秒 + 0.4秒硬件延迟时间等,并且进行四舍五入
        mMediaRecorder.setMaxDuration(10 * 1000 + 400);
        // 获取对应的摄像头需要旋转的角度
        int frontRotation;
        // 判断录制角度
        if (mRotationRecord == 180) {
            // 反向的前置
            frontRotation = 180;
        } else {
            // 正向的前置
            frontRotation = (mRotationRecord == 0) ? 270 - mFrontOri : mFrontOri; // 录制下来的视屏选择角度，此处为前置
            // 判断是否前置摄像头
            if (isFrontCamera && mRotationRecord == 270 && mFrontOri == 270){
                // 如果属于前置摄像头， 并且属于倒过来的, 则重置一下角度
                frontRotation = 90;
            }
        }
        // 设置录制的方向
        mMediaRecorder.setOrientationHint(isFrontCamera ? frontRotation : mRotationRecord);
        // 设置录制保存的地址, 并且开始录制
        mMediaRecorder.setOutputFile(mRecordFile.getAbsolutePath());
        mMediaRecorder.prepare();
        mMediaRecorder.start();
        // 保存录制时间
        mStartRecordTime = System.currentTimeMillis();
        // 打印录制信息
        DevLogger.dTag(TAG, "开始录制时间: " + mStartRecordTime);
        DevLogger.eTag(TAG, "录制文件名: " + mRecordFile.getName());
        DevLogger.eTag(TAG, "录制文件地址: " + mRecordFile.getAbsolutePath());
        DevLogger.eTag(TAG, "录制文件 -> 是否竖屏: " + isPortrait);
        DevLogger.eTag(TAG, "录制文件 -> 宽: " + (isPortrait ? mVideoSize.height : mVideoSize.width) + ", 高: " + (isPortrait ? mVideoSize.width : mVideoSize.height) );
    }

    /**
     * 停止录制
     * @param isAchieve 是否符合条件
     */
    public void stopRecord(boolean isAchieve){
        // 关闭全部定时器
        closeTimers();
        // 通知开始结束
        if (recordCallback != null) {
            recordCallback.onStopRecordNotify(isAchieve);
        }
        // 判断是否符合条件
        if (isAchieve) {
            // 开启定时器
            loadCheckTimer(true);
        }
        // 结束录制
        stopRelease();
        // 表示不属于录制
        this.isRecord = false;
        // 表示不属于录制中
        this.isRecordIng = false;
        // 重置摄像头
        //resetCamera(); // 不能立刻重置，会导致无法保存视频
        // 停止录制
        DevLogger.dTag(TAG, "停止录制");
    }

    /** 开始录制视频 */
    public void record() {
        // 关闭横竖屏监听
        toggleOrientationListener(false);
        // 表示非触发异常
        this.isCallBackError = false;
        // 表示不属于拍照
        this.isTakePic = false;
        // 关闭全部定时器
        closeTimers();
        // 如果录制中则不处理
        if (this.isRecordIng){
            return;
        }
        // 通知开始录制
        if (recordCallback != null){
            recordCallback.onStartRecord();
        }
        // 重置录制时间
        mRecordTime = 0;
        // 开启录制定时器
        loadRecordTimer(true);
        // --
        DevLogger.dTag(TAG, "开始录制");
        this.isRecord = true;
        this.isRecordIng = true;
        this.isRecordTimerClose = false;
        // 返回拼接后的路径
        String nRecordPath = getFilePath(".mp4");
        // 初始化录制文件
        mRecordFile = new File(nRecordPath);
        try {
            // 重新初始化摄像头
            initCamera();
            // 开始录制
            initRecord();
        } catch (Exception e) {
            DevLogger.eTag(TAG, e, "record");
            // --
            if (mMediaRecorder != null) {
                mMediaRecorder.release();
            }
            stopRelease();
            // 关闭全部定时器
            closeTimers();
            // 表示触发了错误回调
            isCallBackError = true;
            // 进行通知
            if (recordCallback != null){
                recordCallback.onRecordFailure(e);
            }
        }
    }

    // == 拍照相关 ==

    // 是否拍照图片
    private boolean isTakePic = false;

    /** 拍照成功回调 */
    private class PicCallback implements Camera.PictureCallback {

        Camera mCamera;

        public PicCallback(Camera camera) {
            mCamera = camera;
        }

        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            try {
                // 返回拼接后的路径
                final String nImagePath = getFilePath("CAMERA_PIC", ".jpg");
                // 如果文件存在则删除
                File file = new File(nImagePath);
                // 先删除旧的文件
                if (file.exists()){
                    file.delete();
                }
                // 进行后台保存
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        // == 第一种保存图片处理 ==
//                        // byte数据转换图片, 并保存
//                        Bitmap picBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//                        // 保存图片
//                        DevUtils.saveBitmap(nImagePath, picBitmap, Bitmap.CompressFormat.JPEG, 80);

//                        // == 第二种保存图片处理 ==
//                        // 保存图片
//                        //DevUtils.saveFile(data, getRootPath(), "CAMERA_PIC.jpg");

                        // == 第三种保存图片处理 ==
                        // byte数据转换图片, 并保存
                        Bitmap picBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        // 保存图片
                        DevUtils.saveBitmap(nImagePath, picBitmap, Bitmap.CompressFormat.JPEG, 70);
//                        // 加载图片
//                        picBitmap = BitmapFactory.decodeFile(nImagePath);
//                        // 进行图片旋转
//                        Matrix matrix = new Matrix();
//                        // 判断是否需要
//                        if (isFrontCamera && isPortrait){
//                            matrix.preRotate(Math.abs(360 - getRotationRecord()));
//                        } else {
//                            matrix.preRotate(getRotationRecord());
//                        }
//                        // 旋转处理图片
//                        picBitmap = Bitmap.createBitmap(picBitmap, 0, 0, picBitmap.getWidth(), picBitmap.getHeight(), matrix, true);
//                        // 保存图片
//                        DevUtils.saveBitmap(nImagePath, picBitmap, Bitmap.CompressFormat.JPEG, 70);
                        // 保存成功进行通知
                        if (recordCallback != null){
                            picBitmap = null;
                            // 表示还没通知
                            isCheckFileNotify = false;
                            // -
                            recordCallback.onTakeDealSuc(picBitmap);
                        }
                    }
                }).start();
                // 触发回调
                if (recordCallback != null) {
                    // 直接显示结果, 不等待验证等
                    recordCallback.onTakeNotify();
                }
                // 关闭全部定时器
                closeTimers();
                // 开启定时器 - 使用第三种方法, 则不处理
                //loadCheckTimer(true);
            } catch (Exception e){
                DevLogger.eTag(TAG, e, "onPictureTaken");
                // 通知异常
                if (recordCallback != null) {
                    recordCallback.onTakeFailure(e);
                }
            }
        }
    }

    /** 进行拍照保存 */
    public void takeFrontPhoto() {
        try {
            // 关闭横竖屏监听
            toggleOrientationListener(false);
            // 表示属于拍照
            this.isTakePic = true;
            // 拍照
            mCamera.takePicture(null, null, new PicCallback(mCamera));
        } catch (Exception e) {
            DevLogger.eTag(TAG, e, "takePicture");
            // 通知异常
            if (recordCallback != null) {
                recordCallback.onTakeFailure(e);
            }
        }
    }

    // == 定时器相关处理 ==

    // 手势定时器 - 按压时间判断
    private TimerUtils mEventTimer;
    // 录制定时器 - 录制时间累加
    private TimerUtils mRecordTimer;
    // 检查定时器 - 检查拍照文件, 录制视频是否存在
    private TimerUtils mCheckTimer;
    // 检查是否已经通知
    private boolean isCheckFileNotify = false;

    /**
     * 加载手势定时器
     * @param isStart 是否启动
     */
    private void loadEventTimer(boolean isStart){
        if(mEventTimer == null){
            // 初始化加载定时器
            mEventTimer = new TimerUtils();
        }
        if(isStart){
            mEventTimer.setHandler(vHandler);
            mEventTimer.setTriggerLimit(-1);
            mEventTimer.setTime(200, 10);
            mEventTimer.setNotifyWhat(NotifyConstants.NOTIFY_RECORD_START);
            mEventTimer.startTimer();
        } else {
            mEventTimer.closeTimer();
        }
    }

    /**
     * 加载录制定时器
     * @param isStart 是否启动
     */
    private void loadRecordTimer(boolean isStart){
        if(mRecordTimer == null){
            // 初始化加载定时器
            mRecordTimer = new TimerUtils();
        }
        if(isStart){
            mRecordTimer.setHandler(vHandler);
            mRecordTimer.setTriggerLimit(-1);
            mRecordTimer.setTime(0, 100);
            mRecordTimer.setNotifyWhat(NotifyConstants.NOTIFY_RECORD_TIMING);
            mRecordTimer.startTimer();
        } else {
            mRecordTimer.closeTimer();
        }
    }

    /**
     * 加载录制定时器
     * @param isStart 是否启动
     */
    private void loadCheckTimer(boolean isStart){
        if(mCheckTimer == null){
            // 初始化加载定时器
            mCheckTimer = new TimerUtils();
        }
        if(isStart){
            // 关闭全部定时器
            closeTimers();
            // 表示没通知过
            isCheckFileNotify = false;
            // --
            mCheckTimer.setHandler(vHandler);
            mCheckTimer.setTriggerLimit(-1);
            mCheckTimer.setTime(0, 20);
            mCheckTimer.setNotifyWhat(NotifyConstants.NOTIFY_RECORD_CHECK);
            mCheckTimer.startTimer();
        } else {
            mCheckTimer.closeTimer();
        }
    }

    /** 关闭定时器 */
    public void closeTimers(){
        loadEventTimer(false);
        loadCheckTimer(false);
        loadRecordTimer(false);
    }

    /**
     * 判断是否检查文件通知
     * @return
     */
    public boolean isCheckFileNotify() {
        return isCheckFileNotify;
    }

    /** 表示已经通知 */
    public void setCheckFileNotify() {
        isCheckFileNotify = true;
    }

    /** 销毁操作 */
    public void destroy(){
        // 关闭横竖屏监听
        toggleOrientationListener(false);
        // 关闭定时器
        closeTimers();
    }

    // == 地址相关 ==

    // 视频文件
    private File mRecordFile = null;

    /**
     * 返回录像文件
     * @return mRecordFile
     */
    public File getRecordFile() {
        return mRecordFile;
    }

    /**
     * 返回录像文件地址
     * @return mRecordFile.getAbsolutePath();
     */
    public String getRecordPath() {
        if (mRecordFile != null){
            return mRecordFile.getAbsolutePath();
        }
        return null;
    }

    /**
     * 获取拍照图片地址
     * @return
     */
    public String getTakePicPath(){
        // 返回拼接后的路径
        final String nImagePath = getFilePath("CAMERA_PIC", ".jpg");
        // 返回图片路径
        return nImagePath;
    }

    /**
     * 删除拍照图片
     * @param mContext
     */
    public static void deleteCameraPic(Context mContext){
        try {
            // 返回拼接后的路径
            final String nImagePath = DevUtils.getCachePath(mContext, ProConstants.AP_VIDEO_RECORD_PATH) + File.separator + "CAMERA_PIC.jpg";
            // 如果文件存在则删除
            File file = new File(nImagePath);
            if (file.exists()){
                file.delete();
            }
        } catch (Exception e){
        }
    }

    // == 路径统一处理控制 ==

    /**
     * 获取随即文件名
     * @return
     */
    public String getRandomName(){
        return DevUtils.MD5((System.currentTimeMillis() + "" + new Random().nextInt(500000)));
    }

    /**
     * 获取文件路径
     * @param suffix 后缀
     * @return
     */
    private String getFilePath(String suffix){
        return getFilePath(getRandomName(), suffix);
    }

    /**
     * 获取文件路径
     * @param fName 文件名
     * @param suffix 后缀
     * @return
     */
    private String getFilePath(String fName, String suffix){
        return getRootPath() + File.separator + fName + suffix;
    }

    /**
     * 获取根文件路径
     * @return
     */
    private String getRootPath(){
        return DevUtils.getCachePath(getContext(), ProConstants.AP_VIDEO_RECORD_PATH);
    }


    // == 对外Get方法 ==

    /**
     * 判断是否竖屏
     * @return
     */
    public boolean isPortrait() {
        return isPortrait;
    }

    /**
     * 判断是否前置摄像头
     * @return
     */
    public boolean isFrontCamera() {
        return isFrontCamera;
    }

    /**
     * 是否录制视频停止异常
     * @return
     */
    public boolean isStopError() {
        return isStopError;
    }

    /**
     * 判断是否拍照
     * @return
     */
    public boolean isTakePic() {
        return isTakePic;
    }

    /**
     * 获取操作的角度
     * @return
     */
    public int getRotationRecord() {
        return mRotationRecord;
    }

    // ==================
    // == 录制接口通知 ==
    // ==================

    /** 录制回调接口 */
    public interface RecordCallback{

        /** 开始录制 */
        void onStartRecord();

        /**
         * 录制失败
         * @param e
         */
        void onRecordFailure(Exception e);

        /**
         * 录制结束通知
         * @param isAchieve 是否符合条件
         */
        void onStopRecordNotify(boolean isAchieve);

        /**
         * 录制时间回调
         * @param recordTime
         */
        void onRecordTiming(int recordTime);

        /** 拍照通知 */
        void onTakeNotify();

        /**
         * 拍照失败
         * @param e
         */
        void onTakeFailure(Exception e);

        /** 拍照处理成功 */
        void onTakeDealSuc(Bitmap bitmap);

        /**
         * 检查结果通知
         * @param isResult 是否存在
         * @param isTakePic 是否图片
         */
        void onCheckResult(boolean isResult, boolean isTakePic);
    }

    /** 录制回调接口 */
    public RecordCallback recordCallback;

    /**
     * 设置录制接口回调
     * @param recordCallback
     */
    public void setRecordCallback(RecordCallback recordCallback) {
        this.recordCallback = recordCallback;
    }

    // == 手势操作 ==

    // 手势状态
    private int actionStatus = -1;

    /** 手势按下 */
    public void actionDown(){
        // 重置录制时间
        mRecordTime = 0;
        // 表示非录制定时结束
        isRecordTimerClose = false;
        // 保存按下状态
        actionStatus = MotionEvent.ACTION_DOWN;
        // 关闭全部定时器
        closeTimers();
        // 准备开启定时器
        loadEventTimer(true);
    }

    /**
     * 手势抬起
     * @return 是否录制
     */
    public boolean actionUp(){
        // 保存按下状态
        actionStatus = MotionEvent.ACTION_UP;
        // 关闭全部定时器
        closeTimers();
        // 判断是否录制中
        boolean result = isRecordIng;
        // 判断是否录制中
        if (result){
            // 停止录制
            vHandler.sendEmptyMessage(NotifyConstants.NOTIFY_RECORD_END);
        } else {
            // 判断是否录制关闭
            result = isRecordTimerClose;
        }
        return result;
    }

    /**
     * 收拾移动触发
     * @param motionEvent
     */
    public void actionMove(MotionEvent motionEvent){
        calcZoom(motionEvent);
    }

    // ==  滑动缩小处理 ==

    // 之前滑动的位置
    private float oldY = 0;

    /**
     * 获取位置
     * @param event
     * @return
     */
    private float calcLocation(MotionEvent event) {
        // 获取手指数量
        int pointerCount = event.getPointerCount();
        // 判断是否多点触控
        if (pointerCount == 1) {
            // 获取之前滑动的位置
            return event.getY();
        } else {
            try {
                return event.getY(0) - event.getY(1);
            } catch (Exception e){
            }
        }
        return event.getY();
    }

    /**
     * 计算缩放比例
     * @param event
     */
    private void calcZoom(MotionEvent event){
        float newY = calcLocation(event);
        // 不同才处理
        if (oldY != 0f && newY != oldY){
            // 表示向下
            if (newY > 0f){
                if (newY > oldY){
                    handleZoom(false, mCamera);
                } else {
                    handleZoom(true, mCamera);
                }
            } else { // 属于负数, 表示向上滑动
                if (Math.abs(newY) > Math.abs(oldY)){
                    handleZoom(true, mCamera);
                } else {
                    handleZoom(false, mCamera);
                }
            }
        }
        // 保存最后的位置
        oldY = newY;
    }

    /**
     * 处理缩放
     * @param isZoomIn
     * @param camera
     */
    private void handleZoom(boolean isZoomIn, Camera camera) {
        if (camera != null) {
            try {
                Camera.Parameters params = camera.getParameters();
                if (params != null && params.isZoomSupported()) {
                    int maxZoom = params.getMaxZoom();
                    int zoom = params.getZoom();
                    if (isZoomIn && zoom < maxZoom) {
                        zoom++;
                    } else if (zoom > 0) {
                        zoom--;
                    }
                    params.setZoom(zoom);
                    camera.setParameters(params);
                }
            } catch (Exception e){
            }
        }
    }

    // == 内部Handler ==

    /** View 操作Handler */
    private Handler vHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 判断通知类型
            switch (msg.what){
                case NotifyConstants.NOTIFY_RECORD_START: // 开始录制
                    // 表示经历过5次, 第6次就录制
                    if (mEventTimer.getTriggerNumber() > 5){
                        if (!isRecordIng) {
                            // 属于按下状态才处理
                            if (actionStatus == MotionEvent.ACTION_DOWN){
                                record();
                            } else {
                                // 关闭全部定时器
                                closeTimers();
                            }
                        } else {
                            // 关闭全部定时器
                            closeTimers();
                        }
                    }
                    break;
                case NotifyConstants.NOTIFY_RECORD_END: // 停止录制
                    // 触发停止录制
                    DevLogger.dTag(TAG, "Handler - NOTIFY_RECORD_END, 录制时间 mRecordTime: " + mRecordTime);
                    // 判断是否大于指定时间
                    boolean isAchieve = (mRecordTime >= 3500);
                    // 停止录制
                    stopRecord(isAchieve);
                    // 判断录制时间, 小于2秒的则进行提示
                    if (!isAchieve){
                        // 不符合时间, 删除文件并停止录制
                        DevLogger.dTag(TAG, "未到最少录制时间, 删除文件并停止录制");
                        // 重置摄像头
                        resetCamera();
                        try {
                            // 删除旧的录制文件
                            if (getRecordFile() != null) {
                                getRecordFile().delete();
                            }
                        } catch (Exception e){
                        }
                        // 如果已经触发了, 则不触发
                        if (isCallBackError) {
                            isCallBackError = false;
                        } else {
                            ToastUtils.showToast(getContext(), "录制时间过短");
                        }
                    }
                    break;
                case NotifyConstants.NOTIFY_RECORD_TIMING: // 录制时间回调
                    // 触发停止录制
                    DevLogger.dTag(TAG, "Handler - NOTIFY_RECORD_TIMING, 录制时间 mRecordTime: " + mRecordTime);
                    // 表示经历过120次, 第121次就停止录制
                    if (mRecordTimer.getTriggerNumber() > 120){
                        // 关闭全部定时器
                        closeTimers();
                        // 表示属于定时关闭
                        isRecordTimerClose = true;
                        // 停止录制
                        vHandler.sendEmptyMessage(NotifyConstants.NOTIFY_RECORD_END);
                    } else {
                        // 累加0.1秒间隔
                        mRecordTime += 100;
                        // 通知录制时间
                        if (recordCallback != null){
                            recordCallback.onRecordTiming(mRecordTime);
                        }
                    }
                    break;
                case NotifyConstants.NOTIFY_RECORD_CHECK: // 检测录制回调
                    // 表示经历过70次, 第71次就停止录制
                    if (mRecordTimer.getTriggerNumber() >= 350){ // 70 * 100 = 7秒 ,超过7秒就认为失败
                        // 关闭全部定时器
                        closeTimers();
                        try {
                            File checkFile = new File(isTakePic ? getTakePicPath() : getRecordPath());
                            // 进行通知 (如果没有通知过, 才通知)
                            if (!isCheckFileNotify() && recordCallback != null){
                                recordCallback.onCheckResult(checkFile.exists(), isTakePic);
                            }
                        } catch (Exception e){
                            // 进行通知 (如果没有通知过, 才通知)
                            if (!isCheckFileNotify() && recordCallback != null){
                                recordCallback.onCheckResult(false, isTakePic);
                            }
                        }
                    } else {
                        try {
                            File checkFile = new File(isTakePic ? getTakePicPath() : getRecordPath());
                            // 判断文件是否存在
                            if (checkFile.exists()){
                                // 关闭全部定时器
                                closeTimers();
                                // 进行通知 (如果没有通知过, 才通知)
                                if (!isCheckFileNotify() && recordCallback != null){
                                    recordCallback.onCheckResult(true, isTakePic);
                                }
                            }
                        } catch (Exception e){
                        }
                    }
                    break;
            }
        }
    };
}