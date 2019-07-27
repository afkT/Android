package replugin.pro;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // 中文文档
    // https://github.com/Qihoo360/RePlugin/blob/dev/README_CN.md

    // 主程序 项目
    // https://github.com/Qihoo360/RePlugin/wiki/主程序接入指南

    // 插件项目 - 属于单独打包出来的 apk
    // https://github.com/Qihoo360/RePlugin/wiki/插件接入指南

    // 完整目录结构
    // https://github.com/Qihoo360/RePlugin/wiki/详细教程

    // 插件安装、卸载更新等
    // https://github.com/Qihoo360/RePlugin/wiki/插件的管理

    // 插件组件、跳转使用等
    // https://github.com/Qihoo360/RePlugin/wiki/插件的组件

    // 插件apk地址, 使用外置插件方式
    private String pluginApk = "plugin_demo.apk";
    // 插件名 -> 其实就是插件的包名, 具有唯一性
    private String pluginPack = "replugin.demo";

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.vid_install:
                try {
                    // 安装插件
                    PluginInfo pi = RePlugin.install("/sdcard/" + pluginApk);
                    if (pi != null) {
                        // 预加载插件
                        RePlugin.preload(pi);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.vid_uninstall:
                try {
                    // 卸载插件
                    RePlugin.uninstall(pluginPack);
                } catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.vid_start:
                try {
                    // 跳转插件页面
                    RePlugin.startActivity(MainActivity.this, RePlugin.createIntent(pluginPack,
                            "replugin.demo.MainActivity"));
                } catch (Exception e){
                    e.printStackTrace();
                }
                break;
        }
    }
}
