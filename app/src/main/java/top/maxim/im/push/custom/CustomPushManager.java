
package top.maxim.im.push.custom;

import android.content.Context;

import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.push.IPushManager;
import top.maxim.im.push.PushClientMgr;

/**
 * Description : 自定义tokenpush
 */
public class CustomPushManager extends IPushManager {

    public CustomPushManager() {

    }

    @Override
    public void register(Context context) {
        String token = SharePreferenceUtils.getInstance().getPushToken();
        PushClientMgr.setPushToken(token);
    }

    @Override
    public void unRegister() {
    }

    @Override
    public void startReceiveNotification() {
    }

    @Override
    public void stopReceiveNotification() {
    }
}
