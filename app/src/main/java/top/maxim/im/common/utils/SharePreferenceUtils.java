
package top.maxim.im.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.ArraySet;

import java.util.Set;

import top.maxim.im.scan.config.ScanConfigs;

/**
 * Description : sp缓存工具类 Created by Mango on 2018/11/06.
 */

public class SharePreferenceUtils {

    private static final String SH_DATA_NAME = "maxIM_user_data";

    private static final String NOTICE_ID_DATA_NAME = "notice_id_list";

    private static final String ABOUT_POPPED_APP_ID_DATA_NAME = "about_popped_app_id_list";

    private static String IS_LOGIN = "isLogin";

    private static String USER_ID = "user_id";

    private static String AGREE_CHECKED = "agree_checked";//是否选中"我已阅读并同意《用户服务》及《隐私政策》"

    private static String HAS_PUSH = "has_push_server";//当前设备有推送平台

    private static String USER_NAME = "user_name";

    private static String USER_PWD = "user_pwd";

    private static String DEEP_LINK = "deep_link";

    private static String TOKEN = "token";

    private static String GUARDIAN_MODE_PASSWORD = "guardianModePassword";

    private static String DEVICE_ID = "device_id";

    private static String PUSH_TOKEN_DEV_TYPE = "pushTokenDevType";

    private static String PUSH_TOKEN = "pushToken";

    private static String DEV_TIPS = "devTips";

    private static String CUSTOM_DNS = "customDns";

    private static String APP_ID = "app_id";

    private static String APP_ID_HISTORY = "app_id_history";

    private static String IS_FIRST = "isFirst";

    private static String IS_SHOW_PROTOCOL_DIALOG = "isShowProtocolDialog";

    private static String LOGIN_USER_DATA = "loginUserData";

    private static String DNS_CONFIG = "dnsConfig";

    private static String APP_LANGUAGE = "appLanguage";

    private static volatile SharePreferenceUtils instance;

    private static SharedPreferences.Editor saveEditor;

    private static SharedPreferences saveInfo;

    private static SharedPreferences.Editor noticeIdEditor;

    private static SharedPreferences noticeId;

    private static SharedPreferences.Editor aboutPoppedAppIdEditor;

    private static SharedPreferences aboutPoppedAppId;

    public static SharePreferenceUtils getInstance() {
        if (instance == null) {
            synchronized (SharePreferenceUtils.class) {
                if (instance == null) {
                    instance = new SharePreferenceUtils();
                    saveInfo = AppContextUtils.getAppContext().getSharedPreferences(SH_DATA_NAME,
                            Context.MODE_PRIVATE);
                    saveEditor = saveInfo.edit();
                    noticeId = AppContextUtils.getAppContext().getSharedPreferences(NOTICE_ID_DATA_NAME,
                            Context.MODE_PRIVATE);
                    noticeIdEditor = noticeId.edit();
                    aboutPoppedAppId = AppContextUtils.getAppContext().getSharedPreferences(ABOUT_POPPED_APP_ID_DATA_NAME,
                            Context.MODE_PRIVATE);
                    aboutPoppedAppIdEditor = aboutPoppedAppId.edit();
                }
            }
        }
        return instance;
    }

    // 当前用户或群ID已被提醒过
    public boolean putNoticeId(long id) {
        noticeIdEditor.putBoolean(String.valueOf(id), true);
        return noticeIdEditor.commit();
    }

    // 当前用户或群ID是否已被提醒过
    public boolean getNoticeId(long id) {
        if (noticeId != null) {
            return noticeId.getBoolean(String.valueOf(id), false);
        }
        return false;
    }

    // 当前App ID已弹出过关于我们
    public boolean putAboutPoppedAppId(String id) {
        aboutPoppedAppIdEditor.putBoolean(id, true);
        return aboutPoppedAppIdEditor.commit();
    }

