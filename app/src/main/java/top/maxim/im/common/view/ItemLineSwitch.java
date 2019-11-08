
package top.maxim.im.common.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import top.maxim.im.R;
import top.maxim.im.common.utils.ScreenUtils;

/**
 * 可切换的view
 */
public class ItemLineSwitch implements IBaseItemView {

    private View mView;

    private TextView mLeftView;

    private CheckBox mRightSwitch;

    private RelativeLayout mContentView;

    private OnItemViewSwitchListener switchListener;

    private ItemLineSwitch(Context context) {
        mView = createView(context);
        initEvent();
    }

    private void initEvent() {
        mRightSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchListener != null) {
                    switchListener.onItemSwitch(v, mRightSwitch.isChecked());
                }
            }
        });
    }

    private View createView(Context context) {
        mView = LayoutInflater.from(context).inflate(R.layout.item_line_switch, null);
        mContentView = mView.findViewById(R.id.item_switch_content);
        mLeftView = mView.findViewById(R.id.item_switch_left_content);
        mRightSwitch = mView.findViewById(R.id.item_switch_check);
        return mView;
    }

    private void setItemSwitchListener(OnItemViewSwitchListener listener) {
        this.switchListener = listener;
    }

    @Override
    public View getView() {
        return mView;
    }

    public static class Builder {
        ItemLineSwitch lineSwitch;

        public Builder(Context context) {
            lineSwitch = new ItemLineSwitch(context);
        }

        public Builder setLeftText(String leftText) {
            lineSwitch.mLeftView.setText(leftText);
            return this;
        }

        public Builder setCheckStatus(boolean status) {
            lineSwitch.mRightSwitch.setChecked(status);
            return this;
        }

        public Builder setMarginTop(int top) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) lineSwitch.mContentView
                    .getLayoutParams();
            if (params == null) {
                params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        ScreenUtils.dp2px(50));
            }
            params.topMargin = top;
            lineSwitch.mContentView.setLayoutParams(params);
            return this;
        }

        public Builder setOnItemSwitchListener(OnItemViewSwitchListener listener) {
            lineSwitch.setItemSwitchListener(listener);
            return this;
        }

        public boolean getCheckStatus() {
            return lineSwitch.mRightSwitch.isChecked();
        }

        public View build() {
            return lineSwitch.getView();
        }
    }

    public interface OnItemViewSwitchListener {
        void onItemSwitch(View v, boolean curCheck);
    }
}
