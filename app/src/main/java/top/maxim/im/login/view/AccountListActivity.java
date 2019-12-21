
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXRosterItem;
import im.floo.floolib.ListOfLongLong;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.bean.UserBean;
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.contact.view.RosterChooseActivity;

/**
 * Description : 账号列表 Created by Mango on 2018/11/06
 */
public class AccountListActivity extends RosterChooseActivity {

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
        multi = false;
        return super.onCreateView();
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
                long rosterId = member.rosterId();
                if (mAdapter.getShowCheck()) {
                    // 单选
                    mSelected.clear();
                    mSelected.put(rosterId, true);
                    mAdapter.notifyDataSetChanged();
                } else {
                    // 切换账号
                    if (rosterId == mUserId) {
                        // 如果是自己不响应
                        return;
                    }
                    long userId = -1;
                    String userName = null;
                    String pwd = null;
                    if (mAccounts != null && mAccounts.size() > 0) {
                        UserBean bean = mAccounts.get(rosterId);
                        if (bean != null) {
                            userId = bean.getUserId();
                            userName = bean.getUserName();
                            pwd = bean.getUserPwd();
                        }
                    }
                    changeAccount(userId, userName, pwd, false);
                }
            }
        });
    }

    @Override
    protected BMXErrorCode initData(ListOfLongLong listOfLongLong, boolean forceRefresh) {
        if (listOfLongLong == null) {
            listOfLongLong = new ListOfLongLong();
        } else {
            listOfLongLong.clear();
        }
        mUserId = SharePreferenceUtils.getInstance().getUserId();
        // 自己添加到第一个
        listOfLongLong.add(mUserId);
        // 获取登录过的账号
        List<UserBean> beans = CommonUtils.getInstance().getLoginUsers();
        if (beans != null && beans.size() > 0) {
            for (UserBean bean : beans) {
                if (bean != null) {
                    mAccounts.put(bean.getUserId(), bean);
                    if (bean.getUserId() != mUserId) {
                        listOfLongLong.add(bean.getUserId());
                    }
                }
            }
        }
        return null;
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
            changeAccount(mUserId, "", "", true);
        } else {
            // 不是自己 只需要清除数据
            CommonUtils.getInstance().removeAccount(removeId);
            init();
        }
    }

    /**
     * 切换账号 首先推出当前账号 然后再登陆
     */
    private void changeAccount(long userId, String userName, String pwd, boolean remove) {
        showLoadingDialog(true);
        Observable.just("").map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                return UserManager.getInstance().signOut();
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
                        dismissLoadingDialog();
                        String error = e == null || TextUtils.isEmpty(e.getMessage()) ? "网络错误"
                                : e.getMessage();
                        ToastUtil.showTextViewPrompt(error);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        dismissLoadingDialog();
                        CommonUtils.getInstance().logout();
                        if (remove) {
                            CommonUtils.getInstance().removeAccount(userId);
                        }
                        handleResult(userName, pwd);
                    }
                });
    }

    private void handleResult(String userName, String pwd) {
        showLoadingDialog(true);
        Observable.just("").subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread()).delay(500, TimeUnit.MICROSECONDS)
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {
                        dismissLoadingDialog();
                        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(pwd)) {
                            // 有数据 直接登录
                            LoginActivity.login(AccountListActivity.this, userName, pwd, false);
                        } else {
                            // 无数据进入登录页
                            WelcomeActivity.openWelcome(AccountListActivity.this);
                        }
                    }
                });
    }
}
