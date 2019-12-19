
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
import top.maxim.im.common.utils.CommonConfig;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.net.HttpResponseCallback;

/**
 * Description : 注册后绑定手机 Created by Mango on 2018/11/21.
 */
public class BindMobileActivity extends BaseTitleActivity {

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

    /* 输入监听 */
    private TextWatcher mInputWatcher;

    private boolean mIsChange;

    private String mSign;

    private boolean mCountDown;

    public static final String CHANGE_PHONE = "changePhone";

    public static final String CHANGE_PHONE_SIGN = "changePhoneSign";

    private CountDownTimer timer = new CountDownTimer(60 * 1000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
            mCountDown = true;
            mVerifyCountDown.setText(millisUntilFinished / 1000 + "s");
        }

        @Override
        public void onFinish() {
            mCountDown = false;
            mVerifyCountDown.setText("");
        }
    };

    public static void openBindMobile(Context context) {
        Intent intent = new Intent(context, BindMobileActivity.class);
        intent.putExtra(CHANGE_PHONE, false);
        context.startActivity(intent);
    }

    public static void openChangeMobile(Context context, String sign) {
        Intent intent = new Intent(context, BindMobileActivity.class);
        intent.putExtra(CHANGE_PHONE, true);
        intent.putExtra(CHANGE_PHONE_SIGN, sign);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setBackIcon(R.drawable.header_back_icon, v -> finish());
        builder.setTitle(R.string.bind_mobile);
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.activity_bind_mobile, null);
        mInputName = view.findViewById(R.id.et_user_phone);
        mInputPwd = view.findViewById(R.id.et_user_verify);
        mLogin = view.findViewById(R.id.tv_login);
        mSendVerify = view.findViewById(R.id.tv_send_verify);
        mSendVerify.setEnabled(false);
        mVerifyCountDown = view.findViewById(R.id.tv_send_verify_count_down);
        mHeader.setTitle(mIsChange ? R.string.bind_new_mobile : R.string.bind_mobile);
        return view;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mIsChange = intent.getBooleanExtra(CHANGE_PHONE, false);
            mSign = intent.getStringExtra(CHANGE_PHONE_SIGN);
        }
    }

    @Override
    protected void setViewListener() {
        // 登陆
        mLogin.setOnClickListener(v -> {
            String name = mInputName.getText().toString().trim();
            String pwd = mInputPwd.getText().toString().trim();
            if (mIsChange) {
                changeMobile(name, pwd);
            } else {
                bindMobile(name, pwd);
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
                        mVerifyCountDown.setText("");
                        timer.cancel();
                    } // mSendVerify.setEnabled(true);
                    // mVerifyCountDown.setText("");
                    // timer.cancel();
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
    }

    /**
     * 验证码倒计时60s
     */
    public void verifyCountDown() {
        mCountDown = true;
        mSendVerify.setEnabled(false);
        timer.start();
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
    }

    private void bindMobile(String mobile, String captcha) {
        showLoadingDialog(true);
        String userName = SharePreferenceUtils.getInstance().getUserName();
        String userPwd = SharePreferenceUtils.getInstance().getUserPwd();
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
                                            Intent intent = new Intent();
                                            intent.setAction(CommonConfig.MOBILE_BIND_ACTION);
                                            RxBus.getInstance().send(intent);
                                            finish();
                                        } else {
                                            ToastUtil.showTextViewPrompt("绑定失败");
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

    private void changeMobile(String mobile, String captcha) {
        showLoadingDialog(true);
        String userName = SharePreferenceUtils.getInstance().getUserName();
        String userPwd = SharePreferenceUtils.getInstance().getUserPwd();
        AppManager.getInstance().getTokenByName(userName, userPwd,
                new HttpResponseCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        AppManager.getInstance().mobileChange(result, mobile, captcha, mSign,
                                new HttpResponseCallback<Boolean>() {
                                    @Override
                                    public void onResponse(Boolean result) {
                                        dismissLoadingDialog();
                                        if (result != null && result) {
                                            Intent intent = new Intent();
                                            intent.setAction(CommonConfig.MOBILE_BIND_ACTION);
                                            RxBus.getInstance().send(intent);
                                            finish();
                                        } else {
                                            ToastUtil.showTextViewPrompt("更换失败");
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

}
