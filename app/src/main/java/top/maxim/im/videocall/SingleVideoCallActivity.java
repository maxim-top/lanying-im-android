package top.maxim.im.videocall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import im.floo.floolib.BMXChatServiceListener;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageConfig;
import im.floo.floolib.BMXMessageList;
import im.floo.floolib.BMXRTCEngine;
import im.floo.floolib.BMXRTCEngineListener;
import im.floo.floolib.BMXRTCServiceListener;
import im.floo.floolib.BMXRoomAuth;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXStream;
import im.floo.floolib.BMXVideoCanvas;
import im.floo.floolib.BMXVideoConfig;
import im.floo.floolib.BMXVideoMediaType;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.ChatManager;
import top.maxim.im.bmxmanager.RTCManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.WeakHandler;
import top.maxim.im.common.utils.permissions.PermissionsConstant;
import top.maxim.im.common.utils.permissions.PermissionsMgr;
import top.maxim.im.common.utils.permissions.PermissionsResultAction;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;
import top.maxim.im.sdk.utils.MessageSendUtils;
import top.maxim.im.videocall.utils.EngineConfig;
import top.maxim.rtc.view.BMXRtcRenderView;
import top.maxim.rtc.view.RTCRenderView;

/**
 * Description 单视频会话
 */
public class SingleVideoCallActivity extends BaseTitleActivity {

    private static final String TAG = "SingleVideoCallActivity";

    public static void openVideoCall(Context context, long chatId, long roomId, String callId, boolean isInitiator,
                                     BMXMessageConfig.RTCCallType callType, String pin) {
        Intent intent = new Intent(context, SingleVideoCallActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, chatId);
        intent.putExtra(MessageConfig.CALL_TYPE, callType.swigValue());
        intent.putExtra(MessageConfig.RTC_ROOM_ID, roomId);
        intent.putExtra(MessageConfig.IS_INITIATOR, isInitiator);
        intent.putExtra(MessageConfig.PIN, pin);
        intent.putExtra(MessageConfig.CALL_ID, callId);
        context.startActivity(intent);
    }

    private ViewGroup mVideoContainer;

    private BMXRtcRenderView mLocalView;

    private BMXRtcRenderView mRemoteView;

    private ViewGroup mAudioContainer;

    private long mChatId;

    private long mUserId;

    private long mRoomId;

    private String mCallId;

    private String mPin;

    private long mPickupTimestamp;

    //是否是发起者
    private boolean mIsInitiator;

    private BMXRosterItem mRosterItem = new BMXRosterItem();

    //默认音频
    private BMXMessageConfig.RTCCallType mCallType = BMXMessageConfig.RTCCallType.AudioCall;

    private boolean mHasVideo = false;

    //扬声器
//    private boolean mSpeaker = EngineConfig.SWITCH_SPEAKER;
    private boolean mSpeaker = true;

    //麦克风
    private boolean mMic = true;

    private CallHandler mHandler;

    private BMXRTCEngine mEngine;

    private BMXRTCEngineListener mListener;

    private MessageSendUtils mSendUtils;

