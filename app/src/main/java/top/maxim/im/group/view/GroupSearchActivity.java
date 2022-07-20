
package top.maxim.im.group.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import im.floo.floolib.BMXGroup;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.common.view.recyclerview.BaseRecyclerAdapter;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.DividerItemDecoration;
import top.maxim.im.message.utils.ChatUtils;

/**
 * Description : 群聊搜索 Created by Mango on 2018/11/06
 */
public class GroupSearchActivity extends BaseTitleActivity {

    private RecyclerView mRecycler;

    private EditText mSearch;

    private SearchAdapter mAdapter;

    public static void openGroupSearch(Context context) {
        Intent intent = new Intent(context, GroupSearchActivity.class);
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
        mSearch.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
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
                    searchGroup(mSearch.getEditableText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 搜索
     * 
     * @param search 内容
     */
    private void searchGroup(String search) {
        if (TextUtils.isEmpty(search)) {
            return;
        }
        if (Pattern.matches("[0-9]+", search)) {
            showLoadingDialog(true);
            // 纯数字
            GroupManager.getInstance().getGroupInfo(Long.valueOf(search), true,
                    (bmxErrorCode, bmxGroup) -> {
                        dismissLoadingDialog();
                        if (BaseManager.bmxFinish(bmxErrorCode)) {
                            List<BMXGroup> groups = new ArrayList<>();
                            groups.add(bmxGroup);
                            mAdapter.replaceList(groups);
                        } else {
                            mAdapter.removeAll();
                            ToastUtil.showTextViewPrompt(getString(R.string.no_group_found));
                        }
                    });
        } else {
            mAdapter.removeAll();
            ToastUtil.showTextViewPrompt(getString(R.string.no_group_found));
        }
    }

    class SearchAdapter extends BaseRecyclerAdapter<BMXGroup> {

        private ImageRequestConfig mConfig;

        public SearchAdapter(Context context) {
            super(context);
            mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
                    .showImageForEmptyUri(R.drawable.default_group_icon)
                    .showImageOnFail(R.drawable.default_group_icon)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .showImageOnLoading(R.drawable.default_group_icon).build();
        }

        @Override
        protected int onCreateViewById(int viewType) {
            return R.layout.item_contact_search_view;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            ShapeImageView avatar = holder.findViewById(R.id.contact_avatar);
            TextView title = holder.findViewById(R.id.contact_title);
            TextView add = holder.findViewById(R.id.add_contact);
            add.setText(getString(R.string.join));
            final BMXGroup item = getItem(position);
            if (item == null) {
                return;
            }
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showJoinReason(item);
                }
            });
            title.setText(TextUtils.isEmpty(item.name()) ? "" : item.name());
            ChatUtils.getInstance().showGroupAvatar(item, avatar, mConfig);
        }

        /**
         * 输入框弹出
         */
        private void showJoinReason(final BMXGroup group) {
            DialogUtils.getInstance().showEditDialog(GroupSearchActivity.this, getString(R.string.join_group_chat),
                    getString(R.string.confirm), getString(R.string.cancel),
                    new CommonEditDialog.OnDialogListener() {
                        @Override
                        public void onConfirmListener(String content) {
                            joinGroup(group, content);
                        }

                        @Override
                        public void onCancelListener() {

                        }
                    });
        }

        private void joinGroup(final BMXGroup group, final String reason) {
            if (group == null) {
                return;
            }
            showLoadingDialog(true);
            GroupManager.getInstance().join(group, reason, bmxErrorCode -> {
                dismissLoadingDialog();
                if (BaseManager.bmxFinish(bmxErrorCode)) {
                    ToastUtil.showTextViewPrompt(getString(R.string.join_successfully));
                    finish();
                } else {
                    String error = bmxErrorCode != null ? bmxErrorCode.name() : getString(R.string.failed_to_join);
                    ToastUtil.showTextViewPrompt(error);
                }
            });
        }
    }
}
