
package top.maxim.im.push.huawei;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import top.maxim.im.push.IPushManager;
import top.maxim.im.push.huawei.agent.HMSAgent;
import top.maxim.im.push.huawei.agent.common.handler.ConnectHandler;
import top.maxim.im.push.huawei.agent.push.handler.EnableReceiveNotifyMsgHandler;
import top.maxim.im.push.huawei.agent.push.handler.GetTokenHandler;

/**
 * Description : 华为push
 */
public class HWPushManager extends IPushManager {

    public HWPushManager(Application context) {
        HMSAgent.init(context);
    }

    @Override
    public void register(Context context) {
        HMSAgent.connect(context, new ConnectHandler() {
            @Override
            public void onConnect(int rst) {
                getToken();
            }
        });
    }

    private void getToken() {
        HMSAgent.Push.getToken(new GetTokenHandler() {
            @Override
            public void onResult(int rst) {
            }
        });
    }

    @Override
    public void unRegister() {
        HMSAgent.destroy();
    }

    @Override
    public void startReceiveNotification() {
        HMSAgent.Push.enableReceiveNotifyMsg(true, new EnableReceiveNotifyMsgHandler() {
            @Override
            public void onResult(int rst) {

            }
        });
    }

    @Override
    public void stopReceiveNotification() {
        HMSAgent.Push.enableReceiveNotifyMsg(false, new EnableReceiveNotifyMsgHandler() {
            @Override
            public void onResult(int rst) {

            }
        });
    }
}
