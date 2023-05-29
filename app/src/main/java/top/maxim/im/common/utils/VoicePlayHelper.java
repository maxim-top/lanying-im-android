
package top.maxim.im.common.utils;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import top.maxim.im.R;

/**
 * Description : 语音播放帮助类 Created by Mango on 2018/11/18.
 */
public class VoicePlayHelper {

    /* 语音播放类标记 */
    private String TAG = "VoicePlayHelper";

    /* 语音文件路径 */
    private String mVoicePath;

    /* 语音播放 MediaPlayer */
    private MediaPlayer mMediaPlayer;

    /* 语音播放完成监听 */
    private OnVoiceFinishListener onVoiceFinishListener;

    private OnVoicePrepareListener mOnVoicePrepareListener;

    private MediaPlayer.OnCompletionListener mOnCompletionListener;

    private MediaPlayer.OnPreparedListener mOnPreparedListener;

    /* 系统声音管理类 */
    private AudioManager mAudioManager;

    /* 系统声音改变监听 */
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;

    /* 距离传感器管理类 */
    private SensorManager mSensorManager;

    /* 距离传感器 */
    private Sensor mSensor;

    /* 距离传感器监听 */
    private SensorEventListener mSensorEventListener;// 距离传感器监听

    /* 记录播放模式 是否是听筒模式 */
    private boolean mIsPhoneMode = false;

    private WeakHandler handler;

    /* 是否注册距离传感器 */
    private boolean mIsRegisterSensor = false;

    private boolean isPrepare;

    public VoicePlayHelper(Activity context) {
        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        handler = new WeakHandler(context);
        initListener();
    }

