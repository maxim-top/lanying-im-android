
package top.maxim.im.group.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.RosterFetcher;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;
import top.maxim.im.net.HttpResponseCallback;

/**
 * Description : 群二维码详情 Created by Mango on 2018/11/21.
 */
public class GroupQrcodeDetailActivity extends BaseTitleActivity {

    private ShapeImageView mUserIcon;

    private TextView mUserName;

    private TextView mUserId;

    /* 开始聊天 */
    private TextView mTvJoin;

    private long mGroupId;

    private ImageRequestConfig mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
            .showImageForEmptyUri(R.drawable.default_group_icon)
            .showImageOnFail(R.drawable.default_group_icon).bitmapConfig(Bitmap.Config.RGB_565)
            .cacheOnDisk(true).showImageOnLoading(R.drawable.default_group_icon).build();

    public static void openGroupQrcodeDetail(Context context, long groupId) {
        Intent intent = new Intent(context, GroupQrcodeDetailActivity.class);
        intent.putExtra(MessageConfig.CHAT_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.group_qrcode);
        builder.setBackIcon(R.drawable.header_back_icon, v -> finish());
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.activity_group_qrcode_detail, null);
        mUserIcon = view.findViewById(R.id.iv_user_avatar);
        mUserName = view.findViewById(R.id.tv_user_name);
        mUserId = view.findViewById(R.id.tv_user_id);
        mTvJoin = view.findViewById(R.id.tv_confirm_join);
        return view;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mGroupId = intent.getLongExtra(MessageConfig.CHAT_ID, 0);
        }
    }

    @Override
    protected void setViewListener() {
        mTvJoin.setOnClickListener(v -> getToken());
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        initGroup();
    }

    private void initGroup() {
        mUserId.setText(mGroupId <= 0 ? "" : "群Id:" + mGroupId);
        showLoadingDialog(true);
        BMXGroup group = new BMXGroup();
        Observable.just(mGroupId).map(new Func1<Long, BMXErrorCode>() {
            @Override
            public BMXErrorCode call(Long aLong) {
                return GroupManager.getInstance().search(mGroupId, group, true);
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
                        GroupManager.getInstance().search(mGroupId, group, false);
                        bindGroup(group);
                    }

                    @Override
                    public void onNext(BMXErrorCode errorCode) {
                        dismissLoadingDialog();
                        bindGroup(group);
                        RosterFetcher.getFetcher().putGroup(group);
                    }
                });
    }

    private void bindGroup(BMXGroup group) {
        String name = group.name();
        ChatUtils.getInstance().showGroupAvatar(group, mUserIcon, mConfig);
        mUserName.setText(TextUtils.isEmpty(name) ? "" : name);
    }

    private void getToken() {
        showLoadingDialog(true);
        AppManager.getInstance().getTokenByName(SharePreferenceUtils.getInstance().getUserName(),
                SharePreferenceUtils.getInstance().getUserPwd(),
                new HttpResponseCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        getGroupSign(result);
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        dismissLoadingDialog();
                        ToastUtil.showTextViewPrompt("加入失败");
                    }
                });
    }

    private void getGroupSign(String token) {
        AppManager.getInstance().getGroupSign(mGroupId, token, new HttpResponseCallback<String>() {
            @Override
            public void onResponse(String result) {
                joinGroup(token, result);
            }

            @Override
            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                dismissLoadingDialog();
                ToastUtil.showTextViewPrompt("加入失败");
            }
        });
    }

    public void joinGroup(String token, String qrInfo) {
        AppManager.getInstance().groupInvite(token, qrInfo, new HttpResponseCallback<String>() {
            @Override
            public void onResponse(String result) {
                dismissLoadingDialog();
                ToastUtil.showTextViewPrompt("加入成功");
                finish();
            }

            @Override
            public void onFailure(int errorCode, String errorMsg, Throwable t) {
                dismissLoadingDialog();
                ToastUtil.showTextViewPrompt("加入失败");
            }
        });
    }

}
