
package top.maxim.im.message.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonCustomDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ItemLine;
import top.maxim.im.common.view.ItemLineArrow;
import top.maxim.im.group.view.GroupApplyActivity;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description : 群聊设置 Created by Mango on 2018/11/25.
 */
public class ChatGroupSettingActivity extends BaseTitleActivity {

    private long mGroupId;

    private BMXGroup mGroup = new BMXGroup();

    /* 屏蔽群信息模式 */
    private ItemLineArrow.Builder mMuteGroupMessage;

    // /* 接收并显示群信息 */
    // private ItemLineSwitch.Builder mShowGroupMessage;
    //
    // /* 邀请确认 */
    // private ItemLineSwitch.Builder mInviteConfirm;

    /* 入群申请 */
    private ItemLineArrow.Builder mGroupApply;

    /* 群消息通知模式 */
    private ItemLineArrow.Builder mGroupNotifyMode;

    /* 入群审批模式 */
    private ItemLineArrow.Builder mGroupJoinAuthMode;

    /* 邀请入群模式 */
    private ItemLineArrow.Builder mGroupInviteMode;

    /* 被邀请入群模式 */
//    private ItemLineArrow.Builder mGroupBeInviteMode;

    /* 群主转移 */
    private ItemLineArrow.Builder mTransGroup;

    /* 群黑名单 */
    private ItemLineArrow.Builder mGroupBlock;

    /* 群禁言列表 */
    private ItemLineArrow.Builder mGroupBan;

    private SparseArray<BMXGroup.MsgMuteMode> muteModeMap = new SparseArray<>();

    private SparseArray<BMXGroup.MsgPushMode> pushModeMap = new SparseArray<>();

    private SparseArray<BMXGroup.JoinAuthMode> joinAuthModeMap = new SparseArray<>();

    private SparseArray<BMXGroup.InviteMode> inviteModeMap = new SparseArray<>();

    public static void startGroupSettingActivity(Context context, long groupId) {
        Intent intent = new Intent(context, ChatGroupSettingActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setHeaderBgColor(getResources().getColor(R.color.c2));
        builder.setTitle(R.string.group_setting);
        builder.setBackIcon(R.drawable.header_back_icon, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        // 屏蔽群信息
        mMuteGroupMessage = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_mute_msg))
                .setMarginTop(ScreenUtils.dp2px(10))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSetGroupMode(getString(R.string.group_mute_msg));
                    }
                });
        container.addView(mMuteGroupMessage.build());

        // 分割线
        ItemLine.Builder itemLine0 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine0.build());

        // // 接收并显示群信息
        // mShowGroupMessage = new ItemLineSwitch.Builder(this)
        // .setLeftText(getString(R.string.group_show_msg))
        // .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
        // @Override
        // public void onItemSwitch(View v, boolean curCheck) {
        //
        // }
        // });
        // container.addView(mShowGroupMessage.build());
        //
        // // 分割线
        // ItemLine.Builder itemLine1 = new ItemLine.Builder(this, container)
        // .setMarginLeft(ScreenUtils.dp2px(15));
        // container.addView(itemLine1.build());
        //
        // // 邀请确认
        // mInviteConfirm = new ItemLineSwitch.Builder(this)
        // .setLeftText(getString(R.string.group_invite_confirm))
        // .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
        // @Override
        // public void onItemSwitch(View v, boolean curCheck) {
        //
        // }
        // });
        // container.addView(mInviteConfirm.build());
        //
        // // 分割线
        // ItemLine.Builder itemLine2 = new ItemLine.Builder(this, container)
        // .setMarginLeft(ScreenUtils.dp2px(15));
        // container.addView(itemLine2.build());

        // 入群审核
        mGroupApply = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_apply))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        GroupApplyActivity.openGroupApply(ChatGroupSettingActivity.this, mGroupId);
                    }

                });
        container.addView(mGroupApply.build());

        // 分割线
        ItemLine.Builder itemLine3 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine3.build());

        // 群消息通知模式
        mGroupNotifyMode = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_notify_mode))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSetGroupMode(getString(R.string.group_notify_mode));
                    }

                });
        container.addView(mGroupNotifyMode.build());

        // 分割线
        ItemLine.Builder itemLine4 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine4.build());

        // 入群审批模式
        mGroupJoinAuthMode = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_join_auth_mode))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSetGroupMode(getString(R.string.group_join_auth_mode));
                    }

                });
        container.addView(mGroupJoinAuthMode.build());

        // 分割线
        ItemLine.Builder itemLine5 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine5.build());

        // 邀请入群模式
        mGroupInviteMode = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_invite_mode))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSetGroupMode(getString(R.string.group_invite_mode));
                    }

                });
        container.addView(mGroupInviteMode.build());

        // 分割线
        ItemLine.Builder itemLine6 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine6.build());

