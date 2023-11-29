
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import top.maxim.im.LoginRegisterActivity;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.common.base.BaseSwitchActivity;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.base.BaseTitleFragment;
import top.maxim.im.common.utils.CommonConfig;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.net.HttpResponseCallback;
import top.maxim.im.scan.config.ScanConfigs;
import top.maxim.im.scan.view.ScannerActivity;
import top.maxim.im.wxapi.WXUtils;

/**
 * Description : 登陆 Created by Mango on 2018/11/21.
 */
public class LoginByVerifyFragment extends BaseTitleFragment {

    /* 账号 */
    private EditText mInputName;

    /* 密码 */
    private EditText mInputPwd;

    /* 登陆 */
    private TextView mLogin;

    private TextView mVerifyLogin;

    /* 发送验证码 */
    private TextView mSendVerify;

    /* 验证码倒计时 */
    private TextView mVerifyCountDown;

    /* 注册 */
    private TextView mRegister;

    private View mWXContainer;

    /* 微信登录 */
    private ImageView mWXLogin;

    /* 扫一扫 */
    private ImageView mIvScan;

    private ImageView mIvChangeAppId;

    /* 输入监听 */
    private TextWatcher mInputWatcher;

    private TextView mTvAppId;

    private TextView mTvRegisterProtocol;
    private String mChangeAppId;

    private CompositeSubscription mSubscription;

    private boolean mCountDown;

    private CheckBox mCheckBox;

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

    public static void openLogin(Context context) {
        Intent intent = new Intent(context, LoginByVerifyFragment.class);
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
        View view = View.inflate(getActivity(), R.layout.activity_login_verify, null);
        mInputName = view.findViewById(R.id.et_user_phone);
        mInputPwd = view.findViewById(R.id.et_user_verify);
        mLogin = view.findViewById(R.id.tv_login);
        mVerifyLogin = view.findViewById(R.id.tv_verify);
        mSendVerify = view.findViewById(R.id.tv_send_verify);
        mSendVerify.setEnabled(false);
        mVerifyCountDown = view.findViewById(R.id.tv_send_verify_count_down);
        mRegister = view.findViewById(R.id.tv_register);
        mWXContainer = view.findViewById(R.id.iv_wx_login);
        mWXLogin = view.findViewById(R.id.iv_wx_login);
        mIvScan = view.findViewById(R.id.iv_scan);
        mIvChangeAppId = view.findViewById(R.id.iv_app_id);
        mTvAppId = view.findViewById(R.id.tv_login_appid);
        mTvRegisterProtocol = view.findViewById(R.id.tv_register_protocol);
        mCheckBox = view.findViewById(R.id.cb_choice);

        buildProtocol();
        initRxBus();
        return view;
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
        // 密码登录
        mVerifyLogin.setOnClickListener(v -> {
            activity.switchFragment(LoginRegisterActivity.LOGIN_REGISTER_INDEX.LOGIN_BY_USERNAME.ordinal());
        });
        // 登陆
        mLogin.setOnClickListener(v -> {
            String name = mInputName.getText().toString().trim();
            String pwd = mInputPwd.getText().toString().trim();
            loginByCaptcha(name, pwd);
        });
        // 微信登录
        mWXLogin.setOnClickListener(v -> {
            if (!WXUtils.getInstance().wxSupported()) {
                ToastUtil.showTextViewPrompt(getString(R.string.please_install_wechat));
                return;
            }
            boolean isAgree = mCheckBox.isChecked();
            if (!isAgree){
                ToastUtil.showTextViewPrompt(getString(R.string.check_first));
                return;
            }
            initWXRxBus();
            WXUtils.getInstance().wxLogin(CommonConfig.SourceToWX.TYPE_LOGIN_VERIFY, mChangeAppId);
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
        mInputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateLoginButton();
                boolean isNameEmpty = TextUtils.isEmpty(mInputName.getText().toString().trim());
                if (!mCountDown) {
                    mSendVerify.setEnabled(!isNameEmpty);
                }
            }
        });
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateLoginButton();
                SharePreferenceUtils.getInstance().putAgreeChecked(isChecked);
            }
        });

        mInputPwd.addTextChangedListener(mInputWatcher);
        // 修改appId
        mIvChangeAppId.setOnClickListener(v -> {
            DNSConfigActivity.startDNSConfigActivity(getActivity());
        });
        // 发送验证码
        mSendVerify.setOnClickListener(v -> {
            verifyCountDown();
            String phone = mInputName.getEditableText().toString().trim();
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
                    }
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

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        // 判断当前是否是扫码登陆
        String appId = SharePreferenceUtils.getInstance().getAppId();
        mTvAppId.setText(appId);
        boolean agreeChecked = SharePreferenceUtils.getInstance().getAgreeChecked();
        if (agreeChecked){
            mCheckBox.setChecked(true);
        }
    }

    private void loginByCaptcha(String mobile, String captcha) {
        showLoadingDialog(true);
        AppManager.getInstance().getInfoPwdByCaptcha(mobile, captcha,
                new HttpResponseCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        dismissLoadingDialog();
                        if (TextUtils.isEmpty(result)) {
                            ToastUtil.showTextViewPrompt(getString(R.string.failed_to_login));
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (!jsonObject.has("username") || !jsonObject.has("password")) {
                                // 没有用户名 密码 需要跳转绑定用户页面
                                if (jsonObject.has("sign")){
                                    String sign = jsonObject.getString("sign");
                                    LoginBindUserActivity.openLoginBindUserWithSign(getActivity(), mobile,
                                            sign, mChangeAppId);
                                }
                                getActivity().finish();
                                return;
                            }
                            // 直接登录
                            String username = jsonObject.getString("username");
                            String password = jsonObject.getString("password");
                            LoginFragment.login(getActivity(), username, password,
                                    false, mChangeAppId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        dismissLoadingDialog();
                        ToastUtil.showTextViewPrompt(errorMsg);
                    }
                });
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
                        LoginFragment.wxChatLogin(getActivity(), openId);
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
