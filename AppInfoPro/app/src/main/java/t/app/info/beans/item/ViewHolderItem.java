package t.app.info.beans.item;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import t.app.info.R;

/**
 * detail: 便捷通用View holder
 * Created by Ttt
 */
public class ViewHolderItem {

    View itemView;
    LinearLayout linear;
    TextView key_tv;
    TextView value_tv;

    public ViewHolderItem(LayoutInflater inflater) {
        itemView = inflater.inflate(R.layout.item_app_details, null, false);
        // 初始化View
        linear = itemView.findViewById(R.id.iad_linear);
        key_tv = itemView.findViewById(R.id.iad_key_tv);
        value_tv = itemView.findViewById(R.id.iad_value_tv);
    }

    public View getItemView() {
        return itemView;
    }

    public LinearLayout getLinear() {
        return linear;
    }

    public TextView getKey_tv() {
        return key_tv;
    }

    public TextView getValue_tv() {
        return value_tv;
    }

    // = 初始化数据 =

    public ViewHolderItem setData(String key, String value, View.OnClickListener onClickListener) {
        key_tv.setText(key);
        value_tv.setText(value);
        linear.setOnClickListener(onClickListener);
        return this;
    }

    public ViewHolderItem setData(int key, int value, View.OnClickListener onClickListener) {
        key_tv.setText(key);
        value_tv.setText(value);
        linear.setOnClickListener(onClickListener);
        return this;
    }
}
