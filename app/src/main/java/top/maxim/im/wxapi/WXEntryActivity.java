
package top.maxim.im.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import top.maxim.im.R;
import top.maxim.im.common.utils.CommonConfig;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.ToastUtil;

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
        ToastUtil.showTextViewPrompt(getString(R.string.authorization_failed));
        finish();
    }

    /**
     * 微信登录
     */
    private void loginWeChat(String code) {
        Intent intent = new Intent();
        intent.setAction(CommonConfig.WX_LOGIN_ACTION);
        intent.putExtra(CommonConfig.WX_OPEN_ID, code);
        RxBus.getInstance().send(intent);
        finish();
    }
}
