
package top.maxim.im.common.utils.dialog;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import top.maxim.im.R;

/**
 * Description : 基础dialog Created by Mango on 2018/11/11.
 */
public abstract class BaseDialog extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            Bundle savedInstanceState) {
        View view = onCreateDialogView();
        init();
        if (container != null) {
            container.addView(container);
        }
        return view;
    }

    /**
     * 初始化dialog
     */
    protected void init() {
        setStyle(R.style.dialog_normal, 0);
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            if (getDialog().getWindow() != null) {
                getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }

    /**
     * 创建dialogView
     * 
     * @return View
     */
    protected abstract View onCreateDialogView();
}
