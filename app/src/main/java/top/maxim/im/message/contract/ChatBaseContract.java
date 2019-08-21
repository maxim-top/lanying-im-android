
package top.maxim.im.message.contract;

import android.content.Intent;
import android.os.Bundle;

import java.util.List;
import java.util.Map;

import im.floo.floolib.BMXMessage;
import top.maxim.im.common.base.IBasePresenter;
import top.maxim.im.common.base.IBaseView;

/**
 * Description : 聊天基类contact Created by Mango on 2018/11/05
 */
public interface ChatBaseContract {

    /**
     * 聊天基类view
     */
    interface View extends IBaseView<Presenter> {

        void setHeadTitle(String title);

        /**
         * 展示消息
         * @param beans 消息列表
         */
        void showChatMessages(List<BMXMessage> beans);

        /**
         * 展示下拉消息
         * @param beans  消息列表
         */
        void showPullChatMessages(List<BMXMessage> beans, int offset);

        /**
         * 发送消息
         * @param bean  消息体
         */
        void sendChatMessage(BMXMessage bean);

        /**
         * 收到消息
         * @param beans  消息体
         */
        void receiveChatMessage(List<BMXMessage> beans);

        /**
         * 发送消息列表
         * @param beans  消息列表
         */
        void sendChatMessages(List<BMXMessage> beans);

        /**
         * 更新消息
         *
         * @param bean 消息体
         */
        void updateChatMessage(BMXMessage bean);

        /**
         * 删除信息
         *
         * @param bean 消息体
         */
        void deleteChatMessage(BMXMessage bean);

        /**
         * 更新列表
         */
        void updateListView();

        /**
         * 获取最后一条消息
         * @return BMXMessage
         */
        BMXMessage getLastMessage();

        /**
         * 清除消息
         *
         */
        void clearChatMessages();

        /**
         * 停止播放的语音
         */
        void cancelVoicePlay();

        /**
         * 展示录制语音view
         */
        void showRecordView();

        /**
         * 展示录制语音音量
         */
        void showRecordMicView(int radio);

        /**
         * 隐藏语音录制view
         */
        void hideRecordView();

        void insertInAt(List<String> atNames);

        /**
         * 设置输入板文字框
         *
         * @param content 显示内容
         */
        void setControlBarText(String content);

        /**
         * 获取输入板文字
         */
        String getControlBarText();

    }

    /**
     * 聊天基类presenter
     */
    interface Presenter extends IBasePresenter<View> {

        /**
         * 加载数据
         */
        void initChatData(long msgId);

        /**
         * 获取下拉的历史消息
         *
         * @param msgId  第一条msgId
         * @param offset view偏移
         */
        void getPullDownChatMessages(long msgId, int offset);

        /**
         * 保存页面信息
         *
         * @param outState 页面数据
         */
        void onSaveInstanceState(Bundle outState);

        /**
         * 获取页面信息
         *
         * @param outState 页面数据
         */
        void onRestoreInstanceState(Bundle outState);

        /**
         * onPause
         */
        void onPause();

        /**
         * 设置聊天双方数据
         * @param chatType  聊天类型
         * @param myUserId  我的id
         * @param chatId    对方id
         */
        void setChatInfo(BMXMessage.MessageType chatType, long myUserId, long chatId);

        /**
         * 设置聊天presenter model
         * @param view   view
         * @param model  model
         */
        void setChatBaseView(ChatBaseContract.View view, ChatBaseContract.Model model);

        /**
         * 功能监听
         */
        void onFunctionRequest(String functionType);

        /**
         * 发送文本
         * @param sendText  文本内容
         */
        void onSendTextRequest(String sendText);

        /**
         * 发送语音
         * @param voiceAction  语音录制事件
         * @param voiceTime 语音录制时间
         */
        void onSendVoiceRequest(int voiceAction, long voiceTime);

        /**
         * 停止一切占用音频播放的
         */
        void stopAudio();

        /**
         * 消息体点击
         * @param bean 消息体
         */
        void onItemFunc(BMXMessage bean);

        /**
         * 消息体长按
         * @param bean 消息体
         */
        void onMessageLongClick(BMXMessage bean);

        /**
         * 已读回执
         * @param bean 消息体
         */
        void onMessageReadAck(BMXMessage bean);

        /**
         * 消息重发
         * @param bean 消息体
         */
        void onReSendMessage(BMXMessage bean);

        /**
         * 返回数据
         * @param requestCode  请求码
         * @param resultCode   返回码
         * @param data         返回数据
         */
        void onActivityResult(int requestCode, int resultCode, Intent data);

        /**
         * 注册距离传感器
         */
        void registerSensor();

        /**
         * 取消注册距离传感器
         */
        void unRegisterSensor();

        /**
         * 获取输入板@的成员对象
         *
         */
        Map<String, String> getChatAtMembers();

        void clearAtFeed();

        /**
         * 设置群聊已读
         */
        void readAllMessage();
    }

    /**
     * 消息model
     */
    interface Model {

    }

}
