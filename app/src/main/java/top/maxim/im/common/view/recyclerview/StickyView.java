
package top.maxim.im.common.view.recyclerview;

import android.view.View;

/**
 * Description : 吸附效果view Created by Mango on 2018/11/24.
 */
public class StickyView implements IStickyView {

    /* 吸顶效果type */
    public static final int StickyType = 1000;

    @Override
    public boolean isStickyView(View view) {
        return (boolean)view.getTag();
    }

    @Override
    public int getStickyViewType() {
        return StickyType;
    }
}
