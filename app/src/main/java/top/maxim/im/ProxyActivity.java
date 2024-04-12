package top.maxim.im;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import top.maxim.im.common.base.BaseActivity;
import top.maxim.im.common.utils.SchemeUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.login.view.WelcomeActivity;

public class ProxyActivity extends BaseActivity {
    private static final String TAG = "ProxyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            dispatchIntent(getIntent());
        }
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        dispatchIntent(intent);
        finish();
    }

    private void dispatchIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        Uri uri = intent.getData();
        String urlString = uri.toString();
        Context context = getApplicationContext();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int activityCount = activityManager.getAppTasks().get(0).getTaskInfo().numActivities;
        if (activityCount == 1) {
            SharePreferenceUtils.getInstance().putDeepLink(urlString);
            WelcomeActivity.openWelcome(getApplicationContext());
        } else {
            boolean isLogin = SharePreferenceUtils.getInstance().getLoginStatus();
            if (isLogin){
                SchemeUtils.handleScheme(this, urlString);
            }else{
                SharePreferenceUtils.getInstance().putDeepLink(urlString);
            }
        }
    }
}