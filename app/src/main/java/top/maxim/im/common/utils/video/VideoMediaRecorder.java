
package top.maxim.im.common.utils.video;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

import top.maxim.im.common.utils.FileConfig;
import top.maxim.im.message.utils.MessageConfig;

public class VideoMediaRecorder implements IRecorder2 {

    public static int videoWidth = 640, videoHeight = 480;

    private MediaRecorder mRecorder;

    private boolean isRecording = false;

    private String mOutputPath;

    public VideoMediaRecorder() {
        mRecorder = new MediaRecorder();
    }

    private void initMediaRecorder(Camera.Size videoSize, int previewWidth, int previewHeight) {

        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(2);
        mRecorder.setAudioSamplingRate(44100);
        if (videoSize != null) {
            videoWidth = videoSize.width;
            videoHeight = videoSize.height;
        }
        mRecorder.setVideoSize(videoWidth, videoHeight);
        mRecorder.setVideoEncodingBitRate(previewWidth * previewHeight);

        // mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
        // @Override
        // public void onInfo(MediaRecorder mr, int what, int extra) {
        // if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
        // mr.stop();
        // mr.reset();
        // mr.release();
        // mRecorder = null;
        //
        // }/*else if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED){
        //
        // }*/
        // }
        // });
        mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_ERROR_SERVER_DIED) {
                    mr.release();
                    mr.reset();
                    mRecorder = null;
                }
            }
        });
    }

    int getDegrees(CameraWrapper wrapper, int orient) {

        int degree = 0;
        if (wrapper.isBackCamera()) {
            degree = orient == 1 ? 0 : (orient == 2 ? 270 : (orient == 3 ? 180 : 90));
            degree = (degree + wrapper.cameraInfo.orientation) % 360;
        } else {
            if (orient == 1) {
                degree = 270;
            } else if (orient == 3) {
                degree = 90;
            } else if (orient == 4) {
                degree = 180;
            }
        }
        return degree;
    }

    @Override
    public boolean startRecord(CameraWrapper wrapper, int width, int height, int orient) {
        if (wrapper == null || wrapper.camera == null || isRecording) {
            return false;
        }
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
        } else {
            mRecorder.reset();
        }

        File file = new File(FileConfig.DIR_APP_CACHE_VIDEO);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (mOutputPath == null) {
            mOutputPath = FileConfig.DIR_APP_CACHE_VIDEO + "/" + System.currentTimeMillis()
                    + MessageConfig.MediaFormat.VIDEO_FORMAT;
        }
        try {
            mRecorder.setOrientationHint(getDegrees(wrapper, orient));

            Camera.Parameters parameters = wrapper.camera.getParameters();
            CameraUtils.setVideoStabilization(parameters);
            Camera.Size size = CameraUtils.getOptimalVideoSize(parameters.getSupportedVideoSizes(),
                    width, height);

            wrapper.camera.unlock();
            mRecorder.setCamera(wrapper.camera);
            initMediaRecorder(size, width, height);
            mRecorder.setOutputFile(mOutputPath);
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        isRecording = true;
        return true;
    }

    @Override
    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public String endRecord(CameraWrapper wrapper) {
        String ret = isRecording ? mOutputPath : null;
        if (mRecorder != null) {
            try {
                mRecorder.stop();
            } catch (Exception e) {
                ret = null;
                if (mOutputPath != null) {
                    File file = new File(mOutputPath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }

            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
        if (wrapper != null) {
            wrapper.stopPreview();
        }
        isRecording = false;
        return ret;
    }

    @Override
    public void takePicture(final CameraWrapper wrapper, final ITakePictureCallback callback,
            final int orient) {
        wrapper.takePicture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                camera.stopPreview();
                if (callback != null) {
                    callback.onPicture(savePicture(wrapper, data, orient));
                }
            }
        });
    }

    private Bitmap savePicture(CameraWrapper wrapper, byte[] data, int orient) {
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        boolean backCamera = wrapper.isBackCamera();
        int degree = 0;
        if (backCamera) {
            degree = orient == 1 ? 0 : (orient == 2 ? 270 : (orient == 3 ? 180 : 90));
            degree = (degree + wrapper.cameraInfo.orientation) % 360;
        } else {
            degree = orient == 1 ? 270 : (orient == 2 ? 0 : (orient == 3 ? 90 : 180));
        }
        if (degree != 0) {
            Matrix matrix = new Matrix();
            matrix.setRotate(degree, bmp.getWidth() / 2.0f, bmp.getHeight() / 2.0f);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        }
        return bmp;
    }

    @Override
    public long getMaxDuration() {
        return 10000;
    }
}
