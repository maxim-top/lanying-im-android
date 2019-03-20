
package top.maxim.im.sdk.utils;

/**
 * Description : 定义IM和通知中用到的类型（依赖于服务端消息类型，不得在此类中定义其他类型
 */
public interface MsgConstants {

    /**
     * 消息发送ack结果码 -1,失败, 0, ACK消息成功 1, ACK消息违禁2, from_client与名片不匹配 3,
     * to_client与名片不匹配 4, 消息超长 5, 服务器错误 6,消息格式错误 7, 用户不在群聊中 8, 名片被禁止沟通能力
     */
    interface MessageSendACK {

        /* 失败 */
        int ACK_FAIL = -1;

        /* 成功 */
        int ACK_SUCCESS = 0;

        /* 违禁 */
        int ACK_VIOLATE = 1;

        /* from不匹配 */
        int ACK_FROM_ERROR = 2;

        /* to不匹配 */
        int ACK_TO_ERROR = 3;

        /* 消息过长 */
        int ACK_SUPER = 4;

        /* 服务器错误 */
        int ACK_SERVER_ERROR = 5;

        /* 消息格式错误 */
        int ACK_MSG_ERROR = 6;

        /* 用户不在群聊 */
        int ACK_NOT_IN_GROUP = 7;

        /* 名片被禁止 */
        int ACK_CARD_FORBIDDEN = 8;
    }

}
