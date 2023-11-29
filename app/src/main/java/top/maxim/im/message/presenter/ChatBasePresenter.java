
package top.maxim.im.message.presenter;

import static top.maxim.im.common.utils.AppContextUtils.getApplication;
import static top.maxim.im.message.presenter.ChatBasePresenter.FunctionType.*;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import im.floo.BMXDataCallBack;
import im.floo.floolib.BMXChatServiceListener;
import im.floo.floolib.BMXConversation;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXFileAttachment;
import im.floo.floolib.BMXImageAttachment;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageAttachment;
import im.floo.floolib.BMXMessageList;
import im.floo.floolib.BMXVideoAttachment;
import im.floo.floolib.BMXVoiceAttachment;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.ChatManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.MaxIMApplication;
import top.maxim.im.common.base.PermissionActivity;
import top.maxim.im.common.bean.FileBean;
import top.maxim.im.common.bean.MessageBean;
import top.maxim.im.common.bean.PhotoViewBean;
import top.maxim.im.common.bean.PhotoViewListBean;
import top.maxim.im.common.provider.CommonProvider;
import top.maxim.im.common.utils.CameraUtils;
import top.maxim.im.common.utils.FileConfig;
import top.maxim.im.common.utils.FileUtils;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.TaskDispatcher;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.VoicePlayHelper;
import top.maxim.im.common.utils.VoiceRecordHelper;
import top.maxim.im.common.utils.dialog.CommonDialog;
import top.maxim.im.common.utils.dialog.CustomDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.utils.permissions.PermissionsConstant;
import top.maxim.im.common.utils.permissions.PermissionsMgr;
import top.maxim.im.common.utils.permissions.PermissionsResultAction;
import top.maxim.im.common.utils.video.PhotoRecorderActivity;
import top.maxim.im.contact.view.ForwardMsgRosterActivity;
import top.maxim.im.message.contract.ChatBaseContract;
import top.maxim.im.message.customviews.MessageInputBar;
import top.maxim.im.message.utils.ChatAttachmentManager;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;
import top.maxim.im.message.utils.RefreshChatActivityEvent;
import top.maxim.im.message.utils.VoicePlayManager;
import top.maxim.im.message.view.ChatBaseActivity;
import top.maxim.im.message.view.ChatGroupActivity;
import top.maxim.im.message.view.ChatSingleActivity;
import top.maxim.im.message.view.ChooseFileActivity;
import top.maxim.im.message.view.PhotoDetailActivity;
import top.maxim.im.message.view.VideoDetailActivity;
import top.maxim.im.sdk.utils.MessageSendUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Description : 聊天基类presenter Created by Mango on 2018/11/11
 */
public class ChatBasePresenter implements ChatBaseContract.Presenter {

    private static final String TAG = ChatBasePresenter.class.getSimpleName();
    public static final long REPORT_ID = 6855234888912L;

    /* 相机 */
    private final int CAMERA_REQUEST = 1000;

    /* 相册 */
    private final int IMAGE_REQUEST = 1001;

    /* 文件 */
    private final int FILE_REQUEST = 1002;

    /* 视频 */
    private final int VIDEO_REQUEST = 1003;

    /* 转发 */
    private final int FORWARD_REQUEST = 1004;

    /* 拍照权限 */
    private final int TYPE_CAMERA_PERMISSION = 1;

    /* 相册权限 */
    private final int TYPE_PHOTO_PERMISSION = 2;

    /* 语音权限 */
    private final int TYPE_VOICE_PERMISSION = 3;

    /* 文件权限 */
    private final int TYPE_FILE_PERMISSION = 4;

    /* 位置权限 */
    private final int TYPE_LOCATION_PERMISSION = 5;

    /* 视频权限 */
    private final int TYPE_VIDEO_PERMISSION = 6;

    /* 视频通话权限 */
    protected final int TYPE_VIDEO_CALL_PERMISSION = 7;

    /* 音频通话权限 */
    private final int TYPE_AUDIO_CALL_PERMISSION = 8;

    /* 我的id */
    protected long mMyUserId;

    /* 对方id */
    protected long mChatId;

    /* 聊天类型 */
    protected BMXMessage.MessageType mChatType;

    protected BMXConversation mConversation;

    /* view */
    private ChatBaseContract.View mView;

    /* model */
    private ChatBaseContract.Model mModel;

    /* 发送消息工具类 */
    protected MessageSendUtils mSendUtils;

    /* 录制语音帮助类 */
    private VoiceRecordHelper mVoiceRecordHelper;

    /* 播放语音帮助类 */
    private VoicePlayHelper mVoicePlayHelper;

    /* 语音录制事件 */
    private int mRecordVoiceAction;

    /* 语音录制时间 */
    private long mRecordVoiceTime;

    /* 语音名称 */
    private String mVoiceName;

    /**
     * 语音录制状态 1:未录制状态;2:录制中状态 *
     */
    private int mRecordingStatus = 1;

    /* 照相的相片名字 */
    private String mCameraName;

    /* 相片所在文件夹 */
    private String mCameraDir;

    /* 相片完整路径 包含相片名字 */
    private String mCameraPath;

    private String myUserName;

    private CompositeSubscription mSubcription;

    private BMXChatServiceListener mListener = new BMXChatServiceListener() {

        @Override
        public void onStatusChanged(BMXMessage msg, BMXErrorCode error) {
            // 发送状态更新页面
            if (msg != null && isCurrentSession(msg)) {
                mView.updateChatMessage(msg);
                // 如果失败判断弹出提示
                if (error != null && error.swigValue() != BMXErrorCode.NoError.swigValue()) {
                    String errorMsg = error.name();
                    ((Activity)mView.getContext())
                            .runOnUiThread(() -> ToastUtil.showTextViewPrompt(errorMsg));
                }
            }
        }

        @Override
        public void onAttachmentStatusChanged(BMXMessage msg, BMXErrorCode error, int percent) {
            // 附件消息下载进度
            ChatAttachmentManager.getInstance().onProgressCallback(msg, percent);
            Log.d("statusChanged", percent + "");
        }

        @Override
        public void onRecallStatusChanged(BMXMessage msg, BMXErrorCode error) {
            if (msg == null) {
                return;
            }
            boolean success = error != null
                    && error.swigValue() == BMXErrorCode.NoError.swigValue();
            String errorMsg = error != null && !TextUtils.isEmpty(error.name()) ? error.name()
                    : "撤回失败";
            if (success) {
                // 撤回成功需要删除原始消息
                if (mView != null) {
                    mView.deleteChatMessage(msg);
                }
            } else {
                // 原始消息不为空 则没有撤回成功
                ((Activity)mView.getContext())
                        .runOnUiThread(() -> ToastUtil.showTextViewPrompt(errorMsg));
            }
        }

        @Override
        public void onReceive(BMXMessageList list) {
            // 收到消息
            if (list != null && !list.isEmpty()) {
                List<BMXMessage> messages = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    BMXMessage message = list.get(i);
                    if (message != null && isCurrentSession(message) && filterMessage(message)) {
                        ((MaxIMApplication) getApplication()).typeWriterMsgId = message.msgId();
                        // 当前会话
                        messages.add(message);
                    }
                }
                mView.receiveChatMessage(messages);
            }
        }

