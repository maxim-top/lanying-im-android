
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
        String metaHost = PushClientMgr.getPushAppId("AGENT_HOST");
        if (TextUtils.isEmpty(metaHost)) {
            return;
        }

        NBSAppAgent.setLicenseKey(metaAppKey).setRedirectHost(metaHost).withCrashReportEnabled(true)
                .enableLogging(true).setStartOption(7).start(context);
    }

}
