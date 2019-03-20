
package top.maxim.im.common.provider;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import top.maxim.im.R;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;

/**
 * Description : 公共provider Created by Mango on 2018/11/11.
 */
public class CommonProvider {

    /**
     * 权限被拒绝弹出框
     * 
     * @param activity 上下文
     */
    public static void openAppPermission(final Activity activity) {
        if (activity != null && !activity.isFinishing()) {
            DialogUtils.getInstance().showDialog(activity,
                    activity.getResources().getString(R.string.permission_title),
                    activity.getResources().getString(R.string.permission_prompt),
                    activity.getResources().getString(R.string.permission_setting),
                    activity.getResources().getString(R.string.cancel),
                    new CommonDialog.OnDialogListener() {

                        @Override
                        public void onConfirmListener() {
                            openSystemSetting(activity);
                        }

                        @Override
                        public void onCancelListener() {
                            ToastUtil.showTextViewPrompt(R.string.not_has_sd_permission);
                        }
                    });
        }
    }

    /**
     * 打开系统设置
     * 
     * @param activity 上下文
     */
    public static void openSystemSetting(Activity activity) {
        Uri packageURI = Uri.parse("package:" + AppContextUtils.getPackageName(activity));
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
        activity.startActivity(intent);
    }
}
