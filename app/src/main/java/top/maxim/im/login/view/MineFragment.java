
package top.maxim.im.login.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gavin.view.flexible.FlexibleLayout;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXUserProfile;
import top.maxim.im.BuildConfig;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleFragment;
import top.maxim.im.common.utils.CommonUtils;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ItemLineArrow;
import top.maxim.im.common.view.ItemLineSwitch;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.contact.view.BlockListActivity;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.net.HttpResponseCallback;

/**
 * Description : 我的 Created by Mango on 2018/11/06
 */
public class MineFragment extends BaseTitleFragment {

    private FlexibleLayout mFlexibleLayout;

    private ScrollView mScrollView;

    private RelativeLayout mRlUserInfo;

    private ImageView mUserEdit;

    private ShapeImageView mUserIcon;

    private TextView mUserName;

    private TextView mUserId;

    private TextView mNickName;

    private TextView mUserPubInfo;

    /* 退出登录 */
    private TextView mQuitView;

    /* 我的二维码 */
    private ImageView mMyQrCode;

    /* 账号管理 */
    private ItemLineArrow.Builder mAccountManger;

    /* 接受新消息通知 */
    private ItemLineSwitch.Builder mSettingPush;

    /* 声音 */
    private ItemLineSwitch.Builder mPushSound;

    private View mPushSoundView;

    /* 振动 */
    private ItemLineSwitch.Builder mPushVibrate;

    private View mPushVibrateView;

    /* 是否显示推送详情 */
    private ItemLineSwitch.Builder mPushDetail;

    /* 设置推送昵称 */
    private ItemLineArrow.Builder mPushName;

    /* 设置自动下载附件 */
    private ItemLineSwitch.Builder autoDownloadAttachment;

    /* 设置自动接收群邀请 */
    private ItemLineSwitch.Builder autoAcceptGroupInvite;

    /* 黑名单 */
    private ItemLineArrow.Builder mBlockList;

    /* 多设备列表 */
    private ItemLineArrow.Builder mDeviceList;

    /* 设置多端提示 */
    private ItemLineSwitch.Builder otherDevTips;

    /* 解绑微信 */
    private ItemLineArrow.Builder mUnBindWeChat;

    /* 关于我们 */
    private ItemLineArrow.Builder mAboutUs;

    /* 用户服务 */
    private ItemLineArrow.Builder mProtocolTerms;

    /* 隐私政策 */
    private ItemLineArrow.Builder mProtocolPrivacy;

    /* app版本号 */
    private TextView mAppVersion;

    private ImageRequestConfig mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
            .showImageForEmptyUri(R.drawable.default_avatar_icon)
            .showImageOnFail(R.drawable.default_avatar_icon).cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.default_avatar_icon)
            .build();

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(getActivity(), headerContainer);
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        hideTitleHeader();
        View view = View.inflate(getActivity(), R.layout.fragment_mine, null);
        mFlexibleLayout = view.findViewById(R.id.flexible_layout);
        mScrollView = view.findViewById(R.id.scroll_user);
        mRlUserInfo = view.findViewById(R.id.rl_user_info);
        mUserEdit = view.findViewById(R.id.iv_user_info_edit);
        mUserIcon = view.findViewById(R.id.iv_user_avatar);
        mUserName = view.findViewById(R.id.tv_user_name);
        mNickName = view.findViewById(R.id.tv_nick_name);
        mUserPubInfo = view.findViewById(R.id.tv_public_info);
        mUserId = view.findViewById(R.id.tv_user_id);
        mQuitView = view.findViewById(R.id.tv_quit_app);
        mAppVersion = view.findViewById(R.id.tv_version_app);
        mMyQrCode = view.findViewById(R.id.icon_qrcode);

        mFlexibleLayout.setHeader(mRlUserInfo).setReadyListener(() ->
        // 下拉放大的条件
        mScrollView.getScrollY() == 0);
        // 获取app版本
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        if (!TextUtils.isEmpty(versionName)) {
            mAppVersion.setText(versionName + "(Build " + versionCode + ")");
        }
        LinearLayout container = view.findViewById(R.id.ll_mine_container);

        // 接受push
        mSettingPush = new ItemLineSwitch.Builder(getActivity())
                .setLeftText(getString(R.string.receive_push_notice))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {
                        setPushEnable(curCheck);
                    }
                });
        container.addView(mSettingPush.build());

        // 分割线
