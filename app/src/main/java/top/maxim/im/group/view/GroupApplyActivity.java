
package top.maxim.im.group.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupList;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.GroupApplicationPage;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.CustomDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseRecyclerAdapter;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description : 群聊申请 Created by Mango on 2018/11/06
 */
public class GroupApplyActivity extends BaseTitleActivity {

    private RecyclerView mRecycler;

    private ApplyAdapter mAdapter;

    private final int DEFAULT_PAGE_SIZE = 10;

    private long mGroupId;

    private BMXGroup mGroup = new BMXGroup();

    public static void openGroupApply(Context context, long groupId) {
        Intent intent = new Intent(context, GroupApplyActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.group_apply);
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
        mRecycler = view.findViewById(R.id.group_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ApplyAdapter(this);
        mRecycler.setAdapter(mAdapter);
        return view;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mGroupId = intent.getLongExtra(MessageConfig.CHAT_ID, 0);
        }
    }

    @Override
    protected void setViewListener() {
    }

    @Override
    protected void initDataForActivity() {
        GroupManager.getInstance().search(mGroupId, true, (bmxErrorCode, bmxGroup) -> {
            if (bmxGroup != null) {
                mGroup = bmxGroup;
            }
            initData("");
        });
    }

    private void initData(String cursor) {
        if (mGroup == null || mGroup.groupId() <= 0) {
            return;
        }
        showLoadingDialog(true);
        final GroupApplicationPage page = new GroupApplicationPage();
        final BMXGroupList list = new BMXGroupList();
        list.add(mGroup);
//        Observable.just(cursor).map(new Func1<String, BMXErrorCode>() {
//            @Override
//            public BMXErrorCode call(String s) {
//                return GroupManager.getInstance().getApplicationList(list, page, s,
//                        DEFAULT_PAGE_SIZE);
//            }
//        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
//            @Override
//            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
//                return BaseManager.bmxFinish(errorCode, errorCode);
//            }
//        }).map(new Func1<BMXErrorCode, BMXErrorCode>() {
//            @Override
//            public BMXErrorCode call(BMXErrorCode errorCode) {
//                if (page.result() != null && !page.result().isEmpty()) {
//                    ListOfLongLong listOfLongLong = new ListOfLongLong();
//                    BMXGroupApplicationList list = page.result();
//                    for (int i = 0; i < list.size(); i++) {
//                        listOfLongLong.add(list.get(i).getMApplicationId());
//                    }
//                    BMXRosterItemList itemList = new BMXRosterItemList();
//                    BMXErrorCode errorCode1 = RosterManager.getInstance().search(listOfLongLong,
//                            itemList, true);
//                    RosterFetcher.getFetcher().putRosters(itemList);
//                    return errorCode1;
//                }
//                return errorCode;
//            }
//        }).map(new Func1<BMXErrorCode, BMXErrorCode>() {
//            @Override
//            public BMXErrorCode call(BMXErrorCode errorCode) {
//                if (page.result() != null && !page.result().isEmpty()) {
//                    ListOfLongLong listOfLongLong = new ListOfLongLong();
//                    BMXGroupApplicationList list = page.result();
//                    for (int i = 0; i < list.size(); i++) {
//                        listOfLongLong.add(list.get(i).getMGroupId());
//                    }
//                    BMXGroupList itemList = new BMXGroupList();
//                    BMXErrorCode errorCode1 = GroupManager.getInstance().search(listOfLongLong,
//                            itemList, true);
//                    if (errorCode1 == BMXErrorCode.NoError) {
//                        RosterFetcher.getFetcher().putGroups(itemList);
//                    }
//                    return errorCode1;
//                }
//                return errorCode;
//            }
//        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<BMXErrorCode>() {
//                    @Override
//                    public void onCompleted() {
//                        dismissLoadingDialog();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        dismissLoadingDialog();
//                        String error = e == null ? "网络错误" : e.getMessage();
//                        ToastUtil.showTextViewPrompt(error);
//                    }
//
//                    @Override
//                    public void onNext(BMXErrorCode errorCode) {
//                        BMXGroupApplicationList list = page.result();
//                        if (list != null && !list.isEmpty()) {
//                            List<BMXGroup.Application> applications = new ArrayList<>();
//                            for (int i = 0; i < list.size(); i++) {
//                                applications.add(list.get(i));
//                            }
//                            mAdapter.replaceList(applications);
//                        }
//                    }
//                });
    }

    class ApplyAdapter extends BaseRecyclerAdapter<BMXGroup.Application> {

        private ImageRequestConfig mConfig;

