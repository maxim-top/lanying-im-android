package top.maxim.im.videocall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import im.floo.floolib.BMXChatServiceListener;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageList;
import im.floo.floolib.BMXRTCEngine;
import im.floo.floolib.BMXRTCEngineListener;
import im.floo.floolib.BMXRoomAuth;
import im.floo.floolib.BMXRosterItemList;
import im.floo.floolib.BMXStream;
import im.floo.floolib.BMXVideoCanvas;
import im.floo.floolib.BMXVideoMediaType;
import im.floo.floolib.ListOfLongLong;
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
import top.maxim.im.message.utils.MessageConfig;
import top.maxim.im.sdk.utils.MessageSendUtils;

/**
 * Description 单视频会话
 */
public class GroupVideoCallActivity extends BaseTitleActivity {

    private static final String TAG = "GroupVideoCallActivity";

    public static void openVideoCall(Context context, ArrayList<Long> chatIds, String roomId, boolean isInitiator, int callMode) {
        Intent intent = new Intent(context, GroupVideoCallActivity.class);
        intent.putExtra(MessageConfig.CHAT_IDS, (Serializable) chatIds);
        intent.putExtra(MessageConfig.CALL_MODE, callMode);
        intent.putExtra(MessageConfig.RTC_ROOM_ID, roomId);
        intent.putExtra(MessageConfig.IS_INITIATOR, isInitiator);
        context.startActivity(intent);
    }

    private RecyclerView mRecyclerView;

    private GroupVideoCallAdapter mAdapter;

    private GridLayoutManager mLayoutManager;

    private ViewGroup mInitiatorContainer;
    private ViewGroup mRecipientContainer;

    private List<Long> mChatIds;

    private long mUserId;

    private String mRoomId;

    //默认音频
    private int mCallMode = MessageConfig.CallMode.CALL_AUDIO;

    //是否是发起者
    private boolean mIsInitiator;

