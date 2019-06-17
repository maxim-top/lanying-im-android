
package top.maxim.im.message.view;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import im.floo.floolib.BMXChatServiceListener;
import im.floo.floolib.BMXConversation;
import im.floo.floolib.BMXConversationList;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroupList;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageList;
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
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.base.BaseTitleFragment;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CustomDialog;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.recyclerview.DividerItemDecoration;
import top.maxim.im.message.adapter.SessionAdapter;
import top.maxim.im.message.contract.SessionContract;

/**
 * Description : 消息列表 Created by Mango on 2018/11/06
 */
public class SessionFragment extends BaseTitleFragment implements SessionContract.View {

    private RecyclerView mRecyclerView;

    private SessionAdapter mAdapter;

    private SessionContract.Presenter mPresenter;

    private BMXChatServiceListener mListener = new BMXChatServiceListener() {

        @Override
        public void onStatusChanged(BMXMessage msg, BMXErrorCode error) {
        }

        @Override
        public void onAttachmentStatusChanged(BMXMessage msg, BMXErrorCode error, int percent) {
        }

        @Override
        public void onRecallStatusChanged(BMXMessage msg, BMXErrorCode error) {
        }

        @Override
        public void onReceive(BMXMessageList list) {
            // 收到消息
            loadSession();
        }

        @Override
        public void onReceiveSystemMessages(BMXMessageList list) {
            // 收到系统消息
            loadSession();
        }

        @Override
        public void onReceiveReadAcks(BMXMessageList list) {
        }

        @Override
        public void onReceiveDeliverAcks(BMXMessageList list) {
        }

        @Override
        public void onReceiveRecallMessages(BMXMessageList list) {
            // 收到撤回消息
            loadSession();
        }

        @Override
        public void onAttachmentUploadProgressChanged(BMXMessage msg, int percent) {
        }
    };

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(getActivity(), headerContainer);
        builder.setTitle(R.string.recent_chat);
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(getActivity(), R.layout.fragment_session, null);
        mRecyclerView = ((RecyclerView)view.findViewById(R.id.session_recycler));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter = new SessionAdapter(getActivity()));
        mRecyclerView
                .addItemDecoration(new DividerItemDecoration(getActivity(), R.color.guide_divider));
        ChatManager.getInstance().addChatListener(mListener);
        buildContactHeaderView();
        receiveRxBus();
        return view;
    }

    /**
     * 设置headerView
     */
    private void buildContactHeaderView() {
        View headerView = View.inflate(getActivity(), R.layout.item_contact_header, null);
        FrameLayout search = headerView.findViewById(R.id.fl_contact_header_search);
        search.setOnClickListener(v -> MessageSearchActivity.openMessageSearch(getActivity()));
        mAdapter.addHeaderView(headerView);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadSession();
    }

    private void loadSession() {
        Observable.just("").map(s -> ChatManager.getInstance().getAllConversations())
                .subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXConversationList>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BMXConversationList bmxConversationList) {
                        if (bmxConversationList != null && !bmxConversationList.isEmpty()) {
                            List<BMXConversation> conversationList = new ArrayList<>();
                            for (int i = 0; i < bmxConversationList.size(); i++) {
                                conversationList.add(bmxConversationList.get(i));
                            }
                            sortSession(conversationList);
                            mAdapter.replaceList(conversationList);
                            notifySession(conversationList);
                        }
                    }
                });
    }

    /**
     * 排序
     * 
     * @param conversationList list
     */
    private void sortSession(List<BMXConversation> conversationList) {
        if (conversationList == null || conversationList.isEmpty()) {
            return;
        }
        Collections.sort(conversationList, (o1, o2) -> {
            BMXMessage m1 = o1 == null || o1.lastMsg() == null ? null : o1.lastMsg();
            BMXMessage m2 = o2 == null || o2.lastMsg() == null ? null : o2.lastMsg();
            long o1Time = m1 == null ? -1 : m1.serverTimestamp();
            long o2Time = m2 == null ? -1 : m2.serverTimestamp();
            if (o1Time == o2Time) {
                return 0;
            }
            return o1Time > o2Time ? -1 : 1;
        });
    }

    /**
     * 刷新roster名称 头像
     * 
     * @param conversationList
     */
    private void notifySession(List<BMXConversation> conversationList) {
        if (conversationList == null || conversationList.isEmpty()) {
            return;
        }
        ListOfLongLong rosterIds = new ListOfLongLong();
        ListOfLongLong groupIds = new ListOfLongLong();
        for (int i = 0; i < conversationList.size(); i++) {
            BMXConversation conversation = conversationList.get(i);
            if (conversation == null) {
                continue;
            }
            if (conversation.type() == BMXConversation.Type.Single) {
                if (RosterFetcher.getFetcher().getRoster(conversation.conversationId()) == null) {
                    rosterIds.add(conversation.conversationId());
                }
            } else if (conversation.type() == BMXConversation.Type.Group) {
                if (RosterFetcher.getFetcher().getGroup(conversation.conversationId()) == null) {
                    groupIds.add(conversation.conversationId());
                }
            }
        }
        if (!rosterIds.isEmpty()) {
            Observable.just(rosterIds).map(new Func1<ListOfLongLong, BMXErrorCode>() {
                @Override
                public BMXErrorCode call(ListOfLongLong listOfLongLong) {
                    BMXRosterItemList itemList = new BMXRosterItemList();
                    BMXErrorCode errorCode = RosterManager.getInstance().search(listOfLongLong,
                            itemList, true);
                    if (errorCode == BMXErrorCode.NoError) {
                        RosterFetcher.getFetcher().putRosters(itemList);
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

                        }

                        @Override
                        public void onNext(BMXErrorCode errorCode) {
                            if (mAdapter != null) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
        if (!groupIds.isEmpty()) {
            Observable.just(groupIds).map(new Func1<ListOfLongLong, BMXErrorCode>() {
                @Override
                public BMXErrorCode call(ListOfLongLong listOfLongLong) {
                    BMXGroupList list = new BMXGroupList();
                    BMXErrorCode errorCode = GroupManager.getInstance().search(listOfLongLong, list,
                            false);
                    if (errorCode == BMXErrorCode.NoError) {
                        RosterFetcher.getFetcher().putGroups(list);
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

    protected void receiveRxBus() {
        RxBus.getInstance().toObservable(Intent.class).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Intent>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Intent intent) {
                        if (intent == null) {
                            return;
                        }
                        String action = intent.getAction();
                        if (TextUtils.equals(action, "onRosterInfoUpdate")) {
                            if (mAdapter != null) {
                                mAdapter.notifyDataSetChanged();
                            }
                        } else if (TextUtils.equals(action, "onGroupInfoUpdate")) {
                            if (mAdapter != null) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    @Override
    protected void setViewListener() {
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAdapter == null) {
                    return;
                }
                BMXConversation item = mAdapter.getItem(position);
                if (item == null) {
                    return;
                }
                BMXMessage.MessageType type = null;
                if (item.type() == BMXConversation.Type.Single) {
                    type = BMXMessage.MessageType.Single;
                } else if (item.type() == BMXConversation.Type.Group) {
                    type = BMXMessage.MessageType.Group;
                }
                if (type != null) {
                    ChatBaseActivity.startChatActivity(getActivity(), type, item.conversationId());
                }
            }
        });

        mAdapter.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                    long id) {
                showOperateSession(mAdapter.getItem(position), position);
                return true;
            }
        });
    }

    private void showOperateSession(final BMXConversation conversation, final int position) {

        final CustomDialog dialog = new CustomDialog();
        LinearLayout ll = new LinearLayout(getActivity());
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // 删除
        TextView name = new TextView(getActivity());
        name.setPadding(ScreenUtils.dp2px(15), 0, ScreenUtils.dp2px(15), 0);
        name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        name.setTextColor(getResources().getColor(R.color.color_black));
        name.setBackgroundColor(getResources().getColor(R.color.color_white));
        name.setText(getString(R.string.delete));
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Observable.just(conversation).map(new Func1<BMXConversation, BMXErrorCode>() {
                    @Override
                    public BMXErrorCode call(BMXConversation conversation) {
                        if (conversation == null) {
                            return null;
                        }
                        ChatManager.getInstance()
                                .deleteConversation(conversation.conversationId());
                        return BMXErrorCode.NoError;
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
                                mAdapter.remove(position);
                            }
                        });
            }
        });
        ll.addView(name, params);
        // 清空聊天记录
        TextView clear = new TextView(getActivity());
        clear.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), 0);
        clear.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        clear.setTextColor(getResources().getColor(R.color.color_black));
        clear.setBackgroundColor(getResources().getColor(R.color.color_white));
        clear.setText(getString(R.string.chat_clear_msg));
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Observable.just(conversation).map(new Func1<BMXConversation, BMXErrorCode>() {
                    @Override
                    public BMXErrorCode call(BMXConversation conversation) {
                        if (conversation == null) {
                            return null;
                        }
                        return conversation.removeAllMessages();
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
                                ToastUtil.showTextViewPrompt("清除失败");
                            }

                            @Override
                            public void onNext(BMXErrorCode errorCode) {
                                ToastUtil.showTextViewPrompt("清除成功");
                                loadSession();
                            }
                        });
            }
        });
        // TODO 清除聊天记录功能暂时不加 C++暂时没有对外提供
        // ll.addView(clear, params);

        dialog.setCustomView(ll);
        dialog.showDialog(getActivity());
    }

    @Override
    public void onDestroyView() {
        if (mPresenter != null) {
            mPresenter.onDestroyPresenter();
            setNull(mPresenter);
        }
        setNull(mRecyclerView);
        ChatManager.getInstance().removeChatListener(mListener);
        super.onDestroyView();
    }

    @Override
    public void setPresenter(SessionContract.Presenter presenter) {

    }
}
