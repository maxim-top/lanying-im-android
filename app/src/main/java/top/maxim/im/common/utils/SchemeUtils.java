
package top.maxim.im.common.utils;

import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import im.floo.floolib.BMXMessage;
import top.maxim.im.R;
import top.maxim.im.message.view.ChatBaseActivity;
import top.maxim.im.wxapi.WXUtils;

/**
 * Description : 自定义协议处理.
 */
public class SchemeUtils {
    private static final String LANYING_SCHEME = "lanying:";
    private static final String DEST_SINGLE_CHAT = "sc";
    private static final String DEST_GROUP_CHAT = "gc";
    private static final String PARAM_APP_ID = "aid";
    private static final String PARAM_CONVERSATION_ID = "cid";

    // 例如：lanying:sc?aid=welovemaxim&cid=12345678 指打开私聊窗口,app ID是welovemaxim,会话ID是12345678
    public static void handleScheme(Context context, String urlString) {
        SharePreferenceUtils.getInstance().putDeepLink("");
        if (!urlString.startsWith(LANYING_SCHEME)){
            ToastUtil.showTextViewPrompt(context.getString(R.string.please_install_wechat));
            return;
        }
        String rearPart = urlString.replaceAll(LANYING_SCHEME, "");
        String [] parts = rearPart.split("\\?", 2);
        if (parts.length != 2){
            ToastUtil.showTextViewPrompt(context.getString(R.string.uri_error));
            return;
        }
        String destination = parts[0];
        String parameters = parts[1];
        String params[] = parameters.split("&");

        Map<String, String> kvMap = new HashMap<>();
        for(String param : params){
            String kv[] = param.split("=");
            String key = kv[0];
            String value = kv[1];
            kvMap.put(key, value);
        }

        String appId = kvMap.get(PARAM_APP_ID);
        if (TextUtils.isEmpty(appId)) {
            ToastUtil.showTextViewPrompt(context.getString(R.string.app_id_required));
            return;
        }

        String curAppId = SharePreferenceUtils.getInstance().getAppId();
        if (!appId.equals(curAppId)){
            ToastUtil.showTextViewPrompt(context.getString(R.string.change_app_id_to)+appId);
            return;
        }
        String conversation = kvMap.get(PARAM_CONVERSATION_ID);
        long conversationId = Long.parseLong(conversation);
        if (destination.equals(DEST_SINGLE_CHAT)){
            ChatBaseActivity.startChatActivity(context, BMXMessage.MessageType.Single, conversationId);
        }else if (destination.equals(DEST_GROUP_CHAT)){
            ChatBaseActivity.startChatActivity(context, BMXMessage.MessageType.Group, conversationId);
        }else{
            ToastUtil.showTextViewPrompt(context.getString(R.string.uri_error));
        }
    }
}
