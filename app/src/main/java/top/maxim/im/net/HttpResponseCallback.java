
package top.maxim.im.net;

import android.os.Handler;
import android.os.Looper;

/**
 * 网络代理回调
 */
public abstract class HttpResponseCallback<T> {

    private static Handler mHandle;

    static {
        mHandle = new Handler(Looper.getMainLooper());
    }

    public abstract void onResponse(T result);

    /**
     * 正确返回
     *
     * @param result 具体实例
     */
    public void onCallResponse(T result) {
        mHandle.post(() -> onResponse(result));
    }

    /**
     * 错误返回
     *
     * @param errorCode 错误码
     * @param errorMsg 错误描述
     * @param t 异常
     */
    public abstract void onFailure(int errorCode, String errorMsg, Throwable t);

    public void onCallFailure(int errorCode, String errorMsg, Throwable t) {
        mHandle.post(() -> onFailure(errorCode, errorMsg, t));
    }
}
