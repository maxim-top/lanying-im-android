
package top.maxim.im.message.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXRosterItemList;
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
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseRecyclerAdapter;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.DividerItemDecoration;
import top.maxim.im.message.utils.ChatUtils;

/**
 * Description : 选择文件
 */
public class GroupAckActivity extends BaseTitleActivity {

    public static final String MEMBER_ID_LIST = "memberIdList";

    public static final String READ_ROSTER_ID_LIST = "readRosterIdList";

    private ListOfLongLong mReadIdList = new ListOfLongLong();

    private ListOfLongLong mUnReadIdList = new ListOfLongLong();

    private RecyclerView mRecyclerView;

    private TextView mTvReaded, mTvUnRead;

    private View mViewReaded, mViewUnRead;

    private ReadAckAdapter mAdapter;

    public static void openGroupAckActivity(Context context, List<Long> memberIdList,
            List<Long> readIdList) {
        Intent intent = new Intent(context, GroupAckActivity.class);
        intent.putExtra(MEMBER_ID_LIST, (Serializable)memberIdList);
        intent.putExtra(READ_ROSTER_ID_LIST, (Serializable)readIdList);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setBackIcon(R.drawable.header_back_icon, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        builder.setTitle(R.string.group_read_list);
        return builder.build();
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            List<Long> memberIdList = (List<Long>)intent.getSerializableExtra(MEMBER_ID_LIST);
            List<Long> readIdList = (List<Long>)intent.getSerializableExtra(READ_ROSTER_ID_LIST);
            if (memberIdList != null && !memberIdList.isEmpty()) {
                for (int i = 0; i < memberIdList.size(); i++) {
                    long id = memberIdList.get(i);
                    if (readIdList != null && readIdList.contains(id)) {
                        mReadIdList.add(id);
                    } else {
                        mUnReadIdList.add(id);
                    }
                }
            }
        }
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.activity_group_ack_view, null);
        mRecyclerView = view.findViewById(R.id.rcv_read_ack);
        mTvReaded = view.findViewById(R.id.tv_readed);
        mTvUnRead = view.findViewById(R.id.tv_unRead);
        mViewReaded = view.findViewById(R.id.tv_readed_selected);
        mViewUnRead = view.findViewById(R.id.tv_unRead_selected);
        mTvReaded.setSelected(true);
        mTvUnRead.setSelected(false);
        mViewReaded.setVisibility(View.VISIBLE);
        mViewUnRead.setVisibility(View.INVISIBLE);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new ReadAckAdapter(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, R.color.guide_divider));
        return view;
    }

    @Override
    protected void setViewListener() {
        super.setViewListener();
        mTvReaded.setOnClickListener(v -> {
            if (mTvReaded.isSelected()) {
                return;
            }
            mTvReaded.setSelected(true);
            mTvUnRead.setSelected(false);
            mViewReaded.setVisibility(View.VISIBLE);
            mViewUnRead.setVisibility(View.INVISIBLE);
            initRoster(mReadIdList, true);
        });
        mTvUnRead.setOnClickListener(v -> {
            if (mTvUnRead.isSelected()) {
                return;
            }
            mTvReaded.setSelected(false);
            mTvUnRead.setSelected(true);
            mViewReaded.setVisibility(View.INVISIBLE);
            mViewUnRead.setVisibility(View.VISIBLE);
            initRoster(mUnReadIdList, true);
        });
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        initRoster(mReadIdList, true);
    }

    private void initRoster(ListOfLongLong list, boolean forceRefresh) {
        if (list == null || list.isEmpty()) {
            mAdapter.removeAll();
            return;
        }
        showLoadingDialog(true);
        final BMXRosterItemList itemList = new BMXRosterItemList();
        Observable.just(list).map(new Func1<ListOfLongLong, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(ListOfLongLong longs) {
                return RosterManager.getInstance().search(longs, itemList, forceRefresh);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
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
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        List<BMXRosterItem> rosterItems = new ArrayList<>();
                        for (int i = 0; i < itemList.size(); i++) {
                            rosterItems.add(itemList.get(i));
                        }
                        RosterFetcher.getFetcher().putRosters(itemList);
                        mAdapter.replaceList(rosterItems);
                    }
                });
    }

    /**
     * 展示成员adapter
     */
    protected class ReadAckAdapter extends BaseRecyclerAdapter<BMXRosterItem> {

        private ImageRequestConfig mConfig;

        public ReadAckAdapter(Context context) {
            super(context);
            mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
                    .showImageForEmptyUri(R.drawable.default_avatar_icon)
                    .showImageOnFail(R.drawable.default_avatar_icon).cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .showImageOnLoading(R.drawable.default_avatar_icon).build();
        }

        @Override
        protected int onCreateViewById(int viewType) {
            return R.layout.item_group_list_member;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            ShapeImageView icon = holder.findViewById(R.id.img_icon);
            TextView tvName = holder.findViewById(R.id.txt_name);
            BMXRosterItem member = getItem(position);
            if (member == null) {
                return;
            }
            String name = "";
            if (!TextUtils.isEmpty(member.alias())) {
                name = member.alias();
            } else if (!TextUtils.isEmpty(member.nickname())) {
                name = member.nickname();
            } else {
                name = member.username();
            }
            tvName.setText(name);
            ChatUtils.getInstance().showRosterAvatar(member, icon, mConfig);
        }
    }
}
