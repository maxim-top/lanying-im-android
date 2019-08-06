
package top.maxim.im.message.interfaces;

import android.os.Handler;
import android.os.Looper;

import im.floo.floolib.ossclient.BMXHttpProgressDispatcher;
import im.floo.floolib.ossclient.BMXHttpProgressHandler;

/**
 * 上传下载监听
 */
public abstract class FileCallback {

    private BMXHttpProgressHandler mHandler;

    private static Handler mainHandler = new Handler(Looper.getMainLooper());

    public FileCallback(String path) {
        mHandler = new BMXHttpProgressHandler(path) {

            @Override
            protected void onCompleted(String path, boolean isThumbnail) {
                super.onCompleted(path, isThumbnail);
                BMXHttpProgressDispatcher.getInstance().removeHttpProgressHandler(mHandler);
                onCallFinish(path, isThumbnail);
            }

            @Override
            protected void onProgress(String path, long current, long total, boolean isThumbnail) {
                super.onProgress(path, current, total, isThumbnail);
                onCallProgress(current, path, isThumbnail);
            }

            @Override
            protected void onFailed(String path, boolean isThumbnail, Exception ex) {
                super.onFailed(path, isThumbnail, ex);
                BMXHttpProgressDispatcher.getInstance().removeHttpProgressHandler(mHandler);
                onCallFail(path, isThumbnail);
            }
        };
        BMXHttpProgressDispatcher.getInstance().addHttpProgressHandler(mHandler);
    }

    /* C层回调回来是子线程 切换到主线程 */
    public final void onCallProgress(final long percent, final String path, final boolean isThumbnail) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                onProgress(percent, path, isThumbnail);
            }
        });
    }

    /* C层回调回来是子线程 切换到主线程 */
    public final void onCallFinish(final String url, final boolean isThumbnail) {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onFinish(url, isThumbnail);
            }
        }, 500);
    }

    /* C层回调回来是子线程 切换到主线程 */
    public final void onCallFail(final String path, final boolean isThumbnail) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                onFail(path, isThumbnail);
            }
        });
    }

    protected abstract void onProgress(long percent, String path, boolean isThumbnail);

    protected abstract void onFinish(String url, boolean isThumbnail);

    protected abstract void onFail(String path, boolean isThumbnail);

}
