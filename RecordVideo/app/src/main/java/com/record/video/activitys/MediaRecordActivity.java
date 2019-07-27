package com.record.video.activitys;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.record.video.R;
import com.record.video.bean.item.MediaItem;
import com.record.video.config.constants.KeyConstants;
import com.record.video.config.constants.NotifyConstants;
import com.record.video.utils.CameraUtils;
import com.record.video.utils.DevUtils;
import com.record.video.utils.RotateTransformation;
import com.record.video.utils.ToastUtils;
import com.record.video.utils.player.RecordMediaManager;
import com.record.video.widget.MediaRecorderView;
import com.record.video.widget.RecordLoadDialog;
import com.record.video.widget.RecordProgressBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * detail: 多媒体录制、拍照 Activity
 * Created by Ttt
 */
public class MediaRecordActivity extends Activity implements View.OnClickListener {

    // 上下文
    Context mContext;
    // ==== Obj ====
    // 加载Dialog
    RecordLoadDialog recordLoadDialog;
    // ==== View ====
    @BindView(R.id.amr_recordview)
    MediaRecorderView amr_recordview;
    @BindView(R.id.amr_reverse_igview)
    ImageView amr_reverse_igview;
    @BindView(R.id.amr_back_igview)
    ImageView amr_back_igview;
    @BindView(R.id.amr_camera_igview)
    ImageView amr_camera_igview;
    @BindView(R.id.amr_record_pbar)
    RecordProgressBar amr_record_pbar;
    @BindView(R.id.amr_focus_igview)
    ImageView amr_focus_igview;

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
        setContentView(R.layout.activity_media_record);
        // 初始化View
        ButterKnife.bind(this);
        // 统一设置跳转不需要动画效果
        this.overridePendingTransition(R.anim.noanim_left_in, R.anim.noanim_left_out);
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
        // 检查是否存在权限
        vHandler.sendEmptyMessage(NotifyConstants.NOTIFY_CHECK_PERMISSION);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 关闭传感器
        amr_recordview.toggleOrientationListener(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 重置为null
        MediaResultPreActivity.gGlideBitmap = null;
        // 关闭播放，防止意外
        RecordMediaManager.getInstance().stop();
        // 销毁操作, 防止浪费资源
        amr_recordview.destroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.amr_reverse_igview: // 镜头反转
                // 反转摄像头
                amr_recordview.reverseCamera();
                break;
            case R.id.amr_back_igview: // 返回键(缩小)
                // 表示取消
                setResult(Activity.RESULT_CANCELED, null);
                finish();
                this.overridePendingTransition(R.anim.noanim_left_in, R.anim.noanim_left_out);
                break;
        }
    }

    public void initValues() {
        // 初始化加载Dialog
        recordLoadDialog = new RecordLoadDialog(mContext);
        // 判断是否支持镜头反转
        DevUtils.setVisibility(CameraUtils.isSupportReverse(), amr_reverse_igview);
        // 设置录制回调
        amr_recordview.setRecordCallback(new MediaRecorderView.RecordCallback() {
            @Override
            public void onStartRecord() {
                // 开始录制, 隐藏View(反转、关闭、拍照按钮、返回、保存)
                DevUtils.setVisibilitys(false, amr_reverse_igview, amr_back_igview, amr_camera_igview);
                // 显示录制进度
                DevUtils.setVisibility(true, amr_record_pbar);
                // 重置进度
                amr_record_pbar.setProgress(0);
                amr_record_pbar.setMax(10000 + 2000); // 因为开始录制后, 但是因为录制时间有延迟, 所以+2秒进行控制延迟
            }

            @Override
            public void onStopRecordNotify(boolean isAchieve) {
                if (isAchieve) {
                    // 显示Dialog
                    recordLoadDialog.showDialog();
                } else {
                    // 重置状态
                    toggleCameraStatus(false);
                }
            }

            @Override
            public void onRecordTiming(int recordTime) {
                // 设置进度
                amr_record_pbar.setProgress(recordTime);
            }

            @Override
            public void onRecordFailure(Exception e) {
                // 重置状态
                toggleCameraStatus(false);
                // 录制失败,则进行提示
                ToastUtils.showToast(mContext, "录制失败");
            }

            @Override
            public void onTakeFailure(Exception e) {
                // 关闭 Dialog
                DevUtils.closeDialog(recordLoadDialog);
                // 判断错误类型, 进行提示
                if (e instanceof FileNotFoundException) {
                    // 保存失败,则进行提示
                    ToastUtils.showToast(mContext, "保存失败, 请检查权限");
                } else {
                    // 拍摄失败,则进行提示
                    ToastUtils.showToast(mContext, "保存失败");
                }
                // 重置状态
                toggleCameraStatus(false);
            }

            @Override
            public void onTakeNotify() {
                // 显示Dialog
                recordLoadDialog.showDialog();
            }

            @Override
            public void onTakeDealSuc(final Bitmap bitmap) {
                vHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 重置为null
                        MediaResultPreActivity.gGlideBitmap = null;
                        // 处理签名值
                        MediaResultPreActivity.gGlideCameraKey = DevUtils.MD5(amr_recordview.getRandomName());
                        // 是否横竖屏
                        boolean isPortrait = amr_recordview.isPortrait();
                        // 是否前置摄像头
                        boolean isFrontCamera = amr_recordview.isFrontCamera();
                        // 获取操作角度
                        int cameraRotate = amr_recordview.getRotationRecord();
                        // 资源地址
                        final String mediaUri = amr_recordview.getTakePicPath();
                        // = 加载配置, 分开便于理解阅读 =
                        RequestOptions options = new RequestOptions();
                        options.priorityOf(Priority.IMMEDIATE);
                        options.signature(new ObjectKey(MediaResultPreActivity.gGlideCameraKey));
                        options.transform(new RotateTransformation(mContext, cameraRotate, isPortrait, isFrontCamera));
                        options.dontAnimate();
                        // 加载图片
                        Glide.with(mContext).asBitmap().load(amr_recordview.getTakePicPath())
                                .apply(options).into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                // 关闭 Dialog
                                DevUtils.closeDialog(recordLoadDialog);
                                // 进行提示
                                ToastUtils.showToast(mContext, "加载失败!");
                            }

                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                // 保存图片
                                DevUtils.saveBitmap(mediaUri, resource, Bitmap.CompressFormat.JPEG, 70);
                                // 保存旋转过后的
                                MediaResultPreActivity.gGlideBitmap = resource;
                                // 通知处理成功
                                onCheckResult(true, true);
                                // 关闭 Dialog
                                DevUtils.closeDialog(recordLoadDialog);
                            }
                        });
                    }
                });
            }

            @Override
            public void onCheckResult(boolean isResult, boolean isTakePic) {
                // 属于视频才进行关闭
                if (!isTakePic) {
                    // 关闭 Dialog
                    DevUtils.closeDialog(recordLoadDialog);
                }
                // 如果已经处理了则不操作
                if (amr_recordview.isCheckFileNotify()){
                    // 关闭 Dialog
                    DevUtils.closeDialog(recordLoadDialog);
                    return;
                }
                // 设置已通知
                amr_recordview.setCheckFileNotify();
                // 判断结果
                if (isResult) {
                    // 判断是否保存成功
                    if (!isTakePic && isResult && amr_recordview.isStopError()){
                        // 如果结果失败, 则进行通知
                        ToastUtils.showToast(mContext, "录制时间过短");
                        // 重置状态
                        toggleCameraStatus(false);
                        return;
                    }
                    toggleCameraStatus(true, isTakePic);
                } else {
                    // 如果结果失败, 则进行通知
                    ToastUtils.showToast(mContext, "保存失败");
                    // 重置状态
                    toggleCameraStatus(false);
                }
            }
        });
    }

    public void initListeners() {
        amr_reverse_igview.setOnClickListener(this);
        amr_back_igview.setOnClickListener(this);
        // 设置滑动事件
        amr_camera_igview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: // 点击
                        // 触发手势
                        amr_recordview.actionDown();
                        break;
                    case MotionEvent.ACTION_MOVE: // 移动
                        // 触发手势
                        amr_recordview.actionMove(motionEvent);
                        break;
                    case MotionEvent.ACTION_UP: // 抬起
                        // 触发手势,并判断是否录制中 -> 如果不是,则表示拍照
                        if (!amr_recordview.actionUp()) {
                            // 进行拍照
                            amr_recordview.takeFrontPhoto();
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL: // 取消
                        // 重置状态
                        toggleCameraStatus(false);
                        break;
                }
                return true;
            }
        });
        // 设置录制View手势事件
        amr_recordview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: // 点击
                        // 防止多次点击
                        if (!DevUtils.isFastDoubleClick(R.id.amr_recordview, 80)) {
                            startFocusAnim(motionEvent);
                        }
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 过滤按键动作
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            // 触发点击事件
            amr_back_igview.performClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /** 回传处理 */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /** 判断回调类型 */
        switch (requestCode) {
            case NotifyConstants.NOTIFY_RECORD_OPERATE: // 录制操作
                if (resultCode == Activity.RESULT_OK && data != null){
                    setResult(Activity.RESULT_OK, data);
                    finish();
                    this.overridePendingTransition(R.anim.noanim_left_in, R.anim.noanim_left_out);
                } else {
                    // 重置为null
                    MediaResultPreActivity.gGlideBitmap = null;
                    try {
                        if (amr_recordview.isTakePic()) { // 图片
                            new File(amr_recordview.getTakePicPath() + "").delete();
                        } else { // 视频
                            new File(amr_recordview.getRecordPath() + "").delete();
                        }
                    } catch (Exception e){
                    }
                }
                break;
        }
    }

    // ==

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
                    // 检查是否存在权限
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        // 提示没有权限
                        Toast.makeText(mContext, "视频录制和录音没有授权", Toast.LENGTH_LONG);
                        finish();
                    } else {
                        // 恢复状态
                        toggleCameraStatus(false);
                    }
                    break;
            }
        }
    };

    // ==

    /**
     * 切换摄像头状态(是否保存)
     * @param isOperate 是否操作成功，显示保存
     */
    private void toggleCameraStatus(boolean isOperate) {
        toggleCameraStatus(isOperate, false);
    }

    /**
     * 切换摄像头状态(是否保存)
     * @param isOperate 是否操作成功，显示保存
     * @param isPic     是否图片
     */
    private void toggleCameraStatus(boolean isOperate, boolean isPic) {
        // 关闭所有定时
        amr_recordview.closeTimers();
        // 判断是否显示 反转、关闭、拍照按钮
        DevUtils.setVisibilitys(!isOperate, amr_reverse_igview, amr_back_igview, amr_camera_igview);
        // 隐藏录制进度
        DevUtils.setVisibilitys(false, amr_record_pbar);
        // 判断处理状态
        if (isOperate) {
            // 跳转页面,播放视频
            Intent intent = new Intent(mContext, MediaResultPreActivity.class);
            intent.putExtra(KeyConstants.EXTRA_VIDEO_ISPORTRAIT, amr_recordview.isPortrait()); // 是否横竖屏
            intent.putExtra(KeyConstants.EXTRA_VIDEO_ISFRONTCAMERA, amr_recordview.isFrontCamera()); // 是否前置摄像头
            intent.putExtra(KeyConstants.EXTRA_MEDIA_ROTATE, amr_recordview.getRotationRecord()); // 获取操作角度
            intent.putExtra(KeyConstants.EXTRA_MEDIA_URI, isPic ? amr_recordview.getTakePicPath() : amr_recordview.getRecordPath()); // 资源地址
            intent.putExtra(KeyConstants.EXTRA_MEDIA_TYPE, isPic ? MediaItem.MediaTypeEnum.IMAGE.getValue() : MediaItem.MediaTypeEnum.VIDEO.getValue()); // 资源类型
            startActivityForResult(intent, NotifyConstants.NOTIFY_RECORD_OPERATE);
            this.overridePendingTransition(R.anim.noanim_left_in, R.anim.noanim_left_out);
        } else {
            // 重置摄像头
            amr_recordview.resetCamera();
            // 删除图片
            MediaRecorderView.deleteCameraPic(mContext);
        }
    }

    // == 焦点动画 ==

    // 设置动画Tag
    String animTag;
    // 缩放动画
    ScaleAnimation scaleAnimation;

    /**
     * 启动焦点动画
     * @param event
     */
    private void startFocusAnim(MotionEvent event) {
        // 生成Tag
        animTag = DevUtils.MD5(System.currentTimeMillis() + "" + new Random().nextInt(50000));
        // 设置Tag
        amr_focus_igview.setTag(animTag);
        // 移除动画
        amr_focus_igview.clearAnimation();
        // 隐藏View
        DevUtils.setVisibilitys(false, amr_focus_igview);
        // 如果动画不为null则停止
        if (scaleAnimation != null) {
            // 取消动画
            scaleAnimation.cancel();
        }
        // 重置位置
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) amr_focus_igview.getLayoutParams();
        layoutParams.leftMargin = 0;
        layoutParams.topMargin = 0;
        // 获取屏幕宽、高度
        int sHeight = DevUtils.getScreenHeight(mContext);
        int sWidth = DevUtils.getScreenWidth(mContext);
        // 获取View宽度
        int vWidth = (int) mContext.getResources().getDimension(R.dimen.x150);
        // 获取尺寸 - 居中显示焦点
        int imgWidth = vWidth / 2;
        // 获取x, y 轴
        float x = event.getX();
        float y = event.getY();
        // 设置边距
        int left = 0, top = 0;
        // 获取最大值
        int diffWidth = (sWidth - vWidth); // 屏幕宽度 - View 宽度(高度)
        if (x > diffWidth) {
            left = diffWidth;
            left = (int) x - imgWidth; // 点击在正中间处理
        } else {
            left = (int) x;
            left -= imgWidth; // 点击在正中间处理
        }
        // 获取最大值
        int diffHeight = (sHeight - vWidth); // 屏幕高度 - View 宽度(高度)
        if (y > diffHeight) {
            top = diffHeight;
            top = (int) y - imgWidth; // 点击在正中间处理
        } else {
            top = (int) y;
            top -= imgWidth; // 点击在正中间处理
        }
        // 重置位置
        layoutParams.leftMargin = left;
        layoutParams.topMargin = top;
        // 缩放动画
        scaleAnimation = new ScaleAnimation(1, 0.6f, 1, 0.6f,
                Animation.RELATIVE_TO_SELF, 0.6f, Animation.RELATIVE_TO_SELF, 0.6f);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 判断是否相同的Tag
                if (amr_focus_igview.getTag() != null && animTag.equals((String) amr_focus_igview.getTag())) {
                    // 隐藏View
                    DevUtils.setVisibilitys(false, amr_focus_igview);
                }
            }
        });
        scaleAnimation.setDuration(350);
        // 延迟加载
        vHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 判断是否相同的Tag
                if (amr_focus_igview.getTag() != null && animTag.equals((String) amr_focus_igview.getTag())) {
                    // 显示View
                    DevUtils.setVisibilitys(true, amr_focus_igview);
                    // 启动动画
                    amr_focus_igview.startAnimation(scaleAnimation);
                }
            }
        }, 100);
    }
}
