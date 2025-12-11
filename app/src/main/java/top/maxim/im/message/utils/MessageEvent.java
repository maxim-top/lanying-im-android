package top.maxim.im.message.utils;

public class MessageEvent{

    private String message;
    private String params;

    public MessageEvent(String message){
        this.message = message;
    }

    public MessageEvent(String message, String params){
        this.message = message;
        this.params  = params;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }
}