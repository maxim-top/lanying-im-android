
package top.maxim.im.common.utils;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

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
}
