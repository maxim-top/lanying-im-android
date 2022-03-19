
package top.maxim.im.message.contract;

import im.floo.floolib.BMXGroup;

/**
 * Description : 群聊 Created by Mango on 2018/11/05.
 */
public interface ChatGroupContract {

    /**
     * 群聊view
     */
    interface View extends ChatBaseContract.View {

        /**
         * 展示加入群聊弹出框
         */
        void showJoinGroupDialog(BMXGroup group);

        void showLoading(boolean cancel);

        void cancelLoading();
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

        /**
         * 加入群聊
         */
        void joinGroup(BMXGroup group, String reason);
    }

    /**
     * 群聊model
     */
    interface Model extends ChatBaseContract.Model {

    }
}
