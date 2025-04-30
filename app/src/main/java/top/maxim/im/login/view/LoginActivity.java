
package top.maxim.im.login.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import im.floo.BMXCallBack;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import top.maxim.im.MainActivity;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.bean.UserBean;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.common.utils.ClickTimeUtils;
import top.maxim.im.common.utils.CommonConfig;
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.TaskDispatcher;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.permissions.PermissionsConstant;
import top.maxim.im.common.view.Header;
import top.maxim.im.login.bean.DNSConfigEvent;
import top.maxim.im.net.ConnectivityReceiver;
import top.maxim.im.net.HttpResponseCallback;
import top.maxim.im.scan.config.ScanConfigs;
import top.maxim.im.scan.view.ScannerActivity;
import top.maxim.im.sdk.utils.MessageDispatcher;
import top.maxim.im.wxapi.WXUtils;

/**
 * Description : 登陆 Created by Mango on 2018/11/21.
 */
public class LoginActivity extends BaseTitleActivity {

    /* 账号 */
    private EditText mInputName;

    /* 密码 */
    private EditText mInputPwd;

    /* 登陆 */
    private TextView mLogin;

    /* 注册 */
    private TextView mRegister;

    private TextView mVerifyLogin;

    /* 切换登陆模式 */
    private TextView mSwitchLoginMode;

    private View mWXContainer;

    /* 微信登录 */
    private ImageView mWXLogin;

    /* 扫一扫 */
    private ImageView mIvScan;

    private ImageView mIvChangeAppId;

    /* 是否是id登陆 */
    private boolean mLoginByUserId = false;

    /* 输入监听 */
    private TextWatcher mInputWatcher;

    private TextView mTvAppId;

    private String mChangeAppId;

    private CompositeSubscription mSubscription;

    private TextView mTvRegisterProtocol;

    private CheckBox mCheckBox;

    public static void openLogin(Context context, boolean clearTask) {
        Intent intent = new Intent(context, LoginActivity.class);
        int flag = Intent.FLAG_ACTIVITY_NEW_TASK;
        if (clearTask){
            flag |= Intent.FLAG_ACTIVITY_CLEAR_TASK;
        }
        intent.addFlags(flag);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        return new Header.Builder(this, headerContainer).build();
    }

