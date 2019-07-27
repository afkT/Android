package t.app.info.base.event;

import dev.base.DevBaseEvent;

/**
 * detail: 搜索文件通知事件
 * Created by Ttt
 */
public class QueryFileEvent extends DevBaseEvent<Integer> {

    public QueryFileEvent(int code) {
        super(code);
    }

    public QueryFileEvent(int code, Integer data) {
        super(code, data);
    }

    public QueryFileEvent(int code, Integer data, Object tag) {
        super(code, data, tag);
    }
}
