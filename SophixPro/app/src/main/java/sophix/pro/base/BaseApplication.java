package sophix.pro.base;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.taobao.sophix.SophixManager;

/**
 * detail: 项目 Application 在 SophixStubApplication 中注解 SophixEntry
 * Created by Ttt
 */
public class BaseApplication extends Application{

    // 生成补丁
    // https://help.aliyun.com/document_detail/53247.html

    public static final String TAG = "BaseApplication";

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "BaseApplication - onCreate()");

        sContext = getApplicationContext();
        // Hotfix拉取不到补丁排查步骤
        // https://help.aliyun.com/knowledge_detail/65433.html
        // 扫描新的路径 -> 这句必须调用, 不然无法拉取需要修复的补丁
        SophixManager.getInstance().queryAndLoadNewPatch();
    }

    /**
     * 获取全局上下文
     * @return
     */
    public static Context getContext(){
        return sContext;
    }
}