        public ApplyAdapter(Context context) {
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
            return R.layout.item_apply_view;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            ShapeImageView avatar = holder.findViewById(R.id.apply_avatar);
            TextView title = holder.findViewById(R.id.apply_title);
            TextView status = holder.findViewById(R.id.apply_status);
            TextView accept = holder.findViewById(R.id.tv_accept);
            final BMXGroup.Application item = getItem(position);
            if (item == null) {
                return;
            }
            final BMXRosterItem rosterItem = RosterFetcher.getFetcher()
                    .getRoster(item.getMApplicationId());
            final BMXGroup groupItem = RosterFetcher.getFetcher()
                    .getGroup(item.getMGroupId());
            ChatUtils.getInstance().showRosterAvatar(rosterItem, avatar, mConfig);
            String name = "";
            if (!TextUtils.isEmpty(rosterItem.alias())) {
                name = rosterItem.alias();
            } else if (!TextUtils.isEmpty(rosterItem.nickname())) {
                name = rosterItem.nickname();
            } else {
                name = rosterItem.username();
            }
            title.setText(name);

            BMXGroup.ApplicationStatus inviteStatus = item.getMStatus();
            String statusDesc = "";
            if (inviteStatus != null) {
                if (inviteStatus == BMXGroup.ApplicationStatus.Accepted) {
                    statusDesc = "已添加";
                    accept.setVisibility(View.INVISIBLE);
                } else if (inviteStatus == BMXGroup.ApplicationStatus.Pending) {
                    statusDesc = "未处理";
                    accept.setVisibility(View.VISIBLE);
                } else if (inviteStatus == BMXGroup.ApplicationStatus.Declined) {
                    statusDesc = "已拒绝";
                    accept.setVisibility(View.INVISIBLE);
                } else {
                    accept.setVisibility(View.INVISIBLE);
                }
            } else {
                accept.setVisibility(View.INVISIBLE);
            }
            String reason = !TextUtils.isEmpty(item.getMReason()) ? item.getMReason() : "";
            if (!TextUtils.isEmpty(reason)) {
                statusDesc = statusDesc + "(" + reason + ")";
            }
            status.setText(statusDesc);
            // 处理通知
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHandleApply(groupItem, item.getMApplicationId());
                }
            });
        }

        private void showHandleApply(final BMXGroup group, final long applicationId) {
            final CustomDialog dialog = new CustomDialog();
            LinearLayout ll = new LinearLayout(mContext);
            ll.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // 接受
            TextView accept = new TextView(mContext);
            accept.setPadding(ScreenUtils.dp2px(15), 0, ScreenUtils.dp2px(15), 0);
            accept.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            accept.setTextColor(mContext.getResources().getColor(R.color.color_black));
            accept.setBackgroundColor(mContext.getResources().getColor(R.color.color_white));
            accept.setText("接受");
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    acceptApply(group, (long)applicationId);
                }
            });
            ll.addView(accept, params);
            // 拒绝
            TextView decline = new TextView(mContext);
            decline.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                    0);
            decline.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            decline.setTextColor(mContext.getResources().getColor(R.color.color_black));
            decline.setBackgroundColor(mContext.getResources().getColor(R.color.color_white));
            decline.setText("拒绝");
            decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    showDeclineReason(group, (int)applicationId);
                }
            });
            ll.addView(decline, params);
            dialog.setCustomView(ll);
            dialog.showDialog((Activity)mContext);
        }

        private void acceptApply(BMXGroup group, final long applicantId) {
            if (group == null || applicantId <= 0) {
                return;
            }
            showLoadingDialog(true);
            GroupManager.getInstance().acceptApplication(group, applicantId, bmxErrorCode -> {
                dismissLoadingDialog();
                if (BaseManager.bmxFinish(bmxErrorCode)) {
                    initData("");
                } else {
                    toastError(bmxErrorCode);
                }
            });
        }

        /**
         * 输入框弹出
         */
        private void showDeclineReason(final BMXGroup group, final int applicationId) {
            DialogUtils.getInstance().showEditDialog((Activity)mContext, "拒绝入群",
                    getString(R.string.confirm), getString(R.string.cancel),
                    new CommonEditDialog.OnDialogListener() {
                        @Override
                        public void onConfirmListener(String content) {
                            declineApply(group, applicationId, content);
                        }

                        @Override
                        public void onCancelListener() {

                        }
                    });
        }

        private void declineApply(BMXGroup group, int applicantId, String reason) {
            if (group == null || applicantId <= 0 || TextUtils.isEmpty(reason)) {
                return;
            }
            showLoadingDialog(true);
            GroupManager.getInstance().declineApplication(group, applicantId, reason, bmxErrorCode -> {
                dismissLoadingDialog();
                if (BaseManager.bmxFinish(bmxErrorCode)) {
                    initData("");
                } else {
                    toastError(bmxErrorCode);
                }
            });
        }
    }

    private void toastError(BMXErrorCode e) {
        String error = e != null ? e.name() : "网络异常";
        ToastUtil.showTextViewPrompt(error);
    }
}
