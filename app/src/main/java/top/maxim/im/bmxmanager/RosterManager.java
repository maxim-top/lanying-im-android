
package top.maxim.im.bmxmanager;

import im.floo.BMXCallBack;
import im.floo.BMXDataCallBack;
import im.floo.floolib.ApplicationPage;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXRosterItemList;
import im.floo.floolib.BMXRosterManager;
import im.floo.floolib.BMXRosterService;
import im.floo.floolib.BMXRosterServiceListener;
import im.floo.floolib.FileProgressListener;
import im.floo.floolib.ListOfLongLong;

/**
 * Description : roster Created by Mango on 2018/12/2.
 */
public class RosterManager extends BaseManager {

    private static final String TAG = RosterManager.class.getSimpleName();

    private static final RosterManager sInstance = new RosterManager();

    private BMXRosterManager mService;

    private BMXRosterService mRosterService;

    public static RosterManager getInstance() {
        return sInstance;
    }

    private RosterManager() {
        if (bmxClient == null){
            initBMXSDK();
        }
        mService = bmxClient.getRosterManager();
        mRosterService = bmxClient.getRosterService();
    }

    /**
     * 获取好友列表，如果forceRefresh == true，则强制从服务端拉取
     **/
    public void get(boolean forceRefresh, BMXDataCallBack<ListOfLongLong> callBack) {
        mService.get(forceRefresh, callBack);
    }

    /**
     * 搜索用户
     **/
    public void getRosterList(long rosterId, boolean forceRefresh, BMXDataCallBack<BMXRosterItem> callBack) {
        mService.search(rosterId, forceRefresh, callBack);
    }

    /**
     * 搜索用户
     **/
    public BMXRosterItem getRosterListByDB(long rosterId) {
        BMXRosterItem item = new BMXRosterItem();
        BMXErrorCode error = mRosterService.search(rosterId, false, item);
        if (error == null || error.swigValue() != BMXErrorCode.NoError.swigValue()) {
            return null;
        }
        return item;
    }

    /**
     * 搜索用户
     **/
    public void getRosterList(String name, boolean forceRefresh, BMXDataCallBack<BMXRosterItem> callBack) {
        mService.search(name, forceRefresh, callBack);
    }

    /**
     * 搜索用户
     **/
    public void getRosterList(ListOfLongLong rosterIdList, boolean forceRefresh,
                              BMXDataCallBack<BMXRosterItemList> callBack) {
        mService.search(rosterIdList, forceRefresh, callBack);
    }

    /**
     * 更新好友扩展信息
     **/
    public void setItemExtension(BMXRosterItem item, String extension, BMXCallBack callBack) {
        mService.setItemExtension(item, extension, callBack);
    }

    /**
     * 设置昵称
     * 
     * @param item
     * @param alias
     */
    public void setItemAlias(BMXRosterItem item, String alias, BMXCallBack callBack) {
        mService.setItemAlias(item, alias, callBack);
    }

    /**
     * 设置免打扰
     * @param item
     * @param status
     */
    public void setItemMuteNotification(BMXRosterItem item, boolean status, BMXCallBack callBack) {
        mService.setItemMuteNotification(item, status, callBack);
    }

    /**
     * 添加好友
     **/
    public void apply(long rosterId, String reason, BMXCallBack callBack) {
        mService.apply(rosterId, reason, callBack);
    }

    /**
     * 删除好友
     **/
    public void remove(long rosterId, BMXCallBack callBack) {
        mService.remove(rosterId, callBack);
    }

    /**
     * 获取好友申请
     */
    public void getApplicationList(String cursor, int pageSize,
            BMXDataCallBack<ApplicationPage> callBack) {
        mService.getApplicationList(cursor, pageSize, callBack);
    }

    /**
     * 接受加好友申请
     **/
    public void accept(long rosterId, BMXCallBack callBack) {
        mService.accept(rosterId, callBack);
    }

    /**
     * 拒绝加好友申请
     **/
    public void decline(long rosterId, String reason, BMXCallBack callBack) {
        mService.decline(rosterId, reason, callBack);
    }

    /**
     * 加入黑名单
     **/
    public void block(long rosterId, BMXCallBack callBack) {
        mService.block(rosterId, callBack);
    }

    /**
     * 从黑名单移除
     **/
    public void unblock(long rosterId, BMXCallBack callBack) {
        mService.unblock(rosterId, callBack);
    }

    /**
     * 获取黑名单，如果forceRefresh == true，则强制从服务端拉取
     **/
    public void getBlockList(boolean forceRefresh, BMXDataCallBack<ListOfLongLong> callBack) {
        mService.getBlockList(forceRefresh, callBack);
    }

    /**
     * 下载头像
     * 
     * @param item
     */
    public void downloadAvatar(BMXRosterItem item, FileProgressListener listener, BMXCallBack callBack) {
        mService.downloadAvatar(item, listener, callBack);
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
