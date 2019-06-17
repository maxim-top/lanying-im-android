
package top.maxim.im.message.interfaces;


import im.floo.floolib.BMXMessage;

/**
 * Description : 聊天操作监听
 * Created by Mango on 2018/11/18.
 */
public interface ChatActionListener {

    /**
     * 消息体点击
     * @param bean 消息体
     */
    void onItemFunc(BMXMessage bean);

    /**
     * 长按
     * @param bean 消息体
     */
    void onMessageLongClick(BMXMessage bean);

    /**
     * 消息已读回执
     * @param bean 消息体
     */
    void onMessageReadAck(BMXMessage bean);

    /**
     * 消息重发
     * @param bean 消息体
     */
    void onReSendMessage(BMXMessage bean);
}
