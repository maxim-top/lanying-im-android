
package top.maxim.im.message.itemholder;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXUserProfile;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.TimeUtils;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.contact.view.RosterDetailActivity;
import top.maxim.im.login.view.SettingUserActivity;
import top.maxim.im.message.interfaces.ChatActionListener;
import top.maxim.im.message.utils.ChatUtils;

/**
 * Description : 聊天基础item Created by Mango on 2018/11/18.
 */
public abstract class MessageItemBaseView extends FrameLayout implements IItemChatFactory {

    /* 头像 */
    protected ImageRequestConfig ICON_CONFIG;

    /**
     * 中间位置
     */
    public static final int ITEM_CENTER = -1;

    /**
     * 右边
     */
    public static final int ITEM_RIGHT = 0;

    /**
     * 左边
     */
    public static final int ITEM_LEFT = 1;

    /*  消息体 */
    protected BMXMessage mMaxMessage;

    protected Context mContext;

    protected ChatActionListener mActionListener;

    protected int mItemPos;

    private View mItemView;

    private TextView mTxtMessageTime;

    private TextView mUsetText;

    private ShapeImageView mIconView;

    private TextView mTvReadStatus;

    /* 发送失败图片 */
    private ImageView mSendFailImg;

    /* 发送中图片 */
    private ProgressBar mSendingImg;

    private boolean mShowReadAck;

    public MessageItemBaseView(Context context, ChatActionListener listener, int itemPos) {
        super(context);
        mContext = context;
        mActionListener = listener;
        mItemPos = itemPos;
        mItemView = initView(this);
        ICON_CONFIG = new ImageRequestConfig.Builder().cacheInMemory(true).cacheOnDisk(true)
                .showImageForEmptyUri(R.drawable.default_avatar_icon)
                .showImageOnFail(R.drawable.default_avatar_icon).cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageOnLoading(R.drawable.default_avatar_icon).build();
    }

