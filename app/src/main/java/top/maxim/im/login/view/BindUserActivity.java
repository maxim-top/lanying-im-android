
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
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.net.HttpResponseCallback;

/**
 * Description : 登陆 Created by Mango on 2018/11/21.
 */
public class BindUserActivity extends BaseTitleActivity {

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

    public static void openBindUser(Context context, String openId, String appId) {
        Intent intent = new Intent(context, BindUserActivity.class);
        intent.putExtra(LOGIN_OPEN_ID, openId);
        intent.putExtra(LOGIN_APP_ID, appId);
        context.startActivity(intent);
    }

    public static void openBindUser(Context context, String mobile, String captcha, String appId) {
        Intent intent = new Intent(context, BindUserActivity.class);
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
        View view = View.inflate(this, R.layout.activity_bind_user, null);
        mInputName = view.findViewById(R.id.et_user_phone);
        mInputPwd = view.findViewById(R.id.et_user_verify);
        mLogin = view.findViewById(R.id.tv_login);
        mTvCheckName = view.findViewById(R.id.tv_check_user);
        mTvBindTag = view.findViewById(R.id.tv_bind_tag);
        mTvBind = view.findViewById(R.id.tv_verify);
        return view;
    }

    @Override
    protected void setViewListener() {
        // 切换注册或绑定
        mTvBind.setOnClickListener(v -> {
            mIsRegister = !mIsRegister;
            mTvCheckName.setVisibility(View.GONE);
            mTvBindTag.setText(mIsRegister ? R.string.bind_register_user : R.string.bind_user);
            mTvBind.setText(mIsRegister ? R.string.bind_not_user : R.string.bind_exit_user);
        });
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
        if (!mIsRegister) {
            // 如果不是注册 直接绑定
            bind(userName, pwd);
            return;
        }
        showLoadingDialog(true);
        AppManager.getInstance().checkName(userName, new HttpResponseCallback<Boolean>() {
            @Override
            public void onResponse(Boolean result) {
                if (result == null || !result) {
                    dismissLoadingDialog();
                    // 不可用
                    mTvCheckName.setVisibility(View.VISIBLE);
                    return;
                }
                mTvCheckName.setVisibility(View.GONE);
                // 校验成功 需要先注册
                register(userName, pwd);
            }

            @Override
            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                dismissLoadingDialog();
                ToastUtil.showTextViewPrompt(errorMsg);
            }
        });
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
        Observable.just(userName).map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                return UserManager.getInstance().signUpNewUser(userName, pwd, new BMXUserProfile());
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
                        // 注册成功 需要绑定微信
                        bind(userName, pwd);
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
        AppManager.getInstance().bindOpenId(token, mOpenId, new HttpResponseCallback<String>() {
            @Override
            public void onResponse(String result) {
                dismissLoadingDialog();
                BindMobileActivity.openBindMobile(BindUserActivity.this, name, pwd, mAppId);
                finish();
            }

            @Override
            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                dismissLoadingDialog();
                ToastUtil.showTextViewPrompt(errorMsg);
            }
        });
    }

    /**
     * 绑定手机号
     */
    public void bindMobile(String token, String name, String pwd) {
        AppManager.getInstance().mobileBind(token, mMobile, mCaptcha,
                new HttpResponseCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        dismissLoadingDialog();
                        LoginActivity.login(BindUserActivity.this, name, pwd, false, mAppId);
                        finish();
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        dismissLoadingDialog();
                        ToastUtil.showTextViewPrompt(errorMsg);
                    }
                });
    }

}
