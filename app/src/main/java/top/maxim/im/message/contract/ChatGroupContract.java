
package top.maxim.im.message.contract;

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
         * 设置群聊ack
         * @param ack
         */
        void setGroupAck(boolean ack);
    }

    /**
     * 群聊model
     */
    interface Model extends ChatBaseContract.Model {

    }
}
