
package top.maxim.im.common.base;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.os.LocaleList;
import androidx.multidex.MultiDex;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;
import java.util.Locale;

import im.floo.floolib.BMXClient;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.rtc.RTCManager;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.common.utils.FileConfig;
import top.maxim.im.common.utils.FileUtils;
import top.maxim.im.login.view.WelcomeActivity;
import top.maxim.im.push.PushClientMgr;
import top.maxim.im.push.PushUtils;
import top.maxim.im.BuildConfig;

/**
 * Description : application Created by Mango on 2018/11/5.
 */
public class MaxIMApplication extends Application {

    private static final int THREAD_POOL_MAX = 5;

    private static final int MAX_IMG_W_FOR_MEMORY_CACHE = 240;

    private static final int MAX_IMG_H_FOR_MEMORY_CACHE = 240;

    private static final int MEMORY_CACHE_SIZE_MAX = 10 * 1024 * 1024;

    public long typeWriterMsgId;

    private int activityAount = 0;

    private boolean isDisconnected = false;

    /**
     * Activity 生命周期监听，用于监控app前后台状态切换
     */
    ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (activityAount == 0) {
                //app回到前台
                if (isDisconnected){
                    boolean initialized = SharePreferenceUtils.getInstance().hasSDKInitialized();
                    if (initialized){
                        BMXClient client = BaseManager.getBMXClient();
                        client.reconnect();
                    }
                }
            }
            activityAount++;
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }
        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
            activityAount--;
            if (activityAount == 0) {
                //app切到后台
                try {
                    boolean initialized = SharePreferenceUtils.getInstance().hasSDKInitialized();
                    if (initialized &&
                            !RTCManager.getInstance().getRTCEngine().isOnCall &&
                            SharePreferenceUtils.getInstance().getHasPush()){
                        BMXClient client = BaseManager.getBMXClient();
                        client.disconnect();
                        isDisconnected = true;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }
        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    };

    private Thread.UncaughtExceptionHandler restartHandler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread thread, Throwable ex) {
            restartApp();
        }
    };

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        typeWriterMsgId = 0;
        initUtils();
        if (!BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(restartHandler);
        }
        initLanguage();
        SharePreferenceUtils.getInstance().putAgreeChecked(false);
        SharePreferenceUtils.getInstance().putSDKInitialized(false);
    }

    private String getCountry() {

        Locale locale;
        //7.0以上和7.0以下获取系统语言方式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        return locale.getCountry();
    }

    private void initLanguage() {
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        if (getCountry().equals("TW")) {
            config.locale = Locale.TAIWAN;
        } else if (getCountry().equals("US")) {
            config.locale = Locale.ENGLISH;
        } else {
            config.locale = Locale.CHINESE;
        }
        resources.updateConfiguration(config, dm);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("109568", name, importance);
            channel.setDescription("IM");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            channel.shouldShowLights();//是否会闪光
            channel.enableLights(true);//闪光
            channel.canShowBadge();//桌面launcher消息角标
            channel.enableVibration(true);//是否允许震动
            channel.setSound(Uri.parse("android.resource://"+ getPackageName() + "/"
                    + R.raw.message_received), Notification.AUDIO_ATTRIBUTES_DEFAULT);
            channel.getGroup();//获取通知渠道组
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void restartApp() {
        createNotificationChannel();
        WelcomeActivity.openWelcome(getApplicationContext());
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 初始化工具类
     */
    private void initUtils() {
        AppContextUtils.initApp(this);
        initImageLoader();
        // push
        PushClientMgr.initManager(this);
        PushUtils.getInstance().registerActivityListener(this);
        if (PushClientMgr.hasOfficialPush()){
            registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
        }
//        BuglyTask.get().init(this);
    }

    /**
     * 初始化图片查看
     */
    private void initImageLoader() {
        // 设置图片缓存目录
        File cacheDir = FileUtils.getFileByPath(FileConfig.DIR_APP_CACHE);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .threadPoolSize(THREAD_POOL_MAX).threadPriority(Thread.MIN_PRIORITY)
                .memoryCache(new UsingFreqLimitedMemoryCache(MEMORY_CACHE_SIZE_MAX))
                .memoryCacheExtraOptions(MAX_IMG_W_FOR_MEMORY_CACHE, MAX_IMG_H_FOR_MEMORY_CACHE)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCache(new UnlimitedDiskCache(cacheDir)).build();
        ImageLoader.getInstance().init(config);
    }
}
