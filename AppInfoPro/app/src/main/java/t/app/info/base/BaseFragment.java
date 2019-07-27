package t.app.info.base;

import butterknife.Unbinder;
import dev.base.DevBaseFragment;
import dev.lib.other.EventBusUtils;
import dev.utils.app.logger.DevLogger;

/**
 * detail: Fragment 基类 -- 必须实现的方法和事件
 * Created by Ttt
 */
public abstract class BaseFragment extends DevBaseFragment {

    // ==== 其他对象 ====

    protected Unbinder unbinder;

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销 Event 事件
        registerEventOperate(false);
        if (unbinder != null){
            unbinder.unbind();
        }
    }

    /** 滑动到顶部 */
    public void onScrollTop() {
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
}
