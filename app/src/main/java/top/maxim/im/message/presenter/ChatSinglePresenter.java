
package top.maxim.im.message.presenter;

import android.text.TextUtils;

import org.json.JSONObject;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXRosterItem;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.ChatManager;
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
        mRoster = new BMXRosterItem();
        Observable.just(chatId).map(new Func1<Long, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(Long aLong) {
                return RosterManager.getInstance().search(aLong, true, mRoster);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mRoster = null;
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
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
        Observable.just(extension).map(new Func1<String, String>() {
            @Override
            public String call(String s) {
                String name = "";
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString(MessageConfig.INPUT_STATUS);
                    if (TextUtils.equals(status, MessageConfig.InputStatus.TYING_STATUS)) {
                        // 显示正在输入
                        name = "对方正在输入...";
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

    @Override
    protected void ackMessage(BMXMessage message) {
        super.ackMessage(message);
        // 已读不在发送 自己发送的消息不设置已读
        if (message == null || message.isReadAcked() || !message.isReceiveMsg()) {
            return;
        }
        Observable.just(message).map(new Func1<BMXMessage, BMXMessage>() {
            @Override
            public BMXMessage call(BMXMessage message) {
                ChatManager.getInstance().ackMessage(message);
                return message;
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXMessage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BMXMessage message) {
                    }
                });
    }
}
