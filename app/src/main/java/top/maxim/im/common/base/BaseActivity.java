
package top.maxim.im.common.base;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;

import java.util.List;
import java.util.Locale;

import top.maxim.im.common.utils.SharePreferenceUtils;

/**
 * Description : 基础activity Created by Mango on 2018/11/05.
 */
public class BaseActivity extends PermissionActivity {

    private String getLanguage() {
        Locale locale;
        String language = SharePreferenceUtils.getInstance().getAppLanguage();
        if (!language.isEmpty()){
            return language;
        }

        //7.0以上和7.0以下获取系统语言方式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = Locale.getDefault();
        }
        return locale.getLanguage();
    }

    private void initLanguage() {
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        String language = getLanguage();
        if (language.equals("en")) {
            config.locale = Locale.US; //android 7.0以下操作系统不识别Locale.ENGLISH
        } else {
            config.locale = Locale.CHINESE;
        }
        resources.updateConfiguration(config, dm);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initLanguage();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onPermissionDenied(List<String> permissions) {
    }

    @Override
    public void onPermissionGranted(List<String> permissions) {
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
