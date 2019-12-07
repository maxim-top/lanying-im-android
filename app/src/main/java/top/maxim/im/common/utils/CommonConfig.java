
package top.maxim.im.common.utils;

/**
 * Description : 消息常量 Created by Mango on 2018/11/11.
 */
public interface CommonConfig {

    String PROTOCOL_PRIVACY_URL = "https://www.maximtop.com/privacy";

    String PROTOCOL_TERMS_URL = "https://www.maximtop.com/terms/";

    String SOURCE_TO_WX = "sourceToWX";

    String SESSION_COUNT_ACTION = "sessionCountAction";

    String TAB_COUNT = "stabCount";

    String WX_LOGIN_ACTION = "wxLoginAction";

    String WX_OPEN_ID = "wxOpenId";

    /**
     * 进入微信来源
     */
    interface SourceToWX {

        // 正常登陆
        int TYPE_LOGIN = 0;

        // 验证码登陆
        int TYPE_LOGIN_VERIFY = 1;

        // 注册
        int TYPE_REGISTER = 2;

    }
}
