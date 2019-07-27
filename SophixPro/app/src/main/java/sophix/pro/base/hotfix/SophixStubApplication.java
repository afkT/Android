package sophix.pro.base.hotfix;

import android.content.Context;
import android.support.annotation.Keep;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.taobao.sophix.PatchStatus;
import com.taobao.sophix.SophixApplication;
import com.taobao.sophix.SophixEntry;
import com.taobao.sophix.SophixManager;
import com.taobao.sophix.listener.PatchLoadStatusListener;

import java.util.ArrayList;

import sophix.pro.base.BaseApplication;

/**
 * Sophix入口类，专门用于初始化Sophix，不应包含任何业务逻辑。
 * 此类必须继承自SophixApplication，onCreate方法不需要实现。
 * 此类不应与项目中的其他类有任何互相调用的逻辑，必须完全做到隔离。
 * AndroidManifest中设置application为此类，而SophixEntry中设为原先Application类。
 * 注意原先Application里不需要再重复初始化Sophix，并且需要避免混淆原先Application类。
 * 如有其它自定义改造，请咨询官方后妥善处理。
 */
public class SophixStubApplication extends SophixApplication {

    private final String TAG = "SophixStubApplication";

    @Keep // 此处SophixEntry应指定真正的Application，并且保证RealApplicationStub类名不被混淆。
    @SophixEntry(BaseApplication.class) // 正常使用的BaseApplication类
    static class RealApplicationStub {
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.d(TAG, "SophixStubApplication - attachBaseContext");
        // 如果需要使用MultiDex，需要在此处调用。
        MultiDex.install(this);
        // 初始化 Sophix
        initSophix();
    }

    // aliyun-emas-services.json

    String IDSECRET = "24917713";
    String APPSECRET = "a2e588af31e2cca975ca1f6b6464c1b7";
    String RSASECRET = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCDNlEj/Kj7/epsZO6eNnm0GcEmlkf1SRkSZxMNu6RnhSavER8ZHpRRneAzU7S8FumoqJhWFYt1H3hJ1dlHlXOGsr8Pj7DXJeEUSd7amjMdPKg3AG5tp9IPbdCqV/tpyEpukbOoCn70uZLoO53OgEcz899lZkJ1tUL+NRjEpIbXG7sX9HdTImLADQLRb+wNcKGtPd8faxcZPzshseA2akK/ckPARbTJ0HYFpAF8MhT+kIV1x42OnULF+k0Al3vxeqKHd83uSWs7vzAPHSIlKF0qViRIpxJzn5RnUTqKnvXAocdgqvABNlMDZ8DqbISJ2v+Ah+Nvx2GPDH5zQ+b1quR1AgMBAAECggEAI+G8uryjcZX0f4PyhS2chpXsbiL2tqswwgmR+lVwFichdwqVmIb2RIOKBj9D05Pofgjs782gf30Fphtm+d5y40kKWrUOxSdEsAmredhlVf96eOATUsVr+nAregnpZPoy8J3DpZaJNV9yPzPeadRGpHxygQY/8nSJR385sLzVawlvrdgKJCovUW5L4lorgs8gc6ALu7scRJ4oCPMU9FZ53ITbssOOKSbZSX7ZvGvXueHccnpdCUtKuMipg+o15Ii3AO0i/Zhd2j9SlBBAgputPGThBcbria/+mGNKs2uXj4ebgRrNCnThIUnUXNICfLEpAwyPjPuFdO+Vev/SmZaswQKBgQD2XoY0gEaTdMUAuD8LALyXByCb5UgmG6bLdxolfhf+3++wHlEPkqDF5yNqVInV3uFG65lD3R2YeGnBy0fbTIEmfQ95vUWOkUcxOIRPYyPjKMpWfiIQjqhf57b9I/C0lpM/6N67iVZxOqP9slK2nbHRYVXmeaI/HB+TDUzUMYohiQKBgQCIV2N2Yuxxn9H5WOW3z8lkg4olNH8aPz08r1GucabTA6rMNAHUqEYFpcVdfGzZ4cEm4WNYlRGQIiZSoogGIwJKR6fk9wCOUrC4XoNGTds57SzhQ+gmI4lpzSNos0vonYNNJmcDoI6N2S1/pV52uOLK0M9j/C+rg0e7ZTQobeQMjQKBgQCSpObbHzNjF9EfQYv8355hgxOM16ffTvw0BZuhj3F2+xIvUYxMaamawp7Y7qLwk26iCSA8PcQc+idw+6+0v8/r+eqZOujJGH44tPxESK2Wy7PMUd2y30cndsP730WqKCpTkyfolrHGSfJM0rTbD/hEudwoiGPjjrjYjcUaVLZ0iQKBgB5qyKDXzwJGcpTbdVx9ueN3yTElthmH9ER+pI1zg5FeJxOpoqLr67tE0XRa1voA+JORAxX3I9TxGXWkFw6vbspdlCJrzK7z2cM4KrdblUYmGLYk7Tzc/sIg5v5rycqaoXlr0N2pS0RwKEtq/FkmREqVLT+UOppcDnvLR0ihw5fdAoGBALusARKMhzbmVM9AjupQ098pRHZUXfT1xT4dVNBsDzm8jdRu7J1Is/sbQCar3jKzyXaIlD+Fs/t9CYFePAM1sWGyEOoyVr1mnT0wYcvkDE26qLjD+PGoB2R7FAPHinUBeYKvj6XH8Cdrt7PLgSKhElb/D90EFpEF2okscWHCBpYJ";

