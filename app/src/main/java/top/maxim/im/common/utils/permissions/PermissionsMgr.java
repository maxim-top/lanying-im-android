
package top.maxim.im.common.utils.permissions;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.provider.Settings;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import top.maxim.im.R;
import top.maxim.im.common.utils.AppContextUtils;

/**
 * Description : 权限管理类
 */
public class PermissionsMgr {
    private static final String TAG = PermissionsMgr.class.getSimpleName();

    /**
     * 待处理的请求
     */
    private final Set<String> mPendingRequests = new HashSet<>(1);

    /**
     * 权限
     */
    private static final Set<String> mPermissions = new HashSet<>(1);

    /**
     * 待处理的操作
     */
    private final List<PermissionsResultAction> mPendingActions = new ArrayList<>(1);

    private static PermissionsMgr mInstance = null;

    private static ViewGroup mRootViewGroup = null;

    private static View mOverlayView = null;

    private static ActivityResultLauncher<String[]> mPermissionLauncher;

    private static Map<String, String> mTitles = new HashMap<String, String>() {
        {
            put(PermissionsConstant.READ_STORAGE, AppContextUtils.getApplication().getResources().getString(R.string.permission_read_storage));
            put(PermissionsConstant.FINE_LOCATION, AppContextUtils.getApplication().getResources().getString(R.string.permission_fine_location));
            put(PermissionsConstant.COARSE_LOCATION, AppContextUtils.getApplication().getResources().getString(R.string.permission_coarse_location));
            put(PermissionsConstant.CAMERA, AppContextUtils.getApplication().getResources().getString(R.string.permission_camera));
            put(PermissionsConstant.RECORD_AUDIO, AppContextUtils.getApplication().getResources().getString(R.string.permission_mic));
        }
    };

    private static Map<String, String> mTexts = new HashMap<String, String>() {
        {
            put(PermissionsConstant.READ_STORAGE, AppContextUtils.getApplication().getResources().getString(R.string.permission_text_read_storage));
            put(PermissionsConstant.FINE_LOCATION, AppContextUtils.getApplication().getResources().getString(R.string.permission_text_fine_location));
            put(PermissionsConstant.COARSE_LOCATION, AppContextUtils.getApplication().getResources().getString(R.string.permission_text_coarse_location));
            put(PermissionsConstant.CAMERA, AppContextUtils.getApplication().getResources().getString(R.string.permission_text_camera));
            put(PermissionsConstant.RECORD_AUDIO, AppContextUtils.getApplication().getResources().getString(R.string.permission_text_mic));
        }
    };

    public static PermissionsMgr getInstance() {
        if (mInstance == null) {
            mInstance = new PermissionsMgr();
        }
        initializePermissionsMap();
        return mInstance;
    }

    private PermissionsMgr() {

    }

    /**
     * 此方法使用反射来读取清单类中的所有权限。因为一些权限不存在于旧版本的安卓系统中
     */
    private static synchronized void initializePermissionsMap() {
        Field[] fields = Manifest.permission.class.getFields();
        for (Field field : fields) {
            String name;
            try {
                name = (String)field.get("");
                mPermissions.add(name);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Could not access field", e);
            }
        }
    }

