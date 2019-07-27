package t.app.info.activitys;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import butterknife.ButterKnife;
import dev.lib.other.EventBusUtils;
import dev.utils.app.AppUtils;
import dev.utils.app.ClipboardUtils;
import dev.utils.app.PermissionUtils;
import dev.utils.app.assist.manager.ActivityManager;
import dev.utils.app.info.AppInfoBean;
import dev.utils.app.info.AppInfoItem;
import dev.utils.app.info.AppInfoUtils;
import dev.utils.app.info.KeyValueBean;
import dev.utils.app.logger.DevLogger;
import dev.utils.app.toast.ToastTintUtils;
import dev.utils.common.FileUtils;
import t.app.info.R;
import t.app.info.base.BaseActivity;
import t.app.info.base.config.Constants;
import t.app.info.base.config.ProConstants;
import t.app.info.base.event.AppUninstallEvent;
import t.app.info.base.event.ExportEvent;
import t.app.info.beans.item.ViewHolderItem;
import t.app.info.utils.ProUtils;

/**
 * detail: App 详情页面(已安装)
 * Created by Ttt
 */
public class AppDetailsActivity extends BaseActivity {

    // app 信息 Item
    private AppInfoItem appInfoItem;
    // ==== View ====
    @BindView(R.id.aad_toolbar)
    Toolbar aad_toolbar;
    @BindView(R.id.aad_app_igview)
    ImageView aad_app_igview;
    @BindView(R.id.aad_name_tv)
    TextView aad_name_tv;
    @BindView(R.id.aad_vname_tv)
    TextView aad_vname_tv;
    @BindView(R.id.aad_params_linear)
    LinearLayout aad_params_linear;
    @BindView(R.id.aad_openapp_tv)
    TextView aad_openapp_tv;
    @BindView(R.id.aad_uninstall_tv)
    TextView aad_uninstall_tv;

