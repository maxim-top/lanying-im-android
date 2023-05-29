
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import im.floo.floolib.BMXErrorCode;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.CommonConfig;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.login.bean.DNSConfigEvent;
import top.maxim.im.net.HttpResponseCallback;
import top.maxim.im.scan.config.ScanConfigs;
import top.maxim.im.wxapi.WXUtils;

/**
 * Description : 登陆 Created by Mango on 2018/11/21.
 */
public class RegisterActivity extends BaseTitleActivity {

    /* 关闭 */
    private TextView mClose;

    /* 账号 */
    private EditText mInputName;

    /* 密码 */
    private EditText mInputPwd;

    /* 手机号 */
    private EditText mInputPhone;

    /* 验证码 */
    private EditText mInputVerify;

    /* 注册 */
    private TextView mRegister;

    /* 发送验证码 */
    private TextView mSendVerify;

    /* 验证码倒计时 */
    private TextView mVerifyCountDown;

    /* 输入监听 */
    private TextWatcher mInputWatcher;

    private TextView mTvAppId;

    private ImageView mIvChangeAppId;

    private String mChangeAppId;

    private TextView mTvRegisterProtocol;

    private View mWXContainer;

    /* 微信登录 */
    private ImageView mWXLogin;

    public static final String REFISTER_ACCOUNT = "registerAccount";

    public static final String REFISTER_PWD = "registerPwd";

    private CompositeSubscription mSubscription;

    private boolean mCountDown;

    private CountDownTimer timer = new CountDownTimer(60 * 1000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
            mCountDown = true;
            mVerifyCountDown.setText(millisUntilFinished / 1000 + "s后重发");
        }

