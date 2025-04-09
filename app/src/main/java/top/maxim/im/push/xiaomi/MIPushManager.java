
package top.maxim.im.push.xiaomi;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;
import android.text.TextUtils;

import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.List;

import top.maxim.im.push.IPushManager;
import top.maxim.im.push.PushClientMgr;

/**
 * Description : 小米push
 */
public class MIPushManager extends IPushManager {

    private String APP_ID;

    private String APP_KEY;

    Context mContext;

    public MIPushManager(Context context) {
        mContext = context;
        String metaAppId = PushClientMgr.getPushAppId("XIAOMI_APPID");
        String metaAppKey = PushClientMgr.getPushAppId("XIAOMI_APP_KEY");
        if (!TextUtils.isEmpty(metaAppId)) {
            APP_ID = metaAppId.substring(2);
        }
        if (!TextUtils.isEmpty(metaAppKey)) {
            APP_KEY = metaAppKey.substring(2);
        }
    }

    private boolean isMainProcess(Context context) {
        ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = context.getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void register(Context context) {
        if (isMainProcess(context) && !TextUtils.isEmpty(APP_ID) && !TextUtils.isEmpty(APP_KEY)) {
            MiPushClient.registerPush(context, APP_ID, APP_KEY);
        }
    }

    @Override
    public void unRegister() {
        MiPushClient.unregisterPush(mContext);
    }

    @Override
    public void startReceiveNotification() {
        MiPushClient.enablePush(mContext);
    }

    @Override
    public void stopReceiveNotification() {
        MiPushClient.disablePush(mContext);
    }
}
