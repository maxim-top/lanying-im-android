
package top.maxim.im.push.huawei;

import android.content.Context;
import android.text.TextUtils;

import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.push.HmsMessaging;

import im.floo.ThreadPool;
import top.maxim.im.push.IPushManager;
import top.maxim.im.push.PushClientMgr;

/**
 * Description : 华为push
 */
public class HWPushManager extends IPushManager {

    private String APP_ID;

    Context mContext;

    public HWPushManager(Context context) {
        mContext = context;
        String metaAppId = PushClientMgr.getPushAppId("HUAWEI_APPID");
        if (!TextUtils.isEmpty(metaAppId)) {
            APP_ID = metaAppId.substring(2);
        }
    }

    @Override
    public void register(Context context) {
        ThreadPool.exec(new Runnable() {
            @Override
            public void run() {
                try {
                    String token = HmsInstanceId.getInstance(context).getToken(APP_ID, HmsMessaging.DEFAULT_TOKEN_SCOPE);
                    if (TextUtils.isEmpty(token)) {
                        return;
                    }
                    // 设置push
                    PushClientMgr.setPushToken(token);
                }catch (ApiException e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void unRegister() {
        ThreadPool.exec(new Runnable() {
            @Override
            public void run() {
                try {
                    HmsInstanceId.getInstance(mContext).deleteToken(APP_ID, HmsMessaging.DEFAULT_TOKEN_SCOPE);
                }catch (ApiException e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void startReceiveNotification() {
        HmsMessaging.getInstance(mContext).turnOnPush();
    }

    @Override
    public void stopReceiveNotification() {
        HmsMessaging.getInstance(mContext).turnOffPush();
    }
}
