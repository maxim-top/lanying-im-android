
package top.maxim.im.login.view;

import static org.webrtc.ContextUtils.getApplicationContext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import im.floo.BMXCallBack;
import im.floo.floolib.BMXClient;
import im.floo.floolib.BMXErrorCode;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import top.maxim.im.BuildConfig;
import top.maxim.im.LoginRegisterActivity;
import top.maxim.im.MainActivity;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseSwitchActivity;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.base.BaseTitleFragment;
import top.maxim.im.common.bean.UserBean;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.common.utils.ClickTimeUtils;
import top.maxim.im.common.utils.CommonConfig;
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.utils.IPAddressUtil;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.TaskDispatcher;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonCustomDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.utils.permissions.PermissionsConstant;
import top.maxim.im.common.view.BrowserActivity;
import top.maxim.im.common.view.Header;
import top.maxim.im.login.bean.DNSConfigEvent;
import top.maxim.im.net.ConnectivityReceiver;
import top.maxim.im.net.HttpResponseCallback;
import top.maxim.im.scan.config.ScanConfigs;
import top.maxim.im.scan.view.ScannerActivity;
import top.maxim.im.sdk.utils.MessageDispatcher;
import top.maxim.im.wxapi.WXUtils;
import top.maxim.rtc.RTCManager;

/**
 * Description : 登陆 Created by Mango on 2018/11/21.
 */
public class LoginFragment extends BaseTitleFragment {

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

    private TextView mTvCompany;

    private String mChangeAppId;

    private CompositeSubscription mSubscription;

    private TextView mTvRegisterProtocol;

    private CheckBox mCheckBox;

