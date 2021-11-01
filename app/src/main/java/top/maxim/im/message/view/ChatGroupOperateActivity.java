
package top.maxim.im.message.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import im.floo.BMXCallBack;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.FileProgressListener;
import im.floo.floolib.ListOfLongLong;
import top.maxim.im.MainActivity;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.ChatManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.provider.CommonProvider;
import top.maxim.im.common.utils.CameraUtils;
import top.maxim.im.common.utils.FileUtils;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonCustomDialog;
import top.maxim.im.common.utils.dialog.CommonDialog;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.utils.permissions.PermissionsConstant;
import top.maxim.im.common.view.BMImageLoader;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ItemLine;
import top.maxim.im.common.view.ItemLineArrow;
import top.maxim.im.common.view.ItemLineSwitch;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseRecyclerAdapter;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.FullyGridLayoutManager;
import top.maxim.im.contact.view.RosterChooseActivity;
import top.maxim.im.contact.view.RosterDetailActivity;
import top.maxim.im.group.view.GroupQrCodeActivity;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description : 群聊设置 Created by Mango on 2018/11/25.
 */
public class ChatGroupOperateActivity extends BaseTitleActivity {

    private long mGroupId;

    private BMXGroup mGroup = new BMXGroup();

    /* 群成员 */
    private TextView mChatGroupMember;

    /* 群类型 */
    private ItemLineArrow.Builder mChatGroupType;

    /* 群id */
    private ItemLineArrow.Builder mChatGroupId;

    /* 群主id */
    private ItemLineArrow.Builder mChatGroupOwnerId;

    /* 群二维码 */
    private ItemLineArrow.Builder mChatGroupQrcode;

    /* 群聊管理 */
    private ItemLineArrow.Builder mChatGroupManager;

    private View mViewChatGroupManager;

    /* 修改名称 */
    private ItemLineArrow.Builder mChatGroupRename;

    private View mViewChatGroupRename;

    /* 群头像 */
    private ItemLineArrow.Builder mGroupAvatar;

    private View mViewGroupAvatar;

    /* 我在群里的昵称 */
    private ItemLineArrow.Builder mGroupMyNickName;

    private View mViewGroupMyNickName;

    /* 管理员列表 */
    private ItemLineArrow.Builder mChatGroupManagerList;

    private View mViewChatGroupManagerList;

    /* 群描述 */
    private ItemLineArrow.Builder mChatGroupDesc;

    private View mViewChatGroupDesc;

    private TextView mViewGroupDesc;

    /* 群公告 */
    private ItemLineArrow.Builder mChatGroupNotice;

    private View mViewChatGroupNotice;

    private TextView mViewGroupNotice;

    /* 群扩展 */
    private ItemLineArrow.Builder mChatGroupExt;

    private View mViewChatGroupExt;

    private TextView mViewGroupExt;

    /* 群共享 */
    private ItemLineArrow.Builder mChatGroupShare;

    private View mViewChatGroupShare;

    /* 设置群已读 */
    private ItemLineSwitch.Builder mChatGroupReadMode;

    /* 屏蔽群信息模式 */
    private ItemLineArrow.Builder mMuteGroupMessage;

    /* 群消息通知模式 */
    private ItemLineArrow.Builder mGroupNotifyMode;

    private View mChatGroupReadModeView;

    /* 群聊成员gridView */
    private RecyclerView mGvGroupMember;

    /* 群聊成员adapter */
    private ChatGroupMemberAdapter mAdapter;

    /* 退出群聊 */
    private TextView mQuitGroup;

    private View itemLine_1, itemLine_2, itemLine0, itemLine1, itemLine2, itemLine3, itemLine4,
            itemLine5, itemLine6, itemLine7, itemLine8, itemLine9, itemLine10, itemLine11,
            itemLine12;

    /* 相册 */
    private final int IMAGE_REQUEST = 1000;

    /* 加人 */
    private final int ADD_MEMBER_REQUEST = 1001;

    /* 减人 */
    private final int REMOVE_MEMBER_REQUEST = 1002;

    /* 是否是群聊管理员 */
    private boolean mIsAdmin;
    /* 是否是群聊创建者 */
    private boolean mIsOwner;

    private List<String> memberIdList;
    
    // 最大展示数目
    private final int MAX_SHOW_MEMBER = 10;

    private SparseArray<BMXGroup.MsgPushMode> pushModeMap = new SparseArray<>();

    private SparseArray<BMXGroup.MsgMuteMode> muteModeMap = new SparseArray<>();

