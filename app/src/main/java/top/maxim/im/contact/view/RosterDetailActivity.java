
package top.maxim.im.contact.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXMessage;
import im.floo.floolib.BMXRosterItem;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.RosterManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.utils.dialog.CommonEditDialog;
import top.maxim.im.common.utils.dialog.DialogUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ItemLine;
import top.maxim.im.common.view.ItemLineArrow;
import top.maxim.im.common.view.ItemLineSwitch;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;
import top.maxim.im.message.view.ChatSingleActivity;

/**
 * Description : 详情 Created by Mango on 2018/11/21.
 */
public class RosterDetailActivity extends BaseTitleActivity {

    private ShapeImageView mUserIcon;

    private TextView mUserName;

    private TextView mUserId;

    private TextView mNickName;

    /* 公有信息 */
    private ItemLineArrow.Builder mShowPublic;

    /* 显示公共信息 */
    private TextView mTvPublic;

    /* 设置别名 */
    private ItemLineArrow.Builder mSetAlias;

    /* 设置扩展信息 */
    private ItemLineArrow.Builder mSetExt;

    /* 设置免打扰 */
    private ItemLineSwitch.Builder mSetDistrub;

    /* 显示扩展信息 */
    private TextView mTvExt;

    /* 开始聊天 */
    private TextView mTvOpenChat;

    private long mRosterId;

    private BMXRosterItem mRosterItem = new BMXRosterItem();

    private ImageRequestConfig mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
            .showImageForEmptyUri(R.drawable.default_avatar_icon)
            .showImageOnFail(R.drawable.default_avatar_icon)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .cacheOnDisk(true)
            .showImageOnLoading(R.drawable.default_avatar_icon).build();

    public static void openRosterDetail(Context context, long rosterId) {
        Intent intent = new Intent(context, RosterDetailActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, rosterId);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.profile);
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
        View view = View.inflate(this, R.layout.activity_setting_user, null);
        LinearLayout container = view.findViewById(R.id.ll_setting_container);
        mUserIcon = view.findViewById(R.id.iv_user_avatar);
        mUserName = view.findViewById(R.id.tv_user_name);
        mNickName = view.findViewById(R.id.tv_nick_name);
        mUserId = view.findViewById(R.id.tv_user_id);
        // 公开扩展信息
        mShowPublic = new ItemLineArrow.Builder(this).setMarginTop(ScreenUtils.dp2px(10))
                .setStartContent(getString(R.string.profile_public));
        container.addView(mShowPublic.build());

