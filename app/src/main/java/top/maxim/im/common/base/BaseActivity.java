
package top.maxim.im.common.base;

import android.os.Bundle;

import java.util.List;

/**
 * Description : 基础activity Created by Mango on 2018/11/05.
 */
public class BaseActivity extends PermissionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
