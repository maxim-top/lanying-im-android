
package top.maxim.im.common.view;

import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import top.maxim.im.message.utils.ChatUtils;

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

    public void display(ImageView imageView, String uri) {
        ChatUtils.getInstance().removeAvatarCache(uri);
        mLoader.displayImage(uri, imageView);
    }

    public void display(ImageView imageView, String uri, ImageLoadingListener listener) {
        ChatUtils.getInstance().removeAvatarCache(uri);
        mLoader.displayImage(uri, imageView, listener);
    }

    public void display(ImageView imageView, String uri, ImageRequestConfig config) {
        ChatUtils.getInstance().removeAvatarCache(uri);
        mLoader.displayImage(uri, imageView, config != null ? config.getOptions() : null);
    }

    public void display(ImageView imageView, String uri, ImageRequestConfig config,
            ImageLoadingListener listener) {
        ChatUtils.getInstance().removeAvatarCache(uri);
        mLoader.displayImage(uri, imageView, config != null ? config.getOptions() : null, listener);
    }

    public void loadImage(String uri, ImageRequestConfig config, ImageLoadingListener listener) {
        ChatUtils.getInstance().removeAvatarCache(uri);
        mLoader.loadImage(uri, config != null ? config.getOptions() : null, listener);
    }
}
