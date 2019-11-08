/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.maxim.im.scan.camera;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;

/**
 * A class which deals with reading, parsing, and setting the camera parameters
 * which are used to configure the camera hardware.
 */
final class CameraConfigurationManager {

    private static final String TAG = "CameraConfiguration";

    private final Context context;
    private Point screenResolution;
    private Point cameraResolution;

    CameraConfigurationManager(Context context) {
        this.context = context;
    }

    /**
     * Reads, one time, values from the camera that are needed by the app.
     */
    void initFromCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point theScreenResolution = new Point();
        display.getSize(theScreenResolution);
        screenResolution = theScreenResolution;
        Log.i(TAG, "Screen resolution: " + screenResolution);

        Point screenResolutionForCamera = new Point();
        screenResolutionForCamera.x = screenResolution.x;
        screenResolutionForCamera.y = screenResolution.y;
        if (screenResolution.x < screenResolution.y) {
            screenResolutionForCamera.x = screenResolution.y;
            screenResolutionForCamera.y = screenResolution.x;
        }
        cameraResolution = CameraConfigurationUtils.findBestPreviewSizeValue(
                parameters, screenResolutionForCamera);
        Log.i(TAG, "Camera resolution: " + cameraResolution);
    }

    void setDesiredCameraParameters(Camera camera, boolean safeMode) {
        Camera.Parameters parameters = camera.getParameters();

        if (parameters == null) {
            Log.w(TAG,
                    "Device error: no camera parameters are available. Proceeding without configuration.");
            return;
        }

        Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

        if (safeMode) {
            Log.w(TAG,
                    "In camera config safe mode -- most settings will not be honored");
        }
        setDisplayOrientation(camera, 90);
        initializeTorch(parameters, safeMode);

        // CameraConfigurationUtils.setFocus(parameters,
        // prefs.getBoolean(Constants.KEY_AUTO_FOCUS, true),
        // prefs.getBoolean(Constants.KEY_DISABLE_CONTINUOUS_FOCUS, true),
        // safeMode);
        CameraConfigurationUtils.setFocus(parameters, true, true, safeMode);

        if (!safeMode) {
            // if (prefs.getBoolean(Constants.KEY_INVERT_SCAN, false)) {
            // CameraConfigurationUtils.setInvertColor(parameters);
            // }

            // if (!prefs.getBoolean(Constants.KEY_DISABLE_BARCODE_SCENE_MODE,
            // true)) {
            CameraConfigurationUtils.setBarcodeSceneMode(parameters);
            // }

            // if (!prefs.getBoolean(Constants.KEY_DISABLE_METERING, true)) {
            CameraConfigurationUtils.setVideoStabilization(parameters);
            CameraConfigurationUtils.setFocusArea(parameters);
            CameraConfigurationUtils.setMetering(parameters);
            // }

        }
        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);

        Log.i(TAG, "Final camera parameters: " + parameters.flatten());

        camera.setParameters(parameters);

        Camera.Parameters afterParameters = camera.getParameters();
        Camera.Size afterSize = afterParameters.getPreviewSize();

        if (afterSize != null
                && (cameraResolution.x != afterSize.width || cameraResolution.y != afterSize.height)) {
            Log.w(TAG, "Camera said it supported preview size "
                    + cameraResolution.x + 'x' + cameraResolution.y
                    + ", but after setting it, preview size is "
                    + afterSize.width + 'x' + afterSize.height);
            cameraResolution.x = afterSize.width;
            cameraResolution.y = afterSize.height;
        }

//        if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi") && !Build.MODEL.equalsIgnoreCase("Redmi Note 4X")) {//判断是不是小米手机
//            ToonLog.log_d("CameraConfigurationManager","Supported:"+parameters.getSupportedFlashModes());
//            String mFlashModesValue = "off,on,auto,torch,red-eye";
//            parameters.setFlashMode(mFlashModesValue);//重新设置闪光灯参数
//            camera.setParameters(parameters);
//        }
    }

    Point getCameraResolution() {
        return cameraResolution;
    }

    Point getScreenResolution() {
        return screenResolution;
    }

    boolean getTorchState(Camera camera) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (parameters != null) {
                String flashMode = parameters.getFlashMode();
                return flashMode != null
                        && (Camera.Parameters.FLASH_MODE_ON.equals(flashMode) || Camera.Parameters.FLASH_MODE_TORCH
                        .equals(flashMode));
            }
        }
        return false;
    }

    protected void setDisplayOrientation(Camera camera, int angle) {

        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod(
                    "setDisplayOrientation", int.class);
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, angle);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    void setTorch(Camera camera, boolean newSetting) {
        Camera.Parameters parameters = camera.getParameters();
        doSetTorch(parameters, newSetting, false);
        camera.setParameters(parameters);
    }

    private void initializeTorch(Camera.Parameters parameters, boolean safeMode) {
        // boolean currentSetting = FrontLightMode.readPref(prefs) ==
        // FrontLightMode.ON;
        doSetTorch(parameters, false, safeMode);
    }

    private void doSetTorch(Camera.Parameters parameters, boolean newSetting,
                            boolean safeMode) {
        CameraConfigurationUtils.setTorch(parameters, newSetting);
        // SharedPreferences prefs = PreferenceManager
        // .getDefaultSharedPreferences(context);
        // if (!safeMode
        // && !prefs.getBoolean(Constants.KEY_DISABLE_EXPOSURE, true)) {
        // CameraConfigurationUtils.setBestExposure(parameters, newSetting);
        // }
    }

}