        @Override
        public void onReceiveSystemMessages(BMXMessageList list) {
            // 收到系统消息
            if (list != null && !list.isEmpty()) {
                List<BMXMessage> messages = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    BMXMessage message = list.get(i);
                    if (message != null && isCurrentSession(message) && filterMessage(message)) {
                        // 当前会话
                        messages.add(message);
                    }
                }
                mView.receiveChatMessage(messages);
            }
        }

        @Override
        public void onReceiveCommandMessages(BMXMessageList list) {
        }

        @Override
        public void onReceiveReadAcks(BMXMessageList list) {
            // 收到已读回执
            if (list != null && !list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    BMXMessage message = list.get(i);
                    if (message != null) {
                        // 当前会话
                        if (mView != null) {
                            mView.updateChatMessage(message);
                        }
                    }
                }
            }
        }

        @Override
        public void onReceiveDeliverAcks(BMXMessageList list) {
        }

        @Override
        public void onReceiveRecallMessages(BMXMessageList list) {
            // 收到撤回消息
            if (list != null && !list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    BMXMessage message = list.get(i);
                    if (message != null && isCurrentSession(message)) {
                        // 当前会话
                        if (mView != null) {
                            ((Activity)mView.getContext())
                                    .runOnUiThread(() -> ToastUtil.showTextViewPrompt("对方撤回一条消息"));
                            mView.deleteChatMessage(message);
                        }
                    }
                }
            }
        }

        @Override
        public void onAttachmentUploadProgressChanged(BMXMessage msg, int percent) {
            // 附件消息上传进度
            ChatAttachmentManager.getInstance().onProgressCallback(msg, percent);
            Log.d("progressChanged", percent + "");
        }
    };

    ChatBasePresenter() {
        ChatManager.getInstance().addChatListener(mListener);
    }

    @Override
    public void initChatData(final long msgId) {
        if (mConversation == null) {
            return;
        }
        if (mChatId == REPORT_ID){
            if (mView != null) {
                mView.setControlBarText("被举报人昵称(选填)：\n\n被举报人用户ID（必填）：\n\n举报事由（必填）：");
            }
        }
        //拉取历史消息
        ChatManager.getInstance().retrieveHistoryMessages(mConversation, msgId,
                MessageConfig.DEFAULT_PAGE_SIZE, (bmxErrorCode, messageList) -> {
                    if (BaseManager.bmxFinish(bmxErrorCode)) {
                        if (!messageList.isEmpty()) {
                            List<BMXMessage> messages = new ArrayList<>();
                            for (int i = 0; i < messageList.size(); i++) {
                                messages.add(messageList.get(i));
                            }
                            if (mView != null) {
                                mView.showChatMessages(messages);
                            }
                        }
                        return;
                    }
                    // 历史消息没有拉到 从DB获取
                    mConversation.loadMessages(msgId, MessageConfig.DEFAULT_PAGE_SIZE,
                            (bmxErrorCode1, bmxMessageList) -> {
                                if (BaseManager.bmxFinish(bmxErrorCode1)) {
                                    if (!bmxMessageList.isEmpty()) {
                                        List<BMXMessage> messages = new ArrayList<>();
                                        for (int i = 0; i < bmxMessageList.size(); i++) {
                                            messages.add(bmxMessageList.get(i));
                                        }
                                        if (mView != null) {
                                            mView.showChatMessages(messages);
                                        }
                                    }
                                } else {
                                    if (bmxErrorCode1 != null
                                            && !TextUtils.isEmpty(bmxErrorCode1.name())) {
                                        TaskDispatcher.postMain(() -> ToastUtil
                                                .showTextViewPrompt(bmxErrorCode1.name()));
                                    }
                                }
                            });
                });
    }

    @Override
    public void getPullDownChatMessages(final long msgId, final int offset) {
        if (mConversation == null || msgId < 0) {
            return;
        }
        ChatManager.getInstance().retrieveHistoryMessages(mConversation, msgId,
                MessageConfig.DEFAULT_PAGE_SIZE, (bmxErrorCode, messageList) -> {
                    if (BaseManager.bmxFinish(bmxErrorCode)) {
                        if (!messageList.isEmpty()) {
                            List<BMXMessage> messages = new ArrayList<>();
                            for (int i = 0; i < messageList.size(); i++) {
                                messages.add(messageList.get(i));
                            }
                            if (mView != null) {
                                mView.showPullChatMessages(messages, offset);
                            }
                        }
                        return;
                    }
                    // 历史消息没有拉到 从DB获取
                    mConversation.loadMessages(msgId, MessageConfig.DEFAULT_PAGE_SIZE,
                            (bmxErrorCode1, bmxMessageList) -> {
                                if (BaseManager.bmxFinish(bmxErrorCode1)) {
                                    if (!bmxMessageList.isEmpty()) {
                                        List<BMXMessage> messages = new ArrayList<>();
                                        for (int i = 0; i < messageList.size(); i++) {
                                            messages.add(messageList.get(i));
                                        }
                                        if (mView != null) {
                                            mView.showPullChatMessages(messages, offset);
                                        }
                                    }
                                }
                            });
                });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (outState != null && !TextUtils.isEmpty(mCameraPath)) {
            outState.putString(MessageConfig.KEY_CAMERA_PATH, mCameraPath);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle outState) {
        if (outState != null) {
            String cameraPath = outState.getString(MessageConfig.KEY_CAMERA_PATH);
            if (!TextUtils.isEmpty(cameraPath))
                mCameraPath = cameraPath;
        }
    }

    @Override
    public void onPause() {
        stopAudio();
        // 聊天页面返回需要更新会话的草稿
        if (mConversation != null) {
            mConversation.setEditMessage(mView == null ? "" : mView.getControlBarText());
        }
        // 同步未读
        readAllMessage();
    }

    @Override
    public void setChatInfo(BMXMessage.MessageType chatType, final long myUserId, long chatId) {
        mChatType = chatType;
        mMyUserId = myUserId;
        mChatId = chatId;
        mSendUtils = new MessageSendUtils();
        if (mConversation == null || mConversation.conversationId() != chatId) {
            BMXDataCallBack<BMXConversation> callBack = (bmxErrorCode, bmxConversation) -> {
                if (BaseManager.bmxFinish(bmxErrorCode) && bmxConversation != null) {
                    mConversation = bmxConversation;
                    // 设置已读
                    if (mConversation.unreadNumber() > 0) {
                        mConversation.setAllMessagesRead(null);
                    }
                    // 获取草稿
                    if (mView != null) {
                        mView.setControlBarText(mConversation.editMessage());
                    }
                    initChatData(0);
                }
            };
            if (chatType != null && chatType == BMXMessage.MessageType.Single) {
                ChatManager.getInstance().openConversation(chatId, BMXConversation.Type.Single,
                        true, callBack);
            } else if (chatType != null && chatType == BMXMessage.MessageType.Group) {
                ChatManager.getInstance().openConversation(chatId, BMXConversation.Type.Group, true,
                        callBack);
            }
        } else {
            // 设置已读
            if (mConversation.unreadNumber() > 0) {
                mConversation.setAllMessagesRead(null);
            }
            // 获取草稿
            if (mView != null) {
                mView.setControlBarText(mConversation.editMessage());
            }
            initChatData(0);
        }
        UserManager.getInstance().getProfile(false, (bmxErrorCode, bmxUserProfile) -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                myUserName = bmxUserProfile == null ? "" : bmxUserProfile.username();
            }
        });
    }

    @Override
    public void setChatBaseView(ChatBaseContract.View view, ChatBaseContract.Model model) {
        mView = view;
        mModel = model;
        mSubcription = new CompositeSubscription();
        receiveRxBus();
    }

    private void receiveRxBus() {
        Subscription subscription = RxBus.getInstance().toObservable(RefreshChatActivityEvent.class)
                .map(new Func1<RefreshChatActivityEvent, Pair<Integer, List<String>>>() {
                    @Override
                    public Pair<Integer, List<String>> call(
                            RefreshChatActivityEvent refreshChatActivityEvent) {
                        List<String> list = refreshChatActivityEvent == null ? null
                                : refreshChatActivityEvent.getMsgBeans();
                        return new Pair<>(refreshChatActivityEvent.getRefreshType(), list);
                    }
                }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Pair<Integer, List<String>>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Pair<Integer, List<String>> pair) {
                        if (pair == null || pair.second == null || pair.second.isEmpty()) {
                            return;
                        }
                        int type = pair.first;
                        List<String> list = pair.second;
                        BMXDataCallBack<BMXMessage> callBack = (bmxErrorCode, bmxMessage) -> {
                            List<BMXMessage> msgs = new ArrayList<>();
                            if (isCurrentSession(bmxMessage)) {
                                msgs.add(bmxMessage);
                            }
                            switch (type) {
                                case RefreshChatActivityEvent.TYPE_ADD:
                                    // 添加
                                    if (mView != null) {
                                        mView.sendChatMessages(msgs);
                                    }
                                    break;
                                case RefreshChatActivityEvent.TYPE_DELETE:
                                    for (BMXMessage msg : msgs) {
                                        if (mView != null) {
                                            mView.deleteChatMessage(msg);
                                        }
                                    }
                                    break;
                                case RefreshChatActivityEvent.TYPE_UPDATE:
                                    for (BMXMessage msg : msgs) {
                                        if (mView != null) {
                                            mView.updateChatMessage(msg);
                                        }
                                    }
                                    break;
                                default:
                                    break;
                            }
                        };
                        for (String msgId : list) {
                            ChatManager.getInstance().getMessage(Long.valueOf(msgId), callBack);
                        }
                    }
                });
        mSubcription.add(subscription);
    }

    enum FunctionType{
        UNKNOWN,
        PHOTOS,
        SNAP,
        VIDEO,
        FILE,
        LOCATION,
        VIDEO_CALL,
        VOICE_CALL,
        REPORT
    }

    @Override
    public void onFunctionRequest(String functionType) {
        FunctionType type = UNKNOWN;
        if (functionType.equals(mView.getContext().getString(R.string.snap))){
            type = SNAP;
        }else if (functionType.equals(mView.getContext().getString(R.string.video))){
            type = VIDEO;
        }else if (functionType.equals(mView.getContext().getString(R.string.file))){
            type = FILE;
        }else if (functionType.equals(mView.getContext().getString(R.string.location))){
            type = LOCATION;
        }else if (functionType.equals(mView.getContext().getString(R.string.photo_album))){
            type = PHOTOS;
        }else if (functionType.equals(mView.getContext().getString(R.string.call_video))){
            type = VIDEO_CALL;
        }else if (functionType.equals(mView.getContext().getString(R.string.call_audio))){
            type = VOICE_CALL;
        }else if (functionType.equals(mView.getContext().getString(R.string.report))){
            type = REPORT;
        }
        switch (type) {
            case PHOTOS:
                // 选择相册 需要SD卡读写权限
                if (hasPermission(PermissionsConstant.READ_STORAGE,
                        PermissionsConstant.WRITE_STORAGE)) {
                    CameraUtils.getInstance().takeGalley((Activity)mView.getContext(),
                            IMAGE_REQUEST);
                } else {
                    requestPermissions(TYPE_PHOTO_PERMISSION, PermissionsConstant.READ_STORAGE);
                }
                break;
            case SNAP:
                // 拍照 需要SD卡读写 拍照权限
                if (hasPermission(PermissionsConstant.CAMERA, PermissionsConstant.READ_STORAGE,
                        PermissionsConstant.WRITE_STORAGE)) {
                    takePic();
                } else {
                    requestPermissions(TYPE_CAMERA_PERMISSION, PermissionsConstant.READ_STORAGE);
                }
                break;
            case VIDEO:
                // 视频需要SD卡读写 麦克风权限
                if (hasPermission(PermissionsConstant.CAMERA, PermissionsConstant.RECORD_AUDIO,
                        PermissionsConstant.READ_STORAGE, PermissionsConstant.WRITE_STORAGE)) {
                    showVideoView();
                } else {
                    // 如果没有权限 首先请求SD读权限
                    requestPermissions(TYPE_VIDEO_PERMISSION, PermissionsConstant.READ_STORAGE);
                }
                break;
            case FILE:
                // 文件 需要SD卡读写 拍照权限
                if (hasPermission(PermissionsConstant.CAMERA, PermissionsConstant.READ_STORAGE,
                        PermissionsConstant.WRITE_STORAGE)) {
                    chooseFile();
                } else {
                    requestPermissions(TYPE_FILE_PERMISSION, PermissionsConstant.READ_STORAGE);
                }
                break;
            case LOCATION:
                if (mView != null) {
                    if (hasPermission(PermissionsConstant.FINE_LOCATION,
                            PermissionsConstant.COARSE_LOCATION)) {
                        sendCurrentLocation();
                    } else {
                        requestPermissions(TYPE_LOCATION_PERMISSION,
                                PermissionsConstant.FINE_LOCATION);
                    }
                }
                break;
            case VIDEO_CALL:
                // 视频需要摄像头 麦克风权限
                if (hasPermission(PermissionsConstant.CAMERA, PermissionsConstant.RECORD_AUDIO)) {
                    handelVideoCall(true);
                } else {
                    // 如果没有权限 首先请求SD读权限
                    requestPermissions(TYPE_VIDEO_CALL_PERMISSION, PermissionsConstant.CAMERA);
                }

                break;
            case VOICE_CALL:
                // 音频需要麦克风权限
                if (hasPermission(PermissionsConstant.RECORD_AUDIO)) {
                    handelVideoCall(false);
                } else {
                    // 如果没有权限 首先请求SD读权限
                    requestPermissions(TYPE_AUDIO_CALL_PERMISSION, PermissionsConstant.RECORD_AUDIO);
                }
                break;
            case REPORT:
                if (mView != null) {
                    ChatBaseActivity.startChatActivity(mView.getContext(), BMXMessage.MessageType.Single, REPORT_ID);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onSendTextRequest(String sendText) {
        if (TextUtils.isEmpty(sendText)) {
            ToastUtil.showTextViewPrompt("文字不能为空!");
            return;
        }
        BMXMessage bean = mSendUtils.sendTextMessage(mView.getContext(), mChatType, mMyUserId, mChatId, sendText,
                getChatAtMembers(), myUserName);
        // 发送成功后需要清除@的对象缓存
        clearAtFeed();
        mView.sendChatMessage(bean);
    }

    @Override
    public void onSendVoiceRequest(int voiceAction, long voiceTime) {
        // 录音开始则不可以进行切换
        mRecordVoiceAction = voiceAction;
        mRecordVoiceTime = voiceTime;
        // 用户开始录音需要判断，其他不需要
        try {
            if (voiceAction == MessageInputBar.VOICE_START) {
                if (!FileUtils.checkSDCard()) {
                    ToastUtil.showTextViewPrompt("SD 不存在！");
                } else {
                    // 语音需要SD卡读写 麦克风权限
                    if (hasPermission(PermissionsConstant.RECORD_AUDIO,
                            PermissionsConstant.READ_STORAGE, PermissionsConstant.WRITE_STORAGE)) {
                        recordMedia(voiceAction, voiceTime);
                    } else {
                        // 如果没有权限 首先请求SD读权限
                        requestPermissions(TYPE_VOICE_PERMISSION, PermissionsConstant.READ_STORAGE);
                    }
                }
            } else {
                recordMedia(voiceAction, voiceTime);
            }
        } catch (IOException e) {
            Log.e(TAG, "recordMedia is failed:" + e);
        }
    }

    @Override
    public void stopAudio() {
        stopVoicePlay();
    }

    @Override
    public void onItemFunc(BMXMessage bean) {
        if (bean == null) {
            return;
        }
        BMXMessage.ContentType contentType = bean.contentType();
        if (contentType == BMXMessage.ContentType.Text) {
            // TODO
        } else if (contentType == BMXMessage.ContentType.Image) {
            // 图片
            onImageItemClick(bean);
        } else if (contentType == BMXMessage.ContentType.Voice) {
            // 语音播放
            onAudioItemClick(bean);
        } else if (contentType == BMXMessage.ContentType.File) {
            // 文件查看
            onFileItemClick(bean);
        } else if (contentType == BMXMessage.ContentType.Video) {
            // 视频
            openVideoItemClick(bean);
        }
    }

    @Override
    public void onMessageLongClick(BMXMessage bean) {
        if (bean == null) {
            return;
        }
        showOperateMessage(bean);
    }

    @Override
    public void onMessageReadAck(BMXMessage bean) {
        ackMessage(bean);
    }

    @Override
    public void onReSendMessage(BMXMessage bean) {
        reSendMessage(bean);
    }

    @Override
    public void onGroupAck(BMXMessage bean) {

    }

    private void showOperateMessage(final BMXMessage message) {
        if (message == null) {
            return;
        }
        final CustomDialog dialog = new CustomDialog();
        LinearLayout ll = new LinearLayout(mView.getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // 删除
        TextView delete = new TextView(mView.getContext());
        delete.setPadding(ScreenUtils.dp2px(15), 0, ScreenUtils.dp2px(15), 0);
        delete.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        delete.setTextColor(mView.getContext().getResources().getColor(R.color.color_black));
        delete.setBackgroundColor(mView.getContext().getResources().getColor(R.color.color_white));
        delete.setText(mView.getContext().getString(R.string.delete));
        delete.setOnClickListener(v -> {
            dialog.dismiss();
            deleteMessage(message);
        });
        ll.addView(delete, params);

        // 复制 文字才有
        // 自己发送的消息才有撤回
        if (message.contentType() == BMXMessage.ContentType.Text) {
            // 撤回
            TextView copy = new TextView(mView.getContext());
            copy.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), 0);
            copy.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            copy.setTextColor(mView.getContext().getResources().getColor(R.color.color_black));
            copy.setBackgroundColor(
                    mView.getContext().getResources().getColor(R.color.color_white));
            copy.setText(mView.getContext().getString(R.string.chat_msg_copy));
            copy.setOnClickListener(v -> {
                dialog.dismiss();
                copyMessage(message);
            });
            ll.addView(copy, params);
        }

        // 转发
        TextView relay = new TextView(mView.getContext());
        relay.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), 0);
        relay.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        relay.setTextColor(mView.getContext().getResources().getColor(R.color.color_black));
        relay.setBackgroundColor(mView.getContext().getResources().getColor(R.color.color_white));
        relay.setText(mView.getContext().getString(R.string.chat_msg_relay));
        relay.setOnClickListener(v -> {
            dialog.dismiss();
            ForwardMsgRosterActivity.openForwardMsgRosterActivity((Activity)mView.getContext(),
                    ChatUtils.getInstance().buildMessage(message, mChatType, mChatId),
                    FORWARD_REQUEST);
        });
        ll.addView(relay, params);

        // 自己发送的消息才有撤回
        if (!message.isReceiveMsg()) {
            // 撤回
            BMXMessage.DeliveryStatus sendStatus = message.deliveryStatus();
            // 发送成功才有撤回
            if (sendStatus == null || sendStatus == BMXMessage.DeliveryStatus.Deliveried) {
                TextView revoke = new TextView(mView.getContext());
                revoke.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                        ScreenUtils.dp2px(15), 0);
                revoke.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
                revoke.setTextColor(
                        mView.getContext().getResources().getColor(R.color.color_black));
                revoke.setBackgroundColor(
                        mView.getContext().getResources().getColor(R.color.color_white));
                revoke.setText(mView.getContext().getString(R.string.chat_msg_revoke));
                revoke.setOnClickListener(v -> {
                    dialog.dismiss();
                    revokeMessage(message);
                });
                ll.addView(revoke, params);
            }
        }
        // 对方发送的消息才有标记已读
        if (message.isReceiveMsg()) {
            // 标记已读
            TextView ackRead = new TextView(mView.getContext());
            ackRead.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                    0);
            ackRead.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            ackRead.setTextColor(mView.getContext().getResources().getColor(R.color.color_black));
            ackRead.setBackgroundColor(
                    mView.getContext().getResources().getColor(R.color.color_white));
            ackRead.setText(mView.getContext().getString(R.string.chat_msg_ack));
            ackRead.setOnClickListener(v -> {
                dialog.dismiss();
                ackMessage(message);
            });
            // ll.addView(ackRead, params);

            // 设置未读
            TextView ackUnRead = new TextView(mView.getContext());
            ackUnRead.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                    ScreenUtils.dp2px(15), 0);
            ackUnRead.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            ackUnRead.setTextColor(mView.getContext().getResources().getColor(R.color.color_black));
            ackUnRead.setBackgroundColor(
                    mView.getContext().getResources().getColor(R.color.color_white));
            ackUnRead.setText(mView.getContext().getString(R.string.chat_msg_unread));
            ackUnRead.setOnClickListener(v -> {
                dialog.dismiss();
                setUnReadMessage(message);
            });
            ll.addView(ackUnRead, params);
        }

        dialog.setCustomView(ll);
        dialog.showDialog((Activity)mView.getContext());
    }

    /**
     * 复制消息
     *
     * @param message 消息
     */
    private void copyMessage(BMXMessage message) {
        if (message == null || message.contentType() != BMXMessage.ContentType.Text) {
            return;
        }
        ClipboardManager clipboard = (ClipboardManager)mView.getContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return;
        }
        String text = message.content();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        ClipData clip = ClipData.newPlainText("chat_text", text);
        clipboard.setPrimaryClip(clip);
        ToastUtil.showTextViewPrompt(mView.getContext().getString(R.string.copy_successful));
    }

    /**
     * 删除消息
     *
     * @param message 消息
     */
    private void deleteMessage(final BMXMessage message) {
        if (message == null || mConversation == null) {
            return;
        }
        Observable.just(message).map(new Func1<BMXMessage, BMXMessage>() {
            @Override
            public BMXMessage call(BMXMessage message) {
                ChatManager.getInstance().removeMessage(message);
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
                    public void onNext(BMXMessage msg) {
                        if (mView != null) {
                            mView.deleteChatMessage(message);
                        }
                    }
                });
    }

    /**
     * 撤回消息
     *
     * @param message 消息
     */
    private void revokeMessage(final BMXMessage message) {
        if (message == null || mConversation == null) {
            return;
        }
        ChatManager.getInstance().recallMessage(message);
    }

    /**
     * 重新发送消息
     *
     * @param message 消息
     */
    private void reSendMessage(BMXMessage message) {
        if (message == null || mConversation == null) {
            return;
        }
        ChatManager.getInstance().resendMessage(message);
    }

    /**
     * 标记已读
     *
     * @param message 消息
     */
    protected void ackMessage(final BMXMessage message) {
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

    /**
     * 设置未读
     *
     * @param message 消息
     */
    private void setUnReadMessage(BMXMessage message) {
    }

    /**
     * 音频item点击
     *
     * @param bean 消息体
     */
    private void onAudioItemClick(final BMXMessage bean) {
        if (mView == null || bean == null || bean.contentType() != BMXMessage.ContentType.Voice) {
            return;
        }
        ackMessage(bean);
        final BMXVoiceAttachment body = BMXVoiceAttachment.dynamic_cast(bean.attachment());
        if (body == null) {
            return;
        }
        if (mVoicePlayHelper == null) {
            mVoicePlayHelper = new VoicePlayHelper((Activity)mView.getContext());
            registerSensor();
        }
        if (mVoicePlayHelper.isPlaying()) {
            stopVoicePlay();
            return;
        }
        if (TextUtils.isEmpty(body.path())) {
            Log.i(TAG, "http voiceUrl is null");
        }
        String voiceUrl = null;
        if (!TextUtils.isEmpty(body.path()) && new File(body.path()).exists()) {
            voiceUrl = body.path();
        } else {
            Log.i(TAG, "local voiceUrl is null");
        }
        stopVoicePlay();
        if (!TextUtils.isEmpty(voiceUrl)) {
            playVoice(voiceUrl, bean);
            return;
        }
        ToastUtil.showTextViewPrompt(mView.getContext().getString(R.string.downloading));
        BMXMessageAttachment.DownloadStatus status = body.downloadStatus();
        if (status == BMXMessageAttachment.DownloadStatus.Downloaing) {
            return;
        }
        ChatManager.getInstance().downloadAttachment(bean);
    }

    /**
     * 如果有权限，则进行录音任务
     */
    private void recordMedia(int action, long time) throws IOException {
        // 停止其他占用音频的地方
        stopAudio();
        if (mVoiceRecordHelper == null) {
            mVoiceRecordHelper = new VoiceRecordHelper((Activity)mView.getContext());
        }
        // 录制音量
        mVoiceRecordHelper.setCallBackSoundDecibel(new VoiceRecordHelper.OnCallBackSoundDecibel() {
            @Override
            public void callBackSoundDecibel(float decibel) {
                if (mView != null) {
                    mView.showRecordMicView((int)decibel);
                }
            }
        });
        if (action == MessageInputBar.VOICE_START && mRecordingStatus == 1) {
            // 展示语音录制view
            mView.showRecordView();
            // 改变录音状态
            mRecordingStatus = 2;
            // 开始时间
            long startVoiceTime = System.currentTimeMillis();
            if (mRecordingStatus == 2) {
                mVoiceName = FileConfig.DIR_APP_CACHE_VOICE + "/" + startVoiceTime
                        + MessageConfig.MediaFormat.VOICE_FORMAT;
                File file = new File(mVoiceName);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
                mVoiceRecordHelper.startVoiceRecord(mVoiceName);
            }
            // 手势抬起
        } else if (action == MessageInputBar.VOICE_FINISH && mRecordingStatus == 2) {// 松开手势时执行录制完成
            restoreRecording();
            if (time < 1) {
                mVoiceRecordHelper.stopVoiceRecord(true, mVoiceName);
                return;
            }
            mVoiceRecordHelper.stopVoiceRecord(false, null);
            if (new File(mVoiceName).exists() && new File(mVoiceName).length() > 0) {
                // 文件长度大于0才发送 否则就是没有录制成功
                mView.sendChatMessage(mSendUtils.sendAudioMessage(mChatType, mMyUserId, mChatId,
                        mVoiceName, (int)time));
            } else {
                // 录制失败 停止录音并删除文件
                mVoiceRecordHelper.stopVoiceRecord(true, mVoiceName);
                ToastUtil.showTextViewPrompt(
                        mView.getContext().getString(R.string.record_fail_check_permission));
            }
            // 如果时间超过一分钟，则直接发送，复原初始状态
        } else if (action == MessageInputBar.VOICE_TIME_OUT && mRecordingStatus == 2) {
            mVoiceRecordHelper.stopVoiceRecord(false, null);
            restoreRecording();
            long fileLength = 0;
            File file = new File(mVoiceName);
            // 如果语音长度小于一分钟，则录音失败
            if (file.isFile() && file.exists()) {
                fileLength = file.length();
            }
            if (fileLength >= 60) {
                mView.sendChatMessage(
                        mSendUtils.sendAudioMessage(mChatType, mMyUserId, mChatId, mVoiceName, 60));
            }
        } else if (action == MessageInputBar.VOICE_OVER && mRecordingStatus == 2) {
            // 手指划出语音录制区域

        } else if (action == MessageInputBar.VOICE_NORMAL && mRecordingStatus == 2) {
            // 手指重新划入语音录制区域

        } else if (action == MessageInputBar.VOICE_CANCEL && mRecordingStatus == 2) {// 锁屏
            // 接电话等触摸失去焦点时
            restoreRecording();
            mVoiceRecordHelper.stopVoiceRecord(true, mVoiceName);
        }
    }

    /**
     * 复原录音初始状态
     */
    private void restoreRecording() {
        mRecordingStatus = 1;
        // 隐藏语音录制view
        mView.hideRecordView();
    }

    /**
     * 取消播放的语音
     */
    private void stopVoicePlay() {
        if (mView != null) {
            mView.cancelVoicePlay();
        }
        if (mVoicePlayHelper != null) {
            mVoicePlayHelper.stopVoice();
        }
    }

    /**
     * 开始播放语音
     *
     * @param bean 语音消息体
     */
    private void playVoice(String voicePath, final BMXMessage bean) {
        if (mView == null || TextUtils.isEmpty(voicePath) || bean == null
                || bean.contentType() != BMXMessage.ContentType.Voice || mVoicePlayHelper == null) {
            return;
        }
        BMXVoiceAttachment body = BMXVoiceAttachment.dynamic_cast(bean.attachment());
        if (body == null) {
            return;
        }
        mVoicePlayHelper.setOnVoiceFinishListener(new VoicePlayHelper.OnVoiceFinishListener() {
            @Override
            public void onFinish() {
                if (mView == null) {
                    return;
                }
                VoicePlayManager.getInstance().onFinishCallback(bean);
                // mView.getVoiceMessageMsgId(chatBean.getMsgId());
            }

            @Override
            public void onStart() {
                VoicePlayManager.getInstance().onStartCallback(bean);
            }

            @Override
            public void onError() {
                onFinish();
            }
        });
        mVoicePlayHelper.startVoice(mView.getContext(), voicePath);
    }

    /**
     * 文件item点击
     *
     * @param bean 消息体
     */
    private void onFileItemClick(BMXMessage bean) {
        if (mView == null || bean == null || bean.contentType() != BMXMessage.ContentType.File) {
            return;
        }
        final BMXFileAttachment body = BMXFileAttachment.dynamic_cast(bean.attachment());
        if (body == null) {
            return;
        }
        String filePath = null;
        if (!TextUtils.isEmpty(body.path()) && new File(body.path()).exists()) {
            filePath = body.path();
        } else {
            Log.i(TAG, "local path is null");
        }
        if (!TextUtils.isEmpty(filePath)) {
            openFilePreView(filePath);
            return;
        }
        ToastUtil.showTextViewPrompt(mView.getContext().getString(R.string.downloading));
        BMXMessageAttachment.DownloadStatus status = body.downloadStatus();
        if (status == BMXMessageAttachment.DownloadStatus.Downloaing) {
            return;
        }
        ChatManager.getInstance().downloadAttachment(bean);
    }

    /**
     * 图片item点击
     *
     * @param bean 消息体
     */
    private void onImageItemClick(final BMXMessage bean) {
        if (mView == null || bean == null || bean.contentType() != BMXMessage.ContentType.Image) {
            return;
        }
        BMXImageAttachment body = BMXImageAttachment.dynamic_cast(bean.attachment());
        if (body == null) {
            return;
        }
        String picUrl = null;
        if (!TextUtils.isEmpty(body.thumbnailPath()) && new File(body.thumbnailPath()).exists()) {
            picUrl = body.thumbnailPath();
        } else if (!TextUtils.isEmpty(body.path()) && new File(body.path()).exists()) {
            picUrl = body.path();
        } else if (!TextUtils.isEmpty(body.thumbnailUrl())) {
            picUrl = body.thumbnailUrl();
        } else if (!TextUtils.isEmpty(body.url())) {
            picUrl = body.url();
        }
        if (TextUtils.isEmpty(picUrl)) {
            // 正在下载
            return;
        }
        List<PhotoViewBean> photoViewBeans = new ArrayList<>();
        PhotoViewBean photoViewBean = new PhotoViewBean();
        photoViewBean.setLocalPath(body.path());
        photoViewBean.setThumbLocalPath(body.thumbnailPath());
        photoViewBean.setThumbHttpUrl(body.thumbnailUrl());
        photoViewBean.setHttpUrl(body.url());
        photoViewBeans.add(photoViewBean);
        PhotoViewListBean listBean = new PhotoViewListBean();
        listBean.setPhotoViewBeans(photoViewBeans);
        PhotoDetailActivity.openPhotoDetail(mView.getContext(), listBean);
    }

    /**
     * 文件预览
     *
     * @param path 路径
     */
    private void openFilePreView(String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = null;
        Context context = mView.getContext();
        if (context == null) {
            return;
        }
        File file = new File(path);
        if (TextUtils.isEmpty(path) || !file.exists()) {
            ToastUtil.showTextViewPrompt(
                    context.getResources().getString(R.string.chat_file_not_exit));
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                uri = FileProvider.getUriForFile(context,
                        context.getPackageName() + ".fileProvider", file);
            } else {
                uri = Uri.fromFile(file);
            }
        } catch (Exception e) {
            ToastUtil.showTextViewPrompt(
                    context.getResources().getString(R.string.chat_file_not_exit));
            return;
        }

        intent.setData(uri);
        ComponentName componentName = intent.resolveActivity(context.getPackageManager());
        if (componentName != null) {
            context.startActivity(intent);
        } else {
            ToastUtil.showTextViewPrompt(
                    context.getResources().getString(R.string.chat_file_not_open));
        }
    }

    /**
     * 视频预览
     */
    private void openVideoItemClick(BMXMessage bean) {
        if (mView == null || bean == null || bean.contentType() != BMXMessage.ContentType.Video) {
            return;
        }
        BMXVideoAttachment body = BMXVideoAttachment.dynamic_cast(bean.attachment());
        if (body == null) {
            return;
        }
        String videoUrl = null;
        if (!TextUtils.isEmpty(body.path()) && new File(body.path()).exists()) {
            videoUrl = body.path();
        }
        if (TextUtils.isEmpty(videoUrl)) {
            // 正在下载
            ToastUtil.showTextViewPrompt(mView.getContext().getString(R.string.downloading));
            return;
        }
        BMXMessageAttachment.DownloadStatus status = body.downloadStatus();
        if (status == BMXMessageAttachment.DownloadStatus.Downloaing) {
            ToastUtil.showTextViewPrompt(mView.getContext().getString(R.string.downloading));
            return;
        }
        VideoDetailActivity.openVideoDetail(mView.getContext(), videoUrl);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IMAGE_REQUEST:
                // 相册
                if (resultCode == Activity.RESULT_OK && data != null) {
                    try {
                        Uri selectedImage = data.getData(); // 获取系统返回的照片的Uri
                        String path = FileUtils.getFilePathByUri(selectedImage);
                        int[] size = ChatUtils.getInstance().getImageSize(path);
                        mView.sendChatMessage(mSendUtils.sendImageMessage(mChatType, mMyUserId,
                                mChatId, path, size[0], size[1]));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CAMERA_REQUEST:
                // 拍照
                if (resultCode == Activity.RESULT_OK) {
                    if (!TextUtils.isEmpty(mCameraPath) && new File(mCameraPath).exists()) {
                        File f = new File(mCameraPath);
                        mView.getContext().sendBroadcast(new Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + f)));
                        int[] size = ChatUtils.getInstance().getImageSize(mCameraPath);
                        mView.sendChatMessage(mSendUtils.sendImageMessage(mChatType, mMyUserId,
                                mChatId, mCameraPath, size[0], size[1]));
                    }
                }
                break;
            case VIDEO_REQUEST:
                // 视频
                if (resultCode == Activity.RESULT_OK && data != null) {
                    int type = data.getIntExtra(PhotoRecorderActivity.EXTRA_RECORD_TYPE, -1);
                    if (type == 1) {
                        String videoPath = data
                                .getStringExtra(PhotoRecorderActivity.EXTRA_VIDEO_PATH);
                        if (TextUtils.isEmpty(videoPath) || !new File(videoPath).exists()
                                || new File(videoPath).length() <= 0) {
                            ToastUtil.showTextViewPrompt(mView.getContext().getString(R.string.video_recording_failed));
                            return;
                        }
                        int videoDuration = data
                                .getIntExtra(PhotoRecorderActivity.EXTRA_VIDEO_DURATION, 0);
                        int width = data.getIntExtra("width", 0);
                        int height = data.getIntExtra("height", 0);
                        mView.sendChatMessage(mSendUtils.sendVideoMessage(mChatType, mMyUserId,
                                mChatId, videoPath, videoDuration, width, height));
                    } else if (type == 2) {
                        String picturePath = data
                                .getStringExtra(PhotoRecorderActivity.EXTRA_CAMERA_PATH);
                        if (TextUtils.isEmpty(picturePath)) {
                            ToastUtil.showTextViewPrompt(mView.getContext().getString(R.string.photo_capture_failed));
                            return;
                        }
                        File f = new File(picturePath);
                        mView.getContext().sendBroadcast(new Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + f)));
                        int[] size = ChatUtils.getInstance().getImageSize(picturePath);
                        mView.sendChatMessage(mSendUtils.sendImageMessage(mChatType, mMyUserId,
                                mChatId, picturePath, size[0], size[1]));
                    }
                }
                break;
            case FILE_REQUEST:
                // 文件
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        List<FileBean> beans = (List<FileBean>)data
                                .getSerializableExtra(ChooseFileActivity.CHOOSE_FILE_DATA);
                        if (beans == null || beans.isEmpty()) {
                            return;
                        }
                        for (FileBean bean : beans) {
                            mView.sendChatMessage(mSendUtils.sendFileMessage(mChatType, mMyUserId,
                                    mChatId, bean.getPath(), bean.getDesc()));
                        }
                    }
                }
                break;
            case FORWARD_REQUEST:
                // 转发
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        BMXMessage.MessageType type = (BMXMessage.MessageType)data
                                .getSerializableExtra(MessageConfig.CHAT_TYPE);
                        MessageBean messageBean = (MessageBean)data
                                .getSerializableExtra(MessageConfig.CHAT_MSG);
                        long chatId = data.getLongExtra(MessageConfig.CHAT_ID, 0);
                        if (messageBean != null && chatId > 0 && mSendUtils != null) {
                            if (mChatType == type && mChatId == chatId) {
                                // 转发给当前会话
                                if (mView != null) {
                                    mView.sendChatMessage(mSendUtils.forwardMessage(messageBean,
                                            type, mMyUserId, chatId));
                                }
                            } else {
                                mSendUtils.forwardMessage(messageBean, type, mMyUserId, chatId);
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void registerSensor() {
        if (mVoicePlayHelper != null) {
            mVoicePlayHelper.registerListener();
        }
    }

    @Override
    public void unRegisterSensor() {
        if (mVoicePlayHelper != null) {
            mVoicePlayHelper.unRegisterListener();
        }
    }

    @Override
    public Map<String, String> getChatAtMembers() {
        return null;
    }

    @Override
    public void clearAtFeed() {

    }

    @Override
    public void readAllMessage() {
        Observable.just("").map(new Func1<String, String>() {
            @Override
            public String call(String s) {
                // 获取当前最后一条消息
                BMXMessage message = mView.getLastMessage();
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
                        Log.d("ChatBasePresenter", "readAllMessage is success");
                    }
                });
    }

    /**
     * 是否有权限
     *
     * @param permissions 权限列表
     * @return 是否具有所有权限
     */
    boolean hasPermission(String... permissions) {
        if (mView.getContext() instanceof PermissionActivity) {
            PermissionActivity activity = (PermissionActivity)mView.getContext();
            return activity.hasPermission(permissions);
        } else {
            throw new IllegalArgumentException("is not allow request permission");
        }
    }

    /**
     * 请求权限
     *
     * @param requestType 权限请求类型
     * @param permissions 权限列表
     * @return 是否具有所有权限
     */
    void requestPermissions(final int requestType, final String... permissions) {
        if (!(mView.getContext() instanceof PermissionActivity)) {
            Log.d(TAG, "activity is not PermissionActivity");
            return;
        }
        PermissionsMgr.getInstance().requestPermissionsIfNecessaryForResult(
                (Activity)mView.getContext(), permissions, new PermissionsResultAction() {

                    @Override
                    public void onGranted(List<String> perms) {
                        Log.d(TAG, "Permission is Granted:" + perms);
                        onGrantedPermission(requestType, perms);
                    }

                    @Override
                    public void onDenied(List<String> perms) {
                        Log.d(TAG, "Permission is Denied" + perms);
                        onDeniedPermission(requestType, perms);
                    }
                });
    }

    /**
     * 权限接受
     *
     * @param requestType 请求权限类型
     * @param permissions 权限接受的列表
     */
    private void onGrantedPermission(int requestType, List<String> permissions) {
        if (permissions == null || permissions.size() == 0) {
            return;
        }
        for (String permission : permissions) {
            switch (permission) {
                case PermissionsConstant.READ_STORAGE:
                    // 读SD权限
                    if (hasPermission(PermissionsConstant.WRITE_STORAGE)) {
                        // 如果有读写权限都有 则直接操作
                        hasPermissionHandler(requestType);
                    } else {
                        requestPermissions(requestType, PermissionsConstant.WRITE_STORAGE);
                    }
                    break;
                case PermissionsConstant.WRITE_STORAGE:
                    // 写SD权限 如果有读写权限都有 则直接操作
                    hasPermissionHandler(requestType);
                    break;
                case PermissionsConstant.CAMERA:
                    if (requestType == TYPE_CAMERA_PERMISSION) {
                        // 拍照
                        takePic();
                    } else if (requestType == TYPE_VIDEO_PERMISSION) {
                        // 视频
                        if (hasPermission(PermissionsConstant.RECORD_AUDIO)) {
                            showVideoView();
                        } else {
                            requestPermissions(requestType, PermissionsConstant.RECORD_AUDIO);
                        }
                    } else if (requestType == TYPE_VIDEO_CALL_PERMISSION) {
                        //视频通话
                        if (hasPermission(PermissionsConstant.RECORD_AUDIO)) {
                            handelVideoCall(true);
                        } else {
                            requestPermissions(requestType, PermissionsConstant.RECORD_AUDIO);
                        }
                    }
                    break;
                case PermissionsConstant.RECORD_AUDIO:
                    if (requestType == TYPE_VOICE_PERMISSION) {
                        try {
                            recordMedia(mRecordVoiceAction, mRecordVoiceTime);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (requestType == TYPE_VIDEO_PERMISSION) {
                        // 视频
                        showVideoView();
                    } else if (requestType == TYPE_VIDEO_CALL_PERMISSION) {
                        // 视频通话
                        handelVideoCall(true);
                    } else if (requestType == TYPE_AUDIO_CALL_PERMISSION) {
                        // 音频通话
                        handelVideoCall(false);
                    }
                    break;
                case PermissionsConstant.FINE_LOCATION:
                    // 定位权限
                    if (hasPermission(PermissionsConstant.COARSE_LOCATION)) {
                        hasPermissionHandler(requestType);
                    } else {
                        requestPermissions(requestType, PermissionsConstant.COARSE_LOCATION);
                    }
                    break;
                case PermissionsConstant.COARSE_LOCATION:
                    hasPermissionHandler(requestType);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 权限拒绝
     *
     * @param requestType 请求权限类型
     * @param permissions 被拒绝的权限
     */
    private void onDeniedPermission(int requestType, List<String> permissions) {
        if (permissions == null || permissions.size() == 0) {
            return;
        }
        for (String permission : permissions) {
            switch (permission) {
                case PermissionsConstant.READ_STORAGE:
                case PermissionsConstant.WRITE_STORAGE:
                    // 读写SD权限拒绝
                    CommonProvider.openAppPermission((Activity)mView.getContext());
                    break;
                case PermissionsConstant.CAMERA:
                    if (requestType == TYPE_CAMERA_PERMISSION) {
                        // 拍照权限拒绝
                        ToastUtil.showTextViewPrompt(mView.getContext()
                                .getString(R.string.camera_fail_check_permission));
                    } else if (requestType == TYPE_VIDEO_PERMISSION) {
                        ToastUtil.showTextViewPrompt(
                                mView.getContext().getString(R.string.video_fail_check_permission));
                    } else if (requestType == TYPE_VIDEO_CALL_PERMISSION) {
                        ToastUtil.showTextViewPrompt(
                                mView.getContext().getString(R.string.video_fail_check_permission));
                    }
                    break;
                case PermissionsConstant.RECORD_AUDIO:
                    if (requestType == TYPE_VOICE_PERMISSION) {
                        // 语音权限拒绝
                        ToastUtil.showTextViewPrompt(mView.getContext()
                                .getString(R.string.record_fail_check_permission));
                    } else if (requestType == TYPE_VIDEO_PERMISSION) {
                        ToastUtil.showTextViewPrompt(
                                mView.getContext().getString(R.string.video_fail_check_permission));
                    } else if (requestType == TYPE_VIDEO_CALL_PERMISSION) {
                        ToastUtil.showTextViewPrompt(
                                mView.getContext().getString(R.string.video_fail_check_permission));
                    } else if (requestType == TYPE_AUDIO_CALL_PERMISSION) {
                        ToastUtil.showTextViewPrompt(mView.getContext()
                                .getString(R.string.record_fail_check_permission));
                    }
                    break;
                case PermissionsConstant.FINE_LOCATION:
                case PermissionsConstant.COARSE_LOCATION:
                    if (requestType == TYPE_LOCATION_PERMISSION) {
                        // 定位权限拒绝
                        ToastUtil.showTextViewPrompt(mView.getContext()
                                .getString(R.string.location_fail_check_permission));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 获取到权限之后的操作
     *
     * @param requestType 请求的权限类型
     */
    private void hasPermissionHandler(int requestType) {
        switch (requestType) {
            case TYPE_CAMERA_PERMISSION:
                // 拍照
                if (hasPermission(PermissionsConstant.CAMERA)) {
                    takePic();
                } else {
                    requestPermissions(requestType, PermissionsConstant.CAMERA);
                }
                break;
            case TYPE_PHOTO_PERMISSION:
                // 相册
                CameraUtils.getInstance().takeGalley((Activity)mView.getContext(), IMAGE_REQUEST);
                break;
            case TYPE_VOICE_PERMISSION:
                // 语音权限
                try {
                    if (hasPermission(PermissionsConstant.RECORD_AUDIO)) {
                        recordMedia(mRecordVoiceAction, mRecordVoiceTime);
                    } else {
                        requestPermissions(requestType, PermissionsConstant.RECORD_AUDIO);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "recordMedia is failed:" + e);
                }
                break;
            case TYPE_FILE_PERMISSION:
                // 文件
                chooseFile();
                break;
            case TYPE_LOCATION_PERMISSION:
                if (hasPermission(PermissionsConstant.COARSE_LOCATION)) {
                    sendCurrentLocation();
                } else {
                    requestPermissions(requestType, PermissionsConstant.COARSE_LOCATION);
                }
                break;
            case TYPE_VIDEO_PERMISSION:
                // 视频
                if (hasPermission(PermissionsConstant.CAMERA)) {
                    // 视频
                    if (hasPermission(PermissionsConstant.RECORD_AUDIO)) {
                        showVideoView();
                    } else {
                        requestPermissions(requestType, PermissionsConstant.RECORD_AUDIO);
                    }
                } else {
                    requestPermissions(requestType, PermissionsConstant.CAMERA);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 拍照
     */
    private void takePic() {
        // 拍照
        if (!TextUtils.isEmpty(mCameraName)) {
            mCameraName = null;
        }
        if (!TextUtils.isEmpty(mCameraDir)) {
            mCameraDir = null;
        }
        if (!TextUtils.isEmpty(mCameraPath)) {
            mCameraPath = null;
        }
        mCameraName = CameraUtils.getInstance().getCameraName();
        mCameraDir = FileConfig.DIR_APP_CACHE_CAMERA + "/";
        mCameraPath = FileConfig.DIR_APP_CACHE_CAMERA + "/" + mCameraName + ".jpg";
        CameraUtils.getInstance().takePhoto(mCameraDir, mCameraPath, (Activity)mView.getContext(),
                CAMERA_REQUEST);
    }

    /**
     * 录制视频
     */
    private void showVideoView() {
        Intent it = new Intent(mView.getContext(), PhotoRecorderActivity.class);
        ((Activity)mView.getContext()).startActivityForResult(it, VIDEO_REQUEST);
    }

    /**
     * 选择文件
     */
    private void chooseFile() {
        ChooseFileActivity.openChooseFileActivity(mView.getContext(), FILE_REQUEST);
    }

    /**
     * 视频通话
     */
    protected void showVideoCallDialog() {
        final CustomDialog dialog = new CustomDialog();
        LinearLayout ll = new LinearLayout(mView.getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // 视频通话
        TextView video = new TextView(mView.getContext());
        video.setPadding(ScreenUtils.dp2px(15), 0, ScreenUtils.dp2px(15), 0);
        video.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        video.setTextColor(mView.getContext().getResources().getColor(R.color.color_black));
        video.setBackgroundColor(mView.getContext().getResources().getColor(R.color.color_white));
        video.setText(mView.getContext().getString(R.string.call_video));
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // 视频需要摄像头 麦克风权限
                if (hasPermission(PermissionsConstant.CAMERA, PermissionsConstant.RECORD_AUDIO)) {
                    handelVideoCall(true);
                } else {
                    // 如果没有权限 首先请求SD读权限
                    requestPermissions(TYPE_VIDEO_CALL_PERMISSION, PermissionsConstant.CAMERA);
                }
            }
        });
        ll.addView(video, params);
        // 语音通话
        TextView audio = new TextView(mView.getContext());
        audio.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), 0);
        audio.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        audio.setTextColor(mView.getContext().getResources().getColor(R.color.color_black));
        audio.setBackgroundColor(mView.getContext().getResources().getColor(R.color.color_white));
        audio.setText(mView.getContext().getString(R.string.call_audio));
        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // 音频需要麦克风权限
                if (hasPermission(PermissionsConstant.RECORD_AUDIO)) {
                    handelVideoCall(false);
                } else {
                    // 如果没有权限 首先请求SD读权限
                    requestPermissions(TYPE_AUDIO_CALL_PERMISSION, PermissionsConstant.RECORD_AUDIO);
                }
            }
        });
        ll.addView(audio, params);
        dialog.setCustomView(ll);
        dialog.showDialog((Activity) mView.getContext());
    }

    /**
     * 获取当前位置
     */
    private void sendCurrentLocation() {
        LocationManager manager = mView == null ? null
                : (LocationManager)mView.getContext().getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            return;
        }
        String provider = "";
        List<String> providers = manager.getProviders(true);
        if (providers == null) {
            ToastUtil.showTextViewPrompt(mView.getContext().getString(R.string.no_card_inserted));
            return;
        }
        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.PASSIVE_PROVIDER)) {
            provider = LocationManager.PASSIVE_PROVIDER;
        }
        if (!TextUtils.isEmpty(provider)) {
            Location location;
            if (ActivityCompat.checkSelfPermission(mView.getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mView.getContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                location = null;
            } else {
                location = manager.getLastKnownLocation(provider);
                if (location != null) {
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    String add = "";
                    Geocoder geocoder = new Geocoder(mView.getContext(), Locale.CHINESE);
                    try {
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (!addresses.isEmpty()){
                            Address address = addresses.get(0);
                            // Address[addressLines=[0:"中国",1:"北京市海淀区",2:"华奥饭店公司写字间中关村创业大街"]latitude=39.980973,hasLongitude=true,longitude=116.301712]
                            int maxLine = address.getMaxAddressLineIndex();
                            if (maxLine >= 2) {
                                add = address.getAddressLine(1) + address.getAddressLine(2);
                            } else if (maxLine >= 1) {
                                add = address.getAddressLine(0) + address.getAddressLine(1);
                            } else if (maxLine >= 0) {
                                add = address.getAddressLine(0);
                            }
                            if (!TextUtils.isEmpty(add)) {
                                showSendLocationDialog(add, latitude, longitude);
                            } else {
                                ToastUtil.showTextViewPrompt(mView.getContext().getString(R.string.failed_to_get_location));
                            }
                        }else{
                            ToastUtil.showTextViewPrompt(mView.getContext().getString(R.string.failed_to_get_location));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtil.showTextViewPrompt(mView.getContext().getString(R.string.failed_to_get_location));
                    }
                } else {
                    ToastUtil.showTextViewPrompt(mView.getContext().getString(R.string.no_card_inserted));
                }
            }
        } else {
            ToastUtil.showTextViewPrompt(mView.getContext().getString(R.string.no_card_inserted));
        }
    }

    private void showSendLocationDialog(String address, double latitude, double longitude) {
        DialogUtils.getInstance().showDialog((Activity)mView.getContext(), ((Activity) mView.getContext()).getString(R.string.confirm_to_send), address,
                new CommonDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener() {
                        mView.sendChatMessage(mSendUtils.sendLocationMessage(mChatType, mMyUserId,
                                mChatId, latitude, longitude, address));
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    @Override
    public void onDestroyPresenter() {
        if (mConversation != null) {
            mConversation = null;
        }
        if (mSubcription != null) {
            mSubcription.unsubscribe();
            mSubcription = null;
        }
        ChatManager.getInstance().removeChatListener(mListener);
    }

    /**
     * 是否当前会话
     * 
     * @return boolean
     */
    protected boolean isCurrentSession(BMXMessage message) {
        return false;
    }

    /**
     * 筛选展示的信息
     * 
     * @param message
     * @return boolean
     */
    protected boolean filterMessage(BMXMessage message) {
        if (message == null) {
            return false;
        }
        if (message.contentType() == BMXMessage.ContentType.Text
                && !TextUtils.isEmpty(message.extension())) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(message.extension());
                if (jsonObject.has(MessageConfig.INPUT_STATUS) && message.fromId() != mMyUserId) {
                    handelInputStatus(message.extension());
                }
                //TODO
//                if (jsonObject.has("rtcKey") && jsonObject.has("rtcValue")) {
//                    if (TextUtils.equals(jsonObject.getString("rtcKey"), "join") && !TextUtils.isEmpty(jsonObject.getString("rtcValue"))) {
//                        String[] values = jsonObject.getString("rtcValue").split("_");
//                        String roomId = values[0];
//                        String[] chatIdArray = values[1].split(",");
//                        boolean hasVideo = TextUtils.equals(MessageConfig.CallMode.CALL_VIDEO+"", values[2]);
//                        List<Long> chatIds = new ArrayList<>();
//                        for (String id : chatIdArray){
//                            chatIds.add(Long.valueOf(id));
//                        }
//                        receiveVideoCall(roomId, chatIds, hasVideo);
//                    }
//                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (jsonObject.has(MessageConfig.INPUT_STATUS)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 处理inputStatus
     */
    protected void handelInputStatus(String extension) {
    }

    /**
     * 音视频  是否有视频
     */
    protected void handelVideoCall(boolean hasVideo) {
    }

    /**
     * 收到音视频
     */
    protected void receiveVideoCall(long roomId, List<Long> chatIds, boolean hasVideo) {
    }
}
