
package top.maxim.im.login.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import top.maxim.im.MainActivity;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.ClickTimeUtils;
import top.maxim.im.common.utils.CommonConfig;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.net.HttpResponseCallback;
import top.maxim.im.scan.config.ScanConfigs;
import top.maxim.im.scan.view.ScannerActivity;
import top.maxim.im.sdk.utils.MessageDispatcher;
import top.maxim.im.wxapi.WXUtils;

/**
 * Description : 登陆 Created by Mango on 2018/11/21.
 */
public class LoginActivity extends BaseTitleActivity {

    public static String LOGIN_OPEN_ID = "loginOpenId";

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

    /* 微信登录 */
    private ImageView mWXLogin;

    /* 扫一扫 */
    private ImageView mIvScan;

    private ImageView mIvChangeAppId;

    /* 是否是id登陆 */
    private boolean mLoginByUserId = false;

    /* 输入监听 */
    private TextWatcher mInputWatcher;

    /* 微信返回code */
    private String mOpenId;

    private TextView mTvAppId;
    
    private String mChangeAppId;

    public static void openLogin(Context context) {
        openLogin(context, null);
    }

    public static void openLogin(Context context, String openId) {
        Intent intent = new Intent(context, LoginActivity.class);
        if (!TextUtils.isEmpty(openId)) {
            intent.putExtra(LOGIN_OPEN_ID, openId);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        return new Header.Builder(this, headerContainer).build();
    }

    @Override
    protected View onCreateView() {
        hideHeader();
        View view = View.inflate(this, R.layout.activity_login, null);
        mInputName = view.findViewById(R.id.et_user_name);
        mInputPwd = view.findViewById(R.id.et_user_pwd);
        mLogin = view.findViewById(R.id.tv_login);
        mRegister = view.findViewById(R.id.tv_register);
        mVerifyLogin = view.findViewById(R.id.tv_verify);
        mWXLogin = view.findViewById(R.id.iv_wx_login);
        mIvScan = view.findViewById(R.id.iv_scan);
        mIvChangeAppId = view.findViewById(R.id.iv_app_id);
        mTvAppId = view.findViewById(R.id.tv_login_appid);
        mSwitchLoginMode = view.findViewById(R.id.tv_switch_login_mode);
        mSwitchLoginMode.setVisibility(View.GONE);
        // 三次点击 进入另一套环境配置
        ClickTimeUtils.setClickTimes(view.findViewById(R.id.tv_login_tag), 3, () -> {
            int newIndex = SharePreferenceUtils.getInstance().getCustomDns();
            if (newIndex < 0 || newIndex > 4) {
                newIndex = 0;
            }
            BaseManager.initTestBMXSDK(newIndex);
            SharePreferenceUtils.getInstance().putCustomDns(newIndex);
            ToastUtil.showTextViewPrompt("切换新的环境配置:" + newIndex);
        });
        return view;
    }

    @Override
    protected void setViewListener() {
        // 注册
        mRegister.setOnClickListener(v -> RegisterActivity.openRegister(LoginActivity.this));
        //验证码登录
        mVerifyLogin.setOnClickListener(v -> LoginByVerifyActivity.openLogin(LoginActivity.this));
        // 登陆
        mLogin.setOnClickListener(v -> {
            String name = mInputName.getText().toString().trim();
            String pwd = mInputPwd.getText().toString().trim();
            if (!TextUtils.isEmpty(mOpenId)) {
                bindOpenId(this, name, pwd, mOpenId);
            } else {
                login(this, name, pwd, mLoginByUserId, mChangeAppId);
            }
        });
        // 微信登录
        mWXLogin.setOnClickListener(v -> {
            if (!WXUtils.getInstance().wxSupported()) {
                ToastUtil.showTextViewPrompt("请安装微信");
                return;
            }
            WXUtils.getInstance().wxLogin(CommonConfig.SourceToWX.TYPE_LOGIN);
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

    /**
     * 绑定openId
     * 
     * @param pwd
     */
    public static void bindOpenId(final Activity activity, String name, final String pwd,
            final String openId) {
        // 首先获取token
        if (activity instanceof BaseTitleActivity && !activity.isFinishing()) {
            ((BaseTitleActivity)activity).showLoadingDialog(true);
        }
        AppManager.getInstance().getTokenByName(name, pwd, new HttpResponseCallback<String>() {
            @Override
            public void onResponse(String result) {
                AppManager.getInstance().bindOpenId(result, openId,
                        new HttpResponseCallback<String>() {
                            @Override
                            public void onResponse(String result) {
                                if (activity instanceof BaseTitleActivity
                                        && !activity.isFinishing()) {
                                    ((BaseTitleActivity)activity).dismissLoadingDialog();
                                }
                                login(activity, name, pwd, false);
                            }

                            @Override
                            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                                if (activity instanceof BaseTitleActivity
                                        && !activity.isFinishing()) {
                                    ((BaseTitleActivity)activity).dismissLoadingDialog();
                                }
                            }
                        });
            }

            @Override
            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                ToastUtil.showTextViewPrompt("绑定失败");
                if (activity instanceof BaseTitleActivity && !activity.isFinishing()) {
                    ((BaseTitleActivity)activity).dismissLoadingDialog();
                }
            }
        });
    }

    public static void login(Activity activity, String name, String pwd, boolean isLoginById) {
        login(activity, name, pwd, isLoginById, null);
    }

    public static void login(final Activity activity, String name, final String pwd,
            final boolean isLoginById, final String changeAppId) {

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)) {
            ToastUtil.showTextViewPrompt("不能为空");
            return;
        }
        if (activity instanceof BaseTitleActivity && !activity.isFinishing()) {
            ((BaseTitleActivity)activity).showLoadingDialog(true);
        }
        Observable.just(name).map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                if (isLoginById) {
                    return UserManager.getInstance().signInById(Long.valueOf(s), pwd);
                }
                // if (Pattern.matches("0?(13|14|15|17|18|19)[0-9]{9}", s)) {
                // // 手机号
                // return UserManager.getInstance().signInByPhone(s, pwd);
                // }
                return UserManager.getInstance().signInByName(s, pwd);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).map(new Func1<BMXErrorCode, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXErrorCode bmxErrorCode) {
                // 登陆成功后 需要将userId存储SP 作为下次自动登陆
                BMXUserProfile profile = new BMXUserProfile();
                BMXErrorCode errorCode = UserManager.getInstance().getProfile(profile, true);
                if (errorCode != null && errorCode.swigValue() == BMXErrorCode.NoError.swigValue()
                        && profile.userId() > 0) {
                    SharePreferenceUtils.getInstance().putUserId(profile.userId());
                    SharePreferenceUtils.getInstance().putUserName(profile.username());
                    SharePreferenceUtils.getInstance().putUserPwd(pwd);
                    AppManager.getInstance().getTokenByName(profile.username(), pwd, null);
                    if (!TextUtils.isEmpty(changeAppId)) {
                        SharePreferenceUtils.getInstance().putAppId(changeAppId);
                        UserManager.getInstance().changeAppId(changeAppId);
                    }
                    // 登陆成功消息预加载
                    WelcomeActivity.initData();
                }
                return bmxErrorCode;
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (activity instanceof BaseTitleActivity && !activity.isFinishing()) {
                            ((BaseTitleActivity)activity).dismissLoadingDialog();
                        }
                        String error = e != null ? e.getMessage() : "网络异常";
                        ToastUtil.showTextViewPrompt(error);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        if (activity instanceof BaseTitleActivity && !activity.isFinishing()) {
                            ((BaseTitleActivity)activity).dismissLoadingDialog();
                        }
                        SharePreferenceUtils.getInstance().putLoginStatus(true);
                        MessageDispatcher.getDispatcher().initialize();
                        MainActivity.openMain(activity);
                    }
                });
    }
}
