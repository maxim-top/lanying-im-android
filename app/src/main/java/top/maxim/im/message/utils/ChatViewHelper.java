
package top.maxim.im.message.utils;

import android.app.Activity;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;

import java.util.List;

import im.floo.floolib.BMXMessage;
import top.maxim.im.common.utils.WeakHandler;
import top.maxim.im.message.contract.ChatBaseContract;
import top.maxim.im.message.customviews.ChatRecyclerView;
import top.maxim.im.message.interfaces.ChatActionListener;

/**
 * Description : 聊天列表点击时间帮助类 Created by Mango on 2018/11/18.
 */
public class ChatViewHelper implements ChatActionListener {

    private static final int SCROLL_TO_BOTTOM = 10000;

    private static final int SCROLL_POSITION = 10001;

    private static final int ADD_CHAT_MESSAGE = 10002;

    private static final int ADD_CHAT_MESSAGES = 10003;

    private static final int ADD_CHAT_MESSAGE_TOP = 10004;

    private static final int ADD_CHAT_MESSAGES_TOP = 10005;

    private static final int UPDATE_CHAT_MESSAGES = 10006;

    private static final int DELETE_CHAT_MESSAGES = 10007;

    private static final int READ_ACK_CHAT_MESSAGES = 10008;

    private ChatRecyclerView mChatRecyclerView;

    private Activity mContext;

    private WeakHandler<Activity> mHandler;

    private LinearLayoutManager mLayoutManager;

    private MessageListHelper mMessageListHelper;

    private ChatBaseContract.Presenter mPresenter;

    public ChatViewHelper(Activity activity, ChatRecyclerView chatRecyclerView) {
        mContext = activity;
        mChatRecyclerView = chatRecyclerView;
        mChatRecyclerView.setItemListener(this);
        mMessageListHelper = mChatRecyclerView.getMessageListHelper();
        mLayoutManager = ((LinearLayoutManager)mChatRecyclerView.getLayoutManager());
        initHandler();
    }

