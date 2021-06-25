
package top.maxim.im.message.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import im.floo.BMXDataCallBack;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupAnnouncementList;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonCustomDialog;
import top.maxim.im.common.utils.dialog.CustomDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ItemLine;
import top.maxim.im.common.view.ItemLineArrow;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.DividerItemDecoration;
import top.maxim.im.common.view.recyclerview.RecyclerWithHFAdapter;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description : 群公告列表 Created by Mango on 2018/11/25.
 */
public class ChatGroupAnnomentActivity extends BaseTitleActivity {

    protected RecyclerView mGvGroupMember;

    protected ChatGroupAnnouncementAdapter mAdapter;

    private TextView mTvLatestTitle;

    private TextView mTvLatestContent;

    protected long mGroupId;

    protected BMXGroup mGroup = new BMXGroup();

    public static void startGroupAnnoucementActivity(Activity context, long groupId) {
        Intent intent = new Intent(context, ChatGroupAnnomentActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.group_notice);
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
        mGvGroupMember.setAdapter(mAdapter = new ChatGroupAnnouncementAdapter(this));
        buildHeaderView();
        buildFooterView();
        return view;
    }

    /**
     * 设置添加view
     */
    protected void buildHeaderView() {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);

        ItemLineArrow.Builder mChatGroupDesc = new ItemLineArrow.Builder(this)
                .setStartContent("最近公告");
        ll.addView(mChatGroupDesc.build());

        // 分割线
        ItemLine.Builder itemLine = new ItemLine.Builder(this, ll)
                .setMarginLeft(ScreenUtils.dp2px(15));
        ll.addView(itemLine.build());

        View view = View.inflate(this, R.layout.item_annocement_view, null);
        mTvLatestTitle = view.findViewById(R.id.tv_title);
        mTvLatestContent = view.findViewById(R.id.tv_content);
        ll.addView(view);

        // 分割线
        ItemLine.Builder itemLine1 = new ItemLine.Builder(this, ll)
                .setMarginLeft(ScreenUtils.dp2px(15));
        ll.addView(itemLine1.build());

