package t.app.info.base.event;

import dev.base.DevBaseEvent;

/**
 * detail: Fragment 相关事件
 * Created by Ttt
 */
public class FragmentEvent extends DevBaseEvent<Integer> {

    public FragmentEvent(int code) {
        super(code);
    }

    public FragmentEvent(int code, Integer data) {
        super(code, data);
    }

    public FragmentEvent(int code, Integer data, Object tag) {
        super(code, data, tag);
    }
}