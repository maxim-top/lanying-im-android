
package top.maxim.im.push.huawei;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.huawei.hms.support.api.push.PushReceiver;

import top.maxim.im.push.PushClientMgr;

/**
 * Description : 华为push广播
 */
public class HuaWeiPushReceiver extends PushReceiver {

    @Override
    public void onToken(Context context, String token, Bundle extras) {
        super.onToken(context, token, extras);
        if (TextUtils.isEmpty(token)) {
            return;
        }
        // 设置push
        PushClientMgr.setPushToken(token);
    }

    @Override
    public void onEvent(Context context, Event event, Bundle extras) {
        if (Event.NOTIFICATION_OPENED.equals(event) || Event.NOTIFICATION_CLICK_BTN.equals(event)) {
            int notifyId = extras.getInt(BOUND_KEY.pushNotifyId, 0);
            if (0 != notifyId) {
                NotificationManager manager = (NotificationManager)context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                if (manager != null) {
                    manager.cancel(notifyId);
                }
            }
        }
        super.onEvent(context, event, extras);
    }
}
