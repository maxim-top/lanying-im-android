
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
 * Description : 验证页面 Created by Mango on 2018/11/06
 */
public class VerifyActivity extends BaseTitleActivity {

    private TextView mVerifyTitle;

    private View mVerifyPhone;

    private View mVerifyPwd;

    private TextView mTvPhone;

    private EditText mEtPwd;

    private View mGetVerify;

    private EditText mEtVerify;

    /* 发送验证码 */
    private TextView mSendVerify;

    /* 验证码倒计时 */
    private TextView mVerifyCountDown;

    private TextView mTvContinue;

    private String mPhone;

    private int mVerifyType = CommonConfig.VerifyType.TYPE_WX;
    
    private int mVerifyOperateType = CommonConfig.VerifyOperateType.TYPE_BIND_MOBILE;

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

    public static void startVerifyActivity(Context context, int type, String phone, int operateType) {
        Intent intent = new Intent(context, VerifyActivity.class);
        intent.putExtra(CommonConfig.VERIFY_TYPE, type);
        intent.putExtra(CommonConfig.PHONE, phone);
        intent.putExtra(CommonConfig.VERIFY_OPERATE_TYPE, operateType);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle("");
        builder.setBackIcon(R.drawable.header_back_icon, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.activity_verify, null);
        mVerifyTitle = view.findViewById(R.id.tv_verify_title);
        mVerifyPhone = view.findViewById(R.id.ll_et_verify_phone);
        mVerifyPwd = view.findViewById(R.id.ll_et_verify_pwd);
        mTvPhone = view.findViewById(R.id.et_verify_phone);
        mEtPwd = view.findViewById(R.id.et_verify_pwd);

        mGetVerify = view.findViewById(R.id.ll_et_user_verify);
        mEtVerify = view.findViewById(R.id.et_user_verify);
        mSendVerify = view.findViewById(R.id.tv_send_verify);
        mSendVerify.setEnabled(true);
        mVerifyCountDown = view.findViewById(R.id.tv_send_verify_count_down);

        mTvContinue = view.findViewById(R.id.tv_continue);
        mEtPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean isPwdEmpty = TextUtils.isEmpty(mEtPwd.getText().toString().trim());
                mTvContinue.setEnabled(!isPwdEmpty);
            }
        });
        mEtVerify.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean isPwdEmpty = TextUtils.isEmpty(mEtVerify.getText().toString().trim());
                mTvContinue.setEnabled(!isPwdEmpty);
            }
        });
        // 发送验证码
        mSendVerify.setOnClickListener(v -> verifyCountDown());
        return view;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mVerifyType = intent.getIntExtra(CommonConfig.VERIFY_TYPE,
                    CommonConfig.VerifyType.TYPE_WX);
            mVerifyOperateType = intent.getIntExtra(CommonConfig.VERIFY_OPERATE_TYPE,
                    CommonConfig.VerifyOperateType.TYPE_BIND_MOBILE);
            mPhone = intent.getStringExtra(CommonConfig.PHONE);
        }
    }

    @Override
    protected void setViewListener() {
        mTvContinue.setOnClickListener(v -> {
            checkPwd(mEtPwd.getText().toString());
        });
    }

    @Override
    protected void initDataForActivity() {
        String title = "", tag = "", continueTitle = "";
        switch (mVerifyType) {
            case CommonConfig.VerifyType.TYPE_WX:
                // 微信解绑验证密码
                mVerifyPhone.setVisibility(View.GONE);
                mVerifyPwd.setVisibility(View.VISIBLE);
                mGetVerify.setVisibility(View.GONE);
                title = getString(R.string.un_bind_wechat);
                tag = getString(R.string.un_bind_wechat_tag);
                continueTitle = getString(R.string.unbind_wechat_continue);
                break;
            case CommonConfig.VerifyType.TYPE_PWD:
                // 更换手机号验证密码
                mVerifyPhone.setVisibility(View.GONE);
                mVerifyPwd.setVisibility(View.VISIBLE);
                mGetVerify.setVisibility(View.GONE);
                title = getString(R.string.verify_pwd_title);
                tag = getString(R.string.verify_pwd_tag);
                continueTitle = getString(R.string.verify_continue);
                break;
            case CommonConfig.VerifyType.TYPE_PHONE_CAPTCHA:
                // 更换手机号验证旧手机验证码
                mVerifyPhone.setVisibility(View.VISIBLE);
                mGetVerify.setVisibility(View.VISIBLE);
                mVerifyPwd.setVisibility(View.GONE);
                title = getString(R.string.verify_captcha_title);
                tag = getString(R.string.verify_captcha_tag);
                mTvPhone.setText(mPhone);
                continueTitle = getString(R.string.verify_continue);
                break;
            default:
                break;
        }
        mHeader.setTitle(title);
        mVerifyTitle.setText(tag);
        mTvContinue.setText(continueTitle);
    }

    /**
     * 校验密码
     * 
     * @param input
     */
    private void checkPwd(String input) {
        String name = SharePreferenceUtils.getInstance().getUserName();
        String pwd = SharePreferenceUtils.getInstance().getUserPwd();
        AppManager.getInstance().getTokenByName(name, pwd, new HttpResponseCallback<String>() {
            @Override
            public void onResponse(String result) {
                HttpResponseCallback<String> callback = new HttpResponseCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        switch (mVerifyType) {
                            case CommonConfig.VerifyType.TYPE_WX:
                                // 解绑微信
                                unBindWX();
                                break;
                            case CommonConfig.VerifyType.TYPE_PWD:
                            case CommonConfig.VerifyType.TYPE_PHONE_CAPTCHA:
                                if (mVerifyOperateType == CommonConfig.VerifyOperateType.TYPE_BIND_MOBILE) {
                                    // 更换手机号
                                    BindMobileActivity.openChangeMobile(VerifyActivity.this,
                                            result);
                                } else if (mVerifyOperateType == CommonConfig.VerifyOperateType.TYPE_CHANGE_PWD) {
                                    // 修改密码
                                    ChangePwdActivity.startChangePwdActivity(VerifyActivity.this, result);
                                }
                                finish();
                                break;
                            default:
                                break;
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        ToastUtil.showTextViewPrompt(errorMsg);
                    }
                };
                if (mVerifyType == CommonConfig.VerifyType.TYPE_PHONE_CAPTCHA) {
                    // 验证码
                    String captcha = mEtVerify.getText().toString().trim();
                    AppManager.getInstance().mobilePrechangeByMobile(result, mPhone, captcha,
                            callback);
                } else {
                    // 密码验证
                    if (!TextUtils.isEmpty(input)) {
                        AppManager.getInstance().mobilePrechangeByPwd(result, input, callback);
                    }
                }
            }

            @Override
            public void onFailure(int errorCode, String errorMsg, Throwable t) {
            }
        });
    }

    /**
     * 解绑微信
     */
    private void unBindWX() {
        showLoadingDialog(true);
        String name = SharePreferenceUtils.getInstance().getUserName();
        String pwd = SharePreferenceUtils.getInstance().getUserPwd();
        AppManager.getInstance().getTokenByName(name, pwd, new HttpResponseCallback<String>() {
            @Override
            public void onResponse(String result) {
                AppManager.getInstance().unBindOpenId(result, new HttpResponseCallback<Boolean>() {
                    @Override
                    public void onResponse(Boolean result) {
                        dismissLoadingDialog();
                        if (result != null && result) {
                            ToastUtil.showTextViewPrompt("解除成功");
                            Intent intent = new Intent();
                            intent.setAction(CommonConfig.WX_UN_BIND_ACTION);
                            RxBus.getInstance().send(intent);
                            finish();
                        } else {
                            ToastUtil.showTextViewPrompt("解除失败");
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        dismissLoadingDialog();
                        ToastUtil.showTextViewPrompt(errorMsg);
                    }
                });

            }

            @Override
            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                dismissLoadingDialog();
                ToastUtil.showTextViewPrompt("解除失败");
            }
        });
    }

    /**
     * 验证码倒计时60s
     */
    public void verifyCountDown() {
        mCountDown = true;
        mSendVerify.setVisibility(View.GONE);
        mSendVerify.setEnabled(false);
        timer.start();
        AppManager.getInstance().captchaSMS(mPhone, new HttpResponseCallback<Boolean>() {
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
                } // mSendVerify.setEnabled(true);
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
    }
}