    private void buildProtocol() {
        mTvRegisterProtocol.setMovementMethod(LinkMovementMethod.getInstance());
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(getResources().getString(R.string.read_agree));
        // 用户服务
        SpannableString spannableString = new SpannableString(
                "《" + getResources().getString(R.string.register_protocol2) + "》");
        spannableString.setSpan(new ClickableSpan() {

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(getResources().getColor(R.color.color_4A90E2));
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(@NonNull View widget) {
                ProtocolActivity.openProtocol(LoginActivity.this, 1);
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
                ds.setColor(getResources().getColor(R.color.color_4A90E2));
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(@NonNull View widget) {
                ProtocolActivity.openProtocol(LoginActivity.this, 0);
            }
        }, 0, spannableString1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString1);
        mTvRegisterProtocol.setText(builder);
    }

    @Override
    protected View onCreateView() {
        hideHeader();
        View view = View.inflate(this, R.layout.activity_login, null);
        mInputName = view.findViewById(R.id.et_user_name);
        mInputPwd = view.findViewById(R.id.et_user_pwd);
        mLogin = view.findViewById(R.id.tv_login);
        mRegister = view.findViewById(R.id.tv_register);
        mVerifyLogin = view.findViewById(R.id.tv_verify);
        mWXContainer = view.findViewById(R.id.ll_wx_container);
        mWXLogin = view.findViewById(R.id.iv_wx_login);
        mIvScan = view.findViewById(R.id.iv_scan);
        mIvChangeAppId = view.findViewById(R.id.iv_app_id);
        mTvAppId = view.findViewById(R.id.tv_login_appid);
        mSwitchLoginMode = view.findViewById(R.id.tv_switch_login_mode);
        mSwitchLoginMode.setVisibility(View.GONE);
        mTvRegisterProtocol = view.findViewById(R.id.tv_register_protocol);
        mCheckBox = view.findViewById(R.id.cb_choice);

        buildProtocol();
        // 三次点击打开日志
        ClickTimeUtils.setClickTimes(view.findViewById(R.id.tv_open_log), 3, () -> {
            // 跳转查看日志
            LogViewActivity.openLogView(this, mChangeAppId);
        });
        
        initRxBus();
        return view;
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
                    String name = mInputName.getText().toString().trim();
                    String pwd = mInputPwd.getText().toString().trim();
                    login(this, name, pwd, mLoginByUserId, mChangeAppId);
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
        String name = mInputName.getText().toString().trim();
        String pwd = mInputPwd.getText().toString().trim();
        login(this, name, pwd, mLoginByUserId, mChangeAppId);
    }

    private void updateLoginButton() {
        boolean isNameEmpty = TextUtils.isEmpty(mInputName.getText().toString().trim());
        boolean isPwdEmpty = TextUtils.isEmpty(mInputPwd.getText().toString().trim());
        boolean isAgree = mCheckBox.isChecked();
        if (!isNameEmpty && !isPwdEmpty && isAgree) {
            mLogin.setEnabled(true);
        } else {
            mLogin.setEnabled(false);
        }
    }

    @Override
    protected void setViewListener() {
        // 注册
        mRegister.setOnClickListener(v -> RegisterActivity.openRegister(LoginActivity.this));
        // 验证码登录
        mVerifyLogin.setOnClickListener(v -> finish());
        // 登陆
        mLogin.setOnClickListener(v -> {
            String name = mInputName.getText().toString().trim();
            String pwd = mInputPwd.getText().toString().trim();
            login(this, name, pwd, mLoginByUserId, mChangeAppId);
        });
        // 微信登录
        mWXLogin.setOnClickListener(v -> {
            if (!WXUtils.getInstance().wxSupported(this)) {
                ToastUtil.showTextViewPrompt(getString(R.string.please_install_wechat));
                return;
            }
            boolean isAgree = mCheckBox.isChecked();
            if (!isAgree){
                ToastUtil.showTextViewPrompt(getString(R.string.check_first));
                return;
            }
            initWXRxBus();
            WXUtils.getInstance().wxLogin(CommonConfig.SourceToWX.TYPE_LOGIN, mChangeAppId);
        });
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateLoginButton();
            }
        });

        // 扫一扫
        mIvScan.setOnClickListener(v ->{
            boolean isAgree = mCheckBox.isChecked();
            if (!isAgree){
                ToastUtil.showTextViewPrompt(getString(R.string.check_first));
                return;
            }
            ScannerActivity.openScan(this);
        });
        mInputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateLoginButton();
            }
        };
        mInputName.addTextChangedListener(mInputWatcher);
        mInputPwd.addTextChangedListener(mInputWatcher);
        // 切换登陆模式
        mSwitchLoginMode.setOnClickListener(v -> {
            if (mLoginByUserId) {
                // id登陆 切换为用户名
                mInputName.setHint(R.string.login_user_name_hint);
                mInputName.setInputType(EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                mInputName.setHint(R.string.login_user_id_hint);
                mInputName.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
            }
            mLoginByUserId = !mLoginByUserId;
        });

        // 修改appId
        mIvChangeAppId.setOnClickListener(v -> {
            DNSConfigActivity.startDNSConfigActivity(this);
//            DialogUtils.getInstance().showEditDialog(this,
//                    "修改AppId", getString(R.string.confirm), getString(R.string.cancel),
//                    new CommonEditDialog.OnDialogListener() {
//                        @Override
//                        public void onConfirmListener(String content) {
//                            changeAppId(LoginActivity.this, content);
//                        }
//
//                        @Override
//                        public void onCancelListener() {
//
//                        }
//                    });
        });
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        // 判断当前是否是扫码登陆
        String scanUserName = ScanConfigs.CODE_USER_NAME;
        if (!TextUtils.isEmpty(scanUserName)) {
            mInputName.setText(scanUserName);
            ScanConfigs.CODE_USER_NAME = "";
        }
        String scanPassword = ScanConfigs.CODE_PASSWORD;
        if (!TextUtils.isEmpty(scanPassword)) {
            mInputPwd.setText(scanPassword);
            ScanConfigs.CODE_PASSWORD = "";
        }

        // 如果是扫码登陆 会将appId存入 会导致和默认appId不同
        String appId = SharePreferenceUtils.getInstance().getAppId();
        mTvAppId.setText(appId);
        if (!TextUtils.equals(appId, ScanConfigs.CODE_APP_ID)) {
            mChangeAppId = appId;
            mWXContainer.setVisibility(View.GONE);
        } else {
            mWXContainer.setVisibility(View.VISIBLE);
        }
    }

    public static void login(Activity activity, String name, String pwd, boolean isLoginById) {
        login(activity, name, pwd, isLoginById, SharePreferenceUtils.getInstance().getAppId());
    }

    public static void login(final Activity activity, String name, final String pwd,
            final boolean isLoginById, final String changeAppId) {
        // 登陆时候获取
        String dns = ScanConfigs.DNS_CONFIG;
        String server = "";
        int port = 0;
        String restServer = "";
        if (!TextUtils.isEmpty(dns)) {
            DNSConfigEvent event = new Gson().fromJson(dns, DNSConfigEvent.class);
            if (event != null) {
                server = event.getServer();
                port = event.getPort();
                restServer = event.getRestServer();
            }
        }
        login(activity, name, pwd, isLoginById, changeAppId, server, port, restServer);
    }

    public static void login(final Activity activity, String name, final String pwd,
            final boolean isLoginById, final String changeAppId, String server, int port, String restServer) {

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)) {
            ToastUtil.showTextViewPrompt(activity.getString(R.string.cannot_be_empty));
            return;
        }
        if (activity instanceof BaseTitleActivity && !activity.isFinishing()) {
            ((BaseTitleActivity)activity).showLoadingDialog(true);
        }
        // 是否自定义dns配置
        boolean changeDns = !TextUtils.isEmpty(server) && port > 0
                && !TextUtils.isEmpty(restServer);
        if (changeDns) {
            // 保存dns配置
            DNSConfigEvent event = new DNSConfigEvent();
            event.setAppId(changeAppId);
            event.setServer(server);
            event.setPort(port);
            event.setRestServer(restServer);
            SharePreferenceUtils.getInstance().putDNSConfig(new Gson().toJson(event));
            BaseManager.changeDNS(server, port, restServer);
        } else {
            // 还原
            SharePreferenceUtils.getInstance().putDNSConfig("");
            BaseManager.changeDNS("", 0, "");
        }
        BMXCallBack callBack = (bmxErrorCode) -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                //启动网络监听
                ConnectivityReceiver.start(AppContextUtils.getApplication());

                // 登陆成功后 需要将userId存储SP 作为下次自动登陆
                UserManager.getInstance().getProfile(true, (bmxErrorCode1, profile) -> {
                    TaskDispatcher.exec(() -> {
                        if (BaseManager.bmxFinish(bmxErrorCode1) && profile != null
                                && profile.userId() > 0) {
                            UserBean bean = new UserBean(profile.username(), profile.userId(), pwd,
                                    changeAppId, System.currentTimeMillis());
                            if (changeDns) {
                                bean.setServer(server);
                                bean.setPort(port);
                                bean.setRestServer(restServer);
                            }
                            CommonUtils.getInstance().addUser(bean);
                        }
                        TaskDispatcher.postMain(() -> {
                            if (activity instanceof BaseTitleActivity
                                    && !activity.isFinishing()) {
                                ((BaseTitleActivity)activity).dismissLoadingDialog();
                            }
                            // 登陆成功消息预加载
                            WelcomeActivity.initData();
                            SharePreferenceUtils.getInstance().putLoginStatus(true);
                            MessageDispatcher.getDispatcher().initialize();
                            MainActivity.openMain(activity);
                        });
                    });
                });
                return;
            }
            // 失败
            if (activity instanceof BaseTitleActivity && !activity.isFinishing()) {
                ((BaseTitleActivity)activity).dismissLoadingDialog();
            }
            String error = bmxErrorCode != null ? bmxErrorCode.name() : activity.getString(R.string.network_exception);
            ToastUtil.showTextViewPrompt(error);
        };
        boolean isChange = !TextUtils.isEmpty(changeAppId)
                && !TextUtils.equals(changeAppId, SharePreferenceUtils.getInstance().getAppId());
        if (isChange) {
            UserManager.getInstance().changeAppId(changeAppId, bmxErrorCode -> {
                if (BaseManager.bmxFinish(bmxErrorCode)) {
                    SharePreferenceUtils.getInstance().putAppId(changeAppId);
                    realLogin(isLoginById, name, pwd, callBack);
                } else {
                    // 失败
                    if (activity instanceof BaseTitleActivity && !activity.isFinishing()) {
                        ((BaseTitleActivity)activity).dismissLoadingDialog();
                    }
                    String error = bmxErrorCode != null ? bmxErrorCode.name() : activity.getString(R.string.switch_appId_failed);
                    ToastUtil.showTextViewPrompt(error);
                }
            });
        } else {
            realLogin(isLoginById, name, pwd, callBack);
        }
    }
    
    private static void realLogin(boolean isLoginById, String name, String pwd, BMXCallBack callBack) {
        if (isLoginById) {
            UserManager.getInstance().signInById(Long.valueOf(name), pwd, callBack);
        } else {
            // if (Pattern.matches("0?(13|14|15|17|18|19)[0-9]{9}", s)) {
            // // 手机号
            // return UserManager.getInstance().signInByPhone(s, pwd);
            // }
            UserManager.getInstance().signInByName(name, pwd, callBack);
        }
    }

    public static void wxChatLogin(final Activity activity, String openId) {
        if (TextUtils.isEmpty(openId)) {
            ToastUtil.showTextViewPrompt(activity.getString(R.string.cannot_be_empty));
            return;
        }
        if (activity instanceof BaseTitleActivity && !activity.isFinishing()) {
            ((BaseTitleActivity)activity).showLoadingDialog(true);
        }
        AppManager.getInstance().weChatLogin(openId, new HttpResponseCallback<String>() {
            @Override
            public void onResponse(String result) {
                if (activity instanceof BaseTitleActivity
                        && !activity.isFinishing()) {
                    ((BaseTitleActivity)activity).dismissLoadingDialog();
                }
                if (TextUtils.isEmpty(result)) {
                    ToastUtil.showTextViewPrompt("登陆失败");
                    return;
                }
                String appId = WXUtils.getInstance().getAppId();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (!jsonObject.has("user_id") || !jsonObject.has("password")) {
                        String openId = jsonObject.getString("openid");
                        // 没有userId 密码 需要跳转绑定微信页面
                        LoginBindUserActivity.openLoginBindUser(activity, openId, appId);
                        activity.finish();
                        return;
                    }
                    // 直接登录
                    String userId = jsonObject.getString("user_id");
                    String pwd = jsonObject.getString("password");
                    LoginFragment.login(activity, userId, pwd, true, appId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                if (activity instanceof BaseTitleActivity
                        && !activity.isFinishing()) {
                    ((BaseTitleActivity)activity).dismissLoadingDialog();
                }
                ToastUtil.showTextViewPrompt("登陆失败");
            }
        });
    }

    /**
     * 切换appId
     */
    public static void changeAppId(final Activity activity, String appId) {
        // 为空 置为默认的appId
        appId = TextUtils.isEmpty(appId) ? ScanConfigs.CODE_APP_ID : appId;
        String oldAppId = SharePreferenceUtils.getInstance().getAppId();
        if (TextUtils.equals(oldAppId, appId)) {
            // 相同不处理
            Intent intent = new Intent();
            intent.setAction(CommonConfig.CHANGE_APP_ID_ACTION);
            intent.putExtra(CommonConfig.CHANGE_APP_ID, appId);
            RxBus.getInstance().send(intent);
            return;
        }
        // TODO 在登陆时候再切换appId
        Intent intent = new Intent();
        intent.setAction(CommonConfig.CHANGE_APP_ID_ACTION);
        intent.putExtra(CommonConfig.CHANGE_APP_ID, appId);
        RxBus.getInstance().send(intent);
//        if (activity instanceof BaseTitleActivity && !activity.isFinishing()) {
//            ((BaseTitleActivity)activity).showLoadingDialog(true);
//        }
//        String finalAppId = appId;
//        UserManager.getInstance().changeAppId(appId, bmxErrorCode -> {
//            if (activity instanceof BaseTitleActivity && !activity.isFinishing()) {
//                ((BaseTitleActivity)activity).dismissLoadingDialog();
//            }
//            if (BaseManager.bmxFinish(bmxErrorCode)) {
//                SharePreferenceUtils.getInstance().putAppId(finalAppId);
//                ToastUtil.showTextViewPrompt("切换appId成功");
//                Intent intent = new Intent();
//                intent.setAction(CommonConfig.CHANGE_APP_ID_ACTION);
//                intent.putExtra(CommonConfig.CHANGE_APP_ID, finalAppId);
//                RxBus.getInstance().send(intent);
//            } else {
//                String error = bmxErrorCode != null ? bmxErrorCode.name() : "切换appId失败";
//                ToastUtil.showTextViewPrompt(error);
//            }
//        });
    }

    private void initWXRxBus() {
        if (mSubscription == null) {
            mSubscription = new CompositeSubscription();
        }
        Subscription wxLogin = RxBus.getInstance().toObservable(Intent.class)
                .subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Intent>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Intent intent) {
                        if (intent == null || !TextUtils.equals(intent.getAction(),
                                CommonConfig.WX_LOGIN_ACTION)) {
                            return;
                        }
                        if (mSubscription != null) {
                            mSubscription.unsubscribe();
                        }
                        String openId = intent.getStringExtra(CommonConfig.WX_OPEN_ID);
                        wxChatLogin(LoginActivity.this, openId);
                    }
                });
        mSubscription.add(wxLogin);
    }

    private void initRxBus() {
        if (mSubscription == null) {
            mSubscription = new CompositeSubscription();
        }
        // 切换appId
        Subscription changeAppId = RxBus.getInstance().toObservable(Intent.class)
                .subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Intent>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Intent intent) {
                        if (intent == null || !TextUtils.equals(intent.getAction(),
                                CommonConfig.CHANGE_APP_ID_ACTION)) {
                            return;
                        }
                        mChangeAppId = intent.getStringExtra(CommonConfig.CHANGE_APP_ID);
                        mTvAppId.setText(mChangeAppId);
                        mWXContainer.setVisibility(
                                TextUtils.equals(mChangeAppId, ScanConfigs.CODE_APP_ID)
                                        ? View.VISIBLE
                                        : View.GONE);
                    }
                });
        mSubscription.add(changeAppId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }
}
