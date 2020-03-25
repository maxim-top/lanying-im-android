
package top.maxim.im.push;

import android.content.Context;

/**
 * Description : push
 */
public class EmptyPushManager extends IPushManager {


    public EmptyPushManager(Context context) {
    }

    @Override
    public void register(Context context) {
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
