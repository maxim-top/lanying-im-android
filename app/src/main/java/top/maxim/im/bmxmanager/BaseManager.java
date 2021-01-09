
package top.maxim.im.bmxmanager;

import android.content.Context;
import android.text.TextUtils;

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
import top.maxim.im.scan.config.ScanConfigs;

public class BaseManager {

    protected static BMXClient bmxClient;

    static {
        System.loadLibrary("floo");
    }

    /**
     * 配置环境
     */
    public static void initBMXSDK() {
        String appPath = AppContextUtils.getAppContext().getFilesDir().getPath();
        File dataPath = new File(appPath + "/data_dir");
        File cachePath = new File(appPath + "/cache_dir");
        dataPath.mkdirs();
        cachePath.mkdirs();

        String pushId = getPushId();
        BMXSDKConfig conf = new BMXSDKConfig(BMXClientType.Android, "1", dataPath.getAbsolutePath(),
                cachePath.getAbsolutePath(), TextUtils.isEmpty(pushId) ? "MaxIM" : pushId);
        conf.setAppID(SharePreferenceUtils.getInstance().getAppId());
        conf.setAppSecret(ScanConfigs.CODE_SECRET);
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
        return BMXPushProviderType.Unknown;
    }

    /**
     * 配置环境
     */
    public static void changeDNS(String server, int port, String restServer) {
        if (bmxClient == null || bmxClient.getSDKConfig() == null) {
            return;
        }
        BMXSDKConfig conf = bmxClient.getSDKConfig();
        if (!TextUtils.isEmpty(server) && port > 0 && !TextUtils.isEmpty(restServer)) {
            // 三项数据都不为空才设置
            BMXSDKConfig.HostConfig hostConfig = conf.getHostConfig();
            hostConfig.setImHost(server);
            hostConfig.setImPort(port);
            hostConfig.setRestHost(restServer);
            conf.setHostConfig(hostConfig);
            conf.setEnableDNS(false);
        } else {
            conf.setEnableDNS(true);
        }
        bmxClient = BMXClient.create(conf);
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
            String metaAppId = PushClientMgr.getPushAppId("VIVO_APP_ID");
            if (!TextUtils.isEmpty(metaAppId)) {
                return metaAppId.substring(2);
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
        return bmxClient;
    }
}
