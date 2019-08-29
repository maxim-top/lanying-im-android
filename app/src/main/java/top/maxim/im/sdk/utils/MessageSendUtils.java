
package top.maxim.im.sdk.utils;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import im.floo.floolib.BMXFileAttachment;
import im.floo.floolib.BMXImageAttachment;
import im.floo.floolib.BMXLocationAttachment;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageAttachment;
import im.floo.floolib.BMXVideoAttachment;
import im.floo.floolib.BMXVoiceAttachment;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.bmxmanager.ChatManager;
import top.maxim.im.common.bean.MessageBean;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.sdk.bean.MentionBean;

/**
 * Description : 消息发送 Created by Mango on 2018/11/11.
 */
public final class MessageSendUtils {

    /**
     * 发送typing消息
     */
    public BMXMessage sendInputStatusMessage(BMXMessage.MessageType type, long from, long to,
            String extension) {
        BMXMessage msg = BMXMessage.createMessage(from, to, type, to, "");
        if (msg == null) {
            return null;
        }
        msg.setDeliveryQos(BMXMessage.DeliveryQos.AtMostOnce);
        msg.setExtension(extension);
        return handlerMessage(msg);
    }

    /**
     * 发送文本消息
     *
     * @param type 消息类型
     * @param from from
     * @param to to
     * @param text 文本
     * @return BMXMessage
     */
    public BMXMessage sendTextMessage(BMXMessage.MessageType type, long from, long to,
            String text) {
        return sendTextMessage(type, from, to, text, null, null);
    }

    /**
     * 发送文本消息
     * 
     * @param type 消息类型
     * @param from from
     * @param to to
     * @param text 文本
     * @return BMXMessage
     */
    public BMXMessage sendTextMessage(BMXMessage.MessageType type, long from, long to, String text,
            Map<String, String> atMap, String senderName) {
        BMXMessage msg = BMXMessage.createMessage(from, to, type, to, text);
        if (msg == null) {
            return null;
        }
        // 文本功能添加@对象
        if (atMap != null && !atMap.isEmpty()) {
            MentionBean mentionBean = new MentionBean();
            mentionBean.setSenderNickname(senderName);
            mentionBean.setPushMessage(ChatUtils.getInstance().getMessageDesc(msg));
            // @对象的存储信息 包括全部成员或者部分成员
            if (atMap.containsKey("-1")) {
                // @全部
                String atTitle = atMap.get("-1");
                if (!TextUtils.isEmpty(atTitle) && text.contains(atTitle)) {
                    // 如果包含全部直接走全部 还需要判断文本消息是否包含完成的@名称 如果没有就不触发@
                    mentionBean.setMentionAll(true);
                }
            } else {
                // @部分成员 需要遍历添加@信息
                List<Long> atIds = new ArrayList<>();
                mentionBean.setMentionAll(false);
                for (Map.Entry<String, String> entry : atMap.entrySet()) {
                    // 发送文字包含@对象的名称时再加入 防止输入框@对象名称被修改
                    if (entry.getValue() != null && !TextUtils.isEmpty(entry.getValue())
                            && text.contains(entry.getValue())) {
                        // @部分成员 feed信息只需要feedId和userId 所以需要去除无用的信息
                        atIds.add(Long.valueOf(entry.getKey()));
                    }
                }
                mentionBean.setMentionList(atIds);
            }
            msg.setConfig(new Gson().toJson(mentionBean));
        }
        return handlerMessage(msg);
    }

    /**
     * 发送图片消息
     * 
     * @param type 消息类型
     * @param from from
     * @param to to
     * @param path 图片路径
     * @param w 图片宽
     * @param h 图片高
     * @return BMXMessage
     */
    public BMXMessage sendImageMessage(BMXMessage.MessageType type, long from, long to, String path,
            int w, int h) {
        BMXImageAttachment.Size size = new BMXMessageAttachment.Size(w, h);
        BMXImageAttachment imageAttachment = new BMXImageAttachment(path, size);
        BMXMessage msg = BMXMessage.createMessage(from, to, type, to, imageAttachment);
        return handlerMessage(msg);
    }

    /**
     * 发送音频消息
     * 
     * @param type 消息类型
     * @param from from
     * @param to to
     * @param path 语音路径
     * @return BMXMessage
     */
    public BMXMessage sendAudioMessage(BMXMessage.MessageType type, long from, long to, String path,
            int time) {
        BMXVoiceAttachment voiceAttachment = new BMXVoiceAttachment(path, time);
        BMXMessage msg = BMXMessage.createMessage(from, to, type, to, voiceAttachment);
        return handlerMessage(msg);
    }

