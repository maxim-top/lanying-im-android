
package top.maxim.im.common.utils.permissions;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Description : 请求的权限被授予或拒绝时可以执行
 */
public abstract class PermissionsResultAction {
    private static final String TAG = PermissionsResultAction.class.getSimpleName();

    private final Set<String> mPermissions = new HashSet<>(1);

    private Looper mLooper = Looper.getMainLooper();

    public PermissionsResultAction() {
    }

    public PermissionsResultAction(@NonNull Looper looper) {
        mLooper = looper;
    }

    /**
     * 当所有权限 已被用户授予
     */
    public abstract void onGranted(List<String> permissions);

    /**
     * 为当一个权限被拒绝的时候
     */
    public abstract void onDenied(List<String> permissions);

    private Handler mHandler = new Handler(mLooper) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    public synchronized boolean shouldIgnorePermissionNotFound(String permission) {
        return true;
    }

    /**
     * 当一个特定的权限已更改
     */
    protected synchronized final boolean onResult(final List<String> permissions,
            final int result) {
        Log.i(TAG, "onResult permissions length:" + permissions.size() + ":" + permissions
                + "\n all mPermissions:" + mPermissions);
        mPermissions.removeAll(permissions);
        switch (result) {
            case PermissionsConstant.GRANTED:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onGranted(permissions);
                        Log.i(TAG, "Permission is Granted: " + permissions);
                    }
                });

                return true;
            case PermissionsConstant.DENIED:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onDenied(permissions);
                        Log.i(TAG, "Permission is Denied: " + permissions);
                    }
                });
                return true;
            case PermissionsConstant.NOT_FOUND:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onGranted(permissions);
                        Log.i(TAG, "Permission not found: " + permissions);
                    }
                });
                return true;
        }
        return false;
    }

    /**
     * 注册指定的权限对象的PermissionsResultAction 让它知道哪些权限来查找更改
     */
    protected synchronized final void registerPermissions(@NonNull String[] perms) {
        Collections.addAll(mPermissions, perms);
    }
}
