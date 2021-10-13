
package top.maxim.im.bmxmanager;

import im.floo.BMXCallBack;
import im.floo.BMXDataCallBack;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupAnnouncementList;
import im.floo.floolib.BMXGroupBannedMemberList;
import im.floo.floolib.BMXGroupBannedMemberResultPage;
import im.floo.floolib.BMXGroupList;
import im.floo.floolib.BMXGroupManager;
import im.floo.floolib.BMXGroupMemberList;
import im.floo.floolib.BMXGroupMemberResultPage;
import im.floo.floolib.BMXGroupService;
import im.floo.floolib.BMXGroupServiceListener;
import im.floo.floolib.BMXGroupSharedFileList;
import im.floo.floolib.FileProgressListener;
import im.floo.floolib.GroupApplicationPage;
import im.floo.floolib.GroupInvitaionPage;
import im.floo.floolib.ListOfLongLong;
import top.maxim.im.common.utils.SharePreferenceUtils;

/**
 * Description : group Created by Mango on 2018/12/2.
 */
public class GroupManager extends BaseManager {

    private static final String TAG = GroupManager.class.getSimpleName();

    private static final GroupManager sInstance = new GroupManager();

    private BMXGroupManager mService;

    private BMXGroupService mGroupService;

    public static GroupManager getInstance() {
        return sInstance;
    }

    private GroupManager() {
        mService = bmxClient.getGroupManager();
        mGroupService = bmxClient.getGroupService();
    }

    /**
     * 获取群组列表，如果设置了forceRefresh则从服务器拉取
     **/
    public void getGroupList(boolean forceRefresh, BMXDataCallBack<BMXGroupList> callBack) {
        mService.getGroupList(forceRefresh, callBack);
    }

    /**
     * 获取群信息
     **/
    public void getGroupList(ListOfLongLong groupIdList,
                             boolean forceRefresh, BMXDataCallBack<BMXGroupList> callBack) {
        mService.getGroupList(groupIdList, forceRefresh, callBack);
    }

    /**
     * 获取群信息
     **/
    public void getGroupList(long groupId, boolean forceUpdate, BMXDataCallBack<BMXGroup> callBack) {
        mService.getGroupList(groupId, forceUpdate, callBack);
    }

    /**
     * 获取群信息
     **/
    public BMXGroup getGroupListByDB(long groupId) {
        BMXGroup group = new BMXGroup();
        BMXErrorCode error = mGroupService.search(groupId, group, false);
        if (error == null || error.swigValue() != BMXErrorCode.NoError.swigValue()) {
            return null;
        }
        return group;
    }

    /**
     * 群聊邀请
     */
    public void getInvitationList(String cursor, int pageSize, BMXDataCallBack<GroupInvitaionPage> callBack) {
        mService.getInvitationList(cursor, pageSize, callBack);
    }

    /**
     * 入群通知
     */
    public void getApplicationList(BMXGroupList list, String cursor, int pageSize,
            BMXDataCallBack<GroupApplicationPage> callBack) {
        mService.getApplicationList(list, cursor, pageSize, callBack);
    }

    /**
     * 创建群
     **/
    public void create(BMXGroupService.CreateGroupOptions options, BMXDataCallBack<BMXGroup> callBack) {
        mService.create(options, callBack);
    }

    /**
     * 销毁群
     **/
    public void destroy(BMXGroup group, BMXCallBack callBack) {
        mService.destroy(group, callBack);
    }

    /**
     * 加入一个群，根据群设置可能需要管理员批准
     **/
    public void join(BMXGroup group, String message, BMXCallBack callBack) {
        mService.join(group, message, callBack);
    }

    /**
     * 退出群
     **/
    public void leave(BMXGroup group, BMXCallBack callBack) {
        mService.leave(group, callBack);
    }

    /**
     * 获取群详情，从服务端拉取最新信息
     **/
    public void getInfo(BMXGroup group, BMXDataCallBack<BMXGroup> callBack) {
        mService.getInfo(group, callBack);
    }

    /**
     * 获取群成员列表，如果设置了forceRefresh则从服务器拉取
     **/
    public void getMembers(BMXGroup group, boolean forceRefresh, BMXDataCallBack<BMXGroupMemberList> callBack) {
        mService.getMembers(group, forceRefresh, callBack);
    }

    /**
     * 获取群成员列表，如果设置了forceRefresh则从服务器拉取
     **/
    public void getMembers(BMXGroup group, String cursor, int pageSize, BMXDataCallBack<BMXGroupMemberResultPage> callBack) {
        mService.getMembers(group, cursor, pageSize, callBack);
    }

    /**
     * 添加群成员
     **/
    public void addMembers(BMXGroup group, ListOfLongLong listOfLongLong, String message, BMXCallBack callBack) {
        mService.addMembers(group, listOfLongLong, message, callBack);
    }

