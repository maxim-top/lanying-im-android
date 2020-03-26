
package top.maxim.im.message.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupMemberList;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.ListOfLongLong;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.BMImageLoader;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseRecyclerAdapter;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.FullyGridLayoutManager;
import top.maxim.im.contact.view.RosterChooseActivity;
import top.maxim.im.contact.view.RosterDetailActivity;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description : 群聊成员 Created by Mango on 2018/11/25.
 */
public class ChatGroupMemberActivity extends BaseTitleActivity {

    /* 群聊成员gridView */
    protected RecyclerView mGvGroupMember;

    /* 群聊成员adapter */
    protected ChatGroupMemberAdapter mAdapter;

    protected long mGroupId;

    protected BMXGroup mGroup = new BMXGroup();

    /* 加人 */
    private final int ADD_MEMBER_REQUEST = 1000;

    /* 减人 */
    private final int REMOVE_MEMBER_REQUEST = 1001;

    /* 是否是群聊创建者 */
    private boolean mIsOwner;
    
    private List<String> memberIdList;
    
    public static void startGroupMemberActivity(Context context, long groupId) {
        Intent intent = new Intent(context, ChatGroupMemberActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.group_member);
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
        View view = View.inflate(this, R.layout.chat_group_member_view, null);
        mGvGroupMember = view.findViewById(R.id.gv_chat_group_member);
        mGvGroupMember.setLayoutManager(new FullyGridLayoutManager(this, 5));
        mGvGroupMember.setAdapter(mAdapter = new ChatGroupMemberAdapter(this));
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
                    RosterChooseActivity.startRosterListActivity(ChatGroupMemberActivity.this, true,
                            true, memberIdList, ADD_MEMBER_REQUEST);
                } else if (memberId == MessageConfig.MEMBER_REMOVE) {
                    // 移除
                    ChatGroupListMemberActivity.startGroupMemberListActivity(
                            ChatGroupMemberActivity.this, mGroupId, true, REMOVE_MEMBER_REQUEST);
                } else {
                    RosterDetailActivity.openRosterDetail(ChatGroupMemberActivity.this, memberId);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        initGroupInfo();
    }

    private void initGroupInfo() {
        showLoadingDialog(true);
        GroupManager.getInstance().getGroupList(mGroupId, false, (bmxErrorCode, bmxGroup) -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                if (bmxGroup != null) {
                    mGroup = bmxGroup;
                }
                mIsOwner = GroupManager.getInstance().isGroupOwner(mGroup.ownerId());
                initGroupMembers();
            } else {
                toastError(bmxErrorCode);
            }
        });
    }

    private void initGroupMembers() {
        GroupManager.getInstance().getMembers(mGroup, true, (bmxErrorCode, memberList) -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                ListOfLongLong listOfLongLong = new ListOfLongLong();
                for (int i = 0; i < memberList.size(); i++) {
                    listOfLongLong.add(memberList.get(i).getMUid());
                }
                RosterManager.getInstance().getRosterList(listOfLongLong, true,
                        (bmxErrorCode1, itemList) -> {
                            RosterFetcher.getFetcher().putRosters(itemList);
                            if (!BaseManager.bmxFinish(bmxErrorCode1)) {
                                toastError(bmxErrorCode1);
                            }
                            bindData(memberList);
                        });
            } else {
                toastError(bmxErrorCode);
            }
        });
    }

    protected void bindData(BMXGroupMemberList memberList) {
        List<BMXGroup.Member> members = new ArrayList<>();
        if (memberIdList == null) {
            memberIdList = new ArrayList<>();
        }
        memberIdList.clear();
        for (int i = 0; i < memberList.size(); i++) {
            BMXGroup.Member member = memberList.get(i);
            if (member != null) {
                members.add(member);
                memberIdList.add(String.valueOf(member.getMUid()));
            }
        }
        // 群主才有添加 删除功能
        if (mIsOwner) {
            // 增加添加 移除
            BMXGroup.Member add = new BMXGroup.Member(MessageConfig.MEMBER_ADD, "", 0);
            BMXGroup.Member remove = new BMXGroup.Member(MessageConfig.MEMBER_REMOVE, "", 0);
            members.add(add);
            members.add(remove);
        }
        mAdapter.replaceList(members);
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
//        DialogUtils.getInstance().showEditDialog(this, title, getString(R.string.confirm),
//                getString(R.string.cancel), new CommonEditDialog.OnDialogListener() {
//                    @Override
//                    public void onConfirmListener(String content) {
//                        if (TextUtils.isEmpty(content)) {
//                            return;
//                        }
//                        if (TextUtils.equals(title, getString(R.string.group_add_members))) {
//                            addMembers(member, content);
//                        } else if (TextUtils.equals(title,
//                                getString(R.string.group_remove_members))) {
//                            removeMembers(member, content);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelListener() {
//
//                    }
//                });
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

    private void toastError(BMXErrorCode e) {
        String error = e != null ? e.name() : "网络异常";
        ToastUtil.showTextViewPrompt(error);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
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
                String name = "";
                if (rosterItem != null) {
                    if (!TextUtils.isEmpty(rosterItem.alias())) {
                        name = rosterItem.alias();
                    } else if (!TextUtils.isEmpty(rosterItem.nickname())) {
                        name = rosterItem.nickname();
                    } else {
                        name = rosterItem.username();
                    }
                }
                tvName.setText(TextUtils.isEmpty(name) ? "" : name);

                ChatUtils.getInstance().showRosterAvatar(rosterItem, icon, mConfig);
            }
        }
    }

}
