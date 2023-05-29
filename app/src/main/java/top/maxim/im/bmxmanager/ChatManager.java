
package top.maxim.im.bmxmanager;

import im.floo.BMXCallBack;
import im.floo.BMXDataCallBack;
import im.floo.floolib.BMXChatManager;
import im.floo.floolib.BMXChatServiceListener;
import im.floo.floolib.BMXConversation;
import im.floo.floolib.BMXConversationList;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageList;
import im.floo.floolib.BMXMessageListList;
import im.floo.floolib.ListOfLongLong;

/**
 * Description : chat Created by Mango on 2018/12/2.
 */
public class ChatManager extends BaseManager {

    private static final String TAG = ChatManager.class.getSimpleName();

    private static final ChatManager sInstance = new ChatManager();

    private BMXChatManager mService;

    public static ChatManager getInstance() {
        return sInstance;
    }

    private ChatManager() {
        if (bmxClient == null){
            initBMXSDK();
        }
        mService = bmxClient.getChatManager();
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
    public void forwardMessage(BMXMessageList list, BMXConversation to, BMXMessage newMsg, BMXCallBack callBack) {
        mService.forwardMessage(list, to, newMsg, callBack);
    }

    /**
     * 转发单个消息
     */
    public void forwardMessage(BMXMessage msg) {
        mService.forwardMessage(msg);
    }

    /**
     * 设置所有消息已读
     * 
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
     * 取消下载附件
     **/
    public void cancelDownloadAttachment(BMXMessage msg) {
        mService.cancelDownloadAttachment(msg);
    }

    /**
     * 取消下载附件
     **/
    public void cancelUploadAttachment(BMXMessage msg) {
//        mService.cancelUploadAttachment(msg);
    }

    /**
     * 获取正在上传或下载的任务总数
     **/
    public int transferingNum() {
        return mService.transferingNum();
    }

    /**
     * 插入消息
     **/
    public void insertMessages(BMXMessageList list, BMXCallBack callBack) {
        mService.insertMessages(list, callBack);
    }

    /**
     * 读取一条消息
     **/
    public void getMessage(long msgId, BMXDataCallBack<BMXMessage> callBack) {
        mService.getMessage(msgId, callBack);
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
    public void openConversation(long conversationId, BMXConversation.Type type,
            boolean createIfNotExist, BMXDataCallBack<BMXConversation> callBack) {
        mService.openConversation(conversationId, type, createIfNotExist, callBack);
    }

    /**
     * 获取所有会话
     **/
    public void getAllConversations(BMXDataCallBack<BMXConversationList> callBack) {
        mService.getAllConversations(callBack);
    }

    /**
     * 获取所有会话未读数
     **/
    public void getAllConversationsUnreadCount(BMXDataCallBack<Integer> callBack) {
        mService.getAllConversationsUnreadCount(callBack);
    }

    /**
     * 拉取历史消息
     **/
    public void retrieveHistoryMessages(BMXConversation conversation, long refMsgId, long size, BMXDataCallBack<BMXMessageList> callBack) {
        mService.retrieveHistoryMessages(conversation, refMsgId, size, callBack);
    }

    public void searchMessages(String keywords, long refTime, long size,
            BMXConversation.Direction arg4, BMXDataCallBack<BMXMessageListList> callBack) {
        mService.searchMessages(keywords, refTime, size, arg4, callBack);
    }

    public void searchMessages(String keywords, long refTime, long size,
            BMXDataCallBack<BMXMessageListList> callBack) {
        mService.searchMessages(keywords, refTime, size, callBack);
    }

    public void getGroupAckMessageUserIdList(BMXMessage msg,
            BMXDataCallBack<ListOfLongLong> callBack) {
        mService.getGroupAckMessageUserIdList(msg, callBack);
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
