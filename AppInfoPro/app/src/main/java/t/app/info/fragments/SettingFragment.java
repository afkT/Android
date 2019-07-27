package t.app.info.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.utils.app.AppUtils;
import dev.utils.app.assist.manager.ActivityManager;
import dev.utils.app.logger.DevLogger;
import dev.utils.app.share.SharedUtils;
import dev.utils.app.toast.ToastTintUtils;
import t.app.info.R;
import t.app.info.base.BaseFragment;
import t.app.info.base.config.Constants;
import t.app.info.base.event.SortEvent;
import t.app.info.dialogs.AppSortDialog;
import t.app.info.dialogs.QuerySuffixDialog;
import t.app.info.utils.ProUtils;
import t.app.info.utils.QuerySuffixUtils;

/**
 * detail: 设置信息 - Fragment
 * Created by Ttt
 */
public class SettingFragment extends BaseFragment {

    // ===== View =====
    @BindView(R.id.fs_appsort_linear)
    LinearLayout fs_appsort_linear;
    @BindView(R.id.fs_appsort_tv)
    TextView fs_appsort_tv;
    @BindView(R.id.fs_scanapk_linear)
    LinearLayout fs_scanapk_linear;
    @BindView(R.id.fs_reset_linear)
    LinearLayout fs_reset_linear;
    // ======== 其他对象 ========
    // 获取排序数据
    private String[] appSortArys;

    /**
     * 获取对象,并且设置数据
     */
    public static BaseFragment getInstance() {
        SettingFragment bFragment = new SettingFragment();
        return bFragment;
    }

    // ==

    @Override
    public int getLayoutId() {
        return R.layout.fragment_setting;
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
    }

    @Override
    public void initValues() {
        super.initValues();
        // 获取数据
        appSortArys = mContext.getResources().getStringArray(R.array.appSortArys);
        // 进行排序
        selectAppSort();

    }

    @Override
    public void initListeners() {
        super.initListeners();
        // 点击排序
        fs_appsort_linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示Dialog
                new AppSortDialog(mContext).showDialog();
            }
        });
        // 设置扫描APK后缀字段
        fs_scanapk_linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示Dialog
                new QuerySuffixDialog(mContext).showDialog();
            }
        });
        // 恢复默认设置
        fs_reset_linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 保存索引
                SharedUtils.put(Constants.Key.KEY_APP_SORT, 0);
                // 清空后缀
                QuerySuffixUtils.reset();
                // 通知刷新
                vHandler.sendEmptyMessage(Constants.Notify.H_APP_SORT_NOTIFY);
                // 进行提示
                ToastTintUtils.success(AppUtils.getString(R.string.reset_desetting_suc));
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
            // 判断通知类型
            switch (msg.what) {
                case Constants.Notify.H_APP_SORT_NOTIFY:
                    // 重置清空数据
                    ProUtils.reset();
                    // 进行排序
                    selectAppSort();
                    break;
            }
        }
    };

    // == 外部开放方法 ==

    /** 选择App 排序 */
    private void selectAppSort() {
        // 更新文案
        fs_appsort_tv.setText(appSortArys[ProUtils.getAppSortType()]);
    }

    // == 事件相关 ==

    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onSortEvent(SortEvent event) {
        DevLogger.dTag(TAG, "onSortEvent");
        if (event != null) {
            int code = event.getCode();
            switch (code) {
                case Constants.Notify.H_APP_SORT_NOTIFY:
                    // 发送通知
                    vHandler.sendEmptyMessage(code);
                    break;
            }
        }
    }
}
