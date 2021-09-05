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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import im.floo.floolib.BMXChatServiceListener;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageList;
import im.floo.floolib.BMXRosterItemList;
import im.floo.floolib.ListOfLongLong;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.WeakHandler;
import top.maxim.im.common.view.Header;
import top.maxim.im.message.utils.MessageConfig;
import top.maxim.im.sdk.utils.MessageSendUtils;
import top.maxim.rtc.bean.BMXRtcStreamInfo;
import top.maxim.rtc.engine.StupidEngine;
import top.maxim.rtc.interfaces.BMXRTCEngineListener;
import top.maxim.rtc.manager.RTCManager;

/**
 * Description 单视频会话
 */
public class GroupVideoCallActivity extends BaseTitleActivity {

    private static final String TAG = "GroupVideoCallActivity";

    public static void openVideoCall(Context context, ArrayList<Long> chatIds, int callMode) {
        Intent intent = new Intent(context, GroupVideoCallActivity.class);
        intent.putExtra(MessageConfig.CHAT_IDS, (Serializable) chatIds);
        intent.putExtra(MessageConfig.CALL_MODE, callMode);
        context.startActivity(intent);
    }

    private RecyclerView mRecyclerView;

    private GroupVideoCallAdapter mAdapter;

    private GridLayoutManager mLayoutManager;

    private ViewGroup mInitiatorContainer;
    private ViewGroup mRecipientContainer;

    private List<Long> mChatIds;

    private long mUserId;

    //默认音频
    private int mCallMode = MessageConfig.CallMode.CALL_AUDIO;

    private boolean mHasVideo = false;

    private CallHandler mHandler;

