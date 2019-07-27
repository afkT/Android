package com.play.pro;

import com.play.pro.R;
import com.play.pro.constants.FinalConstants;
import com.play.pro.player.PlayerControl;
import com.play.pro.utils.ProUtils;
import com.play.pro.utils.TrimVideoUtils;
import com.play.pro.widgets.VideoSeekBar;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends Activity {

	/** 上下文 */
	private Context mContext;
	/** 日志Tag */
	private final String TAG = "MainActivity";
	/** 相册播放器控制器 */
	private PlayerControl playerControl;
	/** 视频剪辑View */
	public VideoSeekBar am_video_seekbar;
	/** 视频裁剪View */
	public ImageView am_cut_igview;
	// --
	/** 封面地址 */
	public static String COVER_URL = "http://www.3lian.com/d/file/201701/03/78e2d5cdc24c6cb8560f30ccdde63519.jpg";
	/** 播放地址 */
	public static String PLAY_URL = "";

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
		// 开始监听
		receiverHomeKeyBind(true);
		// 判断离开时间是否过长
		boolean isStopPlayer = playerControl.judgeExitTime();
		// 是否停止播放
		Log.d(TAG, "isStopPlayer : " + isStopPlayer);
		// 判断是否播放中
		if(playerControl.isVideoView()){
			// 防止资源为null,或者路径不存在
			if(TextUtils.isEmpty(PLAY_URL)){
				// 销毁播放资源
				playerControl.destroy();
			} else { // 开始播放
				vHandler.sendEmptyMessage(FinalConstants.PLAY_START);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// 取消监听
		receiverHomeKeyBind(false);
		// 如果不是跳转全屏才进行暂停
		if (!playerControl.isToggleFullScreen()){
			// 停止播放
			playerControl.joinOnStop();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 停止视频销毁资源
		playerControl.destroy();
		// 销毁资源
		am_video_seekbar.destroy();
	}

	public void initViews(){
		// 初始化View
		am_video_seekbar = (VideoSeekBar) this.findViewById(R.id.am_video_seekbar);
		am_cut_igview = (ImageView) this.findViewById(R.id.am_cut_igview);
	}
	
	public void initValues(){
		try {
			//检测是否有写的权限
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				// 没有写的权限，去申请写的权限，会弹出对话框
				ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 10);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 初始化播放控制器
		playerControl = new PlayerControl(this, vHandler);
		// 根目录
		String rootPath = ProUtils.getSDCartPath();
		// 本地视频
		String videoUrl = rootPath + "/a.mp4";
		// --
		// videoUrl = rootPath + "/b.mp4";
		// 重新赋值地址
		PLAY_URL = videoUrl;
		// 加载视频封面
		playerControl.initLoad(COVER_URL, false);
		// 开始播放
		vHandler.sendEmptyMessage(FinalConstants.PLAY_START);
		
		// ====== 视频剪辑View  ======
		// 进行重置
		am_video_seekbar.reset();
//		// 是否需要绘制进度 - 白色进度动,以及走过的画面背景变暗 - 统一控制setProgressLine(isDrawProgress), setProgressBG(isDrawProgress)
//		am_video_seekbar.setProgressDraw(isDrawProgress);
//		// 是否需要绘制进度 - 播放中,有个白色的线条在动
//		am_video_seekbar.setProgressLine(isDrawProgressLine);
//		// 是否需要绘制进度 - 播放过的画面背景变暗
//		am_video_seekbar.setProgressBG(isDrawProgressBG);
//		// 是否属于裁剪模式 - 两边有进度滑动
//		am_video_seekbar.setCutMode(isCutMode);
//		// 是否属于裁剪模式 - 是否绘制非裁剪模块变暗
//		am_video_seekbar.setCutMode(isCutMode, isDrawProgressLine);
//		// 视频关键帧间隔(毫秒,表示左右两个模块最低限度滑动时间,无法选择低于该关键帧的裁剪时间)
//		float videoFrame = 60 * 1000f;
//		// 设置本地视频路径 - 默认裁剪模式,则不绘制播放背景
//		am_video_seekbar.setVideoUri(true, PLAY_URL, videoFrame);
//		// 不设置关键帧时间,则默认最多是两个ImageView左右多出的宽度
//		am_video_seekbar.setVideoUri(isCutMode, videoUri);

		// =========================================
		// 计算关键帧可能会卡顿一下,最好是在后台运行
		// =========================================
		// 获取视频关键帧间隔 - 如果获取失败,则默认最少需要裁剪3秒长度的视频
		float videoFrame = (float) TrimVideoUtils.getInstance().reckonFrameTime(new File(videoUrl), 3000);
		// 设置本地视频路径 - 默认裁剪模式,则不绘制播放背景
		am_video_seekbar.setVideoUri(true, PLAY_URL, videoFrame);
		// --
		Toast.makeText(MainActivity.this, "视频关键帧：" + videoFrame, Toast.LENGTH_SHORT).show();
	}
	
	public void initListener(){
		// 点击视频裁剪
		am_cut_igview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 禁止点击
				am_cut_igview.setEnabled(false);
				// --
				TrimVideoUtils trimVideoUtils = TrimVideoUtils.getInstance();
				trimVideoUtils.setTrimCallBack(new TrimVideoUtils.TrimFileCallBack() {
					@Override
					public void trimError(int eType) {
						Message msg = new Message();
						msg.what = TrimVideoUtils.TRIM_FAIL;
						switch(eType){
							case TrimVideoUtils.FILE_NOT_EXISTS: // 文件不存在
								msg.obj = "视频文件不存在";
								break;
							case TrimVideoUtils.TRIM_STOP: // 手动停止裁剪
								msg.obj = "停止裁剪";
								break;
							case TrimVideoUtils.TRIM_FAIL:
							default: // 裁剪失败
								msg.obj = "裁剪失败";
								break;
						}
						cutHandler.sendMessage(msg);
					}
					@Override
					public void trimCallback(boolean isNew, int startS, int endS, int vTotal, File file, File trimFile) {
						/**
						 * 裁剪回调
						 * @param isNew 是否新剪辑
						 * @param starts 开始时间(秒)
						 * @param ends 结束时间(秒)
						 * @param vTime 视频长度
						 * @param file 需要裁剪的文件路径
						 * @param trimFile 裁剪后保存的文件路径
						 */
						// ===========
						System.out.println("isNew : " + isNew);
						System.out.println("startS : " + startS);
						System.out.println("endS : " + endS);
						System.out.println("vTotal : " + vTotal);
						System.out.println("file : " + file.getAbsolutePath());
						System.out.println("trimFile : " + trimFile.getAbsolutePath());
						// --
						cutHandler.sendEmptyMessage(TrimVideoUtils.TRIM_SUCCESS);
					}
				});
				// 需要裁剪的视频路径
				String videoPath = PLAY_URL;
				// 保存的路径
				String savePath = ProUtils.getSDCartPath() + File.separator  + System.currentTimeMillis() + "_cut.mp4";
				// ==
				final File file = new File(videoPath); // 视频文件地址
				final File trimFile = new File(savePath);// 裁剪文件保存地址
				// 获取开始时间
				final int startS = (int) am_video_seekbar.getStartTime() / 1000;
				// 获取结束时间
				final int endS = (int) am_video_seekbar.getEndTime() / 1000;
				// 进行裁剪
				new Thread(new Runnable() {
					@Override
					public void run() {
						try { // 开始裁剪
							TrimVideoUtils.getInstance().startTrim(true, startS, endS, file, trimFile);
						} catch (Exception e) {
							e.printStackTrace();
							// 设置回调为null
							TrimVideoUtils.getInstance().setTrimCallBack(null);
						}
					}
				}).start();
				// --
				Toast.makeText(MainActivity.this, "开始裁剪 - 开始: " + startS  + "秒, 结束: " + endS + "秒", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	/**
	 * 广播Home键监听绑定
	 * @param isBind 是否绑定
	 */
	private void receiverHomeKeyBind(boolean isBind){
		try {
			if(isBind){
				// 注册广播
				registerReceiver(mHomeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
			} else {
				// 注册广播
				unregisterReceiver(mHomeKeyEventReceiver);
			}			
		} catch (Exception e) {
		}
	}
	
	/** Home键监听 */
	private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();  
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				// 停止播放
				playerControl.joinOnStop();
			}
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/** 判断回调类型 */
		switch(requestCode){
		/** 全屏回调 */
		case FinalConstants.FULL_SCREEN:
			// 重置时间
			playerControl.joinOnResult();
			// --
			if(data != null){
				// 判断是否播放结束
				boolean isPlayComple = data.getBooleanExtra(FinalConstants.IS_PLAY_FINISH, true);
				// 判断是否点击Home键
				boolean isClickHome = data.getBooleanExtra(FinalConstants.IS_CLICK_HOME, true);
				// 如果是播放结束，则进行隐藏
				if(isPlayComple){ // 销毁播放资源
					playerControl.destroy();
				} else if (isClickHome){ // 停止播放
					playerControl.joinOnStop();
				} else { // 表示播放中
				}
			}
			break;
		}
	}
	
	// -----------------------------
	
	/**
	 * 专门修改View 的Handler
	 */
	private Handler vHandler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			super.dispatchMessage(msg);
			switch(msg.what){
			/** 暂停播放 */
			case FinalConstants.PLAY_PAUSE:
				break;
			/** 播放结束 */
			case FinalConstants.PLAY_COMPLE:
				break;
			/** 开始播放触发 */
			case FinalConstants.PLAY_START:
				if (MainActivity.this.isFinishing()) {
					return;
				}
				// 防止资源为null,或者路径不存在
				if(TextUtils.isEmpty(PLAY_URL)){
					// 销毁播放资源
					playerControl.destroy();
					return;
				}
				// 设置视频封面
				// aPlayerControl.setDrawable(drawable);
				// 加载视频封面
				// aPlayerControl.initLoad(COVER_URL, false);
				// 默认等于0，表示恢复播放
				if(msg.arg1 == 0){ // 恢复播放
					playerControl.startRePlayer(PLAY_URL);
				} else { // 从头开始播放
					playerControl.startPlayer(PLAY_URL);
				}
				break;
			/** 点击全屏 */
			case FinalConstants.FULL_SCREEN:
				// 标识属于切换全屏
				playerControl.setToggleFullScreen(true);
				// 暂停视频
				playerControl.joinOnStop();
				// --
				Intent intent = new Intent(mContext, FullScreenActivity.class);
				intent.putExtra(FinalConstants.VIDEO_URL, PLAY_URL);
				intent.putExtra(FinalConstants.COVER_URL, COVER_URL);
				MainActivity.this.startActivityForResult(intent, FinalConstants.FULL_SCREEN);
				break;
			/** 播放时间改变 */
			case FinalConstants.PLAY_TIME_CHANGE:
				// 是否需要绘制进度 - 白色进度动,以及走过的画面背景变暗
				//am_video_seekbar.setProgressDraw(true);
				// 设置时间
				//am_video_seekbar.setProgress(playerControl.getPlayTime());
				break;
			}
		}
	};

	/**
	 * 裁剪处理 Handler
	 */
	private Handler cutHandler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			super.dispatchMessage(msg);
			switch(msg.what){
				case TrimVideoUtils.TRIM_FAIL: // 裁剪失败
					Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
					// --
					am_video_seekbar.setEnabled(true);
					break;
				case TrimVideoUtils.TRIM_SUCCESS: // 裁剪成功
					Toast.makeText(MainActivity.this, "裁剪成功", Toast.LENGTH_SHORT).show();
					// --
					am_video_seekbar.setEnabled(true);
					break;
			}
		}
	};
}