    @Override
    public int getLayoutId() {
        return R.layout.activity_app_details;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化View
        ButterKnife.bind(this);
        // 初始化方法
        initMethodOrder();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            // 判断是否安装 app
            if (!AppUtils.isInstalledApp(appInfoItem.getAppInfoBean().getAppPackName())) {
                // 发送 App 卸载通知事件
                EventBusUtils.sendEvent(new AppUninstallEvent(Constants.RequestCode.FOR_R_APP_UNINSTALL, appInfoItem.getAppInfoBean().getAppPackName()));
                // -
                Intent intent = new Intent();
                intent.putExtra(Constants.Key.KEY_PACKNAME, appInfoItem.getAppInfoBean().getAppPackName());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 判断请求类型
        switch (requestCode) {
            case Constants.RequestCode.FOR_R_APP_UNINSTALL: // 卸载 app
                // resultCode 一直等于0
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.aad_openapp_tv: // 打开应用
                AppUtils.launchApp(appInfoItem.getAppInfoBean().getAppPackName());
                break;
            case R.id.aad_uninstall_tv: // 卸载应用
                AppUtils.uninstallApp(this, appInfoItem.getAppInfoBean().getAppPackName(), Constants.RequestCode.FOR_R_APP_UNINSTALL);
                break;
        }
    }

    // ==

    @Override // 初始化全部参数，配置等
    public void initValues() {
        try {
            // 解析获取数据
            appInfoItem = AppInfoUtils.getAppInfoItem(getIntent().getStringExtra(Constants.Key.KEY_PACKNAME));
        } catch (Exception e) {
            DevLogger.eTag(TAG, e, "initOperate");
        }
        if (appInfoItem == null) {
            // 提示获取失败
            ToastTintUtils.warning(AppUtils.getString(R.string.get_appinfo_fail));
            finish(); // 销毁页面
            return;
        }
        // 刷新数据
        refData();
        // == 处理 ActionBar
        // https://blog.csdn.net/andygo_520/article/details/51439688
        // https://blog.csdn.net/zouchengxufei/article/details/51199922
        setSupportActionBar(aad_toolbar);
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
        // 打开应用
        aad_openapp_tv.setOnClickListener(this);
        // 卸载应用
        aad_uninstall_tv.setOnClickListener(this);
        // 设置点击事件
        aad_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
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
                    fileName = appInfoItem.getAppInfoBean().getAppName() + "_" + appInfoItem.getAppInfoBean().getAppPackName() + "_" + appInfoItem.getAppInfoBean().getVersionName() + ".txt";
                    // 导出数据
                    result = FileUtils.saveFile(ProConstants.EXPORT_APP_MSG_PATH, fileName, ProUtils.toJsonString(appInfoItem));
                    // 获取提示内容
                    tips = mContext.getString(result ? R.string.export_suc : R.string.export_fail);
                    // 判断结果
                    if (result) {
                        // 拼接保存路径
                        tips += " " + ProConstants.EXPORT_APP_MSG_PATH + fileName;
                    }
                    // 提示结果
                    if (result){
                        ToastTintUtils.success(tips);
                    } else {
                        ToastTintUtils.error(tips);
                    }
                    break;
                case Constants.Notify.H_EXPORT_APP_NOTIFY: // 导出apk安装包
                    // 提示导出中
                    ToastTintUtils.normal(AppUtils.getString(R.string.export_ing));
                    // 后台线程导出
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // 获取文件名 - 应用名_包名_版本名.apk
                            String fileName = appInfoItem.getAppInfoBean().getAppName() + "_" + appInfoItem.getAppInfoBean().getAppPackName() + "_" + appInfoItem.getAppInfoBean().getVersionName() + ".apk";
                            // 导出数据
                            boolean result = FileUtils.copyFile(appInfoItem.getAppInfoBean().getSourceDir(), ProConstants.EXPORT_APK_PATH + fileName, true);
                            // 获取提示内容
                            String tips = mContext.getString(result ? R.string.export_suc : R.string.export_fail);
                            // 判断结果
                            if (result) {
                                // 拼接保存路径
                                tips += " " + ProConstants.EXPORT_APK_PATH + fileName;
                            }
                            // 进行提示
                            final String resultTips = tips;
                            // 提示结果
                            if (result){
                                ToastTintUtils.success(resultTips);
                            } else {
                                ToastTintUtils.error(resultTips);
                            }
                        }
                    }).start();
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
        AppInfoBean appInfoBean = appInfoItem.getAppInfoBean();
        // 设置 app 图标
        aad_app_igview.setImageDrawable(appInfoBean.getAppIcon());
        // 设置 app 名
        aad_name_tv.setText(appInfoBean.getAppName());
        // 设置 app 版本名
        aad_vname_tv.setText(appInfoBean.getVersionName());
        // 初始化View
        forViews();
    }

    /** 循环遍历添加 View 数据 */
    private void forViews() {
        // 清空旧的View
        aad_params_linear.removeAllViews();
        // 其他功能
        otherFunction();
        // 数据源
        List<KeyValueBean> lists = appInfoItem.getListKeyValues();
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
            aad_params_linear.addView(itemView);
        }
    }

    /** 其他功能 */
    private void otherFunction() {
        // LayoutInflater
        LayoutInflater inflater = LayoutInflater.from(this);// 初始化数据源
        // ====== 打开应用商城 ======
        View view = new ViewHolderItem(inflater).setData(R.string.app_market, R.string.goto_app_market, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // https://www.jianshu.com/p/a4a806567368
                // --
                if (!AppUtils.launchAppDetails(appInfoItem.getAppInfoBean().getAppPackName(), "")) {
                    ToastTintUtils.error(AppUtils.getString(R.string.operate_fail));
                }
            }
        }).getItemView();
        // 添加View
        aad_params_linear.addView(view);
        // ====== 跳转设置 ======
        view = new ViewHolderItem(inflater).setData(R.string.app_details_setting, R.string.goto_app_details_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AppUtils.launchAppDetailsSettings(appInfoItem.getAppInfoBean().getAppPackName())) {
                    ToastTintUtils.error(AppUtils.getString(R.string.operate_fail));
                }
            }
        }).getItemView();
        // 添加View
        aad_params_linear.addView(view);
    }

    // ==

    @Override // 默认创建Menu显示
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bar_menu_app_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 需要的权限
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        // 判断点击按钮
        switch (item.getItemId()) {
            case R.id.bmai_export_app_msg: // 导出App信息
                // 判断是否存在读写权限
                if(ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
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
            case R.id.bmai_export_app: // 导出apk 安装包// 需要的权限
                // 判断是否存在读写权限
                if(ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                    // 发出导出应用 APK 安装包通知事件
                    EventBusUtils.sendEvent(new ExportEvent(Constants.Notify.H_EXPORT_APP_NOTIFY));
                } else {
                    PermissionUtils.permission(permission).callBack(new PermissionUtils.PermissionCallBack() {
                        @Override
                        public void onGranted(PermissionUtils permissionUtils) {
                            // 发出导出应用 APK 安装包通知事件
                            EventBusUtils.sendEvent(new ExportEvent(Constants.Notify.H_EXPORT_APP_NOTIFY));
                        }

                        @Override
                        public void onDenied(PermissionUtils permissionUtils) {
                            // 提示导出失败
                            ToastTintUtils.error(AppUtils.getString(R.string.export_fail));
                        }
                    }).request();
                }
                break;
            case R.id.bmai_share: // 分享
                // https://blog.csdn.net/xanxus46/article/details/8228366
                try {
                    // 调用android 系统的分享窗口
                    Intent intent=new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(FileUtils.getFile(appInfoItem.getAppInfoBean().getSourceDir())));
                    startActivity(intent);
                } catch (Exception e) {
                    // https://blog.csdn.net/qq_23179075/article/details/70314473
                    // https://www.jianshu.com/p/8ba7f2f16af9
                    // file:///storage/emulated/0/photo.jpeg exposed beyond app through ClipData.Item.getUri
                    // 提示分享失败
                    ToastTintUtils.error(AppUtils.getString(R.string.share_fail));
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
            switch (code) {
                case Constants.Notify.H_EXPORT_APP_MSG_NOTIFY:
                    // 处理导出 APP 信息操作
                    vHandler.sendEmptyMessage(code);
                    break;
                case Constants.Notify.H_EXPORT_APP_NOTIFY:
                    // 处理导出 APP 操作
                    vHandler.sendEmptyMessage(code);
                    break;
            }
        }
    }
}
