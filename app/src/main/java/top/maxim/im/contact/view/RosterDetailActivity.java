
package top.maxim.im.contact.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import im.floo.BMXCallBack;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXRosterItem;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ItemLine;
import top.maxim.im.common.view.ItemLineArrow;
import top.maxim.im.common.view.ItemLineSwitch;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;
import top.maxim.im.message.view.ChatSingleActivity;

/**
 * Description : 详情 Created by Mango on 2018/11/21.
 */
public class RosterDetailActivity extends BaseTitleActivity {

    private ShapeImageView mUserIcon;

    private TextView mUserName;

    private TextView mUserId;

    private TextView mNickName;

    /* 公有信息 */
    private ItemLineArrow.Builder mShowPublic;

    /* 显示公共信息 */
    private TextView mTvPublic;

    /* 设置别名 */
    private ItemLineArrow.Builder mSetAlias;

    /* 设置扩展信息 */
    private ItemLineArrow.Builder mSetExt;

    /* 设置免打扰 */
    private ItemLineSwitch.Builder mSetDistrub;

    /* 显示扩展信息 */
    private TextView mTvExt;

    /* 开始聊天 */
    private TextView mTvOpenChat;

    private long mRosterId;

    private BMXRosterItem mRosterItem = new BMXRosterItem();

    private ImageRequestConfig mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
            .showImageForEmptyUri(R.drawable.default_avatar_icon)
            .showImageOnFail(R.drawable.default_avatar_icon).bitmapConfig(Bitmap.Config.RGB_565)
            .cacheOnDisk(true).showImageOnLoading(R.drawable.default_avatar_icon).build();

