
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
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.net.HttpResponseCallback;

/**
 * Description : 实名认证-姓名和身份证号
 */
public class UserVerificationIdNumberActivity extends BaseTitleActivity {

    /* 账号 */
    private EditText mName;

    /* 密码 */
    private EditText mIdNumber;

    /* 继续 */
    private TextView mGo;

    /* 输入监听 */
    private TextWatcher mInputWatcher;

    /* 跳过 */
    private TextView mTvSkip;

    public static void openUserVerification(Context context) {
        Intent intent = new Intent(context, UserVerificationIdNumberActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setBackIcon(R.drawable.header_back_icon, v -> finish());
        builder.setTitle(R.string.real_name_verification);
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.activity_real_name_verification_id_number, null);
        mName = view.findViewById(R.id.et_user_phone);
        mIdNumber = view.findViewById(R.id.et_user_verify);
        mGo = view.findViewById(R.id.tv_login);
        mTvSkip = view.findViewById(R.id.tv_skip);
        mTvSkip.setVisibility(CommonUtils.isOptionalVerification("id_card") ?
                View.VISIBLE : View.INVISIBLE);
        mHeader.setTitle(R.string.real_name_verification);
        return view;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
    }

    @Override
    protected void setViewListener() {
        mTvSkip.setOnClickListener(v -> {
            // 人脸识别 todo
            finish();
        });

        // 继续
        mGo.setOnClickListener(v -> {
            String name = mName.getText().toString().trim();
            String idNumber = mIdNumber.getText().toString().trim();
            verify(name, idNumber);
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
                boolean isNameEmpty = TextUtils.isEmpty(mName.getText().toString().trim());
                boolean isPwdEmpty = TextUtils.isEmpty(mIdNumber.getText().toString().trim());
                if (!isNameEmpty && !isPwdEmpty) {
                    mGo.setEnabled(true);
                } else {
                    mGo.setEnabled(false);
                }
            }
        };
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean isNameEmpty = TextUtils.isEmpty(mName.getText().toString().trim());
                boolean isPwdEmpty = TextUtils.isEmpty(mIdNumber.getText().toString().trim());
                if (!isNameEmpty && !isPwdEmpty) {
                    mGo.setEnabled(true);
                } else {
                    mGo.setEnabled(false);
                }
            }
        });
        mIdNumber.addTextChangedListener(mInputWatcher);
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        intent.setAction(CommonConfig.REAL_NAME_VERIFY_ACTION);
        RxBus.getInstance().send(intent);
    }


    private void verify(String name, String idNumber) {
        showLoadingDialog(true);
        String userName = SharePreferenceUtils.getInstance().getUserName();
        String userPwd = SharePreferenceUtils.getInstance().getUserPwd();
        AppManager.getInstance().getTokenByName(userName, userPwd,
                new HttpResponseCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        AppManager.getInstance().userVerifyByIdNumber(result, name, idNumber,
                                new HttpResponseCallback<Boolean>() {
                                    @Override
                                    public void onResponse(Boolean result) {
                                        dismissLoadingDialog();
                                        if (result != null && result) {
                                            Intent intent = new Intent();
                                            intent.setAction(CommonConfig.REAL_NAME_VERIFY_ACTION);
                                            RxBus.getInstance().send(intent);
                                            if (CommonUtils.needShowVerificationPage("face")) {
                                                //人脸识别
                                            }
                                            finish();
                                        } else {
                                            ToastUtil.showTextViewPrompt(getString(R.string.verified_failed));
                                        }
                                    }

                                    @Override
                                    public void onFailure(int errorCode, String errorMsg,
                                            Throwable t) {
                                        dismissLoadingDialog();
                                        ToastUtil.showTextViewPrompt(getString(R.string.verified_failed));
                                    }
                                });
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        dismissLoadingDialog();
                        ToastUtil.showTextViewPrompt(getString(R.string.verified_failed));
                    }
                });
    }
}
