package bugly.hotfix.pro;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    // 热更新使用指南
    // https://bugly.qq.com/docs/user-guide/instruction-manual-android-hotfix/?v=20180521124306

    // 热更新使用详解
    // https://bugly.qq.com/docs/user-guide/instruction-manual-android-hotfix-demo/?v=20180521124306

    // 官方 demo
    // https://github.com/BuglyDevTeam/Bugly-Android-Demo


    Button vid_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vid_btn = findViewById(R.id.vid_btn);

        vid_btn.setText("点击显示结果");
    }

    // 需要注意的是, 假设我发了个包
    // 然后这个包有bug, 这个时候以这个基准包, 生成了Tinker补丁, 然后发布
    // 但是发现修复了第一个bug后, 导致出现了第二个bug
    // 这个时候打的差异包, 必须还是最开始的基准包（发包的apk）, 这样才能直接修复没有更新过补丁的用户
    // 以及修复，更新过一次补丁，导致二次bug的用户
    // 以此类推，每次的基准包都是以发包的版本为基准包, 只是修复的差异包，一直包含最新的代码

    // 反正步骤 如下, 只要基础包生成过一次后, 或者指定位置后, 只需要每次修复都是 buildTinkerPatchRelease 上传补丁就行
    // https://bugly.qq.com/docs/user-guide/instruction-manual-android-hotfix-demo/?v=20180521124306
    // 每次先生成基准包 在 :app -> Tasks -> build -> assembleRelease 构建后，复制最新的地址替换 baseApkDir
    // 接着进行修复bug, 修复好了后, 更改 tinkerId
    // 并且进行 :app -> Tasks -> tinker-support -> buildTinkerPatchRelease 生成补丁包

    @Override
    public void onClick(View v) {

        try {
            String data = null;
            data.split("1");
        } catch (Exception e){
            // 原始崩溃
            Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

            // 第一次修复
//            Toast.makeText(MainActivity.this, "bug 已修复", Toast.LENGTH_SHORT).show();

            // 第二次修复
//            Toast.makeText(MainActivity.this, "bug 二次修复, 并且添加: " + getPackageName(), Toast.LENGTH_SHORT).show();
        }
    }
}