//        ItemLine.Builder itemLine1 = new ItemLine.Builder(getActivity(), container)
//                .setMarginLeft(ScreenUtils.dp2px(15));
//        container.addView(itemLine1.build(), 1);

        // 声音
        mPushSound = new ItemLineSwitch.Builder(getActivity())
                .setLeftText(getString(R.string.push_sound))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {
                        setPushSoundEnable(curCheck);
                    }
                });
        container.addView(mPushSoundView = mPushSound.build());

        // 振动
        mPushVibrate = new ItemLineSwitch.Builder(getActivity())
                .setLeftText(getString(R.string.push_vibrate))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {
                        setPushVibrateEnable(curCheck);
                    }
                });
        container.addView(mPushVibrateView = mPushVibrate.build());

        // 是否推送详情
        mPushDetail = new ItemLineSwitch.Builder(getActivity())
                .setLeftText(getString(R.string.push_detail))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {
                        setPushDetailEnable(curCheck);
                    }
                });
        container.addView(mPushDetail.build());

        // 推送昵称
        mPushName = new ItemLineArrow.Builder(getActivity())
                .setStartContent(getString(R.string.push_name))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showPushNameDialog();
                    }
                });
        container.addView(mPushName.build());

        // 是否自动下载附件
        autoDownloadAttachment = new ItemLineSwitch.Builder(getActivity())
                .setLeftText(getString(R.string.auto_download_attachment))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {
                        setAutoDownloadAttachmentEnable(curCheck);
                    }
                });
        container.addView(autoDownloadAttachment.build());

        // 是否自动接收群邀请
        autoAcceptGroupInvite = new ItemLineSwitch.Builder(getActivity())
                .setLeftText(getString(R.string.auto_accept_group_invite))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {
                        setAutoAcceptGroupInviteEnable(curCheck);
                    }
                });
        container.addView(autoAcceptGroupInvite.build());

        // 黑名单列表
        mBlockList = new ItemLineArrow.Builder(getActivity())
                .setStartContent(getString(R.string.black_list))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        BlockListActivity.startBlockActivity(getActivity());
                    }
                });
        container.addView(mBlockList.build());

        // 是否多端提示
        otherDevTips = new ItemLineSwitch.Builder(getActivity())
                .setLeftText(getString(R.string.other_device_tips))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {
                        SharePreferenceUtils.getInstance().putDevTips(curCheck);
                    }
                });
        container.addView(otherDevTips.build());

        // 多设备列表
        mDeviceList = new ItemLineArrow.Builder(getActivity())
                .setStartContent(getString(R.string.device_list))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        DeviceListActivity.startDeviceActivity(getActivity());
                    }
                });
        container.addView(mDeviceList.build());

        // 微信解绑
        mUnBindWeChat = new ItemLineArrow.Builder(getActivity()).setStartContent("解除微信绑定")
                .setArrowVisible(false).setOnItemClickListener(v -> unBindWeChat());
        View viewBindWeChat = mUnBindWeChat.build();
        container.addView(viewBindWeChat);
        viewBindWeChat.setVisibility(View.GONE);

        // 关于我们
        mAboutUs = new ItemLineArrow.Builder(getActivity())
                .setStartContent(getString(R.string.about_us))
                .setOnItemClickListener(v -> AboutUsActivity.startAboutUsActivity(getActivity()));
        container.addView(mAboutUs.build());

        // 用户服务
        mProtocolTerms = new ItemLineArrow.Builder(getActivity())
                .setStartContent(getString(R.string.register_protocol2))
                .setOnItemClickListener(v -> ProtocolActivity.openProtocol(getActivity(), 1));
        container.addView(mProtocolTerms.build());

        // 隐私政策
        mProtocolPrivacy = new ItemLineArrow.Builder(getActivity())
                .setStartContent(getString(R.string.register_protocol4))
                .setOnItemClickListener(v -> ProtocolActivity.openProtocol(getActivity(), 0));
        container.addView(mProtocolPrivacy.build());

        // 账号管理
        mAccountManger = new ItemLineArrow.Builder(getActivity())
                .setStartContent(getString(R.string.setting_account_manager))
                .setMarginTop(ScreenUtils.dp2px(10))
                .setOnItemClickListener(v -> AccountListActivity.startAccountListActivity(getActivity()));
        container.addView(mAccountManger.build(), 0);

