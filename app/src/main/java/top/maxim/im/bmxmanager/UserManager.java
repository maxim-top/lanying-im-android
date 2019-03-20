
package top.maxim.im.bmxmanager;

import im.floo.floolib.BMXConnectStatus;
import im.floo.floolib.BMXDeviceList;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXSignInStatus;
import im.floo.floolib.BMXUserProfile;
import im.floo.floolib.BMXUserService;
import im.floo.floolib.BMXUserServiceListener;
import im.floo.floolib.SWIGTYPE_p_std__functionT_void_fintF_t;

/**
 * Description : 用户 Created by Mango on 2018/12/2.
 */
public class UserManager extends BaseManager {

    private static final String TAG = UserManager.class.getSimpleName();

    private static final UserManager sInstance = new UserManager();

    private BMXUserService mService;

    public static UserManager getInstance() {
        return sInstance;
    }

    private UserManager() {
        mService = bmxClient.getUserService();
    }

    /**
     * 注册
     * 
     * @param mobile 手机号
     * @param verifyCode 验证码
     * @param password 密码
     * @param username 用户名
     * @return BMXUserProfile
     */
    public BMXErrorCode signUpNewUser(String mobile, String verifyCode, String password,
            String username, BMXUserProfile profile) {
        return mService.signUpNewUser(mobile, verifyCode, password, username, profile);
    }

    /**
     * 用户名登陆
     * 
     * @param name
     * @param password
     * @return
     */
    public BMXErrorCode signInByName(String name, String password) {
        return mService.signInByName(name, password);
    }

    /**
     * 手机号登陆
     * 
     * @param phone
     * @param password
     * @return
     */
    public BMXErrorCode signInByPhone(String phone, String password) {
        return mService.signInByPhone(phone, password);
    }

    /**
     * id 登陆
     * 
     * @param id
     * @param password
     * @return
     */
    public BMXErrorCode signInById(long id, String password) {
        return mService.signInById(id, password);
    }

    /**
     * 自动登陆 根据用户名
     * 
     * @param name
     * @param password
     * @return
     */
    public BMXErrorCode autoSignInByName(String name, String password) {
        return mService.autoSignInByName(name, password);
    }

    /**
     * 自动登陆 根据id
     * 
     * @param uid
     * @param password
     * @return
     */
    public BMXErrorCode autoSignInById(long uid, String password) {
        return mService.autoSignInById(uid, password);
    }

    /**
     * 退出登录
     * 
     * @return
     */
    public BMXErrorCode signOut() {
        return mService.signOut();
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
    public BMXErrorCode bindDevice(String token) {
        return mService.bindDevice(token);
    }

    /**
     * 获取登陆的设备
     */
    public BMXErrorCode getDeviceList(BMXDeviceList deviceList) {
        return mService.getDeviceList(deviceList);
    }

    /**
     * 删除设备
     */
    public BMXErrorCode deleteDevice(int device_sn) {
        return mService.deleteDevice(device_sn);
    }

    /**
     * 获取用户详情
     **/
    public BMXErrorCode getProfile(BMXUserProfile profile, boolean forceRefresh) {
        return mService.getProfile(profile, forceRefresh);
    }

    /**
     * 设置昵称
     **/
    public BMXErrorCode setNickname(String nickname) {
        return mService.setNickname(nickname);
    }

    /**
     * 上传头像
     **/
    public BMXErrorCode uploadAvatar(String avatarPath) {
        return mService.uploadAvatar(avatarPath, new SWIGTYPE_p_std__functionT_void_fintF_t());
    }

    /**
     * 下载头像
     */
    public BMXErrorCode downloadAvatar(BMXUserProfile profile) {
        return mService.downloadAvatar(profile, false, new SWIGTYPE_p_std__functionT_void_fintF_t());
    }

    /**
     * 设置电话号码
     **/
    public BMXErrorCode setMobilePhone(String phone) {
        return mService.setMobilePhone(phone);
    }

    /**
     * 设置公开扩展信息
     **/
    public BMXErrorCode setPublicInfo(String publicInfo) {
        return mService.setPublicInfo(publicInfo);
    }

    /**
     * 设置私有扩展信息
     **/
    public BMXErrorCode setPrivateInfo(String privateInfo) {
        return mService.setPrivateInfo(privateInfo);
    }

    /**
     * 设置加好友验证方式
     **/
    public BMXErrorCode setAddFriendAuthMode(BMXUserProfile.AddFriendAuthMode mode) {
        return mService.setAddFriendAuthMode(mode);
    }

    /**
     * 设置加好友验证问题
     **/
    public BMXErrorCode setAuthQuestion(BMXUserProfile.AuthQuestion authQuestion) {
        return mService.setAuthQuestion(authQuestion);
    }

    /**
     * 设置是否允许推送
     **/
    public BMXErrorCode setEnablePush(boolean enable) {
        return mService.setEnablePush(enable);
    }

    /**
     * 设置是否推送详情
     **/
    public BMXErrorCode setEnablePushDetaile(boolean enable) {
        return mService.setEnablePushDetaile(enable);
    }

    /**
     * 设置推送昵称
     **/
    public BMXErrorCode setPushNickname(String nickname) {
        return mService.setPushNickname(nickname);
    }

    /**
     * 设置收到新消息是否声音提醒
     **/
    public BMXErrorCode setNotificationSound(boolean enable) {
        return mService.setNotificationSound(enable);
    }

    /**
     * 设置收到新消息是否震动
     **/
    public BMXErrorCode setNotificationVibrate(boolean enable) {
        return mService.setNotificationVibrate(enable);
    }

    /**
     * 设置是否自动缩略图和语音附件
     **/
    public BMXErrorCode setAutoDownloadAttachment(boolean enable) {
        return mService.setAutoDownloadAttachment(enable);
    }

    /**
     * 是否设置自动接受群邀请
     */
    public BMXErrorCode setAutoAcceptGroupInvite(boolean enable) {
        return mService.setAutoAcceptGroupInvite(enable);
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
}
