
package top.maxim.im.message.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.HashMap;
import java.util.Map;

import im.floo.floolib.BMXConversation;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXRosterItem;
import top.maxim.im.R;
import top.maxim.im.common.bean.TargetBean;
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.RecyclerWithHFAdapter;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;

public class ForwardingSessionAdapter extends RecyclerWithHFAdapter<TargetBean> {

    private ImageRequestConfig mConfig;

    private ImageRequestConfig mGroupConfig;

    private Context mContext;

    protected Map<Long, Boolean> mSelected = new HashMap<>();

    public ForwardingSessionAdapter(Context context, Map<Long, Boolean> selected) {
        super(context);
        mContext = context;
        mSelected = selected;
        mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
                .showImageForEmptyUri(R.drawable.default_avatar_icon)
                .showImageOnFail(R.drawable.default_avatar_icon)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageOnLoading(R.drawable.default_avatar_icon).build();
        mGroupConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
                .showImageForEmptyUri(R.drawable.default_group_icon)
                .showImageOnFail(R.drawable.default_group_icon)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageOnLoading(R.drawable.default_group_icon).build();
    }

    @Override
    protected int onCreateViewById(int viewType) {
        return R.layout.item_group_list_member;
    }

    @Override
    protected void onBindHolder(BaseViewHolder holder, int position) {
        ShapeImageView icon = holder.findViewById(R.id.img_icon);
        TextView tvName = holder.findViewById(R.id.txt_name);
        CheckBox checkBox = holder.findViewById(R.id.cb_choice);
        TargetBean item = getItem(position);
        if (item == null) {
            return;
        }

        boolean isCheck = mSelected.containsKey(item.getId())
                && mSelected.get(item.getId());
        checkBox.setChecked(isCheck);
        checkBox.setVisibility(item.getId() != MessageConfig.MEMBER_ADD
                && item.getId() != MessageConfig.MEMBER_REMOVE ? View.VISIBLE
                : View.INVISIBLE);

        // 是否开启免打扰
        String name = "";
        if (item.getType() == BMXConversation.Type.Group){
            BMXGroup groupItem = RosterFetcher.getFetcher().getGroup(item.getId());
            if (groupItem!=null){
                name = groupItem.name();
                ChatUtils.getInstance().showGroupAvatar(groupItem, icon, mGroupConfig);
            }
        } else{
            BMXRosterItem rosterItem = RosterFetcher.getFetcher().getRoster(item.getId());
            if (rosterItem != null){
                name = CommonUtils.getRosterDisplayName(rosterItem);
                ChatUtils.getInstance().showRosterAvatar(rosterItem, icon, mConfig);
            } else{
                ChatUtils.getInstance().showRosterAvatar(null, icon, mConfig);
            }
            if (item.getId() == 0){
                name = mContext.getString(R.string.sys_msg);
            }
        }
        tvName.setText(name);
    }
}
