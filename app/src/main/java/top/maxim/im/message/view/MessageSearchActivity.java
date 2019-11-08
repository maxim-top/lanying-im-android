
package top.maxim.im.message.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import im.floo.floolib.BMXConversation;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageList;
import im.floo.floolib.BMXMessageListList;
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
import top.maxim.im.bmxmanager.ChatManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.TimeUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseRecyclerAdapter;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.DividerItemDecoration;
import top.maxim.im.message.utils.ChatRecyclerScrollListener;
import top.maxim.im.message.utils.ChatUtils;

/**
 * Description : 消息搜索 Created by Mango on 2018/11/06
 */
public class MessageSearchActivity extends BaseTitleActivity {

    private RecyclerView mRecycler;

    private EditText mSearch;

    private SearchAdapter mAdapter;

    private ChatRecyclerScrollListener mScrollListener;

    private static int DEFAULT_PAGE = 10;

    public static void openMessageSearch(Context context) {
        Intent intent = new Intent(context, MessageSearchActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.search);
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
        View view = View.inflate(this, R.layout.activity_group_search, null);
        mSearch = view.findViewById(R.id.search_group);
        mRecycler = view.findViewById(R.id.group_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.addItemDecoration(new DividerItemDecoration(this, R.color.guide_divider));
        mAdapter = new SearchAdapter(this);
        mRecycler.setAdapter(mAdapter);
        return view;
    }

    @Override
    protected void setViewListener() {
        mSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchMessage(mSearch.getEditableText().toString());
                    return true;
                }
                return false;
            }
        });

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
                String search = mSearch.getEditableText().toString();
                if (mAdapter != null && mAdapter.getItemCount() > 0) {
                    BMXMessage last = mAdapter.getItem(mAdapter.getItemCount() - 1);
                    if (last != null && last.serverTimestamp() > 0) {
                        searchMessageUp(search, last.serverTimestamp());
                    }
                }
            }
        });

        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAdapter == null) {
                    return;
                }
                BMXMessage item = mAdapter.getItem(position);
                if (item == null) {
                    return;
                }
                BMXMessage.MessageType type = item.type();
                if (type != null) {
                    ChatBaseActivity.startChatActivity(MessageSearchActivity.this, type,
                            item.conversationId());
                }
            }
        });
    }

    /**
     * 搜索
     * 
     * @param search 内容
     */
    private void searchMessage(String search) {
        if (TextUtils.isEmpty(search)) {
            return;
        }
        showLoadingDialog(true);
        final BMXMessageListList list = new BMXMessageListList();
        Observable.just(search).map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                return ChatManager.getInstance().searchMessages(s, 0, DEFAULT_PAGE, list);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).map(new Func1<BMXErrorCode, List<BMXMessage>>() {
            @Override
            public List<BMXMessage> call(BMXErrorCode errorCode) {
                if (!list.isEmpty()) {
                    List<BMXMessage> messages = new ArrayList<>();
                    for (int i = 0; i < list.size(); i++) {
                        BMXMessageList bmxMessageList = list.get(i);
                        if (bmxMessageList != null && !bmxMessageList.isEmpty()) {
                            for (int m = 0; m < bmxMessageList.size(); m++) {
                                messages.add(bmxMessageList.get(m));
                            }
                        }
                    }
                    return messages;
                }
                return null;
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<BMXMessage>>() {
                    @Override
                    public void onCompleted() {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onNext(List<BMXMessage> messages) {
                        isUpLoad(messages);
                        if (messages == null || messages.isEmpty()) {
                            mAdapter.removeAll();
                        } else {
                            mAdapter.replaceList(messages);
                            notifyMessage(messages);
                        }
                    }
                });
    }

    /**
     * 搜索上拉
     * 
     * @param search 内容
     */
    private void searchMessageUp(String search, final long refTime) {
        if (TextUtils.isEmpty(search)) {
            return;
        }
        showLoadingDialog(true);
        final BMXMessageListList list = new BMXMessageListList();
        Observable.just(search).map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                return ChatManager.getInstance().searchMessages(s, refTime, DEFAULT_PAGE, list,
                        BMXConversation.Direction.Down);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).map(new Func1<BMXErrorCode, List<BMXMessage>>() {
            @Override
            public List<BMXMessage> call(BMXErrorCode errorCode) {
                if (!list.isEmpty()) {
                    List<BMXMessage> messages = new ArrayList<>();
                    for (int i = 0; i < list.size(); i++) {
                        BMXMessageList bmxMessageList = list.get(i);
                        if (bmxMessageList != null && !bmxMessageList.isEmpty()) {
                            for (int m = 0; m < bmxMessageList.size(); m++) {
                                messages.add(bmxMessageList.get(m));
                            }
                        }
                    }
                    return messages;
                }
                return null;
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<BMXMessage>>() {
                    @Override
                    public void onCompleted() {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onNext(List<BMXMessage> messages) {
                        isUpLoad(messages);
                        if (messages != null && !messages.isEmpty()) {
                            mAdapter.addListAtEnd(messages);
                            notifyMessage(messages);
                        }
                    }
                });
    }

    /**
     * 刷新roster名称 头像
     */
    private void notifyMessage(List<BMXMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        ListOfLongLong rosterIds = new ListOfLongLong();
        for (int i = 0; i < messages.size(); i++) {
            BMXMessage message = messages.get(i);
            if (message == null) {
                continue;
            }
            rosterIds.add(message.fromId());
        }
        if (!rosterIds.isEmpty()) {
            Observable.just(rosterIds).map(new Func1<ListOfLongLong, BMXErrorCode>() {
                @Override
                public BMXErrorCode call(ListOfLongLong listOfLongLong) {
                    BMXRosterItemList itemList = new BMXRosterItemList();
                    BMXErrorCode errorCode = RosterManager.getInstance().search(listOfLongLong,
                            itemList, true);
                    RosterFetcher.getFetcher().putRosters(itemList);
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

                        }

                        @Override
                        public void onNext(BMXErrorCode errorCode) {
                            if (mAdapter != null) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }

    private void isUpLoad(List<BMXMessage> messages) {
        boolean isHasData = messages != null && messages.size() >= DEFAULT_PAGE;
        if (mScrollListener != null) {
            if (isHasData) {
                // 如果有更多数据 需要重置scrollListener滑动到底部加载数据回调
                mScrollListener.resetUpLoadStatus();
            } else {
                // 如果没有更多数据 需要关闭scrollListener滑动到底部加载数据回调
                mScrollListener.closeUpLoading();
            }
        }
    }
    
    class SearchAdapter extends BaseRecyclerAdapter<BMXMessage> {

        private ImageRequestConfig mConfig;

        public SearchAdapter(Context context) {
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
            return R.layout.item_session_view;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            ShapeImageView avatar = holder.findViewById(R.id.session_avatar);
            TextView tvTitle = holder.findViewById(R.id.session_title);
            TextView desc = holder.findViewById(R.id.session_desc);
            TextView time = holder.findViewById(R.id.session_time);
            BMXMessage message = getItem(position);
            if (message == null) {
                return;
            }
            String name = "";
            BMXRosterItem rosterItem = RosterFetcher.getFetcher().getRoster(message.fromId());
            if (rosterItem != null && !TextUtils.isEmpty(rosterItem.nickname())) {
                name = rosterItem.nickname();
            } else if (rosterItem != null) {
                name = rosterItem.username();
            }
            ChatUtils.getInstance().showRosterAvatar(rosterItem, avatar, mConfig);
            tvTitle.setText(TextUtils.isEmpty(name) ? "" : name);
            time.setText(TimeUtils.millis2String(message.serverTimestamp()));
            String msgDesc = ChatUtils.getInstance().getMessageDesc(message);
            desc.setText(!TextUtils.isEmpty(msgDesc) ? msgDesc : "");
        }
    }
}
