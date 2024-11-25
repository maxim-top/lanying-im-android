
package top.maxim.im.common.bean;

import im.floo.floolib.BMXConnectStatus;
import im.floo.floolib.BMXConversation;

/**
 */
public class TargetBean extends BaseBean {

    private Long id;

    private BMXConversation.Type type;

    public TargetBean(Long id, BMXConversation.Type type) {
        this.id = id;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BMXConversation.Type getType() {
        return type;
    }

    public void setType(BMXConversation.Type type) {
        this.type = type;
    }
}
