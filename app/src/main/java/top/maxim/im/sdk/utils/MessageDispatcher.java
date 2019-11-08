
package top.maxim.im.sdk.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import im.floo.floolib.BMXChatServiceListener;
import im.floo.floolib.BMXConnectStatus;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupList;
import im.floo.floolib.BMXGroupServiceListener;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageList;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXRosterServiceListener;
import im.floo.floolib.BMXUserProfile;
import im.floo.floolib.BMXUserServiceListener;
import im.floo.floolib.FileProgressListener;
import im.floo.floolib.ListOfLongLong;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.ChatManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.login.view.WelcomeActivity;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;
import top.maxim.im.push.PushClientMgr;
import top.maxim.im.push.PushUtils;

/**
 * Description : 消息分发 Created by mango on 2018/12/20.
 */
public class MessageDispatcher {

    private static MessageDispatcher sDispatcher = new MessageDispatcher();

    private List<BMXChatServiceListener> mListener = new ArrayList<>();

    private BMXChatServiceListener mChatListener = new BMXChatServiceListener() {

        @Override
        public void onStatusChanged(BMXMessage msg, BMXErrorCode error) {
            super.onStatusChanged(msg, error);
            for (BMXChatServiceListener listener : mListener) {
                listener.onStatusChanged(msg, error);
            }
        }

        @Override
        public void onAttachmentStatusChanged(BMXMessage msg, BMXErrorCode error, int percent) {
            super.onAttachmentStatusChanged(msg, error, percent);
            for (BMXChatServiceListener listener : mListener) {
                listener.onAttachmentStatusChanged(msg, error, percent);
            }
        }

        @Override
        public void onRecallStatusChanged(BMXMessage msg, BMXErrorCode error) {
            super.onRecallStatusChanged(msg, error);
            for (BMXChatServiceListener listener : mListener) {
                listener.onRecallStatusChanged(msg, error);
            }
        }

        @Override
        public void onReceive(BMXMessageList list) {
            super.onReceive(list);
            if (list != null && !list.isEmpty()) {
                notifyNotification(list.get(0));
            }
            for (BMXChatServiceListener listener : mListener) {
                listener.onReceive(list);
            }
        }

        @Override
        public void onReceiveSystemMessages(BMXMessageList list) {
            super.onReceiveSystemMessages(list);
            for (BMXChatServiceListener listener : mListener) {
                listener.onReceiveSystemMessages(list);
            }
        }

        @Override
        public void onReceiveReadAcks(BMXMessageList list) {
            super.onReceiveReadAcks(list);
            for (BMXChatServiceListener listener : mListener) {
                listener.onReceiveReadAcks(list);
            }
        }

        @Override
        public void onReceiveDeliverAcks(BMXMessageList list) {
            super.onReceiveDeliverAcks(list);
            for (BMXChatServiceListener listener : mListener) {
                listener.onReceiveDeliverAcks(list);
            }
        }

        @Override
        public void onReceiveRecallMessages(BMXMessageList list) {
            super.onReceiveRecallMessages(list);
            for (BMXChatServiceListener listener : mListener) {
                listener.onReceiveRecallMessages(list);
            }
        }

        @Override
        public void onAttachmentUploadProgressChanged(BMXMessage msg, int percent) {
            super.onAttachmentUploadProgressChanged(msg, percent);
            for (BMXChatServiceListener listener : mListener) {
                listener.onAttachmentUploadProgressChanged(msg, percent);
            }
        }

    };

