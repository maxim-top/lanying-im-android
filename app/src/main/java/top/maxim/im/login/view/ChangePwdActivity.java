
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
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.net.HttpResponseCallback;

/**
 * Description : 修改密码 Created by Mango on 2020/06/29
 */
public class ChangePwdActivity extends BaseTitleActivity {

    public static final String CHANGE_PWD_SIGN = "changePwdSign";

    private String mSign;

    private EditText mEtPwd;

    private EditText mEtAgainPwd;

    private TextView mTvContinue;

    public static void startChangePwdActivity(Context context, String sign) {
        Intent intent = new Intent(context, ChangePwdActivity.class);
        intent.putExtra(CHANGE_PWD_SIGN, sign);
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
        View view = View.inflate(this, R.layout.activity_change_pwd, null);
        mEtPwd = view.findViewById(R.id.et_new_pwd);
        mEtAgainPwd = view.findViewById(R.id.et_new_pwd_again);
        mTvContinue = view.findViewById(R.id.tv_continue);
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean isNameEmpty = TextUtils.isEmpty(mEtPwd.getText().toString().trim());
                boolean isPwdEmpty = TextUtils.isEmpty(mEtAgainPwd.getText().toString().trim());
                if (!isNameEmpty && !isPwdEmpty) {
                    mTvContinue.setEnabled(true);
                } else {
                    mTvContinue.setEnabled(false);
                }
            }
        };
        mEtPwd.addTextChangedListener(watcher);
        mEtAgainPwd.addTextChangedListener(watcher);
        return view;
    }

    @Override
    protected void setViewListener() {
        mTvContinue.setOnClickListener(v -> checkInput());
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mSign = intent.getStringExtra(CHANGE_PWD_SIGN);
        }
    }

    /**
     * 校验
     */
    private void checkInput() {
        String inputPwd = mEtPwd.getText().toString().trim();
        String inputAgainPwd = mEtAgainPwd.getText().toString().trim();
        if (!TextUtils.equals(inputPwd, inputAgainPwd)) {
            // 不相等
            ToastUtil.showTextViewPrompt(getString(R.string.entered_passwords_differ));
            return;
        }
        showLoadingDialog(true);
        String name = SharePreferenceUtils.getInstance().getUserName();
        String pwd = SharePreferenceUtils.getInstance().getUserPwd();
        AppManager.getInstance().getTokenByName(name, pwd, new HttpResponseCallback<String>() {
            @Override
            public void onResponse(String result) {
                HttpResponseCallback<Boolean> callback = new HttpResponseCallback<Boolean>() {
                    @Override
                    public void onResponse(Boolean result) {
                        dismissLoadingDialog();
                        if (result != null && result) {
                            finish();
                        } else {
                            ToastUtil.showTextViewPrompt(getString(R.string.failed_to_change_password));
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        dismissLoadingDialog();
                        ToastUtil.showTextViewPrompt(errorMsg);
                    }
                };
                AppManager.getInstance().pwdChange(result, inputPwd, inputAgainPwd, mSign, callback);
            }

            @Override
            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                dismissLoadingDialog();
                ToastUtil.showTextViewPrompt(getString(R.string.failed_to_change_password));
            }
        });
    }
}
