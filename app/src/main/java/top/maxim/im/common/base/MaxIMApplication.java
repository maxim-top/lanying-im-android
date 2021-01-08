
package top.maxim.im.common.base;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.common.utils.FileConfig;
import top.maxim.im.common.utils.FileUtils;
import top.maxim.im.login.view.WelcomeActivity;
import top.maxim.im.push.PushClientMgr;
import top.maxim.im.push.PushUtils;

/**
 * Description : application Created by Mango on 2018/11/5.
 */
public class MaxIMApplication extends Application {

    private static final int THREAD_POOL_MAX = 5;

    private static final int MAX_IMG_W_FOR_MEMORY_CACHE = 240;

    private static final int MAX_IMG_H_FOR_MEMORY_CACHE = 240;

    private static final int MEMORY_CACHE_SIZE_MAX = 10 * 1024 * 1024;

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
        initUtils();
        initBMXSDK();
        Thread.setDefaultUncaughtExceptionHandler(restartHandler);
    }

    public void restartApp() {
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

    /**
     * 初始化sdk
     */
    private void initBMXSDK() {
        BaseManager.initBMXSDK();
    }

    /**
     * 是否主进程
     * @return boolean
     */
    private boolean isMainProcess(){
        String processName = "";
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return TextUtils.equals(processName, getPackageName());
    }
}