    // 当前App ID是否已弹出过关于我们
    public boolean getAboutPoppedAppId(String id) {
        if (aboutPoppedAppId != null) {
            return aboutPoppedAppId.getBoolean(id, false);
        }
        return false;
    }

    public boolean putLoginStatus(boolean status) {
        saveEditor.putBoolean(IS_LOGIN, status);
        return saveEditor.commit();
    }

    public boolean getLoginStatus() {
        if (saveInfo != null) {
            return saveInfo.getBoolean(IS_LOGIN, false);
        }
        return false;
    }

    public boolean putAgreeChecked(boolean agreeChecked) {
        saveEditor.putBoolean(AGREE_CHECKED, agreeChecked);
        return saveEditor.commit();
    }

    public boolean getAgreeChecked() {
        if (saveInfo != null) {
            return saveInfo.getBoolean(AGREE_CHECKED, false);
        }
        return false;
    }

    public boolean putHasPush(boolean hasPush) {
        saveEditor.putBoolean(HAS_PUSH, hasPush);
        return saveEditor.commit();
    }

    public boolean getHasPush() {
        if (saveInfo != null) {
            return saveInfo.getBoolean(HAS_PUSH, false);
        }
        return false;
    }

    public boolean putUserId(long userId) {
        saveEditor.putLong(USER_ID, userId);
        return saveEditor.commit();
    }

    public long getUserId() {
        if (saveInfo != null) {
            return saveInfo.getLong(USER_ID, -1);
        }
        return -1;
    }

    public boolean putUserName(String userName) {
        saveEditor.putString(USER_NAME, userName);
        return saveEditor.commit();
    }

    public String getUserName() {
        if (saveInfo != null) {
            return saveInfo.getString(USER_NAME, "");
        }
        return "";
    }

    public boolean putUserPwd(String pwd) {
        saveEditor.putString(USER_PWD, pwd);
        return saveEditor.commit();
    }

    public boolean putDeepLink(String link) {
        saveEditor.putString(DEEP_LINK, link);
        return saveEditor.commit();
    }

    public String getUserPwd() {
        if (saveInfo != null) {
            return saveInfo.getString(USER_PWD, null);
        }
        return null;
    }

    public String getDeepLink() {
        if (saveInfo != null) {
            return saveInfo.getString(DEEP_LINK, null);
        }
        return null;
    }

    public boolean putToken(String token) {
        saveEditor.putString(TOKEN, token);
        return saveEditor.commit();
    }

    public String getToken() {
        if (saveInfo != null) {
            return saveInfo.getString(TOKEN, null);
        }
        return null;
    }

    public boolean putGuardianModePassword(String val) {
        saveEditor.putString(GUARDIAN_MODE_PASSWORD, val);
        return saveEditor.commit();
    }

    public String getGuardianModePassword() {
        if (saveInfo != null) {
            return saveInfo.getString(GUARDIAN_MODE_PASSWORD, null);
        }
        return null;
    }

    public boolean putDeviceId(String deviceId) {
        saveEditor.putString(DEVICE_ID, deviceId);
        return saveEditor.commit();
    }

    public String getDeviceId() {
        if (saveInfo != null) {
            return saveInfo.getString(DEVICE_ID, null);
        }
        return null;
    }

    public boolean putPushToken(int devType, String token) {
        saveEditor.putInt(PUSH_TOKEN_DEV_TYPE, devType).putString(PUSH_TOKEN, token);
        return saveEditor.commit();
    }

    public String getPushToken() {
        if (saveInfo != null) {
            return saveInfo.getString(PUSH_TOKEN, null);
        }
        return null;
    }

    public boolean putDevTips(boolean tips) {
        saveEditor.putBoolean(DEV_TIPS, tips);
        return saveEditor.commit();
    }

    public boolean getDevTips() {
        if (saveInfo != null) {
            return saveInfo.getBoolean(DEV_TIPS, false);
        }
        return false;
    }

