package t.app.info.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dev.utils.app.AppUtils;
import dev.utils.app.ClipboardUtils;
import dev.utils.app.toast.ToastTintUtils;
import dev.utils.common.DevCommonUtils;
import t.app.info.R;
import t.app.info.beans.DeviceInfoBean;
import t.app.info.beans.item.DeviceInfoItem;

/**
 * detail: 设备信息 Adapter
 * Created by Ttt
 */
public class DeviceInfoAdapter extends RecyclerView.Adapter<DeviceInfoAdapter.ViewHolder> {

    // 上下文
    private Activity mActivity;
    // 数据源
    private List<DeviceInfoItem> mList = new ArrayList<>();

    /**
     * 初始化适配器
     * @param mActivity
     */
    public DeviceInfoAdapter(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder = null;
        // 判断类型
        if (viewType == 0) {
            View itemView = LayoutInflater.from(mActivity).inflate(R.layout.item_device_info, null, false);
            viewHolder = new ViewHolder(itemView, viewType);
        }
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // 判断类型
        if (holder.iType == 0) {
            // 获取实体类
            DeviceInfoItem phoneInfoItem = mList.get(position);
            // 设置提示文案
            holder.idi_tips_tv.setText(AppUtils.getString(phoneInfoItem.getResId()));
            // 设置手机参数信息
            holder.idi_param_tv.setText(DevCommonUtils.toCheckValue(phoneInfoItem.getPhoneParams()));
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    // ==

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        int iType;
        // == View ==
        LinearLayout idi_linear;
        TextView idi_tips_tv;
        TextView idi_param_tv;

        public ViewHolder(View itemView, int iType) {
            super(itemView);
            this.iType = iType;
            // 判断类型
            if (iType == 0) {
                idi_linear = itemView.findViewById(R.id.idi_linear);
                idi_tips_tv = itemView.findViewById(R.id.idi_tips_tv);
                idi_param_tv = itemView.findViewById(R.id.idi_param_tv);
                // 设置点击事件
                idi_linear.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View view) {
            // 判断类型
            if (iType == 0) {
                // 获取当前索引
                int pos = getLayoutPosition();
                // 复制的内容
                String txt = DeviceInfoBean.obtain(mList.get(pos));
                // 复制到剪切板
                ClipboardUtils.copyText(txt);
                // 进行提示
                ToastTintUtils.success(AppUtils.getString(R.string.copy_suc) + " -> " + txt);
            }
        }
    }

    // ==

    /**
     * 设置数据源
     * @param lists
     */
    public void setData(List<DeviceInfoItem> lists) {
        this.mList.clear();
        if (lists != null) {
            this.mList.addAll(lists);
        }
        // 刷新适配器
        notifyDataSetChanged();
    }
}
