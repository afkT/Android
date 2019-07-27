package sophix.pro.utils;

import android.content.Context;
import android.widget.Toast;

import sophix.pro.base.BaseApplication;

/**
 * detail: 自定义Toast工具类
 * Created by Ttt
 */
public final class ToastUtils {

	private ToastUtils() {
	}

	/** 内部持有唯一 */
	private static Toast mToast = null;

	/**
	 * 内部处理防止Context 为null奔溃问题
	 * @return
	 */
	private static Context getContext(Context mContext){
		if (mContext != null){
			return mContext;
		} else {
			// 设置全局Context
			return BaseApplication.getContext();
		}
	}

	/**
	 * 获取内部唯一Toast对象
	 * @return
	 */
	public static Toast getSignleToast(){
		return mToast;
	}

	// =====================
	// === 统一显示Toast ===
	// =====================

	// ========================
	// == Toast.LENGTH_SHORT ==
	// ========================

	public static Toast showShort(Context mContext, String text) {
		return handlerToastStr(true, mContext, text, Toast.LENGTH_SHORT);
	}

	public static Toast showShort(Context mContext, String text, Object... objs) {
		return handlerToastStr(true, mContext, text, Toast.LENGTH_SHORT, objs);
	}

	public static Toast showShort(Context mContext, int resId) {
		return handlerToastRes(true, mContext, resId, Toast.LENGTH_SHORT);
	}

	public static Toast showShort(Context mContext, int resId, Object... objs) {
		return handlerToastRes(true, mContext, resId, Toast.LENGTH_SHORT, objs);
	}

	// ========================
	// == Toast.LENGTH_LONG ===
	// ========================

	public static Toast showLong(Context mContext, String text) {
		return handlerToastStr(true, mContext, text, Toast.LENGTH_LONG);
	}

	public static Toast showLong(Context mContext, String text, Object... objs) {
		return handlerToastStr(true, mContext, text, Toast.LENGTH_LONG, objs);
	}

	public static Toast showLong(Context mContext, int resId) {
		return handlerToastRes(true, mContext, resId, Toast.LENGTH_LONG);
	}

	public static Toast showLong(Context mContext, int resId, Object...objs) {
		return handlerToastRes(true, mContext, resId, Toast.LENGTH_LONG, objs);
	}

	// =======================
	// ==== 最终Toast方法 ====
	// =======================

	/**
	 * 显示Toast
	 * @param mContext
	 * @param resId
	 * @param duration
	 */
	public static Toast showToast(Context mContext, int resId, int duration) {
		return handlerToastRes(true, mContext, resId, duration);
	}

	/**
	 * 显示Toast
	 * @param mContext
	 * @param text
	 * @param duration
	 */
	public static Toast showToast(Context mContext, String text, int duration) {
		return showToast(true, mContext, text, duration);
	}

	// ==

	/**
	 * 最终显示的Toast方法
	 * @param isSingle
	 * @param mContext
	 * @param text
	 * @param duration
	 * @return Toast
	 */
	private static Toast showToast(boolean isSingle, Context mContext, String text, int duration) {
		// 尽心设置为null, 便于提示排查
		if (text == null) {
			text = "null";
		}
		// 判断是否显示唯一, 单独共用一个
		if (isSingle) {
			try {
				if (mToast != null) {
					mToast.setDuration(duration);
					mToast.setText(text);
				} else {
					if (mContext != null) {
						mToast = Toast.makeText(mContext, text, duration);
					}
				}
				// 处理后,不为null,才进行处理
				if (mToast != null) {
					mToast.show();
				}
			} catch (Exception e){
			}
			return mToast;
		} else {
			Toast toast = null;
			try {
				toast = Toast.makeText(mContext, text, duration);
				toast.show();
			} catch (Exception e){
			}
			return toast;
		}
	}

	// =====

	/**
	 * 处理 R.string 资源Toast的格式化
	 * @param isSingle 是否单独共用显示一个
	 * @param mContext
	 * @param resId
	 * @param duration
	 * @param objs
	 */
	private static Toast handlerToastRes(boolean isSingle, Context mContext, int resId, int duration, Object... objs) {
		if (getContext(mContext) != null) {
			String text;
			try {
				// 获取字符串并且进行格式化
				if (objs != null && objs.length != 0) {
					text = getContext(mContext).getString(resId, objs);
				} else {
					text = getContext(mContext).getString(resId);
				}
			} catch (Exception e) {
				text = e.getMessage();
			}
			return showToast(isSingle, mContext, text, duration);
		}
		return null;
	}

	/**
	 * 处理字符串Toast的格式化
	 * @param mContext
	 * @param text
	 * @param duration
	 * @param objs
	 */
	private static Toast handlerToastStr(boolean isSingle, Context mContext, String text, int duration, Object... objs) {
		// 防止上下文为null
		if (mContext != null) {
			// 表示需要格式化字符串,只是为了减少 format步骤,增加判断，为null不影响
			if (objs != null && objs.length != 0) {
				if (text != null) { // String.format() 中的 objs 可以为null,但是 text不能为null
					try {
						return showToast(isSingle, mContext, String.format(text, objs), duration);
					} catch (Exception e){
						return showToast(isSingle, mContext, e.getMessage(), duration);
					}
				} else {
					return showToast(isSingle, mContext, text, duration);
				}
			} else {
				return showToast(isSingle, mContext, text, duration);
			}
		}
		return null;
	}

	// =====================
	// == 非统一显示Toast ==
	// =====================

	// ========================
	// == Toast.LENGTH_SHORT ==
	// ========================

	public static Toast showShortNew(Context mContext, String text) {
		return handlerToastStr(false, mContext, text, Toast.LENGTH_SHORT);
	}

	public static Toast showShortNew(Context mContext, String text, Object... objs) {
		return handlerToastStr(false, mContext, text, Toast.LENGTH_SHORT, objs);
	}

	public static Toast showShortNew(Context mContext, int resId) {
		return handlerToastRes(false, mContext, resId, Toast.LENGTH_SHORT);
	}

	public static Toast showShortNew(Context mContext, int resId, Object... objs) {
		return handlerToastRes(false, mContext, resId, Toast.LENGTH_SHORT, objs);
	}

	// ========================
	// == Toast.LENGTH_LONG ===
	// ========================

	public static Toast showLongNew(Context mContext, String text) {
		return handlerToastStr(false, mContext, text, Toast.LENGTH_LONG);
	}

	public static Toast showLongNew(Context mContext, String text, Object... objs) {
		return handlerToastStr(false, mContext, text, Toast.LENGTH_LONG, objs);
	}

	public static Toast showLongNew(Context mContext, int resId) {
		return handlerToastRes(false, mContext, resId, Toast.LENGTH_LONG);
	}

	public static Toast showLongNew(Context mContext, int resId, Object...objs) {
		return handlerToastRes(false, mContext, resId, Toast.LENGTH_LONG, objs);
	}

	// =======================
	// ==== 最终Toast方法 ====
	// =======================

	/**
	 * 显示Toast
	 * @param mContext
	 * @param resId
	 * @param duration
	 */
	public static Toast showToastNew(Context mContext, int resId, int duration) {
		return handlerToastRes(false, mContext, resId, duration);
	}

	/**
	 * 显示Toast
	 * @param mContext
	 * @param text
	 * @param duration
	 */
	public static Toast showToastNew(Context mContext, String text, int duration) {
		return showToast(false, mContext, text, duration);
	}
}
