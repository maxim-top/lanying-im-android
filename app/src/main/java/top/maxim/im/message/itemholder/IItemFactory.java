
package top.maxim.im.message.itemholder;

import android.view.View;
import android.view.ViewGroup;

/**
 * Description : item控制 Created by Mango on 2018/11/18.
 */
public interface IItemFactory<T> {

    View obtainView(ViewGroup parent);

    void bindData(T t);
}
