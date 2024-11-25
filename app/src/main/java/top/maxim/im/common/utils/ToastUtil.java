
package top.maxim.im.common.utils;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.StringRes;

import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import top.maxim.im.R;

/**
 * Description :Toast的工具类
 */
public class ToastUtil {

    private static Toast sToast;

    /**
     * Toast显示一个View
     * 
     * @param context
     * @param view
     */
    public static void show(Context context, View view) {
        Toast toast = getToast(view);
        toast.show();
    }

    /**
     * 显示一个View为TextView的Toast
     * 
     * @param textResId
     */
    public static void showTextViewPrompt(@StringRes int textResId) {
        showTextViewPrompt(AppContextUtils.getAppContext(),
                AppContextUtils.getAppContext().getString(textResId));
    }

    /**
     * 重载的方法，同上
     * 
     * @param name
     */
    public static void showTextViewPrompt(String name) {
        Handler mainThreadHandler = new Handler(Looper.getMainLooper());
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                showTextViewPrompt(AppContextUtils.getAppContext(), name);
            }
        });
    }

    public static void showTextViewPrompt(Context context, String name) {
        showVerboseToast(name);
    }

    public static void showTextViewPromptShort(Context context, String name) {
        showVerboseToast(name);
    }

    public static void showTextViewPromptLong(Context context, String name) {
        showVerboseToast(name);
    }

    /**
     * 显示View为TextView 的Toast
     * 
     * @param text
     */
    public static void showVerboseToast(String text) {
        View view = View.inflate(AppContextUtils.getAppContext(), R.layout.toast_view_prompt, null);
        TextView tv = (TextView)view.findViewById(R.id.tv_toast_prompt);
        tv.setText(text);
        tv.setCompoundDrawables(null, null, null, null);

        Toast toast = getToast(view);
        toast.show();
    }

    private static Toast getToast(View view) {

        if (sToast == null) {
            Toast toast = new Toast(AppContextUtils.getAppContext());
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            sToast = toast;
        }
        sToast.setView(view);
        return sToast;
    }

}
