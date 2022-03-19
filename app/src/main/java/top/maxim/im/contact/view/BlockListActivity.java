
package top.maxim.im.contact.view;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import im.floo.BMXDataCallBack;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.BMXRosterItemList;
import im.floo.floolib.ListOfLongLong;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.message.view.ChatGroupListMemberActivity;

/**
 * Description : 黑名单列表 Created by Mango on 2018/11/06
 */
public class BlockListActivity extends RosterChooseActivity {

    private boolean isEdit = false;

    private int CHOOSE_BLACK_CODE = 1000;

    public static void startBlockActivity(Context context) {
        Intent intent = new Intent(context, BlockListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.black_list);
        builder.setRightText(R.string.edit, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEdit = !isEdit;
                if (isEdit) {
                    mHeader.setRightText(getString(R.string.confirm));
                } else {
                    mHeader.setRightText(getString(R.string.edit));
                    long id = 0;
                    for (Map.Entry<Long, Boolean> entry : mSelected.entrySet()) {
                        if (entry.getValue()) {
                            id = entry.getKey();
                            break;
                        }
                    }
                    removeBlock(id);
                }
                mAdapter.setShowCheck(isEdit);
                mAdapter.notifyDataSetChanged();
            }
        });
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
        View view = super.onCreateView();
        mEmptyView.setVisibility(View.GONE);
        buildFooterView();
        return view;
    }
    
    private void buildFooterView() {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        View addView = buildAddFooterView(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RosterChooseActivity.startRosterListActivity(BlockListActivity.this, true, false,
                        CHOOSE_BLACK_CODE);
            }
        });
        ll.addView(addView);
        mAdapter.addFooterView(ll);
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
                mSelected.clear();
                mSelected.put(mId, true);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void initData(boolean forceRefresh, BMXDataCallBack<ListOfLongLong> callBack) {
        RosterManager.getInstance().getBlockList(forceRefresh, callBack);
    }

    @Override
    protected void bindData(BMXRosterItemList itemList) {
        List<BMXRosterItem> members = new ArrayList<>();
        if (itemList != null && !itemList.isEmpty()) {
            for (int i = 0; i < itemList.size(); i++) {
                members.add(itemList.get(i));
            }
        }
        mAdapter.replaceList(members);
    }

    private void addBlock(long rosterId) {
        if (rosterId <= 0) {
            return;
        }
        showLoadingDialog(true);
        RosterManager.getInstance().block(rosterId, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                mSelected.clear();
                mAdapter.notifyDataSetChanged();
                init();
            } else {
                String error = bmxErrorCode != null ? bmxErrorCode.name() : "网络错误";
                ToastUtil.showTextViewPrompt(error);
            }
        });
    }

    private void removeBlock(final long rosterId) {
        if (rosterId <= 0) {
            return;
        }
        showLoadingDialog(true);
        RosterManager.getInstance().unblock(rosterId, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                init();
            } else {
                String error = bmxErrorCode != null ? bmxErrorCode.name() : "网络错误";
                ToastUtil.showTextViewPrompt(error);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_BLACK_CODE && resultCode == RESULT_OK && data != null) {
            List<Long> chooseList = (List<Long>)data
                    .getSerializableExtra(ChatGroupListMemberActivity.CHOOSE_DATA);
            if (chooseList != null && chooseList.size() > 0) {
                long rosterId = 0;
                for (Long id : chooseList) {
                    rosterId = id;
                    break;
                }
                addBlock(rosterId);
            }
        }
    }
}
