
package top.maxim.im.bmxmanager;

import im.floo.BMXCallBack;
import im.floo.floolib.BMXMessageList;
import im.floo.floolib.BMXPushManager;
import im.floo.floolib.BMXPushService;
import im.floo.floolib.BMXPushServiceListener;
import im.floo.floolib.TagList;

/**
 * Description : push Created by Mango on 2020/09/07.
 */
public class PushManager extends BaseManager {

    private static final String TAG = PushManager.class.getSimpleName();

    private static final PushManager sInstance = new PushManager();

    private BMXPushManager mService;

    public static PushManager getInstance() {
        return sInstance;
    }

    private PushManager() {
        mService = bmxClient.getPushManager();
    }

    public void start(String alias, String bmxToken, BMXCallBack callBack) {
        mService.start(alias, bmxToken, callBack);
    }

    public void start(String alias, BMXCallBack callBack) {
        mService.start(alias, callBack);
    }

    public void start(BMXCallBack callBack) {
        mService.start(callBack);
    }

    public void stop(BMXCallBack callBack) {
        mService.stop(callBack);
    }

    public void resume(BMXCallBack callBack) {
        mService.resume(callBack);
    }

    public void unbindAlias(String alias, BMXCallBack callBack) {
        mService.unbindAlias(alias, callBack);
    }

    public String getToken() {
        return mService.getToken();
    }

    public String getCert() {
        return mService.getCert();
    }

    public BMXPushService.PushSdkStatus status() {
        return mService.status();
    }

    public void bindDeviceToken(String token, BMXCallBack callBack) {
        mService.bindDeviceToken(token, callBack);
    }

    public void setTags(TagList tags, String operationId, BMXCallBack callBack) {
        mService.setTags(tags, operationId, callBack);
    }

    public void getTags(TagList tags, String operationId, BMXCallBack callBack) {
        mService.getTags(tags, operationId, callBack);
    }

    public void deleteTags(TagList tags, String operationId, BMXCallBack callBack) {
        mService.deleteTags(tags, operationId, callBack);
    }

    public void clearTags(String operationId, BMXCallBack callBack) {
        mService.clearTags(operationId, callBack);
    }

    public void setBadge(int count, BMXCallBack callBack) {
        mService.setBadge(count, callBack);
    }

    public void setPushMode(boolean enable, BMXCallBack callBack) {
        mService.setPushMode(enable, callBack);
    }

    public void setPushMode(BMXCallBack callBack) {
        mService.setPushMode(callBack);
    }

    public void setPushTime(int startHour, int endHour, BMXCallBack callBack) {
        mService.setPushTime(startHour, endHour, callBack);
    }

    public void setSilenceTime(int startHour, int endHour, BMXCallBack callBack) {
        mService.setSilenceTime(startHour, endHour, callBack);
    }

    public void setRunBackgroundMode(boolean enable, BMXCallBack callBack) {
        mService.setRunBackgroundMode(enable, callBack);
    }

    public void setRunBackgroundMode(BMXCallBack callBack) {
        mService.setRunBackgroundMode(callBack);
    }

    public void setGeoFenceMode(boolean enable, boolean isAllow, BMXCallBack callBack) {
        mService.setGeoFenceMode(enable, isAllow, callBack);
    }

    public void setGeoFenceMode(boolean enable, BMXCallBack callBack) {
        mService.setGeoFenceMode(enable, callBack);
    }

    public void setGeoFenceMode(BMXCallBack callBack) {
        mService.setGeoFenceMode(callBack);
    }

    public void clearNotification(long notificationId) {
        mService.clearNotification(notificationId);
    }

    public void clearAllNotifications() {
        mService.clearAllNotifications();
    }

    public void sendMessage(String content) {
        mService.sendMessage(content);
    }

    public void loadLocalPushMessages(long refMsgId, long size, BMXMessageList result,
            BMXPushService.PushDirection arg3, BMXCallBack callBack) {
        mService.loadLocalPushMessages(refMsgId, size, result, arg3, callBack);
    }

    public void loadLocalPushMessages(long refMsgId, long size, BMXMessageList result,
            BMXCallBack callBack) {
        mService.loadLocalPushMessages(refMsgId, size, result, callBack);
    }

    /**
     * 添加监听者
     **/
    public void addPushListener(BMXPushServiceListener listener) {
        mService.addPushListener(listener);
    }

    /**
     * 移除监听者
     **/
    public void removePushListener(BMXPushServiceListener listener) {
        mService.removePushListener(listener);
    }
}
