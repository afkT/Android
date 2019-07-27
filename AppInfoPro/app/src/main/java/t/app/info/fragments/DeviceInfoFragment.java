package t.app.info.fragments;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.utils.app.CPUUtils;
import dev.utils.app.DeviceUtils;
import dev.utils.app.MemoryUtils;
import dev.utils.app.SDCardUtils;
import dev.utils.app.ScreenUtils;
import dev.utils.app.assist.manager.ActivityManager;
import dev.utils.app.logger.DevLogger;
import dev.utils.app.toast.ToastTintUtils;
import dev.utils.common.FileUtils;
import t.app.info.R;
import t.app.info.activitys.MainActivity;
import t.app.info.adapters.DeviceInfoAdapter;
import t.app.info.base.BaseFragment;
import t.app.info.base.config.Constants;
import t.app.info.base.config.ProConstants;
import t.app.info.base.event.ExportEvent;
import t.app.info.beans.DeviceInfoBean;
import t.app.info.beans.TypeEnum;
import t.app.info.beans.item.DeviceInfoItem;

/**
 * detail: 设备信息 - Fragment
 * Created by Ttt
 */
public class DeviceInfoFragment extends BaseFragment {

    // ===== View =====
    @BindView(R.id.fdi_recycleview)
    RecyclerView fdi_recycleview;
    // ======== 其他对象 ========
    // 适配器
    private DeviceInfoAdapter mDeviceInfoAdapter;
    // 获取设备信息
    private ArrayList<DeviceInfoItem> mListDeviceInfos = new ArrayList<>();

    /**
     * 获取对象,并且设置数据
     */
    public static BaseFragment getInstance() {
        DeviceInfoFragment bFragment = new DeviceInfoFragment();
        return bFragment;
    }

    // ==

    @Override
    public int getLayoutId() {
        return R.layout.fragment_device_info;
    }

    @Override
    protected void onInit(View view, ViewGroup container, Bundle savedInstanceState) {
        // 初始化View
        unbinder = ButterKnife.bind(this, view);
        // 注册 EventBus
        registerEventOperate(true);
        // 初始化方法
        initMethodOrder();
    }

