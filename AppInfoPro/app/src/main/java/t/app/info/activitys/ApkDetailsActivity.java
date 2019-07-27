package t.app.info.activitys;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import dev.lib.other.EventBusUtils;
import dev.utils.app.AppUtils;
import dev.utils.app.ClipboardUtils;
import dev.utils.app.PermissionUtils;
import dev.utils.app.assist.manager.ActivityManager;
import dev.utils.app.info.ApkInfoItem;
import dev.utils.app.info.AppInfoBean;
import dev.utils.app.info.AppInfoUtils;
import dev.utils.app.info.KeyValueBean;
import dev.utils.app.logger.DevLogger;
import dev.utils.app.toast.ToastTintUtils;
import dev.utils.common.FileUtils;
import t.app.info.R;
import t.app.info.base.BaseActivity;
import t.app.info.base.config.Constants;
import t.app.info.base.config.ProConstants;
import t.app.info.base.event.ExportEvent;
import t.app.info.base.event.FileOperateEvent;
import t.app.info.utils.ProUtils;

/**
 * detail: Apk 详情页面
 * Created by Ttt
 */
public class ApkDetailsActivity extends BaseActivity {

    // apk 信息 Item
    private ApkInfoItem apkInfoItem;
    // ==== View ====
    @BindView(R.id.apd_toolbar)
    Toolbar apd_toolbar;
    @BindView(R.id.apd_app_igview)
    ImageView apd_app_igview;
    @BindView(R.id.apd_name_tv)
    TextView apd_name_tv;
    @BindView(R.id.apd_vname_tv)
    TextView apd_vname_tv;
    @BindView(R.id.apd_params_linear)
    LinearLayout apd_params_linear;
    @BindView(R.id.apd_install_apk_tv)
    TextView apd_install_apk_tv;
    @BindView(R.id.apd_delete_apk_tv)
    TextView apd_delete_apk_tv;

