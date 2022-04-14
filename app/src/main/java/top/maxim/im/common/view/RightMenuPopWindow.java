
package top.maxim.im.common.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

import top.maxim.im.R;
import top.maxim.im.common.bean.RightMenuBean;
import top.maxim.im.common.utils.ScreenUtils;

;

/**
 * Description : 右键菜单弹出框
 */
public class RightMenuPopWindow extends PopupWindow implements AdapterView.OnItemClickListener {

    private static final String TAG = RightMenuPopWindow.class.getSimpleName();
    public static final int CONTEXT_MENU_WIDTH = 144;

    private final int CORNER_MIDDLE = 0, CORNER_TOP = 1, CORNER_BOTTOM = 2;

    private Context mContext;

    // 目标view
    private View mTargetView;

    private ListView listView;

    private LinearLayout container;

    private RightMenuAdapter mAdapter;

    private List<RightMenuBean> mDataList = null;

    private AnimationSet mShowAnim;

    private AnimationSet mCancelAnim;

    private OnMenuClickListener mOnMenuClickListener;

    private GradientDrawable mShapeDrawable;

    private Drawable mItemDrawable;

    private int mTextColor;

    private @ColorInt int mNormalColor;

    private @ColorInt int mPressColor;

    public RightMenuPopWindow(Context context, View targetView) {
        super(context);
        mContext = context;
        mTargetView = targetView;

        mShapeDrawable = new GradientDrawable();
        mShapeDrawable.setShape(GradientDrawable.RECTANGLE);
        mShapeDrawable.setColor(Color.parseColor("#414040"));
        mShapeDrawable.setCornerRadius(ScreenUtils.dp2px(5));

        mNormalColor = Color.parseColor("#414040");
        mPressColor = Color.parseColor("#414040");
        mItemDrawable = getCornerDrawable(CORNER_MIDDLE);

        mTextColor = context.getResources().getColor(R.color.color_white);

        init(context);
    }

    /**
     * 设置圆角按下效果
     *
     * @param cornerType
     * @return
     */
    public Drawable getCornerDrawable(int cornerType) {
        float[] radii;
        if (cornerType == CORNER_TOP) {
            radii = new float[] {
                    ScreenUtils.dp2px(4), ScreenUtils.dp2px(4), ScreenUtils.dp2px(4),
                    ScreenUtils.dp2px(4), 0, 0, 0, 0
            };
        } else if (cornerType == CORNER_BOTTOM) {
            radii = new float[] {
                    0, 0, 0, 0, ScreenUtils.dp2px(4), ScreenUtils.dp2px(4), ScreenUtils.dp2px(4),
                    ScreenUtils.dp2px(4)
            };
        } else {
            radii = new float[] {
                    0, 0, 0, 0, 0, 0, 0, 0
            };
        }
        StateListDrawable cornerDrawable = new StateListDrawable();

        GradientDrawable normal = new GradientDrawable();
        normal.setShape(GradientDrawable.RECTANGLE);
        normal.setColor(mNormalColor);
        normal.setCornerRadii(radii);

        GradientDrawable press = new GradientDrawable();
        press.setShape(GradientDrawable.RECTANGLE);
        press.setColor(mPressColor);
        press.setCornerRadii(radii);

        cornerDrawable.addState(new int[] {
                android.R.attr.state_selected
        }, press);
        cornerDrawable.addState(new int[] {
                android.R.attr.state_pressed
        }, press);
        cornerDrawable.addState(new int[] {
                android.R.attr.state_enabled
        }, normal);
        cornerDrawable.addState(new int[] {}, normal);

        return cornerDrawable;
    }

    /**
     * 设置背景颜色
     *
     * @param resId
     */
    public void setBackgroundColor(@ColorRes int resId) {
        int color = mContext.getResources().getColor(resId);
        mShapeDrawable = new GradientDrawable();
        mShapeDrawable.setShape(GradientDrawable.RECTANGLE);
        mShapeDrawable.setColor(color);
        mShapeDrawable.setCornerRadius(ScreenUtils.dp2px(5));

        if (mShapeDrawable != null) {
            listView.setBackground(mShapeDrawable);
        }
    }

    /**
     * 设置字体颜色
     *
     * @param textColor
     */
    public void setTextColor(@ColorRes int textColor) {
        mTextColor = mContext.getResources().getColor(textColor);
    }