//        // 分割线
//        ItemLine.Builder itemLine0 = new ItemLine.Builder(getActivity(), container)
//                .setMarginLeft(ScreenUtils.dp2px(15));
//        container.addView(itemLine0.build(), 1);
        return view;
    }

    @Override
    protected void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity() == null) {
                return;
            }
            Window window = getActivity().getWindow();
            int systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_VISIBLE;
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(systemUiVisibility);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mRlUserInfo
                    .getLayoutParams();
            params.height = ScreenUtils.dp2px(150) + ScreenUtils.getStatusBarHeight();
            mRlUserInfo.setPadding(mRlUserInfo.getPaddingLeft(), ScreenUtils.getStatusBarHeight(),
                    mRlUserInfo.getPaddingRight(), mRlUserInfo.getPaddingBottom());
            mScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX,
                        int oldScrollY) {
                    if (ScreenUtils.getStatusBarHeight() < scrollY) {
                        window.setStatusBarColor(getResources().getColor(R.color.color_0079F4));
                    } else {
                        window.setStatusBarColor(Color.TRANSPARENT);
                    }
                }
            });
        }
    }

    @Override
    public void onShow() {
        super.onShow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity() == null || mScrollView == null) {
                return;
            }
            Window window = getActivity().getWindow();
            int scrollY = mScrollView.getScrollY();
            if (ScreenUtils.getStatusBarHeight() < scrollY) {
                window.setStatusBarColor(getResources().getColor(R.color.color_0079F4));
            } else {
                window.setStatusBarColor(Color.TRANSPARENT);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        UserManager.getInstance().getProfile(false, (bmxErrorCode, bmxUserProfile) -> {
            if (BaseManager.bmxFinish(bmxErrorCode) && bmxUserProfile != null) {
                initUser(bmxUserProfile);
            }
        });
    }

    private void initUser(BMXUserProfile profile) {
        String name = profile.username();
        String nickName = profile.nickname();
        String publicInfo = profile.publicInfo();
        ChatUtils.getInstance().showProfileAvatar(profile, mUserIcon, mConfig);
        long userId = profile.userId();
        mUserName.setText(TextUtils.isEmpty(name) ? "" : name);
        mNickName.setText("昵称:" + (TextUtils.isEmpty(nickName) ? "请设置昵称" : nickName));
        mUserId.setText(userId <= 0 ? "" : "ID:" + userId);
        mUserPubInfo.setText("个性签名:" + (TextUtils.isEmpty(publicInfo) ? "赶快去设置签名吧" : publicInfo));
        // push
        BMXUserProfile.MessageSetting setting = profile.messageSetting();
        boolean isPush = setting != null && setting.getMPushEnabled();
        boolean isPushDetail = setting != null && setting.getMPushDetail();
        boolean isPushSound = setting != null && setting.getMNotificationSound();
        boolean isPushVibrate = setting != null && setting.getMNotificationVibrate();
        mSettingPush.setCheckStatus(isPush);
        mPushDetail.setCheckStatus(isPushDetail);
        if (isPush) {
            mPushSoundView.setVisibility(View.VISIBLE);
            mPushVibrateView.setVisibility(View.VISIBLE);
            mPushSound.setCheckStatus(isPushSound);
            mPushVibrate.setCheckStatus(isPushVibrate);
        } else {
            mPushSoundView.setVisibility(View.GONE);
            mPushVibrateView.setVisibility(View.GONE);
        }
        boolean isAutoDownload = setting != null && setting.getMAutoDownloadAttachment();
        autoDownloadAttachment.setCheckStatus(isAutoDownload);

        String pushName = setting != null && !TextUtils.isEmpty(setting.getMPushNickname())
                ? setting.getMPushNickname()
                : "";
        mPushName.setEndContent(pushName);

        autoAcceptGroupInvite.setCheckStatus(profile.isAutoAcceptGroupInvite());
        // 是否多端提示 默认false
        boolean tips = SharePreferenceUtils.getInstance().getDevTips();
        otherDevTips.setCheckStatus(tips);
    }

    @Override
    protected void setViewListener() {
        mQuitView.setOnClickListener(v -> logout());

        mUserEdit.setOnClickListener(v -> SettingUserActivity.openSettingUser(getActivity()));

        mMyQrCode.setOnClickListener(v -> MyQrCodeActivity.openMyQrcode(getActivity()));

//         三次点击 解绑微信
//        ClickTimeUtils.setClickTimes(mUserIcon, 5, () -> unBindWeChat());
    }

    @Override
    public void onDestroyView() {
        setNull(mQuitView);
        super.onDestroyView();
    }

    /**
     * 设置是否推送
     * 
     * @param enable
     */
    private void setPushEnable(final boolean enable) {
        showLoadingDialog(true);
        UserManager.getInstance().setEnablePush(enable, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                if (enable) {
                    mPushSoundView.setVisibility(View.VISIBLE);
                    mPushVibrateView.setVisibility(View.VISIBLE);
                } else {
                    mPushSoundView.setVisibility(View.GONE);
                    mPushVibrateView.setVisibility(View.GONE);

                }
            } else {
                toastError(bmxErrorCode);
                mPushDetail.setCheckStatus(!enable);
            }
        });
    }

    /**
     * 设置是否推送详情
     * 
     * @param enable
     */
    private void setPushDetailEnable(final boolean enable) {
        showLoadingDialog(true);
        UserManager.getInstance().setEnablePushDetaile(enable, bmxErrorCode -> {
            dismissLoadingDialog();
            if (!BaseManager.bmxFinish(bmxErrorCode)) {
                toastError(bmxErrorCode);
                mSettingPush.setCheckStatus(!enable);
            }
        });
    }

    /**
     * 设置是否声音
     * 
     * @param enable
     */
    private void setPushSoundEnable(final boolean enable) {
        showLoadingDialog(true);
        UserManager.getInstance().setNotificationSound(enable, bmxErrorCode -> {
            dismissLoadingDialog();
            if (!BaseManager.bmxFinish(bmxErrorCode)) {
                toastError(bmxErrorCode);
                mPushSound.setCheckStatus(!enable);
            }
        });
    }

    /**
     * 设置是否振动
     * 
     * @param enable
     */
    private void setPushVibrateEnable(final boolean enable) {
        showLoadingDialog(true);
        UserManager.getInstance().setNotificationVibrate(enable, bmxErrorCode -> {
            dismissLoadingDialog();
            if (!BaseManager.bmxFinish(bmxErrorCode)) {
                toastError(bmxErrorCode);
                mPushVibrate.setCheckStatus(!enable);
            }
        });
    }

    /**
     * 设置是否自动下载附件
     * 
     * @param enable
     */
    private void setAutoDownloadAttachmentEnable(final boolean enable) {
        showLoadingDialog(true);
        UserManager.getInstance().setAutoDownloadAttachment(enable, bmxErrorCode -> {
            dismissLoadingDialog();
            if (!BaseManager.bmxFinish(bmxErrorCode)) {
                toastError(bmxErrorCode);
                autoDownloadAttachment.setCheckStatus(!enable);
            }
        });
    }

    /**
     * 设置是否自动接收群邀请
     * 
     * @param enable
     */
    private void setAutoAcceptGroupInviteEnable(final boolean enable) {
        showLoadingDialog(true);
        UserManager.getInstance().setAutoAcceptGroupInvite(enable, bmxErrorCode -> {
            dismissLoadingDialog();
            if (!BaseManager.bmxFinish(bmxErrorCode)) {
                toastError(bmxErrorCode);
                autoAcceptGroupInvite.setCheckStatus(!enable);
            }
        });
    }

    /**
     * 设置推送昵称
     * 
     * @param name
     */
    private void setPushName(final String name) {
        // if (TextUtils.isEmpty(name)) {
        // return;
        // }
        showLoadingDialog(true);
        UserManager.getInstance().setPushNickname(name, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                mPushName.setEndContent(name);
            } else {
                toastError(bmxErrorCode);
            }
        });
    }

    /**
     * 设置推送名称dialog
     */
    private void showPushNameDialog() {
        DialogUtils.getInstance().showEditDialog(getActivity(), getString(R.string.push_name),
                getString(R.string.confirm), getString(R.string.cancel),
                new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        setPushName(content);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    void logout() {
        showLoadingDialog(true);
        UserManager.getInstance().signOut((bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                CommonUtils.getInstance().logout();
                WelcomeActivity.openWelcome(getActivity());
            } else {
                toastError(bmxErrorCode);
            }
        }));
    }

    /**
     * 解除微信绑定
     */
    private void unBindWeChat() {
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
                        ToastUtil.showTextViewPrompt(result != null && result ? "解除成功" : "解除失败");
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        dismissLoadingDialog();
                        ToastUtil.showTextViewPrompt("解除失败");
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

    private void toastError(Throwable e) {
        String error = e != null ? e.getMessage() : "网络异常";
        ToastUtil.showTextViewPrompt(error);
    }

    private void toastError(BMXErrorCode e) {
        String error = e != null ? e.name() : "网络异常";
        ToastUtil.showTextViewPrompt(error);
    }
}
