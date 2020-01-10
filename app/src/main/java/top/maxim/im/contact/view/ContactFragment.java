
package top.maxim.im.contact.view;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXRosterItemList;
import im.floo.floolib.BMXRosterServiceListener;
import im.floo.floolib.ListOfLongLong;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.ChatManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.base.BaseTitleFragment;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CustomDialog;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.DividerItemDecoration;
import top.maxim.im.contact.adapter.ContactAdapter;

/**
 * Description : 通讯录 Created by Mango on 2018/11/06
 */
public class ContactFragment extends BaseTitleFragment {

    private RecyclerView mRecycler;

    private ContactAdapter mAdapter;

    /* roster监听 */
    private BMXRosterServiceListener mRosterListener = new BMXRosterServiceListener() {
        @Override
        public void onFriendAdded(long sponsorId, long recipientId) {
            super.onFriendAdded(sponsorId, recipientId);
            // 添加好友
            initRoster(true);
        }

        @Override
        public void onFriendRemoved(long sponsorId, long recipientId) {
            super.onFriendRemoved(sponsorId, recipientId);
            // 删除好友
            initRoster(true);
        }

        @Override
        public void onApplied(long sponsorId, long recipientId, String message) {
            super.onApplied(sponsorId, recipientId, message);
            // 申请
        }

        @Override
        public void onApplicationAccepted(long sponsorId, long recipientId) {
            super.onApplicationAccepted(sponsorId, recipientId);
            // 申请被接受
        }

        @Override
        public void onApplicationDeclined(long sponsorId, long recipientId, String reason) {
            super.onApplicationDeclined(sponsorId, recipientId, reason);
            // 申请被拒绝
        }

        @Override
        public void onBlockListAdded(long sponsorId, long recipientId) {
            super.onBlockListAdded(sponsorId, recipientId);
            // 被加入黑名单
        }

        @Override
        public void onBlockListRemoved(long sponsorId, long recipientId) {
            super.onBlockListRemoved(sponsorId, recipientId);
            // 被移除黑名单
        }

        @Override
        public void onRosterInfoUpdate(BMXRosterItem item) {
            super.onRosterInfoUpdate(item);
            // roster有更新
            initRoster(true);
        }
    };

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(getActivity(), headerContainer);
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        hideHeader();
        View view = View.inflate(getActivity(), R.layout.fragment_contact, null);
        mRecycler = view.findViewById(R.id.contact_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycler
                .addItemDecoration(new DividerItemDecoration(getActivity(), R.color.guide_divider));
        mAdapter = new ContactAdapter(getActivity());
        mRecycler.setAdapter(mAdapter);
        RosterManager.getInstance().addRosterListener(mRosterListener);
        buildContactHeaderView();
        return view;
    }

    /**
     * 设置联系人headerView
     */
    private void buildContactHeaderView() {
        View headerView = View.inflate(getActivity(), R.layout.item_contact_header, null);
//        FrameLayout search = headerView.findViewById(R.id.fl_contact_header_search);
//        search.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ContactSearchActivity.openRosterSearch(getActivity());
//            }
//        });
        LinearLayout ll = headerView.findViewById(R.id.ll_contact_header);
        // 申请
        View applyView = View.inflate(getActivity(), R.layout.item_contact_view, null);
        applyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RosterApplyActivity.openRosterApply(getActivity());
            }
        });
        ((TextView)applyView.findViewById(R.id.contact_title))
                .setText(getString(R.string.contact_apply_notice));
        ShapeImageView icon = applyView.findViewById(R.id.contact_avatar);
        icon.setFrameStrokeWidth(0);
        icon.setImageResource(R.drawable.icon_apply_notice);
        ll.addView(applyView);

