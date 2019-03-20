
package top.maxim.im.message.customviews.panel;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import top.maxim.im.common.utils.ScreenUtils;

/**
 * Description : 输入板操作 Created by Mango on 2018/11/06.
 */
public class PanelContainer extends FrameLayout {

    private IPanelFactory mFactory;

    private int mHeight = ScreenUtils.dp2px(200);

    public PanelContainer(@NonNull Context context) {
        this(context, null);
    }

    public PanelContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PanelContainer(@NonNull Context context, @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutParams(
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, mHeight));
        setVisibility(View.GONE);
        mFactory = new PanelFactoryImp((Activity)getContext());
    }

    private void showPanel() {
        if (!isShown()) {
            setVisibility(VISIBLE);
        }
    }

    public void hidePanel() {
        if (isShown()) {
            setVisibility(GONE);
        }
    }

    public void showPanel(int type, OnPanelItemListener itemListener) {
        removeAllViews();
        IPanel panel = mFactory.obtainPanel(type);
        if (panel != null) {
            addView(panel.obtainView(itemListener));
        }
        showPanel();
    }
}
