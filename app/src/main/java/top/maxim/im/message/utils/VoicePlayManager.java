
package top.maxim.im.message.utils;

import android.util.LongSparseArray;

import im.floo.floolib.BMXMessage;
import top.maxim.im.message.interfaces.VoicePlayCallback;

;

/**
 * Description :语音播放管理
 */
public final class VoicePlayManager {

    private static final VoicePlayManager manager = new VoicePlayManager();

    private static final String TAG = VoicePlayManager.class.getSimpleName();

    // model 监听，每个任务只有一个
    private LongSparseArray<VoicePlayCallback> mListeners = new LongSparseArray<>();

    private VoicePlayManager() {
    }

    public static VoicePlayManager getInstance() {
        return manager;
    }

    /**
     * 注册页面监听
     *
     * @param referenceId 资源标识id
     * @param callback 回调
     */
    public void registerListener(long referenceId, VoicePlayCallback callback) {
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


    public void onStartCallback(BMXMessage msg) {
        if (msg == null) {
            return;
        }
        long msgId = msg.msgId();
        if (mListeners.get(msgId) != null) {
            mListeners.get(msgId).onStart(msgId);
        }
    }

    public void onFinishCallback(BMXMessage msg) {
        if (msg == null) {
            return;
        }
        long msgId = msg.msgId();
        if (mListeners.get(msgId) != null) {
            mListeners.get(msgId).onFinish(msgId);
        }
    }

    public void onFailCallback(BMXMessage msg) {
        if (msg == null) {
            return;
        }
        long msgId = msg.msgId();
        if (mListeners.get(msgId) != null) {
            mListeners.get(msgId).onFail(msgId);
        }
    }
}
