
package top.maxim.im.common.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import top.maxim.im.R;
import top.maxim.im.common.utils.ScreenUtils;

/**
 * Description : 设置页线条 Create by Mango on 2018/11/22
 */
public class ItemLine implements IBaseItemView {

    private View mView;

    private ItemLine(Context context, ViewGroup root) {
        mView = createView(context, root);
    }

    private View createView(Context context, ViewGroup root) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_line, root, false);
        return view;
    }

    @Override
    public View getView() {
        return mView;
    }

    public static class Builder {
        private ItemLine view;

        public Builder(Context context, ViewGroup root) {
            this.view = new ItemLine(context, root);
        }

        public Builder setLineColor(int color) {
            view.mView.findViewById(R.id.item_line).setBackgroundColor(color);
            return this;
        }

        public Builder setLineBackground(int color) {
            view.mView.setBackgroundColor(color);
            return this;
        }

        public Builder setMarginLeft(int left) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)view.mView
                    .findViewById(R.id.item_line).getLayoutParams();
            if (params == null) {
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ScreenUtils.dp2px(0.3f));
            }
            params.leftMargin = left;
            view.mView.findViewById(R.id.item_line).setLayoutParams(params);
            return this;
        }

        public Builder setMarginRight(int right) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)view.mView
                    .findViewById(R.id.item_line).getLayoutParams();
            if (params == null) {
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ScreenUtils.dp2px(0.3f));
            }
            params.rightMargin = right;
            view.mView.findViewById(R.id.item_line).setLayoutParams(params);
            return this;
        }

        public Builder setMarginBottom(int bottom) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)view.mView
                    .findViewById(R.id.item_line).getLayoutParams();
            if (params == null) {
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ScreenUtils.dp2px(0.3f));
            }
            params.bottomMargin = bottom;
            view.mView.findViewById(R.id.item_line).setLayoutParams(params);
            return this;
        }

        public View build() {
            return view.getView();
        }
    }
}
