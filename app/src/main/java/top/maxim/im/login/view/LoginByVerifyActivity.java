
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.CommonConfig;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.net.HttpResponseCallback;
import top.maxim.im.scan.config.ScanConfigs;
import top.maxim.im.scan.view.ScannerActivity;
import top.maxim.im.wxapi.WXUtils;

/**
 * Description : 登陆 Created by Mango on 2018/11/21.
 */
public class LoginByVerifyActivity extends BaseTitleActivity {

    public static String LOGIN_OPEN_ID = "loginOpenId";

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

    /* 微信登录 */
    private ImageView mWXLogin;

    /* 扫一扫 */
    private ImageView mIvScan;

    private ImageView mIvChangeAppId;

    /* 输入监听 */
    private TextWatcher mInputWatcher;

    /* 微信返回code */
    private String mOpenId;

    private TextView mTvAppId;

    private String mChangeAppId;

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

    public static void openLogin(Context context) {
        openLogin(context, null);
    }

    public static void openLogin(Context context, String openId) {
        Intent intent = new Intent(context, LoginByVerifyActivity.class);
        if (!TextUtils.isEmpty(openId)) {
            intent.putExtra(LOGIN_OPEN_ID, openId);
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
        View view = View.inflate(this, R.layout.activity_login_verify, null);
        mInputName = view.findViewById(R.id.et_user_phone);
        mInputPwd = view.findViewById(R.id.et_user_verify);
        mLogin = view.findViewById(R.id.tv_login);
        mVerifyLogin = view.findViewById(R.id.tv_verify);
        mSendVerify = view.findViewById(R.id.tv_send_verify);
        mVerifyCountDown = view.findViewById(R.id.tv_send_verify_count_down);
        mRegister = view.findViewById(R.id.tv_register);
        mWXLogin = view.findViewById(R.id.iv_wx_login);
        mIvScan = view.findViewById(R.id.iv_scan);
        mIvChangeAppId = view.findViewById(R.id.iv_app_id);
        mTvAppId = view.findViewById(R.id.tv_login_appid);
        return view;
    }

    @Override
    protected void setViewListener() {
        // 注册
        mRegister
                .setOnClickListener(v -> RegisterActivity.openRegister(LoginByVerifyActivity.this));
        // 密码登录
        mVerifyLogin.setOnClickListener(v -> {
            LoginActivity.openLogin(LoginByVerifyActivity.this, mOpenId);
            finish();
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
                ToastUtil.showTextViewPrompt("请安装微信");
                return;
            }
            WXUtils.getInstance().wxLogin(CommonConfig.SourceToWX.TYPE_LOGIN_VERIFY);
        });
        // 扫一扫
        mIvScan.setOnClickListener(v -> ScannerActivity.openScan(this));
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
        mInputName.addTextChangedListener(mInputWatcher);
        mInputPwd.addTextChangedListener(mInputWatcher);
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
        // 发送验证码
        mSendVerify.setOnClickListener(v -> {
            verifyCountDown();
            String phone = mInputName.getEditableText().toString().trim();
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
    }

    /**
     * 验证码倒计时60s
     */
    public void verifyCountDown() {
        mSendVerify.setEnabled(false);
        timer.start();
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mOpenId = intent.getStringExtra(LOGIN_OPEN_ID);
        }
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        // 判断当前是否是扫码登陆
        String scanUserName = ScanConfigs.CODE_USER_NAME;
        if (!TextUtils.isEmpty(scanUserName)) {
            mInputName.setText(scanUserName);
        }
        String appId = SharePreferenceUtils.getInstance().getAppId();
        mTvAppId.setText("APPID:" + appId);
    }

    private void loginByCaptcha(String mobile, String captcha) {
        showLoadingDialog(true);
        AppManager.getInstance().getInfoPwdByCaptcha(mobile, captcha,
                new HttpResponseCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        dismissLoadingDialog();
                        if (TextUtils.isEmpty(result)) {
                            ToastUtil.showTextViewPrompt("登录失败");
                            return;
                        }
                        String username = "", password = "", user_id = "";
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has("username")) {
                                username = jsonObject.getString("username");
                            }
                            if (jsonObject.has("password")) {
                                password = jsonObject.getString("password");
                            }
                            if (jsonObject.has("user_id")) {
                                user_id = jsonObject.getString("user_id");
                            }
                            if (!TextUtils.isEmpty(mOpenId)) {
                                LoginActivity.bindOpenId(LoginByVerifyActivity.this, username,
                                        password, mOpenId);
                            } else {
                                LoginActivity.login(LoginByVerifyActivity.this, username, password,
                                        false);
                            }
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
}