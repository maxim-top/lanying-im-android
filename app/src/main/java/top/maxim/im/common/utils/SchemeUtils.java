
package top.maxim.im.common.utils;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import im.floo.BMXCallBack;
import im.floo.floolib.BMXMessage;
import top.maxim.im.MainActivity;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.bean.UserBean;
import top.maxim.im.login.view.WelcomeActivity;
import top.maxim.im.message.view.ChatBaseActivity;
import top.maxim.im.net.ConnectivityReceiver;
import top.maxim.im.net.HttpResponseCallback;
import top.maxim.im.sdk.utils.MessageDispatcher;

/**
 * Description : 自定义协议处理.
 */
public class SchemeUtils {
    private static final String LANYING_SCHEME = "lanying:";

    // 例如：lanying:sc?aid=welovemaxim&cid=12345678 指打开私聊窗口,app ID是welovemaxim,会话ID是12345678
    public static void handleScheme(BaseTitleActivity context, String urlString) {
        String rearPart = urlString.replaceAll(LANYING_SCHEME, "");
        String [] parts = rearPart.split("\\?", 2);
        if (parts.length != 2){
            ToastUtil.showTextViewPrompt(context.getString(R.string.uri_error));
            return;
        }
        String target = parts[0];
        String parameters = parts[1];
        String params[] = parameters.split("&");

        Map<String, String> kvMap = new HashMap<>();
        for(String param : params){
            String kv[] = param.split("=");
            String key = kv[0];
            String value = kv[1];
            kvMap.put(key, value);
        }

        String link = kvMap.get("link");
        if (TextUtils.isEmpty(link)) {
            return;
        }
        String code = kvMap.get("code");
        if (TextUtils.isEmpty(code)) {
            return;
        }
        awakeFromWeb(context, link, code, target);
    }

    public static void awakeFromWeb(BaseTitleActivity context, String link, String code, String target) {
        context.showLoadingDialog(true);
        AppManager.getInstance().getLanyingLinkInfo(link, new HttpResponseCallback<String>() {
            @Override
            public void onResponse(String result) {
                context.dismissLoadingDialog();
                try {
                    long uId = 0L;
                    String appId = null;
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has("uid")) {
                        uId = jsonObject.getLong("uid");
                    }
                    if (jsonObject.has("app_id")) {
                        appId = jsonObject.getString("app_id");
                    }
                    boolean isLogin = SharePreferenceUtils.getInstance().getLoginStatus();
                    if (isLogin){
                        SharePreferenceUtils.getInstance().putDeepLink("");
                        if (TextUtils.isEmpty(appId)){
                            return;
                        }
                        String curAppId = SharePreferenceUtils.getInstance().getAppId();
                        if (!TextUtils.equals(appId, curAppId)) {
                            String error = context.getString(R.string.network_error);
                            ToastUtil.showTextViewPrompt(error);
                            return;
                        }
                        if (TextUtils.equals(target, "sc")){
                            ChatBaseActivity.startChatActivity(context, BMXMessage.MessageType.Single, uId);
                        }else if (TextUtils.equals(target, "gc")){
                            ChatBaseActivity.startChatActivity(context, BMXMessage.MessageType.Group, uId);
                        }
                    }else{
                        context.showLoadingDialog(true);
                        String finalAppId = appId;
                        AppManager.getInstance().getSecretInfo(code, new HttpResponseCallback<String>() {
                            @Override
                            public void onResponse(String result) {
                                context.dismissLoadingDialog();
                                SharePreferenceUtils.getInstance().putAppId(finalAppId);
                                UserManager.getInstance().changeAppId(finalAppId, bmxErrorCode -> {});
                                String username = null;
                                String password = null;
                                try {
                                    JSONObject jsonObject = new JSONObject(result);
                                    if (jsonObject.has("secret_text")) {
                                        String text = jsonObject.getString("secret_text");
                                        JSONObject textObject = new JSONObject(text);
                                        if (textObject.has("username")) {
                                            username = textObject.getString("username");
                                        }
                                        if (textObject.has("password")) {
                                            password = textObject.getString("password");
                                        }
                                    }

                                    String finalPassword = password;
                                    BMXCallBack callBack = (bmxErrorCode) -> {
                                        if (BaseManager.bmxFinish(bmxErrorCode)) {
                                            SharePreferenceUtils.getInstance().putAppIdHistory();
                                            //启动网络监听
                                            ConnectivityReceiver.start(AppContextUtils.getApplication());

                                            // 登陆成功后 需要将userId存储SP 作为下次自动登陆
                                            UserManager.getInstance().getProfile(true, (bmxErrorCode1, profile) -> {
                                                TaskDispatcher.exec(() -> {
                                                    if (BaseManager.bmxFinish(bmxErrorCode1) && profile != null
                                                            && profile.userId() > 0) {
                                                        UserBean bean = new UserBean(profile.username(), profile.userId(), finalPassword,
                                                                finalAppId, System.currentTimeMillis());
                                                        CommonUtils.getInstance().addUser(bean);
                                                    }
                                                    TaskDispatcher.postMain(() -> {
                                                        if (context instanceof BaseTitleActivity
                                                                && !context.isFinishing()) {
                                                            ((BaseTitleActivity)context).dismissLoadingDialog();
                                                        }
                                                        // 登陆成功消息预加载
                                                        WelcomeActivity.initData();
                                                        SharePreferenceUtils.getInstance().putLoginStatus(true);
                                                        MessageDispatcher.getDispatcher().initialize();
                                                        MainActivity.openMain(context);
                                                    });
                                                });
                                            });
                                            return;
                                        }
                                        // 失败
                                        if (context instanceof BaseTitleActivity && !context.isFinishing()) {
                                            ((BaseTitleActivity)context).dismissLoadingDialog();
                                        }
                                        String error = bmxErrorCode != null ? bmxErrorCode.name() : context.getString(R.string.network_exception);
                                        ToastUtil.showTextViewPrompt(error);
                                    };
                                    if (context instanceof BaseTitleActivity && !context.isFinishing()) {
                                            ((BaseTitleActivity)context).showLoadingDialog(true);
                                    }
                                    UserManager.getInstance().signInByName(username, password, callBack);
                                }catch (JSONException e){
                                    e.printStackTrace();
                                }
                            }
                            @Override
                            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                                context.dismissLoadingDialog();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                context.dismissLoadingDialog();
            }
        });
    }
}
