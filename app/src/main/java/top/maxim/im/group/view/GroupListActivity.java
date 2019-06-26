
package top.maxim.im.group.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupList;
import im.floo.floolib.BMXGroupService;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.ListOfLongLong;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonCustomDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ItemLineSwitch;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.RecyclerWithHFAdapter;
import top.maxim.im.contact.view.RosterChooseActivity;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.view.ChatBaseActivity;
import top.maxim.im.message.view.ChatGroupListMemberActivity;

/**
 * Description : 群 Created by Mango on 2018/11/21.
 */
public class GroupListActivity extends BaseTitleActivity {

    private RecyclerView mGroupView;

    private GroupAdapter mAdapter;

    private int CHOOSE_MEMBER_CODE = 1000;

    public static void openGroup(Context context) {
        Intent intent = new Intent(context, GroupListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.group);
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
        View view = View.inflate(this, R.layout.activity_group, null);
        mGroupView = view.findViewById(R.id.group_recycler);
        mGroupView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new GroupAdapter(this);
        mGroupView.setAdapter(mAdapter);
        buildContactHeaderView();
        return view;
    }

    @Override
    protected void setViewListener() {
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BMXGroup item = mAdapter.getItem(position);
                if (item == null) {
                    return;
                }
                ChatBaseActivity.startChatActivity(GroupListActivity.this,
                        BMXMessage.MessageType.Group, item.groupId());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getAllGroup();
    }

