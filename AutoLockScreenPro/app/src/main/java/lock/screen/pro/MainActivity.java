package lock.screen.pro;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import cn.jpush.android.api.JPushInterface;
import lock.screen.pro.utils.LockScreenUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 表示属于debug
        JPushInterface.setDebugMode(true);
        // 初始化
        JPushInterface.init(getApplicationContext());
        // 绑定别名
        JPushInterface.setAlias(this, 101010, "123456");

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(MainActivity.this, "锁屏", Toast.LENGTH_SHORT).show();
//                // 锁屏
//                LockScreenUtils.lockScreen();
//            }
//        }, 5000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 申请权限
        if (LockScreenUtils.reqPermission(this)){
            // 存在权限不处理
            Toast.makeText(this, "锁屏权限已开启", Toast.LENGTH_SHORT).show();
            // 缩小到桌面
            startHomeActivity();
        }
    }

    /**
     * 回到桌面 -> 同点击Home键效果
     */
    public void startHomeActivity() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        this.startActivity(homeIntent);
    }
}
