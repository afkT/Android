package t.app.info.base.event;

import dev.base.DevBaseEvent;

/**
 * detail: 导出 信息/APK 通知事件
 * Created by Ttt
 */
public class ExportEvent extends DevBaseEvent<String> {

    public ExportEvent(int code) {
        super(code);
    }

    public ExportEvent(int code, String data) {
        super(code, data);
    }

    public ExportEvent(int code, String data, Object tag) {
        super(code, data, tag);
    }
}