
package top.maxim.im.common.bean;

/**
 */
public class UserBean extends BaseBean {

    private String userName;

    private long userId;

    private String userPwd;

    private long timestamp;

    public UserBean(String userName, long userId, String userPwd, long timestamp) {
        this.userName = userName;
        this.userId = userId;
        this.userPwd = userPwd;
        this.timestamp = timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
