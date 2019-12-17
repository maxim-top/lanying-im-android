
package top.maxim.im.login.view;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import top.maxim.im.R;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.CommonConfig;
import top.maxim.im.common.view.Header;

/**
 * Description : 验证页面 Created by Mango on 2018/11/06
 */
public class VerifyActivity extends BaseTitleActivity {

    private TextView mVerifyTitle;

    private View mVerifyPhone;

    private View mVerifyPwd;

    private TextView mTvPhone;

    private EditText mEtPwd;

    private TextView mTvContinue;

    private String mPhone;

    private int mVerifyType = CommonConfig.VerifyType.TYPE_WX;

    public static void startVerifyPwdActivity(Context context, int type, String phone) {
        Intent intent = new Intent(context, VerifyActivity.class);
        intent.putExtra(CommonConfig.VERIFY_TYPE, type);
        intent.putExtra(CommonConfig.PHONE, phone);
        context.startActivity(intent);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setTitle("");
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
        View view = View.inflate(this, R.layout.activity_verify, null);
        mVerifyTitle = view.findViewById(R.id.tv_verify_title);
        mVerifyPhone = view.findViewById(R.id.ll_et_verify_phone);
        mVerifyPwd = view.findViewById(R.id.ll_et_verify_pwd);
        mTvPhone = view.findViewById(R.id.et_verify_phone);
        mEtPwd = view.findViewById(R.id.et_verify_pwd);
        mTvContinue = view.findViewById(R.id.tv_continue);
        return view;
    }

    @Override
    protected void initDataFromFront(Intent intent) {
        super.initDataFromFront(intent);
        if (intent != null) {
            mVerifyType = intent.getIntExtra(CommonConfig.VERIFY_TYPE,
                    CommonConfig.VerifyType.TYPE_WX);
            mPhone = intent.getStringExtra(CommonConfig.PHONE);
        }
    }

    @Override
    protected void setViewListener() {
    }

    @Override
    protected void initDataForActivity() {
        String title = "", tag = "";
        switch (mVerifyType) {
            case CommonConfig.VerifyType.TYPE_WX:
                mVerifyPhone.setVisibility(View.GONE);
                title = getString(R.string.un_bind_wechat);
                tag = getString(R.string.un_bind_wechat_tag);
                break;
            case CommonConfig.VerifyType.TYPE_PHONE:
                mVerifyPhone.setVisibility(View.GONE);
                title = getString(R.string.change_mobile);
                tag = getString(R.string.verify_pwd_tag);
                break;
            case CommonConfig.VerifyType.TYPE_PHONE_VERIFY:
                mVerifyPhone.setVisibility(View.VISIBLE);
                title = getString(R.string.change_mobile);
                tag = getString(R.string.verify_pwd_tag);
                mTvPhone.setText(mPhone);
                break;
            default:
                break;
        }
        mHeader.setTitle(title);
        mVerifyTitle.setText(tag);
    }
}
