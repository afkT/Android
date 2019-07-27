package sophix.pro.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import sophix.pro.base.BaseApplication;

/**
 * detail: App（Android 工具类）
 * Created by Ttt
 */
public final class AppUtils {

	private AppUtils() {
	}

	/**
	 * 获取app 包名
	 * @return
	 */
	public static String getAppPackageName() {
		return BaseApplication.getContext().getPackageName();
	}

	/**
	 * 获取app 名
	 * @return
	 */
	public static String getAppName() {
		return getAppName(BaseApplication.getContext().getPackageName());
	}

	/**
	 * 获取app 名
	 * @param packageName
	 * @return
	 */
	public static String getAppName(final String packageName) {
		if (isSpace(packageName)) return null;
		try {
			PackageManager pm = BaseApplication.getContext().getPackageManager();
			PackageInfo pi = pm.getPackageInfo(packageName, 0);
			return pi == null ? null : pi.applicationInfo.loadLabel(pm).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取app版本名 - 对外显示
	 * @return
	 */
	public static String getAppVersionName() {
		return getAppVersionName(BaseApplication.getContext().getPackageName());
	}

	/**
	 * 获取app版本名 - 对外显示
	 * @param packageName The name of the package.
	 * @return
	 */
	public static String getAppVersionName(final String packageName) {
		if (isSpace(packageName)) return null;
		try {
			PackageInfo pi = BaseApplication.getContext().getPackageManager().getPackageInfo(packageName, 0);
			return pi == null ? null : pi.versionName;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取app版本号 - 内部判断
	 * @return
	 */
	public static int getAppVersionCode() {
		return getAppVersionCode(BaseApplication.getContext().getPackageName());
	}

	/**
	 * 获取app版本号 - 内部判断
	 * @param packageName The name of the package.
	 * @return
	 */
	public static int getAppVersionCode(final String packageName) {
		if (isSpace(packageName)) return -1;
		try {
			PackageInfo pi = BaseApplication.getContext().getPackageManager().getPackageInfo(packageName, 0);
			return pi == null ? -1 : pi.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * 判断字符串是否为 null 或全为空白字符
	 * @param str 待校验字符串
	 * @return
	 */
	private static boolean isSpace(final String str) {
		if (str == null) return true;
		for (int i = 0, len = str.length(); i < len; ++i) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}
