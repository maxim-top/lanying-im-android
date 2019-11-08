
package top.maxim.im.message.utils;

import java.util.List;

public class RefreshChatActivityEvent {

    /* 新增消息 */
    public final static int TYPE_ADD = 1;

    /* 更新消息 */
    public final static int TYPE_UPDATE = 2;

    /* 删除消息 */
    public final static int TYPE_DELETE = 3;

    /**
     * 刷新聊天数据类型
     */
    private int refreshType;

    private List<String> msgBeans;

    public int getRefreshType() {
        return refreshType;
    }

    public void setRefreshType(int refreshType) {
        this.refreshType = refreshType;
    }

    public List<String> getMsgBeans() {
        return msgBeans;
    }

    public void setMsgBeans(List<String> msgBeans) {
        this.msgBeans = msgBeans;
    }

}
