
package top.maxim.im.group.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import top.maxim.im.R;
import top.maxim.im.bmxmanager.AppManager;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.utils.ToastUtil;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.message.utils.MessageConfig;
import top.maxim.im.net.HttpResponseCallback;
import top.maxim.im.scan.utils.QRCodeShowUtils;

/**
 * Description : 群二维码 Created by Mango on 2019-06-26.
 */
public class GroupQrCodeActivity extends BaseTitleActivity {

    private ShapeImageView mGroupIcon;

    private TextView mGroupName;

    private TextView mTvGroupId;

    private ImageView mIvQrCode;

    private long mGroupId;

    private ImageRequestConfig mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
            .showImageForEmptyUri(R.drawable.default_group_icon)
            .showImageOnFail(R.drawable.default_group_icon).cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.default_group_icon)
            .build();

    public static void openGroupQrcode(Context context, long groupId) {
        Intent intent = new Intent(context, GroupQrCodeActivity.class);
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
        View view = View.inflate(this, R.layout.activity_qrcode_detail, null);
        mGroupIcon = view.findViewById(R.id.iv_user_avatar);
        mGroupName = view.findViewById(R.id.tv_user_name);
        mTvGroupId = view.findViewById(R.id.tv_user_id);
        mIvQrCode = view.findViewById(R.id.iv_qrcode);
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
    protected void initDataForActivity() {
        super.initDataForActivity();
        initGroup();

    }

    private void initGroup() {
        initQrCode();
        mTvGroupId.setText(mGroupId <= 0 ? "" : getString(R.string.group_id_colon) + mGroupId);
        GroupManager.getInstance().getGroupInfo(mGroupId, false, (bmxErrorCode, group) -> {
            if (BaseManager.bmxFinish(bmxErrorCode)) {
                String name = group.name();
                ChatUtils.getInstance().showGroupAvatar(group, mGroupIcon, mConfig);
                mGroupName.setText(TextUtils.isEmpty(name) ? "" : name);
            }
        });
    }

    /**
     * 设置二维码
     */
    private void initQrCode() {
        showLoadingDialog(true);
        AppManager.getInstance().getTokenByName(SharePreferenceUtils.getInstance().getUserName(),
                SharePreferenceUtils.getInstance().getUserPwd(),
                new HttpResponseCallback<String>() {
                    @Override
                    public void onResponse(String result) {
                        AppManager.getInstance().getGroupSign(mGroupId, result,
                                new HttpResponseCallback<String>() {
                                    @Override
                                    public void onResponse(String result) {
                                        dismissLoadingDialog();
                                        String qrUrl = QRCodeShowUtils.generateGroupQRCode(
                                                String.valueOf(mGroupId), result);
                                        Drawable drawable = QRCodeShowUtils.generateDrawable(qrUrl);
                                        mIvQrCode.setImageDrawable(drawable);
                                    }

                                    @Override
                                    public void onFailure(int errorCode, String errorMsg,
                                            Throwable t) {
                                        dismissLoadingDialog();
                                        ToastUtil.showTextViewPrompt(getString(R.string.failed_to_get_group_qr_code));
                                    }
                                });
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg, Throwable t) {
                        dismissLoadingDialog();
                        ToastUtil.showTextViewPrompt(getString(R.string.failed_to_get_group_qr_code));
                    }
                });
    }
}
