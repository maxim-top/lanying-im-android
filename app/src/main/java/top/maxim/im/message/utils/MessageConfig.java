
package top.maxim.im.message.utils;

/**
 * Description : 消息常量 Created by Mango on 2018/11/11.
 */
public interface MessageConfig {

    /**
     * 会话id
     */
    String CHAT_ID = "chat_id";

    /**
     * 会话type
     */
    String CHAT_TYPE = "chat_type";

    /**
     * 消息体
     */
    String CHAT_MSG = "chat_msg";

    /**
     * 拍照路径
     */
    String KEY_CAMERA_PATH = "camera_path";

    /**
     * 音视频类型
     */
    String CALL_TYPE = "call_type";

    /**
     * roomId
     */
    String RTC_ROOM_ID = "roomId";

    /**
     * 是否发起者
     */
    String IS_INITIATOR = "isInitiator";

    /**
     * 房间密码
     */
    String PIN = "pin";

    /**
     * 当前通话发起呼叫时的消息的ID
     */
    String MESSAGE_ID = "messageId";

    /**
     * Call ID
     */
    String CALL_ID = "callId";

    /**
     * 会话id列表
     */
    String CHAT_IDS = "chatIds";

    /**
     * 添加
     */
    long MEMBER_ADD = -1;

    /**
     * 移除
     */
    long MEMBER_REMOVE = -2;

    /**
     * 默认消息条目
     */
    int DEFAULT_PAGE_SIZE = 20;

    /**
     * 消息发送状态
     */
    interface MessageSendStatus {

        int SEND_MSG_ING = 0;

        int SEND_NSG_FAIL = 1;

        int SEND_MSG_SUCCESS = 2;
    }

    /**
     * 消息发送者
     */
    interface MessageSender {

        int MY_SEND = 0;

        int OTHER_SEND = 1;
    }

    /**
     * 媒体的格式
     */
    interface MediaFormat {
        /**
         * 音频文件格式
         **/
        String VOICE_FORMAT = ".amr";

        /**
         * 视频文件格式
         **/
        String VIDEO_FORMAT = ".mp4";
    }

    /**
     * 语音状态
     */
    interface VoiceStatus {

        // 语音消息未读状态
        int VOICE_UNREAD = 0;

        // 语音消息正在播放状态
        int VOICE_PLAY = 1;

        // 语音消息正在录制状态
        int VOICE_RECORD = 2;

        // 语音消息已读状态
        int VOICE_READED = 3;
    }

    /**
     * 键盘操作
     */

    String INPUT_STATUS = "input_status";

    interface InputStatus {
        // 空
        String NOTHING_STATUS = "nothing";

        // 键盘弹起
        String TYING_STATUS = "typing";
    }

    interface DeviceType {

        int IOS = 1;

        int Android = 2;

        int WIN = 3;

        int MAC = 4;

        int LINUX = 5;

        int WEB = 6;
    }

    /**
     * 音视频
     */
    interface CallMode {
        /**
         * 音频
         **/
        int CALL_AUDIO = 0;

        /**
         * 视频
         **/
        int CALL_VIDEO = 1;
    }

}
