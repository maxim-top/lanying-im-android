
package top.maxim.im.message.view;

import android.content.Intent;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import im.floo.floolib.BMXChatServiceListener;
import im.floo.floolib.BMXConversation;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageList;
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
import top.maxim.im.common.utils.CommonConfig;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CustomDialog;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.recyclerview.DividerItemDecoration;
import top.maxim.im.message.adapter.SessionAdapter;
import top.maxim.im.message.contract.SessionContract;
import top.maxim.im.scan.view.ScannerActivity;

/**
 * Description : 消息列表 Created by Mango on 2018/11/06
 */
public class SessionFragment extends BaseTitleFragment implements SessionContract.View {

    private RecyclerView mRecyclerView;

    private SessionAdapter mAdapter;

    private View mEmptyView;

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
            loadSession();
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
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        hideTitleHeader();
        View view = View.inflate(getActivity(), R.layout.fragment_session, null);
        mRecyclerView = view.findViewById(R.id.session_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter = new SessionAdapter(getActivity()));
        mRecyclerView
                .addItemDecoration(new DividerItemDecoration(getActivity(), R.color.guide_divider));
        ChatManager.getInstance().addChatListener(mListener);
        buildContactHeaderView();
        buildFooterView();
        receiveRxBus();
        return view;
    }

    /**
     * 设置headerView
     */
    private void buildContactHeaderView() {
        View headerView = View.inflate(getActivity(), R.layout.session_header, null);
        headerView.findViewById(R.id.iv_session_search)
                .setOnClickListener(v -> MessageSearchActivity.openMessageSearch(getActivity()));
        headerView.findViewById(R.id.iv_session_scan)
                .setOnClickListener(v -> ScannerActivity.openScan(getActivity()));
        mAdapter.addHeaderView(headerView);
    }

    /**
     * 设置headerView
     */
    private void buildFooterView() {
        mEmptyView = View.inflate(getActivity(), R.layout.view_empty, null);
        mAdapter.addFooterView(mEmptyView);
        mEmptyView.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadSession();
    }

    private void loadSession() {
        // 获取所有未读数
        ChatManager.getInstance().getAllConversationsUnreadCount((bmxErrorCode, count) -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                Intent intent = new Intent();
                intent.setAction(CommonConfig.SESSION_COUNT_ACTION);
                intent.putExtra(CommonConfig.TAB_COUNT, count == null ? 0 : count);
                RxBus.getInstance().send(intent);
            }
        });
        // 获取所有会话
        ChatManager.getInstance().getAllConversations((bmxErrorCode, bmxConversationList) -> {
            List<BMXConversation> conversationList = new ArrayList<>();
            if (bmxConversationList != null && !bmxConversationList.isEmpty()) {
                for (int i = 0; i < bmxConversationList.size(); i++) {
                    BMXConversation conversation = bmxConversationList.get(i);
                    if (conversation != null && conversation.conversationId() > 0) {
                        conversationList.add(conversation);
                    }
                }
            }
            if (!conversationList.isEmpty()) {
                showEmpty(false);
                sortSession(conversationList);
                mAdapter.replaceList(conversationList);
                notifySession(conversationList);
            } else {
                showEmpty(true);
            }
        });
    }

    private void showEmpty(boolean empty) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)mEmptyView.getLayoutParams();
        if (empty) {
            mEmptyView.setVisibility(View.VISIBLE);
            params.width = RecyclerView.LayoutParams.MATCH_PARENT;
            params.height = RecyclerView.LayoutParams.MATCH_PARENT;
        } else {
            mEmptyView.setVisibility(View.GONE);
            params.width = 0;
            params.height = 0;
        }
        mEmptyView.setLayoutParams(params);
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
            RosterManager.getInstance().getRosterList(rosterIds, true, (bmxErrorCode, itemList) -> {
                if (BaseManager.bmxFinish(bmxErrorCode)) {
                    RosterFetcher.getFetcher().putRosters(itemList);
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
        if (!groupIds.isEmpty()) {
            GroupManager.getInstance().getGroupList(groupIds, true, (bmxErrorCode, itemList) -> {
                if (BaseManager.bmxFinish(bmxErrorCode)) {
                    RosterFetcher.getFetcher().putGroups(itemList);
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
                long conversationId = item.conversationId();
                if (conversationId == 1) {
                    ToastUtil.showTextViewPrompt("推送发出的消息");
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
                        ChatManager.getInstance().deleteConversation(conversation.conversationId(),
                                true);
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
                                showEmpty(mAdapter.getItemCount() <= 2);
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
                if (conversation != null) {
                    conversation.removeAllMessages(bmxErrorCode -> {
                        if (BaseManager.bmxFinish(bmxErrorCode)) {
                            ToastUtil.showTextViewPrompt("清除成功");
                            loadSession();
                        } else {
                            ToastUtil.showTextViewPrompt("清除失败");
                        }
                    });
                } else {
                    ToastUtil.showTextViewPrompt("清除失败");
                }
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
