
package top.maxim.im.bmxmanager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

import im.floo.floolib.BMXClient;
import im.floo.floolib.BMXClientType;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXLogLevel;
import im.floo.floolib.BMXPushEnvironmentType;
import im.floo.floolib.BMXPushProviderType;
import im.floo.floolib.BMXSDKConfig;
import rx.Observable;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.common.utils.RomUtil;
import top.maxim.im.common.utils.RxError;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.push.PushClientMgr;

public class BaseManager {

    protected static BMXClient bmxClient;

    static {
        System.loadLibrary("floo");
    }

    private static String getFilesPath( Context context ){
        return context.getFilesDir().getPath() ;
    }

    private static String getVersionName(Context context){
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "Unknown"; // 如果出现异常，返回"Unknown"
        }
    }

    private static String getUserAgent(Context context){
        return android.os.Build.BRAND + ";" + Build.MODEL + ";" + Build.VERSION.SDK_INT  +
                ";AppVer:" + getVersionName(context) + ";PackName:"+ context.getPackageName();
    }

    /**
     * 配置环境
     */
    public static void initBMXSDK() {
        String appPath = getFilesPath(AppContextUtils.getAppContext());
        File dataPath = new File(appPath + "/data_dir");
        File cachePath = new File(appPath + "/cache_dir");
        dataPath.mkdirs();
        cachePath.mkdirs();

        String pushId = getPushId();
        BMXSDKConfig conf = new BMXSDKConfig(BMXClientType.Android, "1", dataPath.getAbsolutePath(),
                cachePath.getAbsolutePath(), TextUtils.isEmpty(pushId) ? "MaxIM" : pushId, getUserAgent(AppContextUtils.getAppContext()) );
        conf.setAppID(SharePreferenceUtils.getInstance().getAppId());
//        conf.setAppSecret(ScanConfigs.CODE_SECRET);
        conf.setConsoleOutput(true);
        conf.setLoadAllServerConversations(true);
        conf.setLogLevel(BMXLogLevel.Debug);
        conf.setDeviceUuid(RomUtil.getDeviceId());
        conf.setEnvironmentType(BMXPushEnvironmentType.Production);
        conf.setPushProviderType(getProvideType(AppContextUtils.getAppContext()));
        bmxClient = BMXClient.create(conf);
    }

    private static BMXPushProviderType getProvideType(Context context){
        if (PushClientMgr.isHuawei(context)) {
            return BMXPushProviderType.HuaWei;
        }
        if (PushClientMgr.isXiaomi(context)) {
            return BMXPushProviderType.XiaoMi;
        }
        if (PushClientMgr.isMeizu(context)) {
            return BMXPushProviderType.MeiZu;
        }
        if (PushClientMgr.isOppo(context)) {
            return BMXPushProviderType.OPPS;
        }
        if (PushClientMgr.isVivo(context)) {
            return BMXPushProviderType.VIVO;
        }
        if(PushClientMgr.isGoogle(context)){
            return BMXPushProviderType.FCM;
        }
        return BMXPushProviderType.Unknown;
    }

    /**
     * 配置环境
     */
    public static void changeDNS(String server, int port, String restServer) {
        if (bmxClient == null || bmxClient.getSDKConfig() == null) {
            return;
        }
        //Same config of dns enabled
        if (bmxClient.getSDKConfig().getEnableDNS() && (TextUtils.isEmpty(server) || port <= 0 || TextUtils.isEmpty(restServer))){
            return;
        }
        BMXSDKConfig conf  = bmxClient.getSDKConfig();
        BMXSDKConfig.HostConfig hostConfig = conf.getHostConfig();
        //Same config of dns disabled
        if (!bmxClient.getSDKConfig().getEnableDNS() &&
                hostConfig.getImHost().equals(server) &&
                hostConfig.getImPort() == port &&
                hostConfig.getRestHost().equals(restServer)){
            return;
        }
        if (!TextUtils.isEmpty(server) && port > 0 && !TextUtils.isEmpty(restServer)) {
            // 三项数据都不为空才设置
            hostConfig.setImHost(server);
            hostConfig.setImPort(port);
            hostConfig.setRestHost(restServer);
            conf.setHostConfig(hostConfig);
            conf.setEnableDNS(false);
        } else {
            conf.setEnableDNS(true);
        }
    }

    public static String getPushId() {
        Context context = AppContextUtils.getAppContext();
        if (context == null) {
            return "";
        }
        if (PushClientMgr.isHuawei(context)) {
            String metaAppId = PushClientMgr.getPushAppId("HUAWEI_APPID");
            if (!TextUtils.isEmpty(metaAppId)) {
                return metaAppId.substring(2);
            }
        } else if (PushClientMgr.isXiaomi(context)) {
            String metaAppId = PushClientMgr.getPushAppId("XIAOMI_APPID");
            if (!TextUtils.isEmpty(metaAppId)) {
                return metaAppId.substring(2);
            }
            return "";
        } else if (PushClientMgr.isMeizu(context)) {
            String metaAppId = PushClientMgr.getPushAppId("MEIZU_APPID");
            if (!TextUtils.isEmpty(metaAppId)) {
                return metaAppId.substring(2);
            }
            return "";
        } else if (PushClientMgr.isOppo(context)) {
            String metaAppKey = PushClientMgr.getPushAppId("OPPO_APP_KEY");
            if (!TextUtils.isEmpty(metaAppKey)) {
                return metaAppKey.substring(2);
            }
        } else if (PushClientMgr.isVivo(context)) {
            String metaAppId = PushClientMgr.getPushAppId("VIVO_APPID");
            if (!TextUtils.isEmpty(metaAppId)) {
                return metaAppId.substring(2);
            }
        } else if (PushClientMgr.isGoogle(context)) {
            Log.d("BaseManager", "Push client is Google");
            String metaAppId = PushClientMgr.getPushAppId("GOOGLE_PUSH_ID");
            if (!TextUtils.isEmpty(metaAppId)) {
                return metaAppId;
            }
        }
        return "";
    }

    public static <T> Observable<T> bmxFinish(T t, BMXErrorCode error) {
        if (error == null || error.swigValue() != BMXErrorCode.NoError.swigValue()) {
            return Observable.error(RxError.create(RxError.ERROR_TYPE_COMMON,
                    error == null ? -1 : error.swigValue(), error == null ? "" : error.name()));
        }
        return Observable.just(t);
    }

    public static boolean bmxFinish(BMXErrorCode error) {
        if (error == null || error.swigValue() != BMXErrorCode.NoError.swigValue()) {
            return false;
        }
        return true;
    }

    public static BMXClient getBMXClient() {
        if (bmxClient == null){
            initBMXSDK();
        }
        return bmxClient;
    }
}
