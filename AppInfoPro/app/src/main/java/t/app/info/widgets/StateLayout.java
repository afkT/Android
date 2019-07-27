package t.app.info.widgets;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tt.whorlviewlibrary.WhorlView;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.utils.app.ViewUtils;
import t.app.info.R;

/**
 * detail: 状态布局
 * Created by Ttt
 * https://github.com/Kyson/WhorlView
 */
public class StateLayout extends FrameLayout {

    // === 其他属性 ==
    // 当前状态
    private State state;
    // === View ===
    // = 加载 =
    @BindView(R.id.isl_load_linear)
    LinearLayout isl_load_linear;
    @BindView(R.id.isl_load_view)
    WhorlView isl_load_view;
    // = 搜索结束 =
    @BindView(R.id.isl_query_end_linear)
    LinearLayout isl_query_end_linear;
    @BindView(R.id.isl_notfound_linear)
    LinearLayout isl_notfound_linear;
    // = 搜索结果 =
    @BindView(R.id.isl_search_nodata_linear)
    LinearLayout isl_search_nodata_linear;
    @BindView(R.id.isl_search_hint_tv)
    TextView isl_search_hint_tv;


    public StateLayout(Context context) {
        this(context, null);
    }

    public StateLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StateLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // == 初始化View ==
        View view = LayoutInflater.from(context).inflate(R.layout.inflate_state_layout, this);
        // 初始化View
        ButterKnife.bind(this, view);
        // 默认隐藏全部
        ViewUtils.setVisibilitys(false, this);
    }

    // === 状态 ===

    /** 查询状态 */
    public enum State {

        /** 查询/刷新中 */
        REFRESH,

        /** 查询结束 */
        QUERY_END,

        /** 搜索无数据 */
        SEARCH_NO_DATA,

//        /** 搜索无权限 */
//        SEARCH_READ_AUTHOR
    }

    /**
     * 设置View 状态
     * @param state
     */
    public void setState(State state) {
        setState(state, -1);
    }

    /**
     * 设置View 状态
     * @param state
     * @param size 数据总数
     */
    public void setState(State state, int size) {
        // 停止动画
        isl_load_view.stop();
        // 判断类型
        switch (state) {
            case REFRESH: // 刷新中
                // 隐藏全部View
                ViewUtils.setVisibilitys(false, getChildViews());
                // 单独显示查询加载页面
                ViewUtils.setVisibilitys(true, this, isl_load_linear);
                // 开始加载
                isl_load_view.start();
                break;
            case QUERY_END: // 表示加载结束
                // 隐藏当前全部View
                ViewUtils.setVisibilitys(false, getChildViews());
                // 判断是否存在数据
                if (size <= 0) {
                    ViewUtils.setVisibilitys(true, isl_query_end_linear, isl_notfound_linear);
                }
                break;
            case SEARCH_NO_DATA: // 搜索没数据
                // 隐藏全部View
                ViewUtils.setVisibilitys(false, getChildViews());
                // 单独无数据提示页面
                ViewUtils.setVisibilitys(true, this, isl_search_nodata_linear);
                break;
        }
    }

    /**
     * 设置搜索没数据提示
     * @param searchContent 搜索内容
     */
    public void setStateToSearchNoData(String searchContent) {
        // 搜索无数据
        setState(State.SEARCH_NO_DATA);
        // 格式化显示内容
        String tips = "<font color=\"#359AFF\">" + searchContent + "</font>";
        // 进行显示提示
        isl_search_hint_tv.setText(Html.fromHtml(getContext().getString(R.string.search_noresult_tips, tips)));
    }

    /**
     * 获取全部View
     * @return
     */
    private View[] getChildViews() {
        FrameLayout frameLayout = (FrameLayout) getChildAt(0);
        // 获取全部View
        View[] views = new View[frameLayout.getChildCount() + 1];
        // 保存View
        for (int i = 0, len = views.length; i < len; i++) {
            views[i] = frameLayout.getChildAt(i);
        }
        // 返回全部子View
        return views;
    }

    /**
     * 获取状态
     * @return
     */
    public State getState() {
        return state;
    }
}
