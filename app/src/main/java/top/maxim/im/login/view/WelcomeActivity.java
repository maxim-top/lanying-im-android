
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;

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
import top.maxim.im.common.utils.permissions.PermissionsConstant;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.SplashVideoPlayView;
import top.maxim.im.login.bean.DNSConfigEvent;
import top.maxim.im.net.ConnectivityReceiver;
import top.maxim.im.push.NotificationUtils;
import top.maxim.im.sdk.utils.MessageDispatcher;

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
        initJump();
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
        CommonUtils.initializeSDK();
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
}
