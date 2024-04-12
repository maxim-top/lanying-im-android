
package top.maxim.im.wxapi;

import static com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject.MINIPTOGRAM_TYPE_RELEASE;

import android.text.TextUtils;

import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import top.maxim.im.R;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.push.PushClientMgr;

/**
 * Description : 微信
 */
public class WXUtils {

    private static volatile WXUtils mInstance;

    private IWXAPI mApi;

    private int mSource;

    private String mAppId;

    private WXUtils() {
        String appId = PushClientMgr.getPushAppId("WEIXIN_APPID");
        if (!TextUtils.isEmpty(appId)) {
            mApi = WXAPIFactory.createWXAPI(AppContextUtils.getAppContext(), appId, true);
            mApi.registerApp(appId);
        }
    }

    public static WXUtils getInstance() {
        if (mInstance == null) {
            synchronized (WXUtils.class) {
                if (mInstance == null) {
                    mInstance = new WXUtils();
                }
            }
        }
        return mInstance;
    }

    /**
     * 判断微信是否支持
     * 
     * @return boolean
     */
    public boolean wxSupported() {
        if (mApi == null) {
            return false;
        }
        return mApi.isWXAppInstalled();
    }

    /**
     * 微信登录
     */
    public void wxLogin(int source, String appId) {
        if (mApi == null) {
            return;
        }
        mSource = source;
        mAppId = appId;
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = AppContextUtils.getAppContext().getString(R.string.block_message);
        mApi.sendReq(req);
    }

    /**
     * 微信登录
     */
    public void wxMiniProgram(String wxUsername, String wxPath) {
        if (mApi == null) {
            return;
        }
        WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
        req.userName = wxUsername;
        req.path = wxPath;
        req.miniprogramType = MINIPTOGRAM_TYPE_RELEASE;
        mApi.sendReq(req);
    }

    public IWXAPI getWXApi() {
        return mApi;
    }

    public int getSource() {
        return mSource;
    }

    public String getAppId() {
        return mAppId;
    }
}