    // ==

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden) {
            onPause();
            onStop();
        } else {
            onStart();
            onResume();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(this.isHidden()) {
            return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(this.isHidden()) {
            return;
        }
        // 发送请求获取
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 获取设备信息
                getDeviceInfos();
            }
        }).start();
    }

    @Override
    public void initValues() {
        super.initValues();
        // 初始化适配器并绑定
        mDeviceInfoAdapter = new DeviceInfoAdapter(getActivity());
        fdi_recycleview.setAdapter(mDeviceInfoAdapter);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        fdi_recycleview.setLayoutManager(manager);
    }

    // ==

    /** View 操作Handler */
    Handler vHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 如果页面已经关闭,则不进行处理
            if (ActivityManager.isFinishingCtx(mContext)) {
                return;
            }
            // 判断通知类型
            switch (msg.what) {
                case Constants.Notify.H_QUERY_DEVICE_INFO_END_NOTIFY:
                    // 刷新适配器
                    mDeviceInfoAdapter.setData(mListDeviceInfos);
                    break;
                case Constants.Notify.H_EXPORT_DEVICE_MSG_NOTIFY:
                    // 导出数据
                    boolean result = FileUtils.saveFile(ProConstants.EXPORT_PATH, "deviceinfo.txt", DeviceInfoBean.obtain(mListDeviceInfos));
                    // 获取提示内容
                    String tips = mContext.getString(result ? R.string.export_suc : R.string.export_fail);
                    // 判断结果
                    if (result) {
                        // 拼接保存路径
                        tips += " " + ProConstants.EXPORT_PATH + "deviceinfo.txt";
                    }
                    // 提示结果
                    if (result){
                        ToastTintUtils.success(tips);
                    } else {
                        ToastTintUtils.error(tips);
                    }
                    break;
            }
        }
    };

    // == 外部开放方法 ==

    /**
     * 滑动到顶部
     */
    public void onScrollTop() {
        if (fdi_recycleview != null) {
            fdi_recycleview.scrollToPosition(0);
        }
    }

    /** 获取手机信息 */
    private void getDeviceInfos() {
        // https://blog.csdn.net/xx326664162/article/details/52438706
        // https://blog.csdn.net/litianquan/article/details/78572617
        // https://blog.csdn.net/lchad/article/details/43716893

        // 获取手机尺寸
        // https://blog.csdn.net/lincyang/article/details/42679589

        // https://blog.csdn.net/loongggdroid/article/details/12304695

        // http://blog.51cto.com/xujpxm/1961072

        // 设备信息
        HashMap<String, String> mapDeviceInfos = new HashMap<>();
        // 进行初始化获取
        DeviceUtils.getDeviceInfo(mapDeviceInfos);
        mListDeviceInfos.clear();
        // 获取手机型号
        mListDeviceInfos.add(new DeviceInfoItem(R.string.model, android.os.Build.MODEL + ""));
        // 获取设备制造商
        mListDeviceInfos.add(new DeviceInfoItem(R.string.manufacturer, android.os.Build.MANUFACTURER + ""));
        // 获取设备品牌
        mListDeviceInfos.add(new DeviceInfoItem(R.string.brand, android.os.Build.BRAND + ""));
        // 获取Android 系统版本
        mListDeviceInfos.add(new DeviceInfoItem(R.string.version_release, android.os.Build.VERSION.RELEASE + ""));
        // 获取屏幕尺寸(英寸)
        mListDeviceInfos.add(new DeviceInfoItem(R.string.screen, ScreenUtils.getScreenSizeOfDevice() + ""));
        // 获取屏幕分辨率
        mListDeviceInfos.add(new DeviceInfoItem(R.string.screen_size, ScreenUtils.getScreenSize() + ""));
        // 获取手机总空间
        mListDeviceInfos.add(new DeviceInfoItem(R.string.sdcard_total, SDCardUtils.getSDTotalSize() + ""));
        // 获取手机可用空间
        mListDeviceInfos.add(new DeviceInfoItem(R.string.sdcard_available, SDCardUtils.getSDAvailableSize() + ""));
        // 获取手机总内存
        mListDeviceInfos.add(new DeviceInfoItem(R.string.memory_total, MemoryUtils.getTotalMemory() + ""));
        // 获取手机可用内存
        mListDeviceInfos.add(new DeviceInfoItem(R.string.memory_available, MemoryUtils.getMemoryAvailable() + ""));
        // 获取设备版本号
        mListDeviceInfos.add(new DeviceInfoItem(R.string.id, Build.ID + ""));
        // 获取设备版本
        mListDeviceInfos.add(new DeviceInfoItem(R.string.display, android.os.Build.DISPLAY + ""));
        // 获取设备名
        mListDeviceInfos.add(new DeviceInfoItem(R.string.device, android.os.Build.DEVICE + ""));
        // 获取产品名称
        mListDeviceInfos.add(new DeviceInfoItem(R.string.product, android.os.Build.PRODUCT + ""));
        try {
            // 判断是否模拟器
            String result = mapDeviceInfos.get("IS_EMULATOR".toLowerCase());
            // 存在结果才显示
            if (!TextUtils.isEmpty(result)) {
                mListDeviceInfos.add(new DeviceInfoItem(R.string.is_emulator, result));
            }
        } catch (Exception e) {
        }
        try {
            // 判断是否允许debug调试
            String result = mapDeviceInfos.get("IS_DEBUGGABLE".toLowerCase());
            // 存在结果才显示
            if (!TextUtils.isEmpty(result)) {
                mListDeviceInfos.add(new DeviceInfoItem(R.string.is_debuggable, result));
            }
        } catch (Exception e) {
        }
        // 获取基带版本
        mListDeviceInfos.add(new DeviceInfoItem(R.string.baseband_version, DeviceUtils.getBaseband_Ver() + ""));
        // 获取内核版本
        mListDeviceInfos.add(new DeviceInfoItem(R.string.linuxcode_version, DeviceUtils.getLinuxCore_Ver() + ""));
        // 获取序列号
        mListDeviceInfos.add(new DeviceInfoItem(R.string.serial, Build.SERIAL + ""));
        // 设备唯一标识,由设备的多个信息拼接合成.
        mListDeviceInfos.add(new DeviceInfoItem(R.string.fingerprint, Build.FINGERPRINT + ""));
        // 获取设备基板名称
        mListDeviceInfos.add(new DeviceInfoItem(R.string.board, Build.BOARD + ""));
        // 获取设备硬件名称,一般和基板名称一样（BOARD）
        mListDeviceInfos.add(new DeviceInfoItem(R.string.hardware, Build.HARDWARE + ""));
        // 获取CPU 型号
        mListDeviceInfos.add(new DeviceInfoItem(R.string.cpuinfo, CPUUtils.getCpuInfo() + ""));
        // CPU指令集
        mListDeviceInfos.add(new DeviceInfoItem(R.string.cpu_abi1, android.os.Build.CPU_ABI + ""));
        mListDeviceInfos.add(new DeviceInfoItem(R.string.cpu_abi2, android.os.Build.CPU_ABI2 + ""));
        try {
            // 判断支持的指令集
            String result = mapDeviceInfos.get("SUPPORTED_ABIS".toLowerCase());
            // 存在结果才显示
            if (!TextUtils.isEmpty(result)) {
                mListDeviceInfos.add(new DeviceInfoItem(R.string.supported_abis, result));
            }
        } catch (Exception e) {
        }
        // 获取 CPU 数量
        mListDeviceInfos.add(new DeviceInfoItem(R.string.cpu_number, CPUUtils.getCoresNumbers() + ""));
        // 获取 CPU 最高 HZ
        mListDeviceInfos.add(new DeviceInfoItem(R.string.cpu_max, CPUUtils.getMaxCpuFreq() + ""));
        // 获取 CPU 最底 HZ
        mListDeviceInfos.add(new DeviceInfoItem(R.string.cpu_min, CPUUtils.getMinCpuFreq() + ""));
        // 获取 CPU 当前 HZ
        mListDeviceInfos.add(new DeviceInfoItem(R.string.cpu_cur, CPUUtils.getCurCpuFreq() + ""));
        // 发送通知
        vHandler.sendEmptyMessage(Constants.Notify.H_QUERY_DEVICE_INFO_END_NOTIFY);
    }

    // == 事件相关 ==

    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onExportEvent(ExportEvent event) {
        DevLogger.dTag(TAG, "onExportEvent");
        if (event != null) {
            int code = event.getCode();
            switch (code){
                case Constants.Notify.H_EXPORT_DEVICE_MSG_NOTIFY:
                    if (MainActivity.getTypeEnum() == TypeEnum.DEVICE_INFO) {
                        // 发送通知
                        vHandler.sendEmptyMessage(code);
                    }
                    break;
            }
        }
    }
}
