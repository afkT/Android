package t.app.info.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.utils.app.AppUtils;
import dev.utils.app.ScreenUtils;
import dev.utils.app.toast.ToastTintUtils;
import t.app.info.R;
import t.app.info.adapters.QuerySuffixAdapter;

/**
 * detail: 搜索后缀设置 Dialog
 * Created by Ttt
 */
public class QuerySuffixDialog extends Dialog implements View.OnClickListener {

    // 上下文
    Context mContext;
    // 适配器
    QuerySuffixAdapter mQuerySuffixAdapter;
    // ===== View =====
    @BindView(R.id.dqs_recycleview)
    RecyclerView dqs_recycleview;
    @BindView(R.id.dqs_close_tv)
    TextView dqs_close_tv;

    public QuerySuffixDialog(Context mContext) {
        super(mContext, R.style.Theme_Light_FullScreenDialogOperate);
        // 解决小米出现 X给截取一半（状态栏遮盖的问题）
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 布局不会顶起来
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        // 绑定Layout
        this.setContentView(R.layout.dialog_query_suffix);
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
        // 禁止返回键关闭
        this.setCancelable(false);
        // 禁止点击其他地方自动关闭
        this.setCanceledOnTouchOutside(false);
        // ==
        // 初始化参数
        initValues();
        // 设置点击事件
        dqs_close_tv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dqs_close_tv: // 取消
                // 提示设置生效
                ToastTintUtils.success(AppUtils.getString(R.string.setting_scan_suffix_suc));
                // 关闭Dialog
                cancelDialog();
                break;
        }
    }

    private void initValues() {
        // 初始化适配器并绑定
        mQuerySuffixAdapter = new QuerySuffixAdapter(getContext());
        dqs_recycleview.setAdapter(mQuerySuffixAdapter);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        dqs_recycleview.setLayoutManager(manager);
        // 刷新数据源
        mQuerySuffixAdapter.refData();
    }

    // ==

    /** 关闭Dialog */
    public void cancelDialog() {
        if (this.isShowing()) {
            this.cancel();
        }
    }

    /** 显示Dialog */
    public void showDialog() {
        this.show();
    }
}
