
package top.maxim.im.message.utils;

import androidx.recyclerview.widget.RecyclerView;

import im.floo.floolib.BMXMessage;

/**
 * Description : 聊天列表帮助类 Created by Mango on 2018/11/18.
 */
public class MessageListHelper extends ListHelper<BMXMessage> {

    public MessageListHelper(RecyclerView view, RecyclerView.Adapter adapter) {
        super(view, adapter);
    }

    /**
     * 更新某一条消息
     *
     * @param bean 消息体
     */
    void updateMessage(BMXMessage bean) {
        BMXMessage preBean;
        for (int i = 0; i < mList.size(); i++) {
            preBean = mList.get(i);
            if (preBean.msgId() == bean.msgId()) {
                update(i, bean);
                break;
            }
        }
    }

    /**
     * 删除某条消息
     *
     * @param msgId 消息体
     */
    void deleteMessage(long msgId) {
        for (int i = 0; i < mList.size(); i++) {
            BMXMessage bean = mList.get(i);
            if (bean.msgId() ==  msgId) {
                remove(i);
                return;
            }
        }
    }

    /**
     * 取消所有的语音播放状态
     */
    void cancelVoicePlay() {
//        boolean isHasVoice = false;
//        for (BMXMessage bean : mList) {
//            if (bean.contentType() == BMXMessage.ContentType.Voice) {
//                CommonBody.AudioBody voiceBody = (CommonBody.AudioBody)bean.getBody();
//                if (voiceBody != null
//                        && voiceBody.getAudioStatus() == MessageConfig.VoiceStatus.VOICE_PLAY) {
//                    voiceBody.setAudioStatus(MessageConfig.VoiceStatus.VOICE_READED);
//                    isHasVoice = true;
//                }
//            }
//        }
//        if (isHasVoice) {
//            notifyAdapter();
//        }
    }
}
