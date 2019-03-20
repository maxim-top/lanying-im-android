
package top.maxim.im.message.itemholder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.floo.floolib.BMXMessage;
import top.maxim.im.R;
import top.maxim.im.message.interfaces.ChatActionListener;

/**
 * Description : 消息文本类型 Created by Mango on 2018/11/18.
 */
public class MessageItemText extends MessageItemBaseView {

    private TextView mChatText;

    public MessageItemText(@NonNull Context context, ChatActionListener listener, int itemPos) {
        super(context, listener, itemPos);
    }

    @Override
    protected View initView(ViewGroup parent) {
        View view;
        if (mItemPos == ITEM_LEFT) {
            view = View.inflate(mContext, R.layout.item_chat_text_left, parent);
        } else {
            view = View.inflate(mContext, R.layout.item_chat_text_right, parent);
        }
        mChatText = ((TextView)view.findViewById(R.id.txt_message));
        return view;
    }

    @Override
    protected void bindData() {
        fillView();
    }

    /**
     * 填充数据
     */
    private void fillView() {
        setItemViewListener(mChatText);
        showText();
    }

    /**
     * 展示文本数据
     */
    private void showText() {
        if (mMaxMessage == null) {
            mChatText.setText("");
            return;
        }
        if (mMaxMessage.contentType() != BMXMessage.ContentType.Text) {
            // 非文本转为无法识别
            mChatText.setText("未知消息");
            return;
        }
        mChatText.setText(TextUtils.isEmpty(mMaxMessage.content()) ? "" : mMaxMessage.content());
    }
}
