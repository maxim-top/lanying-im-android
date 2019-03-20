
package top.maxim.im.sdk.bean;

import java.util.List;

import top.maxim.im.common.bean.BaseBean;

public class MentionBean extends BaseBean {

    private boolean mentionAll;

    private String mentionedMessage;

    private String pushMessage;

    private String senderNickname;

    private List<Long> mentionList;

    public boolean isMentionAll() {
        return mentionAll;
    }

    public void setMentionAll(boolean mentionAll) {
        this.mentionAll = mentionAll;
    }

    public String getMentionedMessage() {
        return mentionedMessage;
    }

    public void setMentionedMessage(String mentionedMessage) {
        this.mentionedMessage = mentionedMessage;
    }

    public String getPushMessage() {
        return pushMessage;
    }

    public void setPushMessage(String pushMessage) {
        this.pushMessage = pushMessage;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public void setSenderNickname(String senderNickname) {
        this.senderNickname = senderNickname;
    }

    public List<Long> getMentionList() {
        return mentionList;
    }

    public void setMentionList(List<Long> mentionList) {
        this.mentionList = mentionList;
    }
}
