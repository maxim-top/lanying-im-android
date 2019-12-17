
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

    String WX_UN_BIND_ACTION = "wxUnbindAction";

    String WX_OPEN_ID = "wxOpenId";

    String VERIFY_TYPE = "verifyType";

    String PHONE = "phone";

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

        // 绑定
        int TYPE_BIND = 3;

    }

    /**
     * 验证密码
     */
    interface VerifyType {

        // 微信
        int TYPE_WX = 0;

        // 手机号
        int TYPE_PHONE = 1;

        // 手机号验证码
        int TYPE_PHONE_VERIFY = 2;

    }
}
