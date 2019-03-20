
package top.maxim.im.message.itemholder;

import android.view.View;

/**
 * Description : 聊天页面holder Created by Mango on 2018/11/18.
 */
public class BaseChatHolder extends BaseHolder {

    public BaseChatHolder(View view, IItemChatFactory chatPanel) {
        super(view, chatPanel);
    }

    public void showChatTime(boolean showTime) {
        if (mFactory != null) {
            ((IItemChatFactory)mFactory).showChatTime(showTime);
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
}
