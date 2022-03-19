
package top.maxim.im.message.view;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import im.floo.BMXDataCallBack;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupMemberList;
import im.floo.floolib.ListOfLongLong;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description : 群聊黑名单 Created by Mango on 2018/11/25.
 */
public class ChatGroupBlockActivity extends ChatGroupListMemberActivity {

    private boolean isEdit = false;

    private int CHOOSE_BLACK_CODE = 1000;

    public static void startGroupBlockActivity(Context context, long groupId) {
        Intent intent = new Intent(context, ChatGroupBlockActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.group_black);
        builder.setRightText(R.string.edit, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEdit = !isEdit;
                if (isEdit) {
                    mHeader.setRightText(getString(R.string.confirm));
                } else {
                    removeBlack();
                    mHeader.setRightText(getString(R.string.edit));
                }
                mAdapter.setShowCheck(isEdit);
                mAdapter.notifyDataSetChanged();
            }
        });
        builder.setBackIcon(R.drawable.header_back_icon, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        return builder.build();
    }

    @Override
    protected void setViewListener() {
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BMXGroup.Member member = mAdapter.getItem(position);
                if (member == null) {
                    return;
                }
                long mId = member.getMUid();
                if (!mAdapter.getShowCheck() && mId == MessageConfig.MEMBER_ADD) {
                    ChatGroupListMemberActivity.startGroupMemberListActivity(
                            ChatGroupBlockActivity.this, mGroupId, true, CHOOSE_BLACK_CODE);
                    return;
                }
                if (mAdapter.getShowCheck() && mId != MessageConfig.MEMBER_ADD) {
                    if (!mSelected.containsKey(mId) || !mSelected.get(mId)) {
                        mSelected.put(mId, true);
                    } else {
                        mSelected.remove(mId);
                    }
                    mAdapter.notifyItemChanged(position);
                }

            }
        });
    }

    @Override
    protected void initData(String cursor, BMXDataCallBack<BMXGroupMemberList> callBack) {
        GroupManager.getInstance().getBlockList(mGroup, cursor, DEFAULT_PAGE_SIZE, (bmxErrorCode, page) -> {
            BMXGroupMemberList memberListTmp = new BMXGroupMemberList();
            if (page != null && page.result() != null && !page.result().isEmpty()) {
                mCursor = page.cursor();
                BMXGroupMemberList list = page.result();
                long myId = SharePreferenceUtils.getInstance().getUserId();
                for (int i = 0; i < list.size(); i++) {
                    long memberId = list.get(i).getMUid();
                    if (myId != memberId) {
                        memberListTmp.add(list.get(i));
                    }
                }
            }
            if (callBack != null) {
                callBack.onResult(bmxErrorCode, memberListTmp);
            }
        });
    }

    @Override
    protected void initData(boolean forceRefresh, BMXDataCallBack<BMXGroupMemberList> callBack) {
        GroupManager.getInstance().getBlockList(mGroup, forceRefresh, callBack);
    }

    @Override
    protected void bindData(BMXGroupMemberList memberList, boolean upload) {
        List<BMXGroup.Member> members = new ArrayList<>();
        if (memberList != null && !memberList.isEmpty()) {
            for (int i = 0; i < memberList.size(); i++) {
                members.add(memberList.get(i));
            }
        }
        if (upload) {
            int count = mAdapter.getItemCount();
            mAdapter.addList(members, count > 1 ? count - 1 : 0);
        } else {
            BMXGroup.Member add = new BMXGroup.Member(MessageConfig.MEMBER_ADD, "", 0);
            members.add(add);
            mAdapter.replaceList(members);
        }
    }

    private void removeBlack() {
        final ListOfLongLong black = new ListOfLongLong();
        for (Map.Entry<Long, Boolean> entry : mSelected.entrySet()) {
            if (entry.getValue()) {
                black.add(entry.getKey());
            }
        }
        if (black.isEmpty()) {
            return;
        }
        showLoadingDialog(true);
        GroupManager.getInstance().unblockMembers(mGroup, black, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                init();
            } else {
                toastError(bmxErrorCode);
            }
        });
    }

    private void addBlack(ListOfLongLong black) {
        if (black == null || black.isEmpty()) {
            return;
        }
        showLoadingDialog(true);
        GroupManager.getInstance().blockMembers(mGroup, black, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                init();
            } else {
                toastError(bmxErrorCode);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_BLACK_CODE && resultCode == RESULT_OK && data != null) {
            List<Long> chooseList = (List<Long>)data
                    .getSerializableExtra(ChatGroupListMemberActivity.CHOOSE_DATA);
            if (chooseList != null && chooseList.size() > 0) {
                ListOfLongLong admin = new ListOfLongLong();
                for (Long id : chooseList) {
                    admin.add(id);
                }
                addBlack(admin);
            }
        }
    }
}
