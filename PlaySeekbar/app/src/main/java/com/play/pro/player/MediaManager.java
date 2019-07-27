package com.play.pro.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * MediaPlayer 统一管理类
 */
public class MediaManager implements OnBufferingUpdateListener,
		OnCompletionListener, OnPreparedListener, OnVideoSizeChangedListener,
		OnErrorListener, OnSeekCompleteListener {

	/** 日志TAG */
	private String TAG = "MediaManager";
	/** MediaPlayer对象 */
	private MediaPlayer mMediaPlayer;
	/** 单例实例 */
	private static MediaManager instance;

	// --------------------
	private MediaManager() {
	}

	public static MediaManager getInstance() {
		if (instance == null) {
			instance = new MediaManager();
		}
		return instance;
	}

	// =============== 内部处理方法 =================
	/** 创建MediaPlayer */
	private void createMedia() {
		// 销毁MediaPlayer
		destroyMedia();
		// 初始化MediaPlayer
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.reset();
		// 绑定事件
		bindListener();
		// 设置默认流类型
		setAudioStreamType();
	}
	
	/** 销毁MediaPlayer */
	private void destroyMedia(){
		try {
			// 表示非播放状态
			if (mMediaPlayer != null) {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop(); // 停止播放
				}
				mMediaPlayer.release(); // 释放资源
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 重置为null
		mMediaPlayer = null;
		// 清空播放信息
		clearMPlayerData();
	}

	/**
	 * 绑定事件
	 * @return 绑定结果
	 */
	private boolean bindListener() {
		if (mMediaPlayer != null) {
			/** 播放结束回调 */
			mMediaPlayer.setOnBufferingUpdateListener(this);
			/** 播放结束回调 */
			mMediaPlayer.setOnCompletionListener(this);
			/** 预加载完成回调 */
			mMediaPlayer.setOnPreparedListener(this);
			/** 视频宽高大小改变回调 */
			mMediaPlayer.setOnVideoSizeChangedListener(this);
			/** 错误回调 */
			mMediaPlayer.setOnErrorListener(this);
			/** 滑动加载完成回调 */
			mMediaPlayer.setOnSeekCompleteListener(this);
			return true;
		}
		return false;
	}
	
	/**
	 * 设置流类型
	 * @param streamtype
	 */
	public void setAudioStreamType(int streamtype){
		if(mMediaPlayer != null){
			try {
				// 播放流类型
				mMediaPlayer.setAudioStreamType(streamtype);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 设置流类型(音量键可控制)
	 */
	private void setAudioStreamType(){
		setAudioStreamType(AudioManager.STREAM_MUSIC);
	}
	
	/**
	 * 预加载播放
	 * @param mContext 上下文
	 * @param playUri 播放地址
	 */
	public boolean prePlayer(Context mContext, String playUri){
		if(!TextUtils.isEmpty(playUri)){
			try {
				// 初始化MediaPlayer
				createMedia();
				// 保存播放地址
				mUri = playUri;
				// 设置播放路径
	            mMediaPlayer.setDataSource(mContext, Uri.parse(playUri));
	            // 异步加载
	            mMediaPlayer.prepareAsync();
	            return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	// =============== MediaPlayer操作 =================

	/**
	 * 是否播放中(判断null)
	 * @return
	 */
	public boolean isPlaying(){
		if(mMediaPlayer != null){
			return mMediaPlayer.isPlaying();
		}
		return false;
	}
		
	/**
	 * 暂停操作(判断null)
	 */
	public void pause(){
		if(mMediaPlayer != null){
			mMediaPlayer.pause();
		}
	}
		
	/**
	 * 停止操作(判断null) - 销毁MediaPlayer
	 */
	public void stop(){
		// 销毁MediaPlayer
		destroyMedia();
	}
	
	// =============== get/set方法 =================
	/**
	 * 获取MediaPlayer 对象
	 * @return
	 */
	public MediaPlayer getMediaPlayer() {
		return mMediaPlayer;
	}
	
	/**
	 * 设置Tag打印
	 * @param tag
	 */
	public void setTAG(String tag) {
		TAG = tag;
	}
	
	// =============== 快捷操作  ================
	
	/**
	 * 是否忽略错误类型
	 * @param eWhat
	 * @return
	 */
	public static boolean isIgnoreWhat(int eWhat){
		// 是否忽略
		boolean isIgnore = false;
		switch(eWhat){
		case -38:
		case 1:
		case 100:
		case 700:
		case 701:
		case 800:
			isIgnore = true;
			break;
		}
		return isIgnore;
	}
	
	// =============== 回调事件 =================
	
	/** 播放出错回调 */
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.d(TAG, "onError - what: " + what + ", extra: " + extra);
		// ---
		if(mMeidaListener != null){
			mMeidaListener.onError(what, extra);
		}
		return false;
	}
	
	/** 视频大小改变回调 */
	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		Log.d(TAG, "onVideoSizeChanged - width: " + width + ", height: " + height);
		videoWidth = width;
		videoHeight = height;
		// ---
		if(mMeidaListener != null){
			mMeidaListener.onVideoSizeChanged(width, height);
		}
	}
	
	/** 使用 mMediaPlayer.prepareAsync(); 异步播放准备成功回调 */
	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d(TAG, "onPrepared");
		// ---
		if(mMeidaListener != null){
			mMeidaListener.onPrepared();
		}
	}
	
	/** 视频播放结束回调 */
	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(TAG, "onCompletion");
		// ---
		if(mMeidaListener != null){
			mMeidaListener.onCompletion();
		}
	}

	/** MediaPlayer 缓冲更新回调 */
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		Log.d(TAG, "onBufferingUpdate - percent: " + percent);
		// ---
		if(mMeidaListener != null){
			mMeidaListener.onBufferingUpdate(percent);
		}
	}

	/** 滑动加载完成回调 */
	@Override
	public void onSeekComplete(MediaPlayer mp) {
		Log.d(TAG, "onSeekComplete");
		// ---
		if(mMeidaListener != null){
			mMeidaListener.onSeekComplete();
		}
	}
	
	// =============== 封装回调事件 =================
	/** MediaPlayer回调事件 */
	private MediaListener mMeidaListener;
	
	/** MediaPlayer回调接口 */
	public interface MediaListener {
		
		void onPrepared();
		
		void onCompletion();
		
		void onBufferingUpdate(int percent);
		
		void onSeekComplete();
		
		void onError(int what, int extra);
		
		void onVideoSizeChanged(int width, int height);
	}
	
	/**
	 * 设置MediaPlayer回调
	 * @param mMeidaListener
	 */
	public void setMeidaListener(MediaListener mMeidaListener) {
		this.mMeidaListener = mMeidaListener;
	}
	
	// =============== 内部属性 =================
	
	/** 播放路径 */
	private String mUri = null;
	/** 视频宽度 */
	private int videoWidth = 0;
	/** 视频高度 */
	private int videoHeight = 0;
	
	/** 清空播放信息 */
	private void clearMPlayerData(){
		mUri = null;
		videoWidth = 0;
		videoHeight= 0;
	}
	
	/** 获取当前播放的地址 */
	public String getPlayUri(){
		return mUri;
	}

	/** 获取视频宽度 */
	public int getVideoWidth() {
		return videoWidth;
	}

	/** 获取视频高度 */
	public int getVideoHeight() {
		return videoHeight;
	}
}
