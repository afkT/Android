package com.play.pro;

import com.play.pro.R;
import com.play.pro.constants.FinalConstants;
import com.play.pro.player.PlayerControl;
import com.play.pro.utils.ToastUtils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;

/**
 * 全屏播放Activity
 */
public class FullScreenActivity extends Activity implements OnClickListener {

	/** 相册播放引用布局控制器 */
	private PlayerControl aPlayerControl;
	/** 封面地址*/
	private String coverUrl = null;
	/** 视频地址 */
	private String videoUrl = null;
	// ========== View ==============
	/** 返回键 */
	private ImageView afs_back_igview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置无标题  
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置全屏  
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 加载Layout
		setContentView(R.layout.activity_full_screen);
		// ================
		// 进行解析数据
		Intent intent = getIntent();
		if(intent != null){
			// 获取封面地址(本地视频 - 则为null)
			coverUrl = intent.getStringExtra(FinalConstants.COVER_URL);
			try {
				// 获取视频地址
				videoUrl = intent.getStringExtra(FinalConstants.VIDEO_URL);
			} catch (Exception e) {
				videoUrl = null;
			}
		}
		// --
		if(videoUrl == null){
			// 提示获取数据失败
			ToastUtils.showToast(this, "获取数据失败");
			// 关闭当前页面
			finish();
			return;
		}
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
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	/**
	 * 重写返回键
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) { // 判断是否返回操作
			vHandler.sendEmptyMessage(FinalConstants.BACK);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.afs_back_igview: // 手动点击返回
			vHandler.sendEmptyMessage(FinalConstants.BACK);
			break;
		}
	}

	public void initViews(){
		afs_back_igview = (ImageView) this.findViewById(R.id.afs_back_igview);
	}
	
	public void initValues(){
		// 初始化播放控制器
		aPlayerControl = new PlayerControl(this, vHandler);
		// 进行初始化
		aPlayerControl.initLoad(coverUrl, true);
		// 开始播放
		aPlayerControl.startRePlayer(videoUrl);
		// 进行播放全屏 - 用这句会导致延迟无调用surfaceCreate
		//vHandler.sendEmptyMessage(FinalConstants.START_PLAYER);
	}
	
	public void initListener(){
		afs_back_igview.setOnClickListener(this);
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
				// 表示按了Home键,不属于播放结束
				finishAsk(true, false);
			}
		}
	};
	
	/**
	 * 关闭页面回传数据
	 * @param isClickHome 是否点击Home键
	 * @param isPlayComple 是否播放结束
	 */
	private void finishAsk(boolean isClickHome, boolean isPlayComple){
		try {
			// 关闭监听
			receiverHomeKeyBind(false);
		} catch (Exception e) {
		}
		if(isPlayComple){ // 如果属于播放结束
			// 进行销毁
			aPlayerControl.destroy();
		} else if (isClickHome){ // 是否点击Home键
			// 暂停视频
			aPlayerControl.joinOnStop(true);
			// 销毁资源
			aPlayerControl.destroy(false);
		}
		// -- 
		Intent intent = new Intent();
		// 是否点击了Home键
		intent.putExtra(FinalConstants.IS_CLICK_HOME, isClickHome);
		// 是否播放结束
		intent.putExtra(FinalConstants.IS_PLAY_FINISH, isPlayComple);
		// 正常成功
		FullScreenActivity.this.setResult(Activity.RESULT_OK,intent);
		// 关闭当前页面
		FullScreenActivity.this.finish();
	}
	
	// -----------------------------
	
	/** 专门修改View 的Handler */
	private Handler vHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			/** 点击了返回键 */
			case FinalConstants.BACK:
			/** 点击全屏 */
			case FinalConstants.FULL_SCREEN:
				// 表示非按Home键, 也不属于播放结束
				finishAsk(false, false);
				break;
			/** 开始播放触发 */
			case FinalConstants.PLAY_START:
				if(FullScreenActivity.this.isFinishing()){
					return;
				}
				// 判断重新开始播放
				if (msg.arg2 == FinalConstants.PLAY_START){
					aPlayerControl.startPlayer(videoUrl);
				} else { // 恢复播放
					aPlayerControl.startRePlayer(videoUrl);
				}
				break;
			/** 播放结束通知 */
			case FinalConstants.PLAY_COMPLE:
				// 表示非按Home键, 属于播放结束
				finishAsk(false, true);
				break;
			}
		}
	};
}