
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import top.maxim.im.LoginRegisterActivity;
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
import top.maxim.im.common.utils.AgentTask;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonCustomDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.utils.permissions.PermissionsConstant;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.SplashVideoPlayView;
import top.maxim.im.login.bean.DNSConfigEvent;
import top.maxim.im.net.ConnectivityReceiver;
import top.maxim.im.push.NotificationUtils;
import top.maxim.im.sdk.utils.MessageDispatcher;
import top.maxim.rtc.RTCManager;

public class WelcomeActivity extends BaseTitleActivity {

    private ImageView mIvSplash;

    private SplashVideoPlayView mVideoSplash;

    private ActivityResultLauncher<String[]> permissionLauncher;

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
        //听云
        AgentTask.get().init(AppContextUtils.getApplication());
    }

    private void initPermission(){
        NotificationUtils.getInstance().cancelAll();
        showProtocol();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private boolean checkPermission() {
        return hasPermission(PermissionsConstant.READ_STORAGE, PermissionsConstant.WRITE_STORAGE);
    }

    @Override
    public void onPermissionGranted(List<String> permissions) {
        super.onPermissionGranted(permissions);
        if (permissions == null || permissions.size() == 0) {
            return;
        }
        long userId = SharePreferenceUtils.getInstance().getUserId();
        String pwd = SharePreferenceUtils.getInstance().getUserPwd();
        autoLogin(userId, pwd);
    }

    @Override
    public void onPermissionDenied(List<String> permissions) {
        super.onPermissionDenied(permissions);
        if (permissions == null || permissions.size() == 0) {
            return;
        }
        long userId = SharePreferenceUtils.getInstance().getUserId();
        String pwd = SharePreferenceUtils.getInstance().getUserPwd();
        autoLogin(userId, pwd);
    }

    private void initJump() {
        RTCManager.getInstance().init(AppContextUtils.getApplication(), BaseManager.getBMXClient());
        boolean isLogin = SharePreferenceUtils.getInstance().getLoginStatus();
        long userId = SharePreferenceUtils.getInstance().getUserId();
        String pwd = SharePreferenceUtils.getInstance().getUserPwd();
        if (isLogin && userId > 0 && !TextUtils.isEmpty(pwd)) {
//            if (!checkPermission()) {
//                requestPermissions(PermissionsConstant.READ_STORAGE, PermissionsConstant.WRITE_STORAGE);
//            }else{
                autoLogin(userId, pwd);
//            }
            return;
        }
        AppIdActivity.open(WelcomeActivity.this);
        finish();
    }

    /**
     * 展示视频
     */
    private void showVideo() {
        boolean isFirst = SharePreferenceUtils.getInstance().getFirst();
        if (true) {
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

    /**
     * 自动登陆
     */
    private void autoLogin(long userId, final String pwd) {
        String dns = SharePreferenceUtils.getInstance().getDnsConfig();
        // 是否自定义dns配置
        boolean changeDns = false;
        String server = "";
        int port = 0;
        String restServer = "";
        if (!TextUtils.isEmpty(dns)) {
            DNSConfigEvent event = new Gson().fromJson(dns, DNSConfigEvent.class);
            if (event != null) {
                server = event.getServer();
                port = event.getPort();
                restServer = event.getRestServer();
                changeDns = !TextUtils.isEmpty(server) && port > 0
                        && !TextUtils.isEmpty(restServer);
                if (changeDns) {
                    BaseManager.changeDNS(server, port, restServer);
                }
            }
        }
        boolean finalChangeDns = changeDns;
        String finalServer = server;
        int finalPort = port;
        String finalRestServer = restServer;
        UserManager.getInstance().signInById(userId, pwd, bmxErrorCode -> {
            if (!BaseManager.bmxFinish(bmxErrorCode)) {
                ToastUtil.showTextViewPrompt(getString(R.string.network_exception));
                LoginRegisterActivity.openLoginRegister(WelcomeActivity.this, false);
                return;
            }
            //启动网络监听
            ConnectivityReceiver.start(AppContextUtils.getApplication());

            // 登陆成功后 需要将userId存储SP 作为下次自动登陆
            UserManager.getInstance().getProfile(false, (bmxErrorCode1, profile) -> {
                if (BaseManager.bmxFinish(bmxErrorCode1) && profile != null
                        && profile.userId() > 0) {
                    UserBean bean = new UserBean(profile.username(), profile.userId(), pwd,
                            SharePreferenceUtils.getInstance().getAppId(),
                            System.currentTimeMillis());
                    if (finalChangeDns) {
                        bean.setServer(finalServer);
                        bean.setPort(finalPort);
                        bean.setRestServer(finalRestServer);
                    }
                    CommonUtils.getInstance()
                            .addUser(bean);
                }
            });
            initData();
            SharePreferenceUtils.getInstance().putLoginStatus(true);
            MessageDispatcher.getDispatcher().initialize();
            MainActivity.openMain(WelcomeActivity.this);
        });
    }

    /**
     * 预加载数据 每次登陆获取service的profile roster
     */
    public static void initData() {
        Observable.just("").map(s -> {
            // 自己的profile
            UserManager.getInstance().getProfile(true, null);
            // roster列表信息
            RosterManager.getInstance().get(true, null);
            // 群列表信息
            GroupManager.getInstance().getGroupList(true, null);
            // 消息列表信息
            ChatManager.getInstance().getAllConversations(null);
            String name = SharePreferenceUtils.getInstance().getUserName();
            String pwd = SharePreferenceUtils.getInstance().getUserPwd();
            AppManager.getInstance().getTokenByNameFromServer(name, pwd, null);
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
        DialogUtils.getInstance().showCustomDialog(this, tv, false, title.toString(), getString(R.string.agree), getString(R.string.not_available_for_now),
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