    public static void openRosterDetail(Context context, long rosterId) {
        Intent intent = new Intent(context, RosterDetailActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, rosterId);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.profile);
        builder.setRightText("", v -> showAddReason(mRosterId));
        builder.setBackIcon(R.drawable.header_back_icon, v -> finish());
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.activity_roster_detail, null);
        LinearLayout container = view.findViewById(R.id.ll_setting_container);
        mUserIcon = view.findViewById(R.id.iv_user_avatar);
        mUserName = view.findViewById(R.id.tv_user_name);
        mNickName = view.findViewById(R.id.tv_nick_name);
        mUserId = view.findViewById(R.id.tv_user_id);
        // 公开扩展信息
        mShowPublic = new ItemLineArrow.Builder(this).setMarginTop(ScreenUtils.dp2px(10))
                .setStartContent(getString(R.string.profile_public));
        container.addView(mShowPublic.build());

        // 分割线
        ItemLine.Builder itemLine0 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine0.build());

        mTvPublic = new TextView(this);
        LinearLayout.LayoutParams publicP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mTvPublic.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        mTvPublic.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        mTvPublic.setTextColor(getResources().getColor(R.color.color_black));
        mTvPublic.setBackgroundColor(getResources().getColor(R.color.color_white));
        mTvPublic.setLayoutParams(publicP);
        mTvPublic.setVisibility(View.GONE);
        container.addView(mTvPublic);

        // 分割线
        ItemLine.Builder itemLine1 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine1.build());

        // 设置别名
        mSetAlias = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.setting_roster_alias))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSetRosterDialog(getString(R.string.setting_roster_alias));
                    }
                });
        container.addView(mSetAlias.build());

        // 分割线
        ItemLine.Builder itemLine2 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine2.build());

        // 设置扩展信息
        mSetExt = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.setting_roster_ext))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSetRosterDialog(getString(R.string.setting_roster_ext));
                    }
                });
        container.addView(mSetExt.build());

        mTvExt = new TextView(this);
        mTvExt.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        mTvExt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        mTvExt.setTextColor(getResources().getColor(R.color.color_black));
        mTvExt.setBackgroundColor(getResources().getColor(R.color.color_white));
        mTvExt.setLayoutParams(publicP);
        mTvExt.setVisibility(View.GONE);
        container.addView(mTvExt);

        // 分割线
        ItemLine.Builder itemLine3 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine3.build());

        // 设置免打扰
        mSetDistrub = new ItemLineSwitch.Builder(this)
                .setLeftText(getString(R.string.setting_user_disturb))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {
                        setMuteEnable(curCheck);
                    }
                });
        container.addView(mSetDistrub.build());

        // 开始聊天
        mTvOpenChat = new TextView(this);
        mTvOpenChat.setTextColor(getResources().getColor(R.color.color_black));
        mTvOpenChat.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        mTvOpenChat.setGravity(Gravity.CENTER);
        mTvOpenChat.setText(getString(R.string.begin_chat));
        mTvOpenChat.setBackgroundResource(R.drawable.common_yellow_btn_corner_bg);
        mTvOpenChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatSingleActivity.startChatActivity(RosterDetailActivity.this,
                        BMXMessage.MessageType.Single, mRosterId);
                finish();
            }
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dp2px(44));
        params.topMargin = ScreenUtils.dp2px(30);
        params.bottomMargin = ScreenUtils.dp2px(5);
        params.leftMargin = ScreenUtils.dp2px(15);
        params.rightMargin = ScreenUtils.dp2px(15);
        mTvOpenChat.setLayoutParams(params);
        container.addView(mTvOpenChat);
        return view;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mRosterId = intent.getLongExtra(MessageConfig.CHAT_ID, 0);
        }
    }

    @Override
    protected void setViewListener() {
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        initRoster();
    }

    private void initRoster() {
        showLoadingDialog(true);
        RosterManager.getInstance().getRosterList(mRosterId, true, (bmxErrorCode, bmxRosterItem) -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                mRosterItem = bmxRosterItem;
                RosterFetcher.getFetcher().putRoster(bmxRosterItem);
                bindRoster();
            } else {
                RosterManager.getInstance().getRosterList(mRosterId, false,
                        (bmxErrorCode1, bmxRosterItem1) -> {
                            if (bmxRosterItem1 != null) {
                                mRosterItem = bmxRosterItem1;
                            }
                            bindRoster();
                        });
            }
        });
    }

    private void bindRoster() {
        // 是否是好友
        BMXRosterItem.RosterRelation rosterRelation = mRosterItem.relation();
        boolean friend = rosterRelation == BMXRosterItem.RosterRelation.Friend;
        TextView add = mHeader.getRightText();
        if (friend) {
            mTvOpenChat.setVisibility(View.VISIBLE);
            add.setVisibility(View.GONE);
        } else {
            mTvOpenChat.setVisibility(View.GONE);
            add.setVisibility(View.VISIBLE);
            add.setText("添加");
        }
//        if (friend) {
//            // 好友直接跳转单聊
//            ChatSingleActivity.startChatActivity(this, BMXMessage.MessageType.Single, mRosterId);
//            finish();
//            return;
//        }
        String name = mRosterItem.username();
        String nickName = mRosterItem.nickname();
        ChatUtils.getInstance().showRosterAvatar(mRosterItem, mUserIcon, mConfig);
        if (!friend) {
            // 非好友 需要强制更新头像
            ChatUtils.getInstance().downloadUserAvatar(mRosterItem, mUserIcon, mConfig);
        }
        long userId = mRosterItem.rosterId();
        mUserName.setText(TextUtils.isEmpty(name) ? "" : name);
        mNickName.setText(TextUtils.isEmpty(nickName) ? "" : "昵称:" + nickName);
        mUserId.setText(userId <= 0 ? "" : "ID:" + userId);
        String publicInfo = mRosterItem.publicInfo();
        if (TextUtils.isEmpty(publicInfo)) {
            mTvPublic.setVisibility(View.GONE);
        } else {
            mTvPublic.setVisibility(View.VISIBLE);
            mTvPublic.setText(publicInfo);
        }

        String alias = mRosterItem.alias();
        mSetAlias.setEndContent(TextUtils.isEmpty(alias) ? "" : alias);

        String ext = mRosterItem.ext();
        if (TextUtils.isEmpty(publicInfo)) {
            mTvExt.setVisibility(View.GONE);
        } else {
            mTvExt.setVisibility(View.VISIBLE);
            mTvExt.setText(ext);
        }
        // 免打扰
        boolean isBlock = mRosterItem.isMuteNotification();
        mSetDistrub.setCheckStatus(isBlock);

    }

    /**
     * 设置弹出框
     */
    private void showSetRosterDialog(final String title) {
        DialogUtils.getInstance().showEditDialog(this, title, getString(R.string.confirm),
                getString(R.string.cancel), new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        setRosterInfo(TextUtils.isEmpty(content) ? "" : content, title);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 设置roster信息
     *
     * @param info
     */
    private void setRosterInfo(final String info, final String title) {
        showLoadingDialog(true);
        BMXCallBack callBack = bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                if (TextUtils.equals(title, getString(R.string.setting_roster_alias))) {
                    mSetAlias.setEndContent(info);
                } else if (TextUtils.equals(title, getString(R.string.setting_roster_ext))) {
                    mTvExt.setVisibility(TextUtils.isEmpty(info) ? View.GONE : View.VISIBLE);
                    mTvExt.setText(TextUtils.isEmpty(info) ? "" : info);
                }
                return;
            }
            String error = bmxErrorCode == null ? "" : bmxErrorCode.name();
            ToastUtil.showTextViewPrompt(error);

        };
        if (TextUtils.equals(title, getString(R.string.setting_roster_alias))) {
            RosterManager.getInstance().setItemAlias(mRosterItem, info, callBack);
        } else if (TextUtils.equals(title, getString(R.string.setting_roster_ext))) {
            RosterManager.getInstance().setItemExtension(mRosterItem, info, callBack);
        }
    }

    /**
     * 设置免打扰
     *
     * @param enable
     */
    private void setMuteEnable(final boolean enable) {
        showLoadingDialog(true);
        RosterManager.getInstance().setItemMuteNotification(mRosterItem, enable, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                RosterFetcher.getFetcher().putRoster(mRosterItem);
            } else {
                String error = bmxErrorCode == null ? "" : bmxErrorCode.name();
                ToastUtil.showTextViewPrompt(error);
                mSetDistrub.setCheckStatus(!enable);
            }
        });
    }

    /**
     * 输入框弹出
     */
    private void showAddReason(final long rosterId) {
        DialogUtils.getInstance().showEditDialog(this, "添加好友", getString(R.string.confirm),
                getString(R.string.cancel), new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        addRoster(rosterId, content);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    private void addRoster(long rosterId, final String reason) {
        if (rosterId <= 0) {
            return;
        }
        showLoadingDialog(true);
        RosterManager.getInstance().apply(rosterId, reason, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                ToastUtil.showTextViewPrompt("添加成功");
                initRoster();
            } else {
                ToastUtil.showTextViewPrompt("添加失败");
            }
        });
    }

}
