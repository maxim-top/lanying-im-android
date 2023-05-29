
package top.maxim.im.login.view;

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
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.SharePreferenceUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.message.utils.ChatUtils;
import top.maxim.im.scan.utils.QRCodeShowUtils;

/**
 * Description : 我的二维码 Created by Mango on 2019-06-26.
 */
public class MyQrCodeActivity extends BaseTitleActivity {

    private ShapeImageView mUserIcon;

    private TextView mUserName;

    private TextView mUserId;

    private TextView mNickName;

    private ImageView mIvQrCode;

    private ImageRequestConfig mConfig = new ImageRequestConfig.Builder().cacheInMemory(true)
            .showImageForEmptyUri(R.drawable.default_avatar_icon)
            .showImageOnFail(R.drawable.default_avatar_icon).cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.default_avatar_icon)
            .build();

    public static void openMyQrcode(Context context) {
        Intent intent = new Intent(context, MyQrCodeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle(R.string.my_qrcode);
        builder.setBackIcon(R.drawable.header_back_icon, v -> finish());
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.activity_qrcode_detail, null);
        mUserIcon = view.findViewById(R.id.iv_user_avatar);
        mUserName = view.findViewById(R.id.tv_user_name);
        mNickName = view.findViewById(R.id.tv_nick_name);
        mUserId = view.findViewById(R.id.tv_user_id);
        mIvQrCode = view.findViewById(R.id.iv_qrcode);
        return view;
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        initUser();

    }

    private void initUser() {
        initQrCode();
        UserManager.getInstance().getProfile(false, (bmxErrorCode, profile) -> {
            if (BaseManager.bmxFinish(bmxErrorCode) && profile != null) {
                String name = profile.username();
                String nickName = profile.nickname();
                ChatUtils.getInstance().showProfileAvatar(profile, mUserIcon, mConfig);
                long userId = profile.userId();
                mUserName.setText(TextUtils.isEmpty(name) ? "" : name);
                mNickName.setText(TextUtils.isEmpty(nickName) ? "" : getString(R.string.nickname_colon) + nickName);
                mUserId.setText(userId <= 0 ? "" : "BMXID:" + userId);
            }
        });
    }

    /**
     * 设置二维码
     */
    private void initQrCode() {
        String qrUrl = QRCodeShowUtils.generateRosterQRCode(
                String.valueOf(SharePreferenceUtils.getInstance().getUserId()));
        Drawable drawable = QRCodeShowUtils.generateDrawable(qrUrl);
        mIvQrCode.setImageDrawable(drawable);
    }
}