    /**
     * 初始化 Sophix
     * 稳健接入
     * https://help.aliyun.com/document_detail/61082.html
     */
    private void initSophix() {
        // 需要注意的是 AppVersion
        // Sophix 修复的补丁，是通过后台创建的版本, 就是App 设置的AppVersion
        // 所以假设 3.1.1 版本出现bug, 需要热修复, 则在后台创建个 3.1.1的版本, 然后app 只需要修复bug, 版本也不需要升级为3.1.2, 还是照样3.1.1
        // 然后通过使用工具类 https://help.aliyun.com/document_detail/53247.html , 检验旧的包、新的修复bug, 导出补丁jar
        // 然后上传分发修复

        // 参数使用说明
        // https://help.aliyun.com/document_detail/53240.html
        // 初始化管理类
        final SophixManager instance = SophixManager.getInstance();
        // 设置参数, 并初始化
        instance.setContext(this)
                .setAppVersion(getAppVersion()) // 初始化版本 -> 当前app版本同控制台填写版本一致, 并且进行热修复时, 不需要改动 build.gradle 内部的版本信息等
                .setSecretMetaData(IDSECRET, APPSECRET, RSASECRET)
                .setEnableDebug(true) // 是否debug 模式
                .setEnableFullLog()
                .setPatchLoadStatusStub(new PatchLoadStatusListener() { // 设置加载监听
                    @Override
                    public void onLoad(final int mode, final int code, final String info, final int handlePatchVersion) {
//                        PatchLoadStatusListener接口
//                        该接口需要自行实现并传入initialize方法中, 补丁加载状态会回调给该接口, 参数说明如下:
//                        mode: 无实际意义, 为了兼容老版本, 默认始终为0
//                        code: 补丁加载状态码, 详情查看PatchStatus类说明
//                        info: 补丁加载详细说明
//                        handlePatchVersion: 当前处理的补丁版本号, 0:无 -1:本地补丁 其它:后台补丁

                        ArrayList<String> list = new ArrayList<>();
                        list.add("处理版本 handlePatchVersion: " + handlePatchVersion);
                        list.add("状态码 code: " + code);
                        list.add("信息 info: " + info);
                        Log.i(TAG, list.toString());
                        // 常见状态码
                        // https://help.aliyun.com/document_detail/53240.html
                        switch (code){
                            case PatchStatus.CODE_LOAD_SUCCESS: // 加载阶段, 成功
                                Log.e(TAG, "加载阶段, 成功 - sophix load patch success!");
                                break;
                            case PatchStatus.CODE_LOAD_RELAUNCH: // 预加载阶段, 需要重启
                                // 如果需要在后台重启，建议此处用SharePreference保存状态。
                                Log.e(TAG, "预加载阶段, 需要重启 - sophix preload patch success. restart app to make effect.");

//                                可以在PatchLoadStatusListener监听到CODE_LOAD_RELAUNCH后在合适的时机，调用此方法杀死进程。
//                                注意，不可以直接 Process.killProcess(Process.myPid())来杀进程，这样会扰乱Sophix的内部状态。
//                                因此如果需要杀死进程，建议使用这个方法，它在内部做一些适当处理后才杀死本进程。
//                                SophixManager.getInstance().killProcessSafely();
                                break;
                        }
                    }
                }).initialize();
    }

    /**
     * 获取版本
     * @return
     */
    public String getAppVersion(){
        String appVersion = "0.0.0";
        try {
            appVersion = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (Exception ignore) {
        }
        Log.d(TAG, "SophixStubApplication - getAppVersion: " + appVersion);
        return appVersion;
    }
}
