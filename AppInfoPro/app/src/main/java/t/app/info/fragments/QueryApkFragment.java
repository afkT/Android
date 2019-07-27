package t.app.info.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.utils.app.assist.manager.ActivityManager;
import dev.utils.app.info.AppInfoBean;
import dev.utils.app.logger.DevLogger;
import dev.utils.common.DevCommonUtils;
import t.app.info.R;
import t.app.info.activitys.MainActivity;
import t.app.info.adapters.FileResAdapter;
import t.app.info.base.BaseFragment;
import t.app.info.base.config.Constants;
import t.app.info.base.event.FileOperateEvent;
import t.app.info.base.event.FragmentEvent;
import t.app.info.base.event.QueryFileEvent;
import t.app.info.base.event.SearchEvent;
import t.app.info.beans.TypeEnum;
import t.app.info.beans.item.FileResItem;
import t.app.info.utils.QuerySDCardUtils;
import t.app.info.widgets.StateLayout;

/**
 * detail: 设置信息 - Fragment
 * Created by Ttt
 */
public class QueryApkFragment extends BaseFragment {

    // ===== View =====
    @BindView(R.id.fqa_recycleview)
    RecyclerView fqa_recycleview;
    @BindView(R.id.fqa_statelayout)
    StateLayout fqa_statelayout;
    // ======== 其他对象 ========
    // 适配器
    private FileResAdapter mFileResAdapter;

    /**
     * 获取对象,并且设置数据
     */
    public static BaseFragment getInstance() {
        QueryApkFragment bFragment = new QueryApkFragment();
        return bFragment;
    }

    // ==

    @Override
    public int getLayoutId() {
        return R.layout.fragment_query_apk;
    }

    @Override
    protected void onInit(View view, ViewGroup container, Bundle savedInstanceState) {
        // 初始化View
        unbinder = ButterKnife.bind(this, view);
        // 注册 EventBus
        registerEventOperate(true);
        // 初始化方法
        initMethodOrder();
    }

