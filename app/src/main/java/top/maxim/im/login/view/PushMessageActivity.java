
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXMessageList;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.PushManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.recyclerview.BaseRecyclerAdapter;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.message.utils.ChatRecyclerScrollListener;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description : Push消息 Created by Mango on 2018/11/21.
 */
public class PushMessageActivity extends BaseTitleActivity {

    private RecyclerView mRecyclerView;
    
    private PushMessageAdapter mAdapter;

    private ChatRecyclerScrollListener mScrollListener;

    public static void openPushMessageActivity(Context context) {
        Intent intent = new Intent(context, PushMessageActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.set_push_message);
        builder.setBackIcon(R.drawable.header_back_icon, v -> finish());
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.activity_push_message, null);
        mRecyclerView = view.findViewById(R.id.rcv_push_msg);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new PushMessageAdapter(this));
        return view;
    }

    @Override
    protected void setViewListener() {
        super.setViewListener();
        /* 上下拉刷新 */
        mRecyclerView.addOnScrollListener(mScrollListener = new ChatRecyclerScrollListener(
                (LinearLayoutManager)mRecyclerView.getLayoutManager()) {
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
                getPullDownPushMessages(getFirstMessageId(), offset);
            }

            @Override
            protected void onLoadPullUp(int offset) {
                super.onLoadPullUp(offset);
            }
        });
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        initPushMessage(0);
    }

    private void initPushMessage(final long msgId) {
        // 拉取历史消息
        BMXMessageList messageList = new BMXMessageList();
        PushManager.getInstance().loadLocalPushMessages(msgId, MessageConfig.DEFAULT_PAGE_SIZE,
                messageList, bmxErrorCode -> {
                    if (BaseManager.bmxFinish(bmxErrorCode)) {
                        if (!messageList.isEmpty()) {
                            List<BMXMessage> messages = new ArrayList<>();
                            for (int i = 0; i < messageList.size(); i++) {
                                messages.add(messageList.get(i));
                            }
                            showView(messages, false);
                        }
                    }
                });
    }

    private void getPullDownPushMessages(final long msgId, final int offset) {
        if (msgId < 0) {
            return;
        }
        BMXMessageList messageList = new BMXMessageList();
        PushManager.getInstance().loadLocalPushMessages(msgId, MessageConfig.DEFAULT_PAGE_SIZE,
                messageList, bmxErrorCode -> {
                    if (BaseManager.bmxFinish(bmxErrorCode)) {
                        if (!messageList.isEmpty()) {
                            List<BMXMessage> messages = new ArrayList<>();
                            for (int i = 0; i < messageList.size(); i++) {
                                messages.add(messageList.get(i));
                            }
                            showView(messages, true);
                        }
                    }
                });
    }
    
    private void showView(List<BMXMessage> list, boolean pullDown) {
        if (mScrollListener != null) {
            mScrollListener.resetLoadStatus();
        }
        if (list == null || !list.isEmpty()) {
            return;
        }
        if (pullDown) {
            mAdapter.addListAtStart(list);
        } else {
            mAdapter.replaceList(list);
        }
    }
    
    private long getFirstMessageId() {
        if (mAdapter == null) {
            return -1;
        }
        List<BMXMessage> list = mAdapter.getList();
        BMXMessage bean = null;
        if (list != null && list.size() > 0) {
            bean = list.get(0);
        }
        return bean.msgId();
    }

    private class PushMessageAdapter extends BaseRecyclerAdapter<BMXMessage> {

        public PushMessageAdapter(Context context) {
            super(context);
        }

        @Override
        protected int onCreateViewById(int viewType) {
            return R.layout.item_push_message;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            TextView tv = holder.findViewById(R.id.tv_push_msg);
            BMXMessage message = getItem(position);
            String content = message == null ? "" : message.content();
            tv.setText(content);
        }
    }
}
