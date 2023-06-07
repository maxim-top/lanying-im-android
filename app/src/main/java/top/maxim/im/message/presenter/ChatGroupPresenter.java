
package top.maxim.im.message.presenter;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageConfig;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.ChatManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.permissions.PermissionsConstant;
import top.maxim.im.message.contract.ChatGroupContract;
import top.maxim.im.message.utils.MessageConfig;
import top.maxim.im.message.view.ChatGroupAtActivity;
import top.maxim.im.message.view.ChatGroupListMemberActivity;
import top.maxim.im.message.view.GroupAckActivity;
import top.maxim.im.videocall.GroupVideoCallActivity;

/**
 * Description : 群聊presenter Created by Mango on 2018/11/11.
 */
public class ChatGroupPresenter extends ChatBasePresenter implements ChatGroupContract.Presenter {

    private ChatGroupContract.View mView;

    private ChatGroupContract.Model mModel;

    private final int AT_REQUEST = 2000;

    private final int VIDEO_CALL_REQUEST = 2001;

    /* 文本@的对象列表 以feedId作为唯一标志 */
    private Map<String, String> mAtMap;

    private List<Long> mMemberIdList;

    private boolean mShowAckRead = false;

    public ChatGroupPresenter(ChatGroupContract.View view) {
        super();
        mView = view;
        mView.setPresenter(this);
        setChatBaseView(mView, mModel);
    }

