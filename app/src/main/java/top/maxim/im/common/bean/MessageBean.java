
package top.maxim.im.common.bean;

import im.floo.floolib.BMXMessage;

public class MessageBean extends BaseBean {

    private long chatId;

    private BMXMessage.MessageType type;

    private BMXMessage.ContentType contentType;

    private String content;

    private String path;

    private String displayName;

    private int duration;

    private int w;

    private int h;

    private double latitude;

    private double longitude;

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public BMXMessage.MessageType getType() {
        return type;
    }

    public void setType(BMXMessage.MessageType type) {
        this.type = type;
    }

    public BMXMessage.ContentType getContentType() {
        return contentType;
    }

    public void setContentType(BMXMessage.ContentType contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