    /**
     * 删除群成员
     **/
    public void removeMembers(BMXGroup group, ListOfLongLong listOfLongLong, String reason,
            BMXCallBack callBack) {
        mService.removeMembers(group, listOfLongLong, reason, callBack);
    }

    /**
     * 添加管理员
     **/
    public void addAdmins(BMXGroup group, ListOfLongLong listOfLongLong, String message,
            BMXCallBack callBack) {
        mService.addAdmins(group, listOfLongLong, message, callBack);
    }

    /**
     * 删除管理员
     **/
    public void removeAdmins(BMXGroup group, ListOfLongLong listOfLongLong, String reason,
            BMXCallBack callBack) {
        mService.removeAdmins(group, listOfLongLong, reason, callBack);
    }

    /**
     * 获取Admins列表，如果设置了forceRefresh则从服务器拉取
     **/
    public void getAdmins(BMXGroup group, boolean forceRefresh, BMXDataCallBack<BMXGroupMemberList> callBack) {
        mService.getAdmins(group, forceRefresh, callBack);
    }

    /**
     * 添加黑名单
     **/
    public void blockMembers(BMXGroup group, ListOfLongLong listOfLongLong, BMXCallBack callBack) {
        mService.blockMembers(group, listOfLongLong, callBack);
    }

    /**
     * 从黑名单删除
     **/
    public void unblockMembers(BMXGroup group, ListOfLongLong listOfLongLong, BMXCallBack callBack) {
        mService.unblockMembers(group, listOfLongLong, callBack);
    }

    /**
     * 获取黑名单
     **/
    public void getBlockList(BMXGroup group, boolean forceRefresh,
            BMXDataCallBack<BMXGroupMemberList> callBack) {
        mService.getBlockList(group, forceRefresh, callBack);
    }

    /**
     * 获取黑名单
     **/
    public void getBlockList(BMXGroup group, String cursor, int pageSize,
            BMXDataCallBack<BMXGroupMemberResultPage> callBack) {
        mService.getBlockList(group, cursor, pageSize, callBack);
    }

    /**
     * 禁言
     **/
    public void banMembers(BMXGroup group, ListOfLongLong listOfLongLong, long duration,
            String reason, BMXCallBack callBack) {
        mService.banMembers(group, listOfLongLong, duration, reason, callBack);
    }

    /**
     * 解除禁言
     **/
    public void unbanMembers(BMXGroup group, ListOfLongLong listOfLongLong, BMXCallBack callBack) {
        mService.unbanMembers(group, listOfLongLong, callBack);
    }

    /**
     * 全员禁言
     */
    public void banGroup(BMXGroup group, long duration, BMXCallBack callBack) {
        mService.banGroup(group, duration, callBack);
    }

    /**
     * 解除全员禁言
     */
    public void unbanGroup(BMXGroup group, BMXCallBack callBack) {
        mService.unbanGroup(group, callBack);
    }

    /**
     * 获取禁言列表
     **/
    public void getBannedMembers(BMXGroup group, BMXDataCallBack<BMXGroupBannedMemberList> callBack) {
        mService.getBannedMembers(group, callBack);
    }

    /**
     * 获取禁言列表
     **/
    public void getBannedMembers(BMXGroup group, String cursor, int pageSize, BMXDataCallBack<BMXGroupBannedMemberResultPage> callBack) {
        mService.getBannedMembers(group, cursor, pageSize, callBack);
    }

    /**
     * 屏蔽群消息开关
     **/
    public void muteMessage(BMXGroup group, BMXGroup.MsgMuteMode mode, BMXCallBack callBack) {
        mService.muteMessage(group, mode, callBack);
    }

    /**
     * 接受入群申请
     **/
    public void acceptApplication(BMXGroup group, long applicantId, BMXCallBack callBack) {
        mService.acceptApplication(group, applicantId, callBack);
    }

    /**
     * 拒绝入群申请
     **/
    public void declineApplication(BMXGroup group, long applicantId, String reason, BMXCallBack callBack) {
        mService.declineApplication(group, applicantId, reason, callBack);
    }

    /**
     * 接受入群邀请
     **/
    public void acceptInvitation(BMXGroup group, long inviter, BMXCallBack callBack) {
        mService.acceptInvitation(group, inviter, callBack);
    }

    /**
     * 拒绝入群邀请
     **/
    public void declineInvitation(BMXGroup group, long inviter, BMXCallBack callBack) {
        mService.declineInvitation(group, inviter, callBack);
    }

    /**
     * 转移群主
     **/
    public void transferOwner(BMXGroup group, long newOwnerId, BMXCallBack callBack) {
        mService.transferOwner(group, newOwnerId, callBack);
    }

