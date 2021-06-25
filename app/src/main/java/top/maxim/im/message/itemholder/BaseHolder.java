
package top.maxim.im.message.itemholder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * Description : 基础holder Created by Mango on 2018/11/18.
 */
public class BaseHolder<T> extends RecyclerView.ViewHolder {

    protected IItemFactory<T> mFactory;

    public BaseHolder(View view, IItemFactory<T> factory) {
        super(view);
        mFactory = factory;
    }

    public void setData(T data) {
        mFactory.bindData(data);
    }
}
