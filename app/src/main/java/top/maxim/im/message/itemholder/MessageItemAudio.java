
package top.maxim.im.message.itemholder;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXVoiceAttachment;
import top.maxim.im.R;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.message.interfaces.ChatActionListener;

/**
 * Description : 消息语音类型 Created by Mango on 2018/11/18.
 */
public class MessageItemAudio extends MessageItemBaseView {

    /* 语音布局 */
    private FrameLayout mFlChatVoice;

    /* 语音时间 */
    private TextView mVoiceTime;

    private ImageView mIvVoicePlay;

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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,
                ScreenUtils.dp2px(41));
        mFlChatVoice.setLayoutParams(params);
    }

    /**
     * 展示语音播放动画
     */
//    private void showVoiceAnimation() {
//        if (mMaxMessage == null || mMaxMessage.contentType() != BMXMessage.ContentType.Voice) {
//            return;
//        }
//        BMXVoiceAttachment body = BMXVoiceAttachment.dynamic_cast(mMaxMessage.attachment());
//        if (body == null) {
//            return;
//        }
//        Drawable voice = mIvVoicePlay.getDrawable();
//        if (mVoiceBody.getStatus() == ChatConfig.VoiceStatus.VOICE_PLAY) {
//            mVoiceStatusIconImg.setImageResource(mListType == ChatConfig.ChatListPosition.ITEM_LEFT
//                    ? R.drawable.item_voice_pause_left_icon
//                    : R.drawable.item_voice_pause_right_icon);
//            cancelTwinkleVoice();
//            mVoiceIconImg.playAnimation();
//            // 正在播放
//            if (voice != null && !(voice instanceof AnimationDrawable)) {
//                // TODO 语音播放动画 暂时取消
//                // mVoiceIconImg.setImageDrawable(mListType ==
//                // ChatConfig.ChatListPosition.ITEM_LEFT
//                // ? mContext.getResources().getDrawable(R.drawable.voice_left_playanimation)
//                // : createRightAnimation());
//                // AnimationDrawable voiceAnimation =
//                // (AnimationDrawable)mVoiceIconImg.getDrawable();
//                // voiceAnimation.start();
//            }
//        } else if (mVoiceBody.getStatus() == ChatConfig.VoiceStatus.VOICE_RECORD) {
//            // 语音闪烁效果
//            twinkleVoice();
//        } else {
//            mVoiceStatusIconImg.setImageResource(mListType == ChatConfig.ChatListPosition.ITEM_LEFT
//                    ? R.drawable.item_voice_play_left_icon
//                    : R.drawable.item_voice_play_right_icon);
//            cancelTwinkleVoice();
//            mVoiceIconImg.setFrame(0);
//            mVoiceIconImg.cancelAnimation();
//            try {
//                if (null != voice && (voice instanceof AnimationDrawable)) {
//                    // TODO 语音播放动画 暂时取消
//                    // ((AnimationDrawable)voice).stop();
//                    // mVoiceIconImg
//                    // .setImageDrawable(mListType == ChatConfig.ChatListPosition.ITEM_LEFT
//                    // ? mContext.getResources()
//                    // .getDrawable(R.drawable.chat_voice_left3)
//                    // : TAppManager.getContext().getResources().getDrawable(
//                    // R.drawable.chat_voice_right3));
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

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

    @Override
    public void onViewAttach() {
        // 语音需要手动点击发送已读
    }
}
