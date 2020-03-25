
package top.maxim.im.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;

import im.floo.floolib.BMXConversation;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXUserProfile;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.ChatManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.bean.MessageBean;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;
import top.maxim.im.message.view.ChatGroupActivity;
import top.maxim.im.message.view.ChatSingleActivity;
import top.maxim.im.sdk.utils.MsgConstants;

/**
 * Description : 在线push Created by Mango on 2019/06/13.
 */
public class PushReceiver extends BroadcastReceiver {

    public static final String TAG = "PushReceiver";

    private long mLastSendBroadcastTime = 0L;

    @Override
    public void onReceive(Context context, Intent intent) {
        // 是否显示消息内容
        String action = intent.getAction();
        // 获取自己的profile
        UserManager.getInstance().getProfile(false, (bmxErrorCode, profile) -> {
            if (BaseManager.bmxFinish(bmxErrorCode) && profile != null) {
                // 获取设置的push开关
                BMXUserProfile.MessageSetting setting = profile.messageSetting();
                boolean isPush = setting != null && setting.getMPushEnabled();
                boolean isPushDetail = setting != null && setting.getMPushDetail();
                boolean isPushSound = setting != null && setting.getMNotificationSound();
                boolean isPushVibrate = setting != null && setting.getMNotificationVibrate();
                if (!isPush) {
                    // 不push
                    return;
                }
                if (TextUtils.equals(action,
                        String.format(context.getString(R.string.im_push_msg_action),
                                context.getPackageName()))) {
                    MessageBean bean = (MessageBean)intent
                            .getSerializableExtra(MessageConfig.CHAT_MSG);
                    // 如果是我发送不通知
                    if (bean == null || !bean.isReceiveMsg()) {
                        return;
                    }
                    BMXMessage.MessageType type = bean.getType();
                    if (type == BMXMessage.MessageType.Single) {
                        // 查询单聊免打扰
                        RosterManager.getInstance().search(bean.getChatId(), false,
                                (bmxErrorCode1, rosterItem) -> {
                                    if (rosterItem == null || rosterItem.isMuteNotification()) {
                                        return;
                                    }
                                    String name = "";
                                    if (!TextUtils.isEmpty(rosterItem.alias())) {
                                        name = rosterItem.alias();
                                    } else {
                                        name = rosterItem.username();
                                    }
                                    handleNotify(context, name, bean, isPushSound, isPushVibrate,
                                            isPushDetail);
                                });
                    } else if (type == BMXMessage.MessageType.Group) {
                        GroupManager.getInstance().search(bean.getChatId(), false,
                                (bmxErrorCode1, groupItem) -> {
                                    if (groupItem == null
                                            || groupItem.msgMuteMode() != null && groupItem
                                                    .msgMuteMode() == BMXGroup.MsgMuteMode.MuteChat) {
                                        return;
                                    }
                                    String name = groupItem.name();
                                    handleNotify(context, name, bean, isPushSound, isPushVibrate,
                                            isPushDetail);
                                });
                    }
                }
            }
        });
    }
    
    private void handleNotify(Context context, String name, MessageBean bean, boolean isPushSound,
            boolean isPushVibrate, boolean isPushDetail) {
        if (handleSoundAndShake(context, bean, isPushSound, isPushVibrate)) {
            return;
        }
        if (isPushDetail) {
            showChatNotification(name, bean);
        } else {
            hideDetailContentMsgNotification(context, bean);
        }
    }

    /**
     * 处理震动和声响 return 是否直接返回
     */
    private boolean handleSoundAndShake(Context context, MessageBean bean, boolean voice,
            boolean vibrate) {
        if (bean == null) {
            // 离线消息不响铃
            return true;
        }
        // 广播间隔大于2s 不弹出
        if (System.currentTimeMillis() - mLastSendBroadcastTime <= 2 * 1000) {
            return true;
        }
        mLastSendBroadcastTime = System.currentTimeMillis();
        boolean isForeground = PushUtils.getInstance().isAppForeground();
        if (isForeground) {
            return true;
        }
        // 如果不在前台，响铃和震动
        if (voice) {
            SoundManager.getInstance().playPromitReceMsgVoice(context);
        }
        if (vibrate) {
            SoundManager.getInstance().vibrate(context);
        }
        return false;
    }

    /**
     * 显示聊天的通知
     */
    private void showChatNotification(String title, MessageBean bean) {
        if (bean == null) {
            return;
        }
        BMXConversation.Type type = null;
        if (bean.getType() == BMXMessage.MessageType.Single) {
            type = BMXConversation.Type.Single;
        } else {
            type = BMXConversation.Type.Group;
        }
        ChatManager.getInstance().openConversation(bean.getChatId(), type, false,
                (bmxErrorCode, conversation) -> {
                    int count = BaseManager.bmxFinish(bmxErrorCode) && conversation != null
                            ? conversation.unreadNumber()
                            : 0;
                    StringBuilder content = new StringBuilder();
                    if (count > 1) {
                        content.append("[").append(count).append("条]");
                    }
                    if (!TextUtils.isEmpty(title)) {
                        content.append(title).append(":");
                    }
                    content.append(ChatUtils.getInstance().getMessageDesc(bean));
                    Intent intent = new Intent();
                    if (bean.getType() == BMXMessage.MessageType.Single) {
                        intent.setClass(AppContextUtils.getAppContext(), ChatSingleActivity.class);
                    } else {
                        intent.setClass(AppContextUtils.getAppContext(), ChatGroupActivity.class);
                    }
                    intent.putExtra(MessageConfig.CHAT_ID, bean.getChatId());
                    // 跳到通知
                    NotificationUtils.getInstance().showNotify(
                            MsgConstants.ChannelImportance.PRIVATE, title, content.toString(),
                            intent, String.valueOf(bean.getChatId()).hashCode(), count);
                });
    }

    /**
     * 不显示具体消息内容
     *
     * @param context 上下文
     */
    private void hideDetailContentMsgNotification(Context context, MessageBean bean) {
        if (bean == null) {
            return;
        }
        String title = context.getApplicationInfo().name;
        ApplicationInfo info = context.getApplicationInfo();
        if (null != info) {
            CharSequence appLabel = context.getPackageManager().getApplicationLabel(info);
            title = appLabel.toString();
        }
        if (TextUtils.isEmpty(title)) {
            title = "新消息";
        }
        BMXConversation.Type type = null;
        if (bean.getType() == BMXMessage.MessageType.Single) {
            type = BMXConversation.Type.Single;
        } else {
            type = BMXConversation.Type.Group;
        }
        String finalTitle = title;
        ChatManager.getInstance().openConversation(bean.getChatId(), type, false,
                (bmxErrorCode, conversation) -> {
                    int count = BaseManager.bmxFinish(bmxErrorCode) && conversation != null
                            ? conversation.unreadNumber()
                            : 0;
                    String content = context.getString(R.string.push_receiver_rec_msg_count,
                            String.valueOf(count <= 0 ? 1 : count));

                    Intent intent = new Intent();
                    if (bean.getType() == BMXMessage.MessageType.Single) {
                        intent.setClass(AppContextUtils.getAppContext(), ChatSingleActivity.class);
                    } else {
                        intent.setClass(AppContextUtils.getAppContext(), ChatGroupActivity.class);
                    }
                    intent.putExtra(MessageConfig.CHAT_ID, bean.getChatId());
                    // 跳到通知
                    NotificationUtils.getInstance().showNotify(
                            MsgConstants.ChannelImportance.PRIVATE, finalTitle, content, intent,
                            String.valueOf(bean.getChatId()).hashCode(), count);
                });
        
    }
}
