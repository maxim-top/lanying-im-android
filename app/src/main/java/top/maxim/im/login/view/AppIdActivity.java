
package top.maxim.im.login.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Set;

import top.maxim.im.LoginRegisterActivity;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.dialog.DropDownMenu;
import top.maxim.im.common.view.Header;
import top.maxim.im.scan.view.ScannerActivity;

/**
 * Description : App ID 输入.
 */
public class AppIdActivity extends BaseTitleActivity {

    /* App ID */
    private EditText mAppId;

    /* 继续 */
    private TextView mContinue;

    /* 扫一扫 */
    private ImageView mIvScan;

    /* what is app id */
    private ImageView mIvHelp;

    private ImageView mIvAppIdHistory;

    private ImageView mIvClear;

    private View mVUnderline;

    /* 输入监听 */
    private TextWatcher mInputWatcher;

    public static void open(Context context) {
        Intent intent = new Intent(context, AppIdActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(systemUiVisibility | View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        return new Header.Builder(this, headerContainer).build();
    }

    @Override
    protected View onCreateView() {
        hideHeader();
        View view = View.inflate(this, R.layout.activity_appid, null);
        mAppId = view.findViewById(R.id.et_app_id);
        mContinue = view.findViewById(R.id.tv_continue);
        mIvScan = view.findViewById(R.id.iv_scan);
        mIvHelp = view.findViewById(R.id.iv_help);
        mIvClear = view.findViewById(R.id.iv_clear);
        mIvAppIdHistory = view.findViewById(R.id.iv_app_id_history);
        mVUnderline = view.findViewById(R.id.v_underline);
        return view;
    }

    private void updateLoginButton() {
        boolean hasAppId = TextUtils.isEmpty(mAppId.getText().toString().trim());
        if (!hasAppId) {
            mContinue.setEnabled(true);
        } else {
            mContinue.setEnabled(false);
        }
    }

    @Override
    protected void setViewListener() {
        // 登陆
        mContinue.setOnClickListener(v -> {
            String appId = mAppId.getText().toString().trim();
            if (!TextUtils.isEmpty(appId)){
                SharePreferenceUtils.getInstance().putAppId(appId);
                UserManager.getInstance().changeAppId(appId, bmxErrorCode -> {});
                LoginRegisterActivity.openLoginRegister(AppIdActivity.this, false);
            }
        });

        // 扫一扫
        mIvScan.setOnClickListener(v -> ScannerActivity.openScan(this));
        mInputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mIvClear.setVisibility(s.length()>0?View.VISIBLE:View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateLoginButton();
            }
        };
        mAppId.addTextChangedListener(mInputWatcher);
        mIvAppIdHistory.setOnClickListener(v -> {
            showAppIdHistory();
        });
        mIvHelp.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(AppIdActivity.this);
            LayoutInflater inflater = LayoutInflater.from(AppIdActivity.this);
            View view = inflater.inflate(R.layout.activity_what_is_app_id, null);
            WebView mWebView = view.findViewById(R.id.webview);
            final Dialog dlg = builder.create();
            dlg.show();

            WindowManager.LayoutParams params = dlg.getWindow().getAttributes();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ScreenUtils.dp2px(500);
            dlg.getWindow().setAttributes(params);
            dlg.getWindow().setContentView(view);
            mWebView.setWebViewClient(new WebViewClient() {

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    dismissLoadingDialog();
                }
            });
            showLoadingDialog(true);
            mWebView.loadUrl(getString(R.string.what_is_app_id_url));
        });
        mIvClear.setOnClickListener(v -> {
            mAppId.setText("");
        });
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void showAppIdHistory() {
        final DropDownMenu dialog = new DropDownMenu();
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Set<String> appIdHistory = SharePreferenceUtils.getInstance().getAppIdHistory();
        for (String appId: appIdHistory) {
            TextView tvAppId = new TextView(this);
            tvAppId.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(6), ScreenUtils.dp2px(15), ScreenUtils.dp2px(8));
            tvAppId.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            tvAppId.setTextColor(getResources().getColor(R.color.color_black));
            tvAppId.setText(appId);
            tvAppId.setOnClickListener(v -> {
                mAppId.setText(appId);
                dialog.dismiss();
            });
            ll.addView(tvAppId, params);
        }
        TextView tvClear = new TextView(this);
        tvClear.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(6), ScreenUtils.dp2px(15), ScreenUtils.dp2px(8));
        tvClear.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        tvClear.setTextColor(getResources().getColor(R.color.color_0079F4));
        tvClear.setText("清除记录");
        tvClear.setOnClickListener(v -> {
            SharePreferenceUtils.getInstance().clearAppIdHistory();
            dialog.dismiss();
        });
        ll.addView(tvClear, params);


        dialog.setCustomView(ll);

        int[] locationOfUnderline = {-1, -1};
        mVUnderline.getLocationInWindow(locationOfUnderline);
        int left = locationOfUnderline[0];
        int top = locationOfUnderline[1]-getStatusBarHeight(this);
        int width = mVUnderline.getWidth();
        dialog.setViewPosition(new Point(left,top), width, -1);
        dialog.showDialog(this);
    }

}
