package t.app.info.base.event;

import dev.base.DevBaseEvent;

/**
 * detail: 排序通知事件
 * Created by Ttt
 */
public class SortEvent extends DevBaseEvent<String> {

    public SortEvent(int code) {
        super(code);
    }

    public SortEvent(int code, String data) {
        super(code, data);
    }

    public SortEvent(int code, String data, Object tag) {
        super(code, data, tag);
    }
}
