
package top.maxim.im.common.utils;

import android.app.Application;
import android.text.TextUtils;

import com.networkbench.agent.impl.NBSAppAgent;

import top.maxim.im.push.PushClientMgr;

/**
 * 听云 初始化
 */
public class AgentTask {

    private static final AgentTask sInstance = new AgentTask();

    private AgentTask() {

    }

    public static AgentTask get() {
        return sInstance;
    }

    public void init(Application context) {
        String metaAppKey = PushClientMgr.getPushAppId("AGENT_APP_KEY");
        if (TextUtils.isEmpty(metaAppKey)) {
            return;
        }
        NBSAppAgent.setLicenseKey(metaAppKey).withCrashReportEnabled(true)
                .withLocationServiceEnabled(false).start(context);
    }

}
