package t.app.info.base;

import android.view.View;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import dev.base.DevBaseActivity;
import dev.base.DevBaseEvent;
import dev.lib.other.EventBusUtils;
import dev.utils.app.logger.DevLogger;

/**
 * detail: 基类
 * Created by Ttt
 */
public abstract class BaseActivity extends DevBaseActivity {

    Unbinder unbinder;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销 Event 事件
        registerEventOperate(false);
        // 注销绑定
        if (unbinder != null){
            unbinder.unbind();
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        // 初始化View
        unbinder = ButterKnife.bind(this);
        // 注册 EventBus
        registerEventOperate(true);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        // 初始化View
        unbinder = ButterKnife.bind(this);
        // 注册 EventBus
        registerEventOperate(true);
    }

    // = Event 处理 =

    /**
     * 是否注册 Event 事件通知
     * @return
     */
    @Override
    protected boolean isRegisterEvent() {
        return true;
    }

    /**
     * 注册 Event 操作
     * @param isRegister
     */
    @Override
    protected final void registerEventOperate(boolean isRegister) {
        super.registerEventOperate(isRegister);

        if (isRegisterEvent()) {
            try {
                if (isRegister) {
                    EventBusUtils.register(this);
                } else {
                    EventBusUtils.unregister(this);
                }
            } catch (Exception e) {
                DevLogger.eTag(TAG, e, "registerEventOperate");
            }
        }
    }

    // == EventBus ==

    // 默认非粘性
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onEventBus(DevBaseEvent<?> event) {
        DevLogger.dTag(TAG, "onEventBus");
        if (event != null) {
            receiveEvent(event);
        }
    }
}
