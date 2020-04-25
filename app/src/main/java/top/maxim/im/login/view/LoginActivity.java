
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

import org.json.JSONException;
import org.json.JSONObject;

import im.floo.BMXCallBack;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import top.maxim.im.MainActivity;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.bean.UserBean;
import top.maxim.im.common.utils.ClickTimeUtils;
import top.maxim.im.common.utils.CommonConfig;
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.TaskDispatcher;
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

    private View mWXContainer;

    /* 微信登录 */
    private ImageView mWXLogin;

    /* 扫一扫 */
    private ImageView mIvScan;

    private ImageView mIvChangeAppId;

    /* 是否是id登陆 */
    private boolean mLoginByUserId = false;

    /* 输入监听 */
    private TextWatcher mInputWatcher;

    private TextView mTvAppId;

    private String mChangeAppId;

    private CompositeSubscription mSubscription;

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
        mVerifyLogin = view.findViewById(R.id.tv_verify);
        mWXContainer = view.findViewById(R.id.ll_wx_container);
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
        initRxBus();
        return view;
    }

    @Override
    protected void setViewListener() {
        // 注册
        mRegister.setOnClickListener(v -> RegisterActivity.openRegister(LoginActivity.this));
        // 验证码登录
        mVerifyLogin.setOnClickListener(v -> LoginByVerifyActivity.openLogin(LoginActivity.this));
        // 登陆
        mLogin.setOnClickListener(v -> {
            String name = mInputName.getText().toString().trim();
            String pwd = mInputPwd.getText().toString().trim();
            login(this, name, pwd, mLoginByUserId, mChangeAppId);
        });
        // 微信登录
        mWXLogin.setOnClickListener(v -> {
            if (!WXUtils.getInstance().wxSupported()) {
                ToastUtil.showTextViewPrompt("请安装微信");
                return;
            }
            initWXRxBus();
            WXUtils.getInstance().wxLogin(CommonConfig.SourceToWX.TYPE_LOGIN, mChangeAppId);
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
                        changeAppId(LoginActivity.this, content);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                }));
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        // 判断当前是否是扫码登陆
        String scanUserName = ScanConfigs.CODE_USER_NAME;
        if (!TextUtils.isEmpty(scanUserName)) {
            mInputName.setText(scanUserName);
            ScanConfigs.CODE_USER_NAME = "";
        }
        String appId = SharePreferenceUtils.getInstance().getAppId();
        mTvAppId.setText("APPID:" + appId);
