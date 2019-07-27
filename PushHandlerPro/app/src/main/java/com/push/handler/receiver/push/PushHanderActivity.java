package com.push.handler.receiver.push;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.push.handler.utils.LogPrintUtils;

/**
 * detail: 推送消息处理Activity (中转处理) - 不需要继承BaseActivity
 * Created by Ttt
 * 中转处理, 不需要显示页面, 直接进行逻辑判断
 */
public class PushHanderActivity extends Activity {

	// 日志TAG
	private static final String TAG = PushHanderActivity.class.getSimpleName();
	/** 判断是否点击了推送通知栏消息 */
	private static final String IS_CLICK_PUSH_MSG = "isClickPushMsg";
	/** 推送消息 */
	private static final String PUSH_MSG = "pushMsg";
	/** 推送类型 */
	private static final String PUSH_TYPE = "pushType";
	/** LAUNCHER 页面 Class */
	private static Class LAUNCHER_CLASS = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 进行处理点击通知栏推送消息
		handlerPushOp(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// 进行处理点击通知栏推送消息`
		handlerPushOp(intent);
	}

	// =====================================================

	/**
	 * 处理推送操作
	 * @param intent 获取传递的数据
	 */
	private void handlerPushOp(Intent intent) {
		if (LAUNCHER_CLASS == null){
			return;
		}
		// 推送消息
		String pushMsg = null;
		// 推送类型
		String pushType = null;
		// 判读是否存在数据
		if(intent != null){
			// 获取推送消息
			pushMsg = intent.getStringExtra(PushHanderActivity.PUSH_MSG);
			// 获取推送消息
			pushType = intent.getStringExtra(PushHanderActivity.PUSH_TYPE);
		}
		// 保存状态 - 是否存在推送消息
		putBoolean(PushHanderActivity.this, PushHanderActivity.IS_CLICK_PUSH_MSG, true);
		// 保存数据 - 推送消息
		putString(PushHanderActivity.this, PushHanderActivity.PUSH_MSG, pushMsg);
		// 保存数据 - 推送类型
		putString(PushHanderActivity.this, PushHanderActivity.PUSH_TYPE, pushType);
		// 关闭当前页面
		finish();
		// =================  进行跳转  =================
		Intent pIntent = new Intent(Intent.ACTION_MAIN);
		pIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		pIntent.setComponent(new ComponentName(PushHanderActivity.this.getPackageName(), LAUNCHER_CLASS.getCanonicalName()));
		pIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		startActivity(pIntent);
		// ===============================================
	}

	/**
	 * 检查推送操作
	 * @param activity
	 */
	private static void checkPushHandler(Activity activity){
		if (activity != null){
			try {
				// 判断是否点击推送消息 - 必须点击了才进行处理
				if(getBoolean(activity, PushHanderActivity.IS_CLICK_PUSH_MSG, false)){
					// 获取推送消息
					String pushMsg = getString(activity, PushHanderActivity.PUSH_MSG, null);
					// 获取推送类型
					String pushType = getString(activity, PushHanderActivity.PUSH_TYPE, null);
					// 清空全部
					clear(activity);
					// 判断是否处理
					if (pushHandler != null){
						pushHandler.onPushHandler(activity, pushMsg, pushType);
					}
				}
			} catch (Exception e) {
				LogPrintUtils.eTag(TAG, e, "checkPushHandler");
			}
		}
	}

	// == 对外提供方法 ==

	/**
	 * 检查推送
	 * @param activity
	 * @param value
     */
	public static void checkPush(Activity activity, String value){
		if (pushHandler != null){
			// 判断是否处理推送
			if (pushHandler.isHandlerPush(value)){
				// 检查推送处理
				checkPushHandler(activity);
			}
		}
	}

	/**
	 * 获取跳转Intent
	 * @param context
	 * @param pushMsg 推送消息
	 * @param pushType 推送类型
	 * @return
	 */
	public static Intent getIntent(Context context, String pushMsg, String pushType){
		try {
			Intent intent = new Intent(context, PushHanderActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(PushHanderActivity.PUSH_MSG, pushMsg);
			intent.putExtra(PushHanderActivity.PUSH_TYPE, pushType);
			return intent;
		} catch (Exception e){
			LogPrintUtils.eTag(TAG, e, "getIntent");
		}
		return null;
	}

	/**
	 * 跳转到推送, 中转处理页面
	 * @param context
	 * @param pushMsg 推送消息
	 * @param pushType 推送类型
	 */
	public static void startPushHandler(Context context, String pushMsg, String pushType){
		try {
			context.startActivity(getIntent(context, pushMsg, pushType));
		} catch (Exception e){
			LogPrintUtils.eTag(TAG, e, "startPushHandler");
		}
	}

	/**
	 * 设置 android.intent.category.LAUNCHER 类
	 * @param launcherClass
	 */
	public static void setLauncherClass(Class launcherClass) {
		PushHanderActivity.LAUNCHER_CLASS = launcherClass;
	}

	// == 接口 ==

	// 推送判断处理接口
	private static IPushHandler pushHandler;

	/**
	 * 设置推送判断处理接口
	 * @param pushHandler
	 */
	public static void setPushHandler(IPushHandler pushHandler) {
		PushHanderActivity.pushHandler = pushHandler;
	}

	/**
	 * detail: 推送处理接口
	 * Created by Ttt
	 */
	public interface IPushHandler {

		/**
		 * 是否处理推送
		 * @param value
		 * @return
		 */
		boolean isHandlerPush(String value);

		/**
		 * 推送处理方法
		 * @param activity
		 * @param pushData 推送数据
		 * @param pushType 推送类型
		 */
		void onPushHandler(Activity activity, String pushData, String pushType);
	}

	// == 内部 SP 操作 ==

	private static SharedPreferences getSP(Context context) {
		SharedPreferences sp = context.getSharedPreferences("push_config", Context.MODE_PRIVATE);
		return sp;
	}

	private static boolean getBoolean(Context context, String key, boolean defValue){
		return getSP(context).getBoolean(key, defValue);
	}

	private static void putBoolean(Context context, String key, boolean value){
		getSP(context).edit().putBoolean(key, value).commit();
	}

	private static String getString(Context context, String key, String defValue){
		return getSP(context).getString(key, defValue);
	}

	private static void putString(Context context, String key, String value){
		getSP(context).edit().putString(key, value).commit();
	}

	private static void clear(Context context){
		getSP(context).edit().clear().commit();
	}
}
