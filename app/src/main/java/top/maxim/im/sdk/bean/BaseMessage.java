
package top.maxim.im.sdk.bean;

import java.io.Serializable;

/**
 * Description : 消息内容 Created by Mango on 2018/11/11.
 */
public abstract class BaseMessage implements Serializable {

    /* 消息类型 单聊 群聊*/
    int type;

    /* 发送者 */
    String from;

    /* 接收者 */
    String to;

    /* 消息id */
    String msgId;

    /* 消息类型  文本 语音等*/
    int contentType;

    /* 消息内容 */
    String content;

    /* 附件信息 */
    String attachment;

    /* 扩展信息 */
    String ext;

    /* 配置 */
    String config;

    /* 消息时间 */
    long messageTime;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public int getContentType() {
        return contentType;
    }

    public void setContentType(int contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public void setBaseMessage(){

    }
}
