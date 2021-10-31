
package top.maxim.im.message.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.floo.BMXDataCallBack;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupMemberList;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.ListOfLongLong;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.BMImageLoader;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseRecyclerAdapter;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.DividerItemDecoration;
import top.maxim.im.message.utils.ChatRecyclerScrollListener;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description : 群聊成员 Created by Mango on 2018/11/25.
 */
public class ChatGroupListMemberActivity extends BaseTitleActivity {

    /* 群聊成员gridView */
    protected RecyclerView mGvGroupMember;

    /* 群聊成员adapter */
    protected ChatGroupMemberAdapter mAdapter;

    protected View mEmptyView;

    protected long mGroupId;

    private boolean mChoose = false;

    protected BMXGroup mGroup = new BMXGroup();

    protected Map<Long, Boolean> mSelected = new HashMap<>();

    protected static final String CHOOSE = "choose";

    public static final String CHOOSE_DATA = "chooseData";

    //是否有分页拉取
    private boolean mHasPageLoad;

    protected ChatRecyclerScrollListener mScrollListener;

    protected String mCursor = "";

    protected final int DEFAULT_PAGE_SIZE = 10;

    public static void startGroupMemberListActivity(Activity context, long groupId,
            boolean isChoose, int requestCode) {
        Intent intent = new Intent(context, ChatGroupListMemberActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, groupId);
        intent.putExtra(CHOOSE, isChoose);
        context.startActivityForResult(intent, requestCode);
    }

