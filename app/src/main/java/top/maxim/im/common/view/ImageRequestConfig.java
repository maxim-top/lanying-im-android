
package top.maxim.im.common.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

/**
 * Description : 图片配置常量 Created by Mango on 2018/11/06.
 */
public class ImageRequestConfig {
    private final int imageResOnLoading;

    private final int imageResForEmptyUri;

    private final int imageResOnFail;

    private final Drawable imageOnLoading;

    private final Drawable imageForEmptyUri;

    private final Drawable imageOnFail;

    private final boolean cacheInMemory;

    private final boolean cacheOnDisk;

    private DisplayImageOptions opt;

    private final boolean considerExifParams;

    private final ImageScaleType scaleType;

    private Object extraForDownloader;

    private BitmapFactory.Options decodingOptions = new BitmapFactory.Options();

    protected DisplayImageOptions getOptions() {
        return opt;
    }

    private void change() {
        opt = new DisplayImageOptions.Builder().cacheInMemory(cacheInMemory)
                .cacheOnDisk(cacheOnDisk).showImageForEmptyUri(imageForEmptyUri)
                .showImageForEmptyUri(imageResForEmptyUri).showImageOnLoading(imageOnLoading)
                .showImageOnLoading(imageResOnLoading).showImageOnFail(imageOnFail)
                .showImageOnFail(imageResOnFail).decodingOptions(decodingOptions)
                .considerExifParams(considerExifParams).extraForDownloader(extraForDownloader)
                .imageScaleType(scaleType).build();

    }

    private ImageRequestConfig(Builder builder) {
        imageOnLoading = builder.imageOnLoading;
        imageForEmptyUri = builder.imageForEmptyUri;
        imageOnFail = builder.imageOnFail;
        cacheInMemory = builder.cacheInMemory;
        cacheOnDisk = builder.cacheOnDisk;
        imageResOnLoading = builder.imageResOnLoading;
        imageResForEmptyUri = builder.imageResForEmptyUri;
        imageResOnFail = builder.imageResOnFail;
        decodingOptions = builder.decodingOptions;
        considerExifParams = builder.considerExifParams;
        scaleType = builder.scaleType;
        extraForDownloader = builder.extraForDownloader;

        change();
    }

    public static class Builder {
        private int imageResOnLoading = 0;

        private int imageResForEmptyUri = 0;

        private int imageResOnFail = 0;

        private Drawable imageOnLoading = null;

        private Drawable imageForEmptyUri = null;

        private Drawable imageOnFail = null;

        private boolean cacheInMemory = false;

        private boolean cacheOnDisk = false;

        private boolean considerExifParams = false;

        private Object extraForDownloader;

        private ImageScaleType scaleType = ImageScaleType.IN_SAMPLE_POWER_OF_2;

        private BitmapFactory.Options decodingOptions = new BitmapFactory.Options();

        public Builder showImageOnLoading(@DrawableRes int resId) {
            imageResOnLoading = resId;
            return this;
        }

        public Builder showImageOnLoading(Drawable drawable) {
            imageOnLoading = drawable;
            return this;
        }

        public Builder extraForDownloader(Object extra) {
            this.extraForDownloader = extra;
            return this;
        }

        public Builder showImageForEmptyUri(@DrawableRes int resId) {
            imageResForEmptyUri = resId;
            return this;
        }

        public Builder showImageForEmptyUri(Drawable drawable) {
            imageForEmptyUri = drawable;
            return this;
        }

        public Builder showImageOnFail(@DrawableRes int resId) {
            imageResOnFail = resId;
            return this;
        }

        public Builder showImageOnFail(Drawable drawable) {
            imageOnFail = drawable;
            return this;
        }

        public Builder bitmapConfig(Bitmap.Config bitmapConfig) {
            if (bitmapConfig != null)
                decodingOptions.inPreferredConfig = bitmapConfig;
            return this;
        }

        public Builder cacheInMemory(boolean cache) {
            cacheInMemory = cache;
            return this;
        }

        public Builder cacheOnDisk(boolean cache) {
            cacheOnDisk = cache;
            return this;
        }

        public Builder considerExifParams(boolean considerExifParams) {
            this.considerExifParams = considerExifParams;
            return this;
        }

        public Builder imageScaleType(ImageScaleType imageScaleType) {
            scaleType = imageScaleType;
            return this;
        }

        public ImageRequestConfig build() {
            return new ImageRequestConfig(this);
        }

    }
}
