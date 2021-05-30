package top.maxim.im.videocall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import im.floo.floolib.BMXRosterItem;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.WeakHandler;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description 单视频会话
 */
public class SingleVideoCallActivity extends BaseTitleActivity {

    public static void openVideoCall(Context context, long chatId, int callMode) {
        Intent intent = new Intent(context, SingleVideoCallActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, chatId);
        intent.putExtra(MessageConfig.CALL_MODE, callMode);
        context.startActivity(intent);
    }

    private FrameLayout mVideoContainer;

    private FrameLayout mAudioContainer;

    private long mChatId;

    private long mUserId;

    private BMXRosterItem mRosterItem = new BMXRosterItem();

    //默认音频
    private int mCallMode = MessageConfig.CallMode.CALL_AUDIO;

    private boolean mHasVideo = false;

    private CallHandler mHandler;

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
        hideHeader();
        View view = View.inflate(this, R.layout.activity_single_video_call, null);
        mVideoContainer = view.findViewById(R.id.layout_video_container);
        mAudioContainer = view.findViewById(R.id.layout_audio_container);
        mHandler = new CallHandler(this);
        return view;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mChatId = intent.getLongExtra(MessageConfig.CHAT_ID, 0);
            mCallMode = intent.getIntExtra(MessageConfig.CALL_MODE, 0);
        }
        mUserId = SharePreferenceUtils.getInstance().getUserId();
        mHasVideo = mCallMode == MessageConfig.CallMode.CALL_VIDEO;
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        initRoster();
    }

    private void initRoster() {
        RosterManager.getInstance().getRosterList(mChatId, false, (bmxErrorCode, bmxRosterItem) -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                mRosterItem = bmxRosterItem;
                onInitiateCall();
            } else {
                RosterManager.getInstance().getRosterList(mChatId, true,
                        (bmxErrorCode1, bmxRosterItem1) -> {
                            if (!BaseManager.bmxFinish(bmxErrorCode)) {
                                return;
                            }
                            RosterFetcher.getFetcher().putRoster(bmxRosterItem1);
                            mRosterItem = bmxRosterItem1;
                            onInitiateCall();
                        });
            }
        });
    }

    /**
     * 作为发起方发起视频
     */
    private void onInitiateCall() {
        if (mHasVideo) {
            // 视频
            mVideoContainer.setVisibility(View.VISIBLE);
            mAudioContainer.setVisibility(View.GONE);
            showVideoPeerInfo();
        } else {
            // 音频
            mVideoContainer.setVisibility(View.GONE);
            mAudioContainer.setVisibility(View.VISIBLE);
            showAudioPeerInfo();
        }
        /* 30s无响应提示 */
        mHandler.sendEmptyMessageDelayed(CallHandler.MSG_30_SEC_NOTIFY,
                CallHandler.TIME_DELAY_NOTIFY_PEER_NOT_ANSWER);
        /* 无响应关闭 */
        mHandler.sendEmptyMessageDelayed(CallHandler.MSG_PEER_NOT_ANSWER_CLOSE_ACTIVITY,
                CallHandler.TIME_DELAY_CLOSE_ACTIVITY_PEER_NOT_ANSWER);
    }

    private void showVideoPeerInfo(){
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

    private void hideVideoPeerInfo(){
        ViewGroup peerInfo = mVideoContainer.findViewById(R.id.container_video_peer_info);
        peerInfo.setVisibility(View.GONE);
    }

    private void showAudioPeerInfo(){
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

    private void hideAudioPeerInfo(){
        ViewGroup peerInfo = mAudioContainer.findViewById(R.id.container_audio_peer_info);
        peerInfo.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    }
}
