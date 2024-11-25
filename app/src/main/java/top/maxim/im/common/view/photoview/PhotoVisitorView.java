
package top.maxim.im.common.view.photoview;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import im.floo.BMXDataCallBack;
import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXMessage;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.ChatManager;
import top.maxim.im.common.bean.PhotoViewBean;
import top.maxim.im.common.utils.ScreenUtils;
import top.maxim.im.common.utils.dialog.CustomDialog;
import top.maxim.im.common.utils.permissions.PermissionsConstant;
import top.maxim.im.common.utils.permissions.PermissionsMgr;
import top.maxim.im.common.utils.permissions.PermissionsResultAction;
import top.maxim.im.common.view.BMImageLoader;
import top.maxim.im.common.view.ImageRequestConfig;
import top.maxim.im.contact.view.ForwardMsgRosterActivity;
import top.maxim.im.message.interfaces.MsgAttachmentCallback;
import top.maxim.im.message.utils.ChatAttachmentManager;
import top.maxim.im.message.utils.ChatUtils;

public class PhotoVisitorView extends RelativeLayout {

    private Context mContext;

    /* 普通图原图 */
    private PhotoView mPhotoView;

    /* 图片信息 */
    private PhotoViewBean mPhotoViewBean;

    private String mUrl;

    /* 图片配置 */
    private ImageRequestConfig mConfig;
    private MsgAttachmentCallback listener = new MsgAttachmentCallback() {
        @Override
        public void onProgress(long msgId, int percent) {}

        @Override
        public void onFinish(long msgId) {
            showPhoto(0);
        }

        @Override
        public void onFail(long msgId) {}
    };

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

    public void copyImageToGallery(Context context, Uri imageUri) {
        String extName = imageUri.getPath().replaceAll(".*\\.","");
        // Generating a file name
        String filename = System.currentTimeMillis() + "." + extName;
        if (extName.length() > 4){
            filename = System.currentTimeMillis() + ".jpg";
        }
        // Output stream
        OutputStream fos = null;
        try {
            InputStream is = context.getContentResolver().openInputStream(imageUri);
            // 获取图片的MIME类型
            String mimeType = context.getContentResolver().getType(imageUri);

            String newPath;
            // For devices running Android >= Q
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Getting the contentResolver
                ContentResolver resolver = context.getContentResolver();

                // Content resolver will process the contentValues
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                // Inserting the contentValues to contentResolver and getting the Uri
                Uri newImageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                newPath = newImageUri.getPath();
                // Opening an output stream with the Uri that we got
                fos = newImageUri != null ? resolver.openOutputStream(newImageUri) : null;
            } else {
                // For devices running on Android < Q
                File imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File image = new File(imagesDir, filename);
                newPath = image.getPath();
                fos = new FileOutputStream(image);
            }

            // Finally writing the bitmap to the output stream that we opened
            if (fos != null) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                fos.flush();
                fos.close();
            }
            MediaScannerConnection.scanFile(mContext, new String[]{newPath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String s, Uri uri) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ContentValues createImageContentValues(Context context, String name, String mimeType) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
        return values;
    }

    private void showSaveMenu() {
        final CustomDialog dialog = new CustomDialog();
        LinearLayout ll = new LinearLayout(mContext);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // 保存到相册
        TextView delete = new TextView(mContext);
        delete.setPadding(ScreenUtils.dp2px(15), 0, ScreenUtils.dp2px(15), 0);
        delete.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        delete.setTextColor(mContext.getResources().getColor(R.color.color_black));
        delete.setBackgroundColor(mContext.getResources().getColor(R.color.color_white));
        delete.setText(mContext.getString(R.string.save_to_album));
        delete.setOnClickListener(v -> {
            dialog.dismiss();
            copyImageToGallery(mContext, Uri.parse(mUrl));
        });
        ll.addView(delete, params);
        dialog.setCustomView(ll);
        dialog.showDialog((Activity)mContext);
    }

    class ItemLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            String[] permissions = new String[] {
                    PermissionsConstant.WRITE_STORAGE
            };
            if (!PermissionsMgr.getInstance().hasAllPermissions(mContext, permissions)) {
                PermissionsMgr.getInstance().requestPermissionsIfNecessaryForResult((Activity) mContext,
                        permissions, new PermissionsResultAction() {

                            @Override
                            public void onGranted(List<String> perms) {
                                showSaveMenu();
                            }

                            @Override
                            public void onDenied(List<String> perms) {
                            }
                        });
            } else {
                showSaveMenu();
            }

            return true;
        }
    }

    /**
     * 初始化view
     *
     * @param context 上下文
     */
    private void initView(Context context) {
        View view = inflate(context, R.layout.photo_visitor_view, this);
        mPhotoView = view.findViewById(R.id.photoView);
        mPhotoView.setOnLongClickListener(new ItemLongClickListener());
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
            } else if (!TextUtils.isEmpty(mPhotoViewBean.getThumbLocalPath())
                    && new File(mPhotoViewBean.getThumbLocalPath()).exists()) {
                loadUrl = "file://" + mPhotoViewBean.getThumbLocalPath();
            } else if (!TextUtils.isEmpty(mPhotoViewBean.getHttpUrl())) {
                loadUrl = mPhotoViewBean.getHttpUrl();
            } else if (!TextUtils.isEmpty(mPhotoViewBean.getThumbHttpUrl())) {
                loadUrl = mPhotoViewBean.getThumbHttpUrl();
            }
        }

        if (!TextUtils.isEmpty(mPhotoViewBean.getLocalPath())
                && !new File(mPhotoViewBean.getLocalPath()).exists()) {
            BMXDataCallBack<BMXMessage> callBack = new BMXDataCallBack<BMXMessage>() {
                @Override
                public void onResult(BMXErrorCode code, BMXMessage data) {
                    ChatManager.getInstance().downloadAttachment(data);
                    ChatAttachmentManager.getInstance().registerListener(data.msgId(), listener);
                }
            };
            ChatManager.getInstance().getMessage(mPhotoViewBean.getMsgId(), callBack);
        }

        mPhotoView.setVisibility(View.VISIBLE);
        String finalLoadUrl = loadUrl;
        BMImageLoader.getInstance().display(mPhotoView, loadUrl, mConfig, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mUrl = finalLoadUrl;
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
