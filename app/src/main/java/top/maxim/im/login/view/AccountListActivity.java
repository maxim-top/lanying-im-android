
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.bean.UserBean;
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.TaskDispatcher;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.DividerItemDecoration;
import top.maxim.im.common.view.recyclerview.RecyclerWithHFAdapter;
import top.maxim.im.scan.config.ScanConfigs;

/**
 * Description : 账号列表 Created by Mango on 2018/11/06
 */
public class AccountListActivity extends BaseTitleActivity {

    protected RecyclerView mRecycler;

    protected AccountAdapter mAdapter;

    protected View mEmptyView;

    protected boolean mChoose = false;

    protected Map<Long, Boolean> mSelected = new HashMap<>();

    private long mUserId;

    private LongSparseArray<UserBean> mAccounts = new LongSparseArray<>();

    public static void startAccountListActivity(Context context) {
        Intent intent = new Intent(context, AccountListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.setting_account_manager);
        builder.setRightText(R.string.edit, v -> {
            mChoose = !mChoose;
            mHeader.setRightText(getString(mChoose ? R.string.delete : R.string.edit));
            mAdapter.setShowCheck(mChoose);
            mAdapter.notifyDataSetChanged();
            if (!mChoose) {
                removeAccount();
            }
        });
        builder.setBackIcon(R.drawable.header_back_icon, v -> finish());
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
        mAdapter = new AccountAdapter(this);
        mRecycler.setAdapter(mAdapter);
        mAdapter.setShowCheck(mChoose);
        return view;
    }

