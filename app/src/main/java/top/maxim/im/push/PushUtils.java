
package top.maxim.im.push;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Description : pushUtils
 */
public class PushUtils {

    private volatile static PushUtils mInstance;

    private long mActivityCount = 1;

    private PushUtils() {
    }

    public static PushUtils getInstance() {
        if (mInstance == null) {
            synchronized (PushUtils.class) {
                if (mInstance == null) {
                    mInstance = new PushUtils();
                }
            }
        }
        return mInstance;
    }

    /**
     * 所有activity生病周期的回调
     */
    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
            mActivityCount++;
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
            mActivityCount--;
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    };

    // 注册页面监听
    public void registerActivityListener(Application application) {
        application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
        mActivityCount = 0;
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    // 取消页面监听
    public void unregisterActivityListener(Application application) {
        application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
        mActivityCount = 0;
    }

    /**
     * 是否在前台
     * @return boolean
     */
    public boolean isAppForeground() {
        return mActivityCount > 0;
    }
}