    public static void startGroupOperateActivity(Context context, long groupId) {
        Intent intent = new Intent(context, ChatGroupOperateActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle("");
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
        View view = View.inflate(this, R.layout.chat_group_operate_view, null);
        initView(view);
        return view;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mGroupId = intent.getLongExtra(MessageConfig.CHAT_ID, 0);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        initGroupMode();
        initGroupInfo(true);
    }

    private void initGroupMode() {
        muteModeMap.put(R.string.group_mute_none, BMXGroup.MsgMuteMode.None);
        muteModeMap.put(R.string.group_mute_notification, BMXGroup.MsgMuteMode.MuteNotification);
        muteModeMap.put(R.string.group_mute_chat, BMXGroup.MsgMuteMode.MuteChat);

        pushModeMap.put(R.string.group_notify_all, BMXGroup.MsgPushMode.All);
        pushModeMap.put(R.string.group_notify_admin_at, BMXGroup.MsgPushMode.AdminOrAt);
        pushModeMap.put(R.string.group_notify_none, BMXGroup.MsgPushMode.None);
    }

    private void initGroupInfo(final boolean syncMember) {
        showLoadingDialog(true);
        GroupManager.getInstance().getGroupList(mGroupId, false, (bmxErrorCode, bmxGroup) -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                if (bmxGroup != null) {
                    mGroup = bmxGroup;
                }
                bindGroupInfo();
                if (syncMember) {
                    initGroupMembers();
                }
            } else {
                toastError(bmxErrorCode);
            }
        });
    }

    /**
     * 初始化View
     *
     * @param view 设置页布局view
     */
    public void initView(View view) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout container = view.findViewById(R.id.group_operate_container);

        // 群成员
        mChatGroupMember = view.findViewById(R.id.chat_operate_more_member);
        mChatGroupMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatGroupMemberActivity.startGroupMemberActivity(ChatGroupOperateActivity.this,
                        mGroupId);
            }
        });

        /* 群id */
        mChatGroupId = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_id));
        container.addView(mChatGroupId.build());

        // 分割线
        itemLine_1 = new ItemLine.Builder(this, container).setMarginLeft(ScreenUtils.dp2px(15))
                .build();
        container.addView(itemLine_1);

        /* 群主id */
        mChatGroupOwnerId = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_owner_id));
        container.addView(mChatGroupOwnerId.build());

        // 分割线
        itemLine0 = new ItemLine.Builder(this, container).setMarginLeft(ScreenUtils.dp2px(15))
                .build();
        container.addView(itemLine0);

        /* 群二维码 */
        mChatGroupQrcode = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_qrcode))
                .setOnItemClickListener(v -> GroupQrCodeActivity.openGroupQrcode(this, mGroupId));
        container.addView(mChatGroupQrcode.build());

        // 分割线
        itemLine_2 = new ItemLine.Builder(this, container).setMarginLeft(ScreenUtils.dp2px(15))
                .build();
        container.addView(itemLine_2);

        /* 群管理 */
        mChatGroupManager = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_manager))
                .setOnItemClickListener(v -> ChatGroupSettingActivity
                        .startGroupSettingActivity(ChatGroupOperateActivity.this, mGroupId));
        container.addView(mViewChatGroupManager = mChatGroupManager.build());

        // 分割线
        itemLine1 = new ItemLine.Builder(this, container).setMarginLeft(ScreenUtils.dp2px(15))
                .build();
        container.addView(itemLine1);

        /* 修改群名称 */
        mChatGroupRename = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_rename))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSettingDialog(getString(R.string.group_rename));
                    }
                });
        container.addView(mViewChatGroupRename = mChatGroupRename.build());

        /* 群头像 */
        mGroupAvatar = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_avatar))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        // 选择相册 需要SD卡读写权限
                        if (hasPermission(PermissionsConstant.READ_STORAGE,
                                PermissionsConstant.WRITE_STORAGE)) {
                            CameraUtils.getInstance().takeGalley(ChatGroupOperateActivity.this,
                                    IMAGE_REQUEST);
                        } else {
                            requestPermissions(PermissionsConstant.READ_STORAGE,
                                    PermissionsConstant.WRITE_STORAGE);
                        }
                    }
                });
        container.addView(mViewGroupAvatar = mGroupAvatar.build());

        // 分割线
        itemLine2 = new ItemLine.Builder(this, container).setMarginLeft(ScreenUtils.dp2px(15))
                .build();
        container.addView(itemLine2);

        /* 我在群里的昵称 */
        mGroupMyNickName = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_my_name))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSettingDialog(getString(R.string.group_my_name));
                    }
                });
        container.addView(mViewGroupMyNickName = mGroupMyNickName.build());

        // 分割线
        itemLine3 = new ItemLine.Builder(this, container).setMarginLeft(ScreenUtils.dp2px(15))
                .build();
        container.addView(itemLine3);

        /* 管理员列表 */
        mChatGroupManagerList = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_manager_list))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        ChatGroupAdminActivity
                                .startGroupAdminActivity(ChatGroupOperateActivity.this, mGroupId);
                    }
                });
        container.addView(mViewChatGroupManagerList = mChatGroupManagerList.build());
        // 分割线
        itemLine4 = new ItemLine.Builder(this, container).setMarginLeft(ScreenUtils.dp2px(15))
                .build();
        container.addView(itemLine4);

        /* 群描述 */
        mChatGroupDesc = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_desc))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        if (!mIsAdmin) {
                            return;
                        }
                        showSettingDialog(getString(R.string.group_desc));
                    }
                });
        container.addView(mViewChatGroupDesc = mChatGroupDesc.build());

        // 分割线
        itemLine5 = new ItemLine.Builder(this, container).setMarginLeft(ScreenUtils.dp2px(15))
                .build();
        container.addView(itemLine5);

        mViewGroupDesc = new TextView(this);
        mViewGroupDesc.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15), ScreenUtils.dp2px(15));
        mViewGroupDesc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        mViewGroupDesc.setTextColor(getResources().getColor(R.color.color_black));
        mViewGroupDesc.setBackgroundColor(getResources().getColor(R.color.color_white));
        mViewGroupDesc.setLayoutParams(params);
        mViewGroupDesc.setVisibility(View.GONE);
        container.addView(mViewGroupDesc);

        // 分割线
        itemLine6 = new ItemLine.Builder(this, container).setMarginLeft(ScreenUtils.dp2px(15))
                .build();
        container.addView(itemLine6);

        /* 群公告 */
        mChatGroupNotice = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_notice))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        if (!mIsAdmin) {
                            return;
                        }
                        ChatGroupAnnomentActivity.startGroupAnnoucementActivity(
                                ChatGroupOperateActivity.this, mGroupId);
                    }
                });
        container.addView(mViewChatGroupNotice = mChatGroupNotice.build());
        // 分割线
        itemLine7 = new ItemLine.Builder(this, container).setMarginLeft(ScreenUtils.dp2px(15))
                .build();
        container.addView(itemLine7);

        mViewGroupNotice = new TextView(this);
        mViewGroupNotice.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15), ScreenUtils.dp2px(15));
        mViewGroupNotice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        mViewGroupNotice.setTextColor(getResources().getColor(R.color.color_black));
        mViewGroupNotice.setBackgroundColor(getResources().getColor(R.color.color_white));
        mViewGroupNotice.setLayoutParams(params);
        mViewGroupNotice.setVisibility(View.GONE);
        container.addView(mViewGroupNotice);

        // 分割线
        itemLine8 = new ItemLine.Builder(this, container).setMarginLeft(ScreenUtils.dp2px(15))
                .build();
        container.addView(itemLine8);

        /* 群扩展信息 */
        mChatGroupExt = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_ext))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        if (!mIsAdmin) {
                            return;
                        }
                        showSettingDialog(getString(R.string.group_ext));
                    }
                });
        container.addView(mViewChatGroupExt = mChatGroupExt.build());

        // 分割线
        itemLine9 = new ItemLine.Builder(this, container).setMarginLeft(ScreenUtils.dp2px(15))
                .build();
        container.addView(itemLine9);

        mViewGroupExt = new TextView(this);
        mViewGroupExt.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15), ScreenUtils.dp2px(15));
        mViewGroupExt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        mViewGroupExt.setTextColor(getResources().getColor(R.color.color_black));
        mViewGroupExt.setBackgroundColor(getResources().getColor(R.color.color_white));
        mViewGroupExt.setLayoutParams(params);
        mViewGroupExt.setVisibility(View.GONE);
        container.addView(mViewGroupExt);

        // 分割线
        itemLine10 = new ItemLine.Builder(this, container).setMarginLeft(ScreenUtils.dp2px(15))
                .build();
        container.addView(itemLine10);

        /* 群共享 */
        mChatGroupShare = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_share))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        ChatGroupShareActivity
                                .startGroupShareActivity(ChatGroupOperateActivity.this, mGroupId);
                    }
                });
        container.addView(mViewChatGroupShare = mChatGroupShare.build());
        // 分割线
        itemLine11 = new ItemLine.Builder(this, container).setMarginLeft(ScreenUtils.dp2px(15)).build();
        container.addView(itemLine11);

        /* 群已读 */
        mChatGroupReadMode = new ItemLineSwitch.Builder(this)
                .setLeftText(getString(R.string.group_read_mode))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {
                        setGroupReadAck(curCheck);
                    }
                });
        container.addView(mChatGroupReadModeView = mChatGroupReadMode.build());
        // 分割线
        itemLine12 = new ItemLine.Builder(this, container).setMarginLeft(ScreenUtils.dp2px(15)).build();
        container.addView(itemLine12);

        //群类型
        mChatGroupType = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_type))
                .setArrowVisible(false);
        container.addView(mChatGroupType.build());

        // 分割线
        View itemLine13 = new ItemLine.Builder(this, container).setMarginLeft(ScreenUtils.dp2px(15)).build();
        container.addView(itemLine13);

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
        ItemLine.Builder itemLine14 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine14.build());

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
        View itemLine15 = new ItemLine.Builder(this, container).setMarginLeft(ScreenUtils.dp2px(15)).build();
        container.addView(itemLine15);

        // 退出群聊
        mQuitGroup = new TextView(this);
        mQuitGroup.setTextColor(getResources().getColor(R.color.color_FF475A));
        mQuitGroup.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        mQuitGroup.setGravity(Gravity.CENTER);
        mQuitGroup.setBackgroundResource(R.color.color_white);
        mQuitGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtils.getInstance().showDialog(ChatGroupOperateActivity.this,
                        getString(R.string.group_quit), getString(R.string.group_quit_notice),
                        new CommonDialog.OnDialogListener() {
                            @Override
                            public void onConfirmListener() {
                                quitGroup();
                            }

                            @Override
                            public void onCancelListener() {

                            }
                        });
            }
        });
        LinearLayout.LayoutParams quitP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dp2px(54));
        mQuitGroup.setLayoutParams(quitP);
        container.addView(mQuitGroup);

        mGvGroupMember = view.findViewById(R.id.gv_chat_group_member);
        mGvGroupMember.setLayoutManager(new FullyGridLayoutManager(this, 5));
        mGvGroupMember.setAdapter(mAdapter = new ChatGroupMemberAdapter(this));
    }

    @Override
    protected void setViewListener() {
        super.setViewListener();
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BMXGroup.Member member = mAdapter.getItem(position);
                if (member == null) {
                    return;
                }
                long memberId = member.getMUid();
                if (memberId == MessageConfig.MEMBER_ADD) {
                    // 添加
                    RosterChooseActivity.startRosterListActivity(ChatGroupOperateActivity.this,
                            true, true, memberIdList, ADD_MEMBER_REQUEST);
                } else if (memberId == MessageConfig.MEMBER_REMOVE) {
                    // 移除
                    ChatGroupListMemberActivity.startGroupMemberListActivity(
                            ChatGroupOperateActivity.this, mGroupId, true, REMOVE_MEMBER_REQUEST);
                } else {
                    RosterDetailActivity.openRosterDetail(ChatGroupOperateActivity.this, memberId);
                }
            }
        });
    }

    private void bindGroupInfo() {
        mIsOwner = GroupManager.getInstance().isGroupOwner(mGroup.ownerId());
        mIsAdmin = mGroup.roleType() == BMXGroup.MemberRoleType.GroupAdmin || mIsOwner;
        //创建者才能解散  退出群聊文案
        mQuitGroup.setText(getString(mIsOwner ? R.string.group_destroy : R.string.group_quit));

        long groupId = mGroup.groupId();
        mChatGroupId.setEndContent(groupId > 0 ? String.valueOf(groupId) : "");

        long ownerId = mGroup.ownerId();
        mChatGroupOwnerId.setEndContent(ownerId > 0 ? String.valueOf(ownerId) : "");
        mChatGroupOwnerId.setOnItemClickListener(v -> {
            if (ownerId > 0) {
                RosterDetailActivity.openRosterDetail(ChatGroupOperateActivity.this, ownerId);
            }
        });

        String name = mGroup.name();
        mChatGroupRename.setEndContent(!TextUtils.isEmpty(name) ? name : "");

        String myNick = mGroup.myNickname();
        mGroupMyNickName.setEndContent(!TextUtils.isEmpty(myNick) ? myNick : "");

        // 群描述
        String desc = mGroup.description();
        if (!TextUtils.isEmpty(desc)) {
            mViewGroupDesc.setVisibility(View.VISIBLE);
            mViewGroupDesc.setText(desc);
        } else {
            mViewGroupDesc.setVisibility(View.GONE);
        }

        // 群公告 TODO
        // String notice = mGroup.announcement();
        // if (!TextUtils.isEmpty(notice)) {
        // mViewGroupNotice.setVisibility(View.VISIBLE);
        // mViewGroupNotice.setText(notice);
        // } else {
        // mViewGroupNotice.setVisibility(View.GONE);
        // }

        // 群扩展信息
        String ext = mGroup.extension();
        if (!TextUtils.isEmpty(ext)) {
            mViewGroupExt.setVisibility(View.VISIBLE);
            mViewGroupExt.setText(ext);
        } else {
            mViewGroupExt.setVisibility(View.GONE);
        }

        // 群人数
        int members = mGroup.membersCount();
        mHeader.setTitle(String.format(getString(R.string.group_info), String.valueOf(members)));
        // 群管理员人数
        int admins = mGroup.adminsCount();
        mChatGroupManagerList.setEndContent(admins <= 0 ? "" : String.valueOf(admins));
        // 群共享列表
        int shares = mGroup.sharedFilesCount();
        mChatGroupShare.setEndContent(shares <= 0 ? "" : String.valueOf(shares));

        // 群已读
        boolean readAck = mGroup.enableReadAck();
        mChatGroupReadMode.setCheckStatus(readAck);

        //群类型
        String groupType = "";
        switch (mGroup.groupType()){
            case Public:
                groupType = getString(R.string.group_type_public);
                break;
            case Private:
                groupType = getString(R.string.group_type_private);
                break;
            case Chatroom:
                groupType = getString(R.string.group_type_chat_room);
                break;
        }
        mChatGroupType.setEndContent(groupType);
        setManagerVisible();
        // 群消息通知模式
        BMXGroup.MsgPushMode pushMode = mGroup.msgPushMode();
        for (int i = 0; i < pushModeMap.size(); i++) {
            if (pushModeMap.valueAt(i) == pushMode) {
                mGroupNotifyMode.setEndContent(getString(pushModeMap.keyAt(i)));
                break;
            }
        }
        // 屏蔽群信息
        BMXGroup.MsgMuteMode muteMode = mGroup.msgMuteMode();
        for (int i = 0; i < muteModeMap.size(); i++) {
            if (muteModeMap.valueAt(i) == muteMode) {
                mMuteGroupMessage.setEndContent(getString(muteModeMap.keyAt(i)));
                break;
            }
        }
    }

    /**
     * 设置群主和群成员的页面view显隐
     */
    private void setManagerVisible() {
        mViewChatGroupManager.setVisibility(mIsAdmin ? View.VISIBLE : View.GONE);
        mViewChatGroupRename.setVisibility(mIsAdmin ? View.VISIBLE : View.GONE);
        mViewGroupAvatar.setVisibility(mIsAdmin ? View.VISIBLE : View.GONE);
        // mViewGroupMyNickName.setVisibility(mIsAdmin ? View.VISIBLE : View.GONE);
        mViewChatGroupManagerList.setVisibility(mIsAdmin ? View.VISIBLE : View.GONE);

//        mViewChatGroupDesc.setVisibility(mIsAdmin ? View.VISIBLE : View.GONE);
//        mViewGroupDesc.setVisibility(mIsAdmin ? mViewGroupDesc.getVisibility() : View.GONE);

//        mViewChatGroupNotice.setVisibility(mIsAdmin ? View.VISIBLE : View.GONE);
//        mViewGroupNotice.setVisibility(mIsAdmin ? mViewGroupNotice.getVisibility() : View.GONE);

//        mViewChatGroupExt.setVisibility(mIsAdmin ? View.VISIBLE : View.GONE);
//        mViewGroupExt.setVisibility(mIsAdmin ? mViewGroupExt.getVisibility() : View.GONE);

//        mViewChatGroupShare.setVisibility(mIsAdmin ? View.VISIBLE : View.GONE);
        mChatGroupReadModeView.setVisibility(mIsAdmin ? View.VISIBLE : View.GONE);

        View[] views = new View[] {
                itemLine1, itemLine2, itemLine4, itemLine5, itemLine6, itemLine7, itemLine8,
                itemLine9, itemLine10, itemLine11, itemLine12
        };
        for (View view : views) {
            view.setVisibility(mIsAdmin ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 初始化群成员
     */
    private void initGroupMembers() {
        GroupManager.getInstance().getMembers(mGroup, true, (bmxErrorCode, memberList) -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                if (!memberList.isEmpty()) {
                    ListOfLongLong listOfLongLong = new ListOfLongLong();
                    long size = memberList.size();
                    int showSize = mIsAdmin ? MAX_SHOW_MEMBER - 2 : MAX_SHOW_MEMBER;
                    for (int i = 0; i < size; i++) {
                        if (i < showSize) {
                            listOfLongLong.add(memberList.get(i).getMUid());
                        }
                    }
                    RosterManager.getInstance().getRosterList(listOfLongLong, true,
                            (bmxErrorCode1, itemList) -> {
                                RosterFetcher.getFetcher().putRosters(itemList);
                                if (BaseManager.bmxFinish(bmxErrorCode1)) {
                                    List<BMXGroup.Member> members = new ArrayList<>();
                                    if (memberIdList == null) {
                                        memberIdList = new ArrayList<>();
                                    }
                                    memberIdList.clear();
                                    for (int i = 0; i < size; i++) {
                                        BMXGroup.Member member = memberList.get(i);
                                        if (member != null) {
                                            if (i < showSize) {
                                                members.add(member);
                                            }
                                            memberIdList.add(String.valueOf(member.getMUid()));
                                        }
                                    }
                                    // 群人数
                                    long memberCount = memberList.size();
                                    mHeader.setTitle(String.format(getString(R.string.group_info),
                                            String.valueOf(memberCount)));
                                    // 群主才有添加 删除功能
                                    if (mIsAdmin) {
                                        // 增加添加 移除
                                        BMXGroup.Member add = new BMXGroup.Member(
                                                MessageConfig.MEMBER_ADD, "", 0);
                                        BMXGroup.Member remove = new BMXGroup.Member(
                                                MessageConfig.MEMBER_REMOVE, "", 0);
                                        members.add(add);
                                        members.add(remove);
                                    }
                                    mAdapter.replaceList(members);
                                } else {
                                    toastError(bmxErrorCode1);
                                }
                            });
                }
            } else {
                toastError(bmxErrorCode);
            }
        });
    }

    /**
     * 输入框弹出
     */
    private void showSettingDialog(final String title) {
        DialogUtils.getInstance().showEditDialog(this, title, getString(R.string.confirm),
                getString(R.string.cancel), new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        setGroupInfo(title, content);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 输入框弹出
     */
    private void showOperate(final String title, final ListOfLongLong member) {
        // 移除 添加群成员 暂时不加输入框 reason直接传入""
        if (TextUtils.equals(title, getString(R.string.group_add_members))) {
            addMembers(member, "");
        } else if (TextUtils.equals(title, getString(R.string.group_remove_members))) {
            removeMembers(member, "");
        }
        // DialogUtils.getInstance().showEditDialog(this, title,
        // getString(R.string.confirm),
        // getString(R.string.cancel), new CommonEditDialog.OnDialogListener() {
        // @Override
        // public void onConfirmListener(String content) {
        // if (TextUtils.isEmpty(content)) {
        // return;
        // }
        // if (TextUtils.equals(title, getString(R.string.group_add_members))) {
        // addMembers(member, content);
        // } else if (TextUtils.equals(title,
        // getString(R.string.group_remove_members))) {
        // removeMembers(member, content);
        // }
        // }
        //
        // @Override
        // public void onCancelListener() {
        //
        // }
        // });
    }

    /**
     * 更新信息
     */
    private void setGroupInfo(final String title, final String info) {
        if (TextUtils.equals(title, getString(R.string.group_rename))) {
            if (TextUtils.isEmpty(info)) {
                return;
            }
        }
        showLoadingDialog(true);
        BMXCallBack callBack = bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                if (TextUtils.equals(title, getString(R.string.group_rename))) {
                    // 修改群名称
                    mChatGroupRename.setEndContent(info);
                } else if (TextUtils.equals(title, getString(R.string.group_my_name))) {
                    // 我在群里的昵称
                    mGroupMyNickName.setEndContent(info);
                    //设置完我在本群的昵称 刷新member名称
                    initGroupMembers();
                } else if (TextUtils.equals(title, getString(R.string.group_desc))) {
                    // 设置群描述
                    mViewGroupDesc
                            .setVisibility(TextUtils.isEmpty(info) ? View.GONE : View.VISIBLE);
                    mViewGroupDesc.setText(TextUtils.isEmpty(info) ? "" : info);
                } else if (TextUtils.equals(title, getString(R.string.group_ext))) {
                    // 设置群扩展信息
                    mViewGroupExt.setVisibility(TextUtils.isEmpty(info) ? View.GONE : View.VISIBLE);
                    mViewGroupExt.setText(TextUtils.isEmpty(info) ? "" : info);
                }
            } else {
                toastError(bmxErrorCode);
            }
        };
        if (TextUtils.equals(title, getString(R.string.group_rename))) {
            // 修改群名称
            GroupManager.getInstance().setName(mGroup, info, callBack);
        } else if (TextUtils.equals(title, getString(R.string.group_my_name))) {
            // 我在群里的昵称
            GroupManager.getInstance().setMyNickname(mGroup, info, callBack);
        } else if (TextUtils.equals(title, getString(R.string.group_desc))) {
            // 设置群描述
            GroupManager.getInstance().setDescription(mGroup, info, callBack);
        } else if (TextUtils.equals(title, getString(R.string.group_ext))) {
            // 设置群扩展信息
            GroupManager.getInstance().setExtension(mGroup, info, callBack);
        }
    }

    /**
     * 退出群聊
     */
    private void quitGroup() {
        if (mGroup == null) {
            return;
        }
        BMXCallBack callBack = bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                ChatManager.getInstance().deleteConversation(mGroupId, true);
                MainActivity.openMain(ChatGroupOperateActivity.this);
            } else {
                toastError(bmxErrorCode);
            }
        };
        //只有创建者才能解散群聊
        if (mIsOwner) {
            GroupManager.getInstance().destroy(mGroup, callBack);
        } else {
            GroupManager.getInstance().leave(mGroup, callBack);
        }
    }

    /**
     * 设置群已读开关
     * 
     * @param enable 是否开启
     */
    private void setGroupReadAck(boolean enable) {
        showLoadingDialog(true);
        GroupManager.getInstance().setEnableReadAck(mGroup, enable, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                Intent intent = new Intent();
                intent.setAction("onShowReadAckUpdated");
                intent.putExtra("onShowReadAckUpdated", enable);
                RxBus.getInstance().send(intent);
            } else {
                toastError(bmxErrorCode);
                mChatGroupReadMode.setCheckStatus(!enable);
            }
        });
    }

    @Override
    public void onPermissionGranted(List<String> permissions) {
        super.onPermissionGranted(permissions);
        if (permissions == null || permissions.size() == 0) {
            return;
        }
        for (String permission : permissions) {
            switch (permission) {
                case PermissionsConstant.READ_STORAGE:
                    // 读SD权限
                    if (hasPermission(PermissionsConstant.WRITE_STORAGE)) {
                        // 如果有读写权限都有 则直接操作
                        CameraUtils.getInstance().takeGalley(ChatGroupOperateActivity.this,
                                IMAGE_REQUEST);
                    } else {
                        requestPermissions(PermissionsConstant.WRITE_STORAGE);
                    }
                    break;
                case PermissionsConstant.WRITE_STORAGE:
                    // 写SD权限 如果有读写权限都有 则直接操作
                    CameraUtils.getInstance().takeGalley(ChatGroupOperateActivity.this,
                            IMAGE_REQUEST);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onPermissionDenied(List<String> permissions) {
        super.onPermissionDenied(permissions);
        if (permissions == null || permissions.size() == 0) {
            return;
        }
        for (String permission : permissions) {
            switch (permission) {
                case PermissionsConstant.READ_STORAGE:
                case PermissionsConstant.WRITE_STORAGE:
                    // 读写SD权限拒绝
                    CommonProvider.openAppPermission(this);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 展示群聊成员adapter
     */
    private class ChatGroupMemberAdapter extends BaseRecyclerAdapter<BMXGroup.Member> {

        private ImageRequestConfig mConfig;

        public ChatGroupMemberAdapter(Context context) {
            super(context);
            mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
                    .showImageForEmptyUri(R.drawable.default_avatar_icon)
                    .showImageOnFail(R.drawable.default_avatar_icon).cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .showImageOnLoading(R.drawable.default_avatar_icon).build();
        }

        @Override
        protected int onCreateViewById(int viewType) {
            return R.layout.item_group_member;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            ShapeImageView icon = holder.findViewById(R.id.img_icon);
            TextView tvName = holder.findViewById(R.id.txt_name);
            BMXGroup.Member member = getItem(position);
            if (member == null) {
                return;
            }
            long memberId = member.getMUid();
            if (memberId == MessageConfig.MEMBER_ADD) {
                tvName.setText("添加");
                BMImageLoader.getInstance().display(icon,
                        "drawable://" + R.drawable.default_add_icon);
            } else if (memberId == MessageConfig.MEMBER_REMOVE) {
                tvName.setText("移除");
                BMImageLoader.getInstance().display(icon,
                        "drawable://" + R.drawable.default_remove_icon);
            } else {
                BMXRosterItem rosterItem = RosterFetcher.getFetcher().getRoster(memberId);
                String name = "";
                if (rosterItem != null && !TextUtils.isEmpty(rosterItem.alias())) {
                    name = rosterItem.alias();
                } else if (member != null && !TextUtils.isEmpty(member.getMGroupNickname())) {
                    name = member.getMGroupNickname();
                } else if (rosterItem != null && !TextUtils.isEmpty(rosterItem.nickname())) {
                    name = rosterItem.nickname();
                } else if (rosterItem != null) {
                    name = rosterItem.username();
                }
                tvName.setText(TextUtils.isEmpty(name) ? "" : name);
                ChatUtils.getInstance().showRosterAvatar(rosterItem, icon, mConfig);
            }
        }
    }

    private void toastError(Throwable e) {
        String error = e != null ? e.getMessage() : "网络异常";
        ToastUtil.showTextViewPrompt(error);
    }

    private void toastError(BMXErrorCode e) {
        String error = e != null ? e.name() : "网络异常";
        ToastUtil.showTextViewPrompt(error);
    }

    /**
     * 上传头像
     *
     * @param path 路径
     */
    private void uploadAvatar(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        showLoadingDialog(true);
        GroupManager.getInstance().setAvatar(mGroup, path, new FileProgressListener() {
            @Override
            public int onProgressChange(String s) {
                return 0;
            }
        }, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                ToastUtil.showTextViewPrompt("设置成功");
            } else {
                toastError(bmxErrorCode);
            }
        });
    }

    /**
     * 添加群成员
     *
     * @param listOfLongLong 成员
     */
    private void addMembers(ListOfLongLong listOfLongLong, final String message) {
        if (listOfLongLong == null || listOfLongLong.isEmpty()) {
            return;
        }
        showLoadingDialog(true);
        GroupManager.getInstance().addMembers(mGroup, listOfLongLong, message, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                initGroupMembers();
            } else {
                toastError(bmxErrorCode);
            }
        });
    }

    /**
     * 移除群成员
     *
     * @param listOfLongLong 成员
     */
    private void removeMembers(ListOfLongLong listOfLongLong, String reason) {
        if (listOfLongLong == null || listOfLongLong.isEmpty()) {
            return;
        }
        showLoadingDialog(true);
        GroupManager.getInstance().removeMembers(mGroup, listOfLongLong, reason, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                initGroupMembers();
            } else {
                toastError(bmxErrorCode);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IMAGE_REQUEST:
                // 相册
                if (resultCode == Activity.RESULT_OK && data != null) {
                    try {
                        Uri selectedImage = data.getData(); // 获取系统返回的照片的Uri
                        String path = FileUtils.getFilePathByUri(selectedImage);
                        uploadAvatar(path);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            case ADD_MEMBER_REQUEST:
                // 添加群成员
                if (resultCode == RESULT_OK && data != null) {
                    List<Long> chooseList = (List<Long>)data
                            .getSerializableExtra(RosterChooseActivity.CHOOSE_DATA);
                    if (chooseList != null && chooseList.size() > 0) {
                        ListOfLongLong members = new ListOfLongLong();
                        for (Long id : chooseList) {
                            members.add(id);
                        }
                        showOperate(getString(R.string.group_add_members), members);
                    }
                }
                break;
            case REMOVE_MEMBER_REQUEST:
                // 移除群成员
                if (resultCode == RESULT_OK && data != null) {
                    List<Long> chooseList = (List<Long>)data
                            .getSerializableExtra(ChatGroupListMemberActivity.CHOOSE_DATA);
                    if (chooseList != null && chooseList.size() > 0) {
                        ListOfLongLong members = new ListOfLongLong();
                        for (Long id : chooseList) {
                            members.add(id);
                        }
                        showOperate(getString(R.string.group_remove_members), members);
                    }
                }
                break;
            default:
                break;
        }
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
            array = new int[]{
                    R.string.group_notify_all, R.string.group_notify_admin_at,
                    R.string.group_notify_none
            };
        } else if (TextUtils.equals(title, getString(R.string.group_mute_msg))) {
            //屏蔽群消息模式
            array = new int[]{
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
    private void setGroupInfo(String title, final int stringResId) {
        if (stringResId <= 0) {
            return;
        }
        showLoadingDialog(true);
        BMXCallBack callBack = bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                if (TextUtils.equals(title, getString(R.string.group_notify_mode))) {
                    // 群消息通知
                    mGroupNotifyMode.setEndContent(getString(stringResId));
                } else if (TextUtils.equals(title, getString(R.string.group_mute_msg))) {
                    // 屏蔽群消息
                    mMuteGroupMessage.setEndContent(getString(stringResId));
                }
            } else {
                toastError(bmxErrorCode);
            }
        };
        if (TextUtils.equals(title, getString(R.string.group_notify_mode))) {
            // 群消息通知
            BMXGroup.MsgPushMode pushMode = pushModeMap.get(stringResId);
            GroupManager.getInstance().setMsgPushMode(mGroup, pushMode, callBack);
        } else if (TextUtils.equals(title, getString(R.string.group_mute_msg))) {
            // 屏蔽群消息
            BMXGroup.MsgMuteMode muteMode = muteModeMap.get(stringResId);
            GroupManager.getInstance().muteMessage(mGroup, muteMode, callBack);
        }
    }
}
