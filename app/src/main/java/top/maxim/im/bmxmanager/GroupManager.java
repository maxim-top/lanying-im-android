
package top.maxim.im.bmxmanager;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupAnnouncementList;
import im.floo.floolib.BMXGroupBannedMemberList;
import im.floo.floolib.BMXGroupList;
import im.floo.floolib.BMXGroupMemberList;
import im.floo.floolib.BMXGroupService;
import im.floo.floolib.BMXGroupServiceListener;
import im.floo.floolib.BMXGroupSharedFileList;
import im.floo.floolib.GroupApplicationPage;
import im.floo.floolib.GroupInvitaionPage;
import im.floo.floolib.ListOfLongLong;
import im.floo.floolib.SWIGTYPE_p_std__functionT_void_fintF_t;
import top.maxim.im.common.utils.SharePreferenceUtils;

/**
 * Description : group Created by Mango on 2018/12/2.
 */
public class GroupManager extends BaseManager {

    private static final String TAG = GroupManager.class.getSimpleName();

    private static final GroupManager sInstance = new GroupManager();

    private BMXGroupService mService;

    public static GroupManager getInstance() {
        return sInstance;
    }

    private GroupManager() {
        mService = bmxClient.getGroupService();
    }

    /**
     * 获取群组列表，如果设置了forceRefresh则从服务器拉取
     **/
    public BMXErrorCode search(BMXGroupList list, boolean forceRefresh) {
        return mService.search(list, forceRefresh);
    }

    /**
     * 获取群信息
     **/
    public BMXErrorCode search(ListOfLongLong groupIdList, BMXGroupList list,
            boolean forceRefresh) {
        return mService.search(groupIdList, list, forceRefresh);
    }

    /**
     * 获取群信息
     **/
    public BMXErrorCode search(long groupId, BMXGroup group, boolean forceUpdate) {
        return mService.search(groupId, group, forceUpdate);
    }

    /**
     * 群聊邀请
     */
    public BMXErrorCode getInvitationList(GroupInvitaionPage result, String cursor, int pageSize) {
        return mService.getInvitationList(result, cursor, pageSize);
    }

    /**
     * 入群通知
     */
    public BMXErrorCode getApplicationList(BMXGroupList list, GroupApplicationPage result, String cursor, int pageSize) {
        return mService.getApplicationList(list, result, cursor, pageSize);
    }

    /**
     * 创建群
     **/
    public BMXErrorCode create(BMXGroupService.CreateGroupOptions options, BMXGroup group) {
        return mService.create(options, group);
    }

    /**
     * 销毁群
     **/
    public BMXErrorCode destroy(BMXGroup group) {
        return mService.destroy(group);
    }

    /**
     * 加入一个群，根据群设置可能需要管理员批准
     **/
    public BMXErrorCode join(BMXGroup group, String message) {
        return mService.join(group, message);
    }

    /**
     * 退出群
     **/
    public BMXErrorCode leave(BMXGroup group) {
        return mService.leave(group);
    }

    /**
     * 获取群详情，从服务端拉取最新信息
     **/
    public BMXErrorCode getInfo(BMXGroup group) {
        return mService.getInfo(group);
    }

    /**
     * 获取群成员列表，如果设置了forceRefresh则从服务器拉取
     **/
    public BMXErrorCode getMembers(BMXGroup group, BMXGroupMemberList list, boolean forceRefresh) {
        return mService.getMembers(group, list, forceRefresh);
    }

    /**
     * 添加群成员
     **/
    public BMXErrorCode addMembers(BMXGroup group, ListOfLongLong listOfLongLong, String message) {
        return mService.addMembers(group, listOfLongLong, message);
    }

    /**
     * 删除群成员
     **/
    public BMXErrorCode removeMembers(BMXGroup group, ListOfLongLong listOfLongLong,
            String reason) {
        return mService.removeMembers(group, listOfLongLong, reason);
    }

    /**
     * 添加管理员
     **/
    public BMXErrorCode addAdmins(BMXGroup group, ListOfLongLong listOfLongLong, String message) {
        return mService.addAdmins(group, listOfLongLong, message);
    }

    /**
     * 删除管理员
     **/
    public BMXErrorCode removeAdmins(BMXGroup group, ListOfLongLong listOfLongLong, String reason) {
        return mService.removeAdmins(group, listOfLongLong, reason);
    }

    /**
     * 获取Admins列表，如果设置了forceRefresh则从服务器拉取
     **/
    public BMXErrorCode getAdmins(BMXGroup group, BMXGroupMemberList list, boolean forceRefresh) {
        return mService.getAdmins(group, list, forceRefresh);
    }

    /**
     * 添加黑名单
     **/
    public BMXErrorCode blockMembers(BMXGroup group, ListOfLongLong listOfLongLong) {
        return mService.blockMembers(group, listOfLongLong);
    }

    /**
     * 从黑名单删除
     **/
    public BMXErrorCode unblockMembers(BMXGroup group, ListOfLongLong listOfLongLong) {
        return mService.unblockMembers(group, listOfLongLong);
    }

    /**
     * 获取黑名单
     **/
    public BMXErrorCode getBlockList(BMXGroup group, BMXGroupMemberList list,
            boolean forceRefresh) {
        return mService.getBlockList(group, list, forceRefresh);
    }

    /**
     * 禁言
     **/
    public BMXErrorCode banMembers(BMXGroup group, ListOfLongLong listOfLongLong, long duration,
            String reason) {
        return mService.banMembers(group, listOfLongLong, duration, reason);
    }

