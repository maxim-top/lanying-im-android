
package top.maxim.im.common.utils.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;

import top.maxim.im.R;

/**
 * Description : 自定义daolog Created by Mango on 2018/11/11.
 */
public class CustomDialog extends BaseDialog {

    private View mCustomView;

    @Override
    protected View onCreateDialogView() {
        View view = View.inflate(getActivity(), R.layout.dialog_custom_view_group, null);
        FrameLayout container = view.findViewById(R.id.fl_dialog_container);
        if (mCustomView != null) {
            container.addView(mCustomView);
        }
        return view;
    }

    public void setCustomView(View view) {
        mCustomView = view;
    }

    /**
     * 展示dialog
     */
    public void showDialog(Activity activity) {
        show(activity.getFragmentManager(), "CommonDialog");
    }

}
