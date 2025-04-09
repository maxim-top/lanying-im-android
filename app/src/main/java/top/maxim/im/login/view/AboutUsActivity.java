
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.floo.floolib.BMXClient;
import top.maxim.im.BuildConfig;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.TelLinkPopupWindow;
import top.maxim.im.common.view.Header;

/**
 * Description : 关于我们 Created by Mango on 2018/11/06
 */
public class AboutUsActivity extends BaseTitleActivity {

    private TextView mAppVersion;

    private TextView mFlooVersion;

    private TextView mTvNet;

    private TextView mTvPhone;

    private String mUrl;

    private String mPhone;

    /* 链接 电话弹出框 */
    private TelLinkPopupWindow mTelLinkPopupWindow;

    public static void startAboutUsActivity(Context context) {
        Intent intent = new Intent(context, AboutUsActivity.class);
        context.startActivity(intent);
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
        mFlooVersion = view.findViewById(R.id.tv_floo_version);
        mTvNet = view.findViewById(R.id.tv_about_us_net);
        mTvPhone = view.findViewById(R.id.tv_about_us_contact);
        TextView tvCopyRight = view.findViewById(R.id.copyright);
        String ret = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
        tvCopyRight.setText(String.format(getString(R.string.about_us_company), ret));
        mTvNet.setText(mUrl = getString(R.string.about_us_net));
        mTvPhone.setText(mPhone = getString(R.string.about_us_phone));
        return view;
    }

    @Override
    protected void setViewListener() {
        // 跳转浏览器
        mTvNet.setOnClickListener(
                v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl))));
        // 弹起打电话pop
        mTvPhone.setOnClickListener(v -> {
            if (mTelLinkPopupWindow == null) {
                mTelLinkPopupWindow = new TelLinkPopupWindow(this);
            }
            mTelLinkPopupWindow.showAsDropDown(mPhone, mTvPhone);
        });
    }

    @Override
    protected void initDataForActivity() {
        // 获取app版本
        String versionName = BuildConfig.VERSION_NAME;
        mAppVersion.setText("Lanying IM Version:" + versionName);
        BMXClient client = BaseManager.getBMXClient();
        String sdkVersion = "";
        if (client != null && client.getSDKConfig() != null
                && !TextUtils.isEmpty(client.getSDKConfig().getSDKVersion())) {
            sdkVersion = client.getSDKConfig().getSDKVersion();
        }
        mFlooVersion.setText("FlooSDK Version:" + sdkVersion);
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
