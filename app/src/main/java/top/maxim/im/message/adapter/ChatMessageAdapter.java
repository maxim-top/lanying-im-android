
package top.maxim.im.message.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import im.floo.floolib.BMXMessage;
import top.maxim.im.message.interfaces.ChatActionListener;
import top.maxim.im.message.itemholder.BaseChatHolder;
import top.maxim.im.message.itemholder.IItemChatFactory;
import top.maxim.im.message.itemholder.MessageItemAudio;
import top.maxim.im.message.itemholder.MessageItemBaseView;
import top.maxim.im.message.itemholder.MessageItemFile;
import top.maxim.im.message.itemholder.MessageItemImage;
import top.maxim.im.message.itemholder.MessageItemLocation;
import top.maxim.im.message.itemholder.MessageItemText;
import top.maxim.im.sdk.bean.MsgBodyHelper;

/**
 * Description : 聊天页面消息adapter Created by Mango on 2018/11/18.
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<BaseChatHolder> {

    private List<BMXMessage> mBeans;

    private Context mContext;

    private ChatActionListener mActionListener;

    private Map<Class<? extends MessageItemBaseView>, Integer> holder2ViewType;

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
        holder.setData(mBeans.get(position));
        holder.showChatTime(isShowTime(position));
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

    /**
     * 根据类型获取holder
     *
     * @param parent 父布局
     * @param viewType 类型
     * @return BaseChatHolder
     */
    private BaseChatHolder getChatItemHolder(ViewGroup parent, int viewType) {
        IItemChatFactory factory = null;
        boolean isMySend = MsgBodyHelper.getContentBodyClass(viewType) != null;
        int type = isMySend ? viewType : ~viewType;
        if (type == BMXMessage.ContentType.Text.swigValue()) {
            // 文本
            factory = new MessageItemText(mContext, mActionListener,
                    isMySend ? MessageItemBaseView.ITEM_RIGHT : MessageItemBaseView.ITEM_LEFT);
        } else if (type == BMXMessage.ContentType.Image.swigValue()) {
            // 图片
            factory = new MessageItemImage(mContext, mActionListener,
                    isMySend ? MessageItemBaseView.ITEM_RIGHT : MessageItemBaseView.ITEM_LEFT);
        } else if (type == BMXMessage.ContentType.Video.swigValue()) {
            // 视频 TODO
        } else if (type == BMXMessage.ContentType.Location.swigValue()) {
            // 位置
            factory = new MessageItemLocation(mContext, mActionListener,
                    isMySend ? MessageItemBaseView.ITEM_RIGHT : MessageItemBaseView.ITEM_LEFT);
        } else if (type == BMXMessage.ContentType.File.swigValue()) {
            // 文件
            factory = new MessageItemFile(mContext, mActionListener,
                    isMySend ? MessageItemBaseView.ITEM_RIGHT : MessageItemBaseView.ITEM_LEFT);

        } else if (type == BMXMessage.ContentType.Voice.swigValue()) {
            // 语音
            factory = new MessageItemAudio(mContext, mActionListener,
                    isMySend ? MessageItemBaseView.ITEM_RIGHT : MessageItemBaseView.ITEM_LEFT);
        }
        if (factory == null) {
            // 无法识别的类型转为文本
            factory = new MessageItemText(mContext, mActionListener,
                    MessageItemBaseView.ITEM_RIGHT);
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
        int viewType = MsgBodyHelper.getContentBodyClass(bean.contentType().swigValue()) != null
                ? bean.contentType().swigValue()
                : -1;
        return isMySend ? viewType : ~viewType;
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

}