    @Override
    protected void setViewListener() {
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserBean member = mAdapter.getItem(position);
                if (member == null) {
                    return;
                }
                long rosterId = member.getUserId();
                if (mAdapter.getShowCheck()) {
                    // 单选
                    if (rosterId == mUserId) {
                        // 如果是自己不响应
                        return;
                    }
                    mSelected.clear();
                    mSelected.put(rosterId, true);
                    mAdapter.notifyDataSetChanged();
                } else {
                    // 切换账号
                    if (rosterId == mUserId) {
                        // 如果是自己不响应
                        return;
                    }
                    UserBean bean = null;
                    if (mAccounts != null && mAccounts.size() > 0) {
                        bean = mAccounts.get(rosterId);
                    }
                    changeAccount(bean, false);
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
        Observable.just("").map(new Func1<String, List<UserBean>>() {
            @Override
            public List<UserBean> call(String s) {
                return initData();
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<UserBean>>() {
                    @Override
                    public void onCompleted() {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        String error = e != null ? e.getMessage() : getString(R.string.network_error);
                        ToastUtil.showTextViewPrompt(error);
                        bindData(null);
                    }

                    @Override
                    public void onNext(List<UserBean> beans) {
                        bindData(beans);
                    }
                });
    }

    protected List<UserBean> initData() {
        List<UserBean> userBeans = new ArrayList<>();
        mUserId = SharePreferenceUtils.getInstance().getUserId();
        // 获取登录过的账号
        List<UserBean> beans = CommonUtils.getInstance().getLoginUsers();
        if (beans != null && beans.size() > 0) {
            for (UserBean bean : beans) {
                if (bean != null) {
                    mAccounts.put(bean.getUserId(), bean);
                    if (bean.getUserId() != mUserId) {
                        userBeans.add(bean);
                    } else {
                        // 自己添加到第一个
                        userBeans.add(0, bean);
                    }
                }
            }
        }
        return userBeans;
    }

    protected void bindData(List<UserBean> beans) {
        if (beans != null && !beans.isEmpty()) {
            mAdapter.replaceList(beans);
            mRecycler.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        } else {
            mRecycler.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 移除账号
     */
    private void removeAccount() {
        long removeId = -1;
        for (Map.Entry<Long, Boolean> entry : mSelected.entrySet()) {
            if (entry.getValue()) {
                removeId = entry.getKey();
            }
        }
        if (removeId <= 0) {
            return;
        }
        if (removeId == mUserId) {
            // 移除的是自己 跳转退出
            changeAccount(new UserBean("", mUserId, "", "", 0), true);
        } else {
            // 不是自己 只需要清除数据
            CommonUtils.getInstance().removeAccount(removeId);
            init();
        }
    }

    /**
     * 切换账号 首先推出当前账号 然后再登陆
     */
    private void changeAccount(UserBean bean,
            boolean remove) {
        if (bean == null) {
            return;
        }
        showLoadingDialog(true);
        UserManager.getInstance().signOut(bmxErrorCode -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                TaskDispatcher.exec(() -> {
                    CommonUtils.getInstance().logout();
                    if (remove) {
                        CommonUtils.getInstance().removeAccount(mUserId);
                    }
                    TaskDispatcher.postMain(() -> handleResult(bean));
                });
                return;
            }
            // 失败
            dismissLoadingDialog();
            String error = bmxErrorCode == null || TextUtils.isEmpty(bmxErrorCode.name()) ? getString(R.string.network_error)
                    : bmxErrorCode.name();
            ToastUtil.showTextViewPrompt(error);
        });
    }

    private void handleResult(UserBean bean) {
        String userName = "", pwd = "", appId = "";
        String server = "";
        int port = 0;
        String restServer = "";
        if (bean != null) {
            userName = bean.getUserName();
            pwd = bean.getUserPwd();
            appId = bean.getAppId();
            server = bean.getServer();
            port = bean.getPort();
            restServer = bean.getRestServer();
        }
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(pwd)) {
            // 查看切换账号的appId是否和当前一致 一致不需要切换
            if (TextUtils.isEmpty(appId)) {
                // 兼容之前版本 如果为空 则为默认appId
                appId = ScanConfigs.CODE_APP_ID;
            }
            // 有数据 直接登录
            LoginActivity.login(AccountListActivity.this, userName, pwd, false,
                    appId, server, port, restServer);
        } else {
            dismissLoadingDialog();
            // 无数据进入登录页
            WelcomeActivity.openWelcome(AccountListActivity.this);
        }
    }

    /**
     * 展示群聊成员adapter
     */
    protected class AccountAdapter extends RecyclerWithHFAdapter<UserBean> {

        protected boolean mIsShowCheck;

        public AccountAdapter(Context context) {
            super(context);
        }

        public void setShowCheck(boolean showCheck) {
            mIsShowCheck = showCheck;
        }

        public boolean getShowCheck() {
            return mIsShowCheck;
        }

        @Override
        protected int onCreateViewById(int viewType) {
            return R.layout.item_account_list;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            TextView tvName = holder.findViewById(R.id.txt_name);
            TextView tvUserId = holder.findViewById(R.id.txt_userId);
            TextView tvAppId = holder.findViewById(R.id.txt_appId);
            CheckBox checkBox = holder.findViewById(R.id.cb_choice);
            ImageView selecIcon = holder.findViewById(R.id.iv_seleced_skin);
            UserBean bean = getItem(position);
            if (bean == null) {
                return;
            }
            selecIcon.setVisibility(bean.getUserId() == mUserId ? View.VISIBLE : View.GONE);
            if (mIsShowCheck) {
                boolean isCheck = mSelected.containsKey(bean.getUserId())
                        && mSelected.get(bean.getUserId());
                checkBox.setChecked(isCheck);
                checkBox.setVisibility(bean.getUserId() != mUserId ? View.VISIBLE : View.INVISIBLE);
            } else {
                checkBox.setVisibility(View.GONE);
            }
            String name = bean.getUserName();
            long userId = bean.getUserId();
            String appId = TextUtils.isEmpty(bean.getAppId()) ? ScanConfigs.CODE_APP_ID : bean.getAppId();
            tvName.setText(TextUtils.isEmpty(name) ? "" : name);
            tvAppId.setText("(AppID:" + appId + ")");
            tvUserId.setText(String.valueOf(userId));
        }
    }

}
