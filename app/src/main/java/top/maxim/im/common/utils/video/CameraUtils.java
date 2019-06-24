
package top.maxim.im.common.utils.video;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CameraUtils {

    // private static final int AREA_PER_1000 = 400;

    // private static final double MAX_ASPECT_DISTORTION = 0.15;
    //
    // private static final int MIN_PREVIEW_PIXELS = 640 * 480; // normal screen

    static void initCamera(Camera camera, int width, int height) {
        Camera.Parameters params = camera.getParameters();
        params.set("orientation", "portrait");
        params.setPictureFormat(ImageFormat.JPEG);
        params.set("jpeg-quality", 100);
        setBestPreviewFPS(params, 15, 30);
        setFocus(params);
        Camera.Size size = getOptimalSize(params.getSupportedPreviewSizes(), width, height);
        if (size != null) {
            params.setPreviewSize(size.width, size.height);
        }
        size = getOptimalSize(params.getSupportedPictureSizes(), width, height);
        if (size != null) {
            params.setPictureSize(size.width, size.height);
        }
        camera.setParameters(params);
    }

    public static void setFocus(Camera.Parameters parameters) {
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_MACRO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        }
    }

    public static void setVideoStabilization(Camera.Parameters parameters) {
        if (parameters.isVideoStabilizationSupported()) {
            parameters.setVideoStabilization(true);
        }
    }

    public static void setBestPreviewFPS(Camera.Parameters parameters, int minFPS, int maxFPS) {
        List<int[]> supportedPreviewFpsRanges = parameters.getSupportedPreviewFpsRange();
        if (supportedPreviewFpsRanges != null && !supportedPreviewFpsRanges.isEmpty()) {
            int[] suitableFPSRange = null;
            for (int[] fpsRange : supportedPreviewFpsRanges) {
                int thisMin = fpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
                int thisMax = fpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
                if (thisMin >= minFPS * 1000 && thisMax <= maxFPS * 1000) {
                    suitableFPSRange = fpsRange;
                    break;
                }
            }
            if (suitableFPSRange != null) {
                int[] currentFpsRange = new int[2];
                parameters.getPreviewFpsRange(currentFpsRange);
                if (!Arrays.equals(currentFpsRange, suitableFPSRange)) {
                    parameters.setPreviewFpsRange(
                            suitableFPSRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                            suitableFPSRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
                }
            }
        }
    }

    private static List<Camera.Size> getOptimalSizeList(List<Camera.Size> supportSize,
            int screenWidth, int screenHeight) {
        int width = screenWidth;
        int height = screenHeight;
        if (screenWidth < screenHeight) {
            width = screenHeight;
            height = screenWidth;
        }
        List<Camera.Size> optimalList = null;
        Camera.Size result = null;
        if (supportSize != null) {
            List<Camera.Size> size_9_18 = new ArrayList<>();
            List<Camera.Size> size_9_16 = new ArrayList<>();
            List<Camera.Size> size_2_3 = new ArrayList<>();
            // List<Camera.Size> size_9_11 = new ArrayList<>(); 归到2：3里
            List<Camera.Size> size_3_4 = new ArrayList<>();
            // List<Camera.Size> size_9_13 = new ArrayList<>();归到3：4里
            List<Camera.Size> size_1_1 = new ArrayList<>();

            float ratio = height / (float)width;

            for (Camera.Size size : supportSize) {
                float tempRatio = size.height / (float)size.width;
                if (tempRatio < 0.46875f) {// small than 9/18 + (9/16 - 9/18)/2
                    size_9_18.add(size);
                } else if (tempRatio < 0.61458f) {// small than 9/16 + (2/3 - 9/16)/2
                    size_9_16.add(size);
                } else if (tempRatio < 0.7083f) {// small than 2/3 + (3/4 - 2/3)/2
                    size_2_3.add(size);
                } else if (tempRatio < 0.8751) {// small than 3/4 + (1 - 3/4)/2
                    size_3_4.add(size);
                } else {
                    size_1_1.add(size);
                }
            }
            if (ratio < 0.46875f && size_9_18.size() > 0) {
                optimalList = size_9_18;
            } else if (ratio < 0.61458f && size_9_16.size() > 0) {
                optimalList = size_9_16;
            } else if (ratio < 0.7083f && size_2_3.size() > 0) {
                optimalList = size_2_3;
            } else if (ratio < 0.8751 && size_3_4.size() > 0) {
                optimalList = size_3_4;
            } else {
                optimalList = size_1_1;
            }
        }
        return optimalList;
    }

    /**
     * 选择可用的预览尺寸
     *
     * @param supportSize
     * @param screenWidth
     * @param screenHeight
     * @return
     */
    static Camera.Size getOptimalSize(List<Camera.Size> supportSize, int screenWidth,
            int screenHeight) {

        List<Camera.Size> optimalList = getOptimalSizeList(supportSize, screenWidth, screenHeight);

        Camera.Size result = null;
        if (optimalList != null) {
            int multi = screenWidth * screenHeight;
            int delta = Integer.MAX_VALUE;
            for (Camera.Size size : optimalList) {
                int temp = Math.abs(size.width * size.height - multi);
                if (temp < delta) {
                    result = size;
                    delta = temp;
                }
            }
        }
        return result;
    }

    /**
     * 选择可用的预览尺寸
     *
     * @param supportVideoSize
     * @param screenWidth
     * @param screenHeight
     * @return
     */
    static Camera.Size getOptimalVideoSize(List<Camera.Size> supportVideoSize, int screenWidth,
            int screenHeight) {

        List<Camera.Size> optimalList = getOptimalSizeList(supportVideoSize, screenWidth,
                screenHeight);
        Camera.Size result = null;
        if (optimalList != null) {
            int delta = Integer.MAX_VALUE;
            for (Camera.Size size : optimalList) {
                int temp = Math.abs(size.width - screenWidth);// 1920 * 1080 ---> 1280 * 720
                if (temp < delta) {
                    result = size;
                    delta = temp;
                }
            }
        }
        return result;
    }

    /**
     * 手动聚焦
     *
     * @param cb
     * @return
     */
    static boolean manualFocus(Camera.AutoFocusCallback cb, CameraWrapper cameraWrapper,
            Rect touchRect) {
        Camera mCamera = cameraWrapper.camera;
        ArrayList<Camera.Area> focusAreas = new ArrayList<>();
        focusAreas.add(new Camera.Area(touchRect, 1000));
        if (mCamera != null) {
            try {
                Camera.Parameters mParameters = mCamera.getParameters();
                mCamera.cancelAutoFocus();
                cameraWrapper.cancelFocus();

                // getMaxNumFocusAreas检测设备是否支持
                if (mParameters.getMaxNumFocusAreas() > 0) {
                    // mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);//
                    // Macro(close-up) focus mode
                    mParameters.setFocusAreas(focusAreas);
                }

                if (mParameters.getMaxNumMeteringAreas() > 0)
                    mParameters.setMeteringAreas(focusAreas);

                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                mCamera.setParameters(mParameters);
                mCamera.autoFocus(cb);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 计算焦点及测光区域
     *
     * @param focusWidth
     * @param focusHeight
     * @param areaMultiple
     * @param x
     * @param y
     * @param previewleft
     * @param previewRight
     * @param previewTop
     * @param previewBottom
     * @return Rect(left, top, right, bottom) : left、top、right、bottom是以显示区域中心为原点的坐标
     *         。 Rect 是以触摸点为中心的一块区域
     */
    public static Rect calculateTapArea(int focusWidth, int focusHeight, float areaMultiple,
            float x, float y, int previewleft, int previewRight, int previewTop,
            int previewBottom) {
        int areaWidth = (int)(focusWidth * areaMultiple);
        int areaHeight = (int)(focusHeight * areaMultiple);
        int centerX = (previewleft + previewRight) / 2;
        int centerY = (previewTop + previewBottom) / 2;
        double unitx = ((double)previewRight - (double)previewleft) / 2000;
        double unity = ((double)previewBottom - (double)previewTop) / 2000;
        int left = clamp((int)(((x - areaWidth / 2) - centerX) / unitx), -1000, 1000);
        int top = clamp((int)(((y - areaHeight / 2) - centerY) / unity), -1000, 1000);
        int right = clamp((int)(left + areaWidth / unitx), -1000, 1000);
        int bottom = clamp((int)(top + areaHeight / unity), -1000, 1000);

        return new Rect(left, top, right, bottom);
    }

    private static int clamp(int x, int min, int max) {
        if (x > max)
            return max;
        if (x < min)
            return min;
        return x;
    }
}
