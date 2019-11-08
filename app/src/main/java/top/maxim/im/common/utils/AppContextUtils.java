
package top.maxim.im.common.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;

/**
 * Description : 上下文工具类 Created by Mango on 2018/11/05.
 */

public final class AppContextUtils {
    private static Context mContext;

    private static Application mApplication;

    public static Context getAppContext() {
        if (mContext == null) {
            throw new IllegalStateException("AppContextUtils 's initApp not called!!!");
        }
        return mContext;
    }

    public static void initApp(Application application) {
        mApplication = application;
        mContext = application.getApplicationContext();
        ScreenUtils.init(mContext);
    }

    /**
     * 获取包名
     */
    public static String getPackageName(Context context) {
        try {
            return context.getPackageName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Application getApplication() {
        if (mApplication == null) {
            throw new IllegalStateException("AppContextUtils is initApp not called!!!");
        } else {
            return mApplication;
        }
    }

    public static Activity getActivity(Context context) {
        // context is activity
        if (context instanceof Activity) {
            return (Activity)context;
        }
        // context is the subclass of TintContextWrapper
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

}
