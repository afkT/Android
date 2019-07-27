package t.app.info.base.event;

import dev.base.DevBaseEvent;

/**
 * detail: App 卸载事件
 * Created by Ttt
 */
public class AppUninstallEvent extends DevBaseEvent<String> {

    public AppUninstallEvent(int code) {
        super(code);
    }

    public AppUninstallEvent(int code, String data) {
        super(code, data);
    }

    public AppUninstallEvent(int code, String data, Object tag) {
        super(code, data, tag);
    }
}
