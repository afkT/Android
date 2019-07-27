package t.app.info.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.utils.app.KeyBoardUtils;
import dev.utils.app.ScreenUtils;
import t.app.info.R;
import t.app.info.utils.QuerySuffixUtils;

/**
 * detail: 搜索后缀设置 Dialog
 * Created by Ttt
 */
public class QuerySuffixEditDialog extends Dialog implements View.OnClickListener {

    // 上下文
    Context mContext;
    // 点击添加事件
    View.OnClickListener onClickListener;
    // ===== View =====
    @BindView(R.id.dqse_edit_text)
    EditText dqse_edit_text;
    @BindView(R.id.dqs_add_tv)
    TextView dqs_add_tv;
    @BindView(R.id.dqs_cancel_tv)
    TextView dqs_cancel_tv;

    public QuerySuffixEditDialog(Context mContext, View.OnClickListener onClickListener) {
        super(mContext, R.style.Theme_Light_FullScreenDialogOperate);
        // 解决小米出现 X给截取一半（状态栏遮盖的问题）
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        // 绑定Layout
        this.setContentView(R.layout.dialog_query_suffix_edit);
        this.mContext = mContext;
        this.onClickListener = onClickListener;
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
            lParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lParams.gravity = Gravity.CENTER;
            window.setAttributes(lParams);
        } catch (Exception e) {
        }
        // 禁止返回键关闭
        this.setCancelable(false);
        // 禁止点击其他地方自动关闭
        this.setCanceledOnTouchOutside(false);
        // ==
        // 设置点击事件
        dqs_cancel_tv.setOnClickListener(this);
        dqs_add_tv.setOnClickListener(this);
        // 关闭处理
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 关闭输入法
                        KeyBoardUtils.closeKeyboard(dqse_edit_text);
                    }
                }, 100);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dqs_add_tv: // 添加
                // 获取输入内容
                String input = dqse_edit_text.getText().toString();
                // 防止为null
                if (TextUtils.isEmpty(input)) {
                    // 关闭Dialog
                    cancelDialog();
                    return;
                }
                // 统一转换小写
                input = input.toLowerCase();
                // 获取数据源
                LinkedHashMap<String, String> maps = QuerySuffixUtils.getQuerySuffixMap();
                // 判断是否存在key
                if (maps.containsKey(input)) {
                    // 关闭Dialog
                    cancelDialog();
                    return;
                }
                // 进行保存
                maps.put(input, input);
                // 刷新配置
                QuerySuffixUtils.refConfig(maps);
                // 触发事件
                this.onClickListener.onClick(v);
                // 关闭Dialog
                cancelDialog();
                break;
            case R.id.dqs_cancel_tv: // 取消
                // 关闭Dialog
                cancelDialog();
                break;
        }
    }

    // ==

    /** 关闭Dialog */
    public void cancelDialog() {
        // 关闭输入法
        KeyBoardUtils.closeKeyboard(dqse_edit_text);
        // 关闭输入法
        if (this.isShowing()) {
            this.cancel();
        }
    }

    /** 显示Dialog */
    public void showDialog() {
        this.show();
    }
}
