
package top.maxim.im.login.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;

/**
 * Description : 登陆 Created by Mango on 2018/11/21.
 */
public class RegisterActivity extends BaseTitleActivity {

    /* 关闭 */
    private ImageView mClose;

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

    /* 输入监听 */
    private TextWatcher mInputWatcher;

    public static final String REFISTER_ACCOUNT = "registerAccount";

    public static final String REFISTER_PWD = "registerPwd";

    public static void openRegister(Context context, int requestCode) {
        Intent intent = new Intent(context, RegisterActivity.class);
        ((Activity)context).startActivityForResult(intent, requestCode);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        return new Header.Builder(this, headerContainer).build();
    }

    @Override
    protected View onCreateView() {
        hideHeader();
        View view = View.inflate(this, R.layout.activity_register, null);
        mClose = view.findViewById(R.id.iv_register_close);
        mInputName = view.findViewById(R.id.et_user_name);
        mInputPwd = view.findViewById(R.id.et_user_pwd);
        mInputPhone = view.findViewById(R.id.et_user_phone);
        mSendVerify = view.findViewById(R.id.tv_send_verify);
        mInputVerify = view.findViewById(R.id.et_user_verify);
        mRegister = view.findViewById(R.id.tv_register);
        return view;
    }

    @Override
    protected void setViewListener() {
        // 关闭
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        //发送验证码
        mSendVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });
    }

    void register(final String account, final String pwd, final String phone, final String verify) {
        BaseManager.initTestBMXSDK(SharePreferenceUtils.getInstance().getCustomDns());

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
                        LoginActivity.login(RegisterActivity.this,
                                mInputName.getEditableText().toString().trim(),
                                mInputPwd.getEditableText().toString().trim(), false);
                    }
                });
    }
}
