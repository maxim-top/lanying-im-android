
package top.maxim.im.contact.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import im.floo.floolib.BMXRosterItem;
import top.maxim.im.R;
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.RecyclerWithHFAdapter;
import top.maxim.im.message.utils.ChatUtils;

/**
 * Description : 消息列表 Created by Mango on 2018/11/05.
 */
public class ContactAdapter extends RecyclerWithHFAdapter<BMXRosterItem> {

    private ImageRequestConfig mConfig;

    public ContactAdapter(Context context) {
        super(context);
        mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
                .showImageForEmptyUri(R.drawable.default_avatar_icon)
                .showImageOnFail(R.drawable.default_avatar_icon)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageOnLoading(R.drawable.default_avatar_icon).build();
    }

    @Override
    protected int onCreateViewById(int viewType) {
        return R.layout.item_contact_view;
    }

    @Override
    protected void onBindHolder(BaseViewHolder holder, int position) {
        BMXRosterItem bean = getItem(position);
        ShapeImageView avatar = holder.findViewById(R.id.contact_avatar);
        TextView title = holder.findViewById(R.id.contact_title);
        TextView sticky = holder.findViewById(R.id.contact_sticky);
        sticky.setVisibility(View.GONE);
        // if (position == 0) {
        // } else {
        // BMXRosterItem preBean = getItem(position - 1);
        // String pre = preBean.;
        // String cur = bean.getFirst();
        // if (!TextUtils.equals(pre, cur)) {
        // sticky.setVisibility(View.VISIBLE);
        // } else {
        // sticky.setVisibility(View.GONE);
        // }
        // }
        // sticky.setText(bean == null ? "" : bean.getFirst());
        if (bean == null) {
            return;
        }
        String name = CommonUtils.getRosterDisplayName(bean);
        title.setText(name);
        ChatUtils.getInstance().showRosterAvatar(bean, avatar, mConfig);
    }

}
