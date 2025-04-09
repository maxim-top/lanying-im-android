
package top.maxim.im.common.utils.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import top.maxim.im.R;

/**
 * Description : 下拉菜单.
 */
public class DropDownMenu extends DialogFragment {

    private View mCustomView;

    protected Point mViewPosition;

    protected int mWidth;

    protected int mHeight;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = onCreateDialogView();
        if (container != null) {
            container.addView(container);
        }
        return view;
    }

    public void setViewPosition(Point viewPosition, int width, int height){
        mViewPosition = viewPosition;
        mWidth = width;
        mHeight = height;
    }

    protected View onCreateDialogView() {
        View view = View.inflate(getActivity(), R.layout.dropdown_menu, null);
        FrameLayout container = view.findViewById(R.id.fl_dialog_container);
        if (mCustomView != null) {
            container.addView(mCustomView);
        }
        return view;
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext(), R.style.dialog_normal);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dropdown_menu);

        if (mViewPosition != null){
            WindowManager.LayoutParams attributes = dialog.getWindow().getAttributes();
            attributes.gravity = Gravity.TOP | Gravity.LEFT;
            if (mViewPosition.x >= 0){
                attributes.x = mViewPosition.x;
            }else{
                attributes.gravity |= Gravity.CENTER_HORIZONTAL;
            }

            if (mWidth >= 0){
                attributes.width = mWidth;
            }

            if (mHeight >= 0){
                attributes.height = mHeight;
            }

            if (mViewPosition.y >= 0) {
                attributes.y = mViewPosition.y;
            }else{
                attributes.gravity |= Gravity.CENTER_VERTICAL;
            }
        }
        Resources resources=getResources();
        Drawable drawable=resources.getDrawable(R.drawable.bg_edit_menu);
        dialog.getWindow().setBackgroundDrawable(drawable);
        return dialog;
    }


    public void setCustomView(View view) {
        mCustomView = view;
    }

    /**
     * 展示dialog
     */
    public void showDialog(Activity activity) {
        show(activity.getFragmentManager(), "DropDownMenu");
    }

}
