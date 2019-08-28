
package top.maxim.im.message.customviews;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import top.maxim.im.message.adapter.ChatMessageAdapter;
import top.maxim.im.message.interfaces.ChatActionListener;
import top.maxim.im.message.utils.MessageListHelper;

/**
 * Description : 聊天页面recyclerView Created by Mango on 2018/11/18.
 */
public class ChatRecyclerView extends RecyclerView {

    private MessageListHelper mMessageListHelper;

    private ChatMessageAdapter mAdapter;

    public ChatRecyclerView(Context context) {
        this(context, null);
    }

    public ChatRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mAdapter = new ChatMessageAdapter(context);
        setLayoutManager(new LinearLayoutManager(context));
        mMessageListHelper = new MessageListHelper(this, mAdapter);
        mAdapter.setChatMessages(mMessageListHelper.getList());
        setAdapter(mAdapter);
    }

    /**
     * 设置RecyclerView的holder监听
     *
     * @param listener 聊天item监听
     */
    public void setItemListener(ChatActionListener listener) {
        mAdapter.setActionListener(listener);
    }

    public void showReadAck(boolean showReadAck) {
        mAdapter.showReadAck(showReadAck);
    }

    public MessageListHelper getMessageListHelper() {
        return mMessageListHelper;
    }
}
