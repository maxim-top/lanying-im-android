
package top.maxim.im.message.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXGroupMemberList;
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

    protected long mGroupId;

    private boolean mChoose = false;

    protected BMXGroup mGroup = new BMXGroup();

    protected Map<Long, Boolean> mSelected = new HashMap<>();

    protected static final String CHOOSE = "choose";

    public static final String CHOOSE_DATA = "chooseData";

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
        builder.setHeaderBgColor(getResources().getColor(R.color.c2));
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
        mGvGroupMember = view.findViewById(R.id.gv_chat_group_member);
        mGvGroupMember.setLayoutManager(new LinearLayoutManager(this));
        mGvGroupMember.addItemDecoration(new DividerItemDecoration(this, R.color.guide_divider));
        mGvGroupMember.setAdapter(mAdapter = new ChatGroupMemberAdapter(this));
        mAdapter.setShowCheck(mChoose);
        return view;
    }

    @Override
    protected void setViewListener() {
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
        Observable.just(mGroupId).map(new Func1<Long, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(Long aLong) {
                return GroupManager.getInstance().search(mGroupId, mGroup, false);
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
                        String error = e == null ? "网络错误" : e.getMessage();
                        ToastUtil.showTextViewPrompt(error);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        init();
                    }
                });
    }

    protected void init() {
        if (mGroup == null) {
            return;
        }
        showLoadingDialog(true);
        final BMXGroupMemberList memberList = new BMXGroupMemberList();
        Observable.just(mGroup).map(new Func1<BMXGroup, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXGroup group) {
                return initData(memberList, true);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).map(new Func1<BMXErrorCode, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXErrorCode errorCode) {
                if (!memberList.isEmpty()) {
                    ListOfLongLong listOfLongLong = new ListOfLongLong();
                    for (int i = 0; i < memberList.size(); i++) {
                        listOfLongLong.add(memberList.get(i).getMUid());
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
                        String error = e != null ? e.getMessage() : "网络错误";
                        ToastUtil.showTextViewPrompt(error);
                        initData(memberList, false);
                        bindData(memberList);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        bindData(memberList);
                    }
                });
    }

    protected BMXErrorCode initData(BMXGroupMemberList memberList, boolean forceRefresh) {
        BMXGroupMemberList memberListTmp = new BMXGroupMemberList();
        BMXErrorCode errorCode =  GroupManager.getInstance().getMembers(mGroup, memberListTmp, forceRefresh);
        long myId = SharePreferenceUtils.getInstance().getUserId();
        for (int i=0; i< memberListTmp.size(); i++){
            long memberId = memberListTmp.get(i).getMUid();
            if (myId != memberId){
                memberList.add(memberListTmp.get(i));
            }
        }
        return errorCode;
    }

    protected void bindData(BMXGroupMemberList memberList) {
        List<BMXGroup.Member> members = new ArrayList<>();
        if (memberList != null && !memberList.isEmpty()) {
            for (int i = 0; i < memberList.size(); i++) {
                members.add(memberList.get(i));
            }
        }
        mAdapter.replaceList(members);
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
                String name;
                if (rosterItem != null && !TextUtils.isEmpty(rosterItem.alias())) {
                    name = rosterItem.alias();
                } else if (rosterItem != null && !TextUtils.isEmpty(rosterItem.nickname())) {
                    name = rosterItem.nickname();
                } else if (rosterItem != null) {
                    name = rosterItem.username();
                } else {
                    name = member.getMGroupNickname();
                }
                tvName.setText(TextUtils.isEmpty(name) ? "" : name);
                ChatUtils.getInstance().showRosterAvatar(rosterItem, icon, mConfig);
            }
        }
    }

}
