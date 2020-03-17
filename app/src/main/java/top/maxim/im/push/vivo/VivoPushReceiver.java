
package top.maxim.im.push.vivo;

import android.content.Context;

import com.vivo.push.model.UPSNotificationMessage;
import com.vivo.push.sdk.OpenClientPushMessageReceiver;

import top.maxim.im.login.view.WelcomeActivity;

/**
 * Description : vivo push
 */
public class VivoPushReceiver extends OpenClientPushMessageReceiver {

    @Override
    public void onNotificationMessageClicked(Context context, UPSNotificationMessage upsNotificationMessage) {
        WelcomeActivity.openWelcome(context);
    }

    @Override
    public void onReceiveRegId(Context context, String s) {

    }
}