    @Override
    public View obtainView(ViewGroup parent) {
        if (mItemPos == ITEM_LEFT) {
            mItemView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_base_left, parent,
                    false);
        } else if (mItemPos == ITEM_RIGHT) {
            mItemView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_base_right, parent,
                    false);
            mTvReadStatus = mItemView.findViewById(R.id.tv_read_status);
        } else {
            mItemView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_base_center,
                    parent, false);
        }
        FrameLayout frameLayout = (FrameLayout)mItemView.findViewById(R.id.fl_message);
        frameLayout.removeAllViews();
        frameLayout.addView(this);
        bindCommonView();
        return mItemView;
    }

    @Override
    public void bindData(BMXMessage maXMessage) {
        mMaxMessage = maXMessage;
        mTxtMessageTime.setText(TimeUtils.millis2String(mContext, maXMessage.serverTimestamp()));
        showHead();
        bindData();
        showReadStatus();
        showSendStatus();
    }

    /**
     * 公共view
     */
    private void bindCommonView() {
        mTxtMessageTime = mItemView.findViewById(R.id.message_time);
        if (mItemPos != ITEM_CENTER) {
            mIconView = mItemView.findViewById(R.id.message_avatar);
            mUsetText = mItemView.findViewById(R.id.message_name);
            mSendFailImg = mItemView.findViewById(R.id.img_sendfail);
            mSendFailImg.setVisibility(View.GONE);
            mSendingImg = mItemView.findViewById(R.id.img_sending);
            mSendingImg.setVisibility(View.GONE);
            mSendFailImg.setOnClickListener(v -> {
                if (mActionListener != null) {
                    mActionListener.onReSendMessage(mMaxMessage);
                }
            });
        }
    }

    protected abstract View initView(ViewGroup parent);

    protected abstract void bindData();

    @Override
    public void showChatExtra(boolean isShowTime, boolean showReadAck) {
        if (isShowTime) {
            mTxtMessageTime.setVisibility(VISIBLE);
        } else {
            mTxtMessageTime.setVisibility(GONE);
        }
        mShowReadAck = showReadAck;
    }

    /**
     * 展示头像
     */
    private void showHead() {
        if (mItemPos == ITEM_CENTER || mMaxMessage == null) {
            return;
        }
        String userName = null;
        boolean group = mMaxMessage.type() == BMXMessage.MessageType.Group;
        if (mMaxMessage.isReceiveMsg()) {
            BMXRosterItem item = RosterFetcher.getFetcher().getRoster(mMaxMessage.fromId());
            if(group){
                //如果是群  需要获取群成员名称
                BMXGroup.Member member = GroupManager.getInstance().getMemberByDB(mMaxMessage.conversationId(), mMaxMessage.fromId());
                if (item != null && !TextUtils.isEmpty(item.alias())) {
                    userName = item.alias();
                } else if (member != null && !TextUtils.isEmpty(member.getMGroupNickname())) {
                    userName = member.getMGroupNickname();
                } else if (item != null && !TextUtils.isEmpty(item.nickname())) {
                    userName = item.nickname();
                } else if (item != null) {
                    userName = item.username();
                }
            } else{
                if (item != null && !TextUtils.isEmpty(item.alias())) {
                    userName = item.alias();
                } else if (item != null && !TextUtils.isEmpty(item.nickname())) {
                    userName = item.nickname();
                } else if (item != null) {
                    userName = item.username();
                }
            }
            if (mIconView != null) {
                ChatUtils.getInstance().showRosterAvatar(item, mIconView, ICON_CONFIG);
            }
        } else {
            BMXUserProfile profile = RosterFetcher.getFetcher().getProfile();
            if (group) {
                //如果是群  需要获取群成员名称
                BMXGroup.Member member = GroupManager.getInstance().getMemberByDB(mMaxMessage.conversationId(), mMaxMessage.fromId());
                if (member != null && !TextUtils.isEmpty(member.getMGroupNickname())) {
                    userName = member.getMGroupNickname();
                } else if (profile != null && !TextUtils.isEmpty(profile.nickname())) {
                    userName = profile.nickname();
                } else if (profile != null) {
                    userName = profile.username();
                }
            } else {
                if (profile != null && !TextUtils.isEmpty(profile.nickname())) {
                    userName = profile.nickname();
                } else if (profile != null) {
                    userName = profile.username();
                }
            }
            if (mIconView != null) {
                ChatUtils.getInstance().showProfileAvatar(profile, mIconView, ICON_CONFIG);
            }
        }
        if (mIconView != null) {
            mIconView.setOnClickListener(v -> {
                if (mMaxMessage.isReceiveMsg()) {
                    // 收到的消息进入roster详情
                    RosterDetailActivity.openRosterDetail(mContext, mMaxMessage.fromId());
                } else {
                    // 自己的进入设置页面
                    SettingUserActivity.openSettingUser(mContext);
                }
            });
        }
        if (mUsetText != null && group) {
            mUsetText.setText(TextUtils.isEmpty(userName) ? "" : userName);
        }
        if (!group) {
            mUsetText.setHeight(1);
        }
    }

    /**
     * 展示已读状态
     */
    private void showReadStatus() {
        if (mItemPos != ITEM_RIGHT || mTvReadStatus == null) {
            return;
        }
        if (mMaxMessage == null) {
            mTvReadStatus.setVisibility(View.GONE);
            return;
        }
        if (mMaxMessage.type() == BMXMessage.MessageType.Single) {
            // 单聊
            mTvReadStatus.setVisibility(View.VISIBLE);
            boolean isRead = mMaxMessage.isReadAcked();
            mTvReadStatus.setText(isRead ? getResources().getString(R.string.read) : getResources().getString(R.string.unread));
        } else if (mMaxMessage.type() == BMXMessage.MessageType.Group) {
            // 群聊
            mTvReadStatus.setVisibility(mShowReadAck ? View.VISIBLE : View.GONE);
            int readCount = mMaxMessage.groupAckCount();
            mTvReadStatus.setText(getResources().getString(R.string.read_persons) + (readCount > 0 ? readCount : 0));
        } else {
            mTvReadStatus.setVisibility(View.GONE);
        }
        mTvReadStatus.setOnClickListener((v) -> {
            if (!mShowReadAck || mActionListener == null) {
                return;
            }
            mActionListener.onGroupAck(mMaxMessage);
        });
    }

    /**
     * 展示发送消息的发送状态
     */
    private void showSendStatus() {
        if (mItemPos != ITEM_RIGHT || mSendFailImg == null || mSendingImg == null) {
            return;
        }
        BMXMessage.DeliveryStatus sendStatus = mMaxMessage == null ? null
                : mMaxMessage.deliveryStatus();
        // 消息发送状态是否失败
        if (sendStatus == null || sendStatus == BMXMessage.DeliveryStatus.Deliveried) {
            // 空和成功都展示成功
            mSendFailImg.setVisibility(View.GONE);
            mSendingImg.setVisibility(View.GONE);
        } else if (sendStatus == BMXMessage.DeliveryStatus.Failed) {
            // 失败
            mSendFailImg.setVisibility(View.VISIBLE);
            mSendingImg.setVisibility(View.GONE);
        } else {
            mSendFailImg.setVisibility(View.GONE);
            mSendingImg.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置聊天item的长按点击
     *
     * @param view 控件view
     */
    protected void setItemViewListener(View view) {
        // 长按
        view.setOnLongClickListener(new ItemLongClickListener());
        // 点击
        view.setOnClickListener(v -> {
            if (mActionListener != null) {
                mActionListener.onItemFunc(mMaxMessage);
            }
        });
    }

    class ItemLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            if (mActionListener != null) {
                mActionListener.onMessageLongClick(mMaxMessage);
            }
            return true;
        }
    }

    @Override
    public void onViewAttach() {
        if (mActionListener != null) {
            // 对方发送才发送已读回执
            mActionListener.onMessageReadAck(mMaxMessage);
        }
    }

    @Override
    public void onViewDetach() {

    }
}
