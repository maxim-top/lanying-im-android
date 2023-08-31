
package top.maxim.im.message.itemholder;

import android.view.View;

/**
 * Description : 聊天页面holder Created by Mango on 2018/11/18.
 */
public class BaseChatHolder extends BaseHolder {

    public BaseChatHolder(View view, IItemChatFactory chatPanel) {
        super(view, chatPanel);
    }

    public void showChatExtra(boolean showTime, boolean showReadAck) {
        if (mFactory != null) {
            ((IItemChatFactory)mFactory).showChatExtra(showTime, showReadAck);
        }
    }

    /**
     * view进入window
     */
    public void onViewAttach() {
        if (mFactory != null) {
            ((IItemChatFactory)mFactory).onViewAttach();
        }
    }

    /**
     * view移出window
     */
    public void onViewDetach() {
        if (mFactory != null) {
            ((IItemChatFactory)mFactory).onViewDetach();
        }
    }

    /**
     * view被回收
     */
    public void onViewRecycled() {
        if (mFactory != null) {
            ((IItemChatFactory)mFactory).onViewRecycled();
        }
    }
}
