
package top.maxim.im.message.contract;

import im.floo.floolib.BMXMessage;

/**
 * Description : 群聊 Created by Mango on 2018/11/05.
 */
public interface ChatGroupContract {

    /**
     * 群聊view
     */
    interface View extends ChatBaseContract.View {

    }

    /**
     * 群聊presenter
     */
    interface Presenter extends ChatBaseContract.Presenter {

        /* 群成员@*/
        void onChatAtMember();

        /**
         * 设置群聊已读
         * 
         * @param message 最后一条消息
         */
        void readAllMessage(BMXMessage message);
    }

    /**
     * 群聊model
     */
    interface Model extends ChatBaseContract.Model {

    }
}
