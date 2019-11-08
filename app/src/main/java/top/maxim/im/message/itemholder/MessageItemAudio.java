
package top.maxim.im.message.itemholder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
        mFlChatVoice = (FrameLayout)view.findViewById(R.id.fl_voice_message);
        mVoiceTime = ((TextView)view.findViewById(R.id.tv_voice_time));
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

    @Override
    public void onViewAttach() {
        //语音需要手动点击发送已读
    }
}
