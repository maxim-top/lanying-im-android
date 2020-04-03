
package top.maxim.im.push.oppo;

import android.content.Context;
import android.text.TextUtils;

import com.heytap.mcssdk.PushManager;
import com.heytap.mcssdk.callback.PushAdapter;
import com.heytap.mcssdk.mode.ErrorCode;

import top.maxim.im.push.IPushManager;
import top.maxim.im.push.PushClientMgr;

/**
 * Description : oppo push
 */
public class OppoPushManager extends IPushManager {

    private String APP_ID;

    private String APP_KEY;

    private String APP_SECRETE;

    Context mContext;

    public OppoPushManager(Context context) {
        mContext = context;
        String metaAppId = PushClientMgr.getPushAppId("OPPO_APPID");
        String metaAppKey = PushClientMgr.getPushAppId("OPPO_APP_KEY");
        String metaAppSecret = PushClientMgr.getPushAppId("OPPO_APP_SECRET");
        if (!TextUtils.isEmpty(metaAppId)) {
            APP_ID = metaAppId.substring(2);
        }
        if (!TextUtils.isEmpty(metaAppKey)) {
            APP_KEY = metaAppKey.substring(2);
        }
        if (!TextUtils.isEmpty(metaAppSecret)) {
            APP_SECRETE = metaAppSecret.substring(2);
        }
    }

    @Override
    public void register(Context context) {
        String token = PushManager.getInstance().getRegisterID();
        if (!TextUtils.isEmpty(token)) {
            PushClientMgr.setPushToken(token);
            PushManager.getInstance().resumePush();
            return;
        }
        PushManager.getInstance().register(mContext, APP_KEY, APP_SECRETE, new PushAdapter() {
            @Override
            public void onRegister(int i, String s) {
                super.onRegister(i, s);
                if (i == ErrorCode.SUCCESS) {
                    PushClientMgr.setPushToken(s);
                }
            }

            @Override
            public void onUnRegister(int i) {
                super.onUnRegister(i);
            }
        });
    }

    @Override
    public void unRegister() {
        PushManager.getInstance().unRegister();
    }

    @Override
    public void startReceiveNotification() {
        PushManager.getInstance().pausePush();
    }

    @Override
    public void stopReceiveNotification() {
        PushManager.getInstance().resumePush();
    }
}
