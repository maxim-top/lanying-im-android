
package top.maxim.im;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import java.util.List;

import top.maxim.im.common.base.BaseSwitchActivity;
import top.maxim.im.login.view.LoginByVerifyFragment;
import top.maxim.im.login.view.LoginFragment;
import top.maxim.im.login.view.RegisterFragment;

/**
 * Description : 登录与注册界面.
 */
public class LoginRegisterActivity extends BaseSwitchActivity {

    protected TabSwitchView mLoginByUsernameTab;

    protected TabSwitchView mLoginByVerifyTab;

    protected TabSwitchView mRegitsterTab;
    public static void openLoginRegister(Context context, boolean clearTask) {
        Intent intent = new Intent(context, LoginRegisterActivity.class);
        int flag = Intent.FLAG_ACTIVITY_NEW_TASK;
        if (clearTask){
            flag |= Intent.FLAG_ACTIVITY_CLEAR_TASK;
            intent.putExtra("index", LoginRegisterActivity.LOGIN_REGISTER_INDEX.LOGIN_BY_USERNAME.ordinal());
        }
        intent.addFlags(flag);
        context.startActivity(intent);
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            int index = intent.getIntExtra("index", 0);
            setCurrentIndex(index);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mUseCardFlipAnim = true;
        super.onCreate(savedInstanceState);
    }

    protected View getView(){
        return View.inflate(this, R.layout.activity_login_register, null);
    }

    @Override
    protected void initFragment(final List<TabSwitchView> tabSwitch) {
        mLoginByVerifyTab = new TabSwitchView(-1, -1, new LoginByVerifyFragment(), LoginRegisterActivity.LOGIN_REGISTER_INDEX.LOGIN_BY_VERIFY.ordinal());
        tabSwitch.add(mLoginByVerifyTab);

        mLoginByUsernameTab = new TabSwitchView(-1, -1, new LoginFragment(), LOGIN_REGISTER_INDEX.LOGIN_BY_USERNAME.ordinal());
        tabSwitch.add(mLoginByUsernameTab);

        mRegitsterTab = new TabSwitchView(-1, -1, new RegisterFragment(), LOGIN_REGISTER_INDEX.REGISTER.ordinal());
        tabSwitch.add(mRegitsterTab);
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
    }

    @Override
    protected void onTabClick() {
        super.onTabClick();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mCurrentFragment != null) {
            mCurrentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public enum LOGIN_REGISTER_INDEX {
        LOGIN_BY_VERIFY,
        LOGIN_BY_USERNAME,
        REGISTER
    }
}