    public void setChatPresenter(ChatBaseContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @SuppressWarnings("unchecked")
    private void initHandler() {
        mHandler = new WeakHandler(mContext) {
            @Override
            protected void handleWeakMessage(Message msg) {
                super.handleWeakMessage(msg);
                switch (msg.what) {
                    case SCROLL_TO_BOTTOM:
                        mChatRecyclerView.scrollToPosition(mMessageListHelper.getCount() - 1);
                        break;
                    case SCROLL_POSITION:
                        int position = msg.arg1;
                        mChatRecyclerView.scrollToPosition(position);
                        break;
                    case ADD_CHAT_MESSAGE:
                        BMXMessage addBean = (BMXMessage)msg.obj;
                        mMessageListHelper.add(addBean);
                        mChatRecyclerView.scrollToPosition(mMessageListHelper.getCount() - 1);
                        break;
                    case ADD_CHAT_MESSAGES:
                        List<BMXMessage> addBeans = (List<BMXMessage>)msg.obj;
                        mMessageListHelper.add(addBeans);
                        mChatRecyclerView.scrollToPosition(mMessageListHelper.getCount() - 1);
                        break;
                    case ADD_CHAT_MESSAGE_TOP:
                        BMXMessage topBean = (BMXMessage)msg.obj;
                        mMessageListHelper.add(0, topBean);
                        mChatRecyclerView.scrollToPosition(0);
                        break;
                    case ADD_CHAT_MESSAGES_TOP:
                        List<BMXMessage> topBeans = (List<BMXMessage>)msg.obj;
                        mMessageListHelper.add(0, topBeans);
                        mChatRecyclerView.scrollToPosition(topBeans.size() - 1);
                        break;
                    case UPDATE_CHAT_MESSAGES:
                        BMXMessage updateBean = (BMXMessage)msg.obj;
                        mMessageListHelper.updateMessage(updateBean);
                        break;
                    case DELETE_CHAT_MESSAGES:
                        long msgId = (long)msg.obj;
                        mMessageListHelper.deleteMessage(msgId);
                        break;
                    case READ_ACK_CHAT_MESSAGES:
                        boolean ackRead = (boolean) msg.obj;
                        mChatRecyclerView.showReadAck(ackRead);
                        mMessageListHelper.notifyAdapter();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * 添加消息到列表
     *
     * @param bean 消息
     */
    public void addChatMessage(BMXMessage bean) {
        Message message = Message.obtain();
        message.what = ADD_CHAT_MESSAGE;
        message.obj = bean;
        mHandler.sendMessage(message);
    }

    /**
     * 删除聊天消息
     */
    public void deleteMessage(long msgId) {
        Message msg = Message.obtain();
        msg.what = DELETE_CHAT_MESSAGES;
        msg.obj = msgId;
        mHandler.sendMessage(msg);
    }

    /**
     * 初始化聊天数据
     *
     * @param beans 聊天消息list
     */
    public void initMessages(List<BMXMessage> beans, boolean isHasData) {
        if (beans == null || beans.size() == 0) {
            return;
        }
        if (isHasData) {
            // 如果有更多数据增加消息加载
        }
        mMessageListHelper.add(beans);
        mHandler.sendEmptyMessage(SCROLL_TO_BOTTOM);
    }

    /**
     * 展示下拉聊天数据
     *
     * @param beans 聊天消息list
     * @param offset 添加到顶部数据的偏移量
     */
    public void pullDownMessages(List<BMXMessage> beans, int offset, boolean isHasData) {
        if (beans == null || beans.size() == 0) {
            return;
        }
        if (isHasData) {
            // 如果有更多数据增加消息加载
        }
        mMessageListHelper.add(0, beans);
        if (mMessageListHelper.getCount() == beans.size()) {
            // 如果当前没有数据 只有下拉的这一批数据 则直接滚动到底部
            mHandler.sendEmptyMessage(SCROLL_TO_BOTTOM);
        } else {
            mMessageListHelper.scrollSelection(beans.size(), offset);
        }
    }

    /**
     * 清除聊天记录
     */
    public void clearChatMessages() {
        mMessageListHelper.clear();
    }

    /**
     * 添加消息列表到列表
     *
     * @param beans 消息
     */
    public void addChatMessages(List<BMXMessage> beans) {
        Message message = Message.obtain();
        message.what = ADD_CHAT_MESSAGES;
        message.obj = beans;
        mHandler.sendMessage(message);
    }

    /**
     * 添加消息到列表顶部
     *
     * @param bean 消息
     */
    public void addTopChatMessage(BMXMessage bean) {
        Message message = Message.obtain();
        message.what = ADD_CHAT_MESSAGE_TOP;
        message.obj = bean;
        mHandler.sendMessage(message);
    }

    /**
     * 添加消息到列表顶部
     *
     * @param beans 消息
     */
    public void addTopChatMessages(List<BMXMessage> beans) {
        Message message = Message.obtain();
        message.what = ADD_CHAT_MESSAGES_TOP;
        message.obj = beans;
        mHandler.sendMessage(message);
    }

    /**
     * 更新某条消息
     *
     * @param bean 消息
     */
    public void updateChatMessage(BMXMessage bean) {
        if (bean == null) {
            return;
        }
        Message message = Message.obtain();
        message.what = UPDATE_CHAT_MESSAGES;
        message.obj = bean;
        mHandler.sendMessage(message);
    }

    /**
     * 获取第一条消息
     *
     * @return BMXMessage
     */
    public BMXMessage getFirstMessage() {
        if (mMessageListHelper != null) {
            return mMessageListHelper.getFirstData();
        }
        return null;
    }

    /**
     * 获取最后一条消息
     *
     * @return BMXMessage
     */
    public BMXMessage getLastMessage() {
        if (mMessageListHelper != null) {
            return mMessageListHelper.getLastData();
        }
        return null;
    }

    /**
     * 获取第一条消息
     *
     * @return BMXMessage
     */
    public long getFirstMsgId() {
        BMXMessage firstMessage = getFirstMessage();
        return firstMessage != null ? firstMessage.msgId() : -1;
    }

    /**
     * 获取聊天消息
     *
     * @param position 消息位置
     * @return CTNMessage
     */
    public BMXMessage getChatMessageByPosition(int position) {
        return mMessageListHelper != null ? mMessageListHelper.getDataByPosition(position) : null;
    }

    /**
     * 获取聊天当前页面第一条消息的position
     *
     * @return int
     */
    public int getFirstVisiblePosition() {
        return mMessageListHelper.getFirstVisiblePosition();
    }

    /**
     * 获取聊天当前页面最后一条消息的position
     *
     * @return int
     */
    public int getLastVisiblePosition() {
        return mMessageListHelper.getLastVisiblePosition();
    }

    /**
     * 取消所有的语音播放状态
     */
    public void cancelVoicePlay() {
        if (mMessageListHelper != null) {
            mMessageListHelper.cancelVoicePlay();
        }
    }

    /**
     * 滑动到指定位置
     *
     * @param position 位置
     */
    public void scrollMessagesPosition(int position) {
        if (position >= 0) {
            mMessageListHelper.scrollSelection(position, 0);
        }
    }

    /**
     * 滚动到底部
     */
    public void scrollBottom() {
        mHandler.sendEmptyMessageDelayed(SCROLL_TO_BOTTOM, 300);
    }
    
    public void updateListView() {
        if (mMessageListHelper != null) {
            mMessageListHelper.updateListView();
        }
    }

    @Override
    public void onItemFunc(BMXMessage bean) {
        if (mPresenter != null) {
            mPresenter.onItemFunc(bean);
        }
    }

    @Override
    public void onMessageLongClick(BMXMessage bean) {
        if (mPresenter != null) {
            mPresenter.onMessageLongClick(bean);
        }
    }

    @Override
    public void onMessageReadAck(BMXMessage bean) {
        if (mPresenter != null) {
            mPresenter.onMessageReadAck(bean);
        }
    }

    @Override
    public void onReSendMessage(BMXMessage bean) {
        if (mPresenter != null) {
            mPresenter.onReSendMessage(bean);
        }
    }

    @Override
    public void onGroupAck(BMXMessage bean) {
        if (mPresenter != null) {
            mPresenter.onGroupAck(bean);
        }
    }

    public void showReadAck(boolean showReadAck) {
        Message msg = Message.obtain();
        msg.what = READ_ACK_CHAT_MESSAGES;
        msg.obj = showReadAck;
        mHandler.sendMessage(msg);
    }
}