    // ==

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden) {
            onPause();
            onStop();
        } else {
            onStart();
            onResume();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(this.isHidden()) {
            return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(this.isHidden()) {
            return;
        }
        // 查询文件
        QuerySDCardUtils.getInstance().querySDCardRes();
    }

    @Override
    public void initValues() {
        super.initValues();
        // 初始化适配器并绑定
        mFileResAdapter = new FileResAdapter(getActivity());
        fqa_recycleview.setAdapter(mFileResAdapter);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        fqa_recycleview.setLayoutManager(manager);
    }

    // ==

    /** View 操作Handler */
    Handler vHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 如果页面已经关闭,则不进行处理
            if (ActivityManager.isFinishingCtx(mContext)) {
                return;
            }
            // 判断通知类型
            switch (msg.what) {
                case Constants.Notify.H_REFRESH_NOTIFY: // 刷新通知
                    mFileResAdapter.clearData();
                    break;
                case Constants.Notify.H_QUERY_FILE_RES_ING_NOTIFY: // 搜索中
                    // 表示刷新状态
                    fqa_statelayout.setState(StateLayout.State.REFRESH);
                    break;
                case Constants.Notify.H_QUERY_FILE_RES_END_NOTIFY:// 搜索结束
                    // 获取搜索状态
                    int queryState = msg.arg1;
                    // 判断是否搜索
                    if (isSearch) {
                        // 判断是否存在数据
                        if (listSearchs.size() != 0) {
                            // 刷新状态
                            fqa_statelayout.setState(StateLayout.State.QUERY_END, DevCommonUtils.length(listSearchs));
                            // 判断根据操作类型判断
                            mFileResAdapter.setData(listSearchs);
                        } else { // 判断是否输入内容
                            // 判断是否存在内容
                            String searchContent = "";
                            if (msg.obj != null) {
                                try {
                                    searchContent = (String) msg.obj;
                                } catch (Exception e) {
                                }
                            }
                            // 判断是否为null
                            boolean isEmpty = TextUtils.isEmpty(searchContent);
                            // 显示提示
                            if (isEmpty) {
                                // 获取搜索文件数据
                                ArrayList<FileResItem> listFiles = QuerySDCardUtils.getInstance().getListFileResItems();
                                // 判断是否存在数据
                                int size = DevCommonUtils.length(listFiles, -1);
                                // 刷新状态
                                fqa_statelayout.setState(((size == -1) ? StateLayout.State.REFRESH : StateLayout.State.QUERY_END), size);
                                // 判断根据操作类型判断
                                mFileResAdapter.setData(listFiles);
                            } else {
                                // 设置搜索没数据提示
                                fqa_statelayout.setStateToSearchNoData(searchContent);
                                // 刷新数据
                                mFileResAdapter.setData(new ArrayList<FileResItem>());
                            }
                        }
                    } else {
                        // 获取搜索文件数据
                        ArrayList<FileResItem> listFiles = QuerySDCardUtils.getInstance().getListFileResItems();
                        // 判断是否存在数据
                        int size = DevCommonUtils.length(listFiles, -1);
                        // 刷新状态
                        fqa_statelayout.setState(((size == -1) ? StateLayout.State.REFRESH : StateLayout.State.QUERY_END), size);
                        // 判断根据操作类型判断
                        mFileResAdapter.setData(listFiles);
                    }
                    break;
            }
        }
    };

    // == 外部开放方法 ==

    /**
     * 滑动到顶部
     */
    public void onScrollTop() {
        if (fqa_recycleview != null) {
            fqa_recycleview.scrollToPosition(0);
        }
    }

    // = 搜索相关 =
    // 判断是否搜索
    private boolean isSearch = false;
    // 搜索数据源
    private ArrayList<FileResItem> listSearchs = new ArrayList<>();

    /**
     * 筛选群组列表数据
     * @param listDatas 数据源
     * @param listSearchs 筛选结果
     * @param sContent 筛选关键字
     */
    private int filterApkList(List<FileResItem> listDatas, List<FileResItem> listSearchs, String sContent) {
        // 数据总数
        int size = 0;
        // 防止数据为null
        if (listDatas != null) {
            // 保存临时数据 - 主要是预防搜索途中,进行加载，导致遍历List中数据源改变，导致抛出异常 ConcurrentModificationException
            ArrayList<FileResItem> listTemps = new ArrayList<>(listDatas);
            // 进行遍历临时数据源
            for (int i = 0, count = listTemps.size(); i < count; i++) {
                // 获取单独的实体类
                FileResItem fileResItem = listTemps.get(i);
                // 获取App信息
                AppInfoBean appInfoBean = fileResItem.getAppInfoBean();
                // 判断是否包含
                if (DevCommonUtils.isContains(true, sContent, appInfoBean.getAppName(), appInfoBean.getAppPackName())) {
                    // 保存数据
                    listSearchs.add(fileResItem);
                    // 进行累加
                    size++;
                }
            }
        }
        return size;
    }

    // == 事件相关 ==

    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onFragmentEvent(FragmentEvent event) {
        DevLogger.dTag(TAG, "onFragmentEvent");
        if (event != null) {
            int code = event.getCode();
            switch (code){
                /** 切换Fragment 通知 */
                case Constants.Notify.H_TOGGLE_FRAGMENT_NOTIFY:
                    // 合并表示不属于搜索
                    isSearch = false;
                    // 清空数据
                    listSearchs.clear();
                    break;
                case Constants.Notify.H_REFRESH_NOTIFY:
                    // 类型相同才处理
                    if (MainActivity.getTypeEnum() == TypeEnum.QUERY_APK) {
                        // 设置空的数据
                        vHandler.sendEmptyMessage(Constants.Notify.H_REFRESH_NOTIFY);
                        // 查询文件
                        QuerySDCardUtils.getInstance().querySDCardRes();
                    }
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onFileOperateEvent(FileOperateEvent event) {
        DevLogger.dTag(TAG, "onFileOperateEvent");
        if (event != null) {
            int code = event.getCode();
            switch (code){
                case Constants.Notify.H_DELETE_APK_FILE_NOTIFY:
                    try {
                        // 获取路径地址
                        String apkUri = event.getData();
                        // 进行获取
                        ArrayList<FileResItem> lists = QuerySDCardUtils.getInstance().getListFileResItems();
                        // 防止为null
                        if (lists == null) {
                            return;
                        }
                        // 循环判断移除
                        for (int i = 0, len = lists.size(); i < len; i++) {
                            if (lists.get(i).getUri().equals(apkUri)) {
                                FileResItem fileResItem = lists.remove(i); // 删除并返回
                                listSearchs.remove(fileResItem); // 删除搜索的数据源
                                break;
                            }
                        }
                        // 发送通知
                        vHandler.sendEmptyMessage(Constants.Notify.H_QUERY_FILE_RES_END_NOTIFY);
                    } catch (Exception e) {
                        DevLogger.eTag(TAG, e, "Constants.Notify.H_DELETE_APK_FILE_NOTIFY");
                    }
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onQueryFileEvent(QueryFileEvent event) {
        DevLogger.dTag(TAG, "onQueryFileEvent");
        if (event != null) {
            int code = event.getCode();
            switch (code){
                case Constants.Notify.H_QUERY_FILE_RES_END_NOTIFY: // 搜索结束
                    Message msg = new Message();
                    msg.what = code;
                    // 防止数据为null
                    if (event.getData() != null) {
                        msg.arg1 = event.getData();
                    }
                    // 发送通知
                    vHandler.sendMessage(msg);
                    break;
                case Constants.Notify.H_QUERY_FILE_RES_ING_NOTIFY: // 搜索中
                    // 发送通知
                    vHandler.sendEmptyMessage(code);
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onSearchEvent(SearchEvent event) {
        DevLogger.dTag(TAG, "onSearchEvent");
        // 不属于搜索 APK 直接跳过
        if (MainActivity.getTypeEnum() != TypeEnum.QUERY_APK){
            return;
        }
        if (event != null) {
            int code = event.getCode();
            switch (code) {
                /** 搜索合并通知 */
                case Constants.Notify.H_SEARCH_COLLAPSE:
                    // 合并表示不属于搜索
                    isSearch = false;
                    // 发送通知
                    vHandler.sendEmptyMessage(Constants.Notify.H_QUERY_FILE_RES_END_NOTIFY);
                    break;
                /** 搜索展开通知 */
                case Constants.Notify.H_SEARCH_EXPAND:
                    // 展开表示属于搜索
                    isSearch = true;
                    // 删除旧的数据
                    listSearchs.clear();
                    break;
                /** 搜索输入内容通知 */
                case Constants.Notify.H_SEARCH_INPUT_CONTENT:
                    // 类型相同才处理
                    if (MainActivity.getTypeEnum() == TypeEnum.QUERY_APK) {
                        try {
                            // 删除旧的数据
                            listSearchs.clear();
                            // 进行筛选处理
                            filterApkList(QuerySDCardUtils.getInstance().getListFileResItems(), listSearchs, event.getData());
                        } catch (Exception e) {
                            DevLogger.eTag(TAG, e, "Constants.Notify.H_SEARCH_INPUT_CONTENT");
                        }
                        Message msg = new Message();
                        msg.what = Constants.Notify.H_QUERY_FILE_RES_END_NOTIFY;
                        msg.obj = event.getData();
                        // 发送通知
                        vHandler.sendMessage(msg);
                    }
                    break;
            }
        }
    }
}