        @Override
        public void onFinish() {
            mCountDown = false;
            mSendVerify.setEnabled(true);
            mSendVerify.setVisibility(View.VISIBLE);
            mVerifyCountDown.setText("");
        }
    };

    public static void openRegister(Context context) {
        Intent intent = new Intent(context, RegisterActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        return new Header.Builder(this, headerContainer).build();
    }

    @Override
    protected View onCreateView() {
        hideHeader();
        View view = View.inflate(this, R.layout.activity_register, null);
        mClose = view.findViewById(R.id.tv_register_close);
        mInputName = view.findViewById(R.id.et_user_name);
        mInputPwd = view.findViewById(R.id.et_user_pwd);
        mInputPhone = view.findViewById(R.id.et_user_phone);
        mSendVerify = view.findViewById(R.id.tv_send_verify);
        mSendVerify.setEnabled(false);
        mVerifyCountDown = view.findViewById(R.id.tv_send_verify_count_down);
        mInputVerify = view.findViewById(R.id.et_user_verify);
        mRegister = view.findViewById(R.id.tv_register);
        mIvChangeAppId = view.findViewById(R.id.iv_app_id);
        mTvAppId = view.findViewById(R.id.tv_login_appid);
        mTvRegisterProtocol = view.findViewById(R.id.tv_register_protocol);
        mWXContainer = view.findViewById(R.id.ll_wx_container);
        mWXLogin = view.findViewById(R.id.iv_wx_login);
        buildProtocol();
        view.findViewById(R.id.ll_et_user_phone).setVisibility(View.GONE);
        view.findViewById(R.id.ll_et_user_verify).setVisibility(View.GONE);
        initRxBus();
        return view;
    }

    @Override
    protected void initDataForActivity() {
        String appId = SharePreferenceUtils.getInstance().getAppId();
        mTvAppId.setText("APPID:" + appId);
        if (!TextUtils.equals(appId, ScanConfigs.CODE_APP_ID)) {
            mChangeAppId = appId;
            mWXContainer.setVisibility(View.GONE);
        } else {
            mWXContainer.setVisibility(View.VISIBLE);
        }
    }

    private void buildProtocol() {
        mTvRegisterProtocol.setMovementMethod(LinkMovementMethod.getInstance());
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(getResources().getString(R.string.register_protocol1));
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
                ProtocolActivity.openProtocol(RegisterActivity.this, 1);
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
                ProtocolActivity.openProtocol(RegisterActivity.this, 0);
            }
        }, 0, spannableString1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString1);
        mTvRegisterProtocol.setText(builder);
    }

    @Override
    protected void setViewListener() {
        // 关闭
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // 微信登录
        mWXLogin.setOnClickListener(v -> {
            if (!WXUtils.getInstance().wxSupported()) {
                ToastUtil.showTextViewPrompt(getString(R.string.please_install_wechat));
                return;
            }
            initWXRxBus();
            WXUtils.getInstance().wxLogin(CommonConfig.SourceToWX.TYPE_REGISTER, mChangeAppId);
        });
        // 注册
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = mInputName.getEditableText().toString().trim();
                String pwd = mInputPwd.getEditableText().toString().trim();
                String phone = mInputPhone.getEditableText().toString().trim();
                String verify = mInputVerify.getEditableText().toString().trim();
                checkName(account, pwd);
            }
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
                boolean isNameEmpty = TextUtils.isEmpty(mInputName.getText().toString().trim());
                boolean isPwdEmpty = TextUtils.isEmpty(mInputPwd.getText().toString().trim());
                boolean isPhoneEmpty = TextUtils.isEmpty(mInputPhone.getText().toString().trim());
                boolean isVerifyEmpty = TextUtils.isEmpty(mInputVerify.getText().toString().trim());
                if (!isNameEmpty && !isPwdEmpty
                // && !isPhoneEmpty && !isVerifyEmpty
                ) {
                    mRegister.setEnabled(true);
                } else {
                    mRegister.setEnabled(false);
                }
            }
        };
        mInputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean isNameEmpty = TextUtils.isEmpty(mInputName.getText().toString().trim());
                boolean isPwdEmpty = TextUtils.isEmpty(mInputPwd.getText().toString().trim());
                boolean isPhoneEmpty = TextUtils.isEmpty(mInputPhone.getText().toString().trim());
                boolean isVerifyEmpty = TextUtils.isEmpty(mInputVerify.getText().toString().trim());
                if (!isNameEmpty && !isPwdEmpty
                    // && !isPhoneEmpty && !isVerifyEmpty
                ) {
                    mRegister.setEnabled(true);
                } else {
                    mRegister.setEnabled(false);
                }
                if (!mCountDown) {
                    mSendVerify.setEnabled(!isNameEmpty);
                }
            }
        });
        mInputPwd.addTextChangedListener(mInputWatcher);
        mInputPhone.addTextChangedListener(mInputWatcher);
        mInputVerify.addTextChangedListener(mInputWatcher);
        // 发送验证码
        mSendVerify.setOnClickListener(v -> {
            verifyCountDown();
            String phone = mInputPhone.getEditableText().toString().trim();
            AppManager.getInstance().captchaSMS(phone, new HttpResponseCallback<Boolean>() {
                @Override
                public void onResponse(Boolean result) {
                    if (result != null && result) {
                        ToastUtil.showTextViewPrompt(getString(R.string.get_captcha_successfully));
                    } else {
                        ToastUtil.showTextViewPrompt(getString(R.string.failed_to_get_captcha));
                        mSendVerify.setEnabled(true);
                        mSendVerify.setVisibility(View.VISIBLE);
                        mVerifyCountDown.setText("");
                        timer.cancel();
                    }//                    mSendVerify.setEnabled(true);
//                    mVerifyCountDown.setText("");
//                    timer.cancel();
                }

                @Override
                public void onFailure(int errorCode, String errorMsg, Throwable t) {
                    ToastUtil.showTextViewPrompt(getString(R.string.failed_to_get_captcha));
                    mSendVerify.setEnabled(true);
                    mSendVerify.setVisibility(View.VISIBLE);
                    mVerifyCountDown.setText("");
                    timer.cancel();
                }
            });
        });
        // 修改appId
        mIvChangeAppId.setOnClickListener(v -> {
            DNSConfigActivity.startDNSConfigActivity(this);
//            DialogUtils.getInstance().showEditDialog(this,
//                    "修改AppId", getString(R.string.confirm), getString(R.string.cancel),
//                    new CommonEditDialog.OnDialogListener() {
//                        @Override
//                        public void onConfirmListener(String content) {
//                            LoginActivity.changeAppId(RegisterActivity.this, content);
//                        }
//
//                        @Override
//                        public void onCancelListener() {
//
//                        }
//                    });
        });
    }

    /**
     * 校验用户名
     */
    private void checkName(String userName, String pwd) {
        register(userName, pwd);
//        AppManager.getInstance().checkName(userName, new HttpResponseCallback<Boolean>() {
//            @Override
//            public void onResponse(Boolean result) {
//                if (result == null || !result) {
//                    // 不可用
//                    ToastUtil.showTextViewPrompt("账号已被注册");
//                    return;
//                }
//                // 校验成功 注册
//                register(userName, pwd);
//            }
//
//            @Override
//            public void onFailure(int errorCode, String errorMsg, Throwable t) {
//                dismissLoadingDialog();
//                ToastUtil.showTextViewPrompt(errorMsg);
//            }
//        });
    }

    private void register(final String account, final String pwd) {

        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(pwd)
        // || TextUtils.isEmpty(phone)
        // || TextUtils.isEmpty(verify)
        ) {
            ToastUtil.showTextViewPrompt(getString(R.string.cannot_be_empty));
            return;
        }
        // 注册时候获取 对其进行修改
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
        // 是否自定义dns配置
        boolean changeDns = !TextUtils.isEmpty(server) && port > 0
                && !TextUtils.isEmpty(restServer);
        if (changeDns) {
            // 保存dns配置
            DNSConfigEvent event = new DNSConfigEvent();
            event.setAppId(mChangeAppId);
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
        //是否需要切换appId
        boolean isChange = !TextUtils.isEmpty(mChangeAppId)
                && !TextUtils.equals(mChangeAppId, SharePreferenceUtils.getInstance().getAppId());
        if (isChange) {
            UserManager.getInstance().changeAppId(mChangeAppId, bmxErrorCode -> {
                if (BaseManager.bmxFinish(bmxErrorCode)) {
                    SharePreferenceUtils.getInstance().putAppId(mChangeAppId);
                    realRegister(account, pwd);
                } else {
                    // 失败
                    String error = bmxErrorCode != null ? bmxErrorCode.name() : getString(R.string.switch_appId_failed);
                    ToastUtil.showTextViewPrompt(error);
                }
            });
        } else {
            realRegister(account, pwd);
        }

    }

    private void realRegister(String account, String pwd){
        showLoadingDialog(true);
        UserManager.getInstance().signUpNewUser(account, pwd, (bmxErrorCode, bmxUserProfile) -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                RegisterBindMobileActivity.openRegisterBindMobile(RegisterActivity.this,
                        mInputName.getEditableText().toString().trim(),
                        mInputPwd.getEditableText().toString().trim(), mChangeAppId);
                finish();
            } else {
                if (bmxErrorCode.swigValue() == BMXErrorCode.InvalidRequestParameter.swigValue()) {
                    ToastUtil.showTextViewPrompt(getString(R.string.username_only_supports));
                } else if (bmxErrorCode.equals(BMXErrorCode.UserAlreadyExist)) {
                    ToastUtil.showTextViewPrompt(getString(R.string.exit_user));
                } else if (bmxErrorCode.equals(BMXErrorCode.ServerUnknownError)) {
                    ToastUtil.showTextViewPrompt(getString(R.string.exit_user));
                } else {
                    ToastUtil.showTextViewPrompt(getString(R.string.network_exception));
                }
            }
        });
    }

    /**
     * 验证码倒计时60s
     */
    public void verifyCountDown() {
        mCountDown = true;
        mSendVerify.setEnabled(false);
        mSendVerify.setVisibility(View.GONE);
        timer.start();
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
                        LoginActivity.wxChatLogin(RegisterActivity.this, openId);
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
                        mTvAppId.setText("APPID:" + mChangeAppId);
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
