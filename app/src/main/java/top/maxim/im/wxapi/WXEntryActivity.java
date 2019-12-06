
package top.maxim.im.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import org.json.JSONException;
import org.json.JSONObject;

import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.login.view.BindUserActivity;
import top.maxim.im.login.view.LoginActivity;
import top.maxim.im.net.HttpResponseCallback;

/**
 * Description : 微信
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (WXUtils.getInstance().getWXApi() != null) {
            WXUtils.getInstance().getWXApi().handleIntent(intent, this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (WXUtils.getInstance().getWXApi() != null) {
            WXUtils.getInstance().getWXApi().handleIntent(intent, this);
        }
    }

    @Override
    public void onReq(BaseReq baseReq) {
    }

    @Override
    public void onResp(BaseResp baseResp) {
        if (baseResp == null) {
            finish();
            return;
        }
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                // 同意
                String code = ((SendAuth.Resp)baseResp).code;
                loginWeChat(code);
                return;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                // 拒绝
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                // 取消
                break;
            default:
                break;
        }
        ToastUtil.showTextViewPrompt("授权失败");
        finish();
    }

    /**
     * 微信登录
     */
    private void loginWeChat(String code) {
        AppManager.getInstance().weChatLogin(code, new HttpResponseCallback<String>() {
            @Override
            public void onResponse(String result) {
                if (TextUtils.isEmpty(result)) {
                    ToastUtil.showTextViewPrompt("登陆失败");
                    finish();
                    return;
                }
                String appId = WXUtils.getInstance().getAppId();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (!jsonObject.has("user_id") || !jsonObject.has("password")) {
                        String openId = jsonObject.getString("openid");
                        // 没有userId 密码 需要跳转绑定微信页面
                        BindUserActivity.openBindUser(WXEntryActivity.this, openId, appId);
                        finish();
                        return;
                    }
                    // 直接登录
                    String userId = jsonObject.getString("user_id");
                    String pwd = jsonObject.getString("password");
                    LoginActivity.login(WXEntryActivity.this, userId, pwd, true, appId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                ToastUtil.showTextViewPrompt("登陆失败");
                finish();
            }
        });
    }
}
