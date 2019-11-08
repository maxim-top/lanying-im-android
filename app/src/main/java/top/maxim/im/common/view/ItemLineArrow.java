
package top.maxim.im.common.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import top.maxim.im.R;
import top.maxim.im.common.utils.ScreenUtils;

/**
 * 文本item
 */
public class ItemLineArrow implements IBaseItemView {

    private View mView;

    private TextView mTextView;
    
    private TextView mTextEndView;

    private ImageView mRightArrow;

    private RelativeLayout mContentView;

    private OnItemArrowViewClickListener onItemViewClickListener;

    private ItemLineArrow(Context context) {
        mView = createView(context);
        initEvent();
    }

    private void initEvent() {
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemViewClickListener != null) {
                    onItemViewClickListener.onItemClick(v);
                }
            }
        });
    }

    private View createView(Context context) {
        mView = LayoutInflater.from(context).inflate(R.layout.item_line_arrow, null);
        mContentView = mView.findViewById(R.id.item_arrow_content);
        mTextView = mView.findViewById(R.id.item_text);
        mTextEndView = mView.findViewById(R.id.item_end_text);
        mRightArrow = mView.findViewById(R.id.item_right_arrow);
        return mView;
    }

    private void setItemClickListener(OnItemArrowViewClickListener clickListener) {
        this.onItemViewClickListener = clickListener;
    }

    @Override
    public View getView() {
        return mView;
    }

    public static class Builder {
        ItemLineArrow arrow;

        public Builder(Context context) {
            arrow = new ItemLineArrow(context);
        }

        public Builder setStartContent(String startContent) {
            arrow.mTextView.setText(startContent);
            return this;
        }

        public Builder setEndContent(String endContent) {
            arrow.mTextEndView.setText(endContent);
            return this;
        }

        public Builder setBackgroudColor(int color) {
            arrow.mContentView.setBackgroundColor(color);
            return this;
        }

        public Builder setArrowVisible(boolean show) {
            arrow.mRightArrow.setVisibility(show ? View.VISIBLE : View.GONE);
            arrow.mTextEndView.setVisibility(show ? View.GONE : View.VISIBLE);
            return this;
        }

        public Builder setMarginTop(int top) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)arrow.mContentView
                    .getLayoutParams();
            if (params == null) {
                params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        ScreenUtils.dp2px(50));
            }
            params.topMargin = top;
            arrow.mContentView.setLayoutParams(params);
            return this;
        }

        public Builder setOnItemClickListener(OnItemArrowViewClickListener listener) {
            arrow.setItemClickListener(listener);
            return this;
        }

        public View build() {
            return arrow.getView();
        }
    }

    public interface OnItemArrowViewClickListener {
        void onItemClick(View v);
    }
}
