package com.play.pro;

import com.play.pro.R;
import com.play.pro.constants.FinalConstants;
import com.play.pro.player.PlayerControl;
import com.play.pro.utils.ProUtils;
import com.play.pro.widgets.VideoSeekBar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class MainActivity extends Activity {

	/** 上下文 */
	private Context mContext;
	/** 日志Tag */
	private final String TAG = "MainActivity";
	/** 相册播放器控制器 */
	private PlayerControl playerControl;
	/** 视频剪辑View */
	public static VideoSeekBar am_video_seekbar;
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
	}
	
	public void initValues(){
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
//		//// 是否需要绘制进度 - 播放中,有个白色的线条在动
//		am_video_seekbar.setProgressLine(isDrawProgressLine);
//		// 是否需要绘制进度 - 播放过的画面背景变暗
//		am_video_seekbar.setProgressBG(isDrawProgressBG);
//		// 是否属于裁剪模式 - 两边有进度滑动
//		am_video_seekbar.setCutMode(isCutMode);
//		// 是否属于裁剪模式 - 是否绘制非裁剪模块变暗
//		am_video_seekbar.setCutMode(isCutMode, isDrawProgressLine);
		// 视频关键帧间隔(毫秒,表示左右两个模块最低限度滑动时间,无法选择低于该关键帧的裁剪时间)
		float videoFrame = 60 * 1000f;
		// 设置本地视频路径 - 默认裁剪模式,则不绘制播放背景
		am_video_seekbar.setVideoUri(true, PLAY_URL, videoFrame);
//		// 不设置关键帧时间,则默认最多是两个ImageView左右多出的宽度
//		am_video_seekbar.setVideoUri(isCutMode, videoUri);
	}
	
	public void initListener(){
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
}