    /**
     * 设置联系人headerView
     */
    private void buildContactHeaderView() {
        View headerView = View.inflate(this, R.layout.item_contact_header, null);
        FrameLayout search = headerView.findViewById(R.id.fl_contact_header_search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupSearchActivity.openGroupSearch(GroupListActivity.this);
            }
        });
        LinearLayout ll = headerView.findViewById(R.id.ll_contact_header);
        // 群组
        View groupView = View.inflate(this, R.layout.item_contact_view, null);
        groupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RosterChooseActivity.startRosterListActivity(GroupListActivity.this, true, true,
                        CHOOSE_MEMBER_CODE);
            }
        });
        ((TextView)groupView.findViewById(R.id.contact_title))
                .setText(getString(R.string.create_group));
        ((ShapeImageView)groupView.findViewById(R.id.contact_avatar))
                .setImageResource(R.drawable.icon_group);
        ll.addView(groupView);

        // 邀请通知
        View inviteView = View.inflate(this, R.layout.item_contact_view, null);
        inviteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupInviteActivity.openGroupInvite(GroupListActivity.this);
            }
        });
        ((TextView)inviteView.findViewById(R.id.contact_title))
                .setText(getString(R.string.group_invite));
        ((ShapeImageView)inviteView.findViewById(R.id.contact_avatar))
                .setImageResource(R.drawable.icon_group);
        ll.addView(inviteView);
        mAdapter.addHeaderView(headerView);
    }

    private void getAllGroup() {
        final BMXGroupList list = new BMXGroupList();
        Observable.just(list).map(new Func1<BMXGroupList, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXGroupList bmxGroupList) {
                return GroupManager.getInstance().search(bmxGroupList, false);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        String error = e != null ? e.getMessage() : "网络错误";
                        ToastUtil.showTextViewPrompt(error);
                        GroupManager.getInstance().search(list, false);
                        List<BMXGroup> groupList = new ArrayList<>();
                        for (int i = 0; i < list.size(); i++) {
                            groupList.add(list.get(i));
                        }
                        RosterFetcher.getFetcher().putGroups(list);
                        mAdapter.replaceList(groupList);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        List<BMXGroup> groupList = new ArrayList<>();
                        for (int i = 0; i < list.size(); i++) {
                            groupList.add(list.get(i));
                        }
                        RosterFetcher.getFetcher().putGroups(list);
                        mAdapter.replaceList(groupList);
                    }
                });
    }

    /**
     * 创建群聊
     */
    private void showCreateGroup(final ListOfLongLong members) {
        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams editP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editP.setMargins(ScreenUtils.dp2px(10), ScreenUtils.dp2px(5), ScreenUtils.dp2px(10),
                ScreenUtils.dp2px(5));
        // 名称
        TextView name = new TextView(this);
        name.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        name.setTextColor(getResources().getColor(R.color.color_black));
        name.setBackgroundColor(getResources().getColor(R.color.color_white));
        name.setText(getString(R.string.group_name));
        ll.addView(name, textP);

        final EditText editName = new EditText(this);
        editName.setBackgroundResource(R.drawable.common_edit_corner_bg);
        editName.setPadding(ScreenUtils.dp2px(5), 0, ScreenUtils.dp2px(5), 0);
        editName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        editName.setTextColor(getResources().getColor(R.color.color_black));
        editName.setMinHeight(ScreenUtils.dp2px(40));
        ll.addView(editName, editP);

        // 描述
        TextView desc = new TextView(this);
        desc.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        desc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        desc.setTextColor(getResources().getColor(R.color.color_black));
        desc.setBackgroundColor(getResources().getColor(R.color.color_white));
        desc.setText(getString(R.string.group_desc));
        ll.addView(desc, textP);

        final EditText editDesc = new EditText(this);
        editDesc.setBackgroundResource(R.drawable.common_edit_corner_bg);
        editDesc.setPadding(ScreenUtils.dp2px(5), 0, ScreenUtils.dp2px(5), 0);
        editDesc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        editDesc.setTextColor(getResources().getColor(R.color.color_black));
        editDesc.setMinHeight(ScreenUtils.dp2px(40));
        ll.addView(editDesc, editP);

        // 公开
        final ItemLineSwitch.Builder isPublic = new ItemLineSwitch.Builder(this).setLeftText("是否公开")
                .setMarginTop(ScreenUtils.dp2px(15))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {

                    }
                });
        ll.addView(isPublic.build(), textP);

        DialogUtils.getInstance().showCustomDialog(this, ll, getString(R.string.create_group),
                getString(R.string.confirm), getString(R.string.cancel),
                new CommonCustomDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener() {
                        String name = editName.getEditableText().toString().trim();
                        String desc = editDesc.getEditableText().toString().trim();
                        boolean publicCheckStatus = isPublic.getCheckStatus();
                        createGroup(members, name, desc, publicCheckStatus);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 创建群聊
     */
    private void createGroup(ListOfLongLong members, String name, String desc,
            boolean publicCheckStatus) {
        if (TextUtils.isEmpty(name)) {
            ToastUtil.showTextViewPrompt("群聊名称不能为空");
            return;
        }
        BMXGroupService.CreateGroupOptions options = new BMXGroupService.CreateGroupOptions(name,
                desc, publicCheckStatus);
        options.setMMembers(members);
        final BMXGroup group = new BMXGroup();
        showLoadingDialog(true);
        Observable.just(options).map(new Func1<BMXGroupService.CreateGroupOptions, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXGroupService.CreateGroupOptions createGroupOptions) {
                return GroupManager.getInstance().create(createGroupOptions, group);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        String error = e != null ? e.getMessage() : "创建失败";
                        ToastUtil.showTextViewPrompt(error);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        ChatBaseActivity.startChatActivity(GroupListActivity.this,
                                BMXMessage.MessageType.Group, group.groupId());
                        finish();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_MEMBER_CODE && resultCode == RESULT_OK && data != null) {
            List<Long> chooseList = (List<Long>)data
                    .getSerializableExtra(ChatGroupListMemberActivity.CHOOSE_DATA);
            ListOfLongLong members = new ListOfLongLong();
            if (chooseList != null && chooseList.size() > 0) {
                for (Long id : chooseList) {
                    members.add(id);
                }
            }
            showCreateGroup(members);
        }
    }

    private class GroupAdapter extends RecyclerWithHFAdapter<BMXGroup> {

        private ImageRequestConfig mConfig;

        public GroupAdapter(Context context) {
            super(context);
            mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
                    .showImageForEmptyUri(R.drawable.default_group_icon)
                    .showImageOnFail(R.drawable.default_group_icon)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .showImageOnLoading(R.drawable.default_group_icon).build();
        }

        @Override
        protected int onCreateViewById(int viewType) {
            return R.layout.item_contact_view;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            ShapeImageView avatar = holder.findViewById(R.id.contact_avatar);
            TextView title = holder.findViewById(R.id.contact_title);
            BMXGroup bean = getItem(position);
            String name = bean == null ? "" : bean.name();
            title.setText(name);
            ChatUtils.getInstance().showGroupAvatar(bean, avatar, mConfig);
        }
    }
}
