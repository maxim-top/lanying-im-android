
package top.maxim.im.common.base;

import androidx.annotation.DrawableRes;
import androidx.fragment.app.FragmentTransaction;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import top.maxim.im.R;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.view.Header;

/**
 * Description : 可切换页面activity Created by Mango on 2018/11/05.
 */
public abstract class BaseSwitchActivity extends BaseTitleActivity {

    public static final int CAMERA_DISTANCE = 6000;
    protected BaseFragment mCurrentFragment;

    protected int mCurrentIndex = -1;

    private SparseArray<BaseFragment> mFragmentCache;

    protected RelativeLayout mContainer;

    protected LinearLayout mTabLayout;

    private List<TabSwitchView> mTabSwitch;

    public boolean mUseCardFlipAnim; //使用卡片翻转动效

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        return new Header.Builder(this, headerContainer).build();
    }

    protected View getView(){
        return View.inflate(this, R.layout.activity_main, null);
    }

    @Override
    protected View onCreateView() {
        hideHeader();
        View view = getView();
        mContainer = (RelativeLayout)view.findViewById(R.id.container);
        mTabLayout = ((LinearLayout)view.findViewById(R.id.ll_switch_tab));
        mFragmentCache = new SparseArray();
        mTabSwitch = new ArrayList<>();
        initFragment(mTabSwitch);
        initTabView();
        return view;
    }

    @Override
    protected void setStatusBar() {
    }

    protected abstract void initFragment(List<TabSwitchView> mTabSwitch);

    protected void onTabClick() {
    }

    /**
     * 初始化底部切换view
     */
    protected void initTabView() {
        for (int i = 0; i < mTabSwitch.size(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            params.gravity = Gravity.CENTER;
            if (mTabLayout != null){
                mTabLayout.addView((mTabSwitch.get(i)).mSwitchView, params);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCurrentIndex < 0) {
            mCurrentIndex = 0;
        }
        switchFragment(mCurrentIndex);
    }

    public void setCurrentIndex(int index){
        mCurrentIndex = index;
    }

    /**
     * 切换fragment
     * 
     * @param index 索引
     */
    public void switchFragment(int index) {
        if (index < 0 || index > mFragmentCache.size()) {
            index = 0;
        }
        mCurrentIndex = index;
        BaseFragment newFragment = mFragmentCache.get(mCurrentIndex);
        int distance = ScreenUtils.dp2px(CAMERA_DISTANCE);
        if (newFragment != null){
            View newView = newFragment.getView();
            if (newView != null){
                newView.setCameraDistance(distance);
            }
        }
        if (mCurrentFragment != null){
            View currentView = mCurrentFragment.getView();
            if (currentView != null){
                currentView.setCameraDistance(distance);
            }
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mUseCardFlipAnim && mCurrentFragment != null && mCurrentFragment.isAdded()) {
            transaction.setCustomAnimations(R.anim.flip_in, R.anim.flip_out);
        }
        if (mCurrentFragment != null) {
            transaction.hide(mCurrentFragment);
        }
        mCurrentFragment = newFragment;
        if (mCurrentFragment == null) {
            return;
        }
        for (TabSwitchView tabSwitchView : mTabSwitch) {
            tabSwitchView.getTabImageView().setSelected(tabSwitchView.mIndex == index);
        }
        if (!mCurrentFragment.isAdded()) {
            transaction.add(R.id.fl_main, mCurrentFragment, mCurrentFragment.getClass().getName());
        } else {
            transaction.show(mCurrentFragment);
        }
        transaction.commit();
        mCurrentFragment.onShow();
    }

    protected BaseFragment getCurrentFrament() {
        return mCurrentFragment;
    }

    protected final class TabSwitchView implements View.OnClickListener {

        private int mDrawable;

        private int mResString;

        private int mIndex = -1;

        private ImageView mIvTab;

        private TextView mTvTab;

        private TextView mTvCount;

        private View mSwitchView;

        public TabSwitchView(@DrawableRes int drawable, int resString, BaseFragment baseFragment,
                int index) {
            if (baseFragment != null && index > -1) {
                mFragmentCache.put(index, baseFragment);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.fl_main, baseFragment, baseFragment.getClass().getName());
                transaction.hide(baseFragment);
                transaction.commit();
            }
            mDrawable = drawable;
            mResString = resString;
            mIndex = index;
            mSwitchView = View.inflate(AppContextUtils.getAppContext(), R.layout.switch_tab, null);
            mIvTab = mSwitchView.findViewById(R.id.iv_switch_tab);
            mTvTab = mSwitchView.findViewById(R.id.tv_switch_tab);
            mTvCount = mSwitchView.findViewById(R.id.tab_unread_num);
            mTvCount.setVisibility(View.GONE);
            if (mResString > -1){
                mTvTab.setText(getString(mResString));
            }
            if (mDrawable > -1){
                mIvTab.setImageResource(mDrawable);
            }
            mSwitchView.setOnClickListener(this);
        }

        public View getSwitchView() {
            return mSwitchView;
        }

        public ImageView getTabImageView() {
            return mIvTab;
        }

        public void setCount(int count) {
            if (count <= 0) {
                mTvCount.setVisibility(View.GONE);
            } else {
                mTvCount.setVisibility(View.VISIBLE);
                mTvCount.setText(String.valueOf(count));
            }
        }

        public void onClick(View paramView) {
            onTabClick();
            switchFragment(mIndex);
        }
    }
}
