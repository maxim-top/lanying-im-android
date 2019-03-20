
package top.maxim.im.message.itemholder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import im.floo.floolib.BMXFileAttachment;
import im.floo.floolib.BMXMessage;
import top.maxim.im.R;
import top.maxim.im.message.interfaces.ChatActionListener;

/**
 * Description : 消息文件类型 Created by Mango on 2018/11/18.
 */
public class MessageItemFile extends MessageItemBaseView {

    private LinearLayout mFileLayout;

    private TextView mFileDesc;

    public MessageItemFile(@NonNull Context context, ChatActionListener listener, int itemPos) {
        super(context, listener, itemPos);
    }

    @Override
    protected View initView(ViewGroup parent) {
        View view;
        if (mItemPos == ITEM_LEFT) {
            view = View.inflate(mContext, R.layout.item_chat_file_left, parent);
            mFileLayout = view.findViewById(R.id.layout_file_left);
            mFileDesc = ((TextView)view.findViewById(R.id.txt_file_title_left));
        } else {
            view = View.inflate(mContext, R.layout.item_chat_file_right, parent);
            mFileLayout = view.findViewById(R.id.layout_file_right);
            mFileDesc = ((TextView)view.findViewById(R.id.txt_file_title_right));
        }
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
        setItemViewListener(mFileLayout);
        showFile();
    }

    /**
     * 展示数据
     */
    private void showFile() {
        if (mMaxMessage == null || mMaxMessage.contentType() != BMXMessage.ContentType.File) {
            return;
        }
        BMXFileAttachment body = BMXFileAttachment.dynamic_cast(mMaxMessage.attachment());
        if (body == null) {
            return;
        }
        String title = body.displayName();
        mFileDesc.setText(title);
    }
}
