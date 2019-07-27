package com.play.pro.player;

import java.io.File;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.play.pro.R;
import com.play.pro.constants.FinalConstants;
import com.play.pro.utils.ProUtils;
import com.play.pro.utils.ScreenUtils;
import com.play.pro.utils.TimerUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * 播放控制器
 */
public class PlayerControl implements OnClickListener,
		SurfaceHolder.Callback, MediaManager.MediaListener, OnSeekBarChangeListener {

	// ========= 外部回调  =========
	/** 日志Tag */
	private final String TAG = "PlayerControl";
	/** 平常一些信息打印日志TAG */
	private final String OP_TAG = "PlayerOperate";
	/** 上下文 */
	private Context mContext;
	/** 当前页面 */
	private Window window;
	/** 外部Handler，改变View */
	private Handler vHandler;
	// ========= 其他变量  =========
	// --
	/** 上次离开时间 */
	private long lastExitTime = -1l;
	/** 上次触发完成回调时间 */
	private long lastCompletionTime = -1l;
	// --
	/** 当前加载索引 */
	private int cLoadPos = -1;
	/** 是否经过 onPrepared*/
	private boolean isPrepared = false;
	/** 是否全屏播放 */
	private boolean isFullScreen = false;
	/** 是否默认设置SurfaceView背景 */
	private boolean isLoadSurCover = true;
	/** 是否切换全屏(临时状态) - 作用，用于判断是否pause MediaPlayer*/
	private boolean isToggleFullScreen = false;
	/** 是否暂停 */
	private boolean isPause = false;
	/** 是否结束销毁 */
	private boolean isDestroy = false;
	/** 是否本地视频 */
	private boolean isLocalVideo = false;
	/** 是否恢复播放 */
	private boolean isReStart = false;
	/** 是否滑动加载 */
	private boolean isTouchLoadIng = false;
	/** 是否时间改变进行通知 */
	private boolean isTimeChangeNotify = false;
	/** 封面图片 */
	private Drawable drawable;
	/** 加载定时器 */
	private TimerUtils loadTimeUtils; 
	/** 播放时间定时器工具类 */
	private TimerUtils playTimeUtils;
	// --
	/** 画面预览回调 */
	private SurfaceHolder surfaceHolder;
	// ========= View ===========
	/** 最外层FrameLayout */
	private FrameLayout ip_frame;
	/** 封面背景 IgView*/
	private ImageView ip_bg_igview;
	// --
	/** 播放载体SurfaceView */
	private SurfaceView ip_surfaceview;
	// --
	/** 加载最外层Linear */
	private LinearLayout ip_loading_linear;
	/** 第一个加载点IgView */
	private ImageView ip_one_igview;
	/** 第二个加载点IgView */
	private ImageView ip_two_igview;
	/** 第三个加载点IgView */
	private ImageView ip_three_igview;
	// --
	/** 底部功能Linear */
	private LinearLayout ip_function_linear;
	/** 播放、暂停IgView */
	private ImageView ip_play_igview;
	/** 当前播放时间Tv */
	private TextView ip_ctime_tv;
	/** 进度滑动SeekBar */
	private SeekBar ip_seekbar;
	/** 视频总时间Tv */
	private TextView ip_ttime_tv;
	/** 全屏IgView */
	private ImageView ip_fullscreen_igview;
	
	/**
	 * 初始化构造函数
	 * @param activity 当前Activity
	 * @param vHandler 点击触发修改
	 */
	public PlayerControl(Activity activity, Handler vHandler) {
		this.mContext = activity;
		this.window = activity.getWindow();
		this.vHandler = vHandler;
		// 初始化View、Values、Listeners
		initViews();
		initValues();
		initListeners();
	}
	
	/** 初始化View */
	private void initViews(){
		// 初始化View
		if(window != null){
			// 初始化View
			ip_frame = (FrameLayout) window.findViewById(R.id.ip_frame);
			ip_surfaceview = (SurfaceView) window.findViewById(R.id.ip_surfaceview);
			ip_bg_igview = (ImageView) window.findViewById(R.id.ip_bg_igview);
			ip_loading_linear = (LinearLayout) window.findViewById(R.id.ip_loading_linear);
			ip_one_igview = (ImageView) window.findViewById(R.id.ip_one_igview);
			ip_two_igview = (ImageView) window.findViewById(R.id.ip_two_igview);
			ip_three_igview = (ImageView) window.findViewById(R.id.ip_three_igview);
			ip_function_linear = (LinearLayout) window.findViewById(R.id.ip_function_linear);
			ip_play_igview = (ImageView) window.findViewById(R.id.ip_play_igview);
			ip_ctime_tv = (TextView) window.findViewById(R.id.ip_ctime_tv);
			ip_seekbar = (SeekBar) window.findViewById(R.id.ip_seekbar);
			ip_ttime_tv = (TextView) window.findViewById(R.id.ip_ttime_tv);
			ip_fullscreen_igview = (ImageView) window.findViewById(R.id.ip_fullscreen_igview);
		}
	}
	
	/**
	 * 初始化操作
	 */
	private void initValues(){
		// 默认标识非切换全屏
		this.isToggleFullScreen = false;
	}
	
	/** 初始化事件 */
	private void initListeners(){
		// 设置点击事件
		ip_frame.setOnClickListener(this);
		ip_play_igview.setOnClickListener(this);
		ip_seekbar.setOnSeekBarChangeListener(this);
		// 设置全屏点击事件
		ip_fullscreen_igview.setOnClickListener(this);
		// 初始化MediaManager 回调事件类
		MediaManager.getInstance().setMeidaListener(this);
	}

	/** 点击事件 */
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		/** 点击最外层 */
		case R.id.ip_frame:
			// --
			if(ip_loading_linear.getVisibility() == View.VISIBLE){
				return; // 属于加载中则不显示
			}
			// --
			// 判断是否显示底部功能(取反)
			boolean isVisibie = !(ip_function_linear.getVisibility() == View.VISIBLE);
			// 设置对应的状态,是否显示
			ip_function_linear.setVisibility(isVisibie ? View.VISIBLE : View.GONE);
			break;
		/** 播放点击操作 */
		case R.id.ip_play_igview:
			// 如果播放中,则显示暂停
			boolean isPlaying = MediaManager.getInstance().isPlaying();
			// 进行取反设置
			playerView(!isPlaying);
			// 如果播放中则进行暂停
			if(isPlaying){
				// 暂停定时器
				playTimer(false);
				// 暂停视频
				MediaManager.getInstance().pause();
				// --
				if(vHandler != null){
					vHandler.sendEmptyMessage(FinalConstants.PLAY_PAUSE);
				}
			} else {
				// 刷新定时器
				//resetTime(MediaManager.getInstance().getMediaPlayer());
				// 启动定时器
				playTimer(true);
				// 如果重新播放,则重置时间
				MediaManager.getInstance().getMediaPlayer().start();
			}
			break;
		/** 点击全屏 */
		case R.id.ip_fullscreen_igview:
			if(vHandler != null){
				vHandler.sendEmptyMessage(FinalConstants.FULL_SCREEN);
			}
			break;
		}
	}
	
	// --
	
	/** 内部Handler */
	private Handler iHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case FinalConstants.COUNT_DOWN: // 倒计时加载
				// 如果加载View,没有进行显示了,则隐藏
				if(ip_loading_linear.getVisibility() != View.VISIBLE){
					loadTimer(false); // 关闭加载定时器
					return;
				}
				cLoadPos++; // 递增加载索引
				// 判断当前进度点
				switch(cLoadPos){
				case 0:
				case 1:
				case 2:
					break;
				default: // 其他情况
					cLoadPos = 0;
					break;
				}
				// ==== 设置图片加载索引 ====
				switch(cLoadPos){
				case 0:
					ip_one_igview.setImageResource(R.mipmap.ic_loading_point_white);
					ip_two_igview.setImageResource(R.mipmap.ic_loading_point_grey);
					ip_three_igview.setImageResource(R.mipmap.ic_loading_point_grey);
					break;
				case 1:
					ip_one_igview.setImageResource(R.mipmap.ic_loading_point_grey);
					ip_two_igview.setImageResource(R.mipmap.ic_loading_point_white);
					ip_three_igview.setImageResource(R.mipmap.ic_loading_point_grey);
					break;
				case 2:
					ip_one_igview.setImageResource(R.mipmap.ic_loading_point_grey);
					ip_two_igview.setImageResource(R.mipmap.ic_loading_point_grey);
					ip_three_igview.setImageResource(R.mipmap.ic_loading_point_white);
					break;
				}
				break;
			// =======================
			case FinalConstants.PLAY_TIME_CHANGE: // 时间改变触发
				// 时间改变触发
				if (vHandler != null){
					vHandler.sendEmptyMessage(FinalConstants.PLAY_TIME_CHANGE);
				}
				break;
			case FinalConstants.PLAY_TIME: // 播放时间
				// 触发刷新播放时间
				refPlayerTime();
				// --
				if (isTimeChangeNotify){
					// 表示不需要通知
					isTimeChangeNotify = false;
					// 时间改变触发
					iHandler.sendEmptyMessage(FinalConstants.PLAY_TIME_CHANGE);
				}
				break;
			case FinalConstants.RELOAD: // 重新加载
				// 每次重新进行显示处理
				videoLoadIng();
				break;
			case FinalConstants.PLAY_START: // 开始播放回调
				isTriggerMsg = true;
				// --
				Object obj = msg.obj;
				if(obj != null && sTime != null){
					// 是否发送消息
					boolean isSend = false;
					try {
						// 判断是否发送
						isSend = (sTime.equals(((String) obj)));
					} catch (Exception e) {
					}
					// 符合条件则进行发送
					if(isSend && vHandler != null){
						sTime = null; // 发送了则设置为null
						// 如果属于暂停则不进行处理
						if (isPause){
							return;
						}
						// --
						Message nMsg = new Message();
						nMsg.what = FinalConstants.PLAY_START;
						nMsg.arg1 = msg.arg1; // 表示需要重新播放(调用start)
						vHandler.sendMessage(nMsg);
					}
				}
				break;
			case FinalConstants.PLAY_ERROR: // 播放异常回调
				isDestroy = true;
				msgTime = -1l;
				msgNumber = 0;
				// --
				stopPlayer();
				// --
				destroy(true);
				
				//ToastUtils.showToast(mContext, "异常回调");
				// 重新触发播放
				sendPlayMsg(100, 1);
				break;
			}
		}
	};
	
	// ================ 定时器操作方法  ========================
	
	/**
	 * 加载定时器
	 * @param isStart 是否启动
	 */
	public void loadTimer(boolean isStart){
		if(loadTimeUtils == null){
			// 初始化加载定时器
			loadTimeUtils = new TimerUtils(iHandler);
		}
		if(isStart){
			// 重置当前索引
			cLoadPos = -1;
			// --
			loadTimeUtils.setTriggerLimit(-1);
			loadTimeUtils.setTime(0, 700);
			loadTimeUtils.setNotifyWhat(FinalConstants.COUNT_DOWN);
			loadTimeUtils.startTimer();
		} else {
			loadTimeUtils.closeTimer();
		}
	}
	
	/**
	 * 播放定时器
	 * @param isStart 是否启动
	 */
	public void playTimer(boolean isStart){
		if(playTimeUtils == null){
			// 初始化播放定时器
			playTimeUtils = new TimerUtils(iHandler);
		}
		if(isStart){
			playTimeUtils.setTriggerLimit(-1);
			playTimeUtils.setTime(50, 1000);
			playTimeUtils.setNotifyWhat(FinalConstants.PLAY_TIME);
			playTimeUtils.startTimer();
		} else {
			playTimeUtils.closeTimer();
		}
	}
	
	// ================  内部View控制 ==================
	
	/**
	 * 背景View
	 * @param isShow 是否显示加载
	 */
	private void backView(boolean isShow){
		ip_bg_igview.setVisibility(isShow ? View.VISIBLE : View.GONE);
	}
	
	/**
	 * 加载View操作
	 * @param isShow 是否显示加载
	 */
	private void loadView(boolean isShow){
		ip_loading_linear.setVisibility(isShow ? View.VISIBLE : View.GONE);
		// 是否开启定时器
		loadTimer(isShow);
	}
	
	/**
	 * 底部功能View操作
	 * @param isShow 是否显示加载
	 */
	private void functionView(boolean isShow){
		ip_function_linear.setVisibility(isShow ? View.VISIBLE : View.GONE);
		// 判断是否显示全屏按钮
		if(isShow){ // 显示，才判断显示什么图标
			ip_fullscreen_igview.setImageResource(isFullScreen ? R.mipmap.ic_media_fullscreen_shrink_white : R.mipmap.ic_media_fullscreen_stretch_white);
		}
	}
	
	/**
	 * 设置播放View显示的图标
	 * @param isPlayer 是否播放
	 */
	private void playerView(boolean isPlayer){
		ip_play_igview.setImageResource(isPlayer ? R.mipmap.ic_media_stop : R.mipmap.ic_media_play);
	}
	
	/**
	 * 刷新播放时间
	 */
	private void refPlayerTime(){
		// 获取播放进度
		MediaPlayer mPlayer = MediaManager.getInstance().getMediaPlayer();
		// 防止为null
		if(mPlayer == null){
			return;
		}
		// 获取当前播放进度(播放时间 - 毫秒)
		int cTime = mPlayer.getCurrentPosition();
		// 是否滑动加载中
		if(isTouchLoadIng){
			// 处理一次，然后后续根据播放时间
			isTouchLoadIng = false;
			// 重置进度(滑动后的)
			cTime = ip_seekbar.getProgress();
		} else {
			// 重置进度
			ip_seekbar.setProgress(cTime);
		}
		// ==
		//if (ivprap_seekbar.getMax() <= 0){
			// 获取总时间
			int aTime = mPlayer.getDuration();
			// 设置最大值时间
			ip_seekbar.setMax(aTime);
		//}
		// 格式化时间
		String time = ProUtils.secToTimeRetain((int) (cTime / 1000), true);
		// 设置时间
		ip_ctime_tv.setText(time);
	}
	
	/**
	 * 刷新视频总时间
	 */
	private void refTotalTime(){
		// 获取总时间
		int tTime = ip_seekbar.getMax();
		// 防止时间异常（从19700101开始）
		if (tTime >= 86400000) { // 大于1天的时间都是异常状态
			// 设置总时间
			ip_ttime_tv.setText("00:00:00");
			return;
		}
		// 格式化时间
		String time = ProUtils.secToTimeRetain((int) (tTime / 1000), true);
		// 设置总时间
		ip_ttime_tv.setText(time);
	}
	
	/**
	 * 重置时间
	 * @param mPlayer 播放对象
	 */
	private void resetTime(MediaPlayer mPlayer){
		// 设置当前时间
		ip_ctime_tv.setText("00:00:00");
		// --
		if(mPlayer != null){
			// 设置总进度
			ip_seekbar.setMax(mPlayer.getDuration());
			// 设置当前播放进度
			ip_seekbar.setProgress(mPlayer.getCurrentPosition());
			// 开启定时器
			playTimer(true);
		}
		// 刷新总时间
		refTotalTime();
		// 判断是否本地视频
		if(isLocalVideo){ // 设置进度
			onBufferingUpdate(100);
		}
	}
	
	// ================  内部快捷控制 ==================
	
	/** 视频加载中 */
	private void videoLoadIng(){
		Log.d(TAG, "videoLoadIng");
		// 显示加载中
		loadView(true);
		// 显示封面
		backView(true);
		// 隐藏底部功能列表
		functionView(false);
	}
	
	/** 视频加载成功 */
	private void videoLoadSuc(){
		Log.d(TAG, "videoLoadSuc");
		// 隐藏加载中
		loadView(false);
		// 隐藏封面
		backView(false);
		// 表示播放中
		playerView(true);
		// 显示底部功能列表
		functionView(true);
	}
	
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
		surfaceHolder = ip_surfaceview.getHolder();
		// 移除旧的回调
		if(surfaceHolder != null){
			surfaceHolder.removeCallback(this);
		}
		// 添加回调
		surfaceHolder.addCallback(this);
	}
	
	/**
	 * 设置SurfaceView 背景
	 * @param isLoad 是否进行加载背景
	 */
	private void setSurfaceBackGround(boolean isLoad){
		if(isLoad && drawable != null && ip_surfaceview != null){
			Log.d(OP_TAG, "进行设置了背景");
			try {
				ip_surfaceview.setBackgroundDrawable(drawable);
			} catch (Exception e) {
			}
		}
		if(isLoad){ // 如果加载过一次，则不再加载
			this.isLoadSurCover = false;
		}
	}
	
	/**
	 * 是否移除背景
	 * @param isClear 是否移除
	 */
	private void cleSurfaceBackGround(boolean isClear){
		try {
			// 如果背景不为null,才进行重置
			if(isClear){
				ip_surfaceview.setBackgroundDrawable(null);
				// --
				Log.d(OP_TAG, "强制设置 setBackgroundDrawable(null)");
			} else {
				// 获取当前背景
				Drawable cBackDraw = ip_surfaceview.getBackground();
				if(cBackDraw != null && drawable != null && cBackDraw == drawable){
					ip_surfaceview.setBackgroundDrawable(null);
					// --
					Log.d(OP_TAG, "符合条件设置 setBackgroundDrawable(null)");
				} else {
					// --
					String hint = "(cBackDraw != null) : " + (cBackDraw != null) + ", (drawable != null) :" + (drawable != null) + ", (cBackDraw == drawable) : " + (cBackDraw == drawable);
					Log.d(OP_TAG, "不符合条件 -> " + hint);
				}
			}
		} catch (Exception e) {
		}
	}
	
	/**
	 * 重置FrameLayout - View的宽度高度
	 */
	private void resetViewWidthHeight(){
		Log.d(OP_TAG, "resetViewWidthHeight - isFullScreen: " + isFullScreen);
		// --
		// 如果属于全屏则不用处理
		if(isFullScreen){
			return;
		}
		float width = 640;
		float height = 380;
		// -
		int[] whArs = ScreenUtils.reckonVideoWidthHeight(width, height, mContext);
		if(whArs != null){
			int nWidth = whArs[0];
			int nHeight = whArs[1];
			// --
			LinearLayout.LayoutParams sParams = new LinearLayout.LayoutParams(nWidth, nHeight);
			ip_frame.setLayoutParams(sParams);
		}
	}
	
	/**
	 * 加载封面
	 * @param uri 封面路径
	 */
	private void loadCover(String uri){
		if(TextUtils.isEmpty(uri)){ // 如果为null,这不进行显示
			return;
		}
		// 开始加载封面
		ImageLoader.getInstance().loadImage(uri, new ImageLoadingListener() {
			@Override // 开始加载
			public void onLoadingStarted(String imageUri, View view) { }
			@Override // 加载取消
			public void onLoadingCancelled(String imageUri, View view) { }
			@Override // 加载失败
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) { }
			@Override // 加载完成
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				try {
					// 转换背景
					drawable = new BitmapDrawable(loadedImage);
					// 判断是否加载过
					if(!isPrepared && isLoadSurCover){
						// 判断是否加载图片(防止切换全屏默认进入显示图片)
						setSurfaceBackGround(true);
					}
					// 设置背景图片
					ip_bg_igview.setImageBitmap(loadedImage);
				} catch (Exception e) {
				}
			}
		});
	}
	
	// ----------------- SeeBar滑动回调事件  -----------------
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		isTouchLoadIng = false; // 当拖动条发生变化时调用该方法
		isTimeChangeNotify = false;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		isTouchLoadIng = false; // 开始滑动触发
		isTimeChangeNotify = false;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// 获取滑动值
		int touchTime = seekBar.getProgress();
		// 如果视频播放结束，则不处理
		if(!isDestroy){
			// 获取播放对象
			MediaPlayer mPlayer = MediaManager.getInstance().getMediaPlayer();
			if(mPlayer != null){ //  && mPlayer.isPlaying()
				// 表示属于加载中
				isTouchLoadIng = true;
				// 表示需要进行通知
				isTimeChangeNotify = true;
				// 设置当前进度
				ip_seekbar.setProgress(touchTime);
				// 触发刷新播放时间
				refPlayerTime();
				try {
					// 设置滑动的进度
					mPlayer.seekTo(touchTime);
				} catch (Exception e) {
				}
				// 如果属于暂停中,则开始播放
				if (!mPlayer.isPlaying()){
					mPlayer.start();
				}
				// 时间改变触发
				iHandler.sendEmptyMessage(FinalConstants.PLAY_TIME_CHANGE);
				// 显示成播放中的图片
				playerView(true);
			}
		}
	}
	
	// ----------------- Surface回调事件  -----------------

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(OP_TAG, "surfaceCreated");
		// --
		if(!isPrepared){ // 移除背景
			cleSurfaceBackGround(false);
		}
		try {
			// 重新设置Holder
			MediaManager.getInstance().getMediaPlayer().setDisplay(surfaceHolder);
			// --
			Log.d(OP_TAG, "setDisplay(surfaceHolder) - Success");
		} catch (Exception e) {
			Log.e(OP_TAG, "setDisplay(surfaceHolder) - Error", e);
		}
		// --
		// 如果属于全屏,则进行重置处理
		if(isFullScreen){
			Log.d(OP_TAG, "start Player");
			// 表示加载成功
			videoLoadSuc();
			// 进行延迟播放 - 给予初始化时间，直接调用start() 不会进行播放
			iHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						// 开始播放
						MediaManager.getInstance().getMediaPlayer().start();
						// 进行重置最大值
						ip_seekbar.setMax(-1);
						// 重置播放时间
						resetTime(MediaManager.getInstance().getMediaPlayer());
					} catch (Exception e) {
					}
				}
			}, 100);
		} else if (isReStart){ // 如果属于恢复播放,则进行播放
			// 进行延迟播放 - 给予初始化时间，直接调用start() 不会进行播放
			iHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						// 开始播放
						MediaManager.getInstance().getMediaPlayer().start();
					} catch (Exception e) {
					}
				}
			}, 100);
		}
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}
	
	// ----------------- MediaPlayer回调事件 -----------------

	@Override
	public void onPrepared() {
		Log.d(TAG, "onPrepared");
		// 表示经过onPrepared方法
		isPrepared = true;
		// 表示加载成功
		videoLoadSuc();
		// --
		if (ip_surfaceview != null) {
			// 移除背景
			cleSurfaceBackGround(false);
			// 如果等于null，或者不在显示中,则跳过
			if (surfaceHolder.getSurface() == null || !surfaceHolder.getSurface().isValid())
				return;

			try {
				MediaPlayer mPlayer = MediaManager.getInstance().getMediaPlayer();
				mPlayer.setDisplay(surfaceHolder);
				mPlayer.start();
				// --
				// 进行重置最大值
				ip_seekbar.setMax(-1);
				// 重置播放时间
				resetTime(mPlayer);
			} catch (Exception e) {
			}
		}
	}
	
	@Override
	public void onCompletion() {
		Log.d(TAG, "onCompletion -> isFullScreen: " + isFullScreen);
		// 属于销毁则清空处理
		if (isDestroy){
			return;
		}
		// 重置触发onCompletion()时间
		lastCompletionTime = System.currentTimeMillis();
		// 判断是否需要忽略的异常
		if (MediaManager.isIgnoreWhat(eWhat)){
			// 重置清空异常
			eWhat = -1;
			// --
			if (msgTime == -1){
				// 重置时间
				msgTime = System.currentTimeMillis();
				// 重置次数
				msgNumber = 0;
			} else {
				// 获取当前时间
				long cTime = System.currentTimeMillis();
				// 判断触发次数
				if (msgNumber >= 5){
					if (cTime - msgTime <= 500l){
						// 表示进行销毁
						isDestroy = true;
						// --
						iHandler.sendEmptyMessage(FinalConstants.PLAY_ERROR);
						return;
					} else {
						// 重置时间
						msgTime = System.currentTimeMillis();
						// 重置次数
						msgNumber = 0;
					}
				}
				//LogUtils.INSTANCE.d("YJDL", "msgNumber: " + msgNumber + ", time: " + (cTime - msgTime));
				msgNumber ++;
			}
			// --
			// 显示加载完成状态
			videoLoadSuc();
			// 发送播放消息
			sendPlayMsg();
			return;
		}
		// 判断是否播放完成
		if(isPlayComple()){
			if(vHandler != null){
				vHandler.sendEmptyMessage(FinalConstants.PLAY_COMPLE);
			}
		} else { // 不是播放完成,判断是否重新播放
			if (isLocalVideo){ // 属于本地视频,直接触发播放结束
				if(vHandler != null){
					vHandler.sendEmptyMessage(FinalConstants.PLAY_COMPLE);
				}
				return;
			}
			// 属于在线视频,判断触发周期,看是否需要重新播放
			isTriggerCycle();
		}
	}

	@Override
	public void onBufferingUpdate(int percent) {
		Log.d(TAG, "onBufferingUpdate: " + percent);
		
		if (percent >= 0) {
			// 获取总的时间
			int tTime = ip_seekbar.getMax();
			// 防止时间异常（从19700101开始）
			if (tTime >= 86400000) { // 大于1天的时间都是异常状态
				return;
			}
			// 计算进度(转换进度)
			int cProgress = tTime * percent / 100;
			// 进行设置
			ip_seekbar.setSecondaryProgress(cProgress);
		}
	}

	@Override
	public void onSeekComplete() {
		Log.d(TAG, "onSeekComplete");
	}
	
	@Override
	public void onError(int what, int extra) {
		Log.d(TAG, "onError -> what: " + what + ", extra: " + extra);
		// 保存错误类型
		eWhat = what;
	}

	@Override
	public void onVideoSizeChanged(int width, int height) {
	}
	
	// ================== 播放器内部判断  ====================
	
	/** 判断错误类型 */
	private int eWhat = -1;
	/** 上次触发的时间 */
	private long msgTime = -1l;
	/** 触发的次数 */
	private int msgNumber = 0;
	/** 触发的时间 */
	private String sTime = null;
	/** 消息对象 */
	private Message msg = null;
	/** 是否触发消息 */
	private boolean isTriggerMsg = true;
	
	/** 清空消息 */
	private void removeMsg(){
		// 如果Handler 不为null
		if(iHandler != null){
			try {
				// 回收延迟播放通知
				iHandler.removeMessages(FinalConstants.PLAY_START, msg);
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * 发送播放消息(播放完成 异常触发 - 100毫秒)
	 */
	private void sendPlayMsg(){
		sendPlayMsg(100, 0);
	}
	
	/**
	 * 发送播放消息
	 * @param delayMillis 延迟触发时间
	 * @param arg1 0 = 恢复播放， 1 = 重新播放
	 */
	private void sendPlayMsg(long delayMillis, int arg1){
		// 保存触发时间
		 sTime = System.currentTimeMillis() + "";
		// 如果Handler 不为null
		if(iHandler != null && isTriggerMsg){
			// --
			isTriggerMsg = true;
			// 清空旧的消息
			removeMsg();
			// --
			msg = new Message();
			msg.what = FinalConstants.PLAY_START;
			msg.arg1 = arg1;
			msg.obj = sTime; // 保存发送的时间
			iHandler.sendMessageDelayed(msg, delayMillis);
		}
	}
	
	/**
	 * 判断是否播放完成(临近结尾)
	 * @return
	 */
	private boolean isPlayComple(){
		// 获取总进度
		int aTime = ip_seekbar.getMax();
		// 获取当前进度条
		int cTime = ip_seekbar.getProgress();
		// 判断是否接近1秒的时候,是的话则认为是播放结束
		if(aTime >= 1 && cTime >= 1 && aTime - cTime <= 2000){
			return true;
		}
		return false;
//		// 获取播放进度
//		MediaPlayer mPlayer = AlbumMediaManager.getInstance().getMediaPlayer();
//		// 防止为null
//		if(mPlayer == null){
//			// 获取总进度
//			int aTime = ivprap_seekbar.getMax();
//			// 获取当前进度条
//			int cTime = ivprap_seekbar.getProgress();
//			// 判断是否接近1秒的时候,是的话则认为是播放结束
//			if(aTime >= 1 && cTime >= 1 && aTime - cTime <= 1000){
//				return true;
//			}
//			return false;
//		}
//		// 获取总时间
//		int aTime = mPlayer.getDuration();
//		// 获取当前播放进度(播放时间 - 毫秒)
//		int cTime = mPlayer.getCurrentPosition();
//		// 判断是否接近1秒的时候,是的话则认为是播放结束
//		if(aTime >= 1 && cTime >= 1 && aTime - cTime <= 1000){
//			return true;
//		}
//		return false;
	}
	
	/** 判断触发周期(防止连续多次触发) */
	private void isTriggerCycle(){
		/** 是否延迟播放(是否处理完成回调) - 临时状态*/
		boolean isDelayStart = false;
		// 获取时间差
		long dfTime = -1l;
		// --
		if(lastCompletionTime != -1l){
			// 获取当前时间
			long cTime = System.currentTimeMillis();
			// 获取时间差
			dfTime = cTime - lastCompletionTime;
			// 如果当前时间减去触发延迟播放时间大于1秒,则进行处理
			if(dfTime >= 1000){
				isDelayStart = true;
			}
		}
		// 如果不进行处理,这进行打印时间
		if(!isDelayStart){
			// 清空旧的消息
			removeMsg();
			return;
		}
		// --
		// 如果触发播放完成，则延迟进行播放
		if(isDelayStart && !isDestroy){ // 不属于直播结束，或者直播销毁，则进行重新播放
			// 发送播放消息
			sendPlayMsg(2500, 0);
		}
	}
	
	// ================== 播放快捷操作 ======================
	
	/** 暂停播放 */
	public void pausePlayer(){
		MediaManager.getInstance().pause();
	}
	
	/** 停止播放 */
	public void stopPlayer(){
		MediaManager.getInstance().stop();
	}
	
	/**
	 * 开始播放(从头开始加载)
	 * @param uri 播放地址
	 */
	public void startPlayer(String uri){
		// 表示销毁
		isDestroy = true;
		// 停止视频
		destroy();
		// 移除背景
		cleSurfaceBackGround(true);
		// 设置背景
		setSurfaceBackGround(true);
		// 重置操作
		resetOperate();
		// 表示加载中
		videoLoadIng();
		// --
		msgTime = -1l;
		msgNumber = 0;
		// 重置清空异常
		eWhat = -1;
		// --
		if(!TextUtils.isEmpty(uri)){
			// 表示触发消息
			isTriggerMsg = true;
			// 表示非暂停
			isPause = false;
			// 表示播放,非销毁
			isDestroy = false;
			// 表示不属于恢复播放
			isReStart = false;
			// 判断是否本地视频
			isLocalVideo = new File(uri).exists();
			// 异步准备
			MediaManager.getInstance().prePlayer(mContext, uri);
		}
	}
	
	/**
	 * 是否允许恢复播放(自动播放)
	 * @param uri 播放地址
	 * @return 是否可以恢复
	 */
	public boolean startRePlayer(String uri){
		if(!TextUtils.isEmpty(uri)){ // 需要播放的地址,必须不等于null
			// 判断是否本地视频
			isLocalVideo = new File(uri).exists();
			// 获取之前播放路径
			String playUri = MediaManager.getInstance().getPlayUri();
			// 如果不等于null
			if(playUri != null){
				// --
				if(playUri.equals(uri)){
					// 表示属于恢复播放
					isReStart = true;
					// 表示非暂停
					isPause = false;
					// 表示触发消息
					isTriggerMsg = true;
					// 重置清空异常
					eWhat = -1;
					// 移除背景
					cleSurfaceBackGround(true);
					// --
					MediaPlayer mPlayer = MediaManager.getInstance().getMediaPlayer();
					if(mPlayer != null){
						// 表示播放,非销毁
						isDestroy = false;
						// 如果上面四个都要调用，直接用这个
						videoLoadSuc();
						// 表示未经过onPrepared方法
						isPrepared = false;
						// 如果播放中,则暂停播放
						if(mPlayer.isPlaying()){
							// 暂停播放
							mPlayer.pause();
							// 重置操作
							resetOperate();
						} else {
							// 重置操作
							resetOperate();
						}
						if(!isFullScreen){
							//开始播放
							mPlayer.start(); // 暂时不调用播放,防止出现一直调用onError、onCompletion
							// 重置播放时间
							resetTime(mPlayer);
						}
						return true;
					} else { // MediaPlayer 为null，则重新播放
						startPlayer(uri);
					}
				} else { // 播放地址不同，则重新播放
					startPlayer(uri);
				}
			} else { // 如果之前的播放地址为null,则重新播放
				startPlayer(uri);
			}
		}
		return false;
	}
	
	/**
	 * 是否正在播放中
	 * @param uri 播放地址
	 * @return
	 */
	public boolean isPlaying(String uri){
		if(!TextUtils.isEmpty(uri)){ // 需要播放的地址,必须不等于null
			// 获取之前播放路径
			String playUri = MediaManager.getInstance().getPlayUri();
			// 如果不等于null,并且播放地址相同
			if(playUri != null && playUri.equals(uri)){
				destroy(true);
			}
		}
		return false;
	}
	
	// ============ 对外公开方法  ==============
	
	/**
	 * 初始化加载操作
	 * @param url 封面地址
	 * @param isFullScreen 是否全屏
	 * -- isLoadSurCover 是否加载SurfaceView 封面(不传参，默认判断全屏)
	 */
	public void initLoad(String url, boolean isFullScreen){
		if(isFullScreen){ // 如果属于全屏，则不进行加载
			isLoadSurCover = false;
		}
		// 进行初始化加载
		initLoad(url, isFullScreen, isLoadSurCover);
	}
	
	/**
	 * 初始化加载操作
	 * @param url 封面地址
	 * @param isFullScreen 是否全屏
	 * @param isLoadSurCover 是否加载SurfaceView 封面
	 */
	public void initLoad(String url, boolean isFullScreen, boolean isLoadSurCover){
		this.isFullScreen = isFullScreen;
		this.isLoadSurCover = isLoadSurCover;
		// --
		// 加载封面
		loadCover(url);
		// 重置大小
		resetViewWidthHeight();
		// 默认显示属于加载中(非全屏中)
		if(!isFullScreen){
			videoLoadIng();
		}
	}
	
	/**
	 * 关闭播放操作 - 显示封面
	 */
	public void closePlayer(){
		// 进行销毁
		destroy();
	}
	
	/**
	 * 进入onStop方法操作
	 */
	public void joinOnStop(){
		joinOnStop(true);
	}
	
	/**
	 * 进入onStop方法操作
	 * @param isPause 是否暂停
	 */
	public void joinOnStop(boolean isPause){
		// 表示暂停了
		this.isPause = true;
		// 保存时间
		lastExitTime = System.currentTimeMillis();
		// --
		// 关闭加载定时器
		loadTimer(false);
		// 关闭播放定时器
		playTimer(false);
		// 需要暂停,或者非切换全屏,则进行暂停
		if(isPause || !isToggleFullScreen){
			pausePlayer();
		}
	}
	
	/**
	 * 销毁操作
	 */
	public void destroy(){
		destroy(true);
	}
	
	/**
	 * 销毁操作
	 * @param isStop 是否停止播放操作并且销毁资源
	 */
	public void destroy(boolean isStop){
		// 表示属于销毁
		isDestroy = true;
		// 清空旧的消息
		removeMsg();
		// --
		// 关闭加载定时器
		loadTimer(false);
		// 关闭播放定时器
		playTimer(false);
		// 清空图片资源
		try {
			ip_surfaceview.setBackgroundDrawable(null);
		} catch (Exception e) {
		}
		// 清空资源
		drawable = null;
		if(isStop){ // 关闭播放资源
			stopPlayer();
		}
	}
	
	/** 触发返回键 */
	public void goBack(){
		// 表示属于销毁
		isDestroy = true;
		// 清空旧的消息
		removeMsg();
		// --
		// 关闭加载定时器
		loadTimer(false);
		// 关闭播放定时器
		playTimer(false);
		// 暂停播放
		pausePlayer();
	}
	
	/**
	 * 进入Activity 回传方法，重置时间
	 */
	public void joinOnResult(){
		// 重新设置时间(离开时间 - 触发onStop)
		lastExitTime = System.currentTimeMillis();
		// 重置触发onCompletion()时间
		lastCompletionTime = System.currentTimeMillis();
	}
	
	/**
	 * 判断离开时间是否过长
	 * @return 是否重新加载
	 */
	public boolean judgeExitTime(){
		// 重置触发onCompletion()时间
		lastCompletionTime = System.currentTimeMillis();
		// --
		if(lastExitTime != -1l){
			// 当前时间 - 离开时间，大于15秒,则表示需要重新加载
			if(System.currentTimeMillis() - lastExitTime >= 15000l){
				stopPlayer();
				return true;
			}
		}
		// 重置时间
		lastExitTime = -1l;
		// --
		return false;
	}
	
	/**
	 * 获取播放View最外层FrameLayout
	 */
	public FrameLayout getVideoView(){
		return ip_frame;
	}
	
	/**
	 * 设置播放View （最外层FrameLayout） 显示状态
	 * @param isDisplay
	 */
	public void setVideoView(boolean isDisplay){
		ip_frame.setVisibility(isDisplay ? View.VISIBLE : View.GONE);
	}
	
	/**
	 * 是否显示播 放中
	 * @return
	 */
	public boolean isVideoView(){
		return (ip_frame.getVisibility() == View.VISIBLE);
	}
	
	/**
	 * 判断是否切换全屏
	 * @return
	 */
	public boolean isToggleFullScreen() {
		return isToggleFullScreen;
	}

	/**
	 * 标识状态，表示是否切换全屏
	 * @param isToggleFullScreen
	 */
	public void setToggleFullScreen(boolean isToggleFullScreen) {
		this.isToggleFullScreen = isToggleFullScreen;
	}
	
	/**
	 * 设备封面图片
	 * @param draw
	 */
	public void setDrawable(Drawable draw){
		if(draw == null){
			return;
		}
		this.drawable = draw;
		// 设置背景 - 防止播放背景不会清空
		setSurfaceBackGround(true);
		try {
			// 设置图片背景
			ip_bg_igview.setBackgroundDrawable(drawable);
		} catch (Exception e) {
		}
	}
	
	/**
	 * 获取播放时间
	 * @return 毫秒
	 */
	public int getPlayTime(){
		return ip_seekbar.getProgress();
	}
}
