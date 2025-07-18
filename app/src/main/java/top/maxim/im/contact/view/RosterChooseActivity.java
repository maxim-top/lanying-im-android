
package top.maxim.im.contact.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.floo.BMXDataCallBack;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXRosterItemList;
import im.floo.floolib.ListOfLongLong;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.BMImageLoader;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.DividerItemDecoration;
import top.maxim.im.common.view.recyclerview.RecyclerWithHFAdapter;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;

/**
 * Description : 选择 Created by Mango on 2018/11/06
 */
public class RosterChooseActivity extends BaseTitleActivity {

    protected RecyclerView mRecycler;

    protected RosterAdapter mAdapter;

    protected View mEmptyView;

    protected boolean mChoose = false;

    protected boolean multi = true;

    /* 筛选列表 */
    private List<String> mFilterList;

    protected Map<Long, Boolean> mSelected = new HashMap<>();

    private static final String CHOOSE = "choose";

    private static final String MULTI_CHOOSE = "multi_choose";

    public static final String FILTER_LIST = "filterList";

    public static final String CHOOSE_DATA = "chooseData";

    public static void startRosterListActivity(Activity context, boolean isChoose, boolean isMulti,
            int requestCode) {
        Intent intent = new Intent(context, RosterChooseActivity.class);
        intent.putExtra(CHOOSE, isChoose);
        intent.putExtra(MULTI_CHOOSE, isMulti);
        context.startActivityForResult(intent, requestCode);
    }

    public static void startRosterListActivity(Activity context, boolean isChoose, boolean isMulti,
            List<String> filterList, int requestCode) {
        Intent intent = new Intent(context, RosterChooseActivity.class);
        intent.putExtra(CHOOSE, isChoose);
        intent.putExtra(MULTI_CHOOSE, isMulti);
        intent.putExtra(FILTER_LIST, (Serializable)filterList);
        context.startActivityForResult(intent, requestCode);
    }

    public static void startRosterListActivity(Activity context, int requestCode) {
        startRosterListActivity(context, false, true, requestCode);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.choose_contact);
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
        View view = View.inflate(this, R.layout.fragment_contact, null);
        mEmptyView = view.findViewById(R.id.view_empty);
        mEmptyView.setVisibility(View.GONE);
        mRecycler = view.findViewById(R.id.contact_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.addItemDecoration(new DividerItemDecoration(this, R.color.guide_divider));
        mAdapter = initAdapter();
        mRecycler.setAdapter(mAdapter);
        mAdapter.setShowCheck(mChoose);
        return view;
    }

    protected RosterAdapter initAdapter() {
        return new RosterAdapter(this);
    }

    /**
     * 设置添加view
     */
    protected View buildAddFooterView(View.OnClickListener listener) {
        View view = View.inflate(this, R.layout.item_group_list_member, null);
        ShapeImageView icon = view.findViewById(R.id.img_icon);
        TextView tvName = view.findViewById(R.id.txt_name);
        tvName.setText(getString(R.string.add));
        icon.setImageResource(R.drawable.default_add_icon);
        CheckBox checkBox = view.findViewById(R.id.cb_choice);
        view.setOnClickListener(listener);
        return view;
    }

    /**
     * 设置移除view
     */
    protected View buildRemoveFooterView(View.OnClickListener listener) {
        View view = View.inflate(this, R.layout.item_group_list_member, null);
        ShapeImageView icon = view.findViewById(R.id.img_icon);
        TextView tvName = view.findViewById(R.id.txt_name);
        tvName.setText(getString(R.string.remove));
        BMImageLoader.getInstance().display(icon, "drawable://" + R.drawable.default_remove_icon);
        CheckBox checkBox = view.findViewById(R.id.cb_choice);
        view.setOnClickListener(listener);
        return view;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mChoose = intent.getBooleanExtra(CHOOSE, false);
            multi = intent.getBooleanExtra(MULTI_CHOOSE, true);
            mFilterList = (List<String>)intent.getSerializableExtra(FILTER_LIST);
        }
    }

    @Override
    protected void setViewListener() {
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BMXRosterItem member = mAdapter.getItem(position);
                if (member == null) {
                    return;
                }
                long mId = member.rosterId();
                if (!mAdapter.getShowCheck()) {
                    return;
                }
                if (multi) {
                    // 多选
                    if (!mSelected.containsKey(mId) || !mSelected.get(mId)) {
                        mSelected.put(mId, true);
                    } else {
                        mSelected.remove(mId);
                    }
                    mAdapter.notifyItemChanged(position);
                } else {
                    // 单选
                    mSelected.clear();
                    mSelected.put(mId, true);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        init();
    }

    protected void init() {
        showLoadingDialog(true);
        BMXDataCallBack<ListOfLongLong> callBack = (bmxErrorCode, list) -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                if (!list.isEmpty()) {
                    RosterManager.getInstance().getRosterList(list, true, (bmxErrorCode1, itemList) -> {
                        RosterFetcher.getFetcher().putRosters(itemList);
                        if (BaseManager.bmxFinish(bmxErrorCode1)) {
                            bindData(itemList);
                        } else {
                            String error = bmxErrorCode1 != null ? bmxErrorCode1.name() : getString(R.string.network_error);
                            ToastUtil.showTextViewPrompt(error);
                            bindData(null);
                        }
                    });
                } else {
                    bindData(null);
                }
            } else {
                String error = bmxErrorCode != null ? bmxErrorCode.name() : getString(R.string.network_error);
                ToastUtil.showTextViewPrompt(error);
                bindData(null);
            }
        };
        initData(false, callBack);
    }

    protected void initData(boolean forceRefresh, BMXDataCallBack<ListOfLongLong> callBack) {
        RosterManager.getInstance().get(forceRefresh, callBack);
    }

    protected void bindData(BMXRosterItemList itemList) {
        if (itemList != null && !itemList.isEmpty()) {
            List<BMXRosterItem> rosterItems = new ArrayList<>();
            for (int i = 0; i < itemList.size(); i++) {
                BMXRosterItem item = itemList.get(i);
                if (item == null) {
                    continue;
                }
                if (mFilterList == null || !mFilterList.contains(String.valueOf(item.rosterId()))) {
                    rosterItems.add(itemList.get(i));
                }
            }
            mAdapter.replaceList(rosterItems);
            mRecycler.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        } else {
            mRecycler.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 展示群聊成员adapter
     */
    protected class RosterAdapter extends RecyclerWithHFAdapter<BMXRosterItem> {

        protected ImageRequestConfig mConfig;

        protected boolean mIsShowCheck;

        public RosterAdapter(Context context) {
            super(context);
            mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
                    .showImageForEmptyUri(R.drawable.default_avatar_icon)
                    .showImageOnFail(R.drawable.default_avatar_icon).cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
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
            BMXRosterItem member = getItem(position);
            if (member == null) {
                return;
            }
            if (mIsShowCheck) {
                boolean isCheck = mSelected.containsKey(member.rosterId())
                        && mSelected.get(member.rosterId());
                checkBox.setChecked(isCheck);
                checkBox.setVisibility(member.rosterId() != MessageConfig.MEMBER_ADD
                        && member.rosterId() != MessageConfig.MEMBER_REMOVE ? View.VISIBLE
                                : View.INVISIBLE);
            } else {
                checkBox.setVisibility(View.GONE);
            }
            String name = CommonUtils.getRosterDisplayName(member);
            tvName.setText(name);
            ChatUtils.getInstance().showRosterAvatar(member, icon, mConfig);
        }
    }

}
