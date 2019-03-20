
package top.maxim.im.message.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupMemberList;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXRosterItemList;
import im.floo.floolib.ListOfLongLong;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
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
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonDialog;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.utils.permissions.PermissionsConstant;
import top.maxim.im.common.view.BMImageLoader;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ItemLine;
import top.maxim.im.common.view.ItemLineArrow;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseRecyclerAdapter;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.FullyGridLayoutManager;
import top.maxim.im.contact.view.RosterChooseActivity;
import top.maxim.im.contact.view.RosterDetailActivity;
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

    /* 群聊id */
    private ItemLineArrow.Builder mChatGroupId;

    /* 群聊管理 */
    private ItemLineArrow.Builder mChatGroupManager;

    /* 修改名称 */
    private ItemLineArrow.Builder mChatGroupRename;

    /* 群头像 */
    private ItemLineArrow.Builder mGroupAvatar;

    /* 我在群里的昵称 */
    private ItemLineArrow.Builder mGroupMyNickName;

    /* 管理员列表 */
    private ItemLineArrow.Builder mChatGroupManagerList;

    /* 群描述 */
    private ItemLineArrow.Builder mChatGroupDesc;

    private TextView mViewGroupDesc;

    /* 群公告 */
    private ItemLineArrow.Builder mChatGroupNotice;

    private TextView mViewGroupNotice;

    /* 群扩展 */
    private ItemLineArrow.Builder mChatGroupExt;

    private TextView mViewGroupExt;

    /* 群共享 */
    private ItemLineArrow.Builder mChatGroupShare;

    /* 群聊成员gridView */
    private RecyclerView mGvGroupMember;

    /* 群聊成员adapter */
    private ChatGroupMemberAdapter mAdapter;

    /* 退出群聊 */
    private TextView mQuitGroup;

    /* 相册 */
    private final int IMAGE_REQUEST = 1000;

    /* 加人 */
    private final int ADD_MEMBER_REQUEST = 1001;

    /* 减人 */
    private final int REMOVE_MEMBER_REQUEST = 1002;

    public static void startGroupOperateActivity(Context context, long groupId) {
        Intent intent = new Intent(context, ChatGroupOperateActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setHeaderBgColor(getResources().getColor(R.color.c2));
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
        initGroupInfo(true);
    }

    private void initGroupInfo(final boolean syncMember) {
        showLoadingDialog(true);
        Observable.just(mGroupId).map(new Func1<Long, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(Long aLong) {
                return GroupManager.getInstance().search(mGroupId, mGroup, false);
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
                        if (syncMember) {
                            initGroupMembers();
                        }
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

        /* 群主id */
        mChatGroupId = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_owner_id));
        container.addView(mChatGroupId.build());

        // 分割线
        ItemLine.Builder itemLine0 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine0.build());

        /* 群管理 */
        mChatGroupManager = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_manager))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        ChatGroupSettingActivity
                                .startGroupSettingActivity(ChatGroupOperateActivity.this, mGroupId);
                    }
                });
        container.addView(mChatGroupManager.build());

        // 分割线
        ItemLine.Builder itemLine1 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine1.build());

        /* 修改群名称 */
        mChatGroupRename = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_rename))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSettingDialog(getString(R.string.group_rename));
                    }
                });
        container.addView(mChatGroupRename.build());

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
        container.addView(mGroupAvatar.build());

        // 分割线
        ItemLine.Builder itemLine2 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine2.build());

        /* 我在群里的昵称 */
        mGroupMyNickName = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_my_name))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSettingDialog(getString(R.string.group_my_name));
                    }
                });
        container.addView(mGroupMyNickName.build());

        // 分割线
        ItemLine.Builder itemLine3 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine3.build());

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
        container.addView(mChatGroupManagerList.build());
        // 分割线
        ItemLine.Builder itemLine4 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine4.build());

        /* 群描述 */
        mChatGroupDesc = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_desc))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSettingDialog(getString(R.string.group_desc));
                    }
                });
        container.addView(mChatGroupDesc.build());

        // 分割线
        ItemLine.Builder itemLine5 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine5.build());

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
        ItemLine.Builder itemLine6 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine6.build());

        /* 群公告 */
        mChatGroupNotice = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_notice))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        ChatGroupAnnomentActivity.startGroupAnnoucementActivity(
                                ChatGroupOperateActivity.this, mGroupId);
                    }
                });
        container.addView(mChatGroupNotice.build());
        // 分割线
        ItemLine.Builder itemLine7 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine7.build());

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
        ItemLine.Builder itemLine8 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine8.build());

        /* 群扩展信息 */
        mChatGroupExt = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.group_ext))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSettingDialog(getString(R.string.group_ext));
                    }
                });
        container.addView(mChatGroupExt.build());

        // 分割线
        ItemLine.Builder itemLine9 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine9.build());

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
        ItemLine.Builder itemLine10 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine10.build());

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
        container.addView(mChatGroupShare.build());
        // 分割线
        ItemLine.Builder itemLine11 = new ItemLine.Builder(this, container);
        container.addView(itemLine11.build());

        // 退出群聊
        mQuitGroup = new TextView(this);
        mQuitGroup.setTextColor(getResources().getColor(R.color.color_white));
        mQuitGroup.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        mQuitGroup.setGravity(Gravity.CENTER);
        mQuitGroup.setText(getString(R.string.group_quit));
        mQuitGroup.setBackgroundResource(R.drawable.common_red_btn_corner_bg);
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
                ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dp2px(44));
        quitP.topMargin = ScreenUtils.dp2px(5);
        quitP.bottomMargin = ScreenUtils.dp2px(5);
        quitP.leftMargin = ScreenUtils.dp2px(15);
        quitP.rightMargin = ScreenUtils.dp2px(15);
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
                            true, true, ADD_MEMBER_REQUEST);
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
        final long ownerId = mGroup.ownerId();
        mChatGroupId.setEndContent(ownerId > 0 ? String.valueOf(ownerId) : "");
        mChatGroupId.setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
            @Override
            public void onItemClick(View v) {
                if (ownerId > 0) {
                    RosterDetailActivity.openRosterDetail(ChatGroupOperateActivity.this, ownerId);
                }
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
    }

    /**
     * 初始化群成员
     */
    private void initGroupMembers() {
        final BMXGroupMemberList memberList = new BMXGroupMemberList();
        Observable.just(mGroup).map(new Func1<BMXGroup, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXGroup group) {
                return GroupManager.getInstance().getMembers(mGroup, memberList, true);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).map(new Func1<BMXErrorCode, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXErrorCode errorCode) {
                if (!memberList.isEmpty()) {
                    ListOfLongLong listOfLongLong = new ListOfLongLong();
                    for (int i = 0; i < memberList.size(); i++) {
                        listOfLongLong.add(memberList.get(i).getMUid());
                    }
                    BMXRosterItemList itemList = new BMXRosterItemList();
                    BMXErrorCode errorCode1 = RosterManager.getInstance().search(listOfLongLong,
                            itemList, true);
                    RosterFetcher.getFetcher().putRosters(itemList);
                    return errorCode1;
                }
                return errorCode;
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
                        List<BMXGroup.Member> members = new ArrayList<>();
                        for (int i = 0; i < memberList.size(); i++) {
                            members.add(memberList.get(i));
                        }
                        // 群人数
                        long memberCount = memberList.size();
                        mHeader.setTitle(String.format(getString(R.string.group_info),
                                String.valueOf(memberCount)));
                        // 增加添加 移除
                        BMXGroup.Member add = new BMXGroup.Member(MessageConfig.MEMBER_ADD, "", 0);
                        BMXGroup.Member remove = new BMXGroup.Member(MessageConfig.MEMBER_REMOVE,
                                "", 0);
                        members.add(add);
                        members.add(remove);
                        mAdapter.replaceList(members);
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
                        if (TextUtils.isEmpty(content)) {
                            return;
                        }
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
        DialogUtils.getInstance().showEditDialog(this, title, getString(R.string.confirm),
                getString(R.string.cancel), new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        if (TextUtils.isEmpty(content)) {
                            return;
                        }
                        if (TextUtils.equals(title, getString(R.string.group_add_members))) {
                            addMembers(member, content);
                        } else if (TextUtils.equals(title,
                                getString(R.string.group_remove_members))) {
                            removeMembers(member, content);
                        }
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 更新信息
     */
    private void setGroupInfo(final String title, final String info) {
        if (TextUtils.isEmpty(info)) {
            return;
        }
        showLoadingDialog(true);
        Observable.just(info).map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                if (TextUtils.equals(title, getString(R.string.group_rename))) {
                    // 修改群名称
                    return GroupManager.getInstance().setName(mGroup, s);
                }
                if (TextUtils.equals(title, getString(R.string.group_my_name))) {
                    // 我在群里的昵称
                    return GroupManager.getInstance().setMyNickname(mGroup, s);
                }
                if (TextUtils.equals(title, getString(R.string.group_desc))) {
                    // 设置群描述
                    return GroupManager.getInstance().setDescription(mGroup, s);
                }
                if (TextUtils.equals(title, getString(R.string.group_ext))) {
                    // 设置群扩展信息
                    return GroupManager.getInstance().setExtension(mGroup, s);
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
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        toastError(e);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        if (TextUtils.equals(title, getString(R.string.group_rename))) {
                            // 修改群名称
                            mChatGroupRename.setEndContent(info);
                        } else if (TextUtils.equals(title, getString(R.string.group_my_name))) {
                            // 我在群里的昵称
                            mGroupMyNickName.setEndContent(info);
                        } else if (TextUtils.equals(title, getString(R.string.group_desc))) {
                            // 设置群描述
                            mViewGroupDesc.setVisibility(View.VISIBLE);
                            mViewGroupDesc.setText(info);
                        } else if (TextUtils.equals(title, getString(R.string.group_ext))) {
                            // 设置群扩展信息
                            mViewGroupExt.setVisibility(View.VISIBLE);
                            mViewGroupExt.setText(info);
                        }
                    }
                });
    }

    /**
     * 退出群聊
     */
    private void quitGroup() {
        Observable.just(mGroup).map(new Func1<BMXGroup, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXGroup group) {
                boolean isOwner = GroupManager.getInstance().isGroupOwner(mGroup.ownerId());
                return isOwner ? GroupManager.getInstance().destroy(mGroup)
                        : GroupManager.getInstance().leave(group);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                ChatManager.getInstance().deleteConversation(mGroupId);
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
                        MainActivity.openMain(ChatGroupOperateActivity.this);
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
                    .showImageOnFail(R.drawable.default_avatar_icon)
                    .cacheOnDisk(true)
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
                BMImageLoader.getInstance().display(icon, "drawable://" + R.drawable.default_add_icon);
            } else if (memberId == MessageConfig.MEMBER_REMOVE) {
                tvName.setText("移除");
                BMImageLoader.getInstance().display(icon, "drawable://" + R.drawable.default_remove_icon);
            } else {
                BMXRosterItem rosterItem = RosterFetcher.getFetcher().getRoster(memberId);
                String name;
                if (rosterItem != null && !TextUtils.isEmpty(rosterItem.nickname())) {
                    name = rosterItem.nickname();
                } else if (rosterItem != null) {
                    name = rosterItem.username();
                } else {
                    name = member.getMGroupNickname();
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
        Observable.just(path).map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                return GroupManager.getInstance().setAvatar(mGroup, s);
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
        Observable.just(listOfLongLong).map(new Func1<ListOfLongLong, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(ListOfLongLong s) {
                return GroupManager.getInstance().addMembers(mGroup, s, message);
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
                        initGroupMembers();
                    }
                });
    }

    /**
     * 移除群成员
     *
     * @param listOfLongLong 成员
     */
    private void removeMembers(ListOfLongLong listOfLongLong, final String reason) {
        if (listOfLongLong == null || listOfLongLong.isEmpty()) {
            return;
        }
        showLoadingDialog(true);
        Observable.just(listOfLongLong).map(new Func1<ListOfLongLong, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(ListOfLongLong s) {
                return GroupManager.getInstance().removeMembers(mGroup, s, reason);
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
                        initGroupMembers();
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
}
