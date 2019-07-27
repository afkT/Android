package sophix.pro;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import sophix.pro.utils.AppUtils;
import sophix.pro.utils.ToastUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "SophixMain";

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = MainActivity.this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.vid_btn:
                String text = hotfixToast();
                // 显示内容
                ((TextView) findViewById(R.id.vid_tv)).setText(text);
                // 提示 Toast
                ToastUtils.showShort(mContext, text);
                break;
        }
    }

    // 热修复key
    private int hotfixNumber = 1;

    /**
     * 热修复提示内容
     * @return
     */
    private String hotfixToast(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("verName: " + AppUtils.getAppVersionName());
        stringBuilder.append("\nverCode: " + AppUtils.getAppVersionCode());

        // 第一次热修复
        stringBuilder.append(addHotfix1());
        // 第二次热修复
        stringBuilder.append(addHotfix2());

        // 热修复次数
        stringBuilder.append("\n\n\n热修复次数 hotfixNumber: " + hotfixNumber);

        String data = stringBuilder.toString();
        Log.d(TAG, data);
        return data;
    }

    /**
     * 添加第一次热修复, 分别打开注释
     * @return
     */
    private String addHotfix1(){
        StringBuilder stringBuilder = new StringBuilder();
        // =============
//        stringBuilder.append("\n\n\n==============");
//        stringBuilder.append("\n第一次热修复");
//        stringBuilder.append("\n==============");
//        stringBuilder.append("\n\n" + new Gson().toJson(new TempInfo()));
//        hotfixNumber ++;
        // =============
        return stringBuilder.toString();
    }

//    private class TempInfo {
//
//        String uuid;
//
//        public TempInfo() {
//            uuid = UUID.randomUUID().toString();
//        }
//    }

    /**
     * 添加第二次热修复, 分别打开注释
     * @return
     */
    private String addHotfix2(){
        StringBuilder stringBuilder = new StringBuilder();
        // =============
//        stringBuilder.append("\n\n\n==============");
//        stringBuilder.append("\n第二次热修复");
//        stringBuilder.append("\n==============");
//        stringBuilder.append("\n\n" + new Gson().toJson(new UserInfo("林义", "admin", "123798q", "隆东强")));
//        hotfixNumber ++;
        // =============
        return stringBuilder.toString();
    }

//    private class UserInfo {
//
//        String userName;
//
//        String userAccount;
//
//        String userPwd;
//
//        String userNickName;
//
//        public UserInfo(String userName, String userAccount, String userPwd, String userNickName) {
//            this.userName = userName;
//            this.userAccount = userAccount;
//            this.userPwd = userPwd;
//            this.userNickName = userNickName;
//        }
//    }
}
