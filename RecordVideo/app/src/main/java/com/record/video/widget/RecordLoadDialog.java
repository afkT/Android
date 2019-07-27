package com.record.video.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.record.video.R;
import com.record.video.utils.DevUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * detail: 录制加载 Dialog
 * Created by Ttt
 */
public class RecordLoadDialog extends Dialog{

    // 上下文
    Context mContext;
    @BindView(R.id.drl_load_igview)
    ImageView drl_load_igview;

    public RecordLoadDialog(Context mContext) {
        super(mContext, R.style.Theme_Light_FullScreenDialogOperate);
        // 解决小米出现 X给截取一半（状态栏遮盖的问题）
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 绑定Layout
        this.setContentView(R.layout.dialog_record_load);
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
            //lParams.dimAmount = 0.0f;
            // 获取屏幕宽度高度
            int[] screen = DevUtils.getScreenWidthHeight(mContext);
            lParams.width = screen[0];
            lParams.height = screen[1];
            //lParams.x = 0;
            // lParams.y = (screen[1] - ScreenUtils.getStatusHeight(mContext)) / 2;
            lParams.gravity = Gravity.CENTER;
            window.setAttributes(lParams);
        } catch (Exception e) {
        }
        // 禁止返回键关闭
        this.setCancelable(false);
        // 禁止点击其他地方自动关闭
        this.setCanceledOnTouchOutside(false);
    }

    // ==

    /** 关闭Dialog */
    public void cancelDialog() {
        if (this.isShowing()) {
            this.cancel();
        }
    }

    /** 显示Dialog */
    public void showDialog(){
        // 加载动画
        drl_load_igview.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.anim_record_loading));
        // --
        this.show();
    }
}
