
package top.maxim.im.common;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import im.floo.BMXCallBack;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.TaskDispatcher;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.message.utils.MessageConfig;

public class GuardianModeActivity extends BaseTitleActivity {
    /* 开启/关闭 */
    private TextView mSwitch;
    private boolean mIsClosed = true;
    private final int PASSWORD_REQUEST = 1011;

    public static void open(Context context) {
        Intent intent = new Intent(context, GuardianModeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle("");
        builder.setBackIcon(R.drawable.header_back_icon, v -> finish());
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.activity_guardian_mode, null);
        mHeader.setTitle(getString(R.string.guardian_mode));
        mSwitch = view.findViewById(R.id.tv_switch);
        mSwitch.setEnabled(true);
        showLoadingDialog(true);
        mSwitch.setOnClickListener(v -> {
            PasswordActivity.open(this, PASSWORD_REQUEST);
        });
        updateSwitchButton();
        return view;
    }

    private void updateSwitchButton() {
        showLoadingDialog(true);
        UserManager.getInstance().getProfile(true, (bmxErrorCode1, profile) -> {
            dismissLoadingDialog();
            TaskDispatcher.exec(() -> {
                if (BaseManager.bmxFinish(bmxErrorCode1) && profile != null
                        && profile.userId() > 0) {
                    TaskDispatcher.postMain(() -> {
                        try {
                            String enc = profile.privateInfo();
                            String info = URLDecoder.decode(enc, "UTF-8");
                            JSONObject jsonObject = new JSONObject(info);
                            if (jsonObject.has("guardian_mode")) {
                                mIsClosed = !TextUtils.equals(jsonObject.getString("guardian_mode"),"on");
                            }
                            if (mIsClosed) {
                                mSwitch.setText(getString(R.string.open));
                            } else {
                                mSwitch.setText(getString(R.string.close));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
        });
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PASSWORD_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                String password = data.getStringExtra("password");
                String newValue = "on";
                if (!mIsClosed){
                    newValue = "off";
                    String oldPassword = SharePreferenceUtils.getInstance().getGuardianModePassword();
                    if (!TextUtils.equals(oldPassword, password)){
                        ToastUtil.showTextViewPrompt("Password not incorrect!");
                        return;
                    }
                }else{
                    SharePreferenceUtils.getInstance().putGuardianModePassword(password);
                }
                String info = "{\"guardian_mode\":\"" + newValue + "\"}";
                try {
                    String enc = URLEncoder.encode(info, "UTF-8");
                    BMXCallBack callBack = bmxErrorCode -> {
                        dismissLoadingDialog();
                        if (!BaseManager.bmxFinish(bmxErrorCode)) {
                            String error = bmxErrorCode != null ? bmxErrorCode.name() : getString(R.string.network_exception);
                            ToastUtil.showTextViewPrompt(error);
                        }else{
                            updateSwitchButton();
                        }

                    };
                    UserManager.getInstance().setPrivateInfo(enc, callBack);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
