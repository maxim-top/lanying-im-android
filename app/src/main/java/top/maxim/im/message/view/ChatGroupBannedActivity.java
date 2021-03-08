
package top.maxim.im.message.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.ListOfLongLong;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonCustomDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.BMImageLoader;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseRecyclerAdapter;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.DividerItemDecoration;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description : 群禁言列表 Created by Mango on 2018/11/25.
 */
public class ChatGroupBannedActivity extends BaseTitleActivity {

    /* 群聊成员gridView */
    protected RecyclerView mGvGroupMember;

    /* 群聊成员adapter */
    protected ChatGroupBannedAdapter mAdapter;

    protected long mGroupId;

    private boolean mChoose = false;

    protected BMXGroup mGroup = new BMXGroup();

    protected Map<Long, Boolean> mSelected = new HashMap<>();

    protected static final String CHOOSE = "choose";

    public static final String CHOOSE_DATA = "chooseData";

    private boolean isEdit = false;

    private int CHOOSE_MUTE_CODE = 1000;

    public static void startGroupBannedActivity(Context context, long groupId) {
        Intent intent = new Intent(context, ChatGroupBannedActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.group_ban);
        builder.setRightText(R.string.edit, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEdit = !isEdit;
                if (isEdit) {
                    mHeader.setRightText(getString(R.string.confirm));
                } else {
                    removeBan();
                    mHeader.setRightText(getString(R.string.edit));
                }
                mAdapter.setShowCheck(isEdit);
                mAdapter.notifyDataSetChanged();
            }
        });
        builder.setBackIcon(R.drawable.header_back_icon, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.chat_group_list_member_view, null);
        mGvGroupMember = view.findViewById(R.id.gv_chat_group_member);
        mGvGroupMember.setLayoutManager(new LinearLayoutManager(this));
        mGvGroupMember.addItemDecoration(new DividerItemDecoration(this, R.color.guide_divider));
        mGvGroupMember.setAdapter(mAdapter = new ChatGroupBannedAdapter(this));
        mAdapter.setShowCheck(mChoose);
        return view;
    }

    @Override
    protected void setViewListener() {
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BMXGroup.BannedMember member = mAdapter.getItem(position);
                if (member == null) {
                    return;
                }
                long mId = member.getMUid();
                if (!mAdapter.getShowCheck() && mId == MessageConfig.MEMBER_ADD) {
                    ChatGroupListMemberActivity.startGroupMemberListActivity(
                            ChatGroupBannedActivity.this, mGroupId, true, CHOOSE_MUTE_CODE);
                    return;
                }
                if (mAdapter.getShowCheck() && mId != MessageConfig.MEMBER_ADD) {
                    if (!mSelected.containsKey(mId) || !mSelected.get(mId)) {
                        mSelected.put(mId, true);
                    } else {
                        mSelected.remove(mId);
                    }
                    mAdapter.notifyItemChanged(position);
                }
            }
        });
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mGroupId = intent.getLongExtra(MessageConfig.CHAT_ID, 0);
            mChoose = intent.getBooleanExtra(CHOOSE, false);
        }
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        initGroupInfo();
    }

    private void initGroupInfo() {
        showLoadingDialog(true);
        GroupManager.getInstance().getGroupList(mGroupId, false, (bmxErrorCode, bmxGroup) -> {
            dismissLoadingDialog();
            if (bmxGroup != null) {
                mGroup = bmxGroup;
            }
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                init();
            } else {
                toastError(bmxErrorCode);
            }
        });
    }

    private void init() {
        if (mGroup == null) {
            return;
        }
        showLoadingDialog(true);
        GroupManager.getInstance().getBannedMembers(mGroup, (bmxErrorCode, memberList) -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                ListOfLongLong listOfLongLong = new ListOfLongLong();
                for (int i = 0; i < memberList.size(); i++) {
                    listOfLongLong.add(memberList.get(i).getMUid());
                }
                RosterManager.getInstance().getRosterList(listOfLongLong, true,
                        (bmxErrorCode1, itemList) -> {
                            RosterFetcher.getFetcher().putRosters(itemList);
                            List<BMXGroup.BannedMember> members = new ArrayList<>();
                            if (memberList != null && !memberList.isEmpty()) {
                                for (int i = 0; i < memberList.size(); i++) {
                                    members.add(memberList.get(i));
                                }
                            }
                            BMXGroup.BannedMember add = new BMXGroup.BannedMember();
                            add.setMUid(MessageConfig.MEMBER_ADD);
                            members.add(add);
                            mAdapter.replaceList(members);
                        });
            } else {
                toastError(bmxErrorCode);
            }
        });
    }

    private void removeBan() {
        final ListOfLongLong black = new ListOfLongLong();
        for (Map.Entry<Long, Boolean> entry : mSelected.entrySet()) {
            if (entry.getValue()) {
                black.add(entry.getKey());
            }
        }
        if (black.isEmpty()) {
            return;
        }
        showLoadingDialog(true);
        GroupManager.getInstance().unbanMembers(mGroup, black, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                init();
            } else {
                toastError(bmxErrorCode);
            }
        });
    }

    private void addBan(ListOfLongLong mute, String reason, long time) {
        if (mute == null || mute.isEmpty()) {
            return;
        }
        showLoadingDialog(true);
        GroupManager.getInstance().banMembers(mGroup, mute, time, reason, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                init();
            } else {
                toastError(bmxErrorCode);
            }
        });
    }

    private void showAddBan(final ListOfLongLong mute) {
        if (mute == null || mute.isEmpty()) {
            return;
        }
        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams editP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editP.setMargins(ScreenUtils.dp2px(10), ScreenUtils.dp2px(5), ScreenUtils.dp2px(10),
                ScreenUtils.dp2px(5));
        // 禁言时间
        TextView time = new TextView(this);
        time.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        time.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        time.setTextColor(getResources().getColor(R.color.color_black));
        time.setBackgroundColor(getResources().getColor(R.color.color_white));
        time.setText(getString(R.string.group_ban_time));
        ll.addView(time, textP);

        final EditText editTime = new EditText(this);
        editTime.setBackgroundResource(R.drawable.common_edit_corner_bg);
        editTime.setPadding(ScreenUtils.dp2px(5), 0, ScreenUtils.dp2px(5), 0);
        editTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        editTime.setTextColor(getResources().getColor(R.color.color_black));
        editTime.setMinHeight(ScreenUtils.dp2px(40));
        editTime.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams editTimeP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dp2px(40));
        editTimeP.setMargins(ScreenUtils.dp2px(10), ScreenUtils.dp2px(5), ScreenUtils.dp2px(10),
                ScreenUtils.dp2px(5));
        ll.addView(editTime, editTimeP);

        // 描述
        TextView desc = new TextView(this);
        desc.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        desc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        desc.setTextColor(getResources().getColor(R.color.color_black));
        desc.setBackgroundColor(getResources().getColor(R.color.color_white));
        desc.setText(getString(R.string.group_ban_reason));
        ll.addView(desc, textP);

        final EditText editDesc = new EditText(this);
        editDesc.setBackgroundResource(R.drawable.common_edit_corner_bg);
        editDesc.setPadding(ScreenUtils.dp2px(5), 0, ScreenUtils.dp2px(5), 0);
        editDesc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        editDesc.setTextColor(getResources().getColor(R.color.color_black));
        editDesc.setMinHeight(ScreenUtils.dp2px(40));
        ll.addView(editDesc, editP);

        DialogUtils.getInstance().showCustomDialog(this, ll, "禁言", getString(R.string.confirm),
                getString(R.string.cancel), new CommonCustomDialog.OnDialogListener() {

                    @Override
                    public void onConfirmListener() {
                        String time = editTime.getEditableText().toString().trim();
                        long dur = !TextUtils.isEmpty(time) && TextUtils.isDigitsOnly(time)
                                ? Long.valueOf(time)
                                : -1;
                        String reason = editDesc.getEditableText().toString().trim();
                        addBan(mute, reason, dur);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    private void toastError(BMXErrorCode e) {
        String error = e != null ? e.name() : "网络异常";
        ToastUtil.showTextViewPrompt(error);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_MUTE_CODE && resultCode == RESULT_OK && data != null) {
            List<Long> chooseList = (List<Long>)data
                    .getSerializableExtra(ChatGroupListMemberActivity.CHOOSE_DATA);
            if (chooseList != null && chooseList.size() > 0) {
                ListOfLongLong admin = new ListOfLongLong();
                for (Long id : chooseList) {
                    admin.add(id);
                }
                showAddBan(admin);
            }
        }
    }

    /**
     * 展示群聊成员adapter
     */
    protected class ChatGroupBannedAdapter extends BaseRecyclerAdapter<BMXGroup.BannedMember> {

        private ImageRequestConfig mConfig;

        private boolean mIsShowCheck;

        public ChatGroupBannedAdapter(Context context) {
            super(context);
            mConfig = new ImageRequestConfig.Builder().cacheInMemory(true).cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .showImageForEmptyUri(R.drawable.default_avatar_icon)
                    .showImageOnFail(R.drawable.default_avatar_icon)
                    .showImageOnLoading(R.drawable.default_avatar_icon).build();
        }

        public void setShowCheck(boolean showCheck) {
            mIsShowCheck = showCheck;
        }

        public boolean getShowCheck() {
            return mIsShowCheck;
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
            BMXGroup.BannedMember member = getItem(position);
            if (member == null) {
                return;
            }
            if (mIsShowCheck) {
                boolean isCheck = mSelected.containsKey(member.getMUid())
                        && mSelected.get(member.getMUid());
                checkBox.setChecked(isCheck);
                checkBox.setVisibility(member.getMUid() != MessageConfig.MEMBER_ADD
                        && member.getMUid() != MessageConfig.MEMBER_REMOVE ? View.VISIBLE
                                : View.INVISIBLE);
            } else {
                checkBox.setVisibility(View.GONE);
            }
            if (member.getMUid() == MessageConfig.MEMBER_ADD) {
                String addName = member.getMGroupNickname();
                tvName.setText(TextUtils.isEmpty(addName) ? "添加" : addName);
                BMImageLoader.getInstance().display(icon,
                        "drawable://" + R.drawable.default_add_icon);
            } else if (member.getMUid() == MessageConfig.MEMBER_REMOVE) {
                String removeName = member.getMGroupNickname();
                tvName.setText(TextUtils.isEmpty(removeName) ? "移除" : removeName);
                BMImageLoader.getInstance().display(icon,
                        "drawable://" + R.drawable.default_remove_icon);
            } else {
                BMXRosterItem rosterItem = RosterFetcher.getFetcher().getRoster(member.getMUid());
                String name;
                if (rosterItem != null && !TextUtils.isEmpty(rosterItem.alias())) {
                    name = rosterItem.alias();
                } else if (rosterItem != null && !TextUtils.isEmpty(rosterItem.nickname())) {
                    name = rosterItem.nickname();
                } else if (rosterItem != null) {
                    name = rosterItem.username();
                } else {
                    name = member.getMGroupNickname();
                }
                tvName.setText(TextUtils.isEmpty(name) ? "" : name);
                ChatUtils.getInstance().showRosterAvatar(rosterItem, icon, mConfig);
            }
        }
    }
}
