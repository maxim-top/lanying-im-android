
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import top.maxim.im.R;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonDialog;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ItemLine;
import top.maxim.im.common.view.ItemLineArrow;
import top.maxim.im.common.view.ItemLineSwitch;
import top.maxim.im.login.bean.DNSConfigEvent;
import top.maxim.im.scan.config.ScanConfigs;

/**
 * Description : DNS配置 Created by Mango on 2020/06/29
 */
public class DNSConfigActivity extends BaseTitleActivity {

    /* 自定义开关 */
    private ItemLineSwitch.Builder mSetCustom;

    /* 设置appId */
    private ItemLineArrow.Builder mSetAppId;

    /* 设置server */
    private ItemLineArrow.Builder mSetServer;

    /* 设置port */
    private ItemLineArrow.Builder mSetPort;

    /* 设置restServer */
    private ItemLineArrow.Builder mSetRestServer;
    
    /* 是否自定义配置 */
    private boolean mCustom = false;

    private String mAppId;

    private String mServer;

    private String mPort;

    private String mRestServer;

    public static void startDNSConfigActivity(Context context) {
        Intent intent = new Intent(context, DNSConfigActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.dns_config);
        builder.setBackIcon(R.drawable.header_back_icon, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        builder.setRightText(getString(R.string.dns_change_save), v -> saveDNSConfig());
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.activity_dns_config, null);
        LinearLayout container = view.findViewById(R.id.ll_config_container);
        initView(container);
        return view;
    }

    private void initView(LinearLayout container) {
        // appId
        mSetAppId = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.dns_config_appId))
                .setArrowVisible(false)
                .setOnItemClickListener(
                        v -> showEditDialog(getString(R.string.dns_change_appId_title), false));
        container.addView(mSetAppId.build());

        // 自定义服务
        mSetCustom = new ItemLineSwitch.Builder(this).setMarginTop(ScreenUtils.dp2px(20))
                .setLeftText(getString(R.string.dns_config_custom))
                .setOnItemSwitchListener((v, curCheck) -> {
                    if (curCheck) {
                        // 开启
                        changeCustomStatus(true);
                        return;
                    }
                    // 关闭需要弹出提示
                    DialogUtils.getInstance().showDialog(this,
                            getString(R.string.dns_config_custom),
                            getString(R.string.dns_change_custom_title),
                            new CommonDialog.OnDialogListener() {
                                @Override
                                public void onConfirmListener() {
                                    changeCustomStatus(false);
                                }

                                @Override
                                public void onCancelListener() {
                                    changeCustomStatus(true);
                                }
                            });
                });
        container.addView(mSetCustom.build());
        addLineView(container);

        // server
        mSetServer = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.dns_config_server))
                .setEndContent(getString(R.string.dns_config_default))
                .setArrowVisible(false)
                .setOnItemClickListener(v -> {
                    if (mCustom) {
                        showEditDialog(getString(R.string.dns_change_server_title), false);
                    } else {
                        ToastUtil.showTextViewPrompt(getString(R.string.dns_change_custom_tips));
                    }
                });
        container.addView(mSetServer.build());
        addLineView(container);

        // port
        mSetPort = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.dns_config_port))
                .setEndContent(getString(R.string.dns_config_default))
                .setArrowVisible(false)
                .setOnItemClickListener(v -> {
                    if (mCustom) {
                        showEditDialog(getString(R.string.dns_change_port_title), true);
                    } else {
                        ToastUtil.showTextViewPrompt(getString(R.string.dns_change_custom_tips));
                    }
                });
        container.addView(mSetPort.build());
        addLineView(container);

        // rest server
        mSetRestServer = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.dns_config_rest_server))
                .setEndContent(getString(R.string.dns_config_default))
                .setArrowVisible(false)
                .setOnItemClickListener(v -> {
                    if (mCustom) {
                        showEditDialog(getString(R.string.dns_change_rest_server_title), false);
                    } else {
                        ToastUtil.showTextViewPrompt(getString(R.string.dns_change_custom_tips));
                    }
                });
        container.addView(mSetRestServer.build());
    }
    
    // 添加分割线
    private View addLineView(ViewGroup container) {
        // 分割线
        View view;
        ItemLine.Builder itemLine = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(view = itemLine.build());
        return view;
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        String appId = SharePreferenceUtils.getInstance().getAppId();
        mSetAppId.setEndContent(appId);
    }

    /**
     * 切换自定义开关
     * 
     * @param curCheck 状态
     */
    private void changeCustomStatus(boolean curCheck) {
        mCustom = curCheck;
        if (mSetCustom != null) {
            mSetCustom.setCheckStatus(curCheck);
        }
        if (!mCustom) {
            if (mSetServer != null) {
                mServer = null;
                mSetServer.setEndContent(getString(R.string.dns_config_default));
            }
            if (mSetPort != null) {
                mPort = null;
                mSetPort.setEndContent(getString(R.string.dns_config_default));
            }
            if (mSetRestServer != null) {
                mRestServer = null;
                mSetRestServer.setEndContent(getString(R.string.dns_config_default));
            }
        }
    }
    
    /**
     * 展示输入框dialog
     * 
     * @param title 标题
     */
    private void showEditDialog(String title, boolean number) {
        DialogUtils.getInstance().showEditDialog(this, title, getString(R.string.confirm),
                getString(R.string.cancel), number, new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        if (TextUtils.equals(title, getString(R.string.dns_change_appId_title))) {
                            // 修改appId
                            mAppId = content;
                            if (mSetAppId != null) {
                                mSetAppId.setEndContent(
                                        TextUtils.isEmpty(content) ? ScanConfigs.CODE_APP_ID
                                                : content);
                            }
                        } else if (TextUtils.equals(title,
                                getString(R.string.dns_change_server_title))) {
                            // 修改IM Server
                            mServer = content;
                            if (mSetServer != null) {
                                mSetServer.setEndContent(TextUtils.isEmpty(content)
                                        ? getString(R.string.dns_config_default)
                                        : content);
                            }
                        } else if (TextUtils.equals(title,
                                getString(R.string.dns_change_port_title))) {
                            // 修改IM Port
                            mPort = content;
                            if (mSetPort != null) {
                                mSetPort.setEndContent(TextUtils.isEmpty(content)
                                        ? getString(R.string.dns_config_default)
                                        : content);
                            }
                        } else if (TextUtils.equals(title,
                                getString(R.string.dns_change_rest_server_title))) {
                            // 修改Rest Server
                            mRestServer = content;
                            if (mSetRestServer != null) {
                                mSetRestServer.setEndContent(TextUtils.isEmpty(content)
                                        ? getString(R.string.dns_config_default)
                                        : content);
                            }
                        }
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 保存DNS配置
     */
    private void saveDNSConfig() {
        DNSConfigEvent event = new DNSConfigEvent();
        event.setAppId(mAppId);
        if (mCustom) {
            // DNS自定义配置开启时候
            if (TextUtils.isEmpty(mServer) || TextUtils.isEmpty(mPort)
                    || TextUtils.isEmpty(mRestServer)) {
                // 三项配置有一项为空 则不保存
                ToastUtil.showTextViewPrompt(getString(R.string.dns_change_save_tip));
                changeCustomStatus(false);
                return;
            }
            event.setServer(mServer);
            event.setPort(Integer.valueOf(mPort).intValue());
            event.setRestServer(mRestServer);
        }
        RxBus.getInstance().send(event);
        finish();
    }
}
