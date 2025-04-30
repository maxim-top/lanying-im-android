
package top.maxim.im.message.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import im.floo.floolib.BMXConversation;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageConfig;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.ListOfLongLong;
import top.maxim.im.R;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.TimeUtils;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.RecyclerWithHFAdapter;
import top.maxim.im.message.utils.ChatUtils;

/**
 * Description : 消息列表 Created by Mango on 2018/11/05.
 */
public class SessionAdapter extends RecyclerWithHFAdapter<BMXConversation> {

    private ImageRequestConfig mConfig;

    private ImageRequestConfig mGroupConfig;

    private Context mContext;

    public SessionAdapter(Context context) {
        super(context);
        mContext = context;
        mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
                .showImageForEmptyUri(R.drawable.default_avatar_icon)
                .showImageOnFail(R.drawable.default_avatar_icon)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageOnLoading(R.drawable.default_avatar_icon).build();
        mGroupConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
                .showImageForEmptyUri(R.drawable.default_group_icon)
                .showImageOnFail(R.drawable.default_group_icon)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageOnLoading(R.drawable.default_group_icon).build();
    }

    @Override
    protected int onCreateViewById(int viewType) {
        return R.layout.item_session_view;
    }

    @Override
    protected void onBindHolder(BaseViewHolder holder, int position) {
        ShapeImageView avatar = holder.findViewById(R.id.session_avatar);
        TextView tvTitle = holder.findViewById(R.id.session_title);
        TextView desc = holder.findViewById(R.id.session_desc);
        TextView time = holder.findViewById(R.id.session_time);
        TextView tvUnReadCount = holder.findViewById(R.id.session_unread_num);
        ImageView ivDisturb = holder.findViewById(R.id.session_disturb);

        BMXConversation item = getItem(position);
        // 是否开启免打扰
        boolean isDisturb = false;
        BMXConversation.Type type = item == null ? null : item.type();
        String name = "";
        if (type != null && (type == BMXConversation.Type.Single ||
                type == BMXConversation.Type.System)) {
            BMXRosterItem rosterItem = RosterFetcher.getFetcher().getRoster(item.conversationId());
            if (rosterItem != null && !TextUtils.isEmpty(rosterItem.alias())) {
                name = rosterItem.alias();
            } else if (rosterItem != null && !TextUtils.isEmpty(rosterItem.nickname())) {
                name = rosterItem.nickname();
            } else if (rosterItem != null) {
                name = rosterItem.username();
            }
            if (item.conversationId() == 0){
                name = mContext.getString(R.string.sys_msg);
            }
            ChatUtils.getInstance().showRosterAvatar(rosterItem, avatar, mConfig);
            isDisturb = rosterItem != null && rosterItem.isMuteNotification();
        } else if (type != null && type == BMXConversation.Type.Group) {
            BMXGroup groupItem = RosterFetcher.getFetcher().getGroup(item.conversationId());
            name = groupItem != null ? groupItem.name() : "";
            ChatUtils.getInstance().showGroupAvatar(groupItem, avatar, mGroupConfig);
            isDisturb = groupItem != null && groupItem.msgMuteMode() != null
                    && groupItem.msgMuteMode() == BMXGroup.MsgMuteMode.MuteChat;
        } else {
            ChatUtils.getInstance().showRosterAvatar(null, avatar, mConfig);
        }
        BMXMessage lastMsg = item == null ? null : item.lastMsg();
        int unReadCount = item == null ? 0 : item.unreadNumber();
        if (isDisturb) {
            tvUnReadCount.setVisibility(View.GONE);
            ivDisturb.setVisibility(unReadCount > 0 ? View.VISIBLE : View.GONE);
        } else {
            ivDisturb.setVisibility(View.GONE);
            if (unReadCount > 0) {
                tvUnReadCount.setVisibility(View.VISIBLE);
                tvUnReadCount.setText(String.valueOf(unReadCount));
            } else {
                tvUnReadCount.setVisibility(View.GONE);
            }
        }
        tvTitle.setText(TextUtils.isEmpty(name) ? "" : name);
        time.setText(lastMsg != null ? TimeUtils.millis2StringOnConversationList(mContext, lastMsg.serverTimestamp()) : "");
        String draft = item == null ? "" : item.editMessage();
        if (!TextUtils.isEmpty(draft)) {
            // 有草稿
            SpannableStringBuilder spannable = new SpannableStringBuilder();
            String draftText = mContext.getString(R.string.draft);
            SpannableString spannableString = new SpannableString(draftText);
            spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0, draftText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.append(spannableString).append(draft);
            desc.setText(spannable);
        } else {
            String msgDesc = ChatUtils.getInstance().getMessageDesc(mContext, lastMsg);
            if (lastMsg != null && lastMsg.isReceiveMsg() && lastMsg.config() != null) {
                // 有@
                try {
                    BMXMessageConfig config = lastMsg.config();
                    if (config.getMentionAll()) {
                        msgDesc = mContext.getString(R.string.someone_at_you) + msgDesc;
                    } else {
                        ListOfLongLong atList = config.getMentionList();
                        if (atList != null && !atList.isEmpty()) {
                            for (int i = 0; i < atList.size(); i++) {
                                if (atList.get(
                                        i) == (SharePreferenceUtils.getInstance().getUserId())) {
                                    msgDesc = mContext.getString(R.string.someone_at_you) + msgDesc;
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            desc.setText(!TextUtils.isEmpty(msgDesc) ? msgDesc : "");
        }
    }
}