    public static void startGroupMemberListActivity(Activity context, long groupId,
            int requestCode) {
        startGroupMemberListActivity(context, groupId, false, requestCode);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.group_member);
        if (mChoose) {
            builder.setRightText(R.string.confirm, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<Long> chooseList = new ArrayList<>();
                    for (Map.Entry<Long, Boolean> entry : mSelected.entrySet()) {
                        if (entry.getValue()) {
                            chooseList.add(entry.getKey());
                        }
                    }
                    Intent intent = new Intent();
                    intent.putExtra(CHOOSE_DATA, chooseList);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            });
        }
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
        View view = View.inflate(this, R.layout.chat_group_list_member_view, null);
        mEmptyView = view.findViewById(R.id.view_empty);
        mEmptyView.setVisibility(View.GONE);
        mGvGroupMember = view.findViewById(R.id.gv_chat_group_member);
        mGvGroupMember.setLayoutManager(new LinearLayoutManager(this));
        mGvGroupMember.addItemDecoration(new DividerItemDecoration(this, R.color.guide_divider));
        mGvGroupMember.setAdapter(mAdapter = new ChatGroupMemberAdapter(this));
        mAdapter.setShowCheck(mChoose);
        mHasPageLoad = hasPageLoad();
        return view;
    }

    //是否有分页拉取 默认有
    protected boolean hasPageLoad(){
        return true;
    }

    @Override
    protected void setViewListener() {
        /* 上下拉刷新 */
        if(mHasPageLoad){
            mGvGroupMember.addOnScrollListener(mScrollListener = new ChatRecyclerScrollListener(
                    (LinearLayoutManager)mGvGroupMember.getLayoutManager()) {
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
                    init(mCursor, true);
                }
            });
        }
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BMXGroup.Member member = mAdapter.getItem(position);
                if (member == null) {
                    return;
                }
                long mId = member.getMUid();
                if (!mAdapter.getShowCheck() || mId == MessageConfig.MEMBER_ADD
                        || mId == MessageConfig.MEMBER_REMOVE) {
                    return;
                }
                if (!mSelected.containsKey(mId) || !mSelected.get(mId)) {
                    mSelected.put(mId, true);
                } else {
                    mSelected.remove(mId);
                }
                mAdapter.notifyItemChanged(position);
            }
        });
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mGroupId = intent.getLongExtra(MessageConfig.CHAT_ID, 0);
            mChoose = intent.getBooleanExtra(CHOOSE, false);
        }
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        initGroupInfo();
    }

    private void initGroupInfo() {
        showLoadingDialog(true);
        GroupManager.getInstance().getGroupList(mGroupId, false, (bmxErrorCode, bmxGroup) -> {
            dismissLoadingDialog();
            if (bmxGroup != null) {
                mGroup = bmxGroup;
            }
            if (!BaseManager.bmxFinish(bmxErrorCode)) {
                toastError(bmxErrorCode);
            }
            init();
        });
    }

    protected void init(){
        init("", false);
    }

    private void init(String cursor, boolean upload) {
        if (mGroup == null) {
            return;
        }
        showLoadingDialog(true);
        BMXDataCallBack<BMXGroupMemberList> callBack = (bmxErrorCode, list) -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                if (!list.isEmpty()) {
                    ListOfLongLong listOfLongLong = new ListOfLongLong();
                    for (int i = 0; i < list.size(); i++) {
                        listOfLongLong.add(list.get(i).getMUid());
                    }
                    RosterManager.getInstance().getRosterList(listOfLongLong, true,
                            (bmxErrorCode1, itemList) -> {
                                RosterFetcher.getFetcher().putRosters(itemList);
                                bindData(list, upload);
                            });
                } else {
                    bindData(list, upload);
                }
            } else {
                toastError(bmxErrorCode);
                initData(false, (bmxErrorCode1, list1) ->
                        bindData(list1, false));
            }
        };
        if (mHasPageLoad) {
            initData(cursor, callBack);
        } else {
            initData(true, callBack);
        }
    }

    protected BMXDataCallBack<BMXGroupMemberList> handleCallBackData(BMXErrorCode errorCode,
            BMXGroupMemberList list) {
        BMXGroupMemberList memberListTmp = new BMXGroupMemberList();
        long myId = SharePreferenceUtils.getInstance().getUserId();
        for (int i = 0; i < list.size(); i++) {
            long memberId = list.get(i).getMUid();
            if (myId != memberId) {
                memberListTmp.add(list.get(i));
            }
        }
        return new BMXDataCallBack<BMXGroupMemberList>() {
            @Override
            public void onResult(BMXErrorCode bmxErrorCode, BMXGroupMemberList list) {

            }
        };
    }
    
    protected void initData(boolean forceRefresh, BMXDataCallBack<BMXGroupMemberList> callBack) {
        GroupManager.getInstance().getMembers(mGroup, forceRefresh, (bmxErrorCode, list) -> {
            BMXGroupMemberList memberListTmp = new BMXGroupMemberList();
            long myId = SharePreferenceUtils.getInstance().getUserId();
            for (int i = 0; i < list.size(); i++) {
                long memberId = list.get(i).getMUid();
                if (myId != memberId) {
                    memberListTmp.add(list.get(i));
                }
            }
            if (callBack != null) {
                callBack.onResult(bmxErrorCode, memberListTmp);
            }
        });
    }

    protected void initData(String cursor, BMXDataCallBack<BMXGroupMemberList> callBack) {
        GroupManager.getInstance().getMembers(mGroup, cursor, DEFAULT_PAGE_SIZE, (bmxErrorCode, page) -> {
            BMXGroupMemberList memberListTmp = new BMXGroupMemberList();
            if (page != null && page.result() != null && !page.result().isEmpty()) {
                mCursor = page.cursor();
                BMXGroupMemberList list = page.result();
                long myId = SharePreferenceUtils.getInstance().getUserId();
                for (int i = 0; i < list.size(); i++) {
                    long memberId = list.get(i).getMUid();
                    if (myId != memberId) {
                        memberListTmp.add(list.get(i));
                    }
                }
            }
            if (callBack != null) {
                callBack.onResult(bmxErrorCode, memberListTmp);
            }
        });
    }

    protected void bindData(BMXGroupMemberList memberList, boolean upload) {
        if (memberList == null || memberList.isEmpty()) {
            if (!upload) {
                mGvGroupMember.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            }
            return;
        }
        List<BMXGroup.Member> members = new ArrayList<>();
        for (int i = 0; i < memberList.size(); i++) {
            members.add(memberList.get(i));
        }
        if (upload) {
            mAdapter.addListAtEnd(members);
        } else {
            mAdapter.replaceList(members);
        }
        if (mScrollListener != null) {
            mScrollListener.resetUpLoadStatus();
        }
        mGvGroupMember.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 展示群聊成员adapter
     */
    protected class ChatGroupMemberAdapter extends BaseRecyclerAdapter<BMXGroup.Member> {

        private ImageRequestConfig mConfig;

        private boolean mIsShowCheck;

        public ChatGroupMemberAdapter(Context context) {
            super(context);
            mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .showImageForEmptyUri(R.drawable.default_avatar_icon)
                    .showImageOnFail(R.drawable.default_avatar_icon)
                    .showImageOnLoading(R.drawable.default_avatar_icon).build();
        }

        public void setShowCheck(boolean showCheck) {
            mIsShowCheck = showCheck;
        }

        public boolean getShowCheck() {
            return mIsShowCheck;
        }

        @Override
        protected int onCreateViewById(int viewType) {
            return R.layout.item_group_list_member;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            ShapeImageView icon = holder.findViewById(R.id.img_icon);
            TextView tvName = holder.findViewById(R.id.txt_name);
            CheckBox checkBox = holder.findViewById(R.id.cb_choice);
            BMXGroup.Member member = getItem(position);
            if (member == null) {
                return;
            }
            if (mIsShowCheck) {
                boolean isCheck = mSelected.containsKey(member.getMUid())
                        && mSelected.get(member.getMUid());
                checkBox.setChecked(isCheck);
                checkBox.setVisibility(member.getMUid() != MessageConfig.MEMBER_ADD
                        && member.getMUid() != MessageConfig.MEMBER_REMOVE ? View.VISIBLE
                                : View.INVISIBLE);
            } else {
                checkBox.setVisibility(View.GONE);
            }
            if (member.getMUid() == MessageConfig.MEMBER_ADD) {
                String addName = member.getMGroupNickname();
                tvName.setText(TextUtils.isEmpty(addName) ? "添加" : addName);
                BMImageLoader.getInstance().display(icon,
                        "drawable://" + R.drawable.default_add_icon);
            } else if (member.getMUid() == MessageConfig.MEMBER_REMOVE) {
                String removeName = member.getMGroupNickname();
                tvName.setText(TextUtils.isEmpty(removeName) ? "移除" : removeName);
                BMImageLoader.getInstance().display(icon,
                        "drawable://" + R.drawable.default_remove_icon);
            } else {
                BMXRosterItem rosterItem = RosterFetcher.getFetcher().getRoster(member.getMUid());
                String name = "";
                if (rosterItem != null && !TextUtils.isEmpty(rosterItem.alias())) {
                    name = rosterItem.alias();
                } else if (member != null && !TextUtils.isEmpty(member.getMGroupNickname())) {
                    name = member.getMGroupNickname();
                }else if (rosterItem != null && !TextUtils.isEmpty(rosterItem.nickname())) {
                    name = rosterItem.nickname();
                } else if (rosterItem != null) {
                    name = rosterItem.username();
                }
                tvName.setText(TextUtils.isEmpty(name) ? "" : name);
                ChatUtils.getInstance().showRosterAvatar(rosterItem, icon, mConfig);
            }
        }
    }

    protected void toastError(BMXErrorCode e) {
        String error = e != null ? e.name() : "网络异常";
        ToastUtil.showTextViewPrompt(error);
    }

}
