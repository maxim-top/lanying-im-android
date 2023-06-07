
package top.maxim.im.bmxmanager;

import android.app.Application;

import im.floo.floolib.BMXRTCEngine;
import im.floo.floolib.BMXRTCService;
import im.floo.floolib.BMXRTCServiceListener;
import top.maxim.rtc.engine.MaxEngine;

/**
 * Description : RTC Created by Mango on 2018/12/2.
 */
public class RTCManager extends BaseManager {

    private static final String TAG = RTCManager.class.getSimpleName();

    private static final RTCManager sInstance = new RTCManager();

    private BMXRTCService mService;

    public static RTCManager getInstance() {
        return sInstance;
    }

    private RTCManager() {
        mService = bmxClient.getRTCManager();
    }

    public void init(Application application){
        MaxEngine.Companion.init(application);
        mService.setupRTCEngine(new MaxEngine(bmxClient));
//        UCloudEngine.init(application);
//        mService.setupRTCEngine(mEngine = new UCloudEngine());
    }

    public BMXRTCEngine getRTCEngine(){
        return mService.getRTCEngine();
    }

    /**
     * 添加监听者
     **/
    public void addRTCServiceListener(BMXRTCServiceListener listener) {
        mService.addRTCServiceListener(listener);
    }

    /**
     * 移除监听者
     **/
    public void removeRTCServiceListener(BMXRTCServiceListener listener) {
        mService.removeRTCServiceListener(listener);
    }
}