//        // 被邀请审批模式
//        mGroupBeInviteMode = new ItemLineArrow.Builder(this)
//                .setStartContent(getString(R.string.group_beInvite_mode))
//                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
//                    @Override
//                    public void onItemClick(View v) {
//                        showSetGroupMode(getString(R.string.group_beInvite_mode));
//                    }
//
//                });
//        container.addView(mGroupBeInviteMode.build());
//
//        // 分割线
//        ItemLine.Builder itemLine7 = new ItemLine.Builder(this, container)
//                .setMarginLeft(ScreenUtils.dp2px(15));
//        container.addView(itemLine7.build());

        // 群主转移
        mTransGroup = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_trans))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        ChatGroupTransActivity
                                .startGroupTransActivity(ChatGroupSettingActivity.this, mGroupId);
                    }

                });
        container.addView(mTransGroup.build());

        // 分割线
        ItemLine.Builder itemLine8 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine8.build());

        // 群黑名单
        mGroupBlock = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_black))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        ChatGroupBlockActivity
                                .startGroupBlockActivity(ChatGroupSettingActivity.this, mGroupId);

                    }

                });
        container.addView(mGroupBlock.build());

        // 分割线
        ItemLine.Builder itemLine9 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine9.build());

        // 群禁言
        mGroupBan = new ItemLineArrow.Builder(this).setStartContent(getString(R.string.group_ban))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        ChatGroupBannedActivity.startGroupBannedActivity(ChatGroupSettingActivity.this,
                                mGroupId);
                    }

                });
        container.addView(mGroupBan.build());

        container.setPadding(0, 0, 0, ScreenUtils.dp2px(50));
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(container, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return scrollView;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent == null) {
            return;
        }
        mGroupId = intent.getLongExtra(MessageConfig.CHAT_ID, 0);
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        initGroupMode();
        initGroupInfo();
    }

    private void initGroupInfo() {
        showLoadingDialog(true);
        Observable.just(mGroupId).map(new Func1<Long, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(Long aLong) {
                return GroupManager.getInstance().search(mGroupId, mGroup, true);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        toastError(e);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        bindGroupInfo();
                    }
                });
    }

    private void bindGroupInfo() {
        // 屏蔽群信息
        BMXGroup.MsgMuteMode muteMode = mGroup.msgMuteMode();
        for (int i = 0; i < muteModeMap.size(); i++) {
            if (muteModeMap.valueAt(i) == muteMode) {
                mMuteGroupMessage.setEndContent(getString(muteModeMap.keyAt(i)));
                break;
            }
        }
        // 群消息通知模式
        BMXGroup.MsgPushMode pushMode = mGroup.msgPushMode();
        for (int i = 0; i < pushModeMap.size(); i++) {
            if (pushModeMap.valueAt(i) == pushMode) {
                mGroupNotifyMode.setEndContent(getString(pushModeMap.keyAt(i)));
                break;
            }
        }
        // 入群审批模式
        BMXGroup.JoinAuthMode joinAuthMode = mGroup.joinAuthMode();
        for (int i = 0; i < joinAuthModeMap.size(); i++) {
            if (joinAuthModeMap.valueAt(i) == joinAuthMode) {
                mGroupJoinAuthMode.setEndContent(getString(joinAuthModeMap.keyAt(i)));
                break;
            }
        }
        // 邀请入群模式
        BMXGroup.InviteMode inviteMode = mGroup.inviteMode();
        for (int i = 0; i < inviteModeMap.size(); i++) {
            if (inviteModeMap.valueAt(i) == inviteMode) {
                mGroupInviteMode.setEndContent(getString(inviteModeMap.keyAt(i)));
                break;
            }
        }
//        // 被邀请入群模式
//        BMXGroup.BeInvitedMode beInvitedMode = mGroup.beInvitedMode();
//        for (int i = 0; i < beInvitedModeMap.size(); i++) {
//            if (beInvitedModeMap.valueAt(i) == beInvitedMode) {
//                mGroupBeInviteMode.setEndContent(getString(beInvitedModeMap.keyAt(i)));
//                break;
//            }
//        }
        // 黑名单人数
        int blocks = mGroup.blockListSize();
        mGroupBlock.setEndContent(blocks <= 0 ? "" : String.valueOf(blocks));
        // 禁言人数
        int banSize = mGroup.bannedListSize();
        mGroupBan.setEndContent(banSize <= 0 ? "" : String.valueOf(banSize));
    }

    private void initGroupMode() {
        muteModeMap.put(R.string.group_mute_none, BMXGroup.MsgMuteMode.None);
        muteModeMap.put(R.string.group_mute_notification, BMXGroup.MsgMuteMode.MuteNotification);
        muteModeMap.put(R.string.group_mute_chat, BMXGroup.MsgMuteMode.MuteChat);

        pushModeMap.put(R.string.group_notify_all, BMXGroup.MsgPushMode.All);
        pushModeMap.put(R.string.group_notify_admin_at, BMXGroup.MsgPushMode.AdminOrAt);
        pushModeMap.put(R.string.group_notify_none, BMXGroup.MsgPushMode.None);

        joinAuthModeMap.put(R.string.group_join_auth_open, BMXGroup.JoinAuthMode.Open);
        joinAuthModeMap.put(R.string.group_join_auth_approval, BMXGroup.JoinAuthMode.NeedApproval);
        joinAuthModeMap.put(R.string.group_join_auth_reject, BMXGroup.JoinAuthMode.RejectAll);

        inviteModeMap.put(R.string.group_invite_open, BMXGroup.InviteMode.Open);
        inviteModeMap.put(R.string.group_invite_admin, BMXGroup.InviteMode.AdminOnly);
    }

    /**
     * 设置群聊mode
     *
     * @param title 标题
     */
    private void showSetGroupMode(final String title) {
        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int[] array = null;
        if (TextUtils.equals(title, getString(R.string.group_notify_mode))) {
            // 群消息通知模式
            array = new int[] {
                    R.string.group_notify_all, R.string.group_notify_admin_at,
                    R.string.group_notify_none
            };
        } else if (TextUtils.equals(title, getString(R.string.group_join_auth_mode))) {
            // 入群审批模式
            array = new int[] {
                    R.string.group_join_auth_open, R.string.group_join_auth_approval,
                    R.string.group_join_auth_reject
            };
        } else if (TextUtils.equals(title, getString(R.string.group_invite_mode))) {
            // 邀请入群模式
            array = new int[] {
                    R.string.group_invite_open, R.string.group_invite_admin
            };
        } else if (TextUtils.equals(title, getString(R.string.group_beInvite_mode))) {
            // 被邀请模式
            array = new int[] {
                    R.string.group_beInvite_open, R.string.group_beInvite_approval
            };
        } else if (TextUtils.equals(title, getString(R.string.group_mute_msg))) {
            //屏蔽群消息模式
            array = new int[] {
                    R.string.group_mute_none, R.string.group_mute_notification, R.string.group_mute_chat
            };
        }
        if (array == null) {
            return;
        }
        final int[] selectContent = new int[1];
        for (final int s : array) {
            final TextView tv = new TextView(this);
            tv.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                    ScreenUtils.dp2px(15));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            tv.setTextColor(getResources().getColor(R.color.color_black));
            tv.setBackgroundColor(getResources().getColor(R.color.color_white));
            tv.setText(s);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectContent[0] = s;
                    for (int i = 0; i < ll.getChildCount(); i++) {
                        TextView select = (TextView)ll.getChildAt(i);
                        if (TextUtils.equals(select.getText().toString(), getString(s))) {
                            select.setTextColor(Color.RED);
                        } else {
                            select.setTextColor(getResources().getColor(R.color.color_black));
                        }
                    }
                }
            });
            ll.addView(tv, params);
        }
        DialogUtils.getInstance().showCustomDialog(this, ll, title, getString(R.string.confirm),
                getString(R.string.cancel), new CommonCustomDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener() {
                        setGroupInfo(title,
                                selectContent[0]);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 更新信息
     */
    private void setGroupInfo(final String title, final int stringResId) {
        if (stringResId <= 0) {
            return;
        }
        showLoadingDialog(true);
        Observable.just(title).map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                if (TextUtils.equals(title, getString(R.string.group_notify_mode))) {
                    // 群消息通知
                    BMXGroup.MsgPushMode pushMode = pushModeMap.get(stringResId);
                    return GroupManager.getInstance().setMsgPushMode(mGroup, pushMode);
                }
                if (TextUtils.equals(title, getString(R.string.group_join_auth_mode))) {
                    // 入群审批模式
                    BMXGroup.JoinAuthMode joinAuthMode = joinAuthModeMap.get(stringResId);
                    return GroupManager.getInstance().setJoinAuthMode(mGroup, joinAuthMode);
                }
                if (TextUtils.equals(title, getString(R.string.group_invite_mode))) {
                    // 设置邀请模式
                    BMXGroup.InviteMode inviteMode = inviteModeMap.get(stringResId);
                    return GroupManager.getInstance().setInviteMode(mGroup, inviteMode);
                }
                if (TextUtils.equals(title, getString(R.string.group_mute_msg))) {
                    // 屏蔽群消息
                    BMXGroup.MsgMuteMode muteMode = muteModeMap.get(stringResId);
                    return GroupManager.getInstance().muteMessage(mGroup, muteMode);
                }
                return null;
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        toastError(e);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        dismissLoadingDialog();
                        if (TextUtils.equals(title, getString(R.string.group_notify_mode))) {
                            // 群消息通知
                            mGroupNotifyMode.setEndContent(getString(stringResId));
                        } else if (TextUtils.equals(title,
                                getString(R.string.group_join_auth_mode))) {
                            // 入群审批模式
                            mGroupJoinAuthMode.setEndContent(getString(stringResId));
                        } else if (TextUtils.equals(title, getString(R.string.group_invite_mode))) {
                            // 设置邀请模式
                            mGroupInviteMode.setEndContent(getString(stringResId));
                        } else if (TextUtils.equals(title, getString(R.string.group_mute_msg))) {
                            // 屏蔽群消息
                            mMuteGroupMessage.setEndContent(getString(stringResId));
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void toastError(Throwable e) {
        String error = e != null ? e.getMessage() : "网络异常";
        ToastUtil.showTextViewPrompt(error);
    }
}
