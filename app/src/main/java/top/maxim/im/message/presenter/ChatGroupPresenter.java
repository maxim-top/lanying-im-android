
package top.maxim.im.message.presenter;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupMemberList;
import im.floo.floolib.BMXMessage;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.ChatManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.message.contract.ChatGroupContract;
import top.maxim.im.message.view.ChatGroupAtActivity;

/**
 * Description : 群聊presenter Created by Mango on 2018/11/11.
 */
public class ChatGroupPresenter extends ChatBasePresenter implements ChatGroupContract.Presenter {

    private ChatGroupContract.View mView;

    private ChatGroupContract.Model mModel;

    private final int AT_REQUEST = 2000;

    private BMXGroup mGroup = new BMXGroup();

    /* 文本@的对象列表 以feedId作为唯一标志 */
    private Map<String, String> mAtMap;

    public ChatGroupPresenter(ChatGroupContract.View view) {
        super();
        mView = view;
        mView.setPresenter(this);
        setChatBaseView(mView, mModel);
    }

    @Override
    public void setChatInfo(BMXMessage.MessageType chatType, long myUserId, final long chatId) {
        super.setChatInfo(chatType, myUserId, chatId);
        // 先从conversation获取名称展示
        if (mConversation != null) {
            BMXGroup groupItem = RosterFetcher.getFetcher()
                    .getGroup(mConversation.conversationId());
            String name = groupItem != null ? groupItem.name() : "";
            if (mView != null) {
                mView.setHeadTitle(name);
            }
        }
        // 获取群聊信息
        if (chatId <= 0) {
            return;
        }
        Observable.just(mGroup).map(new Func1<BMXGroup, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXGroup group) {
                return GroupManager.getInstance().search(chatId, mGroup, true);
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
                        mGroup = null;
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        RosterFetcher.getFetcher().putGroup(mGroup);
                        if (mView != null) {
                            mView.setHeadTitle(mGroup.name());
                        }
                        syncGroupMember();
                    }
                });
    }

    /**
     * 同步群成员
     */
    private void syncGroupMember() {
        Observable.just(mGroup).map(new Func1<BMXGroup, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXGroup group) {
                return GroupManager.getInstance().getMembers(mGroup, new BMXGroupMemberList(),
                        true);
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

                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {

                    }
                });
    }

    /**
     * 清除@对象 发送完文本消息清除
     */
    @Override
    public void clearAtFeed() {
        if (mAtMap != null) {
            mAtMap.clear();
        }
    }

    @Override
    public Map<String, String> getChatAtMembers() {
        return mAtMap;
    }

    @Override
    protected boolean isCurrentSession(BMXMessage message) {
        return message != null && message.type() == BMXMessage.MessageType.Group
                && message.toId() == mChatId;
    }

    @Override
    public void onChatAtMember() {
        ChatGroupAtActivity.startGroupAtActivity(mView.getContext(), mChatId, AT_REQUEST);
    }

    @Override
    public void readAllMessage(final BMXMessage message) {
        if (message == null) {
            return;
        }
        Observable.just("").map(new Func1<String, String>() {
            @Override
            public String call(String s) {
                ChatManager.getInstance().readAllMessage(message);
                return s;
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {
                        Log.e("GroupChat", "readAllMessage is success");
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AT_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Map<String, String> map = (Map<String, String>)data
                    .getSerializableExtra(ChatGroupAtActivity.CHOOSE_DATA);
            if (map != null && !map.isEmpty()) {
                if (mAtMap == null) {
                    mAtMap = new HashMap<>();
                } else {
                    mAtMap.clear();
                }
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    mAtMap.put(entry.getKey(), entry.getValue());
                }
            }
            handleAt();
        }
    }

    /**
     * 处理@
     */
    private void handleAt() {
        if (mAtMap == null || mAtMap.isEmpty()) {
            return;
        }
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, String> entry : mAtMap.entrySet()) {
            // 发送文字包含@对象的名称时再加入 防止输入框@对象名称被修改
            if (entry.getValue() != null && !TextUtils.isEmpty(entry.getValue())) {
                names.add("@" + entry.getValue());
            }
        }
        // @功能 名字后加空格
        if (!names.isEmpty() && mView != null) {
            // @的内容️以ForegroundColorSpan转换
            mView.insertInAt(names);
        }
    }
}
