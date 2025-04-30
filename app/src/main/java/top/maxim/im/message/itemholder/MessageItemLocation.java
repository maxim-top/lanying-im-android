
package top.maxim.im.message.itemholder;


import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import im.floo.floolib.BMXLocationAttachment;
import im.floo.floolib.BMXMessage;
import top.maxim.im.R;
import top.maxim.im.message.interfaces.ChatActionListener;
import top.maxim.im.sdk.bean.MsgBodyHelper;

/**
 * Description : 消息位置类型 Created by Mango on 2018/11/18.
 */
public class MessageItemLocation extends MessageItemBaseView {

    private RelativeLayout mLocationLayout;

    private TextView mLocationAddr;

    public MessageItemLocation(@NonNull Context context, ChatActionListener listener, int itemPos) {
        super(context, listener, itemPos);
    }

    @Override
    protected View initView(ViewGroup parent) {
        View view;
        if (MsgBodyHelper.isLeftStyle(mItemPos)) {
            view = View.inflate(mContext, R.layout.item_chat_location_left, parent);
            mLocationLayout = view.findViewById(R.id.layout_location_left);
            mLocationAddr = ((TextView)view.findViewById(R.id.txt_local_title_left));
        } else {
            view = View.inflate(mContext, R.layout.item_chat_location_right, parent);
            mLocationLayout = view.findViewById(R.id.layout_location_right);
            mLocationAddr = ((TextView)view.findViewById(R.id.txt_local_title_right));
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
        setItemViewListener(mLocationLayout);
        showLocation();
    }

    /**
     * 展示数据
     */
    private void showLocation() {
        if (mMaxMessage == null || mMaxMessage.contentType() != BMXMessage.ContentType.Location) {
            return;
        }
        BMXLocationAttachment body = BMXLocationAttachment.dynamic_cast(mMaxMessage.attachment());
        if (body == null) {
            return;
        }
        String title = body.address();
        mLocationAddr.setText(title);
    }
}
