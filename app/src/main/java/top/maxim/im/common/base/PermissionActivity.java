
package top.maxim.im.common.base;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import top.maxim.im.common.utils.permissions.PermissionsMgr;
import top.maxim.im.common.utils.permissions.PermissionsResultAction;

/**
 * Description : 基础权限activity Created by Mango on 2018/11/05
 */
public class PermissionActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public boolean hasPermission(String... permission) {
        return PermissionsMgr.getInstance().hasAllPermissions(this, permission);
    }

    public void requestPermissions(String... permissions) {
        if (!PermissionsMgr.getInstance().hasAllPermissions(this, permissions)) {
            PermissionsMgr.getInstance().requestPermissionsIfNecessaryForResult(this, permissions,
                    new PermissionsResultAction() {

                        @Override
                        public void onGranted(List<String> perms) {
                            Log.d(TAG, "Permission is Granted:" + perms);
                            onPermissionGranted(perms);
                        }

                        @Override
                        public void onDenied(List<String> perms) {
                            Log.d(TAG, "Permission is Denied" + perms);
                            onPermissionDenied(perms);
                        }
                    });
        } else {
            onPermissionGranted(Arrays.asList(permissions));
        }
    }

    public void onPermissionDenied(List<String> permissions) {
        PermissionsMgr.getInstance().permissionProcessed();
    }

    public void onPermissionGranted(List<String> permissions) {
        PermissionsMgr.getInstance().permissionProcessed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        PermissionsMgr.getInstance().notifyPermissionsChange(permissions, grantResults);
    }

}