    private BMXChatServiceListener mChatListener = new BMXChatServiceListener(){
        @Override
        public void onReceive(BMXMessageList list) {
            super.onReceive(list);
            // 收到消息
            if (list != null && !list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    BMXMessage message = list.get(i);
                    handleRTCMessage(message);
                }
            }
        }
    };

    private BMXRTCServiceListener mRTCListener = new BMXRTCServiceListener(){

        public void onRTCCallMessageReceive(BMXMessage msg) {

        }

        public void onRTCPickupMessageReceive(BMXMessage msg) {
            if (msg.config().getRTCCallId().equals(mCallId) && msg.fromId() == mUserId){
                leaveRoom();
            }
        }

        public void onRTCHangupMessageReceive(BMXMessage msg) {
            if (msg.config().getRTCCallId().equals(mCallId)){
                leaveRoom();
            }
        }

    };

    private long getTimeStamp(){
        return new Date().getTime();
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        return new Header.Builder(this, headerContainer).build();
    }

    @Override
    protected void setStatusBar() {
        super.setStatusBar();
        mStatusBar.setVisibility(View.GONE);
    }

    @Override
    protected View onCreateView() {
        hideStatusBar();
        View view = View.inflate(this, R.layout.activity_single_video_call, null);
        mVideoContainer = view.findViewById(R.id.layout_video_container);
        mAudioContainer = view.findViewById(R.id.layout_audio_container);
        mHandler = new CallHandler(this);
        mSendUtils = new MessageSendUtils();
        return view;
    }

    /**
     * 设置全屏展示 隐藏状态栏 底部导航栏
     */
    private void hideStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final View decorView = getWindow().getDecorView();
            doFullScreen(decorView);
            decorView.setOnSystemUiVisibilityChangeListener(
                    new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                decorView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        doFullScreen(decorView);
                                    }
                                }, 500);
                            }
                        }
                    });
        }
        hideHeader();
    }

    /**
     * 设置全屏
     *
     * @param decorView
     */
    private void doFullScreen(View decorView) {
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mChatId = intent.getLongExtra(MessageConfig.CHAT_ID, 0);
            mCallType = BMXMessageConfig.RTCCallType.swigToEnum( intent.getIntExtra(MessageConfig.CALL_TYPE, 0));
            mRoomId = intent.getLongExtra(MessageConfig.RTC_ROOM_ID, 0);
            mIsInitiator = intent.getBooleanExtra(MessageConfig.IS_INITIATOR, false);
            mPin = intent.getStringExtra(MessageConfig.PIN);
            mCallId = intent.getStringExtra(MessageConfig.CALL_ID);
        }
        mUserId = SharePreferenceUtils.getInstance().getUserId();
        mHasVideo = mCallType == BMXMessageConfig.RTCCallType.VideoCall;
        if (mIsInitiator){
            mPin = UUID.randomUUID().toString();
        }
    }

    private void initRtc() {
        mEngine = RTCManager.getInstance().getRTCEngine();
        mEngine.addRTCEngineListener(mListener = new BMXRTCEngineListener() {

            @Override
            public void onJoinRoom(String info, long roomId, BMXErrorCode error) {
                super.onJoinRoom(info, roomId, error);
                mRoomId = roomId;
                if (BaseManager.bmxFinish(error)) {
                    mEngine.publish(BMXVideoMediaType.Camera, mHasVideo, true);
                    Log.e(TAG, "加入房间成功 开启发布本地流, roomId= " + roomId + "msg = " + info);
                } else {
                    Log.e(TAG, "加入房间失败 roomId= " + roomId + "msg = " + info);
                }
            }

            @Override
            public void onLeaveRoom(String info, long roomId, BMXErrorCode error, String reason) {
                super.onLeaveRoom(info, roomId, error, reason);
                if (BaseManager.bmxFinish(error)) {
                    Log.e(TAG, "离开房间成功 roomId= " + roomId + "msg = " + reason);
                }else{
                    Log.e(TAG, "离开房间失败 roomId= " + roomId + "msg = " + reason);
                }
            }

            @Override
            public void onReJoinRoom(String info, long roomId, BMXErrorCode error) {
                super.onReJoinRoom(info, roomId, error);
            }

            @Override
            public void onMemberJoined(long roomId, long usedId) {
                super.onMemberJoined(roomId, usedId);
                Log.e(TAG, "远端用户加入 uid= " + usedId);
            }

            @Override
            public void onMemberExited(long roomId, long usedId, String reason) {
                super.onMemberExited(roomId, usedId, reason);
                Log.e(TAG, "远端用户离开 uid= " + usedId);
                onRemoteLeave();
            }

            @Override
            public void onLocalPublish(BMXStream stream, String info, BMXErrorCode error) {
                super.onLocalPublish(stream, info, error);
                if (BaseManager.bmxFinish(error)) {
                    onUserJoin(stream);
                    Log.e(TAG, "发布本地流成功 开启预览 msg = " + info);
                }else{
                    Log.e(TAG, "发布本地流失败 msg = " + info);
                }
            }

            @Override
            public void onLocalUnPublish(BMXStream stream, String info, BMXErrorCode error) {
                super.onLocalUnPublish(stream, info, error);
                if (BaseManager.bmxFinish(error)) {
                    Log.e(TAG, "停止发布本地流成功 msg = " + info);
                }else{
                    Log.e(TAG, "停止发布本地流失败 msg = " + info);
                }
            }

            @Override
            public void onRemotePublish(BMXStream stream, String info, BMXErrorCode error) {
                super.onRemotePublish(stream, info, error);
                if (BaseManager.bmxFinish(error)) {
                    mEngine.subscribe(stream);
                    Log.e(TAG, "远端发布流 开启订阅");
                }else{
                    Log.e(TAG, "远端发布流失败 msg = " + info);
                }
            }

            @Override
            public void onRemoteUnPublish(BMXStream stream, String info, BMXErrorCode error) {
                super.onRemoteUnPublish(stream, info, error);
                if (BaseManager.bmxFinish(error)) {
                    Log.e(TAG, "远端取消发布流");
                    BMXVideoCanvas canvas = new BMXVideoCanvas();
                    canvas.setMStream(stream);
                    mEngine.stopRemoteView(canvas);
                    mEngine.unSubscribe(stream);
                    onRemoteLeave();
                }else{
                    Log.e(TAG, "远端取消发布流失败 msg = " + info);
                }
            }

            @Override
            public void onSubscribe(BMXStream stream, String info, BMXErrorCode error) {
                super.onSubscribe(stream, info, error);
                if (BaseManager.bmxFinish(error)) {
                    onRemoteJoin(stream);
                    mPickupTimestamp = getTimeStamp();
                    Log.e(TAG, "订阅远端流成功 msg = " + info);
                } else {
                    Log.e(TAG, "订阅远端流失败 msg = " + info);
                }
            }

            @Override
            public void onUnSubscribe(BMXStream stream, String info, BMXErrorCode error) {
                super.onUnSubscribe(stream, info, error);
                if (BaseManager.bmxFinish(error)) {
                    Log.e(TAG, "取消订阅远端流成功, 开启预览 msg = " + info);
                } else {
                    Log.e(TAG, "取消订阅远端流失败 msg = " + info);
                }
            }

            @Override
            public void onLocalAudioLevel(int volume) {
                super.onLocalAudioLevel(volume);
            }

            @Override
            public void onRemoteAudioLevel(long userId, int volume) {
                super.onRemoteAudioLevel(userId, volume);
            }

            @Override
            public void onKickoff(String info, BMXErrorCode error) {
                super.onKickoff(info, error);
            }

            @Override
            public void onWarning(String info, BMXErrorCode error) {
                super.onWarning(info, error);
            }

            @Override
            public void onError(String info, BMXErrorCode error) {
                super.onError(info, error);
            }

            @Override
            public void onNetworkQuality(BMXStream stream, String info, BMXErrorCode error) {
                super.onNetworkQuality(stream, info, error);
            }
        });
        //获取之前配置初始化
        if (mHasVideo) {
            BMXVideoConfig config = new BMXVideoConfig();
            config.setProfile(EngineConfig.VIDEO_PROFILE);
            mEngine.setVideoProfile(config);
//            mEngine.setAudioProfile(true);//视频默认开启扬声器
//            mEngine.setAudioOnlyMode(false);
        } else {
//            mEngine.setAudioProfile(mSpeaker);
//            mEngine.setAudioOnlyMode(true);
        }
        if (mIsInitiator) {
            joinRoom();
        }
    }

    /**
     * 添加本地视频view
     */
    private void addRemoteView() {
        ViewGroup localParent = mVideoContainer.findViewById(R.id.video_view_container_large);
        localParent.setVisibility(View.VISIBLE);
        mRemoteView = new RTCRenderView(this);
        mRemoteView.init();
        localParent.addView(mRemoteView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    /**
     * 添加远端视频view
     */
    private void addLocalView() {
        ViewGroup remoteParent = mVideoContainer.findViewById(R.id.video_view_container_small);
        remoteParent.setVisibility(View.VISIBLE);
        mLocalView = new RTCRenderView(this);
        mLocalView.init();
        mLocalView.getSurfaceView().setZOrderMediaOverlay(true);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        remoteParent.addView(mLocalView, layoutParams);
    }

    /**
     * 移除本地视频view
     */
    private void removeLocalView() {
        if (mLocalView != null) {
            mLocalView.release();
        }
        ViewGroup localParent = mVideoContainer.findViewById(R.id.video_view_container_small);
        localParent.removeAllViews();
    }

    /**
     * 移除远端视频view
     */
    private void removeRemoteView() {
        if (mRemoteView != null) {
            mRemoteView.release();
        }
        ViewGroup remoteParent = mVideoContainer.findViewById(R.id.video_view_container_large);
        remoteParent.removeAllViews();
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        ChatManager.getInstance().addChatListener(mChatListener);
        RTCManager.getInstance().addRTCServiceListener(mRTCListener);
        initRoster();
    }

    private void initRoster() {
        RosterManager.getInstance().getRosterList(mChatId, false, (bmxErrorCode, bmxRosterItem) -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                mRosterItem = bmxRosterItem;
                initCallView();
            } else {
                RosterManager.getInstance().getRosterList(mChatId, true,
                        (bmxErrorCode1, bmxRosterItem1) -> {
                            if (!BaseManager.bmxFinish(bmxErrorCode)) {
                                return;
                            }
                            RosterFetcher.getFetcher().putRoster(bmxRosterItem1);
                            mRosterItem = bmxRosterItem1;
                            initCallView();
                        });
            }
        });
    }

    /**
     * 发起或接受音视频view
     */
    private void initCallView(){
        if (mHasVideo) {
            // 视频
            hideAudioPeerInfo();
            showVideoPeerInfo();
        } else {
            // 音频
            hideVideoPeerInfo();
            showAudioPeerInfo();
        }
        if (mIsInitiator) {
            onInitiateCall();
        } else {
            onRecipientCall();
        }
        checkPermission();
    }

    private void checkPermission() {
        if (hasPermission(PermissionsConstant.RECORD_AUDIO)) {
            if (mHasVideo) {
                if (hasPermission(PermissionsConstant.CAMERA)) {
                    initRtc();
                } else {
                    PermissionsMgr.getInstance().requestPermissionsIfNecessaryForResult(
                            this, new String[]{PermissionsConstant.CAMERA}, new PermissionsResultAction() {

                                @Override
                                public void onGranted(List<String> perms) {
                                    initRtc();
                                }

                                @Override
                                public void onDenied(List<String> perms) {
                                    deniedPermission();
                                }
                            });
                }
            } else {
                initRtc();
            }
        } else {
            PermissionsMgr.getInstance().requestPermissionsIfNecessaryForResult(
                    this, new String[]{PermissionsConstant.RECORD_AUDIO}, new PermissionsResultAction() {

                        @Override
                        public void onGranted(List<String> perms) {
                            if (mHasVideo) {
                                if (hasPermission(PermissionsConstant.CAMERA)) {
                                    initRtc();
                                } else {
                                    PermissionsMgr.getInstance().requestPermissionsIfNecessaryForResult(
                                            SingleVideoCallActivity.this, new String[]{PermissionsConstant.CAMERA}, new PermissionsResultAction() {

                                                @Override
                                                public void onGranted(List<String> perms) {
                                                    initRtc();
                                                }

                                                @Override
                                                public void onDenied(List<String> perms) {
                                                    deniedPermission();
                                                }
                                            });
                                }
                            } else {
                                initRtc();
                            }
                        }

                        @Override
                        public void onDenied(List<String> perms) {
                            deniedPermission();
                        }
                    });
        }
    }

    /**
     * 处理无权限
     */
    private void deniedPermission(){
        sendRTCHangupMessage();
        ToastUtil.showTextViewPrompt(
                getString(R.string.video_call_fail_check_permission));
        finish();
    }

    /**
     * 作为发起方发起视频
     */
    private void onInitiateCall() {
        showInitiatorView();
        hideRecipientView();
        hideControlView();
        /* 30s无响应提示 */
        mHandler.sendEmptyMessageDelayed(CallHandler.MSG_30_SEC_NOTIFY,
                CallHandler.TIME_DELAY_NOTIFY_PEER_NOT_ANSWER);
        /* 无响应关闭 */
        mHandler.sendEmptyMessageDelayed(CallHandler.MSG_PEER_NOT_ANSWER_CLOSE_ACTIVITY,
                CallHandler.TIME_DELAY_CLOSE_ACTIVITY_PEER_NOT_ANSWER);
    }

    /**
     * 作为接收方
     */
    private void onRecipientCall() {
        showRecipientView();
        hideInitiatorView();
        hideControlView();
    }

    /**
     * 展示视频发起者信息
     */
    private void showVideoPeerInfo() {
        mVideoContainer.setVisibility(View.VISIBLE);
        mAudioContainer.setVisibility(View.GONE);

        ViewGroup peerInfo = mVideoContainer.findViewById(R.id.container_video_peer_info);
        peerInfo.setVisibility(View.VISIBLE);
        TextView nameText = mVideoContainer.findViewById(R.id.tv_video_peer_name);
        ShapeImageView avatar = mVideoContainer.findViewById(R.id.iv_video_peer_avatar);
        String name = "";
        if (mRosterItem != null && !TextUtils.isEmpty(mRosterItem.alias())) {
            name = mRosterItem.alias();
        } else if (mRosterItem != null && !TextUtils.isEmpty(mRosterItem.nickname())) {
            name = mRosterItem.nickname();
        } else if (mRosterItem != null) {
            name = mRosterItem.username();
        }
        nameText.setText(name);
        ChatUtils.getInstance().showRosterAvatar(mRosterItem, avatar, new ImageRequestConfig.Builder().cacheInMemory(true)
                .showImageForEmptyUri(R.drawable.default_avatar_icon)
                .showImageOnFail(R.drawable.default_avatar_icon)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageOnLoading(R.drawable.default_avatar_icon).build());
    }

    /**
     * 隐藏视频发起者信息
     */
    private void hideVideoPeerInfo() {
        ViewGroup peerInfo = mVideoContainer.findViewById(R.id.container_video_peer_info);
        peerInfo.setVisibility(View.GONE);
    }

    /**
     * 展示音频发起者信息
     */
    private void showAudioPeerInfo() {
        mAudioContainer.setVisibility(View.VISIBLE);
        mVideoContainer.setVisibility(View.GONE);

        ViewGroup peerInfo = mAudioContainer.findViewById(R.id.container_audio_peer_info);
        peerInfo.setVisibility(View.VISIBLE);
        TextView nameText = mAudioContainer.findViewById(R.id.tv_audio_peer_name);
        ShapeImageView avatar = mAudioContainer.findViewById(R.id.iv_audio_peer_avatar);
        String name = "";
        if (mRosterItem != null && !TextUtils.isEmpty(mRosterItem.alias())) {
            name = mRosterItem.alias();
        } else if (mRosterItem != null && !TextUtils.isEmpty(mRosterItem.nickname())) {
            name = mRosterItem.nickname();
        } else if (mRosterItem != null) {
            name = mRosterItem.username();
        }
        nameText.setText(name);
        ChatUtils.getInstance().showRosterAvatar(mRosterItem, avatar, new ImageRequestConfig.Builder().cacheInMemory(true)
                .showImageForEmptyUri(R.drawable.default_avatar_icon)
                .showImageOnFail(R.drawable.default_avatar_icon)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageOnLoading(R.drawable.default_avatar_icon).build());
    }

    /**
     * 隐藏音频发起者信息
     */
    private void hideAudioPeerInfo() {
        ViewGroup peerInfo = mAudioContainer.findViewById(R.id.container_audio_peer_info);
        peerInfo.setVisibility(View.GONE);
    }

    /**
     * 展示发起音视频的view
     */
    private void showInitiatorView() {
        ViewGroup view = findViewById(R.id.ll_initiate_control);
        view.setVisibility(View.VISIBLE);

        View initialView = mHasVideo ? view.findViewById(R.id.layout_initiator_video) : view.findViewById(R.id.layout_initiator_audio);
        initialView.setVisibility(View.VISIBLE);
        if (!mHasVideo) {
            changeSpeaker(mSpeaker, initialView.findViewById(R.id.iv_audio_speaker), initialView.findViewById(R.id.tv_audio_speaker));
        }
    }

    /**
     * 隐藏发起音视频的view
     */
    private void hideInitiatorView() {
        ViewGroup view = findViewById(R.id.ll_initiate_control);
        view.setVisibility(View.GONE);
    }

    /**
     * 展示接收音视频的view
     */
    private void showRecipientView() {
        ViewGroup view = findViewById(R.id.ll_recipient_control);
        view.setVisibility(View.VISIBLE);

        View recipientView = mHasVideo ? view.findViewById(R.id.layout_recipient_video) : view.findViewById(R.id.layout_recipient_audio);
        recipientView.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏接收音视频的view
     */
    private void hideRecipientView() {
        ViewGroup view = findViewById(R.id.ll_recipient_control);
        view.setVisibility(View.GONE);
    }

    /**
     * 展示控制view
     */
    private void showControlView(boolean hasVideo) {
        ViewGroup view = findViewById(R.id.ll_in_call_control);
        view.setVisibility(View.VISIBLE);

        View inCallVideoView = view.findViewById(R.id.layout_calling_video);
        View inCallAudioView = view.findViewById(R.id.layout_calling_audio);
        if (hasVideo) {
            inCallVideoView.setVisibility(View.VISIBLE);
            inCallAudioView.setVisibility(View.GONE);
        } else {
            inCallVideoView.setVisibility(View.GONE);
            inCallAudioView.setVisibility(View.VISIBLE);
            changeSpeaker(mSpeaker, inCallAudioView.findViewById(R.id.iv_audio_speaker), inCallAudioView.findViewById(R.id.tv_audio_speaker));
        }
    }

    /**
     * 隐藏控制view
     */
    private void hideControlView() {
        ViewGroup view = findViewById(R.id.ll_in_call_control);
        view.setVisibility(View.GONE);
    }

    private void changeSpeaker(boolean enable, ImageView imageView, TextView textView) {
        mSpeaker = enable;
        if (imageView != null) {
            imageView.setImageResource(mSpeaker ? R.drawable.icon_call_hand_free_status_opened : R.drawable.icon_call_hand_free_status_closed);
        }
        if (textView != null) {
            textView.setText(mSpeaker ? R.string.call_open_hand_free : R.string.call_close_hand_free);
        }
    }

    private void changeMic(boolean enable, ImageView imageView, TextView textView) {
        mMic = enable;
        if (imageView != null) {
            imageView.setImageResource(mMic ? R.drawable.icon_call_mute_status_open : R.drawable.icon_call_mute_status_close);
        }
        if (textView != null) {
            textView.setText(mMic ? R.string.call_mute : R.string.call_un_mute);
        }
    }

    /**
     * 加入房间
     */
    private void joinRoom() {
        BMXRoomAuth auth = new BMXRoomAuth();
        auth.setMUserId(mUserId);
        auth.setMRoomId(mRoomId);
        auth.setMToken(mPin);
        mEngine.joinRoom(auth);
    }

    /**
     * 离开房间
     */
    private void leaveRoom() {
        mEngine.leaveRoom();
        BMXVideoCanvas canvas = new BMXVideoCanvas();
        BMXStream stream = new BMXStream();
        stream.setMMediaType(BMXVideoMediaType.Camera);
        canvas.setMStream(stream);
        mEngine.stopPreview(canvas);

        if (mLocalView != null) {
            mLocalView.release();
            mLocalView = null;
        }
        if (mRemoteView != null) {
            mRemoteView.release();
            mRemoteView = null;
        }
        finish();
    }

    /**
     * 接听
     */
    public void onCallAnswer(View view) {
        joinRoom();
        sendRTCPickupMessage();
    }

    /**
     * 挂断
     */
    public void onCallHangup(View view){
        sendRTCHangupMessage();
        leaveRoom();
        finish();
    }

    /**
     * 切换摄像头
     */
    public void onSwitchCamera(View view) {
        mEngine.switchCamera();
    }

    /**
     * 切换语音通话
     */
    public void onSwitchAudio(View view) {
        switchAudio();
        sendRTCMessage("mute_video", "");
    }

    /**
     * 切换麦克风
     * @param view
     */
    public void onCallMuteMic(View view){
        ViewGroup parent = findViewById(R.id.ll_in_call_control);
        changeMic(!mMic, parent.findViewById(R.id.iv_audio_mic), parent.findViewById(R.id.tv_audio_mic));
        mEngine.muteLocalAudio(mMic);
    }

    private void switchAudio(){
        BMXVideoCanvas canvas = new BMXVideoCanvas();
        BMXStream stream = new BMXStream();
        stream.setMMediaType(BMXVideoMediaType.Camera);
        canvas.setMStream(stream);
        mEngine.stopPreview(canvas);
        mEngine.muteLocalVideo(BMXVideoMediaType.Camera, true);
        removeLocalView();
        removeRemoteView();
        hideVideoPeerInfo();
        showAudioPeerInfo();
        mCallType = BMXMessageConfig.RTCCallType.AudioCall;
        mHasVideo = false;
        showControlView(mHasVideo);
    }

    /**
     * 切换扬声器
     */
    public void onSwitchSpeakerInitiator(View view) {
        ViewGroup parent = findViewById(R.id.ll_initiate_control);
        changeSpeaker(!mSpeaker, parent.findViewById(R.id.iv_audio_speaker), parent.findViewById(R.id.tv_audio_speaker));
//        mEngine.setAudioProfile(mSpeaker);
        ToastUtil.showTextViewPrompt(mSpeaker ? R.string.call_speaker_on_tips : R.string.call_speaker_off_tips);
    }

    /**
     * 切换扬声器
     */
    public void onSwitchSpeakerCalling(View view) {
        ViewGroup parent = findViewById(R.id.ll_in_call_control);
        changeSpeaker(!mSpeaker, parent.findViewById(R.id.iv_audio_speaker), parent.findViewById(R.id.tv_audio_speaker));
//        mEngine.setAudioProfile(mSpeaker);
        ToastUtil.showTextViewPrompt(mSpeaker ? R.string.call_speaker_on_tips : R.string.call_speaker_off_tips);
    }

    private void onUserJoin(BMXStream info){
        if(info == null){
            return;
        }
        boolean hasVideo = info.getMEnableVideo();
        boolean hasAudio = info.getMEnableAudio();
        if (mHasVideo) {
            addLocalView();
            BMXVideoCanvas canvas = new BMXVideoCanvas();
            canvas.setMView(mLocalView.getObtainView());
            canvas.setMStream(info);
            mEngine.startPreview(canvas);
        } else {

        }
        if (mIsInitiator) {
            //用户加入放入房间 发送给对方信息
            sendRTCCallMessage();
        }
    }

    /**
     * 远端用户加入
     */
    private void onRemoteJoin(BMXStream info) {
        if(info == null){
            return;
        }
        boolean hasVideo = info.getMEnableVideo();
        boolean hasAudio = info.getMEnableAudio();
        hideInitiatorView();
        hideRecipientView();
        showControlView(mHasVideo);
        if (mHasVideo) {
            addRemoteView();
            BMXVideoCanvas canvas = new BMXVideoCanvas();
            canvas.setMView(mRemoteView.getObtainView());
            canvas.setMUserId(info.getMUserId());
            canvas.setMStream(info);
            mEngine.startRemoteView(canvas);
            hideVideoPeerInfo();
        } else {

        }
        mHandler.removeAll();
    }

    /**
     * 远端用户离开
     */
    private void onRemoteLeave() {
        removeRemoteView();
        mHandler.removeAll();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeAll();
        if (mEngine != null) {
            mEngine.removeRTCEngineListener(mListener);
            mListener = null;
        }
        ChatManager.getInstance().removeChatListener(mChatListener);
        mChatListener = null;
        RTCManager.getInstance().removeRTCServiceListener(mRTCListener);
        mRTCListener = null;
    }

    private class CallHandler extends WeakHandler<Activity> {

        /* 关闭页面 */
        private static final int MSG_CLOSE_ACTIVITY = 0;

        /* 对方无应答 */
        private static final int MSG_PEER_NOT_ANSWER_CLOSE_ACTIVITY = 1;

        /* 30s提示 */
        private static final int MSG_30_SEC_NOTIFY = 2;

        /* 延时关闭页面 */
        private static final int TIME_DELAY_CLOSE_ACTIVITY = 500;

        /* 对方无应答延时关闭页面 */
        private static final int TIME_DELAY_CLOSE_ACTIVITY_PEER_NOT_ANSWER = 60000;

        /* 30s对方无应答提示 */
        private static final int TIME_DELAY_NOTIFY_PEER_NOT_ANSWER = 30000;

        public CallHandler(Activity activity) {
            super(activity);
        }

        @Override
        protected void handleWeakMessage(Message msg) {
            super.handleWeakMessage(msg);
            switch (msg.what) {
                case MSG_PEER_NOT_ANSWER_CLOSE_ACTIVITY:
                    ToastUtil.showTextViewPrompt(getString(R.string.call_peer_not_answer));
                    finish();
                    break;
                case MSG_CLOSE_ACTIVITY:
                    finish();
                    break;
                case MSG_30_SEC_NOTIFY:
                    ToastUtil.showTextViewPrompt(getString(R.string.call_suggest_call_later));
                    break;
                default:
                    break;
            }
        }

        public void removeAll() {
            removeMessages(MSG_CLOSE_ACTIVITY);
            removeMessages(MSG_PEER_NOT_ANSWER_CLOSE_ACTIVITY);
            removeMessages(MSG_30_SEC_NOTIFY);
            removeMessages(TIME_DELAY_CLOSE_ACTIVITY);
            removeMessages(TIME_DELAY_CLOSE_ACTIVITY_PEER_NOT_ANSWER);
            removeMessages(TIME_DELAY_NOTIFY_PEER_NOT_ANSWER);
        }
    }

    /**
     * 发送RTC信息
     */
    private void sendRTCMessage(String config, String value){
        String extension = "";
        try {
            JSONObject object = new JSONObject();
            object.put("rtcKey", config);
            object.put("rtcValue", value);
            extension = object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSendUtils.sendInputStatusMessage(BMXMessage.MessageType.Single, SharePreferenceUtils.getInstance().getUserId(), mChatId, extension);
    }

    /**
     * 发送呼叫信息
     */
    private void sendRTCCallMessage(){
        mCallId = mSendUtils.sendRTCCallMessage(
                mHasVideo? BMXMessageConfig.RTCCallType.VideoCall: BMXMessageConfig.RTCCallType.AudioCall,
                mRoomId, mUserId, mChatId, mPin);
    }

    /**
     * 发送接听信息
     */
    private void sendRTCPickupMessage(){
        mCallId = mSendUtils.sendRTCPickupMessage(
                mUserId, mChatId, mCallId);
    }

    /**
     * 发送挂断信息
     */
    private void sendRTCHangupMessage(){
        String content = "canceled";
        if (!mIsInitiator){
            content = "rejected";
        } else{
            if (false) {//todo mRingTimes == 0
                content = "timeout";
            }
        }
        long duration = 0;
        if (mPickupTimestamp > 1){
            duration = getTimeStamp() - mPickupTimestamp;
        }
        if (duration > 1){
            content = String.valueOf(duration);
        }

        mCallId = mSendUtils.sendRTCHangupMessage(
                mUserId, mChatId, mCallId, content);
    }

    /**
     * 处理RTC消息
     */
    private void handleRTCMessage(BMXMessage message){
        if (message == null) {
            return;
        }
        if (message.contentType() == BMXMessage.ContentType.Text
                && !TextUtils.isEmpty(message.extension())) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(message.extension());
                if(!jsonObject.has("rtcKey")){
                    return;
                }
                String key = jsonObject.getString("rtcKey");
                switch (key){
                    case "mute_video":
                        //切换语音通话
                        switchAudio();
                        break;
                    case "mute_audio":
                        break;
                    case "hangup":
                        //挂断
                        leaveRoom();
                        break;
                    default:
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
