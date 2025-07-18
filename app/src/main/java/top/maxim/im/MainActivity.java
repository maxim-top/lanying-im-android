
package top.maxim.im;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.common.GoogleApiAvailability;

import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import top.maxim.im.common.base.BaseSwitchActivity;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.common.utils.CommonConfig;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.SchemeUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.permissions.PermissionsConstant;
import top.maxim.im.common.utils.permissions.PermissionsMgr;
import top.maxim.im.common.utils.permissions.PermissionsResultAction;
import top.maxim.im.contact.view.AllContactFragment;
import top.maxim.im.login.view.MineFragment;
import top.maxim.im.message.view.SessionFragment;
import top.maxim.im.push.NotificationUtils;
import top.maxim.im.push.PushClientMgr;
import top.maxim.im.push.maxim.MaxIMPushService;

/**
 * Description : 首页view Created by Mango on 2018/11/05.
 */
public class MainActivity extends BaseSwitchActivity {

    protected TabSwitchView mSessionTab;

    protected TabSwitchView mContactTab;

    protected TabSwitchView mSettingTab;

    private int mLastSelectedIndex;// 上一次点击的索引
    private long mLastClickTime; // 上一次点击的时间

    public static void openMain(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        ((Activity)context).finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(PushClientMgr.isGoogle(this)){
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);
        }
        initRxBus();
        //启动后台服务
        MaxIMPushService.startPushService(this);
        String urlString = SharePreferenceUtils.getInstance().getDeepLink();
        if (!TextUtils.isEmpty(urlString)){
            SchemeUtils.handleScheme(this, urlString);
        }
        initPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();

        String urlString = SharePreferenceUtils.getInstance().getDeepLink();
        if (!TextUtils.isEmpty(urlString)){
            SchemeUtils.handleScheme(this, urlString);
        }
    }

    private void initPermission(){
        NotificationManager manager = (NotificationManager) AppContextUtils.getAppContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (!manager.areNotificationsEnabled()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
                String[] permissions = new String[] {
                        PermissionsConstant.POST_NOTIFICATIONS
                };
                if (!PermissionsMgr.getInstance().hasAllPermissions(this, permissions)) {
                    PermissionsMgr.getInstance().requestPermissionsIfNecessaryForResult(this,
                            permissions, new PermissionsResultAction() {

                                @Override
                                public void onGranted(List<String> perms) {
                                }

                                @Override
                                public void onDenied(List<String> perms) {
                                }
                            });
                }
            }
        }
    }

    @Override
    protected void initFragment(final List<TabSwitchView> tabSwitch) {
        mSessionTab = new BaseSwitchActivity.TabSwitchView(R.drawable.message_icon_selector,
                R.string.tab_chat, new SessionFragment(), 0);
        tabSwitch.add(mSessionTab);

        mContactTab = new BaseSwitchActivity.TabSwitchView(R.drawable.contact_icon_selector,
                R.string.tab_contact, new AllContactFragment(), 1);
        tabSwitch.add(mContactTab);

        mSettingTab = new BaseSwitchActivity.TabSwitchView(R.drawable.mine_icon_selector,
                R.string.tab_mine, new MineFragment(), 2);
        tabSwitch.add(mSettingTab);
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
    }

    @Override
    protected void onTabClick() {
        super.onTabClick();
        long now = System.currentTimeMillis(); // 返回毫秒级时间戳
        // 两次点击按钮都是“对话”， 时间间隔 < 0.5秒
        if (mLastSelectedIndex == 0 && mCurrentIndex == 0 &&
                (now - mLastClickTime) < 500){
            Intent intent = new Intent();
            intent.setAction("chatDoubleClicked");
            RxBus.getInstance().send(intent);
            mLastClickTime = 0;
        }else{
            mLastClickTime = now;
            mLastSelectedIndex = mCurrentIndex;
        }
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

    private void initRxBus() {
        RxBus.getInstance().toObservable(Intent.class).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Intent>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Intent intent) {
                        if (intent == null) {
                            return;
                        }
                        String action = intent.getAction();
                        if (mSessionTab != null && TextUtils.equals(action, CommonConfig.SESSION_COUNT_ACTION)) {
                            int count = intent.getIntExtra(CommonConfig.TAB_COUNT, 0);
                            mSessionTab.setCount(count);
                            NotificationUtils.getInstance().setCorner(null, count);
                        }
                    }
                });
    }
}
