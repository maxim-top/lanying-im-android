
package top.maxim.im.common.utils.video;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;

import java.util.List;

public class CameraWrapper {

    Camera camera;

    int cameraId;

    Camera.CameraInfo cameraInfo;

    private boolean isFocusing = false;

    private boolean startPreview;

    private boolean isFlash = false;

    private Handler handler = new Handler(Looper.getMainLooper());

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            focus();
        }
    };

    private CameraWrapper() {

    }

    public static CameraWrapper openCamera(boolean backCamera) {
        Camera camera = null;
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int face = backCamera ? Camera.CameraInfo.CAMERA_FACING_BACK
                : Camera.CameraInfo.CAMERA_FACING_FRONT;
        int id = -1;
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == face) {
                try {
                    id = i;
                    camera = Camera.open(i);
                    camera.setParameters(camera.getParameters());
                } catch (Exception e) {
                    camera = null;
                }
                break;
            }
        }
        CameraWrapper wrapper = null;
        if (camera != null) {
            wrapper = new CameraWrapper();
            wrapper.camera = camera;
            wrapper.cameraInfo = cameraInfo;
            wrapper.cameraId = id;
        }
        return wrapper;
    }

    boolean isBackCamera() {
        return cameraInfo != null && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    public int getDegree() {
        int degrees = 0;
        int result;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public boolean enableFlash(boolean isVideo) {
        if (camera != null && isBackCamera()) {
            Camera.Parameters parameters = camera.getParameters();
            // 获取设备支持的mode
            List<String> flashModes = parameters.getSupportedFlashModes();
            if (!flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)
                    || !flashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                isFlash = false;
                return false;
            }
            if (isVideo) {
                isFlash = true;
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);// 开启
            } else {
                isFlash = true;
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);// 开启
            }
            camera.setParameters(parameters);
            return isFlash;
        }
        return false;
    }

    public void closeFlash() {
        Camera.Parameters parameters = camera.getParameters();
        String flashMode = parameters.getFlashMode();
        if (Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)
                || Camera.Parameters.FLASH_MODE_ON.equals(flashMode)) {
            isFlash = false;
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);// 关闭
            camera.setParameters(parameters);
        }
    }

    public boolean isFlash() {
        return isFlash;
    }

    public void release() {
        handler.removeCallbacks(runnable);
        if (camera != null) {
            camera.cancelAutoFocus();
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
            cameraInfo = null;
            cameraId = -1;
            isFocusing = false;
        }
    }

    public void startPreview() {
        cancelFocus();
        if (camera != null) {
            startPreview = true;
            try {
                camera.startPreview();
                focus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stopPreview() {
        cancelFocus();
        if (camera != null) {
            camera.lock();
            startPreview = false;
            camera.stopPreview();
        }
    }

    public void takePicture(Camera.PictureCallback callback) {
        cancelFocus();
        if (camera != null) {
            camera.takePicture(null, null, callback);
        }
    }

    void cancelFocus() {
        handler.removeCallbacks(runnable);
        try {
            if (isFocusing && camera != null && startPreview) {
                camera.cancelAutoFocus();
            }
            isFocusing = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void focus() {
        if (camera != null && isBackCamera() && !isFocusing && startPreview) {
            isFocusing = true;
            try {
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        camera.cancelAutoFocus();
                        isFocusing = false;
                        handler.postDelayed(runnable, 3000);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
