
package top.maxim.im.message.interfaces;

import android.os.Handler;
import android.os.Looper;

import im.floo.floolib.BMXMessage;

/**
 * 上传下载监听
 */
public abstract class MsgAttachmentCallback {

    private static Handler mainHandler = new Handler(Looper.getMainLooper());

    public final void onCallProgress(BMXMessage msg, int percent) {
        if (msg == null) {
            mainHandler.post(() -> onFail(-1));
            return;
        }
        long msgId = msg.msgId();
        if (percent >= 100) {
            mainHandler.post(() -> onFinish(msgId));
        } else {
            mainHandler.post(() -> onProgress(msgId, percent));
        }
    }
    
    public abstract void onProgress(long msgId, int percent);

    public abstract void onFinish(long msgId);

    public abstract void onFail(long msgId);

}
