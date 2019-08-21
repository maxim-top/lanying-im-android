
package top.maxim.im.bmxmanager;

import im.floo.floolib.BMXChatService;
import im.floo.floolib.BMXChatServiceListener;
import im.floo.floolib.BMXConversation;
import im.floo.floolib.BMXConversationList;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageList;
import im.floo.floolib.BMXMessageListList;

/**
 * Description : chat Created by Mango on 2018/12/2.
 */
public class ChatManager extends BaseManager {

    private static final String TAG = ChatManager.class.getSimpleName();

    private static final ChatManager sInstance = new ChatManager();

    private BMXChatService mService;

    public static ChatManager getInstance() {
        return sInstance;
    }

    private ChatManager() {
        mService = bmxClient.getChatService();
    }

    /**
     * 发送消息，消息状态变化会通过listener通知
     **/
    public void sendMessage(BMXMessage msg) {
        mService.sendMessage(msg);
    }

    /**
     * 重新发送消息，消息状态变化会通过listener通知
     **/
    public void resendMessage(BMXMessage msg) {
        mService.resendMessage(msg);
    }

    /**
     * 撤回消息，消息状态变化会通过listener通知
     **/
    public void recallMessage(BMXMessage msg) {
        mService.recallMessage(msg);
    }

    /**
     * 转发消息列表
     */
    public BMXErrorCode forwardMessage(BMXMessageList list, BMXConversation to, BMXMessage newMsg) {
        return mService.forwardMessage(list, to, newMsg);
    }

    /**
     * 转发单个消息
     */
    public void forwardMessage(BMXMessage msg) {
        mService.forwardMessage(msg);
    }

    /**
     * 设置所有消息已读
     * @param msg 最后一条消息
     */
    public void readAllMessage(BMXMessage msg) {
        mService.readAllMessage(msg);
    }

    public void removeMessage(BMXMessage msg, boolean synchronize) {
        mService.removeMessage(msg, synchronize);
    }

    public void removeMessage(BMXMessage msg) {
        mService.removeMessage(msg);
    }

    /**
     * 发送已读回执
     **/
    public void ackMessage(BMXMessage msg) {
        mService.ackMessage(msg);
    }

    /**
     * 设置未读
     **/
    public void readCancel(BMXMessage msg) {
        mService.readCancel(msg);
    }

    /**
     * 下载缩略图，下载状态变化和进度通过listener通知
     **/
    public void downloadThumbnail(BMXMessage msg) {
        mService.downloadThumbnail(msg);
    }

    /**
     * 下载附件，下载状态变化和进度通过listener通知
     **/
    public void downloadAttachment(BMXMessage msg) {
        mService.downloadAttachment(msg);
    }

    /**
     * 插入消息
     **/
    public BMXErrorCode insertMessages(BMXMessageList list) {
        return mService.insertMessages(list);
    }

    /**
     * 读取一条消息
     **/
    public BMXMessage getMessage(long msgId) {
        return mService.getMessage(msgId);
    }

    /**
     * 删除会话
     **/
    public void deleteConversation(long conversationId, Boolean sync) {
        mService.deleteConversation(conversationId, sync);
    }

    /**
     * 打开一个会话
     **/
    public BMXConversation openConversation(long conversationId, BMXConversation.Type type,
            boolean createIfNotExist) {
        return mService.openConversation(conversationId, type, createIfNotExist);
    }

    /**
     * 获取所有会话
     **/
    public BMXConversationList getAllConversations() {
        return mService.getAllConversations();
    }

    /**
     * 拉取历史消息
     **/
    public BMXErrorCode retrieveHistoryMessages(BMXConversation conversation, long refMsgId,
            long size, BMXMessageList result) {
        return mService.retrieveHistoryMessages(conversation, refMsgId, size, result);
    }

    public BMXErrorCode searchMessages(String keywords, long refTime, long size,
            BMXMessageListList result, BMXConversation.Direction arg4) {
        return mService.searchMessages(keywords, refTime, size, result, arg4);
    }

    public BMXErrorCode searchMessages(String keywords, long refTime, long size,
            BMXMessageListList result) {
        return mService.searchMessages(keywords, refTime, size, result);
    }

    /**
     * 添加聊天监听者
     **/
    public void addChatListener(BMXChatServiceListener listener) {
        mService.addChatListener(listener);
    }

    /**
     * 移除聊天监听者
     **/
    public void removeChatListener(BMXChatServiceListener listener) {
        mService.removeChatListener(listener);
    }
}