//        if (!TextUtils.equals(appId, ScanConfigs.CODE_APP_ID)) {
//            mChangeAppId = appId;
//            mWXContainer.setVisibility(View.GONE);
//        } else {
//            mWXContainer.setVisibility(View.VISIBLE);
//        }
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

        BMXCallBack callBack = (bmxErrorCode) -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                // 登陆成功后 需要将userId存储SP 作为下次自动登陆
                UserManager.getInstance().getProfile(true, (bmxErrorCode1, profile) -> {
                    TaskDispatcher.exec(() -> {
                        if (BaseManager.bmxFinish(bmxErrorCode1) && profile != null
                                && profile.userId() > 0) {
                            CommonUtils.getInstance()
                                    .addUser(new UserBean(profile.username(), profile.userId(), pwd,
                                            changeAppId, System.currentTimeMillis()));
                            // 登陆成功消息预加载
                            WelcomeActivity.initData();
                        }
                        TaskDispatcher.postMain(() -> {
                            if (activity instanceof BaseTitleActivity
                                    && !activity.isFinishing()) {
                                ((BaseTitleActivity)activity).dismissLoadingDialog();
                            }
                            SharePreferenceUtils.getInstance().putLoginStatus(true);
                            MessageDispatcher.getDispatcher().initialize();
                            MainActivity.openMain(activity);
                            activity.finish();
                        });
                    });
                });
                return;
            }
            // 失败
            if (activity instanceof BaseTitleActivity && !activity.isFinishing()) {
                ((BaseTitleActivity)activity).dismissLoadingDialog();
            }
            String error = bmxErrorCode != null ? bmxErrorCode.name() : "网络异常";
            ToastUtil.showTextViewPrompt(error);
        };
        
        boolean isChange = !TextUtils.isEmpty(changeAppId)
                && !TextUtils.equals(changeAppId, SharePreferenceUtils.getInstance().getAppId());
        if (isChange) {
            UserManager.getInstance().changeAppId(changeAppId, bmxErrorCode -> {
                if (BaseManager.bmxFinish(bmxErrorCode)) {
                    SharePreferenceUtils.getInstance().putAppId(changeAppId);
                    realLogin(isLoginById, name, pwd, callBack);
                } else {
                    String error = bmxErrorCode != null ? bmxErrorCode.name() : "切换appId失败";
                    ToastUtil.showTextViewPrompt(error);
                }
            });
        } else {
            realLogin(isLoginById, name, pwd, callBack);
        }
    }
    
    private static void realLogin(boolean isLoginById, String name, String pwd, BMXCallBack callBack) {
        if (isLoginById) {
            UserManager.getInstance().signInById(Long.valueOf(name), pwd, callBack);
        } else {
            // if (Pattern.matches("0?(13|14|15|17|18|19)[0-9]{9}", s)) {
            // // 手机号
            // return UserManager.getInstance().signInByPhone(s, pwd);
            // }
            UserManager.getInstance().signInByName(name, pwd, callBack);
        }
    }

    public static void wxChatLogin(final Activity activity, String openId) {
        if (TextUtils.isEmpty(openId)) {
            ToastUtil.showTextViewPrompt("不能为空");
            return;
        }
        if (activity instanceof BaseTitleActivity && !activity.isFinishing()) {
            ((BaseTitleActivity)activity).showLoadingDialog(true);
        }
        AppManager.getInstance().weChatLogin(openId, new HttpResponseCallback<String>() {
            @Override
            public void onResponse(String result) {
                if (activity instanceof BaseTitleActivity
                        && !activity.isFinishing()) {
                    ((BaseTitleActivity)activity).dismissLoadingDialog();
                }
                if (TextUtils.isEmpty(result)) {
                    ToastUtil.showTextViewPrompt("登陆失败");
                    return;
                }
                String appId = WXUtils.getInstance().getAppId();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (!jsonObject.has("user_id") || !jsonObject.has("password")) {
                        String openId = jsonObject.getString("openid");
                        // 没有userId 密码 需要跳转绑定微信页面
                        LoginBindUserActivity.openLoginBindUser(activity, openId, appId);
                        activity.finish();
                        return;
                    }
                    // 直接登录
                    String userId = jsonObject.getString("user_id");
                    String pwd = jsonObject.getString("password");
                    LoginActivity.login(activity, userId, pwd, true, appId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                if (activity instanceof BaseTitleActivity
                        && !activity.isFinishing()) {
                    ((BaseTitleActivity)activity).dismissLoadingDialog();
                }
                ToastUtil.showTextViewPrompt("登陆失败");
            }
        });
    }

    /**
     * 切换appId
     */
    public static void changeAppId(final Activity activity, String appId) {
        // 为空 置为默认的appId
        appId = TextUtils.isEmpty(appId) ? ScanConfigs.CODE_APP_ID : appId;
        String oldAppId = SharePreferenceUtils.getInstance().getAppId();
        if (TextUtils.equals(oldAppId, appId)) {
            // 相同不处理
            Intent intent = new Intent();
            intent.setAction(CommonConfig.CHANGE_APP_ID_ACTION);
            intent.putExtra(CommonConfig.CHANGE_APP_ID, appId);
            RxBus.getInstance().send(intent);
            return;
        }
        // TODO 在登陆时候再切换appId
        Intent intent = new Intent();
        intent.setAction(CommonConfig.CHANGE_APP_ID_ACTION);
        intent.putExtra(CommonConfig.CHANGE_APP_ID, appId);
        RxBus.getInstance().send(intent);
//        if (activity instanceof BaseTitleActivity && !activity.isFinishing()) {
//            ((BaseTitleActivity)activity).showLoadingDialog(true);
//        }
//        String finalAppId = appId;
//        UserManager.getInstance().changeAppId(appId, bmxErrorCode -> {
//            if (activity instanceof BaseTitleActivity && !activity.isFinishing()) {
//                ((BaseTitleActivity)activity).dismissLoadingDialog();
//            }
//            if (BaseManager.bmxFinish(bmxErrorCode)) {
//                SharePreferenceUtils.getInstance().putAppId(finalAppId);
//                ToastUtil.showTextViewPrompt("切换appId成功");
//                Intent intent = new Intent();
//                intent.setAction(CommonConfig.CHANGE_APP_ID_ACTION);
//                intent.putExtra(CommonConfig.CHANGE_APP_ID, finalAppId);
//                RxBus.getInstance().send(intent);
//            } else {
//                String error = bmxErrorCode != null ? bmxErrorCode.name() : "切换appId失败";
//                ToastUtil.showTextViewPrompt(error);
//            }
//        });
    }

    private void initWXRxBus() {
        if (mSubscription == null) {
            mSubscription = new CompositeSubscription();
        }
        Subscription wxLogin = RxBus.getInstance().toObservable(Intent.class)
                .subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Intent>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Intent intent) {
                        if (intent == null || !TextUtils.equals(intent.getAction(),
                                CommonConfig.WX_LOGIN_ACTION)) {
                            return;
                        }
                        if (mSubscription != null) {
                            mSubscription.unsubscribe();
                        }
                        String openId = intent.getStringExtra(CommonConfig.WX_OPEN_ID);
                        wxChatLogin(LoginActivity.this, openId);
                    }
                });
        mSubscription.add(wxLogin);
    }

    private void initRxBus() {
        if (mSubscription == null) {
            mSubscription = new CompositeSubscription();
        }
        // 切换appId
        Subscription changeAppId = RxBus.getInstance().toObservable(Intent.class)
                .subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Intent>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Intent intent) {
                        if (intent == null || !TextUtils.equals(intent.getAction(),
                                CommonConfig.CHANGE_APP_ID_ACTION)) {
                            return;
                        }
                        mChangeAppId = intent.getStringExtra(CommonConfig.CHANGE_APP_ID);
                        mTvAppId.setText("APPID:" + mChangeAppId);
//                        mWXContainer.setVisibility(
//                                TextUtils.equals(mChangeAppId, ScanConfigs.CODE_APP_ID)
//                                        ? View.VISIBLE
//                                        : View.GONE);
                    }
                });
        mSubscription.add(changeAppId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }
}
