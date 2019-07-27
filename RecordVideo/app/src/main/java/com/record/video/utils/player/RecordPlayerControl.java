package com.record.video.utils.player;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;

import java.io.File;

import dev.logger.DevLogger;

/**
 * detail: 录制视频播放控制器
 * Created by Ttt
 */
public class RecordPlayerControl implements OnClickListener,
		SurfaceHolder.Callback, RecordMediaManager.MediaListener {

	// ========= 外部回调  =========
	/** 日志Tag */
	private final String TAG = RecordPlayerControl.class.getSimpleName();
	// 当前页面
	private Activity activity;
	// 设置开始播放时间
	private int startPlayTime = 0;
	// ========= 其他变量  =========
	// ========= View ===========
	/** 播放载体SurfaceView */
	private SurfaceView surfaceview;
	/** 画面预览回调 */
	private SurfaceHolder surfaceHolder;

	/**
	 * 初始化构造函数
	 * @param activity 当前Activity
	 * @param surfaceview
	 */
	public RecordPlayerControl(Activity activity, SurfaceView surfaceview) {
		this.activity = activity;
		this.surfaceview = surfaceview;
		// 初始化View、Values、Listeners
		initValues();
		initListeners();
	}

	/** 初始化操作 */
	private void initValues(){
	}

	/** 初始化事件 */
	private void initListeners(){
		// 初始化AlbumMediaManager 回调事件类
		RecordMediaManager.getInstance().setMeidaListener(this);
	}

	/** 点击事件 */
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		}
	}

	// --

	/** 内部Handler */
	private Handler iHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			}
		}
	};

	// ================  内部快捷控制 ==================

	/**
	 * 重置操作
	 */
	private void resetOperate(){
		// 初始化监听
		initListeners();
		// 移除旧的回调
		if(surfaceHolder != null){
			surfaceHolder.removeCallback(this);
		}
		// 设置Holder
		surfaceHolder = surfaceview.getHolder();
		// 移除旧的回调
		if(surfaceHolder != null){
			surfaceHolder.removeCallback(this);
		}
		// 添加回调
		surfaceHolder.addCallback(this);
	}

	// ----------------- Surface回调事件  -----------------

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		DevLogger.dTag(TAG, "surfaceCreated");
		// --
		try {
			// 重新设置Holder
			RecordMediaManager.getInstance().getMediaPlayer().setDisplay(surfaceHolder);
			// --
			DevLogger.dTag(TAG, "setDisplay(surfaceHolder) - Success");
		} catch (Exception e) {
			DevLogger.eTag(TAG, e, "setDisplay(surfaceHolder) - Error");
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	// ----------------- MediaPlayer回调事件 -----------------

	@Override
	public void onPrepared() {
		DevLogger.dTag(TAG, "onPrepared");
		// --
		if (surfaceview != null) {
			// 如果等于null，或者不在显示中,则跳过
			if (surfaceHolder.getSurface() == null || !surfaceHolder.getSurface().isValid())
				return;

			try {
				MediaPlayer mPlayer = RecordMediaManager.getInstance().getMediaPlayer();
				mPlayer.setDisplay(surfaceHolder);
				mPlayer.start();
				// 小于500毫秒内, 则不处理
				if (startPlayTime <= 500) {
					// 设置播放时间
					mPlayer.seekTo(startPlayTime);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onCompletion() {
		DevLogger.dTag(TAG, "onCompletion");
		// -
		if (mediaListener != null){
			mediaListener.onCompletion();
		}
	}

	@Override
	public void onBufferingUpdate(int percent) {
		DevLogger.dTag(TAG, "onBufferingUpdate: " + percent);
		// -
		if (mediaListener != null){
			mediaListener.onBufferingUpdate(percent);
		}
	}

	@Override
	public void onSeekComplete() {
		DevLogger.dTag(TAG, "onSeekComplete");
		// -
		if (mediaListener != null){
			mediaListener.onSeekComplete();
		}
	}

	@Override
	public void onError(int what, int extra) {
		DevLogger.dTag(TAG, "onError -> what: " + what + ", extra: " + extra);
		// -
		if (mediaListener != null){
			mediaListener.onError(what, extra);
		}
	}

	@Override
	public void onVideoSizeChanged(int width, int height) {
		DevLogger.dTag(TAG, "onVideoSizeChanged -> width: " + width + ", height: " + height);
		// -
		if (mediaListener != null){
			mediaListener.onVideoSizeChanged(width, height);
		}
	}

	// ==

	// 播放事件监听
	private RecordMediaManager.MediaListener mediaListener;

	/**
	 * 设置播放监听事件
	 * @param mediaListener
	 */
	public void setMediaListener(RecordMediaManager.MediaListener mediaListener) {
		this.mediaListener = mediaListener;
	}

	// ================== 播放快捷操作 ======================

	/** 暂停播放 */
	public void pausePlayer(){
		RecordMediaManager.getInstance().pause();
	}

	/** 停止播放 */
	public void stopPlayer(){
		RecordMediaManager.getInstance().stop();
	}

	/**
	 * 开始播放(从头开始加载)
	 * @param uri 播放地址
	 * @param isLooping 是否循环播放
	 */
	public void startPlayer(String uri, boolean isLooping){
		// 打印信
		DevLogger.dTag(TAG, "startPlayer uri: " + uri + ((uri != null) ? ", 是否存在文件: " + new File(uri).exists() : ""));
		// 重置操作
		resetOperate();
		// 异步准备
		RecordMediaManager.getInstance().prePlayer(activity, uri, isLooping);
	}

	/**
	 * 是否正在播放中
	 * @param uri 播放地址
	 * @return
	 */
	public boolean isPlaying(String uri){
		if(!TextUtils.isEmpty(uri)){ // 需要播放的地址,必须不等于null
			// 获取之前播放路径
			String playUri = RecordMediaManager.getInstance().getPlayUri();
			// 如果不等于null,并且播放地址相同
			if(playUri != null && playUri.equals(uri)){
				try {
					return RecordMediaManager.getInstance().getMediaPlayer().isPlaying();
				} catch (Exception e) {
				}
			}
		}
		return false;
	}

	/**
	 * 设置开始播放时间
	 * @param startPlayTime
	 */
	public void setStartPlayTime(int startPlayTime) {
		this.startPlayTime = startPlayTime;
	}

	/**
	 * 获取显示的SurfaceView
	 * @return
	 */
	public SurfaceView getSurfaceview() {
		return surfaceview;
	}
}
