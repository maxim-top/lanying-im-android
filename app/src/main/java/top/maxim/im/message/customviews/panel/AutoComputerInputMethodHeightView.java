
package top.maxim.im.message.customviews.panel;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import top.maxim.im.common.utils.SharePreferenceUtils;

/**
 * Description : 聊天页面软键盘 Created by Mango on 2018/11/06.
 */
public class AutoComputerInputMethodHeightView extends LinearLayout {

    private String mCurrentInputMethod;

    private InputMethodManager mInputManager;

    private int mInputMethodHeight;

    public AutoComputerInputMethodHeightView(Context context) {
        this(context, null);
    }

    public AutoComputerInputMethodHeightView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoComputerInputMethodHeightView(Context context, @Nullable AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSoftInputMethod();
        mCurrentInputMethod = Settings.Secure.getString(getContext().getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);
        mInputMethodHeight = SharePreferenceUtils.getInstance()
                .getKeyboardHeight(mCurrentInputMethod);
        // 如果没有操作过输入板，会执行,主要是确定输入法高度,note:挡手动调整输入板高度时，无法更新输入法高度
        if (mInputMethodHeight == 0) {
            ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
                // 记录原始MessageInputPanelControlBar位置坐标，其内部view改变不会造成该坐标改变（已知虚拟按键和输入法会改变）
                private int mRootBottom = 0;

                public void onGlobalLayout() {
                    Rect r = new Rect();
                    getGlobalVisibleRect(r);
                    // 第一次调用onGlobalLayout，先记录开始软键盘没有弹出时底部的位置
                    if (mRootBottom == 0) {
                        mRootBottom = r.bottom;
                        return;
                    }
                    // 记录位置改变大小
                    int keyboardHeight = mRootBottom - r.bottom;
                    // 如果高度变化超过1/5则意味着弹出了输入法，此时取消监听，把键盘高度记录在本地
                    if (keyboardHeight > 0 && Math.abs(keyboardHeight) > mRootBottom / 5) {
                        mInputMethodHeight = keyboardHeight;
                        SharePreferenceUtils.getInstance().putKeyboardHeight(mCurrentInputMethod,
                                mInputMethodHeight);
                    }
                }
            };
            getViewTreeObserver().addOnGlobalLayoutListener(listener);
        }
    }

    private void initSoftInputMethod() {
        ((Activity)getContext()).getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mInputManager = ((InputMethodManager)getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE));
    }

    protected void dismissKeyBoard() {
        ((Activity)getContext()).getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        mInputManager.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    protected void showKeyBoard(View anchor) {
        if (anchor.isShown()) {
            anchor.setVisibility(GONE);
        }
        int mode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        ((Activity)getContext()).getWindow().setSoftInputMode(mode);
        mInputManager.showSoftInputFromInputMethod(getWindowToken(), 0);
    }
}
