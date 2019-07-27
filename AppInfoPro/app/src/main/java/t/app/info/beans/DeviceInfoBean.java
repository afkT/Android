package t.app.info.beans;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import dev.DevUtils;
import dev.utils.app.info.KeyValueBean;
import t.app.info.beans.item.DeviceInfoItem;

/**
 * detail: 设备参数实体类
 * Created by Ttt
 */
public final class DeviceInfoBean extends KeyValueBean {

    public DeviceInfoBean(String key, String value) {
        super(key, value);
    }

    /**
     * 构造函数
     * @param deviceInfoItem
     */
    public static DeviceInfoBean get(DeviceInfoItem deviceInfoItem) {
        String key = DevUtils.getContext().getString(deviceInfoItem.getResId());
        String value = deviceInfoItem.getPhoneParams();
        return new DeviceInfoBean(key, value);
    }

    /**
     * 生成复制信息
     * @param deviceInfoItem
     * @return
     */
    public static String obtain(DeviceInfoItem deviceInfoItem) {
        return DeviceInfoBean.get(deviceInfoItem).toString();
    }

    /**
     * 生成 JSON格式数据
     * @param listDeviceInfos
     * @return
     */
    public static String obtain(ArrayList<DeviceInfoItem> listDeviceInfos) {
        try {
            ArrayList<DeviceInfoBean> lists = new ArrayList<>();
            for(int i = 0, len = listDeviceInfos.size(); i < len; i++) {
                lists.add(DeviceInfoBean.get(listDeviceInfos.get(i)));
            }
            // 返回 JSON格式数据 - 格式化
            return new GsonBuilder().setPrettyPrinting().create().toJson(lists);
        } catch (Exception e) {
        }
        return "";
    }
}
