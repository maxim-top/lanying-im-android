
package top.maxim.im.common.utils;

/**
 * Description : 消息常量 Created by Mango on 2018/11/11.
 */
public interface CommonConfig {

    String SOURCE_TO_WX = "sourceToWX";

    String SESSION_COUNT_ACTION = "sessionCountAction";

    String TAB_COUNT = "stabCount";

    String WX_LOGIN_ACTION = "wxLoginAction";

    String WX_UN_BIND_ACTION = "wxUnbindAction";

    String MOBILE_BIND_ACTION = "mobilebindAction";

    String REAL_NAME_VERIFY_ACTION = "realNameVerifyAction";

    String WX_OPEN_ID = "wxOpenId";

    String VERIFY_TYPE = "verifyType";

    String VERIFY_OPERATE_TYPE = "verifyOperateType";

    String PHONE = "phone";
    
    String CHANGE_APP_ID_ACTION = "changeAppIdAction";

    String CHANGE_APP_ID = "changeAppId";

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

        // 微信验证
        int TYPE_WX = 0;

        // 密码验证
        int TYPE_PWD = 1;

        // 手机号验证码验证码
        int TYPE_PHONE_CAPTCHA = 2;

    }

    /**
     * 验证之后操作类型
     */
    interface VerifyOperateType {

        // 绑定手机号
        int TYPE_BIND_MOBILE = 0;

        // 修改密码
        int TYPE_CHANGE_PWD = 1;

    }
}
