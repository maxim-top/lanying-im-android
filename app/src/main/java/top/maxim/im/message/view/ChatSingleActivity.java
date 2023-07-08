
package top.maxim.im.message.view;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import im.floo.floolib.BMXMessage;
import top.maxim.im.contact.view.RosterDetailActivity;
import top.maxim.im.message.contract.ChatSingleContract;
import top.maxim.im.message.customviews.MessageInputBar;
import top.maxim.im.message.presenter.ChatSinglePresenter;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description : 单聊activity Created by Mango on 2018/11/06.
 */
public class ChatSingleActivity extends ChatBaseActivity implements ChatSingleContract.View {

    private ChatSingleContract.Presenter mPresenter;

    @Override
    protected void onHeaderRightClick() {
        RosterDetailActivity.openRosterDetail(this, mChatId);
    }

    @Override
    protected void initChatInfo(long myUserId, long chatId) {
        mPresenter = new ChatSinglePresenter(this);
        mPresenter.setChatInfo(BMXMessage.MessageType.Single, myUserId, chatId);
    }

    @Override
    public void onTagChanged(int tag) {
        super.onTagChanged(tag);
        if (tag == MessageInputBar.OnInputPanelListener.TAG_OPEN
                || tag == MessageInputBar.OnInputPanelListener.TAG_CLOSE) {
            // 发送输入板状态
            String extension = "";
            try {
                JSONObject object = new JSONObject();
                object.put(MessageConfig.INPUT_STATUS,
                        tag == MessageInputBar.OnInputPanelListener.TAG_OPEN
                                ? MessageConfig.InputStatus.TYING_STATUS
                                : MessageConfig.InputStatus.NOTHING_STATUS);
                extension = object.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (mPresenter != null) {
                mPresenter.sendInputStatus(extension);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPresenter != null){
            mPresenter.updateChatData();
        }
    }

    @Override
    protected void setViewListener() {
        super.setViewListener();
    }

    @Override
    public Context getContext() {
        return this;
    }
}
