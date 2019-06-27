
package top.maxim.im.common.view;

import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Description : 图片展示工具 Created by Mango on 2018/11/06.
 */
public class BMImageLoader {
    private static BMImageLoader mInstance;

    private ImageLoader mLoader;

    public static BMImageLoader getInstance() {
        if (mInstance == null) {
            synchronized (BMImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new BMImageLoader();
                }
            }
        }
        return mInstance;
    }

    private BMImageLoader() {
        mLoader = ImageLoader.getInstance();
    }

    public void display(ImageView imageView, @DrawableRes int resId) {
        mLoader.displayImage("drawable://" + resId, imageView);
    }

    public void display(ImageView imageView, @DrawableRes int resId,
            ImageLoadingListener listener) {
        mLoader.displayImage("drawable://" + resId, imageView, listener);
    }

    public void display(ImageView imageView, @DrawableRes int resId, ImageRequestConfig config) {
        mLoader.displayImage("drawable://" + resId, imageView,
                config != null ? config.getOptions() : null);
    }

    public void display(ImageView imageView, @DrawableRes int resId, ImageRequestConfig config,
            ImageLoadingListener listener) {
        mLoader.displayImage("drawable://" + resId, imageView,
                config != null ? config.getOptions() : null, listener);
    }

    public void display(ImageView imageView, String uri) {
        mLoader.displayImage(uri, imageView);
    }

    public void display(ImageView imageView, String uri, ImageLoadingListener listener) {
        mLoader.displayImage(uri, imageView, listener);
    }

    public void display(ImageView imageView, String uri, ImageRequestConfig config) {
        mLoader.displayImage(uri, imageView, config != null ? config.getOptions() : null);
    }

    public void display(ImageView imageView, String uri, ImageRequestConfig config,
            ImageLoadingListener listener) {
        mLoader.displayImage(uri, imageView, config != null ? config.getOptions() : null, listener);
    }

    public void loadImage(String uri, ImageRequestConfig config, ImageLoadingListener listener) {
        mLoader.loadImage(uri, config != null ? config.getOptions() : null, listener);
    }
}
