
package top.maxim.im;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.List;

import top.maxim.im.common.base.BaseSwitchActivity;
import top.maxim.im.contact.view.ContactFragment;
import top.maxim.im.login.view.MineFragment;
import top.maxim.im.message.view.SessionFragment;
import top.maxim.im.push.PushClientMgr;

/**
 * Description : 首页view Created by Mango on 2018/11/05.
 */
public class MainActivity extends BaseSwitchActivity {

    protected TabSwitchView mSessionTab;

    protected TabSwitchView mContactTab;

    protected TabSwitchView mSettingTab;

    public static void openMain(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
        ((Activity)context).finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PushClientMgr.getManager().register(this);
    }

    @Override
    protected void initFragment(final List<TabSwitchView> tabSwitch) {
        mSessionTab = new BaseSwitchActivity.TabSwitchView(R.drawable.message_icon_selector,
                R.string.tab_chat, new SessionFragment(), 0);
        tabSwitch.add(mSessionTab);

        mContactTab = new BaseSwitchActivity.TabSwitchView(R.drawable.contact_icon_selector,
                R.string.tab_contact, new ContactFragment(), 1);
        tabSwitch.add(mContactTab);

        mSettingTab = new BaseSwitchActivity.TabSwitchView(R.drawable.mine_icon_selector,
                R.string.tab_mine, new MineFragment(), 2);
        tabSwitch.add(mSettingTab);
    }

    @Override
    protected void onTabClick() {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
