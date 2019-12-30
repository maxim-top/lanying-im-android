
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

import java.util.ArrayList;
import java.util.List;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupInvitationList;
import im.floo.floolib.BMXGroupList;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.GroupInvitaionPage;
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
import top.maxim.im.common.utils.dialog.CustomDialog;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseRecyclerAdapter;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.message.utils.ChatUtils;

/**
 * Description : 群聊邀请 Created by Mango on 2018/11/06
 */
public class GroupInviteActivity extends BaseTitleActivity {

    private RecyclerView mRecycler;

    private InviteAdapter mAdapter;

    private View mEmptyView;

    private final int DEFAULT_PAGE_SIZE = 10;

    public static void openGroupInvite(Context context) {
        Intent intent = new Intent(context, GroupInviteActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.group_invite);
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
        mEmptyView = view.findViewById(R.id.view_empty);
        mEmptyView.setVisibility(View.GONE);
        mRecycler = view.findViewById(R.id.group_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new InviteAdapter(this);
        mRecycler.setAdapter(mAdapter);
        return view;
    }

    @Override
    protected void setViewListener() {
    }

    @Override
    protected void initDataForActivity() {
        initData("");
    }

    private void initData(String cursor) {
        showLoadingDialog(true);
        final GroupInvitaionPage page = new GroupInvitaionPage();
        Observable.just(cursor).map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                return GroupManager.getInstance().getInvitationList(page, s, DEFAULT_PAGE_SIZE);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).map(new Func1<BMXErrorCode, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXErrorCode errorCode) {
                if (page.result() != null && !page.result().isEmpty()) {
                    ListOfLongLong listOfLongLong = new ListOfLongLong();
                    BMXGroupInvitationList list = page.result();
                    for (int i = 0; i < list.size(); i++) {
                        listOfLongLong.add(list.get(i).getMGroupId());
                    }
                    BMXGroupList itemList = new BMXGroupList();
                    BMXErrorCode errorCode1 = GroupManager.getInstance().search(listOfLongLong,
                            itemList, true);
                    if (errorCode1 == BMXErrorCode.NoError) {
                        RosterFetcher.getFetcher().putGroups(itemList);
                    }
                    return errorCode1;
                }
                return errorCode;
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
                        String error = e == null ? "网络错误" : e.getMessage();
                        ToastUtil.showTextViewPrompt(error);
                        mRecycler.setVisibility(View.GONE);
                        mEmptyView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        BMXGroupInvitationList list = page.result();
                        if (list != null && !list.isEmpty()) {
                            List<BMXGroup.Invitation> invitations = new ArrayList<>();
                            for (int i = 0; i < list.size(); i++) {
                                invitations.add(list.get(i));
                            }
                            mAdapter.replaceList(invitations);
                            mRecycler.setVisibility(View.VISIBLE);
                            mEmptyView.setVisibility(View.GONE);
                        } else {
                            mRecycler.setVisibility(View.GONE);
                            mEmptyView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    class InviteAdapter extends BaseRecyclerAdapter<BMXGroup.Invitation> {

        private ImageRequestConfig mConfig;

        public InviteAdapter(Context context) {
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
            return R.layout.item_apply_view;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            ShapeImageView avatar = holder.findViewById(R.id.apply_avatar);
            TextView title = holder.findViewById(R.id.apply_title);
            TextView status = holder.findViewById(R.id.apply_status);
            TextView accept = holder.findViewById(R.id.tv_accept);
            final BMXGroup.Invitation item = getItem(position);
            if (item == null) {
                return;
            }
            final BMXGroup groupItem = RosterFetcher.getFetcher().getGroup(item.getMGroupId());
            ChatUtils.getInstance().showGroupAvatar(groupItem, avatar, mConfig);
            title.setText(
                    groupItem != null && !TextUtils.isEmpty(groupItem.name()) ? groupItem.name()
                            : "");

            BMXRosterItem rosterItem = RosterFetcher.getFetcher().getRoster(item.getMInviterId());
            String name = "";
            if (rosterItem != null) {
                if (!TextUtils.isEmpty(rosterItem.alias())) {
                    name = rosterItem.alias();
                } else if (!TextUtils.isEmpty(rosterItem.nickname())) {
                    name = rosterItem.nickname();
                } else {
                    name = rosterItem.username();
                }
            }

            BMXGroup.InvitationStatus inviteStatus = item.getMStatus();
            String statusDesc = "";
            if (inviteStatus != null) {
                if (inviteStatus == BMXGroup.InvitationStatus.Accepted) {
                    statusDesc = "已添加";
                    accept.setVisibility(View.INVISIBLE);
                } else if (inviteStatus == BMXGroup.InvitationStatus.Pending) {
                    accept.setVisibility(View.VISIBLE);
                } else if (inviteStatus == BMXGroup.InvitationStatus.Declined) {
                    statusDesc = "已拒绝";
                    accept.setVisibility(View.INVISIBLE);
                } else {
                    accept.setVisibility(View.INVISIBLE);
                }
            } else {
                accept.setVisibility(View.INVISIBLE);
            }
            status.setText(statusDesc);
            // 处理通知
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHandleInvite(groupItem, item.getMInviterId());
                }
            });
        }

        private void showHandleInvite(final BMXGroup group, final long inviteId) {
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
                    acceptInvite(group, inviteId);
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
                    declineInvite(group, inviteId);
                }
            });
            ll.addView(decline, params);
            dialog.setCustomView(ll);
            dialog.showDialog((Activity)mContext);
        }

        private void acceptInvite(BMXGroup group, final long inviteId) {
            if (group == null || inviteId <= 0) {
                return;
            }
            showLoadingDialog(true);
            Observable.just(group).map(new Func1<BMXGroup, BMXErrorCode>() {
                @Override
                public BMXErrorCode call(BMXGroup aLong) {
                    return GroupManager.getInstance().acceptInvitation(aLong, inviteId);
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
                        }

                        @Override
                        public void onNext(BMXErrorCode errorCode) {
                            initData("");
                        }
                    });
        }

        private void declineInvite(BMXGroup group, final long inviteId) {
            if (group == null || inviteId <= 0) {
                return;
            }
            showLoadingDialog(true);
            Observable.just(group).map(new Func1<BMXGroup, BMXErrorCode>() {
                @Override
                public BMXErrorCode call(BMXGroup aLong) {
                    return GroupManager.getInstance().declineInvitation(aLong, inviteId);
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
                        }

                        @Override
                        public void onNext(BMXErrorCode errorCode) {
                            initData("");
                        }
                    });
        }
    }
}
