
package top.maxim.im.push.meizu;

import android.content.Context;
import android.text.TextUtils;

import com.meizu.cloud.pushsdk.PushManager;

import top.maxim.im.push.IPushManager;
import top.maxim.im.push.PushClientMgr;

/**
 * Description : 魅族push
 */
public class MZPushManager extends IPushManager {

    private String APP_ID;

    private String APP_KEY;

    Context mContext;

    public MZPushManager(Context context) {
        mContext = context;
        String metaAppId = PushClientMgr.getPushAppId("MEIZU_APPID");
        String metaAppKey = PushClientMgr.getPushAppId("MEIZU_APP_KEY");
        if (!TextUtils.isEmpty(metaAppId)) {
            APP_ID = metaAppId.substring(2);
        }
        if (!TextUtils.isEmpty(metaAppKey)) {
            APP_KEY = metaAppKey.substring(2);
        }
    }

    @Override
    public void register(Context context) {
        String pushId = PushManager.getPushId(mContext);
        if (pushId != null && pushId.length() != 0) {
            startReceiveNotification();
            PushClientMgr.setPushToken(pushId);
        } else {
            PushManager.register(mContext, APP_ID, APP_KEY);
        }
    }

    @Override
    public void unRegister() {
        PushManager.unRegister(mContext, APP_ID, APP_KEY);
    }

    @Override
    public void startReceiveNotification() {
        PushManager.switchPush(mContext, APP_ID, APP_KEY, PushManager.getPushId(mContext), 0, true);
    }

    @Override
    public void stopReceiveNotification() {
        PushManager.switchPush(mContext, APP_ID, APP_KEY, PushManager.getPushId(mContext), 0,
                false);
    }
}
