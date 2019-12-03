
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
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

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXUserProfile;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.net.HttpResponseCallback;

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

    public static final String REFISTER_ACCOUNT = "registerAccount";

    public static final String REFISTER_PWD = "registerPwd";

    public static final String OPEN_ID = "openId";

    /* 微信登录返回的openId */
    private String mOpenId;

    private CountDownTimer timer = new CountDownTimer(60 * 1000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
            mVerifyCountDown.setText(millisUntilFinished / 1000 + "s");
        }

        @Override
        public void onFinish() {
            mVerifyCountDown.setText("");
        }
    };

    public static void openRegister(Context context) {
        openRegister(context, null);
    }

    public static void openRegister(Context context, String openId) {
        Intent intent = new Intent(context, RegisterActivity.class);
        if (!TextUtils.isEmpty(openId)) {
            intent.putExtra(OPEN_ID, openId);
        }
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
        mVerifyCountDown = view.findViewById(R.id.tv_send_verify_count_down);
        mInputVerify = view.findViewById(R.id.et_user_verify);
        mRegister = view.findViewById(R.id.tv_register);
        mIvChangeAppId = view.findViewById(R.id.iv_app_id);
        mTvAppId = view.findViewById(R.id.tv_login_appid);
        mTvRegisterProtocol = view.findViewById(R.id.tv_register_protocol);
        buildProtocol();
        return view;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mOpenId = intent.getStringExtra(OPEN_ID);
        }
    }

    @Override
    protected void initDataForActivity() {
        String appId = SharePreferenceUtils.getInstance().getAppId();
        mTvAppId.setText("APPID:" + appId);
    }

    private void buildProtocol() {
        mTvRegisterProtocol.setMovementMethod(LinkMovementMethod.getInstance());
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(getResources().getString(R.string.register_protocol1));
        //用户服务
        ClickableSpan span = new ClickableSpan() {

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(getResources().getColor(R.color.color_0079F4));
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(@NonNull View widget) {
                ProtocolActivity.openProtocol(RegisterActivity.this, 1);
            }
        };
        SpannableString spannableString = new SpannableString(
                "《" + getResources().getString(R.string.register_protocol2) + "》");
        spannableString.setSpan(span, 0, spannableString.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        // 隐私政策
        builder.append(getResources().getString(R.string.register_protocol3));
        ClickableSpan span1 = new ClickableSpan() {

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(getResources().getColor(R.color.color_0079F4));
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(@NonNull View widget) {
                ProtocolActivity.openProtocol(RegisterActivity.this, 0);
            }
        };
        SpannableString spannableString1 = new SpannableString(
                "《" + getResources().getString(R.string.register_protocol4) + "》");
        spannableString1.setSpan(span1, 0, spannableString1.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString1);
        mTvRegisterProtocol.setText(builder);
    }

    @Override
    protected void setViewListener() {
        // 关闭
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.openLogin(RegisterActivity.this, mOpenId);
                finish();
            }
        });
        // 注册成功
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = mInputName.getEditableText().toString().trim();
                String pwd = mInputPwd.getEditableText().toString().trim();
                String phone = mInputPhone.getEditableText().toString().trim();
                String verify = mInputVerify.getEditableText().toString().trim();
                register(account, pwd, phone, verify);
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
                if (!isNameEmpty && !isPwdEmpty && !isPhoneEmpty && !isVerifyEmpty) {
                    mRegister.setEnabled(true);
                } else {
                    mRegister.setEnabled(false);
                }
            }
        };
        mInputName.addTextChangedListener(mInputWatcher);
        mInputPwd.addTextChangedListener(mInputWatcher);
        mInputPhone.addTextChangedListener(mInputWatcher);
        mInputVerify.addTextChangedListener(mInputWatcher);
        // 发送验证码
        mSendVerify.setOnClickListener(v -> {
            verifyCountDown();
            String phone = mInputPhone.getEditableText().toString().trim();
            AppManager.getInstance().captchaSMS(phone, new HttpResponseCallback<String>() {
                @Override
                public void onResponse(String result) {
                    ToastUtil.showTextViewPrompt("获取验证码成功");
                    mSendVerify.setEnabled(true);
                    mVerifyCountDown.setText("");
                    timer.cancel();
                }

                @Override
                public void onFailure(int errorCode, String errorMsg, Throwable t) {
                    ToastUtil.showTextViewPrompt("获取验证码失败");
                    mSendVerify.setEnabled(true);
                    mVerifyCountDown.setText("");
                    timer.cancel();
                }
            });
        });
        // 修改appId
        mIvChangeAppId.setOnClickListener(v -> DialogUtils.getInstance().showEditDialog(this,
                "修改AppId", getString(R.string.confirm), getString(R.string.cancel),
                new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        mChangeAppId = content;
                        mTvAppId.setText("APPID:" + content);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                }));
    }

    void register(final String account, final String pwd, final String phone, final String verify) {

        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(pwd) || TextUtils.isEmpty(phone)
                || TextUtils.isEmpty(verify)) {
            ToastUtil.showTextViewPrompt("不能为空");
            return;
        }
        showLoadingDialog(true);
        Observable.just(account).map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                return UserManager.getInstance().signUpNewUser(phone, verify, pwd, account,
                        new BMXUserProfile());
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
                        ToastUtil.showTextViewPrompt("网络异常");
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        dismissLoadingDialog();
                        if (TextUtils.isEmpty(mOpenId)) {
                            LoginActivity.login(RegisterActivity.this,
                                    mInputName.getEditableText().toString().trim(),
                                    mInputPwd.getEditableText().toString().trim(), false,
                                    mChangeAppId);
                        } else {
                            LoginActivity.bindOpenId(RegisterActivity.this,
                                    mInputName.getEditableText().toString().trim(),
                                    mInputPwd.getEditableText().toString().trim(), mOpenId);
                        }
                    }
                });
    }

    /**
     * 验证码倒计时60s
     */
    public void verifyCountDown() {
        mSendVerify.setEnabled(false);
        timer.start();
    }
}
