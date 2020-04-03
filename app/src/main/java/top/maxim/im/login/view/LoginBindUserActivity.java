
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.net.HttpResponseCallback;

/**
 * Description : 微信或手机号登陆  无账号时候绑定 Created by Mango on 2018/11/21.
 */
public class LoginBindUserActivity extends BaseTitleActivity {

    public static String LOGIN_OPEN_ID = "loginOpenId";

    public static String LOGIN_MOBILE = "loginMobile";

    public static String LOGIN_CAPTCHA = "loginCaptcha";

    public static String LOGIN_APP_ID = "loginAppId";

    /* 微信返回code */
    private String mOpenId;

    /* 手机号 */
    private String mMobile;

    /* 验证码 */
    private String mCaptcha;

    /* appId */
    private String mAppId;

    /* 账号 */
    private EditText mInputName;

    /* 密码 */
    private EditText mInputPwd;

    /* 登陆 */
    private TextView mLogin;

    private TextView mTvBindTag;

    private TextView mTvBind;

    private TextView mTvCheckName;

    /* 输入监听 */
    private TextWatcher mInputWatcher;

    // 默认注册
    private boolean mIsRegister = true;

    public static void openLoginBindUser(Context context, String openId, String appId) {
        Intent intent = new Intent(context, LoginBindUserActivity.class);
        intent.putExtra(LOGIN_OPEN_ID, openId);
        intent.putExtra(LOGIN_APP_ID, appId);
        context.startActivity(intent);
    }

    public static void openLoginBindUser(Context context, String mobile, String captcha, String appId) {
        Intent intent = new Intent(context, LoginBindUserActivity.class);
        intent.putExtra(LOGIN_MOBILE, mobile);
        intent.putExtra(LOGIN_CAPTCHA, captcha);
        intent.putExtra(LOGIN_APP_ID, appId);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setBackIcon(R.drawable.header_back_icon, v -> finish());
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.activity_login_bind_user, null);
        mInputName = view.findViewById(R.id.et_user_phone);
        mInputPwd = view.findViewById(R.id.et_user_verify);
        mLogin = view.findViewById(R.id.tv_login);
        mTvCheckName = view.findViewById(R.id.tv_check_user);
        mTvBindTag = view.findViewById(R.id.tv_bind_tag);
        mTvBind = view.findViewById(R.id.tv_verify);
        initUI(true);
        return view;
    }

    private void initUI(boolean isRegister) {
        mIsRegister = isRegister;
        mTvCheckName.setVisibility(View.GONE);
        mTvBindTag.setText(mIsRegister ? R.string.bind_register_user : R.string.bind_user);
        mTvBind.setText(mIsRegister ? R.string.bind_exit_user : R.string.bind_not_user);
    }

    @Override
    protected void setViewListener() {
        // 切换注册或绑定
        mTvBind.setOnClickListener(v -> initUI(!mIsRegister));
        // 登陆
        mLogin.setOnClickListener(v -> {
            String name = mInputName.getText().toString().trim();
            String pwd = mInputPwd.getText().toString().trim();
            checkName(name, pwd);
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
        mInputName.addTextChangedListener(mInputWatcher);
        mInputPwd.addTextChangedListener(mInputWatcher);
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mOpenId = intent.getStringExtra(LOGIN_OPEN_ID);
            mMobile = intent.getStringExtra(LOGIN_MOBILE);
            mCaptcha = intent.getStringExtra(LOGIN_CAPTCHA);
            mAppId = intent.getStringExtra(LOGIN_APP_ID);
        }
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
    }

    /**
     * 校验用户名
     */
    private void checkName(String userName, String pwd) {
        showLoadingDialog(true);
        if (mIsRegister) {
            register(userName, pwd);
        } else {
            bind(userName, pwd);
        }
        // AppManager.getInstance().checkName(userName, new
        // HttpResponseCallback<Boolean>() {
        // @Override
        // public void onResponse(Boolean result) {
        // if (result == null || !result) {
        // dismissLoadingDialog();
        // // 不可用
        // mTvCheckName.setVisibility(View.VISIBLE);
        // return;
        // }
        // mTvCheckName.setVisibility(View.GONE);
        // // 校验成功 需要先注册
        // register(userName, pwd);
        // }
        //
        // @Override
        // public void onFailure(int errorCode, String errorMsg, Throwable t) {
        // dismissLoadingDialog();
        // ToastUtil.showTextViewPrompt(errorMsg);
        // }
        // });
    }

    /**
     * 注册
     * 
     * @param userName
     * @param pwd
     */
    private void register(String userName, String pwd) {
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(pwd)) {
            ToastUtil.showTextViewPrompt("不能为空");
            return;
        }
        UserManager.getInstance().signUpNewUser(userName, pwd, (bmxErrorCode, bmxUserProfile) -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                // 注册成功 需要绑定微信
                bind(userName, pwd);
            } else {
                dismissLoadingDialog();
                // 不可用
                mTvCheckName.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * 绑定
     * 
     * @param name
     * @param pwd
     */
    private void bind(String name, String pwd) {
        AppManager.getInstance().getTokenByName(name, pwd, new HttpResponseCallback<String>() {
            @Override
            public void onResponse(String result) {
                if (!TextUtils.isEmpty(mOpenId)) {
                    // 绑定微信
                    bindWeChat(result, name, pwd);
                    return;
                }
                if (!TextUtils.isEmpty(mMobile) && !TextUtils.isEmpty(mCaptcha)) {
                    // 绑定手机
                    bindMobile(result, name, pwd);
                    return;
                }
                dismissLoadingDialog();
                ToastUtil.showTextViewPrompt("绑定失败");
            }

            @Override
            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                dismissLoadingDialog();
                ToastUtil.showTextViewPrompt("绑定失败");
            }
        });
    }

    /**
     * 绑定微信
     */
    public void bindWeChat(String token, String name, String pwd) {
        // 绑定和登陆没有依赖
        AppManager.getInstance().bindOpenId(token, mOpenId, new HttpResponseCallback<Boolean>() {
            @Override
            public void onResponse(Boolean result) {
            }

            @Override
            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                ToastUtil.showTextViewPrompt(errorMsg);
            }
        });
        // 微信绑定完也直接登录
        LoginActivity.login(LoginBindUserActivity.this, name, pwd, false, mAppId);
        // BindMobileActivity.openBindMobile(BindUserActivity.this, name, pwd, mAppId);
        // finish();
    }

    /**
     * 绑定手机号
     */
    public void bindMobile(String token, String name, String pwd) {
        // 绑定和登陆没有依赖
        AppManager.getInstance().mobileBind(token, mMobile, mCaptcha,
                new HttpResponseCallback<Boolean>() {
                    @Override
                    public void onResponse(Boolean result) {
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        ToastUtil.showTextViewPrompt(errorMsg);
                    }
                });
        LoginActivity.login(LoginBindUserActivity.this, name, pwd, false, mAppId);
    }

}