    /**
     * 发送视频消息
     *
     * @param type 消息类型
     * @param from from
     * @param to to
     * @param path 视频路径
     * @return BMXMessage
     */
    public BMXMessage sendVideoMessage(BMXMessage.MessageType type, long from, long to, String path,
            int time, int w, int h) {
        BMXVideoAttachment.Size size = new BMXVideoAttachment.Size(w, h);
        BMXVideoAttachment videoAttachment = new BMXVideoAttachment(path, time, size);
        BMXMessage msg = BMXMessage.createMessage(from, to, type, to, videoAttachment);
        return handlerMessage(msg);
    }

    /**
     * 发送文件消息
     *
     * @param type 消息类型
     * @param from from
     * @param to to
     * @param path 路径
     * @param name 名称
     * @return BMXMessage
     */
    public BMXMessage sendFileMessage(BMXMessage.MessageType type, long from, long to, String path,
            String name) {
        BMXFileAttachment fileAttachment = new BMXFileAttachment(path, name);
        BMXMessage msg = BMXMessage.createMessage(from, to, type, to, fileAttachment);
        return handlerMessage(msg);
    }

    /**
     * 发送文件消息
     *
     * @param type 消息类型
     * @param from from
     * @param to to
     * @param latitude 经度
     * @param longitude 纬度
     * @param address 地址
     * @return BMXMessage
     */
    public BMXMessage sendLocationMessage(BMXMessage.MessageType type, long from, long to,
            double latitude, double longitude, String address) {
        BMXLocationAttachment locationAttachment = new BMXLocationAttachment(latitude, longitude,
                address);
        BMXMessage msg = BMXMessage.createMessage(from, to, type, to, locationAttachment);
        return handlerMessage(msg);
    }

    /**
     * 发送消息
     * 
     * @return BMXMessage
     */
    public BMXMessage forwardMessage(MessageBean messageBean, BMXMessage.MessageType type,
            long from, long to) {
        if (messageBean == null) {
            return null;
        }
        BMXMessage.ContentType contentType = messageBean.getContentType();
        BMXMessage message = null;
        if (contentType == BMXMessage.ContentType.Text) {
            message = BMXMessage.createMessage(from, to, type, to, messageBean.getContent());
        } else if (contentType == BMXMessage.ContentType.Image) {
            // 图片
            BMXImageAttachment.Size size = new BMXMessageAttachment.Size(messageBean.getW(),
                    messageBean.getH());
            BMXImageAttachment imageAttachment = new BMXImageAttachment(messageBean.getPath(),
                    size);
            message = BMXMessage.createMessage(from, to, type, to, imageAttachment);
        } else if (contentType == BMXMessage.ContentType.Voice) {
            // 语音
            BMXVoiceAttachment voiceAttachment = new BMXVoiceAttachment(messageBean.getPath(),
                    messageBean.getDuration());
            message = BMXMessage.createMessage(from, to, type, to, voiceAttachment);
        } else if (contentType == BMXMessage.ContentType.File) {
            // 文件
            BMXFileAttachment fileAttachment = new BMXFileAttachment(messageBean.getPath(),
                    messageBean.getDisplayName());
            message = BMXMessage.createMessage(from, to, type, to, fileAttachment);
        } else if (contentType == BMXMessage.ContentType.Location) {
            // 地图
            BMXLocationAttachment locationAttachment = new BMXLocationAttachment(
                    messageBean.getLatitude(), messageBean.getLongitude(),
                    messageBean.getDisplayName());
            message = BMXMessage.createMessage(from, to, type, to, locationAttachment);
        }
        if (message != null && type == BMXMessage.MessageType.Group) {
            message.setEnableGroupAck(true);
        }
        Observable.just(message).map(new Func1<BMXMessage, BMXMessage>() {
            @Override
            public BMXMessage call(BMXMessage msg) {
                ChatManager.getInstance().forwardMessage(msg);
                return msg;
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXMessage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BMXMessage message) {

                    }
                });
        return message;
    }

    private BMXMessage handlerMessage(BMXMessage msg) {
        if (msg == null) {
            return null;
        }
        if (msg.type() == BMXMessage.MessageType.Group) {
            msg.setEnableGroupAck(true);
        }
        Observable.just(msg).map(new Func1<BMXMessage, BMXMessage>() {
            @Override
            public BMXMessage call(BMXMessage message) {
                ChatManager.getInstance().sendMessage(message);
                return message;
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXMessage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BMXMessage message) {

                    }
                });
        return msg;
    }
}