    @Override
    public void setChatInfo(BMXMessage.MessageType chatType, long myUserId, final long chatId) {
        super.setChatInfo(chatType, myUserId, chatId);
        // 先从conversation获取名称展示
        if (mConversation != null) {
            BMXGroup groupItem = RosterFetcher.getFetcher()
                    .getGroup(mConversation.conversationId());
            String name = groupItem != null ? groupItem.name() : "";
            if (mView != null) {
                mView.setHeadTitle(name);
            }
        }
        // 获取群聊信息
        if (chatId <= 0) {
            return;
        }
        GroupManager.getInstance().getGroupInfo(chatId, true, (bmxErrorCode, group) -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                if (group.groupType() == BMXGroup.GroupType.Chatroom) {
                    //如果是聊天室  需要判断自己是否在群内  不在需要弹出提示框 再次手动入群
                    if (!group.isMember()) {
                        if (mView != null) {
                            mView.showJoinGroupDialog(group);
                        }
                        return;
                    }
                }
                RosterFetcher.getFetcher().putGroup(group);
                if (mView != null) {
                    if (group != null) {
                        mView.setHeadTitle(group.name());
                        mShowAckRead = group.enableReadAck();
                    }
                    mView.showReadAck(mShowAckRead);
                }
                syncGroupMember(group);
            }
        });
    }

    /**
     * 同步群成员
     */
    private void syncGroupMember(BMXGroup group) {
        if (group == null) {
            return;
        }
        GroupManager.getInstance().getMembers(group, true, (bmxErrorCode, memberList) -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                if (mMemberIdList == null) {
                    mMemberIdList = new ArrayList<>();
                }
                mMemberIdList.clear();
                if (memberList != null && !memberList.isEmpty()) {
                    for (int i = 0; i < memberList.size(); i++) {
                        mMemberIdList.add(memberList.get(i).getMUid());
                    }
                }
            }
        });
    }

    /**
     * 清除@对象 发送完文本消息清除
     */
    @Override
    public void clearAtFeed() {
        if (mAtMap != null) {
            mAtMap.clear();
        }
    }

    @Override
    public Map<String, String> getChatAtMembers() {
        return mAtMap;
    }

    @Override
    protected boolean isCurrentSession(BMXMessage message) {
        return message != null && message.type() == BMXMessage.MessageType.Group
                && message.toId() == mChatId;
    }

    @Override
    public void onChatAtMember() {
        ChatGroupAtActivity.startGroupAtActivity(mView.getContext(), mChatId, AT_REQUEST);
    }

    @Override
    public void setGroupAck(boolean ack) {
        mShowAckRead = ack;
    }

    @Override
    public void joinGroup(BMXGroup group, String reason) {
        if (group == null) {
            return;
        }
        if (mView != null) {
            mView.showLoading(true);
        }
        GroupManager.getInstance().join(group, reason, bmxErrorCode -> {
            if (mView != null) {
                mView.cancelLoading();
            }
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                ToastUtil.showTextViewPrompt(mView.getContext().getString(R.string.join_successfully));
            } else {
                String error = bmxErrorCode != null ? bmxErrorCode.name() : mView.getContext().getString(R.string.failed_to_join);
                ToastUtil.showTextViewPrompt(error);
                ((Activity) mView.getContext()).finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AT_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Map<String, String> map = (Map<String, String>) data
                    .getSerializableExtra(ChatGroupAtActivity.CHOOSE_DATA);
            if (map != null && !map.isEmpty()) {
                if (mAtMap == null) {
                    mAtMap = new HashMap<>();
                } else {
                    mAtMap.clear();
                }
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    mAtMap.put(entry.getKey(), entry.getValue());
                }
            }
            handleAt();
        } else if (requestCode == VIDEO_CALL_REQUEST && resultCode == Activity.RESULT_OK
                && data != null) {
            ArrayList<Long> chooseList = (ArrayList<Long>) data
                    .getSerializableExtra(ChatGroupListMemberActivity.CHOOSE_DATA);
//            GroupVideoCallActivity.openVideoCall(mView.getContext(), chooseList, String.valueOf(mChatId), true, MessageConfig.CallMode.CALL_AUDIO);
        }
    }

    /**
     * 处理@
     */
    private void handleAt() {
        if (mAtMap == null || mAtMap.isEmpty()) {
            return;
        }
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, String> entry : mAtMap.entrySet()) {
            // 发送文字包含@对象的名称时再加入 防止输入框@对象名称被修改
            if (entry.getValue() != null && !TextUtils.isEmpty(entry.getValue())) {
                names.add("@" + entry.getValue());
            }
        }
        // @功能 名字后加空格
        if (!names.isEmpty() && mView != null) {
            // @的内容️以ForegroundColorSpan转换
            mView.insertInAt(names);
        }
    }

    @Override
    protected void ackMessage(BMXMessage message) {
        if (!mShowAckRead) {
            return;
        }
        super.ackMessage(message);
    }

    @Override
    public void onGroupAck(BMXMessage bean) {
        if (bean == null || mMemberIdList == null || mMemberIdList.isEmpty()) {
            return;
        }
        ChatManager.getInstance().getGroupAckMessageUserIdList(bean, (bmxErrorCode, list) -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                List<Long> readList = new ArrayList<>();
                if (list != null && !list.isEmpty()) {
                    for (int i = 0; i < list.size(); i++) {
                        readList.add(list.get(i));
                    }
                }
                GroupAckActivity.openGroupAckActivity(mView.getContext(), mMemberIdList, readList);
            } else {
                ToastUtil.showTextViewPrompt(mView.getContext().getString(R.string.failed_to_get_read_list));
            }
        });
    }

    @Override
    protected void showVideoCallDialog() {
        // 视频需要摄像头 麦克风权限
        if (hasPermission(PermissionsConstant.CAMERA, PermissionsConstant.RECORD_AUDIO)) {
            handelVideoCall(true);
        } else {
            // 如果没有权限 首先请求SD读权限
            requestPermissions(TYPE_VIDEO_CALL_PERMISSION, PermissionsConstant.CAMERA);
        }
    }

    @Override
    protected void handelVideoCall(boolean hasVideo) {
        ChatGroupListMemberActivity.startGroupMemberListActivity((Activity) mView.getContext(), mChatId, true, VIDEO_CALL_REQUEST);
    }

    @Override
    protected void receiveVideoCall(long roomId, List<Long> chatIds, boolean hasVideo) {
        GroupVideoCallActivity.openVideoCall(mView.getContext(), (ArrayList<Long>) chatIds, roomId, false,
                hasVideo? BMXMessageConfig.RTCCallType.VideoCall: BMXMessageConfig.RTCCallType.AudioCall);
    }
}
