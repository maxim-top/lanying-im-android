
package top.maxim.im.common.utils.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import top.maxim.im.R;

/**
 * Description : 自定义daolog Created by Mango on 2018/11/11.
 */
public class CommonCustomDialog extends BaseDialog implements View.OnClickListener {

    /* 弹出框标题 */
    private TextView mTvTitle;

    /* 确定 取消 */
    private TextView mTvConfirm, mTvCancel;

    private View mCustomView;

    private OnDialogListener mOnDialogListener;

    public static final String DIALOG_TITLE = "dialogTitle";

    public static final String DIALOG_CONFIRM = "dialogConfirm";

    public static final String DIALOG_CANCEL = "dialogCancel";

    public static final String DIALOG_CAN_CANCEL = "dialogCanCancel";

    private String mDialogTitle, mConfirm, mCancel;

    // 点击外部和返回键是否可取消 默认true
    private boolean mCanCancel = true;

    @Override
    protected View onCreateDialogView() {
        initFromFront();
        View view = View.inflate(getActivity(), R.layout.dialog_custom_view, null);
        FrameLayout container = view.findViewById(R.id.fl_dialog_container);
        if (mCustomView != null) {
            container.addView(mCustomView);
        }
        mTvTitle = (TextView)view.findViewById(R.id.tv_dialog_title);
        mTvConfirm = (TextView)view.findViewById(R.id.tv_dialog_confirm);
        mTvConfirm.setOnClickListener(this);
        mTvCancel = (TextView)view.findViewById(R.id.tv_dialog_cancel);
        mTvCancel.setOnClickListener(this);
        return view;
    }

    /**
     * 初始化传入数据
     */
    private void initFromFront() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        mDialogTitle = bundle.getString(DIALOG_TITLE);
        mConfirm = bundle.getString(DIALOG_CONFIRM);
        mCancel = bundle.getString(DIALOG_CANCEL);
        mCanCancel = bundle.getBoolean(DIALOG_CAN_CANCEL, true);
    }

    @Override
    protected void init() {
        super.init();
        if (getDialog() != null) {
            getDialog().setCanceledOnTouchOutside(mCanCancel);
        }

    }

    public void setCustomView(View view) {
        mCustomView = view;
    }

    @Override
    public void onStart() {
        super.onStart();
        bindView();
    }

    private void bindView() {
        mTvTitle.setText(!TextUtils.isEmpty(mDialogTitle) ? mDialogTitle : "");
        if (!TextUtils.isEmpty(mConfirm)) {
            mTvConfirm.setText(mConfirm);
        } else {
            mTvConfirm.setText(R.string.confirm);
        }
        if (!TextUtils.isEmpty(mCancel)) {
            mTvCancel.setText(mCancel);
        } else {
            mTvCancel.setText(R.string.cancel);
        }
    }

    public void setDialogListener(OnDialogListener listener) {
        mOnDialogListener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_dialog_confirm:
                dismiss();
                if (mOnDialogListener != null) {
                    mOnDialogListener.onConfirmListener();
                }
                break;
            case R.id.tv_dialog_cancel:
                dismiss();
                if (mOnDialogListener != null) {
                    mOnDialogListener.onCancelListener();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 展示dialog
     */
    public void showDialog(Activity activity) {
        show(activity.getFragmentManager(), "CommonDialog");
    }

    /**
     * 确定 取消点击
     */
    public interface OnDialogListener {

        /**
         * 确定
         */
        void onConfirmListener();

        /**
         * 取消
         */
        void onCancelListener();
    }
}