    /**
     * 此方法检索在应用程序清单中声明的所有权限
     *
     * @return String[]
     */
    private synchronized String[] getManifestPermissions(@NonNull
    final Activity activity) {
        PackageInfo packageInfo = null;
        List<String> list = new ArrayList<>(1);
        try {
            packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(),
                    PackageManager.GET_PERMISSIONS);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "A problem occurred when retrieving permissions", e);
        }
        if (packageInfo != null) {
            String[] permissions = packageInfo.requestedPermissions;
            if (permissions != null) {
                list.addAll(Arrays.asList(permissions));
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 添加到当前列表中 将在权限完成时完成的挂起动作 收到。通过此方法的权限列表已注册
     * 在PermissionsResultAction对象，它将变更通知 这些权限
     *
     * @param permissions 权限列表
     */
    private synchronized void addPendingAction(@NonNull String[] permissions,
            @Nullable PermissionsResultAction action) {
        if (action == null) {
            return;
        }
        action.registerPermissions(permissions);
        mPendingActions.add(action);
    }

    /**
     * 此方法从挂起的动作列表中删除挂起的动作。 它是使用的情况下，已被授予的权限，所以 你立即希望删除从队列中等待的动作和 执行该动作。
     */
    private synchronized void removePendingAction(@Nullable PermissionsResultAction action) {
        for (Iterator<PermissionsResultAction> iterator = mPendingActions.iterator(); iterator
                .hasNext();) {
            PermissionsResultAction weakRef = iterator.next();
            if (weakRef == action || action == null) {
                iterator.remove();
            }
        }
    }

    public synchronized void setPermissionLauncher(ActivityResultLauncher<String[]> permissionLauncher){
        mPermissionLauncher = permissionLauncher;
    }

    /**
     * 这个静态方法可以用来检查你是否有一个特定的权限
     */
    public synchronized boolean hasPermission(@Nullable Context context,
            @NonNull String permission) {
        boolean hasPermission = false;
        if (context != null) {
            if (TextUtils.equals(permission, READ_EXTERNAL_STORAGE)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    boolean rmi = ContextCompat.checkSelfPermission(context, READ_MEDIA_IMAGES) == PERMISSION_GRANTED;
                    boolean rmv = ContextCompat.checkSelfPermission(context, READ_MEDIA_VIDEO) == PERMISSION_GRANTED;
                    Log.d("PermMgr", "rmi:"+rmi + " rmv:"+rmv);
                    if (rmi || rmv){
                        // Full access on Android 13 (API level 33) or higher
                        hasPermission = true;
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    boolean rmvss = ContextCompat.checkSelfPermission(context, READ_MEDIA_VISUAL_USER_SELECTED) == PERMISSION_GRANTED;
                    Log.d("PermMgr", "rmvss:"+rmvss);
                    if (rmvss){
                        // Partial access on Android 14 (API level 34) or higher
                        hasPermission = true;
                    }
                }  else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                    boolean res = ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
                    Log.d("PermMgr", "res:"+res);
                    if (res){
                        // Full access up to Android 12 (API level 32)
                        hasPermission = true;
                    }
                }
                return hasPermission;
            }

            int hasWriteContactsPermission = ActivityCompat.checkSelfPermission(context,
                    permission);
            if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                //是否要展示申请权限的理由
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)context,
                        permission)) {
                    // 连续拒绝或选择不再提示功能
                    hasPermission = false;
                }
            } else if (mPermissions.contains(permission)) {
                hasPermission = true;
            }
        }

        return hasPermission;
    }

    /**
     * 用来检查你是否有几个特定的权限
     */
    public synchronized boolean hasAllPermissions(@Nullable Context context,
            @NonNull String[] permissions) {
        if (context == null) {
            return false;
        }
        boolean hasAllPermissions = true;
        for (String perm : permissions) {
            boolean hp = hasPermission(context, perm);
            Log.d("PermMgr", "hp:"+hp);
            if (!hp){
                hasAllPermissions = false;
                break;
            }
        }
        return hasAllPermissions;
    }

    /**
     * 请求在应用程序清单中声明的所有权限,允许用户允许或拒绝每一个权限
     */
    public synchronized void requestAllManifestPermissionsIfNecessary(
            final @Nullable Activity activity, final @Nullable PermissionsResultAction action) {
        if (activity == null) {
            return;
        }
        String[] perms = getManifestPermissions(activity);
        requestPermissionsIfNecessaryForResult(activity, perms, action);
    }

    private void addOverlayView(@Nullable Activity activity, @NonNull String[] permissions){
        ViewGroup rootViewGroup = (ViewGroup) activity.getWindow().getDecorView().getRootView();
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
        View overlayView = inflater.inflate(R.layout.activity_permission_overlay, null);
        TextView tvTitle = overlayView.findViewById(R.id.overlay_title);
        TextView tvText = overlayView.findViewById(R.id.overlay_text);
        StringBuffer sbTitle = new StringBuffer();
        StringBuffer sbText = new StringBuffer();
        for (String permission: permissions) {
            if (!TextUtils.isEmpty(mTexts.get(permission))){
                sbText.append(mTexts.get(permission));
                sbText.append("\n");
                sbTitle.append(mTitles.get(permission));
                sbTitle.append("/");
            }
        }
        if (sbText.length()>0){
            sbTitle.delete(sbTitle.length()-1,sbTitle.length());
            tvTitle.setText(sbTitle.toString());
            tvText.setText(sbText.toString());
        }

        rootViewGroup.addView(overlayView);
        mRootViewGroup = rootViewGroup;
        mOverlayView = overlayView;
    }
    /**
     * 用来执行一个数组 传递给该方法的权限,该方法将请求的权限，如果 他们需要被要求（即我们没有许可），并会增加
     * PermissionsResultAction到队列被通知的权限被授予或 否认
     */
    public synchronized void requestPermissionsIfNecessaryForResult(@Nullable Activity activity,
                                                                    @NonNull String[] permissions, @Nullable PermissionsResultAction action) {
        if (activity == null) {
            return;
        }
        Log.i(TAG,
                " requestPermissionsIfNecessaryForResult permissions length:" + permissions.length);

        addOverlayView(activity, permissions);
        addPendingAction(permissions, action);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ){
            List<String> permList = new ArrayList<>();
            for (String permission: permissions) {
                if (!TextUtils.equals(permission, READ_EXTERNAL_STORAGE)){
                    permList.add(permission);
                }
            }
            if (permList.size() < permissions.length && mPermissionLauncher != null){
                String[] perms = new String[permissions.length+1];
                int i = 0;
                for (String p: permList) {
                    perms[i++] = p;
                }
                perms[i++] = READ_MEDIA_IMAGES;
                perms[i] = READ_MEDIA_VIDEO;
                mPermissionLauncher.launch(perms);
                return;
            }
        }
        // 如果小于23，直接返回现在各权限状态
        if (Build.VERSION.SDK_INT < 23) {
            doPermissionWorkBeforeAndroidM(activity, permissions, action);
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2 && mPermissionLauncher!=null) {
            mPermissionLauncher.launch(permissions);
        } else {
            List<String> permList = getPermissionsListToRequest(activity, permissions, action);
            if (permList.isEmpty()) {
                // if there is no  permission to request, there is no reason to
                // keep the action int the list
                removePendingAction(action);
            } else {
                String[] permsToRequest = permList.toArray(new String[permList.size()]);
                mPendingRequests.addAll(permList);
                ActivityCompat.requestPermissions(activity, permsToRequest, 1);
            }
        }
    }

    /**
     * 用来执行一个数组 传递给该方法的权限,该方法将请求的权限，如果 他们需要被要求（即我们没有许可），并会增加
     * PermissionsResultAction到队列被通知的权限被授予或 否认
     */
    public synchronized void requestPermissionsIfNecessaryForResult(@NonNull Fragment fragment,
                                                                    @NonNull String[] permissions, @Nullable PermissionsResultAction action) {
        Activity activity = fragment.getActivity();
        if (activity == null) {
            return;
        }
        requestPermissionsIfNecessaryForResult(activity, permissions, action);
    }
    public synchronized void permissionProcessed(){
        mRootViewGroup.removeView(mOverlayView);
    }

    /**
     * 这个方法通知permissionsMgr，权限已经改变。如果你正在做 使用活动的权限请求，则应调用此方法
     * 活动回调onRequestPermissionsResult()与传递到方法的变量
     */
    public synchronized void notifyPermissionsChange(@NonNull String[] permissions,
            @NonNull int[] results) {
        Log.i(TAG, " notifyPermissionsChange permissions length:" + permissions.length
                + ";results length:" + results.length);
        mRootViewGroup.removeView(mOverlayView);

        int size = permissions.length;
        if (results.length < size) {
            size = results.length;
        }
        List<String> grantedList = new ArrayList<>();
        List<String> deniedList = new ArrayList<>();
        for (PermissionsResultAction mPendingAction : mPendingActions) {
            for (int n = 0; n < size; n++) {
                switch (results[n]) {
                    case PermissionsConstant.GRANTED:
                        grantedList.add(permissions[n]);
                        break;
                    case PermissionsConstant.DENIED:
                        deniedList.add(permissions[n]);
                        break;
                    default:
                        grantedList.add(permissions[n]);
                        break;
                }

                mPendingRequests.remove(permissions[n]);
            }
            if (mPendingAction != null) {
                if (grantedList.size() > 0) {
                    mPendingAction.onResult(grantedList, PermissionsConstant.GRANTED);
                }
                if (deniedList.size() > 0) {
                    mPendingAction.onResult(deniedList, PermissionsConstant.DENIED);
                }
                removePendingAction(mPendingAction);
            }
        }
    }

    /**
     * 在安卓设备前请求权限 根据权限状态，直接进行或拒绝工作
     */
    private void doPermissionWorkBeforeAndroidM(@NonNull Context activity,
                                                @NonNull String[] permissions, @Nullable PermissionsResultAction action) {
        Log.i(TAG, "doPermissionWorkBeforeAndroidM permissions length:" + permissions.length);
        List<String> grantedList = new ArrayList<>();
        List<String> deniedList = new ArrayList<>();
        List<String> noFoundList = new ArrayList<>();

        if (action != null) {
            for (String perm : permissions) {
                if (!mPermissions.contains(perm)) {
                    noFoundList.add(perm);
                } else if (!hasPermission(activity, perm)) {
                    deniedList.add(perm);
                } else {
                    grantedList.add(perm);
                }
            }
            if (grantedList.size() > 0) {
                action.onResult(grantedList, PermissionsConstant.GRANTED);
            }
            if (deniedList.size() > 0) {
                action.onResult(deniedList, PermissionsConstant.DENIED);
            }
            if (noFoundList.size() > 0) {
                action.onResult(noFoundList, PermissionsConstant.NOT_FOUND);
            }
        }
    }

    /**
     * 过滤权限列表： 如果未授予权限，则将其添加到结果列表中 如果一个权限被授予，则做授予的工作，不要将其添加到结果列表中,如果是拒绝的，则会重新请求
     */
    private List<String> getPermissionsListToRequest(@NonNull Context activity,
                                                     @NonNull String[] permissions, @Nullable PermissionsResultAction action) {
        Log.i(TAG, "getPermissionsListToRequest permissions length:" + permissions.length);
        List<String> permList = new ArrayList<>(permissions.length);
        List<String> grantedList = new ArrayList<>();
        List<String> noFoundList = new ArrayList<>();
        for (String perm : permissions) {
            if (!mPermissions.contains(perm)) {
                noFoundList.add(perm);
            } else if (!hasPermission(activity, perm)) {
                if (!mPendingRequests.contains(perm)) {
                    permList.add(perm);
                }
            } else {
                grantedList.add(perm);
            }
        }
        if (action != null) {
            if (grantedList.size() > 0) {
                action.onResult(grantedList, PermissionsConstant.GRANTED);
            }
            if (noFoundList.size() > 0) {
                action.onResult(noFoundList, PermissionsConstant.NOT_FOUND);
            }
        }
        return permList;
    }

    // ===========================特殊权限======================================
    /**
     * 这个静态方法可以用来检查你是否有系统悬浮层的权限
     */
    @TargetApi(Build.VERSION_CODES.M)
    public synchronized boolean hasAlertWindowPermission(@Nullable Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
    }

    /**
     * 这个静态方法可以用来检查你是否有写系统设置的权限
     */
    @TargetApi(Build.VERSION_CODES.M)
    public synchronized boolean hasWriteSettingPermission(@Nullable Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.System.canWrite(context);
    }
}
