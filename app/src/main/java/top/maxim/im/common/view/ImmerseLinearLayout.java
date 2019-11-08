
package top.maxim.im.common.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.LinearLayout;

/**
 * Description : 沉浸式
 * Created by Mango on 2018/11/05.
 */
public class ImmerseLinearLayout extends LinearLayout {
    public ImmerseLinearLayout(Context context) {
        this(context, null, 0);
    }

    public ImmerseLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImmerseLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        return Build.VERSION.SDK_INT >= 21 ? super.onApplyWindowInsets(
                insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), 0,
                        insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom()))
                : super.onApplyWindowInsets(insets);
    }
}
