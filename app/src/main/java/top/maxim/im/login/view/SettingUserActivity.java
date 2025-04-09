
package top.maxim.im.login.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import im.floo.BMXCallBack;
import im.floo.floolib.BMXUserProfile;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.base.PermissionActivity;
import top.maxim.im.common.provider.CommonProvider;
import top.maxim.im.common.utils.CameraUtils;
import top.maxim.im.common.utils.ClickTimeUtils;
import top.maxim.im.common.utils.CommonConfig;
import top.maxim.im.common.utils.FileConfig;
import top.maxim.im.common.utils.FileUtils;
import top.maxim.im.common.utils.RxBus;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonCustomDialog;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.CustomDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.utils.permissions.PermissionsConstant;
import top.maxim.im.common.utils.permissions.PermissionsMgr;
import top.maxim.im.common.utils.permissions.PermissionsResultAction;
import top.maxim.im.common.view.BMImageLoader;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ItemEnableArrow;
import top.maxim.im.common.view.ItemLine;
import top.maxim.im.common.view.ItemLineArrow;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.filebrowser.FileBrowserActivity;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.net.HttpResponseCallback;
import top.maxim.im.videocall.RTCConfigActivity;
import top.maxim.im.wxapi.WXUtils;

/**
 * Description : 用户设置 Created by Mango on 2018/11/21.
 */
public class SettingUserActivity extends BaseTitleActivity {

    private String TAG = "SettingUserActivity";

    private final int TYPE_PHOTO_PERMISSION = 2;

    private ActivityResultLauncher<String[]> permissionLauncher;

    private ShapeImageView mUserIcon;

    /* Id */
    private ItemLineArrow.Builder mUserId;

    /* 二维码 */
    private ItemLineArrow.Builder mQrCode;

    /* 设置昵称 */
    private ItemLineArrow.Builder mSetName;

    /* 设置手机号 */
    private ItemEnableArrow.Builder mSetPhone;

    /* 修改密码 */
    private ItemLineArrow.Builder mSetPwd;

    /* 绑定微信 */
    private ItemEnableArrow.Builder mBindWeChat;

    /* 设置公有信息 */
    private ItemLineArrow.Builder mSetPublic;

    /* 显示公共信息 */
    private TextView mTvPublic;

    private View mLinePublic;

    /* 设置私有信息 */
    private ItemLineArrow.Builder mSetPrivate;

    /* 显示私有信息 */
    private TextView mTvPrivate;

    private View mLinePrivate;

    /* 设置添加好友验证 */
    private ItemLineArrow.Builder mSetAddFriendAuthMode;

    /* 显示问题区域 */
    private LinearLayout mLlAuthQuestion;

    /* 设置添加好友问题 */
    private ItemLineArrow.Builder mSetAddFriendQuestion;

    /* 显示问题的view */
    private TextView mTvQuestion;

    /* 显示答案的view */
    private TextView mTvAnswer;

    /* 推送设置 */
    private ItemLineArrow.Builder mPushSet;

    /* RTC设置 */
    private ItemLineArrow.Builder mRTCConfig;

    /* 头像路径 */
    private String mIconPath;

    /* 绑定的手机号 */
    private String mPhone;

    private String mNickname;

    private CompositeSubscription mSubscription;

