
package top.maxim.im.message.presenter;

import android.text.TextUtils;

import org.json.JSONObject;

import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXRosterItem;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.message.contract.ChatSingleContract;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description : 单聊presenter Created by Mango on 2018/11/11.
 */
public class ChatSinglePresenter extends ChatBasePresenter implements ChatSingleContract.Presenter {

    private ChatSingleContract.View mView;

    private ChatSingleContract.Model mModel;

    private BMXRosterItem mRoster;

    public ChatSinglePresenter(ChatSingleContract.View view) {
        super();
        mView = view;
        mView.setPresenter(this);
        setChatBaseView(mView, mModel);
    }

    @Override
    public void setChatInfo(BMXMessage.MessageType chatType, final long myUserId, long chatId) {
        super.setChatInfo(chatType, myUserId, chatId);
        // 先从conversation获取名称展示
        if (mConversation != null) {
            String name = "";
            BMXRosterItem rosterItem = RosterFetcher.getFetcher()
                    .getRoster(mConversation.conversationId());
            if (rosterItem != null && !TextUtils.isEmpty(rosterItem.alias())) {
                name = rosterItem.alias();
            } else if (rosterItem != null && !TextUtils.isEmpty(rosterItem.nickname())) {
                name = rosterItem.nickname();
            } else if (rosterItem != null) {
                name = rosterItem.username();
            }
            if (mView != null) {
                mView.setHeadTitle(name);
            }
        }
        RosterManager.getInstance().getRosterList(chatId, true, (bmxErrorCode, bmxRosterItem) -> {
            if (!BaseManager.bmxFinish(bmxErrorCode)) {
                return;
            }
            if (bmxRosterItem != null) {
                mRoster = bmxRosterItem;
            }
            RosterFetcher.getFetcher().putRoster(mRoster);
            String name = "";
            if (mRoster != null && !TextUtils.isEmpty(mRoster.alias())) {
                name = mRoster.alias();
            } else if (mRoster != null && !TextUtils.isEmpty(mRoster.nickname())) {
                name = mRoster.nickname();
            } else if (mRoster != null) {
                name = mRoster.username();
            }
            if (mView != null) {
                mView.setHeadTitle(name);
            }
        });
    }

    @Override
    protected boolean isCurrentSession(BMXMessage message) {
        if (message == null || message.type() != BMXMessage.MessageType.Single) {
            return false;
        }
        if (message.isReceiveMsg()) {
            // 对方发过来的消息
            return message.fromId() == mChatId;
        }
        return message.toId() == mChatId;
    }

    @Override
    protected void handelInputStatus(String extension) {
        super.handelInputStatus(extension);
        if (TextUtils.isEmpty(extension)) {
            return;
        }
        if (mChatId == mMyUserId) {
            // 如果对方是自己 不显示
            return;
        }
        Observable.just(extension).map(new Func1<String, String>() {
            @Override
            public String call(String s) {
                String name = "";
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString(MessageConfig.INPUT_STATUS);
                    if (TextUtils.equals(status, MessageConfig.InputStatus.TYING_STATUS)) {
                        // 显示正在输入
                        name = mView.getContext().getString(R.string.other_party_typing);
                    } else if (TextUtils.equals(status, MessageConfig.InputStatus.NOTHING_STATUS)) {
                        // 还原标题头
                        if (mRoster != null && !TextUtils.isEmpty(mRoster.nickname())) {
                            name = mRoster.nickname();
                        } else if (mRoster != null) {
                            name = mRoster.username();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return name;
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                if (mView != null) {
                    mView.setHeadTitle(s);
                }
            }
        });
    }

    @Override
    public void sendInputStatus(String extension) {
        mSendUtils.sendInputStatusMessage(mChatType, mMyUserId, mChatId, extension);
    }
}
