package t.app.info.base.event;

import dev.base.DevBaseEvent;

/**
 * detail: 搜索事件
 * Created by Ttt
 */
public class SearchEvent extends DevBaseEvent<String> {

    public SearchEvent(int code) {
        super(code);
    }

    public SearchEvent(int code, String data) {
        super(code, data);
    }

    public SearchEvent(int code, String data, Object tag) {
        super(code, data, tag);
    }
}