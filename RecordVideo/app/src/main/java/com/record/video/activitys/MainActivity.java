package com.record.video.activitys;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.record.video.R;
import com.record.video.bean.MediaInfoBean;
import com.record.video.bean.item.MediaItem;
import com.record.video.bean.item.media.ImageInfoItem;
import com.record.video.bean.item.media.VideoInfoItem;
import com.record.video.config.constants.KeyConstants;
import com.record.video.config.constants.NotifyConstants;
import com.record.video.utils.DevUtils;
import com.record.video.utils.MediaDealUtils;
import com.record.video.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.logger.DevLogger;

/**
 * detail: MainActivity
 * Created by Ttt
 */
public class MainActivity extends Activity {

    // 上下文
    Context mContext;
    // 日志TAG
    final String TAG = MainActivity.class.getSimpleName();
    // ==== Obj ====
    @BindView(R.id.am_record_btn)
    Button am_record_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mContext = this;
        // 初始化View
        ButterKnife.bind(this);
        // 设置录制点击事件
        am_record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // 需要的权限 - 读取SD卡
                    String[] permissions = new String[]{ Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA };
                    // 判断是否含有某个权限
                    boolean hasPermission = PermissionsManager.getInstance().hasAllPermissions(MainActivity.this, permissions);
                    // 如果不存在才进行显示授权
                    if (!hasPermission) {
                        //不含权限，弹出开启权限窗
                        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(MainActivity.this, permissions, new PermissionsResultAction() {
                                    @Override
                                    public void onGranted() { // 同意开启权限
                                        // 跳转到录制页面
                                        startActivityForResult(new Intent(mContext, MediaRecordActivity.class), NotifyConstants.NOTIFY_RECORD_OPERATE);
                                    }
                                    @Override
                                    public void onDenied(String permission) { // 拒绝开启权限
                                        try {
                                            // 提示没有权限
                                            ToastUtils.showToast(mContext, "视频录制和录音没有授权");
                                        } catch (Exception e){
                                        }
                                    }
                                }
                        );
                    } else {
                        // 跳转到录制页面
                        startActivityForResult(new Intent(mContext, MediaRecordActivity.class), NotifyConstants.NOTIFY_RECORD_OPERATE);
                    }
                } catch (Exception e){
                    try {
                        ToastUtils.showToast(mContext, "获取权限失败,请稍后重试");
                    } catch (Exception e1){
                    }
                }
            }
        });
    }

    /** 回传处理 */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /** 判断回调类型 */
        switch (requestCode) {
            case NotifyConstants.NOTIFY_RECORD_OPERATE: // 录制操作
                if (resultCode == Activity.RESULT_OK && data != null){
                    // 获取路径
                    String mediaUri = data.getStringExtra(KeyConstants.EXTRA_MEDIA_URI);
                    // 获取类型
                    int mediaType = data.getIntExtra(KeyConstants.EXTRA_MEDIA_TYPE, MediaItem.MediaTypeEnum.UNKNOWN.getValue());
                    // 获取拍摄角度
                    int cameraRotate = data.getIntExtra(KeyConstants.EXTRA_MEDIA_ROTATE, -1);
                    // 判断是否竖屏
                    boolean isPortrait = data.getBooleanExtra(KeyConstants.EXTRA_VIDEO_ISPORTRAIT, false);
                    // 是否前置摄像头
                    boolean isFrontCamera = data.getBooleanExtra(KeyConstants.EXTRA_VIDEO_ISFRONTCAMERA, true);
                    // ===
                    // 获取类型
                    MediaItem.MediaTypeEnum mediaTypeEnum = MediaItem.MediaTypeEnum.getMediaType(mediaType);
                    // 不属于未知则进行处理
                    if (mediaTypeEnum != MediaItem.MediaTypeEnum.UNKNOWN){
                        // 判断文件是否存在
                        if (!DevUtils.isFileExists(mediaUri)){
                            DevLogger.dTag(TAG, "文件不存在");
                            return;
                        }
                        // 获取文件信息
                        MediaInfoBean mediaInfoBean = null;
                        // 判断类型，进行生成
                        if (mediaTypeEnum == MediaItem.MediaTypeEnum.VIDEO){
                            mediaInfoBean = VideoInfoItem.build(mediaUri, isPortrait, isFrontCamera, cameraRotate);
                        } else if (mediaTypeEnum == MediaItem.MediaTypeEnum.IMAGE){
                            mediaInfoBean = ImageInfoItem.build(mediaUri, isPortrait, isFrontCamera, cameraRotate);
                        }
                        // 判断是否为null
                        if (mediaInfoBean == null){
                            DevLogger.dTag(TAG, "转换类型出错");
                            return;
                        }
                        // 进行上传前处理
                        MediaDealUtils.getInstance().uploadDeal(mediaInfoBean);
                    }
                }
                break;
        }
    }
}
