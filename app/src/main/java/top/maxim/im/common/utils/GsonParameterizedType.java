
package top.maxim.im.common.utils;

import android.support.annotation.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Description : Gson解析list Created by Mango on 2019-10-11.
 */
public class GsonParameterizedType implements ParameterizedType {

    private Class clazz;

    public GsonParameterizedType(Class clazz) {
        this.clazz = clazz;
    }

    @NonNull
    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{clazz};
    }

    @NonNull
    @Override
    public Type getRawType() {
        return List.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}
