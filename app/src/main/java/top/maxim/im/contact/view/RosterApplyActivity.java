
package top.maxim.im.contact.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXRosterService;
import im.floo.floolib.BMXRosterServiceApplicationList;
import im.floo.floolib.ListOfLongLong;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.CommonUtils;
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
import top.maxim.im.message.utils.ChatRecyclerScrollListener;
import top.maxim.im.message.utils.ChatUtils;

/**
 * Description : 申请与通知 Created by Mango on 2018/11/06
 */
public class RosterApplyActivity extends BaseTitleActivity {

    private RecyclerView mRecycler;

    private ApplyAdapter mAdapter;

    private View mEmptyView;

    private ChatRecyclerScrollListener mScrollListener;

    private String mCursor = "";

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
        mEmptyView = view.findViewById(R.id.view_empty);
        mEmptyView.setVisibility(View.GONE);
        mRecycler = view.findViewById(R.id.group_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ApplyAdapter(this);
        mRecycler.setAdapter(mAdapter);
        return view;
    }

    @Override
    protected void setViewListener() {
        /* 上下拉刷新 */
        mRecycler.addOnScrollListener(mScrollListener = new ChatRecyclerScrollListener(
                (LinearLayoutManager)mRecycler.getLayoutManager()) {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            protected void onLoadPullDown(int offset) {
                super.onLoadPullDown(offset);
            }

            @Override
            protected void onLoadPullUp(int offset) {
                super.onLoadPullUp(offset);
                initData(mCursor, true);
            }
        });
    }

    @Override
    protected void initDataForActivity() {
        initData("", false);
    }

    private void initData(String cursor, boolean upload) {
        showLoadingDialog(true);
        RosterManager.getInstance().getApplicationList(cursor, DEFAULT_PAGE_SIZE,
                (bmxErrorCode, ap) -> {
                    dismissLoadingDialog();
                    if (BaseManager.bmxFinish(bmxErrorCode)) {
                        BMXRosterServiceApplicationList list;
                        if (ap.result() != null && !ap.result().isEmpty()) {
                            mCursor = ap.cursor();
                            list = ap.result();
                        } else {
                            list = new BMXRosterServiceApplicationList();
                        }
                        ListOfLongLong listOfLongLong = new ListOfLongLong();
                        for (int i = 0; i < list.size(); i++) {
                            listOfLongLong.add(list.get(i).getMRosterId());
                        }
                        if (listOfLongLong.size() > 0){
                            RosterManager.getInstance().getRosterList(listOfLongLong, true,
                                    (bmxErrorCode1, itemList) -> {
                                        RosterFetcher.getFetcher().putRosters(itemList);
                                        if (BaseManager.bmxFinish(bmxErrorCode1)) {
                                            if (list != null && !list.isEmpty()) {
                                                List<BMXRosterService.Application> applications = new ArrayList<>();
                                                for (int i = 0; i < list.size(); i++) {
                                                    applications.add(list.get(i));
                                                }
                                                showList(applications, ap.cursor(), upload);
                                            } else {
                                                showList(null, "", upload);
                                            }
                                        } else {
                                            String error = bmxErrorCode1 == null ? getString(R.string.network_error) : bmxErrorCode1.name();
                                            ToastUtil.showTextViewPrompt(error);
                                            showList(null, "", upload);
                                        }
                                    });
                        }
                    }else{
                        String error = bmxErrorCode == null ? getString(R.string.network_error) : bmxErrorCode.name();
                        ToastUtil.showTextViewPrompt(error);
                        showList(null, "", upload);
                    }
                });
    }

    /**
     * 展示数据
     */
    private void showList(List<BMXRosterService.Application> applications, String cursor, boolean upload) {
        if (applications == null || applications.isEmpty()) {
            if(!upload){
                mAdapter.removeAll();
                mRecycler.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            }
            return;
        }
        mCursor = cursor;
        if (upload) {
            mAdapter.addListAtEnd(applications);
        } else {
            mAdapter.replaceList(applications);
        }
        mScrollListener.resetUpLoadStatus();
        mRecycler.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
    }

    class ApplyAdapter extends BaseRecyclerAdapter<BMXRosterService.Application> {

        private ImageRequestConfig mConfig;

        public ApplyAdapter(Context context) {
            super(context);
            mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
                    .showImageForEmptyUri(R.drawable.default_avatar_icon)
                    .showImageOnFail(R.drawable.default_avatar_icon).cacheOnDisk(true)
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
            String name = CommonUtils.getRosterDisplayName(rosterItem);
            title.setText(!TextUtils.isEmpty(name) ? name : "");

            BMXRosterService.ApplicationStatus applicationStatus = item.getMStatus();
            String statusDesc = "";
            if (applicationStatus != null) {
                if (applicationStatus == BMXRosterService.ApplicationStatus.Accepted) {
                    statusDesc = getString(R.string.added);
                    accept.setVisibility(View.INVISIBLE);
                } else if (applicationStatus == BMXRosterService.ApplicationStatus.Pending) {
                    accept.setVisibility(View.VISIBLE);
                } else if (applicationStatus == BMXRosterService.ApplicationStatus.Declined) {
                    statusDesc = getString(R.string.rejected);
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
            accept.setText(getString(R.string.accept));
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
            decline.setText(getString(R.string.reject));
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
            RosterManager.getInstance().accept(rosterId, bmxErrorCode -> {
                dismissLoadingDialog();
                if (BaseManager.bmxFinish(bmxErrorCode)) {
                    initData("", false);
                } else {
                    String error = bmxErrorCode != null ? bmxErrorCode.name() : getString(R.string.network_error);
                    ToastUtil.showTextViewPrompt(error);
                }
            });
        }

        /**
         * 输入框弹出
         */
        private void showDeclineReason(final long rosterId) {
            DialogUtils.getInstance().showEditDialog((Activity)mContext, getString(R.string.reason_for_rejection),
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
            RosterManager.getInstance().decline(rosterId, reason, bmxErrorCode -> {
                dismissLoadingDialog();
                if (BaseManager.bmxFinish(bmxErrorCode)) {
                    initData("", false);
                } else {
                    String error = bmxErrorCode != null ? bmxErrorCode.name() : getString(R.string.network_error);
                    ToastUtil.showTextViewPrompt(error);
                }
            });
        }
    }
}
