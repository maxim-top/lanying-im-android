
package top.maxim.im.push;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.heytap.mcssdk.PushManager;
import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.meizu.cloud.pushsdk.util.MzSystemUtils;
import com.vivo.push.PushClient;
import com.xiaomi.mipush.sdk.MiPushClient;

import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.common.utils.RomUtil;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.net.HttpResponseCallback;
import top.maxim.im.push.huawei.HWPushManager;
import top.maxim.im.push.meizu.MZPushManager;
import top.maxim.im.push.oppo.OppoPushManager;
import top.maxim.im.push.xiaomi.MIPushManager;

/**
 * Description : push
 */
public final class PushClientMgr {

    private static IPushManager sManager;

    private static boolean isInited = false;

    private static int sDevType = 0;

    public static final int HW_TYPE = 1;

    public static final int MI_TYPE = 2;

    public static final int MZ_TYPE = 3;
    
    public static final int OPPO_TYPE = 4;
    
    public static final int VIVO_TYPE = 5;

    private static PushClientMgr sPushMgr = new PushClientMgr();

    private PushClientMgr() {
    }

    public static boolean initManager(Application application) {
        if (application == null) {
            return false;
        } else if (isInited) {
            return true;
        } else {
            if (isHuawei(application.getApplicationContext())) {
                sManager = new HWPushManager(application);
                sDevType = HW_TYPE;
            } else if (isXiaomi(application.getApplicationContext())) {
                sManager = new MIPushManager(application.getApplicationContext());
                sDevType = MI_TYPE;
            } else if (isMeizu(application.getApplicationContext())) {
                sManager = new MZPushManager(application.getApplicationContext());
                sDevType = MZ_TYPE;
            } else if (isOppo(application.getApplicationContext())) {
                sManager = new OppoPushManager(application.getApplicationContext());
                sDevType = OPPO_TYPE;
            }
//            else if (isVivo(application.getApplicationContext())) {
//                sManager = new VivoPushManager(application.getApplicationContext());
//                sDevType = VIVO_TYPE;
//            }
            else {
                sManager = new EmptyPushManager(application.getApplicationContext());
            }
            isInited = true;
            return true;
        }
    }

    /**
     * 判断华为
     */
    public static boolean isHuawei(Context context) {
        return RomUtil.isHuawei() && HuaweiApiAvailability.getInstance()
                .isHuaweiMobileServicesAvailable(context, 0) == ConnectionResult.SUCCESS;
    }

    /**
     * 判断小米
     */
    public static boolean isXiaomi(Context context) {
        return RomUtil.isXiaomi() && MiPushClient.shouldUseMIUIPush(context);
    }

    /**
     * 判断魅族
     */
    public static boolean isMeizu(Context context) {
        return RomUtil.isFlyme() && MzSystemUtils.isBrandMeizu(context);
    }

    /**
     * 判断oppo
     */
    public static boolean isOppo(Context context) {
        return RomUtil.isOppo() && PushManager.isSupportPush(context);
    }

    /**
     * 判断oppo
     */
    public static boolean isVivo(Context context) {
        if (!isInited) {
            PushClient.getInstance(context).initialize();
        }
        try {
            return RomUtil.isVivo() && PushClient.getInstance(context).isSupport();
        } catch (Exception e) {
        }
        return false;
    }

    public static PushClientMgr getManager() {
        if (!isInited) {
            throw new IllegalStateException(
                    "PushClientMgr not init , call initManager method before");
        } else {
            return sPushMgr;
        }
    }

    public static String getPushAppId(String name) {
        try {
            PackageManager e = AppContextUtils.getAppContext().getPackageManager();
            ApplicationInfo info = e.getApplicationInfo(
                    AppContextUtils.getAppContext().getPackageName(), PackageManager.GET_META_DATA);
            return info.metaData.getString(name);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void setPushToken(String token) {
        SharePreferenceUtils.getInstance().putPushToken(sDevType, token);
        if (TextUtils.isEmpty(token)) {
            return;
        }
        UserManager.getInstance().bindDevice(token, bmxErrorCode -> {
            if (!BaseManager.bmxFinish(bmxErrorCode)) {
                Log.e("bindDevice failed", bmxErrorCode.name());
            }
        });
        top.maxim.im.bmxmanager.PushManager.getInstance().bindDeviceToken(token, bmxErrorCode -> {
            if (!BaseManager.bmxFinish(bmxErrorCode)) {
                Log.e("bindDevice failed", bmxErrorCode.name());
            }
        });
        notifierBind(token);
    }

    /**
     * 绑定证书名和用户id
     * 
     * @param deviceToken
     */
    private static void notifierBind(String deviceToken) {
        if (TextUtils.isEmpty(deviceToken)) {
            return;
        }
        String appId = SharePreferenceUtils.getInstance().getAppId();
        String currentUserName = SharePreferenceUtils.getInstance().getUserName();
        String currentUserId = String.valueOf(SharePreferenceUtils.getInstance().getUserId());
        String pwd = SharePreferenceUtils.getInstance().getUserPwd();
        AppManager.getInstance().getTokenByName(currentUserName, pwd,
                new HttpResponseCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        AppManager.getInstance().notifierBind(result, deviceToken, appId,
                                currentUserId, BaseManager.getPushId(),
                                new HttpResponseCallback<Boolean>() {
                                    @Override
                                    public void onResponse(Boolean result) {

                                    }

                                    @Override
                                    public void onFailure(int errorCode, String errorMsg,
                                            Throwable t) {

                                    }
                                });
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {

                    }
                });
    }

    public static int getDeviceType() {
        return sDevType;
    }

    public void register(Context context) {
        if (sManager != null) {
            sManager.register(context);
        }
    }

    public void unRegister() {
        if (sManager != null) {
            sManager.unRegister();
        }
        // 底层实现解绑 上层不再调用
        // String token = SharePreferenceUtils.getInstance().getToken();
        // if (!TextUtils.isEmpty(token)) {
        // Observable.just(token).map(new Func1<String, BMXErrorCode>() {
        // @Override
        // public BMXErrorCode call(String s) {
        // return UserManager.getInstance().unbindDevice();
        // }
        // }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
        // @Override
        // public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
        // return BaseManager.bmxFinish(errorCode, errorCode);
        // }
        // }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
        // .subscribe(new Subscriber<BMXErrorCode>() {
        // @Override
        // public void onCompleted() {
        //
        // }
        //
        // @Override
        // public void onError(Throwable e) {
        // Log.e("bindDevice failed", e.getMessage());
        // }
        //
        // @Override
        // public void onNext(BMXErrorCode errorCode) {
        //
        // }
        // });
        // }
    }

    public void startReceiveNotification() {
        sManager.startReceiveNotification();
    }

    public void stopReceiveNotification() {
        sManager.stopReceiveNotification();
    }

}
