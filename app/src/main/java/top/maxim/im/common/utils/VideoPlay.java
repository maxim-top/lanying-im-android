
package top.maxim.im.common.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.RawRes;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Description : 视频播放帮助类
 */
public class VideoPlay
        implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener,
        SurfaceHolder.Callback, MediaPlayer.OnErrorListener {
    private static final String TAG = VideoPlay.class.getSimpleName();

    public boolean isPrepare;

    private SurfaceHolder mSurfaceHolder;

    /* 视频播放player */
    private MediaPlayer mMediaPlayer;

    /* 视频播放监听 */
    private OnPlayListener mOnPlayListener;

    /* 系统声音管理类 */
    private AudioManager mAudioManager;

    /* 系统声音改变监听 */
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;

    public VideoPlay(Context context, SurfaceView surfaceView) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.setKeepScreenOn(true);
        mSurfaceHolder.addCallback(this);
        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        initListener();
    }

    public void setOnPlayListener(OnPlayListener onPlayListener) {
        this.mOnPlayListener = onPlayListener;
    }

    public void setNotVolume() {
        mMediaPlayer.setVolume(0, 0);
    }

    public boolean isPlaying() {
        if (mMediaPlayer == null) {
            return false;
        }
        return mMediaPlayer.isPlaying();
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    /**
     * 进度滑动
     *
     * @param msec
     */
    public void seekTo(int msec) {
        mMediaPlayer.seekTo(msec);
    }

    /**
     * 进度滑动
     *
     * @param msec
     */
    public void seekTo(int msec, MediaPlayer.OnSeekCompleteListener listener) {
        mMediaPlayer.seekTo(msec);
        mMediaPlayer.setOnSeekCompleteListener(listener);
    }

    /**
     * 语音焦点变化监听
     */
    private void initListener() {
        // 系统声音改变监听
        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                Log.d(TAG, "Audio Focus Change=" + focusChange);
                // 暂时失去了音频焦点，但很快会重新得到焦点
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    Log.d(TAG, focusChange + "");
                    // 你已经得到焦点了
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    // 永久失去焦点
                    Log.d(TAG, focusChange + "");
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    Log.d(TAG, focusChange + "");
                }
            }
        };
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.d(TAG, "buffering update percent:" + percent);
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.d(TAG, "video size changed width:" + width + "height:" + height);
        if (width != 0 && height != 0) {
            if (mOnPlayListener != null) {
                mOnPlayListener.changeSurfaceViewSize(width, height);
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mOnPlayListener != null && isPrepare) {
            mOnPlayListener.onPlayComplete();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepare = true;
        int mVideoWidth = mp.getVideoWidth();
        int mVideoHeight = mp.getVideoHeight();
        if (mVideoHeight != 0 && mVideoWidth != 0) {
            if (mOnPlayListener != null) {
                mOnPlayListener.changeSurfaceViewSize(mVideoWidth, mVideoHeight);
            }
        }
        if (mOnPlayListener != null) {
            mOnPlayListener.onPrepare();
        }
        Log.d(TAG, "prepared width=" + mVideoWidth + ";height=" + mVideoHeight);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + "surface created error");
        }
        Log.d(TAG, "surface created");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surface changed:width=" + width + ";height=" + height);
        if (mOnPlayListener != null) {
            mOnPlayListener.changeSurfaceViewSize(width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        pause();
        Log.d(TAG, "surface destroyed");
    }

    /**
     * 设置视频路径
     *
     * @param videoPath 视频路径
     */
    public void setVideoPath(String videoPath) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(videoPath);
            mMediaPlayer.prepare();
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 设置视频路径
     *
     */
    public void setVideoPath(@RawRes int videoRes) {
        try {
            mMediaPlayer.reset();
            AssetFileDescriptor afd = AppContextUtils.getAppContext().getResources()
                    .openRawResourceFd(videoRes); // 注意这里的区别
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mMediaPlayer.prepare();
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始播放
     */
    public void start() {
        boolean isFocus = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        if (isFocus) {
            try {
                mMediaPlayer.start();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage() + "start is failed");
            }
        } else {
            Log.d(TAG, "request Audio Focus failed.");
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                isPrepare = false;
            }
            resetMusic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放视频完成后放弃焦点获取
     */
    private void resetMusic() {
        if (mAudioManager != null && mOnAudioFocusChangeListener != null) {
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.stop();
        mp.reset();
        if (mOnPlayListener != null) {
            mOnPlayListener.onError();
        }
        return false;
    }

    public interface OnPlayListener {

        void onPlayComplete();

        void changeSurfaceViewSize(int width, int height);

        void onError();

        void onPrepare();

    }
}
