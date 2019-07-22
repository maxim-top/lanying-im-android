
package top.maxim.im.message.utils;

import android.util.LongSparseArray;

import im.floo.floolib.BMXMessage;
import top.maxim.im.message.interfaces.MsgAttachmentCallback;

;

/**
 * Description :聊天模块文件上传下载管理
 */
public final class ChatAttachmentManager {

    private static final ChatAttachmentManager manager = new ChatAttachmentManager();

    private static final String TAG = ChatAttachmentManager.class.getSimpleName();

    // model 监听，每个任务只有一个
    private LongSparseArray<MsgAttachmentCallback> mListeners = new LongSparseArray<>();

    private ChatAttachmentManager() {
    }

    public static ChatAttachmentManager getInstance() {
        return manager;
    }

    /**
     * 注册页面监听
     *
     * @param referenceId 资源标识id
     * @param callback 回调
     */
    public void registerListener(long referenceId, MsgAttachmentCallback callback) {
        mListeners.put(referenceId, callback);
    }

    /**
     * 取消页面监听
     *
     * @param referenceId 文件资源id
     */
    public void unRegisterListener(long referenceId) {
        mListeners.remove(referenceId);
    }

    public void onProgressCallback(BMXMessage msg, int percent) {
        if (msg == null) {
            return;
        }
        long msgId = msg.msgId();
        if (mListeners.get(msgId) != null) {
            mListeners.get(msgId).onCallProgress(msg, percent);
        }
    }
}
