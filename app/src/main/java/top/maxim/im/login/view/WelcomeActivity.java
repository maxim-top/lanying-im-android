
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import top.maxim.im.bmxmanager.ChatManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.bean.UserBean;
import top.maxim.im.common.provider.CommonProvider;
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonCustomDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.utils.permissions.PermissionsConstant;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.SplashVideoPlayView;
import top.maxim.im.push.NotificationUtils;
import top.maxim.im.sdk.utils.MessageDispatcher;

public class WelcomeActivity extends BaseTitleActivity {

    private ImageView mIvSplash;

    private SplashVideoPlayView mVideoSplash;

    public static void openWelcome(Context context) {
        Intent intent = new Intent(context, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        return new Header.Builder(this, headerContainer).build();
    }

    @Override
    protected View onCreateView() {
        hideHeader();
        View view = View.inflate(this, R.layout.activity_welcome, null);
        mIvSplash = view.findViewById(R.id.view_splash_img);
        mVideoSplash = view.findViewById(R.id.view_splash_video);
        mIvSplash.setVisibility(View.VISIBLE);
        mVideoSplash.setVisibility(View.GONE);
        return view;
    }

    @Override
    protected void setStatusBar() {
        super.setStatusBar();
        mStatusBar.setVisibility(View.GONE);
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        NotificationUtils.getInstance().cancelAll();
        if (checkPermission()) {
            showProtocol();
        } else {
            requestPermissions(PermissionsConstant.READ_STORAGE, PermissionsConstant.WRITE_STORAGE);
        }
    }

    private boolean checkPermission() {
        return hasPermission(PermissionsConstant.READ_STORAGE, PermissionsConstant.WRITE_STORAGE);
    }

    private void initJump() {
        boolean isLogin = SharePreferenceUtils.getInstance().getLoginStatus();
        long userId = SharePreferenceUtils.getInstance().getUserId();
        String pwd = SharePreferenceUtils.getInstance().getUserPwd();
        if (isLogin && userId > 0 && !TextUtils.isEmpty(pwd)) {
            autoLogin(userId, pwd);
            return;
        }
        LoginActivity.openLogin(WelcomeActivity.this);
        finish();
    }

    /**
     * 展示视频
     */
    private void showVideo() {
        boolean isFirst = SharePreferenceUtils.getInstance().getFirst();
        if (!isFirst) {
            initJump();
            return;
        }
        SharePreferenceUtils.getInstance().putIsFirst(false);
        mIvSplash.setVisibility(View.GONE);
        mVideoSplash.setVisibility(View.VISIBLE);
        mVideoSplash.setPlayListener(new SplashVideoPlayView.OnPlayVideoListener() {

            @Override
            public void onStart() {
            }

            @Override
            public void onFinish() {
                initJump();
            }

            @Override
            public void onError() {
                initJump();
            }
        });
        mVideoSplash.setPrepareVideoPath(R.raw.splash_video);
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
                    // SD权限
                    showProtocol();
                    break;
                case PermissionsConstant.WRITE_STORAGE:
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
                    // 读写SD权限拒绝
                    CommonProvider.openAppPermission(this);
                    break;
                case PermissionsConstant.WRITE_STORAGE:
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 自动登陆
     */
    private void autoLogin(long userId, final String pwd) {
        Observable.just(userId).map(new Func1<Long, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(Long s) {
                return UserManager.getInstance().signInById(s, pwd);
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
                    public void onNext(BMXErrorCode bmxErrorCode) {
                        // 登陆成功后 需要将userId存储SP 作为下次自动登陆
                        BMXUserProfile profile = new BMXUserProfile();
                        BMXErrorCode errorCode = UserManager.getInstance().getProfile(profile,
                                false);
                        if (errorCode != null
                                && errorCode.swigValue() == BMXErrorCode.NoError.swigValue()
                                && profile.userId() > 0) {
                            CommonUtils.getInstance().addUser(new UserBean(profile.username(),
                                    profile.userId(), pwd, System.currentTimeMillis()));
                        }
                        initData();
                        SharePreferenceUtils.getInstance().putLoginStatus(true);
                        MessageDispatcher.getDispatcher().initialize();
                        MainActivity.openMain(WelcomeActivity.this);
                        finish();
                    }
                });
    }

    /**
     * 预加载数据 每次登陆获取service的profile roster
     */
    public static void initData() {
        Observable.just("").map(s -> {
            // 自己的profile
            UserManager.getInstance().getProfile(new BMXUserProfile(), true);
            // roster列表信息
            RosterManager.getInstance().get(new ListOfLongLong(), true);
            // 群列表信息
            GroupManager.getInstance().search(new BMXGroupList(), true);
            // 消息列表信息
            ChatManager.getInstance().getAllConversations();
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

    /**
     * 展示用户协议
     */
    private void showProtocol() {
        boolean show = SharePreferenceUtils.getInstance().getProtocolDialogStatus();
        if (show) {
            showVideo();
            return;
        }
        // 标题
        StringBuilder title = new StringBuilder(getString(R.string.register_protocol2))
                .append(getString(R.string.register_protocol3))
                .append(getString(R.string.register_protocol4));

        // 内容
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(getResources().getString(R.string.register_protocol_content1));

        // 用户服务
        SpannableString spannableString = new SpannableString(
                "《" + getResources().getString(R.string.register_protocol2) + "》");
        spannableString.setSpan(new ClickableSpan() {

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(getResources().getColor(R.color.color_0079F4));
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(@NonNull View widget) {
                ProtocolActivity.openProtocol(WelcomeActivity.this, 1);
            }
        }, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        // 隐私政策
        builder.append(getResources().getString(R.string.register_protocol3));
        SpannableString spannableString1 = new SpannableString(
                "《" + getResources().getString(R.string.register_protocol4) + "》");
        spannableString1.setSpan(new ClickableSpan() {

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(getResources().getColor(R.color.color_0079F4));
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(@NonNull View widget) {
                ProtocolActivity.openProtocol(WelcomeActivity.this, 0);
            }
        }, 0, spannableString1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString1);
        builder.append(getString(R.string.register_protocol_content2));

        TextView tv = new TextView(this);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(builder);
        DialogUtils.getInstance().showCustomDialog(this, tv, title.toString(), "同意", "暂不使用",
                new CommonCustomDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener() {
                        SharePreferenceUtils.getInstance().putProtocolDialogStatus(true);
                        showVideo();
                    }

                    @Override
                    public void onCancelListener() {
                        finish();
                    }
                });
    }
}