//        // 分割线
//        ItemLine.Builder itemLine = new ItemLine.Builder(getActivity(), ll)
//                .setMarginLeft(ScreenUtils.dp2px(15));
//        ll.addView(itemLine.build());
//
//        // 群组
//        View groupView = View.inflate(getActivity(), R.layout.item_contact_view, null);
//        groupView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                GroupListActivity.openGroup(getActivity());
//            }
//        });
//        ((TextView)groupView.findViewById(R.id.contact_title))
//                .setText(getString(R.string.contact_group));
//        ((ShapeImageView)groupView.findViewById(R.id.contact_avatar))
//                .setImageResource(R.drawable.icon_group);
//        ll.addView(groupView);
        mAdapter.addHeaderView(headerView);
    }

    @Override
    protected void setViewListener() {
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BMXRosterItem item = mAdapter.getItem(position);
                if (item == null) {
                    return;
                }
                RosterDetailActivity.openRosterDetail(getActivity(), item.rosterId());
            }
        });
        mAdapter.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                    long id) {
                BMXRosterItem item = mAdapter.getItem(position);
                if (item == null) {
                    return true;
                }
                showRemoveDialog(item, position);
                return true;
            }
        });
    }

    private void showRemoveDialog(final BMXRosterItem item, final int position) {
        final CustomDialog dialog = new CustomDialog();
        LinearLayout ll = new LinearLayout(getActivity());
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // 删除
        TextView delete = new TextView(getActivity());
        delete.setPadding(ScreenUtils.dp2px(15), 0, ScreenUtils.dp2px(15), 0);
        delete.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        delete.setTextColor(getResources().getColor(R.color.color_black));
        delete.setBackgroundColor(getResources().getColor(R.color.color_white));
        delete.setText(getString(R.string.delete));
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                removeRoster(item, position);
            }
        });
        ll.addView(delete, params);
        dialog.setCustomView(ll);
        dialog.showDialog(getActivity());
    }

    /**
     * 移除好友
     */
    private void removeRoster(final BMXRosterItem item, final int position) {
        if (item == null) {
            return;
        }
        showLoadingDialog(true);
        Observable.just(item).map(new Func1<BMXRosterItem, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXRosterItem rosterItem) {
                return RosterManager.getInstance().remove(rosterItem.rosterId());
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                ChatManager.getInstance().deleteConversation(item.rosterId(), true);
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
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        mAdapter.remove(position);
                    }
                });
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        initRoster(false);
    }

    @Override
    protected boolean isFullScreen() {
        return false;
    }

    @Override
    public void onShow() {
        if (mContentView != null) {
            initRoster(false);
        }
    }

    private void initRoster(boolean forceRefresh) {
        final ListOfLongLong listOfLongLong = new ListOfLongLong();
        final BMXRosterItemList itemList = new BMXRosterItemList();
        Observable.just(listOfLongLong).map(new Func1<ListOfLongLong, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(ListOfLongLong longs) {
                return RosterManager.getInstance().get(longs, forceRefresh);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).map(new Func1<BMXErrorCode, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXErrorCode errorCode) {
                if (!listOfLongLong.isEmpty()) {
                    BMXErrorCode errorCode1 = RosterManager.getInstance().search(listOfLongLong,
                            itemList, true);
                    RosterFetcher.getFetcher().putRosters(itemList);
                    return errorCode1;
                }
                return errorCode;
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
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (listOfLongLong.size() > 0) {
                            // 空的错误不提示
                            String error = e != null ? e.getMessage() : "网络错误";
                            ToastUtil.showTextViewPrompt(error);
                        }
                        RosterManager.getInstance().get(listOfLongLong, false);
                        List<BMXRosterItem> rosterItems = new ArrayList<>();
                        for (int i = 0; i < itemList.size(); i++) {
                            BMXRosterItem item = itemList.get(i);
                            // 是否是好友
                            BMXRosterItem.RosterRelation rosterRelation = item != null
                                    ? item.relation()
                                    : null;
                            boolean friend = rosterRelation == BMXRosterItem.RosterRelation.Friend;
                            if (friend) {
                                rosterItems.add(item);
                            }
                        }
                        RosterFetcher.getFetcher().putRosters(itemList);
                        mAdapter.replaceList(rosterItems);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        List<BMXRosterItem> rosterItems = new ArrayList<>();
                        for (int i = 0; i < itemList.size(); i++) {
                            BMXRosterItem item = itemList.get(i);
                            // 是否是好友
                            BMXRosterItem.RosterRelation rosterRelation = item != null
                                    ? item.relation()
                                    : null;
                            boolean friend = rosterRelation == BMXRosterItem.RosterRelation.Friend;
                            if (friend) {
                                rosterItems.add(item);
                            }
                        }
                        RosterFetcher.getFetcher().putRosters(itemList);
                        mAdapter.replaceList(rosterItems);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        setNull(mRecycler);
        super.onDestroyView();
    }
}
