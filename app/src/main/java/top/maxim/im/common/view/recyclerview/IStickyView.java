
package top.maxim.im.common.view.recyclerview;

import android.view.View;

/**
 * Description : 吸附效果view Created by Mango on 2018/11/24.
 */
public interface IStickyView {

    boolean isStickyView(View view);

    int getStickyViewType();
}
