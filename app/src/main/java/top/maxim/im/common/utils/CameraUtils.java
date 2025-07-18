
package top.maxim.im.common.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.List;

import top.maxim.im.common.base.BaseActivity;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.utils.permissions.PermissionsConstant;
import top.maxim.im.common.utils.permissions.PermissionsMgr;
import top.maxim.im.common.utils.permissions.PermissionsResultAction;

/**
 * Description : 相机工具类 Created by Mango on 2018/11/11.
 */
public class CameraUtils {

    private static CameraUtils mInstance;

    /**
     * 头像裁剪相关
     **/
    private String IMAGE_UNSPECIFIED = "image/*";

    private CameraUtils() {

    }

    public static CameraUtils getInstance() {
        if (mInstance == null) {
            synchronized (CameraUtils.class) {
                if (mInstance == null) {
                    mInstance = new CameraUtils();
                }
            }
        }
        return mInstance;
    }

    /**
     * 得到当前拍照的图片名称
     *
     * @return 返回照片名称
     */
    public String getCameraName() {
        return "c" + System.currentTimeMillis();
    }

    /**
     * 拍照
     *
     * @param activity 上下文
     * @param fileDir 图片保存路径
     * @param filepath 图片保存路径(包含图片名称)
     */
    public void takePhoto(final String fileDir, final String filepath, final Activity activity,
            final int resultCode) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (activity instanceof BaseTitleActivity || activity instanceof BaseActivity) {
                String[] permissions = new String[] {
                        PermissionsConstant.CAMERA
                };
                if (!PermissionsMgr.getInstance().hasAllPermissions(activity, permissions)) {
                    PermissionsMgr.getInstance().requestPermissionsIfNecessaryForResult(activity,
                            permissions, new PermissionsResultAction() {

                                @Override
                                public void onGranted(List<String> perms) {
                                    openCamera(fileDir, filepath, activity, resultCode);
                                }

                                @Override
                                public void onDenied(List<String> perms) {
                                }
                            });
                } else {
                    openCamera(fileDir, filepath, activity, resultCode);
                }
            } else {
            }
        } else {
            if (cameraIsCanUse()) {
                openCamera(fileDir, filepath, activity, resultCode);
            } else {
            }
        }
    }

    /**
     * 相机是否可用
     * 
     * @return boolean
     */
    public boolean cameraIsCanUse() {
        boolean isCanUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters(); // 针对魅族手机
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            isCanUse = false;
        }
        if (mCamera != null) {
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
                return isCanUse;
            }
        }
        return isCanUse;
    }

    /**
     * 拍照
     *
     * @param activity 上下文
     * @param fileDir 图片保存路径
     * @param filepath 图片保存路径(包含图片名称)
     */
    private void openCamera(String fileDir, String filepath, Activity activity, int resultCode) {
        File file = new File(fileDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        File imageFile = new File(filepath);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // 第二参数是在manifest.xml定义 provider的authorities属性
            Uri photoURI = FileProvider.getUriForFile(activity,
                    activity.getPackageName() + ".fileProvider", imageFile);
            // 将拍取的照片保存到指定URI
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        } else {
            // 将拍取的照片保存到指定URI
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        }
        activity.startActivityForResult(intent, resultCode);
    }

    /**
     * 跳转相册选择图片
     * @param activity  上下文
     * @param resultCode 请求码
     */
    public void takeGalley(Activity activity, int resultCode) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(intent, resultCode);
    }

    /**
     * 跳转相册选择视频
     * @param activity  上下文
     * @param resultCode 请求码
     */
    public void takeGalleyForVideo(Activity activity, int resultCode) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(intent, resultCode);
    }

    /**
     * 裁剪头像(控制生成图片大小)
     *
     * @param originalFile 路径
     * @param targetFile   生成新的图片
     * @param filepath     生成图片保存的路径
     * @param picCropWidth 生成图片的宽
     * @param picCropHigh  生成图片的高
     */
    public void startPhotoZoom(File originalFile, Uri originalUri, Uri targetFile, String filepath, int picCropWidth,
                               int picCropHigh, Activity activity, int resultCode) {
        File file = new File(filepath);
        if (!file.exists()) {
            file.mkdirs();
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(originalUri, IMAGE_UNSPECIFIED);
        intent.putExtra("crop", "true");
        // 是否允许缩放
        intent.putExtra("scale", "true");
        intent.putExtra("scaleUpIfNeeded", true);
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", picCropWidth);
        intent.putExtra("outputY", picCropHigh);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, targetFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            activity.grantUriPermission(packageName, targetFile, Intent.FLAG_GRANT_WRITE_URI_PERMISSION| Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        ComponentName componentName = intent.resolveActivity(activity.getPackageManager());
        if (componentName != null) {
            activity.startActivityForResult(intent, resultCode);
        }
    }

    /**
     * 裁剪头像(控制生成图片大小)
     *
     * @param originalFile 路径
     * @param saveUri      生成新的图片
     * @param filepath     生成图片保存的路径
     * @param picCropWidth 生成图片的宽
     * @param picCropHigh  生成图片的高
     */
    public void startPhotoZoom(File originalFile, Uri saveUri, String filepath, int aspectX, int aspectY,
                               int picCropWidth, int picCropHigh, Activity activity, int resultCode) {
        File file = new File(filepath);
        if (!file.exists()) {
            file.mkdirs();
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(getImageContentUri(activity, originalFile), IMAGE_UNSPECIFIED);
        intent.putExtra("crop", "true");
        // 是否允许缩放
        intent.putExtra("scale", "true");
        intent.putExtra("scaleUpIfNeeded", true);
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", picCropWidth);
        intent.putExtra("outputY", picCropHigh);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, saveUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        activity.startActivityForResult(intent, resultCode);
    }

    /**
     * 兼容7.0系统 content 和 file uri
     *
     * @param context   上下文
     * @param imageFile 图片文件目录
     * @return 可以使用的uri
     */
    public static Uri getImageContentUri(Context context, File imageFile) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            String filePath = imageFile.getAbsolutePath();
            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ", new String[]{filePath}, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                    Uri baseUri = Uri.parse("content://media/external/images/media");
                    return Uri.withAppendedPath(baseUri, "" + id);
                } else {
                    if (imageFile.exists()) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.DATA, filePath);
                        return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            return Uri.fromFile(imageFile);
        }
        return null;
    }
}
