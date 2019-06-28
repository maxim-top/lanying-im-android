
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.List;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroupList;
import im.floo.floolib.BMXUserProfile;
import im.floo.floolib.ListOfLongLong;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.MainActivity;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.provider.CommonProvider;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.permissions.PermissionsConstant;
import top.maxim.im.common.view.Header;
import top.maxim.im.sdk.utils.MessageDispatcher;

public class WelcomeActivity extends BaseTitleActivity {

    public static void openWelcome(Context context) {
        Intent intent = new Intent(context, WelcomeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        return new Header.Builder(this, headerContainer).build();
    }

    @Override
    protected View onCreateView() {
        hideHeader();
        return View.inflate(this, R.layout.activity_welcome, null);
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        if (checkPermission()) {
            initJump();
        } else {
            requestPermissions(PermissionsConstant.READ_STORAGE, PermissionsConstant.WRITE_STORAGE);
        }
    }

    private boolean checkPermission() {
        return hasPermission(PermissionsConstant.READ_STORAGE, PermissionsConstant.WRITE_STORAGE);
    }

    private void initJump() {
        boolean isLogin = SharePreferenceUtils.getInstance().getLoginStatus();
        String userName = SharePreferenceUtils.getInstance().getUserName();
        String pwd = SharePreferenceUtils.getInstance().getUserPwd();
        if (isLogin && !TextUtils.isEmpty(userName) && !TextUtils.isEmpty(pwd)) {
            autoLogin(userName, pwd);
            return;
        }
        LoginActivity.openLogin(this);
        finish();
    }

    @Override
    public void onPermissionGranted(List<String> permissions) {
        super.onPermissionGranted(permissions);
        if (permissions == null || permissions.size() == 0) {
            return;
        }
        for (String permission : permissions) {
            switch (permission) {
                case PermissionsConstant.READ_STORAGE:
                    // 读SD权限
                    if (hasPermission(PermissionsConstant.WRITE_STORAGE)) {
                        // 如果有读写权限都有 则直接操作
                        initJump();
                    } else {
                        requestPermissions(PermissionsConstant.WRITE_STORAGE);
                    }
                    break;
                case PermissionsConstant.WRITE_STORAGE:
                    // 写SD权限 如果有读写权限都有 则直接操作
                    initJump();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onPermissionDenied(List<String> permissions) {
        super.onPermissionDenied(permissions);
        if (permissions == null || permissions.size() == 0) {
            return;
        }
        for (String permission : permissions) {
            switch (permission) {
                case PermissionsConstant.READ_STORAGE:
                case PermissionsConstant.WRITE_STORAGE:
                    // 读写SD权限拒绝
                    CommonProvider.openAppPermission(this);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 自动登陆
     */
    private void autoLogin(String userName, final String pwd) {
        Observable.just(userName).map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                return UserManager.getInstance().signInByName(s, pwd);
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
                        ToastUtil.showTextViewPrompt("网络异常");
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        initData();
                        SharePreferenceUtils.getInstance().putLoginStatus(true);
                        MessageDispatcher.getDispatcher().initialize();
                        MainActivity.openMain(WelcomeActivity.this);
                    }
                });
    }

    /**
     * 预加载数据 每次登陆获取service的profile  roster
     */
    private void initData() {
        Observable.just("").map(s -> {
            UserManager.getInstance().getProfile(new BMXUserProfile(), true);
            RosterManager.getInstance().get(new ListOfLongLong(), true);
            GroupManager.getInstance().search(new BMXGroupList(), true);
            String name = SharePreferenceUtils.getInstance().getUserName();
            String pwd = SharePreferenceUtils.getInstance().getUserPwd();
            AppManager.getInstance().getTokenByName(name, pwd, null);
            return "";
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {

                    }
                });
    }
}