    /**
     * 解除禁言
     **/
    public BMXErrorCode unbanMembers(BMXGroup group, ListOfLongLong listOfLongLong) {
        return mService.unbanMembers(group, listOfLongLong);
    }

    /**
     * 获取禁言列表
     **/
    public BMXErrorCode getBannedMembers(BMXGroup group, BMXGroupBannedMemberList list) {
        return mService.getBannedMembers(group, list);
    }

    /**
     * 屏蔽群消息开关
     **/
    public BMXErrorCode muteMessage(BMXGroup group, BMXGroup.MsgMuteMode mode) {
        return mService.muteMessage(group, mode);
    }

    /**
     * 接受入群申请
     **/
    public BMXErrorCode acceptApplication(BMXGroup group, long applicantId) {
        return mService.acceptApplication(group, applicantId);
    }

    /**
     * 拒绝入群申请
     **/
    public BMXErrorCode declineApplication(BMXGroup group, long applicantId, String reason) {
        return mService.declineApplication(group, applicantId, reason);
    }

    /**
     * 接受入群邀请
     **/
    public BMXErrorCode acceptInvitation(BMXGroup group, long inviter) {
        return mService.acceptInvitation(group, inviter);
    }

    /**
     * 拒绝入群邀请
     **/
    public BMXErrorCode declineInvitation(BMXGroup group, long inviter) {
        return mService.declineInvitation(group, inviter);
    }

    /**
     * 转移群主
     **/
    public BMXErrorCode transferOwner(BMXGroup group, long newOwnerId) {
        return mService.transferOwner(group, newOwnerId);
    }

    /**
     * 添加群共享文件
     **/
    public BMXErrorCode uploadSharedFile(BMXGroup group, String filePath, String displayName,
            String extensionName) {
        return mService.uploadSharedFile(group, filePath, displayName, extensionName,
                new SWIGTYPE_p_std__functionT_void_fintF_t());
    }

    /**
     * 移除群共享文件
     **/
    public BMXErrorCode removeSharedFile(BMXGroup group, BMXGroup.SharedFile sharedFile) {
        return mService.removeSharedFile(group, sharedFile);
    }

    /**
     * 下载群共享文件
     **/
    public BMXErrorCode downloadSharedFile(BMXGroup group, BMXGroup.SharedFile sharedFile) {
        return mService.downloadSharedFile(group, sharedFile,
                new SWIGTYPE_p_std__functionT_void_fintF_t());
    }

    /**
     * 获取群共享文件列表
     **/
    public BMXErrorCode getSharedFilesList(BMXGroup group, BMXGroupSharedFileList list,
            boolean forceRefresh) {
        return mService.getSharedFilesList(group, list, forceRefresh);
    }

    /**
     * 修改群文件名称
     */
    public BMXErrorCode changeSharedFileName(BMXGroup group, BMXGroup.SharedFile sharedFile,
            String name) {
        return mService.changeSharedFileName(group, sharedFile, name);
    }

    public BMXErrorCode getLatestAnnouncement(BMXGroup group, BMXGroup.Announcement announcement,
            boolean forceRefresh) {
        return mService.getLatestAnnouncement(group, announcement, forceRefresh);
    }

    public BMXErrorCode getAnnouncementList(BMXGroup group, BMXGroupAnnouncementList list, boolean forceRefresh) {
        return mService.getAnnouncementList(group, list, forceRefresh);
    }

    public BMXErrorCode editAnnouncement(BMXGroup group, String title, String content) {
        return mService.editAnnouncement(group, title, content);
    }

    public BMXErrorCode deleteAnnouncement(BMXGroup group, long announcementId) {
        return mService.deleteAnnouncement(group, announcementId);
    }

    /**
     * 设置群名称
     **/
    public BMXErrorCode setName(BMXGroup group, String name) {
        return mService.setName(group, name);
    }

    /**
     * 设置群描述信息
     **/
    public BMXErrorCode setDescription(BMXGroup group, String description) {
        return mService.setDescription(group, description);
    }

    /**
     * 设置群扩展信息
     **/
    public BMXErrorCode setExtension(BMXGroup group, String extension) {
        return mService.setExtension(group, extension);
    }

    /**
     * 设置在群里的昵称
     **/
    public BMXErrorCode setMyNickname(BMXGroup group, String nickname) {
        return mService.setMyNickname(group, nickname);
    }

    /**
     * 设置群消息通知模式
     **/
    public BMXErrorCode setMsgPushMode(BMXGroup group, BMXGroup.MsgPushMode mode) {
        return mService.setMsgPushMode(group, mode);
    }

    /**
     * 设置入群审批模式
     **/
    public BMXErrorCode setJoinAuthMode(BMXGroup group, BMXGroup.JoinAuthMode mode) {
        return mService.setJoinAuthMode(group, mode);
    }

    /**
     * 设置邀请模式
     **/
    public BMXErrorCode setInviteMode(BMXGroup group, BMXGroup.InviteMode mode) {
        return mService.setInviteMode(group, mode);
    }

    /**
     * 设置群头像
     **/
    public BMXErrorCode setAvatar(BMXGroup group, String avatarPath) {
        return mService.setAvatar(group, avatarPath, new SWIGTYPE_p_std__functionT_void_fintF_t());
    }

    /**
     * 下载群头像
     */
    public BMXErrorCode downloadAvatar(BMXGroup group) {
        return mService.downloadAvatar(group, false, new SWIGTYPE_p_std__functionT_void_fintF_t());
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
}
