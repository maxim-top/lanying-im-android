
package top.maxim.im.bmxmanager;

import im.floo.floolib.ApplicationPage;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXRosterItemList;
import im.floo.floolib.BMXRosterService;
import im.floo.floolib.BMXRosterServiceListener;
import im.floo.floolib.ListOfLongLong;
import im.floo.floolib.SWIGTYPE_p_std__functionT_void_fintF_t;

/**
 * Description : roster Created by Mango on 2018/12/2.
 */
public class RosterManager extends BaseManager {

    private static final String TAG = RosterManager.class.getSimpleName();

    private static final RosterManager sInstance = new RosterManager();

    private BMXRosterService mService;

    public static RosterManager getInstance() {
        return sInstance;
    }

    private RosterManager() {
        mService = bmxClient.getRosterService();
    }

    /**
     * 获取好友列表，如果forceRefresh == true，则强制从服务端拉取
     **/
    public BMXErrorCode get(ListOfLongLong listOfLongLong, boolean forceRefresh) {
        return mService.get(listOfLongLong, forceRefresh);
    }

    /**
     * 搜索用户
     **/
    public BMXErrorCode search(long rosterId, boolean forceRefresh, BMXRosterItem item) {
        return mService.search(rosterId, forceRefresh, item);
    }

    /**
     * 搜索用户
     **/
    public BMXErrorCode search(String name, boolean forceRefresh, BMXRosterItem item) {
        return mService.search(name, forceRefresh, item);
    }

    /**
     * 搜索用户
     **/
    public BMXErrorCode search(ListOfLongLong rosterIdList, BMXRosterItemList list,
            boolean forceRefresh) {
        return mService.search(rosterIdList, list, forceRefresh);
    }

    /**
     * 更新好友扩展信息
     **/
    public BMXErrorCode setItemExtension(BMXRosterItem item, String extension) {
        return mService.setItemExtension(item, extension);
    }

    /**
     * 设置昵称
     * 
     * @param item
     * @param alias
     * @return
     */
    public BMXErrorCode setItemAlias(BMXRosterItem item, String alias) {
        return mService.setItemAlias(item, alias);
    }

    /**
     * 设置免打扰
     * @param item
     * @param status
     * @return
     */
    public BMXErrorCode setItemMuteNotification(BMXRosterItem item, boolean status) {
        return mService.setItemMuteNotification(item, status);
    }

    /**
     * 添加好友
     **/
    public BMXErrorCode apply(long rosterId, String reason) {
        return mService.apply(rosterId, reason);
    }

    /**
     * 删除好友
     **/
    public BMXErrorCode remove(long rosterId) {
        return mService.remove(rosterId);
    }

    /**
     * 获取好友申请
     */
    public BMXErrorCode getApplicationList(ApplicationPage result, String cursor, int pageSize) {
        return mService.getApplicationList(result, cursor, pageSize);
    }

    /**
     * 接受加好友申请
     **/
    public BMXErrorCode accept(long rosterId) {
        return mService.accept(rosterId);
    }

    /**
     * 拒绝加好友申请
     **/
    public BMXErrorCode decline(long rosterId, String reason) {
        return mService.decline(rosterId, reason);
    }

    /**
     * 加入黑名单
     **/
    public BMXErrorCode block(long rosterId) {
        return mService.block(rosterId);
    }

    /**
     * 从黑名单移除
     **/
    public BMXErrorCode unblock(long rosterId) {
        return mService.unblock(rosterId);
    }

    /**
     * 获取黑名单，如果forceRefresh == true，则强制从服务端拉取
     **/
    public BMXErrorCode getBlockList(ListOfLongLong listOfLongLong, boolean forceRefresh) {
        return mService.getBlockList(listOfLongLong, forceRefresh);
    }

    /**
     * 下载头像
     * 
     * @param item
     */
    public BMXErrorCode downloadAvatar(BMXRosterItem item) {
        return mService.downloadAvatar(item, false, new SWIGTYPE_p_std__functionT_void_fintF_t());
    }

    /**
     * 添加好友变化监听者
     **/
    public void addRosterListener(BMXRosterServiceListener listener) {
        mService.addRosterListener(listener);
    }

    /**
     * 移除好友变化监听者
     **/
    public void removeRosterListener(BMXRosterServiceListener listener) {
        mService.removeRosterListener(listener);
    }
}
