package com.pro.record;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.pro.record.utils.ProUtils;
import com.pro.record.widgets.RecordTimeView;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends Activity {


    /** 上下文 */
    private Context mContext;
    /** 日志Tag */
    private final String TAG = "MainActivity";
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    // ======== View ===========
    /** Camera 预览显示 */
    private SurfaceView am_surfaceview;
    /** 时间进度View */
    private RecordTimeView am_rtview;
    /** 时间进度Tv */
    private TextView am_time_tv;
    /** 底部操作Btn */
    private ImageButton am_operate_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // --
        mContext = MainActivity.this;
        // ================
        initViews();
        initValues();
        initListener();
    }

    @Override
    protected void onStart() { super.onStart(); }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() { super.onStop(); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void initViews(){
        // 初始化View
        am_surfaceview = (SurfaceView) this.findViewById(R.id.am_surfaceview);
        am_rtview = (RecordTimeView) this.findViewById(R.id.am_rtview);
        am_time_tv = (TextView) this.findViewById(R.id.am_time_tv);
        am_operate_btn = (ImageButton) this.findViewById(R.id.am_operate_btn);
    }

    public void initValues(){
        am_surfaceview.setFocusable(true);
        am_surfaceview.setFocusableInTouchMode(true);
        am_surfaceview.setClickable(true);
        mSurfaceHolder = am_surfaceview.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initCamera();
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

    public void initListener(){
        am_surfaceview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.autoFocus(new Camera.AutoFocusCallback() { // 对焦
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) { }
                });
            }
        });
        am_operate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (am_operate_btn.getTag() != null){
                    am_operate_btn.setBackgroundResource(R.mipmap.ic_play);
                    am_operate_btn.setTag(null);
                    // ===== 停止状态 ======
                    // 设置为空白
                    am_time_tv.setText("");
                    // 停止录制
                    am_rtview.stop();
                } else {
                    am_operate_btn.setBackgroundResource(R.mipmap.ic_stop);
                    am_operate_btn.setTag(TAG);
                    // --
                    /** 设置时间触发回调 */
                    am_rtview.setRecordTimeCallBack(new RecordTimeView.RecordTimeCallBack() {
                        @Override
                        public void preSecond(float dTime) { // 满一秒触发
                            // 进行刷新时间
                            am_time_tv.setText(ProUtils.secToTimeRetain((int) (dTime / 1000), true));
                        }
                        @Override
                        public void start(float dTime) { // 开始、恢复时触发
                            // 进行刷新时间
                            am_time_tv.setText(ProUtils.secToTimeRetain((int) (dTime / 1000), true));
                        }
                    });
                    // ===== 开始状态 ======
                    am_rtview.setKeepText(false); // 是否保留绘制文本的位置 - 如果进行属于进行绘制，则无视该参数
                    am_rtview.setDrawText(true); // 是否绘制文本
                    am_rtview.setTextSize(14.0f); // 设置文本大小 - 默认13f
                    // --
                    am_rtview.start(); // 开始
                    //am_rtview.start(20 * 1000f); // 从指定时间开始
                    //am_rtview.pause(); // 暂停
                    //am_rtview.recover(); // 恢复
                    //am_rtview.getTime(); // 获取时间，单位毫秒
                    //am_rtview.getTimes(); // 获取时间，单位秒数
                }
            }
        });
    }

    // =============================

    private void initCamera() {
        mCamera = Camera.open();
        Camera.Parameters parameters = mCamera.getParameters();

        // 操作1 为相机设置某种特效
        List<String> colorEffects = parameters.getSupportedColorEffects();
        Iterator<String> iterator1 = colorEffects.iterator();
        while (iterator1.hasNext()) {
            String effect = (String) iterator1.next();
            if (effect.equals(Camera.Parameters.EFFECT_SOLARIZE)) {
                // 若支持过度曝光效果,则设置该效果
                //parameters.setColorEffect(Camera.Parameters.EFFECT_SOLARIZE);
                break;
            }
        }
        // 操作3 当屏幕变化时,旋转角度.否则不对
        mCamera.setDisplayOrientation(90);
        // 操作结束
        try {
            // 将摄像头的预览显示设置为mSurfaceHolder
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            mCamera.release();
        }
        // 设置输出格式
        parameters.setPictureFormat(PixelFormat.JPEG);
        // 设置摄像头的参数.否则前面的设置无效
        mCamera.setParameters(parameters);
        //parameters.setPictureSize(480, 640); // 设置图片宽度高度
        parameters.set("jpeg-quality", 85);// 照片质量
        // 摄像头开始预览
        mCamera.startPreview();
    }
}
