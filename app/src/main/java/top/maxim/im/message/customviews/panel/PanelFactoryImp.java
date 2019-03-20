
package top.maxim.im.message.customviews.panel;

import android.app.Activity;
import android.util.SparseArray;

/**
 * Description : 输入面板 Created by Mango on 2018/11/06.
 */
public class PanelFactoryImp implements IPanelFactory {

    public static final int TYPE_EMOJI = 1;

    public static final int TYPE_FUNCTION = 2;

    private Activity mActivity;

    private SparseArray<IPanel> mCaches;

    public PanelFactoryImp(Activity activity) {
        mActivity = activity;
        mCaches = new SparseArray();
    }

    @Override
    public IPanel obtainPanel(int type) {
        IPanel panel = mCaches.get(type);
        if (panel == null) {
            switch (type) {
                case TYPE_FUNCTION:
                    panel = new PanelFuncView(mActivity);
                    break;
                case TYPE_EMOJI:
                    break;
                default:
                    break;
            }
            if (panel != null) {
                mCaches.put(type, panel);
            }
        }
        return panel;
    }
}
