
package top.maxim.im.push.huawei;

import android.text.TextUtils;

import com.huawei.hms.push.HmsMessageService;

import top.maxim.im.push.PushClientMgr;

/**
 * Description : 华为push广播
 */
public class HuaWeiPushService extends HmsMessageService {

    private static final String TAG = "HuaWeiPushReceiver";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        if (TextUtils.isEmpty(s)) {
            return;
        }
        // 设置push
        PushClientMgr.setPushToken(s);
    }

    @Override
    public void onTokenError(Exception e) {
        super.onTokenError(e);
    }
}
