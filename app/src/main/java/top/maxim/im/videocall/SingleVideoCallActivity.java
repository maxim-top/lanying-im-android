package top.maxim.im.videocall;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;

import top.maxim.im.R;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.view.Header;
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

    private RelativeLayout mContainer;

    private long mChatId;

    private long mUserId;

    //默认音频
    private int mCallMode = MessageConfig.CallMode.CALL_AUDIO;

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        return new Header.Builder(this, headerContainer).build();
    }

    @Override
    protected View onCreateView() {
        hideHeader();
        View view = View.inflate(this, R.layout.activity_single_video_call, null);
        mContainer = view.findViewById(R.id.rtc_container);
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
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
