package t.app.info.activitys;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import dev.lib.other.EventBusUtils;
import dev.utils.app.AppUtils;
import dev.utils.app.ClickUtils;
import dev.utils.app.PermissionUtils;
import dev.utils.app.ViewUtils;
import dev.utils.app.assist.manager.ActivityManager;
import dev.utils.app.info.AppInfoBean;
import dev.utils.app.toast.ToastTintUtils;
import t.app.info.R;
import t.app.info.base.BaseActivity;
import t.app.info.base.BaseFragment;
import t.app.info.base.config.Constants;
import t.app.info.base.event.ExportEvent;
import t.app.info.base.event.FragmentEvent;
import t.app.info.base.event.SearchEvent;
import t.app.info.beans.TypeEnum;
import t.app.info.fragments.AppListFragment;
import t.app.info.fragments.DeviceInfoFragment;
import t.app.info.fragments.QueryApkFragment;
import t.app.info.fragments.ScreenInfoFragment;
import t.app.info.fragments.SettingFragment;
import t.app.info.utils.ProUtils;
import t.app.info.utils.QuerySDCardUtils;

/**
 * detail: 首页
 * Created by Ttt
 */
public class MainActivity extends BaseActivity {

    // Fragment管理
    private FragmentManager mFgManager;
    // 判断当前的 Fragment 索引
    private int mFragmentPos = -1;
    // 当前Menu 索引
    private static int mMenuPos = -1;
    /** Fragments */
    private List<BaseFragment> mFragments = new ArrayList<>();
    // ==== View ====
    @BindView(R.id.am_toolbar)
    Toolbar am_toolbar;
    @BindView(R.id.am_drawer_layout)
    DrawerLayout am_drawer_layout;
    @BindView(R.id.am_nav_view)
    NavigationView am_nav_view;
    @BindView(R.id.am_top_btn)
    FloatingActionButton am_top_btn;
    // 获取搜索View
    SearchView searchView;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化方法
        initMethodOrder();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 重置处理
        ProUtils.reset();
        // 销毁搜索线程资源
        setSearchRunnStatus(true);
    }

    @Override
    public void initMethodOrder() {
        // 初始化其他操作
        initOtherOperate();
        // 初始化事件
        initListeners();
    }

    @Override
    public void onBackPressed() {
        // 如果显示了侧边栏, 则关闭
        if (am_drawer_layout.isDrawerOpen(GravityCompat.START)) {
            am_drawer_layout.closeDrawer(GravityCompat.START);
        } else {
            // 判断是否显示, 是的话则关闭
            if (searchView != null && !searchView.isIconified()) {
                searchView.setQuery("", false); // 如果不增加，则会清空内容先
                searchView.setIconified(true);
                return;
            }
            // 判断是否首页 - 我的应用, 不是则切换回来
            if (mFragmentPos != 0) {
                toggleFragment(0);
                // 设置选中第一个
                am_nav_view.setCheckedItem(R.id.nav_user_apps);
                // 设置文案
                am_toolbar.setTitle(R.string.user_apps);
                return;
            }
            // 判断是否双击退出
            if (ClickUtils.isFastDoubleClick("quit")) {
                super.onBackPressed();
            } else {
                ToastTintUtils.info(AppUtils.getString(R.string.clickReturn));
                return;
            }
        }
    }

    // ==

    @Override // 初始化其他操作
    public void initOtherOperate() {
        super.initOtherOperate();

        // Toolbar
        // https://blog.csdn.net/carlos1992/article/details/50707695
        // https://www.cnblogs.com/mjsn/p/6150824.html
        // 刷新Menu
        // https://blog.csdn.net/luohaowang320/article/details/38556383
        // 去掉阴影
        // https://blog.csdn.net/dreamsever/article/details/52672739

        // 重置处理
        ProUtils.reset();
        // 销毁搜索线程资源
        setSearchRunnStatus(false);
        // 初始化 Fragment 集
        initFragments();
        // 设置文案
        am_toolbar.setTitle(R.string.user_apps);
        // 设置侧边栏
        setSupportActionBar(am_toolbar);
        // 设置切换动画事件等
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, am_drawer_layout, am_toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        am_drawer_layout.addDrawerListener(toggle);
        toggle.syncState();
        // 默认选中第一个
        am_nav_view.setCheckedItem(R.id.nav_user_apps);
        // 延迟请求权限
        vHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 请求权限
                requestPermission();
            }
        }, 1000);
    }

    @Override // 初始化事件
    public void initListeners() {
        super.initListeners();
        // 设置点击处理 = 双击Title
        am_toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 判断是否双击
                if (ClickUtils.isFastDoubleClick(mFragmentPos + "")) {
                    try {
                        // 双击回到顶部
                        mFragments.get(mFragmentPos).onScrollTop();
                    } catch (Exception e) {
                    }
                }
            }
        });
        // 设置Item 点击事件
        am_nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_user_apps:
                        toggleFragment(0);
                        // 设置文案
                        am_toolbar.setTitle(R.string.user_apps);
                        break;
                    case R.id.nav_system_apps:
                        toggleFragment(1);
                        // 设置文案
                        am_toolbar.setTitle(R.string.system_apps);
                        break;
                    case R.id.nav_phone_info:
                        toggleFragment(2);
                        // 设置文案
                        am_toolbar.setTitle(R.string.phone_info);
                        break;
                    case R.id.nav_screen_info:
                        toggleFragment(3);
                        // 设置文案
                        am_toolbar.setTitle(R.string.screen_info);
                        break;
                    case R.id.nav_query_apk:
                        toggleFragment(4);
                        // 设置文案
                        am_toolbar.setTitle(R.string.query_apk);
                        break;
                    case R.id.nav_setting:
                        toggleFragment(5);
                        // 设置文案
                        am_toolbar.setTitle(R.string.setting);
                        break;
                }
                am_drawer_layout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        // 设置点击事件 = 回到顶部
        am_top_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // 回到顶部
                    mFragments.get(mFragmentPos).onScrollTop();
                } catch (Exception e) {
                }
            }
        });
    }

    /** 初始化 Fragment 集 */
    private void initFragments() {
        // 添加用户
        mFragments.add(AppListFragment.getInstance(AppInfoBean.AppType.USER));
        // 添加系统应用
        mFragments.add(AppListFragment.getInstance(AppInfoBean.AppType.SYSTEM));
//        // 添加全部应用
//        mFragments.add(AppListFragment.getInstance(AppInfoBean.AppType.ALL));
        // 添加手机信息
        mFragments.add(DeviceInfoFragment.getInstance());
        // 添加屏幕信息
        mFragments.add(ScreenInfoFragment.getInstance());
        // 添加扫描APK
        mFragments.add(QueryApkFragment.getInstance());
        // 添加设置
        mFragments.add(SettingFragment.getInstance());
        // --
        // 得到Fragment管理器
        mFgManager = getSupportFragmentManager();
        // 初始化添加对应的布局
        FragmentTransaction fragmentTransaction = mFgManager.beginTransaction();
        // 初始化 Fragment 集
        for (int i = 0, len = mFragments.size(); i < len; i++) {
            // 添加到集合中
            fragmentTransaction.add(R.id.am_linear, mFragments.get(i), i + "");
            // 隐藏布局
            fragmentTransaction.hide(mFragments.get(i));
        }
        // 提交保存
        fragmentTransaction.commit();
        // 默认显示第一个
        toggleFragment(0);
    }

    /**
     * 切换 Fragment 处理
     * @param pos
     */
    private void toggleFragment(int pos) {
        // 判断是否想等
        if (pos != mFragmentPos) {
            // 初始化添加对应的布局
            FragmentTransaction fragmentTransaction = mFgManager.beginTransaction();
            // 判断准备显示的 Fragment
            BaseFragment fragment = mFragments.get(pos);
            // 如果默认未初始化, 则直接显示
            if (mFragmentPos < 0) {
                fragmentTransaction.show(fragment).commit();
            } else {
                fragmentTransaction.hide(mFragments.get(mFragmentPos)).show(fragment).commit();
            }
            // 重新保存索引
            mFragmentPos = pos;
            // 保存新的索引
            mMenuPos = pos;
            // 切换改变处理
            toggleChange();
        }
    }

    /**
     * 切换改变处理
     */
    private void toggleChange() {
        switch (mFragmentPos) {
            case 0: // 我的应用
            case 1: // 系统应用
            case 4: // 扫描APK
                ViewUtils.setVisibility(true, am_top_btn);
                break;
            case 2: // 手机信息
            case 3: // 屏幕信息
            case 5: // 设置
            default:
                ViewUtils.setVisibility(false, am_top_btn);
                break;
        }
        // 通知系统更新菜单
        supportInvalidateOptionsMenu();
        // 发送切换 Fragment 通知事件
        EventBusUtils.sendEvent(new FragmentEvent(Constants.Notify.H_TOGGLE_FRAGMENT_NOTIFY));
    }

    /**
     * 判断是否对应的类型
     * @return
     */
    public static TypeEnum getTypeEnum(){
        switch (mMenuPos) {
            case 0: // 我的应用
                return TypeEnum.APP_USER;
            case 1: // 系统应用
                return TypeEnum.APP_SYSTEM;
            case 2: // 手机信息
                return TypeEnum.DEVICE_INFO;
            case 3: // 屏幕信息
                return TypeEnum.SCREEN_INFO;
            case 4: // 扫描APK
                return TypeEnum.QUERY_APK;
            case 5: // 设置
                return TypeEnum.SETTING;
        }
        return TypeEnum.NONE;
    }

    // == Menu ==

    @Override // 默认创建Menu显示
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bar_menu_apps, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override // 准备显示Menu
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        switch (mFragmentPos) {
            case 0: // 我的应用
            case 1: // 系统应用
            case 4: // 扫描APK
                getMenuInflater().inflate(R.menu.bar_menu_apps, menu);
                // 初始化搜索操作
                initSearchOperate(menu);
                break;
            case 2: // 手机信息
            case 3: // 屏幕信息
                getMenuInflater().inflate(R.menu.bar_menu_device, menu);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bma_refresh: // 刷新
                switch (mFragmentPos) {
                    case 0: // 手机应用
                        ProUtils.clearAppData(AppInfoBean.AppType.USER);
                        break;
                    case 1: // 系统应用
                        ProUtils.clearAppData(AppInfoBean.AppType.SYSTEM);
                        break;
                    case 4: // 扫描APK
                        // 清空数据
                        QuerySDCardUtils.getInstance().reset();
                        break;
                }
                // 发送刷新通知事件
                EventBusUtils.sendEvent(new FragmentEvent(Constants.Notify.H_REFRESH_NOTIFY, mFragmentPos));
                break;
            case R.id.bmd_export_item: // 导出
                // 需要的权限
                String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                // 判断是否存在读写权限
                if(ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                    // 发送导出设备信息通知事件
                    EventBusUtils.sendEvent(new ExportEvent(Constants.Notify.H_EXPORT_DEVICE_MSG_NOTIFY));
                } else {
                    PermissionUtils.permission(permission).callBack(new PermissionUtils.PermissionCallBack() {
                        @Override
                        public void onGranted(PermissionUtils permissionUtils) {
                            // 发送导出设备信息通知事件
                            EventBusUtils.sendEvent(new ExportEvent(Constants.Notify.H_EXPORT_DEVICE_MSG_NOTIFY));
                        }

                        @Override
                        public void onDenied(PermissionUtils permissionUtils) {
                            // 提示导出失败
                            ToastTintUtils.error(AppUtils.getString(R.string.export_fail));
                        }
                    }).request();
                }
                break;
        }
        return true;
    }

    // ==

    /**
     * 初始化搜索操作
     * @param menu
     */
    private void initSearchOperate(Menu menu) {
        // https://www.jianshu.com/p/16f9e995e454
        // https://www.cnblogs.com/tianzhijiexian/p/4226675.html
        // https://www.jianshu.com/p/7c1e78e91506
        // 获取搜索Item
        MenuItem searchItem = menu.findItem(R.id.bma_search);
        // 初始化搜索View
        searchView = (SearchView) searchItem.getActionView();
        // 默认提示
        searchView.setQueryHint(getString(R.string.input_packname_aname_query));
//        // 初始化事件
//        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item) { // 展开
//                ToastTintUtils.showShort(mContext, "展开");
//                // 销毁搜索线程资源
//                setSearchRunnStatus(true);
//                // 发送通知
//                BaseApplication.sDevObservableNotify.onNotify(Constants.Notify.H_SEARCH_EXPAND);
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item) { // 合并
//                ToastTintUtils.showShort(mContext, "合并");
//                // 销毁搜索线程资源
//                setSearchRunnStatus(true);
//                // 发送通知
//                BaseApplication.sDevObservableNotify.onNotify(Constants.Notify.H_SEARCH_COLLAPSE);
//                return true;
//            }
//        });
        // 搜索框展开时后面叉叉按钮的点击事件
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                // 销毁搜索线程资源
                setSearchRunnStatus(true);
                // 发送搜索合并通知事件
                EventBusUtils.sendEvent(new SearchEvent(Constants.Notify.H_SEARCH_COLLAPSE));
                return false;
            }
        });
        // 搜索图标按钮(打开搜索框的按钮)的点击事件
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 销毁搜索线程资源
                setSearchRunnStatus(true);
                // 发送搜索展开通知事件
                EventBusUtils.sendEvent(new SearchEvent(Constants.Notify.H_SEARCH_EXPAND));
            }
        });
        // 设置搜索文本监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override // 当点击搜索按钮时触发该方法
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override // 当搜索内容改变时触发该方法
            public boolean onQueryTextChange(String newText) {
                // 开始进行搜索 - 节省不必要的操作,使用Handler + Runnable,减少搜索处理的次数
                vHandler.removeCallbacks(getSearchRunnable());
                vHandler.postDelayed(getSearchRunnable(), 250);
                return false;
            }
        });
    }

    // ============= 搜索操作 ==============
    /** 搜索线程*/
    private static Runnable searchRunn = null;

    /**
     * 设置搜索线程状态
     * @param isDestroy
     */
    private void setSearchRunnStatus(boolean isDestroy) {
        // 表示属于销毁,则移除之前的任务
        if (isDestroy) {
            // 退出页面,则停止操作
            vHandler.removeCallbacks(getSearchRunnable());
        } else { // 非销毁 - 初始化
            searchRunn = null;
        }
    }

    /**
     * 获取搜索线程
     * @return
     */
    private Runnable getSearchRunnable() {
        if (searchRunn == null) {
            searchRunn = new Runnable() {
                @Override
                public void run() {
                    // 延迟触发
                    vHandler.sendEmptyMessage(Constants.Notify.H_SEARCH_INPUT_CONTENT);
                }
            };
        }
        return searchRunn;
    }

    // ==

    /** View 操作Handler */
    private Handler vHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 如果页面已经关闭,则不进行处理
            if (ActivityManager.isFinishingCtx(mContext)) {
                return;
            }
            // 判断通知类型
            switch (msg.what) {
                // 搜索输入的内容
                case Constants.Notify.H_SEARCH_INPUT_CONTENT:
                    try {
                        // 发送搜索输入内容通知事件
                        EventBusUtils.sendEvent(new SearchEvent(Constants.Notify.H_SEARCH_INPUT_CONTENT, searchView.getQuery().toString()));
                    } catch (Exception e) {
                    }
                    break;
            }
        }
    };

    /** 请求权限 */
    private void requestPermission() {
        // 需要的权限
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        // 判断是否存在读写权限
        if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) { // 已经存在权限
        } else {
            // 请求权限
            PermissionUtils.permission(permission).request();
        }
    }
}