    private StupidEngine mEngine;

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
        initRtc();
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
        }
        mUserId = SharePreferenceUtils.getInstance().getUserId();
        mHasVideo = mCallMode == MessageConfig.CallMode.CALL_VIDEO;
    }

    private void initRtc() {
        mEngine = RTCManager.getInstance().getRTCEngine();
        RTCManager.getInstance().addRtcListener(mListener = new BMXRTCEngineListener() {
            @Override
            public void onConnectionStateChanged() {

            }

            @Override
            public void onNetworkTypeChanged() {

            }

            @Override
            public void onJoinRoom(int code, String msg, String roomId) {
                if (code == 0) {
                    mEngine.publish(mHasVideo, true);
                    Log.e(TAG, "加入房间成功 开启发布本地流, roomId= " + roomId + "msg = " + msg);
                } else {
                    Log.e(TAG, "加入房间失败 roomId= " + roomId + "msg = " + msg);
                }
            }

            @Override
            public void onLeaveRoom(int code, String msg, String roomId) {
                if (code == 0) {
                    Log.e(TAG, "离开房间成功 roomId= " + roomId + "msg = " + msg);
                } else {
                    Log.e(TAG, "离开房间失败 roomId= " + roomId + "msg = " + msg);
                }
            }

            @Override
            public void onReJoinRoom() {

            }

            @Override
            public void onMemberJoined(String uid) {
                Log.e(TAG, "远端用户加入 uid= " + uid);
            }

            @Override
            public void onMemberExited(String uid, int reason) {
                Log.e(TAG, "远端用户离开 uid= " + uid);
                onRemoteLeave();
            }

            @Override
            public void onLocalPublish(int code, String msg, BMXRtcStreamInfo streamInfo) {
                if (code == 0) {
                    onUserJoin(streamInfo);
                    Log.e(TAG, "发布本地流成功 开启预览 msg = " + msg);
                } else {
                    Log.e(TAG, "发布本地流失败 msg = " + msg);
                }
            }

            @Override
            public void onLocalUnPublish(int code, String msg, BMXRtcStreamInfo streamInfo) {
                if (code == 0) {
                    Log.e(TAG, "停止发布本地流成功 msg = " + msg);
                } else {
                    Log.e(TAG, "停止发布本地流失败 msg = " + msg);
                }
            }

            @Override
            public void onRemotePublish(BMXRtcStreamInfo streamInfo) {
                mEngine.subscribe(streamInfo);
                Log.e(TAG, "远端发布流 开启订阅");
            }

            @Override
            public void onRemoteUnPublish(BMXRtcStreamInfo streamInfo) {
                Log.e(TAG, "远端取消发布流");
                mEngine.stopRemotePreview(streamInfo);
                mEngine.unSubscribe(streamInfo);
                onRemoteLeave();
            }

            @Override
            public void onSubscribe(int code, String msg, BMXRtcStreamInfo streamInfo) {
                if (code == 0) {
                    onRemoteJoin(streamInfo);
                    Log.e(TAG, "订阅远端流成功 msg = " + msg);
                } else {
                    Log.e(TAG, "订阅远端流失败 msg = " + msg);
                }
            }

            @Override
            public void onUnSubscribe(int code, String msg, BMXRtcStreamInfo streamInfo) {
                if (code == 0) {
                    Log.e(TAG, "取消订阅远端流成功, 开启预览 msg = " + msg);
                } else {
                    Log.e(TAG, "取消订阅远端流失败 msg = " + msg);
                }
            }

            @Override
            public void onLocalAudioLevel() {

            }

            @Override
            public void onRemoteAudioLevel() {

            }

            @Override
            public void onKickoff() {

            }

            @Override
            public void onWarning() {

            }

            @Override
            public void onError() {

            }

            @Override
            public void onNetworkQuality() {

            }
        });
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
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
                onInitiateCall(itemList);
            } else {
                RosterManager.getInstance().getRosterList(chatIds, true,
                        (bmxErrorCode1, itemList1) -> {
                            if (!BaseManager.bmxFinish(bmxErrorCode1)) {
                                return;
                            }
                            onInitiateCall(itemList1);
                        });
            }
        });
    }

    /**
     * 作为发起方发起视频
     */
    private void onInitiateCall(BMXRosterItemList list) {
        showInitiatorView(list);
        hideControlView();
        hideRecipientView();
        /* 30s无响应提示 */
        mHandler.sendEmptyMessageDelayed(CallHandler.MSG_30_SEC_NOTIFY,
                CallHandler.TIME_DELAY_NOTIFY_PEER_NOT_ANSWER);
        /* 无响应关闭 */
        mHandler.sendEmptyMessageDelayed(CallHandler.MSG_PEER_NOT_ANSWER_CLOSE_ACTIVITY,
                CallHandler.TIME_DELAY_CLOSE_ACTIVITY_PEER_NOT_ANSWER);
        joinRoom(true);
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
     * 展示控制view
     */
    private void showControlView() {
    }

    /**
     * 隐藏控制view
     */
    private void hideControlView() {

    }

    /**
     * 加入房间
     *
     * @param myRoom
     */
    private void joinRoom(boolean myRoom) {
//        String roomId = myRoom ? String.valueOf(mUserId) + mChatId : String.valueOf(mChatId) + mUserId;
        String roomId = "123";
        mEngine.joinRoom(String.valueOf(mUserId), roomId);
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
        joinRoom(false);
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

    private void onUserJoin(BMXRtcStreamInfo info){
        if(info == null){
            return;
        }
        boolean hasVideo = info.isHasVideo();
        boolean hasAudio = info.isHasAudio();
        if (hasVideo) {
        } else {
        }
    }

    /**
     * 远端用户加入
     */
    private void onRemoteJoin(BMXRtcStreamInfo info) {
        if(info == null){
            return;
        }
        boolean hasVideo = info.isHasVideo();
        boolean hasAudio = info.isHasAudio();
        hideInitiatorView();
        hideRecipientView();
        if (hasVideo) {
            showControlView();
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
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveRoom();
        mHandler.removeAll();
        RTCManager.getInstance().removeRtcListener(mListener);
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
}
