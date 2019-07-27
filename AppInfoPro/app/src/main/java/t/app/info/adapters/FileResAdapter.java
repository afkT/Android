package t.app.info.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dev.utils.app.AppUtils;
import dev.utils.app.info.AppInfoBean;
import dev.utils.app.toast.ToastTintUtils;
import dev.utils.common.DevCommonUtils;
import dev.utils.common.FileUtils;
import t.app.info.R;
import t.app.info.activitys.ApkDetailsActivity;
import t.app.info.base.config.Constants;
import t.app.info.beans.item.FileResItem;

/**
 * detail:文件资源 Adapter
 * Created by Ttt
 */
public class FileResAdapter extends RecyclerView.Adapter<FileResAdapter.ViewHolder> {

    // 上下文
    private Activity mActivity;
    // 数据源
    private List<FileResItem> mList = new ArrayList<>();

    /**
     * 初始化适配器
     * @param mActivity
     */
    public FileResAdapter(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder = null;
        // 判断类型
        if (viewType == 0) {
            View itemView = LayoutInflater.from(mActivity).inflate(R.layout.item_app_info, null, false);
            viewHolder = new ViewHolder(itemView, viewType);
        }
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // 判断类型
        if (holder.iType == 0) {
            // 获取实体类
            FileResItem fileResItem = mList.get(position);
            // 获取app 信息
            AppInfoBean appInfoBean = fileResItem.getAppInfoBean();
            // 设置 app 名
            holder.iai_name_tv.setText(DevCommonUtils.toCheckValue(appInfoBean.getAppName()));
            // 设置 app 包名
            holder.iai_pack_tv.setText(DevCommonUtils.toCheckValue(appInfoBean.getAppPackName()));
            // 设置 app 图标
            holder.iai_igview.setImageDrawable(appInfoBean.getAppIcon());
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
        RelativeLayout iai_rela;
        ImageView iai_igview;
        TextView iai_name_tv;
        TextView iai_pack_tv;

        public ViewHolder(View itemView, int iType) {
            super(itemView);
            this.iType = iType;
            // 判断类型
            if (iType == 0) {
                iai_rela = itemView.findViewById(R.id.iai_rela);
                iai_igview = itemView.findViewById(R.id.iai_igview);
                iai_name_tv = itemView.findViewById(R.id.iai_name_tv);
                iai_pack_tv = itemView.findViewById(R.id.iai_pack_tv);
                // 设置点击事件
                iai_rela.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View view) {
            // 判断类型
            if (iType == 0) {
                // 获取Item
                FileResItem fileResItem = mList.get(getLayoutPosition());
                // 文件存在处理
                if (FileUtils.isFileExists(fileResItem.getUri())) {
                    // 进行跳转
                    Intent intent = new Intent(mActivity, ApkDetailsActivity.class);
                    intent.putExtra(Constants.Key.KEY_APK_URI, fileResItem.getUri());
                    mActivity.startActivityForResult(intent, Constants.RequestCode.FOR_R_APP_DETAILS);
                } else {
                    ToastTintUtils.warning(AppUtils.getString(R.string.file_not_exist));
                }
            }
        }
    }

    // ==

    /**
     * 设置数据源
     * @param lists
     */
    public void setData(List<FileResItem> lists) {
        this.mList.clear();
        if (lists != null) {
            this.mList.addAll(lists);
        }
        // 刷新适配器
        notifyDataSetChanged();
    }

    /**
     * 清空数据
     */
    public void clearData() {
        this.mList.clear();
        // 刷新适配器
        notifyDataSetChanged();
    }
}
