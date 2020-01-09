
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.net.HttpResponseCallback;

/**
 * Description : 注册后绑定手机 Created by Mango on 2018/11/21.
 */
public class RegisterBindMobileActivity extends BaseTitleActivity {

    public static String LOGIN_NAME = "loginName";

    public static String LOGIN_PWD = "loginPwd";

    public static String LOGIN_APP_ID = "loginAppId";

    /* 账号 */
    private EditText mInputName;

    /* 密码 */
    private EditText mInputPwd;

    /* 登陆 */
    private TextView mLogin;

    /* 发送验证码 */
    private TextView mSendVerify;

    /* 验证码倒计时 */
    private TextView mVerifyCountDown;

    /* 扫一扫 */
    private TextView mTvSkip;

    /* 输入监听 */
    private TextWatcher mInputWatcher;

    private String userName;

    private String userPwd;

    private String mAppId;

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

    public static void openRegisterBindMobile(Context context, String name, String pwd, String appId) {
        Intent intent = new Intent(context, RegisterBindMobileActivity.class);
        intent.putExtra(LOGIN_NAME, name);
        intent.putExtra(LOGIN_PWD, pwd);
        intent.putExtra(LOGIN_APP_ID, appId);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        return new Header.Builder(this, headerContainer).build();
    }

    @Override
    protected View onCreateView() {
        hideHeader();
        View view = View.inflate(this, R.layout.activity_register_bind_mobile, null);
        mInputName = view.findViewById(R.id.et_user_phone);
        mInputPwd = view.findViewById(R.id.et_user_verify);
        mLogin = view.findViewById(R.id.tv_login);
        mSendVerify = view.findViewById(R.id.tv_send_verify);
        mSendVerify.setEnabled(false);
        mVerifyCountDown = view.findViewById(R.id.tv_send_verify_count_down);
        mTvSkip = view.findViewById(R.id.tv_skip);
        return view;
    }

    @Override
    protected void setViewListener() {
        // 登陆
        mLogin.setOnClickListener(v -> {
            String name = mInputName.getText().toString().trim();
            String pwd = mInputPwd.getText().toString().trim();
            bindMobile(name, pwd);
        });
        // 跳过
        mTvSkip.setOnClickListener(v -> login());
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
                if (!isNameEmpty && !isPwdEmpty) {
                    mLogin.setEnabled(true);
                } else {
                    mLogin.setEnabled(false);
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
                if (!isNameEmpty && !isPwdEmpty) {
                    mLogin.setEnabled(true);
                } else {
                    mLogin.setEnabled(false);
                }
                if (!mCountDown) {
                    mSendVerify.setEnabled(!isNameEmpty);
                }
            }
        });
        mInputPwd.addTextChangedListener(mInputWatcher);
        // 发送验证码
        mSendVerify.setOnClickListener(v -> {
            verifyCountDown();
            String phone = mInputName.getEditableText().toString().trim();
            AppManager.getInstance().captchaSMS(phone, new HttpResponseCallback<Boolean>() {
                @Override
                public void onResponse(Boolean result) {
                    if (result != null && result) {
                        ToastUtil.showTextViewPrompt("获取验证码成功");
                    } else {
                        ToastUtil.showTextViewPrompt("获取验证码失败");
                        mSendVerify.setEnabled(true);
                        mSendVerify.setVisibility(View.VISIBLE);
                        mVerifyCountDown.setText("");
                        timer.cancel();
                    }                    // mSendVerify.setEnabled(true);
                    // mVerifyCountDown.setText("");
                    // timer.cancel();
                }

                @Override
                public void onFailure(int errorCode, String errorMsg, Throwable t) {
                    ToastUtil.showTextViewPrompt("获取验证码失败");
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
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            userName = intent.getStringExtra(LOGIN_NAME);
            userPwd = intent.getStringExtra(LOGIN_PWD);
            mAppId = intent.getStringExtra(LOGIN_APP_ID);
        }
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
    }

    private void bindMobile(String mobile, String captcha) {
        showLoadingDialog(true);
        AppManager.getInstance().getTokenByName(userName, userPwd,
                new HttpResponseCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        AppManager.getInstance().mobileBind(result, mobile, captcha,
                                new HttpResponseCallback<Boolean>() {
                                    @Override
                                    public void onResponse(Boolean result) {
                                        dismissLoadingDialog();
                                        if (result != null && result) {
                                            login();
                                        }
                                    }

                                    @Override
                                    public void onFailure(int errorCode, String errorMsg,
                                            Throwable t) {
                                        dismissLoadingDialog();
                                        ToastUtil.showTextViewPrompt(errorMsg);
                                    }
                                });
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        dismissLoadingDialog();
                        ToastUtil.showTextViewPrompt("绑定失败");
                    }
                });
    }

    private void login() {
        LoginActivity.login(this, userName, userPwd, false, mAppId);
    }
}
