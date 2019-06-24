
package top.maxim.im.common.utils.video;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import top.maxim.im.R;
import top.maxim.im.common.utils.ToastUtil;

public class VideoSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private MediaPlayer mPlayer;

    private CameraWrapper mCamera = null;

    private boolean isBackCamera = true;

    public VideoSurfaceView(Context context) {
        this(context, null);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public boolean switchCamera() {
        stopPlay();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        boolean backCamera = !isBackCamera;
        openCamera(backCamera);
        return backCamera;
    }

    public boolean openFlash(boolean isVideo) {
        if (mCamera != null) {
            return mCamera.enableFlash(isVideo);
        }
        return false;
    }

    public void closeFlash() {
        if (mCamera != null) {
            mCamera.closeFlash();
        }
    }

    public boolean isFlash() {
        if (mCamera != null) {
            return mCamera.isFlash();
        }
        return false;
    }

    CameraWrapper getCamera() {
        return mCamera;
    }

    void openCamera(boolean isback) {
        CameraWrapper wrapper = null;
        try {
            wrapper = CameraWrapper.openCamera(isback);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (wrapper == null) {
            ToastUtil.showTextViewPrompt(R.string.tip_no_camera_or_reject);
            ((Activity)getContext()).finish();
            return;
        }
        isBackCamera = isback;
        Camera camera = wrapper.camera;
        try {
            camera.setDisplayOrientation(wrapper.getDegree());
            CameraUtils.initCamera(camera, getWidth(), getHeight());
            camera.setPreviewDisplay(getHolder());
            // 开启预览
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

        wrapper.focus();
        mCamera = wrapper;
    }

    void reopenCamera() {
        openCamera(isBackCamera);
    }

    void playVideo(String path) {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        mPlayer.reset();
        try {
            mPlayer.setLooping(true);
            mPlayer.setDataSource(path);
            getHolder().setKeepScreenOn(true);
            mPlayer.setDisplay(getHolder());
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void stopPlay() {
        if (mPlayer != null) {
            try {
                mPlayer.stop();
                mPlayer.reset();
                mPlayer.release();
                mPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera(isBackCamera);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera != null && mCamera.camera != null) {
            CameraUtils.initCamera(mCamera.camera, width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPlay();
        if (mCamera != null) {
            try {
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
