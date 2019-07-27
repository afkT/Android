package t.app.info.beans.item;

import android.support.annotation.StringRes;

import t.app.info.R;

/**
 * detail: 设备信息 Item 类
 * Created by Ttt
 */
public class DeviceInfoItem {

    // 提示文案
    private @StringRes int resId = R.string.empty;
    // 手机配置参数
    private String phoneParams;

    public DeviceInfoItem(@StringRes int resId, String phoneParams) {
        this.resId = resId;
        this.phoneParams = phoneParams;
    }

    public int getResId() {
        return resId;
    }

    public String getPhoneParams() {
        return phoneParams;
    }
}