        // 分割线
        ItemLine.Builder itemLine0 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine0.build());

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
        ItemLine.Builder itemLine1 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine1.build());

        // 设置别名
        mSetAlias = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.setting_roster_alias))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSetRosterDialog(getString(R.string.setting_roster_alias));
                    }
                });
        container.addView(mSetAlias.build());

        // 分割线
        ItemLine.Builder itemLine2 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine2.build());

        // 设置扩展信息
        mSetExt = new ItemLineArrow.Builder(this)
                .setStartContent(getString(R.string.setting_roster_ext))
                .setOnItemClickListener(new ItemLineArrow.OnItemArrowViewClickListener() {
                    @Override
                    public void onItemClick(View v) {
                        showSetRosterDialog(getString(R.string.setting_roster_ext));
                    }
                });
        container.addView(mSetExt.build());

        mTvExt = new TextView(this);
        mTvExt.setPadding(ScreenUtils.dp2px(15), ScreenUtils.dp2px(15), ScreenUtils.dp2px(15),
                ScreenUtils.dp2px(15));
        mTvExt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        mTvExt.setTextColor(getResources().getColor(R.color.color_black));
        mTvExt.setBackgroundColor(getResources().getColor(R.color.color_white));
        mTvExt.setLayoutParams(publicP);
        mTvExt.setVisibility(View.GONE);
        container.addView(mTvExt);

        // 分割线
        ItemLine.Builder itemLine3 = new ItemLine.Builder(this, container)
                .setMarginLeft(ScreenUtils.dp2px(15));
        container.addView(itemLine3.build());

        // 设置免打扰
        mSetDistrub = new ItemLineSwitch.Builder(this)
                .setLeftText(getString(R.string.setting_user_disturb))
                .setOnItemSwitchListener(new ItemLineSwitch.OnItemViewSwitchListener() {
                    @Override
                    public void onItemSwitch(View v, boolean curCheck) {
                        setMuteEnable(curCheck);
                    }
                });
        container.addView(mSetDistrub.build());

        // 开始聊天
        mTvOpenChat = new TextView(this);
        mTvOpenChat.setTextColor(getResources().getColor(R.color.color_black));
        mTvOpenChat.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        mTvOpenChat.setGravity(Gravity.CENTER);
        mTvOpenChat.setText(getString(R.string.begin_chat));
        mTvOpenChat.setBackgroundResource(R.drawable.common_yellow_btn_corner_bg);
        mTvOpenChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatSingleActivity.startChatActivity(RosterDetailActivity.this,
                        BMXMessage.MessageType.Single, mRosterId);
                finish();
            }
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dp2px(44));
        params.topMargin = ScreenUtils.dp2px(30);
        params.bottomMargin = ScreenUtils.dp2px(5);
        params.leftMargin = ScreenUtils.dp2px(15);
        params.rightMargin = ScreenUtils.dp2px(15);
        mTvOpenChat.setLayoutParams(params);
        container.addView(mTvOpenChat);
        return view;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mRosterId = intent.getLongExtra(MessageConfig.CHAT_ID, 0);
        }
    }

    @Override
    protected void setViewListener() {
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        showLoadingDialog(true);
        Observable.just(mRosterId).map(new Func1<Long, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(Long aLong) {
                return RosterManager.getInstance().search(mRosterId, true, mRosterItem);
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
                        RosterManager.getInstance().search(mRosterId, false, mRosterItem);
                        bindRoster();
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        bindRoster();
                        RosterFetcher.getFetcher().putRoster(mRosterItem);
                    }
                });
    }

    private void bindRoster() {
        String name = mRosterItem.username();
        String nickName = mRosterItem.nickname();
        ChatUtils.getInstance().showRosterAvatar(mRosterItem, mUserIcon, mConfig);
        long userId = mRosterItem.rosterId();
        mUserName.setText(TextUtils.isEmpty(name) ? "" : name);
        mNickName.setText(TextUtils.isEmpty(nickName) ? "" : "昵称:" + nickName);
        mUserId.setText(userId <= 0 ? "" : "BMXID:" + userId);
        String publicInfo = mRosterItem.publicInfo();
        if (TextUtils.isEmpty(publicInfo)) {
            mTvPublic.setVisibility(View.GONE);
        } else {
            mTvPublic.setVisibility(View.VISIBLE);
            mTvPublic.setText(publicInfo);
        }

        String alias = mRosterItem.alias();
        mSetAlias.setEndContent(TextUtils.isEmpty(alias) ? "" : alias);

        String ext = mRosterItem.ext();
        if (TextUtils.isEmpty(publicInfo)) {
            mTvExt.setVisibility(View.GONE);
        } else {
            mTvExt.setVisibility(View.VISIBLE);
            mTvExt.setText(ext);
        }
        // 免打扰
        boolean isBlock = mRosterItem.isMuteNotification();
        mSetDistrub.setCheckStatus(isBlock);
    }

    /**
     * 设置弹出框
     */
    private void showSetRosterDialog(final String title) {
        DialogUtils.getInstance().showEditDialog(this, title, getString(R.string.confirm),
                getString(R.string.cancel), new CommonEditDialog.OnDialogListener() {
                    @Override
                    public void onConfirmListener(String content) {
                        setRosterInfo(TextUtils.isEmpty(content) ? "" : content, title);
                    }

                    @Override
                    public void onCancelListener() {

                    }
                });
    }

    /**
     * 设置roster信息
     *
     * @param info
     */
    private void setRosterInfo(final String info, final String title) {
        showLoadingDialog(true);
        Observable.just(info).map(new Func1<String, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(String s) {
                if (TextUtils.equals(title, getString(R.string.setting_roster_alias))) {
                    return RosterManager.getInstance().setItemAlias(mRosterItem, info);
                }
                if (TextUtils.equals(title, getString(R.string.setting_roster_ext))) {
                    return RosterManager.getInstance().setItemExtension(mRosterItem, info);
                }
                return null;
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
                        String error = e == null ? "" : e.getMessage();
                        ToastUtil.showTextViewPrompt(error);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        if (TextUtils.equals(title, getString(R.string.setting_roster_alias))) {
                            mSetAlias.setEndContent(info);
                        } else if (TextUtils.equals(title,
                                getString(R.string.setting_roster_ext))) {
                            mTvExt.setVisibility(View.VISIBLE);
                            mTvExt.setText(info);
                        }
                    }
                });
    }

    /**
     * 设置免打扰
     *
     * @param enable
     */
    private void setMuteEnable(final boolean enable) {
        showLoadingDialog(true);
        Observable.just(enable).map(new Func1<Boolean, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(Boolean aBoolean) {
                return RosterManager.getInstance().setItemMuteNotification(mRosterItem, enable);
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
                        String error = e == null ? "" : e.getMessage();
                        ToastUtil.showTextViewPrompt(error);
                        mSetDistrub.setCheckStatus(!enable);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        RosterFetcher.getFetcher().putRoster(mRosterItem);
                    }
                });
    }

}