    /**
     * 初始化语音播放时的监听
     */
    private void initListener() {
        // 系统声音焦点改变监听
        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                Log.d(TAG, "Audio Focus Change=" + focusChange);
                // 暂时失去了音频焦点，但很快会重新得到焦点
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                    }
                    // 你已经得到焦点了
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                        mMediaPlayer.start();
                    }
                    // 永久失去焦点
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    stopVoice();
                }
            }
        };

        // 距离监听
        // 距离监听
        mSensorEventListener = new SensorEventListener() {
            // 记录上次的mode, 在有些机型上回无限次调用此方法，所以增加此变量.
            boolean oldPhoneMode = true;

            @Override
            public void onSensorChanged(SensorEvent event) {
                float range = event.values[0];
                mIsPhoneMode = range < 1.0 && event.sensor.getType() == Sensor.TYPE_PROXIMITY;
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()
                        && oldPhoneMode != mIsPhoneMode) {
                    oldPhoneMode = mIsPhoneMode;
                    stopVoiceNotResetFocus();
                    changePlayMode(mIsPhoneMode);
                    Log.d(TAG, "onSensorChanged PhoneMode:" + mIsPhoneMode);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 切换听筒时候 系统切换速度过慢导致语音开头不完整 所以延时1.5s开始播放
                            if (mIsRegisterSensor) {
                                // 只有注册了传感器之后在播放 防止在后台播放
                                startVoice((Context) handler.getActivity().get(), mVoicePath);
                            }
                        }
                    }, 1500);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        // 语音播放结束
        mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopVoice();
                if (onVoiceFinishListener != null) {
                    onVoiceFinishListener.onFinish();
                }
            }
        };

        mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepare = true;
                if (mOnVoicePrepareListener != null) {
                    mOnVoicePrepareListener.onPrepareListener(mp);
                }
                // 准备完成之后进行播放
                start();
            }
        };
    }

    private void start() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            if (onVoiceFinishListener != null) {
                onVoiceFinishListener.onStart();
            }
        } else {
            isPrepare = false;
        }
    }

    /**
     * 语音播放完毕还原其余声音
     */
    private void startOthersMusic() {
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
    }

    /**
     * 开始播放语音
     *
     * @param voicePath 语音文件路径
     */
    public void startVoice(Context context, String voicePath) {
        // 切换播放模式
        changePlayMode(mIsPhoneMode);
        mVoicePath = voicePath;
        boolean isMusic = mAudioManager.isMusicActive();
        int requestAudioFocusResult = 1;
        if (isMusic) {
            // 以闹钟的流类型获取焦点，这样占用结束之后，可以继续原有播放
            requestAudioFocusResult = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
        // 获取焦点成功，准备播放
        if (requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "request Audio Focus successfully.");
            try {
                File file = new File(voicePath);
                if (file.exists()) {
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        stopVoice();
                    }
                    if (!isPrepare) {
                        prepare(voicePath);
                    } else {
                        start();
                    }

                    if (null != mMediaPlayer) {
                        // 播放错误监听
                        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                stopVoice();
                                if (onVoiceFinishListener != null) {
                                    onVoiceFinishListener.onError();
                                }
                                return false;
                            }
                        });
                        if (mOnCompletionListener != null) {
                            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
                        }
                    }
                } else {
                    ToastUtil.showTextViewPrompt(context.getString(R.string.voice_not_exist));
                    // 语音不存在 返回播放完成
                    stopVoice();
                    if (onVoiceFinishListener != null) {
                        onVoiceFinishListener.onError();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "startVoice failed:" + e.getMessage());
                stopVoice();
                if (onVoiceFinishListener != null) {
                    onVoiceFinishListener.onError();
                }
            }
        } else {
            Log.e(TAG, "request Audio Focus failed.");
            stopVoice();
            if (onVoiceFinishListener != null) {
                onVoiceFinishListener.onFinish();
            }
        }

    }

    private void prepare(String path) {
        if (TextUtils.isEmpty(path)) {
            Log.d(TAG, "prepare ---> path is null");
            return;
        }
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            }
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        } catch (IOException e) {
            e.printStackTrace();
            if (onVoiceFinishListener != null) {
                onVoiceFinishListener.onError();
            }
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void seekTo(int msec) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(msec);
        }
    }

    public int getDuration() {
        if (mMediaPlayer == null) {
            return -1;
        }
        return mMediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        if (mMediaPlayer == null) {
            return -1;
        }
        return mMediaPlayer.getCurrentPosition();
    }

    public boolean isPlaying() {
        if (mMediaPlayer == null) {
            return false;
        }
        return mMediaPlayer.isPlaying();
    }

    /**
     * 继续播放
     */
    public void resume() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    /**
     * 停止播放语音
     */
    public void stopVoice() {
        try {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.release();
                mMediaPlayer = null;
                isPrepare = false;
            }
        } catch (Exception e) {
            // 系统在回收release时候release生效但mediaPlay并不为空
            // 所以在判断isPlaying时会抛IllegalStateException 此时需要至为空
            mMediaPlayer = null;
            isPrepare = false;
            Log.e(TAG, "stopVoice failed:" + e.getMessage());
        } finally {
            startOthersMusic();
        }
    }

    /**
     * 停止播放语音 不还原焦点
     */
    private void stopVoiceNotResetFocus() {
        try {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.release();
                mMediaPlayer = null;
                isPrepare = false;
            }
        } catch (Exception e) {
            // 系统在回收release时候release生效但mediaPlay并不为空
            // 所以在判断isPlaying时会抛IllegalStateException 此时需要至为空
            mMediaPlayer = null;
            isPrepare = false;
            Log.e(TAG, "stopVoice failed:" + e.getMessage());
        }
    }

    // 注册距离传感器
    public void registerListener() {
        mSensorManager.registerListener(mSensorEventListener, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        mIsRegisterSensor = true;
    }

    // 取消注册距离传感器
    public void unRegisterListener() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mSensorEventListener, mSensor);
            mIsRegisterSensor = false;
        }
    }

    public void setOnVoiceFinishListener(OnVoiceFinishListener onVoiceFinishListener) {
        this.onVoiceFinishListener = onVoiceFinishListener;
    }

    public void setOnVoicePrepareListener(OnVoicePrepareListener onVoicePrepareListener) {
        mOnVoicePrepareListener = onVoicePrepareListener;
    }

    /**
     * 切换播放模式
     *
     * @param isPhoneMode 是否是听筒模式
     */
    @SuppressWarnings("unchecked")
    private void changePlayMode(boolean isPhoneMode) {
        if (mAudioManager == null) {
            return;
        }
        if (isPhoneMode) {
            mIsPhoneMode = true;
            // 5.0以上MODE_IN_CALL不管用了，用沟MODE_IN_COMMUNICATION,如果某些机型5.0以下MODE_IN_CALL，不好用，则全部使用后者
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mAudioManager.getMode() != AudioManager.MODE_IN_COMMUNICATION) {
                    mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                }
                try {
                    Class clazz = Class.forName("android.media.AudioSystem");
                    // 设置某种场合强制使用某一设备
                    Method m = clazz.getMethod("setForceUse", Class[].class);
                    m.invoke(null, 1, 1);
                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException
                        | IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                if (mAudioManager.getMode() != AudioManager.MODE_IN_CALL) {
                    mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                }
            }
            // 关闭扬声器
            mAudioManager.setSpeakerphoneOn(false);
            // 听筒设置为通话音量
            mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL), 0);
        } else {
            mIsPhoneMode = false;
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            // 打开扬声器
            mAudioManager.setSpeakerphoneOn(true);
            // 外放设置为媒体音量
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC), 0);
        }
    }

    public interface OnVoiceFinishListener {
        // 播放结束
        void onFinish();

        void onStart();

        void onError();
    }

    public interface OnVoicePrepareListener {

        void onPrepareListener(MediaPlayer mp);
    }

}
