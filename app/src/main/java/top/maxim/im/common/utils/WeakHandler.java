
package top.maxim.im.common.utils;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Description : 软引用handler Created by Mango on 2018/11/18.
 */
public class WeakHandler<T> extends Handler {

    private WeakReference<T> mActivity;

    public WeakHandler(T t) {
        this.mActivity = new WeakReference(t);
    }

    public WeakReference<T> getActivity() {
        return mActivity;
    }

    @Override
    public final void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (mActivity.get() == null) {
            return;
        }
        handleWeakMessage(msg);
    }
    
    protected void handleWeakMessage(Message msg) {

    }
}
