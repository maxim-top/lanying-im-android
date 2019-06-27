
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
import top.maxim.im.common.view.Header;
import top.maxim.im.contact.view.RosterDetailActivity;
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
            long rosterId = Long
                    .valueOf(mResult.replace(ScanConfigs.CODE_ROSTER_PRE, ""));
            RosterDetailActivity.openRosterDetail(this, rosterId);
            finish();
        } else if (mResult.startsWith(ScanConfigs.CODE_GROUP_PRE)) {
            // 群聊
            long groupId = Long
                    .valueOf(mResult.replace(ScanConfigs.CODE_GROUP_PRE, ""));
        }
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        textView.setText(mResult);
    }
}
