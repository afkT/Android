package t.app.info.base.event;

import dev.base.DevBaseEvent;

/**
 * detail: 搜索App通知事件
 * Created by Ttt
 */
public class QueryAppEvent extends DevBaseEvent<String> {

    public QueryAppEvent(int code) {
        super(code);
    }

    public QueryAppEvent(int code, String data) {
        super(code, data);
    }

    public QueryAppEvent(int code, String data, Object tag) {
        super(code, data, tag);
    }
}