    /**
     * 初始化显示view
     *
     * @param context 上下文
     */
    private void init(Context context) {
        View rootView = initView(context);
        this.setContentView(rootView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        // 备注：使popwindow全屏显示
        setClippingEnabled(false);
        // 刷新状态
        this.update();
        this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (RightMenuPopWindow.this.isShowing()) {
                    dismissMenu();
                }
                return false;
            }
        });
    }

    /**
     * 显示菜单
     */
    public void showAsDown(Context context, View targetView) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            // 系统7.0以上， popupwindow底层修改了Gravity属性 Gravity.START | Gravity.TOP
            int[] a = new int[2];
            targetView.getLocationInWindow(a);
            showAtLocation(((Activity)context).getWindow().getDecorView(), Gravity.NO_GRAVITY, 0,
                    a[1] + targetView.getHeight());
        } else {
            showAsDropDown(targetView);
        }
    }

    /**
     * 显示菜单
     */
    public void showMenu() {
        container.clearAnimation();
        container.setAnimation(mShowAnim);
        mShowAnim.startNow();

        showAtLocation(((Activity)mContext).getWindow().getDecorView(), Gravity.NO_GRAVITY, 0, 0);
    }

    /**
     * 取消菜单
     */
    private void dismissMenu() {
        container.clearAnimation();
        container.setAnimation(mCancelAnim);
        mCancelAnim.startNow();
    }

    /**
     * 初始化控件
     */
    private View initView(Context context) {
        View containerView = View.inflate(context, R.layout.contact_right_menu_pop, null);

        container = containerView.findViewById(R.id.layout_menu);
        listView = containerView.findViewById(R.id.lv_menu);
        if (mShapeDrawable != null) {
            listView.setBackground(mShapeDrawable);
        }
        if (mItemDrawable != null) {
            listView.setSelector(mItemDrawable);
        }

        LayoutParams lp = new LayoutParams(ScreenUtils.dp2px(CONTEXT_MENU_WIDTH), LayoutParams.WRAP_CONTENT);
        int[] a = new int[2];
        mTargetView.getLocationOnScreen(a);
        // 计算标题栏底部的屏幕坐标作为pop中的上边距值
        lp.topMargin = a[1] + mTargetView.getMeasuredHeight();
        container.setLayoutParams(lp);

        int[] point = new int[2];
        container.getLocationOnScreen(point);

        int durationTime = 300;
        // 动画
        mShowAnim = new AnimationSet(true);
        mCancelAnim = new AnimationSet(true);
        mShowAnim.addAnimation(new AlphaAnimation(0.0f, 1.0f));
        // mShowAnim.addAnimation(new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f,
        // Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f));
        mShowAnim.setDuration(durationTime);
        mShowAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                container.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                container.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mCancelAnim.addAnimation(new AlphaAnimation(1.0f, 0.0f));
        mCancelAnim.addAnimation(new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f));
        mCancelAnim.setDuration(durationTime);
        mCancelAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                container.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                container.setVisibility(View.GONE);
                dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        int x = (int)ev.getX();
                        int y = (int)ev.getY();
                        int itemnum = listView.pointToPosition(x, y);
                        if (itemnum == AdapterView.INVALID_POSITION) {
                            break;
                        } else {
                            if (itemnum == 0) {
                                // 第一项
                                listView.setSelector(getCornerDrawable(CORNER_TOP));
                            } else if (itemnum == (listView.getAdapter().getCount() - 1))
                                // 最后一项
                                listView.setSelector(getCornerDrawable(CORNER_BOTTOM));
                            else {
                                // 中间项
                                listView.setSelector(getCornerDrawable(CORNER_MIDDLE));
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        });

        return containerView;
    }

    /**
     * 初始化页面数据
     */
    public void setMenuData(List<RightMenuBean> list) {
        mDataList = list;
        if (mAdapter == null) {
            mAdapter = new RightMenuAdapter();
            listView.setAdapter(mAdapter);
            listView.setOnItemClickListener(this);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        this.dismiss();

        if (null == mDataList || mDataList.size() == 0) {
            Log.i(TAG, "onItemClick, menu data is empty, return...");
            return;
        }

        RightMenuBean bean = mDataList.get(position);

        if (null == bean) {
            Log.i(TAG, "onItemClick, click item bean is null, return...");
            return;
        }

        if (mOnMenuClickListener != null) {
            mOnMenuClickListener.onClick(bean);
        }
    }

    public void setOnMenuClickListener(OnMenuClickListener listener) {
        this.mOnMenuClickListener = listener;
    }

    public interface OnMenuClickListener {
        void onClick(RightMenuBean bean);
    }

    /**
     * 适配器
     */
    private class RightMenuAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return null == mDataList ? 0 : mDataList.size();
        }

        @Override
        public RightMenuBean getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (null == convertView) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_right_menu_view,
                        null);
                holder = new ViewHolder();
                holder.text = convertView.findViewById(R.id.tv_right_menu_title);
                holder.icon = convertView.findViewById(R.id.iv_right_menu_icon);
                holder.icon.changeShapeType(ShapeImageView.Shape.RECTANGLE);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            RightMenuBean bean = getItem(position);
            if (null != bean) {
                holder.text.setText(bean.getTitle());
                if (TextUtils.isEmpty(bean.getIcon())) {
                    holder.icon.setVisibility(View.GONE);
                } else {
                    if (holder.icon.getTag() == null) {// 防止延迟缓冲串图
                        holder.icon.setImageResource(mContext.getResources().getIdentifier(
                                bean.getIcon(), "drawable", mContext.getPackageName()));
                        holder.icon.setVisibility(View.VISIBLE);
                        holder.icon.setTag(bean.getIcon());
                    }
                }
            }

            holder.text.setTextColor(mTextColor);
            return convertView;
        }

        private class ViewHolder {
            TextView text;

            ShapeImageView icon;
        }
    }
}
