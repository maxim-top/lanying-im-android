
package top.maxim.im.contact.view;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import im.floo.floolib.BMXConversation;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXRosterItemList;
import im.floo.floolib.ListOfLongLong;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.ChatManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.bean.MessageBean;
import top.maxim.im.common.bean.TargetBean;
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.recyclerview.DividerItemDecoration;
import top.maxim.im.message.adapter.ForwardingSessionAdapter;
import top.maxim.im.message.utils.MessageConfig;

public class ForwardMsgActivity extends BaseTitleActivity {

    private RecyclerView mRecycler;

    private ForwardingSessionAdapter mAdapter;

    private MessageBean messageBean;

    private int messageForwardMaxUserNum;

    protected Map<Long, Boolean> mSelectedRosterItems = new HashMap<>();
    protected Map<Long, Boolean> mSelectedGroups = new HashMap<>();
    protected Map<Long, Boolean> mSelected = new HashMap<>();

    List<TargetBean> targetBeans = new ArrayList<>();
    Set<String> targetIds = new HashSet<>();

    public static void openForwardMsgRosterActivity(Activity context, MessageBean message, int requestCode) {
        Intent intent = new Intent(context, ForwardMsgActivity.class);
        intent.putExtra(MessageConfig.CHAT_MSG, message);
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        messageForwardMaxUserNum = CommonUtils.getAppConfigInteger("message_forward_max_user_num");
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(getString(R.string.chat_msg_relay)+"(0/"+messageForwardMaxUserNum+")");
        builder.setBackIcon(R.drawable.header_back_icon, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        builder.setRightText(R.string.confirm, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Long> chooseRosterItems = new ArrayList<>();
                for (Map.Entry<Long, Boolean> entry : mSelectedRosterItems.entrySet()) {
                    if (entry.getValue()) {
                        chooseRosterItems.add(entry.getKey());
                    }
                }
                ArrayList<Long> chooseRosterGroups = new ArrayList<>();
                for (Map.Entry<Long, Boolean> entry : mSelectedGroups.entrySet()) {
                    if (entry.getValue()) {
                        chooseRosterGroups.add(entry.getKey());
                    }
                }
                if (messageBean == null) {
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(MessageConfig.SELECTED_ROSTER_ITEMS, chooseRosterItems);
                intent.putExtra(MessageConfig.SELECTED_GROUPS, chooseRosterGroups);
                intent.putExtra(MessageConfig.CHAT_MSG, messageBean);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.fragment_contact, null);
        mRecycler = view.findViewById(R.id.contact_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.addItemDecoration(new DividerItemDecoration(this, R.color.guide_divider));
        mAdapter = initAdapter();
        mRecycler.setAdapter(mAdapter);
        return view;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            messageBean = (MessageBean)intent.getSerializableExtra(MessageConfig.CHAT_MSG);
        }
    }

    @Override
    protected void setViewListener() {
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TargetBean targetBean = mAdapter.getItem(position);
                if (targetBean == null) {
                    return;
                }
                long mId = targetBean.getId();
                Map<Long, Boolean> selected = mSelected;
                if (!selected.containsKey(mId) || !selected.get(mId)) {
                    if (mSelected.size() < messageForwardMaxUserNum){
                        selected.put(mId, true);
                    }else{
                        String error = String.format(getString(R.string.max_targets), messageForwardMaxUserNum);
                        ToastUtil.showTextViewPrompt(error);
                    }
                } else {
                    selected.remove(mId);
                }
                mHeader.setTitle(getString(R.string.chat_msg_relay) + "(" + mSelected.size() + "/"
                        + messageForwardMaxUserNum + ")");


                if (targetBean.getType() == BMXConversation.Type.Single){
                    selected = mSelectedRosterItems;
                }else if (targetBean.getType() == BMXConversation.Type.Group){
                    selected = mSelectedGroups;
                }
                if (!selected.containsKey(mId) || !selected.get(mId)) {
                    selected.put(mId, true);
                } else {
                    selected.remove(mId);
                }
                mAdapter.notifyItemChanged(position);
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
     * 刷新名称 头像
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


    protected void bindData(BMXRosterItemList itemList) {
        if (itemList != null && !itemList.isEmpty()) {
            for (int i = 0; i < itemList.size(); i++) {
                BMXRosterItem item = itemList.get(i);
                if (item == null) {
                    continue;
                }
                TargetBean bean = new TargetBean(item.rosterId(), BMXConversation.Type.Single);
                if (!targetIds.contains(String.valueOf(item.rosterId()))){
                    targetBeans.add(bean);
                    targetIds.add(String.valueOf(item.rosterId()));
                }
            }
            mAdapter.replaceList(targetBeans);
        }
    }

    private void loadSession() {
        // 获取所有会话
        ChatManager.getInstance().getAllConversations((bmxErrorCode, bmxConversationList) -> {
            List<BMXConversation> conversationList = new ArrayList<>();
            if (bmxConversationList != null && !bmxConversationList.isEmpty()) {
                for (int i = 0; i < bmxConversationList.size(); i++) {
                    BMXConversation conversation = bmxConversationList.get(i);
                    if (conversation != null) {
                        conversationList.add(conversation);
                    }
                }
            }
            if (!conversationList.isEmpty()) {
                sortSession(conversationList);
                for (BMXConversation conversation: conversationList){
                    targetIds.add(String.valueOf(conversation.conversationId()));
                    TargetBean bean = new TargetBean(conversation.conversationId(), conversation.type());
                    targetBeans.add(bean);
                }
                mAdapter.replaceList(targetBeans);
                notifySession(conversationList);
            }
            RosterManager.getInstance().get(false, (err, list) -> {
                dismissLoadingDialog();
                if (BaseManager.bmxFinish(err)) {
                    if (!list.isEmpty()) {
                        RosterManager.getInstance().getRosterList(list, true, (bmxErrorCode1, itemList) -> {
                            RosterFetcher.getFetcher().putRosters(itemList);
                            if (BaseManager.bmxFinish(bmxErrorCode1)) {
                                bindData(itemList);
                            }
                        });
                    }
                    GroupManager.getInstance().getGroupList(false, (e, l) -> {
                        dismissLoadingDialog();
                        if (!BaseManager.bmxFinish(e)) {
                            return;
                        }
                        List<BMXGroup> groupList = new ArrayList<>();
                        for (int i = 0; i < l.size(); i++) {
                            BMXGroup group = l.get(i);
                            groupList.add(group);
                            TargetBean bean = new TargetBean(group.groupId(), BMXConversation.Type.Group);
                            if (!targetIds.contains(String.valueOf(group.groupId()))){
                                targetBeans.add(bean);
                                targetIds.add(String.valueOf(group.groupId()));
                            }
                        }
                        mAdapter.replaceList(targetBeans);
                    });
                }
            });

        });
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        loadSession();
    }

    protected ForwardingSessionAdapter initAdapter() {
        return new ForwardingSessionAdapter(this, mSelected);
    }

}
