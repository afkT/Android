package com.record.video.utils;

import android.content.Context;
import android.widget.Toast;

import dev.logger.DevLogger;

/**
 * detail: 自定义Toast工具类
 * Created by Ttt
 */
public final class ToastUtils {

	private ToastUtils() {
	}

	// 日志TAG
	private static final String TAG = ToastUtils.class.getSimpleName();
	/** 内部持有唯一 */
	private static Toast mToast;

	// ========================
	// == Toast.LENGTH_SHORT ==
	// ========================

	public static void showToast(Context mContext, String text) {
		handlerToastStr(mContext, text, Toast.LENGTH_SHORT);
	}

	public static void showToast(Context mContext, String text, Object... objs) {
		handlerToastStr(mContext, text, Toast.LENGTH_SHORT, objs);
	}

	public static void showToast(Context mContext, int resId) {
		handlerToastRes(mContext, resId, Toast.LENGTH_SHORT);
	}

	public static void showToast(Context mContext, int resId, Object... objs) {
		handlerToastRes(mContext, resId, Toast.LENGTH_SHORT, objs);
	}

	// ========================
	// == Toast.LENGTH_LONG ===
	// ========================

	public static void showToastLong(Context mContext, String text) {
		handlerToastStr(mContext, text, Toast.LENGTH_LONG);
	}

	public static void showToastLong(Context mContext, String text, Object... objs) {
		handlerToastStr(mContext, text, Toast.LENGTH_LONG, objs);
	}

	public static void showToastLong(Context mContext, int resId) {
		handlerToastRes(mContext, resId, Toast.LENGTH_LONG);
	}

	public static void showToastLong(Context mContext, int resId, Object...objs) {
		handlerToastRes(mContext, resId, Toast.LENGTH_LONG, objs);
	}

	// =======================
	// ==== 最终Toast方法 ====
	// =======================

	public static void showToast(Context mContext, int resId, int duration) {
		handlerToastRes(mContext, resId, duration);
	}

	/**
	 * 最终显示的Toast方法
	 * @param mContext
	 * @param text
	 * @param duration
	 */
	public static void showToast(Context mContext, String text, int duration) {
		// 尽心设置为null, 便于提示排查
		if (text == null) {
			text = "null";
		}
		if (mToast != null) {
			mToast.setDuration(duration);
			mToast.setText(text);
		} else {
			if (mContext != null) {
				try {
					mToast = Toast.makeText(mContext, text, duration);
				} catch (Exception e) {
					e.printStackTrace();
					// --
					DevLogger.eTag(TAG, e, "showToast - 最终显示的Toast方法");
				}
			}
		}
		// 处理后,不为null,才进行处理
		if (mToast != null) {
			mToast.show();
		}
	}

	// =====

	/**
	 * 处理 R.string 资源Toast的格式化
	 * @param mContext
	 * @param resId
	 * @param duration
	 * @param objs
	 */
	private static void handlerToastRes(Context mContext, int resId, int duration, Object... objs) {
		if (mContext != null) {
			// String text = "resId : " + resId + " not find";
			String text;
			try {
				// 获取字符串并且进行格式化
				if (objs != null && objs.length != 0) {
					text = mContext.getString(resId, objs);
				} else {
					text = mContext.getString(resId);
				}
			} catch (Exception e) {
				text = e.getMessage();
			}
			showToast(mContext, text, duration);
		}
	}

	/**
	 * 处理字符串Toast的格式化
	 * @param mContext
	 * @param text
	 * @param duration
	 * @param objs
	 */
	private static void handlerToastStr(Context mContext, String text, int duration, Object... objs) {
		// 防止上下文为null
		if (mContext != null) {
			// 表示需要格式化字符串,只是为了减少 format步骤,增加判断，为null不影响
			if (objs != null && objs.length != 0) {
				if (text != null) { // String.format() 中的 objs 可以为null,但是 text不能为null
					showToast(mContext, String.format(text, objs), duration);
				} else {
					showToast(mContext, text, duration);
				}
			} else {
				showToast(mContext, text, duration);
			}
		}
	}
}
