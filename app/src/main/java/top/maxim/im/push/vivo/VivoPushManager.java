
package top.maxim.im.push.vivo;

import android.content.Context;
import android.text.TextUtils;

import com.vivo.push.IPushActionListener;
import com.vivo.push.PushClient;
import com.vivo.push.ups.TokenResult;
import com.vivo.push.ups.UPSRegisterCallback;
import com.vivo.push.ups.VUpsManager;

import top.maxim.im.push.IPushManager;
import top.maxim.im.push.PushClientMgr;

/**
 * Description : vivo push
 */
public class VivoPushManager extends IPushManager {

    private String APP_ID;

    private String APP_KEY;

    private String APP_SECRETE;

    Context mContext;

    public VivoPushManager(Context context) {
        mContext = context;
        String metaAppId = PushClientMgr.getPushAppId("VIVO_APPID");
        String metaAppKey = PushClientMgr.getPushAppId("VIVO_APP_KEY");
        String metaAppSecret = PushClientMgr.getPushAppId("VIVO_APP_SECRET");
        if (!TextUtils.isEmpty(metaAppId)) {
            APP_ID = metaAppId.substring(2);
        }
        if (!TextUtils.isEmpty(metaAppKey)) {
            APP_KEY = metaAppKey.substring(2);
        }
        if (!TextUtils.isEmpty(metaAppSecret)) {
            APP_SECRETE = metaAppSecret.substring(2);
        }
        PushClient.getInstance(context).turnOnPush(new IPushActionListener() {
            @Override
            public void onStateChanged(int i) {

            }
        });
    }

    @Override
    public void register(Context context) {
        VUpsManager.getInstance().registerToken(mContext, APP_ID, APP_KEY, APP_SECRETE,
                new UPSRegisterCallback() {
                    @Override
                    public void onResult(TokenResult tokenResult) {
                        if (tokenResult != null && tokenResult.getReturnCode() == 0) {
                            PushClientMgr.setPushToken(tokenResult.getToken());
                        }
                    }
                });
    }

    @Override
    public void unRegister() {
        VUpsManager.getInstance().unRegisterToken(mContext, new UPSRegisterCallback() {
            @Override
            public void onResult(TokenResult tokenResult) {

            }
        });
    }

    @Override
    public void startReceiveNotification() {
        VUpsManager.getInstance().turnOnPush(mContext, tokenResult -> {
            
        });
    }

    @Override
    public void stopReceiveNotification() {
        VUpsManager.getInstance().turnOffPush(mContext, tokenResult -> {

        });
    }
}
