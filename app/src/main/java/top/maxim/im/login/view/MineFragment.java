
package top.maxim.im.login.view;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXUserProfile;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.BuildConfig;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleFragment;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ItemLine;
import top.maxim.im.common.view.ItemLineArrow;
import top.maxim.im.common.view.ItemLineSwitch;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.contact.view.BlockListActivity;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.push.PushClientMgr;

/**
 * Description : 我的 Created by Mango on 2018/11/06
 */
public class MineFragment extends BaseTitleFragment {

    /* 用户信息 */
    private RelativeLayout mUserInfo;

    private ShapeImageView mUserIcon;

    private TextView mUserName;

    private TextView mUserId;

    private TextView mNickName;

    /* 退出登录 */
    private TextView mQuitView;

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
        builder.setTitle(R.string.tab_mine);
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(getActivity(), R.layout.fragment_mine, null);
        mUserInfo = view.findViewById(R.id.rl_user_info);
        mUserIcon = view.findViewById(R.id.iv_user_avatar);
        mUserName = view.findViewById(R.id.tv_user_name);
        mNickName = view.findViewById(R.id.tv_nick_name);
        mUserId = view.findViewById(R.id.tv_user_id);
        mQuitView = view.findViewById(R.id.tv_quit_app);
        mAppVersion = view.findViewById(R.id.tv_version_app);
        // 获取app版本
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        if (versionCode > 0 && !TextUtils.isEmpty(versionName)) {
            mAppVersion.setText(versionName + "(Build " + versionCode + ")");
        }
        LinearLayout container = view.findViewById(R.id.ll_mine_container);

