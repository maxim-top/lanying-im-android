
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

    private TextView mTvContinue;

    private String mPhone;

    private int mVerifyType = CommonConfig.VerifyType.TYPE_WX;

    public static void startVerifyPwdActivity(Context context, int type, String phone) {
        Intent intent = new Intent(context, VerifyActivity.class);
        intent.putExtra(CommonConfig.VERIFY_TYPE, type);
        intent.putExtra(CommonConfig.PHONE, phone);
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
        return view;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mVerifyType = intent.getIntExtra(CommonConfig.VERIFY_TYPE,
                    CommonConfig.VerifyType.TYPE_WX);
            mPhone = intent.getStringExtra(CommonConfig.PHONE);
        }
    }

    @Override
    protected void setViewListener() {
        mTvContinue.setOnClickListener(v -> {
            String input = mEtPwd.getText().toString();
            switch (mVerifyType) {
                case CommonConfig.VerifyType.TYPE_WX:
                    // 解绑微信
                    checkPwd(input);
                    break;
            }
        });
    }

    @Override
    protected void initDataForActivity() {
        String title = "", tag = "", continueTitle = "";
        switch (mVerifyType) {
            case CommonConfig.VerifyType.TYPE_WX:
                mVerifyPhone.setVisibility(View.GONE);
                title = getString(R.string.un_bind_wechat);
                tag = getString(R.string.un_bind_wechat_tag);
                continueTitle = getString(R.string.unbind_wechat_continue);
                break;
            case CommonConfig.VerifyType.TYPE_PHONE:
                mVerifyPhone.setVisibility(View.GONE);
                title = getString(R.string.change_mobile);
                tag = getString(R.string.verify_pwd_tag);
                continueTitle = getString(R.string.verify_continue);
                break;
            case CommonConfig.VerifyType.TYPE_PHONE_VERIFY:
                mVerifyPhone.setVisibility(View.VISIBLE);
                title = getString(R.string.change_mobile);
                tag = getString(R.string.verify_pwd_tag);
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
        if (TextUtils.isEmpty(input)) {
            return;
        }
        String name = SharePreferenceUtils.getInstance().getUserName();
        String pwd = SharePreferenceUtils.getInstance().getUserPwd();
        AppManager.getInstance().getTokenByName(name, pwd, new HttpResponseCallback<String>() {
            @Override
            public void onResponse(String result) {
                AppManager.getInstance().mobilePrechangeByPwd(result, input,
                        new HttpResponseCallback<String>() {
                            @Override
                            public void onResponse(String result) {
                                unBindWX();
                            }

                            @Override
                            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                                ToastUtil.showTextViewPrompt(errorMsg);
                            }
                        });
            }

            @Override
            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                ToastUtil.showTextViewPrompt("密码不正确");
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
}
