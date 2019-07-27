package com.play.pro.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * 自定义Toast工具类,防止用户快速操作导致Dialog多次显示
 */
public class ToastUtils {

	/** 系统默认延时 */
	private static Toast mToast;

	public static void showToast(Context mContext, String text, int duration) {
		if (mToast != null) {
			mToast.setText(text);
			mToast.setDuration(duration);
		} else {
			mToast = Toast.makeText(mContext, text, duration);
		}
		mToast.show();
	}

	public static void showToast(Context mContext, String text) {
		showToast(mContext, text, Toast.LENGTH_SHORT);
	}

	public static void showToast(Context mContext, int resId) {
		showToast(mContext, mContext.getResources().getString(resId), Toast.LENGTH_SHORT);
	}

	public static void showToast(Context mContext, int resId, Object... obj) {
		showToast(mContext, mContext.getResources().getString(resId, obj), Toast.LENGTH_SHORT);
	}

	public static void showToast(Context mContext, int resId, int duration) {
		showToast(mContext, mContext.getResources().getString(resId), duration);
	}
}
