
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
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.ClickTimeUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.sdk.utils.MessageDispatcher;

/**
 * Description : 登陆 Created by Mango on 2018/11/21.
 */
public class LoginActivity extends BaseTitleActivity {

    /* 账号 */
    private EditText mInputName;

    /* 密码 */
    private EditText mInputPwd;

    /* 登陆 */
    private TextView mLogin;

    /* 注册 */
    private TextView mRegister;

    /* 切换登陆模式 */
    private TextView mSwitchLoginMode;

    /* 是否是id登陆 */
    private boolean mLoginByUserId = false;

    /* 输入监听 */
    private TextWatcher mInputWatcher;

    private final int REGISTER_CODE = 1000;

    public static void openLogin(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
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
        mSwitchLoginMode = view.findViewById(R.id.tv_switch_login_mode);
        mSwitchLoginMode.setVisibility(View.GONE);
        // 三次点击 进入另一套环境配置
        ClickTimeUtils.setClickTimes(view.findViewById(R.id.tv_login_tag), 3,
                new ClickTimeUtils.IClick() {
                    @Override
                    public void onClick() {
                        ToastUtil.showTextViewPrompt("切换新的环境配置");
                        SharePreferenceUtils.getInstance().putCustomDns(true);
                        BaseManager.initTestBMXSDK();
                    }
                });
        return view;
    }

    @Override
    protected void setViewListener() {

        // 注册
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterActivity.openRegister(LoginActivity.this, REGISTER_CODE);
            }
        });
        // 登陆
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mInputName.getText().toString().trim();
                String pwd = mInputPwd.getText().toString().trim();
                // MainActivity.openMain(LoginActivity.this);

                login(LoginActivity.this, name, pwd, mLoginByUserId);
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
        mInputName.addTextChangedListener(mInputWatcher);
        mInputPwd.addTextChangedListener(mInputWatcher);
        // 切换登陆模式
        mSwitchLoginMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLoginByUserId) {
                    // id登陆 切换为用户名
                    mInputName.setHint(R.string.login_user_name_hint);
                    mInputName.setInputType(EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    mInputName.setHint(R.string.login_user_id_hint);
                    mInputName.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                }
                mLoginByUserId = !mLoginByUserId;
            }
        });
    }

    public static void login(final Activity activity, String name, final String pwd,
            final boolean isLoginById) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)) {
            ToastUtil.showTextViewPrompt("不能为空");
            return;
        }
        ((BaseTitleActivity)activity).showLoadingDialog(true);
        Observable.just(name).map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
//                if (isLoginById) {
//                    return UserManager.getInstance().signInById(Long.valueOf(s), pwd);
//                }
//                if (Pattern.matches("0?(13|14|15|17|18|19)[0-9]{9}", s)) {
//                    // 手机号
//                    return UserManager.getInstance().signInByPhone(s, pwd);
//                }
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
                        ((BaseTitleActivity)activity).dismissLoadingDialog();
                        String error = e != null ? e.getMessage() : "网络异常";
                        ToastUtil.showTextViewPrompt(error);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        ((BaseTitleActivity)activity).dismissLoadingDialog();
                        SharePreferenceUtils.getInstance().putLoginStatus(true);
                        MessageDispatcher.getDispatcher().initialize();
                        MainActivity.openMain(activity);
                    }
                });
    }
}
