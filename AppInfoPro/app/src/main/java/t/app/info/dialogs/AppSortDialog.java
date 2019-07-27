package t.app.info.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.lib.other.EventBusUtils;
import dev.utils.app.ScreenUtils;
import dev.utils.app.share.SharedUtils;
import t.app.info.R;
import t.app.info.base.config.Constants;
import t.app.info.base.event.SortEvent;
import t.app.info.utils.ProUtils;

/**
 * detail: App 排序 Dialog
 * Created by Ttt
 */
public class AppSortDialog extends Dialog implements View.OnClickListener {

    // 上下文
    Context mContext;
    // ===== View =====
    @BindView(R.id.das_cancel_tv)
    TextView das_cancel_tv;
    @BindView(R.id.das_radiogroup)
    RadioGroup das_radiogroup;

    public AppSortDialog(Context mContext) {
        super(mContext, R.style.Theme_Light_FullScreenDialogOperate);
        // 解决小米出现 X给截取一半（状态栏遮盖的问题）
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 绑定Layout
        this.setContentView(R.layout.dialog_app_sort);
        this.mContext = mContext;
        // --
        // 初始化View
        ButterKnife.bind(this);
        // ==------------------------------------------==
        // 设置宽度,高度以及显示的位置
        Window window = this.getWindow();
        WindowManager.LayoutParams lParams = window.getAttributes();
        try {
            // 设置透明度
            lParams.dimAmount = 0.5f;
            // 获取屏幕宽度高度
            int[] screen = ScreenUtils.getScreenWidthHeight();
            lParams.width = screen[0];
            lParams.height = screen[1];
            lParams.gravity = Gravity.CENTER;
            window.setAttributes(lParams);
        } catch (Exception e) {
        }
//        // 禁止返回键关闭
//        this.setCancelable(false);
//        // 禁止点击其他地方自动关闭
//        this.setCanceledOnTouchOutside(false);
        // 初始化View
        initViews();
        // 设置点击事件
        das_cancel_tv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.das_cancel_tv: // 取消
                cancelDialog();
                break;
        }
    }

    private void initViews() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        // 获取数据
        String[] appSortArys = mContext.getResources().getStringArray(R.array.appSortArys);
        // 遍历添加
        for (int i = 0, len = appSortArys.length; i < len; i++) {
            // 当前索引
            final int pos = i;
            // 引入View
            View itemView = inflater.inflate(R.layout.view_radio_btn, null, false);
            // 初始化View
            RadioButton radioButton = itemView.findViewById(R.id.radioButton);
            // 设置标题
            radioButton.setText(appSortArys[i]);
            // 设置id
            radioButton.setId(i);
            // 点击事件
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 获取选中索引
                    int sortPos = ProUtils.getAppSortType();
                    // 如果id不一样则才处理
                    if (pos != sortPos) {
                        // 保存索引
                        SharedUtils.put(Constants.Key.KEY_APP_SORT, pos);
                        // 发送应用排序变更通知事件
                        EventBusUtils.sendEvent(new SortEvent(Constants.Notify.H_APP_SORT_NOTIFY));
                    }
                    // 关闭
                    cancelDialog();
                }
            });
            // 保存View
            das_radiogroup.addView(itemView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        // 设置选中索引
        das_radiogroup.check(ProUtils.getAppSortType());
    }

    // ==

    /**
     * 关闭Dialog
     */
    public void cancelDialog() {
        if (this.isShowing()) {
            this.cancel();
        }
    }

    /**
     * 显示Dialog
     */
    public void showDialog() {
        this.show();
    }
}
