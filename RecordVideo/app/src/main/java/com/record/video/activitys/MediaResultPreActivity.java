package com.record.video.activitys;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.record.video.R;
import com.record.video.bean.item.MediaItem;
import com.record.video.config.constants.KeyConstants;
import com.record.video.config.constants.NotifyConstants;
import com.record.video.utils.DevUtils;
import com.record.video.utils.RotateTransformation;
import com.record.video.utils.ToastUtils;
import com.record.video.utils.player.RecordMediaManager;
import com.record.video.utils.player.RecordPlayerControl;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.logger.DevLogger;

/**
 * detail: 多媒体处理结果预览 Activity
 * Created by Ttt
 */
public class MediaResultPreActivity extends Activity implements View.OnClickListener{

    // 上下文
    Context mContext;
    // 日志TAG
    final String TAG = MediaResultPreActivity.class.getSimpleName();
    // ==== Obj ====
    // 获取路径
    String mediaUri;
    // 获取类型
    int mediaType;
    // 获取拍摄角度
    int cameraRotate;
    // 判断是否竖屏
    boolean isPortrait;
    // 是否前置摄像头
    boolean isFrontCamera;
    // 播放控制器
    RecordPlayerControl videoPlayerControl;
    // ==== View ====
    @BindView(R.id.amrp_surfaceview)
    SurfaceView amrp_surfaceview;
    @BindView(R.id.amrp_takepic_igview)
    ImageView amrp_takepic_igview;
    @BindView(R.id.amrp_return_igview)
    ImageView amrp_return_igview;
    @BindView(R.id.amrp_confirm_igview)
    ImageView amrp_confirm_igview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        //设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 解决SurfaceView 闪烁问题
        this.getWindow().setFormat(PixelFormat.TRANSLUCENT);
        // ==
        setContentView(R.layout.activity_media_result_pre);
        // 初始化View
        ButterKnife.bind(this);
        // 统一设置跳转不需要动画效果
        this.overridePendingTransition(R.anim.noanim_left_in, R.anim.noanim_left_out);
        // 获取传递参数
        Intent intent = getIntent();
        if (intent != null){
            // 获取路径
            mediaUri = intent.getStringExtra(KeyConstants.EXTRA_MEDIA_URI);
            // 获取类型
            mediaType = intent.getIntExtra(KeyConstants.EXTRA_MEDIA_TYPE, MediaItem.MediaTypeEnum.UNKNOWN.getValue());
            // 获取拍摄角度
            cameraRotate = intent.getIntExtra(KeyConstants.EXTRA_MEDIA_ROTATE, -1);
            // 判断是否竖屏
            isPortrait = intent.getBooleanExtra(KeyConstants.EXTRA_VIDEO_ISPORTRAIT, false);
            // 是否前置摄像头
            isFrontCamera = intent.getBooleanExtra(KeyConstants.EXTRA_VIDEO_ISFRONTCAMERA, true);
        }
        // 判断文件是否存在
        if (!isCheckFile()){
            return;
        }
        // --
        initValues();
        initListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 切换播放状态
        togglePlay(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 切换停止状态
        togglePlay(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.amrp_return_igview: // 拍照完成返回 - 取消
//                // 表示取消
//                setResult(Activity.RESULT_CANCELED, null);
                finish();
                this.overridePendingTransition(R.anim.noanim_left_in, R.anim.noanim_left_out);
                break;
            case R.id.amrp_confirm_igview: // 确认
                // 判断文件是否存在
                if (!isCheckFile()){
                    return;
                }
                // 回传到录制页面
                Intent intent = new Intent();
                intent.putExtra(KeyConstants.EXTRA_VIDEO_ISPORTRAIT, isPortrait); // 是否横竖屏
                intent.putExtra(KeyConstants.EXTRA_VIDEO_ISFRONTCAMERA, isFrontCamera); // 是否前置摄像头
                intent.putExtra(KeyConstants.EXTRA_MEDIA_ROTATE, cameraRotate); // 获取操作角度
                intent.putExtra(KeyConstants.EXTRA_MEDIA_URI, mediaUri); // 资源地址
                intent.putExtra(KeyConstants.EXTRA_MEDIA_TYPE, mediaType); // 资源类型
                setResult(Activity.RESULT_OK, intent);
                finish();
                this.overridePendingTransition(R.anim.noanim_left_in, R.anim.noanim_left_out);
                break;
        }
    }

