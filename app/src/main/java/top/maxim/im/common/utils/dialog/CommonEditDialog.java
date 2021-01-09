
package top.maxim.im.common.utils.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import top.maxim.im.R;

/**
 * Description : edit公共弹出框 Created by Mango on 2018/11/11.
 */
public class CommonEditDialog extends BaseDialog implements View.OnClickListener {

    /* 弹出框标题 */
    private TextView mTvTitle;

    private EditText mTvContent;

    /* 确定 取消 */
    private TextView mTvConfirm, mTvCancel;

    private OnDialogListener mOnDialogListener;

    public static final String DIALOG_TITLE = "dialogTitle";

    public static final String DIALOG_CONFIRM = "dialogConfirm";

    public static final String DIALOG_CANCEL = "dialogCancel";

    public static final String DIALOG_INPUT_NUMBER = "dialogInputNumber";
    
    private String mDialogTitle, mConfirm, mCancel;
    
    private boolean mInputNumber = false;
    
    @Override
    protected View onCreateDialogView() {
        initFromFront();
        View view = View.inflate(getActivity(), R.layout.dialog_edit_view, null);
        mTvTitle = (TextView)view.findViewById(R.id.tv_dialog_title);
        mTvContent = (EditText)view.findViewById(R.id.edit_dialog);
        mTvConfirm = (TextView)view.findViewById(R.id.tv_dialog_confirm);
        mTvConfirm.setOnClickListener(this);
        mTvCancel = (TextView)view.findViewById(R.id.tv_dialog_cancel);
        mTvCancel.setOnClickListener(this);
        if (mInputNumber) {
            mTvContent.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
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
        mInputNumber = bundle.getBoolean(DIALOG_INPUT_NUMBER, false);
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
                    String text = mTvContent.getEditableText().toString();
                    mOnDialogListener.onConfirmListener(text);
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
        void onConfirmListener(String content);

        /**
         * 取消
         */
        void onCancelListener();
    }
    
}
