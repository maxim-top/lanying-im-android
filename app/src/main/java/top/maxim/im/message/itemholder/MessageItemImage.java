
package top.maxim.im.message.itemholder;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import top.maxim.im.message.interfaces.MsgAttachmentCallback;
import top.maxim.im.message.utils.ChatAttachmentManager;

/**
 * Description : 消息图片类型 Created by Mango on 2018/11/18.
 */
public class MessageItemImage extends MessageItemBaseView {

    private ShapeImageView mImageView;

    private View mFlShadow;

    private ShapeImageView mShadowView;

    private TextView mProgress;

    /* 图片 */
    protected ImageRequestConfig mImageConfig;

    private LongSparseArray<Integer> mProgressCache = new LongSparseArray<>();

    private MsgAttachmentCallback listener = new MsgAttachmentCallback() {
        @Override
        public void onProgress(long msgId, int percent) {
            if (mMaxMessage != null) {
                mProgressCache.put(mMaxMessage.msgId(), percent);
            }
            showImageProgress();
        }

        @Override
        public void onFinish(long msgId) {
            mProgressCache.remove(msgId);
            mFlShadow.setVisibility(View.GONE);
            showPic();
        }

        @Override
        public void onFail(long msgId) {
            mProgressCache.remove(msgId);
            mFlShadow.setVisibility(View.GONE);
        }
    };

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
            mFlShadow = view.findViewById(R.id.fl_image_message_shadow);
            mShadowView = view.findViewById(R.id.image_message_shadow);
            mProgress = view.findViewById(R.id.image_progress);
        } else {
            view = View.inflate(mContext, R.layout.item_chat_image_right, parent);
            mImageView = view.findViewById(R.id.image_message);
            mFlShadow = view.findViewById(R.id.fl_image_message_shadow);
            mShadowView = view.findViewById(R.id.image_message_shadow);
            mProgress = view.findViewById(R.id.image_progress);
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

    @Override
    public void onViewAttach() {
        super.onViewAttach();
        registerListener();
    }

    @Override
    public void onViewDetach() {
        super.onViewDetach();
        if (mMaxMessage != null) {
            ChatAttachmentManager.getInstance().unRegisterListener(mMaxMessage.msgId());
        }
    }

    /**
     * 展示进度
     */
    private void showImageProgress() {
        if (mMaxMessage == null) {
            mFlShadow.setVisibility(View.GONE);
            return;
        }
        long msgId = mMaxMessage.msgId();
        int percent = mProgressCache.get(msgId, -1);
        if (percent >= 100) {
            mProgressCache.remove(msgId);
            mFlShadow.setVisibility(View.GONE);
            return;
        }
        if (percent == -1) {
            mFlShadow.setVisibility(View.GONE);
        } else {
            mFlShadow.setVisibility(View.VISIBLE);
            mProgress.setText(String.valueOf(percent));
        }
    }

    /**
     * 展示图片
     */
    private void showPic() {
        registerListener();
        if (mMaxMessage == null || mMaxMessage.contentType() != BMXMessage.ContentType.Image) {
            BMImageLoader.getInstance().display(mImageView, "");
            return;
        }
        final BMXImageAttachment body = BMXImageAttachment.dynamic_cast(mMaxMessage.attachment());
        if (body == null) {
            BMImageLoader.getInstance().display(mImageView, "");
            return;
        }
        RelativeLayout.LayoutParams imgLayoutParams = null;
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
                    imgLayoutParams = new RelativeLayout.LayoutParams((int)maxLength,
                            (int)(maxLength / diff));
                } else {
                    // 宽高比大于最大宽高比 按最大宽高比展示
                    imgLayoutParams = new RelativeLayout.LayoutParams((int)maxLength, (int)minLength);
                }
            } else if (w == h) {
                imgLayoutParams = new RelativeLayout.LayoutParams((int)maxLength, (int)maxLength);
            } else {
                diff = h / w;
                if (diff < limitDiff) {
                    // 宽高比不超过标准宽高比 小的一边按比例展示 大的一边标准长
                    imgLayoutParams = new RelativeLayout.LayoutParams((int)(maxLength / diff),
                            (int)maxLength);
                } else {
                    // 宽高比大于最大宽高比 按最大宽高比展示
                    imgLayoutParams = new RelativeLayout.LayoutParams((int)minLength, (int)maxLength);
                }
            }
        } else {
            // 宽高比超过标准宽高比 按标准展示
            imgLayoutParams = new RelativeLayout.LayoutParams((int)maxLength,
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
        } else if (!TextUtils.isEmpty(body.thumbnailUrl())) {
            picUrl = body.thumbnailUrl();
            BMImageLoader.getInstance().display(mImageView, picUrl, mImageConfig);
        } else if (!TextUtils.isEmpty(body.url())) {
            picUrl = body.url();
            BMImageLoader.getInstance().display(mImageView, picUrl, mImageConfig);
        } else {
            BMImageLoader.getInstance().display(mImageView, "", mImageConfig);
            ChatManager.getInstance().downloadAttachment(mMaxMessage);
        }
        showImageProgress();
    }

    /**
     * 注册上传下载监听
     */
    private void registerListener() {
        if (mMaxMessage == null) {
            return;
        }
        boolean register = false;
        BMXImageAttachment body = BMXImageAttachment.dynamic_cast(mMaxMessage.attachment());
        boolean notExit = body != null
                && (TextUtils.isEmpty(body.path()) || !new File(body.path()).exists());
        if (body != null
                && (!TextUtils.isEmpty(body.thumbnailUrl()) || !TextUtils.isEmpty(body.url()))) {
            // 有缩略图 不需要下载
            notExit = false;
        }
        long msgId = mMaxMessage.msgId();
        if (mItemPos == ITEM_RIGHT) {
            BMXMessage.DeliveryStatus sendStatus = mMaxMessage.deliveryStatus();
            register = sendStatus != null && sendStatus != BMXMessage.DeliveryStatus.Deliveried
                    && sendStatus != BMXMessage.DeliveryStatus.Failed || notExit;
        } else if (mItemPos == ITEM_LEFT) {
            register = notExit;
        }
        if (register) {
            if (mProgressCache.get(msgId, -1) == -1) {
                mProgressCache.put(msgId, 0);
            }
            ChatAttachmentManager.getInstance().registerListener(msgId, listener);
        } else {
            mProgressCache.remove(msgId);
        }
    }
}
