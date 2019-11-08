
package top.maxim.im.message.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Description : 聊天页面父布局 Created by Mango on 2018/11/06.
 */
public class ChatViewGroup extends FrameLayout {

    private InterceptTouchListener mListener;

    public ChatViewGroup(Context context) {
        this(context, null);
    }

    public ChatViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mListener != null && mListener.setInterceptTouchListener(ev)) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setInterceptTouchListener(InterceptTouchListener listener) {
        mListener = listener;
    }

    public interface InterceptTouchListener {
        boolean setInterceptTouchListener(MotionEvent ev);
    }
}
