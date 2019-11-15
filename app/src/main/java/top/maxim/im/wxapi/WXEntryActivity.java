
package top.maxim.im.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.login.view.LoginActivity;

/**
 * Description : 微信
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (WXUtils.getInstance().getWXApi() != null) {
            WXUtils.getInstance().getWXApi().handleIntent(getIntent(), this);
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
                LoginActivity.openLogin(this, code);
                finish();
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
}
