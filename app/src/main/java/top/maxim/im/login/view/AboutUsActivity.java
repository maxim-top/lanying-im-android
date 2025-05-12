
package top.maxim.im.login.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.util.TypedValue;

import com.google.common.collect.ImmutableMap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import im.floo.floolib.BMXClient;
import top.maxim.im.BuildConfig;
import top.maxim.im.LoginRegisterActivity;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.CommonConfig;
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.TelLinkPopupWindow;
import top.maxim.im.common.view.Header;

/**
 * Description : 关于我们 Created by Mango on 2018/11/06
 */
public class AboutUsActivity extends BaseTitleActivity {

    private TextView mAppVersion;

    private TextView mTvNet;

    private TextView mThisApp;

    private TextView mCompanyName;

    private TextView mVerificationStatus;

    private TextView mTvGo;

    private String mUrl;

    private boolean mShowGo = false;

    /* 链接 电话弹出框 */
    private TelLinkPopupWindow mTelLinkPopupWindow;

    public static void startAboutUsActivity(Context context, boolean showGoButton) {
        Intent intent = new Intent(context, AboutUsActivity.class);
        intent.putExtra("showGo", showGoButton);
        context.startActivity(intent);
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mShowGo = intent.getBooleanExtra("showGo", false);
        }
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.about_us);
        builder.setBackIcon(R.drawable.header_back_icon, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.activity_about_us, null);
        mAppVersion = view.findViewById(R.id.tv_app_version);
        mTvNet = view.findViewById(R.id.tv_about_us_net);
        mThisApp = view.findViewById(R.id.tv_about_us_this_app);
        mCompanyName = view.findViewById(R.id.tv_about_us_company_name);
        mVerificationStatus = view.findViewById(R.id.tv_about_us_verification_status);
        mTvGo = view.findViewById(R.id.tv_go);
        TextView tvCopyRight = view.findViewById(R.id.copyright);
        String ret = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
        tvCopyRight.setText(String.format(getString(R.string.about_us_company), ret));
        mTvNet.setText(mUrl = getString(R.string.about_us_net));
        String appId = SharePreferenceUtils.getInstance().getAppId();
        mThisApp.setText(String.format(getString(R.string.about_us_this_app), appId));
        String companyName = CommonUtils.getCompanyName(this);
        String verificationStatus = CommonUtils.getVerificationStatusText(this);
        mCompanyName.setText(companyName);
        mVerificationStatus.setText(verificationStatus);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String companyName = CommonUtils.getCompanyName(AboutUsActivity.this);
                        String verificationStatus = CommonUtils.getVerificationStatusChar(AboutUsActivity.this);
                        mCompanyName.setText(companyName);
                        mVerificationStatus.setText(verificationStatus);
                    }
                });
            }
        }, 100);
        mTvGo.setOnClickListener(v -> {
            finish();
        });
        if (!mShowGo){
            mTvGo.setVisibility(View.GONE);
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String companyName = CommonUtils.getCompanyName(AboutUsActivity.this);
                        mCompanyName.setText(companyName);
                    }
                });
            }
        }, 1000);
        return view;
    }

    @Override
    protected void setViewListener() {
        // 跳转浏览器
        if (!mUrl.startsWith("https://")){
            mUrl = String.format("https://%s",mUrl);
        }
        mTvNet.setOnClickListener(
                v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl))));
    }

    @Override
    protected void initDataForActivity() {
        // 获取app版本
        String versionName = BuildConfig.VERSION_NAME;
        BMXClient client = BaseManager.getBMXClient();
        String sdkVersion = "";
        if (client != null && client.getSDKConfig() != null
                && !TextUtils.isEmpty(client.getSDKConfig().getSDKVersion())) {
            sdkVersion = client.getSDKConfig().getSDKVersion();
        }
        mAppVersion.setText("Lanying IM " + versionName + " SDK " + sdkVersion);
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent();
        intent.setAction(CommonConfig.CHANGE_APP_ID_ACTION);
        intent.putExtra(CommonConfig.CHANGE_APP_ID, SharePreferenceUtils.getInstance().getAppId());
        RxBus.getInstance().send(intent);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mTelLinkPopupWindow != null) {
            mTelLinkPopupWindow.dismiss();
            return;
        }
        super.onBackPressed();
    }
}
