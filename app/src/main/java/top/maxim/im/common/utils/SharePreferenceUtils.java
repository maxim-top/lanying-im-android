
package top.maxim.im.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import top.maxim.im.scan.config.ScanConfigs;

/**
 * Description : sp缓存工具类 Created by Mango on 2018/11/06.
 */

public class SharePreferenceUtils {

    private static final String SH_DATA_NAME = "maxIM_user_data";

    private static String IS_LOGIN = "isLogin";

    private static String USER_ID = "user_id";

    private static String USER_NAME = "user_name";

    private static String USER_PWD = "user_pwd";

    private static String TOKEN = "token";

    private static String DEVICE_ID = "device_id";

    private static String PUSH_TOKEN_DEV_TYPE = "pushTokenDevType";

    private static String PUSH_TOKEN = "pushToken";

    private static String DEV_TIPS = "devTips";

    private static String CUSTOM_DNS = "customDns";

    private static String APP_ID = "app_id";

    private static String IS_FIRST = "isFirst";

    private static String IS_SHOW_PROTOCOL_DIALOG = "isShowProtocolDialog";

    private static String LOGIN_USER_DATA = "loginUserData";

    private static String DNS_CONFIG = "dnsConfig";

    private static volatile SharePreferenceUtils instance;

    private static SharedPreferences.Editor saveEditor;

    private static SharedPreferences saveInfo;

    public static SharePreferenceUtils getInstance() {
        if (instance == null) {
            synchronized (SharePreferenceUtils.class) {
                if (instance == null) {
                    instance = new SharePreferenceUtils();
                    saveInfo = AppContextUtils.getAppContext().getSharedPreferences(SH_DATA_NAME,
                            Context.MODE_PRIVATE);
                    saveEditor = saveInfo.edit();
                }
            }
        }
        return instance;
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

    public String getUserPwd() {
        if (saveInfo != null) {
            return saveInfo.getString(USER_PWD, null);
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
}