    /* roster监听 */
    private BMXRosterServiceListener mRosterListener = new BMXRosterServiceListener() {
        @Override
        public void onFriendAdded(long sponsorId, long recipientId) {
            super.onFriendAdded(sponsorId, recipientId);
            // 添加好友
            toastListener("onFriendAdded");
        }

        @Override
        public void onFriendRemoved(long sponsorId, long recipientId) {
            super.onFriendRemoved(sponsorId, recipientId);
            toastListener("onFriendRemoved");
        }

        @Override
        public void onApplied(long sponsorId, long recipientId, String message) {
            super.onApplied(sponsorId, recipientId, message);
            toastListener("onApplied");
        }

        @Override
        public void onApplicationAccepted(long sponsorId, long recipientId) {
            super.onApplicationAccepted(sponsorId, recipientId);
            toastListener("onApplicationAccepted");
        }

        @Override
        public void onApplicationDeclined(long sponsorId, long recipientId, String reason) {
            super.onApplicationDeclined(sponsorId, recipientId, reason);
            toastListener("onApplicationDeclined");
        }

        @Override
        public void onBlockListAdded(long sponsorId, long recipientId) {
            super.onBlockListAdded(sponsorId, recipientId);
            toastListener("onBlockListAdded");
        }

        @Override
        public void onBlockListRemoved(long sponsorId, long recipientId) {
            super.onBlockListRemoved(sponsorId, recipientId);
            toastListener("onBlockListRemoved");
        }

        @Override
        public void onRosterInfoUpdate(BMXRosterItem item) {
            super.onRosterInfoUpdate(item);
            toastListener("onRosterInfoUpdate");
            RosterFetcher.getFetcher().putRoster(item);
            downloadRosterAvatar(item);
        }
    };

