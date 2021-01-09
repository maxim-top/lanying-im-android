
package top.maxim.im.common.view.photoview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;

import top.maxim.im.R;
import top.maxim.im.common.bean.PhotoViewBean;
import top.maxim.im.common.view.BMImageLoader;
import top.maxim.im.common.view.ImageRequestConfig;

public class PhotoVisitorView extends RelativeLayout {

    private Context mContext;

    /* 普通图原图 */
    private PhotoView mPhotoView;

    /* 图片信息 */
    private PhotoViewBean mPhotoViewBean;

    /* 图片配置 */
    private ImageRequestConfig mConfig;

    public PhotoVisitorView(Context context) {
        this(context, null);
    }

    public PhotoVisitorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private PhotoVisitorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mConfig = new ImageRequestConfig.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.NONE)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
                .build();
        initView(context);
    }

    public void setPhotoViewBean(PhotoViewBean photoViewBean, int position) {
        mPhotoViewBean = photoViewBean;
        showPhoto(position);
    }

    /**
     * 初始化view
     *
     * @param context 上下文
     */
    private void initView(Context context) {
        View view = inflate(context, R.layout.photo_visitor_view, this);
        mPhotoView = view.findViewById(R.id.photoView);
    }

    /**
     * 展示图片
     */
    private void showPhoto(final int position) {
        mPhotoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                onBack();
            }
        });

        mPhotoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                onBack();
            }
        });
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mPhotoView.setLayoutParams(params);

        String loadUrl = null;
        if (mPhotoViewBean != null) {
            if (!TextUtils.isEmpty(mPhotoViewBean.getLocalPath())
                    && new File(mPhotoViewBean.getLocalPath()).exists()) {
                loadUrl = "file://" + mPhotoViewBean.getLocalPath();
            } else if (!TextUtils.isEmpty(mPhotoViewBean.getThumbLocalPath())) {
                loadUrl = "file://" + mPhotoViewBean.getThumbLocalPath();
            } else if (!TextUtils.isEmpty(mPhotoViewBean.getHttpUrl())) {
                loadUrl = mPhotoViewBean.getHttpUrl();
            } else if (!TextUtils.isEmpty(mPhotoViewBean.getThumbHttpUrl())) {
                loadUrl = mPhotoViewBean.getThumbHttpUrl();
            }
        }
        mPhotoView.setVisibility(View.VISIBLE);
        BMImageLoader.getInstance().display(mPhotoView, loadUrl, mConfig, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
    }

    /**
     * 返回
     */
    private void onBack() {
        ((Activity)mContext).finish();
    }

}