    @Override
    public int getLayoutId() {
        return R.layout.activity_apk_details;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化方法
        initMethodOrder();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.apd_install_apk_tv: // 安装应用
                // 文件存在处理
                if (FileUtils.isFileExists(apkInfoItem.getAppInfoBean().getSourceDir())) {
                    // Android 8.0以上
                    if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.O) {
                        if (getPackageManager().canRequestPackageInstalls()) {
                            // 安装apk
                            AppUtils.installApp(apkInfoItem.getAppInfoBean().getSourceDir(), "t.app.info.fileprovider");
                        } else {
                            PermissionUtils.permission(Manifest.permission.REQUEST_INSTALL_PACKAGES).callBack(new PermissionUtils.PermissionCallBack() {
                                @Override
                                public void onGranted(PermissionUtils permissionUtils) {
                                    // 安装apk
                                    AppUtils.installApp(apkInfoItem.getAppInfoBean().getSourceDir(), "t.app.info.fileprovider");
                                }

                                @Override
                                public void onDenied(PermissionUtils permissionUtils) {
                                    try {
                                        // 先进行提示
                                        ToastTintUtils.info(AppUtils.getString(R.string.install_request_tips));
                                        // 跳转设置页面, 开启安装未知应用权限
                                        startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));
                                    } catch (Exception e) {
                                        ToastTintUtils.error(AppUtils.getString(R.string.install_fail));
                                    }
                                }
                            }).request();
                        }
                        return;
                    }
                    // 安装apk
                    AppUtils.installApp(apkInfoItem.getAppInfoBean().getSourceDir(), "t.app.info.fileprovider");
                } else {
                    ToastTintUtils.warning(AppUtils.getString(R.string.file_not_exist));
                }
                break;
            case R.id.apd_delete_apk_tv: // 删除apk文件
                // 文件存在处理
                if (FileUtils.isFileExists(apkInfoItem.getAppInfoBean().getSourceDir())) {
                    // 发送删除文件通知事件
                    EventBusUtils.sendEvent(new FileOperateEvent(Constants.Notify.H_DELETE_APK_FILE_NOTIFY, apkInfoItem.getAppInfoBean().getSourceDir()));
                }
                // 删除文件
                FileUtils.deleteFile(apkInfoItem.getAppInfoBean().getSourceDir());
                // 提示删除成功
                ToastTintUtils.success(AppUtils.getString(R.string.delete_suc));
                break;
        }
    }

    // ==

    @Override // 初始化全部参数，配置等
    public void initValues() {
        try {
            // 解析获取数据
            apkInfoItem = AppInfoUtils.getApkInfoItem(getIntent().getStringExtra(Constants.Key.KEY_APK_URI));
        } catch (Exception e) {
            DevLogger.eTag(TAG, e, "initOperate");
        }
        if (apkInfoItem == null) {
            // 提示获取失败
            ToastTintUtils.warning(AppUtils.getString(R.string.get_apkinfo_fail));
            finish(); // 销毁页面
            return;
        }
        // 刷新数据
        refData();
        // == 处理 ActionBar
        // https://blog.csdn.net/andygo_520/article/details/51439688
        // https://blog.csdn.net/zouchengxufei/article/details/51199922
        setSupportActionBar(apd_toolbar);
        ActionBar actionBar =  getSupportActionBar();
        if(actionBar != null) {
            // 给左上角图标的左边加上一个返回的图标
            actionBar.setDisplayHomeAsUpEnabled(true);
            // 对应ActionBar.DISPLAY_SHOW_TITLE
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override // 初始化事件
    public void initListeners() {
        super.initListeners();
        // 安装应用
        apd_install_apk_tv.setOnClickListener(this);
        // 删除apk文件
        apd_delete_apk_tv.setOnClickListener(this);
        // 设置点击事件
        apd_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 关闭页面
                finish();
            }
        });
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
            // 操作结果
            boolean result = false;
            // 操作提示
            String tips = "";
            // 获取文件名
            String fileName = "";
            // 判断通知类型
            switch (msg.what) {
                case Constants.Notify.H_EXPORT_APP_MSG_NOTIFY: // 导出app信息
                    // 获取文件名 应用名_包名_版本名.txt
                    fileName = "apkFile_" + apkInfoItem.getAppInfoBean().getAppName() + "_" + apkInfoItem.getAppInfoBean().getAppPackName() + "_" + apkInfoItem.getAppInfoBean().getVersionName() + ".txt";
                    // 导出数据
                    result = FileUtils.saveFile(ProConstants.EXPORT_APK_MSG_PATH, fileName, ProUtils.toJsonString(apkInfoItem));
                    // 获取提示内容
                    tips = mContext.getString(result ? R.string.export_suc : R.string.export_fail);
                    // 判断结果
                    if (result) {
                        // 拼接保存路径
                        tips += " " + ProConstants.EXPORT_APK_MSG_PATH + fileName;
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

    /**
     * 刷新数据
     */
    private void refData() {
        // https://blog.csdn.net/ruingman/article/details/51347650

        // 获取app信息
        AppInfoBean appInfoBean = apkInfoItem.getAppInfoBean();
        // 设置 app 图标
        apd_app_igview.setImageDrawable(appInfoBean.getAppIcon());
        // 设置 app 名
        apd_name_tv.setText(appInfoBean.getAppName());
        // 设置 app 版本名
        apd_vname_tv.setText(appInfoBean.getVersionName());
        // 初始化View
        forViews();
    }

    /** 循环遍历添加 View 数据 */
    private void forViews() {
        // 清空旧的View
        apd_params_linear.removeAllViews();
        // 数据源
        List<KeyValueBean> lists = apkInfoItem.getListKeyValues();
        // LayoutInflater
        LayoutInflater inflater = LayoutInflater.from(this);
        // 遍历添加
        for (int i = 0, len = lists.size(); i < len; i++) {
            // 获取Item
            final KeyValueBean keyValueBean = lists.get(i);
            // 初始化数据源
            View itemView = inflater.inflate(R.layout.item_app_details, null, false);
            // 初始化View
            LinearLayout iad_linear = itemView.findViewById(R.id.iad_linear);
            TextView iad_key_tv = itemView.findViewById(R.id.iad_key_tv);
            TextView iad_value_tv = itemView.findViewById(R.id.iad_value_tv);
            // 设置值
            iad_key_tv.setText(keyValueBean.getKey());
            iad_value_tv.setText(keyValueBean.getValue());
            // 设置点击事件
            iad_linear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 复制的内容
                    String txt = keyValueBean.toString();
                    // 复制到剪切板
                    ClipboardUtils.copyText(txt);
                    // 进行提示
                    ToastTintUtils.success(AppUtils.getString(R.string.copy_suc) + " -> " + txt);
                }
            });
            // 添加View
            apd_params_linear.addView(itemView);
        }
    }

    // ==

    @Override // 默认创建Menu显示
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bar_menu_apk_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 需要的权限
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        // 判断点击按钮
        switch (item.getItemId()) {
            case R.id.bmpi_export_apk_msg: // 导出Apk信息
                // 判断是否存在读写权限
                if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                    // 发出导出 APP 信息通知事件
                    EventBusUtils.sendEvent(new ExportEvent(Constants.Notify.H_EXPORT_APP_MSG_NOTIFY));
                } else {
                    PermissionUtils.permission(permission).callBack(new PermissionUtils.PermissionCallBack() {
                        @Override
                        public void onGranted(PermissionUtils permissionUtils) {
                            // 发出导出 APP 信息通知事件
                            EventBusUtils.sendEvent(new ExportEvent(Constants.Notify.H_EXPORT_APP_MSG_NOTIFY));
                        }

                        @Override
                        public void onDenied(PermissionUtils permissionUtils) {
                            // 提示导出失败
                            ToastTintUtils.error(AppUtils.getString(R.string.export_fail));
                        }
                    }).request();
                }
                break;
        }
        return true;
    }

    // == 事件相关 ==

    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onExportEvent(ExportEvent event) {
        DevLogger.dTag(TAG, "onExportEvent");
        if (event != null) {
            int code = event.getCode();
            switch (code){
                case Constants.Notify.H_EXPORT_APP_MSG_NOTIFY:
                    // 处理导出 APP信息操作
                    vHandler.sendEmptyMessage(code);
                    break;
            }
        }
    }
}
