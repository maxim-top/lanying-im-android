
package top.maxim.im.message.adapter;

import static top.maxim.im.sdk.bean.MsgBodyHelper.VIEW_TYPE.*;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageConfig;
import top.maxim.im.message.interfaces.ChatActionListener;
import top.maxim.im.message.itemholder.BaseChatHolder;
import top.maxim.im.message.itemholder.IItemChatFactory;
import top.maxim.im.message.itemholder.MessageItemAudio;
import top.maxim.im.message.itemholder.MessageItemBaseView;
import top.maxim.im.message.itemholder.MessageItemFile;
import top.maxim.im.message.itemholder.MessageItemImage;
import top.maxim.im.message.itemholder.MessageItemLocation;
import top.maxim.im.message.itemholder.MessageItemText;
import top.maxim.im.message.itemholder.MessageItemVideo;
import top.maxim.im.sdk.bean.MsgBodyHelper;

/**
 * Description : 聊天页面消息adapter Created by Mango on 2018/11/18.
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<BaseChatHolder> {

    private List<BMXMessage> mBeans;

    private Context mContext;

    private ChatActionListener mActionListener;

    private Map<Class<? extends MessageItemBaseView>, Integer> holder2ViewType;

    private boolean showReadAck;

    public ChatMessageAdapter(Context context) {
        mContext = context;
    }

    public void setChatMessages(List<BMXMessage> beans) {
        mBeans = beans;
    }

    public void setActionListener(ChatActionListener actionListener) {
        mActionListener = actionListener;
    }

    @Override
    public int getItemCount() {
        if (mBeans != null) {
            return mBeans.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (mBeans == null || mBeans.size() == 0) {
            return -1;
        }
        return getChatItemType(mBeans.get(position));
    }

    @Override
    public BaseChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return getChatItemHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(BaseChatHolder holder, int position) {
        if (holder == null) {
            return;
        }
        holder.showChatExtra(isShowTime(position), showReadAck);
        BMXMessage bean = mBeans.get(position);
        BMXMessageConfig config = bean.config();
        if (config != null){
            String action = config.getRTCAction();
            if (action != null && action.length() != 0 && !action.equals("record")){
                RecyclerView.LayoutParams param = new RecyclerView.LayoutParams(1, 1);
                holder.itemView.setLayoutParams(param);
            }
        }

        holder.setData(bean);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull BaseChatHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onViewAttach();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull BaseChatHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onViewDetach();
    }

    @Override
    public void onViewRecycled(@NonNull BaseChatHolder holder) {
        super.onViewRecycled(holder);
        holder.onViewRecycled();
    }

    /**
     * 根据类型获取holder
     *
     * @param parent   父布局
     * @param viewType 类型
     * @return BaseChatHolder
     */
    private BaseChatHolder getChatItemHolder(ViewGroup parent, int viewType) {
        IItemChatFactory factory = null;
        MsgBodyHelper.BASE_VIEW_TYPE baseViewType = MsgBodyHelper.getBaseViewType(viewType);
        switch (baseViewType){
            case BASE_VIEW_TYPE_IMAGE:
                factory = new MessageItemImage(mContext, mActionListener, viewType);
                break;
            case BASE_VIEW_TYPE_VOICE:
                factory = new MessageItemAudio(mContext, mActionListener, viewType);
                break;
            case BASE_VIEW_TYPE_VIDEO:
                factory = new MessageItemVideo(mContext, mActionListener, viewType);
                break;
            case BASE_VIEW_TYPE_FILE:
                factory = new MessageItemFile(mContext, mActionListener, viewType);
                break;
            case BASE_VIEW_TYPE_LOCATION:
                factory = new MessageItemLocation(mContext, mActionListener, viewType);
                break;
            default:
                factory = new MessageItemText(mContext, mActionListener, viewType);
                break;
        }
        return new BaseChatHolder(factory.obtainView(parent), factory);
    }

    /**
     * 获取消息对应的类型
     *
     * @param bean 消息体
     * @return int
     */
    private int getChatItemType(BMXMessage bean) {
        if (bean == null) {
            return -1;
        }
        boolean isMySend = !bean.isReceiveMsg();
        boolean isSystem = bean.type() == BMXMessage.MessageType.System;
        int viewType;
        if (isSystem){
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(bean.extension());
                String style = jsonObject.getString("style");
                if (!TextUtils.isEmpty(style)){
                    viewType = MsgBodyHelper.getViewTypeByStyle(style);
                    return viewType;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            viewType = MsgBodyHelper.VIEW_TYPE.VIEW_TYPE_INFO.ordinal();
            return viewType;
        }

        viewType = MsgBodyHelper.getViewTypeByContentType(bean.contentType(), isMySend);
        return viewType;
    }

    /**
     * 是否展示时间
     *
     * @param position 当前位置
     * @return boolean
     */
    private boolean isShowTime(int position) {
        // 聊天消息时间戳为毫秒级 目前为10分钟间隔 也就是10 * 60 * 1000
        long timeOut = 10 * 60 * 1000;
        BMXMessage current = mBeans.get(position);
        boolean showtime = true;
        if (position > 0) {
            BMXMessage pre = mBeans.get(position - 1);
            if (current.serverTimestamp() == 0 || pre.serverTimestamp() == 0) {
                showtime = false;
            } else {
                long time_cha = 0;
                try {
                    time_cha = (current.serverTimestamp() - pre.serverTimestamp()) / timeOut;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                showtime = time_cha >= 1;
            }
        }
        return showtime;
    }

    public void showReadAck(boolean showReadAck) {
        this.showReadAck = showReadAck;
    }
}
