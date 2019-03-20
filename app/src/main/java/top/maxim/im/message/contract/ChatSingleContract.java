
package top.maxim.im.message.contract;

/**
 * Description : 单聊 Created by Mango on 2018/11/05.
 */
public interface ChatSingleContract {

    /**
     * 单聊view
     */
    interface View extends ChatBaseContract.View {

    }

    /**
     * 单聊presenter
     */
    interface Presenter extends ChatBaseContract.Presenter {

        /**
         * 发送输入状态
         * @param extension
         */
        void sendInputStatus(String extension);
    }

    /**
     * 单聊model
     */
    interface Model extends ChatBaseContract.Model {

    }
}
