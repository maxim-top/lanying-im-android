
package top.maxim.im.common.base;

import android.content.Context;

/**
 * Description : 基础view Created by Mango on 2018/11/05.
 */
public interface IBaseView<T> {

    Context getContext();

    void setPresenter(T t);
}
