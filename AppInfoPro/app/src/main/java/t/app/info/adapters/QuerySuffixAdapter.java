package t.app.info.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import dev.utils.common.DevCommonUtils;
import t.app.info.R;
import t.app.info.dialogs.QuerySuffixEditDialog;
import t.app.info.utils.QuerySuffixUtils;

/**
 * detail: 搜索后缀 Adapter
 * Created by Ttt
 */
public class QuerySuffixAdapter extends RecyclerView.Adapter<QuerySuffixAdapter.ViewHolder> {

    // 上下文
    private Context mContext;
    // 数据源
    private List<String> mList = new ArrayList<>();
    // 获取搜索配置
    private LinkedHashMap<String, String> mQuerySuffixMaps = new LinkedHashMap<>();

    /**
     * 初始化适配器
     * @param mContext
     */
    public QuerySuffixAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_query_suffix, null, false);
        return new ViewHolder(itemView, viewType);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // 获取实体类
        String content = mList.get(position);
        // 判断类型
        if (holder.iType == 0) {
            // 设置提示文案
            holder.iqs_suffix_tv.setText(DevCommonUtils.toCheckValue(content));
        }
    }

    @Override
    public int getItemCount() {
        return DevCommonUtils.length(mList);
    }

    @Override
    public int getItemViewType(int position) {
        // 获取Item
        final String suffix = mList.get(position);
        // 判断是否为null
        if (suffix == null) {
            return 1;
        }
        return 0;
    }

    // ==

    public class ViewHolder extends RecyclerView.ViewHolder {

        int iType;
        // ======= View =======
        // === 默认显示的 ===
        TextView iqs_suffix_tv;
        FrameLayout iqs_framelayout;
        ImageView iqs_igview;

        public ViewHolder(View itemView, int iType) {
            super(itemView);
            this.iType = iType;
            // 初始化View
            iqs_suffix_tv = itemView.findViewById(R.id.iqs_suffix_tv);
            iqs_framelayout = itemView.findViewById(R.id.iqs_framelayout);
            iqs_igview = itemView.findViewById(R.id.iqs_igview);
            // 判断类型
            if (iType == 0) {
                iqs_igview.setImageResource(R.drawable.ic_close);
                // 设置点击事件
                iqs_framelayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 删除配置
                        mQuerySuffixMaps.remove(mList.get(getLayoutPosition()));
                        // 刷新配置
                        QuerySuffixUtils.refConfig(mQuerySuffixMaps);
                        // 刷新数据源
                        refData();
                    }
                });
            } else if (iType == 1) { // 表示添加
                iqs_igview.setImageResource(R.drawable.ic_add);
                // 设置点击事件
                iqs_framelayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 显示添加Dialog
                        new QuerySuffixEditDialog(mContext, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 刷新数据源
                                refData();
                            }
                        }).showDialog();
                    }
                });
            }
        }
    }

    // ==

    /**
     * 刷新数据源
     */
    public void refData() {
        setData(getDatas());
    }

    /**
     * 设置数据源
     * @param lists
     */
    private void setData(ArrayList<String> lists) {
        this.mList.clear();
        if (lists != null) {
            this.mList.addAll(lists);
        }
        // 刷新适配器
        notifyDataSetChanged();
    }

    /**
     * 获取数据源
     * @return
     */
    private ArrayList<String> getDatas() {
        // 保存数据
        mQuerySuffixMaps = QuerySuffixUtils.getQuerySuffixMap();
        // 默认List 集合
        ArrayList<String> listDatas = new ArrayList<>();
        // 获取并且循环数据源
        Set<String> set = mQuerySuffixMaps.keySet();
        for (String key : set) {
            listDatas.add(key);
        }
        // 便于最后判断添加
        listDatas.add(null);
        // 返回数据源
        return listDatas;
    }
}