        ItemLineArrow.Builder list = new ItemLineArrow.Builder(this).setStartContent("公告列表");
        ll.addView(list.build());
        mAdapter.addHeaderView(ll);
    }

    private void buildFooterView() {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        View addView = buildAddFooterView(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAnnomentInfo();
            }
        });
        ll.addView(addView);
        mAdapter.addFooterView(ll);
    }

    /**
     * 设置添加view
     */
    protected View buildAddFooterView(View.OnClickListener listener) {
        View view = View.inflate(this, R.layout.item_group_list_member, null);
        ShapeImageView icon = view.findViewById(R.id.img_icon);
        TextView tvName = view.findViewById(R.id.txt_name);
        tvName.setText("添加");
        icon.setImageResource(R.drawable.default_add_icon);
        CheckBox checkBox = view.findViewById(R.id.cb_choice);
        view.setOnClickListener(listener);
        return view;
    }

    @Override
    protected void setViewListener() {
        mAdapter.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                    long id) {
                BMXGroup.Announcement announcement = mAdapter == null ? null
                        : mAdapter.getItem(position);
                showOperate(announcement);
                return false;
            }
        });
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mGroupId = intent.getLongExtra(MessageConfig.CHAT_ID, 0);
        }
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        GroupManager.getInstance().getGroupList(mGroupId, false, (bmxErrorCode, bmxGroup) -> {
            if (bmxGroup != null) {
                mGroup = bmxGroup;
            }
            init();
        });
    }

    protected void init() {
        if (mGroup == null) {
            return;
        }
        showLoadingDialog(true);
        initData(true, (bmxErrorCode, bmxGroupAnnouncementList) -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                bindData(bmxGroupAnnouncementList);
            } else {
                toastError(bmxErrorCode);
                initData(false, (bmxErrorCode1, bmxGroupAnnouncementList1) -> {
                    bindData(bmxGroupAnnouncementList1);
                });
            }
        });
    }

    private void initLatest() {
        if (mGroup == null) {
            return;
        }
        GroupManager.getInstance().getLatestAnnouncement(mGroup, true,
                (bmxErrorCode, bmxGroupAnnouncement) -> {
                    dismissLoadingDialog();
                    if (BaseManager.bmxFinish(bmxErrorCode)) {
                        bindLatest(bmxGroupAnnouncement);
                    } else {
                        GroupManager.getInstance().getLatestAnnouncement(mGroup, false,
                                (bmxErrorCode1, bmxGroupAnnouncement1) -> {
                                    bindLatest(bmxGroupAnnouncement1);
                                });
                    }
                });
    }

    protected void initData(boolean forceRefresh,
            BMXDataCallBack<BMXGroupAnnouncementList> callBack) {
        GroupManager.getInstance().getAnnouncementList(mGroup, forceRefresh, callBack);
    }

    protected void bindData(BMXGroupAnnouncementList announcementList) {
        List<BMXGroup.Announcement> files = new ArrayList<>();
        for (int i = 0; i < announcementList.size(); i++) {
            files.add(announcementList.get(i));
        }
        mAdapter.replaceList(files);
        initLatest();
    }

    protected void bindLatest(BMXGroup.Announcement announcement) {
        if (announcement != null) {
            String title = announcement.getMTitle();
            String content = announcement.getMContent();
            if (!TextUtils.isEmpty(title)) {
                mTvLatestTitle.setVisibility(View.VISIBLE);
                mTvLatestTitle.setText(title);
            } else {
                mTvLatestTitle.setVisibility(View.GONE);
                mTvLatestTitle.setText("");
            }
            if (!TextUtils.isEmpty(content)) {
                mTvLatestContent.setVisibility(View.VISIBLE);
                mTvLatestContent.setText(content);
            } else {
                mTvLatestContent.setVisibility(View.GONE);
                mTvLatestContent.setText("");
            }
        }
    }

    private void editAnnocument(String title, String content) {
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            return;
        }
        showLoadingDialog(true);
        GroupManager.getInstance().editAnnouncement(mGroup, title, content, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                init();
            } else {
                toastError(bmxErrorCode);
            }
        });
    }

    private void deleteAnnocument(long announcementId) {
        showLoadingDialog(true);
        GroupManager.getInstance().deleteAnnouncement(mGroup, announcementId, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                init();
            } else {
                toastError(bmxErrorCode);
            }
        });
    }

    /**
     * 新增群公告
     */
    private void showAnnomentInfo() {
        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams editP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editP.setMargins(ScreenUtils.dp2px(10), ScreenUtils.dp2px(5), ScreenUtils.dp2px(10),
                ScreenUtils.dp2px(5));
        // 展示名称
        TextView name = new TextView(this);
        name.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        name.setTextColor(getResources().getColor(R.color.color_black));
        name.setBackgroundColor(getResources().getColor(R.color.color_white));
        name.setText("公告标题");
        ll.addView(name, textP);

        final EditText editName = new EditText(this);
        editName.setBackgroundResource(R.drawable.common_edit_corner_bg);
        editName.setPadding(ScreenUtils.dp2px(5), 0, ScreenUtils.dp2px(5), 0);
        editName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        editName.setTextColor(getResources().getColor(R.color.color_black));
        editName.setMinHeight(ScreenUtils.dp2px(40));
        ll.addView(editName, editP);

        // 展示名称
        TextView content = new TextView(this);
        content.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        content.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        content.setTextColor(getResources().getColor(R.color.color_black));
        content.setBackgroundColor(getResources().getColor(R.color.color_white));
        content.setText("公告内容");
        ll.addView(content, textP);

        final EditText editContent = new EditText(this);
        editContent.setBackgroundResource(R.drawable.common_edit_corner_bg);
        editContent.setPadding(ScreenUtils.dp2px(5), 0, ScreenUtils.dp2px(5), 0);
        editContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        editContent.setTextColor(getResources().getColor(R.color.color_black));
        editContent.setMinHeight(ScreenUtils.dp2px(40));
        ll.addView(editContent, editP);

        DialogUtils.getInstance().showCustomDialog(this, ll, getString(R.string.group_notice),
                getString(R.string.confirm), getString(R.string.cancel),
                new CommonCustomDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener() {
                        String name = editName.getEditableText().toString().trim();
                        String content = editContent.getEditableText().toString().trim();
                        editAnnocument(name, content);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    private void showOperate(final BMXGroup.Announcement announcement) {
        if (announcement == null) {
            return;
        }
        final CustomDialog dialog = new CustomDialog();
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // 删除
        TextView delete = new TextView(this);
        delete.setPadding(ScreenUtils.dp2px(15), 0, ScreenUtils.dp2px(15), 0);
        delete.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        delete.setTextColor(getResources().getColor(R.color.color_black));
        delete.setBackgroundColor(getResources().getColor(R.color.color_white));
        delete.setText(getString(R.string.delete));
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                deleteAnnocument(announcement.getMId());
            }
        });
        ll.addView(delete, params);
        dialog.setCustomView(ll);
        dialog.showDialog(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 展示群公告adapter
     */
    protected class ChatGroupAnnouncementAdapter
            extends RecyclerWithHFAdapter<BMXGroup.Announcement> {

        public ChatGroupAnnouncementAdapter(Context context) {
            super(context);
        }

        @Override
        protected int onCreateViewById(int viewType) {
            return R.layout.item_annocement_view;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            TextView title = holder.findViewById(R.id.tv_title);
            TextView content = holder.findViewById(R.id.tv_content);

            BMXGroup.Announcement announcement = getItem(position);
            if (announcement == null) {
                return;
            }
            title.setText(announcement.getMTitle());
            content.setText(announcement.getMContent());
        }
    }

    private void toastError(BMXErrorCode e) {
        String error = e != null ? e.name() : "网络异常";
        ToastUtil.showTextViewPrompt(error);
    }

}
