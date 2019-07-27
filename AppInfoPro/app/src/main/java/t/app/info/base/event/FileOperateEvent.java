package t.app.info.base.event;

import dev.base.DevBaseEvent;

/**
 * detail: 文件操作相关事件
 * Created by Ttt
 */
public class FileOperateEvent extends DevBaseEvent<String> {

    public FileOperateEvent(int code) {
        super(code);
    }

    public FileOperateEvent(int code, String data) {
        super(code, data);
    }

    public FileOperateEvent(int code, String data, Object tag) {
        super(code, data, tag);
    }
}