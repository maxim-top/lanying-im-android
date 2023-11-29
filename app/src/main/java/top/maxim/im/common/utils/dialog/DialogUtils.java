
package top.maxim.im.common.utils.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Description : 弹出框工具类 Created by Mango on 2018/11/11.
 */
public class DialogUtils {

    private static DialogUtils mInstance;

    public static DialogUtils getInstance() {
        if (mInstance == null) {
            synchronized (DialogUtils.class) {
                if (mInstance == null) {
                    mInstance = new DialogUtils();
                }
            }
        }
        return mInstance;
    }

    /**
     * 带标题 内容
     * 
     * @param context 上下文
     * @param title 标题
     * @param content 内容
     * @param listener 监听
     */
    public void showDialog(Activity context, String title, String content,
            CommonDialog.OnDialogListener listener) {
        CommonDialog dialog = new CommonDialog();
        Bundle bundle = new Bundle();
        bundle.putString(CommonDialog.DIALOG_TITLE, title);
        bundle.putString(CommonDialog.DIALOG_CONTENT, content);
        dialog.setArguments(bundle);
        dialog.setDialogListener(listener);
        dialog.showDialog(context);
    }

    /**
     * 带标题 内容
     * 
     * @param context 上下文
     * @param title 标题
     * @param content 内容
     * @param confirm 确认文本
     * @param cancel 取消文本
     * @param listener 监听
     */
    public void showDialog(Activity context, String title, String content, String confirm,
            String cancel, CommonDialog.OnDialogListener listener) {
        CommonDialog dialog = new CommonDialog();
        Bundle bundle = new Bundle();
        bundle.putString(CommonDialog.DIALOG_TITLE, title);
        bundle.putString(CommonDialog.DIALOG_CONTENT, content);
        bundle.putString(CommonDialog.DIALOG_CONFIRM, confirm);
        bundle.putString(CommonDialog.DIALOG_CANCEL, cancel);
        dialog.setArguments(bundle);
        dialog.setDialogListener(listener);
        dialog.showDialog(context);
    }

    /**
     * 带标题 输入框dialog
     *
     * @param context 上下文
     * @param title 标题
     * @param confirm 确认文本
     * @param cancel 取消文本
     * @param listener 监听
     */
    public void showEditDialog(Activity context, String title, String confirm, String cancel,
            CommonEditDialog.OnDialogListener listener) {
        showEditDialog(context, title, confirm, cancel, false, listener);
    }

    /**
     * 带标题 输入框dialog
     *
     * @param context 上下文
     * @param title 标题
     * @param confirm 确认文本
     * @param cancel 取消文本
     * @param listener 监听
     */
    public void showEditDialog(Activity context, String title, String content, String confirm, String cancel,
                               CommonEditDialog.OnDialogListener listener) {
        showEditDialog(context, title, content, confirm, cancel, false, listener);
    }

    /**
     * 带标题 输入框dialog
     *
     * @param context 上下文
     * @param title 标题
     * @param confirm 确认文本
     * @param cancel 取消文本
     * @param listener 监听
     */
    public void showEditDialog(Activity context, String title, String confirm, String cancel,
            boolean onlyNumber, CommonEditDialog.OnDialogListener listener) {
        CommonEditDialog dialog = new CommonEditDialog();
        Bundle bundle = new Bundle();
        bundle.putString(CommonEditDialog.DIALOG_TITLE, title);
        bundle.putString(CommonEditDialog.DIALOG_CONFIRM, confirm);
        bundle.putString(CommonEditDialog.DIALOG_CANCEL, cancel);
        bundle.putBoolean(CommonEditDialog.DIALOG_INPUT_NUMBER, onlyNumber);
        dialog.setArguments(bundle);
        dialog.setDialogListener(listener);
        dialog.showDialog(context);
    }

    /**
     * 带标题 输入框dialog
     *
     * @param context 上下文
     * @param title 标题
     * @param confirm 确认文本
     * @param cancel 取消文本
     * @param listener 监听
     */
    public void showEditDialog(Activity context, String title, String content, String confirm, String cancel,
                               boolean onlyNumber, CommonEditDialog.OnDialogListener listener) {
        CommonEditDialog dialog = new CommonEditDialog();
        Bundle bundle = new Bundle();
        bundle.putString(CommonEditDialog.DIALOG_TITLE, title);
        bundle.putString(CommonEditDialog.DIALOG_CONFIRM, confirm);
        bundle.putString(CommonEditDialog.DIALOG_CANCEL, cancel);
        bundle.putString(CommonEditDialog.DIALOG_CONTENT, content);
        bundle.putBoolean(CommonEditDialog.DIALOG_INPUT_NUMBER, onlyNumber);
        dialog.setArguments(bundle);
        dialog.setDialogListener(listener);
        dialog.showDialog(context);
    }

    /**
     * 带标题 自定义dialog
     *
     * @param context 上下文
     * @param view view
     * @param title 标题
     * @param confirm 确认文本
     * @param cancel 取消文本
     * @param listener 监听
     */
    public void showCustomDialog(Activity context, View view, String title, String confirm,
            String cancel, CommonCustomDialog.OnDialogListener listener) {
        showCustomDialog(context, view, true, title, confirm, cancel, listener);
    }

    /**
     * 带标题 自定义dialog
     *
     * @param context 上下文
     * @param view view
     * @param title 标题
     * @param confirm 确认文本
     * @param cancel 取消文本
     * @param listener 监听
     */
    public void showCustomDialog(Activity context, View view, boolean cancelable, String title, String confirm,
            String cancel, CommonCustomDialog.OnDialogListener listener) {
        CommonCustomDialog dialog = new CommonCustomDialog();
        dialog.setCustomView(view);
        Bundle bundle = new Bundle();
        bundle.putString(CommonCustomDialog.DIALOG_TITLE, title);
        bundle.putString(CommonCustomDialog.DIALOG_CONFIRM, confirm);
        bundle.putString(CommonCustomDialog.DIALOG_CANCEL, cancel);
        bundle.putBoolean(CommonCustomDialog.DIALOG_CAN_CANCEL, cancelable);
        dialog.setArguments(bundle);
        dialog.setDialogListener(listener);
        dialog.showDialog(context);
    }
}
