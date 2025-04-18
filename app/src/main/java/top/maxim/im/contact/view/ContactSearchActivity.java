
package top.maxim.im.contact.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import im.floo.BMXDataCallBack;
import im.floo.floolib.BMXRosterItem;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonCustomDialog;
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
 * Description : 通讯录搜索 Created by Mango on 2018/11/06
 */
public class ContactSearchActivity extends BaseTitleActivity {

    private RecyclerView mRecycler;

    private EditText mSearch;

    private SearchAdapter mAdapter;

    public static void openRosterSearch(Context context) {
        Intent intent = new Intent(context, ContactSearchActivity.class);
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
        View view = View.inflate(this, R.layout.activity_contact_search, null);
        mSearch = view.findViewById(R.id.search_contact);
        mRecycler = view.findViewById(R.id.contact_recycler);
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
                    searchRoster(mSearch.getEditableText().toString());
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
    private void searchRoster(String search) {
        if (TextUtils.isEmpty(search)) {
            return;
        }
        showLoadingDialog(true);
        BMXDataCallBack<BMXRosterItem> callBack = (bmxErrorCode, item) -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                List<BMXRosterItem> mSearchs = new ArrayList<>();
                mSearchs.add(item);
                mAdapter.replaceList(mSearchs);
            } else {
                mAdapter.removeAll();
                ToastUtil.showTextViewPrompt(getString(R.string.no_user_found));
            }
        };
        if (Pattern.matches("[0-9]+", search)) {
            // 纯数字
            RosterManager.getInstance().getRosterList(Long.valueOf(search), true, callBack);
        } else {
            RosterManager.getInstance().getRosterList(search, true, callBack);
        }
    }

    class SearchAdapter extends BaseRecyclerAdapter<BMXRosterItem> {

        private ImageRequestConfig mConfig;

        public SearchAdapter(Context context) {
            super(context);
            mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
                    .cacheOnDisk(true)
                    .showImageForEmptyUri(R.drawable.default_avatar_icon)
                    .showImageOnFail(R.drawable.default_avatar_icon)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .showImageOnLoading(R.drawable.default_avatar_icon).build();
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
            final BMXRosterItem item = getItem(position);
            if (item == null) {
                return;
            }
            //是否是好友
            BMXRosterItem.RosterRelation rosterRelation = item.relation();
            boolean friend = rosterRelation == BMXRosterItem.RosterRelation.Friend;
            long myId = SharePreferenceUtils.getInstance().getUserId();
            // 自己不展示添加按钮
            add.setVisibility(myId == item.rosterId() || friend ? View.GONE : View.VISIBLE);
            add.setOnClickListener(v -> {
                long rosterId = item.rosterId();
                showAddReason(rosterId, item.authQuestion(), item.addFriendAuthMode());
            });

            String name = item.username();
            title.setText(name);
            ChatUtils.getInstance().showRosterAvatar(item, avatar, mConfig);
        }

        /**
         * 输入框弹出
         */
        private void showAddReason(final long rosterId, String authQuestion, BMXRosterItem.AddFriendAuthMode addFriendAuthMode) {
            if (addFriendAuthMode == BMXRosterItem.AddFriendAuthMode.AnswerQuestion && authQuestion.length()>0){
                final LinearLayout ll = new LinearLayout(ContactSearchActivity.this);
                ll.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams textP = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                LinearLayout.LayoutParams editP = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                editP.setMargins(ScreenUtils.dp2px(10), ScreenUtils.dp2px(5), ScreenUtils.dp2px(10),
                        ScreenUtils.dp2px(5));

                TextView tvAuthQuestion = new TextView(ContactSearchActivity.this);
                tvAuthQuestion.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                        ScreenUtils.dp2px(15));
                tvAuthQuestion.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                tvAuthQuestion.setTextColor(getResources().getColor(R.color.color_black));
                tvAuthQuestion.setBackgroundColor(getResources().getColor(R.color.color_white));
                tvAuthQuestion.setText(authQuestion);
                ll.addView(tvAuthQuestion, textP);

                final EditText etAnswer = new EditText(ContactSearchActivity.this);
                etAnswer.setBackgroundResource(R.drawable.common_edit_corner_bg);
                etAnswer.setPadding(ScreenUtils.dp2px(5), 0, ScreenUtils.dp2px(5), 0);
                etAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                etAnswer.setTextColor(getResources().getColor(R.color.color_black));
                etAnswer.setMinHeight(ScreenUtils.dp2px(40));
                ll.addView(etAnswer, editP);

                DialogUtils.getInstance().showCustomDialog(ContactSearchActivity.this, ll,
                        getString(R.string.add_friend_auth_answer), getString(R.string.confirm),
                        getString(R.string.cancel), new CommonCustomDialog.OnDialogListener() {
                            @Override
                            public void onConfirmListener() {
                                String answer = etAnswer.getEditableText().toString().trim();
                                addRoster(rosterId, "", answer);
                            }

                            @Override
                            public void onCancelListener() {

                            }
                        });
            } else {
                DialogUtils.getInstance().showEditDialog(ContactSearchActivity.this, getString(R.string.add_friend),
                        getString(R.string.confirm), getString(R.string.cancel),
                        new CommonEditDialog.OnDialogListener() {
                            @Override
                            public void onConfirmListener(String content) {
                                addRoster(rosterId, content, "");
                            }

                            @Override
                            public void onCancelListener() {

                            }
                        });
            }
        }

        private void addRoster(long rosterId, final String reason, final String authAnswer) {
            if (rosterId <= 0) {
                return;
            }
            showLoadingDialog(true);
            if (authAnswer.length() > 0){
                RosterManager.getInstance().apply(rosterId, "", authAnswer, bmxErrorCode -> {
                    dismissLoadingDialog();
                    if (BaseManager.bmxFinish(bmxErrorCode)) {
                        ToastUtil.showTextViewPrompt(getString(R.string.add_successful));
                        finish();
                    } else {
                        ToastUtil.showTextViewPrompt(CommonUtils.getErrorMessage(bmxErrorCode));
                    }
                });
            } else {
                RosterManager.getInstance().apply(rosterId, reason, bmxErrorCode -> {
                    dismissLoadingDialog();
                    if (BaseManager.bmxFinish(bmxErrorCode)) {
                        ToastUtil.showTextViewPrompt(getString(R.string.add_successful));
                        finish();
                    } else {
                        ToastUtil.showTextViewPrompt(CommonUtils.getErrorMessage(bmxErrorCode));
                    }
                });
            }
        }
    }
}
