
package top.maxim.im.common.base;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;

import top.maxim.im.R;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.LoadingDialog;

/**
 * Description : 基础带标题的fragment Created by Mango on 2018/11/05.
 */
public abstract class BaseTitleFragment extends BaseFragment {

    private LinearLayout mContainer;

    protected View mContentView;

    protected Header mHeader;

    private View mHeaderDiver;

    protected View mStatusBar;

    private LoadingDialog mLoadingDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.base_activity, container, false);
        mContainer = view.findViewById(R.id.container);
        mHeaderDiver = view.findViewById(R.id.header_diver);
        RelativeLayout headerContainer = view.findViewById(R.id.container_header);
        mStatusBar = view.findViewById(R.id.container_status_bar);
        mHeader = onCreateHeader(headerContainer);
        addView();
        if (isFullScreen() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setStatusBar();
        }
        return view;
    }

    /**
     * 添加页面view
     */
    private void addView() {
        mContentView = onCreateView();
        if (mContentView != null) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0);
            params.weight = 1;
            mContainer.addView(mContentView, params);
        }
        setViewListener();
        initDataForActivity();
    }

    protected void setViewListener() {
    }

    protected void initDataForActivity() {
    }

    @Override
    public void onShow() {
        super.onShow();
        setStatusBar();
    }

    /**
     * 设置沉浸式状态栏
     */
    protected void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity() == null) {
                return;
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtils.getStatusBarHeight());
            mStatusBar.setLayoutParams(params);
            mStatusBar.setVisibility(View.VISIBLE);
            int color = getResources().getColor(R.color.color_background);
            mStatusBar.setBackgroundColor(getResources().getColor(R.color.color_background));
            Window window = getActivity().getWindow();
            int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            window.setStatusBarColor(color);
            if (isLightColor(color)) {
                window.getDecorView().setSystemUiVisibility(
                        systemUiVisibility | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                window.getDecorView()
                        .setSystemUiVisibility(systemUiVisibility | View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }
    }

    /**
     * 判断颜色是不是亮色
     *
     * @param color
     * @return
     */
    protected boolean isLightColor(@ColorInt int color) {
        return ColorUtils.calculateLuminance(color) >= 0.5;
    }

    /**
     * 是否启用沉浸式 默认true
     *
     * @return boolean
     */
    protected boolean isFullScreen() {
        return true;
    }

    public void hideHeader() {
        hideTitleHeader();
        mStatusBar.setVisibility(View.GONE);
    }

    public void hideTitleHeader() {
        if (mHeader != null) {
            mHeader.hideHeader();
        }
        mHeaderDiver.setVisibility(View.GONE);
    }

    protected abstract Header onCreateHeader(RelativeLayout headerContainer);

    protected abstract View onCreateView();

    /**
     * 展示加载框
     * 
     * @param cancelable 是否可取消
     */
    public void showLoadingDialog(boolean cancelable) {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(getActivity());
        } else if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
        mLoadingDialog.setCancelable(cancelable);
        mLoadingDialog.show();
    }

    /**
     * 取消加载框
     */
    public void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    protected void setNull(Object object) {
        if (object != null) {
            object = null;
        }
    }

    protected void setNull(List<Object> objects) {
        if (objects != null) {
            objects.clear();
            objects = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
