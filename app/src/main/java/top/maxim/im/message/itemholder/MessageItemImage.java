
package top.maxim.im.message.itemholder;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.File;

import im.floo.floolib.BMXImageAttachment;
import im.floo.floolib.BMXMessage;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.ChatManager;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.view.BMImageLoader;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.common.view.ShapeImageView;
import top.maxim.im.message.interfaces.ChatActionListener;
import top.maxim.im.message.interfaces.FileCallback;

/**
 * Description : 消息图片类型 Created by Mango on 2018/11/18.
 */
public class MessageItemImage extends MessageItemBaseView {

    private ShapeImageView mImageView;

    /* 图片 */
    protected ImageRequestConfig mImageConfig;

    public MessageItemImage(@NonNull Context context, ChatActionListener listener, int itemPos) {
        super(context, listener, itemPos);
        mImageConfig = new ImageRequestConfig.Builder().cacheOnDisk(true).cacheOnDisk(true)
                .showImageOnLoading(R.color.c5).showImageOnFail(R.color.c5)
                .bitmapConfig(Bitmap.Config.RGB_565).showImageForEmptyUri(R.color.c5)
                .considerExifParams(true).build();
    }

    @Override
    protected View initView(ViewGroup parent) {
        View view;
        if (mItemPos == ITEM_LEFT) {
            view = View.inflate(mContext, R.layout.item_chat_image_left, parent);
            mImageView = view.findViewById(R.id.image_message);

        } else {
            view = View.inflate(mContext, R.layout.item_chat_image_right, parent);
            mImageView = view.findViewById(R.id.image_message);
        }
        return view;
    }

    @Override
    protected void bindData() {
        fillView();
    }

    /**
     * 填充数据
     */
    private void fillView() {
        setItemViewListener(mImageView);
        showPic();
    }

    /**
     * 展示图片
     */
    private void showPic() {
        if (mMaxMessage == null || mMaxMessage.contentType() != BMXMessage.ContentType.Image) {
            BMImageLoader.getInstance().display(mImageView, "");
            return;
        }
        final BMXImageAttachment body = BMXImageAttachment.dynamic_cast(mMaxMessage.attachment());
        if (body == null) {
            BMImageLoader.getInstance().display(mImageView, "");
            return;
        }
        FrameLayout.LayoutParams imgLayoutParams = null;
        String imgWidth = null, imgHeight = null;
        BMXImageAttachment.Size size = body.size();
        if (size != null) {
            imgWidth = String.valueOf(size.getMWidth());
            imgHeight = String.valueOf(size.getMHeight());
        }
        double maxLength = ScreenUtils.widthPixels * 0.5;
        double minLength = ScreenUtils.widthPixels * 0.5 * 0.5;
        if (!TextUtils.isEmpty(imgWidth) && !TextUtils.isEmpty(imgHeight)) {
            double limitDiff = maxLength / minLength;
            double diff = 0;
            double w = 0, h = 0;
            try {
                w = Double.valueOf(imgWidth);
                h = Double.valueOf(imgHeight);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (w > h) {
                // 图片角度是横
                diff = w / h;
                if (diff < limitDiff) {
                    // 宽高比不超过标准宽高比 小的一边按比例展示 大的一边标准长
                    imgLayoutParams = new FrameLayout.LayoutParams((int)maxLength,
                            (int)(maxLength / diff));
                } else {
                    // 宽高比大于最大宽高比 按最大宽高比展示
                    imgLayoutParams = new FrameLayout.LayoutParams((int)maxLength, (int)minLength);
                }
            } else if (w == h) {
                imgLayoutParams = new FrameLayout.LayoutParams((int)maxLength, (int)maxLength);
            } else {
                diff = h / w;
                if (diff < limitDiff) {
                    // 宽高比不超过标准宽高比 小的一边按比例展示 大的一边标准长
                    imgLayoutParams = new FrameLayout.LayoutParams((int)(maxLength / diff),
                            (int)maxLength);
                } else {
                    // 宽高比大于最大宽高比 按最大宽高比展示
                    imgLayoutParams = new FrameLayout.LayoutParams((int)minLength, (int)maxLength);
                }
            }
        } else {
            // 宽高比超过标准宽高比 按标准展示
            imgLayoutParams = new FrameLayout.LayoutParams((int)maxLength,
                    (int)(maxLength * 3 / 4));
        }
        mImageView.setLayoutParams(imgLayoutParams);
        String picUrl = null;
        if (!TextUtils.isEmpty(body.thumbnailPath()) && new File(body.thumbnailPath()).exists()) {
            picUrl = "file://" + body.thumbnailPath();
            BMImageLoader.getInstance().display(mImageView, picUrl, mImageConfig);
        } else if (!TextUtils.isEmpty(body.path()) && new File(body.path()).exists()) {
            picUrl = "file://" + body.path();
            BMImageLoader.getInstance().display(mImageView, picUrl, mImageConfig);
        } else {
            BMImageLoader.getInstance().display(mImageView, "", mImageConfig);
            ChatManager.getInstance().downloadAttachment(mMaxMessage,
                    new FileCallback(body.path()) {
                        @Override
                        protected void onProgress(long percent, String path, boolean isThumbnail) {

                        }

                        @Override
                        protected void onFinish(String url, boolean isThumbnail) {
                            BMImageLoader.getInstance().display(mImageView, "file://" + url,
                                    mImageConfig);
                        }

                        @Override
                        protected void onFail(String path, boolean isThumbnail) {

                        }
                    });
        }
    }

}