    public boolean putCustomDns(int index) {
        saveEditor.putInt(CUSTOM_DNS, index);
        return saveEditor.commit();
    }

    public int getCustomDns() {
        if (saveInfo != null) {
            return saveInfo.getInt(CUSTOM_DNS, 0);
        }
        return 0;
    }

    public boolean putAppId(String appId) {
        if (TextUtils.isEmpty(appId)){
            return false;
        }
        saveEditor.putString(APP_ID, appId);
        return saveEditor.commit();
    }

    public String getAppId() {
        if (saveInfo != null) {
            String appId = saveInfo.getString(APP_ID, ScanConfigs.CODE_APP_ID);
            return TextUtils.isEmpty(appId) ? ScanConfigs.CODE_APP_ID : appId;
        }
        return ScanConfigs.CODE_APP_ID;
    }

    public boolean putAppIdHistory() {
        if (saveInfo != null) {
            String appId = saveInfo.getString(APP_ID, ScanConfigs.CODE_APP_ID);
            Set<String> appIds = saveInfo.getStringSet(APP_ID_HISTORY, new ArraySet<>());
            if (!appIds.contains(appId)){
                appIds.add(appId);
                saveEditor.putStringSet(APP_ID_HISTORY, appIds);
                return saveEditor.commit();
            }
        }
        return false;
    }

    public Set<String> getAppIdHistory() {
        if (saveInfo != null) {
            Set<String> appIds = saveInfo.getStringSet(APP_ID_HISTORY, new ArraySet<>());
            appIds.add(ScanConfigs.CODE_APP_ID);
            return appIds;
        }
        return new ArraySet<>();
    }

    public boolean clearAppIdHistory() {
        if (saveEditor != null) {
            saveEditor.putStringSet(APP_ID_HISTORY, new ArraySet<>());
            return saveEditor.commit();
        }
        return false;
    }

    /**
     * 保存键盘高度
     *
     * @param currentInputMethod 当前输入法
     * @param height 软键盘高度
     */
    public void putKeyboardHeight(String currentInputMethod, int height) {
        saveEditor.putInt(currentInputMethod, height);
        saveEditor.commit();
    }

    public int getKeyboardHeight(String currentInputMethod) {
        if (saveInfo != null) {
            return saveInfo.getInt(currentInputMethod, 0);
        }
        return 0;
    }

    public boolean putIsFirst(boolean first) {
        saveEditor.putBoolean(IS_FIRST, first);
        return saveEditor.commit();
    }

    public boolean getFirst() {
        if (saveInfo != null) {
            return saveInfo.getBoolean(IS_FIRST, true);
        }
        return false;
    }

    public boolean putProtocolDialogStatus(boolean status) {
        saveEditor.putBoolean(IS_SHOW_PROTOCOL_DIALOG, status);
        return saveEditor.commit();
    }

    public boolean getProtocolDialogStatus() {
        if (saveInfo != null) {
            return saveInfo.getBoolean(IS_SHOW_PROTOCOL_DIALOG, false);
        }
        return false;
    }

    public boolean putLoginUserData(String data) {
        saveEditor.putString(LOGIN_USER_DATA, data);
        return saveEditor.commit();
    }

    public String getLoginUserData() {
        if (saveInfo != null) {
            return saveInfo.getString(LOGIN_USER_DATA, "");
        }
        return "";
    }

    public boolean putDNSConfig(String data) {
        saveEditor.putString(DNS_CONFIG, data);
        return saveEditor.commit();
    }

    public String getDnsConfig() {
        if (saveInfo != null) {
            return saveInfo.getString(DNS_CONFIG, "");
        }
        return "";
    }

    public boolean putAppLanguage(String val) {
        saveEditor.putString(APP_LANGUAGE, val);
        return saveEditor.commit();
    }

    public String getAppLanguage() {
        if (saveInfo != null) {
            return saveInfo.getString(APP_LANGUAGE, "");
        }
        return "";
    }
}