        // 接受push
        mSettingPush = new ItemLineSwitch.Builder(getActivity())
                .setLeftText(getString(R.string.receive_push_notice))
                .setMarginTop(ScreenUtils.dp2px(10))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {
                        setPushEnable(curCheck);
                    }
                });
        container.addView(mSettingPush.build(), 0);

        // 分割线
        ItemLine.Builder itemLine1 = new ItemLine.Builder(getActivity(), container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine1.build(), 1);

        // 声音
        mPushSound = new ItemLineSwitch.Builder(getActivity())
                .setLeftText(getString(R.string.push_sound))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {
                        setPushSoundEnable(curCheck);
                    }
                });
        container.addView(mPushSoundView = mPushSound.build(), 2);

        // 振动
        mPushVibrate = new ItemLineSwitch.Builder(getActivity())
                .setLeftText(getString(R.string.push_vibrate))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {
                        setPushVibrateEnable(curCheck);
                    }
                });
        container.addView(mPushVibrateView = mPushVibrate.build(), 3);

        // 是否推送详情
        mPushDetail = new ItemLineSwitch.Builder(getActivity())
                .setLeftText(getString(R.string.push_detail))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {
                        setPushDetailEnable(curCheck);
                    }
                });
        container.addView(mPushDetail.build(), 4);

        // 推送昵称
        mPushName = new ItemLineArrow.Builder(getActivity())
                .setStartContent(getString(R.string.push_name))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showPushNameDialog();
                    }
                });
        container.addView(mPushName.build(), 5);

        // 是否自动下载附件
        autoDownloadAttachment = new ItemLineSwitch.Builder(getActivity())
                .setLeftText(getString(R.string.auto_download_attachment))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {
                        setAutoDownloadAttachmentEnable(curCheck);
                    }
                });
        container.addView(autoDownloadAttachment.build(), 6);

        // 是否自动接收群邀请
        autoAcceptGroupInvite = new ItemLineSwitch.Builder(getActivity())
                .setLeftText(getString(R.string.auto_accept_group_invite))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {
                        setAutoAcceptGroupInviteEnable(curCheck);
                    }
                });
        container.addView(autoAcceptGroupInvite.build(), 7);

        // 黑名单列表
        mBlockList = new ItemLineArrow.Builder(getActivity())
                .setStartContent(getString(R.string.black_list))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        BlockListActivity.startBlockActivity(getActivity());
                    }
                });
        container.addView(mBlockList.build(), 8);

        // 是否多端提示
        otherDevTips = new ItemLineSwitch.Builder(getActivity())
                .setLeftText(getString(R.string.other_device_tips))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {
                        SharePreferenceUtils.getInstance().putDevTips(curCheck);
                    }
                });
        container.addView(otherDevTips.build(), 9);

        // 多设备列表
        mDeviceList = new ItemLineArrow.Builder(getActivity())
                .setStartContent(getString(R.string.device_list))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        DeviceListActivity.startDeviceActivity(getActivity());
                    }
                });
        container.addView(mDeviceList.build(), 10);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        final BMXUserProfile profile = new BMXUserProfile();
        Observable.just(profile).map(new Func1<BMXUserProfile, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(BMXUserProfile profile) {
                return UserManager.getInstance().getProfile(profile, true);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        initUser(profile);
                    }
                });
    }

    private void initUser(BMXUserProfile profile) {
        String name = profile.username();
        String nickName = profile.nickname();
        ChatUtils.getInstance().showProfileAvatar(profile, mUserIcon, mConfig);
        long userId = profile.userId();
        mUserName.setText(TextUtils.isEmpty(name) ? "" : name);
        mNickName.setText(TextUtils.isEmpty(nickName) ? "" : "昵称:" + nickName);
        mUserId.setText(userId <= 0 ? "" : "BMXID:" + userId);
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
        mQuitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        mUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingUserActivity.openSettingUser(getActivity());
            }
        });
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
        Observable.just(enable).map(new Func1<Boolean, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(Boolean aBoolean) {
                return UserManager.getInstance().setEnablePush(enable);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        toastError(e);
                        mSettingPush.setCheckStatus(!enable);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        if (enable) {
                            mPushSoundView.setVisibility(View.VISIBLE);
                            mPushVibrateView.setVisibility(View.VISIBLE);
                        } else {
                            mPushSoundView.setVisibility(View.GONE);
                            mPushVibrateView.setVisibility(View.GONE);
                        }
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
        Observable.just(enable).map(new Func1<Boolean, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(Boolean aBoolean) {
                return UserManager.getInstance().setEnablePushDetaile(enable);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        toastError(e);
                        mPushDetail.setCheckStatus(!enable);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
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
        Observable.just(enable).map(new Func1<Boolean, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(Boolean aBoolean) {
                return UserManager.getInstance().setNotificationSound(enable);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        toastError(e);
                        mPushSound.setCheckStatus(!enable);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
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
        Observable.just(enable).map(new Func1<Boolean, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(Boolean aBoolean) {
                return UserManager.getInstance().setNotificationVibrate(enable);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        toastError(e);
                        mPushVibrate.setCheckStatus(!enable);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
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
        Observable.just(enable).map(new Func1<Boolean, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(Boolean aBoolean) {
                return UserManager.getInstance().setAutoDownloadAttachment(enable);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        toastError(e);
                        autoDownloadAttachment.setCheckStatus(!enable);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
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
        Observable.just(enable).map(new Func1<Boolean, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(Boolean aBoolean) {
                return UserManager.getInstance().setAutoAcceptGroupInvite(enable);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        toastError(e);
                        autoAcceptGroupInvite.setCheckStatus(!enable);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                    }
                });
    }

    /**
     * 设置推送昵称
     * 
     * @param name
     */
    private void setPushName(final String name) {
        if (TextUtils.isEmpty(name)) {
            return;
        }
        showLoadingDialog(true);
        Observable.just(name).map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                return UserManager.getInstance().setPushNickname(name);
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        toastError(e);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        mPushName.setEndContent(name);
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
        Observable.just("").map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                return UserManager.getInstance().signOut();
            }
        }).flatMap(new Func1<BMXErrorCode, Observable<BMXErrorCode>>() {
            @Override
            public Observable<BMXErrorCode> call(BMXErrorCode errorCode) {
                return BaseManager.bmxFinish(errorCode, errorCode);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BMXErrorCode>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissLoadingDialog();
                        toastError(e);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        dismissLoadingDialog();
                        SharePreferenceUtils.getInstance().putLoginStatus(false);
                        PushClientMgr.getManager().unRegister();
                        WelcomeActivity.openWelcome(getActivity());
                    }
                });
    }

    private void toastError(Throwable e) {
        String error = e != null ? e.getMessage() : "网络异常";
        ToastUtil.showTextViewPrompt(error);
    }
}
