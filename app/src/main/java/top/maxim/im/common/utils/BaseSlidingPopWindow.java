
package top.maxim.im.common.utils;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import top.maxim.im.R;

/**
 * Description : 向下滑动类型的Popwindow基类 Created by mango
 */
public abstract class BaseSlidingPopWindow extends PopupWindow
        implements PopupWindow.OnDismissListener {

    private static final long ANIM_DURATION = 300;

    private Context mContext;

    /**
     * PopupWindow布局父类
     */
    private FrameLayout mContainer;

    private TranslateAnimation mOpenAnimation;

    private TranslateAnimation mDismissAnim;

    private View mAnimView;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            dismiss();
            return false;
        }
    };

    public BaseSlidingPopWindow(Context context) {
        super(context);
        this.mContext = context;
        initContentView();
        setWidth(ActionBar.LayoutParams.MATCH_PARENT);
        setHeight(ActionBar.LayoutParams.MATCH_PARENT);
        setAnimationStyle(getCustomAnimationStyle());
        setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        setOnDismissListener(this);
    }

    private void initContentView() {
        ViewGroup.LayoutParams pp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer = new FrameLayout(mContext);
        mContainer.setLayoutParams(pp);
        mContainer.setOnTouchListener(mTouchListener);
        mContainer.setBackgroundDrawable(getBackgroundDrawable());

        mContainer.addView(getPopWindowContentView());
        setContentView(mContainer);
    }

    private int getCustomAnimationStyle() {
        return R.style.PopupAnimationAlpha;
    }

    protected Drawable getBackgroundDrawable() {
        return new ColorDrawable(Color.parseColor("#48000000"));
    }

    /**
     * 获取PopupWindow显示的内容
     */
    public abstract View getPopWindowContentView();

    @Override
    public void showAsDropDown(View anchor) {
        if (null == mAnimView && haveAnim()) {
            mAnimView = mContainer.getChildAt(0);
        }

        if (haveAnim()) {
            startOpenAnimation();
        }
        super.showAsDropDown(anchor);
    }

    private void startOpenAnimation() {
        if (null == mOpenAnimation) {
            mOpenAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, -1,
                    Animation.RELATIVE_TO_PARENT, 0);
            mOpenAnimation.setDuration(ANIM_DURATION);
            mOpenAnimation.setInterpolator(new DecelerateInterpolator());
        }
        mAnimView.startAnimation(mOpenAnimation);
    }

    private void startDismissAnim() {
        if (null == mDismissAnim) {
            mDismissAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, 0,
                    Animation.RELATIVE_TO_PARENT, -1);
            mDismissAnim.setDuration(ANIM_DURATION);
            mDismissAnim.setInterpolator(new AccelerateInterpolator());
            mDismissAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    BaseSlidingPopWindow.super.dismiss();
                }
            });
        }
        mAnimView.startAnimation(mDismissAnim);
    }

    @Override
    public void dismiss() {
        if (haveAnim()) {
            startDismissAnim();
        } else {
            super.dismiss();
        }
    }

    /**
     * dismiss回调
     */
    @Override
    public void onDismiss() {
        mContainer = null;
        mAnimView = null;
        mDismissAnim = null;
        mOpenAnimation = null;
    }

    public Context getContext() {
        return mContext;
    }

    public void release() {
        if (mAnimView != null) {
            mAnimView.clearAnimation();
        }
        mContext = null;
        dismiss();
    }

    protected boolean haveAnim() {
        return true;
    }
}
