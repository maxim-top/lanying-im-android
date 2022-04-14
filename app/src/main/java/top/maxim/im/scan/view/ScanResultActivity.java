
package top.maxim.im.scan.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.contact.view.RosterDetailActivity;
import top.maxim.im.group.view.GroupQrcodeDetailActivity;
import top.maxim.im.login.view.LoginActivity;
import top.maxim.im.login.view.SettingUserActivity;
import top.maxim.im.net.HttpResponseCallback;
import top.maxim.im.scan.config.ScanConfigs;
import top.maxim.im.scan.utils.QRCodeConfig;

/**
 * Description : 二维码结果页面
 */
public class ScanResultActivity extends BaseTitleActivity {

    private String mResult;

    private TextView textView;

    private boolean isUploadToken = false;

    public static void openScanResult(Context context, String result) {
        Intent intent = new Intent(context, ScanResultActivity.class);
        intent.putExtra(ScanConfigs.CODE_RESULT, result);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setBackIcon(R.drawable.header_back_icon, v -> finish());
        builder.setTitle(getString(R.string.qr_code_result));
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
        String source = "", action = "", info = "";
        try {
            JSONObject jsonObject = new JSONObject(mResult);
            if (jsonObject.has("source")) {
                source = jsonObject.getString("source");
            }
            if (jsonObject.has("action")) {
                action = jsonObject.getString("action");
            }
            if (jsonObject.has("info")) {
                info = jsonObject.getJSONObject("info").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.equals(source, QRCodeConfig.SOURCE.APP)) {
            boolean isLogin = SharePreferenceUtils.getInstance().getLoginStatus();
            // 非二维码体验功能 如果未登录不能识别
            if (!isLogin) {
                ToastUtil.showTextViewPrompt(getString(R.string.failed_to_recognize_qr_code_info));
                finish();
                return;
            }
            if (TextUtils.equals(action, QRCodeConfig.ACTION.PROFILE)) {
                // roster
                dealAppProfile(source, action, info);
            } else if (TextUtils.equals(action, QRCodeConfig.ACTION.GROUP)) {
                // 群聊
                dealAppGroup(source, action, info);
            }
        } else if (TextUtils.equals(source, QRCodeConfig.SOURCE.CONSOLE)) {
            boolean isLogin = SharePreferenceUtils.getInstance().getLoginStatus();
            // 二维码体验功能
            if (isLogin) {
                // 如果已登录 只可以识别上传deviceToken
                if (TextUtils.equals(action, QRCodeConfig.ACTION.UPLOAD_DEVICE_TOKEN)) {
                    dealConsoleUploadToken(source, action, info);
                } else {
                    ToastUtil.showTextViewPrompt(getString(R.string.failed_to_recognize_qr_code_info));
                    finish();
                    return;
                }
            } else {
                // 如果未登录 可以识别上传login app
                // 二维码体验功能 login
                if (TextUtils.equals(action, QRCodeConfig.ACTION.LOGIN)) {
                    dealConsoleLogin(source, action, info);
                } else if (TextUtils.equals(action, QRCodeConfig.ACTION.APP)) {
                    // 二维码体验功能 app
                    dealConsoleApp(source, action, info);
                } else {
                    ToastUtil.showTextViewPrompt(getString(R.string.failed_to_recognize_qr_code_info));
                    finish();
                    return;
                }
            }
        }
    }

    /**
     * 处理profile
     */
    private void dealAppProfile(String source, String action, String info) {
        if (!TextUtils.equals(source, QRCodeConfig.SOURCE.APP)
                || !TextUtils.equals(action, QRCodeConfig.ACTION.PROFILE)
                || TextUtils.isEmpty(info)) {
            return;
        }
        long uid = 0;
        try {
            JSONObject jsonObject = new JSONObject(info);
            if (jsonObject.has("uid")) {
                uid = jsonObject.getLong("uid");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        long myId = SharePreferenceUtils.getInstance().getUserId();
        if (uid == myId) {
            // 自己进入设置页面
            SettingUserActivity.openSettingUser(this);
        } else {
            RosterDetailActivity.openRosterDetail(this, uid);
        }
        finish();
    }

    /**
     * 处理group
     */
    private void dealAppGroup(String source, String action, String info) {
        if (!TextUtils.equals(source, QRCodeConfig.SOURCE.APP)
                || !TextUtils.equals(action, QRCodeConfig.ACTION.GROUP)
                || TextUtils.isEmpty(info)) {
            return;
        }
        long groupId = 0;
        String qrInfo = null;
        try {
            JSONObject jsonObject = new JSONObject(info);
            if (jsonObject.has("group_id")) {
                groupId = jsonObject.getLong("group_id");
            }
            if (jsonObject.has("info")) {
                qrInfo = jsonObject.getString("info");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        GroupQrcodeDetailActivity.openGroupQrcodeDetail(this, groupId, qrInfo);
        finish();
    }

    /**
     * 处理二维码体验功能 登陆
     */
    private void dealConsoleLogin(String source, String action, String info) {
        if (!TextUtils.equals(source, QRCodeConfig.SOURCE.CONSOLE)
                || !TextUtils.equals(action, QRCodeConfig.ACTION.LOGIN)
                || TextUtils.isEmpty(info)) {
            return;
        }
        String appId = null;
        try {
            JSONObject jsonObject = new JSONObject(info);
            if (jsonObject.has("app_id")) {
                appId = jsonObject.getString("app_id");
            }
            if (jsonObject.has("uid")) {
                ScanConfigs.CODE_USER_ID = jsonObject.getString("uid");
            }
            if (jsonObject.has("username")) {
                ScanConfigs.CODE_USER_NAME = jsonObject.getString("username");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(appId)) {
//            isUploadToken = true;
//            showLoadingDialog(true);
//            String finalAppId = appId;
            SharePreferenceUtils.getInstance().putAppId(appId);
            UserManager.getInstance().changeAppId(appId, bmxErrorCode -> {
                dismissLoadingDialog();
//                if (BaseManager.bmxFinish(bmxErrorCode)) {
//                    SharePreferenceUtils.getInstance().putAppId(finalAppId);
//                } else {
//                    ToastUtil.showTextViewPrompt("切换AppId失败");
//                }
//                LoginActivity.openLogin(this);
//                finish();
            });
            LoginActivity.openLogin(this);
            finish();
        } else {
            LoginActivity.openLogin(this);
            finish();
        }
    }

    /**
     * 处理二维码体验功能
     */
    private void dealConsoleApp(String source, String action, String info) {
        if (!TextUtils.equals(source, QRCodeConfig.SOURCE.CONSOLE)
                || !TextUtils.equals(action, QRCodeConfig.ACTION.APP) || TextUtils.isEmpty(info)) {
            return;
        }
        String appId = null;
        try {
            JSONObject jsonObject = new JSONObject(info);
            if (jsonObject.has("app_id")) {
                appId = jsonObject.getString("app_id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(appId)) {
//            isUploadToken = true;
//            showLoadingDialog(true);
//            String finalAppId = appId;
            SharePreferenceUtils.getInstance().putAppId(appId);
            UserManager.getInstance().changeAppId(appId, bmxErrorCode -> {
//                dismissLoadingDialog();
//                if (BaseManager.bmxFinish(bmxErrorCode)) {
//                    SharePreferenceUtils.getInstance().putAppId(finalAppId);
//                } else {
//                    ToastUtil.showTextViewPrompt("切换AppId失败");
//                }
//                LoginActivity.openLogin(this);
//                finish();
            });
            LoginActivity.openLogin(this);
            finish();
        } else {
            LoginActivity.openLogin(this);
            finish();
        }
    }

    /**
     * 处理二维码体验功能 上传deviceToken
     */
    private void dealConsoleUploadToken(String source, String action, String info) {
        if (!TextUtils.equals(source, QRCodeConfig.SOURCE.CONSOLE)
                || !TextUtils.equals(action, QRCodeConfig.ACTION.UPLOAD_DEVICE_TOKEN)
                || TextUtils.isEmpty(info)) {
            return;
        }
        String platformType = null;
        String deviceInfo = null;
        try {
            JSONObject jsonObject = new JSONObject(info);
            if (jsonObject.has("platform_type")) {
                platformType = jsonObject.getString("platform_type");
            }
            if (jsonObject.has("info")) {
                deviceInfo = jsonObject.getString("info");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(platformType) || TextUtils.equals(platformType, "1")
                || TextUtils.isEmpty(deviceInfo)) {
            // 1 为ios
            ToastUtil.showTextViewPrompt("上传deviceToken失败");
            finish();
            return;
        }
        isUploadToken = true;
        showLoadingDialog(true);
        String finalDeviceInfo = deviceInfo;
        AppManager.getInstance().getTokenByName(SharePreferenceUtils.getInstance().getUserName(),
                SharePreferenceUtils.getInstance().getUserPwd(),
                new HttpResponseCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        AppManager.getInstance().uploadPushInfo(result, finalDeviceInfo,
                                SharePreferenceUtils.getInstance().getAppId(),
                                new HttpResponseCallback<Boolean>() {
                                    @Override
                                    public void onResponse(Boolean result) {
                                        dismissLoadingDialog();
                                        ToastUtil.showTextViewPrompt(
                                                result != null && result ? getString(R.string.devicetoken_uploaded_successfully)
                                                        : getString(R.string.devicetoken_uploading_failed));
                                        finish();
                                    }

                                    @Override
                                    public void onFailure(int errorCode, String errorMsg,
                                            Throwable t) {
                                        dismissLoadingDialog();
                                        ToastUtil.showTextViewPrompt("上传deviceToken失败");
                                        finish();
                                    }
                                });
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        dismissLoadingDialog();
                        ToastUtil.showTextViewPrompt(getString(R.string.failed_to_get_token));
                        finish();
                    }
                });
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        if (!isUploadToken) {
            textView.setText(mResult);
        }
    }
}
