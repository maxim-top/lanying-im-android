package top.maxim.im.push.huawei.agent.push;

import android.os.Handler;
import android.os.Looper;

import com.huawei.hms.api.HuaweiApiClient;
import com.huawei.hms.support.api.push.HuaweiPush;

import top.maxim.im.push.huawei.agent.HMSAgent;
import top.maxim.im.push.huawei.agent.common.ApiClientMgr;
import top.maxim.im.push.huawei.agent.common.BaseApiAgent;
import top.maxim.im.push.huawei.agent.common.CallbackCodeRunnable;
import top.maxim.im.push.huawei.agent.common.HMSAgentLog;
import top.maxim.im.push.huawei.agent.common.StrUtils;
import top.maxim.im.push.huawei.agent.common.ThreadUtil;
import top.maxim.im.push.huawei.agent.push.handler.EnableReceiveNotifyMsgHandler;

/**
 * 打开自呈现消息开关的接口。
 */
public class EnableReceiveNotifyMsgApi extends BaseApiAgent {

    /**
     * 是否打开开关
     */
    boolean enable;

    /**
     * 调用接口回调
     */
    private EnableReceiveNotifyMsgHandler handler;

    /**
     * HuaweiApiClient 连接结果回调
     *
     * @param rst    结果码
     * @param client HuaweiApiClient 实例
     */
    @Override
    public void onConnect(final int rst, final HuaweiApiClient client) {
        //需要在子线程中执行开关的操作
        ThreadUtil.INST.excute(new Runnable() {
            @Override
            public void run() {
                if (client == null || !ApiClientMgr.INST.isConnect(client)) {
                    HMSAgentLog.e("client not connted");
                    onEnableReceiveNotifyMsgResult(rst);
                } else {
                    // 开启/关闭自呈现消息
                    HuaweiPush.HuaweiPushApi.enableReceiveNotifyMsg(client, enable);
                    onEnableReceiveNotifyMsgResult(HMSAgent.AgentResultCode.HMSAGENT_SUCCESS);
                }
            }
        });
    }

    void onEnableReceiveNotifyMsgResult(int rstCode) {
        HMSAgentLog.i("enableReceiveNotifyMsg:callback=" + StrUtils.objDesc(handler) +" retCode=" + rstCode);
        if (handler != null) {
            new Handler(Looper.getMainLooper()).post(new CallbackCodeRunnable(handler, rstCode));
            handler = null;
        }
    }

    /**
     * 打开/关闭自呈现消息
     * @param enable 打开/关闭
     */
    public void enableReceiveNotifyMsg(boolean enable, EnableReceiveNotifyMsgHandler handler) {
        HMSAgentLog.i("enableReceiveNotifyMsg:enable=" + enable + " handler=" + StrUtils.objDesc(handler));
        this.enable = enable;
        this.handler = handler;
        connect();
    }
}