    public static void openLogin(Context context) {
        Intent intent = new Intent(context, LoginFragment.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        return new Header.Builder(getActivity(), headerContainer).build();
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
                ProtocolActivity.openProtocol(getActivity(), 1);
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
                ProtocolActivity.openProtocol(getActivity(), 0);
            }
        }, 0, spannableString1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString1);
        mTvRegisterProtocol.setText(builder);
    }

    @Override
    protected View onCreateView() {
        hideHeader();
        View view = View.inflate(getActivity(), R.layout.activity_login, null);
        mInputName = view.findViewById(R.id.et_user_name);
        mInputPwd = view.findViewById(R.id.et_user_pwd);
        mLogin = view.findViewById(R.id.tv_login);
        mRegister = view.findViewById(R.id.tv_register);
        mVerifyLogin = view.findViewById(R.id.tv_verify);
        mWXContainer = view.findViewById(R.id.iv_wx_login);
        mWXLogin = view.findViewById(R.id.iv_wx_login);
        mIvScan = view.findViewById(R.id.iv_scan);
        mIvChangeAppId = view.findViewById(R.id.iv_app_id);
        mTvAppId = view.findViewById(R.id.tv_login_appid);
        mTvCompany = view.findViewById(R.id.tv_company);
        mSwitchLoginMode = view.findViewById(R.id.tv_switch_login_mode);
        mSwitchLoginMode.setVisibility(View.GONE);
        mTvRegisterProtocol = view.findViewById(R.id.tv_register_protocol);
        mCheckBox = view.findViewById(R.id.cb_choice);
        boolean showed = SharePreferenceUtils.getInstance().getProtocolDialogStatus();
        if (showed){
            CommonUtils.initializeSDKAndAppId();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String companyName = CommonUtils.getCompanyName(getContext());
                            String verificationStatus = CommonUtils.getVerificationStatusChar(getContext());
                            mTvCompany.setText(companyName + " " + verificationStatus);
                        }
                    });
                }
            }, 100);
        }

        buildProtocol();
        // 三次点击打开日志
        ClickTimeUtils.setClickTimes(view.findViewById(R.id.tv_open_log), 3, () -> {
            // 跳转查看日志
            LogViewActivity.openLogView(getActivity(), mChangeAppId);
        });
        
        initRxBus();
        showProtocol();
        return view;
    }

    /**
     * 展示用户协议
     */
    private void showProtocol() {
        boolean show = SharePreferenceUtils.getInstance().getProtocolDialogStatus();
        if (show) {
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
                ProtocolActivity.openProtocol(getContext(), 1);
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
                ProtocolActivity.openProtocol(getContext(), 0);
            }
        }, 0, spannableString1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString1);
        builder.append(getString(R.string.register_protocol_content2));

        TextView tv = new TextView(getContext());
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(builder);
        DialogUtils.getInstance().showCustomDialog(getActivity(), tv, false, title.toString(), getString(R.string.agree), getString(R.string.not_available_for_now),
                new CommonCustomDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener() {
                        SharePreferenceUtils.getInstance().putProtocolDialogStatus(true);

                        CommonUtils.initializeSDKAndAppId();

                        String appId = SharePreferenceUtils.getInstance().getAppId();
                        if (!SharePreferenceUtils.getInstance().getAboutPoppedAppId(appId)){
                            AboutUsActivity.startAboutUsActivity(getContext(), true);
                            SharePreferenceUtils.getInstance().putAboutPoppedAppId(appId);
                        }
                    }

                    @Override
                    public void onCancelListener() {
                        getActivity().finishAffinity();
                    }
                });
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
                    login(getActivity(), name, pwd, mLoginByUserId, mChangeAppId);
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
        login(getActivity(), name, pwd, mLoginByUserId, mChangeAppId);
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
        BaseSwitchActivity activity = (BaseSwitchActivity)getActivity();
        // 注册
        mRegister.setOnClickListener(v -> {
            activity.switchFragment(LoginRegisterActivity.LOGIN_REGISTER_INDEX.REGISTER.ordinal());
        });
        // 验证码登录
        mVerifyLogin.setOnClickListener(v -> activity.switchFragment(LoginRegisterActivity.LOGIN_REGISTER_INDEX.LOGIN_BY_VERIFY.ordinal()));
        // 登陆
        mLogin.setOnClickListener(v -> {
            String name = mInputName.getText().toString().trim();
            String pwd = mInputPwd.getText().toString().trim();
            login(getActivity(), name, pwd, mLoginByUserId, mChangeAppId);
        });
        // 微信登录
        mWXLogin.setOnClickListener(v -> {
            if (!WXUtils.getInstance().wxSupported(activity)) {
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
        if (WXUtils.getInstance().getWXApi() != null && !WXUtils.getInstance().wxSupported(activity)){
            mWXLogin.setVisibility(View.GONE);
        }
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateLoginButton();
                SharePreferenceUtils.getInstance().putAgreeChecked(isChecked);
            }
        });

        // 扫一扫
        mIvScan.setOnClickListener(v ->{
            boolean isAgree = mCheckBox.isChecked();
            if (!isAgree){
                ToastUtil.showTextViewPrompt(getString(R.string.check_first));
                return;
            }
            ScannerActivity.openScan(getActivity());
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
            DNSConfigActivity.startDNSConfigActivity(getActivity());
        });
        mTvCompany.setOnClickListener(v -> {
            AboutUsActivity.startAboutUsActivity(getContext(), false);
        });
        mTvAppId.setOnClickListener(v -> {
            AboutUsActivity.startAboutUsActivity(getContext(), false);
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
        }
        boolean agreeChecked = SharePreferenceUtils.getInstance().getAgreeChecked();
        if (agreeChecked){
            mCheckBox.setChecked(true);
        }
        String name = mInputName.getText().toString().trim();
        String pwd = mInputPwd.getText().toString().trim();
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(pwd) && agreeChecked){
            login(getActivity(), name, pwd, mLoginByUserId, mChangeAppId);
        }
    }

    public static void login(Activity activity, String name, String pwd, boolean isLoginById) {
        login(activity, name, pwd, isLoginById, SharePreferenceUtils.getInstance().getAppId());
    }

    /**
     * 显示错误对话框
     */
    private static void showErrorDialog(final Activity activity, String msg, String solution,
                                        String deviceInfo, String appId) {
        final LinearLayout ll = new LinearLayout(activity);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams editP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editP.setMargins(ScreenUtils.dp2px(10), ScreenUtils.dp2px(5), ScreenUtils.dp2px(10),
                ScreenUtils.dp2px(5));
        // msg
        TextView tvMsg = new TextView(activity);
        tvMsg.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(5), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(5));
        tvMsg.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        tvMsg.setTextColor(activity.getResources().getColor(R.color.color_black));
        tvMsg.setBackgroundColor(activity.getResources().getColor(R.color.color_white));
        tvMsg.setText(msg);
        ll.addView(tvMsg, textP);

        // solution
        TextView tvSolution = new TextView(activity);
        tvSolution.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(5), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(5));
        tvSolution.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        tvSolution.setTextColor(activity.getResources().getColor(R.color.color_black));
        tvSolution.setBackgroundColor(activity.getResources().getColor(R.color.color_white));
        tvSolution.setText(activity.getString(R.string.solution_colon)+solution);
        ll.addView(tvSolution, textP);

        // solution
        TextView tvDevInfo = new TextView(activity);
        tvDevInfo.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(5), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(5));
        tvDevInfo.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        tvDevInfo.setTextColor(activity.getResources().getColor(R.color.color_5c5c5c));
        tvDevInfo.setBackgroundColor(activity.getResources().getColor( R.color.color_white));
        tvDevInfo.setText(deviceInfo);
        ll.addView(tvDevInfo, textP);

        DialogUtils.getInstance().showCustomDialog(activity, ll,
                activity.getString(R.string.failed_to_login), activity.getString(R.string.show_log),
                activity.getString(R.string.faq), new CommonCustomDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener() {
                        LogViewActivity.openLogView(activity, appId);
                    }

                    @Override
                    public void onCancelListener() {
                        BrowserActivity.open(activity, "FAQ", "https://docs.lanyingim.com/faq/");
                    }
                });
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
                SharePreferenceUtils.getInstance().putAppIdHistory();
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
            String ip = IPAddressUtil.getIPAddress(getApplicationContext());
            if (TextUtils.isEmpty(ip)){
                ip = "No IP";
            }
            String appVersion = BuildConfig.VERSION_NAME;
            BMXClient client = BaseManager.getBMXClient();
            String sdkVersion = "";
            if (client != null && client.getSDKConfig() != null
                    && !TextUtils.isEmpty(client.getSDKConfig().getSDKVersion())) {
                sdkVersion = client.getSDKConfig().getSDKVersion();
            }

            String deviceInfo = android.os.Build.BRAND + ";" + Build.PRODUCT + "; " + Build.VERSION.RELEASE + ";" +
                    Build.VERSION.SDK_INT + ";" + ip + ";" + appVersion + ";"+ sdkVersion;
            showErrorDialog(activity, activity.getString(CommonUtils.getErrorMessage(bmxErrorCode)) , activity.getString(CommonUtils.getErrorSolution(bmxErrorCode)), deviceInfo, changeAppId);
