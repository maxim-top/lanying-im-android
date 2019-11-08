
package top.maxim.im.sdk.utils;

/**
 * Description : 定义IM和通知中用到的类型（依赖于服务端消息类型，不得在此类中定义其他类型
 */
public interface MsgConstants {

    /*
     * 通知渠道号
     */

    String NOTIFICATION_CHANNEL_PUBLIC = "公开渠道";

    String NOTIFICATION_CHANNEL_TOPIC = "订阅渠道";

    String NOTIFICATION_CHANNEL_PRIVATE = "私有渠道";

    interface ChannelImportance {
        int PUBLIC = 1;

        int TOPIC = PUBLIC + 1;

        int PRIVATE = +TOPIC + 1;
    }

}
