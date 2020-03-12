
package top.maxim.im.bmxmanager;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;

import im.floo.floolib.BMXClient;
import im.floo.floolib.BMXClientType;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXLogLevel;
import im.floo.floolib.BMXSDKConfig;
import rx.Observable;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.common.utils.RxError;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.push.PushClientMgr;

public class BaseManager {

    static {
        System.loadLibrary("floo");
    }

    protected static BMXClient bmxClient;

    /**
     * 配置环境
     */
    public static void initTestBMXSDK(int index) {
        String appPath = AppContextUtils.getAppContext().getFilesDir().getPath();
        File dataPath = new File(appPath + "/data_dir");
        File cachePath = new File(appPath + "/cache_dir");
        dataPath.mkdirs();
        cachePath.mkdirs();

        String pushId = getPushId();
        BMXSDKConfig conf = new BMXSDKConfig(BMXClientType.Android, "1", dataPath.getAbsolutePath(),
                cachePath.getAbsolutePath(), TextUtils.isEmpty(pushId) ? "MaxIM" : pushId);
        conf.setConsoleOutput(true);
        conf.setLoadAllServerConversations(true);
        conf.setLogLevel(BMXLogLevel.Debug);
        conf.setAppID(SharePreferenceUtils.getInstance().getAppId());
        
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

    public static BMXClient getBMXClient() {
        return bmxClient;
    }
}