//            String error = bmxErrorCode != null ? bmxErrorCode.name() : activity.getString(R.string.network_exception);
//            ToastUtil.showTextViewPrompt(error);
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
            UserManager.getInstance().signInByName(name, pwd, callBack);
        }
    }

    public static void wxChatLogin(final Activity activity, String openId) {
        if (TextUtils.equals(openId, "official_account_followed=true")) {
            ToastUtil.showTextViewPrompt(activity.getString(R.string.cannot_be_empty));
            String appId = SharePreferenceUtils.getInstance().getAppId();
            long userId = SharePreferenceUtils.getInstance().getUserId();
            String pwd = SharePreferenceUtils.getInstance().getUserPwd();
            LoginFragment.login(activity, String.valueOf(userId), pwd, true, appId);
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
                String appId = SharePreferenceUtils.getInstance().getAppId();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (!jsonObject.has("user_id") || !jsonObject.has("password")) {
                        String openId = jsonObject.getString("openid");
                        // 没有userId 密码 需要跳转绑定微信页面
                        LoginBindUserActivity.openLoginBindUser(activity, openId, appId);
                        activity.finish();
                        return;
                    }
                    String userId = jsonObject.getString("user_id");
                    String pwd = jsonObject.getString("password");
                    boolean official_account_followed = false;
                    if (jsonObject.has("official_account_followed")) {
                        official_account_followed = jsonObject.getBoolean("official_account_followed");
                    }
                    if (!official_account_followed){
                        SharePreferenceUtils.getInstance().putAppId(appId);
                        SharePreferenceUtils.getInstance().putUserId(Long.parseLong(userId));
                        SharePreferenceUtils.getInstance().putUserPwd(pwd);
                        String path = String.format("pages/profile/official_account/index?app_id=%s&user_id=%s", appId, userId);
                        WXUtils.getInstance().wxMiniProgram("gh_11b8debeb062", path);
                        return;
                    }
                    // 直接登录
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
                        String openId = intent.getStringExtra(CommonConfig.WX_OPEN_ID);
                        if(openId.contains("official_account_followed=false")){
                            getActivity().finish();
                            LoginRegisterActivity.openLoginRegister(getActivity(), false);
                        }else{
                            wxChatLogin(getActivity(), openId);
                        }
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

                        SharePreferenceUtils.getInstance().putAppId(mChangeAppId);
                        UserManager.getInstance().changeAppId(mChangeAppId, bmxErrorCode -> {
                            if (bmxErrorCode != BMXErrorCode.NoError){
                                ToastUtil.showTextViewPrompt(getString(R.string.error_code_ServerAppIdInvalid));
                            }
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String companyName = CommonUtils.getCompanyName(getContext());
                                            String verificationStatus = CommonUtils.getVerificationStatusChar(getContext());
                                            mTvCompany.setText(companyName + " " + verificationStatus);
                                        }
                                    });
                                }
                            }, 100);
                        });
                    }
                });
        mSubscription.add(changeAppId);
    }

    @Override
    public void onDestroyView() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
        super.onDestroyView();
    }

}
