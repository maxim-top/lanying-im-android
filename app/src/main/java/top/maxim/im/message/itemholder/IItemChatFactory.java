
package top.maxim.im.message.itemholder;

import im.floo.floolib.BMXMessage;

/**
 * Description : 聊天item Created by Mango on 2018/11/18.
 */
public interface IItemChatFactory extends IItemFactory<BMXMessage> {

    void showChatTime(boolean isShowTime);

    /**
     * view进入window
     */
    void onViewAttach();

    /**
     * view移出window
     */
    void onViewDetach();
}