    private ImageRequestConfig mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
            .showImageForEmptyUri(R.drawable.default_avatar_icon)
            .showImageOnFail(R.drawable.default_avatar_icon).cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.default_avatar_icon)
            .build();

    /* 相册 */
    private final int IMAGE_REQUEST = 1000;

    /* 裁剪图片 */
    private final int IMAGE_CROP = 1001;

    public static void openSettingUser(Context context) {
        Intent intent = new Intent(context, SettingUserActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.user_info);
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
        initRxBus();
        View view = View.inflate(this, R.layout.activity_setting_user, null);
        LinearLayout container = view.findViewById(R.id.ll_setting_container);
        mUserIcon = view.findViewById(R.id.iv_user_avatar);

        // Id
        mUserId = new ItemLineArrow.Builder(this).setStartContent("ID")
                .setOnItemClickListener(v -> {
                    // 跳转查看日志
                    LogViewActivity.openLogView(this);
                });
        container.addView(mUserId.build());

        // 分割线
        addLineView(container);

        // 二维码
        mQrCode = new ItemLineArrow.Builder(this).setStartContent(getString(R.string.my_qrcode))
                .setOnItemClickListener(
                        v -> MyQrCodeActivity.openMyQrcode(SettingUserActivity.this));
        container.addView(mQrCode.build());

        // 分割线
        addLineView(container);

        // 推送昵称
        mSetName = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.setting_user_name))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSettingDialog(getString(R.string.setting_user_name), mNickname);
                    }
                });
        container.addView(mSetName.build());

        // 分割线
        addLineView(container);

        // 手机号
        mSetPhone = new ItemEnableArrow.Builder(this)
                .setStartContent(getString(R.string.setting_user_phone))
                .setOnItemClickListener(new ItemEnableArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showChangeMobile(mPhone);
                    }

                    @Override
                    public void onItemEnableClick(View v) {
                        BindMobileActivity.openBindMobile(SettingUserActivity.this);
                    }
                });
        container.addView(mSetPhone.build());

        // 分割线
        addLineView(container);

        // 修改密码
        mSetPwd = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.setting_user_pwd))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        if (TextUtils.isEmpty(mPhone)) {
                            // 手机号为空 未绑定手机 跳转绑定手机号
                            BindMobileActivity.openBindMobile(SettingUserActivity.this);
                            return;
                        }
                        showChangePwd(mPhone);
                    }
                });
        container.addView(mSetPwd.build());

        // 分割线
        addLineView(container);

        // 绑定微信
        mBindWeChat = new ItemEnableArrow.Builder(this)
                .setStartContent(getString(R.string.setting_user_wx))
                .setOnItemClickListener(new ItemEnableArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        // 解绑微信
                        VerifyActivity.startVerifyActivity(SettingUserActivity.this,
                                CommonConfig.VerifyType.TYPE_WX, "", 0);
                    }

                    @Override
                    public void onItemEnableClick(View v) {
                        // 绑定微信
                        WXUtils.getInstance().wxLogin(CommonConfig.SourceToWX.TYPE_BIND,
                                SharePreferenceUtils.getInstance().getAppId());
                    }
                });
        container.addView(mBindWeChat.build());

        // 分割线
        addLineView(container);

        // 公开扩展信息
        mSetPublic = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.setting_user_public))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSettingDialog(getString(R.string.setting_user_public));
                    }
                });
        container.addView(mSetPublic.build());

        // 分割线
        addLineView(container);

        mTvPublic = new TextView(this);
        LinearLayout.LayoutParams publicP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mTvPublic.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        mTvPublic.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        mTvPublic.setTextColor(getResources().getColor(R.color.color_black));
        mTvPublic.setBackgroundColor(getResources().getColor(R.color.color_white));
        mTvPublic.setLayoutParams(publicP);
        mTvPublic.setVisibility(View.GONE);
        container.addView(mTvPublic);

        // 分割线
        mLinePublic = addLineView(container);
        mLinePublic.setVisibility(View.GONE);

        // 私密扩展信息
        mSetPrivate = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.setting_user_private))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSettingDialog(getString(R.string.setting_user_private));
                    }
                });
        container.addView(mSetPrivate.build());

        // 分割线
        addLineView(container);

        mTvPrivate = new TextView(this);
        LinearLayout.LayoutParams privateP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mTvPrivate.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        mTvPrivate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        mTvPrivate.setTextColor(getResources().getColor(R.color.color_black));
        mTvPrivate.setBackgroundColor(getResources().getColor(R.color.color_white));
        mTvPrivate.setLayoutParams(privateP);
        mTvPrivate.setVisibility(View.GONE);
        container.addView(mTvPrivate);

        // 分割线
        mLinePrivate = addLineView(container);
        mLinePrivate.setVisibility(View.GONE);

        // 好友验证类型
        mSetAddFriendAuthMode = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.setting_add_friend_auth_mode))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSetAddFriendAuthMode();
                    }
                });
        container.addView(mSetAddFriendAuthMode.build());

        // 分割线
        addLineView(container);

        mLlAuthQuestion = new LinearLayout(this);
        mLlAuthQuestion.setOrientation(LinearLayout.VERTICAL);
        container.addView(mLlAuthQuestion, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        buildAuthQuestion();

        // 推送设置
        mPushSet = new ItemLineArrow.Builder(this).setStartContent(getString(R.string.set_push))
                .setOnItemClickListener(v -> PushSetActivity.openPushSet(this));
        container.addView(mPushSet.build());
        // 分割线
        addLineView(container);
        // RCT设置
        mRTCConfig = new ItemLineArrow.Builder(this).setStartContent(getString(R.string.config_rtc))
                .setOnItemClickListener(v -> RTCConfigActivity.openRTCConfig(this));
        container.addView(mRTCConfig.build());
        // 分割线
        addLineView(container);
        if (permissionLauncher == null){
            try {
                permissionLauncher = registerForActivityResult(
                        new ActivityResultContracts.RequestMultiplePermissions(),
                        new ActivityResultCallback<Map<String, Boolean>>() {
                            @Override
                            public void onActivityResult(Map<String, Boolean> grantResults) {
                            }
                        }
                );
                PermissionsMgr.getInstance().setPermissionLauncher(permissionLauncher);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return view;
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

    /**
     * 请求权限
     *
     * @param requestType 权限请求类型
     * @param permissions 权限列表
     * @return 是否具有所有权限
     */
    void requestPermissions(final int requestType, final String... permissions) {
       PermissionsMgr.getInstance().requestPermissionsIfNecessaryForResult(
                this, permissions, new PermissionsResultAction() {

                    @Override
                    public void onGranted(List<String> perms) {
                        PermissionsMgr.getInstance().permissionProcessed();
                        Log.d(TAG, "Permission is Granted:" + perms);
                        onGrantedPermission(requestType, perms);
                    }

                    @Override
                    public void onDenied(List<String> perms) {
                        PermissionsMgr.getInstance().permissionProcessed();
                        Log.d(TAG, "Permission is Denied" + perms);
                        onDeniedPermission(requestType, perms);
                    }
                });
    }

    /**
     * 权限接受
     *
     * @param requestType 请求权限类型
     * @param permissions 权限接受的列表
     */
    private void onGrantedPermission(int requestType, List<String> permissions) {
        if (permissions == null || permissions.size() == 0) {
            return;
        }
        for (String permission : permissions) {
            switch (permission) {
                case PermissionsConstant.READ_STORAGE:
                    // 读SD权限
                    if (hasPermission(PermissionsConstant.WRITE_STORAGE)) {
                        // 如果有读写权限都有 则直接操作
                        hasPermissionHandler(requestType);
                    } else {
                        requestPermissions(requestType, PermissionsConstant.WRITE_STORAGE);
                    }
                    break;
               default:
                    break;
            }
        }
    }
    /**
     * 获取到权限之后的操作
     *
     * @param requestType 请求的权限类型
     */
    private void hasPermissionHandler(int requestType) {
        switch (requestType) {
            case TYPE_PHOTO_PERMISSION:
                // 相册
                CameraUtils.getInstance().takeGalley(this, IMAGE_REQUEST);
                break;
            default:
                break;
        }
    }

    /**
     * 权限拒绝
     *
     * @param requestType 请求权限类型
     * @param permissions 被拒绝的权限
     */
    private void onDeniedPermission(int requestType, List<String> permissions) {
        if (permissions == null || permissions.size() == 0) {
            return;
        }
        for (String permission : permissions) {
            switch (permission) {
                case PermissionsConstant.READ_STORAGE:
                    // 读写SD权限拒绝
                    CommonProvider.openAppPermission(this);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void setViewListener() {
        mUserIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 选择相册 需要SD卡读写权限
                if (hasPermission(PermissionsConstant.READ_STORAGE)) {
                    CameraUtils.getInstance().takeGalley(SettingUserActivity.this, IMAGE_REQUEST);
                } else {
                    requestPermissions(TYPE_PHOTO_PERMISSION, PermissionsConstant.READ_STORAGE);
                }
            }
        });
        // 10次点击 进入应用data目录
        ClickTimeUtils.setClickTimes(mHeader.getTitleText(), 5, () -> {
            ToastUtil.showTextViewPrompt("点击5次");
            startActivity(new Intent(this, FileBrowserActivity.class));
        });
    }

    private void initRxBus() {
        if (mSubscription == null) {
            mSubscription = new CompositeSubscription();
        }
        Subscription wxLogin = RxBus.getInstance().toObservable(Intent.class)
                .subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Intent>() {
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
                        if (TextUtils.equals(action, CommonConfig.WX_LOGIN_ACTION)) {
                            // 绑定微信
                            String openId = intent.getStringExtra(CommonConfig.WX_OPEN_ID);
                            checkWeChat(openId);
                        } else if (TextUtils.equals(action, CommonConfig.WX_UN_BIND_ACTION)) {
                            // 解绑微信
                            showBindWeChat();
                        } else if (TextUtils.equals(action, CommonConfig.MOBILE_BIND_ACTION)) {
                            // 绑定手机号
                            initData(true);
                        }
                    }
                });
        mSubscription.add(wxLogin);
    }

    /**
     * 检查微信是否被绑定
     * 
     * @param openId
     */
    private void checkWeChat(String openId) {
        if (TextUtils.isEmpty(openId)) {
            return;
        }
        AppManager.getInstance().weChatLogin(openId, new HttpResponseCallback<String>() {
            @Override
            public void onResponse(String result) {
                if (TextUtils.isEmpty(result)) {
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (!jsonObject.has("user_id") || !jsonObject.has("password")) {
                        bindWeChat(jsonObject.getString("openid"));
                        return;
                    }
                    // 已被绑定
                    ToastUtil.showTextViewPrompt(getString(R.string.wechat_already_bound));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                ToastUtil.showTextViewPrompt(getString(R.string.bind_failed));
            }
        });
    }

    /**
     * 绑定微信
     */
    private void bindWeChat(String openId) {
        if (TextUtils.isEmpty(openId)) {
            return;
        }
        showLoadingDialog(true);
        AppManager.getInstance().getTokenByName(SharePreferenceUtils.getInstance().getUserName(),
                SharePreferenceUtils.getInstance().getUserPwd(),
                new HttpResponseCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        AppManager.getInstance().bindOpenId(result, openId,
                                new HttpResponseCallback<Boolean>() {
                                    @Override
                                    public void onResponse(Boolean result) {
                                        dismissLoadingDialog();
                                        showBindWeChat();
                                    }

                                    @Override
                                    public void onFailure(int errorCode, String errorMsg,
                                            Throwable t) {
                                        dismissLoadingDialog();
                                        ToastUtil.showTextViewPrompt(errorMsg);
                                    }
                                });
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        dismissLoadingDialog();
                    }
                });
    }

    /**
     * 好友验证问题view
     */
    private void buildAuthQuestion() {
        // 好友验证问题
        mSetAddFriendQuestion = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.setting_add_friend_auth_question))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSetAddFriendQuestion();
                    }
                });
        mLlAuthQuestion.addView(mSetAddFriendQuestion.build());

        // 分割线
        addLineView(mLlAuthQuestion);

        // 问题
        mTvQuestion = new TextView(this);
        LinearLayout.LayoutParams questionP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mTvQuestion.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        mTvQuestion.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        mTvQuestion.setTextColor(getResources().getColor(R.color.color_black));
        mTvQuestion.setBackgroundColor(getResources().getColor(R.color.color_white));
        mTvQuestion.setLayoutParams(questionP);
        mLlAuthQuestion.addView(mTvQuestion);

        // 分割线
        addLineView(mLlAuthQuestion);

        // 答案
        mTvAnswer = new TextView(this);
        LinearLayout.LayoutParams answerP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mTvAnswer.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        mTvAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        mTvAnswer.setTextColor(getResources().getColor(R.color.color_black));
        mTvAnswer.setBackgroundColor(getResources().getColor(R.color.color_white));
        mTvAnswer.setLayoutParams(answerP);
        mLlAuthQuestion.addView(mTvAnswer);
    }
    
    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        initData(false);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                initData(true);
            }
        }, 200);
    }

    private void initData(boolean forceRefresh) {
        UserManager.getInstance().getProfile(forceRefresh, (bmxErrorCode, bmxUserProfile) -> {
            if (BaseManager.bmxFinish(bmxErrorCode) && bmxUserProfile != null) {
                initUser(bmxUserProfile);
            }
        });
    }

    private void initUser(BMXUserProfile profile) {
        long id = profile.userId();
        mNickname = profile.nickname();
        ChatUtils.getInstance().showProfileAvatar(profile, mUserIcon, mConfig);
        mUserId.setEndContent(String.valueOf(id));
        mSetName.setEndContent(TextUtils.isEmpty(mNickname) ? "" : mNickname);
        showBindPhone(profile.mobilePhone());
        String publicInfo = profile.publicInfo();
        if (TextUtils.isEmpty(publicInfo)) {
            mTvPublic.setVisibility(View.GONE);
            mLinePublic.setVisibility(View.GONE);
        } else {
            mTvPublic.setVisibility(View.VISIBLE);
            mLinePublic.setVisibility(View.VISIBLE);
            mTvPublic.setText(publicInfo);
        }
        String privateInfo = profile.privateInfo();
        if (TextUtils.isEmpty(privateInfo)) {
            mTvPrivate.setVisibility(View.GONE);
            mLinePrivate.setVisibility(View.GONE);
        } else {
            mTvPrivate.setVisibility(View.VISIBLE);
            mLinePrivate.setVisibility(View.VISIBLE);
            mTvPrivate.setText(privateInfo);
        }

        BMXUserProfile.AddFriendAuthMode mode = profile.addFriendAuthMode();
        bindAddFriendAuth("", mode);
        bindAddFriendAuthQuestion(profile.authQuestion());
        showBindWeChat();
    }

    private void showBindPhone(String phone) {
        mPhone = phone;
        if (TextUtils.isEmpty(phone)) {
            // 未绑定
            mSetPhone.setEnableContent(getString(R.string.go_to_bind));
        } else {
            mSetPhone.setEndContent(phone);
        }
    }

    private void showBindWeChat() {
        String name = SharePreferenceUtils.getInstance().getUserName();
        String pwd = SharePreferenceUtils.getInstance().getUserPwd();
        AppManager.getInstance().getTokenByName(name, pwd, new HttpResponseCallback<String>() {
            @Override
            public void onResponse(String result) {
                AppManager.getInstance().isBind(result, new HttpResponseCallback<Boolean>() {
                    @Override
                    public void onResponse(Boolean result) {
                        if (result != null && result) {
                            // 已绑定
                            mBindWeChat.setEndContent(getString(R.string.bound));
                        } else {
                            mBindWeChat.setEnableContent(getString(R.string.go_to_bind));
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        mBindWeChat.setEnableContent(getString(R.string.go_to_bind));
                    }
                });
            }

            @Override
            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                mBindWeChat.setEndContent(getString(R.string.unbound));
            }
        });
    }

    private void bindAddFriendAuth(String authMode, BMXUserProfile.AddFriendAuthMode mode) {
        if (TextUtils.isEmpty(authMode) && mode == null) {
            return;
        }
        mSetAddFriendAuthMode.setEndContent(
                !TextUtils.isEmpty(authMode) ? authMode : getAddFriendAuthContent(mode));
        if (TextUtils.equals(authMode, getString(R.string.add_friend_auth_answer))
                || mode == BMXUserProfile.AddFriendAuthMode.AnswerQuestion) {
            mLlAuthQuestion.setVisibility(View.VISIBLE);
        } else {
            mLlAuthQuestion.setVisibility(View.GONE);
        }
    }

    private void bindAddFriendAuthQuestion(BMXUserProfile.AuthQuestion authQuestion) {
        String question = authQuestion != null ? authQuestion.getMQuestion() : "";
        String answer = authQuestion != null ? authQuestion.getMAnswer() : "";
        mTvQuestion.setText(getString(R.string.question) + question);
        mTvAnswer.setText(getString(R.string.answer) + answer);
    }

    @Override
    public void onPermissionGranted(List<String> permissions) {
        super.onPermissionGranted(permissions);
        if (permissions == null || permissions.size() == 0) {
            return;
        }
        for (String permission : permissions) {
            switch (permission) {
                case PermissionsConstant.READ_STORAGE:
                    // 读SD权限
                    if (hasPermission(PermissionsConstant.WRITE_STORAGE)) {
                        // 如果有读写权限都有 则直接操作
                        CameraUtils.getInstance().takeGalley(SettingUserActivity.this,
                                IMAGE_REQUEST);
                    } else {
                        requestPermissions(PermissionsConstant.WRITE_STORAGE);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onPermissionDenied(List<String> permissions) {
        super.onPermissionDenied(permissions);
        if (permissions == null || permissions.size() == 0) {
            return;
        }
        for (String permission : permissions) {
            switch (permission) {
                case PermissionsConstant.READ_STORAGE:
                case PermissionsConstant.WRITE_STORAGE:
                    // 读写SD权限拒绝
                    CommonProvider.openAppPermission(this);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 输入框弹出
     */
    private void showSettingDialog(final String title) {
        DialogUtils.getInstance().showEditDialog(this, title, getString(R.string.confirm),
                getString(R.string.cancel), new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        setUserInfo(title, content);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    private void showSettingDialog(final String title, final String content) {
        DialogUtils.getInstance().showEditDialog(this, title, content, getString(R.string.confirm),
                getString(R.string.cancel), new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        setUserInfo(title, content);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 设置好友验证类型
     */
    private void showSetAddFriendAuthMode() {
        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        String[] array = new String[] {
                getString(R.string.add_friend_auth_open),
                getString(R.string.add_friend_auth_approval),
                getString(R.string.add_friend_auth_answer),
                getString(R.string.add_friend_auth_reject)
        };
        final String[] selectContent = new String[1];
        for (final String s : array) {
            final TextView tv = new TextView(this);
            tv.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                    ScreenUtils.dp2px(15));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            tv.setTextColor(getResources().getColor(R.color.color_black));
            tv.setBackgroundColor(getResources().getColor(R.color.color_white));
            tv.setText(s);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectContent[0] = s;
                    for (int i = 0; i < ll.getChildCount(); i++) {
                        TextView select = (TextView)ll.getChildAt(i);
                        if (TextUtils.equals(select.getText().toString(), s)) {
                            select.setTextColor(Color.RED);
                        } else {
                            select.setTextColor(getResources().getColor(R.color.color_black));
                        }
                    }
                }
            });
            ll.addView(tv, params);
        }
        DialogUtils.getInstance().showCustomDialog(this, ll,
                getString(R.string.setting_add_friend_auth_mode), getString(R.string.confirm),
                getString(R.string.cancel), new CommonCustomDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener() {
                        setUserInfo(getString(R.string.setting_add_friend_auth_mode),
                                selectContent[0]);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 设置好友验证问题
     */
    private void showSetAddFriendQuestion() {
        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams editP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editP.setMargins(ScreenUtils.dp2px(10), ScreenUtils.dp2px(5), ScreenUtils.dp2px(10),
                ScreenUtils.dp2px(5));
        // 问题
        TextView question = new TextView(this);
        question.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        question.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        question.setTextColor(getResources().getColor(R.color.color_black));
        question.setBackgroundColor(getResources().getColor(R.color.color_white));
        question.setText(getString(R.string.set_question));
        ll.addView(question, textP);

        final EditText editQuestion = new EditText(this);
        editQuestion.setBackgroundResource(R.drawable.common_edit_corner_bg);
        editQuestion.setPadding(ScreenUtils.dp2px(5), 0, ScreenUtils.dp2px(5), 0);
        editQuestion.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        editQuestion.setTextColor(getResources().getColor(R.color.color_black));
        editQuestion.setMinHeight(ScreenUtils.dp2px(40));
        ll.addView(editQuestion, editP);

        // 答案
        TextView answer = new TextView(this);
        answer.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        answer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        answer.setTextColor(getResources().getColor(R.color.color_black));
        answer.setBackgroundColor(getResources().getColor(R.color.color_white));
        answer.setText(getString(R.string.set_answer));
        ll.addView(answer, textP);

        final EditText editAnswer = new EditText(this);
        editAnswer.setBackgroundResource(R.drawable.common_edit_corner_bg);
        editAnswer.setPadding(ScreenUtils.dp2px(5), 0, ScreenUtils.dp2px(5), 0);
        editAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        editAnswer.setTextColor(getResources().getColor(R.color.color_black));
        editAnswer.setMinHeight(ScreenUtils.dp2px(40));
        ll.addView(editAnswer, editP);

        DialogUtils.getInstance().showCustomDialog(this, ll,
                getString(R.string.setting_add_friend_auth_mode), getString(R.string.confirm),
                getString(R.string.cancel), new CommonCustomDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener() {
                        String question = editQuestion.getEditableText().toString().trim();
                        String answer = editAnswer.getEditableText().toString().trim();
                        setAuthQuestion(question, answer);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 设置验证问题
     * 
     * @param question 问题
     * @param answer 答案
     */
    private void setAuthQuestion(final String question, final String answer) {
        if (TextUtils.isEmpty(question) || TextUtils.isEmpty(answer)) {
            ToastUtil.showTextViewPrompt(getString(R.string.question_or_answer_cannot_be_empty));
            return;
        }
        showLoadingDialog(true);
        final BMXUserProfile.AuthQuestion authQuestion = new BMXUserProfile.AuthQuestion();
        authQuestion.setMQuestion(question);
        authQuestion.setMAnswer(answer);
        UserManager.getInstance().setAuthQuestion(authQuestion, bmxErrorCode -> {
            dismissLoadingDialog();
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                bindAddFriendAuthQuestion(authQuestion);
            } else {
                String error = bmxErrorCode != null ? bmxErrorCode.name() : getString(R.string.network_exception);
                ToastUtil.showTextViewPrompt(error);
            }
        });
    }

    /**
     * 更新信息
     */
    private void setUserInfo(final String title, final String info) {
        if (TextUtils.equals(title, getString(R.string.setting_user_phone))
                || TextUtils.equals(title, getString(R.string.setting_add_friend_auth_mode))) {
            // 设置手机号 好友验证类型时候 content不能为空
            if (TextUtils.isEmpty(info)) {
                return;
            }
        }
        showLoadingDialog(true);
        BMXCallBack callBack = bmxErrorCode -> {
            dismissLoadingDialog();
            if (!BaseManager.bmxFinish(bmxErrorCode)) {
                String error = bmxErrorCode != null ? bmxErrorCode.name() : getString(R.string.network_exception);
                ToastUtil.showTextViewPrompt(error);
                return;
            }
            if (TextUtils.equals(title, getString(R.string.setting_user_name))) {
                // 设置昵称
                mSetName.setEndContent(TextUtils.isEmpty(info) ? "" : info);
                mNickname = info;
            } else if (TextUtils.equals(title, getString(R.string.setting_user_public))) {
                // 设置公有信息
                boolean hide = TextUtils.isEmpty(info);
                mTvPublic.setVisibility(hide ? View.GONE : View.VISIBLE);
                mLinePublic.setVisibility(hide ? View.GONE : View.VISIBLE);
                mTvPublic.setText(TextUtils.isEmpty(info) ? "" : info);
            } else if (TextUtils.equals(title, getString(R.string.setting_user_private))) {
                // 设置私密信息
                boolean hide = TextUtils.isEmpty(info);
                mTvPrivate.setVisibility(hide ? View.GONE : View.VISIBLE);
                mLinePrivate.setVisibility(hide ? View.GONE : View.VISIBLE);
                mTvPrivate.setText(TextUtils.isEmpty(info) ? "" : info);
            } else if (TextUtils.equals(title, getString(R.string.setting_add_friend_auth_mode))) {
                // 设置好友验证类型
                bindAddFriendAuth(info, null);
            }
        };
        if (TextUtils.equals(title, getString(R.string.setting_user_name))) {
            // 设置昵称
            UserManager.getInstance().setNickname(info, callBack);
        } else if (TextUtils.equals(title, getString(R.string.setting_user_public))) {
            // 设置公有信息
            UserManager.getInstance().setPublicInfo(info, callBack);
        } else if (TextUtils.equals(title, getString(R.string.setting_user_private))) {
            // 设置私密信息
            UserManager.getInstance().setPrivateInfo(info, callBack);
        } else if (TextUtils.equals(title, getString(R.string.setting_add_friend_auth_mode))) {
            // 设置好友验证类型
            BMXUserProfile.AddFriendAuthMode mode = null;
            if (TextUtils.equals(info, getString(R.string.add_friend_auth_open))) {
                mode = BMXUserProfile.AddFriendAuthMode.Open;
            } else if (TextUtils.equals(info, getString(R.string.add_friend_auth_approval))) {
                mode = BMXUserProfile.AddFriendAuthMode.NeedApproval;
            } else if (TextUtils.equals(info, getString(R.string.add_friend_auth_answer))) {
                mode = BMXUserProfile.AddFriendAuthMode.AnswerQuestion;
            } else if (TextUtils.equals(info, getString(R.string.add_friend_auth_reject))) {
                mode = BMXUserProfile.AddFriendAuthMode.RejectAll;
            }
            if (mode != null) {
                UserManager.getInstance().setAddFriendAuthMode(mode, callBack);
            }
        }
    }

    private String getAddFriendAuthContent(BMXUserProfile.AddFriendAuthMode mode) {
        if (mode == null) {
            return "";
        }
        if (mode == BMXUserProfile.AddFriendAuthMode.Open) {
            return getString(R.string.add_friend_auth_open);
        }
        if (mode == BMXUserProfile.AddFriendAuthMode.NeedApproval) {
            return getString(R.string.add_friend_auth_approval);
        }
        if (mode == BMXUserProfile.AddFriendAuthMode.AnswerQuestion) {
            return getString(R.string.add_friend_auth_answer);
        }
        if (mode == BMXUserProfile.AddFriendAuthMode.RejectAll) {
            return getString(R.string.add_friend_auth_reject);
        }
        return "";
    }

    /**
     * 展示更换手机号
     */
    private void showChangeMobile(final String phone) {
        View view = View.inflate(this, R.layout.dialog_change_phone, null);
        TextView tvPhone = view.findViewById(R.id.tv_bind_phone_title);
        tvPhone.setText(String.format(getString(R.string.bind_phone_tag), phone));
        TextView tvPhoneVerify = view.findViewById(R.id.tv_phone_verify);
        TextView tvPwdVerify = view.findViewById(R.id.tv_pwd_verify);
        CustomDialog dialog = new CustomDialog();
        tvPhoneVerify.setOnClickListener(v -> {
            // 旧手机号验证
            VerifyActivity.startVerifyActivity(this, CommonConfig.VerifyType.TYPE_PHONE_CAPTCHA,
                    phone, CommonConfig.VerifyOperateType.TYPE_BIND_MOBILE);
            dialog.dismiss();
        });
        tvPwdVerify.setOnClickListener(v -> {
            VerifyActivity.startVerifyActivity(this, CommonConfig.VerifyType.TYPE_PWD, "",
                    CommonConfig.VerifyOperateType.TYPE_BIND_MOBILE);
            dialog.dismiss();
        });
        dialog.setCustomView(view);
        dialog.showDialog(this);
    }

    /**
     * 展示修改手机
     */
    private void showChangePwd(final String phone) {
        View view = View.inflate(this, R.layout.dialog_change_phone, null);
        TextView tvTitle = view.findViewById(R.id.tv_change_phone_title);
        TextView tvPhone = view.findViewById(R.id.tv_bind_phone_title);
        tvPhone.setText(String.format(getString(R.string.bind_phone_tag), phone));
        TextView tvPhoneVerify = view.findViewById(R.id.tv_phone_verify);
        TextView tvPwdVerify = view.findViewById(R.id.tv_pwd_verify);
        tvTitle.setText(R.string.change_pwd_tag);
        CustomDialog dialog = new CustomDialog();
        tvPhoneVerify.setOnClickListener(v -> {
            // 旧手机号验证
            VerifyActivity.startVerifyActivity(this, CommonConfig.VerifyType.TYPE_PHONE_CAPTCHA,
                    phone, CommonConfig.VerifyOperateType.TYPE_CHANGE_PWD);
            dialog.dismiss();
        });
        tvPwdVerify.setOnClickListener(v -> {
            VerifyActivity.startVerifyActivity(this, CommonConfig.VerifyType.TYPE_PWD, "",
                    CommonConfig.VerifyOperateType.TYPE_CHANGE_PWD);
            dialog.dismiss();
        });
        dialog.setCustomView(view);
        dialog.showDialog(this);
    }
    
    /**
     * 上传头像
     * 
     * @param path 路径
     */
    private void uploadUserAvatar(String path) {
        if (TextUtils.isEmpty(path) || !new File(path).exists()) {
            return;
        }
        BMImageLoader.getInstance().display(mUserIcon, "file://" + path, mConfig);
        showLoadingDialog(true);
        UserManager.getInstance().uploadAvatar(path, s -> {
            Log.i("uploadUserAvatar", "onProgressChange:" + path + "-" + s);
            return 0;
        }, bmxErrorCode -> {
            dismissLoadingDialog();
            mIconPath = "";
            if (!BaseManager.bmxFinish(bmxErrorCode)) {
                String error = bmxErrorCode != null ? bmxErrorCode.name() : getString(R.string.network_exception);
                ToastUtil.showTextViewPrompt(error);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IMAGE_REQUEST:
                // 相册
                if (resultCode == Activity.RESULT_OK && data != null) {
                    try {
                        Uri selectedImage = data.getData(); // 获取系统返回的照片的Uri
                        String path = FileUtils.getFilePathByUri(selectedImage);
                        if (TextUtils.isEmpty(path)) {
                            return;
                        }
                        if (!FileUtils.checkSDCard()) {
                            ToastUtil.showTextViewPrompt("SD 不存在！");
                        }
                        File imageFileDir = new File(FileConfig.DIR_APP_CACHE_CAMERA);
                        if (!imageFileDir.exists()) {
                            imageFileDir.mkdirs();
                        }
                        mIconPath = FileConfig.DIR_APP_CACHE_CAMERA + "/"
                                + System.currentTimeMillis() + "icon" + ".jpg";
                        Uri photoURI;
                        File imageFile = new File(mIconPath);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            // 第二参数是在manifest.xml定义 provider的authorities属性
                            photoURI = FileProvider.getUriForFile(this,
                                    this.getPackageName() + ".fileProvider", imageFile);
                        } else {
                            photoURI = Uri.fromFile(imageFile);
                        }

                        CameraUtils.getInstance().startPhotoZoom(new File(path), selectedImage,
                                photoURI, FileConfig.DIR_APP_CACHE_CAMERA,
                                600, 600, this, IMAGE_CROP);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case IMAGE_CROP:
                // 裁剪图片
                if (!TextUtils.isEmpty(mIconPath)) {
                    uploadUserAvatar(mIconPath);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }
}
