
package top.maxim.im.message.view;

import android.content.Context;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import im.floo.floolib.BMXMessage;
import top.maxim.im.contact.view.RosterDetailActivity;
import top.maxim.im.message.contract.ChatSingleContract;
import top.maxim.im.message.customviews.MessageInputBar;
import top.maxim.im.message.presenter.ChatSinglePresenter;
import top.maxim.im.message.utils.MessageConfig;
import top.maxim.im.message.utils.MessageEvent;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(MessageEvent messageEvent) {
        if (messageEvent.getMessage().equals("scroll-to-bottom")){
            LinearLayoutManager llm = (LinearLayoutManager) mChatRecyclerView.getLayoutManager();
            int pos = mChatViewHelper.getLastVisiblePosition();
            llm.scrollToPositionWithOffset(pos,-100000);
        }
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
    protected void setViewListener() {
        super.setViewListener();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void enableInputBar(boolean enable) {
        super.enableInputBar(enable, false);
    }
}
