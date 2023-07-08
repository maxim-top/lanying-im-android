
package top.maxim.im.bmxmanager;

import im.floo.BMXCallBack;
import im.floo.BMXDataCallBack;
import im.floo.floolib.BMXConnectStatus;
import im.floo.floolib.BMXDeviceList;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXSignInStatus;
import im.floo.floolib.BMXUserManager;
import im.floo.floolib.BMXUserProfile;
import im.floo.floolib.BMXUserService;
import im.floo.floolib.BMXUserServiceListener;
import im.floo.floolib.FileProgressListener;

/**
 * Description : 用户 Created by Mango on 2018/12/2.
 */
public class UserManager extends BaseManager {

    private static final String TAG = UserManager.class.getSimpleName();

    private static final UserManager sInstance = new UserManager();

    private BMXUserManager mService;

    private BMXUserService mUserService;

    public static UserManager getInstance() {
        return sInstance;
    }

    private UserManager() {
        if (bmxClient == null){
            initBMXSDK();
        }
        mService = bmxClient.getUserManager();
        mUserService = bmxClient.getUserService();
    }

    /**
     * 注册
     *
     * @param password 密码
     * @param username 用户名
     */
    public void signUpNewUser(String username, String password, BMXDataCallBack<BMXUserProfile> callBack) {
        mService.signUpNewUser(username, password, callBack);
    }

    /**
     * 用户名登陆
     * 
     * @param name
     * @param password
     */
    public void signInByName(String name, String password, BMXCallBack callBack) {
        mService.signInByName(name, password, callBack);
    }

    /**
     * id 登陆
     * 
     * @param id
     * @param password
     */
    public void signInById(long id, String password, BMXCallBack callBack) {
        mService.signInById(id, password, callBack);
    }

    /**
     * 自动登陆 根据用户名
     *
     * @param name
     * @param password
     */
    public void autoSignInByName(String name, String password, BMXCallBack callBack) {
        mService.autoSignInByName(name, password, callBack);
    }

    /**
     * 自动登陆 根据id
     *
     * @param uid
     * @param password
     */
    public void autoSignInById(long uid, String password, BMXCallBack callBack) {
        mService.autoSignInById(uid, password, callBack);
    }

    /**
     * 退出登录
     * 
     * @return
     */
    public void signOut(BMXCallBack callBack) {
        mService.signOut(callBack);
    }

    /**
     * 退出登录
     *
     * @return
     */
    public void signOut(long userId, BMXCallBack callBack) {
        mService.signOut(userId, callBack);
    }

    /**
     * 获取当前和服务器的连接状态
     **/
    public BMXConnectStatus connectStatus() {
        return mService.connectStatus();
    }

    /**
     * 获取当前的登录状态
     **/
    public BMXSignInStatus signInStatus() {
        return mService.signInStatus();
    }

    /**
     * 绑定设备推送token
     **/
    public void bindDevice(String token, BMXCallBack callBack) {
        mService.bindDevice(token, callBack);
    }

    /**
     * 获取登陆的设备
     */
    public void getDeviceList(BMXDataCallBack<BMXDeviceList> callBack) {
        mService.getDeviceList(callBack);
    }

    /**
     * 删除设备
     */
    public void deleteDevice(int device_sn, BMXCallBack callBack) {
        mService.deleteDevice(device_sn, callBack);
    }

    /**
     * 获取用户详情
     **/
    public void getProfile(boolean forceRefresh, BMXDataCallBack<BMXUserProfile> callBack) {
        mService.getProfile(forceRefresh, callBack);
    }

    /**
     * 获取用户详情
     **/
    public BMXUserProfile getProfileByDB() {
        BMXUserProfile profile = new BMXUserProfile();
        BMXErrorCode error = mUserService.getProfile(profile, false);
        if (error == null || error.swigValue() != BMXErrorCode.NoError.swigValue()) {
            return null;
        }
        return profile;
    }

    /**
     * 设置昵称
     **/
    public void setNickname(String nickname, BMXCallBack callBack) {
        mService.setNickname(nickname, callBack);
    }

    /**
     * 上传头像
     **/
    public void uploadAvatar(String avatarPath, FileProgressListener listener, BMXCallBack callBack) {
        mService.uploadAvatar(avatarPath, listener, callBack);
    }

    /**
     * 下载头像
     */
    public void downloadAvatar(BMXUserProfile profile, FileProgressListener listener, BMXCallBack callBack) {
        mService.downloadAvatar(profile, listener, callBack);
    }

    /**
     * 设置公开扩展信息
     **/
    public void setPublicInfo(String publicInfo, BMXCallBack callBack) {
        mService.setPublicInfo(publicInfo, callBack);
    }

    /**
     * 设置私有扩展信息
     **/
    public void setPrivateInfo(String privateInfo, BMXCallBack callBack) {
        mService.setPrivateInfo(privateInfo, callBack);
    }

    /**
     * 设置加好友验证方式
     **/
    public void setAddFriendAuthMode(BMXUserProfile.AddFriendAuthMode mode, BMXCallBack callBack) {
        mService.setAddFriendAuthMode(mode, callBack);
    }

    /**
     * 设置加好友验证问题
     **/
    public void setAuthQuestion(BMXUserProfile.AuthQuestion authQuestion, BMXCallBack callBack) {
        mService.setAuthQuestion(authQuestion, callBack);
    }

    /**
     * 设置是否允许推送
     **/
    public void setEnablePush(boolean enable, BMXCallBack callBack) {
        mService.setEnablePush(enable, callBack);
    }

    /**
     * 设置是否推送详情
     **/
    public void setEnablePushDetaile(boolean enable, BMXCallBack callBack) {
        mService.setEnablePushDetaile(enable, callBack);
    }

    /**
     * 设置推送昵称
     **/
    public void setPushNickname(String nickname, BMXCallBack callBack) {
        mService.setPushNickname(nickname, callBack);
    }

    /**
     * 设置收到新消息是否声音提醒
     **/
    public void setNotificationSound(boolean enable, BMXCallBack callBack) {
        mService.setNotificationSound(enable, callBack);
    }

    /**
     * 设置收到新消息是否震动
     **/
    public void setNotificationVibrate(boolean enable, BMXCallBack callBack) {
        mService.setNotificationVibrate(enable, callBack);
    }

    /**
     * 设置是否自动缩略图和语音附件
     **/
    public void setAutoDownloadAttachment(boolean enable, BMXCallBack callBack) {
        mService.setAutoDownloadAttachment(enable, callBack);
    }

    /**
     * 是否设置自动接受群邀请
     */
    public void setAutoAcceptGroupInvite(boolean enable, BMXCallBack callBack) {
        mService.setAutoAcceptGroupInvite(enable, callBack);
    }

    /**
     * 添加用户状态监听者
     **/
    public void addUserListener(BMXUserServiceListener listener) {
        mService.addUserListener(listener);
    }

    /**
     * 移除用户状态监听者
     **/
    public void removeUserListener(BMXUserServiceListener listener) {
        mService.removeUserListener(listener);
    }

    /**
     * 切换appId
     * @param appId appId
     */
    public void changeAppId(String appId, BMXCallBack callBack) {
        mService.changeAppId(appId, callBack);
    }
}
