
package top.maxim.im.login.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import im.floo.BMXCallBack;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import top.maxim.im.MainActivity;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.bean.UserBean;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.common.utils.CommonConfig;
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.utils.FileConfig;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.TaskDispatcher;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CustomDialog;
import top.maxim.im.common.view.Header;
import top.maxim.im.filebrowser.FileBrowserActivity;
import top.maxim.im.login.bean.DNSConfigEvent;
import top.maxim.im.net.ConnectivityReceiver;
import top.maxim.im.net.HttpResponseCallback;
import top.maxim.im.scan.config.ScanConfigs;
import top.maxim.im.scan.view.ScannerActivity;
import top.maxim.im.sdk.utils.MessageDispatcher;
import top.maxim.im.wxapi.WXUtils;

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

    private ImageView mIvAppIdHistory;

    /* 输入监听 */
    private TextWatcher mInputWatcher;

    public static void open(Context context) {
        Intent intent = new Intent(context, AppIdActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
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
        mIvAppIdHistory = view.findViewById(R.id.iv_app_id_history);
        TextView whatIs = view.findViewById(R.id.tv_what_is);
        whatIs.setMovementMethod(LinkMovementMethod.getInstance());
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
                LoginActivity.openLogin(AppIdActivity.this);
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
    }

    private void showAppIdHistory() {
        final CustomDialog dialog = new CustomDialog();
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Set<String> appIdHistory = SharePreferenceUtils.getInstance().getAppIdHistory();
        for (String appId: appIdHistory) {
            TextView tvAppId = new TextView(this);
            tvAppId.setPadding(ScreenUtils.dp2px(15), 0, ScreenUtils.dp2px(15), ScreenUtils.dp2px(8));
            tvAppId.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            tvAppId.setTextColor(getResources().getColor(R.color.color_black));
            tvAppId.setBackgroundColor((getResources().getColor(R.color.color_white)));
            tvAppId.setText(appId);
            tvAppId.setOnClickListener(v -> {
                mAppId.setText(appId);
                dialog.dismiss();
            });
            ll.addView(tvAppId, params);
        }

        dialog.setCustomView(ll);
        dialog.showDialog(this);
    }

}
