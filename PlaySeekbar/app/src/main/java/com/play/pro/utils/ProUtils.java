package com.play.pro.utils;

import java.io.File;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/** App工具类(不进行拆分,便于copy一个类容易引用) */
public class ProUtils {

	// ===================== SD卡  =====================
	
	/**
	 * 判断SDCard是否正常挂载
	 * @return
	 */
	public static boolean isSDCardEnable() {
		// android.os.Environment
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
	
	/**
	 * 获取SD卡路径（File对象）
	 * @return
	 */
	public static File getSDCartFile(){
		return Environment.getExternalStorageDirectory();
	}
	
	/**
	 * 获取SD卡路径（无添加  -> / -> File.separator）
	 * @return
	 */
	public static String getSDCartPath(){
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}
	
	// ===================== 时间处理  =====================
	
	/**
	 * 传入时间，获取时间(一直保存00:00:00) - 不处理大于一天
	 * @param time 时间（秒为单位）
	 * @return
	 */
	public static String secToTimeRetain(int time){
		return secToTimeRetain(time, false);
	}
	
	/**
	 * 传入时间，获取时间(一直保存00:00:00)
	 * @param time 时间（秒为单位）
	 * @param isHandlerMDay 是否处理大于一天的时间
	 * @return
	 */
	public static String secToTimeRetain(int time, boolean isHandlerMDay){
		try {
			if(time <= 0){
				return "00:00:00";
			} else {
				// 单位秒
				int minute = 60;
				int hour = 3600;
				int day = 86400;
				// 取模
				int rSecond = 0;
				int rMinute = 0;
				// 差数
				int dSecond = 0;
				int dMinute = 0;
				int dHour = 0;
				// 转换时间格式
				if(time < minute){ // 小于1分钟
					return "00:00:" + ((time >=10)?time:("0" + time));
				} else if(time >= minute && time < hour){ // 小于1小时
					dSecond = time % minute; // 取模分钟，获取多出的秒数
					dMinute = (time - dSecond) / minute;
					return "00:" +  ((dMinute >=10)?dMinute:("0" + dMinute)) + ":" + ((dSecond >=10)?dSecond:("0" + dSecond));
				} else if(time >= hour && time < day){ // 小于等于一天
					rMinute = time % hour; // 取模小时，获取多出的分钟
					dHour = (time - rMinute) / hour; // 获取小时
					dSecond = (time - dHour * hour); // 获取多出的秒数
					dMinute = dSecond / minute; // 获取多出的分钟
					rSecond = dSecond % minute; // 取模分钟，获取多余的秒速
					return ((dHour >= 10) ? dHour : ("0" + dHour)) + ":" + ((dMinute >= 10) ? dMinute:("0" + dMinute)) + ":" + ((rSecond >= 10) ? rSecond:"0" + rSecond);
				} else { // 多余的时间，直接格式化
					// 大于一天的情况
					if(isHandlerMDay){
						rMinute = time % hour; // 取模小时，获取多出的分钟
						dHour = (time - rMinute) / hour; // 获取小时
						dSecond = (time - dHour * hour); // 获取多出的秒数
						dMinute = dSecond / minute; // 获取多出的分钟
						rSecond = dSecond % minute; // 取模分钟，获取多余的秒速
						return ((dHour >= 10) ? dHour : ("0" + dHour)) + ":" + ((dMinute >= 10) ? dMinute:("0" + dMinute)) + ":" + ((rSecond >= 10) ? rSecond:"0" + rSecond);	
					}
				}
			}
		} catch (Exception e) {
		}
		return null;
	}
	
	// ===================== App（Android 工具类）  =====================
    
    /**
	 * 通过上下文获取 WindowManager
	 * @param mContext
	 * @return
	 */
	public static WindowManager getWindowManager(Context mContext) {
		try {
			return (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 通过上下文 获取Activity
	 * @param mContext
	 * @return
	 */
	public static Activity getActivity(Context mContext) {
		try {
			Activity activity = (Activity) mContext;
			return activity;
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 通过上下文获取 DisplayMetrics (获取关于显示的通用信息，如显示大小，分辨率和字体)
	 * @param mContext
	 * @return
	 */
	public static DisplayMetrics getDisplayMetrics(Context mContext) {
		try {
			WindowManager wManager = getWindowManager(mContext);
			if (wManager != null) {
				DisplayMetrics dMetrics = new DisplayMetrics();
				wManager.getDefaultDisplay().getMetrics(dMetrics);
				return dMetrics;
			}
		} catch (Exception e) {
		}
		return null;
	}
	
	// == ----------------------------------------- ==
	
	/**
	 * 获取app版本信息
	 * @param mContext
	 * @return 0 = versionName , 1 = versionCode
	 */
	public static String[] getAppVersion(Context mContext){
		try {
			PackageManager pm = mContext.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(),PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null" : pi.versionName;
				String versionCode = pi.versionCode + "";
				
				return new String[]{versionName,versionCode};
			}
		} catch (Exception e) {
		}
		return null;
	}
	
	/**
     * 获取app版本号
     * @param mContext 上下文
     * @return 当前版本Code
     */
    public static int getVerCode(Context mContext) {
        try {
        	PackageManager pm = mContext.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(),PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				return pi.versionCode;
			}
        } catch (Exception e) {
        }
        return -1;
    }
    
    /**
     * 获取app版本信息
     * @param context 上下文
     * @return 当前版本信息
     */
    public static String getVerName(Context mContext) {
    	 try {
         	PackageManager pm = mContext.getPackageManager();
 			PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(),PackageManager.GET_ACTIVITIES);
 			if (pi != null) {
 				return pi.versionName;
 			}
         } catch (Exception e) {
         }
    	 return null;
    }

    // == ----------------------------------------- ==
	
 	/**
 	 * 对内设置指定语言 (app 多语言,单独改变app语言)
 	 * @param mContext
 	 * @param locale
 	 */
 	public static void setLanguage(Context mContext,Locale locale) {
 		try {
 			// 获得res资源对象
 			Resources resources = mContext.getResources();
 			// 获得设置对象
 			Configuration config = resources.getConfiguration();
 			// 获得屏幕参数：主要是分辨率，像素等。
 			DisplayMetrics dm = resources.getDisplayMetrics();
 			// 语言
 			config.locale = locale;
 			// 更新语言
 			resources.updateConfiguration(config, dm);
 		} catch (Exception e) {
 		}
 	}
 	
 	/**
 	 * 重启app
 	 * @param mContext
 	 */
 	public static void restartApplication(Context mContext) {
 		try {
 			Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			mContext.startActivity(intent);
 		} catch (Exception e) {
 		}
 	}
 	
 	// == ----------------------------------------- ==
 	
	/**
	 * 获取屏幕信息
	 * @param mContext 上下文
	 * @return
	 */
	public static int[] getScreen(Context mContext){
		int[] screen = null;
		try {
			WindowManager wManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
			Display display = wManager.getDefaultDisplay();
			// 获取宽度高度
			int width = display.getWidth();;
			int height = display.getHeight();
			if(width != 0 && height != 0){
				screen = new int[]{width,height};
			}
		} catch (Exception e) {
			screen = null;
		}
		return screen;
	}
	
	/**
	 * 获取屏幕宽度（对外公布）
	 * @param mContext 上下文
	 * @return
	 */
	public static int getWidth(Context mContext){
		int[] screen = getScreen(mContext);
		if(screen != null){
			return screen[0];
		}
		return 0;
	}
	
	/**
	 * 获取屏幕高度（对外公布）
	 * @param mContext 上下文
	 * @return
	 */
	public static int getHeight(Context mContext){
		int[] screen = getScreen(mContext);
		if(screen != null){
			return screen[1];
		}
		return 0;
	}
	
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
}
