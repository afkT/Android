package com.play.pro.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

/**
 * 屏幕相关工具类（屏幕信息、dp，px，sp转换，截图等）
 */
public final class ScreenUtils {
	
	/**
     * Don't let anyone instantiate this class.
     */
    private ScreenUtils() {
        throw new Error("Do not need instantiate!");
    }
    
	// == ----------------------------------------- ==
    
	/**
	 * 通过上下文获取屏幕宽度
	 * @param mContext
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static int getScreenWidth(Context mContext) {
		try {
			// 获取屏幕信息
			DisplayMetrics dMetrics = ProUtils.getDisplayMetrics(mContext);
			if (dMetrics != null) {
				return dMetrics.widthPixels;
			}
			// 这种也可以获取，不过已经提问过时(下面这段可以注释掉)
			WindowManager wManager = ProUtils.getWindowManager(mContext);
			if (wManager != null) {
				return wManager.getDefaultDisplay().getWidth();
			}
		} catch (Exception e) {
		}
		return -1;
	}

	/**
	 * 通过上下文获取屏幕高度
	 * @param mContext
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static int getScreenHeight(Context mContext) {
		try {
			// 获取屏幕信息
			DisplayMetrics dMetrics = ProUtils.getDisplayMetrics(mContext);
			if (dMetrics != null) {
				return dMetrics.heightPixels;
			}
			// 这种也可以获取，不过已经提示过时(下面这段可以注释掉)
			WindowManager wManager = ProUtils.getWindowManager(mContext);
			if (wManager != null) {
				return wManager.getDefaultDisplay().getHeight();
			}
		} catch (Exception e) {
		}
		return -1;
	}

	/**
	 * 通过上下文获取屏幕宽度高度
	 * @param mContext
	 * @return int[] 0 = 宽度，1 = 高度
	 */
	@SuppressWarnings("deprecation")
	public static int[] getScreenWidthHeight(Context mContext) {
		try {
			// 获取屏幕信息
			DisplayMetrics dMetrics = ProUtils.getDisplayMetrics(mContext);
			if (dMetrics != null) {
				return new int[] { dMetrics.widthPixels, dMetrics.heightPixels };
			}
			// 这种也可以获取，不过已经提示过时(下面这段可以注释掉)
			WindowManager wManager = ProUtils.getWindowManager(mContext);
			if (wManager != null) {
				int width = wManager.getDefaultDisplay().getWidth();
				int height = wManager.getDefaultDisplay().getHeight();
				return new int[] { width, height };
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 通过上下文获取屏幕密度
	 * @param mContext
	 * @return
	 */
	public static float getDensity(Context mContext) {
		try {
			// 获取屏幕信息
			DisplayMetrics dMetrics = ProUtils.getDisplayMetrics(mContext);
			if (dMetrics != null) {
				// 屏幕密度（0.75 / 1.0 / 1.5 / 2.0）
				return dMetrics.density;
			}
		} catch (Exception e) {
		}
		return -1;
	}

	/**
	 * 通过上下文获取屏幕密度Dpi
	 * @param mContext
	 * @return
	 */
	public static int getDensityDpi(Context mContext) {
		try {
			// 获取屏幕信息
			DisplayMetrics dMetrics = ProUtils.getDisplayMetrics(mContext);
			if (dMetrics != null) {
				// 屏幕密度DPI（120 / 160 / 240 / 320）
				return dMetrics.densityDpi;
			}
		} catch (Exception e) {
		}
		return -1;
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 * @param mContext
	 * @param dpValue
	 */
	public static int dipConvertPx(Context mContext, float dpValue) {
		try {
			float scale = mContext.getResources().getDisplayMetrics().density;
			return (int) (dpValue * scale + 0.5f);
		} catch (Exception e) {
		}
		return -1;
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 * @param mContext
	 * @param pxValue
	 */
	public static int pxConvertDip(Context mContext, float pxValue) {
		try {
			float scale = mContext.getResources().getDisplayMetrics().density;
			return (int) (pxValue / scale + 0.5f);
		} catch (Exception e) {
		}
		return -1;
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 sp
	 * @param mContext
	 * @param pxValue
	 */
	public static int pxConvertSp(Context mContext, float pxValue) {
		try {
			float scale = mContext.getResources().getDisplayMetrics().scaledDensity;
			return (int) (pxValue / scale + 0.5f);
		} catch (Exception e) {
		}
		return -1;
	}

	/**
	 * 根据手机的分辨率从 sp 的单位 转成为 px
	 * @param mContext
	 * @param spValue
	 */
	public static int spConvertPx(Context mContext, float spValue) {
		try {
			float scale = mContext.getResources().getDisplayMetrics().scaledDensity;
			return (int) (spValue * scale + 0.5f);
		} catch (Exception e) {
		}
		return -1;
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 第二种
	 * @param mContext
	 * @param dpValue
	 */
	public static int dipConvertPx2(Context mContext, float dpValue) {
		try {
			return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, mContext.getResources().getDisplayMetrics());
		} catch (Exception e) {
		}
		return -1;
	}

	/**
	 * 根据手机的分辨率从 sp 的单位 转成为 px 第二种
	 * @param mContext
	 * @param pxValue
	 */
	public static int spConvertPx2(Context mContext, float spValue) {
		try {
			// android.util.TypedValue
			return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, mContext.getResources().getDisplayMetrics());
		} catch (Exception e) {
		}
		return -1;
	}

	// == ----------------------------------------- ==

	/**
	 * 获得状态栏的高度(无关 android:theme 获取状态栏高度)
	 * @param mContext
	 * @return
	 */
	public static int getStatusHeight(Context mContext) {
		try {
			Class<?> clazz = Class.forName("com.android.internal.R$dimen");
			Object object = clazz.newInstance();
			int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
			return mContext.getResources().getDimensionPixelSize(height);
		} catch (Exception e) {
		}
		return -1;
	}
	
	/**
	 * 获取应用区域 TitleBar 高度 （顶部灰色TitleBar高度，没有设置 android:theme 的 NoTitleBar 时会显示）
	 * @param mContext
	 * @return
	 */
	public static int getStatusBarHeight(Activity activity){
		try {
			Rect rect = new Rect();
			activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
			return rect.top;
		} catch (Exception e) {
		}
		return -1;
	}
	
	/**
	 * 获取当前屏幕截图，包含状态栏 （顶部灰色TitleBar高度，没有设置 android:theme 的 NoTitleBar 时会显示）
	 * @param activity
	 * @return
	 */
	public static Bitmap snapShotWithStatusBar(Activity activity) {
		try {
			View view = activity.getWindow().getDecorView();
			view.setDrawingCacheEnabled(true);
			view.buildDrawingCache();
			Bitmap bmp = view.getDrawingCache();
			int[] sParams = getScreenWidthHeight(activity);
			Bitmap bitmap = Bitmap.createBitmap(bmp, 0, 0, sParams[0], sParams[1]);
			view.destroyDrawingCache();
			return bitmap;
		} catch (Exception e) {
		}
		return null;
	}
	
	/**
	 * 获取当前屏幕截图，不包含状态栏 (如果 android:theme 全屏了，则截图无状态栏)
	 * @param activity
	 * @return
	 */
	public static Bitmap snapShotWithoutStatusBar(Activity activity) {
		try {
			View view = activity.getWindow().getDecorView();
			view.setDrawingCacheEnabled(true);
			view.buildDrawingCache();
			Bitmap bmp = view.getDrawingCache();
			int[] sParams = getScreenWidthHeight(activity);
			
			int statusBarHeight = getStatusBarHeight(activity);
			if(statusBarHeight == -1){
				statusBarHeight = 0;
			}
			Bitmap bitmap = Bitmap.createBitmap(bmp, 0, statusBarHeight, sParams[0], sParams[1] - statusBarHeight);
			view.destroyDrawingCache();
			return bitmap;
		} catch (Exception e) {
		}
		return null;
	}
	
	// == ----------------------------------------- ==
	
	/**
	 * 计算视频宽高大小，视频比例xxx*xxx按屏幕比例放大或者缩小
	 * @param mContext 上下文
	 * @param width 高度比例
	 * @param height 宽度比例
	 * @return 返回宽高 0 = 宽，1 = 高
	 */
	public static int[] reckonVideoWidthHeight(float width, float height, Context mContext) {
		try {
			// 获取屏幕宽度 
			int sWidth = ScreenUtils.getScreenWidth(mContext);
			// 判断宽度比例
			float wRatio = 0.0f;
			// 计算比例
			wRatio = (sWidth - width) / width;
			// 等比缩放
			int nWidth = sWidth;
			int nHeight = (int) (height * (wRatio + 1));
			return new int []{ nWidth, nHeight };
		} catch (Exception e) {
		}
		return null;
	}
}

