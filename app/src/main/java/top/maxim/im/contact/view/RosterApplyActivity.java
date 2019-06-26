
package top.maxim.im.contact.view;

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

import im.floo.floolib.ApplicationPage;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXRosterItemList;
import im.floo.floolib.BMXRosterService;
import im.floo.floolib.BMXRosterServiceApplicationList;
import im.floo.floolib.ListOfLongLong;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.RosterManager;
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

/**
 * Description : 申请与通知 Created by Mango on 2018/11/06
 */
public class RosterApplyActivity extends BaseTitleActivity {

    private RecyclerView mRecycler;

    private ApplyAdapter mAdapter;

    private final int DEFAULT_PAGE_SIZE = 10;

    public static void openRosterApply(Context context) {
        Intent intent = new Intent(context, RosterApplyActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.contact_apply_notice);
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
    protected void setViewListener() {
    }

    @Override
    protected void initDataForActivity() {
        initData("");
    }

    private void initData(String cursor) {
        showLoadingDialog(true);
        final ApplicationPage ap = new ApplicationPage();
        Observable.just(cursor).map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                return RosterManager.getInstance().getApplicationList(ap, s, DEFAULT_PAGE_SIZE);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).map(new Func1<BMXErrorCode, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXErrorCode errorCode) {
                if (ap.result() != null && !ap.result().isEmpty()) {
                    ListOfLongLong listOfLongLong = new ListOfLongLong();
                    BMXRosterServiceApplicationList list = ap.result();
                    for (int i = 0; i < list.size(); i++) {
                        listOfLongLong.add(list.get(i).getMRosterId());
                    }
                    BMXRosterItemList itemList = new BMXRosterItemList();
                    BMXErrorCode errorCode1 = RosterManager.getInstance().search(listOfLongLong,
                            itemList, true);
                    RosterFetcher.getFetcher().putRosters(itemList);
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
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        BMXRosterServiceApplicationList list = ap.result();
                        String cursor = ap.cursor();
                        if (list != null && !list.isEmpty()) {
                            List<BMXRosterService.Application> applications = new ArrayList<>();
                            for (int i = 0; i < list.size(); i++) {
                                applications.add(list.get(i));
                            }
                            mAdapter.replaceList(applications);
                        }
                    }
                });
    }

    class ApplyAdapter extends BaseRecyclerAdapter<BMXRosterService.Application> {

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
            TextView reason = holder.findViewById(R.id.apply_reason);
            final BMXRosterService.Application item = getItem(position);
            if (item == null) {
                return;
            }
            BMXRosterItem rosterItem = RosterFetcher.getFetcher().getRoster(item.getMRosterId());
            ChatUtils.getInstance().showRosterAvatar(rosterItem, avatar, mConfig);
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

            title.setText(!TextUtils.isEmpty(name) ? name : "");

            BMXRosterService.ApplicationStatus applicationStatus = item.getMStatus();
            String statusDesc = "";
            if (applicationStatus != null) {
                if (applicationStatus == BMXRosterService.ApplicationStatus.Accepted) {
                    statusDesc = "已添加";
                    accept.setVisibility(View.INVISIBLE);
                } else if (applicationStatus == BMXRosterService.ApplicationStatus.Pending) {
                    accept.setVisibility(View.VISIBLE);
                } else if (applicationStatus == BMXRosterService.ApplicationStatus.Declined) {
                    statusDesc = "已拒绝";
                    accept.setVisibility(View.INVISIBLE);
                } else {
                    accept.setVisibility(View.INVISIBLE);
                }
            } else {
                accept.setVisibility(View.INVISIBLE);
            }
            status.setText(statusDesc);
            reason.setText(!TextUtils.isEmpty(item.getMReason()) ? item.getMReason() : "");
            // 处理通知
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHandleApply(item.getMRosterId());
                }
            });
        }

        private void showHandleApply(final long rosterId) {
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
                    acceptApply(rosterId);
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
                    showDeclineReason(rosterId);
                }
            });
            ll.addView(decline, params);
            dialog.setCustomView(ll);
            dialog.showDialog((Activity)mContext);
        }

        private void acceptApply(final long rosterId) {
            showLoadingDialog(true);
            Observable.just(rosterId).map(new Func1<Long, BMXErrorCode>() {
                @Override
                public BMXErrorCode call(Long aLong) {
                    return RosterManager.getInstance().accept(aLong);
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

        /**
         * 输入框弹出
         */
        private void showDeclineReason(final long rosterId) {
            DialogUtils.getInstance().showEditDialog((Activity)mContext, "拒绝原因",
                    getString(R.string.confirm), getString(R.string.cancel),
                    new CommonEditDialog.OnDialogListener() {
                        @Override
                        public void onConfirmListener(String content) {
                            declineApply(rosterId, content);
                        }

                        @Override
                        public void onCancelListener() {

                        }
                    });
        }

        private void declineApply(final long rosterId, final String reason) {
            if (TextUtils.isEmpty(reason)) {
                return;
            }
            showLoadingDialog(true);
            Observable.just(rosterId).map(new Func1<Long, BMXErrorCode>() {
                @Override
                public BMXErrorCode call(Long aLong) {
                    return RosterManager.getInstance().decline(aLong, reason);
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