    private boolean mHasVideo = false;

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
                }
            }
        }
    };

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
        View view = View.inflate(this, R.layout.activity_group_video_call, null);
        mInitiatorContainer = view.findViewById(R.id.layout_group_initiator_container);
        mRecipientContainer = view.findViewById(R.id.layout_group_recipient_container);
        mRecyclerView = mInitiatorContainer.findViewById(R.id.rcy_member);
        mRecyclerView.setAdapter(mAdapter = new GroupVideoCallAdapter(this));
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
            mChatIds = (List<Long>) intent.getSerializableExtra(MessageConfig.CHAT_IDS);
            mCallMode = intent.getIntExtra(MessageConfig.CALL_MODE, 0);
            mRoomId = intent.getStringExtra(MessageConfig.RTC_ROOM_ID);
            mIsInitiator = intent.getBooleanExtra(MessageConfig.IS_INITIATOR, false);
        }
        mUserId = SharePreferenceUtils.getInstance().getUserId();
        mHasVideo = mCallMode == MessageConfig.CallMode.CALL_VIDEO;
    }

    private void initRtc() {
        mEngine = RTCManager.getInstance().getRTCEngine();
        mEngine.addRTCEngineListener(mListener = new BMXRTCEngineListener() {

            @Override
            public void onJoinRoom(String info, String roomId, BMXErrorCode error) {
                super.onJoinRoom(info, roomId, error);
                if (BaseManager.bmxFinish(error)) {
                    mEngine.publish(BMXVideoMediaType.Camera, mHasVideo, true);
                    Log.e(TAG, "加入房间成功 开启发布本地流, roomId= " + roomId + "msg = " + info);
                } else {
                    Log.e(TAG, "加入房间失败 roomId= " + roomId + "msg = " + info);
                }
            }

            @Override
            public void onLeaveRoom(String info, String roomId, BMXErrorCode error, String reason) {
                super.onLeaveRoom(info, roomId, error, reason);
                if (BaseManager.bmxFinish(error)) {
                    Log.e(TAG, "离开房间成功 roomId= " + roomId + "msg = " + reason);
                }else{
                    Log.e(TAG, "离开房间失败 roomId= " + roomId + "msg = " + reason);
                }
            }

            @Override
            public void onReJoinRoom(String info, String roomId, BMXErrorCode error) {
                super.onReJoinRoom(info, roomId, error);
            }

            @Override
            public void onMemberJoined(String roomId, long usedId) {
                super.onMemberJoined(roomId, usedId);
                Log.e(TAG, "远端用户加入 uid= " + usedId);
            }

            @Override
            public void onMemberExited(String roomId, long usedId, String reason) {
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
        if (mIsInitiator) {
            joinRoom();
        }
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        ChatManager.getInstance().addChatListener(mChatListener);
        initRoster();
    }

    private void initRoster() {
        ListOfLongLong chatIds = new ListOfLongLong();
        if (mChatIds != null && mChatIds.size() > 0) {
            for (long chatId : mChatIds) {
                chatIds.add(chatId);
            }
        }
        RosterManager.getInstance().getRosterList(chatIds, false, (bmxErrorCode, itemList) -> {
            RosterFetcher.getFetcher().putRosters(itemList);
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                initCallView(itemList);
            } else {
                RosterManager.getInstance().getRosterList(chatIds, true,
                        (bmxErrorCode1, itemList1) -> {
                            if (!BaseManager.bmxFinish(bmxErrorCode1)) {
                                return;
                            }
                            initCallView(itemList1);
                        });
            }
        });
    }

    private void initCallView(BMXRosterItemList list){
        if (mIsInitiator) {
            onInitiateCall(list);
        } else {
            onRecipientCall(list);
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
                                            GroupVideoCallActivity.this, new String[]{PermissionsConstant.CAMERA}, new PermissionsResultAction() {

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
        sendRTCMessage("hangup", "");
        ToastUtil.showTextViewPrompt(
                getString(R.string.video_call_fail_check_permission));
        finish();
    }

    /**
     * 作为发起方发起视频
     */
    private void onInitiateCall(BMXRosterItemList list) {
        showInitiatorView(list);
        hideRecipientView();
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
    private void onRecipientCall(BMXRosterItemList list) {
        showRecipientView(list);
        hideInitiatorView();
    }

    /**
     * 展示发起音视频的view
     */
    private void showInitiatorView(BMXRosterItemList list) {
        mInitiatorContainer.setVisibility(View.VISIBLE);
        if(list == null){
            return;
        }
        List<String> items = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            items.add(list.get(i).rosterId() + "");
        }
        //自己也加入列表 作为第一个
        items.add(0, mUserId + "");
        //会话成员大于4个  网格最大个数3
        int spanCount = items.size() > 4 ? 3 : 2;
        if (mLayoutManager == null) {
            mLayoutManager = new GridLayoutManager(this, spanCount);
            mRecyclerView.setLayoutManager(mLayoutManager);
        } else {
            mLayoutManager.setSpanCount(spanCount);
        }
        mAdapter.setSpanCount(spanCount);
        mAdapter.replaceList(items);
    }

    /**
     * 隐藏发起音视频的view
     */
    private void hideInitiatorView() {
        mInitiatorContainer.setVisibility(View.GONE);
    }

    /**
     * 展示发起音视频的view
     */
    private void showRecipientView(BMXRosterItemList list) {
        mRecipientContainer.setVisibility(View.VISIBLE);
        if(list == null){
            return;
        }
        List<String> items = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            items.add(list.get(i).rosterId() + "");
        }
        //自己也加入列表 作为第一个
        items.add(0, mUserId + "");
        //会话成员大于4个  网格最大个数3
        int spanCount = items.size() > 4 ? 3 : 2;
        if (mLayoutManager == null) {
            mLayoutManager = new GridLayoutManager(this, spanCount);
            mRecyclerView.setLayoutManager(mLayoutManager);
        } else {
            mLayoutManager.setSpanCount(spanCount);
        }
        mAdapter.setSpanCount(spanCount);
        mAdapter.replaceList(items);
    }

    /**
     * 隐藏发起音视频的view
     */
    private void hideRecipientView() {
        mRecipientContainer.setVisibility(View.GONE);
    }

    /**
     * 加入房间
     *
     */
    private void joinRoom() {
        BMXRoomAuth auth = new BMXRoomAuth();
        auth.setMUserId(mUserId);
        auth.setMRoomId(mRoomId);
        mEngine.joinRoom(auth);
    }

    /**
     * 离开房间
     */
    private void leaveRoom() {
        mEngine.leaveRoom();
    }

    /**
     * 接听
     */
    public void onCallAnswer(View view) {
        joinRoom();
    }

    /**
     * 拒绝
     */
    public void onCallReject(View view) {
        finish();
    }

    /**
     * 取消
     */
    public void onCallCancel(View view) {
        leaveRoom();
        finish();
    }

    private void onUserJoin(BMXStream info){
        if(info == null){
            return;
        }
        boolean hasVideo = info.getMEnableVideo();
        boolean hasAudio = info.getMEnableAudio();
        if (mHasVideo) {
        } else {

        }
        if (mIsInitiator) {
            //用户加入放入房间 发送给对方信息
            String chatIds = "";
            for (Long id : mChatIds) {
                chatIds = chatIds + ",";
            }
            chatIds = chatIds.substring(chatIds.length() - 1);
            sendRTCMessage("join", mRoomId + "_" + chatIds + "_" + mCallMode);
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
        if (mHasVideo) {
        } else {

        }
        mHandler.removeAll();
    }

    /**
     * 远端用户离开
     */
    private void onRemoteLeave() {
        hideInitiatorView();
        hideRecipientView();
        mHandler.removeAll();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveRoom();
        mHandler.removeAll();
        mEngine.removeRTCEngineListener(mListener);
        ChatManager.getInstance().removeChatListener(mChatListener);
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
        mSendUtils.sendInputStatusMessage(BMXMessage.MessageType.Group, SharePreferenceUtils.getInstance().getUserId(), Long.valueOf(mRoomId), extension);
    }
}