    public void initValues() {
        // 开始动画
        startTranslateAnimation(amrp_return_igview, true);
        startTranslateAnimation(amrp_confirm_igview, false);
        // 属于图片类型
        if (mediaType == MediaItem.MediaTypeEnum.IMAGE.getValue()){
            // 加载图片
            loadTakeImage();
        } else {
            // 初始化播放控制器
            videoPlayerControl = new RecordPlayerControl(this, amrp_surfaceview);
            videoPlayerControl.setMediaListener(new RecordMediaManager.MediaListener() {
                @Override
                public void onPrepared() {}
                @Override
                public void onCompletion() {}
                @Override
                public void onBufferingUpdate(int percent) {}
                @Override
                public void onSeekComplete() {}
                @Override
                public void onError(int what, int extra) {}
                @Override
                public void onVideoSizeChanged(int width, int height) {
                    try {
                        // 获取屏幕尺寸
                        int[] whArys = DevUtils.getScreenWidthHeight(mContext);
                        // 设置视频宽度高度
                        int vWidth = width;
                        int vHeight = height;
                        // 获取这个图片的宽和高
                        if(vWidth != -1 && vHeight != -1){
                            // 计算比例
                            if (vWidth >= vHeight){ // 如果宽度大于高度
                                // 获取宽度比例
                                float wScale = (float) whArys[0] / (float) vWidth;
                                // 转换高度
                                float vHeightF = wScale * (float) vHeight;
                                // 设置对应的比例信息
                                ViewGroup.LayoutParams layoutParams = amrp_surfaceview.getLayoutParams();
                                layoutParams.width = whArys[0];
                                layoutParams.height = (int) vHeightF;
                                amrp_surfaceview.setLayoutParams(layoutParams);
                            } else { // 高度大于宽度
                                // 获取高度比例
                                float hScale = (float) whArys[1] / (float) vHeight;
                                // 转换宽度
                                float vWidthF = hScale * (float) vWidth;
                                // 设置对应的比例信息
                                ViewGroup.LayoutParams layoutParams = amrp_surfaceview.getLayoutParams();
                                layoutParams.width = (int) vWidthF;
                                layoutParams.height = whArys[1];
                                amrp_surfaceview.setLayoutParams(layoutParams);
                            }
                        }
                    } catch (Exception e){
                        DevLogger.eTag(TAG, e, "RecordPlayerControl - onVideoSizeChanged");
                    }
                }
            });
        }
    }

    public void initListeners() {
        amrp_return_igview.setOnClickListener(this);
        amrp_confirm_igview.setOnClickListener(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 过滤按键动作
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            // 关闭两个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // =

    /** View 操作Handler */
    Handler vHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 如果页面已经关闭,则不进行处理
            if (DevUtils.isFinishingCtx(mContext)) {
                return;
            }
            // 判断通知类型
            switch (msg.what) {
                case NotifyConstants.NOTIFY_CHECK_PERMISSION: // 检查权限
                    break;
            }
        }
    };

    // == 动画效果 ==

    /**
     * 开始位移动画
     * @param view 需要做动画的View
     * @param isLeft 动画的方向
     */
    public void startTranslateAnimation(View view, boolean isLeft) {
        int val = - 200;
        // 设置动画方向
        int toXVal = isLeft ? val : Math.abs(val);
        // -
        float curTranslationX = view.getTranslationX();
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", curTranslationX, toXVal, toXVal);
        animator.setDuration(700);
        animator.start();
    }

    // == 内部逻辑判断 ==

    private boolean isCheckFile(){
        // 判断文件是否存在
        boolean isExist = new File(mediaUri + "").exists();
        // 如果不存在就进行提示
        if (!isExist){
            // 进行提示
            ToastUtils.showToast(mContext, "文件不存在!");
            // 点击触发
            amrp_return_igview.performClick();
            return false;
        }
        return true;
    }

    // == 视频控制 ==

    /**
     * 切换播放
     * @param isStart
     */
    private void togglePlay(boolean isStart){
        // 属于视频类型才处理
        if (mediaType == MediaItem.MediaTypeEnum.VIDEO.getValue()){
            // 判断文件是否存在
            if (isStart && !isCheckFile()){
                return;
            }
            try {
                if (videoPlayerControl != null){
                    if (isStart){
                        videoPlayerControl.startPlayer(mediaUri, true);
                    } else {
                        videoPlayerControl.stopPlayer();
                    }
                }
            } catch (Exception e){
            }
        }
    }

    // ==

    // MD5值，可以存储到SP中
    public static String gGlideCameraKey = "";
    // 临时Bitmap
    public static Bitmap gGlideBitmap = null;

    /** 加载拍照图片 */
    private void loadTakeImage(){
        if (gGlideBitmap != null){
            amrp_takepic_igview.setImageBitmap(gGlideBitmap);
        } else {
            RequestOptions options = new RequestOptions();
            options.priorityOf(Priority.IMMEDIATE);
            options.signature(new ObjectKey(gGlideCameraKey));
            options.transform(new RotateTransformation(mContext, cameraRotate, isPortrait, isFrontCamera));
            options.dontAnimate();
            Glide.with(mContext).asBitmap().load(mediaUri).apply(options).into(amrp_takepic_igview);
        }
    }
}