    /**
     * 添加群共享文件
     **/
    public void uploadSharedFile(BMXGroup group, String filePath, String displayName,
            String extensionName, FileProgressListener listener, BMXCallBack callBack) {
        mService.uploadSharedFile(group, filePath, displayName, extensionName, listener, callBack);
    }

    /**
     * 移除群共享文件
     **/
    public void removeSharedFile(BMXGroup group, BMXGroup.SharedFile sharedFile, BMXCallBack callBack) {
        mService.removeSharedFile(group, sharedFile, callBack);
    }

    /**
     * 下载群共享文件
     **/
    public void downloadSharedFile(BMXGroup group, BMXGroup.SharedFile sharedFile,
            FileProgressListener listener, BMXCallBack callBack) {
        mService.downloadSharedFile(group, sharedFile, listener, callBack);
    }

    /**
     * 获取群共享文件列表
     **/
    public void getSharedFilesList(BMXGroup group, boolean forceRefresh,
            BMXDataCallBack<BMXGroupSharedFileList> callBack) {
        mService.getSharedFilesList(group, forceRefresh, callBack);
    }

    /**
     * 修改群文件名称
     */
    public void changeSharedFileName(BMXGroup group, BMXGroup.SharedFile sharedFile, String name,
            BMXCallBack callBack) {
        mService.changeSharedFileName(group, sharedFile, name, callBack);
    }

    public void getLatestAnnouncement(BMXGroup group, boolean forceRefresh,
            BMXDataCallBack<BMXGroup.Announcement> callBack) {
        mService.getLatestAnnouncement(group, forceRefresh, callBack);
    }

    public void getAnnouncementList(BMXGroup group, boolean forceRefresh,
            BMXDataCallBack<BMXGroupAnnouncementList> callBack) {
        mService.getAnnouncementList(group, forceRefresh, callBack);
    }

    public void editAnnouncement(BMXGroup group, String title, String content, BMXCallBack callBack) {
        mService.editAnnouncement(group, title, content, callBack);
    }

    public void deleteAnnouncement(BMXGroup group, long announcementId, BMXCallBack callBack) {
        mService.deleteAnnouncement(group, announcementId, callBack);
    }

    /**
     * 设置群名称
     **/
    public void setName(BMXGroup group, String name, BMXCallBack callBack) {
        mService.setName(group, name, callBack);
    }

    /**
     * 设置群描述信息
     **/
    public void setDescription(BMXGroup group, String description,BMXCallBack callBack) {
        mService.setDescription(group, description, callBack);
    }

    /**
     * 设置群扩展信息
     **/
    public void setExtension(BMXGroup group, String extension, BMXCallBack callBack) {
        mService.setExtension(group, extension, callBack);
    }

    /**
     * 设置在群里的昵称
     **/
    public void setMyNickname(BMXGroup group, String nickname, BMXCallBack callBack) {
        mService.setMyNickname(group, nickname, callBack);
    }

    /**
     * 设置群消息通知模式
     **/
    public void setMsgPushMode(BMXGroup group, BMXGroup.MsgPushMode mode, BMXCallBack callBack) {
        mService.setMsgPushMode(group, mode, callBack);
    }

    /**
     * 设置入群审批模式
     **/
    public void setJoinAuthMode(BMXGroup group, BMXGroup.JoinAuthMode mode, BMXCallBack callBack) {
        mService.setJoinAuthMode(group, mode, callBack);
    }

    /**
     * 设置邀请模式
     **/
    public void setInviteMode(BMXGroup group, BMXGroup.InviteMode mode, BMXCallBack callBack) {
        mService.setInviteMode(group, mode, callBack);
    }

    /**
     * 设置群头像
     **/
    public void setAvatar(BMXGroup group, String avatarPath,
            FileProgressListener listener, BMXCallBack callBack) {
        mService.setAvatar(group, avatarPath, listener, callBack);
    }

    /**
     * 下载群头像
     */
    public void downloadAvatar(BMXGroup group, FileProgressListener listener, BMXCallBack callBack) {
        mService.downloadAvatar(group, listener, callBack);
    }

    /**
     * 添加群组变化监听者
     **/
    public void addGroupListener(BMXGroupServiceListener listener) {
        mService.addGroupListener(listener);
    }

    /**
     * 移除群组变化监听者
     **/
    public void removeGroupListener(BMXGroupServiceListener listener) {
        mService.removeGroupListener(listener);
    }

    /**
     * 是否群聊创建者
     **/
    public boolean isGroupOwner(long ownerId) {
        long userId = SharePreferenceUtils.getInstance().getUserId();
        return userId == ownerId;
    }

    /**
     * 设置群已读开关
     */
    public void setEnableReadAck(BMXGroup group, boolean enable, BMXCallBack callBack) {
        mService.setEnableReadAck(group, enable, callBack);
    }
}
