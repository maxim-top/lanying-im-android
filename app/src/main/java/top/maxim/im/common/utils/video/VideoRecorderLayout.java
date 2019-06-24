
package top.maxim.im.common.utils.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import top.maxim.im.R;
import top.maxim.im.common.utils.FileConfig;
import top.maxim.im.common.utils.ScreenUtils;

public class VideoRecorderLayout extends RelativeLayout
        implements VideoCameraTakenView.OnViewActionListener, View.OnTouchListener {
    private static final String TAG = VideoRecorderLayout.class.getSimpleName();

    private View closeBtn, retryBtn, okBtn, cameraSwitchBtn;

    private VideoCameraTakenView cameraTakenView;

    private ImageView pictureView, flashView, mImgRecordFocusing;;

    private TextView recordHint;

    private IRecorder2 mRecorder = null;

    private IRecordView mRecordView;

    private VideoSurfaceView mSurfaceView;

    /**
     * 对焦图片宽度
     */
    private int mFocusWidth;

    /**
     * 屏幕宽度
     */
    private int mWindowWidth;

    /**
     * 屏幕高度
     */
    private int mWindowHeight;

    /**
     * 1 video,2 picture
     */
    private int type = -1;

    private int mOrient = 1, mRecordOrient = 1;

    private String outputPath;

    private Bitmap bitmap;

    private long startTime, endTime;

    private boolean isPreview = true;

    /**
     * 是否开启闪光灯
     */
    private boolean isOpenFlash;

    private OnClickListener mClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v == closeBtn) {
                if (mRecordView != null) {
                    mRecordView.onCloseRecord();
                }
            } else if (v == retryBtn) {
                if (outputPath != null) {
                    File file = new File(outputPath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
                mSurfaceView.stopPlay();
                if (type == 1) {
                    LayoutParams layoutParams = (LayoutParams)mSurfaceView.getLayoutParams();
                    layoutParams.width = LayoutParams.MATCH_PARENT;
                    layoutParams.height = LayoutParams.MATCH_PARENT;
                    layoutParams.topMargin = 0;
                    mSurfaceView.setLayoutParams(layoutParams);
                    if (mRecordOrient == 2 || mRecordOrient == 4) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                mSurfaceView.reopenCamera();
                            }
                        });

                    } else {
                        mSurfaceView.reopenCamera();
                    }
                } else {
                    bitmap = null;
                    mSurfaceView.getCamera().startPreview();
                }
                if (isOpenFlash) {
                    // 解决录制结束后第一次照相没有闪光灯问题
                    mSurfaceView.openFlash(false);
                }
                initUIState();
            } else if (v == okBtn) {
                if (mRecordView != null) {
                    int width = 0, height = 0;
                    if (type == 1) {
                        if (mRecordOrient == 1 || mRecordOrient == 3) {
                            width = VideoMediaRecorder.videoHeight;
                            height = VideoMediaRecorder.videoWidth;
                        } else {
                            width = VideoMediaRecorder.videoWidth;
                            height = VideoMediaRecorder.videoHeight;
                        }
                        mSurfaceView.stopPlay();
                    } else {
                        if (bitmap != null) {
                            width = bitmap.getWidth();
                            height = bitmap.getHeight();
                        }
                        outputPath = saveBitmap(bitmap);
                    }

                    mRecordView.onFinished(type, outputPath, width, height,
                            (int)(endTime - startTime) / 1000);
                }
            } else if (v == cameraSwitchBtn) {
                if (mSurfaceView != null) {
                    flashView.setVisibility(mSurfaceView.switchCamera() ? VISIBLE : GONE);
                }
            } else if (v == flashView) {
                isOpenFlash = !isOpenFlash;
                if (mSurfaceView != null) {
                    if (isOpenFlash) {
                        // 默认开启照相闪光灯
                        mSurfaceView.openFlash(false);
                    } else {
                        mSurfaceView.closeFlash();
                    }
                    flashView.setImageResource(
                            !isOpenFlash ? R.drawable.sv_flash_close : R.drawable.sv_flash_open);
                }
            }
        }
    };

    private Runnable autoFocus = new Runnable() {

        @Override
        public void run() {
            if (mSurfaceView != null && mSurfaceView.getCamera() != null) {
                mSurfaceView.getCamera().startPreview();
                mSurfaceView.getCamera().focus();
            }
        }
    };

    public VideoRecorderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSurfaceView = (VideoSurfaceView)findViewById(R.id.surface_view);
        closeBtn = findViewById(R.id.sv_close_view);
        retryBtn = findViewById(R.id.sv_result_retry);
        okBtn = findViewById(R.id.sv_result_ok);
        cameraSwitchBtn = findViewById(R.id.sv_camera_switch);
        recordHint = (TextView)findViewById(R.id.sv_record_hint);
        cameraTakenView = (VideoCameraTakenView)findViewById(R.id.sv_progress_view);
        pictureView = (ImageView)findViewById(R.id.sv_picture_show);
        flashView = (ImageView)findViewById(R.id.sv_flash_btn);
        mImgRecordFocusing = (ImageView)findViewById(R.id.img_record_focusing);
        cameraTakenView.setActionListener(VideoRecorderLayout.this);
        closeBtn.setOnClickListener(mClickListener);
        retryBtn.setOnClickListener(mClickListener);
        okBtn.setOnClickListener(mClickListener);
        flashView.setOnClickListener(mClickListener);
        cameraSwitchBtn.setOnClickListener(mClickListener);
        mSurfaceView.setOnTouchListener(this);
        mWindowWidth = ScreenUtils.widthPixels;
        mWindowHeight = ScreenUtils.heightPixels;
        mFocusWidth = ScreenUtils.dp2px(64);
        initUIState();
    }

    public void setRecordView(IRecordView recordView) {
        mRecordView = recordView;
    }

    public void setRecorder(IRecorder2 recorder) {
        mRecorder = recorder;
    }

    private void initUIState() {
        isPreview = true;
        closeBtn.setVisibility(VISIBLE);
        retryBtn.setVisibility(INVISIBLE);
        okBtn.setVisibility(INVISIBLE);
        pictureView.setVisibility(GONE);
        recordHint.setVisibility(VISIBLE);
        cameraTakenView.setVisibility(VISIBLE);
        flashView.setVisibility(VISIBLE);
        setCameraSwitchVisible();
    }

    private void setCameraSwitchVisible() {
        cameraSwitchBtn.setVisibility(VISIBLE);
        int toDegrees = (mOrient - 1) * 90;
        cameraSwitchBtn.setRotation(toDegrees);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Rect rect = new Rect();
        findViewById(R.id.sv_press_view).getGlobalVisibleRect(rect);
        if (mRecordView == null || rect.contains((int)event.getX(), (int)event.getY())) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                removeCallbacks(autoFocus);
                // 5s后自动聚焦
                postDelayed(autoFocus, 5000);
                // 检测是否手动对焦
                if (checkCameraFocus(event))
                    return true;
                break;
        }
        return false;
    }

    /**
     * 手动对焦
     *
     * @param event
     * @return
     */
    private boolean checkCameraFocus(MotionEvent event) {
        if (mSurfaceView.getCamera() == null) {
            return false;
        }
        mImgRecordFocusing.setVisibility(View.GONE);
        int[] location = new int[2];
        Rect touchRect = CameraUtils.calculateTapArea(mFocusWidth, mFocusWidth, 1f, event.getRawX(),
                event.getRawY(), location[0], location[0] + mWindowWidth, location[1],
                location[1] + mWindowHeight);

        if (!CameraUtils.manualFocus(new Camera.AutoFocusCallback() {

            @Override
            public void onAutoFocus(boolean success, Camera camera) {

                mImgRecordFocusing.setVisibility(View.GONE);

            }
        }, mSurfaceView.getCamera(), touchRect)) {
            mImgRecordFocusing.setVisibility(View.GONE);
            return false;
        }
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)mImgRecordFocusing
                .getLayoutParams();
        int left = (int)(event.getRawX() - (mFocusWidth / 2));

        int top = (int)(event.getRawY() - (mFocusWidth / 2));
        if (left < 0)
            left = 0;
        else if (left + mFocusWidth > mWindowWidth)
            left = mWindowWidth - mFocusWidth;

        lp.leftMargin = left;
        lp.topMargin = top;
        mImgRecordFocusing.setLayoutParams(lp);
        mImgRecordFocusing.setVisibility(View.VISIBLE);

        Animation mFocusAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.record_focus);

        mImgRecordFocusing.startAnimation(mFocusAnimation);
        return true;
    }

    public boolean onStartRecord() {
        boolean result = false;
        if (mRecorder != null) {
            if (isOpenFlash) {
                mSurfaceView.openFlash(true);
            }
            startTime = System.currentTimeMillis();
            mRecordOrient = mOrient;
            result = mRecorder.startRecord(mSurfaceView.getCamera(), mSurfaceView.getWidth(),
                    mSurfaceView.getHeight(), mRecordOrient);
            if (result) {
                flashView.setVisibility(GONE);
                cameraSwitchBtn.setVisibility(GONE);
                recordHint.setVisibility(GONE);
                isPreview = false;
            }
        }
        return result;
    }

    @Override
    public void onTakePicture() {
        if (isOpenFlash) {
            mSurfaceView.openFlash(false);
        }
        isPreview = false;
        mRecordOrient = mOrient;
        cameraTakenView.setVisibility(GONE);
        recordHint.setVisibility(GONE);
        closeBtn.setVisibility(GONE);
        mRecorder.takePicture(mSurfaceView.getCamera(), new IRecorder2.ITakePictureCallback() {
            @Override
            public void onPicture(Bitmap bmp) {
                buttonAppear();
                type = 2;
                bitmap = bmp;

                retryBtn.setVisibility(VISIBLE);
                okBtn.setVisibility(VISIBLE);
                cameraSwitchBtn.setVisibility(GONE);
                flashView.setVisibility(GONE);
                recordHint.setVisibility(GONE);
                pictureView.setVisibility(VISIBLE);
                pictureView.setImageBitmap(bmp);
            }
        }, mRecordOrient);
    }

    private void buttonAppear() {
        int left = retryBtn.getLeft() + retryBtn.getMeasuredWidth() / 2;
        int right = okBtn.getLeft() + okBtn.getMeasuredWidth() / 2;
        TranslateAnimation toLeft = new TranslateAnimation(getWidth() / 2 - left, 0, 0, 0);
        TranslateAnimation toRight = new TranslateAnimation(getWidth() / 2 - right, 0, 0, 0);
        toLeft.setFillBefore(true);
        toRight.setFillBefore(true);
        toLeft.setDuration(240);
        toRight.setDuration(240);
        retryBtn.startAnimation(toLeft);
        okBtn.startAnimation(toRight);
    }

    @Override
    public void onStopRecord() {
        if (mRecorder != null && mSurfaceView.getCamera() != null) {
            final String path = mRecorder.endRecord(mSurfaceView.getCamera());

            if (path == null) {
                flashView.setVisibility(VISIBLE);
                setCameraSwitchVisible();
                recordHint.setVisibility(VISIBLE);
                // ToastUtil.showTextViewPrompt("录制视频过短，请重新录制");
                isPreview = true;
                mSurfaceView.getCamera().startPreview();
            } else {
                type = 1;
                outputPath = path;
                if (!new File(outputPath).exists() || new File(outputPath).length() <= 0) {
                    mRecordView.onFinished(type, outputPath, 0, 0, 0);
                    return;
                }
                buttonAppear();
                closeBtn.setVisibility(GONE);
                retryBtn.setVisibility(VISIBLE);
                okBtn.setVisibility(VISIBLE);
                recordHint.setVisibility(GONE);
                flashView.setVisibility(GONE);
                recordHint.setVisibility(GONE);
                cameraSwitchBtn.setVisibility(GONE);
                cameraTakenView.setVisibility(GONE);
                mImgRecordFocusing.setVisibility(View.GONE);

                endTime = System.currentTimeMillis();

                LayoutParams layoutParams = (LayoutParams)mSurfaceView.getLayoutParams();
                if (mRecordOrient == 2 || mRecordOrient == 4) {
                    int surfaceHeight = VideoMediaRecorder.videoHeight * getWidth()
                            / VideoMediaRecorder.videoWidth;
                    layoutParams.height = surfaceHeight;
                    layoutParams.topMargin = (getHeight() - surfaceHeight) / 2;
                } else {
                    layoutParams.height = LayoutParams.MATCH_PARENT;
                }
                mSurfaceView.setLayoutParams(layoutParams);
                mSurfaceView.playVideo(path);
            }
        }
    }

    @Override
    public long getRecordDuration() {
        return 10000;
    }

    public void requestOrient(int originOrient, int orient) {
        // if (mRecorder == null || !mRecorder.isRecording()) {
        if (isPreview) {
            int toDegrees = (orient - 1) * 90;
            cameraSwitchBtn.setRotation(toDegrees);
        }
        mOrient = orient;
    }

    String saveBitmap(Bitmap bmp) {
        if (bmp == null) {
            return null;
        }

        String path = FileConfig.DIR_APP_CACHE_CAMERA + "/"
                + System.currentTimeMillis() + ".jpg";

        FileOutputStream fos = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                // TODO: 如果没有mkdirs 会报IOException : no such file or directory
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);

        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage() + "requestOrient failed");
            return null;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage() + "requestOrient failed");
            return null;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage() + "requestOrient failed");
                }
            }
        }
        return path;
    }
}
