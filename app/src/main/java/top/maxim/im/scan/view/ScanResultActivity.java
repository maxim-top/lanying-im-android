
package top.maxim.im.scan.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import top.maxim.im.R;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.contact.view.RosterDetailActivity;
import top.maxim.im.group.view.GroupQrcodeDetailActivity;
import top.maxim.im.login.view.LoginActivity;
import top.maxim.im.login.view.SettingUserActivity;
import top.maxim.im.scan.config.ScanConfigs;

/**
 * Description : 二维码结果页面
 */
public class ScanResultActivity extends BaseTitleActivity {

    private String mResult;

    private TextView textView;

    public static void openScanResult(Context context, String result) {
        Intent intent = new Intent(context, ScanResultActivity.class);
        intent.putExtra(ScanConfigs.CODE_RESULT, result);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setBackIcon(R.drawable.header_back_icon, v -> finish());
        builder.setTitle("二维码结果");
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        textView = new TextView(this);
        textView.setTextColor(getResources().getColor(R.color.color_black));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        return textView;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mResult = intent.getStringExtra(ScanConfigs.CODE_RESULT);
        }
        if (TextUtils.isEmpty(mResult)) {
            finish();
            return;
        }
        if (mResult.startsWith(ScanConfigs.CODE_ROSTER_PRE)) {
            // roster
            long rosterId = Long.valueOf(mResult.replace(ScanConfigs.CODE_ROSTER_PRE, ""));
            long myId = SharePreferenceUtils.getInstance().getUserId();
            if (rosterId == myId) {
                // 自己进入设置页面
                SettingUserActivity.openSettingUser(this);
            } else {
                RosterDetailActivity.openRosterDetail(this, rosterId);
            }
            finish();
        } else if (mResult.startsWith(ScanConfigs.CODE_GROUP_PRE)) {
            // 群聊
            String result = mResult.substring(2);
            String[] info = result.split("_");
            long groupId = Long.valueOf(info[0]);
            String qrInfo = info[1];
            GroupQrcodeDetailActivity.openGroupQrcodeDetail(this, groupId, qrInfo);
            finish();
        } else if (mResult.startsWith(ScanConfigs.CODE_DEMO)) {
            // 二维码体验功能
            String result = mResult.substring(2);
            String[] info = result.split("_");
            ScanConfigs.CODE_APP_ID = info[0];
            ScanConfigs.CODE_USER_ID = info[1];
            ScanConfigs.CODE_USER_NAME = info[2];
            LoginActivity.openLogin(this);
            finish();
        }
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        textView.setText(mResult);
    }
}