    /* userListener */
    private BMXUserServiceListener mUserListener = new BMXUserServiceListener() {
        @Override
        public void onUserSignIn(BMXUserProfile profile) {
            super.onUserSignIn(profile);
            toastListener("onUserSignIn");
        }

        @Override
        public void onUserSignOut(BMXErrorCode error) {
            super.onUserSignOut(error);
            toastListener("onUserSignOut");
            if (error.swigValue() == BMXErrorCode.UserRemoved.swigValue()) {
                // 被其他设备踢下线 跳转到登录页面
                Observable.just("").subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<String>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(String s) {
                                SharePreferenceUtils.getInstance().putLoginStatus(false);
                                PushClientMgr.getManager().unRegister();
                                PushUtils.getInstance().unregisterActivityListener(
                                        AppContextUtils.getApplication());
                                WelcomeActivity.openWelcome(AppContextUtils.getAppContext());
                            }
                        });
            }
        }

        @Override
        public void onConnectStatusChanged(BMXConnectStatus status) {
            super.onConnectStatusChanged(status);
            toastListener("onConnectStatusChanged");
        }

        @Override
        public void onInfoUpdated(BMXUserProfile profile) {
            super.onInfoUpdated(profile);
            toastListener("onInfoUpdated");
            RosterFetcher.getFetcher().putProfile(profile);
            downloadProfileAvatar(profile);
        }

        @Override
        public void onOtherDeviceSingIn(int deviceSN) {
            super.onOtherDeviceSingIn(deviceSN);
            toastListener("onOtherDeviceSingIn");
        }

        @Override
        public void onOtherDeviceSingOut(int deviceSN) {
            super.onOtherDeviceSingOut(deviceSN);
            toastListener("onOtherDeviceSingOut");
        }
    };

    /* 群组 */
    private BMXGroupServiceListener mGroupListener = new BMXGroupServiceListener() {
        @Override
        public void onGroupCreate(BMXGroup group) {
            super.onGroupCreate(group);
            toastListener("onGroupCreate");
        }

        @Override
        public void onGroupInfoUpdate(BMXGroup group, BMXGroup.UpdateInfoType type) {
            super.onGroupInfoUpdate(group, type);
            toastListener("onGroupInfoUpdate");
            RosterFetcher.getFetcher().putGroup(group);
            downloadGroupAvatar(group);
            Intent intent = new Intent();
            intent.setAction("onShowReadAckUpdated");
            intent.putExtra("onShowReadAckUpdated", group != null && group.enableReadAck());
            RxBus.getInstance().send(intent);
        }

        @Override
        public void onGroupJoined(BMXGroup group) {
            super.onGroupJoined(group);
            toastListener("onGroupJoined");
        }

        @Override
        public void onGroupLeft(BMXGroup group, String reason) {
            super.onGroupLeft(group, reason);
            toastListener("onGroupLeft");
        }

        @Override
        public void onGroupListUpdate(BMXGroupList list) {
            super.onGroupListUpdate(list);
            toastListener("onGroupListUpdate");
        }

        @Override
        public void onAdminsAdded(BMXGroup group, ListOfLongLong members) {
            super.onAdminsAdded(group, members);
            toastListener("onAdminsAdded");
        }

        @Override
        public void onAdminsRemoved(BMXGroup group, ListOfLongLong members, String reason) {
            super.onAdminsRemoved(group, members, reason);
            toastListener("onAdminsRemoved");
        }

        @Override
        public void onAnnouncementUpdate(BMXGroup group, BMXGroup.Announcement announcement) {
            super.onAnnouncementUpdate(group, announcement);
            toastListener("onAnnouncementUpdate");
        }

        @Override
        public void onApplicationAccepted(BMXGroup group, long approver) {
            super.onApplicationAccepted(group, approver);
            toastListener("onApplicationAccepted");
        }

        @Override
        public void onApplicationDeclined(BMXGroup group, long approver, String reason) {
            super.onApplicationDeclined(group, approver, reason);
            toastListener("onApplicationDeclined");
        }

        @Override
        public void onApplied(BMXGroup group, long applicantId, String message) {
            super.onApplied(group, applicantId, message);
            toastListener("onApplied");
        }

        @Override
        public void onInvitated(long groupId, long inviter, String message) {
            super.onInvitated(groupId, inviter, message);
            toastListener("onInvitated");
        }

        @Override
        public void onInvitationAccepted(BMXGroup group, long inviteeId) {
            super.onInvitationAccepted(group, inviteeId);
            toastListener("onInvitationAccepted");
        }

        @Override
        public void onInvitationDeclined(BMXGroup group, long inviteeId, String reason) {
            super.onInvitationDeclined(group, inviteeId, reason);
            toastListener("onInvitationDeclined");
        }

        @Override
        public void onMemberChangeNickName(BMXGroup group, long memberId, String nickName) {
            super.onMemberChangeNickName(group, memberId, nickName);
            toastListener("onMemberChangeNickName");
        }

        @Override
        public void onMemberJoined(BMXGroup group, long memberId, long inviter) {
            super.onMemberJoined(group, memberId, inviter);
            toastListener("onMemberJoined");
        }

        @Override
        public void onMemberLeft(BMXGroup group, long memberId, String reason) {
            super.onMemberLeft(group, memberId, reason);
            toastListener("onMemberLeft");
        }

        @Override
        public void onBlockListAdded(BMXGroup group, ListOfLongLong members) {
            super.onBlockListAdded(group, members);
            toastListener("onBlockListAdded");
        }

        @Override
        public void onBlockListRemoved(BMXGroup group, ListOfLongLong members) {
            super.onBlockListRemoved(group, members);
            toastListener("onBlockListRemoved");
        }

        @Override
        public void onMembersBanned(BMXGroup group, ListOfLongLong members, long duration) {
            super.onMembersBanned(group, members, duration);
            toastListener("onMembersBanned");
        }

        @Override
        public void onMembersUnbanned(BMXGroup group, ListOfLongLong members) {
            super.onMembersUnbanned(group, members);
            toastListener("onMembersUnbanned");
        }

        @Override
        public void onOwnerAssigned(BMXGroup group) {
            super.onOwnerAssigned(group);
            toastListener("onOwnerAssigned");
        }

        @Override
        public void onSharedFileDeleted(BMXGroup group, BMXGroup.SharedFile sharedFile) {
            super.onSharedFileDeleted(group, sharedFile);
            toastListener("onSharedFileDeleted");
        }

        @Override
        public void onSharedFileUpdated(BMXGroup group, BMXGroup.SharedFile sharedFile) {
            super.onSharedFileUpdated(group, sharedFile);
            toastListener("onSharedFileUpdated");
        }

        @Override
        public void onSharedFileUploaded(BMXGroup group, BMXGroup.SharedFile sharedFile) {
            super.onSharedFileUploaded(group, sharedFile);
            toastListener("onSharedFileUploaded");
        }
    };

    private MessageDispatcher() {
    }

    public static MessageDispatcher getDispatcher() {
        return sDispatcher;
    }

    public void initialize() {
        ChatManager.getInstance().addChatListener(mChatListener);
        RosterManager.getInstance().addRosterListener(mRosterListener);
        UserManager.getInstance().addUserListener(mUserListener);
        GroupManager.getInstance().addGroupListener(mGroupListener);
    }

    private void toastListener(String content) {
        if (TextUtils.isEmpty(content)) {
            return;
        }
        // 是否多端提示 默认false
        boolean tips = SharePreferenceUtils.getInstance().getDevTips();
        if (!tips) {
            return;
        }
        Observable.just(content).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {
                        ToastUtil.showTextViewPrompt(s);
                    }
                });
    }

    /**
     * 下载头像
     */
    private void downloadProfileAvatar(final BMXUserProfile profile) {
        if (profile == null) {
            return;
        }
        Observable.just("").map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                return UserManager.getInstance().downloadAvatar(profile, new FileProgressListener(){
                    @Override
                    public int onProgressChange(String percent) {
                        return 0;
                    }
                });
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
                        RosterFetcher.getFetcher().putProfile(profile);
                        Intent intent = new Intent();
                        intent.setAction("onInfoUpdated");
                        RxBus.getInstance().send(intent);
                    }
                });
    }

    /**
     * 下载头像
     */
    private void downloadRosterAvatar(final BMXRosterItem item) {
        if (item == null) {
            return;
        }
        Observable.just(item).map(new Func1<BMXRosterItem, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXRosterItem s) {
                return RosterManager.getInstance().downloadAvatar(s, new FileProgressListener(){
                    @Override
                    public int onProgressChange(String percent) {
                        return 0;
                    }
                });
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
                        RosterFetcher.getFetcher().putRoster(item);
                        Intent intent = new Intent();
                        intent.setAction("onRosterInfoUpdate");
                        RxBus.getInstance().send(intent);
                    }
                });
    }

    /**
     * 下载头像
     */
    private void downloadGroupAvatar(final BMXGroup item) {
        if (item == null) {
            return;
        }
        Observable.just(item).map(new Func1<BMXGroup, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXGroup s) {
                return GroupManager.getInstance().downloadAvatar(s, new FileProgressListener(){
                    @Override
                    public int onProgressChange(String percent) {
                        return 0;
                    }
                });
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
                        RosterFetcher.getFetcher().putGroup(item);
                        Intent intent = new Intent();
                        intent.setAction("onGroupInfoUpdate");
                        RxBus.getInstance().send(intent);
                    }
                });
    }

    /**
     * 通知push
     */
    private void notifyNotification(BMXMessage bean) {
        if (bean == null || !bean.isReceiveMsg()) {
            return;
        }
        Context context = AppContextUtils.getAppContext();
        Intent intent = new Intent(String.format(context.getString(R.string.im_push_msg_action),
                context.getPackageName()));
        if (bean.type() == BMXMessage.MessageType.Group) {
            intent.putExtra(MessageConfig.CHAT_MSG,
                    ChatUtils.getInstance().buildMessage(bean, bean.type(), bean.toId()));
        } else {
            intent.putExtra(MessageConfig.CHAT_MSG,
                    ChatUtils.getInstance().buildMessage(bean, bean.type(), bean.fromId()));
        }
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }
}
