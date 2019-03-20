
package top.maxim.im.common.utils.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import top.maxim.im.R;

/**
 * Description : 公共弹出框 Created by Mango on 2018/11/11.
 */
public class CommonDialog extends BaseDialog implements View.OnClickListener {

    /* 弹出框标题 */
    private TextView mTvTitle;

    private TextView mTvContent;

    /* 确定 取消 */
    private TextView mTvConfirm, mTvCancel;

    private OnDialogListener mOnDialogListener;

    public static final String DIALOG_TITLE = "dialogTitle";

    public static final String DIALOG_CONTENT = "dialogContent";

    public static final String DIALOG_CONFIRM = "dialogConfirm";

    public static final String DIALOG_CANCEL = "dialogCancel";

    private String mDialogTitle, mDialogContent, mConfirm, mCancel;

    @Override
    protected View onCreateDialogView() {
        initFromFront();
        View view = View.inflate(getActivity(), R.layout.dialog_view, null);
        mTvTitle = (TextView)view.findViewById(R.id.tv_dialog_title);
        mTvContent = (TextView)view.findViewById(R.id.tv_dialog_content);
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
        mDialogContent = bundle.getString(DIALOG_CONTENT);
        mConfirm = bundle.getString(DIALOG_CONFIRM);
        mCancel = bundle.getString(DIALOG_CANCEL);
    }

    @Override
    public void onStart() {
        super.onStart();
        bindView();
    }

    private void bindView() {
        mTvTitle.setText(!TextUtils.isEmpty(mDialogTitle) ? mDialogTitle : "");
        mTvContent.setText(!TextUtils.isEmpty(mDialogContent) ? mDialogContent : "");
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
