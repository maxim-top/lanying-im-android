
package top.maxim.im.message.itemholder;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXVoiceAttachment;
import top.maxim.im.R;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.message.interfaces.ChatActionListener;
import top.maxim.im.message.interfaces.VoicePlayCallback;
import top.maxim.im.message.utils.VoicePlayManager;

/**
 * Description : 消息语音类型 Created by Mango on 2018/11/18.
 */
public class MessageItemAudio extends MessageItemBaseView {

    /* 语音布局 */
    private FrameLayout mFlChatVoice;

    /* 语音时间 */
    private TextView mVoiceTime;

    private ImageView mIvVoicePlay;

    private VoicePlayCallback listener = new VoicePlayCallback() {

        @Override
        public void onStart(long msgId) {
            if (msgId == mMaxMessage.msgId()) {
                showVoiceAnimation(true);
            }
        }

        @Override
        public void onFinish(long msgId) {
            if (msgId == mMaxMessage.msgId()) {
                showVoiceAnimation(false);
            }
        }

        @Override
        public void onFail(long msgId) {
        }
    };

    public MessageItemAudio(@NonNull Context context, ChatActionListener listener, int itemPos) {
        super(context, listener, itemPos);
    }

    @Override
    protected View initView(ViewGroup parent) {
        View view;
        if (mItemPos == ITEM_LEFT) {
            view = View.inflate(mContext, R.layout.item_chat_voice_left, parent);
        } else {
            view = View.inflate(mContext, R.layout.item_chat_voice_right, parent);
        }
        mFlChatVoice = view.findViewById(R.id.fl_voice_message);
        mVoiceTime = view.findViewById(R.id.tv_voice_time);
        mIvVoicePlay = view.findViewById(R.id.iv_voice_play);
        return view;
    }

    @Override
    protected void bindData() {
        fillView();
    }

    /**
     * 填充数据
     */
    private void fillView() {
        setItemViewListener(mFlChatVoice);
        showVoice();
        showVoiceAnimation(false);
    }

    @Override
    protected void setItemViewListener(View view) {
        // 长按
        view.setOnLongClickListener(new ItemLongClickListener());
        // 点击
        view.setOnClickListener(v -> {
            if (mActionListener != null) {
                registerListener();
                mActionListener.onItemFunc(mMaxMessage);
            }
        });
    }

    /**
     * 展示语音数据
     */
    private void showVoice() {
        if (mMaxMessage == null || mMaxMessage.contentType() != BMXMessage.ContentType.Voice) {
            return;
        }
        BMXVoiceAttachment body = BMXVoiceAttachment.dynamic_cast(mMaxMessage.attachment());
        if (body == null) {
            return;
        }
        int voiceTime = body.duration();
        mVoiceTime.setText(voiceTime + "''");
        int width = ScreenUtils.dp2px((196 - 78) / 60 * voiceTime + 78);
        if (width > ScreenUtils.dp2px(196)) {
            width = ScreenUtils.dp2px(196);
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mFlChatVoice
                .getLayoutParams();
        params.width = width;
        params.height = ScreenUtils.dp2px(41);
        mFlChatVoice.setLayoutParams(params);
    }

    /**
     * 展示语音播放动画
     */
    private void showVoiceAnimation(boolean play) {
        Drawable voice = mIvVoicePlay.getDrawable();
        if (play) {
            // 正在播放
            if (voice != null && !(voice instanceof AnimationDrawable)) {
                mIvVoicePlay.setImageDrawable(createRightAnimation());
                AnimationDrawable voiceAnimation = (AnimationDrawable)mIvVoicePlay.getDrawable();
                voiceAnimation.start();
            }
        } else {
            if (voice instanceof AnimationDrawable) {
                ((AnimationDrawable)voice).stop();
            }
            mIvVoicePlay.setImageDrawable(mItemPos == ITEM_LEFT
                    ? mContext.getResources().getDrawable(R.drawable.voice_play_left3)
                    : mContext.getResources().getDrawable(R.drawable.voice_play_right3));
        }
    }

    /**
     * 创建语音播放动画drawable
     *
     * @return Drawable
     */
    private Drawable createRightAnimation() {
        AnimationDrawable animationDrawable = new AnimationDrawable();
        int[] defValue;
        if (mItemPos == ITEM_LEFT) {
            defValue = new int[] {
                    R.drawable.voice_play_left1, R.drawable.voice_play_left2,
                    R.drawable.voice_play_left3
            };
        } else {
            defValue = new int[] {
                    R.drawable.voice_play_right1, R.drawable.voice_play_right2,
                    R.drawable.voice_play_right3
            };
        }
        int keyLength = defValue.length;

        for (int i = 0; i < keyLength; ++i) {
            animationDrawable.addFrame(mContext.getResources().getDrawable(defValue[i]), 150);
        }
        animationDrawable.setOneShot(false);
        return animationDrawable;
    }

    /**
     * 注册上传下载监听
     */
    private void registerListener() {
        if (mMaxMessage == null) {
            return;
        }
        long msgId = mMaxMessage.msgId();
        VoicePlayManager.getInstance().registerListener(msgId, listener);
    }

    @Override
    public void onViewAttach() {
        // 语音需要手动点击发送已读
    }

    @Override
    public void onViewDetach() {
        super.onViewDetach();
        if (mMaxMessage != null) {
            VoicePlayManager.getInstance().unRegisterListener(mMaxMessage.msgId());
        }
    }
}
